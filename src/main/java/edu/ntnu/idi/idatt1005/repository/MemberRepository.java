package edu.ntnu.idi.idatt1005.repository;

import edu.ntnu.idi.idatt1005.db.DatabaseConnector;
import edu.ntnu.idi.idatt1005.model.Member;
import edu.ntnu.idi.idatt1005.model.MemberStat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberRepository {

    public List<Member> fetchMembers(int userId) throws SQLException {
        List<Member> members = new ArrayList<>();
        String query = "SELECT id, name, user_id FROM members WHERE user_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int memberId = rs.getInt("user_id");

                Member member = new Member(id, name, memberId);
                members.add(member);
            }
        }
        return members;
    }

    public void addMember(Member member) throws SQLException {
        String query = "INSERT INTO members (name, user_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, member.getName());
            stmt.setInt(2, member.getUserId());

            stmt.executeUpdate();

            // Get the auto-generated ID
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                member.setId(generatedKeys.getInt(1));
            }
        }
    }

    public void removeMember(int memberID) throws SQLException {
        String query = "DELETE FROM members WHERE id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, memberID);
            stmt.executeUpdate();
        }
    }

    // In MemberRepository class
    public MemberStat fetchMemberStatistics(Member member) throws SQLException {
        // Change this query to use task_assignments instead of completed_tasks
        String query = "SELECT COUNT(*) AS total_tasks FROM task_assignments WHERE assigned_to = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, member.getId());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int totalTasks = rs.getInt("total_tasks");
                    // Since we don't have completion status yet, all tasks are ongoing
                    return new MemberStat(member.getId(), totalTasks, 0, totalTasks);
                }
            }
        }

        // Return default stats if no data found
        return new MemberStat(member.getId(), 0, 0, 0);
    }


}
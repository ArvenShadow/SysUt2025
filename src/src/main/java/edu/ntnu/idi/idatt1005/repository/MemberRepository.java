package edu.ntnu.idi.idatt1005.repository;

import edu.ntnu.idi.idatt1005.db.DatabaseConnector;
import edu.ntnu.idi.idatt1005.model.Member;
import edu.ntnu.idi.idatt1005.model.MemberStat;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for handling database operations related to members.
 * Provides methods for creating, retrieving, and deleting member records,
 * as well as managing member statistics.
 */
public class MemberRepository {

  /**
  * Default constructor for MemberCardController.
  */
  public MemberRepository() {

  }

  /**
   * Retrieves all members associated with a specific user ID from the database.
   *
   * @param userId the ID of the user whose members should be fetched
   * @return a list of Member objects associated with the specified user
   * @throws SQLException if a database access error occurs
   */
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

  /**
   * Adds a new member to the database.
   * After successful insertion, the member's ID is updated with the auto-generated database ID.
   *
   * @param member the Member object to be added to the database
   * @throws SQLException if a database access error occurs
   */
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

  /**
   * Removes a member and all associated data from the database.
   * This method employs a transaction to ensure all related records
   * (task assignments and statistics) are deleted along with the member.
   *
   * @param memberID the ID of the member to be removed
   * @throws SQLException if a database access error occurs
   */
  public void removeMember(int memberID) throws SQLException {
    String deleteAssignments = "DELETE FROM task_assignments WHERE assigned_to = ?";
    String deleteStats = "DELETE FROM member_stats WHERE member_id = ?";
    String deleteMember = "DELETE FROM members WHERE id = ?";

    try (Connection conn = DatabaseConnector.getConnection()) {
      conn.setAutoCommit(false);

      try (PreparedStatement stmt1 = conn.prepareStatement(deleteAssignments);
           PreparedStatement stmt2 = conn.prepareStatement(deleteStats);
           PreparedStatement stmt3 = conn.prepareStatement(deleteMember)) {

        stmt1.setInt(1, memberID);
        stmt1.executeUpdate();

        stmt2.setInt(1, memberID);
        stmt2.executeUpdate();

        stmt3.setInt(1, memberID);
        stmt3.executeUpdate();

        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    }
  }

  /**
   * Retrieves statistics for a specific member from the database.
   * If no statistics are found for the member, returns a default MemberStat
   * object with all counts initialized to zero.
   *
   * @param member the Member object whose statistics should be fetched
   * @return a MemberStat object containing the member's task statistics
   * @throws SQLException if a database access error occurs
   */
  public MemberStat fetchMemberStatistics(Member member) throws SQLException {
    String query =
            "SELECT ongoing_tasks, completed_tasks, total_tasks "
              + "FROM member_stats "
              + "WHERE member_id = ?";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement statement = conn.prepareStatement(query)) {

      statement.setInt(1, member.getId());
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          int ongoing = rs.getInt("ongoing_tasks");
          int completed = rs.getInt("completed_tasks");
          int total = rs.getInt("total_tasks");

          return new MemberStat(member.getId(), ongoing, completed, total);
        }
      }
    }

    // Return fallback if stats not found
    return new MemberStat(member.getId(), 0, 0, 0);
  }



}
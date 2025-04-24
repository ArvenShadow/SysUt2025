package edu.ntnu.idi.idatt1005.repository;

import edu.ntnu.idi.idatt1005.db.DatabaseConnector;
import edu.ntnu.idi.idatt1005.model.Task;
import edu.ntnu.idi.idatt1005.model.TaskPriority;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing task data in the database.
 *
 * <p>This class provides methods for creating, retrieving, updating, and deleting tasks.
 * It handles database interactions for task operations, including task assignments
 * to members and task completion tracking.
 */
public class TaskRepository {

  /**
  * Default constructor for TaskRepository.
  */
  public TaskRepository() {
    // Default constructor implementation
  }

  /**
   * Retrieves all tasks associated with a specific user.
   *
   * @param userId The ID of the user whose tasks to fetch
   * @return List of Task objects belonging to the specified user
   * @throws SQLException if a database error occurs during the operation
   */
  public List<Task> fetchTasks(int userId) throws SQLException {
    String query =
        "SELECT task_id, description, due_date, priority, details, responsibility "
          + "FROM tasks WHERE user_id = ?";

    List<Task> tasks = new ArrayList<>();

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement statement = conn.prepareStatement(query)) {
      statement.setInt(1, userId);
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          int taskId = resultSet.getInt("task_id");
          String description = resultSet.getString("description");
          LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
          TaskPriority priority = TaskPriority.valueOf(resultSet.getString("priority"));
          String details = resultSet.getString("details");
          String responsibility = resultSet.getString("responsibility");

          Task task = new Task(taskId, description, dueDate, priority, details, responsibility);
          tasks.add(task);

        }
      }
    } catch (SQLException e) {
      System.err.println("Failed to fetch tasks: " + e.getMessage());
      e.printStackTrace();
    }
    return tasks;
  }

  /**
   * Retrieves all tasks assigned to a specific member.
   *
   * @param memberId The ID of the member whose assigned tasks to fetch
   * @return List of Task objects assigned to the specified member
   * @throws SQLException if a database error occurs during the operation
   */
  public List<Task> fetchTasksAssignedToMember(int memberId) throws SQLException {
    String query = """
    SELECT task_id, description, due_date, priority, details, responsibility
    FROM tasks
    WHERE member_id = ?
        """;

    List<Task> tasks = new ArrayList<>();

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

      stmt.setInt(1, memberId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          int taskId = rs.getInt("task_id");
          String description = rs.getString("description");
          LocalDate dueDate = rs.getDate("due_date").toLocalDate();
          TaskPriority priority = TaskPriority.valueOf(rs.getString("priority"));
          String details = rs.getString("details");
          String responsibility = rs.getString("responsibility");

          Task task = new Task(taskId, description, dueDate, priority, details, responsibility);
          tasks.add(task);
        }
      }
    }

    return tasks;
  }

  /**
   * Creates a new task associated with a user in the database.
   *
   * @param task The Task object containing task details to create
   * @param userId The ID of the user who owns the task
   * @return The generated task ID for the newly created task
   * @throws SQLException if a database error occurs or if task creation fails
   */
  public int createTask(Task task, int userId) throws SQLException {
    String query = """
        INSERT INTO tasks (description, due_date, priority, details, user_id, responsibility)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement statement =
            conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, task.getDescription());
      statement.setDate(2, java.sql.Date.valueOf(task.getDueDate()));
      statement.setString(3, task.getPriority().toString());
      statement.setString(4, task.getDetails());
      statement.setInt(5, userId);
      statement.setString(6, task.getResponsibility());

      statement.executeUpdate();

      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (keys.next()) {
          return keys.getInt(1);
        } else {
          throw new SQLException("Creating task failed, no ID obtained.");
        }
      }
    }
  }


  /**
   * Creates a task and assigns it to a member in a single transaction.
   * Also updates member statistics to reflect the new task assignment.
   *
   * @param task The Task object containing task details to create
   * @param createdBy User ID of the task creator
   * @param assignedTo Member ID who will be assigned to the task
   * @return The generated task ID, or -1 if the operation failed
   */
  public int createTaskForMember(Task task, int createdBy, int assignedTo) {
    Connection conn = null;
    try {
      conn = DatabaseConnector.getConnection();
      conn.setAutoCommit(false);

      String query =
          "INSERT INTO tasks "
          + "(description, due_date, priority, details, user_id, responsibility, member_id) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?)";

      int taskId;
      try (PreparedStatement stmt =
             conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, task.getDescription());
        stmt.setDate(2, java.sql.Date.valueOf(task.getDueDate()));
        stmt.setString(3, task.getPriority().toString());
        stmt.setString(4, task.getDetails());
        stmt.setInt(5, createdBy);
        stmt.setString(6, task.getResponsibility());
        stmt.setInt(7, assignedTo); // ✅ KEY: storing member_id directly in tasks table

        stmt.executeUpdate();

        try (ResultSet keys = stmt.getGeneratedKeys()) {
          if (keys.next()) {
            taskId = keys.getInt(1);
          } else {
            throw new SQLException("Task insert failed, no ID returned.");
          }
        }
      }

      // Update member stats directly
      String statsQuery =
          "INSERT INTO member_stats (member_id, ongoing_tasks, completed_tasks, total_tasks) "
          + "VALUES (?, 1, 0, 1) "
          + "ON DUPLICATE KEY UPDATE ongoing_tasks ="
            + " ongoing_tasks + 1, total_tasks = total_tasks + 1";

      try (PreparedStatement stmt = conn.prepareStatement(statsQuery)) {
        stmt.setInt(1, assignedTo);
        stmt.executeUpdate();
      }

      conn.commit();
      return taskId;

    } catch (SQLException e) {
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException rollbackEx) {
          rollbackEx.printStackTrace();
        }
      }
      System.err.println("Failed to create task for member: " + e.getMessage());
      return -1;
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Retrieves the ID of the member assigned to a specific task.
   *
   * @param taskId The ID of the task to check
   * @return The assigned member's ID, or null if no member is assigned
   * @throws SQLException if a database error occurs during the operation
   */
  public Integer getAssignedMemberId(int taskId) throws SQLException {
    String query = "SELECT member_id FROM tasks WHERE task_id = ?";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

      stmt.setInt(1, taskId);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          int memberId = rs.getInt("member_id");
          // Check if it was NULL in the database
          return rs.wasNull() ? null : memberId;
        }
      }
    }
    return null; // No assignment found
  }

  /**
   * Marks a task as completed for a member and updates member statistics.
   * The task remains in the system but is no longer assigned to the member.
   *
   * @param taskId The ID of the task to mark as completed
   * @param memberId The ID of the member who completed the task
   * @throws SQLException if a database error occurs during the operation
   */
  public void completeTaskForMember(int taskId, int memberId) throws SQLException {
    Connection conn = null;
    try {
      conn = DatabaseConnector.getConnection();
      conn.setAutoCommit(false);

      // Update member stats to increase completed tasks and decrease ongoing tasks
      String updateStats = "UPDATE member_stats SET completed_tasks = completed_tasks + 1, "
          + "ongoing_tasks = ongoing_tasks - 1 WHERE member_id = ?";

      // Clear the member assignment from the task
      String clearMemberAssignment = "UPDATE tasks SET member_id = NULL WHERE task_id = ?";

      try (PreparedStatement stmt1 = conn.prepareStatement(updateStats);
           PreparedStatement stmt2 = conn.prepareStatement(clearMemberAssignment)) {

        stmt1.setInt(1, memberId);
        stmt1.executeUpdate();

        stmt2.setInt(1, taskId);
        stmt2.executeUpdate();

        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Deletes a task using an existing database connection.
   * Used primarily as part of transaction operations.
   *
   * @param conn The active database connection to use
   * @param taskID The ID of the task to delete
   * @throws SQLException if a database error occurs during the operation
   */
  public void deleteTask(Connection conn, int taskID) throws SQLException {
    String query = "DELETE FROM tasks WHERE task_id = ?";
    try (PreparedStatement statement = conn.prepareStatement(query)) {
      statement.setInt(1, taskID);
      statement.executeUpdate();
    }
  }

  /**
   * Deletes a task and its assignments from the database.
   * This method handles the database connection and transaction management.
   *
   * @param taskId The ID of the task to delete
   * @throws SQLException if a database error occurs during the operation
   */
  public void deleteTask(int taskId) throws SQLException {
    Connection conn = null;
    try {
      conn = DatabaseConnector.getConnection();
      conn.setAutoCommit(false);

      // First delete any task assignments
      String assignmentQuery = "DELETE FROM task_assignments WHERE task_id = ?";
      try (PreparedStatement statement = conn.prepareStatement(assignmentQuery)) {
        statement.setInt(1, taskId);
        statement.executeUpdate();
      }

      // Then delete the task
      String taskQuery = "DELETE FROM tasks WHERE task_id = ?";
      try (PreparedStatement statement = conn.prepareStatement(taskQuery)) {
        statement.setInt(1, taskId);
        statement.executeUpdate();
      }

      conn.commit();
    } catch (SQLException e) {
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      throw e;
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Completes a task and updates all relevant statistics in one transaction.
   *
   * @param taskId ID of the task to complete
   * @param userId ID of the user completing the task
   * @throws SQLException if a database error occurs
   */
  public void completeTask(int taskId, int userId) throws SQLException {
    Connection conn = null;
    try {
      conn = DatabaseConnector.getConnection();
      conn.setAutoCommit(false);

      // Check if task is assigned to a member
      String checkMemberQuery = "SELECT member_id FROM tasks WHERE task_id = ?";
      Integer memberId = null;
      try (PreparedStatement stmt = conn.prepareStatement(checkMemberQuery)) {
        stmt.setInt(1, taskId);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            memberId = rs.getInt("member_id");
            if (rs.wasNull()) {
              memberId = null;
            }
          }
        }
      }

      // Update member stats if assigned
      if (memberId != null) {
        String updateMemberStatsQuery = "UPDATE member_stats SET completed_tasks = completed_tasks + 1, "
          + "ongoing_tasks = ongoing_tasks - 1 WHERE member_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateMemberStatsQuery)) {
          stmt.setInt(1, memberId);
          stmt.executeUpdate();
        }
      }

      // Update general statistics
      String updateStatsQuery = "UPDATE statistics SET completed_tasks = completed_tasks + 1 WHERE user_id = ?";
      try (PreparedStatement stmt = conn.prepareStatement(updateStatsQuery)) {
        stmt.setInt(1, userId);
        int rowsAffected = stmt.executeUpdate();

        // If no rows affected, create a new statistics record
        if (rowsAffected == 0) {
          String insertStatsQuery = "INSERT INTO statistics (user_id, completed_tasks) VALUES (?, 1)";
          try (PreparedStatement insertStmt = conn.prepareStatement(insertStatsQuery)) {
            insertStmt.setInt(1, userId);
            insertStmt.executeUpdate();
          }
        }
      }

      // Record the completion in history table
      String recordHistoryQuery = "INSERT INTO task_completion_history (user_id, task_id) VALUES (?, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(recordHistoryQuery)) {
        stmt.setInt(1, userId);
        stmt.setInt(2, taskId);
        stmt.executeUpdate();
      }

      // Delete the task
      String deleteTaskQuery = "DELETE FROM tasks WHERE task_id = ?";
      try (PreparedStatement stmt = conn.prepareStatement(deleteTaskQuery)) {
        stmt.setInt(1, taskId);
        stmt.executeUpdate();
      }

      conn.commit();
    } catch (SQLException e) {
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      throw e;
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
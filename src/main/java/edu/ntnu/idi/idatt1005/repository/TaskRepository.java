package edu.ntnu.idi.idatt1005.repository;

import edu.ntnu.idi.idatt1005.db.DatabaseConnector;
import edu.ntnu.idi.idatt1005.model.Task;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import edu.ntnu.idi.idatt1005.model.TaskPriority;

public class TaskRepository {

  public List<Task> fetchTasks(int userId) throws SQLException {
    String query = "SELECT task_id, description, due_date, priority, details FROM tasks WHERE user_id = ?";

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

          Task task = new Task(taskId, description, dueDate, priority, details);
          tasks.add(task);

        }
      }
    } catch (SQLException e) {
      System.err.println("Failed to fetch tasks: " + e.getMessage());
      e.printStackTrace();
    }
    return tasks;
  }

  public List<Task> fetchTasksAssignedToMember(int memberId) throws SQLException {
    String query = "SELECT t.task_id, t.description, t.due_date, t.priority, t.details " +
            "FROM tasks t " +
            "JOIN task_assignments ta ON t.task_id = ta.task_id " +
            "WHERE ta.assigned_to = ?";

    List<Task> tasks = new ArrayList<>();

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement statement = conn.prepareStatement(query)) {
      statement.setInt(1, memberId);
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          int taskId = resultSet.getInt("task_id");
          String description = resultSet.getString("description");
          LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
          TaskPriority priority = TaskPriority.valueOf(resultSet.getString("priority"));
          String details = resultSet.getString("details");

          Task task = new Task(taskId, description, dueDate, priority, details);
          tasks.add(task);
        }
      }
    } catch (SQLException e) {
      System.err.println("Failed to fetch tasks assigned to member: " + e.getMessage());
      throw e;
    }
    return tasks;
  }

  public int createTask(Task task, int userId) throws SQLException {
    String query = "INSERT INTO tasks (description, due_date, priority, details, user_id )"
            + "VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement statement = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);) {
      statement.setString(1, task.getDescription());
      statement.setDate(2, java.sql.Date.valueOf(task.getDueDate()));
      statement.setString(3, task.getPriority().toString());
      statement.setString(4, task.getDetails());
      statement.setInt(5, userId);
      statement.executeUpdate();
      try (ResultSet generatedKeys = statement
              .getGeneratedKeys()) {
        if (generatedKeys.next()) {
          return generatedKeys.getInt(1);
        } else {
          throw new SQLException("Creating task failed successfully, no ID obtained.");
        }
      }
    }
  }

  /**
   * Creates a task and assigns it to a member in one transaction
   * @param task The task to create
   * @param createdBy User ID who creates the task
   * @param assignedTo Member ID who will be assigned the task
   * @return The ID of the created task, or -1 if creation failed
   */
  public int createTaskForMember(Task task, int createdBy, int assignedTo) {
    Connection conn = null;
    try {
      conn = DatabaseConnector.getConnection();
      conn.setAutoCommit(false);

      // First create the task
      String taskQuery = "INSERT INTO tasks (description, due_date, priority, details, user_id) " +
              "VALUES (?, ?, ?, ?, ?)";

      int taskId;
      try (PreparedStatement statement = conn.prepareStatement(taskQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
        statement.setString(1, task.getDescription());
        statement.setDate(2, java.sql.Date.valueOf(task.getDueDate()));
        statement.setString(3, task.getPriority().toString());
        statement.setString(4, task.getDetails());
        statement.setInt(5, createdBy); // Created by the main user

        statement.executeUpdate();

        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            taskId = generatedKeys.getInt(1);
          } else {
            throw new SQLException("Creating task failed, no ID obtained.");
          }
        }
      }

      // Then create the assignment record
      String assignQuery = "INSERT INTO task_assignments (task_id, assigned_by, assigned_to) " +
              "VALUES (?, ?, ?)";

      try (PreparedStatement statement = conn.prepareStatement(assignQuery)) {
        statement.setInt(1, taskId);
        statement.setInt(2, createdBy);
        statement.setInt(3, assignedTo);
        statement.executeUpdate();
      }

      // Update member stats
      String statsQuery = "UPDATE member_stats SET ongoing_tasks = ongoing_tasks + 1, " +
              "total_tasks = total_tasks + 1 WHERE member_id = ?";

      try (PreparedStatement statement = conn.prepareStatement(statsQuery)) {
        statement.setInt(1, assignedTo);
        statement.executeUpdate();
      }

      conn.commit();
      return taskId;

    } catch (SQLException e) {
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      System.err.println("Failed to create task for member: " + e.getMessage());
      e.printStackTrace();
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



  public void updateTask() {

  }

  public void deleteTask(Connection conn, int taskID) throws SQLException {
    String query = "DELETE FROM tasks WHERE task_id = ?";
    try (PreparedStatement statement = conn.prepareStatement(query)) {
      statement.setInt(1, taskID);
      statement.executeUpdate();
    }
  }


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
}
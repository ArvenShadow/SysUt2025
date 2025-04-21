package edu.ntnu.idi.idatt1005.repository;

import edu.ntnu.idi.idatt1005.db.DatabaseConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.WeekFields;

/**
 * Repository class for managing task completion statistics and history.
 * This class provides methods for recording task completions, retrieving
 * statistical data, and analyzing task completion trends over time.
 * It interacts with the database to store and retrieve statistical information.
 */
public class StatisticsRepository {

  private final TaskRepository taskRepository;

  /**
   * Constructs a new StatisticsRepository with a TaskRepository dependency.
   * The TaskRepository is used for task-related operations during statistics management.
   */
  public StatisticsRepository() {
    this.taskRepository = new TaskRepository();
  }

  /**
   * Completes a task and updates relevant statistics in the database.
   * This method performs three operations in a single transaction:
   * 1. Deletes the completed task using the TaskRepository
   * 2. Updates the user's completed task count in the statistics table
   * 3. Records the completion in the task history table
   *
   * @param taskId ID of the task being completed
   * @param userId ID of the user completing the task
   * @throws SQLException if a database error occurs during the transaction
   */
  public void completeTask(int taskId, int userId) throws SQLException {
    // Update statistics query
    String incrementStatQuery =
        "UPDATE statistics SET completed_tasks = completed_tasks + 1 WHERE user_id = ?";

    // Record history query - this is the key addition
    String recordHistoryQuery =
        "INSERT INTO task_completion_history (user_id, task_id) VALUES (?, ?)";

    try (Connection conn = DatabaseConnector.getConnection()) {
      // Start transaction
      conn.setAutoCommit(false);

      try {
        // Delete the task first
        taskRepository.deleteTask(conn, taskId);

        // Update overall statistics
        try (PreparedStatement incrementStatStatement = conn.prepareStatement(incrementStatQuery)) {
          incrementStatStatement.setInt(1, userId);

          int rowsAffected = incrementStatStatement.executeUpdate();

          // If no rows affected, create a new statistics record
          if (rowsAffected == 0) {
            insertNewStatisticsRecord(conn, userId);
          }
        }

        // Record the completion in history table (the key addition)
        try (PreparedStatement historyStatement = conn.prepareStatement(recordHistoryQuery)) {
          historyStatement.setInt(1, userId);
          historyStatement.setInt(2, taskId);
          historyStatement.executeUpdate();
        }

        // Commit the transaction
        conn.commit();

      } catch (SQLException e) {
        // Rollback on error
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    }
  }

  /**
   * Inserts a new statistics record for a user in the database.
   * This method is called when a user completes their first task and
   * doesn't yet have an entry in the statistics table.
   *
   * @param conn the database connection to use for the operation
   * @param userId the ID of the user to create a statistics record for
   * @throws SQLException if a database error occurs during insertion
   */
  private void insertNewStatisticsRecord(Connection conn, int userId) throws SQLException {
    String insertQuery = "INSERT INTO statistics (user_id, completed_tasks) VALUES (?, 1)";

    try (PreparedStatement insertStatement = conn.prepareStatement(insertQuery)) {
      insertStatement.setInt(1, userId);
      insertStatement.executeUpdate();
    }
  }

  /**
   * Retrieves weekly task completion counts for a specified period.
   * Returns data for a number of consecutive weeks, working backwards from the current week.
   * The results are formatted as a 2D array where each row contains [weekNumber, completionCount].
   *
   * @param userId ID of the user whose statistics should be retrieved
   * @param weekCount number of weeks to retrieve data for (including current week)
   * @return a 2D array where each row contains [weekNumber, completionCount]
   * @throws SQLException if a database error occurs during the query
   */
  public int[][] getWeeklyTaskCompletions(int userId, int weekCount) throws SQLException {
    // Use ISO week definition (Monday as first day of week)
    LocalDate now = LocalDate.now();
    WeekFields weekFields = WeekFields.ISO;
    int currentWeek = now.get(weekFields.weekOfYear());
    int currentYear = now.getYear();

    System.out.println("Current ISO week: " + currentWeek);

    // We need to find weeks in reverse order - starting with currentWeek and going back
    // Pre-allocate result array
    int[][] results = new int[weekCount][2];

    // Fill the array with week numbers in reverse order
    for (int i = 0; i < weekCount; i++) {
      int relativeWeek = currentWeek - i;
      int yearToUse = currentYear;

      // Handle year boundary
      if (relativeWeek <= 0) {
        yearToUse--;
        relativeWeek += weeksInYear(yearToUse);
      }

      // Store week number in results array (in reverse order)
      results[weekCount - 1 - i][0] = relativeWeek;
      results[weekCount - 1 - i][1] = 0; // Default to 0 completions
    }

    // Query to get task counts for all weeks we're interested in
    // We'll get all data for the period and then map it to our results
    String query = "SELECT "
        + "WEEK(completed_at, 3) as week_num, "
        + "YEAR(completed_at) as year_num, "
        + "COUNT(*) as task_count "
        + "FROM task_completion_history "
        + "WHERE user_id = ? "
        + "AND completed_at >= DATE_SUB(?, INTERVAL ? WEEK) "
        + "GROUP BY YEAR(completed_at), WEEK(completed_at, 3) "
        + "ORDER BY YEAR(completed_at) DESC, WEEK(completed_at, 3) DESC";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

      stmt.setInt(1, userId);
      stmt.setObject(2, now); // Current date as reference point
      stmt.setInt(3, weekCount - 1); // Go back this many weeks

      try (ResultSet resultSet = stmt.executeQuery()) {
        // Fill in actual counts from database
        while (resultSet.next()) {
          int weekNum = resultSet.getInt("week_num");
          int yearNum = resultSet.getInt("year_num");
          int count = resultSet.getInt("task_count");

          System.out.println("Result: Year=" + yearNum + ", Week=" + weekNum + ", Count=" + count);

          // Find matching week in our results array
          for (int i = 0; i < weekCount; i++) {
            if (results[i][0] == weekNum) {
              // Found matching week number
              results[i][1] = count;
              break;
            }
          }
        }

        return results;
      }
    }
  }

  /**
   * Calculates the number of weeks in a given year according to ISO standard.
   * A year has 52 or 53 weeks depending on when January 1st falls.
   *
   * @param year the year to check
   * @return the number of weeks in the specified year
   */
  private int weeksInYear(int year) {
    LocalDate date = LocalDate.of(year, 12, 31); // Last day of the year
    WeekFields weekFields = WeekFields.ISO;
    return date.get(weekFields.weekOfWeekBasedYear());
  }

  /**
   * Retrieves the count of tasks completed in the current week for a specific user.
   * Uses the ISO week definition (Monday as the first day of the week).
   *
   * @param userId ID of the user whose statistics should be retrieved
   * @return the number of tasks completed this week
   * @throws SQLException if a database error occurs during the query
   */
  public int getTasksCompletedThisWeek(int userId) throws SQLException {
    // Using ISO week definition (Monday first day of week)
    LocalDate now = LocalDate.now();

    // Get the week number to ensure consistency
    WeekFields weekFields = WeekFields.ISO;
    int currentWeek = now.get(weekFields.weekOfYear());
    int currentYear = now.getYear();

    // We can use the WEEK() function in MySQL with mode 3 for ISO week numbering
    String query = "SELECT COUNT(*) FROM task_completion_history "
        + "WHERE user_id = ? AND "
        + "YEAR(completed_at) = ? AND WEEK(completed_at, 3) = ?";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

      stmt.setInt(1, userId);
      stmt.setInt(2, currentYear);
      stmt.setInt(3, currentWeek);

      try (ResultSet resultSet = stmt.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getInt(1);
        }
        return 0;
      }
    }
  }
}
package edu.ntnu.idi.idatt1005.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class DatabaseInitializer {

  /**
   * This method runs during program startup to ensure the database is initialized and
   * kept up-to-date (e.g., ensuring all users exist in statistics, etc.).
   */
  public void initializeDatabase() {
    System.out.println("Initializing database...");
    try {

      initializeStatistics();

      System.out.println("Database initialization completed successfully!");
    } catch (Exception e) {
      System.err.println("Database initialization failed: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void initializeStatistics() throws SQLException {
    String updateStatQuery = "INSERT INTO statistics (user_id, completed_tasks) " +
      "SELECT user_id, 0 " +
      "FROM users WHERE user_id NOT IN (SELECT user_id FROM statistics);";

    try (Connection conn = DatabaseConnector.getConnection()) {
      try (PreparedStatement updateStatStatement = conn.prepareStatement(updateStatQuery)) {

        int rowsUpdated = updateStatStatement.executeUpdate();
        if (rowsUpdated > 0) {
          System.out.println(rowsUpdated + " new users added to statistics");
        } else {
          System.out.println("no new users to add to statistics");
        }
      }
    } catch (SQLException e) {
      System.err.println("Failed to initialize statistics " + e.getMessage());
      e.printStackTrace();
    }
  }

}
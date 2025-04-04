package edu.ntnu.idi.idatt1005.repository;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.db.DatabaseConnector;

import java.sql.*;

/**
 * Repository class for retrieving user data from the database.
 * Handles login/authentication and user-related lookups.
 *
 * This version returns a {@link User} object instead of using static userId or username references.
 *
 * Author: KrissKN
 */
public class UserRepository {

  /**
   * Authenticates a user by username and password.
   *
   * @param username the entered username
   * @param password the entered password
   * @return the authenticated {@link User}, or null if authentication fails
   * @throws SQLException if a database access error occurs
   */
  public User authenticateUser(String username, String password) throws SQLException {
    String query = "SELECT user_id, username FROM users WHERE username = ? AND password = ?";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement statement = conn.prepareStatement(query)) {

      statement.setString(1, username);
      statement.setString(2, password);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          int userId = resultSet.getInt("user_id");
          String userName = resultSet.getString("username");

          return new User(userId, username);
        } else {
          return null;
        }
      }
    }

  }
  public boolean createUser(String username, String password) throws SQLException {
    String query = "INSERT INTO users (username, password) VALUES (?, ?)";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, username);
      stmt.setString(2, password);

      return stmt.executeUpdate() > 0;
    }
  }

}

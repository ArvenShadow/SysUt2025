package edu.ntnu.idi.idatt1005.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton-style utility class for managing database connections.
 * Reads connection settings from a properties file and ensures a shared
 * connection instance across the application.
 *
 * <p>This class is used by repositories like {@code UserRepository} and {@code TaskRepository}
 */
public class DatabaseConnector {

  private static Connection connection;
  private static String url;
  private static String user;
  private static String password;

  // Static block loads credentials when class is first used
  static {
    try {
      Properties properties = new Properties();
      InputStream input = DatabaseConnector.class.getResourceAsStream("/db.properties");
      if (input == null) {
        System.err.println("Unable to find db.properties");
        // Use default values or handle the error
      } else {
        properties.load(input);
        url = properties.getProperty("db.url");
        user = properties.getProperty("db.user");
        password = properties.getProperty("db.password");
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to load database properties", e);
    }
  }

  // Private constructor prevents instantiation
  private DatabaseConnector() {}

  /**
   * Returns a singleton database connection.
   * If the connection is closed or null, a new one is established.
   *
   * @return a valid Connection to the MySQL database
   * @throws SQLException if a database access error occurs
   */
  public static Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
      try {
        Class.forName("com.mysql.cj.jdbc.Driver");
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("MySQL JDBC Driver not found", e);
      }
      connection = DriverManager.getConnection(url, user, password);
    }
    return connection;
  }

  /**
   * Closes the current database connection if it's open.
   *
   * @throws SQLException if a database access error occurs
   */
  public static void closeConnection() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      connection.close();
    }
  }
}

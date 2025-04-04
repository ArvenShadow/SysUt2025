package edu.ntnu.idi.idatt1005;

import edu.ntnu.idi.idatt1005.db.DatabaseConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectorTest {

  private Connection connection;


  @BeforeAll
  public static void setupTestDatabase() throws SQLException {
    Connection connection = DatabaseConnector.getConnection();
    // Setup test database schema if needed
    // e.g., create test tables, insert test data
  }

  @AfterEach
  public void tearDown() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      DatabaseConnector.closeConnection();
    }
  }

  @Test
  public void testConnectionEstablished() {
    assertDoesNotThrow(() -> {
      connection = DatabaseConnector.getConnection();
      assertNotNull(connection, "Connection should not be null");
      assertFalse(connection.isClosed(), "Connection should be open");
    });
  }

  @Test
  public void testConnectionReuse() throws SQLException {
    connection = DatabaseConnector.getConnection();
    Connection reusedConnection = DatabaseConnector.getConnection();
    assertSame(connection, reusedConnection, "Connections should be the same instance");
  }

  @Test
  public void testCloseConnection() throws SQLException {
    connection = DatabaseConnector.getConnection();
    DatabaseConnector.closeConnection();
    assertTrue(connection.isClosed(), "Connection should be closed after calling closeConnection");
  }
}
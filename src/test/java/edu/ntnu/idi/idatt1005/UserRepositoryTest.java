package edu.ntnu.idi.idatt1005;

import edu.ntnu.idi.idatt1005.db.DatabaseConnector;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryTest {

  private static UserRepository userRepository;
  private static final String TEST_USERNAME = "testuser_" + System.currentTimeMillis();
  private static final String TEST_PASSWORD = "password123";
  private static int createdUserId = -1;

  @BeforeAll
  static void setUp() throws SQLException {
    userRepository = new UserRepository();
    cleanupTestUser(); // Clean up any leftover test data
  }

  @AfterAll
  static void tearDown() throws SQLException {
    cleanupTestUser();
    DatabaseConnector.closeConnection();
  }

  private static void cleanupTestUser() throws SQLException {
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE username = ?");
    stmt.setString(1, TEST_USERNAME);
    stmt.executeUpdate();
  }

  @Test
  @Order(1)
  void testCreateUser() throws SQLException {
    // Act
    boolean created = userRepository.createUser(TEST_USERNAME, TEST_PASSWORD);

    // Assert
    assertTrue(created, "User creation should return true");

    // Verify user exists in database
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users WHERE username = ?");
    stmt.setString(1, TEST_USERNAME);
    ResultSet rs = stmt.executeQuery();

    assertTrue(rs.next());
    createdUserId = rs.getInt("user_id");
    assertTrue(createdUserId > 0);
  }

  @Test
  @Order(2)
  void testAuthenticateValidUser() throws SQLException {
    // Arrange - test user should be created in previous test

    // Act
    User user = userRepository.authenticateUser(TEST_USERNAME, TEST_PASSWORD);

    // Assert
    assertNotNull(user);
    assertEquals(TEST_USERNAME, user.getUsername());
    assertEquals(createdUserId, user.getId());
  }

  @Test
  @Order(3)
  void testAuthenticateInvalidPassword() throws SQLException {
    // Act
    User user = userRepository.authenticateUser(TEST_USERNAME, "wrong_password");

    // Assert
    assertNull(user);
  }

  @Test
  @Order(4)
  void testAuthenticateNonexistentUser() throws SQLException {
    // Act
    User user = userRepository.authenticateUser("nonexistent_user", TEST_PASSWORD);

    // Assert
    assertNull(user);
  }

  @Test
  @Order(5)
  void testCreateDuplicateUser() {
    // Arrange - test user already exists

    // Act & Assert
    assertThrows(SQLException.class, () -> {
      userRepository.createUser(TEST_USERNAME, "different_password");
    });
  }

  @Test
  @Order(6)
  void testCreateUserWithEmptyUsername() {
    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      userRepository.createUser("", TEST_PASSWORD);
    });
  }
}
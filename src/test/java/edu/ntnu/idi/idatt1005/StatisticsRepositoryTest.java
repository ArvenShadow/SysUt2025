package edu.ntnu.idi.idatt1005;

import edu.ntnu.idi.idatt1005.db.DatabaseConnector;
import edu.ntnu.idi.idatt1005.model.Task;
import edu.ntnu.idi.idatt1005.model.TaskPriority;
import edu.ntnu.idi.idatt1005.repository.StatisticsRepository;
import edu.ntnu.idi.idatt1005.repository.TaskRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StatisticsRepositoryTest {

  private static StatisticsRepository statisticsRepository;
  private static TaskRepository taskRepository;
  private static int testUserId;
  private static int testTaskId;

  @BeforeAll
  static void setUp() throws SQLException {
    statisticsRepository = new StatisticsRepository();
    taskRepository = new TaskRepository();

    // Create test user and clean up any existing data
    Connection conn = DatabaseConnector.getConnection();

    // Clean up existing test data if any
    PreparedStatement cleanupStats = conn.prepareStatement(
      "DELETE FROM statistics WHERE user_id IN (SELECT user_id FROM users WHERE username = 'statstest')");
    cleanupStats.executeUpdate();

    PreparedStatement cleanupUser = conn.prepareStatement(
      "DELETE FROM users WHERE username = 'statstest'");
    cleanupUser.executeUpdate();

    // Create test user
    PreparedStatement createUser = conn.prepareStatement(
      "INSERT INTO users (username, password) VALUES ('statstest', 'password')",
      PreparedStatement.RETURN_GENERATED_KEYS);
    createUser.executeUpdate();

    ResultSet keys = createUser.getGeneratedKeys();
    if (keys.next()) {
      testUserId = keys.getInt(1);
    } else {
      throw new AssertionError("Failed to create test user");
    }
  }

  @AfterAll
  static void tearDown() throws SQLException {
    // Clean up test data
    Connection conn = DatabaseConnector.getConnection();

    PreparedStatement deleteHistory = conn.prepareStatement(
      "DELETE FROM task_completion_history WHERE user_id = ?");
    deleteHistory.setInt(1, testUserId);
    deleteHistory.executeUpdate();

    PreparedStatement deleteStats = conn.prepareStatement(
      "DELETE FROM statistics WHERE user_id = ?");
    deleteStats.setInt(1, testUserId);
    deleteStats.executeUpdate();

    PreparedStatement deleteUser = conn.prepareStatement(
      "DELETE FROM users WHERE user_id = ?");
    deleteUser.setInt(1, testUserId);
    deleteUser.executeUpdate();

    DatabaseConnector.closeConnection();
  }

  @Test
  @Order(1)
  void testInitialTasksCompletedThisWeek() throws SQLException {
    // Act
    int count = statisticsRepository.getTasksCompletedThisWeek(testUserId);

    // Assert
    assertEquals(0, count);
  }

  @Test
  @Order(2)
  void testCreateAndCompleteTask() throws SQLException {
    // Arrange - create a test task
    Task task = new Task(
      "Statistics Test Task",
      LocalDate.now(),
      TaskPriority.MEDIUM,
      "Test task for statistics testing",
      "Test User"
    );

    testTaskId = taskRepository.createTask(task, testUserId);
    assertTrue(testTaskId > 0);

    // Act - complete the task
    statisticsRepository.completeTask(testTaskId, testUserId);

    // Assert - check statistics were updated
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement stmt = conn.prepareStatement(
      "SELECT completed_tasks FROM statistics WHERE user_id = ?");
    stmt.setInt(1, testUserId);
    ResultSet rs = stmt.executeQuery();

    assertTrue(rs.next());
    assertEquals(1, rs.getInt("completed_tasks"));

    // Also check history table
    PreparedStatement historyStmt = conn.prepareStatement(
      "SELECT COUNT(*) FROM task_completion_history WHERE user_id = ? AND task_id = ?");
    historyStmt.setInt(1, testUserId);
    historyStmt.setInt(2, testTaskId);
    ResultSet historyRs = historyStmt.executeQuery();

    assertTrue(historyRs.next());
    assertEquals(1, historyRs.getInt(1));
  }

  @Test
  @Order(3)
  void testTasksCompletedThisWeekAfterCompletion() throws SQLException {
    // Act
    int count = statisticsRepository.getTasksCompletedThisWeek(testUserId);

    // Assert
    assertEquals(1, count);
  }

  @Test
  @Order(4)
  void testWeeklyTaskCompletions() throws SQLException {
    // Act
    int[][] weeklyStats = statisticsRepository.getWeeklyTaskCompletions(testUserId, 2);

    // Assert
    assertNotNull(weeklyStats);
    assertEquals(2, weeklyStats.length);

    // Current week should have 1 completion
    assertEquals(1, weeklyStats[1][1]);
  }

  @Test
  @Order(5)
  void testCompleteMultipleTasks() throws SQLException {
    // Arrange - create and complete 2 more tasks
    for (int i = 0; i < 2; i++) {
      Task task = new Task(
        "Multiple Task " + i,
        LocalDate.now(),
        TaskPriority.LOW,
        "Testing multiple completions",
        "Test User"
      );

      int taskId = taskRepository.createTask(task, testUserId);
      statisticsRepository.completeTask(taskId, testUserId);
    }

    // Act
    int count = statisticsRepository.getTasksCompletedThisWeek(testUserId);

    // Assert
    assertEquals(3, count);

    // Also verify in database
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement stmt = conn.prepareStatement(
      "SELECT completed_tasks FROM statistics WHERE user_id = ?");
    stmt.setInt(1, testUserId);
    ResultSet rs = stmt.executeQuery();

    assertTrue(rs.next());
    assertEquals(3, rs.getInt("completed_tasks"));
  }

  @Test
  @Order(6)
  void testNonExistentUserStats() throws SQLException {
    // Act
    int count = statisticsRepository.getTasksCompletedThisWeek(999999); // Nonexistent user ID

    // Assert
    assertEquals(0, count);

    int[][] weeklyStats = statisticsRepository.getWeeklyTaskCompletions(999999, 2);
    assertNotNull(weeklyStats);
    assertEquals(2, weeklyStats.length);
    assertEquals(0, weeklyStats[0][1]);
    assertEquals(0, weeklyStats[1][1]);
  }
}
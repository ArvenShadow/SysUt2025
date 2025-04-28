package edu.ntnu.idi.idatt1005;

import edu.ntnu.idi.idatt1005.db.DatabaseConnector;
import edu.ntnu.idi.idatt1005.model.Task;
import edu.ntnu.idi.idatt1005.model.TaskPriority;
import edu.ntnu.idi.idatt1005.repository.TaskRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskRepositoryTest {

  private static TaskRepository repo;
  private static int testUserId;
  private static int testMemberId;
  private static int taskId;

  @BeforeAll
  static void setup() throws SQLException {
    repo = new TaskRepository();

    // Create test user and member for our tests
    Connection conn = DatabaseConnector.getConnection();

    // Clean up any existing test data
    PreparedStatement cleanup = conn.prepareStatement(
      "DELETE FROM users WHERE username = 'taskrepotest'");
    cleanup.executeUpdate();

    // Create test user
    PreparedStatement createUser = conn.prepareStatement(
      "INSERT INTO users (username, password) VALUES ('taskrepotest', 'password')",
      PreparedStatement.RETURN_GENERATED_KEYS);
    createUser.executeUpdate();

    ResultSet userKeys = createUser.getGeneratedKeys();
    if (userKeys.next()) {
      testUserId = userKeys.getInt(1);
    } else {
      throw new AssertionError("Failed to create test user");
    }

    // Create test member
    PreparedStatement createMember = conn.prepareStatement(
      "INSERT INTO members (name, user_id) VALUES ('Test Member', ?)",
      PreparedStatement.RETURN_GENERATED_KEYS);
    createMember.setInt(1, testUserId);
    createMember.executeUpdate();

    ResultSet memberKeys = createMember.getGeneratedKeys();
    if (memberKeys.next()) {
      testMemberId = memberKeys.getInt(1);
    } else {
      throw new AssertionError("Failed to create test member");
    }
  }

  @AfterAll
  static void cleanup() throws SQLException {
    Connection conn = DatabaseConnector.getConnection();

    // Clean up test tasks
    PreparedStatement deleteTasks = conn.prepareStatement(
      "DELETE FROM tasks WHERE user_id = ?");
    deleteTasks.setInt(1, testUserId);
    deleteTasks.executeUpdate();

    // Clean up test history
    PreparedStatement deleteHistory = conn.prepareStatement(
      "DELETE FROM task_completion_history WHERE user_id = ?");
    deleteHistory.setInt(1, testUserId);
    deleteHistory.executeUpdate();

    // Clean up test statistics
    PreparedStatement deleteStats = conn.prepareStatement(
      "DELETE FROM statistics WHERE user_id = ?");
    deleteStats.setInt(1, testUserId);
    deleteStats.executeUpdate();

    // Clean up test member stats
    PreparedStatement deleteMemberStats = conn.prepareStatement(
      "DELETE FROM member_stats WHERE member_id = ?");
    deleteMemberStats.setInt(1, testMemberId);
    deleteMemberStats.executeUpdate();

    // Clean up test member
    PreparedStatement deleteMember = conn.prepareStatement(
      "DELETE FROM members WHERE id = ?");
    deleteMember.setInt(1, testMemberId);
    deleteMember.executeUpdate();

    // Clean up test user
    PreparedStatement deleteUser = conn.prepareStatement(
      "DELETE FROM users WHERE user_id = ?");
    deleteUser.setInt(1, testUserId);
    deleteUser.executeUpdate();

    DatabaseConnector.closeConnection();
  }

  @Test
  @Order(1)
  void testCreateTask() throws SQLException {
    // Arrange
    Task task = new Task(
      "JUnit test task",
      LocalDate.now().plusDays(7),
      TaskPriority.HIGH,
      "Testing task creation",
      "Test User"
    );

    // Act
    taskId = repo.createTask(task, testUserId);

    // Assert
    assertTrue(taskId > 0);

    // Verify in database
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement stmt = conn.prepareStatement(
      "SELECT * FROM tasks WHERE task_id = ?");
    stmt.setInt(1, taskId);
    ResultSet rs = stmt.executeQuery();

    assertTrue(rs.next());
    assertEquals("JUnit test task", rs.getString("description"));
    assertEquals(testUserId, rs.getInt("user_id"));
    assertEquals("HIGH", rs.getString("priority"));
  }

  @Test
  @Order(2)
  void testFetchTasks() throws SQLException {
    // Act
    List<Task> tasks = repo.fetchTasks(testUserId);

    // Assert
    assertFalse(tasks.isEmpty());

    // Find our test task
    Task testTask = tasks.stream()
      .filter(t -> t.getTaskId() == taskId)
      .findFirst()
      .orElse(null);

    assertNotNull(testTask);
    assertEquals("JUnit test task", testTask.getDescription());
    assertEquals(TaskPriority.HIGH, testTask.getPriority());
    assertEquals("Testing task creation", testTask.getDetails());
  }

  @Test
  @Order(3)
  void testCreateTaskForMember() throws SQLException {
    // Arrange
    Task task = new Task(
      "Member Task",
      LocalDate.now().plusDays(7),
      TaskPriority.MEDIUM,
      "Testing member task assignment",
      "Test Member"
    );

    // Act
    int memberTaskId = repo.createTaskForMember(task, testUserId, testMemberId);

    // Assert
    assertTrue(memberTaskId > 0);

    // Verify in database
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement stmt = conn.prepareStatement(
      "SELECT * FROM tasks WHERE task_id = ?");
    stmt.setInt(1, memberTaskId);
    ResultSet rs = stmt.executeQuery();

    assertTrue(rs.next());
    assertEquals("Member Task", rs.getString("description"));
    assertEquals(testMemberId, rs.getInt("member_id"));

    // Also verify member stats were updated
    PreparedStatement statsStmt = conn.prepareStatement(
      "SELECT * FROM member_stats WHERE member_id = ?");
    statsStmt.setInt(1, testMemberId);
    ResultSet statsRs = statsStmt.executeQuery();

    assertTrue(statsRs.next());
    assertEquals(1, statsRs.getInt("ongoing_tasks"));
    assertEquals(1, statsRs.getInt("total_tasks"));
  }

  @Test
  @Order(4)
  void testFetchTasksAssignedToMember() throws SQLException {
    // Act
    List<Task> tasks = repo.fetchTasksAssignedToMember(testMemberId);

    // Assert
    assertFalse(tasks.isEmpty());
    assertEquals(1, tasks.size());
    assertEquals("Member Task", tasks.get(0).getDescription());
    assertEquals("Test Member", tasks.get(0).getResponsibility());
  }

  @Test
  @Order(5)
  void testGetAssignedMemberId() throws SQLException {
    // Act
    Integer memberId = repo.getAssignedMemberId(taskId);

    // Assert
    assertNull(memberId);

    // Get ID of the member task
    List<Task> memberTasks = repo.fetchTasksAssignedToMember(testMemberId);
    int memberTaskId = memberTasks.get(0).getTaskId();

    // Check that task
    Integer assignedMemberId = repo.getAssignedMemberId(memberTaskId);
    assertEquals(testMemberId, assignedMemberId);
  }

  @Test
  @Order(6)
  void testCompleteTask() throws SQLException {
    // Arrange - task already created in first test

    // Get current stats
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement statsQuery = conn.prepareStatement(
      "SELECT COUNT(*) FROM statistics WHERE user_id = ?");
    statsQuery.setInt(1, testUserId);
    ResultSet statsRs = statsQuery.executeQuery();

    boolean statsExist = statsRs.next() && statsRs.getInt(1) > 0;
    statsRs.close();
    statsQuery.close();

    // Act
    repo.completeTask(taskId, testUserId);

    // Assert - task should be deleted
    // Get a fresh connection since completeTask closes its connection
    Connection verifyConn = DatabaseConnector.getConnection();
    PreparedStatement taskQuery = verifyConn.prepareStatement(
      "SELECT COUNT(*) FROM tasks WHERE task_id = ?");
    taskQuery.setInt(1, taskId);
    ResultSet taskRs = taskQuery.executeQuery();

    assertTrue(taskRs.next());
    assertEquals(0, taskRs.getInt(1));
    taskRs.close();
    taskQuery.close();

    // Check statistics were updated
    PreparedStatement statsCheckQuery = verifyConn.prepareStatement(
      "SELECT completed_tasks FROM statistics WHERE user_id = ?");
    statsCheckQuery.setInt(1, testUserId);
    ResultSet statsCheckRs = statsCheckQuery.executeQuery();

    assertTrue(statsCheckRs.next());
    assertEquals(statsExist ? 1 : 1, statsCheckRs.getInt("completed_tasks"));
    statsCheckRs.close();
    statsCheckQuery.close();

    // Check history was updated
    PreparedStatement historyQuery = verifyConn.prepareStatement(
      "SELECT COUNT(*) FROM task_completion_history WHERE task_id = ? AND user_id = ?");
    historyQuery.setInt(1, taskId);
    historyQuery.setInt(2, testUserId);
    ResultSet historyRs = historyQuery.executeQuery();

    assertTrue(historyRs.next());
    assertEquals(1, historyRs.getInt(1));

    historyRs.close();
    historyQuery.close();
  }

  @Test
  @Order(7)
  void testCompleteMemberTask() throws SQLException {
    // Arrange - get the member task ID
    List<Task> memberTasks = repo.fetchTasksAssignedToMember(testMemberId);
    assertFalse(memberTasks.isEmpty());
    int memberTaskId = memberTasks.get(0).getTaskId();

    // Get current member stats
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement statsQuery = conn.prepareStatement(
      "SELECT ongoing_tasks, completed_tasks FROM member_stats WHERE member_id = ?");
    statsQuery.setInt(1, testMemberId);
    ResultSet statsRs = statsQuery.executeQuery();

    assertTrue(statsRs.next());
    int initialOngoing = statsRs.getInt("ongoing_tasks");
    int initialCompleted = statsRs.getInt("completed_tasks");

    // Close resources before calling completeTask
    statsRs.close();
    statsQuery.close();

    // Act
    repo.completeTask(memberTaskId, testUserId);

    // Assert - task should be deleted
    // Get a fresh connection since completeTask closes its connection
    Connection verifyConn = DatabaseConnector.getConnection();
    PreparedStatement taskQuery = verifyConn.prepareStatement(
      "SELECT COUNT(*) FROM tasks WHERE task_id = ?");
    taskQuery.setInt(1, memberTaskId);
    ResultSet taskRs = taskQuery.executeQuery();

    assertTrue(taskRs.next());
    assertEquals(0, taskRs.getInt(1));
    taskRs.close();
    taskQuery.close();

    // Check member stats were updated
    PreparedStatement statsCheckQuery = verifyConn.prepareStatement(
      "SELECT ongoing_tasks, completed_tasks FROM member_stats WHERE member_id = ?");
    statsCheckQuery.setInt(1, testMemberId);
    ResultSet statsCheckRs = statsCheckQuery.executeQuery();

    assertTrue(statsCheckRs.next());
    assertEquals(initialOngoing - 1, statsCheckRs.getInt("ongoing_tasks"));
    assertEquals(initialCompleted + 1, statsCheckRs.getInt("completed_tasks"));

    statsCheckRs.close();
    statsCheckQuery.close();
  }

  @Test
  @Order(8)
  void testDeleteTask() throws SQLException {
    // Arrange - create a task to delete
    Task task = new Task(
      "Task to delete",
      LocalDate.now(),
      TaskPriority.LOW,
      "This task will be deleted directly",
      "Test User"
    );

    int newTaskId = repo.createTask(task, testUserId);
    assertTrue(newTaskId > 0);

    // Act
    repo.deleteTask(newTaskId);

    // Assert
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement stmt = conn.prepareStatement(
      "SELECT COUNT(*) FROM tasks WHERE task_id = ?");
    stmt.setInt(1, newTaskId);
    ResultSet rs = stmt.executeQuery();

    assertTrue(rs.next());
    assertEquals(0, rs.getInt(1));
  }
}
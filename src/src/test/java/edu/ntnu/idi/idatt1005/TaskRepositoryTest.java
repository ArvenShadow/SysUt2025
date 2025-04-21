package edu.ntnu.idi.idatt1005;

import edu.ntnu.idi.idatt1005.db.DatabaseConnector;
import edu.ntnu.idi.idatt1005.model.Task;
import edu.ntnu.idi.idatt1005.model.TaskPriority;
import edu.ntnu.idi.idatt1005.repository.TaskRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskRepositoryTest {

  private static TaskRepository repo;
  private static int userId;

  @BeforeAll
  static void setup() throws Exception {
    DatabaseConnectorTest.setupTestDatabase();
    repo = new TaskRepository();

    // Get test user ID from users_test table
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users_test WHERE username = 'testuser'");
    var rs = stmt.executeQuery();
    assertTrue(rs.next());
    userId = rs.getInt("user_id");
  }

  @Test
  @Order(1)
  void testCreateTask() throws Exception {
    Task task = new Task("JUnit test task", LocalDate.now(), TaskPriority.HIGH, "Testing creation", "Mateja");
    int taskId = repo.createTask(task, userId);
    assertTrue(taskId > 0);
  }

  @Test
  @Order(2)
  void testFetchTasks() throws Exception {
    List<Task> tasks = repo.fetchTasks(userId);
    assertFalse(tasks.isEmpty());
    assertEquals("JUnit test task", tasks.get(0).getDescription());
  }

  @AfterAll
  static void cleanup() throws Exception {
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement stmt = conn.prepareStatement("DELETE FROM tasks_test WHERE user_id = ?");
    stmt.setInt(1, userId);
    stmt.executeUpdate();
    DatabaseConnector.closeConnection();
  }
}
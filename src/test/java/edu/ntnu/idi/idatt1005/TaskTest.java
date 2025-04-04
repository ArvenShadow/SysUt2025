//package edu.ntnu.idi.idatt1005;
//
//import edu.ntnu.idi.idatt1005.db.DatabaseConnector;
//import edu.ntnu.idi.idatt1005.model.Task;
//import edu.ntnu.idi.idatt1005.model.TaskPriority;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class TaskTest {
//
//  private Task task;
//  private Connection connection;
//
//  @BeforeEach
//  void setUp() throws SQLException {
//    // Set up database connection
//    connection = DatabaseConnector.getConnection();
//    task = new Task("Buy groceries", LocalDate.of(2025, 4, 1), TaskPriority.MEDIUM, "Milk, Bread, Eggs");
//  }
//
//  @Test
//  void testDescriptionProperty() {
//    assertEquals("Buy groceries", task.getDescription());
//    task.setDescription("Go running");
//    assertEquals("Go running", task.getDescription());
//
//
//  @Test
//  void testDueDateProperty() {
//    assertEquals(LocalDate.of(2025, 4, 1), task.getDueDate());
//  }
//
//  @Test
//  void testDetailsProperty() {
//    assertEquals("Milk, Bread, Eggs", task.getDetails());
//  }
//
//  @Test
//  void testResponsibilityDefault() {
//    assertEquals("", task.getResponsibility());
//  }
//}
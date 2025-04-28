package edu.ntnu.idi.idatt1005;

import edu.ntnu.idi.idatt1005.model.Task;
import edu.ntnu.idi.idatt1005.model.TaskPriority;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

  private Task task;
  private Task taskWithId;
  private final LocalDate dueDate = LocalDate.of(2025, 5, 15);

  @BeforeEach
  void setUp() {
    task = new Task("Test Task", dueDate, TaskPriority.MEDIUM, "Task details", "John");
    taskWithId = new Task(42, "Task with ID", dueDate, TaskPriority.HIGH, "Detailed notes", "Jane");
  }

  @Test
  void testConstructorWithoutId() {
    // Assert
    assertEquals("Test Task", task.getDescription());
    assertEquals(dueDate, task.getDueDate());
    assertEquals(TaskPriority.MEDIUM, task.getPriority());
    assertEquals("Task details", task.getDetails());
    assertEquals("John", task.getResponsibility());
    assertEquals(0, task.getTaskId());
  }

  @Test
  void testConstructorWithId() {
    // Assert
    assertEquals(42, taskWithId.getTaskId());
    assertEquals("Task with ID", taskWithId.getDescription());
    assertEquals(dueDate, taskWithId.getDueDate());
    assertEquals(TaskPriority.HIGH, taskWithId.getPriority());
    assertEquals("Detailed notes", taskWithId.getDetails());
    assertEquals("Jane", taskWithId.getResponsibility());
  }

  @Test
  void testJavaFxProperties() {
    // Test StringProperty for description
    StringProperty descProperty = task.descriptionProperty();
    assertNotNull(descProperty);
    assertEquals("Test Task", descProperty.get());

    // Test ObjectProperty for due date
    ObjectProperty<LocalDate> dateProperty = task.dueDateProperty();
    assertNotNull(dateProperty);
    assertEquals(dueDate, dateProperty.get());

    // Test ObjectProperty for priority
    ObjectProperty<TaskPriority> priorityProperty = task.priorityProperty();
    assertNotNull(priorityProperty);
    assertEquals(TaskPriority.MEDIUM, priorityProperty.get());

    // Test StringProperty for details
    StringProperty detailsProperty = task.detailsProperty();
    assertNotNull(detailsProperty);
    assertEquals("Task details", detailsProperty.get());

    // Test StringProperty for responsibility
    StringProperty responsibilityProperty = task.responsibilityProperty();
    assertNotNull(responsibilityProperty);
    assertEquals("John", responsibilityProperty.get());
  }

  @Test
  void testSetTaskId() {
    // Act
    task.setTaskId(100);

    // Assert
    assertEquals(100, task.getTaskId());
  }

  @Test
  void testPropertyBinding() {
    // Arrange
    final boolean[] descriptionChanged = {false};

    // Act
    task.descriptionProperty().addListener((observable, oldValue, newValue) -> {
      descriptionChanged[0] = true;
      assertEquals("Updated Description", newValue);
    });

    // Modify through the property
    task.descriptionProperty().set("Updated Description");

    // Assert
    assertTrue(descriptionChanged[0]);
    assertEquals("Updated Description", task.getDescription());
  }

  @Test
  void testAllPriorityValues() {
    // Check all possible priority values
    Task lowTask = new Task("Low Priority", dueDate, TaskPriority.LOW, "Details", "Person");
    Task mediumTask = new Task("Medium Priority", dueDate, TaskPriority.MEDIUM, "Details", "Person");
    Task highTask = new Task("High Priority", dueDate, TaskPriority.HIGH, "Details", "Person");

    assertEquals(TaskPriority.LOW, lowTask.getPriority());
    assertEquals(TaskPriority.MEDIUM, mediumTask.getPriority());
    assertEquals(TaskPriority.HIGH, highTask.getPriority());
  }
}
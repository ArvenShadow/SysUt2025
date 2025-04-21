package edu.ntnu.idi.idatt1005.model;

import java.time.LocalDate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a task in the system.
 * This class uses JavaFX properties to allow for data binding in a UI context.
 * Each task has a description, due date, priority level, details, and an assigned responsibility.
 */
public class Task {
  private final StringProperty description;
  private final ObjectProperty<LocalDate> dueDate;
  private final ObjectProperty<TaskPriority> priority;
  private final StringProperty details;
  private final StringProperty responsibility;
  private int taskId;

  /**
   * Constructs a new Task object with the specified values.
   *
   * @param description    the description of the task
   * @param dueDate        the due date of the task
   * @param priority       the priority level of the task
   * @param details        additional details about the task
   * @param responsibility the person or entity responsible for the task
   */
  public Task(
      String description, LocalDate dueDate, TaskPriority priority,
      String details, String responsibility) {
    this.description = new SimpleStringProperty(description);
    this.dueDate = new SimpleObjectProperty<>(dueDate);
    this.priority = new SimpleObjectProperty<>(priority);
    this.details = new SimpleStringProperty(details);
    this.responsibility = new SimpleStringProperty(responsibility); // Default empty
  }

  /**
   * Constructs a new Task object with the specified values including an ID.
   * This constructor is typically used when retrieving tasks from the database.
   *
   * @param taskId         the unique identifier of the task
   * @param description    the description of the task
   * @param dueDate        the due date of the task
   * @param priority       the priority level of the task
   * @param details        additional details about the task
   * @param responsibility the person or entity responsible for the task
   */
  public Task(
      int taskId, String description, LocalDate dueDate,
      TaskPriority priority, String details, String responsibility) {
    this.taskId = taskId;
    this.description = new SimpleStringProperty(description);
    this.dueDate = new SimpleObjectProperty<>(dueDate);
    this.priority = new SimpleObjectProperty<>(priority);
    this.details = new SimpleStringProperty(details);
    this.responsibility = new SimpleStringProperty(responsibility); // Default empty
  }

  /**
   * Gets the task ID.
   *
   * @return the unique identifier of the task
   */
  public int getTaskId() {
    return taskId;
  }

  /**
   * Gets the description property.
   *
   * @return the description property
   */
  public StringProperty descriptionProperty() {
    return description;
  }

  /**
   * Gets the due date property.
   *
   * @return the due date property
   */
  public ObjectProperty<LocalDate> dueDateProperty() {
    return dueDate;
  }

  /**
   * Gets the priority property.
   *
   * @return the priority property
   */
  public ObjectProperty<TaskPriority> priorityProperty() {
    return priority;
  }

  /**
   * Gets the details property.
   *
   * @return the details property
   */
  public StringProperty detailsProperty() {
    return details;
  }

  /**
   * Gets the responsibility property.
   *
   * @return the responsibility property
   */
  public StringProperty responsibilityProperty() {
    return responsibility;
  }

  /**
   * Gets the description of the task.
   *
   * @return the task description
   */
  public String getDescription() {
    return description.get();
  }

  /**
   * Gets the due date of the task.
   *
   * @return the task due date
   */
  public LocalDate getDueDate() {
    return dueDate.get();
  }

  /**
   * Gets the priority level of the task.
   *
   * @return the task priority
   */
  public TaskPriority getPriority() {
    return priority.get();
  }

  /**
   * Gets the details of the task.
   *
   * @return the task details
   */
  public String getDetails() {
    return details.get();
  }

  /**
   * Gets the responsibility assignment for the task.
   *
   * @return the person or entity responsible for the task
   */
  public String getResponsibility() {
    return responsibility.get();
  }

  /**
   * Sets the task ID.
   *
   * @param newTaskId the new task ID
   */
  public void setTaskId(int newTaskId) {
    this.taskId = newTaskId;
  }
}
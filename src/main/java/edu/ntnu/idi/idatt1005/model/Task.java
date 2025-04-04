package edu.ntnu.idi.idatt1005.model;

import edu.ntnu.idi.idatt1005.model.TaskPriority;
import javafx.beans.property.*;
import java.time.LocalDate;

public class Task {
  private final StringProperty description;
  private final ObjectProperty<LocalDate> dueDate;
  private final ObjectProperty<TaskPriority> priority;
  private final StringProperty details;
  private final StringProperty responsibility;
  private int taskId;

  public Task(String description, LocalDate dueDate, TaskPriority priority, String details) {
    this.description = new SimpleStringProperty(description);
    this.dueDate = new SimpleObjectProperty<>(dueDate);
    this.priority = new SimpleObjectProperty<>(priority);
    this.details = new SimpleStringProperty(details);
    this.responsibility = new SimpleStringProperty(""); // Default empty
  }

  public Task(int taskId, String description, LocalDate dueDate, TaskPriority priority, String details) {
    this.taskId = taskId;
    this.description = new SimpleStringProperty(description);
    this.dueDate = new SimpleObjectProperty<>(dueDate);
    this.priority = new SimpleObjectProperty<>(priority);
    this.details = new SimpleStringProperty(details);
    this.responsibility = new SimpleStringProperty(""); // Default empty
  }

  // Getters for properties
  public int getTaskId() { return taskId; }
  public StringProperty descriptionProperty() { return description; }
  public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }
  public ObjectProperty<TaskPriority> priorityProperty() { return priority; }
  public StringProperty detailsProperty() { return details; }
  public StringProperty responsibilityProperty() { return responsibility; }

  // Existing getters and setters
  public String getDescription() { return description.get(); }
  public LocalDate getDueDate() { return dueDate.get(); }
  public TaskPriority getPriority() { return priority.get(); }
  public String getDetails() { return details.get(); }
  public String getResponsibility() { return responsibility.get(); }

  public void setTaskId(int newTaskId) {
      this.taskId = newTaskId;
  }

  //Setters with property support
  public void setDescription(String desc) { this.description.set(desc); }
  public void setDueDate(LocalDate date) { this.dueDate.set(date); }
  public void setPriority(TaskPriority priority) { this.priority.set(priority); }
  public void setDetails(String details) { this.details.set(details); }
  public void setResponsibility(String responsibility) { this.responsibility.set(responsibility); }
}
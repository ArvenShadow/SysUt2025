package edu.ntnu.idi.idatt1005.view.landing;

import edu.ntnu.idi.idatt1005.app.AppState;
import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.model.Task;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.model.TaskPriority;
import edu.ntnu.idi.idatt1005.repository.TaskRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A screen/form for creating a new task.
 * Allows users to input task data and submit it to the database.
 * Uses {@link AppState} to retrieve the current user and {@link NavigationController}
 * for scene switching.
 * Author: KrissKN
 */
public class TaskInformation {

  private final Stage primaryStage;
  private final NavigationController navigation;
  private final TaskRepository taskRepository = new TaskRepository();

  private DatePicker dueDatePicker;
  private TextField shortDescField;
  private ComboBox<TaskPriority> priorityField;
  private TextArea taskDetailsArea;

  public TaskInformation(Stage primaryStage, NavigationController navigation) {
    this.primaryStage = primaryStage;
    this.navigation = navigation;
  }

  public Scene getTaskInfoScene() {
    GridPane form = new GridPane();
    form.setHgap(15);
    form.setVgap(15);
    form.setPadding(new Insets(20));

    Label titleLabel = new Label("Task Information");
    titleLabel.getStyleClass().add("header");
    form.add(titleLabel, 0, 0, 2, 1);

    // Due Date
    Label dueDateLabel = new Label("Due Date");
    dueDateLabel.getStyleClass().add("form-label");
    dueDatePicker = new DatePicker();
    dueDatePicker.getStyleClass().add("form-input");
    form.add(dueDateLabel, 0, 1);
    form.add(dueDatePicker, 1, 1);

    // Description
    Label descLabel = new Label("Short Description");
    descLabel.getStyleClass().add("form-label");

    shortDescField = new TextField();
    shortDescField.setPromptText("Enter task description...");
    shortDescField.getStyleClass().addAll("text-field", "form-input");

    form.add(descLabel, 0, 2);
    form.add(shortDescField, 1, 2);

    // Priority
    Label priorityLabel = new Label("Priority");
    priorityLabel.getStyleClass().add("form-label");
    priorityField = new ComboBox<>();
    priorityField.getItems().addAll(TaskPriority.LOW, TaskPriority.MEDIUM, TaskPriority.HIGH);
    priorityField.setPromptText("Select Priority");
    priorityField.getStyleClass().add("form-input");
    form.add(priorityLabel, 0, 3);
    form.add(priorityField, 1, 3);

    // Details
    Label detailsLabel = new Label("Details");
    detailsLabel.getStyleClass().add("form-label");
    taskDetailsArea = new TextArea();
    taskDetailsArea.setPromptText("Enter detailed task information...");
    taskDetailsArea.getStyleClass().add("form-input");
    taskDetailsArea.setPrefHeight(100);
    form.add(detailsLabel, 0, 4);
    form.add(taskDetailsArea, 1, 4);

    // Buttons
    Button submitButton = new Button("✓ Submit");
    submitButton.getStyleClass().add("action-button");
    submitButton.setOnAction(e -> createTaskFromInput());

    Button cancelButton = new Button("✕ Cancel");
    cancelButton.getStyleClass().add("button");
    cancelButton.setOnAction(e -> navigation.goToLandingPage());

    HBox buttonBox = new HBox(10, submitButton, cancelButton);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    form.add(buttonBox, 1, 5);

    // Wrapper
    VBox wrapper = new VBox(form);
    wrapper.setAlignment(Pos.CENTER);
    wrapper.setPadding(new Insets(30));
    wrapper.getStyleClass().add("form-wrapper");

    StackPane root = new StackPane(wrapper);
    root.setPrefSize(1000, 700);
    root.setAlignment(Pos.CENTER);
    root.getStyleClass().add("form-background");

    Scene scene = new Scene(root);
    scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/landingExtend.css")).toExternalForm());
    return scene;
  }

  public void show() {
    primaryStage.setTitle("Task Manager - Task Information");
    primaryStage.setScene(getTaskInfoScene());
    primaryStage.show();
  }

  private void createTaskFromInput() {
    String description = shortDescField.getText().trim();
    String details = taskDetailsArea.getText().trim();
    LocalDate dueDate = dueDatePicker.getValue();
    TaskPriority priority = priorityField.getValue();

    if (dueDate == null) {
      showAlert("Due date cannot be empty!");
      return;
    }
    if (priority == null) {
      showAlert("Priority must be selected!");
      return;
    }
    if (description.isEmpty()) {
      showAlert("Description cannot be empty!");
      return;
    }

    Task newTask = new Task(description, dueDate, priority, details);

    try {
      User currentUser = AppState.getInstance().getCurrentUser();
      if (currentUser == null) throw new IllegalStateException("No user logged in!");

      taskRepository.createTask(newTask, currentUser.getId());
      navigation.goToLandingPage();
    } catch (SQLException e) {
      showAlert("Error saving task: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void showAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Input Error");
    alert.setHeaderText("Invalid Task Input");
    alert.setContentText(message);
    alert.showAndWait();
  }
}

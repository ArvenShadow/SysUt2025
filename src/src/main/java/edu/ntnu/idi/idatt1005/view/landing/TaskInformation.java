package edu.ntnu.idi.idatt1005.view.landing;

import edu.ntnu.idi.idatt1005.app.AppState;
import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.model.Member;
import edu.ntnu.idi.idatt1005.model.Task;
import edu.ntnu.idi.idatt1005.model.TaskPriority;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.repository.MemberRepository;
import edu.ntnu.idi.idatt1005.repository.TaskRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * The TaskInformation class provides the functionality for displaying and handling
 * task information input forms. It includes methods to display a task form,
 * manage user input, validate form fields, and create new tasks based on input.
 */
public class TaskInformation {

  private final Stage primaryStage;
  private final NavigationController navigation;
  private final TaskRepository taskRepository = new TaskRepository();
  private final MemberRepository memberRepository = new MemberRepository();

  private DatePicker dueDatePicker;
  private TextField shortDescField;
  private ComboBox<TaskPriority> priorityField;
  private TextArea taskDetailsArea;
  private ComboBox<Member> responsibilityField;

  private List<Member> memberList;

  /**
 * Constructor for TaskInformation.
 * Initializes the class with the primary application stage and the navigation controller.
 *
 * @param primaryStage The primary stage of the application.
 * @param navigation   The navigation controller for handling page transitions.
 */
  public TaskInformation(Stage primaryStage, NavigationController navigation) {
    this.primaryStage = primaryStage;
    this.navigation = navigation;
  }

  /**
 * Creates and returns the task information scene.
 * This scene includes the task input form UI, allowing users to specify task details.
 *
 * @return The Scene object containing the task information form.
 */
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
    priorityField.getItems().addAll(TaskPriority.values());
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

    // Responsibility
    Label responsibilityLabel = new Label("Responsibility");
    responsibilityLabel.getStyleClass().add("form-label");
    responsibilityField = new ComboBox<>();
    responsibilityField.setPromptText("Assign to a member");
    responsibilityField.getStyleClass().add("form-input");
    loadHouseholdMembers();
    form.add(responsibilityLabel, 0, 5);
    form.add(responsibilityField, 1, 5);

    // Buttons
    Button submitButton = new Button("✓ Submit");
    submitButton.getStyleClass().add("action-button");
    submitButton.setOnAction(e -> createTaskFromInput());

    Button cancelButton = new Button("✕ Cancel");
    cancelButton.getStyleClass().add("button");
    cancelButton.setOnAction(e -> navigation.goToLandingPage());

    HBox buttonBox = new HBox(10, submitButton, cancelButton);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    form.add(buttonBox, 1, 6);

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
    scene.getStylesheets().add(Objects.requireNonNull(getClass()
        .getResource("/landingExtend.css")).toExternalForm());
    return scene;
  }

  /**
 * Displays the task information scene on the primary stage.
 * Sets the scene to display the task input form UI.
 */
  public void show() {
    navigation.goToTaskInfoPage(this.getTaskInfoScene());
  }

  /**
 * Creates a new task based on the input provided in the form fields.
 * Validates the user input, constructs a Task object, and saves it to the database.
 * Handles errors encountered during the task creation process.
 */
  private void createTaskFromInput() {
    String description = shortDescField.getText().trim();
    String details = taskDetailsArea.getText().trim();
    LocalDate dueDate = dueDatePicker.getValue();
    TaskPriority priority = priorityField.getValue();
    Member assignedMember = responsibilityField.getValue();

    if (description.isEmpty()) {
      showAlert("Description cannot be empty!");
      return;
    }
    if (dueDate == null || dueDate.isBefore(LocalDate.now())) {
      showAlert("Due date cannot be empty or before todays date!");
      return;
    }
    if (priority == null) {
      showAlert("Priority must be selected!");
      return;
    }

    try {
      User currentUser = AppState.getInstance().getCurrentUser();
      if (currentUser == null) {
        throw new IllegalStateException("No user logged in!");
      }

      String responsibility = assignedMember
          != null ? assignedMember.getName() : currentUser.getUsername();
      Task newTask = new Task(description, dueDate, priority, details, responsibility);

      if (assignedMember != null) {
        taskRepository.createTaskForMember(newTask, currentUser.getId(), assignedMember.getId());
      } else {
        taskRepository.createTask(newTask, currentUser.getId());
      }

      AppState.getInstance().setShouldReloadTasks(true);
      navigation.goToLandingPage();

    } catch (SQLException e) {
      showAlert("Error saving task: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
 * Loads the list of household members associated with the currently logged-in user.
 * Fetches the members from the database and populates the responsibility combo box.
 */
  private void loadHouseholdMembers() {
    try {
      User currentUser = AppState.getInstance().getCurrentUser();
      if (currentUser == null) {
        return;
      }

      memberList = memberRepository.fetchMembers(currentUser.getId());
      responsibilityField.getItems().addAll(memberList);
    } catch (SQLException e) {
      showAlert("Failed to load members: " + e.getMessage());
    }
  }

  /**
 * Displays an alert dialog with the specified error message.
 * Used to inform the user of input validation errors or operation failures.
 *
 * @param message The error message to display in the alert dialog.
 */
  private void showAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Input Error");
    alert.setHeaderText("Invalid Task Input");
    alert.setContentText(message);
    alert.showAndWait();
  }
}

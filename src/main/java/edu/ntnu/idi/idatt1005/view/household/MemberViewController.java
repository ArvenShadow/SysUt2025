package edu.ntnu.idi.idatt1005.view.household;

import edu.ntnu.idi.idatt1005.app.AppState;
import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.model.Task;
import edu.ntnu.idi.idatt1005.model.TaskPriority;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.repository.StatisticsRepository;
import edu.ntnu.idi.idatt1005.repository.TaskRepository;
import edu.ntnu.idi.idatt1005.util.TableCellFactories;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

/**
 * Controller for the Member View that displays and manages tasks assigned to a specific member.
 * This controller allows viewing, creating, and completing tasks associated with a member.
 * It interfaces with the task repository to persist changes and update the UI accordingly.
 */
public class MemberViewController implements Initializable {

  /**
  * Default constructor for MemberCardController.
  */
  public MemberViewController() {

  }

  @FXML private Text usernameTitle;
  @FXML private Button backButton;
  @FXML private TableView<Task> taskTable;
  @FXML private TableColumn<Task, LocalDate> dueDateColumn;
  @FXML private TableColumn<Task, TaskPriority> priorityColumn;
  @FXML private TableColumn<Task, String> descriptionColumn;
  @FXML private TableColumn<Task, String> detailsColumn;
  @FXML private Button filterButton;
  @FXML private Button newTaskButton;
  @FXML private Button completeTaskButton;

  private NavigationController navigation;
  private User member;
  private final TaskRepository taskRepository = new TaskRepository();
  private final ObservableList<Task> taskList = FXCollections.observableArrayList();

  /**
  * Initializes the controller after its root element has been completely processed.
  * Sets up table columns, binds data, and configures event handlers.
  *
  * @param location The location used to resolve relative paths for the root object
  * @param resources The resources used to localize the root object
  */
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    setupTableColumns();
    taskTable.setItems(taskList);
    taskTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    taskTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) ->
                completeTaskButton.setVisible(selected != null)
    );

    completeTaskButton.setOnAction(e -> handleCompleteTask());
  }

  /**
  * Configures the table columns with appropriate cell value factories and formatters.
  * Sets up custom formatting for the priority column.
  */
  private void setupTableColumns() {
    dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());
    priorityColumn.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());
    descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
    detailsColumn.setCellValueFactory(cellData -> cellData.getValue().detailsProperty());

    priorityColumn.setCellFactory(TableCellFactories.priorityCellFactory());
  }

  /**
  * Sets the navigation controller for this view.
  *
  * @param navigation The navigation controller to use for view transitions
  */
  public void setNavigation(NavigationController navigation) {
    this.navigation = navigation;
  }

  /**
  * Sets the member whose tasks will be displayed and managed.
  * Updates the view title and loads the member's tasks from the database.
  *
  * @param member The user object representing the member
  */
  public void setMember(User member) {
    this.member = member;
    if (member != null) {
      usernameTitle.setText(member.getUsername() + "'s tasks");

      taskList.clear();          // Clear old data
      loadMemberTasks();         // Load fresh data from DB
    }
  }

  /**
  * Handles the action of completing a selected task.
  * Updates both task status and member statistics in the database.
  * Removes the completed task from the table view.
  */
  @FXML
  private void handleCompleteTask() {
    Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
    if (selectedTask == null || member == null) {
      return;
    }

    try {
      // Complete the task with the unified method
      taskRepository.completeTask(selectedTask.getTaskId(),
        AppState.getInstance().getCurrentUser().getId());

      taskList.remove(selectedTask);
      taskTable.getSelectionModel().clearSelection();
      completeTaskButton.setVisible(false);

      AppState.getInstance().setShouldReloadTasks(true);

      showAlert(Alert.AlertType.INFORMATION, "Success", "Task Completed",
        "Task marked as completed and stats updated.");
    } catch (SQLException e) {
      e.printStackTrace();
      showAlert(Alert.AlertType.ERROR, "Error", "Failed to complete task",
        "Could not complete task: " + e.getMessage());
    }
  }

  /**
  * Loads all tasks assigned to the current member from the database.
  * Populates the task table with the retrieved data.
  */
  private void loadMemberTasks() {
    if (member == null) {
      return;
    }

    try {
      taskList.setAll(taskRepository.fetchTasksAssignedToMember(member.getId()));
    } catch (SQLException e) {
      showAlert(Alert.AlertType.ERROR, "Error", "Failed to load tasks",
                    "Could not load tasks for this member: " + e.getMessage());
    }
  }

  /**
  * Handles the back button click by navigating to the household page.
  *
  * @param event The action event triggered by clicking the back button
  */
  @FXML
  public void handleBackButton(ActionEvent event) {
    if (navigation != null) {
      navigation.goToHouseholdPage();
    }
  }

  /**
  * Handles the filter button click.
  * Currently, displays a placeholder message as filtering is not yet implemented.
  *
  * @param event The action event triggered by clicking the filter button
  */
  @FXML
  public void handleFilterButton(ActionEvent event) {
    showAlert(Alert.AlertType.INFORMATION, "Filter", "Filter Function",
                "Filter functionality not implemented yet.");
  }


  /**
  * Handles the new task button click by opening a dialog to create a new task.
  * Validates that a member is selected before proceeding.
  *
  * @param event The action event triggered by clicking the new task button
  */
  @FXML
  public void handleNewTaskButton(ActionEvent event) {
    if (member == null) {
      showAlert(Alert.AlertType.ERROR, "Error", "No Member Selected",
                    "Cannot create a task without selecting a member.");
      return;
    }

    createTaskDialog();
  }

  /**
  * Creates and displays a dialog for entering details of a new task.
  * The dialog includes fields for description, due date, priority, and details.
  * On confirmation, creates a new task in the database and adds it to the table view.
  */
  private void createTaskDialog() {
    User currentUser = AppState.getInstance().getCurrentUser();
    if (currentUser == null) {
      showAlert(Alert.AlertType.ERROR, "Error", "Not Logged In",
                    "You must be logged in to create tasks.");
      return;
    }

    Dialog<Task> dialog = new Dialog<>();
    dialog.setTitle("New Task for " + member.getUsername());
    dialog.setHeaderText("Enter task details");

    ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField descriptionField = new TextField();
    descriptionField.setPromptText("Description");
    ComboBox<TaskPriority> priorityField = new ComboBox<>();
    priorityField.getItems().addAll(TaskPriority.values());
    priorityField.setValue(TaskPriority.MEDIUM);
    TextArea detailsArea = new TextArea();
    detailsArea.setPromptText("Details");
    detailsArea.setPrefRowCount(5);
    DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusDays(7));
    grid.add(new Label("Description:"), 0, 0);
    grid.add(descriptionField, 1, 0);
    grid.add(new Label("Due Date:"), 0, 1);
    grid.add(dueDatePicker, 1, 1);
    grid.add(new Label("Priority:"), 0, 2);
    grid.add(priorityField, 1, 2);
    grid.add(new Label("Details:"), 0, 3);
    grid.add(detailsArea, 1, 3);

    dialog.getDialogPane().setContent(grid);
    descriptionField.requestFocus();

    Button createButton = (Button) dialog.getDialogPane().lookupButton(createButtonType);
    createButton.setDisable(true);

    descriptionField.textProperty().addListener((obs, oldVal, newVal) ->
                createButton.setDisable(newVal.trim().isEmpty())
    );

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == createButtonType) {
        return new Task(
                  0,
                   descriptionField.getText().trim(),
                   dueDatePicker.getValue(),
                   priorityField.getValue(),
                   detailsArea.getText().trim(),
                   member.getUsername() // responsibility
                );
      }
      return null;
    });

    dialog.showAndWait().ifPresent(task -> {
      try {
        int taskId = taskRepository.createTaskForMember(
                        task,
                        currentUser.getId(),
                        member.getId()
                );

        if (taskId > 0) {
          task.setTaskId(taskId);
          taskList.add(task);
          showAlert(Alert.AlertType.INFORMATION, "Success", "Task Created",
                            "New task successfully created for " + member.getUsername());
        }
      } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Error", "Task Creation Failed",
                        "Could not create task: " + e.getMessage());
      }
    });
  }

  /**
  * Displays an alert dialog with the specified type, title, header, and content.
  * Used throughout the controller to show informational and error messages.
  *
  * @param type The type of alert (information, warning, error, etc.)
  * @param title The title text for the alert dialog
  * @param header The header text for the alert dialog
  * @param content The main content text for the alert dialog
  */
  private void showAlert(Alert.AlertType type, String title, String header, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }
}

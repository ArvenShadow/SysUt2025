package edu.ntnu.idi.idatt1005.view.household;

import edu.ntnu.idi.idatt1005.app.AppState;
import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.model.Task;
import edu.ntnu.idi.idatt1005.model.TaskPriority;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.repository.TaskRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class MemberViewController implements Initializable {

    @FXML private Text usernameTitle;
    @FXML private Button backButton;
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, LocalDate> dueDateColumn;
    @FXML private TableColumn<Task, TaskPriority> priorityColumn;
    @FXML private TableColumn<Task, String> descriptionColumn;
    @FXML private TableColumn<Task, String> detailsColumn;
    @FXML private Button filterButton;
    @FXML private Button newTaskButton;

    private NavigationController navigation;
    private User member;
    private final TaskRepository taskRepository = new TaskRepository();
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        taskTable.setItems(taskList);
        taskTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupTableColumns() {
        // Set up cell value factories for table columns
        dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());
        priorityColumn.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        detailsColumn.setCellValueFactory(cellData -> cellData.getValue().detailsProperty());

        // Style priority column
        priorityColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(TaskPriority priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    getStyleClass().removeAll("priority-low", "priority-medium", "priority-high");
                } else {
                    setText(priority.name());
                    getStyleClass().removeAll("priority-low", "priority-medium", "priority-high");
                    switch (priority) {
                        case LOW -> getStyleClass().add("priority-low");
                        case MEDIUM -> getStyleClass().add("priority-medium");
                        case HIGH -> getStyleClass().add("priority-high");
                    }
                }
            }
        });
    }

    public void setNavigation(NavigationController navigation) {
        this.navigation = navigation;
    }

    public void setMember(User member) {
        this.member = member;
        if (member != null) {
            // Update the UI with member information
            usernameTitle.setText(member.getUsername() + "'s tasks");
            loadMemberTasks();
        }
    }

    // CHANGED: Updated to use fetchTasksAssignedToMember instead of fetchTasks
    private void loadMemberTasks() {
        if (member == null) return;

        try {
            taskList.setAll(taskRepository.fetchTasksAssignedToMember(member.getId()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load tasks",
                    "Could not load tasks for this member: " + e.getMessage());
        }
    }

    @FXML
    public void handleBackButton(ActionEvent event) {
        if (navigation != null) {
            navigation.goToHouseholdPage(); // Navigate back to household view
        }
    }

    @FXML
    public void handleFilterButton(ActionEvent event) {
        // Implement filter functionality
        showAlert(Alert.AlertType.INFORMATION, "Filter", "Filter Function",
                "Filter functionality not implemented yet.");
    }

    @FXML
    public void handleNewTaskButton(ActionEvent event) {
        if (member == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No Member Selected",
                    "Cannot create a task without selecting a member.");
            return;
        }

        createTaskDialog();
    }

    // CHANGED: Updated to use current user as assignor and member as assignee
    private void createTaskDialog() {
        // Get the current logged-in user (task creator)
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

        DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusDays(7));

        ComboBox<TaskPriority> priorityField = new ComboBox<>();
        priorityField.getItems().addAll(TaskPriority.values());
        priorityField.setValue(TaskPriority.MEDIUM);

        TextArea detailsArea = new TextArea();
        detailsArea.setPromptText("Details");
        detailsArea.setPrefRowCount(5);

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

        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Task(
                        0,
                        descriptionField.getText().trim(),
                        dueDatePicker.getValue(),
                        priorityField.getValue(),
                        detailsArea.getText().trim()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(task -> {
            try {
                int taskId = taskRepository.createTaskForMember(
                        task,
                        currentUser.getId(),  // The creator of the task
                        member.getId()        // The member assigned to the task
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

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
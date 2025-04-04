package edu.ntnu.idi.idatt1005.household;

import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.model.Task;
import edu.ntnu.idi.idatt1005.model.TaskPriority;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.repository.TaskRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Controller for the MemberView – shows tasks assigned to a specific member.
 * Supports creating tasks via TaskRepository#createTaskForMember.
 */
public class MemberViewController {

    // FXML bindings
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, LocalDate> dueDateColumn;
    @FXML private TableColumn<Task, TaskPriority> priorityColumn;
    @FXML private TableColumn<Task, String> descriptionColumn;
    @FXML private TableColumn<Task, String> detailsColumn;
    @FXML private Button filterButton;
    @FXML private Button newTaskButton;
    @FXML private Text usernameTitle;

    private final TaskRepository taskRepository = new TaskRepository();
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();

    private NavigationController navigation;
    private User member; // Representerer brukeren vi viser oppgaver for

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (usernameTitle.getScene() != null) {
                usernameTitle.getScene().getWindow().setWidth(800);
                usernameTitle.getScene().getWindow().setHeight(600);
            }
        });

        // Konfigurer tabellkolonner
        dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());
        priorityColumn.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        detailsColumn.setCellValueFactory(cellData -> cellData.getValue().detailsProperty());

        taskTable.setItems(taskList);
        taskTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Prioritetsfarge
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

        // Knappehendelser
        newTaskButton.setOnAction(e -> handleCreateTask());
        filterButton.setOnAction(e -> handleFilter());

        taskTable.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (taskTable.getSelectionModel().getSelectedItem() == null) {
                taskTable.getSelectionModel().clearSelection();
            }
        });
    }

    /**
     * Setter hvilken bruker (medlem) denne visningen handler om.
     */
    public void setMember(User member) {
        this.member = member;
        if (usernameTitle != null && member != null) {
            usernameTitle.setText(member.getUsername() + " tasks");
        }
        loadTasks();
    }

    /**
     * Setter navigation controller for sidebytte.
     */
    public void setNavigation(NavigationController navigation) {
        this.navigation = navigation;
    }

    /**
     * Laster inn alle oppgaver for medlemmet.
     */
    public void loadTasks() {
        if (member == null) return;

        try {
            taskList.setAll(taskRepository.fetchTasks(member.getId()));
        } catch (SQLException e) {
            System.err.println("Failed to load tasks for member: " + e.getMessage());
        }
    }

    /**
     * Åpner en enkel dialog for å lage en ny oppgave og lagrer den via createTaskForMember.
     */
    private void handleCreateTask() {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("New Task for " + member.getUsername());

        ButtonType createBtnType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);

        // Inputfelt
        DatePicker dueDate = new DatePicker(LocalDate.now());
        ComboBox<TaskPriority> priority = new ComboBox<>();
        priority.getItems().addAll(TaskPriority.values());
        priority.setValue(TaskPriority.MEDIUM);

        TextField description = new TextField();
        TextArea details = new TextArea();

        description.setPromptText("Short description");
        details.setPromptText("Details");

        VBox content = new VBox(10,
                new Label("Due date:"), dueDate,
                new Label("Priority:"), priority,
                new Label("Description:"), description,
                new Label("Details:"), details
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createBtnType) {
                return new Task(0,
                        description.getText(),
                        dueDate.getValue(),
                        priority.getValue(),
                        details.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(task -> {
            try {
                int newTaskId = taskRepository.createTaskForMember(task, member.getId(), member.getId());
                if (newTaskId != -1) {
                    task.setTaskId(newTaskId);
                    taskList.add(task);
                }
            } catch (Exception e) {
                System.err.println("Failed to create task: " + e.getMessage());
            }
        });
    }

    /**
     * Placeholder for filtrering – kan implementeres senere.
     */
    private void handleFilter() {
        // TODO: Legg til popup-filtrering hvis ønsket
        System.out.println("Filter triggered (not implemented)");
    }
}

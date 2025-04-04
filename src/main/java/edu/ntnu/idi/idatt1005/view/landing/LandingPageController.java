package edu.ntnu.idi.idatt1005.view.landing;

import edu.ntnu.idi.idatt1005.app.AppState;
import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.model.Task;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.repository.StatisticsRepository;
import edu.ntnu.idi.idatt1005.repository.TaskRepository;
import edu.ntnu.idi.idatt1005.model.TaskPriority;
import edu.ntnu.idi.idatt1005.util.SoundPlayer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Controller for the landing/dashboard page.
 * Handles task loading, filtering, and navigation.
 * Author: KrissKN
 */
public class LandingPageController {

  public TextField searchField;
  // Task table and columns
  @FXML
  private TableView<Task> taskTable;
  @FXML private TableColumn<Task, LocalDate> dueDateColumn;
  @FXML private TableColumn<Task, TaskPriority> priorityColumn;
  @FXML private TableColumn<Task, String> descriptionColumn;
  @FXML private TableColumn<Task, String> responsibilityColumn;
  @FXML private TableColumn<Task, String> detailsColumn;

  // Top & side buttons
  @FXML private Button filterButton;
  @FXML private Button newTaskButton;
  @FXML private Button logoutButton;
  @FXML private Button completeTaskButton;
  @FXML private Button homeButton;
  @FXML private Button calendarButton;
  @FXML private Button statisticsButton;
  @FXML private Button householdButton;

  @FXML private Label usernameLabel;

  private final ObservableList<Task> masterTaskList = FXCollections.observableArrayList();
  private final TaskRepository taskRepository = new TaskRepository();

  private NavigationController navigation;
  private User user;

  /**
   * Initializes the controller. Loads table config and button actions.
   */
  @FXML
  public void initialize() {
    Platform.runLater(() -> {
      if (usernameLabel.getScene() != null) {
        usernameLabel.getScene().getWindow().setWidth(800);
        usernameLabel.getScene().getWindow().setHeight(600);
      }
    });

    // Setup table columns
    dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());
    priorityColumn.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());
    descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
    responsibilityColumn.setCellValueFactory(cellData -> cellData.getValue().responsibilityProperty());
    detailsColumn.setCellValueFactory(cellData -> cellData.getValue().detailsProperty());

    taskTable.setItems(masterTaskList);
    taskTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // Add color style to priority
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

    // Show/hide complete button on selection
    taskTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) ->
      completeTaskButton.setVisible(selected != null));

    taskTable.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
      if (taskTable.getSelectionModel().getSelectedItem() == null) {
        completeTaskButton.setVisible(false);
      }
    });

    completeTaskButton.setOnAction(e -> handleCompleteTask());

    setupButtonActions();
    updateUsername();
  }

  /**
   * Loads all tasks for the logged-in user.
   */
  public void loadTasks() {
    try {
      masterTaskList.setAll(taskRepository.fetchTasks(user.getId()));
    } catch (SQLException e) {
      System.err.println("Failed to fetch tasks: " + e.getMessage());
    }
  }

  /**
   * Handles task deletion.
   */
  private void handleCompleteTask() {
    Task selected = taskTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
      try {
        new StatisticsRepository().completeTask(selected.getTaskId(), user.getId());
        masterTaskList.remove(selected);
        completeTaskButton.setVisible(false);
        taskTable.getSelectionModel().clearSelection();
      } catch (SQLException e) {
        System.err.println("Failed to delete task: " + e.getMessage());
      }
    }
  }

  /**
   * Sets the navigation controller.
   *
   * @param navigation the app’s navigation controller
   */
  public void setNavigation(NavigationController navigation) {
    this.navigation = navigation;
  }

  /**
   * Sets the current logged-in user.
   *
   * @param user the user
   */
  public void setUser(User user) {
    this.user = user;
    updateUsername();
  }

  /**
   * Sets up all button actions (routing + logic).
   */
  private void setupButtonActions() {
    newTaskButton.setOnAction(e -> {
      TaskInformation taskInfoPage = new TaskInformation(navigation.getStage(), navigation);
      taskInfoPage.show();
    });

    filterButton.setOnAction(e -> showFilterPopup());

    logoutButton.setOnAction(e -> {
      AppState.getInstance().clear();
      SoundPlayer.playSound("/audio/shutdown.mp3");;
      navigation.goToLoginPage();
    });

    homeButton.setOnAction(e -> navigation.goToLandingPage());
    calendarButton.setOnAction(e -> navigation.goToCalendarPage());
    statisticsButton.setOnAction(e -> navigation.goToStatisticsPage());
    householdButton.setOnAction(e -> navigation.goToHouseholdPage());
  }

  private void updateUsername() {
    if (usernameLabel != null && user != null) {
      usernameLabel.setText("Hello, " + user.getUsername() + "!");
    }
  }

  // Filtering logic unchanged
  private void showFilterPopup() {
    Popup filterPopup = new Popup();
    filterPopup.setAutoHide(true);

    VBox content = new VBox(10);
    content.getStyleClass().add("filter-popup");
    content.setPadding(new Insets(15));
    content.setMaxWidth(300);

    Label priorityLabel = new Label("Priority:");
    ComboBox<TaskPriority> priorityFilter = new ComboBox<>();
    priorityFilter.getItems().add(null);
    priorityFilter.getItems().addAll(TaskPriority.values());

    Label responsibilityLabel = new Label("Responsibility:");
    TextField responsibilityFilter = new TextField();

    Label dueDateLabel = new Label("Due by date:");
    DatePicker dueDatePicker = new DatePicker();

    Button apply = new Button("Apply");
    Button reset = new Button("Reset");

    HBox buttons = new HBox(10, reset, apply);
    buttons.setAlignment(Pos.CENTER_RIGHT);

    content.getChildren().addAll(priorityLabel, priorityFilter,
      responsibilityLabel, responsibilityFilter,
      dueDateLabel, dueDatePicker,
      buttons);
    filterPopup.getContent().add(content);

    apply.setOnAction(event -> {
      FilteredList<Task> filtered = new FilteredList<>(masterTaskList, task -> {
        boolean matchesPriority = priorityFilter.getValue() == null ||
          task.getPriority().equals(priorityFilter.getValue());

        boolean matchesResponsibility = responsibilityFilter.getText().isBlank() ||
          (task.getResponsibility() != null &&
            task.getResponsibility().toLowerCase().contains(responsibilityFilter.getText().toLowerCase()));

        boolean matchesDate = dueDatePicker.getValue() == null ||
          !task.getDueDate().isAfter(dueDatePicker.getValue());

        return matchesPriority && matchesResponsibility && matchesDate;
      });

      taskTable.setItems(filtered);
      filterPopup.hide();
    });

    reset.setOnAction(event -> {
      priorityFilter.setValue(null);
      responsibilityFilter.clear();
      dueDatePicker.setValue(null);
      taskTable.setItems(masterTaskList);
      filterPopup.hide();
    });

    filterPopup.show(filterButton,
      filterButton.localToScreen(filterButton.getBoundsInLocal()).getMinX(),
      filterButton.localToScreen(filterButton.getBoundsInLocal()).getMaxY());
  }
}

//package edu.ntnu.idi.idatt1005;
//
//import edu.ntnu.idi.idatt1005.model.Task;
//import edu.ntnu.idi.idatt1005.model.TaskPriority;
//import edu.ntnu.idi.idatt1005.view.landing.LandingPageController;
//import javafx.collections.ObservableList;
//import javafx.scene.control.TableView;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class LandingPageControllerTest {
//
//  private LandingPageController controller;
//
//  @BeforeEach
//  void setUp() {
//    controller = new LandingPageController();
//    controller.initialize(); // Optional unless it loads UI components
//  }
//
//  @Test
//  void addAndSelectTask_shouldAppearInTable() {
//    Task task = new Task("Test Task", LocalDate.now(), TaskPriority.LOW, "Unit test task");
//    TableView<Task> table = new TableView<>();
//    table.getItems().add(task);
//    ObservableList<Task> tasks = table.getItems();
//
//    assertFalse(tasks.isEmpty());
//    assertEquals("Test Task", tasks.get(0).getDescription());
//  }
//}

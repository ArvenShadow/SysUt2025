package edu.ntnu.idi.idatt1005.util;

import edu.ntnu.idi.idatt1005.model.TaskPriority;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
* Utility class providing factory methods for custom table cells.
* Used to create specialized cell renderers for TableView components.
*/
public class TableCellFactories {

  /**
  * Default constructor for TableCellFactories.
  */
  public TableCellFactories() {
    // Default constructor implementation
  }

  /**
  * Creates a callback for rendering task priority in table cells.
  * The cells will have different styles based on the priority value.
  *
  * @param <T> the type of object in the TableView
  * @return a callback that creates styled table cells for TaskPriority values
  */
  public static <T> Callback<TableColumn<T, TaskPriority>, TableCell<T, TaskPriority>> priorityCellFactory() {
    return column -> new TableCell<>() {
      @Override
      protected void updateItem(TaskPriority priority, boolean empty) {
        super.updateItem(priority, empty);
        if (empty || priority == null) {
          setText(null);
          setStyle("");
        } else {
          setText(priority.name());
          switch (priority) {
            case LOW -> setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            case MEDIUM -> setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold;");
            case HIGH -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
          }
        }
      }
    };
  }
}
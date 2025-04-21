package edu.ntnu.idi.idatt1005;

import edu.ntnu.idi.idatt1005.app.NavigationController;

import java.net.URL;
import java.util.Objects;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * The {@code TaskManagerApp} class serves as the entry point for the Task Manager application.
 * It extends the {@code Application} class from the JavaFX framework and is responsible
 * for initializing the JavaFX application, setting up the primary {@code Stage}, and
 * navigating to the login page.
 *
 * <p>The application also sets a custom icon for the primary stage and makes use of the
 * {@code NavigationController} to manage navigation between different views in the application.</p>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Launch the Task Manager application by running this class.</li>
 *   <li>The {@link #start(Stage)} method is automatically invoked to initialize the UI.</li>
 * </ul>
 *
 * @see Application
 * @see Stage
 * @see NavigationController
 */

public class TaskManagerApp extends Application {

  /**
  * Default constructor for TaskManagerApp.
  */
  public TaskManagerApp() {
    // Default constructor implementation
  }


  /**
   * Initializes the JavaFX application. This method sets up the primary {@code Stage} by:
   * <ul>
   * <li>Adding a custom icon to the primary stage.</li>
   * <li>Instantiating the {@link NavigationController} to handle navigation.</li>
   * <li>Directing the application to the login page on startup.</li>
   * </ul>
   *
   * @param primaryStage the main stage for the application where the scene will be displayed
   * @throws NullPointerException if the icon resource cannot be found
   */

  @Override
  public void start(Stage primaryStage) {
    primaryStage.getIcons().add(
      new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Icon.png")))
    );

    NavigationController navigation = new NavigationController(primaryStage);
    navigation.goToLoginPage();
  }
  /**
  * Main method to launch the JavaFX application.
  * This method delegates to the JavaFX Application.launch() method.
  *
  * @param args command line arguments
  */
  public static void main(String[] args) {
    launch(args);

    // In your main class or initialization code
    URL fontUrl = TaskManagerApp.class.getClassLoader().getResource("fonts/FugazOne-Regular.ttf");
    System.out.println("Font URL: " + fontUrl);

    URL cssUrl = TaskManagerApp.class.getClassLoader().getResource("loginStyle.css");
    System.out.println("CSS URL: " + cssUrl);

// Check if FXML is loading
    URL fxmlUrl = TaskManagerApp.class.getClassLoader().getResource("LandingPage.fxml");
    System.out.println("FXML URL: " + fxmlUrl);

  }

}

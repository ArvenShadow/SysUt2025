package edu.ntnu.idi.idatt1005.view;

import edu.ntnu.idi.idatt1005.app.AppState;
import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.view.household.HouseHoldController;
import edu.ntnu.idi.idatt1005.view.household.MemberViewController;
import edu.ntnu.idi.idatt1005.view.landing.LandingPageController;
import java.io.IOException;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * Factory class for creating JavaFX views (scenes).
 * Centralizes FXML loading and style application for consistent behavior across the app.
 *
 * @see NavigationController
 */
public class ViewFactory {

  private final NavigationController navigation;

  /**
   * Constructs a ViewFactory with the specified NavigationController.
   *
   * @param navigation The NavigationController to manage page navigation.
   */
  public ViewFactory(NavigationController navigation) {
    this.navigation = navigation;
  }

  /**
   * Creates and returns the login page scene.
   *
   * @return A Scene object containing the login page view.
   */
  public Scene createLoginPage() {
    var loginCard = new edu.ntnu.idi.idatt1005.view.login.LoginCard(navigation);
    Scene scene = new Scene(loginCard, 600, 600);
    scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/loginStyle.css"))
        .toExternalForm());
    return scene;
  }

  /**
   * Creates and returns the landing page scene.
   *
   * <p>This page includes tasks and statistics loading and applies
   * specific stylesheets for consistent appearance. </p>
   *
   *@return A Scene object containing the landing page view.
   */
  public Scene createLandingPage() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/LandingPage.fxml"));
      Parent root = loader.load();
      LandingPageController controller = loader.getController();
      controller.setNavigation(navigation);
      controller.setUser(AppState.getInstance().getCurrentUser());

      // Always load tasks initially, but we can optimize this later if needed
      controller.loadTasks();
      controller.loadStatistics();

      // Reset the reload flag after loading tasks
      AppState.getInstance().setShouldReloadTasks(false);

      Scene scene = new Scene(root);
      scene.getStylesheets().add(Objects.requireNonNull(getClass()
          .getResource("/landingPageStyle.css")).toExternalForm());
      scene.getStylesheets().add(Objects.requireNonNull(getClass()
          .getResource("/landingExtend.css")).toExternalForm());
      return scene;
    } catch (IOException e) {
      e.printStackTrace();
      return new Scene(new BorderPane(), 800, 600); // fallback
    }
  }


  /**
   * Creates and returns the household page scene.
   *
   *<p>Displays household information specific to the logged-in user.</p>
   *
   * @return A Scene object containing the household page view,
   *         or an error scene if no user is logged in or an error occurs.
   */
  public Scene createHouseholdPage() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/Household.fxml"));
      Parent root = loader.load();

      // Get the controller
      HouseHoldController controller = loader.getController();
      controller.setNavigation(navigation);
      controller.setHouseholdName("My Household");

      User currentUser = AppState.getInstance().getCurrentUser();
      if (currentUser != null) {
        controller.setCurrentUserId(currentUser.getId()); // Set the ID for data loading
      } else {
        System.err.println("Error: No user logged in. Cannot initialize Household page properly.");
        return createErrorScene("Error", "User not logged in. Cannot display household.");
      }

      Scene scene = new Scene(root, 800, 600);

      scene.getStylesheets().add(Objects.requireNonNull(getClass()
          .getResource("/household.css")).toExternalForm());

      return scene;
    } catch (IOException | NullPointerException e) {
      System.err.println("Error loading Household FXML or CSS: " + e.getMessage());
      e.printStackTrace();
      return fallback("Household");
    }
  }

  /**
   * Creates and returns the member view page scene for a specific user.
   *
   * @param member The User object representing the member to display.
   * @return A Scene object containing the member view page,
   *         or a fallback scene in case of errors.
   */
  public Scene createMemberView(User member) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/MemberView.fxml"));
      Parent root = loader.load();

      // Make sure to use the correct package name here
      MemberViewController controller = loader.getController();
      controller.setNavigation(navigation);
      controller.setMember(member);

      Scene scene = new Scene(root, 800, 600);
      scene.getStylesheets().add(Objects.requireNonNull(getClass()
          .getResource("/landingExtend.css")).toExternalForm());
      return scene;
    } catch (IOException | NullPointerException e) {
      e.printStackTrace();
      return fallback("MemberView");
    }
  }

  /**
   * Creates a scene displaying a specific error message.
   *
   * @param title   The title for the error scene/window.
   * @param message The error message to display in the scene.
   * @return A Scene object containing the error message view.
   */
  private Scene createErrorScene(String title, String message) {
    BorderPane pane = new BorderPane();
    Label errorLabel = new Label(message);
    pane.setCenter(errorLabel);
    Scene scene = new Scene(pane, 400, 200);
    return scene;
  }



  /**
   * Fallback method for creating a blank scene when loading fails.
   *
   * @param pageName The name of the page that failed to load.
   * @return A blank Scene object to serve as a fallback.
   */
  private Scene fallback(String pageName) {
    System.err.println("Failed to load " + pageName + " page. Showing blank fallback.");
    return new Scene(new BorderPane(), 800, 600);
  }
}

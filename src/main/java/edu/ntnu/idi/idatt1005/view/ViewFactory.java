package edu.ntnu.idi.idatt1005.view;

import edu.ntnu.idi.idatt1005.app.AppState;
import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.view.household.HouseHoldController;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.view.household.MemberViewController;
import edu.ntnu.idi.idatt1005.view.landing.LandingPageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Objects;

/**
 * Factory class for creating JavaFX views (scenes).
 * Centralizes FXML loading and style application for consistent behavior across the app.
 *
 * @see NavigationController
 */
public class ViewFactory {

  private final NavigationController navigation;

  public ViewFactory(NavigationController navigation) {
    this.navigation = navigation;
  }

  public Scene createLoginPage() {
    var loginCard = new edu.ntnu.idi.idatt1005.view.login.LoginCard(navigation);
    Scene scene = new Scene(loginCard, 600, 600);
    scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/loginStyle.css")).toExternalForm());
    return scene;
  }

  public Scene createLandingPage() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/landingPage.fxml"));
      Parent root = loader.load();

      LandingPageController controller = loader.getController();
      controller.setNavigation(navigation);
      controller.setUser(AppState.getInstance().getCurrentUser());
      controller.loadTasks();

      Scene scene = new Scene(root);
      scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/landingPageStyle.css")).toExternalForm());
      scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/landingExtend.css")).toExternalForm());
      return scene;
    } catch (IOException e) {
      e.printStackTrace();
      return new Scene(new javafx.scene.layout.BorderPane(), 800, 600); // fallback
    }
  }

  public Scene createStatisticsPage() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/statisticsPage.fxml"));
      Parent root = loader.load();
      Scene scene = new Scene(root, 800, 600);
      scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/landingExtend.css")).toExternalForm());
      return scene;
    } catch (IOException e) {
      e.printStackTrace();
      return fallback("Statistics");
    }
  }

  public Scene createCalendarPage() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/calendarPage.fxml"));
      Parent root = loader.load();
      Scene scene = new Scene(root, 800, 600);
      scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/landingExtend.css")).toExternalForm());
      return scene;
    } catch (IOException e) {
      e.printStackTrace();
      return fallback("Calendar");
    }
  }

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

      Scene scene = new Scene(root, 800, 600); // Adjust size as needed

      scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/household.css")).toExternalForm());

      return scene;
    } catch (IOException | NullPointerException e) {
      System.err.println("Error loading Household FXML or CSS: " + e.getMessage());
      e.printStackTrace();
      return fallback("Household");
    }
  }

  public Scene createMemberView(User member) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/memberView.fxml"));
      Parent root = loader.load();

      // Make sure to use the correct package name here
      MemberViewController controller = loader.getController();
      controller.setNavigation(navigation);
      controller.setMember(member);

      Scene scene = new Scene(root, 800, 600);
      scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/landingExtend.css")).toExternalForm());
      return scene;
    } catch (IOException | NullPointerException e) {
      e.printStackTrace();
      return fallback("MemberView");
    }
  }

  /**
   * Creates a scene displaying a specific error message.
   *
   * @param title Title for the error scene/window.
   * @param message The error message to display.
   * @return A Scene containing the error message.
   */
  private Scene createErrorScene(String title, String message) {
    BorderPane pane = new BorderPane();
    Label errorLabel = new Label(message);
    pane.setCenter(errorLabel);
    Scene scene = new Scene(pane, 400, 200);
    return scene;
  }



  private Scene fallback(String pageName) {
    System.err.println("Failed to load " + pageName + " page. Showing blank fallback.");
    return new Scene(new javafx.scene.layout.BorderPane(), 800, 600);
  }
}

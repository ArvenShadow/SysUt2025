package edu.ntnu.idi.idatt1005.app;

import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.view.ViewFactory;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Central controller for navigation and scene switching.
 * Uses the ViewFactory to construct scenes.
 */
public class NavigationController {

  private final Stage stage;
  private final ViewFactory viewFactory;

  // CSS file paths
  private static final String MAIN_STYLESHEET = "/landingPageStyle.css";
  private static final String LOGIN_STYLESHEET = "/loginStyle.css";
  private static final String LANDING_EXTEND_STYLESHEET = "/landingExtend.css";
  private static final String HOUSEHOLD_STYLESHEET = "/household.css";

  /**
   * Constructs a NavigationController with the specified stage.
   *
   * @param stage the primary stage for the application
   */
  public NavigationController(Stage stage) {
    this.stage = stage;
    this.viewFactory = new ViewFactory(this);
  }

  /**
   * Navigates to the login page.
   */
  public void goToLoginPage() {
    Scene loginScene = viewFactory.createLoginPage();
    setSceneWithStylesheet(loginScene, "Flowstate - Login", LOGIN_STYLESHEET);
  }

  /**
   * Navigates to the landing page (dashboard).
   */
  public void goToLandingPage() {
    Scene landingScene = viewFactory.createLandingPage();
    setSceneWithStylesheet(landingScene, "Flowstate - Dashboard", MAIN_STYLESHEET);
    stage.setMaximized(false);
    stage.setMaximized(true);
  }
  /**
  * Navigates to the task information page.
  *
  * @param scene the scene containing the task information to display
  */
  public void goToTaskInfoPage(Scene scene) {
    boolean wasMaximized = stage.isMaximized();
    stage.setScene(scene);
    stage.setTitle("Flowstate - Task Information");
    if (wasMaximized) {
      // Force refresh of maximized state
      stage.setMaximized(false);
      stage.setMaximized(true);
    }
    stage.show();
  }


  /**
   * Navigates to the household page.
   */
  public void goToHouseholdPage() {
    Scene householdScene = viewFactory.createHouseholdPage();
    setSceneWithStylesheet(householdScene, "Flowstate - Household", HOUSEHOLD_STYLESHEET);
    stage.setMaximized(false);
    stage.setMaximized(true);
  }

  /**
   * Navigates to the member view page for a specific user.
   *
   * @param member the user whose member view page is to be displayed
   */
  public void goToMemberView(User member) {
    Scene memberScene = viewFactory.createMemberView(member);
    setSceneWithStylesheet(memberScene, "Flowstate - " + member.getUsername(), MAIN_STYLESHEET);
    stage.setMaximized(false);
    stage.setMaximized(true);
  }

  /**
   * Sets the scene with one stylesheet.
   */
  private void setSceneWithStylesheet(Scene scene, String title, String stylesheet) {
    setSceneWithStylesheet(scene, title, stylesheet, null);
  }

  /**
   * Sets the scene with primary and optional secondary stylesheet.
   */
  private void setSceneWithStylesheet(Scene scene, String title, String primaryStylesheet, String secondaryStylesheet) {
    // Clear existing stylesheets to avoid conflicts
    scene.getStylesheets().clear();

    // Add primary stylesheet
    scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(primaryStylesheet)).toExternalForm());

    // Add secondary stylesheet if provided
    if (secondaryStylesheet != null) {
      scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(secondaryStylesheet)).toExternalForm());
    }

    stage.setScene(scene);
    stage.setTitle(title);
    stage.show();
  }

  /**
  * Returns the primary stage used by this navigation controller.
  *
  * @return the application's primary stage
  */
  public Stage getStage() {
    return stage;
  }
}
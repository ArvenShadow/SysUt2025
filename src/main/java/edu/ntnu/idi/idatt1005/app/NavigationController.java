package edu.ntnu.idi.idatt1005.app;

import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.view.ViewFactory;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Central controller for navigation and scene switching.
 * Uses the ViewFactory to construct scenes.
 *
 * Author: KrissKN
 */
public class NavigationController {

  private final Stage stage;
  private final ViewFactory viewFactory;

  public NavigationController(Stage stage) {
    this.stage = stage;
    this.viewFactory = new ViewFactory(this);
  }

  public void goToLoginPage() {
    setScene(viewFactory.createLoginPage(), "Flowstate - Login");
  }

  public void goToLandingPage() {
    setScene(viewFactory.createLandingPage(), "Flowstate - Dashboard");
  }

  public void goToStatisticsPage() {
    setScene(viewFactory.createStatisticsPage(), "Flowstate - Statistics");
  }

  public void goToCalendarPage() {
    setScene(viewFactory.createCalendarPage(), "Flowstate - Calendar");
  }

  public void goToHouseholdPage() {
    setScene(viewFactory.createHouseholdPage(), "Flowstate - Household");
  }

  public void goToMemberView(User member) {
    setScene(viewFactory.createMemberView(member), "Flowstate - " + member.getUsername());
  }

  private void setScene(Scene scene, String title) {
    stage.setScene(scene);
    stage.setTitle(title);
    stage.show();
  }

  public Stage getStage() {
    return stage;
  }
}

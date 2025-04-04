package edu.ntnu.idi.idatt1005;

import edu.ntnu.idi.idatt1005.app.NavigationController;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class TaskManagerApp extends Application {

  @Override
  public void start(Stage primaryStage) {
    primaryStage.getIcons().add(
      new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Icon.png")))
    );

    NavigationController navigation = new NavigationController(primaryStage);
    navigation.goToLoginPage();
  }
}

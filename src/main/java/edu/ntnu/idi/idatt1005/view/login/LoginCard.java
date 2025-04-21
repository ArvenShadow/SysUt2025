package edu.ntnu.idi.idatt1005.view.login;

import edu.ntnu.idi.idatt1005.app.AppState;
import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.db.DatabaseInitializer;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.repository.UserRepository;
import edu.ntnu.idi.idatt1005.util.SoundPlayer;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


/**
 * Represents the login UI card used for authenticating users.
 * Handles login form input, plays sound on success, and navigates
 * to the landing page using {@link NavigationController}.
 *
 */
public class LoginCard extends VBox {

  private final NavigationController navigation;
  private final UserRepository userRepository;

  /**
   * Constructs the LoginCard UI and wires event handlers.
   *
   * @param navigation the application's navigation controller
   */
  public LoginCard(NavigationController navigation) {
    this.navigation = navigation;
    this.userRepository = new UserRepository();

    this.setSpacing(15);
    this.setAlignment(Pos.CENTER);
    this.getStyleClass().addAll("login-page");

    VBox container = new VBox(15);
    container.setAlignment(Pos.CENTER);
    container.getStyleClass().addAll("login-container", "inner-frame");

    BorderPane centeringPane = new BorderPane(container);
    this.getChildren().add(centeringPane);

    Label title = new Label("FlowState");
    title.getStyleClass().add("login-title");

    Label errorLabel = new Label();
    errorLabel.getStyleClass().add("error-label");
    errorLabel.setVisible(false);

    TextField usernameField = new TextField();
    usernameField.setPromptText("Username");
    usernameField.getStyleClass().add("login-input");

    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText("Password");
    passwordField.getStyleClass().add("login-input");

    Button loginButton = new Button("Log in");
    loginButton.setDefaultButton(true);
    loginButton.getStyleClass().add("login-button");

    // Handle login button click
    loginButton.setOnAction(e -> {
      String username = usernameField.getText();
      String password = passwordField.getText();
      handleLogin(username, password, errorLabel, usernameField, passwordField);
    });

    // Allow Enter key to trigger login
    passwordField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ENTER) {
        loginButton.fire();
        e.consume();
      }
    });

    Button signupButton = new Button("Sign Up");
    signupButton.getStyleClass().add("signup-button");

    signupButton.setOnAction(e -> {
      new SignupDialog(navigation.getStage()).show();
    });

    VBox buttonBox = new VBox(10, loginButton, signupButton);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setPadding(new Insets(20, 0, 0, 0));

    container.getChildren().addAll(title, errorLabel, usernameField, passwordField, buttonBox);
  }

  /**
   * Handles login logic: authenticates user, updates AppState, and navigates.
   *
   * @param username      the entered username
   * @param password      the entered password
   * @param errorLabel    label to show errors
   * @param usernameField username input field (for reset)
   * @param passwordField password input field (for reset)
   */
  private void handleLogin(String username, String password, Label errorLabel,
                           TextField usernameField, PasswordField passwordField) {
    try {
      User user = userRepository.authenticateUser(username, password);
      DatabaseInitializer databaseInit = new DatabaseInitializer();

      if (user != null) {
        AppState.getInstance().setCurrentUser(user);  // Store user globally
        databaseInit.initializeDatabase();
        playStartupSound();                           // Feedback
        navigation.goToLandingPage();                 // Navigate
      } else {
        errorLabel.setText("Invalid username or password");
        errorLabel.setVisible(true);
        usernameField.clear();
        passwordField.clear();
        playFadeTransition(errorLabel);
      }
    } catch (Exception e) {
      errorLabel.setText("An error occurred during login.");
      errorLabel.setVisible(true);
      e.printStackTrace();
    }
  }

  /**
   * Plays a startup sound on successful login.
   */
  private void playStartupSound() {
    SoundPlayer.playSound("/audio/startup.mp3");
  }


  /**
   * Fades out the error label after a short delay.
   *
   * @param label the label to fade out
   */
  private void playFadeTransition(Label label) {
    label.setOpacity(1.0);
    FadeTransition fade = new FadeTransition(Duration.seconds(5), label);
    fade.setFromValue(1.0);
    fade.setToValue(0.0);
    fade.setDelay(Duration.seconds(3));
    fade.setOnFinished(e -> label.setVisible(false));
    fade.play();
  }
}

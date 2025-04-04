package edu.ntnu.idi.idatt1005.view.login;

import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.repository.UserRepository;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Dialog for signing up a new user.
 * Uses {@link UserRepository} to create the user.
 *
 * Author: KrissKN
 */
public class SignupDialog extends Dialog<User> {

  private final TextField usernameField = new TextField();
  private final PasswordField passwordField = new PasswordField();
  private final UserRepository userRepository = new UserRepository();

  /**
   * Constructs and shows the signup dialog.
   *
   * @param owner the parent stage
   */
  public SignupDialog(Stage owner) {
    initOwner(owner);
    initStyle(StageStyle.UTILITY);
    setTitle("Sign Up");
    setHeaderText("Create a new Flowstate account");

    ButtonType signupButtonType = new ButtonType("Sign Up", ButtonBar.ButtonData.OK_DONE);
    getDialogPane().getButtonTypes().addAll(signupButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20));

    usernameField.setPromptText("Username");
    passwordField.setPromptText("Password");


    grid.add(new Label("Username:"), 0, 0);
    grid.add(usernameField, 1, 0);
    grid.add(new Label("Password:"), 0, 1);
    grid.add(passwordField, 1, 1);


    getDialogPane().setContent(grid);

    // Handle result
    setResultConverter(dialogButton -> {
      if (dialogButton == signupButtonType) {
        createUser();
      }
      return null;
    });
  }

  /**
   * Attempts to create a new user using form input.
   *
   * @return the created {@link User}, or null if creation fails
   */
  private boolean createUser() {
    String username = usernameField.getText().trim();
    String password = passwordField.getText().trim();

    try {
      if(userRepository.createUser(username, password)) {
        showAlert("Signup successful!");
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      showAlert("Signup failed: " + e.getMessage());
      return false;
    }
  }

  /**
   * Shows an information alert.
   *
   * @param message the message to display
   */
  private void showAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.initOwner(getOwner());
    alert.showAndWait();
  }
}

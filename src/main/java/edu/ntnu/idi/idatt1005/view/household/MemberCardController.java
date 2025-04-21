package edu.ntnu.idi.idatt1005.view.household;

import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.model.Member;
import edu.ntnu.idi.idatt1005.model.User;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

/**
 * Controller class for the Member Card view component.
 * Manages interactions between the Member Card UI and the application data model.
 * Handles navigation to detailed member views when requested by the user.
 */
public class MemberCardController implements Initializable {

  /**
  * Default constructor for MemberCardController.
  */
  public MemberCardController() {

  }

  @FXML private Button viewMoreButton;

  private Member member;
  private NavigationController navigation;

  /**
  * Initializes the controller after FXML elements have been loaded.
  * Sets up button actions and other initialization tasks.
  *
  * @param location The location used to resolve relative paths for the root object
  * @param resources The resources used to localize the root object
  */
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    setButtonActions();
  }

  /**
  * Sets the member to be displayed by this card.
  *
  * @param member The member to associate with this card controller
  */
  public void setMember(Member member) {
    this.member = member;
  }

  /**
  * Configures the actions for the view's buttons.
  * Sets up the viewMoreButton to navigate to the detailed member view
  * when clicked, converting the Member to a User for navigation purposes.
  */
  public void setButtonActions() {
    viewMoreButton.setOnAction(event -> {
      if (member != null && navigation != null) {
        User user = new User(member.getId(), member.getName());
        navigation.goToMemberView(user);
      }
    });
  }


}

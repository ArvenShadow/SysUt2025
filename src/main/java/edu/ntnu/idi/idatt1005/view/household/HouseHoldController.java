package edu.ntnu.idi.idatt1005.view.household;

import edu.ntnu.idi.idatt1005.app.AppState;
import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.model.Member;
import edu.ntnu.idi.idatt1005.model.MemberStat;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.repository.MemberRepository;
import edu.ntnu.idi.idatt1005.util.SoundPlayer;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Controller for the household view that displays and manages household members.
 *
 * <p>This controller handles the following primary functions:
 * <ul>
 *   <li>Displaying a list of household members and their statistics</li>
 *   <li>Adding new members to the household</li>
 *   <li>Removing members from the household</li>
 *   <li>Navigating to member details view</li>
 *   <li>Managing member selection and interactions</li>
 * </ul>
 *
 * <p>The controller maintains an observable list of {@link Member} objects and their
 * associated {@link MemberStat} data retrieved from the database through the
 * {@link MemberRepository}. It provides property bindings for JavaFX controls
 * to support reactive UI updates.
 *
 * <p>Navigation between views is handled through the {@link NavigationController}.
 */
public class HouseHoldController implements Initializable {

  /**
  * Default constructor for HouseHoldController.
  */
  public HouseHoldController() {
  }

  @FXML private Button deleteMemberButton;
  @FXML private Button homeButton;
  @FXML private Button addMemberButton;
  @FXML private Button logoutButton;
  @FXML private FlowPane membersContainer;
  @FXML private Text householdNameText;
  @FXML private Label usernameLabel;


  private NavigationController navigation;
  private Member selectedMember = null;
  private MemberCard selectedCard = null;

  // Properties
  private final StringProperty householdName = new SimpleStringProperty();
  private final ListProperty<Member> members =
      new SimpleListProperty<>(FXCollections.observableArrayList());
  private final Map<Integer, MemberStat> memberStats = new HashMap<>();

  // Repository for data access
  private final MemberRepository memberRepository = new MemberRepository();

  // Current user ID - this would be set from login session
  private int currentUserId = 1; // Default value, should be set from login session
  private User user;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Bind UI elements to properties
    householdNameText.textProperty().bind(householdNameProperty());


    // Initialize the members list listener to update UI when members change
    members.addListener((observable, oldValue, newValue) -> {
      refreshMembersView();
    });

    setupEventHandlers();
    loadMembersData();
    updateUsername();
  }

  private void setupEventHandlers() {
    addMemberButton.setOnAction(event -> handleAddMember());

    homeButton.setOnAction(e ->
            navigation.goToLandingPage());

    deleteMemberButton.setOnAction(e -> handleDeleteMember());

    logoutButton.setOnAction(e -> {
      AppState.getInstance().clear();
      SoundPlayer.playSound("/audio/shutdown.mp3");
      navigation.goToLoginPage();
    });
  }

  private void handleAddMember() {
    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Write Member Name");

    // Add OK and Cancel buttons
    ButtonType okButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

    // Create TextField and layout
    TextField inputName = new TextField();
    inputName.setPromptText("Enter member name");

    VBox content = new VBox(10, new Label("Name:"), inputName);
    dialog.getDialogPane().setContent(content);

    // Handle result conversion
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == okButtonType) {
        return inputName.getText();
      }
      return null;
    });

    // Show dialog and wait for input
    Optional<String> result = dialog.showAndWait();

    result.ifPresent(name -> {
      try {
        Member newMember = new Member(name, currentUserId);
        memberRepository.addMember(newMember);

        int newId = newMember.getId();

        MemberStat newStat = new MemberStat(newId, 0, 0, 0);
        memberStats.put(newId, newStat);
        members.add(newMember);
      } catch (SQLException e) {
        System.err.println("Error adding member: " + e.getMessage());
      }
    });
  }

  private void handleDeleteMember() {
    if (selectedMember == null) {
      return;
    }

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Confirm Deletion");
    confirm.setHeaderText("Delete member '" + selectedMember.getName() + "'?");
    confirm.setContentText("This will remove the member and all their data.");

    Optional<ButtonType> result = confirm.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      try {
        memberRepository.removeMember(selectedMember.getId());

        members.remove(selectedMember);
        memberStats.remove(selectedMember.getId());

        selectedMember = null;
        deleteMemberButton.setVisible(false);
      } catch (SQLException e) {
        System.err.println("Failed to delete member: " + e.getMessage());
        new Alert(Alert.AlertType.ERROR, "Failed to delete member.").showAndWait();
      }
    }
  }

  /**
   * Sets the NavigationController instance.
   * This method should be called by the ViewFactory after loading the FXML.
   *
   * @param navigation The NavigationController instance.
   */
  public void setNavigation(NavigationController navigation) {
    this.navigation = navigation;
  }

  /**
  * Navigates to the detailed view for a specific household member.
  *
  * This method creates a User object from the Member's data and uses the navigation
  * controller to display the member details view. It includes null-checking to prevent
  * NullPointerExceptions.
  *
  * @param member the Member object whose details should be displayed
  */
  public void showMemberDetails(Member member) {
    if (navigation != null && member != null) {
      User user = new User(member.getId(), member.getName());
      navigation.goToMemberView(user);
    }
  }



  private void refreshMembersView() {
    // Clear existing member cards
    membersContainer.getChildren().clear();

    // Create and add member cards for each member
    for (Member member : members) {
      try {
        // FIXED: Always update stats for this member before creating the card
        updateMemberStats(member.getId());

        // Get updated stats
        MemberStat stat = memberStats.get(member.getId());

        // Create and add the card
        MemberCard card = new MemberCard(member, stat, this);
        card.setOnMouseClicked(e -> {
          if (selectedCard != null) {
            selectedCard.getStyleClass().remove("selected");
          }

          selectedCard = card;
          selectedCard.getStyleClass().add("selected");

          selectedMember = member;
          deleteMemberButton.setVisible(true);
        });
        membersContainer.getChildren().add(card);
      } catch (Exception e) {
        System.err.println("Error creating member card: " + e.getMessage());
      }
    }
  }

  private void loadMembersData() {
    try {
      // Fetch members from database
      List<Member> memberList = memberRepository.fetchMembers(currentUserId);

      // Fetch stats for each member
      for (Member member : memberList) {
        MemberStat stat = memberRepository.fetchMemberStatistics(member);
        memberStats.put(member.getId(), stat);
      }

      // Update the observable list
      members.setAll(memberList);

      // Check if tasks have been completed elsewhere and refresh
      if (AppState.getInstance().shouldReloadTasks()) {
        refreshMemberStats();
        AppState.getInstance().setShouldReloadTasks(false);
      }
    } catch (SQLException e) {
      System.err.println("Error loading members: " + e.getMessage());
    }
  }

  /**
   * Refreshes all member statistics from the database.
   */
  public void refreshMemberStats() {
    try {
      for (Member member : members) {
        updateMemberStats(member.getId());
      }
      refreshMembersView(); // Refresh the UI
    } catch (Exception e) {
      System.err.println("Error refreshing member stats: " + e.getMessage());
    }
  }

  /**
   * Updates a member's stats by counting tasks from the task_assignments table.
   * This method should be called whenever task assignments change.
   * @param memberId ID of updated member
   */
  public void updateMemberStats(int memberId) {
    try {
      Member member = members.stream()
                    .filter(m -> m.getId() == memberId)
                    .findFirst()
                    .orElse(null);

      if (member == null) {
        return;
      }
      // Fetch fresh stats from DB using your updated repo method
      MemberStat updated = memberRepository.fetchMemberStatistics(member);

      memberStats.put(memberId, updated); // Overwrite cached version

    } catch (SQLException e) {
      System.err.println("Failed to update member stats for " + memberId + ": " + e.getMessage());
    }
  }


  /**
   * Returns the property containing the household name.
   *
   * <p>This property is used for JavaFX data binding to UI elements that display
   * or modify the household name.
   *
   * @return the {@link StringProperty} containing the household name
   */
  public StringProperty householdNameProperty() {
    return householdName;
  }

  /**
  * Sets the name of the current household.
  *
  * <p>Updates the householdName property which is bound to UI elements
  * displaying the household name.
  *
  * @param name the name to set for the household
  */
  public void setHouseholdName(String name) {
    householdName.set(name);
  }

  /**
   * Sets the ID of the current user and refreshes household data.
   *
   * <p>This method updates the controller's reference to the current user
   * and reloads all member data for this user's household from the database.
   *
   * @param userId the ID of the current user
   */
  public void setCurrentUserId(int userId) {
    this.currentUserId = userId;
    loadMembersData(); // Reload data for the new user
  }


  /**
   * Sets the current logged-in user and updates the UI.
   *
   * @param user the user
   */
  public void setUser(User user) {
    this.user = user;
    updateUsername();
  }

  /**
   * Updates the username label with the current user's username.
   */
  private void updateUsername() {
    if (usernameLabel != null && user != null) {
      usernameLabel.setText("Hello, " + user.getUsername() + "!");
    }
  }

}
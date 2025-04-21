package edu.ntnu.idi.idatt1005.view.household;

import edu.ntnu.idi.idatt1005.model.Member;
import edu.ntnu.idi.idatt1005.model.MemberStat;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

/**
 * A card component that displays member information and statistics.
 * Extends VBox to create a vertical layout containing member details,
 * task statistics, and action buttons.
 */
public class MemberCard extends VBox {
  private final Member member;
  private final MemberStat memberStat;
  private final HouseHoldController controller;

  /**
   * Creates a new MemberCard with the specified member data and controller.
   *
   * @param member The member to display information for
   * @param memberStat Statistics about the member's tasks
   * @param controller Reference to the household controller for handling actions
   */
  public MemberCard(Member member, MemberStat memberStat, HouseHoldController controller) {
    this.member = member;
    this.memberStat = memberStat;
    this.controller = controller;

    this.getStyleClass().add("member-card");

    // Set up card container
    this.setAlignment(Pos.CENTER);
    this.setSpacing(8);
    this.setPadding(new Insets(10));
    this.setPrefWidth(180);

    // Avatar with initials
    Circle avatar = new Circle(25, Color.WHITE);
    avatar.setStroke(Color.LIGHTGRAY);

    String initials = getInitials(member.getName());
    Label avatarLabel = new Label(initials);
    avatarLabel.setFont(new Font(14));

    StackPane avatarPane = new StackPane(avatar, avatarLabel);
    avatarPane.setAlignment(Pos.CENTER);

    // Member info
    Label nameLabel = new Label(member.getName());
    nameLabel.setFont(new Font(16));

    Label memberLabel = new Label("Member");
    memberLabel.setFont(new Font(12));
    memberLabel.setTextFill(Color.GRAY);

    // Task statistics with bindings
    Label ongoingLabel = new Label();
    Label completedLabel = new Label();
    Label totalLabel = new Label();

    ongoingLabel.textProperty().bind(
                memberStat.ongoingTasksProperty().asString("Tasks Ongoing: %d"));

    completedLabel.textProperty().bind(
                memberStat.completedTasksProperty().asString("Tasks Completed: %d"));

    totalLabel.textProperty().bind(
                new SimpleStringProperty("Progression: ").concat(
                        memberStat.completedTasksProperty().asString()
                                .concat("/").concat(memberStat.totalTasksProperty().asString())
                )
    );

    // Create buttons container (HBox for side-by-side placement)
    HBox buttonsContainer = new HBox();
    buttonsContainer.setAlignment(Pos.CENTER);
    buttonsContainer.setSpacing(10);


    // View More Button (left)
    Button viewMoreBtn = new Button("View More");
    viewMoreBtn.setOnAction(e -> controller.showMemberDetails(member));

    // Add buttons to container with appropriate spacing
    buttonsContainer.getChildren().addAll(viewMoreBtn);

    // Set button alignment
    HBox.setHgrow(viewMoreBtn, Priority.ALWAYS);
    viewMoreBtn.setMaxWidth(Double.MAX_VALUE);

    // Add all components to the card
    this.getChildren().addAll(
                avatarPane,
                nameLabel,
                memberLabel,
                ongoingLabel,
                completedLabel,
                totalLabel,
                buttonsContainer
    );
  }

  /**
   * Extracts the initials from a person's name.
   *
   * @param name The full name to extract initials from
   * @return A string containing the uppercase first letter of each word in the name
   */
  private String getInitials(String name) {
    if (name == null || name.isEmpty()) {
      return "";
    }

    String[] parts = name.split("\\s+");
    StringBuilder initials = new StringBuilder();

    for (String part : parts) {
      if (!part.isEmpty()) {
        initials.append(part.charAt(0));
      }
    }

    return initials.toString().toUpperCase();
  }
}
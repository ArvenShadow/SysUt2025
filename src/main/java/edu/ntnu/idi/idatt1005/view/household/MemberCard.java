package edu.ntnu.idi.idatt1005.view.household;

import edu.ntnu.idi.idatt1005.model.Member;
import edu.ntnu.idi.idatt1005.model.MemberStat;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

public class MemberCard extends VBox {
    private final Member member;
    private final MemberStat memberStat;
    private final HouseHoldController controller;

    public MemberCard(Member member, MemberStat memberStat, HouseHoldController controller) {
        this.member = member;
        this.memberStat = memberStat;
        this.controller = controller;

        // Set up card container
        this.setAlignment(Pos.CENTER);
        this.setSpacing(8);
        this.setPadding(new Insets(10));
        this.setStyle("-fx-background-color: white; -fx-border-color: lightgray; -fx-border-radius: 10; -fx-padding: 10;");
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
                memberStat.ongoingTasksProperty().add(memberStat.completedTasksProperty())
                        .asString("Total completion: %d").concat("/").concat(memberStat.totalTasksProperty().asString()));

        // Create buttons container (HBox for side-by-side placement)
        HBox buttonsContainer = new HBox();
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.setSpacing(10);


        // View More Button (left)
        Button viewMoreBtn = new Button("View More");
        viewMoreBtn.setOnAction(e -> controller.showMemberDetails(member));

        // Plus Button (right) for assigning tasks
        Button addTaskBtn = new Button("+");
        addTaskBtn.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        addTaskBtn.setOnAction(e -> controller.assignTaskToMember(member));

        // Add buttons to container with appropriate spacing
        buttonsContainer.getChildren().addAll(viewMoreBtn, addTaskBtn);

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

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "";

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
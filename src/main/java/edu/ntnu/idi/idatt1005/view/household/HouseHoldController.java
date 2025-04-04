package edu.ntnu.idi.idatt1005.view.household;

import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.model.Task;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.repository.MemberRepository;
import edu.ntnu.idi.idatt1005.repository.TaskRepository;
import edu.ntnu.idi.idatt1005.model.Member;
import edu.ntnu.idi.idatt1005.model.MemberStat;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class HouseHoldController implements Initializable {

    @FXML private Button homeButton;
    @FXML private Button addMemberButton;
    @FXML private FlowPane membersContainer; // This should be a FlowPane in your FXML for better card layout
    @FXML private Text householdNameText;

    private NavigationController navigation;

    // Properties
    private final StringProperty householdName = new SimpleStringProperty();
    private final ListProperty<Member> members = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final Map<Integer, MemberStat> memberStats = new HashMap<>();

    // Repository for data access
    private final MemberRepository memberRepository = new MemberRepository();

    // Current user ID - this would be set from login session
    private int currentUserId = 1; // Default value, should be set from login session

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
    }

    private void setupEventHandlers() {
        addMemberButton.setOnAction(event -> handleAddMember());

        homeButton.setOnAction(e ->
            navigation.goToLandingPage());
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

                // You might need to fetch the ID again if it's auto-generated in DB
                int newId = newMember.getId();

                MemberStat newStat = new MemberStat(newId, 0, 0, 0);
                memberStats.put(newId, newStat);
                members.add(newMember);
            } catch (SQLException e) {
                System.err.println("Error adding member: " + e.getMessage());
            }
        });
    }

    /**
     * Sets the NavigationController instance.
     * This method should be called by the ViewFactory after loading the FXML.
     * @param navigation The NavigationController instance.
     */
    public void setNavigation(NavigationController navigation) {
        this.navigation = navigation;
    }

    public void showMemberDetails(Member member) {
        if (navigation != null && member != null) {
            User user = new User(member.getId(), member.getName());
            navigation.goToMemberView(user);
        }
    }



    public void assignTaskToMember(Member member) {
        // Logic to assign task to member
        System.out.println("Assigning task to " + member.getName());
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
        } catch (SQLException e) {
            System.err.println("Error loading members: " + e.getMessage());
            // Show error to user
        }
    }

    /**
     * Updates a member's stats by counting tasks from the task_assignments table
     * This method should be called whenever task assignments change
     */
    public void updateMemberStats(int memberId) {
        TaskRepository taskRepo = new TaskRepository();
        MemberStat stat = memberStats.get(memberId);

        // FIXED: Create a new stat object if none exists
        if (stat == null) {
            stat = new MemberStat(memberId, 0, 0, 0);
            memberStats.put(memberId, stat);
        }

        try {
            // Get all tasks assigned to this member
            List<Task> assignedTasks = taskRepo.fetchTasksAssignedToMember(memberId);

            // For now, we're treating all tasks as "ongoing" since we don't have
            // a completion status in the task model yet
            int totalTasks = assignedTasks.size();
            int ongoingTasks = totalTasks; // All tasks are considered ongoing for now
            int completedTasks = 0;        // We would need to implement task completion logic

            // Update the member's statistics
            stat.setTotalTasks(totalTasks);
            stat.setOngoingTasks(ongoingTasks);
            stat.setCompletedTasks(completedTasks);

        } catch (SQLException e) {
            System.err.println("Error updating member stats: " + e.getMessage());
        }
    }


    // Property accessors
    public StringProperty householdNameProperty() {
        return householdName;
    }

    public ListProperty<Member> membersProperty() {
        return members;
    }

    // Value getters and setters
    public String getHouseholdName() {
        return householdName.get();
    }

    public void setHouseholdName(String name) {
        householdName.set(name);
    }

    public ObservableList<Member> getMembers() {
        return members.get();
    }

    public void setMembers(ObservableList<Member> members) {
        this.members.set(members);
    }

    public void addMember(Member member) {
        this.members.add(member);
    }

    public void removeMember(Member member) {
        this.members.remove(member);
    }

    // Method to set current user ID (should be called from the login process)
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        loadMembersData(); // Reload data for the new user
    }
}
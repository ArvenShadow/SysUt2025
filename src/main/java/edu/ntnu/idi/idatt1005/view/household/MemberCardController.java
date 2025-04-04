package edu.ntnu.idi.idatt1005.view.household;

import edu.ntnu.idi.idatt1005.app.NavigationController;
import edu.ntnu.idi.idatt1005.model.User;
import edu.ntnu.idi.idatt1005.model.Member;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class MemberCardController implements Initializable {

    @FXML private Button viewMoreButton;
    @FXML private Button addTaskButton;

    private Member member;
    private NavigationController navigation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setButtonActions();
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void setNavigation(NavigationController navigation) {
        this.navigation = navigation;
    }

    public void setButtonActions() {
        viewMoreButton.setOnAction(event -> {
            if (member != null && navigation != null) {
                User user = new User(member.getId(), member.getName());
                navigation.goToMemberView(user);
            }
        });

        addTaskButton.setOnAction(event -> {
            System.out.println("Assigning task to " + member.getName());
            // Evt: åpne oppgave-dialog for medlem
        });
    }
}

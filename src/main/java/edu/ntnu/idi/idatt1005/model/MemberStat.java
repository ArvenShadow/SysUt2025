package edu.ntnu.idi.idatt1005.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MemberStat {
    private final IntegerProperty memberId = new SimpleIntegerProperty();
    private final IntegerProperty ongoingTasks = new SimpleIntegerProperty();
    private final IntegerProperty completedTasks = new SimpleIntegerProperty();
    private final IntegerProperty totalTasks = new SimpleIntegerProperty();

    public MemberStat(int memberId, int ongoingTasks, int completedTasks, int totalTasks) {
        setMemberId(memberId);
        setOngoingTasks(ongoingTasks);
        setCompletedTasks(completedTasks);
        setTotalTasks(totalTasks);
    }

    // Property getters
    public IntegerProperty memberIdProperty() {
        return memberId;
    }

    public IntegerProperty ongoingTasksProperty() {
        return ongoingTasks;
    }

    public IntegerProperty completedTasksProperty() {
        return completedTasks;
    }

    public IntegerProperty totalTasksProperty() {
        return totalTasks;
    }

    // Value getters
    public int getMemberId() {
        return memberId.get();
    }

    public int getOngoingTasks() {
        return ongoingTasks.get();
    }

    public int getCompletedTasks() {
        return completedTasks.get();
    }

    public int getTotalTasks() {
        return totalTasks.get();
    }

    // Value setters
    public void setMemberId(int memberId) {
        this.memberId.set(memberId);
    }

    public void setOngoingTasks(int ongoingTasks) {
        this.ongoingTasks.set(ongoingTasks);
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks.set(completedTasks);
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks.set(totalTasks);
    }
}
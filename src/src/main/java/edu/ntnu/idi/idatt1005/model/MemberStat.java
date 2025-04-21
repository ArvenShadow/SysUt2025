package edu.ntnu.idi.idatt1005.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Represents the statistics of a member, including ongoing, completed, and total tasks.
 * This class uses JavaFX properties to allow for data binding in a UI context.
 */
public class MemberStat {
  private final IntegerProperty memberId = new SimpleIntegerProperty();
  private final IntegerProperty ongoingTasks = new SimpleIntegerProperty();
  private final IntegerProperty completedTasks = new SimpleIntegerProperty();
  private final IntegerProperty totalTasks = new SimpleIntegerProperty();

  /**
   * Constructs a new MemberStat object with the specified values.
   *
   * @param memberId       the ID of the member
   * @param ongoingTasks   the number of ongoing tasks
   * @param completedTasks the number of completed tasks
   * @param totalTasks     the total number of tasks
   */
  public MemberStat(int memberId, int ongoingTasks, int completedTasks, int totalTasks) {
    setMemberId(memberId);
    setOngoingTasks(ongoingTasks);
    setCompletedTasks(completedTasks);
    setTotalTasks(totalTasks);
  }

  /**
   * Gets the member ID property.
   *
   * @return the member ID property
   */
  public IntegerProperty ongoingTasksProperty() {
    return ongoingTasks;
  }

  /**
   * Gets the completed tasks property.
   *
   * @return the completed tasks property
   */
  public IntegerProperty completedTasksProperty() {
    return completedTasks;
  }

  /**
   * Gets the total tasks' property.
   *
   * @return the total tasks property
   */
  public IntegerProperty totalTasksProperty() {
    return totalTasks;
  }

  /**
   * Sets the member ID value.
   *
   * @param memberId the new member ID
   */
  public void setMemberId(int memberId) {
    this.memberId.set(memberId);
  }

  /**
   * Sets the number of ongoing tasks.
   *
   * @param ongoingTasks the new number of ongoing tasks
   */
  public void setOngoingTasks(int ongoingTasks) {
    this.ongoingTasks.set(ongoingTasks);
  }

  /**
   * Sets the number of completed tasks.
   *
   * @param completedTasks the new number of completed tasks
   */
  public void setCompletedTasks(int completedTasks) {
    this.completedTasks.set(completedTasks);
  }

  /**
   * Sets the total number of tasks.
   *
   * @param totalTasks the new total number of tasks
   */
  public void setTotalTasks(int totalTasks) {
    this.totalTasks.set(totalTasks);
  }
}
package edu.ntnu.idi.idatt1005;

import edu.ntnu.idi.idatt1005.model.MemberStat;
import javafx.beans.property.IntegerProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MemberStatTest {

  @Test
  public void testConstructor() {
    // Arrange & Act
    MemberStat stat = new MemberStat(1, 5, 10, 15);

    // Assert
    assertEquals(5, stat.ongoingTasksProperty().get());
    assertEquals(10, stat.completedTasksProperty().get());
    assertEquals(15, stat.totalTasksProperty().get());
  }

  @Test
  public void testProperties() {
    // Arrange
    MemberStat stat = new MemberStat(1, 2, 3, 5);

    // Act & Assert
    IntegerProperty ongoingProperty = stat.ongoingTasksProperty();
    IntegerProperty completedProperty = stat.completedTasksProperty();
    IntegerProperty totalProperty = stat.totalTasksProperty();

    // Properties should not be null
    assertNotNull(ongoingProperty);
    assertNotNull(completedProperty);
    assertNotNull(totalProperty);

    // Values should match constructor inputs
    assertEquals(2, ongoingProperty.get());
    assertEquals(3, completedProperty.get());
    assertEquals(5, totalProperty.get());
  }

  @Test
  public void testSetters() {
    // Arrange
    MemberStat stat = new MemberStat(1, 0, 0, 0);

    // Act
    stat.setOngoingTasks(3);
    stat.setCompletedTasks(7);
    stat.setTotalTasks(10);

    // Assert
    assertEquals(3, stat.ongoingTasksProperty().get());
    assertEquals(7, stat.completedTasksProperty().get());
    assertEquals(10, stat.totalTasksProperty().get());
  }

  @Test
  public void testPropertyBinding() {
    // Arrange
    MemberStat stat = new MemberStat(1, 5, 10, 15);
    final int[] propertyChangeCount = {0}; // Using array to modify in lambda

    // Act
    stat.ongoingTasksProperty().addListener((observable, oldValue, newValue) -> {
      propertyChangeCount[0]++;
      assertEquals(8, newValue);
    });

    stat.setOngoingTasks(8);

    // Assert
    assertEquals(1, propertyChangeCount[0]);
    assertEquals(8, stat.ongoingTasksProperty().get());
  }

  @Test
  public void testPropertyConsistency() {
    // Arrange
    MemberStat stat1 = new MemberStat(1, 5, 10, 15);
    MemberStat stat2 = new MemberStat(2, 5, 10, 15);

    // Act & Assert
    // Two different instances should have different property objects
    assertNotSame(stat1.ongoingTasksProperty(), stat2.ongoingTasksProperty());

    // But the same instance should return the same property object each time
    assertSame(stat1.ongoingTasksProperty(), stat1.ongoingTasksProperty());
  }
}
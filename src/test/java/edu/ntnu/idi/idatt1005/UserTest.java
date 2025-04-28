package edu.ntnu.idi.idatt1005;

import edu.ntnu.idi.idatt1005.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

  @Test
  public void testUserConstructor() {
    // Arrange & Act
    User user = new User(1, "testUser");

    // Assert
    assertEquals(1, user.getId());
    assertEquals("testUser", user.getUsername());
  }

  @Test
  public void testToString() {
    // Arrange
    User user = new User(1, "alice");

    // Act
    String result = user.toString();

    // Assert
    assertEquals("alice", result);
  }
}
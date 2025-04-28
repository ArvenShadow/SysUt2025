package edu.ntnu.idi.idatt1005;

import edu.ntnu.idi.idatt1005.model.Member;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MemberTest {

  @Test
  public void testDefaultConstructor() {
    // Arrange & Act
    Member member = new Member();

    // Assert - default values should be initialized
    assertEquals(0, member.getId());
    assertNull(member.getName());
    assertEquals(0, member.getUserId());
  }

  @Test
  public void testConstructorWithNameAndUserId() {
    // Arrange & Act
    Member member = new Member("John Doe", 42);

    // Assert
    assertEquals(0, member.getId());
    assertEquals("John Doe", member.getName());
    assertEquals(42, member.getUserId());
  }

  @Test
  public void testFullConstructor() {
    // Arrange & Act
    Member member = new Member(5, "Jane Smith", 10);

    // Assert
    assertEquals(5, member.getId());
    assertEquals("Jane Smith", member.getName());
    assertEquals(10, member.getUserId());
  }

  @Test
  public void testSetId() {
    // Arrange
    Member member = new Member("Test Member", 1);

    // Act
    member.setId(100);

    // Assert
    assertEquals(100, member.getId());
  }

  @Test
  public void testSetName() {
    // Arrange
    Member member = new Member(1, "Original Name", 5);

    // Act
    member.setName("Updated Name");

    // Assert
    assertEquals("Updated Name", member.getName());
  }

  @Test
  public void testToString() {
    // Arrange
    Member member = new Member(7, "Test Member", 3);

    // Act
    String result = member.toString();

    // Assert
    assertTrue(result.contains("id=7"));
    assertTrue(result.contains("name='Test Member'"));
    assertTrue(result.contains("userId=3"));
  }
}
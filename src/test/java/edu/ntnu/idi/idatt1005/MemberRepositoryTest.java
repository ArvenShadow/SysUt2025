package edu.ntnu.idi.idatt1005;

import edu.ntnu.idi.idatt1005.db.DatabaseConnector;
import edu.ntnu.idi.idatt1005.model.Member;
import edu.ntnu.idi.idatt1005.model.MemberStat;
import edu.ntnu.idi.idatt1005.repository.MemberRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemberRepositoryTest {

  private static MemberRepository memberRepository;
  private static int testUserId;
  private static int testMemberId;
  private static final String TEST_MEMBER_NAME = "Test Member";

  @BeforeAll
  static void setUp() throws SQLException {
    memberRepository = new MemberRepository();

    // Create a test user for our member tests
    Connection conn = DatabaseConnector.getConnection();

    // First delete any existing test data
    PreparedStatement cleanup = conn.prepareStatement(
      "DELETE FROM users WHERE username = 'memberrepotest'");
    cleanup.executeUpdate();

    // Create test user
    PreparedStatement createUser = conn.prepareStatement(
      "INSERT INTO users (username, password) VALUES ('memberrepotest', 'password')",
      PreparedStatement.RETURN_GENERATED_KEYS);
    createUser.executeUpdate();

    ResultSet keys = createUser.getGeneratedKeys();
    if (keys.next()) {
      testUserId = keys.getInt(1);
    } else {
      throw new AssertionError("Failed to create test user");
    }
  }

  @AfterAll
  static void tearDown() throws SQLException {
    // Clean up test data
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement deleteMembers = conn.prepareStatement(
      "DELETE FROM members WHERE user_id = ?");
    deleteMembers.setInt(1, testUserId);
    deleteMembers.executeUpdate();

    PreparedStatement deleteUser = conn.prepareStatement(
      "DELETE FROM users WHERE user_id = ?");
    deleteUser.setInt(1, testUserId);
    deleteUser.executeUpdate();

    DatabaseConnector.closeConnection();
  }

  @Test
  @Order(1)
  void testAddMember() throws SQLException {
    // Arrange
    Member member = new Member(TEST_MEMBER_NAME, testUserId);

    // Act
    memberRepository.addMember(member);

    // Assert
    assertTrue(member.getId() > 0);
    testMemberId = member.getId();

    // Verify in database
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement stmt = conn.prepareStatement(
      "SELECT * FROM members WHERE id = ?");
    stmt.setInt(1, testMemberId);
    ResultSet rs = stmt.executeQuery();

    assertTrue(rs.next());
    assertEquals(TEST_MEMBER_NAME, rs.getString("name"));
    assertEquals(testUserId, rs.getInt("user_id"));
  }

  @Test
  @Order(2)
  void testFetchMembers() throws SQLException {
    // Act
    List<Member> members = memberRepository.fetchMembers(testUserId);

    // Assert
    assertFalse(members.isEmpty());

    // Find our test member
    Member testMember = members.stream()
      .filter(m -> m.getId() == testMemberId)
      .findFirst()
      .orElse(null);

    assertNotNull(testMember);
    assertEquals(TEST_MEMBER_NAME, testMember.getName());
    assertEquals(testUserId, testMember.getUserId());
  }

  @Test
  @Order(3)
  void testFetchMemberStatistics() throws SQLException {
    // Arrange
    Member member = new Member(testMemberId, TEST_MEMBER_NAME, testUserId);

    // Act
    MemberStat stats = memberRepository.fetchMemberStatistics(member);

    // Assert
    assertNotNull(stats);

    // Initial stats should be 0 (or whatever default is defined)
    assertEquals(0, stats.ongoingTasksProperty().get());
    assertEquals(0, stats.completedTasksProperty().get());
    assertEquals(0, stats.totalTasksProperty().get());
  }

  @Test
  @Order(4)
  void testRemoveMember() throws SQLException {
    // Arrange - test member should exist from previous tests

    // Act
    memberRepository.removeMember(testMemberId);

    // Assert - verify member no longer exists
    Connection conn = DatabaseConnector.getConnection();
    PreparedStatement stmt = conn.prepareStatement(
      "SELECT COUNT(*) FROM members WHERE id = ?");
    stmt.setInt(1, testMemberId);
    ResultSet rs = stmt.executeQuery();

    assertTrue(rs.next());
    assertEquals(0, rs.getInt(1));

    // Also verify the member no longer appears in the fetchMembers result
    List<Member> members = memberRepository.fetchMembers(testUserId);
    boolean memberExists = members.stream()
      .anyMatch(m -> m.getId() == testMemberId);

    assertFalse(memberExists);
  }

  @Test
  @Order(5)
  void testFetchMembersEmpty() throws SQLException {
    // Arrange - we've removed all members in previous test

    // Act
    List<Member> members = memberRepository.fetchMembers(999999); // Nonexistent user ID

    // Assert
    assertTrue(members.isEmpty());
  }
}
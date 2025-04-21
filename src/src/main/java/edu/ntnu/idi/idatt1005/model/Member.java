package edu.ntnu.idi.idatt1005.model;

/**
 * Represents a member in the system.
 * A member is associated with a user and can be used to represent
 * additional entities related to the user, such as household members.
 */
public class Member {
  private int id;
  private String name;
  private int userId;

  /**
   * Default constructor for creating an empty Member object.
   */
  public Member() {
  }

  /**
   * Constructor for creating a new Member object.
   *
   * @param name   the name of the member
   * @param userId the ID of the associated user
   */
  public Member(String name, int userId) {
    this.name = name;
    this.userId = userId;
  }

  /**
   * Constructor for creating a Member object retrieved from the database.
   *
   * @param id     the unique ID of the member
   * @param name   the name of the member
   * @param userId the ID of the associated user
   */
  public Member(int id, String name, int userId) {
    this.id = id;
    this.name = name;
    this.userId = userId;
  }

  /**
  * Returns the unique identifier of this member.
  *
  * @return the member's ID
  */
  public int getId() {
    return id;
  }

  /**
  * Sets the unique identifier for this member.
  *
  * @param id the ID to set
  */
  public void setId(int id) {
    this.id = id;
  }

  /**
  * Returns the name of this member.
  *
  * @return the member's name
  */
  public String getName() {
    return name;
  }

  /**
  * Sets the name for this member.
  *
  * @param name the name to set
  */
  public void setName(String name) {
    this.name = name;
  }

  /**
  * Returns the user ID associated with this member.
  *
  * @return the associated user ID
  */
  public int getUserId() {
    return userId;
  }


  @Override
  public String toString() {
    return "Member{"
      + "id="
      + id
      + ", name='"
      + name
      + '\''
      + ", userId="
      + userId
      + '}';
  }
}
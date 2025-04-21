package edu.ntnu.idi.idatt1005.model;

/**
 * Represents a user in the system.
 * This class contains the basic identity information for a user,
 * which is used for session management and task associations.
 * User objects are immutable as both fields are final.
 */
public class User {

  private final int id;
  private final String username;

  /**
   * Constructs a new User object with the specified ID and username.
   *
   * @param id       the unique identifier of the user
   * @param username the username of the user
   */
  public User(int id, String username) {
    this.id = id;
    this.username = username;
  }

  /**
  * Returns the unique identifier of this user.
  *
  * @return the user's ID
  */
  public int getId() {
    return id;
  }

  /**
  * Returns the username of this user.
  *
  * @return the user's username
  */
  public String getUsername() {
    return username;
  }

  @Override
  public String toString() {
    return username;
  }
}

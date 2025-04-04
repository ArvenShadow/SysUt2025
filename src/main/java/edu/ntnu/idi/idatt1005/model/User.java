package edu.ntnu.idi.idatt1005.model;

/**
 * Represents a user in the system.
 * Stores identity info for session and task association.
 * Author: KrissKN
 */
public class User {

  private final int id;
  private final String username;

  public User(int id, String username) {
    this.id = id;
    this.username = username;
  }

  public int getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  @Override
  public String toString() {
    return username;
  }
}

package edu.ntnu.idi.idatt1005.app;

import edu.ntnu.idi.idatt1005.model.User;

/**
 * Singleton class for managing application-wide state, such as the current user session.
 * Use this to store data that needs to be accessed throughout the application,
 * such as the logged-in user.
 * This replaces the use of static fields like LoginCard.getUserId().
 *
 * @author KrissKN
 */
public class AppState {

  // Singleton instance
  private static final AppState instance = new AppState();

  private User currentUser;

  // Private constructor to prevent external instantiation
  private AppState() {}

  /**
   * Gets the singleton instance of AppState.
   *
   * @return the AppState instance
   */
  public static AppState getInstance() {
    return instance;
  }

  /**
   * Gets the currently logged-in user.
   *
   * @return the current user
   */
  public User getCurrentUser() {
    return currentUser;
  }

  /**
   * Sets the currently logged-in user.
   *
   * @param currentUser the user who has logged in
   */
  public void setCurrentUser(User currentUser) {
    this.currentUser = currentUser;
  }

  /**
   * Clears the current session.
   */
  public void clear() {
    this.currentUser = null;
  }

  /**
   * Checks whether a user is currently logged in.
   *
   * @return true if a user is logged in, false otherwise
   */
  public boolean isLoggedIn() {
    return currentUser != null;
  }
}

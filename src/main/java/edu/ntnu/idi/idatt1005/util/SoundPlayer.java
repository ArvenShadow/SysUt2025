package edu.ntnu.idi.idatt1005.util;

import java.util.Objects;
import javafx.scene.media.AudioClip;


/**
 * Utility class for playing sounds like startup or shutdown notifications.
 */
public class SoundPlayer {

  /**
  * Default constructor for SoundPlayer.
  */
  public SoundPlayer() {
    // Default constructor implementation
  }

  /**
   * Plays a sound file located in the resources' folder.
   *
   * @param resourcePath the path to the sound file (e.g., "/audio/startup.mp3")
   */
  public static void playSound(String resourcePath) {
    try {
      String soundFile =
          Objects.requireNonNull(SoundPlayer.class.getResource(resourcePath)).toExternalForm();
      AudioClip clip = new AudioClip(soundFile);
      clip.setVolume(1.0);
      clip.play();
    } catch (Exception e) {
      System.err.println("Could not play sound: " + resourcePath);
      e.printStackTrace();
    }
  }
}

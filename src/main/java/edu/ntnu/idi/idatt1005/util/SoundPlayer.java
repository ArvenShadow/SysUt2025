package edu.ntnu.idi.idatt1005.util;

import javafx.scene.media.AudioClip;

import java.util.Objects;

/**
 * Utility class for playing sounds like startup or shutdown notifications.
 * Author: KrissKn
 */
public class SoundPlayer {

  /**
   * Plays a sound file located in the resources' folder.
   *
   * @param resourcePath the path to the sound file (e.g., "/audio/startup.mp3")
   */
  public static void playSound(String resourcePath) {
    try {
      String soundFile = Objects.requireNonNull(SoundPlayer.class.getResource(resourcePath)).toExternalForm();
      AudioClip clip = new AudioClip(soundFile);
      clip.setVolume(1.0);
      clip.play();
    } catch (Exception e) {
      System.err.println("Could not play sound: " + resourcePath);
      e.printStackTrace();
    }
  }
}

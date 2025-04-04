package edu.ntnu.idi.idatt1005.util;

import javafx.scene.Scene;

import java.util.Objects;

/**
 * Utility class to apply shared CSS styles to JavaFX scenes.
 * Keeps style loading consistent and reusable.
 * Author: KrissKn
 */
public class StyleUtil {

  /**
   * Applies the main Flowstate style sheets to the given scene.
   *
   * @param scene the scene to apply styles to
   */
  public static void applyFlowstateStyle(Scene scene) {
    scene.getStylesheets().add(Objects.requireNonNull(StyleUtil.class.getResource("/landingPageStyle.css")).toExternalForm());
    scene.getStylesheets().add(Objects.requireNonNull(StyleUtil.class.getResource("/landingExtend.css")).toExternalForm());
  }

  /**
   * Applies login page styling to the scene.
   *
   * @param scene the login scene
   */
  public static void applyLoginStyle(Scene scene) {
    scene.getStylesheets().add(Objects.requireNonNull(StyleUtil.class.getResource("/loginStyle.css")).toExternalForm());
  }
}

package com.google.android.apps.common.testing.accessibility.framework;

/**
 * Constants to be used as keys for {@link AccessibilityCheck} metadata.
 */
public final class AccessibilityCheckMetadata {
  /**
   * Screen capture data. This is typically used by checks that assess properties of the visual
   * appearance of an interface. For example, contrast evaluation samples regions of the display to
   * heuristically evaluate foreground/background contrast ratios.
   * <p>
   * Expected type: Parcelable / Bitmap
   * <p>
   * Expected data: A Bitmap containing a full image of the device's default display at the time
   * check execution occurs. The Bitmap should have dimensions which match the DisplayMetrics
   * obtained from WindowManager's default display. The screen area should not be altered, cropped,
   * or adjusted in any way.
   */
  public static final String METADATA_KEY_SCREEN_CAPTURE_BITMAP =
      "com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckMetadata.METADATA_KEY_SCREEN_CAPTURE_BITMAP";

  private AccessibilityCheckMetadata() {}
}

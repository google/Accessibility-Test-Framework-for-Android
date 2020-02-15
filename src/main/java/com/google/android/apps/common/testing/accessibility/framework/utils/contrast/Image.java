package com.google.android.apps.common.testing.accessibility.framework.utils.contrast;

/** Platform-independent representation of a graphical image for contrast analysis. */
public interface Image {

  /** Returns the image's height */
  int getHeight();

  /** Returns the image's width */
  int getWidth();

  /**
   * Returns an image representing a rectangular region within this image. The result will be a view
   * into the original image, so its contents will change if the original is modified.
   *
   * @param left The leftmost coordinate of the region
   * @param top The toptmost coordinate of the region
   * @param width The width of the region
   * @param height The height of the region
   */
  Image crop(int left, int top, int width, int height);

  /**
   * Returns a copy of the data within the image. Each value is a packed int representing a Color.
   */
  int[] getPixels();
}

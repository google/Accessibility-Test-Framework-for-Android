package com.google.android.apps.common.testing.accessibility.framework.utils.contrast;

import static com.google.common.base.Preconditions.checkArgument;

import android.graphics.Bitmap;

/**
 * Implementation of an {@link Image} using {@link Bitmap}. This depends upon an Android runtime.
 */
public final class BitmapImage implements Image {

  private final Bitmap bitmap;
  private final int left;
  private final int top;
  private final int width;
  private final int height;

  public BitmapImage(Bitmap bitmap) {
    this(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
  }

  private BitmapImage(Bitmap bitmap, int left, int top, int width, int height) {
    this.bitmap = bitmap;
    this.left = left;
    this.top = top;
    this.width = width;
    this.height = height;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public BitmapImage crop(int left, int top, int width, int height) {
    checkArgument(left >= 0, "left must be >= 0");
    checkArgument(top >= 0, "top must be >= 0");
    checkArgument(width > 0, "width must be > 0");
    checkArgument(height > 0, "height must be > 0");
    checkArgument(left + width <= this.width);
    checkArgument(top + height <= this.height);
    return new BitmapImage(bitmap, this.left + left, this.top + top, width, height);
  }

  @Override
  public int[] getPixels() {
    int[] pixels = new int[width * height];
    bitmap.getPixels(
        pixels, /* offset= */ 0, /* stride= */ width, /* x= */ left, /* y= */ top, width, height);
    return pixels;
  }

  /** Creates a Bitmap from this BitmapImage. */
  public Bitmap getBitmap() {
    return Bitmap.createBitmap(bitmap, /* x= */ left, /* y= */ top, width, height);
  }

  @Override
  public String toString() {
    return String.format(
        "{BitmapImage left=%d top=%d width=%d height=%d}", left, top, width, height);
  }
}

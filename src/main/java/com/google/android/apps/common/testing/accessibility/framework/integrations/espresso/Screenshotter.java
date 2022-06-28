package com.google.android.apps.common.testing.accessibility.framework.integrations.espresso;

import static com.google.common.base.Preconditions.checkArgument;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import com.google.android.libraries.accessibility.utils.log.LogUtils;

/**
 * Creates a pseudo screenshot.
 *
 * <p>The implementation intends to satisfy two requirements:
 *
 * <ol>
 *   <li>It is suitable for use within an Espresso {@link android.support.test.espresso.ViewAction}
 *       or {@link android.support.test.espresso.ViewAssertion}, which execute within the UI thread.
 *       So it must be a lightweight operation, unlike {@link
 *       android.app.UiAutomation#takeScreenshot()}.
 *   <li>The coordinates of the visible portion of the View - and its children - match the
 *       coordinates of the pixels within the Bitmap.
 * </ol>
 */
// Adapted from java/com/google/testing/screendiffing/android/AndroidImageDiffer.java
class Screenshotter {
  private static final String TAG = "Screenshotter";

  /**
   * Returns a Bitmap with a rendering of the visible portion of the view and all of its children.
   * In some cases, the Bitmap may be smaller than the size of the display. This assumes that the
   * content of the display below and to the right of the View are of no interest.
   */
  public Bitmap getScreenshot(View view) {
    checkArgument(view.getWidth() > 0, "View width must be >0");
    checkArgument(view.getHeight() > 0, "View height must be >0");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      return getScreenShotPPlus(view);
    } else {
      return getScreenshotPreP(view);
    }
  }

  private static Bitmap getScreenShotPPlus(View view) {
    LogUtils.i(TAG, ">= P");
    Picture picture = new Picture();
    int[] windowOffset = getWindowOffset(view);
    Canvas canvas =
        picture.beginRecording(
            windowOffset[0] + view.getWidth(), windowOffset[1] + view.getHeight());
    view.computeScroll();
    canvas.translate(windowOffset[0] - view.getScrollX(), windowOffset[1] - view.getScrollY());
    view.draw(canvas);

    // End recording before creating the Bitmap so that the Picture is fully initialized prior to
    // creating the Bitmap copy below. Matches the previous call to beginRecording. See b/80539264.
    picture.endRecording();
    Bitmap bitmap =
        Bitmap.createBitmap(
            picture, picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
    return bitmap;
  }

  private static Bitmap getScreenshotPreP(View view) {
    // The drawing cache is a cheap, easy and compatible way to generate our screenshot.
    // However, because the cache is capped at a maximum size, this method may not work
    // for large views. So, we first try the drawing cache, and then if that fails
    // we fall back to building the screenshot bitmap manually.
    view.buildDrawingCache();
    Bitmap bitmap = view.getDrawingCache();
    if (bitmap == null) {
      LogUtils.i(TAG, "PreP without drawing cache");
      bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
      bitmap.setDensity(view.getResources().getDisplayMetrics().densityDpi);
      view.computeScroll();
      Canvas canvas = new Canvas(bitmap);
      canvas.translate(-view.getScrollX(), -view.getScrollY());
      view.draw(canvas);
    } else {
      LogUtils.i(TAG, "PreP from drawing cache");
      bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
      view.destroyDrawingCache();
    }

    return expandBitmapIfWindowOffset(bitmap, view);
  }

  // If the window is offset, expand the bitmap
  private static Bitmap expandBitmapIfWindowOffset(Bitmap bitmap, View view) {
    int[] windowOffset = getWindowOffset(view);
    if ((windowOffset[0] != 0) || (windowOffset[1] != 0)) {
      Rect destRect = new Rect();
      if (view.getGlobalVisibleRect(destRect)) {
        LogUtils.i(TAG, "Resizing " + destRect);
        Bitmap biggerBitmap =
            Bitmap.createBitmap(
                view.getWidth() + windowOffset[0],
                view.getHeight() + windowOffset[1],
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(biggerBitmap);
        destRect.offset(windowOffset[0], windowOffset[1]);
        canvas.drawBitmap(bitmap, /* src= */ null, destRect, /* paint= */ null);
        return biggerBitmap;
      }
    }
    return bitmap;
  }

  private static int[] getWindowOffset(View view) {
    int[] locationOnScreen = new int[2];
    int[] locationInWindow = new int[2];
    view.getLocationOnScreen(locationOnScreen);
    view.getLocationInWindow(locationInWindow);

    // Usually these offsets will be zero, except when the view is in a dialog window.
    int xOffset = locationOnScreen[0] - locationInWindow[0];
    int yOffset = locationOnScreen[1] - locationInWindow[1];
    return new int[] {xOffset, yOffset};
  }
}

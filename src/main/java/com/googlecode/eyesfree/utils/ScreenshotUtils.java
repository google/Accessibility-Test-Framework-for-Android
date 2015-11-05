/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.eyesfree.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.googlecode.eyesfree.compat.view.SurfaceControlCompatUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A utility class for taking screenshots.
 *
 * @author caseyburkhardt@google.com (Casey Burkhardt)
 */
public class ScreenshotUtils {

    private ScreenshotUtils() {
        // This class is non-instantiable.
    }

    public static boolean hasScreenshotPermission(Context context) {
        return (context.checkCallingOrSelfPermission(Manifest.permission.READ_FRAME_BUFFER) ==
                PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Returns a screenshot with the contents of the current display that
     * matches the current display rotation.
     *
     * @param context The current context.
     * @return A bitmap of the screenshot.
     */
    public static Bitmap createScreenshot(Context context) {
        if (!hasScreenshotPermission(context)) {
            LogUtils.log(ScreenshotUtils.class, Log.ERROR, "Screenshot permission denied.");
            return null;
        }

        final WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        final Bitmap bitmap = SurfaceControlCompatUtils.screenshot(0, 0);

        // Bail if we couldn't take the screenshot.
        if (bitmap == null) {
            LogUtils.log(ScreenshotUtils.class, Log.ERROR, "Failed to take screenshot.");
            return null;
        }

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final int rotation = windowManager.getDefaultDisplay().getRotation();

        final int outWidth;
        final int outHeight;
        final float rotationDegrees;

        switch (rotation) {
            case Surface.ROTATION_90:
                outWidth = height;
                outHeight = width;
                rotationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                outWidth = width;
                outHeight = height;
                rotationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                outWidth = height;
                outHeight = width;
                rotationDegrees = 270;
                break;
            default:
                return bitmap;
        }

        // Rotate the screenshot to match the screen orientation.
        final Bitmap rotatedBitmap =
                Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.RGB_565);
        final Canvas c = new Canvas(rotatedBitmap);

        c.translate(outWidth / 2.0f, outHeight / 2.0f);
        c.rotate(-rotationDegrees);
        c.translate(-width / 2.0f, -height / 2.0f);
        c.drawBitmap(bitmap, 0, 0, null);

        bitmap.recycle();

        return rotatedBitmap;
    }

    /**
     * Creates a new {@link Bitmap} object with a rectangular region of pixels
     * from the source bitmap.
     * <p>
     * The source bitmap is unaffected by this operation.
     *
     * @param sourceBitmap The source bitmap to crop.
     * @param bounds The rectangular bounds to keep when cropping.
     * @return A new bitmap of the cropped area, or {@code null} if the source
     *         was {@code null} or the crop parameters were out of bounds.
     */
    public static Bitmap cropBitmap(Bitmap sourceBitmap, Rect bounds) {
        if (sourceBitmap == null) {
            return null;
        }

        try {
            return Bitmap.createBitmap(
                    sourceBitmap, bounds.left, bounds.top, bounds.width(), bounds.height());
        } catch (IllegalArgumentException ex) {
            // Can throw exception if cropping arguments are out of bounds.
            LogUtils.log(ScreenshotUtils.class, Log.ERROR, Log.getStackTraceString(ex));
            return null;
        }
    }

    /**
     * Writes a {@link Bitmap} object to a file in the current context's files
     * directory.
     *
     * @param context The current context.
     * @param bitmap The bitmap object to output.
     * @param dir The output directory name within the files directory.
     * @param filename The name of the file to output.
     * @return A file where the Bitmap was stored, or {@code null} if the write
     *         operation failed.
     */
    public static File writeBitmap(Context context, Bitmap bitmap, String dir, String filename) {
        if (bitmap == null) {
            return null;
        }

        final File dirFile = new File(context.getFilesDir(), dir);
        if (!dirFile.exists() && !dirFile.mkdirs()) {
            LogUtils.log(ScreenshotUtils.class, Log.WARN,
                    "Directory %s does not exist and could not be created.",
                    dirFile.getAbsolutePath());
            return null;
        }

        final File outFile = new File(dirFile, filename);
        if (outFile.exists()) {
            LogUtils.log(ScreenshotUtils.class, Log.WARN,
                    "Tried to write a bitmap to a file that already exists.");
            return null;
        }

        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(outFile);
            final boolean compressSuccess = bitmap.compress(
                    CompressFormat.PNG, 0 /* quality, ignored for PNG */, outStream);

            if (compressSuccess) {
                LogUtils.log(ScreenshotUtils.class, Log.VERBOSE, "Wrote bitmap to %s.",
                        outFile.getAbsolutePath());
                return outFile;
            } else {
                LogUtils.log(ScreenshotUtils.class, Log.WARN,
                        "Bitmap failed to compress to file %s.", outFile.getAbsolutePath());
                return null;
            }
        } catch (IOException e) {
            LogUtils.log(ScreenshotUtils.class, Log.WARN,
                    "Could not output bitmap file to %s.", outFile.getAbsolutePath());
            return null;
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    LogUtils.log(ScreenshotUtils.class, Log.WARN, "Unable to close resource.");
                }
            }
        }
    }
}
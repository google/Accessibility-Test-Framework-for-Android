/*
 * Copyright (C) 2015 Google Inc.
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

package com.googlecode.eyesfree.compat.view;

import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.eyesfree.compat.CompatUtils;

import java.lang.reflect.Method;

/**
 * Provides access to {@code android.view.SurfaceControl} methods.
 *
 * @author alanv@google.com (Alan Viverette)
 */
public class SurfaceControlCompatUtils {
    private static final String TAG = SurfaceControlCompatUtils.class.getSimpleName();

    private static final Class<?> CLASS_Surface_Control = CompatUtils.getClass(
            "android.view.SurfaceControl");
    private static final Method METHOD_screenshot_II = CompatUtils.getMethod(CLASS_Surface_Control,
            "screenshot", int.class, int.class);

    private SurfaceControlCompatUtils() {
        // This class is non-instantiable.
    }

    /**
     * Copy the current screen contents into a bitmap and return it. Use width =
     * 0 and height = 0 to obtain an unscaled screenshot.
     *
     * @param width The desired width of the returned bitmap; the raw screen
     *            will be scaled down to this size.
     * @param height The desired height of the returned bitmap; the raw screen
     *            will be scaled down to this size.
     */
    public static Bitmap screenshot(int width, int height) {
        if (METHOD_screenshot_II == null) {
            Log.e(TAG, "screenshot method was not found.");
            return null;
        }

        return (Bitmap) CompatUtils.invoke(null, null, METHOD_screenshot_II, width, height);
    }
}

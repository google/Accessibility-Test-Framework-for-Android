/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.eyesfree.utils;

import android.graphics.Color;

/**
 * Utilities for dealing with colors and evaluation of their relative contrast.
 *
 * @author caseyburkhardt@google.com (Casey Burkhardt)
 */
public class ContrastUtils {

    /**
     * The minimum text size considered large for contrast checking purposes, as taken from the WCAG
     * standards at http://www.w3.org/TR/UNDERSTANDING-WCAG20/visual-audio-contrast-contrast.html
     */
    public static final int WCAG_LARGE_TEXT_MIN_SIZE = 18;

    /**
     * The minimum text size for bold text to be considered large for contrast checking purposes,
     * as taken from the WCAG standards at
     * http://www.w3.org/TR/UNDERSTANDING-WCAG20/visual-audio-contrast-contrast.html
     */
    public static final int WCAG_LARGE_BOLD_TEXT_MIN_SIZE = 14;

    /** The color value used to censor secure windows from screen capture */
    public static final int COLOR_SECURE_WINDOW_CENSOR = Color.BLACK;

    public static final double CONTRAST_RATIO_WCAG_NORMAL_TEXT = 4.5;

    public static final double CONTRAST_RATIO_WCAG_LARGE_TEXT = 3.0;

    private ContrastUtils() {
        // Not instantiable
    }

    /**
     * Calculates the contrast ratio of two color ints.
     * <p>
     * Derived from formula at http://gmazzocato.altervista.org/colorwheel/algo.php
     */
    public static double calculateContrastRatio(int color1, int color2) {
      return calculateContrastRatio(calculateLuminance(color1), calculateLuminance(color2));
    }

    /**
     * Calculates the luminance value of a given {@code int} representation of {@link Color}.
     * <p>
     * Derived from formula at http://gmazzocato.altervista.org/colorwheel/algo.php
     *
     * @param color The {@link Color} to evaluate
     * @return the luminance value of the given color
     */
    public static double calculateLuminance(int color) {
        double r = linearColor(Color.red(color));
        double g = linearColor(Color.green(color));
        double b = linearColor(Color.blue(color));
        return 0.2126d * r + 0.7152d * g + 0.0722d * b;
    }

    private static double linearColor(int component) {
        double sRGB = component / 255.0d;
        if (sRGB <= 0.03928d) {
            return sRGB / 12.92d;
        } else {
            return Math.pow(((sRGB + 0.055d) / 1.055d), 2.4d);
        }
    }

    /**
     * Calculates the contrast ratio of two order-independent luminance values.
     * <p>
     * Derived from formula at http://gmazzocato.altervista.org/colorwheel/algo.php
     *
     * @param lum1 The first luminance value
     * @param lum2 The second luminance value
     * @return The contrast ratio of the luminance values
     * @throws IllegalArgumentException if luminance values are < 0
     */
    public static double calculateContrastRatio(double lum1, double lum2) {
        if ((lum1 < 0.0d) || (lum2 < 0.0d)) {
            throw new IllegalArgumentException("Luminance values may not be negative.");
        }

        return (Math.max(lum1, lum2) + 0.05d) / (Math.min(lum1, lum2) + 0.05d);
    }

    /**
     * Converts an {@code int} representation of a {@link Color} to a hex string.
     *
     * @param color The {@link Color} value to convert
     * @return The hex string representation of the color
     */
    public static CharSequence colorToHexString(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
}

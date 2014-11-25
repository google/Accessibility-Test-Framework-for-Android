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

    public static final double CONTRAST_RATIO_WCAG_NORMAL_TEXT = 4.5;

    public static final double CONTRAST_RATIO_WCAG_LARGE_TEXT = 3.0;

    private ContrastUtils() {
        // Not instantiable
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
        final double[] sRGB = new double[3];
        sRGB[0] = Color.red(color) / 255.0d;
        sRGB[1] = Color.green(color) / 255.0d;
        sRGB[2] = Color.blue(color) / 255.0d;

        final double[] lumRGB = new double[3];
        for (int i = 0; i < sRGB.length; ++i) {
            lumRGB[i] = (sRGB[i] <= 0.03928d) ? sRGB[i] / 12.92d
                    : Math.pow(((sRGB[i] + 0.055d) / 1.055d), 2.4d);
        }

        return 0.2126d * lumRGB[0] + 0.7152d * lumRGB[1] + 0.0722d * lumRGB[2];
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
     * Converts a collection of {@code int} representations of colors to a
     * string representation of those colors in hex format.
     *
     * @param colors Collection of {@link Color} values to convert
     * @return The hex string representation of the colors
     */
    public static CharSequence colorsToHexString(Iterable<Integer> colors) {
        StringBuilder colorStr = new StringBuilder(7);
        for (int color : colors) {
            if (colorStr.length() != 0) {
                colorStr.append(", ");
            }

            colorStr.append(ContrastUtils.colorToHexString(color));
        }

        return colorStr;
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

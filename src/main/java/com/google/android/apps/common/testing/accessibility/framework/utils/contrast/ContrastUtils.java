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

package com.google.android.apps.common.testing.accessibility.framework.utils.contrast;

import static java.lang.Math.max;
import static java.lang.Math.min;

import com.google.common.collect.Range;

/** Utilities for dealing with colors and evaluation of their relative contrast. */
public final class ContrastUtils {

  /**
   * The minimum text size considered large for contrast checking purposes, as taken from the WCAG
   * standards at http://www.w3.org/TR/UNDERSTANDING-WCAG20/visual-audio-contrast-contrast.html
   */
  public static final int WCAG_LARGE_TEXT_MIN_SIZE = 18;

  /**
   * The minimum text size for bold text to be considered large for contrast checking purposes, as
   * taken from the WCAG standards at
   * http://www.w3.org/TR/UNDERSTANDING-WCAG20/visual-audio-contrast-contrast.html
   */
  public static final int WCAG_LARGE_BOLD_TEXT_MIN_SIZE = 14;

  /** The color value used to censor secure windows from screen capture */
  public static final int COLOR_SECURE_WINDOW_CENSOR = Color.BLACK;

  public static final double CONTRAST_RATIO_WCAG_NORMAL_TEXT = 4.5;

  public static final double CONTRAST_RATIO_WCAG_LARGE_TEXT = 3.0;

  private static final int COLOR_MASK = 0xFF;

  private ContrastUtils() {
    // Not instantiable
  }

  /**
   * Calculates the luminance value of a given {@code int} representation of {@link Color}.
   *
   * <p>Derived from formula at http://gmazzocato.altervista.org/colorwheel/algo.php
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
   * Calculates the Delta E of CIE-94 perceived color difference of two color ints.
   *
   * <p>Derived from formula at https://www.easyrgb.com/en/math.php and
   * https://en.wikipedia.org/wiki/Color_difference
   *
   * @return the perceived color diffrence Delta E.
   */
  public static double colorDifference(int color1, int color2) {
    double[] lab1 = rgb2lab(color1);
    double[] lab2 = rgb2lab(color2);

    double deltaL = lab1[0] - lab2[0];
    double deltaA = lab1[1] - lab2[1];
    double deltaB = lab1[2] - lab2[2];
    double c1 = Math.hypot(lab1[1], lab1[2]);
    double c2 = Math.hypot(lab2[1], lab2[2]);
    double deltaC = c1 - c2;
    double deltaH = deltaA * deltaA + deltaB * deltaB - deltaC * deltaC;
    deltaH = deltaH < 0 ? 0 : Math.sqrt(deltaH);
    double sc = 1.0 + 0.045d * c1;
    double sh = 1.0 + 0.015d * c1;
    double deltaLKlsl = deltaL / 1.0d;
    double deltaCkcsc = deltaC / sc;
    double deltaHkhsh = deltaH / sh;
    return Math.sqrt(deltaLKlsl * deltaLKlsl + deltaCkcsc * deltaCkcsc + deltaHkhsh * deltaHkhsh);
  }

  /**
   * Convert linear RGB to CIE-L*ab color space.
   *
   * <p>Derived from formula at https://www.easyrgb.com/en/math.php
   *
   * @param color The int representation of an sRGB color.
   * @return the three values for L*, a* and b*
   */
  public static double[] rgb2lab(int color) {
    double r = linearColor(Color.red(color));
    double g = linearColor(Color.green(color));
    double b = linearColor(Color.blue(color));
    double x = (r * 0.4124d + g * 0.3576d + b * 0.1805d) / 0.95047d;
    double y = (r * 0.2126d + g * 0.7152d + b * 0.0722d) / 1.00000d;
    double z = (r * 0.0193d + g * 0.1192d + b * 0.9505d) / 1.08883d;
    x = (x > 0.008856d) ? Math.cbrt(x) : (7.787d * x) + 16.0d / 116;
    y = (y > 0.008856d) ? Math.cbrt(y) : (7.787d * y) + 16.0d / 116;
    z = (z > 0.008856d) ? Math.cbrt(z) : (7.787d * z) + 16.0d / 116;
    return new double[] {(116 * y) - 16, 500 * (x - y), 200 * (y - z)};
  }

  /**
   * Calculates the contrast ratio of two color ints.
   */
  public static double calculateContrastRatio(int color1, int color2) {
    return calculateContrastRatio(calculateLuminance(color1), calculateLuminance(color2));
  }

  /**
   * Calculates the contrast ratio of two order-independent luminance values.
   *
   * <p>Derived from formula at http://gmazzocato.altervista.org/colorwheel/algo.php
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

    return (max(lum1, lum2) + 0.05d) / (min(lum1, lum2) + 0.05d);
  }

  /**
   * Calculates contrast ratio between foreground color and background color when the background is
   * overlaid on an opaque backdrop.
   *
   * @param foregroundColor The foreground color (can be opaque or non-opaque)
   * @param backgroundColor The background color (can be opaque or non-opaque)
   * @param backdrop The opaque backdrop
   * @return The contrast ratio of foreground with background overlaid on backdrop
   */
  private static double calculateContrastOnBackdrop(
      int foregroundColor, int backgroundColor, int backdrop) {
    int backgroundOnBackdrop = compositeColors(backgroundColor, backdrop);
    int compositeforegroundColorOnBackgroundOnBackdrop =
        compositeColors(foregroundColor, backgroundOnBackdrop);
    return calculateContrastRatio(
        compositeforegroundColorOnBackgroundOnBackdrop, backgroundOnBackdrop);
  }

  /**
   * Calculates maximum and minimum value of contrast ratio when background color is not opaque.
   * Approach: https://lists.w3.org/Archives/Public/w3c-wai-ig/2012OctDec/0011.html#replies
   *
   * @param foregroundColor The foreground color (can be opaque or non-opaque)
   * @param backgroundColor The background color (can be opaque or non-opaque)
   * @return The range [minimum, maximum] of contrast ratio
   */
  public static Range<Double> calculateContrastRatioRange(
      int foregroundColor, int backgroundColor) {
    double contrastOnBlackBackdrop =
        calculateContrastOnBackdrop(foregroundColor, backgroundColor, Color.BLACK);
    double contrastOnWhiteBackdrop =
        calculateContrastOnBackdrop(foregroundColor, backgroundColor, Color.WHITE);

    double backgroundOnBlackLuminance =
        calculateLuminance(compositeColors(backgroundColor, Color.BLACK));
    double backgroundOnWhiteLuminance =
        calculateLuminance(compositeColors(backgroundColor, Color.WHITE));
    double foregroundColorLuminance = calculateLuminance(foregroundColor);

    double minimumContrastRatio = 1.0;
    if (foregroundColorLuminance < backgroundOnBlackLuminance) {
      minimumContrastRatio = contrastOnBlackBackdrop;
    } else if (backgroundOnWhiteLuminance < foregroundColorLuminance) {
      minimumContrastRatio = contrastOnWhiteBackdrop;
    }
    return Range.closed(
        minimumContrastRatio, max(contrastOnBlackBackdrop, contrastOnWhiteBackdrop));
  }

  /**
   * Converts an {@code int} representation of a {@link Color} to a hex string in the form of <code>
   * #<i>rrggbb</i></code> if opaque or <code>#<i>aarrggbb</i></code> if not.
   *
   * @param color The {@link Color} value to convert
   * @return The hex string representation of the color
   */
  public static String colorToHexString(int color) {
    if (Color.alpha(color) == 255) {
      return String.format("#%06X", (0xFFFFFF & color));
    }
    return String.format("#%08X", color);
  }

  /**
   * Overlays a translucent color over an opaque color.
   *
   * @param color The translucent {@link Color}
   * @param colorToOverlayOn The opaque {@link Color}
   * @return The alpha blended {@link Color}
   */
  public static int compositeColors(int color, int colorToOverlayOn) {
    int alpha = Color.alpha(color);
    int r = compositeComponents(Color.red(color), Color.red(colorToOverlayOn), alpha);
    int g = compositeComponents(Color.green(color), Color.green(colorToOverlayOn), alpha);
    int b = compositeComponents(Color.blue(color), Color.blue(colorToOverlayOn), alpha);
    return Color.argb(COLOR_MASK, r, g, b);
  }

  /**
   * Overlays a translucent color component over an opaque color component.
   *
   * @param component The translucent color component
   * @param componentToOverlayOn The opaque color component
   * @return The alpha blended color component
   */
  private static int compositeComponents(int component, int componentToOverlayOn, int alpha) {
    return (component * alpha + componentToOverlayOn * (COLOR_MASK - alpha)) / COLOR_MASK;
  }
}

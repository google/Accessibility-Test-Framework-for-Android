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

package com.google.android.apps.common.testing.accessibility.framework.utils.contrast;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Represents a section of a screenshot bitmap and its associated color and contrast data. */
public class ContrastSwatch {

  public static final int MAX_FOREGROUND_COLOR = 5;

  private static final double COLOR_DIFFERENCE_LIMIT = 2.0;

  private static final double COLOR_SIGNIFICANCE_PERCENTAGE = 0.03;

  private static final double COLOR_CUTOFF_PERCENTAGE = 0.01;

  private final SeparatedColors separatedColors;

  /**
   * Constructs a ContrastSwatch, also extracting certain properties from the bitmap related to
   * contrast and luminance.
   *
   * <p>NOTE: Invoking this constructor performs image processing tasks, which are relatively
   * heavyweight. Callers should create these objects off the UI thread.
   *
   * @param image {@link Image} to be evaluated
   * @param multipleForegroundColors if multiple foreground colors should be identified
   */
  public ContrastSwatch(Image image, boolean multipleForegroundColors) {
    separatedColors = processSwatch(image, multipleForegroundColors);
  }

  /** Compute the background and foreground colors and luminance for the image. */
  private static SeparatedColors processSwatch(Image image, boolean multipleForegroundColors) {
    int[] pixels = image.getPixels();
    ColorHistogram colorHistogram = new ColorHistogram(pixels);
    int imageSize = pixels.length;
    return separateColors(colorHistogram, imageSize, multipleForegroundColors);
  }

  /**
   * Separates the colors in the color histogram into foreground and background colors, and computes
   * luminance values.
   *
   * @param imageSize total number of pixels in the image
   */
  private static SeparatedColors separateColors(
      ColorHistogram colorHistogram, int imageSize, boolean multipleForegroundColors) {
    if (colorHistogram.getColors().isEmpty()) {
      // An empty histogram indicates we've encountered a 0px area image.
      return new SeparatedColors(Color.BLACK);
    }

    if (colorHistogram.getColors().size() == 1) {
      // Deal with views that only contain a single color
      int singleColor = colorHistogram.getColors().iterator().next();
      return new SeparatedColors(singleColor);
    }

    double averageLuminance = colorHistogram.calculateAverageLuminance();

    if (multipleForegroundColors) {
      return separateColorsUsingMultipleForegroundMethod(
          colorHistogram, averageLuminance, imageSize);
    }

    return separateColorsUsingSingleForegroundMethod(colorHistogram, averageLuminance);
  }

  private static SeparatedColors separateColorsUsingMultipleForegroundMethod(
      ColorHistogram colorHistogram, double averageLuminance, int imageSize) {

    // Reduce the color histogram.
    ColorHistogram dominantColorHistogram =
        reduceColors(colorHistogram, imageSize, COLOR_CUTOFF_PERCENTAGE);

    // Treat as single color image if cannot find a second dominant color.

    if (dominantColorHistogram.getColors().size() < 2) {
      int singleColor = dominantColorHistogram.getColors().iterator().next();
      return new SeparatedColors(singleColor);
    }
    // Sort colors with a max heap.
    final PriorityQueue<Map.Entry<Integer, Integer>> frequencyMaxHeap =
        new PriorityQueue<>(
            dominantColorHistogram.getColors().size(), (a, b) -> (b.getValue() - a.getValue()));
    for (Map.Entry<Integer, Integer> entry : dominantColorHistogram.entrySet()) {
      frequencyMaxHeap.offer(entry);
    }
    // The color with the most numbers of pixels is considered the background.
    int backgroundColor = checkNotNull(frequencyMaxHeap.poll()).getKey();

    // The other dominant colors are considered as the foregrounds.
    List<Integer> foregroundColors =
        extractDominantForegroundColors(
            backgroundColor, frequencyMaxHeap, averageLuminance, imageSize);

    // Treat as single color image if cannot find a second dominant color that has opposite
    // luminance or covers enough area of the image.
    if (foregroundColors.isEmpty()) {
      return new SeparatedColors(backgroundColor);
    }
    return new SeparatedColors(backgroundColor, foregroundColors);
  }

  private static SeparatedColors separateColorsUsingSingleForegroundMethod(
      ColorHistogram colorHistogram, double averageLuminance) {

    // Select the most common color with above average luminance, and the most common color with
    // below average luminance as our background and foreground colors.
    int lowLuminanceColor = -1;
    int highLuminanceColor = -1;
    int maxLowLuminanceFrequency = 0;
    int maxHighLuminanceFrequency = 0;
    for (Map.Entry<Integer, Integer> entry : colorHistogram.entrySet()) {
      final int color = entry.getKey();
      final double luminanceValue = ContrastUtils.calculateLuminance(color);
      final int frequency = entry.getValue();
      if ((luminanceValue < averageLuminance) && (frequency > maxLowLuminanceFrequency)) {
        maxLowLuminanceFrequency = frequency;
        lowLuminanceColor = color;
      } else if ((luminanceValue >= averageLuminance) && (frequency > maxHighLuminanceFrequency)) {
        maxHighLuminanceFrequency = frequency;
        highLuminanceColor = color;
      }
    }
    // Background luminance is that which occurs more frequently
    int backgroundColor;
    int foregroundColor;
    if (maxHighLuminanceFrequency > maxLowLuminanceFrequency) {
      backgroundColor = highLuminanceColor;
      foregroundColor = lowLuminanceColor;
    } else {
      backgroundColor = lowLuminanceColor;
      foregroundColor = highLuminanceColor;
    }
    return new SeparatedColors(backgroundColor, foregroundColor);
  }

  /**
   * Reduces and combines colors based on a cutoff percentage and perceptual color difference Delta
   * E.
   *
   * @param colorHistogram the color histogram to be processed.
   * @param imageSize total number of pixels in the image
   * @param cutoff the percentage value below which all the colors are ignored.
   * @return a new histogram that contains the dominant colors from the given histogram.
   */
  private static ColorHistogram reduceColors(
      ColorHistogram colorHistogram, int imageSize, double cutoff) {
    Map<Integer, Integer> dominantColorHistogram = new HashMap<>();
    ArrayList<Map.Entry<Integer, Integer>> dominantColorsList = new ArrayList<>();

    // Remove noises and maintain iteration order
    for (Map.Entry<Integer, Integer> entry : colorHistogram.entrySet()) {
      if (entry.getValue() >= imageSize * cutoff) {
        dominantColorsList.add(entry);
      }
    }
    Collections.sort(dominantColorsList, (a, b) -> (b.getValue() - a.getValue()));

    // Combine similar colors
    for (Map.Entry<Integer, Integer> entry : dominantColorsList) {
      int color = entry.getKey();
      int colorCount = entry.getValue();
      for (int dominantColor : dominantColorHistogram.keySet()) {
        if (ContrastUtils.colorDifference(dominantColor, entry.getKey()) < COLOR_DIFFERENCE_LIMIT) {
          color = dominantColor;
          colorCount += dominantColorHistogram.get(dominantColor);
          break;
        }
      }
      dominantColorHistogram.put(color, colorCount);
    }
    return new ColorHistogram(dominantColorHistogram);
  }

  /**
   * Extract dominant foreground colors. Colors that have opposite luminance values are prioritized
   * in the list.
   *
   * @param frequencyMaxHeap a max heap of all the foreground colors.
   * @param averageLuminance the average Luminance of the image.
   * @param imageSize total number of pixels in the image
   */
  private static List<Integer> extractDominantForegroundColors(
      int backgroundColor,
      PriorityQueue<Map.Entry<Integer, Integer>> frequencyMaxHeap,
      double averageLuminance,
      int imageSize) {
    double backgroundLuminance = ContrastUtils.calculateLuminance(backgroundColor);
    boolean backgroundLuminanceBelowAverage = (backgroundLuminance < averageLuminance);
    List<Integer> foregroundColors = new ArrayList<>();
    int priorityIndex = 0;
    while (!frequencyMaxHeap.isEmpty() && (foregroundColors.size() < MAX_FOREGROUND_COLOR)) {
      Map.Entry<Integer, Integer> entry = frequencyMaxHeap.poll();
      int newColor = checkNotNull(entry).getKey();
      double newLuminance = ContrastUtils.calculateLuminance(newColor);
      boolean newLuminanceBelowAverage = newLuminance <= averageLuminance;
      boolean oppositeLuminance = backgroundLuminanceBelowAverage != newLuminanceBelowAverage;

      if (oppositeLuminance) {
        foregroundColors.add(priorityIndex++, newColor);
      } else if (entry.getValue() > imageSize * COLOR_SIGNIFICANCE_PERCENTAGE) {
        foregroundColors.add(newColor);
      }
    }
    return foregroundColors;
  }

  public int getBackgroundColor() {
    return separatedColors.getBackgroundColor();
  }

  /**
   * Returns the foreground colors in the image. The list contains at least one and up to {@link
   * MAX_FOREGROUND_COLOR} elements.
   */
  public ImmutableList<Integer> getForegroundColors() {
    return separatedColors.getForegroundColors();
  }

  public double getBackgroundLuminance() {
    return ContrastUtils.calculateLuminance(getBackgroundColor());
  }

  /**
   * Returns luminances of the foreground colors in the image. Each element in the list corresponds
   * to the luminance of a color in {@link #getForegroundColors()} with the same index.
   */
  public ImmutableList<Double> getForegroundLuminances() {
    ImmutableList.Builder<Double> foregroundLuminances = ImmutableList.builder();
    for (Integer color : getForegroundColors()) {
      foregroundLuminances.add(ContrastUtils.calculateLuminance(checkNotNull(color)));
    }
    return foregroundLuminances.build();
  }

  /**
   * Returns a {@link List<Double>} representing the contrast ratios of the image. Each element in
   * the list corresponds to the contrast ratio between {@code backgroundColor} and the color in
   * {@code foregroundColors} with the same index.
   */
  public ImmutableList<Double> getContrastRatios() {
    ImmutableList.Builder<Double> contrastRatios = ImmutableList.builder();

    // Two-decimal digits of precision for the contrast ratio
    double backgroundLuminance = getBackgroundLuminance();
    List<Double> foregroundLuminances = getForegroundLuminances();
    for (Double element : foregroundLuminances) {
      contrastRatios.add(
          Math.round(ContrastUtils.calculateContrastRatio(backgroundLuminance, element) * 100.0d)
              / 100.0d);
    }

    return contrastRatios.build();
  }

  @Override
  public String toString() {
    return "{contrast:1:"
        + getContrastRatios().get(0)
        + ", background:"
        + ContrastUtils.colorToHexString(separatedColors.getBackgroundColor())
        + ", foreground:"
        + ContrastUtils.colorToHexString(separatedColors.getForegroundColors().get(0))
        + "}";
  }

  private static class SeparatedColors {

    private final int backgroundColor;
    private final ImmutableList<Integer> foregroundColors;

    /** Constructs an instance with the foreground and background having the same color. */
    SeparatedColors(int singleColor) {
      this(singleColor, singleColor);
    }

    /** Constructs an instance with a background color and a single foreground color. */
    SeparatedColors(int backgroundColor, int foregroundColor) {
      this(backgroundColor, ImmutableList.of(foregroundColor));
    }

    /** Constructs an instance with a background color and multiple foreground colors. */
    SeparatedColors(int backgroundColor, List<Integer> foregroundColors) {
      this.backgroundColor = backgroundColor;
      this.foregroundColors = ImmutableList.copyOf(foregroundColors);
    }

    int getBackgroundColor() {
      return backgroundColor;
    }

    ImmutableList<Integer> getForegroundColors() {
      return foregroundColors;
    }
  }

  /**
   * The set of colors from an image, and the count (number of pixels) associated with each color.
   */
  private static class ColorHistogram {

    private final ImmutableMap<Integer, Integer> colorHistogram;

    ColorHistogram(int[] pixels) {
      colorHistogram = processLuminanceData(pixels);
    }

    private ColorHistogram(Map<Integer, Integer> map) {
      colorHistogram = ImmutableMap.copyOf(map);
    }

    ImmutableSet<Integer> getColors() {
      return colorHistogram.keySet();
    }

    /** Returns each color paired with its count. The keys are colors, */
    ImmutableSet<Map.Entry<Integer, Integer>> entrySet() {
      return colorHistogram.entrySet();
    }

    @Nullable
    Integer getCount(int color) {
      return colorHistogram.get(color);
    }

    /**
     * Finds the average luminance value within the set of colors for purposes of splitting colors
     * into high-luminance and low-luminance buckets. This is explicitly not a weighted average.
     */
    double calculateAverageLuminance() {
      double luminanceSum = 0;
      for (int color : getColors()) {
        luminanceSum += ContrastUtils.calculateLuminance(color);
      }
      return luminanceSum / getColors().size();
    }

    /**
     * @return a map where the keys are colors that are present in the image, and the values are the
     *     number of pixels with each color.
     */
    private static ImmutableMap<Integer, Integer> processLuminanceData(int[] pixels) {
      if (pixels.length == 0) {
        return ImmutableMap.of();
      }
      Arrays.sort(pixels);

      ImmutableMap.Builder<Integer, Integer> colorHistogram = ImmutableMap.builder();
      int currentColor = pixels[0];
      int currentColorCount = 1;
      for (int i = 1; i < pixels.length; i++) {
        int color = pixels[i];
        if (color == currentColor) {
          currentColorCount++;
        } else {
          colorHistogram.put(currentColor, currentColorCount);
          currentColor = color;
          currentColorCount = 1;
        }
      }
      // Catch the last unprocessed color
      colorHistogram.put(currentColor, currentColorCount);
      return colorHistogram.buildOrThrow();
    }
  }
}

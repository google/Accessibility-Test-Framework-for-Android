/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.apps.common.testing.accessibility.framework.utils.contrast;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;

/** Used as a local replacement for Android's {@link android.graphics.Color} */
public final class Color {

  public static final int BLACK = 0xFF000000;
  public static final int WHITE = 0xFFFFFFFF;

  private Color() {
    // Not instantiable
  }

  /** See {@link android.graphics.Color#alpha(int)}. */
  @IntRange(from = 0, to = 255)
  public static int alpha(int color) {
    return color >>> 24;
  }

  /** See {@link android.graphics.Color#red(int)}. */
  @IntRange(from = 0, to = 255)
  public static int red(int color) {
    return (color >> 16) & 0xFF;
  }

  /** See {@link android.graphics.Color#green(int)}. */
  @IntRange(from = 0, to = 255)
  public static int green(int color) {
    return (color >> 8) & 0xFF;
  }

  /** See {@link android.graphics.Color#blue(int)}. */
  @IntRange(from = 0, to = 255)
  public static int blue(int color) {
    return color & 0xFF;
  }

  /** See {@link android.graphics.Color#argb(int, int, int, int)}. */
  @ColorInt
  public static int argb(
      @IntRange(from = 0, to = 255) int alpha,
      @IntRange(from = 0, to = 255) int red,
      @IntRange(from = 0, to = 255) int green,
      @IntRange(from = 0, to = 255) int blue) {
    return (alpha << 24) | (red << 16) | (green << 8) | blue;
  }
}

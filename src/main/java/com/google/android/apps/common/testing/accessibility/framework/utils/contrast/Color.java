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

/** Used as a local replacement for Android's {@link android.graphics.Color} */
public final class Color {

  public static final int BLACK = 0xFF000000;

  private Color() {
    // Not instantiable
  }

  /** @see android.graphics.Color#alpha(int) */
  public static int alpha(int color) {
    return color >>> 24;
  }

  /** @see android.graphics.Color#red(int) */
  public static int red(int color) {
    return (color >> 16) & 0xFF;
  }

  /** @see android.graphics.Color#green(int) */
  public static int green(int color) {
    return (color >> 8) & 0xFF;
  }

  /** @see android.graphics.Color#blue(int) */
  public static int blue(int color) {
    return color & 0xFF;
  }
}

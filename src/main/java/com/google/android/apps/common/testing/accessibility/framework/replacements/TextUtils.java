/*
 * Copyright (C) 2017 Google Inc.
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

package com.google.android.apps.common.testing.accessibility.framework.replacements;

import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Used as a local replacement for Android's {@link android.text.TextUtils} */
public class TextUtils {
  private TextUtils() {
    // Not instantiable
  }

  /**
   * @see android.text.TextUtils#isEmpty(CharSequence)
   */
  @EnsuresNonNullIf(expression = "#1", result = false)
  public static boolean isEmpty(@Nullable CharSequence str) {
    return (str == null) || (str.length() == 0);
  }

  /** See {@link android.text.TextUtils#getTrimmedLength(CharSequence)}. */
  public static int getTrimmedLength(CharSequence str) {
    return str.toString().trim().length();
  }

  /** See {@link android.text.TextUtils#equals(CharSequence, CharSequence)}. */
  public static boolean equals(@Nullable CharSequence s1, @Nullable CharSequence s2) {
    if (s1 == s2) {
      return true;
    }

    if ((s1 != null) && (s2 != null) && (s1.length() == s2.length())) {
      if (s1 instanceof String && s2 instanceof String) {
        return ((String) s1).equals((String) s2);
      } else {
        for (int i = 0; i < s1.length(); i++) {
          if (s1.charAt(i) != s2.charAt(i)) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }
}

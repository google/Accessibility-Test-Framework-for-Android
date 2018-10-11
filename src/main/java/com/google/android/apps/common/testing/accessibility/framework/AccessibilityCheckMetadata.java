/*
 * Copyright (C) 2016 Google Inc.
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

package com.google.android.apps.common.testing.accessibility.framework;

import static com.google.common.base.Preconditions.checkNotNull;

import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import com.google.android.apps.common.testing.accessibility.framework.checks.ImageContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck;

/** Constants and methods to be used as input keys for {@link AccessibilityCheck} metadata. */
public final class AccessibilityCheckMetadata {
  /**
   * Screen capture data. This is typically used by checks that assess properties of the visual
   * appearance of an interface. For example, contrast evaluation samples regions of the display to
   * heuristically evaluate foreground/background contrast ratios.
   * <p>
   * Expected type: android.graphics.Bitmap, stored in {@link Metadata} as a non-serialized object
   * <p>
   * Expected data: A Bitmap containing a full image of the device's default display at the time
   * check execution occurs. The Bitmap should have dimensions which match the DisplayMetrics
   * obtained from WindowManager's default display. The screen area should not be altered, cropped,
   * or adjusted in any way.
   */
  public static final String METADATA_KEY_SCREEN_CAPTURE_BITMAP =
      "com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckMetadata.METADATA_KEY_SCREEN_CAPTURE_BITMAP";

  /**
   * Metadata key for the {@code double} user-defined heuristic contrast ratio. This is used by
   * {@link ImageContrastCheck} or {@link TextContrastCheck}. When the value for this key is set, it
   * will override the default value used by those checks.
   */
  public static final String METADATA_KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO =
      "com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckMetadata.METADATA_KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO";

  /**
   * Metadata key for the {@code int} user-defined minimum touch target size which is used by {@link
   * TouchTargetSizeCheck}. When the value for this key is set, it will override the default value
   * used by the check.
   */
  public static final String METADATA_KEY_CUSTOMIZED_TOUCH_TARGET_SIZE =
      "com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckMetadata.METADATA_KEY_CUSTOMIZED_TOUCH_TARGET_SIZE";

  private AccessibilityCheckMetadata() {}

  /**
   * @param metadata A {@link Metadata} from which to extract screen capture data.
   * @return A {@link Bitmap} containing screen capture data from the provided {@code metadata}, or
   *     {@code null} if no screen capture data was present.
   */
  public static @Nullable Bitmap getScreenCaptureFromMetadata(@Nullable Metadata metadata) {
    Bitmap screenCapture = null;
    if (metadata != null) {
      screenCapture =
          (Bitmap)
              metadata.getNonSerializedObject(
                  AccessibilityCheckMetadata.METADATA_KEY_SCREEN_CAPTURE_BITMAP, null);
    }
    return screenCapture;
  }

  /**
   * Adds screen capture data from the provided {@code bitmap} to {@code metadata}
   *
   * @param bitmap A {@link Bitmap} containing screen capture data as described by {@link
   *     #METADATA_KEY_SCREEN_CAPTURE_BITMAP}
   * @param metadata The {@link Metadata} to which the screen capture data should be added
   */
  public static void putScreenCaptureInMetadata(Bitmap bitmap, Metadata metadata) {
    checkNotNull(metadata)
        .putNonSerializedObject(METADATA_KEY_SCREEN_CAPTURE_BITMAP, checkNotNull(bitmap));
  }

  /**
   * @param metadata A {@link Metadata} from which to extract the user-defined heuristic contrast
   *     ratio used by {@link ImageContrastCheck} or {@link TextContrastCheck}.
   * @return A {@link Double} for the user-defined heuristic contrast ratio from the provided {@code
   *     metadata}, or {@code null} when the metadata is {@code null} or it does not contain the
   *     key.
   */
  public static @Nullable Double getCustomizedHeuristicContrastRatioInMetadata(
      @Nullable Metadata metadata) {
    if ((metadata != null)
        && metadata.containsKey(METADATA_KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO)) {
      return Double.valueOf(metadata.getDouble(METADATA_KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO));
    } else {
      return null;
    }
  }

  /**
   * @param metadata A {@link Metadata} from which to extract the user-defined minimum touch target
   *     size used by {@link TouchTargetSizeCheck}.
   * @return A {@link Integer} for the user-defined minimum touch target size from the provided
   *     {@code metadata}, or {@code null} when the metadata is {@code null} or it does not contain
   *     the key.
   */
  public static @Nullable Integer getCustomizedTouchTargetSizeInMetadata(
      @Nullable Metadata metadata) {
    if ((metadata != null) && metadata.containsKey(METADATA_KEY_CUSTOMIZED_TOUCH_TARGET_SIZE)) {
      return Integer.valueOf(metadata.getInt(METADATA_KEY_CUSTOMIZED_TOUCH_TARGET_SIZE));
    } else {
      return null;
    }
  }
}

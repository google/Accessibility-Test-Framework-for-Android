/*
 * Copyright (C) 2015 Google Inc.
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

package com.google.android.apps.common.testing.accessibility.framework.checks;

import static com.google.common.base.Preconditions.checkNotNull;

import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import android.widget.ImageView;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckMetadata;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Metadata;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.googlecode.eyesfree.utils.ContrastSwatch;
import com.googlecode.eyesfree.utils.ContrastUtils;
import com.googlecode.eyesfree.utils.ScreenshotUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Check that ensures image foregrounds have sufficient contrast against their background
 */
public class ImageContrastCheck extends AccessibilityHierarchyCheck {

  /** Result when the evaluated element was determined to be not visible to the user */
  public static final int RESULT_ID_NOT_VISIBLE = 1;
  /** Result when the evaluated element was determined to be a view type other than an ImageView */
  public static final int RESULT_ID_NOT_IMAGEVIEW = 2;
  /** Result when we did not receive screen capture information */
  public static final int RESULT_ID_NO_SCREENCAPTURE = 3;
  /** Result when the evaluated element was not within the bounds of the provided capture image */
  public static final int RESULT_ID_VIEW_NOT_WITHIN_SCREENCAPTURE = 4;
  /** Result when the evaluated element's contrast ratio falls below the suggested value */
  public static final int RESULT_ID_IMAGE_CONTRAST_NOT_SUFFICIENT = 5;
  /** Result when the evaluated element was determined to be not enabled */
  public static final int RESULT_ID_NOT_ENABLED = 6;
  /** Result when the evaluated element's screen capture data was determined to be hidden */
  public static final int RESULT_ID_SCREENCAPTURE_DATA_HIDDEN = 7;
  /**
   * Result when the evaluated element's contrast ratio falls below the user-defined contrast ratio
   */
  public static final int RESULT_ID_CUSTOMIZED_IMAGE_CONTRAST_NOT_SUFFICIENT = 8;

  /** Result metadata key for the {@code int} background color of the view. */
  public static final String KEY_BACKGROUND_COLOR = "KEY_BACKGROUND_COLOR";
  /** Result metadata key for the {@code double} computed contrast ratio. */
  public static final String KEY_CONTRAST_RATIO = "KEY_CONTRAST_RATIO";
  /** Result metadata key for the {@code int} foreground color of the view. */
  public static final String KEY_FOREGROUND_COLOR = "KEY_FOREGROUND_COLOR";
  /** Result metadata key for the {@code String} bounds of the screenshot. */
  public static final String KEY_SCREENSHOT_BOUNDS_STRING = "KEY_SCREENSHOT_BOUNDS_STRING";
  /** Result metadata key for the {@code String} bounds of the view. */
  public static final String KEY_VIEW_BOUNDS_STRING = "KEY_VIEW_BOUNDS_STRING";
  /** Result metadata key for the {@code double} user-defined heuristic contrast ratio. */
  public static final String KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO =
      "KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO";

  /** The amount by which a view's computed contrast ratio may fall below defined thresholds */
  private static final double CONTRAST_TOLERANCE = 0.01;

  @Override
  protected String getHelpTopic() {
    return "7158390"; // Color contrast
  }

  @Override
  public Category getCategory() {
    return Category.LOW_CONTRAST;
  }

  @Override
  public List<AccessibilityHierarchyCheckResult> runCheckOnHierarchy(
      AccessibilityHierarchy hierarchy,
      @Nullable ViewHierarchyElement fromRoot,
      @Nullable Metadata metadata) {
    List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();
    Bitmap screenCapture = AccessibilityCheckMetadata.getScreenCaptureFromMetadata(metadata);
    List<ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
    for (ViewHierarchyElement view : viewsToEval) {
      if (!Boolean.TRUE.equals(view.isVisibleToUser())) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_NOT_VISIBLE,
            null));
        continue;
      }

      if (!Boolean.TRUE.equals(view.checkInstanceOf(ImageView.class))) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_NOT_IMAGEVIEW,
            null));
        continue;
      }

      if (!view.isEnabled()) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_NOT_ENABLED,
            null));
        continue;
      }

      if (screenCapture == null) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_NO_SCREENCAPTURE,
            null));
        continue;
      }

      Rect screenCaptureBounds =
          new Rect(0, 0, screenCapture.getWidth() - 1, screenCapture.getHeight() - 1);
      Rect viewBounds = view.getBoundsInScreen();
      if (!screenCaptureBounds.contains(viewBounds)) {
        // If an off-screen view reports itself as visible, we shouldn't evaluate it.
        Metadata resultMetadata = new Metadata();
        resultMetadata.putString(KEY_VIEW_BOUNDS_STRING, viewBounds.toShortString());
        resultMetadata.putString(KEY_SCREENSHOT_BOUNDS_STRING, screenCaptureBounds.toShortString());
        results.add(new AccessibilityHierarchyCheckResult(
            getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_VIEW_NOT_WITHIN_SCREENCAPTURE,
            resultMetadata));
        continue;
      }

      ContrastSwatch candidateSwatch =
          new ContrastSwatch(
              ScreenshotUtils.cropBitmap(
                  screenCapture,
                  viewBounds.getLeft(),
                  viewBounds.getTop(),
                  viewBounds.getWidth(),
                  viewBounds.getHeight()),
              viewBounds.getAndroidInstance(),
              view.getResourceName());

      double contrastRatio = candidateSwatch.getContrastRatio();
      int foreground = candidateSwatch.getForegroundColor();
      int background = candidateSwatch.getBackgroundColor();

      if ((foreground == ContrastUtils.COLOR_SECURE_WINDOW_CENSOR) && (background == foreground)) {
        // In the case the foreground and background colors from ContrastSwatch are the same, and
        // they match the secure window censor color, we can assume the system has intentionally
        // censored our screen capture data, and we should not evaluate this content.
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_SCREENCAPTURE_DATA_HIDDEN,
            null));
        continue;
      }

      // Lower confidence in heuristics for ImageViews, so we'll report only warnings and use the
      // more permissive threshold ratio since images are generally large.
      Double customizedHeuristicContrastRatio =
          AccessibilityCheckMetadata.getCustomizedHeuristicContrastRatioInMetadata(metadata);
      if (customizedHeuristicContrastRatio != null) {
        if (customizedHeuristicContrastRatio - contrastRatio > CONTRAST_TOLERANCE) {
          Metadata resultMetadata = new Metadata();
          resultMetadata.putDouble(KEY_CONTRAST_RATIO, contrastRatio);
          resultMetadata.putDouble(
              KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO,
              customizedHeuristicContrastRatio);
          resultMetadata.putInt(KEY_FOREGROUND_COLOR, foreground);
          resultMetadata.putInt(KEY_BACKGROUND_COLOR, background);
          results.add(
              new AccessibilityHierarchyCheckResult(
                  getClass(),
                  AccessibilityCheckResultType.WARNING,
                  view,
                  RESULT_ID_CUSTOMIZED_IMAGE_CONTRAST_NOT_SUFFICIENT,
                  resultMetadata));
        }
      } else if ((ContrastUtils.CONTRAST_RATIO_WCAG_LARGE_TEXT - contrastRatio)
          > CONTRAST_TOLERANCE) {
        Metadata resultMetadata = new Metadata();
        resultMetadata.putDouble(KEY_CONTRAST_RATIO, contrastRatio);
        resultMetadata.putInt(KEY_FOREGROUND_COLOR, foreground);
        resultMetadata.putInt(KEY_BACKGROUND_COLOR, background);
        results.add(new AccessibilityHierarchyCheckResult(
            getClass(),
            AccessibilityCheckResultType.WARNING,
            view,
            RESULT_ID_IMAGE_CONTRAST_NOT_SUFFICIENT,
            resultMetadata));
      }

      candidateSwatch.recycle();
    }

    return results;
  }

  @Override
  public String getMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    String generated = generateMessageForResultId(locale, resultId);
    if (generated != null) {
      return generated;
    }

    // For each of the following result IDs, metadata will have been set on the result.
    checkNotNull(metadata);
    switch (resultId) {
      case RESULT_ID_VIEW_NOT_WITHIN_SCREENCAPTURE:
        return String.format(locale,
            StringManager.getString(locale, "result_message_view_not_within_screencapture"),
            metadata.getString(KEY_VIEW_BOUNDS_STRING),
            metadata.getString(KEY_SCREENSHOT_BOUNDS_STRING));
      case RESULT_ID_IMAGE_CONTRAST_NOT_SUFFICIENT:
        return String.format(locale,
            StringManager.getString(locale, "result_message_image_contrast_not_sufficient"),
            metadata.getDouble(KEY_CONTRAST_RATIO),
            ContrastUtils.CONTRAST_RATIO_WCAG_LARGE_TEXT,
            metadata.getInt(KEY_FOREGROUND_COLOR) & 0xFFFFFF,
            metadata.getInt(KEY_BACKGROUND_COLOR) & 0xFFFFFF);
      case RESULT_ID_CUSTOMIZED_IMAGE_CONTRAST_NOT_SUFFICIENT:
        return String.format(
            locale,
            StringManager.getString(
                locale, "result_message_image_customized_contrast_not_sufficient"),
            metadata.getDouble(KEY_CONTRAST_RATIO),
            metadata.getDouble(KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO),
            metadata.getInt(KEY_FOREGROUND_COLOR) & 0xFFFFFF,
            metadata.getInt(KEY_BACKGROUND_COLOR) & 0xFFFFFF);
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getShortMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    String generated = generateMessageForResultId(locale, resultId);
    if (generated != null) {
      return generated;
    }

    switch (resultId) {
      case RESULT_ID_VIEW_NOT_WITHIN_SCREENCAPTURE:
        return StringManager.getString(locale, "result_message_no_screencapture"); // Close enough
      case RESULT_ID_IMAGE_CONTRAST_NOT_SUFFICIENT:
      case RESULT_ID_CUSTOMIZED_IMAGE_CONTRAST_NOT_SUFFICIENT:
        return StringManager.getString(
            locale, "result_message_brief_image_contrast_not_sufficient");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_image_contrast");
  }

  private static @Nullable String generateMessageForResultId(Locale locale, int resultId) {
    switch (resultId) {
      case RESULT_ID_NOT_VISIBLE:
        return StringManager.getString(locale, "result_message_not_visible");
      case RESULT_ID_NOT_IMAGEVIEW:
        return StringManager.getString(locale, "result_message_not_imageview");
      case RESULT_ID_NOT_ENABLED:
        return StringManager.getString(locale, "result_message_not_enabled");
      case RESULT_ID_NO_SCREENCAPTURE:
        return StringManager.getString(locale, "result_message_no_screencapture");
      case RESULT_ID_SCREENCAPTURE_DATA_HIDDEN:
        return StringManager.getString(locale, "result_message_screencapture_data_hidden");
      default:
        return null;
    }
  }
}

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
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import android.widget.Switch;
import android.widget.TextView;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckMetadata;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Metadata;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
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
 * Check that ensures text content has sufficient contrast against its background
 */
public class TextContrastCheck extends AccessibilityHierarchyCheck {

  /** Result when the view is not visible. */
  public static final int RESULT_ID_NOT_VISIBLE = 1;
  /** Result when the view is not a {@link TextView}. */
  public static final int RESULT_ID_NOT_TEXT_VIEW = 2;
  /** Result when the {@link TextView} is empty. */
  public static final int RESULT_ID_TEXTVIEW_EMPTY = 3;
  /** Result when the view's text color could not be obtained. */
  public static final int RESULT_ID_COULD_NOT_GET_TEXT_COLOR = 4;
  /** Result when the view's background color could not be obtained. */
  public static final int RESULT_ID_COULD_NOT_GET_BACKGROUND_COLOR = 5;
  /** Result when the view's text is not opaque. */
  public static final int RESULT_ID_TEXT_MUST_BE_OPAQUE = 6;
  /** Result when the view's backgorund is not opaque. */
  public static final int RESULT_ID_BACKGROUND_MUST_BE_OPAQUE = 7;
  /** Result when the view's contrast is insufficient based on opaque text/background. */
  public static final int RESULT_ID_TEXTVIEW_CONTRAST_NOT_SUFFICIENT = 8;
  /** Result when the heuristic evaluation could not obtain a screenshot. */
  public static final int RESULT_ID_HEURISTIC_COULD_NOT_GET_SCREENCAPTURE = 9;
  /** Result when the view is not completely within the screenshot. */
  public static final int RESULT_ID_VIEW_NOT_WITHIN_SCREENCAPTURE = 10;
  /** Result when the view's contrast is insufficient based on heuristic evaluation. */
  public static final int RESULT_ID_TEXTVIEW_HEURISTIC_CONTRAST_NOT_SUFFICIENT = 11;
  /** Result when the view's contrast is between the small text ratio and the large text ratio. */
  public static final int RESULT_ID_TEXTVIEW_HEURISTIC_CONTRAST_BORDERLINE = 12;
  /** Result when the evaluated element was determined to be not enabled */
  public static final int RESULT_ID_NOT_ENABLED = 13;
  /** Result when the evaluated element's screen capture data was determined to be hidden */
  public static final int RESULT_ID_SCREENCAPTURE_DATA_HIDDEN = 14;
  /**
   * Result when the view's contrast is insufficient based on heuristic evaluation which uses
   * user-defined heuristic contrast ratio.
   */
  public static final int RESULT_ID_CUSTOMIZED_TEXTVIEW_HEURISTIC_CONTRAST_NOT_SUFFICIENT = 15;

  /** Result metadata key for the {@code int} color of the view's background. */
  public static final String KEY_BACKGROUND_COLOR = "KEY_BACKGROUND_COLOR";
  /** Result metadata key for the {@code int} (0-100)% opacity of the view's background. */
  public static final String KEY_BACKGROUND_OPACITY = "KEY_BACKGROUND_OPACITY";
  /** Result metadata key for the {@code double} computed contrast ratio of the view. */
  public static final String KEY_CONTRAST_RATIO = "KEY_CONTRAST_RATIO";
  /** Result metadata key for the {@code int} color of the view's foreground. */
  public static final String KEY_FOREGROUND_COLOR = "KEY_FOREGROUND_COLOR";
  /** Result metadata key for the {@code double} required contrast ratio for this view. */
  public static final String KEY_REQUIRED_CONTRAST_RATIO = "KEY_REQUIRED_CONTRAST_RATIO";
  /**
   * Result metadata key for the {@code double} user-defined heuristic contrast ratio for this view.
   */
  public static final String KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO =
      "KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO";
  /** Result metadata key for the {@code String} bounds of the screenshot. */
  public static final String KEY_SCREENSHOT_BOUNDS_STRING = "KEY_SCREENSHOT_BOUNDS_STRING";
  /** Result metadata key for the {@code int} color of the view's text. */
  public static final String KEY_TEXT_COLOR = "KEY_TEXT_COLOR";
  /** Result metadata key for the {@code int} (0-100)% opacity of the view's text. */
  public static final String KEY_TEXT_OPACITY = "KEY_TEXT_OPACITY";
  /** Result metadata key for {@code double} required contrast ratio for large text. */
  public static final String KEY_TOLERANT_CONTRAST_RATIO = "KEY_TOLERANT_CONTRAST_RATIO";
  /** Result metadata key for the {@code String} bounds of the view. */
  public static final String KEY_VIEW_BOUNDS_STRING = "KEY_VIEW_BOUNDS_STRING";

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

      if ((!Boolean.TRUE.equals(view.checkInstanceOf(TextView.class)))
          || (Boolean.TRUE.equals(view.checkInstanceOf(Switch.class)))) {
        // Only evaluate TextView subclasses, but exclude Switch, which has not displayed text by
        // default since API 21.
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_NOT_TEXT_VIEW,
                null));
        continue;
      }

      if (TextUtils.isEmpty(view.getText())) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_TEXTVIEW_EMPTY,
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

      AccessibilityHierarchyCheckResult lightweightResult = attemptLightweightEval(view);
      if (lightweightResult != null) {
        results.add(lightweightResult);
        if (lightweightResult.getType() == AccessibilityCheckResultType.NOT_RUN) {
          // Lightweight evaluation didn't run successfully.
          AccessibilityHierarchyCheckResult heavyweightResult =
              attemptHeavyweightEval(view, screenCapture, metadata);
          if (heavyweightResult != null) {
            // Heavyweight evaluation found a noteworthy issue.
            results.add(heavyweightResult);
          }
        }
      }
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
    StringBuilder builder;
    switch(resultId) {
      case RESULT_ID_TEXT_MUST_BE_OPAQUE:
        builder = new StringBuilder();
        builder.append(StringManager.getString(locale, "result_message_text_must_be_opaque"))
            .append(" ")
            .append(String.format(locale,
                StringManager.getString(locale, "result_message_addendum_opacity_description"),
                metadata.getFloat(KEY_TEXT_OPACITY)));
        return builder.toString();
      case RESULT_ID_BACKGROUND_MUST_BE_OPAQUE:
        builder = new StringBuilder();
        builder.append(StringManager.getString(locale, "result_message_background_must_be_opaque"))
            .append(" ")
            .append(String.format(locale,
                StringManager.getString(locale, "result_message_addendum_opacity_description"),
                metadata.getFloat(KEY_BACKGROUND_OPACITY)));
        return builder.toString();
      case RESULT_ID_TEXTVIEW_CONTRAST_NOT_SUFFICIENT:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_textview_contrast_not_sufficient"),
            metadata.getDouble(KEY_CONTRAST_RATIO),
            metadata.getDouble(KEY_REQUIRED_CONTRAST_RATIO),
            metadata.getInt(KEY_TEXT_COLOR) & 0xFFFFFF,
            metadata.getInt(KEY_BACKGROUND_COLOR) & 0xFFFFFF);
      case RESULT_ID_VIEW_NOT_WITHIN_SCREENCAPTURE:
        return String.format(locale,
            StringManager.getString(locale, "result_message_view_not_within_screencapture"),
            metadata.getString(KEY_VIEW_BOUNDS_STRING),
            metadata.getString(KEY_SCREENSHOT_BOUNDS_STRING));
      case RESULT_ID_TEXTVIEW_HEURISTIC_CONTRAST_NOT_SUFFICIENT:
        return String.format(
            locale,
            StringManager.getString(
                locale, "result_message_textview_heuristic_contrast_not_sufficient"),
            metadata.getDouble(KEY_CONTRAST_RATIO),
            metadata.getDouble(KEY_REQUIRED_CONTRAST_RATIO),
            metadata.getInt(KEY_FOREGROUND_COLOR) & 0xFFFFFF,
            metadata.getInt(KEY_BACKGROUND_COLOR) & 0xFFFFFF);
      case RESULT_ID_TEXTVIEW_HEURISTIC_CONTRAST_BORDERLINE:
        return String.format(
            locale,
            StringManager.getString(
                locale, "result_message_textview_heuristic_contrast_borderline"),
            metadata.getDouble(KEY_CONTRAST_RATIO),
            metadata.getDouble(KEY_REQUIRED_CONTRAST_RATIO),
            metadata.getDouble(KEY_TOLERANT_CONTRAST_RATIO),
            metadata.getInt(KEY_FOREGROUND_COLOR) & 0xFFFFFF,
            metadata.getInt(KEY_BACKGROUND_COLOR) & 0xFFFFFF);
      case RESULT_ID_CUSTOMIZED_TEXTVIEW_HEURISTIC_CONTRAST_NOT_SUFFICIENT:
        return String.format(
            locale,
            StringManager.getString(
                locale, "result_message_textview_heuristic_customized_contrast_not_sufficient"),
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

    switch(resultId) {
      case RESULT_ID_TEXT_MUST_BE_OPAQUE:
        return StringManager.getString(locale, "result_message_text_must_be_opaque");
      case RESULT_ID_BACKGROUND_MUST_BE_OPAQUE:
        return StringManager.getString(locale, "result_message_background_must_be_opaque");
      case RESULT_ID_VIEW_NOT_WITHIN_SCREENCAPTURE:
        return StringManager.getString(locale, "result_message_no_screencapture"); // Close enough
      case RESULT_ID_TEXTVIEW_CONTRAST_NOT_SUFFICIENT:
      case RESULT_ID_TEXTVIEW_HEURISTIC_CONTRAST_NOT_SUFFICIENT:
      case RESULT_ID_TEXTVIEW_HEURISTIC_CONTRAST_BORDERLINE:
      case RESULT_ID_CUSTOMIZED_TEXTVIEW_HEURISTIC_CONTRAST_NOT_SUFFICIENT:
        return StringManager.getString(locale, "result_message_brief_text_contrast_not_sufficient");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_text_contrast");
  }

  private static @Nullable String generateMessageForResultId(Locale locale, int resultId) {
    switch(resultId) {
      case RESULT_ID_NOT_VISIBLE:
        return StringManager.getString(locale, "result_message_not_visible");
      case RESULT_ID_NOT_TEXT_VIEW:
        return StringManager.getString(locale, "result_message_not_text_view");
      case RESULT_ID_NOT_ENABLED:
        return StringManager.getString(locale, "result_message_not_enabled");
      case RESULT_ID_TEXTVIEW_EMPTY:
        return StringManager.getString(locale, "result_message_textview_empty");
      case RESULT_ID_COULD_NOT_GET_TEXT_COLOR:
        return StringManager.getString(locale, "result_message_could_not_get_text_color");
      case RESULT_ID_COULD_NOT_GET_BACKGROUND_COLOR:
        return StringManager.getString(locale, "result_message_could_not_get_background_color");
      case RESULT_ID_HEURISTIC_COULD_NOT_GET_SCREENCAPTURE:
        return StringManager.getString(locale, "result_message_no_screencapture");
      case RESULT_ID_SCREENCAPTURE_DATA_HIDDEN:
        return StringManager.getString(locale, "result_message_screencapture_data_hidden");
      default:
        return null;
    }
  }

  /**
   * Performs lightweight contrast evaluation on the provided {@code view}. During lightweight
   * evaluation, we examine text color and background Drawables to extract properties about
   * component colors.
   *
   * @param view The {@link ViewHierarchyElement} to evaluate
   * @return an {@link AccessibilityHierarchyCheckResult} describing the result of the lightweight
   *         evaluation, or {@code null} if there is sufficient text contrast.
   */
  private @Nullable AccessibilityHierarchyCheckResult attemptLightweightEval(
      ViewHierarchyElement view) {
    Integer textColor = view.getTextColor();
    Integer backgroundDrawableColor = view.getBackgroundDrawableColor();
    if (textColor == null) {
      return new AccessibilityHierarchyCheckResult(
          this.getClass(),
          AccessibilityCheckResultType.NOT_RUN,
          view,
          RESULT_ID_COULD_NOT_GET_TEXT_COLOR,
          null);
    }

    if (backgroundDrawableColor == null) {
      return new AccessibilityHierarchyCheckResult(
          this.getClass(),
          AccessibilityCheckResultType.NOT_RUN,
          view,
          RESULT_ID_COULD_NOT_GET_BACKGROUND_COLOR,
          null);
    }

    int textAlpha = Color.alpha(textColor);
    if (textAlpha < 255) {
      Metadata resultMetadata = new Metadata();
      resultMetadata.putFloat(KEY_TEXT_OPACITY, (textAlpha / 255f) * 100);

      return new AccessibilityHierarchyCheckResult(
          this.getClass(),
          AccessibilityCheckResultType.NOT_RUN,
          view,
          RESULT_ID_TEXT_MUST_BE_OPAQUE,
          resultMetadata);
    }

    int backgroundAlpha = Color.alpha(backgroundDrawableColor);
    if (backgroundAlpha < 255) {
      Metadata resultMetadata = new Metadata();
      resultMetadata.putFloat(KEY_BACKGROUND_OPACITY, (backgroundAlpha / 255f) * 100);

      return new AccessibilityHierarchyCheckResult(
          this.getClass(),
          AccessibilityCheckResultType.NOT_RUN,
          view,
          RESULT_ID_BACKGROUND_MUST_BE_OPAQUE,
          resultMetadata);
    }

    double contrastRatio = ContrastUtils.calculateContrastRatio(textColor, backgroundDrawableColor);
    double requiredContrast = isLargeText(view) ? ContrastUtils.CONTRAST_RATIO_WCAG_LARGE_TEXT
        : ContrastUtils.CONTRAST_RATIO_WCAG_NORMAL_TEXT;
    if ((requiredContrast - contrastRatio) > CONTRAST_TOLERANCE) {
      Metadata resultMetadata = new Metadata();
      resultMetadata.putDouble(KEY_REQUIRED_CONTRAST_RATIO, requiredContrast);
      resultMetadata.putDouble(KEY_CONTRAST_RATIO, contrastRatio);
      resultMetadata.putInt(KEY_TEXT_COLOR, textColor);
      resultMetadata.putInt(KEY_BACKGROUND_COLOR, backgroundDrawableColor);

      return new AccessibilityHierarchyCheckResult(
          this.getClass(),
          AccessibilityCheckResultType.ERROR,
          view,
          RESULT_ID_TEXTVIEW_CONTRAST_NOT_SUFFICIENT,
          resultMetadata);
    }

    // Sufficient contrast
    return null;
  }

  /**
   * Performs heavyweight contrast evaluation on the provided {@code view}. During heavyweight
   * evaluation, we examine the contents of a {@link Bitmap} screen capture to heuristically
   * determine foreground and background colors, and from there, compute a contrast ratio.
   *
   * @param view The {@link ViewHierarchyElement} to evaluate
   * @param screenCapture A {@link Bitmap} of screen capture data
   * @param metadata An optional {@link Metadata} that may contain check metadata defined by {@link
   *     AccessibilityCheckMetadata}.
   * @return an {@link AccessibilityHierarchyCheckResult} describing the results of the heavyweight
   *     evaluation, or {@code null} if there is sufficient text contrast.
   */
  private @Nullable AccessibilityHierarchyCheckResult attemptHeavyweightEval(
      ViewHierarchyElement view, @Nullable Bitmap screenCapture, @Nullable Metadata metadata) {
    if (screenCapture == null) {
      return new AccessibilityHierarchyCheckResult(
          this.getClass(),
          AccessibilityCheckResultType.NOT_RUN,
          view,
          RESULT_ID_HEURISTIC_COULD_NOT_GET_SCREENCAPTURE,
          null);
    }
    Rect screenCaptureBounds =
        new Rect(0, 0, screenCapture.getWidth() - 1, screenCapture.getHeight() - 1);
    Rect viewBounds = view.getBoundsInScreen();
    if (!screenCaptureBounds.contains(viewBounds)) {
      // If an off-screen view reports itself as visible, we shouldn't evaluate it.
      Metadata resultMetadata = new Metadata();
      resultMetadata.putString(KEY_VIEW_BOUNDS_STRING, viewBounds.toShortString());
      resultMetadata.putString(KEY_SCREENSHOT_BOUNDS_STRING, screenCaptureBounds.toShortString());

      return new AccessibilityHierarchyCheckResult(
          getClass(),
          AccessibilityCheckResultType.NOT_RUN,
          view,
          RESULT_ID_VIEW_NOT_WITHIN_SCREENCAPTURE,
          resultMetadata);
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
    try {
      double contrastRatio = candidateSwatch.getContrastRatio();
      int foreground = candidateSwatch.getForegroundColor();
      int background = candidateSwatch.getBackgroundColor();

      if ((foreground == ContrastUtils.COLOR_SECURE_WINDOW_CENSOR) && (background == foreground)) {
        // In the case the foreground and background colors from ContrastSwatch are the same, and
        // they match the secure window censor color, we can assume the system has intentionally
        // censored our screen capture data, and we should not evaluate this content.
        return new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_SCREENCAPTURE_DATA_HIDDEN,
            null);
      }

      Double customizedHeuristicContrastRatio =
          AccessibilityCheckMetadata.getCustomizedHeuristicContrastRatioInMetadata(metadata);
      if (customizedHeuristicContrastRatio != null) {
        if ((customizedHeuristicContrastRatio - contrastRatio) > CONTRAST_TOLERANCE) {
          Metadata resultMetadata = new Metadata();
          resultMetadata.putDouble(KEY_CONTRAST_RATIO, contrastRatio);
          resultMetadata.putDouble(
              KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO, customizedHeuristicContrastRatio);
          resultMetadata.putInt(KEY_FOREGROUND_COLOR, foreground);
          resultMetadata.putInt(KEY_BACKGROUND_COLOR, background);

          return new AccessibilityHierarchyCheckResult(
              getClass(),
              AccessibilityCheckResultType.ERROR,
              view,
              RESULT_ID_CUSTOMIZED_TEXTVIEW_HEURISTIC_CONTRAST_NOT_SUFFICIENT,
              resultMetadata);
        }
      } else {
        if ((ContrastUtils.CONTRAST_RATIO_WCAG_LARGE_TEXT - contrastRatio) > CONTRAST_TOLERANCE) {
          Metadata resultMetadata = new Metadata();
          resultMetadata.putDouble(
              KEY_REQUIRED_CONTRAST_RATIO, ContrastUtils.CONTRAST_RATIO_WCAG_LARGE_TEXT);
          resultMetadata.putDouble(KEY_CONTRAST_RATIO, contrastRatio);
          resultMetadata.putInt(KEY_FOREGROUND_COLOR, foreground);
          resultMetadata.putInt(KEY_BACKGROUND_COLOR, background);

          return new AccessibilityHierarchyCheckResult(
              getClass(),
              AccessibilityCheckResultType.ERROR,
              view,
              RESULT_ID_TEXTVIEW_HEURISTIC_CONTRAST_NOT_SUFFICIENT,
              resultMetadata);
        } else if ((ContrastUtils.CONTRAST_RATIO_WCAG_NORMAL_TEXT - contrastRatio)
            > CONTRAST_TOLERANCE) {
          Metadata resultMetadata = new Metadata();
          resultMetadata.putDouble(
              KEY_REQUIRED_CONTRAST_RATIO, ContrastUtils.CONTRAST_RATIO_WCAG_NORMAL_TEXT);
          resultMetadata.putDouble(
              KEY_TOLERANT_CONTRAST_RATIO, ContrastUtils.CONTRAST_RATIO_WCAG_LARGE_TEXT);
          resultMetadata.putDouble(KEY_CONTRAST_RATIO, contrastRatio);
          resultMetadata.putInt(KEY_FOREGROUND_COLOR, foreground);
          resultMetadata.putInt(KEY_BACKGROUND_COLOR, background);

          return new AccessibilityHierarchyCheckResult(
              getClass(),
              AccessibilityCheckResultType.WARNING,
              view,
              RESULT_ID_TEXTVIEW_HEURISTIC_CONTRAST_BORDERLINE,
              resultMetadata);
        }
      }

      // Sufficient contrast
      return null;
    } finally {
      candidateSwatch.recycle();
    }
  }

  private static boolean isLargeText(ViewHierarchyElement view) {
    float density = view.getWindow().getAccessibilityHierarchy().getDeviceState()
        .getDefaultDisplayInfo().getMetricsWithoutDecoration().getScaledDensity();
    Float textSize = view.getTextSize();
    float dpSize = (textSize != null) ? textSize / density : 0;
    int style = (view.getTypefaceStyle() != null) ? view.getTypefaceStyle() : Typeface.NORMAL;
    return (dpSize >= ContrastUtils.WCAG_LARGE_TEXT_MIN_SIZE)
        || ((dpSize >= ContrastUtils.WCAG_LARGE_BOLD_TEXT_MIN_SIZE)
            && ((style & Typeface.BOLD) != 0));
  }
}

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

import static com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils.isPotentiallyObscured;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResultWithImage;
import com.google.android.apps.common.testing.accessibility.framework.HashMapResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.QuestionHandler;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.ContrastSwatch;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.ContrastUtils;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.Image;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

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
  /** Result when the evaluated element's screen capture has a uniform color */
  public static final int RESULT_ID_SCREENCAPTURE_UNIFORM_COLOR = 9;

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
  /** Result metadata key for whether the view may be obscured by other on-screen content. */
  public static final String KEY_IS_POTENTIALLY_OBSCURED = "KEY_IS_POTENTIALLY_OBSCURED";
  /**
   * Result metadata key for a {@code boolean} which is {@code true} iff the view is determined to
   * be touching the scrollable edge of a scrollable container.
   */
  public static final String KEY_IS_AGAINST_SCROLLABLE_EDGE = "KEY_IS_AGAINST_SCROLLABLE_EDGE";

  /** Result metadata key for the {@code ArrayList<String>} foreground colors of the view. */
  public static final String KEY_ADDITIONAL_FOREGROUND_COLORS = "KEY_ADDITIONAL_FOREGROUND_COLORS";
  /** Result metadata key for the {@code ArrayList<String>} computed contrast ratio. */
  public static final String KEY_ADDITIONAL_CONTRAST_RATIOS = "KEY_ADDITIONAL_CONTRAST_RATIOS";

  /** The amount by which a view's computed contrast ratio may fall below defined thresholds */
  public static final double CONTRAST_TOLERANCE = 0.01;

  private static final Class<? extends AccessibilityHierarchyCheck> CHECK_CLASS =
      ImageContrastCheck.class;

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
      @Nullable Parameters parameters) {
    List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();
    List<? extends ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
    for (ViewHierarchyElement view : viewsToEval) {
      if (!Boolean.TRUE.equals(view.isVisibleToUser())) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                CHECK_CLASS,
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_NOT_VISIBLE,
                null));
        continue;
      }

      if (!view.checkInstanceOf(ViewHierarchyElementUtils.IMAGE_VIEW_CLASS_NAME)) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                CHECK_CLASS,
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_NOT_IMAGEVIEW,
                null));
        continue;
      }

      if (!view.isEnabled()) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                CHECK_CLASS,
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_NOT_ENABLED,
                null));
        continue;
      }

      AccessibilityHierarchyCheckResult heavyweightResult =
          attemptHeavyweightEval(view, parameters);
      if (heavyweightResult != null) {
        results.add(heavyweightResult);
      }
    }

    return results;
  }

  /**
   * Performs heavyweight contrast evaluation on the provided {@code view}. During heavyweight
   * evaluation, we examine the contents of a screen capture to heuristically determine foreground
   * and background colors, and from there, compute a contrast ratio.
   *
   * @param view The {@link ViewHierarchyElement} to evaluate
   * @param parameters Optional check input parameters
   * @return an {@link AccessibilityHierarchyCheckResult} describing the results of the heavyweight
   *     evaluation, or {@code null} if there is no problem detected.
   */
  private @Nullable AccessibilityHierarchyCheckResult attemptHeavyweightEval(
      ViewHierarchyElement view, @Nullable Parameters parameters) {
    Image screenCapture = (parameters == null) ? null : parameters.getScreenCapture();
    if (screenCapture == null) {
      return new AccessibilityHierarchyCheckResult(
          CHECK_CLASS,
          AccessibilityCheckResultType.NOT_RUN,
          view,
          RESULT_ID_NO_SCREENCAPTURE,
          null);
    }

    Rect screenCaptureBounds = new Rect(0, 0, screenCapture.getWidth(), screenCapture.getHeight());
    Rect viewBounds = view.getBoundsInScreen();
    if (viewBounds.isEmpty() || !screenCaptureBounds.contains(viewBounds)) {
      // If an off-screen view reports itself as visible, we shouldn't evaluate it.
      ResultMetadata resultMetadata = new HashMapResultMetadata();
      resultMetadata.putString(KEY_VIEW_BOUNDS_STRING, viewBounds.toShortString());
      resultMetadata.putString(KEY_SCREENSHOT_BOUNDS_STRING, screenCaptureBounds.toShortString());
      return new AccessibilityHierarchyCheckResult(
          CHECK_CLASS,
          AccessibilityCheckResultType.NOT_RUN,
          view,
          RESULT_ID_VIEW_NOT_WITHIN_SCREENCAPTURE,
          resultMetadata);
    }

    // viewBounds cannot be out of bounds because the bounds were checked above.
    Image viewImage = crop(screenCapture, viewBounds);
    ContrastSwatch contrastSwatch =
        getContrastSwatch(
            viewImage,
            (parameters == null) ? null : parameters.getEnableEnhancedContrastEvaluation());
    ResultMetadata resultMetadata = new HashMapResultMetadata();
    if (view.isAgainstScrollableEdge()) {
      resultMetadata.putBoolean(KEY_IS_AGAINST_SCROLLABLE_EDGE, true);
    }
    int foreground = contrastSwatch.getForegroundColors().get(0);
    int background = contrastSwatch.getBackgroundColor();

    if (background == foreground) {
      // In the case the foreground and background colors from ContrastSwatch are the same, and
      // they match the secure window censor color, we can assume the system has intentionally
      // censored our screen capture data, and we should not evaluate this content.
      return new AccessibilityHierarchyCheckResult(
          CHECK_CLASS,
          AccessibilityCheckResultType.NOT_RUN,
          view,
          (foreground == ContrastUtils.COLOR_SECURE_WINDOW_CENSOR)
              ? RESULT_ID_SCREENCAPTURE_DATA_HIDDEN
              : RESULT_ID_SCREENCAPTURE_UNIFORM_COLOR,
          resultMetadata);
    }

    List<Integer> foregroundColors = contrastSwatch.getForegroundColors();
    List<Double> contrastRatios = contrastSwatch.getContrastRatios();
    ArrayList<Integer> lowForegroundColors = new ArrayList<>();
    ArrayList<Double> lowContrastRatios = new ArrayList<>();

    // Lower confidence in heuristics for ImageViews, so we'll report only warnings and use the
    // more permissive threshold ratio since images are generally large.
    Double customizedHeuristicContrastRatio =
        (parameters == null) ? null : parameters.getCustomImageContrastRatio();
    if (customizedHeuristicContrastRatio != null) {

      for (int i = 0; i < contrastRatios.size(); i++) {
        if (customizedHeuristicContrastRatio - contrastRatios.get(i) > CONTRAST_TOLERANCE) {
          lowForegroundColors.add(foregroundColors.get(i));
          lowContrastRatios.add(contrastRatios.get(i));
        }
      }
      if (!lowContrastRatios.isEmpty()) {
        if (isPotentiallyObscured(view)) {
          resultMetadata.putBoolean(KEY_IS_POTENTIALLY_OBSCURED, true);
        }
        resultMetadata.putDouble(
            KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO, customizedHeuristicContrastRatio);
        storeColorsAndContrastRatios(
            resultMetadata, background, lowForegroundColors, lowContrastRatios);
        return resultPossiblyWithImage(
            AccessibilityCheckResultType.WARNING,
            view,
            RESULT_ID_CUSTOMIZED_IMAGE_CONTRAST_NOT_SUFFICIENT,
            resultMetadata,
            parameters,
            viewImage);
      }
    } else {
      for (int i = 0; i < contrastRatios.size(); i++) {
        if ((ContrastUtils.CONTRAST_RATIO_WCAG_LARGE_TEXT - contrastRatios.get(i))
            > CONTRAST_TOLERANCE) {
          lowForegroundColors.add(foregroundColors.get(i));
          lowContrastRatios.add(contrastRatios.get(i));
        }
      }
      if (!lowContrastRatios.isEmpty()) {
        if (isPotentiallyObscured(view)) {
          resultMetadata.putBoolean(KEY_IS_POTENTIALLY_OBSCURED, true);
        }
        storeColorsAndContrastRatios(
            resultMetadata, background, lowForegroundColors, lowContrastRatios);
        return resultPossiblyWithImage(
            AccessibilityCheckResultType.WARNING,
            view,
            RESULT_ID_IMAGE_CONTRAST_NOT_SUFFICIENT,
            resultMetadata,
            parameters,
            viewImage);
      }
    }
    return null;
  }

  @Override
  public String getMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    String generated = generateMessageForResultId(locale, resultId);
    if (generated != null) {
      return generated;
    }

    // For each of the following result IDs, metadata will have been set on the result.

    StringBuilder builder;
    checkNotNull(metadata);
    switch (resultId) {
      case RESULT_ID_VIEW_NOT_WITHIN_SCREENCAPTURE:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_view_not_within_screencapture"),
            metadata.getString(KEY_VIEW_BOUNDS_STRING),
            metadata.getString(KEY_SCREENSHOT_BOUNDS_STRING));
      case RESULT_ID_IMAGE_CONTRAST_NOT_SUFFICIENT:
        builder =
            new StringBuilder(
                String.format(
                    locale,
                    StringManager.getString(locale, "result_message_image_contrast_not_sufficient"),
                    metadata.getDouble(KEY_CONTRAST_RATIO),
                    ContrastUtils.CONTRAST_RATIO_WCAG_LARGE_TEXT,
                    metadata.getInt(KEY_FOREGROUND_COLOR) & 0xFFFFFF,
                    metadata.getInt(KEY_BACKGROUND_COLOR) & 0xFFFFFF));
        appendMetadataStringsToMessageIfNeeded(locale, metadata, builder);
        return builder.toString();
      case RESULT_ID_CUSTOMIZED_IMAGE_CONTRAST_NOT_SUFFICIENT:
        builder =
            new StringBuilder(
                String.format(
                    locale,
                    StringManager.getString(
                        locale, "result_message_image_customized_contrast_not_sufficient"),
                    metadata.getDouble(KEY_CONTRAST_RATIO),
                    metadata.getDouble(KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO),
                    metadata.getInt(KEY_FOREGROUND_COLOR) & 0xFFFFFF,
                    metadata.getInt(KEY_BACKGROUND_COLOR) & 0xFFFFFF));
        appendMetadataStringsToMessageIfNeeded(locale, metadata, builder);
        return builder.toString();
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

  /**
   * Calculates a secondary priority for an image contrast result.
   *
   * <p>The result is the amount that the actual contrast ratio is below the required ratio. Thus,
   * for a given required contrast ratio, decreasing contrast gives a higher priority.
   */
  @Override
  public @Nullable Double getSecondaryPriority(AccessibilityHierarchyCheckResult result) {
    ResultMetadata metadata = result.getMetadata();
    switch (result.getResultId()) {
      case RESULT_ID_IMAGE_CONTRAST_NOT_SUFFICIENT:
        return ContrastUtils.CONTRAST_RATIO_WCAG_LARGE_TEXT
            - checkNotNull(metadata).getDouble(KEY_CONTRAST_RATIO, 0.0);
      case RESULT_ID_CUSTOMIZED_IMAGE_CONTRAST_NOT_SUFFICIENT:
        return checkNotNull(metadata).getDouble(KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO, 0)
            - checkNotNull(metadata).getDouble(KEY_CONTRAST_RATIO, 0.0);
      default:
        return null;
    }
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_image_contrast");
  }

  @VisibleForTesting
  ContrastSwatch getContrastSwatch(
      Image image, @Nullable Boolean enableEnhancedContrastEvaluation) {
    return new ContrastSwatch(
        image,
        (enableEnhancedContrastEvaluation == null) ? false : enableEnhancedContrastEvaluation);
  }

  private static Image crop(Image screenCapture, Rect viewBounds) {
    return screenCapture.crop(
        viewBounds.getLeft(), viewBounds.getTop(), viewBounds.getWidth(), viewBounds.getHeight());
  }

  /**
   * Returns a new instance of AccessibilityHierarchyCheckResultWithImage if {@code viewImage} is
   * not {@code null} and {@code parameters} specifies that view images should be saved. Otherwise,
   * returns a new instance of AccessibilityHierarchyCheckResult.
   */
  private static AccessibilityHierarchyCheckResult resultPossiblyWithImage(
      AccessibilityCheckResultType type,
      ViewHierarchyElement view,
      int resultId,
      ResultMetadata metadata,
      @Nullable Parameters parameters,
      @Nullable Image viewImage) {
    if ((viewImage != null)
        && (parameters != null)
        && Boolean.TRUE.equals(parameters.getSaveViewImages())) {
      return new AccessibilityHierarchyCheckResultWithImage(
          CHECK_CLASS, type, view, resultId, metadata, viewImage);
    } else {
      return new AccessibilityHierarchyCheckResult(CHECK_CLASS, type, view, resultId, metadata);
    }
  }

  /** Stores extracted or estimated colors and contrast ratios in metadata. */
  private static void storeColorsAndContrastRatios(
      ResultMetadata resultMetadata,
      int background,
      List<Integer> foregroundColors,
      List<Double> contrastRatios) {
    resultMetadata.putInt(KEY_BACKGROUND_COLOR, background);
    storeForegroundColors(resultMetadata, foregroundColors);
    storeContrastRatios(resultMetadata, contrastRatios);
  }

  /**
   * Stores foreground colors in metadata. The first color is stored with the key {@link
   * #KEY_FOREGROUND_COLOR}. If {@code foregroundColors} has more than one value, all but the first
   * value will be stored with the key {@link #KEY_ADDITIONAL_FOREGROUND_COLORS}.
   */
  private static void storeForegroundColors(
      ResultMetadata resultMetadata, List<Integer> foregroundColors) {
    resultMetadata.putInt(KEY_FOREGROUND_COLOR, foregroundColors.get(0));
    if (foregroundColors.size() > 1) {
      resultMetadata.putStringList(
          KEY_ADDITIONAL_FOREGROUND_COLORS,
          Lists.transform(
              foregroundColors.subList(1, foregroundColors.size()), integer -> integer.toString()));
    }
  }

  /**
   * Stores contrast ratios in metadata. The first ratio is stored with the key {@link
   * #KEY_CONTRAST_RATIO}. If {@code contrastRatios} has more than one value, all but the first
   * value will be stored with the key {@link #KEY_ADDITIONAL_CONTRAST_RATIOS}.
   */
  private static void storeContrastRatios(
      ResultMetadata resultMetadata, List<Double> contrastRatios) {
    resultMetadata.putDouble(KEY_CONTRAST_RATIO, contrastRatios.get(0));
    if (contrastRatios.size() > 1) {
      resultMetadata.putStringList(
          KEY_ADDITIONAL_CONTRAST_RATIOS,
          Lists.transform(
              contrastRatios.subList(1, contrastRatios.size()), ratio -> ratio.toString()));
    }
  }

  /**
   * Appends messages for {@link #KEY_IS_POTENTIALLY_OBSCURED} and {@link
   * #KEY_IS_AGAINST_SCROLLABLE_EDGE} to the provided {@code builder} if the relevant keys are set
   * in the given {@code resultMetadata}.
   *
   * @param builder the {@link StringBuilder} to which result messages should be appended
   */
  @SuppressWarnings("unused") // {@code locale} and {@code builder} may not be used
  private static void appendMetadataStringsToMessageIfNeeded(
      Locale locale, ResultMetadata resultMetadata, StringBuilder builder) {
    if (resultMetadata.getBoolean(KEY_IS_POTENTIALLY_OBSCURED, false)) {
      builder
          .append(' ')
          .append(
              StringManager.getString(locale, "result_message_addendum_view_potentially_obscured"));
    }
    if (resultMetadata.getBoolean(KEY_IS_AGAINST_SCROLLABLE_EDGE, false)) {
      builder
          .append(' ')
          .append(
              StringManager.getString(locale, "result_message_addendum_against_scrollable_edge"));
    }
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
      case RESULT_ID_SCREENCAPTURE_UNIFORM_COLOR:
        return StringManager.getString(locale, "result_message_screencapture_uniform_color");
      default:
        return null;
    }
  }
}

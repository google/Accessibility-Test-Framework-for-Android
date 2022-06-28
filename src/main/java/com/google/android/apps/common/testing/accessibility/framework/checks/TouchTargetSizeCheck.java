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

import static com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils.ABS_LIST_VIEW_CLASS_NAME;
import static com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils.WEB_VIEW_CLASS_NAME;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Boolean.TRUE;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.HashMapResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Point;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.DisplayInfo;
import com.google.android.apps.common.testing.accessibility.framework.uielement.DisplayInfo.Metrics;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Check ensuring touch targets have a minimum size, 48x48dp by default
 *
 * <p>This check takes into account and supports:
 *
 * <ul>
 *   <li>Use of {@link android.view.TouchDelegate} to extend the touchable region or hit-Rect of UI
 *       elements
 *   <li>UI elements with interactable ancestors
 *   <li>UI elements along the scrollable edge of containers
 *   <li>Clipping effects applied by ancestors' sizing
 *   <li>Touch targets at the screen edge or within IMEs, requiring a reduced size
 *   <li>Customization of the minimum threshold for required size
 * </ul>
 */
public class TouchTargetSizeCheck extends AccessibilityHierarchyCheck {

  /** Result when the view is not clickable. */
  public static final int RESULT_ID_NOT_CLICKABLE = 1;
  /** Result when the view is not visible. */
  public static final int RESULT_ID_NOT_VISIBLE = 2;
  /** Result when the view's height and width are both too small. */
  public static final int RESULT_ID_SMALL_TOUCH_TARGET_WIDTH_AND_HEIGHT = 3;
  /** Result when the view's height is too small. */
  public static final int RESULT_ID_SMALL_TOUCH_TARGET_HEIGHT = 4;
  /** Result when the view's width is too small. */
  public static final int RESULT_ID_SMALL_TOUCH_TARGET_WIDTH = 5;
  /**
   * Result when the view's height and width are both smaller than the user-defined touch target
   * size.
   */
  public static final int RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_WIDTH_AND_HEIGHT = 6;
  /** Result when the view's height is smaller than the user-defined touch target size. */
  public static final int RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_HEIGHT = 7;
  /** Result when the view's width is smaller than the user-defined touch target size. */
  public static final int RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_WIDTH = 8;

  /**
   * Result metadata key for a {@code boolean} which is {@code true} iff the view has a {@link
   * android.view.TouchDelegate} that may be handling touches on the view's behalf, but that
   * delegate's hit-Rect is not available.
   */
  public static final String KEY_HAS_TOUCH_DELEGATE = "KEY_HAS_TOUCH_DELEGATE";
  /**
   * Result metadata key for a {@code boolean} which is {@code true} iff the view has a {@link
   * android.view.TouchDelegate} with a hit-Rect available. When this key is set to {@code true},
   * {@link #KEY_HIT_RECT_WIDTH} and {@link #KEY_HIT_RECT_HEIGHT} are also provided within the
   * result metadata.
   */
  public static final String KEY_HAS_TOUCH_DELEGATE_WITH_HIT_RECT =
      "KEY_HAS_TOUCH_DELEGATE_WITH_HIT_RECT";
  /**
   * Result metadata key for a {@code boolean} which is {@code true} iff the view has an ancestor
   * (of a suitable size) which may be handling click actions on behalf of the view.
   */
  public static final String KEY_HAS_CLICKABLE_ANCESTOR = "KEY_HAS_CLICKABLE_ANCESTOR";
  /**
   * Result metadata key for a {@code boolean} which is {@code true} iff the view is determined to
   * be touching the scrollable edge of a scrollable container.
   */
  public static final String KEY_IS_AGAINST_SCROLLABLE_EDGE = "KEY_IS_AGAINST_SCROLLABLE_EDGE";
  /**
   * Result metadata key for a {@code boolean} which is {@code true} iff the view has a reduced
   * visible size because it is clipped by a parent view.  When this key is set to {@code true},
   * {@link #KEY_NONCLIPPED_HEIGHT} and {@link #KEY_NONCLIPPED_WIDTH} are also provided within the
   * result metadata.
   */
  public static final String KEY_IS_CLIPPED_BY_ANCESTOR = "KEY_IS_CLIPPED_BY_ANCESTOR";
  /**
   * Result metadata key for a {@code boolean} which is {@code true} when the view is detremined to
   * originate from web content.
   */
  public static final String KEY_IS_WEB_CONTENT = "KEY_IS_WEB_CONTENT";
  /** Result metadata key for the {@code int} height of the view. */
  public static final String KEY_HEIGHT = "KEY_HEIGHT";
  /** Result metadata key for the {@code int} width of the view. */
  public static final String KEY_WIDTH = "KEY_WIDTH";
  /**
   * Result metadata key for the {@code int} height of the view not considering clipping effects
   * applied by parent views. This value is populated only when {@link #KEY_IS_CLIPPED_BY_ANCESTOR}
   * is set to {@code true}.
   */
  public static final String KEY_NONCLIPPED_HEIGHT = "KEY_NONCLIPPED_HEIGHT";
  /**
   * Result metadata key for the {@code int} width of the view not considering clipping effects
   * applied by parent views. This value is populated only when {@link #KEY_IS_CLIPPED_BY_ANCESTOR}
   * is set to {@code true}.
   */
  public static final String KEY_NONCLIPPED_WIDTH = "KEY_NONCLIPPED_WIDTH";
  /** Result metadata key for the {@code int} required height of the view */
  public static final String KEY_REQUIRED_HEIGHT = "KEY_REQUIRED_HEIGHT";
  /** Result metadata key for the {@code int} required width of the view */
  public static final String KEY_REQUIRED_WIDTH = "KEY_REQUIRED_WIDTH";
  /** Result metadata key for the {@code int} user-defined minimum width of the view */
  public static final String KEY_CUSTOMIZED_REQUIRED_WIDTH = "KEY_CUSTOMIZED_REQUIRED_WIDTH";
  /** Result metadata key for the {@code int} user-defined minimum height of the view */
  public static final String KEY_CUSTOMIZED_REQUIRED_HEIGHT = "KEY_CUSTOMIZED_REQUIRED_HEIGHT";
  /**
   * Result metadata key for the {@code int} conveying the width of the largest {@link
   * android.view.TouchDelegate} hit-Rect of the view
   */
  public static final String KEY_HIT_RECT_WIDTH = "KEY_HIT_RECT_WIDTH";
  /**
   * Result metadata key for the {@code int} conveying the height of the largest {@link
   * android.view.TouchDelegate} hit-Rect of the view
   */
  public static final String KEY_HIT_RECT_HEIGHT = "KEY_HIT_RECT_HEIGHT";

  /**
   * Value of android.view.accessibility.AccessibilityWindowInfo.TYPE_INPUT_METHOD. This avoids a
   * dependency upon Android libraries.
   */
  @VisibleForTesting static final int TYPE_INPUT_METHOD = 2;

  /**
   * Minimum height and width are set according to
   * <a href="http://developer.android.com/design/patterns/accessibility.html"></a>
   *
   * With the modification that targets against the edge of the screen may be narrower.
   */
  private static final int TOUCH_TARGET_MIN_HEIGHT = 48;
  private static final int TOUCH_TARGET_MIN_WIDTH = 48;
  private static final int TOUCH_TARGET_MIN_HEIGHT_ON_EDGE = 32;
  private static final int TOUCH_TARGET_MIN_WIDTH_ON_EDGE = 32;
  private static final int TOUCH_TARGET_MIN_HEIGHT_IME_CONTAINER = 32;
  private static final int TOUCH_TARGET_MIN_WIDTH_IME_CONTAINER = 32;

  @Override
  protected String getHelpTopic() {
    return "7101858"; // Touch target size
  }

  @Override
  public Category getCategory() {
    return Category.TOUCH_TARGET_SIZE;
  }

  @Override
  public List<AccessibilityHierarchyCheckResult> runCheckOnHierarchy(
      AccessibilityHierarchy hierarchy,
      @Nullable ViewHierarchyElement fromRoot,
      @Nullable Parameters parameters) {
    List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();

    DisplayInfo defaultDisplay = hierarchy.getDeviceState().getDefaultDisplayInfo();
    DisplayInfo.Metrics metricsWithoutDecorations = defaultDisplay.getMetricsWithoutDecoration();
    List<? extends ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
    for (ViewHierarchyElement view : viewsToEval) {
      if (!(TRUE.equals(view.isClickable())
          || TRUE.equals(view.isLongClickable()))) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_NOT_CLICKABLE,
            null));
        continue;
      }

      if (!TRUE.equals(view.isVisibleToUser())) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_NOT_VISIBLE,
            null));
        continue;
      }

      Rect bounds = view.getBoundsInScreen();
      Point requiredSize = getMinimumAllowableSizeForView(view, parameters);
      float density = metricsWithoutDecorations.getDensity();
      int actualHeight = Math.round(bounds.getHeight() / density);
      int actualWidth = Math.round(bounds.getWidth() / density);

      if (!meetsRequiredSize(bounds, requiredSize, density)) {
        // Before we know a view fails this check, we must check if another View may be handling
        // touches on its behalf. One mechanism for this is a TouchDelegate.
        boolean hasDelegate = false;
        Rect largestDelegateHitRect = null;
        // There are two approaches to detecting such a delegate.  One (on Android Q+) allows us
        // access to the hit-Rect.  Since this is the most precise signal, we try to use this first.
        if (hasTouchDelegateWithHitRects(view)) {
          hasDelegate = true;
          if (hasTouchDelegateOfRequiredSize(view, requiredSize, density)) {
            // Emit no result if a delegate's hit-Rect is above the required size
            continue;
          }
          // If no associated hit-Rect is of the required size, reference the largest one for
          // inclusion in the result message.
          largestDelegateHitRect = getLargestTouchDelegateHitRect(view);
        } else {
          // Without hit-Rects, another approach is to check (View) ancestors for the presence of
          // any TouchDelegate, which indicates that the element may have its hit-Rect adjusted,
          // but does not tell us what its size is.
          hasDelegate = hasAncestorWithTouchDelegate(view);
        }
        // Another approach is to have the parent handle touches for smaller child views, such as a
        // android.widget.Switch, which retains its clickable state for a "handle drag" effect. In
        // these cases, the parent must perform the same action as the child, which is beyond the
        // scope of this test.  We append this important exception message to the result by setting
        // KEY_HAS_CLICKABLE_ANCESTOR within the result metadata.
        boolean hasClickableAncestor = hasQualifyingClickableAncestor(view, parameters);
        // When evaluating a View-based hierarchy, we can check if the visible size of the view is
        // less than the drawing (nonclipped) size, which indicates an ancestor may scroll,
        // expand/collapse, or otherwise constrain the size of the clickable item.
        boolean isClippedByAncestor = hasQualifyingClippingAncestor(view, requiredSize, density);
        // Web content exposed through an AccessibilityNodeInfo-based hierarchy from WebView cannot
        // precisely represent the clickable area for DOM elements in a number of cases. We reduce
        // severity and append a message recommending manual testing when encountering WebView.
        boolean isWebContent = hasWebViewAncestor(view);

        // In each of these cases, with the exception of when we have precise hit-Rect coordinates,
        // we cannot determine how exactly click actions are being handled by the underlying
        // application, so to avoid false positives, we will demote ERROR to WARNING.
        AccessibilityCheckResultType resultType =
            ((hasDelegate && (largestDelegateHitRect == null))
                    || hasClickableAncestor
                    || isClippedByAncestor
                    || isWebContent)
                ? AccessibilityCheckResultType.WARNING
                : AccessibilityCheckResultType.ERROR;

        // We must also detect the case where an item is indicated as a small target because it
        // appears along the scrollable edge of a scrolling container.  In this case, we cannot
        // determine the native nonclipped bounds of the view, so we demote to NOT_RUN.
        boolean isAtScrollableEdge = view.isAgainstScrollableEdge();
        resultType = isAtScrollableEdge ? AccessibilityCheckResultType.NOT_RUN : resultType;

        ResultMetadata resultMetadata = new HashMapResultMetadata();
        resultMetadata.putInt(KEY_HEIGHT, actualHeight);
        resultMetadata.putInt(KEY_WIDTH, actualWidth);
        if (hasDelegate) {
          if (largestDelegateHitRect != null) {
            resultMetadata.putBoolean(KEY_HAS_TOUCH_DELEGATE_WITH_HIT_RECT, true);
            resultMetadata.putInt(
                KEY_HIT_RECT_WIDTH, Math.round(largestDelegateHitRect.getWidth() / density));
            resultMetadata.putInt(
                KEY_HIT_RECT_HEIGHT, Math.round(largestDelegateHitRect.getHeight() / density));
          } else {
            resultMetadata.putBoolean(KEY_HAS_TOUCH_DELEGATE, true);
          }
        }
        if (hasClickableAncestor) {
          resultMetadata.putBoolean(KEY_HAS_CLICKABLE_ANCESTOR, true);
        }
        if (isAtScrollableEdge) {
          resultMetadata.putBoolean(KEY_IS_AGAINST_SCROLLABLE_EDGE, true);
        }
        if (isClippedByAncestor) {
          // If the view is clipped by an ancestor, add the nonclipped dimensions to metadata.
          // The non-clipped height and width cannot be null if isClippedByAncestor is true.
          resultMetadata.putBoolean(KEY_IS_CLIPPED_BY_ANCESTOR, true);
          resultMetadata.putInt(KEY_NONCLIPPED_HEIGHT, checkNotNull(view.getNonclippedHeight()));
          resultMetadata.putInt(KEY_NONCLIPPED_WIDTH, checkNotNull(view.getNonclippedWidth()));
        }
        if (isWebContent) {
          resultMetadata.putBoolean(KEY_IS_WEB_CONTENT, true);
        }

        Integer customizedTouchTargetSize =
            (parameters == null) ? null : parameters.getCustomTouchTargetSize();
        if (customizedTouchTargetSize != null) {
          resultMetadata.putInt(KEY_CUSTOMIZED_REQUIRED_WIDTH, requiredSize.getX());
          resultMetadata.putInt(KEY_CUSTOMIZED_REQUIRED_HEIGHT, requiredSize.getY());
        } else {
          resultMetadata.putInt(KEY_REQUIRED_HEIGHT, requiredSize.getY());
          resultMetadata.putInt(KEY_REQUIRED_WIDTH, requiredSize.getX());
        }

        if ((actualHeight < requiredSize.getY()) && (actualWidth < requiredSize.getX())) {
          // Neither wide enough nor tall enough
          results.add(
              new AccessibilityHierarchyCheckResult(
                  this.getClass(),
                  resultType,
                  view,
                  (customizedTouchTargetSize == null)
                      ? RESULT_ID_SMALL_TOUCH_TARGET_WIDTH_AND_HEIGHT
                      : RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_WIDTH_AND_HEIGHT,
                  resultMetadata));
        } else if (actualHeight < requiredSize.getY()) {
          // Not tall enough
          results.add(
              new AccessibilityHierarchyCheckResult(
                  this.getClass(),
                  resultType,
                  view,
                  (customizedTouchTargetSize == null)
                      ? RESULT_ID_SMALL_TOUCH_TARGET_HEIGHT
                      : RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_HEIGHT,
                  resultMetadata));
        } else {
          // Not wide enough
          results.add(
              new AccessibilityHierarchyCheckResult(
                  this.getClass(),
                  resultType,
                  view,
                  (customizedTouchTargetSize == null)
                      ? RESULT_ID_SMALL_TOUCH_TARGET_WIDTH
                      : RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_WIDTH,
                  resultMetadata));
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
    StringBuilder builder = new StringBuilder();
    int requiredHeight = metadata.getInt(KEY_REQUIRED_HEIGHT, TOUCH_TARGET_MIN_HEIGHT);
    int requiredWidth = metadata.getInt(KEY_REQUIRED_WIDTH, TOUCH_TARGET_MIN_WIDTH);
    switch (resultId) {
      case RESULT_ID_SMALL_TOUCH_TARGET_WIDTH_AND_HEIGHT:
        builder.append(String.format(locale,
            StringManager.getString(locale, "result_message_small_touch_target_width_and_height"),
            metadata.getInt(KEY_WIDTH), metadata.getInt(KEY_HEIGHT), requiredWidth,
            requiredHeight));
        appendMetadataStringsToMessageIfNeeded(locale, metadata, builder);
        return builder.toString();
      case RESULT_ID_SMALL_TOUCH_TARGET_HEIGHT:
        builder.append(String.format(locale,
            StringManager.getString(locale, "result_message_small_touch_target_height"),
            metadata.getInt(KEY_HEIGHT), requiredHeight));
        appendMetadataStringsToMessageIfNeeded(locale, metadata, builder);
        return builder.toString();
      case RESULT_ID_SMALL_TOUCH_TARGET_WIDTH:
        builder.append(String.format(locale,
            StringManager.getString(locale, "result_message_small_touch_target_width"),
            metadata.getInt(KEY_WIDTH), requiredWidth));
        appendMetadataStringsToMessageIfNeeded(locale, metadata, builder);
        return builder.toString();
      case RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_WIDTH_AND_HEIGHT:
        builder.append(
            String.format(
                locale,
                StringManager.getString(
                    locale, "result_message_customized_small_touch_target_width_and_height"),
                metadata.getInt(KEY_WIDTH),
                metadata.getInt(KEY_HEIGHT),
                metadata.getInt(KEY_CUSTOMIZED_REQUIRED_WIDTH),
                metadata.getInt(KEY_CUSTOMIZED_REQUIRED_HEIGHT)));
        appendMetadataStringsToMessageIfNeeded(locale, metadata, builder);
        return builder.toString();
      case RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_HEIGHT:
        builder.append(
            String.format(
                locale,
                StringManager.getString(
                    locale, "result_message_customized_small_touch_target_height"),
                metadata.getInt(KEY_HEIGHT),
                metadata.getInt(KEY_CUSTOMIZED_REQUIRED_HEIGHT)));
        appendMetadataStringsToMessageIfNeeded(locale, metadata, builder);
        return builder.toString();
      case RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_WIDTH:
        builder.append(
            String.format(
                locale,
                StringManager.getString(
                    locale, "result_message_customized_small_touch_target_width"),
                metadata.getInt(KEY_WIDTH),
                metadata.getInt(KEY_CUSTOMIZED_REQUIRED_WIDTH)));
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
      case RESULT_ID_SMALL_TOUCH_TARGET_WIDTH_AND_HEIGHT:
      case RESULT_ID_SMALL_TOUCH_TARGET_HEIGHT:
      case RESULT_ID_SMALL_TOUCH_TARGET_WIDTH:
      case RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_WIDTH_AND_HEIGHT:
      case RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_WIDTH:
      case RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_HEIGHT:
        return StringManager.getString(locale, "result_message_brief_small_touch_target");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  /**
   * Calculates a secondary priority for a touch target result.
   *
   * <p>The primary influence on this priority is the minimum touch target dimension in the result.
   * For example, any result that has a minimum dimension of 2dp (ex. 2dp x 5dp, 45dp x 2dp, or just
   * 2dp wide) should have a greater priority than any result that has a minimum dimension of 3dp
   * (ex. 3dp x 3dp, 36dp x 3dp, or just 3dp high).
   *
   * <p>The secondary influence on this priority is the maximum touch target dimension in the
   * result. If a result only has one dimension, the other is regarded as infinite. For example,
   * among results with a 3dp minimum threshold, 3dp x 3dp would have the highest priority, 3dp x
   * 5dp (or 5dp x 3dp) would be lower, and just 3dp wide (or just 3dp high) would have the lowest
   * priority.
   */

  @Override
  public @Nullable Double getSecondaryPriority(AccessibilityHierarchyCheckResult result) {
    ResultMetadata meta = result.getMetadata();
    if (meta == null) {
      return null;
    }

    int width = meta.getInt(KEY_WIDTH, Integer.MAX_VALUE);
    int height = meta.getInt(KEY_HEIGHT, Integer.MAX_VALUE);
    double primary = Math.min(width, height);
    if (primary == Integer.MAX_VALUE) {
      return null; // Neither width nor height is present.
    }
    // The divisor of 30 delays the exponential expression from reaching its max value.
    double secondary = 1.0 / Math.exp(Math.max(width, height) / 30.0d);
    return -(primary - secondary);
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_touch_target_size");
  }

  private static @Nullable String generateMessageForResultId(Locale locale, int resultId) {
    switch (resultId) {
      case RESULT_ID_NOT_CLICKABLE:
        return StringManager.getString(locale, "result_message_not_clickable");
      case RESULT_ID_NOT_VISIBLE:
        return StringManager.getString(locale, "result_message_not_visible");
      default:
        return null;
    }
  }

  /**
   * Derives the minimum allowable size for the given {@code view} in dp
   *
   * @param view the {@link ViewHierarchyElement} to evaluate
   * @param parameters Optional check input parameters
   * @return a {@link Point} representing the minimum allowable size for {@code view} in dp units
   */
  private static Point getMinimumAllowableSizeForView(
      ViewHierarchyElement view, @Nullable Parameters parameters) {
    Rect bounds = view.getBoundsInScreen();
    Metrics realMetrics = view.getWindow().getAccessibilityHierarchy().getDeviceState()
        .getDefaultDisplayInfo().getRealMetrics();

    final int touchTargetMinWidth;
    final int touchTargetMinHeight;
    final int touchTargetMinWidthImeContainer;
    final int touchTargetMinHeightImeContainer;
    final int touchTargetMinWidthOnEdge;
    final int touchTargetMinHeightOnEdge;
    Integer customizedTargetSize =
        (parameters == null) ? null : parameters.getCustomTouchTargetSize();
    if (customizedTargetSize != null) {
      float targetSize = (float) customizedTargetSize;
      touchTargetMinWidth = customizedTargetSize;
      touchTargetMinHeight = customizedTargetSize;
      touchTargetMinHeightImeContainer =
          Math.round(TOUCH_TARGET_MIN_HEIGHT_IME_CONTAINER * targetSize / TOUCH_TARGET_MIN_HEIGHT);
      touchTargetMinWidthImeContainer =
          Math.round(TOUCH_TARGET_MIN_WIDTH_IME_CONTAINER * targetSize / TOUCH_TARGET_MIN_WIDTH);
      touchTargetMinHeightOnEdge =
          Math.round(TOUCH_TARGET_MIN_HEIGHT_ON_EDGE * targetSize / TOUCH_TARGET_MIN_HEIGHT);
      touchTargetMinWidthOnEdge =
          Math.round(TOUCH_TARGET_MIN_WIDTH_ON_EDGE * targetSize / TOUCH_TARGET_MIN_WIDTH);
    } else {
      touchTargetMinWidth = TOUCH_TARGET_MIN_WIDTH;
      touchTargetMinHeight = TOUCH_TARGET_MIN_HEIGHT;
      touchTargetMinHeightImeContainer = TOUCH_TARGET_MIN_HEIGHT_IME_CONTAINER;
      touchTargetMinWidthImeContainer = TOUCH_TARGET_MIN_WIDTH_IME_CONTAINER;
      touchTargetMinHeightOnEdge = TOUCH_TARGET_MIN_HEIGHT_ON_EDGE;
      touchTargetMinWidthOnEdge = TOUCH_TARGET_MIN_WIDTH_ON_EDGE;
    }

    final int requiredWidth;
    final int requiredHeight;
    Integer windowType = view.getWindow().getType();
    if ((windowType != null) && (windowType == TYPE_INPUT_METHOD)) {
      // Contents of input method windows may be smaller
      requiredWidth = touchTargetMinWidthImeContainer;
      requiredHeight = touchTargetMinHeightImeContainer;
    } else if (realMetrics != null) { // JB MR1 and above
      // Views against the edge of the screen may be smaller in the neighboring dimension
      boolean viewAgainstSide =
          (bounds.getLeft() == 0) || (bounds.getRight() == realMetrics.getWidthPixels());
      boolean viewAgainstTopOrBottom =
          (bounds.getTop() == 0) || (bounds.getBottom() == realMetrics.getHeightPixels());

      requiredWidth = viewAgainstSide ? touchTargetMinWidthOnEdge : touchTargetMinWidth;
      requiredHeight = viewAgainstTopOrBottom ? touchTargetMinHeightOnEdge : touchTargetMinHeight;
    } else {
      // Before JB MR1, we can't get the real size of the screen and thus can't be sure that a
      // view is against an edge. In that case, we only enforce that the view is above the most
      // lenient threshold.
      requiredWidth = Math.min(touchTargetMinWidthOnEdge, touchTargetMinWidth);
      requiredHeight = Math.min(touchTargetMinHeightOnEdge, touchTargetMinHeight);
    }

    return new Point(requiredWidth, requiredHeight);
  }

  /**
   * Determines if {@code boundingRectInPx} is at least as large in both dimensions as the size
   * denoted by {@code requiredSizeInDp}. Handles conversion between px and dp based on {@code
   * density}, rounding the result of such conversion.
   */
  private static boolean meetsRequiredSize(
      Rect boundingRectInPx, Point requiredSizeInDp, float density) {
    return (Math.round(boundingRectInPx.getWidth() / density) >= requiredSizeInDp.getX())
        && (Math.round(boundingRectInPx.getHeight() / density) >= requiredSizeInDp.getY());
  }

  /**
   * Returns {@code true} if {@code view} has a {@link android.view.TouchDelegate} with hit-Rects of
   * a known size, {@code false} otherwise
   */
  private static boolean hasTouchDelegateWithHitRects(ViewHierarchyElement view) {
    return !view.getTouchDelegateBounds().isEmpty();
  }

  /**
   * Determines if any of the {@link android.view.TouchDelegate} hit-Rects delegated to {@code view}
   * meet the required size represented by {@code requiredSizeInDp}
   */
  private static boolean hasTouchDelegateOfRequiredSize(
      ViewHierarchyElement view, Point requiredSizeInDp, float density) {
    for (Rect hitRect : view.getTouchDelegateBounds()) {
      if (meetsRequiredSize(hitRect, requiredSizeInDp, density)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the largest hit-Rect (by area) in screen coordinates (px units) associated with {@code
   * view}, or {@code null} if no hit-Rects are used
   */
  private static @Nullable Rect getLargestTouchDelegateHitRect(ViewHierarchyElement view) {
    int largestArea = -1;
    Rect largestHitRect = null;
    for (Rect hitRect : view.getTouchDelegateBounds()) {
      int area = hitRect.getWidth() * hitRect.getHeight();
      if (area > largestArea) {
        largestArea = area;
        largestHitRect = hitRect;
      }
    }
    return largestHitRect;
  }

  /**
   * Determines if any view in the hierarchy above the provided {@code view} has a {@link
   * android.view.TouchDelegate} set.
   *
   * @param view the {@link ViewHierarchyElement} to evaluate
   * @return {@code true} if an ancestor has a {@link android.view.TouchDelegate} set, {@code false}
   * if no delegate is set or if this could not be determined.
   */
  private static boolean hasAncestorWithTouchDelegate(ViewHierarchyElement view) {
    for (ViewHierarchyElement evalView = view.getParentView(); evalView != null;
        evalView = evalView.getParentView()) {
      if (TRUE.equals(evalView.hasTouchDelegate())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determines if any view in the hierarchy above the provided {@code view} matches {@code view}'s
   * clickability and meets its minimum allowable size.
   *
   * @param view the {@link ViewHierarchyElement} to evaluate
   * @param parameters Optional check input parameters
   * @return {@code true} if any view in {@code view}'s ancestry that is clickable and/or
   *     long-clickable and meets its minimum allowable size.
   */
  private static boolean hasQualifyingClickableAncestor(
      ViewHierarchyElement view, @Nullable Parameters parameters) {
    boolean isTargetClickable = TRUE.equals(view.isClickable());
    boolean isTargetLongClickable = TRUE.equals(view.isLongClickable());
    ViewHierarchyElement evalView = view.getParentView();

    while (evalView != null) {
      if ((TRUE.equals(evalView.isClickable()) && isTargetClickable)
          || (TRUE.equals(evalView.isLongClickable()) && isTargetLongClickable)) {
        Point requiredSize = getMinimumAllowableSizeForView(evalView, parameters);
        Rect bounds = evalView.getBoundsInScreen();
        if (!evalView.checkInstanceOf(ABS_LIST_VIEW_CLASS_NAME)
            && (bounds.getHeight() >= requiredSize.getY())
            && (bounds.getWidth() >= requiredSize.getX())) {
          return true;
        }
      }
      evalView = evalView.getParentView();
    }
    return false;
  }

  /**
   * Determines if the provided {@code view} is possibly clipped by one of its ancestor views in
   * such a way that it may be sufficiently sized if the view were not clipped.
   *
   * @param view the {@link ViewHierarchyElement} to evaluate
   * @param requiredSize a {@link Point} representing the minimum required size of {@code view}
   * @param density the display density
   * @return {@code true} if {@code view}'s size is reduced due to the size of one of its ancestor
   * views, or {@code false} if it is not or this could not be determined.
   */
  private static boolean hasQualifyingClippingAncestor(ViewHierarchyElement view,
      Point requiredSize, float density) {
    Integer rawNonclippedHeight = view.getNonclippedHeight();
    Integer rawNonclippedWidth = view.getNonclippedWidth();
    if ((rawNonclippedHeight == null) || (rawNonclippedWidth == null)) {
      return false;
    }

    Rect clippedBounds = view.getBoundsInScreen();
    int clippedHeight = (int) (clippedBounds.getHeight() / density);
    int clippedWidth = (int) (clippedBounds.getWidth() / density);
    int nonclippedHeight = (int) (rawNonclippedHeight / density);
    int nonclippedWidth = (int) (rawNonclippedWidth / density);
    boolean clippedTooSmallY = clippedHeight < requiredSize.getY();
    boolean clippedTooSmallX = clippedWidth < requiredSize.getX();
    boolean nonclippedTooSmallY = nonclippedHeight < requiredSize.getY();
    boolean nonclippedTooSmallX = nonclippedWidth < requiredSize.getX();

    return (clippedTooSmallY && !nonclippedTooSmallY) || (clippedTooSmallX && !nonclippedTooSmallX);
  }

  /**
   * Identifies web content by checking the ancestors of {@code view} for elements which are WebView
   * containers.
   *
   * @param view the {@link ViewHierarchyElement} to evaluate
   * @return {@code true} if {@code WebView} was identified as an ancestor, {@code false} otherwise
   */
  private static boolean hasWebViewAncestor(ViewHierarchyElement view) {
    ViewHierarchyElement parent = view.getParentView();
    return (parent != null)
        && (parent.checkInstanceOf(WEB_VIEW_CLASS_NAME) || hasWebViewAncestor(parent));
  }

  /**
   * Appends result messages for additional metadata fields to the provided {@code builder} if the
   * relevant keys are set in the given {@code resultMetadata}.
   *
   * @param resultMetadata the metadata for the result which should be evaluated
   * @param builder the {@link StringBuilder} to which result messages should be appended
   */
  private static void appendMetadataStringsToMessageIfNeeded(
      Locale locale, ResultMetadata resultMetadata, StringBuilder builder) {
    boolean hasDelegate = resultMetadata.getBoolean(KEY_HAS_TOUCH_DELEGATE, false);
    boolean hasDelegateWithHitRect =
        resultMetadata.getBoolean(KEY_HAS_TOUCH_DELEGATE_WITH_HIT_RECT, false);
    boolean hasClickableAncestor = resultMetadata.getBoolean(KEY_HAS_CLICKABLE_ANCESTOR, false);
    boolean isClippedByAncestor = resultMetadata.getBoolean(KEY_IS_CLIPPED_BY_ANCESTOR, false);
    boolean isAgainstScrollableEdge =
        resultMetadata.getBoolean(KEY_IS_AGAINST_SCROLLABLE_EDGE, false);
    boolean isWebContent = resultMetadata.getBoolean(KEY_IS_WEB_CONTENT, false);

    if (hasDelegateWithHitRect) {
      builder
          .append(' ')
          .append(
              String.format(
                  locale,
                  StringManager.getString(
                      locale, "result_message_addendum_touch_delegate_with_hit_rect"),
                  resultMetadata.getInt(KEY_HIT_RECT_WIDTH),
                  resultMetadata.getInt(KEY_HIT_RECT_HEIGHT)));
    } else if (hasDelegate) {
      builder.append(' ')
          .append(StringManager.getString(locale, "result_message_addendum_touch_delegate"));
    }
    if (isWebContent) {
      builder.append(' ')
          .append(StringManager.getString(locale, "result_message_addendum_web_touch_target_size"));
    } else if (hasClickableAncestor) {
      // The Web content addendum should supersede more-generic ancestor clickability information
      builder
          .append(' ')
          .append(StringManager.getString(locale, "result_message_addendum_clickable_ancestor"));
    }
    if (isClippedByAncestor) {
      builder.append(' ').append(String.format(locale,
          StringManager.getString(locale, "result_message_addendum_clipped_by_ancestor"),
          resultMetadata.getInt(KEY_NONCLIPPED_WIDTH),
          resultMetadata.getInt(KEY_NONCLIPPED_HEIGHT)));
    }
    if (isAgainstScrollableEdge) {
      builder
          .append(' ')
          .append(
              StringManager.getString(locale, "result_message_addendum_against_scrollable_edge"));
    }
  }
}

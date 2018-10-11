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
import static java.lang.Boolean.TRUE;

import androidx.annotation.Nullable;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.AbsListView;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckMetadata;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Metadata;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Point;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.DisplayInfo;
import com.google.android.apps.common.testing.accessibility.framework.uielement.DisplayInfo.Metrics;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Check to ensure that a view has a touch target that is at least 48x48dp.
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
   * Result metadata key for a {@code boolean} which is {@code true} iff the view has a
   * {@link android.view.TouchDelegate} that may be handling touches on the view's behalf.
   */
  public static final String KEY_HAS_TOUCH_DELEGATE = "KEY_HAS_TOUCH_DELEGATE";
  /**
   * Result metadata key for a {@code boolean} which is {@code true} iff the view has an ancestor
   * (of a suitable size) which may be handling click actions on behalf of the view.
   */
  public static final String KEY_HAS_CLICKABLE_ANCESTOR = "KEY_HAS_CLICKABLE_ANCESTOR";
  /**
   * Result metadata key for a {@code boolean} which is {@code true} iff the view is determined to
   * be touching the scrollable edge of a scrollable container.
   */
  public static final String KEY_IS_AGAINST_SCROLLABLE_EDGE = "KEY_IS_AGAIST_SCROLLABLE_EDGE";
  /**
   * Result metadata key for a {@code boolean} which is {@code true} iff the view has a reduced
   * visible size because it is clipped by a parent view.  When this key is set to {@code true},
   * {@link #KEY_NONCLIPPED_HEIGHT} and {@link #KEY_NONCLIPPED_WIDTH} are also provided within the
   * result metadata.
   */
  public static final String KEY_IS_CLIPPED_BY_ANCESTOR = "KEY_IS_CLIPPED_BY_ANCESTOR";
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
      @Nullable Metadata metadata) {
    List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();

    DisplayInfo defaultDisplay = hierarchy.getDeviceState().getDefaultDisplayInfo();
    DisplayInfo.Metrics metricsWithoutDecorations = defaultDisplay.getMetricsWithoutDecoration();

    // Obtain a list of elements which are adjacent to a scrollable edge of a scrollable container
    Set<ViewHierarchyElement> edgeScrollElements = new HashSet<>();
    ViewHierarchyElement rootView = hierarchy.getActiveWindow().getRootView();
    if (rootView != null) {
      populateSetWithExemptedEdgeScrollEdgeElements(rootView, edgeScrollElements);
    }

    List<ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
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

      if (!(TRUE.equals(view.isVisibleToUser()))) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_NOT_VISIBLE,
            null));
        continue;
      }

      // dp calculation is pixels/density
      Rect bounds = view.getBoundsInScreen();
      float density = metricsWithoutDecorations.getDensity();
      int actualHeight = Math.round(bounds.getHeight() / density);
      int actualWidth = Math.round(bounds.getWidth() / density);
      Point requiredSize = getMinimumAllowableSizeForView(view, metadata);

      if (actualHeight < requiredSize.getY() || actualWidth < requiredSize.getX()) {
        // Before we know a view fails this check, we must check if one of the view's ancestors may
        // be handling touches on its behalf. One mechanism for this is presence of a TouchDelegate.
        boolean hasDelegate = hasAncestorWithTouchDelegate(view);
        // Another approach is to have the parent handle touches for smaller child views, such as a
        // android.widget.Switch, which retains its clickable state for a "handle drag" effect. In
        // these cases, the parent must perform the same action as the child, which is beyond the
        // scope of this test.  We append this important exception message to the result by setting
        // KEY_HAS_CLICKABLE_ANCESTOR within the result metadata.
        boolean hasClickableAncestor = hasQualifyingClickableAncestor(view, metadata);
        // When evaluating a View-based hierarchy, we can check if the visible size of the view is
        // less than the drawing (nonclipped) size, which indicates an ancestor may scroll,
        // expand/collapse, or otherwise constrain the size of the clickable item.
        boolean isClippedByAncestor = hasQualifyingClippingAncestor(view, requiredSize, density);

        // In each of these cases, we cannot determine how exactly click actions are being handled
        // by the underlying application, but to avoid false positives, we will demote ERROR to
        // WARNING.
        AccessibilityCheckResultType resultType =
            (hasDelegate || hasClickableAncestor || isClippedByAncestor)
                ? AccessibilityCheckResultType.WARNING : AccessibilityCheckResultType.ERROR;

        // We must also detect the case where an item is indicated as a small target because it
        // appears along the scrollable edge of a scrolling container.  In this case, we cannot
        // determine the native nonclipped bounds of the view, so we demote to NOT_RUN.
        boolean isAtScrollableEdge = edgeScrollElements.contains(view);
        resultType = (isAtScrollableEdge) ? AccessibilityCheckResultType.NOT_RUN : resultType;

        Metadata resultMetadata = new Metadata();
        resultMetadata.putInt(KEY_HEIGHT, actualHeight);
        resultMetadata.putInt(KEY_WIDTH, actualWidth);
        resultMetadata.putBoolean(KEY_HAS_TOUCH_DELEGATE, hasDelegate);
        resultMetadata.putBoolean(KEY_HAS_CLICKABLE_ANCESTOR, hasClickableAncestor);
        resultMetadata.putBoolean(KEY_IS_CLIPPED_BY_ANCESTOR, isClippedByAncestor);
        resultMetadata.putBoolean(KEY_IS_AGAINST_SCROLLABLE_EDGE, isAtScrollableEdge);
        if (isClippedByAncestor) {
          // If the view is clipped by an ancestor, add the nonclipped dimensions to metadata.
          // The non-clipped height and width cannot be null if isClippedByAncestor is true.
          resultMetadata.putInt(KEY_NONCLIPPED_HEIGHT, checkNotNull(view.getNonclippedHeight()));
          resultMetadata.putInt(KEY_NONCLIPPED_WIDTH, checkNotNull(view.getNonclippedWidth()));
        }

        Integer customizedTouchTargetSize =
            AccessibilityCheckMetadata.getCustomizedTouchTargetSizeInMetadata(metadata);
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
    switch(resultId) {
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

    switch(resultId) {
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

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_touch_target_size");
  }

  private static @Nullable String generateMessageForResultId(Locale locale, int resultId) {
    switch(resultId) {
      case RESULT_ID_NOT_CLICKABLE:
        return StringManager.getString(locale, "result_message_not_clickable");
      case RESULT_ID_NOT_VISIBLE:
        return StringManager.getString(locale, "result_message_not_visible");
      default:
        return null;
    }
  }

  /**
   * Derives the minimum allowable size for the given {@code view}
   *
   * @param view the {@link ViewHierarchyElement} to evaluate
   * @param metadata An optional {@link Metadata} that may contain check metadata defined by {@link
   *     AccessibilityCheckMetadata}.
   * @return a {@link Point} representing the minimum allowable size for {@code view}
   */
  private static Point getMinimumAllowableSizeForView(
      ViewHierarchyElement view, @Nullable Metadata metadata) {
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
        AccessibilityCheckMetadata.getCustomizedTouchTargetSizeInMetadata(metadata);
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
    if ((windowType != null) && (windowType == AccessibilityWindowInfo.TYPE_INPUT_METHOD)) {
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
   * Populates the provided {@code setToPopulate} with {@link ViewHierarchyElement}s which may be
   * positioned against the scrollable edge of a scrollable container.  This is useful for
   * determining elements that may fall partially "below the fold" of such a container.
   *
   * @param root the {@link ViewHierarchyElement} from which to evaluate
   * @param setToPopulate the {@link Set} to which identified elements should be added
   */
  private static void populateSetWithExemptedEdgeScrollEdgeElements(ViewHierarchyElement root,
      Set<ViewHierarchyElement> setToPopulate) {
    if (root == null) {
      return;
    }

    // Identify scrollable containers that can be scrolled backward
    if (TRUE.equals(root.canScrollBackward())) {
      Rect scrollableBounds = root.getBoundsInScreen();

      // Locate the first visible child
      ViewHierarchyElement firstVisibleChild = null;
      for (int i = 0; i < root.getChildViewCount(); ++i) {
        ViewHierarchyElement child = root.getChildView(i);
        if (TRUE.equals(child.isVisibleToUser())) {
          firstVisibleChild = child;
          break;
        }
      }

      // Identify items in this sub-hierarchy which are touching one of the potentially scrollable
      // container edges.
      List<ViewHierarchyElement> backwardEdgeChildren =
          (firstVisibleChild != null) ? firstVisibleChild.getSelfAndAllDescendants()
              : new ArrayList<ViewHierarchyElement>(0);
      for (ViewHierarchyElement edgeChild : backwardEdgeChildren) {
        Rect childBounds = edgeChild.getBoundsInScreen();
        if ((childBounds.getTop() <= scrollableBounds.getTop())
            || (childBounds.getLeft() <= scrollableBounds.getLeft())) {
          setToPopulate.add(edgeChild);
        }
      }
    }

    // Perform the same analysis for scrollable containers that can be scrolled forward
    if (TRUE.equals(root.canScrollForward())) {
      Rect scrollableBounds = root.getBoundsInScreen();
      ViewHierarchyElement lastVisibleChild = null;
      for (int i = (root.getChildViewCount() - 1); i >= 0; --i) {
        ViewHierarchyElement child = root.getChildView(i);
        if (TRUE.equals(child.isVisibleToUser())) {
          lastVisibleChild = child;
          break;
        }
      }
      List<ViewHierarchyElement> forwardEdgeChildren =
          (lastVisibleChild != null) ? lastVisibleChild.getSelfAndAllDescendants()
              : new ArrayList<ViewHierarchyElement>(0);
      for (ViewHierarchyElement edgeChild : forwardEdgeChildren) {
        Rect childBounds = edgeChild.getBoundsInScreen();
        if ((childBounds.getBottom() >= scrollableBounds.getBottom())
            || (childBounds.getRight() >= scrollableBounds.getRight())) {
          setToPopulate.add(edgeChild);
        }
      }
    }

    // Recurse all children
    for (int i = 0; i < root.getChildViewCount(); ++i) {
      populateSetWithExemptedEdgeScrollEdgeElements(root.getChildView(i), setToPopulate);
    }
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
   * @param metadata An optional {@link Metadata} that may contain check metadata defined by {@link
   *     AccessibilityCheckMetadata}.
   * @return {@code true} if any view in {@code view}'s ancestry that is clickable and/or
   *     long-clickable and meets its minimum allowable size.
   */
  private static boolean hasQualifyingClickableAncestor(
      ViewHierarchyElement view, @Nullable Metadata metadata) {
    boolean isTargetClickable = TRUE.equals(view.isClickable());
    boolean isTargetLongClickable = TRUE.equals(view.isLongClickable());
    ViewHierarchyElement evalView = view.getParentView();

    while (evalView != null) {
      if ((TRUE.equals(evalView.isClickable()) && isTargetClickable)
          || (TRUE.equals(evalView.isLongClickable()) && isTargetLongClickable)) {
        Point requiredSize = getMinimumAllowableSizeForView(evalView, metadata);
        Rect bounds = evalView.getBoundsInScreen();
        if (!(TRUE.equals(evalView.checkInstanceOf(AbsListView.class)))
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
   * Appends result messages for {@link #KEY_HAS_TOUCH_DELEGATE} and
   * {@link #KEY_HAS_CLICKABLE_ANCESTOR} to the provided {@code builder} if the relevant keys are
   * set in the given {@code resultMetadata}.
   *
   * @param builder the {@link StringBuilder} to which result messages should be appended
   */
  private static void appendMetadataStringsToMessageIfNeeded(
      Locale locale, ResultMetadata resultMetadata, StringBuilder builder) {
    boolean hasDelegate = resultMetadata.getBoolean(KEY_HAS_TOUCH_DELEGATE, false);
    boolean hasClickableAncestor = resultMetadata.getBoolean(KEY_HAS_CLICKABLE_ANCESTOR, false);
    boolean isClippedByAncestor = resultMetadata.getBoolean(KEY_IS_CLIPPED_BY_ANCESTOR, false);
    boolean isAgainstScrollableEdge =
        resultMetadata.getBoolean(KEY_IS_AGAINST_SCROLLABLE_EDGE, false);

    if (hasDelegate) {
      builder.append(' ')
          .append(StringManager.getString(locale, "result_message_addendum_touch_delegate"));
    }
    if (hasClickableAncestor) {
      builder.append(' ')
          .append(StringManager.getString(locale, "result_message_addendum_clickable_ancestor"));
    }
    if (isClippedByAncestor) {
      builder.append(' ').append(String.format(locale,
          StringManager.getString(locale, "result_message_addendum_clipped_by_ancestor"),
          resultMetadata.getInt(KEY_NONCLIPPED_WIDTH),
          resultMetadata.getInt(KEY_NONCLIPPED_HEIGHT)));
    }
    if (isAgainstScrollableEdge) {
      builder.append(' ')
          .append(StringManager.getString(
              locale, "result_message_addendum_against_scrollable_edge"));
    }
  }
}

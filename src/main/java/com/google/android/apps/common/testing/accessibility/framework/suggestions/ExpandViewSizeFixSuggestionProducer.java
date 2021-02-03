package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck;
import com.google.android.apps.common.testing.accessibility.framework.replacements.LayoutParams;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.DisplayInfo;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Produces a {@link FixSuggestion} which suggests expanding the culprit View's size to fix a small
 * touch target size issue.
 *
 * <p>The produced fix suggestion can either be a {@link SetViewAttributeFixSuggestion} if only one
 * {@link ViewAttribute} suggested to be changed, or a {@link CompoundFixSuggestions} if multiple
 * {@link ViewAttribute} suggested to be changed. No fix suggestions will be produced if it is not
 * possible to fix the touch target size issue by expanding the culprit View's size.
 */
class ExpandViewSizeFixSuggestionProducer implements FixSuggestionProducer<FixSuggestion> {

  private static final String VIEW_ATTRIBUTE_MIN_WIDTH = "minWidth";
  private static final String VIEW_ATTRIBUTE_MIN_HEIGHT = "minHeight";
  private static final String VIEW_ATTRIBUTE_LAYOUT_WIDTH = "layout_width";
  private static final String VIEW_ATTRIBUTE_LAYOUT_HEIGHT = "layout_height";

  @Override
  public @Nullable FixSuggestion produceFixSuggestion(
      AccessibilityHierarchyCheckResult checkResult,
      AccessibilityHierarchy hierarchy,
      @Nullable Parameters parameters) {
    ViewHierarchyElement element = checkResult.getElement();
    ResultMetadata resultMetadata = checkResult.getMetadata();
    if ((element == null) || (resultMetadata == null)) {
      return null;
    }

    switch (checkResult.getResultId()) {
      case TouchTargetSizeCheck.RESULT_ID_SMALL_TOUCH_TARGET_WIDTH:
      case TouchTargetSizeCheck.RESULT_ID_SMALL_TOUCH_TARGET_HEIGHT:
      case TouchTargetSizeCheck.RESULT_ID_SMALL_TOUCH_TARGET_WIDTH_AND_HEIGHT:
      case TouchTargetSizeCheck.RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_WIDTH:
      case TouchTargetSizeCheck.RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_HEIGHT:
      case TouchTargetSizeCheck.RESULT_ID_CUSTOMIZED_SMALL_TOUCH_TARGET_WIDTH_AND_HEIGHT:
        return produceExpandingViewSizeSuggestion(element, resultMetadata, hierarchy);
      default:
        return null;
    }
  }

  private static @Nullable FixSuggestion produceExpandingViewSizeSuggestion(
      ViewHierarchyElement element,
      ResultMetadata resultMetadata,
      AccessibilityHierarchy hierarchy) {
    // Do not produce a fix suggestion if the layout params of the culprit View are not available
    LayoutParams layoutParams = element.getLayoutParams();
    if (layoutParams == null) {
      return null;
    }

    // Do not produce a fix suggestion if an {@link TouchDelegate} has been set for the culprit View
    if (resultMetadata.getBoolean(
            TouchTargetSizeCheck.KEY_HAS_TOUCH_DELEGATE, /* defaultValue= */ false)
        || resultMetadata.getBoolean(
            TouchTargetSizeCheck.KEY_HAS_TOUCH_DELEGATE_WITH_HIT_RECT, /* defaultValue= */ false)) {
      return null;
    }

    // Do not produce a fix suggestion if the culprit View is clipped by any ancestor Views
    if (resultMetadata.getBoolean(
        TouchTargetSizeCheck.KEY_IS_CLIPPED_BY_ANCESTOR, /* defaultValue= */ false)) {
      return null;
    }

    // Do not produce a fix suggestion if the size of the culprit View's parent View does not meet
    // the width and height requirement
    if (!isParentViewBigEnoughForExpanding(hierarchy, element, resultMetadata)) {
      return null;
    }

    return createFixSuggestion(resultMetadata, layoutParams);
  }

  private static @Nullable FixSuggestion createFixSuggestion(
      ResultMetadata resultMetadata, LayoutParams layoutParams) {
    int widthInDp = getWidth(resultMetadata);
    int heightInDp = getHeight(resultMetadata);
    int requiredWidthInDp = getRequiredWidth(resultMetadata);
    int requiredHeightInDp = getRequiredHeight(resultMetadata);

    // When layout params for width or height is WRAP_CONTENT, it is better to suggest setting the
    // {@code minWidth} attribute or the {@code minHeight} attribute because the text in the culprit
    // View could be truncated with large system font when setting fixed layout_width value or
    // layout_height value.
    ImmutableList.Builder<FixSuggestion> fixSuggestionBuilder = ImmutableList.builder();
    if (widthInDp < requiredWidthInDp) {
      int layoutWidth = layoutParams.getWidth();
      if (layoutWidth == LayoutParams.WRAP_CONTENT) {
        fixSuggestionBuilder.add(
            new SetViewAttributeFixSuggestion(VIEW_ATTRIBUTE_MIN_WIDTH, requiredWidthInDp + "dp"));
      } else if (layoutWidth > 0) {
        fixSuggestionBuilder.add(
            new SetViewAttributeFixSuggestion(
                VIEW_ATTRIBUTE_LAYOUT_WIDTH, requiredWidthInDp + "dp"));
      }
      // The layout width won't be MATCH_PARENT because we have checked the parent view's size meets
      // both width and height requirement.
      // When the layout width is 0, it means the width of the culprit view is calculated by its
      // constraints. No fix suggestions will be produced if the layout width is 0.
    }
    if (heightInDp < requiredHeightInDp) {
      int layoutHeight = layoutParams.getHeight();
      if (layoutHeight == LayoutParams.WRAP_CONTENT) {
        fixSuggestionBuilder.add(
            new SetViewAttributeFixSuggestion(
                VIEW_ATTRIBUTE_MIN_HEIGHT, requiredHeightInDp + "dp"));
      } else if (layoutHeight > 0) {
        fixSuggestionBuilder.add(
            new SetViewAttributeFixSuggestion(
                VIEW_ATTRIBUTE_LAYOUT_HEIGHT, requiredHeightInDp + "dp"));
      }
    }

    ImmutableList<FixSuggestion> fixSuggestions = fixSuggestionBuilder.build();
    if (fixSuggestions.isEmpty()) {
      return null;
    } else if (fixSuggestions.size() == 1) {
      /**
       * If both the width and the height of the culprit View does not meet the minimum touch target
       * size requirement, the layout width is {@link LayoutParams#WRAP_CONTENT} and the layout
       * height is 0 (calculated by constraints), {@link ExpandViewSizeFixSuggestionProducer} will
       * produce a {@link SetViewAttributeFixSuggestion}. But even when this {@link
       * SetViewAttributeFixSuggestion} has been applied, the height of the culprit View still won't
       * meet the requirement.
       */
      return fixSuggestions.get(0);
    } else {
      return new CompoundFixSuggestions(fixSuggestions);
    }
  }

  /**
   * Returns {@code true} if the size of culprit View's parent View meets the width and height
   * requirement.
   */
  private static boolean isParentViewBigEnoughForExpanding(
      AccessibilityHierarchy hierarchy, ViewHierarchyElement view, ResultMetadata resultMetadata) {
    ViewHierarchyElement parentView = view.getParentView();
    if (parentView == null) {
      // Do not produce a fix suggestion if the culprit View has no parent view
      return false;
    }

    int requiredWidthInDp = getRequiredWidth(resultMetadata);
    int requiredHeightInDp = getRequiredHeight(resultMetadata);
    DisplayInfo defaultDisplay = hierarchy.getDeviceState().getDefaultDisplayInfo();
    float density = defaultDisplay.getMetricsWithoutDecoration().getDensity();
    Rect parentViewBoundsInScreen = parentView.getBoundsInScreen();
    return (Math.round(parentViewBoundsInScreen.getWidth() / density) >= requiredWidthInDp)
        && (Math.round(parentViewBoundsInScreen.getHeight() / density) >= requiredHeightInDp);
  }

  private static int getWidth(ResultMetadata resultMetadata) {
    return resultMetadata.getInt(TouchTargetSizeCheck.KEY_WIDTH);
  }

  private static int getHeight(ResultMetadata resultMetadata) {
    return resultMetadata.getInt(TouchTargetSizeCheck.KEY_HEIGHT);
  }

  private static int getRequiredWidth(ResultMetadata resultMetadata) {
    if (resultMetadata.containsKey(TouchTargetSizeCheck.KEY_CUSTOMIZED_REQUIRED_WIDTH)) {
      return resultMetadata.getInt(TouchTargetSizeCheck.KEY_CUSTOMIZED_REQUIRED_WIDTH);
    }
    return resultMetadata.getInt(TouchTargetSizeCheck.KEY_REQUIRED_WIDTH);
  }

  private static int getRequiredHeight(ResultMetadata resultMetadata) {
    if (resultMetadata.containsKey(TouchTargetSizeCheck.KEY_CUSTOMIZED_REQUIRED_HEIGHT)) {
      return resultMetadata.getInt(TouchTargetSizeCheck.KEY_CUSTOMIZED_REQUIRED_HEIGHT);
    }
    return resultMetadata.getInt(TouchTargetSizeCheck.KEY_REQUIRED_HEIGHT);
  }
}

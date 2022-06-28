package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.ContrastUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link FixSuggestionProducer} which produces a {@link SetViewAttributeFixSuggestion} to fix a
 * text low contrast issue.
 *
 * <p>Checks all material colors to find the best color which is the closest color in terms of color
 * distance to the original background color and meets the contrast ratio requirement. If the best
 * color could be found, produces a {@link SetViewAttributeFixSuggestion} which recommends changing
 * background to a suggested value to fix a text low contrast issue. Otherwise no fix suggestions
 * will be produced.
 */
class TextBackgroundColorFixSuggestionProducer extends BaseTextContrastFixSuggestionProducer {

  private static final String VIEW_ATTRIBUTE_BACKGROUND = "background";

  @Override
  protected @Nullable SetViewAttributeFixSuggestion produceTextContrastFixSuggestion(
      int checkResultId, ViewHierarchyElement element, ResultMetadata metadata) {
    switch (checkResultId) {
      case TextContrastCheck.RESULT_ID_TEXTVIEW_CONTRAST_NOT_SUFFICIENT:
        return produceBackgroundColorFixSuggestion(metadata);
      default:
        // For other check result id, no fix suggestions will be produced because it may require
        // to set background color to the culprit view's ancestor view.
        return null;
    }
  }

  private static @Nullable SetViewAttributeFixSuggestion produceBackgroundColorFixSuggestion(
      ResultMetadata resultMetadata) {
    Integer bestColorCandidate =
        findBestBackgroundColorCandidate(
            getTextColor(resultMetadata),
            getBackgroundColor(resultMetadata),
            getRequiredContrastRatio(resultMetadata));
    return (bestColorCandidate == null)
        ? null
        : createSetViewAttributeFixSuggestion(VIEW_ATTRIBUTE_BACKGROUND, bestColorCandidate);
  }

  private static @Nullable Integer findBestBackgroundColorCandidate(
      int textColor, int backgroundColor, double requiredContrastRatio) {
    Integer bestColorCandidate = null;
    double minColorDistance = Double.MAX_VALUE;
    for (MaterialDesignColor designColor : MaterialDesignColor.values()) {
      for (int testColor : designColor.getColorMap().values()) {
        if (ContrastUtils.calculateContrastRatio(textColor, testColor) < requiredContrastRatio) {
          continue;
        }

        double colorDistance = ContrastUtils.colorDifference(testColor, backgroundColor);
        if (minColorDistance > colorDistance) {
          minColorDistance = colorDistance;
          bestColorCandidate = testColor;
        }
      }
    }
    return bestColorCandidate;
  }
}

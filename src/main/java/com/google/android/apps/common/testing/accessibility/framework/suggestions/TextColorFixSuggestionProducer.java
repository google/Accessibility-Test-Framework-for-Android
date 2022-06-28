package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.ContrastUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link FixSuggestionProducer} which produces a {@link SetViewAttributeFixSuggestion} to fix a
 * text low contrast issue.
 *
 * <p>Checks all material colors to find the best color which is the closest color in terms of color
 * distance to the original text color and meets the contrast ratio requirement. If the best color
 * could be found, produces a {@link SetViewAttributeFixSuggestion} which recommends changing text
 * color or hint color to fix a text low contrast issue. Otherwise no fix suggestions will be
 * produced.
 */
class TextColorFixSuggestionProducer extends BaseTextContrastFixSuggestionProducer {

  private static final String VIEW_ATTRIBUTE_TEXT_COLOR = "textColor";
  private static final String VIEW_ATTRIBUTE_TEXT_COLOR_HINT = "textColorHint";

  @Override
  protected @Nullable SetViewAttributeFixSuggestion produceTextContrastFixSuggestion(
      int checkResultId, ViewHierarchyElement element, ResultMetadata metadata) {
    switch (checkResultId) {
      case TextContrastCheck.RESULT_ID_TEXTVIEW_CONTRAST_NOT_SUFFICIENT:
      case TextContrastCheck.RESULT_ID_TEXTVIEW_HEURISTIC_CONTRAST_NOT_SUFFICIENT:
      case TextContrastCheck.RESULT_ID_CUSTOMIZED_TEXTVIEW_HEURISTIC_CONTRAST_NOT_SUFFICIENT:
      case TextContrastCheck.RESULT_ID_TEXTVIEW_HEURISTIC_CONTRAST_BORDERLINE:
        return produceTextColorFixSuggestion(element, metadata);
      default:
        return null;
    }
  }

  private static @Nullable SetViewAttributeFixSuggestion produceTextColorFixSuggestion(
      ViewHierarchyElement element, ResultMetadata resultMetadata) {
    Integer bestColorCandidate =
        findBestTextColorCandidate(
            getTextColor(resultMetadata),
            getBackgroundColor(resultMetadata),
            getRequiredContrastRatio(resultMetadata));

    // If the element's text is empty and a low color contrast ratio issue has been detected, then
    // the element's hint is being displayed with the current hint text color. So we should suggest
    // changing the hint text color instead of the text color in this case.
    String viewAttribute =
        TextUtils.isEmpty(element.getText())
            ? VIEW_ATTRIBUTE_TEXT_COLOR_HINT
            : VIEW_ATTRIBUTE_TEXT_COLOR;
    return (bestColorCandidate == null)
        ? null
        : createSetViewAttributeFixSuggestion(viewAttribute, bestColorCandidate);
  }

  private static @Nullable Integer findBestTextColorCandidate(
      int textColor, int backgroundColor, double requiredContrastRatio) {
    // Tries to find a color in all material colors which meets contrast ratio requirement and is
    // the closest color to the culprit View's text color.
    double minColorDistance = Double.MAX_VALUE;
    Integer bestColorCandidate = null;
    for (MaterialDesignColor designColor : MaterialDesignColor.values()) {
      for (int testColor : designColor.getColorMap().values()) {
        if (ContrastUtils.calculateContrastRatio(testColor, backgroundColor)
            < requiredContrastRatio) {
          continue;
        }

        double colorDistance = ContrastUtils.colorDifference(testColor, textColor);
        if (minColorDistance > colorDistance) {
          minColorDistance = colorDistance;
          bestColorCandidate = testColor;
        }
      }
    }
    return bestColorCandidate;
  }
}

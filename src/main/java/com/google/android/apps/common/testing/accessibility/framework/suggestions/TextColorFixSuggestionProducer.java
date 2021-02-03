package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.ContrastUtils;
import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link FixSuggestionProducer} which produces a {@link SetViewAttributeFixSuggestion} to fix a
 * text low contrast issue.
 *
 * <p>If a similar text color which meets the contrast ratio requirement could be found, produces a
 * {@link SetViewAttributeFixSuggestion} which recommends changing text color to fix a text low
 * contrast issue. Otherwise no fix suggestions will be produced.
 */
class TextColorFixSuggestionProducer extends BaseTextContrastFixSuggestionProducer {

  private static final String VIEW_ATTRIBUTE_TEXT_COLOR = "textColor";
  private static final String VIEW_ATTRIBUTE_HINT_TEXT_COLOR = "hintTextColor";

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
    int textColor = getTextColor(resultMetadata);
    int backgroundColor = getBackgroundColor(resultMetadata);
    double requiredContrastRatio = getRequiredContrastRatio(resultMetadata);
    MaterialDesignColor closestColor = MaterialDesignColor.findClosestColor(textColor);
    Integer bestColorCandidate =
        findBestTextColorCandidate(closestColor, textColor, backgroundColor, requiredContrastRatio);

    // If the element's text is empty and a low color contrast ratio issue has been detected, then
    // the element's hint is being displayed with the current hint text color. So we should suggest
    // changing the hint text color instead of the text color in this case.
    String viewAttribute =
        TextUtils.isEmpty(element.getText())
            ? VIEW_ATTRIBUTE_HINT_TEXT_COLOR
            : VIEW_ATTRIBUTE_TEXT_COLOR;
    return (bestColorCandidate == null)
        ? null
        : createSetViewAttributeFixSuggestion(viewAttribute, bestColorCandidate);
  }

  private static @Nullable Integer findBestTextColorCandidate(
      MaterialDesignColor closestColor,
      int textColor,
      int backgroundColor,
      double requiredContrastRatio) {
    // Always test/suggest white and black regardless of the original color family
    ImmutableSet<Integer> similarColors =
        ImmutableSet.<Integer>builder()
            .add(WHITE_COLOR)
            .addAll(closestColor.getColorMap().values())
            .add(BLACK_COLOR)
            .build();

    // Tries to find a color in similar colors which meets contrast ratio requirement and is the
    // closest color to the culprit View's text color.
    double minColorDistance = Double.MAX_VALUE;
    Integer bestColorCandidate = null;
    for (int color : similarColors) {
      if (ContrastUtils.calculateContrastRatio(color, backgroundColor) >= requiredContrastRatio) {
        double colorDistance = ContrastUtils.colorDifference(color, textColor);
        if (minColorDistance > colorDistance) {
          minColorDistance = colorDistance;
          bestColorCandidate = color;
        }
      }
    }

    return bestColorCandidate;
  }
}


package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.ContrastUtils;
import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link FixSuggestionProducer} which produces a {@link SetViewAttributeFixSuggestion} to fix a
 * text low contrast issue.
 *
 * <p>If a similar background color which meets the contrast ratio requirement could be found,
 * produces a {@link SetViewAttributeFixSuggestion} which recommends changing background to a
 * suggested value to fix a text low contrast issue. Otherwise no fix suggestions will be produced.
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
    int textColor = getTextColor(resultMetadata);
    int backgroundColor = getBackgroundColor(resultMetadata);
    double requiredContrastRatio = getRequiredContrastRatio(resultMetadata);
    MaterialDesignColor closestColor = MaterialDesignColor.findClosestColor(backgroundColor);
    Integer bestColorCandidate =
        findBestBackgroundColorCandidate(
            closestColor, textColor, backgroundColor, requiredContrastRatio);
    return (bestColorCandidate == null)
        ? null
        : createSetViewAttributeFixSuggestion(VIEW_ATTRIBUTE_BACKGROUND, bestColorCandidate);
  }

  private static @Nullable Integer findBestBackgroundColorCandidate(
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
    // closest color to the culprit View's background color.
    double minColorDistance = Double.MAX_VALUE;
    Integer bestColorCandidate = null;
    for (int color : similarColors) {
      if (ContrastUtils.calculateContrastRatio(textColor, color) >= requiredContrastRatio) {
        double colorDistance = ContrastUtils.colorDifference(color, backgroundColor);
        if (minColorDistance > colorDistance) {
          minColorDistance = colorDistance;
          bestColorCandidate = color;
        }
      }
    }

    return bestColorCandidate;
  }
}

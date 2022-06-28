package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.ContrastUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base class which produces a {@link SetViewAttributeFixSuggestion} to fix a text low contrast
 * issue.
 */
abstract class BaseTextContrastFixSuggestionProducer
    implements FixSuggestionProducer<SetViewAttributeFixSuggestion> {

  @Override
  public @Nullable SetViewAttributeFixSuggestion produceFixSuggestion(
      AccessibilityHierarchyCheckResult checkResult,
      AccessibilityHierarchy hierarchy,
      @Nullable Parameters parameters) {
    ResultMetadata metadata = checkResult.getMetadata();
    if (metadata == null) {
      return null;
    }

    ViewHierarchyElement element = checkResult.getElement();
    if (element == null) {
      return null;
    }

    if (metadata.getBoolean(
        TextContrastCheck.KEY_IS_POTENTIALLY_OBSCURED, /* defaultValue= */ false)) {
      // Do not produce a fix suggestion if the culprit view may be obscured by other on-screen
      // content.
      return null;
    }

    return produceTextContrastFixSuggestion(checkResult.getResultId(), element, metadata);
  }

  /**
   * Produces a {@link SetViewAttributeFixSuggestion} to fix a text low contrast issue. Returns
   * {@code null} if no similar colors meet the text contrast ratio requirement.
   *
   * @param checkResultId the Integer value of the result
   * @param element the {@link ViewHierarchyElement} which has the text color low contrast issue
   * @param metadata the metadata stored in this result
   */
  protected abstract @Nullable SetViewAttributeFixSuggestion produceTextContrastFixSuggestion(
      int checkResultId, ViewHierarchyElement element, ResultMetadata metadata);

  static SetViewAttributeFixSuggestion createSetViewAttributeFixSuggestion(
      String viewAttributeName, int suggestedColor) {
    String suggestedColorValue = ContrastUtils.colorToHexString(suggestedColor);
    return new SetViewAttributeFixSuggestion(viewAttributeName, suggestedColorValue);
  }

  static int getTextColor(ResultMetadata metadata) {
    if (metadata.containsKey(TextContrastCheck.KEY_TEXT_COLOR)) {
      return metadata.getInt(TextContrastCheck.KEY_TEXT_COLOR);
    } else {
      return metadata.getInt(TextContrastCheck.KEY_FOREGROUND_COLOR);
    }
  }

  static int getBackgroundColor(ResultMetadata metadata) {
    return metadata.getInt(TextContrastCheck.KEY_BACKGROUND_COLOR);
  }

  static double getRequiredContrastRatio(ResultMetadata metadata) {
    if (metadata.containsKey(TextContrastCheck.KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO)) {
      return metadata.getDouble(TextContrastCheck.KEY_CUSTOMIZED_HEURISTIC_CONTRAST_RATIO);
    } else {
      return metadata.getDouble(TextContrastCheck.KEY_REQUIRED_CONTRAST_RATIO);
    }
  }
}

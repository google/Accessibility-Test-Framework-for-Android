package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.checks.RedundantDescriptionCheck;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link FixSuggestionsProvider} which recommends setting the {@code contentDescription} view
 * attribute to a suggested value if the culprit view's speakable text contains redundant
 * information about the view's type.
 */
class RedundantDescriptionFixSuggestionProducer
    implements FixSuggestionProducer<SetViewAttributeFixSuggestion> {

  private static final ViewAttribute VIEW_ATTRIBUTE_CONTENT_DESCRIPTION =
      new ViewAttribute("contentDescription");

  @Override
  public @Nullable SetViewAttributeFixSuggestion produceFixSuggestion(
      AccessibilityHierarchyCheckResult checkResult,
      AccessibilityHierarchy hierarchy,
      @Nullable Parameters parameters) {
    if (checkResult.getResultId()
        != RedundantDescriptionCheck.RESULT_ID_CONTENT_DESC_CONTAINS_ITEM_TYPE) {
      return null;
    }

    ResultMetadata resultMetadata = checkNotNull(checkResult.getMetadata());
    String contentDescription =
        resultMetadata.getString(RedundantDescriptionCheck.KEY_CONTENT_DESCRIPTION);
    String redundantWord = resultMetadata.getString(RedundantDescriptionCheck.KEY_REDUNDANT_WORD);

    // Replaces every occurrence of the redundant word with a space (ignoring case), and then
    // collapses repeated spaces and removes leading and trailing spaces.
    // For example, changes "foo button bar" to "foo bar".
    // If the redundant word is part of another word, do not replace it.
    // For example, changes "Order Buttonhole button" to "Order Buttonhole"
    String suggestedContentDescription =
        contentDescription
            .replaceAll("\\b(?i)" + redundantWord + "\\b", " ")
            .replaceAll(" +", " ")
            .trim();
    if (suggestedContentDescription.length() == contentDescription.length()) {
      // For example, no fix suggestions will be produced for "Order Buttonhole"
      return null;
    }

    return new SetViewAttributeFixSuggestion(
        VIEW_ATTRIBUTE_CONTENT_DESCRIPTION, suggestedContentDescription);
  }
}

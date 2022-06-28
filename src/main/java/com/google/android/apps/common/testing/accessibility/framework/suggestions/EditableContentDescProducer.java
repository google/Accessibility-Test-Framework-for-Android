package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.checks.EditableContentDescCheck;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link FixSuggestionProducer} which recommends removing the {{@code contentDescription}} view
 * attribute if the culprit TextView is labeled by a contentDescription.
 */
class EditableContentDescProducer
    implements FixSuggestionProducer<RemoveViewAttributeFixSuggestion> {

  private static final String VIEW_PROPERTY_CONTENT_DESCRIPTION = "contentDescription";

  @Override
  public @Nullable RemoveViewAttributeFixSuggestion produceFixSuggestion(
      AccessibilityHierarchyCheckResult checkResult,
      AccessibilityHierarchy hierarchy,
      @Nullable Parameters parameters) {
    if (checkResult.getResultId()
        == EditableContentDescCheck.RESULT_ID_EDITABLE_TEXTVIEW_CONTENT_DESC) {
      // Suggests removing the contentDescription attribute of the editable TextView
      return new RemoveViewAttributeFixSuggestion(VIEW_PROPERTY_CONTENT_DESCRIPTION);
    }
    return null;
  }
}

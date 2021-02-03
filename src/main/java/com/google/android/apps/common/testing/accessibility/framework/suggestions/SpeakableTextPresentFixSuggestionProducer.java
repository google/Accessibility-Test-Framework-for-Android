package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils;
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link FixSuggestionProducer} which produces a {@link SetViewAttributeFixSuggestion} if the
 * culprit view is missing speakable text. The {@link SetViewAttributeFixSuggestion} recommends
 * setting the {@code contentDescription} or {@code hint} or {@code text} attribute to the culprit
 * View according to the view's type.
 */
class SpeakableTextPresentFixSuggestionProducer
    implements FixSuggestionProducer<SetViewAttributeFixSuggestion> {

  private static final String VIEW_ATTRIBUTE_CONTENT_DESCRIPTION = "contentDescription";

  private static final String VIEW_ATTRIBUTE_HINT = "hint";

  private static final String VIEW_ATTRIBUTE_TEXT = "text";

  @Override
  public @Nullable SetViewAttributeFixSuggestion produceFixSuggestion(
      AccessibilityHierarchyCheckResult checkResult,
      AccessibilityHierarchy hierarchy,
      @Nullable Parameters parameters) {
    ViewHierarchyElement viewHierarchyElement = checkResult.getElement();
    if (viewHierarchyElement == null) {
      return null;
    }

    if (checkResult.getResultId() == SpeakableTextPresentCheck.RESULT_ID_MISSING_SPEAKABLE_TEXT) {
      // If we set contentDescription on an editable TextView or an EditText, it creates a
      // redundant description issue.
      boolean shouldSetHint =
          (viewHierarchyElement.checkInstanceOf(ViewHierarchyElementUtils.EDIT_TEXT_CLASS_NAME)
              || (viewHierarchyElement.checkInstanceOf(
                      ViewHierarchyElementUtils.TEXT_VIEW_CLASS_NAME)
                  && Boolean.TRUE.equals(viewHierarchyElement.isEditable())));
      CharSequence className = viewHierarchyElement.getClassName();
      if (shouldSetHint) {
        return new SetViewAttributeFixSuggestion(VIEW_ATTRIBUTE_HINT, /* suggestedValue= */ "");
      } else if ((className != null)
          && Objects.equals(className.toString(), ViewHierarchyElementUtils.TEXT_VIEW_CLASS_NAME)) {
        // If the culprit view's class name is "android.widget.TextView" and not editable, suggest
        // setting "android:text" view attribute. We don't want to include subclasses, because
        // different subclasses handle android:text differently.
        return new SetViewAttributeFixSuggestion(VIEW_ATTRIBUTE_TEXT, /* suggestedValue= */ "");
      } else {
        return new SetViewAttributeFixSuggestion(
            VIEW_ATTRIBUTE_CONTENT_DESCRIPTION, /* suggestedValue= */ "");
      }
    }

    return null;
  }
}

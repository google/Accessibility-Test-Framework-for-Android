package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.common.annotations.Beta;
import java.util.Locale;

/**
 * A {@link FixSuggestion} which suggests setting a value to a view attribute to fix a {@link
 * AccessibilityHierarchyCheckResult}.
 *
 * <ul>
 *   <li>If the view attribute has not been set before, add the view attribute and set its value to
 *       the suggested value.
 *   <li>If the view attribute has been set before, replace its value with the suggested value.
 *   <li>If the suggested value is an empty string, ask the developer to set the view attribute to a
 *       meaningful non-empty string or resource reference. DO NOT set the view attribute to an
 *       empty string.
 * </ul>
 */
@Beta
public class SetViewAttributeFixSuggestion implements FixSuggestion {

  private final ViewAttribute viewAttribute;

  private final String suggestedValue;

  public SetViewAttributeFixSuggestion(ViewAttribute viewAttribute, String suggestedValue) {
    this.viewAttribute = viewAttribute;
    this.suggestedValue = suggestedValue;
  }

  public SetViewAttributeFixSuggestion(String attributeName, String suggestedValue) {
    this(new ViewAttribute(attributeName), suggestedValue);
  }

  /** Returns the {@link ViewAttribute} suggested to be changed. */
  public ViewAttribute getViewAttribute() {
    return viewAttribute;
  }

  /** Returns the suggested value for the view attribute. */
  public String getSuggestedValue() {
    return suggestedValue;
  }

  @Override
  public CharSequence getRawDescription(Locale locale) {
    if (suggestedValue.isEmpty()) {
      return String.format(
          locale,
          StringManager.getString(locale, "suggestion_set_view_attribute_with_an_non_empty_string"),
          viewAttribute.getFullyQualifiedName());
    } else {
      return String.format(
          locale,
          StringManager.getString(locale, "suggestion_set_view_attribute"),
          viewAttribute.getFullyQualifiedName(),
          suggestedValue);
    }
  }
}

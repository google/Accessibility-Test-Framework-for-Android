package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.common.annotations.Beta;
import java.util.Locale;

/**
 * A {@link FixSuggestion} which suggests removing a view attribute to fix a {@link
 * AccessibilityHierarchyCheckResult}
 */
@Beta
public class RemoveViewAttributeFixSuggestion implements FixSuggestion {

  private final ViewAttribute viewAttribute;

  public RemoveViewAttributeFixSuggestion(ViewAttribute viewAttribute) {
    this.viewAttribute = viewAttribute;
  }

  public RemoveViewAttributeFixSuggestion(String viewAttribute) {
    this(new ViewAttribute(viewAttribute));
  }

  /** Returns the {@link ViewAttribute} suggested to be removed. */
  public ViewAttribute getViewAttribute() {
    return viewAttribute;
  }

  @Override
  public CharSequence getRawDescription(Locale locale) {
    return String.format(
        locale,
        StringManager.getString(locale, "suggestion_remove_view_attribute"),
        viewAttribute.getFullyQualifiedName());
  }
}

package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.common.annotations.Beta;
import java.util.Locale;

/** A suggested fix for a {@link AccessibilityHierarchyCheckResult}. */
@Beta
public interface FixSuggestion {

  /**
   * Returns a human-readable description for this fix suggestion.
   *
   * @param locale desired locale for the description
   */
  CharSequence getDescription(Locale locale);
}

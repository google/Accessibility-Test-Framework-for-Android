package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Responsible for generating and sorting an immutable list of {@link FixSuggestion} for the given
 * {@link AccessibilityHierarchyCheckResult}.
 */
public interface FixSuggestionsProvider {

  /**
   * Provides an immutable list of {@link FixSuggestion} for the given {@link
   * AccessibilityHierarchyCheckResult}.
   *
   * <p>The list of {@link FixSuggestion} is sorted according to which {@link FixSuggestion} is
   * considered to be a better or more applicable fix.
   *
   * @param checkResult the {@link AccessibilityHierarchyCheckResult} which needs fix suggestions
   * @param hierarchy The hierarchy which contains the culprit {@link ViewHierarchyElement}
   * @param parameters Optional input data or preferences
   * @return suggested fixes (possibly none) sorted so that the better suggestions appear first
   */
  ImmutableList<FixSuggestion> provideFixSuggestions(
      AccessibilityHierarchyCheckResult checkResult,
      AccessibilityHierarchy hierarchy,
      @Nullable Parameters parameters);
}

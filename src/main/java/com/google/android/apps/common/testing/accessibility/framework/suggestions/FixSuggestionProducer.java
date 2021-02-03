package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Responsible for generating a special type of {@link FixSuggestion} for a given {@link
 * AccessibilityHierarchyCheckResult}.
 *
 * @param <T> the type of {@link FixSuggestion} to be generated
 */
interface FixSuggestionProducer<T extends FixSuggestion> {

  /**
   * Produces a {@link FixSuggestion} for the given {@link AccessibilityHierarchyCheckResult}.
   *
   * @param checkResult the {@link AccessibilityHierarchyCheckResult} which needs fix suggestions
   * @param hierarchy the hierarchy which contains the culprit
   * @param parameters Optional input data or preferences
   * @return the generated {@link FixSuggestion} or {@code null} if there is no feasible fix
   *     suggestion
   */
  @Nullable
  T produceFixSuggestion(
      AccessibilityHierarchyCheckResult checkResult,
      AccessibilityHierarchy hierarchy,
      @Nullable Parameters parameters);
}

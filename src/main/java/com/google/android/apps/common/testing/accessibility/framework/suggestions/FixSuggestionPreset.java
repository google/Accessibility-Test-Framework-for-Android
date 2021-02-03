package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.checks.EditableContentDescCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.RedundantDescriptionCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Provides an immutable list of {@link FixSuggestion} for a {@link
 * AccessibilityHierarchyCheckResult}.
 */
@Beta
public final class FixSuggestionPreset {

  private static final ImmutableMap<
          Class<? extends AccessibilityHierarchyCheck>, FixSuggestionsProvider>
      CHECK_CLASS_TO_FIX_SUGGESTION_PROVIDER =
          ImmutableMap.of(
              EditableContentDescCheck.class,
              new EditableContentDescFixSuggestionsProvider(),
              TouchTargetSizeCheck.class,
              new TouchTargetSizeFixSuggestionsProvider(),
              TextContrastCheck.class,
              new TextContrastFixSuggestionsProvider(),
              SpeakableTextPresentCheck.class,
              new SpeakableTextPresentFixSuggestionsProvider(),
              RedundantDescriptionCheck.class,
              new RedundantDescriptionFixSuggestionsProvider());

  private FixSuggestionPreset() {
    // Not instantiable
  }

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
  public static ImmutableList<FixSuggestion> provideFixSuggestions(
      AccessibilityHierarchyCheckResult checkResult,
      AccessibilityHierarchy hierarchy,
      @Nullable Parameters parameters) {
    FixSuggestionsProvider fixSuggestionsProvider =
        CHECK_CLASS_TO_FIX_SUGGESTION_PROVIDER.get(checkResult.getSourceCheckClass());
    return (fixSuggestionsProvider == null)
        ? ImmutableList.of()
        : fixSuggestionsProvider.provideFixSuggestions(checkResult, hierarchy, parameters);
  }
}

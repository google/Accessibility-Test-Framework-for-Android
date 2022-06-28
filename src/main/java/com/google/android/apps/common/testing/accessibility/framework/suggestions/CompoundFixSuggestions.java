package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.StandardSystemProperty.LINE_SEPARATOR;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Locale;

/**
 * A {@link FixSuggestion} which recommends applying multiple {@link FixSuggestion} together to fix
 * an {@link AccessibilityHierarchyCheckResult}.
 *
 * <p>A {@link CompoundFixSuggestions} must contain at least 2 {@link FixSuggestion}.
 */
@Beta
public class CompoundFixSuggestions implements FixSuggestion {

  private final ImmutableList<FixSuggestion> fixSuggestions;

  /**
   * Creates a {@link CompoundFixSuggestions} from an immutable list of {@link FixSuggestion}.
   *
   * @param fixSuggestions an immutable list of {@link FixSuggestion} suggested to be applied
   *     together
   * @throws IllegalArgumentException if the given fix suggestion list contains less than 2 fix
   *     suggestions
   */
  public CompoundFixSuggestions(ImmutableList<FixSuggestion> fixSuggestions) {
    checkArgument(
        fixSuggestions.size() > 1,
        "The fix suggestion list must contain at least 2 fix suggestions");
    this.fixSuggestions = fixSuggestions;
  }

  /** Returns an immutable list of {@link FixSuggestion} suggested to be applied together. */
  public ImmutableList<FixSuggestion> getFixSuggestions() {
    return fixSuggestions;
  }

  @Override
  public CharSequence getRawDescription(Locale locale) {
    String description = "";
    for (int i = 0; i < fixSuggestions.size(); i++) {
      description += fixSuggestions.get(i).getRawDescription(locale);
      if (i < fixSuggestions.size() - 1) {
        description += LINE_SEPARATOR.value();
      }
    }
    return description;
  }
}

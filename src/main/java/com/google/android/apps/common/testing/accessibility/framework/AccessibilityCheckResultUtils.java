/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.apps.common.testing.accessibility.framework;

import static org.hamcrest.Matchers.equalTo;

import android.view.View;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.checks.ClickableSpanCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.DuplicateClickableBoundsCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.DuplicateSpeakableTextCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.EditableContentDescCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.RedundantDescriptionCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck;
import com.google.common.collect.ImmutableBiMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

/** Utility class for dealing with {@code AccessibilityCheckResult}s */
@SuppressWarnings("deprecation") // Need to support AccessibilityViewCheckResult.
public final class AccessibilityCheckResultUtils {

  /**
   * Mapping from the names of obsolete AccessibilityViewHierarchyCheck to the replacement
   * AccessibilityHierarchyCheck.
   */
  private static final ImmutableBiMap<String, Class<?>> VIEW_CHECK_ALIASES =
      ImmutableBiMap.<String, Class<?>>builder()
          .put("ClickableSpanViewCheck", ClickableSpanCheck.class)
          .put("DuplicateClickableBoundsViewCheck", DuplicateClickableBoundsCheck.class)
          .put("DuplicateSpeakableTextViewHierarchyCheck", DuplicateSpeakableTextCheck.class)
          .put("EditableContentDescViewCheck", EditableContentDescCheck.class)
          .put("RedundantContentDescViewCheck", RedundantDescriptionCheck.class)
          .put("SpeakableTextPresentViewCheck", SpeakableTextPresentCheck.class)
          .put("TextContrastViewCheck", TextContrastCheck.class)
          .put("TouchTargetSizeViewCheck", TouchTargetSizeCheck.class)
          .buildOrThrow();

  private AccessibilityCheckResultUtils() {
  }

  /**
   * Takes a list of {@code AccessibilityCheckResult}s and returns a list with only results obtained
   * from the given {@code AccessibilityCheck}.
   * <p>
   * NOTE: This method explicitly does not take subtypes of {@code checkClass} into account when
   * filtering results.
   *
   * @param results a list of {@code AccessibilityCheckResult}s
   * @param checkClass the {@code Class} of the {@code AccessibilityCheck} to get results for
   * @return a list of {@code AccessibilityCheckResult}s obtained from the given
   *         {@code AccessibilityCheck}.
   */
  public static <T extends AccessibilityCheckResult> List<T> getResultsForCheck(
      Iterable<T> results, Class<? extends AccessibilityCheck> checkClass) {
    return AccessibilityCheckResultBaseUtils.getResultsForCheck(results, checkClass);
  }

  /**
   * Filters {@link AccessibilityCheckResult}s and returns a list with only results which match the
   * given {@link AccessibilityCheckResultType}.
   *
   * @param results an {@link Iterable} of {@link AccessibilityCheckResult}s
   * @param type the {@link AccessibilityCheckResultType} for the results to be returned
   * @return a list of {@link AccessibilityCheckResult}s with the given
   *         {@link AccessibilityCheckResultType}.
   */
  public static <T extends AccessibilityCheckResult> List<T> getResultsForType(
      Iterable<T> results, AccessibilityCheckResultType type) {
    return AccessibilityCheckResultBaseUtils.getResultsForType(results, type);
  }

  /**
   * Filters {@code AccessibilityCheckResult}s and returns a list with only results which match the
   * given {@link AccessibilityCheckResultType}s.
   *
   * @param results an {@link Iterable} of {@code AccessibilityCheckResult}s
   * @param types a {@link Set} of {@link AccessibilityCheckResultType}s that should be returned in
   *        the filtered list
   * @return a {@link List} containing only the {@link AccessibilityHierarchyCheckResult}s from
   *         {@code results} which are of a type included in {@code types}
   */
  public static <T extends AccessibilityCheckResult> List<T> getResultsForTypes(
      Iterable<T> results, Set<AccessibilityCheckResultType> types) {
    return AccessibilityCheckResultBaseUtils.getResultsForTypes(results, types);
  }

  /**
   * Takes a list of {@code AccessibilityCheckResult}s and returns a list with only results
   * pertaining to the given {@code View}.
   *
   * @param results a list of {@code AccessibilityCheckResult}s
   * @param view the {@code View} to get results for
   * @return a list of {@code AccessibilityCheckResult}s pertaining to the given {@code View}.
   */
  public static List<AccessibilityViewCheckResult> getResultsForView(
      Iterable<AccessibilityViewCheckResult> results, View view) {
    List<AccessibilityViewCheckResult> resultsForView = new ArrayList<>();
    for (AccessibilityViewCheckResult result : results) {
      if (result.getView() == view) {
        resultsForView.add(result);
      }
    }
    return resultsForView;
  }

  /**
   * Returns a {@link Matcher} for an {@link AccessibilityCheckResult} whose result type matches the
   * given matcher for {@link AccessibilityCheckResultType}.
   *
   * @param typeMatcher a {@code Matcher} for an {@code AccessibilityCheckResultType}
   * @return a {@code Matcher} for an {@code AccessibilityCheckResult}
   */
  public static Matcher<AccessibilityCheckResult> matchesTypes(
      final Matcher<? super AccessibilityCheckResultType> typeMatcher) {
    return AccessibilityCheckResultBaseUtils.matchesTypes(typeMatcher);
  }

  /**
   * Returns a {@link Matcher} for an {@link AccessibilityCheckResult} whose source check class is
   * equal to the given check class.
   *
   * <p>This is syntactic sugar for {@code matchesChecks(equalTo(checkClass))}.
   */
  public static Matcher<AccessibilityCheckResult> matchesCheck(
      Class<? extends AccessibilityCheck> checkClass) {
    return matchesChecks(equalTo(checkClass));
  }

  /**
   * Returns a {@link Matcher} for an {@link AccessibilityCheckResult} whose source check class
   * matches the given matcher.
   * <p>
   * Note: Do not use {@link Matchers#is} for a {@link Class}, as the deprecated form will match
   * only objects of that class instead of the class object itself. Use {@link Matchers#equalTo}
   * instead.
   *
   * @param classMatcher a {@code Matcher} for a {@code Class<? extends AccessibilityCheck>}.
   *                     Note: strict typing not enforced for Java 7 compatibility
   * @return a {@code Matcher} for a {@code AccessibilityCheckResult}
   */
  public static Matcher<AccessibilityCheckResult> matchesChecks(final Matcher<?> classMatcher) {
    return AccessibilityCheckResultBaseUtils.matchesChecks(classMatcher);
  }

  /**
   * Returns a {@link Matcher} for an {@link AccessibilityCheckResult} whose source check class has
   * a simple name that matches the given matcher for a {@code String}.
   *
   * @param classNameMatcher a {@code Matcher} for a {@code String}
   * @return a {@code Matcher} for an {@code AccessibilityCheckResult}
   */
  public static Matcher<AccessibilityCheckResult> matchesCheckNames(
      final Matcher<? super String> classNameMatcher) {
    return AccessibilityCheckResultBaseUtils.matchesCheckNames(
        classNameMatcher, VIEW_CHECK_ALIASES);
  }

  /**
   * Returns a {@link Matcher} for an {@link AccessibilityViewCheckResult} whose view matches the
   * given matcher for a {@link View}.
   *
   * @param viewMatcher a {@code Matcher} for a {@code View}
   * @return a {@code Matcher} for an {@code AccessibilityViewCheckResult}
   */
  public static Matcher<AccessibilityViewCheckResult> matchesViews(
      final Matcher<? super View> viewMatcher) {
    return new TypeSafeMemberMatcher<AccessibilityViewCheckResult>("View", viewMatcher) {
      @Override
      public boolean matchesSafely(AccessibilityViewCheckResult result) {
        View view = result.getView();
        return (view != null) && viewMatcher.matches(view);
      }
    };
  }

  /**
   * Returns a {@link Matcher} that matches if the given Integer matcher matches the {@link
   * AccessibilityViewCheckResult#getResultId()} of the examined {@code
   * AccessibilityViewCheckResult}.
   *
   * @param resultIdMatcher a {@code Matcher} for result ID values
   * @return a {@code Matcher} for an {@code AccessibilityViewCheckResult}
   */
  public static Matcher<AccessibilityViewCheckResult> matchesResultId(
      final Matcher<Integer> resultIdMatcher) {
    return new TypeSafeMemberMatcher<AccessibilityViewCheckResult>("result id", resultIdMatcher) {
      @Override
      public boolean matchesSafely(AccessibilityViewCheckResult result) {
        return resultIdMatcher.matches(result.getResultId());
      }
    };
  }

  private abstract static class TypeSafeMemberMatcher<T> extends TypeSafeMatcher<T> {
    private final String memberDescription;
    private final Matcher<?> matcher;

    public TypeSafeMemberMatcher(String member, Matcher<?> matcher) {
      memberDescription = String.format("with %s: ", member);
      this.matcher = matcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(memberDescription);
      matcher.describeTo(description);
    }
  }
}

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

import androidx.annotation.Nullable;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

/**
 * Utility class for dealing with {@code AccessibilityCheckResult}s
 */
public final class AccessibilityCheckResultUtils {

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
    List<T> resultsForCheck = new ArrayList<T>();
    for (T result : results) {
      if (checkClass.equals(result.getSourceCheckClass())) {
        resultsForCheck.add(result);
      }
    }
    return resultsForCheck;
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
    List<T> resultsForType = new ArrayList<T>();
    for (T result : results) {
      if (result.getType() == type) {
        resultsForType.add(result);
      }
    }
    return resultsForType;
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
    List<T> resultsForTypes = new ArrayList<T>();
    for (T result : results) {
      if (types.contains(result.getType())) {
        resultsForTypes.add(result);
      }
    }
    return resultsForTypes;
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
   * Takes a list of {@code AccessibilityCheckResult}s and returns a list with only results
   * pertaining to the given {@code AccessibilityNodeInfo}.
   *
   * @param results a list of {@code AccessibilityCheckResult}s
   * @param info the {@code AccessibilityNodeInfo} to get results for
   * @return a list of {@code AccessibilityCheckResult}s pertaining to the given
   *         {@code AccessibilityNodeInfo}.
   */
  public static List<AccessibilityInfoCheckResult> getResultsForInfo(
      Iterable<AccessibilityInfoCheckResult> results, AccessibilityNodeInfo info) {
    List<AccessibilityInfoCheckResult> resultsForInfo = new ArrayList<>();
    for (AccessibilityInfoCheckResult result : results) {
      if (info.equals(result.getInfo())) {
        resultsForInfo.add(result);
      }
    }
    return resultsForInfo;
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
    return new TypeSafeMemberMatcher<AccessibilityCheckResult>("result type", typeMatcher) {
      @Override
      public boolean matchesSafely(AccessibilityCheckResult result) {
        return typeMatcher.matches(result.getType());
      }
    };
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
    return new TypeSafeMemberMatcher<AccessibilityCheckResult>("source check", classMatcher) {
      @Override
      public boolean matchesSafely(AccessibilityCheckResult result) {
        return classMatcher.matches(result.getSourceCheckClass());
      }
    };
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
    return new TypeSafeMemberMatcher<AccessibilityCheckResult>("source check name",
        classNameMatcher) {
      @Override
      public boolean matchesSafely(AccessibilityCheckResult result) {
        return classNameMatcher.matches(result.getSourceCheckClass().getSimpleName());
      }
    };
  }

  /**
   * Returns a {@link Matcher} for an {@link AccessibilityViewCheckResult} whose view
   * matches the given matcher for a {@link View}.
   *
   * @param viewMatcher a {@code Matcher} for a {@code View}
   * @return a {@code Matcher} for an {@code AccessibilityCheckResult}
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
   * Returns a {@link Matcher} for an {@link AccessibilityNodeInfo} whose
   * {@link AccessibilityNodeInfo} matches the given matcher.
   *
   * @param infoMatcher a {@code Matcher} for a {@code AccessibilityNodeInfo}
   * @return a {@code Matcher} for an {@code AccessibilityCheckResult}
   */
  public static Matcher<AccessibilityInfoCheckResult> matchesInfos(
      final Matcher<? super AccessibilityNodeInfo> infoMatcher) {
    return new TypeSafeMemberMatcher<AccessibilityInfoCheckResult>("AccessibilityNodeInfo",
        infoMatcher) {
      @Override
      public boolean matchesSafely(AccessibilityInfoCheckResult result) {
        AccessibilityNodeInfo info = result.getInfo();
        return (info != null) && infoMatcher.matches(info);
      }
    };
  }

  /**
   * Change the result type to {@code SUPPRESSED} for all results in the given list that match the
   * given matcher.
   *
   * @param results a list of {@code AccessibilityCheckResult}s to be matched against
   * @param matcher a Matcher that determines whether a given {@code AccessibilityCheckResult}
   *        should be suppressed
   */
  public static <T extends AccessibilityCheckResult> void suppressMatchingResults(
      List<T> results, @Nullable Matcher<? super T> matcher) {
    if (matcher != null) {
      modifyResultType(results, matcher, AccessibilityCheckResultType.SUPPRESSED);
    }
  }

  private static <T extends AccessibilityCheckResult> void modifyResultType(
      List<T> results, Matcher<? super T> matcher, AccessibilityCheckResultType newType) {
    for (T result : results) {
      if (matcher.matches(result)) {
        result.setType(newType);
      }
    }
  }

  private abstract static class TypeSafeMemberMatcher<T> extends TypeSafeMatcher<T> {
    private static final String DESCRIPTION_FORMAT_STRING = "with %s: ";
    private final String memberDescription;
    private final Matcher<?> matcher;

    public TypeSafeMemberMatcher(String member, Matcher<?> matcher) {
      memberDescription = String.format(DESCRIPTION_FORMAT_STRING, member);
      this.matcher = matcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(memberDescription);
      matcher.describeTo(description);
    }
  }
}

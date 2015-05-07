/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.android.apps.common.testing.accessibility.framework.integrations.espresso;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultDescriptor;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.integrations.AccessibilityViewCheckException;

import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A configurable executor for the {@link AccessibilityViewHierarchyCheck}s designed for use with
 * Espresso. Clients can call {@link #checkAndReturnResults} on a {@link View}
 * to run all of the checks with the options specified in this object.
 */
public class AccessibilityValidator {

  private static final String TAG = "AccessibilityValidator";
  private boolean runChecksFromRootView = false;
  private boolean throwExceptionForErrors = true;
  private AccessibilityCheckResultDescriptor resultDescriptor =
      new AccessibilityCheckResultDescriptor();
  private List<AccessibilityViewHierarchyCheck> viewHierarchyChecks =
      new LinkedList<AccessibilityViewHierarchyCheck>();
  private Matcher<? super AccessibilityViewCheckResult> suppressingMatcher = null;

  public AccessibilityValidator() {
    viewHierarchyChecks.addAll(AccessibilityCheckPreset.getViewChecksForPreset(
        AccessibilityCheckPreset.LATEST));
  }

  /**
   * Runs accessibility checks and returns the list of results.
   *
   * @param view the {@link View} to check
   * @return the resulting list of {@link AccessibilityViewCheckResult}
   */
  public final List<AccessibilityViewCheckResult> checkAndReturnResults(View view) {
    if (view != null) {
      View viewToCheck = runChecksFromRootView ? view.getRootView() : view;
      return runAccessibilityChecks(viewToCheck);
    }
    return Collections.<AccessibilityViewCheckResult>emptyList();
  }

  /**
   * @param runChecksFromRootView {@code true} to check all views in the hierarchy, {@code false} to
   *        check only views in the hierarchy rooted at the passed in view. Default: {@code false}
   * @return this
   */
  public AccessibilityValidator setRunChecksFromRootView(
      boolean runChecksFromRootView) {
    this.runChecksFromRootView = runChecksFromRootView;
    return this;
  }

  /**
   * Suppresses all results that match the given matcher. Suppressed results will not be included
   * in any logs or cause any {@code Exception} to be thrown
   *
   * @param resultMatcher a matcher to match a {@link AccessibilityViewCheckResult}
   * @return this
   */
  public AccessibilityValidator setSuppressingResultMatcher(
      Matcher<? super AccessibilityViewCheckResult> resultMatcher) {
      suppressingMatcher = resultMatcher;
    return this;
  }

  /**
   * @param throwExceptionForErrors {@code true} to throw an {@code Exception} when there is at
   *        least one error result, {@code false} to just log the error results to logcat.
   *        Default: {@code true}
   * @return this
   */
  public AccessibilityValidator setThrowExceptionForErrors(
      boolean throwExceptionForErrors) {
    this.throwExceptionForErrors = throwExceptionForErrors;
    return this;
  }

  /**
   * Sets the {@link AccessibilityCheckResultDescriptor} that is used to convert results to readable
   * messages in exceptions and logcat statements.
   *
   * @return this
   */
  public AccessibilityValidator setResultDescriptor(
      AccessibilityCheckResultDescriptor resultDescriptor) {
    this.resultDescriptor = resultDescriptor;
    return this;
  }

  /**
   * Runs accessibility checks on a {@code View} hierarchy
   *
   * @param root the root {@code View} of the hierarchy
   * @return a list of the results of the checks
   */
  private List<AccessibilityViewCheckResult> runAccessibilityChecks(
      View root) {
    List<AccessibilityViewCheckResult> results = new LinkedList<>();
    for (AccessibilityViewHierarchyCheck check : viewHierarchyChecks) {
      results.addAll(check.runCheckOnViewHierarchy(root));
    }
    AccessibilityCheckResultUtils.suppressMatchingResults(results, suppressingMatcher);
    processResults(results);
    return results;
  }

  // TODO(sjrush): Determine a more robust reporting mechanism instead of using logcat.
  private void processResults(Iterable<AccessibilityViewCheckResult> results) {
    if (results == null) {
      return;
    }
    List<AccessibilityViewCheckResult> infos = AccessibilityCheckResultUtils.getResultsForType(
        results, AccessibilityCheckResultType.INFO);
    List<AccessibilityViewCheckResult> warnings = AccessibilityCheckResultUtils.getResultsForType(
        results, AccessibilityCheckResultType.WARNING);
    List<AccessibilityViewCheckResult> errors = AccessibilityCheckResultUtils.getResultsForType(
        results, AccessibilityCheckResultType.ERROR);
    for (AccessibilityViewCheckResult result : infos) {
      Log.i(TAG, resultDescriptor.describeResult(result));
    }
    for (AccessibilityViewCheckResult result : warnings) {
      Log.w(TAG, resultDescriptor.describeResult(result));
    }
    if (!errors.isEmpty() && throwExceptionForErrors) {
      throw new AccessibilityViewCheckException(errors)
          .setResultDescriptor(resultDescriptor);
    } else {
      for (AccessibilityViewCheckResult result : errors) {
        Log.e(TAG, resultDescriptor.describeResult(result));
      }
    }
  }
}

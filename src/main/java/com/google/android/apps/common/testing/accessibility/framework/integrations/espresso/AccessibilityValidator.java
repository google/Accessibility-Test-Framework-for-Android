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

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.util.Log;
import android.view.View;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultDescriptor;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.integrations.AccessibilityViewCheckException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hamcrest.Matcher;

/**
 * A configurable executor for the {@link AccessibilityViewHierarchyCheck}s designed for use with
 * Espresso. Clients can call {@link #checkAndReturnResults} on a {@link View}
 * to run all of the checks with the options specified in this object.
 */
public final class AccessibilityValidator {

  private static final String TAG = "AccessibilityValidator";
  private AccessibilityCheckPreset preset = AccessibilityCheckPreset.LATEST;
  private boolean runChecksFromRootView = false;
  private boolean throwExceptionForErrors = true;
  private AccessibilityCheckResultDescriptor resultDescriptor =
      new AccessibilityCheckResultDescriptor();
  private Matcher<? super AccessibilityViewCheckResult> suppressingMatcher = null;
  private final List<AccessibilityCheckListener> checkListeners = new ArrayList<>();

  public AccessibilityValidator() {
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
   * Specify the set of checks to be run. The default is {link AccessibilityCheckPreset.LATEST}.
   *
   * @param preset The preset specifying the group of checks to run.
   * @return this
   */
  public AccessibilityValidator setCheckPreset(AccessibilityCheckPreset preset) {
    this.preset = preset;
    return this;
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
   * Adds a listener to receive all {@link AccessibilityCheckResult}s after suppression. Listeners
   * will be called in the order they are added and before any
   * {@link AccessibilityViewCheckException} would be thrown.
   *
   * @return this
   */
  public AccessibilityValidator addCheckListener(AccessibilityCheckListener listener) {
    checkNotNull(listener);
    checkListeners.add(listener);
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
    List<AccessibilityViewHierarchyCheck> viewHierarchyChecks = new ArrayList<>(
        AccessibilityCheckPreset.getViewChecksForPreset(preset));
    List<AccessibilityViewCheckResult> results = new ArrayList<>();
    for (AccessibilityViewHierarchyCheck check : viewHierarchyChecks) {
      results.addAll(check.runCheckOnViewHierarchy(root));
    }
    AccessibilityCheckResultUtils.suppressMatchingResults(results, suppressingMatcher);

    for (AccessibilityCheckListener checkListener : checkListeners) {
      checkListener.onResults(root.getContext(), results);
    }

    processResults(results);
    return results;
  }

  /**
   * Reports the given results to the user using logcat and/or exceptions depending on the options
   * set for this {@code AccessibilityValidator}. Results of type {@code INFO} and {@code WARNING}
   * will be logged to logcat, and results of type {@code ERROR} will be logged to logcat or
   * a single {@link AccessibilityViewCheckException} will be thrown containing all {@code ERROR}
   * results, depending on the value of {@link #throwExceptionForErrors}.
   */
  private void processResults(Iterable<AccessibilityViewCheckResult> results) {
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
      throw new AccessibilityViewCheckException(errors, resultDescriptor);
    } else {
      for (AccessibilityViewCheckResult result : errors) {
        Log.e(TAG, resultDescriptor.describeResult(result));
      }
    }
  }

  /**
   * Interface for receiving callbacks when results have been obtained.
   */
  public static interface AccessibilityCheckListener {
    void onResults(Context context, List<? extends AccessibilityViewCheckResult> results);
  }
}

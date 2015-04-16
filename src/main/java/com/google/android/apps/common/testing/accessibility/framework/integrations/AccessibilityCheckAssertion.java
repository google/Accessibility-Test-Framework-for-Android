package com.google.android.apps.common.testing.accessibility.framework.integrations;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckException;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewHierarchyCheck;
import com.google.android.apps.common.testing.testrunner.InstrumentationArgumentsRegistry;
import com.google.android.apps.common.testing.ui.espresso.EspressoException;
import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.google.android.apps.common.testing.ui.espresso.ViewAssertion;
import com.google.android.apps.common.testing.ui.espresso.action.ViewActions;
import com.google.android.apps.common.testing.ui.espresso.util.HumanReadables;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A class to enable automated accessibility checks in Espresso tests. These checks will run
 * as a global {@code ViewAssertion} ({@see ViewActions#addGlobalAssertion(ViewAssertion)}), and
 * cover a variety of accessibility issues (see {@link AccessibilityCheckPreset#VIEW_CHECKS} and
 * {@link AccessibilityCheckPreset#VIEW_HIERARCHY_CHECKS} to see which checks are run).
 */
public class AccessibilityCheckAssertion implements ViewAssertion {

  private static final String TAG = "AccessibilityCheckAssertion";
  private static final String SUPPRESS_ACCESSIBILITY_CHECKS_FLAG = "suppress_a11y_checks";
  private boolean checksEnabled = false;
  private boolean runChecksFromRootView = false;
  private boolean throwExceptionForErrors = true;
  private List<AccessibilityViewHierarchyCheck> viewHierarchyChecks =
      new LinkedList<AccessibilityViewHierarchyCheck>();
  private Matcher<? super AccessibilityViewCheckResult> suppressingMatcher = null;

  public AccessibilityCheckAssertion() {
    viewHierarchyChecks.addAll(AccessibilityCheckPreset.getViewChecksForPreset(
        AccessibilityCheckPreset.LATEST));
  }

  @Override
  public void check(View view, NoMatchingViewException noViewFoundException) {
    if (noViewFoundException != null) {
      Log.e(TAG,
          String.format("'accessibility checks could not be performed because view '%s' was not"
              + "found.\n", noViewFoundException.getViewMatcherDescription()));
      throw noViewFoundException;
    }
    if (view == null) {
      throw new NullPointerException();
    }
    checkAndReturnResults(view);
  }

  /**
   * Runs accessibility checks and returns the list of results.
   *
   * @param view the {@link View} to check
   * @return the resulting list of {@link AccessibilityViewCheckResult}
   */
  protected final List<AccessibilityViewCheckResult> checkAndReturnResults(View view) {
    if (view != null) {
      View viewToCheck = runChecksFromRootView ? view.getRootView() : view;
      return runAccessibilityChecks(viewToCheck);
    }
    return Collections.<AccessibilityViewCheckResult>emptyList();
  }

  /**
   * Enables accessibility checking as a global ViewAssertion in {@link ViewActions}.
   * Check {@link #isEnabled()} before calling to avoid an {code IllegalStateException".}
   *
   * @throws {@code IllegalStateException} if accessibilty checks were already enabled
   */
  public void enable() {
    if (checksEnabled) {
      throw new IllegalStateException("Accessibility checks already enabled!");
    }
    checksEnabled = true;
    ViewActions.addGlobalAssertion("Accessibility Checks", this);
  }

  /**
   * Calls {@link #enable()} if a flag with the given key is present in the instrumentation
   * arguments. These flags can be passed to {@code adb instrument} with the -e flag.
   */
  public void enableIfFlagPresent(String flag) {
    Bundle args = InstrumentationArgumentsRegistry.getInstance();
    String flagValue = args.getString(flag);
    if (flagValue != null) {
      enable();
    }
  }

  /**
   * Disables accessibility checking.
   * Check {@link #isEnabled()} before calling to avoid an {code IllegalStateException".}
   *
   * @throws {@code IllegalStateException} if accessibilty checks were already disabled
   */
  public void disable() {
    if (!checksEnabled) {
      throw new IllegalStateException("Accessibility checks already disabled!");
    }
    checksEnabled = false;
    ViewActions.removeGlobalAssertion(this);
  }

  /**
   * @return true if accessibility checking is enabled
   */
  public boolean isEnabled() {
    return checksEnabled;
  }

  /**
   * @param runChecksFromRootView {@code true} to check all views in the hierarchy, {@code false} to
   *        check only views in the hierarchy rooted at the passed in view. Default: {@code false}
   * @return this
   */
  public AccessibilityCheckAssertion setRunChecksFromRootView(boolean runChecksFromRootView) {
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
  public AccessibilityCheckAssertion setSuppressingResultMatcher(
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
  public AccessibilityCheckAssertion setThrowExceptionForErrors(boolean throwExceptionForErrors) {
    this.throwExceptionForErrors = throwExceptionForErrors;
    return this;
  }

  private static boolean shouldCheckAccessibility() {
    Bundle args = InstrumentationArgumentsRegistry.getInstance();
    if (args != null && Boolean.valueOf(args.getString(SUPPRESS_ACCESSIBILITY_CHECKS_FLAG))) {
      return false;
    }
    return true;
  }

  /**
   * Runs accessibility checks on a {@code View} with the arguments passed through
   * 'adb am shell instrument -e' (see {@link shouldCheckAccessibility()} to determine which
   * arguments will allow these checks to run)
   *
   * @param view the {@code View} to run accessibility checks on
   * @return a list of the results of the checks
   */
  private List<AccessibilityViewCheckResult> runAccessibilityChecks(
      View view) {
    if (!shouldCheckAccessibility()) {
      return null;
    }
    List<AccessibilityViewCheckResult> results = new LinkedList<AccessibilityViewCheckResult>();
    results.addAll(getViewHierarchyCheckResults(view, viewHierarchyChecks));
    AccessibilityCheckResultUtils.suppressMatchingResults(results, suppressingMatcher);
    processResults(results);
    return results;
  }

  private static List<AccessibilityViewCheckResult> getViewHierarchyCheckResults(View root,
      Iterable<AccessibilityViewHierarchyCheck> checks) {
    List<AccessibilityViewCheckResult> results = new LinkedList<AccessibilityViewCheckResult>();
    for (AccessibilityViewHierarchyCheck check : checks) {
      results.addAll(check.runCheckOnViewHierarchy(root));
    }
    return results;
  }

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
      Log.i(TAG, getResultMessage(result));
    }
    for (AccessibilityViewCheckResult result : warnings) {
      Log.w(TAG, getResultMessage(result));
    }
    if (!errors.isEmpty() && throwExceptionForErrors) {
      throw new EspressoAccessibilityException(errors);
    } else {
      for (AccessibilityViewCheckResult result : errors) {
        Log.e(TAG, getResultMessage(result));
      }
    }
  }

  private static String getResultMessage(AccessibilityViewCheckResult result) {
    StringBuilder message = new StringBuilder();
    message.append(HumanReadables.describe(result.getView()));
    message.append(": ");
    message.append(result.getMessage());
    return message.toString();
  }

  private static class EspressoAccessibilityException extends AccessibilityViewCheckException
      implements EspressoException {
    protected EspressoAccessibilityException(List<AccessibilityViewCheckResult> results) {
      super(results);
    }

    @Override
    protected String getResultMessage(AccessibilityViewCheckResult result) {
      return AccessibilityCheckAssertion.getResultMessage(result);
    }
  }
}

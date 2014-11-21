package com.google.android.apps.common.testing.accessibility.framework;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;

import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for dealing with {@code AccessibilityCheckResult}s
 */
public class AccessibilityCheckResultUtils {

  public AccessibilityCheckResultUtils() {
  }

  /**
   * Takes a list of {@code AccessibilityCheckResult}s and returns a list with only results
   * obtained from the given {@code AccessibilityCheck}.
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
      if (checkClass.isInstance(result.getSourceCheck())) {
        resultsForCheck.add(result);
      }
    }
    return resultsForCheck;
  }

  /**
   * Takes a list of {@code AccessibilityCheckResult}s and returns a list with only results
   * with the given {@code AccessibilityCheckResultType}.
   *
   * @param results a list of {@code AccessibilityCheckResult}s
   * @param type the {@code AccessibilityCheckResultType} for the results to be returned
   * @return a list of {@code AccessibilityCheckResult}s with the given
   *         {@code AccessibilityCheckResultType}.
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
   * Takes a list of {@code AccessibilityCheckResult}s and returns a list with only results
   * pertaining to the given {@code View}.
   *
   * @param results a list of {@code AccessibilityCheckResult}s
   * @param view the {@code View} to get results for
   * @return a list of {@code AccessibilityCheckResult}s pertaining to the given {@code View}.
   */
  public static List<AccessibilityViewCheckResult> getResultsForView(
      Iterable<AccessibilityViewCheckResult> results, View view) {
    List<AccessibilityViewCheckResult> resultsForView =
        new ArrayList<AccessibilityViewCheckResult>();
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
    List<AccessibilityInfoCheckResult> resultsForInfo =
        new ArrayList<AccessibilityInfoCheckResult>();
    for (AccessibilityInfoCheckResult result : results) {
      if (info.equals(result.getInfo())) {
        resultsForInfo.add(result);
      }
    }
    return resultsForInfo;
  }
}

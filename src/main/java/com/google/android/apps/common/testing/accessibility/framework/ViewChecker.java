package com.google.android.apps.common.testing.accessibility.framework;

import android.view.View;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.android.libraries.accessibility.utils.log.LogUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Evaluates accessibility checks against specified Views. */
@CheckReturnValue
@SuppressWarnings("deprecation") // AccessibilityViewCheckResult used for legacy check delegation
public class ViewChecker {

  private static final String TAG = "ViewChecker";
  private boolean obtainCharacterLocations = false;

  /**
   * Indicates whether text character locations should be requested.
   *
   * @param obtainCharacterLocations The value to which the flag should be set.
   * @return this
   */
  @CanIgnoreReturnValue
  public ViewChecker setObtainCharacterLocations(boolean obtainCharacterLocations) {
    this.obtainCharacterLocations = obtainCharacterLocations;
    return this;
  }

  /**
   * Runs a single AccessibilityHierarchyCheck on a View.
   *
   * @param root The root view of the hierarchy to check.
   * @param check The AccessibilityHierarchyCheck to be run.
   * @param parameters Optional input data or preferences.
   * @return A list of interesting results encountered while running the check. The list will be
   *     empty if the check passes without incident.
   */
  /* package */ ImmutableList<AccessibilityViewCheckResult> runCheckOnView(
      AccessibilityHierarchyCheck check, View root, @Nullable Parameters parameters) {
    return runChecksOnView(ImmutableSet.of(check), root, parameters);
  }

  /**
   * Runs a set of AccessibilityHierarchyChecks on a View.
   *
   * @param root The root view of the hierarchy to check.
   * @param checks The AccessibilityHierarchyChecks to be run.
   * @param parameters Optional input data or preferences.
   * @return A list of interesting results encountered while running the checks. The list will be
   *     empty if the checks pass without incident.
   */
  public ImmutableList<AccessibilityViewCheckResult> runChecksOnView(
      ImmutableSet<AccessibilityHierarchyCheck> checks,
      View root,
      @Nullable Parameters parameters) {

    // Construct the AccessibilityHierarchyAndroid from the actual view root, as to capture all
    // available information within the view hierarchy.
    View actualRoot = root.getRootView();
    BiMap<Long, View> mapFromElementIdToView = HashBiMap.<Long, View>create();
    AccessibilityHierarchyAndroid hierarchy =
        AccessibilityHierarchyAndroid.newBuilder(actualRoot)
            .setViewOriginMap(mapFromElementIdToView)
            .setObtainCharacterLocations(obtainCharacterLocations)
            .build();

    // Although we captured our hierarchy from the actual root view, we pass along information
    // about the provided "root" in order to constrain evaluation to the provided sub-hierarchy.
    Long rootId = mapFromElementIdToView.inverse().get(root);
    ViewHierarchyElement evalRoot = (rootId != null) ? hierarchy.getViewById(rootId) : null;
    if (evalRoot == null) {
      LogUtils.e(TAG, "Unable to determine root View for evaluation, using full hierarchy.");
    }

    // Run each check and collect the results.
    ImmutableList.Builder<AccessibilityViewCheckResult> results = ImmutableList.builder();
    for (AccessibilityHierarchyCheck check : checks) {
      List<AccessibilityHierarchyCheckResult> hierarchyCheckResults =
          check.runCheckOnHierarchy(hierarchy, evalRoot, parameters);
      for (AccessibilityHierarchyCheckResult hierarchyCheckResult : hierarchyCheckResults) {
        // Try to map each element back to its corresponding View
        ViewHierarchyElement element = hierarchyCheckResult.getElement();
        View checkedView =
            (element != null) ? mapFromElementIdToView.get(element.getCondensedUniqueId()) : null;
        results.add(
            new AccessibilityViewCheckResult(check.getClass(), hierarchyCheckResult, checkedView));
      }
    }
    return results.build();
  }

  /**
   * Runs a set of AccessibilityViewHierarchyChecks on a View.
   *
   * @param checks The checks to be run.
   * @param root The root view of the hierarchy to check.
   * @param parameters Optional input data or preferences.
   * @return A list of interesting results encountered while running the checks. The list will be
   *     empty if the checks pass without incident.
   */
  public ImmutableList<AccessibilityViewCheckResult> runViewChecksOnView(
      ImmutableSet<AccessibilityViewHierarchyCheck> checks,
      View root,
      @Nullable Parameters parameters) {
    return runChecksOnView(convertViewChecks(checks), root, parameters);
  }

  /**
   * Converts a set of AccessibilityViewHierarchyChecks - each assumed to be an {@link
   * AccessibilityCheckPresetAndroid.DelegatedViewHierarchyCheck} - to a set of the wrapped
   * AccessibilityHierarchyChecks.
   */
  private static ImmutableSet<AccessibilityHierarchyCheck> convertViewChecks(
      ImmutableSet<AccessibilityViewHierarchyCheck> viewChecks) {
    ImmutableSet.Builder<AccessibilityHierarchyCheck> checks = ImmutableSet.builder();
    for (AccessibilityViewHierarchyCheck viewCheck : viewChecks) {
      checks.add(
          ((AccessibilityCheckPresetAndroid.DelegatedViewHierarchyCheck) viewCheck)
              .getAccessibilityHierarchyCheck());
    }
    return checks.build();
  }
}

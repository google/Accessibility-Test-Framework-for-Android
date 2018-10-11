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
import android.util.Log;
import android.view.View;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.googlecode.eyesfree.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Base class to check the accessibility of all {@link View}s in a hierarchy.
 *
 * @deprecated New accessibility checks should use {@link AccessibilityHierarchyCheck} to evaluate
 *             an {@link AccessibilityHierarchy} rather than a hierarchy of
 *             {@link View}s directly.  ATF integrations using
 *             {@link AccessibilityViewHierarchyCheck} sublcasses may continue to do so, but may
 *             incur additional overhead during execution, since an {@link AccessibilityHierarchy}
 *             is captured implicitly with each call to
 *             {@link #runCheckOnViewHierarchy(View, Metadata)}
 */
@Deprecated
public abstract class AccessibilityViewHierarchyCheck extends AccessibilityCheck {

  public AccessibilityViewHierarchyCheck() {
  }

  /**
   * Run the check on the view.
   *
   * @param root The non-null root view of the hierarchy to check.
   * @param metadata An optional {@link Metadata} that may contain check metadata defined by {@link
   *     AccessibilityCheckMetadata}.
   * @return A list of interesting results encountered while running the check. The list will be
   *     empty if the check passes without incident.
   */
  public abstract List<AccessibilityViewCheckResult> runCheckOnViewHierarchy(
      View root, @Nullable Metadata metadata);

  /**
   * @see AccessibilityViewHierarchyCheck#runCheckOnViewHierarchy(View, Metadata)
   */
  public List<AccessibilityViewCheckResult> runCheckOnViewHierarchy(View root) {
    return runCheckOnViewHierarchy(root, null);
  }

  /**
   * Delegate a legacy view based check to a more complete AccessibilityHierarchyCheck
   *
   * @param root The root view of the hierarchy to check.
   * @param fromCheck The legacy view which calls this method.
   * @param toCheck The AccessibilityHierarchyCheck to be run.
   * @param metadata An optional {@link Metadata} that may contain check metadata defined by {@link
   *     AccessibilityCheckMetadata}.
   * @return A list of interesting results encountered while running the check. The list will be
   *     empty if the check passes without incident.
   */
  @SuppressWarnings("deprecation") // AccessibilityViewCheckResult used for legacy check delegation
  protected List<AccessibilityViewCheckResult> runDelegationCheckOnView(
      View root,
      AccessibilityCheck fromCheck,
      AccessibilityHierarchyCheck toCheck,
      @Nullable Metadata metadata) {
    Locale locale = Locale.getDefault();

    // Construct the AccessibilityHierarchy from the actual view root, as to capture all available
    // information within the view hierarchy.
    View actualRoot = root.getRootView();
    BiMap<Long, View> mapFromElementIdToView = HashBiMap.<Long, View>create();
    AccessibilityHierarchy hierarchy =
        AccessibilityHierarchy
            .newBuilder(actualRoot)
            .setViewOriginMap(mapFromElementIdToView)
            .build();

    // Although we captured our hierarchy from the actual root view, we pass along information about
    // the provided "root" in order to constrain evaluation to the provided sub-hierarchy.
    Long rootId = mapFromElementIdToView.inverse().get(root);
    ViewHierarchyElement evalRoot = (rootId != null) ? hierarchy.getViewById(rootId) : null;
    if (evalRoot == null) {
      LogUtils.log(this, Log.ERROR,
          "Unable to determine root during accessibility check delegation, using full hierarchy.");
    }

    // Run the delegated check
    List<AccessibilityHierarchyCheckResult> hierarchyCheckResults =
        toCheck.runCheckOnHierarchy(hierarchy, evalRoot, metadata);

    // Remap results to the original format
    ArrayList<AccessibilityViewCheckResult> results = new ArrayList<>(hierarchyCheckResults.size());
    for (AccessibilityHierarchyCheckResult hierarchyCheckResult : hierarchyCheckResults) {
      ViewHierarchyElement element = hierarchyCheckResult.getElement();
      View checkedView = (element != null)
          ? mapFromElementIdToView.get(element.getCondensedUniqueId()) : null;
      results.add(new AccessibilityViewCheckResult(fromCheck.getClass(),
          hierarchyCheckResult.getType(),
          hierarchyCheckResult.getMessage(locale),
          checkedView));
    }

    return results;
  }
}

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

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.googlecode.eyesfree.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Base class to check the accessibility of all {@link AccessibilityNodeInfo}s in a hierarchy.
 *
 * @deprecated New accessibility checks should use {@link AccessibilityHierarchyCheck} to evaluate
 *             an {@link AccessibilityHierarchy} rather than a hierarchy of
 *             {@link AccessibilityNodeInfo}s directly.  ATF integrations using
 *             {@link AccessibilityInfoHierarchyCheck} sublcasses may continue to do so, but may
 *             incur additional overhead during execution, since an {@link AccessibilityHierarchy}
 *             is captured implicitly with each call to
 *             {@link #runCheckOnInfoHierarchy(AccessibilityNodeInfo, Context, Metadata)}
 */
@Deprecated
public abstract class AccessibilityInfoHierarchyCheck extends AccessibilityCheck {

  public AccessibilityInfoHierarchyCheck() {
  }

  /**
   * Run the check on the info.
   *
   * @param root The non-null root of the {@link AccessibilityNodeInfo} hierarchy to check.
   * @param context The non-null {@link Context} from which certain aspects of a device's state will
   *     be captured
   * @param metadata An optional {@link Metadata} that may contain check metadata defined by {@link
   *     AccessibilityCheckMetadata}.
   * @return A list of interesting results encountered while running the check. The list will be
   *     empty if the check passes without incident.
   */
  public abstract List<AccessibilityInfoCheckResult> runCheckOnInfoHierarchy(
      AccessibilityNodeInfo root, Context context, @Nullable Metadata metadata);

  /**
   * @see AccessibilityInfoHierarchyCheck#runCheckOnInfoHierarchy(AccessibilityNodeInfo, Context,
   *      Metadata)
   */
  public List<AccessibilityInfoCheckResult> runCheckOnInfoHierarchy(
      AccessibilityNodeInfo root, Context context) {
    return runCheckOnInfoHierarchy(root, context, null);
  }

  /**
   * Delegate a legacy {@link AccessibilityNodeInfo} based check to a more recent {@link
   * AccessibilityHierarchyCheck}
   *
   * @param root The root node of the hierarchy to check
   * @param fromCheck The legacy check which calls this method
   * @param toCheck The AccessibilityHierarchyCheck to be run
   * @param context The {@link Context} used to capture hierarchy and device state
   * @param metadata An optional {@link Metadata} that may contain check metadata defined by {@link
   *     AccessibilityCheckMetadata}
   * @return A list of interesting results encountered while running the check. The list will be
   *     empty if the check passes without incident
   */
  @SuppressWarnings("deprecation") // AccessibilityInfoCheckResult used for legacy check delegation
  protected List<AccessibilityInfoCheckResult> runDelegationCheckOnInfo(
      AccessibilityNodeInfo root,
      AccessibilityCheck fromCheck,
      AccessibilityHierarchyCheck toCheck,
      Context context,
      @Nullable Metadata metadata) {
    Locale locale = Locale.ENGLISH;

    // Construct the AccessibilityHierarchy from the actual root, as to capture all available
    // information within the hierarchy.
    AccessibilityNodeInfo current = AccessibilityNodeInfo.obtain(root);
    AccessibilityNodeInfo next = current.getParent();
    while (next != null) {
      current.recycle();
      current = next;
      next = next.getParent();
    }
    AccessibilityNodeInfo actualRoot = current;
    BiMap<Long, AccessibilityNodeInfo> mapFromElementIdToInfo =
        HashBiMap.<Long, AccessibilityNodeInfo>create();
    AccessibilityHierarchy hierarchy =
        AccessibilityHierarchy
            .newBuilder(actualRoot, context)
            .setNodeInfoOriginMap(mapFromElementIdToInfo)
            .build();

    // Although we captured our hierarchy from the actual root view, we pass along information about
    // the provided "root" in order to constrain evaluation to the provided sub-hierarchy.
    Long rootId = mapFromElementIdToInfo.inverse().get(root);
    ViewHierarchyElement evalRoot = (rootId != null) ? hierarchy.getViewById(rootId) : null;
    if (evalRoot == null) {
      LogUtils.log(this, Log.ERROR,
          "Unable to determine root during accessibility check delegation, using full hierarchy.");
    }

    // Run the delegated check
    List<AccessibilityHierarchyCheckResult> hierarchyCheckResults =
        toCheck.runCheckOnHierarchy(hierarchy, evalRoot, metadata);

    // Remap results to the original format
    ArrayList<AccessibilityInfoCheckResult> results = new ArrayList<>(hierarchyCheckResults.size());
    for (AccessibilityHierarchyCheckResult hierarchyCheckResult : hierarchyCheckResults) {
      ViewHierarchyElement element = hierarchyCheckResult.getElement();
      AccessibilityNodeInfo checkedInfo = (element != null)
          ? mapFromElementIdToInfo.get(element.getCondensedUniqueId()) : null;
      results.add(new AccessibilityInfoCheckResult(fromCheck.getClass(),
          hierarchyCheckResult.getType(),
          hierarchyCheckResult.getMessage(locale),
          checkedInfo));
    }

    return results;
  }
}

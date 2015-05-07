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
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.accessibility.AccessibilityNodeInfo;

import com.googlecode.eyesfree.utils.AccessibilityNodeInfoUtils;
import com.googlecode.eyesfree.utils.NodeFilter;

import java.util.List;

/**
 * Base class to check the accessibility of all {@code Info}s in a hierarchy.
 */
public abstract class AccessibilityInfoHierarchyCheck extends AccessibilityCheck {
  private static final NodeFilter WIDE_OPEN_FILTER = new NodeFilter() {
    @Override
    public boolean accept(Context context, AccessibilityNodeInfoCompat node) {
      return true;
    }
  };

  public AccessibilityInfoHierarchyCheck() {
  }

  /**
   * Run the check on the info.
   *
   * @param root The root of the {@link AccessibilityNodeInfo} hierarchy to check.
   * @param context The context of the service.
   * @param metadata An optional {@link Bundle} that may contain check metadata defined by
   *        {@link AccessibilityCheckMetadata}.
   * @return A list of interesting results encountered while running the check. The list will be
   *         empty if the check passes without incident.
   */
  public abstract List<AccessibilityInfoCheckResult> runCheckOnInfoHierarchy(
      AccessibilityNodeInfo root, Context context, Bundle metadata);

  /**
   * @see AccessibilityInfoHierarchyCheck#runCheckOnInfoHierarchy(AccessibilityNodeInfo, Context,
   *      Bundle)
   */
  public List<AccessibilityInfoCheckResult> runCheckOnInfoHierarchy(
      AccessibilityNodeInfo root, Context context) {
    return runCheckOnInfoHierarchy(root, context, null);
  }

  static List<AccessibilityNodeInfoCompat> getAllInfoCompatsInHierarchy(Context context,
      AccessibilityNodeInfo root) {
    return AccessibilityNodeInfoUtils.searchAllFromBfs(context,
        new AccessibilityNodeInfoCompat(root),
        AccessibilityInfoHierarchyCheck.WIDE_OPEN_FILTER);
  }
}

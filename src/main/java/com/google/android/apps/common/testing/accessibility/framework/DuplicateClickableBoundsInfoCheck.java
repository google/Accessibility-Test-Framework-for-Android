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

package com.google.android.apps.common.testing.accessibility.framework;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Developers sometimes have containers marked clickable when they don't process click events.
 * This error is difficult to detect, but when a container shares its bounds with a child view,
 * that is a clear error. This class catches that case.
 */
public class DuplicateClickableBoundsInfoCheck extends AccessibilityInfoHierarchyCheck {

  @Override
  public List<AccessibilityInfoCheckResult> runCheckOnInfoHierarchy(AccessibilityNodeInfo root,
      Context context, Bundle metadata) {
    List<AccessibilityInfoCheckResult> results = new ArrayList<>(1);
    Map<Rect, AccessibilityNodeInfo> clickableRectToInfoMap = new HashMap<>();

    checkForDuplicateClickableViews(root, clickableRectToInfoMap, results);
    for (AccessibilityNodeInfo info : clickableRectToInfoMap.values()) {
      info.recycle();
    }
    return results;
  }

  private void checkForDuplicateClickableViews(AccessibilityNodeInfo root,
      Map<Rect, AccessibilityNodeInfo> clickableRectToInfoMap,
      List<AccessibilityInfoCheckResult> results) {
    /*
     * TODO(pweaver) It may be possible for this check to false-negative if one view is marked
     * clickable and the other is only long clickable and/or has custom actions. Determine if this
     * limitation applies to real UIs.
     */
    if (root.isClickable() && root.isVisibleToUser()) {
      Rect bounds = new Rect();
      root.getBoundsInScreen(bounds);
      if (clickableRectToInfoMap.containsKey(bounds)) {
        results.add(new AccessibilityInfoCheckResult(this.getClass(),
            AccessibilityCheckResultType.ERROR,
            "Clickable view has same bounds as another clickable view (likely a descendent)",
            clickableRectToInfoMap.get(bounds)));
      } else {
        clickableRectToInfoMap.put(bounds, AccessibilityNodeInfo.obtain(root));
      }
    }

    for (int i = 0; i < root.getChildCount(); ++i) {
      AccessibilityNodeInfo child = root.getChild(i);
      checkForDuplicateClickableViews(child, clickableRectToInfoMap, results);
      child.recycle();
    }
  }
}

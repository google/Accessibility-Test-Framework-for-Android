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

import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Developers sometimes have containers marked clickable when they don't process click events.
 * This error is difficult to detect, but when a container shares its bounds with a child view,
 * that is a clear error. This class catches that case.
 */
public class DuplicateClickableBoundsViewCheck extends AccessibilityViewHierarchyCheck {

  @Override
  public List<AccessibilityViewCheckResult> runCheckOnViewHierarchy(View root) {
    List<AccessibilityViewCheckResult> results = new ArrayList<>(1);
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
      results.add(new AccessibilityViewCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN,
          "This check only runs on Android 2.3.3 and above.",
          root));
      return results;
    }
    Map<Rect, View> clickableRectToViewMap = new HashMap<>();

    checkForDuplicateClickableViews(root, clickableRectToViewMap, results);
    return results;
  }

  private void checkForDuplicateClickableViews(View root, Map<Rect, View> clickableRectToViewMap,
      List<AccessibilityViewCheckResult> results) {
    if (!ViewAccessibilityUtils.isVisibleToUser(root)) {
      return;
    }
    if (root.isClickable() && ViewAccessibilityUtils.isImportantForAccessibility(root)) {
      Rect bounds = new Rect();
      if (root.getGlobalVisibleRect(bounds)) {
        if (clickableRectToViewMap.containsKey(bounds)) {
          results.add(new AccessibilityViewCheckResult(this.getClass(),
              AccessibilityCheckResultType.ERROR,
              "Clickable view has same bounds as another clickable view (likely a descendent)",
              clickableRectToViewMap.get(bounds)));
        } else {
          clickableRectToViewMap.put(bounds, root);
        }
      }
    }
    if (!(root instanceof ViewGroup)) {
      return;
    }
    ViewGroup viewGroup = (ViewGroup) root;
    for (int i = 0; i < viewGroup.getChildCount(); ++i) {
      View child = viewGroup.getChildAt(i);
      checkForDuplicateClickableViews(child, clickableRectToViewMap, results);
    }
  }
}

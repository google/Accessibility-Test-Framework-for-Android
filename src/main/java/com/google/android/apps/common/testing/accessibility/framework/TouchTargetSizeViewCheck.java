/*
 * Copyright (C) 2014 Google Inc.
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

import android.view.View;
import android.view.ViewParent;

import java.util.ArrayList;
import java.util.List;

/**
 * Check to ensure that a view has a touch target that is at least 48x48dp.
 */
public class TouchTargetSizeViewCheck extends AccessibilityViewCheck {

  /**
   * Minimum height and width are set according to <a
   * href="http://developer.android.com/design/patterns/accessibility.html"></a>
   */
  private static final int TOUCH_TARGET_MIN_HEIGHT = 48;
  private static final int TOUCH_TARGET_MIN_WIDTH = 48;

  @Override
  public List<AccessibilityViewCheckResult> runCheckOnView(View view) {
    ArrayList<AccessibilityViewCheckResult> results = new ArrayList<AccessibilityViewCheckResult>();

    if (!(view.isClickable() || view.isLongClickable())) {
      results.add(new AccessibilityViewCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "View is not clickable", view));
      return results;
    }

    if (!ViewAccessibilityUtils.isVisibleToUser(view)) {
      results.add(new AccessibilityViewCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "View is not visible", view));
      return results;
    }

    // dp calculation is pixels/density
    float density = view.getContext().getResources().getDisplayMetrics().density;
    float targetHeight = view.getHeight() / density;
    float targetWidth = view.getWidth() / density;

    if (targetHeight < TOUCH_TARGET_MIN_HEIGHT || targetWidth < TOUCH_TARGET_MIN_WIDTH) {
      // Before we know a view fails this check, we must check if one of the view's ancestors may be
      // handling touches on its behalf.
      boolean hasDelegate = hasAncestorWithTouchDelegate(view);

      // We can't get the delegated view from a TouchDelegate, so any TouchDelegate in the view's
      // lineage will demote ERROR to WARNING.
      AccessibilityCheckResultType resultType =
          hasDelegate ? AccessibilityCheckResultType.WARNING : AccessibilityCheckResultType.ERROR;

      StringBuilder messageBuilder = new StringBuilder(String.format(
          "View falls below the minimum recommended size for touch targets. Minimum touch target "
          + "size is %dx%ddp. Actual size is %.1fx%.1fdp (screen density is %.1f).",
          TOUCH_TARGET_MIN_WIDTH,
          TOUCH_TARGET_MIN_HEIGHT,
          targetWidth,
          targetHeight,
          density));
      if (hasDelegate) {
        messageBuilder.append(
            " A TouchDelegate has been detected on one of this view's ancestors. If the delegate "
            + "is of sufficient size and handles touches for this view, this warning may be "
            + "ignored.");
      }

      results.add(
          new AccessibilityViewCheckResult(this.getClass(), resultType, messageBuilder, view));
    }

    return results;
  }

  private static boolean hasAncestorWithTouchDelegate(View view) {
    if (view == null) {
      return false;
    }

    View evalView = null;
    ViewParent parent = view.getParent();
    if (parent instanceof View) {
      evalView = (View) parent;
      if (evalView.getTouchDelegate() != null) {
        return true;
      }
    }

    return hasAncestorWithTouchDelegate(evalView);
  }
}

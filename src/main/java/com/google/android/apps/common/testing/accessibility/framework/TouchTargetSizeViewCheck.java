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

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Check to ensure that a view has a touch target that is at least 48x48dp.
 */
public class TouchTargetSizeViewCheck extends AccessibilityViewCheck {

  /**
   * Minimum height and width are set according to
   * <a href="http://developer.android.com/design/patterns/accessibility.html"></a>
   */
  private static final int TOUCH_TARGET_MIN_HEIGHT = 48;
  private static final int TOUCH_TARGET_MIN_WIDTH = 48;

  @Override
  public List<AccessibilityViewCheckResult> runCheckOnView(View view) {
    ArrayList<AccessibilityViewCheckResult> results = new ArrayList<AccessibilityViewCheckResult>();

    // dp calculation is pixels/density
    float density = view.getContext().getResources().getDisplayMetrics().density;
    float targetHeight = view.getHeight() / density;
    float targetWidth = view.getWidth() / density;
    // If this object passes touches to a different (possibly larger) object, don't check its bounds
    if (view.getTouchDelegate() == null) {
      if (targetHeight < TOUCH_TARGET_MIN_HEIGHT || targetWidth < TOUCH_TARGET_MIN_WIDTH) {
        String message = String.format("View is too small of a touch target. Minimum touch target "
            + "size is %dx%ddp. Actual size is %.1fx%.1fdp (screen density is %.1f).",
            TOUCH_TARGET_MIN_WIDTH, TOUCH_TARGET_MIN_HEIGHT, targetWidth, targetHeight, density);
        results.add(new AccessibilityViewCheckResult(
            this, AccessibilityCheckResultType.ERROR, message, view));
      }
    } else {
      results.add(new AccessibilityViewCheckResult(this, AccessibilityCheckResultType.NOT_RUN,
          "View must not have a TouchDelegate", view));
    }
    return results;
  }
}

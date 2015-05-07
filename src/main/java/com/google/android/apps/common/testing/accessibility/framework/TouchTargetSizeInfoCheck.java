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

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.accessibility.AccessibilityNodeInfo;

import com.googlecode.eyesfree.utils.AccessibilityNodeInfoUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Check to ensure that a view has a touch target that is at least 48x48dp.
 */
public class TouchTargetSizeInfoCheck extends AccessibilityInfoCheck {

  /**
   * Minimum height and width are set according to <a
   * href="http://developer.android.com/design/patterns/accessibility.html"></a>
   */
  private static final int TOUCH_TARGET_MIN_HEIGHT = 48;
  private static final int TOUCH_TARGET_MIN_WIDTH = 48;

  @Override
  public List<AccessibilityInfoCheckResult> runCheckOnInfo(AccessibilityNodeInfo info,
      Context context, Bundle metadata) {
    ArrayList<AccessibilityInfoCheckResult> results = new ArrayList<AccessibilityInfoCheckResult>();

    // TODO(sjrush): Have all info checks use AccessibilityNodeInfoCompat
    AccessibilityNodeInfoCompat infoCompat = new AccessibilityNodeInfoCompat(info);
    if (!(AccessibilityNodeInfoUtils.isClickable(infoCompat)
          || AccessibilityNodeInfoUtils.isLongClickable(infoCompat))) {
      results.add(new AccessibilityInfoCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "View is not clickable", info));
      return results;
    }
    if (context == null) {
      results.add(new AccessibilityInfoCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "This check needs a context", info));
      return results;
    }

    // TODO(sjrush): Find a way to make this check work without a context
    // dp calculation is pixels/density
    float density = context.getResources().getDisplayMetrics().density;
    Rect bounds = new Rect();
    info.getBoundsInScreen(bounds);
    float targetHeight = bounds.height() / density;
    float targetWidth = bounds.width() / density;
    if (targetHeight < TOUCH_TARGET_MIN_HEIGHT || targetWidth < TOUCH_TARGET_MIN_WIDTH) {
      String message = String.format(Locale.US,
          "View is too small of a touch target. Minimum touch target size is %dx%ddp. "
          + "Actual size is %.1fx%.1fdp (screen density is %.1f).",
          TOUCH_TARGET_MIN_WIDTH,
          TOUCH_TARGET_MIN_HEIGHT,
          targetWidth,
          targetHeight,
          density);
      results.add(new AccessibilityInfoCheckResult(this.getClass(),
          AccessibilityCheckResultType.ERROR, message, info));
    }
    return results;
  }
}

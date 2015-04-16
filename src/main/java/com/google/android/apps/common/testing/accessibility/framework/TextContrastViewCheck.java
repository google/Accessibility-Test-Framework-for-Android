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

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.googlecode.eyesfree.utils.ContrastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Check to ensure that a TextView has sufficient contrast between text color and background color
 */
public class TextContrastViewCheck extends AccessibilityViewCheck {

  @Override
  public List<AccessibilityViewCheckResult> runCheckOnView(View view) {
    ArrayList<AccessibilityViewCheckResult> results = new ArrayList<AccessibilityViewCheckResult>();
    // TODO(caseyburkhardt): Implement this for other types of views
    if ((view instanceof TextView) && (!TextUtils.isEmpty(((TextView) view).getText()))) {
      TextView textView = (TextView) view;
      int textColor = textView.getCurrentTextColor();
      Drawable background = textView.getBackground();
      // Can only check if the background is a single color.
      // TODO(caseyburkhardt): Allow checking of views with more complex backgrounds.
      if (background instanceof ColorDrawable) {
        // ColorDrawable.getColor() was introduced in honeycomb, cannot get color without it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
          int backgroundColor = ((ColorDrawable) background).getColor();
          double contrast = ContrastUtils.calculateContrastRatio(
              ContrastUtils.calculateLuminance(textColor),
              ContrastUtils.calculateLuminance(backgroundColor));
          double backgroundAlpha = Color.alpha(backgroundColor);
          double requiredContrast =
              isLargeText(textView) ? ContrastUtils.CONTRAST_RATIO_WCAG_LARGE_TEXT
                  : ContrastUtils.CONTRAST_RATIO_WCAG_NORMAL_TEXT;
          if (contrast < requiredContrast) {
            if (backgroundAlpha < 255) {
              // Cannot guarantee contrast ratio if the background is not opaque
              String message = "View's background color must be opaque";
              results.add(new AccessibilityViewCheckResult(this.getClass(),
                  AccessibilityCheckResultType.NOT_RUN, message, view));
            } else {
              String message = String.format(Locale.US,
                  "TextView does not have required contrast of " + "%f. Actual contrast is %f",
                  requiredContrast, contrast);
              results.add(new AccessibilityViewCheckResult(this.getClass(),
                  AccessibilityCheckResultType.ERROR, message, view));
            }
          }
        } else {
          String message = "Cannot be run on API levels lower than 11";
          results.add(new AccessibilityViewCheckResult(this.getClass(),
              AccessibilityCheckResultType.NOT_RUN, message, view));
        }
      } else { // non-solid background color
        results.add(new AccessibilityViewCheckResult(this.getClass(),
            AccessibilityCheckResultType.NOT_RUN, "TextView does not have a solid background color",
            view));
      }
    } else {
      results.add(new AccessibilityViewCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "View must be a non-empty TextView", view));
    }
    return results;
  }

  /**
   * Given a TextView, returns true if it contains text which is large for contrast purposes as
   * defined at http://www.w3.org/TR/2008/REC-WCAG20-20081211/#larger-scaledef
   */
  private static boolean isLargeText(TextView textView) {
    float textSize = textView.getTextSize();
    if ((textSize >= ContrastUtils.WCAG_LARGE_TEXT_MIN_SIZE) || (
        (textSize >= ContrastUtils.WCAG_LARGE_BOLD_TEXT_MIN_SIZE)
        && textView.getTypeface().isBold())) {
      return true;
    }
    return false;
  }
}

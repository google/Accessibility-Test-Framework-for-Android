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

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Check to ensure that an editable TextView is not labeled by a contentDescription
 */
public class EditableContentDescViewCheck extends AccessibilityViewCheck {

  @Override
  public List<AccessibilityViewCheckResult> runCheckOnView(View view) {
    List<AccessibilityViewCheckResult> results = new ArrayList<AccessibilityViewCheckResult>(1);
    if (view instanceof TextView) {
      TextView textView = (TextView) view;
      if ((textView.getEditableText() != null)) {
        if (!TextUtils.isEmpty(textView.getContentDescription())) {
          results.add(new AccessibilityViewCheckResult(this.getClass(),
              AccessibilityCheckResultType.ERROR,
              "Editable TextView should not have a contentDescription.", textView));
        }
      } else {
        results.add(new AccessibilityViewCheckResult(this.getClass(),
            AccessibilityCheckResultType.NOT_RUN, "TextView must be editable", textView));
      }
    } else {
      results.add(new AccessibilityViewCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "View must be a TextView", view));
    }

    return results;
  }
}

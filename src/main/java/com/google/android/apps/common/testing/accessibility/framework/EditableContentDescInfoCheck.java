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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;

import com.googlecode.eyesfree.utils.AccessibilityNodeInfoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Check to ensure that editable nodes are not also labeled with a contentDescription
 */
public class EditableContentDescInfoCheck extends AccessibilityInfoCheck {

  @Override
  public List<AccessibilityInfoCheckResult> runCheckOnInfo(AccessibilityNodeInfo info,
      Context context, Bundle metadata) {
    List<AccessibilityInfoCheckResult> results = new ArrayList<AccessibilityInfoCheckResult>(1);
    AccessibilityNodeInfoCompat compatInfo = new AccessibilityNodeInfoCompat(info);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      if (info.isEditable()) {
        if (!TextUtils.isEmpty(info.getContentDescription())) {
          results.add(new AccessibilityInfoCheckResult(this.getClass(),
              AccessibilityCheckResultType.ERROR,
              "Editable view should not have a contentDescription", info));
        }
      } else {
        results.add(new AccessibilityInfoCheckResult(this.getClass(),
            AccessibilityCheckResultType.NOT_RUN, "Associated view must be editable", info));
      }
    } else {
      // Earlier API versions don't allow us to filter based on editable state, so we'll fall back
      // to using EditTexts instead.

      // TODO(caseyburkhardt): The missing context below will cause us to fail to resolve classes
      // defined within other packages.
      if (AccessibilityNodeInfoUtils.nodeMatchesAnyClassByType(null, compatInfo, EditText.class)) {
        if (!TextUtils.isEmpty(compatInfo.getContentDescription())) {
          results.add(new AccessibilityInfoCheckResult(this.getClass(),
              AccessibilityCheckResultType.ERROR, "EditText should not have a contentDescription",
              info));
        }
      } else {
        results.add(new AccessibilityInfoCheckResult(this.getClass(),
            AccessibilityCheckResultType.NOT_RUN, "Associated view must be an EditText", info));
      }
    }
    return results;
  }
}

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
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.ScrollView;

import com.googlecode.eyesfree.utils.AccessibilityNodeInfoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Check to ensure that an info has speakable text for a screen reader
 */
public class SpeakableTextPresentInfoCheck extends AccessibilityInfoCheck {
  private static List<Class<? extends ViewGroup>> blacklistedViewTypes =
      /*
       * TODO(pweaver) Revisit this list once we have robust testing for info checks. It should
       * only contain classes that can't be handled any other way. ShouldFocusNode likely
       * handles many of them, for example.
       */
      Arrays.asList(ListView.class, ScrollView.class, ViewPager.class, WebView.class);

  @Override
  public List<AccessibilityInfoCheckResult> runCheckOnInfo(AccessibilityNodeInfo info,
      Context context, Bundle metadata) {
    List<AccessibilityInfoCheckResult> results = new ArrayList<AccessibilityInfoCheckResult>();
    AccessibilityNodeInfoCompat compatInfo = new AccessibilityNodeInfoCompat(info);
    for (Class<? extends ViewGroup> clazz : blacklistedViewTypes) {
      if (AccessibilityNodeInfoUtils.nodeMatchesAnyClassByType(null, compatInfo, clazz)) {
        String msg =
            String.format("Views of type %s are not checked for speakable text.", clazz.getName());
        results.add(new AccessibilityInfoCheckResult(this.getClass(),
            AccessibilityCheckResultType.NOT_RUN, msg, info));
        return results;
      }
    }
    if (AccessibilityNodeInfoUtils.shouldFocusNode(context, compatInfo)) {
      if (TextUtils.isEmpty(AccessibilityCheckUtils.getSpeakableTextForInfo(info))) {
        results.add(new AccessibilityInfoCheckResult(this.getClass(),
            AccessibilityCheckResultType.ERROR,
            "View is missing speakable text needed for a screen reader", info));
      }
    } else {
      results.add(new AccessibilityInfoCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "View is not focused by screen readers", info));
    }
    return results;
  }

}

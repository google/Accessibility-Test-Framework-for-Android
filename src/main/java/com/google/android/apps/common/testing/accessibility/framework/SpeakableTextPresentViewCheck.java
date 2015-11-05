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

import android.os.Build;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Check to ensure that a view has a content description for a screen reader
 */
public class SpeakableTextPresentViewCheck extends AccessibilityViewCheck {
  /*
   * TODO(pweaver) Determine if we can reduce this list, and add notes for those that need to
   * be here.
   * WebView: This class doesn't behave consistently across platforms, and it can report
   * that it has no children even when it is displaying content.
   */
  private static final List<Class<? extends ViewGroup>> blacklistedViewTypes =
      Arrays.asList(ListView.class, ScrollView.class, ViewPager.class, WebView.class);

  @Override
  public List<AccessibilityViewCheckResult> runCheckOnView(View view) {
    List<AccessibilityViewCheckResult> results = new ArrayList<AccessibilityViewCheckResult>();

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
      results.add(new AccessibilityViewCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "This check only runs on Android 4.1 and above.",
          view));
      return results;
    }
    if (!ViewAccessibilityUtils.isImportantForAccessibility(view)) {
      results.add(new AccessibilityViewCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "View is not important for accessibility.", view));
      return results;
    }
    for (Class<? extends ViewGroup> clazz : blacklistedViewTypes) {
      if (clazz.isAssignableFrom(view.getClass())) {
        String msg =
            String.format("Views of type %s are not checked for speakable text.", clazz.getName());
        results.add(new AccessibilityViewCheckResult(this.getClass(),
            AccessibilityCheckResultType.NOT_RUN, msg, view));
        return results;
      }
    }
    if (shouldFocusView(view)) {
      // We must evaluate this view for speakable text
      if (TextUtils.isEmpty(AccessibilityCheckUtils.getSpeakableTextForView(view))) {
        results.add(new AccessibilityViewCheckResult(this.getClass(),
            AccessibilityCheckResultType.ERROR,
            "View is missing speakable text needed for a screen reader", view));
      }
    } else {
      results.add(new AccessibilityViewCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "View is not focused by a screen reader", view));
    }
    return results;
  }

  /* TODO(pweaver) Remove this awkward way of allowing Robolectric to use this */
  protected boolean shouldFocusView(View view) {
    return ViewAccessibilityUtils.shouldFocusView(view);
  }
}

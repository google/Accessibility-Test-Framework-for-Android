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

import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Checks to ensure that speakable text does not contain redundant information about the view's
 * type. Accessibility services are aware of the view's type and can use that information as needed
 * (ex: Screen readers may append "button" to the speakable text of a {@link Button}).
 */
public class RedundantContentDescViewCheck extends AccessibilityViewHierarchyCheck {
  private static List<CharSequence> redundantWords = new ArrayList<>();
  static {
    redundantWords.add("button");
  }

  @Override
  public List<AccessibilityViewCheckResult> runCheckOnViewHierarchy(View root) {
    List<AccessibilityViewCheckResult> results = new ArrayList<AccessibilityViewCheckResult>();
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
      results.add(new AccessibilityViewCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "This check only runs on Android 4.1 and above.",
          root));
      return results;
    }
    // TODO(sjrush): This check needs internationalization support
    if (!Locale.getDefault().getLanguage().equals(Locale.ENGLISH.getLanguage())) {
      results.add(new AccessibilityViewCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "This check only runs in English locales", root));
      return results;
    }

    for (View view : ViewAccessibilityUtils.getAllViewsInHierarchy(root)) {
      if (!ViewAccessibilityUtils.isImportantForAccessibility(view)) {
        results.add(new AccessibilityViewCheckResult(this.getClass(),
            AccessibilityCheckResultType.NOT_RUN, "View is not important for accessibility", view));
        continue;
      }
      CharSequence contentDescription = view.getContentDescription();
      if (TextUtils.isEmpty(contentDescription)) {
        results.add(new AccessibilityViewCheckResult(this.getClass(),
            AccessibilityCheckResultType.NOT_RUN, "View has no content description", view));
        continue;
      }
      for (CharSequence redundantWord : redundantWords) {
        if (contentDescription.toString().toLowerCase().contains(redundantWord)) {
          results.add(new AccessibilityViewCheckResult(this.getClass(),
              AccessibilityCheckResultType.WARNING,
              "View's speakable text ends with view type",
              view));
        }
      }
    }
    return results;
  }
}

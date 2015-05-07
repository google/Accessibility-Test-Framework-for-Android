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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Checks to ensure that speakable text does not contain redundant information about the view's
 * type. Accessibility services are aware of the view's type and can use that information as needed
 * (ex: Screen readers may append "button" to the speakable text of a {@link Button}).
 */
public class RedundantContentDescInfoCheck extends AccessibilityInfoHierarchyCheck {
  private static List<CharSequence> redundantWords = new ArrayList<>();
  static {
    redundantWords.add("button");
  }

  @Override
  public List<AccessibilityInfoCheckResult> runCheckOnInfoHierarchy(AccessibilityNodeInfo root,
      Context context, Bundle metadata) {
    List<AccessibilityInfoCheckResult> results = new ArrayList<AccessibilityInfoCheckResult>();
    // TODO(sjrush): This check needs internationalization support
    if (!Locale.getDefault().getLanguage().equals(Locale.ENGLISH.getLanguage())) {
      results.add(new AccessibilityInfoCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "This check only runs in English locales", root));
      return results;
    }

    List<AccessibilityNodeInfoCompat> compatInfos = getAllInfoCompatsInHierarchy(context, root);
    for (AccessibilityNodeInfoCompat compatInfo : compatInfos) {
      AccessibilityNodeInfo info = (AccessibilityNodeInfo) compatInfo.getInfo();
      CharSequence contentDescription = info.getContentDescription();
      if (TextUtils.isEmpty(contentDescription)) {
        results.add(new AccessibilityInfoCheckResult(this.getClass(),
            AccessibilityCheckResultType.NOT_RUN, "View has no content description", info));
        continue;
      }
      for (CharSequence redundantWord : redundantWords) {
        if (contentDescription.toString().toLowerCase().contains(redundantWord)) {
          results.add(new AccessibilityInfoCheckResult(this.getClass(),
              AccessibilityCheckResultType.WARNING,
              "View's speakable text ends with view type",
              info));
          break;
        }
      }
    }
    return results;
  }
}

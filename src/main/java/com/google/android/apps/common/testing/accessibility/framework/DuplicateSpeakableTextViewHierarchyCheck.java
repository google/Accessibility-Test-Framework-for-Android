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
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * If two Views in a hierarchy have the same speakable text, that could be confusing for users. Two
 * Views with the same text, and at least one of them is clickable we warn in that situation. If we
 * find two non-clickable Views with the same speakable text, we report that fact as info. If no
 * Views in the hierarchy have any speakable text, we report that the test was not run.
 */
public class DuplicateSpeakableTextViewHierarchyCheck extends AccessibilityViewHierarchyCheck {

  @Override
  public List<AccessibilityViewCheckResult> runCheckOnViewHierarchy(View root) {
    List<AccessibilityViewCheckResult> results = new ArrayList<AccessibilityViewCheckResult>();

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
      results.add(new AccessibilityViewCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "This check only runs on Andorid 4.1 and above.",
          root));
      return results;
    }

    /* Find all text and the views that have that text */
    Set<View> allViews = ViewAccessibilityUtils.getAllViewsInHierarchy(root);
    Map<String, List<View>> textToViewMap = getSpeakableTextToViewMap(allViews);

    /* Deal with any duplicated text */
    for (String speakableText : textToViewMap.keySet()) {
      if (textToViewMap.get(speakableText).size() < 2) {
        continue; // Text is not duplicated
      }

      /* We've found duplicated text. Sort the Views into clickable and non-clickable. */
      List<View> clickableViews = new ArrayList<View>();
      List<View> nonClickableViews = new ArrayList<View>();
      for (View view : textToViewMap.get(speakableText)) {
        if (view.isClickable()) {
          clickableViews.add(view);
        } else {
          nonClickableViews.add(view);
        }
      }

      if (clickableViews.size() > 0) {
        /* Display warning */
        results.add(new AccessibilityViewCheckResult(this.getClass(),
            AccessibilityCheckResultType.WARNING, String.format(Locale.US,
                "Clickable view's speakable text: \"%s\" is identical to that of %d "
                + "other view(s)", speakableText, nonClickableViews.size()),
            clickableViews.get(0)));
        clickableViews.remove(0);
      } else {
        /* Only duplication is on non-clickable views */
        results.add(new AccessibilityViewCheckResult(this.getClass(),
            AccessibilityCheckResultType.INFO, String.format(Locale.US,
                "Non-clickable view's speakable text: \"%s\" is identical to that of %d "
                + "other non-clickable view(s)", speakableText, nonClickableViews.size() - 1),
            nonClickableViews.get(0)));
        nonClickableViews.remove(0);
      }

      /* Add infos to help track down the duplication */
      for (View clickableView : clickableViews) {
        results.add(new AccessibilityViewCheckResult(this.getClass(),
            AccessibilityCheckResultType.INFO,
            String.format("  Clickable View has speakable text: \"%s\".", speakableText),
            clickableView));
      }
      for (View clickableView : nonClickableViews) {
        results.add(new AccessibilityViewCheckResult(this.getClass(),
            AccessibilityCheckResultType.INFO,
            String.format("  Non-clickable View has speakable text: \"%s\".", speakableText),
            clickableView));
      }
    }

    if (textToViewMap.size() == 0) {
      results.add(new AccessibilityViewCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "No Views in hierarchy have speakable text", root));
      return results;
    }

    return results;
  }

  /**
   * @param allViews Set of views to index by their speakable text
   * @return map from speakable text to all views with that speakable text
   */
  private Map<String, List<View>> getSpeakableTextToViewMap(Set<View> allViews) {
    Map<String, List<View>> textToViewMap = new HashMap<String, List<View>>();

    for (View view : allViews) {
      String speakableText =
          AccessibilityCheckUtils.getSpeakableTextForView(view).toString().trim();
      if (TextUtils.isEmpty(speakableText)) {
        continue;
      }

      if (!textToViewMap.containsKey(speakableText)) {
        textToViewMap.put(speakableText, new ArrayList<View>());
      }
      textToViewMap.get(speakableText).add(view);
    }
    return textToViewMap;
  }
}

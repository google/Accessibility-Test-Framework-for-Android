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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * If two Views in a hierarchy have the same speakable text, that could be confusing for users.
 * If one of those Views is clickable and the other isn't, that's an error because a user will
 * not be able to distinguish the View that performs the desired action from the "decoy".
 * Two clickable Views can have the same speakable text if clicking the two Views produces the same
 * result. Since it is not possible to determine if they do, in fact, product the same result, we
 * warn in that situation.
 * If we find two non-clickable Views with the same speakable text, we report that fact as info.
 * If no Views in the hierarchy have any speakable text, we report that the test was not run.
 */
public class SpeakableTextUniqueViewCheck extends AccessibilityViewHierarchyCheck {

  @Override
  public List<AccessibilityViewCheckResult> runCheckOnViewHierarchy(View root) {
    List<AccessibilityViewCheckResult> results = new ArrayList<AccessibilityViewCheckResult>();

    /* Find all text and the views that have that text */
    Set<View> allViews = AccessibilityCheckUtils.getAllViewsInHierarchy(root);
    Map<CharSequence, List<View>> textToViewMap = getSpeakableTextToViewMap(allViews);

    /* Deal with any duplicated text */
    for (CharSequence speakableText : textToViewMap.keySet()) {
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

      /* Display warning or errors */
      if (clickableViews.size() > 0) {
        if (nonClickableViews.size() > 0) {
          results.add(new AccessibilityViewCheckResult(this, AccessibilityCheckResultType.ERROR,
              String.format("Clickable view's speakable text: \"%s\" is identical to that of %d "
                  + "non-clickable view(s)", speakableText.toString(),
                  nonClickableViews.size()), clickableViews.get(0)));
        } else {
          results.add(new AccessibilityViewCheckResult(this, AccessibilityCheckResultType.WARNING,
              String.format("Clickable view's speakable text: \"%s\" is identical to that of %d "
                  + "other clickable view(s)", speakableText.toString(),
                  nonClickableViews.size()), clickableViews.get(0)));
        }
        clickableViews.remove(0);
      } else {
        /* Only duplication is on non-clickable views */
        results.add(new AccessibilityViewCheckResult(this, AccessibilityCheckResultType.INFO,
            String.format("Non-clickable view's speakable text: \"%s\" is identical to that of %d "
                + "other non-clickable view(s)", speakableText.toString(),
                nonClickableViews.size() - 1), nonClickableViews.get(0)));
        nonClickableViews.remove(0);
      }

      /* Add infos to help track down the duplication */
      for (View clickableView : clickableViews) {
        results.add(new AccessibilityViewCheckResult(this, AccessibilityCheckResultType.INFO,
            String.format("  Clickable View has speakable text: \"%s\".",
                speakableText.toString()), clickableView));
      }
      for (View clickableView : nonClickableViews) {
        results.add(new AccessibilityViewCheckResult(this, AccessibilityCheckResultType.INFO,
            String.format("  Non-clickable View has speakable text: \"%s\".",
                speakableText.toString()), clickableView));
      }
    }

    if (textToViewMap.size() == 0) {
      results.add(new AccessibilityViewCheckResult(this, AccessibilityCheckResultType.NOT_RUN,
          "No Views in hierarchy have speakable text", root));
      return results;
    }

    return results;
  }

  /**
   * @param allViews Set of views to index by their speakable text
   * @return map from speakable text to all views with that speakable text
   */
  private Map<CharSequence, List<View>> getSpeakableTextToViewMap(Set<View> allViews) {
    Map<CharSequence, List<View>> textToViewMap = new HashMap<CharSequence, List<View>>();

    for (View view : allViews) {
      CharSequence speakableText = AccessibilityCheckUtils.getSpeakableTextForView(view);
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

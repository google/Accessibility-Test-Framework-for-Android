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

package com.google.android.apps.common.testing.accessibility.framework.checks;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck.Category;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.HashMapResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * If two Views in a hierarchy have the same speakable text, that could be confusing for users. Two
 * Views with the same text, and at least one of them is clickable we warn in that situation. If we
 * find two non-clickable Views with the same speakable text, we report that fact as info. If no
 * Views in the hierarchy have any speakable text, we report that the test was not run.
 */
public class DuplicateSpeakableTextCheck extends AccessibilityHierarchyCheck {
  /** Result when two clickable views have the same speakable text. */
  public static final int RESULT_ID_CLICKABLE_SAME_SPEAKABLE_TEXT = 1;
  /** Result when two non-clickable views have the same speakable text. */
  public static final int RESULT_ID_NON_CLICKABLE_SAME_SPEAKABLE_TEXT = 2;
  /** [Legacy] Info result containing a clickable view's speakable text. */
  public static final int RESULT_ID_CLICKABLE_SPEAKABLE_TEXT = 3;
  /** [Legacy] Info result containing a non-clickable view's speakable text. */
  public static final int RESULT_ID_NON_CLICKABLE_SPEAKABLE_TEXT = 4;

  /** Result metadata key for a view's speakable text {@link String} */
  public static final String KEY_SPEAKABLE_TEXT = "KEY_SPEAKABLE_TEXT";
  /** Result metadata key for the {@code int} number of other views with the same speakable text. */
  public static final String KEY_CONFLICTING_VIEW_COUNT = "KEY_CONFLICTING_VIEW_COUNT";

  @Override
  protected String getHelpTopic() {
    return "7102513"; // Duplicate descriptions
  }

  @Override
  public Category getCategory() {
    return Category.CONTENT_LABELING;
  }

  @Override
  public List<AccessibilityHierarchyCheckResult> runCheckOnHierarchy(
      AccessibilityHierarchy hierarchy,
      @Nullable ViewHierarchyElement fromRoot,
      @Nullable Parameters parameters) {
    List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();

    /* Find all text and the views that have that text throughout the full hierarchy */
    Map<String, List<ViewHierarchyElement>> textToViewMap =
        getSpeakableTextToViewMap(
            hierarchy.getActiveWindow().getAllViews(), hierarchy.getDeviceState().getLocale());

    /* Deal with any duplicated text */
    for (String speakableText : textToViewMap.keySet()) {
      if (textToViewMap.get(speakableText).size() < 2) {
        continue; // Text is not duplicated
      }

      // We've found duplicated text. Sort the Views into clickable and non-clickable if they're
      // within scope for evaluation.
      List<ViewHierarchyElement> clickableViews = new ArrayList<>();
      List<ViewHierarchyElement> nonClickableViews = new ArrayList<>();
      List<? extends ViewHierarchyElement> viewsToEval =
          (fromRoot != null) ? fromRoot.getSelfAndAllDescendants() : null;
      for (ViewHierarchyElement view : textToViewMap.get(speakableText)) {
        if ((viewsToEval == null) || viewsToEval.contains(view)) {
          if (Boolean.TRUE.equals(view.isClickable())) {
            clickableViews.add(view);
          } else {
            nonClickableViews.add(view);
          }
        }
      }

      if (!clickableViews.isEmpty()) {
        /* Display warning */
        ResultMetadata resultMetadata = new HashMapResultMetadata();
        resultMetadata.putString(
            KEY_SPEAKABLE_TEXT, speakableText);
        resultMetadata.putInt(KEY_CONFLICTING_VIEW_COUNT,
            (clickableViews.size() + nonClickableViews.size() - 1));
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.WARNING,
            clickableViews.get(0),
            RESULT_ID_CLICKABLE_SAME_SPEAKABLE_TEXT,
            resultMetadata));
        clickableViews.remove(0);
      } else if (!nonClickableViews.isEmpty()) {
        /* Only duplication is on non-clickable views */
        ResultMetadata resultMetadata = new HashMapResultMetadata();
        resultMetadata.putString(
            KEY_SPEAKABLE_TEXT, speakableText);
        resultMetadata.putInt(KEY_CONFLICTING_VIEW_COUNT,
            (clickableViews.size() + nonClickableViews.size() - 1));
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.INFO,
            nonClickableViews.get(0),
            RESULT_ID_NON_CLICKABLE_SAME_SPEAKABLE_TEXT,
            resultMetadata));
        nonClickableViews.remove(0);
      }
    }

    return results;
  }

  @Override
  public String getMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    // For each of the following result IDs, metadata will have been set on the result.
    checkNotNull(metadata);
    switch (resultId) {
      case RESULT_ID_CLICKABLE_SAME_SPEAKABLE_TEXT:
        return String.format(locale,
            StringManager.getString(locale, "result_message_same_speakable_text"),
            StringManager.getString(locale, "clickable"),
            metadata.getString(KEY_SPEAKABLE_TEXT),
            metadata.getInt(KEY_CONFLICTING_VIEW_COUNT));
      case RESULT_ID_NON_CLICKABLE_SAME_SPEAKABLE_TEXT:
        return String.format(locale,
            StringManager.getString(locale, "result_message_same_speakable_text"),
            StringManager.getString(locale, "non_clickable"),
            metadata.getString(KEY_SPEAKABLE_TEXT),
            metadata.getInt(KEY_CONFLICTING_VIEW_COUNT));
      // Legacy
      case RESULT_ID_CLICKABLE_SPEAKABLE_TEXT:
        return String.format(locale,
            StringManager.getString(locale, "result_message_speakable_text"),
            StringManager.getString(locale, "clickable"),
            metadata.getString(KEY_SPEAKABLE_TEXT));
      case RESULT_ID_NON_CLICKABLE_SPEAKABLE_TEXT:
        return String.format(locale,
            StringManager.getString(locale, "result_message_speakable_text"),
            StringManager.getString(locale, "non_clickable"),
            metadata.getString(KEY_SPEAKABLE_TEXT));
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getShortMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    switch (resultId) {
      case RESULT_ID_CLICKABLE_SAME_SPEAKABLE_TEXT:
      case RESULT_ID_NON_CLICKABLE_SAME_SPEAKABLE_TEXT:
      case RESULT_ID_CLICKABLE_SPEAKABLE_TEXT:
      case RESULT_ID_NON_CLICKABLE_SPEAKABLE_TEXT:
        return StringManager.getString(locale, "result_message_brief_same_speakable_text");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  /**
   * Calculates a secondary priority for a duplicate speakable text result.
   *
   * <p>The result is the number of other views with the same text. Thus, the greater the number of
   * repetitions, the higher the priority.
   */
  @Override
  public @Nullable Double getSecondaryPriority(AccessibilityHierarchyCheckResult result) {
    ResultMetadata metadata = result.getMetadata();
    switch (result.getResultId()) {
      case RESULT_ID_CLICKABLE_SAME_SPEAKABLE_TEXT:
      case RESULT_ID_NON_CLICKABLE_SAME_SPEAKABLE_TEXT:
        return (double) checkNotNull(metadata).getInt(KEY_CONFLICTING_VIEW_COUNT, 0);
      default:
        return null;
    }
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_duplicate_speakable_text");
  }

  /**
   * @param allViews Set of views to index by their speakable text
   * @return map from speakable text to all views with that speakable text
   */
  private Map<String, List<ViewHierarchyElement>> getSpeakableTextToViewMap(
      Collection<? extends ViewHierarchyElement> allViews, Locale locale) {
    Map<String, List<ViewHierarchyElement>> textToViewMap = new HashMap<>();

    for (ViewHierarchyElement view : allViews) {
      if (!ViewHierarchyElementUtils.shouldFocusView(view)) {
        // If the screen reader won't focus the control, the description is unimportant
        continue;
      }

      String speakableText =
          ViewHierarchyElementUtils.getSpeakableTextForElement(view, locale).toString().trim();
      if (TextUtils.isEmpty(speakableText)) {
        continue;
      }

      if (!textToViewMap.containsKey(speakableText)) {
        textToViewMap.put(speakableText, new ArrayList<ViewHierarchyElement>());
      }
      textToViewMap.get(speakableText).add(view);
    }
    return textToViewMap;
  }
}

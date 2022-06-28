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

import static java.lang.Boolean.TRUE;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck.Category;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Checks that items that require speakable text have some. Does not check if the text makes sense.
 */
public class SpeakableTextPresentCheck extends AccessibilityHierarchyCheck {

  /** Result when thew view is not visible. */
  public static final int RESULT_ID_NOT_VISIBLE = 1;
  /** Result when the view is not {@code importantForAccessibility}. */
  public static final int RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY = 2;
  /** Result when the view should not be focused by a screen reader. */
  public static final int RESULT_ID_SHOULD_NOT_FOCUS = 3;
  /** Result when the view is missing speakable text. */
  public static final int RESULT_ID_MISSING_SPEAKABLE_TEXT = 4;
  /** Result when the view type is excluded. */
  public static final int RESULT_ID_WEB_CONTENT = 5;

  @Override
  protected String getHelpTopic() {
    return "7158690"; // Content labels
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
    List<? extends ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
    for (ViewHierarchyElement element : viewsToEval) {
      if (!TRUE.equals(element.isVisibleToUser())) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            element,
            RESULT_ID_NOT_VISIBLE,
            null));
        continue;
      }

      if (!element.isImportantForAccessibility()) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            element,
            RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY,
            null));
        continue;
      }

      if (element.checkInstanceOf(ViewHierarchyElementUtils.WEB_VIEW_CLASS_NAME)
          && element.getChildViewCount() == 0) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            element,
            RESULT_ID_WEB_CONTENT,
            null));
        continue;
      }

      if (!ViewHierarchyElementUtils.shouldFocusView(element)) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            element,
            RESULT_ID_SHOULD_NOT_FOCUS,
            null));
        continue;
      }

      if (TextUtils.isEmpty(
          ViewHierarchyElementUtils.getSpeakableTextForElement(
              element, hierarchy.getDeviceState().getLocale()))) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.ERROR,
            element,
            RESULT_ID_MISSING_SPEAKABLE_TEXT,
            null));
      }
    }
    return results;
  }

  @Override
  public String getMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    return generateMessageForResultId(locale, resultId);
  }

  @Override
  public String getShortMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    return generateMessageForResultId(locale, resultId);
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_speakable_text_present");
  }

  private static String generateMessageForResultId(Locale locale, int resultId) {
    switch(resultId) {
      case RESULT_ID_NOT_VISIBLE:
        return StringManager.getString(locale, "result_message_not_visible");
      case RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY:
        return StringManager.getString(locale, "result_message_not_important_for_accessibility");
      case RESULT_ID_SHOULD_NOT_FOCUS:
        return StringManager.getString(locale, "result_message_should_not_focus");
      case RESULT_ID_MISSING_SPEAKABLE_TEXT:
        return StringManager.getString(locale, "result_message_missing_speakable_text");
      case RESULT_ID_WEB_CONTENT:
        return StringManager.getString(locale, "result_message_web_content");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }
}

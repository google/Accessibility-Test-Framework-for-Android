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
 * Check to ensure that an editable TextView is not labeled by a contentDescription
 */
public class EditableContentDescCheck extends AccessibilityHierarchyCheck {

  /** Result when thew view is not {@code importantForAccessibility} */
  public static final int RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY = 1;
  /** Result when the view is an editable {@link TextView} with a {@code contentDescription}. */
  public static final int RESULT_ID_EDITABLE_TEXTVIEW_CONTENT_DESC = 2;
  /** Result when the view is not an editable {@link TextView}. */
  public static final int RESULT_ID_NOT_EDITABLE_TEXTVIEW = 3;

  @Override
  protected String getHelpTopic() {
    return "6378120"; // Editable View labels
  }

  @Override
  public Category getCategory() {
    return Category.IMPLEMENTATION;
  }

  @Override
  public List<AccessibilityHierarchyCheckResult> runCheckOnHierarchy(
      AccessibilityHierarchy hierarchy,
      @Nullable ViewHierarchyElement fromRoot,
      @Nullable Parameters parameters) {
    List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();
    List<? extends ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
    for (ViewHierarchyElement view : viewsToEval) {
      if (!view.isImportantForAccessibility()) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY,
            null));
        continue;
      }

      if (TRUE.equals(view.isEditable())
          || view.checkInstanceOf(ViewHierarchyElementUtils.EDIT_TEXT_CLASS_NAME)) {
        if (!TextUtils.isEmpty(view.getContentDescription())) {
          results.add(new AccessibilityHierarchyCheckResult(this.getClass(),
              AccessibilityCheckResultType.ERROR,
              view,
              RESULT_ID_EDITABLE_TEXTVIEW_CONTENT_DESC,
              null));
        }
      } else {
        results.add(new AccessibilityHierarchyCheckResult(this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_NOT_EDITABLE_TEXTVIEW,
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
    return StringManager.getString(locale, "check_title_editable_content_desc");
  }

  private static String generateMessageForResultId(Locale locale, int resultId) {
    switch(resultId) {
      case RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY:
        return StringManager.getString(locale, "result_message_not_important_for_accessibility");
      case RESULT_ID_EDITABLE_TEXTVIEW_CONTENT_DESC:
        return StringManager.getString(locale, "result_message_editable_textview_content_desc");
      case RESULT_ID_NOT_EDITABLE_TEXTVIEW:
        return StringManager.getString(locale, "result_message_not_editable_textview");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }
}

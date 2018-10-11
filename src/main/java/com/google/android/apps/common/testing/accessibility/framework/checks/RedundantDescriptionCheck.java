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

import androidx.annotation.Nullable;
import android.widget.Button;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck.Category;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Metadata;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.DeviceState;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.base.Ascii;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Checks to ensure that speakable text does not contain redundant information about the view's
 * type. Accessibility services are aware of the view's type and can use that information as needed
 * (ex: Screen readers may append "button" to the speakable text of a {@link Button}).
 */
public class RedundantDescriptionCheck extends AccessibilityHierarchyCheck {

  /** Result when the locale is not English. */
  public static final int RESULT_ID_ENGLISH_LOCALE_ONLY = 1;
  /** Result when the view is not {@code importantForAccessibility}. */
  public static final int RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY = 2;
  /** Result when the view does not have a {@code contentDescription}*/
  public static final int RESULT_ID_NO_CONTENT_DESC = 3;
  /** Result when the view's {@code contentDescription} ends with the view's type. */
  public static final int RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE = 4;

  /**
   * Result metadata key for a view's content description as a {@link String}. Populated with
   * results of ID {@code RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE}
   */
  public static final String KEY_CONTENT_DESCRIPTION = "KEY_CONTENT_DESCRIPTION";

  private static final List<CharSequence> redundantWords =
      Lists.<CharSequence>newArrayList("button");

  @Override
  protected String getHelpTopic() {
    return "6378990"; // Items labeled with type or state
  }

  @Override
  public Category getCategory() {
    return Category.CONTENT_LABELING;
  }

  @Override
  public List<AccessibilityHierarchyCheckResult> runCheckOnHierarchy(
      AccessibilityHierarchy hierarchy,
      @Nullable ViewHierarchyElement fromRoot,
      @Nullable Metadata metadata) {
    List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();

    if (!isEnglish(hierarchy)) {
      results.add(new AccessibilityHierarchyCheckResult(
          this.getClass(), AccessibilityCheckResultType.NOT_RUN,
          null,
          RESULT_ID_ENGLISH_LOCALE_ONLY,
          null));
      return results;
    }

    List<ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
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

      CharSequence contentDescription = view.getContentDescription();
      if (TextUtils.isEmpty(contentDescription)) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_NO_CONTENT_DESC,
            null));
        continue;
      }
      for (CharSequence redundantWord : redundantWords) {
        if (Ascii.toLowerCase(contentDescription.toString()).contains(redundantWord)) {
          Metadata resultMetadata = new Metadata();
          resultMetadata.putString(KEY_CONTENT_DESCRIPTION, contentDescription.toString());
          results.add(
              new AccessibilityHierarchyCheckResult(
                  this.getClass(),
                  AccessibilityCheckResultType.WARNING,
                  view,
                  RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE,
                  resultMetadata));
        }
      }
    }
    return results;
  }

  @Override
  public String getMessageForResult(Locale locale, AccessibilityHierarchyCheckResult result) {
    if (result.getResultId() == RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE) {
      ViewHierarchyElement culprit = result.getElement();
      ResultMetadata metadata = result.getMetadata();
      if ((culprit != null)
          && ((metadata == null) || !metadata.containsKey(KEY_CONTENT_DESCRIPTION))) {
        // For legacy results, remap hierarchy element content description to metadata
        ResultMetadata updatedMetadata = (metadata != null) ? metadata.clone() : new Metadata();
        updatedMetadata.putString(
            KEY_CONTENT_DESCRIPTION, checkNotNull(culprit.getContentDescription()).toString());
        AccessibilityHierarchyCheckResult updatedResult =
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                result.getType(),
                culprit,
                result.getResultId(),
                updatedMetadata);
        return super.getMessageForResult(locale, updatedResult);
      }
    }

    return super.getMessageForResult(locale, result);
  }

  @Override
  public String getMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    String generated = generateMessageForResultId(locale, resultId);
    if (generated != null) {
      return generated;
    }

    switch (resultId) {
      case RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE:
        // Metadata will have been set for this result ID
        checkNotNull(metadata);
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_content_desc_ends_with_view_type"),
            metadata.getString(KEY_CONTENT_DESCRIPTION));
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getShortMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    String generated = generateMessageForResultId(locale, resultId);
    if (generated != null) {
      return generated;
    }

    switch (resultId) {
      case RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE:
        return StringManager.getString(
            locale, "result_message_brief_content_desc_ends_with_view_type");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_redundant_description");
  }

  /**
   * Indicates whether the locale recorded in the {@link DeviceState} was English.
   */
  private static boolean isEnglish(AccessibilityHierarchy hierarchy) {
    return hierarchy.getDeviceState().getLocale().getLanguage().equals(
        Locale.ENGLISH.getLanguage());
  }

  private static @Nullable String generateMessageForResultId(Locale locale, int resultId) {
    switch (resultId) {
      case RESULT_ID_ENGLISH_LOCALE_ONLY:
        return StringManager.getString(locale, "result_message_english_locale_only");
      case RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY:
        return StringManager.getString(locale, "result_message_not_important_for_accessibility");
      case RESULT_ID_NO_CONTENT_DESC:
        return StringManager.getString(locale, "result_message_no_content_desc");
      default:
        return null;
    }
  }
}

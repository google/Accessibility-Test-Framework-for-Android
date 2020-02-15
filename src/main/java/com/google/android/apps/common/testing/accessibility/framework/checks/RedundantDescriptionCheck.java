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
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.DeviceState;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Checks to ensure that speakable text does not contain redundant information about the view's
 * type. Accessibility services are aware of the view's type and can use that information as needed
 * (ex: Screen readers may append "button" to the speakable text of a {@link
 * android.widged.Button}).
 */
public class RedundantDescriptionCheck extends AccessibilityHierarchyCheck {

  /** [Legacy] Result when the locale is not English. */
  public static final int RESULT_ID_ENGLISH_LOCALE_ONLY = 1;
  /** Result when the view is not {@code importantForAccessibility}. */
  public static final int RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY = 2;
  /** Result when the view does not have a {@code contentDescription}*/
  public static final int RESULT_ID_NO_CONTENT_DESC = 3;
  /** [Legacy] Result when the view's {@code contentDescription} ends with the view's type. */
  public static final int RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE = 4;
  /** Result when the view's {@code contentDescription} contains the view's type. */
  public static final int RESULT_ID_CONTENT_DESC_CONTAINS_REDUNDANT_WORD = 5;
  /** Result when thew view is not visible. */
  public static final int RESULT_ID_NOT_VISIBLE = 6;

  /**
   * Result metadata key for a view's content description as a {@code String}. Populated in results
   * with {@link #RESULT_ID_CONTENT_DESC_CONTAINS_REDUNDANT_WORD} or {@link
   * #RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE}.
   */
  public static final String KEY_CONTENT_DESCRIPTION = "KEY_CONTENT_DESCRIPTION";

  /**
   * Result metadata key for a possibly redundant word in the view's content description, as a
   * {@code String}. Populated in results with {@link
   * #RESULT_ID_CONTENT_DESC_CONTAINS_REDUNDANT_WORD}.
   */
  public static final String KEY_REDUNDANT_WORD = "KEY_REDUNDANT_WORD";

  /** Keys of String resources. */
  private static final ImmutableList<String> redundantWordKeys =
      ImmutableList.of("button_item_type");

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
      @Nullable Parameters parameters) {
    List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();
    Locale recordedLocale = getRecordedLocale(hierarchy);

    List<? extends ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
    for (ViewHierarchyElement view : viewsToEval) {
      if (!Boolean.TRUE.equals(view.isVisibleToUser())) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_NOT_VISIBLE,
                null));
        continue;
      }
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
      for (String redundantWordKey : redundantWordKeys) {
        CharSequence redundantWord = StringManager.getString(recordedLocale, redundantWordKey);
        if (containsIgnoreCase(contentDescription, redundantWord)) {
          ResultMetadata resultMetadata = new HashMapResultMetadata();
          resultMetadata.putString(KEY_CONTENT_DESCRIPTION, contentDescription.toString());
          resultMetadata.putString(KEY_REDUNDANT_WORD, redundantWord.toString());
          results.add(
              new AccessibilityHierarchyCheckResult(
                  this.getClass(),
                  AccessibilityCheckResultType.WARNING,
                  view,
                  RESULT_ID_CONTENT_DESC_CONTAINS_REDUNDANT_WORD,
                  resultMetadata));
        }
      }
    }
    return results;
  }

  @Override
  public String getMessageForResult(Locale locale, AccessibilityHierarchyCheckResult result) {
    if (result.getResultId() == RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE) {
      ResultMetadata metadata = result.getMetadata();
      if (((metadata == null) || !metadata.containsKey(KEY_CONTENT_DESCRIPTION))
          && (result.getElement() != null)) {
        // For legacy results, remap hierarchy element content description to metadata
        ViewHierarchyElement culprit = result.getElement();
        ResultMetadata updatedMetadata =
            (metadata != null) ? metadata.clone() : new HashMapResultMetadata();
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

    // Metadata will have been set for these result IDs.
    checkNotNull(metadata);
    switch (resultId) {
      case RESULT_ID_CONTENT_DESC_CONTAINS_REDUNDANT_WORD:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_content_desc_contains_redundant_word"),
            metadata.getString(KEY_CONTENT_DESCRIPTION),
            metadata.getString(KEY_REDUNDANT_WORD));
      case RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE:
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
      case RESULT_ID_CONTENT_DESC_CONTAINS_REDUNDANT_WORD:
      case RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE:
        return StringManager.getString(
            locale, "result_message_brief_content_desc_contains_redundant_word");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_redundant_description");
  }

  private static boolean containsIgnoreCase(CharSequence sequence, CharSequence subSequence) {
    return Ascii.toLowerCase(sequence.toString()).contains(Ascii.toLowerCase(subSequence));
  }

  /** Indicates the locale recorded in the {@link DeviceState}. */
  private static Locale getRecordedLocale(AccessibilityHierarchy hierarchy) {
    return hierarchy.getDeviceState().getLocale();
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

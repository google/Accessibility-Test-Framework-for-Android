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
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Checks for speakable text that may contain redundant or inappropriate information:
 *
 * <ul>
 *   <li>The view's type. Accessibility services are aware of the view's type and can use that
 *       information as needed. Ex: Screen readers may append "button" to the speakable text of an
 *       {@link android.widget.Button}).
 *   <li>The view's state. Accessibility services should be aware of the view's current state and
 *       can report it. Ex: Screen readers may report that an {@link android.widget.CheckBox} is
 *       "checked" or "not checked".
 *   <li>The actions to which a view responds. Screen readers may announce the available actions.
 *       And the actions available to some users may differ based upon assistive technology being
 *       used.
 * </ul>
 *
 * <p>The name of this class is a misnomer because it now checks for more than just redundant
 * information.
 */
public class RedundantDescriptionCheck extends AccessibilityHierarchyCheck {

  /** [Legacy] Result when the locale is not English. */
  public static final int RESULT_ID_ENGLISH_LOCALE_ONLY = 1;
  /** Result when the view is not {@code importantForAccessibility}. */
  public static final int RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY = 2;
  /** Result when the view does not have a {@code contentDescription} */
  public static final int RESULT_ID_NO_CONTENT_DESC = 3;
  /** [Legacy] Result when the view's {@code contentDescription} ends with the view's type. */
  public static final int RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE = 4;
  /** Result when the view's {@code contentDescription} contains the view's type. */
  public static final int RESULT_ID_CONTENT_DESC_CONTAINS_ITEM_TYPE = 5;
  /** Result when thew view is not visible. */
  public static final int RESULT_ID_NOT_VISIBLE = 6;
  /** Result when the view's {@code contentDescription} contains a term indicating state. */
  public static final int RESULT_ID_CONTENT_DESC_CONTAINS_STATE = 7;
  /** Result when the view's {@code contentDescription} contains the name of an action. */
  public static final int RESULT_ID_CONTENT_DESC_CONTAINS_ACTION = 8;

  /**
   * Result metadata key for a view's content description as a {@code String}. Populated in results
   * with {@link #RESULT_ID_CONTENT_DESC_CONTAINS_ITEM_TYPE}, {@link
   * #RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE}, {@link #RESULT_ID_CONTENT_DESC_CONTAINS_STATE} or
   * {@link #RESULT_ID_CONTENT_DESC_CONTAINS_ACTION}.
   */
  public static final String KEY_CONTENT_DESCRIPTION = "KEY_CONTENT_DESCRIPTION";

  /**
   * Result metadata key for a possibly inappropriate word in the view's content description, as a
   * {@code String}. Populated in results with {@link #RESULT_ID_CONTENT_DESC_CONTAINS_ITEM_TYPE},
   * {@link #RESULT_ID_CONTENT_DESC_CONTAINS_STATE} or {@link
   * #RESULT_ID_CONTENT_DESC_CONTAINS_ACTION}.
   */
  public static final String KEY_REDUNDANT_WORD = "KEY_REDUNDANT_WORD";

  /** Keys of String resources that are item types. */
  private static final ImmutableList<String> ITEM_TYPE_WORD_KEYS =
      ImmutableList.of(
          "button_item_type", "checkbox_item_type", "checkbox_item_type_separate_words");

  /** Keys of String resources that are item states. */
  private static final ImmutableList<String> STATE_WORD_KEYS =

      ImmutableList.of("checked_state", "unchecked_state", "selected_state", "unselected_state");

  /** Keys of String resources that are names of actions. */
  private static final ImmutableList<String> ACTION_WORD_KEYS =
      ImmutableList.of("click_action", "swipe_action", "tap_action");

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
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY,
                null));
        continue;
      }

      CharSequence contentDescription = view.getContentDescription();
      if (TextUtils.isEmpty(contentDescription)) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_NO_CONTENT_DESC,
                null));
        continue;
      }
      // This can potentially produce multiple results for one element.
      checkForWords(
          RESULT_ID_CONTENT_DESC_CONTAINS_ITEM_TYPE,
          ITEM_TYPE_WORD_KEYS,
          recordedLocale,
          view,
          results);
      checkForWords(
          RESULT_ID_CONTENT_DESC_CONTAINS_STATE, STATE_WORD_KEYS, recordedLocale, view, results);
      checkForWords(
          RESULT_ID_CONTENT_DESC_CONTAINS_ACTION, ACTION_WORD_KEYS, recordedLocale, view, results);
    }
    return results;
  }

  /**
   * Checks to see if the view's {@code contentDescription} contains any of the localized words
   * indicated by {@code wordKeys}. If found, a WARNING is added to {@code results} with the
   * specified result ID.
   */
  private void checkForWords(
      int resultId,
      ImmutableList<String> wordKeys,
      Locale locale,
      ViewHierarchyElement view,
      List<AccessibilityHierarchyCheckResult> results) {
    CharSequence contentDescription = checkNotNull(view.getContentDescription());
    for (String wordKey : wordKeys) {
      CharSequence word = StringManager.getString(locale, wordKey);
      if (containsWordIgnoreCase(contentDescription, word)) {
        ResultMetadata resultMetadata = new HashMapResultMetadata();
        resultMetadata.putString(KEY_CONTENT_DESCRIPTION, contentDescription.toString());
        resultMetadata.putString(KEY_REDUNDANT_WORD, word.toString());
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.WARNING,
                view,
                resultId,
                resultMetadata));
      }
    }
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
                this.getClass(), result.getType(), culprit, result.getResultId(), updatedMetadata);
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
      case RESULT_ID_CONTENT_DESC_CONTAINS_ITEM_TYPE:
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
      case RESULT_ID_CONTENT_DESC_CONTAINS_STATE:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_content_desc_contains_state"),
            metadata.getString(KEY_CONTENT_DESCRIPTION),
            metadata.getString(KEY_REDUNDANT_WORD));
      case RESULT_ID_CONTENT_DESC_CONTAINS_ACTION:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_content_desc_contains_action"),
            metadata.getString(KEY_CONTENT_DESCRIPTION),
            metadata.getString(KEY_REDUNDANT_WORD));
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
      case RESULT_ID_CONTENT_DESC_CONTAINS_ITEM_TYPE:
      case RESULT_ID_CONTENT_DESC_ENDS_WITH_VIEW_TYPE:
      case RESULT_ID_CONTENT_DESC_CONTAINS_STATE:
      case RESULT_ID_CONTENT_DESC_CONTAINS_ACTION:
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

  private static boolean containsWordIgnoreCase(CharSequence sequence, CharSequence word) {
    // Dot . generally matches any character, but it excludes \n, \r, \u0085, \u2028, and \u2029.
    // (?s) at the beginning makes . matches any character without exception.
    // (?i) is used for ignore case match. \b matches word boundary.
    return Pattern.matches("(?s).*\\b(?i)" + word + "\\b.*", sequence.toString());
  }

  /** Indicates the locale recorded in the {@link DeviceState}. */
  private static Locale getRecordedLocale(AccessibilityHierarchy hierarchy) {
    return hierarchy.getDeviceState().getLocale();
  }

  private static @Nullable String generateMessageForResultId(Locale locale, int resultId) {
    switch (resultId) {
      case RESULT_ID_NOT_VISIBLE:
        return StringManager.getString(locale, "result_message_not_visible");
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

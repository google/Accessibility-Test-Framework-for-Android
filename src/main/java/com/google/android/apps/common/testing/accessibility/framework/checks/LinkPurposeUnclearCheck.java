/*
 * Copyright (C) 2019 Google Inc.
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
import com.google.android.apps.common.testing.accessibility.framework.replacements.Span;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableString;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Spans;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Check to warn about a link (ClickableSpan) whose purpose is unclear. */
public class LinkPurposeUnclearCheck extends AccessibilityHierarchyCheck {

  /** Result when the locale is not English. */
  public static final int RESULT_ID_ENGLISH_LOCALE_ONLY = 1;
  /** Result when the view is not a {@link TextView}. */
  public static final int RESULT_ID_NOT_TEXT_VIEW = 2;
  /** Result when the view contains a {@link URLSpan} with link text that is not descriptive. */
  public static final int RESULT_ID_LINK_TEXT_NOT_DESCRIPTIVE = 3;

  /** Result metadata key for a view's speakable text {@link String} */
  public static final String KEY_LINK_TEXT = "KEY_LINK_TEXT";

  /*
   * From src/audits/LinkWithUnclearPurpose.js
   * in https://github.com/GoogleChrome/accessibility-developer-tools
   */
  private static final ImmutableList<String> ENGLISH_STOPWORDS =
      ImmutableList.of(
          "click", "tap", "go", "here", "learn", "more", "this", "page", "link", "about");

  private static final Pattern WORD_PATTERN = Pattern.compile("\\w+");

  @Override
  protected @Nullable String getHelpTopic() {
    return "9663312"; // Link purpose unclear
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

    if (!isEnglish(hierarchy)) {
      results.add(
          new AccessibilityHierarchyCheckResult(
              this.getClass(),
              AccessibilityCheckResultType.NOT_RUN,
              null,
              RESULT_ID_ENGLISH_LOCALE_ONLY,
              null));
      return results;
    }

    List<? extends ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
    for (ViewHierarchyElement element : viewsToEval) {
      if (!element.checkInstanceOf(ViewHierarchyElementUtils.TEXT_VIEW_CLASS_NAME)) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.NOT_RUN,
                element,
                RESULT_ID_NOT_TEXT_VIEW,
                null));
      } else { // Element is a TextView
        SpannableString text = element.getText();
        if (text != null) {
          for (Span span : text.getSpans()) {
            if (span instanceof Spans.ClickableSpan) {
              CharSequence linkText = text.subSequence(span.getStart(), span.getEnd());
              if (hasOnlyStopwords(linkText)) {
                ResultMetadata resultMetadata = new HashMapResultMetadata();
                resultMetadata.putString(KEY_LINK_TEXT, linkText.toString());
                results.add(
                    new AccessibilityHierarchyCheckResult(
                        this.getClass(),
                        AccessibilityCheckResultType.WARNING,
                        element,
                        RESULT_ID_LINK_TEXT_NOT_DESCRIPTIVE,
                        resultMetadata));
              }
            }
          }
        }
      }
    }
    return results;
  }

  /** Indicates whether all of the word characters are in the form of stopwords. */
  @VisibleForTesting
  static boolean hasOnlyStopwords(CharSequence linkText) {
    Matcher m = WORD_PATTERN.matcher(linkText);
    while (m.find()) {
      if (!isStopword(m.group())) {
        return false;
      }
    }

    return true;
  }

  /** Is this one of the ENGLISH_STOPWORDS, ignoring case? */

  private static boolean isStopword(String term) {
    for (String word : ENGLISH_STOPWORDS) {
      if (Ascii.equalsIgnoreCase(word, term)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String getMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    String generated = generateMessageForResultId(locale, resultId);
    if (generated != null) {
      return generated;
    }

    // For each of the following result IDs, metadata will have been set on the result.
    checkNotNull(metadata);
    switch (resultId) {
      case RESULT_ID_LINK_TEXT_NOT_DESCRIPTIVE:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_link_text_not_descriptive"),
            metadata.getString(KEY_LINK_TEXT));
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
      case RESULT_ID_LINK_TEXT_NOT_DESCRIPTIVE:
        return StringManager.getString(locale, "result_message_brief_link_text_not_descriptive");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_link_test");
  }

  private static @Nullable String generateMessageForResultId(Locale locale, int resultId) {
    switch (resultId) {
      case RESULT_ID_ENGLISH_LOCALE_ONLY:
        return StringManager.getString(locale, "result_message_english_locale_only");
      case RESULT_ID_NOT_TEXT_VIEW:
        return StringManager.getString(locale, "result_message_not_text_view");
      default:
        return null;
    }
  }
}

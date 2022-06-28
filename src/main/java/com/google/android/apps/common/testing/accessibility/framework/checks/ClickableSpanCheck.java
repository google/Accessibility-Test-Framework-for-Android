/*
 * Copyright (C) 2015 Google Inc.
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
package com.google.android.apps.common.testing.accessibility.framework.checks;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck.Category;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Span;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableString;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Spans;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Uri;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.DeviceState;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Check to ensure that {@code ClickableSpan} is not being used in a TextView on a device running
 * Android prior to O.
 *
 * <p>{@code ClickableSpan} was inaccessible because individual spans could not be selected
 * independently in a single TextView and because accessibility services were unable to call {@link
 * ClickableSpan#onClick}. This was remedied in Anroid O.
 *
 * <p>The exception to this rule is that {@code URLSpan}s are accessible if they do not contain a
 * relative URI.
 */
public class ClickableSpanCheck extends AccessibilityHierarchyCheck {
  /** Result when the view type could not be determined. */
  public static final int RESULT_ID_NO_DETERMINED_TYPE = 1;
  /** Result when the view is not a {@link TextView}. */
  public static final int RESULT_ID_NOT_TEXT_VIEW = 2;
  /** Result when the view contains a {@link URLSpan} with a {@code null} URL. */
  public static final int RESULT_ID_NULL_URL = 3;
  /** Result when the view contains a {@link URLSpan} with a relative URL. */
  public static final int RESULT_ID_RELATIVE_LINK = 4;
  /** Result when the view contains a {@link ClickableSpan} that is not a {@link URLSpan}. */
  public static final int RESULT_ID_CLICKABLE_SPAN = 5;
  /**
   * Result when the Android SDK version {@link DeviceState#sdkVersion} is greater than or equal to
   * Android version 8.0 (SDK 26).
   */
  public static final int RESULT_ID_VERSION_NOT_APPLICABLE = 6;

  /** From Android 8.0 O+ (SDK 26), ClickableSpans work properly with accessibility services. */
  private static final int APPLICABLE_UNTIL_ANDROID_SDK_VERSION = 26;

  @Override
  protected String getHelpTopic() {
    return "6378148"; // Clickable links
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
    if (hierarchy.getDeviceState().getSdkVersion() >= APPLICABLE_UNTIL_ANDROID_SDK_VERSION) {
      results.add(
          new AccessibilityHierarchyCheckResult(
              this.getClass(),
              AccessibilityCheckResultType.NOT_RUN,
              null,
              RESULT_ID_VERSION_NOT_APPLICABLE,
              null));
      return results;
    }

    List<? extends ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
    for (ViewHierarchyElement element : viewsToEval) {
      if (!element.checkInstanceOf(ViewHierarchyElementUtils.TEXT_VIEW_CLASS_NAME)) {
        results.add(new AccessibilityHierarchyCheckResult(this.getClass(),
            AccessibilityCheckResultType.NOT_RUN, element, RESULT_ID_NOT_TEXT_VIEW, null));
        continue;
      } else { // Element is a TextView
        SpannableString text = element.getText();
        if (text != null) {
          for (Span span : text.getSpans()) {
            if (span instanceof Spans.URLSpan) {
              String url = ((Spans.URLSpan) span).getUrl();
              if (url == null) {
                results.add(new AccessibilityHierarchyCheckResult(this.getClass(),
                    AccessibilityCheckResultType.ERROR, element, RESULT_ID_NULL_URL, null));
              } else {
                Uri uri = new Uri(url);
                if (uri.isRelative()) {
                  // Relative URIs cannot be resolved.
                  results.add(new AccessibilityHierarchyCheckResult(this.getClass(),
                      AccessibilityCheckResultType.ERROR, element, RESULT_ID_RELATIVE_LINK, null));
                }
              }
            } else if (span instanceof Spans.ClickableSpan) { // Non-URLSpan ClickableSpan
              results.add(new AccessibilityHierarchyCheckResult(this.getClass(),
                  AccessibilityCheckResultType.ERROR, element, RESULT_ID_CLICKABLE_SPAN, null));
            }
          }
        }
      }
    }
    return results;
  }

  @Override
  public String getMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    return generateMessageForResult(locale, resultId);
  }

  @Override
  public String getShortMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    return generateMessageForResult(locale, resultId);
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_clickablespan");
  }

  private static String generateMessageForResult(Locale locale, int resultId) {
    switch (resultId) {
      case RESULT_ID_VERSION_NOT_APPLICABLE:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_sdk_version_not_applicable"),
            "8.0");

      case RESULT_ID_NO_DETERMINED_TYPE:
        return StringManager.getString(locale, "result_message_clickablespan_no_determined_type");

      case RESULT_ID_NOT_TEXT_VIEW:
        return StringManager.getString(locale, "result_message_not_text_view");

      case RESULT_ID_NULL_URL:
      case RESULT_ID_RELATIVE_LINK:
        return StringManager.getString(locale, "result_message_urlspan_invalid_url");

      case RESULT_ID_CLICKABLE_SPAN:
        return StringManager.getString(locale, "result_message_urlspan_not_clickablespan");

      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }
}

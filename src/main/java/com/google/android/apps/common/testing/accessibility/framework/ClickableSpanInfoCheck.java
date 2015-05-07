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

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import com.googlecode.eyesfree.utils.AccessibilityNodeInfoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Check to ensure that {@code ClickableSpan} is not being used in a TextView.
 *
 * <p>{@code ClickableSpan} is inaccessible because individual spans cannot be selected
 * independently in a single TextView and because accessibility services are unable to call
 * {@link ClickableSpan#onClick}.
 *
 * <p>The exception to this rule is that {@code URLSpan}s are accessible if they do not contain a
 * relative URI.
 */
public class ClickableSpanInfoCheck extends AccessibilityInfoCheck {

  @Override
  public List<AccessibilityInfoCheckResult> runCheckOnInfo(AccessibilityNodeInfo info,
      Context context, Bundle metadata) {
    List<AccessibilityInfoCheckResult> results = new ArrayList<AccessibilityInfoCheckResult>(1);
    AccessibilityNodeInfoCompat compatInfo = new AccessibilityNodeInfoCompat(info);
    if (AccessibilityNodeInfoUtils.nodeMatchesAnyClassByType(context, compatInfo, TextView.class)) {
      if (info.getText() instanceof Spanned) {
        Spanned text = (Spanned) info.getText();
        ClickableSpan[] clickableSpans = text.getSpans(0, text.length(), ClickableSpan.class);
        for (ClickableSpan clickableSpan : clickableSpans) {
          if (clickableSpan instanceof URLSpan) {
            String url = ((URLSpan) clickableSpan).getURL();
            if (url == null) {
              results.add(new AccessibilityInfoCheckResult(this.getClass(),
                  AccessibilityCheckResultType.ERROR, "URLSpan has null URL", info));
            } else {
              Uri uri = Uri.parse(url);
              if (uri.isRelative()) {
                // Relative URIs cannot be resolved.
                results.add(new AccessibilityInfoCheckResult(this.getClass(),
                    AccessibilityCheckResultType.ERROR, "URLSpan should not contain relative links",
                    info));
              }
            }
          } else { // Non-URLSpan ClickableSpan
            results.add(new AccessibilityInfoCheckResult(this.getClass(),
                AccessibilityCheckResultType.ERROR,
                "URLSpan should be used in place of ClickableSpan for improved accessibility",
                info));
          }
        }
      }
    } else {
      results.add(new AccessibilityInfoCheckResult(this.getClass(),
          AccessibilityCheckResultType.NOT_RUN, "View must be a TextView", info));
    }
    return results;
  }
}

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
import java.util.List;

/**
 * Check to ensure that a view has a content description for a screen reader
 */
public class SpeakableTextPresentViewCheck extends AccessibilityViewCheck {
  @Override
  public List<AccessibilityViewCheckResult> runCheckOnView(View view) {
    List<AccessibilityViewCheckResult> results = new ArrayList<AccessibilityViewCheckResult>();
    if (TextUtils.isEmpty(AccessibilityCheckUtils.getSpeakableTextForView(view))) {
      results.add(new AccessibilityViewCheckResult(this, AccessibilityCheckResultType.ERROR,
          "View is missing speakable text needed for a screen reader", view));
    }
    return results;
  }
}

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

package com.google.android.apps.common.testing.accessibility.framework;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.accessibility.AccessibilityNodeInfo;
import com.google.android.apps.common.testing.accessibility.framework.checks.ImageContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks to ensure that certain eligible items on-screen items have sufficient contrast. This check
 * uses screen capture data to heuristically evaluate foreground/background color contrast ratios.
 *
 * @deprecated Replaced by {@link TextContrastCheck} and {@link ImageContrastCheck}
 */
@Deprecated
public class ContrastInfoCheck extends AccessibilityInfoHierarchyCheck {

  private static final TextContrastCheck DELEGATION_CHECK_TEXT = new TextContrastCheck();

  private static final ImageContrastCheck DELEGATION_CHECK_IMAGES = new ImageContrastCheck();

  @Override
  public List<AccessibilityInfoCheckResult> runCheckOnInfoHierarchy(
      AccessibilityNodeInfo root, Context context, @Nullable Metadata metadata) {
    // The legacy ContrastInfoCheck evaluated both text and image content, whereas this
    // functionality is separated across two checks in the next-generation logic.
    List<AccessibilityInfoCheckResult> results = new ArrayList<>();
    results.addAll(
        super.runDelegationCheckOnInfo(root, this, DELEGATION_CHECK_TEXT, context, metadata));
    results.addAll(
        super.runDelegationCheckOnInfo(root, this, DELEGATION_CHECK_IMAGES, context, metadata));
    return results;
  }
}

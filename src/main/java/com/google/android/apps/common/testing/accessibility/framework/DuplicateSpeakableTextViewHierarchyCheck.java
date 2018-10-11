/*
 * Copyright (C) 2014 Google Inc.
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

import androidx.annotation.Nullable;
import android.view.View;
import com.google.android.apps.common.testing.accessibility.framework.checks.DuplicateSpeakableTextCheck;
import java.util.List;

/**
 * If two Views in a hierarchy have the same speakable text, that could be confusing for users. Two
 * Views with the same text, and at least one of them is clickable we warn in that situation. If we
 * find two non-clickable Views with the same speakable text, we report that fact as info. If no
 * Views in the hierarchy have any speakable text, we report that the test was not run.
 *
 * @deprecated Replaced by {@link DuplicateSpeakableTextCheck}
 */
@Deprecated
public class DuplicateSpeakableTextViewHierarchyCheck extends AccessibilityViewHierarchyCheck {

  private static final DuplicateSpeakableTextCheck DELEGATION_CHECK =
      new DuplicateSpeakableTextCheck();

  @Override
  public List<AccessibilityViewCheckResult> runCheckOnViewHierarchy(
      View root, @Nullable Metadata metadata) {
    return super.runDelegationCheckOnView(root, this, DELEGATION_CHECK, metadata);
  }
}

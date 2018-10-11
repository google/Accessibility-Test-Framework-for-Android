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
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck;
import java.util.List;

/**
 * Check to ensure that a view has a content description for a screen reader
 *
 * @deprecated Replaced by {@link SpeakableTextPresentCheck}
 */
@Deprecated
public class SpeakableTextPresentViewCheck extends AccessibilityViewHierarchyCheck {

  private static final SpeakableTextPresentCheck DELEGATION_CHECK = new SpeakableTextPresentCheck();

  @Override
  public List<AccessibilityViewCheckResult> runCheckOnViewHierarchy(
      View root, @Nullable Metadata metadata) {
    return super.runDelegationCheckOnView(root, this, DELEGATION_CHECK, metadata);
  }
}

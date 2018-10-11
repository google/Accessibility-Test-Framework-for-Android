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

import androidx.annotation.Nullable;
import android.text.style.ClickableSpan;
import android.view.View;
import com.google.android.apps.common.testing.accessibility.framework.checks.ClickableSpanCheck;
import java.util.List;

/**
 * Check to ensure that {@code ClickableSpan} is not being used in a TextView.
 *
 * <p>{@code ClickableSpan} is inaccessible because individual spans cannot be selected
 * independently in a single {@code TextView} and because accessibility services are unable to call
 * {@link ClickableSpan#onClick}.
 *
 * <p>The exception to this rule is that {@code URLSpan}s are accessible if they do not contain a
 * relative URI.
 *
 * @deprecated Replaced by {@link ClickableSpanCheck}
 */
@Deprecated
public class ClickableSpanViewCheck extends AccessibilityViewHierarchyCheck {

  private static final ClickableSpanCheck DELEGATION_CHECK = new ClickableSpanCheck();

  @Override
  public List<AccessibilityViewCheckResult> runCheckOnViewHierarchy(
      View root, @Nullable Metadata metadata) {
    return super.runDelegationCheckOnView(root, this, DELEGATION_CHECK, metadata);
  }
}

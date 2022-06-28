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

import android.view.View;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base class to check the accessibility of all {@link View}s in a hierarchy.
 *
 * @deprecated New accessibility checks should use {@link AccessibilityHierarchyCheck} to evaluate
 *     an {@link AccessibilityHierarchyAndroid} rather than a hierarchy of {@link View}s directly.
 */
@Deprecated
public abstract class AccessibilityViewHierarchyCheck extends AccessibilityCheck {

  /**
   * Run the check on the view.
   *
   * @param root The non-null root view of the hierarchy to check.
   * @param parameters Optional input data or preferences.
   * @return A list of interesting results encountered while running the check. The list will be
   *     empty if the check passes without incident.
   */
  public abstract List<AccessibilityViewCheckResult> runCheckOnViewHierarchy(
      View root, @Nullable Parameters parameters);

  /** @see AccessibilityViewHierarchyCheck#runCheckOnViewHierarchy(View, Parameters) */
  public List<AccessibilityViewCheckResult> runCheckOnViewHierarchy(View root) {
    return runCheckOnViewHierarchy(root, null);
  }
}

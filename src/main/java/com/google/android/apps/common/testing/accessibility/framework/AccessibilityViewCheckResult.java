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
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;

/**
 * Result generated when an accessibility check runs on a {@code View}.
 *
 * @deprecated Direct evaluation of {@link View}s is deprecated.  Instead, create an
 *             {@link AccessibilityHierarchy} using a source {@link View}, and run
 *             {@link AccessibilityHierarchyCheck}s obtained with {@link AccessibilityCheckPreset}.
 *             Results will be provided in the form of {@link AccessibilityHierarchyCheckResult}s.
 */
@Deprecated
public class AccessibilityViewCheckResult extends AccessibilityCheckResult {

  private final @Nullable View view;

  /**
   * @param checkClass The check that generated the error
   * @param type The type of the result
   * @param message A human-readable message explaining the error
   * @param view The view that was responsible for generating the error. This may be {@code null} if
   *     the result does not apply to a specific {@code View}.
   */
  public AccessibilityViewCheckResult(Class<? extends AccessibilityCheck> checkClass,
      AccessibilityCheckResultType type, CharSequence message, @Nullable View view) {
    super(checkClass, type, message);
    this.view = view;
  }

  /**
   * @return The view to which the result applies, or {@code null} if the result does not apply to a
   *     specific {@code View}.
   */
  public @Nullable View getView() {
    return view;
  }
}

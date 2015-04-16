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

import android.view.View;

/**
 * Result generated when an accessibility check runs on a {@code View}.
 */
public class AccessibilityViewCheckResult extends AccessibilityCheckResult {

  private View view;

  /**
   * @param checkClass The check that generated the error
   * @param type The type of the result
   * @param message A human-readable message explaining the error
   * @param view The view that was responsible for generating the error
   */
  public AccessibilityViewCheckResult(Class<? extends AccessibilityCheck> checkClass,
      AccessibilityCheckResultType type, CharSequence message, View view) {
    super(checkClass, type, message);
    this.view = view;
  }

  /**
   * @return The view to which the result applies.
   */
  public View getView() {
    return view;
  }

  @Override
  public void recycle() {
    super.recycle();
    view = null;
  }
}

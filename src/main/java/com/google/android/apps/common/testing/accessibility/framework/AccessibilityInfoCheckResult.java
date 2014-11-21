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

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Result generated when an accessibility check runs on a {@code AccessibilityNodeInfo}.
 */
public class AccessibilityInfoCheckResult extends AccessibilityCheckResult {

  private AccessibilityNodeInfo info;

  /**
   * @param check The check that generated the error
   * @param type The type of the result
   * @param message A human-readable message explaining the error
   * @param info The info that was responsible for generating the error
   */
  AccessibilityInfoCheckResult(AccessibilityCheck check, AccessibilityCheckResultType type,
      CharSequence message, AccessibilityNodeInfo info) {
    super(check, type, message);
    this.info = info;
  }

  /**
   * @return The info to which the result applies.
   */
  AccessibilityNodeInfo getInfo() {
    return info;
  }

}

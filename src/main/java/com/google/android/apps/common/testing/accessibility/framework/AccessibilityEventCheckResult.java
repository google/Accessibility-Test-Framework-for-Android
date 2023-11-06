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

import android.view.accessibility.AccessibilityEvent;

/** Result generated when an accessibility check runs on a {@link AccessibilityEvent}. */
public final class AccessibilityEventCheckResult extends AccessibilityCheckResult {

  private final AccessibilityEvent event;

  /**
   * @param checkClass The check that generated the error
   * @param type The type of the result
   * @param message A human-readable message explaining the error
   * @param event The {@link AccessibilityEvent} reported as the cause of the result
   */
  public AccessibilityEventCheckResult(Class<? extends AccessibilityCheck> checkClass,
      AccessibilityCheckResultType type, CharSequence message, AccessibilityEvent event) {
    super(checkClass, type, message);
    this.event = AccessibilityEvent.obtain(event);
  }

  /**
   * @return The {@link AccessibilityEvent} to which the result applies
   */
  public AccessibilityEvent getEvent() {
    return event;
  }
}

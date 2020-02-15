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

package com.google.android.apps.common.testing.accessibility.framework.integrations.espresso;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultDescriptor;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import java.util.List;

/** An exception class to be used for throwing exceptions with accessibility results. */
public final class AccessibilityViewCheckException
    extends com.google.android.apps.common.testing.accessibility.framework.integrations
        .AccessibilityViewCheckException {

  /** Create an instance with the default {@link AccessibilityCheckResultDescriptor} */
  public AccessibilityViewCheckException(List<AccessibilityViewCheckResult> results) {
    super(results);
  }

  /**
   * Create an exception with results and a result descriptor to generate the message.
   *
   * @param results a list of {@link AccessibilityViewCheckResult}s that are associated with the
   *     failure(s) that cause this to be thrown.
   * @param resultDescriptor the {@link AccessibilityCheckResultDescriptor} used to generate the
   *     exception message.
   */
  public AccessibilityViewCheckException(
      List<AccessibilityViewCheckResult> results,
      AccessibilityCheckResultDescriptor resultDescriptor) {
    super(results, resultDescriptor);
  }
}

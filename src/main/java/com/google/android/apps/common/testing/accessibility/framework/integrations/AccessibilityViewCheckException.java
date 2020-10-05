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

package com.google.android.apps.common.testing.accessibility.framework.integrations;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import java.util.List;

/**
 * An exception class to be used for throwing exceptions with accessibility results.
 *
 * @deprecated Use the AccessibilityViewCheckException in the espresso sub-package.
 */
@Deprecated
public abstract class AccessibilityViewCheckException extends RuntimeException {
  private final List<AccessibilityViewCheckResult> results;

  protected AccessibilityViewCheckException(List<AccessibilityViewCheckResult> results) {
    this.results = results;
  }

  /**
   * @return the list of results associated with this instance
   */
  public List<AccessibilityViewCheckResult> getResults() {
    return results;
  }
}

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

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultDescriptor;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;

import java.util.List;
import java.util.Locale;

/**
 * An exception class to be used for throwing exceptions with accessibility results.
 */
public class AccessibilityViewCheckException extends RuntimeException {
  private List<AccessibilityViewCheckResult> results;
  private AccessibilityCheckResultDescriptor resultDescriptor =
      new AccessibilityCheckResultDescriptor();

  /**
   * Any extension of this class must call this constructor.
   *
   * @param results a list of {@link AccessibilityViewCheckResult}s that are associated with the
   *        failure(s) that cause this to be thrown.
   */
  public AccessibilityViewCheckException(List<AccessibilityViewCheckResult> results) {
    super();
    if ((results == null) || (results.size() == 0)) {
      throw new RuntimeException(
          "AccessibilityViewCheckException requires at least 1 AccessibilityViewCheckResult");
    }
    this.results = results;
  }

  @Override
  public String getMessage() {
    // Lump all error result messages into one string to be the exception message
    StringBuilder exceptionMessage = new StringBuilder();
    String errorCountMessage = (results.size() == 1)
        ? "There was 1 accessibility error:\n"
        : String.format(Locale.US, "There were %d accessibility errors:\n", results.size());
    exceptionMessage.append(errorCountMessage);
    for (int i = 0; i < results.size(); i++) {
      if (i > 0) {
        exceptionMessage.append(",\n");
      }
      AccessibilityViewCheckResult result = results.get(i);
      exceptionMessage.append(resultDescriptor.describeResult(result));
    }
    return exceptionMessage.toString();
  }

  /**
   * Sets the {@link AccessibilityCheckResultDescriptor} used to generate the exception message.
   *
   * @return this
   */
  public AccessibilityViewCheckException setResultDescriptor(
      AccessibilityCheckResultDescriptor resultDescriptor) {
    this.resultDescriptor = resultDescriptor;
    return this;
  }

  /**
   * @return the list of results associated with this instance
   */
  public List<AccessibilityViewCheckResult> getResults() {
    return results;
  }
}

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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultDescriptor;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import java.util.List;
import java.util.Locale;

/**
 * An exception class to be used for throwing exceptions with accessibility results.
 */
public final class AccessibilityViewCheckException extends RuntimeException {
  private final List<AccessibilityViewCheckResult> results;
  private final AccessibilityCheckResultDescriptor resultDescriptor;

  /**
   * Create an instance with the default {@link AccessibilityCheckResultDescriptor}
   */
  public AccessibilityViewCheckException(List<AccessibilityViewCheckResult> results) {
    this(results, new AccessibilityCheckResultDescriptor());
  }

  /**
   * Create an exception with results and a result descriptor to generate the message.
   *
   * @param results a list of {@link AccessibilityViewCheckResult}s that are associated with the
   *        failure(s) that cause this to be thrown.
   * @param resultDescriptor the {@link AccessibilityCheckResultDescriptor} used to generate the
   *        exception message.
   */
  public AccessibilityViewCheckException(List<AccessibilityViewCheckResult> results,
      AccessibilityCheckResultDescriptor resultDescriptor) {
    super();
    checkArgument(
        results != null && !results.isEmpty(),
        "AccessibilityViewCheckException requires at least 1 AccessibilityViewCheckResult");
    checkNotNull(resultDescriptor);
    this.results = results;
    this.resultDescriptor = resultDescriptor;
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
   * @return the list of results associated with this instance
   */
  public List<AccessibilityViewCheckResult> getResults() {
    return results;
  }
}

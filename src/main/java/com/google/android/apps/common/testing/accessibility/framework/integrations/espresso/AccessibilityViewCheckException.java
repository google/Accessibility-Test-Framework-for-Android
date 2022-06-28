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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultDescriptor;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import java.util.List;
import java.util.Locale;

/** An exception class to be used for throwing exceptions with accessibility results. */
@SuppressWarnings("deprecation")
public final class AccessibilityViewCheckException
    extends com.google.android.apps.common.testing.accessibility.framework.integrations
        .AccessibilityViewCheckException {

  private final AccessibilityCheckResultDescriptor resultDescriptor;

  /** Create an instance with the default {@link AccessibilityCheckResultDescriptor} */
  public AccessibilityViewCheckException(List<AccessibilityViewCheckResult> results) {
    this(results, new AccessibilityCheckResultDescriptor());
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
    super(results);
    checkArgument(
        results != null && !results.isEmpty(),
        "AccessibilityViewCheckException requires at least 1 AccessibilityViewCheckResult");
    this.resultDescriptor = checkNotNull(resultDescriptor);
  }

  @Override
  public String getMessage() {
    List<AccessibilityViewCheckResult> results = getResults();
    // Lump all result messages into one string to be the exception message
    StringBuilder exceptionMessage = new StringBuilder();

    String resultCountMessage =
        (results.size() == 1)
            ? "There was 1 accessibility result:\n"
            : String.format(Locale.US, "There were %d accessibility results:\n", results.size());
    exceptionMessage.append(resultCountMessage);
    for (int i = 0; i < results.size(); i++) {
      if (i > 0) {
        exceptionMessage.append(",\n");
      }
      exceptionMessage.append(resultDescriptor.describeResult(results.get(i)));
    }
    return exceptionMessage.toString();
  }
}

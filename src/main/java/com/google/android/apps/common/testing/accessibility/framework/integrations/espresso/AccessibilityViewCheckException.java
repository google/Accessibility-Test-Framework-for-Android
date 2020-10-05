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

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultDescriptor;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import java.util.List;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

/** An exception class to be used for throwing exceptions with accessibility results. */
@SuppressWarnings("deprecation")
public final class AccessibilityViewCheckException
    extends com.google.android.apps.common.testing.accessibility.framework.integrations
        .AccessibilityViewCheckException {

  // Either resultDescriptor or deprecatedResultDescriptor must have a non-null value, but not both.
  private final @Nullable AccessibilityCheckResultDescriptor resultDescriptor;
  private final AccessibilityCheckResult.@Nullable AccessibilityCheckResultDescriptor
      deprecatedResultDescriptor;

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
    this(results, checkNotNull(resultDescriptor), /* deprecatedResultDescriptor= */ null);
  }

  /**
   * Create an exception with results and a deprecated result descriptor to generate the message.
   *
   * @deprecated Use {@link AccessibilityViewCheckException(List<AccessibilityViewCheckResult>,
   *     AccessibilityCheckResultDescriptor)} instead.
   */
  @Deprecated
  public AccessibilityViewCheckException(
      List<AccessibilityViewCheckResult> results,
      AccessibilityCheckResult.AccessibilityCheckResultDescriptor deprecatedResultDescriptor) {
    this(results, /* resultDescriptor= */ null, checkNotNull(deprecatedResultDescriptor));
  }

  private AccessibilityViewCheckException(
      List<AccessibilityViewCheckResult> results,
      @Nullable AccessibilityCheckResultDescriptor resultDescriptor,
      AccessibilityCheckResult.@Nullable AccessibilityCheckResultDescriptor
          deprecatedResultDescriptor) {
    super(results);
    checkArgument(
        results != null && !results.isEmpty(),
        "AccessibilityViewCheckException requires at least 1 AccessibilityViewCheckResult");
    checkArgument((resultDescriptor != null) || (deprecatedResultDescriptor != null));
    this.resultDescriptor = resultDescriptor;
    this.deprecatedResultDescriptor = deprecatedResultDescriptor;
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
      AccessibilityViewCheckResult result = results.get(i);
      exceptionMessage.append(
          (resultDescriptor != null)
              ? resultDescriptor.describeResult(result)
              : checkNotNull(deprecatedResultDescriptor).describeResult(result));
    }
    return exceptionMessage.toString();
  }
}

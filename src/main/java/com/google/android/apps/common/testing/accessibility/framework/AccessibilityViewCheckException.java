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

import android.view.View;

import java.util.List;
import java.util.Locale;

/**
 * An exception class to be used for throwing exceptions with accessibility results. This class can
 * be extended to provide descriptions of {@link AccessibilityViewCheckResult}s that the developer
 * considers readable. To extend this class, override the constructor and call super, and override
 * {@link #getResultMessage} to provide a readable String description of a result.
 */
public class AccessibilityViewCheckException extends RuntimeException {
  private List<AccessibilityViewCheckResult> results;

  /**
   * Any extension of this class must call this constructor.
   *
   * @param results a list of {@link AccessibilityViewCheckResult}s that are associated with the
   *        failure(s) that cause this to be thrown.
   */
  protected AccessibilityViewCheckException(List<AccessibilityViewCheckResult> results) {
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
      exceptionMessage.append(getResultMessage(result));
    }
    return exceptionMessage.toString();
  }

  /**
   * @return the list of results associated with this instance
   */
  public List<AccessibilityViewCheckResult> getResults() {
    return results;
  }

  /**
   * Returns a String description of the given {@link AccessibilityViewCheckResult}. The default
   * is to return the view's resource entry name followed by the result's message.
   *
   * @param result the {@link AccessibilityViewCheckResult} to describe
   * @return a String description of the result
   */
  protected String getResultMessage(AccessibilityViewCheckResult result) {
    StringBuilder msg = new StringBuilder();
    View view = result.getView();
    if ((view != null) && (view.getId() != View.NO_ID) && (view.getResources() != null)) {
      msg.append("View ");
      msg.append(view.getResources().getResourceEntryName(view.getId()));
      msg.append(": ");
    } else {
      msg.append("View with no valid resource name: ");
    }
    msg.append(result.getMessage());
    return msg.toString();
  }
}

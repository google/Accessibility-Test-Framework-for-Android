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

/**
 * The result of an accessibility check. The results are "interesting" in the sense that they
 * indicate some sort of accessibility issue. {@code AccessibilityCheck}s return lists of classes
 * that extend this one. There is no "passing" result; checks that return lists that contain no
 * {@code AccessibilityCheckResultType.ERROR}s have passed.
 * <p>
 * NOTE: Some subtypes of this class retain copies of resources that should be explicitly recycled.
 * Callers should use {@link #recycle()} to dispose of data in this object and release these
 * resources.
 */
public abstract class AccessibilityCheckResult {
  /**
   * Types of results. This must be kept consistent (other than UNKNOWN) with the ResultType enum in
   * {@code AccessibilityCheckResultProto}.
   */
  public enum AccessibilityCheckResultType {
    /** Clearly an accessibility bug, for example no speakable text on a clicked button */
    ERROR,
    /**
     * Potentially an accessibility bug, for example finding another view with the same speakable
     * text as a clicked view
     */
    WARNING,
    /**
     * Information that may be helpful when evaluating accessibility, for example a listing of all
     * speakable text in a view hierarchy in the traversal order used by an accessibility service.
     */
    INFO,
    /**
     * A signal that the check was not run at all (ex. because the API level was too low)
     */
    NOT_RUN,
    /**
     * A result that has been explicitly suppressed from throwing any Exceptions, used to allow for
     * known issues.
     */
    SUPPRESSED
  }

  protected Class<? extends AccessibilityCheck> checkClass;

  protected AccessibilityCheckResultType type;

  protected CharSequence message;

  /**
   * @param checkClass The class of the check that generated the error
   * @param type The type of the result
   * @param message A human-readable message explaining the error
   */
  public AccessibilityCheckResult(Class<? extends AccessibilityCheck> checkClass,
      AccessibilityCheckResultType type, CharSequence message) {
    this.checkClass = checkClass;
    this.type = type;
    this.message = message;
  }

  /**
   * @return The check that generated the result.
   */
  public Class<? extends AccessibilityCheck> getSourceCheckClass() {
    return checkClass;
  }

  /**
   * @return The type of the result.
   */
  public AccessibilityCheckResultType getType() {
    return type;
  }

  /**
   * @param type The type of the result.
   */
  /* package */ void setType(AccessibilityCheckResultType type) {
    this.type = type;
  }

  /**
   * @return A human-readable message explaining the result.
   */
  public CharSequence getMessage() {
    return message;
  }

  /**
   * Recycles all resources held by this object.
   */
  public void recycle() {
    checkClass = null;
    type = null;
    message = null;
  }
}

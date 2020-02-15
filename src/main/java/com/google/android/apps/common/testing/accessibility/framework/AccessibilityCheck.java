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

/**
 * Abstract base class for all accessibility checks. Abstract subclasses include:
 * <ul>
 *   <li>{@link AccessibilityHierarchyCheck} - the base class for all checks that run against
 *   {@code AccessibilityHierarchy}</li>
 *   <li>{@link AccessibilityEventCheck} - the base class for all checks that that run against
 *   {@code AccessibilityEvent}</li>
 *   <li>Deprecated {@link AccessibilityViewHierarchyCheck} - the base class for all checks that run
 *   against {@code View}s</li>
 * </ul>
 *
 * <p>Classes extending this one must implement {@code runCheck...} that return {@code List}s of
 * a subclass of {@code AccessibilityCheckResult}.
 */
public abstract class AccessibilityCheck {

  /** Categories of accessibility checks. */
  public enum Category {

    /** Checks for controls whose content labels are missing or confusing. */
    CONTENT_LABELING,

    /** Checks for touch targets that could cause difficulty for users with motor impairments. */
    TOUCH_TARGET_SIZE,

    /** Checks for elements that may be difficult to see due to low contrast. */
    LOW_CONTRAST,

    /**
     * Checks for conditions that impact accessibility due to the way a UI presents itself to
     * accessibility services.
     */
    IMPLEMENTATION;
  }
}

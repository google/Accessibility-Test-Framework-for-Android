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
 * Abstract base class for all accessibility checks. Abstract subclasses include
 * {@code AccessibilityViewCheck}, the base class for all that run against {@code View}s, and
 * {@code AccessibilityInfoCheck}, the base class for checks that run against
 * {@code AccessibilityNodeInfo}s.
 *
 * <p>Classes extending this one must implement {@code runCheck...} that return {@code List}s of
 * a subclass of {@code AccessibilityCheckResult}.
 */
public abstract class AccessibilityCheck {
}

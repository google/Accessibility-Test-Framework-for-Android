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
import android.view.accessibility.AccessibilityEvent;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Locale;

/**
 * Check which may be used to flag use of {@link View#announceForAccessibility(CharSequence)} or
 * dispatch of {@link AccessibilityEvent}s of type {@link AccessibilityEvent#TYPE_ANNOUNCEMENT}. The
 * use of these events, expect in specific situations, can be disruptive to the user.
 */
public class AnnouncementEventCheck extends AccessibilityEventCheck {

  @Override
  public boolean shouldHandleEvent(AccessibilityEvent event) {
    return (event != null) && (event.getEventType() == AccessibilityEvent.TYPE_ANNOUNCEMENT);
  }

  @Override
  public List<AccessibilityEventCheckResult> runCheckOnEvent(AccessibilityEvent event) {

    String message =
        StringManager.getString(Locale.getDefault(), "result_message_disruptive_announcement");
    return ImmutableList.<AccessibilityEventCheckResult>of(
        new AccessibilityEventCheckResult(
            this.getClass(), AccessibilityCheckResultType.WARNING, message, event));
  }
}

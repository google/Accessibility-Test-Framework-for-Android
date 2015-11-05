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

import android.view.View;

import java.util.HashSet;
import java.util.Set;

/**
 * Pre-sets for check configurations used with {@code getConfigForPreset}
 */
public enum AccessibilityCheckPreset {
  /** The latest set of checks (in general the most comprehensive list */
  LATEST,

  /** The set of checks available in the 1.0 release of the framework */
  VERSION_1_0_CHECKS,

  /** The set of checks available in the 2.0 release of the framework */
  VERSION_2_0_CHECKS,

  /** Don't check anything */
  NO_CHECKS,

  /**
   * Preset used occasionally to hold checks that are about to be part of {@code LATEST}.
   * Includes all checks in {@code LATEST}.
   */
  PRERELEASE,

  /**
   * Included for compatibility with Robolectric. Do not use.
   */
  @Deprecated
  VIEW_CHECKS,
  @Deprecated
  VIEW_HIERARCHY_CHECKS;

  /**
   * @param preset The preset of interest
   * @return A set of all checks for {@code View}s with scopes for the preset
   */
  public static Set<AccessibilityViewHierarchyCheck>
      getViewChecksForPreset(AccessibilityCheckPreset preset) {
    Set<AccessibilityViewHierarchyCheck> checks = new HashSet<>();
    if (preset == NO_CHECKS) {
      return checks;
    }

    /* Checks included in version 1.0 */
    checks.add(new TouchTargetSizeViewCheck());
    checks.add(new TextContrastViewCheck());
    checks.add(new DuplicateSpeakableTextViewHierarchyCheck());
    checks.add(new SpeakableTextPresentViewCheck());
    checks.add(new EditableContentDescViewCheck());
    if (preset == VERSION_1_0_CHECKS) {
      return checks;
    }

    checks.add(new ClickableSpanViewCheck());
    checks.add(new RedundantContentDescViewCheck());
    checks.add(new DuplicateClickableBoundsViewCheck());
    if (preset == VERSION_2_0_CHECKS) {
      return checks;
    }

    /* Checks added since last release */
    if (preset == LATEST) {
      return checks;
    }

    if (preset == PRERELEASE) {
      return checks;
    }
    /*
     * Throw an exception if we didn't handle a preset. This code should be unreachable, but it
     * makes writing a test for unhandled presets trivial.
     */
    throw new IllegalArgumentException();
  }

  /**
   * @param preset The preset of interest
   * @return A set of all checks for {@code AccessibilityNodeInfo}s with scopes for the preset
   */
  public static Set<AccessibilityInfoHierarchyCheck>
      getInfoChecksForPreset(AccessibilityCheckPreset preset) {
    Set<AccessibilityInfoHierarchyCheck> checks = new HashSet<>();
    if (preset == NO_CHECKS) {
      return checks;
    }

    /* Checks included in version 1.0 */
    checks.add(new EditableContentDescInfoCheck());
    checks.add(new SpeakableTextPresentInfoCheck());
    if (preset == VERSION_1_0_CHECKS) {
      return checks;
    }

    checks.add(new ClickableSpanInfoCheck());
    checks.add(new TouchTargetSizeInfoCheck());
    checks.add(new RedundantContentDescInfoCheck());
    checks.add(new DuplicateClickableBoundsInfoCheck());
    if (preset == VERSION_2_0_CHECKS) {
      return checks;
    }

    checks.add(new ContrastInfoCheck());
    /* Checks added since last release */
    if (preset == LATEST) {
      return checks;
    }

    if (preset == PRERELEASE) {
      return checks;
    }

    /*
     * Throw an exception if we didn't handle a preset. This code should be unreachable, but it
     * makes writing a test for unhandled presets trivial.
     */
    throw new IllegalArgumentException();
  }

  /**
   * @param preset The preset of interest
   * @return A set of all checks for {@code AccessibilityNodeInfo}s with scopes for the preset
   */
  public static Set<AccessibilityEventCheck>
      getEventChecksForPreset(AccessibilityCheckPreset preset) {
    Set<AccessibilityEventCheck> checks = new HashSet<>();
    if ((preset == NO_CHECKS) || (preset == VERSION_1_0_CHECKS)) {
      return checks;
    }

    checks.add(new AnnouncementEventCheck());
    if (preset == VERSION_2_0_CHECKS) {
      return checks;
    }

    /* Checks added since last release */
    if (preset == LATEST) {
      return checks;
    }
    if (preset == PRERELEASE) {
      return checks;
    }

    /*
     * Throw an exception if we didn't handle a preset. This code should be unreachable, but it
     * makes writing a test for unhandled presets trivial.
     */
    throw new IllegalArgumentException();
  }

  /**
   * Included for compatibility with older Robolectric version. Do not use.
   * Remove for public release.
   * TODO(pweaver) This is a workaround to make Robolectric built against 1.0 of the framework
   * continue to function with later versions of the framework. Fix Robolectric properly.
   */
  @Deprecated
  public static Set<? extends AccessibilityCheck> getAllChecksForPreset(
      @SuppressWarnings("unused") AccessibilityCheckPreset preset) {
    Set<AccessibilityViewHierarchyCheck> checks = new HashSet<>();

    if (preset == VIEW_HIERARCHY_CHECKS) {
      checks.add(new SpeakableTextPresentViewCheck() {
        @Override
        protected boolean shouldFocusView(View view) {
          return true;
        }
      });
      checks.add(new ClickableSpanViewCheck());
      checks.add(new EditableContentDescViewCheck());
    }
    return checks;
  }
}

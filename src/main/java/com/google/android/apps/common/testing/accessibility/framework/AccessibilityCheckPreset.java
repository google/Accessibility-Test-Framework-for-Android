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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Pre-sets for check configurations used with {@code getConfigForPreset}
 */
public enum AccessibilityCheckPreset {
  /** Don't check anything */
  NO_CHECKS,

  /** Check everything on a single View */
  VIEW_CHECKS,

  /** Check everything on a single AccessibilityNodeInfo */
  INFO_CHECKS,

  /** Check everything on all Views in a hierarchy */
  VIEW_HIERARCHY_CHECKS,

  /** Check everything on all AccessibilityNodeInfos in a hierarchy */
  INFO_HIERARCHY_CHECKS;

  private static final AccessibilityCheck[] ALL_ACCESSIBILITY_CHECKS = {
      new DuplicateSpeakableTextViewHierarchyCheck(),
      new EditableContentDescInfoCheck(),
      new EditableContentDescViewCheck(),
      new SpeakableTextPresentInfoCheck(),
      new SpeakableTextPresentViewCheck(),
      new TextContrastViewCheck(),
      new TouchTargetSizeViewCheck()
    };

  /**
   * @param preset The preset of interest
   * @return An unmodifiable set of all checks with scopes that are covered by the specified scope
   */
  public static Set<AccessibilityCheck> getAllChecksForPreset(AccessibilityCheckPreset preset) {
    Set<AccessibilityCheck> checks = new HashSet<AccessibilityCheck>();
    for (AccessibilityCheck check : ALL_ACCESSIBILITY_CHECKS) {
      switch (preset) {
        case VIEW_CHECKS:
          if (check instanceof AccessibilityViewCheck) {
            checks.add(check);
          }
          break;
        case VIEW_HIERARCHY_CHECKS:
          if (check instanceof AccessibilityViewHierarchyCheck) {
            checks.add(check);
          }
          break;
        case INFO_CHECKS:
          if (check instanceof AccessibilityInfoCheck) {
            checks.add(check);
          }
          break;
        case INFO_HIERARCHY_CHECKS:
          if (check instanceof AccessibilityInfoHierarchyCheck) {
            checks.add(check);
          }
          break;
        case NO_CHECKS:
        default:
          break;
      }
    }
    return Collections.unmodifiableSet(checks);
  }
}

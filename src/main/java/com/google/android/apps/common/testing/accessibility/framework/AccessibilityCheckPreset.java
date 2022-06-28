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

import com.google.android.apps.common.testing.accessibility.framework.checks.ClassNameCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.ClickableSpanCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.DuplicateClickableBoundsCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.DuplicateSpeakableTextCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.EditableContentDescCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.ImageContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.LinkPurposeUnclearCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.RedundantDescriptionCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TextContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TextSizeCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TraversalOrderCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.UnexposedTextCheck;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Pre-sets for check configurations used with {@code getConfigForPreset} */
public enum AccessibilityCheckPreset {
  /** The latest set of checks (in general the most comprehensive list */
  LATEST,

  /** The set of checks available in the 1.0 release of the framework */
  VERSION_1_0_CHECKS,

  /** The set of checks available in the 2.0 release of the framework */
  VERSION_2_0_CHECKS,

  /** The set of checks available in the 3.0 release of the framework */
  VERSION_3_0_CHECKS,

  /** The set of checks available in the 3.1 release of the framework */
  VERSION_3_1_CHECKS,

  /** The set of checks available in the 4.0 release of the framework */
  VERSION_4_0_CHECKS,

  /** Don't check anything */
  NO_CHECKS,

  /**
   * Preset used occasionally to hold checks that are about to be part of {@code LATEST}. Includes
   * all checks in {@code LATEST}.
   */
  PRERELEASE;

  private static final ImmutableClassToInstanceMap<AccessibilityHierarchyCheck>
      CLASS_TO_HIERARCHY_CHECK =
          new ImmutableClassToInstanceMap.Builder<AccessibilityHierarchyCheck>()
              .put(SpeakableTextPresentCheck.class, new SpeakableTextPresentCheck())
              .put(EditableContentDescCheck.class, new EditableContentDescCheck())
              .put(TouchTargetSizeCheck.class, new TouchTargetSizeCheck())
              .put(DuplicateSpeakableTextCheck.class, new DuplicateSpeakableTextCheck())
              .put(TextContrastCheck.class, new TextContrastCheck())
              .put(ClickableSpanCheck.class, new ClickableSpanCheck())
              .put(DuplicateClickableBoundsCheck.class, new DuplicateClickableBoundsCheck())
              .put(RedundantDescriptionCheck.class, new RedundantDescriptionCheck())
              .put(ImageContrastCheck.class, new ImageContrastCheck())
              .put(ClassNameCheck.class, new ClassNameCheck())
              .put(TraversalOrderCheck.class, new TraversalOrderCheck())
              .put(LinkPurposeUnclearCheck.class, new LinkPurposeUnclearCheck())
              .put(TextSizeCheck.class, new TextSizeCheck())
              .put(UnexposedTextCheck.class, new UnexposedTextCheck())
              .build();

  private static final ImmutableMap<String, AccessibilityHierarchyCheck>
      CLASS_NAME_TO_HIERARCHY_CHECK = mapClassNameToInstance(CLASS_TO_HIERARCHY_CHECK);

  /** @return an instance of a {@link AccessibilityHierarchyCheck} of the given class type. */
  public static @Nullable AccessibilityHierarchyCheck getHierarchyCheckForClass(
      Class<? extends AccessibilityHierarchyCheck> clazz) {
    return CLASS_TO_HIERARCHY_CHECK.get(clazz);
  }

  /**
   * @return an instance of the class with the given name if it extends from {@link
   *     AccessibilityHierarchyCheck}.
   */
  public static @Nullable AccessibilityHierarchyCheck getHierarchyCheckForClassName(
      String className) {
    return CLASS_NAME_TO_HIERARCHY_CHECK.get(className);
  }

  /**
   * Retrieve checks for {@code AccessibilityHierarchy}s based on a desired preset.
   *
   * @param preset The preset of interest
   * @return A set of all checks for {@code AccessibilityHierarchy}s with scopes for the preset
   */
  // incompatible argument for parameter element of add.
  @SuppressWarnings("nullness:argument")
  public static ImmutableSet<AccessibilityHierarchyCheck> getAccessibilityHierarchyChecksForPreset(
      AccessibilityCheckPreset preset) {
    ImmutableSet.Builder<AccessibilityHierarchyCheck> checks = ImmutableSet.builder();

    if (preset == NO_CHECKS) {
      return checks.build();
    }

    /*
     * AccessibilityHierarchy was added after 2.0's release, but the checks that match those for
     * Views in previous versions should be returned for the same preset to support migrating from
     * View checks to AccessibilityHierarchy checks.
     */
    /* Checks included in version 1.0 */
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(SpeakableTextPresentCheck.class));
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(EditableContentDescCheck.class));
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(TouchTargetSizeCheck.class));
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(DuplicateSpeakableTextCheck.class));
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(TextContrastCheck.class));
    if (preset == VERSION_1_0_CHECKS) {
      return checks.build();
    }

    /* Checks included in version 2.0 */
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(ClickableSpanCheck.class));
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(DuplicateClickableBoundsCheck.class));
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(RedundantDescriptionCheck.class));
    if (preset == VERSION_2_0_CHECKS) {
      return checks.build();
    }

    /* Checks included in version 3.0 */
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(ImageContrastCheck.class));
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(ClassNameCheck.class));
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(TraversalOrderCheck.class));
    if (preset == VERSION_3_0_CHECKS) {
      return checks.build();
    }

    /* Checks included in version 3.1 */
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(LinkPurposeUnclearCheck.class));
    if (preset == VERSION_3_1_CHECKS) {
      return checks.build();
    }

    /* Checks included in version 4.0 */
    checks.add(CLASS_TO_HIERARCHY_CHECK.get(TextSizeCheck.class));
    if (preset == VERSION_4_0_CHECKS) {
      return checks.build();
    }

    /* Checks added since last release */
    if (preset == LATEST) {
      return checks.build();
    }

    checks.add(CLASS_TO_HIERARCHY_CHECK.get(UnexposedTextCheck.class));
    if (preset == PRERELEASE) {
      return checks.build();
    }

    /*
     * Throw an exception if we didn't handle a preset. This code should be unreachable, but it
     * makes writing a test for unhandled presets trivial.
     */
    throw new IllegalArgumentException();
  }

  private static ImmutableMap<String, AccessibilityHierarchyCheck> mapClassNameToInstance(
      ImmutableClassToInstanceMap<AccessibilityHierarchyCheck> classToInstanceMap) {
    ImmutableMap.Builder<String, AccessibilityHierarchyCheck> builder = ImmutableMap.builder();
    for (Map.Entry<Class<? extends AccessibilityHierarchyCheck>, AccessibilityHierarchyCheck>
        entry : classToInstanceMap.entrySet()) {
      builder.put(entry.getKey().getName(), entry.getValue());
    }
    return builder.buildOrThrow();
  }
}

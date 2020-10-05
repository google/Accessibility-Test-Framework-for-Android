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

import static com.google.common.base.Preconditions.checkNotNull;

import android.view.View;
import com.google.android.apps.common.testing.accessibility.framework.checks.ImageContrastCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.LinkPurposeUnclearCheck;
import com.google.android.apps.common.testing.accessibility.framework.checks.TraversalOrderCheck;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Provides deprecated methods for {@link AccessibilityCheckPreset}. */
@SuppressWarnings("deprecation") // Need to support AccessibilityViewHierarchyCheck..
public final class AccessibilityCheckPresetAndroid {

  /**
   * Retrieve checks for {@code View}s based on a desired preset.
   *
   * @param preset The preset of interest
   * @return A set of all checks for {@code View}s with scopes for the preset
   * @deprecated ATF integrations should now use {@link
   *     AccessibilityCheckPreset#getAccessibilityHierarchyChecksForPreset(AccessibilityCheckPreset)}
   *     for the most up to date set of accessibility checks.
   */
  @Deprecated
  public static ImmutableSet<AccessibilityViewHierarchyCheck> getViewChecksForPreset(
      AccessibilityCheckPreset preset) {
    ImmutableSet.Builder<AccessibilityViewHierarchyCheck> checks = ImmutableSet.builder();
    if (preset == AccessibilityCheckPreset.NO_CHECKS) {
      return checks.build();
    }

    /* Checks included in version 1.0 */
    checks.add(new TouchTargetSizeViewCheck());
    checks.add(new TextContrastViewCheck());
    checks.add(new DuplicateSpeakableTextViewHierarchyCheck());
    checks.add(new SpeakableTextPresentViewCheck());
    checks.add(new EditableContentDescViewCheck());
    if (preset == AccessibilityCheckPreset.VERSION_1_0_CHECKS) {
      return checks.build();
    }

    /* Checks included in version 2.0 */
    checks.add(new ClickableSpanViewCheck());
    checks.add(new RedundantContentDescViewCheck());
    checks.add(new DuplicateClickableBoundsViewCheck());
    if (preset == AccessibilityCheckPreset.VERSION_2_0_CHECKS) {
      return checks.build();
    }

    /* No View-based checks added in version 3.0 */
    if (preset == AccessibilityCheckPreset.VERSION_3_0_CHECKS) {
      return checks.build();
    }

    /* Checks included in version 3.1 */
    // ClassNameCheck is not included because View-based checks do not have access to the class
    // names populated into an AccessibilityNodeInfo, making the check a no-op.
    checks.add(
        new DelegatedViewHierarchyCheck(
            checkNotNull(
                AccessibilityCheckPreset.getHierarchyCheckForClass(TraversalOrderCheck.class))));
    checks.add(
        new DelegatedViewHierarchyCheck(
            checkNotNull(
                AccessibilityCheckPreset.getHierarchyCheckForClass(ImageContrastCheck.class))));
    checks.add(
        new DelegatedViewHierarchyCheck(
            checkNotNull(
                AccessibilityCheckPreset.getHierarchyCheckForClass(
                    LinkPurposeUnclearCheck.class))));
    if (preset == AccessibilityCheckPreset.VERSION_3_1_CHECKS) {
      return checks.build();
    }

    /* Checks added since last release */
    if (preset == AccessibilityCheckPreset.LATEST) {
      return checks.build();
    }

    if (preset == AccessibilityCheckPreset.PRERELEASE) {
      return checks.build();
    }

    /*
     * Throw an exception if we didn't handle a preset. This code should be unreachable, but it
     * makes writing a test for unhandled presets trivial.
     */
    throw new IllegalArgumentException();
  }

  /**
   * Retrieve checks for {@code AccessibilityEvent}s based on a desired preset.
   *
   * @param preset The preset of interest
   * @return A set of all checks for {@code AccessibilityEvent}s with scopes for the preset
   */
  public static ImmutableSet<AccessibilityEventCheck> getEventChecksForPreset(
      AccessibilityCheckPreset preset) {
    ImmutableSet.Builder<AccessibilityEventCheck> checks = ImmutableSet.builder();
    if ((preset == AccessibilityCheckPreset.NO_CHECKS)
        || (preset == AccessibilityCheckPreset.VERSION_1_0_CHECKS)) {
      return checks.build();
    }

    /* Checks included in version 2.0 */
    checks.add(new AnnouncementEventCheck());
    if (preset == AccessibilityCheckPreset.VERSION_2_0_CHECKS) {
      return checks.build();
    }

    /* No event-based checks added in version 3.0 */
    if (preset == AccessibilityCheckPreset.VERSION_3_0_CHECKS) {
      return checks.build();
    }

    /* No event-based checks added in version 3.1 */
    if (preset == AccessibilityCheckPreset.VERSION_3_1_CHECKS) {
      return checks.build();
    }

    /* Checks added since last release */
    if (preset == AccessibilityCheckPreset.LATEST) {
      return checks.build();
    }
    if (preset == AccessibilityCheckPreset.PRERELEASE) {
      return checks.build();
    }

    /*
     * Throw an exception if we didn't handle a preset. This code should be unreachable, but it
     * makes writing a test for unhandled presets trivial.
     */
    throw new IllegalArgumentException();
  }

  /** An adapter to present an AccessibilityHierarchyCheck as an AccessibilityViewHierarchyCheck */
  @VisibleForTesting
  /*package*/ static class DelegatedViewHierarchyCheck extends AccessibilityViewHierarchyCheck {

    @VisibleForTesting /*package*/ final AccessibilityHierarchyCheck toCheck;

    DelegatedViewHierarchyCheck(AccessibilityHierarchyCheck toCheck) {
      this.toCheck = toCheck;
    }

    @Override
    public List<AccessibilityViewCheckResult> runCheckOnViewHierarchy(
        View root, @Nullable Parameters parameters) {
      // We lie about the name of the fromCheck so that this class is not known externally.
      return super.runDelegationCheckOnView(root, toCheck, toCheck, parameters);
    }
  }

  private AccessibilityCheckPresetAndroid() {}
}

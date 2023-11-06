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

import static com.google.common.base.Preconditions.checkNotNull;

import android.view.View;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.Image;
import com.google.common.annotations.VisibleForTesting;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/** Result generated when an accessibility check runs on a {@code View}. */
public class AccessibilityViewCheckResult extends AccessibilityCheckResult {

  private final @Nullable ViewHierarchyElement element;
  private final @Nullable View view;
  private final @Nullable Image viewImage;
  private final @Nullable AccessibilityHierarchyCheckResult hierarchyCheckResult;

  /**
   * @param checkClass The check that generated the error
   * @param type The type of the result
   * @param message A human-readable message explaining the error
   * @param view The view that was responsible for generating the error. This may be {@code null} if
   *     the result does not apply to a specific {@code View}.
   * @param viewImage An image of the {@code View} associated with this finding. This will usually
   *     be {@code null} except when debugging contrast checks. See {@link
   *     Parameters#setSaveViewImages(boolean)}.
   */
  public AccessibilityViewCheckResult(
      Class<? extends AccessibilityCheck> checkClass,
      AccessibilityCheckResultType type,
      CharSequence message,
      @Nullable View view,
      @Nullable Image viewImage) {
    super(checkClass, type, message);
    element = null;
    this.view = view;
    this.viewImage = viewImage;
    hierarchyCheckResult = null;
  }

  /** Constructor without viewImage. */
  public AccessibilityViewCheckResult(
      Class<? extends AccessibilityCheck> checkClass,
      AccessibilityCheckResultType type,
      CharSequence message,
      @Nullable View view) {
    this(checkClass, type, message, view, /* viewImage= */ null);
  }


  /**
   * Constructor that takes an AccessibilityHierarchyCheckResult.
   *
   * @param checkClass The check that generated the error. This should be a ViewCheck
   */
  /* package */ AccessibilityViewCheckResult(
      Class<? extends AccessibilityCheck> checkClass,
      AccessibilityHierarchyCheckResult hierarchyCheckResult,
      @Nullable ViewHierarchyElement element,
      @Nullable View view) {
    super(checkClass, hierarchyCheckResult.getType(), /* message= */ null);
    this.element = element;
    this.view = view;
    viewImage =
        (hierarchyCheckResult instanceof AccessibilityHierarchyCheckResultWithImage)
            ? ((AccessibilityHierarchyCheckResultWithImage) hierarchyCheckResult).getViewImage()
            : null;
    this.hierarchyCheckResult = hierarchyCheckResult;
  }

  /**
   * Returns a copy of this result, but with the AccessibilityCheckResultType changed to SUPPRESSED.
   */
  public AccessibilityViewCheckResult getSuppressedResultCopy() {
    AccessibilityViewCheckResult result =
        (hierarchyCheckResult == null)
            ? new AccessibilityViewCheckResult(
                getSourceCheckClass(),
                AccessibilityCheckResultType.SUPPRESSED,
                getMessage(Locale.ENGLISH),
                getView())
            : new AccessibilityViewCheckResult(
                getSourceCheckClass(),
                hierarchyCheckResult.getSuppressedResultCopy(),
                getElement(),
                getView());
    return result;
  }

  /**
   * Returns the view to which the result applies, or {@code null} if the result does not apply to a
   * specific {@code View} or the view is not available.
   */
  public @Nullable View getView() {
    return view;
  }

  /** Returns an image of the view to which the result applies, in some cases, or {@code null}. */
  public @Nullable Image getViewImage() {
    return viewImage;
  }

  /**
   * Returns the element to which the result applies, or {@code null} if the result does not apply
   * to a specific element or the element is not available.
   */
  public @Nullable ViewHierarchyElement getElement() {
    return element;
  }

  /**
   * Returns the AccessibilityHierarchyCheckResult with which this result was constructed.
   *
   * @throws NullPointerException if this result was not constructed with an
   *     AccessibilityHierarchyCheckResult
   */
  public AccessibilityHierarchyCheckResult getAccessibilityHierarchyCheckResult() {
    return checkNotNull(hierarchyCheckResult);
  }

  /**
   * Returns the type of AccessibilityHierarchyCheck that detected this result.
   *
   * @throws NullPointerException if this result was not constructed with an
   *     AccessibilityHierarchyCheckResult
   */
  public Class<? extends AccessibilityHierarchyCheck> getAccessibilityHierarchyCheck() {
    return getAccessibilityHierarchyCheckResult()
        .getSourceCheckClass()
        .asSubclass(AccessibilityHierarchyCheck.class);
  }

  /**
   * Returns the integer id of this result.
   *
   * @see AccessibilityHierarchyCheckResult#getResultId()
   * @throws NullPointerException if this result was not constructed with an
   *     AccessibilityHierarchyCheckResult
   */
  public int getResultId() {
    return getAccessibilityHierarchyCheckResult().getResultId();
  }


  /**
   * Retrieve the metadata stored in this result.
   *
   * @see AccessibilityHierarchyCheckResult#getMetadata()
   * @throws NullPointerException if this result was not constructed with an
   *     AccessibilityHierarchyCheckResult
   */
  @Pure
  public @Nullable ResultMetadata getMetadata() {
    return getAccessibilityHierarchyCheckResult().getMetadata();
  }

  @Override
  public CharSequence getMessage() {
    return (hierarchyCheckResult == null) ? super.getMessage() : hierarchyCheckResult.getMessage();
  }

  @Override
  public CharSequence getMessage(Locale locale) {
    return (hierarchyCheckResult == null)
        ? super.getMessage(locale)
        : hierarchyCheckResult.getMessage(locale);
  }

  // For debugging
  @Override
  public String toString() {
    return String.format(
        "[AccessibilityViewCheckResult check=%s view=%s]", hierarchyCheckResult, view);
  }
}

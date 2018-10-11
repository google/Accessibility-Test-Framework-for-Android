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

package com.google.android.apps.common.testing.accessibility.framework.uielement;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Parcel;
import androidx.core.view.ViewCompat;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Checkable;
import android.widget.TextView;
import com.google.android.apps.common.testing.accessibility.framework.ViewAccessibilityUtils;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableString;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.ViewHierarchyElementProto;
import com.google.common.base.Preconditions;
import com.googlecode.eyesfree.utils.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Representation of a {@link View} hierarchy for accessibility checking
 * <p>
 * These views hold references to surrounding {@link ViewHierarchyElement}s in its local view
 * hierarchy and the containing {@link WindowHierarchyElement}. An individual view may be uniquely
 * identified in the context of its containing {@link WindowHierarchyElement} by the {@code id}
 * value returned by {@link #getId()}, or it may be uniquely identified in the context of its
 * containing {@link AccessibilityHierarchy} by the {@code long} returned by
 * {@link #getCondensedUniqueId()}.
 */
public class ViewHierarchyElement {
  private static final boolean AT_18 =
      (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2);
  private static final boolean AT_16 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
  private static final boolean AT_11 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB);

  private final int id;
  private final @Nullable Integer parentId;

  // Created lazily, because many views are leafs.
  private @MonotonicNonNull List<Integer> childIds;

  // This field is set to a non-null value after construction.
  private @MonotonicNonNull WindowHierarchyElement windowElement;

  private final @Nullable CharSequence packageName;
  private final @Nullable CharSequence className;
  private final @Nullable CharSequence accessibilityClassName;
  private final @Nullable String resourceName;
  private final @Nullable SpannableString contentDescription;
  private final @Nullable SpannableString text;
  private final boolean importantForAccessibility;
  private final @Nullable Boolean visibleToUser;
  private final boolean clickable;
  private final boolean longClickable;
  private final boolean focusable;
  private final @Nullable Boolean editable;
  private final @Nullable Boolean scrollable;
  private final @Nullable Boolean canScrollForward;
  private final @Nullable Boolean canScrollBackward;
  private final @Nullable Boolean checkable;
  private final @Nullable Boolean checked;
  private final @Nullable Boolean hasTouchDelegate;
  private final @Nullable Rect boundsInScreen;
  private final @Nullable Integer nonclippedHeight;
  private final @Nullable Integer nonclippedWidth;
  private final @Nullable Float textSize;
  private final @Nullable Integer textColor;
  private final @Nullable Integer backgroundDrawableColor;
  private final @Nullable Integer typefaceStyle;
  private final boolean enabled;

  // Populated only after a hierarchy is constructed
  private @Nullable Long labeledById;
  private @Nullable Long accessibilityTraversalBeforeId;
  private @Nullable Long accessibilityTraversalAfterId;

  ViewHierarchyElement(
      int id, @Nullable ViewHierarchyElement parent, AccessibilityNodeInfo fromInfo) {
    // Bookkeeping
    this.id = id;
    this.parentId = (parent != null) ? parent.getId() : null;

    // API 18+ properties
    this.resourceName = AT_18 ? fromInfo.getViewIdResourceName() : null;
    this.editable = AT_18 ? fromInfo.isEditable() : null;

    // API 16+ properties
    this.visibleToUser = AT_16 ? fromInfo.isVisibleToUser() : null;

    // Base properties
    this.className = fromInfo.getClassName();
    this.packageName = fromInfo.getPackageName();
    this.accessibilityClassName = fromInfo.getClassName();
    this.contentDescription = SpannableString.valueOf(fromInfo.getContentDescription());
    this.text = SpannableString.valueOf(fromInfo.getText());
    this.importantForAccessibility = true;
    this.clickable = fromInfo.isClickable();
    this.longClickable = fromInfo.isLongClickable();
    this.focusable = fromInfo.isFocusable();
    this.scrollable = fromInfo.isScrollable();
    this.canScrollForward =
        ((fromInfo.getActions() & AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) != 0);
    this.canScrollBackward =
        ((fromInfo.getActions() & AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) != 0);
    this.checkable = fromInfo.isCheckable();
    this.checked = fromInfo.isChecked();
    this.hasTouchDelegate = false; /* Touch delegates are not considered by AccessibilityServices */
    android.graphics.Rect tempRect = new android.graphics.Rect();
    fromInfo.getBoundsInScreen(tempRect);
    this.boundsInScreen = new Rect(tempRect);
    this.nonclippedHeight = null; /* AccessibilityServices cannot discover nonclipped dimensions */
    this.nonclippedWidth = null; /* AccessibilityServices cannot discover nonclipped dimensions */
    this.textSize = null;
    this.textColor = null;
    this.backgroundDrawableColor = null;
    this.typefaceStyle = null;
    this.enabled = fromInfo.isEnabled();
  }

  ViewHierarchyElement(int id, @Nullable ViewHierarchyElement parent, View fromView) {
    // Bookkeeping
    this.id = id;
    this.parentId = (parent != null) ? parent.getId() : null;

    // API 16+ properties
    this.scrollable = AT_16 ? fromView.isScrollContainer() : null;

    // API 11+ properties
    this.backgroundDrawableColor =
        (AT_11 && (fromView != null) && (fromView.getBackground() instanceof ColorDrawable))
            ? ((ColorDrawable) fromView.getBackground()).getColor() : null;

    // Base properties
    this.visibleToUser = ViewAccessibilityUtils.isVisibleToUser(fromView);
    this.className = fromView.getClass().getName();
    this.accessibilityClassName = null;
    this.packageName = fromView.getContext().getPackageName();
    this.resourceName =
        (fromView.getId() != View.NO_ID) ? ViewAccessibilityUtils.getResourceNameForView(fromView)
            : null;
    this.contentDescription = SpannableString.valueOf(fromView.getContentDescription());
    this.enabled = fromView.isEnabled();
    if (fromView instanceof TextView) {
      TextView textView = (TextView) fromView;
      // Hint text takes precedence if no text is present.
      CharSequence text = textView.getText();
      if (TextUtils.isEmpty(text)) {
        text = textView.getHint();
      }
      this.text = SpannableString.valueOf(text);
      this.textSize = textView.getTextSize();
      this.textColor = textView.getCurrentTextColor();
      this.typefaceStyle =
          (textView.getTypeface() != null) ? textView.getTypeface().getStyle() : null;
    } else {
      this.text = null;
      this.textSize = null;
      this.textColor = null;
      this.typefaceStyle = null;
    }

    this.importantForAccessibility = ViewAccessibilityUtils.isImportantForAccessibility(fromView);
    this.clickable = fromView.isClickable();
    this.longClickable = fromView.isLongClickable();
    this.focusable = fromView.isFocusable();
    this.editable = ViewAccessibilityUtils.isViewEditable(fromView);
    this.canScrollForward = (ViewCompat.canScrollVertically(fromView, 1) || ViewCompat
        .canScrollHorizontally(fromView, 1));
    this.canScrollBackward = (ViewCompat.canScrollVertically(fromView, -1) || ViewCompat
        .canScrollHorizontally(fromView, -1));
    this.checkable = (fromView instanceof Checkable);
    this.checked = (fromView instanceof Checkable) ? ((Checkable) fromView).isChecked() : null;
    this.hasTouchDelegate = (fromView.getTouchDelegate() != null);

    // There may be subtle differences between the bounds from a View instance compared to that of
    // its AccessibilityNodeInfo. The latter uses a @hide getBoundsOnScreen method, which clips to
    // parent bounds.
    android.graphics.Rect tempRect = new android.graphics.Rect();
    if (fromView.getGlobalVisibleRect(tempRect)) {
      this.boundsInScreen = new Rect(tempRect);
    } else {
      this.boundsInScreen = null;
    }
    this.nonclippedHeight = fromView.getHeight();
    this.nonclippedWidth = fromView.getWidth();
  }

  ViewHierarchyElement(int id, @Nullable ViewHierarchyElement parent, Parcel in) {
    // Bookkeeping
    this.id = id;
    this.parentId = (parent != null) ? parent.getId() : null;

    packageName = ParcelUtils.readNullableString(in);
    className = ParcelUtils.readNullableString(in);
    resourceName = ParcelUtils.readNullableString(in);
    contentDescription =
        (in.readInt() == 1) ? in.readParcelable(getClass().getClassLoader()) : null;
    text = (in.readInt() == 1) ? in.readParcelable(getClass().getClassLoader()) : null;
    importantForAccessibility = in.readInt() != 0;
    visibleToUser = ParcelUtils.readNullableBoolean(in);
    clickable = in.readInt() != 0;
    longClickable = in.readInt() != 0;
    focusable = in.readInt() != 0;
    editable = ParcelUtils.readNullableBoolean(in);
    scrollable = ParcelUtils.readNullableBoolean(in);
    canScrollForward = ParcelUtils.readNullableBoolean(in);
    canScrollBackward = ParcelUtils.readNullableBoolean(in);
    checkable = ParcelUtils.readNullableBoolean(in);
    checked = ParcelUtils.readNullableBoolean(in);
    hasTouchDelegate = ParcelUtils.readNullableBoolean(in);
    enabled = in.readInt() != 0;
    boundsInScreen = (in.readInt() == 1) ? Rect.CREATOR.createFromParcel(in) : null;
    nonclippedHeight = ParcelUtils.readNullableInteger(in);
    nonclippedWidth = ParcelUtils.readNullableInteger(in);
    textSize = ParcelUtils.readNullableFloat(in);
    textColor = ParcelUtils.readNullableInteger(in);
    backgroundDrawableColor = ParcelUtils.readNullableInteger(in);
    typefaceStyle = ParcelUtils.readNullableInteger(in);
    labeledById = ParcelUtils.readNullableLong(in);
    accessibilityClassName = ParcelUtils.readNullableString(in);
    accessibilityTraversalBeforeId = ParcelUtils.readNullableLong(in);
    accessibilityTraversalAfterId = ParcelUtils.readNullableLong(in);
  }

  ViewHierarchyElement(ViewHierarchyElementProto proto) {
    checkNotNull(proto);

    // Bookkeeping
    this.id = proto.getId();
    this.parentId = (proto.getParentId() != -1) ? proto.getParentId() : null;
    if (proto.getChildIdsCount() > 0) {
      this.childIds = new ArrayList<>(proto.getChildIdsCount());
      this.childIds.addAll(proto.getChildIdsList());
    }

    packageName = proto.hasPackageName() ? proto.getPackageName() : null;
    className = proto.hasClassName() ? proto.getClassName() : null;
    accessibilityClassName =
        proto.hasAccessibilityClassName() ? proto.getAccessibilityClassName() : null;
    resourceName = proto.hasResourceName() ? proto.getResourceName() : null;
    contentDescription =
        proto.hasContentDescription() ? new SpannableString(proto.getContentDescription()) : null;
    text = proto.hasText() ? new SpannableString(proto.getText()) : null;
    importantForAccessibility = proto.getImportantForAccessibility();
    visibleToUser = proto.hasVisibleToUser() ? proto.getVisibleToUser() : null;
    clickable = proto.getClickable();
    longClickable = proto.getLongClickable();
    focusable = proto.getFocusable();
    editable = proto.hasEditable() ? proto.getEditable() : null;
    scrollable = proto.hasScrollable() ? proto.getScrollable() : null;
    canScrollForward = proto.hasCanScrollForward() ? proto.getCanScrollForward() : null;
    canScrollBackward = proto.hasCanScrollBackward() ? proto.getCanScrollBackward() : null;
    checkable = proto.hasCheckable() ? proto.getCheckable() : null;
    checked = proto.hasChecked() ?  proto.getChecked() : null;
    hasTouchDelegate = proto.hasHasTouchDelegate() ? proto.getHasTouchDelegate() : null;
    this.boundsInScreen = proto.hasBoundsInScreen() ? new Rect(proto.getBoundsInScreen()) : null;
    nonclippedHeight = proto.hasNonclippedHeight() ? proto.getNonclippedHeight() : null;
    nonclippedWidth = proto.hasNonclippedWidth() ? proto.getNonclippedWidth() : null;
    textSize = proto.hasTextSize() ? proto.getTextSize() : null;
    textColor = proto.hasTextColor() ? proto.getTextColor() : null;
    backgroundDrawableColor =
        proto.hasBackgroundDrawableColor() ? proto.getBackgroundDrawableColor() : null;
    typefaceStyle = proto.hasTypefaceStyle() ? proto.getTypefaceStyle() : null;
    enabled = proto.getEnabled();
    labeledById = proto.hasLabeledById() ? proto.getLabeledById() : null;
    accessibilityTraversalBeforeId =
        proto.hasAccessibilityTraversalBeforeId()
            ? proto.getAccessibilityTraversalBeforeId()
            : null;
    accessibilityTraversalAfterId =
        proto.hasAccessibilityTraversalAfterId() ? proto.getAccessibilityTraversalAfterId() : null;
  }

  /**
   * @return The value uniquely identifying this window within the context of its containing
   *         {@link WindowHierarchyElement}
   */
  public int getId() {
    return id;
  }

  /**
   * @return a value uniquely representing this {@link ViewHierarchyElement} and its containing
   *         {@link WindowHierarchyElement} in the context of it's containing
   *         {@link AccessibilityHierarchy}.
   */
  public long getCondensedUniqueId() {
    return (((long) getWindow().getId() << 32) | getId());
  }

  /**
   * @return The parent {@link ViewHierarchyElement} of this view, or {@code null} if this is a root
   *     view.
   * @see AccessibilityNodeInfo#getParent()
   * @see View#getParent()
   */
  @Pure
  public @Nullable ViewHierarchyElement getParentView() {
    return (parentId != null) ? getWindow().getViewById(parentId) : null;
  }

  /**
   * @return The number of child {@link ViewHierarchyElement}s rooted at this view
   *
   * @see AccessibilityNodeInfo#getChildCount()
   * @see ViewGroup#getChildCount()
   */
  public int getChildViewCount() {
    return (childIds == null) ? 0 : childIds.size();
  }

  /**
   * @param atIndex The index of the child {@link ViewHierarchyElement} to obtain. Must be &ge 0 and
   *        &lt {@link #getChildViewCount()}.
   * @return The requested child, or {@code null} if no such child exists at the given
   *         {@code atIndex}
   * @throws NoSuchElementException if {@code atIndex} is less than 0 or greater than
   *         {@code getChildViewCount() - 1}
   */
  public ViewHierarchyElement getChildView(int atIndex) {
    if ((atIndex < 0) || (childIds == null) || (atIndex >= childIds.size())) {
      throw new NoSuchElementException();
    }
    return getWindow().getViewById(childIds.get(atIndex));
  }

  /**
   * @return an unmodifiable {@link List} containing this {@link ViewHierarchyElement} and any
   *         descendants, direct or indirect, in depth-first ordering.
   */
  public List<ViewHierarchyElement> getSelfAndAllDescendants() {
    List<ViewHierarchyElement> listToPopulate = new ArrayList<>();
    listToPopulate.add(this);
    for (int i = 0; i < getChildViewCount(); ++i) {
      listToPopulate.addAll(getChildView(i).getSelfAndAllDescendants());
    }

    return Collections.unmodifiableList(listToPopulate);
  }

  /**
   * @return The containing {@link WindowHierarchyElement} of this view.
   */
  public WindowHierarchyElement getWindow() {

    // The type is explicit because the @MonotonicNonNull field is not read as @Nullable.
    return Preconditions.<@Nullable WindowHierarchyElement>checkNotNull(windowElement);
  }

  /**
   * @return The package name to which this view belongs, or {@code null} if one cannot be
   *     determined
   * @see AccessibilityNodeInfo#getPackageName()
   * @see Context#getPackageName()
   */
  public @Nullable CharSequence getPackageName() {
    return packageName;
  }

  /**
   * @return The class name to which this view belongs, or {@code null} if one cannot be determined
   * @see AccessibilityNodeInfo#getPackageName()
   * @see View#getClass()
   */
  public @Nullable CharSequence getClassName() {
    return className;
  }

  /**
   * @return The view id's associated resource name, or {@code null} if one cannot be determined or
   *     is not available
   * @see AccessibilityNodeInfo#getViewIdResourceName()
   * @see View#getId()
   * @see Resources#getResourceName(int)
   */
  @Pure
  public @Nullable String getResourceName() {
    return resourceName;
  }

  /**
   * Check if the {@link View} this element represents matches a particular class.
   *
   * @param referenceClass the class to check against the class of this element
   * @return {@link Boolean#TRUE} if the {@code View} this element represents is an instance of the
   *     class whose name is {@code referenceClass}. {@link Boolean#FALSE} if it does not. {@code
   *     null} if a determination cannot be made.
   */
  public @Nullable Boolean checkInstanceOf(Class<?> referenceClass) {
    if ((className == null) || (referenceClass == null)) {
      return null;
    }

    Class<?> targetClass = null;
    ClassLoader classLoader = getClass().getClassLoader();
    try {
      if (classLoader != null) {
        targetClass = classLoader.loadClass(className.toString());
      }
    } catch (ClassNotFoundException e) {
      // Do nothing
    }

    if (targetClass == null) {
      LogUtils.log(this, Log.WARN,
          "Unsuccessful attempt to resolve class %1$s while comparing against %2$s", className,
          referenceClass);
      return null;
    }

    return referenceClass.isAssignableFrom(targetClass);
  }

  /**
   * @return This view's content description, or {@code null} if one is not present
   * @see AccessibilityNodeInfo#getContentDescription()
   * @see View#getContentDescription()
   */
  public @Nullable SpannableString getContentDescription() {
    return contentDescription;
  }

  /**
   * Indicates whether the element is important for accessibility and would be reported to
   * accessibility services.
   * @see View#isImportantForAccessibility()
   * @see ViewAccessibilityUtils#isImportantForAccessibility(View)
   */
  public boolean isImportantForAccessibility() {
    return importantForAccessibility;
  }

  /**
   * @return This view's text content, or {@code null} if none is present
   * @see AccessibilityNodeInfo#getText()
   * @see TextView#getText()
   * @see TextView#getHint()
   */
  public @Nullable SpannableString getText() {
    return text;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is visible to the user, {@link Boolean#FALSE} if
   *     not, or {@code null} if this cannot be determined.
   * @see AccessibilityNodeInfo#isVisibleToUser()
   * @see ViewAccessibilityUtils#isVisibleToUser(View)
   */
  public @Nullable Boolean isVisibleToUser() {
    return visibleToUser;
  }

  /**
   * Indicates whether this view reports that it reacts to click events or not.
   *
   * @see AccessibilityNodeInfo#isClickable()
   * @see View#isClickable()
   */
  public boolean isClickable() {
    return clickable;
  }

  /**
   * Indicates whether this view reports that it reacts to long click events or not.
   *
   * @see AccessibilityNodeInfo#isLongClickable()
   * @see View#isLongClickable()
   */
  public boolean isLongClickable() {
    return longClickable;
  }

  /**
   * Indicates whether this view reports that it is currently able to take focus.
   *
   * @see AccessibilityNodeInfo#isFocusable()
   * @see View#isFocusable()
   */
  public boolean isFocusable() {
    return focusable;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is editable, {@link Boolean#FALSE} if not, or
   *     {@code null} if this cannot be determined.
   */
  public @Nullable Boolean isEditable() {
    return editable;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is potentially scrollable or indicated as a
   *     scrollable container, {@link Boolean#FALSE} if not, or {@code null} if this cannot be
   *     determined. Scrollable in this context refers only to a element's potential for being
   *     scrolled, and doesn't indicate if the container holds enough wrapped content to scroll. To
   *     determine if an element is actually scrollable based on contents use {@link
   *     #canScrollForward} or {@link #canScrollBackward}.
   */
  public @Nullable Boolean isScrollable() {
    return scrollable;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is scrollable in the "forward" direction, typically
   *         meaning either vertically downward or horizontally to the right (in left-to-right
   *         locales), {@link Boolean#FALSE} if not, or {@code null if this cannot be determined.
   */
  public @Nullable Boolean canScrollForward() {
    return canScrollForward;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is scrollable in the "backward" direction,
   *         typically meaning either vertically downward or horizontally to the right (in
   *         left-to-right locales), {@link Boolean#FALSE} if not, or {@code null if this cannot be
   *         determined.
   */
  public @Nullable Boolean canScrollBackward() {
    return canScrollBackward;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is checkable, {@link Boolean#FALSE} if not, or
   *     {@code null} if this cannot be determined.
   */
  public @Nullable Boolean isCheckable() {
    return checkable;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is checked, {@link Boolean#FALSE} if not, or {@code
   *     null} if this cannot be determined.
   */
  public @Nullable Boolean isChecked() {
    return checked;
  }

  /**
   * @return {@link Boolean#TRUE} if the element has a {@link TouchDelegate}, {@link Boolean#FALSE}
   *     if not, or {@code null} if this cannot be determined.
   */
  public @Nullable Boolean hasTouchDelegate() {
    return hasTouchDelegate;
  }

  /**
   * Retrieves the visible bounds of this element in absolute screen coordinates.
   * <p>
   * NOTE: This method provides dimensions that may be reduced in size due to clipping effects from
   * parent elements. To determine nonclipped dimensions, consider using
   * {@link #getNonclippedHeight()} and {@link #getNonclippedWidth}.
   *
   * @return the view's bounds, or {@link Rect#EMPTY} if the view's bounds are unavailable, such as
   * when it is positioned off-screen.
   *
   * @see AccessibilityNodeInfo#getBoundsInScreen(android.graphics.Rect)
   * @see View#getGlobalVisibleRect(android.graphics.Rect)
   */
  public Rect getBoundsInScreen() {
    return (boundsInScreen != null) ? boundsInScreen : Rect.EMPTY;
  }

  /**
   * Retrieves the visible bounds of this element in absolute screen coordinates. Suitable for use
   * in Android runtime environments.
   *
   * <p>NOTE: This method provides dimensions that may be reduced in size due to clipping effects
   * from parent elements. To determine nonclipped dimensions, consider using {@link
   * #getNonclippedHeight()} and {@link #getNonclippedWidth}.
   *
   * @param outBounds The destination {@link android.graphics.Rect} into which the view's bounds are
   *     copied, or if this view has no bounds, {@code outBounds}' {@link
   *     android.graphics.Rect#isEmpty()} will return {@code true}.
   * @see AccessibilityNodeInfo#getBoundsInScreen(android.graphics.Rect)
   * @see View#getGlobalVisibleRect(android.graphics.Rect)
   */
  public void getBoundsInScreen(android.graphics.Rect outBounds) {
    if (boundsInScreen != null) {
      outBounds.set(boundsInScreen.getAndroidInstance());
    } else {
      outBounds.setEmpty();
    }
  }

  /**
   * @return the height of this element (in raw pixels) not taking into account clipping effects
   *     applied by parent elements.
   * @see View#getHeight()
   */
  public @Nullable Integer getNonclippedHeight() {
    return nonclippedHeight;
  }

  /**
   * @return the width of this element (in raw pixels) not taking into account clipping effects
   *     applied by parent elements.
   * @see View#getWidth()
   */
  public @Nullable Integer getNonclippedWidth() {
    return nonclippedWidth;
  }

  /**
   * @return The size (in raw pixels) of the default text appearing in this view, or {@code null} if
   *     this cannot be determined
   * @see TextView#getTextSize()
   */
  public @Nullable Float getTextSize() {
    return textSize;
  }

  /**
   * @return The color of the default text appearing in this view, or {@code null} if this cannot be
   *     determined
   * @see TextView#getCurrentTextColor()
   */
  public @Nullable Integer getTextColor() {
    return textColor;
  }

  /**
   * @return The color of this View's background drawable, or {@code null} if the view does not have
   *     a {@link ColorDrawable} background
   * @see View#getBackground()
   * @see ColorDrawable#getColor()
   */
  public @Nullable Integer getBackgroundDrawableColor() {
    return backgroundDrawableColor;
  }

  /**
   * @return The style attributes of the {@link Typeface} of the default text appearing in this
   *     view, or @code null} if this cannot be determined.
   * @see TextView#getTypeface()
   * @see Typeface#getStyle()
   * @see Typeface#NORMAL
   * @see Typeface#BOLD
   * @see Typeface#ITALIC
   * @see Typeface#BOLD_ITALIC
   */
  @Pure
  public @Nullable Integer getTypefaceStyle() {
    return typefaceStyle;
  }

  /**
   * Returns the enabled status for this view.
   *
   * @see View#isEnabled()
   * @see AccessibilityNodeInfo#isEnabled()
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * @return The class name as reported to accessibility services, or {@code null} if this cannot be
   *     determined
   *     <p>NOTE: Unavailable for instances originally created from a {@link View}
   * @see AccessibilityNodeInfo#getClassName()
   */
  public @Nullable CharSequence getAccessibilityClassName() {
    return accessibilityClassName;
  }

  /**
   * @return the {@link ViewHierarchyElement} which acts as a label for this element, or
   *    {@code null} if this element is not labeled by another
   * @see AccessibilityNodeInfo#getLabeledBy()
   * @see AccessibilityNodeInfo#getLabelFor()
   * @see View#getLabelFor()
   */
  @Pure
  public @Nullable ViewHierarchyElement getLabeledBy() {
    return getViewHierarchyElementById(labeledById);
  }

  /**
   * @return a view before which this one is visited in accessibility traversal
   * @see AccessibilityNodeInfo#getTraversalBefore()
   * @see View#getAccessibilityTraversalBefore()
   */
  public @Nullable ViewHierarchyElement getAccessibilityTraversalBefore() {
    return getViewHierarchyElementById(accessibilityTraversalBeforeId);
  }

  /**
   * @return a view after which this one is visited in accessibility traversal
   * @see AccessibilityNodeInfo#getTraversalAfter()
   * @see View#getAccessibilityTraversalAfter()
   */
  public @Nullable ViewHierarchyElement getAccessibilityTraversalAfter() {
    return getViewHierarchyElementById(accessibilityTraversalAfterId);
  }

  void writeToParcel(Parcel out) {
    ParcelUtils.writeNullableString(out, packageName == null ? null : packageName.toString());
    ParcelUtils.writeNullableString(out, className == null ? null : className.toString());
    ParcelUtils.writeNullableString(out, resourceName);
    if (contentDescription != null) {
      out.writeInt(1);
      out.writeParcelable(contentDescription, 0);
    } else {
      out.writeInt(0);
    }
    if (text != null) {
      out.writeInt(1);
      out.writeParcelable(text, 0);
    } else {
      out.writeInt(0);
    }
    out.writeInt(importantForAccessibility ? 1 : 0);
    ParcelUtils.writeNullableBoolean(out, visibleToUser);
    out.writeInt(clickable ? 1 : 0);
    out.writeInt(longClickable ? 1 : 0);
    out.writeInt(focusable ? 1 : 0);
    ParcelUtils.writeNullableBoolean(out, editable);
    ParcelUtils.writeNullableBoolean(out, scrollable);
    ParcelUtils.writeNullableBoolean(out, checkable);
    ParcelUtils.writeNullableBoolean(out, checked);
    ParcelUtils.writeNullableBoolean(out, canScrollForward);
    ParcelUtils.writeNullableBoolean(out, canScrollBackward);
    ParcelUtils.writeNullableBoolean(out, hasTouchDelegate);
    out.writeInt(enabled ? 1 : 0);
    if (boundsInScreen != null) {
      out.writeInt(1);
      boundsInScreen.writeToParcel(out, 0);
    } else {
      out.writeInt(0);
    }
    ParcelUtils.writeNullableInteger(out, nonclippedHeight);
    ParcelUtils.writeNullableInteger(out, nonclippedWidth);
    ParcelUtils.writeNullableFloat(out, textSize);
    ParcelUtils.writeNullableInteger(out, textColor);
    ParcelUtils.writeNullableInteger(out, backgroundDrawableColor);
    ParcelUtils.writeNullableInteger(out, typefaceStyle);
    ParcelUtils.writeNullableLong(out, labeledById);
    ParcelUtils.writeNullableString(
        out, accessibilityClassName == null ? null : accessibilityClassName.toString());
    ParcelUtils.writeNullableLong(out, accessibilityTraversalBeforeId);
    ParcelUtils.writeNullableLong(out, accessibilityTraversalAfterId);
  }

  ViewHierarchyElementProto toProto() {
    ViewHierarchyElementProto.Builder builder = ViewHierarchyElementProto.newBuilder();
    // Bookkeeping
    builder.setId(id);
    if (parentId != null) {
      builder.setParentId(parentId);
    }
    if ((childIds != null) && !childIds.isEmpty()) {
      builder.addAllChildIds(childIds);
    }

    // View properties
    if (!TextUtils.isEmpty(packageName)) {
      builder.setPackageName(packageName.toString());
    }
    if (!TextUtils.isEmpty(className)) {
      builder.setClassName(className.toString());
    }
    if (!TextUtils.isEmpty(resourceName)) {
      builder.setResourceName(resourceName);
    }
    if (!TextUtils.isEmpty(contentDescription)) {
      builder.setContentDescription(contentDescription.toProto());
    }
    if (!TextUtils.isEmpty(text)) {
      builder.setText(text.toProto());
    }
    builder.setImportantForAccessibility(importantForAccessibility);
    if (visibleToUser != null) {
      builder.setVisibleToUser(visibleToUser);
    }
    builder.setClickable(clickable);
    builder.setLongClickable(longClickable);
    builder.setFocusable(focusable);
    if (editable != null) {
      builder.setEditable(editable);
    }
    if (scrollable != null) {
      builder.setScrollable(scrollable);
    }
    if (canScrollForward != null) {
      builder.setCanScrollForward(canScrollForward);
    }
    if (canScrollBackward != null) {
      builder.setCanScrollBackward(canScrollBackward);
    }
    if (checkable != null) {
      builder.setCheckable(checkable);
    }
    if (checked != null) {
      builder.setChecked(checked);
    }
    if (hasTouchDelegate != null) {
      builder.setHasTouchDelegate(hasTouchDelegate);
    }
    if (boundsInScreen != null) {
      builder.setBoundsInScreen(boundsInScreen.toProto());
    }
    if (nonclippedHeight != null) {
      builder.setNonclippedHeight(nonclippedHeight);
    }
    if (nonclippedWidth != null) {
      builder.setNonclippedWidth(nonclippedWidth);
    }
    if (textSize != null) {
      builder.setTextSize(textSize);
    }
    if (textColor != null) {
      builder.setTextColor(textColor);
    }
    if (backgroundDrawableColor != null) {
      builder.setBackgroundDrawableColor(backgroundDrawableColor);
    }
    if (typefaceStyle != null) {
      builder.setTypefaceStyle(typefaceStyle);
    }
    builder.setEnabled(enabled);
    if (labeledById != null) {
      builder.setLabeledById(labeledById);
    }
    if (accessibilityClassName != null) {
      builder.setAccessibilityClassName(accessibilityClassName.toString());
    }
    if (accessibilityTraversalBeforeId != null) {
      builder.setAccessibilityTraversalBeforeId(accessibilityTraversalBeforeId);
    }
    if (accessibilityTraversalAfterId != null) {
      builder.setAccessibilityTraversalAfterId(accessibilityTraversalAfterId);
    }
    return builder.build();
  }

  /** Set the containing {@link WindowHierarchyElement} of this view. */
  void setWindow(WindowHierarchyElement window) {
    this.windowElement = window;
  }

  /**
   * @param child The child {@link ViewHierarchyElement} to add as a child of this view
   */
  void addChild(ViewHierarchyElement child) {
    if (childIds == null) {
      childIds = new ArrayList<>();
    }
    childIds.add(child.id);
  }

  /**
   * Denotes that {@code labelingElement} acts as a label for this element
   *
   * @param labelingElement The element that labels this element, or {@code null} if this element is
   *        not labeled by another
   */
  void setLabeledBy(ViewHierarchyElement labelingElement) {
    labeledById = (labelingElement != null) ? labelingElement.getCondensedUniqueId() : null;
  }

  /**
   * Sets a view before which this one is visited in accessibility traversal. A screen-reader must
   * visit the content of this view before the content of the one it precedes.
   */
  void setAccessibilityTraversalBefore(ViewHierarchyElement element) {
    accessibilityTraversalBeforeId = element.getCondensedUniqueId();
  }

  /**
   * Sets a view after which this one is visited in accessibility traversal. A screen-reader must
   * visit the content of the other view before the content of this one.
   */
  void setAccessibilityTraversalAfter(ViewHierarchyElement element) {
    accessibilityTraversalAfterId = element.getCondensedUniqueId();
  }

  private @Nullable ViewHierarchyElement getViewHierarchyElementById(@Nullable Long id) {
    return (id != null) ? getWindow().getAccessibilityHierarchy().getViewById(id) : null;
  }
}

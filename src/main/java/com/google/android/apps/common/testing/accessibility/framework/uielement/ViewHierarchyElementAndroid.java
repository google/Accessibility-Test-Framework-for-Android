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

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.widget.Checkable;
import android.widget.Switch;
import android.widget.TextView;
import com.google.android.apps.common.testing.accessibility.framework.ViewAccessibilityUtils;
import com.google.android.apps.common.testing.accessibility.framework.replacements.LayoutParams;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableString;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableStringAndroid;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableStringBuilder;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityNodeInfoExtraDataExtractor.ExtraData;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.ViewHierarchyElementProto;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Representation of a {@link View} hierarchy for accessibility checking
 *
 * <p>These views hold references to surrounding {@link ViewHierarchyElementAndroid}s in its local
 * view hierarchy and the containing {@link WindowHierarchyElementAndroid}. An individual view may
 * be uniquely identified in the context of its containing {@link WindowHierarchyElementAndroid} by
 * the {@code id} value returned by {@link #getId()}, or it may be uniquely identified in the
 * context of its containing {@link AccessibilityHierarchy} by the {@code long} returned by {@link
 * #getCondensedUniqueId()}.
 */
public class ViewHierarchyElementAndroid extends ViewHierarchyElement {
  private static final boolean AT_30 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R);
  private static final boolean AT_29 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
  private static final boolean AT_28 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P);
  private static final boolean AT_26 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
  private static final boolean AT_24 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
  private static final boolean AT_23 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
  private static final boolean AT_21 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
  private static final boolean AT_18 =
      (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2);
  private static final boolean AT_16 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);

  // This field is set to a non-null value after construction.
  private @MonotonicNonNull WindowHierarchyElementAndroid windowElement;

  protected ViewHierarchyElementAndroid(
      int id,
      @Nullable Integer parentId,
      List<Integer> childIds,
      @Nullable CharSequence packageName,
      @Nullable CharSequence className,
      @Nullable CharSequence accessibilityClassName,
      @Nullable String resourceName,
      @Nullable SpannableString contentDescription,
      @Nullable SpannableString text,
      @Nullable SpannableString stateDescription,
      boolean importantForAccessibility,
      @Nullable Boolean visibleToUser,
      boolean clickable,
      boolean longClickable,
      boolean focusable,
      @Nullable Boolean editable,
      @Nullable Boolean scrollable,
      @Nullable Boolean canScrollForward,
      @Nullable Boolean canScrollBackward,
      @Nullable Boolean checkable,
      @Nullable Boolean checked,
      @Nullable Boolean hasTouchDelegate,
      boolean isScreenReaderFocusable,
      List<Rect> touchDelegateBounds,
      @Nullable Rect boundsInScreen,
      @Nullable Integer nonclippedHeight,
      @Nullable Integer nonclippedWidth,
      @Nullable Float textSize,
      @Nullable Integer textSizeUnit,
      @Nullable Integer textColor,
      @Nullable Integer backgroundDrawableColor,
      @Nullable Integer typefaceStyle,
      boolean enabled,
      @Nullable Long labeledById,
      @Nullable Long accessibilityTraversalBeforeId,
      @Nullable Long accessibilityTraversalAfterId,
      @Nullable Integer drawingOrder,
      List<Integer> superclassViews,
      List<ViewHierarchyActionAndroid> actionList,
      @Nullable LayoutParams layoutParams,
      @Nullable SpannableString hintText,
      @Nullable Integer hintTextColor,
      List<Rect> textCharacterLocations) {
    super(
        id,
        parentId,
        childIds,
        packageName,
        className,
        accessibilityClassName,
        resourceName,
        contentDescription,
        text,
        stateDescription,
        importantForAccessibility,
        visibleToUser,
        clickable,
        longClickable,
        focusable,
        editable,
        scrollable,
        canScrollForward,
        canScrollBackward,
        checkable,
        checked,
        hasTouchDelegate,
        isScreenReaderFocusable,
        touchDelegateBounds,
        boundsInScreen,
        nonclippedHeight,
        nonclippedWidth,
        textSize,
        textSizeUnit,
        textColor,
        backgroundDrawableColor,
        typefaceStyle,
        enabled,
        labeledById,
        accessibilityTraversalBeforeId,
        accessibilityTraversalAfterId,
        drawingOrder,
        superclassViews,
        actionList,
        layoutParams,
        hintText,
        hintTextColor,
        textCharacterLocations);
  }

  /**
   * @return The parent {@link ViewHierarchyElementAndroid} of this view, or {@code null} if this is
   *     a root view.
   * @see AccessibilityNodeInfo#getParent()
   * @see View#getParent()
   */
  @Pure
  @Override
  public @Nullable ViewHierarchyElementAndroid getParentView() {
    Integer parentIdTmp = parentId;
    return (parentIdTmp != null) ? getWindow().getViewById(parentIdTmp) : null;
  }

  /**
   * @param atIndex The index of the child {@link ViewHierarchyElementAndroid} to obtain. Must be
   *     &ge 0 and &lt {@link #getChildViewCount()}.
   * @return The requested child, or {@code null} if no such child exists at the given {@code
   *     atIndex}
   * @throws NoSuchElementException if {@code atIndex} is less than 0 or greater than {@code
   *     getChildViewCount() - 1}
   */
  @Override
  public ViewHierarchyElementAndroid getChildView(int atIndex) {
    if ((atIndex < 0) || (childIds == null) || (atIndex >= childIds.size())) {
      throw new NoSuchElementException();
    }
    return getWindow().getViewById(childIds.get(atIndex));
  }

  /**
   * @return an unmodifiable {@link List} containing this {@link ViewHierarchyElementAndroid} and
   *     any descendants, direct or indirect, in depth-first ordering.
   */
  @Override
  public List<ViewHierarchyElementAndroid> getSelfAndAllDescendants() {
    List<ViewHierarchyElementAndroid> listToPopulate = new ArrayList<>();
    listToPopulate.add(this);
    for (int i = 0; i < getChildViewCount(); ++i) {
      listToPopulate.addAll(getChildView(i).getSelfAndAllDescendants());
    }

    return Collections.unmodifiableList(listToPopulate);
  }

  /** Returns the containing {@link WindowHierarchyElementAndroid} of this view. */
  @Override
  public WindowHierarchyElementAndroid getWindow() {

    // The type is explicit because the @MonotonicNonNull field is not read as @Nullable.
    return Preconditions.<@Nullable WindowHierarchyElementAndroid>checkNotNull(windowElement);
  }

  /**
   * @return the {@link ViewHierarchyElementAndroid} which acts as a label for this element, or
   *     {@code null} if this element is not labeled by another
   * @see AccessibilityNodeInfo#getLabeledBy()
   * @see AccessibilityNodeInfo#getLabelFor()
   * @see View#getLabelFor()
   */
  @Pure
  @Override
  public @Nullable ViewHierarchyElementAndroid getLabeledBy() {
    return getViewHierarchyElementById(labeledById);
  }

  /**
   * @return a view before which this one is visited in accessibility traversal
   * @see AccessibilityNodeInfo#getTraversalBefore()
   * @see View#getAccessibilityTraversalBefore()
   */
  @Override
  public @Nullable ViewHierarchyElementAndroid getAccessibilityTraversalBefore() {
    return getViewHierarchyElementById(accessibilityTraversalBeforeId);
  }

  /**
   * @return a view after which this one is visited in accessibility traversal
   * @see AccessibilityNodeInfo#getTraversalAfter()
   * @see View#getAccessibilityTraversalAfter()
   */
  @Override
  public @Nullable ViewHierarchyElementAndroid getAccessibilityTraversalAfter() {
    return getViewHierarchyElementById(accessibilityTraversalAfterId);
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
   * @see android.view.accessibility.AccessibilityNodeInfo#getBoundsInScreen(android.graphics.Rect)
   * @see android.view.View#getGlobalVisibleRect(android.graphics.Rect)
   */
  public void getBoundsInScreen(android.graphics.Rect outBounds) {
    if (boundsInScreen != null) {
      outBounds.set(
          new android.graphics.Rect(
              boundsInScreen.getLeft(),
              boundsInScreen.getTop(),
              boundsInScreen.getRight(),
              boundsInScreen.getBottom()));
    } else {
      outBounds.setEmpty();
    }
  }

  void writeToParcel(Parcel out) {
    ParcelUtils.writeNullableString(out, packageName == null ? null : packageName.toString());
    ParcelUtils.writeNullableString(out, className == null ? null : className.toString());
    ParcelUtils.writeNullableString(out, resourceName);
    ParcelUtils.writeNullableSpannableString(out, contentDescription);
    ParcelUtils.writeNullableSpannableString(out, text);
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
    ParcelUtils.writeRectList(out, touchDelegateBounds);
    out.writeInt(enabled ? 1 : 0);
    Rect boundsInScreenTmp = boundsInScreen;
    if (boundsInScreenTmp != null) {
      out.writeInt(1);
      ParcelUtils.writeRectToParcel(boundsInScreenTmp, out);
    } else {
      out.writeInt(0);
    }
    ParcelUtils.writeNullableInteger(out, nonclippedHeight);
    ParcelUtils.writeNullableInteger(out, nonclippedWidth);
    ParcelUtils.writeNullableFloat(out, textSize);
    ParcelUtils.writeNullableInteger(out, textSizeUnit);
    ParcelUtils.writeNullableInteger(out, textColor);
    ParcelUtils.writeNullableInteger(out, backgroundDrawableColor);
    ParcelUtils.writeNullableInteger(out, typefaceStyle);
    ParcelUtils.writeNullableLong(out, labeledById);
    ParcelUtils.writeNullableString(
        out, accessibilityClassName == null ? null : accessibilityClassName.toString());
    ParcelUtils.writeNullableLong(out, accessibilityTraversalBeforeId);
    ParcelUtils.writeNullableLong(out, accessibilityTraversalAfterId);

    out.writeInt(superclassViews.size());
    for (int id : superclassViews) {
      out.writeInt(id);
    }
    ParcelUtils.writeNullableInteger(out, drawingOrder);

    out.writeInt(actionList.size());
    for (ViewHierarchyAction action : actionList) {
      ((ViewHierarchyActionAndroid) action).writeToParcel(out);
    }
    if (layoutParams != null) {
      out.writeInt(1);
      out.writeInt(layoutParams.getWidth());
      out.writeInt(layoutParams.getHeight());
    } else {
      out.writeInt(0);
    }
    ParcelUtils.writeNullableSpannableString(out, hintText);
    ParcelUtils.writeNullableInteger(out, hintTextColor);
    ParcelUtils.writeRectList(out, textCharacterLocations);
    ParcelUtils.writeNullableBoolean(out, isScreenReaderFocusable);
    ParcelUtils.writeNullableSpannableString(out, stateDescription);
  }

  /** Set the containing {@link WindowHierarchyElementAndroid} of this view. */
  void setWindow(WindowHierarchyElementAndroid window) {
    this.windowElement = window;
  }

  /** @param child The child {@link ViewHierarchyElementAndroid} to add as a child of this view */
  void addChild(ViewHierarchyElementAndroid child) {
    if (childIds == null) {
      childIds = new ArrayList<>();
    }
    childIds.add(child.id);
  }

  /**
   * Denotes that {@code labelingElement} acts as a label for this element
   *
   * @param labelingElement The element that labels this element, or {@code null} if this element is
   *     not labeled by another
   */
  void setLabeledBy(ViewHierarchyElementAndroid labelingElement) {
    labeledById = (labelingElement != null) ? labelingElement.getCondensedUniqueId() : null;
  }

  /**
   * Sets a view before which this one is visited in accessibility traversal. A screen-reader must
   * visit the content of this view before the content of the one it precedes.
   */
  void setAccessibilityTraversalBefore(ViewHierarchyElementAndroid element) {
    accessibilityTraversalBeforeId = element.getCondensedUniqueId();
  }

  /**
   * Sets a view after which this one is visited in accessibility traversal. A screen-reader must
   * visit the content of the other view before the content of this one.
   */
  void setAccessibilityTraversalAfter(ViewHierarchyElementAndroid element) {
    accessibilityTraversalAfterId = element.getCondensedUniqueId();
  }

  private @Nullable ViewHierarchyElementAndroid getViewHierarchyElementById(@Nullable Long id) {
    return (id != null) ? getWindow().getAccessibilityHierarchy().getViewById(id) : null;
  }

  /** Returns a new builder that can build a ViewHierarchyElementAndroid from a View. */
  static Builder newBuilder(
      int id,
      @Nullable ViewHierarchyElementAndroid parent,
      View fromView,
      CustomViewBuilderAndroid customViewBuilder,
      AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
    return new Builder(id, parent, fromView, customViewBuilder, extraDataExtractor);
  }

  /**
   * Returns a new builder that can build a ViewHierarchyElementAndroid with extra rendering data
   * from an AccessibilityNodeInfo.
   */
  static Builder newBuilder(
      int id,
      @Nullable ViewHierarchyElementAndroid parent,
      AccessibilityNodeInfo fromInfo,
      @Nullable AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
    return new Builder(id, parent, fromInfo, extraDataExtractor);
  }

  /** Returns a new builder that can build a ViewHierarchyElementAndroid from a proto. */
  static Builder newBuilder(ViewHierarchyElementProto proto) {
    return new Builder(checkNotNull(proto));
  }

  /** Returns a new builder that can build a ViewHierarchyElementAndroid from a Parcel. */
  static Builder newBuilder(int id, @Nullable ViewHierarchyElementAndroid parent, Parcel in) {
    return new Builder(id, parent, in);
  }

  /**
   * A builder for {@link ViewHierarchyElementAndroid}; obtained using {@link
   * ViewHierarchyElementAndroid#builder}.
   */
  public static class Builder {
    private final int id;
    private final @Nullable Integer parentId;
    private List<Integer> childIds = new ArrayList<>();
    private final @Nullable CharSequence packageName;
    private final @Nullable CharSequence className;
    private final @Nullable CharSequence accessibilityClassName;
    private final @Nullable String resourceName;
    private final @Nullable SpannableString contentDescription;
    private final @Nullable SpannableString text;
    private final @Nullable SpannableString stateDescription;
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
    private final boolean isScreenReaderFocusable;
    private final List<Rect> touchDelegateBounds;
    private final @Nullable Rect boundsInScreen;
    private final @Nullable Integer nonclippedHeight;
    private final @Nullable Integer nonclippedWidth;
    private final @Nullable Float textSize;
    private final @Nullable Integer textSizeUnit;
    private final @Nullable Integer textColor;
    private final @Nullable Integer backgroundDrawableColor;
    private final @Nullable Integer typefaceStyle;
    private final boolean enabled;
    private @Nullable Long labeledById;
    private @Nullable Long accessibilityTraversalBeforeId;
    private @Nullable Long accessibilityTraversalAfterId;
    private final List<Integer> superclassViews = new ArrayList<>();
    private final @Nullable Integer drawingOrder;
    private ImmutableList<ViewHierarchyActionAndroid> actionList;
    private final @Nullable LayoutParams layoutParams;
    private final @Nullable SpannableString hintText;
    private final @Nullable Integer hintTextColor;
    private final List<Rect> textCharacterLocations;

    Builder(
        int id,
        @Nullable ViewHierarchyElementAndroid parent,
        AccessibilityNodeInfo fromInfo,
        @Nullable AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
      // Bookkeeping
      this.id = id;
      this.parentId = (parent != null) ? parent.getId() : null;

      ExtraData extraData =
          (extraDataExtractor != null) ? extraDataExtractor.getExtraData(fromInfo) : null;

      // API 18+ properties
      this.resourceName = AT_18 ? fromInfo.getViewIdResourceName() : null;
      this.editable = AT_18 ? fromInfo.isEditable() : null;

      // API 16+ properties
      this.visibleToUser = AT_16 ? fromInfo.isVisibleToUser() : null;

      // API 21+ properties
      if (AT_21) {
        ImmutableList.Builder<ViewHierarchyActionAndroid> actionBuilder =
            new ImmutableList.Builder<>();
        actionBuilder.addAll(
            Lists.transform(
                fromInfo.getActionList(),
                action -> ViewHierarchyActionAndroid.newBuilder(action).build()));
        this.actionList = actionBuilder.build();
      }

      // API 24+ properties
      this.drawingOrder = AT_24 ? fromInfo.getDrawingOrder() : null;
      this.importantForAccessibility = AT_24 ? fromInfo.isImportantForAccessibility() : true;

      // API 28+ properties
      this.isScreenReaderFocusable = AT_28 && fromInfo.isScreenReaderFocusable();

      // API 29+ properties
      this.hasTouchDelegate = AT_29 ? (fromInfo.getTouchDelegateInfo() != null) : null;

      // API 30+ properties
      this.stateDescription =
          AT_30 ? SpannableStringAndroid.valueOf(fromInfo.getStateDescription()) : null;

      // Base properties
      this.className = fromInfo.getClassName();
      this.packageName = fromInfo.getPackageName();
      this.accessibilityClassName = fromInfo.getClassName();
      this.contentDescription = SpannableStringAndroid.valueOf(fromInfo.getContentDescription());
      this.text =
          (AT_26 && fromInfo.isShowingHintText())
              ? null
              : SpannableStringAndroid.valueOf(fromInfo.getText());
      this.clickable = fromInfo.isClickable();
      this.longClickable = fromInfo.isLongClickable();
      this.focusable = fromInfo.isFocusable();
      this.scrollable = fromInfo.isScrollable();
      this.canScrollForward =
          AT_21
              ? fromInfo.getActionList().contains(AccessibilityAction.ACTION_SCROLL_FORWARD)
              : ((fromInfo.getActions() & AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) != 0);
      this.canScrollBackward =
          AT_21
              ? fromInfo.getActionList().contains(AccessibilityAction.ACTION_SCROLL_BACKWARD)
              : ((fromInfo.getActions() & AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) != 0);
      this.checkable = fromInfo.isCheckable();
      this.checked = fromInfo.isChecked();
      this.touchDelegateBounds = new ArrayList<>(); // Populated after construction
      android.graphics.Rect tempRect = new android.graphics.Rect();
      fromInfo.getBoundsInScreen(tempRect);
      this.boundsInScreen = new Rect(tempRect.left, tempRect.top, tempRect.right, tempRect.bottom);
      this.nonclippedHeight = null;
      this.nonclippedWidth = null;
      this.textColor = null;
      this.backgroundDrawableColor = null;
      this.typefaceStyle = null;
      this.enabled = fromInfo.isEnabled();

      this.hintText =
          (AT_26 && fromInfo.isShowingHintText())
              ? SpannableStringAndroid.valueOf(fromInfo.getHintText())
              : null;
      this.hintTextColor = null;
      ImmutableList<Rect> characterLocations =
          (extraData != null) ? extraData.getTextCharacterLocations() : null;
      this.textCharacterLocations =
          (characterLocations != null) ? characterLocations : ImmutableList.of();
      this.textSize = (extraData != null) ? extraData.getTextSize() : null;
      this.textSizeUnit = (extraData != null) ? extraData.getTextSizeUnit() : null;
      Size layoutSize = (extraData != null) ? extraData.getLayoutSize() : null;
      this.layoutParams =
          (layoutSize == null)
              ? null
              : new LayoutParams(layoutSize.getWidth(), layoutSize.getHeight());
    }

    private Builder(
        int id,
        @Nullable ViewHierarchyElementAndroid parent,
        View fromView,
        CustomViewBuilderAndroid customViewBuilder,
        AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
      // Bookkeeping
      this.id = id;
      this.parentId = (parent != null) ? parent.getId() : null;

      this.drawingOrder = null;

      // API 16+ properties
      this.scrollable = AT_16 ? fromView.isScrollContainer() : null;

      // Base properties
      this.visibleToUser = ViewAccessibilityUtils.isVisibleToUser(fromView);
      this.className = fromView.getClass().getName();
      this.accessibilityClassName = AT_23 ? fromView.getAccessibilityClassName() : null;
      this.packageName = fromView.getContext().getPackageName();
      this.resourceName = ViewAccessibilityUtils.getResourceNameForView(fromView);
      this.contentDescription = SpannableStringAndroid.valueOf(fromView.getContentDescription());
      this.stateDescription =
          AT_30 ? SpannableStringAndroid.valueOf(fromView.getStateDescription()) : null;
      this.enabled = fromView.isEnabled();
      if (fromView instanceof TextView) {
        TextView textView = (TextView) fromView;
        CharSequence text = textView.getText();
        // Mimic the way Switch and SwitchCompat initialize AccessibilityNodeInfo.
        if ((fromView instanceof Switch) && !AT_30) {
          Switch swtch = (Switch) fromView;
          CharSequence switchText = swtch.isChecked() ? swtch.getTextOn() : swtch.getTextOff();
          this.text =
              new SpannableStringBuilder()
                  .append(SpannableStringAndroid.valueOf(text))
                  .appendWithSeparator(SpannableStringAndroid.valueOf(switchText))
                  .build();
        } else {
          this.text = SpannableStringAndroid.valueOf(text);
        }
        this.textSize = textView.getTextSize();
        this.textSizeUnit = AT_30 ? textView.getTextSizeUnit() : null;
        this.textColor = textView.getCurrentTextColor();
        this.typefaceStyle =
            (textView.getTypeface() != null) ? textView.getTypeface().getStyle() : null;
        this.hintText = SpannableStringAndroid.valueOf(textView.getHint());
        this.hintTextColor = textView.getCurrentHintTextColor();
        this.textCharacterLocations = extraDataExtractor.getTextCharacterLocations(textView);
      } else {
        this.text = null;
        this.textSize = null;
        this.textSizeUnit = null;
        this.textColor = null;
        this.typefaceStyle = null;
        this.hintText = null;
        this.hintTextColor = null;
        this.textCharacterLocations = ImmutableList.of();
      }

      this.importantForAccessibility = ViewAccessibilityUtils.isImportantForAccessibility(fromView);
      this.backgroundDrawableColor = getBackgroundDrawableColor(fromView);
      this.clickable = fromView.isClickable();
      this.longClickable = fromView.isLongClickable();
      this.focusable = fromView.isFocusable();
      this.editable = ViewAccessibilityUtils.isViewEditable(fromView);
      this.canScrollForward =
          (fromView.canScrollVertically(1) || fromView.canScrollHorizontally(1));
      this.canScrollBackward =
          (fromView.canScrollVertically(-1) || fromView.canScrollHorizontally(-1));
      this.checkable = customViewBuilder.isCheckable(fromView);
      this.checked = (fromView instanceof Checkable) ? ((Checkable) fromView).isChecked() : null;
      this.hasTouchDelegate = (fromView.getTouchDelegate() != null);
      this.isScreenReaderFocusable = AT_28 && fromView.isScreenReaderFocusable();
      this.touchDelegateBounds = ImmutableList.of(); // Unavailable from the View API
      this.boundsInScreen = getBoundsInScreen(fromView);
      this.nonclippedHeight = fromView.getHeight();
      this.nonclippedWidth = fromView.getWidth();
      this.actionList = ImmutableList.of(); // Unavailable from the View API

      ViewGroup.LayoutParams layoutParams = fromView.getLayoutParams();
      this.layoutParams =
          (layoutParams == null) ? null : new LayoutParams(layoutParams.width, layoutParams.height);
    }

    Builder(int id, @Nullable ViewHierarchyElementAndroid parent, Parcel in) {
      // Bookkeeping
      this.id = id;
      this.parentId = (parent != null) ? parent.getId() : null;

      packageName = ParcelUtils.readNullableString(in);
      className = ParcelUtils.readNullableString(in);
      resourceName = ParcelUtils.readNullableString(in);
      contentDescription = ParcelUtils.readNullableSpannableString(in);
      text = ParcelUtils.readNullableSpannableString(in);
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
      int touchDelegateBoundsSize = in.readInt();
      if (touchDelegateBoundsSize > 0) {
        ImmutableList.Builder<Rect> builder = new ImmutableList.Builder<>();
        for (int i = 0; i < touchDelegateBoundsSize; ++i) {
          builder.add(ParcelUtils.readRectFromParcel(in));
        }
        touchDelegateBounds = builder.build();
      } else {
        touchDelegateBounds = ImmutableList.of();
      }
      enabled = in.readInt() != 0;
      boundsInScreen = (in.readInt() == 1) ? ParcelUtils.readRectFromParcel(in) : null;
      nonclippedHeight = ParcelUtils.readNullableInteger(in);
      nonclippedWidth = ParcelUtils.readNullableInteger(in);
      textSize = ParcelUtils.readNullableFloat(in);
      textSizeUnit = ParcelUtils.readNullableInteger(in);
      textColor = ParcelUtils.readNullableInteger(in);
      backgroundDrawableColor = ParcelUtils.readNullableInteger(in);
      typefaceStyle = ParcelUtils.readNullableInteger(in);
      labeledById = ParcelUtils.readNullableLong(in);
      accessibilityClassName = ParcelUtils.readNullableString(in);
      accessibilityTraversalBeforeId = ParcelUtils.readNullableLong(in);
      accessibilityTraversalAfterId = ParcelUtils.readNullableLong(in);

      int superclassViewsSize = in.readInt();
      for (int i = 0; i < superclassViewsSize; i++) {
        this.superclassViews.add(in.readInt());
      }
      drawingOrder = ParcelUtils.readNullableInteger(in);

      int actionsSize = in.readInt();
      ImmutableList.Builder<ViewHierarchyActionAndroid> actionBuilder =
          new ImmutableList.Builder<>();
      for (int i = 0; i < actionsSize; i++) {
        actionBuilder.add(ViewHierarchyActionAndroid.newBuilder(in).build());
      }
      actionList = actionBuilder.build();
      layoutParams = (in.readInt() == 1) ? new LayoutParams(in.readInt(), in.readInt()) : null;
      hintText = ParcelUtils.readNullableSpannableString(in);
      hintTextColor = ParcelUtils.readNullableInteger(in);
      textCharacterLocations = ParcelUtils.readRectList(in);
      isScreenReaderFocusable = Boolean.TRUE.equals(ParcelUtils.readNullableBoolean(in));
      stateDescription = ParcelUtils.readNullableSpannableString(in);
    }

    Builder(ViewHierarchyElementProto proto) {
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
      stateDescription =
          proto.hasStateDescription() ? new SpannableString(proto.getStateDescription()) : null;
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
      checked = proto.hasChecked() ? proto.getChecked() : null;
      hasTouchDelegate = proto.hasHasTouchDelegate() ? proto.getHasTouchDelegate() : null;
      isScreenReaderFocusable = proto.getScreenReaderFocusable();
      if (proto.getTouchDelegateBoundsCount() > 0) {
        ImmutableList.Builder<Rect> builder = new ImmutableList.Builder<>();
        for (int i = 0; i < proto.getTouchDelegateBoundsCount(); ++i) {
          builder.add(new Rect(proto.getTouchDelegateBounds(i)));
        }
        touchDelegateBounds = builder.build();
      } else {
        touchDelegateBounds = ImmutableList.of();
      }
      boundsInScreen = proto.hasBoundsInScreen() ? new Rect(proto.getBoundsInScreen()) : null;
      nonclippedHeight = proto.hasNonclippedHeight() ? proto.getNonclippedHeight() : null;
      nonclippedWidth = proto.hasNonclippedWidth() ? proto.getNonclippedWidth() : null;
      textSize = proto.hasTextSize() ? proto.getTextSize() : null;
      textSizeUnit = proto.hasTextSizeUnit() ? proto.getTextSizeUnit() : null;
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
          proto.hasAccessibilityTraversalAfterId()
              ? proto.getAccessibilityTraversalAfterId()
              : null;
      this.superclassViews.addAll(proto.getSuperclassesList());
      drawingOrder = proto.hasDrawingOrder() ? proto.getDrawingOrder() : null;
      ImmutableList.Builder<ViewHierarchyActionAndroid> actionsBuilder =
          ImmutableList.<ViewHierarchyActionAndroid>builder();
      for (int i = 0; i < proto.getActionsCount(); i++) {
        actionsBuilder.add(ViewHierarchyActionAndroid.newBuilder(proto.getActions(i)).build());
      }
      actionList = actionsBuilder.build();
      layoutParams = proto.hasLayoutParams() ? new LayoutParams(proto.getLayoutParams()) : null;
      hintText = proto.hasHintText() ? new SpannableString(proto.getHintText()) : null;
      hintTextColor = proto.hasHintTextColor() ? proto.getHintTextColor() : null;
      if (proto.getTextCharacterLocationsCount() > 0) {
        ImmutableList.Builder<Rect> builder = new ImmutableList.Builder<>();
        for (int i = 0; i < proto.getTextCharacterLocationsCount(); ++i) {
          builder.add(new Rect(proto.getTextCharacterLocations(i)));
        }
        textCharacterLocations = builder.build();
      } else {
        textCharacterLocations = ImmutableList.of();
      }
    }

    public ViewHierarchyElementAndroid build() {
      return new ViewHierarchyElementAndroid(
          id,
          parentId,
          childIds,
          packageName,
          className,
          accessibilityClassName,
          resourceName,
          contentDescription,
          text,
          stateDescription,
          importantForAccessibility,
          visibleToUser,
          clickable,
          longClickable,
          focusable,
          editable,
          scrollable,
          canScrollForward,
          canScrollBackward,
          checkable,
          checked,
          hasTouchDelegate,
          isScreenReaderFocusable,
          touchDelegateBounds,
          boundsInScreen,
          nonclippedHeight,
          nonclippedWidth,
          textSize,
          textSizeUnit,
          textColor,
          backgroundDrawableColor,
          typefaceStyle,
          enabled,
          labeledById,
          accessibilityTraversalBeforeId,
          accessibilityTraversalAfterId,
          drawingOrder,
          superclassViews,
          actionList,
          layoutParams,
          hintText,
          hintTextColor,
          textCharacterLocations);
    }

    /** Try to get the background color of the View. */
    private static @Nullable Integer getBackgroundDrawableColor(View fromView) {
      Drawable background = fromView.getBackground();
      return (background instanceof ColorDrawable) ? ((ColorDrawable) background).getColor() : null;
    }

    /**
     * Try to match the result from {@link View#getBoundsInScreen}, which is not available because
     * it has @hide.
     *
     * <p>There may be subtle differences between the bounds from a View instance compared to that
     * of its AccessibilityNodeInfo. The latter uses View#getBoundsOnScreen method.
     */
    private static @Nullable Rect getBoundsInScreen(View fromView) {
      android.graphics.Rect tempRect = new android.graphics.Rect();
      if (!fromView.getGlobalVisibleRect(tempRect)) {
        return null;
      }

      int[] locationOnScreen = new int[2];
      int[] locationInWindow = new int[2];
      fromView.getLocationOnScreen(locationOnScreen);
      fromView.getLocationInWindow(locationInWindow);

      // Usually these offsets will be zero, except when the view is in a dialog window.
      int xOffset = locationOnScreen[0] - locationInWindow[0];
      int yOffset = locationOnScreen[1] - locationInWindow[1];
      tempRect.offset(xOffset, yOffset);
      return new Rect(tempRect.left, tempRect.top, tempRect.right, tempRect.bottom);
    }
  }
}

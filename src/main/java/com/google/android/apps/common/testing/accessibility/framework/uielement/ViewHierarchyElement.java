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
import static java.lang.Boolean.TRUE;

import com.google.android.apps.common.testing.accessibility.framework.replacements.LayoutParams;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableString;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.ViewHierarchyActionProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.ViewHierarchyElementProto;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Representation of a {@link android.view.View} hierarchy for accessibility checking
 *
 * <p>These views hold references to surrounding {@link ViewHierarchyElement}s in its local view
 * hierarchy and the containing {@link WindowHierarchyElement}. An individual view may be uniquely
 * identified in the context of its containing {@link WindowHierarchyElement} by the {@code id}
 * value returned by {@link #getId()}, or it may be uniquely identified in the context of its
 * containing {@link AccessibilityHierarchy} by the {@code long} returned by {@link
 * #getCondensedUniqueId()}.
 */
public class ViewHierarchyElement {
  protected final int id;
  protected final @Nullable Integer parentId;

  // Created lazily, because many views are leafs.
  protected @MonotonicNonNull List<Integer> childIds;

  // This field is set to a non-null value after construction.
  private @MonotonicNonNull WindowHierarchyElement windowElement;

  protected final @Nullable CharSequence packageName;
  protected final @Nullable CharSequence className;
  protected final @Nullable CharSequence accessibilityClassName;
  protected final @Nullable String resourceName;
  protected final @Nullable SpannableString contentDescription;
  protected final @Nullable SpannableString text;
  protected final @Nullable SpannableString stateDescription;
  protected final boolean importantForAccessibility;
  protected final @Nullable Boolean visibleToUser;
  protected final boolean clickable;
  protected final boolean longClickable;
  protected final boolean focusable;
  protected final @Nullable Boolean editable;
  protected final @Nullable Boolean scrollable;
  protected final @Nullable Boolean canScrollForward;
  protected final @Nullable Boolean canScrollBackward;
  protected final @Nullable Boolean checkable;
  protected final @Nullable Boolean checked;
  protected final @Nullable Boolean hasTouchDelegate;
  protected final boolean isScreenReaderFocusable;
  protected final @Nullable Rect boundsInScreen;
  protected final @Nullable Integer nonclippedHeight;
  protected final @Nullable Integer nonclippedWidth;
  protected final @Nullable Float textSize;
  protected final @Nullable Integer textSizeUnit;
  protected final @Nullable Integer textColor;
  protected final @Nullable Integer backgroundDrawableColor;
  protected final @Nullable Integer typefaceStyle;
  protected final boolean enabled;
  protected final @Nullable Integer drawingOrder;
  protected final ImmutableList<ViewHierarchyAction> actionList;
  protected final @Nullable LayoutParams layoutParams;
  protected final @Nullable SpannableString hintText; // only for TextView
  protected final @Nullable Integer hintTextColor; // only for TextView
  protected final ImmutableList<Rect> textCharacterLocations;

  // Populated only after a hierarchy is constructed
  protected @Nullable Long labeledById;
  protected @Nullable Long accessibilityTraversalBeforeId;
  protected @Nullable Long accessibilityTraversalAfterId;
  protected List<Rect> touchDelegateBounds;

  // A list of identifiers that represents all the superclasses of the corresponding view element.
  protected final List<Integer> superclassViews;

  protected ViewHierarchyElement(
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
      List<? extends ViewHierarchyAction> actionList,
      @Nullable LayoutParams layoutParams,
      @Nullable SpannableString hintText,
      @Nullable Integer hintTextColor,
      List<Rect> textCharacterLocations) {
    this.id = id;
    this.parentId = parentId;
    if (!childIds.isEmpty()) {
      this.childIds = new ArrayList<>(childIds.size());
      this.childIds.addAll(childIds);
    }
    this.packageName = packageName;
    this.className = className;
    this.accessibilityClassName = accessibilityClassName;
    this.resourceName = resourceName;
    this.contentDescription = contentDescription;
    this.text = text;
    this.stateDescription = stateDescription;
    this.importantForAccessibility = importantForAccessibility;
    this.visibleToUser = visibleToUser;
    this.clickable = clickable;
    this.longClickable = longClickable;
    this.focusable = focusable;
    this.editable = editable;
    this.scrollable = scrollable;
    this.canScrollForward = canScrollForward;
    this.canScrollBackward = canScrollBackward;
    this.checkable = checkable;
    this.checked = checked;
    this.hasTouchDelegate = hasTouchDelegate;
    this.isScreenReaderFocusable = isScreenReaderFocusable;
    this.touchDelegateBounds = touchDelegateBounds;
    this.boundsInScreen = boundsInScreen;
    this.nonclippedHeight = nonclippedHeight;
    this.nonclippedWidth = nonclippedWidth;
    this.textSize = textSize;
    this.textSizeUnit = textSizeUnit;
    this.textColor = textColor;
    this.backgroundDrawableColor = backgroundDrawableColor;
    this.typefaceStyle = typefaceStyle;
    this.enabled = enabled;
    this.labeledById = labeledById;
    this.accessibilityTraversalBeforeId = accessibilityTraversalBeforeId;
    this.accessibilityTraversalAfterId = accessibilityTraversalAfterId;
    this.drawingOrder = drawingOrder;
    this.superclassViews = superclassViews;
    if (actionList != null && !actionList.isEmpty()) {
      this.actionList = ImmutableList.copyOf(actionList);
    } else {
      this.actionList = ImmutableList.of();
    }
    this.layoutParams = layoutParams;
    this.hintText = hintText;
    this.hintTextColor = hintTextColor;
    this.textCharacterLocations = ImmutableList.copyOf(textCharacterLocations);
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
      ImmutableList.Builder<Rect> builder = ImmutableList.<Rect>builder();
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
        proto.hasAccessibilityTraversalAfterId() ? proto.getAccessibilityTraversalAfterId() : null;
    superclassViews = proto.getSuperclassesList();
    drawingOrder = proto.hasDrawingOrder() ? proto.getDrawingOrder() : null;
    ImmutableList.Builder<ViewHierarchyAction> actionBuilder = new ImmutableList.Builder<>();
    for (ViewHierarchyActionProto actionProto : proto.getActionsList()) {
      actionBuilder.add(new ViewHierarchyAction(actionProto));
    }
    actionList = actionBuilder.build();
    layoutParams = proto.hasLayoutParams() ? new LayoutParams(proto.getLayoutParams()) : null;
    hintText = proto.hasHintText() ? new SpannableString(proto.getHintText()) : null;
    hintTextColor = proto.hasHintTextColor() ? proto.getHintTextColor() : null;
    ImmutableList.Builder<Rect> characterLocations = ImmutableList.<Rect>builder();
    if (proto.getTextCharacterLocationsCount() > 0) {
      for (int i = 0; i < proto.getTextCharacterLocationsCount(); ++i) {
        characterLocations.add(new Rect(proto.getTextCharacterLocations(i)));
      }
    }
    textCharacterLocations = characterLocations.build();
  }

  /**
   * Returns the value uniquely identifying this window within the context of its containing {@link
   * WindowHierarchyElement}.
   */
  @Pure
  public int getId() {
    return id;
  }

  /**
   * @return a value uniquely representing this {@link ViewHierarchyElement} and its containing
   *     {@link WindowHierarchyElement} in the context of it's containing {@link
   *     AccessibilityHierarchy}.
   */
  public long getCondensedUniqueId() {
    return (((long) getWindow().getId() << 32) | getId());
  }

  /**
   * @return The parent {@link ViewHierarchyElement} of this view, or {@code null} if this is a root
   *     view.
   * @see android.view.accessibility.AccessibilityNodeInfo#getParent()
   * @see android.view.View#getParent()
   */
  @Pure
  public @Nullable ViewHierarchyElement getParentView() {
    Integer parentIdtmp = parentId;
    return (parentIdtmp != null) ? getWindow().getViewById(parentIdtmp) : null;
  }

  /**
   * @return The number of child {@link ViewHierarchyElement}s rooted at this view
   * @see android.view.accessibility.AccessibilityNodeInfo#getChildCount()
   * @see android.view.ViewGroup#getChildCount()
   */
  public int getChildViewCount() {
    return (childIds == null) ? 0 : childIds.size();
  }

  /**
   * @param atIndex The index of the child {@link ViewHierarchyElement} to obtain. Must be &ge 0 and
   *     &lt {@link #getChildViewCount()}.
   * @return The requested child, or {@code null} if no such child exists at the given {@code
   *     atIndex}
   * @throws NoSuchElementException if {@code atIndex} is less than 0 or greater than {@code
   *     getChildViewCount() - 1}
   */
  public ViewHierarchyElement getChildView(int atIndex) {
    if ((atIndex < 0) || (childIds == null) || (atIndex >= childIds.size())) {
      throw new NoSuchElementException();
    }
    return getWindow().getViewById(childIds.get(atIndex));
  }

  /**
   * @return an unmodifiable {@link List} containing this {@link ViewHierarchyElement} and any
   *     descendants, direct or indirect, in depth-first ordering.
   */
  public List<? extends ViewHierarchyElement> getSelfAndAllDescendants() {
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
   * @see android.view.accessibility.AccessibilityNodeInfo#getPackageName()
   * @see android.content.Context#getPackageName()
   */
  public @Nullable CharSequence getPackageName() {
    return packageName;
  }

  /**
   * @return The class name to which this view belongs, or {@code null} if one cannot be determined
   * @see android.view.accessibility.AccessibilityNodeInfo#getPackageName()
   * @see android.view.View#getClass()
   */
  public @Nullable CharSequence getClassName() {
    return className;
  }

  /**
   * @return The view id's associated resource name, or {@code null} if one cannot be determined or
   *     is not available
   * @see android.view.accessibility.AccessibilityNodeInfo#getViewIdResourceName()
   * @see android.view.View#getId()
   * @see android.content.res.Resources#getResourceName(int)
   */
  @Pure
  public @Nullable String getResourceName() {
    return resourceName;
  }

  /**
   * Check if the {@link android.view.View} this element represents matches a particular class using
   * its class name and accessibility class name if available.
   *
   * @param referenceClassName the name of the class to check against the class of this element.
   * @return true if the {@code android.view.View} this element represents is an instance of the
   *     class whose name is {@code referenceClassName}. False if it does not.
   */
  public boolean checkInstanceOf(String referenceClassName) {
    AccessibilityHierarchy hierarchy = getWindow().getAccessibilityHierarchy();
    Integer id = hierarchy.getViewElementClassNames().getIdentifierForClassName(referenceClassName);
    if (id == null) {
      return false;
    }
    return superclassViews.contains(id);
  }

  /**
   * Check if the {@link android.view.View} this element represents matches one of the classes.
   *
   * @param referenceClassNameList the list of names of classes to check against the class of this
   *     element.
   * @return true if the {@code android.view.View} this element represents is an instance of at
   *     least one of the class names in {@code referenceClassNameList}. False if it does not.
   */
  public boolean checkInstanceOfAny(List<String> referenceClassNameList) {
    for (String referenceClassName : referenceClassNameList) {
      if (checkInstanceOf(referenceClassName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return This view's content description, or {@code null} if one is not present
   * @see android.view.accessibility.AccessibilityNodeInfo#getContentDescription()
   * @see android.view.View#getContentDescription()
   */
  public @Nullable SpannableString getContentDescription() {
    return contentDescription;
  }

  /**
   * Indicates whether the element is important for accessibility and would be reported to
   * accessibility services.
   *
   * @see android.view.View#isImportantForAccessibility()
   */
  @Pure
  public boolean isImportantForAccessibility() {
    return importantForAccessibility;
  }

  /**
   * @return This view's text content, or {@code null} if none is present
   * @see android.view.accessibility.AccessibilityNodeInfo#getText()
   * @see android.widget.TextView#getText()
   * @see android.widget.TextView#getHint()
   */
  public @Nullable SpannableString getText() {
    return text;
  }

  /**
   * Returns the View's state description.
   *
   * @see android.view.getStateDescription()
   * @see android.view.accessibility.AccessibilityNodeInfo#getStateDescription()
   */
  @Pure
  public @Nullable SpannableString getStateDescription() {
    return stateDescription;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is visible to the user, {@link Boolean#FALSE} if
   *     not, or {@code null} if this cannot be determined.
   * @see android.view.accessibility.AccessibilityNodeInfo#isVisibleToUser()
   */
  @Pure
  public @Nullable Boolean isVisibleToUser() {
    return visibleToUser;
  }

  /**
   * Indicates whether this view reports that it reacts to click events or not.
   *
   * @see android.view.accessibility.AccessibilityNodeInfo#isClickable()
   * @see android.view.View#isClickable()
   */
  @Pure
  public boolean isClickable() {
    return clickable;
  }

  /**
   * Indicates whether this view reports that it reacts to long click events or not.
   *
   * @see android.view.accessibility.AccessibilityNodeInfo#isLongClickable()
   * @see android.view.View#isLongClickable()
   */
  @Pure
  public boolean isLongClickable() {
    return longClickable;
  }

  /**
   * Indicates whether this view reports that it is currently able to take focus.
   *
   * @see android.view.accessibility.AccessibilityNodeInfo#isFocusable()
   * @see android.view.View#isFocusable()
   */
  @Pure
  public boolean isFocusable() {
    return focusable;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is editable, {@link Boolean#FALSE} if not, or
   *     {@code null} if this cannot be determined.
   */
  @Pure
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
  @Pure
  public @Nullable Boolean isScrollable() {
    return scrollable;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is scrollable in the "forward" direction, typically
   *     meaning either vertically downward or horizontally to the right (in left-to-right locales),
   *     {@link Boolean#FALSE} if not, or {@code null} if this cannot be determined.
   */
  @Pure
  public @Nullable Boolean canScrollForward() {
    return canScrollForward;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is scrollable in the "backward" direction,
   *     typically meaning either vertically downward or horizontally to the right (in left-to-right
   *     locales), {@link Boolean#FALSE} if not, or {@code null} if this cannot be determined.
   */
  @Pure
  public @Nullable Boolean canScrollBackward() {
    return canScrollBackward;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is checkable, {@link Boolean#FALSE} if not, or
   *     {@code null} if this cannot be determined.
   */
  @Pure
  public @Nullable Boolean isCheckable() {
    return checkable;
  }

  /**
   * @return {@link Boolean#TRUE} if the element is checked, {@link Boolean#FALSE} if not, or {@code
   *     null} if this cannot be determined.
   */
  @Pure
  public @Nullable Boolean isChecked() {
    return checked;
  }

  /**
   * Returns {@link Boolean#TRUE} if the element has a {@link android.view.TouchDelegate}, {@link
   * Boolean#FALSE} if not, or {@code null} if this cannot be determined. This indicates only that
   * this element may be responsible for delegating its touches to another element.
   */
  @Pure
  public @Nullable Boolean hasTouchDelegate() {
    return hasTouchDelegate;
  }

  /**
   * Returns whether the view should be treated as a focusable unit by screenreaders.
   *
   * @see android.view.accessibility.AccessibilityNodeInfo#isScreenReaderFocusable()
   * @see android.view.View#isScreenReaderFocusable()
   */
  @Pure
  public boolean isScreenReaderFocusable() {
    return isScreenReaderFocusable;
  }

  /**
   * Returns a list of the touchable bounds of this element if rectangular {@link
   * android.view.TouchDelegate}s are used to modify this delegatee's hit region from another
   * delegating element.
   *
   * <p>NOTE: This is distinct from {@link #hasTouchDelegate()}, that indicates whether the element
   * may be a delegator of touches.
   */
  public List<Rect> getTouchDelegateBounds() {
    return touchDelegateBounds;
  }

  /** Returns a list of character locations in screen coordinates. */
  @Pure
  public List<Rect> getTextCharacterLocations() {
    return textCharacterLocations;
  }

  /**
   * Retrieves the visible bounds of this element in absolute screen coordinates.
   *
   * <p>NOTE: This method provides dimensions that may be reduced in size due to clipping effects
   * from parent elements. To determine nonclipped dimensions, consider using {@link
   * #getNonclippedHeight()} and {@link #getNonclippedWidth}.
   *
   * @return the view's bounds, or {@link Rect#EMPTY} if the view's bounds are unavailable, such as
   *     when it is positioned off-screen.
   * @see android.view.accessibility.AccessibilityNodeInfo#getBoundsInScreen(android.graphics.Rect)
   * @see android.view.View#getGlobalVisibleRect(android.graphics.Rect)
   */
  @Pure
  public Rect getBoundsInScreen() {
    return (boundsInScreen != null) ? boundsInScreen : Rect.EMPTY;
  }

  /**
   * @return the height of this element (in raw pixels) not taking into account clipping effects
   *     applied by parent elements.
   * @see android.view.View#getHeight()
   */
  @Pure
  public @Nullable Integer getNonclippedHeight() {
    return nonclippedHeight;
  }

  /**
   * @return the width of this element (in raw pixels) not taking into account clipping effects
   *     applied by parent elements.
   * @see android.view.View#getWidth()
   */
  @Pure
  public @Nullable Integer getNonclippedWidth() {
    return nonclippedWidth;
  }

  /**
   * @return The size (in raw pixels) of the default text appearing in this view, or {@code null} if
   *     this cannot be determined
   * @see android.widget.TextView#getTextSize()
   */
  @Pure
  public @Nullable Float getTextSize() {
    return textSize;
  }

  /*
   * @return the dimension type of the text size unit originally defined.
   * @see android.util.TypedValue#TYPE_DIMENSION
   */
  @Pure
  public @Nullable Integer getTextSizeUnit() {
    return textSizeUnit;
  }

  /**
   * @return The color of the default text appearing in this view, or {@code null} if this cannot be
   *     determined
   * @see android.widget.TextView#getCurrentTextColor()
   */
  @Pure
  public @Nullable Integer getTextColor() {
    return textColor;
  }

  /**
   * @return The color of this View's background drawable, or {@code null} if the view does not have
   *     a {@link android.graphics.drawable.ColorDrawable} background
   * @see android.view.View#getBackground()
   * @see android.graphics.drawable.ColorDrawable#getColor()
   */
  @Pure
  public @Nullable Integer getBackgroundDrawableColor() {
    return backgroundDrawableColor;
  }

  /**
   * Returns The style attributes of the {@link android.graphics.Typeface} of the default text
   * appearing in this view, or @code null} if this cannot be determined.
   *
   * @see android.widget.TextView#getTypeface()
   * @see android.graphics.Typeface#getStyle()
   * @see android.graphics.Typeface#NORMAL
   * @see android.graphics.Typeface#BOLD
   * @see android.graphics.Typeface#ITALIC
   * @see android.graphics.Typeface#BOLD_ITALIC
   */
  @Pure
  public @Nullable Integer getTypefaceStyle() {
    return typefaceStyle;
  }

  /**
   * Returns the enabled status for this view.
   *
   * @see android.view.View#isEnabled()
   * @see android.view.accessibility.AccessibilityNodeInfo#isEnabled()
   */
  @Pure
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * @return The class name as reported to accessibility services, or {@code null} if this cannot be
   *     determined
   *     <p>NOTE: Unavailable for instances originally created from a {@link android.view.View} on
   *     API < 23.
   * @see android.view.accessibility.AccessibilityNodeInfo#getClassName()
   */
  @Pure
  public @Nullable CharSequence getAccessibilityClassName() {
    return accessibilityClassName;
  }

  /**
   * Returns the {@link ViewHierarchyElement} which acts as a label for this element, or {@code
   * null} if this element is not labeled by another
   *
   * @see android.view.accessibility.AccessibilityNodeInfo#getLabeledBy()
   * @see android.view.accessibility.AccessibilityNodeInfo#getLabelFor()
   * @see android.view.View#getLabelFor()
   */
  public @Nullable ViewHierarchyElement getLabeledBy() {
    return getViewHierarchyElementById(labeledById);
  }

  /**
   * @return a view before which this one is visited in accessibility traversal
   * @see android.view.accessibility.AccessibilityNodeInfo#getTraversalBefore()
   * @see android.view.View#getAccessibilityTraversalBefore()
   */
  public @Nullable ViewHierarchyElement getAccessibilityTraversalBefore() {
    return getViewHierarchyElementById(accessibilityTraversalBeforeId);
  }

  /**
   * @return a view after which this one is visited in accessibility traversal
   * @see android.view.accessibility.AccessibilityNodeInfo#getTraversalAfter()
   * @see android.view.View#getAccessibilityTraversalAfter()
   */
  public @Nullable ViewHierarchyElement getAccessibilityTraversalAfter() {
    return getViewHierarchyElementById(accessibilityTraversalAfterId);
  }

  /**
   * Returns this element's drawing order within its parent, or {@code null} if the element's
   * drawing order is not available.
   *
   * @see android.view.accessibility.AccessibilityNodeInfo#getDrawingOrder()
   */
  @Pure
  public @Nullable Integer getDrawingOrder() {
    return drawingOrder;
  }

  /**
   * Returns how the element wants to be laid out in its parent, or {@code null} if the info is not
   * available.
   *
   * @see android.view.ViewGroup.LayoutParams
   */
  @Pure
  public @Nullable LayoutParams getLayoutParams() {
    return layoutParams;
  }

  /**
   * Returns the hint that is displayed when this view is a TextView and the text of the TextView is
   * empty, or {@code null} if the view is not a TextView, or if the info in not available.
   *
   * @see android.widget.TextView#getHint()
   */
  @Pure
  public @Nullable SpannableString getHintText() {
    return hintText;
  }

  /**
   * Returns the current color selected to paint the hint text, or {@code null} if this cannot be
   * determined.
   *
   * @see android.widget.TextView#getCurrentHintTextColor()
   */
  @Pure
  public @Nullable Integer getHintTextColor() {
    return hintTextColor;
  }

  /**
   * Returns {@code true} if this element {@link #isVisibleToUser} and its visible bounds are
   * adjacent to the scrollable edge of a scrollable container. This would indicate that the element
   * may be partially obscured by the container.
   */
  public boolean isAgainstScrollableEdge() {
    return TRUE.equals(isVisibleToUser()) && isAgaistScrollableEdgeOfAncestor(this);
  }

  private boolean isAgaistScrollableEdgeOfAncestor(ViewHierarchyElement view) {
    ViewHierarchyElement ancestor = view.getParentView();
    if (ancestor == null) {
      return false;
    }

    // See if this element is at the top or left edge of a scrollable container that can be scrolled
    // backward.
    if (TRUE.equals(ancestor.canScrollBackward())) {
      Rect scrollableBounds = ancestor.getBoundsInScreen();
      Rect descendantBounds = this.getBoundsInScreen();

      if ((descendantBounds.getTop() <= scrollableBounds.getTop())
          || (descendantBounds.getLeft() <= scrollableBounds.getLeft())) {
        return true;
      }
    }

    // See if this element is at the bottom or right edge of a scrollable container that can be
    // scrolled forward.
    if (TRUE.equals(ancestor.canScrollForward())) {
      Rect scrollableBounds = ancestor.getBoundsInScreen();
      Rect descendantBounds = this.getBoundsInScreen();

      if ((descendantBounds.getBottom() >= scrollableBounds.getBottom())
          || (descendantBounds.getRight() >= scrollableBounds.getRight())) {
        return true;
      }
    }

    // Recurse for ancestors.
    return isAgaistScrollableEdgeOfAncestor(ancestor);
  }

  @Override
  public int hashCode() {
    return getId();
  }

  @Override
  public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof ViewHierarchyElement)) {
      return false;
    }

    ViewHierarchyElement element = (ViewHierarchyElement) object;
    if (!propertiesEquals((ViewHierarchyElement) object)) {
      return false;
    }
    for (int i = 0; i < getChildViewCount(); i++) {
      if (!getChildView(i).equals(element.getChildView(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns a list of identifiers that represents all the superclasses of the corresponding view
   * element.
   */
  List<Integer> getSuperclassList() {
    return superclassViews;
  }

  /** Add a view class id to superclass list. */
  void addIdToSuperclassViewList(int id) {
    this.superclassViews.add(id);
  }

  /** Returns a list of actions exposed by this view element. */
  @Pure
  ImmutableList<ViewHierarchyAction> getActionList() {
    return actionList;
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
    if (!TextUtils.isEmpty(stateDescription)) {
      builder.setStateDescription(stateDescription.toProto());
    }
    builder.setImportantForAccessibility(importantForAccessibility);
    if (visibleToUser != null) {
      builder.setVisibleToUser(visibleToUser);
    }
    builder.setClickable(clickable).setLongClickable(longClickable).setFocusable(focusable);
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
    builder.setScreenReaderFocusable(isScreenReaderFocusable);
    for (Rect bounds : touchDelegateBounds) {
      builder.addTouchDelegateBounds(bounds.toProto());
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
    if (textSizeUnit != null) {
      builder.setTextSizeUnit(textSizeUnit);
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
    if (drawingOrder != null) {
      builder.setDrawingOrder(drawingOrder);
    }
    if (layoutParams != null) {
      builder.setLayoutParams(layoutParams.toProto());
    }
    if (!TextUtils.isEmpty(hintText)) {
      builder.setHintText(hintText.toProto());
    }
    if (hintTextColor != null) {
      builder.setHintTextColor(hintTextColor);
    }

    builder.addAllSuperclasses(superclassViews);
    for (ViewHierarchyAction action : actionList) {
      builder.addActions(action.toProto());
    }
    for (Rect rect : textCharacterLocations) {
      builder.addTextCharacterLocations(rect.toProto());
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
   *     not labeled by another
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

  /**
   * Adds a Rect to this element's list of {@code TouchDelegate} hit-Rect bounds, indicating it may
   * receive touches via another element within these bounds.
   *
   * <p>NOTE: This should be invoked only on instances from an AccessibilityHierarchy built from
   * {@link android.view.accessibility.AccessibilityNodeInfo}. Hierarchies built from other
   * structures expect this field to be Immutable after construction.
   */
  void addTouchDelegateBounds(Rect bounds) {
    touchDelegateBounds.add(bounds);
  }

  private @Nullable ViewHierarchyElement getViewHierarchyElementById(@Nullable Long id) {
    return (id != null) ? getWindow().getAccessibilityHierarchy().getViewById(id) : null;
  }

  private boolean propertiesEquals(ViewHierarchyElement element) {
    return (getCondensedUniqueId() == element.getCondensedUniqueId())
        && (getChildViewCount() == element.getChildViewCount())
        && TextUtils.equals(getPackageName(), element.getPackageName())
        && TextUtils.equals(getClassName(), element.getClassName())
        && TextUtils.equals(getResourceName(), element.getResourceName())
        && isImportantForAccessibility() == element.isImportantForAccessibility()
        && TextUtils.equals(getContentDescription(), element.getContentDescription())
        && TextUtils.equals(getText(), element.getText())
        && TextUtils.equals(getStateDescription(), element.getStateDescription())
        && Objects.equals(getTextColor(), element.getTextColor())
        && Objects.equals(getBackgroundDrawableColor(), element.getBackgroundDrawableColor())
        && Objects.equals(isVisibleToUser(), element.isVisibleToUser())
        && (isClickable() == element.isClickable())
        && (isLongClickable() == element.isLongClickable())
        && (isFocusable() == element.isFocusable())
        && Objects.equals(isEditable(), element.isEditable())
        && Objects.equals(isScrollable(), element.isScrollable())
        && Objects.equals(canScrollForward(), element.canScrollForward())
        && Objects.equals(canScrollBackward(), element.canScrollBackward())
        && Objects.equals(isCheckable(), element.isCheckable())
        && Objects.equals(isChecked(), element.isChecked())
        && Objects.equals(hasTouchDelegate(), element.hasTouchDelegate())
        && (isScreenReaderFocusable() == element.isScreenReaderFocusable())
        && Objects.equals(getTouchDelegateBounds(), element.getTouchDelegateBounds())
        && Objects.equals(getBoundsInScreen(), element.getBoundsInScreen())
        && Objects.equals(getNonclippedWidth(), element.getNonclippedWidth())
        && Objects.equals(getNonclippedHeight(), element.getNonclippedHeight())
        && Objects.equals(getTextSize(), element.getTextSize())
        && Objects.equals(getTextSizeUnit(), element.getTextSizeUnit())
        && Objects.equals(getTypefaceStyle(), element.getTypefaceStyle())
        && (isEnabled() == element.isEnabled())
        && condensedUniqueIdEquals(getLabeledBy(), element.getLabeledBy())
        && TextUtils.equals(getAccessibilityClassName(), element.getAccessibilityClassName())
        && condensedUniqueIdEquals(
            getAccessibilityTraversalAfter(), element.getAccessibilityTraversalAfter())
        && condensedUniqueIdEquals(
            getAccessibilityTraversalBefore(), element.getAccessibilityTraversalBefore())
        && Objects.equals(getDrawingOrder(), element.getDrawingOrder())
        && Objects.equals(getLayoutParams(), element.getLayoutParams())
        && TextUtils.equals(getHintText(), element.getHintText())
        && Objects.equals(getHintTextColor(), element.getHintTextColor())
        && Objects.equals(getTextCharacterLocations(), element.getTextCharacterLocations());
  }

  private static boolean condensedUniqueIdEquals(
      @Nullable ViewHierarchyElement ve1, @Nullable ViewHierarchyElement ve2) {
    return (ve1 == null)
        ? (ve2 == null)
        : ((ve2 != null) && (ve1.getCondensedUniqueId() == ve2.getCondensedUniqueId()));
  }
}

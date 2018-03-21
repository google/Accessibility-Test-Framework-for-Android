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
import static com.google.common.base.Preconditions.checkState;

import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.ViewHierarchyElementProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.WindowHierarchyElementProto;
import com.google.common.base.Preconditions;
import com.googlecode.eyesfree.utils.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Representation of a {@link Window} hierarchy for accessibility checking
 * <p>
 * These windows hold references to surrounding {@link WindowHierarchyElement}s in its local window
 * hierarchy, the window's root {@link ViewHierarchyElement}, and the containing
 * {@link AccessibilityHierarchy}. An individual window may be uniquely identified in the context of
 * an {@link AccessibilityHierarchy} by the {@code id} value returned by {@link #getId()}.
 */
public class WindowHierarchyElement {
  public static final int WINDOW_TYPE_APPLICATION = 1;
  public static final int WINDOW_TYPE_INPUT_METHOD = 2;
  public static final int WINDOW_TYPE_SYSTEM = 3;
  public static final int WINDOW_TYPE_ACCESSIBILITY = 4;

  /* The id of each view corresponds to its position in this list */
  private final List<ViewHierarchyElement> viewHierarchyElements;

  private final int id;
  private final @Nullable Integer parentId;
  private final List<Integer> childIds = new ArrayList<>();

  // This field is set to a non-null value after construction.
  private @MonotonicNonNull AccessibilityHierarchy accessibilityHierarchy;

  private final @Nullable Integer windowId;
  private final @Nullable Integer layer;
  private final @Nullable Integer type;
  private final @Nullable Boolean focused;
  private final @Nullable Boolean accessibilityFocused;
  private final @Nullable Boolean active;
  private final @Nullable Rect boundsInScreen;

  private WindowHierarchyElement(
      int id,
      @Nullable WindowHierarchyElement parent,
      AccessibilityWindowInfo fromWindow,
      Map<ViewHierarchyElement, AccessibilityNodeInfo> elementToNodeInfoMap) {
    // Bookkeeping
    this.id = id;
    this.parentId = (parent != null) ? parent.getId() : null;

    // Window properties
    this.windowId = fromWindow.getId();
    this.layer = fromWindow.getLayer();
    this.type = fromWindow.getType();
    this.focused = fromWindow.isFocused();
    this.accessibilityFocused = fromWindow.isAccessibilityFocused();
    this.active = fromWindow.isActive();

    android.graphics.Rect tempRect = new android.graphics.Rect();
    fromWindow.getBoundsInScreen(tempRect);
    this.boundsInScreen = new Rect(tempRect);

    // Build the window's view hierarchy
    AccessibilityNodeInfo rootInfo = fromWindow.getRoot();
    this.viewHierarchyElements = new ArrayList<>(); // The ultimate size is unknown
    if (rootInfo != null) {
      buildViewHierarchy(
          rootInfo, viewHierarchyElements, null /* no parent */, elementToNodeInfoMap);
      rootInfo.recycle();
    } else {
      // This could occur in the case where the application state changes between the time that the
      // AccessibilityWindowInfo object is obtained and when its root AccessibilityNodeInfo is
      // extracted.
      LogUtils.log(
          WindowHierarchyElement.class, Log.WARN,
          "Constructed WindowHierarchyElement with no valid root.");
    }
  }

  private WindowHierarchyElement(
      int id,
      @Nullable WindowHierarchyElement parent,
      AccessibilityNodeInfo fromRootNode,
      Map<ViewHierarchyElement, AccessibilityNodeInfo> elementToNodeInfoMap) {
    // Bookkeeping
    this.id = id;
    this.parentId = (parent != null) ? parent.getId() : null;

    // Window properties
    this.windowId = fromRootNode.getWindowId();
    android.graphics.Rect tempRect = new android.graphics.Rect();
    fromRootNode.getBoundsInScreen(tempRect);
    this.boundsInScreen = new Rect(tempRect);

    // We make the assumption that if we're passed a root node, it's coming from the active window
    // of an application.
    this.active = true;
    this.type = WindowHierarchyElement.WINDOW_TYPE_APPLICATION;

    // We can't evaluate other window properties from an AccessibilityNodeInfo instance.
    this.layer = null;
    this.focused = null;
    this.accessibilityFocused = null;

    // Build the window's view hierarchy
    this.viewHierarchyElements = new ArrayList<>(); // The ultimate size is unknown
    buildViewHierarchy(
        fromRootNode, viewHierarchyElements, null /* no parent */, elementToNodeInfoMap);
  }

  private WindowHierarchyElement(
      int id,
      @Nullable WindowHierarchyElement parent,
      View fromRootView,
      Map<ViewHierarchyElement, View> elementToViewMap) {
    // Bookkeeping
    this.id = id;
    this.parentId = (parent != null) ? parent.getId() : null;

    // Window properties
    android.graphics.Rect tempRect = new android.graphics.Rect();
    fromRootView.getWindowVisibleDisplayFrame(tempRect);
    this.boundsInScreen = new Rect(tempRect);

    // We make the assumption that if we have an instance of View, it's coming from the active
    // window of an application.
    this.active = true;
    this.type = WindowHierarchyElement.WINDOW_TYPE_APPLICATION;

    // We can't evaluate other window properties from a View instance.
    this.windowId = null;
    this.layer = null;
    this.focused = null;
    this.accessibilityFocused = null;
    this.viewHierarchyElements = new ArrayList<>(); // The ultimate size is unknown

    buildViewHierarchy(fromRootView, viewHierarchyElements, null /* no parent */, elementToViewMap);
  }

  private WindowHierarchyElement(int id, @Nullable WindowHierarchyElement parent, Parcel in) {
    // Bookkeeping
    this.id = id;
    this.parentId = (parent != null) ? parent.getId() : null;

    // Window properties
    this.windowId = ParcelUtils.readNullableInteger(in);
    this.layer = ParcelUtils.readNullableInteger(in);
    this.type = ParcelUtils.readNullableInteger(in);
    this.focused = ParcelUtils.readNullableBoolean(in);
    this.accessibilityFocused = ParcelUtils.readNullableBoolean(in);
    this.active = ParcelUtils.readNullableBoolean(in);

    if (in.readInt() == 1) {
      this.boundsInScreen = Rect.CREATOR.createFromParcel(in);
    } else {
      this.boundsInScreen = null;
    }

    int totalExpectedNodes = in.readInt();
    this.viewHierarchyElements = new ArrayList<>(totalExpectedNodes);
    if (totalExpectedNodes > 0) {
      buildViewHierarchy(in, viewHierarchyElements, null/* no parent */);
      checkState(
          totalExpectedNodes == viewHierarchyElements.size(),
          "View hierarchy failed consistency check.");
    }
  }

  private WindowHierarchyElement(WindowHierarchyElementProto proto) {
    // Bookkeeping
    this.id = proto.getId();
    this.parentId = (proto.getParentId() != -1) ? proto.getParentId() : null;
    this.childIds.addAll(proto.getChildIdsList());

    // Window properties
    this.windowId = proto.hasWindowId() ? proto.getWindowId() : null;
    this.layer = proto.hasLayer() ? proto.getLayer() : null;
    this.type = proto.hasType() ? proto.getType() : null;
    this.focused = proto.hasFocused() ? proto.getFocused() : null;
    this.accessibilityFocused =
        proto.hasAccessibilityFocused() ? proto.getAccessibilityFocused() : null;
    this.active = proto.hasActive() ? proto.getActive() : null;
    this.boundsInScreen = proto.hasBoundsInScreen() ? new Rect(proto.getBoundsInScreen()) : null;

    // Window contents
    int totalNodes = proto.getViewsCount();
    this.viewHierarchyElements = new ArrayList<>(totalNodes);
    for (ViewHierarchyElementProto view : proto.getViewsList()) {
      viewHierarchyElements.add(new ViewHierarchyElement(view));
    }
  }

  /**
   * @return The id uniquely identifying this window within the context of its containing
   *         {@link AccessibilityHierarchy}
   */
  public int getId() {
    return id;
  }

  /**
   * @return The root {@link ViewHierarchyElement} of this window, or {@code null} if the window
   *     does not have a root view.
   * @see AccessibilityWindowInfo#getRoot()
   */
  public @Nullable ViewHierarchyElement getRootView() {
    if (viewHierarchyElements.isEmpty()) {
      return null;
    }
    return viewHierarchyElements.get(0);
  }

  /**
   * Get all {@code ViewHierarchyElement}s in the window
   *
   * @return an unmodifiable {@link List} containing all {@link ViewHierarchyElement}s in this
   *         window, in depth-first ordering.
   */
  public List<ViewHierarchyElement> getAllViews() {
    return Collections.unmodifiableList(viewHierarchyElements);
  }

  /**
   * @return The parent {@link WindowHierarchyElement} of this window, or {@code null} if this
   *     window is a root window.
   * @see AccessibilityWindowInfo#getParent()
   */
  public @Nullable WindowHierarchyElement getParentWindow() {
    return (parentId != null) ? getAccessibilityHierarchy().getWindowById(parentId) : null;
  }

  /**
   * @return The number of child {@link WindowHierarchyElement}s rooted at this window
   *
   * @see AccessibilityWindowInfo#getChildCount()
   */
  public int getChildWindowCount() {
    return childIds.size();
  }

  /**
   * @param atIndex The index of the child {@link WindowHierarchyElement} to obtain.
   * @return The requested child window
   * @throws NoSuchElementException if {@code atIndex} is less than 0 or greater than
   *         {@code getChildWindowCount() - 1}
   * @see AccessibilityWindowInfo#getChild(int)
   */
  public WindowHierarchyElement getChildWindow(int atIndex) {
    if (atIndex < 0 || atIndex >= childIds.size()) {
      throw new NoSuchElementException();
    }
    return getAccessibilityHierarchy().getWindowById(childIds.get(atIndex));
  }

  /**
   * @param id The identifier for the desired {@link ViewHierarchyElement}, as returned by
   *        {@link ViewHierarchyElement#getId()}.
   * @return The {@link ViewHierarchyElement} identified by {@code id} in this window
   * @throws NoSuchElementException if no view within this window matches the provided {@code id}
   */
  public ViewHierarchyElement getViewById(int id) {
    if ((id < 0) || (id >= viewHierarchyElements.size())) {
      throw new NoSuchElementException();
    }
    return viewHierarchyElements.get(id);
  }

  /**
   * @return The containing {@link AccessibilityHierarchy} of this window.
   */
  public AccessibilityHierarchy getAccessibilityHierarchy() {

    // The type is explicit because the @MonotonicNonNull field is not read as @Nullable.
    return Preconditions.<@Nullable AccessibilityHierarchy>checkNotNull(accessibilityHierarchy);
  }

  /**
   * @return The system-defined window identifier for this window, or {@code null} if this cannot be
   *     determined
   * @see AccessibilityWindowInfo#getId()
   * @see AccessibilityNodeInfo#getWindowId()
   */
  public @Nullable Integer getWindowId() {
    return windowId;
  }

  /**
   * @return The layer value indicating z-ordering of this window, or {@code null} if this cannot be
   *     determined
   * @see AccessibilityWindowInfo#getLayer()
   */
  public @Nullable Integer getLayer() {
    return layer;
  }

  /**
   * @return One of {@link #WINDOW_TYPE_APPLICATION}, {@link #WINDOW_TYPE_INPUT_METHOD}, {@link
   *     #WINDOW_TYPE_SYSTEM}, {@link #WINDOW_TYPE_ACCESSIBILITY}, or {@code null} if this cannot be
   *     determined
   * @see AccessibilityWindowInfo#getType()
   */
  public @Nullable Integer getType() {
    return type;
  }

  /**
   * @return {@link Boolean#TRUE} if this window contains system input focus, {@link Boolean#FALSE}
   *     if not, or {@code null} if this cannot be determined
   * @see AccessibilityWindowInfo#isFocused()
   */
  public @Nullable Boolean isFocused() {
    return focused;
  }

  /**
   * @return {@link Boolean#TRUE} if this window contains system accessibility focus, {@link
   *     Boolean#FALSE} if not, or {@code null} if this cannot be determined
   * @see AccessibilityWindowInfo#isAccessibilityFocused()
   */
  public @Nullable Boolean isAccessibilityFocused() {
    return accessibilityFocused;
  }

  /**
   * @return {@link Boolean#TRUE} if this is considered the active window, {@link Boolean#FALSE} if
   *     not, or {@code null} if this cannot be determined
   * @see AccessibilityWindowInfo#isActive()
   */
  public @Nullable Boolean isActive() {
    return active;
  }

  /**
   * Retrieves the bounds of this window in absolute screen coordinates.
   *
   * @return the window's bounds, or {@link Rect#EMPTY} if the window's bounds are unavailable, such
   * as when it is positioned off-screen.
   *
   * @see AccessibilityWindowInfo#getBoundsInScreen(android.graphics.Rect)
   * @see View#getWindowVisibleDisplayFrame(android.graphics.Rect)
   */
  public Rect getBoundsInScreen() {
    return (boundsInScreen != null) ? boundsInScreen : Rect.EMPTY;
  }

  /**
   * Retrieves the bounds of this window in absolute screen coordinates. Suitable for use in Android
   * runtime environments.
   *
   * @param outBounds The destination {@link android.graphics.Rect} into which the window's bounds
   *     are copied, or if this window has no bounds, {@code outBounds}' {@link
   *     android.graphics.Rect#isEmpty()} will return {@code true}.
   * @see AccessibilityWindowInfo#getBoundsInScreen(android.graphics.Rect)
   */
  public void getBoundsInScreen(android.graphics.Rect outBounds) {
    if (boundsInScreen != null) {
      outBounds.set(boundsInScreen.getAndroidInstance());
    } else {
      outBounds.setEmpty();
    }
  }

  void writeToParcel(Parcel out) {
    ParcelUtils.writeNullableInteger(out, windowId);
    ParcelUtils.writeNullableInteger(out, layer);
    ParcelUtils.writeNullableInteger(out, type);
    ParcelUtils.writeNullableBoolean(out, focused);
    ParcelUtils.writeNullableBoolean(out, accessibilityFocused);
    ParcelUtils.writeNullableBoolean(out, active);
    if (boundsInScreen != null) {
      out.writeInt(1);
      boundsInScreen.writeToParcel(out, 0);
    } else {
      out.writeInt(0);
    }

    // Consistency check - the total number of view elements in the window
    out.writeInt(viewHierarchyElements.size());

    // Depth-first traversal of views
    if (!viewHierarchyElements.isEmpty()) {
      writeViewHierarchyToParcel(checkNotNull(getRootView()), out);
    }
  }

  private void writeViewHierarchyToParcel(ViewHierarchyElement element, Parcel out) {
    element.writeToParcel(out);
    int children = element.getChildViewCount();
    out.writeInt(children);
    for (int i = 0; i < children; ++i) {
      writeViewHierarchyToParcel(element.getChildView(i), out);
    }
  }

  /** Set the containing {@link AccessibilityHierarchy} of this window. */
  void setAccessibilityHierarchy(AccessibilityHierarchy accessibilityHierarchy) {
    this.accessibilityHierarchy = accessibilityHierarchy;
  }

  WindowHierarchyElementProto toProto() {
    WindowHierarchyElementProto.Builder builder = WindowHierarchyElementProto.newBuilder();
    // Bookkeeping
    builder.setId(id);
    if (parentId != null) {
      builder.setParentId(parentId);
    }
    if (!childIds.isEmpty()) {
      builder.addAllChildIds(childIds);
    }

    // Window properties
    if (windowId != null) {
      builder.setWindowId(windowId);
    }
    if (layer != null) {
      builder.setLayer(layer);
    }
    if (type != null) {
      builder.setType(type);
    }
    if (focused != null) {
      builder.setFocused(focused);
    }
    if (accessibilityFocused != null) {
      builder.setFocused(accessibilityFocused);
    }
    if (active != null) {
      builder.setActive(active);
    }
    if (boundsInScreen != null) {
      builder.setBoundsInScreen(boundsInScreen.toProto());
    }

    // Window contents
    for (ViewHierarchyElement view : viewHierarchyElements) {
      builder.addViews(view.toProto());
    }
    return builder.build();
  }

  /**
   * @param child The child {@link WindowHierarchyElement} to add as a child of this window
   */
  void addChild(WindowHierarchyElement child) {
    childIds.add(child.id);
  }

  /**
   * Create a new {@link ViewHierarchyElement} from an {@link AccessibilityNodeInfo} and appends it
   * and its children to {@code elementList}. The new elements' {@link ViewHierarchyElement#getId()}
   * will match their index in {@code elementList}. This also adds the newly created elements as
   * children to the provided {@code parent} element.
   *
   * @param forInfo The non-null {@link AccessibilityNodeInfo} from which to create the elements
   * @param elementList The list to hold the elements
   * @param parent The {@link ViewHierarchyElement} corresponding to {@code forInfo}'s parent, or
   *     {@code null} if {@code forInfo} is a root view.
   * @param elementToNodeInfoMap A {@link Map} to populate with the {@link ViewHierarchyElement}s
   *     created during construction of the hierarchy mapped to their originating {@link
   *     AccessibilityNodeInfo}s
   * @return The newly created element
   */
  private static ViewHierarchyElement buildViewHierarchy(
      AccessibilityNodeInfo forInfo,
      List<ViewHierarchyElement> elementList,
      @Nullable ViewHierarchyElement parent,
      Map<ViewHierarchyElement, AccessibilityNodeInfo> elementToNodeInfoMap) {
    checkNotNull(forInfo, "Attempted to build hierarchy from null root node");

    ViewHierarchyElement element = new ViewHierarchyElement(elementList.size(), parent, forInfo);
    elementList.add(element);
    elementToNodeInfoMap.put(element, AccessibilityNodeInfo.obtain(forInfo));

    for (int i = 0; i < forInfo.getChildCount(); ++i) {
      AccessibilityNodeInfo child = forInfo.getChild(i);
      if (child != null) {
        element.addChild(buildViewHierarchy(child, elementList, element, elementToNodeInfoMap));
        child.recycle();
      }
    }

    return element;
  }

  /**
   * Create a new {@link ViewHierarchyElement} from an {@link View} and appends it and its children
   * to {@code elementList}. The new elements' {@link ViewHierarchyElement#getId()} will match their
   * index in {@code elementList}. This also adds the newly created elements as children to the
   * provided {@code parent} element.
   *
   * @param forView The {@link View} from which to create the elements
   * @param elementList The list to hold the elements
   * @param parent The {@link ViewHierarchyElement} corresponding to {@code forView}'s parent, or
   *     {@code null} if {@code forView} is a root view.
   * @param elementToViewMap A {@link Map} to populate with the {@link ViewHierarchyElement}s
   *     created during construction of the hierarchy mapped to their originating {@link View}s
   * @return The newly created element
   */
  private static ViewHierarchyElement buildViewHierarchy(
      View forView,
      List<ViewHierarchyElement> elementList,
      @Nullable ViewHierarchyElement parent,
      Map<ViewHierarchyElement, View> elementToViewMap) {
    ViewHierarchyElement element = new ViewHierarchyElement(elementList.size(), parent, forView);
    elementList.add(element);
    elementToViewMap.put(element, forView);

    // Recurse for child views
    if (forView instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) forView;
      for (int i = 0; i < viewGroup.getChildCount(); ++i) {
        element.addChild(
            buildViewHierarchy(viewGroup.getChildAt(i), elementList, element, elementToViewMap));
      }
    }

    return element;
  }

  private static ViewHierarchyElement buildViewHierarchy(
      Parcel fromParcel,
      List<ViewHierarchyElement> elementList,
      @Nullable ViewHierarchyElement parent) {
    ViewHierarchyElement element = new ViewHierarchyElement(elementList.size(), parent, fromParcel);
    elementList.add(element);

    int childElementCount = fromParcel.readInt();
    for (int i = 0; i < childElementCount; ++i) {
      element.addChild(buildViewHierarchy(fromParcel, elementList, element));
    }

    return element;
  }

  /** Returns a new builder that can build a WindowHierarchyElement from a View. */
  static Builder newBuilder(int id, View view) {
    Builder builder = new Builder(id);
    builder.fromRootView = checkNotNull(view);
    return builder;
  }

  /**
   * Returns a new builder that can build a WindowHierarchyElement from an AccessibilityWindowInfo.
   */
  static Builder newBuilder(int id, AccessibilityWindowInfo window) {
    Builder builder = new Builder(id);
    builder.fromWindowInfo = checkNotNull(window);
    return builder;
  }

  /**
   * Returns a new builder that can build a WindowHierarchyElement from an AccessibilityNodeInfo.
   */
  static Builder newBuilder(int id, AccessibilityNodeInfo nodeInfo) {
    Builder builder = new Builder(id);
    builder.fromNodeInfo = checkNotNull(nodeInfo);
    return builder;
  }

  /** Returns a new builder that can build a WindowHierarchyElement from a proto. */
  static Builder newBuilder(WindowHierarchyElementProto proto) {
    int id = proto.getId();
    Builder builder = new Builder(id);
    builder.proto = checkNotNull(proto);
    return builder;
  }

  /** Returns a new builder that can build a WindowHierarchyElement from a Parcel. */
  static Builder newBuilder(int id, Parcel in) {
    Builder builder = new Builder(id);
    builder.in = checkNotNull(in);
    return builder;
  }

  /**
   * A builder for {@link WindowHierarchyElement}; obtained using {@link
   * WindowHierarchyElement#builder}.
   */
  public static class Builder {
    private final int id;
    private @Nullable View fromRootView;
    private @Nullable AccessibilityWindowInfo fromWindowInfo;
    private @Nullable AccessibilityNodeInfo fromNodeInfo;
    private @Nullable Parcel in;
    private @Nullable WindowHierarchyElementProto proto;
    private @Nullable WindowHierarchyElement parent;
    private @MonotonicNonNull Map<Long, AccessibilityNodeInfo> nodeInfoOriginMap;
    private @MonotonicNonNull Map<Long, View> viewOriginMap;

    Builder(int id) {
      this.id = id;
    }

    public Builder setParent(@Nullable WindowHierarchyElement parent) {
      this.parent = parent;
      return this;
    }

    public Builder setNodeInfoOriginMap(Map<Long, AccessibilityNodeInfo> originMap) {
      this.nodeInfoOriginMap = originMap;
      return this;
    }

    public Builder setViewOriginMap(Map<Long, View> originMap) {
      this.viewOriginMap = originMap;
      return this;
    }

    public WindowHierarchyElement build() {
      WindowHierarchyElement result;
      Map<ViewHierarchyElement, View> elementToViewMap = null;
      Map<ViewHierarchyElement, AccessibilityNodeInfo> elementToNodeInfoMap = null;

      if (fromRootView != null) {
        elementToViewMap = new HashMap<>();
        result = new WindowHierarchyElement(id, parent, fromRootView, elementToViewMap);
      } else if (fromWindowInfo != null) {
        elementToNodeInfoMap = new HashMap<>();
        result = new WindowHierarchyElement(id, parent, fromWindowInfo, elementToNodeInfoMap);
      } else if (fromNodeInfo != null) {
        elementToNodeInfoMap = new HashMap<>();
        result = new WindowHierarchyElement(id, parent, fromNodeInfo, elementToNodeInfoMap);
      } else if (in != null) {
        result = new WindowHierarchyElement(id, parent, in);
      } else if (proto != null) {
        result = new WindowHierarchyElement(proto);
      } else {
        throw new IllegalStateException("Nothing from which to build");
      }

      // Add entries to the origin maps after pointers to the window have been set.
      // The condensed unique IDs cannot be obtained without the window.
      setWindow(result);
      populateOriginMaps(elementToViewMap, elementToNodeInfoMap);
      return result;
    }

    private void populateOriginMaps(
        @Nullable Map<ViewHierarchyElement, View> elementToViewMap,
        @Nullable Map<ViewHierarchyElement, AccessibilityNodeInfo> elementToNodeInfoMap) {
      if (viewOriginMap != null) {
        for (Map.Entry<ViewHierarchyElement, View> entry :
            checkNotNull(elementToViewMap).entrySet()) {
          viewOriginMap.put(entry.getKey().getCondensedUniqueId(), entry.getValue());
        }
      }
      if (nodeInfoOriginMap != null) {
        for (Map.Entry<ViewHierarchyElement, AccessibilityNodeInfo> entry :
            checkNotNull(elementToNodeInfoMap).entrySet()) {
          nodeInfoOriginMap.put(entry.getKey().getCondensedUniqueId(), entry.getValue());
        }
      }
    }

    /** Set backpointers from the window's views to the window. */
    private void setWindow(WindowHierarchyElement window) {
      if (window.viewHierarchyElements != null) {
        for (ViewHierarchyElement view : window.viewHierarchyElements) {
          view.setWindow(window);
        }
      }
    }
  }
}

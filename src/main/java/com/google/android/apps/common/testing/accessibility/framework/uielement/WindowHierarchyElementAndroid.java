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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.ViewHierarchyElementProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.WindowHierarchyElementProto;
import com.google.android.libraries.accessibility.utils.log.LogUtils;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
 *
 * <p>These windows hold references to surrounding {@link WindowHierarchyElementAndroid}s in its
 * local window hierarchy, the window's root {@link ViewHierarchyElementAndroid}, and the containing
 * {@link AccessibilityHierarchy}. An individual window may be uniquely identified in the context of
 * an {@link AccessibilityHierarchy} by the {@code id} value returned by {@link #getId()}.
 */
public class WindowHierarchyElementAndroid extends WindowHierarchyElement {

  private static final String TAG = "WindowHierarchyElementA";

  /* The id of each view corresponds to its position in this list */
  private final List<ViewHierarchyElementAndroid> viewHierarchyElements;

  // This field is set to a non-null value after construction.
  private @MonotonicNonNull AccessibilityHierarchyAndroid accessibilityHierarchy;

  private WindowHierarchyElementAndroid(
      int id,
      @Nullable Integer parentId,
      List<Integer> childIds,
      @Nullable Integer windowId,
      @Nullable Integer layer,
      @Nullable Integer type,
      @Nullable Boolean focused,
      @Nullable Boolean accessibilityFocused,
      @Nullable Boolean active,
      @Nullable Rect boundsInScreen,
      List<ViewHierarchyElementAndroid> viewHierarchyElements) {
    super(
        id,
        parentId,
        childIds,
        windowId,
        layer,
        type,
        focused,
        accessibilityFocused,
        active,
        boundsInScreen);
    this.viewHierarchyElements = viewHierarchyElements;
  }

  /**
   * @return The root {@link ViewHierarchyElementAndroid} of this window, or {@code null} if the
   *     window does not have a root view.
   * @see AccessibilityWindowInfo#getRoot()
   */
  @Override
  public @Nullable ViewHierarchyElementAndroid getRootView() {
    if (viewHierarchyElements.isEmpty()) {
      return null;
    }
    return viewHierarchyElements.get(0);
  }

  /**
   * Get all {@code ViewHierarchyElementAndroid}s in the window
   *
   * @return an unmodifiable {@link List} containing all {@link ViewHierarchyElementAndroid}s in
   *     this window, in depth-first ordering.
   */
  @Override
  public List<ViewHierarchyElementAndroid> getAllViews() {
    return Collections.unmodifiableList(viewHierarchyElements);
  }

  /**
   * @return The parent {@link WindowHierarchyElement} of this window, or {@code null} if this
   *     window is a root window.
   * @see AccessibilityWindowInfo#getParent()
   */
  @Override
  public @Nullable WindowHierarchyElementAndroid getParentWindow() {
    Integer parentIdTmp = parentId;
    return (parentIdTmp != null) ? getAccessibilityHierarchy().getWindowById(parentIdTmp) : null;
  }

  /**
   * @param atIndex The index of the child {@link WindowHierarchyElementAndroid} to obtain.
   * @return The requested child window
   * @throws NoSuchElementException if {@code atIndex} is less than 0 or greater than {@code
   *     getChildWindowCount() - 1}
   * @see AccessibilityWindowInfo#getChild(int)
   */
  @Override
  public WindowHierarchyElementAndroid getChildWindow(int atIndex) {
    if (atIndex < 0 || atIndex >= childIds.size()) {
      throw new NoSuchElementException();
    }
    return getAccessibilityHierarchy().getWindowById(childIds.get(atIndex));
  }

  /**
   * @param id The identifier for the desired {@link ViewHierarchyElementAndroid}, as returned by
   *     {@link ViewHierarchyElementAndroid#getId()}.
   * @return The {@link ViewHierarchyElementAndroid} identified by {@code id} in this window
   * @throws NoSuchElementException if no view within this window matches the provided {@code id}
   */
  @Override
  public ViewHierarchyElementAndroid getViewById(int id) {
    if ((id < 0) || (id >= viewHierarchyElements.size())) {
      throw new NoSuchElementException();
    }
    return viewHierarchyElements.get(id);
  }

  /** Returns the containing {@link AccessibilityHierarchyAndroid} of this window. */
  @Override
  public AccessibilityHierarchyAndroid getAccessibilityHierarchy() {

    // The type is explicit because the @MonotonicNonNull field is not read as @Nullable.
    return Preconditions.<@Nullable AccessibilityHierarchyAndroid>checkNotNull(
        accessibilityHierarchy);
  }

  /**
   * Retrieves the bounds of this window in absolute screen coordinates. Suitable for use in Android
   * runtime environments.
   *
   * @param outBounds The destination {@link android.graphics.Rect} into which the window's bounds
   *     are copied, or if this window has no bounds, {@code outBounds}' {@link
   *     android.graphics.Rect#isEmpty()} will return {@code true}.
   * @see
   *     android.view.accessibility.AccessibilityWindowInfo#getBoundsInScreen(android.graphics.Rect)
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
    ParcelUtils.writeNullableInteger(out, windowId);
    ParcelUtils.writeNullableInteger(out, layer);
    ParcelUtils.writeNullableInteger(out, type);
    ParcelUtils.writeNullableBoolean(out, focused);
    ParcelUtils.writeNullableBoolean(out, accessibilityFocused);
    ParcelUtils.writeNullableBoolean(out, active);
    Rect boundsInScreenTmp = boundsInScreen;
    if (boundsInScreenTmp != null) {
      out.writeInt(1);
      out.writeInt(boundsInScreenTmp.getLeft());
      out.writeInt(boundsInScreenTmp.getTop());
      out.writeInt(boundsInScreenTmp.getRight());
      out.writeInt(boundsInScreenTmp.getBottom());
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

  private static void writeViewHierarchyToParcel(ViewHierarchyElementAndroid element, Parcel out) {
    element.writeToParcel(out);
    int children = element.getChildViewCount();
    out.writeInt(children);
    for (int i = 0; i < children; ++i) {
      writeViewHierarchyToParcel(element.getChildView(i), out);
    }
  }

  /** Set the containing {@link AccessibilityHierarchyAndroid} of this window. */
  void setAccessibilityHierarchy(AccessibilityHierarchyAndroid accessibilityHierarchy) {
    this.accessibilityHierarchy = accessibilityHierarchy;
  }

  @Override
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
    for (ViewHierarchyElementAndroid view : viewHierarchyElements) {
      builder.addViews(view.toProto());
    }
    return builder.build();
  }

  /**
   * @param child The child {@link WindowHierarchyElementAndroid} to add as a child of this window
   */
  void addChild(WindowHierarchyElementAndroid child) {
    childIds.add(child.id);
  }

  /** Returns a new builder that can build a WindowHierarchyElementAndroid from a View. */
  static BuilderAndroid newBuilder(
      int id,
      View view,
      CustomViewBuilderAndroid customViewBuilder,
      AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
    BuilderAndroid builder = new BuilderAndroid(id);
    builder.fromRootView = checkNotNull(view);
    builder.customViewBuilder = customViewBuilder;
    builder.aniExtraDataExtractor = extraDataExtractor;
    return builder;
  }

  /**
   * Returns a new builder that can build a WindowHierarchyElementAndroid from an
   * AccessibilityWindowInfo.
   */
  static BuilderAndroid newBuilder(
      int id,
      AccessibilityWindowInfo window,
      AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
    BuilderAndroid builder = new BuilderAndroid(id);
    builder.fromWindowInfo = checkNotNull(window);
    builder.aniExtraDataExtractor = extraDataExtractor;
    return builder;
  }

  /**
   * Returns a new builder that can build a WindowHierarchyElementAndroid from an
   * AccessibilityNodeInfo.
   */
  static BuilderAndroid newBuilder(
      int id,
      AccessibilityNodeInfo nodeInfo,
      AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
    BuilderAndroid builder = new BuilderAndroid(id);
    builder.fromNodeInfo = checkNotNull(nodeInfo);
    builder.aniExtraDataExtractor = extraDataExtractor;
    return builder;
  }

  /** Returns a new builder that can build a WindowHierarchyElementAndroid from a proto. */
  static BuilderAndroid newBuilder(WindowHierarchyElementProto proto) {
    int id = proto.getId();
    BuilderAndroid builder = new BuilderAndroid(id);
    builder.proto = checkNotNull(proto);
    return builder;
  }

  /** Returns a new builder that can build a WindowHierarchyElementAndroid from a Parcel. */
  static BuilderAndroid newBuilder(int id, Parcel in) {
    BuilderAndroid builder = new BuilderAndroid(id);
    builder.in = checkNotNull(in);
    return builder;
  }

  /**
   * A builder for {@link WindowHierarchyElementAndroid}; obtained using {@link
   * WindowHierarchyElementAndroid#builder}.
   */
  public static class BuilderAndroid extends Builder {
    private final int id;
    private @Nullable View fromRootView;
    private @Nullable CustomViewBuilderAndroid customViewBuilder;

    private @Nullable AccessibilityWindowInfo fromWindowInfo;
    private @Nullable AccessibilityNodeInfo fromNodeInfo;
    private @Nullable AccessibilityNodeInfoExtraDataExtractor aniExtraDataExtractor;
    private @Nullable Parcel in;
    private @Nullable WindowHierarchyElementAndroid parent;
    private @MonotonicNonNull Map<Long, AccessibilityNodeInfo> nodeInfoOriginMap;
    private @MonotonicNonNull Map<Long, View> viewOriginMap;

    private @Nullable Integer parentId;
    private final List<Integer> childIds = new ArrayList<>();
    private @Nullable Integer windowId;
    private @Nullable Integer layer;
    private @Nullable Integer type;
    private @Nullable Boolean focused;
    private @Nullable Boolean accessibilityFocused;
    private @Nullable Boolean active;
    private @Nullable Rect boundsInScreen;
    private List<ViewHierarchyElementAndroid> viewHierarchyElements;

    BuilderAndroid(int id) {
      super();
      this.id = id;
    }

    @CanIgnoreReturnValue
    public BuilderAndroid setParent(@Nullable WindowHierarchyElementAndroid parent) {
      this.parent = parent;
      return this;
    }

    @CanIgnoreReturnValue
    public BuilderAndroid setNodeInfoOriginMap(Map<Long, AccessibilityNodeInfo> originMap) {
      this.nodeInfoOriginMap = originMap;
      return this;
    }

    @CanIgnoreReturnValue
    public BuilderAndroid setViewOriginMap(Map<Long, View> originMap) {
      this.viewOriginMap = originMap;
      return this;
    }

    @Override
    public WindowHierarchyElementAndroid build() {
      WindowHierarchyElementAndroid result;
      Map<ViewHierarchyElementAndroid, View> elementToViewMap = null;
      Map<ViewHierarchyElementAndroid, AccessibilityNodeInfo> elementToNodeInfoMap = null;

      if (fromRootView != null) {
        elementToViewMap = new HashMap<>();
        result =
            construct(
                id,
                parent,
                fromRootView,
                elementToViewMap,
                checkNotNull(customViewBuilder),
                checkNotNull(aniExtraDataExtractor));
      } else if (fromWindowInfo != null) {
        elementToNodeInfoMap = new HashMap<>();
        result =
            construct(
                id,
                parent,
                fromWindowInfo,
                elementToNodeInfoMap,
                checkNotNull(aniExtraDataExtractor));
      } else if (fromNodeInfo != null) {
        elementToNodeInfoMap = new HashMap<>();
        result =
            construct(
                id,
                parent,
                fromNodeInfo,
                elementToNodeInfoMap,
                checkNotNull(aniExtraDataExtractor));
      } else if (in != null) {
        result = construct(id, parent, in);
      } else if (proto != null) {
        result = construct(proto);
      } else {
        throw new IllegalStateException("Nothing from which to build");
      }

      // Add entries to the origin maps after pointers to the window have been set.
      // The condensed unique IDs cannot be obtained without the window.
      setWindow(result);
      populateOriginMaps(elementToViewMap, elementToNodeInfoMap);
      return result;
    }

    /**
     * Creates a {@link ViewHierarchyElementAndroid.Builder} from a {@link View}.
     *
     * @param id The identifier for the desired {@link ViewHierarchyElementAndroid}
     * @param parent The {@link ViewHierarchyElementAndroid} corresponding to {@code forView}'s
     *     parent, or {@code null} if {@code forView} is a root view.
     * @param fromView The {@link View} from which to create the elements
     * @param customViewBuilder The {@link CustomViewBuilderAndroid} which customizes how to build
     *     an {@link AccessibilityHierarchyAndroid} from {@code forView}
     * @param extraDataExtractor The {@link AccessibilityNodeInfoExtraDataExtractor} for extracting
     *     extra rendering data
     * @return The newly created element
     */
    private static ViewHierarchyElementAndroid.Builder createViewHierarchyElementAndroidBuilder(
        int id,
        @Nullable ViewHierarchyElementAndroid parent,
        View fromView,
        CustomViewBuilderAndroid customViewBuilder,
        AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
      return ViewHierarchyElementAndroid.newBuilder(
          id, parent, fromView, customViewBuilder, extraDataExtractor);
    }

    /**
     * Create a new {@link ViewHierarchyElementAndroid} from a {@link View} and appends it and its
     * children to {@code elementList}. The new elements' {@link
     * ViewHierarchyElementAndroid#getId()} will match their index in {@code elementList}. This also
     * adds the newly created elements as children to the provided {@code parent} element.
     *
     * @param forView The {@link View} from which to create the elements
     * @param elementList The list to hold the elements
     * @param parent The {@link ViewHierarchyElementAndroid} corresponding to {@code forView}'s
     *     parent, or {@code null} if {@code forView} is a root view.
     * @param elementToViewMap A {@link Map} to populate with the {@link
     *     ViewHierarchyElementAndroid}s created during construction of the hierarchy mapped to
     *     their originating {@link View}s
     * @param customViewBuilder The {@link CustomViewBuilderAndroid} which customizes how to build
     *     an {@link AccessibilityHierarchyAndroid} from {@code forView}
     * @param extraDataExtractor The {@link AccessibilityNodeInfoExtraDataExtractor} for extracting
     *     extra rendering data
     * @return The newly created element
     */
    private static ViewHierarchyElementAndroid buildViewHierarchyFromView(
        View forView,
        List<ViewHierarchyElementAndroid> elementList,
        @Nullable ViewHierarchyElementAndroid parent,
        Map<ViewHierarchyElementAndroid, View> elementToViewMap,
        CustomViewBuilderAndroid customViewBuilder,
        AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
      ViewHierarchyElementAndroid element =
          createViewHierarchyElementAndroidBuilder(
                  elementList.size(), parent, forView, customViewBuilder, extraDataExtractor)
              .build();
      elementList.add(element);
      elementToViewMap.put(element, forView);

      // Recurse for child views
      if (forView instanceof ViewGroup) {
        ViewGroup viewGroup = (ViewGroup) forView;
        for (int i = 0; i < viewGroup.getChildCount(); ++i) {
          element.addChild(
              buildViewHierarchyFromView(
                  viewGroup.getChildAt(i),
                  elementList,
                  element,
                  elementToViewMap,
                  customViewBuilder,
                  extraDataExtractor));
        }
      }

      return element;
    }

    private static ViewHierarchyElementAndroid buildViewHierarchy(
        Parcel fromParcel,
        List<ViewHierarchyElementAndroid> elementList,
        @Nullable ViewHierarchyElementAndroid parent) {
      ViewHierarchyElementAndroid element =
          ViewHierarchyElementAndroid.newBuilder(elementList.size(), parent, fromParcel).build();
      elementList.add(element);

      int childElementCount = fromParcel.readInt();
      for (int i = 0; i < childElementCount; ++i) {
        element.addChild(buildViewHierarchy(fromParcel, elementList, element));
      }

      return element;
    }

    /**
     * Create a new {@link ViewHierarchyElementAndroid} from an {@link AccessibilityNodeInfo} and
     * appends it and its children to {@code elementList}. The new elements' {@link
     * ViewHierarchyElementAndroid#getId()} will match their index in {@code elementList}. This also
     * adds the newly created elements as children to the provided {@code parent} element.
     *
     * @param forInfo The non-null {@link AccessibilityNodeInfo} from which to create the elements
     * @param elementList The list to hold the elements
     * @param parent The {@link ViewHierarchyElementAndroid} corresponding to {@code forInfo}'s
     *     parent, or {@code null} if {@code forInfo} is a root view.
     * @param elementToNodeInfoMap A {@link Map} to populate with the {@link
     *     ViewHierarchyElementAndroid}s created during construction of the hierarchy mapped to
     *     their originating {@link AccessibilityNodeInfo}s
     * @param extraDataExtractor The {@link AccessibilityNodeInfoExtraDataExtractor} for extracting
     *     extra rendering data
     * @return The newly created element
     */
    private static ViewHierarchyElementAndroid buildViewHierarchy(
        AccessibilityNodeInfo forInfo,
        List<ViewHierarchyElementAndroid> elementList,
        @Nullable ViewHierarchyElementAndroid parent,
        Map<ViewHierarchyElementAndroid, AccessibilityNodeInfo> elementToNodeInfoMap,
        AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
      ViewHierarchyElementAndroid element =
          ViewHierarchyElementAndroid.newBuilder(
                  elementList.size(), parent, forInfo, extraDataExtractor)
              .build();
      elementList.add(element);
      elementToNodeInfoMap.put(element, AccessibilityNodeInfo.obtain(forInfo));

      for (int i = 0; i < forInfo.getChildCount(); ++i) {
        AccessibilityNodeInfo child = forInfo.getChild(i);
        if (child != null) {
          element.addChild(
              buildViewHierarchy(
                  child, elementList, element, elementToNodeInfoMap, extraDataExtractor));
          child.recycle();
        }
      }

      return element;
    }

    private WindowHierarchyElementAndroid construct(
        int id,
        @Nullable WindowHierarchyElementAndroid parent,
        AccessibilityWindowInfo fromWindow,
        Map<ViewHierarchyElementAndroid, AccessibilityNodeInfo> elementToNodeInfoMap,
        AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
      // Bookkeeping
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
      this.boundsInScreen = new Rect(tempRect.left, tempRect.top, tempRect.right, tempRect.bottom);

      // Build the window's view hierarchy
      AccessibilityNodeInfo rootInfo = fromWindow.getRoot();
      this.viewHierarchyElements = new ArrayList<>(); // The ultimate size is unknown
      if (rootInfo != null) {
        buildViewHierarchy(
            rootInfo,
            viewHierarchyElements,
            null /* no parent */,
            elementToNodeInfoMap,
            extraDataExtractor);
        rootInfo.recycle();
      } else {
        // This could occur in the case where the application state changes between the time that
        // the AccessibilityWindowInfo object is obtained and when its root AccessibilityNodeInfo is
        // extracted.
        LogUtils.w(TAG, "Constructed WindowHierarchyElement with no valid root.");
      }
      return new WindowHierarchyElementAndroid(
          id,
          parentId,
          childIds,
          windowId,
          layer,
          type,
          focused,
          accessibilityFocused,
          active,
          boundsInScreen,
          viewHierarchyElements);
    }

    private WindowHierarchyElementAndroid construct(
        int id,
        @Nullable WindowHierarchyElementAndroid parent,
        AccessibilityNodeInfo fromRootNode,
        Map<ViewHierarchyElementAndroid, AccessibilityNodeInfo> elementToNodeInfoMap,
        AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
      // Bookkeeping
      this.parentId = (parent != null) ? parent.getId() : null;

      // Window properties
      this.windowId = fromRootNode.getWindowId();
      android.graphics.Rect tempRect = new android.graphics.Rect();
      fromRootNode.getBoundsInScreen(tempRect);
      this.boundsInScreen = new Rect(tempRect.left, tempRect.top, tempRect.right, tempRect.bottom);

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
          fromRootNode,
          viewHierarchyElements,
          null /* no parent */,
          elementToNodeInfoMap,
          extraDataExtractor);
      return new WindowHierarchyElementAndroid(
          id,
          parentId,
          childIds,
          windowId,
          layer,
          type,
          focused,
          accessibilityFocused,
          active,
          boundsInScreen,
          viewHierarchyElements);
    }

    private WindowHierarchyElementAndroid construct(
        int id,
        @Nullable WindowHierarchyElementAndroid parent,
        View fromRootView,
        Map<ViewHierarchyElementAndroid, View> elementToViewMap,
        CustomViewBuilderAndroid customViewBuilder,
        AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
      // Bookkeeping
      this.parentId = (parent != null) ? parent.getId() : null;

      // Window properties
      android.graphics.Rect tempRect = new android.graphics.Rect();
      fromRootView.getWindowVisibleDisplayFrame(tempRect);
      this.boundsInScreen = new Rect(tempRect.left, tempRect.top, tempRect.right, tempRect.bottom);

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

      buildViewHierarchyFromView(
          fromRootView,
          viewHierarchyElements,
          null /* no parent */,
          elementToViewMap,
          customViewBuilder,
          extraDataExtractor);
      return new WindowHierarchyElementAndroid(
          id,
          parentId,
          childIds,
          windowId,
          layer,
          type,
          focused,
          accessibilityFocused,
          active,
          boundsInScreen,
          viewHierarchyElements);
    }

    private WindowHierarchyElementAndroid construct(
        int id, @Nullable WindowHierarchyElementAndroid parent, Parcel in) {
      // Bookkeeping
      this.parentId = (parent != null) ? parent.getId() : null;

      // Window properties
      this.windowId = ParcelUtils.readNullableInteger(in);
      this.layer = ParcelUtils.readNullableInteger(in);
      this.type = ParcelUtils.readNullableInteger(in);
      this.focused = ParcelUtils.readNullableBoolean(in);
      this.accessibilityFocused = ParcelUtils.readNullableBoolean(in);
      this.active = ParcelUtils.readNullableBoolean(in);

      if (in.readInt() == 1) {
        this.boundsInScreen =
            new Rect(
                /** left = */
                in.readInt(),
                /** top = */
                in.readInt(),
                /** right = */
                in.readInt(),
                /** bottom = */
                in.readInt());
      } else {
        this.boundsInScreen = null;
      }

      int totalExpectedNodes = in.readInt();
      this.viewHierarchyElements = new ArrayList<>(totalExpectedNodes);
      if (totalExpectedNodes > 0) {
        buildViewHierarchy(in, viewHierarchyElements, null /* no parent */);
        checkState(
            totalExpectedNodes == viewHierarchyElements.size(),
            "View hierarchy failed consistency check.");
      }
      return new WindowHierarchyElementAndroid(
          id,
          parentId,
          childIds,
          windowId,
          layer,
          type,
          focused,
          accessibilityFocused,
          active,
          boundsInScreen,
          viewHierarchyElements);
    }

    private WindowHierarchyElementAndroid construct(WindowHierarchyElementProto proto) {
      // Bookkeeping
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
        viewHierarchyElements.add(ViewHierarchyElementAndroid.newBuilder(view).build());
      }
      return new WindowHierarchyElementAndroid(
          proto.getId(),
          parentId,
          childIds,
          windowId,
          layer,
          type,
          focused,
          accessibilityFocused,
          active,
          boundsInScreen,
          viewHierarchyElements);
    }

    private void populateOriginMaps(
        @Nullable Map<ViewHierarchyElementAndroid, View> elementToViewMap,
        @Nullable Map<ViewHierarchyElementAndroid, AccessibilityNodeInfo> elementToNodeInfoMap) {
      if (viewOriginMap != null) {
        for (Map.Entry<ViewHierarchyElementAndroid, View> entry :
            checkNotNull(elementToViewMap).entrySet()) {
          viewOriginMap.put(entry.getKey().getCondensedUniqueId(), entry.getValue());
        }
      }
      if (nodeInfoOriginMap != null) {
        for (Map.Entry<ViewHierarchyElementAndroid, AccessibilityNodeInfo> entry :
            checkNotNull(elementToNodeInfoMap).entrySet()) {
          nodeInfoOriginMap.put(entry.getKey().getCondensedUniqueId(), entry.getValue());
        }
      }
    }

    /** Set backpointers from the window's views to the window. */
    private static void setWindow(WindowHierarchyElementAndroid window) {
      if (window.viewHierarchyElements != null) {
        for (ViewHierarchyElementAndroid view : window.viewHierarchyElements) {
          view.setWindow(window);
        }
      }
    }
  }
}

/*
 * Copyright (C) 2015 Google Inc.
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
package com.google.android.apps.common.testing.accessibility.framework.uielement;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.accessibilityservice.AccessibilityService;
import android.app.UiAutomation;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.AccessibilityHierarchyProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.WindowHierarchyElementProto;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.googlecode.eyesfree.utils.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

/**
 * Representation of a UI hierarchy for accessibility checking
 * <p>
 * Such a hierarchy may contain a forest of {@link WindowHierarchyElement}s, each of which contain a
 * tree of {@link ViewHierarchyElement}s.
 */
public class AccessibilityHierarchy implements Parcelable {
  private static final boolean AT_22 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1);
  private static final boolean AT_17 = (Build.VERSION.SDK_INT
      >= Build.VERSION_CODES.JELLY_BEAN_MR1);

  /* A representation of the device's state at the time a hierarchy is initially captured */
  private final DeviceState deviceState;

  /* The id of each window corresponds to its position in this list */
  private final List<WindowHierarchyElement> windowHierarchyElements;

  /* A reference to the 'active' window. Exactly one such window exists in any hierarchy. */
  private final WindowHierarchyElement activeWindow;

  private AccessibilityHierarchy(
      DeviceState deviceState,
      List<WindowHierarchyElement> windowHierarchyElements,
      WindowHierarchyElement activeWindow) {
    this.deviceState = deviceState;
    this.windowHierarchyElements = windowHierarchyElements;
    this.activeWindow = activeWindow;
  }

  /**
   * @return the {@link DeviceState} representing certain properties of the device at the time an
   *         {@link AccessibilityHierarchy} was originally captured.
   */
  public DeviceState getDeviceState() {
    return deviceState;
  }

  /**
   * Get all {@link WindowHierarchyElement}s in this hierarchy.
   *
   * @return An unmodifiable collection of all windows in hierarchy
   */
  public Collection<WindowHierarchyElement> getAllWindows() {
    return Collections.unmodifiableCollection(windowHierarchyElements);
  }

  /**
   * @return a {@link WindowHierarchyElement} representing the active window in this hierarchy. If
   *         this hierarchy was constructed from a {@link AccessibilityNodeInfo} or {@link View},
   *         this returns the default {@link WindowHierarchyElement} that was implicitly created to
   *         hold the hierarchy.
   */
  public WindowHierarchyElement getActiveWindow() {
    return activeWindow;
  }

  /**
   * @param id The identifier for the desired {@link WindowHierarchyElement}, as returned by
   *        {@link WindowHierarchyElement#getId()}.
   * @return The {@link WindowHierarchyElement} identified by {@code id} in this hierarchy
   * @throws NoSuchElementException if no window within this hierarchy matches the provided
   *         {@code id}
   */
  public WindowHierarchyElement getWindowById(int id) {
    if ((id < 0) || (id >= windowHierarchyElements.size())) {
      throw new NoSuchElementException();
    }
    return windowHierarchyElements.get(id);
  }

  /**
   * @param condensedUniqueId The identifier for the desired {@link ViewHierarchyElement}, as
   *        returned by {@link ViewHierarchyElement#getCondensedUniqueId()}
   * @return The {@link ViewHierarchyElement} identified by {@code id} in this hierarchy
   * @throws NoSuchElementException if no view within this hierarchy matches the provided
   *         {@code condensedUniqueId}
   */
  public ViewHierarchyElement getViewById(long condensedUniqueId) {
    int windowId = (int) (condensedUniqueId >>> 32);
    int viewId = (int) condensedUniqueId;
    return getWindowById(windowId).getViewById(viewId);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  /**
   * @return an {@link AccessibilityHierarchyProto} protocol buffer representation of this hierarchy
   */
  public AccessibilityHierarchyProto toProto() {
    AccessibilityHierarchyProto.Builder builder = AccessibilityHierarchyProto.newBuilder();
    builder.setDeviceState(deviceState.toProto());
    builder.setActiveWindowId(activeWindow.getId());
    for (WindowHierarchyElement window : windowHierarchyElements) {
      builder.addWindows(window.toProto());
    }

    return builder.build();
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    deviceState.writeToParcel(out, flags);
    List<WindowHierarchyElement> rootWindows = new ArrayList<>();
    for (WindowHierarchyElement window : windowHierarchyElements) {
      if (window.getParentWindow() == null) {
        rootWindows.add(window);
      }
    }

    // Consistency check - the total number of windows in the hierarchy
    out.writeInt(windowHierarchyElements.size());

    // The number of root windows present
    out.writeInt(rootWindows.size());

    // Traverse each window structure and read its content depth-first
    for (WindowHierarchyElement window : rootWindows) {
      writeWindowHierarchyToParcel(window, out);
    }
  }

  private static void writeWindowHierarchyToParcel(WindowHierarchyElement element, Parcel out) {
    element.writeToParcel(out);
    int childCount = element.getChildWindowCount();
    out.writeInt(childCount);
    for (int i = 0; i < childCount; ++i) {
      writeWindowHierarchyToParcel(element.getChildWindow(i), out);
    }
  }

  /**
   * @param in non-null {@link Parcel} written by {@link #writeToParcel(Parcel, int)} from which to
   *        create a hierarchy.
   */
  private static AccessibilityHierarchy readFromParcel(Parcel in) {
    DeviceState deviceState = new DeviceState(in);
    int totalWindows = in.readInt();
    int rootWindows = in.readInt();

    List<WindowHierarchyElement> windowHierarchyElements = new ArrayList<>(totalWindows);
    for (int i = 0; i < rootWindows; ++i) {
      buildWindowHierarchy(in, windowHierarchyElements, null);
    }

    checkState(
        windowHierarchyElements.size() == totalWindows,
        "Window hierarchy failed consistency check.");

    // Identify the active window - there should be exactly one.
    WindowHierarchyElement activeWindow = null;
    for (WindowHierarchyElement window : windowHierarchyElements) {
      if (Boolean.TRUE.equals(window.isActive())) {
        checkState(activeWindow == null, "More than one active window detected.");
        activeWindow = window;
      }
    }
    checkNotNull(activeWindow, "No active windows detected.");

    AccessibilityHierarchy hierarchy =
        new AccessibilityHierarchy(deviceState, windowHierarchyElements, activeWindow);
    hierarchy.setAccessibilityHierarchy();
    return hierarchy;
  }

  /**
   * Create a new {@link WindowHierarchyElement} from an AccessibilityWindowInfo and append it and
   * its child windows to the list of windows within this hierarchy. Its {@code id} will match its
   * position in this list. This also adds the newly created elements as children to the provided
   * {@code parent} element.
   *
   * @param info The {@link AccessibilityWindowInfo} from which to create the element
   * @param elementList The list to hold the element
   * @param parent The {@link WindowHierarchyElement} corresponding to the info's parent, or
   *        {@code null} if {@code info} is a root window
   * @param originMap A map to populate with the condensed unique IDs of the
   *        {@link ViewHierarchyElement}s created during construction of the hierarchy mapped to
   *        their originating {@link AccessibilityNodeInfo}s, or {@code null} if no such map should
   *        be populated
   * @return The newly created element
   */
  private static WindowHierarchyElement buildWindowHierarchy(
      AccessibilityWindowInfo info,
      List<WindowHierarchyElement> elementList,
      @Nullable WindowHierarchyElement parent,
      BiMap<Long, AccessibilityNodeInfo> originMap) {
    WindowHierarchyElement element =
        WindowHierarchyElement.newBuilder(elementList.size(), info)
            .setParent(parent)
            .setNodeInfoOriginMap(originMap)
            .build();
    elementList.add(element);

    for (int i = 0; i < info.getChildCount(); ++i) {
      AccessibilityWindowInfo child = info.getChild(i);
      if (child != null) {
        element.addChild(buildWindowHierarchy(child, elementList, element, originMap));
        child.recycle();
      }
    }

    return element;
  }

  private static WindowHierarchyElement buildWindowHierarchy(
      Parcel fromParcel,
      List<WindowHierarchyElement> elementList,
      @Nullable WindowHierarchyElement parent) {
    WindowHierarchyElement element =
        WindowHierarchyElement.newBuilder(elementList.size(), fromParcel).setParent(parent).build();
    elementList.add(element);

    int childWindowCount = fromParcel.readInt();
    for (int i = 0; i < childWindowCount; ++i) {
      element.addChild(buildWindowHierarchy(fromParcel, elementList, element));
    }

    return element;
  }

  /**
   * Associates {@link ViewHierarchyElement}s based on labeling ({@code android:labelFor})
   * relationships
   *
   * @param originMap A map from condensed unique IDs to the originating {@link
   *     AccessibilityNodeInfo} structures from which a {@link ViewHierarchyElement} was
   *     constructed, as populated by {@link AccessibilityHierarchy} constructors.
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  private void resolveLabelForRelationshipsAmongInfos(
      BiMap<Long, AccessibilityNodeInfo> originMap) {
    for (Entry<Long, AccessibilityNodeInfo> entry : originMap.entrySet()) {
      AccessibilityNodeInfo labeledNode = entry.getValue().getLabelFor();
      if (labeledNode != null) {
        Long labeledElementId = originMap.inverse().get(labeledNode);
        if (labeledElementId != null) {
          ViewHierarchyElement labelElement = getViewById(entry.getKey());
          ViewHierarchyElement labeledElement = getViewById(labeledElementId);
          labeledElement.setLabeledBy(labelElement);
        }
      }
    }
  }

  /**
   * For every View in {@code originMap} that has a labelFor value, set labeledBy on the
   * ViewHierarchyElement that represents the View with the referenced View ID.
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  private void resolveLabelForRelationshipsAmongViews(
      Context context, BiMap<Long, View> originMap) {
    for (Entry<Long, View> entry : originMap.entrySet()) {
      int labeledViewId = entry.getValue().getLabelFor();
      if (labeledViewId != View.NO_ID) {
        ViewHierarchyElement labelElement = getViewById(entry.getKey());
        ViewHierarchyElement labeledElement = findElementByViewId(labeledViewId, originMap);
        if (labeledElement == null) {
          LogUtils.w(
              this, "View not found for labelFor = %1$s", resourceName(context, labeledViewId));
        } else {
          labeledElement.setLabeledBy(labelElement);
        }
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
  private void resolveAccessibilityTraversalRelationshipsAmongInfos(
      BiMap<Long, AccessibilityNodeInfo> originMap) {
    for (Entry<Long, AccessibilityNodeInfo> entry : originMap.entrySet()) {
      AccessibilityNodeInfo beforeNode = entry.getValue().getTraversalBefore();
      AccessibilityNodeInfo afterNode = entry.getValue().getTraversalAfter();
      if (beforeNode != null || afterNode != null) {
        ViewHierarchyElement currentElement = getViewById(entry.getKey());
        if (beforeNode != null) {
          ViewHierarchyElement beforeElement = findElementByNodeInfo(beforeNode, originMap);
          if (beforeElement == null) {
            LogUtils.w(this, "Element not found for accessibilityTraversalBefore.");
          } else {
            currentElement.setAccessibilityTraversalBefore(beforeElement);
          }
        }
        if (afterNode != null) {
          ViewHierarchyElement afterElement = findElementByNodeInfo(afterNode, originMap);
          if (afterElement == null) {
            LogUtils.w(this, "Element not found for accessibilityTraversalAfter.");
          } else {
            currentElement.setAccessibilityTraversalAfter(afterElement);
          }
        }
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
  private void resolveAccessibilityTraversalRelationshipsAmongViews(
      Context context, BiMap<Long, View> originMap) {
    for (Entry<Long, View> entry : originMap.entrySet()) {
      int beforeViewId = entry.getValue().getAccessibilityTraversalBefore();
      int afterViewId = entry.getValue().getAccessibilityTraversalAfter();
      ViewHierarchyElement viewElement = getViewById(entry.getKey());
      if (beforeViewId != View.NO_ID) {
        ViewHierarchyElement matchingElement = findElementByViewId(beforeViewId, originMap);
        if (matchingElement == null) {
          LogUtils.w(
              this,
              "View not found for accessibilityTraversalBefore = %1$s",
              resourceName(context, beforeViewId));
        } else {
          viewElement.setAccessibilityTraversalBefore(matchingElement);
        }
      }
      if (afterViewId != View.NO_ID) {
        ViewHierarchyElement matchingElement = findElementByViewId(afterViewId, originMap);
        if (matchingElement == null) {
          LogUtils.w(
              this,
              "View not found for accessibilityTraversalAfter = %1$s",
              resourceName(context, afterViewId));
        } else {
          viewElement.setAccessibilityTraversalAfter(matchingElement);
        }
      }
    }
  }

  /**
   * Find the element with the condensed unique ID corresponding to the {@code targetNode} in the
   * value of {@code originMap}, or {@code null} if no element is found with that ID.
   *
   * @param originMap A map from condensed unique IDs to the originating {@link
   *     AccessibilityNodeInfo} structures from which a {@link ViewHierarchyElement} was
   *     constructed, as populated by {@link AccessibilityHierarchy} constructors.
   */
  private @Nullable ViewHierarchyElement findElementByNodeInfo(
      AccessibilityNodeInfo targetNode, BiMap<Long, AccessibilityNodeInfo> originMap) {
    Long viewId = originMap.inverse().get(targetNode);
    if (viewId == null) {
      return null;
    }
    return getViewById(viewId);
  }

  /**
   * Find the element corresponding to the View in value of {@code originMap} whose ID is {@code
   * targetViewId}, or {@code null} if no view is found with that ID.
   */
  private @Nullable ViewHierarchyElement findElementByViewId(
      int targetViewId, BiMap<Long, View> originMap) {
    for (Entry<Long, View> matchingEntry : originMap.entrySet()) {
      if (matchingEntry.getValue().getId() == targetViewId) {
        return getViewById(matchingEntry.getKey());
      }
    }
    return null;
  }

  /**
   * @return the name of the resource corresponding to the referenced view. Or, if the name cannot
   *     be found, return the string representation of the view id.
   */
  private static String resourceName(Context context, int viewId) {
    String resourceName = context.getResources().getResourceEntryName(viewId);
    if (resourceName != null) {
      return "\"" + resourceName + "\"";
    }
    return Integer.toString(viewId);
  }

  /** Set backpointers from the windows to the accessibility hierarchy. */
  private void setAccessibilityHierarchy() {
    for (WindowHierarchyElement window : this.windowHierarchyElements) {
      window.setAccessibilityHierarchy(this);
    }
  }

  public static final Parcelable.Creator<AccessibilityHierarchy> CREATOR =
      new Parcelable.Creator<AccessibilityHierarchy>() {
        @Override
        public AccessibilityHierarchy createFromParcel(Parcel in) {
          return readFromParcel(checkNotNull(in));
        }

        @Override
        public AccessibilityHierarchy[] newArray(int size) {
          return new AccessibilityHierarchy[size];
        }
      };

  /**
   * Returns a new builder that can build an AccessibilityHierarchy from a View.
   * <p>NOTE: When using this builder, a default {@link WindowHierarchyElement} representing the
   * active window is created implicitly to hold the view hierarchy rooted at {@code fromRootView}.
   *
   * @param view the root view of the hierarchy from which to build a snapshot of the UI's state
   */
  public static Builder newBuilder(View view) {
    Builder builder = new Builder();
    builder.fromRootView = checkNotNull(view);
    return builder;
  }

  /**
   * Returns a new builder that can build an AccessibilityHierarchy from window info.
   *
   * @param fromWindowList A {@link List} of {@link AccessibilityWindowInfo}s representing the UI's
   *     state. Most commonly, this list will be provided by {@link
   *     AccessibilityService#getWindows()} or {@link UiAutomation#getWindows()}.
   * @param context The {@link Context} from which certain aspects of a device's state will be
   *     captured
   */
  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  public static Builder newBuilder(List<AccessibilityWindowInfo> fromWindowList, Context context) {
    Builder builder = new Builder();
    builder.fromWindowList = checkNotNull(fromWindowList);
    builder.context = checkNotNull(context);
    return builder;
  }

  /**
   * Returns a new builder that can build an AccessibilityHierarchy from AccessibilityNodeInfo.
   * <p>NOTE: When using this builder, a default {@link WindowHierarchyElement} representing the
   * active window is created implicitly to hold the view hierarchy rooted at {@code fromRootNode}.
   *
   * @param fromRootNode An {@link AccessibilityNodeInfo} representing the root view of the
   *     hierarchy from which to build a snapshot of the UI's state
   * @param context The {@link Context} from which certain aspects of a device's state will be
   *     captured
   */
  public static Builder newBuilder(AccessibilityNodeInfo fromRootNode, Context context) {
    Builder builder = new Builder();
    builder.fromRootNode = checkNotNull(fromRootNode);
    builder.context = checkNotNull(context);
    return builder;
  }

  /**
   * Returns a new builder that can build an AccessibilityHierarchy from a proto.
   *
   * @param proto A protocol buffer representation of a hierarchy
   */
  public static Builder newBuilder(AccessibilityHierarchyProto proto) {
    Builder builder = new Builder();
    builder.proto = checkNotNull(proto);
    return builder;
  }

  /**
   * A builder for {@link AccessibilityHierarchy}; obtained using {@link
   * AccessibilityHierarchy#builder}.
   */
  public static class Builder {
    private @Nullable View fromRootView;
    private @Nullable List<AccessibilityWindowInfo> fromWindowList;
    private @Nullable AccessibilityNodeInfo fromRootNode;
    private @Nullable Context context;
    private @Nullable AccessibilityHierarchyProto proto;
    private @Nullable BiMap<Long, AccessibilityNodeInfo> nodeInfoOriginMap;
    private @Nullable BiMap<Long, View> viewOriginMap;
    private boolean disposeInstances = false;

    /**
     * Set a map to populate with the condensed unique IDs of the {@link ViewHierarchyElement}s
     * created during construction of the hierarchy mapped to their originating {@link
     * AccessibilityNodeInfo}s. This is optional, and is only used when building a hierarchy from
     * an {@code AccessibilityNodeInfo} or a {@code List&lt;AccessibilityWindowInfo&gt;}.
     */
    public Builder setNodeInfoOriginMap(BiMap<Long, AccessibilityNodeInfo> originMap) {
      this.nodeInfoOriginMap = originMap;
      return this;
    }

    /**
     * Set a map to populate with the condensed unique IDs of the {@link ViewHierarchyElement}s
     * created during construction of the hierarchy mapped to their originating {@link View}s.
     * This is optional, and is only used when building a hierarchy from a {@code View}.
     */
    public Builder setViewOriginMap(BiMap<Long, View> originMap) {
      this.viewOriginMap = originMap;
      return this;
    }

    public AccessibilityHierarchy build() {
      AccessibilityHierarchy result;
      if (fromRootView != null) {
        result = buildHierarchyFromView(fromRootView);
      } else if ((fromRootNode != null) && (context != null)) {
        result = buildHierarchyFromNodeInfo(fromRootNode, context);
      } else if ((fromWindowList != null) && (context != null)) {
        result = buildHierarchyFromWindowList(fromWindowList, context);
      } else if (proto != null) {
        result = buildHierarchyFromProto(proto);
      } else {
        throw new IllegalStateException("Nothing from which to build");
      }
      disposeOfMaps();
      return result;
    }

    private AccessibilityHierarchy buildHierarchyFromView(View fromRootView) {
      if (this.viewOriginMap == null) {
        // If we're not provided with a map to populate with originating structures, create one to
        // be used internally during hierarchy constructions
        this.disposeInstances = true;
        this.viewOriginMap = HashBiMap.<Long, View>create();
      }

      WindowHierarchyElement activeWindow =
          WindowHierarchyElement
              .newBuilder(0, fromRootView)
              .setViewOriginMap(checkNotNull(viewOriginMap))
              .build();
      List<WindowHierarchyElement> windowHierarchyElements =
          Lists.<WindowHierarchyElement>newArrayList(activeWindow);
      Context context = fromRootView.getContext();
      DeviceState deviceState = new DeviceState(context);
      AccessibilityHierarchy hierarchy =
          new AccessibilityHierarchy(deviceState, windowHierarchyElements, activeWindow);
      hierarchy.setAccessibilityHierarchy();

      // Resolve inter-node relationships once the hierarchy is constructed
      if (AT_17) {
        hierarchy.resolveLabelForRelationshipsAmongViews(context, checkNotNull(viewOriginMap));
      }
      if (AT_22) {
        hierarchy.resolveAccessibilityTraversalRelationshipsAmongViews(
            context, checkNotNull(viewOriginMap));
      }
      return hierarchy;
    }

    private AccessibilityHierarchy buildHierarchyFromNodeInfo(
        AccessibilityNodeInfo fromRootNode, Context context) {
      if (nodeInfoOriginMap == null) {
        // If we're not provided with a map to populate with originating structures, create one to
        // be used internally during hierarchy constructions
        disposeInstances = true;
        nodeInfoOriginMap = HashBiMap.<Long, AccessibilityNodeInfo>create();
      }

      WindowHierarchyElement activeWindow =
          WindowHierarchyElement
              .newBuilder(0, fromRootNode)
              .setNodeInfoOriginMap(checkNotNull(nodeInfoOriginMap))
              .build();
      List<WindowHierarchyElement> windowHierarchyElements =
          Lists.<WindowHierarchyElement>newArrayList(activeWindow);
      DeviceState deviceState = new DeviceState(context);
      AccessibilityHierarchy hierarchy =
          new AccessibilityHierarchy(deviceState, windowHierarchyElements, activeWindow);
      hierarchy.setAccessibilityHierarchy();

      // Resolve inter-node relationships once the hierarchy is constructed
      if (AT_17) {
        hierarchy.resolveLabelForRelationshipsAmongInfos(checkNotNull(nodeInfoOriginMap));
      }
      if (AT_22) {
        hierarchy.resolveAccessibilityTraversalRelationshipsAmongInfos(
            checkNotNull(nodeInfoOriginMap));
      }
      return hierarchy;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private AccessibilityHierarchy buildHierarchyFromWindowList(
        List<AccessibilityWindowInfo> fromWindowList, Context context) {
      if (nodeInfoOriginMap == null) {
        // If we're not provided with a map to populate with originating structures, create one to
        // be used internally during hierarchy constructions
        disposeInstances = true;
        nodeInfoOriginMap = HashBiMap.<Long, AccessibilityNodeInfo>create();
      }

      List<WindowHierarchyElement> windowHierarchyElements = new ArrayList<>(fromWindowList.size());
      for (AccessibilityWindowInfo window : fromWindowList) {
        if (window.getParent() == null) {
          /*
           * The window list as provided by AccessibilityService#getWindows() gives all windows, not
           * only root windows, so we'll only evaluate those without parents, and let the recursive
           * builder create the window subtrees. NOTE: This breaks the AccessibilityService contract
           * that the window list will retain order based on layer. This is acceptable as this
           * structure provides no such guarantee.
           */
          buildWindowHierarchy(
              window, windowHierarchyElements, /* parent= */ null, checkNotNull(nodeInfoOriginMap));
        }
      }

      // Store a reference to the active window - there should be exactly one.
      WindowHierarchyElement activeWindow = null;
      for (WindowHierarchyElement window : windowHierarchyElements) {
        if (Boolean.TRUE.equals(window.isActive())) {
          checkState(activeWindow == null, "More than one active window detected.");
          activeWindow = window;
        }
      }
      checkNotNull(activeWindow, "No active windows detected.");
      DeviceState deviceState = new DeviceState(context);
      AccessibilityHierarchy hierarchy =
          new AccessibilityHierarchy(deviceState, windowHierarchyElements, activeWindow);
      hierarchy.setAccessibilityHierarchy();

      // Resolve inter-node relationships once the hierarchy is constructed
      if (AT_17) {
        hierarchy.resolveLabelForRelationshipsAmongInfos(checkNotNull(nodeInfoOriginMap));
      }
      if (AT_22) {
        hierarchy.resolveAccessibilityTraversalRelationshipsAmongInfos(
            checkNotNull(nodeInfoOriginMap));
      }
      return hierarchy;
    }

    private AccessibilityHierarchy buildHierarchyFromProto(AccessibilityHierarchyProto proto) {
      DeviceState deviceState = new DeviceState(proto.getDeviceState());
      int activeWindowId = proto.getActiveWindowId();

      List<WindowHierarchyElement> windowHierarchyElements =
          new ArrayList<>(proto.getWindowsCount());
      for (WindowHierarchyElementProto windowProto : proto.getWindowsList()) {
        windowHierarchyElements.add(WindowHierarchyElement.newBuilder(windowProto).build());
      }
      checkState(
          !windowHierarchyElements.isEmpty(),
          "Hierarchies must contain at least one window.");
      WindowHierarchyElement activeWindow = windowHierarchyElements.get(activeWindowId);

      AccessibilityHierarchy hierarchy =
          new AccessibilityHierarchy(deviceState, windowHierarchyElements, activeWindow);
      hierarchy.setAccessibilityHierarchy();
      return hierarchy;
    }

    private void disposeOfMaps() {
      if (disposeInstances) {
        if (nodeInfoOriginMap != null) {
          for (AccessibilityNodeInfo nodeInfo : nodeInfoOriginMap.values()) {
            nodeInfo.recycle();
          }
        }
      }
      viewOriginMap = null;
      nodeInfoOriginMap = null;
    }
  }
}

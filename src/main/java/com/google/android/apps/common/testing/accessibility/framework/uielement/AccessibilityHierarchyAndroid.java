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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.accessibilityservice.AccessibilityService;
import android.app.UiAutomation;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Region;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.TouchDelegateInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import com.google.android.apps.common.testing.accessibility.framework.ViewAccessibilityUtils;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.AccessibilityHierarchyProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.ViewElementClassNamesProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.WindowHierarchyElementProto;
import com.google.android.libraries.accessibility.utils.log.LogUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Representation of a UI hierarchy for accessibility checking
 *
 * <p>Such a hierarchy may contain a forest of {@link WindowHierarchyElementAndroid}s, each of which
 * contain a tree of {@link ViewHierarchyElementAndroid}s.
 */
public class AccessibilityHierarchyAndroid extends AccessibilityHierarchy implements Parcelable {

  private static final String TAG = "A11yHierarchyAndroid";

  private static final boolean AT_29 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
  private static final boolean AT_22 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1);
  private static final boolean AT_17 =
      (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1);

  private final DeviceStateAndroid deviceState;

  private final List<WindowHierarchyElementAndroid> windowHierarchyElements;

  /* A reference to the 'active' window. Exactly one such window exists in any hierarchy. */
  private final WindowHierarchyElementAndroid activeWindow;

  private AccessibilityHierarchyAndroid(
      DeviceStateAndroid deviceState,
      List<WindowHierarchyElementAndroid> windowHierarchyElements,
      WindowHierarchyElementAndroid activeWindow,
      ViewElementClassNamesAndroid viewElementClassNames) {
    super(deviceState, windowHierarchyElements, activeWindow, viewElementClassNames);
    this.deviceState = deviceState;
    this.windowHierarchyElements = windowHierarchyElements;
    this.activeWindow = activeWindow;
  }

  /**
   * Returns certain properties of the device at the time an {@link AccessibilityHierarchyAndroid}
   * was originally captured.
   */
  @Override
  public DeviceStateAndroid getDeviceState() {
    return deviceState;
  }

  /**
   * Get all {@link WindowHierarchyElementAndroid}s in this hierarchy.
   *
   * @return An unmodifiable collection of all windows in hierarchy
   */
  @Override
  public Collection<WindowHierarchyElementAndroid> getAllWindows() {
    return Collections.unmodifiableCollection(windowHierarchyElements);
  }

  /**
   * Returns a {@link WindowHierarchyElementAndroid} representing the active window in this
   * hierarchy. If this hierarchy was constructed from a {@link AccessibilityNodeInfo} or {@link
   * View}, this returns the default {@link WindowHierarchyElementAndroid} that was implicitly
   * created to hold the hierarchy.
   */
  @Override
  public WindowHierarchyElementAndroid getActiveWindow() {
    return activeWindow;
  }

  /**
   * @param id The identifier for the desired {@link WindowHierarchyElementAndroid}, as returned by
   *     {@link WindowHierarchyElementAndroid#getId()}.
   * @return The {@link WindowHierarchyElementAndroid} identified by {@code id} in this hierarchy
   * @throws NoSuchElementException if no window within this hierarchy matches the provided {@code
   *     id}
   */
  @Override
  public WindowHierarchyElementAndroid getWindowById(int id) {
    if ((id < 0) || (id >= windowHierarchyElements.size())) {
      throw new NoSuchElementException();
    }
    return windowHierarchyElements.get(id);
  }

  /**
   * @param condensedUniqueId The identifier for the desired {@link ViewHierarchyElementAndroid}, as
   *     returned by {@link ViewHierarchyElementAndroid#getCondensedUniqueId()}
   * @return The {@link ViewHierarchyElementAndroid} identified by {@code id} in this hierarchy
   * @throws NoSuchElementException if no view within this hierarchy matches the provided {@code
   *     condensedUniqueId}
   */
  @Override
  public ViewHierarchyElementAndroid getViewById(long condensedUniqueId) {
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
  @Override
  public AccessibilityHierarchyProto toProto() {
    AccessibilityHierarchyProto.Builder builder = AccessibilityHierarchyProto.newBuilder();
    builder
        .setDeviceState(deviceState.toProto())
        .setActiveWindowId(activeWindow.getId())
        .setViewElementClassNames(viewElementClassNames.toProto());
    for (WindowHierarchyElementAndroid window : windowHierarchyElements) {
      builder.addWindows(window.toProto());
    }

    return builder.build();
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    deviceState.writeToParcel(out, flags);
    List<WindowHierarchyElementAndroid> rootWindows = new ArrayList<>();
    for (WindowHierarchyElementAndroid window : windowHierarchyElements) {
      if (window.getParentWindow() == null) {
        rootWindows.add(window);
      }
    }

    // Consistency check - the total number of windows in the hierarchy
    out.writeInt(windowHierarchyElements.size());

    // The number of root windows present
    out.writeInt(rootWindows.size());

    // Traverse each window structure and read its content depth-first
    for (WindowHierarchyElementAndroid window : rootWindows) {
      writeWindowHierarchyToParcel(window, out);
    }

    writeMapToParcel(viewElementClassNames.getMap(), out);
  }

  private static void writeWindowHierarchyToParcel(
      WindowHierarchyElementAndroid element, Parcel out) {
    element.writeToParcel(out);
    int childCount = element.getChildWindowCount();
    out.writeInt(childCount);
    for (int i = 0; i < childCount; ++i) {
      writeWindowHierarchyToParcel(element.getChildWindow(i), out);
    }
  }

  private static void writeMapToParcel(Map<String, Integer> map, Parcel out) {
    out.writeInt(map.size());
    for (Map.Entry<String, Integer> entry : map.entrySet()) {
      out.writeString(entry.getKey());
      out.writeInt(entry.getValue());
    }
  }

  private static Map<String, Integer> readMapFromParcel(Parcel in) {
    BiMap<String, Integer> viewElementsMap = HashBiMap.<String, Integer>create();
    int mapSize = in.readInt();
    for (int i = 0; i < mapSize; i++) {
      viewElementsMap.put(in.readString(), in.readInt());
    }
    return viewElementsMap;
  }

  /**
   * @param in non-null {@link Parcel} written by {@link #writeToParcel(Parcel, int)} from which to
   *     create a hierarchy.
   */
  private static AccessibilityHierarchyAndroid readFromParcel(Parcel in) {
    DeviceStateAndroid deviceState = DeviceStateAndroid.newBuilder(in).build();
    int totalWindows = in.readInt();
    int rootWindows = in.readInt();

    List<WindowHierarchyElementAndroid> windowHierarchyElements = new ArrayList<>(totalWindows);
    for (int i = 0; i < rootWindows; ++i) {
      buildWindowHierarchy(in, windowHierarchyElements, null);
    }

    checkState(
        windowHierarchyElements.size() == totalWindows,
        "Window hierarchy failed consistency check.");

    // Identify the active window - there should be exactly one.
    WindowHierarchyElementAndroid activeWindow = null;
    for (WindowHierarchyElementAndroid window : windowHierarchyElements) {
      if (Boolean.TRUE.equals(window.isActive())) {
        checkState(activeWindow == null, "More than one active window detected.");
        activeWindow = window;
      }
    }
    checkNotNull(activeWindow, "No active windows detected.");

    ViewElementClassNamesAndroid viewElementClassNames =
        new ViewElementClassNamesAndroid(readMapFromParcel(in));

    AccessibilityHierarchyAndroid hierarchy =
        new AccessibilityHierarchyAndroid(
            deviceState, windowHierarchyElements, activeWindow, viewElementClassNames);
    hierarchy.setAccessibilityHierarchy();
    return hierarchy;
  }

  /**
   * Create a new {@link WindowHierarchyElementAndroid} from an AccessibilityWindowInfo and append
   * it and its child windows to the list of windows within this hierarchy. Its {@code id} will
   * match its position in this list. This also adds the newly created elements as children to the
   * provided {@code parent} element.
   *
   * @param info The {@link AccessibilityWindowInfo} from which to create the element
   * @param elementList The list to hold the element
   * @param parent The {@link WindowHierarchyElementAndroid} corresponding to the info's parent, or
   *     {@code null} if {@code info} is a root window
   * @param originMap A map to populate with the condensed unique IDs of the {@link
   *     ViewHierarchyElementAndroid}s created during construction of the hierarchy mapped to their
   *     originating {@link AccessibilityNodeInfo}s, or {@code null} if no such map should be
   *     populated
   * @param extraDataExtractor The {@link AccessibilityNodeInfoExtraDataExtractor} for extracting
   *     extra rendering data
   * @return The newly created element
   */
  private static WindowHierarchyElementAndroid buildWindowHierarchy(
      AccessibilityWindowInfo info,
      List<WindowHierarchyElementAndroid> elementList,
      @Nullable WindowHierarchyElementAndroid parent,
      BiMap<Long, AccessibilityNodeInfo> originMap,
      AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
    WindowHierarchyElementAndroid element =
        WindowHierarchyElementAndroid.newBuilder(elementList.size(), info, extraDataExtractor)
            .setParent(parent)
            .setNodeInfoOriginMap(originMap)
            .build();
    elementList.add(element);

    for (int i = 0; i < info.getChildCount(); ++i) {
      AccessibilityWindowInfo child = info.getChild(i);
      if (child != null) {
        element.addChild(
            buildWindowHierarchy(child, elementList, element, originMap, extraDataExtractor));
        child.recycle();
      }
    }

    return element;
  }

  private static WindowHierarchyElementAndroid buildWindowHierarchy(
      Parcel fromParcel,
      List<WindowHierarchyElementAndroid> elementList,
      @Nullable WindowHierarchyElementAndroid parent) {
    WindowHierarchyElementAndroid element =
        WindowHierarchyElementAndroid.newBuilder(elementList.size(), fromParcel)
            .setParent(parent)
            .build();
    elementList.add(element);

    int childWindowCount = fromParcel.readInt();
    for (int i = 0; i < childWindowCount; ++i) {
      element.addChild(buildWindowHierarchy(fromParcel, elementList, element));
    }

    return element;
  }

  /**
   * Associates {@link ViewHierarchyElementAndroid}s based on labeling ({@code android:labelFor})
   * relationships
   *
   * @param originMap A map from condensed unique IDs to the originating {@link
   *     AccessibilityNodeInfo} structures from which a {@link ViewHierarchyElementAndroid} was
   *     constructed, as populated by {@link AccessibilityHierarchyAndroid} constructors.
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  private void resolveLabelForRelationshipsAmongInfos(
      BiMap<Long, AccessibilityNodeInfo> originMap) {
    for (Map.Entry<Long, AccessibilityNodeInfo> entry : originMap.entrySet()) {
      AccessibilityNodeInfo labeledNode = entry.getValue().getLabelFor();
      if (labeledNode != null) {
        Long labeledElementId = originMap.inverse().get(labeledNode);
        if (labeledElementId != null) {
          ViewHierarchyElementAndroid labelElement = getViewById(entry.getKey());
          ViewHierarchyElementAndroid labeledElement = getViewById(labeledElementId);
          labeledElement.setLabeledBy(labelElement);
        }
      }
    }
  }

  /**
   * For every View in {@code originMap} that has a labelFor value, set labeledBy on the
   * ViewHierarchyElementAndroid that represents the View with the referenced View ID.
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  private void resolveLabelForRelationshipsAmongViews(
      Context context, BiMap<Long, View> originMap) {
    for (Map.Entry<Long, View> entry : originMap.entrySet()) {
      int labeledViewId = entry.getValue().getLabelFor();
      if (labeledViewId != View.NO_ID) {
        ViewHierarchyElementAndroid labelElement = getViewById(entry.getKey());
        ViewHierarchyElementAndroid labeledElement = findElementByViewId(labeledViewId, originMap);
        if (labeledElement == null) {
          LogUtils.w(
              TAG, "View not found for labelFor = %1$s", resourceName(context, labeledViewId));
        } else {
          labeledElement.setLabeledBy(labelElement);
        }
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
  private void resolveAccessibilityTraversalRelationshipsAmongInfos(
      BiMap<Long, AccessibilityNodeInfo> originMap) {
    for (Map.Entry<Long, AccessibilityNodeInfo> entry : originMap.entrySet()) {
      AccessibilityNodeInfo beforeNode = entry.getValue().getTraversalBefore();
      AccessibilityNodeInfo afterNode = entry.getValue().getTraversalAfter();
      if (beforeNode != null || afterNode != null) {
        ViewHierarchyElementAndroid currentElement = getViewById(entry.getKey());
        if (beforeNode != null) {
          ViewHierarchyElementAndroid beforeElement = findElementByNodeInfo(beforeNode, originMap);
          if (beforeElement == null) {
            LogUtils.w(TAG, "Element not found for accessibilityTraversalBefore.");
          } else {
            currentElement.setAccessibilityTraversalBefore(beforeElement);
          }
        }
        if (afterNode != null) {
          ViewHierarchyElementAndroid afterElement = findElementByNodeInfo(afterNode, originMap);
          if (afterElement == null) {
            LogUtils.w(TAG, "Element not found for accessibilityTraversalAfter.");
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
    for (Map.Entry<Long, View> entry : originMap.entrySet()) {
      int beforeViewId = entry.getValue().getAccessibilityTraversalBefore();
      int afterViewId = entry.getValue().getAccessibilityTraversalAfter();
      ViewHierarchyElementAndroid viewElement = getViewById(entry.getKey());
      if (beforeViewId != View.NO_ID) {
        ViewHierarchyElementAndroid matchingElement = findElementByViewId(beforeViewId, originMap);
        if (matchingElement == null) {
          LogUtils.w(
              TAG,
              "View not found for accessibilityTraversalBefore = %1$s",
              resourceName(context, beforeViewId));
        } else {
          viewElement.setAccessibilityTraversalBefore(matchingElement);
        }
      }
      if (afterViewId != View.NO_ID) {
        ViewHierarchyElementAndroid matchingElement = findElementByViewId(afterViewId, originMap);
        if (matchingElement == null) {
          LogUtils.w(
              TAG,
              "View not found for accessibilityTraversalAfter = %1$s",
              resourceName(context, afterViewId));
        } else {
          viewElement.setAccessibilityTraversalAfter(matchingElement);
        }
      }
    }
  }

  /**
   * For every TouchDelegate with a rectangular hit region, record the region on the delegatee's
   * ViewHierarchyElement using {@link ViewHierarchyElement#addTouchDelegateBounds(Rect)}
   */
  @RequiresApi(Build.VERSION_CODES.Q)
  private void resolveTouchDelegateRelationshipsAmongInfos(
      BiMap<Long, AccessibilityNodeInfo> originMap) {
    for (Map.Entry<Long, AccessibilityNodeInfo> entry : originMap.entrySet()) {
      AccessibilityNodeInfo nodeInfo = entry.getValue();
      TouchDelegateInfo delegateInfo = nodeInfo.getTouchDelegateInfo();
      if (delegateInfo != null) {
        android.graphics.Rect boundsInScreen = new android.graphics.Rect();
        nodeInfo.getBoundsInScreen(boundsInScreen);
        for (int i = 0; i < delegateInfo.getRegionCount(); ++i) {
          Region hitRegion = delegateInfo.getRegionAt(i);
          if ((hitRegion != null) && hitRegion.isRect()) {
            AccessibilityNodeInfo delegateeNode = delegateInfo.getTargetForRegion(hitRegion);
            ViewHierarchyElement delegatee =
                (delegateeNode != null) ? findElementByNodeInfo(delegateeNode, originMap) : null;
            if (delegatee != null) {
              // The region is in view coordinates
              android.graphics.Rect hitRect = hitRegion.getBounds();
              delegatee.addTouchDelegateBounds(
                  new Rect(
                      hitRect.left + boundsInScreen.left,
                      hitRect.top + boundsInScreen.top,
                      hitRect.right + boundsInScreen.left,
                      hitRect.bottom + boundsInScreen.top));
            }
          }
        }
      }
    }
  }

  /**
   * Find the element with the condensed unique ID corresponding to the {@code targetNode} in the
   * value of {@code originMap}, or {@code null} if no element is found with that ID.
   *
   * @param originMap A map from condensed unique IDs to the originating {@link
   *     AccessibilityNodeInfo} structures from which a {@link ViewHierarchyElementAndroid} was
   *     constructed, as populated by {@link AccessibilityHierarchyAndroid} constructors.
   */
  private @Nullable ViewHierarchyElementAndroid findElementByNodeInfo(
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
  private @Nullable ViewHierarchyElementAndroid findElementByViewId(
      int targetViewId, BiMap<Long, View> originMap) {
    for (Map.Entry<Long, View> matchingEntry : originMap.entrySet()) {
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
    if (!ViewAccessibilityUtils.isViewIdGenerated(viewId)) {
      try {
        String resourceName = context.getResources().getResourceEntryName(viewId);
        if (resourceName != null) {
          return "\"" + resourceName + "\"";
        }
      } catch (Resources.NotFoundException e) {
        // Fall through
      }
    }
    return Integer.toString(viewId);
  }

  /** Set backpointers from the windows to the accessibility hierarchy. */
  private void setAccessibilityHierarchy() {
    for (WindowHierarchyElementAndroid window : this.windowHierarchyElements) {
      window.setAccessibilityHierarchy(this);
    }
  }

  public static final Parcelable.Creator<AccessibilityHierarchyAndroid> CREATOR =
      new Parcelable.Creator<AccessibilityHierarchyAndroid>() {
        @Override
        public AccessibilityHierarchyAndroid createFromParcel(Parcel in) {
          return readFromParcel(checkNotNull(in));
        }

        @Override
        public AccessibilityHierarchyAndroid[] newArray(int size) {
          return new AccessibilityHierarchyAndroid[size];
        }
      };

  /**
   * Returns a new builder that can build an AccessibilityHierarchy from a View.
   *
   * <p>NOTE: When using this builder, a default {@link WindowHierarchyElementAndroid} representing
   * the active window is created implicitly to hold the view hierarchy rooted at {@code
   * fromRootView}.
   *
   * @param view the root view of the hierarchy from which to build a snapshot of the UI's state
   */
  public static BuilderAndroid newBuilder(View view) {
    BuilderAndroid builder = new BuilderAndroid();
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
  public static BuilderAndroid newBuilder(
      List<AccessibilityWindowInfo> fromWindowList, Context context) {
    BuilderAndroid builder = new BuilderAndroid();
    builder.fromWindowList = checkNotNull(fromWindowList);
    builder.context = checkNotNull(context);
    return builder;
  }

  /**
   * Returns a new builder that can build an AccessibilityHierarchy from AccessibilityNodeInfo.
   *
   * <p>NOTE: When using this builder, a default {@link WindowHierarchyElementAndroid} representing
   * the active window is created implicitly to hold the view hierarchy rooted at {@code
   * fromRootNode}.
   *
   * @param fromRootNode An {@link AccessibilityNodeInfo} representing the root view of the
   *     hierarchy from which to build a snapshot of the UI's state
   * @param context The {@link Context} from which certain aspects of a device's state will be
   *     captured
   */
  public static BuilderAndroid newBuilder(AccessibilityNodeInfo fromRootNode, Context context) {
    BuilderAndroid builder = new BuilderAndroid();
    builder.fromRootNode = checkNotNull(fromRootNode);
    builder.context = checkNotNull(context);
    return builder;
  }

  /**
   * Returns a new builder that can build an AccessibilityHierarchy from a proto.
   *
   * @param proto A protocol buffer representation of a hierarchy
   */
  public static BuilderAndroid newBuilder(AccessibilityHierarchyProto proto) {
    BuilderAndroid builder = new BuilderAndroid();
    builder.proto = checkNotNull(proto);
    return builder;
  }

  /**
   * Maintains a bidirectional mapping between View class names and a unique integer identifier for
   * each of those classes.
   */
  protected static class ViewElementClassNamesAndroid extends ViewElementClassNames {

    /**
     * Populate bimap to contain all unique view element class names of a given {@link
     * windowHierarchyElements}.
     */
    public ViewElementClassNamesAndroid(
        List<WindowHierarchyElementAndroid> windowHierarchyElements,
        CustomViewBuilderAndroid customViewBuilder) {
      super(createClassIdMap(windowHierarchyElements, customViewBuilder));
    }

    /** Populate bimap from a given map. */
    public ViewElementClassNamesAndroid(Map<String, Integer> viewElementsMap) {
      super(viewElementsMap);
    }

    /** Populate bimap from a given proto. */
    public ViewElementClassNamesAndroid(ViewElementClassNamesProto proto) {
      super(proto.getClassNameMap());
    }

    private static @Nullable Class<?> getClassByName(
        ViewHierarchyElementAndroid view,
        String className,
        CustomViewBuilderAndroid customViewBuilder) {
      return customViewBuilder.getClassByName(view, className);
    }

    private static Map<String, Integer> createClassIdMap(
        List<WindowHierarchyElementAndroid> windowHierarchyElements,
        CustomViewBuilderAndroid customViewBuilder) {
      Map<String, Integer> classIdMap = new HashMap<>();
      for (WindowHierarchyElementAndroid window : windowHierarchyElements) {
        for (ViewHierarchyElementAndroid view : window.getAllViews()) {
          Set<String> classReferenceSet = getSuperclassSet(view, customViewBuilder);

          for (String className : classReferenceSet) {
            Integer classNameId = classIdMap.get(className);
            if (classNameId == null) {
              classNameId = classIdMap.size();
              classIdMap.put(className, classNameId);
            }
            view.addIdToSuperclassViewList(classNameId);
          }
        }
      }
      return classIdMap;
    }

    /**
     * Returns all the superclasses of view element represented by {@link
     * ViewHierarchyElementAndroid}.The result will contain all of the superclasses of view's class
     * name and accessibility class name, if any.
     */
    private static ImmutableSet<String> getSuperclassSet(
        ViewHierarchyElementAndroid view, CustomViewBuilderAndroid customViewBuilder) {
      CharSequence className = view.getClassName();
      CharSequence accessibilityClassName = view.getAccessibilityClassName();
      if ((className == null) && (accessibilityClassName == null)) {
        return ImmutableSet.of();
      }

      if ((accessibilityClassName == null) || TextUtils.equals(className, accessibilityClassName)) {
        return getSuperclassSet(view, className, customViewBuilder);
      } else {
        return new ImmutableSet.Builder<String>()
            .addAll(getSuperclassSet(view, accessibilityClassName, customViewBuilder))
            .addAll(getSuperclassSet(view, className, customViewBuilder))
            .build();
      }
    }

    private static ImmutableSet<String> getSuperclassSet(
        ViewHierarchyElementAndroid view,
        CharSequence className,
        CustomViewBuilderAndroid customViewBuilder) {
      ImmutableSet.Builder<String> builder = new ImmutableSet.Builder<>();
      Class<?> viewClass = getClassByName(view, className.toString(), customViewBuilder);
      while ((viewClass != null) && !viewClass.equals(Object.class)) {
        builder.add(viewClass.getName());
        addAllSuperinterfacesRecursivelyToSetBuilder(viewClass, builder);
        viewClass = viewClass.getSuperclass();
      }
      return builder.build();
    }

    private static void addAllSuperinterfacesRecursivelyToSetBuilder(
        Class<?> viewClass, ImmutableSet.Builder<String> builder) {
      Class<?>[] interfaces = viewClass.getInterfaces();
      for (Class<?> element : interfaces) {
        builder.add(element.getName());
        addAllSuperinterfacesRecursivelyToSetBuilder(element, builder);
      }
    }
  }

  /**
   * A builder for {@link AccessibilityHierarchyAndroid}; obtained using {@link
   * AccessibilityHierarchyAndroid#builder}.
   */
  public static class BuilderAndroid extends Builder {
    private static final CustomViewBuilderAndroid DEFAULT_CUSTOM_VIEW_BUILDER =
        new DefaultCustomViewBuilderAndroid();

    private @Nullable View fromRootView;
    private CustomViewBuilderAndroid customViewBuilder = DEFAULT_CUSTOM_VIEW_BUILDER;

    private @Nullable List<AccessibilityWindowInfo> fromWindowList;
    private @Nullable AccessibilityNodeInfo fromRootNode;
    private @Nullable Context context;
    private @Nullable BiMap<Long, AccessibilityNodeInfo> nodeInfoOriginMap;
    private @Nullable BiMap<Long, View> viewOriginMap;
    private boolean disposeInstances = false;
    private boolean obtainCharacterLocations = false;
    private boolean obtainRenderingInfo = true;

    /** The maximum allowed length of the requested text location data. */
    private @Nullable Integer characterLocationArgMaxLength;

    /**
     * Indicates whether text character locations should be requested.
     *
     * @param obtainCharacterLocations The value to which the flag should be set.
     * @return this
     */
    @CanIgnoreReturnValue
    public BuilderAndroid setObtainCharacterLocations(boolean obtainCharacterLocations) {
      this.obtainCharacterLocations = obtainCharacterLocations;
      return this;
    }

    /**
     * Sets the maximum allowed length of the requested text location data.
     *
     * @param characterLocationArgMaxLength The maximum allowed length of the requested text
     *     location data. Must be positive.
     * @throws IllegalArgumentException If the given value is not positive.
     * @return this
     */
    @CanIgnoreReturnValue
    public BuilderAndroid setCharacterLocationArgMaxLength(int characterLocationArgMaxLength) {
      checkArgument(
          characterLocationArgMaxLength > 0, "characterLocationArgMaxLength must be positive.");
      this.characterLocationArgMaxLength = characterLocationArgMaxLength;
      return this;
    }

    /**
     * Set a {@link CustomViewBuilderAndroid} which customizes how to build a {@link
     * AccessibilityHierarchyAndroid} from a View.
     */
    @CanIgnoreReturnValue
    public BuilderAndroid setCustomViewBuilder(CustomViewBuilderAndroid customViewBuilder) {
      this.customViewBuilder = customViewBuilder;
      return this;
    }

    /**
     * Indicates whether rendering info should be requested. This is enabled by default.
     *
     * @param enabled whether rendering info should be requested.
     * @return this
     */
    @CanIgnoreReturnValue
    public BuilderAndroid setObtainRenderingInfo(boolean enabled) {
      this.obtainRenderingInfo = enabled;
      return this;
    }

    /**
     * Set a map to populate with the condensed unique IDs of the {@link
     * ViewHierarchyElementAndroid}s created during construction of the hierarchy mapped to their
     * originating {@link AccessibilityNodeInfo}s. This is optional, and is only used when building
     * a hierarchy from an {@code AccessibilityNodeInfo} or a {@code List<AccessibilityWindowInfo>}.
     */
    @CanIgnoreReturnValue
    public BuilderAndroid setNodeInfoOriginMap(BiMap<Long, AccessibilityNodeInfo> originMap) {
      this.nodeInfoOriginMap = originMap;
      return this;
    }

    /**
     * Set a map to populate with the condensed unique IDs of the {@link
     * ViewHierarchyElementAndroid}s created during construction of the hierarchy mapped to their
     * originating {@link View}s. This is optional, and is only used when building a hierarchy from
     * a {@code View}.
     */
    @CanIgnoreReturnValue
    public BuilderAndroid setViewOriginMap(BiMap<Long, View> originMap) {
      this.viewOriginMap = originMap;
      return this;
    }

    @Override
    public AccessibilityHierarchyAndroid build() {
      AccessibilityHierarchyAndroid result;
      if (fromRootView != null) {
        result = buildHierarchyFromView(fromRootView, getAccessibilityNodeInfoExtraDataExtractor());
      } else if ((fromRootNode != null) && (context != null)) {
        result =
            buildHierarchyFromNodeInfo(
                fromRootNode, context, getAccessibilityNodeInfoExtraDataExtractor());
      } else if ((fromWindowList != null) && (context != null)) {
        result =
            buildHierarchyFromWindowList(
                fromWindowList, context, getAccessibilityNodeInfoExtraDataExtractor());
      } else if (proto != null) {
        result = buildHierarchyFromProto(proto);
      } else {
        throw new IllegalStateException("Nothing from which to build");
      }
      disposeOfMaps();
      return result;
    }

    @VisibleForTesting
    AccessibilityNodeInfoExtraDataExtractor getAccessibilityNodeInfoExtraDataExtractor() {
      return new AccessibilityNodeInfoExtraDataExtractor(
          obtainCharacterLocations, obtainRenderingInfo, characterLocationArgMaxLength);
    }

    private AccessibilityHierarchyAndroid buildHierarchyFromView(
        View fromRootView, AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
      if (this.viewOriginMap == null) {
        // If we're not provided with a map to populate with originating structures, create one to
        // be used internally during hierarchy constructions
        this.disposeInstances = true;
        this.viewOriginMap = HashBiMap.<Long, View>create();
      }

      WindowHierarchyElementAndroid activeWindow =
          WindowHierarchyElementAndroid.newBuilder(
                  0, fromRootView, customViewBuilder, extraDataExtractor)
              .setViewOriginMap(checkNotNull(viewOriginMap))
              .build();
      List<WindowHierarchyElementAndroid> windowHierarchyElements =
          Lists.<WindowHierarchyElementAndroid>newArrayList(activeWindow);
      Context context = fromRootView.getContext();
      DeviceStateAndroid deviceState = DeviceStateAndroid.newBuilder(context).build();
      ViewElementClassNamesAndroid viewElementClassNames =
          new ViewElementClassNamesAndroid(windowHierarchyElements, customViewBuilder);

      AccessibilityHierarchyAndroid hierarchy =
          new AccessibilityHierarchyAndroid(
              deviceState, windowHierarchyElements, activeWindow, viewElementClassNames);
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

    private AccessibilityHierarchyAndroid buildHierarchyFromNodeInfo(
        AccessibilityNodeInfo fromRootNode,
        Context context,
        AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
      if (nodeInfoOriginMap == null) {
        // If we're not provided with a map to populate with originating structures, create one to
        // be used internally during hierarchy constructions
        disposeInstances = true;
        nodeInfoOriginMap = HashBiMap.<Long, AccessibilityNodeInfo>create();
      }

      WindowHierarchyElementAndroid activeWindow =
          WindowHierarchyElementAndroid.newBuilder(0, fromRootNode, extraDataExtractor)
              .setNodeInfoOriginMap(checkNotNull(nodeInfoOriginMap))
              .build();
      List<WindowHierarchyElementAndroid> windowHierarchyElements =
          Lists.<WindowHierarchyElementAndroid>newArrayList(activeWindow);
      DeviceStateAndroid deviceState = DeviceStateAndroid.newBuilder(context).build();
      ViewElementClassNamesAndroid viewElementClassNames =
          new ViewElementClassNamesAndroid(windowHierarchyElements, customViewBuilder);

      AccessibilityHierarchyAndroid hierarchy =
          new AccessibilityHierarchyAndroid(
              deviceState, windowHierarchyElements, activeWindow, viewElementClassNames);
      hierarchy.setAccessibilityHierarchy();

      // Resolve inter-node relationships once the hierarchy is constructed
      if (AT_17) {
        hierarchy.resolveLabelForRelationshipsAmongInfos(checkNotNull(nodeInfoOriginMap));
      }
      if (AT_22) {
        hierarchy.resolveAccessibilityTraversalRelationshipsAmongInfos(
            checkNotNull(nodeInfoOriginMap));
      }
      if (AT_29) {
        hierarchy.resolveTouchDelegateRelationshipsAmongInfos(checkNotNull(nodeInfoOriginMap));
      }
      return hierarchy;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private AccessibilityHierarchyAndroid buildHierarchyFromWindowList(
        List<AccessibilityWindowInfo> fromWindowList,
        Context context,
        AccessibilityNodeInfoExtraDataExtractor extraDataExtractor) {
      if (nodeInfoOriginMap == null) {
        // If we're not provided with a map to populate with originating structures, create one to
        // be used internally during hierarchy constructions
        disposeInstances = true;
        nodeInfoOriginMap = HashBiMap.<Long, AccessibilityNodeInfo>create();
      }

      List<WindowHierarchyElementAndroid> windowHierarchyElements =
          new ArrayList<>(fromWindowList.size());
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
              window,
              windowHierarchyElements,
              /* parent= */ null,
              checkNotNull(nodeInfoOriginMap),
              extraDataExtractor);
        }
      }

      // Store a reference to the active window - there should be exactly one.
      WindowHierarchyElementAndroid activeWindow = null;
      for (WindowHierarchyElementAndroid window : windowHierarchyElements) {
        if (Boolean.TRUE.equals(window.isActive())) {
          checkState(activeWindow == null, "More than one active window detected.");
          activeWindow = window;
        }
      }
      checkNotNull(activeWindow, "No active windows detected.");
      DeviceStateAndroid deviceState = DeviceStateAndroid.newBuilder(context).build();
      ViewElementClassNamesAndroid viewElementClassNames =
          new ViewElementClassNamesAndroid(windowHierarchyElements, customViewBuilder);

      AccessibilityHierarchyAndroid hierarchy =
          new AccessibilityHierarchyAndroid(
              deviceState, windowHierarchyElements, activeWindow, viewElementClassNames);
      hierarchy.setAccessibilityHierarchy();

      // Resolve inter-node relationships once the hierarchy is constructed
      if (AT_17) {
        hierarchy.resolveLabelForRelationshipsAmongInfos(checkNotNull(nodeInfoOriginMap));
      }
      if (AT_22) {
        hierarchy.resolveAccessibilityTraversalRelationshipsAmongInfos(
            checkNotNull(nodeInfoOriginMap));
      }
      if (AT_29) {
        hierarchy.resolveTouchDelegateRelationshipsAmongInfos(checkNotNull(nodeInfoOriginMap));
      }
      return hierarchy;
    }

    private static AccessibilityHierarchyAndroid buildHierarchyFromProto(
        AccessibilityHierarchyProto proto) {
      DeviceStateAndroid deviceState =
          DeviceStateAndroid.newBuilder(proto.getDeviceState()).build();
      int activeWindowId = proto.getActiveWindowId();

      List<WindowHierarchyElementAndroid> windowHierarchyElements =
          new ArrayList<>(proto.getWindowsCount());
      for (WindowHierarchyElementProto windowProto : proto.getWindowsList()) {
        windowHierarchyElements.add(WindowHierarchyElementAndroid.newBuilder(windowProto).build());
      }
      checkState(
          !windowHierarchyElements.isEmpty(), "Hierarchies must contain at least one window.");
      WindowHierarchyElementAndroid activeWindow = windowHierarchyElements.get(activeWindowId);
      ViewElementClassNamesAndroid viewElementClassNames =
          new ViewElementClassNamesAndroid(proto.getViewElementClassNames());

      AccessibilityHierarchyAndroid hierarchy =
          new AccessibilityHierarchyAndroid(
              deviceState, windowHierarchyElements, activeWindow, viewElementClassNames);
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

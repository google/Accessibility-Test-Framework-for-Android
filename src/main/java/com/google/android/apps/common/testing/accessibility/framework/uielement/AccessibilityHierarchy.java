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

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.AccessibilityHierarchyProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.ViewElementClassNamesProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.WindowHierarchyElementProto;
import com.google.common.collect.ImmutableBiMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Representation of a UI hierarchy for accessibility checking
 *
 * <p>Such a hierarchy may contain a forest of {@link WindowHierarchyElement}s, each of which
 * contain a tree of {@link ViewHierarchyElement}s.
 */
public class AccessibilityHierarchy {
  /* A representation of the device's state at the time a hierarchy is initially captured */
  private final DeviceState deviceState;

  /* The id of each window corresponds to its position in this list */
  private final List<? extends WindowHierarchyElement> windowHierarchyElements;

  /* A reference to the 'active' window. Exactly one such window exists in any hierarchy. */
  private final WindowHierarchyElement activeWindow;

  /* A nested class that stores all unique view element class names. */
  protected final ViewElementClassNames viewElementClassNames;

  protected AccessibilityHierarchy(
      DeviceState deviceState,
      List<? extends WindowHierarchyElement> windowHierarchyElements,
      WindowHierarchyElement activeWindow,
      ViewElementClassNames viewElementClassNames) {
    this.deviceState = deviceState;
    this.windowHierarchyElements = windowHierarchyElements;
    this.activeWindow = activeWindow;
    this.viewElementClassNames = viewElementClassNames;
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
  public Collection<? extends WindowHierarchyElement> getAllWindows() {
    return Collections.unmodifiableCollection(windowHierarchyElements);
  }

  /**
   * @return a {@link WindowHierarchyElement} representing the active window in this hierarchy. If
   *     this hierarchy was originally constructed from a {@code AccessibilityNodeInfo} or {@code
   *     View}, this returns the default {@link WindowHierarchyElement} that was implicitly created
   *     to hold the hierarchy.
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

  /**
   * @return an {@link AccessibilityHierarchyProto} protocol buffer representation of this hierarchy
   */
  public AccessibilityHierarchyProto toProto() {
    AccessibilityHierarchyProto.Builder builder = AccessibilityHierarchyProto.newBuilder();
    builder
        .setDeviceState(deviceState.toProto())
        .setActiveWindowId(activeWindow.getId())
        .setViewElementClassNames(viewElementClassNames.toProto());
    for (WindowHierarchyElement window : windowHierarchyElements) {
      builder.addWindows(window.toProto());
    }

    return builder.build();
  }

  /** Set backpointers from the windows to the accessibility hierarchy. */
  private void setAccessibilityHierarchy() {
    for (WindowHierarchyElement window : this.windowHierarchyElements) {
      window.setAccessibilityHierarchy(this);
    }
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

  /** Returns a {@link ViewElementClassNames} that contains view class name and identifiers. */
  ViewElementClassNames getViewElementClassNames() {
    return viewElementClassNames;
  }

  /**
   * Maintains a bidirectional mapping between View class names and a unique integer identifier for
   * each of those classes.
   */
  static class ViewElementClassNames {
    protected ImmutableBiMap<String, Integer> uniqueViewElementsClassNames;

    protected ViewElementClassNames() {}

    /** Populate bimap from a given map. */
    public ViewElementClassNames(Map<String, Integer> viewElementsMap) {
      this.uniqueViewElementsClassNames = ImmutableBiMap.copyOf(viewElementsMap);
    }

    /** Populate bimap from a given proto. */
    public ViewElementClassNames(ViewElementClassNamesProto proto) {
      this.uniqueViewElementsClassNames = ImmutableBiMap.copyOf(proto.getClassNameMap());
    }

    /** Returns an identifier associated with a given class name. Returns null if not found. */
    public @Nullable Integer getIdentifierForClassName(String className) {
      return uniqueViewElementsClassNames.get(className);
    }

    /** Returns a class name associated with a given identifier. Returns null if not found. */
    public @Nullable String getClassNameForIdentifier(int id) {
      return uniqueViewElementsClassNames.inverse().get(id);
    }

    ViewElementClassNamesProto toProto() {
      return ViewElementClassNamesProto.newBuilder()
          .putAllClassName(uniqueViewElementsClassNames)
          .build();
    }

    ImmutableBiMap<String, Integer> getMap() {
      return uniqueViewElementsClassNames;
    }
  }

  /**
   * A builder for {@link AccessibilityHierarchy}; obtained using {@link
   * AccessibilityHierarchy#builder}.
   */
  public static class Builder {
    protected @Nullable AccessibilityHierarchyProto proto;

    public AccessibilityHierarchy build() {
      AccessibilityHierarchy result;

      if (proto != null) {
        result = buildHierarchyFromProto(proto);
      } else {
        throw new IllegalStateException("Nothing from which to build");
      }
      return result;
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
          !windowHierarchyElements.isEmpty(), "Hierarchies must contain at least one window.");
      WindowHierarchyElement activeWindow = windowHierarchyElements.get(activeWindowId);
      ViewElementClassNames viewElementClassNames =
          new ViewElementClassNames(proto.getViewElementClassNames());

      AccessibilityHierarchy hierarchy =
          new AccessibilityHierarchy(
              deviceState, windowHierarchyElements, activeWindow, viewElementClassNames);
      hierarchy.setAccessibilityHierarchy();
      return hierarchy;
    }
  }
}

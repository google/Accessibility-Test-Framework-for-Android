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
import com.google.common.collect.ImmutableList;
import java.util.Collection;
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

  /* The type of data source from which the AccessibilityHierarchy was originally constructed. */
  private final AccessibilityHierarchyOrigin origin;

  /* The id of each window corresponds to its position in this list */
  private final ImmutableList<? extends WindowHierarchyElement> windowHierarchyElements;

  /* A reference to the 'active' window. Exactly one such window exists in any hierarchy. */
  private final WindowHierarchyElement activeWindow;

  /* A nested class that stores all unique view element class names. */
  protected final ViewElementClassNames viewElementClassNames;

  protected AccessibilityHierarchy(
      DeviceState deviceState,
      AccessibilityHierarchyOrigin origin,
      ImmutableList<? extends WindowHierarchyElement> windowHierarchyElements,
      WindowHierarchyElement activeWindow,
      ViewElementClassNames viewElementClassNames) {
    this.deviceState = deviceState;
    this.origin = origin;
    this.windowHierarchyElements = windowHierarchyElements;
    this.activeWindow = activeWindow;
    this.viewElementClassNames = viewElementClassNames;
  }

  /** Returns certain properties of the device at the time the hierarchy was originally captured. */
  public DeviceState getDeviceState() {
    return deviceState;
  }

  /** Returns the type of data source from which the hierarchy was originally constructed. */
  public AccessibilityHierarchyOrigin getOrigin() {
    return origin;
  }

  /**
   * Get all {@link WindowHierarchyElement}s in this hierarchy.
   *
   * @return An unmodifiable collection of all windows in hierarchy
   */
  public Collection<? extends WindowHierarchyElement> getAllWindows() {
    return windowHierarchyElements;
  }

  /**
   * Returns the WindowHierarchyElement representing the active window in this hierarchy. If this
   * hierarchy was originally constructed from an {@code AccessibilityNodeInfo} or {@code View},
   * this returns a WindowHierarchyElement that was implicitly created to hold the hierarchy.
   */
  public WindowHierarchyElement getActiveWindow() {
    return activeWindow;
  }

  /**
   * Returns the WindowHierarchyElement identified by the given ID in this hierarchy.
   *
   * @param id The identifier for the desired {@link WindowHierarchyElement}, as returned by {@link
   *     WindowHierarchyElement#getId()}.
   * @return The {@link WindowHierarchyElement} identified by {@code id} in this hierarchy
   * @throws NoSuchElementException if no window within this hierarchy matches the provided {@code
   *     id}
   */
  public WindowHierarchyElement getWindowById(int id) {
    if ((id < 0) || (id >= windowHierarchyElements.size())) {
      throw new NoSuchElementException();
    }
    return windowHierarchyElements.get(id);
  }

  /**
   * Returns the ViewHierarchyElement identified by the given ID in this hierarchy.
   *
   * @param condensedUniqueId The identifier for the desired {@link ViewHierarchyElement}, as
   *     returned by {@link ViewHierarchyElement#getCondensedUniqueId()}
   * @return The {@link ViewHierarchyElement} identified by {@code id} in this hierarchy
   * @throws NoSuchElementException if no view within this hierarchy matches the provided {@code
   *     condensedUniqueId}
   */
  public ViewHierarchyElement getViewById(long condensedUniqueId) {
    int windowId = (int) (condensedUniqueId >>> 32);
    int viewId = (int) condensedUniqueId;
    return getWindowById(windowId).getViewById(viewId);
  }

  /** Returns a protocol buffer representation of this hierarchy. */
  public AccessibilityHierarchyProto toProto() {
    AccessibilityHierarchyProto.Builder builder = AccessibilityHierarchyProto.newBuilder();
    builder
        .setDeviceState(getDeviceState().toProto())
        .setOrigin(getOrigin().toProto())
        .setActiveWindowId(getActiveWindow().getId())
        .setViewElementClassNames(getViewElementClassNames().toProto());
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
    checkNotNull(proto);
    return new Builder(proto);
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
   * AccessibilityHierarchy#newBuilder(AccessibilityHierarchyProto)}.
   */
  public static class Builder {
    private final AccessibilityHierarchyProto proto;

    Builder(AccessibilityHierarchyProto proto) {
      this.proto = proto;
    }

    public AccessibilityHierarchy build() {
      DeviceState deviceState = new DeviceState(proto.getDeviceState());
      AccessibilityHierarchyOrigin origin =
          AccessibilityHierarchyOrigin.fromProto(proto.getOrigin());
      int activeWindowId = proto.getActiveWindowId();

      ImmutableList.Builder<WindowHierarchyElement> windowHierarchyElementsBuilder =
          ImmutableList.<WindowHierarchyElement>builderWithExpectedSize(proto.getWindowsCount());
      for (WindowHierarchyElementProto windowProto : proto.getWindowsList()) {
        windowHierarchyElementsBuilder.add(WindowHierarchyElement.newBuilder(windowProto).build());
      }
      ImmutableList<WindowHierarchyElement> windowHierarchyElements =
          windowHierarchyElementsBuilder.build();
      checkState(
          !windowHierarchyElements.isEmpty(), "Hierarchies must contain at least one window.");
      WindowHierarchyElement activeWindow = windowHierarchyElements.get(activeWindowId);
      ViewElementClassNames viewElementClassNames =
          new ViewElementClassNames(proto.getViewElementClassNames());

      AccessibilityHierarchy hierarchy =
          new AccessibilityHierarchy(
              deviceState, origin, windowHierarchyElements, activeWindow, viewElementClassNames);
      hierarchy.setAccessibilityHierarchy();
      return hierarchy;
    }
  }
}

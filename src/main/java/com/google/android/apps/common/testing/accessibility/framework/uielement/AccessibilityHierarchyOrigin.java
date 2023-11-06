package com.google.android.apps.common.testing.accessibility.framework.uielement;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.AccessibilityHierarchyOriginProto;
import com.google.common.collect.EnumBiMap;
import com.google.common.collect.ImmutableMap;

/** Represents a type of data source from which an AccessibilityHierarchy may be constructed. */
public enum AccessibilityHierarchyOrigin {
  UNKNOWN,
  VIEWS,
  ACCESSIBILITY_NODE_INFOS,
  ACCESSIBILITY_NODE_INFOS_AND_VIEWS,
  WINDOW_LIST;

  /** Mapping between Java and proto values. */
  private static final EnumBiMap<AccessibilityHierarchyOrigin, AccessibilityHierarchyOriginProto>
      ORIGIN_MAP =
          EnumBiMap.create(
              ImmutableMap
                  .<AccessibilityHierarchyOrigin, AccessibilityHierarchyOriginProto>builder()
                  .put(
                      AccessibilityHierarchyOrigin.UNKNOWN,
                      AccessibilityHierarchyOriginProto.ORIGIN_UNSPECIFIED)
                  .put(
                      AccessibilityHierarchyOrigin.VIEWS,
                      AccessibilityHierarchyOriginProto.ORIGIN_VIEWS)
                  .put(
                      AccessibilityHierarchyOrigin.ACCESSIBILITY_NODE_INFOS,
                      AccessibilityHierarchyOriginProto.ORIGIN_ACCESSIBILITY_NODE_INFOS)
                  .put(
                      AccessibilityHierarchyOrigin.ACCESSIBILITY_NODE_INFOS_AND_VIEWS,
                      AccessibilityHierarchyOriginProto.ORIGIN_ACCESSIBILITY_NODE_INFOS_AND_VIEWS)
                  .put(
                      AccessibilityHierarchyOrigin.WINDOW_LIST,
                      AccessibilityHierarchyOriginProto.ORIGIN_WINDOW_LIST)
                  .buildOrThrow());

  public static AccessibilityHierarchyOrigin fromProto(AccessibilityHierarchyOriginProto proto) {
    return checkNotNull(ORIGIN_MAP.inverse().get(proto));
  }

  public AccessibilityHierarchyOriginProto toProto() {
    return checkNotNull(ORIGIN_MAP.get(this));
  }
}

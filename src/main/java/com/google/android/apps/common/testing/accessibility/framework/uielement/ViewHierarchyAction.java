package com.google.android.apps.common.testing.accessibility.framework.uielement;

import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.ViewHierarchyActionProto;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Representation of {@code AccessibilityNodeInfo.AccessibilityAction} in a {@code View}.
 *
 * <p>The action exists within {@code ViewHierarchyElement} and it is one-to-one map from {@code
 * AccessibilityAction}.
 */
public class ViewHierarchyAction {
  private final int actionId;
  private final @Nullable CharSequence actionLabel;

  protected ViewHierarchyAction(int actionId, @Nullable CharSequence actionLabel) {
    this.actionId = actionId;
    this.actionLabel = actionLabel;
  }

  ViewHierarchyAction(ViewHierarchyActionProto proto) {
    this.actionId = proto.getActionId();
    this.actionLabel = proto.hasActionLabel() ? proto.getActionLabel() : null;
  }

  /**
   * Returns the action Id. Action Id will be one of the actions that are listed in {@code
   * AccessibilityNodeInfo}.
   */
  int getActionId() {
    return actionId;
  }

  /** Returns the label of the corresponding action id. */
  @Nullable
  CharSequence getActionLabel() {
    return actionLabel;
  }

  ViewHierarchyActionProto toProto() {
    ViewHierarchyActionProto.Builder builder = ViewHierarchyActionProto.newBuilder();

    builder.setActionId(actionId);
    if (!TextUtils.isEmpty(actionLabel)) {
      builder.setActionLabel(actionLabel.toString());
    }
    return builder.build();
  }
}

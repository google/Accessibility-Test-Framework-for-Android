package com.google.android.apps.common.testing.accessibility.framework.uielement;

import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.ViewHierarchyActionProto;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Representation of {@code AccessibilityNodeInfo.AccessibilityAction} in a {@code View}.
 *
 * <p>The action exists within {@code ViewHierarchyElement} and it is one-to-one map from {@code
 * AccessibilityAction}.
 */
public class ViewHierarchyActionAndroid extends ViewHierarchyAction {

  private ViewHierarchyActionAndroid(int actionId, @Nullable CharSequence actionLabel) {
    super(actionId, actionLabel);
  }

  /** Creates a builder which can build ViewHierarchyActionAndroid from AccessibilityAction */
  static Builder newBuilder(AccessibilityAction action) {
    return new Builder(action);
  }

  /** Creates a builder which can build ViewHierarchyActionAndroid from ViewHierarchyActionProto */
  static Builder newBuilder(ViewHierarchyActionProto actionProto) {
    return new Builder(actionProto);
  }

  /** Builder for {@code ViewHierarchyElementAndroid} */
  static class Builder {
    private final int actionId;
    private final @Nullable CharSequence actionLabel;

    Builder(AccessibilityAction action) {
      this.actionId = action.getId();
      this.actionLabel = action.getLabel();
    }

    Builder(ViewHierarchyActionProto actionProto) {
      this.actionId = actionProto.getActionId();
      this.actionLabel = actionProto.hasActionLabel() ? actionProto.getActionLabel() : null;
    }

    public ViewHierarchyActionAndroid build() {
      return new ViewHierarchyActionAndroid(actionId, actionLabel);
    }
  }
}

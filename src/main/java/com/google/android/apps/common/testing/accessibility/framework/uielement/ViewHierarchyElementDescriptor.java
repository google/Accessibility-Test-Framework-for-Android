package com.google.android.apps.common.testing.accessibility.framework.uielement;

import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;

/** An object that describes a {@link ViewHierarchyElement} */
public class ViewHierarchyElementDescriptor {

  /**
   * Returns a String description of the given {@link ViewHierarchyElement}. The default is to
   * return the view's resource entry name. If the view has no valid resource entry name, then
   * returns the view's bounds.
   *
   * @param element the {@link ViewHierarchyElement} to describe
   * @return a String description of the given {@link ViewHierarchyElement}
   */
  public String describe(ViewHierarchyElement element) {
    StringBuilder message = new StringBuilder();
    message.append("View ");
    if (!TextUtils.isEmpty(element.getResourceName())) {
      message.append(element.getResourceName());
    } else {
      Rect bounds = element.getBoundsInScreen();
      if (!bounds.isEmpty()) {
        message.append("with bounds: ");
        message.append(bounds.toShortString());
      } else {
        message.append("with no valid resource name or bounds");
      }
    }
    return message.toString();
  }
}

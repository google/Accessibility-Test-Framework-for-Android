package com.google.android.apps.common.testing.accessibility.framework.uielement;

import android.view.View;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base interface for customizing how to build an {@link AccessibilityHierarchyAndroid} from a given
 * {@link View}.
 *
 * <p>Currently only two methods are provided to support Android Studio integration.
 *
 * <p>Follow the following steps to add a new customization method into this interface:
 *
 * <ul>
 *   <li>Define a new customization methond in {@link CustomViewBuilderAndroid}
 *   <li>Start to use the new method in {@link CustomViewBuilderAndroid}
 *   <li>Add the default implementation of the method in {@link DefaultCustomViewBuilderAndroid}
 *   <li>Customized the implementation of the new method
 * </ul>
 */
public interface CustomViewBuilderAndroid {

  /**
   * Returns the class instance based on a given string class name. Returns {@code null} if not
   * found.
   */
  @Nullable
  Class<?> getClassByName(ViewHierarchyElementAndroid view, String className);

  /** Returns {@code true} if the given view is checkable. */
  boolean isCheckable(View fromView);
}

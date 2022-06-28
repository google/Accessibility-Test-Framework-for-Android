package com.google.android.apps.common.testing.accessibility.framework.uielement;

import android.os.StrictMode;
import android.view.View;
import android.widget.Checkable;
import com.google.android.libraries.accessibility.utils.log.LogUtils;
import com.google.android.material.button.MaterialButton;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Default implementation for interface {@link CustomViewBuilderAndroid}. */
public class DefaultCustomViewBuilderAndroid implements CustomViewBuilderAndroid {

  private static final String TAG = "DefaultClassByNameResolverAndroid";

  @Override
  public @Nullable Class<?> getClassByName(ViewHierarchyElementAndroid view, String className) {

    StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
    try {
      ClassLoader classLoader = view.getClass().getClassLoader();
      if (classLoader != null) {
        return classLoader.loadClass(className);
      } else {
        LogUtils.w(TAG, "Unsuccessful attempt to get ClassLoader to load %1$s", className);
      }
    } catch (ClassNotFoundException e) {
      LogUtils.w(TAG, "Unsuccessful attempt to load class %1$s.", className);
    } finally {
      StrictMode.setThreadPolicy(oldPolicy);
    }
    return null;
  }

  @Override
  public boolean isCheckable(View fromView) {
    if (fromView instanceof MaterialButton) {
      // Although MaterialButton implements Checkable, it isn't always checkable for accessibility.
      return ((MaterialButton) fromView).isCheckable();
    } else {
      return (fromView instanceof Checkable);
    }
  }
}

/*
 * Copyright (C) 2014 Google Inc.
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

package com.google.android.apps.common.testing.accessibility.framework;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import com.googlecode.eyesfree.utils.AccessibilityNodeInfoUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for checking accessibility of apps.
 *
 * <p>All methods that take {@link View} or {@link AccessibilityNodeInfo} objects as arguments
 * require them to be fully initialized and in a valid hierarchy. Neither a newly constructed
 * {@link View} nor an object returned by {@code View.createAccessibilityNodeInfo()} have the
 * required properties because calls like {@code View.getRootView()} and {@code
 * AccessibilityNodeInfo.getParent} do not return reasonable values unless the objects are part
 * of a valid hierarchy.
 */
public final class AccessibilityCheckUtils {

  private AccessibilityCheckUtils() {}

  private static Method isImportantForAccessibilityMethod;

  static {
    try {
      isImportantForAccessibilityMethod = View.class.getMethod("isImportantForAccessibility");
    } catch (NoSuchMethodException e) {
      isImportantForAccessibilityMethod = null;
    } catch (SecurityException e) {
      isImportantForAccessibilityMethod = null;
    }
  }
  /**
   * @param rootView The root of a View hierarchy
   * @return A Set containing the root view and all views below it in the hierarchy
   */
  public static Set<View> getAllViewsInHierarchy(View rootView) {
    Set<View> allViews = new HashSet<View>();
    allViews.add(rootView);
    addAllChildrenToSet(rootView, allViews);
    return allViews;
  }

  /**
   * Add all children in the view tree rooted at rootView to a set
   * @param rootView The root of the view tree desired
   * @param theSet The set to add views to
   */
  private static void addAllChildrenToSet(View rootView, Set<View> theSet) {
    if (!(rootView instanceof ViewGroup)) {
      return;
    }

    ViewGroup rootViewGroup = (ViewGroup) rootView;
    for (int i = 0; i < rootViewGroup.getChildCount(); ++i) {
      View nextView = rootViewGroup.getChildAt(i);
      theSet.add(nextView);
      addAllChildrenToSet(nextView, theSet);
    }
  }

  /**
   * Retrieve text for a node, which may include text from children of the node. This text is an
   * approximation of, but not always identical to, what TalkBack would speak for the node. One
   * difference is that there are no separators between the speakable text from different nodes.
   *
   * @param info The info whose text should be returned.
   *
   * @return Speakable text derived from the info and its children. Returns an empty string if
   * there is no such text, and {@code null} if {@code info == null}.
   */
  public static CharSequence getSpeakableTextForInfo(AccessibilityNodeInfo info) {
    if (info == null) {
      return null;
    }
    AccessibilityNodeInfoCompat compat = new AccessibilityNodeInfoCompat(info);

    // TODO(caseburkhardt) Pull in TalkBack's actual logic
    CharSequence nodeText = AccessibilityNodeInfoUtils.getNodeText(compat);
    StringBuilder returnStringBuilder = new StringBuilder((nodeText == null) ? "" : nodeText);
    /* If this node has a contentDescription, it overrides anything in children */
    if (TextUtils.isEmpty(compat.getContentDescription())) {
      for (int i = 0; i < compat.getChildCount(); ++i) {
        AccessibilityNodeInfoCompat child = compat.getChild(i);
        if (AccessibilityNodeInfoUtils.isVisibleOrLegacy(child)
            && !AccessibilityNodeInfoUtils.isActionableForAccessibility(child)) {
          returnStringBuilder
              .append(getSpeakableTextForInfo((AccessibilityNodeInfo) child.getInfo()));
        }
      }
    }
    return returnStringBuilder;
  }

  /**
   * Retrieve text for a {@link View}, which may include text from the children of the {@code View}.
   * This text is an approximation of, but not identical to, what TalkBack would speak for the
   * {@link View}. One difference is that there are no separators between the speakable text from
   * different nodes.
   * <p>
   * TalkBack also will not speak {@link View}s that aren't visible. This method assumes that the
   * {@link View} passed in is visible. The visibility of the rest of child nodes is inferred from
   * {@code view.getVisibility}.
   *
   * @param view The {@link View} whose text should be returned.
   *
   * @return Speakable text derived from the {@link View} and its children. Returns an empty string
   * if there is no such text, and {@code null} if {@code view == null}.
   */
  public static CharSequence getSpeakableTextForView(View view) {
    if (view == null) {
      return null;
    }

    SpannableStringBuilder returnStringBuilder = new SpannableStringBuilder("");

    boolean isImportantForAccessibility;
    /*
     * TODO(pweaver): Call view.isImportantForAccessibility() if (Build.VERSION.SDK_INT). Need
     * ViewCompat to support method
     */
    if (isImportantForAccessibilityMethod != null) {
      try {
        isImportantForAccessibility = (Boolean) isImportantForAccessibilityMethod.invoke(view);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        isImportantForAccessibility = true;
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
        isImportantForAccessibility = true;
      } catch (InvocationTargetException e) {
        e.printStackTrace();
        isImportantForAccessibility = true;
      }
    } else {
      isImportantForAccessibility = true;
    }

    /* Content descriptions override everything else */
    if (isImportantForAccessibility) {
        if (!TextUtils.isEmpty(view.getContentDescription())) {
          return view.getContentDescription();
        }
        if (view instanceof TextView) {
          if (!TextUtils.isEmpty(((TextView) view).getText())) {
            returnStringBuilder.append(((TextView) view).getText());
          } else if (!TextUtils.isEmpty(((TextView) view).getHint())) {
            returnStringBuilder.append(((TextView) view).getHint());
          }
        }
    }

    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i = 0; i < group.getChildCount(); ++i) {
        View childView = group.getChildAt(i);
        if ((childView.getVisibility() == View.VISIBLE)
            && !isActionableForAccessibility(childView)) {
          returnStringBuilder.append(getSpeakableTextForView(childView));
        }
      }
    }
    return returnStringBuilder;
  }

  /**
   * @param view
   * @return {@code true} if view's {@link AccessibilityNodeInfo} would be deemed actionable for
   * accessibility.
   */
  private static boolean isActionableForAccessibility(View view) {
    if (view == null) {
      return false;
    }

    return ((view.isClickable() || view.isLongClickable()) || view.isFocusable());
  }
}

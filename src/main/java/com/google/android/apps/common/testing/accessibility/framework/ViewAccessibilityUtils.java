/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.apps.common.testing.accessibility.framework;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import com.google.android.libraries.accessibility.utils.log.LogUtils;
import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class provides a set of utilities used to evaluate accessibility properties and behaviors of
 * hierarchies of {@link View}s.
 */

public final class ViewAccessibilityUtils {

  private static final String TAG = "ViewA11yUtils";

  private ViewAccessibilityUtils() {}

  /**
   * @param rootView The root of a View hierarchy
   * @return A Set containing the root view and all views below it in the hierarchy
   */
  public static Set<View> getAllViewsInHierarchy(View rootView) {
    Set<View> allViews = new HashSet<>();
    allViews.add(rootView);
    addAllChildrenToSet(rootView, allViews);
    return allViews;
  }

  /** See {@link View#isImportantForAccessibility()}. */
  public static boolean isImportantForAccessibility(View view) {
    if (view == null) {
      return false;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return view.isImportantForAccessibility();
    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
      // Prior to Jelly Bean, all Views were considered important for accessibility.
      return true;
    } else {
      // On APIs between 16 and 21, we must piece together accessibility importance from the
      // available properties. We return false incorrectly for some cases where unretrievable
      // listeners prevent us from determining importance.

      // If the developer marked the view as explicitly not important, it isn't.
      int mode = view.getImportantForAccessibility();
      if ((mode == View.IMPORTANT_FOR_ACCESSIBILITY_NO)
          || (mode == View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)) {
        return false;
      }

      // No parent view can be hiding us. (APIs 19 to 21)
      ViewParent parent = view.getParent();
      while (parent instanceof View) {
        if (((View) parent).getImportantForAccessibility()
            == View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS) {
          return false;
        }
        parent = parent.getParent();
      }

      // Interrogate the view's other properties to determine importance.
      return (mode == View.IMPORTANT_FOR_ACCESSIBILITY_YES)
          || isActionableForAccessibility(view)
          || hasListenersForAccessibility(view)
          || (view.getAccessibilityNodeProvider() != null)
          || isAccessibilityLiveRegion(view);
    }
  }

  /**
   * Determines if the supplied {@link View} is actionable for accessibility purposes.
   *
   * @param view The {@link View} to evaluate
   * @return {@code true} if {@code view} is considered actionable for accessibility
   */
  public static boolean isActionableForAccessibility(View view) {
    if (view == null) {
      return false;
    }

    return (view.isClickable() || view.isLongClickable() || view.isFocusable());
  }

  /**
   * Determines if the supplied {@link View} is visible to the user, which requires that it be
   * marked visible, that all its parents are visible, that it and all parents have alpha greater
   * than 0, and that it has non-zero size. This code attempts to replicate the protected method
   * {@code View.isVisibleToUser}.
   *
   * @param view The {@link View} to evaluate
   * @return {@code true} if {@code view} is visible to the user
   */
  public static boolean isVisibleToUser(View view) {
    if (view == null) {
      return false;
    }

    Object current = view;
    while (current instanceof View) {
      View currentView = (View) current;
      if ((currentView.getAlpha() <= 0) || (currentView.getVisibility() != View.VISIBLE)) {
        return false;
      }
      current = currentView.getParent();
    }
    return view.getGlobalVisibleRect(new Rect());
  }

  /**
   * Determines if the supplied {@link View} would be focused during navigation operations with a
   * screen reader.
   *
   * @param view The {@link View} to evaluate
   * @return {@code true} if a screen reader would choose to place accessibility focus on {@code
   *     view}, {@code false} otherwise.
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
  public static boolean shouldFocusView(View view) {
    if (view == null) {
      return false;
    }

    if (!isVisibleToUser(view) || !isImportantForAccessibility(view)) {
      // We don't focus views that are not visible or not important for accessibility
      return false;
    }

    if (isAccessibilityFocusable(view)) {
      if (!(view instanceof ViewGroup)
          || ((view instanceof ViewGroup) && !hasAnyImportantDescendant((ViewGroup) view))) {
        // Leaves that are accessibility focusable always gain focus regardless of presence of a
        // spoken description. This allows unlabeled, but still actionable, widgets to be activated
        // by the user.
        return true;
      } else if (isSpeakingView(view)) {
        // The view (or its grouped non-actionable children) have content to speak.
        return true;
      }

      return false;
    }

    if (hasText(view) && !hasFocusableAncestor(view)) {
      return true;
    }

    return false;
  }

  /**
   * Find a {@code View}, if one exists, that labels a given {@code View}.
   *
   * @param view The target of the labelFor.
   * @return The {@code View} that is the labelFor the specified view. {@code null} if nothing
   *     labels it.
   */
  public static @Nullable View getLabelForView(View view) {
    if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)) {
      /* Earlier versions don't support labelFor */
      return null;
    }
    int idToFind = view.getId();
    if (idToFind == View.NO_ID) {
      /* Views lacking IDs can't be labeled by others */
      return null;
    }

    /*
     * Search for the "nearest" View that labels this one, since IDs aren't unique. This code
     * follows the framework code by DFSing first children, then siblings, then parent and its
     * siblings, etc. childToSkip is passed in to the helper method to avoid repeating consideration
     * of a View when examining its parent.
     */
    View childToSkip = null;
    while (true) {
      View labelingView = lookForLabelForViewInViewAndChildren(view, childToSkip, idToFind);
      if (labelingView != null) {
        return labelingView;
      }
      ViewParent parent = view.getParent();
      childToSkip = view;
      if (!(parent instanceof View)) {
        return null;
      }
      view = (View) parent;
    }
  }

  /**
   * @param view The {@link View} to evaluate
   * @return {@link Boolean#TRUE} if {@code view} is considered editable, {@link Boolean#FALSE} if
   *     not, or {@code null} if this information cannot be determined.
   */
  public static @Nullable Boolean isViewEditable(View view) {
    if (view == null) {
      return null;
    }
    if (view instanceof EditText) {
      return true;
    }
    if (view instanceof TextView) {
      return ((TextView) view).getEditableText() != null;
    }
    return false;
  }

  /**
   * @param view The {@link View} to identify
   * @return a {@link String} resource name for the provided {@code view} in the format
   *     "package:type/entry", or {@code null} if a resource name does not exist or cannot be
   *     resolved.
   */
  public static @Nullable String getResourceNameForView(View view) {
    if (view == null
        || view.getId() == 0
        || view.getId() == View.NO_ID
        || view.getResources() == null) {
      return null;
    }

    if (!isViewIdGenerated(view.getId())) {
      try {
        return view.getResources().getResourceName(view.getId());
      } catch (Resources.NotFoundException nfe) {
        // Do nothing -- Potential test environment issue
        LogUtils.w(TAG, "Unable to resolve resource name from view ID.");
      }
    }
    return null;
  }

  /**
   * Determines if a View's resource identifier was generated at runtime.
   *
   * @param resourceId to evaluate
   * @return {@code true} if the identifier was generated a runtime, or {@code false} if generated
   *     by AAPT.
   */
  public static boolean isViewIdGenerated(int resourceId) {
    return (resourceId & 0xFF000000) == 0 && (resourceId & 0x00FFFFFF) != 0;
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1) // Calls View#getLabelFor
  private static @Nullable View lookForLabelForViewInViewAndChildren(
      View view, @Nullable View childToSkip, int idToFind) {
    if (view.getLabelFor() == idToFind) {
      return view;
    }
    if (!(view instanceof ViewGroup)) {
      return null;
    }
    ViewGroup viewGroup = (ViewGroup) view;
    for (int i = 0; i < viewGroup.getChildCount(); ++i) {
      View child = viewGroup.getChildAt(i);
      if (!child.equals(childToSkip)) {
        View labelingView = lookForLabelForViewInViewAndChildren(child, null, idToFind);
        if (labelingView != null) {
          return labelingView;
        }
      }
    }
    return null;
  }

  /**
   * Add all children in the view tree rooted at rootView to a set
   *
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
   * Determines if the supplied {@link View} has any retrievable listeners that might qualify the
   * view to be important for accessibility purposes.
   *
   * <p>NOTE: This method tries to behave like the hidden {@code
   * View#hasListenersForAccessibility()} method, but cannot retrieve several of the listeners.
   *
   * @param view The {@link View} to evaluate
   * @return {@code true} if any of the retrievable listeners on {@code view} might qualify it to be
   *     important for accessibility purposes.
   */
  private static boolean hasListenersForAccessibility(View view) {
    if (view == null) {
      return false;
    }

    boolean result = false;
    // Ideally, here we check for...
    // TouchDelegate
    result |= view.getTouchDelegate() != null;

    // OnKeyListener, OnTouchListener, OnGenericMotionListener, OnHoverListener, OnDragListener
    // aren't accessible to us.
    return result;
  }

  private static boolean isAccessibilityLiveRegion(View view) {
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        && (view.getAccessibilityLiveRegion() != View.ACCESSIBILITY_LIVE_REGION_NONE);
  }

  /**
   * Determines if the supplied {@link View} has an ancestor which meets the criteria for gaining
   * accessibility focus.
   *
   * <p>NOTE: This method only evaluates ancestors which may be considered important for
   * accessibility and explicitly does not evaluate the supplied {@code view}.
   *
   * @param view The {@link View} to evaluate
   * @return {@code true} if an ancestor of {@code view} may gain accessibility focus, {@code false}
   *     otherwise
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN) // Calls View#getParentForAccessibility
  private static boolean hasFocusableAncestor(View view) {
    if (view == null) {
      return false;
    }

    ViewParent parent = view.getParentForAccessibility();
    if (!(parent instanceof View)) {
      return false;
    }

    if (isAccessibilityFocusable((View) parent)) {
      return true;
    }

    return hasFocusableAncestor((View) parent);
  }

  /**
   * Determines if the supplied {@link View} meets the criteria for gaining accessibility focus.
   *
   * @param view The {@link View} to evaluate
   * @return {@code true} if it is possible for {@code view} to gain accessibility focus, {@code
   *     false} otherwise.
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN) // Calls isChildOfScrollableContainer
  private static boolean isAccessibilityFocusable(View view) {
    if (view == null) {
      return false;
    }

    if (view.getVisibility() != View.VISIBLE) {
      return false;
    }

    if (!isImportantForAccessibility(view)) {
      return false;
    }

    if (isActionableForAccessibility(view)) {
      return true;
    }

    return isChildOfScrollableContainer(view) && isSpeakingView(view);
  }

  /**
   * Determines if the supplied {@link View} is a top-level item within a scrollable container.
   *
   * @param view The {@link View} to evaluate
   * @return {@code true} if {@code view} is a top-level view within a scrollable container, {@code
   *     false} otherwise
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN) // Calls View#getParentForAccessibility
  private static boolean isChildOfScrollableContainer(View view) {
    if (view == null) {
      return false;
    }

    ViewParent viewParent = view.getParentForAccessibility();
    if ((viewParent == null) || !(viewParent instanceof View)) {
      return false;
    }

    View parent = (View) viewParent;
    if (parent.isScrollContainer()) {
      return true;
    }

    // Specifically check for parents that are AdapterView, ScrollView, or HorizontalScrollView, but
    // exclude Spinners, which are a special case of AdapterView.
    return (((parent instanceof AdapterView)
            || (parent instanceof ScrollView)
            || (parent instanceof HorizontalScrollView))
        && !(parent instanceof Spinner));
  }

  /**
   * Determines if the supplied {@link View} is one which would produce speech if it were to gain
   * accessibility focus.
   *
   * <p>NOTE: This method also evaluates the subtree of the {@code view} for children that should be
   * included in {@code view}'s spoken description.
   *
   * @param view The {@link View} to evaluate
   * @return {@code true} if a spoken description for {@code view} was determined, {@code false}
   *     otherwise.
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN) // Calls hasNonActionableSpeakingChildren
  private static boolean isSpeakingView(View view) {
    if (hasText(view)) {
      return true;
    } else if (view instanceof Checkable) {
      // Special case for checkable items, which screen readers may describe without text
      return true;
    } else if (hasNonActionableSpeakingChildren(view)) {
      return true;
    }

    return false;
  }

  /**
   * Determines if the supplied {@link View} has child view(s) which are not independently
   * accessibility focusable and also have a spoken description. Put another way, this method
   * determines if {@code view} has at least one child which should be included in {@code view}'s
   * spoken description if {@code view} were to be accessibility focused.
   *
   * @param view The {@link View} to evaluate
   * @return {@code true} if {@code view} has non-actionable speaking children within its subtree
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN) // Calls isAccessibilityFocusable
  private static boolean hasNonActionableSpeakingChildren(View view) {
    if ((view == null) || !(view instanceof ViewGroup)) {
      return false;
    }

    ViewGroup group = (ViewGroup) view;
    for (int i = 0; i < group.getChildCount(); ++i) {
      View child = group.getChildAt(i);
      if ((child == null)
          || (child.getVisibility() != View.VISIBLE)
          || isAccessibilityFocusable(child)) {
        continue;
      }

      if (isImportantForAccessibility(child) && isSpeakingView(child)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines if the supplied {@link View} has some form of a text description.
   *
   * @param view The {@link View} to evaluate
   * @return {@code true} if {@code view} has a contentDescription, text or hint.
   */
  private static boolean hasText(View view) {
    if (!TextUtils.isEmpty(view.getContentDescription())) {
      return true;
    } else if (view instanceof TextView) {
      TextView textView = (TextView) view;
      return !TextUtils.isEmpty(textView.getText()) || !TextUtils.isEmpty(textView.getHint());
    }

    return false;
  }

  /**
   * Determines if the provided {@code group} has any descendant, direct or indirect, which is
   * considered important for accessibility. This is useful in determining whether or not the
   * Android framework will attempt to reparent any child in the subtree as a direct descendant of
   * {@code group} while converting the hierarchy to an accessibility API representation.
   *
   * @param group the {@link ViewGroup} to evaluate
   * @return {@code true} if any child in {@code group}'s subtree is considered important for
   *     accessibility, {@code false} otherwise
   */
  private static boolean hasAnyImportantDescendant(ViewGroup group) {
    if (group == null) {
      return false;
    }

    for (int i = 0; i < group.getChildCount(); ++i) {
      View child = group.getChildAt(i);
      if (isImportantForAccessibility(child)) {
        return true;
      }

      if (child instanceof ViewGroup) {
        if (hasAnyImportantDescendant((ViewGroup) child)) {
          return true;
        }
      }
    }

    return false;
  }
}

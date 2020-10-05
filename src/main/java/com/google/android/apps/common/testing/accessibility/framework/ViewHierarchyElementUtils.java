package com.google.android.apps.common.testing.accessibility.framework;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableString;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableStringBuilder;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.android.apps.common.testing.accessibility.framework.uielement.WindowHierarchyElement;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class for initialization and evaluation of ViewHierarchyElements
 */
public final class ViewHierarchyElementUtils {
  public static final String ABS_LIST_VIEW_CLASS_NAME = "android.widget.AbsListView";
  public static final String ADAPTER_VIEW_CLASS_NAME = "android.widget.AdapterView";
  public static final String SCROLL_VIEW_CLASS_NAME = "android.widget.ScrollView";
  public static final String HORIZONTAL_SCROLL_VIEW_CLASS_NAME =
      "android.widget.HorizontalScrollView";
  public static final String SPINNER_CLASS_NAME = "android.widget.Spinner";
  public static final String TEXT_VIEW_CLASS_NAME = "android.widget.TextView";
  public static final String EDIT_TEXT_CLASS_NAME = "android.widget.EditText";
  public static final String IMAGE_VIEW_CLASS_NAME = "android.widget.ImageView";
  public static final String WEB_VIEW_CLASS_NAME = "android.webkit.WebView";
  public static final String SWITCH_CLASS_NAME = "android.widget.Switch";

  static final ImmutableList<String> SCROLLABLE_CONTAINER_CLASS_NAME_LIST =
      ImmutableList.of(
          ADAPTER_VIEW_CLASS_NAME, SCROLL_VIEW_CLASS_NAME, HORIZONTAL_SCROLL_VIEW_CLASS_NAME);

  private ViewHierarchyElementUtils() {}

  /**
   * Determine what text would be spoken by a screen reader for an element.
   *
   * @param element The element whose spoken text is desired. If it or its children are only
   *     partially initialized, this method may return additional text that would not be spoken.
   * @return An approximation of what a screen reader would speak for the element
   */
  public static SpannableString getSpeakableTextForElement(ViewHierarchyElement element) {
    if (element.isImportantForAccessibility()) {
      // Determine if this element is labeled by another element
      if (element.getLabeledBy() != null) {
        return getSpeakableTextFromElementSubtree(element.getLabeledBy());
      }
    }
    return getSpeakableTextFromElementSubtree(element);
  }

  /**
   * Determine what text would be spoken by a screen reader for an element and its subtree,
   * disregarding other labeling relationships within the hierarchy.
   *
   * @param element The element whose spoken text is desired
   * @return An approximation of what a screen reader would speak for the element and its subtree
   */
  private static SpannableString getSpeakableTextFromElementSubtree(ViewHierarchyElement element) {
    SpannableStringBuilder returnStringBuilder = new SpannableStringBuilder();
    if (element.isImportantForAccessibility()) {
      // Content descriptions override everything else -- including children
      SpannableString contentDescription = element.getContentDescription();
      if (!TextUtils.isEmpty(contentDescription)) {
        return returnStringBuilder.appendWithSeparator(contentDescription).build();
      }

      SpannableString text = element.getText();
      if (!TextUtils.isEmpty(text) && (TextUtils.getTrimmedLength(text) > 0)) {
        returnStringBuilder.appendWithSeparator(text);
      } else {
        SpannableString hint = element.getHintText();
        if (!TextUtils.isEmpty(hint) && (TextUtils.getTrimmedLength(hint) > 0)) {
          returnStringBuilder.appendWithSeparator(hint);
        }
      }

      if (TRUE.equals(element.isCheckable())) {
        if (TRUE.equals(element.isChecked())) {
          returnStringBuilder.appendWithSeparator("Checked");
        } else if (FALSE.equals(element.isChecked())) {
          returnStringBuilder.appendWithSeparator("Not checked");
        }
      }

      if (element.checkInstanceOf(ABS_LIST_VIEW_CLASS_NAME) && element.getChildViewCount() == 0) {
        returnStringBuilder.appendWithSeparator("List showing 0 items");
      }
    }

    /* Collect speakable text from children */
    for (int i = 0; i < element.getChildViewCount(); ++i) {
      ViewHierarchyElement child = element.getChildView(i);
      if (!FALSE.equals(child.isVisibleToUser()) && !isActionableForAccessibility(child)) {
        SpannableString childDesc = getSpeakableTextFromElementSubtree(child);
        if (!TextUtils.isEmpty(childDesc)) {
          returnStringBuilder.appendWithSeparator(childDesc);
        }
      }
    }

    return returnStringBuilder.build();
  }

  /**
   * Determines if the supplied {@link ViewHierarchyElement} would be focused during navigation
   * operations with a screen reader.
   *
   * @param view The {@link ViewHierarchyElement} to evaluate
   * @return {@code true} if a screen reader would choose to place accessibility focus on {@code
   *     view}, {@code false} otherwise.
   */
  public static boolean shouldFocusView(ViewHierarchyElement view) {
    if (!TRUE.equals(view.isVisibleToUser())) {
      // We don't focus views that are not visible
      return false;
    }

    if (isAccessibilityFocusable(view)) {
      if (!hasAnyImportantDescendant(view)) {
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

    if (hasText(view) && view.isImportantForAccessibility() && !hasFocusableAncestor(view)) {
      return true;
    }

    return false;
  }

  /**
   * Check if an element would correspond to an {@link
   * android.view.accessibility.AccessibilityNodeInfo} that would be deemed actionable for
   * accessibility.
   *
   * @param element The element to check
   * @return {@code true} if the element is actionable, {@code false} if not.
   */
  private static boolean isActionableForAccessibility(ViewHierarchyElement element) {
    return element.isClickable() || element.isFocusable() || element.isLongClickable();
  }

  /**
   * Determines if the supplied {@link ViewHierarchyElement} has an ancestor which meets the
   * criteria for gaining accessibility focus.
   *
   * <p>NOTE: This method only evaluates ancestors which may be considered important for
   * accessibility and explicitly does not evaluate the supplied {@code view}.
   *
   * @param view The {@link ViewHierarchyElement} to evaluate
   * @return {@code true} if an ancestor of {@code view} may gain accessibility focus, {@code false}
   *     otherwise
   */
  private static boolean hasFocusableAncestor(ViewHierarchyElement view) {
    ViewHierarchyElement parent = getImportantForAccessibilityAncestor(view);
    if (parent == null) {
      return false;
    }

    if (isAccessibilityFocusable(parent)) {
      return true;
    }

    return hasFocusableAncestor(parent);
  }

  /**
   * Determines if the supplied {@link ViewHierarchyElement} meets the criteria for gaining
   * accessibility focus.
   *
   * @param view The {@link ViewHierarchyElement} to evaluate
   * @return {@code true} if it is possible for {@code view} to gain accessibility focus, {@code
   *     false} otherwise.
   */
  private static boolean isAccessibilityFocusable(ViewHierarchyElement view) {
    if (!TRUE.equals(view.isVisibleToUser())) {
      return false;
    }

    if (!view.isImportantForAccessibility()) {
      return false;
    }

    if (isActionableForAccessibility(view)) {
      return true;
    }

    return isChildOfScrollableContainer(view) && isSpeakingView(view);
  }

  /**
   * Determines if the supplied {@link ViewHierarchyElement} is a top-level item within a scrollable
   * container.
   *
   * @param view The {@link ViewHierarchyElement} to evaluate
   * @return {@code true} if {@code view} is a top-level view within a scrollable container, {@code
   * false} otherwise
   */
  private static boolean isChildOfScrollableContainer(ViewHierarchyElement view) {

    // Identify the nearest importantForAccessibility parent
    ViewHierarchyElement parent = getImportantForAccessibilityAncestor(view);

    if (parent == null) {
      return false;
    }

    if (TRUE.equals(parent.isScrollable())) {
      return true;
    }

    // Specifically check for parents that are AdapterView, ScrollView, or HorizontalScrollView, but
    // exclude Spinners, which are a special case of AdapterView.  TalkBack explicitly identifies
    // views with parents matching these classes as direct children of a scrollable container.
    if (parent.checkInstanceOf(SPINNER_CLASS_NAME)) {
      return false;
    }

    return parent.checkInstanceOfAny(SCROLLABLE_CONTAINER_CLASS_NAME_LIST);
  }

  /**
   * Determines if the supplied {@link ViewHierarchyElement} is one which would produce speech if it
   * were to gain accessibility focus. <p> NOTE: This method also evaluates the subtree of the
   * {@code view} for children that should be included in {@code view}'s spoken description.
   *
   * @param view The {@link ViewHierarchyElement} to evaluate
   * @return {@code true} if a spoken description for {@code view} was determined, {@code false}
   * otherwise.
   */
  private static boolean isSpeakingView(ViewHierarchyElement view) {
    if (hasText(view)) {
      return true;
    } else if (TRUE.equals(view.isCheckable())) {
      // Special case for checkable items, which screen readers may describe without text
      return true;
    } else if (hasNonFocusableSpeakingChildren(view)) {
      return true;
    }

    return false;
  }

  /**
   * Determines if the supplied {@link ViewHierarchyElement} has child view(s) which are not
   * independently accessibility focusable and also have a spoken description. Put another way, this
   * method determines if {@code view} has at least one child which should be included in {@code
   * view}'s spoken description if {@code view} were to be accessibility focused.
   *
   * @param view The {@link ViewHierarchyElement} to evaluate
   * @return {@code true} if {@code view} has non-actionable speaking children within its subtree
   */
  private static boolean hasNonFocusableSpeakingChildren(ViewHierarchyElement view) {
    for (int i = 0; i < view.getChildViewCount(); ++i) {
      ViewHierarchyElement child = view.getChildView(i);
      if ((child == null)
          || !TRUE.equals(child.isVisibleToUser())
          || isAccessibilityFocusable(child)) {
        continue;
      }

      if (child.isImportantForAccessibility() && isSpeakingView(child)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines if the supplied {@link ViewHierarchyElement} has a contentDescription, text or hint.
   *
   * @param view The {@link ViewHierarchyElement} to evaluate
   * @return {@code true} if {@code view} has a contentDescription, text or hint, {@code false}
   *     otherwise.
   */
  private static boolean hasText(ViewHierarchyElement view) {
    return !TextUtils.isEmpty(view.getText())
        || !TextUtils.isEmpty(view.getContentDescription())
        || !TextUtils.isEmpty(view.getHintText());
  }

  /**
   * Returns the nearest ancestor in the provided {@code view}'s lineage that is important for
   * accessibility.
   *
   * @param view The {@link ViewHierarchyElement} to evaluate
   * @return The first important for accessibility {@link ViewHierarchyElement} in {@code view}'s
   * lineage, or {@code null} if no such ancestor exists.
   */
  private static @Nullable ViewHierarchyElement getImportantForAccessibilityAncestor(
      ViewHierarchyElement view) {
    ViewHierarchyElement parent = view.getParentView();
    while ((parent != null) && !view.isImportantForAccessibility()) {
      parent = parent.getParentView();
    }

    return parent;
  }

  /**
   * Determines if the provided {@code element} has any descendant, direct or indirect, which is
   * considered important for accessibility.  This is useful in determining whether or not the
   * Android framework will attempt to reparent any child in the subtree as a direct descendant of
   * {@code element} while converting the hierarchy to an accessibility API representation.
   *
   * @param element the {@link ViewHierarchyElement} to evaluate
   * @return {@code true} if any child in {@code element}'s subtree is considered important for
   *         accessibility, {@code false} otherwise
   */
  private static boolean hasAnyImportantDescendant(ViewHierarchyElement element) {
    for (int i = 0; i < element.getChildViewCount(); ++i) {
      ViewHierarchyElement child = element.getChildView(i);
      if (child.isImportantForAccessibility()) {
        return true;
      }

      if (child.getChildViewCount() > 0) {
        if (hasAnyImportantDescendant(child)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Determines whether the provided {@code viewHierarchyElement} on the active window is
   * intersected by any overlay {@link WindowHierarchyElement} whose z-order is greater than the
   * z-order of the active window.
   *
   * @param viewHierarchyElement the element to check
   * @return {@code true} if the {@code viewHierarchyElement} is intersected by any overlay {@link
   *     WindowHierarchyElement}, otherwise {@code false}
   */
  public static boolean isIntersectedByOverlayWindow(ViewHierarchyElement viewHierarchyElement) {
    AccessibilityHierarchy hierarchy = viewHierarchyElement.getWindow().getAccessibilityHierarchy();
    Integer activeWindowLayer = hierarchy.getActiveWindow().getLayer();
    if (activeWindowLayer == null) {
      return false;
    }

    for (WindowHierarchyElement window : hierarchy.getAllWindows()) {
      if ((window.getLayer() != null) && (checkNotNull(window.getLayer()) > activeWindowLayer)) {
        if (Rect.intersects(viewHierarchyElement.getBoundsInScreen(), window.getBoundsInScreen())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Determines whether the {@code element} on the active window is known to have an intersecting
   * overlay {@link ViewHierarchyElement} based upon their drawing orders in their parent views.
   *
   * @param element the element to check
   * @return {@code true} if the element is known to have an intersecting overlay element based upon
   *     their drawing orders, otherwise {@code false}
   * @see android.view.accessibility.AccessibilityNodeInfo#getDrawingOrder()
   */
  @SuppressWarnings("ReferenceEquality")
  public static boolean isIntersectedByOverlayView(ViewHierarchyElement element) {
    if (element.getDrawingOrder() == null) {
      return false;
    }

    AccessibilityHierarchy hierarchy = element.getWindow().getAccessibilityHierarchy();
    ViewHierarchyElement rootView = hierarchy.getActiveWindow().getRootView();

    ViewHierarchyElement view = element;
    while (view != rootView) {
      ViewHierarchyElement parentView = checkNotNull(view.getParentView());
      for (int i = 0; i < parentView.getChildViewCount(); i++) {
        ViewHierarchyElement siblingView = parentView.getChildView(i);
        if (checkNotNull(siblingView.getDrawingOrder()) > checkNotNull(view.getDrawingOrder())
            && Rect.intersects(element.getBoundsInScreen(), siblingView.getBoundsInScreen())) {
          return true;
        }
      }
      view = parentView;
    }

    return false;
  }

  /**
   * Determines whether the provided {@code viewHierarchyElement} on the active window may be
   * obscured by other on-screen content.
   *
   * @param viewHierarchyElement the element to check
   * @return {@code true} if the {@code viewHierarchyElement} may be obscured by other on-screen
   *     content, otherwise {@code false}
   */
  public static boolean isPotentiallyObscured(ViewHierarchyElement viewHierarchyElement) {
    return isIntersectedByOverlayWindow(viewHierarchyElement)
        || isIntersectedByOverlayView(viewHierarchyElement);
  }
}

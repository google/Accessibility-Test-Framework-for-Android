package com.google.android.apps.common.testing.accessibility.framework;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableString;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableStringBuilder;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.android.apps.common.testing.accessibility.framework.uielement.WindowHierarchyElement;
import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.Locale;
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
  public static final String TOGGLE_BUTTON_CLASS_NAME = "android.widget.ToggleButton";
  private static final String ANDROIDX_SCROLLING_VIEW_CLASS_NAME =
      "androidx.core.view.ScrollingView";

  private static final ImmutableList<String> SCROLLABLE_CONTAINER_CLASS_NAME_LIST =
      ImmutableList.of(
          ADAPTER_VIEW_CLASS_NAME,
          SCROLL_VIEW_CLASS_NAME,
          HORIZONTAL_SCROLL_VIEW_CLASS_NAME,
          ANDROIDX_SCROLLING_VIEW_CLASS_NAME);

  private ViewHierarchyElementUtils() {}

  /** @deprecated Use {@link #getSpeakableTextForElement(ViewHierarchyElement, Locale)} instead */
  @Deprecated
  public static SpannableString getSpeakableTextForElement(ViewHierarchyElement element) {
    return getSpeakableTextForElement(element, Locale.ENGLISH);
  }

  /**
   * Determine what text would be spoken by a screen reader for an element.
   *
   * @param element The element whose spoken text is desired. If it or its children are only
   *     partially initialized, this method may return additional text that would not be spoken.
   * @param locale The that was used to produce labels for the element. This should normally be the
   *     default Locale at the time that the app was tested.
   * @return An approximation of what a screen reader would speak for the element. This may not
   *     include any spans if the element is labeled by another element.
   */
  public static SpannableString getSpeakableTextForElement(
      ViewHierarchyElement element, Locale locale) {
    SpannableString speakableText = getSpeakableTextFromElementSubtree(element, locale);
    if (element.isImportantForAccessibility()) {
      // Determine if this element is labeled by another element
      ViewHierarchyElement labeledBy = element.getLabeledBy();
      if (labeledBy != null) {
        SpannableString label = getSpeakableElementTextOrLabel(labeledBy);
        if (!TextUtils.isEmpty(label)) {
          // Assumes that caller is not interested in any spans that may appear within 'label' or
          // 'speakableText'.
          return new SpannableString(
              String.format(
                  locale,
                  StringManager.getString(locale, "template_labeled_item"),
                  speakableText,
                  label),
              ImmutableList.of());
        }
      }
    }
    return speakableText;
  }

  /**
   * Determine what text would be spoken by a screen reader for an element and its subtree,
   * disregarding other labeling relationships within the hierarchy.
   *
   * @param element The element whose spoken text is desired
   * @return An approximation of what a screen reader would speak for the element and its subtree
   */
  private static SpannableString getSpeakableTextFromElementSubtree(
      ViewHierarchyElement element, Locale locale) {
    if (element.checkInstanceOf(TOGGLE_BUTTON_CLASS_NAME)
        || element.checkInstanceOf(SWITCH_CLASS_NAME)) {
      return ruleSwitch(element, locale);
    }

    SpannableStringBuilder returnStringBuilder = new SpannableStringBuilder();
    if (element.isImportantForAccessibility()) {
      CharSequence stateDescription = getDescriptionForTreeStatus(element, locale);
      if (stateDescription != null) {
        returnStringBuilder.appendWithSeparator(stateDescription);
      }

      // Content descriptions override everything else -- including children
      SpannableString contentDescription = element.getContentDescription();
      if (!TextUtils.isEmpty(contentDescription)) {
        return returnStringBuilder.appendWithSeparator(contentDescription).build();
      }

      SpannableString text = element.getText();
      if (!TextUtils.isEmpty(text) && (TextUtils.getTrimmedLength(text) > 0)) {
        returnStringBuilder.appendWithSeparator(text);
      }

      if (element.checkInstanceOf(ABS_LIST_VIEW_CLASS_NAME) && element.getChildViewCount() == 0) {
        returnStringBuilder.appendWithSeparator(
            String.format(
                locale,
                StringManager.getString(locale, "template_containers_quantity_other"),
                StringManager.getString(locale, "value_listview"),
                0));
      }
    }

    /* Collect speakable text from children */
    for (int i = 0; i < element.getChildViewCount(); ++i) {
      ViewHierarchyElement child = element.getChildView(i);
      if (!isFocusableOrClickableForAccessibility(child)) {
        SpannableString childDesc = getSpeakableTextFromElementSubtree(child, locale);
        if (!TextUtils.isEmpty(childDesc)) {
          returnStringBuilder.appendWithSeparator(childDesc);
        }
      }
    }

    if (element.isImportantForAccessibility()) {
      SpannableString hint = element.getHintText();
      if (!TextUtils.isEmpty(hint) && (TextUtils.getTrimmedLength(hint) > 0)) {
        returnStringBuilder.appendWithSeparator(hint);
      }
    }

    return returnStringBuilder.build();
  }

  /** Gets the state description for an element that is not a Switch or ToggleButton. */
  private static @Nullable CharSequence getDescriptionForTreeStatus(
      ViewHierarchyElement element, Locale locale) {
    if (element.getStateDescription() != null) {
      return element.getStateDescription();
    }

    if (TRUE.equals(element.isCheckable())) {
      if (TRUE.equals(element.isChecked())) {
        return StringManager.getString(locale, "value_checked");
      } else if (FALSE.equals(element.isChecked())) {
        return StringManager.getString(locale, "value_not_checked");
      }
    }
    return null;
  }

  private static SpannableString ruleSwitch(ViewHierarchyElement element, Locale locale) {
    if (element.isImportantForAccessibility()) {
      return dedupeJoin(getSwitchState(element, locale), getSwitchContent(element));
    }
    return new SpannableString("", ImmutableList.of()); // Empty string
  }

  private static @Nullable CharSequence getSwitchContent(ViewHierarchyElement element) {
    SpannableString contentDescription = element.getContentDescription();
    if (!TextUtils.isEmpty(contentDescription)) {
      return contentDescription;
    }

    CharSequence stateDescription = element.getStateDescription();
    SpannableString text = element.getText();
    if ((stateDescription != null)
        && !TextUtils.isEmpty(text)
        && (TextUtils.getTrimmedLength(text) > 0)) {
      return text;
    }

    return null;
  }

  private static @Nullable CharSequence getSwitchState(
      ViewHierarchyElement element, Locale locale) {
    if (element.getStateDescription() != null) {
      return element.getStateDescription();
    }

    SpannableString text = element.getText();
    if (!TextUtils.isEmpty(text) && (TextUtils.getTrimmedLength(text) > 0)) {
      return text;
    }

    if (TRUE.equals(element.isChecked())) {
      return StringManager.getString(locale, "value_on");
    } else if (FALSE.equals(element.isChecked())) {
      return StringManager.getString(locale, "value_off");
    }
    return null;
  }

  /**
   * Determine speakable text for an individual element, suitable for use as a label.
   *
   * @param element The element whose spoken text is desired
   * @return An approximation of what a screen reader would speak for the element
   */
  private static @Nullable SpannableString getSpeakableElementTextOrLabel(
      ViewHierarchyElement element) {
    if (element.isImportantForAccessibility()) {
      SpannableString contentDescription = element.getContentDescription();
      if (!TextUtils.isEmpty(contentDescription)) {
        return contentDescription;
      }

      SpannableString text = element.getText();
      if (!TextUtils.isEmpty(text) && (TextUtils.getTrimmedLength(text) > 0)) {
        return text;
      }
    }
    return null;
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

    if ((hasText(view) || !TextUtils.isEmpty(view.getStateDescription()))
        && view.isImportantForAccessibility()
        && !hasFocusableAncestor(view)) {
      return true;
    }

    return false;
  }

  /**
   * Returns the first ancestor of {@code view} that is focusable for accessibility. If no such
   * ancestor exists, returns {@code null}. First means the ancestor closest to {@code view}, not
   * the ancestor closest to the root of the view hierarchy. If {@code view} itself is accessibility
   * focusable, returns {@code view}.
   *
   * @param view The {@link ViewHierarchyElement} to evaluate.
   * @return The first ancestor of {@code view} that is accessiblity focusable.
   */
  public static @Nullable ViewHierarchyElement getFocusableForAccessibilityAncestor(
      ViewHierarchyElement view) {
    ViewHierarchyElement currentView = view;
    while ((currentView != null) && !isAccessibilityFocusable(currentView)) {
      currentView = currentView.getParentView();
    }
    return currentView;
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

    if (isFocusableOrClickableForAccessibility(view)) {
      return true;
    }

    return isChildOfScrollableContainer(view) && isSpeakingView(view);
  }

  /**
   * Returns whether a {@link ViewHierarchyElement} is focusable or clickable for accessibility.
   *
   * @param view the {@link ViewHierarchyElement} to check
   * @return {@code true} if the view is focusable or clickable for accessibility
   */
  private static boolean isFocusableOrClickableForAccessibility(ViewHierarchyElement view) {
    return !FALSE.equals(view.isVisibleToUser())
        && view.isImportantForAccessibility()
        && (view.isScreenReaderFocusable()
            || view.isClickable()
            || view.isFocusable()
            || view.isLongClickable());
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
    if (view.isImportantForAccessibility()) {
      if (hasText(view)) {
        return true;
      } else if (TRUE.equals(view.isCheckable())) {
        // Special case for checkable items, which screen readers may describe without text
        return true;
      }
    }

    if (hasNonFocusableSpeakingChildren(view)) {
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

      if (isSpeakingView(child)) {
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
    while ((parent != null) && !parent.isImportantForAccessibility()) {
      parent = parent.getParentView();
    }

    return parent;
  }

  /**
   * Determines if the provided {@code element} has any descendant, direct or indirect, which is
   * considered important for accessibility. This is useful in determining whether or not the
   * Android framework will attempt to reparent any child in the subtree as a direct descendant of
   * {@code element} while converting the hierarchy to an accessibility API representation.
   *
   * @param element the {@link ViewHierarchyElement} to evaluate
   * @return {@code true} if any child in {@code element}'s subtree is considered important for
   *     accessibility, {@code false} otherwise
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

  private static SpannableString dedupeJoin(@Nullable CharSequence... values) {
    SpannableStringBuilder returnStringBuilder = new SpannableStringBuilder();
    HashSet<String> uniqueValues = new HashSet<>();
    for (CharSequence value : values) {
      if (TextUtils.isEmpty(value)) {
        continue;
      }
      String lvalue = Ascii.toLowerCase(value.toString());
      if (uniqueValues.contains(lvalue)) {
        continue;
      }
      uniqueValues.add(lvalue);
      returnStringBuilder.appendWithSeparator(value);
    }
    return returnStringBuilder.build();
  }
}

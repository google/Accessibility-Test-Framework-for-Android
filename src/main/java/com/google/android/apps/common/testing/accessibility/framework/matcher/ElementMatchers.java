package com.google.android.apps.common.testing.accessibility.framework.matcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.is;

import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.base.Function;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/** A collection of hamcrest matchers that match {@link ViewHierarchyElement}s. */
public final class ElementMatchers {

  private ElementMatchers() {}

  /**
   * Returns a matcher that matches if the {@code toString} value of the element's text value is
   * equal to the provided string.
   *
   * <p>Convenience method for {@code withText(is(text))}.
   *
   * @param string value to match with the element's text
   */
  public static Matcher<ViewHierarchyElement> withText(String string) {
    return withText(is(string));
  }

  /**
   * Returns a matcher that matches if the {@code toString} value of the element's text value
   * matches the provided matcher.
   *
   * @param stringMatcher matcher applied to the toString value of the element's text
   */
  public static Matcher<ViewHierarchyElement> withText(Matcher<String> stringMatcher) {
    return new WithStringFeatureMatcher(stringMatcher, "text", ViewHierarchyElement::getText);
  }

  /**
   * Returns a matcher that matches if the {@code toString} value of the element's
   * contentDescription is equal to the provided string.
   *
   * <p>Convenience method for {@code withContentDescription(is(text))}.
   *
   * @param string value to match with the element's text
   */
  public static Matcher<ViewHierarchyElement> withContentDescription(String string) {
    return withContentDescription(is(string));
  }

  /**
   * Returns a matcher that matches if the {@code toString} value of the element's
   * contentDescription matches the provided matcher.
   *
   * @param stringMatcher matcher applied to the toString value of the element's text
   */
  public static Matcher<ViewHierarchyElement> withContentDescription(
      Matcher<String> stringMatcher) {
    return new WithStringFeatureMatcher(
        stringMatcher, "contentDescription", ViewHierarchyElement::getContentDescription);
  }

  /**
   * Returns a matcher that matches if the {@code toString} value of the element's testTag is equal
   * to the provided string.
   *
   * <p>Convenience method for {@code withTestTag(is(string))}.
   *
   * @param string value to match with the element's testTag
   */
  public static Matcher<ViewHierarchyElement> withTestTag(String string) {
    return withTestTag(is(string));
  }

  /**
   * Returns a matcher that matches if the {@code toString} value of the element's testTag matches
   * the provided matcher.
   *
   * @param stringMatcher matcher applied to the toString value of the element's testTag
   */
  public static Matcher<ViewHierarchyElement> withTestTag(Matcher<String> stringMatcher) {
    return new WithStringFeatureMatcher(stringMatcher, "testTag", ViewHierarchyElement::getTestTag);
  }

  /**
   * Returns a matcher that matches if the {@code toString} value of the element's className is
   * equal to the provided string.
   *
   * <p>Convenience method for {@code withClassName(is(string))}.
   *
   * @param string value to match with the element's className
   */
  public static Matcher<ViewHierarchyElement> withClassName(String string) {
    return withClassName(is(string));
  }

  /**
   * Returns a matcher that matches if the {@code toString} value of the element's className matches
   * the provided matcher.
   *
   * @param stringMatcher matcher applied to the toString value of the element's className
   */
  public static Matcher<ViewHierarchyElement> withClassName(Matcher<String> stringMatcher) {
    return new WithStringFeatureMatcher(
        stringMatcher, "className", ViewHierarchyElement::getClassName);
  }

  /** Returns a matcher that matches if the element is clickable. */
  public static Matcher<ViewHierarchyElement> isClickable() {
    return new WithBooleanFeatureMatcher(is(true), "clickable", ViewHierarchyElement::isClickable);
  }

  /**
   * Returns a matcher that matches if the element has a child that matches the provided matcher.
   *
   * @param childMatcher matcher applied to the element's children
   */
  public static Matcher<ViewHierarchyElement> withChild(
      Matcher<ViewHierarchyElement> childMatcher) {
    return new WithChildMatcher(checkNotNull(childMatcher));
  }

  /**
   * Matcher that matches if the {@code toString} value of a specified feature of the element
   * matches the provided matcher.
   */
  private static class WithStringFeatureMatcher
      extends FeatureMatcher<ViewHierarchyElement, String> {
    private final Function<ViewHierarchyElement, @Nullable CharSequence> extractor;

    /**
     * @param stringMatcher matcher applied to the toString value of the feature
     * @param extractor function that obtains the feature value from an element
     */
    private WithStringFeatureMatcher(
        Matcher<String> stringMatcher,
        String fetureName,
        Function<ViewHierarchyElement, @Nullable CharSequence> extractor) {
      super(stringMatcher, "Element with " + fetureName, fetureName);
      this.extractor = extractor;
    }

    @Override
    // Many Hamcrest matchers handle null but are not declared with @Nullable.

    @SuppressWarnings("override.return.invalid")
    protected @Nullable String featureValueOf(ViewHierarchyElement element) {
      CharSequence value = extractor.apply(element);
      return (value == null) ? null : value.toString();
    }
  }

  /**
   * Matcher that matches if the value of a specified Boolean feature of the element matches the
   * provided matcher.
   */
  private static class WithBooleanFeatureMatcher
      extends FeatureMatcher<ViewHierarchyElement, @Nullable Boolean> {
    private final Function<ViewHierarchyElement, @Nullable Boolean> extractor;

    /**
     * @param booleanMatcher matcher applied to the value of the feature
     * @param extractor function that obtains the feature value from an element
     */
    private WithBooleanFeatureMatcher(
        Matcher<@Nullable Boolean> booleanMatcher,
        String fetureName,
        Function<ViewHierarchyElement, @Nullable Boolean> extractor) {
      super(booleanMatcher, "Element with " + fetureName, fetureName);
      this.extractor = extractor;
    }

    @Override
    protected @Nullable Boolean featureValueOf(ViewHierarchyElement element) {
      return extractor.apply(element);
    }
  }

  /** Matcher that matches if the element has a child that matches the provided matcher. */
  private static class WithChildMatcher extends TypeSafeDiagnosingMatcher<ViewHierarchyElement> {
    private final Matcher<ViewHierarchyElement> childMatcher;

    private WithChildMatcher(Matcher<ViewHierarchyElement> childMatcher) {
      this.childMatcher = childMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("Element has child matching: ").appendDescriptionOf(childMatcher);
    }

    @Override
    protected boolean matchesSafely(ViewHierarchyElement element, Description mismatchDescription) {
      int count = element.getChildViewCount();
      if (count == 0) {
        mismatchDescription.appendText("Element has no children");
        return false;
      }

      for (int i = 0; i < count; i++) {
        if (childMatcher.matches(element.getChildView(i))) {
          return true;
        }
      }

      mismatchDescription.appendText("mismatches were: [");
      boolean isPastFirst = false;
      for (int i = 0; i < count; i++) {
        if (isPastFirst) {
          mismatchDescription.appendText(", ");
        }
        childMatcher.describeMismatch(element.getChildView(i), mismatchDescription);
        isPastFirst = true;
      }
      mismatchDescription.appendText("]");
      return false;
    }
  }
}

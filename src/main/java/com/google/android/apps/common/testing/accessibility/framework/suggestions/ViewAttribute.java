package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.common.annotations.Beta;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Represents a View attribute which contains a namespace and an attribute name. */
@Beta
public class ViewAttribute {

  public static final String NAMESPACE_ANDROID = "android";

  public static final String NAMESPACE_APP = "app";

  private final String namespace;

  private final String attributeName;

  public ViewAttribute(String namespace, String attributeName) {
    this.namespace = namespace;
    this.attributeName = attributeName;
  }

  /**
   * Creates a {@link ViewAttribute} in the "android" namespace.
   *
   * @param attributeName the name of the attribute
   */
  public ViewAttribute(String attributeName) {
    this(NAMESPACE_ANDROID, attributeName);
  }

  /** Returns the name of this attribute. */
  public String getAttributeName() {
    return attributeName;
  }

  /** Returns the namespace for this View attribute. */
  public String getNamespace() {
    return namespace;
  }

  /** Returns the fully qualified name of this View attribute such as "android:layout_width". */
  public String getFullyQualifiedName() {
    return namespace + ":" + attributeName;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof ViewAttribute) {
      ViewAttribute attribute = (ViewAttribute) o;
      return Objects.equals(attributeName, attribute.getAttributeName())
          && Objects.equals(namespace, attribute.namespace);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespace, attributeName);
  }
}

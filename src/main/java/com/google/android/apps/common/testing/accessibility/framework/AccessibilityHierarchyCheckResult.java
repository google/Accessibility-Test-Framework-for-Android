package com.google.android.apps.common.testing.accessibility.framework;

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.Nullable;
import android.text.Html;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.AccessibilityHierarchyCheckResultProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import java.util.Locale;
import java.util.Objects;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Result generated when an accessibility check runs on a {@code ViewHierarchyElement}.
 */
public class AccessibilityHierarchyCheckResult extends AccessibilityCheckResult {

  private final int resultId;
  private final @Nullable ViewHierarchyElement element;
  private final @Nullable ResultMetadata metadata;

  /**
   * @param checkClass The class of the check reporting the error
   * @param type The type of result
   * @param element The element that the result pertains to
   * @param resultId an integer unique to all results emitted from a single class
   * @param metadata a {@link Metadata} of extra data about this result
   */
  public AccessibilityHierarchyCheckResult(
      Class<? extends AccessibilityHierarchyCheck> checkClass,
      AccessibilityCheckResultType type,
      @Nullable ViewHierarchyElement element,
      int resultId,
      @Nullable ResultMetadata metadata) {
    super(checkNotNull(checkClass), checkNotNull(type), null);
    this.element = element;
    this.resultId = resultId;
    this.metadata = metadata;
  }

  /**
   * Creates an {@link AccessibilityHierarchyCheckResult} from its protocol buffer format
   *
   * @param proto The protocol buffer representation of a result, created with {@link #toProto()}
   * @param associatedHierarchy The {@link AccessibilityHierarchy} that was evaluated to produce
   *     this result
   */
  @SuppressWarnings("unchecked") // all Hierarchy result protos have Hierarchy check classes
  public static AccessibilityHierarchyCheckResult fromProto(
      AccessibilityHierarchyCheckResultProto proto, AccessibilityHierarchy associatedHierarchy) {
    Class<? extends AccessibilityHierarchyCheck> clazz;
    try {
      clazz =
          (Class<? extends AccessibilityHierarchyCheck>) Class.forName(proto.getSourceCheckClass());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(
          String.format("Failed to resolve check class: %1$s", proto.getSourceCheckClass()));
    }
    int resultId = proto.getResultId();
    AccessibilityCheckResultType type =
        AccessibilityCheckResultType.fromProto(proto.getResultType());
    Metadata metadata = (proto.hasMetadata()) ? Metadata.fromProto(proto.getMetadata()) : null;
    ViewHierarchyElement element =
        proto.hasHierarchySourceId()
            ? associatedHierarchy.getViewById(proto.getHierarchySourceId())
            : null;

    return new AccessibilityHierarchyCheckResult(clazz, type, element, resultId, metadata);
  }

  /**
   * Returns a general static "title" message for the {@link AccessibilityHierarchyCheck} that
   * generated this result, which may contain formatting markup.
   *
   * @param locale desired locale for the message
   */
  @SuppressWarnings("unchecked") // all Hierarchy results have Hierarchy check classes
  public String getRawTitleMessage(Locale locale) {
    AccessibilityHierarchyCheck check =
        AccessibilityCheckPreset.getHierarchyCheckForClass(
            (Class<? extends AccessibilityHierarchyCheck>) checkClass);
    checkNotNull(check, "Failed to resolve check class.");
    return check.getTitleMessage(locale);
  }

  /**
   * Returns a general static "title" message for the {@link AccessibilityHierarchyCheck} that
   * generated this result, without formatting markup.
   *
   * @param locale desired locale for the message
   */
  public CharSequence getTitleMessage(Locale locale) {
    return Html.fromHtml(getRawTitleMessage(locale));
  }

  /**
   * Returns a human-readable message representing the result identified by the check, which may
   * contain formatting markup
   *
   * @param locale desired locale for the message
   */
  @SuppressWarnings("unchecked") // all Hierarchy results have Hierarchy check classes
  public String getRawMessage(Locale locale) {
    AccessibilityHierarchyCheck check =
        AccessibilityCheckPreset.getHierarchyCheckForClass(
            (Class<? extends AccessibilityHierarchyCheck>) checkClass);
    checkNotNull(check, "Failed to resolve check class.");
    return check.getMessageForResult(locale, this);
  }

  @Override
  public CharSequence getMessage() {
    return getMessage(Locale.getDefault());
  }

  @Override
  public CharSequence getMessage(Locale locale) {
    return Html.fromHtml(getRawMessage(locale));
  }

  /**
   * Returns a concise human-readable message representing the result identified by the check,
   * which may contain formatting markup
   *
   * @param locale desired locale for the message
   */
  @SuppressWarnings("unchecked") // all Hierarchy results have Hierarchy check classes
  public String getRawShortMessage(Locale locale) {
    AccessibilityHierarchyCheck check =
        AccessibilityCheckPreset.getHierarchyCheckForClass(
            (Class<? extends AccessibilityHierarchyCheck>) checkClass);
    checkNotNull(check, "Failed to resolve check class.");
    return check.getShortMessageForResult(locale, this);
  }

  /**
   * Returns a concise human-readable message representing the result identified by the check,
   * which may contain formatting markup.
   *
   * @param locale desired locale for the message
   */
  public CharSequence getShortMessage(Locale locale) {
    return Html.fromHtml(getRawShortMessage(locale));
  }

  /**
   * Returns the integer id of this result. This id is unique within the class of the
   * {@link AccessibilityHierarchyCheck} class associated with this result, and is used to
   * differentiate different types of issues identified by a single check.
   * @return the id of this result.
   */
  public int getResultId() {
    return resultId;
  }

  /**
   * Retrieve the metadata stored in this result. This metadata is data computed during check
   * execution and is used to describe the specifics of a result or provide additional details about
   * a particular finding. The metadata keys for a given {@link AccessibilityHierarchyCheck} are
   * defined as constants in each {@code AccessibilityHierarchyCheck} class, and are unique within
   * that class.
   *
   * @return a {@link Metadata} for this result, or {@code null} if none was provided
   */
  @Pure
  public @Nullable ResultMetadata getMetadata() {
    return metadata;
  }

  /**
   * The {@link ViewHierarchyElement} within a view hierarchy to which this result refers
   *
   * @return the element associated with this result, or {@code null} if this result does not refer
   *     to a specific element within a view hierarchy.
   */
  @Pure
  public @Nullable ViewHierarchyElement getElement() {
    return element;
  }

  public AccessibilityHierarchyCheckResultProto toProto() {
    AccessibilityHierarchyCheckResultProto.Builder builder =
        AccessibilityHierarchyCheckResultProto.newBuilder();
    builder.setResultId(getResultId());
    if (getSourceCheckClass() != null) {
      builder.setSourceCheckClass(getSourceCheckClass().getName());
    }
    if (getType() != null) {
      builder.setResultType(getType().toProto());
    }
    if (getMetadata() instanceof Metadata) {
      builder.setMetadata(((Metadata) getMetadata()).toProto());
    }
    if (getElement() != null) {
      builder.setHierarchySourceId(getElement().getCondensedUniqueId());
    }

    return builder.build();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }

    AccessibilityHierarchyCheckResult that = (AccessibilityHierarchyCheckResult) o;

    if (getType() != that.getType()) {
      return false;
    }
    if (getResultId() != that.getResultId()) {
      return false;
    }
    if (getSourceCheckClass() != that.getSourceCheckClass()) {
      return false;
    }
    ViewHierarchyElement thatElement = that.getElement();
    if (getElement() != null) {
      if ((thatElement == null)
          || (getElement().getCondensedUniqueId() != thatElement.getCondensedUniqueId())) {
        return false;
      }
    } else if (thatElement != null) {
      return false;
    }

    return Objects.equals(getMetadata(), that.getMetadata());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getType(), getResultId(), getSourceCheckClass(), getElement(), getMetadata());
  }
}

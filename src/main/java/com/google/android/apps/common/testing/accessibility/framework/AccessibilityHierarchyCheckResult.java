package com.google.android.apps.common.testing.accessibility.framework;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.AccessibilityHierarchyCheckResultProto;
import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.AnswerProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Locale;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.jsoup.Jsoup;

/**
 * Result generated when an accessibility check runs on a {@code ViewHierarchyElement}.
 */
public class AccessibilityHierarchyCheckResult extends AccessibilityCheckResult {

  private final int resultId;
  private final @Nullable ViewHierarchyElement element;
  private final @Nullable ResultMetadata metadata;
  private final ImmutableList<Answer> answers;

  /**
   * Constructor when there are {@link Answer} elements associated with this result
   *
   * @param checkClass The class of the check reporting the error
   * @param type The type of result
   * @param element The element that the result pertains to
   * @param resultId an integer unique to all results emitted from a single class
   * @param metadata extra data about this result
   * @param answers the answers about this result
   */
  @Beta
  public AccessibilityHierarchyCheckResult(
      Class<? extends AccessibilityHierarchyCheck> checkClass,
      AccessibilityCheckResultType type,
      @Nullable ViewHierarchyElement element,
      int resultId,
      @Nullable ResultMetadata metadata,
      ImmutableList<Answer> answers) {
    super(checkNotNull(checkClass), checkNotNull(type), null);
    this.element = element;
    this.resultId = resultId;
    this.metadata = metadata;
    this.answers = answers;
  }

  /**
   * @param checkClass The class of the check reporting the error
   * @param type The type of result
   * @param element The element that the result pertains to
   * @param resultId an integer unique to all results emitted from a single class
   * @param metadata extra data about this result
   */
  public AccessibilityHierarchyCheckResult(
      Class<? extends AccessibilityHierarchyCheck> checkClass,
      AccessibilityCheckResultType type,
      @Nullable ViewHierarchyElement element,
      int resultId,
      @Nullable ResultMetadata metadata) {
    this(checkClass, type, element, resultId, metadata, ImmutableList.of());
  }

  /**
   * Returns a copy of this result, but with the AccessibilityCheckResultType changed to SUPPRESSED.
   */
  public AccessibilityHierarchyCheckResult getSuppressedResultCopy() {
    return new AccessibilityHierarchyCheckResult(
        getSourceCheckClass().asSubclass(AccessibilityHierarchyCheck.class),
        AccessibilityCheckResultType.SUPPRESSED,
        getElement(),
        resultId,
        metadata,
        answers);
  }

  /**
   * Creates an {@link AccessibilityHierarchyCheckResult} from its protocol buffer format
   *
   * @param proto The protocol buffer representation of a result, created with {@link #toProto()}
   * @param associatedHierarchy The {@link AccessibilityHierarchy} that was evaluated to produce
   *     this result
   */
  public static AccessibilityHierarchyCheckResult fromProto(
      AccessibilityHierarchyCheckResultProto proto, AccessibilityHierarchy associatedHierarchy) {
    AccessibilityHierarchyCheck check =
        AccessibilityCheckPreset.getHierarchyCheckForClassName(proto.getSourceCheckClass());
    checkNotNull(check, "Failed to resolve check class: %s", proto.getSourceCheckClass());
    int resultId = proto.getResultId();
    AccessibilityCheckResultType type =
        AccessibilityCheckResultType.fromProto(proto.getResultType());
    HashMapResultMetadata metadata =
        proto.hasMetadata() ? HashMapResultMetadata.fromProto(proto.getMetadata()) : null;
    ViewHierarchyElement element =
        proto.hasHierarchySourceId()
            ? associatedHierarchy.getViewById(proto.getHierarchySourceId())
            : null;
    ImmutableList.Builder<Answer> answersBuilder = ImmutableList.builder();

    for (AnswerProto answer : proto.getAnswersList()) {
      answersBuilder.add(Answer.fromProto(answer, associatedHierarchy));
    }

    return new AccessibilityHierarchyCheckResult(
        check.getClass(), type, element, resultId, metadata, answersBuilder.build());
  }

  /**
   * Returns a general static "title" message for the {@link AccessibilityHierarchyCheck} that
   * generated this result, which may contain formatting markup.
   *
   * @param locale desired locale for the message
   */
  public String getRawTitleMessage(Locale locale) {
    return getCheck().getTitleMessage(locale);
  }

  /**
   * Returns a general static "title" message for the {@link AccessibilityHierarchyCheck} that
   * generated this result, without formatting markup.
   *
   * @param locale desired locale for the message
   */
  public CharSequence getTitleMessage(Locale locale) {
    return Jsoup.parse(getRawTitleMessage(locale)).text();
  }

  /**
   * Returns a human-readable message representing the result identified by the check, which may
   * contain formatting markup
   *
   * @param locale desired locale for the message
   */
  public String getRawMessage(Locale locale) {
    return getCheck().getMessageForResult(locale, this);
  }

  @Override
  public CharSequence getMessage(Locale locale) {
    return Jsoup.parse(getRawMessage(locale)).text();
  }

  /**
   * Returns a concise human-readable message representing the result identified by the check,
   * which may contain formatting markup
   *
   * @param locale desired locale for the message
   */
  public String getRawShortMessage(Locale locale) {
    return getCheck().getShortMessageForResult(locale, this);
  }

  /**
   * Returns a concise human-readable message representing the result identified by the check,
   * which may contain formatting markup.
   *
   * @param locale desired locale for the message
   */
  public CharSequence getShortMessage(Locale locale) {
    return Jsoup.parse(getRawShortMessage(locale)).text();
  }

  /**
   * Returns the string representation of a URL for a Google Accessibility Help article related to
   * this issue, or {@code null} if no article is available.
   */
  public @Nullable String getHelpUrl() {
    return getCheck().getHelpUrl();
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
   * Returns the list of {@link Answer} to questions asked and answered for this result.
   *
   * @return the list of answers to asked questions
   */
  @Beta
  public ImmutableList<Answer> getAnswers() {
    return answers;
  }

  /**
   * Retrieve the metadata stored in this result. This metadata is data computed during check
   * execution and is used to describe the specifics of a result or provide additional details about
   * a particular finding. The metadata keys for a given {@link AccessibilityHierarchyCheck} are
   * defined as constants in each {@code AccessibilityHierarchyCheck} class, and are unique within
   * that class.
   *
   * @return a metadata for this result, or {@code null} if none was provided
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

  /**
   * Gets a secondary priority for the result.
   *
   * @see AccessibilityHierarchyCheck#getSecondaryPriority
   */
  public @Nullable Double getSecondaryPriority() {
    return getCheck().getSecondaryPriority(this);
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
    if (getMetadata() instanceof HashMapResultMetadata) {
      builder.setMetadata(((HashMapResultMetadata) getMetadata()).toProto());
    }
    if (getElement() != null) {
      builder.setHierarchySourceId(getElement().getCondensedUniqueId());
    }
    for (Answer answer : getAnswers()) {
      builder.addAnswers(answer.toProto());
    }

    return builder.build();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AccessibilityHierarchyCheckResult)) {
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
    if (!Objects.equals(getMetadata(), that.getMetadata())) {
      return false;
    }
    return getAnswers().equals(that.getAnswers());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getType(), getResultId(), getSourceCheckClass(), getElement(), getMetadata(), getAnswers());
  }

  // For debugging
  @Override
  public String toString() {
    return String.format(
        "AccessibilityHierarchyCheckResult %s %s %s %s %s num_answers:%d",
        getType(),
        getSourceCheckClass().getSimpleName(),
        getResultId(),
        getElement(),
        getMetadata(),
        getAnswers().size());
  }

  @SuppressWarnings("unchecked") // all Hierarchy results have Hierarchy check classes
  private AccessibilityHierarchyCheck getCheck() {
    AccessibilityHierarchyCheck check =
        AccessibilityCheckPreset.getHierarchyCheckForClass(
            (Class<? extends AccessibilityHierarchyCheck>) getSourceCheckClass());
    return checkNotNull(check, "Failed to resolve check class: %s", getSourceCheckClass());
  }
}

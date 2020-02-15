package com.google.android.apps.common.testing.accessibility.framework;

import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.AnswerProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.common.annotations.Beta;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/** A response to a {@link Question} about an {@link AccessibilityHierarchyCheckResult} */
@Beta
public class Answer {
  private final Class<? extends AnswerType> answerTypeClass;
  private final Question question;
  private final ResultMetadata metadata;

  /**
   * @param answerTypeClass class of the answer type represented by this answer
   * @param question the Question this is an answer to
   * @param metadata data needed to capture an answer
   */
  public Answer(
      Class<? extends AnswerType> answerTypeClass, Question question, ResultMetadata metadata) {
    this.answerTypeClass = answerTypeClass;
    this.question = question;
    this.metadata = metadata;
  }

  /** Returns the {@link AnswerType} class of this answer. */
  public Class<? extends AnswerType> getAnswerTypeClass() {
    return answerTypeClass;
  }

  /** Returns the {@link Question} this is the answer to. */
  public Question getQuestion() {
    return question;
  }

  /**
   * Returns the {@link ResultMetadata} that holds the answer data. The keys of the metadata are
   * determined by the {@link AnswerType} and interpreted by the {@link QuestionHandler}.
   */
  public ResultMetadata getMetadata() {
    return metadata;
  }

  /** Creates a protocol buffer for this {@link Answer} following its format */
  public AnswerProto toProto() {
    AnswerProto.Builder builder = AnswerProto.newBuilder();
    builder.setAnswerTypeClass(getAnswerTypeClass().getName());
    builder.setQuestion(getQuestion().toProto());
    if (getMetadata() instanceof HashMapResultMetadata) {
      builder.setMetadata(((HashMapResultMetadata) getMetadata()).toProto());
    }
    return builder.build();
  }

  /**
   * Creates an {@link Answer} from its protocol buffer format.
   *
   * @param proto The protocol buffer representation of an answer, created with {@link #toProto()}
   * @param associatedHierarchy The {@link AccessibilityHierarchy} that this answer is about
   * @throws IllegalArgumentException passing the {@link ClassNotFoundException} if there is an
   *     invalid {@link AnswerType} class in the {@link AnswerProto}. This should not occur as the
   *     {@link AnswerProto} is written in the same environment of ATF in which it is read.
   */
  public static Answer fromProto(AnswerProto proto, AccessibilityHierarchy associatedHierarchy) {
    Class<? extends AnswerType> answerTypeClass = null;
    try {
      answerTypeClass = Class.forName(proto.getAnswerTypeClass()).asSubclass(AnswerType.class);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
    Question question = Question.fromProto(proto.getQuestion(), associatedHierarchy);

    HashMapResultMetadata metadata = HashMapResultMetadata.fromProto(proto.getMetadata());

    return new Answer(answerTypeClass, question, metadata);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Answer)) {
      return false;
    }

    Answer that = (Answer) o;
    if (getAnswerTypeClass() != that.getAnswerTypeClass()) {
      return false;
    }
    if (!getQuestion().equals(that.getQuestion())) {
      return false;
    }
    return Objects.equals(getMetadata(), that.getMetadata());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getAnswerTypeClass(), getQuestion(), getMetadata());
  }
}

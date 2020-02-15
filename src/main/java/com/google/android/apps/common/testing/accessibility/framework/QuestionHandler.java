package com.google.android.apps.common.testing.accessibility.framework;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Manages questions, answers, and updating of results related to an {@link
 * AccessibilityHierarchyCheck}
 */
@Beta
public abstract class QuestionHandler {
  /** Returns whether the result has an unanswered {@link Question} */
  public boolean hasQuestion(AccessibilityHierarchyCheckResult result) {
    return getNextQuestion(result) != null;
  }

  /**
   * Returns the next {@link Question} if the {@link AccessibilityHierarchyCheckResult} has one,
   * else returns {@code null}
   */
  public abstract @Nullable Question getNextQuestion(AccessibilityHierarchyCheckResult result);

  /**
   * Returns the string for the phrasing of the passed question
   *
   * @param question the question needing to be phrased
   * @param locale the desired locale of the message
   * @return a human-readable String representing the phrasing of the Question
   */
  public abstract String getQuestionMessage(Question question, Locale locale);

  /**
   * Returns zero to many results to replace the original result based on an answer to a question
   *
   * @param answer the {@link Answer} (which contains its associated {@link Question} and original
   *     {@link AccessibilityHierarchyCheckResult}) that will be used to update the result
   * @return a potentially empty list of new result(s) to replace the original result based on the
   *     information in the original result and the answer list
   */
  public abstract ImmutableList<AccessibilityHierarchyCheckResult> updateResult(Answer answer);

  /**
   * Returns an {@link AccessibilityHierarchyCheckResult} with the information of the original
   * result and the new answer appended to list of answers
   */
  // originalResult is an {@link AccessibilityHierarchyCheckResult} so the sourceCheckClass is
  // guaranteed to be from a {@link AccessibilityHierarchyCheck} subclass
  @SuppressWarnings("unchecked")
  protected static ImmutableList<AccessibilityHierarchyCheckResult> updateResultByAppendingAnswer(
      Answer answer) {
    AccessibilityHierarchyCheckResult originalResult = answer.getQuestion().getOriginalResult();
    ImmutableList<Answer> answers =
        ImmutableList.<Answer>builder().addAll(originalResult.getAnswers()).add(answer).build();
    return ImmutableList.of(
        new AccessibilityHierarchyCheckResult(
            (Class<? extends AccessibilityHierarchyCheck>) originalResult.getSourceCheckClass(),
            originalResult.getType(),
            originalResult.getElement(),
            originalResult.getResultId(),
            originalResult.getMetadata(),
            answers));
  }

  /**
   * Return a {@link Answer} that answers the question of questionId, if it has been answered, else
   * returns {@code null}. Assumes answers are kept in an ordered list.
   */
  protected static @Nullable Answer getFirstAnswerForQuestionId(
      AccessibilityHierarchyCheckResult result, int questionId) {
    for (Answer answer : result.getAnswers()) {
      if (answer.getQuestion().getQuestionId() == questionId) {
        return answer;
      }
    }
    return null;
  }

  /**
   * Return a collection of {@link Answer} objects that answers the question of questionId. The
   * collection will be empty if the question is unanswered.
   */
  protected static ImmutableList<Answer> getAnswersForQuestionId(
      AccessibilityHierarchyCheckResult result, int questionId) {
    ImmutableList.Builder<Answer> answersBuilder = ImmutableList.builder();
    for (Answer answer : result.getAnswers()) {
      if (answer.getQuestion().getQuestionId() == questionId) {
        answersBuilder.add(answer);
      }
    }
    return answersBuilder.build();
  }

  /** Returns whether a question with questionId has been asked about a given result */
  protected static boolean haveAskedQuestion(
      AccessibilityHierarchyCheckResult result, int questionId) {
    return getFirstAnswerForQuestionId(result, questionId) != null;
  }
}

/*
 * Copyright (C) 2017 Google Inc.
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

package com.google.android.apps.common.testing.accessibility.framework.checks;

import static java.lang.Boolean.TRUE;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck.Category;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Check to detect problems in the developer specified accessibility traversal ordering. */
public class TraversalOrderCheck extends AccessibilityHierarchyCheck {

  /** Result when thew view is not visible. */
  public static final int RESULT_ID_NOT_VISIBLE = 1;
  /** Result when thew view is not {@code importantForAccessibility} */
  public static final int RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY = 2;
  /** Result when the view has an element involved in an accessibilityTraversalBefore loop. */
  public static final int RESULT_ID_TRAVERSAL_BEFORE_CYCLE = 3;
  /** Result when the view has an element involved in an accessibilityTraversalAfter loop. */
  public static final int RESULT_ID_TRAVERSAL_AFTER_CYCLE = 4;
  /** Result when the view has conflicting accessibilityTraversalBefore/After constraints. */
  public static final int RESULT_ID_TRAVERSAL_OVER_CONSTRAINED = 5;

  @Override
  protected @Nullable String getHelpTopic() {
    return "7664232";
  }

  @Override
  public Category getCategory() {
    return Category.IMPLEMENTATION;
  }

  @Override
  public List<AccessibilityHierarchyCheckResult> runCheckOnHierarchy(
      AccessibilityHierarchy hierarchy,
      @Nullable ViewHierarchyElement fromRoot,
      @Nullable Parameters parameters) {
    List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();
    List<? extends ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
    for (ViewHierarchyElement view : viewsToEval) {
      if (!TRUE.equals(view.isVisibleToUser())) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_NOT_VISIBLE,
            null));
        continue;
      }

      if (!view.isImportantForAccessibility()) {
        results.add(new AccessibilityHierarchyCheckResult(
            this.getClass(),
            AccessibilityCheckResultType.NOT_RUN,
            view,
            RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY,
            null));
        continue;
      }

      // See if view is involved in an accessibilityTraversalBefore cycle or an
      // accessibilityTraversalAfter cycle.
      List<ViewHierarchyElement> beforeChain;
      List<ViewHierarchyElement> afterChain;
      try {
        beforeChain = buildNodeChain(view, (el) -> el.getAccessibilityTraversalBefore());
      } catch (CycleException e) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.WARNING,
                view,
                RESULT_ID_TRAVERSAL_BEFORE_CYCLE,
                null));
        continue;
      }
      try {
        afterChain = buildNodeChain(view, (el) -> el.getAccessibilityTraversalAfter());
      } catch (CycleException e) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.WARNING,
                view,
                RESULT_ID_TRAVERSAL_AFTER_CYCLE,
                null));
        continue;
      }

      // See if view is involved in over constraint by before and after.
      Set<ViewHierarchyElement> intersection = intersectionOf(beforeChain, afterChain);
      intersection.remove(view);
      if (!intersection.isEmpty()) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.WARNING,
                view,
                RESULT_ID_TRAVERSAL_OVER_CONSTRAINED,
                null));
      }
    }

    return results;
  }

  @Override
  public String getMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    return generateMessageForResultId(locale, resultId);
  }

  @Override
  public String getShortMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    switch(resultId) {
      case RESULT_ID_NOT_VISIBLE:
        return StringManager.getString(locale, "result_message_not_visible");
      case RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY:
        return StringManager.getString(locale, "result_message_not_important_for_accessibility");
      default:
        return StringManager.getString(locale, "result_message_brief_unpredictable_traversal");
    }
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_accessibility_traversal");
  }

  private static String generateMessageForResultId(Locale locale, int resultId) {
    switch(resultId) {
      case RESULT_ID_NOT_VISIBLE:
        return StringManager.getString(locale, "result_message_not_visible");
      case RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY:
        return StringManager.getString(locale, "result_message_not_important_for_accessibility");
      case RESULT_ID_TRAVERSAL_BEFORE_CYCLE:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_traversal_cycle"),
            "android:accessibilityTraversalBefore");
      case RESULT_ID_TRAVERSAL_AFTER_CYCLE:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_traversal_cycle"),
            "android:accessibilityTraversalAfter");
      case RESULT_ID_TRAVERSAL_OVER_CONSTRAINED:
        return StringManager.getString(locale, "result_message_traversal_over_constrained");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  /**
   * Build a sequence of elements starting from {@code start} by repeatedly applying {@code
   * nextElementFunction}.
   *
   * @throws CycleException if a loop is detected
   */
  private static List<ViewHierarchyElement> buildNodeChain(
      ViewHierarchyElement start, NextElementFunction nextElementFunction) throws CycleException {
    List<ViewHierarchyElement> chain = new ArrayList<>();
    chain.add(start);
    ViewHierarchyElement ptr = start;
    while (true) {
      ptr = nextElementFunction.apply(ptr);
      if (ptr == null) {
        return chain;
      }
      if (chain.contains(ptr)) {
        throw new CycleException(chain);
      }
      chain.add(ptr);
    }
  }

  /** Find the elements that two lists have in common. */
  private static <T> Set<T> intersectionOf(List<T> list1, List<T> list2) {
    Set<T> intersection = new HashSet<T>(list1);
    intersection.retainAll(new HashSet<T>(list2));
    return intersection;
  }

  private interface NextElementFunction {
    @Nullable ViewHierarchyElement apply(ViewHierarchyElement el);
  }

  private static class CycleException extends Exception {
    private final List<ViewHierarchyElement> elements;

    CycleException(List<ViewHierarchyElement> elements) {
      this.elements = elements;
    }

    List<ViewHierarchyElement> getElements() {
      return elements;
    }
  }
}

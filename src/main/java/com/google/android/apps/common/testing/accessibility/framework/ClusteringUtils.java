/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.apps.common.testing.accessibility.framework;

import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Utilities for clustering and organizing check results for reporting or display.
 *
 * <p>The method {@link #cluster} is a generalized method that can take a collection of objects
 * and form groups of similar objects according to a criteria of {@link Similarity} specified as an
 * method argument.
 *
 * <p>This class also proposes three possible criteria for the similarity of
 * AccessibilityHierarchyCheckResults: {@link #SIMILAR_RESULTS}, {@link
 * #SIMILAR_RESULTS_NEAREST_ANCESTOR_RELATION} and {@link #SIMILAR_RESULTS_NEAREST_ANCESTOR_CHAIN}.
 * Others are possible. These are all experimental heuristics, and could potentially judge two
 * results to be similar when they are in fact due to unrelated problems.
 */
public class ClusteringUtils {

  /**
   * Represents a predicate indicating similarity between two values.
   *
   * @param <T> the type of values being compared
   */

  public interface Similarity<T> {
    /**
     * Evaluates this predicate on the given arguments.
     *
     * @return {@code true} if the two arguments are considered similar.
     */
    boolean test(T value1, T value2);
  }

  /** Function that produces a resource identifier for an element. */

  public interface ResourceIdGenerator {
    /** Returns a resource identifier for the given element. */
    @Nullable String apply(ViewHierarchyElement vhe);
  }

  /**
   * A predicate indicating similarity between two {@link AccessibilityHierarchyCheckResult}s. Two
   * results are similar if they have the same values for {@link
   * AccessibilityHierarchyCheckResult#getType}, {@link
   * AccessibilityHierarchyCheckResult#getSourceCheckClass}, and {@link
   * AccessibilityHierarchyCheckResult#getResultId}, and their ViewHierarchyElements have the same
   * resource name.
   */
  public static final Similarity<AccessibilityHierarchyCheckResult> SIMILAR_RESULTS =
      new ResultSimilarity(ViewHierarchyElement::getResourceName);

  /**
   * A predicate indicating similarity between two {@link AccessibilityHierarchyCheckResult}s.
   * The criteria for similarity is more lenient than {@link #SIMILAR_RESULTS} in that the
   * ViewHierarchyElements do not need to have the same resource name. The ViewHierarchyElements
   * have to have the same pseudo-resource ID, including indices.
   *
   * <p>Two results are "pseudo similar" if they have the same values for {@link
   * AccessibilityHierarchyCheckResult#getType}, {@link
   * AccessibilityHierarchyCheckResult#getSourceCheckClass}, and {@link
   * AccessibilityHierarchyCheckResult#getResultId}, and their ViewHierarchyElements have the same
   * "path" below elements with the same resource name.
   *
   * @see #getPseudoResourceId
   */
  public static final Similarity<AccessibilityHierarchyCheckResult>
      SIMILAR_RESULTS_NEAREST_ANCESTOR_RELATION =
      new ResultSimilarity(vhe -> getPseudoResourceId(vhe, /* includeIndices= */ true));

  /**
   * A predicate indicating similarity between two {@link AccessibilityHierarchyCheckResult}s.
   * The criteria for similarity is more lenient than
   * {@link #SIMILAR_RESULTS_NEAREST_ANCESTOR_RELATION} in that the ViewHierarchyElements do not
   * need to have the same path below elements with the same resource name. They only need to have
   * the same pseudo-resource ID, excluding indices.
   *
   * <p>Two results are "pseudo similar" if they have the same values for {@link
   * AccessibilityHierarchyCheckResult#getType}, {@link
   * AccessibilityHierarchyCheckResult#getSourceCheckClass}, and {@link
   * AccessibilityHierarchyCheckResult#getResultId}, and their ViewHierarchyElements have the same
   * sequence of element class names below elements with the same resource name.
   *
   * @see #getPseudoResourceId
   */
  public static final Similarity<AccessibilityHierarchyCheckResult>
      SIMILAR_RESULTS_NEAREST_ANCESTOR_CHAIN =
      new ResultSimilarity(vhe -> getPseudoResourceId(vhe, /* includeIndices= */ false));

  /**
   * A generalized predicate indicating similarity between two
   * {@link AccessibilityHierarchyCheckResult}s.
   *
   * <p>Two results are similar if they have the same values for {@link
   * AccessibilityHierarchyCheckResult#getType}, {@link
   * AccessibilityHierarchyCheckResult#getSourceCheckClass}, and {@link
   * AccessibilityHierarchyCheckResult#getResultId}, and their ViewHierarchyElements have the same
   * non-null resource ID according to the specified resource ID producing function.
   */
  private static class ResultSimilarity implements Similarity<AccessibilityHierarchyCheckResult> {

    private final ResourceIdGenerator idGenerator;

    /**
     * @param idGenerator function that produces a resource ID for an element.
     */
    ResultSimilarity(ResourceIdGenerator idGenerator) {
      this.idGenerator = idGenerator;
    }

    @Override
    public boolean test(
        AccessibilityHierarchyCheckResult result1, AccessibilityHierarchyCheckResult result2) {
      ViewHierarchyElement vhe1 = result1.getElement();
      ViewHierarchyElement vhe2 = result2.getElement();

      if (Objects.equals(result1.getType(), result2.getType())
          && Objects.equals(result1.getSourceCheckClass(), result2.getSourceCheckClass())
          && (result1.getResultId() == result2.getResultId())
          && (vhe1 != null)
          && (vhe2 != null)) {
        String resourceId1 = idGenerator.apply(vhe1);
        if (resourceId1 != null) {
          return resourceId1.equals(idGenerator.apply(vhe2));
        }
      }
      return false;
    }
  }

  private ClusteringUtils() {} // Utility class

  /**
   * Groups values into sets of similar values.
   *
   * <p>This algorithm has worst case complexity of O(n^2).
   *
   * @param values values to be clustered. This is expected to be fairly small in size.
   * @param similarity predicate that indicates whether two values are similar
   * @return a list wherein each entry is a list of similar values. The values within each entry
   *     have the same relative ordering as in the original list. And the first values of the
   *     entries have the same relative ordering as in the original list.
   */
  public static <T> List<List<T>> cluster(Collection<T> values, Similarity<T> similarity) {
    List<List<T>> clusters = new ArrayList<>();
    for (T value : values) {
      boolean alreadySeen = false;
      for (List<T> cluster : clusters) {
        if (similarity.test(cluster.get(0), value)) {
          cluster.add(value);
          alreadySeen = true;
          break;
        }
      }
      if (!alreadySeen) {
        clusters.add(Lists.<T>newArrayList(value));
      }
    }
    return clusters;
  }

  /**
   * Gets a String that attempts to identify an element based upon a resource name. The result will
   * be the same as the element's resource name if it has one. Otherwise the result will indicate
   * the relationship of the given element to its nearest ancestor with a resource name.
   *
   * <p>If {@code includeIndices} is {@code false}, then the result will indicate the simple class
   * names of the ancestor elements in the accessibility hierarchy leading up to an element with a
   * resource name. For example, "app:id/toolbar/LinearLayout/ItemRenderer" would indicate an
   * element with a simple class name of "ItemRenderer" under an element with a simple class name
   * of "LinearLayout", under an element with the resource name {@code app:id/toolbar}.
   *
   * <p>If {@code includeIndices} is true, then the result will also indicate the indices of the
   * children in the path from the ancestor. For example,
   * "app:id/toolbar/LinearLayout[2]/ItemRenderer[1]" would indicate the element is the second
   * child of the third child of an element with the resource name {@code app:id/toolbar}.
   *
   * @return the pseudo resource ID, or {@code null} if neither the element or any of its ancestors
   *     has a resource name.
   */
  @Pure
  public static @Nullable String getPseudoResourceId(
      ViewHierarchyElement vhe, boolean includeIndices) {
    if (vhe.getResourceName() != null) {
      return vhe.getResourceName();
    }
    StringBuilder resourceId = getResourceIdBuilder(vhe, includeIndices);
    return (resourceId == null) ? null : resourceId.toString();
  }

  @SuppressWarnings("ReferenceEquality")
  private static @Nullable StringBuilder getResourceIdBuilder(
      ViewHierarchyElement vhe, boolean includeIndices) {
    String resourceName = vhe.getResourceName();
    if (resourceName != null) {
      return new StringBuilder(resourceName);
    }
    ViewHierarchyElement parent = vhe.getParentView();
    if (parent != null) {
      StringBuilder parentResourceId = getResourceIdBuilder(parent, includeIndices);
      if (parentResourceId != null) {
        int childCount = parent.getChildViewCount();
        for (int i = 0; i < childCount; i++) {
          // Comparing object instances because ViewHierarchyElement.equals() can be expensive.
          if (parent.getChildView(i) == vhe) {
            CharSequence shortClassName = getShortClassName(vhe);
            if (shortClassName != null) {
              parentResourceId.append('/').append(shortClassName);
              if (includeIndices) {
                parentResourceId.append('[').append(i).append(']');
              }
            } else if (includeIndices) {
              parentResourceId.append(":nth-child(").append(i).append(')');
            } else {
              parentResourceId.append(":child");

            }
            return parentResourceId;
          }
        }
      }
    }
    return null;
  }

  /**
   * Returns the simple name of the class to which the given view belongs, or {@code null} if one
   * cannot be determined.
   */
  private static @Nullable CharSequence getShortClassName(ViewHierarchyElement vhe) {
    CharSequence className = vhe.getClassName();
    if (className != null) {
      return simpleClassName(className);
    }
    className = vhe.getAccessibilityClassName();
    if (className != null) {
      return simpleClassName(className);
    }
    return null;
  }

  /**
   * Returns the part of the given name that is after the last period or dollar sign. Returns the
   * whole name if these characters are not present.
   */
  private static CharSequence simpleClassName(CharSequence className) {
    for (int i = className.length() - 1; i > 0; i--) {
      char ithChar = className.charAt(i);
      if ((ithChar == '.') || (ithChar == '$')) {
        return className.subSequence(i + 1, className.length());
      }
    }
    return className;
  }
}

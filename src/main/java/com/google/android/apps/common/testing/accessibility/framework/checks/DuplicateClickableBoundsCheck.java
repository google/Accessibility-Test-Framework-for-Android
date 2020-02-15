/*
 * Copyright (C) 2015 Google Inc.
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck.Category;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.HashMapResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Developers sometimes have containers marked clickable when they don't process click events. This
 * error is difficult to detect, but when a container shares its bounds with a child view, that is a
 * clear error. This class catches that case.
 */
public class DuplicateClickableBoundsCheck extends AccessibilityHierarchyCheck {
  /** Result when the view has the same bounds as another view. */
  public static final int RESULT_ID_SAME_BOUNDS = 1;
  /** [Legacy] Result to inform the user of a view's bounds .*/
  public static final int RESULT_ID_VIEW_BOUNDS = 2;

  /** Result metadata key for a {@code boolean} of whether both views were clickable. */
  public static final String KEY_CONFLICTS_BECAUSE_CLICKABLE =
      "KEY_CONFLICTS_BECAUSE_CLICKABLE";
  /** Result metadata key for a {@code boolean} of whether both views were long clickable. */
  public static final String KEY_CONFLICTS_BECAUSE_LONG_CLICKABLE =
      "KEY_CONFLICTS_BECAUSE_LONG_CLICKABLE";
  /** Result metadata key for the {@code int} number of other views with the same bounds. */
  public static final String KEY_CONFLICTING_VIEW_COUNT = "KEY_CONFLICTING_VIEW_COUNT";
  /**
   * Result metadata key for the {@code int} of the left coordinate of the location of views with
   * the same bounds.
   */
  public static final String KEY_CONFLICTING_LOCATION_LEFT = "KEY_CONFLICTING_LOCATION_LEFT";
  /**
   * Result metadata key for the {@code int} of the top coordinate of the location of views with
   * the same bounds.
   */
  public static final String KEY_CONFLICTING_LOCATION_TOP = "KEY_CONFLICTING_LOCATION_TOP";
  /**
   * Result metadata key for the {@code int} of the right coordinate of the location of views with
   * the same bounds.
   */
  public static final String KEY_CONFLICTING_LOCATION_RIGHT = "KEY_CONFLICTING_LOCATION_RIGHT";
  /**
   * Result metadata key for the {@code int} of the bottom coordinate of the location of views with
   * the same bounds.
   */
  public static final String KEY_CONFLICTING_LOCATION_BOTTOM = "KEY_CONFLICTING_LOCATION_BOTTOM";

  /** A bitmask for a clickable view. */
  public static final byte ELEMENT_MASK_CLICKABLE = 0x1;
  /** A bitmask for a long-clickable view. */
  public static final byte ELEMENT_MASK_LONG_CLICKABLE = 0x2;

  @Override
  protected String getHelpTopic() {
    return "6378943"; // Duplicate clickable Views
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

    /* Find all bounds and the clickable views that have those bounds within the full hierarchy */
    Map<ViewLocationActionHolder, List<ViewHierarchyElement>> locationActionToViewMap =
        getLocationActionToViewMap(hierarchy.getActiveWindow().getAllViews());

    /* Deal with any duplicate bounds within our set of elements to evaluate */
    List<? extends ViewHierarchyElement> viewsToEval =
        (fromRoot != null) ? fromRoot.getSelfAndAllDescendants() : null;
    for (List<? extends ViewHierarchyElement> elements : locationActionToViewMap.values()) {
      if (elements.size() < 2) {
        continue; // Bounds are not duplicated
      }

      for (ViewHierarchyElement culprit : elements) {
        if ((viewsToEval == null) || viewsToEval.contains(culprit)) {
          ResultMetadata resultMetadata = new HashMapResultMetadata();
          resultMetadata.putBoolean(KEY_CONFLICTS_BECAUSE_CLICKABLE, culprit.isClickable());
          resultMetadata
              .putBoolean(KEY_CONFLICTS_BECAUSE_LONG_CLICKABLE, culprit.isLongClickable());
          resultMetadata.putInt(KEY_CONFLICTING_VIEW_COUNT, elements.size() - 1);
          setBoundsInMetadata(culprit.getBoundsInScreen(), resultMetadata);
          results.add(new AccessibilityHierarchyCheckResult(
              this.getClass(),
              AccessibilityCheckResultType.ERROR,
              culprit,
              RESULT_ID_SAME_BOUNDS,
              resultMetadata));
          break;
        }
      }
    }

    return results;
  }

  @Override
  public String getMessageForResult(Locale locale, AccessibilityHierarchyCheckResult result) {
    int resultId = result.getResultId();
    if ((resultId == RESULT_ID_SAME_BOUNDS) || (resultId == RESULT_ID_VIEW_BOUNDS)) {
      ResultMetadata metadata = result.getMetadata();
      Rect bounds = getBoundsFromMetadata(metadata);
      if ((bounds == null) && (result.getElement() != null)) {
        // For legacy results, remap hierarchy element bounds to metadata
        ViewHierarchyElement culprit = result.getElement();
        ResultMetadata updatedMetadata =
            (metadata != null) ? metadata.clone() : new HashMapResultMetadata();
        setBoundsInMetadata(culprit.getBoundsInScreen(), updatedMetadata);
        AccessibilityHierarchyCheckResult updatedResult =
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                result.getType(),
                culprit,
                resultId,
                updatedMetadata);
        return super.getMessageForResult(locale, updatedResult);
      }
    }

    return super.getMessageForResult(locale, result);
  }

  @Override
  public String getMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    // For each of the following result IDs, metadata will have been set on the result, and metadata
    // will contain bounds.
    checkNotNull(metadata);
    Rect bounds = checkNotNull(getBoundsFromMetadata(metadata));
    String actionString = getActionString(
        locale,
        metadata.getBoolean(KEY_CONFLICTS_BECAUSE_CLICKABLE, false),
        metadata.getBoolean(KEY_CONFLICTS_BECAUSE_LONG_CLICKABLE, false));
    switch(resultId) {
      case RESULT_ID_SAME_BOUNDS:
        return String.format(locale,
            StringManager.getString(locale, "result_message_same_view_bounds"), actionString,
            bounds.toShortString(), metadata.getInt(KEY_CONFLICTING_VIEW_COUNT));

      // Legacy
      case RESULT_ID_VIEW_BOUNDS:
        return String.format(locale,
            StringManager.getString(locale, "result_message_view_bounds"), actionString,
            bounds.toShortString());
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getShortMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    // For each of the following result IDs, metadata will have been set on the result.
    checkNotNull(metadata);
    String actionString = getShortActionString(
        locale,
        metadata.getBoolean(KEY_CONFLICTS_BECAUSE_CLICKABLE),
        metadata.getBoolean(KEY_CONFLICTS_BECAUSE_LONG_CLICKABLE));
    switch(resultId) {
      case RESULT_ID_SAME_BOUNDS:
      case RESULT_ID_VIEW_BOUNDS:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_brief_same_view_bounds"),
            actionString);
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_duplicate_clickable_bounds");
  }

  /**
   * @param allViews Set of views to index by their speakable text
   * @return map from speakable text to all views with that speakable text
   */
  private Map<ViewLocationActionHolder, List<ViewHierarchyElement>> getLocationActionToViewMap(
      Collection<? extends ViewHierarchyElement> allViews) {
    Map<ViewLocationActionHolder, List<ViewHierarchyElement>> locationActionToViewMap =
        new HashMap<>();

    for (ViewHierarchyElement view : allViews) {
      if (!Boolean.TRUE.equals(view.isVisibleToUser())) {
        continue;
      }

      boolean clickable = view.isClickable();
      boolean longClickable = view.isLongClickable();
      Rect bounds = view.getBoundsInScreen();
      if (view.isImportantForAccessibility() && (clickable || longClickable)) {
        ViewLocationActionHolder viewLocationActionHolder =
            new ViewLocationActionHolder(bounds, clickable, longClickable);
        if (!locationActionToViewMap.containsKey(viewLocationActionHolder)) {
          locationActionToViewMap.put(viewLocationActionHolder,
              new ArrayList<ViewHierarchyElement>());
        }
        locationActionToViewMap.get(viewLocationActionHolder).add(view);
      }
    }
    return locationActionToViewMap;
  }

  private static String getActionString(Locale locale, boolean clickable, boolean longClickable) {
    if (clickable && longClickable) {
      return StringManager.getString(locale, "clickable_and_long_clickable");
    } else if (clickable) {
      return StringManager.getString(locale, "clickable");
    } else if (longClickable) {
      return StringManager.getString(locale, "long_clickable");
    } else {
      // Consider getShortActionString if making updates to logic
      return "";
    }
  }

  private static String getShortActionString(
      Locale locale, boolean clickable, boolean longClickable) {
    if (clickable && longClickable) {
      return StringManager.getString(locale, "actionable");
    }
    return getActionString(locale, clickable, longClickable);
  }

  private static void setBoundsInMetadata(Rect rect, ResultMetadata metadata) {
    metadata.putInt(KEY_CONFLICTING_LOCATION_LEFT, rect.getLeft());
    metadata.putInt(KEY_CONFLICTING_LOCATION_TOP, rect.getTop());
    metadata.putInt(KEY_CONFLICTING_LOCATION_RIGHT, rect.getRight());
    metadata.putInt(KEY_CONFLICTING_LOCATION_BOTTOM, rect.getBottom());
  }

  private static @Nullable Rect getBoundsFromMetadata(@Nullable ResultMetadata metadata) {
    if ((metadata != null)
        && metadata.containsKey(KEY_CONFLICTING_LOCATION_LEFT)
        && metadata.containsKey(KEY_CONFLICTING_LOCATION_TOP)
        && metadata.containsKey(KEY_CONFLICTING_LOCATION_RIGHT)
        && metadata.containsKey(KEY_CONFLICTING_LOCATION_BOTTOM)) {
      return new Rect(
          metadata.getInt(KEY_CONFLICTING_LOCATION_LEFT),
          metadata.getInt(KEY_CONFLICTING_LOCATION_TOP),
          metadata.getInt(KEY_CONFLICTING_LOCATION_RIGHT),
          metadata.getInt(KEY_CONFLICTING_LOCATION_BOTTOM));
    }
    return null;
  }

  private static class ViewLocationActionHolder {
    private final Rect location;
    private final boolean clickable;
    private final boolean longClickable;

    public ViewLocationActionHolder(Rect location, boolean clickable, boolean longClickable) {
      this.location = location;
      this.clickable = clickable;
      this.longClickable = longClickable;
    }

    @Override
    public int hashCode() {
      // We explicitly ignore the action mask when computing the hash so equals() can compare
      // individual masked actions.
      return location.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) {
        return true;
      }

      if (!(obj instanceof ViewLocationActionHolder)) {
        return false;
      }

      ViewLocationActionHolder other = (ViewLocationActionHolder) obj;
      if (!location.equals(other.location)) {
        return false;
      }

      // Consider any shared action as equivalent.
      return (clickable && other.clickable) || (longClickable && other.longClickable);
    }
  }
}

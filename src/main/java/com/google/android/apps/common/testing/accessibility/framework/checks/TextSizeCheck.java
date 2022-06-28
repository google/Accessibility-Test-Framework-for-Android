package com.google.android.apps.common.testing.accessibility.framework.checks;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.lang.Math.min;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck.Category;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.HashMapResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils;
import com.google.android.apps.common.testing.accessibility.framework.replacements.LayoutParams;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableString;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Looks for text that may have visibility problems related to text scaling.
 *
 * <p>The recommended dimension type for text is scale-independent pixels (sp); for example
 * "14.5sp". This allows the user to make the text larger (or smaller) using an Android device's
 * Accessibility &gt; Font size setting. When a different dimension type is used, it may be
 * difficult for some users to read text at the specified size, especially if it is small.
 *
 * <p>If text has a dimension type of {@code sp}, then it is possible that increasing the Font size
 * will cause the text to become so large that some of it is no longer visible if the view has a
 * fixed size or is contained with another view that has a fixed size.
 *
 * <p>When a {@code TextView} is visible and has visible text, and when its textSize and
 * textSizeUnit are available, this check looks for three conditions:
 *
 * <ol>
 *   <li>The text size unit is not {@code sp},
 *   <li>The text size unit is {@code sp} and the TextView has a fixed width or height, and
 *   <li>The text size unit is {@code sp} and the TextView is within a container with a fixed width
 *       or height.
 * </ol>
 *
 * <p>This check is not perfect and can produce false positive (FP) results. One possible reason for
 * an FP result is that the size of text could be specified in a non-scaling unit but be changed
 * programmatically in response to changes in the layout. Similarly, it is also possible for
 * scalable text to be rendered in a layout with a fixed dimension, but the text may wrap, or the
 * layout or text could be change programmatically.
 *
 * <p>NOTE: The detection of fixed size containers works best if {@code android:accessibilityFlags}
 * includes {@code flagIncludeNotImportantViews}; otherwise some containers may not be considered.
 *
 * @see TextView#getTextSize()
 * @see TextView#getTextSizeUnit()
 * @see android.util.TypedValue#TYPE_DIMENSION
 */
public class TextSizeCheck extends AccessibilityHierarchyCheck {

  /** Result when the view is not visible. */
  public static final int RESULT_ID_NOT_VISIBLE = 1;
  /** Result when the view is not a {@code TextView}. */
  public static final int RESULT_ID_NOT_TEXT_VIEW = 2;
  /** Result when the {@code TextView} is empty. */
  public static final int RESULT_ID_TEXTVIEW_EMPTY = 3;
  /**
   * Result when a {@code TextView} has a text size that is small and NOT in units of {@code sp}.
   */
  public static final int RESULT_ID_SMALL_FIXED_TEXT_SIZE = 4;
  /**
   * Result when a {@code TextView} has a text size that is not small and NOT in units of {@code
   * sp}.
   */
  public static final int RESULT_ID_FIXED_TEXT_SIZE = 5;
  /** Result when the text size and/or text size unit is not available. */
  public static final int RESULT_ID_TEXT_SIZE_NOT_AVAILABLE = 6;
  /**
   * Result when a {@code TextView} appears within a {@code Toolbar} and has a text size unit of
   * {@code dip}.
   */
  public static final int RESULT_ID_TOOLBAR_TITLE_IN_DIP = 9;
  /** Result when a {@code DialogTitle} has text size unit of {@code px}. */
  public static final int RESULT_ID_DIALOG_TITLE_IN_PX = 10;
  /**
   * Result when a {@code TextView} appears within a Material {@code TabLayout} and has a text size
   * unit of {@code px}.
   */
  public static final int RESULT_ID_TAB_IN_PX = 11;
  /**
   * Result when a {@code TextView} has a text size unit of {@code px}, and is not within a {@code
   * Toolbar} or Material {@code TabLayout}.
   */
  public static final int RESULT_ID_TEXT_SIZE_IN_PX = 12;

  /**
   * Result when a {@code TextView} has a text size unit of {@code sp} and the view has a fixed
   * width.
   */
  public static final int RESULT_ID_FIXED_WIDTH_TEXT_VIEW_WITH_SCALABLE_TEXT = 31;
  /**
   * Result when a {@code TextView} has a text size unit of {@code sp} and the view has a fixed
   * height.
   */
  public static final int RESULT_ID_FIXED_HEIGHT_TEXT_VIEW_WITH_SCALABLE_TEXT = 32;
  /**
   * Result when a {@code ViewGroup} has a fixed width and height and contains a {@code TextView}
   * that has a text size unit of {@code sp}.
   */
  public static final int RESULT_ID_FIXED_SIZE_TEXT_VIEW_WITH_SCALABLE_TEXT = 33;
  /**
   * Result when a {@code ViewGroup} has a fixed width and contains a {@code TextView} that has a
   * text size unit that is {@code sp}.
   */
  public static final int RESULT_ID_FIXED_WIDTH_VIEW_GROUP_WITH_SCALABLE_TEXT = 34;
  /**
   * Result when a {@code ViewGroup} has a fixed height and contains a {@code TextView} that has a
   * text size unit that is {@code sp}.
   */
  public static final int RESULT_ID_FIXED_HEIGHT_VIEW_GROUP_WITH_SCALABLE_TEXT = 35;
  /**
   * Result when a {@code ViewGroup} fixed width and height and contains a {@code TextView} that has
   * a text size unit that is {@code sp}.
   */
  public static final int RESULT_ID_FIXED_SIZE_VIEW_GROUP_WITH_SCALABLE_TEXT = 36;

  /**
   * Emit an ERROR when text unit is NOT {@code sp} and the text size is less than this value, in
   * {@code dip}. This is an arbitrary value.
   */
  @VisibleForTesting static final float MIN_TEXT_SIZE = 16f;

  /**
   * Emit a WARNING when text unit is NOT {@code sp} and the text size is less than this value, in
   * {@code dip}, but greater than {@link #MIN_TEXT_SIZE}. This is an arbitrary value.
   */
  @VisibleForTesting static final float ADEQUATE_TEXT_SIZE = 28f;

  /**
   * A WARNING may be emitted if the fraction of a fixed dimension occupied by scalable text is at
   * least this value when the Accessibility Font scale is 1.0. This is an arbitrary value.
   */
  private static final float OCCUPIED_FRACTION_THRESHOLD = 0.70f;

  /**
   * Values of size units, from android.util.TypedValue. These avoid a dependency upon Android
   * libraries.
   */
  @VisibleForTesting static final int COMPLEX_UNIT_DIP = 1;

  @VisibleForTesting static final int COMPLEX_UNIT_IN = 4;
  @VisibleForTesting static final int COMPLEX_UNIT_MM = 5;
  @VisibleForTesting static final int COMPLEX_UNIT_PX = 0;
  @VisibleForTesting static final int COMPLEX_UNIT_PT = 3;
  @VisibleForTesting static final int COMPLEX_UNIT_SP = 2;

  private static final ImmutableMap<Integer, String> SIZE_UNIT_TO_STRING_MAP =
      ImmutableMap.<Integer, String>builder()
          .put(COMPLEX_UNIT_DIP, "dip")
          .put(COMPLEX_UNIT_IN, "in")
          .put(COMPLEX_UNIT_MM, "mm")
          .put(COMPLEX_UNIT_PT, "pt")
          .put(COMPLEX_UNIT_PX, "px")
          .buildOrThrow();

  /**
   * Result metadata key for the {@code int} indicating the text size unit.
   *
   * @see android.util.TypedValue#TYPE_DIMENSION
   */
  public static final String KEY_TEXT_SIZE_UNIT = "KEY_TEXT_SIZE_UNIT";

  /** Result metadata key for a {@code float} indicating the estimated text size in {@code dip}. */
  public static final String KEY_ESTIMATED_TEXT_SIZE_DP = "KEY_ESTIMATED_TEXT_SIZE_DP";
  /**
   * Result metadata key for a {@code boolean} which is {@code true} iff the view is determined to
   * be touching the scrollable edge of a scrollable container.
   */
  public static final String KEY_IS_AGAINST_SCROLLABLE_EDGE = "KEY_IS_AGAINST_SCROLLABLE_EDGE";

  public static final String KEY_TEXT = "KEY_TEXT";

  /**
   * Result metadata key for a {@code float} indication the fraction of the visible width occupied
   * by rendered text.
   */
  private static final String KEY_OCCUPIED_FRACTION_OF_WIDTH = "KEY_OCCUPIED_FRACTION_OF_WIDTH";

  /**
   * Result metadata key for a {@code float} indication the fraction of the visible height occupied
   * by rendered text.
   */
  private static final String KEY_OCCUPIED_FRACTION_OF_HEIGHT = "KEY_OCCUPIED_FRACTION_OF_HEIGHT";

  private static final Class<? extends AccessibilityHierarchyCheck> CHECK_CLASS =
      TextSizeCheck.class;

  private static final String DIALOG_TITLE_CLASS_NAME = "com.android.internal.widget.DialogTitle";
  private static final String MATERIAL_TAB_LAYOUT = "com.google.android.material.tabs.TabLayout";

  private static final ImmutableList<String> TOOLBAR_CLASS_NAME_LIST =
      ImmutableList.of(
          "android.support.v7.widget.Toolbar",
          "android.widget.Toolbar",
          "androidx.appcompat.widget.Toolbar");

  @Override
  protected @Nullable String getHelpTopic() {
    return "12159181";
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
    // Mapping from ViewGroup with a fixed dimension to TextViews within it that have scalable text.
    ListMultimap<ViewHierarchyElement, ViewHierarchyElement> collector = ArrayListMultimap.create();

    List<? extends ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
    for (ViewHierarchyElement view : viewsToEval) {
      if (!Boolean.TRUE.equals(view.isVisibleToUser())) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                CHECK_CLASS,
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_NOT_VISIBLE,
                null));
        continue;
      }

      if (!view.checkInstanceOf(ViewHierarchyElementUtils.TEXT_VIEW_CLASS_NAME)) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                CHECK_CLASS,
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_NOT_TEXT_VIEW,
                null));
        continue;
      }

      if (TextUtils.isEmpty(view.getText()) && TextUtils.isEmpty(view.getHintText())) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                CHECK_CLASS,
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_TEXTVIEW_EMPTY,
                null));
        continue;
      }

      Float textSize = view.getTextSize();
      Integer textSizeUnit = view.getTextSizeUnit();
      if ((textSize == null) || (textSizeUnit == null)) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                CHECK_CLASS,
                AccessibilityCheckResultType.NOT_RUN,
                null,
                RESULT_ID_TEXT_SIZE_NOT_AVAILABLE,
                null));
        continue;
      }

      AccessibilityHierarchyCheckResult result =
          (textSizeUnit == COMPLEX_UNIT_SP)
              ? checkTextViewWithScalingText(view, collector)
              : checkTextViewWithNonScalingText(hierarchy, view, textSize, textSizeUnit);
      if (result != null) {
        results.add(result);
      }
    }

    processCollectedViewGroups(collector, results);
    return results;
  }

  @Override
  public String getMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    String generated = generateMessageForResultId(locale, resultId, metadata);
    if (generated != null) {
      return generated;
    }

    StringBuilder builder = new StringBuilder(getBaseMessageForResultData(locale, resultId));
    appendMetadataStringsToMessageIfNeeded(locale, metadata, builder);
    return builder.toString();
  }

  private String getBaseMessageForResultData(Locale locale, int resultId) {
    switch (resultId) {
      case RESULT_ID_FIXED_TEXT_SIZE:
      case RESULT_ID_TEXT_SIZE_IN_PX:
        return StringManager.getString(locale, "result_message_fixed_text_size");
      case RESULT_ID_SMALL_FIXED_TEXT_SIZE:
        return StringManager.getString(locale, "result_message_small_fixed_text_size");
      case RESULT_ID_FIXED_WIDTH_TEXT_VIEW_WITH_SCALABLE_TEXT:
      case RESULT_ID_FIXED_HEIGHT_TEXT_VIEW_WITH_SCALABLE_TEXT:
      case RESULT_ID_FIXED_SIZE_TEXT_VIEW_WITH_SCALABLE_TEXT:
      case RESULT_ID_FIXED_WIDTH_VIEW_GROUP_WITH_SCALABLE_TEXT:
      case RESULT_ID_FIXED_HEIGHT_VIEW_GROUP_WITH_SCALABLE_TEXT:
      case RESULT_ID_FIXED_SIZE_VIEW_GROUP_WITH_SCALABLE_TEXT:
        return StringManager.getString(
            locale, "result_message_fixed_size_text_view_with_scaled_text");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getShortMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    String generated = generateMessageForResultId(locale, resultId, metadata);
    if (generated != null) {
      return generated;
    }

    switch (resultId) {
      case RESULT_ID_FIXED_TEXT_SIZE:
      case RESULT_ID_SMALL_FIXED_TEXT_SIZE:
      case RESULT_ID_TEXT_SIZE_IN_PX:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_brief_text_size_unit"),
            unitToString(checkNotNull(metadata).getInt(KEY_TEXT_SIZE_UNIT)));
      case RESULT_ID_FIXED_WIDTH_TEXT_VIEW_WITH_SCALABLE_TEXT:
        return StringManager.getString(
            locale, "result_message_brief_fixed_width_text_view_with_scaled_text");
      case RESULT_ID_FIXED_HEIGHT_TEXT_VIEW_WITH_SCALABLE_TEXT:
        return StringManager.getString(
            locale, "result_message_brief_fixed_height_text_view_with_scaled_text");
      case RESULT_ID_FIXED_SIZE_TEXT_VIEW_WITH_SCALABLE_TEXT:
        return StringManager.getString(
            locale, "result_message_brief_fixed_size_text_view_with_scaled_text");
      case RESULT_ID_FIXED_WIDTH_VIEW_GROUP_WITH_SCALABLE_TEXT:
        return StringManager.getString(
            locale, "result_message_brief_fixed_width_view_group_with_scaled_text");
      case RESULT_ID_FIXED_HEIGHT_VIEW_GROUP_WITH_SCALABLE_TEXT:
        return StringManager.getString(
            locale, "result_message_brief_fixed_height_view_group_with_scaled_text");
      case RESULT_ID_FIXED_SIZE_VIEW_GROUP_WITH_SCALABLE_TEXT:
        return StringManager.getString(
            locale, "result_message_brief_fixed_size_view_group_with_scaled_text");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_text_size");
  }

  /**
   * Appends messages for {@link #KEY_IS_AGAINST_SCROLLABLE_EDGE} to the provided {@code builder} if
   * the relevant key is set in the given {@code resultMetadata}.
   *
   * @param builder the {@link StringBuilder} to which result messages should be appended
   */
  private static void appendMetadataStringsToMessageIfNeeded(
      Locale locale, @Nullable ResultMetadata metadata, StringBuilder builder) {
    if (metadata == null) {
      return;
    }

    if (metadata.getBoolean(KEY_IS_AGAINST_SCROLLABLE_EDGE, false)) {
      builder
          .append(' ')
          .append(
              StringManager.getString(locale, "result_message_addendum_against_scrollable_edge"));
    }
  }

  private static @Nullable String generateMessageForResultId(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    switch (resultId) {
      case RESULT_ID_TEXT_SIZE_NOT_AVAILABLE:
        return StringManager.getString(locale, "result_message_no_text_size_unit");
      case RESULT_ID_NOT_VISIBLE:
        return StringManager.getString(locale, "result_message_not_visible");
      case RESULT_ID_NOT_TEXT_VIEW:
        return StringManager.getString(locale, "result_message_not_text_view");
      case RESULT_ID_TEXTVIEW_EMPTY:
        return StringManager.getString(locale, "result_message_textview_empty");
      case RESULT_ID_TOOLBAR_TITLE_IN_DIP:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_item_type_with_text_size_unit"),
            "Toolbar",
            unitToString(checkNotNull(metadata).getInt(KEY_TEXT_SIZE_UNIT)));
      case RESULT_ID_DIALOG_TITLE_IN_PX:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_item_type_with_text_size_unit"),
            "DialogTitle",
            unitToString(checkNotNull(metadata).getInt(KEY_TEXT_SIZE_UNIT)));
      case RESULT_ID_TAB_IN_PX:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_item_type_with_text_size_unit"),
            "Tab",
            unitToString(checkNotNull(metadata).getInt(KEY_TEXT_SIZE_UNIT)));
      default:
        return null;
    }
  }

  /**
   * For a TextView with scaled textSize, return a WARNING or INFO if the TextView has a fixed width
   * or fixed height. If it does not, then see if it is contained within a ViewGroup with a fixed
   * width or fixed height, and if so, add the ViewGroup and TextView together into collector.
   */
  private static @Nullable AccessibilityHierarchyCheckResult checkTextViewWithScalingText(
      ViewHierarchyElement textView,
      ListMultimap<ViewHierarchyElement, ViewHierarchyElement> collector) {

    LayoutParams layoutParams = textView.getLayoutParams();

    if ((layoutParams != null)
        && (isFixed(layoutParams.getWidth()) || isFixed(layoutParams.getHeight()))) {

      ResultMetadata metadata = new HashMapResultMetadata();
      if (textView.isAgainstScrollableEdge()) {
        metadata.putBoolean(KEY_IS_AGAINST_SCROLLABLE_EDGE, true);
      }
      OccupiedFractions occupiedFractions =
          addOccupiedFractions(
              metadata, textView.getBoundsInScreen(), union(textView.getTextCharacterLocations()));

      Float fontScale =
          textView.getWindow().getAccessibilityHierarchy().getDeviceState().getFontScale();
      if (fontScale != null) {
        metadata.putFloat("FONT_SCALE", fontScale);
      }
      float threshold = OCCUPIED_FRACTION_THRESHOLD * ((fontScale != null) ? fontScale : 1.0f);

      // The result_id indicates whether the TextView has a fixed width, fixed height or both.
      int resultId =
          isFixed(layoutParams.getWidth())
              ? (isFixed(layoutParams.getHeight())
                  ? RESULT_ID_FIXED_SIZE_TEXT_VIEW_WITH_SCALABLE_TEXT
                  : RESULT_ID_FIXED_WIDTH_TEXT_VIEW_WITH_SCALABLE_TEXT)
              : RESULT_ID_FIXED_HEIGHT_TEXT_VIEW_WITH_SCALABLE_TEXT;
      // The result_type will be WARNING if character locations are available and the characters
      // occupy at least the 'threshold' of a fixed dimension and the TextView is not against a
      // scrolling edge. Otherwise it will be INFO.
      AccessibilityCheckResultType resultType =
          (!textView.isAgainstScrollableEdge()
                  && (((occupiedFractions.ofWidth >= threshold) && isFixed(layoutParams.getWidth()))
                      || ((occupiedFractions.ofHeight >= threshold)
                          && isFixed(layoutParams.getHeight()))))
              ? AccessibilityCheckResultType.WARNING
              : AccessibilityCheckResultType.INFO;
      return new AccessibilityHierarchyCheckResult(
          CHECK_CLASS, resultType, textView, resultId, metadata);
    }

    checkViewGroupWithTextViewWithScalingText(textView.getParentView(), textView, collector);
    return null;
  }

  /**
   * Check to see if the TextView with scaled textSize is contained within a ViewGroup with a fixed
   * width or fixed height. If it is, put the ViewGroup and the TextView into collector together.
   */
  private static void checkViewGroupWithTextViewWithScalingText(
      @Nullable ViewHierarchyElement viewGroup,
      ViewHierarchyElement textView,
      ListMultimap<ViewHierarchyElement, ViewHierarchyElement> collector) {
    if (viewGroup == null) {
      return;
    }

    // This checks whether a scroll accessibility action is currently available. It cannot determine
    // whether the container will become scrollable if its content becomes too large to display.
    if (Boolean.TRUE.equals(viewGroup.canScrollForward())
        || Boolean.TRUE.equals(viewGroup.canScrollBackward())) {
      return;
    }

    LayoutParams layoutParams = viewGroup.getLayoutParams();
    if ((layoutParams != null)
        && (isFixed(layoutParams.getWidth()) || isFixed(layoutParams.getHeight()))) {
      collector.put(viewGroup, textView);
      return;
    }
    checkViewGroupWithTextViewWithScalingText(viewGroup.getParentView(), textView, collector);
  }

  /** Add one AccessibilityHierarchyCheckResult to results for each ViewGroup in collector. */
  private static void processCollectedViewGroups(
      ListMultimap<ViewHierarchyElement, ViewHierarchyElement> collector,
      List<AccessibilityHierarchyCheckResult> results) {
    for (Map.Entry<ViewHierarchyElement, Collection<ViewHierarchyElement>> entry :
        collector.asMap().entrySet()) {
      results.add(
          createViewGroupResult(entry.getKey(), (List<ViewHierarchyElement>) entry.getValue()));
    }
  }

  /**
   * Creates a WARNING or INFO result for a ViewGroup with a fixed width or fixed height containing
   * one or more TextViews with scalable textSize.
   */
  private static AccessibilityHierarchyCheckResult createViewGroupResult(
      ViewHierarchyElement viewGroup, List<ViewHierarchyElement> scalableTextViews) {
    ResultMetadata metadata = new HashMapResultMetadata();

    SpannableString text = scalableTextViews.get(0).getText();

    metadata.putString(KEY_TEXT, ((text == null) ? "" : text.toString()));
    if (viewGroup.isAgainstScrollableEdge()) {
      metadata.putBoolean(KEY_IS_AGAINST_SCROLLABLE_EDGE, true);
    }

    Rect unionOfUnions =
        union(Lists.transform(scalableTextViews, v -> union(v.getTextCharacterLocations())));
    OccupiedFractions occupiedFractions =
        addOccupiedFractions(metadata, viewGroup.getBoundsInScreen(), unionOfUnions);

    Float fontScale =
        viewGroup.getWindow().getAccessibilityHierarchy().getDeviceState().getFontScale();
    if (fontScale != null) {
      metadata.putFloat("FONT_SCALE", fontScale);
    }
    float threshold = OCCUPIED_FRACTION_THRESHOLD * ((fontScale != null) ? fontScale : 1.0f);

    // The result_id indicates whether the ViewGroup has a fixed width, fixed height or both.
    LayoutParams layoutParams = checkNotNull(viewGroup.getLayoutParams());
    int resultId =
        isFixed(layoutParams.getWidth())
            ? (isFixed(layoutParams.getHeight())
                ? RESULT_ID_FIXED_SIZE_VIEW_GROUP_WITH_SCALABLE_TEXT
                : RESULT_ID_FIXED_WIDTH_VIEW_GROUP_WITH_SCALABLE_TEXT)
            : RESULT_ID_FIXED_HEIGHT_VIEW_GROUP_WITH_SCALABLE_TEXT;
    // The result_type will be WARNING if character locations are available and the characters
    // occupy at least the 'threshold' of a fixed dimension and the container is not against a
    // scrolling edge. Otherwise it will be INFO.
    AccessibilityCheckResultType resultType =
        (!viewGroup.isAgainstScrollableEdge()
                && (((occupiedFractions.ofWidth >= threshold) && isFixed(layoutParams.getWidth()))
                    || ((occupiedFractions.ofHeight >= threshold)
                        && isFixed(layoutParams.getHeight()))))
            ? AccessibilityCheckResultType.WARNING
            : AccessibilityCheckResultType.INFO;
    // The reported culprit element is the ViewGroup, not the TextView.

    return new AccessibilityHierarchyCheckResult(
        CHECK_CLASS, resultType, viewGroup, resultId, metadata);
  }

  private static boolean isFixed(int dimension) {
    return (dimension != LayoutParams.MATCH_PARENT)
        && (dimension != LayoutParams.WRAP_CONTENT)
        && (dimension != 0);
  }

  /** Add to metadata the portion of the visible area that is occupied by text. */
  private static OccupiedFractions addOccupiedFractions(
      ResultMetadata metadata, Rect boundsInScreen, Rect union) {
    float occupiedFractionOfWidth =
        (boundsInScreen.getWidth() > 0)
            ? ((float) union.getWidth() / boundsInScreen.getWidth())
            : 0.0f;
    float occupiedFractionOfHeight =
        (boundsInScreen.getHeight() > 0)
            ? ((float) union.getHeight() / boundsInScreen.getHeight())
            : 0.0f;
    metadata.putFloat(KEY_OCCUPIED_FRACTION_OF_WIDTH, occupiedFractionOfWidth);
    metadata.putFloat(KEY_OCCUPIED_FRACTION_OF_HEIGHT, occupiedFractionOfHeight);
    return new OccupiedFractions(occupiedFractionOfWidth, occupiedFractionOfHeight);
  }

  /**
   * Returns a {@link Rect} that encloses all of the given rectangles that are not empty. If the
   * list does not contain any non-empty rectangles, then {@link Rect#EMPTY} is returned.
   */
  private static Rect union(List<Rect> rectangles) {
    // Remove any empty Rects.
    rectangles = ImmutableList.copyOf(Iterables.filter(rectangles, Predicates.not(Rect::isEmpty)));

    if (rectangles.isEmpty()) {
      return Rect.EMPTY;
    }
    int left = rectangles.get(0).getLeft();
    int top = rectangles.get(0).getTop();
    int right = rectangles.get(0).getRight();
    int bottom = rectangles.get(0).getBottom();
    for (Rect r : rectangles.subList(1, rectangles.size())) {
      left = min(left, r.getLeft());
      top = min(top, r.getTop());
      right = max(right, r.getRight());
      bottom = max(bottom, r.getBottom());
    }
    return new Rect(left, top, right, bottom);
  }

  /**
   * For a TextView with non-scaled text, possibly return an ERROR (if the text is small) or a
   * WARNING. Returns {@code null} if the rendered size in {@code dip} is greater than or equal to
   * {@link #ADEQUATE_TEXT_SIZE}.
   *
   * <p>There are special cases for Toolbar title in {@code px}, Dialog title in {@code dip},
   * Material Tab in {@code px}. These will all return a NOT_RUN result.
   */
  private static @Nullable AccessibilityHierarchyCheckResult checkTextViewWithNonScalingText(
      AccessibilityHierarchy hierarchy,
      ViewHierarchyElement view,
      float textSize,
      int textSizeUnit) {

    float textSizeDp = textSize / getDensity(hierarchy);
    if (textSizeDp >= ADEQUATE_TEXT_SIZE) {
      return null;
    }

    boolean isSmallText = textSizeDp < MIN_TEXT_SIZE;
    boolean isToolbarTitleInDip = (textSizeUnit == COMPLEX_UNIT_DIP) && isToolbarTitle(view);
    boolean isDialogTitleInPx = (textSizeUnit == COMPLEX_UNIT_PX) && isDialogTitle(view);
    boolean inMaterialTabLayoutInPx =
        (textSizeUnit == COMPLEX_UNIT_PX) && inMaterialTabLayout(view);
    ResultMetadata metadata = new HashMapResultMetadata();
    metadata.putInt(KEY_TEXT_SIZE_UNIT, textSizeUnit);
    metadata.putFloat(KEY_ESTIMATED_TEXT_SIZE_DP, textSizeDp);
    return new AccessibilityHierarchyCheckResult(
        CHECK_CLASS,
        getResultTypeForFixedTextSize(
            isSmallText,
            isToolbarTitleInDip,
            isDialogTitleInPx,
            inMaterialTabLayoutInPx,
            (textSizeUnit == COMPLEX_UNIT_PX)),
        view,
        getResultIdForFixedTextSize(
            isSmallText,
            isToolbarTitleInDip,
            isDialogTitleInPx,
            inMaterialTabLayoutInPx,
            (textSizeUnit == COMPLEX_UNIT_PX)),
        metadata);
  }

  private static AccessibilityCheckResultType getResultTypeForFixedTextSize(
      boolean isSmallText,
      boolean isToolbarTitleInDip,
      boolean isDialogTitleInPx,
      boolean inMaterialTabLayoutInPx,
      boolean textSizeInPx) {
    if (isToolbarTitleInDip || isDialogTitleInPx || inMaterialTabLayoutInPx || textSizeInPx) {
      return AccessibilityCheckResultType.NOT_RUN;
    }
    return isSmallText ? AccessibilityCheckResultType.ERROR : AccessibilityCheckResultType.WARNING;
  }

  private static int getResultIdForFixedTextSize(
      boolean isSmallText,
      boolean isToolbarTitleInDip,
      boolean isDialogTitleInPx,
      boolean inMaterialTabLayoutInPx,
      boolean textSizeInPx) {
    if (isToolbarTitleInDip) {
      return RESULT_ID_TOOLBAR_TITLE_IN_DIP;
    } else if (isDialogTitleInPx) {
      return RESULT_ID_DIALOG_TITLE_IN_PX;
    } else if (inMaterialTabLayoutInPx) {
      return RESULT_ID_TAB_IN_PX;
    } else if (textSizeInPx) {
      return RESULT_ID_TEXT_SIZE_IN_PX;
    } else if (isSmallText) {
      return RESULT_ID_SMALL_FIXED_TEXT_SIZE;
    } else {
      return RESULT_ID_FIXED_TEXT_SIZE;
    }
  }

  private static boolean isToolbarTitle(ViewHierarchyElement view) {
    ViewHierarchyElement parent = view.getParentView();
    return (parent != null) && parent.checkInstanceOfAny(TOOLBAR_CLASS_NAME_LIST);
  }

  private static boolean isDialogTitle(ViewHierarchyElement view) {
    return view.checkInstanceOf(DIALOG_TITLE_CLASS_NAME);
  }

  private static boolean inMaterialTabLayout(ViewHierarchyElement view) {
    ViewHierarchyElement parent = view.getParentView();
    return view.checkInstanceOf(MATERIAL_TAB_LAYOUT)
        || ((parent != null) && inMaterialTabLayout(parent));
  }

  private static String unitToString(int unit) {
    return SIZE_UNIT_TO_STRING_MAP.getOrDefault(unit, "?");
  }

  private static float getDensity(AccessibilityHierarchy hierarchy) {
    return checkNotNull(hierarchy.getDeviceState().getDefaultDisplayInfo().getRealMetrics())
        .getScaledDensity();
  }

  private static final class OccupiedFractions {
    final float ofWidth;
    final float ofHeight;

    OccupiedFractions(float ofWidth, float ofHeight) {
      this.ofWidth = ofWidth;
      this.ofHeight = ofHeight;
    }
  }
}

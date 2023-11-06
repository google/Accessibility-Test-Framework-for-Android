package com.google.android.apps.common.testing.accessibility.framework.uielement;

import static android.view.accessibility.AccessibilityNodeInfo.EXTRA_DATA_RENDERING_INFO_KEY;
import static android.view.accessibility.AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH;
import static android.view.accessibility.AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX;
import static android.view.accessibility.AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.min;

import android.graphics.RectF;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Size;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Extracts extra data from the View associated with an {@link AccessibilityNodeInfo}. */
class AccessibilityNodeInfoExtraDataExtractor {

  /**
   * See
   * https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/package-summary#(androidx.compose.ui.semantics.SemanticsPropertyReceiver).testTag()
   */
  static final String EXTRA_DATA_TEST_TAG = "androidx.compose.ui.semantics.testTag";

  private final boolean obtainCharacterLocations;
  @VisibleForTesting final boolean obtainRenderingInfo;

  /** The maximum allowed length of the requested text location data. */
  private final @Nullable Integer characterLocationArgMaxLength;

  /**
   * @param obtainCharacterLocations whether character locations are desired
   * @param obtainRenderingInfo whether rendering info is desired
   * @param characterLocationArgMaxLength The maximum allowed length of the requested text location
   *     data
   */
  AccessibilityNodeInfoExtraDataExtractor(
      boolean obtainCharacterLocations,
      boolean obtainRenderingInfo,
      @Nullable Integer characterLocationArgMaxLength) {
    this.obtainCharacterLocations = obtainCharacterLocations;
    this.obtainRenderingInfo = obtainRenderingInfo;
    this.characterLocationArgMaxLength = characterLocationArgMaxLength;
  }

  ExtraData getExtraData(AccessibilityNodeInfo fromInfo) {
    ExtraData extraData = new ExtraData();

    if (obtainCharacterLocations && (VERSION.SDK_INT >= VERSION_CODES.O)) {
      fetchTextCharacterLocations(fromInfo, extraData, characterLocationArgMaxLength);
    }

    if (obtainRenderingInfo && (VERSION.SDK_INT >= VERSION_CODES.R)) {
      fetchRenderingInfo(fromInfo, extraData);
    }

    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      fetchTestTag(fromInfo, extraData);
    }

    return extraData;
  }

  /**
   * Retrieves text character locations for the {@code TextView}'s text.
   *
   * <p>Returns an empty list if the text is {@code null} or an empty string, or if the character
   * locations are not available. Limits the size of the result if the constructor was given a
   * non-null value for characterLocationArgMaxLength.
   *
   * @param textView A {@code TextView} which may have a text
   * @return The locations of each text character in screen coordinates
   */
  ImmutableList<Rect> getTextCharacterLocations(TextView textView) {
    return getTextCharacterLocations(textView, characterLocationArgMaxLength);
  }

  @VisibleForTesting
  @RequiresApi(VERSION_CODES.O)
  ImmutableList<Rect> getTextCharacterLocations(
      TextView textView, @Nullable Integer characterLocationArgMaxLength) {
    return (obtainCharacterLocations && (VERSION.SDK_INT >= VERSION_CODES.O))
        ? getTextCharacterLocationsAux(textView, characterLocationArgMaxLength)
        : ImmutableList.of();
  }

  @RequiresApi(VERSION_CODES.O)
  private static ImmutableList<Rect> getTextCharacterLocationsAux(
      TextView textView, @Nullable Integer maxCharacterLocationLength) {
    CharSequence text = textView.getText();
    if (TextUtils.isEmpty(text) || (textView.getLayout() == null)) {
      return ImmutableList.of();
    }

    AccessibilityNodeInfo nodeInfo = AccessibilityNodeInfo.obtain();
    Bundle args = createTextCharacterLocationsRequestBundle(text, maxCharacterLocationLength);
    textView.addExtraDataToAccessibilityNodeInfo(
        nodeInfo, EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY, args);
    ImmutableList<Rect> locations = parseCharacterLocationsFromExtras(nodeInfo.getExtras());
    return (locations == null) ? ImmutableList.of() : locations;
  }

  /**
   * Retrieves the rendering info (text size, text size unit and layout size) and stores it in
   * {@code extraData}.
   *
   * <p>The results will be {@code null} if the info is not available.
   *
   * @param fromInfo The node of interest
   * @param extraData destination for rendering info
   */
  @RequiresApi(VERSION_CODES.R)
  private static void fetchRenderingInfo(AccessibilityNodeInfo fromInfo, ExtraData extraData) {
    if (safeRefreshWithExtraData(fromInfo, EXTRA_DATA_RENDERING_INFO_KEY, new Bundle())) {
        AccessibilityNodeInfo.ExtraRenderingInfo extraRenderingInfo =
            fromInfo.getExtraRenderingInfo();
        if (extraRenderingInfo != null) {
          float size = extraRenderingInfo.getTextSizeInPx();
          if (size >= 0) {
            extraData.setTextSize(size);
          }
          int textSizeUnit = extraRenderingInfo.getTextSizeUnit();
          if (textSizeUnit >= 0) {
            extraData.setTextSizeUnit(textSizeUnit);
          }
          Size layoutSize = extraRenderingInfo.getLayoutSize();
          if (layoutSize != null) {
            extraData.setLayoutSize(layoutSize);
          }
        }
    }
  }

  /**
   * Retrieves the locations of each text character in screen coordinates and stores them in {@code
   * extraData}.
   *
   * <p>The result will be an empty list if the text is {@code null} or an empty string, and will be
   * {@code null} if the character locations are not available.
   *
   * @param fromInfo The {@link AccessibilityNodeInfo} which may have a text
   * @param extraData destination for locations
   */
  @RequiresApi(VERSION_CODES.O)
  private static void fetchTextCharacterLocations(
      AccessibilityNodeInfo fromInfo,
      ExtraData extraData,
      @Nullable Integer maxCharacterLocationLength) {
    CharSequence text = fromInfo.getText();
    if (TextUtils.isEmpty(text)) {
      extraData.setTextCharacterLocations(ImmutableList.of());
    } else {
      Bundle args = createTextCharacterLocationsRequestBundle(text, maxCharacterLocationLength);
      if (safeRefreshWithExtraData(fromInfo, EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY, args)) {
        ImmutableList<Rect> locations = parseCharacterLocationsFromExtras(fromInfo.getExtras());
        if (locations != null) {
          extraData.setTextCharacterLocations(locations);
        }
      }
    }
  }

  @RequiresApi(VERSION_CODES.O)
  private static @Nullable ImmutableList<Rect> parseCharacterLocationsFromExtras(Bundle extras) {
    Parcelable[] data = extras.getParcelableArray(EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY);
    if (data == null) {
      return null;
    }

    ImmutableList.Builder<Rect> charLocations = ImmutableList.builder();
    for (Parcelable item : data) {
      if (item instanceof RectF) {
        // Rounds "out" the rectangle by choosing the floor of top and left, and the ceiling of
        // right and bottom.
        RectF rectF = (RectF) item;
        charLocations.add(
            new Rect(
                (int) floor(rectF.left),
                (int) floor(rectF.top),
                (int) ceil(rectF.right),
                (int) ceil(rectF.bottom)));
      }
    }
    return charLocations.build();
  }

  @RequiresApi(VERSION_CODES.O)
  private static Bundle createTextCharacterLocationsRequestBundle(
      CharSequence text, @Nullable Integer maxCharacterLocationLength) {
    Bundle args = new Bundle();
    args.putInt(EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX, 0);
    args.putInt(
        EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH,
        (maxCharacterLocationLength == null)
            ? text.length()
            : min(maxCharacterLocationLength, text.length()));
    return args;
  }

  /**
   * Retrieves the Compose testTag and stores it in {@code extraData}.
   *
   * <p>The result will be {@code null} if a testTag has not been set.
   *
   * @param fromInfo The {@link AccessibilityNodeInfo} which may have a test tag
   * @param extraData destination for the testTag
   */
  @RequiresApi(VERSION_CODES.O)
  private static void fetchTestTag(AccessibilityNodeInfo fromInfo, ExtraData extraData) {
    if (fromInfo.getAvailableExtraData().contains(EXTRA_DATA_TEST_TAG)
        && safeRefreshWithExtraData(fromInfo, EXTRA_DATA_TEST_TAG, new Bundle())) {
        CharSequence testTag = fromInfo.getExtras().getCharSequence(EXTRA_DATA_TEST_TAG);
        if (testTag != null) {
          extraData.setTestTag(testTag);
        }
      }
  }

  /**
   * Calls AccessibilityNodeInfo#refreshWithExtraData(String, Bundle), but handles any thrown
   * IllegalStateException if this code is being run in a Robolectric test. refreshWithExtraData
   * throws an IllegalStateException when it is called on a sealed instance.
   *
   * @return {@code true} iff the refresh succeeded.
   */
  private static boolean safeRefreshWithExtraData(
      AccessibilityNodeInfo fromInfo, String extraDataKey, Bundle args) {
    try {
      return fromInfo.refreshWithExtraData(extraDataKey, args);
    } catch (IllegalStateException e) {
      if (isRobolectric()) {
        return false;
      }
      throw e;
    }
  }

  private static boolean isRobolectric() {
    return "robolectric".equals(Build.FINGERPRINT);
  }

  static class ExtraData {
    private @Nullable Float textSize = null;
    private @Nullable Integer textSizeUnit = null;
    private @Nullable Size layoutSize = null;
    private @Nullable ImmutableList<Rect> textCharacterLocations = null;
    private @Nullable CharSequence testTag = null;

    ExtraData() {}

    /**
     * Gets the text size if the node is a {@code TextView}.
     *
     * <p>Returns {@code null} if {@code obtainRenderingInfo} was {@code false}, or if the text size
     * is not available.
     *
     * @return the text size in px
     */
    @Nullable Float getTextSize() {
      return textSize;
    }

    @CanIgnoreReturnValue
    @VisibleForTesting
    ExtraData setTextSize(Float textSize) {
      this.textSize = textSize;
      return this;
    }

    /**
     * Gets the text size unit defined by the developer if {@code obtainRenderingInfo} is {@code
     * true}.
     *
     * <p>Returns {@code null} if {@code obtainRenderingInfo} was {@code false}, or if the text size
     * unit is not available.
     *
     * @return the dimension type of the text size unit originally defined.
     * @see android.util.TypedValue#TYPE_DIMENSION
     */
    @Nullable Integer getTextSizeUnit() {
      return textSizeUnit;
    }

    @CanIgnoreReturnValue
    @VisibleForTesting
    ExtraData setTextSizeUnit(Integer textSizeUnit) {
      this.textSizeUnit = textSizeUnit;
      return this;
    }

    /**
     * Gets the size object containing the height and the width of {@link
     * android.view.ViewGroup.LayoutParams} if the node is a {@link ViewGroup} or a {@link
     * TextView}, or null otherwise.
     *
     * <p>Returns {@code null} if {@code obtainRenderingInfo} was {@code false}, or if the text size
     * unit is not available.
     *
     * @see android.view.accessibility.AccessibilityNodeInfo.ExtraRenderingInfo#getLayoutSize
     */
    @Nullable Size getLayoutSize() {
      return layoutSize;
    }

    @CanIgnoreReturnValue
    @VisibleForTesting
    ExtraData setLayoutSize(Size layoutSize) {
      this.layoutSize = layoutSize;
      return this;
    }

    /**
     * Retrieves text character locations if {@code obtainCharacterLocations} is {@code true}.
     *
     * <p>Returns an empty list if the text is {@code null} or an empty string. Returns {@code null}
     * if {@code obtainCharacterLocations} was {@code false}, or if the character locations are not
     * available.
     *
     * @return The locations of each text character in screen coordinates
     */
    @Nullable ImmutableList<Rect> getTextCharacterLocations() {
      return textCharacterLocations;
    }

    @CanIgnoreReturnValue
    @VisibleForTesting
    ExtraData setTextCharacterLocations(ImmutableList<Rect> textCharacterLocations) {
      this.textCharacterLocations = textCharacterLocations;
      return this;
    }

    /**
     * Gets the Compose testTag if one has been set.
     *
     * @return The Compose testTag or {@code null} if one has not been set
     */
    @Nullable CharSequence getTestTag() {
      return testTag;
    }

    @CanIgnoreReturnValue
    @VisibleForTesting
    ExtraData setTestTag(CharSequence testTag) {
      this.testTag = testTag;
      return this;
    }
  }
}

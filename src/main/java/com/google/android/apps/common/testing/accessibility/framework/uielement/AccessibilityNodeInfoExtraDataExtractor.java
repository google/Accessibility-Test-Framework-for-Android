package com.google.android.apps.common.testing.accessibility.framework.uielement;

import static android.view.accessibility.AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH;
import static android.view.accessibility.AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;

import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.annotation.RequiresApi;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Extracting extra rendering data from an {@link AccessibilityNodeInfo}. */
class AccessibilityNodeInfoExtraDataExtractor {

  private static final boolean AT_26 = (VERSION.SDK_INT >= VERSION_CODES.O);

  private final boolean obtainCharacterLocations;

  private final boolean obtainRenderingInfo;

  /**
   * @param obtainCharacterLocations whether character locations are desired
   * @param obtainRenderingInfo whether rendering info is desired
   */
  AccessibilityNodeInfoExtraDataExtractor(
      boolean obtainCharacterLocations, boolean obtainRenderingInfo) {
    this.obtainCharacterLocations = obtainCharacterLocations;
    this.obtainRenderingInfo = obtainRenderingInfo;
  }

  /** Obtains the extra rendering data that is desired and available. */
  ExtraData getExtraData(AccessibilityNodeInfo fromInfo) {
    ExtraData.Builder builder = new ExtraData.Builder();
    if (obtainCharacterLocations && AT_26) {
      builder.setTextCharacterLocations(getTextCharacterLocations(fromInfo));
    }
    return builder.build();
  }

  /**
   * Retrieves text character locations if the given {@link AccessibilityNodeInfo} has a non-empty
   * text.
   *
   * <p>Returns an empty list if The text is {@code null} or an empty string. Returns {@code null}
   * if the extra data could not be obtained.
   *
   * @param fromInfo The {@link AccessibilityNodeInfo} which may has a text
   * @return The locations of each text character in screen coordinates
   */
  @RequiresApi(VERSION_CODES.O)
  private static @Nullable ImmutableList<Rect> getTextCharacterLocations(
      AccessibilityNodeInfo fromInfo) {
    CharSequence text = fromInfo.getText();
    if (TextUtils.isEmpty(text)) {
      return ImmutableList.of();
    }

    Bundle args = createTextCharacterLocationsRequestBundle(text);
    if (fromInfo.refreshWithExtraData(
        AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY, args)) {
      Bundle extras = fromInfo.getExtras();
      Parcelable[] data =
          extras.getParcelableArray(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY);
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
    return null;
  }

  private static Bundle createTextCharacterLocationsRequestBundle(CharSequence text) {
    Bundle args = new Bundle();
    args.putInt(EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX, 0);
    args.putInt(EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH, text.length());
    return args;
  }

  /** Extra rendering data. */
  static class ExtraData {

    private final Optional<ImmutableList<Rect>> textCharacterLocations;

    private ExtraData(Optional<ImmutableList<Rect>> textCharacterLocations) {
      this.textCharacterLocations = textCharacterLocations;
    }

    /**
     * Gets the character locations of the element's text.
     *
     * <p>Returns an empty list if The text is {@code null} or an empty string. Returns {@link
     * Optional#absent} if the locations could not be obtained or were not requested.
     */
    Optional<ImmutableList<Rect>> getTextCharacterLocations() {
      return textCharacterLocations;
    }

    static Builder builder() {
      return new Builder();
    }

    static class Builder {
      Optional<ImmutableList<Rect>> textCharacterLocations = Optional.absent();

      Builder setTextCharacterLocations(@Nullable ImmutableList<Rect> textCharacterLocations) {
        this.textCharacterLocations = Optional.fromNullable(textCharacterLocations);
        return this;
      }

      ExtraData build() {
        return new ExtraData(textCharacterLocations);
      }
    }
  }
}

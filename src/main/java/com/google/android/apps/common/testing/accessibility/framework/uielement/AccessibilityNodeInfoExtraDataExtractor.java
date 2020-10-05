package com.google.android.apps.common.testing.accessibility.framework.uielement;

import static android.view.accessibility.AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH;
import static android.view.accessibility.AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;

import android.graphics.RectF;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.annotation.RequiresApi;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.common.collect.ImmutableList;

/** Extracting extra rendering data from an {@link AccessibilityNodeInfo}. */
class AccessibilityNodeInfoExtraDataExtractor {

  /**
   * Retrieves text character locations if the given {@link AccessibilityNodeInfo} has a non-empty
   * text.
   *
   * <p>Returns an empty list if The text is {@code null} or an empty string.
   *
   * @param fromInfo The {@link AccessibilityNodeInfo} which may has a text
   * @return The locations of each text character in screen coordinates
   */
  @RequiresApi(VERSION_CODES.O)
  ImmutableList<Rect> getTextCharacterLocations(AccessibilityNodeInfo fromInfo) {
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
        return ImmutableList.of();
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
    return ImmutableList.of();
  }

  private static Bundle createTextCharacterLocationsRequestBundle(CharSequence text) {
    Bundle args = new Bundle();
    args.putInt(EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX, 0);
    args.putInt(EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH, text.length());
    return args;
  }
}

/*
 * Copyright (C) 2015 Google Inc.
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
package com.google.android.apps.common.testing.accessibility.framework.uielement;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.view.WindowManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.DeviceStateProto;
import com.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.Immutable;
import java.util.List;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Representation of the state of a device at the time an {@link AccessibilityHierarchy} is
 * captured.
 *
 * <p>Display properties, such as screen resolution and pixel density, are stored within {@link
 * DisplayInfoAndroid} and as fields in the associated {@link DeviceStateProto}.
 */
@Immutable
public class DeviceStateAndroid extends DeviceState {

  /** @see WindowManager#getDefaultDisplay() */
  private final DisplayInfoAndroid defaultDisplayInfo;

  private DeviceStateAndroid(
      int sdkVersion,
      Locale locale,
      DisplayInfoAndroid defaultDisplayInfo,
      @Nullable Float fontScale) {
    super(sdkVersion, locale, fontScale);
    this.defaultDisplayInfo = defaultDisplayInfo;
  }

  /** See {@link WindowManager#getDefaultDisplay()}. */
  @Override
  public DisplayInfoAndroid getDefaultDisplayInfo() {
    return defaultDisplayInfo;
  }

  void writeToParcel(Parcel out, int flags) {
    defaultDisplayInfo.writeToParcel(out, flags);
    out.writeInt(sdkVersion);
    out.writeString(getLanguageTag());
    ParcelUtils.writeNullableFloat(out, fontScale);
  }

  @Override
  DeviceStateProto toProto() {
    DeviceStateProto.Builder builder = DeviceStateProto.newBuilder();
    builder.setSdkVersion(sdkVersion);
    builder.setDefaultDisplayInfo(defaultDisplayInfo.toProto());
    builder.setLocale(getLanguageTag());
    if (fontScale != null) {
      builder.setFontScale(fontScale);
    }
    return builder.build();
  }

  private String getLanguageTag() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return locale.toLanguageTag();
    } else {
      return getStringFromLocale(locale);
    }
  }

  private static Locale getLocaleFromLanguageTag(String languageTag) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return Locale.forLanguageTag(languageTag);
    } else {
      return getLocaleFromString(languageTag);
    }
  }

  /** Returns a new builder that can build a DeviceStateAndroid from a context. */
  static Builder newBuilder(Context context) {
    return new Builder(context);
  }

  /** Returns a new builder that can build a DeviceStateAndroid from a parcel. */
  static Builder newBuilder(Parcel fromParcel) {
    return new Builder(fromParcel);
  }

  /** Returns a new builder that can build a DeviceStateAndroid from a proto. */
  static Builder newBuilder(DeviceStateProto fromProto) {
    return new Builder(checkNotNull(fromProto));
  }

  /**
   * Attempts to produce the same result as {@link Locale#toLanguageTag} for those locales supported
   * by ATF.
   *
   * <p>For use with builds prior to LOLLIPOP, where toLanguageTag is not available.
   */
  @VisibleForTesting
  static String getStringFromLocale(Locale locale) {
    return locale.toString().replace('_', '-');
  }

  /**
   * Attempts to produce the same result as {@link Locale#forLanguageTag} for those locales
   * supported by ATF.
   *
   * <p>For use with builds prior to LOLLIPOP, where forLanguageTag is not available.
   */
  @VisibleForTesting
  static Locale getLocaleFromString(String str) {
    List<String> parts = HYPHEN_SPLITTER.splitToList(str);
    switch (parts.size()) {
      case 1:
        return new Locale(parts.get(0));
      case 2:
        return new Locale(parts.get(0), parts.get(1));
      case 3:
        return new Locale(parts.get(0), parts.get(1), parts.get(2));
      default:
        throw new IllegalArgumentException("Unsupported locale string: " + str);
    }
  }

  /**
   * A builder for {@link DeviceStateAndroid}; obtained using {@link DeviceStateAndroid#builder}.
   */
  public static class Builder {
    private final int sdkVersion;
    private final Locale locale;
    private final DisplayInfoAndroid defaultDisplayInfo;
    private final @Nullable Float fontScale;

    // dereference of possibly-null reference wm
    @SuppressWarnings("nullness:dereference.of.nullable")
    Builder(Context context) {
      WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
      defaultDisplayInfo = new DisplayInfoAndroid(wm.getDefaultDisplay());
      sdkVersion = Build.VERSION.SDK_INT;
      locale = Locale.getDefault();
      fontScale = context.getResources().getConfiguration().fontScale;
    }

    Builder(Parcel fromParcel) {
      defaultDisplayInfo = new DisplayInfoAndroid(fromParcel);
      sdkVersion = fromParcel.readInt();
      locale = getLocaleFromLanguageTag(checkNotNull(fromParcel.readString()));
      fontScale = ParcelUtils.readNullableFloat(fromParcel);
    }

    Builder(DeviceStateProto fromProto) {
      sdkVersion = fromProto.getSdkVersion();
      defaultDisplayInfo = new DisplayInfoAndroid(fromProto.getDefaultDisplayInfo());
      String languageTag = fromProto.getLocale();
      // Use the default Locale if no locale was recorded in the proto.
      // This is for backward compatibility.
      locale = languageTag.isEmpty() ? Locale.getDefault() : getLocaleFromLanguageTag(languageTag);
      fontScale = fromProto.hasFontScale() ? fromProto.getFontScale() : null;
    }

    public DeviceStateAndroid build() {
      return new DeviceStateAndroid(sdkVersion, locale, defaultDisplayInfo, fontScale);
    }
  }
}

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
import androidx.annotation.VisibleForTesting;
import android.view.WindowManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.DeviceStateProto;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Locale;

/**
 * Representation of the state of a device at the time an {@link AccessibilityHierarchy} is
 * captured.
 * <p>
 * Display properties, such as screen resolution and pixel density, are stored within
 * {@link DisplayInfo} and as fields in the associated {@link DeviceStateProto}.
 */
public class DeviceState {

  private static final Splitter HYPHEN_SPLITTER = Splitter.on('-');

  /** @see WindowManager#getDefaultDisplay() */
  private final DisplayInfo defaultDisplayInfo;

  /** @see Build.VERSION#SDK_INT */
  private final int sdkVersion;

  private final Locale locale;

  /**
   * Creates a record of the device state at the time of construction.
   *
   * @param context The {@link Context} used to resolve properties of device state.
   */
  public DeviceState(Context context) {
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    defaultDisplayInfo = new DisplayInfo(wm.getDefaultDisplay());
    sdkVersion = Build.VERSION.SDK_INT;
    locale = Locale.getDefault();
  }

  DeviceState(Parcel fromParcel) {
    defaultDisplayInfo = new DisplayInfo(fromParcel);
    sdkVersion = fromParcel.readInt();
    locale = getLocaleFromLanguageTag(checkNotNull(fromParcel.readString()));
  }

  DeviceState(DeviceStateProto fromProto) {
    sdkVersion = fromProto.getSdkVersion();
    defaultDisplayInfo = new DisplayInfo(fromProto.getDefaultDisplayInfo());
    String languageTag = fromProto.getLocale();
    // Use the default Locale if no locale was recorded in the proto.
    // This is for backward compatibility.
    locale = languageTag.isEmpty() ? Locale.getDefault() : getLocaleFromLanguageTag(languageTag);
  }

  /**
   * @see WindowManager#getDefaultDisplay()
   */
  public DisplayInfo getDefaultDisplayInfo() {
    return defaultDisplayInfo;
  }

  /** @see Build.VERSION#SDK_INT */
  public int getSdkVersion() {
    return sdkVersion;
  }

  /** Gets the locale at the time the device state was captured. */
  public Locale getLocale() {
    return locale;
  }

  void writeToParcel(Parcel out, int flags) {
    defaultDisplayInfo.writeToParcel(out, flags);
    out.writeInt(sdkVersion);
    out.writeString(getLanguageTag());
  }

  DeviceStateProto toProto() {
    DeviceStateProto.Builder builder = DeviceStateProto.newBuilder();
    builder.setSdkVersion(sdkVersion);
    builder.setDefaultDisplayInfo(defaultDisplayInfo.toProto());
    builder.setLocale(getLanguageTag());
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

  /**
   * Attempts to produce the same result as {@link Locale#toLanguageTag} for those locales
   * supported by ATF.
   * <p>For use with builds prior to LOLLIPOP, where toLanguageTag is not available.
   */
  @VisibleForTesting
  static String getStringFromLocale(Locale locale) {
    return locale.toString().replace('_', '-');
  }

  /**
   * Attempts to produce the same result as {@link Locale#forLanguageTag} for those locales
   * supported by ATF.
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
}

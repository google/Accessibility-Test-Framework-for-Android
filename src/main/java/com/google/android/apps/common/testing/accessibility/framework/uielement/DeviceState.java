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

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.DeviceStateProto;
import com.google.common.base.Splitter;
import com.google.errorprone.annotations.Immutable;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Representation of the state of a device at the time an {@link AccessibilityHierarchy} is
 * captured.
 *
 * <p>Display properties, such as screen resolution and pixel density, are stored within {@link
 * DisplayInfo} and as fields in the associated {@link DeviceStateProto}.
 */
@Immutable
public class DeviceState {

  protected static final Splitter HYPHEN_SPLITTER = Splitter.on('-');

  /** @see android.view.WindowManager#getDefaultDisplay() */
  private final DisplayInfo defaultDisplayInfo;

  /** @see Build.VERSION#SDK_INT */
  protected final int sdkVersion;

  protected final Locale locale;

  protected final @Nullable Float fontScale;

  /** Creates a record of the device state at the time of construction. */
  DeviceState(DeviceStateProto fromProto) {
    sdkVersion = fromProto.getSdkVersion();
    defaultDisplayInfo = new DisplayInfo(fromProto.getDefaultDisplayInfo());
    String languageTag = fromProto.getLocale();
    // Use English if no locale was recorded in the proto.
    locale = languageTag.isEmpty() ? Locale.ENGLISH : getLocaleFromLanguageTag(languageTag);
    fontScale = fromProto.hasFontScale() ? fromProto.getFontScale() : null;
  }

  protected DeviceState(int sdkVersion, Locale locale, @Nullable Float fontScale) {
    this.sdkVersion = sdkVersion;
    this.locale = locale;
    this.fontScale = fontScale;
    defaultDisplayInfo = new DisplayInfo();
  }

  /** See {@link android.view.WindowManager#getDefaultDisplay() getDefaultDisplay}. */
  public DisplayInfo getDefaultDisplayInfo() {
    return defaultDisplayInfo;
  }

  /** @see Build.VERSION#SDK_INT */
  public int getSdkVersion() {
    return sdkVersion;
  }

  /**
   * Gets the locale at the time the device state was captured. Returns Locale.ENGLISH for instances
   * persisted before DeviceState began storing capture-time locale (ca. Feb 2018)
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Gets the Accessibility Font scale at the time the device state was captured.
   *
   * @see android.content.res.Configuration#fontScale
   */
  public @Nullable Float getFontScale() {
    return fontScale;
  }

  DeviceStateProto toProto() {
    DeviceStateProto.Builder builder = DeviceStateProto.newBuilder();
    builder.setSdkVersion(sdkVersion);
    builder.setDefaultDisplayInfo(getDefaultDisplayInfo().toProto());
    builder.setLocale(getLanguageTag());
    if (fontScale != null) {
      builder.setFontScale(fontScale);
    }
    return builder.build();
  }

  private String getLanguageTag() {
    return locale.toLanguageTag();
  }

  private static Locale getLocaleFromLanguageTag(String languageTag) {
    return Locale.forLanguageTag(languageTag);
  }
}

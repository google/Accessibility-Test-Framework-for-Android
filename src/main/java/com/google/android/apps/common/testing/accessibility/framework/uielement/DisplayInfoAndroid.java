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
package com.google.android.apps.common.testing.accessibility.framework.uielement;

import android.util.DisplayMetrics;
import android.view.Display;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.DisplayInfoMetricsProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.DisplayInfoProto;
import com.google.errorprone.annotations.Immutable;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Representation of a {@link Display}
 *
 * <p>NOTE: Currently, this class holds only {@link MetricsAndroid}, but will likely have additional
 * fields in the future.
 */
@Immutable
public class DisplayInfoAndroid extends DisplayInfo {

  private final MetricsAndroid metricsWithoutDecoration;
  private final @Nullable MetricsAndroid realMetrics;

  /**
   * Derives an instance from a {@link Display}
   *
   * @param display The {@link Display} instance from which to construct
   */
  public DisplayInfoAndroid(Display display) {
    super();
    DisplayMetrics tempMetrics = new DisplayMetrics();
    display.getMetrics(tempMetrics);
    this.metricsWithoutDecoration = new MetricsAndroid(tempMetrics);
    tempMetrics.setToDefaults();
    display.getRealMetrics(tempMetrics);
    this.realMetrics = new MetricsAndroid(tempMetrics);
  }

  DisplayInfoAndroid(DisplayInfoProto fromProto) {
    super();
    this.metricsWithoutDecoration = new MetricsAndroid(fromProto.getMetricsWithoutDecoration());
    this.realMetrics =
        fromProto.hasRealMetrics() ? new MetricsAndroid(fromProto.getRealMetrics()) : null;
  }

  /**
   * See {@link Display#getMetrics(DisplayMetrics)}.
   *
   * @return a {@link MetricsAndroid} representing the display's metrics excluding certain system
   *     decorations.
   */
  @Override
  public MetricsAndroid getMetricsWithoutDecoration() {
    return metricsWithoutDecoration;
  }

  /**
   * See {@link Display#getRealMetrics(DisplayMetrics)}.
   *
   * @return a {@link MetricsAndroid} representing the display's real metrics, which include system
   *     decorations. This value can be {@code null} for instances created on platform versions that
   *     don't support resolution of real metrics.
   */
  @Override
  public @Nullable MetricsAndroid getRealMetrics() {
    return realMetrics;
  }

  @Override
  DisplayInfoProto toProto() {
    DisplayInfoProto.Builder builder = DisplayInfoProto.newBuilder();
    builder.setMetricsWithoutDecoration(metricsWithoutDecoration.toProto());
    if (realMetrics != null) {
      builder.setRealMetrics(realMetrics.toProto());
    }
    return builder.build();
  }

  /** Representation of a {@link DisplayMetrics} */
  @Immutable
  public static class MetricsAndroid extends Metrics {
    /**
     * Derives an instance from a {@link DisplayMetrics}
     *
     * @param metrics The {@link DisplayMetrics} instance from which to construct
     */
    public MetricsAndroid(DisplayMetrics metrics) {
      super(
          metrics.density,
          metrics.scaledDensity,
          metrics.xdpi,
          metrics.ydpi,
          metrics.densityDpi,
          metrics.heightPixels,
          metrics.widthPixels);
    }

    MetricsAndroid(DisplayInfoMetricsProto fromProto) {
      super(fromProto);
    }
  }
}

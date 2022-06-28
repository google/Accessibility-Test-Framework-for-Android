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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.DisplayInfoMetricsProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.DisplayInfoProto;
import com.google.errorprone.annotations.Immutable;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Representation of a {@link android.view.Display}
 *
 * <p>NOTE: Currently, this class holds only {@link Metrics}, but will likely have additional fields
 * in the future.
 */
@Immutable
public class DisplayInfo {

  private final @Nullable Metrics metricsWithoutDecoration;
  private final @Nullable Metrics realMetrics;

  DisplayInfo(DisplayInfoProto fromProto) {
    this.metricsWithoutDecoration = new Metrics(fromProto.getMetricsWithoutDecoration());
    this.realMetrics = fromProto.hasRealMetrics() ? new Metrics(fromProto.getRealMetrics()) : null;
  }

  protected DisplayInfo() {
    this.metricsWithoutDecoration = null;
    this.realMetrics = null;
  }

  /**
   * @return a {@link Metrics} representing the display's metrics excluding certain system
   *     decorations.
   * @see android.view.Display#getMetrics(android.util.DisplayMetrics)
   */
  public Metrics getMetricsWithoutDecoration() {
    checkNotNull(metricsWithoutDecoration);
    return metricsWithoutDecoration;
  }

  /**
   * @return a {@link Metrics} representing the display's real metrics, which include system
   *     decorations. This value can be {@code null} for instances created on platform versions that
   *     don't support resolution of real metrics.
   * @see android.view.Display#getRealMetrics(android.util.DisplayMetrics)
   */
  public @Nullable Metrics getRealMetrics() {
    return realMetrics;
  }

  DisplayInfoProto toProto() {
    checkNotNull(metricsWithoutDecoration);
    DisplayInfoProto.Builder builder = DisplayInfoProto.newBuilder();
    builder.setMetricsWithoutDecoration(metricsWithoutDecoration.toProto());
    if (realMetrics != null) {
      builder.setRealMetrics(realMetrics.toProto());
    }
    return builder.build();
  }

  /** Representation of a {@link android.util.DisplayMetrics} */
  @Immutable
  public static class Metrics {

    protected final float density;
    protected final float scaledDensity;
    protected final float xDpi;
    protected final float yDpi;
    protected final int densityDpi;
    protected final int heightPixels;
    protected final int widthPixels;

    Metrics(
        float density,
        float scaledDensity,
        float xDpi,
        float yDpi,
        int densityDpi,
        int heightPixels,
        int widthPixels) {
      this.density = density;
      this.scaledDensity = scaledDensity;
      this.xDpi = xDpi;
      this.yDpi = yDpi;
      this.densityDpi = densityDpi;
      this.heightPixels = heightPixels;
      this.widthPixels = widthPixels;
    }

    Metrics(DisplayInfoMetricsProto fromProto) {
      this.density = fromProto.getDensity();
      this.scaledDensity = fromProto.getScaledDensity();
      this.xDpi = fromProto.getXDpi();
      this.yDpi = fromProto.getYDpi();
      this.densityDpi = fromProto.getDensityDpi();
      this.heightPixels = fromProto.getHeightPixels();
      this.widthPixels = fromProto.getWidthPixels();
    }

    /** See {@link android.util.DisplayMetrics#density}. */
    public float getDensity() {
      return density;
    }

    /** See {@link android.util.DisplayMetrics#scaledDensity}. */
    public float getScaledDensity() {
      return scaledDensity;
    }

    /** See {@link android.util.DisplayMetrics#xdpi}. */
    public float getxDpi() {
      return xDpi;
    }

    /** See {@link android.util.DisplayMetrics#ydpi}. */
    public float getyDpi() {
      return yDpi;
    }

    /** See {@link android.util.DisplayMetrics#densityDpi}. */
    public int getDensityDpi() {
      return densityDpi;
    }

    /** See {@link android.util.DisplayMetrics#heightPixels}. */
    public int getHeightPixels() {
      return heightPixels;
    }

    /** See {@link android.util.DisplayMetrics#widthPixels}. */
    public int getWidthPixels() {
      return widthPixels;
    }

    DisplayInfoMetricsProto toProto() {
      DisplayInfoMetricsProto.Builder builder = DisplayInfoMetricsProto.newBuilder();
      builder.setDensity(density);
      builder.setScaledDensity(scaledDensity);
      builder.setXDpi(xDpi);
      builder.setYDpi(yDpi);
      builder.setDensityDpi(densityDpi);
      builder.setHeightPixels(heightPixels);
      builder.setWidthPixels(widthPixels);
      return builder.build();
    }
  }
}

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

import android.os.Build;
import android.os.Parcel;
import androidx.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Display;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.DisplayInfoMetricsProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.DisplayInfoProto;

/**
 * Representation of a {@link Display}
 * <p>
 * NOTE: Currently, this class holds only {@link Metrics}, but will likely have additional fields in
 * the future.
 */
public class DisplayInfo {

  private final Metrics metricsWithoutDecoration;
  private final @Nullable Metrics realMetrics;

  /**
   * Derives an instance from a {@link Display}
   *
   * @param display The {@link Display} instance from which to construct
   */
  public DisplayInfo(Display display) {
    DisplayMetrics tempMetrics = new DisplayMetrics();
    display.getMetrics(tempMetrics);
    this.metricsWithoutDecoration = new Metrics(tempMetrics);
    tempMetrics.setToDefaults();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      display.getRealMetrics(tempMetrics);
      this.realMetrics = new Metrics(tempMetrics);
    } else {
      this.realMetrics = null;
    }
  }

  DisplayInfo(Parcel fromParcel) {
    this.metricsWithoutDecoration = new Metrics(fromParcel);
    this.realMetrics = (fromParcel.readInt() == 1) ? new Metrics(fromParcel) : null;
  }

  DisplayInfo(DisplayInfoProto fromProto) {
    this.metricsWithoutDecoration = new Metrics(fromProto.getMetricsWithoutDecoration());
    this.realMetrics =
        (fromProto.hasRealMetrics()) ? new Metrics(fromProto.getRealMetrics()) : null;
  }

  /**
   * @return a {@link Metrics} representing the display's metrics excluding certain system
   *         decorations.
   * @see Display#getMetrics(DisplayMetrics)
   */
  public Metrics getMetricsWithoutDecoration() {
    return metricsWithoutDecoration;
  }

  /**
   * @return a {@link Metrics} representing the display's real metrics, which include system
   *         decorations. This value can be {@code null} for instances created on platform versions
   *         that don't support resolution of real metrics.
   * @see Display#getRealMetrics(DisplayMetrics)
   */
  public @Nullable Metrics getRealMetrics() {
    return realMetrics;
  }

  void writeToParcel(Parcel out, int flags) {
    metricsWithoutDecoration.writeToParcel(out, flags);
    if (realMetrics != null) {
      out.writeInt(1);
      realMetrics.writeToParcel(out, flags);
    } else {
      out.writeInt(0);
    }
  }

  DisplayInfoProto toProto() {
    DisplayInfoProto.Builder builder = DisplayInfoProto.newBuilder();
    builder.setMetricsWithoutDecoration(metricsWithoutDecoration.toProto());
    if (realMetrics != null) {
      builder.setRealMetrics(realMetrics.toProto());
    }
    return builder.build();
  }

  /**
   * Representation of a {@link DisplayMetrics}
   */
  public static class Metrics {

    private final float density;
    private final float scaledDensity;
    private final float xDpi;
    private final float yDpi;
    private final int densityDpi;
    private final int heightPixels;
    private final int widthPixels;

    /**
     * Derives an instance from a {@link DisplayMetrics}
     *
     * @param metrics The {@link DisplayMetrics} instance from which to construct
     */
    public Metrics(DisplayMetrics metrics) {
      this.density = metrics.density;
      this.scaledDensity = metrics.scaledDensity;
      this.xDpi = metrics.xdpi;
      this.yDpi = metrics.ydpi;
      this.densityDpi = metrics.densityDpi;
      this.heightPixels = metrics.heightPixels;
      this.widthPixels = metrics.widthPixels;
    }

    Metrics(Parcel fromParcel) {
      this.density = fromParcel.readFloat();
      this.scaledDensity = fromParcel.readFloat();
      this.xDpi = fromParcel.readFloat();
      this.yDpi = fromParcel.readFloat();
      this.densityDpi = fromParcel.readInt();
      this.heightPixels = fromParcel.readInt();
      this.widthPixels = fromParcel.readInt();
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

    /**
     * @see DisplayMetrics#density
     */
    public float getDensity() {
      return density;
    }

    /**
     * @see DisplayMetrics#scaledDensity
     */
    public float getScaledDensity() {
      return scaledDensity;
    }

    /**
     * @see DisplayMetrics#xdpi
     */
    public float getxDpi() {
      return xDpi;
    }

    /**
     * @see DisplayMetrics#ydpi
     */
    public float getyDpi() {
      return yDpi;
    }

    /**
     * @see DisplayMetrics#densityDpi
     */
    public int getDensityDpi() {
      return densityDpi;
    }

    /**
     * @see DisplayMetrics#heightPixels
     */
    public int getHeightPixels() {
      return heightPixels;
    }

    /**
     * @see DisplayMetrics#widthPixels
     */
    public int getWidthPixels() {
      return widthPixels;
    }

    void writeToParcel(Parcel dest, @SuppressWarnings("unused") int flags) {
      dest.writeFloat(density);
      dest.writeFloat(scaledDensity);
      dest.writeFloat(xDpi);
      dest.writeFloat(yDpi);
      dest.writeInt(densityDpi);
      dest.writeInt(heightPixels);
      dest.writeInt(widthPixels);
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

package com.google.android.apps.common.testing.accessibility.framework.replacements;

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.LayoutParamsProto;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Used as a local immutable replacement for Android's {@link android.view.ViewGroup.LayoutParams}
 */
public final class LayoutParams {

  /** @see android.view.ViewGroup.LayoutParams#MATCH_PARENT */
  public static final int MATCH_PARENT = -1;

  /** @see android.view.ViewGroup.LayoutParams#WRAP_CONTENT */
  public static final int WRAP_CONTENT = -2;

  private final int width;

  private final int height;

  public LayoutParams(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public LayoutParams(LayoutParamsProto layoutParamsProto) {
    this(layoutParamsProto.getWidth(), layoutParamsProto.getHeight());
  }

  /** @see android.view.ViewGroup.LayoutParams#width */
  public int getWidth() {
    return width;
  }

  /** @see android.view.ViewGroup.LayoutParams#height */
  public int getHeight() {
    return height;
  }

  public LayoutParamsProto toProto() {
    return LayoutParamsProto.newBuilder().setWidth(width).setHeight(height).build();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }

    if (o instanceof LayoutParams) {
      LayoutParams params = (LayoutParams) o;
      return (params.width == width) && (params.height == height);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 31 * width + height;
  }

  @Override
  public String toString() {
    return "LayoutParams(" + width + ", " + height + ")";
  }
}

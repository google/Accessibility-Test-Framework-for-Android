/*
 * Copyright (C) 2017 Google Inc.
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

package com.google.android.apps.common.testing.accessibility.framework.replacements;

import static java.lang.Math.max;
import static java.lang.Math.min;

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.RectProto;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Used as a local immutable replacement for Android's {@link android.graphics.Rect} */
public final class Rect {

  public static final Rect EMPTY = new Rect(0, 0, 0, 0);

  private final int left;
  private final int top;
  private final int right;
  private final int bottom;

  public Rect(int left, int top, int right, int bottom) {
    // Ensure coordinate ordering is sensible
    this.left = min(left, right);
    this.top = min(top, bottom);
    this.right = max(left, right);
    this.bottom = max(top, bottom);
  }

  public Rect(RectProto rectProto) {
    this(rectProto.getLeft(), rectProto.getTop(), rectProto.getRight(), rectProto.getBottom());
  }

  /** See {@link android.graphics.Rect#left} */
  public final int getLeft() {
    return left;
  }

  /** See {@link android.graphics.Rect#top} */
  public final int getTop() {
    return top;
  }

  /** See {@link android.graphics.Rect#right} */
  public final int getRight() {
    return right;
  }

  /** See {@link android.graphics.Rect#bottom} */
  public final int getBottom() {
    return bottom;
  }

  /** See {@link android.graphics.Rect#width()} */
  public final int getWidth() {
    return right - left;
  }

  /** See {@link android.graphics.Rect#height()} */
  public final int getHeight() {
    return bottom - top;
  }

  /** See {@link android.graphics.Rect#contains(android.graphics.Rect)} */
  public boolean contains(Rect r) {
    return !isEmpty()
        && (this.left <= r.left)
        && (this.top <= r.top)
        && (this.right >= r.right)
        && (this.bottom >= r.bottom);
  }

  /** See {@link android.graphics.Rect#contains(int, int)} */
  public boolean contains(int x, int y) {
    return !isEmpty() && x >= left && x < right && y >= top && y < bottom;
  }

  /** See {@link android.graphics.Rect#isEmpty()} */
  public boolean isEmpty() {
    return (left == right) || (top == bottom);
  }

  /**
   * Returns a {@link Rect} which encloses this rectangle and the specified rectangle. If the
   * specified rectangle is empty, return {@code this}. If this rectangle is empty, return the
   * specified rectangle.
   *
   * <p>See {@link android.graphics.Rect#union(android.graphics.Rect)}
   *
   * @param r The rectangle being unioned with this rectangle
   */
  public Rect union(Rect r) {
    return isEmpty() ? r : union(r.left, r.top, r.right, r.bottom);
  }

  /**
   * Returns a {@link Rect} which encloses this rectangle and the specified rectangle. If the
   * specified rectangle is empty, return {@code this}. If this rectangle is empty, return the
   * specified rectangle.
   *
   * <p>See {@link android.graphics.Rect#union(int, int, int, int)}
   *
   * @param left The left edge being unioned with this rectangle
   * @param top The top edge being unioned with this rectangle
   * @param right The right edge being unioned with this rectangle
   * @param bottom The bottom edge being unioned with this rectangle
   */
  public Rect union(int left, int top, int right, int bottom) {
    if ((right <= left) || (bottom <= top)) {
      return this;
    }

    if (isEmpty()) {
      return new Rect(left, top, right, bottom);
    } else {
      return new Rect(
          min(left, this.left),
          min(top, this.top),
          max(right, this.right),
          max(bottom, this.bottom));
    }
  }

  public RectProto toProto() {
    return RectProto.newBuilder()
        .setLeft(left)
        .setTop(top)
        .setRight(right)
        .setBottom(bottom)
        .build();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || Rect.class != o.getClass()) {
      return false;
    }

    Rect r = (Rect) o;
    return ((left == r.left) && (top == r.top) && (right == r.right) && (bottom == r.bottom));
  }

  @Override
  public int hashCode() {
    int result = left;
    result = 31 * result + top;
    result = 31 * result + right;
    result = 31 * result + bottom;
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(32);
    sb.append("Rect(");
    sb.append(left);
    sb.append(", ");
    sb.append(top);
    sb.append(" - ");
    sb.append(right);
    sb.append(", ");
    sb.append(bottom);
    sb.append(")");
    return sb.toString();
  }

  /** See {@link android.graphics.Rect#toShortString()} */
  public String toShortString() {
    StringBuilder sb = new StringBuilder(32);
    sb.append('[');
    sb.append(left);
    sb.append(',');
    sb.append(top);
    sb.append("][");
    sb.append(right);
    sb.append(',');
    sb.append(bottom);
    sb.append(']');
    return sb.toString();
  }

  /** See {@link android.graphics.Rect#intersects(android.graphics.Rect, android.graphics.Rect)} */
  public static boolean intersects(Rect a, Rect b) {
    return (a.top < b.bottom) && (b.top < a.bottom) && (a.left < b.right) && (b.left < a.right);
  }
}

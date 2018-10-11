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

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.RectProto;

/** Used as a local immutable replacement for Android's {@link android.graphics.Rect} */
public class Rect implements Replaceable<android.graphics.Rect>, Parcelable {

  public static final Rect EMPTY = new Rect(0, 0, 0, 0);

  private final int left;
  private final int top;
  private final int right;
  private final int bottom;

  public Rect(int left, int top, int right, int bottom) {
    // Ensure coordinate ordering is sensible
    this.left = Math.min(left, right);
    this.top = Math.min(top, bottom);
    this.right = Math.max(left, right);
    this.bottom = Math.max(top, bottom);
  }

  public Rect(android.graphics.Rect rect) {
    this(rect.left, rect.top, rect.right, rect.bottom);
  }

  public Rect(RectProto rectProto) {
    this(rectProto.getLeft(), rectProto.getTop(), rectProto.getRight(), rectProto.getBottom());
  }

  /**
   * @see android.graphics.Rect#left
   */
  public final int getLeft() {
    return left;
  }

  /**
   * @see android.graphics.Rect#top
   */
  public final int getTop() {
    return top;
  }

  /**
   * @see android.graphics.Rect#right
   */
  public final int getRight() {
    return right;
  }

  /**
   * @see android.graphics.Rect#bottom
   */
  public final int getBottom() {
    return bottom;
  }

  /**
   * @see android.graphics.Rect#width()
   */
  public final int getWidth() {
    return right - left;
  }

  /**
   * @see android.graphics.Rect#height()
   */
  public final int getHeight() {
    return bottom - top;
  }

  /**
   * @see android.graphics.Rect#contains(android.graphics.Rect)
   */
  public boolean contains(Rect r) {
    return !isEmpty()
        && (this.left <= r.left)
        && (this.top <= r.top)
        && (this.right >= r.right)
        && (this.bottom >= r.bottom);
  }

  /**
   * @see android.graphics.Rect#isEmpty()
   */
  public boolean isEmpty() {
    return (left == right) || (top == bottom);
  }

  @Override
  public android.graphics.Rect getAndroidInstance() {
    return new android.graphics.Rect(left, top, right, bottom);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public RectProto toProto() {
    RectProto.Builder builder = RectProto.newBuilder();
    builder.setLeft(left);
    builder.setTop(top);
    builder.setRight(right);
    builder.setBottom(bottom);
    return builder.build();
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeInt(left);
    out.writeInt(top);
    out.writeInt(right);
    out.writeInt(bottom);
  }

  private static Rect readFromParcel(Parcel in) {
    int left = in.readInt();
    int top = in.readInt();
    int right = in.readInt();
    int bottom = in.readInt();
    return new Rect(left, top, right, bottom);
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

  /**
   * @see android.graphics.Rect#toShortString()
   */
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

  public static final Parcelable.Creator<Rect> CREATOR =
      new Parcelable.Creator<Rect>() {
        @Override
        public Rect createFromParcel(Parcel in) {
          return readFromParcel(in);
        }

        @Override
        public Rect[] newArray(int size) {
          return new Rect[size];
        }
      };
}

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

import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

/** Used as a local immutable replacement for Android's {@link android.graphics.Point} */
public class Point implements Replaceable<android.graphics.Point>, Parcelable {

  private final int x;
  private final int y;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Point(android.graphics.Point point) {
    this(checkNotNull(point).x, checkNotNull(point).y);
  }

  /** @see android.graphics.Point#x */
  public final int getX() {
    return x;
  }

  /** @see android.graphics.Point#y */
  public final int getY() {
    return y;
  }

  @Override
  public android.graphics.Point getAndroidInstance() {
    return new android.graphics.Point(x, y);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeInt(x);
    out.writeInt(y);
  }

  private static Point readFromParcel(Parcel in) {
    int x = in.readInt();
    int y = in.readInt();
    return new Point(x, y);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Point point = (Point) o;
    return (x == point.x) && (y == point.y);
  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(32);
    sb.append("Point(");
    sb.append(x);
    sb.append(", ");
    sb.append(y);
    sb.append(")");
    return sb.toString();
  }

  public static final Parcelable.Creator<Point> CREATOR =
      new Parcelable.Creator<Point>() {
        @Override
        public Point createFromParcel(Parcel in) {
          return readFromParcel(in);
        }

        @Override
        public Point[] newArray(int size) {
          return new Point[size];
        }
      };
}

/*
 * Copyright (C) 2014 Google Inc.
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

package com.google.android.apps.common.testing.accessibility.framework;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.googlecode.eyesfree.utils.LogUtils;

/**
 * Result generated when an accessibility check runs on a {@code AccessibilityNodeInfo}.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public final class AccessibilityInfoCheckResult extends AccessibilityCheckResult implements
    Parcelable {

  private AccessibilityNodeInfo info;

  /**
   * @param checkClass The check that generated the error
   * @param type The type of the result
   * @param message A human-readable message explaining the error
   * @param info The info that was responsible for generating the error
   */
  public AccessibilityInfoCheckResult(Class<? extends AccessibilityCheck> checkClass,
      AccessibilityCheckResultType type, CharSequence message, AccessibilityNodeInfo info) {
    super(checkClass, type, message);
    if (info != null) {
      this.info = AccessibilityNodeInfo.obtain(info);
    }
  }

  private AccessibilityInfoCheckResult(Parcel in) {
    super(null, null, null);
    readFromParcel(in);
  }

  /**
   * @return The info to which the result applies.
   */
  public AccessibilityNodeInfo getInfo() {
    return info;
  }

  @Override
  public void recycle() {
    super.recycle();

    if (info != null) {
      info.recycle();
      info = null;
    }
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString((checkClass != null) ? checkClass.getName() : "");
    dest.writeInt((type != null) ? type.ordinal() : -1);
    TextUtils.writeToParcel(message, dest, flags);
    // Info requires a presence flag
    if (info != null) {
      dest.writeInt(1);
      info.writeToParcel(dest, flags);
    } else {
      dest.writeInt(0);
    }
  }

  @SuppressWarnings("unchecked")
  private void readFromParcel(Parcel in) {
    // Check class (unchecked cast checked by isAssignableFrom)
    checkClass = null;
    String checkClassName = in.readString();
    if (!("".equals(checkClassName))) {
      try {
        Class<?> uncheckedClass = Class.forName(checkClassName);
        if (AccessibilityCheck.class.isAssignableFrom(uncheckedClass)) {
          checkClass = (Class<? extends AccessibilityCheck>) uncheckedClass;
        }
      } catch (ClassNotFoundException e) {
        // If the reference can't be resolved by our class loader, remain null.
        LogUtils.log(this, Log.WARN, "Attempt to obtain unknown class %1$s", checkClassName);
      }
    }

    // Type
    final int type = in.readInt();
    this.type = (type != -1) ? AccessibilityCheckResultType.values()[type] : null;

    // Message
    this.message = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);

    // Info
    this.info = (in.readInt() == 1) ? AccessibilityNodeInfo.CREATOR.createFromParcel(in) : null;

  }

  public static final Parcelable.Creator<AccessibilityInfoCheckResult> CREATOR =
      new Parcelable.Creator<AccessibilityInfoCheckResult>() {
        @Override
        public AccessibilityInfoCheckResult createFromParcel(Parcel in) {
          return new AccessibilityInfoCheckResult(in);
        }

        @Override
        public AccessibilityInfoCheckResult[] newArray(int size) {
          return new AccessibilityInfoCheckResult[size];
        }
      };
}

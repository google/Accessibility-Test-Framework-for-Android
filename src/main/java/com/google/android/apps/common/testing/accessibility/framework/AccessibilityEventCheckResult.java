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

package com.google.android.apps.common.testing.accessibility.framework;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.googlecode.eyesfree.utils.LogUtils;

/**
 * Result generated when an accessibility check runs on a {@link AccessibilityEvent}.
 */
@TargetApi(Build.VERSION_CODES.DONUT)
public final class AccessibilityEventCheckResult extends AccessibilityCheckResult implements
    Parcelable {

  private AccessibilityEvent event;

  /**
   * @param checkClass The check that generated the error
   * @param type The type of the result
   * @param message A human-readable message explaining the error
   * @param event The {@link AccessibilityEvent} reported as the cause of the result
   */
  public AccessibilityEventCheckResult(Class<? extends AccessibilityCheck> checkClass,
      AccessibilityCheckResultType type, CharSequence message, AccessibilityEvent event) {
    super(checkClass, type, message);
    if (event != null) {
      this.event = AccessibilityEvent.obtain(event);
    }
  }

  private AccessibilityEventCheckResult(Parcel in) {
    super(null, null, null);
    readFromParcel(in);
  }

  /**
   * @return The {@link AccessibilityEvent} to which the result applies
   */
  public AccessibilityEvent getEvent() {
    return event;
  }

  @Override
  public void recycle() {
    super.recycle();
    if (event != null) {
      event.recycle();
      event = null;
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

    // Event requires a presence flag
    if (event != null) {
      dest.writeInt(1);
      event.writeToParcel(dest, flags);
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

    // Event
    this.event = (in.readInt() == 1) ? AccessibilityEvent.CREATOR.createFromParcel(in) : null;
  }

  public static final Parcelable.Creator<AccessibilityEventCheckResult> CREATOR =
      new Parcelable.Creator<AccessibilityEventCheckResult>() {
        @Override
        public AccessibilityEventCheckResult createFromParcel(Parcel in) {
          return new AccessibilityEventCheckResult(in);
        }

        @Override
        public AccessibilityEventCheckResult[] newArray(int size) {
          return new AccessibilityEventCheckResult[size];
        }
      };
}

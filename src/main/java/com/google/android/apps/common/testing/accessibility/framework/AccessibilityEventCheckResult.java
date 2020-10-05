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

import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import java.util.Locale;

/**
 * Result generated when an accessibility check runs on a {@link AccessibilityEvent}.
 */
public final class AccessibilityEventCheckResult extends AccessibilityCheckResult implements
    Parcelable {

  private final AccessibilityEvent event;

  /**
   * @param checkClass The check that generated the error
   * @param type The type of the result
   * @param message A human-readable message explaining the error
   * @param event The {@link AccessibilityEvent} reported as the cause of the result
   */
  public AccessibilityEventCheckResult(Class<? extends AccessibilityCheck> checkClass,
      AccessibilityCheckResultType type, CharSequence message, AccessibilityEvent event) {
    super(checkClass, type, message);
    this.event = AccessibilityEvent.obtain(event);
  }

  /**
   * @return The {@link AccessibilityEvent} to which the result applies
   */
  public AccessibilityEvent getEvent() {
    return event;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(getSourceCheckClass().getName());
    dest.writeInt(getType().ordinal());
    TextUtils.writeToParcel(getMessage(Locale.ENGLISH), dest, flags);
    event.writeToParcel(dest, flags);
  }

  @SuppressWarnings("unchecked")
  private static AccessibilityEventCheckResult readFromParcel(Parcel in) {
    // Check class (unchecked cast checked by isAssignableFrom)
    String checkClassName = checkNotNull(in.readString());
    Class<? extends AccessibilityCheck> checkClass;
    try {
      Class<?> uncheckedClass = Class.forName(checkClassName);
      if ((uncheckedClass != null) && AccessibilityCheck.class.isAssignableFrom(uncheckedClass)) {
        checkClass = (Class<? extends AccessibilityCheck>) uncheckedClass;
      } else {
        throw new RuntimeException(
            String.format("Class: %1$s is not assignable from AccessibilityCheck", checkClassName));
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(
          String.format("Failed to resolve check class: %1$s", checkClassName), e);
    }

    // Type
    int typeInt = in.readInt();
    AccessibilityCheckResultType type = AccessibilityCheckResultType.values()[typeInt];

    // Message
    CharSequence message = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);

    // Event
    AccessibilityEvent event = AccessibilityEvent.CREATOR.createFromParcel(in);

    return new AccessibilityEventCheckResult(checkClass, checkNotNull(type), message, event);
  }

  public static final Parcelable.Creator<AccessibilityEventCheckResult> CREATOR =
      new Parcelable.Creator<AccessibilityEventCheckResult>() {
        @Override
        public AccessibilityEventCheckResult createFromParcel(Parcel in) {
          return readFromParcel(in);
        }

        @Override
        public AccessibilityEventCheckResult[] newArray(int size) {
          return new AccessibilityEventCheckResult[size];
        }
      };
}

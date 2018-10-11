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

import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.googlecode.eyesfree.compat.CompatUtils;
import java.lang.reflect.Method;

/**
 * Result generated when an accessibility check runs on a {@code AccessibilityNodeInfo}.
 *
 * @deprecated Direct evaluation of {@link AccessibilityNodeInfo}s is deprecated.  Instead, create
 *             an {@link AccessibilityHierarchy} using a source {@link AccessibilityNodeInfo}, and
 *             run {@link AccessibilityHierarchyCheck}s obtained with
 *             {@link AccessibilityCheckPreset}.  Results will be provided in the form of
 *             {@link AccessibilityHierarchyCheckResult}s.
 */
@Deprecated
public final class AccessibilityInfoCheckResult extends AccessibilityCheckResult implements
    Parcelable {

  private final @Nullable AccessibilityNodeInfo info;

  /**
   * @param checkClass The check that generated the error
   * @param type The type of the result
   * @param message A human-readable message explaining the error
   * @param info The info that was responsible for generating the error
   */
  public AccessibilityInfoCheckResult(
      Class<? extends AccessibilityCheck> checkClass,
      AccessibilityCheckResultType type,
      CharSequence message,
      @Nullable AccessibilityNodeInfo info) {
    super(checkClass, type, message);
    this.info = (info != null) ? AccessibilityNodeInfo.obtain(info) : null;
  }

  /**
   * @return The info to which the result applies.
   */
  public @Nullable AccessibilityNodeInfo getInfo() {
    return info;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(checkClass.getName());
    dest.writeInt(type.ordinal());
    TextUtils.writeToParcel(checkNotNull(message), dest, flags);
    if (info != null) {
      dest.writeInt(1);
      new AccessibilityNodeInfoWrapper(info).writeToParcel(dest, flags);
    } else {
      dest.writeInt(0);
    }
  }

  @SuppressWarnings("unchecked")
  private static AccessibilityInfoCheckResult readFromParcel(Parcel in) {
    // Check class (unchecked cast checked by isAssignableFrom)
    String checkClassName = in.readString();
    Class<? extends AccessibilityCheck> checkClass;
    try {
      Class<?> uncheckedClass = (checkClassName == null) ? null : Class.forName(checkClassName);
      if ((uncheckedClass != null) && AccessibilityCheck.class.isAssignableFrom(uncheckedClass)) {
        checkClass = (Class<? extends AccessibilityCheck>) uncheckedClass;
      } else {
        throw new RuntimeException(
            String.format("Class: %1$s is not assignable from AccessibilityCheck", checkClassName));
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(
          String.format("Failed to resolve check class: %1$s", checkClassName));
    }

    // Type
    final int typeInt = in.readInt();
    AccessibilityCheckResultType type = AccessibilityCheckResultType.values()[typeInt];

    // Message
    CharSequence message = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);

    // Info wrapper
    AccessibilityNodeInfo info =
        (in.readInt() == 1)
            ? AccessibilityNodeInfoWrapper.WRAPPER_CREATOR.createFromParcel(in).getWrappedInfo()
            : null;
    return new AccessibilityInfoCheckResult(checkClass, checkNotNull(type), message, info);
  }

  public static final Parcelable.Creator<AccessibilityInfoCheckResult> CREATOR =
      new Parcelable.Creator<AccessibilityInfoCheckResult>() {

        @Override
        public AccessibilityInfoCheckResult createFromParcel(Parcel in) {
          return readFromParcel(in);
        }

        @Override
        public AccessibilityInfoCheckResult[] newArray(int size) {
          return new AccessibilityInfoCheckResult[size];
        }
      };

  /**
   * We use a Parcelable wrapper for {@link AccessibilityNodeInfo} to work around a bug within the
   * Android framework, which improperly unparcels instances which are sealed.
   */
  private static class AccessibilityNodeInfoWrapper implements Parcelable {
    private static final Method METHOD_isSealed = CompatUtils.getMethod(
        AccessibilityNodeInfo.class, "isSealed");

    private static final Method METHOD_setSealed = CompatUtils.getMethod(
        AccessibilityNodeInfo.class, "setSealed", boolean.class);

    private final AccessibilityNodeInfo wrappedInfo;

    public AccessibilityNodeInfoWrapper(AccessibilityNodeInfo wrappedNode) {
      wrappedInfo = wrappedNode;
    }

    @Override
    public int describeContents() {
      return 0;
    }

    public AccessibilityNodeInfo getWrappedInfo() {
      return wrappedInfo;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      if (isSealed(wrappedInfo)) {
        // In the case we've encountered a sealed info, we need to unseal it before parceling.
        // Otherwise, the Android framework won't allow it to be recreated from a parcel due to a
        // bug which improperly checks sealed state when adding parceled actions to an instance.
        // We write our own int to indicate that the node must be re-sealed.
        dest.writeInt(1); // Is sealed
        CompatUtils.invoke(wrappedInfo, null, METHOD_setSealed, false);
      } else {
        dest.writeInt(0); // Is not sealed
      }
      wrappedInfo.writeToParcel(dest, flags);
    }

    /**
     * Use Reflection to see if the given node is sealed. This property is not exposed by
     * AccessibilityNodeInfo.
     */
    private static boolean isSealed(AccessibilityNodeInfo info) {
      return Boolean.TRUE.equals(
          CompatUtils.invoke(info, /* defaultReturnValue= */ false, METHOD_isSealed));
    }

    private static AccessibilityNodeInfoWrapper readFromParcel(Parcel in) {
      boolean shouldSeal = (in.readInt() == 1);
      AccessibilityNodeInfo info =
          AccessibilityNodeInfo.CREATOR.createFromParcel(in);
      if (shouldSeal) {
        CompatUtils.invoke(info, null, METHOD_setSealed, true);
      }
      return new AccessibilityNodeInfoWrapper(info);
    }


    public static final Parcelable.Creator<AccessibilityNodeInfoWrapper> WRAPPER_CREATOR =
        new Parcelable.Creator<AccessibilityNodeInfoWrapper>() {

          @Override
          public AccessibilityNodeInfoWrapper createFromParcel(Parcel in) {
            return readFromParcel(in);
          }

          @Override
          public AccessibilityNodeInfoWrapper[] newArray(int size) {
            return new AccessibilityNodeInfoWrapper[size];
          }
        };
  }
}

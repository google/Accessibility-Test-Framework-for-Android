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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import android.graphics.Rect;
import android.os.Build;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.ResultTypeProto;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The result of an accessibility check. The results are "interesting" in the sense that they
 * indicate some sort of accessibility issue. {@code AccessibilityCheck}s return lists of classes
 * that extend this one. There is no "passing" result; checks that return lists that contain no
 * {@code AccessibilityCheckResultType.ERROR}s have passed.
 * <p>
 * NOTE: Some subtypes of this class retain copies of resources that should be explicitly recycled.
 * Callers should use {@link #recycle()} to dispose of data in this object and release these
 * resources.
 */
public abstract class AccessibilityCheckResult {
  /**
   * Types of results. This must be kept consistent (other than UNKNOWN) with the ResultTypeProto
   * enum in {@code AccessibilityEvaluation.proto}
   *
   * <p>CONTRACT: Once a value is defined here, it must not be removed and its tag number as defined
   * in the protocol buffer representation cannot change. Data may be persisted using these values,
   * so incompatible changes may result in corruption during deserialization.
   */
  public enum AccessibilityCheckResultType {
    /** Clearly an accessibility bug, for example no speakable text on a clicked button */
    ERROR(ResultTypeProto.ERROR),
    /**
     * Potentially an accessibility bug, for example finding another view with the same speakable
     * text as a clicked view
     */
    WARNING(ResultTypeProto.WARNING),
    /**
     * Information that may be helpful when evaluating accessibility, for example a listing of all
     * speakable text in a view hierarchy in the traversal order used by an accessibility service.
     */
    INFO(ResultTypeProto.INFO),
    /**
     * A signal that the check was not run at all (ex. because the API level was too low)
     */
    NOT_RUN(ResultTypeProto.NOT_RUN),
    /**
     * A result that has been explicitly suppressed from throwing any Exceptions, used to allow for
     * known issues.
     */
    SUPPRESSED(ResultTypeProto.SUPPRESSED);

    private static final Map<Integer, AccessibilityCheckResultType> PROTO_NUMBER_MAP =
        new HashMap<>();

    static {
      for (AccessibilityCheckResultType type : values()) {
        PROTO_NUMBER_MAP.put(type.protoNumber, type);
      }
    }

    final int protoNumber;

    private AccessibilityCheckResultType(ResultTypeProto proto) {
      this.protoNumber = proto.getNumber();
    }

    public static AccessibilityCheckResultType fromProto(ResultTypeProto proto) {
      AccessibilityCheckResultType type = PROTO_NUMBER_MAP.get(proto.getNumber());
      checkArgument(
          (type != null),
          "Failed to create AccessibilityCheckResultType from proto with unknown value: %s",
          proto.getNumber());
      return checkNotNull(type);
    }

    public ResultTypeProto toProto() {
      return ResultTypeProto.forNumber(protoNumber);
    }
  }

  protected Class<? extends AccessibilityCheck> checkClass;

  protected AccessibilityCheckResultType type;

  protected @Nullable CharSequence message;

  /**
   * @param checkClass The class of the check that generated the error
   * @param type The type of the result
   * @param message A human-readable message explaining the error. This may be {@code null} when
   *     a subclass overrides {@link #getMessage}.
   */
  public AccessibilityCheckResult(
      Class<? extends AccessibilityCheck> checkClass,
      AccessibilityCheckResultType type,
      @Nullable CharSequence message) {
    this.checkClass = checkClass;
    this.type = type;
    this.message = message;
  }

  /**
   * @return The check that generated the result.
   */
  public Class<? extends AccessibilityCheck> getSourceCheckClass() {
    return checkClass;
  }

  /**
   * @return The type of the result.
   */
  public AccessibilityCheckResultType getType() {
    return type;
  }

  /**
   * @param type The type of the result.
   */
  /* package */ void setType(AccessibilityCheckResultType type) {
    this.type = type;
  }

  /**
   * @return A human-readable message explaining the result.
   */
  public CharSequence getMessage() {
    return checkNotNull(message, "No message was provided");
  }

  /**
   * @return A human-readable message explaining the result.
   *
   * @param locale desired locale for the message
   */
  @SuppressWarnings("unused") // locale is provided for possible future use
  public CharSequence getMessage(Locale locale) {
    return getMessage();
  }

  // For debugging
  @Override
  public String toString() {
    return String.format("AccessibilityCheckResult %s %s \"%s\"", type, checkClass, message);
  }

  /**
   * An object that describes an {@link AccessibilityCheckResult}. This can be extended to provide
   * descriptions of the result and their contents in a form that is localized to the environment in
   * which checks are being run.
   */
  public static class AccessibilityCheckResultDescriptor {

    /**
     * Returns a String description of the given {@link AccessibilityCheckResult}.
     *
     * @param result the {@link AccessibilityCheckResult} to describe
     * @return a String description of the result
     */
    public String describeResult(AccessibilityCheckResult result) {
      StringBuilder message = new StringBuilder();
      if (result instanceof AccessibilityViewCheckResult) {
        message.append(describeView(((AccessibilityViewCheckResult) result).getView()));
        message.append(": ");
      } else if (result instanceof AccessibilityInfoCheckResult) {
        message.append(describeInfo(((AccessibilityInfoCheckResult) result).getInfo()));
        message.append(": ");
      }
      message.append(result.getMessage());
      Class<? extends AccessibilityCheck> checkClass = result.getSourceCheckClass();
      if (checkClass != null) {
        message.append(" Reported by ");
        message.append(result.getSourceCheckClass().getName());
      }
      return message.toString();
    }

    /**
     * Returns a String description of the given {@link View}. The default is to return the view's
     * resource entry name.
     *
     * @param view the {@link View} to describe
     * @return a String description of the given {@link View}
     */
    public String describeView(@Nullable View view) {
      StringBuilder message = new StringBuilder();
      if ((view != null) && (view.getId() != View.NO_ID) && (view.getResources() != null)) {
        message.append("View ");
        try {
          message.append(view.getResources().getResourceEntryName(view.getId()));
        } catch (Exception e) {
          /* In some integrations (seen in Robolectric), the resources may behave inconsistently */
          message.append("with no valid resource name");
        }
      } else {
        message.append("View with no valid resource name");
      }
      return message.toString();
    }

    /**
     * Returns a String description of the given {@link AccessibilityNodeInfo}. The default is to
     * return the view's resource entry name.
     *
     * @param info the {@link AccessibilityNodeInfo} to describe
     * @return a String description of the given {@link AccessibilityNodeInfo}
     */
    public String describeInfo(@Nullable AccessibilityNodeInfo info) {
      if (info == null) {
        return "<null>";
      }
      StringBuilder message = new StringBuilder();
      message.append("View ");
      if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
          && (info != null) && (info.getViewIdResourceName() != null)) {
        message.append(info.getViewIdResourceName());
      } else {
        message.append("with bounds: ");
        Rect bounds = new Rect();
        info.getBoundsInScreen(bounds);
        message.append(bounds.toShortString());
      }
      return message.toString();
    }
  }
}

/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.libraries.accessibility.utils.log;

import android.util.Log;
import com.google.common.base.Strings;
import java.util.IllegalFormatException;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Handles logging formatted strings. */
public class LogUtils {

  private LogUtils() {} // Not instantiable.

  /** Plug-in for custom printing complex objects, especially accessibility event & node. */
  public interface ParameterCustomizer {
    /** Converts an object for display. */
    @Nullable
    Object customize(@Nullable Object object);
  }

  /** Customizer for log parameters. By default, changes nothing. */
  private static @Nullable ParameterCustomizer parameterCustomizer = null;

  private static final String TAG = "LogUtils";

  /**
   * The minimum log level that will be printed to the console. Set this to {@link Log#ERROR} for
   * release or {@link Log#VERBOSE} for debugging.
   */
  private static int minLogLevel = Log.ERROR;

  private static String logTagPrefix = "";

  /**
   * Set the prefix that will be prepended to all logging tags. This is useful for filtering logs
   * specific to a particular application.
   */
  public static void setTagPrefix(String prefix) {
    logTagPrefix = prefix;
  }

  /**
   * Logs a formatted string to the console.
   *
   * @param tag The tag that should be associated with the event
   * @param priority The log entry priority, see {@link Log#println(int, String, String)}
   * @param index The index of the log entry in the current log sequence
   * @param limit The maximum number of log entries allowed in the current sequence
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void logWithLimit(
      String tag, int priority, int index, int limit, String format, @Nullable Object... args) {
    String formatWithIndex;
    if (index > limit) {
      return;
    } else if (index == limit) {
      formatWithIndex = String.format("%s (%d); further messages suppressed", format, index);
    } else {
      formatWithIndex = String.format("%s (%d)", format, index);
    }

    log(tag, priority, formatWithIndex, args);
  }

  /**
   * Logs a string to the console at the VERBOSE log level.
   *
   * @param tag The tag that should be associated with the event
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void v(String tag, @Nullable String format, @Nullable Object... args) {
    log(tag, Log.VERBOSE, format, args);
  }

  /**
   * Logs a string to the console.
   *
   * @param tag The tag that should be associated with the event
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void v(String tag, Throwable throwable, String format, @Nullable Object... args) {
    log(tag, Log.VERBOSE, throwable, format, args);
  }

  /**
   * Logs a string to the console at the DEBUG log level.
   *
   * @param tag The tag that should be associated with the event
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void d(String tag, String format, @Nullable Object... args) {
    log(tag, Log.DEBUG, format, args);
  }

  /**
   * Logs a string to the console at the DEBUG log level.
   *
   * @param tag The tag that should be associated with the event
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void d(String tag, Throwable throwable, String format, @Nullable Object... args) {
    log(tag, Log.DEBUG, throwable, format, args);
  }

  /**
   * Logs a string to the console at the INFO log level.
   *
   * @param tag The tag that should be associated with the event
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void i(String tag, String format, @Nullable Object... args) {
    log(tag, Log.INFO, format, args);
  }

  /**
   * Logs a string to the console at the INFO log level.
   *
   * @param tag The tag that should be associated with the event
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void i(String tag, Throwable throwable, String format, @Nullable Object... args) {
    log(tag, Log.INFO, throwable, format, args);
  }

  /**
   * Logs a string to the console at the WARN log level.
   *
   * @param tag The tag that should be associated with the event
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void w(String tag, String format, @Nullable Object... args) {
    log(tag, Log.WARN, format, args);
  }

  /**
   * Logs a string to the console at the WARN log level.
   *
   * @param tag The tag that should be associated with the event
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void w(String tag, Throwable throwable, String format, @Nullable Object... args) {
    log(tag, Log.WARN, throwable, format, args);
  }

  /**
   * Logs a string to the console at the ERROR log level.
   *
   * @param tag The tag that should be associated with the event
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void e(String tag, @Nullable String format, @Nullable Object... args) {
    log(tag, Log.ERROR, format, args);
  }

  /**
   * Logs a string to the console at the ERROR log level.
   *
   * @param tag The tag that should be associated with the event
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void e(String tag, Throwable throwable, String format, @Nullable Object... args) {
    log(tag, Log.ERROR, throwable, format, args);
  }

  /**
   * Logs a string to the console at the ASSERT log level.
   *
   * @param tag The tag that should be associated with the event
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void wtf(String tag, String format, @Nullable Object... args) {
    log(tag, Log.ASSERT, format, args);
  }

  /**
   * Logs a string to the console at the ASSERT log level.
   *
   * @param tag The tag that should be associated with the event
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void wtf(String tag, Throwable throwable, String format, @Nullable Object... args) {
    log(tag, Log.ASSERT, throwable, format, args);
  }

  /**
   * Logs a formatted string to the console.
   *
   * <p>Example usage: <br>
   * <code>
   * LogUtils.log("LogUtils", Log.ERROR, myException, "Invalid value: %d", value);
   * </code>
   *
   * @param tag The tag that should be associated with the event
   * @param priority The log entry priority, see {@link Log#println(int, String, String)}
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void log(
      String tag,
      int priority,
      @Nullable Throwable throwable,
      @Nullable String format,
      @Nullable Object... args) {
    if (priority < minLogLevel) {
      return;
    }

    String prefixedTag = logTagPrefix + tag;

    // For each argument... replace with custom text.
    if (parameterCustomizer != null) {
      for (int a = 0; a < args.length; ++a) {
        args[a] = parameterCustomizer.customize(args[a]);
      }
    }

    try {
      String message = String.format(Strings.nullToEmpty(format), args);
      if (throwable == null) {
        Log.println(priority, prefixedTag, message);
      } else {
        Log.println(
            priority,
            prefixedTag,
            String.format("%s\n%s", message, Log.getStackTraceString(throwable)));
      }
    } catch (IllegalFormatException e) {
      Log.e(TAG, "Bad formatting string: \"" + format + "\"", e);
    }
  }

  /**
   * Logs a formatted string to the console.
   *
   * <p>Example usage: <br>
   * <code>
   * LogUtils.log("LogUtils", Log.ERROR, "Invalid value: %d", value);
   * </code>
   *
   * @param tag The tag that should be associated with the event
   * @param priority The log entry priority, see {@link Log#println(int, String, String)}
   * @param format A format string, see {@link String#format(String, Object...)}
   * @param args String formatter arguments
   */
  public static void log(
      String tag, int priority, @Nullable String format, @Nullable Object... args) {
    log(tag, priority, null, format, args);
  }

  /** Sets customizer for log parameters. */
  public static void setParameterCustomizer(@Nullable ParameterCustomizer parameterCustomizerArg) {
    parameterCustomizer = parameterCustomizerArg;
  }

  /**
   * Sets the log display level.
   *
   * @param logLevel The minimum log level that will be printed to the console.
   */
  public static void setLogLevel(int logLevel) {
    minLogLevel = logLevel;
  }

  /** Gets the log display level. */
  public static int getLogLevel() {
    return minLogLevel;
  }

  /**
   * Tells if the current settings are at or above the specified level of verboseness. This
   * indicates whether or not a log statement at the provided level will actually result in logging.
   *
   * @param logLevel The log level to compare the current setting to.
   */
  public static boolean shouldLog(int logLevel) {
    // Higher levels of verbosity correspond with lower int values.
    // https://developer.android.com/reference/android/util/Log.html#ASSERT
    return minLogLevel <= logLevel;
  }
}

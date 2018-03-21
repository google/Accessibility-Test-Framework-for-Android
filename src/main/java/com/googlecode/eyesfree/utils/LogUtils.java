/*
 * Copyright (C) 2014 Google Inc.
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

package com.googlecode.eyesfree.utils;

import android.util.Log;
import java.util.IllegalFormatException;

/** Handles logging formatted strings. */
public class LogUtils {
  private static final String TAG = "LogUtils";

  /**
   * The minimum log level that will be printed to the console. Set this to {@link Log#ERROR} for
   * release or {@link Log#VERBOSE} for debugging.
   */
  private static int sLogLevel = Log.ERROR;

  /**
   * Logs a formatted string to the console using the source object's name as the log tag. If the
   * source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * <p>Example usage: <br>
   * <code>
   * LogUtils.log(this, Log.ERROR, "Invalid value: %d", value);
   * </code>
   *
   * @param source The object that generated the log event.
   * @param priority The log entry priority, see {@link Log#println(int, String, String)}.
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void log(
      Object source, int priority, Throwable throwable, String format, Object... args) {
    if (priority < sLogLevel) {
      return;
    }

    final String sourceClass;

    if (source == null) {
      sourceClass = TAG;
    } else if (source instanceof Class<?>) {
      sourceClass = ((Class<?>) source).getSimpleName();
    } else {
      sourceClass = source.getClass().getSimpleName();
    }

    try {
      String message = String.format(format, args);
      if (throwable == null) {
        Log.println(priority, sourceClass, message);
      } else {
        Log.println(
            priority,
            sourceClass,
            String.format("%s\n%s", message, Log.getStackTraceString(throwable)));
      }
    } catch (IllegalFormatException e) {
      Log.e(TAG, "Bad formatting string: \"" + format + "\"", e);
    }
  }

  /**
   * Logs a formatted string to the console using the source object's name as the log tag. If the
   * source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * <p>Example usage: <br>
   * <code>
   * LogUtils.log(this, Log.ERROR, "Invalid value: %d", value);
   * </code>
   *
   * @param source The object that generated the log event.
   * @param priority The log entry priority, see {@link Log#println(int, String, String)}.
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void log(Object source, int priority, String format, Object... args) {
    log(source, priority, null, format, args);
  }

  /**
   * Logs a formatted string to the console using the default tag (see {@link LogUtils#TAG}).
   *
   * @param priority The log entry priority, see {@link Log#println(int, String, String)}.
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void log(int priority, String format, Object... args) {
    log(null, priority, format, args);
  }

  /**
   * Logs a string to the console using the source object's name as the log tag at the verbose
   * level. If the source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * @param source The object that generated the log event.
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void v(Object source, String format, Object... args) {
    log(source, Log.VERBOSE, format, args);
  }

  /**
   * Logs a string to the console using the source object's name as the log tag at the verbose
   * level. If the source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * @param source The object that generated the log event.
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void v(Object source, Throwable throwable, String format, Object... args) {
    log(source, Log.VERBOSE, throwable, format, args);
  }

  /**
   * Logs a string to the console using the source object's name as the log tag at the debug level.
   * If the source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * @param source The object that generated the log event.
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void d(Object source, String format, Object... args) {
    log(source, Log.DEBUG, format, args);
  }

  /**
   * Logs a string to the console using the source object's name as the log tag at the debug level.
   * If the source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * @param source The object that generated the log event.
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void d(Object source, Throwable throwable, String format, Object... args) {
    log(source, Log.DEBUG, throwable, format, args);
  }

  /**
   * Logs a string to the console using the source object's name as the log tag at the info level.
   * If the source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * @param source The object that generated the log event.
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void i(Object source, String format, Object... args) {
    log(source, Log.INFO, format, args);
  }

  /**
   * Logs a string to the console using the source object's name as the log tag at the info level.
   * If the source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * @param source The object that generated the log event.
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void i(Object source, Throwable throwable, String format, Object... args) {
    log(source, Log.INFO, throwable, format, args);
  }

  /**
   * Logs a string to the console using the source object's name as the log tag at the warning
   * level. If the source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * @param source The object that generated the log event.
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void w(Object source, String format, Object... args) {
    log(source, Log.WARN, format, args);
  }

  /**
   * Logs a string to the console using the source object's name as the log tag at the warning
   * level. If the source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * @param source The object that generated the log event.
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void w(Object source, Throwable throwable, String format, Object... args) {
    log(source, Log.WARN, throwable, format, args);
  }

  /**
   * Logs a string to the console using the source object's name as the log tag at the error level.
   * If the source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * @param source The object that generated the log event.
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void e(Object source, String format, Object... args) {
    log(source, Log.ERROR, format, args);
  }

  /**
   * Logs a string to the console using the source object's name as the log tag at the error level.
   * If the source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * @param source The object that generated the log event.
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void e(Object source, Throwable throwable, String format, Object... args) {
    log(source, Log.ERROR, throwable, format, args);
  }

  /**
   * Logs a string to the console using the source object's name as the log tag at the assert level.
   * If the source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * @param source The object that generated the log event.
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void wtf(Object source, String format, Object... args) {
    log(source, Log.ASSERT, format, args);
  }

  /**
   * Logs a string to the console using the source object's name as the log tag at the assert level.
   * If the source object is null, the default tag (see {@link LogUtils#TAG}) is used.
   *
   * @param source The object that generated the log event.
   * @param throwable A {@link Throwable} containing system state information to log
   * @param format A format string, see {@link String#format(String, Object...)}.
   * @param args String formatter arguments.
   */
  public static void wtf(Object source, Throwable throwable, String format, Object... args) {
    log(source, Log.ASSERT, throwable, format, args);
  }

  /**
   * Sets the log display level.
   *
   * @param logLevel The minimum log level that will be printed to the console.
   */
  public static void setLogLevel(int logLevel) {
    sLogLevel = logLevel;
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
    return sLogLevel <= logLevel;
  }

  /** Gets the current level logging is configured for */
  // VisibleForTesting
  public static int getLogLevel() {
    return sLogLevel;
  }
}

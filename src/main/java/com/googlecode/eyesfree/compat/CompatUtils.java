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

package com.googlecode.eyesfree.compat;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CompatUtils {
    private static final String TAG = CompatUtils.class.getSimpleName();

    /** Whether to log debug output. */
    public static boolean DEBUG = false;

    public static Class<?> getClass(String className) {
        if (TextUtils.isEmpty(className)) {
            return null;
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Method getMethod(Class<?> targetClass, String name,
            Class<?>... parameterTypes) {
        if ((targetClass == null) || TextUtils.isEmpty(name)) {
            return null;
        }

        try {
            return targetClass.getDeclaredMethod(name, parameterTypes);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Field getField(Class<?> targetClass, String name) {
        if ((targetClass == null) || (TextUtils.isEmpty(name))) {
            return null;
        }

        try {
            return targetClass.getDeclaredField(name);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Constructor<?> getConstructor(Class<?> targetClass, Class<?>... types) {
        if ((targetClass == null) || (types == null)) {
            return null;
        }

        try {
            return targetClass.getConstructor(types);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Object newInstance(Constructor<?> constructor, Object... args) {
        if (constructor == null) {
            return null;
        }

        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            Log.e(TAG, "Exception in newInstance: " + e.getClass().getSimpleName());

            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Object invoke(
            Object receiver, Object defaultValue, Method method, Object... args) {
        if (method == null) {
            return defaultValue;
        }

        try {
            return method.invoke(receiver, args);
        } catch (Exception e) {
            Log.e(TAG, "Exception in invoke: " + e.getClass().getSimpleName());

            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return defaultValue;
    }

    public static Object getFieldValue(Object receiver, Object defaultValue, Field field) {
        if (field == null) {
            return defaultValue;
        }

        try {
            return field.get(receiver);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }

        return defaultValue;
    }

    public static void setFieldValue(Object receiver, Field field, Object value) {
        if (field == null) {
            return;
        }

        try {
            field.set(receiver, value);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private CompatUtils() {
        // This class is non-instantiable.
    }
}

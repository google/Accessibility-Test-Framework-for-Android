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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * This class manages efficient loading of classes.
 *
 * @author svetoslavganov@google.com (Svetoslav R. Ganov)
 * @author alanv@google.com (Alan Viverette)
 */
public class ClassLoadingManager {

    /**
     * The singleton instance of this class.
     */
    private static ClassLoadingManager sInstance;

    /**
     * Mapping from class names to classes form outside packages.
     */
    private final HashMap<String, Class<?>> mClassNameToClassMap = new HashMap<String, Class<?>>();

    /**
     * A set of classes not found to be loaded. Used to avoid multiple attempts
     * that will fail.
     */
    private final HashMap<String, HashSet<String>> mNotFoundClassesMap =
            new HashMap<String, HashSet<String>>();

    /**
     * The singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static ClassLoadingManager getInstance() {
        if (sInstance == null) {
            sInstance = new ClassLoadingManager();
        }
        return sInstance;
    }

    /**
     * Builds the package cache and registers the package monitor
     *
     * @param context The {@link Context} to use for monitor registration
     */
    public void init(Context context) {
        if (!mNotFoundClassesMap.isEmpty()) {
            buildInstalledPackagesCache(context);
        }

        mPackageMonitor.register(context);
    }

    /**
     * Clears the package cache and unregisteres the package monitor
     */
    public void shutdown() {
        mClassNameToClassMap.clear();
        mPackageMonitor.unregister();
    }

    /**
     * Builds a cache of installed packages.
     */
    private void buildInstalledPackagesCache(Context context) {
        final List<PackageInfo> installedPackages =
                context.getPackageManager().getInstalledPackages(0);

        for (PackageInfo installedPackage : installedPackages) {
            addInstalledPackageToCache(installedPackage.packageName);
        }
    }

    /**
     * Adds the specified package to the installed package cache.
     *
     * @param packageName The package name to add.
     */
    private void addInstalledPackageToCache(String packageName) {
        synchronized (mNotFoundClassesMap) {
            mNotFoundClassesMap.remove(packageName);
        }
    }

    /**
     * Returns a class by given <code>className</code>. The loading proceeds as
     * follows: </br> 1. Try to load with the current context class loader (it
     * caches loaded classes). </br> 2. If (1) fails try if we have loaded the
     * class before and return it if that is the cases. </br> 3. If (2) failed,
     * try to create a package context and load the class. </p> Note: If the
     * package name is null and an attempt for loading of a package context is
     * required the it is extracted from the class name.
     *
     * @param context The context from which to first try loading the class.
     * @param className The name of the class to load.
     * @param packageName The name of the package to which the class belongs.
     * @return The class if loaded successfully, null otherwise.
     */
    public Class<?> loadOrGetCachedClass(Context context, CharSequence className,
            CharSequence packageName) {
        if (TextUtils.isEmpty(className)) {
            LogUtils.log(this, Log.DEBUG, "Missing class name. Failed to load class.");
            return null;
        }

        // If we don't know the package name, get it from the class name.
        if (TextUtils.isEmpty(packageName)) {
            final int lastDotIndex = TextUtils.lastIndexOf(className, '.');

            if (lastDotIndex < 0) {
                LogUtils.log(this, Log.DEBUG, "Missing package name. Failed to load class: %s",
                        className);
                return null;
            }

            packageName = TextUtils.substring(className, 0, lastDotIndex);
        }

        final String classNameStr = className.toString();
        final String packageNameStr = packageName.toString();

        // If we failed loading this class once, don't bother trying again.
        HashSet<String> notFoundClassesSet = null;
        synchronized (mNotFoundClassesMap) {
            notFoundClassesSet = mNotFoundClassesMap.get(packageNameStr);
            if ((notFoundClassesSet != null) && notFoundClassesSet.contains(classNameStr)) {
                return null;
            }
        }

        // See if we have a cached class.
        final Class<?> clazz = mClassNameToClassMap.get(classNameStr);
        if (clazz != null) {
            return clazz;
        }

        // Try the current ClassLoader.
        try {
            final Class<?> insideClazz = getClass().getClassLoader().loadClass(classNameStr);
            if (insideClazz != null) {
                mClassNameToClassMap.put(classNameStr, insideClazz);
                return insideClazz;
            }
        } catch (ClassNotFoundException e) {
            // Do nothing.
        }

        // Context is required past this point.
        if (context == null) {
            return null;
        }

        // Attempt to load class by creating a package context.
        try {
            final int flags = (Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            final Context packageContext = context.createPackageContext(packageNameStr, flags);
            final Class<?> outsideClazz = packageContext.getClassLoader().loadClass(classNameStr);

            if (outsideClazz != null) {
                mClassNameToClassMap.put(classNameStr, outsideClazz);
                return outsideClazz;
            }
        } catch (Exception e) {
            LogUtils.log(this, Log.ERROR, "Error encountered. Failed to load outside class: %s",
                    classNameStr);
        }

        if (notFoundClassesSet == null) {
            notFoundClassesSet = new HashSet<String>();
            mNotFoundClassesMap.put(packageNameStr, notFoundClassesSet);
        }

        notFoundClassesSet.add(classNameStr);

        LogUtils.log(Log.DEBUG, "Failed to load class: %s", classNameStr);

        return null;
    }

    /**
     * Returns whether a target class is an instance of a reference class.
     * <p>
     * If a class cannot be loaded by the default {@link ClassLoader}, this
     * method will attempt to use the loader for the specified app package.
     * </p>
     */
    public boolean checkInstanceOf(Context context, CharSequence targetClassName,
            CharSequence loaderPackage, CharSequence referenceClassName) {
        if ((targetClassName == null) || (referenceClassName == null)) {
            return false;
        }

        // Try a shortcut for efficiency.
        if (TextUtils.equals(targetClassName, referenceClassName)) {
            return true;
        }

        final Class<?> referenceClass = loadOrGetCachedClass(
                context, referenceClassName, loaderPackage);
        if (referenceClass == null) {
            return false;
        }

        return checkInstanceOf(context, targetClassName, loaderPackage, referenceClass);
    }

    /**
     * Returns whether a target class is an instance of a reference class.
     * <p>
     * If a class cannot be loaded by the default {@link ClassLoader}, this
     * method will attempt to use the loader for the specified app package.
     * </p>
     */
    public boolean checkInstanceOf(Context context, CharSequence targetClassName,
            CharSequence loaderPackage, Class<?> referenceClass) {
        if ((targetClassName == null) || (referenceClass == null)) {
            return false;
        }

        final Class<?> targetClass = loadOrGetCachedClass(context, targetClassName, loaderPackage);
        if (targetClass == null) {
            return false;
        }

        return referenceClass.isAssignableFrom(targetClass);
    }

    /**
     * Monitor to keep track of the installed packages.
     */
    private final BasePackageMonitor mPackageMonitor = new BasePackageMonitor() {
        @Override
        protected void onPackageAdded(String packageName) {
            addInstalledPackageToCache(packageName);
        }

        @Override
        protected void onPackageRemoved(String packageName) {
            // Do nothing.
        }

        @Override
        protected void onPackageChanged(String packageName) {
            // Do nothing.
        }
    };
}

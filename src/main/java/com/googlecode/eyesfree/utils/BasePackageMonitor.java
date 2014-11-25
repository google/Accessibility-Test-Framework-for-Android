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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

/**
 * Helper class for monitoring packages on the system.
 *
 * @author svetoslavganov@google.com (Svetoslav R. Ganov)
 * @author alanv@google.com (Alan Viverette)
 */
public abstract class BasePackageMonitor extends BroadcastReceiver {

    /**
     * The intent filter to match package modifications.
     */
    private final IntentFilter mPackageFilter;

    /**
     * The context in which this monitor is registered.
     */
    private Context mRegisteredContext;

    /**
     * Creates a new instance.
     */
    public BasePackageMonitor() {
        mPackageFilter = new IntentFilter();
        mPackageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        mPackageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        mPackageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        mPackageFilter.addDataScheme("package");
    }

    /**
     * Register this monitor via the given <code>context</code>. Throws an
     * {@link IllegalStateException} if this monitor was already registered.
     */
    public void register(Context context) {
        if (mRegisteredContext != null) {
            throw new IllegalStateException("Already registered");
        }

        mRegisteredContext = context;
        context.registerReceiver(this, mPackageFilter);
    }

    /**
     * Unregister this monitor. Throws an {@link IllegalStateException} if this
     * monitor wasn't registered.
     */
    public void unregister() {
        if (mRegisteredContext == null) {
            throw new IllegalStateException("Not registered");
        }

        mRegisteredContext.unregisterReceiver(this);
        mRegisteredContext = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String packageName = getPackageName(intent);
        final String action = intent.getAction();

        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            onPackageAdded(packageName);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            onPackageRemoved(packageName);
        } else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
            onPackageChanged(packageName);
        }
    }

    /**
     * @return The name of the package from an <code>intent</code>.
     */
    private static String getPackageName(Intent intent) {
        final Uri uri = intent.getData();

        if (uri == null) {
            return null;
        }

        return uri.getSchemeSpecificPart();
    }

    /**
     * Called when a new application package has been installed on the device.
     *
     * @param packageName The name of the package that was added.
     */
    protected abstract void onPackageAdded(String packageName);

    /**
     * Called when an existing application package has been removed from the
     * device.
     *
     * @param packageName The name of the package that was removed.
     */
    protected abstract void onPackageRemoved(String packageName);

    /**
     * Called when an existing application package has been changed (e.g. a
     * component has been disabled or enabled).
     *
     * @param packageName The name of the package that was changed.
     */
    protected abstract void onPackageChanged(String packageName);
}

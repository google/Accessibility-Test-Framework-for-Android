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

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;

public class NodeFocusFinder {
    public static final int SEARCH_FORWARD = 1;
    public static final int SEARCH_BACKWARD = -1;

    /**
     * Perform in-order navigation from a given node in a particular direction.
     *
     * @param node The starting node.
     * @param direction The direction to travel.
     * @return The next node in the specified direction, or {@code null} if
     *         there are no more nodes.
     */
    public static AccessibilityNodeInfoCompat focusSearch(
            AccessibilityNodeInfoCompat node, int direction) {
        final AccessibilityNodeInfoRef ref = AccessibilityNodeInfoRef.unOwned(node);

        switch (direction) {
            case SEARCH_FORWARD: {
                if (!ref.nextInOrder()) {
                    return null;
                }
                return ref.release();
            }
            case SEARCH_BACKWARD: {
                if (!ref.previousInOrder()) {
                    return null;
                }
                return ref.release();
            }
        }

        return null;
    }
}

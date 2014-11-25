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
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;

import java.util.LinkedList;

/**
 * Filters {@link AccessibilityNodeInfoCompat}s.
 */
public abstract class NodeFilter {
    /**
     * Returns whether the specified node matches the filter.
     *
     * @param context The parent context.
     * @param node The node info to filter.
     * @return {@code true} if the node is accepted.
     */
    public abstract boolean accept(Context context, AccessibilityNodeInfoCompat node);

    /**
     * Returns the logical AND of this and the specified filter.
     *
     * @param filter The filter to AND this filter with.
     * @return A filter where calling <code>accept()</code> returns the result of
     *         <code>(this.accept() &amp;&amp; filter.accept())</code>.
     */
    public NodeFilter and(NodeFilter filter) {
        if (filter == null) {
            return this;
        }

        return new NodeFilterAnd(this, filter);
    }

    /**
     * Returns the logical OR of this and the specified filter.
     *
     * @param filter The filter to OR this filter with.
     * @return A filter where calling <code>accept()</code> returns the result of
     *         <code>(this.accept() || filter.accept())</code>.
     */
    public NodeFilter or(NodeFilter filter) {
        if (filter == null) {
            return this;
        }

        return new NodeFilterOr(this, filter);
    }

    private static class NodeFilterAnd extends NodeFilter {
        private final LinkedList<NodeFilter> mFilters = new LinkedList<NodeFilter>();

        public NodeFilterAnd(NodeFilter lhs, NodeFilter rhs) {
            mFilters.add(lhs);
            mFilters.add(rhs);
        }

        @Override
        public boolean accept(Context context, AccessibilityNodeInfoCompat node) {
            for (NodeFilter filter : mFilters) {
                if (!filter.accept(context, node)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public NodeFilter and(NodeFilter filter) {
            mFilters.add(filter);

            return this;
        }
    }

    private static class NodeFilterOr extends NodeFilter {
        private final LinkedList<NodeFilter> mFilters = new LinkedList<NodeFilter>();

        public NodeFilterOr(NodeFilter lhs, NodeFilter rhs) {
            mFilters.add(lhs);
            mFilters.add(rhs);
        }

        @Override
        public boolean accept(Context context, AccessibilityNodeInfoCompat node) {
            for (NodeFilter filter : mFilters) {
                if (filter.accept(context, node)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public NodeFilter or(NodeFilter filter) {
            mFilters.add(filter);

            return this;
        }
    }
}
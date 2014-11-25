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
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;

/**
 * Utility class for sending commands to ChromeVox.
 *
 * @author caseyburkhardt@google.com (Casey Burkhardt)
 */
public class WebInterfaceUtils {
    /**
     * If injection of accessibility enhancing JavaScript screen-reader is
     * enabled.
     * <p>
     * This property represents a boolean value encoded as an integer (1 is
     * true, 0 is false).
     */
    private static final String ACCESSIBILITY_SCRIPT_INJECTION = "accessibility_script_injection";

    /**
     * Direction constant for forward movement within a page.
     */
    public static final int DIRECTION_FORWARD = 1;

    /**
     * Direction constant for backward movement within a page.
     */
    public static final int DIRECTION_BACKWARD = -1;

    /**
     * Action argument to use with
     * {@link #performSpecialAction(AccessibilityNodeInfoCompat, int)} to
     * instruct ChromeVox to read the currently focused element within the node.
     * within the page.
     */
    public static final int ACTION_READ_CURRENT_HTML_ELEMENT = -1;

    /**
     * Action argument to use with
     * {@link #performSpecialAction(AccessibilityNodeInfoCompat, int)} to
     * instruct ChromeVox to read the title of the page within the node.
     */
    public static final int ACTION_READ_PAGE_TITLE_ELEMENT = -2;

    /**
     * Action argument to use with
     * {@link #performSpecialAction(AccessibilityNodeInfoCompat, int)} to
     * instruct ChromeVox to stop all speech and automatic actions.
     */
    public static final int ACTION_STOP_SPEECH = -3;

    /**
     * Action argument to use with
     * {@link #performSpecialAction(AccessibilityNodeInfoCompat, int, int)} to
     * instruct ChromeVox to move into or out of the special content navigation
     * mode.
     * <p>
     * Using this constant also requires specifying a direction.
     * {@link #DIRECTION_FORWARD} indicates ChromeVox should move into this
     * content navigation mode, {@link #DIRECTION_BACKWARD} indicates ChromeVox
     * should move out of this mode.
     */
    private static final int ACTION_TOGGLE_SPECIAL_CONTENT = -4;

    /**
     * Action argument to use with
     * {@link #performSpecialAction(AccessibilityNodeInfoCompat, int, int)} to
     * instruct ChromeVox to move into or out of the incremental search mode.
     * <p>
     * Using this constant does not require a direction as it only toggles
     * the state.
     */
    public static final int ACTION_TOGGLE_INCREMENTAL_SEARCH = -5;

    /**
     * HTML element argument to use with
     * {@link #performNavigationToHtmlElementAction(AccessibilityNodeInfoCompat,
     * int, String)} to instruct ChromeVox to move to the next or previous page
     * section.
     */
    public static final String HTML_ELEMENT_MOVE_BY_SECTION = "SECTION";

    /**
     * HTML element argument to use with
     * {@link #performNavigationToHtmlElementAction(AccessibilityNodeInfoCompat,
     * int, String)} to instruct ChromeVox to move to the next or previous list.
     */
    public static final String HTML_ELEMENT_MOVE_BY_LIST = "LIST";

    /**
     * HTML element argument to use with
     * {@link #performNavigationToHtmlElementAction(AccessibilityNodeInfoCompat,
     * int, String)} to instruct ChromeVox to move to the next or previous
     * control.
     */
    public static final String HTML_ELEMENT_MOVE_BY_CONTROL = "CONTROL";

    /**
     * Sends an instruction to ChromeVox to read the specified HTML element in
     * the given direction within a node.
     * <p>
     * WARNING: Calling this method with a source node of
     * {@link android.webkit.WebView} has the side effect of closing the IME
     * if currently displayed.
     *
     * @param node The node containing web content with ChromeVox to which the
     *            message should be sent
     * @param direction {@link #DIRECTION_FORWARD} or
     *            {@link #DIRECTION_BACKWARD}
     * @param htmlElement The HTML tag to send
     * @return {@code true} if the action was performed, {@code false}
     *         otherwise.
     */
    public static boolean performNavigationToHtmlElementAction(
            AccessibilityNodeInfoCompat node, int direction, String htmlElement) {
        final int action = (direction == DIRECTION_FORWARD)
                ? AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT
                : AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT;
        final Bundle args = new Bundle();
        args.putString(
                AccessibilityNodeInfoCompat.ACTION_ARGUMENT_HTML_ELEMENT_STRING, htmlElement);
        return node.performAction(action, args);
    }

    /**
     * Sends an instruction to ChromeVox to navigate by DOM object in
     * the given direction within a node.
     *
     * @param node The node containing web content with ChromeVox to which the
     *            message should be sent
     * @param direction {@link #DIRECTION_FORWARD} or
     *            {@link #DIRECTION_BACKWARD}
     * @return {@code true} if the action was performed, {@code false}
     *         otherwise.
     */
    public static boolean performNavigationByDOMObject(
            AccessibilityNodeInfoCompat node, int direction) {
        final int action = (direction == DIRECTION_FORWARD)
                ? AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT
                : AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT;
        return node.performAction(action);
    }

    /**
     * Sends an instruction to ChromeVox to move within a page at a specified
     * granularity in a given direction.
     * <p>
     * WARNING: Calling this method with a source node of
     * {@link android.webkit.WebView} has the side effect of closing the IME
     * if currently displayed.
     *
     * @param node The node containing web content with ChromeVox to which the
     *            message should be sent
     * @param direction {@link #DIRECTION_FORWARD} or
     *            {@link #DIRECTION_BACKWARD}
     * @param granularity The granularity with which to move or a special case argument.
     * @return {@code true} if the action was performed, {@code false} otherwise.
     */
    public static boolean performNavigationAtGranularityAction(
            AccessibilityNodeInfoCompat node, int direction, int granularity) {
        final int action = (direction == DIRECTION_FORWARD)
                ? AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY
                : AccessibilityNodeInfoCompat.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY;
        final Bundle args = new Bundle();
        args.putInt(
                AccessibilityNodeInfoCompat.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT, granularity);
        return node.performAction(action, args);
    }

    /**
     * Sends instruction to ChromeVox to perform one of the special actions
     * defined by the ACTION constants in this class.
     * <p>
     * WARNING: Calling this method with a source node of
     * {@link android.webkit.WebView} has the side effect of closing the IME if
     * currently displayed.
     *
     * @param node The node containing web content with ChromeVox to which the
     *            message should be sent
     * @param action The ACTION constant in this class match the special action
     *            that ChromeVox should perform.
     * @return {@code true} if the action was performed, {@code false}
     *         otherwise.
     */
    public static boolean performSpecialAction(AccessibilityNodeInfoCompat node, int action) {
        return performSpecialAction(node, action, DIRECTION_FORWARD);
    }

    /**
     * Sends instruction to ChromeVox to perform one of the special actions
     * defined by the ACTION constants in this class.
     * <p>
     * WARNING: Calling this method with a source node of
     * {@link android.webkit.WebView} has the side effect of closing the IME if
     * currently displayed.
     *
     * @param node The node containing web content with ChromeVox to which the
     *            message should be sent
     * @param action The ACTION constant in this class match the special action
     *            that ChromeVox should perform.
     * @param direction The DIRECTION constant in this class to add as an extra
     *            argument to the special action.
     * @return {@code true} if the action was performed, {@code false}
     *         otherwise.
     */
    public static boolean performSpecialAction(
            AccessibilityNodeInfoCompat node, int action, int direction) {
        /*
         * We use performNavigationAtGranularity to communicate with ChromeVox
         * for these actions because it is side-effect-free. If we use
         * performNavigationToHtmlElementAction and ChromeVox isn't injected,
         * we'll actually move selection within the fallback implementation. We
         * use the granularity field to hold a value that ChromeVox interprets
         * as a special command.
         */
        return performNavigationAtGranularityAction(node, direction, action /* fake granularity */);
    }

    /**
     * Sends a message to ChromeVox indicating that it should enter or exit
     * special content navigation. This is applicable for things like tables and
     * math expressions.
     * <p>
     * NOTE: further navigation should occur at the default movement
     * granularity.
     *
     * @param node The node representing the web content
     * @param enabled Whether this mode should be entered or exited
     * @return {@code true} if the action was performed, {@code false}
     *         otherwise.
     */
    public static boolean setSpecialContentModeEnabled(
            AccessibilityNodeInfoCompat node, boolean enabled) {
        final int direction = (enabled) ? DIRECTION_FORWARD : DIRECTION_BACKWARD;
        return performSpecialAction(node, ACTION_TOGGLE_SPECIAL_CONTENT, direction);
    }

    /**
     * Determines whether or not the given node contains web content.
     *
     * @param node The node to evaluate
     * @return {@code true} if the node contains web content, {@code false} otherwise
     */
    public static boolean supportsWebActions(AccessibilityNodeInfoCompat node) {
        return AccessibilityNodeInfoUtils.supportsAnyAction(node,
                AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT,
                AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT);
    }

    /**
     * Determines whether or not the given node contains native web content (and not ChromeVox).
     *
     * @param node The node to evaluate
     * @return {@code true} if the node contains native web content, {@code false} otherwise
     */
    public static boolean hasNativeWebContent(AccessibilityNodeInfoCompat node) {
        if (node == null) {
            return false;
        }

        if (!supportsWebActions(node)) {
            return false;
        }

        // ChromeVox does not have sub elements, so if the parent element also has web content
        // this cannot be ChromeVox.
        AccessibilityNodeInfoCompat parent = node.getParent();
        if (supportsWebActions(parent)) {
            if (parent != null) {
                parent.recycle();
            }
            return true;
        }

        if (parent != null) {
            parent.recycle();
        }

        // ChromeVox never has child elements
        return node.getChildCount() > 0;
    }

    /**
     * Determines whether or not the given node contains ChromeVox content.
     *
     * @param node The node to evaluate
     * @return {@code true} if the node contains ChromeVox content, {@code false} otherwise
     */
    public static boolean hasLegacyWebContent(AccessibilityNodeInfoCompat node) {
        if (node == null) {
            return false;
        }

        if (!supportsWebActions(node)) {
            return false;
        }

        // ChromeVox does not have sub elements, so if the parent element also has web content
        // this cannot be ChromeVox.
        AccessibilityNodeInfoCompat parent = node.getParent();
        if (supportsWebActions(parent)) {
            if (parent != null) {
                parent.recycle();
            }

            return false;
        }

        if (parent != null) {
            parent.recycle();
        }

        // ChromeVox never has child elements
        return node.getChildCount() == 0;
    }

    /**
     * @return {@code true} if the user has explicitly enabled injection of
     *         accessibility scripts into web content.
     */
    public static boolean isScriptInjectionEnabled(Context context) {
        final int injectionSetting = Settings.Secure.getInt(
                context.getContentResolver(), ACCESSIBILITY_SCRIPT_INJECTION, 0);
        return (injectionSetting == 1);
    }

    /**
     * Returns whether the given node has navigable web content, either legacy (ChromeVox) or native
     * web content.
     *
     * @param context The parent context.
     * @param node The node to check for web content.
     * @return Whether the given node has navigable web content.
     */
    public static boolean hasNavigableWebContent(
            Context context, AccessibilityNodeInfoCompat node) {
        return (supportsWebActions(node) && isScriptInjectionEnabled(context))
                || hasNativeWebContent(node);
    }
}

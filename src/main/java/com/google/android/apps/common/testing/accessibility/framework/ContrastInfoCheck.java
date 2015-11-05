/*
 * Copyright (C) 2015 Google Inc.
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

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.eyesfree.utils.AccessibilityNodeInfoUtils;
import com.googlecode.eyesfree.utils.ContrastSwatch;
import com.googlecode.eyesfree.utils.ContrastUtils;
import com.googlecode.eyesfree.utils.NodeFilter;
import com.googlecode.eyesfree.utils.ScreenshotUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks to ensure that certain eligible items on-screen items have sufficient contrast. This check
 * uses screen capture data to heuristically evaluate foreground/background color contrast ratios.
 */
public class ContrastInfoCheck extends AccessibilityInfoHierarchyCheck {

  private static final NodeFilter FILTER_CONTRAST_EVAL_ELIGIBLE = new NodeFilter() {

    @Override
    public boolean accept(Context context, AccessibilityNodeInfoCompat node) {
      boolean isText =
          AccessibilityNodeInfoUtils.nodeMatchesAnyClassByType(context, node, TextView.class);
      boolean isImage =
          AccessibilityNodeInfoUtils.nodeMatchesAnyClassByType(context, node, ImageView.class);
      boolean hasText = !TextUtils.isEmpty(node.getText());
      boolean isVisible = AccessibilityNodeInfoUtils.isVisibleOrLegacy(node);

      return isVisible && ((isText && hasText) || isImage);
    }
  };

  private static final NodeFilter FILTER_CONTRAST_EVAL_INELIGIBLE = new NodeFilter() {

    @Override
    public boolean accept(Context context, AccessibilityNodeInfoCompat node) {
      return !FILTER_CONTRAST_EVAL_ELIGIBLE.accept(context, node);
    }
  };

  @Override
  public List<AccessibilityInfoCheckResult> runCheckOnInfoHierarchy(AccessibilityNodeInfo root,
      Context context, Bundle metadata) {
    List<AccessibilityInfoCheckResult> results = new ArrayList<AccessibilityInfoCheckResult>();
    Bitmap screenCapture = null;
    if (metadata != null) {
      screenCapture =
          metadata.getParcelable(AccessibilityCheckMetadata.METADATA_KEY_SCREEN_CAPTURE_BITMAP);
    }

    if (screenCapture == null) {
      results.add(new AccessibilityInfoCheckResult(getClass(), AccessibilityCheckResultType.NOT_RUN,
          "This check did not execute because it was unable to obtain screen capture data.", null));
      return results;
    }

    AccessibilityNodeInfoCompat rootCompat = new AccessibilityNodeInfoCompat(root);
    List<AccessibilityNodeInfoCompat> candidates = AccessibilityNodeInfoUtils.searchAllFromBfs(
        context, rootCompat, FILTER_CONTRAST_EVAL_ELIGIBLE);
    List<AccessibilityNodeInfoCompat> nonCandidates = AccessibilityNodeInfoUtils.searchAllFromBfs(
        context, rootCompat, FILTER_CONTRAST_EVAL_INELIGIBLE);

    // Ineligible nodes all receive NOT_RUN results
    for (AccessibilityNodeInfoCompat nonCandidate : nonCandidates) {
      AccessibilityNodeInfo unwrappedNonCandidate = (AccessibilityNodeInfo) nonCandidate.getInfo();
      results.add(new AccessibilityInfoCheckResult(getClass(), AccessibilityCheckResultType.NOT_RUN,
          "This view's contrast was not evaluated because it contains neither text nor an image.",
          unwrappedNonCandidate));
    }

    Rect screenCaptureBounds =
        new Rect(0, 0, screenCapture.getWidth() - 1, screenCapture.getHeight() - 1);
    for (AccessibilityNodeInfoCompat candidate : candidates) {
      AccessibilityNodeInfo unwrappedCandidate = (AccessibilityNodeInfo) candidate.getInfo();
      Rect viewBounds = new Rect();
      unwrappedCandidate.getBoundsInScreen(viewBounds);
      if (!screenCaptureBounds.contains(viewBounds)) {
        // If an off-screen view reports itself as visible, we shouldn't evaluate it.
        String message = String.format(
            "View bounds %1$s were not within the screen capture bounds %2$s.", viewBounds,
            screenCaptureBounds);
        results.add(new AccessibilityInfoCheckResult(getClass(),
            AccessibilityCheckResultType.NOT_RUN, message, unwrappedCandidate));
        continue;
      }
      ContrastSwatch candidateSwatch = new ContrastSwatch(
          ScreenshotUtils.cropBitmap(screenCapture, viewBounds), viewBounds,
          unwrappedCandidate.getViewIdResourceName());
      double contrastRatio = candidateSwatch.getContrastRatio();
      if (AccessibilityNodeInfoUtils.nodeMatchesAnyClassByType(context, candidate,
          TextView.class)) {
        if (contrastRatio < ContrastUtils.CONTRAST_RATIO_WCAG_LARGE_TEXT) {
          String message = String.format("This view's foreground to background contrast ratio "
              + "(%1$.2f) is not sufficient.", contrastRatio);
          results.add(new AccessibilityInfoCheckResult(getClass(),
              AccessibilityCheckResultType.ERROR, message, unwrappedCandidate));
        } else if (contrastRatio < ContrastUtils.CONTRAST_RATIO_WCAG_NORMAL_TEXT) {
          String message = String.format("This view's foreground to background contrast ratio "
              + "(%1$.2f) may not be sufficient unless it contains large text.", contrastRatio);
          results.add(new AccessibilityInfoCheckResult(getClass(),
              AccessibilityCheckResultType.WARNING, message, unwrappedCandidate));
        }
      } else if (AccessibilityNodeInfoUtils.nodeMatchesAnyClassByType(context, candidate,
          ImageView.class)) {
        // Lower confidence in heuristics for ImageViews, so we'll report only warnings and use
        // the more permissive threshold ratio since images are generally large.
        if (contrastRatio < ContrastUtils.CONTRAST_RATIO_WCAG_LARGE_TEXT) {
          String message = String.format("This image's foreground to background contrast ratio "
              + "(%1$.2f) is not sufficient.  NOTE: This test is experimental and may be less "
              + "accurate for some images.", contrastRatio);
          results.add(new AccessibilityInfoCheckResult(getClass(),
              AccessibilityCheckResultType.WARNING, message, unwrappedCandidate));
        }
      }
      candidateSwatch.recycle();
    }

    AccessibilityNodeInfoUtils.recycleNodes(candidates);
    AccessibilityNodeInfoUtils.recycleNodes(nonCandidates);
    return results;
  }
}

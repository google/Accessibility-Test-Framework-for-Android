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
package com.google.android.apps.common.testing.accessibility.framework.integrations.espresso;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import androidx.test.platform.io.PlatformTestStorage;
import androidx.test.services.storage.TestStorage;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPresetAndroid;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultDescriptor;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ViewChecker;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.BitmapImage;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.Image;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hamcrest.Matcher;

/**
 * A configurable executor for the {@link AccessibilityViewHierarchyCheck}s designed for use with
 * Espresso or Robolectric tests. Clients can call {@link #checkAndReturnResults} on a {@link View}
 * to run all of the checks with the options specified in this object.
 */
public final class AccessibilityValidator {

  private static final String TAG = "AccessibilityValidator";


  private AccessibilityCheckPreset preset = AccessibilityCheckPreset.LATEST;
  private boolean runChecksFromRootView = false;

  @VisibleForTesting Screenshotter screenshotter = new Screenshotter();
  @VisibleForTesting BitmapWriter bitmapWriter = new BitmapWriter();
  private @MonotonicNonNull PlatformTestStorage testStorage;

  private boolean captureScreenshots = true;
  private @Nullable Boolean saveScreenshots;
  private @Nullable Boolean saveViewImages;
  private int screenshotsCaptured = 0;

  private @Nullable AccessibilityCheckResultType throwExceptionFor =
      AccessibilityCheckResultType.ERROR;

  private AccessibilityCheckResultDescriptor resultDescriptor =
      new AccessibilityCheckResultDescriptor();

  /** TextView.addExtraDataToAccessibilityNodeInfo throws NPE when shadows are used. */
  private static final ViewChecker viewChecker =
      new ViewChecker().setObtainCharacterLocations(!isRobolectric());

  private @Nullable Matcher<? super AccessibilityViewCheckResult> suppressingMatcher = null;
  private final List<AccessibilityCheckListener> checkListeners = new ArrayList<>();
  private final List<CheckResultsListener> checkResultsListeners = new ArrayList<>();
  private Parameters parameters = new Parameters();

  public AccessibilityValidator() {
  }

  /**
   * Runs accessibility checks with default parameters. The default parameters can be set using
   * {@link #setParameters(Parameters)}.
   *
   * @param view the {@link View} to check
   */
  public final void check(View view) {
    check(view, parameters);
  }

  /**
   * Runs accessibility checks.
   *
   * @param view the {@link View} to check
   * @param parameters supplemental input data and preferences
   */
  public final void check(View view, Parameters parameters) {
    ImmutableList<AccessibilityViewCheckResult> unused = checkAndReturnResults(view, parameters);
  }

  /**
   * Runs accessibility checks with default parameters and returns the list of results. If the
   * result is not needed, call {@link #check(View)} instead. The default parameters can be set
   * using {@link #setParameters(Parameters)}.
   *
   * @param view the {@link View} to check
   * @return an immutable list of the resulting {@link AccessibilityViewCheckResult}s
   */
  public final List<AccessibilityViewCheckResult> checkAndReturnResults(View view) {
    return checkAndReturnResults(view, parameters);
  }

  /**
   * Runs accessibility checks and returns the list of results. If the result is not needed, call
   * {@link #check(View, Parameters)} instead.
   *
   * @param view the {@link View} to check
   * @param parameters supplemental input data and preferences
   * @return an immutable list of the resulting {@link AccessibilityViewCheckResult}s
   */
  private final ImmutableList<AccessibilityViewCheckResult> checkAndReturnResults(
      View view, Parameters parameters) {
    checkNotNull(view);
    checkNotNull(parameters);

    View viewToCheck = runChecksFromRootView ? view.getRootView() : view;
    return runAccessibilityChecks(viewToCheck, parameters);
  }


  /**
   * Specify the set of checks to be run. The default is {link AccessibilityCheckPreset.LATEST}.
   *
   * @param preset The preset specifying the group of checks to run.
   * @return this
   */
  @CanIgnoreReturnValue
  public AccessibilityValidator setCheckPreset(AccessibilityCheckPreset preset) {
    this.preset = preset;
    return this;
  }

  /**
   * @param runChecksFromRootView {@code true} to check all views in the hierarchy, {@code false} to
   *     check only views in the hierarchy rooted at the passed in view. Default: {@code false}
   * @return this
   */
  @CanIgnoreReturnValue
  public AccessibilityValidator setRunChecksFromRootView(boolean runChecksFromRootView) {
    this.runChecksFromRootView = runChecksFromRootView;
    return this;
  }

  /**
   * Specifies a preference for whether screenshots should be captured. When enabled, a screenshot
   * will be captured each time {@link #checkAndReturnResults} is called, and the screenshot will be
   * provided to the ATF checks. This allows more through testing by some checks - for example, in
   * heavyweight contrast checking - but incurs additional overhead.
   *
   * <p>Default: {@code true}
   *
   * @return this
   * @see #setSaveImages(boolean, boolean)
   */
  @CanIgnoreReturnValue
  public AccessibilityValidator setCaptureScreenshots(boolean capture) {
    captureScreenshots = capture;
    return this;
  }

  /**
   * Specify a preference for whether screenshots and images of Views that produce results should be
   * retained after check evaluation. These can be useful for debugging, but produce more test
   * artifacts.
   *
   * <p>This is syntactic sugar for {@link #setSaveImages(boolean, boolean)}.
   */
  @CanIgnoreReturnValue
  public AccessibilityValidator setSaveImages(boolean save) {
    return setSaveImages(save, save);
  }

  /**
   * Specify a preference for whether screenshots and images of Views that produce results should be
   * retained after check evaluation. These can be useful for debugging, but produce more test
   * artifacts. These settings have no effect unless screenshot capture has been enabled.
   *
   * @param saveScreenshots whether screenshots should be saved after evaluation. By default, these
   *     images are saved when the checks produce any result other than {@code NOT_RUN}.
   * @param saveViewImages whether an image should be saved of each View to which heavyweight
   *     contrast checking is applied. By default, these images are saved when the checks produce
   *     findings based on the images.
   */
  @CanIgnoreReturnValue
  public AccessibilityValidator setSaveImages(boolean saveScreenshots, boolean saveViewImages) {
    this.saveScreenshots = saveScreenshots;
    this.saveViewImages = saveViewImages;
    return this;
  }

  /**
   * Suppresses all results that match the given matcher. Suppressed results will not be included in
   * any logs or cause any {@code Exception} to be thrown
   *
   * @param resultMatcher a matcher that specifies result to be suppressed. If {@code null}, then
   *     any previously set matcher will be removed and the default behavior will be restored.
   * @return this
   */
  @CanIgnoreReturnValue
  public AccessibilityValidator setSuppressingResultMatcher(
      @Nullable Matcher<? super AccessibilityViewCheckResult> resultMatcher) {
    suppressingMatcher = resultMatcher;
    return this;
  }

  /**
   * @param throwExceptionForErrors {@code true} to throw an {@code Exception} when there is at
   *     least one error result, {@code false} to just log the error results to logcat. Default:
   *     {@code true}
   * @return this
   * @deprecated Use {@link #setThrowExceptionFor}
   */
  @CanIgnoreReturnValue
  @Deprecated
  public AccessibilityValidator setThrowExceptionForErrors(boolean throwExceptionForErrors) {
    return setThrowExceptionFor(
        throwExceptionForErrors ? AccessibilityCheckResultType.ERROR : null);
  }

  /**
   * Specifies the types of results that should produce a thrown exception.
   *
   * <ul>
   *   If the value is:
   *   <li>{@link AccessibilityCheckResultType#ERROR}, an exception will be thrown for any ERROR
   *   <li>{@link AccessibilityCheckResultType#WARNING}, an exception will be thrown for any ERROR
   *       or WARNING
   *   <li>{@link AccessibilityCheckResultType#INFO}, an exception will be thrown for any ERROR,
   *       WARNING or INFO
   *   <li>{@code null}, no exception will be thrown
   * </ul>
   *
   * The default is {@code ERROR}.
   *
   * @return this
   */
  @CanIgnoreReturnValue
  public AccessibilityValidator setThrowExceptionFor(
      @Nullable AccessibilityCheckResultType throwFor) {
    checkArgument(
        (throwFor == AccessibilityCheckResultType.ERROR)
            || (throwFor == AccessibilityCheckResultType.WARNING)
            || (throwFor == AccessibilityCheckResultType.INFO)
            || (throwFor == null),
        "Argument was %s but expected ERROR, WARNING, INFO or null.",
        throwFor);
    throwExceptionFor = throwFor;
    return this;
  }

  /**
   * Sets the {@link AccessibilityCheckResultDescriptor} that is used to convert results to readable
   * messages in exceptions and logcat statements.
   *
   * @return this
   */
  @CanIgnoreReturnValue
  public AccessibilityValidator setResultDescriptor(
      AccessibilityCheckResultDescriptor resultDescriptor) {
    this.resultDescriptor = checkNotNull(resultDescriptor);
    return this;
  }

  /**
   * Adds a listener to receive all {@link AccessibilityCheckResult}s after suppression. Listeners
   * will be called in the order they are added and before any {@link
   * AccessibilityViewCheckException} would be thrown.
   *
   * @return this
   */
  @CanIgnoreReturnValue
  public AccessibilityValidator addCheckListener(AccessibilityCheckListener listener) {
    checkNotNull(listener);
    checkListeners.add(listener);
    return this;
  }

  /**
   * Adds a listener to receive a callback after checks have been evaluated. Listeners will be
   * called in the order they are added and before any {@link AccessibilityViewCheckException} would
   * be thrown.
   *
   * @return this
   */
  @CanIgnoreReturnValue
  public AccessibilityValidator addCheckListener(CheckResultsListener listener) {
    checkResultsListeners.add(listener);
    return this;
  }

  /**
   * Sets preferences to used by when evaluating checks unless explicitly provided by argument.
   *
   * @return this
   */
  @CanIgnoreReturnValue
  public AccessibilityValidator setParameters(Parameters parameters) {
    this.parameters = parameters;
    return this;
  }


  /**
   * Runs accessibility checks on a {@code View} hierarchy
   *
   * @param view the {@link View} to check
   * @return a list of the results of the checks
   */
  private ImmutableList<AccessibilityViewCheckResult> runAccessibilityChecks(
      View view, Parameters parameters) {
    try {
      parameters = parameters.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Could not clone parameters", e);
    }
    Bitmap screenshot = null;
    if (captureScreenshots) {
      screenshot = screenshotter.getScreenshot(view.getRootView());
      if (screenshot != null) {
        parameters.putScreenCapture(new BitmapImage(screenshot));
        if ((parameters.getSaveViewImages() == null) && !Boolean.FALSE.equals(saveViewImages)) {
          parameters.setSaveViewImages(true);
        }
        screenshotsCaptured++;
      }
    }

    return processResults(
        view.getContext(),
        viewChecker.runViewChecksOnView(
            AccessibilityCheckPresetAndroid.getViewChecksForPreset(preset), view, parameters),
        screenshot);
  }

  private static boolean isRobolectric() {
    return Objects.equals(Build.FINGERPRINT, "robolectric");
  }

  /** Returns the number of times that this instance has captured a screenshot. */
  @VisibleForTesting
  int getScreenshotsCaptured() {
    return screenshotsCaptured;
  }

  /**
   * If any of the {@code results} include images of the Views associated with the results, this
   * method will write those images out to files.
   *
   * <p>The name of the output files will be "View-{R}-{S}.png" where {S} is the one-based index of
   * the screenshot taken during the test, and {R} is an identifier of the View associated with the
   * result. The identifier may be the name of the resource used to construct the View, or some
   * other string if the View, View ID or resource name cannot be determined. Since there may be
   * more than one result in a screenshot with the same View ID, a single letter ("b", "c", etc.)
   * may be appended to {R} to avoid overwritting data.
   */
  private void saveResultImages(
      PlatformTestStorage testStorage, List<AccessibilityViewCheckResult> results) {
    HashMap<String, Integer> resourceIdCounts = new HashMap<>();
    for (AccessibilityViewCheckResult result : results) {
      Image viewImage = result.getViewImage();
      if (viewImage instanceof BitmapImage) {
        Bitmap bitmap = ((BitmapImage) viewImage).getBitmap();
        String resourceId = getResourceIdentifier(result);
        Integer resourceIdCount = resourceIdCounts.get(resourceId);
        resourceIdCount = (resourceIdCount == null) ? 0 : (resourceIdCount + 1);
        resourceIdCounts.put(resourceId, resourceIdCount);
        String outputPath =
            String.format(
                Locale.ENGLISH,
                "View-%s%s-%d.png",
                resourceId,
                (((resourceIdCount > 0) && (resourceIdCount < 26))
                    ? Character.toString((char) ('a' + resourceIdCount))
                    : ""),
                screenshotsCaptured);
        bitmapWriter.write(testStorage, bitmap, outputPath);
      }
    }
  }

  /**
   * Returns a String that identifies the View associated with this result. The identifier may be
   * the name of the resource used to construct the View, or some other string if the View, View ID
   * or resource name cannot be determined.
   */
  private static String getResourceIdentifier(AccessibilityViewCheckResult result) {
    View view = result.getView();
    if (view == null) {
      return "NO_VIEW";
    }
    int viewId = view.getId();
    if ((viewId == View.NO_ID) || (viewId == 0)) {
      return "NO_ID";
    }
    if (view.getResources() != null && !isViewIdGenerated(viewId)) {
      try {
        return view.getResources().getResourceEntryName(viewId);
      } catch (Resources.NotFoundException ignore) {
        // Do nothing.
      }
    }
    return Integer.toString(viewId);
  }

  /**
   * IDs generated by {@link View#generateViewId} will fail if used as a resource ID in attempted
   * resources lookups. This now logs an error in API 28, causing test failures. This method is
   * taken from {@link View#isViewIdGenerated} to prevent resource lookup to check if a view id was
   * generated.
   */
  private static boolean isViewIdGenerated(int id) {
    return (id & 0xFF000000) == 0 && (id & 0x00FFFFFF) != 0;
  }

  /**
   * Reports the given check results. Any result matching {@link #suppressingMatcher} is replaced
   * with a copy whose type is set to SUPPRESSED.
   *
   * <ol>
   *   <li>Calls {@link AccessibilityCheckListener#onResults} for any registered listeners.
   *   <li>Throws an {@link AccessibilityViewCheckException} containing all severe results,
   *       depending on the value of {@link #throwExceptionFor}.
   *   <li>Results of type {@code INFO}, {@code WARNING} and {@code ERROR} will be logged to logcat.
   * </ol>
   *
   * @param screenshot screenshot image, if one was captured
   * @return The same values as in {@code results}, except that any result that matches {@link
   *     #suppressingMatcher} will be replaced with a copy whose type is SUPPRESSED.
   */
  @VisibleForTesting
  ImmutableList<AccessibilityViewCheckResult> processResults(
      Context context,
      ImmutableList<AccessibilityViewCheckResult> results,
      @Nullable Bitmap screenshot) {

    ImmutableList<AccessibilityViewCheckResult> processedResults =
        suppressMatchingResults(results, suppressingMatcher);
    for (AccessibilityCheckListener checkListener : checkListeners) {
      checkListener.onResults(context, processedResults);
    }

    List<AccessibilityViewCheckResult> infos =
        AccessibilityCheckResultUtils.getResultsForType(
            processedResults, AccessibilityCheckResultType.INFO);
    List<AccessibilityViewCheckResult> warnings =
        AccessibilityCheckResultUtils.getResultsForType(
            processedResults, AccessibilityCheckResultType.WARNING);
    List<AccessibilityViewCheckResult> errors =
        AccessibilityCheckResultUtils.getResultsForType(
            processedResults, AccessibilityCheckResultType.ERROR);

    List<AccessibilityViewCheckResult> severeResults = getSevereResults(errors, warnings, infos);

    String screenshotPath = null;
    CheckResultsCallback checkResultsCallback =
        CheckResultsCallback.builder()
            .setAccessibilityViewCheckResults(processedResults)
            .setScreenshotPath(screenshotPath)
            .build();
    for (CheckResultsListener checkResultsListener : checkResultsListeners) {
      checkResultsListener.onResults(checkResultsCallback);
    }

    if (!severeResults.isEmpty()) {
      throw new AccessibilityViewCheckException(severeResults, resultDescriptor);
    }

    for (AccessibilityViewCheckResult result : infos) {
      Log.i(TAG, describeResult(result));
    }
    for (AccessibilityViewCheckResult result : warnings) {
      Log.w(TAG, describeResult(result));
    }
    for (AccessibilityViewCheckResult result : errors) {
      Log.w(TAG, describeResult(result));
    }
    return processedResults;
  }

  private String describeResult(AccessibilityViewCheckResult result) {
    return resultDescriptor.describeResult(result);
  }

  /**
   * Returns a copy of the list where any result that matches the given matcher is replaced by a
   * copy of the result with the type set to {@code SUPPRESSED}.
   *
   * @param results a list of {@code AccessibilityCheckResult}s to be matched against
   * @param matcher a Matcher that determines whether a given {@code AccessibilityCheckResult}
   *     should be suppressed
   */
  @VisibleForTesting
  static ImmutableList<AccessibilityViewCheckResult> suppressMatchingResults(
      ImmutableList<AccessibilityViewCheckResult> results,
      @Nullable Matcher<? super AccessibilityViewCheckResult> matcher) {
    if (matcher == null) {
      return results;
    }

    return FluentIterable.from(results)
        .transform(result -> matcher.matches(result) ? result.getSuppressedResultCopy() : result)
        .toList();
  }

  /** Returns {@code true} iff there is any result that is not of type {@code NOT_RUN}. */
  private static boolean hasRunResult(ImmutableList<AccessibilityViewCheckResult> results) {
    for (AccessibilityViewCheckResult result : results) {
      if (result.getType() != AccessibilityCheckResultType.NOT_RUN) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the list of those results that should cause an exception to be thrown, depending upon
   * the value of {@link #throwExceptionFor}.
   */
  private List<AccessibilityViewCheckResult> getSevereResults(
      List<AccessibilityViewCheckResult> errors,
      List<AccessibilityViewCheckResult> warnings,
      List<AccessibilityViewCheckResult> infos) {
    if (throwExceptionFor != null) {
      switch (throwExceptionFor) {
        case ERROR:
          if (!errors.isEmpty()) {
            return errors;
          }
          break;
        case WARNING:
          if (!(errors.isEmpty() && warnings.isEmpty())) {
            return new ImmutableList.Builder<AccessibilityViewCheckResult>()
                .addAll(errors)
                .addAll(warnings)
                .build();
          }
          break;
        case INFO:
          if (!(errors.isEmpty() && warnings.isEmpty() && infos.isEmpty())) {
            return new ImmutableList.Builder<AccessibilityViewCheckResult>()
                .addAll(errors)
                .addAll(warnings)
                .addAll(infos)
                .build();
          }
          break;
        default:
      }
    }
    return ImmutableList.<AccessibilityViewCheckResult>of();
  }

  /** Interface for receiving callbacks when results have been obtained. */
  public static interface AccessibilityCheckListener {
    /**
     * @param results results from the evaluation of checks. This may include results whose {@code
     *     getType} returns {@code NOT_RUN} or {@code SUPPRESSED} if a suppressing result matcher
     *     was specified.
     */
    void onResults(Context context, List<? extends AccessibilityViewCheckResult> results);
  }

  /** Interface to receive a callback after checks have been evaluated. */
  public static interface CheckResultsListener {
    void onResults(CheckResultsCallback callback);
  }

  /** Utility to write a Bitmap to a test output file. */
  @VisibleForTesting
  static class BitmapWriter {
    /**
     * Writes the bitmap out to a file that will be included in the test outputs.
     *
     * <p>This is an expensive, synchronous operation performed on the UI thread. We really
     * shouldn't be doing this, but don't have any convenient alternatives.
     *
     * @return whether bitmap was successfully written to path
     */
    @CanIgnoreReturnValue
    boolean write(PlatformTestStorage testStorage, Bitmap bitmap, String path) {
      // StrictMode.permitCustomSlowCalls is needed to use Bitmap.compress. Normally, this operation
      // should not be performed on the UI thread. But it is permissible here because this code
      // should only be used for testing, and it must finish before the end of the test's lifecycle.
      StrictMode.ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
      StrictMode.setThreadPolicy(
          new StrictMode.ThreadPolicy.Builder(oldPolicy).permitCustomSlowCalls().build());
      try (BufferedOutputStream stream =
          new BufferedOutputStream(testStorage.openOutputFile(path))) {

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return true;
      } catch (IOException e) {
        Log.w(TAG, "Error writing bitmap to file", e);
        return false;
      } finally {
        StrictMode.setThreadPolicy(oldPolicy);
      }
    }
  }

}

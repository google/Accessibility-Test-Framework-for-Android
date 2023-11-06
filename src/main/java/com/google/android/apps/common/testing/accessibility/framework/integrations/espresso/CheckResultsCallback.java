package com.google.android.apps.common.testing.accessibility.framework.integrations.espresso;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Data passed to a listener after one evaluation of accessibility checks. */
@AutoValue
public abstract class CheckResultsCallback {

  /**
   * Results from the evaluation of checks. This may include results whose {@code getType} returns
   * {@code NOT_RUN} or {@code SUPPRESSED} if a suppressing result matcher was specified.
   */
  public abstract ImmutableList<AccessibilityViewCheckResult> getAccessibilityViewCheckResults();

  /** Path to a screenshot if one was captured and saved to a file. */
  public abstract @Nullable String getScreenshotPath();

  public static Builder builder() {
    return new AutoValue_CheckResultsCallback.Builder();
  }

  /** Builder for CheckResultsCallback. */
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setAccessibilityViewCheckResults(
        ImmutableList<AccessibilityViewCheckResult> results);

    public abstract Builder setScreenshotPath(@Nullable String screenshotPath);

    public abstract CheckResultsCallback build();
  }
}

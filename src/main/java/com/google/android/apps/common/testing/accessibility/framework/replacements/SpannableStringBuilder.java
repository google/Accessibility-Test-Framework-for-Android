package com.google.android.apps.common.testing.accessibility.framework.replacements;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/** A fairly lightweight local replacement for {@link android.text.SpannableStringBuilder} */
public class SpannableStringBuilder implements CharSequence {

  private static final String SEPARATOR = ", ";

  private final StringBuilder rawTextBuilder = new StringBuilder();
  private @MonotonicNonNull List<Span> spans;

  public SpannableStringBuilder() {}

  public SpannableStringBuilder append(@Nullable SpannableString string) {
    if (!TextUtils.isEmpty(string)) {
      copyAndAppendAdjustedSpans(string.getSpans(), 0);
      append(string.toString());
    }
    return this;
  }

  public SpannableStringBuilder append(@Nullable String string) {
    if (!TextUtils.isEmpty(string)) {
      rawTextBuilder.append(string);
    }
    return this;
  }

  public SpannableStringBuilder appendWithSeparator(@Nullable SpannableString string) {
    if (!TextUtils.isEmpty(string)) {
      copyAndAppendAdjustedSpans(string.getSpans(), (needsSeparator() ? SEPARATOR.length() : 0));
      appendWithSeparator(string.toString());
    }
    return this;
  }

  public SpannableStringBuilder appendWithSeparator(@Nullable String string) {
    if (!TextUtils.isEmpty(string)) {
      if (needsSeparator()) {
        append(SEPARATOR);
      }
      append(string);
    }
    return this;
  }

  public List<Span> getSpans() {
    return (spans == null) ? ImmutableList.of() : Collections.unmodifiableList(spans);
  }

  public SpannableString build() {
    return new SpannableString(rawTextBuilder.toString(), getSpans());
  }

  @Override
  public int length() {
    return rawTextBuilder.length();
  }

  @Override
  public char charAt(int index) {
    return rawTextBuilder.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return rawTextBuilder.subSequence(start, end);
  }

  /**
   * @return {@code true} if this instance's {@code rawTextBuilder} requires a separator to be
   *     appended prior to appending additional text.
   */
  private boolean needsSeparator() {
    return rawTextBuilder.length() > 0;
  }

  /**
   * Copy the provided {@code spans} into the structure for tracking span data while adjusting the
   * span positional information to match the current state of {@code rawTextBuilder}. Generally,
   * this should be called prior to appending any associated raw text into {@code rawTextBuilder}.
   *
   * @param spans the {@link Span} objects to adjust and copy
   * @param adjustment additional value by which to adjust the positional information of the added
   *     spans
   */
  private void copyAndAppendAdjustedSpans(List<Span> spans, int adjustment) {
    if (this.spans == null) {
      this.spans = new ArrayList<>();
    }

    for (Span span : spans) {
      // Create a copy with 'start' and 'end' positions adjusted to match the span's new position.
      Span adjustedSpan =
          span.copyWithAdjustedPosition(
              (span.getStart() + rawTextBuilder.length() + adjustment),
              (span.getEnd() + rawTextBuilder.length()) + adjustment);
      this.spans.add(adjustedSpan);
    }
  }
}

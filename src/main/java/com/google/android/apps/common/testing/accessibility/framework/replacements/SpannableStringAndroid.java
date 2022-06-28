/*
 * Copyright (C) 2019 Google Inc.
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

package com.google.android.apps.common.testing.accessibility.framework.replacements;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Used as a local replacement for Android's {@link android.text.SpannableString} */
public class SpannableStringAndroid extends SpannableString {

  public SpannableStringAndroid(CharSequence text) {
    super(checkNotNull(text));
    if (text instanceof android.text.Spanned) {
      // Capture span information within an Android environment
      this.spans = getSpansAndroid((android.text.Spanned) text);
    } else if (text instanceof SpannableString) {
      SpannableString spannableString = (SpannableString) text;
      this.spans = ImmutableList.<Span>copyOf(spannableString.getSpans());
    } else {
      this.spans = ImmutableList.<Span>of();
    }
  }

  /**
   * Convenience method for creating a {@link SpannableStringAndroid} from a {@link CharSequence}.
   *
   * @param source The {@link CharSequence} with which to create an instance
   * @return A {@link SpannableStringAndroid} with data matching {@code source}, or {@code null} if
   *     {@code source} is {@code null} or empty.
   */
  public static @Nullable SpannableStringAndroid valueOf(@Nullable CharSequence source) {
    if (TextUtils.isEmpty(source)) {
      return null;
    }

    if (source instanceof SpannableStringAndroid) {
      return (SpannableStringAndroid) source;
    }

    return new SpannableStringAndroid(source);
  }

  private static ImmutableList<Span> getSpansAndroid(android.text.Spanned spanned) {

    if (TextUtils.isEmpty(spanned)) {
      return ImmutableList.<Span>of();
    }

    int length = spanned.length();
    ImmutableList.Builder<Span> spansBuilder = ImmutableList.<Span>builder();
    Object[] spans = spanned.getSpans(0, (length - 1), Object.class);
    for (int i = 0; i < spans.length; i++) {
      int start = spanned.getSpanStart(spans[i]);
      int end = spanned.getSpanEnd(spans[i]);
      int flags = spanned.getSpanFlags(spans[i]);

      Span newSpan;
      // Build local replacement Spans, most specific first
      if (spans[i] instanceof android.text.NoCopySpan) {
        // Don't copy NoCopySpans
        continue;
      } else if (spans[i] instanceof android.text.style.URLSpan) {
        android.text.style.URLSpan androidUrlSpan = (android.text.style.URLSpan) spans[i];
        newSpan =
            new Spans.URLSpan(
                Spans.URLSpan.ANDROID_CLASS_NAME, start, end, flags, androidUrlSpan.getURL());
      } else if (spans[i] instanceof android.text.style.ClickableSpan) {
        newSpan =
            new Spans.ClickableSpan(Spans.ClickableSpan.ANDROID_CLASS_NAME, start, end, flags);
      } else if (spans[i] instanceof android.text.style.StyleSpan) {
        android.text.style.StyleSpan androidStyleSpan = (android.text.style.StyleSpan) spans[i];
        newSpan =
            new Spans.StyleSpan(
                Spans.StyleSpan.ANDROID_CLASS_NAME, start, end, flags, androidStyleSpan.getStyle());
      } else if (spans[i] instanceof android.text.style.UnderlineSpan) {
        newSpan =
            new Spans.UnderlineSpan(Spans.UnderlineSpan.ANDROID_CLASS_NAME, start, end, flags);
      } else {
        // Keep track of the unknown span types without creating a subclass-specific instance
        newSpan = new Span(spans[i].getClass().getName(), start, end, flags);
      }

      spansBuilder.add(newSpan);
    }
    return spansBuilder.build();
  }
}

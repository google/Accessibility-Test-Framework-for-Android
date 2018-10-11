/*
 * Copyright (C) 2017 Google Inc.
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

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import android.view.View;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.CharSequenceProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.SpanProto;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

/** Used as a local replacement for Android's {@link android.text.SpannableString} */
public class SpannableString
    implements CharSequence, Parcelable, Replaceable<android.text.SpannableString> {

  private final String rawString;
  private final ImmutableList<Span> spans;

  public SpannableString(CharSequence text) {
    this.rawString = checkNotNull(text).toString();
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

  public SpannableString(CharSequenceProto proto) {
    rawString = proto.getText();
    ImmutableList.Builder<Span> spansBuilder = ImmutableList.<Span>builder();
    for (SpanProto span : proto.getSpanList()) {
      Span localSpan;
      switch (span.getType()) {
        case URL:
          localSpan = new Spans.URLSpan(span);
          break;
        case CLICKABLE:
          localSpan = new Spans.ClickableSpan(span);
          break;
        case UNKNOWN:
          localSpan = new Span(span);
          break;
        default:
          localSpan = null;
      }

      if (localSpan != null) {
        spansBuilder.add(localSpan);
      }
    }
    this.spans = spansBuilder.build();
  }

  protected SpannableString(String rawString, List<Span> spans) {
    this.rawString = rawString;
    this.spans = ImmutableList.<Span>copyOf(spans);
  }

  /**
   * @return a {@link List} of {@link Span} objects representing markup spans annotating this {@code
   *     CharSequence}
   */
  public List<Span> getSpans() {
    return spans;
  }

  @Override
  public int length() {
    return rawString.length();
  }

  @Override
  public char charAt(int index) {
    return rawString.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return rawString.subSequence(start, end);
  }

  @Override
  public android.text.SpannableString getAndroidInstance() {
    android.text.SpannableStringBuilder ssb = new android.text.SpannableStringBuilder(rawString);
    for (Span span : spans) {
      Object androidSpan;
      // Match and replace span with actual Android instances, most specific first.
      // NOTE that only know span types are recreated
      if (span instanceof Spans.URLSpan) {
        Spans.URLSpan urlSpan = (Spans.URLSpan) span;
        androidSpan = urlSpan.getAndroidInstance();
      } else if (span instanceof Spans.ClickableSpan) {
        // ClickableSpans are abstract markers-only. ATF cannot copy ClickableSpan implementations
        androidSpan = new android.text.style.ClickableSpan() {
          @Override
          public void onClick(View view) {
            /* no implementation */
          }
        };
      } else {
        continue;
      }

      ssb.setSpan(androidSpan, span.getStart(), span.getEnd(), span.getFlags());
    }

    return android.text.SpannableString.valueOf(ssb);
  }

  @Override
  public String toString() {
    return rawString;
  }

  public CharSequenceProto toProto() {
    CharSequenceProto.Builder builder = CharSequenceProto.newBuilder();
    builder.setText(rawString);
    for (Span span : spans) {
      builder.addSpan(span.toProto());
    }

    return builder.build();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeString(rawString);
    parcel.writeInt(spans.size());
    for (Span span : spans) {
      parcel.writeParcelable(span, flags);
    }
  }

  /**
   * Convenience method for creating a {@link SpannableString} from a {@link CharSequence}.
   *
   * @param source The {@link CharSequence} with which to create an instance
   * @return A {@link SpannableString} with data matching {@code source}, or {@code null} if {@code
   *     source} is {@code null} or empty.
   */
  public static @Nullable SpannableString valueOf(@Nullable CharSequence source) {
    if (TextUtils.isEmpty(source)) {
      return null;
    }

    if (source instanceof SpannableString) {
      return (SpannableString) source;
    }

    return new SpannableString(source);
  }

  private static SpannableString readFromParcel(Parcel parcel) {
    String rawString = checkNotNull(parcel.readString());
    int spanSize = parcel.readInt();
    List<Span> spans = new ArrayList<>();
    for (int i = 0; i < spanSize; ++i) {
      spans.add(parcel.readParcelable(SpannableString.class.getClassLoader()));
    }
    return new SpannableString(rawString, spans);
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
      } else {
        // Keep track of the unknown span types without creating a subclass-specific instance
        newSpan = new Span(spans[i].getClass().getName(), start, end, flags);
      }

      spansBuilder.add(newSpan);
    }
    return spansBuilder.build();
  }

  public static final Parcelable.Creator<SpannableString> CREATOR =
      new Parcelable.Creator<SpannableString>() {
        @Override
        public SpannableString createFromParcel(Parcel in) {
          return readFromParcel(in);
        }

        @Override
        public SpannableString[] newArray(int size) {
          return new SpannableString[size];
        }
      };
}

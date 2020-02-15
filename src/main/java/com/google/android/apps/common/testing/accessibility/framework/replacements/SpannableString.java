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

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.CharSequenceProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.SpanProto;
import com.google.common.collect.ImmutableList;
import java.util.List;

/** Used as a local replacement for Android's {@link android.text.SpannableString} */
public class SpannableString implements CharSequence {

  protected final CharSequence rawString;
  protected ImmutableList<Span> spans;

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
        case STYLE:
          localSpan = new Spans.StyleSpan(span);
          break;
        case UNDERLINE:
          localSpan = new Spans.UnderlineSpan(span);
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

  public SpannableString(CharSequence rawString, List<Span> spans) {
    this.rawString = rawString;
    this.spans = ImmutableList.<Span>copyOf(spans);
  }

  protected SpannableString(CharSequence rawString) {
    this(rawString, ImmutableList.of());
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
  public String toString() {
    return rawString.toString();
  }

  public CharSequenceProto toProto() {
    CharSequenceProto.Builder builder = CharSequenceProto.newBuilder();
    builder.setText(rawString.toString());
    for (Span span : spans) {
      builder.addSpan(span.toProto());
    }

    return builder.build();
  }
}

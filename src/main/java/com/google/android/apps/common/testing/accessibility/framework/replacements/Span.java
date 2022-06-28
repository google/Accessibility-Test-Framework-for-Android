package com.google.android.apps.common.testing.accessibility.framework.replacements;

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.SpanProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.SpanProto.SpanType;
import com.google.errorprone.annotations.Immutable;

/**
 * Represents a generic markup span within a {@link CharSequence}. Specific span implementations may
 * be found in {@link Spans}
 */
@Immutable
public class Span {
  private final String spanClassName;
  private final int start;
  private final int end;
  private final int flags;

  public Span(String spanClassName, int start, int end, int flags) {
    this.spanClassName = spanClassName;
    this.start = start;
    this.end = end;
    this.flags = flags;
  }

  public Span(SpanProto proto) {
    this.spanClassName = proto.getSpanClassName();
    this.start = proto.getStart();
    this.end = proto.getEnd();
    this.flags = proto.getFlags();
  }

  public String getSpanClassName() {
    return spanClassName;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public int getFlags() {
    return flags;
  }

  public SpanProto toProto() {
    // Note that subclasses explicitly do not rely on inheritance to write properties managed by
    // this class.  This is to ensure the SpanType is properly set from each specific span.
    SpanProto.Builder builder = SpanProto.newBuilder();
    builder.setSpanClassName(spanClassName);
    builder.setStart(start);
    builder.setEnd(end);
    builder.setFlags(flags);
    builder.setType(SpanType.UNKNOWN);

    return builder.build();
  }

  protected Span copyWithAdjustedPosition(int newStart, int newEnd) {
    return new Span(spanClassName, newStart, newEnd, flags);
  }
}

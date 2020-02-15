package com.google.android.apps.common.testing.accessibility.framework.replacements;

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.SpanProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.SpanProto.SpanType;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Types of spans that are known and usable within {@link
 * com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck}
 * implementations.
 */
public final class Spans {

  /**
   * Used as a local replacement for Android's {@link android.text.style.ClickableSpan}
   *
   * <p>NOTE: Android's ClickableSpan is abstract, but this class is only used to track the presence
   * of this type of span within a CharSequence.
   */
  public static class ClickableSpan extends Span {

    public static final String ANDROID_CLASS_NAME = "android.text.style.ClickableSpan";

    public ClickableSpan(String spanClass, int start, int end, int flags) {
      // No state -- marker class only
      super(spanClass, start, end, flags);
    }

    public ClickableSpan(SpanProto proto) {
      super(proto);
    }

    @Override
    public SpanProto toProto() {
      // Explicitly not invoking super to ensure the SpanType is properly set
      SpanProto.Builder builder = SpanProto.newBuilder();
      builder.setSpanClassName(getSpanClassName());
      builder.setStart(getStart());
      builder.setEnd(getEnd());
      builder.setFlags(getFlags());
      builder.setType(SpanType.CLICKABLE);

      return builder.build();
    }

    @Override
    protected Span copyWithAdjustedPosition(int newStart, int newEnd) {
      return new ClickableSpan(getSpanClassName(), newStart, newEnd, getFlags());
    }
  }

  /** Used as a local replacement for Android's {@link android.text.style.URLSpan} */
  public static class URLSpan extends ClickableSpan {

    public static final String ANDROID_CLASS_NAME = "android.text.style.URLSpan";

    private final @Nullable String url;

    public URLSpan(String spanClass, int start, int end, int flags, @Nullable String url) {
      super(spanClass, start, end, flags);
      this.url = url;
    }

    public URLSpan(SpanProto proto) {
      super(proto);
      this.url = proto.getUrl();
    }

    /** @see android.text.style.URLSpan#getURL() */
    public @Nullable String getUrl() {
      return url;
    }

    @Override
    public SpanProto toProto() {
      // Explicitly not invoking super to ensure the SpanType is properly set
      SpanProto.Builder builder = SpanProto.newBuilder();
      builder.setSpanClassName(getSpanClassName());
      builder.setStart(getStart());
      builder.setEnd(getEnd());
      builder.setFlags(getFlags());
      builder.setType(SpanType.URL);
      String url = getUrl();
      if (url != null) {
        builder.setUrl(url);
      }
      return builder.build();
    }

    @Override
    protected Span copyWithAdjustedPosition(int newStart, int newEnd) {
      return new URLSpan(getSpanClassName(), newStart, newEnd, getFlags(), url);
    }
  }

  /** Used as a local replacement for Android's {@link android.text.style.StyleSpan} */
  public static class StyleSpan extends Span {

    public static final String ANDROID_CLASS_NAME = "android.text.style.StyleSpan";

    private final int style;

    public StyleSpan(String spanClass, int start, int end, int flags, int style) {
      super(spanClass, start, end, flags);
      this.style = style;
    }

    public StyleSpan(SpanProto proto) {
      super(proto);
      this.style = proto.getStyle();
    }

    /**
     * An integer constant describing the style for this span, as defined by constants in {@link
     * android.graphics.Typeface}
     *
     * @see android.text.style.StyleSpan#getStyle()
     */
    public int getStyle() {
      return style;
    }

    @Override
    public SpanProto toProto() {
      SpanProto.Builder builder = SpanProto.newBuilder();

      builder.setSpanClassName(getSpanClassName());
      builder.setStart(getStart());
      builder.setEnd(getEnd());
      builder.setFlags(getFlags());
      builder.setType(SpanType.STYLE);
      builder.setStyle(getStyle());

      return builder.build();
    }

    @Override
    protected Span copyWithAdjustedPosition(int newStart, int newEnd) {
      return new StyleSpan(getSpanClassName(), newStart, newEnd, getFlags(), style);
    }
  }

  /** Used as a local replacement for Android's {@link android.text.style.UnderlineSpan} */
  public static class UnderlineSpan extends Span {

    public static final String ANDROID_CLASS_NAME = "android.text.style.UnderlineSpan";

    public UnderlineSpan(String spanClass, int start, int end, int flags) {
      super(spanClass, start, end, flags);
    }

    public UnderlineSpan(SpanProto proto) {
      super(proto);
    }

    @Override
    public SpanProto toProto() {
      SpanProto.Builder builder = SpanProto.newBuilder();

      builder.setSpanClassName(getSpanClassName());
      builder.setStart(getStart());
      builder.setEnd(getEnd());
      builder.setFlags(getFlags());
      builder.setType(SpanType.UNDERLINE);

      return builder.build();
    }

    @Override
    protected Span copyWithAdjustedPosition(int newStart, int newEnd) {
      return new UnderlineSpan(getSpanClassName(), newStart, newEnd, getFlags());
    }
  }
}

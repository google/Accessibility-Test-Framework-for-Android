package com.google.android.apps.common.testing.accessibility.framework.replacements;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.SpanProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.SpanProto.SpanType;

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
  public static class ClickableSpan extends Span implements Parcelable {

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
    public void writeToParcel(Parcel parcel, int flags) {
      super.writeToParcel(parcel, flags);
    }

    private static ClickableSpan readFromParcel(Parcel parcel) {
      Span span = Span.readSpanFromParcel(parcel);
      return new ClickableSpan(
          span.getSpanClassName(), span.getStart(), span.getEnd(), span.getFlags());
    }

    @Override
    protected Span copyWithAdjustedPosition(int newStart, int newEnd) {
      return new ClickableSpan(getSpanClassName(), newStart, newEnd, getFlags());
    }

    public static final Parcelable.Creator<ClickableSpan> CREATOR =
        new Parcelable.Creator<ClickableSpan>() {
          @Override
          public ClickableSpan createFromParcel(Parcel in) {
            return readFromParcel(in);
          }

          @Override
          public ClickableSpan[] newArray(int size) {
            return new ClickableSpan[size];
          }
        };
  }

  /**
   * Used as a local replacement for Android's {@link android.text.style.URLSpan}
   */
  public static class URLSpan extends ClickableSpan
      implements Parcelable, Replaceable<android.text.style.URLSpan> {

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
    public void writeToParcel(Parcel parcel, int flags) {
      super.writeToParcel(parcel, flags);
      parcel.writeString(url);
    }

    private static URLSpan readFromParcel(Parcel parcel) {
      Span span = ClickableSpan.readSpanFromParcel(parcel);
      String url = parcel.readString();
      return new URLSpan(
          span.getSpanClassName(), span.getStart(), span.getEnd(), span.getFlags(), url);
    }

    @Override
    // An android.text.style.URLSpan *can* be constructed with null, but it is not documented
    // or annotated as such.
    @SuppressWarnings("nullness")
    public android.text.style.URLSpan getAndroidInstance() {
      return new android.text.style.URLSpan(url);
    }

    @Override
    protected Span copyWithAdjustedPosition(int newStart, int newEnd) {
      return new URLSpan(getSpanClassName(), newStart, newEnd, getFlags(), url);
    }

    public static final Parcelable.Creator<URLSpan> CREATOR =
        new Parcelable.Creator<URLSpan>() {
          @Override
          public URLSpan createFromParcel(Parcel in) {
            return readFromParcel(in);
          }

          @Override
          public URLSpan[] newArray(int size) {
            return new URLSpan[size];
          }
        };
  }
}

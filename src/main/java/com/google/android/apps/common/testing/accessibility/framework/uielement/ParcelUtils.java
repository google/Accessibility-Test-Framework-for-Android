package com.google.android.apps.common.testing.accessibility.framework.uielement;

import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Parcel;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Span;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableString;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Spans;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class for writing to and reading from Parcels
 */
final class ParcelUtils {

  private static final int ABSENT = 0;
  private static final int PRESENT = 1;

  private static final int SPAN_TYPE_UNKNOWN = 0;
  private static final int SPAN_TYPE_CLICKABLE = 1;
  private static final int SPAN_TYPE_URL = 2;
  private static final int SPAN_TYPE_STYLE = 3;
  private static final int SPAN_TYPE_UNDERLINE = 4;

  private ParcelUtils() {}

  /**
   * Writes values to the provided {@link Parcel} to represent a Nullable Integer.
   * If {@code val} is {@code null}, a single Int {@code 0} will be written. Otherwise, an Int
   * {@code 1} will be written followed by the value of the Integer.
   *
   * @param out a {@link Parcel} to which to write
   * @param val a value to represent
   */
  public static void writeNullableInteger(Parcel out, @Nullable Integer val) {
    if (val == null) {
      out.writeInt(ABSENT);
    } else {
      out.writeInt(PRESENT);
      out.writeInt(val);
    }
  }

  /**
   * Reads values from the provided {@link Parcel} representing a Nullable Integer.
   *
   * @param in a {@link Parcel} from which to read
   * @return the represented value
   */
  public static @Nullable Integer readNullableInteger(Parcel in) {
    int marker = in.readInt();
    if (marker == ABSENT) {
      return null;
    } else if (marker == PRESENT) {
      return in.readInt();
    } else {
      throw new IllegalStateException("Parcel contained unexpected marker value.");
    }
  }

  /**
   * Writes values to the provided {@link Parcel} to represent a Nullable Long.
   * If {@code val} is {@code null}, a single Int {@code 0} will be written. Otherwise, an Int
   * {@code 1} will be written followed by the value of the Long.
   *
   * @param out a {@link Parcel} to which to write
   * @param val a value to represent
   */
  public static void writeNullableLong(Parcel out, @Nullable Long val) {
    if (val == null) {
      out.writeInt(ABSENT);
    } else {
      out.writeInt(PRESENT);
      out.writeLong(val);
    }
  }

  /**
   * Reads values from the provided {@link Parcel} representing a Nullable Long.
   *
   * @param in a {@link Parcel} from which to read
   * @return the represented value
   */
  public static @Nullable Long readNullableLong(Parcel in) {
    int marker = in.readInt();
    if (marker == ABSENT) {
      return null;
    } else if (marker == PRESENT) {
      return in.readLong();
    } else {
      throw new IllegalStateException("Parcel contained unexpected marker value.");
    }
  }

  /**
   * Writes values to the provided {@link Parcel} to represent a Nullable Float.
   * If {@code val} is {@code null}, a single Int {@code 0} will be written. Otherwise, an Int
   * {@code 1} will be written followed by the value of the Float.
   *
   * @param out a {@link Parcel} to which to write
   * @param val a value to represent
   */
  public static void writeNullableFloat(Parcel out, @Nullable Float val) {
    if (val == null) {
      out.writeInt(ABSENT);
    } else {
      out.writeInt(PRESENT);
      out.writeFloat(val);
    }
  }

  /**
   * Reads values from the provided {@link Parcel} representing a Nullable Float.
   *
   * @param in a {@link Parcel} from which to read
   * @return the represented value
   */
  public static @Nullable Float readNullableFloat(Parcel in) {
    int marker = in.readInt();
    if (marker == ABSENT) {
      return null;
    } else if (marker == PRESENT) {
      return in.readFloat();
    } else {
      throw new IllegalStateException("Parcel contained unexpected marker value.");
    }
  }

  /**
   * Writes values to the provided {@link Parcel} to represent a Nullable String.
   * If {@code val} is {@code null}, a single Int {@code 0} will be written. Otherwise, an Int
   * {@code 1} will be written followed by the value of the String.
   *
   * @param out a {@link Parcel} to which to write
   * @param val a value to represent
   */
  public static void writeNullableString(Parcel out, @Nullable String val) {
    if (val == null) {
      out.writeInt(ABSENT);
    } else {
      out.writeInt(PRESENT);
      out.writeString(val);
    }
  }

  /**
   * Reads values from the provided {@link Parcel} representing a Nullable String.
   *
   * @param in a {@link Parcel} from which to read
   * @return the represented value
   */
  public static @Nullable String readNullableString(Parcel in) {
    int marker = in.readInt();
    if (marker == ABSENT) {
      return null;
    } else if (marker == PRESENT) {
      return in.readString();
    } else {
      throw new IllegalStateException("Parcel contained unexpected marker value.");
    }
  }

  /**
   * Writes values to the provided {@link Parcel} to represent a Nullable Boolean.
   * A single byte will be written, which will be {@code -1} if {@code val} is {@code null},
   * {@code 1} if {@link Boolean#TRUE}, or {@code 0} if {@link Boolean#FALSE}.
   *
   * @param out a {@link Parcel} to which to write
   * @param val a value to represent
   */
  public static void writeNullableBoolean(Parcel out, @Nullable Boolean val) {
    byte byteValue;
    if (val == null) {
      byteValue = -1;
    } else {
      byteValue = val ? (byte) 1 : (byte) 0;
    }
    out.writeByte(byteValue);
  }

  /**
   * Reads values from the provided {@link Parcel} representing a Nullable Boolean.
   *
   * @param in a {@link Parcel} from which to read
   * @return the represented value
   */
  public static @Nullable Boolean readNullableBoolean(Parcel in) {
    byte byteValue = in.readByte();
    if (byteValue == (byte) -1) {
      return null;
    } else if (byteValue == (byte) 0) {
      return false;
    } else if (byteValue == (byte) 1) {
      return true;
    } else {
      throw new IllegalStateException("Parcel contained unexpected Boolean byte.");
    }
  }

  /**
   * Writes values to the provided {@link Parcel} to represent a Nullable SpannableString. If {@code
   * val} is {@code null}, a single Int {@code 0} will be written. Otherwise, an Int {@code 1} will
   * be written followed by values representing the SpannableString.
   *
   * @param out a {@link Parcel} to which to write
   * @param val a value to represent
   */
  static void writeNullableSpannableString(Parcel out, @Nullable SpannableString val) {
    if (val == null) {
      out.writeInt(ABSENT);
    } else {
      out.writeInt(PRESENT);
      writeSpannableString(out, val);
    }
  }

  /**
   * Reads values from the provided {@link Parcel} representing a Nullable SpannableString.
   *
   * @param in a {@link Parcel} from which to read
   * @return the represented value
   */
  static @Nullable SpannableString readNullableSpannableString(Parcel in) {
    int marker = in.readInt();
    if (marker == ABSENT) {
      return null;
    } else if (marker == PRESENT) {
      return readSpannableString(in);
    } else {
      throw new IllegalStateException("Parcel contained unexpected marker value.");
    }
  }

  private static void writeSpannableString(Parcel out, SpannableString spannableString) {
    List<Span> spans = spannableString.getSpans();
    out.writeString(spannableString.toString());
    out.writeInt(spans.size());
    for (Span span : spans) {
      out.writeString(span.getSpanClassName());
      out.writeInt(span.getStart());
      out.writeInt(span.getEnd());
      out.writeInt(span.getFlags());

      if (span instanceof Spans.URLSpan) {
        out.writeInt(SPAN_TYPE_URL);
        out.writeString(((Spans.URLSpan) span).getUrl());
      } else if (span instanceof Spans.ClickableSpan) {
        out.writeInt(SPAN_TYPE_CLICKABLE);
      } else if (span instanceof Spans.StyleSpan) {
        out.writeInt(SPAN_TYPE_STYLE);
        out.writeInt(((Spans.StyleSpan) span).getStyle());
      } else if (span instanceof Spans.UnderlineSpan) {
        out.writeInt(SPAN_TYPE_UNDERLINE);
      } else {
        out.writeInt(SPAN_TYPE_UNKNOWN);
      }
    }
  }

  private static SpannableString readSpannableString(Parcel in) {
    String rawString = checkNotNull(in.readString());
    int spanSize = in.readInt();
    List<Span> spans = new ArrayList<>(spanSize);
    for (int i = 0; i < spanSize; ++i) {
      String spanClassName = checkNotNull(in.readString());
      int start = in.readInt();
      int end = in.readInt();
      int flags = in.readInt();
      int spanType = in.readInt();
      switch (spanType) {
        case SPAN_TYPE_UNKNOWN:
          spans.add(new Span(spanClassName, start, end, flags));
          break;
        case SPAN_TYPE_CLICKABLE:
          spans.add(new Spans.ClickableSpan(spanClassName, start, end, flags));
          break;
        case SPAN_TYPE_URL:
          spans.add(new Spans.URLSpan(spanClassName, start, end, flags, in.readString()));
          break;
        case SPAN_TYPE_STYLE:
          spans.add(new Spans.StyleSpan(spanClassName, start, end, flags, in.readInt()));
          break;
        case SPAN_TYPE_UNDERLINE:
          spans.add(new Spans.UnderlineSpan(spanClassName, start, end, flags));
          break;
        default:
          break;
      }
    }
    return new SpannableString(rawString, spans);
  }

  public static void writeRectToParcel(Rect rect, Parcel out) {
    out.writeInt(rect.getLeft());
    out.writeInt(rect.getTop());
    out.writeInt(rect.getRight());
    out.writeInt(rect.getBottom());
  }

  public static Rect readRectFromParcel(Parcel in) {
    return new Rect(
        /** left = */
        in.readInt(),
        /** top = */
        in.readInt(),
        /** right = */
        in.readInt(),
        /** bottom = */
        in.readInt());
  }

  /**
   * Writes values to the provided {@link Parcel} to represent a list of {@link Rect}.
   *
   * @param out a {@link Parcel} to which to write
   * @param rects a list of {@link Rect} to represent
   */
  public static void writeRectList(Parcel out, List<Rect> rects) {
    out.writeInt(rects.size());
    for (Rect rect : rects) {
      ParcelUtils.writeRectToParcel(rect, out);
    }
  }

  /**
   * Reads values from the provided {@link Parcel} representing a list of {@link Rect}.
   *
   * @param in a {@link Parcel} from which to read
   * @return the represented value
   */
  public static ImmutableList<Rect> readRectList(Parcel in) {
    int size = in.readInt();
    if (size > 0) {
      ImmutableList.Builder<Rect> rects = new ImmutableList.Builder<>();
      for (int i = 0; i < size; ++i) {
        rects.add(ParcelUtils.readRectFromParcel(in));
      }
      return rects.build();
    }
    return ImmutableList.of();
  }
}

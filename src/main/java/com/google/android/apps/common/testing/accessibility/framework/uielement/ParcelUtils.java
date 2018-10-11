package com.google.android.apps.common.testing.accessibility.framework.uielement;

import android.os.Parcel;
import androidx.annotation.Nullable;

/**
 * Utility class for writing to and reading from Parcels
 */
final class ParcelUtils {

  private static final int ABSENT = 0;
  private static final int PRESENT = 1;

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
}

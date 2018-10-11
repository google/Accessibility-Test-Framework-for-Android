package com.google.android.apps.common.testing.accessibility.framework;

import static com.google.common.base.Preconditions.checkState;

import androidx.annotation.Nullable;
import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.MetadataProto;
import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.TypedValueProto;
import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.TypedValueProto.TypeProto;
import com.google.protobuf.ByteString;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

/**
 * Metadata acts as a map between String keys and mixed-type values, similar to Android's {@code
 * android.os.Bundle} class. Within the Android Accessibility Test Framework, this class is used to:
 *
 * <ul>
 *   <li>Provide information or input data to an {@link AccessibilityHierarchyCheck}, which is
 *       conventionally managed by the convenience methods in {@link AccessibilityCheckMetadata}
 *   <li>Store parameters about {@link AccessibilityHierarchyCheckResult}s as defined by individual
 *       {@link AccessibilityHierarchyCheck} implementations
 * </ul>
 */
public class Metadata implements ResultMetadata {

  private final Map<String, TypedValue> map;

  public Metadata() {
    map = new HashMap<>();
  }

  /**
   * Constructs Metadata containing a copy of the mappings from the given Metadata.
   *
   * @param metadata The instance from which to copy mappings
   */
  public Metadata(Metadata metadata) {
    map = new HashMap<>(metadata.map);
  }

  public static Metadata fromProto(MetadataProto proto) {
    Metadata metadata = new Metadata();
    for (Entry<String, TypedValueProto> entry : proto.getMetadataMapMap().entrySet()) {
      metadata.map.put(entry.getKey(), new TypedValue(entry.getValue()));
    }

    return metadata;
  }

  @Override
  public boolean getBoolean(String key) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      throw invalidKeyException(key);
    } else if (TypedValue.Type.BOOLEAN != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.BOOLEAN, tv.type);
    }

    return (boolean) tv.value;
  }

  @Override
  public boolean getBoolean(String key, boolean defaultValue) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      return defaultValue;
    } else if (TypedValue.Type.BOOLEAN != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.BOOLEAN, tv.type);
    }

    return (boolean) tv.value;
  }

  @Override
  public void putBoolean(String key, boolean value) {
    map.put(key, new TypedValue(TypedValue.Type.BOOLEAN, value));
  }

  @Override
  public byte getByte(String key) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      throw invalidKeyException(key);
    } else if (TypedValue.Type.BYTE != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.BYTE, tv.type);
    }

    return (byte) tv.value;
  }

  @Override
  public byte getByte(String key, byte defaultValue) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      return defaultValue;
    } else if (TypedValue.Type.BYTE != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.BYTE, tv.type);
    }

    return (byte) tv.value;
  }

  @Override
  public void putByte(String key, byte value) {
    map.put(key, new TypedValue(TypedValue.Type.BYTE, value));
  }

  @Override
  public short getShort(String key) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      throw invalidKeyException(key);
    } else if (TypedValue.Type.SHORT != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.SHORT, tv.type);
    }

    return (short) tv.value;
  }

  @Override
  public short getShort(String key, short defaultValue) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      return defaultValue;
    } else if (TypedValue.Type.SHORT != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.SHORT, tv.type);
    }

    return (short) tv.value;
  }

  @Override
  public void putShort(String key, short value) {
    map.put(key, new TypedValue(TypedValue.Type.SHORT, value));
  }

  @Override
  public char getChar(String key) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      throw invalidKeyException(key);
    } else if (TypedValue.Type.CHAR != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.CHAR, tv.type);
    }

    return (char) tv.value;
  }

  @Override
  public char getChar(String key, char defaultValue) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      return defaultValue;
    } else if (TypedValue.Type.CHAR != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.CHAR, tv.type);
    }

    return (char) tv.value;
  }

  @Override
  public void putChar(String key, char value) {
    map.put(key, new TypedValue(TypedValue.Type.CHAR, value));
  }

  @Override
  public int getInt(String key) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      throw invalidKeyException(key);
    } else if (TypedValue.Type.INT != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.INT, tv.type);
    }

    return (int) tv.value;
  }

  @Override
  public int getInt(String key, int defaultValue) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      return defaultValue;
    } else if (TypedValue.Type.INT != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.INT, tv.type);
    }

    return (int) tv.value;
  }

  @Override
  public void putInt(String key, int value) {
    map.put(key, new TypedValue(TypedValue.Type.INT, value));
  }

  @Override
  public float getFloat(String key) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      throw invalidKeyException(key);
    } else if (TypedValue.Type.FLOAT != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.FLOAT, tv.type);
    }

    return (float) tv.value;
  }

  @Override
  public float getFloat(String key, float defaultValue) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      return defaultValue;
    } else if (TypedValue.Type.FLOAT != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.FLOAT, tv.type);
    }

    return (float) tv.value;
  }

  @Override
  public void putFloat(String key, float value) {
    map.put(key, new TypedValue(TypedValue.Type.FLOAT, value));
  }

  @Override
  public long getLong(String key) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      throw invalidKeyException(key);
    } else if (TypedValue.Type.LONG != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.LONG, tv.type);
    }

    return (long) tv.value;
  }

  @Override
  public long getLong(String key, long defaultValue) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      return defaultValue;
    } else if (TypedValue.Type.LONG != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.LONG, tv.type);
    }

    return (long) tv.value;
  }

  @Override
  public void putLong(String key, long value) {
    map.put(key, new TypedValue(TypedValue.Type.LONG, value));
  }

  @Override
  public double getDouble(String key) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      throw invalidKeyException(key);
    } else if (TypedValue.Type.DOUBLE != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.DOUBLE, tv.type);
    }

    return (double) tv.value;
  }

  @Override
  public double getDouble(String key, double defaultValue) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      return defaultValue;
    } else if (TypedValue.Type.DOUBLE != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.DOUBLE, tv.type);
    }

    return (double) tv.value;
  }

  @Override
  public void putDouble(String key, double value) {
    map.put(key, new TypedValue(TypedValue.Type.DOUBLE, value));
  }

  @Override
  public String getString(String key) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      throw invalidKeyException(key);
    } else if (TypedValue.Type.STRING != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.STRING, tv.type);
    }

    return (String) tv.value;
  }

  @Override
  public String getString(String key, String defaultValue) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      return defaultValue;
    } else if (TypedValue.Type.STRING != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.STRING, tv.type);
    }

    return (String) tv.value;
  }

  @Override
  public void putString(String key, String value) {
    map.put(key, new TypedValue(TypedValue.Type.STRING, value));
  }

  /**
   * Retrieves a {@code Object} stored within metadata. A {@link RuntimeException} is thrown in the
   * case the provided {@code key} is not stored within metadata or is stored as another type.
   *
   * <p>NOTE: Although this method returns {@code Object}, it is only intended to retrieve objects
   * that were stored within metadata using {@link #putNonSerializedObject(String, Object)}
   *
   * @param key The key of the value to retrieve
   * @return The {@code Object} associated with {@code key}
   */
  public Object getNonSerializedObject(String key) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      throw invalidKeyException(key);
    } else if (TypedValue.Type.NON_SERIALIZED_OBJECT != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.NON_SERIALIZED_OBJECT, tv.type);
    }

    return tv.value;
  }

  /**
   * Retrieves a {@code Object} stored within metadata. If the metadata does not contain the
   * provided {@code key}, {@code defaultValue} is returned. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is not stored within metadata or is stored as another type.
   *
   * <p>NOTE: Although this method returns {@code Object}, it is only intended to retrieve objects
   * that were stored within metadata using {@link #putNonSerializedObject(String, Object)}
   *
   * @param key The key of the value to retrieve
   * @param defaultValue The value to return if this metadata does not contain {@code key}
   * @return The {@code Object} associated with {@code key}
   */
  public @Nullable Object getNonSerializedObject(String key, @Nullable Object defaultValue) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      return defaultValue;
    } else if (TypedValue.Type.NON_SERIALIZED_OBJECT != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.NON_SERIALIZED_OBJECT, tv.type);
    }

    return tv.value;
  }

  /**
   * Stores an {@code Object} within metadata.
   *
   * <p>NOTE: Objects stored within metadata using this method will not be serialized or
   * deserialized in any form, will not be added to any {@link android.os.Bundle} created from this
   * instance using {@link #toBundle()}, and will only remain present in locally-retained instances.
   * As such, this method is only suitable for locally passing input to
   * {@link AccessibilityHierarchyCheck}s, which should typically be managed by
   * {@link AccessibilityCheckMetadata}.
   *
   * @param key The key of the value to store
   * @param value The value to store associated with {@code key}
   */
  public void putNonSerializedObject(String key, Object value) {
    map.put(key, new TypedValue(TypedValue.Type.NON_SERIALIZED_OBJECT, value));
  }

  @Override
  public boolean containsKey(String key) {
    return map.containsKey(key);
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public android.os.Bundle toBundle() {
    android.os.Bundle bundle = new android.os.Bundle();
    for (Entry<String, TypedValue> entry : map.entrySet()) {
      if (!entry.getValue().type.serialize) {
        // Don't add non-serializable types to Bundles
        continue;
      }
      String key = entry.getKey();
      TypedValue.Type type = entry.getValue().type;
      Object value = entry.getValue().value;
      switch (type) {
        case BOOLEAN:
          bundle.putBoolean(key, (boolean) value);
          break;
        case BYTE:
          bundle.putByte(key, (byte) value);
          break;
        case SHORT:
          bundle.putShort(key, (short) value);
          break;
        case CHAR:
          bundle.putChar(key, (char) value);
          break;
        case INT:
          bundle.putInt(key, (int) value);
          break;
        case FLOAT:
          bundle.putFloat(key, (float) value);
          break;
        case LONG:
          bundle.putLong(key, (long) value);
          break;
        case DOUBLE:
          bundle.putDouble(key, (double) value);
          break;
        case STRING:
          bundle.putString(key, (String) value);
          break;
        default:
          continue;
      }
    }

    return bundle;
  }

  @Override
  public ResultMetadata clone() {
    return new Metadata(this);
  }

  public MetadataProto toProto() {
    MetadataProto.Builder builder = MetadataProto.newBuilder();
    for (Entry<String, TypedValue> entry : map.entrySet()) {
      if (entry.getValue().type.serialize) {
        builder.putMetadataMap(entry.getKey(), entry.getValue().toProto());
      }
    }

    return builder.build();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Metadata metadata = (Metadata) o;

    return map.equals(metadata.map);
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  private static NoSuchElementException invalidKeyException(String key) {
    return new NoSuchElementException("No Metadata element found for key '" + key + "'.");
  }

  private static ClassCastException invalidTypeException(
      String key, TypedValue.Type requestedType, TypedValue.Type foundType) {
    return new ClassCastException(
        "Invalid type '"
            + requestedType.name()
            + "' requested from Metadata for key '"
            + key
            + "'.  Found type '"
            + foundType.name()
            + "' instead.");
  }

  /**
   * Used internally to track the value types of entries in metadata's {@code map}. Explicitly
   * storing types in this fashion allows for simpler serialization and deserialization.
   */
  private static class TypedValue {

    /**
     * The supported types for entries within metadata maps
     *
     * <p>CONTRACT: Any Type defined here must match {@code TypeProto} definitions in
     * AccessibilityEvaluation.proto, and its ordinal value must match the field number as defined
     * in the protocol buffer definition
     *
     * <p>CONTRACT: Once a Type is defined here, it must not be removed and its ordinal value must
     * not be changed. Additions are permitted at the end of enum. Data may be persisted using these
     * values, so incompatible changes may result in corruption during deserialization.
     *
     * <p>CONTRACT: All Types defined with a {@code serialize} value of {@code true} must provide:
     *
     * <ul>
     *   <li>An implementation to serialize its typed data to a protocol buffer format within {@link
     *       TypedValue#toProto()}
     *   <li>An implementation to deserialize its typed data from a protocol buffer format within
     *       {@link TypedValue}'s protocol buffer constructor
     *   <li>Logic to convert typed data into an entry compatible for storage in a {@link
     *       android.os.Bundle} within {@link TypedValue#toBundle()}
     * </ul>
     */
    public enum Type {
      NON_SERIALIZED_OBJECT(false),
      BOOLEAN(true),
      BYTE(true),
      SHORT(true),
      CHAR(true),
      INT(true),
      FLOAT(true),
      LONG(true),
      DOUBLE(true),
      STRING(true);

      private static final Type[] VALUES = values();

      public final boolean serialize;

      private Type(boolean serialize) {
        this.serialize = serialize;
      }

      public static Type fromProto(TypeProto proto) {
        return VALUES[proto.getNumber()];
      }

      public TypeProto toProto() {
        return TypeProto.forNumber(ordinal());
      }
    }

    public Type type;
    public Object value;

    public TypedValue(Type type, Object value) {
      this.type = type;
      this.value = value;
    }

    public TypedValue(TypedValueProto proto) {
      type = Type.fromProto(proto.getType());
      checkState(
          type.serialize,
          "Invalid request to deserialize a TypedValue with a non-serializable Type");

      switch (type) {
        case BOOLEAN:
          value = proto.getBooleanValue();
          break;
        case BYTE:
          value = proto.getByteValue().asReadOnlyByteBuffer().get();
          break;
        case SHORT:
          value = proto.getShortValue().asReadOnlyByteBuffer().getShort();
          break;
        case CHAR:
          value = proto.getCharValue().asReadOnlyByteBuffer().getChar();
          break;
        case INT:
          value = proto.getIntValue();
          break;
        case FLOAT:
          value = proto.getFloatValue();
          break;
        case LONG:
          value = proto.getLongValue();
          break;
        case DOUBLE:
          value = proto.getDoubleValue();
          break;
        case STRING:
          value = proto.getStringValue();
          break;
        default:
          break;
      }
    }

    public TypedValueProto toProto() {
      checkState(
          type.serialize, "Invalid request to serialize a TypedValue with a non-serializable Type");
      TypedValueProto.Builder builder = TypedValueProto.newBuilder();
      builder.setType(type.toProto());
      switch (type) {
        case BOOLEAN:
          builder.setBooleanValue((boolean) value);
          break;
        case BYTE:
          ByteBuffer byteBuffer = ByteBuffer.allocate(1);
          byteBuffer.put((byte) value).flip();
          builder.setByteValue(ByteString.copyFrom(byteBuffer));
          break;
        case SHORT:
          ByteBuffer shortBuffer = ByteBuffer.allocate(2);
          shortBuffer.putShort((short) value).flip();
          builder.setShortValue(ByteString.copyFrom(shortBuffer));
          break;
        case CHAR:
          ByteBuffer charBuffer = ByteBuffer.allocate(2);
          charBuffer.putChar((char) value).flip();
          builder.setCharValue(ByteString.copyFrom(charBuffer));
          break;
        case INT:
          builder.setIntValue((int) value);
          break;
        case FLOAT:
          builder.setFloatValue((float) value);
          break;
        case LONG:
          builder.setLongValue((long) value);
          break;
        case DOUBLE:
          builder.setDoubleValue((double) value);
          break;
        case STRING:
          builder.setStringValue((String) value);
          break;
        default:
          break;
      }
      return builder.build();
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TypedValue that = (TypedValue) o;

      if (type != that.type) {
        return false;
      }
      return value.equals(that.value);
    }

    @Override
    public int hashCode() {
      int result = type.hashCode();
      result = 31 * result + value.hashCode();
      return result;
    }
  }
}

package com.google.android.apps.common.testing.accessibility.framework;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.IntListProto;
import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.MetadataProto;
import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.StringListProto;
import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.TypedValueProto;
import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.TypedValueProto.TypeProto;
import com.google.common.collect.EnumBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import javax.annotation.Nullable;

/** An implementation of {@link ResultMetadata} backed by a {@link HashMap} */
public class HashMapResultMetadata implements ResultMetadata {

  protected final Map<String, TypedValue> map;

  public HashMapResultMetadata() {
    map = new HashMap<>();
  }

  /**
   * Creates an instance containing a copy of the mappings from the given instance.
   *
   * @param metadata The instance from which to copy mappings
   */
  protected HashMapResultMetadata(HashMapResultMetadata metadata) {
    map = new HashMap<>(metadata.map);
  }

  @Override
  public HashMapResultMetadata clone() {
    return new HashMapResultMetadata(this);
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

  @SuppressWarnings("unchecked") // Safe specification in TypedValue
  @Override
  public ImmutableList<String> getStringList(String key) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      throw invalidKeyException(key);
    } else if (TypedValue.Type.STRING_LIST != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.STRING_LIST, tv.type);
    }

    return (ImmutableList<String>) tv.value;
  }

  @SuppressWarnings("unchecked") // Safe specification in TypedValue
  @Override
  public ImmutableList<String> getStringList(String key, ImmutableList<String> defaultValue) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      return defaultValue;
    } else if (TypedValue.Type.STRING_LIST != tv.type) {
      throw invalidTypeException(key, TypedValue.Type.STRING_LIST, tv.type);
    }

    return (ImmutableList<String>) tv.value;
  }

  @Override
  public void putStringList(String key, List<String> value) {
    checkArgument(!value.isEmpty());
    map.put(key, new TypedValue(TypedValue.Type.STRING_LIST, ImmutableList.copyOf(value)));
  }

  @Override
  public ImmutableList<Integer> getIntegerList(String key) {
    return getValue(key, TypedValue.Type.INTEGER_LIST, null);
  }

  @Override
  public ImmutableList<Integer> getIntegerList(String key, ImmutableList<Integer> defaultValue) {
    return getValue(key, TypedValue.Type.INTEGER_LIST, defaultValue);
  }

  @Override
  public void putIntegerList(String key, List<Integer> value) {
    checkArgument(!value.isEmpty());
    map.put(key, new TypedValue(TypedValue.Type.INTEGER_LIST, ImmutableList.copyOf(value)));
  }

  @Override
  public boolean containsKey(String key) {
    return map.containsKey(key);
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  public static HashMapResultMetadata fromProto(MetadataProto proto) {
    HashMapResultMetadata metadata = new HashMapResultMetadata();
    for (Entry<String, TypedValueProto> entry : proto.getMetadataMapMap().entrySet()) {
      metadata.map.put(entry.getKey(), new TypedValue(entry.getValue()));
    }

    return metadata;
  }

  public MetadataProto toProto() {
    MetadataProto.Builder builder = MetadataProto.newBuilder();
    for (Entry<String, TypedValue> entry : map.entrySet()) {
      builder.putMetadataMap(entry.getKey(), entry.getValue().toProto());
    }

    return builder.build();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof HashMapResultMetadata)) {
      return false;
    }

    HashMapResultMetadata metadata = (HashMapResultMetadata) o;

    return map.equals(metadata.map);
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  // For debugging
  @Override
  public String toString() {
    return new TreeMap<String, TypedValue>(map).toString();
  }

  @SuppressWarnings("unchecked") // Safe specification in TypedValue
  private <T> T getValue(String key, TypedValue.Type type, @Nullable T defaultValue) {
    TypedValue tv = map.get(key);
    if (tv == null) {
      if (defaultValue != null) {
        return defaultValue;
      } else {
        throw invalidKeyException(key);
      }
    } else if (type != tv.type) {
      throw invalidTypeException(key, type, tv.type);
    }

    return (T) tv.value;
  }

  private static NoSuchElementException invalidKeyException(String key) {
    return new NoSuchElementException(
        "No HashMapResultMetadata element found for key '" + key + "'.");
  }

  private static ClassCastException invalidTypeException(
      String key, TypedValue.Type requestedType, TypedValue.Type foundType) {
    return new ClassCastException(
        "Invalid type '"
            + requestedType.name()
            + "' requested from HashMapResultMetadata for key '"
            + key
            + "'.  Found type '"
            + foundType.name()
            + "' instead.");
  }

  /**
   * Used internally to track the value types of entries in metadata's {@code map}. Explicitly
   * storing types in this fashion allows for simpler serialization and deserialization.
   */
  protected static class TypedValue {

    /** Mapping between in memory and serializable types. */
    private static final EnumBiMap<Type, TypeProto> TYPE_MAP =
        EnumBiMap.create(
            ImmutableMap.<Type, TypeProto>builder()
                .put(Type.BOOLEAN, TypeProto.BOOLEAN)
                .put(Type.BYTE, TypeProto.BYTE)
                .put(Type.SHORT, TypeProto.SHORT)
                .put(Type.CHAR, TypeProto.CHAR)
                .put(Type.INT, TypeProto.INT)
                .put(Type.FLOAT, TypeProto.FLOAT)
                .put(Type.LONG, TypeProto.LONG)
                .put(Type.DOUBLE, TypeProto.DOUBLE)
                .put(Type.STRING, TypeProto.STRING)
                .put(Type.STRING_LIST, TypeProto.STRING_LIST)
                .put(Type.INTEGER_LIST, TypeProto.INT_LIST)
                .buildOrThrow());

    /**
     * The supported types for entries within metadata maps
     *
     * <p>CONTRACT: Any Type defined here must have a corresponding {@code TypeProto} in {@link
     * #TYPE_MAP}.
     *
     * <p>CONTRACT: All Types must provide:
     *
     * <ul>
     *   <li>An implementation to serialize its typed data to a protocol buffer format within {@link
     *       TypedValue#toProto()}
     *   <li>An implementation to deserialize its typed data from a protocol buffer format within
     *       {@link TypedValue}'s protocol buffer constructor
     * </ul>
     */
    public enum Type {
      BOOLEAN,
      BYTE,
      SHORT,
      CHAR,
      INT,
      FLOAT,
      LONG,
      DOUBLE,
      STRING,
      STRING_LIST,
      INTEGER_LIST;

      public static Type fromProto(TypeProto proto) {
        return checkNotNull(TYPE_MAP.inverse().get(proto));
      }

      public TypeProto toProto() {
        return checkNotNull(TYPE_MAP.get(this));
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
        case STRING_LIST:
          value = ImmutableList.copyOf(proto.getStringListValue().getValuesList());
          break;
        case INTEGER_LIST:
          value = ImmutableList.copyOf(proto.getIntListValue().getValuesList());
          break;
      }
    }

    @SuppressWarnings("unchecked") // Safe specification in TypedValue
    public TypedValueProto toProto() {
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
        case STRING_LIST:
          builder.setStringListValue(
              StringListProto.newBuilder().addAllValues((ImmutableList<String>) value).build());
          break;
        case INTEGER_LIST:
          builder.setIntListValue(
              IntListProto.newBuilder().addAllValues((ImmutableList<Integer>) value).build());
          break;
      }
      return builder.build();
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof TypedValue)) {
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

    // For debugging
    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }
}

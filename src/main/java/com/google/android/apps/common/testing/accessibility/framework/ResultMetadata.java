package com.google.android.apps.common.testing.accessibility.framework;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;

/**
 * Definition of implementations used to retain metadata related to {@link
 * AccessibilityHierarchyCheckResult}s
 */
public interface ResultMetadata {

  /**
   * Retrieves a {@code boolean} value stored within metadata. A {@link RuntimeException} is thrown
   * in the case the provided {@code key} is not stored within metadata or is stored as another
   * type.
   *
   * @param key The key of the value to retrieve
   * @return The {@code boolean} value associated with {@code key}
   */
  boolean getBoolean(String key);

  /**
   * Retrieves a {@code boolean} value stored within metadata. If the metadata does not contain the
   * provided {@code key}, {@code defaultValue} is returned. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @param defaultValue The value to return if this metadata does not contain {@code key}
   * @return The {@code boolean} value associated with {@code key}, or {@code defaultValue} if the
   *     provided key does not exist
   */
  boolean getBoolean(String key, boolean defaultValue);

  /**
   * Stores a {@code boolean} value within metadata.
   *
   * @param key The key of the value to store
   * @param value The value to store associated with {@code key}
   */
  void putBoolean(String key, boolean value);

  /**
   * Retrieves a {@code byte} value stored within metadata. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is not stored within metadata or is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @return The {@code byte} value associated with {@code key}
   */
  byte getByte(String key);

  /**
   * Retrieves a {@code byte} value stored within metadata. If the metadata does not contain the
   * provided {@code key}, {@code defaultValue} is returned. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @param defaultValue The value to return if this metadata does not contain {@code key}
   * @return The {@code byte} value associated with {@code key}, or {@code defaultValue} if the
   *     provided key does not exist
   */
  byte getByte(String key, byte defaultValue);

  /**
   * Stores a {@code byte} value within metadata.
   *
   * @param key The key of the value to store
   * @param value The value to store associated with {@code key}
   */
  void putByte(String key, byte value);

  /**
   * Retrieves a {@code short} value stored within metadata. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is not stored within metadata or is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @return The {@code short} value associated with {@code key}
   */
  short getShort(String key);

  /**
   * Retrieves a {@code short} value stored within metadata. If the metadata does not contain the
   * provided {@code key}, {@code defaultValue} is returned. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @param defaultValue The value to return if this metadata does not contain {@code key}
   * @return The {@code short} value associated with {@code key}, or {@code defaultValue} if the
   *     provided key does not exist
   */
  short getShort(String key, short defaultValue);

  /**
   * Stores a {@code short} value within metadata.
   *
   * @param key The key of the value to store
   * @param value The value to store associated with {@code key}
   */
  void putShort(String key, short value);

  /**
   * Retrieves a {@code char} value stored within metadata. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is not stored within metadata or is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @return The {@code char} value associated with {@code key}
   */
  char getChar(String key);

  /**
   * Retrieves a {@code char} value stored within metadata. If the metadata does not contain the
   * provided {@code key}, {@code defaultValue} is returned. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @param defaultValue The value to return if this metadata does not contain {@code key}
   * @return The {@code char} value associated with {@code key}, or {@code defaultValue} if the
   *     provided key does not exist
   */
  char getChar(String key, char defaultValue);

  /**
   * Stores a {@code char} value within metadata.
   *
   * @param key The key of the value to store
   * @param value The value to store associated with {@code key}
   */
  void putChar(String key, char value);

  /**
   * Retrieves a {@code int} value stored within metadata. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is not stored within metadata or is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @return The {@code int} value associated with {@code key}
   */
  int getInt(String key);

  /**
   * Retrieves a {@code int} value stored within metadata. If the metadata does not contain the
   * provided {@code key}, {@code defaultValue} is returned. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @param defaultValue The value to return if this metadata does not contain {@code key}
   * @return The {@code int} value associated with {@code key}, or {@code defaultValue} if the
   *     provided key does not exist
   */
  int getInt(String key, int defaultValue);

  /**
   * Stores a {@code int} value within metadata.
   *
   * @param key The key of the value to store
   * @param value The value to store associated with {@code key}
   */
  void putInt(String key, int value);

  /**
   * Retrieves a {@code float} value stored within metadata. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is not stored within metadata or is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @return The {@code float} value associated with {@code key}
   */
  float getFloat(String key);

  /**
   * Retrieves a {@code float} value stored within metadata. If the metadata does not contain the
   * provided {@code key}, {@code defaultValue} is returned. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @param defaultValue The value to return if this metadata does not contain {@code key}
   * @return The {@code float} value associated with {@code key}, or {@code defaultValue} if the
   *     provided key does not exist
   */
  float getFloat(String key, float defaultValue);

  /**
   * Stores a {@code float} value within metadata.
   *
   * @param key The key of the value to store
   * @param value The value to store associated with {@code key}
   */
  void putFloat(String key, float value);

  /**
   * Retrieves a {@code long} value stored within metadata. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is not stored within metadata or is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @return The {@code long} value associated with {@code key}
   */
  long getLong(String key);

  /**
   * Retrieves a {@code long} value stored within metadata. If the metadata does not contain the
   * provided {@code key}, {@code defaultValue} is returned. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @param defaultValue The value to return if this metadata does not contain {@code key}
   * @return The {@code long} value associated with {@code key}, or {@code defaultValue} if the
   *     provided key does not exist
   */
  long getLong(String key, long defaultValue);

  /**
   * Stores a {@code long} value within metadata.
   *
   * @param key The key of the value to store
   * @param value The value to store associated with {@code key}
   */
  void putLong(String key, long value);

  /**
   * Retrieves a {@code double} value stored within metadata. A {@link RuntimeException} is thrown
   * in the case the provided {@code key} is not stored within metadata or is stored as another
   * type.
   *
   * @param key The key of the value to retrieve
   * @return The {@code double} value associated with {@code key}
   */
  double getDouble(String key);

  /**
   * Retrieves a {@code double} value stored within metadata. If the metadata does not contain the
   * provided {@code key}, {@code defaultValue} is returned. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @param defaultValue The value to return if this metadata does not contain {@code key}
   * @return The {@code double} value associated with {@code key}, or {@code defaultValue} if the
   *     provided key does not exist
   */
  double getDouble(String key, double defaultValue);

  /**
   * Stores a {@code double} value within metadata.
   *
   * @param key The key of the value to store
   * @param value The value to store associated with {@code key}
   */
  void putDouble(String key, double value);

  /**
   * Retrieves a {@code String} value stored within metadata. A {@link RuntimeException} is thrown
   * in the case the provided {@code key} is not stored within metadata or is stored as another
   * type.
   *
   * @param key The key of the value to retrieve
   * @return The {@code String} value associated with {@code key}
   */
  String getString(String key);

  /**
   * Retrieves a {@code String} value stored within metadata. If the metadata does not contain the
   * provided {@code key}, {@code defaultValue} is returned. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @param defaultValue The value to return if this metadata does not contain {@code key}
   * @return The {@code String} value associated with {@code key}, or {@code defaultValue} if the
   *     provided key does not exist
   */
  String getString(String key, String defaultValue);

  /**
   * Stores a {@code String} value within metadata.
   *
   * @param key The key of the value to store
   * @param value The value to store associated with {@code key}
   */
  void putString(String key, String value);

  /**
   * Retrieves a List of String values stored within metadata. A {@link RuntimeException} is thrown
   * in the case the provided {@code key} is not stored within metadata or is stored as another
   * type.
   *
   * @param key The key of the value to retrieve
   * @return The values associated with {@code key}
   */
  ImmutableList<String> getStringList(String key);

  /**
   * Retrieves a List of String values stored within metadata. If the metadata does not contain the
   * provided {@code key}, {@code defaultValue} is returned. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @param defaultValue The value to return if this metadata does not contain {@code key}
   * @return The values associated with {@code key}, or {@code defaultValue} if the provided key
   *     does not exist
   */
  ImmutableList<String> getStringList(String key, ImmutableList<String> defaultValue);

  /**
   * Stores a non-empty List of String values within metadata, replacing any existing value for the
   * given key.
   *
   * @param key The key of the value to store
   * @param value The value to store associated with {@code key}
   */
  void putStringList(String key, List<String> value);

  /**
   * Retrieve a List of Integer values stored within metadata. A {@link RuntimeException} is thrown
   * in the case the provided {@code key} is not stored within metadata or is stored as another
   * type.
   *
   * @param key The key of the value to retrieve
   * @return The values associated with {@code key}
   */
  ImmutableList<Integer> getIntegerList(String key);

  /**
   * Retrieves a List of Integer values stored within metadata. If the metadata does not contain the
   * provided {@code key}, {@code defaultValue} is returned. A {@link RuntimeException} is thrown in
   * the case the provided {@code key} is stored as another type.
   *
   * @param key The key of the value to retrieve
   * @param defaultValue The value to return if this metadata does not contain {@code key}
   * @return The {@code ImmutableList<Integer>} value associated with {@code key}, or {@code
   *     defaultValue} if the provided key does not exist
   */
  ImmutableList<Integer> getIntegerList(String key, ImmutableList<Integer> defaultValue);

  /**
   * Stores a non-empty List of Integer values within metadata, replacing any existing value for the
   * given key.
   *
   * @param key The key of the value to store
   * @param value The value to store associated with {@code key}
   */
  void putIntegerList(String key, List<Integer> value);

  /** See {@link Map#containsKey(Object)}. */
  boolean containsKey(String key);

  /** Returns {@code true} if this map contains no key-value mappings. */
  boolean isEmpty();

  /** Creates a shallow copy of this metadata */
  ResultMetadata clone();
}

package com.google.android.apps.common.testing.accessibility.framework.strings;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Manager for obtaining localized strings.
 */
public final class StringManager {

  private static final String STRINGS_PACKAGE_NAME =
      "com.google.android.apps.common.testing.accessibility.framework";
  private static final String STRINGS_FILE_NAME = "strings";

  private StringManager() {
  }

  /**
   * @param locale the desired locale
   * @param name the name of the {@link String} to get from properties
   * @return a localized {@link String} corresponding to the given name and locale
   * @throw MissingResourceException if the string is not found
   */
  public static String getString(Locale locale, String name) {
    return getResourceBundle(locale).getString(name);
  }

  private static ResourceBundle getResourceBundle(Locale locale) {
    // ResourceBundle handles necessary caching
    return ResourceBundle.getBundle(
      AndroidXMLResourceBundle.Control.getBaseName(STRINGS_PACKAGE_NAME, STRINGS_FILE_NAME),
      locale,
      checkNotNull(StringManager.class.getClassLoader()),
      new AndroidXMLResourceBundle.Control());
  }
}

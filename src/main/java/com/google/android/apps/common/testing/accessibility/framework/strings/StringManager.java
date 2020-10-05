package com.google.android.apps.common.testing.accessibility.framework.strings;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Manager for obtaining localized strings.
 */
public final class StringManager {

  private static final String STRINGS_PACKAGE_NAME =
      "com.google.android.apps.common.testing.accessibility.framework";
  private static final String STRINGS_FILE_NAME = "strings";
  private static @Nullable ResourceBundleProvider resourceBundleProvider;

  private StringManager() {
  }

  /**
   * Override the default resource bundle used by {@link StringManager}. If no provider is set, a
   * default ResourceBundle will be used.
   *
   * <p>Example usage : If Accessibility Testing Framework is used outside the
   * AndroidXmlResourceBundle environment or if the ClassLoader does not recognize the xml in
   * specific package {@link #STRINGS_PACKAGE_NAME}, use this to override the resource provider.
   */
  public static void setResourceBundleProvider(ResourceBundleProvider provider) {
    resourceBundleProvider = provider;
  }

  /**
   * @param locale the desired locale
   * @param name the name of the {@link String} to get from properties
   * @return a localized {@link String} corresponding to the given name and locale
   * @throws MissingResourceException if the string is not found
   */
  public static String getString(Locale locale, String name) {
    return getResourceBundle(locale).getString(name);
  }

  private static ResourceBundle getResourceBundle(Locale locale) {
    if (resourceBundleProvider != null) {
      return resourceBundleProvider.getResourceBundle(locale);
    }

    // ResourceBundle handles necessary caching.
    try {
      // Try to load the resources from under the package directory. This is location used for
      // resource files in JAR files within Google.
      return ResourceBundle.getBundle(
          AndroidXMLResourceBundle.Control.getBaseName(STRINGS_PACKAGE_NAME, STRINGS_FILE_NAME),
          locale,
          checkNotNull(StringManager.class.getClassLoader()),
          new AndroidXMLResourceBundle.Control());
    } catch (MissingResourceException e) {

      // If that doesn't work, try again, this time without the package name. This is the location
      // used for assets in an AAR file.
      return ResourceBundle.getBundle(
          AndroidXMLResourceBundle.Control.getBaseName("", STRINGS_FILE_NAME),
          locale,
          checkNotNull(StringManager.class.getClassLoader()),
          new AndroidXMLResourceBundle.Control());
    }
  }

  /**
   * Provider for resource bundle. Use this to override the default resource bundle to use in {@link
   * StringManager}.
   */
  public interface ResourceBundleProvider {

    /** Returns {@link ResourceBundle} that is suitable for the env, and the ClassLoader. */
    ResourceBundle getResourceBundle(Locale locale);
  }
}

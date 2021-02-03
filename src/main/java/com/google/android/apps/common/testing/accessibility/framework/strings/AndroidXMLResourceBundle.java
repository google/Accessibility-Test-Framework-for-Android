package com.google.android.apps.common.testing.accessibility.framework.strings;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A {@link ResourceBundle} that reads strings from Android-style resource files.
 */
public class AndroidXMLResourceBundle extends ResourceBundle {
  private static final String ANDROID_STRING_TAG_NAME = "string";
  private static final String ANDROID_STRING_NAME_ATTRIBUTE = "name";

  private final Properties properties = new Properties();

  private AndroidXMLResourceBundle(InputStream inputStream) throws IOException {
    checkNotNull(inputStream);
    Document document = getDocument(inputStream);
    inputStream.close();
    addStringsToProperties(document, properties);
  }

  @Override
  protected @Nullable Object handleGetObject(String key) {
    return properties.getProperty(key);
  }

  @Override
  public Enumeration<String> getKeys() {
    return Collections.enumeration(properties.stringPropertyNames());
  }

  /**
   * Parses an xml input and returns a {@link Document}.
   *
   * @param inputStream an {@link InputStream} to parse XML form
   * @return a {@link Document} containing the DOM for the input
   * @throws {@link RuntimeException} if the xml could not be parsed
   */
  private static Document getDocument(InputStream inputStream) {
    Document document = null;
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setIgnoringElementContentWhitespace(true);
      documentBuilderFactory.setIgnoringComments(true);
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      InputSource inputSource = new InputSource(inputStream);
      document = documentBuilder.parse(inputSource);
    } catch (SAXException | ParserConfigurationException | IOException e) {
      throw new RuntimeException("Could not read xml properties file", e);
    }
    return document;
  }

  /**
   * For every <string> tag in the given document, adds a property to the given {@link Properties}
   * with the tag's name attribute as the key and the tag's text as the value.
   *
   * @param document a {@link Document} containing android style <string> tags
   * @param properties a {@link Properties} to add strings found in the given {@link Document} to
   */
  private static void addStringsToProperties(Document document, Properties properties) {
    NodeList stringNodes = document.getElementsByTagName(ANDROID_STRING_TAG_NAME);
    for (int i = 0; i < stringNodes.getLength(); i++) {
      Node node = stringNodes.item(i);

      if (node != null) {
        String key = getStringName(node);
        String value = getStringValue(node);
        if ((key != null) && (value != null)) {
          properties.setProperty(key, value);
        }
      }
    }
  }

  /** Gets the value of 'name' attribute of an Android style {@code <string>} element. */
  private static @Nullable String getStringName(Node node) {
    NamedNodeMap attributes = node.getAttributes();
    if (attributes != null) {
      Node nameNode = attributes.getNamedItem(ANDROID_STRING_NAME_ATTRIBUTE);
      if (nameNode != null) {
        return nameNode.getNodeValue();
      }
    }
    return null;
  }

  /** Gets the value of an Android style {@code <string>} element. */
  private static @Nullable String getStringValue(Node node) {
    String value = node.getTextContent();
    if (value == null) {
      return null;
    }
    return value
        // Android trims whitespace throughout (getTextContent does it internally but not at the
        // ends) so we trim for parity
        .trim()
        // The XML parser does not unescape quotes, so we replace \" with " here for Android
        // parity
        .replace("\\\"", "\"")
        // The XML parser does not unescape quotes, so we replace \' with ' here for Android
        // parity
        .replace("\\'", "'");
  }

  /**
   * A {@link ResourceBundle.Control} implementation that can create a {@link ResourceBundle}
   * containing the localized strings from an android values XML file.
   */
  static class Control extends ResourceBundle.Control {
    private static final String XML_FORMAT = "xml";
    private static final List<String> ACCEPTED_FORMATS =
        Collections.unmodifiableList(Arrays.asList(XML_FORMAT));

    @Override
    public List<String> getFormats(String baseName) {
      checkNotNull(baseName);
      return ACCEPTED_FORMATS;
    }

    @Override
    public @Nullable ResourceBundle newBundle(
        String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
        throws IllegalAccessException, InstantiationException, IOException {
      String resource = toResourceName(toBundleName(baseName, locale), XML_FORMAT);
      if (resource != null) {
        URL url = loader.getResource(resource);
        if (url != null) {
          URLConnection urlConnection = url.openConnection();
          if (urlConnection != null) {
            if (reload) {
              urlConnection.setUseCaches(false);
            }
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
              ResourceBundle bundle = new AndroidXMLResourceBundle(inputStream);
              inputStream.close();
              return bundle;
            }
          }
        }
      }
      return null;
    }

    @Override
    public @Nullable Locale getFallbackLocale(String baseName, Locale locale) {
      // When android has no corresponding locale, it falls back to the ROOT (values/), so this
      // should not fall back to Locale.getDefault() for parity
      return null;
    }

    @Override
    public String toBundleName(String baseName, Locale locale) {
      checkNotNull(locale);
      checkNotNull(baseName);
      checkArgument(!baseName.isEmpty(), "Attempted to get resource name for empty base name");
      String language = locale.getLanguage();
      StringBuilder localeName = new StringBuilder();
      if (!language.isEmpty()) {
        localeName.append("-");
        localeName.append(language);
        String country = locale.getCountry();
        if (!country.isEmpty()) {
          localeName.append("-r");
          localeName.append(country);
        }
      }
      int packageNameDividerIndex = baseName.lastIndexOf('.');
      String packageName = baseName.substring(0, packageNameDividerIndex);
      String fileName = baseName.substring(packageNameDividerIndex + 1);

      // Example:
      // resources-el.strings.xml
      // or
      // com.google.android.apps.common.testing.accessibility.framework.resources-el.strings.xml
      return packageName.isEmpty()
          ? String.format("resources%s.%s", localeName, fileName)
          : String.format("%s.resources%s.%s", packageName, localeName, fileName);
    }

    /**
     * Returns a {@link String} name derived from the android package name and file name that can be
     * passed to any {@link ResourceBundle#getBundle} method for use with this class.
     *
     * @param packageName the android package where resources can be found
     * @param fileName the name of the xml strings file without an extension (e.g. for
     *     resources/strings.xml, fileName = "strings")
     * @return a {@link String} to be used with {@link ResourceBundle#getBundle}
     */
    static String getBaseName(String packageName, String fileName) {
      return String.format("%s.%s", packageName, fileName);
    }
  }
}

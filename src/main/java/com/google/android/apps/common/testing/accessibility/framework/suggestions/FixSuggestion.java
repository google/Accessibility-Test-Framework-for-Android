package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.common.annotations.Beta;
import java.util.Locale;
import org.jsoup.Jsoup;

/** A suggested fix for a {@link AccessibilityHierarchyCheckResult}. */
@Beta
public interface FixSuggestion {

  /**
   * Returns a human-readable description for this fix suggestion which may contain formatting
   * markup.
   *
   * @param locale desired locale for the description
   */
  CharSequence getRawDescription(Locale locale);

  /**
   * Returns a human-readable description for this fix suggestion without formatting markup.
   *
   * @param locale desired locale for the description
   */
  default String getDescription(Locale locale) {
    return Jsoup.parse(getRawDescription(locale).toString()).text();
  }
}

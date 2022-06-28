package com.google.android.apps.common.testing.accessibility.framework.checks;

import static com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils.IMAGE_VIEW_CLASS_NAME;
import static com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils.shouldFocusView;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.primitives.Ints.min;
import static java.lang.Math.abs;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.HashMapResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils;
import com.google.android.apps.common.testing.accessibility.framework.ocr.TextComponent;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableString;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.android.apps.common.testing.accessibility.framework.uielement.WindowHierarchyElement;
import com.google.common.annotations.Beta;
import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Check for finding those OCR recognized texts which are not exposed to Accessibility service. The
 * OCR results are provided via a Parameters object.
 *
 * <p>This check is under development.
 */
@Beta
public class UnexposedTextCheck extends AccessibilityHierarchyCheck {

  /** Result when the view is not visible. */
  public static final int RESULT_ID_NOT_VISIBLE = 1;
  /** Result when thew view is not {@code importantForAccessibility} */
  public static final int RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY = 2;
  /** Result when OCR result is not available. */
  public static final int RESULT_ID_OCR_RESULT_NOT_AVAILABLE = 3;
  /** Result when the view should not be focused by a screen reader. */
  public static final int RESULT_ID_SHOULD_NOT_FOCUS = 4;
  /** Result when the view type is excluded. */
  public static final int RESULT_ID_WEB_CONTENT = 5;
  /** Result when OCR results were detected inside an ImageView. */
  public static final int RESULT_ID_TEXT_DETECTED_IN_IMAGE_VIEW = 6;
  /** Result when no matching OCR results were detected for a focusable view. */
  public static final int RESULT_ID_NO_MATCHING_OCR_TEXT = 7;
  /** Result when OCR results can not match the speakable text of their best matching view. */
  public static final int RESULT_ID_UNEXPOSED_TEXT = 8;
  /** Result when OCR results match multiple views and it is ambiguous which should be used. */
  public static final int RESULT_ID_MULTIPLE_BEST_MATCH_VIEWS = 9;
  /** Result when OCR results were detected inside a SurfaceView. */
  public static final int RESULT_ID_UNEXPOSED_TEXT_IN_SURFACE_VIEW = 10;
  /** Result when multiple unexposed OCR results were detected inside a View. */
  public static final int RESULT_ID_MULTIPLE_UNEXPOSED_TEXTS = 11;
  /** Result when only a single character is identified by OCR but no text is rendered there. */
  public static final int RESULT_ID_SINGLE_OCR_CHARACTER_WITHOUT_TEXT = 12;

  public static final String SURFACE_VIEW_CLASS_NAME = "android.view.SurfaceView";

  /**
   * Result metadata key for OCR results which are not exposed to the Accessibility service as a
   * {@code String}. Populated in results with {@link #RESULT_ID_UNEXPOSED_TEXT}.
   */
  public static final String KEY_UNEXPOSED_TEXT = "KEY_UNEXPOSED_TEXT";
  /**
   * Result metadata key for OCR results which are not exposed to the Accessibility service as a
   * {@code String}. Populated in results with {@link #RESULT_ID_UNEXPOSED_TEXT}.
   */
  public static final String KEY_UNEXPOSED_TEXTS = "KEY_UNEXPOSED_TEXTS";
  /** Bounds of OCR text within the screen in the form of a flattened Rect. */
  public static final String KEY_OCR_BOUNDS = "KEY_OCR_BOUNDS";
  /**
   * Result metadata key for OCR results which wre detected inside an ImageView as a {@code String}.
   * Populated in results with {@link #RESULT_ID_TEXT_DETECTED_IN_IMAGE_VIEW}.
   */
  public static final String KEY_TEXT_DETECTED_IN_IMAGE_VIEW = "KEY_TEXT_DETECTED_IN_IMAGE_VIEW";
  /** OCR results with confidence scores not great than this threshold will be ignored. */
  private static final float CONFIDENCE_FILTER_THRESHOLD = 0.5f;
  /** Maximum edit distance allowed to consider OCR text to match speakable text. */
  private static final int OCR_EDIT_DISTANCE_THRESHOLD = 2;
  /**
   * If there are more than 2 unexposed OCR results are detected inside one View, only report one
   * warning for all unexposed OCR results against this View.
   */
  private static final int MERGE_MULTIPLE_UNEXPOSED_OCR_RESULTS_IN_ONE_VIEW_THRESHOLD = 2;

  private static final ImmutableMap<Integer, String> MESSAGE_IDS =
      ImmutableMap.of(
          RESULT_ID_NOT_VISIBLE,
          "result_message_not_visible",
          RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY,
          "result_message_not_important_for_accessibility",
          RESULT_ID_WEB_CONTENT,
          "result_message_web_content",
          RESULT_ID_SHOULD_NOT_FOCUS,
          "result_message_should_not_focus",
          RESULT_ID_OCR_RESULT_NOT_AVAILABLE,
          "result_message_ocr_result_not_available",
          RESULT_ID_NO_MATCHING_OCR_TEXT,
          "result_message_no_matching_ocr_results",
          RESULT_ID_MULTIPLE_BEST_MATCH_VIEWS,
          "result_message_multiple_best_match_views",
          RESULT_ID_UNEXPOSED_TEXT_IN_SURFACE_VIEW,
          "result_message_text_detected_in_surface_view",
          RESULT_ID_SINGLE_OCR_CHARACTER_WITHOUT_TEXT,
          "result_message_single_ocr_character_without_text");

  @Override
  protected @Nullable String getHelpTopic() {

    return null;
  }

  @Override
  public Category getCategory() {
    return Category.CONTENT_LABELING;
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_unexposed_text");
  }

  @Override
  public String getMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    String generated = generateMessageForResultId(locale, resultId);
    if (generated != null) {
      return generated;
    }

    switch (resultId) {
      case RESULT_ID_UNEXPOSED_TEXT:
        return StringManager.getString(locale, "result_message_unexposed_text");
      case RESULT_ID_TEXT_DETECTED_IN_IMAGE_VIEW:
        return StringManager.getString(locale, "result_message_text_detected_in_image_view");
      case RESULT_ID_MULTIPLE_UNEXPOSED_TEXTS:
        return StringManager.getString(locale, "result_message_multiple_unexposed_texts");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getShortMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    String generated = generateMessageForResultId(locale, resultId);
    if (generated != null) {
      return generated;
    }

    switch (resultId) {
      case RESULT_ID_UNEXPOSED_TEXT:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_brief_unexposed_text"),
            checkNotNull(metadata).getString(KEY_UNEXPOSED_TEXT));
      case RESULT_ID_TEXT_DETECTED_IN_IMAGE_VIEW:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_brief_text_detected_in_image_view"),
            checkNotNull(metadata).getString(KEY_TEXT_DETECTED_IN_IMAGE_VIEW));
      case RESULT_ID_MULTIPLE_UNEXPOSED_TEXTS:
        return String.format(
            locale,
            StringManager.getString(locale, "result_message_brief_multiple_unexposed_texts"),
            checkNotNull(metadata).getStringList(KEY_UNEXPOSED_TEXTS).get(0));
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public List<AccessibilityHierarchyCheckResult> runCheckOnHierarchy(
      AccessibilityHierarchy hierarchy,
      @Nullable ViewHierarchyElement fromRoot,
      @Nullable Parameters parameters) {
    List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();
    Map<ViewHierarchyElement, List<TextComponent>> elementToTextListMap =
        preprocessOcrResults(hierarchy, fromRoot, parameters);

    List<? extends ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
    Locale locale = hierarchy.getDeviceState().getLocale();
    for (ViewHierarchyElement view : viewsToEval) {
      results.addAll(
          generateCheckResults(
              view,
              elementToTextListMap,
              parameters,
              locale,
              viewsToEval,
              anyViewHasTextCharacterLocations(viewsToEval)));
    }
    return results;
  }

  /**
   * @return {@code true} if at least one view in {@code views} has a non-empty text character
   *     locations array, {@code false} otherwise.
   */
  private boolean anyViewHasTextCharacterLocations(List<? extends ViewHierarchyElement> views) {
    for (ViewHierarchyElement view : views) {
      if (!view.getTextCharacterLocations().isEmpty()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Preprocesses the OCR results and builds a mapping from {@link ViewHierarchyElement}s to their
   * best matching OCR results.
   */
  private static Map<ViewHierarchyElement, List<TextComponent>> preprocessOcrResults(
      AccessibilityHierarchy hierarchy,
      @Nullable ViewHierarchyElement fromRoot,
      @Nullable Parameters parameters) {

    Map<ViewHierarchyElement, List<TextComponent>> elementToTextListMap = new HashMap<>();
    if ((parameters == null) || (parameters.getOcrResult() == null)) {
      return elementToTextListMap;
    }

    List<Rect> systemWindowBounds = getSystemWindowBounds(hierarchy);
    Rect activeWindowBounds = hierarchy.getActiveWindow().getBoundsInScreen();
    List<? extends ViewHierarchyElement> allViews = hierarchy.getActiveWindow().getAllViews();
    ImmutableList<TextComponent> texts = checkNotNull(parameters.getOcrResult()).getTexts();
    for (TextComponent textComponent : texts) {
      if (!activeWindowBounds.contains(textComponent.getBoundsInScreen())) {
        // Ignore texts recognized outside the active window. The active window could be a modal
        // dialog and the bounds of a modal dialog could be only small part of the whole screen.
        continue;
      }

      if (isTextInsideSystemWindows(textComponent, systemWindowBounds)) {
        // Ignore texts recognized inside the system status bar, the navigation bar or the
        // soft keyboard
        continue;
      }

      if ((fromRoot != null)
          && !Rect.intersects(textComponent.getBoundsInScreen(), fromRoot.getBoundsInScreen())) {
        // Ignore texts recognized outside of the bounds of the root view if the given root view
        // is not null.
        continue;
      }

      Map<TextComponent, ViewHierarchyElement> map = buildBestMatchMap(textComponent, allViews);
      for (Map.Entry<TextComponent, ViewHierarchyElement> entry : map.entrySet()) {
        TextComponent text = entry.getKey();
        ViewHierarchyElement bestMatchElement = entry.getValue();
        List<TextComponent> textComponentList = elementToTextListMap.get(bestMatchElement);
        if (textComponentList == null) {
          textComponentList = new ArrayList<>();
          elementToTextListMap.put(bestMatchElement, textComponentList);
        }
        textComponentList.add(text);
      }
    }
    return elementToTextListMap;
  }

  private ImmutableList<AccessibilityHierarchyCheckResult> generateCheckResults(
      ViewHierarchyElement view,
      Map<ViewHierarchyElement, List<TextComponent>> elementToTextListMap,
      @Nullable Parameters parameters,
      Locale locale,
      List<? extends ViewHierarchyElement> viewsToEval,
      boolean containsCharacterLocations) {
    if ((parameters == null) || (parameters.getOcrResult() == null)) {
      return ImmutableList.of(createNotRunCheckResult(view, RESULT_ID_OCR_RESULT_NOT_AVAILABLE));
    }


    if (!Boolean.TRUE.equals(view.isVisibleToUser())) {
      return ImmutableList.of(createNotRunCheckResult(view, RESULT_ID_NOT_VISIBLE));
    }

    List<TextComponent> textList = elementToTextListMap.get(view);
    boolean isTextListEmpty = (textList == null) || textList.isEmpty();
    if (!isTextListEmpty && view.checkInstanceOf(IMAGE_VIEW_CLASS_NAME)) {
      // Any random texts could be possibly recognized in an image
      ImmutableList.Builder<AccessibilityHierarchyCheckResult> builder = ImmutableList.builder();
      for (TextComponent text : checkNotNull(textList)) {
        builder.add(createNotRunCheckResultForTextInImageView(view, text.getValue()));
      }
      return builder.build();
    }

    if (!view.isImportantForAccessibility()) {
      return ImmutableList.of(
          createNotRunCheckResult(view, RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY));
    }

    if (view.checkInstanceOf(ViewHierarchyElementUtils.WEB_VIEW_CLASS_NAME)
        && view.getChildViewCount() == 0) {
      return ImmutableList.of(createNotRunCheckResult(view, RESULT_ID_WEB_CONTENT));
    }

    if (!ViewHierarchyElementUtils.shouldFocusView(view)) {
      return ImmutableList.of(createNotRunCheckResult(view, RESULT_ID_SHOULD_NOT_FOCUS));
    }

    SpannableString speakableText =
        ViewHierarchyElementUtils.getSpeakableTextForElement(view, locale);
    if (isTextListEmpty) {
      return ImmutableList.of(createNotRunCheckResult(view, RESULT_ID_NO_MATCHING_OCR_TEXT));
    }

    ImmutableList.Builder<AccessibilityHierarchyCheckResult> builder = ImmutableList.builder();
    for (TextComponent text : checkNotNull(textList)) {
      String ocrText = text.getValue();
      // If any view contains character locations, attempt to match the OCR text to known text
      // character locations. Even if this view doesn't contain them, views in its speakable subtree
      // might.
      if (containsCharacterLocations) {
        List<ViewHierarchyElement> bestMatchViews =
            filterViewsByOverlappingCharacterLocations(text, viewsToEval);
        if (bestMatchViews.size() == 1) {
          ViewHierarchyElement bestMatchView = bestMatchViews.get(0);
          String renderedText = matchTextToCharacterLocations(text, bestMatchView);
          if (ocrResultMatchesRenderedText(text.getValue(), renderedText)) {
            // Sometimes the OCR recognizes text incorrectly. If text character locations can be
            // derived from the accessibility hierarchy, the character bounds can be matched with
            // the OCR bounds to determine the actual text. If the OCR text is close enough, it
            // probably was an OCR error, and the rendered text is the actual speakable text.
            // If not, it's probably a more drastic mistake, and it's better to ignore this OCR
            // text entirely.
            ocrText = renderedText;
          } else {
            // If the OCR text bounds matches on screen character locations, but the OCR text itself
            // is too dissimilar from those characters, the OCR is likely wrong. Processing this
            // text component would likely cause a false positive, because the incorrect text cannot
            // exist in the accessibility hierarchy.
            continue;
          }
        } else if (bestMatchViews.size() > 1) {
          return ImmutableList.of(
              createNotRunCheckResult(view, RESULT_ID_MULTIPLE_BEST_MATCH_VIEWS));
        } else if (bestMatchViews.isEmpty() && (ocrText.length() == 1)) {
          // If character locations exist but there is no view that could contain text corresponding
          // to the OCR text, and the OCR text is a single character, the OCR is probably an icon.
          return ImmutableList.of(createNotRunCheckResultForSingleCharOcr(view, ocrText));
        }
      }
      if (!isTextMatching(ocrText, speakableText.toString())) {
        // Checks if this text is recognized from an image view which is not important for
        // accessibility
        ViewHierarchyElement bestMatchViewIncludeNotImportantViews =
            findBestMatchView(
                text, view.getSelfAndAllDescendants(), /* includeNotImportantViews= */ true);
        if ((bestMatchViewIncludeNotImportantViews != null)
            && bestMatchViewIncludeNotImportantViews.checkInstanceOf(IMAGE_VIEW_CLASS_NAME)) {
          builder.add(
              createNotRunCheckResultForTextInImageView(
                  bestMatchViewIncludeNotImportantViews, text.getValue()));
          continue;
        }

        if (view.checkInstanceOf(SURFACE_VIEW_CLASS_NAME)) {
          // Returns one warning result for this SurfaceView because its texts may not be exposed to
          // accessibility services.
          builder.add(
              new AccessibilityHierarchyCheckResult(
                  this.getClass(),
                  AccessibilityCheckResultType.WARNING,
                  view,
                  RESULT_ID_UNEXPOSED_TEXT_IN_SURFACE_VIEW,
                  /* metadata= */ null));
          return builder.build();
        }

        ResultMetadata resultMetadata = new HashMapResultMetadata();
        resultMetadata.putString(KEY_UNEXPOSED_TEXT, ocrText);
        resultMetadata.putString(KEY_OCR_BOUNDS, text.getBoundsInScreen().flattenToString());
        builder.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.WARNING,
                view,
                RESULT_ID_UNEXPOSED_TEXT,
                resultMetadata));
      }
    }
    ImmutableList<AccessibilityHierarchyCheckResult> results = builder.build();
    if (results.size() > MERGE_MULTIPLE_UNEXPOSED_OCR_RESULTS_IN_ONE_VIEW_THRESHOLD) {
      ResultMetadata resultMetadata = new HashMapResultMetadata();
      List<String> unexposedTexts = new ArrayList<>();
      for (AccessibilityHierarchyCheckResult result : results) {
        unexposedTexts.add(checkNotNull(result.getMetadata()).getString(KEY_UNEXPOSED_TEXT));
      }
      resultMetadata.putStringList(KEY_UNEXPOSED_TEXTS, unexposedTexts);
      return ImmutableList.of(
          new AccessibilityHierarchyCheckResult(
              this.getClass(),
              AccessibilityCheckResultType.WARNING,
              view,
              RESULT_ID_MULTIPLE_UNEXPOSED_TEXTS,
              resultMetadata));
    } else {
      return results;
    }
  }

  private static @Nullable String generateMessageForResultId(Locale locale, int resultId) {
    String messageId = MESSAGE_IDS.get(resultId);
    return (messageId == null) ? null : StringManager.getString(locale, messageId);
  }

  private AccessibilityHierarchyCheckResult createNotRunCheckResult(
      ViewHierarchyElement element, int resultId) {
    return new AccessibilityHierarchyCheckResult(
        this.getClass(),
        AccessibilityCheckResultType.NOT_RUN,
        element,
        resultId,
        /* metadata= */ null);
  }

  private AccessibilityHierarchyCheckResult createNotRunCheckResultForTextInImageView(
      ViewHierarchyElement view, String text) {
    ResultMetadata resultMetadata = new HashMapResultMetadata();
    resultMetadata.putString(KEY_TEXT_DETECTED_IN_IMAGE_VIEW, text);
    return new AccessibilityHierarchyCheckResult(
        this.getClass(),
        AccessibilityCheckResultType.NOT_RUN,
        view,
        RESULT_ID_TEXT_DETECTED_IN_IMAGE_VIEW,
        resultMetadata);
  }

  private AccessibilityHierarchyCheckResult createNotRunCheckResultForSingleCharOcr(
      ViewHierarchyElement view, String ocrText) {
    // If only a single character is recognized and it doesn't correspond to any text
    // character locations, it's probably an icon, such as a close icon. This is not text
    // and should be ignored.
    ResultMetadata resultMetadata = new HashMapResultMetadata();
    resultMetadata.putString(KEY_UNEXPOSED_TEXT, ocrText);
    return new AccessibilityHierarchyCheckResult(
        this.getClass(),
        AccessibilityCheckResultType.NOT_RUN,
        view,
        RESULT_ID_SINGLE_OCR_CHARACTER_WITHOUT_TEXT,
        resultMetadata);
  }

  /**
   * Returns a list of the bounds of the system windows (status bar, navigation bar and soft
   * keyboard) in screen coordinates.
   *
   * <p>The returned list contains the bounds of the soft keyboard only if the soft keyboard is
   * shown.
   */
  private static List<Rect> getSystemWindowBounds(AccessibilityHierarchy hierarchy) {

    List<Rect> systemWindowBounds = new ArrayList<>();
    Collection<? extends WindowHierarchyElement> windows = hierarchy.getAllWindows();
    for (WindowHierarchyElement window : windows) {
      Integer windowType = window.getType();
      if ((windowType != null)
          && ((windowType == WindowHierarchyElement.WINDOW_TYPE_SYSTEM)
              || (windowType == WindowHierarchyElement.WINDOW_TYPE_INPUT_METHOD))) {
        systemWindowBounds.add(window.getBoundsInScreen());
      }
    }
    return systemWindowBounds;
  }

  private static boolean isTextInsideSystemWindows(
      TextComponent textComponent, List<Rect> systemWindowBounds) {
    for (Rect windowBoundsInScreen : systemWindowBounds) {
      if (windowBoundsInScreen.contains(textComponent.getBoundsInScreen())) {
        return true;
      }
    }
    return false;
  }

  private static boolean ocrResultMatchesRenderedText(String ocrText, String renderedText) {
    return editDistance(ocrText, renderedText) <= OCR_EDIT_DISTANCE_THRESHOLD;
  }

  /** Checks whether the recognized text matches the given target text. */
  private static boolean isTextMatching(String text, String targetText) {
    if (TextUtils.isEmpty(targetText)) {
      return false;
    }

    if (targetText.contains(text)) {
      return true;
    }

    // Keep only Latin letters and numbers (alphanumeric)

    String asciiOcrText = Ascii.toLowerCase(text.replaceAll("[^a-zA-Z0-9]", ""));
    String asciiSpeakableText = Ascii.toLowerCase(targetText.replaceAll("[^a-zA-Z0-9]", ""));
    if (asciiSpeakableText.contains(asciiOcrText)) {
      return true;
    }

    // OCR engine always makes recognition errors. If the edit distance between the two strings is
    // less than 1, the recognized text is accepted as a match of the speakable text.
    if ((abs(asciiOcrText.length() - asciiSpeakableText.length()) <= 1)
        && editDistance(asciiOcrText, asciiSpeakableText) <= 1) {
      return true;
    }


    return false;
  }

  /**
   * Flattens the given {@link TextComponent} to a list of smaller {@link TextComponent}.
   *
   * <p>Each of the smaller {@link TextComponent} will contain only one word and have an empty child
   * {@link TextComponent} list.
   */
  private static List<TextComponent> flattenTextComponent(TextComponent textComponent) {
    List<TextComponent> components = new ArrayList<>();
    if (textComponent.getComponents().isEmpty()) {
      if ((textComponent.getConfidence() == null)
          || (checkNotNull(textComponent.getConfidence()) > CONFIDENCE_FILTER_THRESHOLD)) {
        components.add(textComponent);
      }
    } else {
      for (TextComponent subText : textComponent.getComponents()) {
        components.addAll(flattenTextComponent(subText));
      }
    }
    return components;
  }

  /**
   * Creates a mapping from Ocr texts to their best match views which are important for
   * accessibility.
   *
   * <p>The best match views must be important for accessibility because each Ocr text will be
   * compared with the speakable text of its best match view to determine whether the Ocr text has
   * been exposed to the accessibility service.
   *
   * <p>NOTE: TextComponent values in the result's keys may not appear within the textComponent
   * input parameter. Each value will either be an individual word (leaf) from the input, or a group
   * of consecutive words. In either case, getComponents() will return an empty list.
   *
   * <p>Group consecutive texts together when
   *
   * <ul>
   *   <li>They are in the same line meaning that the ranges of their vertical coordinates intersect
   *   <li>They have the same best match view in all views including views which are not important
   *       for accessibility
   *   <li>They have the same best match view in all views which are important for accessibility
   * </ul>
   *
   * <p>NOTE: This algorithm assumes that the text is written horizontally.
   */
  @SuppressWarnings("ReferenceEquality")
  private static Map<TextComponent, ViewHierarchyElement> buildBestMatchMap(
      TextComponent textComponent, List<? extends ViewHierarchyElement> allViews) {
    Map<TextComponent, ViewHierarchyElement> map = new HashMap<>();

    List<TextComponent> wordList = flattenTextComponent(textComponent);
    Map<TextComponent, Integer> wordStartIndexMap = createWordStartIndices(wordList, textComponent);

    // Regroup text segments in the TextComponent as they may have different best match views.
    List<TextComponent> consecutiveWordList = new ArrayList<>();
    ViewHierarchyElement lastBestMatchA11yImportantOnly = null;
    ViewHierarchyElement lastBestMatchIncludingA11yNotImportant = null;
    TextComponent lastSubText = null;

    for (TextComponent word : wordList) {
      ViewHierarchyElement bestMatchViewA11yImportantOnly =
          findBestMatchView(word, allViews, /* includeNotImportantViews= */ false);
      ViewHierarchyElement bestMatchViewIncludingA11yNotImportant =
          findBestMatchView(word, allViews, /* includeNotImportantViews= */ true);
      if ((bestMatchViewA11yImportantOnly == null)
          || (bestMatchViewIncludingA11yNotImportant == null)) {
        continue;
      }

      if ((lastBestMatchA11yImportantOnly == null)
          || (isSameLine(word, checkNotNull(lastSubText))
              && ((bestMatchViewIncludingA11yNotImportant == lastBestMatchIncludingA11yNotImportant)
                  && (bestMatchViewA11yImportantOnly == lastBestMatchA11yImportantOnly)))) {
        consecutiveWordList.add(word);
      } else {
        String substring =
            getSubString(textComponent.getValue(), consecutiveWordList, wordStartIndexMap);
        map.put(
            TextComponent.newBuilder(substring, getSubStringBoundsInScreen(consecutiveWordList))
                .build(),
            lastBestMatchA11yImportantOnly);

        consecutiveWordList = new ArrayList<>();
        consecutiveWordList.add(word);
      }
      lastSubText = word;
      lastBestMatchA11yImportantOnly = bestMatchViewA11yImportantOnly;
      lastBestMatchIncludingA11yNotImportant = bestMatchViewIncludingA11yNotImportant;
    }

    if ((lastBestMatchA11yImportantOnly != null) && !consecutiveWordList.isEmpty()) {
      String substring =
          getSubString(textComponent.getValue(), consecutiveWordList, wordStartIndexMap);
      map.put(
          TextComponent.newBuilder(substring, getSubStringBoundsInScreen(consecutiveWordList))
              .build(),
          lastBestMatchA11yImportantOnly);
    }

    return map;
  }

  /**
   * Creates a mapping from each word in a paragraph to the index of the word's first character in
   * the paragraph.
   */
  private static Map<TextComponent, Integer> createWordStartIndices(
      List<TextComponent> wordList, TextComponent textComponent) {
    Map<TextComponent, Integer> wordStartIndexMap = new HashMap<>();
    int startIndex = 0;
    for (TextComponent subText : wordList) {
      startIndex = textComponent.getValue().indexOf(subText.getValue(), startIndex);
      wordStartIndexMap.put(subText, startIndex);
      startIndex += subText.getValue().length();
    }
    return wordStartIndexMap;
  }

  /** Returns {@code true} if the ranges of their vertical coordinates intersect. */
  private static boolean isSameLine(TextComponent firstWord, TextComponent secondWord) {
    return (secondWord.getBoundsInScreen().getTop() < firstWord.getBoundsInScreen().getBottom())
        && (firstWord.getBoundsInScreen().getTop() < secondWord.getBoundsInScreen().getBottom());
  }

  /**
   * Returns a sub string of the given paragraph which contains a list of consecutive words.
   *
   * <p>For example, "this is an example" is a paragraph and its sub string "is an example" will be
   * returned for a consecutive list of words ("is", "an, "example").
   *
   * @param paragraph A paragraph which may be composed of multiple lines of text
   * @param consecutiveWordList The consecutive words in the paragraph
   * @param wordStartIndexMap A mapping from a word to the index of the word's first character in
   *     the paragraph
   */
  private static String getSubString(
      String paragraph,
      List<TextComponent> consecutiveWordList,
      Map<TextComponent, Integer> wordStartIndexMap) {
    if (consecutiveWordList.size() == 1) {
      return consecutiveWordList.get(0).getValue();
    }

    TextComponent lastTextComponent = Iterables.getLast(consecutiveWordList);
    return paragraph.substring(
        checkNotNull(wordStartIndexMap.get(consecutiveWordList.get(0))),
        checkNotNull(wordStartIndexMap.get(lastTextComponent))
            + lastTextComponent.getValue().length());
  }

  /***
   *  Calculates the smallest rectangle which covers the screen bounds of all the text components
   *  in the given {@link TextComponent} list.
   */
  private static Rect getSubStringBoundsInScreen(List<TextComponent> wordList) {
    if (wordList.isEmpty()) {
      return Rect.EMPTY;
    }
    Rect subRect = wordList.get(0).getBoundsInScreen();
    for (int i = 1; i < wordList.size(); i++) {
      subRect = subRect.union(wordList.get(i).getBoundsInScreen());
    }
    return subRect;
  }

  /**
   * If includeNotImportantViews is true, returns the view (among allViews) that has the best match
   * between its visible region and that of the text; that is, the highest intersection over union.
   * If includeNotImportantViews is false, returns the view that has the best match from among only
   * those views which are important for accessibility and focusable.
   *
   * @return {@code null} if the region of the text does not intersect with any qualified view.
   */
  private static @Nullable ViewHierarchyElement findBestMatchView(
      TextComponent textComponent,
      List<? extends ViewHierarchyElement> allViews,
      boolean includeNotImportantViews) {
    // If there are elements with text that overlap with textComponent, it is highly likely one of
    // those is the best match view, regardless of exact IOU scores. If so, only iterate through the
    // overlapping views. If no views have text overlapping textComponent, iterate over all views.
    //
    // There are multiple cases to consider involving includeNotImportantViews and the number of
    // overlapping views with text character locations.
    //
    // includeNotImportantViews == true:
    //   0 overlapping views: no views found, search allViews.
    //   1 or more overlapping views: views found, search only overlapping views.
    // includeNotImportantViews == false:
    //   0 overlapping views: no views found, search allViews.
    //   1 overlapping view: view found, but that view might not be important for accessibility.
    //       Search only that view's nearest focusable for accessibility ancestor.
    //   2 or more overlapping views: views found, but they might not be important for
    //       accessibility. It would be ambiguous to traverse the tree for all those views'
    //       ancestors. Instead, search all views.
    List<? extends ViewHierarchyElement> viewsWithOverlappingCharacters =
        filterViewsByOverlappingCharacterLocations(textComponent, allViews);
    if (includeNotImportantViews && !viewsWithOverlappingCharacters.isEmpty()) {
      allViews = viewsWithOverlappingCharacters;
    } else if (!includeNotImportantViews && viewsWithOverlappingCharacters.size() == 1) {
      ViewHierarchyElement focusableForAccessibilityAncestor =
          ViewHierarchyElementUtils.getFocusableForAccessibilityAncestor(
              viewsWithOverlappingCharacters.get(0));
      if (focusableForAccessibilityAncestor != null) {
        // If there's only one view, no need to calculate IOU scores.
        return focusableForAccessibilityAncestor;
      }
    }

    ViewHierarchyElement bestMatch = null;
    float highestIOU = 0;
    for (ViewHierarchyElement view : allViews) {
      if (!includeNotImportantViews) {
        if (!view.isImportantForAccessibility() || !shouldFocusView(view)) {
          continue;
        }
      }

      float iou =
          calculateIntersectionOverUnion(
              view.getBoundsInScreen(), textComponent.getBoundsInScreen());
      if (iou > 0 && iou >= highestIOU) {
        bestMatch = view;
        highestIOU = iou;
      }
    }

    return bestMatch;
  }

  /**
   * @return the views in {@code allViews} whose text character locations intersect with the bounds
   *     of {@code textComponent}. Views without text character locations are not returned.
   */
  private static List<ViewHierarchyElement> filterViewsByOverlappingCharacterLocations(
      TextComponent textComponent, List<? extends ViewHierarchyElement> allViews) {
    List<ViewHierarchyElement> results = new ArrayList<>();
    for (ViewHierarchyElement view : allViews) {
      for (Rect charBounds : view.getTextCharacterLocations()) {
        if (Rect.intersects(textComponent.getBoundsInScreen(), charBounds)) {
          results.add(view);
          break;
        }
      }
    }
    return results;
  }

  /**
   * Calculates a string from the text character locations in {@code view} that match the bounds of
   * {@code text}.
   *
   * @param ocrText A TextComponent representing on screen text.
   * @param view The view whose text components should be used to construct the returned string.
   * @return A string containing characters in {@code view} that match the bounds of {@code text}.
   */
  private static String matchTextToCharacterLocations(
      TextComponent ocrText, ViewHierarchyElement view) {
    SpannableString viewText = checkNotNull(view.getText());
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < view.getTextCharacterLocations().size() && i < viewText.length(); i++) {
      if (Rect.intersects(view.getTextCharacterLocations().get(i), ocrText.getBoundsInScreen())) {
        stringBuilder.append(viewText.charAt(i));
      }
    }
    return stringBuilder.toString();
  }

  private static float calculateIntersectionOverUnion(Rect r1, Rect r2) {
    if (!Rect.intersects(r1, r2)) {
      return 0;
    }

    Rect union = r1.union(r2);
    Rect intersect = r1.intersect(r2);
    return (float) intersect.area() / (float) union.area();
  }

  /**
   * Returns Levenshtein edit distance of the given two strings.
   *
   * <p>Assumes that both strings are lowercase.
   */
  private static int editDistance(String s1, String s2) {
    // Make sure s2 is shorter than s1
    if (s1.length() < s2.length()) {
      String temp = s2;
      s2 = s1;
      s1 = temp;
    }

    int[] costs = new int[s2.length() + 1];
    for (int i = 0; i <= s1.length(); i++) {
      int lastValue = i;
      for (int j = 0; j <= s2.length(); j++) {
        if (i == 0) {
          costs[j] = j;
        } else {
          if (j > 0) {
            int newValue = costs[j - 1];
            if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
              newValue = min(newValue, lastValue, costs[j]) + 1;
            }
            costs[j - 1] = lastValue;
            lastValue = newValue;
          }
        }
      }
      if (i > 0) {
        costs[s2.length()] = lastValue;
      }
    }
    return costs[s2.length()];
  }
}

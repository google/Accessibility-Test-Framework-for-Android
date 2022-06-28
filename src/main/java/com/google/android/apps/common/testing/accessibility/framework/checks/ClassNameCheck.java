package com.google.android.apps.common.testing.accessibility.framework.checks;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Boolean.TRUE;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck.Category;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.HashMapResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Checks that the {@link ViewHierarchyElement#getAccessibilityClassName()} is supported by
 * accessibility services.
 *
 * <p>Screen readers and other accessibility services may announce information about the type of an
 * interface element. For example, a screen reader may speak "checkbox" or "button" after the label
 * associated with one of these controls. These types are conveyed by the class name reported to
 * services by the {@code AccessibilityNodeInfo}'s {@code className} field. However, apps sometimes
 * use custom views, and accessibility services may not handle these views correctly if they do not
 * recognize their types.
 *
 * <p>When an {@link AccessibilityHierarchy} is constructed from {@code AccessibilityNodeInfo} data,
 * the {@link ViewHierarchyElement}'s {@code accessibilityClassName} is populated with a reliable
 * class name. So when an element is visible and important for accessibility, and if the {@code
 * accessibilityClassName} is empty, or if the name does belong to one of the standard, supported UI
 * packages, then this check will produce a {@link AccessibilityCheckResultType#WARNING WARNING}.
 *
 * <p>When an {@code AccessibilityHierarchy} is constructed from {@code View} data on API < 23, the
 * {@code className} that would be populated within its {@code AccessibilityNodeInfo} representation
 * cannot be determined reliably, so the {@code ViewHierarchyElement}'s {@code
 * accessibilityClassName} is set to {@code null}, and this check will produce only {@link
 * AccessibilityCheckResultType#NOT_RUN NOT_RUN} results, essentially making this check a no-op.
 *
 * @see <a href="https://support.google.com/accessibility/android/answer/7661305">Unsupported item
 *     type</a>
 */
public class ClassNameCheck extends AccessibilityHierarchyCheck {

  /** Result when the view is not visible */
  public static final int RESULT_ID_NOT_VISIBLE = 1;
  /** Result when the view is not {@code importantForAccessibility}. */
  public static final int RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY = 2;
  /** Result when the view's {@code CharSequence} class name is null. */
  public static final int RESULT_ID_CLASS_NAME_UNKNOWN = 3;
  /** Result when the view's {@code CharSequence} class name is empty. */
  public static final int RESULT_ID_CLASS_NAME_IS_EMPTY = 4;
  /**
   * Result when the view's {@code CharSequence} class name is not supported by the accessibility
   * service.
   */
  public static final int RESULT_ID_CLASS_NAME_NOT_SUPPORTED = 5;

  /**
   * Result metadata key for the {@code String} accessibility class name for an element. Populated
   * in results with {@link #RESULT_ID_CLASS_NAME_NOT_SUPPORTED}
   */
  public static final String KEY_ACCESSIBILITY_CLASS_NAME = "KEY_ACCESSIBILITY_CLASS_NAME";

  private static final ImmutableSet<String> VALID_UI_PACKAGE_NAME_PREFIXES =
      ImmutableSet.of(
          "android.app",
          "android.appwidget",
          "android.inputmethodservice",
          "android.support",
          "android.view",
          "android.webkit",
          "android.widget",
          "androidx");

  @Override
  protected @Nullable String getHelpTopic() {
    return "7661305";
  }

  @Override
  public Category getCategory() {
    return Category.IMPLEMENTATION;
  }

  @Override
  public List<AccessibilityHierarchyCheckResult> runCheckOnHierarchy(
      AccessibilityHierarchy hierarchy,
      @Nullable ViewHierarchyElement fromRoot,
      @Nullable Parameters parameters) {
    List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();

    List<? extends ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
    for (ViewHierarchyElement view : viewsToEval) {
      if (!view.isImportantForAccessibility()) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY,
                null));
        continue;
      }

      if (!TRUE.equals(view.isVisibleToUser())) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_NOT_VISIBLE,
                null));
        continue;
      }

      CharSequence className = view.getAccessibilityClassName();
      if (className == null) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.NOT_RUN,
                view,
                RESULT_ID_CLASS_NAME_UNKNOWN,
                null));
        continue;
      }

      if (TextUtils.isEmpty(className)) {
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.WARNING,
                view,
                RESULT_ID_CLASS_NAME_IS_EMPTY,
                null));
        continue;
      }

      boolean isValidUiClass = false;
      for (String packageName : VALID_UI_PACKAGE_NAME_PREFIXES) {
        if (className.toString().startsWith(packageName)) {
          isValidUiClass = true;
          break;
        }
      }

      if (!isValidUiClass) {
        ResultMetadata resultMetadata = new HashMapResultMetadata();
        resultMetadata.putString(KEY_ACCESSIBILITY_CLASS_NAME, className.toString());
        results.add(
            new AccessibilityHierarchyCheckResult(
                this.getClass(),
                AccessibilityCheckResultType.WARNING,
                view,
                RESULT_ID_CLASS_NAME_NOT_SUPPORTED,
                resultMetadata));
      }
    }
    return results;
  }

  @Override
  public String getMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    switch (resultId) {
      case RESULT_ID_NOT_VISIBLE:
        return StringManager.getString(locale, "result_message_not_visible");
      case RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY:
        return StringManager.getString(locale, "result_message_not_important_for_accessibility");
      case RESULT_ID_CLASS_NAME_UNKNOWN:
        return StringManager.getString(locale, "result_message_class_name_is_unknown");
      case RESULT_ID_CLASS_NAME_IS_EMPTY:
        return StringManager.getString(locale, "result_message_class_name_is_empty");
      case RESULT_ID_CLASS_NAME_NOT_SUPPORTED:
        // Metadata will have been set for this result ID
        checkNotNull(metadata);
        return String.format(
            StringManager.getString(locale, "result_message_class_name_not_supported_detail"),
            checkNotNull(metadata.getString(KEY_ACCESSIBILITY_CLASS_NAME)));
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getShortMessageForResultData(
      Locale locale, int resultId, @Nullable ResultMetadata metadata) {
    switch (resultId) {
      case RESULT_ID_NOT_VISIBLE:
        return StringManager.getString(locale, "result_message_not_visible");
      case RESULT_ID_NOT_IMPORTANT_FOR_ACCESSIBILITY:
        return StringManager.getString(locale, "result_message_not_important_for_accessibility");
      case RESULT_ID_CLASS_NAME_UNKNOWN:
        return StringManager.getString(locale, "result_message_class_name_is_unknown");
      case RESULT_ID_CLASS_NAME_IS_EMPTY:
      case RESULT_ID_CLASS_NAME_NOT_SUPPORTED:
        return StringManager.getString(locale, "result_message_class_name_not_supported_brief");
      default:
        throw new IllegalStateException("Unsupported result id");
    }
  }

  @Override
  public String getTitleMessage(Locale locale) {
    return StringManager.getString(locale, "check_title_class_name_not_supported");
  }
}

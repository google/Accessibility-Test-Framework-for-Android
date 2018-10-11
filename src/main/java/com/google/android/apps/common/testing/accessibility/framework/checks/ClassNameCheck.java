package com.google.android.apps.common.testing.accessibility.framework.checks;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Boolean.TRUE;

import androidx.annotation.Nullable;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck.Category;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Metadata;
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Checks that the {@link ViewHierarchyElement#getAccessibilityClassName()} is supported by the
 * accessibility service.
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

  private static final List<String> VALID_UI_PACKAGE_NAMES = new ArrayList<>(11);

  static {
    VALID_UI_PACKAGE_NAMES.add("android.app");
    VALID_UI_PACKAGE_NAMES.add("android.appwidget");
    VALID_UI_PACKAGE_NAMES.add("android.inputmethodservice");
    VALID_UI_PACKAGE_NAMES.add("android.support");
    VALID_UI_PACKAGE_NAMES.add("android.view");
    VALID_UI_PACKAGE_NAMES.add("android.webkit");
    VALID_UI_PACKAGE_NAMES.add("android.widget");
  }

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
      @Nullable Metadata metadata) {
    List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();

    List<ViewHierarchyElement> viewsToEval = getElementsToEvaluate(fromRoot, hierarchy);
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

      if (!(TRUE.equals(view.isVisibleToUser()))) {
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
      for (String packageName : VALID_UI_PACKAGE_NAMES) {
        if (className.toString().startsWith(packageName)) {
          isValidUiClass = true;
          break;
        }
      }

      if (!isValidUiClass) {
        Metadata resultMetadata = new Metadata();
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

package com.google.android.apps.common.testing.accessibility.framework;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.Image;

/**
 * Result generated when an accessibility check runs on a {@code ViewHierarchyElement}. Includes an
 * image of the visible region of the View that is the subject of the result.
 *
 * <p>The Image should be regarded as supplemental information regarding the result, and not part of
 * the result. It presence does not affect the equality or hashCode of a result, and it will not be
 * preserved by the {@link #toProto()} method.
 */
public class AccessibilityHierarchyCheckResultWithImage extends AccessibilityHierarchyCheckResult {

  private final Image viewImage;

  /**
   * @param checkClass The class of the check reporting the error
   * @param type The type of result
   * @param element The element that the result pertains to
   * @param resultId an integer unique to all results emitted from a single class
   * @param metadata extra data about this result
   * @param viewImage An image of the visible region of the view that is the subject of the result
   */
  public AccessibilityHierarchyCheckResultWithImage(
      Class<? extends AccessibilityHierarchyCheck> checkClass,
      AccessibilityCheckResultType type,
      ViewHierarchyElement element,
      int resultId,
      ResultMetadata metadata,
      Image viewImage) {
    super(checkClass, type, element, resultId, metadata);
    this.viewImage = viewImage;
  }

  @Override
  public AccessibilityHierarchyCheckResultWithImage getSuppressedResultCopy() {
    return new AccessibilityHierarchyCheckResultWithImage(
        getSourceCheckClass().asSubclass(AccessibilityHierarchyCheck.class),
        AccessibilityCheckResultType.SUPPRESSED,
        checkNotNull(getElement()),
        getResultId(),
        checkNotNull(getMetadata()),
        viewImage);
  }

  /**
   * Returns an image of the visible region of the View that is the subject of the result, if an
   * image was given.
   */
  public Image getViewImage() {
    return viewImage;
  }
}

package com.google.android.apps.common.testing.accessibility.framework.ocr;

import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.Image;

/**
 * Platform-independent representation of an OCR engine which does actual character identification.
 */
public interface OcrEngine {

  /**
   * Detects and recognizes texts in a screenshot.
   *
   * @param screenshot The screenshot to detect and recognize texts from
   * @return A {@link OcrResult} which contains a list of recognized texts
   */
  OcrResult detect(Image screenshot);
}

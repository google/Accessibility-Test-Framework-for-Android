package com.google.android.apps.common.testing.accessibility.framework.ocr;

import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.OcrResultProto;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;

/** The OCR text recognition result from a screenshot */
public class OcrResult {

  public static final OcrResult EMPTY_RESULT = new OcrResult(ImmutableList.of());

  private final ImmutableList<TextComponent> texts;

  public OcrResult(ImmutableList<TextComponent> texts) {
    this.texts = texts;
  }

  public OcrResult(OcrResultProto ocrResultProto) {
    ImmutableList.Builder<TextComponent> builder = new ImmutableList.Builder<>();
    for (int i = 0; i < ocrResultProto.getTextsCount(); i++) {
      builder.add(new TextComponent(ocrResultProto.getTexts(i)));
    }
    texts = builder.build();
  }

  /** Returns an immutable list of recognized texts from the screenshot. */
  public ImmutableList<TextComponent> getTexts() {
    return texts;
  }

  /** Creates a protocol buffer for this {@link OcrResult} following its format. */
  public OcrResultProto toProto() {
    OcrResultProto.Builder builder = OcrResultProto.newBuilder();
    for (TextComponent text : texts) {
      builder.addTexts(text.toProto());
    }
    return builder.build();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OcrResult)) {
      return false;
    }

    OcrResult that = (OcrResult) o;
    return texts.equals(that.getTexts());
  }

  @Override
  public int hashCode() {
    return texts.hashCode();
  }
}

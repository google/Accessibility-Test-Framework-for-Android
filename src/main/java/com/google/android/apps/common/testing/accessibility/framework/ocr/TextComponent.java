package com.google.android.apps.common.testing.accessibility.framework.ocr;

import com.google.android.apps.common.testing.accessibility.framework.proto.AccessibilityEvaluationProtos.TextComponentProto;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents every entity across the hierarchy of recognized text. An entity may contain other
 * smaller entities, or may be an atom.
 */
public class TextComponent {

  private final String value;

  private final Rect boundsInScreen;

  private final @Nullable String language;

  private final @Nullable Float confidence;

  private final ImmutableList<TextComponent> components;

  private TextComponent(
      String value,
      Rect boundsInScreen,
      @Nullable String language,
      @Nullable Float confidence,
      ImmutableList<TextComponent> components) {
    this.value = value;
    this.boundsInScreen = boundsInScreen;
    this.language = language;
    this.confidence = confidence;
    this.components = components;
  }

  TextComponent(TextComponentProto proto) {
    this.value = proto.getValue();
    this.boundsInScreen = new Rect(proto.getBoundsInScreen());
    this.language = proto.hasLanguage() ? proto.getLanguage() : null;
    this.confidence = proto.hasConfidence() ? proto.getConfidence() : null;
    ImmutableList.Builder<TextComponent> builder = new ImmutableList.Builder<>();
    for (int i = 0; i < proto.getComponentsCount(); i++) {
      builder.add(new TextComponent(proto.getComponents(i)));
    }
    this.components = builder.build();
  }

  /** Returns the recognized text as a string. */
  public String getValue() {
    return value;
  }

  /** Returns the bounding box containing the text in screen coordinates. */
  public Rect getBoundsInScreen() {
    return boundsInScreen;
  }

  /**
   * Returns the prevailing language in the text, or {@code null} if the information is not
   * available.
   */
  public @Nullable String getLanguage() {
    return language;
  }

  /**
   * Returns the confidence score of the recognized text, or {@code null} if the information is not
   * available.
   *
   * <p>The value of the confidence score is between 0.0 and 1.0.
   */
  public @Nullable Float getConfidence() {
    return confidence;
  }

  /**
   * Returns a list of smaller components that comprise this entity. If this entity is an atom, an
   * empty list will be returned.
   */
  public List<TextComponent> getComponents() {
    return components;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TextComponent)) {
      return false;
    }

    TextComponent that = (TextComponent) o;
    return value.equals(that.getValue())
        && boundsInScreen.equals(that.getBoundsInScreen())
        && Objects.equals(language, that.getLanguage())
        && Objects.equals(confidence, that.getConfidence())
        && components.equals(that.getComponents());
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, boundsInScreen, language, confidence, components);
  }

  /**
   * Returns a new {@link Builder} that can build a {@link TextComponent} from the text as a string
   * and a bounding box which contains the text.
   */
  public static Builder newBuilder(String value, Rect boundsInScreen) {
    return new Builder(value, boundsInScreen);
  }

  TextComponentProto toProto() {
    TextComponentProto.Builder builder =
        TextComponentProto.newBuilder().setValue(value).setBoundsInScreen(boundsInScreen.toProto());
    if (language != null) {
      builder.setLanguage(language);
    }
    if (confidence != null) {
      builder.setConfidence(confidence);
    }
    for (TextComponent text : components) {
      builder.addComponents(text.toProto());
    }
    return builder.build();
  }

  /** A builder for {@link TextComponent}. */
  public static class Builder {

    private final Rect boundsInScreen;

    private final String value;

    private @Nullable String language;

    private @Nullable Float confidence;

    private ImmutableList<TextComponent> components = ImmutableList.of();

    private Builder(String value, Rect boundsInScreen) {
      this.value = value;
      this.boundsInScreen = boundsInScreen;
    }

    @CanIgnoreReturnValue
    public Builder setLanguage(String language) {
      this.language = language;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder setConfidence(float confidence) {
      this.confidence = confidence;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder setTextComponents(ImmutableList<TextComponent> textComponents) {
      this.components = textComponents;
      return this;
    }

    public TextComponent build() {
      return new TextComponent(value, boundsInScreen, language, confidence, components);
    }
  }
}

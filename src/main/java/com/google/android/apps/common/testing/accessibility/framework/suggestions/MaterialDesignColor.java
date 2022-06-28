package com.google.android.apps.common.testing.accessibility.framework.suggestions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.ContrastUtils;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Material Design color palette.
 * {@see https://www.google.com/design/spec/style/color.html#color-color-palette}
 */
public enum MaterialDesignColor {
  RED(
      "Red",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFEBEE)
          .put(Shade.PRIMARY_100, 0xFFCDD2)
          .put(Shade.PRIMARY_200, 0xEF9A9A)
          .put(Shade.PRIMARY_300, 0xE57373)
          .put(Shade.PRIMARY_400, 0xEF5350)
          .put(Shade.PRIMARY_500, 0xF44336)
          .put(Shade.PRIMARY_600, 0xE53935)
          .put(Shade.PRIMARY_700, 0xD32F2F)
          .put(Shade.PRIMARY_800, 0xC62828)
          .put(Shade.PRIMARY_900, 0xB71C1C)
          .put(Shade.ACCENT_100, 0xFF8A80)
          .put(Shade.ACCENT_200, 0xFF5252)
          .put(Shade.ACCENT_400, 0xFF1744)
          .put(Shade.ACCENT_700, 0xD50000)
          .buildOrThrow()),

  PINK(
      "Pink",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFCE4EC)
          .put(Shade.PRIMARY_100, 0xF8BBD0)
          .put(Shade.PRIMARY_200, 0xF48FB1)
          .put(Shade.PRIMARY_300, 0xF06292)
          .put(Shade.PRIMARY_400, 0xEC407A)
          .put(Shade.PRIMARY_500, 0xE91E63)
          .put(Shade.PRIMARY_600, 0xD81B60)
          .put(Shade.PRIMARY_700, 0xC2185B)
          .put(Shade.PRIMARY_800, 0xAD1457)
          .put(Shade.PRIMARY_900, 0x880E4F)
          .put(Shade.ACCENT_100, 0xFF80AB)
          .put(Shade.ACCENT_200, 0xFF4081)
          .put(Shade.ACCENT_400, 0xF50057)
          .put(Shade.ACCENT_700, 0xC51162)
          .buildOrThrow()),

  PURPLE(
      "Purple",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xF3E5F5)
          .put(Shade.PRIMARY_100, 0xE1BEE7)
          .put(Shade.PRIMARY_200, 0xCE93D8)
          .put(Shade.PRIMARY_300, 0xBA68C8)
          .put(Shade.PRIMARY_400, 0xAB47BC)
          .put(Shade.PRIMARY_500, 0x9C27B0)
          .put(Shade.PRIMARY_600, 0x8E24AA)
          .put(Shade.PRIMARY_700, 0x7B1FA2)
          .put(Shade.PRIMARY_800, 0x6A1B9A)
          .put(Shade.PRIMARY_900, 0x4A148C)
          .put(Shade.ACCENT_100, 0xEA80FC)
          .put(Shade.ACCENT_200, 0xE040FB)
          .put(Shade.ACCENT_400, 0xD500F9)
          .put(Shade.ACCENT_700, 0xAA00FF)
          .buildOrThrow()),

  DEEP_PURPLE(
      "Deep Purple",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xEDE7F6)
          .put(Shade.PRIMARY_100, 0xD1C4E9)
          .put(Shade.PRIMARY_200, 0xB39DDB)
          .put(Shade.PRIMARY_300, 0x9575CD)
          .put(Shade.PRIMARY_400, 0x7E57C2)
          .put(Shade.PRIMARY_500, 0x673AB7)
          .put(Shade.PRIMARY_600, 0x5E35B1)
          .put(Shade.PRIMARY_700, 0x512DA8)
          .put(Shade.PRIMARY_800, 0x4527A0)
          .put(Shade.PRIMARY_900, 0x311B92)
          .put(Shade.ACCENT_100, 0xB388FF)
          .put(Shade.ACCENT_200, 0x7C4DFF)
          .put(Shade.ACCENT_400, 0x651FFF)
          .put(Shade.ACCENT_700, 0x6200EA)
          .buildOrThrow()),

  INDIGO(
      "Indigo",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xE8EAF6)
          .put(Shade.PRIMARY_100, 0xC5CAE9)
          .put(Shade.PRIMARY_200, 0x9FA8DA)
          .put(Shade.PRIMARY_300, 0x7986CB)
          .put(Shade.PRIMARY_400, 0x5C6BC0)
          .put(Shade.PRIMARY_500, 0x3F51B5)
          .put(Shade.PRIMARY_600, 0x3949AB)
          .put(Shade.PRIMARY_700, 0x303F9F)
          .put(Shade.PRIMARY_800, 0x283593)
          .put(Shade.PRIMARY_900, 0x1A237E)
          .put(Shade.ACCENT_100, 0x8C9EFF)
          .put(Shade.ACCENT_200, 0x536DFE)
          .put(Shade.ACCENT_400, 0x3D5AFE)
          .put(Shade.ACCENT_700, 0x304FFE)
          .buildOrThrow()),

  BLUE(
      "Blue",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xE3F2FD)
          .put(Shade.PRIMARY_100, 0xBBDEFB)
          .put(Shade.PRIMARY_200, 0x90CAF9)
          .put(Shade.PRIMARY_300, 0x64B5F6)
          .put(Shade.PRIMARY_400, 0x42A5F5)
          .put(Shade.PRIMARY_500, 0x2196F3)
          .put(Shade.PRIMARY_600, 0x1E88E5)
          .put(Shade.PRIMARY_700, 0x1976D2)
          .put(Shade.PRIMARY_800, 0x1565C0)
          .put(Shade.PRIMARY_900, 0x0D47A1)
          .put(Shade.ACCENT_100, 0x82B1FF)
          .put(Shade.ACCENT_200, 0x448AFF)
          .put(Shade.ACCENT_400, 0x2979FF)
          .put(Shade.ACCENT_700, 0x2962FF)
          .buildOrThrow()),

  LIGHT_BLUE(
      "Light Blue",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xE1F5FE)
          .put(Shade.PRIMARY_100, 0xB3E5FC)
          .put(Shade.PRIMARY_200, 0x81D4FA)
          .put(Shade.PRIMARY_300, 0x4FC3F7)
          .put(Shade.PRIMARY_400, 0x29B6F6)
          .put(Shade.PRIMARY_500, 0x03A9F4)
          .put(Shade.PRIMARY_600, 0x039BE5)
          .put(Shade.PRIMARY_700, 0x0288D1)
          .put(Shade.PRIMARY_800, 0x0277BD)
          .put(Shade.PRIMARY_900, 0x01579B)
          .put(Shade.ACCENT_100, 0x80D8FF)
          .put(Shade.ACCENT_200, 0x40C4FF)
          .put(Shade.ACCENT_400, 0x00B0FF)
          .put(Shade.ACCENT_700, 0x0091EA)
          .buildOrThrow()),

  CYAN(
      "Cyan",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xE0F7FA)
          .put(Shade.PRIMARY_100, 0xB2EBF2)
          .put(Shade.PRIMARY_200, 0x80DEEA)
          .put(Shade.PRIMARY_300, 0x4DD0E1)
          .put(Shade.PRIMARY_400, 0x26C6DA)
          .put(Shade.PRIMARY_500, 0x00BCD4)
          .put(Shade.PRIMARY_600, 0x00ACC1)
          .put(Shade.PRIMARY_700, 0x0097A7)
          .put(Shade.PRIMARY_800, 0x00838F)
          .put(Shade.PRIMARY_900, 0x006064)
          .put(Shade.ACCENT_100, 0x84FFFF)
          .put(Shade.ACCENT_200, 0x18FFFF)
          .put(Shade.ACCENT_400, 0x00E5FF)
          .put(Shade.ACCENT_700, 0x00B8D4)
          .buildOrThrow()),

  TEAL(
      "Teal",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xE0F2F1)
          .put(Shade.PRIMARY_100, 0xB2DFDB)
          .put(Shade.PRIMARY_200, 0x80CBC4)
          .put(Shade.PRIMARY_300, 0x4DB6AC)
          .put(Shade.PRIMARY_400, 0x26A69A)
          .put(Shade.PRIMARY_500, 0x009688)
          .put(Shade.PRIMARY_600, 0x00897B)
          .put(Shade.PRIMARY_700, 0x00796B)
          .put(Shade.PRIMARY_800, 0x00695C)
          .put(Shade.PRIMARY_900, 0x004D40)
          .put(Shade.ACCENT_100, 0xA7FFEB)
          .put(Shade.ACCENT_200, 0x64FFDA)
          .put(Shade.ACCENT_400, 0x1DE9B6)
          .put(Shade.ACCENT_700, 0x00BFA5)
          .buildOrThrow()),

  GREEN(
      "Green",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xE8F5E9)
          .put(Shade.PRIMARY_100, 0xC8E6C9)
          .put(Shade.PRIMARY_200, 0xA5D6A7)
          .put(Shade.PRIMARY_300, 0x81C784)
          .put(Shade.PRIMARY_400, 0x66BB6A)
          .put(Shade.PRIMARY_500, 0x4CAF50)
          .put(Shade.PRIMARY_600, 0x43A047)
          .put(Shade.PRIMARY_700, 0x388E3C)
          .put(Shade.PRIMARY_800, 0x2E7D32)
          .put(Shade.PRIMARY_900, 0x1B5E20)
          .put(Shade.ACCENT_100, 0xB9F6CA)
          .put(Shade.ACCENT_200, 0x69F0AE)
          .put(Shade.ACCENT_400, 0x00E676)
          .put(Shade.ACCENT_700, 0x00C853)
          .buildOrThrow()),

  LIGHT_GREEN(
      "Light Green",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xF1F8E9)
          .put(Shade.PRIMARY_100, 0xDCEDC8)
          .put(Shade.PRIMARY_200, 0xC5E1A5)
          .put(Shade.PRIMARY_300, 0xAED581)
          .put(Shade.PRIMARY_400, 0x9CCC65)
          .put(Shade.PRIMARY_500, 0x8BC34A)
          .put(Shade.PRIMARY_600, 0x7CB342)
          .put(Shade.PRIMARY_700, 0x689F38)
          .put(Shade.PRIMARY_800, 0x558B2F)
          .put(Shade.PRIMARY_900, 0x33691E)
          .put(Shade.ACCENT_100, 0xCCFF90)
          .put(Shade.ACCENT_200, 0xB2FF59)
          .put(Shade.ACCENT_400, 0x76FF03)
          .put(Shade.ACCENT_700, 0x64DD17)
          .buildOrThrow()),

  LIME(
      "Lime",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xF9FBE7)
          .put(Shade.PRIMARY_100, 0xF0F4C3)
          .put(Shade.PRIMARY_200, 0xE6EE9C)
          .put(Shade.PRIMARY_300, 0xDCE775)
          .put(Shade.PRIMARY_400, 0xD4E157)
          .put(Shade.PRIMARY_500, 0xCDDC39)
          .put(Shade.PRIMARY_600, 0xC0CA33)
          .put(Shade.PRIMARY_700, 0xAFB42B)
          .put(Shade.PRIMARY_800, 0x9E9D24)
          .put(Shade.PRIMARY_900, 0x827717)
          .put(Shade.ACCENT_100, 0xF4FF81)
          .put(Shade.ACCENT_200, 0xEEFF41)
          .put(Shade.ACCENT_400, 0xC6FF00)
          .put(Shade.ACCENT_700, 0xAEEA00)
          .buildOrThrow()),

  YELLOW(
      "Yellow",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFFDE7)
          .put(Shade.PRIMARY_100, 0xFFF9C4)
          .put(Shade.PRIMARY_200, 0xFFF59D)
          .put(Shade.PRIMARY_300, 0xFFF176)
          .put(Shade.PRIMARY_400, 0xFFEE58)
          .put(Shade.PRIMARY_500, 0xFFEB3B)
          .put(Shade.PRIMARY_600, 0xFDD835)
          .put(Shade.PRIMARY_700, 0xFBC02D)
          .put(Shade.PRIMARY_800, 0xF9A825)
          .put(Shade.PRIMARY_900, 0xF57F17)
          .put(Shade.ACCENT_100, 0xFFFF8D)
          .put(Shade.ACCENT_200, 0xFFFF00)
          .put(Shade.ACCENT_400, 0xFFEA00)
          .put(Shade.ACCENT_700, 0xFFD600)
          .buildOrThrow()),

  AMBER(
      "Amber",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFF8E1)
          .put(Shade.PRIMARY_100, 0xFFECB3)
          .put(Shade.PRIMARY_200, 0xFFE082)
          .put(Shade.PRIMARY_300, 0xFFD54F)
          .put(Shade.PRIMARY_400, 0xFFCA28)
          .put(Shade.PRIMARY_500, 0xFFC107)
          .put(Shade.PRIMARY_600, 0xFFB300)
          .put(Shade.PRIMARY_700, 0xFFA000)
          .put(Shade.PRIMARY_800, 0xFF8F00)
          .put(Shade.PRIMARY_900, 0xFF6F00)
          .put(Shade.ACCENT_100, 0xFFE57F)
          .put(Shade.ACCENT_200, 0xFFD740)
          .put(Shade.ACCENT_400, 0xFFC400)
          .put(Shade.ACCENT_700, 0xFFAB00)
          .buildOrThrow()),

  ORANGE(
      "Orange",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFF3E0)
          .put(Shade.PRIMARY_100, 0xFFE0B2)
          .put(Shade.PRIMARY_200, 0xFFCC80)
          .put(Shade.PRIMARY_300, 0xFFB74D)
          .put(Shade.PRIMARY_400, 0xFFA726)
          .put(Shade.PRIMARY_500, 0xFF9800)
          .put(Shade.PRIMARY_600, 0xFB8C00)
          .put(Shade.PRIMARY_700, 0xF57C00)
          .put(Shade.PRIMARY_800, 0xEF6C00)
          .put(Shade.PRIMARY_900, 0xE65100)
          .put(Shade.ACCENT_100, 0xFFD180)
          .put(Shade.ACCENT_200, 0xFFAB40)
          .put(Shade.ACCENT_400, 0xFF9100)
          .put(Shade.ACCENT_700, 0xFF6D00)
          .buildOrThrow()),

  DEEP_ORANGE(
      "Deep Orange",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFBE9E7)
          .put(Shade.PRIMARY_100, 0xFFCCBC)
          .put(Shade.PRIMARY_200, 0xFFAB91)
          .put(Shade.PRIMARY_300, 0xFF8A65)
          .put(Shade.PRIMARY_400, 0xFF7043)
          .put(Shade.PRIMARY_500, 0xFF5722)
          .put(Shade.PRIMARY_600, 0xF4511E)
          .put(Shade.PRIMARY_700, 0xE64A19)
          .put(Shade.PRIMARY_800, 0xD84315)
          .put(Shade.PRIMARY_900, 0xBF360C)
          .put(Shade.ACCENT_100, 0xFF9E80)
          .put(Shade.ACCENT_200, 0xFF6E40)
          .put(Shade.ACCENT_400, 0xFF3D00)
          .put(Shade.ACCENT_700, 0xDD2C00)
          .buildOrThrow()),

  BROWN(
      "Brown",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xEFEBE9)
          .put(Shade.PRIMARY_100, 0xD7CCC8)
          .put(Shade.PRIMARY_200, 0xBCAAA4)
          .put(Shade.PRIMARY_300, 0xA1887F)
          .put(Shade.PRIMARY_400, 0x8D6E63)
          .put(Shade.PRIMARY_500, 0x795548)
          .put(Shade.PRIMARY_600, 0x6D4C41)
          .put(Shade.PRIMARY_700, 0x5D4037)
          .put(Shade.PRIMARY_800, 0x4E342E)
          .put(Shade.PRIMARY_900, 0x3E2723)
          .buildOrThrow()),

  GREY(
      "Grey",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFAFAFA)
          .put(Shade.PRIMARY_100, 0xF5F5F5)
          .put(Shade.PRIMARY_200, 0xEEEEEE)
          .put(Shade.PRIMARY_300, 0xE0E0E0)
          .put(Shade.PRIMARY_400, 0xBDBDBD)
          .put(Shade.PRIMARY_500, 0x9E9E9E)
          .put(Shade.PRIMARY_600, 0x757575)
          .put(Shade.PRIMARY_700, 0x616161)
          .put(Shade.PRIMARY_800, 0x424242)
          .put(Shade.PRIMARY_900, 0x212121)
          .buildOrThrow()),

  BLUE_GREY(
      "Blue Grey",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xECEFF1)
          .put(Shade.PRIMARY_100, 0xCFD8DC)
          .put(Shade.PRIMARY_200, 0xB0BEC5)
          .put(Shade.PRIMARY_300, 0x90A4AE)
          .put(Shade.PRIMARY_400, 0x78909C)
          .put(Shade.PRIMARY_500, 0x607D8B)
          .put(Shade.PRIMARY_600, 0x546E7A)
          .put(Shade.PRIMARY_700, 0x455A64)
          .put(Shade.PRIMARY_800, 0x37474F)
          .put(Shade.PRIMARY_900, 0x263238)
          .buildOrThrow()),

  BLACK("Black", ImmutableBiMap.of(Shade.PRIMARY_500, 0x000000)),

  WHITE("White", ImmutableBiMap.of(Shade.PRIMARY_500, 0xFFFFFF));

  /**
   * Standard luminance scale to rank colors on.
   * Some colors may not have every shade on this scale.
   */
  public enum Shade {
    PRIMARY_050("50"),
    PRIMARY_100("100"),
    PRIMARY_200("200"),
    PRIMARY_300("300"),
    PRIMARY_400("400"),
    PRIMARY_500("500"),
    PRIMARY_600("600"),
    PRIMARY_700("700"),
    PRIMARY_800("800"),
    PRIMARY_900("900"),
    ACCENT_100("A100"),
    ACCENT_200("A200"),
    ACCENT_400("A400"),
    ACCENT_700("A700");

    public static ImmutableList<Shade> primaryShades =
        ImmutableList.of(
            PRIMARY_050,
            PRIMARY_100,
            PRIMARY_200,
            PRIMARY_300,
            PRIMARY_400,
            PRIMARY_500,
            PRIMARY_600,
            PRIMARY_700,
            PRIMARY_800,
            PRIMARY_900);

    public static ImmutableList<Shade> accentShades =
        ImmutableList.of(ACCENT_100, ACCENT_200, ACCENT_400, ACCENT_700);

    private final String code;

    public String getCode() {
      return code;
    }

    Shade(String code) {
      this.code = code;
    }
  }

  private final String name;
  private final ImmutableBiMap<Shade, Integer> colorMap;

  MaterialDesignColor(String name, ImmutableBiMap<Shade, Integer> colorMap) {
    this.name = name;
    this.colorMap = colorMap;
  }

  public String getName() {
    return name;
  }

  public ImmutableBiMap<Shade, Integer> getColorMap() {
    return colorMap;
  }

  /** Converts a rgb value to a {@link MaterialDesignColor}. */
  public static @Nullable MaterialDesignColor fromColor(int color) {
    for (MaterialDesignColor materialDesignColor : MaterialDesignColor.values()) {
      if (materialDesignColor.getColorMap().containsValue(color)) {
        return materialDesignColor;
      }
    }
    return null;
  }

  /** Determines whether a given color is in material design palette. */
  public static boolean isMaterialDesignColor(int color) {
    return fromColor(color) != null;
  }

  /**
   * Returns a {@link MaterialDesignColor} which contains the color closest to the given color in
   * terms of color distance.
   */
  public static MaterialDesignColor findClosestColor(int color) {
    MaterialDesignColor materialDesignColor = fromColor(color);
    if (materialDesignColor != null) {
      return materialDesignColor;
    }

    double minColorDistance = Double.MAX_VALUE;
    MaterialDesignColor closestColor = null;
    for (MaterialDesignColor designColor : MaterialDesignColor.values()) {
      for (int testColor : designColor.getColorMap().values()) {
        double colorDistance = ContrastUtils.colorDifference(testColor, color);
        if (minColorDistance > colorDistance) {
          minColorDistance = colorDistance;
          closestColor = designColor;
        }
      }
    }
    return checkNotNull(closestColor);
  }
}

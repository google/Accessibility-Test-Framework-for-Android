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
          .put(Shade.PRIMARY_050, 0xFFFFEBEE)
          .put(Shade.PRIMARY_100, 0xFFFFCDD2)
          .put(Shade.PRIMARY_200, 0xFFEF9A9A)
          .put(Shade.PRIMARY_300, 0xFFE57373)
          .put(Shade.PRIMARY_400, 0xFFEF5350)
          .put(Shade.PRIMARY_500, 0xFFF44336)
          .put(Shade.PRIMARY_600, 0xFFE53935)
          .put(Shade.PRIMARY_700, 0xFFD32F2F)
          .put(Shade.PRIMARY_800, 0xFFC62828)
          .put(Shade.PRIMARY_900, 0xFFB71C1C)
          .put(Shade.ACCENT_100, 0xFFFF8A80)
          .put(Shade.ACCENT_200, 0xFFFF5252)
          .put(Shade.ACCENT_400, 0xFFFF1744)
          .put(Shade.ACCENT_700, 0xFFD50000)
          .buildOrThrow()),

  PINK(
      "Pink",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFFCE4EC)
          .put(Shade.PRIMARY_100, 0xFFF8BBD0)
          .put(Shade.PRIMARY_200, 0xFFF48FB1)
          .put(Shade.PRIMARY_300, 0xFFF06292)
          .put(Shade.PRIMARY_400, 0xFFEC407A)
          .put(Shade.PRIMARY_500, 0xFFE91E63)
          .put(Shade.PRIMARY_600, 0xFFD81B60)
          .put(Shade.PRIMARY_700, 0xFFC2185B)
          .put(Shade.PRIMARY_800, 0xFFAD1457)
          .put(Shade.PRIMARY_900, 0xFF880E4F)
          .put(Shade.ACCENT_100, 0xFFFF80AB)
          .put(Shade.ACCENT_200, 0xFFFF4081)
          .put(Shade.ACCENT_400, 0xFFF50057)
          .put(Shade.ACCENT_700, 0xFFC51162)
          .buildOrThrow()),

  PURPLE(
      "Purple",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFF3E5F5)
          .put(Shade.PRIMARY_100, 0xFFE1BEE7)
          .put(Shade.PRIMARY_200, 0xFFCE93D8)
          .put(Shade.PRIMARY_300, 0xFFBA68C8)
          .put(Shade.PRIMARY_400, 0xFFAB47BC)
          .put(Shade.PRIMARY_500, 0xFF9C27B0)
          .put(Shade.PRIMARY_600, 0xFF8E24AA)
          .put(Shade.PRIMARY_700, 0xFF7B1FA2)
          .put(Shade.PRIMARY_800, 0xFF6A1B9A)
          .put(Shade.PRIMARY_900, 0xFF4A148C)
          .put(Shade.ACCENT_100, 0xFFEA80FC)
          .put(Shade.ACCENT_200, 0xFFE040FB)
          .put(Shade.ACCENT_400, 0xFFD500F9)
          .put(Shade.ACCENT_700, 0xFFAA00FF)
          .buildOrThrow()),

  DEEP_PURPLE(
      "Deep Purple",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFEDE7F6)
          .put(Shade.PRIMARY_100, 0xFFD1C4E9)
          .put(Shade.PRIMARY_200, 0xFFB39DDB)
          .put(Shade.PRIMARY_300, 0xFF9575CD)
          .put(Shade.PRIMARY_400, 0xFF7E57C2)
          .put(Shade.PRIMARY_500, 0xFF673AB7)
          .put(Shade.PRIMARY_600, 0xFF5E35B1)
          .put(Shade.PRIMARY_700, 0xFF512DA8)
          .put(Shade.PRIMARY_800, 0xFF4527A0)
          .put(Shade.PRIMARY_900, 0xFF311B92)
          .put(Shade.ACCENT_100, 0xFFB388FF)
          .put(Shade.ACCENT_200, 0xFF7C4DFF)
          .put(Shade.ACCENT_400, 0xFF651FFF)
          .put(Shade.ACCENT_700, 0xFF6200EA)
          .buildOrThrow()),

  INDIGO(
      "Indigo",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFE8EAF6)
          .put(Shade.PRIMARY_100, 0xFFC5CAE9)
          .put(Shade.PRIMARY_200, 0xFF9FA8DA)
          .put(Shade.PRIMARY_300, 0xFF7986CB)
          .put(Shade.PRIMARY_400, 0xFF5C6BC0)
          .put(Shade.PRIMARY_500, 0xFF3F51B5)
          .put(Shade.PRIMARY_600, 0xFF3949AB)
          .put(Shade.PRIMARY_700, 0xFF303F9F)
          .put(Shade.PRIMARY_800, 0xFF283593)
          .put(Shade.PRIMARY_900, 0xFF1A237E)
          .put(Shade.ACCENT_100, 0xFF8C9EFF)
          .put(Shade.ACCENT_200, 0xFF536DFE)
          .put(Shade.ACCENT_400, 0xFF3D5AFE)
          .put(Shade.ACCENT_700, 0xFF304FFE)
          .buildOrThrow()),

  BLUE(
      "Blue",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFE3F2FD)
          .put(Shade.PRIMARY_100, 0xFFBBDEFB)
          .put(Shade.PRIMARY_200, 0xFF90CAF9)
          .put(Shade.PRIMARY_300, 0xFF64B5F6)
          .put(Shade.PRIMARY_400, 0xFF42A5F5)
          .put(Shade.PRIMARY_500, 0xFF2196F3)
          .put(Shade.PRIMARY_600, 0xFF1E88E5)
          .put(Shade.PRIMARY_700, 0xFF1976D2)
          .put(Shade.PRIMARY_800, 0xFF1565C0)
          .put(Shade.PRIMARY_900, 0xFF0D47A1)
          .put(Shade.ACCENT_100, 0xFF82B1FF)
          .put(Shade.ACCENT_200, 0xFF448AFF)
          .put(Shade.ACCENT_400, 0xFF2979FF)
          .put(Shade.ACCENT_700, 0xFF2962FF)
          .buildOrThrow()),

  LIGHT_BLUE(
      "Light Blue",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFE1F5FE)
          .put(Shade.PRIMARY_100, 0xFFB3E5FC)
          .put(Shade.PRIMARY_200, 0xFF81D4FA)
          .put(Shade.PRIMARY_300, 0xFF4FC3F7)
          .put(Shade.PRIMARY_400, 0xFF29B6F6)
          .put(Shade.PRIMARY_500, 0xFF03A9F4)
          .put(Shade.PRIMARY_600, 0xFF039BE5)
          .put(Shade.PRIMARY_700, 0xFF0288D1)
          .put(Shade.PRIMARY_800, 0xFF0277BD)
          .put(Shade.PRIMARY_900, 0xFF01579B)
          .put(Shade.ACCENT_100, 0xFF80D8FF)
          .put(Shade.ACCENT_200, 0xFF40C4FF)
          .put(Shade.ACCENT_400, 0xFF00B0FF)
          .put(Shade.ACCENT_700, 0xFF0091EA)
          .buildOrThrow()),

  CYAN(
      "Cyan",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFE0F7FA)
          .put(Shade.PRIMARY_100, 0xFFB2EBF2)
          .put(Shade.PRIMARY_200, 0xFF80DEEA)
          .put(Shade.PRIMARY_300, 0xFF4DD0E1)
          .put(Shade.PRIMARY_400, 0xFF26C6DA)
          .put(Shade.PRIMARY_500, 0xFF00BCD4)
          .put(Shade.PRIMARY_600, 0xFF00ACC1)
          .put(Shade.PRIMARY_700, 0xFF0097A7)
          .put(Shade.PRIMARY_800, 0xFF00838F)
          .put(Shade.PRIMARY_900, 0xFF006064)
          .put(Shade.ACCENT_100, 0xFF84FFFF)
          .put(Shade.ACCENT_200, 0xFF18FFFF)
          .put(Shade.ACCENT_400, 0xFF00E5FF)
          .put(Shade.ACCENT_700, 0xFF00B8D4)
          .buildOrThrow()),

  TEAL(
      "Teal",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFE0F2F1)
          .put(Shade.PRIMARY_100, 0xFFB2DFDB)
          .put(Shade.PRIMARY_200, 0xFF80CBC4)
          .put(Shade.PRIMARY_300, 0xFF4DB6AC)
          .put(Shade.PRIMARY_400, 0xFF26A69A)
          .put(Shade.PRIMARY_500, 0xFF009688)
          .put(Shade.PRIMARY_600, 0xFF00897B)
          .put(Shade.PRIMARY_700, 0xFF00796B)
          .put(Shade.PRIMARY_800, 0xFF00695C)
          .put(Shade.PRIMARY_900, 0xFF004D40)
          .put(Shade.ACCENT_100, 0xFFA7FFEB)
          .put(Shade.ACCENT_200, 0xFF64FFDA)
          .put(Shade.ACCENT_400, 0xFF1DE9B6)
          .put(Shade.ACCENT_700, 0xFF00BFA5)
          .buildOrThrow()),

  GREEN(
      "Green",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFE8F5E9)
          .put(Shade.PRIMARY_100, 0xFFC8E6C9)
          .put(Shade.PRIMARY_200, 0xFFA5D6A7)
          .put(Shade.PRIMARY_300, 0xFF81C784)
          .put(Shade.PRIMARY_400, 0xFF66BB6A)
          .put(Shade.PRIMARY_500, 0xFF4CAF50)
          .put(Shade.PRIMARY_600, 0xFF43A047)
          .put(Shade.PRIMARY_700, 0xFF388E3C)
          .put(Shade.PRIMARY_800, 0xFF2E7D32)
          .put(Shade.PRIMARY_900, 0xFF1B5E20)
          .put(Shade.ACCENT_100, 0xFFB9F6CA)
          .put(Shade.ACCENT_200, 0xFF69F0AE)
          .put(Shade.ACCENT_400, 0xFF00E676)
          .put(Shade.ACCENT_700, 0xFF00C853)
          .buildOrThrow()),

  LIGHT_GREEN(
      "Light Green",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFF1F8E9)
          .put(Shade.PRIMARY_100, 0xFFDCEDC8)
          .put(Shade.PRIMARY_200, 0xFFC5E1A5)
          .put(Shade.PRIMARY_300, 0xFFAED581)
          .put(Shade.PRIMARY_400, 0xFF9CCC65)
          .put(Shade.PRIMARY_500, 0xFF8BC34A)
          .put(Shade.PRIMARY_600, 0xFF7CB342)
          .put(Shade.PRIMARY_700, 0xFF689F38)
          .put(Shade.PRIMARY_800, 0xFF558B2F)
          .put(Shade.PRIMARY_900, 0xFF33691E)
          .put(Shade.ACCENT_100, 0xFFCCFF90)
          .put(Shade.ACCENT_200, 0xFFB2FF59)
          .put(Shade.ACCENT_400, 0xFF76FF03)
          .put(Shade.ACCENT_700, 0xFF64DD17)
          .buildOrThrow()),

  LIME(
      "Lime",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFF9FBE7)
          .put(Shade.PRIMARY_100, 0xFFF0F4C3)
          .put(Shade.PRIMARY_200, 0xFFE6EE9C)
          .put(Shade.PRIMARY_300, 0xFFDCE775)
          .put(Shade.PRIMARY_400, 0xFFD4E157)
          .put(Shade.PRIMARY_500, 0xFFCDDC39)
          .put(Shade.PRIMARY_600, 0xFFC0CA33)
          .put(Shade.PRIMARY_700, 0xFFAFB42B)
          .put(Shade.PRIMARY_800, 0xFF9E9D24)
          .put(Shade.PRIMARY_900, 0xFF827717)
          .put(Shade.ACCENT_100, 0xFFF4FF81)
          .put(Shade.ACCENT_200, 0xFFEEFF41)
          .put(Shade.ACCENT_400, 0xFFC6FF00)
          .put(Shade.ACCENT_700, 0xFFAEEA00)
          .buildOrThrow()),

  YELLOW(
      "Yellow",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFFFFDE7)
          .put(Shade.PRIMARY_100, 0xFFFFF9C4)
          .put(Shade.PRIMARY_200, 0xFFFFF59D)
          .put(Shade.PRIMARY_300, 0xFFFFF176)
          .put(Shade.PRIMARY_400, 0xFFFFEE58)
          .put(Shade.PRIMARY_500, 0xFFFFEB3B)
          .put(Shade.PRIMARY_600, 0xFFFDD835)
          .put(Shade.PRIMARY_700, 0xFFFBC02D)
          .put(Shade.PRIMARY_800, 0xFFF9A825)
          .put(Shade.PRIMARY_900, 0xFFF57F17)
          .put(Shade.ACCENT_100, 0xFFFFFF8D)
          .put(Shade.ACCENT_200, 0xFFFFFF00)
          .put(Shade.ACCENT_400, 0xFFFFEA00)
          .put(Shade.ACCENT_700, 0xFFFFD600)
          .buildOrThrow()),

  AMBER(
      "Amber",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFFFF8E1)
          .put(Shade.PRIMARY_100, 0xFFFFECB3)
          .put(Shade.PRIMARY_200, 0xFFFFE082)
          .put(Shade.PRIMARY_300, 0xFFFFD54F)
          .put(Shade.PRIMARY_400, 0xFFFFCA28)
          .put(Shade.PRIMARY_500, 0xFFFFC107)
          .put(Shade.PRIMARY_600, 0xFFFFB300)
          .put(Shade.PRIMARY_700, 0xFFFFA000)
          .put(Shade.PRIMARY_800, 0xFFFF8F00)
          .put(Shade.PRIMARY_900, 0xFFFF6F00)
          .put(Shade.ACCENT_100, 0xFFFFE57F)
          .put(Shade.ACCENT_200, 0xFFFFD740)
          .put(Shade.ACCENT_400, 0xFFFFC400)
          .put(Shade.ACCENT_700, 0xFFFFAB00)
          .buildOrThrow()),

  ORANGE(
      "Orange",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFFFF3E0)
          .put(Shade.PRIMARY_100, 0xFFFFE0B2)
          .put(Shade.PRIMARY_200, 0xFFFFCC80)
          .put(Shade.PRIMARY_300, 0xFFFFB74D)
          .put(Shade.PRIMARY_400, 0xFFFFA726)
          .put(Shade.PRIMARY_500, 0xFFFF9800)
          .put(Shade.PRIMARY_600, 0xFFFB8C00)
          .put(Shade.PRIMARY_700, 0xFFF57C00)
          .put(Shade.PRIMARY_800, 0xFFEF6C00)
          .put(Shade.PRIMARY_900, 0xFFE65100)
          .put(Shade.ACCENT_100, 0xFFFFD180)
          .put(Shade.ACCENT_200, 0xFFFFAB40)
          .put(Shade.ACCENT_400, 0xFFFF9100)
          .put(Shade.ACCENT_700, 0xFFFF6D00)
          .buildOrThrow()),

  DEEP_ORANGE(
      "Deep Orange",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFFBE9E7)
          .put(Shade.PRIMARY_100, 0xFFFFCCBC)
          .put(Shade.PRIMARY_200, 0xFFFFAB91)
          .put(Shade.PRIMARY_300, 0xFFFF8A65)
          .put(Shade.PRIMARY_400, 0xFFFF7043)
          .put(Shade.PRIMARY_500, 0xFFFF5722)
          .put(Shade.PRIMARY_600, 0xFFF4511E)
          .put(Shade.PRIMARY_700, 0xFFE64A19)
          .put(Shade.PRIMARY_800, 0xFFD84315)
          .put(Shade.PRIMARY_900, 0xFFBF360C)
          .put(Shade.ACCENT_100, 0xFFFF9E80)
          .put(Shade.ACCENT_200, 0xFFFF6E40)
          .put(Shade.ACCENT_400, 0xFFFF3D00)
          .put(Shade.ACCENT_700, 0xFFDD2C00)
          .buildOrThrow()),

  BROWN(
      "Brown",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFEFEBE9)
          .put(Shade.PRIMARY_100, 0xFFD7CCC8)
          .put(Shade.PRIMARY_200, 0xFFBCAAA4)
          .put(Shade.PRIMARY_300, 0xFFA1887F)
          .put(Shade.PRIMARY_400, 0xFF8D6E63)
          .put(Shade.PRIMARY_500, 0xFF795548)
          .put(Shade.PRIMARY_600, 0xFF6D4C41)
          .put(Shade.PRIMARY_700, 0xFF5D4037)
          .put(Shade.PRIMARY_800, 0xFF4E342E)
          .put(Shade.PRIMARY_900, 0xFF3E2723)
          .buildOrThrow()),

  GREY(
      "Grey",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFFAFAFA)
          .put(Shade.PRIMARY_100, 0xFFF5F5F5)
          .put(Shade.PRIMARY_200, 0xFFEEEEEE)
          .put(Shade.PRIMARY_300, 0xFFE0E0E0)
          .put(Shade.PRIMARY_400, 0xFFBDBDBD)
          .put(Shade.PRIMARY_500, 0xFF9E9E9E)
          .put(Shade.PRIMARY_600, 0xFF757575)
          .put(Shade.PRIMARY_700, 0xFF616161)
          .put(Shade.PRIMARY_800, 0xFF424242)
          .put(Shade.PRIMARY_900, 0xFF212121)
          .buildOrThrow()),

  BLUE_GREY(
      "Blue Grey",
      ImmutableBiMap.<Shade, Integer>builder()
          .put(Shade.PRIMARY_050, 0xFFECEFF1)
          .put(Shade.PRIMARY_100, 0xFFCFD8DC)
          .put(Shade.PRIMARY_200, 0xFFB0BEC5)
          .put(Shade.PRIMARY_300, 0xFF90A4AE)
          .put(Shade.PRIMARY_400, 0xFF78909C)
          .put(Shade.PRIMARY_500, 0xFF607D8B)
          .put(Shade.PRIMARY_600, 0xFF546E7A)
          .put(Shade.PRIMARY_700, 0xFF455A64)
          .put(Shade.PRIMARY_800, 0xFF37474F)
          .put(Shade.PRIMARY_900, 0xFF263238)
          .buildOrThrow()),

  BLACK("Black", ImmutableBiMap.of(Shade.PRIMARY_500, 0xFF000000)),

  WHITE("White", ImmutableBiMap.of(Shade.PRIMARY_500, 0xFFFFFFFF));

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

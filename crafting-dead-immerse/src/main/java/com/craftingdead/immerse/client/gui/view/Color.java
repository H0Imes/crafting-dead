/*
 * Crafting Dead
 * Copyright (C) 2021  NexusNode LTD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.craftingdead.immerse.client.gui.view;

import javax.annotation.Nullable;
import org.jdesktop.core.animation.timing.Evaluator;
import org.jdesktop.core.animation.timing.evaluators.KnownEvaluators;
import com.craftingdead.immerse.client.util.RenderUtil;

public class Color {

  static {
    KnownEvaluators.getInstance().register(new Evaluator<Color>() {

      @Override
      public Color evaluate(Color v0, Color v1, double fraction) {
        return new Color(RenderUtil.lerp(v0.getValue4f(), v1.getValue4f(), (float) fraction));
      }

      @Override
      public Class<Color> getEvaluatorClass() {
        return Color.class;
      }
    });
  }

  public static final Color INVISIBLE = new Color(0, 0, 0, 0);

  public static final Color BLACK = new Color(0, 0, 0);
  public static final Color DARK_BLUE = new Color(0, 0, 170);
  public static final Color DARK_GREEN = new Color(0, 170, 0);
  public static final Color DARK_AQUA = new Color(0, 170, 170);
  public static final Color DARK_RED = new Color(170, 0, 0);
  public static final Color DARK_PURPLE = new Color(170, 0, 170);
  public static final Color GOLD = new Color(255, 170, 0);
  public static final Color GRAY = new Color(170, 170, 170);
  public static final Color DARK_GRAY = new Color(85, 85, 85);
  public static final Color BLUE = new Color(85, 85, 255);
  public static final Color GREEN = new Color(85, 255, 85);
  public static final Color AQUA = new Color(85, 255, 255);
  public static final Color RED = new Color(255, 85, 85);
  public static final Color LIGHT_PURPLE = new Color(255, 85, 255);
  public static final Color YELLOW = new Color(255, 255, 85);
  public static final Color WHITE = new Color(255, 255, 255);
  public static final Color BLUE_C = new Color(170, 220, 240);
  public static final Color GRAY_224 = new Color(224, 224, 224);

  private static final Color[] VANILLA_COLORS =
      new Color[] {BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY,
          DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE};

  private final float[] value4f;
  private final int[] value4i;
  private final int valueHex;

  public Color(int valueHex) {
    this.value4i = RenderUtil.getColour4i(valueHex);
    this.value4f = RenderUtil.getColour4f(this.value4i);
    this.valueHex = valueHex;
  }

  public Color(int red, int green, int blue) {
    this(red, green, blue, 255);
  }

  public Color(int red, int green, int blue, int alpha) {
    this(new int[] {red, green, blue, alpha});
  }

  public Color(int[] value4i) {
    this.value4i = value4i;
    this.value4f = RenderUtil.getColour4f(this.value4i);
    this.valueHex = RenderUtil.getColour(this.value4i);
  }

  public Color(float red, float green, float blue) {
    this(red, green, blue, 1.0F);
  }

  public Color(float red, float green, float blue, float alpha) {
    this(new float[] {red, green, blue, alpha});
  }

  public Color(float[] value4f) {
    this.value4f = value4f;
    this.value4i = RenderUtil.getColour4i(value4f);
    this.valueHex = RenderUtil.getColour(this.value4i);
  }

  public float[] getValue4f() {
    float[] result = new float[4];
    System.arraycopy(this.value4f, 0, result, 0, 4);
    return result;
  }

  public int[] getValue4i() {
    int[] result = new int[4];
    System.arraycopy(this.value4i, 0, result, 0, 4);
    return result;
  }

  public int getHex() {
    return this.valueHex;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Color && ((Color) obj).valueHex == this.valueHex;
  }

  @Nullable
  public static Color getFormattingColor(int code) {
    if (code >= 0 && code <= 15) {
      return VANILLA_COLORS[code];
    }
    return null;
  }
}
/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.eyesfree.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a section of a screenshot bitmap and its associated color and
 * contrast data.
 *
 * @author caseyburkhardt@google.com (Casey Burkhardt)
 */
public class ContrastSwatch implements Parcelable {

    private Bitmap mImage;

    private final @Nullable String mName;

    private int mBackgroundColor;

    private int mForegroundColor;

    private double mBackgroundLuminance;

    private double mForegroundLuminance;

    private final Rect mScreenBounds;

    private double mContrastRatio;

    /**
     * Constructs a ContrastSwatch, also extracting certain properties from the
     * bitmap related to contrast and luminance.
     * <p>
     * NOTE: Invoking this constructor performs image processing tasks, which
     * are relatively heavyweight. Callers should create these objects off the
     * UI thread.
     *
     * @param image {@link Bitmap} of the area of an image to be evaluated
     * @param screenBounds The bounds in screen coordinates of the image being
     *            evaluated
     * @param name Optional name identifying the image being evaluated
     */
    public ContrastSwatch(Bitmap image, Rect screenBounds, @Nullable String name) {
        mImage = image;
        mScreenBounds = screenBounds;
        mName = name;

        processSwatch(image);
    }

    private ContrastSwatch(Parcel source) {
        mImage = null;
        mName = source.readString();
        mBackgroundColor = source.readInt();
        mForegroundColor = source.readInt();
        mBackgroundLuminance = source.readDouble();
        mForegroundLuminance = source.readDouble();
        mScreenBounds = (Rect) source.readValue(Rect.class.getClassLoader());
        mContrastRatio = source.readDouble();
    }

    public void recycle() {
        if (mImage != null) {
            mImage.recycle();
        }
    }

    /**
     * Compute the background and foreground colors and luminance for the image, and the contrast
     * ratio.
     */
    private void processSwatch(Bitmap image) {
        final Map<Integer, Integer> colorHistogram = processLuminanceData(image);
        extractFgBgData(colorHistogram);

        // Two-decimal digits of precision for the contrast ratio
        mContrastRatio = Math.round(
                ContrastUtils.calculateContrastRatio(mBackgroundLuminance, mForegroundLuminance)
                * 100.0d) / 100.0d;
    }

   /**
    * @return a map where the keys are colors that are present in the image, and the values are the
    *     number of pixels with each color.
    */
    private static Map<Integer, Integer> processLuminanceData(Bitmap image) {
        final Map<Integer, Integer> colorHistogram = new HashMap<>();
        final int width = image.getWidth();
        final int height = image.getHeight();
        if ((width * height) == 0) {
            return colorHistogram;
        }
        int[] pixels = new int[width * height];
        image.getPixels(pixels, 0, width, 0, 0, width, height);

        Arrays.sort(pixels);

        // Loop invariants
        int currentColor = pixels[0];
        int currentColorCount = 1;
        for (int i = 1; i < pixels.length; i++) {
            int color = pixels[i];
            if (color == currentColor) {
                currentColorCount++;
            } else {
                colorHistogram.put(currentColor, currentColorCount);
                currentColor = color;
                currentColorCount = 1;
            }
        }
        // Catch the last unprocessed color
        colorHistogram.put(currentColor, currentColorCount);
        return colorHistogram;
    }

    /**
     * Set the fields mBackgroundColor, mForegroundColor, mBackgroundLuminance and
     * mForegroundLuminance based upon the color histogram.
     */
    private void extractFgBgData(Map<Integer, Integer> colorHistogram) {
        if (colorHistogram.isEmpty()) {
            // An empty histogram indicates we've encountered a 0px area image. It has no luminance.
            mBackgroundLuminance = mForegroundLuminance = 0;
            mBackgroundColor = Color.BLACK;
            mForegroundColor = Color.BLACK;
        } else if (colorHistogram.size() == 1) {
            // Deal with views that only contain a single color
            final int singleColor = colorHistogram.keySet().iterator().next();
            mBackgroundLuminance = mForegroundLuminance =
                    ContrastUtils.calculateLuminance(singleColor);
            mForegroundColor = singleColor;
            mBackgroundColor = singleColor;
        } else {
            // Find the average luminance value within the set of luminances for
            // purposes of splitting luminance values into high-luminance and
            // low-luminance buckets. This is explicitly not a weighted average.
            double luminanceSum = 0;
            for (int color : colorHistogram.keySet()) {
                luminanceSum += ContrastUtils.calculateLuminance(color);
            }

            final double averageLuminance = luminanceSum / colorHistogram.size();

            // Select the highest and lowest luminance values that contribute to
            // most number of pixels in the image -- our background and
            // foreground colors.
            double lowLuminanceContributor = 0.0d;
            double highLuminanceContributor = 1.0d;
            int lowLuminanceColor = -1;
            int highLuminanceColor = -1;
            int maxLowLuminanceFrequency = 0;
            int maxHighLuminanceFrequency = 0;
            for (Entry<Integer, Integer> entry : colorHistogram.entrySet()) {
                final int color = entry.getKey();
                final double luminanceValue = ContrastUtils.calculateLuminance(color);
                final int frequency = entry.getValue();
                if ((luminanceValue < averageLuminance) && (frequency > maxLowLuminanceFrequency)) {
                    lowLuminanceContributor = luminanceValue;
                    maxLowLuminanceFrequency = frequency;
                    lowLuminanceColor = color;
                } else if ((luminanceValue >= averageLuminance)
                        && (frequency > maxHighLuminanceFrequency)) {
                    highLuminanceContributor = luminanceValue;
                    maxHighLuminanceFrequency = frequency;
                    highLuminanceColor = color;
                }
            }

            // Background luminance is that which occurs more frequently
            if (maxHighLuminanceFrequency > maxLowLuminanceFrequency) {
                mBackgroundLuminance = highLuminanceContributor;
                mBackgroundColor = highLuminanceColor;
                mForegroundLuminance = lowLuminanceContributor;
                mForegroundColor = lowLuminanceColor;
            } else {
                mBackgroundLuminance = lowLuminanceContributor;
                mBackgroundColor = lowLuminanceColor;
                mForegroundLuminance = highLuminanceContributor;
                mForegroundColor = highLuminanceColor;
            }
        }
    }

    public Bitmap getImage() {
        return Bitmap.createBitmap(mImage);
    }

    public void setImage(Bitmap image) {
        mImage = image;
    }

    public @Nullable CharSequence getName() {
        return mName;
    }

    public int getBackgroundColor() {
      return mBackgroundColor;
    }

    public int getForegroundColor() {
        return mForegroundColor;
    }

    public double getBackgroundLuminance() {
        return mBackgroundLuminance;
    }

    public double getForegroundLuminance() {
        return mForegroundLuminance;
    }

    public Rect getBounds() {
        return new Rect(mScreenBounds);
    }

    public double getContrastRatio() {
        return mContrastRatio;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeInt(mBackgroundColor);
        dest.writeInt(mForegroundColor);
        dest.writeDouble(mBackgroundLuminance);
        dest.writeDouble(mForegroundLuminance);
        dest.writeValue(mScreenBounds);
        dest.writeDouble(mContrastRatio);
    }

    @Override
    public String toString() {
        return "{name:" + mName + ", contrast:1:" + mContrastRatio + ", background:"
                + ContrastUtils.colorToHexString(mBackgroundColor) + ", foreground:"
                + ContrastUtils.colorToHexString(mForegroundColor) + "}";
    }

    public static final Parcelable.Creator<ContrastSwatch>
            CREATOR = new Parcelable.Creator<ContrastSwatch>() {

        @Override
        public ContrastSwatch createFromParcel(Parcel source) {
            return new ContrastSwatch(source);
        }

        @Override
        public ContrastSwatch[] newArray(int size) {
            return new ContrastSwatch[size];
        }
    };
}

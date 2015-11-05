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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Represents a section of a screenshot bitmap and its associated color and
 * contrast data.
 *
 * @author caseyburkhardt@google.com (Casey Burkhardt)
 */
public class ContrastSwatch implements Parcelable {

    private Bitmap mImage;

    private final String mName;

    private final HashMap<Integer, Double> mLuminanceMap;

    private final HashMap<Double, Integer> mLuminanceHistogram;

    private final List<Integer> mBackgroundColors;

    private final List<Integer> mForegroundColors;

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
    public ContrastSwatch(Bitmap image, Rect screenBounds, String name) {
        mImage = image;
        mScreenBounds = screenBounds;
        mName = name;
        mBackgroundColors = new LinkedList<Integer>();
        mForegroundColors = new LinkedList<Integer>();
        mLuminanceMap = new HashMap<Integer, Double>();
        mLuminanceHistogram = new HashMap<Double, Integer>();

        processSwatch();
    }

    private ContrastSwatch(Parcel source) {
        mBackgroundColors = new LinkedList<Integer>();
        mForegroundColors = new LinkedList<Integer>();
        mLuminanceMap = new HashMap<Integer, Double>();
        mLuminanceHistogram = new HashMap<Double, Integer>();

        mName = source.readString();
        source.readMap(mLuminanceMap, null);
        source.readMap(mLuminanceHistogram, null);
        source.readList(mBackgroundColors, null);
        source.readList(mForegroundColors, null);
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

    private void processSwatch() {
        processLuminanceData();
        extractFgBgData();

        // Two-decimal digits of precision for the contrast ratio
        mContrastRatio = Math.round(
                ContrastUtils.calculateContrastRatio(mBackgroundLuminance, mForegroundLuminance)
                * 100.0d) / 100.0d;
    }

    private void processLuminanceData() {
        for (int x = 0; x < mImage.getWidth(); ++x) {
            for (int y = 0; y < mImage.getHeight(); ++y) {
                final int color = mImage.getPixel(x, y);
                final double luminance = ContrastUtils.calculateLuminance(color);
                if (!mLuminanceMap.containsKey(color)) {
                    mLuminanceMap.put(color, luminance);
                }

                if (!mLuminanceHistogram.containsKey(luminance)) {
                    mLuminanceHistogram.put(luminance, 0);
                }

                mLuminanceHistogram.put(luminance, mLuminanceHistogram.get(luminance) + 1);
            }
        }
    }

    private void extractFgBgData() {
        if (mLuminanceMap.isEmpty()) {
            // An empty luminance map indicates we've encountered a 0px area
            // image. It has no luminance.
            mBackgroundLuminance = mForegroundLuminance = 0;
            mBackgroundColors.add(Color.BLACK);
            mForegroundColors.add(Color.BLACK);
        } else if (mLuminanceMap.size() == 1) {
            // Deal with views that only contain a single color
            mBackgroundLuminance = mForegroundLuminance = mLuminanceHistogram.keySet().iterator()
                    .next();
            final int singleColor = mLuminanceMap.keySet().iterator().next();
            mForegroundColors.add(singleColor);
            mBackgroundColors.add(singleColor);
        } else {
            // Sort all luminance values seen from low to high
            final ArrayList<Entry<Integer, Double>> colorsByLuminance = new ArrayList<
                    Entry<Integer, Double>>(mLuminanceMap.size());
            colorsByLuminance.addAll(mLuminanceMap.entrySet());
            Collections.sort(colorsByLuminance, new Comparator<Entry<Integer, Double>>() {
                @Override
                public int compare(Entry<Integer, Double> lhs, Entry<Integer, Double> rhs) {
                    return Double.compare(lhs.getValue(), rhs.getValue());
                }
            });

            // Sort luminance values seen by frequency in the image
            final ArrayList<Entry<Double, Integer>> luminanceByFrequency = new ArrayList<
                    Entry<Double, Integer>>(mLuminanceHistogram.size());
            luminanceByFrequency.addAll(mLuminanceHistogram.entrySet());
            Collections.sort(luminanceByFrequency, new Comparator<Entry<Double, Integer>>() {
                @Override
                public int compare(Entry<Double, Integer> lhs, Entry<Double, Integer> rhs) {
                    return Integer.compare(lhs.getValue(), rhs.getValue());
                }
            });

            // Find the average luminance value within the set of luminances for
            // purposes of splitting luminance values into high-luminance and
            // low-luminance buckets. This is explicitly not a weighted average.
            double luminanceSum = 0;
            for (Entry<Double, Integer> luminanceCount : luminanceByFrequency) {
                luminanceSum += luminanceCount.getKey();
            }

            final double averageLuminance = luminanceSum / luminanceByFrequency.size();

            // Select the highest and lowest luminance values that contribute to
            // most number of pixels in the image -- our background and
            // foreground colors.
            double lowLuminanceContributor = 0.0d;
            for (int i = luminanceByFrequency.size() - 1; i >= 0; --i) {
                final double luminanceValue = luminanceByFrequency.get(i).getKey();
                if (luminanceValue < averageLuminance) {
                    lowLuminanceContributor = luminanceValue;
                    break;
                }
            }

            double highLuminanceContributor = 1.0d;
            for (int i = luminanceByFrequency.size() - 1; i >= 0; --i) {
                final double luminanceValue = luminanceByFrequency.get(i).getKey();
                if (luminanceValue >= averageLuminance) {
                    highLuminanceContributor = luminanceValue;
                    break;
                }
            }

            // Background luminance is that which occurs more frequently
            if (mLuminanceHistogram.get(highLuminanceContributor)
                    > mLuminanceHistogram.get(lowLuminanceContributor)) {
                mBackgroundLuminance = highLuminanceContributor;
                mForegroundLuminance = lowLuminanceContributor;
            } else {
                mBackgroundLuminance = lowLuminanceContributor;
                mForegroundLuminance = highLuminanceContributor;
            }

            // Determine the contributing colors for those luminance values
            // TODO(caseyburkhardt): I know, this is gross to iterate through
            // the whole image again...
            for (Entry<Integer, Double> colorLuminance : mLuminanceMap.entrySet()) {
                if (colorLuminance.getValue() == mBackgroundLuminance) {
                    mBackgroundColors.add(colorLuminance.getKey());
                }

                if (colorLuminance.getValue() == mForegroundLuminance) {
                    mForegroundColors.add(colorLuminance.getKey());
                }
            }
        }
    }

    public Bitmap getImage() {
        return Bitmap.createBitmap(mImage);
    }

    public void setImage(Bitmap image) {
        mImage = image;
    }

    public CharSequence getName() {
        return mName;
    }

    public List<Integer> getBackgroundColors() {
        return Collections.unmodifiableList(mBackgroundColors);
    }

    public List<Integer> getForegroundColors() {
        return Collections.unmodifiableList(mForegroundColors);
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
        dest.writeMap(mLuminanceMap);
        dest.writeMap(mLuminanceHistogram);
        dest.writeList(mBackgroundColors);
        dest.writeList(mForegroundColors);
        dest.writeDouble(mBackgroundLuminance);
        dest.writeDouble(mForegroundLuminance);
        dest.writeValue(mScreenBounds);
        dest.writeDouble(mContrastRatio);
    }

    @Override
    public String toString() {
        return "{name:" + mName + ", contrast:1:" + mContrastRatio + ", background:"
                + ContrastUtils.colorsToHexString(mBackgroundColors) + ", foreground:"
                + ContrastUtils.colorsToHexString(mForegroundColors) + "}";
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

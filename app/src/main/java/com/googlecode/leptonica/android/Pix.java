/*
 * Copyright (C) 2010 Google Inc.
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

package com.googlecode.leptonica.android;

import android.graphics.Rect;

/**
 * Java representation of a native Leptonica PIX object.
 *
 * @author alanv@google.com (Your Name Here)
 */
public class Pix {
    static {
        System.loadLibrary("lept");
    }

    /**
     * Index of the image width within the dimensions array.
     */
    public static final int INDEX_W = 0;

    /**
     * Index of the image height within the dimensions array.
     */
    public static final int INDEX_H = 1;

    /**
     * Index of the image bit-depth within the dimensions array.
     */
    public static final int INDEX_D = 2;

    /**
     * Package-accessible pointer to native pix
     */
    final int mNativePix;

    private boolean mRecycled;

    /**
     * Creates a new Pix wrapper for the specified native PIX object. Never call
     * this twice on the same native pointer, because finalize() will attempt to
     * free native memory twice.
     *
     * @param nativePix A pointer to the native PIX object.
     */
    public Pix(int nativePix) {
        mNativePix = nativePix;
        mRecycled = false;
    }

    public Pix(int width, int height, int depth) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Pix width and height must be > 0");
        } else if (depth != 1 && depth != 2 && depth != 4 && depth != 8 && depth != 16
                && depth != 24 && depth != 32) {
            throw new IllegalArgumentException("Depth must be one of 1, 2, 4, 8, 16, or 32");
        }

        mNativePix = nativeCreatePix(width, height, depth);
        mRecycled = false;
    }

    /**
     * Creates a new Pix from raw Pix data obtained from getData().
     *
     * @param pixData Raw pix data obtained from getData().
     * @param width   The width of the original Pix.
     * @param height  The height of the original Pix.
     * @param depth   The bit-depth of the original Pix.
     * @return a new Pix or <code>null</code> on error
     */
    public static Pix createFromPix(byte[] pixData, int width, int height, int depth) {
        int nativePix = nativeCreateFromData(pixData, width, height, depth);

        if (nativePix == 0) {
            throw new OutOfMemoryError();
        }

        return new Pix(nativePix);
    }

    private static native int nativeCreatePix(int w, int h, int d);

    private static native int nativeCreateFromData(byte[] data, int w, int h, int d);

    private static native boolean nativeGetData(int nativePix, byte[] data);

    private static native int nativeGetDataSize(int nativePix);

    private static native int nativeClone(int nativePix);

    private static native int nativeCopy(int nativePix);

    private static native boolean nativeInvert(int nativePix);

    private static native void nativeDestroy(int nativePix);

    private static native boolean nativeGetDimensions(int nativePix, int[] dimensions);

    private static native int nativeGetWidth(int nativePix);

    private static native int nativeGetHeight(int nativePix);

    private static native int nativeGetDepth(int nativePix);

    private static native int nativeGetPixel(int nativePix, int x, int y);

    private static native void nativeSetPixel(int nativePix, int x, int y, int color);

    /**
     * Returns a pointer to the native Pix object. This is used by native code
     * and is only valid within the same process in which the Pix was created.
     *
     * @return a native pointer to the Pix object
     */
    public int getNativePix() {
        return mNativePix;
    }

    // ***************
    // * NATIVE CODE *
    // ***************

    /**
     * Return the raw bytes of the native PIX object. You can reconstruct the
     * Pix from this data using createFromPix().
     *
     * @return a copy of this PIX object's raw data
     */
    public byte[] getData() {
        int size = nativeGetDataSize(mNativePix);

        byte[] buffer = new byte[size];

        if (!nativeGetData(mNativePix, buffer)) {
            throw new RuntimeException("native getData failed");
        }

        return buffer;
    }

    /**
     * Returns an array of this image's dimensions. See Pix.INDEX_* for indices.
     *
     * @return an array of this image's dimensions or <code>null</code> on
     * failure
     */
    public int[] getDimensions() {
        int[] dimensions = new int[4];

        if (getDimensions(dimensions)) {
            return dimensions;
        }

        return null;
    }

    /**
     * Fills an array with this image's dimensions. The array must be at least 3
     * elements long.
     *
     * @param dimensions An integer array with at least three elements.
     * @return <code>true</code> on success
     */
    public boolean getDimensions(int[] dimensions) {
        return nativeGetDimensions(mNativePix, dimensions);
    }

    /**
     * Returns a clone of this Pix. This does NOT create a separate copy, just a
     * new pointer that can be recycled without affecting other clones.
     *
     * @return a clone (shallow copy) of the Pix
     */
    @Override
    public Pix clone() {
        int nativePix = nativeClone(mNativePix);

        if (nativePix == 0) {
            throw new OutOfMemoryError();
        }

        return new Pix(nativePix);
    }

    /**
     * Returns a deep copy of this Pix that can be modified without affecting
     * the original Pix.
     *
     * @return a copy of the Pix
     */
    public Pix copy() {
        int nativePix = nativeCopy(mNativePix);

        if (nativePix == 0) {
            throw new OutOfMemoryError();
        }

        return new Pix(nativePix);
    }

    /**
     * Inverts this Pix in-place.
     *
     * @return <code>true</code> on success
     */
    public boolean invert() {
        return nativeInvert(mNativePix);
    }

    /**
     * Releases resources and frees any memory associated with this Pix. You may
     * not modify or access the pix after calling this method.
     */
    public void recycle() {
        if (!mRecycled) {
            nativeDestroy(mNativePix);

            mRecycled = true;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        recycle();

        super.finalize();
    }

    /**
     * Returns a Rect with the width and height of this Pix.
     *
     * @return a Rect with the width and height of this Pix
     */
    public Rect getRect() {
        int w = getWidth();
        int h = getHeight();

        return new Rect(0, 0, w, h);
    }

    /**
     * Returns the width of this Pix.
     *
     * @return the width of this Pix
     */
    public int getWidth() {
        return nativeGetWidth(mNativePix);
    }

    /**
     * Returns the height of this Pix.
     *
     * @return the height of this Pix
     */
    public int getHeight() {
        return nativeGetHeight(mNativePix);
    }

    /**
     * Returns the depth of this Pix.
     *
     * @return the depth of this Pix
     */
    public int getDepth() {
        return nativeGetDepth(mNativePix);
    }

    /**
     * Returns the {@link android.graphics.Color} at the specified location.
     *
     * @param x The x coordinate (0...width-1) of the pixel to return.
     * @param y The y coordinate (0...height-1) of the pixel to return.
     * @return The argb {@link android.graphics.Color} at the specified
     * coordinate.
     * @throws IllegalArgumentException If x, y exceeds the image bounds.
     */
    public int getPixel(int x, int y) {
        if (x < 0 || x >= getWidth()) {
            throw new IllegalArgumentException("Supplied x coordinate exceeds image bounds");
        } else if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Supplied x coordinate exceeds image bounds");
        }

        return nativeGetPixel(mNativePix, x, y);
    }

    /**
     * Sets the {@link android.graphics.Color} at the specified location.
     *
     * @param x     The x coordinate (0...width-1) of the pixel to set.
     * @param y     The y coordinate (0...height-1) of the pixel to set.
     * @param color The argb {@link android.graphics.Color} to set at the
     *              specified coordinate.
     * @throws IllegalArgumentException If x, y exceeds the image bounds.
     */
    public void setPixel(int x, int y, int color) {
        if (x < 0 || x >= getWidth()) {
            throw new IllegalArgumentException("Supplied x coordinate exceeds image bounds");
        } else if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Supplied x coordinate exceeds image bounds");
        }

        nativeSetPixel(mNativePix, x, y, color);
    }
}

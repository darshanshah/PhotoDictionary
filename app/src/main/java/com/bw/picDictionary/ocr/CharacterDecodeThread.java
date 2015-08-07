package com.bw.picDictionary.ocr;

import android.os.Handler;
import android.os.Looper;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.concurrent.CountDownLatch;

/**
 * This thread does all the heavy lifting of decoding the images.
 * <p/>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
final class CharacterDecodeThread extends Thread {

    private final ImageCaptureActivity activity;
    private final CountDownLatch handlerInitLatch;
    private final TessBaseAPI baseApi;
    private Handler handler;

    CharacterDecodeThread(ImageCaptureActivity activity, TessBaseAPI baseApi) {
        this.activity = activity;
        this.baseApi = baseApi;
        handlerInitLatch = new CountDownLatch(1);
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new CharacterDecodeHandler(activity, baseApi);
        handlerInitLatch.countDown();
        Looper.loop();
    }
}

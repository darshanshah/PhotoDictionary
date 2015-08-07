

package com.bw.picDictionary.ocr;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * send bitmap Data
 * <p/>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
final class CharacterDecodeHandler extends Handler {

    private static boolean isDecodePending;
    private final ImageCaptureActivity activity;
    private final TessBaseAPI baseApi;
    private boolean running = true;

    CharacterDecodeHandler(ImageCaptureActivity activity, TessBaseAPI baseApi) {
        this.activity = activity;
        this.baseApi = baseApi;
    }

    static void resetDecodeState() {
        isDecodePending = false;
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case R.id.ocr_continuous_decode:

            case R.id.ocr_decode:
                ocrDecode((byte[]) message.obj, message.arg2, message.arg1);
                break;
            case R.id.quit:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Perform an OCR decode for single-shot mode.
     *
     */
    private void ocrDecode(byte[] data, int width, int height) {
        Log.d("Hello", "width: " + width + ", height: " + height);

        ImageCaptureActivity.shutterSoundSelector.playSound(1);


        ProgressDialog indeterminateDialog = new ProgressDialog(activity);
        indeterminateDialog.setTitle("Please wait");
        String ocrEngineModeName = activity.getOcrEngineModeName();
        if (ocrEngineModeName.equals("Both")) {
            indeterminateDialog.setMessage("Performing OCR using Cube and Tesseract...");
        } else {
            indeterminateDialog.setMessage("Performing OCR using " + ocrEngineModeName + "...");
        }
        indeterminateDialog.setCancelable(false);
        indeterminateDialog.show();

        PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(data, width, height);
        new OcrRecognizer(activity, baseApi, indeterminateDialog, source.renderCroppedGreyscaleBitmap()).execute();

    }


}












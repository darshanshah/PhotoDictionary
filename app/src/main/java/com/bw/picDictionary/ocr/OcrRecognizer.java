
package com.bw.picDictionary.ocr;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.List;

/**
 * Send request of OCR to the ocr machine using threading (AsysncTask) gets the result status
 */
final class OcrRecognizer extends AsyncTask<String, String, Boolean> {

    private ImageCaptureActivity activity;
    private TessBaseAPI baseApi;
    private Bitmap bitmap;
    private OcrBean ocrBean;
    private OcrBeanFailure ocrBeanFailure;
    private boolean isContinuous;
    private ProgressDialog indeterminateDialog;
    private long start;
    private long end;

    OcrRecognizer(ImageCaptureActivity activity, TessBaseAPI baseApi,
                  ProgressDialog indeterminateDialog, Bitmap bitmap) {
        this.activity = activity;
        this.baseApi = baseApi;
        this.indeterminateDialog = indeterminateDialog;
        this.bitmap = bitmap;
        isContinuous = false;
    }

    OcrRecognizer(ImageCaptureActivity activity, TessBaseAPI baseApi, Bitmap bitmap) {
        this.activity = activity;
        this.baseApi = baseApi;
        this.bitmap = bitmap;
        isContinuous = true;
    }

    @Override
    protected Boolean doInBackground(String... arg0) {
        String textResult = null;
        int[] wordConfidences = null;
        int overallConf = -1;
        start = System.currentTimeMillis();
        end = start;

        try {
            Log.e("OcrRecognizeAsyncTask", "converted to bitmap. doing setImage()...");
            baseApi.setImage(bitmap);

            Log.e("OcrRecognizeAsyncTask", "setImage() completed");
            textResult = baseApi.getUTF8Text();
            Log.e("OcrRecognizeAsyncTask", "getUTF8Text() completed");
            wordConfidences = baseApi.wordConfidences();
            overallConf = baseApi.meanConfidence();
            end = System.currentTimeMillis();
        } catch (RuntimeException e) {
            Log.e("OcrRecognizeAsyncTask", "Caught RuntimeException in request to Tesseract. Setting state to CONTINUOUS_STOPPED.");
            e.printStackTrace();
            try {
                baseApi.clear();
                activity.stopHandler();
            } catch (NullPointerException e1) {
                // Continue
            }
            return false;
        }

        // Check for failure to recognize text
        if (textResult == null || textResult.equals("")) {
            ocrBeanFailure = new OcrBeanFailure(end - start);
            return false;
        }

        // Get bounding boxes for characters and words
        List<Rect> wordBoxes = baseApi.getWords().getBoxRects();
        List<Rect> characterBoxes = baseApi.getCharacters().getBoxRects();
        List<Rect> textlineBoxes = baseApi.getTextlines().getBoxRects();
        List<Rect> regionBoxes = baseApi.getRegions().getBoxRects();

        ocrBean = new OcrBean(bitmap, textResult, wordConfidences, overallConf, characterBoxes, textlineBoxes, wordBoxes, regionBoxes, (end - start));
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        Handler handler = activity.getHandler();
        if (!isContinuous && handler != null) {
            if (result) {
                Message message = Message.obtain(handler, R.id.ocr_decode_succeeded, ocrBean);
                message.sendToTarget();
            } else {
                bitmap.recycle();
                Message message = Message.obtain(handler, R.id.ocr_decode_failed, ocrBean);
                message.sendToTarget();
            }
            indeterminateDialog.dismiss();
        }
        if (baseApi != null) {
            baseApi.clear();
        }
    }
}

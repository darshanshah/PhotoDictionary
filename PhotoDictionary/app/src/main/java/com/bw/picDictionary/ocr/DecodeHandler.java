/*
 * Copyright (C) 2010 ZXing authors
 * Copyright 2011 Robert Theis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bw.picDictionary.ocr;

import com.googlecode.tesseract.android.TessBaseAPI;


import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Class to send bitmap data for OCR.
 * 
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
final class DecodeHandler extends Handler {

  private final CaptureActivity activity;
  private boolean running = true;
  private final TessBaseAPI baseApi;
  //private BeepManager beepManager;
  
  private static boolean isDecodePending;

  DecodeHandler(CaptureActivity activity, TessBaseAPI baseApi) {
    this.activity = activity;
    this.baseApi = baseApi;
    
    //beepManager = new BeepManager(activity);
    //beepManager.updatePrefs();
    
  }

  @Override
  public void handleMessage(Message message) {
    if (!running) {
      return;
    }
    switch (message.what) {        
      case R.id.ocr_continuous_decode:
        // Only request a decode if a request is not already pending.
       /* if (!isDecodePending) {
          isDecodePending = true;
          ocrContinuousDecode((byte[]) message.obj, message.arg1, message.arg2);
        }
        break;*/
      case R.id.ocr_decode:
        ocrDecode((byte[]) message.obj, message.arg2, message.arg1);
        break;
      case R.id.quit:
        running = false;
        Looper.myLooper().quit();
        break;
    }
  }
  
  static void resetDecodeState() {
    isDecodePending = false;
  }
  
  /**
   *  Perform an OCR decode for single-shot mode.
   *  
   * @param data Image data
   * @param width Image width
   * @param height Image height
   */
  private void ocrDecode(byte[] data, int width, int height) {
    Log.d("Hello", "width: " + width + ", height: " + height);
 
    //beepManager.playBeepSoundAndVibrate();
    
    CaptureActivity.beepManager.playSound(1);

    
    // Set up the indeterminate progress dialog box
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
    
    //int orientation = activity.getResources().getConfiguration().orientation;
    
    // Asyncrhonously launch the OCR process
    PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(data, width, height);
    new OcrRecognizeAsyncTask(activity, baseApi, indeterminateDialog, source.renderCroppedGreyscaleBitmap()).execute();

  }
  
  /**
   *  Perform an OCR decode for realtime recognition mode.
   *  
   * @param data Image data
   * @param width Image width
   * @param height Image height
   */
  /*private void ocrContinuousDecode(byte[] data, int width, int height) {
    // Asyncrhonously launch the OCR process
    PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(data, width, height);
    new OcrRecognizeAsyncTask(activity, baseApi, source.renderCroppedGreyscaleBitmap()).execute();
  }*/
}












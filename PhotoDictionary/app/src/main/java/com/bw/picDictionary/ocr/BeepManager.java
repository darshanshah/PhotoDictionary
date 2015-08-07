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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.preference.PreferenceManager;

import java.util.HashMap;


/**
 * Manages beeps and vibrations for {@link CaptureActivity}.
 * 
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
public final class BeepManager {

  private static final String TAG = BeepManager.class.getSimpleName();
  
  private  SoundPool mSoundPool;
  private  HashMap mSoundPoolMap;
  private  AudioManager  mAudioManager;
  private  Context mContext;
  private AudioManager audioService;
  SharedPreferences prefs;
  
  public void initSounds(Context theContext,Activity activity) {
      mContext = theContext;
      mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
      mSoundPoolMap = new HashMap();
      mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
      audioService = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
      prefs = PreferenceManager.getDefaultSharedPreferences(activity);
  }

  public void addSound(int index, int SoundID) {
      mSoundPoolMap.put(index, mSoundPool.load(mContext, SoundID, 1));
  }

  public void playSound(int index) {
	  boolean shouldPlayBeep = prefs.getBoolean(PreferencesActivity.KEY_PLAY_BEEP, CaptureActivity.DEFAULT_TOGGLE_BEEP);
	    if ((shouldPlayBeep) && !(audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)) {
	      // play beep
	    	float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	        streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	        mSoundPool.play(index, streamVolume, streamVolume, 1, 0, 1f);
	      }      
  }

  public void playLoopedSound(int index) {
      float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
      streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      mSoundPool.play(index, streamVolume, streamVolume, 1, -1, 1f);
  }
  
  
}

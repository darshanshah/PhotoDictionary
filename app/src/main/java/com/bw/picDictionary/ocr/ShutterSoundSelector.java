package com.bw.picDictionary.ocr;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.preference.PreferenceManager;

import java.util.HashMap;


/**
 * Shutter sound for camera.
 * <p/>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
public final class ShutterSoundSelector {
    SharedPreferences prefs;
    private SoundPool mSoundPool;
    private HashMap mSoundPoolMap;
    private AudioManager mAudioManager;
    private Context mContext;
    private AudioManager audioService;

    public void initSounds(Context theContext, Activity activity) {
        mContext = theContext;
        mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        mSoundPoolMap = new HashMap();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioService = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public void addSound(int index, int SoundID) {
        mSoundPoolMap.put(index, mSoundPool.load(mContext, SoundID, 1));
    }

    public void playSound(int index) {
        boolean shouldPlayBeep = prefs.getBoolean(PreferencesActivity.KEY_PLAY_BEEP, ImageCaptureActivity.DEFAULT_TOGGLE_BEEP);
        if ((shouldPlayBeep) && !(audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)) {
            // play beep
            float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mSoundPool.play(index, streamVolume, streamVolume, 1, 0, 1f);
        }
    }

}

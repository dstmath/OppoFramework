package com.color.inner.media;

import android.media.AudioManager;
import android.util.Log;

public class AudioManagerWrapper {
    public static final String STREAM_MUTE_CHANGED_ACTION = "android.media.STREAM_MUTE_CHANGED_ACTION";
    public static final int STREAM_SYSTEM_ENFORCED = 7;
    private static final String TAG = "AudioManagerWrapper";
    public static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";

    public static void setRingerModeInternal(AudioManager audioManager, int ringerMode) {
        try {
            audioManager.setRingerModeInternal(ringerMode);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static int getRingerModeInternal(AudioManager audioManager) {
        try {
            return audioManager.getRingerModeInternal();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return 2;
        }
    }
}

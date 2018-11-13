package com.oppo.media;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OppoAudioTrack extends AudioTrack {
    public static final int ERROR = -1;
    public static final int ERROR_BAD_VALUE = -2;
    public static final int ERROR_INVALID_OPERATION = -3;
    private static final float GAIN_MAX = 1.0f;
    private static final float GAIN_MIN = 0.0f;
    public static final int PLAYSTATE_PAUSED = 2;
    public static final int PLAYSTATE_PLAYING = 3;
    public static final int PLAYSTATE_STOPPED = 1;
    public static final int STATE_INITIALIZED = 1;
    public static final int STATE_NO_STATIC_DATA = 2;
    public static final int STATE_UNINITIALIZED = 0;
    public static final int SUCCESS = 0;
    private static final String TAG = "OppoAudioTrack";
    private int mOppoPlayState;
    private Object mOppoPlayStateLock;
    private int mOppoState;
    Method method_baseSetVolume = null;
    Method method_baseStart = null;
    Method method_clampGainOrLevel = null;
    Method method_native_setVolume = null;
    Method method_native_start = null;

    public OppoAudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, int mode, int sessionId) throws IllegalArgumentException {
        super(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode, sessionId);
        try {
            Class cls = Class.forName("android.media.AudioTrack");
            Class clas = Class.forName("android.media.PlayerBase");
            Field mState = cls.getDeclaredField("mState");
            mState.setAccessible(true);
            this.mOppoState = ((Integer) mState.get(this)).intValue();
            Field mPlayState = cls.getDeclaredField("mPlayState");
            mPlayState.setAccessible(true);
            this.mOppoPlayState = ((Integer) mPlayState.get(this)).intValue();
            Field mPlayStateLock = cls.getDeclaredField("mPlayStateLock");
            mPlayStateLock.setAccessible(true);
            this.mOppoPlayStateLock = mPlayStateLock.get(this);
            this.method_baseStart = clas.getDeclaredMethod("baseStart", new Class[0]);
            this.method_baseStart.setAccessible(true);
            this.method_baseSetVolume = clas.getDeclaredMethod("baseSetVolume", new Class[]{Float.TYPE, Float.TYPE});
            this.method_baseSetVolume.setAccessible(true);
            this.method_native_start = cls.getDeclaredMethod("native_start", new Class[0]);
            this.method_native_start.setAccessible(true);
            this.method_native_setVolume = cls.getDeclaredMethod("native_setVolume", new Class[]{Float.TYPE, Float.TYPE});
            this.method_native_setVolume.setAccessible(true);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
        }
    }

    public OppoAudioTrack(AudioAttributes attributes, AudioFormat format, int bufferSizeInBytes, int mode, int sessionId) throws IllegalArgumentException {
        super(attributes, format, bufferSizeInBytes, mode, sessionId);
        try {
            Class cls = Class.forName("android.media.AudioTrack");
            Class clas = Class.forName("android.media.PlayerBase");
            Field mState = cls.getDeclaredField("mState");
            mState.setAccessible(true);
            this.mOppoState = ((Integer) mState.get(this)).intValue();
            Field mPlayState = cls.getDeclaredField("mPlayState");
            mPlayState.setAccessible(true);
            this.mOppoPlayState = ((Integer) mPlayState.get(this)).intValue();
            Field mPlayStateLock = cls.getDeclaredField("mPlayStateLock");
            mPlayStateLock.setAccessible(true);
            this.mOppoPlayStateLock = mPlayStateLock.get(this);
            this.method_baseStart = clas.getDeclaredMethod("baseStart", new Class[0]);
            this.method_baseStart.setAccessible(true);
            this.method_baseSetVolume = clas.getDeclaredMethod("baseSetVolume", new Class[]{Float.TYPE, Float.TYPE});
            this.method_baseSetVolume.setAccessible(true);
            this.method_native_start = cls.getDeclaredMethod("native_start", new Class[0]);
            this.method_native_start.setAccessible(true);
            this.method_native_setVolume = cls.getDeclaredMethod("native_setVolume", new Class[]{Float.TYPE, Float.TYPE});
            this.method_native_setVolume.setAccessible(true);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
        }
    }

    public void play() throws IllegalStateException {
        if (super.getState() != 1) {
            throw new IllegalStateException("play() called on uninitialized AudioTrack.");
        }
        try {
            this.method_baseStart.invoke(this, new Object[0]);
            synchronized (this.mOppoPlayStateLock) {
                this.method_native_start.invoke(this, new Object[0]);
                this.mOppoPlayState = 3;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
    }

    private static float clampGainOrLevel(float gainOrLevel) {
        if (Float.isNaN(gainOrLevel)) {
            throw new IllegalArgumentException();
        } else if (gainOrLevel < GAIN_MIN) {
            return GAIN_MIN;
        } else {
            if (gainOrLevel > GAIN_MAX) {
                return GAIN_MAX;
            }
            return gainOrLevel;
        }
    }

    public int setStereoVolume(float leftGain, float rightGain) {
        try {
            if (this.mOppoState == 0) {
                return -3;
            }
            this.method_baseSetVolume.invoke(this, new Object[]{Float.valueOf(leftGain), Float.valueOf(rightGain)});
            return 0;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
    }
}

package com.oppo.media;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class OppoRecorder {
    public static final int HAMR_BITRATE = 12200;
    public static final int HAMR_BYTES_P_SEC = 1600;
    public static final int HWAV_BYTES_P_SEC = 88200;
    public static final int HWAV_SAMPLERATE = 44100;
    public static final int MEDIA_RECORDER_ERROR_UNKNOWN = 1;
    public static final int MEDIA_RECORDER_INFO_MAX_DURATION_REACHED = 800;
    public static final int MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED = 801;
    public static final int MEDIA_RECORDER_INFO_UNKNOWN = 1;
    public static final int MEDIA_RECORDER_TRACK_INFO_COMPLETION_STATUS = 1000;
    public static final int MEDIA_RECORDER_TRACK_INFO_DATA_KBYTES = 1009;
    public static final int MEDIA_RECORDER_TRACK_INFO_DURATION_MS = 1003;
    public static final int MEDIA_RECORDER_TRACK_INFO_ENCODED_FRAMES = 1005;
    public static final int MEDIA_RECORDER_TRACK_INFO_INITIAL_DELAY_MS = 1007;
    public static final int MEDIA_RECORDER_TRACK_INFO_LIST_END = 2000;
    public static final int MEDIA_RECORDER_TRACK_INFO_LIST_START = 1000;
    public static final int MEDIA_RECORDER_TRACK_INFO_MAX_CHUNK_DUR_MS = 1004;
    public static final int MEDIA_RECORDER_TRACK_INFO_PROGRESS_IN_TIME = 1001;
    public static final int MEDIA_RECORDER_TRACK_INFO_START_OFFSET_MS = 1008;
    public static final int MEDIA_RECORDER_TRACK_INFO_TYPE = 1002;
    public static final int MEDIA_RECORDER_TRACK_INTER_CHUNK_TIME_MS = 1006;
    public static final int NAMR_BITRATE = 5150;
    public static final int NAMR_BYTES_P_SEC = 700;
    public static final int NWAV_BYTES_P_SEC = 16000;
    public static final int NWAV_SAMPLERATE = 8000;
    private static final String TAG = "OppoRecorder_Java";
    private EventHandler mEventHandler;
    private FileDescriptor mFd;
    private long mNativeContext;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private String mPath;
    private Surface mSurface;

    public final class AudioEncoder {
        public static final int AAC = 3;
        public static final int AAC_ELD = 5;
        public static final int AMR_NB = 1;
        public static final int AMR_WB = 2;
        public static final int DEFAULT = 0;
        public static final int HE_AAC = 4;
        public static final int MPEG = 6;
        public static final int WAV = 8;

        private AudioEncoder() {
        }
    }

    public final class AudioSource {
        public static final int CAMCORDER = 5;
        public static final int DEFAULT = 0;
        public static final int MIC = 1;
        public static final int VOICE_CALL = 4;
        public static final int VOICE_COMMUNICATION = 7;
        public static final int VOICE_DOWNLINK = 3;
        public static final int VOICE_RECOGNITION = 6;
        public static final int VOICE_UPLINK = 2;

        private AudioSource() {
        }
    }

    private class EventHandler extends Handler {
        private static final int MEDIA_RECORDER_EVENT_ERROR = 1;
        private static final int MEDIA_RECORDER_EVENT_INFO = 2;
        private static final int MEDIA_RECORDER_EVENT_LIST_END = 99;
        private static final int MEDIA_RECORDER_EVENT_LIST_START = 1;
        private static final int MEDIA_RECORDER_TRACK_EVENT_ERROR = 100;
        private static final int MEDIA_RECORDER_TRACK_EVENT_INFO = 101;
        private static final int MEDIA_RECORDER_TRACK_EVENT_LIST_END = 1000;
        private static final int MEDIA_RECORDER_TRACK_EVENT_LIST_START = 100;
        private OppoRecorder mOPPORecorder;

        public EventHandler(OppoRecorder mr, Looper looper) {
            super(looper);
            this.mOPPORecorder = mr;
        }

        public void handleMessage(Message msg) {
            if (this.mOPPORecorder.mNativeContext == 0) {
                Log.w(OppoRecorder.TAG, "OppoRecorder went away with unhandled events");
                return;
            }
            switch (msg.what) {
                case 1:
                case 100:
                    if (OppoRecorder.this.mOnErrorListener != null) {
                        OppoRecorder.this.mOnErrorListener.onError(this.mOPPORecorder, msg.arg1, msg.arg2);
                    }
                    return;
                case 2:
                case 101:
                    if (OppoRecorder.this.mOnInfoListener != null) {
                        OppoRecorder.this.mOnInfoListener.onInfo(this.mOPPORecorder, msg.arg1, msg.arg2);
                    }
                    return;
                default:
                    Log.e(OppoRecorder.TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
    }

    public interface OnErrorListener {
        void onError(OppoRecorder oppoRecorder, int i, int i2);
    }

    public interface OnInfoListener {
        void onInfo(OppoRecorder oppoRecorder, int i, int i2);
    }

    public final class OutputFormat {
        public static final int AAC_ADIF = 5;
        public static final int AAC_ADTS = 6;
        public static final int AMR_NB = 3;
        public static final int AMR_WB = 4;
        public static final int DEFAULT = 0;
        public static final int MP3 = 9;
        public static final int MPEG_4 = 2;
        public static final int OUTPUT_FORMAT_MPEG2TS = 8;
        public static final int OUTPUT_FORMAT_RTP_AVP = 7;
        public static final int RAW_AMR = 3;
        public static final int THREE_GPP = 1;
        public static final int WAV = 11;

        private OutputFormat() {
        }
    }

    public final class VideoEncoder {
        public static final int DEFAULT = 0;
        public static final int H263 = 1;
        public static final int H264 = 2;
        public static final int MPEG_4_SP = 3;

        private VideoEncoder() {
        }
    }

    public final class VideoSource {
        public static final int CAMERA = 1;
        public static final int DEFAULT = 0;
        public static final int GRALLOC_BUFFER = 2;

        private VideoSource() {
        }
    }

    private native void _prepare() throws IllegalStateException, IOException;

    private native void _setOutputFile(FileDescriptor fileDescriptor, long j, long j2) throws IllegalStateException, IOException;

    private final native void native_finalize();

    private static final native void native_init();

    private native void native_reset();

    private final native void native_setup(Object obj) throws IllegalStateException;

    private native void setParameter(String str);

    public native void expandFile(String str, int i) throws IllegalStateException;

    public native int getMaxAmplitude() throws IllegalStateException;

    public native int getduration();

    public native void pause() throws IllegalStateException;

    public native void release();

    public native void resume() throws IllegalStateException;

    public native void setAudioEncoder(int i) throws IllegalStateException;

    public native void setAudioSource(int i) throws IllegalStateException;

    public native void setCamera(Camera camera);

    public native void setMaxDuration(int i) throws IllegalArgumentException;

    public native void setMaxFileSize(long j) throws IllegalArgumentException;

    public native void setOutputFormat(int i) throws IllegalStateException;

    public native void setVideoEncoder(int i) throws IllegalStateException;

    public native void setVideoFrameRate(int i) throws IllegalStateException;

    public native void setVideoSize(int i, int i2) throws IllegalStateException;

    public native void setVideoSource(int i) throws IllegalStateException;

    public native void start() throws IllegalStateException;

    public native void stop() throws IllegalStateException;

    static {
        Log.v(TAG, "loadLibrary");
        System.loadLibrary("opporecorder");
        native_init();
    }

    public OppoRecorder() {
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            looper = Looper.getMainLooper();
            if (looper != null) {
                this.mEventHandler = new EventHandler(this, looper);
            } else {
                this.mEventHandler = null;
            }
        }
        Log.i(TAG, "OppoRecorder()");
        native_setup(new WeakReference(this));
    }

    public void setPreviewDisplay(Surface sv) {
        this.mSurface = sv;
    }

    public static final int getAudioSourceMax() {
        return 7;
    }

    public void setProfile(CamcorderProfile profile) {
        setOutputFormat(profile.fileFormat);
        setVideoFrameRate(profile.videoFrameRate);
        setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        setVideoEncodingBitRate(profile.videoBitRate);
        setVideoEncoder(profile.videoCodec);
        if (profile.quality < 1000 || profile.quality > MEDIA_RECORDER_TRACK_INFO_INITIAL_DELAY_MS) {
            setAudioEncodingBitRate(profile.audioBitRate);
            setAudioChannels(profile.audioChannels);
            setAudioSamplingRate(profile.audioSampleRate);
            setAudioEncoder(profile.audioCodec);
        }
    }

    public void setCaptureRate(double fps) {
        setParameter(String.format("time-lapse-enable=1", new Object[0]));
        int timeBetweenFrameCaptureMs = (int) (1000.0d * (1.0d / fps));
        setParameter(String.format("time-between-time-lapse-frame-capture=%d", new Object[]{Integer.valueOf(timeBetweenFrameCaptureMs)}));
    }

    public void setOrientationHint(int degrees) {
        if (degrees == 0 || degrees == 90 || degrees == 180 || degrees == 270) {
            setParameter("video-param-rotation-angle-degrees=" + degrees);
            return;
        }
        throw new IllegalArgumentException("Unsupported angle: " + degrees);
    }

    public void setLocation(float latitude, float longitude) {
        int latitudex10000 = (int) (((double) (latitude * 10000.0f)) + 0.5d);
        int longitudex10000 = (int) (((double) (longitude * 10000.0f)) + 0.5d);
        if (latitudex10000 > 900000 || latitudex10000 < -900000) {
            throw new IllegalArgumentException("Latitude: " + latitude + " out of range.");
        } else if (longitudex10000 > 1800000 || longitudex10000 < -1800000) {
            throw new IllegalArgumentException("Longitude: " + longitude + " out of range");
        } else {
            setParameter("param-geotag-latitude=" + latitudex10000);
            setParameter("param-geotag-longitude=" + longitudex10000);
        }
    }

    public void setAudioSamplingRate(int samplingRate) {
        if (samplingRate <= 0) {
            throw new IllegalArgumentException("Audio sampling rate is not positive");
        }
        setParameter("audio-param-sampling-rate=" + samplingRate);
    }

    public void setAudioChannels(int numChannels) {
        if (numChannels <= 0) {
            throw new IllegalArgumentException("Number of channels is not positive");
        }
        setParameter("audio-param-number-of-channels=" + numChannels);
    }

    public void setAudioEncodingBitRate(int bitRate) {
        if (bitRate <= 0) {
            throw new IllegalArgumentException("Audio encoding bit rate is not positive");
        }
        setParameter("audio-param-encoding-bitrate=" + bitRate);
    }

    public void setVideoEncodingBitRate(int bitRate) {
        if (bitRate <= 0) {
            throw new IllegalArgumentException("Video encoding bit rate is not positive");
        }
        setParameter("video-param-encoding-bitrate=" + bitRate);
    }

    public void setAuxiliaryOutputFile(FileDescriptor fd) {
        Log.w(TAG, "setAuxiliaryOutputFile(FileDescriptor) is no longer supported.");
    }

    public void setAuxiliaryOutputFile(String path) {
        Log.w(TAG, "setAuxiliaryOutputFile(String) is no longer supported.");
    }

    public void setOutputFile(FileDescriptor fd) throws IllegalStateException {
        this.mPath = null;
        this.mFd = fd;
    }

    public void setOutputFile(String path) throws IllegalStateException {
        this.mFd = null;
        this.mPath = path;
    }

    public void prepare() throws IllegalStateException, IOException {
        if (this.mPath != null) {
            FileOutputStream fos = new FileOutputStream(this.mPath);
            try {
                _setOutputFile(fos.getFD(), 0, 0);
            } finally {
                fos.close();
            }
        } else if (this.mFd != null) {
            _setOutputFile(this.mFd, 0, 0);
        } else {
            throw new IOException("No valid output file");
        }
        _prepare();
    }

    public void reset() {
        native_reset();
        this.mEventHandler.removeCallbacksAndMessages(null);
    }

    public void setOnErrorListener(OnErrorListener l) {
        this.mOnErrorListener = l;
    }

    public void setOnInfoListener(OnInfoListener listener) {
        this.mOnInfoListener = listener;
    }

    private static void postEventFromNative(Object mediarecorder_ref, int what, int arg1, int arg2, Object obj) {
        OppoRecorder mr = (OppoRecorder) ((WeakReference) mediarecorder_ref).get();
        if (!(mr == null || mr.mEventHandler == null)) {
            mr.mEventHandler.sendMessage(mr.mEventHandler.obtainMessage(what, arg1, arg2, obj));
        }
    }

    protected void finalize() {
        native_finalize();
    }
}

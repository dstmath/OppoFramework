package com.oppo.media;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IAppOpsService.Stub;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Map.Entry;
import oppo.content.res.OppoExtraConfiguration;

public class OppoMediaPlayer {
    public static final boolean APPLY_METADATA_FILTER = true;
    public static final boolean BYPASS_METADATA_FILTER = false;
    private static final String IMEDIA_PLAYER = "android.media.IMediaPlayer";
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE = 2;
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE_FD = 3;
    private static final int INVOKE_ID_DESELECT_TRACK = 5;
    private static final int INVOKE_ID_GET_TRACK_INFO = 1;
    private static final int INVOKE_ID_SELECT_TRACK = 4;
    private static final int INVOKE_ID_SET_VIDEO_SCALE_MODE = 6;
    private static final int MEDIA_BUFFERING_UPDATE = 3;
    private static final int MEDIA_ERROR = 100;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    private static final int MEDIA_INFO = 200;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    public static final String MEDIA_MIMETYPE_TEXT_SUBRIP = "application/x-subrip";
    private static final int MEDIA_NOP = 0;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_SET_VIDEO_SIZE = 5;
    private static final int MEDIA_TIMED_TEXT = 99;
    public static final boolean METADATA_ALL = false;
    public static final boolean METADATA_UPDATE_ONLY = true;
    private static final String TAG = "OppoMediaPlayer";
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
    private final IAppOpsService mAppOps;
    private int mCurrentPosition;
    private EventHandler mEventHandler;
    private int mListenerContext;
    private long mNativeContext;
    private int mNativeSurfaceTexture;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnPreparedListener mOnPreparedListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnTimedTextListener mOnTimedTextListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private OppoAudioTrack mOppoAudioTrack;
    private boolean mScreenOnWhilePlaying;
    private int mSeekPosition;
    private boolean mStayAwake;
    private int mStreamType = Integer.MIN_VALUE;
    private SurfaceHolder mSurfaceHolder;
    private int mUsage = -1;
    private WakeLock mWakeLock = null;

    private class EventHandler extends Handler {
        private OppoMediaPlayer mMediaPlayer;

        public EventHandler(OppoMediaPlayer mp, Looper looper) {
            super(looper);
            this.mMediaPlayer = mp;
        }

        public void handleMessage(Message msg) {
            if (this.mMediaPlayer.mNativeContext == 0) {
                Log.w(OppoMediaPlayer.TAG, "mediaplayer went away with unhandled events");
                return;
            }
            Log.e(OppoMediaPlayer.TAG, "handleMessage (" + msg.arg1 + "," + msg.arg2 + ")");
            switch (msg.what) {
                case 0:
                    return;
                case 1:
                    if (OppoMediaPlayer.this.mOnPreparedListener != null) {
                        OppoMediaPlayer.this.mOnPreparedListener.onPrepared(this.mMediaPlayer);
                    }
                    return;
                case 2:
                    if (OppoMediaPlayer.this.mOnCompletionListener != null) {
                        OppoMediaPlayer.this.mOnCompletionListener.onCompletion(this.mMediaPlayer);
                    }
                    OppoMediaPlayer.this.stayAwake(false);
                    return;
                case 3:
                    if (OppoMediaPlayer.this.mOnBufferingUpdateListener != null) {
                        OppoMediaPlayer.this.mOnBufferingUpdateListener.onBufferingUpdate(this.mMediaPlayer, msg.arg1);
                    }
                    return;
                case 4:
                    if (OppoMediaPlayer.this.mSeekPosition != OppoMediaPlayer.this.mCurrentPosition) {
                        Log.v(OppoMediaPlayer.TAG, "Executing queued seekTo " + OppoMediaPlayer.this.mSeekPosition);
                        OppoMediaPlayer.this.mSeekPosition = -1;
                        OppoMediaPlayer.this.seekTo(OppoMediaPlayer.this.mCurrentPosition);
                    } else {
                        Log.v(OppoMediaPlayer.TAG, "All seeks complete - return to regularly scheduled program");
                        OppoMediaPlayer.this.mCurrentPosition = OppoMediaPlayer.this.mSeekPosition = -1;
                    }
                    if (OppoMediaPlayer.this.mOnSeekCompleteListener != null) {
                        OppoMediaPlayer.this.mOnSeekCompleteListener.onSeekComplete(this.mMediaPlayer);
                    }
                    return;
                case 5:
                    if (OppoMediaPlayer.this.mOnVideoSizeChangedListener != null) {
                        OppoMediaPlayer.this.mOnVideoSizeChangedListener.onVideoSizeChanged(this.mMediaPlayer, msg.arg1, msg.arg2);
                    }
                    return;
                case 100:
                    Log.e(OppoMediaPlayer.TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
                    int error_was_handled = 0;
                    if (OppoMediaPlayer.this.mOnErrorListener != null) {
                        error_was_handled = OppoMediaPlayer.this.mOnErrorListener.onError(this.mMediaPlayer, msg.arg1, msg.arg2);
                    }
                    if (!(OppoMediaPlayer.this.mOnCompletionListener == null || (error_was_handled ^ 1) == 0)) {
                        OppoMediaPlayer.this.mOnCompletionListener.onCompletion(this.mMediaPlayer);
                    }
                    OppoMediaPlayer.this.stayAwake(false);
                    return;
                case 200:
                    if (msg.arg1 != 700) {
                        Log.i(OppoMediaPlayer.TAG, "Info (" + msg.arg1 + "," + msg.arg2 + ")");
                    }
                    if (OppoMediaPlayer.this.mOnInfoListener != null) {
                        OppoMediaPlayer.this.mOnInfoListener.onInfo(this.mMediaPlayer, msg.arg1, msg.arg2);
                    }
                    return;
                default:
                    Log.e(OppoMediaPlayer.TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
    }

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(OppoMediaPlayer oppoMediaPlayer, int i);
    }

    public interface OnCompletionListener {
        void onCompletion(OppoMediaPlayer oppoMediaPlayer);
    }

    public interface OnErrorListener {
        boolean onError(OppoMediaPlayer oppoMediaPlayer, int i, int i2);
    }

    public interface OnInfoListener {
        boolean onInfo(OppoMediaPlayer oppoMediaPlayer, int i, int i2);
    }

    public interface OnPreparedListener {
        void onPrepared(OppoMediaPlayer oppoMediaPlayer);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(OppoMediaPlayer oppoMediaPlayer);
    }

    public interface OnTimedTextListener {
        void onTimedText(OppoMediaPlayer oppoMediaPlayer);
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(OppoMediaPlayer oppoMediaPlayer, int i, int i2);
    }

    public static class TrackInfo implements Parcelable {
        static final Creator<TrackInfo> CREATOR = new Creator<TrackInfo>() {
            public TrackInfo createFromParcel(Parcel in) {
                return new TrackInfo(in);
            }

            public TrackInfo[] newArray(int size) {
                return new TrackInfo[size];
            }
        };
        public static final int MEDIA_TRACK_TYPE_AUDIO = 2;
        public static final int MEDIA_TRACK_TYPE_TIMEDTEXT = 3;
        public static final int MEDIA_TRACK_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TRACK_TYPE_VIDEO = 1;
        final String mLanguage;
        final int mTrackType;

        public int getTrackType() {
            return this.mTrackType;
        }

        public String getLanguage() {
            return this.mLanguage;
        }

        TrackInfo(Parcel in) {
            this.mTrackType = in.readInt();
            this.mLanguage = in.readString();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mTrackType);
            dest.writeString(this.mLanguage);
        }
    }

    private native int _getAudioStreamType() throws IllegalStateException;

    private native void _pause() throws IllegalStateException;

    private native void _release();

    private native void _reset();

    private native void _setDataSource(String str, String[] strArr, String[] strArr2) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    private native void _setVideoSurface(Surface surface);

    private native void _start() throws IllegalStateException;

    private native void _stop() throws IllegalStateException;

    private final native void native_finalize();

    private static final native void native_init();

    private final native int native_invoke(Parcel parcel, Parcel parcel2);

    private final native void native_setup(Object obj);

    public native int _getCurrentPosition();

    public native void _seekTo(int i) throws IllegalStateException;

    public native void attachAuxEffect(int i);

    public native int getAudioSessionId();

    public native int getDuration();

    public native int getVideoHeight();

    public native int getVideoWidth();

    public native boolean isLooping();

    public native boolean isPlaying();

    public native void prepare() throws IOException, IllegalStateException;

    public native void prepareAsync() throws IllegalStateException;

    public native void setAudioSessionId(int i) throws IllegalArgumentException, IllegalStateException;

    public native void setAudioStreamType(int i);

    public native void setAuxEffectSendLevel(float f);

    public native void setDataSource(FileDescriptor fileDescriptor, long j, long j2) throws IOException, IllegalArgumentException, IllegalStateException;

    public native void setLooping(boolean z);

    public native void setVolume(float f, float f2);

    static {
        Log.v(TAG, "system load");
        System.loadLibrary("oppostagefright");
        System.loadLibrary("oppoplayer_jni");
        native_init();
    }

    public OppoMediaPlayer() {
        Log.v(TAG, "new OppoMediaPlayer");
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
        this.mAppOps = Stub.asInterface(ServiceManager.getService("appops"));
        native_setup(new WeakReference(this));
        this.mSeekPosition = -1;
        this.mCurrentPosition = -1;
    }

    public Parcel newRequest() {
        Parcel parcel = Parcel.obtain();
        parcel.writeInterfaceToken(IMEDIA_PLAYER);
        return parcel;
    }

    public void invoke(Parcel request, Parcel reply) {
        int retcode = native_invoke(request, reply);
        reply.setDataPosition(0);
        if (retcode != 0) {
            throw new RuntimeException("failure code: " + retcode);
        }
    }

    public void setDisplay(SurfaceHolder sh) {
        Surface surface;
        this.mSurfaceHolder = sh;
        if (sh != null) {
            surface = sh.getSurface();
        } else {
            surface = null;
        }
        _setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    public void setSurface(Surface surface) {
        if (this.mScreenOnWhilePlaying && surface != null) {
            Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective for Surface");
        }
        this.mSurfaceHolder = null;
        _setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    public void setVideoScalingMode(int mode) {
        if (isVideoScalingModeSupported(mode)) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                request.writeInt(6);
                request.writeInt(mode);
                invoke(request, reply);
            } finally {
                request.recycle();
                reply.recycle();
            }
        } else {
            throw new IllegalArgumentException("Scaling mode " + mode + " is not supported");
        }
    }

    public static OppoMediaPlayer create(Context context, Uri uri) {
        return create(context, uri, null);
    }

    public static OppoMediaPlayer create(Context context, Uri uri, SurfaceHolder holder) {
        try {
            OppoMediaPlayer mp = new OppoMediaPlayer();
            mp.setDataSource(context, uri);
            if (holder != null) {
                mp.setDisplay(holder);
            }
            mp.prepare();
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
            return null;
        } catch (IllegalArgumentException ex2) {
            Log.d(TAG, "create failed:", ex2);
            return null;
        } catch (SecurityException ex3) {
            Log.d(TAG, "create failed:", ex3);
            return null;
        }
    }

    public static OppoMediaPlayer create(Context context, int resid) {
        try {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resid);
            if (afd == null) {
                return null;
            }
            OppoMediaPlayer mp = new OppoMediaPlayer();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.prepare();
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
            return null;
        } catch (IllegalArgumentException ex2) {
            Log.d(TAG, "create failed:", ex2);
            return null;
        } catch (SecurityException ex3) {
            Log.d(TAG, "create failed:", ex3);
            return null;
        }
    }

    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, null);
    }

    /* JADX WARNING: Missing block: B:25:0x0059, code:
            android.util.Log.d(TAG, "Couldn't open file on client side, trying server side");
            setDataSource(r13.toString(), (java.util.Map) r14);
     */
    /* JADX WARNING: Missing block: B:26:0x0069, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("file")) {
            setDataSource(uri.getPath());
            return;
        }
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (assetFileDescriptor == null) {
                if (assetFileDescriptor != null) {
                    assetFileDescriptor.close();
                }
                return;
            }
            if (assetFileDescriptor.getDeclaredLength() < 0) {
                setDataSource(assetFileDescriptor.getFileDescriptor());
            } else {
                setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getDeclaredLength());
            }
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (SecurityException e) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (IOException e2) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (Throwable th) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        }
    }

    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(path, null, null);
    }

    public void setDataSource(String path, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        String[] keys = null;
        String[] values = null;
        if (headers != null) {
            keys = new String[headers.size()];
            values = new String[headers.size()];
            int i = 0;
            for (Entry<String, String> entry : headers.entrySet()) {
                keys[i] = (String) entry.getKey();
                values[i] = (String) entry.getValue();
                i++;
            }
        }
        setDataSource(path, keys, values);
    }

    private void setDataSource(String path, String[] keys, String[] values) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        File file = new File(path);
        if (file.exists()) {
            FileInputStream is = new FileInputStream(file);
            setDataSource(is.getFD());
            is.close();
            return;
        }
        _setDataSource(path, keys, values);
    }

    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        setDataSource(fd, 0, 576460752303423487L);
    }

    public void start() throws IllegalStateException {
        stayAwake(true);
        _start();
    }

    private int getAudioStreamType() {
        if (this.mStreamType == Integer.MIN_VALUE) {
            this.mStreamType = _getAudioStreamType();
        }
        return this.mStreamType;
    }

    public void stop() throws IllegalStateException {
        stayAwake(false);
        _stop();
    }

    public void pause() throws IllegalStateException {
        stayAwake(false);
        _pause();
    }

    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (this.mWakeLock != null) {
            if (this.mWakeLock.isHeld()) {
                washeld = true;
                this.mWakeLock.release();
            }
            this.mWakeLock = null;
        }
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(OppoExtraConfiguration.CONFIG_FLIPFONT | mode, OppoMediaPlayer.class.getName());
        this.mWakeLock.setReferenceCounted(false);
        if (washeld) {
            this.mWakeLock.acquire();
        }
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (this.mScreenOnWhilePlaying != screenOn) {
            if (screenOn && this.mSurfaceHolder == null) {
                Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
            }
            this.mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
    }

    private void stayAwake(boolean awake) {
        if (this.mWakeLock != null) {
            if (awake && (this.mWakeLock.isHeld() ^ 1) != 0) {
                this.mWakeLock.acquire();
            } else if (!awake && this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
        this.mStayAwake = awake;
        updateSurfaceScreenOn();
    }

    private void updateSurfaceScreenOn() {
        if (this.mSurfaceHolder != null) {
            this.mSurfaceHolder.setKeepScreenOn(this.mScreenOnWhilePlaying ? this.mStayAwake : false);
        }
    }

    public void seekTo(int msec) throws IllegalStateException {
        this.mCurrentPosition = msec;
        if (this.mSeekPosition < 0) {
            this.mSeekPosition = msec;
            _seekTo(msec);
            return;
        }
        Log.w(TAG, "Seek in progress - queue up seekTo " + msec);
    }

    public int getCurrentPosition() {
        if (this.mCurrentPosition >= 0) {
            return this.mCurrentPosition;
        }
        return _getCurrentPosition();
    }

    public void release() {
        stayAwake(false);
        updateSurfaceScreenOn();
        this.mOnPreparedListener = null;
        this.mOnBufferingUpdateListener = null;
        this.mOnCompletionListener = null;
        this.mOnSeekCompleteListener = null;
        this.mOnErrorListener = null;
        this.mOnInfoListener = null;
        this.mOnVideoSizeChangedListener = null;
        this.mOnTimedTextListener = null;
        _release();
    }

    public void reset() {
        stayAwake(false);
        _reset();
        this.mEventHandler.removeCallbacksAndMessages(null);
    }

    public TrackInfo[] getTrackInfo() throws IllegalStateException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInt(1);
            invoke(request, reply);
            TrackInfo[] trackInfo = (TrackInfo[]) reply.createTypedArray(TrackInfo.CREATOR);
            return trackInfo;
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    private static boolean availableMimeTypeForExternalSource(String mimeType) {
        if (mimeType == MEDIA_MIMETYPE_TEXT_SUBRIP) {
            return true;
        }
        return false;
    }

    public void addTimedTextSource(String path, String mimeType) throws IOException, IllegalArgumentException, IllegalStateException {
        if (availableMimeTypeForExternalSource(mimeType)) {
            File file = new File(path);
            if (file.exists()) {
                FileInputStream is = new FileInputStream(file);
                addTimedTextSource(is.getFD(), mimeType);
                is.close();
                return;
            }
            throw new IOException(path);
        }
        throw new IllegalArgumentException("Illegal mimeType for timed text source: " + mimeType);
    }

    /* JADX WARNING: Missing block: B:21:0x003e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addTimedTextSource(Context context, Uri uri, String mimeType) throws IOException, IllegalArgumentException, IllegalStateException {
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("file")) {
            addTimedTextSource(uri.getPath(), mimeType);
            return;
        }
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (assetFileDescriptor == null) {
                if (assetFileDescriptor != null) {
                    assetFileDescriptor.close();
                }
                return;
            }
            addTimedTextSource(assetFileDescriptor.getFileDescriptor(), mimeType);
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (SecurityException e) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (IOException e2) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (Throwable th) {
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        }
    }

    public void addTimedTextSource(FileDescriptor fd, String mimeType) throws IllegalArgumentException, IllegalStateException {
        addTimedTextSource(fd, 0, 576460752303423487L, mimeType);
    }

    public void addTimedTextSource(FileDescriptor fd, long offset, long length, String mimeType) throws IllegalArgumentException, IllegalStateException {
        if (availableMimeTypeForExternalSource(mimeType)) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                request.writeInt(3);
                request.writeFileDescriptor(fd);
                request.writeLong(offset);
                request.writeLong(length);
                request.writeString(mimeType);
                invoke(request, reply);
            } finally {
                request.recycle();
                reply.recycle();
            }
        } else {
            throw new IllegalArgumentException("Illegal mimeType for timed text source: " + mimeType);
        }
    }

    public void selectTrack(int index) throws IllegalStateException {
        selectOrDeselectTrack(index, true);
    }

    public void deselectTrack(int index) throws IllegalStateException {
        selectOrDeselectTrack(index, false);
    }

    private void selectOrDeselectTrack(int index, boolean select) throws IllegalStateException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInt(select ? 4 : 5);
            request.writeInt(index);
            invoke(request, reply);
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    protected void finalize() {
        native_finalize();
    }

    private static void postEventFromNative(Object mediaplayer_ref, int what, int arg1, int arg2, Object obj) {
        OppoMediaPlayer mp = (OppoMediaPlayer) ((WeakReference) mediaplayer_ref).get();
        if (mp != null) {
            if (what == 200 && arg1 == 2) {
                mp.start();
            }
            if (mp.mEventHandler != null) {
                mp.mEventHandler.sendMessage(mp.mEventHandler.obtainMessage(what, arg1, arg2, obj));
            }
        }
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        this.mOnPreparedListener = listener;
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        this.mOnCompletionListener = listener;
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        this.mOnBufferingUpdateListener = listener;
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        this.mOnSeekCompleteListener = listener;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        this.mOnVideoSizeChangedListener = listener;
    }

    public void setOnTimedTextListener(OnTimedTextListener listener) {
        this.mOnTimedTextListener = listener;
    }

    public void setOnErrorListener(OnErrorListener listener) {
        this.mOnErrorListener = listener;
    }

    public void setOnInfoListener(OnInfoListener listener) {
        this.mOnInfoListener = listener;
    }

    private boolean isVideoScalingModeSupported(int mode) {
        return mode == 1 || mode == 2;
    }
}

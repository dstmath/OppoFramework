package com.oppo.media;

import android.app.ActivityThread;
import android.app.ColorNotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.system.Os;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.app.IAppOpsService;
import com.oppo.atlas.OppoAtlasManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class OppoMediaPlayer {
    public static final boolean APPLY_METADATA_FILTER = true;
    public static final boolean BYPASS_METADATA_FILTER = false;
    public static final String CTS_TEST_PACKAGE = ".cts";
    public static final int ERROR_CANNOT_CONNECT_TO_SERVER = 261;
    public static final String GOOGLE_MUSIC_PACKAGE = "com.google.android.music";
    private static final String IMEDIA_PLAYER = "android.media.IMediaPlayer";
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE = 2;
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE_FD = 3;
    private static final int INVOKE_ID_DESELECT_TRACK = 5;
    private static final int INVOKE_ID_GET_TRACK_INFO = 1;
    private static final int INVOKE_ID_SELECT_TRACK = 4;
    private static final int INVOKE_ID_SET_VIDEO_SCALE_MODE = 6;
    public static final int KEY_PARAMETER_INTERCEPT = 10011;
    private static final int MEDIA_BUFFERING_UPDATE = 3;
    private static final int MEDIA_ERROR = 100;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    private static final int MEDIA_INFO = 200;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_HAS_UNSUPPORT_VIDEO = 860;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    public static final String MEDIA_MIMETYPE_TEXT_SUBRIP = "application/x-subrip";
    private static final int MEDIA_NOP = 0;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    public static final int MEDIA_PLAYER_IDLE = 1;
    public static final int MEDIA_PLAYER_INITIALIZED = 2;
    public static final int MEDIA_PLAYER_PAUSED = 32;
    public static final int MEDIA_PLAYER_PLAYBACK_COMPLETE = 128;
    public static final int MEDIA_PLAYER_PREPARED = 8;
    public static final int MEDIA_PLAYER_PREPARING = 4;
    public static final int MEDIA_PLAYER_STARTED = 16;
    public static final int MEDIA_PLAYER_STOPPED = 64;
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_SET_VIDEO_SIZE = 5;
    private static final int MEDIA_TIMED_TEXT = 99;
    public static final boolean METADATA_ALL = false;
    public static final boolean METADATA_UPDATE_ONLY = true;
    public static final int MUTE_FLAG = 1;
    private static final String SYSTEM_NOTIFICATION_AUDIO_PATH = "/system/media/audio/notifications/";
    private static final String TAG = "OppoMediaPlayer";
    public static final int UNMUTE_FLAG = 0;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
    private static final Object mPlayerLock = new Object();
    private static HashMap<String, Integer> mPlayerMap = new HashMap<>();
    public boolean isCreateOppoMediaPlayer = false;
    public boolean isOppoCreate = false;
    private final IAppOpsService mAppOps;
    public int mAudioSessionId = -1;
    public int mAudioStreamType = -1;
    public Context mContext = null;
    private int mCurrentPosition;
    public int mCurrentState;
    public boolean mDebugLog = true;
    public boolean mDefaultPlayerStarted = false;
    public boolean mDisableOppoMedia = false;
    private EventHandler mEventHandler;
    public FileDescriptor mFd = null;
    public Map<String, String> mHeaders = null;
    private boolean mInterceptFlag = false;
    public long mLength = -1;
    private int mListenerContext;
    private long mNativeContext;
    private int mNativeSurfaceTexture;
    private boolean mNeedMute = false;
    public boolean mNeedSeeking = false;
    public boolean mNotUsingOppoMedia = false;
    private final ColorNotificationManager mNotificationManager;
    public long mOffset = -1;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnPreparedListener mOnPreparedListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnTimedTextListener mOnTimedTextListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private OppoAudioTrack mOppoAudioTrack;
    public OppoMediaPlayer mOppoMediaPlayer = null;
    public String mPath = null;
    public boolean mPrepareAsync = true;
    private boolean mRecoverFlag = false;
    public boolean mScreenOn = false;
    private boolean mScreenOnWhilePlaying;
    public int mSeekMs = -1;
    private int mSeekPosition;
    private boolean mStayAwake;
    private int mStreamType = Integer.MIN_VALUE;
    public Surface mSurface = null;
    private SurfaceHolder mSurfaceHolder;
    public Uri mUri = null;
    private int mUsage = -1;
    private PowerManager.WakeLock mWakeLock = null;

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

    private void addMediaPlayerInstance(String packageName) {
        if (packageName != null && packageName.length() > 0) {
            boolean needRecycle = false;
            synchronized (mPlayerLock) {
                Integer count = mPlayerMap.get(packageName);
                if (count != null) {
                    Integer count2 = Integer.valueOf(count.intValue() + 1);
                    mPlayerMap.put(packageName, count2);
                    if (count2.intValue() > 40) {
                        mPlayerMap.remove(packageName);
                        needRecycle = true;
                    }
                } else {
                    mPlayerMap.put(packageName, 1);
                }
            }
            if (needRecycle) {
                recycleMediaPlayerInstance(packageName);
            }
        }
    }

    private void recycleMediaPlayerInstance(String packageName) {
        try {
            if (Process.myPid() > 0) {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("kill -10 " + Process.myPid());
                Log.d(TAG, "recycle mediaplayer instance");
            }
        } catch (Exception e) {
            Log.e(TAG, "exec error");
        }
    }

    private void releaseMediaPlayerInstance(String packageName) {
        if (packageName != null && packageName.length() > 0) {
            synchronized (mPlayerLock) {
                Integer count = mPlayerMap.get(packageName);
                if (count != null) {
                    Integer count2 = Integer.valueOf(count.intValue() - 1);
                    mPlayerMap.put(packageName, count2);
                    if (count2.intValue() <= 0) {
                        mPlayerMap.remove(packageName);
                    }
                }
            }
        }
    }

    public OppoMediaPlayer() {
        Log.v(TAG, "new OppoMediaPlayer");
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            Looper looper2 = Looper.getMainLooper();
            if (looper2 != null) {
                this.mEventHandler = new EventHandler(this, looper2);
            } else {
                this.mEventHandler = null;
            }
        }
        this.mAppOps = IAppOpsService.Stub.asInterface(ServiceManager.getService(Context.APP_OPS_SERVICE));
        native_setup(new WeakReference(this));
        this.mSeekPosition = -1;
        this.mCurrentPosition = -1;
        this.mNotificationManager = new ColorNotificationManager();
        this.mCurrentState = 1;
        String packageName = ActivityThread.currentPackageName();
        if (packageName != null && packageName.length() > 0) {
            Log.i(TAG, "new mediaplayer ,packageName = " + packageName);
            if (packageName.contains(CTS_TEST_PACKAGE)) {
                Log.i(TAG, "Cts test start ");
            }
            if (packageName.equals(GOOGLE_MUSIC_PACKAGE)) {
                this.mDisableOppoMedia = true;
                Log.i(TAG, "Google Music don't use oppo player");
            }
            String result_gc = OppoAtlasManager.getInstance(null).getParameters("check_listinfo_byname=mediaplayer-recycle=" + packageName);
            if (result_gc != null && result_gc.equals("true")) {
                addMediaPlayerInstance(packageName);
            }
        }
        if (SystemProperties.getBoolean("remove.oppomedia.support", false)) {
            this.mDisableOppoMedia = true;
            Log.i(TAG, "remove.oppomedia.support is true!");
        }
    }

    public void closeFd() {
        try {
            Log.d(TAG, "closeFd()");
            if (this.mFd != null && this.mFd.valid()) {
                Log.d(TAG, "mFd is valid, close it.");
                Os.close(this.mFd);
                this.mFd = null;
                this.mOffset = -1;
                this.mLength = -1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception = " + e);
            e.printStackTrace();
        }
    }

    public boolean setDisplayWithOppoIfNeeded(SurfaceHolder sh) {
        if (this.mDebugLog) {
            Log.i(TAG, "setDisplay() isOppoCreate=" + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        setDisplay(sh);
        return true;
    }

    public boolean setSurfaceWithOppoIfNeeded(Surface surface) {
        if (this.mDebugLog) {
            Log.i(TAG, "setDisplay() isOppoCreate=" + this.isOppoCreate);
        }
        this.mSurface = surface;
        if (!this.isOppoCreate) {
            return false;
        }
        setSurface(surface);
        return true;
    }

    public boolean setVideoScalingModeWithOppoIfNeeded(int mode) {
        if (this.mDebugLog) {
            Log.i(TAG, "setDisplay() isOppoCreate=" + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        setVideoScalingMode(mode);
        return true;
    }

    public void preSetDataSource(Context context, Uri uri) {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(Context, Uri) isOppoCreate=" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = false;
        this.mDefaultPlayerStarted = false;
        this.mContext = context;
        this.mUri = uri;
        this.mCurrentState = 2;
    }

    public void preSetDataSource(Context context, Uri uri, Map<String, String> headers) {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(Context, Uri, headers) isOppoCreate=" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = false;
        this.mDefaultPlayerStarted = false;
        this.mContext = context;
        this.mUri = uri;
        this.mHeaders = headers;
        this.mCurrentState = 2;
    }

    public void preSetDataSource(String path, Map<String, String> headers) {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(path, headers) isOppoCreate=" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = false;
        this.mDefaultPlayerStarted = false;
        this.mPath = path;
        this.mHeaders = headers;
        this.mCurrentState = 2;
    }

    public void preSetDataSource(FileDescriptor fd) {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(FileDescriptor fd) isOppoCreate=" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = false;
        this.mDefaultPlayerStarted = false;
        this.mCurrentState = 2;
    }

    public void preSetDataSource(String path) {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(String) isOppoCreate=" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = false;
        this.mDefaultPlayerStarted = false;
        this.mPath = path;
        this.mCurrentState = 2;
    }

    public void preSetDataSource(FileDescriptor fd, long offset, long length) {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(FileDescriptor, long, long) isOppoCreate=" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = false;
        this.mDefaultPlayerStarted = false;
        try {
            closeFd();
            this.mFd = Os.dup(fd);
            this.mOffset = offset;
            this.mLength = length;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        }
        this.mCurrentState = 2;
    }

    public boolean preSetDataSource(MediaDataSource dataSource) {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(MediaDataSource) isOppoCreate = " + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = true;
        this.mDefaultPlayerStarted = false;
        if (!this.isOppoCreate) {
            return false;
        }
        Log.e(TAG, "setDataSource(MediaDataSource) oppomedia do not support! return directly");
        return true;
    }

    public boolean prepareWithOppoIfNeeded() throws IOException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "prepare() isOppoCreate=" + this.isOppoCreate);
        }
        this.mPrepareAsync = false;
        this.mCurrentState = 4;
        if (!this.isOppoCreate) {
            return false;
        }
        prepare();
        return true;
    }

    public boolean prepareAsyncWithOppoIfNeeded() throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "prepareAsync() isOppoCreate=" + this.isOppoCreate);
        }
        this.mPrepareAsync = true;
        this.mCurrentState = 4;
        if (!this.isOppoCreate) {
            return false;
        }
        prepareAsync();
        return true;
    }

    public void checkAudioStreamType(AudioAttributes attributes) {
        if (this.mAudioStreamType == -1) {
            this.mAudioStreamType = audioAttributesToStreamType(attributes);
        }
    }

    private int audioAttributesToStreamType(AudioAttributes attr) {
        if ((attr.getAllFlags() & 1) == 1) {
            return 7;
        }
        if ((attr.getAllFlags() & 4) == 4) {
            return 6;
        }
        switch (attr.getUsage()) {
            case 1:
            case 12:
            case 14:
            case 16:
                return 3;
            case 2:
                return 0;
            case 3:
                return 8;
            case 4:
                return 4;
            case 5:
            case 7:
            case 8:
            case 9:
            case 10:
                return 5;
            case 6:
                return 2;
            case 11:
                return 10;
            case 13:
                return 1;
            case 15:
            default:
                return 3;
        }
    }

    public boolean handleMediaPlayerError(MediaPlayer mPlayer) {
        if (this.mDebugLog) {
            Log.i(TAG, "handleMediaPlayerError() mCurrentState=" + this.mCurrentState);
        }
        int mChangeState = this.mCurrentState;
        this.mCurrentState = 1;
        if (this.mNotUsingOppoMedia) {
            Log.i(TAG, "handleMediaPlayerError() mNotUsingOppoMedia is true");
            return false;
        } else if (mPlayer == null) {
            Log.w(TAG, "MediaPlayer is null, return false");
            return false;
        } else {
            createOppoMediaPlayer(mPlayer);
            if (mChangeState >= 2) {
                try {
                    if (this.mPath != null) {
                        Log.d(TAG, "handleMediaPlayerError() mOppoMediaPlayer setDataSource(String)");
                        setDataSource(this.mPath);
                    } else if (this.mFd == null || !this.mFd.valid()) {
                        if (this.mUri != null) {
                            Log.d(TAG, "handleMediaPlayerError() mOppoMediaPlayer setDataSource(Context, Uri, Map)");
                            setDataSource(this.mContext, this.mUri, this.mHeaders);
                        } else {
                            Log.e(TAG, "handleMediaPlayerError() mOppoMediaPlayer setDataSource null");
                            closeFd();
                            return false;
                        }
                    } else if (this.mOffset > 0) {
                        Log.d(TAG, "handleMediaPlayerError() mOppoMediaPlayer setDataSource(fd, offset,length) mOffset = " + this.mOffset + " mLength = " + this.mLength);
                        setDataSource(this.mFd, this.mOffset, this.mLength);
                    } else {
                        Log.d(TAG, "handleMediaPlayerError() mOppoMediaPlayer setDataSource(fd)");
                        setDataSource(this.mFd);
                    }
                    closeFd();
                    try {
                        if (mPlayer.getSurfaceHolder() != null) {
                            mPlayer.setDisplay(mPlayer.getSurfaceHolder());
                        }
                        if (this.mSurface != null) {
                            setSurface(this.mSurface);
                        }
                    } catch (Exception iex) {
                        iex.printStackTrace();
                        return false;
                    }
                } catch (Exception ex) {
                    Log.w(TAG, "mOppoMediaPlayer setDataSource error mPath=" + this.mPath + " mUri=" + this.mUri);
                    closeFd();
                    ex.printStackTrace();
                    return false;
                }
            }
            if (mChangeState >= 4) {
                Log.e(TAG, "handleMediaPlayerError() mOppoMediaPlayer prepare mPrepareAsync=" + this.mPrepareAsync);
                if (this.mPrepareAsync) {
                    try {
                        mPlayer.prepareAsync();
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "handleMediaPlayerError() prepareAsync error!");
                        e.printStackTrace();
                        return false;
                    }
                } else {
                    try {
                        prepare();
                        this.mCurrentState = 8;
                    } catch (IOException e2) {
                        Log.e(TAG, "handleMediaPlayerError() prepare error!");
                        e2.printStackTrace();
                        return false;
                    }
                }
            }
            if (this.mSeekMs >= 0) {
                this.mNeedSeeking = true;
            }
            if (mChangeState >= 16 && mChangeState != 32) {
                Log.v(TAG, "handleMediaPlayerError() mOppoMediaPlayer start mPrepareAsync=" + this.mPrepareAsync);
                if (!this.mPrepareAsync) {
                    mPlayer.start();
                }
            }
            return true;
        }
    }

    private void createOppoMediaPlayer(final MediaPlayer mPlayer) {
        if (!this.mNotUsingOppoMedia && mPlayer != null) {
            int tempSeekMs = this.mSeekMs;
            boolean tempScreenOn = this.mScreenOn;
            this.isCreateOppoMediaPlayer = true;
            mPlayer.reset();
            this.mSeekMs = tempSeekMs;
            this.mScreenOn = tempScreenOn;
            if (mPlayer.mOppoMediaPlayer == null || !this.isOppoCreate) {
                if (mPlayer.mOppoMediaPlayer == null) {
                    mPlayer.mOppoMediaPlayer = new OppoMediaPlayer();
                }
                this.isOppoCreate = true;
                if (this.mAudioSessionId >= 0) {
                    mPlayer.mOppoMediaPlayer.setAudioSessionId(this.mAudioSessionId);
                }
                if (this.mAudioStreamType >= 0) {
                    mPlayer.mOppoMediaPlayer.setAudioStreamType(this.mAudioStreamType);
                }
                if (this.mScreenOn) {
                    mPlayer.mOppoMediaPlayer.setScreenOnWhilePlaying(true);
                }
                mPlayer.mOppoMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                    /* class com.oppo.media.OppoMediaPlayer.AnonymousClass1 */

                    @Override // com.oppo.media.OppoMediaPlayer.OnPreparedListener
                    public void onPrepared(OppoMediaPlayer mp) {
                        if (mPlayer.getOnPreparedListener() != null) {
                            OppoMediaPlayer.this.mCurrentState = 8;
                            mPlayer.getOnPreparedListener().onPrepared(mPlayer);
                            if (OppoMediaPlayer.this.mDefaultPlayerStarted) {
                                Log.d(OppoMediaPlayer.TAG, "mDefaultPlayerStarted is true ,start !");
                                mPlayer.start();
                                OppoMediaPlayer.this.mDefaultPlayerStarted = false;
                            }
                        }
                    }
                });
                mPlayer.mOppoMediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
                    /* class com.oppo.media.OppoMediaPlayer.AnonymousClass2 */

                    @Override // com.oppo.media.OppoMediaPlayer.OnBufferingUpdateListener
                    public void onBufferingUpdate(OppoMediaPlayer mp, int percent) {
                        if (mPlayer.getOnBufferingUpdateListener() != null) {
                            mPlayer.getOnBufferingUpdateListener().onBufferingUpdate(mPlayer, percent);
                        }
                    }
                });
                mPlayer.mOppoMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    /* class com.oppo.media.OppoMediaPlayer.AnonymousClass3 */

                    @Override // com.oppo.media.OppoMediaPlayer.OnCompletionListener
                    public void onCompletion(OppoMediaPlayer mp) {
                        if (mPlayer.getOnCompletionListener() != null) {
                            mPlayer.getOnCompletionListener().onCompletion(mPlayer);
                        }
                    }
                });
                mPlayer.mOppoMediaPlayer.setOnErrorListener(new OnErrorListener() {
                    /* class com.oppo.media.OppoMediaPlayer.AnonymousClass4 */

                    @Override // com.oppo.media.OppoMediaPlayer.OnErrorListener
                    public boolean onError(OppoMediaPlayer mp, int what, int extra) {
                        Log.e(OppoMediaPlayer.TAG, "mOppoMediaPlayer OnError is running");
                        if (mPlayer.getOnErrorListener() == null) {
                            return true;
                        }
                        mPlayer.getOnErrorListener().onError(mPlayer, what, extra);
                        return true;
                    }
                });
                mPlayer.mOppoMediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
                    /* class com.oppo.media.OppoMediaPlayer.AnonymousClass5 */

                    @Override // com.oppo.media.OppoMediaPlayer.OnSeekCompleteListener
                    public void onSeekComplete(OppoMediaPlayer mp) {
                        if (mPlayer.getOnSeekCompleteListener() != null) {
                            mPlayer.getOnSeekCompleteListener().onSeekComplete(mPlayer);
                        }
                    }
                });
                mPlayer.mOppoMediaPlayer.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
                    /* class com.oppo.media.OppoMediaPlayer.AnonymousClass6 */

                    @Override // com.oppo.media.OppoMediaPlayer.OnVideoSizeChangedListener
                    public void onVideoSizeChanged(OppoMediaPlayer mp, int width, int height) {
                        if (mPlayer.getOnVideoSizeChangedListener() != null) {
                            mPlayer.getOnVideoSizeChangedListener().onVideoSizeChanged(mPlayer, width, height);
                        }
                    }
                });
                mPlayer.mOppoMediaPlayer.setOnInfoListener(new OnInfoListener() {
                    /* class com.oppo.media.OppoMediaPlayer.AnonymousClass7 */

                    @Override // com.oppo.media.OppoMediaPlayer.OnInfoListener
                    public boolean onInfo(OppoMediaPlayer mp, int what, int extra) {
                        if (mPlayer.getOnInfoListener() == null) {
                            return true;
                        }
                        mPlayer.getOnInfoListener().onInfo(mPlayer, what, extra);
                        return true;
                    }
                });
                return;
            }
            mPlayer.mOppoMediaPlayer.reset();
            mPlayer.mOppoMediaPlayer.isOppoCreate = true;
            Log.i(TAG, "mOppoMediaPlayer has exist, return.");
        }
    }

    public boolean handleError(Message msg, MediaPlayer mPlayer) {
        int i;
        if (msg.arg1 == 100 || msg.arg1 == -38 || msg.arg2 == -19 || msg.arg2 == -38 || (i = this.mCurrentState) == 64 || i == 261 || this.mDisableOppoMedia || this.mNotUsingOppoMedia) {
            return false;
        }
        if (this.isOppoCreate || handleMediaPlayerError(mPlayer)) {
            return true;
        }
        boolean was_handled = false;
        MediaPlayer.OnErrorListener onErrorListener = mPlayer.getOnErrorListener();
        if (onErrorListener != null) {
            was_handled = onErrorListener.onError(mPlayer, msg.arg1, msg.arg2);
        }
        MediaPlayer.OnCompletionListener onCompletionListener = mPlayer.getOnCompletionListener();
        if (onCompletionListener != null && !was_handled) {
            onCompletionListener.onCompletion(mPlayer);
        }
        stayAwake(false);
        return true;
    }

    public Parcel checkZenMode() {
        String packageName = ActivityThread.currentPackageName();
        if (packageName == null || packageName.length() <= 0) {
            return null;
        }
        String result = OppoAtlasManager.getInstance(null).getParameters("check_listinfo_byname=zenmode=" + packageName);
        if (result == null || !result.equals("true")) {
            return null;
        }
        Log.i(TAG, "The package name is " + packageName + "qq & wecha zenmode control !");
        int uid = Process.myUid();
        ColorNotificationManager colorNotificationManager = this.mNotificationManager;
        if (colorNotificationManager != null) {
            try {
                boolean isDriverMode = colorNotificationManager.isSuppressedByDriveMode(uid);
                Log.i(TAG, "isDriverMode: " + isDriverMode);
                if (isDriverMode) {
                    this.mInterceptFlag = this.mNotificationManager.shouldInterceptSound(packageName, uid);
                }
            } catch (RemoteException e) {
                Log.e(TAG, " start RemoteException");
            }
        }
        Parcel pMute = Parcel.obtain();
        pMute.writeInt(this.mInterceptFlag ? 1 : 0);
        return pMute;
    }

    public Parcel checkWechatMute() {
        String packageName = ActivityThread.currentPackageName();
        if (packageName == null || packageName.length() <= 0) {
            return null;
        }
        String result = OppoAtlasManager.getInstance(null).getParameters("check_listinfo_byname=zenmode=" + packageName);
        if (result != null && result.equals("wechat_mute")) {
            Log.i(TAG, "The package name is " + packageName + "wecha zenmode control !");
            int i = this.mStreamType;
            if (i == 5 || i == 2) {
                Parcel pMute = Parcel.obtain();
                pMute.writeInt(this.mInterceptFlag ? 1 : 0);
                return pMute;
            }
        }
        return null;
    }

    public boolean startImplWithOppoIfNeeded(MediaPlayer mPlayer) {
        if (this.mDebugLog) {
            Log.i(TAG, "start() isOppoCreate = " + this.isOppoCreate + " mCurrentState = " + this.mCurrentState);
        }
        if (mPlayer == null) {
            Log.w(TAG, "MediaPlayer is null");
            return false;
        } else if (!this.isOppoCreate) {
            this.mDefaultPlayerStarted = true;
            this.mCurrentState = 16;
            return false;
        } else if (this.mCurrentState < 8 && this.mPrepareAsync) {
            return true;
        } else {
            if (this.mNeedSeeking) {
                Log.v(TAG, "mOppoMediaPlayer start seekTo: " + this.mSeekMs);
                mPlayer.seekTo(this.mSeekMs);
                this.mNeedSeeking = false;
                this.mSeekMs = -1;
            }
            start();
            this.mCurrentState = 16;
            return true;
        }
    }

    public boolean stopWithOppoIfNeeded() {
        if (this.mDebugLog) {
            Log.i(TAG, "stop() isOppoCreate = " + this.isOppoCreate);
        }
        this.mCurrentState = 64;
        this.mScreenOn = false;
        if (!this.isOppoCreate) {
            return false;
        }
        stop();
        return true;
    }

    public boolean pauseWithOppoIfNeeded() {
        if (this.mDebugLog) {
            Log.i(TAG, "pause() isOppoCreate=" + this.isOppoCreate);
        }
        this.mCurrentState = 32;
        this.mDefaultPlayerStarted = false;
        if (!this.isOppoCreate) {
            return false;
        }
        pause();
        return true;
    }

    public boolean setWakeModeWithOppoIfNeeded(Context context, int mode) {
        if (this.mDebugLog) {
            Log.i(TAG, "setWakeMode() isOppoCreate=" + this.isOppoCreate + " mode=" + mode);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        setWakeMode(context, mode);
        return true;
    }

    public boolean setScreenOnWhilePlayingWithOppoIfNeeded(boolean screenOn) {
        if (this.mDebugLog) {
            Log.i(TAG, "setScreenOnWhilePlaying() isOppoCreate=" + this.isOppoCreate + " screenOn=" + screenOn);
        }
        if (screenOn) {
            this.mScreenOn = true;
        }
        if (!this.isOppoCreate) {
            return false;
        }
        setScreenOnWhilePlaying(screenOn);
        return true;
    }

    public int getVideoWidthWithOppo() {
        if (this.mDebugLog) {
            Log.i(TAG, "getVideoWidth() isOppoCreate=" + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            return getVideoWidth();
        }
        return -1;
    }

    public int getVideoHeightWithOppo() {
        if (this.mDebugLog) {
            Log.i(TAG, "getVideoHeight() isOppoCreate=" + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            return getVideoHeight();
        }
        return -1;
    }

    public boolean isOppoCreate() {
        if (this.mDebugLog) {
            Log.i(TAG, "isPlaying() isOppoCreate=" + this.isOppoCreate);
        }
        return this.isOppoCreate;
    }

    public boolean seekToWithOppoIfNeeded(long msec, int mode) {
        this.mSeekMs = (int) msec;
        if (!this.isOppoCreate) {
            return false;
        }
        if (this.mCurrentState < 8 && this.mPrepareAsync) {
            return true;
        }
        seekTo((int) msec);
        return true;
    }

    public int getCurrentPositionWithOppo() {
        boolean z = this.mDebugLog;
        int i = this.mCurrentState;
        if (i < 2) {
            return 0;
        }
        if (!this.isOppoCreate) {
            return -1;
        }
        if (i < 8) {
            return 0;
        }
        return getCurrentPosition();
    }

    public boolean needGetFromOppo() {
        if (this.mCurrentState >= 2 && !this.isOppoCreate) {
            return false;
        }
        return true;
    }

    public int getDurationWithOppo() {
        int i = this.mCurrentState;
        if (i < 2 || !this.isOppoCreate) {
            return -1;
        }
        if (i < 8) {
            return 0;
        }
        return getDuration();
    }

    public void releaseWithOppo() {
        if (this.mDebugLog) {
            Log.i(TAG, "release() isOppoCreate =" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = true;
        this.mCurrentState = 1;
        closeFd();
        release();
        this.isOppoCreate = false;
        this.mAudioSessionId = -1;
        this.mAudioStreamType = -1;
        this.mScreenOn = false;
        String packageName = ActivityThread.currentPackageName();
        String result_gc = OppoAtlasManager.getInstance(null).getParameters("check_listinfo_byname=mediaplayer-recycle=" + packageName);
        if (result_gc != null && result_gc.equals("true")) {
            releaseMediaPlayerInstance(packageName);
        }
    }

    public boolean resetWithOppoIfNeeded() {
        if (this.mDebugLog) {
            Log.i(TAG, "reset() isOppoCreate =" + this.isOppoCreate);
        }
        this.mSeekMs = -1;
        this.mCurrentState = 1;
        if (!this.isCreateOppoMediaPlayer) {
            closeFd();
        } else {
            this.isCreateOppoMediaPlayer = false;
        }
        if (this.isOppoCreate) {
            reset();
            this.mDefaultPlayerStarted = false;
            this.mScreenOn = false;
            this.isOppoCreate = false;
            return true;
        }
        this.mScreenOn = false;
        this.isOppoCreate = false;
        return false;
    }

    public void resetZenModeFlag() {
        this.mInterceptFlag = false;
    }

    public boolean setAudioStreamTypeWithOppoIfNeeded(int streamtype) {
        this.mAudioStreamType = streamtype;
        if (!this.isOppoCreate) {
            return false;
        }
        setAudioStreamType(streamtype);
        return true;
    }

    public boolean setAudioAttributesWithOppoIfNeeded() {
        if (this.mDebugLog) {
            Log.i(TAG, "setAudioAttributes() isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        Log.e(TAG, "setAudioAttributes() oppomedia do not support! return directly");
        return true;
    }

    public boolean setLoopingWithOppoIfNeeded(boolean looping) {
        if (!this.isOppoCreate) {
            return false;
        }
        setLooping(looping);
        return true;
    }

    public boolean setVolumeWithOppoIfNeeded(float leftVolume, float rightVolume) {
        if (!this.isOppoCreate) {
            return false;
        }
        setVolume(leftVolume, rightVolume);
        return true;
    }

    public boolean setAudioSessionIdWithOppoIfNeeded(int sessionId) throws IllegalArgumentException, IllegalStateException {
        this.mAudioSessionId = sessionId;
        if (!this.isOppoCreate) {
            return false;
        }
        setAudioSessionId(sessionId);
        return true;
    }

    public boolean attachAuxEffectWithOppoIfNeeded(int effectId) {
        if (this.mDebugLog) {
            Log.i(TAG, "attachAuxEffect(), isOppoCreate =" + this.isOppoCreate + ", effectId = " + effectId);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        attachAuxEffect(effectId);
        return true;
    }

    public boolean setAuxEffectSendLevelWithOppoIfNeeded(float level) {
        if (this.mDebugLog) {
            Log.i(TAG, "setAuxEffectSendLevel(), isOppoCreate =" + this.isOppoCreate + ", level = " + level);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        setAuxEffectSendLevel(level);
        return true;
    }

    public MediaPlayer.TrackInfo[] getTrackInfoWithOppo(MediaPlayer mPlayer) throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "getTrackInfo() isOppoCreate=" + this.isOppoCreate);
        }
        if (mPlayer == null) {
            Log.w(TAG, "MediaPlayer is null");
            return null;
        } else if (!this.isOppoCreate) {
            return null;
        } else {
            Parcel mParcel = Parcel.obtain();
            TrackInfo[] mtrackInfo = getTrackInfo();
            MediaPlayer.TrackInfo[] trackInfo1 = new MediaPlayer.TrackInfo[mtrackInfo.length];
            for (int i = 0; i < mtrackInfo.length; i++) {
                mParcel.setDataPosition(0);
                mParcel.writeInt(mtrackInfo[i].getTrackType());
                mParcel.writeString(mtrackInfo[i].getLanguage());
                mParcel.setDataPosition(0);
                trackInfo1[i] = mPlayer.getTrackInfo(mParcel);
            }
            return trackInfo1;
        }
    }

    public boolean setSubtitleAnchorWithOppoIfNeeded() {
        if (this.mDebugLog) {
            Log.i(TAG, "setSubtitleAnchor() isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        Log.e(TAG, "setSubtitleAnchor() oppomedia do not support! return directly");
        return true;
    }

    public boolean onSubtitleTrackSelectedWithOppoIfNeeded() {
        if (this.mDebugLog) {
            Log.i(TAG, "onSubtitleTrackSelected() isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        Log.e(TAG, "onSubtitleTrackSelected() oppomedia do not support! return directly");
        return true;
    }

    public boolean addSubtitleSourceWithOppoIfNeeded() {
        if (this.mDebugLog) {
            Log.i(TAG, "addSubtitleSource() isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        Log.e(TAG, "addSubtitleSource() oppomedia do not support! return directly");
        return true;
    }

    public boolean addTimedTextSourceWithOppoIfNeeded() {
        if (this.mDebugLog) {
            Log.i(TAG, "addTimedTextSource() isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        Log.e(TAG, "addTimedTextSource(String, String ) oppomedia do not support! return directly");
        return true;
    }

    public boolean addTimedTextSourceWithOppoIfNeeded(Context context, Uri uri, String mimeType) throws IOException, IllegalArgumentException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "addTimedTextSource() isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        Log.e(TAG, "addTimedTextSource(Context , Uri , String) oppomedia do not support! return directly");
        return true;
    }

    public boolean addTimedTextSourceWithOppoIfNeeded(FileDescriptor fd, String mimeType) throws IllegalArgumentException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "addTimedTextSource(FileDescriptor, String) isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        Log.e(TAG, "addTimedTextSource(FileDescriptor, String) oppomedia do not support! return directly");
        return true;
    }

    public boolean addTimedTextSourceWithOppoIfNeeded(FileDescriptor fd, long offset, long length, String mime) throws IllegalArgumentException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "addTimedTextSource(FileDescriptor, long, long, String) isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        Log.e(TAG, "addTimedTextSource(FileDescriptor, long, long, String) oppomedia do not support! return directly");
        return true;
    }

    public boolean getSelectedTrackWithOppoIfNeeded(int trackType) throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "getSelectedTrack() isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        Log.e(TAG, "getSelectedTrack() oppomedia do not support! return directly");
        return true;
    }

    public boolean selectTrackWithOppoIfNeeded(int index) throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "selectTrack() isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        selectTrack(index);
        return true;
    }

    public boolean deselectTrackWithOppoIfNeeded(int index) throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "deselectTrack() isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        deselectTrack(index);
        return true;
    }

    public boolean setRetransmitEndpointWithOppoIfNeeded(InetSocketAddress endpoint) throws IllegalStateException, IllegalArgumentException {
        if (this.mDebugLog) {
            Log.i(TAG, "setRetransmitEndpoint() isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        Log.e(TAG, "setRetransmitEndpoint() oppomedia do not support! return directly");
        return true;
    }

    public boolean getMediaTimeProviderWithOppoIfNeeded() {
        if (this.mDebugLog) {
            Log.i(TAG, "getMediaTimeProvider() isOppoCreate = " + this.isOppoCreate);
        }
        if (!this.isOppoCreate) {
            return false;
        }
        Log.e(TAG, "getMediaTimeProvider() oppomedia do not support! return directly");
        return true;
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
        AssetFileDescriptor afd = null;
        try {
            AssetFileDescriptor afd2 = context.getResources().openRawResourceFd(resid);
            if (afd2 == null) {
                if (afd2 != null) {
                    try {
                        afd2.close();
                    } catch (IOException e) {
                    }
                }
                return null;
            }
            OppoMediaPlayer mp = new OppoMediaPlayer();
            mp.setDataSource(afd2.getFileDescriptor(), afd2.getStartOffset(), afd2.getLength());
            mp.prepare();
            try {
                afd2.close();
            } catch (IOException e2) {
            }
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
            if (0 != 0) {
                afd.close();
            }
            return null;
        } catch (IllegalArgumentException ex2) {
            Log.d(TAG, "create failed:", ex2);
            if (0 != 0) {
                afd.close();
            }
            return null;
        } catch (SecurityException ex3) {
            Log.d(TAG, "create failed:", ex3);
            if (0 != 0) {
                try {
                    afd.close();
                } catch (IOException e3) {
                }
            }
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    afd.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, (Map<String, String>) null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0053, code lost:
        if (0 == 0) goto L_0x005d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0055, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005a, code lost:
        if (0 == 0) goto L_0x005d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005d, code lost:
        android.util.Log.d(com.oppo.media.OppoMediaPlayer.TAG, "Couldn't open file on client side, trying server side");
        setDataSource(r11.toString(), r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006b, code lost:
        return;
     */
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals(ContentResolver.SCHEME_FILE)) {
            setDataSource(uri.getPath());
            return;
        }
        AssetFileDescriptor fd = null;
        try {
            AssetFileDescriptor fd2 = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (fd2 != null) {
                if (fd2.getDeclaredLength() < 0) {
                    setDataSource(fd2.getFileDescriptor());
                } else {
                    setDataSource(fd2.getFileDescriptor(), fd2.getStartOffset(), fd2.getDeclaredLength());
                }
                fd2.close();
            } else if (fd2 != null) {
                fd2.close();
            }
        } catch (SecurityException e) {
        } catch (IOException e2) {
        } catch (Throwable th) {
            if (0 != 0) {
                fd.close();
            }
            throw th;
        }
    }

    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(path, (String[]) null, (String[]) null);
    }

    public void setDataSource(String path, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        String[] keys = null;
        String[] values = null;
        if (headers != null) {
            keys = new String[headers.size()];
            values = new String[headers.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = entry.getValue();
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
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                washeld = true;
                this.mWakeLock.release();
            }
            this.mWakeLock = null;
        }
        this.mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(536870912 | mode, OppoMediaPlayer.class.getName());
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stayAwake(boolean awake) {
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null) {
            if (awake && !wakeLock.isHeld()) {
                this.mWakeLock.acquire();
            } else if (!awake && this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
        this.mStayAwake = awake;
        updateSurfaceScreenOn();
    }

    private void updateSurfaceScreenOn() {
        SurfaceHolder surfaceHolder = this.mSurfaceHolder;
        if (surfaceHolder != null) {
            surfaceHolder.setKeepScreenOn(this.mScreenOnWhilePlaying && this.mStayAwake);
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
        int i = this.mCurrentPosition;
        if (i >= 0) {
            return i;
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
        this.mContext = null;
        _release();
    }

    public void reset() {
        stayAwake(false);
        _reset();
        this.mEventHandler.removeCallbacksAndMessages(null);
    }

    public static class TrackInfo implements Parcelable {
        static final Parcelable.Creator<TrackInfo> CREATOR = new Parcelable.Creator<TrackInfo>() {
            /* class com.oppo.media.OppoMediaPlayer.TrackInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public TrackInfo createFromParcel(Parcel in) {
                return new TrackInfo(in);
            }

            @Override // android.os.Parcelable.Creator
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

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mTrackType);
            dest.writeString(this.mLanguage);
        }
    }

    public TrackInfo[] getTrackInfo() throws IllegalStateException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInt(1);
            invoke(request, reply);
            return (TrackInfo[]) reply.createTypedArray(TrackInfo.CREATOR);
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    private static boolean availableMimeTypeForExternalSource(String mimeType) {
        if (mimeType == "application/x-subrip") {
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

    public void addTimedTextSource(Context context, Uri uri, String mimeType) throws IOException, IllegalArgumentException, IllegalStateException {
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals(ContentResolver.SCHEME_FILE)) {
            addTimedTextSource(uri.getPath(), mimeType);
            return;
        }
        AssetFileDescriptor fd = null;
        try {
            AssetFileDescriptor fd2 = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (fd2 != null) {
                addTimedTextSource(fd2.getFileDescriptor(), mimeType);
                fd2.close();
            } else if (fd2 != null) {
                fd2.close();
            }
        } catch (SecurityException e) {
            if (0 == 0) {
                return;
            }
            fd.close();
        } catch (IOException e2) {
            if (0 == 0) {
                return;
            }
            fd.close();
        } catch (Throwable th) {
            if (0 != 0) {
                fd.close();
            }
            throw th;
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

    /* access modifiers changed from: protected */
    public void finalize() {
        native_finalize();
    }

    /* access modifiers changed from: private */
    public class EventHandler extends Handler {
        private OppoMediaPlayer mMediaPlayer;

        public EventHandler(OppoMediaPlayer mp, Looper looper) {
            super(looper);
            this.mMediaPlayer = mp;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (this.mMediaPlayer.mNativeContext == 0) {
                Log.w(OppoMediaPlayer.TAG, "mediaplayer went away with unhandled events");
                return;
            }
            Log.e(OppoMediaPlayer.TAG, "handleMessage (" + msg.arg1 + SmsManager.REGEX_PREFIX_DELIMITER + msg.arg2 + ")");
            int i = msg.what;
            if (i == 0) {
                return;
            }
            if (i != 1) {
                if (i == 2) {
                    if (OppoMediaPlayer.this.mOnCompletionListener != null) {
                        OppoMediaPlayer.this.mOnCompletionListener.onCompletion(this.mMediaPlayer);
                    }
                    OppoMediaPlayer.this.stayAwake(false);
                } else if (i != 3) {
                    if (i == 4) {
                        if (OppoMediaPlayer.this.mSeekPosition != OppoMediaPlayer.this.mCurrentPosition) {
                            Log.v(OppoMediaPlayer.TAG, "Executing queued seekTo " + OppoMediaPlayer.this.mSeekPosition);
                            OppoMediaPlayer.this.mSeekPosition = -1;
                            OppoMediaPlayer oppoMediaPlayer = OppoMediaPlayer.this;
                            oppoMediaPlayer.seekTo(oppoMediaPlayer.mCurrentPosition);
                        } else {
                            Log.v(OppoMediaPlayer.TAG, "All seeks complete - return to regularly scheduled program");
                            OppoMediaPlayer oppoMediaPlayer2 = OppoMediaPlayer.this;
                            oppoMediaPlayer2.mCurrentPosition = oppoMediaPlayer2.mSeekPosition = -1;
                        }
                        if (OppoMediaPlayer.this.mOnSeekCompleteListener != null) {
                            OppoMediaPlayer.this.mOnSeekCompleteListener.onSeekComplete(this.mMediaPlayer);
                        }
                    } else if (i != 5) {
                        if (i == 100) {
                            Log.e(OppoMediaPlayer.TAG, "Error (" + msg.arg1 + SmsManager.REGEX_PREFIX_DELIMITER + msg.arg2 + ")");
                            boolean error_was_handled = false;
                            if (OppoMediaPlayer.this.mOnErrorListener != null) {
                                error_was_handled = OppoMediaPlayer.this.mOnErrorListener.onError(this.mMediaPlayer, msg.arg1, msg.arg2);
                            }
                            if (OppoMediaPlayer.this.mOnCompletionListener != null && !error_was_handled) {
                                OppoMediaPlayer.this.mOnCompletionListener.onCompletion(this.mMediaPlayer);
                            }
                            OppoMediaPlayer.this.stayAwake(false);
                        } else if (i != 200) {
                            Log.e(OppoMediaPlayer.TAG, "Unknown message type " + msg.what);
                        } else {
                            if (msg.arg1 != 700) {
                                Log.i(OppoMediaPlayer.TAG, "Info (" + msg.arg1 + SmsManager.REGEX_PREFIX_DELIMITER + msg.arg2 + ")");
                            }
                            if (OppoMediaPlayer.this.mOnInfoListener != null) {
                                OppoMediaPlayer.this.mOnInfoListener.onInfo(this.mMediaPlayer, msg.arg1, msg.arg2);
                            }
                        }
                    } else if (OppoMediaPlayer.this.mOnVideoSizeChangedListener != null) {
                        OppoMediaPlayer.this.mOnVideoSizeChangedListener.onVideoSizeChanged(this.mMediaPlayer, msg.arg1, msg.arg2);
                    }
                } else if (OppoMediaPlayer.this.mOnBufferingUpdateListener != null) {
                    OppoMediaPlayer.this.mOnBufferingUpdateListener.onBufferingUpdate(this.mMediaPlayer, msg.arg1);
                }
            } else if (OppoMediaPlayer.this.mOnPreparedListener != null) {
                OppoMediaPlayer.this.mOnPreparedListener.onPrepared(this.mMediaPlayer);
            }
        }
    }

    private static void postEventFromNative(Object mediaplayer_ref, int what, int arg1, int arg2, Object obj) {
        OppoMediaPlayer mp = (OppoMediaPlayer) ((WeakReference) mediaplayer_ref).get();
        if (mp != null) {
            if (what == 200 && arg1 == 2) {
                mp.start();
            }
            EventHandler eventHandler = mp.mEventHandler;
            if (eventHandler != null) {
                mp.mEventHandler.sendMessage(eventHandler.obtainMessage(what, arg1, arg2, obj));
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

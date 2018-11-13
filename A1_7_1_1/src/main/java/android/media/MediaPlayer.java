package android.media;

import android.app.ActivityThread;
import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.app.backup.FullBackup;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.camera2.params.TonemapCurve;
import android.media.AudioAttributes.Builder;
import android.media.MediaTimeProvider.OnMediaTimeListener;
import android.media.SubtitleController.Anchor;
import android.media.SubtitleController.Listener;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.util.Preconditions;
import com.mediatek.common.MPlugin;
import com.mediatek.common.media.IOmaSettingHelper;
import com.oppo.media.OppoMediaPlayer;
import com.oppo.media.OppoMultimediaManager;
import com.oppo.media.OppoMultimediaServiceDefine.ModuleTag;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.BitSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import libcore.io.Libcore;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class MediaPlayer extends PlayerBase implements Listener {
    public static final boolean APPLY_METADATA_FILTER = true;
    public static final boolean BYPASS_METADATA_FILTER = false;
    private static final String CTS_TEST_PACKAGE = ".cts";
    public static final int ERROR_CANNOT_CONNECT_TO_SERVER = 261;
    private static final String GOOGLE_MUSIC_PACKAGE = "com.google.android.music";
    private static final String IMEDIA_PLAYER = "android.media.IMediaPlayer";
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE = 2;
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE_FD = 3;
    private static final int INVOKE_ID_DESELECT_TRACK = 5;
    private static final int INVOKE_ID_GET_SELECTED_TRACK = 7;
    private static final int INVOKE_ID_GET_TRACK_INFO = 1;
    private static final int INVOKE_ID_SELECT_TRACK = 4;
    private static final int INVOKE_ID_SET_VIDEO_SCALE_MODE = 6;
    private static final int KEY_PARAMETER_AUDIO_ATTRIBUTES = 1400;
    private static final int KEY_PARAMETER_INTERCEPT = 10011;
    private static final int MEDIA_BUFFERING_UPDATE = 3;
    private static final int MEDIA_DURATION_UPDATE = 300;
    private static final int MEDIA_ERROR = 100;
    public static final int MEDIA_ERROR_IO = -1004;
    public static final int MEDIA_ERROR_MALFORMED = -1007;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    public static final int MEDIA_ERROR_SYSTEM = Integer.MIN_VALUE;
    public static final int MEDIA_ERROR_TIMED_OUT = -110;
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010;
    private static final int MEDIA_INFO = 200;
    public static final int MEDIA_INFO_AUDIO_NOT_SUPPORTED = 862;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_EXTERNAL_METADATA_UPDATE = 803;
    public static final int MEDIA_INFO_HAS_UNSUPPORT_VIDEO = 860;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    public static final int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_PAUSE_COMPLETED = 858;
    public static final int MEDIA_INFO_PLAY_COMPLETED = 859;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    public static final int MEDIA_INFO_VIDEO_NOT_SUPPORTED = 860;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    private static final int MEDIA_META_DATA = 202;
    public static final String MEDIA_MIMETYPE_TEXT_CEA_608 = "text/cea-608";
    public static final String MEDIA_MIMETYPE_TEXT_CEA_708 = "text/cea-708";
    public static final String MEDIA_MIMETYPE_TEXT_SUBRIP = "application/x-subrip";
    public static final String MEDIA_MIMETYPE_TEXT_VTT = "text/vtt";
    private static final int MEDIA_NOP = 0;
    private static final int MEDIA_PAUSED = 7;
    private static final int MEDIA_PAUSE_COMPLETE = 600;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    public static final int MEDIA_PLAYER_IDLE = 1;
    public static final int MEDIA_PLAYER_INITIALIZED = 2;
    public static final int MEDIA_PLAYER_PAUSED = 32;
    public static final int MEDIA_PLAYER_PLAYBACK_COMPLETE = 128;
    public static final int MEDIA_PLAYER_PREPARED = 8;
    public static final int MEDIA_PLAYER_PREPARING = 4;
    public static final int MEDIA_PLAYER_STARTED = 16;
    public static final int MEDIA_PLAYER_STOPPED = 64;
    private static final int MEDIA_PLAY_COMPLETE = 601;
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_SET_VIDEO_SIZE = 5;
    private static final int MEDIA_SKIPPED = 9;
    private static final int MEDIA_STARTED = 6;
    private static final int MEDIA_STOPPED = 8;
    private static final int MEDIA_SUBTITLE_DATA = 201;
    private static final int MEDIA_TIMED_TEXT = 99;
    public static final boolean METADATA_ALL = false;
    public static final boolean METADATA_UPDATE_ONLY = true;
    public static final int MUTE_FLAG = 1;
    public static final int PLAYBACK_RATE_AUDIO_MODE_DEFAULT = 0;
    public static final int PLAYBACK_RATE_AUDIO_MODE_RESAMPLE = 2;
    public static final int PLAYBACK_RATE_AUDIO_MODE_STRETCH = 1;
    private static final String SYSTEM_NOTIFICATION_AUDIO_PATH = "/system/media/audio/notifications/";
    private static final String TAG = "MediaPlayer";
    public static final int UNMUTE_FLAG = 0;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
    private boolean isCreateOppoMediaPlayer;
    private boolean isOppoCreate;
    private int mAudioSessionId;
    private int mAudioStreamType;
    private boolean mBypassInterruptionPolicy;
    private Context mContext;
    private boolean mCtsCallingPackage;
    private int mCurrentState;
    private boolean mDebugLog;
    private boolean mDefaultPlayerStarted;
    private EventHandler mEventHandler;
    private FileDescriptor mFd;
    private Map<String, String> mHeaders;
    private BitSet mInbandTrackIndices;
    private Vector<Pair<Integer, SubtitleTrack>> mIndexTrackPairs;
    private boolean mInterceptFlag;
    private long mLength;
    private int mListenerContext;
    private long mNativeContext;
    private long mNativeSurfaceTexture;
    private boolean mNeedMute;
    private boolean mNeedSeeking;
    private boolean mNotUsingOppoMedia;
    private final INotificationManager mNotificationManager;
    private long mOffset;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private OnCompletionListener mOnCompletionListener;
    private OnDurationUpdateListener mOnDurationUpdateListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnPreparedListener mOnPreparedListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnSubtitleDataListener mOnSubtitleDataListener;
    private OnTimedMetaDataAvailableListener mOnTimedMetaDataAvailableListener;
    private OnTimedTextListener mOnTimedTextListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private Vector<InputStream> mOpenSubtitleSources;
    private OppoMediaPlayer mOppoMediaPlayer;
    private String mPath;
    private boolean mPrepareAsync;
    private boolean mRecoverFlag;
    private boolean mScreenOn;
    private boolean mScreenOnWhilePlaying;
    private int mSeekMs;
    private int mSelectedSubtitleTrackIndex;
    private boolean mStayAwake;
    private int mStreamType;
    private SubtitleController mSubtitleController;
    private OnSubtitleDataListener mSubtitleDataListener;
    private Surface mSurface;
    private SurfaceHolder mSurfaceHolder;
    private TimeProvider mTimeProvider;
    private Uri mUri;
    private int mUsage;
    private WakeLock mWakeLock;

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i2);
    }

    public interface OnPreparedListener {
        void onPrepared(MediaPlayer mediaPlayer);
    }

    public interface OnCompletionListener {
        void onCompletion(MediaPlayer mediaPlayer);
    }

    /* renamed from: android.media.MediaPlayer$10 */
    class AnonymousClass10 implements Runnable {
        final /* synthetic */ MediaPlayer this$0;
        final /* synthetic */ MediaFormat val$fFormat;
        final /* synthetic */ InputStream val$fIs;
        final /* synthetic */ HandlerThread val$thread;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.10.<init>(android.media.MediaPlayer, java.io.InputStream, android.media.MediaFormat, android.os.HandlerThread):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass10(android.media.MediaPlayer r1, java.io.InputStream r2, android.media.MediaFormat r3, android.os.HandlerThread r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.10.<init>(android.media.MediaPlayer, java.io.InputStream, android.media.MediaFormat, android.os.HandlerThread):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.10.<init>(android.media.MediaPlayer, java.io.InputStream, android.media.MediaFormat, android.os.HandlerThread):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.10.addTrack():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private int addTrack() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.10.addTrack():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.10.addTrack():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.10.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.10.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.10.run():void");
        }
    }

    /* renamed from: android.media.MediaPlayer$11 */
    class AnonymousClass11 implements Runnable {
        final /* synthetic */ MediaPlayer this$0;
        final /* synthetic */ FileDescriptor val$fd3;
        final /* synthetic */ long val$length2;
        final /* synthetic */ long val$offset2;
        final /* synthetic */ HandlerThread val$thread;
        final /* synthetic */ SubtitleTrack val$track;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.11.<init>(android.media.MediaPlayer, java.io.FileDescriptor, long, long, android.media.SubtitleTrack, android.os.HandlerThread):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass11(android.media.MediaPlayer r1, java.io.FileDescriptor r2, long r3, long r5, android.media.SubtitleTrack r7, android.os.HandlerThread r8) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.11.<init>(android.media.MediaPlayer, java.io.FileDescriptor, long, long, android.media.SubtitleTrack, android.os.HandlerThread):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.11.<init>(android.media.MediaPlayer, java.io.FileDescriptor, long, long, android.media.SubtitleTrack, android.os.HandlerThread):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.11.addTrack():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private int addTrack() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.11.addTrack():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.11.addTrack():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.11.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.11.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.11.run():void");
        }
    }

    public interface OnSubtitleDataListener {
        void onSubtitleData(MediaPlayer mediaPlayer, SubtitleData subtitleData);
    }

    /* renamed from: android.media.MediaPlayer$1 */
    class AnonymousClass1 implements OnSubtitleDataListener {
        final /* synthetic */ MediaPlayer this$0;

        AnonymousClass1(MediaPlayer this$0) {
            this.this$0 = this$0;
        }

        public void onSubtitleData(MediaPlayer mp, SubtitleData data) {
            int index = data.getTrackIndex();
            synchronized (this.this$0.mIndexTrackPairs) {
                for (Pair<Integer, SubtitleTrack> p : this.this$0.mIndexTrackPairs) {
                    if (!(p.first == null || ((Integer) p.first).intValue() != index || p.second == null)) {
                        p.second.onData(data);
                    }
                }
            }
        }
    }

    /* renamed from: android.media.MediaPlayer$2 */
    class AnonymousClass2 implements com.oppo.media.OppoMediaPlayer.OnPreparedListener {
        final /* synthetic */ MediaPlayer this$0;

        AnonymousClass2(MediaPlayer this$0) {
            this.this$0 = this$0;
        }

        public void onPrepared(OppoMediaPlayer mp) {
            if (this.this$0.mOnPreparedListener != null) {
                this.this$0.mCurrentState = 8;
                this.this$0.mOnPreparedListener.onPrepared(this.this$0);
                if (this.this$0.mDefaultPlayerStarted) {
                    Log.d(MediaPlayer.TAG, "mDefaultPlayerStarted is true ,start !");
                    this.this$0.start();
                    this.this$0.mDefaultPlayerStarted = false;
                }
            }
        }
    }

    /* renamed from: android.media.MediaPlayer$3 */
    class AnonymousClass3 implements com.oppo.media.OppoMediaPlayer.OnBufferingUpdateListener {
        final /* synthetic */ MediaPlayer this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.3.<init>(android.media.MediaPlayer):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass3(android.media.MediaPlayer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.3.<init>(android.media.MediaPlayer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.3.<init>(android.media.MediaPlayer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.3.onBufferingUpdate(com.oppo.media.OppoMediaPlayer, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onBufferingUpdate(com.oppo.media.OppoMediaPlayer r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.3.onBufferingUpdate(com.oppo.media.OppoMediaPlayer, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.3.onBufferingUpdate(com.oppo.media.OppoMediaPlayer, int):void");
        }
    }

    /* renamed from: android.media.MediaPlayer$4 */
    class AnonymousClass4 implements com.oppo.media.OppoMediaPlayer.OnCompletionListener {
        final /* synthetic */ MediaPlayer this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.4.<init>(android.media.MediaPlayer):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass4(android.media.MediaPlayer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.4.<init>(android.media.MediaPlayer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.4.<init>(android.media.MediaPlayer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.4.onCompletion(com.oppo.media.OppoMediaPlayer):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onCompletion(com.oppo.media.OppoMediaPlayer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.4.onCompletion(com.oppo.media.OppoMediaPlayer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.4.onCompletion(com.oppo.media.OppoMediaPlayer):void");
        }
    }

    /* renamed from: android.media.MediaPlayer$5 */
    class AnonymousClass5 implements com.oppo.media.OppoMediaPlayer.OnErrorListener {
        final /* synthetic */ MediaPlayer this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.5.<init>(android.media.MediaPlayer):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass5(android.media.MediaPlayer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.5.<init>(android.media.MediaPlayer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.5.<init>(android.media.MediaPlayer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.5.onError(com.oppo.media.OppoMediaPlayer, int, int):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean onError(com.oppo.media.OppoMediaPlayer r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.5.onError(com.oppo.media.OppoMediaPlayer, int, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.5.onError(com.oppo.media.OppoMediaPlayer, int, int):boolean");
        }
    }

    /* renamed from: android.media.MediaPlayer$6 */
    class AnonymousClass6 implements com.oppo.media.OppoMediaPlayer.OnSeekCompleteListener {
        final /* synthetic */ MediaPlayer this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.6.<init>(android.media.MediaPlayer):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass6(android.media.MediaPlayer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.6.<init>(android.media.MediaPlayer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.6.<init>(android.media.MediaPlayer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.media.MediaPlayer.6.onSeekComplete(com.oppo.media.OppoMediaPlayer):void, dex:  in method: android.media.MediaPlayer.6.onSeekComplete(com.oppo.media.OppoMediaPlayer):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.media.MediaPlayer.6.onSeekComplete(com.oppo.media.OppoMediaPlayer):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:72)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public void onSeekComplete(com.oppo.media.OppoMediaPlayer r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.media.MediaPlayer.6.onSeekComplete(com.oppo.media.OppoMediaPlayer):void, dex:  in method: android.media.MediaPlayer.6.onSeekComplete(com.oppo.media.OppoMediaPlayer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.6.onSeekComplete(com.oppo.media.OppoMediaPlayer):void");
        }
    }

    /* renamed from: android.media.MediaPlayer$7 */
    class AnonymousClass7 implements com.oppo.media.OppoMediaPlayer.OnVideoSizeChangedListener {
        final /* synthetic */ MediaPlayer this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.7.<init>(android.media.MediaPlayer):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass7(android.media.MediaPlayer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.7.<init>(android.media.MediaPlayer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.7.<init>(android.media.MediaPlayer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.7.onVideoSizeChanged(com.oppo.media.OppoMediaPlayer, int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onVideoSizeChanged(com.oppo.media.OppoMediaPlayer r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.7.onVideoSizeChanged(com.oppo.media.OppoMediaPlayer, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.7.onVideoSizeChanged(com.oppo.media.OppoMediaPlayer, int, int):void");
        }
    }

    /* renamed from: android.media.MediaPlayer$8 */
    class AnonymousClass8 implements com.oppo.media.OppoMediaPlayer.OnInfoListener {
        final /* synthetic */ MediaPlayer this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.8.<init>(android.media.MediaPlayer):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass8(android.media.MediaPlayer r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.8.<init>(android.media.MediaPlayer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.8.<init>(android.media.MediaPlayer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.8.onInfo(com.oppo.media.OppoMediaPlayer, int, int):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean onInfo(com.oppo.media.OppoMediaPlayer r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.8.onInfo(com.oppo.media.OppoMediaPlayer, int, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.8.onInfo(com.oppo.media.OppoMediaPlayer, int, int):boolean");
        }
    }

    /* renamed from: android.media.MediaPlayer$9 */
    class AnonymousClass9 implements Runnable {
        final /* synthetic */ MediaPlayer this$0;
        final /* synthetic */ HandlerThread val$thread;

        /* renamed from: android.media.MediaPlayer$9$1 */
        class AnonymousClass1 implements Anchor {
            final /* synthetic */ AnonymousClass9 this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.9.1.<init>(android.media.MediaPlayer$9):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            AnonymousClass1(android.media.MediaPlayer.AnonymousClass9 r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.9.1.<init>(android.media.MediaPlayer$9):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.9.1.<init>(android.media.MediaPlayer$9):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.MediaPlayer.9.1.setSubtitleWidget(android.media.SubtitleTrack$RenderingWidget):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void setSubtitleWidget(android.media.SubtitleTrack.RenderingWidget r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.MediaPlayer.9.1.setSubtitleWidget(android.media.SubtitleTrack$RenderingWidget):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.9.1.setSubtitleWidget(android.media.SubtitleTrack$RenderingWidget):void");
            }

            public Looper getSubtitleLooper() {
                return Looper.getMainLooper();
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.9.<init>(android.media.MediaPlayer, android.os.HandlerThread):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass9(android.media.MediaPlayer r1, android.os.HandlerThread r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MediaPlayer.9.<init>(android.media.MediaPlayer, android.os.HandlerThread):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.9.<init>(android.media.MediaPlayer, android.os.HandlerThread):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.9.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MediaPlayer.9.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.9.run():void");
        }
    }

    private class EventHandler extends Handler {
        private MediaPlayer mMediaPlayer;
        final /* synthetic */ MediaPlayer this$0;

        public EventHandler(MediaPlayer this$0, MediaPlayer mp, Looper looper) {
            this.this$0 = this$0;
            super(looper);
            this.mMediaPlayer = mp;
        }

        /* JADX WARNING: Missing block: B:24:0x00fd, code:
            return;
     */
        /* JADX WARNING: Missing block: B:39:0x015e, code:
            r24 = android.media.MediaPlayer.-get21(r30.this$0);
     */
        /* JADX WARNING: Missing block: B:40:0x0168, code:
            if (r24 == null) goto L_0x0177;
     */
        /* JADX WARNING: Missing block: B:41:0x016a, code:
            r24.onSeekComplete(r30.mMediaPlayer);
     */
        /* JADX WARNING: Missing block: B:42:0x0177, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            if (this.mMediaPlayer.mNativeContext == 0) {
                Log.w(MediaPlayer.TAG, "mediaplayer went away with unhandled events");
                return;
            }
            Log.d(MediaPlayer.TAG, "handleMessage msg:(" + msg.what + ", " + msg.arg1 + ", " + msg.arg2 + ")");
            OnCompletionListener onCompletionListener;
            TimeProvider timeProvider;
            Parcel parcel;
            switch (msg.what) {
                case 0:
                    break;
                case 1:
                    try {
                        this.this$0.scanInternalSubtitleTracks();
                    } catch (RuntimeException e) {
                        sendMessage(obtainMessage(100, 1, MediaPlayer.MEDIA_ERROR_UNSUPPORTED, null));
                    }
                    OnPreparedListener onPreparedListener = this.this$0.mOnPreparedListener;
                    if (onPreparedListener != null) {
                        onPreparedListener.onPrepared(this.mMediaPlayer);
                    }
                    return;
                case 2:
                    onCompletionListener = this.this$0.mOnCompletionListener;
                    if (onCompletionListener != null) {
                        onCompletionListener.onCompletion(this.mMediaPlayer);
                    }
                    this.this$0.stayAwake(false);
                    return;
                case 3:
                    OnBufferingUpdateListener onBufferingUpdateListener = this.this$0.mOnBufferingUpdateListener;
                    if (onBufferingUpdateListener != null) {
                        onBufferingUpdateListener.onBufferingUpdate(this.mMediaPlayer, msg.arg1);
                    }
                    return;
                case 4:
                    OnSeekCompleteListener onSeekCompleteListener = this.this$0.mOnSeekCompleteListener;
                    if (onSeekCompleteListener != null) {
                        onSeekCompleteListener.onSeekComplete(this.mMediaPlayer);
                        break;
                    }
                    break;
                case 5:
                    OnVideoSizeChangedListener onVideoSizeChangedListener = this.this$0.mOnVideoSizeChangedListener;
                    if (onVideoSizeChangedListener != null) {
                        onVideoSizeChangedListener.onVideoSizeChanged(this.mMediaPlayer, msg.arg1, msg.arg2);
                    }
                    return;
                case 6:
                case 7:
                    timeProvider = this.this$0.mTimeProvider;
                    if (timeProvider != null) {
                        timeProvider.onPaused(msg.what == 7);
                        break;
                    }
                    break;
                case 8:
                    timeProvider = this.this$0.mTimeProvider;
                    if (timeProvider != null) {
                        timeProvider.onStopped();
                        break;
                    }
                    break;
                case 9:
                    break;
                case 99:
                    OnTimedTextListener onTimedTextListener = this.this$0.mOnTimedTextListener;
                    if (onTimedTextListener != null) {
                        if (msg.obj == null) {
                            onTimedTextListener.onTimedText(this.mMediaPlayer, null);
                        } else if (msg.obj instanceof Parcel) {
                            parcel = msg.obj;
                            TimedText timedText = new TimedText(parcel);
                            parcel.recycle();
                            onTimedTextListener.onTimedText(this.mMediaPlayer, timedText);
                        }
                        return;
                    }
                    return;
                case 100:
                    Log.e(MediaPlayer.TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
                    boolean error_was_handled;
                    OnErrorListener onErrorListener;
                    if (msg.arg1 == 100 || msg.arg1 == -38 || msg.arg2 == -19 || msg.arg2 == -38 || this.this$0.mCurrentState == 64 || this.this$0.mCurrentState == 261 || this.this$0.mNotUsingOppoMedia) {
                        error_was_handled = false;
                        onErrorListener = this.this$0.mOnErrorListener;
                        if (onErrorListener != null) {
                            error_was_handled = onErrorListener.onError(this.mMediaPlayer, msg.arg1, msg.arg2);
                        }
                        onCompletionListener = this.this$0.mOnCompletionListener;
                        if (!(onCompletionListener == null || error_was_handled)) {
                            onCompletionListener.onCompletion(this.mMediaPlayer);
                        }
                        this.this$0.stayAwake(false);
                    } else if (!this.this$0.isOppoCreate) {
                        if (this.this$0.mCtsCallingPackage) {
                            Log.i(MediaPlayer.TAG, "cts test not handle error mCurrentState=" + this.this$0.mCurrentState);
                            error_was_handled = false;
                            onErrorListener = this.this$0.mOnErrorListener;
                            if (onErrorListener != null) {
                                error_was_handled = onErrorListener.onError(this.mMediaPlayer, msg.arg1, msg.arg2);
                            }
                            onCompletionListener = this.this$0.mOnCompletionListener;
                            if (!(onCompletionListener == null || error_was_handled)) {
                                onCompletionListener.onCompletion(this.mMediaPlayer);
                            }
                            this.this$0.stayAwake(false);
                            return;
                        } else if (!this.this$0.handleMediaPlayerError()) {
                            boolean was_handled = false;
                            onErrorListener = this.this$0.mOnErrorListener;
                            if (onErrorListener != null) {
                                was_handled = onErrorListener.onError(this.mMediaPlayer, msg.arg1, msg.arg2);
                            }
                            onCompletionListener = this.this$0.mOnCompletionListener;
                            if (!(onCompletionListener == null || was_handled)) {
                                onCompletionListener.onCompletion(this.mMediaPlayer);
                            }
                            this.this$0.stayAwake(false);
                            return;
                        }
                    }
                    return;
                case 200:
                    switch (msg.arg1) {
                        case 700:
                            Log.i(MediaPlayer.TAG, "Info (" + msg.arg1 + "," + msg.arg2 + ")");
                            break;
                        case 701:
                        case 702:
                            timeProvider = this.this$0.mTimeProvider;
                            if (timeProvider != null) {
                                timeProvider.onBuffering(msg.arg1 == 701);
                                break;
                            }
                            break;
                        case 802:
                            try {
                                this.this$0.scanInternalSubtitleTracks();
                                break;
                            } catch (RuntimeException e2) {
                                sendMessage(obtainMessage(100, 1, MediaPlayer.MEDIA_ERROR_UNSUPPORTED, null));
                                break;
                            }
                        case 803:
                            break;
                    }
                    msg.arg1 = 802;
                    if (this.this$0.mSubtitleController != null) {
                        this.this$0.mSubtitleController.selectDefaultTrack();
                    }
                    OnInfoListener onInfoListener = this.this$0.mOnInfoListener;
                    if (onInfoListener != null) {
                        onInfoListener.onInfo(this.mMediaPlayer, msg.arg1, msg.arg2);
                    }
                    return;
                case 201:
                    OnSubtitleDataListener onSubtitleDataListener = this.this$0.mOnSubtitleDataListener;
                    if (onSubtitleDataListener != null && (msg.obj instanceof Parcel)) {
                        parcel = (Parcel) msg.obj;
                        SubtitleData data = new SubtitleData(parcel);
                        parcel.recycle();
                        onSubtitleDataListener.onSubtitleData(this.mMediaPlayer, data);
                    }
                    return;
                case 202:
                    OnTimedMetaDataAvailableListener onTimedMetaDataAvailableListener = this.this$0.mOnTimedMetaDataAvailableListener;
                    if (onTimedMetaDataAvailableListener != null && (msg.obj instanceof Parcel)) {
                        parcel = (Parcel) msg.obj;
                        TimedMetaData data2 = TimedMetaData.createTimedMetaDataFromParcel(parcel);
                        parcel.recycle();
                        onTimedMetaDataAvailableListener.onTimedMetaDataAvailable(this.mMediaPlayer, data2);
                    }
                    return;
                case 300:
                    Log.v(MediaPlayer.TAG, "Duration update (duration=" + msg.arg1 + ")");
                    if (this.this$0.mOnDurationUpdateListener != null) {
                        this.this$0.mOnDurationUpdateListener.onDurationUpdate(this.mMediaPlayer, msg.arg1);
                        break;
                    }
                    break;
                case 600:
                    if (this.this$0.mOnInfoListener != null) {
                        if (msg.arg1 != 0) {
                            Log.e(MediaPlayer.TAG, "MEDIA_PAUSE_COMPLETE failed " + msg.arg1);
                        }
                        this.this$0.mOnInfoListener.onInfo(this.mMediaPlayer, MediaPlayer.MEDIA_INFO_PAUSE_COMPLETED, msg.arg1);
                        break;
                    }
                    break;
                case 601:
                    if (this.this$0.mOnInfoListener != null) {
                        if (msg.arg1 != 0) {
                            Log.e(MediaPlayer.TAG, "MEDIA_PLAY_COMPLETE failed " + msg.arg1);
                        }
                        this.this$0.mOnInfoListener.onInfo(this.mMediaPlayer, MediaPlayer.MEDIA_INFO_PLAY_COMPLETED, msg.arg1);
                        break;
                    }
                    break;
                default:
                    Log.e(MediaPlayer.TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
    }

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(MediaPlayer mediaPlayer, int i);
    }

    public interface OnDurationUpdateListener {
        void onDurationUpdate(MediaPlayer mediaPlayer, int i);
    }

    public interface OnErrorListener {
        boolean onError(MediaPlayer mediaPlayer, int i, int i2);
    }

    public interface OnInfoListener {
        boolean onInfo(MediaPlayer mediaPlayer, int i, int i2);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(MediaPlayer mediaPlayer);
    }

    public interface OnTimedMetaDataAvailableListener {
        void onTimedMetaDataAvailable(MediaPlayer mediaPlayer, TimedMetaData timedMetaData);
    }

    public interface OnTimedTextListener {
        void onTimedText(MediaPlayer mediaPlayer, TimedText timedText);
    }

    static class TimeProvider implements OnSeekCompleteListener, MediaTimeProvider {
        private static final long MAX_EARLY_CALLBACK_US = 1000;
        private static final long MAX_NS_WITHOUT_POSITION_CHECK = 5000000000L;
        private static final int NOTIFY = 1;
        private static final int NOTIFY_SEEK = 3;
        private static final int NOTIFY_STOP = 2;
        private static final int NOTIFY_TIME = 0;
        private static final int NOTIFY_TRACK_DATA = 4;
        private static final int REFRESH_AND_NOTIFY_TIME = 1;
        private static final String TAG = "MTP";
        private static final long TIME_ADJUSTMENT_RATE = 2;
        public boolean DEBUG;
        private boolean mBuffering;
        private Handler mEventHandler;
        private HandlerThread mHandlerThread;
        private long mLastNanoTime;
        private long mLastReportedTime;
        private long mLastTimeUs;
        private OnMediaTimeListener[] mListeners;
        private boolean mPaused;
        private boolean mPausing;
        private MediaPlayer mPlayer;
        private boolean mRefresh;
        private boolean mSeeking;
        private boolean mStopped;
        private long mTimeAdjustment;
        private long[] mTimes;

        private class EventHandler extends Handler {
            final /* synthetic */ TimeProvider this$1;

            public EventHandler(TimeProvider this$1, Looper looper) {
                this.this$1 = this$1;
                super(looper);
            }

            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    switch (msg.arg1) {
                        case 0:
                            this.this$1.notifyTimedEvent(false);
                            return;
                        case 1:
                            this.this$1.notifyTimedEvent(true);
                            return;
                        case 2:
                            this.this$1.notifyStop();
                            return;
                        case 3:
                            this.this$1.notifySeek();
                            return;
                        case 4:
                            this.this$1.notifyTrackData((Pair) msg.obj);
                            return;
                        default:
                            return;
                    }
                }
            }
        }

        public TimeProvider(MediaPlayer mp) {
            this.mLastTimeUs = 0;
            this.mPaused = true;
            this.mStopped = true;
            this.mRefresh = false;
            this.mPausing = false;
            this.mSeeking = false;
            this.DEBUG = false;
            this.mPlayer = mp;
            try {
                getCurrentTimeUs(true, false);
            } catch (IllegalStateException e) {
                this.mRefresh = true;
            }
            Looper looper = Looper.myLooper();
            if (looper == null) {
                looper = Looper.getMainLooper();
                if (looper == null) {
                    this.mHandlerThread = new HandlerThread("MediaPlayerMTPEventThread", -2);
                    this.mHandlerThread.start();
                    looper = this.mHandlerThread.getLooper();
                }
            }
            this.mEventHandler = new EventHandler(this, looper);
            this.mListeners = new OnMediaTimeListener[0];
            this.mTimes = new long[0];
            this.mLastTimeUs = 0;
            this.mTimeAdjustment = 0;
        }

        private void scheduleNotification(int type, long delayUs) {
            if (!this.mSeeking || (type != 0 && type != 1)) {
                if (this.DEBUG) {
                    Log.v(TAG, "scheduleNotification " + type + " in " + delayUs);
                }
                this.mEventHandler.removeMessages(1);
                this.mEventHandler.sendMessageDelayed(this.mEventHandler.obtainMessage(1, type, 0), (long) ((int) (delayUs / MAX_EARLY_CALLBACK_US)));
            }
        }

        public void close() {
            this.mEventHandler.removeMessages(1);
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
                this.mHandlerThread = null;
            }
        }

        protected void finalize() {
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
            }
        }

        public void onPaused(boolean paused) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onPaused: " + paused);
                }
                if (this.mStopped) {
                    this.mStopped = false;
                    this.mSeeking = true;
                    scheduleNotification(3, 0);
                } else {
                    this.mPausing = paused;
                    this.mSeeking = false;
                    scheduleNotification(1, 0);
                }
            }
        }

        public void onBuffering(boolean buffering) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onBuffering: " + buffering);
                }
                this.mBuffering = buffering;
                scheduleNotification(1, 0);
            }
        }

        public void onStopped() {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onStopped");
                }
                this.mPaused = true;
                this.mStopped = true;
                this.mSeeking = false;
                this.mBuffering = false;
                scheduleNotification(2, 0);
            }
        }

        public void onSeekComplete(MediaPlayer mp) {
            synchronized (this) {
                this.mStopped = false;
                this.mSeeking = true;
                scheduleNotification(3, 0);
            }
        }

        public void onNewPlayer() {
            if (this.mRefresh) {
                synchronized (this) {
                    this.mStopped = false;
                    this.mSeeking = true;
                    this.mBuffering = false;
                    scheduleNotification(3, 0);
                }
            }
        }

        private synchronized void notifySeek() {
            synchronized (this) {
                this.mSeeking = false;
                try {
                    long timeUs = getCurrentTimeUs(true, false);
                    if (this.DEBUG) {
                        Log.d(TAG, "onSeekComplete at " + timeUs);
                    }
                    for (OnMediaTimeListener listener : this.mListeners) {
                        if (listener == null) {
                            break;
                        }
                        listener.onSeek(timeUs);
                    }
                } catch (IllegalStateException e) {
                    if (this.DEBUG) {
                        Log.d(TAG, "onSeekComplete but no player");
                    }
                    this.mPausing = true;
                    notifyTimedEvent(false);
                }
            }
        }

        private synchronized void notifyTrackData(Pair<SubtitleTrack, byte[]> trackData) {
            trackData.first.onData(trackData.second, true, -1);
        }

        private synchronized void notifyStop() {
            for (OnMediaTimeListener listener : this.mListeners) {
                if (listener == null) {
                    break;
                }
                listener.onStop();
            }
        }

        private int registerListener(OnMediaTimeListener listener) {
            int i = 0;
            while (i < this.mListeners.length && this.mListeners[i] != listener && this.mListeners[i] != null) {
                i++;
            }
            if (i >= this.mListeners.length) {
                OnMediaTimeListener[] newListeners = new OnMediaTimeListener[(i + 1)];
                long[] newTimes = new long[(i + 1)];
                System.arraycopy(this.mListeners, 0, newListeners, 0, this.mListeners.length);
                System.arraycopy(this.mTimes, 0, newTimes, 0, this.mTimes.length);
                this.mListeners = newListeners;
                this.mTimes = newTimes;
            }
            if (this.mListeners[i] == null) {
                this.mListeners[i] = listener;
                this.mTimes[i] = -1;
            }
            return i;
        }

        public void notifyAt(long timeUs, OnMediaTimeListener listener) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "notifyAt " + timeUs);
                }
                this.mTimes[registerListener(listener)] = timeUs;
                scheduleNotification(0, 0);
            }
        }

        public void scheduleUpdate(OnMediaTimeListener listener) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "scheduleUpdate");
                }
                int i = registerListener(listener);
                if (!this.mStopped) {
                    this.mTimes[i] = 0;
                    scheduleNotification(0, 0);
                }
            }
        }

        public void cancelNotifications(OnMediaTimeListener listener) {
            synchronized (this) {
                int i = 0;
                while (i < this.mListeners.length) {
                    if (this.mListeners[i] != listener) {
                        if (this.mListeners[i] == null) {
                            break;
                        }
                        i++;
                    } else {
                        System.arraycopy(this.mListeners, i + 1, this.mListeners, i, (this.mListeners.length - i) - 1);
                        System.arraycopy(this.mTimes, i + 1, this.mTimes, i, (this.mTimes.length - i) - 1);
                        this.mListeners[this.mListeners.length - 1] = null;
                        this.mTimes[this.mTimes.length - 1] = -1;
                        break;
                    }
                }
                scheduleNotification(0, 0);
            }
        }

        private synchronized void notifyTimedEvent(boolean refreshTime) {
            long nowUs;
            try {
                nowUs = getCurrentTimeUs(refreshTime, true);
            } catch (IllegalStateException e) {
                this.mRefresh = true;
                this.mPausing = true;
                nowUs = getCurrentTimeUs(refreshTime, true);
            }
            long nextTimeUs = nowUs;
            if (!this.mSeeking) {
                if (this.DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("notifyTimedEvent(").append(this.mLastTimeUs).append(" -> ").append(nowUs).append(") from {");
                    boolean first = true;
                    for (long time : this.mTimes) {
                        if (time != -1) {
                            if (!first) {
                                sb.append(", ");
                            }
                            sb.append(time);
                            first = false;
                        }
                    }
                    sb.append("}");
                    Log.d(TAG, sb.toString());
                }
                Vector<OnMediaTimeListener> activatedListeners = new Vector();
                int ix = 0;
                while (ix < this.mTimes.length && this.mListeners[ix] != null) {
                    if (this.mTimes[ix] > -1) {
                        if (this.mTimes[ix] <= MAX_EARLY_CALLBACK_US + nowUs) {
                            activatedListeners.add(this.mListeners[ix]);
                            if (this.DEBUG) {
                                Log.d(TAG, Environment.MEDIA_REMOVED);
                            }
                            this.mTimes[ix] = -1;
                        } else if (nextTimeUs == nowUs || this.mTimes[ix] < nextTimeUs) {
                            nextTimeUs = this.mTimes[ix];
                        }
                    }
                    ix++;
                }
                if (nextTimeUs <= nowUs || this.mPaused) {
                    this.mEventHandler.removeMessages(1);
                } else {
                    if (this.DEBUG) {
                        Log.d(TAG, "scheduling for " + nextTimeUs + " and " + nowUs);
                    }
                    scheduleNotification(0, nextTimeUs - nowUs);
                }
                for (OnMediaTimeListener listener : activatedListeners) {
                    listener.onTimedEvent(nowUs);
                }
            }
        }

        private long getEstimatedTime(long nanoTime, boolean monotonic) {
            if (this.mPaused) {
                this.mLastReportedTime = this.mLastTimeUs + this.mTimeAdjustment;
            } else {
                long timeSinceRead = (nanoTime - this.mLastNanoTime) / MAX_EARLY_CALLBACK_US;
                this.mLastReportedTime = this.mLastTimeUs + timeSinceRead;
                if (this.mTimeAdjustment > 0) {
                    long adjustment = this.mTimeAdjustment - (timeSinceRead / 2);
                    if (adjustment <= 0) {
                        this.mTimeAdjustment = 0;
                    } else {
                        this.mLastReportedTime += adjustment;
                    }
                }
            }
            return this.mLastReportedTime;
        }

        public long getCurrentTimeUs(boolean refreshTime, boolean monotonic) throws IllegalStateException {
            boolean z = true;
            synchronized (this) {
                long estimatedTime;
                if (!this.mPaused || refreshTime) {
                    long nanoTime = System.nanoTime();
                    if (refreshTime || nanoTime >= this.mLastNanoTime + MAX_NS_WITHOUT_POSITION_CHECK) {
                        try {
                            this.mLastTimeUs = ((long) this.mPlayer.getCurrentPosition()) * MAX_EARLY_CALLBACK_US;
                            if (this.mPlayer.isPlaying()) {
                                z = this.mBuffering;
                            }
                            this.mPaused = z;
                            if (this.DEBUG) {
                                String str;
                                String str2 = TAG;
                                StringBuilder stringBuilder = new StringBuilder();
                                if (this.mPaused) {
                                    str = "paused";
                                } else {
                                    str = "playing";
                                }
                                Log.v(str2, stringBuilder.append(str).append(" at ").append(this.mLastTimeUs).toString());
                            }
                            this.mLastNanoTime = nanoTime;
                            if (!monotonic || this.mLastTimeUs >= this.mLastReportedTime) {
                                this.mTimeAdjustment = 0;
                            } else {
                                this.mTimeAdjustment = this.mLastReportedTime - this.mLastTimeUs;
                                if (this.mTimeAdjustment > 1000000) {
                                    this.mStopped = false;
                                    this.mSeeking = true;
                                    scheduleNotification(3, 0);
                                }
                            }
                        } catch (IllegalStateException e) {
                            if (this.mPausing) {
                                this.mPausing = false;
                                getEstimatedTime(nanoTime, monotonic);
                                this.mPaused = true;
                                if (this.DEBUG) {
                                    Log.d(TAG, "illegal state, but pausing: estimating at " + this.mLastReportedTime);
                                }
                                return this.mLastReportedTime;
                            }
                            throw e;
                        }
                    }
                    estimatedTime = getEstimatedTime(nanoTime, monotonic);
                    return estimatedTime;
                }
                estimatedTime = this.mLastReportedTime;
                return estimatedTime;
            }
        }
    }

    public static class TrackInfo implements Parcelable {
        static final Creator<TrackInfo> CREATOR = null;
        public static final int MEDIA_TRACK_TYPE_AUDIO = 2;
        public static final int MEDIA_TRACK_TYPE_METADATA = 5;
        public static final int MEDIA_TRACK_TYPE_SUBTITLE = 4;
        public static final int MEDIA_TRACK_TYPE_TIMEDTEXT = 3;
        public static final int MEDIA_TRACK_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TRACK_TYPE_VIDEO = 1;
        final MediaFormat mFormat;
        final int mTrackType;

        /* renamed from: android.media.MediaPlayer$TrackInfo$1 */
        static class AnonymousClass1 implements Creator<TrackInfo> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public TrackInfo createFromParcel(Parcel in) {
                return new TrackInfo(in);
            }

            public /* bridge */ /* synthetic */ Object[] newArray(int size) {
                return newArray(size);
            }

            public TrackInfo[] newArray(int size) {
                return new TrackInfo[size];
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.MediaPlayer.TrackInfo.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.MediaPlayer.TrackInfo.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.TrackInfo.<clinit>():void");
        }

        public int getTrackType() {
            return this.mTrackType;
        }

        public String getLanguage() {
            String language = this.mFormat.getString("language");
            return language == null ? "und" : language;
        }

        public MediaFormat getFormat() {
            if (this.mTrackType == 3 || this.mTrackType == 4) {
                return this.mFormat;
            }
            return null;
        }

        TrackInfo(Parcel in) {
            this.mTrackType = in.readInt();
            this.mFormat = MediaFormat.createSubtitleFormat(in.readString(), in.readString());
            if (this.mTrackType == 4) {
                this.mFormat.setInteger(MediaFormat.KEY_IS_AUTOSELECT, in.readInt());
                this.mFormat.setInteger(MediaFormat.KEY_IS_DEFAULT, in.readInt());
                this.mFormat.setInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE, in.readInt());
            }
        }

        TrackInfo(int type, MediaFormat format) {
            this.mTrackType = type;
            this.mFormat = format;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mTrackType);
            dest.writeString(getLanguage());
            if (this.mTrackType == 4) {
                dest.writeString(this.mFormat.getString(MediaFormat.KEY_MIME));
                dest.writeInt(this.mFormat.getInteger(MediaFormat.KEY_IS_AUTOSELECT));
                dest.writeInt(this.mFormat.getInteger(MediaFormat.KEY_IS_DEFAULT));
                dest.writeInt(this.mFormat.getInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE));
            }
        }

        public String toString() {
            StringBuilder out = new StringBuilder(128);
            out.append(getClass().getName());
            out.append('{');
            switch (this.mTrackType) {
                case 1:
                    out.append("VIDEO");
                    break;
                case 2:
                    out.append("AUDIO");
                    break;
                case 3:
                    out.append("TIMEDTEXT");
                    break;
                case 4:
                    out.append("SUBTITLE");
                    break;
                default:
                    out.append("UNKNOWN");
                    break;
            }
            out.append(", ").append(this.mFormat.toString());
            out.append("}");
            return out.toString();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.MediaPlayer.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.MediaPlayer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.<clinit>():void");
    }

    private native int _getAudioStreamType() throws IllegalStateException;

    private native void _pause() throws IllegalStateException;

    private native void _prepare() throws IOException, IllegalStateException;

    private native void _release();

    private native void _reset();

    private native void _setAudioStreamType(int i);

    private native void _setAuxEffectSendLevel(float f);

    private native void _setDataSource(MediaDataSource mediaDataSource) throws IllegalArgumentException, IllegalStateException;

    private native void _setDataSource(FileDescriptor fileDescriptor, long j, long j2) throws IOException, IllegalArgumentException, IllegalStateException;

    private native void _setVideoSurface(Surface surface);

    private native void _setVolume(float f, float f2);

    private native void _start() throws IllegalStateException;

    private native void _stop() throws IllegalStateException;

    private native void getParameter(int i, Parcel parcel);

    private native void nativeSetDataSource(IBinder iBinder, String str, String[] strArr, String[] strArr2) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    private final native void native_finalize();

    private final native boolean native_getMetadata(boolean z, boolean z2, Parcel parcel);

    private static final native void native_init();

    private final native int native_invoke(Parcel parcel, Parcel parcel2);

    public static native int native_pullBatteryData(Parcel parcel);

    private final native int native_setMetadataFilter(Parcel parcel);

    private final native int native_setRetransmitEndpoint(String str, int i);

    private final native void native_setup(Object obj);

    private native boolean setParameter(int i, Parcel parcel);

    public native void _attachAuxEffect(int i);

    public native int _getAudioSessionId();

    public native int _getCurrentPosition();

    public native int _getDuration();

    public native int _getVideoHeight();

    public native int _getVideoWidth();

    public native boolean _isLooping();

    public native boolean _isPlaying();

    public native void _prepareAsync() throws IllegalStateException;

    public native void _seekTo(int i) throws IllegalStateException;

    public native void _setAudioSessionId(int i) throws IllegalArgumentException, IllegalStateException;

    public native void _setLooping(boolean z);

    public native PlaybackParams getPlaybackParams();

    public native SyncParams getSyncParams();

    public native void setNextMediaPlayer(MediaPlayer mediaPlayer);

    public native void setPlaybackParams(PlaybackParams playbackParams);

    public native void setSyncParams(SyncParams syncParams);

    public MediaPlayer() {
        super(new Builder().build());
        this.mWakeLock = null;
        this.mStreamType = Integer.MIN_VALUE;
        this.mUsage = -1;
        this.isOppoCreate = false;
        this.mPrepareAsync = true;
        this.mNeedSeeking = false;
        this.isCreateOppoMediaPlayer = false;
        this.mUri = null;
        this.mPath = null;
        this.mContext = null;
        this.mFd = null;
        this.mOffset = -1;
        this.mLength = -1;
        this.mHeaders = null;
        this.mOppoMediaPlayer = null;
        this.mSurface = null;
        this.mSeekMs = -1;
        this.mAudioSessionId = -1;
        this.mAudioStreamType = -1;
        this.mScreenOn = false;
        this.mNotUsingOppoMedia = false;
        this.mDebugLog = false;
        this.mCtsCallingPackage = false;
        this.mDefaultPlayerStarted = false;
        this.mNeedMute = false;
        this.mInterceptFlag = false;
        this.mRecoverFlag = false;
        this.mIndexTrackPairs = new Vector();
        this.mInbandTrackIndices = new BitSet();
        this.mSelectedSubtitleTrackIndex = -1;
        this.mSubtitleDataListener = new AnonymousClass1(this);
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, this, looper);
        } else {
            looper = Looper.getMainLooper();
            if (looper != null) {
                this.mEventHandler = new EventHandler(this, this, looper);
            } else {
                this.mEventHandler = null;
            }
        }
        this.mCurrentState = 1;
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            this.mDebugLog = true;
        }
        String packageName = ActivityThread.currentPackageName();
        if (packageName != null && packageName.length() > 0) {
            Log.i(TAG, "new mediaplayer ,packageName = " + packageName);
            if (packageName.contains(CTS_TEST_PACKAGE)) {
                this.mCtsCallingPackage = true;
                Log.i(TAG, "Cts test start ");
            }
            if (packageName.equals(GOOGLE_MUSIC_PACKAGE)) {
                this.mCtsCallingPackage = true;
                Log.i(TAG, "Google Music don't use oppo player");
            }
            String result = OppoMultimediaManager.getInstance(null).getParameters("check_daemon_listinfo_byname=" + Integer.toString(ModuleTag.DISABLE_OPPOMEDIA.ordinal()) + "=" + packageName);
            if (result != null && result.equals("true")) {
                this.mCtsCallingPackage = true;
                Log.i(TAG, packageName + " don't use oppo player");
            }
        }
        if (SystemProperties.getBoolean("remove.oppomedia.support", false)) {
            this.mCtsCallingPackage = true;
            Log.i(TAG, "remove.oppomedia.support is true!");
        }
        this.mTimeProvider = new TimeProvider(this);
        this.mOpenSubtitleSources = new Vector();
        this.mNotificationManager = Stub.asInterface(ServiceManager.getService("notification"));
        native_setup(new WeakReference(this));
    }

    private void closeFd() {
        try {
            Log.d(TAG, "closeFd()");
            if (this.mFd != null && this.mFd.valid()) {
                Log.d(TAG, "mFd is valid, close it.");
                Libcore.os.close(this.mFd);
                this.mFd = null;
                this.mOffset = -1;
                this.mLength = -1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception = " + e);
            e.printStackTrace();
        }
    }

    private boolean handleMediaPlayerError() {
        if (this.mDebugLog) {
            Log.i(TAG, "handleMediaPlayerError() mCurrentState=" + this.mCurrentState);
        }
        int mChangeState = this.mCurrentState;
        this.mCurrentState = 1;
        if (this.mNotUsingOppoMedia) {
            Log.i(TAG, "handleMediaPlayerError() mNotUsingOppoMedia is true");
            return false;
        }
        createOppoMediaPlayer();
        if (mChangeState >= 2) {
            try {
                if (this.mPath != null) {
                    Log.d(TAG, "handleMediaPlayerError() mOppoMediaPlayer setDataSource(String)");
                    this.mOppoMediaPlayer.setDataSource(this.mPath);
                } else if (this.mFd == null || !this.mFd.valid()) {
                    if (this.mUri != null) {
                        Log.d(TAG, "handleMediaPlayerError() mOppoMediaPlayer setDataSource(Context, Uri, Map)");
                        this.mOppoMediaPlayer.setDataSource(this.mContext, this.mUri, this.mHeaders);
                    } else {
                        Log.e(TAG, "handleMediaPlayerError() mOppoMediaPlayer setDataSource null");
                        closeFd();
                        return false;
                    }
                } else if (this.mOffset > 0) {
                    Log.d(TAG, "handleMediaPlayerError() mOppoMediaPlayer setDataSource(fd, offset,length) mOffset = " + this.mOffset + " mLength = " + this.mLength);
                    this.mOppoMediaPlayer.setDataSource(this.mFd, this.mOffset, this.mLength);
                } else {
                    Log.d(TAG, "handleMediaPlayerError() mOppoMediaPlayer setDataSource(fd)");
                    this.mOppoMediaPlayer.setDataSource(this.mFd);
                }
                closeFd();
                if (this.mSurfaceHolder != null) {
                    setDisplay(this.mSurfaceHolder);
                }
                if (this.mSurface != null) {
                    this.mOppoMediaPlayer.setSurface(this.mSurface);
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
                    prepareAsync();
                } catch (IllegalStateException e) {
                    Log.e(TAG, "handleMediaPlayerError() prepareAsync error!");
                    e.printStackTrace();
                    return false;
                }
            }
            try {
                prepare();
                this.mCurrentState = 8;
            } catch (IOException e2) {
                Log.e(TAG, "handleMediaPlayerError() prepare error!");
                e2.printStackTrace();
                return false;
            }
        }
        if (this.mSeekMs >= 0) {
            this.mNeedSeeking = true;
        }
        if (mChangeState >= 16 && mChangeState != 32) {
            Log.v(TAG, "handleMediaPlayerError() mOppoMediaPlayer start mPrepareAsync=" + this.mPrepareAsync);
            if (!this.mPrepareAsync) {
                start();
            }
        }
        return true;
    }

    private void createOppoMediaPlayer() {
        if (!this.mNotUsingOppoMedia) {
            int tempSeekMs = this.mSeekMs;
            boolean tempScreenOn = this.mScreenOn;
            this.isCreateOppoMediaPlayer = true;
            reset();
            this.mSeekMs = tempSeekMs;
            this.mScreenOn = tempScreenOn;
            if (this.mOppoMediaPlayer != null) {
                this.mOppoMediaPlayer.reset();
                this.isOppoCreate = true;
                Log.i(TAG, "mOppoMediaPlayer has exist, return.");
                return;
            }
            this.mOppoMediaPlayer = new OppoMediaPlayer();
            this.isOppoCreate = true;
            if (this.mAudioSessionId >= 0) {
                this.mOppoMediaPlayer.setAudioSessionId(this.mAudioSessionId);
            }
            if (this.mAudioStreamType >= 0) {
                this.mOppoMediaPlayer.setAudioStreamType(this.mAudioStreamType);
            }
            if (this.mScreenOn) {
                this.mOppoMediaPlayer.setScreenOnWhilePlaying(true);
            }
            this.mOppoMediaPlayer.setOnPreparedListener(new AnonymousClass2(this));
            this.mOppoMediaPlayer.setOnBufferingUpdateListener(new AnonymousClass3(this));
            this.mOppoMediaPlayer.setOnCompletionListener(new AnonymousClass4(this));
            this.mOppoMediaPlayer.setOnErrorListener(new AnonymousClass5(this));
            this.mOppoMediaPlayer.setOnSeekCompleteListener(new AnonymousClass6(this));
            this.mOppoMediaPlayer.setOnVideoSizeChangedListener(new AnonymousClass7(this));
            this.mOppoMediaPlayer.setOnInfoListener(new AnonymousClass8(this));
        }
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
        if (this.mDebugLog) {
            Log.i(TAG, "setDisplay() isOppoCreate=" + this.isOppoCreate);
        }
        this.mSurfaceHolder = sh;
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.setDisplay(sh);
            return;
        }
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
        if (this.mDebugLog) {
            Log.i(TAG, "setDisplay() isOppoCreate=" + this.isOppoCreate);
        }
        this.mSurface = surface;
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.setSurface(surface);
            return;
        }
        if (this.mScreenOnWhilePlaying && surface != null) {
            Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective for Surface");
        }
        this.mSurfaceHolder = null;
        _setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    public void setVideoScalingMode(int mode) {
        if (this.mDebugLog) {
            Log.i(TAG, "setDisplay() isOppoCreate=" + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.setVideoScalingMode(mode);
        } else if (isVideoScalingModeSupported(mode)) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                request.writeInterfaceToken(IMEDIA_PLAYER);
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

    public static MediaPlayer create(Context context, Uri uri) {
        return create(context, uri, null);
    }

    public static MediaPlayer create(Context context, Uri uri, SurfaceHolder holder) {
        int s = AudioSystem.newAudioSessionId();
        if (s <= 0) {
            s = 0;
        }
        return create(context, uri, holder, null, s);
    }

    public static MediaPlayer create(Context context, Uri uri, SurfaceHolder holder, AudioAttributes audioAttributes, int audioSessionId) {
        try {
            AudioAttributes aa;
            MediaPlayer mp = new MediaPlayer();
            if (audioAttributes != null) {
                aa = audioAttributes;
            } else {
                aa = new Builder().build();
            }
            mp.setAudioAttributes(aa);
            mp.setAudioSessionId(audioSessionId);
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

    public static MediaPlayer create(Context context, int resid) {
        int s = AudioSystem.newAudioSessionId();
        if (s <= 0) {
            s = 0;
        }
        return create(context, resid, null, s);
    }

    public static MediaPlayer create(Context context, int resid, AudioAttributes audioAttributes, int audioSessionId) {
        try {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resid);
            if (afd == null) {
                return null;
            }
            AudioAttributes aa;
            MediaPlayer mp = new MediaPlayer();
            if (audioAttributes != null) {
                aa = audioAttributes;
            } else {
                aa = new Builder().build();
            }
            mp.setAudioAttributes(aa);
            mp.setAudioSessionId(audioSessionId);
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
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(Context, Uri) isOppoCreate=" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = false;
        this.mDefaultPlayerStarted = false;
        this.mContext = context;
        this.mUri = uri;
        this.mCurrentState = 2;
        setDataSource(context, uri, null);
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(Context, Uri, headers) isOppoCreate=" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = false;
        this.mDefaultPlayerStarted = false;
        this.mContext = context;
        this.mUri = uri;
        this.mHeaders = headers;
        this.mCurrentState = 2;
        ContentResolver resolver = context.getContentResolver();
        String scheme = uri.getScheme();
        if (scheme == null) {
            setDataSource(uri.toString());
        } else if ("file".equals(scheme)) {
            setDataSource(uri.getPath());
        } else {
            if ("content".equals(scheme) && "settings".equals(uri.getAuthority())) {
                int type = RingtoneManager.getDefaultType(uri);
                Uri cacheUri = RingtoneManager.getCacheForType(type);
                Uri actualUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
                if (!attemptDataSource(resolver, cacheUri) && !attemptDataSource(resolver, actualUri)) {
                    setDataSource(uri.toString(), (Map) headers);
                }
            } else if (!attemptDataSource(resolver, uri)) {
                Map headers2;
                IOmaSettingHelper helper = (IOmaSettingHelper) MPlugin.createInstance(IOmaSettingHelper.class.getName(), context);
                if (helper != null) {
                    headers2 = helper.setSettingHeader(context, uri, headers2);
                } else {
                    Log.w(TAG, "IOmaSettingHelper plugin returns null, uses default headers");
                }
                setDataSource(uri.toString(), headers2);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0015 A:{ExcHandler: java.lang.NullPointerException (r1_0 'ex' java.lang.Exception), Splitter: B:8:0x0014} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0015 A:{ExcHandler: java.lang.NullPointerException (r1_0 'ex' java.lang.Exception), Splitter: B:8:0x0014} */
    /* JADX WARNING: Missing block: B:10:0x0015, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x0016, code:
            android.util.Log.w(TAG, "Couldn't open " + r8 + ": " + r1);
     */
    /* JADX WARNING: Missing block: B:12:0x003c, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean attemptDataSource(ContentResolver resolver, Uri uri) {
        Throwable th;
        Throwable th2 = null;
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = resolver.openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
            setDataSource(assetFileDescriptor);
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (Throwable th3) {
                    th2 = th3;
                }
            }
            if (th2 == null) {
                return true;
            }
            try {
                throw th2;
            } catch (Exception ex) {
            }
        } catch (Throwable th22) {
            Throwable th4 = th22;
            th22 = th;
            th = th4;
        }
        if (assetFileDescriptor != null) {
            try {
                assetFileDescriptor.close();
            } catch (Throwable th5) {
                if (th22 == null) {
                    th22 = th5;
                } else if (th22 != th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }

    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(String) isOppoCreate=" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = false;
        this.mDefaultPlayerStarted = false;
        this.mPath = path;
        this.mCurrentState = 2;
        setDataSource(path, null, null);
    }

    public void setDataSource(String path, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(path, headers) isOppoCreate=" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = false;
        this.mDefaultPlayerStarted = false;
        this.mPath = path;
        this.mHeaders = headers;
        this.mCurrentState = 2;
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
        Uri uri = Uri.parse(path);
        String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            path = uri.getPath();
        } else if (scheme != null) {
            if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("widevine://") || path.startsWith("rtsp://") || path.endsWith(".sdp") || path.endsWith(".sdp?")) {
                this.mNotUsingOppoMedia = true;
            } else if (path.startsWith("file://") && path.endsWith(".m3u8")) {
                this.mNotUsingOppoMedia = true;
            } else {
                this.mNotUsingOppoMedia = false;
            }
            nativeSetDataSource(MediaHTTPService.createHttpServiceBinderIfNecessary(path), path, keys, values);
            return;
        }
        File file = new File(path);
        if (file.exists()) {
            FileInputStream is = new FileInputStream(file);
            setDataSource(is.getFD());
            is.close();
            return;
        }
        throw new IOException("setDataSource failed.");
    }

    public void setDataSource(AssetFileDescriptor afd) throws IOException, IllegalArgumentException, IllegalStateException {
        Preconditions.checkNotNull(afd);
        if (afd.getDeclaredLength() < 0) {
            setDataSource(afd.getFileDescriptor());
            return;
        }
        setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
    }

    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(FileDescriptor fd) isOppoCreate=" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = false;
        this.mDefaultPlayerStarted = false;
        this.mCurrentState = 2;
        setDataSource(fd, 0, 576460752303423487L);
    }

    public void setDataSource(FileDescriptor fd, long offset, long length) throws IOException, IllegalArgumentException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(FileDescriptor, long, long) isOppoCreate=" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = false;
        this.mDefaultPlayerStarted = false;
        try {
            closeFd();
            this.mFd = Libcore.os.dup(fd);
            this.mOffset = offset;
            this.mLength = length;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        }
        this.mCurrentState = 2;
        _setDataSource(fd, offset, length);
        String packageName = ActivityThread.currentPackageName();
        if (packageName != null && packageName.length() > 0) {
            String result = OppoMultimediaManager.getInstance(null).getParameters("check_daemon_listinfo_byname=3=" + packageName);
            if (result != null && result.equals("true")) {
                Log.i(TAG, "The package name is " + packageName + "qq & wecha zenmode control !");
                int uid = Process.myUid();
                if (this.mNotificationManager != null) {
                    try {
                        this.mInterceptFlag = this.mNotificationManager.shouldInterceptSound(packageName, uid);
                    } catch (RemoteException e) {
                        Log.e(TAG, " start RemoteException");
                    }
                }
                Parcel pMute = Parcel.obtain();
                pMute.writeInt(this.mInterceptFlag ? 1 : 0);
                setParameter(10011, pMute);
                pMute.recycle();
            }
        }
    }

    public void setDataSource(MediaDataSource dataSource) throws IllegalArgumentException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "setDataSource(MediaDataSource) isOppoCreate = " + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = true;
        this.mDefaultPlayerStarted = false;
        if (this.isOppoCreate) {
            Log.e(TAG, "setDataSource(MediaDataSource) oppomedia do not support! return directly");
        } else {
            _setDataSource(dataSource);
        }
    }

    public void prepare() throws IOException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "prepare() isOppoCreate=" + this.isOppoCreate);
        }
        this.mPrepareAsync = false;
        this.mCurrentState = 4;
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.prepare();
            return;
        }
        try {
            _prepare();
            scanInternalSubtitleTracks();
        } catch (IOException e) {
            e.printStackTrace();
            if (this.mCtsCallingPackage) {
                Log.i(TAG, "cts test not handle error mCurrentState=" + this.mCurrentState);
                throw e;
            } else if (!handleMediaPlayerError()) {
                Log.i(TAG, "prepare failed ,throw IOException to app");
                throw e;
            }
        }
    }

    public void prepareAsync() throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "prepareAsync() isOppoCreate=" + this.isOppoCreate);
        }
        this.mPrepareAsync = true;
        this.mCurrentState = 4;
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.prepareAsync();
        } else {
            _prepareAsync();
        }
    }

    public void start() throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "start() isOppoCreate = " + this.isOppoCreate + " mCurrentState = " + this.mCurrentState);
        }
        if (!this.isOppoCreate) {
            baseStart();
            stayAwake(true);
            _start();
            this.mDefaultPlayerStarted = true;
        } else if (this.mCurrentState >= 8 || !this.mPrepareAsync) {
            if (this.mNeedSeeking) {
                Log.v(TAG, "mOppoMediaPlayer start seekTo: " + this.mSeekMs);
                seekTo(this.mSeekMs);
                this.mNeedSeeking = false;
                this.mSeekMs = -1;
            }
            this.mOppoMediaPlayer.start();
        } else {
            return;
        }
        this.mCurrentState = 16;
    }

    private int getAudioStreamType() {
        if (this.mStreamType == Integer.MIN_VALUE) {
            this.mStreamType = _getAudioStreamType();
        }
        return this.mStreamType;
    }

    public void stop() throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "stop() isOppoCreate = " + this.isOppoCreate);
        }
        this.mCurrentState = 64;
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.stop();
        } else {
            stayAwake(false);
            _stop();
        }
        this.mScreenOn = false;
    }

    public void pause() throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "pause() isOppoCreate=" + this.isOppoCreate);
        }
        this.mCurrentState = 32;
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.pause();
            return;
        }
        stayAwake(false);
        _pause();
        this.mDefaultPlayerStarted = false;
    }

    public void setWakeMode(Context context, int mode) {
        if (this.mDebugLog) {
            Log.i(TAG, "setWakeMode() isOppoCreate=" + this.isOppoCreate + " mode=" + mode);
        }
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.setWakeMode(context, mode);
        } else {
            boolean washeld = false;
            if (SystemProperties.getBoolean("audio.offload.ignore_setawake", false)) {
                Log.w(TAG, "IGNORING setWakeMode " + mode);
                return;
            }
            if (this.mWakeLock != null) {
                if (this.mWakeLock.isHeld()) {
                    washeld = true;
                    this.mWakeLock.release();
                }
                this.mWakeLock = null;
            }
            this.mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(536870912 | mode, MediaPlayer.class.getName());
            this.mWakeLock.setReferenceCounted(false);
            if (washeld) {
                this.mWakeLock.acquire();
            }
        }
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (this.mDebugLog) {
            Log.i(TAG, "setScreenOnWhilePlaying() isOppoCreate=" + this.isOppoCreate + " screenOn=" + screenOn);
        }
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.setScreenOnWhilePlaying(screenOn);
        } else if (this.mScreenOnWhilePlaying != screenOn) {
            if (screenOn && this.mSurfaceHolder == null) {
                Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
            }
            this.mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
        if (screenOn) {
            this.mScreenOn = true;
        }
    }

    private void stayAwake(boolean awake) {
        if (this.mWakeLock != null) {
            if (awake && !this.mWakeLock.isHeld()) {
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

    public int getVideoWidth() {
        if (this.mDebugLog) {
            Log.i(TAG, "getVideoWidth() isOppoCreate=" + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            return this.mOppoMediaPlayer.getVideoWidth();
        }
        return _getVideoWidth();
    }

    public int getVideoHeight() {
        if (this.mDebugLog) {
            Log.i(TAG, "getVideoHeight() isOppoCreate=" + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            return this.mOppoMediaPlayer.getVideoHeight();
        }
        return _getVideoHeight();
    }

    public boolean isPlaying() {
        if (this.mDebugLog) {
            Log.i(TAG, "isPlaying() isOppoCreate=" + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            return this.mOppoMediaPlayer.isPlaying();
        }
        return _isPlaying();
    }

    public PlaybackParams easyPlaybackParams(float rate, int audioMode) {
        PlaybackParams params = new PlaybackParams();
        params.allowDefaults();
        switch (audioMode) {
            case 0:
                params.setSpeed(rate).setPitch(1.0f);
                break;
            case 1:
                params.setSpeed(rate).setPitch(1.0f).setAudioFallbackMode(2);
                break;
            case 2:
                params.setSpeed(rate).setPitch(rate);
                break;
            default:
                throw new IllegalArgumentException("Audio playback mode " + audioMode + " is not supported");
        }
        return params;
    }

    public void seekTo(int msec) throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "seekTo() isOppoCreate=" + this.isOppoCreate + " msec=" + msec);
        }
        this.mSeekMs = msec;
        if (!this.isOppoCreate) {
            _seekTo(msec);
        } else if (this.mCurrentState >= 8 || !this.mPrepareAsync) {
            this.mOppoMediaPlayer.seekTo(msec);
        }
    }

    public MediaTimestamp getTimestamp() {
        try {
            return new MediaTimestamp(((long) getCurrentPosition()) * 1000, System.nanoTime(), isPlaying() ? getPlaybackParams().getSpeed() : TonemapCurve.LEVEL_BLACK);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public int getCurrentPosition() {
        if (this.mDebugLog) {
            Log.i(TAG, "getCurrentPosition() isOppoCreate=" + this.isOppoCreate + " mCurrentState = " + this.mCurrentState);
        }
        if (this.mCurrentState < 2) {
            return 0;
        }
        if (!this.isOppoCreate) {
            return _getCurrentPosition();
        }
        if (this.mCurrentState < 8) {
            return 0;
        }
        return this.mOppoMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        if (this.mDebugLog) {
            Log.i(TAG, "getDuration() isOppoCreate=" + this.isOppoCreate + " mCurrentState = " + this.mCurrentState);
        }
        if (this.mCurrentState < 2) {
            return -1;
        }
        if (!this.isOppoCreate) {
            return _getDuration();
        }
        if (this.mCurrentState < 8) {
            return 0;
        }
        return this.mOppoMediaPlayer.getDuration();
    }

    public Metadata getMetadata(boolean update_only, boolean apply_filter) {
        Parcel reply = Parcel.obtain();
        Metadata data = new Metadata();
        if (!native_getMetadata(update_only, apply_filter, reply)) {
            reply.recycle();
            return null;
        } else if (data.parse(reply)) {
            return data;
        } else {
            reply.recycle();
            return null;
        }
    }

    public int setMetadataFilter(Set<Integer> allow, Set<Integer> block) {
        Parcel request = newRequest();
        int capacity = request.dataSize() + ((((allow.size() + 1) + 1) + block.size()) * 4);
        if (request.dataCapacity() < capacity) {
            request.setDataCapacity(capacity);
        }
        request.writeInt(allow.size());
        for (Integer t : allow) {
            request.writeInt(t.intValue());
        }
        request.writeInt(block.size());
        for (Integer t2 : block) {
            request.writeInt(t2.intValue());
        }
        return native_setMetadataFilter(request);
    }

    public void release() {
        if (this.mDebugLog) {
            Log.i(TAG, "release() isOppoCreate =" + this.isOppoCreate);
        }
        this.mNotUsingOppoMedia = true;
        this.mCurrentState = 1;
        closeFd();
        if (this.mOppoMediaPlayer != null) {
            this.mOppoMediaPlayer.release();
            this.mOppoMediaPlayer = null;
        }
        baseRelease();
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
        this.mOnDurationUpdateListener = null;
        if (this.mTimeProvider != null) {
            this.mTimeProvider.close();
            this.mTimeProvider = null;
        }
        this.mOnSubtitleDataListener = null;
        _release();
        this.isOppoCreate = false;
        this.mAudioSessionId = -1;
        this.mAudioStreamType = -1;
        this.mScreenOn = false;
    }

    public void reset() {
        if (this.mDebugLog) {
            Log.i(TAG, "reset() isOppoCreate =" + this.isOppoCreate);
        }
        this.mSeekMs = -1;
        this.mCurrentState = 1;
        if (this.isCreateOppoMediaPlayer) {
            this.isCreateOppoMediaPlayer = false;
        } else {
            closeFd();
        }
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.reset();
            this.mDefaultPlayerStarted = false;
        } else {
            this.mSelectedSubtitleTrackIndex = -1;
            synchronized (this.mOpenSubtitleSources) {
                for (InputStream is : this.mOpenSubtitleSources) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
                this.mOpenSubtitleSources.clear();
            }
            if (this.mSubtitleController != null) {
                this.mSubtitleController.reset();
            }
            if (this.mTimeProvider != null) {
                this.mTimeProvider.close();
                this.mTimeProvider = null;
            }
            stayAwake(false);
            _reset();
            if (this.mEventHandler != null) {
                this.mEventHandler.removeCallbacksAndMessages(null);
            }
            synchronized (this.mIndexTrackPairs) {
                this.mIndexTrackPairs.clear();
                this.mInbandTrackIndices.clear();
            }
        }
        this.mScreenOn = false;
        this.isOppoCreate = false;
        this.mInterceptFlag = false;
    }

    public void setAudioStreamType(int streamtype) {
        int i = 0;
        this.mAudioStreamType = streamtype;
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.setAudioStreamType(streamtype);
        } else {
            baseUpdateAudioAttributes(new Builder().setInternalLegacyStreamType(streamtype).build());
            _setAudioStreamType(streamtype);
            this.mStreamType = streamtype;
        }
        String packageName = ActivityThread.currentPackageName();
        if (packageName != null && packageName.length() > 0) {
            String result = OppoMultimediaManager.getInstance(null).getParameters("get_daemon_listinfo_byname=3=" + packageName);
            if (result != null && result.equals("wechat_mute")) {
                Log.i(TAG, "The package name is " + packageName + "wecha zenmode control !");
                if (this.mStreamType == 5 || this.mStreamType == 2) {
                    Parcel pMute = Parcel.obtain();
                    if (this.mInterceptFlag) {
                        i = 1;
                    }
                    pMute.writeInt(i);
                    setParameter(10011, pMute);
                    pMute.recycle();
                }
            }
        }
    }

    public void setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {
        boolean z = false;
        if (this.mDebugLog) {
            Log.i(TAG, "setAudioAttributes() isOppoCreate = " + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            Log.e(TAG, "setAudioAttributes() oppomedia do not support! return directly");
        } else if (attributes == null) {
            String msg = "Cannot set AudioAttributes to null";
            throw new IllegalArgumentException("Cannot set AudioAttributes to null");
        } else {
            baseUpdateAudioAttributes(attributes);
            this.mUsage = attributes.getUsage();
            if ((attributes.getAllFlags() & 64) != 0) {
                z = true;
            }
            this.mBypassInterruptionPolicy = z;
            Parcel pattributes = Parcel.obtain();
            attributes.writeToParcel(pattributes, 1);
            setParameter((int) KEY_PARAMETER_AUDIO_ATTRIBUTES, pattributes);
            pattributes.recycle();
        }
    }

    public void setLooping(boolean looping) {
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.setLooping(looping);
        } else {
            _setLooping(looping);
        }
    }

    public boolean isLooping() {
        if (this.isOppoCreate) {
            return this.mOppoMediaPlayer.isLooping();
        }
        return _isLooping();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.setVolume(leftVolume, rightVolume);
        } else {
            baseSetVolume(leftVolume, rightVolume);
        }
    }

    void playerSetVolume(float leftVolume, float rightVolume) {
        _setVolume(leftVolume, rightVolume);
    }

    public void setVolume(float volume) {
        setVolume(volume, volume);
    }

    public void setAudioSessionId(int sessionId) throws IllegalArgumentException, IllegalStateException {
        this.mAudioSessionId = sessionId;
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.setAudioSessionId(sessionId);
        } else {
            _setAudioSessionId(sessionId);
        }
    }

    public int getAudioSessionId() {
        if (this.isOppoCreate) {
            this.mAudioSessionId = this.mOppoMediaPlayer.getAudioSessionId();
        } else {
            this.mAudioSessionId = _getAudioSessionId();
        }
        return this.mAudioSessionId;
    }

    public void attachAuxEffect(int effectId) {
        if (this.mDebugLog) {
            Log.i(TAG, "attachAuxEffect(), isOppoCreate =" + this.isOppoCreate + ", effectId = " + effectId);
        }
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.attachAuxEffect(effectId);
        } else {
            _attachAuxEffect(effectId);
        }
    }

    public boolean setParameter(int key, String value) {
        Parcel p = Parcel.obtain();
        p.writeString(value);
        boolean ret = setParameter(key, p);
        p.recycle();
        return ret;
    }

    public boolean setParameter(int key, int value) {
        Parcel p = Parcel.obtain();
        p.writeInt(value);
        boolean ret = setParameter(key, p);
        p.recycle();
        return ret;
    }

    public Parcel getParcelParameter(int key) {
        Parcel p = Parcel.obtain();
        getParameter(key, p);
        return p;
    }

    public String getStringParameter(int key) {
        Parcel p = Parcel.obtain();
        getParameter(key, p);
        String ret = p.readString();
        p.recycle();
        return ret;
    }

    public int getIntParameter(int key) {
        Parcel p = Parcel.obtain();
        getParameter(key, p);
        int ret = p.readInt();
        p.recycle();
        return ret;
    }

    public void setAuxEffectSendLevel(float level) {
        if (this.mDebugLog) {
            Log.i(TAG, "setAuxEffectSendLevel(), isOppoCreate =" + this.isOppoCreate + ", level = " + level);
        }
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.setAuxEffectSendLevel(level);
        } else {
            baseSetAuxEffectSendLevel(level);
        }
    }

    int playerSetAuxEffectSendLevel(float level) {
        _setAuxEffectSendLevel(level);
        return 0;
    }

    public TrackInfo[] getTrackInfo() throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "getTrackInfo() isOppoCreate=" + this.isOppoCreate);
        }
        int i;
        if (this.isOppoCreate) {
            Parcel mParcel = Parcel.obtain();
            com.oppo.media.OppoMediaPlayer.TrackInfo[] mtrackInfo = this.mOppoMediaPlayer.getTrackInfo();
            TrackInfo[] trackInfo1 = new TrackInfo[mtrackInfo.length];
            for (i = 0; i < mtrackInfo.length; i++) {
                mParcel.setDataPosition(0);
                mParcel.writeInt(mtrackInfo[i].getTrackType());
                mParcel.writeString(mtrackInfo[i].getLanguage());
                mParcel.setDataPosition(0);
                trackInfo1[i] = new TrackInfo(mParcel);
            }
            return trackInfo1;
        }
        TrackInfo[] allTrackInfo;
        TrackInfo[] trackInfo = getInbandTrackInfo();
        synchronized (this.mIndexTrackPairs) {
            allTrackInfo = new TrackInfo[this.mIndexTrackPairs.size()];
            for (i = 0; i < allTrackInfo.length; i++) {
                Pair<Integer, SubtitleTrack> p = (Pair) this.mIndexTrackPairs.get(i);
                if (p.first != null) {
                    allTrackInfo[i] = trackInfo[((Integer) p.first).intValue()];
                } else {
                    SubtitleTrack track = p.second;
                    allTrackInfo[i] = new TrackInfo(track.getTrackType(), track.getFormat());
                }
            }
        }
        return allTrackInfo;
    }

    private TrackInfo[] getInbandTrackInfo() throws IllegalStateException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
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
        if (MEDIA_MIMETYPE_TEXT_SUBRIP.equals(mimeType)) {
            return true;
        }
        return false;
    }

    public void setSubtitleAnchor(SubtitleController controller, Anchor anchor) {
        if (this.mDebugLog) {
            Log.i(TAG, "setSubtitleAnchor() isOppoCreate = " + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            Log.e(TAG, "setSubtitleAnchor() oppomedia do not support! return directly");
            return;
        }
        this.mSubtitleController = controller;
        this.mSubtitleController.setAnchor(anchor);
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private synchronized void setSubtitleAnchor() {
        /*
        r5 = this;
        monitor-enter(r5);
        r3 = r5.mSubtitleController;	 Catch:{ all -> 0x0038 }
        if (r3 != 0) goto L_0x0024;	 Catch:{ all -> 0x0038 }
    L_0x0005:
        r2 = new android.os.HandlerThread;	 Catch:{ all -> 0x0038 }
        r3 = "SetSubtitleAnchorThread";	 Catch:{ all -> 0x0038 }
        r2.<init>(r3);	 Catch:{ all -> 0x0038 }
        r2.start();	 Catch:{ all -> 0x0038 }
        r1 = new android.os.Handler;	 Catch:{ all -> 0x0038 }
        r3 = r2.getLooper();	 Catch:{ all -> 0x0038 }
        r1.<init>(r3);	 Catch:{ all -> 0x0038 }
        r3 = new android.media.MediaPlayer$9;	 Catch:{ all -> 0x0038 }
        r3.<init>(r5, r2);	 Catch:{ all -> 0x0038 }
        r1.post(r3);	 Catch:{ all -> 0x0038 }
        r2.join();	 Catch:{ InterruptedException -> 0x0026 }
    L_0x0024:
        monitor-exit(r5);
        return;
    L_0x0026:
        r0 = move-exception;
        r3 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0038 }
        r3.interrupt();	 Catch:{ all -> 0x0038 }
        r3 = "MediaPlayer";	 Catch:{ all -> 0x0038 }
        r4 = "failed to join SetSubtitleAnchorThread";	 Catch:{ all -> 0x0038 }
        android.util.Log.w(r3, r4);	 Catch:{ all -> 0x0038 }
        goto L_0x0024;
    L_0x0038:
        r3 = move-exception;
        monitor-exit(r5);
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.setSubtitleAnchor():void");
    }

    public void onSubtitleTrackSelected(SubtitleTrack track) {
        if (this.mDebugLog) {
            Log.i(TAG, "onSubtitleTrackSelected() isOppoCreate = " + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            Log.e(TAG, "onSubtitleTrackSelected() oppomedia do not support! return directly");
            return;
        }
        if (this.mSelectedSubtitleTrackIndex >= 0) {
            try {
                selectOrDeselectInbandTrack(this.mSelectedSubtitleTrackIndex, false);
            } catch (IllegalStateException e) {
            }
            this.mSelectedSubtitleTrackIndex = -1;
        }
        setOnSubtitleDataListener(null);
        if (track != null) {
            synchronized (this.mIndexTrackPairs) {
                for (Pair<Integer, SubtitleTrack> p : this.mIndexTrackPairs) {
                    if (p.first != null && p.second == track) {
                        this.mSelectedSubtitleTrackIndex = ((Integer) p.first).intValue();
                        break;
                    }
                }
            }
            if (this.mSelectedSubtitleTrackIndex >= 0) {
                try {
                    selectOrDeselectInbandTrack(this.mSelectedSubtitleTrackIndex, true);
                } catch (IllegalStateException e2) {
                }
                setOnSubtitleDataListener(this.mSubtitleDataListener);
            }
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public void addSubtitleSource(java.io.InputStream r8, android.media.MediaFormat r9) throws java.lang.IllegalStateException {
        /*
        r7 = this;
        r4 = "MediaPlayer";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "addSubtitleSource: MediaFormat = ";
        r5 = r5.append(r6);
        r5 = r5.append(r9);
        r5 = r5.toString();
        android.util.Log.d(r4, r5);
        r4 = r7.mDebugLog;
        if (r4 == 0) goto L_0x003a;
    L_0x001e:
        r4 = "MediaPlayer";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "addSubtitleSource() isOppoCreate = ";
        r5 = r5.append(r6);
        r6 = r7.isOppoCreate;
        r5 = r5.append(r6);
        r5 = r5.toString();
        android.util.Log.i(r4, r5);
    L_0x003a:
        r4 = r7.isOppoCreate;
        if (r4 == 0) goto L_0x0048;
    L_0x003e:
        r4 = "MediaPlayer";
        r5 = "addSubtitleSource() oppomedia do not support! return directly";
        android.util.Log.e(r4, r5);
        return;
    L_0x0048:
        r1 = r8;
        r0 = r9;
        if (r8 == 0) goto L_0x007a;
    L_0x004c:
        r5 = r7.mOpenSubtitleSources;
        monitor-enter(r5);
        r4 = r7.mOpenSubtitleSources;	 Catch:{ all -> 0x0077 }
        r4.add(r8);	 Catch:{ all -> 0x0077 }
        monitor-exit(r5);
    L_0x0055:
        r7.getMediaTimeProvider();
        r3 = new android.os.HandlerThread;
        r4 = "SubtitleReadThread";
        r5 = 9;
        r3.<init>(r4, r5);
        r3.start();
        r2 = new android.os.Handler;
        r4 = r3.getLooper();
        r2.<init>(r4);
        r4 = new android.media.MediaPlayer$10;
        r4.<init>(r7, r8, r9, r3);
        r2.post(r4);
        return;
    L_0x0077:
        r4 = move-exception;
        monitor-exit(r5);
        throw r4;
    L_0x007a:
        r4 = "MediaPlayer";
        r5 = "addSubtitleSource called with null InputStream";
        android.util.Log.w(r4, r5);
        goto L_0x0055;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.addSubtitleSource(java.io.InputStream, android.media.MediaFormat):void");
    }

    private void scanInternalSubtitleTracks() {
        if (this.mSubtitleController == null) {
            Log.d(TAG, "setSubtitleAnchor in MediaPlayer");
            setSubtitleAnchor();
        }
        populateInbandTracks();
        if (this.mSubtitleController != null) {
            this.mSubtitleController.selectDefaultTrack();
        }
    }

    private void populateInbandTracks() {
        TrackInfo[] tracks = getInbandTrackInfo();
        synchronized (this.mIndexTrackPairs) {
            for (int i = 0; i < tracks.length; i++) {
                if (!this.mInbandTrackIndices.get(i)) {
                    this.mInbandTrackIndices.set(i);
                    if (tracks[i].getTrackType() == 4) {
                        this.mIndexTrackPairs.add(Pair.create(Integer.valueOf(i), this.mSubtitleController.addTrack(tracks[i].getFormat())));
                    } else {
                        this.mIndexTrackPairs.add(Pair.create(Integer.valueOf(i), null));
                    }
                }
            }
        }
    }

    public void addTimedTextSource(String path, String mimeType) throws IOException, IllegalArgumentException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "addTimedTextSource() isOppoCreate = " + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            Log.e(TAG, "addTimedTextSource(String, String ) oppomedia do not support! return directly");
        } else if (availableMimeTypeForExternalSource(mimeType)) {
            File file = new File(path);
            if (file.exists()) {
                FileInputStream is = new FileInputStream(file);
                addTimedTextSource(is.getFD(), mimeType);
                is.close();
                return;
            }
            throw new IOException(path);
        } else {
            throw new IllegalArgumentException("Illegal mimeType for timed text source: " + mimeType);
        }
    }

    /* JADX WARNING: Missing block: B:31:0x00a0, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addTimedTextSource(Context context, Uri uri, String mimeType) throws IOException, IllegalArgumentException, IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "addTimedTextSource() isOppoCreate = " + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            Log.e(TAG, "addTimedTextSource(Context , Uri , String) oppomedia do not support! return directly");
            return;
        }
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("file")) {
            addTimedTextSource(uri.getPath(), mimeType);
            return;
        }
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
            if (assetFileDescriptor == null) {
                Log.e(TAG, "addTimedTextSource: Null fd! uri=" + uri);
                if (assetFileDescriptor != null) {
                    assetFileDescriptor.close();
                }
                return;
            }
            addTimedTextSource(assetFileDescriptor.getFileDescriptor(), mimeType);
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (SecurityException ex) {
            Log.e(TAG, "addTimedTextSource: SecurityException! uri=" + uri, ex);
            if (assetFileDescriptor != null) {
                assetFileDescriptor.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "addTimedTextSource: IOException! uri=" + uri);
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
        if (this.mDebugLog) {
            Log.i(TAG, "addTimedTextSource(FileDescriptor, String) isOppoCreate = " + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            Log.e(TAG, "addTimedTextSource(FileDescriptor, String) oppomedia do not support! return directly");
        } else {
            addTimedTextSource(fd, 0, 576460752303423487L, mimeType);
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public void addTimedTextSource(java.io.FileDescriptor r21, long r22, long r24, java.lang.String r26) throws java.lang.IllegalArgumentException, java.lang.IllegalStateException {
        /*
        r20 = this;
        r0 = r20;
        r3 = r0.mDebugLog;
        if (r3 == 0) goto L_0x0024;
    L_0x0006:
        r3 = "MediaPlayer";
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r6 = "addTimedTextSource(FileDescriptor, long, long, String) isOppoCreate = ";
        r4 = r4.append(r6);
        r0 = r20;
        r6 = r0.isOppoCreate;
        r4 = r4.append(r6);
        r4 = r4.toString();
        android.util.Log.i(r3, r4);
    L_0x0024:
        r0 = r20;
        r3 = r0.isOppoCreate;
        if (r3 == 0) goto L_0x0034;
    L_0x002a:
        r3 = "MediaPlayer";
        r4 = "addTimedTextSource(FileDescriptor, long, long, String) oppomedia do not support! return directly";
        android.util.Log.e(r3, r4);
        return;
    L_0x0034:
        r3 = availableMimeTypeForExternalSource(r26);
        if (r3 != 0) goto L_0x0056;
    L_0x003a:
        r3 = new java.lang.IllegalArgumentException;
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r6 = "Illegal mimeType for timed text source: ";
        r4 = r4.append(r6);
        r0 = r26;
        r4 = r4.append(r0);
        r4 = r4.toString();
        r3.<init>(r4);
        throw r3;
    L_0x0056:
        r3 = libcore.io.Libcore.os;	 Catch:{ ErrnoException -> 0x00e0 }
        r0 = r21;	 Catch:{ ErrnoException -> 0x00e0 }
        r5 = r3.dup(r0);	 Catch:{ ErrnoException -> 0x00e0 }
        r13 = new android.media.MediaFormat;
        r13.<init>();
        r3 = "mime";
        r0 = r26;
        r13.setString(r3, r0);
        r3 = "is-timed-text";
        r4 = 1;
        r13.setInteger(r3, r4);
        r0 = r20;
        r3 = r0.mSubtitleController;
        if (r3 != 0) goto L_0x007b;
    L_0x0078:
        r20.setSubtitleAnchor();
    L_0x007b:
        r0 = r20;
        r3 = r0.mSubtitleController;
        r3 = r3.hasRendererFor(r13);
        if (r3 != 0) goto L_0x0099;
    L_0x0085:
        r2 = android.app.ActivityThread.currentApplication();
        r0 = r20;
        r3 = r0.mSubtitleController;
        r4 = new android.media.SRTRenderer;
        r0 = r20;
        r6 = r0.mEventHandler;
        r4.<init>(r2, r6);
        r3.registerRenderer(r4);
    L_0x0099:
        r0 = r20;
        r3 = r0.mSubtitleController;
        r10 = r3.addTrack(r13);
        r0 = r20;
        r4 = r0.mIndexTrackPairs;
        monitor-enter(r4);
        r0 = r20;	 Catch:{ all -> 0x00f1 }
        r3 = r0.mIndexTrackPairs;	 Catch:{ all -> 0x00f1 }
        r6 = 0;	 Catch:{ all -> 0x00f1 }
        r6 = android.util.Pair.create(r6, r10);	 Catch:{ all -> 0x00f1 }
        r3.add(r6);	 Catch:{ all -> 0x00f1 }
        monitor-exit(r4);
        r20.getMediaTimeProvider();
        r14 = r5;
        r18 = r22;
        r16 = r24;
        r11 = new android.os.HandlerThread;
        r3 = "TimedTextReadThread";
        r4 = 9;
        r11.<init>(r3, r4);
        r11.start();
        r15 = new android.os.Handler;
        r3 = r11.getLooper();
        r15.<init>(r3);
        r3 = new android.media.MediaPlayer$11;
        r4 = r20;
        r6 = r22;
        r8 = r24;
        r3.<init>(r4, r5, r6, r8, r10, r11);
        r15.post(r3);
        return;
    L_0x00e0:
        r12 = move-exception;
        r3 = "MediaPlayer";
        r4 = r12.getMessage();
        android.util.Log.e(r3, r4, r12);
        r3 = new java.lang.RuntimeException;
        r3.<init>(r12);
        throw r3;
    L_0x00f1:
        r3 = move-exception;
        monitor-exit(r4);
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaPlayer.addTimedTextSource(java.io.FileDescriptor, long, long, java.lang.String):void");
    }

    public int getSelectedTrack(int trackType) throws IllegalStateException {
        int i;
        if (this.mDebugLog) {
            Log.i(TAG, "getSelectedTrack() isOppoCreate = " + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            Log.e(TAG, "getSelectedTrack() oppomedia do not support! return directly");
            return -1;
        }
        if (this.mSubtitleController != null && (trackType == 4 || trackType == 3)) {
            SubtitleTrack subtitleTrack = this.mSubtitleController.getSelectedTrack();
            if (subtitleTrack != null) {
                synchronized (this.mIndexTrackPairs) {
                    i = 0;
                    while (i < this.mIndexTrackPairs.size()) {
                        if (((Pair) this.mIndexTrackPairs.get(i)).second == subtitleTrack && subtitleTrack.getTrackType() == trackType) {
                            return i;
                        }
                        i++;
                    }
                }
            }
        }
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(7);
            request.writeInt(trackType);
            invoke(request, reply);
            int inbandTrackIndex = reply.readInt();
            synchronized (this.mIndexTrackPairs) {
                i = 0;
                while (i < this.mIndexTrackPairs.size()) {
                    Pair<Integer, SubtitleTrack> p = (Pair) this.mIndexTrackPairs.get(i);
                    if (p.first == null || ((Integer) p.first).intValue() != inbandTrackIndex) {
                        i++;
                    }
                }
                request.recycle();
                reply.recycle();
                return -1;
            }
        } finally {
            request.recycle();
            reply.recycle();
        }
        return i;
    }

    public void selectTrack(int index) throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "selectTrack() isOppoCreate = " + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.selectTrack(index);
        } else {
            selectOrDeselectTrack(index, true);
        }
    }

    public void deselectTrack(int index) throws IllegalStateException {
        if (this.mDebugLog) {
            Log.i(TAG, "deselectTrack() isOppoCreate = " + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            this.mOppoMediaPlayer.deselectTrack(index);
        } else {
            selectOrDeselectTrack(index, false);
        }
    }

    private void selectOrDeselectTrack(int index, boolean select) throws IllegalStateException {
        populateInbandTracks();
        try {
            Pair<Integer, SubtitleTrack> p = (Pair) this.mIndexTrackPairs.get(index);
            SubtitleTrack track = p.second;
            if (track == null) {
                selectOrDeselectInbandTrack(((Integer) p.first).intValue(), select);
            } else if (this.mSubtitleController != null) {
                if (select) {
                    if (track.getTrackType() == 3) {
                        int ttIndex = getSelectedTrack(3);
                        synchronized (this.mIndexTrackPairs) {
                            if (ttIndex >= 0) {
                                if (ttIndex < this.mIndexTrackPairs.size()) {
                                    Pair<Integer, SubtitleTrack> p2 = (Pair) this.mIndexTrackPairs.get(ttIndex);
                                    if (p2.first != null && p2.second == null) {
                                        selectOrDeselectInbandTrack(((Integer) p2.first).intValue(), false);
                                    }
                                }
                            }
                        }
                    }
                    this.mSubtitleController.selectTrack(track);
                    return;
                }
                if (this.mSubtitleController.getSelectedTrack() == track) {
                    this.mSubtitleController.selectTrack(null);
                } else {
                    Log.w(TAG, "trying to deselect track that was not selected");
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private void selectOrDeselectInbandTrack(int index, boolean select) throws IllegalStateException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(select ? 4 : 5);
            request.writeInt(index);
            invoke(request, reply);
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    public void setRetransmitEndpoint(InetSocketAddress endpoint) throws IllegalStateException, IllegalArgumentException {
        if (this.mDebugLog) {
            Log.i(TAG, "setRetransmitEndpoint() isOppoCreate = " + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            Log.e(TAG, "setRetransmitEndpoint() oppomedia do not support! return directly");
            return;
        }
        String addrString = null;
        int port = 0;
        if (endpoint != null) {
            addrString = endpoint.getAddress().getHostAddress();
            port = endpoint.getPort();
        }
        int ret = native_setRetransmitEndpoint(addrString, port);
        if (ret != 0) {
            throw new IllegalArgumentException("Illegal re-transmit endpoint; native ret " + ret);
        }
    }

    protected void finalize() {
        baseRelease();
        native_finalize();
    }

    public MediaTimeProvider getMediaTimeProvider() {
        if (this.mDebugLog) {
            Log.i(TAG, "getMediaTimeProvider() isOppoCreate = " + this.isOppoCreate);
        }
        if (this.isOppoCreate) {
            Log.e(TAG, "getMediaTimeProvider() oppomedia do not support! return directly");
            return null;
        }
        if (this.mTimeProvider == null) {
            this.mTimeProvider = new TimeProvider(this);
        }
        return this.mTimeProvider;
    }

    private static void postEventFromNative(Object mediaplayer_ref, int what, int arg1, int arg2, Object obj) {
        MediaPlayer mp = (MediaPlayer) ((WeakReference) mediaplayer_ref).get();
        if (mp == null) {
            Log.e(TAG, "postEventFromNative: Null mp! what=" + what + ", arg1=" + arg1 + ", arg2=" + arg2);
            return;
        }
        if (what == 200 && arg1 == 2) {
            mp.start();
        }
        if (mp.mEventHandler != null) {
            mp.mEventHandler.sendMessage(mp.mEventHandler.obtainMessage(what, arg1, arg2, obj));
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

    public void setOnSubtitleDataListener(OnSubtitleDataListener listener) {
        this.mOnSubtitleDataListener = listener;
    }

    public void setOnTimedMetaDataAvailableListener(OnTimedMetaDataAvailableListener listener) {
        this.mOnTimedMetaDataAvailableListener = listener;
    }

    public void setOnErrorListener(OnErrorListener listener) {
        this.mOnErrorListener = listener;
    }

    public void setOnInfoListener(OnInfoListener listener) {
        this.mOnInfoListener = listener;
    }

    private boolean isVideoScalingModeSupported(int mode) {
        if (mode == 1 || mode == 2) {
            return true;
        }
        return false;
    }

    public void setOnDurationUpdateListener(OnDurationUpdateListener listener) {
        this.mOnDurationUpdateListener = listener;
    }
}

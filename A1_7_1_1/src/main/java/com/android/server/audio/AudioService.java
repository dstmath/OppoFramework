package com.android.server.audio;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.hdmi.HdmiPlaybackClient.DisplayStatusCallback;
import android.hardware.hdmi.HdmiTvClient;
import android.media.AudioAttributes;
import android.media.AudioDevicePort;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.AudioManagerInternal.RingerModeDelegate;
import android.media.AudioPort;
import android.media.AudioRecordingConfiguration;
import android.media.AudioRoutesInfo;
import android.media.AudioSystem;
import android.media.AudioSystem.DynamicPolicyCallback;
import android.media.AudioSystem.ErrorCallback;
import android.media.IAudioFocusDispatcher;
import android.media.IAudioRoutesObserver;
import android.media.IAudioService.Stub;
import android.media.IRecordingConfigDispatcher;
import android.media.IRingtonePlayer;
import android.media.IVolumeController;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.SoundPool;
import android.media.SoundPool.Builder;
import android.media.SoundPool.OnLoadCompleteListener;
import android.media.VolumePolicy;
import android.media.audiopolicy.AudioMix;
import android.media.audiopolicy.AudioPolicyConfig;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.UserManagerInternal.UserRestrictionsListener;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener;
import com.android.internal.util.XmlUtils;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.SystemService;
import com.android.server.am.OppoPermissionConstants;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oppo.IElsaManager;
import com.android.server.pm.UserManagerService;
import com.oppo.media.OppoMultimediaManager;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParserException;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class AudioService extends Stub {
    private static final String ASSET_FILE_VERSION = "1.0";
    private static final String ATTR_ASSET_FILE = "file";
    private static final String ATTR_ASSET_ID = "id";
    private static final String ATTR_GROUP_NAME = "name";
    private static final String ATTR_VERSION = "version";
    private static final int BTA2DP_DOCK_TIMEOUT_MILLIS = 8000;
    private static final int BT_HEADSET_CNCT_TIMEOUT_MS = 3000;
    public static final String CONNECT_INTENT_KEY_ADDRESS = "address";
    public static final String CONNECT_INTENT_KEY_DEVICE_CLASS = "class";
    public static final String CONNECT_INTENT_KEY_HAS_CAPTURE = "hasCapture";
    public static final String CONNECT_INTENT_KEY_HAS_MIDI = "hasMIDI";
    public static final String CONNECT_INTENT_KEY_HAS_PLAYBACK = "hasPlayback";
    public static final String CONNECT_INTENT_KEY_PORT_NAME = "portName";
    public static final String CONNECT_INTENT_KEY_STATE = "state";
    protected static final boolean DEBUG_AP = false;
    protected static final boolean DEBUG_DEVICES = false;
    protected static final boolean DEBUG_MODE = false;
    public static final boolean DEBUG_VOL = false;
    private static final int FLAG_ADJUST_VOLUME = 1;
    private static final String GETTXPATHENABLESTATE = "GetFmTxEnable";
    private static final String GROUP_TOUCH_SOUNDS = "touch_sounds";
    private static final int INDICATE_SYSTEM_READY_RETRY_DELAY_MS = 1000;
    private static final boolean IS_DOLBY_DAP_SUPPORT = false;
    protected static final boolean LOGD = false;
    private static int[] MAX_STREAM_VOLUME = null;
    private static int[] MIN_STREAM_VOLUME = null;
    private static final int MSG_AUDIO_SERVER_DIED = 4;
    private static final int MSG_BROADCAST_AUDIO_BECOMING_NOISY = 15;
    private static final int MSG_BROADCAST_BT_CONNECTION_STATE = 19;
    private static final int MSG_BTA2DP_DOCK_TIMEOUT = 6;
    private static final int MSG_BT_HEADSET_CNCT_FAILED = 9;
    private static final int MSG_CHECK_MUSIC_ACTIVE = 14;
    private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME = 16;
    private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME_FORCED = 17;
    private static final int MSG_DYN_POLICY_MIX_STATE_UPDATE = 25;
    private static final int MSG_FADEUP = 60;
    private static final int MSG_INDICATE_SYSTEM_READY = 26;
    private static final int MSG_LOAD_SOUND_EFFECTS = 7;
    private static final int MSG_PERSIST_MUSIC_ACTIVE_MS = 22;
    private static final int MSG_PERSIST_RINGER_MODE = 3;
    private static final int MSG_PERSIST_SAFE_VOLUME_STATE = 18;
    private static final int MSG_PERSIST_VOLUME = 1;
    private static final int MSG_PLAY_SOUND_EFFECT = 5;
    private static final int MSG_REPORT_NEW_ROUTES = 12;
    private static final int MSG_SET_A2DP_SINK_CONNECTION_STATE = 102;
    private static final int MSG_SET_A2DP_SRC_CONNECTION_STATE = 101;
    private static final int MSG_SET_ALL_VOLUMES = 10;
    private static final int MSG_SET_DEVICE_VOLUME = 0;
    private static final int MSG_SET_FORCE_BT_A2DP_USE = 13;
    private static final int MSG_SET_FORCE_USE = 8;
    private static final int MSG_SET_WIRED_DEVICE_CONNECTION_STATE = 100;
    private static final int MSG_SYSTEM_READY = 21;
    private static final int MSG_UNLOAD_SOUND_EFFECTS = 20;
    private static final int MSG_UNMUTE_STREAM = 24;
    private static final int MUSIC_ACTIVE_POLL_PERIOD_MS = 60000;
    private static final int NUM_SOUNDPOOL_CHANNELS = 4;
    private static final int PERSIST_DELAY = 500;
    private static final String[] RINGER_MODE_NAMES = null;
    private static final int SAFE_MEDIA_VOLUME_ACTIVE = 3;
    private static final int SAFE_MEDIA_VOLUME_DISABLED = 1;
    private static final int SAFE_MEDIA_VOLUME_INACTIVE = 2;
    private static final int SAFE_MEDIA_VOLUME_NOT_CONFIGURED = 0;
    private static final int SAFE_VOLUME_CONFIGURE_TIMEOUT_MS = 30000;
    private static final int SCO_MODE_MAX = 2;
    private static final int SCO_MODE_RAW = 1;
    private static final int SCO_MODE_UNDEFINED = -1;
    private static final int SCO_MODE_VIRTUAL_CALL = 0;
    private static final int SCO_MODE_VR = 2;
    private static final int SCO_STATE_ACTIVATE_REQ = 1;
    private static final int SCO_STATE_ACTIVE_EXTERNAL = 2;
    private static final int SCO_STATE_ACTIVE_INTERNAL = 3;
    private static final int SCO_STATE_DEACTIVATE_EXT_REQ = 4;
    private static final int SCO_STATE_DEACTIVATE_REQ = 5;
    private static final int SCO_STATE_INACTIVE = 0;
    private static final int SENDMSG_NOOP = 1;
    private static final int SENDMSG_QUEUE = 2;
    private static final int SENDMSG_REPLACE = 0;
    private static final String SETTXPATHDISENABLE = "SetFmTxEnable=0";
    private static final String SETTXPATHENABLE = "SetFmTxEnable=1";
    private static final int SOUND_EFFECTS_LOAD_TIMEOUT_MS = 5000;
    private static final String SOUND_EFFECTS_PATH = "/media/audio/ui/";
    private static final List<String> SOUND_EFFECT_FILES = null;
    private static final int[] STREAM_VOLUME_OPS = null;
    private static final String TAG = "AudioService";
    private static final String TAG_ASSET = "asset";
    private static final String TAG_AUDIO_ASSETS = "audio_assets";
    private static final String TAG_GROUP = "group";
    private static final String TXPATHDISABLESTATE = "GetFmTxEnable=false";
    private static final String TXPATHENABLESTATE = "GetFmTxEnable=true";
    private static final int UNMUTE_STREAM_DELAY = 350;
    private static final int UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX = 72000000;
    private static Long mLastDeviceConnectMsgTime;
    private static int sSoundEffectVolumeDb;
    private final int[][] SOUND_EFFECT_FILES_MAP;
    private final int[] STREAM_VOLUME_ALIAS_DEFAULT;
    private final int[] STREAM_VOLUME_ALIAS_TELEVISION;
    private final int[] STREAM_VOLUME_ALIAS_VOICE;
    private BluetoothA2dp mA2dp;
    private final Object mA2dpAvrcpLock;
    private boolean mAdjustVolumeAction;
    private final AppOpsManager mAppOps;
    private WakeLock mAudioEventWakeLock;
    private AudioHandler mAudioHandler;
    private AudioManager mAudioManager;
    private HashMap<IBinder, AudioPolicyProxy> mAudioPolicies;
    private int mAudioPolicyCounter;
    private final ErrorCallback mAudioSystemCallback;
    private AudioSystemThread mAudioSystemThread;
    private boolean mAvrcpAbsVolSupported;
    int mBecomingNoisyIntentDevices;
    private boolean mBluetoothA2dpEnabled;
    private final Object mBluetoothA2dpEnabledLock;
    private BluetoothHeadset mBluetoothHeadset;
    private BluetoothDevice mBluetoothHeadsetDevice;
    private ServiceListener mBluetoothProfileServiceListener;
    private Boolean mCameraSoundForced;
    private final ArrayMap<String, DeviceListSpec> mConnectedDevices;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final ControllerService mControllerService;
    final AudioRoutesInfo mCurAudioRoutes;
    private int mDeviceOrientation;
    private String mDockAddress;
    private boolean mDockAudioMediaEnabled;
    private int mDockState;
    private final DynamicPolicyCallback mDynPolicyCallback;
    private int mFadeInCurrentVolume;
    private int mFadeInDevice;
    private boolean mFadeInFinish;
    private int mFadeInMusicVolume;
    int mFixedVolumeDevices;
    private ForceControlStreamClient mForceControlStreamClient;
    private final Object mForceControlStreamLock;
    private int mForcedUseForComm;
    int mFullVolumeDevices;
    private final boolean mHasVibrator;
    private boolean mHdmiCecSink;
    private MyDisplayStatusCallback mHdmiDisplayStatusCallback;
    private HdmiControlManager mHdmiManager;
    private HdmiPlaybackClient mHdmiPlaybackClient;
    private boolean mHdmiSystemAudioSupported;
    private HdmiTvClient mHdmiTvClient;
    private boolean mIsExportVersion;
    private boolean mIsInSetCallMode;
    private long mLoweredFromNormalToVibrateTime;
    private int mMcc;
    private final MediaFocusControl mMediaFocusControl;
    private int mMode;
    private final boolean mMonitorOrientation;
    private final boolean mMonitorRotation;
    private int mMusicActiveMs;
    private int mMuteAffectedStreams;
    private NotificationManager mNm;
    private StreamVolumeCommand mPendingVolumeCommand;
    private final int mPlatformType;
    private int mPrevVolDirection;
    private final BroadcastReceiver mReceiver;
    private final RecordingActivityMonitor mRecordMonitor;
    private int mRingerMode;
    private int mRingerModeAffectedStreams;
    private RingerModeDelegate mRingerModeDelegate;
    private int mRingerModeExternal;
    private int mRingerModeMutedStreams;
    private volatile IRingtonePlayer mRingtonePlayer;
    private ArrayList<RmtSbmxFullVolDeathHandler> mRmtSbmxFullVolDeathHandlers;
    private int mRmtSbmxFullVolRefCount;
    final RemoteCallbackList<IAudioRoutesObserver> mRoutesObservers;
    private final int mSafeMediaVolumeDevices;
    private int mSafeMediaVolumeIndex;
    private Integer mSafeMediaVolumeState;
    private int mScoAudioMode;
    private int mScoAudioState;
    private final ArrayList<ScoClient> mScoClients;
    private int mScoConnectionState;
    private final ArrayList<SetModeDeathHandler> mSetModeDeathHandlers;
    private final Object mSettingCallLock;
    private final Object mSettingsLock;
    private SettingsObserver mSettingsObserver;
    private final Object mSoundEffectsLock;
    private SoundPool mSoundPool;
    private SoundPoolCallback mSoundPoolCallBack;
    private SoundPoolListenerThread mSoundPoolListenerThread;
    private Looper mSoundPoolLooper;
    private VolumeStreamState[] mStreamStates;
    private int[] mStreamVolumeAlias;
    private boolean mSystemReady;
    private final boolean mUseFixedVolume;
    private final UserManagerInternal mUserManagerInternal;
    private final UserRestrictionsListener mUserRestrictionsListener;
    private boolean mUserSwitchedReceived;
    private int mVibrateSetting;
    private int mVolumeControlStream;
    private final VolumeController mVolumeController;
    private VolumePolicy mVolumePolicy;

    private class AudioHandler extends Handler {
        /* synthetic */ AudioHandler(AudioService this$0, AudioHandler audioHandler) {
            this();
        }

        private AudioHandler() {
        }

        private void setDeviceVolume(VolumeStreamState streamState, int device) {
            synchronized (VolumeStreamState.class) {
                streamState.applyDeviceVolume_syncVSS(device);
                int streamType = AudioSystem.getNumStreamTypes() - 1;
                while (streamType >= 0) {
                    if (streamType != streamState.mStreamType && AudioService.this.mStreamVolumeAlias[streamType] == streamState.mStreamType) {
                        int streamDevice = AudioService.this.getDeviceForStream(streamType);
                        if (!(device == streamDevice || !AudioService.this.mAvrcpAbsVolSupported || (device & 896) == 0)) {
                            AudioService.this.mStreamStates[streamType].applyDeviceVolume_syncVSS(device);
                        }
                        AudioService.this.mStreamStates[streamType].applyDeviceVolume_syncVSS(streamDevice);
                    }
                    streamType--;
                }
            }
            AudioService.sendMsg(AudioService.this.mAudioHandler, 1, 2, device, 0, streamState, 500);
        }

        private void setAllVolumes(VolumeStreamState streamState) {
            streamState.applyAllVolumes();
            int streamType = AudioSystem.getNumStreamTypes() - 1;
            while (streamType >= 0) {
                if (streamType != streamState.mStreamType && AudioService.this.mStreamVolumeAlias[streamType] == streamState.mStreamType) {
                    AudioService.this.mStreamStates[streamType].applyAllVolumes();
                }
                streamType--;
            }
        }

        private void persistVolume(VolumeStreamState streamState, int device) {
            if (!AudioService.this.mUseFixedVolume) {
                if (!AudioService.this.isPlatformTelevision() || streamState.mStreamType == 3) {
                    System.putIntForUser(AudioService.this.mContentResolver, streamState.getSettingNameForDevice(device), (streamState.getIndex(device) + 5) / 10, -2);
                }
            }
        }

        private void persistRingerMode(int ringerMode) {
            if (!AudioService.this.mUseFixedVolume) {
                Global.putInt(AudioService.this.mContentResolver, "mode_ringer", ringerMode);
            }
        }

        /* JADX WARNING: Missing block: B:88:0x0256, code:
            if (r9 != 0) goto L_0x025a;
     */
        /* JADX WARNING: Missing block: B:89:0x0258, code:
            r10 = true;
     */
        /* JADX WARNING: Missing block: B:90:0x0259, code:
            return r10;
     */
        /* JADX WARNING: Missing block: B:91:0x025a, code:
            r10 = false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean onLoadSoundEffects() {
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (!AudioService.this.mSystemReady) {
                    Log.w(AudioService.TAG, "onLoadSoundEffects() called before boot complete");
                    return false;
                } else if (AudioService.this.mSoundPool != null) {
                    return true;
                } else {
                    int attempts;
                    AudioService.this.loadTouchSoundAssets();
                    AudioService.this.mSoundPool = new Builder().setMaxStreams(4).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
                    AudioService.this.mSoundPoolCallBack = null;
                    AudioService.this.mSoundPoolListenerThread = new SoundPoolListenerThread();
                    AudioService.this.mSoundPoolListenerThread.start();
                    int attempts2 = 3;
                    while (true) {
                        attempts = attempts2;
                        if (AudioService.this.mSoundPoolCallBack != null) {
                            attempts2 = attempts;
                            break;
                        }
                        attempts2 = attempts - 1;
                        if (attempts > 0) {
                            try {
                                AudioService.this.mSoundEffectsLock.wait(5000);
                            } catch (InterruptedException e) {
                                Log.w(AudioService.TAG, "Interrupted while waiting sound pool listener thread.");
                            }
                        }
                    }
                    if (AudioService.this.mSoundPoolCallBack == null) {
                        Log.w(AudioService.TAG, "onLoadSoundEffects() SoundPool listener or thread creation error");
                        if (AudioService.this.mSoundPoolLooper != null) {
                            AudioService.this.mSoundPoolLooper.quit();
                            AudioService.this.mSoundPoolLooper = null;
                        }
                        AudioService.this.mSoundPoolListenerThread = null;
                        AudioService.this.mSoundPool.release();
                        AudioService.this.mSoundPool = null;
                        return false;
                    }
                    int effect;
                    int status;
                    int[] poolId = new int[AudioService.SOUND_EFFECT_FILES.size()];
                    for (int fileIdx = 0; fileIdx < AudioService.SOUND_EFFECT_FILES.size(); fileIdx++) {
                        poolId[fileIdx] = -1;
                    }
                    int numSamples = 0;
                    for (effect = 0; effect < 10; effect++) {
                        if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] != 0) {
                            if (poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] == -1) {
                                String filePath = Environment.getRootDirectory() + AudioService.SOUND_EFFECTS_PATH + ((String) AudioService.SOUND_EFFECT_FILES.get(AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]));
                                int sampleId = AudioService.this.mSoundPool.load(filePath, 0);
                                if (sampleId <= 0) {
                                    Log.w(AudioService.TAG, "Soundpool could not load file: " + filePath);
                                } else {
                                    AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] = sampleId;
                                    poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] = sampleId;
                                    numSamples++;
                                }
                            } else {
                                AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] = poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]];
                            }
                        }
                    }
                    if (numSamples > 0) {
                        AudioService.this.mSoundPoolCallBack.setSamples(poolId);
                        attempts2 = 3;
                        status = 1;
                        while (true) {
                            attempts = attempts2;
                            if (status != 1) {
                                attempts2 = attempts;
                                break;
                            }
                            attempts2 = attempts - 1;
                            if (attempts > 0) {
                                try {
                                    AudioService.this.mSoundEffectsLock.wait(5000);
                                    status = AudioService.this.mSoundPoolCallBack.status();
                                } catch (InterruptedException e2) {
                                    Log.w(AudioService.TAG, "Interrupted while waiting sound pool callback.");
                                }
                            }
                        }
                    } else {
                        status = -1;
                    }
                    if (AudioService.this.mSoundPoolLooper != null) {
                        AudioService.this.mSoundPoolLooper.quit();
                        AudioService.this.mSoundPoolLooper = null;
                    }
                    AudioService.this.mSoundPoolListenerThread = null;
                    if (status != 0) {
                        Log.w(AudioService.TAG, "onLoadSoundEffects(), Error " + status + " while loading samples");
                        for (effect = 0; effect < 10; effect++) {
                            if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] > 0) {
                                AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] = -1;
                            }
                        }
                        AudioService.this.mSoundPool.release();
                        AudioService.this.mSoundPool = null;
                    }
                }
            }
        }

        private void onUnloadSoundEffects() {
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (AudioService.this.mSoundPool == null) {
                    return;
                }
                int[] poolId = new int[AudioService.SOUND_EFFECT_FILES.size()];
                for (int fileIdx = 0; fileIdx < AudioService.SOUND_EFFECT_FILES.size(); fileIdx++) {
                    poolId[fileIdx] = 0;
                }
                int effect = 0;
                while (effect < 10) {
                    if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] > 0 && poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] == 0) {
                        AudioService.this.mSoundPool.unload(AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1]);
                        AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] = -1;
                        poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] = -1;
                    }
                    effect++;
                }
                AudioService.this.mSoundPool.release();
                AudioService.this.mSoundPool = null;
            }
        }

        private void onPlaySoundEffect(int effectType, int volume) {
            synchronized (AudioService.this.mSoundEffectsLock) {
                onLoadSoundEffects();
                if (AudioService.this.mSoundPool == null) {
                    return;
                }
                float volFloat;
                if (volume < 0) {
                    volFloat = (float) Math.pow(10.0d, (double) (((float) AudioService.sSoundEffectVolumeDb) / 20.0f));
                } else {
                    volFloat = ((float) volume) / 1000.0f;
                }
                if (AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][1] > 0) {
                    AudioService.this.mSoundPool.play(AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][1], volFloat, volFloat, 0, 0, 1.0f);
                } else {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(Environment.getRootDirectory() + AudioService.SOUND_EFFECTS_PATH + ((String) AudioService.SOUND_EFFECT_FILES.get(AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][0])));
                        mediaPlayer.setAudioStreamType(1);
                        mediaPlayer.prepare();
                        mediaPlayer.setVolume(volFloat);
                        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                AudioHandler.this.cleanupPlayer(mp);
                            }
                        });
                        mediaPlayer.setOnErrorListener(new OnErrorListener() {
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                AudioHandler.this.cleanupPlayer(mp);
                                return true;
                            }
                        });
                        mediaPlayer.start();
                    } catch (IOException ex) {
                        Log.w(AudioService.TAG, "MediaPlayer IOException: " + ex);
                    } catch (IllegalArgumentException ex2) {
                        Log.w(AudioService.TAG, "MediaPlayer IllegalArgumentException: " + ex2);
                    } catch (IllegalStateException ex3) {
                        Log.w(AudioService.TAG, "MediaPlayer IllegalStateException: " + ex3);
                    }
                }
            }
        }

        private void cleanupPlayer(MediaPlayer mp) {
            if (mp != null) {
                try {
                    mp.stop();
                    mp.release();
                } catch (IllegalStateException ex) {
                    Log.w(AudioService.TAG, "MediaPlayer IllegalStateException: " + ex);
                }
            }
        }

        private void setForceUse(int usage, int config) {
            synchronized (AudioService.this.mConnectedDevices) {
                AudioService.this.setForceUseInt_SyncDevices(usage, config);
            }
        }

        private void onPersistSafeVolumeState(int state) {
            Global.putInt(AudioService.this.mContentResolver, "audio_safe_volume_state", state);
        }

        private String findDeviceUniqueId(int device) {
            switch (device) {
                case 128:
                case 256:
                case 512:
                    return AudioService.this.mCurAudioRoutes.bluetoothName.toString();
                case DumpState.DUMP_PREFERRED_XML /*8192*/:
                case 16384:
                    DeviceListSpec deviceSpec = (DeviceListSpec) AudioService.this.mConnectedDevices.get(Integer.valueOf(device));
                    return deviceSpec == null ? IElsaManager.EMPTY_PACKAGE : deviceSpec.mDeviceAddress;
                default:
                    return IElsaManager.EMPTY_PACKAGE;
            }
        }

        public void handleMessage(Message msg) {
            ArrayMap -get15;
            switch (msg.what) {
                case 0:
                    setDeviceVolume((VolumeStreamState) msg.obj, msg.arg1);
                    return;
                case 1:
                    persistVolume((VolumeStreamState) msg.obj, msg.arg1);
                    return;
                case 3:
                    persistRingerMode(AudioService.this.getRingerModeInternal());
                    return;
                case 4:
                    AudioService.this.onAudioServerDied();
                    return;
                case 5:
                    onPlaySoundEffect(msg.arg1, msg.arg2);
                    return;
                case 6:
                    -get15 = AudioService.this.mConnectedDevices;
                    synchronized (-get15) {
                        AudioService.this.makeA2dpDeviceUnavailableNow((String) msg.obj);
                        break;
                    }
                case 7:
                    boolean loaded = onLoadSoundEffects();
                    if (msg.obj != null) {
                        LoadSoundEffectReply reply = msg.obj;
                        synchronized (reply) {
                            reply.mStatus = loaded ? 0 : -1;
                            reply.notify();
                        }
                        return;
                    }
                    return;
                case 8:
                case 13:
                    setForceUse(msg.arg1, msg.arg2);
                    return;
                case 9:
                    AudioService.this.resetBluetoothSco();
                    return;
                case 10:
                    setAllVolumes((VolumeStreamState) msg.obj);
                    return;
                case 12:
                    int N = AudioService.this.mRoutesObservers.beginBroadcast();
                    if (N > 0) {
                        AudioRoutesInfo audioRoutesInfo;
                        synchronized (AudioService.this.mCurAudioRoutes) {
                            audioRoutesInfo = new AudioRoutesInfo(AudioService.this.mCurAudioRoutes);
                        }
                        while (N > 0) {
                            N--;
                            try {
                                ((IAudioRoutesObserver) AudioService.this.mRoutesObservers.getBroadcastItem(N)).dispatchAudioRoutesChanged(audioRoutesInfo);
                            } catch (RemoteException e) {
                            }
                        }
                    }
                    AudioService.this.mRoutesObservers.finishBroadcast();
                    AudioService.this.observeDevicesForStreams(-1);
                    if (AudioService.IS_DOLBY_DAP_SUPPORT) {
                        Intent intent = new Intent("DOLBY_DEVICE_CHANGE");
                        intent.addFlags(67108864);
                        int newDevice = AudioService.this.getDeviceForStream(3);
                        intent.putExtra("Device", newDevice);
                        intent.putExtra("DeviceUniqueId", findDeviceUniqueId(newDevice));
                        AudioService.this.sendStickyBroadcastToAll(intent);
                        return;
                    }
                    return;
                case 14:
                    AudioService.this.onCheckMusicActive((String) msg.obj);
                    return;
                case 15:
                    AudioService.this.onSendBecomingNoisyIntent();
                    return;
                case 16:
                case 17:
                    AudioService.this.onConfigureSafeVolume(msg.what == 17, (String) msg.obj);
                    return;
                case 18:
                    onPersistSafeVolumeState(msg.arg1);
                    return;
                case 19:
                    AudioService.this.onBroadcastScoConnectionState(msg.arg1);
                    return;
                case 20:
                    onUnloadSoundEffects();
                    return;
                case 21:
                    AudioService.this.onSystemReady();
                    return;
                case 22:
                    Secure.putIntForUser(AudioService.this.mContentResolver, "unsafe_volume_music_active_ms", msg.arg1, -2);
                    return;
                case 24:
                    AudioService.this.onUnmuteStream(msg.arg1, msg.arg2);
                    return;
                case 25:
                    AudioService.this.onDynPolicyMixStateUpdate((String) msg.obj, msg.arg1);
                    return;
                case 26:
                    AudioService.this.onIndicateSystemReady();
                    return;
                case 60:
                    -get15 = AudioService.this.mConnectedDevices;
                    synchronized (-get15) {
                        int device = AudioService.this.getDeviceForStream(3);
                        if (device == AudioService.this.mFadeInDevice) {
                            if (AudioService.this.mFadeInCurrentVolume >= AudioService.this.mFadeInMusicVolume) {
                                AudioService.this.mFadeInFinish = true;
                                break;
                            }
                            AudioService audioService = AudioService.this;
                            audioService.mFadeInCurrentVolume = audioService.mFadeInCurrentVolume + 1;
                            Log.d(AudioService.TAG, "music volume fadein : " + AudioService.this.mFadeInCurrentVolume + " -> " + AudioService.this.mFadeInMusicVolume);
                            AudioSystem.setStreamVolumeIndex(3, AudioService.this.mFadeInCurrentVolume, AudioService.this.mFadeInDevice);
                            AudioService.this.mAudioHandler.sendEmptyMessageDelayed(60, 100);
                            break;
                        }
                        Log.d(AudioService.TAG, "music volume fadein stop due to device change " + AudioService.this.mFadeInDevice + " -> " + device);
                        AudioSystem.setStreamVolumeIndex(3, AudioService.this.mFadeInMusicVolume, AudioService.this.mFadeInDevice);
                        AudioService.this.mFadeInFinish = true;
                        break;
                    }
                case 100:
                    WiredDeviceConnectionState connectState = msg.obj;
                    AudioService.this.onSetWiredDeviceConnectionState(connectState.mType, connectState.mState, connectState.mAddress, connectState.mName, connectState.mCaller);
                    AudioService.this.mAudioEventWakeLock.release();
                    return;
                case 101:
                    AudioService.this.onSetA2dpSourceConnectionState((BluetoothDevice) msg.obj, msg.arg1);
                    AudioService.this.mAudioEventWakeLock.release();
                    return;
                case 102:
                    AudioService.this.onSetA2dpSinkConnectionState((BluetoothDevice) msg.obj, msg.arg1);
                    AudioService.this.mAudioEventWakeLock.release();
                    return;
                default:
                    return;
            }
        }
    }

    private class AudioPathToFMTxDeathHandler implements DeathRecipient {
        private IBinder mCb;

        AudioPathToFMTxDeathHandler(IBinder cb) {
            this.mCb = cb;
        }

        public void binderDied() {
            Log.w(AudioService.TAG, "AudioPathToFMTxDeathHandler::binderDied");
            AudioService.this.setAudioPathOutofFMTx();
        }

        public IBinder getBinder() {
            return this.mCb;
        }
    }

    public class AudioPolicyProxy extends AudioPolicyConfig implements DeathRecipient {
        private static final String TAG = "AudioPolicyProxy";
        int mFocusDuckBehavior = 0;
        boolean mHasFocusListener;
        IAudioPolicyCallback mPolicyCallback;

        AudioPolicyProxy(AudioPolicyConfig config, IAudioPolicyCallback token, boolean hasFocusListener) {
            super(config);
            StringBuilder append = new StringBuilder().append(config.hashCode()).append(":ap:");
            int -get10 = AudioService.this.mAudioPolicyCounter;
            AudioService.this.mAudioPolicyCounter = -get10 + 1;
            setRegistration(new String(append.append(-get10).toString()));
            this.mPolicyCallback = token;
            this.mHasFocusListener = hasFocusListener;
            if (this.mHasFocusListener) {
                AudioService.this.mMediaFocusControl.addFocusFollower(this.mPolicyCallback);
            }
            connectMixes();
        }

        public void binderDied() {
            synchronized (AudioService.this.mAudioPolicies) {
                Log.i(TAG, "audio policy " + this.mPolicyCallback + " died");
                release();
                AudioService.this.mAudioPolicies.remove(this.mPolicyCallback.asBinder());
            }
        }

        String getRegistrationId() {
            return getRegistration();
        }

        void release() {
            if (this.mFocusDuckBehavior == 1) {
                AudioService.this.mMediaFocusControl.setDuckingInExtPolicyAvailable(false);
            }
            if (this.mHasFocusListener) {
                AudioService.this.mMediaFocusControl.removeFocusFollower(this.mPolicyCallback);
            }
            AudioSystem.registerPolicyMixes(this.mMixes, false);
        }

        void connectMixes() {
            AudioSystem.registerPolicyMixes(this.mMixes, true);
        }
    }

    private class AudioServiceBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ AudioServiceBroadcastReceiver(AudioService this$0, AudioServiceBroadcastReceiver audioServiceBroadcastReceiver) {
            this();
        }

        private AudioServiceBroadcastReceiver() {
        }

        /* JADX WARNING: Missing block: B:48:0x013d, code:
            if (com.android.server.audio.AudioService.-get31(r20.this$0) == 5) goto L_0x00e4;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AudioService.DEBUG_DEVICES) {
                Log.d(AudioService.TAG, "BluetoothHeadset.action:" + action + ",intent:" + intent);
            }
            int state;
            if (action.equals("android.intent.action.DOCK_EVENT")) {
                int config;
                int dockState = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
                switch (dockState) {
                    case 1:
                        config = 7;
                        break;
                    case 2:
                        config = 6;
                        break;
                    case 3:
                        config = 8;
                        break;
                    case 4:
                        config = 9;
                        break;
                    default:
                        config = 0;
                        break;
                }
                if (!(dockState == 3 || (dockState == 0 && AudioService.this.mDockState == 3))) {
                    AudioSystem.setForceUse(3, config);
                }
                AudioService.this.mDockState = dockState;
            } else if (action.equals("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED")) {
                if (AudioService.DEBUG_VOL) {
                    Log.d(AudioService.TAG, "BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED");
                }
                state = intent.getIntExtra("android.bluetooth.profile.extra.STATE", 0);
                AudioService.this.setBtScoDeviceConnectionState((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE"), state);
            } else if (action.equals("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED")) {
                boolean broadcast = false;
                int scoAudioState = -1;
                synchronized (AudioService.this.mScoClients) {
                    int btState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1);
                    if (!AudioService.this.mScoClients.isEmpty()) {
                        if (!(AudioService.this.mScoAudioState == 3 || AudioService.this.mScoAudioState == 1)) {
                        }
                        broadcast = true;
                    }
                    switch (btState) {
                        case 10:
                            scoAudioState = 0;
                            AudioService.this.mScoAudioState = 0;
                            AudioService.this.clearAllScoClients(0, false);
                            break;
                        case 11:
                            if (!(AudioService.this.mScoAudioState == 3 || AudioService.this.mScoAudioState == 5 || AudioService.this.mScoAudioState == 4)) {
                                AudioService.this.mScoAudioState = 2;
                                break;
                            }
                        case 12:
                            scoAudioState = 1;
                            if (!(AudioService.this.mScoAudioState == 3 || AudioService.this.mScoAudioState == 5 || AudioService.this.mScoAudioState == 4)) {
                                AudioService.this.mScoAudioState = 2;
                                break;
                            }
                    }
                    broadcast = false;
                }
                if (broadcast) {
                    AudioService.this.broadcastScoConnectionState(scoAudioState);
                    Intent newIntent = new Intent("android.media.SCO_AUDIO_STATE_CHANGED");
                    newIntent.putExtra("android.media.extra.SCO_AUDIO_STATE", scoAudioState);
                    AudioService.this.sendStickyBroadcastToAll(newIntent);
                }
                if (AudioService.DEBUG_VOL) {
                    Log.d(AudioService.TAG, "BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED mScoAudioState = " + AudioService.this.mScoAudioState);
                }
            } else if (action.equals("android.intent.action.SCREEN_ON")) {
                if (AudioService.this.mMonitorRotation) {
                    RotationHelper.enable();
                }
                AudioSystem.setParameters("screen_state=on");
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                if (AudioService.this.mMonitorRotation) {
                    RotationHelper.disable();
                }
                AudioSystem.setParameters("screen_state=off");
            } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                AudioService.this.handleConfigurationChanged(context);
            } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                if (AudioService.this.mUserSwitchedReceived) {
                    AudioService.sendMsg(AudioService.this.mAudioHandler, 15, 0, 0, 0, null, 0);
                }
                AudioService.this.mUserSwitchedReceived = true;
                AudioService.this.mMediaFocusControl.discardAudioFocusOwner();
                AudioService.this.readAudioSettings(true);
                AudioService.sendMsg(AudioService.this.mAudioHandler, 10, 2, 0, 0, AudioService.this.mStreamStates[3], 0);
            } else if (action.equals("android.intent.action.USER_BACKGROUND")) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userId >= 0) {
                    AudioService.this.killBackgroundUserProcessesWithRecordAudioPermission(UserManagerService.getInstance().getUserInfo(userId));
                }
                UserManagerService.getInstance().setUserRestriction("no_record_audio", true, userId);
            } else if (action.equals("android.intent.action.USER_FOREGROUND")) {
                UserManagerService.getInstance().setUserRestriction("no_record_audio", false, intent.getIntExtra("android.intent.extra.user_handle", -1));
            } else if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                if (state == 10 || state == 13) {
                    AudioService.this.disconnectAllBluetoothProfiles();
                }
            } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                boolean enabled = intent.getBooleanExtra("state", false);
                Log.d(AudioService.TAG, "ACTION_AIRPLANE_MODE_CHANGED enabled =" + enabled);
                if (enabled) {
                    AudioSystem.setParameters("SetFlightMode=1");
                } else {
                    AudioSystem.setParameters("SetFlightMode=0");
                }
            }
        }
    }

    final class AudioServiceInternal extends AudioManagerInternal {
        AudioServiceInternal() {
        }

        public void setRingerModeDelegate(RingerModeDelegate delegate) {
            AudioService.this.mRingerModeDelegate = delegate;
            if (AudioService.this.mRingerModeDelegate != null) {
                AudioService.this.updateRingerModeAffectedStreams();
                setRingerModeInternal(getRingerModeInternal(), "AudioService.setRingerModeDelegate");
            }
        }

        public void adjustSuggestedStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            AudioService.this.adjustSuggestedStreamVolume(direction, streamType, flags, callingPackage, callingPackage, uid);
        }

        public void adjustStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            AudioService.this.adjustStreamVolume(streamType, direction, flags, callingPackage, callingPackage, uid);
        }

        public void setStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            AudioService.this.setStreamVolume(streamType, direction, flags, callingPackage, callingPackage, uid);
        }

        public int getRingerModeInternal() {
            return AudioService.this.getRingerModeInternal();
        }

        public void setRingerModeInternal(int ringerMode, String caller) {
            AudioService.this.setRingerModeInternal(ringerMode, caller);
        }

        public int getVolumeControllerUid() {
            return AudioService.this.mControllerService.mUid;
        }

        public void updateRingerModeAffectedStreamsInternal() {
            synchronized (AudioService.this.mSettingsLock) {
                if (AudioService.this.updateRingerModeAffectedStreams()) {
                    AudioService.this.setRingerModeInt(getRingerModeInternal(), false);
                }
            }
        }
    }

    private class AudioServiceUserRestrictionsListener implements UserRestrictionsListener {
        /* synthetic */ AudioServiceUserRestrictionsListener(AudioService this$0, AudioServiceUserRestrictionsListener audioServiceUserRestrictionsListener) {
            this();
        }

        private AudioServiceUserRestrictionsListener() {
        }

        public void onUserRestrictionsChanged(int userId, Bundle newRestrictions, Bundle prevRestrictions) {
            boolean wasRestricted = prevRestrictions.getBoolean("no_unmute_microphone");
            boolean isRestricted = newRestrictions.getBoolean("no_unmute_microphone");
            if (wasRestricted != isRestricted) {
                AudioService.this.setMicrophoneMuteNoCallerCheck(isRestricted, userId);
            }
            if (prevRestrictions.getBoolean("no_adjust_volume")) {
                wasRestricted = true;
            } else {
                wasRestricted = prevRestrictions.getBoolean("disallow_unmute_device");
            }
            if (newRestrictions.getBoolean("no_adjust_volume")) {
                isRestricted = true;
            } else {
                isRestricted = newRestrictions.getBoolean("disallow_unmute_device");
            }
            if (wasRestricted != isRestricted) {
                AudioService.this.setMasterMuteInternalNoCallerCheck(isRestricted, 0, userId);
            }
        }
    }

    private class AudioSystemThread extends Thread {
        AudioSystemThread() {
            super(AudioService.TAG);
        }

        public void run() {
            Looper.prepare();
            synchronized (AudioService.this) {
                AudioService.this.mAudioHandler = new AudioHandler(AudioService.this, null);
                AudioService.this.notify();
            }
            Looper.loop();
        }
    }

    private class ControllerService extends ContentObserver {
        private ComponentName mComponent;
        private int mUid;

        public ControllerService() {
            super(null);
        }

        public String toString() {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(this.mUid);
            objArr[1] = this.mComponent;
            return String.format("{mUid=%d,mComponent=%s}", objArr);
        }

        public void init() {
            onChange(true);
            AudioService.this.mContentResolver.registerContentObserver(Secure.getUriFor("volume_controller_service_component"), false, this);
        }

        public void onChange(boolean selfChange) {
            this.mUid = 0;
            this.mComponent = null;
            String setting = Secure.getString(AudioService.this.mContentResolver, "volume_controller_service_component");
            if (setting != null) {
                try {
                    this.mComponent = ComponentName.unflattenFromString(setting);
                    if (this.mComponent != null) {
                        this.mUid = AudioService.this.mContext.getPackageManager().getApplicationInfo(this.mComponent.getPackageName(), 0).uid;
                        if (AudioService.DEBUG_VOL) {
                            Log.d(AudioService.TAG, "Reloaded controller service: " + this);
                        }
                    }
                } catch (Exception e) {
                    Log.w(AudioService.TAG, "Error loading controller service", e);
                }
            }
        }
    }

    private class DeviceListSpec {
        String mDeviceAddress;
        String mDeviceName;
        int mDeviceType;

        public DeviceListSpec(int deviceType, String deviceName, String deviceAddress) {
            this.mDeviceType = deviceType;
            this.mDeviceName = deviceName;
            this.mDeviceAddress = deviceAddress;
        }

        public String toString() {
            return "[type:0x" + Integer.toHexString(this.mDeviceType) + " name:" + this.mDeviceName + " address:" + this.mDeviceAddress + "]";
        }
    }

    private class ForceControlStreamClient implements DeathRecipient {
        private IBinder mCb;

        ForceControlStreamClient(IBinder cb) {
            if (cb != null) {
                try {
                    cb.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    Log.w(AudioService.TAG, "ForceControlStreamClient() could not link to " + cb + " binder death");
                    cb = null;
                }
            }
            this.mCb = cb;
        }

        public void binderDied() {
            synchronized (AudioService.this.mForceControlStreamLock) {
                Log.w(AudioService.TAG, "SCO client died");
                if (AudioService.this.mForceControlStreamClient != this) {
                    Log.w(AudioService.TAG, "unregistered control stream client died");
                } else {
                    AudioService.this.mForceControlStreamClient = null;
                    AudioService.this.mVolumeControlStream = -1;
                }
            }
        }

        public void release() {
            if (this.mCb != null) {
                this.mCb.unlinkToDeath(this, 0);
                this.mCb = null;
            }
        }
    }

    public static final class Lifecycle extends SystemService {
        private AudioService mService;

        public Lifecycle(Context context) {
            super(context);
            this.mService = new AudioService(context);
        }

        public void onStart() {
            publishBinderService("audio", this.mService);
        }

        public void onBootPhase(int phase) {
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mService.systemReady();
            }
        }
    }

    class LoadSoundEffectReply {
        public int mStatus = 1;

        LoadSoundEffectReply() {
        }
    }

    private class MyDisplayStatusCallback implements DisplayStatusCallback {
        /* synthetic */ MyDisplayStatusCallback(AudioService this$0, MyDisplayStatusCallback myDisplayStatusCallback) {
            this();
        }

        private MyDisplayStatusCallback() {
        }

        public void onComplete(int status) {
            if (AudioService.this.mHdmiManager != null) {
                synchronized (AudioService.this.mHdmiManager) {
                    AudioService.this.mHdmiCecSink = status != -1;
                    if (AudioService.this.isPlatformTelevision() && !AudioService.this.mHdmiCecSink) {
                        AudioService audioService = AudioService.this;
                        audioService.mFixedVolumeDevices &= -1025;
                    }
                    AudioService.this.checkAllFixedVolumeDevices();
                }
            }
        }
    }

    private class RmtSbmxFullVolDeathHandler implements DeathRecipient {
        private IBinder mICallback;

        RmtSbmxFullVolDeathHandler(IBinder cb) {
            this.mICallback = cb;
            try {
                cb.linkToDeath(this, 0);
            } catch (RemoteException e) {
                Log.e(AudioService.TAG, "can't link to death", e);
            }
        }

        boolean isHandlerFor(IBinder cb) {
            return this.mICallback.equals(cb);
        }

        void forget() {
            try {
                this.mICallback.unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
                Log.e(AudioService.TAG, "error unlinking to death", e);
            }
        }

        public void binderDied() {
            Log.w(AudioService.TAG, "Recorder with remote submix at full volume died " + this.mICallback);
            AudioService.this.forceRemoteSubmixFullVolume(false, this.mICallback);
        }
    }

    private class ScoClient implements DeathRecipient {
        private IBinder mCb;
        private int mCreatorPid = Binder.getCallingPid();
        private int mStartcount = 0;

        ScoClient(IBinder cb) {
            this.mCb = cb;
        }

        public void binderDied() {
            synchronized (AudioService.this.mScoClients) {
                Log.w(AudioService.TAG, "SCO client died");
                if (AudioService.this.mScoClients.indexOf(this) < 0) {
                    Log.w(AudioService.TAG, "unregistered SCO client died");
                } else {
                    clearCount(true);
                    AudioService.this.mScoClients.remove(this);
                }
            }
        }

        public void incCount(int scoAudioMode) {
            synchronized (AudioService.this.mScoClients) {
                requestScoState(12, scoAudioMode);
                if (this.mStartcount == 0) {
                    try {
                        this.mCb.linkToDeath(this, 0);
                    } catch (RemoteException e) {
                        Log.w(AudioService.TAG, "ScoClient  incCount() could not link to " + this.mCb + " binder death");
                    }
                }
                this.mStartcount++;
            }
            return;
        }

        public void decCount() {
            synchronized (AudioService.this.mScoClients) {
                if (this.mStartcount == 0) {
                    Log.w(AudioService.TAG, "ScoClient.decCount() already 0");
                } else {
                    this.mStartcount--;
                    if (this.mStartcount == 0) {
                        try {
                            this.mCb.unlinkToDeath(this, 0);
                        } catch (NoSuchElementException e) {
                            Log.w(AudioService.TAG, "decCount() going to 0 but not registered to binder");
                        }
                    }
                    requestScoState(10, 0);
                }
            }
            return;
        }

        public void clearCount(boolean stopSco) {
            synchronized (AudioService.this.mScoClients) {
                if (this.mStartcount != 0) {
                    try {
                        this.mCb.unlinkToDeath(this, 0);
                    } catch (NoSuchElementException e) {
                        Log.w(AudioService.TAG, "clearCount() mStartcount: " + this.mStartcount + " != 0 but not registered to binder");
                    }
                }
                this.mStartcount = 0;
                if (stopSco) {
                    requestScoState(10, 0);
                }
            }
            return;
        }

        public int getCount() {
            return this.mStartcount;
        }

        public IBinder getBinder() {
            return this.mCb;
        }

        public int getPid() {
            return this.mCreatorPid;
        }

        public int totalCount() {
            int count;
            synchronized (AudioService.this.mScoClients) {
                count = 0;
                for (int i = 0; i < AudioService.this.mScoClients.size(); i++) {
                    count += ((ScoClient) AudioService.this.mScoClients.get(i)).getCount();
                }
            }
            return count;
        }

        private void requestScoState(int state, int scoAudioMode) {
            if (AudioService.DEBUG_VOL) {
                Log.d(AudioService.TAG, "requestScoState:state = " + state + ", scoAudioMode =" + scoAudioMode);
            }
            AudioService.this.checkScoAudioState();
            if (totalCount() != 0) {
                return;
            }
            boolean status;
            if (state == 12) {
                AudioService.this.broadcastScoConnectionState(2);
                synchronized (AudioService.this.mSetModeDeathHandlers) {
                    if ((!AudioService.this.mSetModeDeathHandlers.isEmpty() && ((SetModeDeathHandler) AudioService.this.mSetModeDeathHandlers.get(0)).getPid() != this.mCreatorPid) || (AudioService.this.mScoAudioState != 0 && AudioService.this.mScoAudioState != 5)) {
                        AudioService.this.broadcastScoConnectionState(0);
                    } else if (AudioService.this.mScoAudioState == 0) {
                        AudioService.this.mScoAudioMode = scoAudioMode;
                        if (scoAudioMode == -1) {
                            if (AudioService.DEBUG_DEVICES) {
                                Log.d(AudioService.TAG, "requestScoState:mBluetoothHeadsetDevice=" + AudioService.this.mBluetoothHeadsetDevice + ",mBluetoothHeadset:" + AudioService.this.mBluetoothHeadset);
                            }
                            if (AudioService.this.mBluetoothHeadsetDevice == null && AudioService.this.mBluetoothHeadset != null) {
                                List<BluetoothDevice> devices = AudioService.this.mBluetoothHeadset.getConnectedDevices();
                                if (devices.size() > 0) {
                                    AudioService.this.mBluetoothHeadsetDevice = (BluetoothDevice) devices.get(0);
                                }
                            }
                            if (AudioService.DEBUG_DEVICES) {
                                Log.d(AudioService.TAG, "requestScoState:mBluetoothHeadsetDevice = " + AudioService.this.mBluetoothHeadsetDevice);
                            }
                            if (AudioService.this.mBluetoothHeadsetDevice != null) {
                                AudioService.this.mScoAudioMode = new Integer(Global.getInt(AudioService.this.mContentResolver, "bluetooth_sco_channel_" + AudioService.this.mBluetoothHeadsetDevice.getAddress(), 0)).intValue();
                                if (AudioService.this.mScoAudioMode > 2 || AudioService.this.mScoAudioMode < 0) {
                                    AudioService.this.mScoAudioMode = 0;
                                }
                            } else {
                                AudioService.this.mScoAudioMode = 1;
                            }
                        }
                        if (AudioService.this.mBluetoothHeadset != null && AudioService.this.mBluetoothHeadsetDevice != null) {
                            status = false;
                            if (AudioService.this.mScoAudioMode == 1) {
                                status = AudioService.this.mBluetoothHeadset.connectAudio();
                            } else if (AudioService.this.mScoAudioMode == 0) {
                                status = AudioService.this.mBluetoothHeadset.startScoUsingVirtualVoiceCall(AudioService.this.mBluetoothHeadsetDevice);
                            } else if (AudioService.this.mScoAudioMode == 2) {
                                status = AudioService.this.mBluetoothHeadset.startVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                            }
                            if (status) {
                                AudioService.this.mScoAudioState = 3;
                            } else {
                                AudioService.this.broadcastScoConnectionState(0);
                            }
                        } else if (AudioService.this.getBluetoothHeadset()) {
                            AudioService.this.mScoAudioState = 1;
                        }
                    } else {
                        AudioService.this.mScoAudioState = 3;
                        AudioService.this.broadcastScoConnectionState(1);
                    }
                }
            } else if (state != 10) {
            } else {
                if (AudioService.this.mScoAudioState != 3 && AudioService.this.mScoAudioState != 1) {
                    return;
                }
                if (AudioService.this.mScoAudioState != 3) {
                    AudioService.this.mScoAudioState = 0;
                    AudioService.this.broadcastScoConnectionState(0);
                } else if (AudioService.this.mBluetoothHeadset != null && AudioService.this.mBluetoothHeadsetDevice != null) {
                    status = false;
                    if (AudioService.this.mScoAudioMode == 1) {
                        status = AudioService.this.mBluetoothHeadset.disconnectAudio();
                    } else if (AudioService.this.mScoAudioMode == 0) {
                        status = AudioService.this.mBluetoothHeadset.stopScoUsingVirtualVoiceCall(AudioService.this.mBluetoothHeadsetDevice);
                    } else if (AudioService.this.mScoAudioMode == 2) {
                        status = AudioService.this.mBluetoothHeadset.stopVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                    }
                    if (!status) {
                        AudioService.this.mScoAudioState = 0;
                        AudioService.this.broadcastScoConnectionState(0);
                    }
                } else if (AudioService.this.getBluetoothHeadset()) {
                    AudioService.this.mScoAudioState = 5;
                }
            }
        }
    }

    private class SetModeDeathHandler implements DeathRecipient {
        private IBinder mCb;
        private int mMode = 0;
        private int mPid;

        SetModeDeathHandler(IBinder cb, int pid) {
            this.mCb = cb;
            this.mPid = pid;
        }

        public void binderDied() {
            int newModeOwnerPid = 0;
            synchronized (AudioService.this.mSetModeDeathHandlers) {
                Log.w(AudioService.TAG, "setMode() client died");
                if (AudioService.this.mSetModeDeathHandlers.indexOf(this) < 0) {
                    Log.w(AudioService.TAG, "unregistered setMode() client died");
                } else {
                    newModeOwnerPid = AudioService.this.setModeInt(0, this.mCb, this.mPid, AudioService.TAG);
                }
            }
            if (newModeOwnerPid != 0) {
                long ident = Binder.clearCallingIdentity();
                AudioService.this.disconnectBluetoothSco(newModeOwnerPid);
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getPid() {
            return this.mPid;
        }

        public void setMode(int mode) {
            this.mMode = mode;
        }

        public int getMode() {
            return this.mMode;
        }

        public IBinder getBinder() {
            return this.mCb;
        }
    }

    private class SettingsObserver extends ContentObserver {
        private int mEncodedSurroundMode;

        SettingsObserver() {
            super(new Handler());
            AudioService.this.mContentResolver.registerContentObserver(System.getUriFor("mode_ringer_streams_affected"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Global.getUriFor("dock_audio_media_enabled"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(System.getUriFor("master_mono"), false, this);
            this.mEncodedSurroundMode = Global.getInt(AudioService.this.mContentResolver, "encoded_surround_output", 0);
            AudioService.this.mContentResolver.registerContentObserver(Global.getUriFor("encoded_surround_output"), false, this);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (AudioService.this.mSettingsLock) {
                if (AudioService.this.updateRingerModeAffectedStreams()) {
                    AudioService.this.setRingerModeInt(AudioService.this.getRingerModeInternal(), false);
                }
                AudioService.this.readDockAudioSettings(AudioService.this.mContentResolver);
                AudioService.this.updateMasterMono(AudioService.this.mContentResolver);
                updateEncodedSurroundOutput();
            }
        }

        private void updateEncodedSurroundOutput() {
            int newSurroundMode = Global.getInt(AudioService.this.mContentResolver, "encoded_surround_output", 0);
            if (this.mEncodedSurroundMode != newSurroundMode) {
                AudioService.this.sendEncodedSurroundMode(newSurroundMode);
                synchronized (AudioService.this.mConnectedDevices) {
                    if (((DeviceListSpec) AudioService.this.mConnectedDevices.get(AudioService.this.makeDeviceListKey(1024, IElsaManager.EMPTY_PACKAGE))) != null) {
                        AudioService.this.setWiredDeviceConnectionState(1024, 0, IElsaManager.EMPTY_PACKAGE, IElsaManager.EMPTY_PACKAGE, "android");
                        AudioService.this.setWiredDeviceConnectionState(1024, 1, IElsaManager.EMPTY_PACKAGE, IElsaManager.EMPTY_PACKAGE, "android");
                    }
                }
                this.mEncodedSurroundMode = newSurroundMode;
            }
        }
    }

    private final class SoundPoolCallback implements OnLoadCompleteListener {
        List<Integer> mSamples;
        int mStatus;

        /* synthetic */ SoundPoolCallback(AudioService this$0, SoundPoolCallback soundPoolCallback) {
            this();
        }

        private SoundPoolCallback() {
            this.mStatus = 1;
            this.mSamples = new ArrayList();
        }

        public int status() {
            return this.mStatus;
        }

        public void setSamples(int[] samples) {
            for (int i = 0; i < samples.length; i++) {
                if (samples[i] > 0) {
                    this.mSamples.add(Integer.valueOf(samples[i]));
                }
            }
        }

        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            synchronized (AudioService.this.mSoundEffectsLock) {
                int i = this.mSamples.indexOf(Integer.valueOf(sampleId));
                if (i >= 0) {
                    this.mSamples.remove(i);
                }
                if (status != 0 || this.mSamples.isEmpty()) {
                    this.mStatus = status;
                    AudioService.this.mSoundEffectsLock.notify();
                }
            }
        }
    }

    class SoundPoolListenerThread extends Thread {
        public SoundPoolListenerThread() {
            super("SoundPoolListenerThread");
        }

        public void run() {
            Looper.prepare();
            AudioService.this.mSoundPoolLooper = Looper.myLooper();
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (AudioService.this.mSoundPool != null) {
                    AudioService.this.mSoundPoolCallBack = new SoundPoolCallback(AudioService.this, null);
                    AudioService.this.mSoundPool.setOnLoadCompleteListener(AudioService.this.mSoundPoolCallBack);
                }
                AudioService.this.mSoundEffectsLock.notify();
            }
            Looper.loop();
        }
    }

    private static class StreamOverride implements TouchExplorationStateChangeListener {
        private static final int DEFAULT_STREAM_TYPE_OVERRIDE_DELAY_MS = 0;
        private static final int TOUCH_EXPLORE_STREAM_TYPE_OVERRIDE_DELAY_MS = 1000;
        static int sDelayMs;

        private StreamOverride() {
        }

        static void init(Context ctxt) {
            AccessibilityManager accessibilityManager = (AccessibilityManager) ctxt.getSystemService("accessibility");
            updateDefaultStreamOverrideDelay(accessibilityManager.isTouchExplorationEnabled());
            accessibilityManager.addTouchExplorationStateChangeListener(new StreamOverride());
        }

        public void onTouchExplorationStateChanged(boolean enabled) {
            updateDefaultStreamOverrideDelay(enabled);
        }

        private static void updateDefaultStreamOverrideDelay(boolean touchExploreEnabled) {
            if (touchExploreEnabled) {
                sDelayMs = 1000;
            } else {
                sDelayMs = 0;
            }
            if (AudioService.DEBUG_VOL) {
                Log.d(AudioService.TAG, "Touch exploration enabled=" + touchExploreEnabled + " stream override delay is now " + sDelayMs + " ms");
            }
        }
    }

    class StreamVolumeCommand {
        public final int mDevice;
        public final int mFlags;
        public final int mIndex;
        public final int mStreamType;

        StreamVolumeCommand(int streamType, int index, int flags, int device) {
            this.mStreamType = streamType;
            this.mIndex = index;
            this.mFlags = flags;
            this.mDevice = device;
        }

        public String toString() {
            return "{streamType=" + this.mStreamType + ",index=" + this.mIndex + ",flags=" + this.mFlags + ",device=" + this.mDevice + '}';
        }
    }

    public static class VolumeController {
        private static final String TAG = "VolumeController";
        private IVolumeController mController;
        private int mLongPressTimeout;
        private long mNextLongPress;
        private boolean mVisible;

        public void setController(IVolumeController controller) {
            this.mController = controller;
            this.mVisible = false;
        }

        public void loadSettings(ContentResolver cr) {
            this.mLongPressTimeout = Secure.getIntForUser(cr, "long_press_timeout", 500, -2);
        }

        public boolean suppressAdjustment(int resolvedStream, int flags, boolean isMute) {
            if (isMute) {
                return false;
            }
            boolean suppress = false;
            if (resolvedStream == 2 && this.mController != null) {
                long now = SystemClock.uptimeMillis();
                if ((flags & 1) != 0 && !this.mVisible) {
                    if (this.mNextLongPress < now) {
                        this.mNextLongPress = ((long) this.mLongPressTimeout) + now;
                    }
                    suppress = true;
                } else if (this.mNextLongPress > 0) {
                    if (now > this.mNextLongPress) {
                        this.mNextLongPress = 0;
                    } else {
                        suppress = true;
                    }
                }
            }
            return suppress;
        }

        public void setVisible(boolean visible) {
            this.mVisible = visible;
        }

        public boolean isSameBinder(IVolumeController controller) {
            return Objects.equals(asBinder(), binder(controller));
        }

        public IBinder asBinder() {
            return binder(this.mController);
        }

        private static IBinder binder(IVolumeController controller) {
            return controller == null ? null : controller.asBinder();
        }

        public String toString() {
            return "VolumeController(" + asBinder() + ",mVisible=" + this.mVisible + ")";
        }

        public void postDisplaySafeVolumeWarning(int flags) {
            if (this.mController != null) {
                try {
                    this.mController.displaySafeVolumeWarning(flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling displaySafeVolumeWarning", e);
                }
            }
        }

        public void postVolumeChanged(int streamType, int flags) {
            if (this.mController != null) {
                try {
                    this.mController.volumeChanged(streamType, flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling volumeChanged", e);
                }
            }
        }

        public void postMasterMuteChanged(int flags) {
            if (this.mController != null) {
                try {
                    this.mController.masterMuteChanged(flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling masterMuteChanged", e);
                }
            }
        }

        public void setLayoutDirection(int layoutDirection) {
            if (this.mController != null) {
                try {
                    this.mController.setLayoutDirection(layoutDirection);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling setLayoutDirection", e);
                }
            }
        }

        public void postDismiss() {
            if (this.mController != null) {
                try {
                    this.mController.dismiss();
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling dismiss", e);
                }
            }
        }
    }

    public class VolumeStreamState {
        private final SparseIntArray mIndexMap;
        private final int mIndexMax;
        private final int mIndexMin;
        private boolean mIsMuted;
        private int mObservedDevices;
        private final Intent mStreamDevicesChanged;
        private final int mStreamType;
        private final Intent mVolumeChanged;
        private String mVolumeIndexSettingName;

        /* synthetic */ VolumeStreamState(AudioService this$0, String settingName, int streamType, VolumeStreamState volumeStreamState) {
            this(settingName, streamType);
        }

        private VolumeStreamState(String settingName, int streamType) {
            this.mIndexMap = new SparseIntArray(8);
            this.mVolumeIndexSettingName = settingName;
            this.mStreamType = streamType;
            this.mIndexMin = AudioService.MIN_STREAM_VOLUME[streamType] * 10;
            this.mIndexMax = AudioService.MAX_STREAM_VOLUME[streamType] * 10;
            if (this.mStreamType == 4) {
                AudioSystem.initStreamVolume(streamType, 0, this.mIndexMax / 10);
            } else {
                AudioSystem.initStreamVolume(streamType, this.mIndexMin / 10, this.mIndexMax / 10);
            }
            readSettings();
            this.mVolumeChanged = new Intent("android.media.VOLUME_CHANGED_ACTION");
            this.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", this.mStreamType);
            this.mStreamDevicesChanged = new Intent("android.media.STREAM_DEVICES_CHANGED_ACTION");
            this.mStreamDevicesChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", this.mStreamType);
        }

        public int observeDevicesForStream_syncVSS(boolean checkOthers) {
            int devices = AudioSystem.getDevicesForStream(this.mStreamType);
            if (devices == this.mObservedDevices) {
                return devices;
            }
            int prevDevices = this.mObservedDevices;
            this.mObservedDevices = devices;
            if (checkOthers) {
                AudioService.this.observeDevicesForStreams(this.mStreamType);
            }
            if (AudioService.this.mStreamVolumeAlias[this.mStreamType] == this.mStreamType) {
                EventLogTags.writeStreamDevicesChanged(this.mStreamType, prevDevices, devices);
            }
            AudioService.this.sendBroadcastToAll(this.mStreamDevicesChanged.putExtra("android.media.EXTRA_PREV_VOLUME_STREAM_DEVICES", prevDevices).putExtra("android.media.EXTRA_VOLUME_STREAM_DEVICES", devices));
            return devices;
        }

        public String getSettingNameForDevice(int device) {
            String name = this.mVolumeIndexSettingName;
            String suffix = AudioSystem.getOutputDeviceName(device);
            if (suffix.isEmpty()) {
                return name;
            }
            return name + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + suffix;
        }

        public void readSettings() {
            synchronized (VolumeStreamState.class) {
                int index;
                if (AudioService.this.mUseFixedVolume) {
                    this.mIndexMap.put(1073741824, this.mIndexMax);
                } else if (this.mStreamType == 1 || this.mStreamType == 7) {
                    index = AudioSystem.DEFAULT_STREAM_VOLUME[this.mStreamType] * 10;
                    synchronized (AudioService.this.mCameraSoundForced) {
                        if (AudioService.this.mCameraSoundForced.booleanValue()) {
                            index = this.mIndexMax;
                        }
                    }
                    this.mIndexMap.put(1073741824, index);
                } else {
                    int remainingDevices = 1107296255;
                    int i = 0;
                    while (remainingDevices != 0) {
                        int device = 1 << i;
                        if ((device & remainingDevices) != 0) {
                            remainingDevices &= ~device;
                            index = System.getIntForUser(AudioService.this.mContentResolver, getSettingNameForDevice(device), device == 1073741824 ? AudioSystem.DEFAULT_STREAM_VOLUME[this.mStreamType] : -1, -2);
                            if (index != -1) {
                                this.mIndexMap.put(device, getValidIndex(index * 10));
                            }
                        }
                        i++;
                    }
                }
            }
        }

        private int getAbsoluteVolumeIndex(int index) {
            if (index == 0) {
                return 0;
            }
            if (index == 1) {
                return ((int) (((double) this.mIndexMax) * 0.5d)) / 10;
            }
            if (index == 2) {
                return ((int) (((double) this.mIndexMax) * 0.7d)) / 10;
            }
            if (index == 3) {
                return ((int) (((double) this.mIndexMax) * 0.85d)) / 10;
            }
            return (this.mIndexMax + 5) / 10;
        }

        public void applyDeviceVolume_syncVSS(int device) {
            int index;
            if (this.mIsMuted) {
                index = 0;
            } else if ((device & 896) != 0 && AudioService.this.mAvrcpAbsVolSupported) {
                index = getAbsoluteVolumeIndex((getIndex(device) + 5) / 10);
            } else if ((AudioService.this.mFullVolumeDevices & device) != 0) {
                index = (this.mIndexMax + 5) / 10;
            } else {
                index = (getIndex(device) + 5) / 10;
            }
            Log.d(AudioService.TAG, "applyDeviceVolumemStreamType:" + this.mStreamType + ",device:" + device);
            AudioSystem.setStreamVolumeIndex(this.mStreamType, index, device);
        }

        public void applyAllVolumes() {
            synchronized (VolumeStreamState.class) {
                int index;
                if (this.mIsMuted) {
                    index = 0;
                } else {
                    index = (getIndex(1073741824) + 5) / 10;
                }
                AudioSystem.setStreamVolumeIndex(this.mStreamType, index, 1073741824);
                for (int i = 0; i < this.mIndexMap.size(); i++) {
                    int device = this.mIndexMap.keyAt(i);
                    if (device != 1073741824) {
                        if (this.mIsMuted) {
                            index = 0;
                        } else if ((device & 896) != 0 && AudioService.this.mAvrcpAbsVolSupported) {
                            index = getAbsoluteVolumeIndex((getIndex(device) + 5) / 10);
                        } else if ((AudioService.this.mFullVolumeDevices & device) != 0) {
                            index = (this.mIndexMax + 5) / 10;
                        } else {
                            index = (this.mIndexMap.valueAt(i) + 5) / 10;
                        }
                        AudioSystem.setStreamVolumeIndex(this.mStreamType, index, device);
                    }
                }
            }
        }

        public boolean adjustIndex(int deltaIndex, int device, String caller) {
            return setIndex(getIndex(device) + deltaIndex, device, caller);
        }

        /* JADX WARNING: Unexpected end of synchronized block */
        /* JADX WARNING: Missing block: B:7:0x0016, code:
            if (r11.mStreamType != 7) goto L_0x0026;
     */
        /* JADX WARNING: Missing block: B:9:0x0022, code:
            if (com.android.server.audio.AudioService.-get14(r11.this$0).booleanValue() == false) goto L_0x0026;
     */
        /* JADX WARNING: Missing block: B:10:0x0024, code:
            r12 = r11.mIndexMax;
     */
        /* JADX WARNING: Missing block: B:13:0x0027, code:
            r2 = r11.mIndexMap.get(r13, -1);
            r11.mIndexMap.put(r13, r12);
     */
        /* JADX WARNING: Missing block: B:14:0x0033, code:
            if (r4 != r12) goto L_0x0038;
     */
        /* JADX WARNING: Missing block: B:16:0x0036, code:
            if (r2 != -1) goto L_0x008d;
     */
        /* JADX WARNING: Missing block: B:17:0x0038, code:
            r0 = true;
     */
        /* JADX WARNING: Missing block: B:18:0x0039, code:
            if (r0 == false) goto L_0x0091;
     */
        /* JADX WARNING: Missing block: B:20:0x0043, code:
            if (r13 != com.android.server.audio.AudioService.-wrap4(r11.this$0, r11.mStreamType)) goto L_0x008f;
     */
        /* JADX WARNING: Missing block: B:21:0x0045, code:
            r1 = true;
     */
        /* JADX WARNING: Missing block: B:22:0x0046, code:
            r6 = android.media.AudioSystem.getNumStreamTypes() - 1;
     */
        /* JADX WARNING: Missing block: B:23:0x004c, code:
            if (r6 < 0) goto L_0x0091;
     */
        /* JADX WARNING: Missing block: B:25:0x0050, code:
            if (r6 == r11.mStreamType) goto L_0x0084;
     */
        /* JADX WARNING: Missing block: B:27:0x005c, code:
            if (com.android.server.audio.AudioService.-get41(r11.this$0)[r6] != r11.mStreamType) goto L_0x0084;
     */
        /* JADX WARNING: Missing block: B:28:0x005e, code:
            r5 = com.android.server.audio.AudioService.-wrap6(r11.this$0, r12, r11.mStreamType, r6);
            com.android.server.audio.AudioService.-get40(r11.this$0)[r6].setIndex(r5, r13, r14);
     */
        /* JADX WARNING: Missing block: B:29:0x0071, code:
            if (r1 == false) goto L_0x0084;
     */
        /* JADX WARNING: Missing block: B:30:0x0073, code:
            com.android.server.audio.AudioService.-get40(r11.this$0)[r6].setIndex(r5, com.android.server.audio.AudioService.-wrap4(r11.this$0, r6), r14);
     */
        /* JADX WARNING: Missing block: B:31:0x0084, code:
            r6 = r6 - 1;
     */
        /* JADX WARNING: Missing block: B:38:0x008d, code:
            r0 = false;
     */
        /* JADX WARNING: Missing block: B:39:0x008f, code:
            r1 = false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean setIndex(int index, int device, String caller) {
            int oldIndex;
            synchronized (VolumeStreamState.class) {
                oldIndex = getIndex(device);
                index = getValidIndex(index);
                synchronized (AudioService.this.mCameraSoundForced) {
                }
            }
            if (changed) {
                oldIndex = (oldIndex + 5) / 10;
                index = (index + 5) / 10;
                if (AudioService.this.mStreamVolumeAlias[this.mStreamType] == this.mStreamType) {
                    if (caller == null) {
                        Log.w(AudioService.TAG, "No caller for volume_changed event", new Throwable());
                    }
                    EventLogTags.writeVolumeChanged(this.mStreamType, oldIndex, index, this.mIndexMax / 10, caller);
                }
                this.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", index);
                this.mVolumeChanged.putExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", oldIndex);
                this.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE_ALIAS", AudioService.this.mStreamVolumeAlias[this.mStreamType]);
                AudioService.this.sendBroadcastToAll(this.mVolumeChanged);
            }
            return changed;
        }

        public int getIndex(int device) {
            int index;
            synchronized (VolumeStreamState.class) {
                index = this.mIndexMap.get(device, -1);
                if (index == -1) {
                    index = this.mIndexMap.get(1073741824);
                }
            }
            return index;
        }

        public int getMaxIndex() {
            return this.mIndexMax;
        }

        public int getMinIndex() {
            return this.mIndexMin;
        }

        public void setAllIndexes(VolumeStreamState srcStream, String caller) {
            synchronized (VolumeStreamState.class) {
                int i;
                int srcStreamType = srcStream.getStreamType();
                int index = AudioService.this.rescaleIndex(srcStream.getIndex(1073741824), srcStreamType, this.mStreamType);
                for (i = 0; i < this.mIndexMap.size(); i++) {
                    this.mIndexMap.put(this.mIndexMap.keyAt(i), index);
                }
                SparseIntArray srcMap = srcStream.mIndexMap;
                for (i = 0; i < srcMap.size(); i++) {
                    setIndex(AudioService.this.rescaleIndex(srcMap.valueAt(i), srcStreamType, this.mStreamType), srcMap.keyAt(i), caller);
                }
            }
        }

        public void setAllIndexesToMax() {
            synchronized (VolumeStreamState.class) {
                for (int i = 0; i < this.mIndexMap.size(); i++) {
                    this.mIndexMap.put(this.mIndexMap.keyAt(i), this.mIndexMax);
                }
            }
        }

        public void mute(boolean state) {
            boolean changed = false;
            synchronized (VolumeStreamState.class) {
                if (state != this.mIsMuted) {
                    changed = true;
                    this.mIsMuted = state;
                    AudioService.sendMsg(AudioService.this.mAudioHandler, 10, 2, 0, 0, this, 0);
                }
            }
            if (changed) {
                Intent intent = new Intent("android.media.STREAM_MUTE_CHANGED_ACTION");
                intent.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", this.mStreamType);
                intent.putExtra("android.media.EXTRA_STREAM_VOLUME_MUTED", state);
                AudioService.this.sendBroadcastToAll(intent);
            }
        }

        public int getStreamType() {
            return this.mStreamType;
        }

        public void checkFixedVolumeDevices() {
            synchronized (VolumeStreamState.class) {
                if (AudioService.this.mStreamVolumeAlias[this.mStreamType] == 3) {
                    for (int i = 0; i < this.mIndexMap.size(); i++) {
                        int device = this.mIndexMap.keyAt(i);
                        int index = this.mIndexMap.valueAt(i);
                        if (!((AudioService.this.mFullVolumeDevices & device) == 0 && ((AudioService.this.mFixedVolumeDevices & device) == 0 || index == 0))) {
                            this.mIndexMap.put(device, this.mIndexMax);
                        }
                        applyDeviceVolume_syncVSS(device);
                    }
                }
            }
        }

        private int getValidIndex(int index) {
            if (index < this.mIndexMin) {
                return this.mIndexMin;
            }
            if (AudioService.this.mUseFixedVolume || index > this.mIndexMax) {
                return this.mIndexMax;
            }
            return index;
        }

        private void dump(PrintWriter pw) {
            int i;
            int device;
            pw.print("   Muted: ");
            pw.println(this.mIsMuted);
            pw.print("   Min: ");
            pw.println((this.mIndexMin + 5) / 10);
            pw.print("   Max: ");
            pw.println((this.mIndexMax + 5) / 10);
            pw.print("   Current: ");
            for (i = 0; i < this.mIndexMap.size(); i++) {
                String deviceName;
                if (i > 0) {
                    pw.print(", ");
                }
                device = this.mIndexMap.keyAt(i);
                pw.print(Integer.toHexString(device));
                if (device == 1073741824) {
                    deviceName = "default";
                } else {
                    deviceName = AudioSystem.getOutputDeviceName(device);
                }
                if (!deviceName.isEmpty()) {
                    pw.print(" (");
                    pw.print(deviceName);
                    pw.print(")");
                }
                pw.print(": ");
                pw.print((this.mIndexMap.valueAt(i) + 5) / 10);
            }
            pw.println();
            pw.print("   Devices: ");
            int devices = AudioService.this.getDevicesForStream(this.mStreamType);
            i = 0;
            int n = 0;
            while (true) {
                int n2 = n;
                device = 1 << i;
                if (device != 1073741824) {
                    if ((devices & device) != 0) {
                        n = n2 + 1;
                        if (n2 > 0) {
                            pw.print(", ");
                        }
                        pw.print(AudioSystem.getOutputDeviceName(device));
                    } else {
                        n = n2;
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    private class WiredDeviceConnectionState {
        public final String mAddress;
        public final String mCaller;
        public final String mName;
        public final int mState;
        public final int mType;

        public WiredDeviceConnectionState(int type, int state, String address, String name, String caller) {
            this.mType = type;
            this.mState = state;
            this.mAddress = address;
            this.mName = name;
            this.mCaller = caller;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.audio.AudioService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.audio.AudioService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.AudioService.<clinit>():void");
    }

    private boolean isPlatformVoice() {
        return this.mPlatformType == 1;
    }

    private boolean isPlatformTelevision() {
        return this.mPlatformType == 2;
    }

    private String makeDeviceListKey(int device, String deviceAddress) {
        return "0x" + Integer.toHexString(device) + ":" + deviceAddress;
    }

    public static String makeAlsaAddressString(int card, int device) {
        return "card=" + card + ";device=" + device + ";";
    }

    public AudioService(Context context) {
        boolean z;
        int i;
        this.mAudioManager = null;
        this.mAdjustVolumeAction = false;
        this.mFadeInCurrentVolume = 0;
        this.mFadeInFinish = true;
        this.mFadeInMusicVolume = AudioSystem.DEFAULT_STREAM_VOLUME[3];
        this.mFadeInDevice = 4;
        this.mIsExportVersion = false;
        this.mVolumeController = new VolumeController();
        this.mControllerService = new ControllerService();
        this.mMode = 0;
        this.mSettingsLock = new Object();
        this.mSoundEffectsLock = new Object();
        this.SOUND_EFFECT_FILES_MAP = (int[][]) Array.newInstance(Integer.TYPE, new int[]{10, 2});
        this.STREAM_VOLUME_ALIAS_VOICE = new int[]{0, 2, 2, 3, 2, 2, 6, 2, 2, 3};
        this.STREAM_VOLUME_ALIAS_TELEVISION = new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
        this.STREAM_VOLUME_ALIAS_DEFAULT = new int[]{0, 2, 2, 3, 4, 2, 6, 2, 2, 3};
        this.mIsInSetCallMode = false;
        this.mSettingCallLock = new Object();
        this.mAudioSystemCallback = new ErrorCallback() {
            public void onError(int error) {
                switch (error) {
                    case 100:
                        AudioService.sendMsg(AudioService.this.mAudioHandler, 4, 1, 0, 0, null, 0);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mRingerModeExternal = -1;
        this.mRingerModeAffectedStreams = 0;
        this.mReceiver = new AudioServiceBroadcastReceiver(this, null);
        this.mUserRestrictionsListener = new AudioServiceUserRestrictionsListener(this, null);
        this.mConnectedDevices = new ArrayMap();
        this.mSetModeDeathHandlers = new ArrayList();
        this.mScoClients = new ArrayList();
        this.mSoundPoolLooper = null;
        this.mPrevVolDirection = 0;
        this.mVolumeControlStream = -1;
        this.mForceControlStreamLock = new Object();
        this.mForceControlStreamClient = null;
        this.mDeviceOrientation = 0;
        this.mBluetoothA2dpEnabledLock = new Object();
        this.mCurAudioRoutes = new AudioRoutesInfo();
        this.mRoutesObservers = new RemoteCallbackList();
        this.mFixedVolumeDevices = 2890752;
        this.mFullVolumeDevices = 0;
        this.mDockAudioMediaEnabled = true;
        this.mDockState = 0;
        this.mA2dpAvrcpLock = new Object();
        this.mAvrcpAbsVolSupported = false;
        this.mVolumePolicy = VolumePolicy.DEFAULT;
        this.mRmtSbmxFullVolRefCount = 0;
        this.mRmtSbmxFullVolDeathHandlers = new ArrayList();
        this.mBluetoothProfileServiceListener = new ServiceListener() {
            /* JADX WARNING: Missing block: B:64:0x01c5, code:
            if (com.android.server.audio.AudioService.-get31(r17.this$0) == 4) goto L_0x0181;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (AudioService.DEBUG_VOL) {
                    Log.d(AudioService.TAG, "BluetoothProfile.ServiceListener onServiceConnected = " + profile);
                }
                List<BluetoothDevice> deviceList;
                BluetoothDevice btDevice;
                switch (profile) {
                    case 1:
                        synchronized (AudioService.this.mScoClients) {
                            AudioService.this.mAudioHandler.removeMessages(9);
                            AudioService.this.mBluetoothHeadset = (BluetoothHeadset) proxy;
                            deviceList = AudioService.this.mBluetoothHeadset.getConnectedDevices();
                            if (deviceList.size() > 0) {
                                AudioService.this.mBluetoothHeadsetDevice = (BluetoothDevice) deviceList.get(0);
                            } else {
                                AudioService.this.mBluetoothHeadsetDevice = null;
                            }
                            AudioService.this.checkScoAudioState();
                            if (AudioService.DEBUG_DEVICES) {
                                Log.d(AudioService.TAG, "BluetoothProfile.ServiceListener 1,mScoAudioState: " + AudioService.this.mScoAudioState + ",mBluetoothHeadsetDevice:" + AudioService.this.mBluetoothHeadsetDevice + ",mScoAudioMode:" + AudioService.this.mScoAudioMode + ",deviceList:" + deviceList);
                            }
                            if (!(AudioService.this.mScoAudioState == 1 || AudioService.this.mScoAudioState == 5)) {
                                break;
                            }
                            boolean status = false;
                            if (AudioService.this.mBluetoothHeadsetDevice != null) {
                                switch (AudioService.this.mScoAudioState) {
                                    case 1:
                                        AudioService.this.mScoAudioState = 3;
                                        if (AudioService.this.mScoAudioMode != 1) {
                                            if (AudioService.this.mScoAudioMode != 0) {
                                                if (AudioService.this.mScoAudioMode == 2) {
                                                    status = AudioService.this.mBluetoothHeadset.startVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                                                    break;
                                                }
                                            }
                                            status = AudioService.this.mBluetoothHeadset.startScoUsingVirtualVoiceCall(AudioService.this.mBluetoothHeadsetDevice);
                                            break;
                                        }
                                        status = AudioService.this.mBluetoothHeadset.connectAudio();
                                        break;
                                        break;
                                    case 4:
                                        status = AudioService.this.mBluetoothHeadset.stopVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                                        break;
                                    case 5:
                                        if (AudioService.this.mScoAudioMode != 1) {
                                            if (AudioService.this.mScoAudioMode != 0) {
                                                if (AudioService.this.mScoAudioMode == 2) {
                                                    status = AudioService.this.mBluetoothHeadset.stopVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                                                    break;
                                                }
                                            }
                                            status = AudioService.this.mBluetoothHeadset.stopScoUsingVirtualVoiceCall(AudioService.this.mBluetoothHeadsetDevice);
                                            break;
                                        }
                                        status = AudioService.this.mBluetoothHeadset.disconnectAudio();
                                        break;
                                        break;
                                }
                            }
                            if (!status) {
                                AudioService.sendMsg(AudioService.this.mAudioHandler, 9, 0, 0, 0, null, 0);
                            }
                        }
                        return;
                    case 2:
                        synchronized (AudioService.this.mConnectedDevices) {
                            synchronized (AudioService.this.mA2dpAvrcpLock) {
                                AudioService.this.mA2dp = (BluetoothA2dp) proxy;
                                deviceList = AudioService.this.mA2dp.getConnectedDevices();
                                if (deviceList.size() > 0) {
                                    btDevice = (BluetoothDevice) deviceList.get(0);
                                    int state = AudioService.this.mA2dp.getConnectionState(btDevice);
                                    AudioService.this.queueMsgUnderWakeLock(AudioService.this.mAudioHandler, 102, state, 0, btDevice, AudioService.this.checkSendBecomingNoisyIntent(128, state == 2 ? 1 : 0));
                                }
                            }
                        }
                        return;
                    case 11:
                        deviceList = proxy.getConnectedDevices();
                        if (deviceList.size() > 0) {
                            btDevice = (BluetoothDevice) deviceList.get(0);
                            synchronized (AudioService.this.mConnectedDevices) {
                                AudioService.this.queueMsgUnderWakeLock(AudioService.this.mAudioHandler, 101, proxy.getConnectionState(btDevice), 0, btDevice, 0);
                            }
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }

            public void onServiceDisconnected(int profile) {
                switch (profile) {
                    case 1:
                        AudioService.this.disconnectHeadset();
                        return;
                    case 2:
                        AudioService.this.disconnectA2dp();
                        return;
                    case 11:
                        AudioService.this.disconnectA2dpSink();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mBecomingNoisyIntentDevices = 163724;
        this.mMcc = 0;
        this.mSafeMediaVolumeDevices = 12;
        this.mHdmiSystemAudioSupported = false;
        this.mHdmiDisplayStatusCallback = new MyDisplayStatusCallback(this, null);
        this.mDynPolicyCallback = new DynamicPolicyCallback() {
            public void onDynamicPolicyMixStateUpdate(String regId, int state) {
                if (!TextUtils.isEmpty(regId)) {
                    AudioService.sendMsg(AudioService.this.mAudioHandler, 25, 2, state, 0, regId, 0);
                }
            }
        };
        this.mRecordMonitor = new RecordingActivityMonitor();
        this.mAudioPolicies = new HashMap();
        this.mAudioPolicyCounter = 0;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mPlatformType = AudioSystem.getPlatformType(context);
        this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mAudioEventWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "handleAudioEvent");
        Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
        this.mHasVibrator = vibrator == null ? false : vibrator.hasVibrator();
        if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("CN")) {
            z = false;
        } else {
            z = true;
        }
        this.mIsExportVersion = z;
        int maxVolume = SystemProperties.getInt("ro.config.vc_call_vol_steps", MAX_STREAM_VOLUME[0]);
        if (maxVolume != MAX_STREAM_VOLUME[0]) {
            MAX_STREAM_VOLUME[0] = maxVolume;
            AudioSystem.DEFAULT_STREAM_VOLUME[0] = (maxVolume * 3) / 4;
        }
        maxVolume = SystemProperties.getInt("ro.config.media_vol_steps", MAX_STREAM_VOLUME[3]);
        if (maxVolume != MAX_STREAM_VOLUME[3]) {
            MAX_STREAM_VOLUME[3] = maxVolume;
            AudioSystem.DEFAULT_STREAM_VOLUME[3] = (maxVolume * 3) / 4;
        }
        sSoundEffectVolumeDb = context.getResources().getInteger(17694724);
        this.mForcedUseForComm = 0;
        createAudioSystemThread();
        AudioSystem.setErrorCallback(this.mAudioSystemCallback);
        boolean cameraSoundForced = readCameraSoundForced();
        this.mCameraSoundForced = new Boolean(cameraSoundForced);
        Handler handler = this.mAudioHandler;
        if (cameraSoundForced) {
            i = 11;
        } else {
            i = 0;
        }
        sendMsg(handler, 8, 2, 4, i, null, 0);
        this.mSafeMediaVolumeState = new Integer(Global.getInt(this.mContentResolver, "audio_safe_volume_state", 0));
        this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(17694862) * 10;
        this.mUseFixedVolume = this.mContext.getResources().getBoolean(17956994);
        updateStreamVolumeAlias(false, TAG);
        readPersistedSettings();
        readUserRestrictions();
        this.mSettingsObserver = new SettingsObserver();
        createStreamStates();
        this.mMediaFocusControl = new MediaFocusControl(this.mContext);
        readAndSetLowRamDevice();
        this.mRingerModeMutedStreams = 0;
        setRingerModeInt(getRingerModeInternal(), false);
        IntentFilter intentFilter = new IntentFilter("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.DOCK_EVENT");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_BACKGROUND");
        intentFilter.addAction("android.intent.action.USER_FOREGROUND");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mMonitorOrientation = SystemProperties.getBoolean("ro.audio.monitorOrientation", false);
        if (this.mMonitorOrientation) {
            Log.v(TAG, "monitoring device orientation");
            setOrientationForAudioSystem();
        }
        this.mMonitorRotation = SystemProperties.getBoolean("ro.audio.monitorRotation", false);
        if (this.mMonitorRotation) {
            RotationHelper.init(this.mContext, this.mAudioHandler);
        }
        context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
        LocalServices.addService(AudioManagerInternal.class, new AudioServiceInternal());
        this.mUserManagerInternal.addUserRestrictionsListener(this.mUserRestrictionsListener);
        this.mRecordMonitor.initMonitor();
    }

    public void systemReady() {
        sendMsg(this.mAudioHandler, 21, 2, 0, 0, null, 0);
    }

    public void onSystemReady() {
        this.mSystemReady = true;
        sendMsg(this.mAudioHandler, 7, 2, 0, 0, null, 0);
        this.mScoConnectionState = -1;
        resetBluetoothSco();
        getBluetoothHeadset();
        Intent newIntent = new Intent("android.media.SCO_AUDIO_STATE_CHANGED");
        newIntent.putExtra("android.media.extra.SCO_AUDIO_STATE", 0);
        sendStickyBroadcastToAll(newIntent);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.getProfileProxy(this.mContext, this.mBluetoothProfileServiceListener, 2);
        }
        this.mHdmiManager = (HdmiControlManager) this.mContext.getSystemService("hdmi_control");
        if (this.mHdmiManager != null) {
            synchronized (this.mHdmiManager) {
                this.mHdmiTvClient = this.mHdmiManager.getTvClient();
                if (this.mHdmiTvClient != null) {
                    this.mFixedVolumeDevices &= -2883587;
                }
                this.mHdmiPlaybackClient = this.mHdmiManager.getPlaybackClient();
                this.mHdmiCecSink = false;
            }
        }
        this.mNm = (NotificationManager) this.mContext.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME);
        sendMsg(this.mAudioHandler, 17, 0, 0, 0, TAG, 30000);
        StreamOverride.init(this.mContext);
        this.mControllerService.init();
        onIndicateSystemReady();
    }

    void onIndicateSystemReady() {
        if (AudioSystem.systemReady() != 0) {
            sendMsg(this.mAudioHandler, 26, 0, 0, 0, null, 1000);
        }
    }

    public void onAudioServerDied() {
        int i = 0;
        if (this.mSystemReady && AudioSystem.checkAudioFlinger() == 0) {
            Log.e(TAG, "Audioserver started.");
            AudioSystem.setParameters("restarting=true");
            readAndSetLowRamDevice();
            synchronized (this.mConnectedDevices) {
                for (int i2 = 0; i2 < this.mConnectedDevices.size(); i2++) {
                    DeviceListSpec spec = (DeviceListSpec) this.mConnectedDevices.valueAt(i2);
                    AudioSystem.setDeviceConnectionState(spec.mDeviceType, 1, spec.mDeviceAddress, spec.mDeviceName);
                }
            }
            AudioSystem.setPhoneState(this.mMode);
            AudioSystem.setForceUse(0, this.mForcedUseForComm);
            AudioSystem.setForceUse(2, this.mForcedUseForComm);
            AudioSystem.setForceUse(4, this.mCameraSoundForced.booleanValue() ? 11 : 0);
            for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                VolumeStreamState streamState = this.mStreamStates[streamType];
                if (streamType == 4) {
                    AudioSystem.initStreamVolume(streamType, 0, streamState.mIndexMax / 10);
                } else {
                    AudioSystem.initStreamVolume(streamType, streamState.mIndexMin / 10, streamState.mIndexMax / 10);
                }
                streamState.applyAllVolumes();
            }
            updateMasterMono(this.mContentResolver);
            setRingerModeInt(getRingerModeInternal(), false);
            if (this.mMonitorOrientation) {
                setOrientationForAudioSystem();
            }
            if (this.mMonitorRotation) {
                RotationHelper.updateOrientation();
            }
            synchronized (this.mBluetoothA2dpEnabledLock) {
                int i3;
                if (this.mBluetoothA2dpEnabled) {
                    i3 = 0;
                } else {
                    i3 = 10;
                }
                AudioSystem.setForceUse(1, i3);
            }
            synchronized (this.mSettingsLock) {
                if (this.mDockAudioMediaEnabled) {
                    i = 8;
                }
                AudioSystem.setForceUse(3, i);
                sendEncodedSurroundMode(this.mContentResolver);
            }
            if (this.mHdmiManager != null) {
                synchronized (this.mHdmiManager) {
                    if (this.mHdmiTvClient != null) {
                        setHdmiSystemAudioSupported(this.mHdmiSystemAudioSupported);
                    }
                }
            }
            synchronized (this.mAudioPolicies) {
                for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                    policy.connectMixes();
                }
            }
            onIndicateSystemReady();
            if (IS_DOLBY_DAP_SUPPORT) {
                Intent broadcast = new Intent("media_server_started");
                broadcast.setPackage("com.dolby");
                broadcast.addFlags(67108864);
                this.mContext.sendBroadcast(broadcast);
            }
            AudioSystem.setParameters("restarting=false");
            return;
        }
        Log.e(TAG, "Audioserver died.");
        sendMsg(this.mAudioHandler, 4, 1, 0, 0, null, 500);
    }

    private void createAudioSystemThread() {
        this.mAudioSystemThread = new AudioSystemThread();
        this.mAudioSystemThread.start();
        waitForAudioHandlerCreation();
    }

    private void waitForAudioHandlerCreation() {
        synchronized (this) {
            while (this.mAudioHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while waiting on volume handler.");
                }
            }
        }
    }

    private void checkAllAliasStreamVolumes() {
        synchronized (VolumeStreamState.class) {
            int numStreamTypes = AudioSystem.getNumStreamTypes();
            for (int streamType = 0; streamType < numStreamTypes; streamType++) {
                if (streamType != this.mStreamVolumeAlias[streamType]) {
                    this.mStreamStates[streamType].setAllIndexes(this.mStreamStates[this.mStreamVolumeAlias[streamType]], TAG);
                }
                if (!this.mStreamStates[streamType].mIsMuted) {
                    this.mStreamStates[streamType].applyAllVolumes();
                }
            }
        }
    }

    private void checkAllFixedVolumeDevices() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = 0; streamType < numStreamTypes; streamType++) {
            this.mStreamStates[streamType].checkFixedVolumeDevices();
        }
    }

    private void checkAllFixedVolumeDevices(int streamType) {
        this.mStreamStates[streamType].checkFixedVolumeDevices();
    }

    private void checkMuteAffectedStreams() {
        for (VolumeStreamState vss : this.mStreamStates) {
            if (vss.mIndexMin > 0) {
                this.mMuteAffectedStreams &= ~(1 << vss.mStreamType);
            }
        }
    }

    private void createStreamStates() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        VolumeStreamState[] streams = new VolumeStreamState[numStreamTypes];
        this.mStreamStates = streams;
        for (int i = 0; i < numStreamTypes; i++) {
            streams[i] = new VolumeStreamState(this, System.VOLUME_SETTINGS[this.mStreamVolumeAlias[i]], i, null);
        }
        checkAllFixedVolumeDevices();
        checkAllAliasStreamVolumes();
        checkMuteAffectedStreams();
    }

    private void dumpStreamStates(PrintWriter pw) {
        pw.println("\nStream volumes (device: index)");
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int i = 0; i < numStreamTypes; i++) {
            pw.println("- " + AudioSystem.STREAM_NAMES[i] + ":");
            this.mStreamStates[i].dump(pw);
            pw.println(IElsaManager.EMPTY_PACKAGE);
        }
        pw.print("\n- mute affected streams = 0x");
        pw.println(Integer.toHexString(this.mMuteAffectedStreams));
    }

    private void updateStreamVolumeAlias(boolean updateVolumes, String caller) {
        int dtmfStreamAlias;
        Log.d(TAG, "Mits--- updateStreamVolumeAlias: updateVolumes = " + updateVolumes + "caller:" + caller);
        switch (this.mPlatformType) {
            case 1:
                this.mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_VOICE;
                dtmfStreamAlias = 2;
                break;
            case 2:
                this.mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_TELEVISION;
                dtmfStreamAlias = 3;
                break;
            default:
                this.mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_DEFAULT;
                dtmfStreamAlias = 3;
                break;
        }
        if (isPlatformTelevision()) {
            this.mRingerModeAffectedStreams = 0;
        } else if (isInCommunication()) {
            dtmfStreamAlias = 0;
            this.mRingerModeAffectedStreams &= -257;
        } else {
            this.mRingerModeAffectedStreams |= 256;
        }
        this.mStreamVolumeAlias[8] = dtmfStreamAlias;
        if (updateVolumes) {
            this.mStreamStates[8].setAllIndexes(this.mStreamStates[dtmfStreamAlias], caller);
            setRingerModeInt(getRingerModeInternal(), false);
            sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[8], 0);
        }
    }

    private void readDockAudioSettings(ContentResolver cr) {
        int i;
        boolean z = true;
        if (Global.getInt(cr, "dock_audio_media_enabled", 0) != 1) {
            z = false;
        }
        this.mDockAudioMediaEnabled = z;
        Handler handler = this.mAudioHandler;
        if (this.mDockAudioMediaEnabled) {
            i = 8;
        } else {
            i = 0;
        }
        sendMsg(handler, 8, 2, 3, i, null, 0);
    }

    private void updateMasterMono(ContentResolver cr) {
        boolean masterMono = System.getIntForUser(cr, "master_mono", 0, -2) == 1;
        if (DEBUG_VOL) {
            String str = TAG;
            Object[] objArr = new Object[1];
            objArr[0] = Boolean.valueOf(masterMono);
            Log.d(str, String.format("Master mono %b", objArr));
        }
        AudioSystem.setMasterMono(masterMono);
    }

    private void sendEncodedSurroundMode(ContentResolver cr) {
        sendEncodedSurroundMode(Global.getInt(cr, "encoded_surround_output", 0));
    }

    private void sendEncodedSurroundMode(int encodedSurroundMode) {
        int forceSetting = 15;
        switch (encodedSurroundMode) {
            case 0:
                forceSetting = 0;
                break;
            case 1:
                forceSetting = 13;
                break;
            case 2:
                forceSetting = 14;
                break;
            default:
                Log.e(TAG, "updateSurroundSoundSettings: illegal value " + encodedSurroundMode);
                break;
        }
        if (forceSetting != 15) {
            sendMsg(this.mAudioHandler, 8, 2, 6, forceSetting, null, 0);
        }
    }

    private void readPersistedSettings() {
        int i = 2;
        ContentResolver cr = this.mContentResolver;
        int ringerModeFromSettings = Global.getInt(cr, "mode_ringer", 2);
        int ringerMode = ringerModeFromSettings;
        if (!isValidRingerMode(ringerModeFromSettings)) {
            ringerMode = 2;
        }
        if (ringerMode == 1 && !this.mHasVibrator) {
            ringerMode = 0;
        }
        if (ringerMode != ringerModeFromSettings) {
            Global.putInt(cr, "mode_ringer", ringerMode);
        }
        if (this.mUseFixedVolume || isPlatformTelevision()) {
            ringerMode = 2;
        }
        synchronized (this.mSettingsLock) {
            int i2;
            this.mRingerMode = ringerMode;
            if (this.mRingerModeExternal == -1) {
                this.mRingerModeExternal = this.mRingerMode;
            }
            if (this.mHasVibrator) {
                i2 = 2;
            } else {
                i2 = 0;
            }
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(0, 1, i2);
            i2 = this.mVibrateSetting;
            if (!this.mHasVibrator) {
                i = 0;
            }
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(i2, 0, i);
            updateRingerModeAffectedStreams();
            readDockAudioSettings(cr);
            sendEncodedSurroundMode(cr);
        }
        this.mMuteAffectedStreams = System.getIntForUser(cr, "mute_streams_affected", 46, -2);
        updateMasterMono(cr);
        broadcastRingerMode("android.media.RINGER_MODE_CHANGED", this.mRingerModeExternal);
        broadcastRingerMode("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION", this.mRingerMode);
        broadcastVibrateSetting(0);
        broadcastVibrateSetting(1);
        this.mVolumeController.loadSettings(cr);
    }

    private void readUserRestrictions() {
        boolean masterMute;
        String str;
        Object[] objArr;
        int currentUser = getCurrentUserId();
        if (this.mUserManagerInternal.getUserRestriction(currentUser, "disallow_unmute_device")) {
            masterMute = true;
        } else {
            masterMute = this.mUserManagerInternal.getUserRestriction(currentUser, "no_adjust_volume");
        }
        if (this.mUseFixedVolume) {
            masterMute = false;
            AudioSystem.setMasterVolume(1.0f);
        }
        if (DEBUG_VOL) {
            str = TAG;
            objArr = new Object[2];
            objArr[0] = Boolean.valueOf(masterMute);
            objArr[1] = Integer.valueOf(currentUser);
            Log.d(str, String.format("Master mute %s, user=%d", objArr));
        }
        setSystemAudioMute(masterMute);
        AudioSystem.setMasterMute(masterMute);
        broadcastMasterMuteStatus(masterMute);
        boolean microphoneMute = this.mUserManagerInternal.getUserRestriction(currentUser, "no_unmute_microphone");
        if (DEBUG_VOL) {
            str = TAG;
            objArr = new Object[2];
            objArr[0] = Boolean.valueOf(microphoneMute);
            objArr[1] = Integer.valueOf(currentUser);
            Log.d(str, String.format("Mic mute %s, user=%d", objArr));
        }
        AudioSystem.muteMicrophone(microphoneMute);
    }

    private int rescaleIndex(int index, int srcStream, int dstStream) {
        return ((this.mStreamStates[dstStream].getMaxIndex() * index) + (this.mStreamStates[srcStream].getMaxIndex() / 2)) / this.mStreamStates[srcStream].getMaxIndex();
    }

    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, String caller) {
        adjustSuggestedStreamVolume(direction, suggestedStreamType, flags, callingPackage, caller, Binder.getCallingUid());
    }

    private void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, String caller, int uid) {
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            Log.d(TAG, "adjustSuggestedStreamVolume called by" + Debug.getCallers(8));
        }
        Log.d(TAG, "+adjustSuggestedStreamVolume() stream=" + suggestedStreamType + ", flags=" + flags + ", caller=" + caller);
        boolean isMute = isMuteAdjust(direction);
        this.mAdjustVolumeAction = true;
        int streamType = getActiveStreamType(suggestedStreamType);
        ensureValidStreamType(streamType);
        int resolvedStream = this.mStreamVolumeAlias[streamType];
        if (!((flags & 4) == 0 || resolvedStream == 2)) {
            flags &= -5;
        }
        if (this.mVolumeController.suppressAdjustment(resolvedStream, flags, isMute)) {
            direction = 0;
            flags = (flags & -5) & -17;
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume controller suppressed adjustment");
            }
        }
        adjustStreamVolume(streamType, direction, flags, callingPackage, caller, uid);
    }

    public void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage) {
        adjustStreamVolume(streamType, direction, flags, callingPackage, callingPackage, Binder.getCallingUid());
    }

    private void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage, String caller, int uid) {
        if (!this.mUseFixedVolume) {
            Log.d(TAG, "+adjustStreamVolume() stream=" + streamType + ", dir=" + direction + ", flags=" + flags + ", caller=" + caller);
            ensureValidDirection(direction);
            ensureValidStreamType(streamType);
            boolean isMuteAdjust = isMuteAdjust(direction);
            if (!isMuteAdjust || isStreamAffectedByMute(streamType)) {
                int streamTypeAlias = this.mStreamVolumeAlias[streamType];
                VolumeStreamState streamState = this.mStreamStates[streamTypeAlias];
                int device = getDeviceForStream(streamTypeAlias);
                int aliasIndex = streamState.getIndex(device);
                boolean adjustVolume = true;
                if ((device & 896) != 0 || (flags & 64) == 0) {
                    if (uid == 1000) {
                        uid = UserHandle.getUid(getCurrentUserId(), UserHandle.getAppId(uid));
                    }
                    if (this.mAppOps.noteOp(STREAM_VOLUME_OPS[streamTypeAlias], uid, callingPackage) == 0) {
                        int step;
                        synchronized (this.mSafeMediaVolumeState) {
                            this.mPendingVolumeCommand = null;
                        }
                        flags &= -33;
                        if (streamTypeAlias != 3 || (this.mFixedVolumeDevices & device) == 0) {
                            step = rescaleIndex(10, streamType, streamTypeAlias);
                        } else {
                            flags |= 32;
                            if (this.mSafeMediaVolumeState.intValue() != 3 || (device & 12) == 0) {
                                step = streamState.getMaxIndex();
                            } else {
                                step = this.mSafeMediaVolumeIndex;
                            }
                            if (aliasIndex != 0) {
                                aliasIndex = step;
                            }
                        }
                        if ((flags & 2) != 0 || streamTypeAlias == getUiSoundsStreamType()) {
                            if (getRingerModeInternal() == 1) {
                                flags &= -17;
                            }
                            int result = 1;
                            if (caller.contains(".cts")) {
                                result = checkForRingerModeChange(aliasIndex, direction, step, streamState.mIsMuted, callingPackage, flags);
                            }
                            adjustVolume = (result & 1) != 0;
                            if ((result & 128) != 0) {
                                flags |= 128;
                            }
                            if ((result & 2048) != 0) {
                                flags |= 2048;
                            }
                        }
                        if (!volumeAdjustmentAllowedByDnd(streamTypeAlias, flags)) {
                            adjustVolume = false;
                        }
                        int oldIndex = this.mStreamStates[streamType].getIndex(device);
                        if (adjustVolume && direction != 0) {
                            this.mAudioHandler.removeMessages(24);
                            if (streamTypeAlias == 3 && (device & 896) != 0 && (flags & 64) == 0) {
                                synchronized (this.mA2dpAvrcpLock) {
                                    if (this.mA2dp != null && this.mAvrcpAbsVolSupported) {
                                        this.mA2dp.adjustAvrcpAbsoluteVolume(direction);
                                    }
                                }
                            }
                            int stream;
                            if (isMuteAdjust) {
                                boolean state = direction == 101 ? !streamState.mIsMuted : direction == -100;
                                if (streamTypeAlias == 3) {
                                    setSystemAudioMute(state);
                                }
                                stream = 0;
                                while (stream < this.mStreamStates.length) {
                                    if (streamTypeAlias == this.mStreamVolumeAlias[stream]) {
                                        Object obj = readCameraSoundForced() ? this.mStreamStates[stream].getStreamType() == 7 ? 1 : null : null;
                                        if (obj == null && this.mStreamStates[stream].getStreamType() != 4 && (streamTypeAlias != 2 || this.mRingerMode == 2)) {
                                            this.mStreamStates[stream].mute(state);
                                        }
                                    }
                                    stream++;
                                }
                            } else {
                                if (direction == 1) {
                                    if (!checkSafeMediaVolume(streamTypeAlias, aliasIndex + step, device)) {
                                        Log.e(TAG, "adjustStreamVolume() safe volume index = " + oldIndex);
                                        this.mVolumeController.postDisplaySafeVolumeWarning(flags);
                                    }
                                }
                                if (streamState.adjustIndex(direction * step, device, caller) || streamState.mIsMuted) {
                                    if (streamState.mIsMuted) {
                                        if (direction == 1) {
                                            if (streamTypeAlias == 3) {
                                                for (stream = 0; stream < this.mStreamStates.length; stream++) {
                                                    if (streamTypeAlias == this.mStreamVolumeAlias[stream]) {
                                                        this.mStreamStates[stream].mute(false);
                                                    }
                                                }
                                            } else if (streamTypeAlias == 2 && this.mRingerMode == 2) {
                                                for (stream = 0; stream < this.mStreamStates.length; stream++) {
                                                    if (streamTypeAlias == this.mStreamVolumeAlias[stream]) {
                                                        this.mStreamStates[stream].mute(false);
                                                    }
                                                }
                                            } else if (streamType == 0 || streamType == 6) {
                                                this.mStreamStates[streamType].mute(false);
                                            }
                                        } else if (direction == -1 && this.mPlatformType == 2) {
                                            sendMsg(this.mAudioHandler, 24, 2, streamTypeAlias, flags, null, UNMUTE_STREAM_DELAY);
                                        }
                                    }
                                    sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
                                }
                            }
                            int newIndex = this.mStreamStates[streamType].getIndex(device);
                            if (streamTypeAlias == 3) {
                                setSystemAudioVolume(oldIndex, newIndex, getStreamMaxVolume(streamType), flags);
                            }
                            if (this.mHdmiManager != null) {
                                synchronized (this.mHdmiManager) {
                                    if (this.mHdmiCecSink && streamTypeAlias == 3 && oldIndex != newIndex) {
                                        synchronized (this.mHdmiPlaybackClient) {
                                            int keyCode;
                                            if (direction == -1) {
                                                keyCode = 25;
                                            } else {
                                                keyCode = 24;
                                            }
                                            long ident = Binder.clearCallingIdentity();
                                            try {
                                                this.mHdmiPlaybackClient.sendKeyEvent(keyCode, true);
                                                this.mHdmiPlaybackClient.sendKeyEvent(keyCode, false);
                                                Binder.restoreCallingIdentity(ident);
                                            } catch (Throwable th) {
                                                Binder.restoreCallingIdentity(ident);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        int index = this.mStreamStates[streamType].getIndex(device);
                        Log.d(TAG, "sendVolumeUpdate() streamType :" + streamType + " oldIndex:" + oldIndex + " index:" + index + " flags:" + flags);
                        sendVolumeUpdate(streamType, oldIndex, index, flags);
                    }
                }
            }
        }
    }

    private void onUnmuteStream(int stream, int flags) {
        this.mStreamStates[stream].mute(false);
        int index = this.mStreamStates[stream].getIndex(getDeviceForStream(stream));
        sendVolumeUpdate(stream, index, index, flags);
    }

    private void setSystemAudioVolume(int oldVolume, int newVolume, int maxVolume, int flags) {
        if (this.mHdmiManager != null && this.mHdmiTvClient != null && oldVolume != newVolume && (flags & 256) == 0) {
            synchronized (this.mHdmiManager) {
                if (this.mHdmiSystemAudioSupported) {
                    synchronized (this.mHdmiTvClient) {
                        long token = Binder.clearCallingIdentity();
                        try {
                            this.mHdmiTvClient.setSystemAudioVolume(oldVolume, newVolume, maxVolume);
                            Binder.restoreCallingIdentity(token);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(token);
                        }
                    }
                    return;
                }
            }
        }
    }

    private int getNewRingerMode(int stream, int index, int flags) {
        if ((flags & 2) == 0 && stream != getUiSoundsStreamType()) {
            return getRingerModeExternal();
        }
        int newRingerMode;
        if (index != 0) {
            newRingerMode = 2;
        } else if (this.mHasVibrator) {
            newRingerMode = 1;
        } else if (this.mVolumePolicy.volumeDownToEnterSilent) {
            newRingerMode = 0;
        } else {
            newRingerMode = 2;
        }
        return newRingerMode;
    }

    private boolean isAndroidNPlus(String caller) {
        try {
            return this.mContext.getPackageManager().getApplicationInfoAsUser(caller, 0, UserHandle.getUserId(Binder.getCallingUid())).targetSdkVersion >= 24;
        } catch (NameNotFoundException e) {
            return true;
        }
    }

    private boolean wouldToggleZenMode(int newMode) {
        if (getRingerModeExternal() != 0 || newMode == 0) {
            return getRingerModeExternal() != 0 && newMode == 0;
        } else {
            return true;
        }
    }

    private void onSetStreamVolume(int streamType, int index, int flags, int device, String caller) {
        boolean z = false;
        int stream = this.mStreamVolumeAlias[streamType];
        setStreamVolumeInt(stream, index, device, false, caller);
        if (((flags & 2) != 0 || stream == getUiSoundsStreamType()) && caller.contains(".cts")) {
            setRingerMode(getNewRingerMode(stream, index, flags), "AudioService.onSetStreamVolume", false);
        }
        if (stream == 2 && index > 0 && this.mRingerMode != 2) {
            return;
        }
        if (stream != 3 || index != 0) {
            VolumeStreamState volumeStreamState = this.mStreamStates[stream];
            if (index == 0) {
                z = true;
            }
            volumeStreamState.mute(z);
        }
    }

    public void setStreamVolume(int streamType, int index, int flags, String callingPackage) {
        setStreamVolume(streamType, index, flags, callingPackage, callingPackage, Binder.getCallingUid());
    }

    private void setStreamVolume(int streamType, int index, int flags, String callingPackage, String caller, int uid) {
        if (!this.mUseFixedVolume) {
            Log.d(TAG, "+setStreamVolume() stream=" + streamType + ", index=" + index + ", flags=" + flags + ", caller=" + caller);
            if (index < 0) {
                index = this.mStreamStates[streamType].getMinIndex();
            }
            ensureValidStreamType(streamType);
            int streamTypeAlias = this.mStreamVolumeAlias[streamType];
            VolumeStreamState streamState = this.mStreamStates[streamTypeAlias];
            int device = getDeviceForStream(streamType);
            if (streamType == 6 && (device & 112) == 0) {
                if (callingPackage.equals("com.android.bluetooth")) {
                    device = 32;
                }
            }
            if ((device & 896) != 0 || (flags & 64) == 0) {
                if (uid == 1000) {
                    uid = UserHandle.getUid(getCurrentUserId(), UserHandle.getAppId(uid));
                }
                if (this.mAppOps.noteOp(STREAM_VOLUME_OPS[streamTypeAlias], uid, callingPackage) == 0) {
                    if (isAndroidNPlus(callingPackage) && wouldToggleZenMode(getNewRingerMode(streamTypeAlias, index, flags)) && !this.mNm.isNotificationPolicyAccessGrantedForPackage(callingPackage)) {
                        throw new SecurityException("Not allowed to change Do Not Disturb state");
                    } else if (volumeAdjustmentAllowedByDnd(streamTypeAlias, flags)) {
                        int oldIndex;
                        synchronized (this.mSafeMediaVolumeState) {
                            this.mPendingVolumeCommand = null;
                            oldIndex = streamState.getIndex(device);
                            index = rescaleIndex(index * 10, streamType, streamTypeAlias);
                            if (streamTypeAlias == 3 && (device & 896) != 0 && (flags & 64) == 0) {
                                synchronized (this.mA2dpAvrcpLock) {
                                    if (this.mA2dp != null && this.mAvrcpAbsVolSupported) {
                                        this.mA2dp.setAvrcpAbsoluteVolume(index / 10);
                                    }
                                }
                            }
                            if (streamTypeAlias == 3) {
                                setSystemAudioVolume(oldIndex, index, getStreamMaxVolume(streamType), flags);
                            }
                            flags &= -33;
                            if (streamTypeAlias == 3 && (this.mFixedVolumeDevices & device) != 0) {
                                flags |= 32;
                                if (index != 0) {
                                    index = (this.mSafeMediaVolumeState.intValue() != 3 || (device & 12) == 0) ? streamState.getMaxIndex() : this.mSafeMediaVolumeIndex;
                                }
                            }
                            if (checkSafeMediaVolume(streamTypeAlias, index, device)) {
                                onSetStreamVolume(streamType, index, flags, device, caller);
                                index = this.mStreamStates[streamType].getIndex(device);
                            } else {
                                this.mVolumeController.postDisplaySafeVolumeWarning(flags);
                                this.mPendingVolumeCommand = new StreamVolumeCommand(streamType, index, flags, device);
                            }
                        }
                        if (callingPackage.equals("com.tencent.mm")) {
                            Log.w(TAG, "com.tencent.mm sendVolumeUpdate streamType = " + streamType);
                            if (streamType == 3) {
                                flags = updateFlagsForSystemAudio(flags);
                            }
                            this.mVolumeController.postVolumeChanged(streamType, flags);
                        } else {
                            sendVolumeUpdate(streamType, oldIndex, index, flags);
                        }
                    }
                }
            }
        }
    }

    private boolean volumeAdjustmentAllowedByDnd(int streamTypeAlias, int flags) {
        if (this.mNm.getZenMode() == 2 && isStreamMutedByRingerMode(streamTypeAlias) && (flags & 2) == 0 && streamTypeAlias != getUiSoundsStreamType()) {
            return false;
        }
        return true;
    }

    public void forceVolumeControlStream(int streamType, IBinder cb) {
        synchronized (this.mForceControlStreamLock) {
            this.mVolumeControlStream = streamType;
            if (this.mVolumeControlStream != -1) {
                this.mForceControlStreamClient = new ForceControlStreamClient(cb);
            } else if (this.mForceControlStreamClient != null) {
                this.mForceControlStreamClient.release();
                this.mForceControlStreamClient = null;
            }
        }
    }

    private void sendBroadcastToAll(Intent intent) {
        intent.addFlags(67108864);
        intent.addFlags(268435456);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void sendStickyBroadcastToAll(Intent intent) {
        intent.addFlags(268435456);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private int getCurrentUserId() {
        long ident = Binder.clearCallingIdentity();
        try {
            int i = ActivityManagerNative.getDefault().getCurrentUser().id;
            return i;
        } catch (RemoteException e) {
            return 0;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void sendVolumeUpdate(int streamType, int oldIndex, int index, int flags) {
        streamType = this.mStreamVolumeAlias[streamType];
        if (streamType == 3) {
            flags = updateFlagsForSystemAudio(flags);
        }
        this.mVolumeController.postVolumeChanged(streamType, flags);
    }

    private int updateFlagsForSystemAudio(int flags) {
        if (this.mHdmiTvClient != null) {
            synchronized (this.mHdmiTvClient) {
                if (this.mHdmiSystemAudioSupported && (flags & 256) == 0) {
                    flags &= -2;
                }
            }
        }
        return flags;
    }

    private void sendMasterMuteUpdate(boolean muted, int flags) {
        this.mVolumeController.postMasterMuteChanged(updateFlagsForSystemAudio(flags));
        broadcastMasterMuteStatus(muted);
    }

    private void broadcastMasterMuteStatus(boolean muted) {
        Intent intent = new Intent("android.media.MASTER_MUTE_CHANGED_ACTION");
        intent.putExtra("android.media.EXTRA_MASTER_VOLUME_MUTED", muted);
        intent.addFlags(603979776);
        sendStickyBroadcastToAll(intent);
    }

    private void setStreamVolumeInt(int streamType, int index, int device, boolean force, String caller) {
        VolumeStreamState streamState = this.mStreamStates[streamType];
        Log.d(TAG, "Mits setStreamVolumeInt: streamType = " + streamType + " device: " + device + " index" + index);
        if (streamState.setIndex(index, device, caller) || force) {
            sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
        }
    }

    private void setSystemAudioMute(boolean state) {
        if (this.mHdmiManager != null && this.mHdmiTvClient != null) {
            synchronized (this.mHdmiManager) {
                if (this.mHdmiSystemAudioSupported) {
                    synchronized (this.mHdmiTvClient) {
                        long token = Binder.clearCallingIdentity();
                        try {
                            this.mHdmiTvClient.setSystemAudioMute(state);
                            Binder.restoreCallingIdentity(token);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(token);
                        }
                    }
                    return;
                }
            }
        }
    }

    public boolean isStreamMute(int streamType) {
        boolean -get3;
        if (streamType == Integer.MIN_VALUE) {
            streamType = getActiveStreamType(streamType);
        }
        synchronized (VolumeStreamState.class) {
            -get3 = this.mStreamStates[streamType].mIsMuted;
        }
        return -get3;
    }

    private boolean discardRmtSbmxFullVolDeathHandlerFor(IBinder cb) {
        Iterator<RmtSbmxFullVolDeathHandler> it = this.mRmtSbmxFullVolDeathHandlers.iterator();
        while (it.hasNext()) {
            RmtSbmxFullVolDeathHandler handler = (RmtSbmxFullVolDeathHandler) it.next();
            if (handler.isHandlerFor(cb)) {
                handler.forget();
                this.mRmtSbmxFullVolDeathHandlers.remove(handler);
                return true;
            }
        }
        return false;
    }

    private boolean hasRmtSbmxFullVolDeathHandlerFor(IBinder cb) {
        Iterator<RmtSbmxFullVolDeathHandler> it = this.mRmtSbmxFullVolDeathHandlers.iterator();
        while (it.hasNext()) {
            if (((RmtSbmxFullVolDeathHandler) it.next()).isHandlerFor(cb)) {
                return true;
            }
        }
        return false;
    }

    public void forceRemoteSubmixFullVolume(boolean startForcing, IBinder cb) {
        if (cb != null) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.CAPTURE_AUDIO_OUTPUT") != 0) {
                Log.w(TAG, "Trying to call forceRemoteSubmixFullVolume() without CAPTURE_AUDIO_OUTPUT");
                return;
            }
            synchronized (this.mRmtSbmxFullVolDeathHandlers) {
                boolean applyRequired = false;
                if (startForcing) {
                    if (!hasRmtSbmxFullVolDeathHandlerFor(cb)) {
                        this.mRmtSbmxFullVolDeathHandlers.add(new RmtSbmxFullVolDeathHandler(cb));
                        if (this.mRmtSbmxFullVolRefCount == 0) {
                            this.mFullVolumeDevices |= 32768;
                            this.mFixedVolumeDevices |= 32768;
                            applyRequired = true;
                        }
                        this.mRmtSbmxFullVolRefCount++;
                    }
                } else if (discardRmtSbmxFullVolDeathHandlerFor(cb) && this.mRmtSbmxFullVolRefCount > 0) {
                    this.mRmtSbmxFullVolRefCount--;
                    if (this.mRmtSbmxFullVolRefCount == 0) {
                        this.mFullVolumeDevices &= -32769;
                        this.mFixedVolumeDevices &= -32769;
                        applyRequired = true;
                    }
                }
                if (applyRequired) {
                    checkAllFixedVolumeDevices(3);
                    this.mStreamStates[3].applyAllVolumes();
                }
            }
        }
    }

    private void setMasterMuteInternal(boolean mute, int flags, String callingPackage, int uid, int userId) {
        if (uid == 1000) {
            uid = UserHandle.getUid(userId, UserHandle.getAppId(uid));
        }
        if (!mute && this.mAppOps.noteOp(33, uid, callingPackage) != 0) {
            return;
        }
        if (userId == UserHandle.getCallingUserId() || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            setMasterMuteInternalNoCallerCheck(mute, flags, userId);
        }
    }

    private void setMasterMuteInternalNoCallerCheck(boolean mute, int flags, int userId) {
        if (DEBUG_VOL) {
            String str = TAG;
            Object[] objArr = new Object[3];
            objArr[0] = Boolean.valueOf(mute);
            objArr[1] = Integer.valueOf(flags);
            objArr[2] = Integer.valueOf(userId);
            Log.d(str, String.format("Master mute %s, %d, user=%d", objArr));
        }
        if (!(this.mUseFixedVolume || getCurrentUserId() != userId || mute == AudioSystem.getMasterMute())) {
            setSystemAudioMute(mute);
            AudioSystem.setMasterMute(mute);
            sendMasterMuteUpdate(mute, flags);
            Intent intent = new Intent("android.media.MASTER_MUTE_CHANGED_ACTION");
            intent.putExtra("android.media.EXTRA_MASTER_VOLUME_MUTED", mute);
            sendBroadcastToAll(intent);
        }
    }

    public boolean isMasterMute() {
        return AudioSystem.getMasterMute();
    }

    public void setMasterMute(boolean mute, int flags, String callingPackage, int userId) {
        setMasterMuteInternal(mute, flags, callingPackage, Binder.getCallingUid(), userId);
    }

    public int getStreamVolume(int streamType) {
        int i;
        ensureValidStreamType(streamType);
        int device = getDeviceForStream(streamType);
        synchronized (VolumeStreamState.class) {
            int index = this.mStreamStates[streamType].getIndex(device);
            if (this.mStreamStates[streamType].mIsMuted) {
                index = 0;
            }
            if (!(index == 0 || this.mStreamVolumeAlias[streamType] != 3 || (this.mFixedVolumeDevices & device) == 0)) {
                index = this.mStreamStates[streamType].getMaxIndex();
            }
            i = (index + 5) / 10;
        }
        return i;
    }

    public int getOppoStreamVolume(int streamType, String callingPackage) {
        int i;
        ensureValidStreamType(streamType);
        int device = getDeviceForStream(streamType);
        if (streamType == 6 && (device & 112) == 0 && callingPackage.equals("com.android.bluetooth")) {
            device = 32;
        }
        synchronized (VolumeStreamState.class) {
            int index = this.mStreamStates[streamType].getIndex(device);
            if (this.mStreamStates[streamType].mIsMuted && callingPackage.equals("com.android.bluetooth")) {
                index = 0;
            }
            if (!(index == 0 || this.mStreamVolumeAlias[streamType] != 3 || (this.mFixedVolumeDevices & device) == 0)) {
                index = this.mStreamStates[streamType].getMaxIndex();
            }
            i = (index + 5) / 10;
        }
        return i;
    }

    private int getStreamVolumeByDevice(int streamType, int device) {
        int i;
        synchronized (VolumeStreamState.class) {
            int index = this.mStreamStates[streamType].getIndex(device);
            if (this.mStreamStates[streamType].mIsMuted) {
                index = 0;
            }
            if (!(index == 0 || this.mStreamVolumeAlias[streamType] != 3 || (this.mFixedVolumeDevices & device) == 0)) {
                index = this.mStreamStates[streamType].getMaxIndex();
            }
            i = (index + 5) / 10;
        }
        return i;
    }

    public int getStreamMaxVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (this.mStreamStates[streamType].getMaxIndex() + 5) / 10;
    }

    public int getStreamMinVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (this.mStreamStates[streamType].getMinIndex() + 5) / 10;
    }

    public int getLastAudibleStreamVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (this.mStreamStates[streamType].getIndex(getDeviceForStream(streamType)) + 5) / 10;
    }

    public int getUiSoundsStreamType() {
        return this.mStreamVolumeAlias[1];
    }

    public void setMicrophoneMute(boolean on, String callingPackage, int userId) {
        int uid = Binder.getCallingUid();
        if (uid == 1000) {
            uid = UserHandle.getUid(userId, UserHandle.getAppId(uid));
        }
        if ((!on && this.mAppOps.noteOp(44, uid, callingPackage) != 0) || !checkAudioSettingsPermission("setMicrophoneMute()")) {
            return;
        }
        if (userId == UserHandle.getCallingUserId() || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            setMicrophoneMuteNoCallerCheck(on, userId);
        }
    }

    private void setMicrophoneMuteNoCallerCheck(boolean on, int userId) {
        if (DEBUG_VOL) {
            String str = TAG;
            Object[] objArr = new Object[2];
            objArr[0] = Boolean.valueOf(on);
            objArr[1] = Integer.valueOf(userId);
            Log.d(str, String.format("Mic mute %s, user=%d", objArr));
        }
        if (getCurrentUserId() == userId) {
            AudioSystem.muteMicrophone(on);
        }
    }

    public int getRingerModeExternal() {
        int i;
        synchronized (this.mSettingsLock) {
            i = this.mRingerModeExternal;
        }
        return i;
    }

    public int getRingerModeInternal() {
        int i;
        synchronized (this.mSettingsLock) {
            i = this.mRingerMode;
        }
        return i;
    }

    private void ensureValidRingerMode(int ringerMode) {
        if (!isValidRingerMode(ringerMode)) {
            throw new IllegalArgumentException("Bad ringer mode " + ringerMode);
        }
    }

    public boolean isValidRingerMode(int ringerMode) {
        return ringerMode >= 0 && ringerMode <= 2;
    }

    public void setRingerModeExternal(int ringerMode, String caller) {
        if (isAndroidNPlus(caller) && wouldToggleZenMode(ringerMode) && !this.mNm.isNotificationPolicyAccessGrantedForPackage(caller)) {
            throw new SecurityException("Not allowed to change Do Not Disturb state");
        }
        setRingerMode(ringerMode, caller, true);
    }

    public void setRingerModeInternal(int ringerMode, String caller) {
        enforceVolumeController("setRingerModeInternal");
        setRingerMode(ringerMode, caller, false);
    }

    private void setRingerMode(int ringerMode, String caller, boolean external) {
        if (!this.mUseFixedVolume && !isPlatformTelevision()) {
            if (caller == null || caller.length() == 0) {
                throw new IllegalArgumentException("Bad caller: " + caller);
            }
            ensureValidRingerMode(ringerMode);
            if (ringerMode == 1 && !this.mHasVibrator) {
                ringerMode = 0;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (this.mSettingsLock) {
                    int ringerModeInternal = getRingerModeInternal();
                    int ringerModeExternal = getRingerModeExternal();
                    if (external) {
                        setRingerModeExt(ringerMode);
                        if (this.mRingerModeDelegate != null) {
                            ringerMode = this.mRingerModeDelegate.onSetRingerModeExternal(ringerModeExternal, ringerMode, caller, ringerModeInternal, this.mVolumePolicy);
                        }
                        if (ringerMode != ringerModeInternal) {
                            setRingerModeInt(ringerMode, true);
                        }
                    } else {
                        if (ringerMode != ringerModeInternal) {
                            setRingerModeInt(ringerMode, true);
                        }
                        if (this.mRingerModeDelegate != null) {
                            ringerMode = this.mRingerModeDelegate.onSetRingerModeInternal(ringerModeInternal, ringerMode, caller, ringerModeExternal, this.mVolumePolicy);
                        }
                        setRingerModeExt(ringerMode);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private void setRingerModeExt(int ringerMode) {
        synchronized (this.mSettingsLock) {
            if (ringerMode == this.mRingerModeExternal) {
                return;
            }
            this.mRingerModeExternal = ringerMode;
            broadcastRingerMode("android.media.RINGER_MODE_CHANGED", ringerMode);
        }
    }

    private void muteRingerModeStreams() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        boolean ringerModeMute = this.mRingerMode != 1 ? this.mRingerMode == 0 : true;
        int streamType = numStreamTypes - 1;
        while (streamType >= 0) {
            boolean isMuted = isStreamMutedByRingerMode(streamType);
            boolean shouldMute = ringerModeMute ? isStreamAffectedByRingerMode(streamType) : false;
            if (isMuted != shouldMute) {
                if (shouldMute) {
                    this.mStreamStates[streamType].mute(true);
                    this.mRingerModeMutedStreams |= 1 << streamType;
                } else {
                    if (this.mStreamVolumeAlias[streamType] == 2) {
                        synchronized (VolumeStreamState.class) {
                            VolumeStreamState vss = this.mStreamStates[streamType];
                            for (int i = 0; i < vss.mIndexMap.size(); i++) {
                                int device = vss.mIndexMap.keyAt(i);
                                if (vss.mIndexMap.valueAt(i) == 0) {
                                    vss.setIndex(10, device, TAG);
                                }
                            }
                            sendMsg(this.mAudioHandler, 1, 2, getDeviceForStream(streamType), 0, this.mStreamStates[streamType], 500);
                        }
                    }
                    this.mStreamStates[streamType].mute(false);
                    this.mRingerModeMutedStreams &= ~(1 << streamType);
                }
            }
            streamType--;
        }
    }

    private void setRingerModeInt(int ringerMode, boolean persist) {
        boolean change;
        if (DEBUG_VOL) {
            Log.d(TAG, "setRingerModeInt: Ringermode = " + ringerMode + ", old RingerMode = " + this.mRingerMode);
        }
        synchronized (this.mSettingsLock) {
            change = this.mRingerMode != ringerMode;
            this.mRingerMode = ringerMode;
        }
        muteRingerModeStreams();
        if (persist) {
            sendMsg(this.mAudioHandler, 3, 0, 0, 0, null, 500);
        }
        if (change) {
            broadcastRingerMode("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION", ringerMode);
        }
    }

    public boolean shouldVibrate(int vibrateType) {
        boolean z = true;
        if (!this.mHasVibrator) {
            return false;
        }
        switch (getVibrateSetting(vibrateType)) {
            case 0:
                return false;
            case 1:
                if (getRingerModeExternal() == 0) {
                    z = false;
                }
                return z;
            case 2:
                if (getRingerModeExternal() != 1) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    public int getVibrateSetting(int vibrateType) {
        if (this.mHasVibrator) {
            return (this.mVibrateSetting >> (vibrateType * 2)) & 3;
        }
        return 0;
    }

    public void setVibrateSetting(int vibrateType, int vibrateSetting) {
        if (this.mHasVibrator) {
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(this.mVibrateSetting, vibrateType, vibrateSetting);
            broadcastVibrateSetting(vibrateType);
        }
    }

    /* JADX WARNING: Missing block: B:21:0x002c, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isInvalidMode(int mode, String callingPackage) {
        if ((this.mMode == 2 || this.mMode == 4) && mode == 3) {
            return true;
        }
        synchronized (this.mSettingCallLock) {
            if (mode == 2 || mode == 4) {
                this.mIsInSetCallMode = true;
            }
            if (this.mIsInSetCallMode && mode == 3) {
                Log.w(TAG, "Phone is in set Call mode, denial set communication mode.");
                return true;
            }
        }
    }

    public void setMode(int mode, IBinder cb, String callingPackage) {
        Log.d(TAG, "setMode(mode=" + mode + ", callingPackage=" + callingPackage + ")");
        if (!checkAudioSettingsPermission("setMode()")) {
            if (DEBUG_VOL) {
                Log.e(TAG, "setMode: No permission!");
            }
        } else if ((mode == 2 || mode == 4) && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            Log.w(TAG, "MODIFY_PHONE_STATE Permission Denial: setMode(MODE_IN_CALL) from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        } else if (mode < -1 || mode >= 6) {
            if (DEBUG_VOL) {
                Log.e(TAG, "setMode: Invalid mode!");
            }
        } else if (isInvalidMode(mode, callingPackage)) {
            Log.w(TAG, "MODIFY_PHONE_STATE Permission Denial: Current mode is in call");
        } else {
            int newModeOwnerPid;
            synchronized (this.mSetModeDeathHandlers) {
                if (mode == -1) {
                    mode = this.mMode;
                }
                newModeOwnerPid = setModeInt(mode, cb, Binder.getCallingPid(), callingPackage);
            }
            if (newModeOwnerPid != 0) {
                disconnectBluetoothSco(newModeOwnerPid);
            }
        }
    }

    private int setModeInt(int mode, IBinder cb, int pid, String caller) {
        if (DEBUG_MODE) {
            Log.v(TAG, "setModeInt(mode=" + mode + ", pid=" + pid + ", caller=" + caller + ")");
        }
        int newModeOwnerPid = 0;
        if (cb == null) {
            Log.e(TAG, "setModeInt() called with null binder");
            synchronized (this.mSettingCallLock) {
                if (this.mIsInSetCallMode) {
                    this.mIsInSetCallMode = false;
                }
            }
            return 0;
        }
        int status;
        SetModeDeathHandler hdlr = null;
        Iterator iter = this.mSetModeDeathHandlers.iterator();
        while (iter.hasNext()) {
            SetModeDeathHandler h = (SetModeDeathHandler) iter.next();
            if (h.getPid() == pid) {
                hdlr = h;
                iter.remove();
                h.getBinder().unlinkToDeath(h, 0);
                break;
            }
        }
        do {
            if (mode != 0) {
                if (hdlr == null) {
                    hdlr = new SetModeDeathHandler(cb, pid);
                }
                try {
                    cb.linkToDeath(hdlr, 0);
                } catch (RemoteException e) {
                    Log.w(TAG, "setMode() could not link to " + cb + " binder death");
                }
                this.mSetModeDeathHandlers.add(0, hdlr);
                hdlr.setMode(mode);
            } else if (!this.mSetModeDeathHandlers.isEmpty()) {
                hdlr = (SetModeDeathHandler) this.mSetModeDeathHandlers.get(0);
                cb = hdlr.getBinder();
                mode = hdlr.getMode();
                if (DEBUG_MODE) {
                    Log.w(TAG, " using mode=" + mode + " instead due to death hdlr at pid=" + hdlr.mPid);
                }
            }
            if (mode != this.mMode) {
                status = AudioSystem.setPhoneState(mode);
                if (status == 0) {
                    if (DEBUG_MODE) {
                        Log.v(TAG, " mode successfully set to " + mode);
                    }
                    this.mMode = mode;
                } else {
                    if (hdlr != null) {
                        this.mSetModeDeathHandlers.remove(hdlr);
                        cb.unlinkToDeath(hdlr, 0);
                    }
                    if (DEBUG_MODE) {
                        Log.w(TAG, " mode set to MODE_NORMAL after phoneState pb");
                    }
                    mode = 0;
                }
                notifyMultimediaServiceModesUpdate(false);
            } else {
                status = 0;
                notifyMultimediaServiceModesUpdate(true);
            }
            if (status == 0) {
                break;
            }
        } while (!this.mSetModeDeathHandlers.isEmpty());
        if (status == 0) {
            if (mode != 0) {
                if (this.mSetModeDeathHandlers.isEmpty()) {
                    Log.e(TAG, "setMode() different from MODE_NORMAL with empty mode client stack");
                } else {
                    newModeOwnerPid = ((SetModeDeathHandler) this.mSetModeDeathHandlers.get(0)).getPid();
                }
            }
            int streamType = getActiveStreamType(Integer.MIN_VALUE);
            int device = getDeviceForStream(streamType);
            int index = this.mStreamStates[this.mStreamVolumeAlias[streamType]].getIndex(device);
            setStreamVolumeInt(this.mStreamVolumeAlias[streamType], index, device, true, caller);
            updateStreamVolumeAlias(true, caller);
        }
        synchronized (this.mSettingCallLock) {
            if (this.mIsInSetCallMode) {
                this.mIsInSetCallMode = false;
            }
        }
        return newModeOwnerPid;
    }

    private void notifyMultimediaServiceModesUpdate(boolean onlyRead) {
        String modekey = "allmodes=";
        String allmodes = IElsaManager.EMPTY_PACKAGE;
        Log.d(TAG, "+notifyMultimediaServiceModesUpdate");
        Iterator iter = this.mSetModeDeathHandlers.iterator();
        while (iter.hasNext()) {
            SetModeDeathHandler h = (SetModeDeathHandler) iter.next();
            allmodes = allmodes + h.getPid() + "," + h.getMode() + ",";
            Log.d(TAG, "allmodes = " + allmodes);
        }
        Log.d(TAG, "modekey = " + (modekey + allmodes));
        if (onlyRead) {
            OppoMultimediaManager.getInstance(this.mContext).setEventInfo(7, allmodes);
        } else {
            OppoMultimediaManager.getInstance(this.mContext).setEventInfo(5, allmodes);
        }
        Log.d(TAG, "-notifyMultimediaServiceModesUpdate");
    }

    public int getMode() {
        return this.mMode;
    }

    private void loadTouchSoundAssetDefaults() {
        SOUND_EFFECT_FILES.add("Effect_Tick.ogg");
        for (int i = 0; i < 10; i++) {
            this.SOUND_EFFECT_FILES_MAP[i][0] = 0;
            this.SOUND_EFFECT_FILES_MAP[i][1] = -1;
        }
    }

    private void loadTouchSoundAssets() {
        XmlResourceParser xmlResourceParser = null;
        if (SOUND_EFFECT_FILES.isEmpty()) {
            loadTouchSoundAssetDefaults();
            try {
                xmlResourceParser = this.mContext.getResources().getXml(17891329);
                XmlUtils.beginDocument(xmlResourceParser, TAG_AUDIO_ASSETS);
                boolean inTouchSoundsGroup = false;
                if (ASSET_FILE_VERSION.equals(xmlResourceParser.getAttributeValue(null, "version"))) {
                    String element;
                    while (true) {
                        XmlUtils.nextElement(xmlResourceParser);
                        element = xmlResourceParser.getName();
                        if (element == null) {
                            break;
                        } else if (element.equals(TAG_GROUP)) {
                            if (GROUP_TOUCH_SOUNDS.equals(xmlResourceParser.getAttributeValue(null, ATTR_GROUP_NAME))) {
                                inTouchSoundsGroup = true;
                                break;
                            }
                        }
                    }
                    while (inTouchSoundsGroup) {
                        XmlUtils.nextElement(xmlResourceParser);
                        element = xmlResourceParser.getName();
                        if (element == null || !element.equals(TAG_ASSET)) {
                            break;
                        }
                        String id = xmlResourceParser.getAttributeValue(null, "id");
                        String file = xmlResourceParser.getAttributeValue(null, ATTR_ASSET_FILE);
                        try {
                            int fx = AudioManager.class.getField(id).getInt(null);
                            int i = SOUND_EFFECT_FILES.indexOf(file);
                            if (i == -1) {
                                i = SOUND_EFFECT_FILES.size();
                                SOUND_EFFECT_FILES.add(file);
                            }
                            this.SOUND_EFFECT_FILES_MAP[fx][0] = i;
                        } catch (Exception e) {
                            Log.w(TAG, "Invalid touch sound ID: " + id);
                        }
                    }
                }
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (NotFoundException e2) {
                Log.w(TAG, "audio assets file not found", e2);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (XmlPullParserException e3) {
                Log.w(TAG, "XML parser exception reading touch sound assets", e3);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (IOException e4) {
                Log.w(TAG, "I/O exception reading touch sound assets", e4);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (Throwable th) {
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            }
        }
    }

    public void playSoundEffect(int effectType) {
        playSoundEffectVolume(effectType, -1.0f);
    }

    public void playSoundEffectVolume(int effectType, float volume) {
        if (effectType >= 10 || effectType < 0) {
            Log.w(TAG, "AudioService effectType value " + effectType + " out of range");
            return;
        }
        sendMsg(this.mAudioHandler, 5, 2, effectType, (int) (1000.0f * volume), null, 0);
    }

    /* JADX WARNING: Missing block: B:16:0x0027, code:
            if (r5.mStatus != 0) goto L_0x0039;
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:34:?, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean loadSoundEffects() {
        Throwable th;
        int attempts = 3;
        LoadSoundEffectReply reply = new LoadSoundEffectReply();
        synchronized (reply) {
            try {
                sendMsg(this.mAudioHandler, 7, 2, 0, 0, reply, 0);
                while (true) {
                    int attempts2;
                    try {
                        attempts2 = attempts;
                        if (reply.mStatus != 1) {
                            attempts = attempts2;
                            break;
                        }
                        attempts = attempts2 - 1;
                        if (attempts2 <= 0) {
                            break;
                        }
                        reply.wait(5000);
                    } catch (Throwable th2) {
                        th = th2;
                        attempts = attempts2;
                    }
                }
            } catch (InterruptedException e) {
                Log.w(TAG, "loadSoundEffects Interrupted while waiting sound pool loaded.");
            } catch (Throwable th3) {
                th = th3;
            }
        }
        throw th;
    }

    public void unloadSoundEffects() {
        sendMsg(this.mAudioHandler, 20, 2, 0, 0, null, 0);
    }

    public void reloadAudioSettings() {
        readAudioSettings(false);
    }

    private void readAudioSettings(boolean userSwitch) {
        readPersistedSettings();
        readUserRestrictions();
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        int streamType = 0;
        while (streamType < numStreamTypes) {
            VolumeStreamState streamState = this.mStreamStates[streamType];
            if (!userSwitch || this.mStreamVolumeAlias[streamType] != 3) {
                streamState.readSettings();
                synchronized (VolumeStreamState.class) {
                    if (streamState.mIsMuted && (!(isStreamAffectedByMute(streamType) || isStreamMutedByRingerMode(streamType)) || this.mUseFixedVolume)) {
                        streamState.mIsMuted = false;
                    }
                }
            }
            streamType++;
        }
        setRingerModeInt(getRingerModeInternal(), false);
        checkAllFixedVolumeDevices();
        checkAllAliasStreamVolumes();
        checkMuteAffectedStreams();
        synchronized (this.mSafeMediaVolumeState) {
            this.mMusicActiveMs = MathUtils.constrain(Secure.getIntForUser(this.mContentResolver, "unsafe_volume_music_active_ms", 0, -2), 0, UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX);
            if (this.mSafeMediaVolumeState.intValue() == 3) {
                enforceSafeMediaVolume(TAG);
            }
        }
    }

    public void setSpeakerphoneOn(boolean on) {
        if (checkAudioSettingsPermission("setSpeakerphoneOn()")) {
            if (on) {
                if (this.mForcedUseForComm == 3) {
                    sendMsg(this.mAudioHandler, 8, 2, 2, 0, null, 0);
                }
                this.mForcedUseForComm = 1;
            } else if (this.mForcedUseForComm == 1) {
                this.mForcedUseForComm = 0;
            }
            sendMsg(this.mAudioHandler, 8, 2, 0, this.mForcedUseForComm, null, 0);
            if (DEBUG_VOL) {
                Log.d(TAG, "setSpeakerphoneOn " + this.mForcedUseForComm);
            }
        }
    }

    public boolean isSpeakerphoneOn() {
        return this.mForcedUseForComm == 1;
    }

    public void setBluetoothScoOn(boolean on) {
        if (checkAudioSettingsPermission("setBluetoothScoOn()")) {
            setBluetoothScoOnInt(on);
        }
    }

    public void setBluetoothScoOnInt(boolean on) {
        if (on) {
            this.mForcedUseForComm = 3;
        } else if (this.mForcedUseForComm == 3) {
            this.mForcedUseForComm = 0;
        }
        sendMsg(this.mAudioHandler, 8, 2, 0, this.mForcedUseForComm, null, 0);
        sendMsg(this.mAudioHandler, 8, 2, 2, this.mForcedUseForComm, null, 0);
        if (DEBUG_VOL) {
            Log.d(TAG, "setBluetoothScoOn " + this.mForcedUseForComm);
        }
    }

    public boolean isBluetoothScoOn() {
        if (DEBUG_VOL) {
            Log.d(TAG, "isBluetoothScoOn " + this.mForcedUseForComm);
        }
        return this.mForcedUseForComm == 3;
    }

    public void setBluetoothA2dpOn(boolean on) {
        int i = 0;
        synchronized (this.mBluetoothA2dpEnabledLock) {
            this.mBluetoothA2dpEnabled = on;
            Handler handler = this.mAudioHandler;
            if (!this.mBluetoothA2dpEnabled) {
                i = 10;
            }
            sendMsg(handler, 13, 2, 1, i, null, 0);
        }
    }

    public boolean isBluetoothA2dpOn() {
        boolean z;
        synchronized (this.mBluetoothA2dpEnabledLock) {
            z = this.mBluetoothA2dpEnabled;
        }
        return z;
    }

    public void startBluetoothSco(IBinder cb, int targetSdkVersion) {
        if (DEBUG_VOL) {
            Log.d(TAG, "startBluetoothSco");
        }
        startBluetoothScoInt(cb, targetSdkVersion < 18 ? 0 : -1);
    }

    public void startBluetoothScoVirtualCall(IBinder cb) {
        startBluetoothScoInt(cb, 0);
    }

    void startBluetoothScoInt(IBinder cb, int scoAudioMode) {
        if (DEBUG_VOL) {
            Log.d(TAG, "startBluetoothScoInt: scoAudioMode = " + scoAudioMode);
        }
        if (checkAudioSettingsPermission("startBluetoothSco()") && this.mSystemReady) {
            ScoClient client = getScoClient(cb, true);
            long ident = Binder.clearCallingIdentity();
            client.incCount(scoAudioMode);
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void stopBluetoothSco(IBinder cb) {
        if (DEBUG_VOL) {
            Log.d(TAG, "stopBluetoothSco");
        }
        if (checkAudioSettingsPermission("stopBluetoothSco()") && this.mSystemReady) {
            ScoClient client = getScoClient(cb, false);
            long ident = Binder.clearCallingIdentity();
            if (client != null) {
                client.decCount();
            }
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void checkScoAudioState() {
        if (this.mBluetoothHeadset != null && this.mBluetoothHeadsetDevice != null && this.mScoAudioState == 0 && this.mBluetoothHeadset.getAudioState(this.mBluetoothHeadsetDevice) != 10) {
            this.mScoAudioState = 2;
        }
    }

    /* JADX WARNING: Missing block: B:21:0x002f, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ScoClient getScoClient(IBinder cb, boolean create) {
        Throwable th;
        synchronized (this.mScoClients) {
            try {
                ScoClient client;
                int size = this.mScoClients.size();
                int i = 0;
                ScoClient client2 = null;
                while (i < size) {
                    try {
                        client = (ScoClient) this.mScoClients.get(i);
                        if (client.getBinder() == cb) {
                            return client;
                        }
                        i++;
                        client2 = client;
                    } catch (Throwable th2) {
                        th = th2;
                        client = client2;
                    }
                }
                if (create) {
                    client = new ScoClient(cb);
                    this.mScoClients.add(client);
                } else {
                    client = client2;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
        throw th;
    }

    public void clearAllScoClients(int exceptPid, boolean stopSco) {
        synchronized (this.mScoClients) {
            Object savedClient = null;
            int size = this.mScoClients.size();
            for (int i = 0; i < size; i++) {
                ScoClient cl = (ScoClient) this.mScoClients.get(i);
                if (cl.getPid() != exceptPid) {
                    cl.clearCount(stopSco);
                } else {
                    ScoClient savedClient2 = cl;
                }
            }
            this.mScoClients.clear();
            if (savedClient2 != null) {
                this.mScoClients.add(savedClient2);
            }
        }
    }

    private boolean getBluetoothHeadset() {
        int i;
        boolean result = false;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            result = adapter.getProfileProxy(this.mContext, this.mBluetoothProfileServiceListener, 1);
        }
        Handler handler = this.mAudioHandler;
        if (result) {
            i = 3000;
        } else {
            i = 0;
        }
        sendMsg(handler, 9, 0, 0, 0, null, i);
        return result;
    }

    private void disconnectBluetoothSco(int exceptPid) {
        if (DEBUG_VOL) {
            Log.d(TAG, "disconnectBluetoothSco: exceptPid = " + exceptPid);
        }
        synchronized (this.mScoClients) {
            checkScoAudioState();
            if (this.mScoAudioState != 2 && this.mScoAudioState != 4) {
                clearAllScoClients(exceptPid, true);
            } else if (this.mBluetoothHeadsetDevice != null) {
                if (this.mBluetoothHeadset != null) {
                    if (!this.mBluetoothHeadset.stopVoiceRecognition(this.mBluetoothHeadsetDevice)) {
                        sendMsg(this.mAudioHandler, 9, 0, 0, 0, null, 0);
                    }
                } else if (this.mScoAudioState == 2 && getBluetoothHeadset()) {
                    this.mScoAudioState = 4;
                }
            }
        }
    }

    private void resetBluetoothSco() {
        synchronized (this.mScoClients) {
            clearAllScoClients(0, false);
            this.mScoAudioState = 0;
            broadcastScoConnectionState(0);
        }
        AudioSystem.setParameters("A2dpSuspended=false");
        setBluetoothScoOnInt(false);
    }

    private void broadcastScoConnectionState(int state) {
        sendMsg(this.mAudioHandler, 19, 2, state, 0, null, 0);
    }

    private void onBroadcastScoConnectionState(int state) {
        if (state != this.mScoConnectionState) {
            Intent newIntent = new Intent("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
            newIntent.putExtra("android.media.extra.SCO_AUDIO_STATE", state);
            newIntent.putExtra("android.media.extra.SCO_AUDIO_PREVIOUS_STATE", this.mScoConnectionState);
            sendStickyBroadcastToAll(newIntent);
            this.mScoConnectionState = state;
        }
    }

    void setBtScoDeviceConnectionState(BluetoothDevice btDevice, int state) {
        if (btDevice != null) {
            boolean success;
            String address = btDevice.getAddress();
            BluetoothClass btClass = btDevice.getBluetoothClass();
            int outDevice = 16;
            if (btClass != null) {
                switch (btClass.getDeviceClass()) {
                    case 1028:
                    case 1032:
                        outDevice = 32;
                        break;
                    case 1056:
                        outDevice = 64;
                        break;
                }
            }
            if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                address = IElsaManager.EMPTY_PACKAGE;
            }
            boolean connected = state == 2;
            String btDeviceName = btDevice.getName();
            if (handleDeviceConnection(connected, outDevice, address, btDeviceName)) {
                success = handleDeviceConnection(connected, -2147483640, address, btDeviceName);
            } else {
                success = false;
            }
            if (success) {
                synchronized (this.mScoClients) {
                    if (connected) {
                        this.mBluetoothHeadsetDevice = btDevice;
                    } else {
                        this.mBluetoothHeadsetDevice = null;
                        resetBluetoothSco();
                    }
                }
            }
        }
    }

    void disconnectAllBluetoothProfiles() {
        disconnectA2dp();
        disconnectA2dpSink();
        disconnectHeadset();
    }

    /* JADX WARNING: Missing block: B:18:0x0031, code:
            if (r4 == null) goto L_0x004d;
     */
    /* JADX WARNING: Missing block: B:19:0x0033, code:
            r0 = checkSendBecomingNoisyIntent(128, 0);
            r2 = 0;
     */
    /* JADX WARNING: Missing block: B:21:0x003f, code:
            if (r2 >= r4.size()) goto L_0x004d;
     */
    /* JADX WARNING: Missing block: B:22:0x0041, code:
            makeA2dpDeviceUnavailableLater((java.lang.String) r4.valueAt(r2), r0);
     */
    /* JADX WARNING: Missing block: B:23:0x004a, code:
            r2 = r2 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void disconnectA2dp() {
        Throwable th;
        synchronized (this.mConnectedDevices) {
            synchronized (this.mA2dpAvrcpLock) {
                ArraySet<String> toRemove = null;
                int i = 0;
                while (true) {
                    ArraySet<String> toRemove2;
                    try {
                        toRemove2 = toRemove;
                        if (i >= this.mConnectedDevices.size()) {
                            break;
                        }
                        DeviceListSpec deviceSpec = (DeviceListSpec) this.mConnectedDevices.valueAt(i);
                        if (deviceSpec.mDeviceType == 128) {
                            toRemove = toRemove2 != null ? toRemove2 : new ArraySet();
                            try {
                                toRemove.add(deviceSpec.mDeviceAddress);
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        } else {
                            toRemove = toRemove2;
                        }
                        i++;
                    } catch (Throwable th3) {
                        th = th3;
                        toRemove = toRemove2;
                        throw th;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:23:0x0044, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void disconnectA2dpSink() {
        Throwable th;
        synchronized (this.mConnectedDevices) {
            ArraySet<String> toRemove = null;
            int i = 0;
            while (true) {
                ArraySet<String> toRemove2;
                try {
                    toRemove2 = toRemove;
                    if (i < this.mConnectedDevices.size()) {
                        DeviceListSpec deviceSpec = (DeviceListSpec) this.mConnectedDevices.valueAt(i);
                        if (deviceSpec.mDeviceType == -2147352576) {
                            toRemove = toRemove2 != null ? toRemove2 : new ArraySet();
                            try {
                                toRemove.add(deviceSpec.mDeviceAddress);
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        } else {
                            toRemove = toRemove2;
                        }
                        i++;
                    } else if (toRemove2 != null) {
                        for (i = 0; i < toRemove2.size(); i++) {
                            makeA2dpSrcUnavailable((String) toRemove2.valueAt(i));
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    toRemove = toRemove2;
                    throw th;
                }
            }
        }
    }

    void disconnectHeadset() {
        synchronized (this.mScoClients) {
            if (this.mBluetoothHeadsetDevice != null) {
                setBtScoDeviceConnectionState(this.mBluetoothHeadsetDevice, 0);
            }
            if (isBluetoothScoOn()) {
                Log.d(TAG, "setBluetoothScoOn to false, onServiceDisconnected");
                setBluetoothScoOn(false);
            }
            this.mBluetoothHeadset = null;
        }
    }

    private void onCheckMusicActive(String caller) {
        synchronized (this.mSafeMediaVolumeState) {
            if (this.mSafeMediaVolumeState.intValue() == 2) {
                int device = getDeviceForStream(3);
                if ((device & 12) != 0) {
                    sendMsg(this.mAudioHandler, 14, 0, 0, 0, caller, 60000);
                    int index = this.mStreamStates[3].getIndex(device);
                    if (AudioSystem.isStreamActive(3, 0) && index > this.mSafeMediaVolumeIndex) {
                        this.mMusicActiveMs += 60000;
                        if (this.mMusicActiveMs > UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX) {
                            setSafeMediaVolumeEnabled(true, caller);
                            this.mMusicActiveMs = 0;
                        }
                        saveMusicActiveMs();
                    }
                }
            }
        }
    }

    private void saveMusicActiveMs() {
        this.mAudioHandler.obtainMessage(22, this.mMusicActiveMs, 0).sendToTarget();
    }

    private void onConfigureSafeVolume(boolean force, String caller) {
        synchronized (this.mSafeMediaVolumeState) {
            int mcc = this.mContext.getResources().getConfiguration().mcc;
            if (this.mMcc != mcc || (this.mMcc == 0 && force)) {
                boolean safeMediaVolumeEnabled;
                int persistedState;
                this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(17694862) * 10;
                if (SystemProperties.getBoolean("audio.safemedia.force", false)) {
                    safeMediaVolumeEnabled = true;
                } else {
                    safeMediaVolumeEnabled = this.mContext.getResources().getBoolean(17956987);
                }
                boolean safeMediaVolumeBypass = SystemProperties.getBoolean("audio.safemedia.bypass", false);
                if (!safeMediaVolumeEnabled || safeMediaVolumeBypass) {
                    persistedState = 1;
                    this.mSafeMediaVolumeState = Integer.valueOf(1);
                } else {
                    persistedState = 3;
                    if (this.mSafeMediaVolumeState.intValue() != 2) {
                        if (this.mMusicActiveMs == 0) {
                            this.mSafeMediaVolumeState = Integer.valueOf(3);
                            enforceSafeMediaVolume(caller);
                        } else {
                            this.mSafeMediaVolumeState = Integer.valueOf(2);
                        }
                    }
                }
                this.mMcc = mcc;
                sendMsg(this.mAudioHandler, 18, 2, persistedState, 0, null, 0);
            }
        }
    }

    private int checkForRingerModeChange(int oldIndex, int direction, int step, boolean isMuted, String caller, int flags) {
        boolean isTv = this.mPlatformType == 2;
        int result = 1;
        int ringerMode = getRingerModeInternal();
        switch (ringerMode) {
            case 0:
                if (isTv && direction == -1 && oldIndex >= step * 2 && isMuted) {
                    ringerMode = 2;
                } else if (direction == 1 || direction == 101 || direction == 100) {
                    if (this.mVolumePolicy.volumeUpToExitSilent) {
                        ringerMode = (this.mHasVibrator && direction == 1) ? 1 : 2;
                    } else {
                        result = 129;
                    }
                }
                result &= -2;
                break;
            case 1:
                if (!this.mHasVibrator) {
                    Log.e(TAG, "checkForRingerModeChange() current ringer mode is vibratebut no vibrator is present");
                    break;
                }
                if (direction == -1) {
                    if (isTv && oldIndex >= step * 2 && isMuted) {
                        ringerMode = 2;
                    } else if (this.mPrevVolDirection != -1) {
                        if (!this.mVolumePolicy.volumeDownToEnterSilent) {
                            result = 2049;
                        } else if (SystemClock.uptimeMillis() - this.mLoweredFromNormalToVibrateTime > ((long) this.mVolumePolicy.vibrateToSilentDebounce) && this.mRingerModeDelegate.canVolumeDownEnterSilent()) {
                            ringerMode = 0;
                        }
                    }
                } else if (direction == 1 || direction == 101 || direction == 100) {
                    ringerMode = 2;
                }
                result &= -2;
                break;
                break;
            case 2:
                if (direction != -1) {
                    if (isTv && (direction == 101 || direction == -100)) {
                        if (this.mHasVibrator) {
                            ringerMode = 1;
                        } else {
                            ringerMode = 0;
                        }
                        result = 0;
                        break;
                    }
                } else if (!this.mHasVibrator) {
                    if (oldIndex == step && this.mVolumePolicy.volumeDownToEnterSilent) {
                        ringerMode = 0;
                        break;
                    }
                } else if (step <= oldIndex && oldIndex < step * 2) {
                    ringerMode = 1;
                    this.mLoweredFromNormalToVibrateTime = SystemClock.uptimeMillis();
                    break;
                }
            default:
                Log.e(TAG, "checkForRingerModeChange() wrong ringer mode: " + ringerMode);
                break;
        }
        if (isAndroidNPlus(caller) && wouldToggleZenMode(ringerMode) && !this.mNm.isNotificationPolicyAccessGrantedForPackage(caller) && (flags & 4096) == 0) {
            throw new SecurityException("Not allowed to change Do Not Disturb state");
        }
        setRingerMode(ringerMode, "AudioService.checkForRingerModeChange", false);
        this.mPrevVolDirection = direction;
        return result;
    }

    public boolean isStreamAffectedByRingerMode(int streamType) {
        return (this.mRingerModeAffectedStreams & (1 << streamType)) != 0;
    }

    private boolean isStreamMutedByRingerMode(int streamType) {
        return (this.mRingerModeMutedStreams & (1 << streamType)) != 0;
    }

    private boolean updateRingerModeAffectedStreams() {
        int ringerModeAffectedStreams = System.getIntForUser(this.mContentResolver, "mode_ringer_streams_affected", 166, -2);
        if (this.mPlatformType == 2) {
            ringerModeAffectedStreams = 0;
        } else if (this.mRingerModeDelegate != null) {
            ringerModeAffectedStreams = this.mRingerModeDelegate.getRingerModeAffectedStreams(ringerModeAffectedStreams);
        }
        synchronized (this.mCameraSoundForced) {
            if (this.mCameraSoundForced.booleanValue()) {
                ringerModeAffectedStreams &= -129;
            } else {
                ringerModeAffectedStreams |= 128;
            }
        }
        if (this.mStreamVolumeAlias[8] == 2) {
            ringerModeAffectedStreams |= 256;
        } else {
            ringerModeAffectedStreams &= -257;
        }
        if (ringerModeAffectedStreams == this.mRingerModeAffectedStreams) {
            return false;
        }
        System.putIntForUser(this.mContentResolver, "mode_ringer_streams_affected", ringerModeAffectedStreams, -2);
        this.mRingerModeAffectedStreams = ringerModeAffectedStreams;
        return true;
    }

    public boolean isStreamAffectedByMute(int streamType) {
        return (this.mMuteAffectedStreams & (1 << streamType)) != 0;
    }

    private void ensureValidDirection(int direction) {
        switch (direction) {
            case -100:
            case -1:
            case 0:
            case 1:
            case 100:
            case 101:
                return;
            default:
                throw new IllegalArgumentException("Bad direction " + direction);
        }
    }

    private void ensureValidStreamType(int streamType) {
        if (streamType < 0 || streamType >= this.mStreamStates.length) {
            throw new IllegalArgumentException("Bad stream type " + streamType);
        }
    }

    private boolean isMuteAdjust(int adjust) {
        if (adjust == -100 || adjust == 100 || adjust == 101) {
            return true;
        }
        return false;
    }

    private boolean isInCommunication() {
        boolean z = true;
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        long ident = Binder.clearCallingIdentity();
        boolean IsInCall = telecomManager.isInCall();
        Binder.restoreCallingIdentity(ident);
        Log.d(TAG, "isInCommunication mode :" + getMode());
        if (getMode() == 0) {
            return false;
        }
        if (!(IsInCall || getMode() == 3)) {
            z = false;
        }
        return z;
    }

    private boolean isAfMusicActiveRecently(int delay_ms) {
        if (AudioSystem.isStreamActive(3, delay_ms)) {
            return true;
        }
        return AudioSystem.isStreamActiveRemotely(3, delay_ms);
    }

    private int getActiveStreamType(int suggestedStreamType) {
        switch (this.mPlatformType) {
            case 1:
                if (isInCommunication()) {
                    if (getMode() == 3 && this.mAdjustVolumeAction) {
                        this.mAdjustVolumeAction = false;
                        String state = OppoMultimediaManager.getInstance(this.mContext).getParameters("streamtype_adjust_revise");
                        Log.d(TAG, "+streamtype_adjust_revise = " + state);
                        if (state != null && state.equalsIgnoreCase("true") && isAfMusicActiveRecently(StreamOverride.sDelayMs)) {
                            OppoMultimediaManager.getInstance(this.mContext).setEventInfo(103, null);
                            Log.d(TAG, "getActiveStreamType: Forcing STREAM_MUSIC stream active");
                            return 3;
                        }
                    }
                    return AudioSystem.getForceUse(0) == 3 ? 6 : 0;
                } else if (AudioSystem.isStreamActive(0, 0)) {
                    if (AudioSystem.getForceUse(0) == 3) {
                        if (DEBUG_VOL) {
                            Log.v(TAG, "getActiveStreamType: return BT-SCO stream for wechat voice msg");
                        }
                        return 6;
                    }
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: return voice stream when voice stream active");
                    }
                    return 0;
                } else if (suggestedStreamType == Integer.MIN_VALUE) {
                    if (isAfMusicActiveRecently(StreamOverride.sDelayMs)) {
                        if (DEBUG_VOL) {
                            Log.v(TAG, "getActiveStreamType: Forcing STREAM_MUSIC stream active");
                        }
                        return 3;
                    }
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_RING b/c default");
                    }
                    return 2;
                } else if (isAfMusicActiveRecently(0)) {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_MUSIC stream active");
                    }
                    return 3;
                }
                break;
            case 2:
                if (suggestedStreamType == Integer.MIN_VALUE) {
                    return 3;
                }
                break;
            default:
                if (isInCommunication()) {
                    if (AudioSystem.getForceUse(0) == 3) {
                        if (DEBUG_VOL) {
                            Log.v(TAG, "getActiveStreamType: Forcing STREAM_BLUETOOTH_SCO");
                        }
                        return 6;
                    }
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_VOICE_CALL");
                    }
                    return 0;
                } else if (AudioSystem.isStreamActive(5, StreamOverride.sDelayMs) || AudioSystem.isStreamActive(2, StreamOverride.sDelayMs)) {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION");
                    }
                    return 5;
                } else if (suggestedStreamType == Integer.MIN_VALUE) {
                    if (isAfMusicActiveRecently(StreamOverride.sDelayMs)) {
                        if (DEBUG_VOL) {
                            Log.v(TAG, "getActiveStreamType: forcing STREAM_MUSIC");
                        }
                        return 3;
                    }
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: using STREAM_NOTIFICATION as default");
                    }
                    return 5;
                }
                break;
        }
        if (DEBUG_VOL) {
            Log.v(TAG, "getActiveStreamType: Returning suggested type " + suggestedStreamType);
        }
        return suggestedStreamType;
    }

    private void broadcastRingerMode(String action, int ringerMode) {
        Intent broadcast = new Intent(action);
        broadcast.putExtra("android.media.EXTRA_RINGER_MODE", ringerMode);
        broadcast.addFlags(603979776);
        sendStickyBroadcastToAll(broadcast);
    }

    private void broadcastVibrateSetting(int vibrateType) {
        if (ActivityManagerNative.isSystemReady()) {
            Intent broadcast = new Intent("android.media.VIBRATE_SETTING_CHANGED");
            broadcast.putExtra("android.media.EXTRA_VIBRATE_TYPE", vibrateType);
            broadcast.putExtra("android.media.EXTRA_VIBRATE_SETTING", getVibrateSetting(vibrateType));
            sendBroadcastToAll(broadcast);
        }
    }

    private void queueMsgUnderWakeLock(Handler handler, int msg, int arg1, int arg2, Object obj, int delay) {
        long ident = Binder.clearCallingIdentity();
        this.mAudioEventWakeLock.acquire();
        Binder.restoreCallingIdentity(ident);
        sendMsg(handler, msg, 2, arg1, arg2, obj, delay);
    }

    private static void sendMsg(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delay) {
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == 1 && handler.hasMessages(msg)) {
            Log.d(TAG, "sendMsg: Msg " + msg + " existed!");
            return;
        }
        synchronized (mLastDeviceConnectMsgTime) {
            long time = SystemClock.uptimeMillis() + ((long) delay);
            handler.sendMessageAtTime(handler.obtainMessage(msg, arg1, arg2, obj), time);
            if (msg == 100 || msg == 101 || msg == 102) {
                mLastDeviceConnectMsgTime = Long.valueOf(time);
            }
        }
    }

    boolean checkAudioSettingsPermission(String method) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_SETTINGS") == 0) {
            return true;
        }
        Log.w(TAG, "Audio Settings Permission Denial: " + method + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return false;
    }

    private int getDeviceForStream(int stream) {
        int device = getDevicesForStream(stream);
        if (((device - 1) & device) != 0) {
            if ((device & 2) != 0) {
                device = 2;
            } else if ((DumpState.DUMP_DOMAIN_PREFERRED & device) != 0) {
                device = DumpState.DUMP_DOMAIN_PREFERRED;
            } else if ((DumpState.DUMP_FROZEN & device) != 0) {
                device = DumpState.DUMP_FROZEN;
            } else if ((DumpState.DUMP_COMPILER_STATS & device) != 0) {
                device = DumpState.DUMP_COMPILER_STATS;
            } else {
                device &= 896;
            }
        }
        Log.d(TAG, "Mits getDeviceForStream: stream" + stream + " device:" + device);
        return device;
    }

    private int getDevicesForStream(int stream) {
        return getDevicesForStream(stream, true);
    }

    private int getDevicesForStream(int stream, boolean checkOthers) {
        int observeDevicesForStream_syncVSS;
        ensureValidStreamType(stream);
        synchronized (VolumeStreamState.class) {
            observeDevicesForStream_syncVSS = this.mStreamStates[stream].observeDevicesForStream_syncVSS(checkOthers);
        }
        return observeDevicesForStream_syncVSS;
    }

    private void observeDevicesForStreams(int skipStream) {
        synchronized (VolumeStreamState.class) {
            for (int stream = 0; stream < this.mStreamStates.length; stream++) {
                if (stream != skipStream) {
                    this.mStreamStates[stream].observeDevicesForStream_syncVSS(false);
                }
            }
        }
    }

    public void setWiredDeviceConnectionState(int type, int state, String address, String name, String caller) {
        synchronized (this.mConnectedDevices) {
            if (DEBUG_DEVICES) {
                Slog.i(TAG, "setWiredDeviceConnectionState(" + state + " nm: " + name + " addr:" + address + ")");
            }
            int delay = checkSendBecomingNoisyIntent(type, state);
            queueMsgUnderWakeLock(this.mAudioHandler, 100, 0, 0, new WiredDeviceConnectionState(type, state, address, name, caller), delay);
        }
    }

    public int setBluetoothA2dpDeviceConnectionState(BluetoothDevice device, int state, int profile) {
        int i = 0;
        if (profile == 2 || profile == 11) {
            int delay;
            synchronized (this.mConnectedDevices) {
                if (profile == 2) {
                    if (state == 2) {
                        i = 1;
                    }
                    delay = checkSendBecomingNoisyIntent(128, i);
                } else {
                    delay = 0;
                }
                queueMsgUnderWakeLock(this.mAudioHandler, profile == 2 ? 102 : 101, state, 0, device, delay);
            }
            return delay;
        }
        throw new IllegalArgumentException("invalid profile " + profile);
    }

    private void makeA2dpDeviceAvailable(String address, String name) {
        sendMsg(this.mAudioHandler, 0, 2, 128, 0, this.mStreamStates[3], 0);
        setBluetoothA2dpOnInt(true);
        AudioSystem.setDeviceConnectionState(128, 1, address, name);
        AudioSystem.setParameters("A2dpSuspended=false");
        this.mConnectedDevices.put(makeDeviceListKey(128, address), new DeviceListSpec(128, name, address));
    }

    private void onSendBecomingNoisyIntent() {
        sendBroadcastToAll(new Intent("android.media.AUDIO_BECOMING_NOISY"));
    }

    private void makeA2dpDeviceUnavailableNow(String address) {
        synchronized (this.mA2dpAvrcpLock) {
            this.mAvrcpAbsVolSupported = false;
        }
        AudioSystem.setParameters("LosslessBT_AbsoluteVolume=0");
        AudioSystem.setDeviceConnectionState(128, 0, address, IElsaManager.EMPTY_PACKAGE);
        this.mConnectedDevices.remove(makeDeviceListKey(128, address));
        synchronized (this.mCurAudioRoutes) {
            if (this.mCurAudioRoutes.bluetoothName != null) {
                this.mCurAudioRoutes.bluetoothName = null;
                sendMsg(this.mAudioHandler, 12, 1, 0, 0, null, 0);
            }
        }
    }

    private void makeA2dpDeviceUnavailableLater(String address, int delayMs) {
        AudioSystem.setParameters("A2dpSuspended=true");
        this.mConnectedDevices.remove(makeDeviceListKey(128, address));
        this.mAudioHandler.sendMessageDelayed(this.mAudioHandler.obtainMessage(6, address), (long) delayMs);
    }

    private void makeA2dpSrcAvailable(String address) {
        AudioSystem.setDeviceConnectionState(-2147352576, 1, address, IElsaManager.EMPTY_PACKAGE);
        this.mConnectedDevices.put(makeDeviceListKey(-2147352576, address), new DeviceListSpec(-2147352576, IElsaManager.EMPTY_PACKAGE, address));
    }

    private void makeA2dpSrcUnavailable(String address) {
        AudioSystem.setDeviceConnectionState(-2147352576, 0, address, IElsaManager.EMPTY_PACKAGE);
        this.mConnectedDevices.remove(makeDeviceListKey(-2147352576, address));
    }

    private void cancelA2dpDeviceTimeout() {
        this.mAudioHandler.removeMessages(6);
    }

    private boolean hasScheduledA2dpDockTimeout() {
        return this.mAudioHandler.hasMessages(6);
    }

    private void onSetA2dpSinkConnectionState(BluetoothDevice btDevice, int state) {
        if (DEBUG_VOL) {
            Log.d(TAG, "onSetA2dpSinkConnectionState btDevice=" + btDevice + "state=" + state);
        }
        if (btDevice != null) {
            String address = btDevice.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                address = IElsaManager.EMPTY_PACKAGE;
            }
            synchronized (this.mConnectedDevices) {
                boolean isConnected = ((DeviceListSpec) this.mConnectedDevices.get(makeDeviceListKey(128, btDevice.getAddress()))) != null;
                if (isConnected && state != 2) {
                    if (!btDevice.isBluetoothDock()) {
                        makeA2dpDeviceUnavailableNow(address);
                    } else if (state == 0) {
                        makeA2dpDeviceUnavailableLater(address, BTA2DP_DOCK_TIMEOUT_MILLIS);
                    }
                    synchronized (this.mCurAudioRoutes) {
                        if (this.mCurAudioRoutes.bluetoothName != null) {
                            this.mCurAudioRoutes.bluetoothName = null;
                            sendMsg(this.mAudioHandler, 12, 1, 0, 0, null, 0);
                        }
                    }
                } else if (!isConnected && state == 2) {
                    if (btDevice.isBluetoothDock()) {
                        cancelA2dpDeviceTimeout();
                        this.mDockAddress = address;
                    } else if (hasScheduledA2dpDockTimeout()) {
                        cancelA2dpDeviceTimeout();
                        makeA2dpDeviceUnavailableNow(this.mDockAddress);
                    }
                    makeA2dpDeviceAvailable(address, btDevice.getName());
                    synchronized (this.mCurAudioRoutes) {
                        String name = btDevice.getAliasName();
                        if (!TextUtils.equals(this.mCurAudioRoutes.bluetoothName, name)) {
                            this.mCurAudioRoutes.bluetoothName = name;
                            sendMsg(this.mAudioHandler, 12, 1, 0, 0, null, 0);
                        }
                    }
                }
            }
        }
    }

    private void onSetA2dpSourceConnectionState(BluetoothDevice btDevice, int state) {
        if (DEBUG_VOL) {
            Log.d(TAG, "onSetA2dpSourceConnectionState btDevice=" + btDevice + " state=" + state);
        }
        if (btDevice != null) {
            String address = btDevice.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                address = IElsaManager.EMPTY_PACKAGE;
            }
            synchronized (this.mConnectedDevices) {
                boolean isConnected = ((DeviceListSpec) this.mConnectedDevices.get(makeDeviceListKey(-2147352576, address))) != null;
                if (isConnected && state != 2) {
                    makeA2dpSrcUnavailable(address);
                } else if (!isConnected && state == 2) {
                    makeA2dpSrcAvailable(address);
                }
            }
        }
    }

    public void avrcpSupportsAbsoluteVolume(String address, boolean support) {
        synchronized (this.mA2dpAvrcpLock) {
            this.mAvrcpAbsVolSupported = support;
            if (this.mAvrcpAbsVolSupported) {
                AudioSystem.setParameters("LosslessBT_AbsoluteVolume=1");
            } else {
                AudioSystem.setParameters("LosslessBT_AbsoluteVolume=0");
            }
            sendMsg(this.mAudioHandler, 0, 2, 128, 0, this.mStreamStates[3], 0);
            sendMsg(this.mAudioHandler, 0, 2, 128, 0, this.mStreamStates[2], 0);
        }
    }

    /* JADX WARNING: Missing block: B:61:0x024d, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean handleDeviceConnection(boolean connect, int device, String address, String deviceName) {
        if (DEBUG_DEVICES) {
            Slog.i(TAG, "handleDeviceConnection(" + connect + " dev:" + Integer.toHexString(device) + " address:" + address + " name:" + deviceName + ")");
        }
        synchronized (this.mConnectedDevices) {
            String deviceKey = makeDeviceListKey(device, address);
            if (DEBUG_DEVICES) {
                Slog.i(TAG, "deviceKey:" + deviceKey);
            }
            DeviceListSpec deviceSpec = (DeviceListSpec) this.mConnectedDevices.get(deviceKey);
            boolean isConnected = deviceSpec != null;
            if (DEBUG_DEVICES) {
                Slog.i(TAG, "deviceSpec:" + deviceSpec + " is(already)Connected:" + isConnected);
            }
            if (connect && !isConnected) {
                boolean bFadeInFlag = false;
                if (getActiveStreamType(-1) == 3 && (device == 4 || device == 8)) {
                    Log.d(TAG, "mute music first if it's active when headset connect,mFadeInFinish :" + this.mFadeInFinish);
                    AudioSystem.setStreamVolumeIndex(3, 0, device);
                    if (!(this.mFadeInDevice == device || this.mFadeInFinish)) {
                        Log.d(TAG, "mFadeInDevice :" + this.mFadeInDevice + " device:" + device + " mFadeInMusicVolume:" + this.mFadeInMusicVolume);
                        if (getStreamVolumeByDevice(3, this.mFadeInDevice) != this.mFadeInMusicVolume) {
                            AudioSystem.setStreamVolumeIndex(3, this.mFadeInMusicVolume, this.mFadeInDevice);
                        }
                    }
                    this.mFadeInDevice = device;
                    this.mFadeInMusicVolume = getStreamVolumeByDevice(3, device);
                    bFadeInFlag = true;
                    this.mFadeInFinish = false;
                }
                int res = AudioSystem.setDeviceConnectionState(device, 1, address, deviceName);
                if (res != 0) {
                    Slog.e(TAG, "not connecting device 0x" + Integer.toHexString(device) + " due to command error " + res);
                    if (bFadeInFlag) {
                        Log.d(TAG, "connect device " + device + " fail, skip fade in");
                        this.mFadeInFinish = true;
                        AudioSystem.setStreamVolumeIndex(3, this.mFadeInMusicVolume, this.mFadeInDevice);
                    }
                    return false;
                }
                this.mConnectedDevices.put(deviceKey, new DeviceListSpec(device, deviceName, address));
                if (bFadeInFlag) {
                    Log.d(TAG, "begin to fade in music volume to " + this.mFadeInMusicVolume);
                    this.mFadeInCurrentVolume = 0;
                    sendMsg(this.mAudioHandler, 60, 2, 0, 0, null, 50);
                }
                return true;
            } else if (connect || !isConnected) {
            } else {
                AudioSystem.setDeviceConnectionState(device, 0, address, deviceName);
                this.mConnectedDevices.remove(deviceKey);
                return true;
            }
        }
    }

    private int checkSendBecomingNoisyIntent(int device, int state) {
        int delay = 0;
        if (state == 0 && (this.mBecomingNoisyIntentDevices & device) != 0) {
            int devices = 0;
            for (int i = 0; i < this.mConnectedDevices.size(); i++) {
                int dev = ((DeviceListSpec) this.mConnectedDevices.valueAt(i)).mDeviceType;
                if ((Integer.MIN_VALUE & dev) == 0 && (this.mBecomingNoisyIntentDevices & dev) != 0) {
                    devices |= dev;
                }
            }
            Log.d(TAG, "checkSendBecomingNoisyIntent device :" + device + " state:" + state + " devices :" + devices + " mConnectedDevices :" + this.mConnectedDevices);
            if (devices == device) {
                sendMsg(this.mAudioHandler, 15, 0, 0, 0, null, 0);
                delay = 1000;
            }
        }
        if (this.mAudioHandler.hasMessages(101) || this.mAudioHandler.hasMessages(102) || this.mAudioHandler.hasMessages(100)) {
            synchronized (mLastDeviceConnectMsgTime) {
                long time = SystemClock.uptimeMillis();
                if (mLastDeviceConnectMsgTime.longValue() > time) {
                    delay = ((int) (mLastDeviceConnectMsgTime.longValue() - time)) + 30;
                }
            }
        }
        return delay;
    }

    private void sendDeviceConnectionIntent(int device, int state, String address, String deviceName, boolean shouldBroadcast) {
        if (DEBUG_DEVICES) {
            Slog.i(TAG, "sendDeviceConnectionIntent(dev:0x" + Integer.toHexString(device) + " state:0x" + Integer.toHexString(state) + " address:" + address + " name:" + deviceName + ");");
        }
        Intent intent = new Intent();
        intent.putExtra("state", state);
        intent.putExtra(CONNECT_INTENT_KEY_ADDRESS, address);
        intent.putExtra(CONNECT_INTENT_KEY_PORT_NAME, deviceName);
        intent.addFlags(1073741824);
        int connType = 0;
        if (device == 4) {
            connType = 1;
            intent.setAction("android.intent.action.HEADSET_PLUG");
            intent.putExtra("microphone", 1);
            intent.addFlags(4194304);
        } else if (device == 8 || device == 131072) {
            connType = 2;
            intent.setAction("android.intent.action.HEADSET_PLUG");
            intent.putExtra("microphone", 0);
        } else if (device == 1024 || device == 262144) {
            connType = 8;
            configureHdmiPlugIntent(intent, state);
        } else if (device == 16384) {
            connType = 16;
        }
        synchronized (this.mCurAudioRoutes) {
            if (connType != 0) {
                int newConn = this.mCurAudioRoutes.mainType;
                if (state != 0) {
                    newConn |= connType;
                } else {
                    newConn &= ~connType;
                }
                if (newConn != this.mCurAudioRoutes.mainType) {
                    this.mCurAudioRoutes.mainType = newConn;
                    sendMsg(this.mAudioHandler, 12, 1, 0, 0, null, 0);
                }
            }
        }
        if (shouldBroadcast) {
            long ident = Binder.clearCallingIdentity();
            try {
                ActivityManagerNative.broadcastStickyIntent(intent, null, -1);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            Log.d(TAG, "sendDeviceConnectionIntent() no need to broadcast");
        }
    }

    /* JADX WARNING: Missing block: B:72:0x015c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onSetWiredDeviceConnectionState(int device, int state, String address, String deviceName, String caller) {
        if (DEBUG_DEVICES) {
            Slog.i(TAG, "onSetWiredDeviceConnectionState(dev:" + Integer.toHexString(device) + " state:" + Integer.toHexString(state) + " address:" + address + " deviceName:" + deviceName + " caller: " + caller + ");");
        }
        synchronized (this.mConnectedDevices) {
            Log.d(TAG, "onSetWiredDeviceConnectionState:device:" + device + ",state:" + state);
            boolean shouldNotify = true;
            if (state == 0 && (device == 4 || device == 8 || device == DumpState.DUMP_INTENT_FILTER_VERIFIERS)) {
                boolean isHeadphoneOff = device == 8;
                boolean isHeadsetOn = this.mConnectedDevices.containsKey("0x" + 4 + ":");
                if (isHeadphoneOff && isHeadsetOn) {
                    shouldNotify = false;
                    Log.d(TAG, "onSetWiredDeviceConnectionState() no need to setForceUse");
                }
                if (shouldNotify) {
                    setBluetoothA2dpOnInt(true);
                }
            }
            boolean isUsb = (device & -24577) != 0 ? (Integer.MIN_VALUE & device) != 0 ? (2147477503 & device) == 0 : false : true;
            if (handleDeviceConnection(state == 1, device, address, deviceName)) {
                if (state != 0) {
                    if (device == 4 || device == 8 || device == DumpState.DUMP_INTENT_FILTER_VERIFIERS) {
                        setBluetoothA2dpOnInt(false);
                    }
                    if ((device & 12) != 0) {
                        sendMsg(this.mAudioHandler, 14, 0, 0, 0, caller, 60000);
                    }
                    if (isPlatformTelevision() && (device & 1024) != 0) {
                        this.mFixedVolumeDevices |= 1024;
                        checkAllFixedVolumeDevices();
                        if (this.mHdmiManager != null) {
                            synchronized (this.mHdmiManager) {
                                if (this.mHdmiPlaybackClient != null) {
                                    this.mHdmiCecSink = false;
                                    this.mHdmiPlaybackClient.queryDisplayStatus(this.mHdmiDisplayStatusCallback);
                                }
                            }
                        }
                    }
                } else if (!(!isPlatformTelevision() || (device & 1024) == 0 || this.mHdmiManager == null)) {
                    synchronized (this.mHdmiManager) {
                        this.mHdmiCecSink = false;
                    }
                }
                if (!(isUsb || device == -2147483632)) {
                    sendDeviceConnectionIntent(device, state, address, deviceName, shouldNotify);
                }
            }
        }
    }

    private void configureHdmiPlugIntent(Intent intent, int state) {
        intent.setAction("android.media.action.HDMI_AUDIO_PLUG");
        intent.putExtra("android.media.extra.AUDIO_PLUG_STATE", state);
        if (state == 1) {
            ArrayList<AudioPort> ports = new ArrayList();
            if (AudioSystem.listAudioPorts(ports, new int[1]) == 0) {
                for (AudioPort port : ports) {
                    if (port instanceof AudioDevicePort) {
                        AudioDevicePort devicePort = (AudioDevicePort) port;
                        if (devicePort.type() == 1024 || devicePort.type() == 262144) {
                            int[] formats = AudioFormat.filterPublicFormats(devicePort.formats());
                            if (formats.length > 0) {
                                ArrayList<Integer> encodingList = new ArrayList(1);
                                for (int format : formats) {
                                    if (format != 0) {
                                        encodingList.add(Integer.valueOf(format));
                                    }
                                }
                                int[] encodingArray = new int[encodingList.size()];
                                for (int i = 0; i < encodingArray.length; i++) {
                                    encodingArray[i] = ((Integer) encodingList.get(i)).intValue();
                                }
                                intent.putExtra("android.media.extra.ENCODINGS", encodingArray);
                            }
                            int maxChannels = 0;
                            for (int mask : devicePort.channelMasks()) {
                                int channelCount = AudioFormat.channelCountFromOutChannelMask(mask);
                                if (channelCount > maxChannels) {
                                    maxChannels = channelCount;
                                }
                            }
                            intent.putExtra("android.media.extra.MAX_CHANNEL_COUNT", maxChannels);
                        }
                    }
                }
            }
        }
    }

    private void killBackgroundUserProcessesWithRecordAudioPermission(UserInfo oldUser) {
        PackageManager pm = this.mContext.getPackageManager();
        ComponentName homeActivityName = null;
        if (!oldUser.isManagedProfile()) {
            homeActivityName = ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).getHomeActivityForUser(oldUser.id);
        }
        String[] permissions = new String[1];
        permissions[0] = OppoPermissionConstants.PERMISSION_RECORD_AUDIO;
        try {
            List<PackageInfo> packages = AppGlobals.getPackageManager().getPackagesHoldingPermissions(permissions, 0, oldUser.id).getList();
            for (int j = packages.size() - 1; j >= 0; j--) {
                PackageInfo pkg = (PackageInfo) packages.get(j);
                if (!(UserHandle.getAppId(pkg.applicationInfo.uid) < 10000 || pm.checkPermission("android.permission.INTERACT_ACROSS_USERS", pkg.packageName) == 0 || (homeActivityName != null && pkg.packageName.equals(homeActivityName.getPackageName()) && pkg.applicationInfo.isSystemApp()))) {
                    try {
                        int uid = pkg.applicationInfo.uid;
                        ActivityManagerNative.getDefault().killUid(UserHandle.getAppId(uid), UserHandle.getUserId(uid), "killBackgroundUserProcessesWithAudioRecordPermission");
                    } catch (RemoteException e) {
                        Log.w(TAG, "Error calling killUid", e);
                    }
                }
            }
        } catch (RemoteException e2) {
            throw new AndroidRuntimeException(e2);
        }
    }

    public int requestAudioFocus(AudioAttributes aa, int durationHint, IBinder cb, IAudioFocusDispatcher fd, String clientId, String callingPackageName, int flags, IAudioPolicyCallback pcb) {
        if ((flags & 4) == 4) {
            if (!"AudioFocus_For_Phone_Ring_And_Calls".equals(clientId)) {
                synchronized (this.mAudioPolicies) {
                    if (!this.mAudioPolicies.containsKey(pcb.asBinder())) {
                        Log.e(TAG, "Invalid unregistered AudioPolicy to (un)lock audio focus");
                        return 0;
                    }
                }
            } else if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
                Log.e(TAG, "Invalid permission to (un)lock audio focus", new Exception());
                return 0;
            }
        }
        return this.mMediaFocusControl.requestAudioFocus(aa, durationHint, cb, fd, clientId, callingPackageName, flags);
    }

    public int abandonAudioFocus(IAudioFocusDispatcher fd, String clientId, AudioAttributes aa) {
        return this.mMediaFocusControl.abandonAudioFocus(fd, clientId, aa);
    }

    public void unregisterAudioFocusClient(String clientId) {
        this.mMediaFocusControl.unregisterAudioFocusClient(clientId);
    }

    public int getCurrentAudioFocus() {
        return this.mMediaFocusControl.getCurrentAudioFocus();
    }

    private boolean readCameraSoundForced() {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.media.camerasound.forced")) {
            Log.d(TAG, "set CameraSoundForced to true for cmcc model");
            return true;
        }
        Log.d(TAG, "set CameraSoundForced to false by default");
        return false;
    }

    public boolean isAppInFocus(String name) {
        return this.mMediaFocusControl.isAppInFocus(name);
    }

    private void handleConfigurationChanged(Context context) {
        try {
            Configuration config = context.getResources().getConfiguration();
            if (this.mMonitorOrientation) {
                int newOrientation = config.orientation;
                if (newOrientation != this.mDeviceOrientation) {
                    this.mDeviceOrientation = newOrientation;
                    setOrientationForAudioSystem();
                }
            }
            sendMsg(this.mAudioHandler, 16, 0, 0, 0, TAG, 0);
            boolean cameraSoundForced = readCameraSoundForced();
            synchronized (this.mSettingsLock) {
                boolean cameraSoundForcedChanged = false;
                synchronized (this.mCameraSoundForced) {
                    if (cameraSoundForced != this.mCameraSoundForced.booleanValue()) {
                        this.mCameraSoundForced = Boolean.valueOf(cameraSoundForced);
                        cameraSoundForcedChanged = true;
                    }
                }
                if (cameraSoundForcedChanged) {
                    if (!isPlatformTelevision()) {
                        VolumeStreamState s = this.mStreamStates[7];
                        if (cameraSoundForced) {
                            s.setAllIndexesToMax();
                            this.mRingerModeAffectedStreams &= -129;
                        } else {
                            s.setAllIndexes(this.mStreamStates[1], TAG);
                            this.mRingerModeAffectedStreams |= 128;
                        }
                        setRingerModeInt(getRingerModeInternal(), false);
                    }
                    sendMsg(this.mAudioHandler, 8, 2, 4, cameraSoundForced ? 11 : 0, null, 0);
                    sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[7], 0);
                }
            }
            this.mVolumeController.setLayoutDirection(config.getLayoutDirection());
        } catch (Exception e) {
            Log.e(TAG, "Error handling configuration change: ", e);
        }
    }

    private void setOrientationForAudioSystem() {
        switch (this.mDeviceOrientation) {
            case 0:
                AudioSystem.setParameters("orientation=undefined");
                return;
            case 1:
                AudioSystem.setParameters("orientation=portrait");
                return;
            case 2:
                AudioSystem.setParameters("orientation=landscape");
                return;
            case 3:
                AudioSystem.setParameters("orientation=square");
                return;
            default:
                Log.e(TAG, "Unknown orientation");
                return;
        }
    }

    public void setBluetoothA2dpOnInt(boolean on) {
        synchronized (this.mBluetoothA2dpEnabledLock) {
            this.mBluetoothA2dpEnabled = on;
            this.mAudioHandler.removeMessages(13);
            setForceUseInt_SyncDevices(1, this.mBluetoothA2dpEnabled ? 0 : 10);
        }
    }

    private void setForceUseInt_SyncDevices(int usage, int config) {
        AudioSystem.setForceUse(usage, config);
    }

    public void setRingtonePlayer(IRingtonePlayer player) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.REMOTE_AUDIO_PLAYBACK", null);
        this.mRingtonePlayer = player;
    }

    public IRingtonePlayer getRingtonePlayer() {
        return this.mRingtonePlayer;
    }

    public AudioRoutesInfo startWatchingRoutes(IAudioRoutesObserver observer) {
        AudioRoutesInfo routes;
        synchronized (this.mCurAudioRoutes) {
            routes = new AudioRoutesInfo(this.mCurAudioRoutes);
            this.mRoutesObservers.register(observer);
        }
        return routes;
    }

    private void setSafeMediaVolumeEnabled(boolean on, String caller) {
        synchronized (this.mSafeMediaVolumeState) {
            if (!(this.mSafeMediaVolumeState.intValue() == 0 || this.mSafeMediaVolumeState.intValue() == 1)) {
                if (on && this.mSafeMediaVolumeState.intValue() == 2) {
                    this.mSafeMediaVolumeState = Integer.valueOf(3);
                    enforceSafeMediaVolume(caller);
                } else if (!on) {
                    if (this.mSafeMediaVolumeState.intValue() == 3) {
                        this.mSafeMediaVolumeState = Integer.valueOf(2);
                        this.mMusicActiveMs = 1;
                        saveMusicActiveMs();
                        sendMsg(this.mAudioHandler, 14, 0, 0, 0, caller, 60000);
                    }
                }
            }
        }
    }

    private void enforceSafeMediaVolume(String caller) {
        VolumeStreamState streamState = this.mStreamStates[3];
        int devices = 12;
        int i = 0;
        while (devices != 0) {
            int i2 = i + 1;
            int device = 1 << i;
            if ((device & devices) == 0) {
                i = i2;
            } else {
                if (streamState.getIndex(device) > this.mSafeMediaVolumeIndex) {
                    streamState.setIndex(this.mSafeMediaVolumeIndex, device, caller);
                    sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
                }
                devices &= ~device;
                i = i2;
            }
        }
    }

    private boolean checkSafeMediaVolume(int streamType, int index, int device) {
        synchronized (this.mSafeMediaVolumeState) {
            if (this.mSafeMediaVolumeState.intValue() != 3 || this.mStreamVolumeAlias[streamType] != 3 || (device & 12) == 0 || index <= this.mSafeMediaVolumeIndex) {
                return true;
            }
            return false;
        }
    }

    public void disableSafeMediaVolume(String callingPackage) {
        enforceVolumeController("disable the safe media volume");
        synchronized (this.mSafeMediaVolumeState) {
            setSafeMediaVolumeEnabled(false, callingPackage);
            if (this.mPendingVolumeCommand != null) {
                onSetStreamVolume(this.mPendingVolumeCommand.mStreamType, this.mPendingVolumeCommand.mIndex, this.mPendingVolumeCommand.mFlags, this.mPendingVolumeCommand.mDevice, callingPackage);
                this.mPendingVolumeCommand = null;
            }
        }
    }

    public int setHdmiSystemAudioSupported(boolean on) {
        int i = 0;
        int device = 0;
        if (this.mHdmiManager != null) {
            synchronized (this.mHdmiManager) {
                if (this.mHdmiTvClient == null) {
                    Log.w(TAG, "Only Hdmi-Cec enabled TV device supports system audio mode.");
                    return 0;
                }
                synchronized (this.mHdmiTvClient) {
                    if (this.mHdmiSystemAudioSupported != on) {
                        this.mHdmiSystemAudioSupported = on;
                        if (on) {
                            i = 12;
                        }
                        AudioSystem.setForceUse(5, i);
                    }
                    device = getDevicesForStream(3);
                }
            }
        }
        return device;
    }

    public boolean isHdmiSystemAudioSupported() {
        return this.mHdmiSystemAudioSupported;
    }

    public boolean isCameraSoundForced() {
        boolean booleanValue;
        synchronized (this.mCameraSoundForced) {
            booleanValue = this.mCameraSoundForced.booleanValue();
        }
        return booleanValue;
    }

    private void dumpRingerMode(PrintWriter pw) {
        pw.println("\nRinger mode: ");
        pw.println("- mode (internal) = " + RINGER_MODE_NAMES[this.mRingerMode]);
        pw.println("- mode (external) = " + RINGER_MODE_NAMES[this.mRingerModeExternal]);
        dumpRingerModeStreams(pw, "affected", this.mRingerModeAffectedStreams);
        dumpRingerModeStreams(pw, "muted", this.mRingerModeMutedStreams);
        pw.print("- delegate = ");
        pw.println(this.mRingerModeDelegate);
    }

    private void dumpRingerModeStreams(PrintWriter pw, String type, int streams) {
        pw.print("- ringer mode ");
        pw.print(type);
        pw.print(" streams = 0x");
        pw.print(Integer.toHexString(streams));
        if (streams != 0) {
            pw.print(" (");
            boolean first = true;
            for (int i = 0; i < AudioSystem.STREAM_NAMES.length; i++) {
                int stream = 1 << i;
                if ((streams & stream) != 0) {
                    if (!first) {
                        pw.print(',');
                    }
                    pw.print(AudioSystem.STREAM_NAMES[i]);
                    streams &= ~stream;
                    first = false;
                }
            }
            if (streams != 0) {
                if (!first) {
                    pw.print(',');
                }
                pw.print(streams);
            }
            pw.print(')');
        }
        pw.println();
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        this.mMediaFocusControl.dump(pw);
        dumpStreamStates(pw);
        dumpRingerMode(pw);
        pw.println("\nAudio routes:");
        pw.print("  mMainType=0x");
        pw.println(Integer.toHexString(this.mCurAudioRoutes.mainType));
        pw.print("  mBluetoothName=");
        pw.println(this.mCurAudioRoutes.bluetoothName);
        pw.println("\nOther state:");
        pw.print("  mVolumeController=");
        pw.println(this.mVolumeController);
        pw.print("  mSafeMediaVolumeState=");
        pw.println(safeMediaVolumeStateToString(this.mSafeMediaVolumeState));
        pw.print("  mSafeMediaVolumeIndex=");
        pw.println(this.mSafeMediaVolumeIndex);
        pw.print("  mPendingVolumeCommand=");
        pw.println(this.mPendingVolumeCommand);
        pw.print("  mMusicActiveMs=");
        pw.println(this.mMusicActiveMs);
        pw.print("  mMcc=");
        pw.println(this.mMcc);
        pw.print("  mCameraSoundForced=");
        pw.println(this.mCameraSoundForced);
        pw.print("  mHasVibrator=");
        pw.println(this.mHasVibrator);
        pw.print("  mControllerService=");
        pw.println(this.mControllerService);
        pw.print("  mVolumePolicy=");
        pw.println(this.mVolumePolicy);
        dumpAudioPolicies(pw);
    }

    private static String safeMediaVolumeStateToString(Integer state) {
        switch (state.intValue()) {
            case 0:
                return "SAFE_MEDIA_VOLUME_NOT_CONFIGURED";
            case 1:
                return "SAFE_MEDIA_VOLUME_DISABLED";
            case 2:
                return "SAFE_MEDIA_VOLUME_INACTIVE";
            case 3:
                return "SAFE_MEDIA_VOLUME_ACTIVE";
            default:
                return null;
        }
    }

    private static void readAndSetLowRamDevice() {
        int status = AudioSystem.setLowRamDevice(ActivityManager.isLowRamDeviceStatic());
        if (status != 0) {
            Log.w(TAG, "AudioFlinger informed of device's low RAM attribute; status " + status);
        }
    }

    private void enforceVolumeController(String action) {
        if (this.mControllerService.mUid == 0 || Binder.getCallingUid() != this.mControllerService.mUid) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "Only SystemUI can " + action);
        }
    }

    public void setVolumeController(final IVolumeController controller) {
        enforceVolumeController("set the volume controller");
        if (!this.mVolumeController.isSameBinder(controller)) {
            this.mVolumeController.postDismiss();
            if (controller != null) {
                try {
                    controller.asBinder().linkToDeath(new DeathRecipient() {
                        public void binderDied() {
                            if (AudioService.this.mVolumeController.isSameBinder(controller)) {
                                Log.w(AudioService.TAG, "Current remote volume controller died, unregistering");
                                AudioService.this.setVolumeController(null);
                            }
                        }
                    }, 0);
                } catch (RemoteException e) {
                }
            }
            this.mVolumeController.setController(controller);
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume controller: " + this.mVolumeController);
            }
        }
    }

    public void notifyVolumeControllerVisible(IVolumeController controller, boolean visible) {
        enforceVolumeController("notify about volume controller visibility");
        if (this.mVolumeController.isSameBinder(controller)) {
            this.mVolumeController.setVisible(visible);
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume controller visible: " + visible);
            }
        }
    }

    public void setVolumePolicy(VolumePolicy policy) {
        enforceVolumeController("set volume policy");
        if (policy != null && !policy.equals(this.mVolumePolicy)) {
            this.mVolumePolicy = policy;
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume policy changed: " + this.mVolumePolicy);
            }
        }
    }

    public String registerAudioPolicy(AudioPolicyConfig policyConfig, IAudioPolicyCallback pcb, boolean hasFocusListener) {
        boolean hasPermissionForPolicy = false;
        AudioSystem.setDynamicPolicyCallback(this.mDynPolicyCallback);
        if (DEBUG_AP) {
            Log.d(TAG, "registerAudioPolicy for " + pcb.asBinder() + " with config:" + policyConfig);
        }
        if (this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0) {
            hasPermissionForPolicy = true;
        }
        if (hasPermissionForPolicy) {
            synchronized (this.mAudioPolicies) {
                try {
                    if (this.mAudioPolicies.containsKey(pcb.asBinder())) {
                        Slog.e(TAG, "Cannot re-register policy");
                        return null;
                    }
                    AudioPolicyProxy app = new AudioPolicyProxy(policyConfig, pcb, hasFocusListener);
                    pcb.asBinder().linkToDeath(app, 0);
                    String regId = app.getRegistrationId();
                    this.mAudioPolicies.put(pcb.asBinder(), app);
                    return regId;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Audio policy registration failed, could not link to " + pcb + " binder death", e);
                    return null;
                }
            }
        }
        Slog.w(TAG, "Can't register audio policy for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", need MODIFY_AUDIO_ROUTING");
        return null;
    }

    public void unregisterAudioPolicyAsync(IAudioPolicyCallback pcb) {
        if (DEBUG_AP) {
            Log.d(TAG, "unregisterAudioPolicyAsync for " + pcb.asBinder());
        }
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = (AudioPolicyProxy) this.mAudioPolicies.remove(pcb.asBinder());
            if (app == null) {
                Slog.w(TAG, "Trying to unregister unknown audio policy for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid());
                return;
            }
            pcb.asBinder().unlinkToDeath(app, 0);
            app.release();
        }
    }

    public int setFocusPropertiesForPolicy(int duckingBehavior, IAudioPolicyCallback pcb) {
        boolean hasPermissionForPolicy;
        boolean z = true;
        if (DEBUG_AP) {
            Log.d(TAG, "setFocusPropertiesForPolicy() duck behavior=" + duckingBehavior + " policy " + pcb.asBinder());
        }
        if (this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0) {
            hasPermissionForPolicy = true;
        } else {
            hasPermissionForPolicy = false;
        }
        if (hasPermissionForPolicy) {
            synchronized (this.mAudioPolicies) {
                if (this.mAudioPolicies.containsKey(pcb.asBinder())) {
                    AudioPolicyProxy app = (AudioPolicyProxy) this.mAudioPolicies.get(pcb.asBinder());
                    if (duckingBehavior == 1) {
                        for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                            if (policy.mFocusDuckBehavior == 1) {
                                Slog.e(TAG, "Cannot change audio policy ducking behavior, already handled");
                                return -1;
                            }
                        }
                    }
                    app.mFocusDuckBehavior = duckingBehavior;
                    MediaFocusControl mediaFocusControl = this.mMediaFocusControl;
                    if (duckingBehavior != 1) {
                        z = false;
                    }
                    mediaFocusControl.setDuckingInExtPolicyAvailable(z);
                    return 0;
                }
                Slog.e(TAG, "Cannot change audio policy focus properties, unregistered policy");
                return -1;
            }
        }
        Slog.w(TAG, "Cannot change audio policy ducking handling for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", need MODIFY_AUDIO_ROUTING");
        return -1;
    }

    private void dumpAudioPolicies(PrintWriter pw) {
        pw.println("\nAudio policies:");
        synchronized (this.mAudioPolicies) {
            for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                pw.println(policy.toLogFriendlyString());
            }
        }
    }

    private void onDynPolicyMixStateUpdate(String regId, int state) {
        if (DEBUG_AP) {
            Log.d(TAG, "onDynamicPolicyMixStateUpdate(" + regId + ", " + state + ")");
        }
        synchronized (this.mAudioPolicies) {
            for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                for (AudioMix mix : policy.getMixes()) {
                    if (mix.getRegistration().equals(regId)) {
                        try {
                            policy.mPolicyCallback.notifyMixStateUpdate(regId, state);
                        } catch (RemoteException e) {
                            Log.e(TAG, "Can't call notifyMixStateUpdate() on IAudioPolicyCallback " + policy.mPolicyCallback.asBinder(), e);
                        }
                    }
                }
            }
            return;
        }
    }

    public void registerRecordingCallback(IRecordingConfigDispatcher rcdb) {
        this.mRecordMonitor.registerRecordingCallback(rcdb);
    }

    public void unregisterRecordingCallback(IRecordingConfigDispatcher rcdb) {
        this.mRecordMonitor.unregisterRecordingCallback(rcdb);
    }

    public List<AudioRecordingConfiguration> getActiveRecordingConfigurations() {
        return this.mRecordMonitor.getActiveRecordingConfigurations();
    }

    public boolean setAudioPathToFMTx(IBinder cb) {
        AudioPathToFMTxDeathHandler hdl = new AudioPathToFMTxDeathHandler(cb);
        String state = this.mAudioManager.getParameters(GETTXPATHENABLESTATE);
        if (state.equalsIgnoreCase(TXPATHENABLESTATE)) {
            Log.d(TAG, "audio path already set to FM Tx, audio state =\" " + state + "\"");
        } else {
            this.mAudioManager.setParameters(SETTXPATHENABLE);
            state = this.mAudioManager.getParameters(GETTXPATHENABLESTATE);
            if (state.equalsIgnoreCase(TXPATHENABLESTATE)) {
                Log.d(TAG, "set audio path to FM Tx succeed, audio state =\" " + state + "\"");
            } else {
                Log.e(TAG, "set audio path to FM Tx failed, audio state = \"" + state + "\"");
                return false;
            }
        }
        try {
            cb.linkToDeath(hdl, 0);
            return true;
        } catch (RemoteException e) {
            Log.w(TAG, "setAudioPathToFMTx could not link to " + cb + " binder death.");
            return false;
        }
    }

    public boolean setAudioPathOutofFMTx() {
        if (!this.mAudioManager.getParameters(GETTXPATHENABLESTATE).equalsIgnoreCase(TXPATHENABLESTATE)) {
            return false;
        }
        this.mAudioManager.setParameters(SETTXPATHDISENABLE);
        String state = this.mAudioManager.getParameters(GETTXPATHENABLESTATE);
        if (state.equalsIgnoreCase(TXPATHDISABLESTATE)) {
            Log.d(TAG, "set audio path out of FM Tx succeed, audio state =\" " + state + "\"");
            return true;
        }
        Log.e(TAG, "set audio path out of FM Tx failed, audio state = \"" + state + "\"");
        return false;
    }
}

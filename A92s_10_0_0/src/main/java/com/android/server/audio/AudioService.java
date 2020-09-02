package com.android.server.audio;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IUidObserver;
import android.app.NotificationManager;
import android.app.role.OnRoleHoldersChangedListener;
import android.app.role.RoleManager;
import android.bluetooth.BluetoothDevice;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.hardware.hdmi.HdmiAudioSystemClient;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.hdmi.HdmiTvClient;
import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioRecordingConfiguration;
import android.media.AudioRoutesInfo;
import android.media.AudioSystem;
import android.media.IAudioFocusDispatcher;
import android.media.IAudioRoutesObserver;
import android.media.IAudioServerStateDispatcher;
import android.media.IAudioService;
import android.media.IPlaybackConfigDispatcher;
import android.media.IRecordingConfigDispatcher;
import android.media.IRingtonePlayer;
import android.media.IVolumeController;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.PlayerBase;
import android.media.SoundPool;
import android.media.VolumePolicy;
import android.media.audiopolicy.AudioMix;
import android.media.audiopolicy.AudioPolicyConfig;
import android.media.audiopolicy.AudioProductStrategy;
import android.media.audiopolicy.AudioVolumeGroup;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionCallback;
import android.media.projection.IMediaProjectionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.IntArray;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.BatteryService;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.audio.AudioEventLogger;
import com.android.server.audio.AudioServiceEvents;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.OppoBrightUtils;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.oppo.IElsaManager;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.pm.DumpState;
import com.android.server.pm.UserManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.usb.descriptors.UsbTerminalTypes;
import com.android.server.utils.PriorityDump;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.oppo.atlas.OppoAtlasManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xmlpull.v1.XmlPullParserException;

public class AudioService extends IAudioService.Stub implements AccessibilityManager.TouchExplorationStateChangeListener, AccessibilityManager.AccessibilityServicesStateChangeListener {
    private static final String ASSET_FILE_VERSION = "1.0";
    private static final String ATTR_ASSET_FILE = "file";
    private static final String ATTR_ASSET_ID = "id";
    private static final String ATTR_GROUP_NAME = "name";
    private static final String ATTR_VERSION = "version";
    public static final String AUDIO_INPUT_CHANNEL = "coloros_sound_input_channel";
    static final int CONNECTION_STATE_CONNECTED = 1;
    static final int CONNECTION_STATE_DISCONNECTED = 0;
    protected static final boolean DEBUG_AP;
    protected static final boolean DEBUG_DEVICES;
    protected static final boolean DEBUG_MODE;
    protected static final boolean DEBUG_VOL;
    private static final int DEFAULT_STREAM_TYPE_OVERRIDE_DELAY_MS = 0;
    protected static final int DEFAULT_VOL_STREAM_NO_PLAYBACK = 3;
    private static final int FLAG_ADJUST_VOLUME = 1;
    private static final String GROUP_TOUCH_SOUNDS = "touch_sounds";
    private static final int INDICATE_SYSTEM_READY_RETRY_DELAY_MS = 1000;
    protected static final boolean LOGD = ("eng".equals(Build.TYPE) || "userdebug".equals(Build.TYPE));
    static final int LOG_NB_EVENTS_DEVICE_CONNECTION = 30;
    static final int LOG_NB_EVENTS_DYN_POLICY = 10;
    static final int LOG_NB_EVENTS_FORCE_USE = 20;
    static final int LOG_NB_EVENTS_PHONE_STATE = 20;
    static final int LOG_NB_EVENTS_VOLUME = 40;
    protected static int[] MAX_STREAM_VOLUME = {7, 16, 16, 16, 16, 16, 15, 16, 16, 16, 15};
    protected static int[] MIN_STREAM_VOLUME = {1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0};
    private static final int MSG_ACCESSORY_PLUG_MEDIA_UNMUTE = 21;
    private static final int MSG_AUDIO_SERVER_DIED = 4;
    private static final int MSG_BT_HEADSET_CNCT_FAILED = 9;
    private static final int MSG_CHECK_MUSIC_ACTIVE = 11;
    private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME = 12;
    private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME_FORCED = 13;
    private static final int MSG_DISABLE_AUDIO_FOR_UID = 100;
    private static final int MSG_DISPATCH_AUDIO_SERVER_STATE = 23;
    private static final int MSG_DYN_POLICY_MIX_STATE_UPDATE = 19;
    private static final int MSG_ENABLE_SURROUND_FORMATS = 24;
    private static final int MSG_HDMI_VOLUME_CHECK = 28;
    private static final int MSG_INDICATE_SYSTEM_READY = 20;
    private static final int MSG_LOAD_SOUND_EFFECTS = 7;
    private static final int MSG_NOTIFY_VOL_EVENT = 22;
    private static final int MSG_OBSERVE_DEVICES_FOR_ALL_STREAMS = 27;
    private static final int MSG_PERSIST_MUSIC_ACTIVE_MS = 17;
    private static final int MSG_PERSIST_RINGER_MODE = 3;
    private static final int MSG_PERSIST_SAFE_VOLUME_STATE = 14;
    private static final int MSG_PERSIST_VOLUME = 1;
    private static final int MSG_PLAYBACK_CONFIG_CHANGE = 29;
    private static final int MSG_PLAY_SOUND_EFFECT = 5;
    private static final int MSG_SET_ALL_VOLUMES = 10;
    private static final int MSG_SET_DEVICE_STREAM_VOLUME = 26;
    private static final int MSG_SET_DEVICE_VOLUME = 0;
    private static final int MSG_SET_FORCE_USE = 8;
    private static final int MSG_SYSTEM_READY = 16;
    private static final int MSG_UNLOAD_SOUND_EFFECTS = 15;
    private static final int MSG_UNMUTE_STREAM = 18;
    private static final int MSG_UPDATE_RINGER_MODE = 25;
    private static final int MSG_VOLUME_FADE = 61;
    private static final int MUSIC_ACTIVE_POLL_PERIOD_MS = 60000;
    private static final int NUM_SOUNDPOOL_CHANNELS = 4;
    private static final String OPPO_MODE_RINGER = "oppo_mode_ringer";
    private static final int PERSIST_DELAY = 500;
    private static final String[] RINGER_MODE_NAMES = {"SILENT", "VIBRATE", PriorityDump.PRIORITY_ARG_NORMAL};
    private static final int SAFE_MEDIA_VOLUME_ACTIVE = 3;
    private static final int SAFE_MEDIA_VOLUME_DISABLED = 1;
    private static final int SAFE_MEDIA_VOLUME_INACTIVE = 2;
    private static final int SAFE_MEDIA_VOLUME_NOT_CONFIGURED = 0;
    private static final int SAFE_VOLUME_CONFIGURE_TIMEOUT_MS = 30000;
    private static final int SENDMSG_NOOP = 1;
    private static final int SENDMSG_QUEUE = 2;
    private static final int SENDMSG_REPLACE = 0;
    private static final int SOUND_EFFECTS_LOAD_TIMEOUT_MS = 5000;
    private static final String SOUND_EFFECTS_PATH = "/media/audio/ui/";
    /* access modifiers changed from: private */
    public static final List<String> SOUND_EFFECT_FILES = new ArrayList();
    private static final int[] STREAM_VOLUME_OPS = {34, 36, 35, 36, 37, 38, 39, 36, 36, 36, 64};
    private static final String TAG = "AS.AudioService";
    private static final String TAG_ASSET = "asset";
    private static final String TAG_AUDIO_ASSETS = "audio_assets";
    private static final String TAG_GROUP = "group";
    private static final int TOUCH_EXPLORE_STREAM_TYPE_OVERRIDE_DELAY_MS = 1000;
    private static final int UNMUTE_STREAM_DELAY = 350;
    private static final int UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX = 72000000;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    public static boolean mDialogOn = false;
    protected static int[] mStreamVolumeAlias;
    static final AudioEventLogger sDeviceLogger = new AudioEventLogger(30, "wired/A2DP/hearing aid device connection");
    static final AudioEventLogger sForceUseLogger = new AudioEventLogger(20, "force use (logged before setForceUse() is executed)");
    private static boolean sIndependentA11yVolume = false;
    /* access modifiers changed from: private */
    public static int sSoundEffectVolumeDb;
    private static int sStreamOverrideDelayMs;
    static final AudioEventLogger sVolumeLogger = new AudioEventLogger(40, "volume changes (logged when command received by AudioService)");
    /* access modifiers changed from: private */
    public final int[][] SOUND_EFFECT_FILES_MAP = ((int[][]) Array.newInstance(int.class, 10, 2));
    private final int[] STREAM_VOLUME_ALIAS_DEFAULT = {0, 2, 2, 3, 4, 2, 6, 2, 2, 3, 3};
    private final int[] STREAM_VOLUME_ALIAS_TELEVISION = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
    private final int[] STREAM_VOLUME_ALIAS_VOICE = {0, 2, 2, 3, 4, 2, 6, 2, 2, 3, 3};
    int mAbsVolumeMultiModeCaseDevices = 134217728;
    /* access modifiers changed from: private */
    public int[] mAccessibilityServiceUids;
    /* access modifiers changed from: private */
    public final Object mAccessibilityServiceUidsLock = new Object();
    private final ActivityManagerInternal mActivityManagerInternal;
    private boolean mAdjustVolumeAction = false;
    private final AppOpsManager mAppOps;
    @GuardedBy({"mSettingsLock"})
    private int mAssistantUid;
    /* access modifiers changed from: private */
    public PowerManager.WakeLock mAudioEventWakeLock;
    /* access modifiers changed from: private */
    public AudioHandler mAudioHandler;
    /* access modifiers changed from: private */
    public final HashMap<IBinder, AudioPolicyProxy> mAudioPolicies = new HashMap<>();
    @GuardedBy({"mAudioPolicies"})
    private int mAudioPolicyCounter = 0;
    /* access modifiers changed from: private */
    public HashMap<IBinder, AsdProxy> mAudioServerStateListeners = new HashMap<>();
    private final Object mAudioSettingLock = new Object();
    private final AudioSystem.ErrorCallback mAudioSystemCallback = new AudioSystem.ErrorCallback() {
        /* class com.android.server.audio.AudioService.AnonymousClass1 */

        public void onError(int error) {
            if (error == 100) {
                AudioService.this.mRecordMonitor.onAudioServerDied();
                AudioService.sendMsg(AudioService.this.mAudioHandler, 4, 1, 0, 0, null, 0);
                AudioService.sendMsg(AudioService.this.mAudioHandler, 23, 2, 0, 0, null, 0);
            }
        }
    };
    private AudioSystemThread mAudioSystemThread;
    /* access modifiers changed from: private */
    @GuardedBy({"mSettingsLock"})
    public boolean mCameraSoundForced;
    /* access modifiers changed from: private */
    public final ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final AudioDeviceBroker mDeviceBroker;
    private boolean mDockAudioMediaEnabled = true;
    /* access modifiers changed from: private */
    public int mDockState = 0;
    private final AudioSystem.DynamicPolicyCallback mDynPolicyCallback = new AudioSystem.DynamicPolicyCallback() {
        /* class com.android.server.audio.AudioService.AnonymousClass5 */

        public void onDynamicPolicyMixStateUpdate(String regId, int state) {
            if (!TextUtils.isEmpty(regId)) {
                AudioService.sendMsg(AudioService.this.mAudioHandler, 19, 2, state, 0, regId, 0);
            }
        }
    };
    private final AudioEventLogger mDynPolicyLogger = new AudioEventLogger(10, "dynamic policy events (logged when command received by AudioService)");
    /* access modifiers changed from: private */
    public String mEnabledSurroundFormats;
    /* access modifiers changed from: private */
    public int mEncodedSurroundMode;
    /* access modifiers changed from: private */
    public IAudioPolicyCallback mExtVolumeController;
    /* access modifiers changed from: private */
    public final Object mExtVolumeControllerLock = new Object();
    int mFixedVolumeDevices = 2890752;
    /* access modifiers changed from: private */
    public ForceControlStreamClient mForceControlStreamClient = null;
    /* access modifiers changed from: private */
    public final Object mForceControlStreamLock = new Object();
    int mFullVolumeDevices = 0;
    private final boolean mHasVibrator;
    @GuardedBy({"mHdmiClientLock"})
    private HdmiAudioSystemClient mHdmiAudioSystemClient;
    /* access modifiers changed from: private */
    public boolean mHdmiCecSink;
    /* access modifiers changed from: private */
    public final Object mHdmiClientLock = new Object();
    private MyDisplayStatusCallback mHdmiDisplayStatusCallback = new MyDisplayStatusCallback();
    /* access modifiers changed from: private */
    @GuardedBy({"mHdmiClientLock"})
    public HdmiControlManager mHdmiManager;
    @GuardedBy({"mHdmiClientLock"})
    private HdmiPlaybackClient mHdmiPlaybackClient;
    private boolean mHdmiSystemAudioSupported = false;
    @GuardedBy({"mHdmiClientLock"})
    private HdmiTvClient mHdmiTvClient;
    private boolean mIsExportVersion = false;
    private boolean mIsInSetCallMode = false;
    private boolean mIsJPVersion = false;
    private boolean mIsReadAudioSetting = false;
    /* access modifiers changed from: private */
    public final boolean mIsSingleVolume;
    private long mLoweredFromNormalToVibrateTime;
    private int mMcc = 0;
    /* access modifiers changed from: private */
    public final MediaFocusControl mMediaFocusControl;
    private int mMode = 0;
    private final AudioEventLogger mModeLogger = new AudioEventLogger(20, "phone state (logged after successfull call to AudioSystem.setPhoneState(int))");
    /* access modifiers changed from: private */
    public final boolean mMonitorRotation;
    private int mMusicActiveMs;
    private int mMuteAffectedStreams;
    private NotificationManager mNm;
    private StreamVolumeCommand mPendingVolumeCommand;
    private final int mPlatformType;
    /* access modifiers changed from: private */
    public final PlaybackActivityMonitor mPlaybackMonitor;
    /* access modifiers changed from: private */
    public float[] mPrescaleAbsoluteVolume = {0.5f, 0.7f, 0.85f};
    private int mPrevVolDirection = 0;
    private IMediaProjectionManager mProjectionService;
    private final BroadcastReceiver mReceiver = new AudioServiceBroadcastReceiver();
    /* access modifiers changed from: private */
    public final RecordingActivityMonitor mRecordMonitor;
    private int mRingerAndZenModeMutedStreams;
    @GuardedBy({"mSettingsLock"})
    private int mRingerMode;
    private int mRingerModeAffectedStreams = 0;
    /* access modifiers changed from: private */
    public AudioManagerInternal.RingerModeDelegate mRingerModeDelegate;
    @GuardedBy({"mSettingsLock"})
    private int mRingerModeExternal = -1;
    private volatile IRingtonePlayer mRingtonePlayer;
    private ArrayList<RmtSbmxFullVolDeathHandler> mRmtSbmxFullVolDeathHandlers = new ArrayList<>();
    private int mRmtSbmxFullVolRefCount = 0;
    RoleObserver mRoleObserver;
    int mSafeMediaVolumeDevices = 67109308;
    private int mSafeMediaVolumeIndex;
    private int mSafeMediaVolumeState;
    private final Object mSafeMediaVolumeStateLock = new Object();
    private float mSafeUsbMediaVolumeDbfs;
    private int mSafeUsbMediaVolumeIndex;
    @GuardedBy({"mDeviceBroker.mSetModeLock"})
    final ArrayList<SetModeDeathHandler> mSetModeDeathHandlers = new ArrayList<>();
    private final Object mSettingCallLock = new Object();
    /* access modifiers changed from: private */
    public final Object mSettingsLock = new Object();
    private SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public final Object mSoundEffectsLock = new Object();
    /* access modifiers changed from: private */
    public SoundPool mSoundPool;
    /* access modifiers changed from: private */
    public SoundPoolCallback mSoundPoolCallBack;
    /* access modifiers changed from: private */
    public SoundPoolListenerThread mSoundPoolListenerThread;
    /* access modifiers changed from: private */
    public Looper mSoundPoolLooper = null;
    /* access modifiers changed from: private */
    public VolumeStreamState[] mStreamStates;
    /* access modifiers changed from: private */
    public boolean mSurroundModeChanged;
    /* access modifiers changed from: private */
    public boolean mSystemReady;
    private final IUidObserver mUidObserver = new IUidObserver.Stub() {
        /* class com.android.server.audio.AudioService.AnonymousClass2 */

        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
        }

        public void onUidGone(int uid, boolean disabled) {
            disableAudioForUid(false, uid);
        }

        public void onUidActive(int uid) throws RemoteException {
        }

        public void onUidIdle(int uid, boolean disabled) {
        }

        public void onUidCachedChanged(int uid, boolean cached) {
            disableAudioForUid(cached, uid);
        }

        private void disableAudioForUid(boolean disable, int uid) {
            AudioService audioService = AudioService.this;
            audioService.queueMsgUnderWakeLock(audioService.mAudioHandler, 100, disable ? 1 : 0, uid, null, 0);
        }
    };
    /* access modifiers changed from: private */
    public final boolean mUseFixedVolume;
    private final UserManagerInternal mUserManagerInternal;
    private final UserManagerInternal.UserRestrictionsListener mUserRestrictionsListener = new AudioServiceUserRestrictionsListener();
    /* access modifiers changed from: private */
    public boolean mUserSelectedVolumeControlStream = false;
    /* access modifiers changed from: private */
    public boolean mUserSwitchedReceived;
    private int mVibrateSetting;
    private Vibrator mVibrator;
    private AtomicBoolean mVoiceActive = new AtomicBoolean(false);
    private final IPlaybackConfigDispatcher mVoiceActivityMonitor = new IPlaybackConfigDispatcher.Stub() {
        /* class com.android.server.audio.AudioService.AnonymousClass3 */

        public void dispatchPlaybackConfigChange(List<AudioPlaybackConfiguration> configs, boolean flush) {
            AudioService.sendMsg(AudioService.this.mAudioHandler, 29, 0, 0, 0, configs, 0);
        }
    };
    /* access modifiers changed from: private */
    public int mVolumeControlStream = -1;
    /* access modifiers changed from: private */
    public final VolumeController mVolumeController = new VolumeController();
    private int mVolumeCurrentIndex = 0;
    private int mVolumeFadeDevice = 0;
    private int mVolumeFinalIndex = 0;
    private VolumePolicy mVolumePolicy = VolumePolicy.DEFAULT;
    private int mZenModeAffectedStreams = 0;

    @Retention(RetentionPolicy.SOURCE)
    public @interface BtProfileConnectionState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectionState {
    }

    static /* synthetic */ int access$9708(AudioService x0) {
        int i = x0.mAudioPolicyCounter;
        x0.mAudioPolicyCounter = i + 1;
        return i;
    }

    static {
        boolean z = LOGD;
        DEBUG_MODE = z;
        DEBUG_AP = z;
        DEBUG_VOL = z;
        DEBUG_DEVICES = z;
    }

    private boolean isPlatformVoice() {
        return this.mPlatformType == 1;
    }

    /* access modifiers changed from: package-private */
    public boolean isPlatformTelevision() {
        return this.mPlatformType == 2;
    }

    /* access modifiers changed from: package-private */
    public boolean isPlatformAutomotive() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive");
    }

    /* access modifiers changed from: package-private */
    public int getVssVolumeForDevice(int stream, int device) {
        return this.mStreamStates[stream].getIndex(device);
    }

    public static String makeAlsaAddressString(int card, int device) {
        return "card=" + card + ";device=" + device + ";";
    }

    public static final class Lifecycle extends SystemService {
        private AudioService mService;

        public Lifecycle(Context context) {
            super(context);
            this.mService = new AudioService(context);
        }

        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.audio.AudioService, android.os.IBinder] */
        @Override // com.android.server.SystemService
        public void onStart() {
            publishBinderService("audio", this.mService);
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mService.systemReady();
            }
        }
    }

    public AudioService(Context context) {
        int i;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mPlatformType = AudioSystem.getPlatformType(context);
        this.mIsSingleVolume = AudioSystem.isSingleVolume(context);
        this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mAudioEventWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "handleAudioEvent");
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        Vibrator vibrator = this.mVibrator;
        this.mHasVibrator = vibrator == null ? false : vibrator.hasVibrator();
        this.mIsExportVersion = !SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("CN");
        if (AudioProductStrategy.getAudioProductStrategies().size() > 0) {
            for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                AudioAttributes attr = AudioProductStrategy.getAudioAttributesForStrategyWithLegacyStreamType(streamType);
                int maxVolume = AudioSystem.getMaxVolumeIndexForAttributes(attr);
                if (maxVolume != -1) {
                    MAX_STREAM_VOLUME[streamType] = maxVolume;
                }
                int minVolume = AudioSystem.getMinVolumeIndexForAttributes(attr);
                if (minVolume != -1) {
                    MIN_STREAM_VOLUME[streamType] = minVolume;
                }
            }
        }
        sSoundEffectVolumeDb = context.getResources().getInteger(17694895);
        createAudioSystemThread();
        AudioSystem.setErrorCallback(this.mAudioSystemCallback);
        this.mIsJPVersion = isJPVersion();
        boolean cameraSoundForced = readCameraSoundForced();
        this.mCameraSoundForced = new Boolean(cameraSoundForced).booleanValue();
        AudioHandler audioHandler = this.mAudioHandler;
        if (cameraSoundForced) {
            i = 11;
        } else {
            i = 0;
        }
        sendMsg(audioHandler, 8, 2, 4, i, new String("AudioService ctor"), 0);
        this.mSafeMediaVolumeState = Settings.Global.getInt(this.mContentResolver, "audio_safe_volume_state", 0);
        this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(17694879) * 10;
        this.mUseFixedVolume = this.mContext.getResources().getBoolean(17891559);
        this.mDeviceBroker = new AudioDeviceBroker(this.mContext, this);
        updateStreamVolumeAlias(false, TAG);
        readPersistedSettings();
        readUserRestrictions();
        this.mSettingsObserver = new SettingsObserver();
        createStreamStates();
        this.mSafeUsbMediaVolumeIndex = getSafeUsbMediaVolumeIndex();
        this.mPlaybackMonitor = new PlaybackActivityMonitor(context, MAX_STREAM_VOLUME[4]);
        this.mMediaFocusControl = new MediaFocusControl(this.mContext, this.mPlaybackMonitor);
        this.mRecordMonitor = new RecordingActivityMonitor(this.mContext);
        readAndSetLowRamDevice();
        this.mRingerAndZenModeMutedStreams = 0;
        setRingerModeInt(getRingerModeInternal(), false);
        IntentFilter intentFilter = new IntentFilter("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED");
        intentFilter.addAction("android.intent.action.DOCK_EVENT");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_BACKGROUND");
        intentFilter.addAction("android.intent.action.USER_FOREGROUND");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        intentFilter.addAction("android.intent.action.PACKAGES_SUSPENDED");
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mMonitorRotation = SystemProperties.getBoolean("ro.audio.monitorRotation", false);
        if (this.mMonitorRotation) {
            RotationHelper.init(this.mContext, this.mAudioHandler);
        }
        intentFilter.addAction("android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION");
        intentFilter.addAction("android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION");
        context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
        LocalServices.addService(AudioManagerInternal.class, new AudioServiceInternal());
        this.mUserManagerInternal.addUserRestrictionsListener(this.mUserRestrictionsListener);
        this.mRecordMonitor.initMonitor();
        float[] preScale = {this.mContext.getResources().getFraction(18022403, 1, 1), this.mContext.getResources().getFraction(18022404, 1, 1), this.mContext.getResources().getFraction(18022405, 1, 1)};
        for (int i2 = 0; i2 < preScale.length; i2++) {
            if (OppoBrightUtils.MIN_LUX_LIMITI <= preScale[i2] && preScale[i2] <= 1.0f) {
                this.mPrescaleAbsoluteVolume[i2] = preScale[i2];
            }
        }
    }

    public void systemReady() {
        sendMsg(this.mAudioHandler, 16, 2, 0, 0, null, 0);
    }

    public void onSystemReady() {
        this.mSystemReady = true;
        scheduleLoadSoundEffects();
        this.mDeviceBroker.onSystemReady();
        int i = 0;
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.hdmi.cec")) {
            synchronized (this.mHdmiClientLock) {
                this.mHdmiManager = (HdmiControlManager) this.mContext.getSystemService(HdmiControlManager.class);
                this.mHdmiTvClient = this.mHdmiManager.getTvClient();
                if (this.mHdmiTvClient != null) {
                    this.mFixedVolumeDevices &= -2883587;
                }
                this.mHdmiPlaybackClient = this.mHdmiManager.getPlaybackClient();
                if (this.mHdmiPlaybackClient != null) {
                    this.mFixedVolumeDevices &= -1025;
                    this.mFullVolumeDevices |= 1024;
                }
                this.mHdmiCecSink = false;
                this.mHdmiAudioSystemClient = this.mHdmiManager.getAudioSystemClient();
            }
        }
        this.mNm = (NotificationManager) this.mContext.getSystemService("notification");
        AudioHandler audioHandler = this.mAudioHandler;
        if (!SystemProperties.getBoolean("audio.safemedia.bypass", false)) {
            i = SAFE_VOLUME_CONFIGURE_TIMEOUT_MS;
        }
        sendMsg(audioHandler, 13, 0, 0, 0, TAG, i);
        initA11yMonitoring();
        this.mRoleObserver = new RoleObserver();
        this.mRoleObserver.register();
        onIndicateSystemReady();
    }

    class RoleObserver implements OnRoleHoldersChangedListener {
        private final Executor mExecutor;
        private RoleManager mRm;

        RoleObserver() {
            this.mExecutor = AudioService.this.mContext.getMainExecutor();
        }

        public void register() {
            this.mRm = (RoleManager) AudioService.this.mContext.getSystemService("role");
            RoleManager roleManager = this.mRm;
            if (roleManager != null) {
                roleManager.addOnRoleHoldersChangedListenerAsUser(this.mExecutor, this, UserHandle.ALL);
                AudioService.this.updateAssistantUId(true);
            }
        }

        public void onRoleHoldersChanged(String roleName, UserHandle user) {
            if ("android.app.role.ASSISTANT".equals(roleName)) {
                AudioService.this.updateAssistantUId(false);
            }
        }

        public String getAssistantRoleHolder() {
            RoleManager roleManager = this.mRm;
            if (roleManager == null) {
                return "";
            }
            List<String> assistants = roleManager.getRoleHolders("android.app.role.ASSISTANT");
            return assistants.size() == 0 ? "" : assistants.get(0);
        }
    }

    /* access modifiers changed from: package-private */
    public void onIndicateSystemReady() {
        if (AudioSystem.systemReady() != 0) {
            sendMsg(this.mAudioHandler, 20, 0, 0, 0, null, 1000);
        }
    }

    public void onAudioServerDied() {
        int forDock;
        int forSys;
        if (!this.mSystemReady || AudioSystem.checkAudioFlinger() != 0) {
            Log.e(TAG, "Audioserver died.");
            sendMsg(this.mAudioHandler, 4, 1, 0, 0, null, 500);
            return;
        }
        Log.e(TAG, "Audioserver started.");
        AudioSystem.setParameters("restarting=true");
        readAndSetLowRamDevice();
        this.mDeviceBroker.onAudioServerDied();
        if (AudioSystem.setPhoneState(this.mMode) == 0) {
            this.mModeLogger.log(new AudioEventLogger.StringEvent("onAudioServerDied causes setPhoneState(" + AudioSystem.modeToString(this.mMode) + ")"));
        }
        synchronized (this.mSettingsLock) {
            forDock = 0;
            forSys = this.mCameraSoundForced ? 11 : 0;
        }
        if (LOGD) {
            Log.i(TAG, "setForceUse(" + AudioSystem.forceUseUsageToString(4) + ", " + AudioSystem.forceUseConfigToString(forSys) + ") due to " + "onAudioServerDied");
        }
        this.mDeviceBroker.setForceUse_Async(4, forSys, "onAudioServerDied");
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
        updateMasterBalance(this.mContentResolver);
        setRingerModeInt(getRingerModeInternal(), false);
        if (this.mMonitorRotation) {
            RotationHelper.updateOrientation();
        }
        updateInputDevice(this.mContentResolver);
        synchronized (this.mSettingsLock) {
            if (this.mDockAudioMediaEnabled) {
                forDock = 8;
            }
            if (LOGD) {
                Log.i(TAG, "setForceUse(" + AudioSystem.forceUseUsageToString(3) + ", " + AudioSystem.forceUseConfigToString(forDock) + ") due to " + "onAudioServerDied");
            }
            this.mDeviceBroker.setForceUse_Async(3, forDock, "onAudioServerDied");
            sendEncodedSurroundMode(this.mContentResolver, "onAudioServerDied");
            sendEnabledSurroundFormats(this.mContentResolver, true);
            updateAssistantUId(true);
            updateRttEanbled(this.mContentResolver);
        }
        synchronized (this.mAccessibilityServiceUidsLock) {
            AudioSystem.setA11yServicesUids(this.mAccessibilityServiceUids);
        }
        synchronized (this.mHdmiClientLock) {
            if (!(this.mHdmiManager == null || this.mHdmiTvClient == null)) {
                setHdmiSystemAudioSupported(this.mHdmiSystemAudioSupported);
            }
        }
        synchronized (this.mAudioPolicies) {
            for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                policy.connectMixes();
            }
        }
        onIndicateSystemReady();
        AudioSystem.setParameters("restarting=false");
        sendBroadcastToAll(new Intent("audio_server_restarted"));
        sendMsg(this.mAudioHandler, 23, 2, 1, 0, null, 0);
    }

    /* access modifiers changed from: private */
    public void onDispatchAudioServerStateChange(boolean state) {
        synchronized (this.mAudioServerStateListeners) {
            for (AsdProxy asdp : this.mAudioServerStateListeners.values()) {
                try {
                    asdp.callback().dispatchAudioServerStateChange(state);
                } catch (RemoteException e) {
                    Log.w(TAG, "Could not call dispatchAudioServerStateChange()", e);
                }
            }
        }
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

    public List<AudioProductStrategy> getAudioProductStrategies() {
        return AudioProductStrategy.getAudioProductStrategies();
    }

    public List<AudioVolumeGroup> getAudioVolumeGroups() {
        return AudioVolumeGroup.getAudioVolumeGroups();
    }

    private void checkAllAliasStreamVolumes() {
        synchronized (this.mSettingsLock) {
            synchronized (VolumeStreamState.class) {
                int numStreamTypes = AudioSystem.getNumStreamTypes();
                for (int streamType = 0; streamType < numStreamTypes; streamType++) {
                    this.mStreamStates[streamType].setAllIndexes(this.mStreamStates[mStreamVolumeAlias[streamType]], TAG);
                    if (!this.mStreamStates[streamType].mIsMuted) {
                        this.mStreamStates[streamType].applyAllVolumes();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void postCheckVolumeCecOnHdmiConnection(int state, String caller) {
        sendMsg(this.mAudioHandler, 28, 0, state, 0, caller, 0);
    }

    /* access modifiers changed from: private */
    public void onCheckVolumeCecOnHdmiConnection(int state, String caller) {
        if (state == 1) {
            if ((this.mSafeMediaVolumeDevices & 1024) != 0) {
                sendMsg(this.mAudioHandler, 11, 0, 0, 0, caller, 60000);
            }
            if (isPlatformTelevision()) {
                checkAddAllFixedVolumeDevices(1024, caller);
                synchronized (this.mHdmiClientLock) {
                    if (!(this.mHdmiManager == null || this.mHdmiPlaybackClient == null)) {
                        this.mHdmiCecSink = false;
                        this.mHdmiPlaybackClient.queryDisplayStatus(this.mHdmiDisplayStatusCallback);
                    }
                }
            }
            sendEnabledSurroundFormats(this.mContentResolver, true);
        } else if (isPlatformTelevision()) {
            synchronized (this.mHdmiClientLock) {
                if (this.mHdmiManager != null) {
                    this.mHdmiCecSink = false;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void checkAddAllFixedVolumeDevices(int device, String caller) {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = 0; streamType < numStreamTypes; streamType++) {
            if (!this.mStreamStates[streamType].hasIndexForDevice(device)) {
                VolumeStreamState[] volumeStreamStateArr = this.mStreamStates;
                volumeStreamStateArr[streamType].setIndex(volumeStreamStateArr[mStreamVolumeAlias[streamType]].getIndex(1073741824), device, caller);
            }
            this.mStreamStates[streamType].checkFixedVolumeDevices();
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
        int i = 0;
        while (true) {
            VolumeStreamState[] volumeStreamStateArr = this.mStreamStates;
            if (i < volumeStreamStateArr.length) {
                VolumeStreamState vss = volumeStreamStateArr[i];
                if (!(vss.mIndexMin <= 0 || vss.mStreamType == 0 || vss.mStreamType == 6)) {
                    this.mMuteAffectedStreams &= ~(1 << vss.mStreamType);
                }
                i++;
            } else {
                return;
            }
        }
    }

    private void createStreamStates() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        VolumeStreamState[] streams = new VolumeStreamState[numStreamTypes];
        this.mStreamStates = streams;
        for (int i = 0; i < numStreamTypes; i++) {
            streams[i] = new VolumeStreamState(Settings.System.VOLUME_SETTINGS_INT[mStreamVolumeAlias[i]], i);
        }
        checkAllFixedVolumeDevices();
        checkAllAliasStreamVolumes();
        checkMuteAffectedStreams();
        updateDefaultVolumes();
    }

    private void updateDefaultVolumes() {
        for (int stream = 0; stream < this.mStreamStates.length; stream++) {
            if (stream != mStreamVolumeAlias[stream]) {
                int[] iArr = AudioSystem.DEFAULT_STREAM_VOLUME;
                int[] iArr2 = AudioSystem.DEFAULT_STREAM_VOLUME;
                int[] iArr3 = mStreamVolumeAlias;
                iArr[stream] = rescaleIndex(iArr2[iArr3[stream]], iArr3[stream], stream);
            }
        }
    }

    private void dumpStreamStates(PrintWriter pw) {
        pw.println("\nStream volumes (device: index)");
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int i = 0; i < numStreamTypes; i++) {
            pw.println("- " + AudioSystem.STREAM_NAMES[i] + ":");
            this.mStreamStates[i].dump(pw);
            pw.println("");
        }
        pw.print("\n- mute affected streams = 0x");
        pw.println(Integer.toHexString(this.mMuteAffectedStreams));
    }

    private void updateStreamVolumeAlias(boolean updateVolumes, String caller) {
        int dtmfStreamAlias;
        int dtmfStreamAlias2;
        int dtmfStreamAlias3;
        int a11yStreamAlias = sIndependentA11yVolume ? 10 : 3;
        if (this.mIsSingleVolume) {
            mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_TELEVISION;
            dtmfStreamAlias = 3;
        } else if (this.mPlatformType != 1) {
            mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_DEFAULT;
            dtmfStreamAlias = 3;
        } else {
            mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_VOICE;
            dtmfStreamAlias = 2;
        }
        if (this.mIsSingleVolume) {
            this.mRingerModeAffectedStreams = 0;
        } else if (isInCommunication()) {
            if (isBluetoothScoOn()) {
                dtmfStreamAlias3 = 6;
            } else {
                dtmfStreamAlias3 = 0;
            }
            this.mRingerModeAffectedStreams &= -257;
            dtmfStreamAlias2 = dtmfStreamAlias3;
            int[] iArr = mStreamVolumeAlias;
            iArr[8] = dtmfStreamAlias2;
            iArr[10] = a11yStreamAlias;
            if (updateVolumes && this.mStreamStates != null) {
                updateDefaultVolumes();
                synchronized (this.mSettingsLock) {
                    synchronized (VolumeStreamState.class) {
                        this.mStreamStates[8].setAllIndexes(this.mStreamStates[dtmfStreamAlias2], caller);
                        String unused = this.mStreamStates[10].mVolumeIndexSettingName = Settings.System.VOLUME_SETTINGS_INT[a11yStreamAlias];
                        this.mStreamStates[10].setAllIndexes(this.mStreamStates[a11yStreamAlias], caller);
                    }
                }
                if (sIndependentA11yVolume) {
                    this.mStreamStates[10].readSettings();
                }
                setRingerModeInt(getRingerModeInternal(), false);
                sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[8], 0);
                sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[10], 0);
                return;
            }
        } else {
            this.mRingerModeAffectedStreams |= 256;
        }
        dtmfStreamAlias2 = dtmfStreamAlias;
        int[] iArr2 = mStreamVolumeAlias;
        iArr2[8] = dtmfStreamAlias2;
        iArr2[10] = a11yStreamAlias;
        if (updateVolumes) {
        }
    }

    /* access modifiers changed from: private */
    public void readDockAudioSettings(ContentResolver cr) {
        int i = 0;
        boolean z = true;
        if (Settings.Global.getInt(cr, "dock_audio_media_enabled", 0) != 1) {
            z = false;
        }
        this.mDockAudioMediaEnabled = z;
        AudioHandler audioHandler = this.mAudioHandler;
        if (this.mDockAudioMediaEnabled) {
            i = 8;
        }
        sendMsg(audioHandler, 8, 2, 3, i, new String("readDockAudioSettings"), 0);
    }

    /* access modifiers changed from: private */
    public void updateMasterMono(ContentResolver cr) {
        boolean masterMono = Settings.System.getIntForUser(cr, "master_mono", 0, -2) == 1;
        if (DEBUG_VOL) {
            Log.d(TAG, String.format("Master mono %b", Boolean.valueOf(masterMono)));
        }
        AudioSystem.setMasterMono(masterMono);
    }

    /* access modifiers changed from: private */
    public void updateMasterBalance(ContentResolver cr) {
        float masterBalance = Settings.System.getFloatForUser(cr, "master_balance", OppoBrightUtils.MIN_LUX_LIMITI, -2);
        if (DEBUG_VOL) {
            Log.d(TAG, String.format("Master balance %f", Float.valueOf(masterBalance)));
        }
        if (AudioSystem.setMasterBalance(masterBalance) != 0) {
            Log.e(TAG, String.format("setMasterBalance failed for %f", Float.valueOf(masterBalance)));
        }
    }

    private void sendEncodedSurroundMode(ContentResolver cr, String eventSource) {
        sendEncodedSurroundMode(Settings.Global.getInt(cr, "encoded_surround_output", 0), eventSource);
    }

    /* access modifiers changed from: private */
    public void sendEncodedSurroundMode(int encodedSurroundMode, String eventSource) {
        int forceSetting = 16;
        if (encodedSurroundMode == 0) {
            forceSetting = 0;
        } else if (encodedSurroundMode == 1) {
            forceSetting = 13;
        } else if (encodedSurroundMode == 2) {
            forceSetting = 14;
        } else if (encodedSurroundMode != 3) {
            Log.e(TAG, "updateSurroundSoundSettings: illegal value " + encodedSurroundMode);
        } else {
            forceSetting = 15;
        }
        if (forceSetting != 16) {
            this.mDeviceBroker.setForceUse_Async(6, forceSetting, eventSource);
        }
    }

    /* access modifiers changed from: private */
    public void sendEnabledSurroundFormats(ContentResolver cr, boolean forceUpdate) {
        String enabledSurroundFormats;
        if (this.mEncodedSurroundMode == 3) {
            String enabledSurroundFormats2 = Settings.Global.getString(cr, "encoded_surround_output_enabled_formats");
            if (enabledSurroundFormats2 == null) {
                enabledSurroundFormats = "";
            } else {
                enabledSurroundFormats = enabledSurroundFormats2;
            }
            if (forceUpdate || !TextUtils.equals(enabledSurroundFormats, this.mEnabledSurroundFormats)) {
                this.mEnabledSurroundFormats = enabledSurroundFormats;
                String[] surroundFormats = TextUtils.split(enabledSurroundFormats, ",");
                ArrayList<Integer> formats = new ArrayList<>();
                for (String format : surroundFormats) {
                    try {
                        int audioFormat = Integer.valueOf(format).intValue();
                        boolean isSurroundFormat = false;
                        int[] iArr = AudioFormat.SURROUND_SOUND_ENCODING;
                        int length = iArr.length;
                        int i = 0;
                        while (true) {
                            if (i >= length) {
                                break;
                            } else if (iArr[i] == audioFormat) {
                                isSurroundFormat = true;
                                break;
                            } else {
                                i++;
                            }
                        }
                        if (isSurroundFormat && !formats.contains(Integer.valueOf(audioFormat))) {
                            formats.add(Integer.valueOf(audioFormat));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Invalid enabled surround format:" + format);
                    }
                }
                Settings.Global.putString(this.mContext.getContentResolver(), "encoded_surround_output_enabled_formats", TextUtils.join(",", formats));
                sendMsg(this.mAudioHandler, 24, 2, 0, 0, formats, 0);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateInputDevice(ContentResolver cr) {
        int newInputChannel = Settings.Secure.getInt(cr, AUDIO_INPUT_CHANNEL, 0);
        AudioSystem.setParameters("update_input_device=" + newInputChannel);
    }

    public void oppoSetParameters(String keyValuePairs) {
        Log.d(TAG, "oppoSetParameters() keyValuePairs:" + keyValuePairs);
        if (keyValuePairs.startsWith("restoreVolumeBeforeSafeMediaVolume")) {
            oppoRestoreVolumeBeforeSafeMediaVolume();
        } else if (keyValuePairs.startsWith("dialogON")) {
            mDialogOn = true;
        } else if (keyValuePairs.startsWith("dialogOFF")) {
            mDialogOn = false;
        }
    }

    public int isNeedShowUiWarnings(int flags, String callingPackage) {
        if ((flags & 1) != 0 || (flags & 1024) != 0 || callingPackage.equals("com.oppo.engineermode")) {
            return flags;
        }
        Log.d(TAG, "postDisplaySafeVolumeWarning flags = " + flags);
        return flags | 1024;
    }

    /* access modifiers changed from: private */
    public void onEnableSurroundFormats(ArrayList<Integer> enabledSurroundFormats) {
        int[] iArr = AudioFormat.SURROUND_SOUND_ENCODING;
        for (int surroundFormat : iArr) {
            boolean enabled = enabledSurroundFormats.contains(Integer.valueOf(surroundFormat));
            Log.i(TAG, "enable surround format:" + surroundFormat + StringUtils.SPACE + enabled + StringUtils.SPACE + AudioSystem.setSurroundFormatEnabled(surroundFormat, enabled));
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mSettingsLock"})
    public void updateAssistantUId(boolean forceUpdate) {
        int assistantUid = 0;
        String packageName = "";
        RoleObserver roleObserver = this.mRoleObserver;
        if (roleObserver != null) {
            packageName = roleObserver.getAssistantRoleHolder();
        }
        if (TextUtils.isEmpty(packageName)) {
            String assistantName = Settings.Secure.getStringForUser(this.mContentResolver, "voice_interaction_service", -2);
            if (TextUtils.isEmpty(assistantName)) {
                assistantName = Settings.Secure.getStringForUser(this.mContentResolver, "assistant", -2);
            }
            if (!TextUtils.isEmpty(assistantName)) {
                ComponentName componentName = ComponentName.unflattenFromString(assistantName);
                if (componentName == null) {
                    Slog.w(TAG, "Invalid service name for voice_interaction_service: " + assistantName);
                    return;
                }
                packageName = componentName.getPackageName();
            }
        }
        if (!TextUtils.isEmpty(packageName)) {
            PackageManager pm = this.mContext.getPackageManager();
            if (pm.checkPermission("android.permission.CAPTURE_AUDIO_HOTWORD", packageName) == 0) {
                try {
                    assistantUid = pm.getPackageUidAsUser(packageName, 0, getCurrentUserId());
                    Log.i(TAG, "updateAssistantUId assistantUid=" + assistantUid + " packageName=" + packageName + " ,getCurrentUserId=" + getCurrentUserId());
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "updateAssistantUId() could not find UID for package: " + packageName);
                }
            }
        }
        if (assistantUid != this.mAssistantUid || forceUpdate) {
            AudioSystem.setAssistantUid(assistantUid);
            this.mAssistantUid = assistantUid;
        }
    }

    /* access modifiers changed from: private */
    public void updateRttEanbled(ContentResolver cr) {
        boolean rttEnabled = false;
        if (Settings.Secure.getIntForUser(cr, "rtt_calling_mode", 0, -2) != 0) {
            rttEnabled = true;
        }
        AudioSystem.setRttEnabled(rttEnabled);
    }

    private void readPersistedSettings() {
        int i;
        ContentResolver cr = this.mContentResolver;
        int ringerModeFromSettings = Settings.System.getIntForUser(cr, OPPO_MODE_RINGER, -1, -2);
        Log.d(TAG, "ringerModeFromSettings :" + ringerModeFromSettings);
        int i2 = 2;
        if (ringerModeFromSettings == -1) {
            ringerModeFromSettings = Settings.Global.getInt(cr, "mode_ringer", 2);
        }
        int ringerMode = ringerModeFromSettings;
        if (!isValidRingerMode(ringerMode)) {
            ringerMode = 2;
        }
        if (ringerMode == 1 && !this.mHasVibrator) {
            ringerMode = 0;
        }
        if (ringerMode != ringerModeFromSettings) {
            Settings.Global.putInt(cr, "mode_ringer", ringerMode);
            Settings.System.putIntForUser(cr, OPPO_MODE_RINGER, ringerMode, -2);
        }
        if (this.mUseFixedVolume || this.mIsSingleVolume) {
            ringerMode = 2;
        }
        synchronized (this.mSettingsLock) {
            this.mRingerMode = ringerMode;
            if (this.mRingerModeExternal == -1) {
                this.mRingerModeExternal = this.mRingerMode;
            }
            if (this.mHasVibrator) {
                i = 2;
            } else {
                i = 0;
            }
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(0, 1, i);
            int i3 = this.mVibrateSetting;
            if (!this.mHasVibrator) {
                i2 = 0;
            }
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(i3, 0, i2);
            updateRingerAndZenModeAffectedStreams();
            readDockAudioSettings(cr);
            sendEncodedSurroundMode(cr, "readPersistedSettings");
            sendEnabledSurroundFormats(cr, true);
            updateAssistantUId(true);
            updateRttEanbled(cr);
        }
        this.mMuteAffectedStreams = Settings.System.getIntForUser(cr, "mute_streams_affected", IElsaManager.TRANSACTION_ELSA_NOTIFY_APP_SWITCH, -2);
        updateMasterMono(cr);
        updateMasterBalance(cr);
        broadcastRingerMode("android.media.RINGER_MODE_CHANGED", this.mRingerModeExternal);
        broadcastRingerMode("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION", this.mRingerMode);
        broadcastVibrateSetting(0);
        broadcastVibrateSetting(1);
        this.mVolumeController.loadSettings(cr);
    }

    private void readUserRestrictions() {
        int currentUser = getCurrentUserId();
        boolean masterMute = this.mUserManagerInternal.getUserRestriction(currentUser, "disallow_unmute_device") || this.mUserManagerInternal.getUserRestriction(currentUser, "no_adjust_volume");
        if (this.mUseFixedVolume) {
            masterMute = false;
            AudioSystem.setMasterVolume(1.0f);
        }
        if (DEBUG_VOL) {
            Log.d(TAG, String.format("Master mute %s, user=%d", Boolean.valueOf(masterMute), Integer.valueOf(currentUser)));
        }
        setSystemAudioMute(masterMute);
        AudioSystem.setMasterMute(masterMute);
        broadcastMasterMuteStatus(masterMute);
        boolean microphoneMute = this.mUserManagerInternal.getUserRestriction(currentUser, "no_unmute_microphone");
        if (DEBUG_VOL) {
            Log.d(TAG, String.format("Mic mute %s, user=%d", Boolean.valueOf(microphoneMute), Integer.valueOf(currentUser)));
        }
        AudioSystem.muteMicrophone(microphoneMute);
    }

    /* access modifiers changed from: private */
    public int rescaleIndex(int index, int srcStream, int dstStream) {
        int rescaled = ((this.mStreamStates[dstStream].getMaxIndex() * index) + (this.mStreamStates[srcStream].getMaxIndex() / 2)) / this.mStreamStates[srcStream].getMaxIndex();
        if (rescaled < this.mStreamStates[dstStream].getMinIndex()) {
            return this.mStreamStates[dstStream].getMinIndex();
        }
        return rescaled;
    }

    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, String caller) {
        IAudioPolicyCallback extVolCtlr;
        synchronized (this.mExtVolumeControllerLock) {
            extVolCtlr = this.mExtVolumeController;
        }
        if (extVolCtlr != null) {
            sendMsg(this.mAudioHandler, 22, 2, direction, 0, extVolCtlr, 0);
        } else {
            adjustSuggestedStreamVolume(direction, suggestedStreamType, flags, callingPackage, caller, Binder.getCallingUid());
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00a0  */
    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, String caller, int uid) {
        int maybeActiveStreamType;
        int flags2;
        int flags3;
        int direction2;
        boolean activeForReal;
        if (DEBUG_VOL) {
            Log.d(TAG, "adjustSuggestedStreamVolume() stream=" + suggestedStreamType + ", flags=" + flags + ", caller=" + caller + ", volControlStream=" + this.mVolumeControlStream + ", userSelect=" + this.mUserSelectedVolumeControlStream);
        }
        if (direction != 0) {
            sVolumeLogger.log(new AudioServiceEvents.VolumeEvent(0, suggestedStreamType, direction, flags, callingPackage + SliceClientPermissions.SliceAuthority.DELIMITER + caller + " uid:" + uid));
        }
        this.mAdjustVolumeAction = true;
        synchronized (this.mForceControlStreamLock) {
            if (this.mUserSelectedVolumeControlStream) {
                maybeActiveStreamType = this.mVolumeControlStream;
            } else {
                maybeActiveStreamType = getActiveStreamType(suggestedStreamType);
                if (maybeActiveStreamType != 2) {
                    if (maybeActiveStreamType != 5) {
                        activeForReal = AudioSystem.isStreamActive(maybeActiveStreamType, 0);
                        if (!activeForReal) {
                            if (this.mVolumeControlStream != -1) {
                                maybeActiveStreamType = this.mVolumeControlStream;
                            }
                        }
                    }
                }
                activeForReal = wasStreamActiveRecently(maybeActiveStreamType, 0);
                if (!activeForReal) {
                }
            }
        }
        boolean isMute = isMuteAdjust(direction);
        ensureValidStreamType(maybeActiveStreamType);
        int resolvedStream = mStreamVolumeAlias[maybeActiveStreamType];
        if ((flags & 4) == 0 || resolvedStream == 2) {
            flags2 = flags;
        } else {
            flags2 = flags & -5;
        }
        if (!isRingVolumeDefault() || !this.mVolumeController.suppressAdjustment(resolvedStream, flags2, isMute) || this.mIsSingleVolume) {
            direction2 = direction;
            flags3 = flags2;
        } else {
            int flags4 = flags2 & -5 & -17;
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume controller suppressed adjustment");
            }
            flags3 = flags4;
            direction2 = 0;
        }
        adjustStreamVolume(maybeActiveStreamType, direction2, flags3, callingPackage, caller, uid);
        this.mAdjustVolumeAction = false;
    }

    public void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage) {
        if (streamType != 10 || canChangeAccessibilityVolume()) {
            sVolumeLogger.log(new AudioServiceEvents.VolumeEvent(1, streamType, direction, flags, callingPackage));
            adjustStreamVolume(streamType, direction, flags, callingPackage, callingPackage, Binder.getCallingUid());
            return;
        }
        Log.w(TAG, "Trying to call adjustStreamVolume() for a11y withoutCHANGE_ACCESSIBILITY_VOLUME / callingPackage=" + callingPackage);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x0382, code lost:
        r0 = th;
     */
    public void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage, String caller, int uid) {
        int uid2;
        int step;
        int aliasIndex;
        int device;
        int device2;
        int flags2;
        int oldIndex;
        int oldIndex2;
        int i;
        boolean z;
        boolean state;
        int step2;
        if (!this.mUseFixedVolume) {
            if (DEBUG_VOL) {
                Log.d(TAG, "adjustStreamVolume() stream=" + streamType + ", dir=" + direction + ", flags=" + flags + ", caller=" + caller);
            }
            ensureValidDirection(direction);
            ensureValidStreamType(streamType);
            boolean isMuteAdjust = isMuteAdjust(direction);
            if (isMuteAdjust && !isStreamAffectedByMute(streamType)) {
                return;
            }
            if (!isMuteAdjust || (!(streamType == 0 || streamType == 6) || this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") == 0)) {
                int streamTypeAlias = mStreamVolumeAlias[streamType];
                VolumeStreamState streamState = this.mStreamStates[streamTypeAlias];
                int device3 = getDeviceForStream(streamTypeAlias);
                int aliasIndex2 = streamState.getIndex(device3);
                boolean adjustVolume = true;
                if ((device3 & 896) != 0 || (flags & 64) == 0) {
                    if (uid == 1000) {
                        uid2 = UserHandle.getUid(getCurrentUserId(), UserHandle.getAppId(uid));
                    } else {
                        uid2 = uid;
                    }
                    if (this.mAppOps.noteOp(STREAM_VOLUME_OPS[streamTypeAlias], uid2, callingPackage) == 0) {
                        synchronized (this.mSafeMediaVolumeStateLock) {
                            this.mPendingVolumeCommand = null;
                        }
                        int flags3 = flags & -33;
                        if (streamTypeAlias != 3 || (this.mFixedVolumeDevices & device3) == 0) {
                            step = rescaleIndex(10, streamType, streamTypeAlias);
                            aliasIndex = aliasIndex2;
                        } else {
                            flags3 |= 32;
                            if (this.mSafeMediaVolumeState != 3 || (this.mSafeMediaVolumeDevices & device3) == 0) {
                                step2 = streamState.getMaxIndex();
                            } else {
                                step2 = safeMediaVolumeIndex(device3);
                            }
                            if (aliasIndex2 != 0) {
                                step = step2;
                                aliasIndex = step2;
                            } else {
                                step = step2;
                                aliasIndex = aliasIndex2;
                            }
                        }
                        if ((flags3 & 2) != 0 || streamTypeAlias == getUiSoundsStreamType()) {
                            if (getRingerModeInternal() == 1) {
                                flags3 &= -17;
                            }
                            device = device3;
                            int result = OppoCheckForRingerModeChange(aliasIndex, direction, step, streamState.mIsMuted, callingPackage, flags3);
                            adjustVolume = (result & 1) != 0;
                            if ((result & 128) != 0) {
                                flags3 |= 128;
                            }
                            if ((result & 2048) != 0) {
                                device2 = flags3 | 2048;
                            } else {
                                device2 = flags3;
                            }
                        } else {
                            device = device3;
                            device2 = flags3;
                        }
                        if (!volumeAdjustmentAllowedByDnd(streamTypeAlias, device2)) {
                            adjustVolume = false;
                        }
                        int oldIndex3 = this.mStreamStates[streamType].getIndex(device);
                        if (!adjustVolume || direction == 0) {
                            flags2 = device2;
                            oldIndex = oldIndex3;
                        } else {
                            this.mAudioHandler.removeMessages(18);
                            if (isMuteAdjust) {
                                if (direction == 101) {
                                    state = !streamState.mIsMuted;
                                } else {
                                    state = direction == -100;
                                }
                                if (streamTypeAlias == 3) {
                                    setSystemAudioMute(state);
                                }
                                for (int stream = 0; stream < this.mStreamStates.length; stream++) {
                                    if (streamTypeAlias == mStreamVolumeAlias[stream] && ((!readCameraSoundForced() || this.mStreamStates[stream].getStreamType() != 7) && this.mStreamStates[stream].getStreamType() != 4)) {
                                        this.mStreamStates[stream].mute(state);
                                    }
                                }
                                oldIndex2 = oldIndex3;
                                flags2 = device2;
                                i = 3;
                            } else if (direction == 1 && !checkSafeMediaVolume(streamTypeAlias, aliasIndex + step, device)) {
                                Log.e(TAG, "adjustStreamVolume() safe volume index = " + oldIndex3);
                                int tempflags = isNeedShowUiWarnings(device2, callingPackage);
                                if (!mDialogOn) {
                                    this.mVolumeController.postDisplaySafeVolumeWarning(tempflags);
                                }
                                oldIndex2 = oldIndex3;
                                flags2 = device2;
                                i = 3;
                            } else if ((this.mFullVolumeDevices & device) != 0) {
                                oldIndex2 = oldIndex3;
                                flags2 = device2;
                                i = 3;
                            } else if (streamState.adjustIndex(direction * step, device, caller) || streamState.mIsMuted) {
                                if (!streamState.mIsMuted) {
                                    oldIndex2 = oldIndex3;
                                    flags2 = device2;
                                    i = 3;
                                } else if (direction == 1) {
                                    streamState.mute(false);
                                    oldIndex2 = oldIndex3;
                                    flags2 = device2;
                                    i = 3;
                                } else if (direction != -1) {
                                    oldIndex2 = oldIndex3;
                                    flags2 = device2;
                                    i = 3;
                                } else if (this.mIsSingleVolume) {
                                    i = 3;
                                    oldIndex2 = oldIndex3;
                                    flags2 = device2;
                                    sendMsg(this.mAudioHandler, 18, 2, streamTypeAlias, device2, null, UNMUTE_STREAM_DELAY);
                                } else {
                                    oldIndex2 = oldIndex3;
                                    flags2 = device2;
                                    i = 3;
                                }
                                sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
                            } else {
                                oldIndex2 = oldIndex3;
                                flags2 = device2;
                                i = 3;
                            }
                            int newIndex = this.mStreamStates[streamType].getIndex(device);
                            if (streamTypeAlias == i && (device & 896) != 0 && (flags2 & 64) == 0) {
                                if (DEBUG_VOL) {
                                    Log.d(TAG, "adjustSreamVolume: postSetAvrcpAbsoluteVolumeIndex index=" + newIndex + "stream=" + streamType);
                                }
                                this.mDeviceBroker.postSetAvrcpAbsoluteVolumeIndex(newIndex / 10);
                            }
                            if ((134217728 & device) != 0 && streamType == getHearingAidStreamType()) {
                                if (DEBUG_VOL) {
                                    Log.d(TAG, "adjustSreamVolume postSetHearingAidVolumeIndex index=" + newIndex + " stream=" + streamType);
                                }
                                this.mDeviceBroker.postSetHearingAidVolumeIndex(newIndex, streamType);
                            }
                            if (streamTypeAlias == i) {
                                oldIndex = oldIndex2;
                                setSystemAudioVolume(oldIndex, newIndex, getStreamMaxVolume(streamType), flags2);
                            } else {
                                oldIndex = oldIndex2;
                            }
                            synchronized (this.mHdmiClientLock) {
                                try {
                                    if (this.mHdmiManager != null) {
                                        if (this.mHdmiCecSink && streamTypeAlias == i) {
                                            try {
                                                if ((this.mFullVolumeDevices & device) != 0) {
                                                    int keyCode = 0;
                                                    if (direction != -1) {
                                                        z = true;
                                                        if (direction == 1) {
                                                            keyCode = 24;
                                                        } else if (direction == 101) {
                                                            keyCode = 164;
                                                        }
                                                    } else {
                                                        z = true;
                                                        keyCode = 25;
                                                    }
                                                    if (keyCode != 0) {
                                                        long ident = Binder.clearCallingIdentity();
                                                        try {
                                                            this.mHdmiPlaybackClient.sendKeyEvent(keyCode, z);
                                                            this.mHdmiPlaybackClient.sendKeyEvent(keyCode, false);
                                                        } finally {
                                                            Binder.restoreCallingIdentity(ident);
                                                        }
                                                    }
                                                }
                                            } catch (Throwable th) {
                                                th = th;
                                                throw th;
                                            }
                                        }
                                        if (this.mHdmiAudioSystemClient != null && this.mHdmiSystemAudioSupported && streamTypeAlias == 3) {
                                            if (oldIndex != newIndex || isMuteAdjust) {
                                                long identity = Binder.clearCallingIdentity();
                                                this.mHdmiAudioSystemClient.sendReportAudioStatusCecCommand(isMuteAdjust, getStreamVolume(3), getStreamMaxVolume(3), isStreamMute(3));
                                                Binder.restoreCallingIdentity(identity);
                                            }
                                        }
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            }
                        }
                        sendVolumeUpdate(streamType, oldIndex, this.mStreamStates[streamType].getIndex(device), flags2, device);
                        return;
                    }
                    return;
                }
                return;
            }
            Log.w(TAG, "MODIFY_PHONE_STATE Permission Denial: adjustStreamVolume from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        return;
        while (true) {
        }
    }

    /* access modifiers changed from: private */
    public void onUnmuteStream(int stream, int flags) {
        this.mStreamStates[stream].mute(false);
        int device = getDeviceForStream(stream);
        int index = this.mStreamStates[stream].getIndex(device);
        sendVolumeUpdate(stream, index, index, flags, device);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x002b, code lost:
        return;
     */
    private void setSystemAudioVolume(int oldVolume, int newVolume, int maxVolume, int flags) {
        synchronized (this.mHdmiClientLock) {
            if (!(this.mHdmiManager == null || this.mHdmiTvClient == null || oldVolume == newVolume || (flags & 256) != 0)) {
                if (this.mHdmiSystemAudioSupported) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        this.mHdmiTvClient.setSystemAudioVolume(oldVolume, newVolume, maxVolume);
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
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

    private int getNewRingerMode(int stream, int index, int flags) {
        int newRingerMode;
        if (this.mIsSingleVolume) {
            return getRingerModeExternal();
        }
        if ((flags & 2) == 0 && stream != getUiSoundsStreamType()) {
            return getRingerModeExternal();
        }
        if (index != 0) {
            return 2;
        }
        if (this.mHasVibrator) {
            newRingerMode = 1;
        } else if (this.mVolumePolicy.volumeDownToEnterSilent) {
            newRingerMode = 0;
        } else {
            newRingerMode = 2;
        }
        if (newRingerMode == 2) {
            return newRingerMode;
        }
        if (isVibrateInRingSilentMode()) {
            return 1;
        }
        return 0;
    }

    private boolean isAndroidNPlus(String caller) {
        try {
            if (this.mContext.getPackageManager().getApplicationInfoAsUser(caller, 0, UserHandle.getUserId(Binder.getCallingUid())).targetSdkVersion >= 24) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    private boolean wouldToggleZenMode(int newMode) {
        if (getRingerModeExternal() == 0 && newMode != 0) {
            return true;
        }
        if (getRingerModeExternal() == 0 || newMode != 0) {
            return false;
        }
        return true;
    }

    private void onSetStreamVolume(int streamType, int index, int flags, int device, String caller) {
        int stream = mStreamVolumeAlias[streamType];
        setStreamVolumeInt(stream, index, device, false, caller);
        boolean z = false;
        if ((flags & 2) != 0 || stream == getUiSoundsStreamType()) {
            setRingerMode(getNewRingerMode(stream, index, flags), "AS.AudioService.onSetStreamVolume", false);
        }
        if (streamType == 6) {
            return;
        }
        if ((stream != 4 && stream != 3) || index != 0) {
            VolumeStreamState volumeStreamState = this.mStreamStates[stream];
            if (index == 0) {
                z = true;
            }
            volumeStreamState.mute(z);
        }
    }

    private void enforceModifyAudioRoutingPermission() {
        if (this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") != 0) {
            throw new SecurityException("Missing MODIFY_AUDIO_ROUTING permission");
        }
    }

    public void setVolumeIndexForAttributes(AudioAttributes attr, int index, int flags, String callingPackage) {
        enforceModifyAudioRoutingPermission();
        Preconditions.checkNotNull(attr, "attr must not be null");
        int device = getDeviceForStream(AudioProductStrategy.getLegacyStreamTypeForStrategyWithAudioAttributes(attr));
        AudioSystem.getVolumeIndexForAttributes(attr, device);
        AudioSystem.setVolumeIndexForAttributes(attr, index, device);
        AudioVolumeGroup avg = getAudioVolumeGroupById(getVolumeGroupIdForAttributes(attr));
        if (avg != null) {
            int[] legacyStreamTypes = avg.getLegacyStreamTypes();
            int i = 0;
            for (int length = legacyStreamTypes.length; i < length; length = length) {
                setStreamVolume(legacyStreamTypes[i], index, flags, callingPackage, callingPackage, Binder.getCallingUid());
                i++;
            }
        }
    }

    private AudioVolumeGroup getAudioVolumeGroupById(int volumeGroupId) {
        for (AudioVolumeGroup avg : AudioVolumeGroup.getAudioVolumeGroups()) {
            if (avg.getId() == volumeGroupId) {
                return avg;
            }
        }
        Log.e(TAG, ": invalid volume group id: " + volumeGroupId + " requested");
        return null;
    }

    public int getVolumeIndexForAttributes(AudioAttributes attr) {
        enforceModifyAudioRoutingPermission();
        Preconditions.checkNotNull(attr, "attr must not be null");
        return AudioSystem.getVolumeIndexForAttributes(attr, getDeviceForStream(AudioProductStrategy.getLegacyStreamTypeForStrategyWithAudioAttributes(attr)));
    }

    public int getMaxVolumeIndexForAttributes(AudioAttributes attr) {
        enforceModifyAudioRoutingPermission();
        Preconditions.checkNotNull(attr, "attr must not be null");
        return AudioSystem.getMaxVolumeIndexForAttributes(attr);
    }

    public int getMinVolumeIndexForAttributes(AudioAttributes attr) {
        enforceModifyAudioRoutingPermission();
        Preconditions.checkNotNull(attr, "attr must not be null");
        return AudioSystem.getMinVolumeIndexForAttributes(attr);
    }

    public void setStreamVolume(int streamType, int index, int flags, String callingPackage) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(84, Binder.getCallingUid());
        if (streamType == 10 && !canChangeAccessibilityVolume()) {
            Log.w(TAG, "Trying to call setStreamVolume() for a11y without CHANGE_ACCESSIBILITY_VOLUME  callingPackage=" + callingPackage);
        } else if (streamType == 0 && index == 0 && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            Log.w(TAG, "Trying to call setStreamVolume() for STREAM_VOICE_CALL and index 0 without MODIFY_PHONE_STATE  callingPackage=" + callingPackage);
        } else {
            sVolumeLogger.log(new AudioServiceEvents.VolumeEvent(2, streamType, index, flags, callingPackage));
            setStreamVolume(streamType, index, flags, callingPackage, callingPackage, Binder.getCallingUid());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002b, code lost:
        return false;
     */
    private boolean canChangeAccessibilityVolume() {
        synchronized (this.mAccessibilityServiceUidsLock) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.CHANGE_ACCESSIBILITY_VOLUME") == 0) {
                return true;
            }
            if (this.mAccessibilityServiceUids != null) {
                int callingUid = Binder.getCallingUid();
                for (int i = 0; i < this.mAccessibilityServiceUids.length; i++) {
                    if (this.mAccessibilityServiceUids[i] == callingUid) {
                        return true;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getHearingAidStreamType() {
        return getHearingAidStreamType(this.mMode);
    }

    private int getHearingAidStreamType(int mode) {
        return (mode == 2 || mode == 3 || this.mVoiceActive.get()) ? 0 : 3;
    }

    /* access modifiers changed from: private */
    public void onPlaybackConfigChange(List<AudioPlaybackConfiguration> configs) {
        boolean voiceActive = false;
        Iterator<AudioPlaybackConfiguration> it = configs.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            AudioPlaybackConfiguration config = it.next();
            int usage = config.getAudioAttributes().getUsage();
            if ((usage == 2 || usage == 3) && config.getPlayerState() == 2) {
                voiceActive = true;
                break;
            }
        }
        if (this.mVoiceActive.getAndSet(voiceActive) != voiceActive) {
            updateHearingAidVolumeOnVoiceActivityUpdate();
        }
    }

    private void updateHearingAidVolumeOnVoiceActivityUpdate() {
        int streamType = getHearingAidStreamType();
        int index = getStreamVolume(streamType);
        sVolumeLogger.log(new AudioServiceEvents.VolumeEvent(6, this.mVoiceActive.get(), streamType, index));
        this.mDeviceBroker.postSetHearingAidVolumeIndex(index * 10, streamType);
    }

    /* access modifiers changed from: package-private */
    public void updateAbsVolumeMultiModeDevices(int oldMode, int newMode) {
        if (oldMode != newMode) {
            if (newMode != 0) {
                if (newMode == 1) {
                    return;
                }
                if (!(newMode == 2 || newMode == 3)) {
                    return;
                }
            }
            int streamType = getHearingAidStreamType(newMode);
            int device = AudioSystem.getDevicesForStream(streamType);
            int i = this.mAbsVolumeMultiModeCaseDevices;
            if ((device & i) != 0 && (i & device) == 134217728) {
                int index = getStreamVolume(streamType);
                sVolumeLogger.log(new AudioServiceEvents.VolumeEvent(7, newMode, streamType, index));
                this.mDeviceBroker.postSetHearingAidVolumeIndex(index * 10, streamType);
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x023d, code lost:
        r1 = r19.mHdmiClientLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x023f, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x0242, code lost:
        if (r19.mHdmiManager == null) goto L_0x0278;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x0246, code lost:
        if (r19.mHdmiAudioSystemClient == null) goto L_0x0278;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x024a, code lost:
        if (r19.mHdmiSystemAudioSupported == false) goto L_0x0278;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x024d, code lost:
        if (r10 != 3) goto L_0x0278;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x024f, code lost:
        if (r0 == r6) goto L_0x0278;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x0251, code lost:
        r2 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:?, code lost:
        r19.mHdmiAudioSystemClient.sendReportAudioStatusCecCommand(false, getStreamVolume(3), getStreamMaxVolume(3), isStreamMute(3));
        android.os.Binder.restoreCallingIdentity(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x026e, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x0272, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:?, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x027c, code lost:
        if (r20 != 3) goto L_0x028a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x0284, code lost:
        if (r23.equals("com.tencent.mm") == false) goto L_0x028a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x0286, code lost:
        r16 = r16 & -5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x028a, code lost:
        sendVolumeUpdate(r20, r0, r6, r16, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0298, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x0299, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x029d, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:?, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x02a3, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x02a4, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0223  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01bc  */
    public void setStreamVolume(int streamType, int index, int flags, String callingPackage, String caller, int uid) {
        int index2;
        int uid2;
        int flags2;
        int index3;
        int index4;
        int tempflags;
        if (DEBUG_VOL) {
            Log.d(TAG, "setStreamVolume(stream=" + streamType + ", index=" + index + ", calling=" + callingPackage + ")");
        }
        if (!this.mUseFixedVolume) {
            if (index < 0) {
                index2 = (this.mStreamStates[streamType].getMinIndex() + 5) / 10;
            } else {
                index2 = index;
            }
            ensureValidStreamType(streamType);
            int streamTypeAlias = mStreamVolumeAlias[streamType];
            VolumeStreamState streamState = this.mStreamStates[streamTypeAlias];
            int device = getDeviceForStream(streamType);
            if (streamType == 6 && (device & HdmiCecKeycode.UI_BROADCAST_DIGITAL_CABLE) == 0 && callingPackage.equals("com.android.bluetooth")) {
                if (DEBUG_VOL) {
                    Log.d(TAG, "getDeviceForStream device :" + device);
                }
                device = this.mDeviceBroker.getCurrentConnectedScoDevices();
                if (DEBUG_VOL) {
                    Log.d(TAG, "getCurrentConnectedScoDevices :" + device);
                }
            }
            if ((device & 896) != 0 || (flags & 64) == 0) {
                if (uid == 1000) {
                    uid2 = UserHandle.getUid(getCurrentUserId(), UserHandle.getAppId(uid));
                } else {
                    uid2 = uid;
                }
                if (this.mAppOps.noteOpNoThrow(STREAM_VOLUME_OPS[streamTypeAlias], uid2, callingPackage) == 0) {
                    if (isAndroidNPlus(callingPackage) && wouldToggleZenMode(getNewRingerMode(streamTypeAlias, index2, flags)) && !this.mNm.isNotificationPolicyAccessGrantedForPackage(callingPackage)) {
                        throw new SecurityException("Not allowed to change Do Not Disturb state");
                    } else if (volumeAdjustmentAllowedByDnd(streamTypeAlias, flags)) {
                        synchronized (this.mSafeMediaVolumeStateLock) {
                            try {
                                this.mPendingVolumeCommand = null;
                                int oldIndex = streamState.getIndex(device);
                                int index5 = rescaleIndex(index2 * 10, streamType, streamTypeAlias);
                                if (streamTypeAlias == 3 && (device & 896) != 0 && (flags & 64) == 0) {
                                    try {
                                        if (DEBUG_VOL) {
                                            Log.d(TAG, "setStreamVolume postSetAvrcpAbsoluteVolumeIndex index=" + index5 + "stream=" + streamType);
                                        }
                                        this.mDeviceBroker.postSetAvrcpAbsoluteVolumeIndex(index5 / 10);
                                    } catch (Throwable th) {
                                        th = th;
                                        while (true) {
                                            try {
                                                break;
                                            } catch (Throwable th2) {
                                                th = th2;
                                            }
                                        }
                                        throw th;
                                    }
                                }
                                if ((134217728 & device) != 0 && streamType == getHearingAidStreamType()) {
                                    Log.i(TAG, "setStreamVolume postSetHearingAidVolumeIndex index=" + index5 + " stream=" + streamType);
                                    this.mDeviceBroker.postSetHearingAidVolumeIndex(index5, streamType);
                                }
                                if (streamTypeAlias == 3) {
                                    setSystemAudioVolume(oldIndex, index5, getStreamMaxVolume(streamType), flags);
                                }
                                int flags3 = flags & -33;
                                if (streamTypeAlias == 3) {
                                    try {
                                        if ((this.mFixedVolumeDevices & device) != 0) {
                                            int flags4 = flags3 | 32;
                                            if (index5 == 0) {
                                                flags2 = flags4;
                                                index3 = index5;
                                            } else if (this.mSafeMediaVolumeState != 3 || (this.mSafeMediaVolumeDevices & device) == 0) {
                                                flags2 = flags4;
                                                index3 = streamState.getMaxIndex();
                                            } else {
                                                flags2 = flags4;
                                                index3 = safeMediaVolumeIndex(device);
                                            }
                                            if (checkSafeMediaVolume(streamTypeAlias, index3, device)) {
                                                if ((flags2 & 1) == 0 && (flags2 & 1024) == 0) {
                                                    try {
                                                        if (!callingPackage.equals("com.oppo.engineermode")) {
                                                            Log.d(TAG, "setStreamVolume postDisplaySafeVolumeWarning flags = " + flags2);
                                                            tempflags = flags2 | 1024;
                                                            this.mVolumeController.postDisplaySafeVolumeWarning(tempflags);
                                                            this.mPendingVolumeCommand = new StreamVolumeCommand(streamType, index3, flags2, device);
                                                            index4 = index3;
                                                        }
                                                    } catch (Throwable th3) {
                                                        th = th3;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                }
                                                tempflags = flags2;
                                                try {
                                                    this.mVolumeController.postDisplaySafeVolumeWarning(tempflags);
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                                try {
                                                    this.mPendingVolumeCommand = new StreamVolumeCommand(streamType, index3, flags2, device);
                                                    index4 = index3;
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            } else {
                                                try {
                                                    onSetStreamVolume(streamType, index3, flags2, device, caller);
                                                    index4 = this.mStreamStates[streamType].getIndex(device);
                                                } catch (Throwable th6) {
                                                    th = th6;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            }
                                        }
                                    } catch (Throwable th7) {
                                        th = th7;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                                flags2 = flags3;
                                index3 = index5;
                                try {
                                    if (checkSafeMediaVolume(streamTypeAlias, index3, device)) {
                                    }
                                } catch (Throwable th8) {
                                    th = th8;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th9) {
                                th = th9;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                    }
                }
            }
        }
    }

    private int getVolumeGroupIdForAttributes(AudioAttributes attributes) {
        Preconditions.checkNotNull(attributes, "attributes must not be null");
        int volumeGroupId = getVolumeGroupIdForAttributesInt(attributes);
        if (volumeGroupId != -1) {
            return volumeGroupId;
        }
        return getVolumeGroupIdForAttributesInt(AudioProductStrategy.sDefaultAttributes);
    }

    private int getVolumeGroupIdForAttributesInt(AudioAttributes attributes) {
        Preconditions.checkNotNull(attributes, "attributes must not be null");
        for (AudioProductStrategy productStrategy : AudioProductStrategy.getAudioProductStrategies()) {
            int volumeGroupId = productStrategy.getVolumeGroupIdForAudioAttributes(attributes);
            if (volumeGroupId != -1) {
                return volumeGroupId;
            }
        }
        return -1;
    }

    private boolean volumeAdjustmentAllowedByDnd(int streamTypeAlias, int flags) {
        int zenMode = this.mNm.getZenMode();
        if (zenMode == 0) {
            return true;
        }
        if ((zenMode == 1 || zenMode == 2 || zenMode == 3) && isStreamMutedByRingerOrZenMode(streamTypeAlias) && streamTypeAlias != getUiSoundsStreamType() && (flags & 2) == 0) {
            return false;
        }
        return true;
    }

    public void forceVolumeControlStream(int streamType, IBinder cb) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") == 0) {
            if (DEBUG_VOL) {
                Log.d(TAG, String.format("forceVolumeControlStream(%d)", Integer.valueOf(streamType)));
            }
            synchronized (this.mForceControlStreamLock) {
                if (!(this.mVolumeControlStream == -1 || streamType == -1)) {
                    this.mUserSelectedVolumeControlStream = true;
                }
                this.mVolumeControlStream = streamType;
                if (this.mVolumeControlStream == -1) {
                    if (this.mForceControlStreamClient != null) {
                        this.mForceControlStreamClient.release();
                        this.mForceControlStreamClient = null;
                    }
                    this.mUserSelectedVolumeControlStream = false;
                } else if (this.mForceControlStreamClient == null) {
                    this.mForceControlStreamClient = new ForceControlStreamClient(cb);
                } else if (this.mForceControlStreamClient.getBinder() == cb) {
                    Log.d(TAG, "forceVolumeControlStream cb:" + cb + " is already linked.");
                } else {
                    this.mForceControlStreamClient.release();
                    this.mForceControlStreamClient = new ForceControlStreamClient(cb);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class ForceControlStreamClient implements IBinder.DeathRecipient {
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
                    ForceControlStreamClient unused = AudioService.this.mForceControlStreamClient = null;
                    int unused2 = AudioService.this.mVolumeControlStream = -1;
                    boolean unused3 = AudioService.this.mUserSelectedVolumeControlStream = false;
                }
            }
        }

        public void release() {
            IBinder iBinder = this.mCb;
            if (iBinder != null) {
                iBinder.unlinkToDeath(this, 0);
                this.mCb = null;
            }
        }

        public IBinder getBinder() {
            return this.mCb;
        }
    }

    /* access modifiers changed from: private */
    public void sendBroadcastToAll(Intent intent) {
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

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    public int getCurrentUserId() {
        long ident = Binder.clearCallingIdentity();
        try {
            int i = ActivityManager.getService().getCurrentUser().id;
            Binder.restoreCallingIdentity(ident);
            return i;
        } catch (RemoteException e) {
            Binder.restoreCallingIdentity(ident);
            return 0;
        } catch (Throwable currentUser) {
            Binder.restoreCallingIdentity(ident);
            throw currentUser;
        }
    }

    /* access modifiers changed from: protected */
    public void sendVolumeUpdate(int streamType, int oldIndex, int index, int flags, int device) {
        int streamType2 = mStreamVolumeAlias[streamType];
        if (streamType2 == 3) {
            flags = updateFlagsForTvPlatform(flags);
            if ((this.mFullVolumeDevices & device) != 0) {
                flags &= -2;
            }
        }
        this.mVolumeController.postVolumeChanged(streamType2, flags);
    }

    private int updateFlagsForTvPlatform(int flags) {
        synchronized (this.mHdmiClientLock) {
            if (this.mHdmiTvClient != null && this.mHdmiSystemAudioSupported && (flags & 256) == 0) {
                flags &= -2;
            }
        }
        return flags;
    }

    private void sendMasterMuteUpdate(boolean muted, int flags) {
        this.mVolumeController.postMasterMuteChanged(updateFlagsForTvPlatform(flags));
        broadcastMasterMuteStatus(muted);
    }

    private void broadcastMasterMuteStatus(boolean muted) {
        Intent intent = new Intent("android.media.MASTER_MUTE_CHANGED_ACTION");
        intent.putExtra("android.media.EXTRA_MASTER_VOLUME_MUTED", muted);
        intent.addFlags(603979776);
        sendStickyBroadcastToAll(intent);
    }

    private void setStreamVolumeInt(int streamType, int index, int device, boolean force, String caller) {
        if ((this.mFullVolumeDevices & device) == 0) {
            VolumeStreamState streamState = this.mStreamStates[streamType];
            if (streamState.setIndex(index, device, caller) || force) {
                sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0025, code lost:
        return;
     */
    private void setSystemAudioMute(boolean state) {
        synchronized (this.mHdmiClientLock) {
            if (!(this.mHdmiManager == null || this.mHdmiTvClient == null)) {
                if (this.mHdmiSystemAudioSupported) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        this.mHdmiTvClient.setSystemAudioMute(state);
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            }
        }
    }

    public boolean isStreamMute(int streamType) {
        boolean access$1000;
        if (streamType == Integer.MIN_VALUE) {
            streamType = getActiveStreamType(streamType);
        }
        synchronized (VolumeStreamState.class) {
            ensureValidStreamType(streamType);
            access$1000 = this.mStreamStates[streamType].mIsMuted;
        }
        return access$1000;
    }

    private class RmtSbmxFullVolDeathHandler implements IBinder.DeathRecipient {
        private IBinder mICallback;

        RmtSbmxFullVolDeathHandler(IBinder cb) {
            this.mICallback = cb;
            try {
                cb.linkToDeath(this, 0);
            } catch (RemoteException e) {
                Log.e(AudioService.TAG, "can't link to death", e);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isHandlerFor(IBinder cb) {
            return this.mICallback.equals(cb);
        }

        /* access modifiers changed from: package-private */
        public void forget() {
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

    private boolean discardRmtSbmxFullVolDeathHandlerFor(IBinder cb) {
        Iterator<RmtSbmxFullVolDeathHandler> it = this.mRmtSbmxFullVolDeathHandlers.iterator();
        while (it.hasNext()) {
            RmtSbmxFullVolDeathHandler handler = it.next();
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
            if (it.next().isHandlerFor(cb)) {
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

    /* access modifiers changed from: private */
    public void setMasterMuteInternalNoCallerCheck(boolean mute, int flags, int userId) {
        if (DEBUG_VOL) {
            Log.d(TAG, String.format("Master mute %s, %d, user=%d", Boolean.valueOf(mute), Integer.valueOf(flags), Integer.valueOf(userId)));
        }
        if (!isPlatformAutomotive() && this.mUseFixedVolume) {
            return;
        }
        if (((isPlatformAutomotive() && userId == 0) || getCurrentUserId() == userId) && mute != AudioSystem.getMasterMute()) {
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
            if (!(index == 0 || mStreamVolumeAlias[streamType] != 3 || (this.mFixedVolumeDevices & device) == 0)) {
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
            if (!(index == 0 || mStreamVolumeAlias[streamType] != 3 || (this.mFixedVolumeDevices & device) == 0)) {
                index = this.mStreamStates[streamType].getMaxIndex();
            }
            i = (index + 5) / 10;
        }
        return i;
    }

    public int getOppoStreamVolume(int streamType, String callingPackage) {
        int i;
        String state;
        ensureValidStreamType(streamType);
        int device = getDeviceForStream(streamType);
        if (streamType == 6 && (device & HdmiCecKeycode.UI_BROADCAST_DIGITAL_CABLE) == 0 && callingPackage.equals("com.android.bluetooth")) {
            device = 32;
        }
        synchronized (VolumeStreamState.class) {
            int index = this.mStreamStates[streamType].getIndex(device);
            if (this.mStreamStates[streamType].mIsMuted && callingPackage.equals("com.android.bluetooth") && ((state = OppoAtlasManager.getInstance((Context) null).getParameters("get_do_mute_music")) == null || state.equalsIgnoreCase(TemperatureProvider.SWITCH_OFF) || getMode() != 2)) {
                index = 0;
            }
            if (!(index == 0 || mStreamVolumeAlias[streamType] != 3 || (this.mFixedVolumeDevices & device) == 0)) {
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
        return mStreamVolumeAlias[1];
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

    /* access modifiers changed from: private */
    public void setMicrophoneMuteNoCallerCheck(boolean on, int userId) {
        if (DEBUG_VOL) {
            Log.d(TAG, String.format("Mic mute %s, user=%d", Boolean.valueOf(on), Integer.valueOf(userId)));
        }
        if (getCurrentUserId() == userId) {
            boolean currentMute = AudioSystem.isMicrophoneMuted();
            long identity = Binder.clearCallingIdentity();
            AudioSystem.muteMicrophone(on);
            Binder.restoreCallingIdentity(identity);
            if (on != currentMute) {
                this.mContext.sendBroadcast(new Intent("android.media.action.MICROPHONE_MUTE_CHANGED").setFlags(1073741824));
            }
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
        if (!isAndroidNPlus(caller) || !wouldToggleZenMode(ringerMode) || this.mNm.isNotificationPolicyAccessGrantedForPackage(caller)) {
            setRingerMode(ringerMode, caller, true);
            return;
        }
        throw new SecurityException("Not allowed to change Do Not Disturb state");
    }

    public void setRingerModeInternal(int ringerMode, String caller) {
        enforceVolumeController("setRingerModeInternal");
        setRingerMode(ringerMode, caller, false);
    }

    public void silenceRingerModeInternal(String reason) {
        VibrationEffect effect = null;
        int ringerMode = 0;
        int toastText = 0;
        int silenceRingerSetting = 0;
        if (this.mContext.getResources().getBoolean(17891572)) {
            silenceRingerSetting = Settings.Secure.getIntForUser(this.mContentResolver, "volume_hush_gesture", 0, -2);
        }
        if (silenceRingerSetting == 1) {
            effect = VibrationEffect.get(5);
            ringerMode = 1;
            toastText = 17041196;
        } else if (silenceRingerSetting == 2) {
            effect = VibrationEffect.get(1);
            ringerMode = 0;
            toastText = 17041195;
        }
        maybeVibrate(effect, reason);
        setRingerModeInternal(ringerMode, reason);
        Toast.makeText(this.mContext, toastText, 0).show();
    }

    private boolean maybeVibrate(VibrationEffect effect, String reason) {
        if (!this.mHasVibrator) {
            return false;
        }
        if ((Settings.System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", 0, -2) == 0) || effect == null) {
            return false;
        }
        this.mVibrator.vibrate(Binder.getCallingUid(), this.mContext.getOpPackageName(), effect, reason, VIBRATION_ATTRIBUTES);
        return true;
    }

    private void setRingerMode(int ringerMode, String caller, boolean external) {
        int ringerMode2;
        if (this.mUseFixedVolume) {
            return;
        }
        if (!this.mIsSingleVolume) {
            if (caller == null || caller.length() == 0) {
                throw new IllegalArgumentException("Bad caller: " + caller);
            }
            ensureValidRingerMode(ringerMode);
            if (ringerMode != 1 || this.mHasVibrator) {
                ringerMode2 = ringerMode;
            } else {
                ringerMode2 = 0;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (this.mSettingsLock) {
                    int ringerModeInternal = getRingerModeInternal();
                    int ringerModeExternal = getRingerModeExternal();
                    if (external) {
                        setRingerModeExt(ringerMode2);
                        if (this.mRingerModeDelegate != null) {
                            ringerMode2 = this.mRingerModeDelegate.onSetRingerModeExternal(ringerModeExternal, ringerMode2, caller, ringerModeInternal, this.mVolumePolicy);
                        }
                        if (ringerMode2 != ringerModeInternal) {
                            setRingerModeInt(ringerMode2, true);
                        }
                    } else {
                        if (ringerMode2 != ringerModeInternal) {
                            setRingerModeInt(ringerMode2, true);
                        }
                        if (this.mRingerModeDelegate != null) {
                            ringerMode2 = this.mRingerModeDelegate.onSetRingerModeInternal(ringerModeInternal, ringerMode2, caller, ringerModeExternal, this.mVolumePolicy);
                        }
                        setRingerModeExt(ringerMode2);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private void setRingerModeExt(int ringerMode) {
        synchronized (this.mSettingsLock) {
            if (ringerMode != this.mRingerModeExternal) {
                this.mRingerModeExternal = ringerMode;
                broadcastRingerMode("android.media.RINGER_MODE_CHANGED", ringerMode);
            }
        }
    }

    /* JADX WARN: Failed to insert an additional move for type inference into block B:65:0x0117 */
    /* JADX DEBUG: Additional 1 move instruction added to help type inference */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: com.android.server.audio.AudioService$VolumeStreamState[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: com.android.server.audio.AudioService$VolumeStreamState} */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v0 */
    /* JADX WARN: Type inference failed for: r4v1, types: [int, boolean] */
    /* JADX WARN: Type inference failed for: r4v2 */
    /* JADX WARN: Type inference failed for: r4v3 */
    /* JADX WARN: Type inference failed for: r4v7 */
    @GuardedBy({"mSettingsLock"})
    private void muteRingerModeStreams() {
        int numStreamTypes;
        int numStreamTypes2;
        int numStreamTypes3;
        int numStreamTypes4 = AudioSystem.getNumStreamTypes();
        if (this.mNm == null) {
            this.mNm = (NotificationManager) this.mContext.getSystemService("notification");
        }
        int ringerMode = this.mRingerMode;
        int i = 0;
        ? r4 = 1;
        boolean ringerModeMute = ringerMode == 1 || ringerMode == 0;
        boolean shouldRingSco = ringerMode == 1 && isBluetoothScoOn();
        sendMsg(this.mAudioHandler, 8, 2, 7, shouldRingSco ? 3 : 0, "muteRingerModeStreams() from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid(), 0);
        int streamType = numStreamTypes4 - 1;
        while (streamType >= 0) {
            boolean isMuted = isStreamMutedByRingerOrZenMode(streamType);
            int i2 = (shouldZenMuteStream(streamType) || (ringerModeMute && isStreamAffectedByRingerMode(streamType) && ((!shouldRingSco || streamType != 2) ? r4 : i) != 0)) ? r4 : i;
            if (isMuted == i2) {
                numStreamTypes = numStreamTypes4;
                numStreamTypes2 = i;
            } else if (i2 == 0) {
                if (mStreamVolumeAlias[streamType] == 2) {
                    synchronized (VolumeStreamState.class) {
                        try {
                            VolumeStreamState vss = this.mStreamStates[streamType];
                            int i3 = i;
                            while (i3 < vss.mIndexMap.size()) {
                                int device = vss.mIndexMap.keyAt(i3);
                                if (vss.mIndexMap.valueAt(i3) == 0) {
                                    numStreamTypes3 = numStreamTypes4;
                                    vss.setIndex(10, device, TAG);
                                } else {
                                    numStreamTypes3 = numStreamTypes4;
                                }
                                i3++;
                                numStreamTypes4 = numStreamTypes3;
                            }
                            numStreamTypes = numStreamTypes4;
                            sendMsg(this.mAudioHandler, 1, 2, getDeviceForStream(streamType), 0, this.mStreamStates[streamType], 500);
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } else {
                    numStreamTypes = numStreamTypes4;
                }
                numStreamTypes2 = 0;
                this.mStreamStates[streamType].mute(false);
                r4 = 1;
                this.mRingerAndZenModeMutedStreams &= ~(1 << streamType);
            } else {
                numStreamTypes = numStreamTypes4;
                numStreamTypes2 = i;
                this.mStreamStates[streamType].mute(r4);
                this.mRingerAndZenModeMutedStreams |= r4 << streamType;
            }
            streamType--;
            i = numStreamTypes2;
            numStreamTypes4 = numStreamTypes;
            r4 = r4;
        }
    }

    private boolean isAlarm(int streamType) {
        return streamType == 4;
    }

    private boolean isNotificationOrRinger(int streamType) {
        return streamType == 5 || streamType == 2;
    }

    private boolean isMedia(int streamType) {
        return streamType == 3;
    }

    private boolean isSystem(int streamType) {
        return streamType == 1;
    }

    /* access modifiers changed from: private */
    public void setRingerModeInt(int ringerMode, boolean persist) {
        boolean change;
        synchronized (this.mSettingsLock) {
            change = this.mRingerMode != ringerMode;
            this.mRingerMode = ringerMode;
            muteRingerModeStreams();
        }
        if (persist) {
            sendMsg(this.mAudioHandler, 3, 0, 0, 0, null, 500);
        }
        if (change) {
            broadcastRingerMode("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION", ringerMode);
        }
    }

    /* access modifiers changed from: package-private */
    public void postUpdateRingerModeServiceInt() {
        sendMsg(this.mAudioHandler, 25, 2, 0, 0, null, 0);
    }

    /* access modifiers changed from: private */
    public void onUpdateRingerModeServiceInt() {
        setRingerModeInt(getRingerModeInternal(), false);
    }

    public boolean shouldVibrate(int vibrateType) {
        int vibrateSetting;
        if (!this.mHasVibrator || (vibrateSetting = getVibrateSetting(vibrateType)) == 0) {
            return false;
        }
        if (vibrateSetting != 1) {
            if (vibrateSetting == 2 && getRingerModeExternal() == 1) {
                return true;
            }
            return false;
        } else if (getRingerModeExternal() != 0) {
            return true;
        } else {
            return false;
        }
    }

    public int getVibrateSetting(int vibrateType) {
        if (!this.mHasVibrator) {
            return 0;
        }
        return (this.mVibrateSetting >> (vibrateType * 2)) & 3;
    }

    public void setVibrateSetting(int vibrateType, int vibrateSetting) {
        if (this.mHasVibrator) {
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(this.mVibrateSetting, vibrateType, vibrateSetting);
            broadcastVibrateSetting(vibrateType);
        }
    }

    /* access modifiers changed from: package-private */
    public int getModeOwnerPid() {
        try {
            return this.mSetModeDeathHandlers.get(0).getPid();
        } catch (Exception e) {
            return 0;
        }
    }

    private class SetModeDeathHandler implements IBinder.DeathRecipient {
        private IBinder mCb;
        private int mMode = 0;
        /* access modifiers changed from: private */
        public int mPid;

        SetModeDeathHandler(IBinder cb, int pid) {
            this.mCb = cb;
            this.mPid = pid;
        }

        public void binderDied() {
            int oldModeOwnerPid = 0;
            int newModeOwnerPid = 0;
            synchronized (AudioService.this.mDeviceBroker.mSetModeLock) {
                Log.w(AudioService.TAG, "setMode() client died");
                if (!AudioService.this.mSetModeDeathHandlers.isEmpty()) {
                    oldModeOwnerPid = AudioService.this.mSetModeDeathHandlers.get(0).getPid();
                }
                if (AudioService.this.mSetModeDeathHandlers.indexOf(this) < 0) {
                    Log.w(AudioService.TAG, "unregistered setMode() client died");
                } else {
                    newModeOwnerPid = AudioService.this.setModeInt(0, this.mCb, this.mPid, AudioService.TAG);
                }
            }
            if (newModeOwnerPid != oldModeOwnerPid && newModeOwnerPid != 0) {
                AudioService.this.mDeviceBroker.postDisconnectBluetoothSco(newModeOwnerPid);
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

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0057, code lost:
        return false;
     */
    private boolean isInvalidMode(int mode, String callingPackage) {
        if (this.mMode == 2 && mode == 3) {
            OppoAtlasManager.getInstance(this.mContext).setEvent((int) UsbTerminalTypes.TERMINAL_USB_STREAMING, "EventID,2,packageName," + callingPackage);
            return true;
        }
        synchronized (this.mSettingCallLock) {
            if (mode == 2) {
                this.mIsInSetCallMode = true;
            }
            if (this.mIsInSetCallMode && mode == 3) {
                Log.w(TAG, "Phone is in set Call mode, denial set communication mode.");
                OppoAtlasManager.getInstance(this.mContext).setEvent((int) UsbTerminalTypes.TERMINAL_USB_STREAMING, "EventID,2,packageName," + callingPackage);
                return true;
            }
        }
    }

    public void setMode(int mode, IBinder cb, String callingPackage) {
        int newModeOwnerPid;
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(13, Binder.getCallingUid());
        if (DEBUG_MODE) {
            Log.v(TAG, "setMode(mode=" + mode + "[" + AudioSystem.modeToString(mode) + "], callingPackage=" + callingPackage + ")");
        }
        if (checkAudioSettingsPermission("setMode()")) {
            if (mode == 2 && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
                Log.w(TAG, "MODIFY_PHONE_STATE Permission Denial: setMode(MODE_IN_CALL) from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            } else if (mode >= -1 && mode < 4) {
                String value = OppoAtlasManager.getInstance((Context) null).getParameters("check_listinfo_byname=setvoipmode-incall=" + callingPackage);
                if (!isInvalidMode(mode, callingPackage) || value == null || !value.equals(TemperatureProvider.SWITCH_ON)) {
                    int oldModeOwnerPid = 0;
                    synchronized (this.mDeviceBroker.mSetModeLock) {
                        if (!this.mSetModeDeathHandlers.isEmpty()) {
                            oldModeOwnerPid = this.mSetModeDeathHandlers.get(0).getPid();
                        }
                        if (mode == -1) {
                            mode = this.mMode;
                        }
                        newModeOwnerPid = setModeInt(mode, cb, Binder.getCallingPid(), callingPackage);
                    }
                    if (newModeOwnerPid != oldModeOwnerPid && newModeOwnerPid != 0) {
                        this.mDeviceBroker.postDisconnectBluetoothSco(newModeOwnerPid);
                        return;
                    }
                    return;
                }
                Log.w(TAG, "MODIFY_PHONE_STATE Permission Denial: Current mode is in call");
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0179  */
    @GuardedBy({"mDeviceBroker.mSetModeLock"})
    public int setModeInt(int mode, IBinder cb, int pid, String caller) {
        int mode2;
        IBinder cb2;
        SetModeDeathHandler hdlr;
        int actualMode;
        int mode3;
        int status;
        int newModeOwnerPid;
        SetModeDeathHandler hdlr2;
        if (DEBUG_MODE) {
            StringBuilder sb = new StringBuilder();
            sb.append("setModeInt(mode=");
            mode2 = mode;
            sb.append(mode2);
            sb.append("[");
            sb.append(AudioSystem.modeToString(mode));
            sb.append("], pid=");
            sb.append(pid);
            sb.append(", caller=");
            sb.append(caller);
            sb.append(")");
            Log.v(TAG, sb.toString());
        } else {
            mode2 = mode;
        }
        int newModeOwnerPid2 = 0;
        if (cb == null) {
            Log.e(TAG, "setModeInt() called with null binder");
            synchronized (this.mSettingCallLock) {
                if (this.mIsInSetCallMode) {
                    this.mIsInSetCallMode = false;
                }
            }
            return 0;
        }
        SetModeDeathHandler hdlr3 = null;
        Iterator iter = this.mSetModeDeathHandlers.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            SetModeDeathHandler h = iter.next();
            if (h.getPid() == pid) {
                hdlr3 = h;
                iter.remove();
                hdlr3.getBinder().unlinkToDeath(hdlr3, 0);
                break;
            }
        }
        int oldMode = this.mMode;
        IBinder cb3 = cb;
        while (true) {
            if (mode2 != 0) {
                if (hdlr3 == null) {
                    hdlr2 = new SetModeDeathHandler(cb3, pid);
                } else {
                    hdlr2 = hdlr3;
                }
                try {
                    cb3.linkToDeath(hdlr2, 0);
                } catch (RemoteException e) {
                    Log.w(TAG, "setMode() could not link to " + cb3 + " binder death");
                }
                this.mSetModeDeathHandlers.add(0, hdlr2);
                hdlr2.setMode(mode2);
                cb2 = cb3;
                actualMode = mode2;
                hdlr = hdlr2;
            } else if (!this.mSetModeDeathHandlers.isEmpty()) {
                SetModeDeathHandler hdlr4 = this.mSetModeDeathHandlers.get(0);
                IBinder cb4 = hdlr4.getBinder();
                int actualMode2 = hdlr4.getMode();
                if (DEBUG_MODE) {
                    Log.w(TAG, " using actualmode=" + mode2 + " instead due to death hdlr at pid=" + hdlr4.mPid);
                }
                hdlr = hdlr4;
                cb2 = cb4;
                actualMode = actualMode2;
            } else {
                hdlr = hdlr3;
                cb2 = cb3;
                actualMode = mode2;
            }
            if (actualMode != this.mMode) {
                long identity = Binder.clearCallingIdentity();
                int status2 = AudioSystem.setPhoneState(actualMode);
                Binder.restoreCallingIdentity(identity);
                if (status2 == 0) {
                    if (DEBUG_MODE) {
                        Log.v(TAG, " mode successfully set to " + actualMode);
                    }
                    this.mMode = actualMode;
                } else {
                    if (hdlr != null) {
                        this.mSetModeDeathHandlers.remove(hdlr);
                        cb2.unlinkToDeath(hdlr, 0);
                    }
                    if (DEBUG_MODE) {
                        Log.w(TAG, " mode set to MODE_NORMAL after phoneState pb");
                    }
                    mode2 = 0;
                }
                notifyAtlasServiceModesUpdate(false);
                status = status2;
                mode3 = mode2;
            } else {
                notifyAtlasServiceModesUpdate(true);
                status = 0;
                mode3 = mode2;
            }
            if (status != 0 && !this.mSetModeDeathHandlers.isEmpty()) {
                hdlr3 = hdlr;
                cb3 = cb2;
                mode2 = mode3;
            } else if (status == 0) {
                if (actualMode != 0) {
                    if (this.mSetModeDeathHandlers.isEmpty()) {
                        Log.e(TAG, "setMode() different from MODE_NORMAL with empty mode client stack");
                    } else {
                        newModeOwnerPid = this.mSetModeDeathHandlers.get(0).getPid();
                        this.mModeLogger.log(new AudioServiceEvents.PhoneStateEvent(caller, pid, mode3, newModeOwnerPid, actualMode));
                        int streamType = getActiveStreamType(Integer.MIN_VALUE);
                        int device = getDeviceForStream(streamType);
                        setStreamVolumeInt(mStreamVolumeAlias[streamType], this.mStreamStates[mStreamVolumeAlias[streamType]].getIndex(device), device, true, caller);
                        updateStreamVolumeAlias(true, caller);
                        updateAbsVolumeMultiModeDevices(oldMode, actualMode);
                        newModeOwnerPid2 = newModeOwnerPid;
                    }
                }
                newModeOwnerPid = 0;
                this.mModeLogger.log(new AudioServiceEvents.PhoneStateEvent(caller, pid, mode3, newModeOwnerPid, actualMode));
                int streamType2 = getActiveStreamType(Integer.MIN_VALUE);
                int device2 = getDeviceForStream(streamType2);
                setStreamVolumeInt(mStreamVolumeAlias[streamType2], this.mStreamStates[mStreamVolumeAlias[streamType2]].getIndex(device2), device2, true, caller);
                updateStreamVolumeAlias(true, caller);
                updateAbsVolumeMultiModeDevices(oldMode, actualMode);
                newModeOwnerPid2 = newModeOwnerPid;
            }
        }
        if (status == 0) {
        }
        synchronized (this.mSettingCallLock) {
            if (this.mIsInSetCallMode) {
                this.mIsInSetCallMode = false;
            }
        }
        return newModeOwnerPid2;
    }

    private void notifyAtlasServiceModesUpdate(boolean onlyRead) {
        String allmodes = "";
        Log.d(TAG, "+notifyAtlasServiceModesUpdate onlyRead :" + onlyRead);
        Iterator iter = this.mSetModeDeathHandlers.iterator();
        while (iter.hasNext()) {
            SetModeDeathHandler h = iter.next();
            allmodes = allmodes + h.getPid() + "," + h.getMode() + ",";
            Log.d(TAG, "allmodes = " + allmodes);
        }
        Log.d(TAG, "modekey = " + ("allmodes=" + allmodes));
        if (onlyRead) {
            OppoAtlasManager.getInstance(this.mContext).setEvent(16, allmodes);
        } else {
            OppoAtlasManager.getInstance(this.mContext).setEvent(17, allmodes);
        }
        Log.d(TAG, "-notifyAtlasServiceModesUpdate");
    }

    public int getMode() {
        return this.mMode;
    }

    public int oppoGetMode() {
        int i;
        synchronized (this.mDeviceBroker.mSetModeLock) {
            i = this.mMode;
        }
        return i;
    }

    public String getScoClientInfo() {
        return this.mDeviceBroker.getScoClientInfo();
    }

    public int OppoGetDeviceForStream(int stream) {
        return getDeviceForStream(stream);
    }

    public String getBluetoothName() {
        AudioDeviceBroker audioDeviceBroker = this.mDeviceBroker;
        if (audioDeviceBroker == null || audioDeviceBroker.getCurAudioRoutes().bluetoothName == null) {
            return null;
        }
        return this.mDeviceBroker.getCurAudioRoutes().bluetoothName.toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0142, code lost:
        r17.mDeviceBroker.postDisconnectBluetoothSco(r18);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0147, code lost:
        if (r1 == 0) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0149, code lost:
        r17.mDeviceBroker.postDisconnectBluetoothSco(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:?, code lost:
        return;
     */
    public void removeMode(int pid, String caller) {
        SetModeDeathHandler hdlr;
        int actualMode;
        int status;
        int newModeOwnerPid;
        Log.d(TAG, "removeMode pid:" + pid);
        int newModeOwnerPid2 = 0;
        synchronized (this.mDeviceBroker.mSetModeLock) {
            SetModeDeathHandler hdlr2 = null;
            try {
                Iterator iter = this.mSetModeDeathHandlers.iterator();
                while (true) {
                    if (!iter.hasNext()) {
                        break;
                    }
                    SetModeDeathHandler h = iter.next();
                    if (h.getPid() == pid) {
                        hdlr2 = h;
                        iter.remove();
                        hdlr2.getBinder().unlinkToDeath(hdlr2, 0);
                        break;
                    }
                }
                int actualMode2 = 0;
                while (true) {
                    if (!this.mSetModeDeathHandlers.isEmpty()) {
                        SetModeDeathHandler hdlr3 = this.mSetModeDeathHandlers.get(0);
                        hdlr = hdlr3;
                        actualMode = hdlr3.getMode();
                    } else {
                        hdlr = hdlr2;
                        actualMode = actualMode2;
                    }
                    Log.d(TAG, "actualMode :" + actualMode + " mMode:" + this.mMode);
                    if (actualMode != this.mMode) {
                        Log.d(TAG, "+AudioSystem setPhoneState");
                        int status2 = AudioSystem.setPhoneState(actualMode);
                        Log.d(TAG, "-AudioSystem setPhoneState");
                        if (status2 == 0) {
                            if (DEBUG_MODE) {
                                Log.v(TAG, " mode successfully set to " + actualMode);
                            }
                            this.mMode = actualMode;
                        } else if (hdlr != null) {
                            this.mSetModeDeathHandlers.remove(hdlr);
                            hdlr.getBinder().unlinkToDeath(hdlr, 0);
                        }
                        notifyAtlasServiceModesUpdate(false);
                        status = status2;
                    } else {
                        notifyAtlasServiceModesUpdate(true);
                        status = 0;
                    }
                    if (status == 0) {
                        break;
                    } else if (this.mSetModeDeathHandlers.isEmpty()) {
                        break;
                    } else {
                        actualMode2 = actualMode;
                        hdlr2 = hdlr;
                    }
                }
                if (status == 0) {
                    if (actualMode != 0) {
                        if (this.mSetModeDeathHandlers.isEmpty()) {
                            Log.e(TAG, "setMode() different from MODE_NORMAL with empty mode client stack");
                        } else {
                            newModeOwnerPid = this.mSetModeDeathHandlers.get(0).getPid();
                            int streamType = getActiveStreamType(Integer.MIN_VALUE);
                            int device = getDeviceForStream(streamType);
                            setStreamVolumeInt(mStreamVolumeAlias[streamType], this.mStreamStates[mStreamVolumeAlias[streamType]].getIndex(device), device, true, caller);
                            updateStreamVolumeAlias(true, caller);
                            newModeOwnerPid2 = newModeOwnerPid;
                        }
                    }
                    newModeOwnerPid = 0;
                    try {
                        int streamType2 = getActiveStreamType(Integer.MIN_VALUE);
                        int device2 = getDeviceForStream(streamType2);
                        setStreamVolumeInt(mStreamVolumeAlias[streamType2], this.mStreamStates[mStreamVolumeAlias[streamType2]].getIndex(device2), device2, true, caller);
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                    try {
                        updateStreamVolumeAlias(true, caller);
                        newModeOwnerPid2 = newModeOwnerPid;
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                try {
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
    }

    class LoadSoundEffectReply {
        public int mStatus = 1;

        LoadSoundEffectReply() {
        }
    }

    private void loadTouchSoundAssetDefaults() {
        SOUND_EFFECT_FILES.add("Effect_Tick.ogg");
        for (int i = 0; i < 10; i++) {
            int[][] iArr = this.SOUND_EFFECT_FILES_MAP;
            iArr[i][0] = 0;
            iArr[i][1] = -1;
        }
    }

    /* access modifiers changed from: private */
    public void loadTouchSoundAssets() {
        XmlResourceParser parser = null;
        if (SOUND_EFFECT_FILES.isEmpty()) {
            loadTouchSoundAssetDefaults();
            try {
                parser = this.mContext.getResources().getXml(18284545);
                XmlUtils.beginDocument(parser, TAG_AUDIO_ASSETS);
                boolean inTouchSoundsGroup = false;
                if (ASSET_FILE_VERSION.equals(parser.getAttributeValue(null, "version"))) {
                    while (true) {
                        XmlUtils.nextElement(parser);
                        String element = parser.getName();
                        if (element != null) {
                            if (element.equals(TAG_GROUP) && GROUP_TOUCH_SOUNDS.equals(parser.getAttributeValue(null, "name"))) {
                                inTouchSoundsGroup = true;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    while (true) {
                        if (!inTouchSoundsGroup) {
                            break;
                        }
                        XmlUtils.nextElement(parser);
                        String element2 = parser.getName();
                        if (element2 != null) {
                            if (!element2.equals(TAG_ASSET)) {
                                break;
                            }
                            String id = parser.getAttributeValue(null, ATTR_ASSET_ID);
                            String file = parser.getAttributeValue(null, ATTR_ASSET_FILE);
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
                        } else {
                            break;
                        }
                    }
                }
            } catch (Resources.NotFoundException e2) {
                Log.w(TAG, "audio assets file not found", e2);
                if (parser == null) {
                    return;
                }
            } catch (XmlPullParserException e3) {
                Log.w(TAG, "XML parser exception reading touch sound assets", e3);
                if (parser == null) {
                    return;
                }
            } catch (IOException e4) {
                Log.w(TAG, "I/O exception reading touch sound assets", e4);
                if (parser == null) {
                    return;
                }
            } catch (Throwable th) {
                if (parser != null) {
                    parser.close();
                }
                throw th;
            }
            parser.close();
        }
    }

    public void playSoundEffect(int effectType) {
        playSoundEffectVolume(effectType, -1.0f);
    }

    public void playSoundEffectVolume(int effectType, float volume) {
        if (!isStreamMutedByRingerOrZenMode(1)) {
            if (effectType >= 10 || effectType < 0) {
                Log.w(TAG, "AudioService effectType value " + effectType + " out of range");
                return;
            }
            sendMsg(this.mAudioHandler, 5, 2, effectType, (int) (1000.0f * volume), null, 0);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0031, code lost:
        if (r1.mStatus != 0) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0034, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return true;
     */
    public boolean loadSoundEffects() {
        Throwable th;
        int attempts = 3;
        LoadSoundEffectReply reply = new LoadSoundEffectReply();
        synchronized (reply) {
            try {
                sendMsg(this.mAudioHandler, 7, 2, 0, 0, reply, 0);
                while (true) {
                    if (reply.mStatus != 1) {
                        break;
                    }
                    int attempts2 = attempts - 1;
                    if (attempts <= 0) {
                        break;
                    }
                    try {
                        reply.wait(5000);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "loadSoundEffects Interrupted while waiting sound pool loaded.");
                    } catch (Throwable th2) {
                        th = th2;
                    }
                    attempts = attempts2;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void scheduleLoadSoundEffects() {
        sendMsg(this.mAudioHandler, 7, 2, 0, 0, null, 0);
    }

    public void unloadSoundEffects() {
        sendMsg(this.mAudioHandler, 15, 2, 0, 0, null, 0);
    }

    /* access modifiers changed from: package-private */
    public class SoundPoolListenerThread extends Thread {
        public SoundPoolListenerThread() {
            super("SoundPoolListenerThread");
        }

        public void run() {
            Looper.prepare();
            Looper unused = AudioService.this.mSoundPoolLooper = Looper.myLooper();
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (AudioService.this.mSoundPool != null) {
                    SoundPoolCallback unused2 = AudioService.this.mSoundPoolCallBack = new SoundPoolCallback();
                    AudioService.this.mSoundPool.setOnLoadCompleteListener(AudioService.this.mSoundPoolCallBack);
                }
                AudioService.this.mSoundEffectsLock.notify();
            }
            Looper.loop();
        }
    }

    /* access modifiers changed from: private */
    public final class SoundPoolCallback implements SoundPool.OnLoadCompleteListener {
        List<Integer> mSamples;
        int mStatus;

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

    public void reloadAudioSettings() {
        readAudioSettings(false);
    }

    /* access modifiers changed from: private */
    public void readAudioSettings(boolean userSwitch) {
        this.mIsReadAudioSetting = false;
        readPersistedSettings();
        readUserRestrictions();
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = 0; streamType < numStreamTypes; streamType++) {
            VolumeStreamState streamState = this.mStreamStates[streamType];
            if (!userSwitch || mStreamVolumeAlias[streamType] != 3) {
                streamState.readSettings();
                synchronized (VolumeStreamState.class) {
                    if (streamState.mIsMuted && ((!isStreamAffectedByMute(streamType) && !isStreamMutedByRingerOrZenMode(streamType)) || this.mUseFixedVolume)) {
                        boolean unused = streamState.mIsMuted = false;
                    }
                }
            }
        }
        setRingerModeInt(getRingerModeInternal(), false);
        checkAllFixedVolumeDevices();
        checkAllAliasStreamVolumes();
        checkMuteAffectedStreams();
        synchronized (this.mSafeMediaVolumeStateLock) {
            this.mMusicActiveMs = MathUtils.constrain(Settings.Secure.getIntForUser(this.mContentResolver, "unsafe_volume_music_active_ms", 0, -2), 0, (int) UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX);
            if (this.mSafeMediaVolumeState == 3) {
                enforceSafeMediaVolume(TAG);
            }
        }
        updateInputDevice(this.mContentResolver);
        synchronized (this.mAudioSettingLock) {
            this.mIsReadAudioSetting = true;
            this.mAudioSettingLock.notify();
        }
    }

    public void waitReadAudioSetting() {
        synchronized (this.mAudioSettingLock) {
            if (!this.mIsReadAudioSetting) {
                try {
                    this.mAudioSettingLock.wait(1000);
                } catch (InterruptedException e) {
                    Log.w(TAG, "Interrupted while waiting mAudioSettingLock");
                }
            }
        }
    }

    public void setSpeakerphoneOn(boolean on) {
        if (checkAudioSettingsPermission("setSpeakerphoneOn()")) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
                synchronized (this.mSetModeDeathHandlers) {
                    Iterator<SetModeDeathHandler> it = this.mSetModeDeathHandlers.iterator();
                    while (it.hasNext()) {
                        if (it.next().getMode() == 2) {
                            Log.w(TAG, "getMode is call, Permission Denial: setSpeakerphoneOn from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                            return;
                        }
                    }
                }
            }
            String eventSource = "setSpeakerphoneOn(" + on + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
            if (DEBUG_MODE) {
                Log.d(TAG, eventSource);
            }
            if (this.mDeviceBroker.setSpeakerphoneOn(on, eventSource)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    this.mContext.sendBroadcastAsUser(new Intent("android.media.action.SPEAKERPHONE_STATE_CHANGED").setFlags(1073741824), UserHandle.ALL);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    public boolean isSpeakerphoneOn() {
        return this.mDeviceBroker.isSpeakerphoneOn();
    }

    public void setBluetoothScoOn(boolean on) {
        if (checkAudioSettingsPermission("setBluetoothScoOn()")) {
            if (UserHandle.getCallingAppId() >= 10000) {
                this.mDeviceBroker.setBluetoothScoOnByApp(on);
                return;
            }
            String eventSource = "setBluetoothScoOn(" + on + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
            if (DEBUG_MODE) {
                Log.d(TAG, eventSource);
            }
            this.mDeviceBroker.setBluetoothScoOn(on, eventSource);
        }
    }

    public int getScoStartcount(IBinder cb) {
        return 0;
    }

    public boolean isBluetoothScoOn() {
        return this.mDeviceBroker.isBluetoothScoOnForApp();
    }

    public void setBluetoothA2dpOn(boolean on) {
        String eventSource = "setBluetoothA2dpOn(" + on + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
        if (DEBUG_MODE) {
            Log.d(TAG, eventSource);
        }
        this.mDeviceBroker.setBluetoothA2dpOn_Async(on, eventSource);
    }

    public boolean isBluetoothScoAvailableOffCall() {
        return this.mDeviceBroker.isBluetoothScoAvailableOffCall();
    }

    public boolean isBluetoothA2dpOn() {
        return this.mDeviceBroker.isBluetoothA2dpOn();
    }

    public void startBluetoothSco(IBinder cb, int targetSdkVersion) {
        int scoAudioMode = targetSdkVersion < 18 ? 0 : -1;
        String eventSource = "startBluetoothSco()" + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
        if (DEBUG_MODE) {
            Log.d(TAG, eventSource);
        }
        startBluetoothScoInt(cb, scoAudioMode, eventSource);
    }

    public void startBluetoothScoVirtualCall(IBinder cb) {
        String eventSource = "startBluetoothScoVirtualCall()" + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
        if (DEBUG_MODE) {
            Log.d(TAG, eventSource);
        }
        startBluetoothScoInt(cb, 0, eventSource);
    }

    /* access modifiers changed from: package-private */
    public void startBluetoothScoInt(IBinder cb, int scoAudioMode, String eventSource) {
        if (checkAudioSettingsPermission("startBluetoothSco()") && this.mSystemReady) {
            synchronized (this.mDeviceBroker.mSetModeLock) {
                this.mDeviceBroker.startBluetoothScoForClient_Sync(cb, scoAudioMode, eventSource);
            }
        }
    }

    public void stopBluetoothSco(IBinder cb) {
        if (checkAudioSettingsPermission("stopBluetoothSco()") && this.mSystemReady) {
            String eventSource = "stopBluetoothSco()" + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
            if (DEBUG_MODE) {
                Log.d(TAG, eventSource);
            }
            synchronized (this.mDeviceBroker.mSetModeLock) {
                this.mDeviceBroker.stopBluetoothScoForClient_Sync(cb, eventSource);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ContentResolver getContentResolver() {
        return this.mContentResolver;
    }

    /* access modifiers changed from: private */
    public void onCheckMusicActive(String caller) {
        synchronized (this.mSafeMediaVolumeStateLock) {
            if (this.mSafeMediaVolumeState == 2) {
                int device = getDeviceForStream(3);
                if ((this.mSafeMediaVolumeDevices & device) != 0) {
                    sendMsg(this.mAudioHandler, 11, 0, 0, 0, caller, 60000);
                    int index = this.mStreamStates[3].getIndex(device);
                    if (AudioSystem.isStreamActive(3, 0) && index > safeMediaVolumeIndex(device)) {
                        this.mMusicActiveMs += 60000;
                        Log.d(TAG, "20H onCheckMusicActive:" + this.mMusicActiveMs);
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
        this.mAudioHandler.obtainMessage(17, this.mMusicActiveMs, 0).sendToTarget();
    }

    private int getSafeUsbMediaVolumeIndex() {
        int min = MIN_STREAM_VOLUME[3];
        int max = MAX_STREAM_VOLUME[3];
        this.mSafeUsbMediaVolumeDbfs = ((float) this.mContext.getResources().getInteger(17694880)) / 100.0f;
        while (true) {
            if (Math.abs(max - min) <= 1) {
                break;
            }
            int index = (max + min) / 2;
            float gainDB = AudioSystem.getStreamVolumeDB(3, index, 67108864);
            if (Float.isNaN(gainDB)) {
                break;
            }
            float f = this.mSafeUsbMediaVolumeDbfs;
            if (gainDB == f) {
                break;
            } else if (gainDB < f) {
                min = index;
            } else {
                max = index;
            }
        }
        return this.mContext.getResources().getInteger(17694879) * 10;
    }

    /* access modifiers changed from: private */
    public void onConfigureSafeVolume(boolean force, String caller) {
        boolean safeMediaVolumeEnabled;
        int persistedState;
        synchronized (this.mSafeMediaVolumeStateLock) {
            int mcc = this.mContext.getResources().getConfiguration().mcc;
            if (this.mMcc != mcc || (this.mMcc == 0 && force)) {
                this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(17694879) * 10;
                this.mSafeUsbMediaVolumeIndex = getSafeUsbMediaVolumeIndex();
                if (!SystemProperties.getBoolean("audio.safemedia.force", false)) {
                    if (!this.mContext.getResources().getBoolean(17891505)) {
                        safeMediaVolumeEnabled = false;
                        boolean safeMediaVolumeBypass = SystemProperties.getBoolean("audio.safemedia.bypass", false);
                        if (safeMediaVolumeEnabled || safeMediaVolumeBypass) {
                            this.mSafeMediaVolumeState = 1;
                            persistedState = 1;
                        } else {
                            persistedState = 3;
                            if (this.mSafeMediaVolumeState != 2 && this.mMusicActiveMs == 0) {
                                this.mSafeMediaVolumeState = 3;
                                enforceSafeMediaVolume(caller);
                            }
                        }
                        this.mMcc = mcc;
                        sendMsg(this.mAudioHandler, 14, 2, persistedState, 0, null, 0);
                    }
                }
                safeMediaVolumeEnabled = true;
                boolean safeMediaVolumeBypass2 = SystemProperties.getBoolean("audio.safemedia.bypass", false);
                if (safeMediaVolumeEnabled) {
                }
                this.mSafeMediaVolumeState = 1;
                persistedState = 1;
                this.mMcc = mcc;
                sendMsg(this.mAudioHandler, 14, 2, persistedState, 0, null, 0);
            }
        }
    }

    private int checkForRingerModeChange(int oldIndex, int direction, int step, boolean isMuted, String caller, int flags) {
        int result = 1;
        if (isPlatformTelevision() || this.mIsSingleVolume) {
            return 1;
        }
        int ringerMode = getRingerModeInternal();
        if (ringerMode == 0) {
            if (this.mIsSingleVolume && direction == -1 && oldIndex >= step * 2 && isMuted) {
                ringerMode = 2;
            } else if (direction == 1 || direction == 101 || direction == 100) {
                if (!this.mVolumePolicy.volumeUpToExitSilent) {
                    result = 1 | 128;
                } else {
                    ringerMode = (!this.mHasVibrator || direction != 1) ? 2 : 1;
                }
            }
            result &= -2;
        } else if (ringerMode != 1) {
            if (ringerMode != 2) {
                Log.e(TAG, "checkForRingerModeChange() wrong ringer mode: " + ringerMode);
            } else if (direction == -1) {
                if (this.mHasVibrator) {
                    if (step <= oldIndex && oldIndex < step * 2) {
                        ringerMode = 1;
                        this.mLoweredFromNormalToVibrateTime = SystemClock.uptimeMillis();
                    }
                } else if (oldIndex == step && this.mVolumePolicy.volumeDownToEnterSilent) {
                    ringerMode = 0;
                }
            } else if (this.mIsSingleVolume && (direction == 101 || direction == -100)) {
                if (this.mHasVibrator) {
                    ringerMode = 1;
                } else {
                    ringerMode = 0;
                }
                result = 1 & -2;
            }
        } else if (!this.mHasVibrator) {
            Log.e(TAG, "checkForRingerModeChange() current ringer mode is vibratebut no vibrator is present");
        } else {
            if (direction == -1) {
                if (this.mIsSingleVolume && oldIndex >= step * 2 && isMuted) {
                    ringerMode = 2;
                } else if (this.mPrevVolDirection != -1) {
                    if (!this.mVolumePolicy.volumeDownToEnterSilent) {
                        result = 1 | 2048;
                    } else if (SystemClock.uptimeMillis() - this.mLoweredFromNormalToVibrateTime > ((long) this.mVolumePolicy.vibrateToSilentDebounce) && this.mRingerModeDelegate.canVolumeDownEnterSilent()) {
                        ringerMode = 0;
                    }
                }
            } else if (direction == 1 || direction == 101 || direction == 100) {
                ringerMode = 2;
            }
            result &= -2;
        }
        if (!isAndroidNPlus(caller) || !wouldToggleZenMode(ringerMode) || this.mNm.isNotificationPolicyAccessGrantedForPackage(caller) || (flags & 4096) != 0) {
            setRingerMode(ringerMode, "AS.AudioService.checkForRingerModeChange", false);
            this.mPrevVolDirection = direction;
            return result;
        }
        throw new SecurityException("Not allowed to change Do Not Disturb state");
    }

    private boolean isVibrateInRingSilentMode() {
        if (Settings.System.getInt(this.mContext.getContentResolver(), "vibrate_when_silent", 0) != 0) {
            return true;
        }
        return false;
    }

    private boolean isRingVolumeDefault() {
        int ringVolumeDefault = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "default_volume_type_coloros_six", 0, -2);
        Log.d(TAG, "ringVolumeDefault :" + ringVolumeDefault);
        if (ringVolumeDefault != 0) {
            return true;
        }
        return false;
    }

    private int OppoCheckForRingerModeChange(int oldIndex, int direction, int step, boolean isMuted, String caller, int flags) {
        boolean z;
        int result = 1;
        int ringerMode = getRingerModeInternal();
        if (ringerMode == 0 || ringerMode == 1) {
            if (direction == 1 || direction == 101 || direction == 100) {
                ringerMode = 2;
            }
            result = 1 & -2;
        } else if (ringerMode != 2) {
            Log.e(TAG, "checkForRingerModeChange() wrong ringer mode: " + ringerMode);
        } else if (direction == -1 && oldIndex == step && ((z = this.mHasVibrator) || (!z && this.mVolumePolicy.volumeDownToEnterSilent))) {
            ringerMode = isVibrateInRingSilentMode() ? 1 : 0;
        }
        if (!isAndroidNPlus(caller) || !wouldToggleZenMode(ringerMode) || this.mNm.isNotificationPolicyAccessGrantedForPackage(caller) || (flags & 4096) != 0) {
            if (direction != 0) {
                setRingerMode(ringerMode, "AS.AudioService.checkForRingerModeChange", false);
                this.mPrevVolDirection = direction;
            }
            return result;
        }
        throw new SecurityException("Not allowed to change Do Not Disturb state");
    }

    public boolean isStreamAffectedByRingerMode(int streamType) {
        return (this.mRingerModeAffectedStreams & (1 << streamType)) != 0;
    }

    private boolean shouldZenMuteStream(int streamType) {
        if (this.mNm.getZenMode() != 1) {
            return false;
        }
        NotificationManager.Policy zenPolicy = this.mNm.getConsolidatedNotificationPolicy();
        return (((zenPolicy.priorityCategories & 32) == 0) && isAlarm(streamType)) || (((zenPolicy.priorityCategories & 64) == 0) && isMedia(streamType)) || ((((zenPolicy.priorityCategories & 128) == 0) && isSystem(streamType)) || (ZenModeConfig.areAllPriorityOnlyNotificationZenSoundsMuted(this.mNm.getConsolidatedNotificationPolicy()) && isNotificationOrRinger(streamType)));
    }

    private boolean isStreamMutedByRingerOrZenMode(int streamType) {
        return (this.mRingerAndZenModeMutedStreams & (1 << streamType)) != 0;
    }

    private boolean updateZenModeAffectedStreams() {
        int zenModeAffectedStreams = 0;
        if (this.mSystemReady && this.mNm.getZenMode() == 1) {
            NotificationManager.Policy zenPolicy = this.mNm.getConsolidatedNotificationPolicy();
            if ((zenPolicy.priorityCategories & 32) == 0) {
                zenModeAffectedStreams = 0 | 16;
            }
            if ((zenPolicy.priorityCategories & 64) == 0) {
                zenModeAffectedStreams |= 8;
            }
            if ((zenPolicy.priorityCategories & 128) == 0) {
                zenModeAffectedStreams |= 2;
            }
        }
        if (this.mZenModeAffectedStreams == zenModeAffectedStreams) {
            return false;
        }
        this.mZenModeAffectedStreams = zenModeAffectedStreams;
        return true;
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mSettingsLock"})
    public boolean updateRingerAndZenModeAffectedStreams() {
        int ringerModeAffectedStreams;
        int ringerModeAffectedStreams2;
        boolean updatedZenModeAffectedStreams = updateZenModeAffectedStreams();
        int ringerModeAffectedStreams3 = Settings.System.getIntForUser(this.mContentResolver, "mode_ringer_streams_affected", 166, -2);
        if (this.mIsSingleVolume) {
            ringerModeAffectedStreams3 = 0;
        } else {
            AudioManagerInternal.RingerModeDelegate ringerModeDelegate = this.mRingerModeDelegate;
            if (ringerModeDelegate != null) {
                ringerModeAffectedStreams3 = ringerModeDelegate.getRingerModeAffectedStreams(ringerModeAffectedStreams3);
            }
        }
        if (this.mCameraSoundForced) {
            ringerModeAffectedStreams = ringerModeAffectedStreams3 & -129;
        } else {
            ringerModeAffectedStreams = ringerModeAffectedStreams3 | 128;
        }
        if (mStreamVolumeAlias[8] == 2) {
            ringerModeAffectedStreams2 = ringerModeAffectedStreams | 256;
        } else {
            ringerModeAffectedStreams2 = ringerModeAffectedStreams & -257;
        }
        if (ringerModeAffectedStreams2 == this.mRingerModeAffectedStreams) {
            return updatedZenModeAffectedStreams;
        }
        Settings.System.putIntForUser(this.mContentResolver, "mode_ringer_streams_affected", ringerModeAffectedStreams2, -2);
        this.mRingerModeAffectedStreams = ringerModeAffectedStreams2;
        return true;
    }

    public boolean isStreamAffectedByMute(int streamType) {
        return (this.mMuteAffectedStreams & (1 << streamType)) != 0;
    }

    private void ensureValidDirection(int direction) {
        if (direction != -100 && direction != -1 && direction != 0 && direction != 1 && direction != 100 && direction != 101) {
            throw new IllegalArgumentException("Bad direction " + direction);
        }
    }

    private void ensureValidStreamType(int streamType) {
        if (streamType < 0 || streamType >= this.mStreamStates.length) {
            throw new IllegalArgumentException("Bad stream type " + streamType);
        }
    }

    private boolean isMuteAdjust(int adjust) {
        return adjust == -100 || adjust == 100 || adjust == 101;
    }

    /* access modifiers changed from: package-private */
    public boolean isInCommunication() {
        long ident = Binder.clearCallingIdentity();
        boolean IsInCall = ((TelecomManager) this.mContext.getSystemService("telecom")).isInCall();
        Binder.restoreCallingIdentity(ident);
        Log.d(TAG, "isInCommunication mode :" + getMode());
        if (getMode() == 0) {
            return false;
        }
        if (IsInCall || getMode() == 3 || getMode() == 2) {
            return true;
        }
        return false;
    }

    private boolean wasStreamActiveRecently(int stream, int delay_ms) {
        return AudioSystem.isStreamActive(stream, delay_ms) || AudioSystem.isStreamActiveRemotely(stream, delay_ms);
    }

    private int getActiveStreamType(int suggestedStreamType) {
        if (this.mIsSingleVolume && suggestedStreamType == Integer.MIN_VALUE) {
            return 3;
        }
        if (this.mPlatformType == 1) {
            if (isInCommunication()) {
                if (getMode() == 3 && this.mAdjustVolumeAction) {
                    String state = OppoAtlasManager.getInstance(this.mContext).getParameters("streamtype_adjust_revise");
                    Log.d(TAG, "streamtype_adjust_revise = " + state);
                    if (state != null && state.equalsIgnoreCase(TemperatureProvider.SWITCH_ON)) {
                        Log.d(TAG, "return STREAM_MUSIC");
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
                if (wasStreamActiveRecently(2, sStreamOverrideDelayMs)) {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_RING stream active");
                    }
                    return 2;
                } else if (wasStreamActiveRecently(5, sStreamOverrideDelayMs)) {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION stream active");
                    }
                    return 5;
                } else if (isRingVolumeDefault() && !wasStreamActiveRecently(3, sStreamOverrideDelayMs)) {
                    return 2;
                } else {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing DEFAULT_VOL_STREAM_NO_PLAYBACK(3) b/c default");
                    }
                    return 3;
                }
            } else if (wasStreamActiveRecently(5, sStreamOverrideDelayMs)) {
                if (DEBUG_VOL) {
                    Log.v(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION stream active");
                }
                return 5;
            } else if (wasStreamActiveRecently(2, sStreamOverrideDelayMs)) {
                if (DEBUG_VOL) {
                    Log.v(TAG, "getActiveStreamType: Forcing STREAM_RING stream active");
                }
                return 2;
            }
        }
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
        } else if (AudioSystem.isStreamActive(5, sStreamOverrideDelayMs)) {
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION");
            }
            return 5;
        } else if (AudioSystem.isStreamActive(2, sStreamOverrideDelayMs)) {
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Forcing STREAM_RING");
            }
            return 2;
        } else if (suggestedStreamType != Integer.MIN_VALUE) {
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Returning suggested type " + suggestedStreamType);
            }
            return suggestedStreamType;
        } else if (AudioSystem.isStreamActive(5, sStreamOverrideDelayMs)) {
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION");
            }
            return 5;
        } else if (AudioSystem.isStreamActive(2, sStreamOverrideDelayMs)) {
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Forcing STREAM_RING");
            }
            return 2;
        } else {
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Forcing DEFAULT_VOL_STREAM_NO_PLAYBACK(3) b/c default");
            }
            return 3;
        }
    }

    private void broadcastRingerMode(String action, int ringerMode) {
        Intent broadcast = new Intent(action);
        broadcast.putExtra("android.media.EXTRA_RINGER_MODE", ringerMode);
        broadcast.addFlags(603979776);
        sendStickyBroadcastToAll(broadcast);
    }

    private void broadcastVibrateSetting(int vibrateType) {
        if (this.mActivityManagerInternal.isSystemReady()) {
            Intent broadcast = new Intent("android.media.VIBRATE_SETTING_CHANGED");
            broadcast.putExtra("android.media.EXTRA_VIBRATE_TYPE", vibrateType);
            broadcast.putExtra("android.media.EXTRA_VIBRATE_SETTING", getVibrateSetting(vibrateType));
            sendBroadcastToAll(broadcast);
        }
    }

    /* access modifiers changed from: private */
    public void queueMsgUnderWakeLock(Handler handler, int msg, int arg1, int arg2, Object obj, int delay) {
        long ident = Binder.clearCallingIdentity();
        this.mAudioEventWakeLock.acquire();
        Binder.restoreCallingIdentity(ident);
        sendMsg(handler, msg, 2, arg1, arg2, obj, delay);
    }

    /* access modifiers changed from: private */
    public static void sendMsg(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delay) {
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == 1 && handler.hasMessages(msg)) {
            return;
        }
        handler.sendMessageAtTime(handler.obtainMessage(msg, arg1, arg2, obj), SystemClock.uptimeMillis() + ((long) delay));
    }

    /* access modifiers changed from: package-private */
    public boolean checkAudioSettingsPermission(String method) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_SETTINGS") == 0) {
            return true;
        }
        Log.w(TAG, "Audio Settings Permission Denial: " + method + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return false;
    }

    /* access modifiers changed from: package-private */
    public int getDeviceForStream(int stream) {
        int device = getDevicesForStream(stream);
        if (((device - 1) & device) == 0) {
            return device;
        }
        if ((device & 2) != 0) {
            return 2;
        }
        if ((262144 & device) != 0) {
            return DumpState.DUMP_DOMAIN_PREFERRED;
        }
        if ((524288 & device) != 0) {
            return DumpState.DUMP_FROZEN;
        }
        if ((2097152 & device) != 0) {
            return DumpState.DUMP_COMPILER_STATS;
        }
        return device & 896;
    }

    /* access modifiers changed from: private */
    public int getDevicesForStream(int stream) {
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

    /* access modifiers changed from: private */
    public void observeDevicesForStreams(int skipStream) {
        synchronized (VolumeStreamState.class) {
            for (int stream = 0; stream < this.mStreamStates.length; stream++) {
                if (stream != skipStream) {
                    this.mStreamStates[stream].observeDevicesForStream_syncVSS(false);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void postObserveDevicesForAllStreams() {
        sendMsg(this.mAudioHandler, 27, 2, 0, 0, null, 0);
    }

    /* access modifiers changed from: private */
    public void onObserveDevicesForAllStreams() {
        observeDevicesForStreams(-1);
    }

    public void setWiredDeviceConnectionState(int type, int state, String address, String name, String caller) {
        if (state == 1 || state == 0) {
            if (DEBUG_DEVICES) {
                Log.d(TAG, "setWiredDeviceConnectionState()" + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid() + ", type=" + type + ", state=" + state + ", address=" + address + ", name=" + name + ", caller=" + caller);
            }
            this.mDeviceBroker.setWiredDeviceConnectionState(type, state, address, name, caller);
            return;
        }
        throw new IllegalArgumentException("Invalid state " + state);
    }

    public void setBluetoothHearingAidDeviceConnectionState(BluetoothDevice device, int state, boolean suppressNoisyIntent, int musicDevice) {
        if (device == null) {
            throw new IllegalArgumentException("Illegal null device");
        } else if (state == 2 || state == 0) {
            if (DEBUG_DEVICES) {
                Log.d(TAG, "setBluetoothHearingAidDeviceConnectionState()" + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid() + ", device=" + device + ", state=" + state + ", suppressNoisyIntent=" + suppressNoisyIntent + ", musicDevice=" + musicDevice);
            }
            this.mDeviceBroker.postBluetoothHearingAidDeviceConnectionState(device, state, suppressNoisyIntent, musicDevice, "AudioService");
        } else {
            throw new IllegalArgumentException("Illegal BluetoothProfile state for device  (dis)connection, got " + state);
        }
    }

    public void setBluetoothA2dpDeviceConnectionStateSuppressNoisyIntent(BluetoothDevice device, int state, int profile, boolean suppressNoisyIntent, int a2dpVolume) {
        if (device == null) {
            throw new IllegalArgumentException("Illegal null device");
        } else if (state == 2 || state == 0) {
            if (DEBUG_DEVICES) {
                Log.d(TAG, "setBluetoothA2dpDeviceConnectionStateSuppressNoisyIntent()" + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid() + ", device=" + device + ", state=" + state + ", suppressNoisyIntent=" + suppressNoisyIntent + ", a2dpVolume=" + a2dpVolume);
            }
            this.mDeviceBroker.postBluetoothA2dpDeviceConnectionStateSuppressNoisyIntent(device, state, profile, suppressNoisyIntent, a2dpVolume);
        } else {
            throw new IllegalArgumentException("Illegal BluetoothProfile state for device  (dis)connection, got " + state);
        }
    }

    public void handleBluetoothA2dpDeviceConfigChange(BluetoothDevice device) {
        if (device != null) {
            if (DEBUG_DEVICES) {
                Log.d(TAG, "handleBluetoothA2dpDeviceConfigChange()" + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid() + ", device=" + device);
            }
            this.mDeviceBroker.postBluetoothA2dpDeviceConfigChange(device);
            return;
        }
        throw new IllegalArgumentException("Illegal null device");
    }

    /* access modifiers changed from: package-private */
    public void postAccessoryPlugMediaUnmute(int newDevice) {
    }

    public boolean hasHapticChannels(Uri uri) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(this.mContext, uri, (Map<String, String>) null);
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                if (format.containsKey("haptic-channel-count") && format.getInteger("haptic-channel-count") > 0) {
                    return true;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "hasHapticChannels failure:" + e);
        }
        return false;
    }

    /* access modifiers changed from: private */
    public class VolumeStreamState {
        /* access modifiers changed from: private */
        public final SparseIntArray mIndexMap;
        /* access modifiers changed from: private */
        public int mIndexMax;
        /* access modifiers changed from: private */
        public int mIndexMin;
        /* access modifiers changed from: private */
        public boolean mIsMuted;
        /* access modifiers changed from: private */
        public final SparseIntArray mMediaVolumeIndexMap;
        private int mObservedDevices;
        private final Intent mStreamDevicesChanged;
        /* access modifiers changed from: private */
        public final int mStreamType;
        private final Intent mVolumeChanged;
        /* access modifiers changed from: private */
        public String mVolumeIndexSettingName;

        private VolumeStreamState(String settingName, int streamType) {
            this.mMediaVolumeIndexMap = new SparseIntArray(8);
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
            int[] iArr = AudioService.mStreamVolumeAlias;
            int i = this.mStreamType;
            if (iArr[i] == i) {
                EventLogTags.writeStreamDevicesChanged(i, prevDevices, devices);
            }
            AudioService.this.sendBroadcastToAll(this.mStreamDevicesChanged.putExtra("android.media.EXTRA_PREV_VOLUME_STREAM_DEVICES", prevDevices).putExtra("android.media.EXTRA_VOLUME_STREAM_DEVICES", devices));
            return devices;
        }

        public String getSettingNameForDevice(int device) {
            if (!hasValidSettingsName()) {
                return null;
            }
            String suffix = AudioSystem.getOutputDeviceName(device);
            if (suffix.isEmpty()) {
                return this.mVolumeIndexSettingName;
            }
            return this.mVolumeIndexSettingName + "_" + suffix;
        }

        /* access modifiers changed from: private */
        public boolean hasValidSettingsName() {
            String str = this.mVolumeIndexSettingName;
            return str != null && !str.isEmpty();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:19:0x002d, code lost:
            monitor-enter(com.android.server.audio.AudioService.VolumeStreamState.class);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x002e, code lost:
            r0 = 1342177279;
            r2 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0032, code lost:
            if (r0 == 0) goto L_0x0071;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0034, code lost:
            r5 = 1 << r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0038, code lost:
            if ((r5 & r0) != 0) goto L_0x003b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x003b, code lost:
            r0 = r0 & (~r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x003e, code lost:
            if (r5 != 1073741824) goto L_0x0047;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
            r7 = android.media.AudioSystem.DEFAULT_STREAM_VOLUME[r11.mStreamType];
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0047, code lost:
            r7 = -1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x004c, code lost:
            if (hasValidSettingsName() != false) goto L_0x0050;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x004e, code lost:
            r8 = r7;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0050, code lost:
            r8 = android.provider.Settings.System.getIntForUser(com.android.server.audio.AudioService.access$3300(r11.this$0), getSettingNameForDevice(r5), r7, -2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0060, code lost:
            if (r8 != -1) goto L_0x0063;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x0063, code lost:
            r11.mIndexMap.put(r5, getValidIndex(r8 * 10));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x006e, code lost:
            r2 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0071, code lost:
            monitor-exit(com.android.server.audio.AudioService.VolumeStreamState.class);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x0072, code lost:
            return;
         */
        public void readSettings() {
            synchronized (AudioService.this.mSettingsLock) {
                synchronized (VolumeStreamState.class) {
                    if (AudioService.this.mUseFixedVolume) {
                        this.mIndexMap.put(1073741824, this.mIndexMax);
                    } else if (this.mStreamType == 1 || this.mStreamType == 7) {
                        int index = AudioSystem.DEFAULT_STREAM_VOLUME[this.mStreamType] * 10;
                        if (AudioService.this.mCameraSoundForced) {
                            index = this.mIndexMax;
                        }
                        this.mIndexMap.put(1073741824, index);
                    }
                }
            }
        }

        private int getAbsoluteVolumeIndex(int index) {
            if (index == 0) {
                return 0;
            }
            if (index <= 0 || index > 3) {
                return (this.mIndexMax + 5) / 10;
            }
            return ((int) (((float) this.mIndexMax) * AudioService.this.mPrescaleAbsoluteVolume[index - 1])) / 10;
        }

        private void setStreamVolumeIndex(int index, int device) {
            if (this.mStreamType == 6 && index == 0 && !this.mIsMuted) {
                index = 1;
            }
            AudioSystem.setStreamVolumeIndexAS(this.mStreamType, index, device);
        }

        /* access modifiers changed from: package-private */
        public void applyDeviceVolume_syncVSS(int device, boolean isAvrcpAbsVolSupported) {
            int index;
            if (this.mIsMuted) {
                index = 0;
            } else if ((device & 896) != 0 && isAvrcpAbsVolSupported) {
                index = getAbsoluteVolumeIndex((getIndex(device) + 5) / 10);
            } else if ((AudioService.this.mFullVolumeDevices & device) != 0) {
                index = (this.mIndexMax + 5) / 10;
            } else if ((134217728 & device) != 0) {
                index = (this.mIndexMax + 5) / 10;
            } else {
                index = (getIndex(device) + 5) / 10;
            }
            setStreamVolumeIndex(index, device);
        }

        public void applyAllVolumes() {
            int index;
            int index2;
            boolean isAvrcpAbsVolSupported = AudioService.this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported();
            synchronized (VolumeStreamState.class) {
                for (int i = 0; i < this.mIndexMap.size(); i++) {
                    int device = this.mIndexMap.keyAt(i);
                    if (device != 1073741824) {
                        if (this.mIsMuted) {
                            index2 = 0;
                        } else if ((device & 896) != 0 && isAvrcpAbsVolSupported) {
                            index2 = getAbsoluteVolumeIndex((getIndex(device) + 5) / 10);
                        } else if ((AudioService.this.mFullVolumeDevices & device) != 0) {
                            index2 = (this.mIndexMax + 5) / 10;
                        } else if ((134217728 & device) != 0) {
                            index2 = (this.mIndexMax + 5) / 10;
                        } else {
                            index2 = (this.mIndexMap.valueAt(i) + 5) / 10;
                        }
                        setStreamVolumeIndex(index2, device);
                    }
                }
                if (this.mIsMuted) {
                    index = 0;
                } else {
                    index = (getIndex(1073741824) + 5) / 10;
                }
                setStreamVolumeIndex(index, 1073741824);
            }
        }

        public boolean adjustIndex(int deltaIndex, int device, String caller) {
            return setIndex(getIndex(device) + deltaIndex, device, caller);
        }

        public boolean setIndex(int index, int device, String caller) {
            int oldIndex;
            int index2;
            boolean changed;
            synchronized (AudioService.this.mSettingsLock) {
                synchronized (VolumeStreamState.class) {
                    oldIndex = getIndex(device);
                    index2 = getValidIndex(index);
                    if (this.mStreamType == 7 && AudioService.this.mCameraSoundForced) {
                        index2 = this.mIndexMax;
                    }
                    this.mIndexMap.put(device, index2);
                    boolean isCurrentDevice = true;
                    changed = oldIndex != index2;
                    if (device != AudioService.this.getDeviceForStream(this.mStreamType)) {
                        isCurrentDevice = false;
                    }
                    for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                        VolumeStreamState aliasStreamState = AudioService.this.mStreamStates[streamType];
                        if (streamType != this.mStreamType && AudioService.mStreamVolumeAlias[streamType] == this.mStreamType && (changed || !aliasStreamState.hasIndexForDevice(device))) {
                            int scaledIndex = AudioService.this.rescaleIndex(index2, this.mStreamType, streamType);
                            aliasStreamState.setIndex(scaledIndex, device, caller);
                            if (isCurrentDevice) {
                                aliasStreamState.setIndex(scaledIndex, AudioService.this.getDeviceForStream(streamType), caller);
                            }
                        }
                    }
                    if (changed && this.mStreamType == 2 && device == 2) {
                        for (int i = 0; i < this.mIndexMap.size(); i++) {
                            int otherDevice = this.mIndexMap.keyAt(i);
                            if ((otherDevice & HdmiCecKeycode.UI_BROADCAST_DIGITAL_CABLE) != 0) {
                                this.mIndexMap.put(otherDevice, index2);
                            }
                        }
                    }
                }
            }
            if (changed) {
                int oldIndex2 = (oldIndex + 5) / 10;
                int index3 = (index2 + 5) / 10;
                int[] iArr = AudioService.mStreamVolumeAlias;
                int i2 = this.mStreamType;
                if (iArr[i2] == i2) {
                    if (caller == null) {
                        Log.w(AudioService.TAG, "No caller for volume_changed event", new Throwable());
                    }
                    EventLogTags.writeVolumeChanged(this.mStreamType, oldIndex2, index3, this.mIndexMax / 10, caller);
                }
                this.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", index3);
                this.mVolumeChanged.putExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", oldIndex2);
                this.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE_ALIAS", AudioService.mStreamVolumeAlias[this.mStreamType]);
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

        public boolean hasIndexForDevice(int device) {
            boolean z;
            synchronized (VolumeStreamState.class) {
                z = this.mIndexMap.get(device, -1) != -1;
            }
            return z;
        }

        public int getMaxIndex() {
            return this.mIndexMax;
        }

        public int getMinIndex() {
            return this.mIndexMin;
        }

        /* JADX INFO: Multiple debug info for r2v3 android.util.SparseIntArray: [D('i' int), D('srcMap' android.util.SparseIntArray)] */
        @GuardedBy({"VolumeStreamState.class"})
        public void setAllIndexes(VolumeStreamState srcStream, String caller) {
            if (this.mStreamType != srcStream.mStreamType) {
                int srcStreamType = srcStream.getStreamType();
                int index = AudioService.this.rescaleIndex(srcStream.getIndex(1073741824), srcStreamType, this.mStreamType);
                for (int i = 0; i < this.mIndexMap.size(); i++) {
                    SparseIntArray sparseIntArray = this.mIndexMap;
                    sparseIntArray.put(sparseIntArray.keyAt(i), index);
                }
                SparseIntArray srcMap = srcStream.mIndexMap;
                for (int i2 = 0; i2 < srcMap.size(); i2++) {
                    setIndex(AudioService.this.rescaleIndex(srcMap.valueAt(i2), srcStreamType, this.mStreamType), srcMap.keyAt(i2), caller);
                }
            }
        }

        @GuardedBy({"VolumeStreamState.class"})
        public void setAllIndexesToMax() {
            for (int i = 0; i < this.mIndexMap.size(); i++) {
                SparseIntArray sparseIntArray = this.mIndexMap;
                sparseIntArray.put(sparseIntArray.keyAt(i), this.mIndexMax);
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
            boolean isAvrcpAbsVolSupported = AudioService.this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported();
            synchronized (VolumeStreamState.class) {
                if (AudioService.mStreamVolumeAlias[this.mStreamType] == 3) {
                    for (int i = 0; i < this.mIndexMap.size(); i++) {
                        int device = this.mIndexMap.keyAt(i);
                        int index = this.mIndexMap.valueAt(i);
                        if (!((AudioService.this.mFullVolumeDevices & device) == 0 && ((AudioService.this.mFixedVolumeDevices & device) == 0 || index == 0))) {
                            this.mIndexMap.put(device, this.mIndexMax);
                        }
                        applyDeviceVolume_syncVSS(device, isAvrcpAbsVolSupported);
                    }
                }
            }
        }

        private int getValidIndex(int index) {
            int i = this.mIndexMin;
            if (index < i) {
                return i;
            }
            if (AudioService.this.mUseFixedVolume || index > this.mIndexMax) {
                return this.mIndexMax;
            }
            return index;
        }

        /* access modifiers changed from: private */
        public void dump(PrintWriter pw) {
            String deviceName;
            pw.print("   Muted: ");
            pw.println(this.mIsMuted);
            pw.print("   Min: ");
            pw.println((this.mIndexMin + 5) / 10);
            pw.print("   Max: ");
            pw.println((this.mIndexMax + 5) / 10);
            pw.print("   streamVolume:");
            pw.println(AudioService.this.getStreamVolume(this.mStreamType));
            pw.print("   Current: ");
            for (int i = 0; i < this.mIndexMap.size(); i++) {
                if (i > 0) {
                    pw.print(", ");
                }
                int device = this.mIndexMap.keyAt(i);
                pw.print(Integer.toHexString(device));
                if (device == 1073741824) {
                    deviceName = BatteryService.HealthServiceWrapper.INSTANCE_VENDOR;
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
            int i2 = 0;
            int n = 0;
            while (true) {
                int device2 = 1 << i2;
                if (device2 != 1073741824) {
                    if ((devices & device2) != 0) {
                        int n2 = n + 1;
                        if (n > 0) {
                            pw.print(", ");
                        }
                        pw.print(AudioSystem.getOutputDeviceName(device2));
                        n = n2;
                    }
                    i2++;
                } else {
                    return;
                }
            }
        }
    }

    private class AudioSystemThread extends Thread {
        AudioSystemThread() {
            super("AudioService");
        }

        public void run() {
            Looper.prepare();
            synchronized (AudioService.this) {
                AudioHandler unused = AudioService.this.mAudioHandler = new AudioHandler();
                AudioService.this.notify();
            }
            Looper.loop();
        }
    }

    /* access modifiers changed from: private */
    public static final class DeviceVolumeUpdate {
        private static final int NO_NEW_INDEX = -2049;
        final String mCaller;
        final int mDevice;
        final int mStreamType;
        private final int mVssVolIndex;

        DeviceVolumeUpdate(int streamType, int vssVolIndex, int device, String caller) {
            this.mStreamType = streamType;
            this.mVssVolIndex = vssVolIndex;
            this.mDevice = device;
            this.mCaller = caller;
        }

        DeviceVolumeUpdate(int streamType, int device, String caller) {
            this.mStreamType = streamType;
            this.mVssVolIndex = NO_NEW_INDEX;
            this.mDevice = device;
            this.mCaller = caller;
        }

        /* access modifiers changed from: package-private */
        public boolean hasVolumeIndex() {
            return this.mVssVolIndex != NO_NEW_INDEX;
        }

        /* access modifiers changed from: package-private */
        public int getVolumeIndex() throws IllegalStateException {
            Preconditions.checkState(this.mVssVolIndex != NO_NEW_INDEX);
            return this.mVssVolIndex;
        }
    }

    /* access modifiers changed from: package-private */
    public void postSetVolumeIndexOnDevice(int streamType, int vssVolIndex, int device, String caller) {
        sendMsg(this.mAudioHandler, 26, 2, 0, 0, new DeviceVolumeUpdate(streamType, vssVolIndex, device, caller), 0);
    }

    /* access modifiers changed from: package-private */
    public void postApplyVolumeOnDevice(int streamType, int device, String caller) {
        sendMsg(this.mAudioHandler, 26, 2, 0, 0, new DeviceVolumeUpdate(streamType, device, caller), 0);
    }

    /* access modifiers changed from: private */
    public void onSetVolumeIndexOnDevice(DeviceVolumeUpdate update) {
        VolumeStreamState streamState = this.mStreamStates[update.mStreamType];
        if (update.hasVolumeIndex()) {
            int index = update.getVolumeIndex();
            streamState.setIndex(index, update.mDevice, update.mCaller);
            AudioEventLogger audioEventLogger = sVolumeLogger;
            audioEventLogger.log(new AudioEventLogger.StringEvent(update.mCaller + " dev:0x" + Integer.toHexString(update.mDevice) + " volIdx:" + index));
        } else {
            AudioEventLogger audioEventLogger2 = sVolumeLogger;
            audioEventLogger2.log(new AudioEventLogger.StringEvent(update.mCaller + " update vol on dev:0x" + Integer.toHexString(update.mDevice)));
        }
        setDeviceVolume(streamState, update.mDevice);
    }

    /* access modifiers changed from: package-private */
    public void setDeviceVolume(VolumeStreamState streamState, int device) {
        boolean isAvrcpAbsVolSupported = this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported();
        synchronized (VolumeStreamState.class) {
            streamState.applyDeviceVolume_syncVSS(device, isAvrcpAbsVolSupported);
            for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                if (streamType != streamState.mStreamType && mStreamVolumeAlias[streamType] == streamState.mStreamType) {
                    int streamDevice = getDeviceForStream(streamType);
                    if (!(device == streamDevice || !isAvrcpAbsVolSupported || (device & 896) == 0)) {
                        this.mStreamStates[streamType].applyDeviceVolume_syncVSS(device, isAvrcpAbsVolSupported);
                    }
                    this.mStreamStates[streamType].applyDeviceVolume_syncVSS(streamDevice, isAvrcpAbsVolSupported);
                }
            }
        }
        sendMsg(this.mAudioHandler, 1, 2, device, 0, streamState, 500);
    }

    /* access modifiers changed from: private */
    public class AudioHandler extends Handler {
        private AudioHandler() {
        }

        private void setAllVolumes(VolumeStreamState streamState) {
            streamState.applyAllVolumes();
            for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                if (streamType != streamState.mStreamType && AudioService.mStreamVolumeAlias[streamType] == streamState.mStreamType) {
                    AudioService.this.mStreamStates[streamType].applyAllVolumes();
                }
            }
        }

        private void persistVolume(VolumeStreamState streamState, int device) {
            if (!AudioService.this.mUseFixedVolume) {
                if ((!AudioService.this.mIsSingleVolume || streamState.mStreamType == 3) && streamState.hasValidSettingsName()) {
                    Settings.System.putIntForUser(AudioService.this.mContentResolver, streamState.getSettingNameForDevice(device), (streamState.getIndex(device) + 5) / 10, -2);
                }
            }
        }

        private void persistRingerMode(int ringerMode) {
            if (!AudioService.this.mUseFixedVolume) {
                Settings.Global.putInt(AudioService.this.mContentResolver, "mode_ringer", ringerMode);
                Settings.System.putIntForUser(AudioService.this.mContentResolver, AudioService.OPPO_MODE_RINGER, ringerMode, -2);
            }
        }

        private String getSoundEffectFilePath(int effectType) {
            String filePath = Environment.getProductDirectory() + AudioService.SOUND_EFFECTS_PATH + ((String) AudioService.SOUND_EFFECT_FILES.get(AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][0]));
            if (new File(filePath).isFile()) {
                return filePath;
            }
            return Environment.getRootDirectory() + AudioService.SOUND_EFFECTS_PATH + ((String) AudioService.SOUND_EFFECT_FILES.get(AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][0]));
        }

        /* JADX WARNING: Code restructure failed: missing block: B:74:0x0204, code lost:
            if (r5 != 0) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:97:?, code lost:
            return true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:98:?, code lost:
            return false;
         */
        private boolean onLoadSoundEffects() {
            int status;
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (!AudioService.this.mSystemReady) {
                    Log.w(AudioService.TAG, "onLoadSoundEffects() called before boot complete");
                    return false;
                } else if (AudioService.this.mSoundPool != null) {
                    return true;
                } else {
                    AudioService.this.loadTouchSoundAssets();
                    SoundPool unused = AudioService.this.mSoundPool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
                    SoundPoolCallback unused2 = AudioService.this.mSoundPoolCallBack = null;
                    SoundPoolListenerThread unused3 = AudioService.this.mSoundPoolListenerThread = new SoundPoolListenerThread();
                    AudioService.this.mSoundPoolListenerThread.start();
                    int attempts = 3;
                    while (true) {
                        if (AudioService.this.mSoundPoolCallBack != null) {
                            break;
                        }
                        int attempts2 = attempts - 1;
                        if (attempts <= 0) {
                            break;
                        }
                        try {
                            AudioService.this.mSoundEffectsLock.wait(5000);
                        } catch (InterruptedException e) {
                            Log.w(AudioService.TAG, "Interrupted while waiting sound pool listener thread.");
                        }
                        attempts = attempts2;
                    }
                    if (AudioService.this.mSoundPoolCallBack == null) {
                        Log.w(AudioService.TAG, "onLoadSoundEffects() SoundPool listener or thread creation error");
                        if (AudioService.this.mSoundPoolLooper != null) {
                            AudioService.this.mSoundPoolLooper.quit();
                            Looper unused4 = AudioService.this.mSoundPoolLooper = null;
                        }
                        SoundPoolListenerThread unused5 = AudioService.this.mSoundPoolListenerThread = null;
                        AudioService.this.mSoundPool.release();
                        SoundPool unused6 = AudioService.this.mSoundPool = null;
                        return false;
                    }
                    int[] poolId = new int[AudioService.SOUND_EFFECT_FILES.size()];
                    for (int fileIdx = 0; fileIdx < AudioService.SOUND_EFFECT_FILES.size(); fileIdx++) {
                        poolId[fileIdx] = -1;
                    }
                    int numSamples = 0;
                    for (int effect = 0; effect < 10; effect++) {
                        if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] != 0) {
                            if (poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] == -1) {
                                String filePath = getSoundEffectFilePath(effect);
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
                        int attempts3 = 3;
                        status = 1;
                        while (true) {
                            if (status != 1) {
                                break;
                            }
                            int attempts4 = attempts3 - 1;
                            if (attempts3 <= 0) {
                                break;
                            }
                            try {
                                AudioService.this.mSoundEffectsLock.wait(5000);
                                status = AudioService.this.mSoundPoolCallBack.status();
                                attempts3 = attempts4;
                            } catch (InterruptedException e2) {
                                Log.w(AudioService.TAG, "Interrupted while waiting sound pool callback.");
                                attempts3 = attempts4;
                            }
                        }
                    } else {
                        status = -1;
                    }
                    if (AudioService.this.mSoundPoolLooper != null) {
                        AudioService.this.mSoundPoolLooper.quit();
                        Looper unused7 = AudioService.this.mSoundPoolLooper = null;
                    }
                    SoundPoolListenerThread unused8 = AudioService.this.mSoundPoolListenerThread = null;
                    if (status != 0) {
                        Log.w(AudioService.TAG, "onLoadSoundEffects(), Error " + status + " while loading samples");
                        for (int effect2 = 0; effect2 < 10; effect2++) {
                            if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect2][1] > 0) {
                                AudioService.this.SOUND_EFFECT_FILES_MAP[effect2][1] = -1;
                            }
                        }
                        AudioService.this.mSoundPool.release();
                        SoundPool unused9 = AudioService.this.mSoundPool = null;
                    }
                }
            }
        }

        private void onUnloadSoundEffects() {
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (AudioService.this.mSoundPool != null) {
                    int[] poolId = new int[AudioService.SOUND_EFFECT_FILES.size()];
                    for (int fileIdx = 0; fileIdx < AudioService.SOUND_EFFECT_FILES.size(); fileIdx++) {
                        poolId[fileIdx] = 0;
                    }
                    for (int effect = 0; effect < 10; effect++) {
                        if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] > 0) {
                            if (poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] == 0) {
                                AudioService.this.mSoundPool.unload(AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1]);
                                AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] = -1;
                                poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] = -1;
                            }
                        }
                    }
                    AudioService.this.mSoundPool.release();
                    SoundPool unused = AudioService.this.mSoundPool = null;
                }
            }
        }

        private void onPlaySoundEffect(int effectType, int volume) {
            float volFloat;
            synchronized (AudioService.this.mSoundEffectsLock) {
                onLoadSoundEffects();
                if (AudioService.this.mSoundPool != null) {
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
                            mediaPlayer.setDataSource(getSoundEffectFilePath(effectType));
                            mediaPlayer.setAudioStreamType(1);
                            mediaPlayer.prepare();
                            mediaPlayer.setVolume(volFloat);
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                /* class com.android.server.audio.AudioService.AudioHandler.AnonymousClass1 */

                                public void onCompletion(MediaPlayer mp) {
                                    AudioHandler.this.cleanupPlayer(mp);
                                }
                            });
                            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                /* class com.android.server.audio.AudioService.AudioHandler.AnonymousClass2 */

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
        }

        /* access modifiers changed from: private */
        public void cleanupPlayer(MediaPlayer mp) {
            if (mp != null) {
                try {
                    mp.stop();
                    mp.release();
                } catch (IllegalStateException ex) {
                    Log.w(AudioService.TAG, "MediaPlayer IllegalStateException: " + ex);
                }
            }
        }

        private void onPersistSafeVolumeState(int state) {
            Settings.Global.putInt(AudioService.this.mContentResolver, "audio_safe_volume_state", state);
        }

        private void onNotifyVolumeEvent(IAudioPolicyCallback apc, int direction) {
            try {
                apc.notifyVolumeAdjust(direction);
            } catch (Exception e) {
            }
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                boolean z = true;
                if (i == 1) {
                    persistVolume((VolumeStreamState) msg.obj, msg.arg1);
                } else if (i == 3) {
                    persistRingerMode(AudioService.this.getRingerModeInternal());
                } else if (i == 4) {
                    AudioService.this.onAudioServerDied();
                } else if (i != 5) {
                    int i2 = 0;
                    if (i == 7) {
                        boolean loaded = onLoadSoundEffects();
                        if (msg.obj != null) {
                            LoadSoundEffectReply reply = (LoadSoundEffectReply) msg.obj;
                            synchronized (reply) {
                                if (!loaded) {
                                    i2 = -1;
                                }
                                reply.mStatus = i2;
                                reply.notify();
                            }
                        }
                    } else if (i == 8) {
                        String eventSource = (String) msg.obj;
                        int useCase = msg.arg1;
                        int config = msg.arg2;
                        if (useCase == 1) {
                            Log.wtf(AudioService.TAG, "Invalid force use FOR_MEDIA in AudioService from " + eventSource);
                            return;
                        }
                        AudioService.sForceUseLogger.log(new AudioServiceEvents.ForceUseEvent(useCase, config, eventSource));
                        AudioSystem.setForceUse(useCase, config);
                    } else if (i == 61) {
                        AudioService.this.onOppoRestoreVolumeBeforeSafeMediaVolume();
                    } else if (i != 100) {
                        switch (i) {
                            case 10:
                                setAllVolumes((VolumeStreamState) msg.obj);
                                return;
                            case 11:
                                AudioService.this.onCheckMusicActive((String) msg.obj);
                                return;
                            case 12:
                            case 13:
                                AudioService audioService = AudioService.this;
                                if (msg.what != 13) {
                                    z = false;
                                }
                                audioService.onConfigureSafeVolume(z, (String) msg.obj);
                                return;
                            case 14:
                                onPersistSafeVolumeState(msg.arg1);
                                return;
                            case 15:
                                onUnloadSoundEffects();
                                return;
                            case 16:
                                AudioService.this.onSystemReady();
                                return;
                            case 17:
                                Settings.Secure.putIntForUser(AudioService.this.mContentResolver, "unsafe_volume_music_active_ms", msg.arg1, -2);
                                return;
                            case 18:
                                AudioService.this.onUnmuteStream(msg.arg1, msg.arg2);
                                return;
                            case 19:
                                AudioService.this.onDynPolicyMixStateUpdate((String) msg.obj, msg.arg1);
                                return;
                            case 20:
                                AudioService.this.onIndicateSystemReady();
                                return;
                            default:
                                switch (i) {
                                    case 22:
                                        onNotifyVolumeEvent((IAudioPolicyCallback) msg.obj, msg.arg1);
                                        return;
                                    case 23:
                                        AudioService audioService2 = AudioService.this;
                                        if (msg.arg1 != 1) {
                                            z = false;
                                        }
                                        audioService2.onDispatchAudioServerStateChange(z);
                                        return;
                                    case 24:
                                        AudioService.this.onEnableSurroundFormats((ArrayList) msg.obj);
                                        return;
                                    case 25:
                                        AudioService.this.onUpdateRingerModeServiceInt();
                                        return;
                                    case 26:
                                        AudioService.this.onSetVolumeIndexOnDevice((DeviceVolumeUpdate) msg.obj);
                                        return;
                                    case 27:
                                        AudioService.this.onObserveDevicesForAllStreams();
                                        return;
                                    case 28:
                                        AudioService.this.onCheckVolumeCecOnHdmiConnection(msg.arg1, (String) msg.obj);
                                        return;
                                    case 29:
                                        AudioService.this.onPlaybackConfigChange((List) msg.obj);
                                        return;
                                    default:
                                        return;
                                }
                        }
                    } else {
                        PlaybackActivityMonitor access$4900 = AudioService.this.mPlaybackMonitor;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        access$4900.disableAudioForUid(z, msg.arg2);
                        AudioService.this.mAudioEventWakeLock.release();
                    }
                } else {
                    onPlaySoundEffect(msg.arg1, msg.arg2);
                }
            } else {
                AudioService.this.setDeviceVolume((VolumeStreamState) msg.obj, msg.arg1);
            }
        }
    }

    private class SettingsObserver extends ContentObserver {
        SettingsObserver() {
            super(new Handler());
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("zen_mode"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("zen_mode_config_etag"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.System.getUriFor("mode_ringer_streams_affected"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("dock_audio_media_enabled"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.System.getUriFor("master_mono"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.System.getUriFor("master_balance"), false, this);
            int unused = AudioService.this.mEncodedSurroundMode = Settings.Global.getInt(AudioService.this.mContentResolver, "encoded_surround_output", 0);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("encoded_surround_output"), false, this);
            String unused2 = AudioService.this.mEnabledSurroundFormats = Settings.Global.getString(AudioService.this.mContentResolver, "encoded_surround_output_enabled_formats");
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("encoded_surround_output_enabled_formats"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor("voice_interaction_service"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor("rtt_calling_mode"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor(AudioService.AUDIO_INPUT_CHANNEL), false, this);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (AudioService.this.mSettingsLock) {
                if (AudioService.this.updateRingerAndZenModeAffectedStreams()) {
                    AudioService.this.setRingerModeInt(AudioService.this.getRingerModeInternal(), false);
                }
                AudioService.this.readDockAudioSettings(AudioService.this.mContentResolver);
                AudioService.this.updateMasterMono(AudioService.this.mContentResolver);
                AudioService.this.updateMasterBalance(AudioService.this.mContentResolver);
                updateEncodedSurroundOutput();
                AudioService.this.sendEnabledSurroundFormats(AudioService.this.mContentResolver, AudioService.this.mSurroundModeChanged);
                AudioService.this.updateAssistantUId(false);
                AudioService.this.updateRttEanbled(AudioService.this.mContentResolver);
                AudioService.this.updateInputDevice(AudioService.this.mContentResolver);
            }
        }

        private void updateEncodedSurroundOutput() {
            int newSurroundMode = Settings.Global.getInt(AudioService.this.mContentResolver, "encoded_surround_output", 0);
            if (AudioService.this.mEncodedSurroundMode != newSurroundMode) {
                AudioService.this.sendEncodedSurroundMode(newSurroundMode, "SettingsObserver");
                AudioService.this.mDeviceBroker.toggleHdmiIfConnected_Async();
                int unused = AudioService.this.mEncodedSurroundMode = newSurroundMode;
                boolean unused2 = AudioService.this.mSurroundModeChanged = true;
                return;
            }
            boolean unused3 = AudioService.this.mSurroundModeChanged = false;
        }
    }

    public void avrcpSupportsAbsoluteVolume(String address, boolean support) {
        AudioEventLogger audioEventLogger = sVolumeLogger;
        audioEventLogger.log(new AudioEventLogger.StringEvent("avrcpSupportsAbsoluteVolume addr=" + address + " support=" + support));
        this.mDeviceBroker.setAvrcpAbsoluteVolumeSupported(support);
        sendMsg(this.mAudioHandler, 0, 2, 128, 0, this.mStreamStates[3], 0);
    }

    /* access modifiers changed from: package-private */
    public boolean hasMediaDynamicPolicy() {
        synchronized (this.mAudioPolicies) {
            if (this.mAudioPolicies.isEmpty()) {
                return false;
            }
            for (AudioPolicyProxy app : this.mAudioPolicies.values()) {
                if (app.hasMixAffectingUsage(1)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void checkMusicActive(int deviceType, String caller) {
        if ((this.mSafeMediaVolumeDevices & deviceType) != 0) {
            sendMsg(this.mAudioHandler, 11, 0, 0, 0, caller, 60000);
        }
    }

    private class AudioServiceBroadcastReceiver extends BroadcastReceiver {
        private AudioServiceBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            int config;
            String action = intent.getAction();
            if (action.equals("android.intent.action.DOCK_EVENT")) {
                int dockState = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
                if (dockState == 1) {
                    config = 7;
                } else if (dockState == 2) {
                    config = 6;
                } else if (dockState == 3) {
                    config = 8;
                } else if (dockState != 4) {
                    config = 0;
                } else {
                    config = 9;
                }
                if (!(dockState == 3 || (dockState == 0 && AudioService.this.mDockState == 3))) {
                    AudioService.this.mDeviceBroker.setForceUse_Async(3, config, "ACTION_DOCK_EVENT intent");
                }
                int unused = AudioService.this.mDockState = dockState;
            } else if (action.equals("android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED") || action.equals("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED")) {
                AudioService.this.mDeviceBroker.receiveBtEvent(intent);
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
                    AudioService.this.mDeviceBroker.postBroadcastBecomingNoisy();
                }
                boolean unused2 = AudioService.this.mUserSwitchedReceived = true;
                AudioService.this.mMediaFocusControl.discardAudioFocusOwner();
                AudioService.this.readAudioSettings(true);
                AudioService.sendMsg(AudioService.this.mAudioHandler, 10, 2, 0, 0, AudioService.this.mStreamStates[3], 0);
                int currentUser = AudioService.this.getCurrentUserId();
                Log.d(AudioService.TAG, "ACTION_USER_SWITCHED notify dolby when user switched currentUser=" + currentUser);
                Intent broadcast = new Intent("android.Multimedia.dolby.USER_SWITCHED");
                broadcast.putExtra("user_switched_userid", currentUser);
                AudioService.this.sendBroadcastToAll(broadcast);
            } else if (action.equals("android.intent.action.USER_BACKGROUND")) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userId >= 0) {
                    AudioService.this.killBackgroundUserProcessesWithRecordAudioPermission(UserManagerService.getInstance().getUserInfo(userId));
                }
                UserManagerService.getInstance().setUserRestriction("no_record_audio", true, userId);
            } else if (action.equals("android.intent.action.USER_FOREGROUND")) {
                UserManagerService.getInstance().setUserRestriction("no_record_audio", false, intent.getIntExtra("android.intent.extra.user_handle", -1));
            } else if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                if (state == 10 || state == 13) {
                    Log.d(AudioService.TAG, "onReceive BluetoothAdapter state=" + state);
                    AudioService.this.mDeviceBroker.disconnectAllBluetoothProfiles();
                }
            } else if (action.equals("android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION") || action.equals("android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION")) {
                AudioService.this.handleAudioEffectBroadcast(context, intent);
            } else if (action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                int[] suspendedUids = intent.getIntArrayExtra("android.intent.extra.changed_uid_list");
                String[] suspendedPackages = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                if (suspendedPackages != null && suspendedUids != null && suspendedPackages.length == suspendedUids.length) {
                    for (int i = 0; i < suspendedUids.length; i++) {
                        if (!TextUtils.isEmpty(suspendedPackages[i])) {
                            AudioService.this.mMediaFocusControl.noFocusForSuspendedApp(suspendedPackages[i], suspendedUids[i]);
                        }
                    }
                }
            }
        }
    }

    private class AudioServiceUserRestrictionsListener implements UserManagerInternal.UserRestrictionsListener {
        private AudioServiceUserRestrictionsListener() {
        }

        public void onUserRestrictionsChanged(int userId, Bundle newRestrictions, Bundle prevRestrictions) {
            boolean wasRestricted = prevRestrictions.getBoolean("no_unmute_microphone");
            boolean isRestricted = newRestrictions.getBoolean("no_unmute_microphone");
            if (wasRestricted != isRestricted) {
                AudioService.this.setMicrophoneMuteNoCallerCheck(isRestricted, userId);
            }
            boolean isRestricted2 = true;
            boolean wasRestricted2 = prevRestrictions.getBoolean("no_adjust_volume") || prevRestrictions.getBoolean("disallow_unmute_device");
            if (!newRestrictions.getBoolean("no_adjust_volume") && !newRestrictions.getBoolean("disallow_unmute_device")) {
                isRestricted2 = false;
            }
            if (wasRestricted2 != isRestricted2) {
                AudioService.this.setMasterMuteInternalNoCallerCheck(isRestricted2, 0, userId);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleAudioEffectBroadcast(Context context, Intent intent) {
        ResolveInfo ri;
        String target = intent.getPackage();
        if (target != null) {
            Log.w(TAG, "effect broadcast already targeted to " + target);
            return;
        }
        intent.addFlags(32);
        List<ResolveInfo> ril = context.getPackageManager().queryBroadcastReceivers(intent, 0);
        if (ril == null || ril.size() == 0 || (ri = ril.get(0)) == null || ri.activityInfo == null || ri.activityInfo.packageName == null) {
            Log.w(TAG, "couldn't find receiver package for effect intent");
            return;
        }
        intent.setPackage(ri.activityInfo.packageName);
        context.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    public void killBackgroundUserProcessesWithRecordAudioPermission(UserInfo oldUser) {
        PackageManager pm = this.mContext.getPackageManager();
        ComponentName homeActivityName = null;
        if (!oldUser.isManagedProfile()) {
            homeActivityName = ((ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class)).getHomeActivityForUser(oldUser.id);
        }
        try {
            List<PackageInfo> packages = AppGlobals.getPackageManager().getPackagesHoldingPermissions(new String[]{"android.permission.RECORD_AUDIO"}, 0, oldUser.id).getList();
            for (int j = packages.size() - 1; j >= 0; j--) {
                PackageInfo pkg = packages.get(j);
                if (UserHandle.getAppId(pkg.applicationInfo.uid) >= 10000 && pm.checkPermission("android.permission.INTERACT_ACROSS_USERS", pkg.packageName) != 0 && (homeActivityName == null || !pkg.packageName.equals(homeActivityName.getPackageName()) || !pkg.applicationInfo.isSystemApp())) {
                    try {
                        int uid = pkg.applicationInfo.uid;
                        ActivityManager.getService().killUid(UserHandle.getAppId(uid), UserHandle.getUserId(uid), "killBackgroundUserProcessesWithAudioRecordPermission");
                    } catch (RemoteException e) {
                        Log.w(TAG, "Error calling killUid", e);
                    }
                }
            }
        } catch (RemoteException e2) {
            throw new AndroidRuntimeException(e2);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x003f, code lost:
        return false;
     */
    private boolean forceFocusDuckingForAccessibility(AudioAttributes aa, int request, int uid) {
        Bundle extraInfo;
        if (aa == null || aa.getUsage() != 11 || request != 3 || (extraInfo = aa.getBundle()) == null || !extraInfo.getBoolean("a11y_force_ducking")) {
            return false;
        }
        if (uid == 0) {
            return true;
        }
        synchronized (this.mAccessibilityServiceUidsLock) {
            if (this.mAccessibilityServiceUids != null) {
                int callingUid = Binder.getCallingUid();
                for (int i = 0; i < this.mAccessibilityServiceUids.length; i++) {
                    if (this.mAccessibilityServiceUids[i] == callingUid) {
                        return true;
                    }
                }
            }
        }
    }

    public int requestAudioFocus(AudioAttributes aa, int durationHint, IBinder cb, IAudioFocusDispatcher fd, String clientId, String callingPackageName, int flags, IAudioPolicyCallback pcb, int sdk) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(14, Binder.getCallingUid());
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
        if (callingPackageName != null && clientId != null) {
            if (aa != null) {
                return this.mMediaFocusControl.requestAudioFocus(aa, durationHint, cb, fd, clientId, callingPackageName, flags, sdk, forceFocusDuckingForAccessibility(aa, durationHint, Binder.getCallingUid()));
            }
        }
        Log.e(TAG, "Invalid null parameter to request audio focus");
        return 0;
    }

    public int abandonAudioFocus(IAudioFocusDispatcher fd, String clientId, AudioAttributes aa, String callingPackageName) {
        return this.mMediaFocusControl.abandonAudioFocus(fd, clientId, aa, callingPackageName);
    }

    public void unregisterAudioFocusClient(String clientId) {
        this.mMediaFocusControl.unregisterAudioFocusClient(clientId);
    }

    public int getCurrentAudioFocus() {
        return this.mMediaFocusControl.getCurrentAudioFocus();
    }

    public int getFocusRampTimeMs(int focusGain, AudioAttributes attr) {
        MediaFocusControl mediaFocusControl = this.mMediaFocusControl;
        return MediaFocusControl.getFocusRampTimeMs(focusGain, attr);
    }

    /* access modifiers changed from: package-private */
    public boolean hasAudioFocusUsers() {
        return this.mMediaFocusControl.hasAudioFocusUsers();
    }

    private boolean isJPVersion() {
        return SystemProperties.get("ro.oppo.regionmark", "0").equals("JP");
    }

    private boolean readCameraSoundForced() {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.media.camerasound.forced")) {
            Log.d(TAG, "set CameraSoundForced to true for cmcc model");
            return true;
        } else if (this.mIsJPVersion) {
            Log.d(TAG, "set CameraSoundForced to true for JP version");
            return true;
        } else {
            Log.d(TAG, "set CameraSoundForced to false by default");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void handleConfigurationChanged(Context context) {
        try {
            Configuration config = context.getResources().getConfiguration();
            sendMsg(this.mAudioHandler, 12, 0, 0, 0, TAG, 0);
            boolean cameraSoundForced = readCameraSoundForced();
            synchronized (this.mSettingsLock) {
                int i = 0;
                boolean cameraSoundForcedChanged = cameraSoundForced != this.mCameraSoundForced;
                this.mCameraSoundForced = cameraSoundForced;
                if (cameraSoundForcedChanged) {
                    if (!this.mIsSingleVolume) {
                        synchronized (VolumeStreamState.class) {
                            VolumeStreamState s = this.mStreamStates[7];
                            if (cameraSoundForced) {
                                s.setAllIndexesToMax();
                                this.mRingerModeAffectedStreams &= -129;
                            } else {
                                s.setAllIndexes(this.mStreamStates[1], TAG);
                                this.mRingerModeAffectedStreams |= 128;
                            }
                        }
                        setRingerModeInt(getRingerModeInternal(), false);
                    }
                    AudioDeviceBroker audioDeviceBroker = this.mDeviceBroker;
                    if (cameraSoundForced) {
                        i = 11;
                    }
                    audioDeviceBroker.setForceUse_Async(4, i, "handleConfigurationChanged");
                    sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[7], 0);
                }
            }
            this.mVolumeController.setLayoutDirection(config.getLayoutDirection());
        } catch (Exception e) {
            Log.e(TAG, "Error handling configuration change: ", e);
        }
    }

    public void setRingtonePlayer(IRingtonePlayer player) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.REMOTE_AUDIO_PLAYBACK", null);
        this.mRingtonePlayer = player;
    }

    public IRingtonePlayer getRingtonePlayer() {
        return this.mRingtonePlayer;
    }

    public AudioRoutesInfo startWatchingRoutes(IAudioRoutesObserver observer) {
        return this.mDeviceBroker.startWatchingRoutes(observer);
    }

    private int safeMediaVolumeIndex(int device) {
        if ((this.mSafeMediaVolumeDevices & device) == 0) {
            return MAX_STREAM_VOLUME[3];
        }
        if (device == 67108864) {
            return this.mSafeUsbMediaVolumeIndex;
        }
        return this.mSafeMediaVolumeIndex;
    }

    private void setSafeMediaVolumeEnabled(boolean on, String caller) {
        synchronized (this.mSafeMediaVolumeStateLock) {
            if (!(this.mSafeMediaVolumeState == 0 || this.mSafeMediaVolumeState == 1)) {
                if (on && this.mSafeMediaVolumeState == 2) {
                    this.mSafeMediaVolumeState = 3;
                    sendBroadcastToAll(new Intent("android.media.action.SET_SAFE_VOLUME"));
                    enforceSafeMediaVolume(caller);
                } else if (!on && this.mSafeMediaVolumeState == 3) {
                    this.mSafeMediaVolumeState = 2;
                    this.mMusicActiveMs = 1;
                    saveMusicActiveMs();
                    sendMsg(this.mAudioHandler, 11, 0, 0, 0, caller, 60000);
                }
            }
        }
    }

    private void enforceSafeMediaVolume(String caller) {
        VolumeStreamState streamState = this.mStreamStates[3];
        int i = 0;
        int devices = this.mSafeMediaVolumeDevices;
        while (devices != 0) {
            int i2 = i + 1;
            int device = 1 << i;
            if ((device & devices) == 0) {
                i = i2;
            } else {
                int index = streamState.getIndex(device);
                if (index > safeMediaVolumeIndex(device)) {
                    streamState.mMediaVolumeIndexMap.put(device, index);
                    streamState.setIndex(safeMediaVolumeIndex(device), device, caller);
                    sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
                    boolean isAvrcpAbsVolSupported = this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported();
                    if (AudioSystem.isStreamActive(3, 0) && (device & 896) != 0 && isAvrcpAbsVolSupported) {
                        this.mDeviceBroker.postSetAvrcpAbsoluteVolumeIndex(streamState.getIndex(device) / 10);
                    }
                }
                devices &= ~device;
                i = i2;
            }
        }
    }

    private boolean checkSafeMediaVolume(int streamType, int index, int device) {
        synchronized (this.mSafeMediaVolumeStateLock) {
            if (this.mSafeMediaVolumeState != 3 || mStreamVolumeAlias[streamType] != 3 || (this.mSafeMediaVolumeDevices & device) == 0 || index <= safeMediaVolumeIndex(device)) {
                return true;
            }
            return false;
        }
    }

    public void disableSafeMediaVolume(String callingPackage) {
        enforceVolumeController("disable the safe media volume");
        synchronized (this.mSafeMediaVolumeStateLock) {
            setSafeMediaVolumeEnabled(false, callingPackage);
            if (this.mPendingVolumeCommand != null) {
                onSetStreamVolume(this.mPendingVolumeCommand.mStreamType, this.mPendingVolumeCommand.mIndex, this.mPendingVolumeCommand.mFlags, this.mPendingVolumeCommand.mDevice, callingPackage);
                this.mPendingVolumeCommand = null;
            }
        }
    }

    private class MyDisplayStatusCallback implements HdmiPlaybackClient.DisplayStatusCallback {
        private MyDisplayStatusCallback() {
        }

        public void onComplete(int status) {
            synchronized (AudioService.this.mHdmiClientLock) {
                if (AudioService.this.mHdmiManager != null) {
                    boolean unused = AudioService.this.mHdmiCecSink = status != -1;
                    if (AudioService.this.mHdmiCecSink) {
                        if (AudioService.DEBUG_VOL) {
                            Log.d(AudioService.TAG, "CEC sink: setting HDMI as full vol device");
                        }
                        AudioService.this.mFullVolumeDevices |= 1024;
                    } else {
                        if (AudioService.DEBUG_VOL) {
                            Log.d(AudioService.TAG, "TV, no CEC: setting HDMI as regular vol device");
                        }
                        AudioService.this.mFullVolumeDevices &= -1025;
                    }
                    AudioService.this.checkAddAllFixedVolumeDevices(1024, "HdmiPlaybackClient.DisplayStatusCallback");
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0036, code lost:
        return r0;
     */
    public int setHdmiSystemAudioSupported(boolean on) {
        int config;
        int device = 0;
        synchronized (this.mHdmiClientLock) {
            if (this.mHdmiManager != null) {
                if (this.mHdmiTvClient == null && this.mHdmiAudioSystemClient == null) {
                    Log.w(TAG, "Only Hdmi-Cec enabled TV or audio system device supportssystem audio mode.");
                    return 0;
                }
                if (this.mHdmiSystemAudioSupported != on) {
                    this.mHdmiSystemAudioSupported = on;
                    if (on) {
                        config = 12;
                    } else {
                        config = 0;
                    }
                    this.mDeviceBroker.setForceUse_Async(5, config, "setHdmiSystemAudioSupported");
                }
                device = getDevicesForStream(3);
            }
        }
    }

    public boolean isHdmiSystemAudioSupported() {
        return this.mHdmiSystemAudioSupported;
    }

    private void initA11yMonitoring() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        updateDefaultStreamOverrideDelay(accessibilityManager.isTouchExplorationEnabled());
        updateA11yVolumeAlias(accessibilityManager.isAccessibilityVolumeStreamActive());
        accessibilityManager.addTouchExplorationStateChangeListener(this, null);
        accessibilityManager.addAccessibilityServicesStateChangeListener(this, null);
    }

    public void onTouchExplorationStateChanged(boolean enabled) {
        updateDefaultStreamOverrideDelay(enabled);
    }

    private void updateDefaultStreamOverrideDelay(boolean touchExploreEnabled) {
        if (touchExploreEnabled) {
            sStreamOverrideDelayMs = 1000;
        } else {
            sStreamOverrideDelayMs = 0;
        }
        if (DEBUG_VOL) {
            Log.d(TAG, "Touch exploration enabled=" + touchExploreEnabled + " stream override delay is now " + sStreamOverrideDelayMs + " ms");
        }
    }

    public void onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager) {
        updateA11yVolumeAlias(accessibilityManager.isAccessibilityVolumeStreamActive());
    }

    private void updateA11yVolumeAlias(boolean a11VolEnabled) {
        if (DEBUG_VOL) {
            Log.d(TAG, "Accessibility volume enabled = " + a11VolEnabled);
        }
        if (sIndependentA11yVolume != a11VolEnabled) {
            sIndependentA11yVolume = a11VolEnabled;
            int i = 1;
            updateStreamVolumeAlias(true, TAG);
            VolumeController volumeController = this.mVolumeController;
            if (!sIndependentA11yVolume) {
                i = 0;
            }
            volumeController.setA11yMode(i);
            this.mVolumeController.postVolumeChanged(10, 0);
        }
    }

    public boolean isCameraSoundForced() {
        boolean z;
        synchronized (this.mSettingsLock) {
            z = this.mCameraSoundForced;
        }
        return z;
    }

    private void dumpRingerMode(PrintWriter pw) {
        pw.println("\nRinger mode: ");
        pw.println("- mode (internal) = " + RINGER_MODE_NAMES[this.mRingerMode]);
        pw.println("- mode (external) = " + RINGER_MODE_NAMES[this.mRingerModeExternal]);
        dumpRingerModeStreams(pw, "affected", this.mRingerModeAffectedStreams);
        dumpRingerModeStreams(pw, "muted", this.mRingerAndZenModeMutedStreams);
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

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            this.mMediaFocusControl.dump(pw);
            dumpStreamStates(pw);
            dumpRingerMode(pw);
            pw.println("\nAudio routes:");
            pw.print("  mMainType=0x");
            pw.println(Integer.toHexString(this.mDeviceBroker.getCurAudioRoutes().mainType));
            pw.print("  mBluetoothName=");
            pw.println(this.mDeviceBroker.getCurAudioRoutes().bluetoothName);
            pw.println("\nOther state:");
            pw.print("  mVolumeController=");
            pw.println(this.mVolumeController);
            pw.print("  mSafeMediaVolumeState=");
            pw.println(safeMediaVolumeStateToString(this.mSafeMediaVolumeState));
            pw.print("  mSafeMediaVolumeIndex=");
            pw.println(this.mSafeMediaVolumeIndex);
            pw.print("  mSafeUsbMediaVolumeIndex=");
            pw.println(this.mSafeUsbMediaVolumeIndex);
            pw.print("  mSafeUsbMediaVolumeDbfs=");
            pw.println(this.mSafeUsbMediaVolumeDbfs);
            pw.print("  sIndependentA11yVolume=");
            pw.println(sIndependentA11yVolume);
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
            pw.print("  mVolumePolicy=");
            pw.println(this.mVolumePolicy);
            pw.print("  mAvrcpAbsVolSupported=");
            pw.println(this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported());
            pw.print("  mIsSingleVolume=");
            pw.println(this.mIsSingleVolume);
            pw.print("  mUseFixedVolume=");
            pw.println(this.mUseFixedVolume);
            pw.print("  mFixedVolumeDevices=0x");
            pw.println(Integer.toHexString(this.mFixedVolumeDevices));
            pw.print("  mHdmiCecSink=");
            pw.println(this.mHdmiCecSink);
            pw.print("  mHdmiAudioSystemClient=");
            pw.println(this.mHdmiAudioSystemClient);
            pw.print("  mHdmiPlaybackClient=");
            pw.println(this.mHdmiPlaybackClient);
            pw.print("  mHdmiTvClient=");
            pw.println(this.mHdmiTvClient);
            pw.print("  mHdmiSystemAudioSupported=");
            pw.println(this.mHdmiSystemAudioSupported);
            dumpAudioPolicies(pw);
            this.mDynPolicyLogger.dump(pw);
            this.mPlaybackMonitor.dump(pw);
            this.mRecordMonitor.dump(pw);
            pw.println(StringUtils.LF);
            pw.println("\nEvent logs:");
            this.mModeLogger.dump(pw);
            pw.println(StringUtils.LF);
            sDeviceLogger.dump(pw);
            pw.println(StringUtils.LF);
            sForceUseLogger.dump(pw);
            pw.println(StringUtils.LF);
            sVolumeLogger.dump(pw);
        }
    }

    private static String safeMediaVolumeStateToString(int state) {
        if (state == 0) {
            return "SAFE_MEDIA_VOLUME_NOT_CONFIGURED";
        }
        if (state == 1) {
            return "SAFE_MEDIA_VOLUME_DISABLED";
        }
        if (state == 2) {
            return "SAFE_MEDIA_VOLUME_INACTIVE";
        }
        if (state != 3) {
            return null;
        }
        return "SAFE_MEDIA_VOLUME_ACTIVE";
    }

    private static void readAndSetLowRamDevice() {
        boolean isLowRamDevice = ActivityManager.isLowRamDeviceStatic();
        long totalMemory = 1073741824;
        try {
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            ActivityManager.getService().getMemoryInfo(info);
            totalMemory = info.totalMem;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot obtain MemoryInfo from ActivityManager, assume low memory device");
            isLowRamDevice = true;
        }
        int status = AudioSystem.setLowRamDevice(isLowRamDevice, totalMemory);
        if (status != 0) {
            Log.w(TAG, "AudioFlinger informed of device's low RAM attribute; status " + status);
        }
    }

    private void enforceVolumeController(String action) {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "Only SystemUI can " + action);
    }

    public void setVolumeController(final IVolumeController controller) {
        enforceVolumeController("set the volume controller");
        if (!this.mVolumeController.isSameBinder(controller)) {
            this.mVolumeController.postDismiss();
            if (controller != null) {
                try {
                    controller.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.android.server.audio.AudioService.AnonymousClass4 */

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
            this.mLongPressTimeout = Settings.Secure.getIntForUser(cr, "long_press_timeout", 500, -2);
        }

        public boolean suppressAdjustment(int resolvedStream, int flags, boolean isMute) {
            if (isMute || resolvedStream != 2 || this.mController == null) {
                return false;
            }
            long now = SystemClock.uptimeMillis();
            if ((flags & 1) == 0 || this.mVisible) {
                long j = this.mNextLongPress;
                if (j <= 0) {
                    return false;
                }
                if (now <= j) {
                    return true;
                }
                this.mNextLongPress = 0;
                return false;
            }
            if (this.mNextLongPress < now) {
                this.mNextLongPress = ((long) this.mLongPressTimeout) + now;
            }
            return true;
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
            if (controller == null) {
                return null;
            }
            return controller.asBinder();
        }

        public String toString() {
            return "VolumeController(" + asBinder() + ",mVisible=" + this.mVisible + ")";
        }

        public void postDisplaySafeVolumeWarning(int flags) {
            IVolumeController iVolumeController = this.mController;
            if (iVolumeController != null) {
                try {
                    iVolumeController.displaySafeVolumeWarning(flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling displaySafeVolumeWarning", e);
                }
            }
        }

        public void postVolumeChanged(int streamType, int flags) {
            IVolumeController iVolumeController = this.mController;
            if (iVolumeController != null) {
                try {
                    iVolumeController.volumeChanged(streamType, flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling volumeChanged", e);
                }
            }
        }

        public void postMasterMuteChanged(int flags) {
            IVolumeController iVolumeController = this.mController;
            if (iVolumeController != null) {
                try {
                    iVolumeController.masterMuteChanged(flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling masterMuteChanged", e);
                }
            }
        }

        public void setLayoutDirection(int layoutDirection) {
            IVolumeController iVolumeController = this.mController;
            if (iVolumeController != null) {
                try {
                    iVolumeController.setLayoutDirection(layoutDirection);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling setLayoutDirection", e);
                }
            }
        }

        public void postDismiss() {
            IVolumeController iVolumeController = this.mController;
            if (iVolumeController != null) {
                try {
                    iVolumeController.dismiss();
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling dismiss", e);
                }
            }
        }

        public void setA11yMode(int a11yMode) {
            IVolumeController iVolumeController = this.mController;
            if (iVolumeController != null) {
                try {
                    iVolumeController.setA11yMode(a11yMode);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling setA11Mode", e);
                }
            }
        }
    }

    final class AudioServiceInternal extends AudioManagerInternal {
        AudioServiceInternal() {
        }

        public void setRingerModeDelegate(AudioManagerInternal.RingerModeDelegate delegate) {
            AudioManagerInternal.RingerModeDelegate unused = AudioService.this.mRingerModeDelegate = delegate;
            if (AudioService.this.mRingerModeDelegate != null) {
                synchronized (AudioService.this.mSettingsLock) {
                    boolean unused2 = AudioService.this.updateRingerAndZenModeAffectedStreams();
                }
                setRingerModeInternal(getRingerModeInternal(), "AS.AudioService.setRingerModeDelegate");
            }
        }

        public void adjustSuggestedStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            AudioService.this.adjustSuggestedStreamVolume(direction, streamType, flags, callingPackage, callingPackage, uid);
        }

        public void adjustStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            if (direction != 0) {
                AudioEventLogger audioEventLogger = AudioService.sVolumeLogger;
                audioEventLogger.log(new AudioServiceEvents.VolumeEvent(5, streamType, direction, flags, callingPackage + " uid:" + uid));
            }
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

        public void silenceRingerModeInternal(String caller) {
            AudioService.this.silenceRingerModeInternal(caller);
        }

        public void updateRingerModeAffectedStreamsInternal() {
            synchronized (AudioService.this.mSettingsLock) {
                if (AudioService.this.updateRingerAndZenModeAffectedStreams()) {
                    AudioService.this.setRingerModeInt(getRingerModeInternal(), false);
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:14:0x0031 A[LOOP:0: B:14:0x0031->B:19:0x004a, LOOP_START, PHI: r2 
          PHI: (r2v2 'i' int) = (r2v0 'i' int), (r2v3 'i' int) binds: [B:13:0x002e, B:19:0x004a] A[DONT_GENERATE, DONT_INLINE]] */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x004f  */
        public void setAccessibilityServiceUids(IntArray uids) {
            boolean changed;
            synchronized (AudioService.this.mAccessibilityServiceUidsLock) {
                if (uids.size() == 0) {
                    int[] unused = AudioService.this.mAccessibilityServiceUids = null;
                } else {
                    int i = 0;
                    if (AudioService.this.mAccessibilityServiceUids != null) {
                        if (AudioService.this.mAccessibilityServiceUids.length == uids.size()) {
                            changed = false;
                            if (!changed) {
                                while (true) {
                                    if (i >= AudioService.this.mAccessibilityServiceUids.length) {
                                        break;
                                    } else if (uids.get(i) != AudioService.this.mAccessibilityServiceUids[i]) {
                                        changed = true;
                                        break;
                                    } else {
                                        i++;
                                    }
                                }
                            }
                            if (changed) {
                                int[] unused2 = AudioService.this.mAccessibilityServiceUids = uids.toArray();
                            }
                        }
                    }
                    changed = true;
                    if (!changed) {
                    }
                    if (changed) {
                    }
                }
                AudioSystem.setA11yServicesUids(AudioService.this.mAccessibilityServiceUids);
            }
        }
    }

    public String registerAudioPolicy(AudioPolicyConfig policyConfig, IAudioPolicyCallback pcb, boolean hasFocusListener, boolean isFocusPolicy, boolean isTestFocusPolicy, boolean isVolumeController, IMediaProjection projection) {
        HashMap<IBinder, AudioPolicyProxy> hashMap;
        AudioSystem.setDynamicPolicyCallback(this.mDynPolicyCallback);
        if (!isPolicyRegisterAllowed(policyConfig, isFocusPolicy || isTestFocusPolicy || hasFocusListener, isVolumeController, projection)) {
            Slog.w(TAG, "Permission denied to register audio policy for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", need MODIFY_AUDIO_ROUTING or MediaProjection that can project audio");
            return null;
        }
        AudioEventLogger audioEventLogger = this.mDynPolicyLogger;
        audioEventLogger.log(new AudioEventLogger.StringEvent("registerAudioPolicy for " + pcb.asBinder() + " with config:" + policyConfig).printLog(TAG));
        HashMap<IBinder, AudioPolicyProxy> hashMap2 = this.mAudioPolicies;
        synchronized (hashMap2) {
            try {
                if (this.mAudioPolicies.containsKey(pcb.asBinder())) {
                    Slog.e(TAG, "Cannot re-register policy");
                } else {
                    try {
                        hashMap = hashMap2;
                        try {
                            AudioPolicyProxy app = new AudioPolicyProxy(policyConfig, pcb, hasFocusListener, isFocusPolicy, isTestFocusPolicy, isVolumeController, projection);
                            pcb.asBinder().linkToDeath(app, 0);
                            String regId = app.getRegistrationId();
                            this.mAudioPolicies.put(pcb.asBinder(), app);
                            return regId;
                        } catch (RemoteException e) {
                            e = e;
                        } catch (IllegalStateException e2) {
                            e = e2;
                            Slog.w(TAG, "Audio policy registration failed for binder " + pcb, e);
                            return null;
                        } catch (Throwable th) {
                            e = th;
                            throw e;
                        }
                    } catch (RemoteException e3) {
                        e = e3;
                        hashMap = hashMap2;
                        Slog.w(TAG, "Audio policy registration failed, could not link to " + pcb + " binder death", e);
                        return null;
                    } catch (IllegalStateException e4) {
                        e = e4;
                        hashMap = hashMap2;
                        Slog.w(TAG, "Audio policy registration failed for binder " + pcb, e);
                        return null;
                    }
                }
            } catch (Throwable th2) {
                e = th2;
                hashMap = hashMap2;
                throw e;
            }
        }
        return null;
    }

    private boolean isPolicyRegisterAllowed(AudioPolicyConfig policyConfig, boolean hasFocusAccess, boolean isVolumeController, IMediaProjection projection) {
        boolean requireValidProjection = false;
        boolean requireCaptureAudioOrMediaOutputPerm = false;
        boolean requireModifyRouting = false;
        if (hasFocusAccess || isVolumeController) {
            requireModifyRouting = false | true;
        } else if (policyConfig.getMixes().isEmpty()) {
            requireModifyRouting = false | true;
        }
        Iterator it = policyConfig.getMixes().iterator();
        while (it.hasNext()) {
            AudioMix mix = (AudioMix) it.next();
            if (mix.getRule().allowPrivilegedPlaybackCapture()) {
                requireCaptureAudioOrMediaOutputPerm |= true;
                String error = AudioMix.canBeUsedForPrivilegedCapture(mix.getFormat());
                if (error != null) {
                    Log.e(TAG, error);
                    return false;
                }
            }
            if (mix.getRouteFlags() != 3 || projection == null) {
                requireModifyRouting |= true;
            } else {
                requireValidProjection |= true;
            }
        }
        if (requireCaptureAudioOrMediaOutputPerm && !callerHasPermission("android.permission.CAPTURE_MEDIA_OUTPUT") && !callerHasPermission("android.permission.CAPTURE_AUDIO_OUTPUT")) {
            Log.e(TAG, "Privileged audio capture requires CAPTURE_MEDIA_OUTPUT or CAPTURE_AUDIO_OUTPUT system permission");
            return false;
        } else if (requireValidProjection && !canProjectAudio(projection)) {
            return false;
        } else {
            if (!requireModifyRouting || callerHasPermission("android.permission.MODIFY_AUDIO_ROUTING")) {
                return true;
            }
            Log.e(TAG, "Can not capture audio without MODIFY_AUDIO_ROUTING");
            return false;
        }
    }

    private boolean callerHasPermission(String permission) {
        return this.mContext.checkCallingPermission(permission) == 0;
    }

    private boolean canProjectAudio(IMediaProjection projection) {
        if (projection == null) {
            Log.e(TAG, "MediaProjection is null");
            return false;
        }
        IMediaProjectionManager projectionService = getProjectionService();
        if (projectionService == null) {
            Log.e(TAG, "Can't get service IMediaProjectionManager");
            return false;
        }
        try {
            if (!projectionService.isValidMediaProjection(projection)) {
                Log.w(TAG, "App passed invalid MediaProjection token");
                return false;
            }
            try {
                if (projection.canProjectAudio()) {
                    return true;
                }
                Log.w(TAG, "App passed MediaProjection that can not project audio");
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "Can't call .canProjectAudio() on valid IMediaProjection" + projection.asBinder(), e);
                return false;
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "Can't call .isValidMediaProjection() on IMediaProjectionManager" + projectionService.asBinder(), e2);
            return false;
        }
    }

    private IMediaProjectionManager getProjectionService() {
        if (this.mProjectionService == null) {
            this.mProjectionService = IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection"));
        }
        return this.mProjectionService;
    }

    public void unregisterAudioPolicyAsync(IAudioPolicyCallback pcb) {
        unregisterAudioPolicy(pcb);
    }

    public void unregisterAudioPolicy(IAudioPolicyCallback pcb) {
        if (pcb != null) {
            unregisterAudioPolicyInt(pcb);
        }
    }

    private void unregisterAudioPolicyInt(IAudioPolicyCallback pcb) {
        AudioEventLogger audioEventLogger = this.mDynPolicyLogger;
        audioEventLogger.log(new AudioEventLogger.StringEvent("unregisterAudioPolicyAsync for " + pcb.asBinder()).printLog(TAG));
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = this.mAudioPolicies.remove(pcb.asBinder());
            if (app == null) {
                Slog.w(TAG, "Trying to unregister unknown audio policy for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid());
                return;
            }
            pcb.asBinder().unlinkToDeath(app, 0);
            app.release();
        }
    }

    @GuardedBy({"mAudioPolicies"})
    private AudioPolicyProxy checkUpdateForPolicy(IAudioPolicyCallback pcb, String errorMsg) {
        if (!(this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0)) {
            Slog.w(TAG, errorMsg + " for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", need MODIFY_AUDIO_ROUTING");
            return null;
        }
        AudioPolicyProxy app = this.mAudioPolicies.get(pcb.asBinder());
        if (app != null) {
            return app;
        }
        Slog.w(TAG, errorMsg + " for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", unregistered policy");
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0042, code lost:
        return r2;
     */
    public int addMixForPolicy(AudioPolicyConfig policyConfig, IAudioPolicyCallback pcb) {
        if (DEBUG_AP) {
            Log.d(TAG, "addMixForPolicy for " + pcb.asBinder() + " with config:" + policyConfig);
        }
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = checkUpdateForPolicy(pcb, "Cannot add AudioMix in audio policy");
            int i = -1;
            if (app == null) {
                return -1;
            }
            if (app.addMixes(policyConfig.getMixes()) == 0) {
                i = 0;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0043, code lost:
        return r2;
     */
    public int removeMixForPolicy(AudioPolicyConfig policyConfig, IAudioPolicyCallback pcb) {
        if (DEBUG_AP) {
            Log.d(TAG, "removeMixForPolicy for " + pcb.asBinder() + " with config:" + policyConfig);
        }
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = checkUpdateForPolicy(pcb, "Cannot add AudioMix in audio policy");
            int i = -1;
            if (app == null) {
                return -1;
            }
            if (app.removeMixes(policyConfig.getMixes()) == 0) {
                i = 0;
            }
        }
    }

    public int setUidDeviceAffinity(IAudioPolicyCallback pcb, int uid, int[] deviceTypes, String[] deviceAddresses) {
        if (DEBUG_AP) {
            Log.d(TAG, "setUidDeviceAffinity for " + pcb.asBinder() + " uid:" + uid);
        }
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = checkUpdateForPolicy(pcb, "Cannot change device affinity in audio policy");
            if (app == null) {
                return -1;
            }
            if (!app.hasMixRoutedToDevices(deviceTypes, deviceAddresses)) {
                return -1;
            }
            int uidDeviceAffinities = app.setUidDeviceAffinities(uid, deviceTypes, deviceAddresses);
            return uidDeviceAffinities;
        }
    }

    public int removeUidDeviceAffinity(IAudioPolicyCallback pcb, int uid) {
        if (DEBUG_AP) {
            Log.d(TAG, "removeUidDeviceAffinity for " + pcb.asBinder() + " uid:" + uid);
        }
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = checkUpdateForPolicy(pcb, "Cannot remove device affinity in audio policy");
            if (app == null) {
                return -1;
            }
            int removeUidDeviceAffinities = app.removeUidDeviceAffinities(uid);
            return removeUidDeviceAffinities;
        }
    }

    public int setFocusPropertiesForPolicy(int duckingBehavior, IAudioPolicyCallback pcb) {
        if (DEBUG_AP) {
            Log.d(TAG, "setFocusPropertiesForPolicy() duck behavior=" + duckingBehavior + " policy " + pcb.asBinder());
        }
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = checkUpdateForPolicy(pcb, "Cannot change audio policy focus properties");
            if (app == null) {
                return -1;
            }
            if (!this.mAudioPolicies.containsKey(pcb.asBinder())) {
                Slog.e(TAG, "Cannot change audio policy focus properties, unregistered policy");
                return -1;
            }
            boolean z = true;
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
    }

    public boolean hasRegisteredDynamicPolicy() {
        boolean z;
        synchronized (this.mAudioPolicies) {
            z = !this.mAudioPolicies.isEmpty();
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void setExtVolumeController(IAudioPolicyCallback apc) {
        if (!this.mContext.getResources().getBoolean(17891463)) {
            Log.e(TAG, "Cannot set external volume controller: device not set for volume keys handled in PhoneWindowManager");
            return;
        }
        synchronized (this.mExtVolumeControllerLock) {
            if (this.mExtVolumeController != null && !this.mExtVolumeController.asBinder().pingBinder()) {
                Log.e(TAG, "Cannot set external volume controller: existing controller");
            }
            this.mExtVolumeController = apc;
        }
    }

    private void dumpAudioPolicies(PrintWriter pw) {
        pw.println("\nAudio policies:");
        synchronized (this.mAudioPolicies) {
            for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                pw.println(policy.toLogFriendlyString());
            }
        }
    }

    /* access modifiers changed from: private */
    public void onDynPolicyMixStateUpdate(String regId, int state) {
        if (DEBUG_AP) {
            Log.d(TAG, "onDynamicPolicyMixStateUpdate(" + regId + ", " + state + ")");
        }
        synchronized (this.mAudioPolicies) {
            for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                Iterator it = policy.getMixes().iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (((AudioMix) it.next()).getRegistration().equals(regId)) {
                            try {
                                policy.mPolicyCallback.notifyMixStateUpdate(regId, state);
                            } catch (RemoteException e) {
                                Log.e(TAG, "Can't call notifyMixStateUpdate() on IAudioPolicyCallback " + policy.mPolicyCallback.asBinder(), e);
                            }
                        }
                    }
                }
            }
        }
    }

    public void registerRecordingCallback(IRecordingConfigDispatcher rcdb) {
        this.mRecordMonitor.registerRecordingCallback(rcdb, this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0);
    }

    public void unregisterRecordingCallback(IRecordingConfigDispatcher rcdb) {
        this.mRecordMonitor.unregisterRecordingCallback(rcdb);
    }

    public List<AudioRecordingConfiguration> getActiveRecordingConfigurations() {
        return this.mRecordMonitor.getActiveRecordingConfigurations(this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0);
    }

    public int trackRecorder(IBinder recorder) {
        return this.mRecordMonitor.trackRecorder(recorder);
    }

    public void recorderEvent(int riid, int event) {
        this.mRecordMonitor.recorderEvent(riid, event);
    }

    public void releaseRecorder(int riid) {
        this.mRecordMonitor.releaseRecorder(riid);
    }

    public void disableRingtoneSync(int userId) {
        if (UserHandle.getCallingUserId() != userId) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "disable sound settings syncing for another profile");
        }
        long token = Binder.clearCallingIdentity();
        try {
            Settings.Secure.putIntForUser(this.mContentResolver, "sync_parent_sounds", 0, userId);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void registerPlaybackCallback(IPlaybackConfigDispatcher pcdb) {
        this.mPlaybackMonitor.registerPlaybackCallback(pcdb, this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0);
    }

    public void unregisterPlaybackCallback(IPlaybackConfigDispatcher pcdb) {
        this.mPlaybackMonitor.unregisterPlaybackCallback(pcdb);
    }

    public List<AudioPlaybackConfiguration> getActivePlaybackConfigurations() {
        return this.mPlaybackMonitor.getActivePlaybackConfigurations(this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0);
    }

    public int trackPlayer(PlayerBase.PlayerIdCard pic) {
        return this.mPlaybackMonitor.trackPlayer(pic);
    }

    public void playerAttributes(int piid, AudioAttributes attr) {
        this.mPlaybackMonitor.playerAttributes(piid, attr, Binder.getCallingUid());
    }

    public void playerEvent(int piid, int event) {
        this.mPlaybackMonitor.playerEvent(piid, event, Binder.getCallingUid());
    }

    public void playerHasOpPlayAudio(int piid, boolean hasOpPlayAudio) {
        this.mPlaybackMonitor.playerHasOpPlayAudio(piid, hasOpPlayAudio, Binder.getCallingUid());
    }

    public void releasePlayer(int piid) {
        this.mPlaybackMonitor.releasePlayer(piid, Binder.getCallingUid());
    }

    private static final class AudioDeviceArray {
        final String[] mDeviceAddresses;
        final int[] mDeviceTypes;

        AudioDeviceArray(int[] types, String[] addresses) {
            this.mDeviceTypes = types;
            this.mDeviceAddresses = addresses;
        }
    }

    public class AudioPolicyProxy extends AudioPolicyConfig implements IBinder.DeathRecipient {
        private static final String TAG = "AudioPolicyProxy";
        int mFocusDuckBehavior = 0;
        final boolean mHasFocusListener;
        boolean mIsFocusPolicy = false;
        boolean mIsTestFocusPolicy = false;
        final boolean mIsVolumeController;
        final IAudioPolicyCallback mPolicyCallback;
        final IMediaProjection mProjection;
        UnregisterOnStopCallback mProjectionCallback;
        final HashMap<Integer, AudioDeviceArray> mUidDeviceAffinities = new HashMap<>();

        private final class UnregisterOnStopCallback extends IMediaProjectionCallback.Stub {
            private UnregisterOnStopCallback() {
            }

            public void onStop() {
                AudioService.this.unregisterAudioPolicyAsync(AudioPolicyProxy.this.mPolicyCallback);
            }
        }

        AudioPolicyProxy(AudioPolicyConfig config, IAudioPolicyCallback token, boolean hasFocusListener, boolean isFocusPolicy, boolean isTestFocusPolicy, boolean isVolumeController, IMediaProjection projection) {
            super(config);
            setRegistration(new String(config.hashCode() + ":ap:" + AudioService.access$9708(AudioService.this)));
            this.mPolicyCallback = token;
            this.mHasFocusListener = hasFocusListener;
            this.mIsVolumeController = isVolumeController;
            this.mProjection = projection;
            if (this.mHasFocusListener) {
                AudioService.this.mMediaFocusControl.addFocusFollower(this.mPolicyCallback);
                if (isFocusPolicy) {
                    this.mIsFocusPolicy = true;
                    this.mIsTestFocusPolicy = isTestFocusPolicy;
                    AudioService.this.mMediaFocusControl.setFocusPolicy(this.mPolicyCallback, this.mIsTestFocusPolicy);
                }
            }
            if (this.mIsVolumeController) {
                AudioService.this.setExtVolumeController(this.mPolicyCallback);
            }
            if (this.mProjection != null) {
                this.mProjectionCallback = new UnregisterOnStopCallback();
                try {
                    this.mProjection.registerCallback(this.mProjectionCallback);
                } catch (RemoteException e) {
                    release();
                    throw new IllegalStateException("MediaProjection callback registration failed, could not link to " + projection + " binder death", e);
                }
            }
            int status = connectMixes();
            if (status != 0) {
                release();
                throw new IllegalStateException("Could not connect mix, error: " + status);
            }
        }

        public void binderDied() {
            synchronized (AudioService.this.mAudioPolicies) {
                Log.i(TAG, "audio policy " + this.mPolicyCallback + " died");
                release();
                AudioService.this.mAudioPolicies.remove(this.mPolicyCallback.asBinder());
            }
            if (this.mIsVolumeController) {
                synchronized (AudioService.this.mExtVolumeControllerLock) {
                    IAudioPolicyCallback unused = AudioService.this.mExtVolumeController = null;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public String getRegistrationId() {
            return getRegistration();
        }

        /* access modifiers changed from: package-private */
        public void release() {
            if (this.mIsFocusPolicy) {
                AudioService.this.mMediaFocusControl.unsetFocusPolicy(this.mPolicyCallback, this.mIsTestFocusPolicy);
            }
            if (this.mFocusDuckBehavior == 1) {
                AudioService.this.mMediaFocusControl.setDuckingInExtPolicyAvailable(false);
            }
            if (this.mHasFocusListener) {
                AudioService.this.mMediaFocusControl.removeFocusFollower(this.mPolicyCallback);
            }
            UnregisterOnStopCallback unregisterOnStopCallback = this.mProjectionCallback;
            if (unregisterOnStopCallback != null) {
                try {
                    this.mProjection.unregisterCallback(unregisterOnStopCallback);
                } catch (RemoteException e) {
                    Log.e(TAG, "Fail to unregister Audiopolicy callback from MediaProjection");
                }
            }
            long identity = Binder.clearCallingIdentity();
            AudioSystem.registerPolicyMixes(this.mMixes, false);
            Binder.restoreCallingIdentity(identity);
        }

        /* access modifiers changed from: package-private */
        public boolean hasMixAffectingUsage(int usage) {
            Iterator it = this.mMixes.iterator();
            while (it.hasNext()) {
                if (((AudioMix) it.next()).isAffectingUsage(usage)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean hasMixRoutedToDevices(int[] deviceTypes, String[] deviceAddresses) {
            for (int i = 0; i < deviceTypes.length; i++) {
                boolean hasDevice = false;
                Iterator it = this.mMixes.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (((AudioMix) it.next()).isRoutedToDevice(deviceTypes[i], deviceAddresses[i])) {
                            hasDevice = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!hasDevice) {
                    return false;
                }
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public int addMixes(ArrayList<AudioMix> mixes) {
            int registerPolicyMixes;
            synchronized (this.mMixes) {
                AudioSystem.registerPolicyMixes(this.mMixes, false);
                add(mixes);
                registerPolicyMixes = AudioSystem.registerPolicyMixes(this.mMixes, true);
            }
            return registerPolicyMixes;
        }

        /* access modifiers changed from: package-private */
        public int removeMixes(ArrayList<AudioMix> mixes) {
            int registerPolicyMixes;
            synchronized (this.mMixes) {
                AudioSystem.registerPolicyMixes(this.mMixes, false);
                remove(mixes);
                registerPolicyMixes = AudioSystem.registerPolicyMixes(this.mMixes, true);
            }
            return registerPolicyMixes;
        }

        /* access modifiers changed from: package-private */
        public int connectMixes() {
            long identity = Binder.clearCallingIdentity();
            int status = AudioSystem.registerPolicyMixes(this.mMixes, true);
            Binder.restoreCallingIdentity(identity);
            return status;
        }

        /* access modifiers changed from: package-private */
        public int setUidDeviceAffinities(int uid, int[] types, String[] addresses) {
            Integer Uid = new Integer(uid);
            if (this.mUidDeviceAffinities.remove(Uid) != null) {
                long identity = Binder.clearCallingIdentity();
                int res = AudioSystem.removeUidDeviceAffinities(uid);
                Binder.restoreCallingIdentity(identity);
                if (res != 0) {
                    Log.e(TAG, "AudioSystem. removeUidDeviceAffinities(" + uid + ") failed,  cannot call AudioSystem.setUidDeviceAffinities");
                    return -1;
                }
            }
            long identity2 = Binder.clearCallingIdentity();
            int res2 = AudioSystem.setUidDeviceAffinities(uid, types, addresses);
            Binder.restoreCallingIdentity(identity2);
            if (res2 == 0) {
                this.mUidDeviceAffinities.put(Uid, new AudioDeviceArray(types, addresses));
                return 0;
            }
            Log.e(TAG, "AudioSystem. setUidDeviceAffinities(" + uid + ") failed");
            return -1;
        }

        /* access modifiers changed from: package-private */
        public int removeUidDeviceAffinities(int uid) {
            if (this.mUidDeviceAffinities.remove(new Integer(uid)) != null) {
                long identity = Binder.clearCallingIdentity();
                int res = AudioSystem.removeUidDeviceAffinities(uid);
                Binder.restoreCallingIdentity(identity);
                if (res == 0) {
                    return 0;
                }
            }
            Log.e(TAG, "AudioSystem. removeUidDeviceAffinities failed");
            return -1;
        }

        public String toLogFriendlyString() {
            String textDump = (AudioService.super.toLogFriendlyString() + " Proxy:\n") + "   is focus policy= " + this.mIsFocusPolicy + StringUtils.LF;
            if (this.mIsFocusPolicy) {
                textDump = ((textDump + "     focus duck behaviour= " + this.mFocusDuckBehavior + StringUtils.LF) + "     is test focus policy= " + this.mIsTestFocusPolicy + StringUtils.LF) + "     has focus listener= " + this.mHasFocusListener + StringUtils.LF;
            }
            return textDump + "   media projection= " + this.mProjection + StringUtils.LF;
        }
    }

    public int dispatchFocusChange(AudioFocusInfo afi, int focusChange, IAudioPolicyCallback pcb) {
        int dispatchFocusChange;
        if (afi == null) {
            throw new IllegalArgumentException("Illegal null AudioFocusInfo");
        } else if (pcb != null) {
            synchronized (this.mAudioPolicies) {
                if (this.mAudioPolicies.containsKey(pcb.asBinder())) {
                    dispatchFocusChange = this.mMediaFocusControl.dispatchFocusChange(afi, focusChange);
                } else {
                    throw new IllegalStateException("Unregistered AudioPolicy for focus dispatch");
                }
            }
            return dispatchFocusChange;
        } else {
            throw new IllegalArgumentException("Illegal null AudioPolicy callback");
        }
    }

    public void setFocusRequestResultFromExtPolicy(AudioFocusInfo afi, int requestResult, IAudioPolicyCallback pcb) {
        if (afi == null) {
            throw new IllegalArgumentException("Illegal null AudioFocusInfo");
        } else if (pcb != null) {
            synchronized (this.mAudioPolicies) {
                if (this.mAudioPolicies.containsKey(pcb.asBinder())) {
                    this.mMediaFocusControl.setFocusRequestResultFromExtPolicy(afi, requestResult);
                } else {
                    throw new IllegalStateException("Unregistered AudioPolicy for external focus");
                }
            }
        } else {
            throw new IllegalArgumentException("Illegal null AudioPolicy callback");
        }
    }

    public void setParameters(String keyValuePairs) {
        oppoSetParameters(keyValuePairs);
    }

    public void oppoRestoreVolumeBeforeSafeMediaVolume() {
        int device = getDeviceForStream(3);
        if ((this.mSafeMediaVolumeDevices & device) != 0) {
            this.mSafeMediaVolumeState = 2;
            this.mMusicActiveMs = 1;
            saveMusicActiveMs();
            sendMsg(this.mAudioHandler, 11, 0, 0, 0, TAG, 60000);
            int index = this.mStreamStates[3].mMediaVolumeIndexMap.get(device, -1);
            if (index > safeMediaVolumeIndex(device)) {
                this.mVolumeCurrentIndex = safeMediaVolumeIndex(device);
                this.mVolumeFinalIndex = index;
                this.mVolumeFadeDevice = device;
                sendMsg(this.mAudioHandler, 61, 2, 0, 0, null, 50);
            }
        }
    }

    public int getA2dpVolume(boolean cmpToSafeVolume, int a2dpVolume) {
        int cmpValue;
        int mFinalA2dpVolume = a2dpVolume;
        if (cmpToSafeVolume) {
            cmpValue = safeMediaVolumeIndex(128);
        } else {
            cmpValue = getVssVolumeForDevice(3, 128);
        }
        if (this.mSafeMediaVolumeState == 3) {
            mFinalA2dpVolume = mFinalA2dpVolume < cmpValue ? mFinalA2dpVolume : cmpValue;
        }
        Log.d(TAG, "20H cmpValue:" + cmpValue + " cmpValue:" + cmpValue + " mFinalA2dpVolume:" + mFinalA2dpVolume);
        return mFinalA2dpVolume;
    }

    /* access modifiers changed from: private */
    public void onOppoRestoreVolumeBeforeSafeMediaVolume() {
        int i;
        int device = getDeviceForStream(3);
        if (device != this.mVolumeFadeDevice || (i = this.mVolumeCurrentIndex) > this.mVolumeFinalIndex) {
            Log.d(TAG, "music volume fadein fail or finish,current index" + this.mVolumeCurrentIndex + " finla index " + this.mVolumeFinalIndex + " stream device " + device + " fadein device " + this.mVolumeFadeDevice);
            return;
        }
        VolumeStreamState streamState = this.mStreamStates[3];
        streamState.setIndex(i, device, TAG);
        sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
        this.mVolumeCurrentIndex += 10;
        boolean isAvrcpAbsVolSupported = this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported();
        if (AudioSystem.isStreamActive(3, 0) && (device & 896) != 0 && isAvrcpAbsVolSupported) {
            this.mDeviceBroker.postSetAvrcpAbsoluteVolumeIndex(streamState.getIndex(device) / 10);
        }
        Log.d(TAG, "20H music volume fadein current index " + this.mVolumeCurrentIndex + " finla index " + this.mVolumeFinalIndex);
        sendMsg(this.mAudioHandler, 61, 2, 0, 0, null, 50);
    }

    /* access modifiers changed from: private */
    public class AsdProxy implements IBinder.DeathRecipient {
        private final IAudioServerStateDispatcher mAsd;

        AsdProxy(IAudioServerStateDispatcher asd) {
            this.mAsd = asd;
        }

        public void binderDied() {
            synchronized (AudioService.this.mAudioServerStateListeners) {
                AudioService.this.mAudioServerStateListeners.remove(this.mAsd.asBinder());
            }
        }

        /* access modifiers changed from: package-private */
        public IAudioServerStateDispatcher callback() {
            return this.mAsd;
        }
    }

    private void checkMonitorAudioServerStatePermission() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_ROUTING") != 0) {
            throw new SecurityException("Not allowed to monitor audioserver state");
        }
    }

    public void registerAudioServerStateDispatcher(IAudioServerStateDispatcher asd) {
        checkMonitorAudioServerStatePermission();
        synchronized (this.mAudioServerStateListeners) {
            if (this.mAudioServerStateListeners.containsKey(asd.asBinder())) {
                Slog.w(TAG, "Cannot re-register audio server state dispatcher");
                return;
            }
            AsdProxy asdp = new AsdProxy(asd);
            try {
                asd.asBinder().linkToDeath(asdp, 0);
            } catch (RemoteException e) {
            }
            this.mAudioServerStateListeners.put(asd.asBinder(), asdp);
        }
    }

    public void unregisterAudioServerStateDispatcher(IAudioServerStateDispatcher asd) {
        checkMonitorAudioServerStatePermission();
        synchronized (this.mAudioServerStateListeners) {
            AsdProxy asdp = this.mAudioServerStateListeners.remove(asd.asBinder());
            if (asdp == null) {
                Slog.w(TAG, "Trying to unregister unknown audioserver state dispatcher for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid());
                return;
            }
            asd.asBinder().unlinkToDeath(asdp, 0);
        }
    }

    public boolean isAudioServerRunning() {
        checkMonitorAudioServerStatePermission();
        return AudioSystem.checkAudioFlinger() == 0;
    }
}

package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.ActivityThread;
import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.Dialog;
import android.app.IActivityController;
import android.app.IApplicationThread;
import android.app.IAssistDataReceiver;
import android.app.INotificationManager;
import android.app.IOppoKinectActivityController;
import android.app.IRequestFinishCallback;
import android.app.ITaskStackListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.ProfilerInfo;
import android.app.RemoteAction;
import android.app.WaitResult;
import android.app.WindowConfiguration;
import android.app.admin.DevicePolicyCache;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.app.usage.UsageStatsManagerInternal;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.FactoryTest;
import android.os.Handler;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UpdateLock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.os.storage.IStorageManager;
import android.provider.Settings;
import android.service.voice.IVoiceInteractionSession;
import android.service.voice.VoiceInteractionManagerInternal;
import android.sysprop.DisplayProperties;
import android.telecom.TelecomManager;
import android.util.ArrayMap;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.StatsLog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.IRecentsAnimationRunner;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationDefinition;
import android.view.inputmethod.InputMethodSystemProperty;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.app.ProcessMap;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.TransferPipe;
import com.android.internal.os.logging.MetricsLoggerWrapper;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.KeyguardDismissCallback;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.AttributeCache;
import com.android.server.DeviceIdleController;
import com.android.server.LocalServices;
import com.android.server.OppoCommonServiceFactory;
import com.android.server.SystemService;
import com.android.server.SystemServiceManager;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.AppTimeTracker;
import com.android.server.am.BaseErrorDialog;
import com.android.server.am.EventLogTags;
import com.android.server.am.IColorAbnormalAppManager;
import com.android.server.am.IColorAppCrashClearManager;
import com.android.server.am.IColorFastAppManager;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.am.OppoActivityControlerScheduler;
import com.android.server.am.PendingIntentController;
import com.android.server.am.PendingIntentRecord;
import com.android.server.am.UserState;
import com.android.server.appop.AppOpsService;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.engineer.OppoEngineerFunctionManager;
import com.android.server.firewall.IntentFirewall;
import com.android.server.pm.CompatibilityHelper;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.UserManagerService;
import com.android.server.policy.PermissionPolicyInternal;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.uri.UriGrantsManagerInternal;
import com.android.server.util.ColorZoomWindowManagerHelper;
import com.android.server.vr.VrManagerInternal;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.android.server.wm.ActivityTaskManagerService;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.color.zoomwindow.ColorZoomWindowManager;
import com.mediatek.server.MtkSystemServer;
import com.mediatek.server.MtkSystemServiceFactory;
import com.mediatek.server.am.AmsExt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ActivityTaskManagerService extends OppoBaseActivityTaskManagerService {
    static final long ACTIVITY_BG_START_GRACE_PERIOD_MS = 10000;
    static final boolean ANIMATE = true;
    private static final long APP_SWITCH_DELAY_TIME = 5000;
    public static final String DUMP_ACTIVITIES_CMD = "activities";
    public static final String DUMP_ACTIVITIES_SHORT_CMD = "a";
    public static final String DUMP_CONTAINERS_CMD = "containers";
    public static final String DUMP_LASTANR_CMD = "lastanr";
    public static final String DUMP_LASTANR_TRACES_CMD = "lastanr-traces";
    public static final String DUMP_RECENTS_CMD = "recents";
    public static final String DUMP_RECENTS_SHORT_CMD = "r";
    public static final String DUMP_STARTER_CMD = "starter";
    static final int INSTRUMENTATION_KEY_DISPATCHING_TIMEOUT_MS = 60000;
    public static final int KEY_DISPATCHING_TIMEOUT_MS = 5000;
    private static final int PENDING_ASSIST_EXTRAS_LONG_TIMEOUT = 2000;
    private static final int PENDING_ASSIST_EXTRAS_TIMEOUT = 500;
    private static final int PENDING_AUTOFILL_ASSIST_STRUCTURE_TIMEOUT = 2000;
    public static final int RELAUNCH_REASON_FREE_RESIZE = 2;
    public static final int RELAUNCH_REASON_NONE = 0;
    public static final int RELAUNCH_REASON_WINDOWING_MODE_RESIZE = 1;
    private static final int SERVICE_LAUNCH_IDLE_WHITELIST_DURATION_MS = 5000;
    private static final long START_AS_CALLER_TOKEN_EXPIRED_TIMEOUT = 1802000;
    private static final long START_AS_CALLER_TOKEN_TIMEOUT = 600000;
    private static final long START_AS_CALLER_TOKEN_TIMEOUT_IMPL = 602000;
    private static final String TAG = "ActivityTaskManager";
    private static final String TAG_CONFIGURATION = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_CONFIGURATION);
    private static final String TAG_FOCUS = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_FOCUS);
    private static final String TAG_IMMERSIVE = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_IMMERSIVE);
    private static final String TAG_LOCKTASK = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_LOCKTASK);
    private static final String TAG_STACK = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_STACK);
    private static final String TAG_SWITCH = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_SWITCH);
    private static final String TAG_VISIBILITY = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_VISIBILITY);
    final int GL_ES_VERSION;
    /* access modifiers changed from: private */
    public final MirrorActiveUids mActiveUids = new MirrorActiveUids();
    ComponentName mActiveVoiceInteractionServiceComponent;
    private ActivityStartController mActivityStartController;
    final SparseArray<ArrayMap<String, Integer>> mAllowAppSwitchUids = new SparseArray<>();
    ActivityManagerInternal mAmInternal;
    public AmsExt mAmsExt = MtkSystemServiceFactory.getInstance().makeAmsExt();
    private AppOpsService mAppOpsService;
    private long mAppSwitchesAllowedTime;
    /* access modifiers changed from: private */
    public AppWarnings mAppWarnings;
    private AssistUtils mAssistUtils;
    /* access modifiers changed from: private */
    public final Map<Integer, Set<Integer>> mCompanionAppUidsMap = new ArrayMap();
    CompatModePackages mCompatModePackages;
    private int mConfigurationSeq;
    Context mContext;
    IActivityController mController = null;
    boolean mControllerIsAMonkey = false;
    AppTimeTracker mCurAppTimeTracker;
    private int mDeviceOwnerUid = -1;
    private boolean mDidAppSwitch;
    final ArrayList<IBinder> mExpiredStartAsCallerTokens = new ArrayList<>();
    final int mFactoryTest;
    private FontScaleSettingObserver mFontScaleSettingObserver;
    boolean mForceResizableActivities;
    private float mFullscreenThumbnailScale;
    final WindowManagerGlobalLock mGlobalLock = new WindowManagerGlobalLock();
    final Object mGlobalLockWithoutBoost = this.mGlobalLock;
    H mH;
    boolean mHasHeavyWeightFeature;
    WindowProcessController mHeavyWeightProcess = null;
    WindowProcessController mHomeProcess;
    IntentFirewall mIntentFirewall;
    @VisibleForTesting
    final ActivityTaskManagerInternal mInternal;
    KeyguardController mKeyguardController;
    private boolean mKeyguardShown = false;
    String mLastANRState;
    ActivityRecord mLastResumedActivity;
    private long mLastStopAppSwitchesTime;
    private final ClientLifecycleManager mLifecycleManager;
    private LockTaskController mLockTaskController;
    OppoActivityControlerScheduler mOppoActivityControlerScheduler = null;
    OppoArmyController mOppoArmyController;
    IOppoKinectActivityController mOppoKinectController = null;
    private final ArrayList<PendingAssistExtras> mPendingAssistExtras = new ArrayList<>();
    PendingIntentController mPendingIntentController;
    /* access modifiers changed from: private */
    public final SparseArray<String> mPendingTempWhitelist = new SparseArray<>();
    private PermissionPolicyInternal mPermissionPolicyInternal;
    private PackageManagerInternal mPmInternal;
    PowerManagerInternal mPowerManagerInternal;
    WindowProcessController mPreviousProcess;
    long mPreviousProcessVisibleTime;
    final WindowProcessControllerMap mProcessMap = new WindowProcessControllerMap();
    final ProcessMap<WindowProcessController> mProcessNames = new ProcessMap<>();
    String mProfileApp = null;
    WindowProcessController mProfileProc = null;
    ProfilerInfo mProfilerInfo = null;
    /* access modifiers changed from: private */
    public RecentTasks mRecentTasks;
    RootActivityContainer mRootActivityContainer;
    IVoiceInteractionSession mRunningVoice;
    final List<ActivityTaskManagerInternal.ScreenObserver> mScreenObservers = new ArrayList();
    /* access modifiers changed from: private */
    public boolean mShowDialogs = true;
    boolean mShuttingDown = false;
    /* access modifiers changed from: private */
    public boolean mSleeping = false;
    public ActivityStackSupervisor mStackSupervisor;
    final HashMap<IBinder, IBinder> mStartActivitySources = new HashMap<>();
    final StringBuilder mStringBuilder = new StringBuilder(256);
    private String[] mSupportedSystemLocales = null;
    boolean mSupportsFreeformWindowManagement;
    boolean mSupportsMultiDisplay;
    boolean mSupportsMultiWindow;
    boolean mSupportsPictureInPicture;
    boolean mSupportsSplitScreenMultiWindow;
    boolean mSuppressResizeConfigChanges;
    final ActivityThread mSystemThread;
    private TaskChangeNotificationController mTaskChangeNotificationController;
    private Configuration mTempConfig = new Configuration();
    private int mThumbnailHeight;
    private int mThumbnailWidth;
    private final UpdateConfigurationResult mTmpUpdateConfigurationResult = new UpdateConfigurationResult();
    String mTopAction = "android.intent.action.MAIN";
    ComponentName mTopComponent;
    String mTopData;
    int mTopProcessState = 2;
    private ActivityRecord mTracedResumedActivity;
    UriGrantsManagerInternal mUgmInternal;
    final Context mUiContext;
    UiHandler mUiHandler;
    private final UpdateLock mUpdateLock = new UpdateLock("immersive");
    private UsageStatsManagerInternal mUsageStatsInternal;
    private UserManagerService mUserManager;
    private int mViSessionId = 1000;
    PowerManager.WakeLock mVoiceWakeLock;
    int mVr2dDisplayId = -1;
    VrController mVrController;
    WindowManagerService mWindowManager;

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    @interface HotPath {
        public static final int LRU_UPDATE = 2;
        public static final int NONE = 0;
        public static final int OOM_ADJUSTMENT = 1;
        public static final int PROCESS_CHANGE = 3;

        int caller() default 0;
    }

    static final class UpdateConfigurationResult {
        boolean activityRelaunched;
        int changes;

        UpdateConfigurationResult() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.changes = 0;
            this.activityRelaunched = false;
        }
    }

    private final class FontScaleSettingObserver extends ContentObserver {
        private final Uri mFontScaleUri = Settings.System.getUriFor("font_scale");
        private final Uri mHideErrorDialogsUri = Settings.Global.getUriFor("hide_error_dialogs");

        public FontScaleSettingObserver() {
            super(ActivityTaskManagerService.this.mH);
            ContentResolver resolver = ActivityTaskManagerService.this.mContext.getContentResolver();
            resolver.registerContentObserver(this.mFontScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mHideErrorDialogsUri, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (this.mFontScaleUri.equals(uri)) {
                ActivityTaskManagerService.this.updateFontScaleIfNeeded(userId);
            } else if (this.mHideErrorDialogsUri.equals(uri)) {
                synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        ActivityTaskManagerService.this.updateShouldShowDialogsLocked(ActivityTaskManagerService.this.getGlobalConfiguration());
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public ActivityTaskManagerService(Context context) {
        this.mContext = context;
        this.mFactoryTest = FactoryTest.getMode();
        this.mSystemThread = ActivityThread.currentActivityThread();
        this.mUiContext = this.mSystemThread.getSystemUiContext();
        this.mLifecycleManager = new ClientLifecycleManager();
        this.mInternal = new LocalService();
        this.GL_ES_VERSION = SystemProperties.getInt("ro.opengles.version", 0);
        this.mColorAtmsInner = new ColorActivityTaskManagerServiceInner();
        enableDefaultLogIfNeed();
    }

    /* JADX INFO: finally extract failed */
    public void onSystemReady() {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mHasHeavyWeightFeature = this.mContext.getPackageManager().hasSystemFeature("android.software.cant_save_state");
                this.mAssistUtils = new AssistUtils(this.mContext);
                this.mVrController.onSystemReady();
                this.mRecentTasks.onSystemReadyLocked();
                this.mStackSupervisor.onSystemReady();
                OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).onSystemReady();
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        this.mOppoArmyController.systemReady();
    }

    public void onInitPowerManagement() {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.initPowerManagement();
                this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
                this.mVoiceWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "*voice*");
                this.mVoiceWakeLock.setReferenceCounted(false);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void installSystemProviders() {
        this.mFontScaleSettingObserver = new FontScaleSettingObserver();
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00ed A[Catch:{ all -> 0x0144 }] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0106 A[Catch:{ all -> 0x0144 }] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0126 A[Catch:{ all -> 0x0144 }] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0135 A[Catch:{ all -> 0x0144 }] */
    public void retrieveSettings(ContentResolver resolver) {
        boolean multiWindowFormEnabled;
        Configuration globalConfig;
        boolean freeformWindowManagement = this.mContext.getPackageManager().hasSystemFeature("android.software.freeform_window_management") || Settings.Global.getInt(resolver, "enable_freeform_support", 0) != 0;
        boolean supportsMultiWindow = ActivityTaskManager.supportsMultiWindow(this.mContext);
        boolean supportsPictureInPicture = supportsMultiWindow && this.mContext.getPackageManager().hasSystemFeature("android.software.picture_in_picture");
        boolean supportsSplitScreenMultiWindow = ActivityTaskManager.supportsSplitScreenMultiWindow(this.mContext);
        boolean supportsMultiDisplay = this.mContext.getPackageManager().hasSystemFeature("android.software.activities_on_secondary_displays");
        boolean forceRtl = Settings.Global.getInt(resolver, "debug.force_rtl", 0) != 0;
        boolean forceResizable = Settings.Global.getInt(resolver, "force_resizable_activities", 0) != 0;
        boolean isPc = this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.pc");
        DisplayProperties.debug_force_rtl(Boolean.valueOf(forceRtl));
        Configuration configuration = new Configuration();
        Settings.System.getConfiguration(resolver, configuration);
        updateExtraConfigurationForUser(this.mContext, configuration, resolver.getUserId());
        if (forceRtl) {
            configuration.setLayoutDirection(configuration.locale);
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mForceResizableActivities = forceResizable;
                if (!freeformWindowManagement && !supportsSplitScreenMultiWindow && !supportsPictureInPicture) {
                    if (!supportsMultiDisplay) {
                        multiWindowFormEnabled = false;
                        if ((!supportsMultiWindow || forceResizable) && multiWindowFormEnabled) {
                            this.mSupportsMultiWindow = true;
                            this.mSupportsFreeformWindowManagement = freeformWindowManagement;
                            this.mSupportsSplitScreenMultiWindow = supportsSplitScreenMultiWindow;
                            this.mSupportsPictureInPicture = supportsPictureInPicture;
                            this.mSupportsMultiDisplay = supportsMultiDisplay;
                        } else {
                            this.mSupportsMultiWindow = false;
                            this.mSupportsFreeformWindowManagement = false;
                            this.mSupportsSplitScreenMultiWindow = false;
                            this.mSupportsPictureInPicture = false;
                            this.mSupportsMultiDisplay = false;
                        }
                        this.mWindowManager.setForceResizableTasks(this.mForceResizableActivities);
                        this.mWindowManager.setSupportsPictureInPicture(this.mSupportsPictureInPicture);
                        this.mWindowManager.setSupportsFreeformWindowManagement(this.mSupportsFreeformWindowManagement);
                        this.mWindowManager.setIsPc(isPc);
                        this.mWindowManager.mRoot.onSettingsRetrieved();
                        updateConfigurationLocked(configuration, null, true);
                        globalConfig = getGlobalConfiguration();
                        if (!ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "Initial config: " + globalConfig);
                        }
                        Resources res = this.mContext.getResources();
                        this.mThumbnailWidth = res.getDimensionPixelSize(17104898);
                        this.mThumbnailHeight = res.getDimensionPixelSize(17104897);
                        if ((globalConfig.uiMode & 4) != 4) {
                            this.mFullscreenThumbnailScale = ((float) res.getInteger(17695002)) / ((float) globalConfig.screenWidthDp);
                        } else {
                            this.mFullscreenThumbnailScale = res.getFraction(18022414, 1, 1);
                        }
                    }
                }
                multiWindowFormEnabled = true;
                if (!supportsMultiWindow) {
                }
                this.mSupportsMultiWindow = true;
                this.mSupportsFreeformWindowManagement = freeformWindowManagement;
                this.mSupportsSplitScreenMultiWindow = supportsSplitScreenMultiWindow;
                this.mSupportsPictureInPicture = supportsPictureInPicture;
                this.mSupportsMultiDisplay = supportsMultiDisplay;
                this.mWindowManager.setForceResizableTasks(this.mForceResizableActivities);
                this.mWindowManager.setSupportsPictureInPicture(this.mSupportsPictureInPicture);
                this.mWindowManager.setSupportsFreeformWindowManagement(this.mSupportsFreeformWindowManagement);
                this.mWindowManager.setIsPc(isPc);
                this.mWindowManager.mRoot.onSettingsRetrieved();
                updateConfigurationLocked(configuration, null, true);
                globalConfig = getGlobalConfiguration();
                if (!ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                }
                Resources res2 = this.mContext.getResources();
                this.mThumbnailWidth = res2.getDimensionPixelSize(17104898);
                this.mThumbnailHeight = res2.getDimensionPixelSize(17104897);
                if ((globalConfig.uiMode & 4) != 4) {
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public WindowManagerGlobalLock getGlobalLock() {
        return this.mGlobalLock;
    }

    @VisibleForTesting
    public ActivityTaskManagerInternal getAtmInternal() {
        return this.mInternal;
    }

    public void initialize(IntentFirewall intentFirewall, PendingIntentController intentController, Looper looper) {
        this.mH = new H(looper);
        this.mUiHandler = new UiHandler();
        this.mIntentFirewall = intentFirewall;
        File systemDir = SystemServiceManager.ensureSystemDir();
        this.mAppWarnings = new AppWarnings(this, this.mUiContext, this.mH, this.mUiHandler, systemDir);
        this.mCompatModePackages = new CompatModePackages(this, systemDir, this.mH);
        this.mPendingIntentController = intentController;
        this.mTempConfig.setToDefaults();
        this.mTempConfig.setLocales(LocaleList.getDefault());
        this.mTempConfig.seq = 1;
        this.mConfigurationSeq = 1;
        this.mStackSupervisor = createStackSupervisor();
        this.mRootActivityContainer = new RootActivityContainer(this);
        this.mRootActivityContainer.onConfigurationChanged(this.mTempConfig);
        this.mTaskChangeNotificationController = new TaskChangeNotificationController(this.mGlobalLock, this.mStackSupervisor, this.mH);
        this.mLockTaskController = new LockTaskController(this.mContext, this.mStackSupervisor, this.mH);
        this.mActivityStartController = new ActivityStartController(this);
        this.mRecentTasks = createRecentTasks();
        this.mStackSupervisor.setRecentTasks(this.mRecentTasks);
        this.mVrController = new VrController(this.mGlobalLock);
        this.mKeyguardController = this.mStackSupervisor.getKeyguardController();
    }

    public void onActivityManagerInternalAdded() {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mAmInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
                this.mUgmInternal = (UriGrantsManagerInternal) LocalServices.getService(UriGrantsManagerInternal.class);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int increaseConfigurationSeqLocked() {
        int i = this.mConfigurationSeq + 1;
        this.mConfigurationSeq = i;
        this.mConfigurationSeq = Math.max(i, 1);
        return this.mConfigurationSeq;
    }

    /* access modifiers changed from: protected */
    public ActivityStackSupervisor createStackSupervisor() {
        ActivityStackSupervisor supervisor = createActivityStackSupervisor(this, this.mH.getLooper());
        supervisor.initialize();
        return supervisor;
    }

    public void setWindowManager(WindowManagerService wm) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mWindowManager = wm;
                this.mLockTaskController.setWindowManager(wm);
                this.mStackSupervisor.setWindowManager(wm);
                this.mRootActivityContainer.setWindowManager(wm);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setUsageStatsManager(UsageStatsManagerInternal usageStatsManager) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mUsageStatsInternal = usageStatsManager;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public UserManagerService getUserManager() {
        if (this.mUserManager == null) {
            this.mUserManager = IUserManager.Stub.asInterface(ServiceManager.getService("user"));
        }
        return this.mUserManager;
    }

    /* access modifiers changed from: package-private */
    public AppOpsService getAppOpsService() {
        if (this.mAppOpsService == null) {
            this.mAppOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        }
        return this.mAppOpsService;
    }

    /* access modifiers changed from: package-private */
    public boolean hasUserRestriction(String restriction, int userId) {
        return getUserManager().hasUserRestriction(restriction, userId);
    }

    /* access modifiers changed from: package-private */
    public boolean hasSystemAlertWindowPermission(int callingUid, int callingPid, String callingPackage) {
        int mode = getAppOpsService().noteOperation(24, callingUid, callingPackage);
        return mode == 3 ? checkPermission("android.permission.SYSTEM_ALERT_WINDOW", callingPid, callingUid) == 0 : mode == 0;
    }

    /* access modifiers changed from: protected */
    public RecentTasks createRecentTasks() {
        return new RecentTasks(this, this.mStackSupervisor);
    }

    /* access modifiers changed from: package-private */
    public RecentTasks getRecentTasks() {
        return this.mRecentTasks;
    }

    /* access modifiers changed from: package-private */
    public ClientLifecycleManager getLifecycleManager() {
        return this.mLifecycleManager;
    }

    /* access modifiers changed from: package-private */
    public ActivityStartController getActivityStartController() {
        return this.mActivityStartController;
    }

    /* access modifiers changed from: package-private */
    public TaskChangeNotificationController getTaskChangeNotificationController() {
        return this.mTaskChangeNotificationController;
    }

    /* access modifiers changed from: package-private */
    public LockTaskController getLockTaskController() {
        return this.mLockTaskController;
    }

    /* access modifiers changed from: package-private */
    public Configuration getGlobalConfigurationForCallingPid() {
        return getGlobalConfigurationForPid(Binder.getCallingPid());
    }

    /* access modifiers changed from: package-private */
    public Configuration getGlobalConfigurationForPid(int pid) {
        Configuration configuration;
        if (pid == ActivityManagerService.MY_PID || pid < 0) {
            return getGlobalConfiguration();
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                WindowProcessController app = this.mProcessMap.getProcess(pid);
                configuration = app != null ? app.getConfiguration() : getGlobalConfiguration();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return configuration;
    }

    public ConfigurationInfo getDeviceConfigurationInfo() {
        ConfigurationInfo config = new ConfigurationInfo();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                Configuration globalConfig = getGlobalConfigurationForCallingPid();
                config.reqTouchScreen = globalConfig.touchscreen;
                config.reqKeyboardType = globalConfig.keyboard;
                config.reqNavigation = globalConfig.navigation;
                if (globalConfig.navigation == 2 || globalConfig.navigation == 3) {
                    config.reqInputFeatures |= 2;
                }
                if (!(globalConfig.keyboard == 0 || globalConfig.keyboard == 1)) {
                    config.reqInputFeatures |= 1;
                }
                config.reqGlEsVersion = this.GL_ES_VERSION;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return config;
    }

    /* access modifiers changed from: private */
    public void start() {
        LocalServices.addService(ActivityTaskManagerInternal.class, this.mInternal);
        onOppoStart();
        OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).publish();
        this.mOppoArmyController = new OppoArmyController(this.mUiContext);
    }

    public static final class Lifecycle extends SystemService {
        private final ActivityTaskManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            ActivityTaskManagerService oppoAtms = OppoCommonServiceFactory.getActivityTaskManagerService(context);
            if (oppoAtms != null) {
                this.mService = oppoAtms;
            } else {
                this.mService = new ActivityTaskManagerService(context);
            }
        }

        /* JADX WARN: Type inference failed for: r0v0, types: [android.os.IBinder, com.android.server.wm.ActivityTaskManagerService] */
        @Override // com.android.server.SystemService
        public void onStart() {
            publishBinderService("activity_task", this.mService);
            this.mService.start();
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userId) {
            synchronized (this.mService.getGlobalLock()) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mService.mStackSupervisor.onUserUnlocked(userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.SystemService
        public void onCleanupUser(int userId) {
            synchronized (this.mService.getGlobalLock()) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mService.mStackSupervisor.mLaunchParamsPersister.onCleanupUser(userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public ActivityTaskManagerService getService() {
            return this.mService;
        }
    }

    public final int startActivity(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(2, Binder.getCallingUid());
        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo, resultWho, requestCode, startFlags, profilerInfo, bOptions, UserHandle.getCallingUserId());
    }

    public final int startActivities(IApplicationThread caller, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle bOptions, int userId) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(4, Binder.getCallingUid());
        enforceNotIsolatedCaller("startActivities");
        return getActivityStartController().startActivities(caller, -1, 0, -1, callingPackage, intents, resolvedTypes, resultTo, SafeActivityOptions.fromBundle(bOptions), handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, "startActivities"), "startActivities", null, false);
    }

    public int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo, resultWho, requestCode, startFlags, profilerInfo, bOptions, userId, true);
    }

    /* access modifiers changed from: package-private */
    public int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId, boolean validateIncomingUser) {
        if (this.mAmsExt.preLaunchApplication(callingPackage, intent, resolvedType, startFlags)) {
            return 0;
        }
        enforceNotIsolatedCaller("startActivityAsUser");
        int userId2 = getActivityStartController().checkTargetUser(userId, validateIncomingUser, Binder.getCallingPid(), Binder.getCallingUid(), "startActivityAsUser");
        int flag = bOptions == null ? -1 : bOptions.getInt("extra_window_mode");
        if (flag != ColorZoomWindowManager.WINDOWING_MODE_ZOOM_LEGACY && flag != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            if (flag != ColorZoomWindowManager.WINDOWING_MODE_ZOOM_TO_FULLSCREEN) {
                return getActivityStartController().obtainStarter(intent, "startActivityAsUser").setCaller(caller).setCallingPackage(callingPackage).setResolvedType(resolvedType).setResultTo(resultTo).setResultWho(resultWho).setRequestCode(requestCode).setStartFlags(startFlags).setProfilerInfo(profilerInfo).setActivityOptions(bOptions).setMayWait(userId2).execute();
            }
        }
        ColorZoomWindowManagerHelper.getInstance();
        return ColorZoomWindowManagerHelper.getZoomWindowManager().startZoomWindow(intent, bOptions, userId2, callingPackage);
    }

    /* JADX INFO: finally extract failed */
    public int startActivityIntentSender(IApplicationThread caller, IIntentSender target, IBinder whitelistToken, Intent fillInIntent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flagsMask, int flagsValues, Bundle bOptions) {
        enforceNotIsolatedCaller("startActivityIntentSender");
        if (fillInIntent != null && fillInIntent.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        } else if (target instanceof PendingIntentRecord) {
            PendingIntentRecord pir = (PendingIntentRecord) target;
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityStack stack = getTopDisplayFocusedStack();
                    if (stack.mResumedActivity != null && stack.mResumedActivity.info.applicationInfo.uid == Binder.getCallingUid()) {
                        this.mAppSwitchesAllowedTime = 0;
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return pir.sendInner(0, fillInIntent, resolvedType, whitelistToken, null, null, resultTo, resultWho, requestCode, flagsMask, flagsValues, bOptions);
        } else {
            throw new IllegalArgumentException("Bad PendingIntent object");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0121, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0124, code lost:
        return false;
     */
    public boolean startNextMatchingActivity(IBinder callingActivity, Intent intent, Bundle bOptions) {
        String str;
        if (intent == null || !intent.hasFileDescriptors()) {
            SafeActivityOptions options = SafeActivityOptions.fromBundle(bOptions);
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(callingActivity);
                    if (r == null) {
                        SafeActivityOptions.abort(options);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    } else if (!r.attachedToProcess()) {
                        SafeActivityOptions.abort(options);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    } else {
                        Intent intent2 = new Intent(intent);
                        try {
                            intent2.setDataAndType(r.intent.getData(), r.intent.getType());
                            intent2.setComponent(null);
                            int i = 1;
                            boolean debug = (intent2.getFlags() & 8) != 0;
                            ActivityInfo aInfo = null;
                            try {
                                List<ResolveInfo> resolves = AppGlobals.getPackageManager().queryIntentActivities(intent2, r.resolvedType, 66560, UserHandle.getCallingUserId()).getList();
                                int N = resolves != null ? resolves.size() : 0;
                                int i2 = 0;
                                while (true) {
                                    if (i2 >= N) {
                                        break;
                                    }
                                    ResolveInfo rInfo = resolves.get(i2);
                                    if (!rInfo.activityInfo.packageName.equals(r.packageName) || !rInfo.activityInfo.name.equals(r.info.name)) {
                                        i2++;
                                        i = 1;
                                    } else {
                                        int i3 = i2 + i;
                                        if (i3 < N) {
                                            aInfo = resolves.get(i3).activityInfo;
                                        }
                                        if (debug) {
                                            Slog.v(TAG, "Next matching activity: found current " + r.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + r.info.name);
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("Next matching activity: next is ");
                                            if (aInfo == null) {
                                                str = "null";
                                            } else {
                                                str = aInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + aInfo.name;
                                            }
                                            sb.append(str);
                                            Slog.v(TAG, sb.toString());
                                        }
                                    }
                                }
                            } catch (RemoteException e) {
                            }
                            if (aInfo == null) {
                                SafeActivityOptions.abort(options);
                                if (debug) {
                                    Slog.d(TAG, "Next matching activity: nothing found");
                                }
                            } else {
                                intent2.setComponent(new ComponentName(aInfo.applicationInfo.packageName, aInfo.name));
                                intent2.setFlags(intent2.getFlags() & -503316481);
                                boolean wasFinishing = r.finishing;
                                r.finishing = true;
                                ActivityRecord resultTo = r.resultTo;
                                String resultWho = r.resultWho;
                                int requestCode = r.requestCode;
                                r.resultTo = null;
                                if (resultTo != null) {
                                    resultTo.removeResultsLocked(r, resultWho, requestCode);
                                }
                                long origId = Binder.clearCallingIdentity();
                                int res = getActivityStartController().obtainStarter(intent2, "startNextMatchingActivity").setCaller(r.app.getThread()).setResolvedType(r.resolvedType).setActivityInfo(aInfo).setResultTo(resultTo != null ? resultTo.appToken : null).setResultWho(resultWho).setRequestCode(requestCode).setCallingPid(-1).setCallingUid(r.launchedFromUid).setCallingPackage(r.launchedFromPackage).setRealCallingPid(-1).setRealCallingUid(r.launchedFromUid).setActivityOptions(options).execute();
                                Binder.restoreCallingIdentity(origId);
                                r.finishing = wasFinishing;
                                if (res != 0) {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    return false;
                                }
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return true;
                            }
                        } catch (Throwable th) {
                            th = th;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
    }

    public final WaitResult startActivityAndWait(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        int i;
        WaitResult res = new WaitResult();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                enforceNotIsolatedCaller("startActivityAndWait");
                i = userId;
                try {
                } catch (Throwable th) {
                    th = th;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
                try {
                    try {
                        try {
                            try {
                                try {
                                } catch (Throwable th2) {
                                    th = th2;
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
                try {
                    try {
                        try {
                            try {
                                try {
                                } catch (Throwable th7) {
                                    th = th7;
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            } catch (Throwable th8) {
                                th = th8;
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } catch (Throwable th9) {
                            th = th9;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } catch (Throwable th10) {
                        th = th10;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                    try {
                        getActivityStartController().obtainStarter(intent, "startActivityAndWait").setCaller(caller).setCallingPackage(callingPackage).setResolvedType(resolvedType).setResultTo(resultTo).setResultWho(resultWho).setRequestCode(requestCode).setStartFlags(startFlags).setActivityOptions(bOptions).setMayWait(handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), i, "startActivityAndWait")).setProfilerInfo(profilerInfo).setWaitResult(res).execute();
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return res;
                    } catch (Throwable th11) {
                        th = th11;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                } catch (Throwable th12) {
                    th = th12;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            } catch (Throwable th13) {
                th = th13;
                i = userId;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    public final int startActivityWithConfig(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, Configuration config, Bundle bOptions, int userId) {
        int execute;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                enforceNotIsolatedCaller("startActivityWithConfig");
                execute = getActivityStartController().obtainStarter(intent, "startActivityWithConfig").setCaller(caller).setCallingPackage(callingPackage).setResolvedType(resolvedType).setResultTo(resultTo).setResultWho(resultWho).setRequestCode(requestCode).setStartFlags(startFlags).setGlobalConfiguration(config).setActivityOptions(bOptions).setMayWait(handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, "startActivityWithConfig")).execute();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return execute;
    }

    /* JADX INFO: finally extract failed */
    public IBinder requestStartActivityPermissionToken(IBinder delegatorToken) {
        int callingUid = Binder.getCallingUid();
        if (UserHandle.getAppId(callingUid) == 1000) {
            IBinder permissionToken = new Binder();
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mStartActivitySources.put(permissionToken, delegatorToken);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            this.mUiHandler.sendMessageDelayed(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$3DTHgCAeEd5OOF7ACeXoCk8mmrQ.INSTANCE, this, permissionToken), START_AS_CALLER_TOKEN_TIMEOUT_IMPL);
            this.mUiHandler.sendMessageDelayed(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$7ieG0s7Zp4H2bLiWdOgB6MqhcI.INSTANCE, this, permissionToken), START_AS_CALLER_TOKEN_EXPIRED_TIMEOUT);
            return permissionToken;
        }
        throw new SecurityException("Only the system process can request a permission token, received request from uid: " + callingUid);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00d7, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        r10 = r30;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00de, code lost:
        if (r10 != -10000) goto L_0x00e9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00e0, code lost:
        r10 = android.os.UserHandle.getUserId(r6.app.mUid);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:?, code lost:
        r0 = getActivityStartController().obtainStarter(r20, "startActivityAsCaller").setCallingUid(r7).setCallingPackage(r8).setResolvedType(r21).setResultTo(r22).setResultWho(r23).setRequestCode(r24).setStartFlags(r25).setActivityOptions(r27).setMayWait(r10).setIgnoreTargetSecurity(r29);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0127, code lost:
        if (r9 == false) goto L_0x012e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0129, code lost:
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x012e, code lost:
        r1 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x013c, code lost:
        return r0.setFilterCallingUid(r1).setAllowBackgroundActivityStart(true).execute();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x013d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x013f, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0141, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0143, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0145, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0147, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0149, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:?, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:?, code lost:
        throw r0;
     */
    public final int startActivityAsCaller(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, IBinder permissionToken, boolean ignoreTargetSecurity, int userId) {
        IBinder sourceToken;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (resultTo != null) {
                    if (permissionToken != null) {
                        this.mAmInternal.enforceCallingPermission("android.permission.START_ACTIVITY_AS_CALLER", "startActivityAsCaller");
                        sourceToken = this.mStartActivitySources.remove(permissionToken);
                        if (sourceToken == null) {
                            if (this.mExpiredStartAsCallerTokens.contains(permissionToken)) {
                                throw new SecurityException("Called with expired permission token: " + permissionToken);
                            }
                            throw new SecurityException("Called with invalid permission token: " + permissionToken);
                        }
                    } else {
                        sourceToken = resultTo;
                    }
                    ActivityRecord sourceRecord = this.mRootActivityContainer.isInAnyStack(sourceToken);
                    if (sourceRecord == null) {
                        throw new SecurityException("Called with bad activity token: " + sourceToken);
                    } else if (sourceRecord.app == null) {
                        throw new SecurityException("Called without a process attached to activity");
                    } else if (sourceRecord.info.packageName.equals(PackageManagerService.PLATFORM_PACKAGE_NAME)) {
                        if (UserHandle.getAppId(sourceRecord.app.mUid) != 1000) {
                            if (sourceRecord.app.mUid != sourceRecord.launchedFromUid) {
                                throw new SecurityException("Calling activity in uid " + sourceRecord.app.mUid + " must be system uid or original calling uid " + sourceRecord.launchedFromUid);
                            }
                        }
                        if (ignoreTargetSecurity) {
                            if (intent.getComponent() == null) {
                                throw new SecurityException("Component must be specified with ignoreTargetSecurity");
                            } else if (intent.getSelector() != null) {
                                throw new SecurityException("Selector not allowed with ignoreTargetSecurity");
                            }
                        }
                        int targetUid = sourceRecord.launchedFromUid;
                        String targetPackage = sourceRecord.launchedFromPackage;
                        boolean isResolver = sourceRecord.isResolverOrChildActivity();
                    } else {
                        throw new SecurityException("Must be called from an activity that is declared in the android package");
                    }
                } else {
                    throw new SecurityException("Must be called from an activity");
                }
            } catch (Throwable th) {
                th = th;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int handleIncomingUser(int callingPid, int callingUid, int userId, String name) {
        return this.mAmInternal.handleIncomingUser(callingPid, callingUid, userId, false, 2, name, (String) null);
    }

    public int startVoiceActivity(String callingPackage, int callingPid, int callingUid, Intent intent, String resolvedType, IVoiceInteractionSession session, IVoiceInteractor interactor, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        this.mAmInternal.enforceCallingPermission("android.permission.BIND_VOICE_INTERACTION", "startVoiceActivity()");
        if (session == null || interactor == null) {
            throw new NullPointerException("null session or interactor");
        }
        return getActivityStartController().obtainStarter(intent, "startVoiceActivity").setCallingUid(callingUid).setCallingPackage(callingPackage).setResolvedType(resolvedType).setVoiceSession(session).setVoiceInteractor(interactor).setStartFlags(startFlags).setProfilerInfo(profilerInfo).setActivityOptions(bOptions).setMayWait(handleIncomingUser(callingPid, callingUid, userId, "startVoiceActivity")).setAllowBackgroundActivityStart(true).execute();
    }

    public int startAssistantActivity(String callingPackage, int callingPid, int callingUid, Intent intent, String resolvedType, Bundle bOptions, int userId) {
        this.mAmInternal.enforceCallingPermission("android.permission.BIND_VOICE_INTERACTION", "startAssistantActivity()");
        return getActivityStartController().obtainStarter(intent, "startAssistantActivity").setCallingUid(callingUid).setCallingPackage(callingPackage).setResolvedType(resolvedType).setActivityOptions(bOptions).setMayWait(handleIncomingUser(callingPid, callingUid, userId, "startAssistantActivity")).setAllowBackgroundActivityStart(true).execute();
    }

    /* JADX INFO: finally extract failed */
    public void startRecentsActivity(Intent intent, IAssistDataReceiver assistDataReceiver, IRecentsAnimationRunner recentsAnimationRunner) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "startRecentsActivity()");
        int callingPid = Binder.getCallingPid();
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    new RecentsAnimation(this, this.mStackSupervisor, getActivityStartController(), this.mWindowManager, callingPid).startRecentsActivity(intent, recentsAnimationRunner, this.mRecentTasks.getRecentsComponent(), this.mRecentTasks.getRecentsComponentUid(), assistDataReceiver);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    public final int startActivityFromRecents(int taskId, Bundle bOptions) {
        int startActivityFromRecents;
        enforceCallerIsRecentsOrHasPermission("android.permission.START_TASKS_FROM_RECENTS", "startActivityFromRecents()");
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        SafeActivityOptions safeOptions = SafeActivityOptions.fromBundle(bOptions);
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    startActivityFromRecents = this.mStackSupervisor.startActivityFromRecents(callingPid, callingUid, taskId, safeOptions);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return startActivityFromRecents;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    public final boolean isActivityStartAllowedOnDisplay(int displayId, Intent intent, String resolvedType, int userId) {
        boolean canPlaceEntityOnDisplay;
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        long origId = Binder.clearCallingIdentity();
        try {
            ActivityInfo aInfo = this.mAmInternal.getActivityInfoForUser(this.mStackSupervisor.resolveActivity(intent, resolvedType, 0, null, userId, ActivityStarter.computeResolveFilterUid(callingUid, callingUid, -10000)), userId);
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    canPlaceEntityOnDisplay = this.mStackSupervisor.canPlaceEntityOnDisplay(displayId, callingPid, callingUid, aInfo);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return canPlaceEntityOnDisplay;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public final boolean finishActivity(IBinder token, int resultCode, Intent resultData, int finishTask) {
        boolean res;
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(6, Binder.getCallingUid());
        if (resultData == null || !resultData.hasFileDescriptors()) {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    boolean finishWithRootActivity = true;
                    if (r == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return true;
                    }
                    TaskRecord tr = r.getTaskRecord();
                    ActivityRecord rootR = tr.getRootActivity();
                    if (rootR == null) {
                        Slog.w(TAG, "Finishing task with all activities already finished");
                    }
                    if (getLockTaskController().activityBlockedFromFinish(r)) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    if (this.mController != null) {
                        try {
                            ActivityRecord next = r.getActivityStack().topRunningActivityLocked(token, 0);
                            if (next != null) {
                                boolean resumeOK = true;
                                try {
                                    resumeOK = this.mOppoActivityControlerScheduler != null ? this.mOppoActivityControlerScheduler.scheduleActivityResuming(next.packageName) : this.mController.activityResuming(next.packageName);
                                } catch (RemoteException e) {
                                    this.mController = null;
                                    Watchdog.getInstance().setActivityController(null);
                                    if (this.mOppoActivityControlerScheduler != null) {
                                        this.mOppoActivityControlerScheduler.exitRunningScheduler();
                                        this.mOppoActivityControlerScheduler = null;
                                    }
                                }
                                if (!resumeOK) {
                                    Slog.i(TAG, "Not finishing activity because controller resumed");
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    return false;
                                }
                            }
                        } catch (Throwable th) {
                            th = th;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                    if (r.app != null) {
                        r.app.setLastActivityFinishTimeIfNeeded(SystemClock.uptimeMillis());
                    }
                    long origId = Binder.clearCallingIdentity();
                    if (finishTask != 1) {
                        finishWithRootActivity = false;
                    }
                    if (finishTask == 2 || (finishWithRootActivity && r == rootR)) {
                        boolean res2 = this.mStackSupervisor.removeTaskByIdLocked(tr.taskId, false, finishWithRootActivity, "finish-activity");
                        if (!res2) {
                            Slog.i(TAG, "Removing task failed to finish activity");
                        }
                        r.mRelaunchReason = 0;
                        res = res2;
                    } else {
                        try {
                            res = tr.getStack().requestFinishActivityLocked(token, resultCode, resultData, "app-request", true);
                            if (!res) {
                                Slog.i(TAG, "Failed to finish by app-request");
                            }
                        } catch (Throwable th2) {
                            Binder.restoreCallingIdentity(origId);
                            throw th2;
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return res;
                } catch (Throwable th3) {
                    th = th3;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
    }

    public boolean finishActivityAffinity(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        return false;
                    }
                    TaskRecord task = r.getTaskRecord();
                    if (getLockTaskController().activityBlockedFromFinish(r)) {
                        Binder.restoreCallingIdentity(origId);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    boolean finishActivityAffinityLocked = task.getStack().finishActivityAffinityLocked(r);
                    Binder.restoreCallingIdentity(origId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return finishActivityAffinityLocked;
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public final void activityIdle(IBinder token, Configuration config, boolean stopProfiling) {
        long origId = Binder.clearCallingIdentity();
        WindowProcessController proc = null;
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityRecord.getStackLocked(token) == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    ActivityRecord r = this.mStackSupervisor.activityIdleInternalLocked(token, false, false, config);
                    if (r != null) {
                        proc = r.app;
                    }
                    if (stopProfiling && proc != null) {
                        proc.clearProfilerIfNeeded();
                    }
                    this.mAmsExt.onEndOfActivityIdle(this.mContext, r.intent);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    public final void activityResumed(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord.activityResumedLocked(token);
                this.mWindowManager.notifyAppResumedFinished(token);
                ActivityRecord record = ActivityRecord.forTokenLocked(token);
                OppoFeatureCache.get(IColorAppCrashClearManager.DEFAULT).resetStartTime(record != null ? record.mColorArEx : null);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        Binder.restoreCallingIdentity(origId);
    }

    /* JADX INFO: finally extract failed */
    public final void activityTopResumedStateLost() {
        long origId = Binder.clearCallingIdentity();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.handleTopResumedStateReleased(false);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        Binder.restoreCallingIdentity(origId);
    }

    /* JADX INFO: finally extract failed */
    public final void activityPaused(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack != null) {
                    stack.activityPausedLocked(token, false);
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        Binder.restoreCallingIdentity(origId);
    }

    public final void activityStopped(IBinder token, Bundle icicle, PersistableBundle persistentState, CharSequence description) {
        ActivityRecord r;
        if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
            Slog.v(TAG, "Activity stopped: token=" + token);
        }
        if (icicle == null || !icicle.hasFileDescriptors()) {
            long origId = Binder.clearCallingIdentity();
            String restartingName = null;
            int restartingUid = 0;
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    r = ActivityRecord.isInStackLocked(token);
                    if (r != null) {
                        if (r.attachedToProcess() && r.isState(ActivityStack.ActivityState.RESTARTING_PROCESS)) {
                            restartingName = r.app.mName;
                            restartingUid = r.app.mUid;
                        }
                        r.activityStoppedLocked(icicle, persistentState, description);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            if (restartingName != null) {
                this.mStackSupervisor.removeRestartTimeouts(r);
                this.mAmInternal.killProcess(restartingName, restartingUid, "restartActivityProcess");
            }
            this.mAmInternal.trimApplications();
            Binder.restoreCallingIdentity(origId);
            return;
        }
        throw new IllegalArgumentException("File descriptors passed in Bundle");
    }

    public final void activityDestroyed(IBinder token) {
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
            String str = TAG_SWITCH;
            Slog.v(str, "ACTIVITY DESTROYED: " + token);
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack != null) {
                    stack.activityDestroyedLocked(token, "activityDestroyed");
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public final void activityRelaunched(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.activityRelaunchedLocked(token);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        Binder.restoreCallingIdentity(origId);
    }

    /* JADX INFO: finally extract failed */
    public final void activitySlept(IBinder token) {
        if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
            Slog.v(TAG, "Activity slept: token=" + token);
        }
        long origId = Binder.clearCallingIdentity();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    this.mStackSupervisor.activitySleptLocked(r);
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        Binder.restoreCallingIdentity(origId);
    }

    public final void clientTransactionComplete(IBinder activityToken, int seq) {
        ClientLifecycleMonitor.getInstance().transactionEnd(activityToken, seq);
    }

    public void setRequestedOrientation(IBinder token, int requestedOrientation) {
        if (ActivityTaskManagerDebugConfig.DEBUG_AMS || ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
            int callingPid = Binder.getCallingPid();
            int callingUid = Binder.getCallingUid();
            Slog.v(TAG, "Requested  Orientation call pid " + callingPid + " call Uid " + callingUid + " requestedOrientation " + requestedOrientation);
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (ActivityTaskManagerDebugConfig.DEBUG_AMS || ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.v(TAG, "Requested  Orientation r " + r);
                }
                if (r != null) {
                    long origId = Binder.clearCallingIdentity();
                    try {
                        r.setRequestedOrientation(requestedOrientation);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } finally {
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int getRequestedOrientation(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    return -1;
                }
                int orientation = r.getOrientation();
                WindowManagerService.resetPriorityAfterLockedSection();
                return orientation;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setImmersive(IBinder token, boolean immersive) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    r.immersive = immersive;
                    if (r.isResumedActivityOnDisplay()) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_IMMERSIVE) {
                            String str = TAG_IMMERSIVE;
                            Slog.d(str, "Frontmost changed immersion: " + r);
                        }
                        applyUpdateLockStateLocked(r);
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void applyUpdateLockStateLocked(ActivityRecord r) {
        this.mH.post(new Runnable(r != null && r.immersive, r) {
            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$p4I6RZJqLXjaEjdISFyNzjAe4HE */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ ActivityRecord f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ActivityTaskManagerService.this.lambda$applyUpdateLockStateLocked$0$ActivityTaskManagerService(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$applyUpdateLockStateLocked$0$ActivityTaskManagerService(boolean nextState, ActivityRecord r) {
        if (this.mUpdateLock.isHeld() != nextState) {
            if (ActivityTaskManagerDebugConfig.DEBUG_IMMERSIVE) {
                String str = TAG_IMMERSIVE;
                Slog.d(str, "Applying new update lock state '" + nextState + "' for " + r);
            }
            if (nextState) {
                this.mUpdateLock.acquire();
            } else {
                this.mUpdateLock.release();
            }
        }
    }

    public boolean isImmersive(IBinder token) {
        boolean z;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    z = r.immersive;
                } else {
                    throw new IllegalArgumentException();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public boolean isTopActivityImmersive() {
        boolean z;
        enforceNotIsolatedCaller("isTopActivityImmersive");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = getTopDisplayFocusedStack().topRunningActivityLocked();
                z = r != null ? r.immersive : false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public void overridePendingTransition(IBinder token, String packageName, int enterAnim, int exitAnim) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord self = ActivityRecord.isInStackLocked(token);
                if (self != null) {
                    long origId = Binder.clearCallingIdentity();
                    if (self.isState(ActivityStack.ActivityState.RESUMED, ActivityStack.ActivityState.PAUSING)) {
                        self.getDisplay().mDisplayContent.mAppTransition.overridePendingAppTransition(packageName, enterAnim, exitAnim, null);
                    }
                    Binder.restoreCallingIdentity(origId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int getFrontActivityScreenCompatMode() {
        enforceNotIsolatedCaller("getFrontActivityScreenCompatMode");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = getTopDisplayFocusedStack().topRunningActivityLocked();
                if (r == null) {
                    return -3;
                }
                int computeCompatModeLocked = this.mCompatModePackages.computeCompatModeLocked(r.info.applicationInfo);
                WindowManagerService.resetPriorityAfterLockedSection();
                return computeCompatModeLocked;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setFrontActivityScreenCompatMode(int mode) {
        this.mAmInternal.enforceCallingPermission("android.permission.SET_SCREEN_COMPATIBILITY", "setFrontActivityScreenCompatMode");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = getTopDisplayFocusedStack().topRunningActivityLocked();
                if (r == null) {
                    Slog.w(TAG, "setFrontActivityScreenCompatMode failed: no top activity");
                    return;
                }
                this.mCompatModePackages.setPackageScreenCompatModeLocked(r.info.applicationInfo, mode);
                WindowManagerService.resetPriorityAfterLockedSection();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int getLaunchedFromUid(IBinder activityToken) {
        ActivityRecord srec;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                srec = ActivityRecord.forTokenLocked(activityToken);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (srec == null) {
            return -1;
        }
        try {
            return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getLaunchedFromUid(AppGlobals.getPackageManager().getNameForUid(Binder.getCallingUid()), srec.launchedFromUid);
        } catch (RemoteException e) {
            return srec.launchedFromUid;
        }
    }

    public String getLaunchedFromPackage(IBinder activityToken) {
        ActivityRecord srec;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                srec = ActivityRecord.forTokenLocked(activityToken);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (srec == null) {
            return null;
        }
        return srec.launchedFromPackage;
    }

    /* JADX INFO: finally extract failed */
    public boolean convertFromTranslucent(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    boolean translucentChanged = r.changeWindowTranslucency(true);
                    if (translucentChanged) {
                        this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                    }
                    this.mWindowManager.setAppFullscreen(token, true);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return translucentChanged;
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean convertToTranslucent(IBinder token, Bundle options) {
        SafeActivityOptions safeOptions = SafeActivityOptions.fromBundle(options);
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    TaskRecord task = r.getTaskRecord();
                    int index = task.mActivities.lastIndexOf(r);
                    if (index > 0) {
                        task.mActivities.get(index - 1).returningOptions = safeOptions != null ? safeOptions.getOptions(r) : null;
                    }
                    boolean translucentChanged = r.changeWindowTranslucency(false);
                    if (translucentChanged) {
                        r.getActivityStack().convertActivityToTranslucent(r);
                    }
                    this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                    this.mWindowManager.setAppFullscreen(token, false);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return translucentChanged;
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void notifyActivityDrawn(IBinder token) {
        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
            String str = TAG_VISIBILITY;
            Slog.d(str, "notifyActivityDrawn: token=" + token);
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = this.mRootActivityContainer.isInAnyStack(token);
                if (r != null) {
                    r.getActivityStack().notifyActivityDrawnLocked(r);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void reportActivityFullyDrawn(IBinder token, boolean restoredFromBundle) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    r.reportFullyDrawnLocked(restoredFromBundle);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int getActivityDisplayId(IBinder activityToken) throws RemoteException {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(activityToken);
                if (stack == null || stack.mDisplayId == -1) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return 0;
                }
                int i = stack.mDisplayId;
                return i;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public ActivityManager.StackInfo getFocusedStackInfo() throws RemoteException {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getStackInfo()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityStack focusedStack = getTopDisplayFocusedStack();
                    if (focusedStack != null) {
                        ActivityManager.StackInfo stackInfo = this.mRootActivityContainer.getStackInfo(focusedStack.mStackId);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return stackInfo;
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                    return null;
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0067, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x006e, code lost:
        return;
     */
    public void setFocusedStack(int stackId) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setFocusedStack()");
        if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS) {
            String str = TAG_FOCUS;
            Slog.d(str, "setFocusedStack: stackId=" + stackId);
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityStack stack = this.mRootActivityContainer.getStack(stackId);
                    if (stack == null) {
                        Slog.w(TAG, "setFocusedStack: No stack with id=" + stackId);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    ActivityRecord r = stack.topRunningActivityLocked();
                    if (r != null && r.moveFocusableActivityToTop("setFocusedStack")) {
                        this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0052, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0059, code lost:
        return;
     */
    public void setFocusedTask(int taskId) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setFocusedTask()");
        if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS) {
            String str = TAG_FOCUS;
            Slog.d(str, "setFocusedTask: taskId=" + taskId);
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    ActivityRecord r = task.topRunningActivityLocked();
                    if (r != null && r.moveFocusableActivityToTop("setFocusedTask")) {
                        this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX INFO: finally extract failed */
    public void restartActivityProcessIfVisible(IBinder activityToken) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "restartActivityProcess()");
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(activityToken);
                    if (r == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    r.restartProcessIfVisible();
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(callingId);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean removeTask(int taskId) {
        boolean removeTaskByIdLocked;
        enforceCallerIsRecentsOrHasPermission("android.permission.REMOVE_TASKS", "removeTask()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    removeTaskByIdLocked = this.mStackSupervisor.removeTaskByIdLocked(taskId, true, true, "remove-task");
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return removeTaskByIdLocked;
    }

    public void removeAllVisibleRecentTasks() {
        enforceCallerIsRecentsOrHasPermission("android.permission.REMOVE_TASKS", "removeAllVisibleRecentTasks()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    getRecentTasks().removeAllVisibleTasks(this.mAmInternal.getCurrentUserId());
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean shouldUpRecreateTask(IBinder token, String destAffinity) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord srec = ActivityRecord.forTokenLocked(token);
                if (srec != null) {
                    boolean shouldUpRecreateTaskLocked = srec.getActivityStack().shouldUpRecreateTaskLocked(srec, destAffinity);
                    return shouldUpRecreateTaskLocked;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean navigateUpTo(IBinder token, Intent destIntent, int resultCode, Intent resultData) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.forTokenLocked(token);
                if (r != null) {
                    boolean navigateUpToLocked = r.getActivityStack().navigateUpToLocked(r, destIntent, resultCode, resultData);
                    return navigateUpToLocked;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) {
        enforceNotIsolatedCaller("moveActivityTaskToBack");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    int taskId = ActivityRecord.getTaskForActivityLocked(token, !nonRoot);
                    if (this.mRootActivityContainer.anyTaskForId(taskId) != null) {
                        boolean moveTaskToBackLocked = ActivityRecord.getStackLocked(token).moveTaskToBackLocked(taskId);
                        return moveTaskToBackLocked;
                    }
                    Binder.restoreCallingIdentity(origId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0066, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006d, code lost:
        return r2;
     */
    public Rect getTaskBounds(int taskId) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getTaskBounds()");
        long ident = Binder.clearCallingIdentity();
        Rect rect = new Rect();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 1);
                    if (task == null) {
                        Slog.w(TAG, "getTaskBounds: taskId=" + taskId + " not found");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return rect;
                    } else if (task.getStack() != null) {
                        task.getWindowContainerBounds(rect);
                    } else if (!task.matchParentBounds()) {
                        rect.set(task.getBounds());
                    } else if (task.mLastNonFullscreenBounds != null) {
                        rect.set(task.mLastNonFullscreenBounds);
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public ActivityManager.TaskDescription getTaskDescription(int id) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getTaskDescription()");
                TaskRecord tr = this.mRootActivityContainer.anyTaskForId(id, 1);
                if (tr != null) {
                    ActivityManager.TaskDescription taskDescription = tr.lastTaskDescription;
                    return taskDescription;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return null;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setTaskWindowingMode(int taskId, int windowingMode, boolean toTop) {
        if (windowingMode == 3) {
            setTaskWindowingModeSplitScreenPrimary(taskId, 0, toTop, true, null, true);
            return;
        }
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setTaskWindowingMode()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        Slog.w(TAG, "setTaskWindowingMode: No task for id=" + taskId);
                        return;
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                        String str = TAG_STACK;
                        Slog.d(str, "setTaskWindowingMode: moving task=" + taskId + " to windowingMode=" + windowingMode + " toTop=" + toTop);
                    }
                    if (task.isActivityTypeStandardOrUndefined()) {
                        ActivityStack stack = task.getStack();
                        if (toTop) {
                            stack.moveToFront("setTaskWindowingMode", task);
                        }
                        stack.setWindowingMode(windowingMode);
                        Binder.restoreCallingIdentity(ident);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    throw new IllegalArgumentException("setTaskWindowingMode: Attempt to move non-standard task " + taskId + " to windowing mode=" + windowingMode);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public String getCallingPackage(IBinder token) {
        String resultPkg;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = getCallingRecordLocked(token);
                if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                    Slog.v(TAG, "getCallingPackage token " + token);
                }
                resultPkg = r != null ? r.info.packageName : null;
                if (resultPkg != null) {
                    resultPkg = OppoFeatureCache.get(IColorFastAppManager.DEFAULT).fastThirdAppLoginPkgIfNeeded(resultPkg, getPackageForToken(token));
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return resultPkg;
    }

    public ComponentName getCallingActivity(IBinder token) {
        ComponentName component;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = getCallingRecordLocked(token);
                component = r != null ? r.intent.getComponent() : null;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return component;
    }

    private ActivityRecord getCallingRecordLocked(IBinder token) {
        ActivityRecord r = ActivityRecord.isInStackLocked(token);
        if (r == null) {
            return null;
        }
        return r.resultTo;
    }

    public void unhandledBack() {
        this.mAmInternal.enforceCallingPermission("android.permission.FORCE_BACK", "unhandledBack()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    getTopDisplayFocusedStack().unhandledBackLocked();
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void onBackPressedOnTaskRoot(IBinder token, IRequestFinishCallback callback) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    ActivityStack stack = r.getActivityStack();
                    if (stack == null || !stack.isSingleTaskInstance()) {
                        try {
                            callback.requestFinish();
                        } catch (RemoteException e) {
                            Slog.e(TAG, "Failed to invoke request finish callback", e);
                        }
                    } else {
                        this.mTaskChangeNotificationController.notifyBackPressedOnTaskRoot(r.getTaskRecord().getTaskInfo());
                    }
                } else {
                    return;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public void moveTaskToFront(IApplicationThread appThread, String callingPackage, int taskId, int flags, Bundle bOptions) {
        this.mAmInternal.enforceCallingPermission("android.permission.REORDER_TASKS", "moveTaskToFront()");
        if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
            String str = TAG_STACK;
            Slog.d(str, "moveTaskToFront: moving taskId=" + taskId);
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                moveTaskToFrontLocked(appThread, callingPackage, taskId, flags, SafeActivityOptions.fromBundle(bOptions), false);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void moveTaskToFrontLocked(IApplicationThread appThread, String callingPackage, int taskId, int flags, SafeActivityOptions options, boolean fromRecents) {
        WindowProcessController callerApp;
        ActivityOptions realOptions;
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        if (!isSameApp(callingUid, callingPackage)) {
            String msg = "Permission Denial: moveTaskToFrontLocked() from pid=" + Binder.getCallingPid() + " as package " + callingPackage;
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        } else if (!checkAppSwitchAllowedLocked(callingPid, callingUid, -1, -1, "Task to front")) {
            SafeActivityOptions.abort(options);
        } else {
            long origId = Binder.clearCallingIdentity();
            if (appThread != null) {
                callerApp = getProcessController(appThread);
            } else {
                callerApp = null;
            }
            if (!getActivityStartController().obtainStarter(null, "moveTaskToFront").shouldAbortBackgroundActivityStart(callingUid, callingPid, callingPackage, -1, -1, callerApp, null, false, null) || isBackgroundActivityStartsEnabled()) {
                try {
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId);
                    if (task == null) {
                        Slog.d(TAG, "Could not find task for id: " + taskId);
                        SafeActivityOptions.abort(options);
                        Binder.restoreCallingIdentity(origId);
                    } else if (getLockTaskController().isLockTaskModeViolation(task)) {
                        Slog.e(TAG, "moveTaskToFront: Attempt to violate Lock Task Mode");
                        SafeActivityOptions.abort(options);
                        Binder.restoreCallingIdentity(origId);
                    } else {
                        if (options != null) {
                            try {
                                realOptions = options.getOptions(this.mStackSupervisor);
                            } catch (Throwable th) {
                                th = th;
                                Binder.restoreCallingIdentity(origId);
                                throw th;
                            }
                        } else {
                            realOptions = null;
                        }
                        if (OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).shouldAbortMoveTaskToFront(task)) {
                            Slog.d(TAG, "moveTaskToFront: abort move encryption task to front, taskId=" + taskId);
                            Binder.restoreCallingIdentity(origId);
                            return;
                        }
                        this.mStackSupervisor.findTaskToMoveToFront(task, flags, realOptions, "moveTaskToFront", false);
                        ActivityRecord topActivity = task.getTopActivity();
                        if (topActivity != null) {
                            try {
                                topActivity.showStartingWindow(null, false, true, fromRecents);
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                        Binder.restoreCallingIdentity(origId);
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Binder.restoreCallingIdentity(origId);
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSameApp(int callingUid, String packageName) {
        if (callingUid == 0 || callingUid == 1000) {
            return true;
        }
        if (packageName == null) {
            return false;
        }
        try {
            return UserHandle.isSameApp(callingUid, AppGlobals.getPackageManager().getPackageUid(packageName, 268435456, UserHandle.getUserId(callingUid)));
        } catch (RemoteException e) {
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkAppSwitchAllowedLocked(int sourcePid, int sourceUid, int callingPid, int callingUid, String name) {
        if (this.mAppSwitchesAllowedTime < SystemClock.uptimeMillis() || getRecentTasks().isCallerRecents(sourceUid) || checkComponentPermission("android.permission.STOP_APP_SWITCHES", sourcePid, sourceUid, -1, true) == 0 || checkAllowAppSwitchUid(sourceUid)) {
            return true;
        }
        if (callingUid != -1 && callingUid != sourceUid && (checkComponentPermission("android.permission.STOP_APP_SWITCHES", callingPid, callingUid, -1, true) == 0 || checkAllowAppSwitchUid(callingUid))) {
            return true;
        }
        Slog.w(TAG, name + " request from " + sourceUid + " stopped");
        return false;
    }

    private boolean checkAllowAppSwitchUid(int uid) {
        ArrayMap<String, Integer> types = this.mAllowAppSwitchUids.get(UserHandle.getUserId(uid));
        if (types == null) {
            return false;
        }
        for (int i = types.size() - 1; i >= 0; i--) {
            if (types.valueAt(i).intValue() == uid) {
                return true;
            }
        }
        return false;
    }

    public void setActivityController(IActivityController controller, boolean imAMonkey) {
        this.mAmInternal.enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "setActivityController()");
        if (SystemProperties.getBoolean("oppo.app.secure.enable", false)) {
            Slog.i(TAG, "can't setActivityController because safe");
            return;
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                OppoSecureProtectUtils.setMonkeyControllerLocked(this, controller);
                this.mControllerIsAMonkey = imAMonkey;
                if (this.mOppoActivityControlerScheduler != null) {
                    this.mOppoActivityControlerScheduler.exitRunningScheduler();
                }
                if (this.mController != null) {
                    this.mOppoActivityControlerScheduler = new OppoActivityControlerScheduler(this.mController);
                } else {
                    this.mOppoActivityControlerScheduler = null;
                }
                Watchdog.getInstance().setActivityController(controller);
                if (controller == null) {
                    OppoSecureProtectUtils.nofityMonkeyFinish(this);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isControllerAMonkey() {
        boolean z;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                z = this.mController != null && this.mControllerIsAMonkey && OppoSecureProtectUtils.isMonkeyController(this);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public int getTaskForActivity(IBinder token, boolean onlyRoot) {
        int taskForActivityLocked;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                taskForActivityLocked = ActivityRecord.getTaskForActivityLocked(token, onlyRoot);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return taskForActivityLocked;
    }

    public List<ActivityManager.RunningTaskInfo> getTasks(int maxNum) {
        return getFilteredTasks(maxNum, 0, 0);
    }

    public List<ActivityManager.RunningTaskInfo> getFilteredTasks(int maxNum, @WindowConfiguration.ActivityType int ignoreActivityType, @WindowConfiguration.WindowingMode int ignoreWindowingMode) {
        int callingUid = Binder.getCallingUid();
        ArrayList<ActivityManager.RunningTaskInfo> list = new ArrayList<>();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
                    Slog.v(TAG, "getTasks: max=" + maxNum);
                }
                this.mRootActivityContainer.getRunningTasks(maxNum, list, ignoreActivityType, ignoreWindowingMode, callingUid, isGetTasksAllowed("getTasks", Binder.getCallingPid(), callingUid));
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return list;
    }

    public final void finishSubActivity(IBinder token, String resultWho, int requestCode) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    r.getActivityStack().finishSubActivityLocked(r, resultWho, requestCode);
                }
                Binder.restoreCallingIdentity(origId);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean willActivityBeVisible(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack != null) {
                    boolean willActivityBeVisibleLocked = stack.willActivityBeVisibleLocked(token);
                    return willActivityBeVisibleLocked;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, boolean, int, boolean, boolean, java.lang.String):boolean
     arg types: [com.android.server.wm.ActivityStack, boolean, int, int, int, java.lang.String]
     candidates:
      com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, int, int, boolean, boolean, java.lang.String):boolean
      com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, boolean, int, boolean, boolean, java.lang.String):boolean */
    public void moveTaskToStack(int taskId, int stackId, boolean toTop) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "moveTaskToStack()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId);
                    if (task == null) {
                        Slog.w(TAG, "moveTaskToStack: No task for id=" + taskId);
                        return;
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                        String str = TAG_STACK;
                        Slog.d(str, "moveTaskToStack: moving task=" + taskId + " to stackId=" + stackId + " toTop=" + toTop);
                    }
                    ActivityStack stack = this.mRootActivityContainer.getStack(stackId);
                    if (stack == null) {
                        throw new IllegalStateException("moveTaskToStack: No stack for stackId=" + stackId);
                    } else if (stack.isActivityTypeStandardOrUndefined()) {
                        if (stack.inSplitScreenPrimaryWindowingMode()) {
                            this.mWindowManager.setDockedStackCreateState(0, null);
                        }
                        task.reparent(stack, toTop, 1, true, false, "moveTaskToStack");
                        Binder.restoreCallingIdentity(ident);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else {
                        throw new IllegalArgumentException("moveTaskToStack: Attempt to move task " + taskId + " to stack " + stackId);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00b9, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00c0, code lost:
        return;
     */
    public void resizeStack(int stackId, Rect destBounds, boolean allowResizeInDockedMode, boolean preserveWindows, boolean animate, int animationDuration) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "resizeStack()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (animate) {
                        try {
                            ActivityStack stack = this.mRootActivityContainer.getStack(stackId);
                            if (stack == null) {
                                Slog.w(TAG, "resizeStack: stackId " + stackId + " not found.");
                                WindowManagerService.resetPriorityAfterLockedSection();
                            } else if (stack.getWindowingMode() == 2) {
                                stack.animateResizePinnedStack(null, destBounds, animationDuration, false);
                            } else {
                                throw new IllegalArgumentException("Stack: " + stackId + " doesn't support animated resize.");
                            }
                        } catch (Throwable th) {
                            th = th;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } else {
                        ActivityStack stack2 = this.mRootActivityContainer.getStack(stackId);
                        if (stack2 == null) {
                            Slog.w(TAG, "resizeStack: stackId " + stackId + " not found.");
                            WindowManagerService.resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        this.mRootActivityContainer.resizeStack(stack2, destBounds, null, null, preserveWindows, allowResizeInDockedMode, false);
                    }
                } catch (Throwable th2) {
                    th = th2;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    public void offsetPinnedStackBounds(int stackId, Rect compareBounds, int xOffset, int yOffset, int animationDuration) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "offsetPinnedStackBounds()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (xOffset == 0 && yOffset == 0) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    ActivityStack stack = this.mRootActivityContainer.getStack(stackId);
                    if (stack == null) {
                        Slog.w(TAG, "offsetPinnedStackBounds: stackId " + stackId + " not found.");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(ident);
                    } else if (stack.getWindowingMode() == 2) {
                        Rect destBounds = new Rect();
                        stack.getAnimationOrCurrentBounds(destBounds);
                        if (destBounds.isEmpty() || !destBounds.equals(compareBounds)) {
                            Slog.w(TAG, "The current stack bounds does not matched! It may be obsolete.");
                            WindowManagerService.resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        destBounds.offset(xOffset, yOffset);
                        stack.animateResizePinnedStack(null, destBounds, animationDuration, false);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(ident);
                    } else {
                        throw new IllegalArgumentException("Stack: " + stackId + " doesn't support animated resize.");
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean setTaskWindowingModeSplitScreenPrimary(int taskId, int createMode, boolean toTop, boolean animate, Rect initialBounds, boolean showRecents) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setTaskWindowingModeSplitScreenPrimary()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    boolean z = false;
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        Slog.w(TAG, "setTaskWindowingModeSplitScreenPrimary: No task for id=" + taskId);
                        Binder.restoreCallingIdentity(ident);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                        String str = TAG_STACK;
                        Slog.d(str, "setTaskWindowingModeSplitScreenPrimary: moving task=" + taskId + " to createMode=" + createMode + " toTop=" + toTop);
                    }
                    if (task.isActivityTypeStandardOrUndefined()) {
                        try {
                            this.mWindowManager.setDockedStackCreateState(createMode, initialBounds);
                            int windowingMode = task.getWindowingMode();
                            ActivityStack stack = task.getStack();
                            if (toTop) {
                                stack.moveToFront("setTaskWindowingModeSplitScreenPrimary", task);
                            }
                            stack.setWindowingMode(3, animate, showRecents, false, false, false);
                            OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).notifyInSplitScreenMode(stack);
                            if (windowingMode != task.getWindowingMode()) {
                                z = true;
                            }
                        } catch (Throwable th) {
                            th = th;
                            Binder.restoreCallingIdentity(ident);
                            throw th;
                        }
                        try {
                            Binder.restoreCallingIdentity(ident);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return z;
                        } catch (Throwable th2) {
                            th = th2;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } else {
                        throw new IllegalArgumentException("setTaskWindowingMode: Attempt to move non-standard task " + taskId + " to split-screen windowing mode");
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    public void removeStacksInWindowingModes(int[] windowingModes) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "removeStacksInWindowingModes()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    this.mRootActivityContainer.removeStacksInWindowingModes(windowingModes);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void removeStacksWithActivityTypes(int[] activityTypes) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "removeStacksWithActivityTypes()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    this.mRootActivityContainer.removeStacksWithActivityTypes(activityTypes);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public ParceledListSlice<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum, int flags, int userId) {
        ParceledListSlice<ActivityManager.RecentTaskInfo> recentTasks;
        int callingUid = Binder.getCallingUid();
        int userId2 = handleIncomingUser(Binder.getCallingPid(), callingUid, userId, "getRecentTasks");
        boolean allowed = isGetTasksAllowed("getRecentTasks", Binder.getCallingPid(), callingUid);
        boolean detailed = checkGetTasksPermission("android.permission.GET_DETAILED_TASKS", Binder.getCallingPid(), UserHandle.getAppId(callingUid)) == 0;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                recentTasks = this.mRecentTasks.getRecentTasks(maxNum, flags, allowed, detailed, userId2, callingUid);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return recentTasks;
    }

    /* JADX INFO: finally extract failed */
    public List<ActivityManager.StackInfo> getAllStackInfos() {
        ArrayList<ActivityManager.StackInfo> allStackInfos;
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getAllStackInfos()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    allStackInfos = this.mRootActivityContainer.getAllStackInfos();
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return allStackInfos;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    public ActivityManager.StackInfo getStackInfo(int windowingMode, int activityType) {
        ActivityManager.StackInfo stackInfo;
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getStackInfo()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    stackInfo = this.mRootActivityContainer.getStackInfo(windowingMode, activityType);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return stackInfo;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    public void cancelRecentsAnimation(boolean restoreHomeStackPosition) {
        int i;
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "cancelRecentsAnimation()");
        long callingUid = (long) Binder.getCallingUid();
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowManagerService windowManagerService = this.mWindowManager;
                    if (restoreHomeStackPosition) {
                        i = 2;
                    } else {
                        i = 0;
                    }
                    windowManagerService.cancelRecentsAnimationSynchronously(i, "cancelRecentsAnimation/uid=" + callingUid);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void startLockTaskModeByToken(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.forTokenLocked(token);
                if (r != null) {
                    startLockTaskModeLocked(r.getTaskRecord(), false);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void startSystemLockTaskMode(int taskId) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "startSystemLockTaskMode");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    task.getStack().moveToFront("startSystemLockTaskMode");
                    startLockTaskModeLocked(task, true);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void stopLockTaskModeByToken(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.forTokenLocked(token);
                if (r != null) {
                    stopLockTaskModeInternal(r.getTaskRecord(), false);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void stopSystemLockTaskMode() throws RemoteException {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "stopSystemLockTaskMode");
        stopLockTaskModeInternal(null, true);
    }

    private void startLockTaskModeLocked(TaskRecord task, boolean isSystemCaller) {
        if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
            String str = TAG_LOCKTASK;
            Slog.w(str, "startLockTaskModeLocked: " + task);
        }
        if (task != null && task.mLockTaskAuth != 0) {
            ActivityStack stack = this.mRootActivityContainer.getTopDisplayFocusedStack();
            if (stack == null || task != stack.topTask()) {
                throw new IllegalArgumentException("Invalid task, not in foreground");
            }
            int callingUid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                this.mRootActivityContainer.removeStacksInWindowingModes(2);
                getLockTaskController().startLockTaskMode(task, isSystemCaller, callingUid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private void stopLockTaskModeInternal(TaskRecord task, boolean isSystemCaller) {
        int callingUid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    getLockTaskController().stopLockTaskMode(task, isSystemCaller, callingUid);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            TelecomManager tm = (TelecomManager) this.mContext.getSystemService("telecom");
            if (tm != null) {
                tm.showInCallScreen(false);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void updateLockTaskPackages(int userId, String[] packages) {
        int callingUid = Binder.getCallingUid();
        if (!(callingUid == 0 || callingUid == 1000)) {
            this.mAmInternal.enforceCallingPermission("android.permission.UPDATE_LOCK_TASK_PACKAGES", "updateLockTaskPackages()");
        }
        synchronized (this) {
            if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
                String str = TAG_LOCKTASK;
                Slog.w(str, "Whitelisting " + userId + ":" + Arrays.toString(packages));
            }
            getLockTaskController().updateLockTaskPackages(userId, packages);
        }
    }

    public boolean isInLockTaskMode() {
        return getLockTaskModeState() != 0;
    }

    public int getLockTaskModeState() {
        int lockTaskModeState;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                lockTaskModeState = getLockTaskController().getLockTaskModeState();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return lockTaskModeState;
    }

    public void setTaskDescription(IBinder token, ActivityManager.TaskDescription td) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    r.setTaskDescription(td);
                    TaskRecord task = r.getTaskRecord();
                    task.updateTaskDescription();
                    this.mTaskChangeNotificationController.notifyTaskDescriptionChanged(task.getTaskInfo());
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0024, code lost:
        return r4;
     */
    public Bundle getActivityOptions(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    Bundle bundle = null;
                    if (r != null) {
                        ActivityOptions activityOptions = r.takeOptionsLocked(true);
                        if (activityOptions != null) {
                            bundle = activityOptions.toBundle();
                        }
                    } else {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return null;
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    public List<IBinder> getAppTasks(String callingPackage) {
        ArrayList<IBinder> appTasksList;
        int callingUid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    appTasksList = this.mRecentTasks.getAppTasksList(callingUid, callingPackage);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return appTasksList;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void finishVoiceTask(IVoiceInteractionSession session) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    this.mRootActivityContainer.finishVoiceTask(session);
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isTopOfTask(IBinder token) {
        boolean z;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                z = r != null && r.getTaskRecord().getTopActivity() == r;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public void notifyLaunchTaskBehindComplete(IBinder token) {
        this.mStackSupervisor.scheduleLaunchTaskBehindComplete(token);
    }

    public void notifyEnterAnimationComplete(IBinder token) {
        this.mH.post(new Runnable(token) {
            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$BXul1K8BX6JEv_ff3NT76qpeZGQ */
            private final /* synthetic */ IBinder f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ActivityTaskManagerService.this.lambda$notifyEnterAnimationComplete$1$ActivityTaskManagerService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$notifyEnterAnimationComplete$1$ActivityTaskManagerService(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.forTokenLocked(token);
                if (r != null && r.attachedToProcess()) {
                    try {
                        r.app.getThread().scheduleEnterAnimationComplete(r.appToken);
                    } catch (RemoteException e) {
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003d, code lost:
        r2 = null;
        r3 = r9.mGlobalLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0041, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        com.android.server.wm.WindowManagerService.boostPriorityForLockedSection();
        buildAssistBundleLocked(r0, r11);
        r4 = r9.mPendingAssistExtras.remove(r0);
        r9.mUiHandler.removeCallbacks(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0053, code lost:
        if (r4 != false) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0055, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0059, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005a, code lost:
        r5 = r0.receiver;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005d, code lost:
        if (r5 == null) goto L_0x0097;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005f, code lost:
        r2 = new android.os.Bundle();
        r2.putInt(com.android.server.wm.ActivityTaskManagerInternal.ASSIST_TASK_ID, r0.activity.getTaskRecord().taskId);
        r2.putBinder(com.android.server.wm.ActivityTaskManagerInternal.ASSIST_ACTIVITY_ID, r0.activity.assistToken);
        r2.putBundle("data", r0.extras);
        r2.putParcelable(com.android.server.wm.ActivityTaskManagerInternal.ASSIST_KEY_STRUCTURE, r0.structure);
        r2.putParcelable(com.android.server.wm.ActivityTaskManagerInternal.ASSIST_KEY_CONTENT, r0.content);
        r2.putBundle(com.android.server.wm.ActivityTaskManagerInternal.ASSIST_KEY_RECEIVER_EXTRAS, r0.receiverExtras);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0097, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0098, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x009b, code lost:
        if (r5 == null) goto L_0x00a3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r5.onHandleAssistData(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00a3, code lost:
        r3 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b3, code lost:
        if (android.text.TextUtils.equals(r0.intent.getAction(), "android.service.voice.VoiceInteractionService") == false) goto L_0x00c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00b5, code lost:
        r0.intent.putExtras(r0.extras);
        startVoiceInteractionServiceAsUser(r0.intent, r0.userHandle, "AssistContext");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c6, code lost:
        r0.intent.replaceExtras(r0.extras);
        r0.intent.setFlags(872415232);
        r9.mInternal.closeSystemDialogs(com.android.server.policy.PhoneWindowManager.SYSTEM_DIALOG_REASON_ASSIST);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        r9.mContext.startActivityAsUser(r0.intent, new android.os.UserHandle(r0.userHandle));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ea, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00eb, code lost:
        android.util.Slog.w(com.android.server.wm.ActivityTaskManagerService.TAG, "No activity to handle assist action.", r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00f7, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f8, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00fb, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00fc, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00fe, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0101, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        return;
     */
    public void reportAssistContextExtras(IBinder token, Bundle extras, AssistStructure structure, AssistContent content, Uri referrer) {
        PendingAssistExtras pae = (PendingAssistExtras) token;
        synchronized (pae) {
            pae.result = extras;
            pae.structure = structure;
            pae.content = content;
            if (referrer != null) {
                pae.extras.putParcelable("android.intent.extra.REFERRER", referrer);
            }
            if (structure != null) {
                structure.setTaskId(pae.activity.getTaskRecord().taskId);
                structure.setActivityComponent(pae.activity.mActivityComponent);
                structure.setHomeActivity(pae.isHome);
            }
            pae.haveResult = true;
            pae.notifyAll();
            if (pae.intent == null && pae.receiver == null) {
            }
        }
    }

    private void startVoiceInteractionServiceAsUser(Intent intent, int userHandle, String reason) {
        ResolveInfo resolveInfo = this.mContext.getPackageManager().resolveServiceAsUser(intent, 0, userHandle);
        if (resolveInfo == null || resolveInfo.serviceInfo == null) {
            Slog.e(TAG, "VoiceInteractionService intent does not resolve. Not starting.");
            return;
        }
        intent.setPackage(resolveInfo.serviceInfo.packageName);
        ((DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class)).addPowerSaveTempWhitelistApp(Process.myUid(), intent.getPackage(), 5000, userHandle, false, reason);
        try {
            this.mContext.startServiceAsUser(intent, UserHandle.of(userHandle));
        } catch (RuntimeException e) {
            Slog.e(TAG, "VoiceInteractionService failed to start.", e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0160, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x016e, code lost:
        r0 = th;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:35:0x00a5, B:52:0x0155] */
    public int addAppTask(IBinder activityToken, Intent intent, ActivityManager.TaskDescription description, Bitmap thumbnail) throws RemoteException {
        int callingUid = Binder.getCallingUid();
        long callingIdent = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(activityToken);
                    if (r != null) {
                        try {
                            ComponentName comp = intent.getComponent();
                            if (comp != null) {
                                if (thumbnail.getWidth() == this.mThumbnailWidth) {
                                    if (thumbnail.getHeight() == this.mThumbnailHeight) {
                                        if (intent.getSelector() != null) {
                                            intent.setSelector(null);
                                        }
                                        if (intent.getSourceBounds() != null) {
                                            intent.setSourceBounds(null);
                                        }
                                        if ((intent.getFlags() & DumpState.DUMP_FROZEN) != 0 && (intent.getFlags() & 8192) == 0) {
                                            intent.addFlags(8192);
                                        }
                                        ActivityInfo ainfo = AppGlobals.getPackageManager().getActivityInfo(comp, 1024, UserHandle.getUserId(callingUid));
                                        if (ainfo.applicationInfo.uid == callingUid) {
                                            ActivityStack stack = r.getActivityStack();
                                            TaskRecord task = stack.createTaskRecord(this.mStackSupervisor.getNextTaskIdForUserLocked(r.mUserId), ainfo, intent, null, null, false);
                                            if (!this.mRecentTasks.addToBottom(task)) {
                                                stack.removeTask(task, "addAppTask", 0);
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                Binder.restoreCallingIdentity(callingIdent);
                                                return -1;
                                            }
                                            task.lastTaskDescription.copyFrom(description);
                                            int i = task.taskId;
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            Binder.restoreCallingIdentity(callingIdent);
                                            return i;
                                        }
                                        throw new SecurityException("Can't add task for another application: target uid=" + ainfo.applicationInfo.uid + ", calling uid=" + callingUid);
                                    }
                                }
                                throw new IllegalArgumentException("Bad thumbnail size: got " + thumbnail.getWidth() + "x" + thumbnail.getHeight() + ", require " + this.mThumbnailWidth + "x" + this.mThumbnailHeight);
                            }
                            throw new IllegalArgumentException("Intent " + intent + " must specify explicit component");
                        } catch (Throwable th) {
                            th = th;
                            try {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            } catch (Throwable th2) {
                                th = th2;
                                Binder.restoreCallingIdentity(callingIdent);
                                throw th;
                            }
                        }
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Activity does not exist; token=");
                        sb.append(activityToken);
                        throw new IllegalArgumentException(sb.toString());
                    }
                } catch (Throwable th3) {
                    th = th3;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } catch (Throwable th4) {
            th = th4;
            Binder.restoreCallingIdentity(callingIdent);
            throw th;
        }
    }

    public Point getAppTaskThumbnailSize() {
        Point point;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                point = new Point(this.mThumbnailWidth, this.mThumbnailHeight);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return point;
    }

    public void setTaskResizeable(int taskId, int resizeableMode) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 1);
                if (task == null) {
                    Slog.w(TAG, "setTaskResizeable: taskId=" + taskId + " not found");
                    return;
                }
                task.setResizeMode(resizeableMode);
                WindowManagerService.resetPriorityAfterLockedSection();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, boolean, int, boolean, boolean, java.lang.String):boolean
     arg types: [com.android.server.wm.ActivityStack, int, int, int, int, java.lang.String]
     candidates:
      com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, int, int, boolean, boolean, java.lang.String):boolean
      com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, boolean, int, boolean, boolean, java.lang.String):boolean */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x007a A[SYNTHETIC, Splitter:B:21:0x007a] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00ab A[Catch:{ all -> 0x00e4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00ac A[Catch:{ all -> 0x00e4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00b5 A[Catch:{ all -> 0x00e4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00c9 A[Catch:{ all -> 0x00e4 }] */
    public void resizeTask(int taskId, Rect bounds, int resizeMode) {
        Rect bounds2;
        ActivityStack stack;
        boolean preserveWindow;
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "resizeTask()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        Slog.w(TAG, "resizeTask: taskId=" + taskId + " not found");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    ActivityStack stack2 = task.getStack();
                    boolean preserveWindow2 = true;
                    if (!task.getWindowConfiguration().canResizeTask()) {
                        Slog.e(TAG, "resizeTask not allowed on task=" + task);
                        if (stack2 != null && stack2.getWindowingMode() == 1) {
                            bounds2 = null;
                            if (bounds2 == null) {
                                try {
                                    if (stack2.getWindowingMode() == 5) {
                                        stack = stack2.getDisplay().getOrCreateStack(1, stack2.getActivityType(), true);
                                        if ((resizeMode & 1) == 0) {
                                            preserveWindow2 = false;
                                        }
                                        if (stack != task.getStack()) {
                                            task.reparent(stack, true, 1, true, true, "resizeTask");
                                            preserveWindow = false;
                                        } else {
                                            preserveWindow = preserveWindow2;
                                        }
                                        task.resize(bounds2, resizeMode, preserveWindow, false);
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                        Binder.restoreCallingIdentity(ident);
                                    }
                                } catch (Throwable th) {
                                    th = th;
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            if (bounds2 != null || stack2.getWindowingMode() == 5) {
                                stack = stack2;
                            } else {
                                stack = stack2.getDisplay().getOrCreateStack(5, stack2.getActivityType(), true);
                            }
                            if ((resizeMode & 1) == 0) {
                            }
                            if (stack != task.getStack()) {
                            }
                            task.resize(bounds2, resizeMode, preserveWindow, false);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(ident);
                        }
                    }
                    bounds2 = bounds;
                    if (bounds2 == null) {
                    }
                    if (bounds2 != null) {
                    }
                    stack = stack2;
                    if ((resizeMode & 1) == 0) {
                    }
                    if (stack != task.getStack()) {
                    }
                    task.resize(bounds2, resizeMode, preserveWindow, false);
                    try {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th2) {
                        th = th2;
                        Binder.restoreCallingIdentity(ident);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } catch (Throwable th4) {
            th = th4;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    public boolean releaseActivityInstance(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        return false;
                    }
                    boolean safelyDestroyActivityLocked = r.getActivityStack().safelyDestroyActivityLocked(r, "app-req");
                    Binder.restoreCallingIdentity(origId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return safelyDestroyActivityLocked;
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void releaseSomeActivities(IApplicationThread appInt) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    this.mRootActivityContainer.releaseSomeActivitiesLocked(getProcessController(appInt), "low-mem");
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void setLockScreenShown(boolean keyguardShowing, boolean aodShowing) {
        if (checkCallingPermission("android.permission.DEVICE_POWER") == 0) {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    long ident = Binder.clearCallingIdentity();
                    if (this.mKeyguardShown != keyguardShowing) {
                        this.mKeyguardShown = keyguardShowing;
                        this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$zwLNi4Hz7werGBGptK8eYRpBWpw.INSTANCE, this.mAmInternal, Boolean.valueOf(keyguardShowing)));
                    }
                    try {
                        this.mKeyguardController.setKeyguardShown(keyguardShowing, aodShowing);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            this.mH.post(new Runnable(keyguardShowing) {
                /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$iduseKQrjIWQYD0hJ8Q5DMmuSfE */
                private final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ActivityTaskManagerService.this.lambda$setLockScreenShown$2$ActivityTaskManagerService(this.f$1);
                }
            });
            return;
        }
        throw new SecurityException("Requires permission android.permission.DEVICE_POWER");
    }

    public /* synthetic */ void lambda$setLockScreenShown$2$ActivityTaskManagerService(boolean keyguardShowing) {
        for (int i = this.mScreenObservers.size() - 1; i >= 0; i--) {
            this.mScreenObservers.get(i).onKeyguardStateChanged(keyguardShowing);
        }
    }

    public void onScreenAwakeChanged(boolean isAwake) {
        this.mH.post(new Runnable(isAwake) {
            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$Uli7s8UWTEj0IpBUtoST5bmgvKk */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ActivityTaskManagerService.this.lambda$onScreenAwakeChanged$3$ActivityTaskManagerService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onScreenAwakeChanged$3$ActivityTaskManagerService(boolean isAwake) {
        for (int i = this.mScreenObservers.size() - 1; i >= 0; i--) {
            this.mScreenObservers.get(i).onAwakeStateChanged(isAwake);
        }
    }

    public Bitmap getTaskDescriptionIcon(String filePath, int userId) {
        int userId2 = handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTaskDescriptionIcon");
        if (new File(TaskPersister.getUserImagesDir(userId2), new File(filePath).getName()).getPath().equals(filePath) && filePath.contains("_activity_icon_")) {
            return this.mRecentTasks.getTaskDescriptionIcon(filePath);
        }
        throw new IllegalArgumentException("Bad file path: " + filePath + " passed for userId " + userId2);
    }

    public void startInPlaceAnimationOnFrontMostApplication(Bundle opts) {
        ActivityOptions activityOptions;
        SafeActivityOptions safeOptions = SafeActivityOptions.fromBundle(opts);
        if (safeOptions != null) {
            activityOptions = safeOptions.getOptions(this.mStackSupervisor);
        } else {
            activityOptions = null;
        }
        if (activityOptions == null || activityOptions.getAnimationType() != 10 || activityOptions.getCustomInPlaceResId() == 0) {
            throw new IllegalArgumentException("Expected in-place ActivityOption with valid animation");
        }
        ActivityStack focusedStack = getTopDisplayFocusedStack();
        if (focusedStack != null) {
            DisplayContent dc = focusedStack.getDisplay().mDisplayContent;
            dc.prepareAppTransition(17, false);
            dc.mAppTransition.overrideInPlaceAppTransition(activityOptions.getPackageName(), activityOptions.getCustomInPlaceResId());
            dc.executeAppTransition();
        }
    }

    public void removeStack(int stackId) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "removeStack()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    ActivityStack stack = this.mRootActivityContainer.getStack(stackId);
                    if (stack == null) {
                        Slog.w(TAG, "removeStack: No stack with id=" + stackId);
                    } else if (stack.isActivityTypeStandardOrUndefined()) {
                        this.mStackSupervisor.removeStack(stack);
                        Binder.restoreCallingIdentity(ident);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else {
                        throw new IllegalArgumentException("Removing non-standard stack is not allowed.");
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void moveStackToDisplay(int stackId, int displayId) {
        this.mAmInternal.enforceCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "moveStackToDisplay()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                        String str = TAG_STACK;
                        Slog.d(str, "moveStackToDisplay: moving stackId=" + stackId + " to displayId=" + displayId);
                    }
                    this.mRootActivityContainer.moveStackToDisplay(stackId, displayId, true);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void toggleFreeformWindowingMode(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    ActivityRecord r = ActivityRecord.forTokenLocked(token);
                    if (r != null) {
                        ActivityStack stack = r.getActivityStack();
                        if (stack != null) {
                            if (!stack.inFreeformWindowingMode()) {
                                if (stack.getWindowingMode() != 1) {
                                    throw new IllegalStateException("toggleFreeformWindowingMode: You can only toggle between fullscreen and freeform.");
                                }
                            }
                            if (stack.inFreeformWindowingMode()) {
                                stack.setWindowingMode(1);
                            } else if (stack.getParent().inFreeformWindowingMode()) {
                                stack.setWindowingMode(0);
                            } else {
                                stack.setWindowingMode(5);
                            }
                        } else {
                            throw new IllegalStateException("toggleFreeformWindowingMode: the activity doesn't have a stack");
                        }
                    } else {
                        throw new IllegalArgumentException("toggleFreeformWindowingMode: No activity record matching token=" + token);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerTaskStackListener(ITaskStackListener listener) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "registerTaskStackListener()");
        this.mTaskChangeNotificationController.registerTaskStackListener(listener);
    }

    public void unregisterTaskStackListener(ITaskStackListener listener) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "unregisterTaskStackListener()");
        this.mTaskChangeNotificationController.unregisterTaskStackListener(listener);
    }

    public boolean requestAssistContextExtras(int requestType, IAssistDataReceiver receiver, Bundle receiverExtras, IBinder activityToken, boolean focused, boolean newSessionId) {
        return enqueueAssistContext(requestType, null, null, receiver, receiverExtras, activityToken, focused, newSessionId, UserHandle.getCallingUserId(), null, 2000, 0) != null;
    }

    public boolean requestAutofillData(IAssistDataReceiver receiver, Bundle receiverExtras, IBinder activityToken, int flags) {
        return enqueueAssistContext(2, null, null, receiver, receiverExtras, activityToken, true, true, UserHandle.getCallingUserId(), null, 2000, flags) != null;
    }

    public boolean launchAssistIntent(Intent intent, int requestType, String hint, int userHandle, Bundle args) {
        return enqueueAssistContext(requestType, intent, hint, null, null, null, true, true, userHandle, args, 500, 0) != null;
    }

    /* JADX INFO: finally extract failed */
    public Bundle getAssistContextExtras(int requestType) {
        PendingAssistExtras pae = enqueueAssistContext(requestType, null, null, null, null, null, true, true, UserHandle.getCallingUserId(), null, 500, 0);
        if (pae == null) {
            return null;
        }
        synchronized (pae) {
            while (!pae.haveResult) {
                try {
                    pae.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                buildAssistBundleLocked(pae, pae.result);
                this.mPendingAssistExtras.remove(pae);
                this.mUiHandler.removeCallbacks(pae);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        return pae.extras;
    }

    private static int checkCallingPermission(String permission) {
        return checkPermission(permission, Binder.getCallingPid(), UserHandle.getAppId(Binder.getCallingUid()));
    }

    /* access modifiers changed from: private */
    public void enforceCallerIsRecentsOrHasPermission(String permission, String func) {
        if (!getRecentTasks().isCallerRecents(Binder.getCallingUid())) {
            this.mAmInternal.enforceCallingPermission(permission, func);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int checkGetTasksPermission(String permission, int pid, int uid) {
        return checkPermission(permission, pid, uid);
    }

    static int checkPermission(String permission, int pid, int uid) {
        if (permission == null) {
            return -1;
        }
        return checkComponentPermission(permission, pid, uid, -1, true);
    }

    public static int checkComponentPermission(String permission, int pid, int uid, int owningUid, boolean exported) {
        return ActivityManagerService.checkComponentPermission(permission, pid, uid, owningUid, exported);
    }

    /* access modifiers changed from: package-private */
    public boolean isGetTasksAllowed(String caller, int callingPid, int callingUid) {
        boolean z = true;
        if (getRecentTasks().isCallerRecents(callingUid)) {
            return true;
        }
        if (checkGetTasksPermission("android.permission.REAL_GET_TASKS", callingPid, callingUid) != 0) {
            z = false;
        }
        boolean allowed = z;
        if (!allowed) {
            if (checkGetTasksPermission("android.permission.GET_TASKS", callingPid, callingUid) == 0) {
                try {
                    if (AppGlobals.getPackageManager().isUidPrivileged(callingUid)) {
                        allowed = true;
                        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                            Slog.w(TAG, caller + ": caller " + callingUid + " is using old GET_TASKS but privileged; allowing");
                        }
                    }
                } catch (RemoteException e) {
                }
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.w(TAG, caller + ": caller " + callingUid + " does not hold REAL_GET_TASKS; limiting output");
            }
        }
        return allowed;
    }

    private PendingAssistExtras enqueueAssistContext(int requestType, Intent intent, String hint, IAssistDataReceiver receiver, Bundle receiverExtras, IBinder activityToken, boolean focused, boolean newSessionId, int userHandle, Bundle args, long timeout, int flags) {
        ActivityRecord activity;
        ActivityRecord caller;
        this.mAmInternal.enforceCallingPermission("android.permission.GET_TOP_ACTIVITY_INFO", "enqueueAssistContext()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord activity2 = getTopDisplayFocusedStack().getTopActivity();
                if (activity2 == null) {
                    Slog.w(TAG, "getAssistContextExtras failed: no top activity");
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return null;
                } else if (!activity2.attachedToProcess()) {
                    Slog.w(TAG, "getAssistContextExtras failed: no process for " + activity2);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return null;
                } else {
                    if (!focused) {
                        ActivityRecord activity3 = ActivityRecord.forTokenLocked(activityToken);
                        if (activity3 == null) {
                            Slog.w(TAG, "enqueueAssistContext failed: activity for token=" + activityToken + " couldn't be found");
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return null;
                        } else if (!activity3.attachedToProcess()) {
                            Slog.w(TAG, "enqueueAssistContext failed: no process for " + activity3);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return null;
                        } else {
                            activity = activity3;
                        }
                    } else if (activityToken == null || activity2 == (caller = ActivityRecord.forTokenLocked(activityToken))) {
                        activity = activity2;
                    } else {
                        Slog.w(TAG, "enqueueAssistContext failed: caller " + caller + " is not current top " + activity2);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    }
                    Bundle extras = new Bundle();
                    if (args != null) {
                        extras.putAll(args);
                    }
                    extras.putString("android.intent.extra.ASSIST_PACKAGE", activity.packageName);
                    extras.putInt("android.intent.extra.ASSIST_UID", activity.app.mUid);
                    PendingAssistExtras pae = new PendingAssistExtras(activity, extras, intent, hint, receiver, receiverExtras, userHandle);
                    pae.isHome = activity.isActivityTypeHome();
                    if (newSessionId) {
                        this.mViSessionId++;
                    }
                    try {
                        activity.app.getThread().requestAssistContextExtras(activity.appToken, pae, requestType, this.mViSessionId, flags);
                        this.mPendingAssistExtras.add(pae);
                        try {
                            this.mUiHandler.postDelayed(pae, timeout);
                        } catch (RemoteException e) {
                        }
                    } catch (RemoteException e2) {
                        Slog.w(TAG, "getAssistContextExtras failed: crash calling " + activity);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    }
                    try {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return pae;
                    } catch (Throwable th) {
                        e = th;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw e;
                    }
                }
            } catch (Throwable th2) {
                e = th2;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw e;
            }
        }
    }

    private void buildAssistBundleLocked(PendingAssistExtras pae, Bundle result) {
        if (result != null) {
            pae.extras.putBundle("android.intent.extra.ASSIST_CONTEXT", result);
        }
        if (pae.hint != null) {
            pae.extras.putBoolean(pae.hint, true);
        }
    }

    /* access modifiers changed from: private */
    public void pendingAssistExtrasTimedOut(PendingAssistExtras pae) {
        IAssistDataReceiver receiver;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mPendingAssistExtras.remove(pae);
                receiver = pae.receiver;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (receiver != null) {
            Bundle sendBundle = new Bundle();
            sendBundle.putBundle(ActivityTaskManagerInternal.ASSIST_KEY_RECEIVER_EXTRAS, pae.receiverExtras);
            try {
                pae.receiver.onHandleAssistData(sendBundle);
            } catch (RemoteException e) {
            }
        }
    }

    public class PendingAssistExtras extends Binder implements Runnable {
        public final ActivityRecord activity;
        public AssistContent content = null;
        public final Bundle extras;
        public boolean haveResult = false;
        public final String hint;
        public final Intent intent;
        public boolean isHome;
        public final IAssistDataReceiver receiver;
        public Bundle receiverExtras;
        public Bundle result = null;
        public AssistStructure structure = null;
        public final int userHandle;

        public PendingAssistExtras(ActivityRecord _activity, Bundle _extras, Intent _intent, String _hint, IAssistDataReceiver _receiver, Bundle _receiverExtras, int _userHandle) {
            this.activity = _activity;
            this.extras = _extras;
            this.intent = _intent;
            this.hint = _hint;
            this.receiver = _receiver;
            this.receiverExtras = _receiverExtras;
            this.userHandle = _userHandle;
        }

        public void run() {
            Slog.w(ActivityTaskManagerService.TAG, "getAssistContextExtras failed: timeout retrieving from " + this.activity);
            synchronized (this) {
                this.haveResult = true;
                notifyAll();
            }
            ActivityTaskManagerService.this.pendingAssistExtrasTimedOut(this);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0035, code lost:
        return false;
     */
    public boolean isAssistDataAllowedOnCurrentActivity() {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityStack focusedStack = getTopDisplayFocusedStack();
                if (focusedStack != null) {
                    if (!focusedStack.isActivityTypeAssistant()) {
                        ActivityRecord activity = focusedStack.getTopActivity();
                        if (activity == null) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return false;
                        }
                        int userId = activity.mUserId;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return !DevicePolicyCache.getInstance().getScreenCaptureDisabled(userId);
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean showAssistFromActivity(IBinder token, Bundle args) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord caller = ActivityRecord.forTokenLocked(token);
                    ActivityRecord top = getTopDisplayFocusedStack().getTopActivity();
                    if (top != caller) {
                        Slog.w(TAG, "showAssistFromActivity failed: caller " + caller + " is not current top " + top);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    } else if (!top.nowVisible) {
                        Slog.w(TAG, "showAssistFromActivity failed: caller " + caller + " is not visible");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    } else {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        boolean showSessionForActiveService = this.mAssistUtils.showSessionForActiveService(args, 8, (IVoiceInteractionSessionShowCallback) null, token);
                        Binder.restoreCallingIdentity(ident);
                        return showSessionForActiveService;
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean isRootVoiceInteraction(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    return false;
                }
                boolean z = r.rootVoiceInteraction;
                WindowManagerService.resetPriorityAfterLockedSection();
                return z;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onLocalVoiceInteractionStartedLocked(IBinder activity, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
        ActivityRecord activityToCallback = ActivityRecord.forTokenLocked(activity);
        if (activityToCallback != null) {
            activityToCallback.setVoiceSessionLocked(voiceSession);
            try {
                activityToCallback.app.getThread().scheduleLocalVoiceInteractionStarted(activity, voiceInteractor);
                long token = Binder.clearCallingIdentity();
                try {
                    startRunningVoiceLocked(voiceSession, activityToCallback.appInfo.uid);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } catch (RemoteException e) {
                activityToCallback.clearVoiceSessionLocked();
            }
        }
    }

    private void startRunningVoiceLocked(IVoiceInteractionSession session, int targetUid) {
        Slog.d(TAG, "<<<  startRunningVoiceLocked()");
        this.mVoiceWakeLock.setWorkSource(new WorkSource(targetUid));
        IVoiceInteractionSession iVoiceInteractionSession = this.mRunningVoice;
        if (iVoiceInteractionSession == null || iVoiceInteractionSession.asBinder() != session.asBinder()) {
            boolean wasRunningVoice = this.mRunningVoice != null;
            this.mRunningVoice = session;
            if (!wasRunningVoice) {
                this.mVoiceWakeLock.acquire();
                updateSleepIfNeededLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void finishRunningVoiceLocked() {
        if (this.mRunningVoice != null) {
            this.mRunningVoice = null;
            this.mVoiceWakeLock.release();
            updateSleepIfNeededLocked();
        }
    }

    public void setVoiceKeepAwake(IVoiceInteractionSession session, boolean keepAwake) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mRunningVoice != null && this.mRunningVoice.asBinder() == session.asBinder()) {
                    if (keepAwake) {
                        this.mVoiceWakeLock.acquire();
                    } else {
                        this.mVoiceWakeLock.release();
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public ComponentName getActivityClassForToken(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    return null;
                }
                ComponentName component = r.intent.getComponent();
                WindowManagerService.resetPriorityAfterLockedSection();
                return component;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public String getPackageForToken(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    return null;
                }
                String str = r.packageName;
                WindowManagerService.resetPriorityAfterLockedSection();
                return str;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void showLockTaskEscapeMessage(IBinder token) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (ActivityRecord.forTokenLocked(token) != null) {
                    getLockTaskController().showLockTaskToast();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void keyguardGoingAway(int flags) {
        enforceNotIsolatedCaller("keyguardGoingAway");
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mKeyguardController.keyguardGoingAway(flags);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, int, int, boolean, boolean, java.lang.String):boolean
     arg types: [com.android.server.wm.ActivityStack, int, int, int, int, java.lang.String]
     candidates:
      com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, boolean, int, boolean, boolean, java.lang.String):boolean
      com.android.server.wm.TaskRecord.reparent(com.android.server.wm.ActivityStack, int, int, boolean, boolean, java.lang.String):boolean */
    public void positionTaskInStack(int taskId, int stackId, int position) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "positionTaskInStack()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                        String str = TAG_STACK;
                        Slog.d(str, "positionTaskInStack: positioning task=" + taskId + " in stackId=" + stackId + " at position=" + position);
                    }
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId);
                    if (task != null) {
                        ActivityStack stack = this.mRootActivityContainer.getStack(stackId);
                        if (stack == null) {
                            throw new IllegalArgumentException("positionTaskInStack: no stack for id=" + stackId);
                        } else if (!stack.isActivityTypeStandardOrUndefined()) {
                            throw new IllegalArgumentException("positionTaskInStack: Attempt to change the position of task " + taskId + " in/to non-standard stack");
                        } else if (task.getStack() == stack) {
                            stack.positionChildAt(task, position);
                        } else {
                            task.reparent(stack, position, 2, false, false, "positionTaskInStack");
                        }
                    } else {
                        throw new IllegalArgumentException("positionTaskInStack: no task for id=" + taskId);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void reportSizeConfigurations(IBinder token, int[] horizontalSizeConfiguration, int[] verticalSizeConfigurations, int[] smallestSizeConfigurations) {
        if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
            Slog.v(TAG, "Report configuration: " + token + StringUtils.SPACE + horizontalSizeConfiguration + StringUtils.SPACE + verticalSizeConfigurations);
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord record = ActivityRecord.isInStackLocked(token);
                if (record != null) {
                    record.setSizeConfigurations(horizontalSizeConfiguration, verticalSizeConfigurations, smallestSizeConfigurations);
                } else {
                    throw new IllegalArgumentException("reportSizeConfigurations: ActivityRecord not found for: " + token);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void dismissSplitScreenMode(boolean toTop) {
        ActivityStack otherStack;
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "dismissSplitScreenMode()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityStack stack = this.mRootActivityContainer.getDefaultDisplay().getSplitScreenPrimaryStack();
                    if (stack == null) {
                        Slog.w(TAG, "dismissSplitScreenMode: primary split-screen stack not found.");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    if (toTop) {
                        stack.moveToFront("dismissSplitScreenMode");
                    } else if (this.mRootActivityContainer.isTopDisplayFocusedStack(stack) && (otherStack = stack.getDisplay().getTopStackInWindowingMode(4)) != null) {
                        otherStack.moveToFront("dismissSplitScreenMode_other");
                    }
                    stack.setWindowingMode(0);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0042, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0049, code lost:
        return;
     */
    public void dismissPip(boolean animate, int animationDuration) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "dismissPip()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityStack stack = this.mRootActivityContainer.getDefaultDisplay().getPinnedStack();
                    if (stack == null) {
                        Slog.w(TAG, "dismissPip: pinned stack not found.");
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else if (stack.getWindowingMode() != 2) {
                        throw new IllegalArgumentException("Stack: " + stack + " doesn't support animated resize.");
                    } else if (animate) {
                        stack.animateResizePinnedStack(null, null, animationDuration, false);
                    } else {
                        this.mStackSupervisor.moveTasksToFullscreenStackLocked(stack, true);
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void suppressResizeConfigChanges(boolean suppress) throws RemoteException {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "suppressResizeConfigChanges()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mSuppressResizeConfigChanges = suppress;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void moveTasksToFullscreenStack(int fromStackId, boolean onTop) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "moveTasksToFullscreenStack()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    ActivityStack stack = this.mRootActivityContainer.getStack(fromStackId);
                    if (stack != null) {
                        if (stack.isActivityTypeStandardOrUndefined()) {
                            this.mStackSupervisor.moveTasksToFullscreenStackLocked(stack, onTop);
                        } else {
                            throw new IllegalArgumentException("You can't move tasks from non-standard stacks.");
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean moveTopActivityToPinnedStack(int stackId, Rect bounds) {
        boolean moveTopStackActivityToPinnedStack;
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "moveTopActivityToPinnedStack()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mSupportsPictureInPicture) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        moveTopStackActivityToPinnedStack = this.mRootActivityContainer.moveTopStackActivityToPinnedStack(stackId);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    throw new IllegalStateException("moveTopActivityToPinnedStack:Device doesn't support picture-in-picture mode");
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return moveTopStackActivityToPinnedStack;
    }

    /* JADX INFO: finally extract failed */
    public boolean isInMultiWindowMode(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    boolean inMultiWindowMode = r.inMultiWindowMode();
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return inMultiWindowMode;
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean isInPictureInPictureMode(IBinder token) {
        boolean isInPictureInPictureMode;
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    isInPictureInPictureMode = isInPictureInPictureMode(ActivityRecord.forTokenLocked(token));
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            return isInPictureInPictureMode;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private boolean isInPictureInPictureMode(ActivityRecord r) {
        if (r == null || r.getActivityStack() == null || !r.inPinnedWindowingMode() || r.getActivityStack().isInStackLocked(r) == null) {
            return false;
        }
        return !r.getActivityStack().getTaskStack().isAnimatingBoundsToFullscreen();
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0049, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004f, code lost:
        return true;
     */
    public boolean enterPictureInPictureMode(IBinder token, PictureInPictureParams params) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ensureValidPictureInPictureActivityParamsLocked("enterPictureInPictureMode", token, params);
                    if (isInPictureInPictureMode(r)) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return true;
                    } else if (!r.checkEnterPictureInPictureState("enterPictureInPictureMode", false)) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return false;
                    } else {
                        final Runnable enterPipRunnable = new Runnable(r, params) {
                            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$js0zprxhKzo_Mx9ozR8logP_1c */
                            private final /* synthetic */ ActivityRecord f$1;
                            private final /* synthetic */ PictureInPictureParams f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void run() {
                                ActivityTaskManagerService.this.lambda$enterPictureInPictureMode$4$ActivityTaskManagerService(this.f$1, this.f$2);
                            }
                        };
                        if (isKeyguardLocked()) {
                            dismissKeyguard(token, new KeyguardDismissCallback() {
                                /* class com.android.server.wm.ActivityTaskManagerService.AnonymousClass1 */

                                public void onDismissSucceeded() {
                                    ActivityTaskManagerService.this.mH.post(enterPipRunnable);
                                }
                            }, null);
                        } else {
                            enterPipRunnable.run();
                        }
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public /* synthetic */ void lambda$enterPictureInPictureMode$4$ActivityTaskManagerService(ActivityRecord r, PictureInPictureParams params) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                r.pictureInPictureArgs.copyOnlySet(params);
                float aspectRatio = r.pictureInPictureArgs.getAspectRatio();
                List<RemoteAction> actions = r.pictureInPictureArgs.getActions();
                this.mRootActivityContainer.moveActivityToPinnedStack(r, new Rect(r.pictureInPictureArgs.getSourceRectHint()), aspectRatio, "enterPictureInPictureMode");
                if (r.getActivityStack() != null) {
                    ActivityStack stack = r.getActivityStack();
                    stack.setPictureInPictureAspectRatio(aspectRatio);
                    stack.setPictureInPictureActions(actions);
                    MetricsLoggerWrapper.logPictureInPictureEnter(this.mContext, r.appInfo.uid, r.shortComponentName, r.supportsEnterPipOnTaskSwitch);
                    logPictureInPictureArgs(params);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void setPictureInPictureParams(IBinder token, PictureInPictureParams params) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ensureValidPictureInPictureActivityParamsLocked("setPictureInPictureParams", token, params);
                    r.pictureInPictureArgs.copyOnlySet(params);
                    if (r.inPinnedWindowingMode()) {
                        ActivityStack stack = r.getActivityStack();
                        if (!stack.isAnimatingBoundsToFullscreen()) {
                            stack.setPictureInPictureAspectRatio(r.pictureInPictureArgs.getAspectRatio());
                            stack.setPictureInPictureActions(r.pictureInPictureArgs.getActions());
                        }
                    }
                    logPictureInPictureArgs(params);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public int getMaxNumPictureInPictureActions(IBinder token) {
        return 3;
    }

    private void logPictureInPictureArgs(PictureInPictureParams params) {
        if (params.hasSetActions()) {
            MetricsLogger.histogram(this.mContext, "tron_varz_picture_in_picture_actions_count", params.getActions().size());
        }
        if (params.hasSetAspectRatio()) {
            LogMaker lm = new LogMaker(824);
            lm.addTaggedData(825, Float.valueOf(params.getAspectRatio()));
            MetricsLogger.action(lm);
        }
    }

    private ActivityRecord ensureValidPictureInPictureActivityParamsLocked(String caller, IBinder token, PictureInPictureParams params) {
        if (this.mSupportsPictureInPicture) {
            ActivityRecord r = ActivityRecord.forTokenLocked(token);
            if (r == null) {
                throw new IllegalStateException(caller + ": Can't find activity for token=" + token);
            } else if (!r.supportsPictureInPicture()) {
                throw new IllegalStateException(caller + ": Current activity does not support picture-in-picture.");
            } else if (!params.hasSetAspectRatio() || this.mWindowManager.isValidPictureInPictureAspectRatio(r.getActivityStack().mDisplayId, params.getAspectRatio())) {
                params.truncateActions(getMaxNumPictureInPictureActions(token));
                return r;
            } else {
                float minAspectRatio = this.mContext.getResources().getFloat(17105069);
                float maxAspectRatio = this.mContext.getResources().getFloat(17105068);
                throw new IllegalArgumentException(String.format(caller + ": Aspect ratio is too extreme (must be between %f and %f).", Float.valueOf(minAspectRatio), Float.valueOf(maxAspectRatio)));
            }
        } else {
            throw new IllegalStateException(caller + ": Device doesn't support picture-in-picture mode.");
        }
    }

    public IBinder getUriPermissionOwnerForActivity(IBinder activityToken) {
        Binder externalToken;
        enforceNotIsolatedCaller("getUriPermissionOwnerForActivity");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(activityToken);
                if (r != null) {
                    externalToken = r.getUriPermissionsLocked().getExternalToken();
                } else {
                    throw new IllegalArgumentException("Activity does not exist; token=" + activityToken);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return externalToken;
    }

    /* JADX INFO: finally extract failed */
    public void resizeDockedStack(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "resizeDockedStack()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mStackSupervisor.resizeDockedStackLocked(dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds, tempOtherTaskBounds, tempOtherTaskInsetBounds, true);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    public void setSplitScreenResizing(boolean resizing) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setSplitScreenResizing()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mStackSupervisor.setSplitScreenResizing(resizing);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void enforceSystemHasVrFeature() {
        if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.vr.high_performance")) {
            throw new UnsupportedOperationException("VR mode not supported on this device!");
        }
    }

    /* JADX INFO: finally extract failed */
    public int setVrMode(IBinder token, boolean enabled, ComponentName packageName) {
        ActivityRecord r;
        enforceSystemHasVrFeature();
        VrManagerInternal vrService = (VrManagerInternal) LocalServices.getService(VrManagerInternal.class);
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                r = ActivityRecord.isInStackLocked(token);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (r != null) {
            int err = vrService.hasVrPackage(packageName, r.mUserId);
            if (err != 0) {
                return err;
            }
            long callingId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        r.requestedVrComponent = enabled ? packageName : null;
                        if (r.isResumedActivityOnDisplay()) {
                            applyUpdateVrModeLocked(r);
                        }
                    } catch (Throwable th) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return 0;
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void startLocalVoiceInteraction(IBinder callingActivity, Bundle options) {
        Slog.i(TAG, "Activity tried to startLocalVoiceInteraction");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord activity = getTopDisplayFocusedStack().getTopActivity();
                if (ActivityRecord.forTokenLocked(callingActivity) == activity) {
                    if (this.mRunningVoice == null && activity.getTaskRecord().voiceSession == null) {
                        if (activity.voiceSession == null) {
                            if (activity.pendingVoiceInteractionStart) {
                                Slog.w(TAG, "Pending start of voice interaction already.");
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            activity.pendingVoiceInteractionStart = true;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            ((VoiceInteractionManagerInternal) LocalServices.getService(VoiceInteractionManagerInternal.class)).startLocalVoiceInteraction(callingActivity, options);
                            return;
                        }
                    }
                    Slog.w(TAG, "Already in a voice interaction, cannot start new voice interaction");
                    return;
                }
                throw new SecurityException("Only focused activity can call startVoiceInteraction");
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void stopLocalVoiceInteraction(IBinder callingActivity) {
        ((VoiceInteractionManagerInternal) LocalServices.getService(VoiceInteractionManagerInternal.class)).stopLocalVoiceInteraction(callingActivity);
    }

    public boolean supportsLocalVoiceInteraction() {
        return ((VoiceInteractionManagerInternal) LocalServices.getService(VoiceInteractionManagerInternal.class)).supportsLocalVoiceInteraction();
    }

    public void notifyPinnedStackAnimationStarted() {
        this.mTaskChangeNotificationController.notifyPinnedStackAnimationStarted();
    }

    public void notifyPinnedStackAnimationEnded() {
        this.mTaskChangeNotificationController.notifyPinnedStackAnimationEnded();
    }

    /* JADX INFO: finally extract failed */
    public void resizePinnedStack(Rect pinnedBounds, Rect tempPinnedTaskBounds) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "resizePinnedStack()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mStackSupervisor.resizePinnedStackLocked(pinnedBounds, tempPinnedTaskBounds);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0036, code lost:
        return false;
     */
    public boolean updateDisplayOverrideConfiguration(Configuration values, int displayId) {
        this.mAmInternal.enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateDisplayOverrideConfiguration()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                boolean z = false;
                if (this.mRootActivityContainer.isDisplayAdded(displayId)) {
                    if (values == null && this.mWindowManager != null) {
                        values = this.mWindowManager.computeNewConfiguration(displayId);
                    }
                    if (this.mWindowManager != null) {
                        this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ADNhW0r9Skcs9ezrOGURijIlyQ.INSTANCE, this.mAmInternal, Integer.valueOf(displayId)));
                    }
                    long origId = Binder.clearCallingIdentity();
                    if (values != null) {
                        try {
                            Settings.System.clearConfiguration(values);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    }
                    updateDisplayOverrideConfigurationLocked(values, null, false, displayId, this.mTmpUpdateConfigurationResult);
                    if (this.mTmpUpdateConfigurationResult.changes != 0) {
                        z = true;
                    }
                    Binder.restoreCallingIdentity(origId);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return z;
                } else if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.w(TAG, "Trying to update display configuration for non-existing displayId=" + displayId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean updateConfiguration(Configuration values) {
        boolean z;
        this.mAmInternal.enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateConfiguration()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                z = false;
                if (values == null && this.mWindowManager != null) {
                    values = this.mWindowManager.computeNewConfiguration(0);
                }
                if (this.mWindowManager != null) {
                    this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ADNhW0r9Skcs9ezrOGURijIlyQ.INSTANCE, this.mAmInternal, 0));
                }
                long origId = Binder.clearCallingIdentity();
                if (values != null) {
                    try {
                        Settings.System.clearConfiguration(values);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(origId);
                        throw th;
                    }
                }
                updateConfigurationLocked(values, null, false, false, -10000, false, this.mTmpUpdateConfigurationResult);
                if (this.mTmpUpdateConfigurationResult.changes != 0) {
                    z = true;
                }
                Binder.restoreCallingIdentity(origId);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    /* JADX INFO: finally extract failed */
    public void dismissKeyguard(IBinder token, IKeyguardDismissCallback callback, CharSequence message) {
        if (message != null) {
            this.mAmInternal.enforceCallingPermission("android.permission.SHOW_KEYGUARD_MESSAGE", "dismissKeyguard()");
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mKeyguardController.dismissKeyguard(token, callback, message);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX INFO: finally extract failed */
    public void cancelTaskWindowTransition(int taskId) {
        enforceCallerIsRecentsOrHasPermission("android.permission.MANAGE_ACTIVITY_STACKS", "cancelTaskWindowTransition()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                    if (task == null) {
                        Slog.w(TAG, "cancelTaskWindowTransition: taskId=" + taskId + " not found");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    task.cancelWindowTransition();
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution) {
        enforceCallerIsRecentsOrHasPermission("android.permission.READ_FRAME_BUFFER", "getTaskSnapshot()");
        long ident = Binder.clearCallingIdentity();
        try {
            return getTaskSnapshot(taskId, reducedResolution, true);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    public ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution, boolean restoreFromDisk) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 1);
                if (task == null) {
                    Slog.w(TAG, "getTaskSnapshot: taskId=" + taskId + " not found");
                    return null;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return task.getSnapshot(reducedResolution, restoreFromDisk);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setDisablePreviewScreenshots(IBinder token, boolean disable) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    Slog.w(TAG, "setDisablePreviewScreenshots: Unable to find activity for token=" + token);
                    return;
                }
                long origId = Binder.clearCallingIdentity();
                try {
                    r.setDisablePreviewScreenshots(disable);
                    WindowManagerService.resetPriorityAfterLockedSection();
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int getLastResumedActivityUserId() {
        this.mAmInternal.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "getLastResumedActivityUserId()");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mLastResumedActivity == null) {
                    int currentUserId = getCurrentUserId();
                    return currentUserId;
                }
                int i = this.mLastResumedActivity.mUserId;
                WindowManagerService.resetPriorityAfterLockedSection();
                return i;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void updateLockTaskFeatures(int userId, int flags) {
        int callingUid = Binder.getCallingUid();
        if (!(callingUid == 0 || callingUid == 1000)) {
            this.mAmInternal.enforceCallingPermission("android.permission.UPDATE_LOCK_TASK_PACKAGES", "updateLockTaskFeatures()");
        }
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
                    String str = TAG_LOCKTASK;
                    Slog.w(str, "Allowing features " + userId + ":0x" + Integer.toHexString(flags));
                }
                getLockTaskController().updateLockTaskFeatures(userId, flags);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setShowWhenLocked(IBinder token, boolean showWhenLocked) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    long origId = Binder.clearCallingIdentity();
                    try {
                        r.setShowWhenLocked(showWhenLocked);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } finally {
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setInheritShowWhenLocked(IBinder token, boolean inheritShowWhenLocked) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    long origId = Binder.clearCallingIdentity();
                    try {
                        r.setInheritShowWhenLocked(inheritShowWhenLocked);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } finally {
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setTurnScreenOn(IBinder token, boolean turnScreenOn) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    long origId = Binder.clearCallingIdentity();
                    try {
                        r.setTurnScreenOn(turnScreenOn);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } finally {
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerRemoteAnimations(IBinder token, RemoteAnimationDefinition definition) {
        this.mAmInternal.enforceCallingPermission("android.permission.CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS", "registerRemoteAnimations");
        definition.setCallingPid(Binder.getCallingPid());
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    long origId = Binder.clearCallingIdentity();
                    try {
                        r.registerRemoteAnimations(definition);
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } finally {
                        Binder.restoreCallingIdentity(origId);
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerRemoteAnimationForNextActivityStart(String packageName, RemoteAnimationAdapter adapter) {
        this.mAmInternal.enforceCallingPermission("android.permission.CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS", "registerRemoteAnimationForNextActivityStart");
        adapter.setCallingPid(Binder.getCallingPid());
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    getActivityStartController().registerRemoteAnimationForNextActivityStart(packageName, adapter);
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerRemoteAnimationsForDisplay(int displayId, RemoteAnimationDefinition definition) {
        this.mAmInternal.enforceCallingPermission("android.permission.CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS", "registerRemoteAnimations");
        definition.setCallingPid(Binder.getCallingPid());
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ActivityDisplay display = this.mRootActivityContainer.getActivityDisplay(displayId);
                if (display == null) {
                    Slog.e(TAG, "Couldn't find display with id: " + displayId);
                    return;
                }
                long origId = Binder.clearCallingIdentity();
                try {
                    display.mDisplayContent.registerRemoteAnimations(definition);
                    WindowManagerService.resetPriorityAfterLockedSection();
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void alwaysShowUnsupportedCompileSdkWarning(ComponentName activity) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    this.mAppWarnings.alwaysShowUnsupportedCompileSdkWarning(activity);
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setVrThread(int tid) {
        enforceSystemHasVrFeature();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                int pid = Binder.getCallingPid();
                this.mVrController.setVrThreadLocked(tid, pid, this.mProcessMap.getProcess(pid));
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setPersistentVrThread(int tid) {
        if (checkCallingPermission("android.permission.RESTRICTED_VR_ACCESS") == 0) {
            enforceSystemHasVrFeature();
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    int pid = Binder.getCallingPid();
                    this.mVrController.setPersistentVrThreadLocked(tid, pid, this.mProcessMap.getProcess(pid));
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        String msg = "Permission Denial: setPersistentVrThread() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.RESTRICTED_VR_ACCESS";
        Slog.w(TAG, msg);
        throw new SecurityException(msg);
    }

    public void stopAppSwitches() {
        ActivityRecord top = this.mRootActivityContainer.getTopResumedActivity();
        if (top == null || !ActivityThread.inCptWhiteList((int) CompatibilityHelper.FLOATING_WIN_START, top.packageName)) {
            enforceCallerIsRecentsOrHasPermission("android.permission.STOP_APP_SWITCHES", "stopAppSwitches");
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    this.mAppSwitchesAllowedTime = SystemClock.uptimeMillis() + 5000;
                    this.mLastStopAppSwitchesTime = SystemClock.uptimeMillis();
                    this.mDidAppSwitch = false;
                    getActivityStartController().schedulePendingActivityLaunches(5000);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        Slog.i(TAG, "current app float win can start the activity immediately,packagename:" + top.packageName);
    }

    public void resumeAppSwitches() {
        enforceCallerIsRecentsOrHasPermission("android.permission.STOP_APP_SWITCHES", "resumeAppSwitches");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mAppSwitchesAllowedTime = 0;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public long getLastStopAppSwitchesTime() {
        return this.mLastStopAppSwitchesTime;
    }

    /* access modifiers changed from: package-private */
    public void onStartActivitySetDidAppSwitch() {
        if (this.mDidAppSwitch) {
            this.mAppSwitchesAllowedTime = 0;
        } else {
            this.mDidAppSwitch = true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shouldDisableNonVrUiLocked() {
        return this.mVrController.shouldDisableNonVrUiLocked();
    }

    private void applyUpdateVrModeLocked(ActivityRecord r) {
        if (!(r.requestedVrComponent == null || r.getDisplayId() == 0)) {
            Slog.i(TAG, "Moving " + r.shortComponentName + " from display " + r.getDisplayId() + " to main display for VR");
            this.mRootActivityContainer.moveStackToDisplay(r.getStackId(), 0, true);
        }
        this.mH.post(new Runnable(r) {
            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$7Ia1bmRpPHHSNlbH8cuLw8dKG04 */
            private final /* synthetic */ ActivityRecord f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ActivityTaskManagerService.this.lambda$applyUpdateVrModeLocked$5$ActivityTaskManagerService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$applyUpdateVrModeLocked$5$ActivityTaskManagerService(ActivityRecord r) {
        if (this.mVrController.onVrModeChanged(r)) {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    boolean disableNonVrUi = this.mVrController.shouldDisableNonVrUiLocked();
                    this.mWindowManager.disableNonVrUi(disableNonVrUi);
                    if (disableNonVrUi) {
                        this.mRootActivityContainer.removeStacksInWindowingModes(2);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    public int getPackageScreenCompatMode(String packageName) {
        int packageScreenCompatModeLocked;
        enforceNotIsolatedCaller("getPackageScreenCompatMode");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                packageScreenCompatModeLocked = this.mCompatModePackages.getPackageScreenCompatModeLocked(packageName);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return packageScreenCompatModeLocked;
    }

    public void setPackageScreenCompatMode(String packageName, int mode) {
        this.mAmInternal.enforceCallingPermission("android.permission.SET_SCREEN_COMPATIBILITY", "setPackageScreenCompatMode");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mCompatModePackages.setPackageScreenCompatModeLocked(packageName, mode);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean getPackageAskScreenCompat(String packageName) {
        boolean packageAskCompatModeLocked;
        enforceNotIsolatedCaller("getPackageAskScreenCompat");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                packageAskCompatModeLocked = this.mCompatModePackages.getPackageAskCompatModeLocked(packageName);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return packageAskCompatModeLocked;
    }

    public void setPackageAskScreenCompat(String packageName, boolean ask) {
        this.mAmInternal.enforceCallingPermission("android.permission.SET_SCREEN_COMPATIBILITY", "setPackageAskScreenCompat");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mCompatModePackages.setPackageAskCompatModeLocked(packageName, ask);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public static String relaunchReasonToString(int relaunchReason) {
        if (relaunchReason == 1) {
            return "window_resize";
        }
        if (relaunchReason != 2) {
            return null;
        }
        return "free_resize";
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getTopDisplayFocusedStack() {
        return this.mRootActivityContainer.getTopDisplayFocusedStack();
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskPersisterLocked(TaskRecord task, boolean flush) {
        this.mRecentTasks.notifyTaskPersisterLocked(task, flush);
    }

    /* access modifiers changed from: package-private */
    public boolean isKeyguardLocked() {
        return this.mKeyguardController.isKeyguardLocked();
    }

    public void clearLaunchParamsForPackages(List<String> packageNames) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "clearLaunchParamsForPackages");
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                for (int i = 0; i < packageNames.size(); i++) {
                    this.mStackSupervisor.mLaunchParamsPersister.removeRecordForPackage(packageNames.get(i));
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setDisplayToSingleTaskInstance(int displayId) {
        this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setDisplayToSingleTaskInstance");
        long origId = Binder.clearCallingIdentity();
        try {
            ActivityDisplay display = this.mRootActivityContainer.getActivityDisplayOrCreate(displayId);
            if (display != null) {
                display.setDisplayToSingleTaskInstance();
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpLastANRLocked(PrintWriter pw) {
        pw.println("ACTIVITY MANAGER LAST ANR (dumpsys activity lastanr)");
        String str = this.mLastANRState;
        if (str == null) {
            pw.println("  <no ANR has occurred since boot>");
        } else {
            pw.println(str);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005f, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0064, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0065, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0068, code lost:
        throw r4;
     */
    public void dumpLastANRTracesLocked(PrintWriter pw) {
        pw.println("ACTIVITY MANAGER LAST ANR TRACES (dumpsys activity lastanr-traces)");
        File[] files = new File(ActivityManagerService.ANR_TRACE_DIR).listFiles();
        if (ArrayUtils.isEmpty(files)) {
            pw.println("  <no ANR has occurred since boot>");
            return;
        }
        File latest = null;
        for (File f : files) {
            if (latest == null || latest.lastModified() < f.lastModified()) {
                latest = f;
            }
        }
        pw.print("File: ");
        pw.print(latest.getName());
        pw.println();
        try {
            BufferedReader in = new BufferedReader(new FileReader(latest));
            while (true) {
                String line = in.readLine();
                if (line != null) {
                    pw.println(line);
                } else {
                    in.close();
                    return;
                }
            }
        } catch (IOException e) {
            pw.print("Unable to read: ");
            pw.print(e);
            pw.println();
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, boolean dumpClient, String dumpPackage) {
        dumpActivitiesLocked(fd, pw, args, opti, dumpAll, dumpClient, dumpPackage, "ACTIVITY MANAGER ACTIVITIES (dumpsys activity activities)");
    }

    /* access modifiers changed from: package-private */
    public void dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, boolean dumpClient, String dumpPackage, String header) {
        pw.println(header);
        boolean printedAnything = this.mRootActivityContainer.dumpActivities(fd, pw, dumpAll, dumpClient, dumpPackage);
        boolean needSep = printedAnything;
        if (ActivityStackSupervisor.printThisActivity(pw, this.mRootActivityContainer.getTopResumedActivity(), dumpPackage, needSep, "  ResumedActivity: ")) {
            printedAnything = true;
            needSep = false;
        }
        if (dumpPackage == null) {
            if (needSep) {
                pw.println();
            }
            printedAnything = true;
            this.mStackSupervisor.dump(pw, "  ");
        }
        if (!printedAnything) {
            pw.println("  (nothing)");
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpActivityContainersLocked(PrintWriter pw) {
        pw.println("ACTIVITY MANAGER STARTER (dumpsys activity containers)");
        this.mRootActivityContainer.dumpChildrenNames(pw, StringUtils.SPACE);
        pw.println(StringUtils.SPACE);
    }

    /* access modifiers changed from: package-private */
    public void dumpActivityStarterLocked(PrintWriter pw, String dumpPackage) {
        pw.println("ACTIVITY MANAGER STARTER (dumpsys activity starter)");
        getActivityStartController().dump(pw, "", dumpPackage);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0026, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0027, code lost:
        r7 = new java.lang.String[(r27.length - r28)];
        java.lang.System.arraycopy(r27, r28, r7, 0, r27.length - r28);
        r6 = r0.size() - 1;
        r1 = null;
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0040, code lost:
        if (r6 < 0) goto L_0x00c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0042, code lost:
        r17 = r0.get(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004a, code lost:
        if (r0 == null) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004c, code lost:
        r25.println();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004f, code lost:
        r2 = r23.mGlobalLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0053, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        com.android.server.wm.WindowManagerService.boostPriorityForLockedSection();
        r0 = r17.getTaskRecord();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005b, code lost:
        if (r1 == r0) goto L_0x008c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r25.print("TASK ");
        r25.print(r0.affinity);
        r25.print(" id=");
        r25.print(r0.taskId);
        r25.print(" userId=");
        r25.println(r0.userId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007c, code lost:
        if (r29 == false) goto L_0x0083;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007e, code lost:
        r0.dump(r25, "  ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0083, code lost:
        r19 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0086, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x008c, code lost:
        r19 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008f, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        dumpActivity("  ", r24, r25, r0.get(r6), r7, r29);
        r6 = r6 - 1;
        r0 = 1;
        r1 = r19;
        r7 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00b4, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00bc, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00c2, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c5, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00c6, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c8, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001c, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0024, code lost:
        if (r0.size() > 0) goto L_0x0027;
     */
    public boolean dumpActivity(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll, boolean dumpVisibleStacksOnly, boolean dumpFocusedStackOnly) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                try {
                    ArrayList<ActivityRecord> activities = this.mRootActivityContainer.getDumpActivities(name, dumpVisibleStacksOnly, dumpFocusedStackOnly);
                } catch (Throwable th) {
                    th = th;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private void dumpActivity(String prefix, FileDescriptor fd, PrintWriter pw, ActivityRecord r, String[] args, boolean dumpAll) {
        String innerPrefix = prefix + "  ";
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                pw.print(prefix);
                pw.print("ACTIVITY ");
                pw.print(r.shortComponentName);
                pw.print(StringUtils.SPACE);
                pw.print(Integer.toHexString(System.identityHashCode(r)));
                pw.print(" pid=");
                if (r.hasProcess()) {
                    pw.println(r.app.getPid());
                } else {
                    pw.println("(not running)");
                }
                if (dumpAll) {
                    r.dump(pw, innerPrefix);
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        if (r.attachedToProcess()) {
            pw.flush();
            try {
                TransferPipe tp = new TransferPipe();
                try {
                    r.app.getThread().dumpActivity(tp.getWriteFd(), r.appToken, innerPrefix, args);
                    tp.go(fd);
                } finally {
                    tp.kill();
                }
            } catch (IOException e) {
                pw.println(innerPrefix + "Failure while dumping the activity: " + e);
            } catch (RemoteException e2) {
                pw.println(innerPrefix + "Got a RemoteException while dumping the activity");
            }
        }
    }

    /* access modifiers changed from: private */
    public void writeSleepStateToProto(ProtoOutputStream proto, int wakeFullness, boolean testPssMode) {
        long sleepToken = proto.start(1146756268059L);
        proto.write(1159641169921L, PowerManagerInternal.wakefulnessToProtoEnum(wakeFullness));
        Iterator<ActivityTaskManagerInternal.SleepToken> it = this.mRootActivityContainer.mSleepTokens.iterator();
        while (it.hasNext()) {
            proto.write(2237677961218L, it.next().toString());
        }
        proto.write(1133871366147L, this.mSleeping);
        proto.write(1133871366148L, this.mShuttingDown);
        proto.write(1133871366149L, testPssMode);
        proto.end(sleepToken);
    }

    /* access modifiers changed from: package-private */
    public int getCurrentUserId() {
        return this.mAmInternal.getCurrentUserId();
    }

    /* access modifiers changed from: private */
    public void enforceNotIsolatedCaller(String caller) {
        if (UserHandle.isIsolated(Binder.getCallingUid())) {
            throw new SecurityException("Isolated process not allowed to call " + caller);
        }
    }

    public Configuration getConfiguration() {
        Configuration ci;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ci = new Configuration(getGlobalConfigurationForCallingPid());
                ci.userSetLocale = false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return ci;
    }

    /* access modifiers changed from: package-private */
    public Configuration getGlobalConfiguration() {
        return this.mRootActivityContainer.getConfiguration();
    }

    /* access modifiers changed from: package-private */
    public boolean updateConfigurationLocked(Configuration values, ActivityRecord starting, boolean initLocale) {
        return updateConfigurationLocked(values, starting, initLocale, false);
    }

    /* access modifiers changed from: package-private */
    public boolean updateConfigurationLocked(Configuration values, ActivityRecord starting, boolean initLocale, boolean deferResume) {
        return updateConfigurationLocked(values, starting, initLocale, false, -10000, deferResume);
    }

    /* JADX INFO: finally extract failed */
    public void updatePersistentConfiguration(Configuration values, int userId) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    updateConfigurationLocked(values, null, false, true, userId, false);
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* access modifiers changed from: private */
    public boolean updateConfigurationLocked(Configuration values, ActivityRecord starting, boolean initLocale, boolean persistent, int userId, boolean deferResume) {
        return updateConfigurationLocked(values, starting, initLocale, persistent, userId, deferResume, null);
    }

    /* access modifiers changed from: package-private */
    public boolean updateConfigurationLocked(Configuration values, ActivityRecord starting, boolean initLocale, boolean persistent, int userId, boolean deferResume, UpdateConfigurationResult result) {
        Log.d("ATMS", "updateConfigurationLocked : updateUserIdInExtraCOnfiguration userid = " + userId);
        updateUserIdInExtraConfiguration(values, userId);
        updateBurmeseFontLinkForUser(values, userId, this.mContext);
        int changes = 0;
        WindowManagerService windowManagerService = this.mWindowManager;
        if (windowManagerService != null) {
            windowManagerService.deferSurfaceLayout();
        }
        if (values != null) {
            try {
                changes = updateGlobalConfigurationLocked(values, initLocale, persistent, userId, deferResume);
            } catch (Throwable th) {
                WindowManagerService windowManagerService2 = this.mWindowManager;
                if (windowManagerService2 != null) {
                    windowManagerService2.continueSurfaceLayout();
                }
                throw th;
            }
        }
        boolean kept = ensureConfigAndVisibilityAfterUpdate(starting, changes);
        WindowManagerService windowManagerService3 = this.mWindowManager;
        if (windowManagerService3 != null) {
            windowManagerService3.continueSurfaceLayout();
        }
        if (result != null) {
            result.changes = changes;
            result.activityRelaunched = !kept;
        }
        return kept;
    }

    private int updateGlobalConfigurationLocked(Configuration values, boolean initLocale, boolean persistent, int userId, boolean deferResume) {
        this.mTempConfig.setTo(getGlobalConfiguration());
        int changes = this.mTempConfig.updateFrom(values);
        handleUiModeChanged(changes);
        OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).clearCacheWhenOnConfigurationChange(changes);
        if (changes == 0) {
            Configuration configPersistCopy = new Configuration(this.mTempConfig);
            if (persistent) {
                Slog.i(TAG, "event if changes is 0, we still need to persist the locale change " + this.mTempConfig + "caller: " + Debug.getCallers(10));
                this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$yP9TbBmrgQ4lrgcxb8oL1pBAs4.INSTANCE, this, Integer.valueOf(userId), configPersistCopy));
            }
            performDisplayOverrideConfigUpdate(values, deferResume, 0);
            return 0;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
            Slog.i(TAG_CONFIGURATION, "Updating global configuration to: " + values);
        }
        EventLog.writeEvent((int) EventLogTags.CONFIGURATION_CHANGED, changes);
        StatsLog.write(66, values.colorMode, values.densityDpi, values.fontScale, values.hardKeyboardHidden, values.keyboard, values.keyboardHidden, values.mcc, values.mnc, values.navigation, values.navigationHidden, values.orientation, values.screenHeightDp, values.screenLayout, values.screenWidthDp, values.smallestScreenWidthDp, values.touchscreen, values.uiMode);
        if (!initLocale && !values.getLocales().isEmpty() && values.userSetLocale) {
            LocaleList locales = values.getLocales();
            int bestLocaleIndex = 0;
            if (locales.size() > 1) {
                if (this.mSupportedSystemLocales == null) {
                    this.mSupportedSystemLocales = Resources.getSystem().getAssets().getLocales();
                }
                bestLocaleIndex = Math.max(0, locales.getFirstMatchIndex(this.mSupportedSystemLocales));
            }
            SystemProperties.set("persist.sys.locale", locales.get(bestLocaleIndex).toLanguageTag());
            LocaleList.setDefault(locales, bestLocaleIndex);
            this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$U6g1UdnOPnEF9wX1OTm9nKVXY5k.INSTANCE, this, locales.get(bestLocaleIndex)));
        }
        this.mTempConfig.seq = increaseConfigurationSeqLocked();
        this.mRootActivityContainer.onConfigurationChanged(this.mTempConfig);
        Slog.i(TAG, "Config changes=" + Integer.toHexString(changes) + StringUtils.SPACE + this.mTempConfig);
        this.mUsageStatsInternal.reportConfigurationChange(this.mTempConfig, this.mAmInternal.getCurrentUserId());
        updateShouldShowDialogsLocked(this.mTempConfig);
        AttributeCache ac = AttributeCache.instance();
        if (ac != null) {
            ac.updateConfiguration(this.mTempConfig);
        }
        this.mSystemThread.applyConfigurationToResources(this.mTempConfig);
        Configuration configCopy = new Configuration(this.mTempConfig);
        if (persistent && Settings.System.hasInterestingConfigurationChanges(changes)) {
            this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$yP9TbBmrgQ4lrgcxb8oL1pBAs4.INSTANCE, this, Integer.valueOf(userId), configCopy));
        }
        getOppoPackageManagerInternalLocked().clearIconCache();
        SparseArray<WindowProcessController> pidMap = this.mProcessMap.getPidMap();
        for (int i = pidMap.size() - 1; i >= 0; i--) {
            WindowProcessController app = pidMap.get(pidMap.keyAt(i));
            if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG_CONFIGURATION, "Update process config of " + app.mName + " to new config " + configCopy);
            }
            if (!shouldIgnore(changes, app.mName)) {
                app.onConfigurationChanged(configCopy);
            }
        }
        handleExtraConfigurationChanges(changes, configCopy, this.mContext, this.mUiHandler, getCurrentUserId());
        this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$swA_sUfSJdP8eC8AA9Iby3SuOY.INSTANCE, this.mAmInternal, Integer.valueOf(changes), Boolean.valueOf(initLocale)));
        performDisplayOverrideConfigUpdate(this.mRootActivityContainer.getConfiguration(), deferResume, 0);
        return changes;
    }

    /* access modifiers changed from: package-private */
    public boolean updateDisplayOverrideConfigurationLocked(Configuration values, ActivityRecord starting, boolean deferResume, int displayId) {
        return updateDisplayOverrideConfigurationLocked(values, starting, deferResume, displayId, null);
    }

    /* access modifiers changed from: package-private */
    public boolean updateDisplayOverrideConfigurationLocked(Configuration values, ActivityRecord starting, boolean deferResume, int displayId, UpdateConfigurationResult result) {
        int changes = 0;
        WindowManagerService windowManagerService = this.mWindowManager;
        if (windowManagerService != null) {
            windowManagerService.deferSurfaceLayout();
        }
        if (values != null) {
            if (displayId == 0) {
                try {
                    changes = updateGlobalConfigurationLocked(values, false, false, -10000, deferResume);
                } catch (Throwable th) {
                    WindowManagerService windowManagerService2 = this.mWindowManager;
                    if (windowManagerService2 != null) {
                        windowManagerService2.continueSurfaceLayout();
                    }
                    throw th;
                }
            } else {
                changes = performDisplayOverrideConfigUpdate(values, deferResume, displayId);
            }
        }
        boolean kept = ensureConfigAndVisibilityAfterUpdate(starting, changes);
        WindowManagerService windowManagerService3 = this.mWindowManager;
        if (windowManagerService3 != null) {
            windowManagerService3.continueSurfaceLayout();
        }
        if (result != null) {
            result.changes = changes;
            result.activityRelaunched = !kept;
        }
        return kept;
    }

    private int performDisplayOverrideConfigUpdate(Configuration values, boolean deferResume, int displayId) {
        this.mTempConfig.setTo(this.mRootActivityContainer.getDisplayOverrideConfiguration(displayId));
        int changes = this.mTempConfig.updateFrom(values);
        if (changes != 0) {
            Slog.i(TAG, "Override config changes=" + Integer.toHexString(changes) + StringUtils.SPACE + this.mTempConfig + " for displayId=" + displayId);
            this.mRootActivityContainer.setDisplayOverrideConfiguration(this.mTempConfig, displayId);
            if (((changes & 4096) != 0) && displayId == 0) {
                this.mAppWarnings.onDensityChanged();
                this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ibmQVLjaQW2x74Wk8TcE0Og2MJM.INSTANCE, this.mAmInternal, 24, 7));
            }
        }
        return changes;
    }

    /* access modifiers changed from: private */
    public void updateEventDispatchingLocked(boolean booted) {
        this.mWindowManager.setEventDispatching(booted && !this.mShuttingDown);
    }

    /* access modifiers changed from: private */
    public void sendPutConfigurationForUserMsg(int userId, Configuration config) {
        Settings.System.putConfigurationForUser(this.mContext.getContentResolver(), config, userId);
    }

    /* access modifiers changed from: private */
    public void sendLocaleToMountDaemonMsg(Locale l) {
        try {
            IStorageManager storageManager = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
            Log.d(TAG, "Storing locale " + l.toLanguageTag() + " for decryption UI");
            storageManager.setField("SystemLocale", l.toLanguageTag());
        } catch (RemoteException e) {
            Log.e(TAG, "Error storing locale for decryption UI", e);
        }
    }

    /* access modifiers changed from: private */
    public void expireStartAsCallerTokenMsg(IBinder permissionToken) {
        this.mStartActivitySources.remove(permissionToken);
        this.mExpiredStartAsCallerTokens.add(permissionToken);
    }

    /* access modifiers changed from: private */
    public void forgetStartAsCallerTokenMsg(IBinder permissionToken) {
        this.mExpiredStartAsCallerTokens.remove(permissionToken);
    }

    /* access modifiers changed from: package-private */
    public boolean isActivityStartsLoggingEnabled() {
        return this.mAmInternal.isActivityStartsLoggingEnabled();
    }

    /* access modifiers changed from: package-private */
    public boolean isBackgroundActivityStartsEnabled() {
        return this.mAmInternal.isBackgroundActivityStartsEnabled();
    }

    /* access modifiers changed from: package-private */
    public void enableScreenAfterBoot(boolean booted) {
        EventLog.writeEvent((int) EventLogTags.BOOT_PROGRESS_ENABLE_SCREEN, SystemClock.uptimeMillis());
        this.mWindowManager.enableScreenAfterBoot();
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                updateEventDispatchingLocked(booted);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    static long getInputDispatchingTimeoutLocked(ActivityRecord r) {
        if (r == null || !r.hasProcess()) {
            return 5000;
        }
        return getInputDispatchingTimeoutLocked(r.app);
    }

    private static long getInputDispatchingTimeoutLocked(WindowProcessController r) {
        if (r != null) {
            return r.getInputDispatchingTimeout();
        }
        return 5000;
    }

    /* access modifiers changed from: private */
    public void updateShouldShowDialogsLocked(Configuration config) {
        boolean z = false;
        boolean inputMethodExists = (config.keyboard == 1 && config.touchscreen == 1 && config.navigation == 1) ? false : true;
        int modeType = config.uiMode & 15;
        boolean uiModeSupportsDialogs = (modeType == 3 || (modeType == 6 && Build.IS_USER) || modeType == 4 || modeType == 7) ? false : true;
        boolean hideDialogsSet = Settings.Global.getInt(this.mContext.getContentResolver(), "hide_error_dialogs", 0) != 0;
        if (inputMethodExists && uiModeSupportsDialogs && !hideDialogsSet) {
            z = true;
        }
        this.mShowDialogs = z;
    }

    /* access modifiers changed from: private */
    public void updateFontScaleIfNeeded(int userId) {
        float scaleFactor = Settings.System.getFloatForUser(this.mContext.getContentResolver(), "font_scale", 1.0f, userId);
        synchronized (this) {
            if (getGlobalConfiguration().fontScale != scaleFactor) {
                Configuration configuration = this.mWindowManager.computeNewConfiguration(0);
                configuration.fontScale = scaleFactor;
                updatePersistentConfiguration(configuration, userId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSleepingOrShuttingDownLocked() {
        return isSleepingLocked() || this.mShuttingDown;
    }

    /* access modifiers changed from: package-private */
    public boolean isSleepingLocked() {
        return this.mSleeping;
    }

    /* access modifiers changed from: package-private */
    public void setResumedActivityUncheckLocked(ActivityRecord r, String reason) {
        IVoiceInteractionSession session;
        if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS) {
            String str = TAG_FOCUS;
            Slog.d(str, "mLastResumedActivity:" + this.mLastResumedActivity + " r " + r + " reason " + reason);
        }
        TaskRecord task = r.getTaskRecord();
        if (!task.isActivityTypeStandard()) {
            r.appTimeTracker = null;
        } else if (this.mCurAppTimeTracker != r.appTimeTracker) {
            AppTimeTracker appTimeTracker = this.mCurAppTimeTracker;
            if (appTimeTracker != null) {
                appTimeTracker.stop();
                this.mH.obtainMessage(1, this.mCurAppTimeTracker).sendToTarget();
                this.mRootActivityContainer.clearOtherAppTimeTrackers(r.appTimeTracker);
                this.mCurAppTimeTracker = null;
            }
            if (r.appTimeTracker != null) {
                this.mCurAppTimeTracker = r.appTimeTracker;
                startTimeTrackingFocusedActivityLocked();
            }
        } else {
            startTimeTrackingFocusedActivityLocked();
        }
        if (task.voiceInteractor != null) {
            startRunningVoiceLocked(task.voiceSession, r.info.applicationInfo.uid);
        } else {
            finishRunningVoiceLocked();
            ActivityRecord activityRecord = this.mLastResumedActivity;
            if (activityRecord != null) {
                TaskRecord lastResumedActivityTask = activityRecord.getTaskRecord();
                if (lastResumedActivityTask == null || lastResumedActivityTask.voiceSession == null) {
                    session = ((ActivityRecord) this.mLastResumedActivity).voiceSession;
                } else {
                    session = lastResumedActivityTask.voiceSession;
                }
                if (session != null) {
                    finishVoiceTask(session);
                }
            }
        }
        if (!(this.mLastResumedActivity == null || r.mUserId == this.mLastResumedActivity.mUserId)) {
            this.mAmInternal.sendForegroundProfileChanged(r.mUserId);
        }
        updateResumedAppTrace(r);
        this.mLastResumedActivity = r;
        r.getDisplay().setFocusedApp(r, true);
        applyUpdateLockStateLocked(r);
        applyUpdateVrModeLocked(r);
        EventLogTags.writeAmSetResumedActivity(r.mUserId, r.shortComponentName, reason);
    }

    /* access modifiers changed from: package-private */
    public ActivityTaskManagerInternal.SleepToken acquireSleepToken(String tag, int displayId) {
        ActivityTaskManagerInternal.SleepToken token;
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                token = this.mRootActivityContainer.createSleepToken(tag, displayId);
                updateSleepIfNeededLocked();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return token;
    }

    /* access modifiers changed from: package-private */
    public void updateSleepIfNeededLocked() {
        boolean shouldSleep = !this.mRootActivityContainer.hasAwakeDisplay();
        boolean wasSleeping = this.mSleeping;
        boolean updateOomAdj = false;
        if (!shouldSleep) {
            if (wasSleeping) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.d(TAG, "updateSleepIfNeededLocked: comeOutOfSleep");
                }
                this.mSleeping = false;
                StatsLog.write(14, 2);
                startTimeTrackingFocusedActivityLocked();
                this.mTopProcessState = 2;
                Slog.d(TAG, "Top Process State changed to PROCESS_STATE_TOP");
                this.mStackSupervisor.comeOutOfSleepIfNeededLocked();
            }
            this.mRootActivityContainer.applySleepTokens(true);
            if (wasSleeping) {
                updateOomAdj = true;
            }
        } else if (!this.mSleeping && shouldSleep) {
            OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).checkGoToSleep(null, getCurrentUserId());
            if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                Slog.d(TAG, "updateSleepIfNeededLocked: goingToSleep");
            }
            this.mSleeping = true;
            StatsLog.write(14, 1);
            AppTimeTracker appTimeTracker = this.mCurAppTimeTracker;
            if (appTimeTracker != null) {
                appTimeTracker.stop();
            }
            this.mTopProcessState = 13;
            Slog.d(TAG, "Top Process State changed to PROCESS_STATE_TOP_SLEEPING");
            this.mStackSupervisor.goingToSleepLocked();
            updateResumedAppTrace(null);
            updateOomAdj = true;
        }
        if (updateOomAdj) {
            H h = this.mH;
            ActivityManagerInternal activityManagerInternal = this.mAmInternal;
            Objects.requireNonNull(activityManagerInternal);
            h.post(new Runnable(activityManagerInternal) {
                /* class com.android.server.wm.$$Lambda$yIIsPVyXvnU3Rv8mcliitgIpSs */
                private final /* synthetic */ ActivityManagerInternal f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    this.f$0.updateOomAdj();
                }
            });
        }
        this.mAmsExt.onUpdateSleep(wasSleeping, this.mSleeping);
    }

    /* access modifiers changed from: package-private */
    public void updateOomAdj() {
        H h = this.mH;
        ActivityManagerInternal activityManagerInternal = this.mAmInternal;
        Objects.requireNonNull(activityManagerInternal);
        h.post(new Runnable(activityManagerInternal) {
            /* class com.android.server.wm.$$Lambda$yIIsPVyXvnU3Rv8mcliitgIpSs */
            private final /* synthetic */ ActivityManagerInternal f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                this.f$0.updateOomAdj();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void updateCpuStats() {
        H h = this.mH;
        ActivityManagerInternal activityManagerInternal = this.mAmInternal;
        Objects.requireNonNull(activityManagerInternal);
        h.post(new Runnable(activityManagerInternal) {
            /* class com.android.server.wm.$$Lambda$LYW1ECaEajjYgarzgKdTZ4O1fi0 */
            private final /* synthetic */ ActivityManagerInternal f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                this.f$0.updateCpuStats();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void updateBatteryStats(ActivityRecord component, boolean resumed) {
        this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$hT1kyMEAhvB1Uxr0DFAlnuU3cQ.INSTANCE, this.mAmInternal, component.mActivityComponent, Integer.valueOf(component.app.mUid), Integer.valueOf(component.mUserId), Boolean.valueOf(resumed)));
    }

    /* access modifiers changed from: package-private */
    public void updateActivityUsageStats(ActivityRecord activity, int event) {
        ActivityRecord rootActivity;
        ComponentName taskRoot = null;
        TaskRecord task = activity.getTaskRecord();
        if (!(task == null || (rootActivity = task.getRootActivity()) == null)) {
            taskRoot = rootActivity.mActivityComponent;
        }
        this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$UB90fpYUkajpKCLGR93ZDlgDhyw.INSTANCE, this.mAmInternal, activity.mActivityComponent, Integer.valueOf(activity.mUserId), Integer.valueOf(event), activity.appToken, taskRoot));
    }

    /* access modifiers changed from: package-private */
    public void setBooting(boolean booting) {
        this.mAmInternal.setBooting(booting);
    }

    /* access modifiers changed from: package-private */
    public boolean isBooting() {
        return this.mAmInternal.isBooting();
    }

    /* access modifiers changed from: package-private */
    public void setBooted(boolean booted) {
        this.mAmInternal.setBooted(booted);
    }

    /* access modifiers changed from: package-private */
    public boolean isBooted() {
        return this.mAmInternal.isBooted();
    }

    /* access modifiers changed from: package-private */
    public void postFinishBooting(boolean finishBooting, boolean enableScreen) {
        this.mH.post(new Runnable(finishBooting, enableScreen) {
            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$oP6xxIfnD4kb4JN7aSJU073ULR4 */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ActivityTaskManagerService.this.lambda$postFinishBooting$6$ActivityTaskManagerService(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$postFinishBooting$6$ActivityTaskManagerService(boolean finishBooting, boolean enableScreen) {
        if (finishBooting) {
            this.mAmInternal.finishBooting();
        }
        if (enableScreen) {
            this.mInternal.enableScreenAfterBoot(isBooted());
        }
    }

    /* access modifiers changed from: package-private */
    public void setHeavyWeightProcess(ActivityRecord root) {
        this.mHeavyWeightProcess = root.app;
        this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$x3j1aVkumtfulORwKd6dHysJyE0.INSTANCE, this, root.app, root.intent, Integer.valueOf(root.mUserId)));
    }

    /* access modifiers changed from: package-private */
    public void clearHeavyWeightProcessIfEquals(WindowProcessController proc) {
        WindowProcessController windowProcessController = this.mHeavyWeightProcess;
        if (windowProcessController != null && windowProcessController == proc) {
            this.mHeavyWeightProcess = null;
            this.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$ActivityTaskManagerService$w70cT1_hTWQQAYctmXaA0BeZuBc.INSTANCE, this, Integer.valueOf(proc.mUserId)));
        }
    }

    /* access modifiers changed from: private */
    public void cancelHeavyWeightProcessNotification(int userId) {
        INotificationManager inm = NotificationManager.getService();
        if (inm != null) {
            try {
                inm.cancelNotificationWithTag(PackageManagerService.PLATFORM_PACKAGE_NAME, (String) null, 11, userId);
            } catch (RuntimeException e) {
                Slog.w(TAG, "Error canceling notification for service", e);
            } catch (RemoteException e2) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void postHeavyWeightProcessNotification(WindowProcessController proc, Intent intent, int userId) {
        INotificationManager inm;
        if (proc != null && (inm = NotificationManager.getService()) != null) {
            try {
                Context context = this.mContext.createPackageContext(proc.mInfo.packageName, 0);
                String text = this.mContext.getString(17040085, context.getApplicationInfo().loadLabel(context.getPackageManager()));
                try {
                    inm.enqueueNotificationWithTag(PackageManagerService.PLATFORM_PACKAGE_NAME, PackageManagerService.PLATFORM_PACKAGE_NAME, (String) null, 11, new Notification.Builder(context, SystemNotificationChannels.HEAVY_WEIGHT_APP).setSmallIcon(17303520).setWhen(0).setOngoing(true).setTicker(text).setColor(this.mContext.getColor(17170460)).setContentTitle(text).setContentText(this.mContext.getText(17040086)).setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent, 268435456, null, new UserHandle(userId))).build(), userId);
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Error showing notification for heavy-weight app", e);
                } catch (RemoteException e2) {
                }
            } catch (PackageManager.NameNotFoundException e3) {
                Slog.w(TAG, "Unable to create context for heavy notification", e3);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public IIntentSender getIntentSenderLocked(int type, String packageName, int callingUid, int userId, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle bOptions) {
        ActivityRecord activity;
        if (type == 3) {
            ActivityRecord activity2 = ActivityRecord.isInStackLocked(token);
            if (activity2 == null) {
                Slog.w(TAG, "Failed createPendingResult: activity " + token + " not in any stack");
                return null;
            } else if (activity2.finishing) {
                Slog.w(TAG, "Failed createPendingResult: activity " + activity2 + " is finishing");
                return null;
            } else {
                activity = activity2;
            }
        } else {
            activity = null;
        }
        PendingIntentRecord rec = this.mPendingIntentController.getIntentSender(type, packageName, callingUid, userId, token, resultWho, requestCode, intents, resolvedTypes, flags, bOptions);
        if (!((flags & 536870912) != 0) && type == 3) {
            if (activity.pendingResults == null) {
                activity.pendingResults = new HashSet<>();
            }
            activity.pendingResults.add(rec.ref);
        }
        return rec;
    }

    private void startTimeTrackingFocusedActivityLocked() {
        AppTimeTracker appTimeTracker;
        ActivityRecord resumedActivity = this.mRootActivityContainer.getTopResumedActivity();
        if (!this.mSleeping && (appTimeTracker = this.mCurAppTimeTracker) != null && resumedActivity != null) {
            appTimeTracker.start(resumedActivity.packageName);
        }
    }

    private void updateResumedAppTrace(ActivityRecord resumed) {
        ActivityRecord activityRecord = this.mTracedResumedActivity;
        if (activityRecord != null) {
            Trace.asyncTraceEnd(64, constructResumedTraceName(activityRecord.packageName), 0);
        }
        if (resumed != null) {
            Trace.asyncTraceBegin(64, constructResumedTraceName(resumed.packageName), 0);
        }
        this.mTracedResumedActivity = resumed;
    }

    private String constructResumedTraceName(String packageName) {
        return "focused app: " + packageName;
    }

    private boolean ensureConfigAndVisibilityAfterUpdate(ActivityRecord starting, int changes) {
        ActivityStack mainStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
        if (mainStack == null) {
            return true;
        }
        if (changes != 0 && starting == null) {
            starting = mainStack.topRunningActivityLocked();
        }
        if (starting == null) {
            return true;
        }
        boolean kept = starting.ensureActivityConfiguration(changes, false);
        this.mRootActivityContainer.ensureActivitiesVisible(starting, changes, false);
        return kept;
    }

    public /* synthetic */ void lambda$scheduleAppGcsLocked$7$ActivityTaskManagerService() {
        this.mAmInternal.scheduleAppGcs();
    }

    /* access modifiers changed from: package-private */
    public void scheduleAppGcsLocked() {
        this.mH.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$xFyZDUKMraVkermSJGXQdN3oJ4 */

            public final void run() {
                ActivityTaskManagerService.this.lambda$scheduleAppGcsLocked$7$ActivityTaskManagerService();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public CompatibilityInfo compatibilityInfoForPackageLocked(ApplicationInfo ai) {
        return this.mCompatModePackages.compatibilityInfoForPackageLocked(ai);
    }

    /* access modifiers changed from: package-private */
    public IPackageManager getPackageManager() {
        return AppGlobals.getPackageManager();
    }

    /* access modifiers changed from: package-private */
    public PackageManagerInternal getPackageManagerInternalLocked() {
        if (this.mPmInternal == null) {
            this.mPmInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        }
        return this.mPmInternal;
    }

    /* access modifiers changed from: package-private */
    public PermissionPolicyInternal getPermissionPolicyInternal() {
        if (this.mPermissionPolicyInternal == null) {
            this.mPermissionPolicyInternal = (PermissionPolicyInternal) LocalServices.getService(PermissionPolicyInternal.class);
        }
        return this.mPermissionPolicyInternal;
    }

    /* access modifiers changed from: package-private */
    public AppWarnings getAppWarningsLocked() {
        return this.mAppWarnings;
    }

    /* access modifiers changed from: package-private */
    public Intent getHomeIntent() {
        String str = this.mTopAction;
        String str2 = this.mTopData;
        Intent intent = new Intent(str, str2 != null ? Uri.parse(str2) : null);
        intent.setComponent(this.mTopComponent);
        intent.addFlags(256);
        if (this.mFactoryTest != 1) {
            intent.addCategory("android.intent.category.HOME");
        }
        return intent;
    }

    /* access modifiers changed from: package-private */
    public Intent getSecondaryHomeIntent(String preferredPackage) {
        String str = this.mTopAction;
        String str2 = this.mTopData;
        Intent intent = new Intent(str, str2 != null ? Uri.parse(str2) : null);
        boolean useSystemProvidedLauncher = this.mContext.getResources().getBoolean(17891562);
        if (preferredPackage == null || useSystemProvidedLauncher) {
            intent.setComponent(ComponentName.unflattenFromString(this.mContext.getResources().getString(17039768)));
        } else {
            intent.setPackage(preferredPackage);
        }
        intent.addFlags(256);
        if (this.mFactoryTest != 1) {
            intent.addCategory("android.intent.category.SECONDARY_HOME");
        }
        return intent;
    }

    /* access modifiers changed from: package-private */
    public ApplicationInfo getAppInfoForUser(ApplicationInfo info, int userId) {
        if (info == null) {
            return null;
        }
        ApplicationInfo newInfo = new ApplicationInfo(info);
        newInfo.initForUser(userId);
        return newInfo;
    }

    /* access modifiers changed from: package-private */
    public WindowProcessController getProcessController(String processName, int uid) {
        if (uid == 1000) {
            SparseArray<WindowProcessController> procs = (SparseArray) this.mProcessNames.getMap().get(processName);
            if (procs == null) {
                return null;
            }
            int procCount = procs.size();
            for (int i = 0; i < procCount; i++) {
                int procUid = procs.keyAt(i);
                if (!UserHandle.isApp(procUid) && UserHandle.isSameUser(procUid, uid)) {
                    return procs.valueAt(i);
                }
            }
        }
        return (WindowProcessController) this.mProcessNames.get(processName, uid);
    }

    /* access modifiers changed from: package-private */
    public WindowProcessController getProcessController(IApplicationThread thread) {
        if (thread == null) {
            return null;
        }
        IBinder threadBinder = thread.asBinder();
        ArrayMap<String, SparseArray<WindowProcessController>> pmap = this.mProcessNames.getMap();
        for (int i = pmap.size() - 1; i >= 0; i--) {
            SparseArray<WindowProcessController> procs = pmap.valueAt(i);
            for (int j = procs.size() - 1; j >= 0; j--) {
                WindowProcessController proc = procs.valueAt(j);
                if (proc.hasThread() && proc.getThread().asBinder() == threadBinder) {
                    return proc;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public WindowProcessController getProcessController(int pid, int uid) {
        WindowProcessController proc = this.mProcessMap.getProcess(pid);
        if (proc != null && UserHandle.isApp(uid) && proc.mUid == uid) {
            return proc;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getUidState(int uid) {
        return this.mActiveUids.getUidState(uid);
    }

    /* access modifiers changed from: package-private */
    public boolean isUidForeground(int uid) {
        return this.mWindowManager.mRoot.isAnyNonToastWindowVisibleForUid(uid);
    }

    /* access modifiers changed from: package-private */
    public boolean isDeviceOwner(int uid) {
        return uid >= 0 && this.mDeviceOwnerUid == uid;
    }

    /* access modifiers changed from: package-private */
    public void setDeviceOwnerUid(int uid) {
        this.mDeviceOwnerUid = uid;
    }

    /* access modifiers changed from: package-private */
    public String getPendingTempWhitelistTagForUidLocked(int uid) {
        return this.mPendingTempWhitelist.get(uid);
    }

    /* access modifiers changed from: package-private */
    public void logAppTooSlow(WindowProcessController app, long startTime, String msg) {
    }

    /* access modifiers changed from: package-private */
    public boolean isAssociatedCompanionApp(int userId, int uid) {
        Set<Integer> allUids = this.mCompanionAppUidsMap.get(Integer.valueOf(userId));
        if (allUids == null) {
            return false;
        }
        return allUids.contains(Integer.valueOf(uid));
    }

    final class H extends Handler {
        static final int FIRST_ACTIVITY_STACK_MSG = 100;
        static final int FIRST_SUPERVISOR_STACK_MSG = 200;
        static final int REPORT_TIME_TRACKER_MSG = 1;

        H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                ((AppTimeTracker) msg.obj).deliverResult(ActivityTaskManagerService.this.mContext);
            }
        }
    }

    final class UiHandler extends Handler {
        static final int DISMISS_DIALOG_UI_MSG = 1;

        public UiHandler() {
            super(UiThread.get().getLooper(), null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                ((Dialog) msg.obj).dismiss();
            }
        }
    }

    final class LocalService extends ActivityTaskManagerInternal {
        LocalService() {
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ActivityTaskManagerInternal.SleepToken acquireSleepToken(String tag, int displayId) {
            Preconditions.checkNotNull(tag);
            return ActivityTaskManagerService.this.acquireSleepToken(tag, displayId);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ComponentName getHomeActivityForUser(int userId) {
            ComponentName componentName;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord homeActivity = ActivityTaskManagerService.this.mRootActivityContainer.getDefaultDisplayHomeActivityForUser(userId);
                    componentName = homeActivity == null ? null : homeActivity.mActivityComponent;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return componentName;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onLocalVoiceInteractionStarted(IBinder activity, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.onLocalVoiceInteractionStartedLocked(activity, voiceSession, voiceInteractor);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyAppTransitionStarting(SparseIntArray reasons, long timestamp) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mStackSupervisor.getActivityMetricsLogger().notifyTransitionStarting(reasons, timestamp);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyAppTransitionFinished() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mStackSupervisor.notifyAppTransitionDone();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyAppTransitionCancelled() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mStackSupervisor.notifyAppTransitionDone();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public List<IBinder> getTopVisibleActivities() {
            List<IBinder> topVisibleActivities;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    topVisibleActivities = ActivityTaskManagerService.this.mRootActivityContainer.getTopVisibleActivities();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return topVisibleActivities;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyDockedStackMinimizedChanged(boolean minimized) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.setDockedStackMinimized(minimized);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public int startActivitiesAsPackage(String packageName, int userId, Intent[] intents, Bundle bOptions) {
            int packageUid;
            Preconditions.checkNotNull(intents, "intents");
            String[] resolvedTypes = new String[intents.length];
            long ident = Binder.clearCallingIdentity();
            int i = 0;
            while (i < intents.length) {
                try {
                    resolvedTypes[i] = intents[i].resolveTypeIfNeeded(ActivityTaskManagerService.this.mContext.getContentResolver());
                    i++;
                } catch (RemoteException e) {
                    Binder.restoreCallingIdentity(ident);
                    packageUid = 0;
                    OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).onStartAppShotcut();
                    return ActivityTaskManagerService.this.getActivityStartController().startActivitiesInPackage(packageUid, packageName, intents, resolvedTypes, null, SafeActivityOptions.fromBundle(bOptions), userId, false, null, false);
                } catch (Throwable th) {
                    th = th;
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            }
            try {
                packageUid = AppGlobals.getPackageManager().getPackageUid(packageName, 268435456, userId);
                Binder.restoreCallingIdentity(ident);
            } catch (RemoteException e2) {
                Binder.restoreCallingIdentity(ident);
                packageUid = 0;
                OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).onStartAppShotcut();
                return ActivityTaskManagerService.this.getActivityStartController().startActivitiesInPackage(packageUid, packageName, intents, resolvedTypes, null, SafeActivityOptions.fromBundle(bOptions), userId, false, null, false);
            } catch (Throwable th2) {
                th = th2;
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
            OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).onStartAppShotcut();
            return ActivityTaskManagerService.this.getActivityStartController().startActivitiesInPackage(packageUid, packageName, intents, resolvedTypes, null, SafeActivityOptions.fromBundle(bOptions), userId, false, null, false);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public int startActivitiesInPackage(int uid, int realCallingPid, int realCallingUid, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, SafeActivityOptions options, int userId, boolean validateIncomingUser, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
            int startActivitiesInPackage;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    startActivitiesInPackage = ActivityTaskManagerService.this.getActivityStartController().startActivitiesInPackage(uid, realCallingPid, realCallingUid, callingPackage, intents, resolvedTypes, resultTo, options, userId, validateIncomingUser, originatingPendingIntent, allowBackgroundActivityStart);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return startActivitiesInPackage;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public int startActivityInPackage(int uid, int realCallingPid, int realCallingUid, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, SafeActivityOptions options, int userId, TaskRecord inTask, String reason, boolean validateIncomingUser, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
            int startActivityInPackage;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    startActivityInPackage = ActivityTaskManagerService.this.getActivityStartController().startActivityInPackage(uid, realCallingPid, realCallingUid, callingPackage, intent, resolvedType, resultTo, resultWho, requestCode, startFlags, options, userId, inTask, reason, validateIncomingUser, originatingPendingIntent, allowBackgroundActivityStart);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return startActivityInPackage;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public int startActivityAsUser(IApplicationThread caller, String callerPacakge, Intent intent, Bundle options, int userId) {
            ActivityTaskManagerService activityTaskManagerService = ActivityTaskManagerService.this;
            return activityTaskManagerService.startActivityAsUser(caller, callerPacakge, intent, intent.resolveTypeIfNeeded(activityTaskManagerService.mContext.getContentResolver()), null, null, 0, 268435456, null, options, userId, false);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0038, code lost:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x003b, code lost:
            if (r8 == null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x003d, code lost:
            r8.run();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
            return;
         */
        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyKeyguardFlagsChanged(Runnable callback, int displayId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityDisplay activityDisplay = ActivityTaskManagerService.this.mRootActivityContainer.getActivityDisplay(displayId);
                    if (activityDisplay != null) {
                        DisplayContent dc = activityDisplay.mDisplayContent;
                        boolean wasTransitionSet = dc.mAppTransition.getAppTransition() != 0;
                        if (!wasTransitionSet) {
                            dc.prepareAppTransition(0, false);
                        }
                        ActivityTaskManagerService.this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                        if (!wasTransitionSet) {
                            dc.executeAppTransition();
                        }
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyKeyguardTrustedChanged() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mKeyguardController.isKeyguardShowing(0)) {
                        ActivityTaskManagerService.this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setVr2dDisplayId(int vr2dDisplayId) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityTaskManagerService.TAG, "setVr2dDisplayId called for: " + vr2dDisplayId);
            }
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mVr2dDisplayId = vr2dDisplayId;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setFocusedActivity(IBinder token) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.forTokenLocked(token);
                    if (r == null) {
                        throw new IllegalArgumentException("setFocusedActivity: No activity record matching token=" + token);
                    } else if (r.moveFocusableActivityToTop("setFocusedActivity")) {
                        ActivityTaskManagerService.this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void registerScreenObserver(ActivityTaskManagerInternal.ScreenObserver observer) {
            ActivityTaskManagerService.this.mScreenObservers.add(observer);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isCallerRecents(int callingUid) {
            return ActivityTaskManagerService.this.getRecentTasks().isCallerRecents(callingUid);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isRecentsComponentHomeActivity(int userId) {
            return ActivityTaskManagerService.this.getRecentTasks().isRecentsComponentHomeActivity(userId);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void cancelRecentsAnimation(boolean restoreHomeStackPosition) {
            ActivityTaskManagerService.this.cancelRecentsAnimation(restoreHomeStackPosition);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void enforceCallerIsRecentsOrHasPermission(String permission, String func) {
            ActivityTaskManagerService.this.enforceCallerIsRecentsOrHasPermission(permission, func);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyActiveVoiceInteractionServiceChanged(ComponentName component) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mActiveVoiceInteractionServiceComponent = component;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0042, code lost:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0045, code lost:
            return;
         */
        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setAllowAppSwitches(String type, int uid, int userId) {
            if (ActivityTaskManagerService.this.mAmInternal.isUserRunning(userId, 1)) {
                synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        ArrayMap<String, Integer> types = ActivityTaskManagerService.this.mAllowAppSwitchUids.get(userId);
                        if (types == null) {
                            if (uid >= 0) {
                                types = new ArrayMap<>();
                                ActivityTaskManagerService.this.mAllowAppSwitchUids.put(userId, types);
                            } else {
                                return;
                            }
                        }
                        if (uid < 0) {
                            types.remove(type);
                        } else {
                            types.put(type, Integer.valueOf(uid));
                        }
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onUserStopped(int userId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.getRecentTasks().unloadUserDataFromMemoryLocked(userId);
                    ActivityTaskManagerService.this.mAllowAppSwitchUids.remove(userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isGetTasksAllowed(String caller, int callingPid, int callingUid) {
            boolean isGetTasksAllowed;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    isGetTasksAllowed = ActivityTaskManagerService.this.isGetTasksAllowed(caller, callingPid, callingUid);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return isGetTasksAllowed;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onProcessAdded(WindowProcessController proc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityTaskManagerService.this.mProcessNames.put(proc.mName, proc.mUid, proc);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onProcessRemoved(String name, int uid) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityTaskManagerService.this.mProcessNames.remove(name, uid);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onCleanUpApplicationRecord(WindowProcessController proc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                if (proc == ActivityTaskManagerService.this.mHomeProcess) {
                    ActivityTaskManagerService.this.mHomeProcess = null;
                }
                if (proc == ActivityTaskManagerService.this.mPreviousProcess) {
                    ActivityTaskManagerService.this.mPreviousProcess = null;
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public int getTopProcessState() {
            int i;
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                i = ActivityTaskManagerService.this.mTopProcessState;
            }
            return i;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isHeavyWeightProcess(WindowProcessController proc) {
            boolean z;
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                z = proc == ActivityTaskManagerService.this.mHeavyWeightProcess;
            }
            return z;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void clearHeavyWeightProcessIfEquals(WindowProcessController proc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityTaskManagerService.this.clearHeavyWeightProcessIfEquals(proc);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void finishHeavyWeightApp() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mHeavyWeightProcess != null) {
                        ActivityTaskManagerService.this.mHeavyWeightProcess.finishActivities();
                    }
                    ActivityTaskManagerService.this.clearHeavyWeightProcessIfEquals(ActivityTaskManagerService.this.mHeavyWeightProcess);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isSleeping() {
            boolean isSleepingLocked;
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                isSleepingLocked = ActivityTaskManagerService.this.isSleepingLocked();
            }
            return isSleepingLocked;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isShuttingDown() {
            boolean z;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    z = ActivityTaskManagerService.this.mShuttingDown;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return z;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean shuttingDown(boolean booted, int timeout) {
            boolean shutdownLocked;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mShuttingDown = true;
                    ActivityTaskManagerService.this.mRootActivityContainer.prepareForShutdown();
                    ActivityTaskManagerService.this.updateEventDispatchingLocked(booted);
                    ActivityTaskManagerService.this.notifyTaskPersisterLocked(null, true);
                    shutdownLocked = ActivityTaskManagerService.this.mStackSupervisor.shutdownLocked(timeout);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return shutdownLocked;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void enableScreenAfterBoot(boolean booted) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    EventLog.writeEvent((int) EventLogTags.BOOT_PROGRESS_ENABLE_SCREEN, SystemClock.uptimeMillis());
                    MtkSystemServer.getInstance().addBootEvent("AMS:ENABLE_SCREEN");
                    ActivityTaskManagerService.this.mWindowManager.enableScreenAfterBoot();
                    ActivityTaskManagerService.this.updateEventDispatchingLocked(booted);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean showStrictModeViolationDialog() {
            boolean z;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    z = ActivityTaskManagerService.this.mShowDialogs && !ActivityTaskManagerService.this.mSleeping && !ActivityTaskManagerService.this.mShuttingDown;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return z;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void showSystemReadyErrorDialogsIfNeeded() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (AppGlobals.getPackageManager().hasSystemUidErrors()) {
                        Slog.e(ActivityTaskManagerService.TAG, "UIDs on the system are inconsistent, you need to wipe your data partition or your device will be unstable.");
                        ActivityTaskManagerService.this.mUiHandler.post(new Runnable() {
                            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$LocalService$hXNJNh8HjV10X1ZEOI6o0Yzmq8o */

                            public final void run() {
                                ActivityTaskManagerService.LocalService.this.lambda$showSystemReadyErrorDialogsIfNeeded$0$ActivityTaskManagerService$LocalService();
                            }
                        });
                    }
                } catch (RemoteException e) {
                }
                try {
                    if (!Build.isBuildConsistent()) {
                        Slog.e(ActivityTaskManagerService.TAG, "Build fingerprint is not consistent, warning user");
                        ActivityTaskManagerService.this.mUiHandler.post(new Runnable() {
                            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$LocalService$xIfx_hFO4SXyNq34zoEHe3S9eU */

                            public final void run() {
                                ActivityTaskManagerService.LocalService.this.lambda$showSystemReadyErrorDialogsIfNeeded$1$ActivityTaskManagerService$LocalService();
                            }
                        });
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public /* synthetic */ void lambda$showSystemReadyErrorDialogsIfNeeded$0$ActivityTaskManagerService$LocalService() {
            if (ActivityTaskManagerService.this.mShowDialogs) {
                AlertDialog d = new BaseErrorDialog(ActivityTaskManagerService.this.mUiContext);
                d.getWindow().setType(2010);
                d.setCancelable(false);
                d.setTitle(ActivityTaskManagerService.this.mUiContext.getText(17039490));
                d.setMessage(ActivityTaskManagerService.this.mUiContext.getText(17041119));
                d.setButton(-1, ActivityTaskManagerService.this.mUiContext.getText(17039370), ActivityTaskManagerService.this.mUiHandler.obtainMessage(1, d));
                d.show();
            }
        }

        public /* synthetic */ void lambda$showSystemReadyErrorDialogsIfNeeded$1$ActivityTaskManagerService$LocalService() {
            if (ActivityTaskManagerService.this.mShowDialogs) {
                AlertDialog d = new BaseErrorDialog(ActivityTaskManagerService.this.mUiContext);
                d.getWindow().setType(2010);
                d.setCancelable(false);
                d.setTitle(ActivityTaskManagerService.this.mUiContext.getText(17039490));
                d.setMessage(ActivityTaskManagerService.this.mUiContext.getText(17041118));
                d.setButton(-1, ActivityTaskManagerService.this.mUiContext.getText(17039370), ActivityTaskManagerService.this.mUiHandler.obtainMessage(1, d));
                d.show();
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onProcessMapped(int pid, WindowProcessController proc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mProcessMap.put(pid, proc);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onProcessUnMapped(int pid) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mProcessMap.remove(pid);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onPackageDataCleared(String name) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mCompatModePackages.handlePackageDataClearedLocked(name);
                    ActivityTaskManagerService.this.mAppWarnings.onPackageDataCleared(name);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onPackageUninstalled(String name) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mAppWarnings.onPackageUninstalled(name);
                    ActivityTaskManagerService.this.mCompatModePackages.handlePackageUninstalledLocked(name);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onPackageAdded(String name, boolean replacing) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mCompatModePackages.handlePackageAddedLocked(name, replacing);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onPackageReplaced(ApplicationInfo aInfo) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.updateActivityApplicationInfo(aInfo);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public CompatibilityInfo compatibilityInfoForPackage(ApplicationInfo ai) {
            CompatibilityInfo compatibilityInfoForPackageLocked;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    compatibilityInfoForPackageLocked = ActivityTaskManagerService.this.compatibilityInfoForPackageLocked(ai);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return compatibilityInfoForPackageLocked;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x003c, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0062, code lost:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0065, code lost:
            return;
         */
        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onImeWindowSetOnDisplay(int pid, int displayId) {
            if (!InputMethodSystemProperty.MULTI_CLIENT_IME_ENABLED) {
                if (pid != ActivityManagerService.MY_PID && pid >= 0) {
                    synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            ActivityDisplay activityDisplay = ActivityTaskManagerService.this.mRootActivityContainer.getActivityDisplay(displayId);
                            if (activityDisplay != null) {
                                WindowProcessController process = ActivityTaskManagerService.this.mProcessMap.getProcess(pid);
                                if (process != null) {
                                    process.registerDisplayConfigurationListenerLocked(activityDisplay);
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                } else if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                    Slog.w(ActivityTaskManagerService.TAG, "Trying to update display configuration for invalid process, pid=" + pid);
                                }
                            } else if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                Slog.w(ActivityTaskManagerService.TAG, "Trying to update display configuration for non-existing displayId=" + displayId);
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                } else if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.w(ActivityTaskManagerService.TAG, "Trying to update display configuration for system/invalid process.");
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void sendActivityResult(int callingUid, IBinder activityToken, String resultWho, int requestCode, int resultCode, Intent data) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(activityToken);
                    if (!(r == null || r.getActivityStack() == null)) {
                        r.getActivityStack().sendActivityResultLocked(callingUid, r, resultWho, requestCode, resultCode, data);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void clearPendingResultForActivity(IBinder activityToken, WeakReference<PendingIntentRecord> pir) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(activityToken);
                    if (!(r == null || r.pendingResults == null)) {
                        r.pendingResults.remove(pir);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ActivityTaskManagerInternal.ActivityTokens getTopActivityForTask(int taskId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    TaskRecord taskRecord = ActivityTaskManagerService.this.mRootActivityContainer.anyTaskForId(taskId);
                    if (taskRecord == null) {
                        Slog.w(ActivityTaskManagerService.TAG, "getApplicationThreadForTopActivity failed: Requested task not found");
                        return null;
                    }
                    ActivityRecord activity = taskRecord.getTopActivity();
                    if (activity == null) {
                        Slog.w(ActivityTaskManagerService.TAG, "getApplicationThreadForTopActivity failed: Requested activity not found");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    } else if (!activity.attachedToProcess()) {
                        Slog.w(ActivityTaskManagerService.TAG, "getApplicationThreadForTopActivity failed: No process for " + activity);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    } else {
                        ActivityTaskManagerInternal.ActivityTokens activityTokens = new ActivityTaskManagerInternal.ActivityTokens(activity.appToken, activity.assistToken, activity.app.getThread());
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return activityTokens;
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public IIntentSender getIntentSender(int type, String packageName, int callingUid, int userId, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle bOptions) {
            IIntentSender intentSenderLocked;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    intentSenderLocked = ActivityTaskManagerService.this.getIntentSenderLocked(type, packageName, callingUid, userId, token, resultWho, requestCode, intents, resolvedTypes, flags, bOptions);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return intentSenderLocked;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ActivityServiceConnectionsHolder getServiceConnectionsHolder(IBinder token) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        return null;
                    }
                    if (r.mServiceConnectionsHolder == null) {
                        r.mServiceConnectionsHolder = new ActivityServiceConnectionsHolder(ActivityTaskManagerService.this, r);
                    }
                    ActivityServiceConnectionsHolder activityServiceConnectionsHolder = r.mServiceConnectionsHolder;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return activityServiceConnectionsHolder;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public Intent getHomeIntent() {
            Intent homeIntent;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    homeIntent = ActivityTaskManagerService.this.getHomeIntent();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return homeIntent;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean startHomeActivity(int userId, String reason) {
            boolean startHomeOnDisplay;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    startHomeOnDisplay = ActivityTaskManagerService.this.mRootActivityContainer.startHomeOnDisplay(userId, reason, 0);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return startHomeOnDisplay;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean startHomeOnDisplay(int userId, String reason, int displayId, boolean allowInstrumenting, boolean fromHomeKey) {
            boolean startHomeOnDisplay;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    startHomeOnDisplay = ActivityTaskManagerService.this.mRootActivityContainer.startHomeOnDisplay(userId, reason, displayId, allowInstrumenting, fromHomeKey);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return startHomeOnDisplay;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean startHomeOnAllDisplays(int userId, String reason) {
            boolean startHomeOnAllDisplays;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    startHomeOnAllDisplays = ActivityTaskManagerService.this.mRootActivityContainer.startHomeOnAllDisplays(userId, reason);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return startHomeOnAllDisplays;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:21:0x003e, code lost:
            return r2;
         */
        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isFactoryTestProcess(WindowProcessController wpc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                boolean z = false;
                if (ActivityTaskManagerService.this.mFactoryTest == 0) {
                    return false;
                }
                if (ActivityTaskManagerService.this.mFactoryTest == 1 && ActivityTaskManagerService.this.mTopComponent != null && wpc.mName.equals(ActivityTaskManagerService.this.mTopComponent.getPackageName())) {
                    return true;
                }
                if (ActivityTaskManagerService.this.mFactoryTest == 2 && (wpc.mInfo.flags & 16) != 0) {
                    z = true;
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void updateTopComponentForFactoryTest() {
            CharSequence errorMsg;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mFactoryTest == 1) {
                        ResolveInfo ri = ActivityTaskManagerService.this.mContext.getPackageManager().resolveActivity(new Intent("android.intent.action.FACTORY_TEST"), 1024);
                        if (ri != null) {
                            ActivityInfo ai = ri.activityInfo;
                            ApplicationInfo app = ai.applicationInfo;
                            if ((1 & app.flags) != 0) {
                                ActivityTaskManagerService.this.mTopAction = "android.intent.action.FACTORY_TEST";
                                ActivityTaskManagerService.this.mTopData = null;
                                ActivityTaskManagerService.this.mTopComponent = new ComponentName(app.packageName, ai.name);
                                errorMsg = null;
                            } else {
                                errorMsg = ActivityTaskManagerService.this.mContext.getResources().getText(17039997);
                            }
                        } else {
                            errorMsg = ActivityTaskManagerService.this.mContext.getResources().getText(17039996);
                        }
                        if (errorMsg == null) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                        ActivityTaskManagerService.this.mTopAction = null;
                        ActivityTaskManagerService.this.mTopData = null;
                        ActivityTaskManagerService.this.mTopComponent = null;
                        ActivityTaskManagerService.this.mUiHandler.post(new Runnable(errorMsg) {
                            /* class com.android.server.wm.$$Lambda$ActivityTaskManagerService$LocalService$smesvyl87CxHptMAvRA559Glc1k */
                            private final /* synthetic */ CharSequence f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                ActivityTaskManagerService.LocalService.this.lambda$updateTopComponentForFactoryTest$2$ActivityTaskManagerService$LocalService(this.f$1);
                            }
                        });
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public /* synthetic */ void lambda$updateTopComponentForFactoryTest$2$ActivityTaskManagerService$LocalService(CharSequence errorMsg) {
            new FactoryErrorDialog(ActivityTaskManagerService.this.mUiContext, errorMsg).show();
            ActivityTaskManagerService.this.mAmInternal.ensureBootCompleted();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0090, code lost:
            return;
         */
        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void handleAppDied(WindowProcessController wpc, boolean restarting, Runnable finishInstrumentationCallback) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                boolean hasVisibleActivities = ActivityTaskManagerService.this.mRootActivityContainer.handleAppDied(wpc);
                if (ActivityTaskManagerService.this.getColorFreeformManager() != null) {
                    ActivityTaskManagerService.this.getColorFreeformManager().handleParentDied(wpc.getPid());
                }
                wpc.clearRecentTasks();
                wpc.clearActivities();
                if (wpc.isInstrumenting()) {
                    finishInstrumentationCallback.run();
                }
                if (OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).validRestartProcess(wpc.mUid, wpc.mPkgList)) {
                    Slog.i(IColorAbnormalAppManager.TAG, "UL restart for activity " + wpc + " : is R");
                } else if (!restarting && hasVisibleActivities) {
                    ActivityTaskManagerService.this.mWindowManager.deferSurfaceLayout();
                    try {
                        if (!ActivityTaskManagerService.this.mRootActivityContainer.resumeFocusedStacksTopActivities()) {
                            ActivityTaskManagerService.this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
                        }
                    } finally {
                        ActivityTaskManagerService.this.mWindowManager.continueSurfaceLayout();
                    }
                }
            }
        }

        /* JADX INFO: finally extract failed */
        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void closeSystemDialogs(String reason) {
            ActivityTaskManagerService.this.enforceNotIsolatedCaller("closeSystemDialogs");
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        if (uid >= 10000) {
                            WindowProcessController proc = ActivityTaskManagerService.this.mProcessMap.getProcess(pid);
                            if (!proc.isPerceptible()) {
                                Slog.w(ActivityTaskManagerService.TAG, "Ignoring closeSystemDialogs " + reason + " from background process " + proc);
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                        }
                        ActivityTaskManagerService.this.mWindowManager.closeSystemDialogs(reason);
                        ActivityTaskManagerService.this.mRootActivityContainer.closeSystemDialogs();
                        WindowManagerService.resetPriorityAfterLockedSection();
                        ActivityTaskManagerService.this.mAmInternal.broadcastCloseSystemDialogs(reason);
                        Binder.restoreCallingIdentity(origId);
                    } catch (Throwable th) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void cleanupDisabledPackageComponents(String packageName, Set<String> disabledClasses, int userId, boolean booted) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mRootActivityContainer.finishDisabledPackageActivities(packageName, disabledClasses, true, false, userId) && booted) {
                        ActivityTaskManagerService.this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                        ActivityTaskManagerService.this.mStackSupervisor.scheduleIdleLocked();
                    }
                    ActivityTaskManagerService.this.getRecentTasks().cleanupDisabledPackageTasksLocked(packageName, disabledClasses, userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean onForceStopPackage(String packageName, boolean doit, boolean evenPersistent, int userId) {
            boolean didSomething;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    didSomething = ActivityTaskManagerService.this.getActivityStartController().clearPendingActivityLaunches(packageName) | ActivityTaskManagerService.this.mRootActivityContainer.finishDisabledPackageActivities(packageName, null, doit, evenPersistent, userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return didSomething;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void resumeTopActivities(boolean scheduleIdle) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                    if (scheduleIdle) {
                        ActivityTaskManagerService.this.mStackSupervisor.scheduleIdleLocked();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void preBindApplication(WindowProcessController wpc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityTaskManagerService.this.mStackSupervisor.getActivityMetricsLogger().notifyBindApplication(wpc.mInfo);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean attachApplication(WindowProcessController wpc) throws RemoteException {
            boolean attachApplication;
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                attachApplication = ActivityTaskManagerService.this.mRootActivityContainer.attachApplication(wpc);
            }
            return attachApplication;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void notifyLockedProfile(int userId, int currentUserId) {
            try {
                if (AppGlobals.getPackageManager().isUidPrivileged(Binder.getCallingUid())) {
                    synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            long ident = Binder.clearCallingIdentity();
                            try {
                                if (ActivityTaskManagerService.this.mAmInternal.shouldConfirmCredentials(userId)) {
                                    if (ActivityTaskManagerService.this.mKeyguardController.isKeyguardLocked()) {
                                        startHomeActivity(currentUserId, "notifyLockedProfile");
                                    }
                                    ActivityTaskManagerService.this.mRootActivityContainer.lockAllProfileTasks(userId);
                                }
                            } finally {
                                Binder.restoreCallingIdentity(ident);
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                }
                throw new SecurityException("Only privileged app can call notifyLockedProfile");
            } catch (RemoteException ex) {
                throw new SecurityException("Fail to check is caller a privileged app", ex);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void startConfirmDeviceCredentialIntent(Intent intent, Bundle options) {
            ActivityTaskManagerService.this.mAmInternal.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "startConfirmDeviceCredentialIntent");
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    long ident = Binder.clearCallingIdentity();
                    try {
                        intent.addFlags(276824064);
                        ActivityTaskManagerService.this.mContext.startActivityAsUser(intent, (options != null ? new ActivityOptions(options) : ActivityOptions.makeBasic()).toBundle(), UserHandle.CURRENT);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void writeActivitiesToProto(ProtoOutputStream proto) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.writeToProto(proto, 1146756268033L, 0);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void saveANRState(String reason) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new FastPrintWriter(sw, false, 1024);
                    pw.println("  ANR time: " + DateFormat.getDateTimeInstance().format(new Date()));
                    if (reason != null) {
                        pw.println("  Reason: " + reason);
                    }
                    pw.println();
                    ActivityTaskManagerService.this.getActivityStartController().dump(pw, "  ", null);
                    pw.println();
                    pw.println("-------------------------------------------------------------------------------");
                    ActivityTaskManagerService.this.dumpActivitiesLocked(null, pw, null, 0, true, false, null, "");
                    pw.println();
                    pw.close();
                    ActivityTaskManagerService.this.mLastANRState = sw.toString();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void clearSavedANRState() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mLastANRState = null;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a6, code lost:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a9, code lost:
            return;
         */
        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void dump(String cmd, FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, boolean dumpClient, String dumpPackage) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (!ActivityTaskManagerService.DUMP_ACTIVITIES_CMD.equals(cmd)) {
                        if (!ActivityTaskManagerService.DUMP_ACTIVITIES_SHORT_CMD.equals(cmd)) {
                            if (ActivityTaskManagerService.DUMP_LASTANR_CMD.equals(cmd)) {
                                ActivityTaskManagerService.this.dumpLastANRLocked(pw);
                            } else if (ActivityTaskManagerService.DUMP_LASTANR_TRACES_CMD.equals(cmd)) {
                                ActivityTaskManagerService.this.dumpLastANRTracesLocked(pw);
                            } else if (ActivityTaskManagerService.DUMP_STARTER_CMD.equals(cmd)) {
                                ActivityTaskManagerService.this.dumpActivityStarterLocked(pw, dumpPackage);
                            } else if (ActivityTaskManagerService.DUMP_CONTAINERS_CMD.equals(cmd)) {
                                ActivityTaskManagerService.this.dumpActivityContainersLocked(pw);
                            } else {
                                if (!ActivityTaskManagerService.DUMP_RECENTS_CMD.equals(cmd)) {
                                    if (ActivityTaskManagerService.DUMP_RECENTS_SHORT_CMD.equals(cmd)) {
                                    }
                                }
                                if (ActivityTaskManagerService.this.getRecentTasks() != null) {
                                    try {
                                        ActivityTaskManagerService.this.getRecentTasks().dump(pw, dumpAll, dumpPackage);
                                    } catch (Throwable th) {
                                        th = th;
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                        throw th;
                                    }
                                }
                            }
                        }
                    }
                    ActivityTaskManagerService.this.dumpActivitiesLocked(fd, pw, args, opti, dumpAll, dumpClient, dumpPackage);
                } catch (Throwable th2) {
                    th = th2;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:103:0x0353, code lost:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:104:0x0356, code lost:
            return r5;
         */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0065 A[Catch:{ all -> 0x035b }] */
        /* JADX WARNING: Removed duplicated region for block: B:40:0x00c4 A[Catch:{ all -> 0x035b }] */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x00e3 A[Catch:{ all -> 0x035b }] */
        /* JADX WARNING: Removed duplicated region for block: B:45:0x0108 A[Catch:{ all -> 0x035b }] */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x0191 A[SYNTHETIC, Splitter:B:61:0x0191] */
        /* JADX WARNING: Removed duplicated region for block: B:70:0x0253 A[Catch:{ all -> 0x024e, all -> 0x0357 }] */
        /* JADX WARNING: Removed duplicated region for block: B:73:0x025b A[Catch:{ all -> 0x024e, all -> 0x0357 }] */
        /* JADX WARNING: Removed duplicated region for block: B:76:0x026f A[Catch:{ all -> 0x024e, all -> 0x0357 }] */
        /* JADX WARNING: Removed duplicated region for block: B:96:0x02f0 A[Catch:{ all -> 0x036d }] */
        /* JADX WARNING: Removed duplicated region for block: B:98:0x02f4 A[Catch:{ all -> 0x036d }] */
        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean dumpForProcesses(FileDescriptor fd, PrintWriter pw, boolean dumpAll, String dumpPackage, int dumpAppId, boolean needSep, boolean testPssMode, int wakefulness) {
            boolean needSep2;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mHomeProcess != null) {
                        if (dumpPackage != 0) {
                            try {
                                if (ActivityTaskManagerService.this.mHomeProcess.mPkgList.contains(dumpPackage)) {
                                }
                            } catch (Throwable th) {
                                th = th;
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                        if (needSep) {
                            pw.println();
                            needSep2 = false;
                        } else {
                            needSep2 = needSep;
                        }
                        try {
                            pw.println("  mHomeProcess: " + ActivityTaskManagerService.this.mHomeProcess);
                            if (ActivityTaskManagerService.this.mPreviousProcess != null && (dumpPackage == 0 || ActivityTaskManagerService.this.mPreviousProcess.mPkgList.contains(dumpPackage))) {
                                if (needSep2) {
                                    pw.println();
                                    needSep2 = false;
                                }
                                pw.println("  mPreviousProcess: " + ActivityTaskManagerService.this.mPreviousProcess);
                            }
                            if (dumpAll && (ActivityTaskManagerService.this.mPreviousProcess == null || dumpPackage == 0 || ActivityTaskManagerService.this.mPreviousProcess.mPkgList.contains(dumpPackage))) {
                                StringBuilder sb = new StringBuilder(128);
                                sb.append("  mPreviousProcessVisibleTime: ");
                                TimeUtils.formatDuration(ActivityTaskManagerService.this.mPreviousProcessVisibleTime, sb);
                                pw.println(sb);
                            }
                            if (ActivityTaskManagerService.this.mHeavyWeightProcess != null && (dumpPackage == 0 || ActivityTaskManagerService.this.mHeavyWeightProcess.mPkgList.contains(dumpPackage))) {
                                if (needSep2) {
                                    pw.println();
                                    needSep2 = false;
                                }
                                pw.println("  mHeavyWeightProcess: " + ActivityTaskManagerService.this.mHeavyWeightProcess);
                            }
                            if (dumpPackage == 0) {
                                pw.println("  mGlobalConfiguration: " + ActivityTaskManagerService.this.getGlobalConfiguration());
                                ActivityTaskManagerService.this.mRootActivityContainer.dumpDisplayConfigs(pw, "  ");
                            }
                            if (dumpAll) {
                                if (dumpPackage == 0) {
                                    pw.println("  mConfigWillChange: " + ActivityTaskManagerService.this.getTopDisplayFocusedStack().mConfigWillChange);
                                }
                                if (ActivityTaskManagerService.this.mCompatModePackages.getPackages().size() > 0) {
                                    boolean printed = false;
                                    for (Map.Entry<String, Integer> entry : ActivityTaskManagerService.this.mCompatModePackages.getPackages().entrySet()) {
                                        String pkg = entry.getKey();
                                        int mode = entry.getValue().intValue();
                                        if (dumpPackage == 0 || dumpPackage.equals(pkg)) {
                                            if (!printed) {
                                                pw.println("  mScreenCompatPackages:");
                                                printed = true;
                                            }
                                            pw.println("    " + pkg + ": " + mode);
                                        }
                                    }
                                }
                            }
                            if (dumpPackage != 0) {
                                try {
                                    pw.println("  mWakefulness=" + PowerManagerInternal.wakefulnessToString(wakefulness));
                                    pw.println("  mSleepTokens=" + ActivityTaskManagerService.this.mRootActivityContainer.mSleepTokens);
                                    if (ActivityTaskManagerService.this.mRunningVoice != null) {
                                        pw.println("  mRunningVoice=" + ActivityTaskManagerService.this.mRunningVoice);
                                        pw.println("  mVoiceWakeLock" + ActivityTaskManagerService.this.mVoiceWakeLock);
                                    }
                                    pw.println("  mSleeping=" + ActivityTaskManagerService.this.mSleeping);
                                    StringBuilder sb2 = new StringBuilder();
                                    sb2.append("  mShuttingDown=");
                                    sb2.append(ActivityTaskManagerService.this.mShuttingDown);
                                    sb2.append(" mTestPssMode=");
                                    sb2.append(testPssMode);
                                    pw.println(sb2.toString());
                                    pw.println("  mVrController=" + ActivityTaskManagerService.this.mVrController);
                                } catch (Throwable th2) {
                                    th = th2;
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            if (ActivityTaskManagerService.this.mCurAppTimeTracker != null) {
                                ActivityTaskManagerService.this.mCurAppTimeTracker.dumpWithHeader(pw, "  ", true);
                            }
                            if (ActivityTaskManagerService.this.mAllowAppSwitchUids.size() <= 0) {
                                boolean printed2 = false;
                                for (int i = 0; i < ActivityTaskManagerService.this.mAllowAppSwitchUids.size(); i++) {
                                    ArrayMap<String, Integer> types = ActivityTaskManagerService.this.mAllowAppSwitchUids.valueAt(i);
                                    for (int j = 0; j < types.size(); j++) {
                                        if (dumpPackage != 0) {
                                            if (UserHandle.getAppId(types.valueAt(j).intValue()) != dumpAppId) {
                                            }
                                        }
                                        if (needSep2) {
                                            try {
                                                pw.println();
                                                needSep2 = false;
                                            } catch (Throwable th3) {
                                                th = th3;
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                throw th;
                                            }
                                        }
                                        if (!printed2) {
                                            pw.println("  mAllowAppSwitchUids:");
                                            printed2 = true;
                                        }
                                        pw.print("    User ");
                                        pw.print(ActivityTaskManagerService.this.mAllowAppSwitchUids.keyAt(i));
                                        pw.print(": Type ");
                                        pw.print(types.keyAt(j));
                                        pw.print(" = ");
                                        UserHandle.formatUid(pw, types.valueAt(j).intValue());
                                        pw.println();
                                    }
                                }
                            }
                            if (dumpPackage == 0) {
                                if (ActivityTaskManagerService.this.mController != null) {
                                    pw.println("  mController=" + ActivityTaskManagerService.this.mController + " mControllerIsAMonkey=" + ActivityTaskManagerService.this.mControllerIsAMonkey);
                                }
                                pw.println("  mGoingToSleepWakeLock=" + ActivityTaskManagerService.this.mStackSupervisor.mGoingToSleepWakeLock);
                                pw.println("  mLaunchingActivityWakeLock=" + ActivityTaskManagerService.this.mStackSupervisor.mLaunchingActivityWakeLock);
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                    needSep2 = needSep;
                    if (needSep2) {
                    }
                    pw.println("  mPreviousProcess: " + ActivityTaskManagerService.this.mPreviousProcess);
                    StringBuilder sb3 = new StringBuilder(128);
                    sb3.append("  mPreviousProcessVisibleTime: ");
                    TimeUtils.formatDuration(ActivityTaskManagerService.this.mPreviousProcessVisibleTime, sb3);
                    pw.println(sb3);
                    if (needSep2) {
                    }
                    pw.println("  mHeavyWeightProcess: " + ActivityTaskManagerService.this.mHeavyWeightProcess);
                    if (dumpPackage == 0) {
                    }
                    if (dumpAll) {
                    }
                    if (dumpPackage != 0) {
                    }
                    if (ActivityTaskManagerService.this.mCurAppTimeTracker != null) {
                    }
                    if (ActivityTaskManagerService.this.mAllowAppSwitchUids.size() <= 0) {
                    }
                    if (dumpPackage == 0) {
                    }
                } catch (Throwable th5) {
                    th = th5;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void writeProcessesToProto(ProtoOutputStream proto, String dumpPackage, int wakeFullness, boolean testPssMode) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (dumpPackage == 0) {
                        ActivityTaskManagerService.this.getGlobalConfiguration().writeToProto(proto, 1146756268051L);
                        proto.write(1133871366165L, ActivityTaskManagerService.this.getTopDisplayFocusedStack().mConfigWillChange);
                        ActivityTaskManagerService.this.writeSleepStateToProto(proto, wakeFullness, testPssMode);
                        if (ActivityTaskManagerService.this.mRunningVoice != null) {
                            long vrToken = proto.start(1146756268060L);
                            proto.write(1138166333441L, ActivityTaskManagerService.this.mRunningVoice.toString());
                            ActivityTaskManagerService.this.mVoiceWakeLock.writeToProto(proto, 1146756268034L);
                            proto.end(vrToken);
                        }
                        ActivityTaskManagerService.this.mVrController.writeToProto(proto, 1146756268061L);
                        if (ActivityTaskManagerService.this.mController != null) {
                            long token = proto.start(1146756268069L);
                            proto.write(1146756268069L, ActivityTaskManagerService.this.mController.toString());
                            proto.write(1133871366146L, ActivityTaskManagerService.this.mControllerIsAMonkey);
                            proto.end(token);
                        }
                        ActivityTaskManagerService.this.mStackSupervisor.mGoingToSleepWakeLock.writeToProto(proto, 1146756268079L);
                        ActivityTaskManagerService.this.mStackSupervisor.mLaunchingActivityWakeLock.writeToProto(proto, 1146756268080L);
                    }
                    if (ActivityTaskManagerService.this.mHomeProcess != null && (dumpPackage == 0 || ActivityTaskManagerService.this.mHomeProcess.mPkgList.contains(dumpPackage))) {
                        ActivityTaskManagerService.this.mHomeProcess.writeToProto(proto, 1146756268047L);
                    }
                    if (ActivityTaskManagerService.this.mPreviousProcess != null && (dumpPackage == 0 || ActivityTaskManagerService.this.mPreviousProcess.mPkgList.contains(dumpPackage))) {
                        ActivityTaskManagerService.this.mPreviousProcess.writeToProto(proto, 1146756268048L);
                        proto.write(1112396529681L, ActivityTaskManagerService.this.mPreviousProcessVisibleTime);
                    }
                    if (ActivityTaskManagerService.this.mHeavyWeightProcess != null && (dumpPackage == 0 || ActivityTaskManagerService.this.mHeavyWeightProcess.mPkgList.contains(dumpPackage))) {
                        ActivityTaskManagerService.this.mHeavyWeightProcess.writeToProto(proto, 1146756268050L);
                    }
                    for (Map.Entry<String, Integer> entry : ActivityTaskManagerService.this.mCompatModePackages.getPackages().entrySet()) {
                        String pkg = entry.getKey();
                        int mode = entry.getValue().intValue();
                        if (dumpPackage == 0 || dumpPackage.equals(pkg)) {
                            long compatToken = proto.start(2246267895830L);
                            proto.write(1138166333441L, pkg);
                            proto.write(1120986464258L, mode);
                            proto.end(compatToken);
                        }
                    }
                    if (ActivityTaskManagerService.this.mCurAppTimeTracker != null) {
                        ActivityTaskManagerService.this.mCurAppTimeTracker.writeToProto(proto, 1146756268063L, true);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean dumpActivity(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll, boolean dumpVisibleStacksOnly, boolean dumpFocusedStackOnly) {
            boolean dumpActivity;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    dumpActivity = ActivityTaskManagerService.this.dumpActivity(fd, pw, name, args, opti, dumpAll, dumpVisibleStacksOnly, dumpFocusedStackOnly);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return dumpActivity;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void dumpForOom(PrintWriter pw) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    pw.println("  mHomeProcess: " + ActivityTaskManagerService.this.mHomeProcess);
                    pw.println("  mPreviousProcess: " + ActivityTaskManagerService.this.mPreviousProcess);
                    if (ActivityTaskManagerService.this.mHeavyWeightProcess != null) {
                        pw.println("  mHeavyWeightProcess: " + ActivityTaskManagerService.this.mHeavyWeightProcess);
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean canGcNow() {
            boolean z;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (!isSleeping()) {
                        if (!ActivityTaskManagerService.this.mRootActivityContainer.allResumedActivitiesIdle()) {
                            z = false;
                        }
                    }
                    z = true;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return z;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public WindowProcessController getTopApp() {
            WindowProcessController windowProcessController;
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityRecord top = ActivityTaskManagerService.this.mRootActivityContainer.getTopResumedActivity();
                windowProcessController = top != null ? top.app : null;
            }
            return windowProcessController;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void rankTaskLayersIfNeeded() {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                if (ActivityTaskManagerService.this.mRootActivityContainer != null) {
                    ActivityTaskManagerService.this.mRootActivityContainer.rankTaskLayersIfNeeded();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void scheduleDestroyAllActivities(String reason) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.scheduleDestroyAllActivities(null, reason);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void removeUser(int userId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.removeUser(userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean switchUser(int userId, UserState userState) {
            boolean switchUser;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    switchUser = ActivityTaskManagerService.this.mRootActivityContainer.switchUser(userId, userState);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return switchUser;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onHandleAppCrash(WindowProcessController wpc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRootActivityContainer.handleAppCrash(wpc);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public int finishTopCrashedActivities(WindowProcessController crashedApp, String reason) {
            int finishTopCrashedActivities;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    finishTopCrashedActivities = ActivityTaskManagerService.this.mRootActivityContainer.finishTopCrashedActivities(crashedApp, reason);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return finishTopCrashedActivities;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onUidActive(int uid, int procState) {
            ActivityTaskManagerService.this.mActiveUids.onUidActive(uid, procState);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onUidInactive(int uid) {
            ActivityTaskManagerService.this.mActiveUids.onUidInactive(uid);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onActiveUidsCleared() {
            ActivityTaskManagerService.this.mActiveUids.onActiveUidsCleared();
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onUidProcStateChanged(int uid, int procState) {
            ActivityTaskManagerService.this.mActiveUids.onUidProcStateChanged(uid, procState);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onUidAddedToPendingTempWhitelist(int uid, String tag) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityTaskManagerService.this.mPendingTempWhitelist.put(uid, tag);
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onUidRemovedFromPendingTempWhitelist(int uid) {
            synchronized (ActivityTaskManagerService.this.mGlobalLockWithoutBoost) {
                ActivityTaskManagerService.this.mPendingTempWhitelist.remove(uid);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:25:0x006e, code lost:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0071, code lost:
            return false;
         */
        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean handleAppCrashInActivityController(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace, Runnable killCrashingAppCallback) {
            boolean procRes;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (ActivityTaskManagerService.this.mController == null) {
                        return false;
                    }
                    try {
                        if (ActivityTaskManagerService.this.mOppoActivityControlerScheduler != null) {
                            procRes = ActivityTaskManagerService.this.mOppoActivityControlerScheduler.scheduleAppCrash(processName, pid, shortMsg, longMsg, timeMillis, stackTrace);
                        } else {
                            procRes = ActivityTaskManagerService.this.mController.appCrashed(processName, pid, shortMsg, longMsg, timeMillis, stackTrace);
                        }
                        if (!procRes) {
                            killCrashingAppCallback.run();
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return true;
                        }
                    } catch (RemoteException e) {
                        ActivityTaskManagerService.this.mController = null;
                        Watchdog.getInstance().setActivityController(null);
                        if (ActivityTaskManagerService.this.mOppoActivityControlerScheduler != null) {
                            ActivityTaskManagerService.this.mOppoActivityControlerScheduler.exitRunningScheduler();
                            ActivityTaskManagerService.this.mOppoActivityControlerScheduler = null;
                        }
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void removeRecentTasksByPackageName(String packageName, int userId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRecentTasks.removeTasksByPackageName(packageName, userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void cleanupRecentTasksForUser(int userId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRecentTasks.cleanupLocked(userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void loadRecentTasksForUser(int userId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    OppoEngineerFunctionManager.tryRemoveAllUserRecentTasksLocked();
                    ActivityTaskManagerService.this.mRecentTasks.loadUserRecentsLocked(userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void onPackagesSuspendedChanged(String[] packages, boolean suspended, int userId) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mRecentTasks.onPackagesSuspendedChanged(packages, suspended, userId);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void flushRecentTasks() {
            ActivityTaskManagerService.this.mRecentTasks.flush();
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public WindowProcessController getHomeProcess() {
            WindowProcessController windowProcessController;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    windowProcessController = ActivityTaskManagerService.this.mHomeProcess;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return windowProcessController;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public WindowProcessController getPreviousProcess() {
            WindowProcessController windowProcessController;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    windowProcessController = ActivityTaskManagerService.this.mPreviousProcess;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return windowProcessController;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void clearLockedTasks(String reason) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.getLockTaskController().clearLockedTasks(reason);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void updateUserConfiguration() {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    Configuration configuration = new Configuration(ActivityTaskManagerService.this.getGlobalConfiguration());
                    int currentUserId = ActivityTaskManagerService.this.mAmInternal.getCurrentUserId();
                    Settings.System.adjustConfigurationForUser(ActivityTaskManagerService.this.mContext.getContentResolver(), configuration, currentUserId, Settings.System.canWrite(ActivityTaskManagerService.this.mContext));
                    ActivityTaskManagerService.this.updateExtraConfigurationForUser(ActivityTaskManagerService.this.mContext, configuration, currentUserId);
                    Log.d("ATMS", "updateConfigurationLocked : updateUserIdInExtraCOnfiguration userid = " + currentUserId);
                    ActivityTaskManagerService.this.updateUserIdInExtraConfiguration(configuration, currentUserId);
                    boolean unused = ActivityTaskManagerService.this.updateConfigurationLocked(configuration, null, false, false, currentUserId, false);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean canShowErrorDialogs() {
            boolean z;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    z = false;
                    if (ActivityTaskManagerService.this.mShowDialogs && !ActivityTaskManagerService.this.mSleeping && !ActivityTaskManagerService.this.mShuttingDown && !ActivityTaskManagerService.this.mKeyguardController.isKeyguardOrAodShowing(0) && !ActivityTaskManagerService.this.hasUserRestriction("no_system_error_dialogs", ActivityTaskManagerService.this.mAmInternal.getCurrentUserId()) && (!UserManager.isDeviceInDemoMode(ActivityTaskManagerService.this.mContext) || !ActivityTaskManagerService.this.mAmInternal.getCurrentUser().isDemo())) {
                        z = true;
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return z;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setProfileApp(String profileApp) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mProfileApp = profileApp;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setProfileProc(WindowProcessController wpc) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mProfileProc = wpc;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setProfilerInfo(ProfilerInfo profilerInfo) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mProfilerInfo = profilerInfo;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ActivityMetricsLaunchObserverRegistry getLaunchObserverRegistry() {
            ActivityMetricsLaunchObserverRegistry launchObserverRegistry;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    launchObserverRegistry = ActivityTaskManagerService.this.mStackSupervisor.getActivityMetricsLogger().getLaunchObserverRegistry();
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return launchObserverRegistry;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public ActivityManager.TaskSnapshot getTaskSnapshotNoRestore(int taskId, boolean reducedResolution) {
            return ActivityTaskManagerService.this.getTaskSnapshot(taskId, reducedResolution, false);
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isUidForeground(int uid) {
            boolean isUidForeground;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    isUidForeground = ActivityTaskManagerService.this.isUidForeground(uid);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return isUidForeground;
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setDeviceOwnerUid(int uid) {
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.setDeviceOwnerUid(uid);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public void setCompanionAppPackages(int userId, Set<String> companionAppPackages) {
            HashSet hashSet = new HashSet();
            for (String pkg : companionAppPackages) {
                int uid = ActivityTaskManagerService.this.getPackageManagerInternalLocked().getPackageUid(pkg, 0, userId);
                if (uid >= 0) {
                    hashSet.add(Integer.valueOf(uid));
                }
            }
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityTaskManagerService.this.mCompanionAppUidsMap.put(Integer.valueOf(userId), hashSet);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        @Override // com.android.server.wm.ActivityTaskManagerInternal
        public boolean isActivityInActivityStack(IBinder token) {
            boolean z;
            synchronized (ActivityTaskManagerService.this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    z = ActivityRecord.forTokenLocked(token) != null;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return z;
        }
    }

    public class ColorActivityTaskManagerServiceInner implements IColorActivityTaskManagerServiceInner {
        public ColorActivityTaskManagerServiceInner() {
        }

        @Override // com.android.server.wm.IColorActivityTaskManagerServiceInner
        public boolean getShowDialogs() {
            return ActivityTaskManagerService.this.mShowDialogs;
        }
    }

    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public IColorActivityTaskManagerServiceInner createColorActivityTaskManagerServiceInner() {
        return new ColorActivityTaskManagerServiceInner();
    }

    public void setOppoKinectActivityController(IOppoKinectActivityController controller) {
        this.mAmInternal.enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "setActivityController()");
        synchronized (this) {
            this.mOppoKinectController = controller;
        }
    }

    private void enableDefaultLogIfNeed() {
        if (ActivityTaskManagerDebugConfig.DEBUG_AMS) {
            ActivityTaskManagerDebugConfig.DEBUG_SWITCH = true;
            ActivityTaskManagerDebugConfig.DEBUG_PAUSE = true;
            ActivityTaskManagerDebugConfig.DEBUG_RECENTS = true;
            ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS = true;
            ActivityTaskManagerDebugConfig.DEBUG_STACK = true;
            ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE = true;
            ActivityTaskManagerDebugConfig.DEBUG_IDLE = true;
            ActivityTaskManagerDebugConfig.DEBUG_STATES = true;
        }
    }

    public OppoArmyController getOppoArmyController() {
        return this.mOppoArmyController;
    }
}

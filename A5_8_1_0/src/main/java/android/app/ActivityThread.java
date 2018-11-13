package android.app;

import android.R;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.IApplicationThread.Stub;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.app.backup.BackupAgent;
import android.app.job.JobInfo;
import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver.PendingResult;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.IContentProvider;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDebug;
import android.database.sqlite.SQLiteDebug.DbStats;
import android.database.sqlite.SQLiteDebug.PagerStats;
import android.ddm.DdmHandleAppName;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.hardware.display.DisplayManagerGlobal;
import android.media.tv.TvContract;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.Proxy;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.AsyncTask;
import android.os.BaseBundle;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.GraphicsEnvironment;
import android.os.Handler;
import android.os.IBinder;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.provider.FontsContract;
import android.renderscript.RenderScriptCacheDir;
import android.security.NetworkSecurityPolicy;
import android.security.net.config.NetworkSecurityConfigProvider;
import android.util.ArrayMap;
import android.util.BoostFramework;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.util.LogWriter;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.SuperNotCalledException;
import android.view.ContextThemeWrapper;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewManager;
import android.view.ViewRootImpl;
import android.view.ViewRootImpl.ActivityConfigCallback;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.webkit.WebView;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.content.ReferrerIntent;
import com.android.internal.os.BinderInternal;
import com.android.internal.os.RuntimeInit;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.IndentingPrintWriter;
import com.android.org.conscrypt.OpenSSLSocketImpl;
import com.android.org.conscrypt.TrustedCertificateStore;
import com.color.util.ColorDisplayCompatUtils;
import com.color.util.ColorDisplayOptimizationUtils;
import com.color.util.ColorFormatterCompatibilityUtils;
import com.color.util.ColorNavigationBarUtil;
import com.color.util.ColorReflectDataUtils;
import com.oppo.debug.InputLog;
import com.oppo.hypnus.Hypnus;
import com.oppo.hypnus.HypnusManager;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.CloseGuard;
import dalvik.system.VMDebug;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TimeZone;
import libcore.io.DropBox;
import libcore.io.DropBox.Reporter;
import libcore.io.EventLogger;
import libcore.io.IoUtils;
import libcore.net.event.NetworkEventDispatcher;
import org.apache.harmony.dalvik.ddmc.DdmVmInternal;

public final class ActivityThread {
    private static final int ACTIVITY_THREAD_CHECKIN_VERSION = 4;
    private static final boolean DEBUG_BACKUP = false;
    public static boolean DEBUG_BROADCAST = false;
    public static boolean DEBUG_BROADCAST_LIGHT = (DEBUG_BROADCAST);
    public static boolean DEBUG_CONFIGURATION = false;
    private static boolean DEBUG_MEMORY_TRIM = false;
    static boolean DEBUG_MESSAGES = false;
    private static final boolean DEBUG_ORDER = false;
    private static boolean DEBUG_PROVIDER = false;
    private static final boolean DEBUG_RESULTS = false;
    private static boolean DEBUG_SERVICE = false;
    private static final int DONT_REPORT = 2;
    private static final String HEAP_COLUMN = "%13s %8s %8s %8s %8s %8s %8s %8s";
    private static final String HEAP_FULL_COLUMN = "%13s %8s %8s %8s %8s %8s %8s %8s %8s %8s %8s";
    public static final long INVALID_PROC_STATE_SEQ = -1;
    public static final int IN_ENQUEUEING = 1;
    public static final int IN_FINISHING = 3;
    public static final int IN_HANDLING = 2;
    public static final int IN_IDLE = 0;
    private static final int LOG_AM_ON_PAUSE_CALLED = 30021;
    private static final int LOG_AM_ON_RESUME_CALLED = 30022;
    private static final int LOG_AM_ON_STOP_CALLED = 30049;
    private static final long MIN_TIME_BETWEEN_GCS = 5000;
    private static final String ONE_COUNT_COLUMN = "%21s %8d";
    private static final String ONE_COUNT_COLUMN_HEADER = "%21s %8s";
    private static final boolean REPORT_TO_ACTIVITY = true;
    public static final int SERVICE_DONE_EXECUTING_ANON = 0;
    public static final int SERVICE_DONE_EXECUTING_START = 1;
    public static final int SERVICE_DONE_EXECUTING_STOP = 2;
    private static final int SQLITE_MEM_RELEASED_EVENT_LOG_TAG = 75003;
    private static final long STOP_TIMEOUT = 500;
    public static final String TAG = "ActivityThread";
    private static final Config THUMBNAIL_FORMAT = Config.RGB_565;
    private static final String TWO_COUNT_COLUMNS = "%21s %8d %21s %8d";
    private static final int USER_LEAVING = 1;
    static boolean localLOGV = false;
    public static int mBgBrState = 0;
    public static int mFgBrState = 0;
    private static HypnusManager mHM = null;
    public static int mOppoBgBrState = 0;
    public static int mOppoFgBrState = 0;
    private static volatile ActivityThread sCurrentActivityThread;
    private static final ThreadLocal<Intent> sCurrentBroadcastIntent = new ThreadLocal();
    static volatile Handler sMainThreadHandler;
    static volatile IPackageManager sPackageManager;
    private final int enable_uxe = SystemProperties.getInt("iop.enable_uxe", 0);
    final ArrayMap<IBinder, ActivityClientRecord> mActivities = new ArrayMap();
    final ArrayList<Application> mAllApplications = new ArrayList();
    final ApplicationThread mAppThread = new ApplicationThread(this, null);
    private Bitmap mAvailThumbnailBitmap = null;
    final ArrayMap<String, BackupAgent> mBackupAgents = new ArrayMap();
    AppBindData mBoundApplication;
    Configuration mCompatConfiguration;
    Configuration mConfiguration;
    Bundle mCoreSettings = null;
    int mCurDefaultDisplayDpi;
    boolean mDebugOn;
    boolean mDensityCompatMode;
    final GcIdler mGcIdler = new GcIdler();
    boolean mGcIdlerScheduled = false;
    final H mH = new H(this, null);
    Application mInitialApplication;
    Instrumentation mInstrumentation;
    String mInstrumentationAppDir = null;
    String mInstrumentationLibDir = null;
    String mInstrumentationPackageName = null;
    String[] mInstrumentationSplitAppDirs = null;
    String mInstrumentedAppDir = null;
    String mInstrumentedLibDir = null;
    String[] mInstrumentedSplitAppDirs = null;
    boolean mIsDebugTarget = false;
    boolean mJitEnabled = false;
    ArrayList<WeakReference<AssistStructure>> mLastAssistStructures = new ArrayList();
    private int mLastSessionId;
    @GuardedBy("mResourcesManager")
    int mLifecycleSeq = 0;
    final ArrayMap<IBinder, ProviderClientRecord> mLocalProviders = new ArrayMap();
    final ArrayMap<ComponentName, ProviderClientRecord> mLocalProvidersByName = new ArrayMap();
    final Looper mLooper = Looper.myLooper();
    private Configuration mMainThreadConfig = new Configuration();
    @GuardedBy("mNetworkPolicyLock")
    private long mNetworkBlockSeq = -1;
    private final Object mNetworkPolicyLock = new Object();
    ActivityClientRecord mNewActivities = null;
    int mNumVisibleActivities = 0;
    final ArrayMap<Activity, ArrayList<OnActivityPausedListener>> mOnPauseListeners = new ArrayMap();
    @GuardedBy("mResourcesManager")
    final ArrayMap<String, WeakReference<LoadedApk>> mPackages = new ArrayMap();
    @GuardedBy("mResourcesManager")
    Configuration mPendingConfiguration = null;
    Profiler mProfiler;
    final ArrayMap<ProviderKey, ProviderClientRecord> mProviderMap = new ArrayMap();
    final ArrayMap<IBinder, ProviderRefCount> mProviderRefCountMap = new ArrayMap();
    @GuardedBy("mResourcesManager")
    final ArrayList<ActivityClientRecord> mRelaunchingActivities = new ArrayList();
    @GuardedBy("mResourcesManager")
    final ArrayMap<String, WeakReference<LoadedApk>> mResourcePackages = new ArrayMap();
    private final ResourcesManager mResourcesManager = ResourcesManager.getInstance();
    final ArrayMap<IBinder, Service> mServices = new ArrayMap();
    boolean mSomeActivitiesChanged = false;
    private ContextImpl mSystemContext;
    boolean mSystemThread = false;
    private ContextImpl mSystemUiContext;
    private Canvas mThumbnailCanvas = null;
    private int mThumbnailHeight = -1;
    private int mThumbnailWidth = -1;
    boolean mUpdatingSystemConfig = false;

    static final class ActivityClientRecord {
        Activity activity;
        ActivityInfo activityInfo;
        CompatibilityInfo compatInfo;
        ActivityConfigCallback configCallback = new -$Lambda$9I5WEMsoBc7l4QrNqZ4wx59yuHU(this);
        Configuration createdConfig;
        String embeddedID = null;
        boolean hideForNow = false;
        int ident;
        Intent intent;
        boolean isForward;
        NonConfigurationInstances lastNonConfigurationInstances;
        int lastProcessedSeq = 0;
        Window mPendingRemoveWindow;
        WindowManager mPendingRemoveWindowManager;
        boolean mPreserveWindow;
        boolean needInvisible;
        Configuration newConfig;
        ActivityClientRecord nextIdle = null;
        boolean onlyLocalRequest;
        Configuration overrideConfig;
        LoadedApk packageInfo;
        Activity parent = null;
        boolean paused = false;
        int pendingConfigChanges;
        List<ReferrerIntent> pendingIntents;
        List<ResultInfo> pendingResults;
        PersistableBundle persistentState;
        ProfilerInfo profilerInfo;
        String referrer;
        int relaunchSeq = 0;
        boolean startsNotResumed;
        Bundle state;
        boolean stopped = false;
        private Configuration tmpConfig = new Configuration();
        IBinder token;
        IVoiceInteractor voiceInteractor;
        Window window;

        ActivityClientRecord() {
        }

        /* renamed from: lambda$-android_app_ActivityThread$ActivityClientRecord_17734 */
        /* synthetic */ void m5lambda$-android_app_ActivityThread$ActivityClientRecord_17734(Configuration overrideConfig, int newDisplayId) {
            if (this.activity == null) {
                throw new IllegalStateException("Received config update for non-existing activity");
            }
            this.activity.mMainThread.handleActivityConfigurationChanged(new ActivityConfigChangeData(this.token, overrideConfig), newDisplayId);
        }

        public boolean isPreHoneycomb() {
            boolean z = false;
            if (this.activity == null) {
                return false;
            }
            if (this.activity.getApplicationInfo().targetSdkVersion < 11) {
                z = true;
            }
            return z;
        }

        public boolean isPersistable() {
            return this.activityInfo.persistableMode == 2;
        }

        public String toString() {
            ComponentName componentName = this.intent != null ? this.intent.getComponent() : null;
            return "ActivityRecord{" + Integer.toHexString(System.identityHashCode(this)) + " token=" + this.token + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + (componentName == null ? "no component name" : componentName.toShortString()) + "}";
        }

        public String getStateString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ActivityClientRecord{");
            sb.append("paused=").append(this.paused);
            sb.append(", stopped=").append(this.stopped);
            sb.append(", hideForNow=").append(this.hideForNow);
            sb.append(", startsNotResumed=").append(this.startsNotResumed);
            sb.append(", isForward=").append(this.isForward);
            sb.append(", pendingConfigChanges=").append(this.pendingConfigChanges);
            sb.append(", onlyLocalRequest=").append(this.onlyLocalRequest);
            sb.append(", preserveWindow=").append(this.mPreserveWindow);
            if (this.activity != null) {
                sb.append(", Activity{");
                sb.append("resumed=").append(this.activity.mResumed);
                sb.append(", stopped=").append(this.activity.mStopped);
                sb.append(", finished=").append(this.activity.isFinishing());
                sb.append(", destroyed=").append(this.activity.isDestroyed());
                sb.append(", startedActivity=").append(this.activity.mStartedActivity);
                sb.append(", temporaryPause=").append(this.activity.mTemporaryPause);
                sb.append(", changingConfigurations=").append(this.activity.mChangingConfigurations);
                sb.append("}");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    static final class ActivityConfigChangeData {
        final IBinder activityToken;
        final Configuration overrideConfig;

        public ActivityConfigChangeData(IBinder token, Configuration config) {
            this.activityToken = token;
            this.overrideConfig = config;
        }
    }

    static final class AppBindData {
        ApplicationInfo appInfo;
        String buildSerial;
        CompatibilityInfo compatInfo;
        Configuration config;
        int debugMode;
        boolean enableBinderTracking;
        LoadedApk info;
        ProfilerInfo initProfilerInfo;
        Bundle instrumentationArgs;
        ComponentName instrumentationName;
        IUiAutomationConnection instrumentationUiAutomationConnection;
        IInstrumentationWatcher instrumentationWatcher;
        boolean persistent;
        String processName;
        List<ProviderInfo> providers;
        boolean restrictedBackupMode;
        boolean trackAllocation;

        AppBindData() {
        }

        public String toString() {
            return "AppBindData{appInfo=" + this.appInfo + "}";
        }
    }

    private class ApplicationThread extends Stub {
        private static final String DB_INFO_FORMAT = "  %8s %8s %14s %14s  %s";
        private int mLastProcessState;

        /* synthetic */ ApplicationThread(ActivityThread this$0, ApplicationThread -this1) {
            this();
        }

        private ApplicationThread() {
            this.mLastProcessState = -1;
        }

        private void updatePendingConfiguration(Configuration config) {
            synchronized (ActivityThread.this.mResourcesManager) {
                if (ActivityThread.this.mPendingConfiguration == null || ActivityThread.this.mPendingConfiguration.isOtherSeqNewer(config)) {
                    ActivityThread.this.mPendingConfiguration = config;
                }
            }
        }

        public final void schedulePauseActivity(IBinder token, boolean finished, boolean userLeaving, int configChanges, boolean dontReport) {
            int i;
            int i2 = 0;
            int seq = ActivityThread.this.getLifecycleSeq();
            ActivityThread activityThread = ActivityThread.this;
            int i3 = finished ? 102 : 101;
            if (userLeaving) {
                i = 1;
            } else {
                i = 0;
            }
            if (dontReport) {
                i2 = 2;
            }
            activityThread.sendMessage(i3, (Object) token, i | i2, configChanges, seq);
        }

        public final void scheduleStopActivity(IBinder token, boolean showWindow, int configChanges) {
            int seq = ActivityThread.this.getLifecycleSeq();
            ActivityClientRecord r = (ActivityClientRecord) ActivityThread.this.mActivities.get(token);
            if (r != null) {
                r.needInvisible = true;
            }
            ActivityThread.this.sendMessage(showWindow ? 103 : 104, (Object) token, 0, configChanges, seq);
        }

        public final void scheduleWindowVisibility(IBinder token, boolean showWindow) {
            ActivityThread.this.sendMessage(showWindow ? 105 : 106, token);
        }

        public final void scheduleSleeping(IBinder token, boolean sleeping) {
            ActivityThread.this.sendMessage(137, token, sleeping ? 1 : 0);
        }

        public final void scheduleResumeActivity(IBinder token, int processState, boolean isForward, Bundle resumeArgs) {
            int i;
            int seq = ActivityThread.this.getLifecycleSeq();
            updateProcessState(processState, false);
            ActivityClientRecord r = (ActivityClientRecord) ActivityThread.this.mActivities.get(token);
            if (r != null) {
                r.needInvisible = false;
            }
            ActivityThread activityThread = ActivityThread.this;
            if (isForward) {
                i = 1;
            } else {
                i = 0;
            }
            activityThread.sendMessage(107, (Object) token, i, 0, seq);
        }

        public final void scheduleSendResult(IBinder token, List<ResultInfo> results) {
            ResultData res = new ResultData();
            res.token = token;
            res.results = results;
            ActivityThread.this.sendMessage(108, res);
        }

        public final void scheduleLaunchActivity(Intent intent, IBinder token, int ident, ActivityInfo info, Configuration curConfig, Configuration overrideConfig, CompatibilityInfo compatInfo, String referrer, IVoiceInteractor voiceInteractor, int procState, Bundle state, PersistableBundle persistentState, List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents, boolean notResumed, boolean isForward, ProfilerInfo profilerInfo) {
            updateProcessState(procState, false);
            ActivityClientRecord r = new ActivityClientRecord();
            r.token = token;
            r.ident = ident;
            r.intent = intent;
            r.referrer = referrer;
            r.voiceInteractor = voiceInteractor;
            r.activityInfo = info;
            r.compatInfo = compatInfo;
            r.state = state;
            r.persistentState = persistentState;
            r.pendingResults = pendingResults;
            r.pendingIntents = pendingNewIntents;
            r.startsNotResumed = notResumed;
            r.isForward = isForward;
            r.profilerInfo = profilerInfo;
            r.overrideConfig = overrideConfig;
            updatePendingConfiguration(curConfig);
            ActivityThread.this.sendMessage(100, r);
        }

        public final void scheduleRelaunchActivity(IBinder token, List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents, int configChanges, boolean notResumed, Configuration config, Configuration overrideConfig, boolean preserveWindow) {
            ActivityThread.this.requestRelaunchActivity(token, pendingResults, pendingNewIntents, configChanges, notResumed, config, overrideConfig, true, preserveWindow);
        }

        public final void scheduleNewIntent(List<ReferrerIntent> intents, IBinder token, boolean andPause) {
            NewIntentData data = new NewIntentData();
            data.intents = intents;
            data.token = token;
            data.andPause = andPause;
            ActivityThread.this.sendMessage(112, data);
        }

        public final void scheduleDestroyActivity(IBinder token, boolean finishing, int configChanges) {
            ActivityThread.this.sendMessage(109, token, finishing ? 1 : 0, configChanges);
        }

        public final void scheduleReceiver(Intent intent, ActivityInfo info, CompatibilityInfo compatInfo, int resultCode, String data, Bundle extras, boolean sync, int sendingUser, int processState, int hasCode) {
            updateProcessState(processState, false);
            ReceiverData r = new ReceiverData(intent, resultCode, data, extras, sync, false, ActivityThread.this.mAppThread.asBinder(), sendingUser, hasCode);
            r.info = info;
            r.compatInfo = compatInfo;
            if (sync) {
                r.setBroadcastState(intent.getFlags(), 1);
            }
            if (ActivityThread.DEBUG_BROADCAST_LIGHT) {
                Slog.v(ActivityThread.TAG, "scheduleReceiver info = " + info + " intent = " + intent + " sync = " + sync + " hasCode = " + hasCode);
            }
            ActivityThread.this.sendMessage(113, r);
        }

        public final void scheduleCreateBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo, int backupMode) {
            CreateBackupAgentData d = new CreateBackupAgentData();
            d.appInfo = app;
            d.compatInfo = compatInfo;
            d.backupMode = backupMode;
            ActivityThread.this.sendMessage(128, d);
        }

        public final void scheduleDestroyBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo) {
            CreateBackupAgentData d = new CreateBackupAgentData();
            d.appInfo = app;
            d.compatInfo = compatInfo;
            ActivityThread.this.sendMessage(129, d);
        }

        public final void scheduleCreateService(IBinder token, ServiceInfo info, CompatibilityInfo compatInfo, int processState) {
            updateProcessState(processState, false);
            CreateServiceData s = new CreateServiceData();
            s.token = token;
            s.info = info;
            s.compatInfo = compatInfo;
            ActivityThread.this.sendMessage(114, s);
        }

        public final void scheduleBindService(IBinder token, Intent intent, boolean rebind, int processState) {
            updateProcessState(processState, false);
            BindServiceData s = new BindServiceData();
            s.token = token;
            s.intent = intent;
            s.rebind = rebind;
            if (ActivityThread.DEBUG_SERVICE) {
                Slog.v(ActivityThread.TAG, "scheduleBindService token=" + token + " intent=" + intent + " uid=" + Binder.getCallingUid() + " pid=" + Binder.getCallingPid());
            }
            ActivityThread.this.sendMessage(121, s);
        }

        public final void scheduleUnbindService(IBinder token, Intent intent) {
            BindServiceData s = new BindServiceData();
            s.token = token;
            s.intent = intent;
            ActivityThread.this.sendMessage(122, s);
        }

        public final void scheduleServiceArgs(IBinder token, ParceledListSlice args) {
            List<ServiceStartArgs> list = args.getList();
            for (int i = 0; i < list.size(); i++) {
                ServiceStartArgs ssa = (ServiceStartArgs) list.get(i);
                ServiceArgsData s = new ServiceArgsData();
                s.token = token;
                s.taskRemoved = ssa.taskRemoved;
                s.startId = ssa.startId;
                s.flags = ssa.flags;
                s.args = ssa.args;
                ActivityThread.this.sendMessage(115, s);
            }
        }

        public final void scheduleStopService(IBinder token) {
            ActivityThread.this.sendMessage(116, token);
        }

        public final void bindApplication(String processName, ApplicationInfo appInfo, List<ProviderInfo> providers, ComponentName instrumentationName, ProfilerInfo profilerInfo, Bundle instrumentationArgs, IInstrumentationWatcher instrumentationWatcher, IUiAutomationConnection instrumentationUiConnection, int debugMode, boolean enableBinderTracking, boolean trackAllocation, boolean isRestrictedBackupMode, boolean persistent, Configuration config, CompatibilityInfo compatInfo, Map services, Bundle coreSettings, String buildSerial) {
            if (services != null) {
                ServiceManager.initServiceCache(services);
            }
            setCoreSettings(coreSettings);
            AppBindData data = new AppBindData();
            data.processName = processName;
            data.appInfo = appInfo;
            data.providers = providers;
            data.instrumentationName = instrumentationName;
            data.instrumentationArgs = instrumentationArgs;
            data.instrumentationWatcher = instrumentationWatcher;
            data.instrumentationUiAutomationConnection = instrumentationUiConnection;
            data.debugMode = debugMode;
            data.enableBinderTracking = enableBinderTracking;
            data.trackAllocation = trackAllocation;
            data.restrictedBackupMode = isRestrictedBackupMode;
            data.persistent = persistent;
            data.config = config;
            data.compatInfo = compatInfo;
            data.initProfilerInfo = profilerInfo;
            data.buildSerial = buildSerial;
            ActivityThread.this.sendMessage(110, data);
        }

        public final void scheduleExit() {
            ActivityThread.this.sendMessage(111, null);
        }

        public final void scheduleSuicide() {
            ActivityThread.this.sendMessage(130, null);
        }

        public void scheduleConfigurationChanged(Configuration config) {
            updatePendingConfiguration(config);
            ActivityThread.this.sendMessage(118, config);
        }

        public void scheduleApplicationInfoChanged(ApplicationInfo ai) {
            ActivityThread.this.sendMessage(156, ai);
        }

        public void updateTimeZone() {
            TimeZone.setDefault(null);
        }

        public void clearDnsCache() {
            InetAddress.clearDnsCache();
            NetworkEventDispatcher.getInstance().onNetworkConfigurationChanged();
        }

        public void setHttpProxy(String host, String port, String exclList, Uri pacFileUrl) {
            ConnectivityManager cm = ConnectivityManager.from(ActivityThread.this.getSystemContext());
            if (cm.getBoundNetworkForProcess() != null) {
                Proxy.setHttpProxySystemProperty(cm.getDefaultProxy());
            } else {
                Proxy.setHttpProxySystemProperty(host, port, exclList, pacFileUrl);
            }
        }

        public void processInBackground() {
            ActivityThread.this.mH.removeMessages(120);
            ActivityThread.this.mH.sendMessage(ActivityThread.this.mH.obtainMessage(120));
        }

        public void dumpService(ParcelFileDescriptor pfd, IBinder servicetoken, String[] args) {
            DumpComponentInfo data = new DumpComponentInfo();
            try {
                data.fd = pfd.dup();
                data.token = servicetoken;
                data.args = args;
                ActivityThread.this.sendMessage(123, (Object) data, 0, 0, true);
            } catch (IOException e) {
                Slog.w(ActivityThread.TAG, "dumpService failed", e);
            } finally {
                IoUtils.closeQuietly(pfd);
            }
        }

        public void scheduleRegisteredReceiver(IIntentReceiver receiver, Intent intent, int resultCode, String dataStr, Bundle extras, boolean ordered, boolean sticky, int sendingUser, int processState) throws RemoteException {
            updateProcessState(processState, false);
            if (ActivityThread.DEBUG_BROADCAST_LIGHT) {
                Slog.v(ActivityThread.TAG, "scheduleRegisteredReceiver receiver= " + receiver + " intent= " + intent + " ordered= " + ordered + " sticky = " + sticky);
            }
            receiver.performReceive(intent, resultCode, dataStr, extras, ordered, sticky, sendingUser);
        }

        public void scheduleLowMemory() {
            ActivityThread.this.sendMessage(124, null);
        }

        public void scheduleActivityConfigurationChanged(IBinder token, Configuration overrideConfig) {
            ActivityThread.this.sendMessage(125, new ActivityConfigChangeData(token, overrideConfig));
        }

        public void scheduleActivityMovedToDisplay(IBinder token, int displayId, Configuration overrideConfig) {
            ActivityThread.this.sendMessage(157, new ActivityConfigChangeData(token, overrideConfig), displayId);
        }

        public void profilerControl(boolean start, ProfilerInfo profilerInfo, int profileType) {
            ActivityThread.this.sendMessage(127, profilerInfo, start ? 1 : 0, profileType);
        }

        public void dumpHeap(boolean managed, boolean mallocInfo, boolean runGc, String path, ParcelFileDescriptor fd) {
            DumpHeapData dhd = new DumpHeapData();
            dhd.managed = managed;
            dhd.mallocInfo = mallocInfo;
            dhd.runGc = runGc;
            dhd.path = path;
            dhd.fd = fd;
            ActivityThread.this.sendMessage(135, (Object) dhd, 0, 0, true);
        }

        public void attachAgent(String agent) {
            ActivityThread.this.sendMessage(155, agent);
        }

        public void setSchedulingGroup(int group) {
            try {
                Process.setProcessGroup(Process.myPid(), group);
            } catch (Exception e) {
                Slog.w(ActivityThread.TAG, "Failed setting process group to " + group, e);
            }
        }

        public void dispatchPackageBroadcast(int cmd, String[] packages) {
            ActivityThread.this.sendMessage(133, packages, cmd);
        }

        public void scheduleCrash(String msg) {
            ActivityThread.this.sendMessage(134, msg);
        }

        public void dumpActivity(ParcelFileDescriptor pfd, IBinder activitytoken, String prefix, String[] args) {
            DumpComponentInfo data = new DumpComponentInfo();
            try {
                data.fd = pfd.dup();
                data.token = activitytoken;
                data.prefix = prefix;
                data.args = args;
                ActivityThread.this.sendMessage(136, (Object) data, 0, 0, true);
            } catch (IOException e) {
                Slog.w(ActivityThread.TAG, "dumpActivity failed", e);
            } finally {
                IoUtils.closeQuietly(pfd);
            }
        }

        public void dumpProvider(ParcelFileDescriptor pfd, IBinder providertoken, String[] args) {
            DumpComponentInfo data = new DumpComponentInfo();
            try {
                data.fd = pfd.dup();
                data.token = providertoken;
                data.args = args;
                ActivityThread.this.sendMessage(141, (Object) data, 0, 0, true);
            } catch (IOException e) {
                Slog.w(ActivityThread.TAG, "dumpProvider failed", e);
            } finally {
                IoUtils.closeQuietly(pfd);
            }
        }

        public void dumpMemInfo(ParcelFileDescriptor pfd, MemoryInfo mem, boolean checkin, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) {
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(pfd.getFileDescriptor()));
            try {
                dumpMemInfo(pw, mem, checkin, dumpFullInfo, dumpDalvik, dumpSummaryOnly, dumpUnreachable);
            } finally {
                pw.flush();
                IoUtils.closeQuietly(pfd);
            }
        }

        private void dumpMemInfo(PrintWriter pw, MemoryInfo memInfo, boolean checkin, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable) {
            long nativeMax = Debug.getNativeHeapSize() / 1024;
            long nativeAllocated = Debug.getNativeHeapAllocatedSize() / 1024;
            long nativeFree = Debug.getNativeHeapFreeSize() / 1024;
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long dalvikMax = runtime.totalMemory() / 1024;
            long dalvikFree = runtime.freeMemory() / 1024;
            long dalvikAllocated = dalvikMax - dalvikFree;
            long[] instanceCounts = VMDebug.countInstancesOfClasses(new Class[]{ContextImpl.class, Activity.class, WebView.class, OpenSSLSocketImpl.class}, true);
            long appContextInstanceCount = instanceCounts[0];
            long activityInstanceCount = instanceCounts[1];
            long webviewInstanceCount = instanceCounts[2];
            long openSslSocketCount = instanceCounts[3];
            long viewInstanceCount = ViewDebug.getViewInstanceCount();
            long viewRootInstanceCount = ViewDebug.getViewRootImplCount();
            int globalAssetCount = AssetManager.getGlobalAssetCount();
            int globalAssetManagerCount = AssetManager.getGlobalAssetManagerCount();
            int binderLocalObjectCount = Debug.getBinderLocalObjectCount();
            int binderProxyObjectCount = Debug.getBinderProxyObjectCount();
            int binderDeathObjectCount = Debug.getBinderDeathObjectCount();
            long parcelSize = Parcel.getGlobalAllocSize();
            long parcelCount = Parcel.getGlobalAllocCount();
            PagerStats stats = SQLiteDebug.getDatabaseInfo();
            ActivityThread.dumpMemInfoTable(pw, memInfo, checkin, dumpFullInfo, dumpDalvik, dumpSummaryOnly, Process.myPid(), ActivityThread.this.mBoundApplication != null ? ActivityThread.this.mBoundApplication.processName : "unknown", nativeMax, nativeAllocated, nativeFree, dalvikMax, dalvikAllocated, dalvikFree);
            int i;
            DbStats dbStats;
            if (checkin) {
                pw.print(viewInstanceCount);
                pw.print(',');
                pw.print(viewRootInstanceCount);
                pw.print(',');
                pw.print(appContextInstanceCount);
                pw.print(',');
                pw.print(activityInstanceCount);
                pw.print(',');
                pw.print(globalAssetCount);
                pw.print(',');
                pw.print(globalAssetManagerCount);
                pw.print(',');
                pw.print(binderLocalObjectCount);
                pw.print(',');
                pw.print(binderProxyObjectCount);
                pw.print(',');
                pw.print(binderDeathObjectCount);
                pw.print(',');
                pw.print(openSslSocketCount);
                pw.print(',');
                pw.print(stats.memoryUsed / 1024);
                pw.print(',');
                pw.print(stats.memoryUsed / 1024);
                pw.print(',');
                pw.print(stats.pageCacheOverflow / 1024);
                pw.print(',');
                pw.print(stats.largestMemAlloc / 1024);
                for (i = 0; i < stats.dbStats.size(); i++) {
                    dbStats = (DbStats) stats.dbStats.get(i);
                    pw.print(',');
                    pw.print(dbStats.dbName);
                    pw.print(',');
                    pw.print(dbStats.pageSize);
                    pw.print(',');
                    pw.print(dbStats.dbSize);
                    pw.print(',');
                    pw.print(dbStats.lookaside);
                    pw.print(',');
                    pw.print(dbStats.cache);
                    pw.print(',');
                    pw.print(dbStats.cache);
                }
                pw.println();
                return;
            }
            pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            pw.println(" Objects");
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "Views:", Long.valueOf(viewInstanceCount), "ViewRootImpl:", Long.valueOf(viewRootInstanceCount));
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "AppContexts:", Long.valueOf(appContextInstanceCount), "Activities:", Long.valueOf(activityInstanceCount));
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "Assets:", Integer.valueOf(globalAssetCount), "AssetManagers:", Integer.valueOf(globalAssetManagerCount));
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "Local Binders:", Integer.valueOf(binderLocalObjectCount), "Proxy Binders:", Integer.valueOf(binderProxyObjectCount));
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "Parcel memory:", Long.valueOf(parcelSize / 1024), "Parcel count:", Long.valueOf(parcelCount));
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "Death Recipients:", Integer.valueOf(binderDeathObjectCount), "OpenSSL Sockets:", Long.valueOf(openSslSocketCount));
            ActivityThread.printRow(pw, ActivityThread.ONE_COUNT_COLUMN, "WebViews:", Long.valueOf(webviewInstanceCount));
            pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            pw.println(" SQL");
            ActivityThread.printRow(pw, ActivityThread.ONE_COUNT_COLUMN, "MEMORY_USED:", Integer.valueOf(stats.memoryUsed / 1024));
            ActivityThread.printRow(pw, ActivityThread.TWO_COUNT_COLUMNS, "PAGECACHE_OVERFLOW:", Integer.valueOf(stats.pageCacheOverflow / 1024), "MALLOC_SIZE:", Integer.valueOf(stats.largestMemAlloc / 1024));
            pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            int N = stats.dbStats.size();
            if (N > 0) {
                pw.println(" DATABASES");
                ActivityThread.printRow(pw, DB_INFO_FORMAT, "pgsz", "dbsz", "Lookaside(b)", "cache", "Dbname");
                for (i = 0; i < N; i++) {
                    dbStats = (DbStats) stats.dbStats.get(i);
                    String str = DB_INFO_FORMAT;
                    Object[] objArr = new Object[5];
                    objArr[0] = dbStats.pageSize > 0 ? String.valueOf(dbStats.pageSize) : WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
                    objArr[1] = dbStats.dbSize > 0 ? String.valueOf(dbStats.dbSize) : WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
                    objArr[2] = dbStats.lookaside > 0 ? String.valueOf(dbStats.lookaside) : WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
                    objArr[3] = dbStats.cache;
                    objArr[4] = dbStats.dbName;
                    ActivityThread.printRow(pw, str, objArr);
                }
            }
            String assetAlloc = AssetManager.getAssetAllocations();
            if (assetAlloc != null) {
                pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                pw.println(" Asset Allocations");
                pw.print(assetAlloc);
            }
            if (dumpUnreachable) {
                boolean showContents;
                if (ActivityThread.this.mBoundApplication == null || (ActivityThread.this.mBoundApplication.appInfo.flags & 2) == 0) {
                    showContents = Build.IS_DEBUGGABLE;
                } else {
                    showContents = true;
                }
                pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                pw.println(" Unreachable memory");
                pw.print(Debug.getUnreachableMemory(100, showContents));
            }
        }

        public void dumpGfxInfo(ParcelFileDescriptor pfd, String[] args) {
            ActivityThread.this.nDumpGraphicsInfo(pfd.getFileDescriptor());
            WindowManagerGlobal.getInstance().dumpGfxInfo(pfd.getFileDescriptor(), args);
            IoUtils.closeQuietly(pfd);
        }

        private void dumpDatabaseInfo(ParcelFileDescriptor pfd, String[] args) {
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(pfd.getFileDescriptor()));
            SQLiteDebug.dump(new PrintWriterPrinter(pw), args);
            pw.flush();
        }

        public void dumpDbInfo(ParcelFileDescriptor pfd, final String[] args) {
            if (ActivityThread.this.mSystemThread) {
                try {
                    ParcelFileDescriptor dup = pfd.dup();
                    AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        public void run() {
                            try {
                                ApplicationThread.this.dumpDatabaseInfo(dup, args);
                            } finally {
                                IoUtils.closeQuietly(dup);
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.w(ActivityThread.TAG, "Could not dup FD " + pfd.getFileDescriptor().getInt$());
                } finally {
                    IoUtils.closeQuietly(pfd);
                }
            } else {
                dumpDatabaseInfo(pfd, args);
                IoUtils.closeQuietly(pfd);
            }
        }

        public void unstableProviderDied(IBinder provider) {
            ActivityThread.this.sendMessage(142, provider);
        }

        public void requestAssistContextExtras(IBinder activityToken, IBinder requestToken, int requestType, int sessionId, int flags) {
            RequestAssistContextExtras cmd = new RequestAssistContextExtras();
            cmd.activityToken = activityToken;
            cmd.requestToken = requestToken;
            cmd.requestType = requestType;
            cmd.sessionId = sessionId;
            cmd.flags = flags;
            ActivityThread.this.sendMessage(143, cmd);
        }

        public void setCoreSettings(Bundle coreSettings) {
            ActivityThread.this.sendMessage(138, coreSettings);
        }

        public void updatePackageCompatibilityInfo(String pkg, CompatibilityInfo info) {
            UpdateCompatibilityData ucd = new UpdateCompatibilityData();
            ucd.pkg = pkg;
            ucd.info = info;
            ActivityThread.this.sendMessage(139, ucd);
        }

        public void scheduleTrimMemory(int level) {
            ActivityThread.this.sendMessage(140, null, level);
        }

        public void scheduleTranslucentConversionComplete(IBinder token, boolean drawComplete) {
            ActivityThread.this.sendMessage(144, token, drawComplete ? 1 : 0);
        }

        public void scheduleOnNewActivityOptions(IBinder token, Bundle options) {
            ActivityThread.this.sendMessage(146, new Pair(token, ActivityOptions.fromBundle(options)));
        }

        public void setProcessState(int state) {
            updateProcessState(state, true);
        }

        public void updateProcessState(int processState, boolean fromIpc) {
            synchronized (this) {
                if (this.mLastProcessState != processState) {
                    this.mLastProcessState = processState;
                    int dalvikProcessState = 1;
                    if (processState <= 6) {
                        dalvikProcessState = 0;
                    }
                    VMRuntime.getRuntime().updateProcessState(dalvikProcessState);
                }
            }
        }

        public void setNetworkBlockSeq(long procStateSeq) {
            synchronized (ActivityThread.this.mNetworkPolicyLock) {
                ActivityThread.this.mNetworkBlockSeq = procStateSeq;
            }
        }

        public void scheduleInstallProvider(ProviderInfo provider) {
            ActivityThread.this.sendMessage(145, provider);
        }

        public final void updateTimePrefs(int timeFormatPreference) {
            Boolean timeFormatPreferenceBool;
            if (timeFormatPreference == 0) {
                timeFormatPreferenceBool = Boolean.FALSE;
            } else if (timeFormatPreference == 1) {
                timeFormatPreferenceBool = Boolean.TRUE;
            } else {
                timeFormatPreferenceBool = null;
            }
            DateFormat.set24HourTimePref(timeFormatPreferenceBool);
        }

        public void scheduleEnterAnimationComplete(IBinder token) {
            ActivityThread.this.sendMessage(149, token);
        }

        public void notifyCleartextNetwork(byte[] firstPacket) {
            if (StrictMode.vmCleartextNetworkEnabled()) {
                StrictMode.onCleartextNetworkDetected(firstPacket);
            }
        }

        public void startBinderTracking() {
            ActivityThread.this.sendMessage(150, null);
        }

        public void stopBinderTrackingAndDump(ParcelFileDescriptor pfd) {
            try {
                ActivityThread.this.sendMessage(151, pfd.dup());
            } catch (IOException e) {
            } finally {
                IoUtils.closeQuietly(pfd);
            }
        }

        public void scheduleMultiWindowModeChanged(IBinder token, boolean isInMultiWindowMode, Configuration overrideConfig) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = token;
            args.arg2 = overrideConfig;
            args.argi1 = isInMultiWindowMode ? 1 : 0;
            ActivityThread.this.sendMessage(152, args);
        }

        public void schedulePictureInPictureModeChanged(IBinder token, boolean isInPipMode, Configuration overrideConfig) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = token;
            args.arg2 = overrideConfig;
            args.argi1 = isInPipMode ? 1 : 0;
            ActivityThread.this.sendMessage(153, args);
        }

        public void scheduleLocalVoiceInteractionStarted(IBinder token, IVoiceInteractor voiceInteractor) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = token;
            args.arg2 = voiceInteractor;
            ActivityThread.this.sendMessage(154, args);
        }

        public void openActivityLog(boolean on) {
            Slog.v(ActivityThread.TAG, "openActivityLog on " + on);
            if (on) {
                ActivityThread.localLOGV = true;
                ActivityThread.DEBUG_BROADCAST = true;
                ActivityThread.DEBUG_SERVICE = true;
                ActivityThread.DEBUG_MESSAGES = true;
                ActivityThread.DEBUG_MEMORY_TRIM = true;
                ActivityThread.DEBUG_BROADCAST_LIGHT = true;
                ActivityThread.DEBUG_CONFIGURATION = true;
                ActivityThread.DEBUG_PROVIDER = true;
                return;
            }
            ActivityThread.localLOGV = false;
            ActivityThread.DEBUG_BROADCAST = false;
            ActivityThread.DEBUG_SERVICE = false;
            ActivityThread.DEBUG_MESSAGES = false;
            ActivityThread.DEBUG_MEMORY_TRIM = false;
            ActivityThread.DEBUG_BROADCAST_LIGHT = false;
            ActivityThread.DEBUG_CONFIGURATION = false;
            ActivityThread.DEBUG_PROVIDER = false;
        }

        public void getBroadcastState(int flag) {
            int state;
            if ((524288 & flag) != 0) {
                if ((flag & 268435456) != 0) {
                    Slog.v(ActivityThread.TAG, "getBroadcastState mOppoFgBrState " + ActivityThread.mOppoFgBrState);
                    state = ActivityThread.mOppoFgBrState;
                } else {
                    Slog.v(ActivityThread.TAG, "getBroadcastState mOppoBgBrState " + ActivityThread.mOppoBgBrState);
                    state = ActivityThread.mOppoBgBrState;
                }
            } else if ((flag & 268435456) != 0) {
                Slog.v(ActivityThread.TAG, "getBroadcastState mFgBrState " + ActivityThread.mFgBrState);
                state = ActivityThread.mFgBrState;
            } else {
                Slog.v(ActivityThread.TAG, "getBroadcastState mBgBrState " + ActivityThread.mBgBrState);
                state = ActivityThread.mBgBrState;
            }
            if (state == 1) {
                try {
                    Looper.getMainLooper().getQueue().dumpMessage();
                } catch (Exception e) {
                    Log.e(ActivityThread.TAG, "Failure dump msg " + e);
                }
            }
        }

        public void handleTrustStorageUpdate() {
            NetworkSecurityPolicy.getInstance().handleTrustStorageUpdate();
        }
    }

    static final class BindServiceData {
        Intent intent;
        boolean rebind;
        IBinder token;

        BindServiceData() {
        }

        public String toString() {
            return "BindServiceData{token=" + this.token + " intent=" + this.intent + "}";
        }
    }

    static final class ContextCleanupInfo {
        ContextImpl context;
        String what;
        String who;

        ContextCleanupInfo() {
        }
    }

    static final class CreateBackupAgentData {
        ApplicationInfo appInfo;
        int backupMode;
        CompatibilityInfo compatInfo;

        CreateBackupAgentData() {
        }

        public String toString() {
            return "CreateBackupAgentData{appInfo=" + this.appInfo + " backupAgent=" + this.appInfo.backupAgentName + " mode=" + this.backupMode + "}";
        }
    }

    static final class CreateServiceData {
        CompatibilityInfo compatInfo;
        ServiceInfo info;
        Intent intent;
        IBinder token;

        CreateServiceData() {
        }

        public String toString() {
            return "CreateServiceData{token=" + this.token + " className=" + this.info.name + " packageName=" + this.info.packageName + " intent=" + this.intent + "}";
        }
    }

    private class DropBoxReporter implements Reporter {
        private DropBoxManager dropBox;

        public void addData(String tag, byte[] data, int flags) {
            ensureInitialized();
            this.dropBox.addData(tag, data, flags);
        }

        public void addText(String tag, String data) {
            ensureInitialized();
            this.dropBox.addText(tag, data);
        }

        private synchronized void ensureInitialized() {
            if (this.dropBox == null) {
                this.dropBox = (DropBoxManager) ActivityThread.this.getSystemContext().getSystemService(Context.DROPBOX_SERVICE);
            }
        }
    }

    static final class DumpComponentInfo {
        String[] args;
        ParcelFileDescriptor fd;
        String prefix;
        IBinder token;

        DumpComponentInfo() {
        }
    }

    static final class DumpHeapData {
        ParcelFileDescriptor fd;
        public boolean mallocInfo;
        public boolean managed;
        String path;
        public boolean runGc;

        DumpHeapData() {
        }
    }

    private static class EventLoggingReporter implements EventLogger.Reporter {
        /* synthetic */ EventLoggingReporter(EventLoggingReporter -this0) {
            this();
        }

        private EventLoggingReporter() {
        }

        public void report(int code, Object... list) {
            EventLog.writeEvent(code, list);
        }
    }

    final class GcIdler implements IdleHandler {
        GcIdler() {
        }

        public final boolean queueIdle() {
            ActivityThread.this.doGcIfNeeded();
            return false;
        }
    }

    private class H extends Handler {
        public static final int ACTIVITY_CONFIGURATION_CHANGED = 125;
        public static final int ACTIVITY_MOVED_TO_DISPLAY = 157;
        public static final int APPLICATION_INFO_CHANGED = 156;
        public static final int ATTACH_AGENT = 155;
        public static final int BIND_APPLICATION = 110;
        public static final int BIND_SERVICE = 121;
        public static final int CLEAN_UP_CONTEXT = 119;
        public static final int CONFIGURATION_CHANGED = 118;
        public static final int CREATE_BACKUP_AGENT = 128;
        public static final int CREATE_SERVICE = 114;
        public static final int DESTROY_ACTIVITY = 109;
        public static final int DESTROY_BACKUP_AGENT = 129;
        public static final int DISPATCH_PACKAGE_BROADCAST = 133;
        public static final int DUMP_ACTIVITY = 136;
        public static final int DUMP_HEAP = 135;
        public static final int DUMP_PROVIDER = 141;
        public static final int DUMP_SERVICE = 123;
        public static final int ENABLE_JIT = 132;
        public static final int ENTER_ANIMATION_COMPLETE = 149;
        public static final int EXIT_APPLICATION = 111;
        public static final int GC_WHEN_IDLE = 120;
        public static final int HIDE_WINDOW = 106;
        public static final int INSTALL_PROVIDER = 145;
        public static final int LAUNCH_ACTIVITY = 100;
        public static final int LOCAL_VOICE_INTERACTION_STARTED = 154;
        public static final int LOW_MEMORY = 124;
        public static final int MULTI_WINDOW_MODE_CHANGED = 152;
        public static final int NEW_INTENT = 112;
        public static final int ON_NEW_ACTIVITY_OPTIONS = 146;
        public static final int PAUSE_ACTIVITY = 101;
        public static final int PAUSE_ACTIVITY_FINISHING = 102;
        public static final int PICTURE_IN_PICTURE_MODE_CHANGED = 153;
        public static final int PROFILER_CONTROL = 127;
        public static final int RECEIVER = 113;
        public static final int RELAUNCH_ACTIVITY = 126;
        public static final int REMOVE_PROVIDER = 131;
        public static final int REQUEST_ASSIST_CONTEXT_EXTRAS = 143;
        public static final int RESUME_ACTIVITY = 107;
        public static final int SCHEDULE_CRASH = 134;
        public static final int SEND_RESULT = 108;
        public static final int SERVICE_ARGS = 115;
        public static final int SET_CORE_SETTINGS = 138;
        public static final int SHOW_WINDOW = 105;
        public static final int SLEEPING = 137;
        public static final int START_BINDER_TRACKING = 150;
        public static final int STOP_ACTIVITY_HIDE = 104;
        public static final int STOP_ACTIVITY_SHOW = 103;
        public static final int STOP_BINDER_TRACKING_AND_DUMP = 151;
        public static final int STOP_SERVICE = 116;
        public static final int SUICIDE = 130;
        public static final int TRANSLUCENT_CONVERSION_COMPLETE = 144;
        public static final int TRIM_MEMORY = 140;
        public static final int UNBIND_SERVICE = 122;
        public static final int UNSTABLE_PROVIDER_DIED = 142;
        public static final int UPDATE_PACKAGE_COMPATIBILITY_INFO = 139;

        /* synthetic */ H(ActivityThread this$0, H -this1) {
            this();
        }

        private H() {
        }

        String codeToString(int code) {
            if (ActivityThread.DEBUG_MESSAGES) {
                switch (code) {
                    case 100:
                        return "LAUNCH_ACTIVITY";
                    case 101:
                        return "PAUSE_ACTIVITY";
                    case 102:
                        return "PAUSE_ACTIVITY_FINISHING";
                    case 103:
                        return "STOP_ACTIVITY_SHOW";
                    case 104:
                        return "STOP_ACTIVITY_HIDE";
                    case 105:
                        return "SHOW_WINDOW";
                    case 106:
                        return "HIDE_WINDOW";
                    case 107:
                        return "RESUME_ACTIVITY";
                    case 108:
                        return "SEND_RESULT";
                    case 109:
                        return "DESTROY_ACTIVITY";
                    case 110:
                        return "BIND_APPLICATION";
                    case 111:
                        return "EXIT_APPLICATION";
                    case 112:
                        return "NEW_INTENT";
                    case 113:
                        return "RECEIVER";
                    case 114:
                        return "CREATE_SERVICE";
                    case 115:
                        return "SERVICE_ARGS";
                    case 116:
                        return "STOP_SERVICE";
                    case 118:
                        return "CONFIGURATION_CHANGED";
                    case 119:
                        return "CLEAN_UP_CONTEXT";
                    case 120:
                        return "GC_WHEN_IDLE";
                    case 121:
                        return "BIND_SERVICE";
                    case 122:
                        return "UNBIND_SERVICE";
                    case 123:
                        return "DUMP_SERVICE";
                    case 124:
                        return "LOW_MEMORY";
                    case 125:
                        return "ACTIVITY_CONFIGURATION_CHANGED";
                    case 126:
                        return "RELAUNCH_ACTIVITY";
                    case 127:
                        return "PROFILER_CONTROL";
                    case 128:
                        return "CREATE_BACKUP_AGENT";
                    case 129:
                        return "DESTROY_BACKUP_AGENT";
                    case 130:
                        return "SUICIDE";
                    case 131:
                        return "REMOVE_PROVIDER";
                    case 132:
                        return "ENABLE_JIT";
                    case 133:
                        return "DISPATCH_PACKAGE_BROADCAST";
                    case 134:
                        return "SCHEDULE_CRASH";
                    case 135:
                        return "DUMP_HEAP";
                    case 136:
                        return "DUMP_ACTIVITY";
                    case 137:
                        return "SLEEPING";
                    case 138:
                        return "SET_CORE_SETTINGS";
                    case 139:
                        return "UPDATE_PACKAGE_COMPATIBILITY_INFO";
                    case 140:
                        return "TRIM_MEMORY";
                    case 141:
                        return "DUMP_PROVIDER";
                    case 142:
                        return "UNSTABLE_PROVIDER_DIED";
                    case 143:
                        return "REQUEST_ASSIST_CONTEXT_EXTRAS";
                    case 144:
                        return "TRANSLUCENT_CONVERSION_COMPLETE";
                    case 145:
                        return "INSTALL_PROVIDER";
                    case 146:
                        return "ON_NEW_ACTIVITY_OPTIONS";
                    case 149:
                        return "ENTER_ANIMATION_COMPLETE";
                    case 152:
                        return "MULTI_WINDOW_MODE_CHANGED";
                    case 153:
                        return "PICTURE_IN_PICTURE_MODE_CHANGED";
                    case 154:
                        return "LOCAL_VOICE_INTERACTION_STARTED";
                    case 155:
                        return "ATTACH_AGENT";
                    case 156:
                        return "APPLICATION_INFO_CHANGED";
                    case 157:
                        return "ACTIVITY_MOVED_TO_DISPLAY";
                }
            }
            return Integer.toString(code);
        }

        public void handleMessage(Message msg) {
            if (ActivityThread.DEBUG_MESSAGES) {
                Slog.v(ActivityThread.TAG, ">>> handling: " + codeToString(msg.what));
            }
            SomeArgs args;
            switch (msg.what) {
                case 100:
                    Trace.traceBegin(64, "activityStart");
                    ActivityClientRecord r = msg.obj;
                    r.packageInfo = ActivityThread.this.getPackageInfoNoCheck(r.activityInfo.applicationInfo, r.compatInfo);
                    ActivityThread.this.handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
                    Trace.traceEnd(64);
                    break;
                case 101:
                    Trace.traceBegin(64, "activityPause");
                    args = msg.obj;
                    ActivityThread.this.handlePauseActivity((IBinder) args.arg1, false, (args.argi1 & 1) != 0, args.argi2, (args.argi1 & 2) != 0, args.argi3);
                    Trace.traceEnd(64);
                    break;
                case 102:
                    Trace.traceBegin(64, "activityPause");
                    args = (SomeArgs) msg.obj;
                    ActivityThread.this.handlePauseActivity((IBinder) args.arg1, true, (args.argi1 & 1) != 0, args.argi2, (args.argi1 & 2) != 0, args.argi3);
                    Trace.traceEnd(64);
                    break;
                case 103:
                    Trace.traceBegin(64, "activityStop");
                    args = (SomeArgs) msg.obj;
                    ActivityThread.this.handleStopActivity((IBinder) args.arg1, true, args.argi2, args.argi3);
                    Trace.traceEnd(64);
                    break;
                case 104:
                    Trace.traceBegin(64, "activityStop");
                    args = (SomeArgs) msg.obj;
                    ActivityThread.this.handleStopActivity((IBinder) args.arg1, false, args.argi2, args.argi3);
                    Trace.traceEnd(64);
                    break;
                case 105:
                    Trace.traceBegin(64, "activityShowWindow");
                    ActivityThread.this.handleWindowVisibility((IBinder) msg.obj, true);
                    Trace.traceEnd(64);
                    break;
                case 106:
                    Trace.traceBegin(64, "activityHideWindow");
                    ActivityThread.this.handleWindowVisibility((IBinder) msg.obj, false);
                    Trace.traceEnd(64);
                    break;
                case 107:
                    Trace.traceBegin(64, "activityResume");
                    args = (SomeArgs) msg.obj;
                    ActivityThread.this.handleResumeActivity((IBinder) args.arg1, true, args.argi1 != 0, true, args.argi3, "RESUME_ACTIVITY");
                    Trace.traceEnd(64);
                    break;
                case 108:
                    Trace.traceBegin(64, "activityDeliverResult");
                    ActivityThread.this.handleSendResult((ResultData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 109:
                    Trace.traceBegin(64, "activityDestroy");
                    ActivityThread.this.handleDestroyActivity((IBinder) msg.obj, msg.arg1 != 0, msg.arg2, false);
                    Trace.traceEnd(64);
                    break;
                case 110:
                    Trace.traceBegin(64, "bindApplication");
                    ActivityThread.this.handleBindApplication(msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 111:
                    if (ActivityThread.this.mInitialApplication != null) {
                        ActivityThread.this.mInitialApplication.onTerminate();
                    }
                    Looper.myLooper().quit();
                    break;
                case 112:
                    Trace.traceBegin(64, "activityNewIntent");
                    ActivityThread.this.handleNewIntent((NewIntentData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 113:
                    Trace.traceBegin(64, "broadcastReceiveComp");
                    ActivityThread.this.handleReceiver((ReceiverData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 114:
                    Trace.traceBegin(64, "serviceCreate: " + String.valueOf(msg.obj));
                    ActivityThread.this.handleCreateService((CreateServiceData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 115:
                    Trace.traceBegin(64, "serviceStart: " + String.valueOf(msg.obj));
                    ActivityThread.this.handleServiceArgs((ServiceArgsData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 116:
                    Trace.traceBegin(64, "serviceStop");
                    ActivityThread.this.handleStopService((IBinder) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 118:
                    Trace.traceBegin(64, "configChanged");
                    ActivityThread.this.mCurDefaultDisplayDpi = ((Configuration) msg.obj).densityDpi;
                    ActivityThread.this.mUpdatingSystemConfig = true;
                    try {
                        ActivityThread.this.handleConfigurationChanged((Configuration) msg.obj, null);
                        Trace.traceEnd(64);
                        break;
                    } finally {
                        ActivityThread.this.mUpdatingSystemConfig = false;
                    }
                case 119:
                    ContextCleanupInfo cci = msg.obj;
                    cci.context.performFinalCleanup(cci.who, cci.what);
                    break;
                case 120:
                    ActivityThread.this.scheduleGcIdler();
                    break;
                case 121:
                    Trace.traceBegin(64, "serviceBind");
                    ActivityThread.this.handleBindService((BindServiceData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 122:
                    Trace.traceBegin(64, "serviceUnbind");
                    ActivityThread.this.handleUnbindService((BindServiceData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 123:
                    ActivityThread.this.handleDumpService((DumpComponentInfo) msg.obj);
                    break;
                case 124:
                    Trace.traceBegin(64, "lowMemory");
                    ActivityThread.this.handleLowMemory();
                    Trace.traceEnd(64);
                    break;
                case 125:
                    Trace.traceBegin(64, "activityConfigChanged");
                    ActivityThread.this.handleActivityConfigurationChanged((ActivityConfigChangeData) msg.obj, -1);
                    Trace.traceEnd(64);
                    break;
                case 126:
                    Trace.traceBegin(64, "activityRestart");
                    ActivityThread.this.handleRelaunchActivity((ActivityClientRecord) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 127:
                    ActivityThread.this.handleProfilerControl(msg.arg1 != 0, (ProfilerInfo) msg.obj, msg.arg2);
                    break;
                case 128:
                    Trace.traceBegin(64, "backupCreateAgent");
                    ActivityThread.this.handleCreateBackupAgent((CreateBackupAgentData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 129:
                    Trace.traceBegin(64, "backupDestroyAgent");
                    ActivityThread.this.handleDestroyBackupAgent((CreateBackupAgentData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 130:
                    Process.killProcess(Process.myPid());
                    break;
                case 131:
                    Trace.traceBegin(64, "providerRemove");
                    ActivityThread.this.completeRemoveProvider((ProviderRefCount) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 132:
                    ActivityThread.this.ensureJitEnabled();
                    break;
                case 133:
                    Trace.traceBegin(64, "broadcastPackage");
                    ActivityThread.this.handleDispatchPackageBroadcast(msg.arg1, (String[]) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 134:
                    if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                        String msgStr = msg.obj;
                        if (msgStr != null && msgStr.contains("can't deliver broadcast")) {
                            Log.d(ActivityThread.TAG, "can't deliver broadcast, before died, dump msgs:");
                            Looper.getMainLooper().getQueue().dumpMessage();
                        }
                    }
                    throw new RemoteServiceException((String) msg.obj);
                case 135:
                    ActivityThread.handleDumpHeap((DumpHeapData) msg.obj);
                    break;
                case 136:
                    ActivityThread.this.handleDumpActivity((DumpComponentInfo) msg.obj);
                    break;
                case 137:
                    Trace.traceBegin(64, "sleeping");
                    ActivityThread.this.handleSleeping((IBinder) msg.obj, msg.arg1 != 0);
                    Trace.traceEnd(64);
                    break;
                case 138:
                    Trace.traceBegin(64, "setCoreSettings");
                    ActivityThread.this.handleSetCoreSettings((Bundle) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 139:
                    ActivityThread.this.handleUpdatePackageCompatibilityInfo((UpdateCompatibilityData) msg.obj);
                    break;
                case 140:
                    Trace.traceBegin(64, "trimMemory");
                    ActivityThread.this.handleTrimMemory(msg.arg1);
                    Trace.traceEnd(64);
                    break;
                case 141:
                    ActivityThread.this.handleDumpProvider((DumpComponentInfo) msg.obj);
                    break;
                case 142:
                    ActivityThread.this.handleUnstableProviderDied((IBinder) msg.obj, false);
                    break;
                case 143:
                    ActivityThread.this.handleRequestAssistContextExtras((RequestAssistContextExtras) msg.obj);
                    break;
                case 144:
                    ActivityThread.this.handleTranslucentConversionComplete((IBinder) msg.obj, msg.arg1 == 1);
                    break;
                case 145:
                    ActivityThread.this.handleInstallProvider((ProviderInfo) msg.obj);
                    break;
                case 146:
                    Pair<IBinder, ActivityOptions> pair = msg.obj;
                    ActivityThread.this.onNewActivityOptions((IBinder) pair.first, (ActivityOptions) pair.second);
                    break;
                case 149:
                    ActivityThread.this.handleEnterAnimationComplete((IBinder) msg.obj);
                    break;
                case 150:
                    ActivityThread.this.handleStartBinderTracking();
                    break;
                case 151:
                    ActivityThread.this.handleStopBinderTrackingAndDump((ParcelFileDescriptor) msg.obj);
                    break;
                case 152:
                    ActivityThread.this.handleMultiWindowModeChanged((IBinder) ((SomeArgs) msg.obj).arg1, ((SomeArgs) msg.obj).argi1 == 1, (Configuration) ((SomeArgs) msg.obj).arg2);
                    break;
                case 153:
                    ActivityThread.this.handlePictureInPictureModeChanged((IBinder) ((SomeArgs) msg.obj).arg1, ((SomeArgs) msg.obj).argi1 == 1, (Configuration) ((SomeArgs) msg.obj).arg2);
                    break;
                case 154:
                    ActivityThread.this.handleLocalVoiceInteractionStarted((IBinder) ((SomeArgs) msg.obj).arg1, (IVoiceInteractor) ((SomeArgs) msg.obj).arg2);
                    break;
                case 155:
                    ActivityThread.handleAttachAgent((String) msg.obj);
                    break;
                case 156:
                    ActivityThread.this.mUpdatingSystemConfig = true;
                    try {
                        ActivityThread.this.handleApplicationInfoChanged((ApplicationInfo) msg.obj);
                        break;
                    } finally {
                        ActivityThread.this.mUpdatingSystemConfig = false;
                    }
                case 157:
                    Trace.traceBegin(64, "activityMovedToDisplay");
                    ActivityThread.this.handleActivityConfigurationChanged((ActivityConfigChangeData) msg.obj, msg.arg1);
                    Trace.traceEnd(64);
                    break;
            }
            Object obj = msg.obj;
            if (obj instanceof SomeArgs) {
                ((SomeArgs) obj).recycle();
            }
            if (ActivityThread.DEBUG_MESSAGES) {
                Slog.v(ActivityThread.TAG, "<<< done: " + codeToString(msg.what));
            }
        }
    }

    private class Idler implements IdleHandler {
        /* synthetic */ Idler(ActivityThread this$0, Idler -this1) {
            this();
        }

        private Idler() {
        }

        public final boolean queueIdle() {
            ActivityClientRecord a = ActivityThread.this.mNewActivities;
            boolean stopProfiling = false;
            if (!(ActivityThread.this.mBoundApplication == null || ActivityThread.this.mProfiler.profileFd == null || !ActivityThread.this.mProfiler.autoStopProfiler)) {
                stopProfiling = true;
            }
            if (a != null) {
                ActivityThread.this.mNewActivities = null;
                IActivityManager am = ActivityManager.getService();
                do {
                    if (ActivityThread.localLOGV) {
                        boolean z;
                        String str = ActivityThread.TAG;
                        StringBuilder append = new StringBuilder().append("Reporting idle of ").append(a).append(" finished=");
                        if (a.activity != null) {
                            z = a.activity.mFinished;
                        } else {
                            z = false;
                        }
                        Slog.v(str, append.append(z).toString());
                    }
                    if (!(a.activity == null || (a.activity.mFinished ^ 1) == 0)) {
                        try {
                            am.activityIdle(a.token, a.createdConfig, stopProfiling);
                            a.createdConfig = null;
                        } catch (RemoteException ex) {
                            throw ex.rethrowFromSystemServer();
                        }
                    }
                    ActivityClientRecord prev = a;
                    a = a.nextIdle;
                    prev.nextIdle = null;
                } while (a != null);
            }
            if (stopProfiling) {
                ActivityThread.this.mProfiler.stopProfiling();
            }
            ActivityThread.this.ensureJitEnabled();
            return false;
        }
    }

    static final class NewIntentData {
        boolean andPause;
        List<ReferrerIntent> intents;
        IBinder token;

        NewIntentData() {
        }

        public String toString() {
            return "NewIntentData{intents=" + this.intents + " token=" + this.token + " andPause=" + this.andPause + "}";
        }
    }

    static final class Profiler {
        boolean autoStopProfiler;
        boolean handlingProfiling;
        ParcelFileDescriptor profileFd;
        String profileFile;
        boolean profiling;
        int samplingInterval;
        boolean streamingOutput;

        Profiler() {
        }

        public void setProfiler(ProfilerInfo profilerInfo) {
            ParcelFileDescriptor fd = profilerInfo.profileFd;
            if (this.profiling) {
                if (fd != null) {
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                }
                return;
            }
            if (this.profileFd != null) {
                try {
                    this.profileFd.close();
                } catch (IOException e2) {
                }
            }
            this.profileFile = profilerInfo.profileFile;
            this.profileFd = fd;
            this.samplingInterval = profilerInfo.samplingInterval;
            this.autoStopProfiler = profilerInfo.autoStopProfiler;
            this.streamingOutput = profilerInfo.streamingOutput;
        }

        public void startProfiling() {
            boolean z = true;
            if (this.profileFd != null && !this.profiling) {
                try {
                    int bufferSize = SystemProperties.getInt("debug.traceview-buffer-size-mb", 8);
                    String str = this.profileFile;
                    FileDescriptor fileDescriptor = this.profileFd.getFileDescriptor();
                    int i = (bufferSize * 1024) * 1024;
                    if (this.samplingInterval == 0) {
                        z = false;
                    }
                    VMDebug.startMethodTracing(str, fileDescriptor, i, 0, z, this.samplingInterval, this.streamingOutput);
                    this.profiling = true;
                } catch (RuntimeException e) {
                    Slog.w(ActivityThread.TAG, "Profiling failed on path " + this.profileFile);
                    try {
                        this.profileFd.close();
                        this.profileFd = null;
                    } catch (IOException e2) {
                        Slog.w(ActivityThread.TAG, "Failure closing profile fd", e2);
                    }
                }
            }
        }

        public void stopProfiling() {
            if (this.profiling) {
                this.profiling = false;
                Debug.stopMethodTracing();
                if (this.profileFd != null) {
                    try {
                        this.profileFd.close();
                    } catch (IOException e) {
                    }
                }
                this.profileFd = null;
                this.profileFile = null;
            }
        }
    }

    final class ProviderClientRecord {
        final ContentProviderHolder mHolder;
        final ContentProvider mLocalProvider;
        final String[] mNames;
        final IContentProvider mProvider;

        ProviderClientRecord(String[] names, IContentProvider provider, ContentProvider localProvider, ContentProviderHolder holder) {
            this.mNames = names;
            this.mProvider = provider;
            this.mLocalProvider = localProvider;
            this.mHolder = holder;
        }
    }

    private static final class ProviderKey {
        final String authority;
        final int userId;

        public ProviderKey(String authority, int userId) {
            this.authority = authority;
            this.userId = userId;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof ProviderKey)) {
                return false;
            }
            ProviderKey other = (ProviderKey) o;
            if (Objects.equals(this.authority, other.authority) && this.userId == other.userId) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (this.authority != null ? this.authority.hashCode() : 0) ^ this.userId;
        }
    }

    private static final class ProviderRefCount {
        public final ProviderClientRecord client;
        public final ContentProviderHolder holder;
        public boolean removePending;
        public int stableCount;
        public int unstableCount;

        ProviderRefCount(ContentProviderHolder inHolder, ProviderClientRecord inClient, int sCount, int uCount) {
            this.holder = inHolder;
            this.client = inClient;
            this.stableCount = sCount;
            this.unstableCount = uCount;
        }
    }

    static final class ReceiverData extends PendingResult {
        CompatibilityInfo compatInfo;
        ActivityInfo info;
        Intent intent;

        public ReceiverData(Intent intent, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, IBinder token, int sendingUser, int hasCode) {
            super(resultCode, resultData, resultExtras, 0, ordered, sticky, token, sendingUser, intent.getFlags());
            this.intent = intent;
            if (ActivityThread.DEBUG_BROADCAST) {
                Slog.i(ActivityThread.TAG, "new ReceiverData() hasCode " + hasCode);
            }
            setHascode(hasCode);
        }

        public String toString() {
            return "ReceiverData{intent=" + this.intent + " packageName=" + this.info.packageName + " resultCode=" + getResultCode() + " resultData=" + getResultData() + " resultExtras=" + getResultExtras(false) + "}";
        }
    }

    static final class RequestAssistContextExtras {
        IBinder activityToken;
        int flags;
        IBinder requestToken;
        int requestType;
        int sessionId;

        RequestAssistContextExtras() {
        }
    }

    static final class ResultData {
        List<ResultInfo> results;
        IBinder token;

        ResultData() {
        }

        public String toString() {
            return "ResultData{token=" + this.token + " results" + this.results + "}";
        }
    }

    static final class ServiceArgsData {
        Intent args;
        int flags;
        int startId;
        boolean taskRemoved;
        IBinder token;

        ServiceArgsData() {
        }

        public String toString() {
            return "ServiceArgsData{token=" + this.token + " startId=" + this.startId + " args=" + this.args + "}";
        }
    }

    private static class StopInfo implements Runnable {
        ActivityClientRecord activity;
        CharSequence description;
        PersistableBundle persistentState;
        Bundle state;

        /* synthetic */ StopInfo(StopInfo -this0) {
            this();
        }

        private StopInfo() {
        }

        public void run() {
            try {
                if (ActivityThread.DEBUG_MEMORY_TRIM) {
                    Slog.v(ActivityThread.TAG, "Reporting activity stopped: " + this.activity);
                }
                ActivityManager.getService().activityStopped(this.activity.token, this.state, this.persistentState, this.description);
            } catch (RemoteException ex) {
                IndentingPrintWriter pw = new IndentingPrintWriter(new LogWriter(5, ActivityThread.TAG), "  ");
                pw.println("Bundle stats:");
                BaseBundle.dumpStats(pw, this.state);
                pw.println("PersistableBundle stats:");
                BaseBundle.dumpStats(pw, this.persistentState);
                if (!(ex instanceof TransactionTooLargeException) || this.activity.packageInfo.getTargetSdkVersion() >= 24) {
                    throw ex.rethrowFromSystemServer();
                }
                Log.e(ActivityThread.TAG, "App sent too much data in instance state, so it was ignored", ex);
            }
        }
    }

    static final class UpdateCompatibilityData {
        CompatibilityInfo info;
        String pkg;

        UpdateCompatibilityData() {
        }
    }

    private native void nDumpGraphicsInfo(FileDescriptor fileDescriptor);

    private int getLifecycleSeq() {
        int i;
        synchronized (this.mResourcesManager) {
            i = this.mLifecycleSeq;
            this.mLifecycleSeq = i + 1;
        }
        return i;
    }

    public static ActivityThread currentActivityThread() {
        return sCurrentActivityThread;
    }

    public static boolean isSystem() {
        return sCurrentActivityThread != null ? sCurrentActivityThread.mSystemThread : false;
    }

    public static String currentOpPackageName() {
        ActivityThread am = currentActivityThread();
        if (am == null || am.getApplication() == null) {
            return null;
        }
        return am.getApplication().getOpPackageName();
    }

    public static String currentPackageName() {
        ActivityThread am = currentActivityThread();
        if (am == null || am.mBoundApplication == null) {
            return null;
        }
        return am.mBoundApplication.appInfo.packageName;
    }

    public static String currentProcessName() {
        ActivityThread am = currentActivityThread();
        if (am == null || am.mBoundApplication == null) {
            return null;
        }
        return am.mBoundApplication.processName;
    }

    public static Application currentApplication() {
        ActivityThread am = currentActivityThread();
        if (am != null) {
            return am.mInitialApplication;
        }
        return null;
    }

    public static IPackageManager getPackageManager() {
        if (sPackageManager != null) {
            return sPackageManager;
        }
        sPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService(TvContract.PARAM_PACKAGE));
        return sPackageManager;
    }

    Configuration applyConfigCompatMainThread(int displayDensity, Configuration config, CompatibilityInfo compat) {
        if (config == null) {
            return null;
        }
        if (!compat.supportsScreen()) {
            this.mMainThreadConfig.setTo(config);
            config = this.mMainThreadConfig;
            compat.applyToConfiguration(displayDensity, config);
        }
        return config;
    }

    Resources getTopLevelResources(String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, LoadedApk pkgInfo) {
        return this.mResourcesManager.getResources(null, resDir, splitResDirs, overlayDirs, libDirs, displayId, null, pkgInfo.getCompatibilityInfo(), pkgInfo.getClassLoader());
    }

    final Handler getHandler() {
        return this.mH;
    }

    public final LoadedApk getPackageInfo(String packageName, CompatibilityInfo compatInfo, int flags) {
        return getPackageInfo(packageName, compatInfo, flags, UserHandle.myUserId());
    }

    /* JADX WARNING: Missing block: B:32:0x008a, code:
            return r3;
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            r0 = getPackageManager().getApplicationInfo(r10, 268436480, r13);
     */
    /* JADX WARNING: Missing block: B:37:0x0098, code:
            if (r0 == null) goto L_0x00a5;
     */
    /* JADX WARNING: Missing block: B:39:0x009e, code:
            return getPackageInfo(r0, r11, r12);
     */
    /* JADX WARNING: Missing block: B:40:0x009f, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:42:0x00a4, code:
            throw r2.rethrowFromSystemServer();
     */
    /* JADX WARNING: Missing block: B:43:0x00a5, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final LoadedApk getPackageInfo(String packageName, CompatibilityInfo compatInfo, int flags, int userId) {
        boolean differentUser = UserHandle.myUserId() != userId;
        synchronized (this.mResourcesManager) {
            WeakReference ref;
            if (differentUser) {
                ref = null;
            } else if ((flags & 1) != 0) {
                ref = (WeakReference) this.mPackages.get(packageName);
            } else {
                ref = (WeakReference) this.mResourcePackages.get(packageName);
            }
            LoadedApk packageInfo = ref != null ? (LoadedApk) ref.get() : null;
            if (packageInfo == null || !(packageInfo.mResources == null || packageInfo.mResources.getAssets().isUpToDate())) {
            } else if (packageInfo.isSecurityViolation() && (flags & 2) == 0) {
                throw new SecurityException("Requesting code from " + packageName + " to be run in process " + this.mBoundApplication.processName + "/" + this.mBoundApplication.appInfo.uid);
            }
        }
    }

    public final LoadedApk getPackageInfo(ApplicationInfo ai, CompatibilityInfo compatInfo, int flags) {
        boolean securityViolation;
        boolean includeCode = (flags & 1) != 0;
        if (!includeCode || ai.uid == 0 || ai.uid == 1000) {
            securityViolation = false;
        } else if (this.mBoundApplication != null) {
            securityViolation = UserHandle.isSameApp(ai.uid, this.mBoundApplication.appInfo.uid) ^ 1;
        } else {
            securityViolation = true;
        }
        boolean registerPackage = includeCode && (1073741824 & flags) != 0;
        if ((flags & 3) != 1 || !securityViolation) {
            return getPackageInfo(ai, compatInfo, null, securityViolation, includeCode, registerPackage);
        }
        String msg = "Requesting code from " + ai.packageName + " (with uid " + ai.uid + ")";
        if (this.mBoundApplication != null) {
            msg = msg + " to be run in process " + this.mBoundApplication.processName + " (with uid " + this.mBoundApplication.appInfo.uid + ")";
        }
        throw new SecurityException(msg);
    }

    public final LoadedApk getPackageInfoNoCheck(ApplicationInfo ai, CompatibilityInfo compatInfo) {
        return getPackageInfo(ai, compatInfo, null, false, true, false);
    }

    public final LoadedApk peekPackageInfo(String packageName, boolean includeCode) {
        LoadedApk loadedApk = null;
        synchronized (this.mResourcesManager) {
            WeakReference<LoadedApk> ref;
            if (includeCode) {
                ref = (WeakReference) this.mPackages.get(packageName);
            } else {
                ref = (WeakReference) this.mResourcePackages.get(packageName);
            }
            if (ref != null) {
                loadedApk = (LoadedApk) ref.get();
            }
        }
        return loadedApk;
    }

    private LoadedApk getPackageInfo(ApplicationInfo aInfo, CompatibilityInfo compatInfo, ClassLoader baseLoader, boolean securityViolation, boolean includeCode, boolean registerPackage) {
        LoadedApk packageInfo;
        boolean differentUser = UserHandle.myUserId() != UserHandle.getUserId(aInfo.uid);
        synchronized (this.mResourcesManager) {
            WeakReference ref;
            if (differentUser) {
                ref = null;
            } else if (includeCode) {
                ref = (WeakReference) this.mPackages.get(aInfo.packageName);
            } else {
                ref = (WeakReference) this.mResourcePackages.get(aInfo.packageName);
            }
            packageInfo = ref != null ? (LoadedApk) ref.get() : null;
            if (packageInfo == null || !(packageInfo.mResources == null || (packageInfo.mResources.getAssets().isUpToDate() ^ 1) == 0)) {
                if (localLOGV) {
                    String str;
                    String str2 = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    if (includeCode) {
                        str = "Loading code package ";
                    } else {
                        str = "Loading resource-only package ";
                    }
                    Slog.v(str2, stringBuilder.append(str).append(aInfo.packageName).append(" (in ").append(this.mBoundApplication != null ? this.mBoundApplication.processName : null).append(")").toString());
                }
                boolean z = includeCode ? (aInfo.flags & 4) != 0 : false;
                packageInfo = new LoadedApk(this, aInfo, compatInfo, baseLoader, securityViolation, z, registerPackage);
                if (this.mSystemThread && "android".equals(aInfo.packageName)) {
                    packageInfo.installSystemApplicationInfo(aInfo, getSystemContext().mPackageInfo.getClassLoader());
                }
                if (!differentUser) {
                    if (includeCode) {
                        this.mPackages.put(aInfo.packageName, new WeakReference(packageInfo));
                    } else {
                        this.mResourcePackages.put(aInfo.packageName, new WeakReference(packageInfo));
                    }
                }
            }
        }
        return packageInfo;
    }

    ActivityThread() {
    }

    public ApplicationThread getApplicationThread() {
        return this.mAppThread;
    }

    public Instrumentation getInstrumentation() {
        return this.mInstrumentation;
    }

    public boolean isProfiling() {
        if (this.mProfiler == null || this.mProfiler.profileFile == null || this.mProfiler.profileFd != null) {
            return false;
        }
        return true;
    }

    public String getProfileFilePath() {
        return this.mProfiler.profileFile;
    }

    public Looper getLooper() {
        return this.mLooper;
    }

    public Application getApplication() {
        return this.mInitialApplication;
    }

    public String getProcessName() {
        return this.mBoundApplication.processName;
    }

    public ContextImpl getSystemContext() {
        ContextImpl contextImpl;
        synchronized (this) {
            if (this.mSystemContext == null) {
                this.mSystemContext = ContextImpl.createSystemContext(this);
            }
            contextImpl = this.mSystemContext;
        }
        return contextImpl;
    }

    public ContextImpl getSystemUiContext() {
        ContextImpl contextImpl;
        synchronized (this) {
            if (this.mSystemUiContext == null) {
                this.mSystemUiContext = ContextImpl.createSystemUiContext(getSystemContext());
            }
            contextImpl = this.mSystemUiContext;
        }
        return contextImpl;
    }

    public void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        synchronized (this) {
            getSystemContext().installSystemApplicationInfo(info, classLoader);
            getSystemUiContext().installSystemApplicationInfo(info, classLoader);
            this.mProfiler = new Profiler();
        }
    }

    void ensureJitEnabled() {
        if (!this.mJitEnabled) {
            this.mJitEnabled = true;
            VMRuntime.getRuntime().startJitCompilation();
        }
    }

    void scheduleGcIdler() {
        if (!this.mGcIdlerScheduled) {
            this.mGcIdlerScheduled = true;
            Looper.myQueue().addIdleHandler(this.mGcIdler);
        }
        this.mH.removeMessages(120);
    }

    void unscheduleGcIdler() {
        if (this.mGcIdlerScheduled) {
            this.mGcIdlerScheduled = false;
            Looper.myQueue().removeIdleHandler(this.mGcIdler);
        }
        this.mH.removeMessages(120);
    }

    void doGcIfNeeded() {
        this.mGcIdlerScheduled = false;
        if (BinderInternal.getLastGcTime() + MIN_TIME_BETWEEN_GCS < SystemClock.uptimeMillis()) {
            BinderInternal.forceGc("bg");
        }
    }

    static void printRow(PrintWriter pw, String format, Object... objs) {
        pw.println(String.format(format, objs));
    }

    public static void dumpMemInfoTable(PrintWriter pw, MemoryInfo memInfo, boolean checkin, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, int pid, String processName, long nativeMax, long nativeAllocated, long nativeFree, long dalvikMax, long dalvikAllocated, long dalvikFree) {
        int i;
        if (checkin) {
            pw.print(4);
            pw.print(',');
            pw.print(pid);
            pw.print(',');
            pw.print(processName);
            pw.print(',');
            pw.print(nativeMax);
            pw.print(',');
            pw.print(dalvikMax);
            pw.print(',');
            pw.print("N/A,");
            pw.print(nativeMax + dalvikMax);
            pw.print(',');
            pw.print(nativeAllocated);
            pw.print(',');
            pw.print(dalvikAllocated);
            pw.print(',');
            pw.print("N/A,");
            pw.print(nativeAllocated + dalvikAllocated);
            pw.print(',');
            pw.print(nativeFree);
            pw.print(',');
            pw.print(dalvikFree);
            pw.print(',');
            pw.print("N/A,");
            pw.print(nativeFree + dalvikFree);
            pw.print(',');
            pw.print(memInfo.nativePss);
            pw.print(',');
            pw.print(memInfo.dalvikPss);
            pw.print(',');
            pw.print(memInfo.otherPss);
            pw.print(',');
            pw.print(memInfo.getTotalPss());
            pw.print(',');
            pw.print(memInfo.nativeSwappablePss);
            pw.print(',');
            pw.print(memInfo.dalvikSwappablePss);
            pw.print(',');
            pw.print(memInfo.otherSwappablePss);
            pw.print(',');
            pw.print(memInfo.getTotalSwappablePss());
            pw.print(',');
            pw.print(memInfo.nativeSharedDirty);
            pw.print(',');
            pw.print(memInfo.dalvikSharedDirty);
            pw.print(',');
            pw.print(memInfo.otherSharedDirty);
            pw.print(',');
            pw.print(memInfo.getTotalSharedDirty());
            pw.print(',');
            pw.print(memInfo.nativeSharedClean);
            pw.print(',');
            pw.print(memInfo.dalvikSharedClean);
            pw.print(',');
            pw.print(memInfo.otherSharedClean);
            pw.print(',');
            pw.print(memInfo.getTotalSharedClean());
            pw.print(',');
            pw.print(memInfo.nativePrivateDirty);
            pw.print(',');
            pw.print(memInfo.dalvikPrivateDirty);
            pw.print(',');
            pw.print(memInfo.otherPrivateDirty);
            pw.print(',');
            pw.print(memInfo.getTotalPrivateDirty());
            pw.print(',');
            pw.print(memInfo.nativePrivateClean);
            pw.print(',');
            pw.print(memInfo.dalvikPrivateClean);
            pw.print(',');
            pw.print(memInfo.otherPrivateClean);
            pw.print(',');
            pw.print(memInfo.getTotalPrivateClean());
            pw.print(',');
            pw.print(memInfo.nativeSwappedOut);
            pw.print(',');
            pw.print(memInfo.dalvikSwappedOut);
            pw.print(',');
            pw.print(memInfo.otherSwappedOut);
            pw.print(',');
            pw.print(memInfo.getTotalSwappedOut());
            pw.print(',');
            if (memInfo.hasSwappedOutPss) {
                pw.print(memInfo.nativeSwappedOutPss);
                pw.print(',');
                pw.print(memInfo.dalvikSwappedOutPss);
                pw.print(',');
                pw.print(memInfo.otherSwappedOutPss);
                pw.print(',');
                pw.print(memInfo.getTotalSwappedOutPss());
                pw.print(',');
            } else {
                pw.print("N/A,");
                pw.print("N/A,");
                pw.print("N/A,");
                pw.print("N/A,");
            }
            for (i = 0; i < 17; i++) {
                pw.print(MemoryInfo.getOtherLabel(i));
                pw.print(',');
                pw.print(memInfo.getOtherPss(i));
                pw.print(',');
                pw.print(memInfo.getOtherSwappablePss(i));
                pw.print(',');
                pw.print(memInfo.getOtherSharedDirty(i));
                pw.print(',');
                pw.print(memInfo.getOtherSharedClean(i));
                pw.print(',');
                pw.print(memInfo.getOtherPrivateDirty(i));
                pw.print(',');
                pw.print(memInfo.getOtherPrivateClean(i));
                pw.print(',');
                pw.print(memInfo.getOtherSwappedOut(i));
                pw.print(',');
                if (memInfo.hasSwappedOutPss) {
                    pw.print(memInfo.getOtherSwappedOutPss(i));
                    pw.print(',');
                } else {
                    pw.print("N/A,");
                }
            }
            return;
        }
        if (!dumpSummaryOnly) {
            String str;
            String[] strArr;
            int i2;
            int myPss;
            int mySwappablePss;
            int mySharedDirty;
            int myPrivateDirty;
            int mySharedClean;
            int myPrivateClean;
            int mySwappedOut;
            int mySwappedOutPss;
            String str2;
            String[] strArr2;
            if (dumpFullInfo) {
                str = HEAP_FULL_COLUMN;
                strArr = new Object[11];
                strArr[0] = "";
                strArr[1] = "Pss";
                strArr[2] = "Pss";
                strArr[3] = "Shared";
                strArr[4] = "Private";
                strArr[5] = "Shared";
                strArr[6] = "Private";
                strArr[7] = memInfo.hasSwappedOutPss ? "SwapPss" : "Swap";
                strArr[8] = "Heap";
                strArr[9] = "Heap";
                strArr[10] = "Heap";
                printRow(pw, str, strArr);
                printRow(pw, HEAP_FULL_COLUMN, "", "Total", "Clean", "Dirty", "Dirty", "Clean", "Clean", "Dirty", "Size", "Alloc", "Free");
                printRow(pw, HEAP_FULL_COLUMN, "", "------", "------", "------", "------", "------", "------", "------", "------", "------", "------");
                str = HEAP_FULL_COLUMN;
                strArr = new Object[11];
                strArr[0] = "Native Heap";
                strArr[1] = Integer.valueOf(memInfo.nativePss);
                strArr[2] = Integer.valueOf(memInfo.nativeSwappablePss);
                strArr[3] = Integer.valueOf(memInfo.nativeSharedDirty);
                strArr[4] = Integer.valueOf(memInfo.nativePrivateDirty);
                strArr[5] = Integer.valueOf(memInfo.nativeSharedClean);
                strArr[6] = Integer.valueOf(memInfo.nativePrivateClean);
                strArr[7] = Integer.valueOf(memInfo.hasSwappedOutPss ? memInfo.nativeSwappedOutPss : memInfo.nativeSwappedOut);
                strArr[8] = Long.valueOf(nativeMax);
                strArr[9] = Long.valueOf(nativeAllocated);
                strArr[10] = Long.valueOf(nativeFree);
                printRow(pw, str, strArr);
                str = HEAP_FULL_COLUMN;
                strArr = new Object[11];
                strArr[0] = "Dalvik Heap";
                strArr[1] = Integer.valueOf(memInfo.dalvikPss);
                strArr[2] = Integer.valueOf(memInfo.dalvikSwappablePss);
                strArr[3] = Integer.valueOf(memInfo.dalvikSharedDirty);
                strArr[4] = Integer.valueOf(memInfo.dalvikPrivateDirty);
                strArr[5] = Integer.valueOf(memInfo.dalvikSharedClean);
                strArr[6] = Integer.valueOf(memInfo.dalvikPrivateClean);
                strArr[7] = Integer.valueOf(memInfo.hasSwappedOutPss ? memInfo.dalvikSwappedOutPss : memInfo.dalvikSwappedOut);
                strArr[8] = Long.valueOf(dalvikMax);
                strArr[9] = Long.valueOf(dalvikAllocated);
                strArr[10] = Long.valueOf(dalvikFree);
                printRow(pw, str, strArr);
            } else {
                str = HEAP_COLUMN;
                strArr = new Object[8];
                strArr[0] = "";
                strArr[1] = "Pss";
                strArr[2] = "Private";
                strArr[3] = "Private";
                strArr[4] = memInfo.hasSwappedOutPss ? "SwapPss" : "Swap";
                strArr[5] = "Heap";
                strArr[6] = "Heap";
                strArr[7] = "Heap";
                printRow(pw, str, strArr);
                printRow(pw, HEAP_COLUMN, "", "Total", "Dirty", "Clean", "Dirty", "Size", "Alloc", "Free");
                printRow(pw, HEAP_COLUMN, "", "------", "------", "------", "------", "------", "------", "------", "------");
                str = HEAP_COLUMN;
                strArr = new Object[8];
                strArr[0] = "Native Heap";
                strArr[1] = Integer.valueOf(memInfo.nativePss);
                strArr[2] = Integer.valueOf(memInfo.nativePrivateDirty);
                strArr[3] = Integer.valueOf(memInfo.nativePrivateClean);
                if (memInfo.hasSwappedOutPss) {
                    i2 = memInfo.nativeSwappedOutPss;
                } else {
                    i2 = memInfo.nativeSwappedOut;
                }
                strArr[4] = Integer.valueOf(i2);
                strArr[5] = Long.valueOf(nativeMax);
                strArr[6] = Long.valueOf(nativeAllocated);
                strArr[7] = Long.valueOf(nativeFree);
                printRow(pw, str, strArr);
                str = HEAP_COLUMN;
                strArr = new Object[8];
                strArr[0] = "Dalvik Heap";
                strArr[1] = Integer.valueOf(memInfo.dalvikPss);
                strArr[2] = Integer.valueOf(memInfo.dalvikPrivateDirty);
                strArr[3] = Integer.valueOf(memInfo.dalvikPrivateClean);
                if (memInfo.hasSwappedOutPss) {
                    i2 = memInfo.dalvikSwappedOutPss;
                } else {
                    i2 = memInfo.dalvikSwappedOut;
                }
                strArr[4] = Integer.valueOf(i2);
                strArr[5] = Long.valueOf(dalvikMax);
                strArr[6] = Long.valueOf(dalvikAllocated);
                strArr[7] = Long.valueOf(dalvikFree);
                printRow(pw, str, strArr);
            }
            int otherPss = memInfo.otherPss;
            int otherSwappablePss = memInfo.otherSwappablePss;
            int otherSharedDirty = memInfo.otherSharedDirty;
            int otherPrivateDirty = memInfo.otherPrivateDirty;
            int otherSharedClean = memInfo.otherSharedClean;
            int otherPrivateClean = memInfo.otherPrivateClean;
            int otherSwappedOut = memInfo.otherSwappedOut;
            int otherSwappedOutPss = memInfo.otherSwappedOutPss;
            for (i = 0; i < 17; i++) {
                myPss = memInfo.getOtherPss(i);
                mySwappablePss = memInfo.getOtherSwappablePss(i);
                mySharedDirty = memInfo.getOtherSharedDirty(i);
                myPrivateDirty = memInfo.getOtherPrivateDirty(i);
                mySharedClean = memInfo.getOtherSharedClean(i);
                myPrivateClean = memInfo.getOtherPrivateClean(i);
                mySwappedOut = memInfo.getOtherSwappedOut(i);
                mySwappedOutPss = memInfo.getOtherSwappedOutPss(i);
                if (myPss == 0 && mySharedDirty == 0 && myPrivateDirty == 0 && mySharedClean == 0 && myPrivateClean == 0) {
                    if ((memInfo.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut) == 0) {
                    }
                }
                if (dumpFullInfo) {
                    str = HEAP_FULL_COLUMN;
                    strArr = new Object[11];
                    strArr[0] = MemoryInfo.getOtherLabel(i);
                    strArr[1] = Integer.valueOf(myPss);
                    strArr[2] = Integer.valueOf(mySwappablePss);
                    strArr[3] = Integer.valueOf(mySharedDirty);
                    strArr[4] = Integer.valueOf(myPrivateDirty);
                    strArr[5] = Integer.valueOf(mySharedClean);
                    strArr[6] = Integer.valueOf(myPrivateClean);
                    if (memInfo.hasSwappedOutPss) {
                        i2 = mySwappedOutPss;
                    } else {
                        i2 = mySwappedOut;
                    }
                    strArr[7] = Integer.valueOf(i2);
                    strArr[8] = "";
                    strArr[9] = "";
                    strArr[10] = "";
                    printRow(pw, str, strArr);
                } else {
                    str = HEAP_COLUMN;
                    strArr = new Object[8];
                    strArr[0] = MemoryInfo.getOtherLabel(i);
                    strArr[1] = Integer.valueOf(myPss);
                    strArr[2] = Integer.valueOf(myPrivateDirty);
                    strArr[3] = Integer.valueOf(myPrivateClean);
                    if (memInfo.hasSwappedOutPss) {
                        i2 = mySwappedOutPss;
                    } else {
                        i2 = mySwappedOut;
                    }
                    strArr[4] = Integer.valueOf(i2);
                    strArr[5] = "";
                    strArr[6] = "";
                    strArr[7] = "";
                    printRow(pw, str, strArr);
                }
                otherPss -= myPss;
                otherSwappablePss -= mySwappablePss;
                otherSharedDirty -= mySharedDirty;
                otherPrivateDirty -= myPrivateDirty;
                otherSharedClean -= mySharedClean;
                otherPrivateClean -= myPrivateClean;
                otherSwappedOut -= mySwappedOut;
                otherSwappedOutPss -= mySwappedOutPss;
            }
            if (dumpFullInfo) {
                str2 = HEAP_FULL_COLUMN;
                strArr2 = new Object[11];
                strArr2[0] = "Unknown";
                strArr2[1] = Integer.valueOf(otherPss);
                strArr2[2] = Integer.valueOf(otherSwappablePss);
                strArr2[3] = Integer.valueOf(otherSharedDirty);
                strArr2[4] = Integer.valueOf(otherPrivateDirty);
                strArr2[5] = Integer.valueOf(otherSharedClean);
                strArr2[6] = Integer.valueOf(otherPrivateClean);
                if (!memInfo.hasSwappedOutPss) {
                    otherSwappedOutPss = otherSwappedOut;
                }
                strArr2[7] = Integer.valueOf(otherSwappedOutPss);
                strArr2[8] = "";
                strArr2[9] = "";
                strArr2[10] = "";
                printRow(pw, str2, strArr2);
                str = HEAP_FULL_COLUMN;
                strArr = new Object[11];
                strArr[0] = "TOTAL";
                strArr[1] = Integer.valueOf(memInfo.getTotalPss());
                strArr[2] = Integer.valueOf(memInfo.getTotalSwappablePss());
                strArr[3] = Integer.valueOf(memInfo.getTotalSharedDirty());
                strArr[4] = Integer.valueOf(memInfo.getTotalPrivateDirty());
                strArr[5] = Integer.valueOf(memInfo.getTotalSharedClean());
                strArr[6] = Integer.valueOf(memInfo.getTotalPrivateClean());
                if (memInfo.hasSwappedOutPss) {
                    i2 = memInfo.getTotalSwappedOutPss();
                } else {
                    i2 = memInfo.getTotalSwappedOut();
                }
                strArr[7] = Integer.valueOf(i2);
                strArr[8] = Long.valueOf(nativeMax + dalvikMax);
                strArr[9] = Long.valueOf(nativeAllocated + dalvikAllocated);
                strArr[10] = Long.valueOf(nativeFree + dalvikFree);
                printRow(pw, str, strArr);
            } else {
                str2 = HEAP_COLUMN;
                strArr2 = new Object[8];
                strArr2[0] = "Unknown";
                strArr2[1] = Integer.valueOf(otherPss);
                strArr2[2] = Integer.valueOf(otherPrivateDirty);
                strArr2[3] = Integer.valueOf(otherPrivateClean);
                if (!memInfo.hasSwappedOutPss) {
                    otherSwappedOutPss = otherSwappedOut;
                }
                strArr2[4] = Integer.valueOf(otherSwappedOutPss);
                strArr2[5] = "";
                strArr2[6] = "";
                strArr2[7] = "";
                printRow(pw, str2, strArr2);
                str = HEAP_COLUMN;
                strArr = new Object[8];
                strArr[0] = "TOTAL";
                strArr[1] = Integer.valueOf(memInfo.getTotalPss());
                strArr[2] = Integer.valueOf(memInfo.getTotalPrivateDirty());
                strArr[3] = Integer.valueOf(memInfo.getTotalPrivateClean());
                if (memInfo.hasSwappedOutPss) {
                    i2 = memInfo.getTotalSwappedOutPss();
                } else {
                    i2 = memInfo.getTotalSwappedOut();
                }
                strArr[4] = Integer.valueOf(i2);
                strArr[5] = Long.valueOf(nativeMax + dalvikMax);
                strArr[6] = Long.valueOf(nativeAllocated + dalvikAllocated);
                strArr[7] = Long.valueOf(nativeFree + dalvikFree);
                printRow(pw, str, strArr);
            }
            if (dumpDalvik) {
                pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                pw.println(" Dalvik Details");
                for (i = 17; i < 31; i++) {
                    myPss = memInfo.getOtherPss(i);
                    mySwappablePss = memInfo.getOtherSwappablePss(i);
                    mySharedDirty = memInfo.getOtherSharedDirty(i);
                    myPrivateDirty = memInfo.getOtherPrivateDirty(i);
                    mySharedClean = memInfo.getOtherSharedClean(i);
                    myPrivateClean = memInfo.getOtherPrivateClean(i);
                    mySwappedOut = memInfo.getOtherSwappedOut(i);
                    mySwappedOutPss = memInfo.getOtherSwappedOutPss(i);
                    if (myPss == 0 && mySharedDirty == 0 && myPrivateDirty == 0 && mySharedClean == 0 && myPrivateClean == 0) {
                        if ((memInfo.hasSwappedOutPss ? mySwappedOutPss : mySwappedOut) == 0) {
                        }
                    }
                    if (dumpFullInfo) {
                        str2 = HEAP_FULL_COLUMN;
                        strArr2 = new Object[11];
                        strArr2[0] = MemoryInfo.getOtherLabel(i);
                        strArr2[1] = Integer.valueOf(myPss);
                        strArr2[2] = Integer.valueOf(mySwappablePss);
                        strArr2[3] = Integer.valueOf(mySharedDirty);
                        strArr2[4] = Integer.valueOf(myPrivateDirty);
                        strArr2[5] = Integer.valueOf(mySharedClean);
                        strArr2[6] = Integer.valueOf(myPrivateClean);
                        if (!memInfo.hasSwappedOutPss) {
                            mySwappedOutPss = mySwappedOut;
                        }
                        strArr2[7] = Integer.valueOf(mySwappedOutPss);
                        strArr2[8] = "";
                        strArr2[9] = "";
                        strArr2[10] = "";
                        printRow(pw, str2, strArr2);
                    } else {
                        str2 = HEAP_COLUMN;
                        strArr2 = new Object[8];
                        strArr2[0] = MemoryInfo.getOtherLabel(i);
                        strArr2[1] = Integer.valueOf(myPss);
                        strArr2[2] = Integer.valueOf(myPrivateDirty);
                        strArr2[3] = Integer.valueOf(myPrivateClean);
                        if (!memInfo.hasSwappedOutPss) {
                            mySwappedOutPss = mySwappedOut;
                        }
                        strArr2[4] = Integer.valueOf(mySwappedOutPss);
                        strArr2[5] = "";
                        strArr2[6] = "";
                        strArr2[7] = "";
                        printRow(pw, str2, strArr2);
                    }
                }
            }
        }
        pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        pw.println(" App Summary");
        printRow(pw, ONE_COUNT_COLUMN_HEADER, "", "Pss(KB)");
        printRow(pw, ONE_COUNT_COLUMN_HEADER, "", "------");
        printRow(pw, ONE_COUNT_COLUMN, "Java Heap:", Integer.valueOf(memInfo.getSummaryJavaHeap()));
        printRow(pw, ONE_COUNT_COLUMN, "Native Heap:", Integer.valueOf(memInfo.getSummaryNativeHeap()));
        printRow(pw, ONE_COUNT_COLUMN, "Code:", Integer.valueOf(memInfo.getSummaryCode()));
        printRow(pw, ONE_COUNT_COLUMN, "Stack:", Integer.valueOf(memInfo.getSummaryStack()));
        printRow(pw, ONE_COUNT_COLUMN, "Graphics:", Integer.valueOf(memInfo.getSummaryGraphics()));
        printRow(pw, ONE_COUNT_COLUMN, "Private Other:", Integer.valueOf(memInfo.getSummaryPrivateOther()));
        printRow(pw, ONE_COUNT_COLUMN, "System:", Integer.valueOf(memInfo.getSummarySystem()));
        pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        if (memInfo.hasSwappedOutPss) {
            printRow(pw, TWO_COUNT_COLUMNS, "TOTAL:", Integer.valueOf(memInfo.getSummaryTotalPss()), "TOTAL SWAP PSS:", Integer.valueOf(memInfo.getSummaryTotalSwapPss()));
        } else {
            printRow(pw, TWO_COUNT_COLUMNS, "TOTAL:", Integer.valueOf(memInfo.getSummaryTotalPss()), "TOTAL SWAP (KB):", Integer.valueOf(memInfo.getSummaryTotalSwap()));
        }
    }

    public void registerOnActivityPausedListener(Activity activity, OnActivityPausedListener listener) {
        synchronized (this.mOnPauseListeners) {
            ArrayList<OnActivityPausedListener> list = (ArrayList) this.mOnPauseListeners.get(activity);
            if (list == null) {
                list = new ArrayList();
                this.mOnPauseListeners.put(activity, list);
            }
            list.add(listener);
        }
    }

    public void unregisterOnActivityPausedListener(Activity activity, OnActivityPausedListener listener) {
        synchronized (this.mOnPauseListeners) {
            ArrayList<OnActivityPausedListener> list = (ArrayList) this.mOnPauseListeners.get(activity);
            if (list != null) {
                list.remove(listener);
            }
        }
    }

    public final ActivityInfo resolveActivityInfo(Intent intent) {
        ActivityInfo aInfo = intent.resolveActivityInfo(this.mInitialApplication.getPackageManager(), 1024);
        if (aInfo == null) {
            Instrumentation.checkStartActivityResult(-92, intent);
        }
        return aInfo;
    }

    public final Activity startActivityNow(Activity parent, String id, Intent intent, ActivityInfo activityInfo, IBinder token, Bundle state, NonConfigurationInstances lastNonConfigurationInstances) {
        ActivityClientRecord r = new ActivityClientRecord();
        r.token = token;
        r.ident = 0;
        r.intent = intent;
        r.state = state;
        r.parent = parent;
        r.embeddedID = id;
        r.activityInfo = activityInfo;
        r.lastNonConfigurationInstances = lastNonConfigurationInstances;
        if (localLOGV) {
            String name;
            ComponentName compname = intent.getComponent();
            if (compname != null) {
                name = compname.toShortString();
            } else {
                name = "(Intent " + intent + ").getComponent() returned null";
            }
            Slog.v(TAG, "Performing launch: action=" + intent.getAction() + ", comp=" + name + ", token=" + token);
        }
        return performLaunchActivity(r, null);
    }

    public final Activity getActivity(IBinder token) {
        return ((ActivityClientRecord) this.mActivities.get(token)).activity;
    }

    public final void sendActivityResult(IBinder token, String id, int requestCode, int resultCode, Intent data) {
        ArrayList<ResultInfo> list = new ArrayList();
        list.add(new ResultInfo(id, requestCode, resultCode, data));
        this.mAppThread.scheduleSendResult(token, list);
    }

    private void sendMessage(int what, Object obj) {
        sendMessage(what, obj, 0, 0, false);
    }

    private void sendMessage(int what, Object obj, int arg1) {
        sendMessage(what, obj, arg1, 0, false);
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2) {
        sendMessage(what, obj, arg1, arg2, false);
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2, boolean async) {
        if (DEBUG_MESSAGES) {
            Slog.v(TAG, "SCHEDULE " + what + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mH.codeToString(what) + ": " + arg1 + " / " + obj);
        }
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (async) {
            msg.setAsynchronous(true);
        }
        this.mH.sendMessage(msg);
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2, int seq) {
        if (DEBUG_MESSAGES) {
            Slog.v(TAG, "SCHEDULE " + this.mH.codeToString(what) + " arg1=" + arg1 + " arg2=" + arg2 + "seq= " + seq);
        }
        Message msg = Message.obtain();
        msg.what = what;
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = obj;
        args.argi1 = arg1;
        args.argi2 = arg2;
        args.argi3 = seq;
        msg.obj = args;
        this.mH.sendMessage(msg);
    }

    final void scheduleContextCleanup(ContextImpl context, String who, String what) {
        ContextCleanupInfo cci = new ContextCleanupInfo();
        cci.context = context;
        cci.who = who;
        cci.what = what;
        sendMessage(119, cci);
    }

    private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
        ActivityInfo aInfo = r.activityInfo;
        if (r.packageInfo == null) {
            r.packageInfo = getPackageInfo(aInfo.applicationInfo, r.compatInfo, 1);
        }
        ComponentName component = r.intent.getComponent();
        if (component == null) {
            component = r.intent.resolveActivity(this.mInitialApplication.getPackageManager());
            r.intent.setComponent(component);
        }
        if (r.activityInfo.targetActivity != null) {
            ComponentName componentName = new ComponentName(r.activityInfo.packageName, r.activityInfo.targetActivity);
        }
        ContextImpl appContext = createBaseContextForActivity(r);
        Context activity = null;
        try {
            ClassLoader cl = appContext.getClassLoader();
            activity = this.mInstrumentation.newActivity(cl, component.getClassName(), r.intent);
            StrictMode.incrementExpectedActivityCount(activity.getClass());
            r.intent.setExtrasClassLoader(cl);
            r.intent.prepareToEnterProcess();
            if (r.state != null) {
                r.state.setClassLoader(cl);
            }
        } catch (Throwable e) {
            if (!this.mInstrumentation.onException(null, e)) {
                throw new RuntimeException("Unable to instantiate activity " + component + ": " + e.toString(), e);
            }
        }
        try {
            Application app = r.packageInfo.makeApplication(false, this.mInstrumentation);
            if (localLOGV) {
                Slog.v(TAG, "Performing launch of " + r);
            }
            if (localLOGV) {
                Slog.v(TAG, r + ": app=" + app + ", appName=" + app.getPackageName() + ", pkg=" + r.packageInfo.getPackageName() + ", comp=" + r.intent.getComponent().toShortString() + ", dir=" + r.packageInfo.getAppDir());
            }
            if (activity != null) {
                CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
                Configuration config = new Configuration(this.mCompatConfiguration);
                if (r.overrideConfig != null) {
                    config.updateFrom(r.overrideConfig);
                }
                if (DEBUG_CONFIGURATION) {
                    Slog.v(TAG, "Launching activity " + r.activityInfo.name + " with config " + config);
                }
                Window window = null;
                if (r.mPendingRemoveWindow != null && r.mPreserveWindow) {
                    window = r.mPendingRemoveWindow;
                    r.mPendingRemoveWindow = null;
                    r.mPendingRemoveWindowManager = null;
                }
                appContext.setOuterContext(activity);
                activity.attach(appContext, this, getInstrumentation(), r.token, r.ident, app, r.intent, r.activityInfo, title, r.parent, r.embeddedID, r.lastNonConfigurationInstances, config, r.referrer, r.voiceInteractor, window, r.configCallback);
                if (customIntent != null) {
                    activity.mIntent = customIntent;
                }
                r.lastNonConfigurationInstances = null;
                checkAndBlockForNetworkAccess();
                activity.mStartedActivity = false;
                int theme = r.activityInfo.getThemeResource();
                if (theme != 0) {
                    activity.setTheme(theme);
                }
                activity.mCalled = false;
                if (r.isPersistable()) {
                    this.mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
                } else {
                    this.mInstrumentation.callActivityOnCreate(activity, r.state);
                }
                if (activity.mCalled) {
                    r.activity = activity;
                    r.stopped = true;
                    if (!r.activity.mFinished) {
                        activity.performStart();
                        r.stopped = false;
                    }
                    if (!r.activity.mFinished) {
                        if (r.isPersistable()) {
                            if (!(r.state == null && r.persistentState == null)) {
                                this.mInstrumentation.callActivityOnRestoreInstanceState(activity, r.state, r.persistentState);
                            }
                        } else if (r.state != null) {
                            this.mInstrumentation.callActivityOnRestoreInstanceState(activity, r.state);
                        }
                    }
                    if (!r.activity.mFinished) {
                        activity.mCalled = false;
                        if (r.isPersistable()) {
                            this.mInstrumentation.callActivityOnPostCreate(activity, r.state, r.persistentState);
                        } else {
                            this.mInstrumentation.callActivityOnPostCreate(activity, r.state);
                        }
                        if (!activity.mCalled) {
                            throw new SuperNotCalledException("Activity " + r.intent.getComponent().toShortString() + " did not call through to super.onPostCreate()");
                        }
                    }
                }
                throw new SuperNotCalledException("Activity " + r.intent.getComponent().toShortString() + " did not call through to super.onCreate()");
            }
            r.paused = true;
            this.mActivities.put(r.token, r);
        } catch (SuperNotCalledException e2) {
            throw e2;
        } catch (Throwable e3) {
            if (!this.mInstrumentation.onException(activity, e3)) {
                throw new RuntimeException("Unable to start activity " + component + ": " + e3.toString(), e3);
            }
        }
        return activity;
    }

    private void checkAndBlockForNetworkAccess() {
        synchronized (this.mNetworkPolicyLock) {
            if (this.mNetworkBlockSeq != -1) {
                try {
                    ActivityManager.getService().waitForNetworkStateUpdate(this.mNetworkBlockSeq);
                    this.mNetworkBlockSeq = -1;
                } catch (RemoteException e) {
                }
            }
        }
    }

    private ContextImpl createBaseContextForActivity(ActivityClientRecord r) {
        try {
            ContextImpl appContext = ContextImpl.createActivityContext(this, r.packageInfo, r.activityInfo, r.token, ActivityManager.getService().getActivityDisplayId(r.token), r.overrideConfig);
            DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();
            String pkgName = SystemProperties.get("debug.second-display.pkg");
            if (pkgName == null || (pkgName.isEmpty() ^ 1) == 0 || !r.packageInfo.mPackageName.contains(pkgName)) {
                return appContext;
            }
            for (int id : dm.getDisplayIds()) {
                if (id != 0) {
                    return (ContextImpl) appContext.createDisplayContext(dm.getCompatibleDisplay(id, appContext.getResources()));
                }
            }
            return appContext;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void handleLaunchActivity(ActivityClientRecord r, Intent customIntent, String reason) {
        unscheduleGcIdler();
        this.mSomeActivitiesChanged = true;
        if (r.profilerInfo != null) {
            this.mProfiler.setProfiler(r.profilerInfo);
            this.mProfiler.startProfiling();
        }
        handleConfigurationChanged(null, null);
        if (localLOGV) {
            Slog.v(TAG, "Handling launch of " + r);
        }
        if (!ThreadedRenderer.sRendererDisabled) {
            GraphicsEnvironment.earlyInitEGL();
        }
        WindowManagerGlobal.initialize();
        if (performLaunchActivity(r, customIntent) != null) {
            boolean z;
            r.createdConfig = new Configuration(this.mConfiguration);
            reportSizeConfigurations(r);
            Bundle oldState = r.state;
            IBinder iBinder = r.token;
            boolean z2 = r.isForward;
            if (r.activity.mFinished) {
                z = false;
            } else {
                z = r.startsNotResumed ^ 1;
            }
            handleResumeActivity(iBinder, false, z2, z, r.lastProcessedSeq, reason);
            if (!r.activity.mFinished && r.startsNotResumed) {
                performPauseActivityIfNeeded(r, reason);
                if (r.isPreHoneycomb()) {
                    r.state = oldState;
                    return;
                }
                return;
            }
            return;
        }
        try {
            ActivityManager.getService().finishActivity(r.token, 0, null, 0);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    private void reportSizeConfigurations(ActivityClientRecord r) {
        Configuration[] configurations = r.activity.getResources().getSizeConfigurations();
        if (configurations != null) {
            SparseIntArray horizontal = new SparseIntArray();
            SparseIntArray vertical = new SparseIntArray();
            SparseIntArray smallest = new SparseIntArray();
            for (int i = configurations.length - 1; i >= 0; i--) {
                Configuration config = configurations[i];
                if (config.screenHeightDp != 0) {
                    vertical.put(config.screenHeightDp, 0);
                }
                if (config.screenWidthDp != 0) {
                    horizontal.put(config.screenWidthDp, 0);
                }
                if (config.smallestScreenWidthDp != 0) {
                    smallest.put(config.smallestScreenWidthDp, 0);
                }
            }
            try {
                ActivityManager.getService().reportSizeConfigurations(r.token, horizontal.copyKeys(), vertical.copyKeys(), smallest.copyKeys());
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
    }

    private void deliverNewIntents(ActivityClientRecord r, List<ReferrerIntent> intents) {
        int N = intents.size();
        for (int i = 0; i < N; i++) {
            ReferrerIntent intent = (ReferrerIntent) intents.get(i);
            intent.setExtrasClassLoader(r.activity.getClassLoader());
            intent.prepareToEnterProcess();
            r.activity.mFragments.noteStateNotSaved();
            this.mInstrumentation.callActivityOnNewIntent(r.activity, intent);
        }
    }

    void performNewIntents(IBinder token, List<ReferrerIntent> intents, boolean andPause) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r != null) {
            boolean resumed = r.paused ^ 1;
            if (resumed) {
                r.activity.mTemporaryPause = true;
                this.mInstrumentation.callActivityOnPause(r.activity);
            }
            checkAndBlockForNetworkAccess();
            deliverNewIntents(r, intents);
            if (resumed) {
                r.activity.performResume();
                r.activity.mTemporaryPause = false;
            }
            if (r.paused && andPause) {
                performResumeActivity(token, false, "performNewIntents");
                performPauseActivityIfNeeded(r, "performNewIntents");
            }
        }
    }

    private void handleNewIntent(NewIntentData data) {
        performNewIntents(data.token, data.intents, data.andPause);
    }

    public void handleRequestAssistContextExtras(RequestAssistContextExtras cmd) {
        AssistStructure structure;
        boolean forAutofill = cmd.requestType == 2;
        if (this.mLastSessionId != cmd.sessionId) {
            this.mLastSessionId = cmd.sessionId;
            for (int i = this.mLastAssistStructures.size() - 1; i >= 0; i--) {
                structure = (AssistStructure) ((WeakReference) this.mLastAssistStructures.get(i)).get();
                if (structure != null) {
                    structure.clearSendChannel();
                }
                this.mLastAssistStructures.remove(i);
            }
        }
        Bundle data = new Bundle();
        structure = null;
        AssistContent content = forAutofill ? null : new AssistContent();
        long startTime = SystemClock.uptimeMillis();
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(cmd.activityToken);
        Uri referrer = null;
        if (r != null) {
            if (!forAutofill) {
                r.activity.getApplication().dispatchOnProvideAssistData(r.activity, data);
                r.activity.onProvideAssistData(data);
                referrer = r.activity.onProvideReferrer();
            }
            if (cmd.requestType == 1 || forAutofill) {
                structure = new AssistStructure(r.activity, forAutofill, cmd.flags);
                Intent activityIntent = r.activity.getIntent();
                boolean notSecure = r.window != null ? (r.window.getAttributes().flags & 8192) == 0 : true;
                if (activityIntent == null || !notSecure) {
                    if (!forAutofill) {
                        content.setDefaultIntent(new Intent());
                    }
                } else if (!forAutofill) {
                    Intent intent = new Intent(activityIntent);
                    intent.setFlags(intent.getFlags() & -67);
                    intent.removeUnsafeExtras();
                    content.setDefaultIntent(intent);
                }
                if (!forAutofill) {
                    r.activity.onProvideAssistContent(content);
                }
            }
        }
        if (structure == null) {
            structure = new AssistStructure();
        }
        structure.setAcquisitionStartTime(startTime);
        structure.setAcquisitionEndTime(SystemClock.uptimeMillis());
        this.mLastAssistStructures.add(new WeakReference(structure));
        try {
            ActivityManager.getService().reportAssistContextExtras(cmd.requestToken, data, structure, content, referrer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void handleTranslucentConversionComplete(IBinder token, boolean drawComplete) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r != null) {
            r.activity.onTranslucentConversionComplete(drawComplete);
        }
    }

    public void onNewActivityOptions(IBinder token, ActivityOptions options) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r != null) {
            r.activity.onNewActivityOptions(options);
        }
    }

    public void handleInstallProvider(ProviderInfo info) {
        ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            installContentProviders(this.mInitialApplication, Arrays.asList(new ProviderInfo[]{info}));
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    private void handleEnterAnimationComplete(IBinder token) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r != null) {
            r.activity.dispatchEnterAnimationComplete();
        }
    }

    private void handleStartBinderTracking() {
        Binder.enableTracing();
    }

    private void handleStopBinderTrackingAndDump(ParcelFileDescriptor fd) {
        try {
            Binder.disableTracing();
            Binder.getTransactionTracker().writeTracesToFile(fd);
        } finally {
            IoUtils.closeQuietly(fd);
            Binder.getTransactionTracker().clearTraces();
        }
    }

    private void handleMultiWindowModeChanged(IBinder token, boolean isInMultiWindowMode, Configuration overrideConfig) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r != null) {
            Configuration newConfig = new Configuration(this.mConfiguration);
            if (overrideConfig != null) {
                newConfig.updateFrom(overrideConfig);
            }
            r.activity.dispatchMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        }
    }

    private void handlePictureInPictureModeChanged(IBinder token, boolean isInPipMode, Configuration overrideConfig) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r != null) {
            Configuration newConfig = new Configuration(this.mConfiguration);
            if (overrideConfig != null) {
                newConfig.updateFrom(overrideConfig);
            }
            r.activity.dispatchPictureInPictureModeChanged(isInPipMode, newConfig);
        }
    }

    private void handleLocalVoiceInteractionStarted(IBinder token, IVoiceInteractor interactor) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r != null) {
            r.voiceInteractor = interactor;
            r.activity.setVoiceInteractor(interactor);
            if (interactor == null) {
                r.activity.onLocalVoiceInteractionStopped();
            } else {
                r.activity.onLocalVoiceInteractionStarted();
            }
        }
    }

    static final void handleAttachAgent(String agent) {
        try {
            VMDebug.attachAgent(agent);
        } catch (IOException e) {
            Slog.e(TAG, "Attaching agent failed: " + agent);
        }
    }

    public static Intent getIntentBeingBroadcast() {
        return (Intent) sCurrentBroadcastIntent.get();
    }

    private void handleReceiver(ReceiverData data) {
        unscheduleGcIdler();
        String component = data.intent.getComponent().getClassName();
        LoadedApk packageInfo = getPackageInfoNoCheck(data.info.applicationInfo, data.compatInfo);
        IActivityManager mgr = ActivityManager.getService();
        try {
            Application app = packageInfo.makeApplication(false, this.mInstrumentation);
            ContextImpl context = (ContextImpl) app.getBaseContext();
            if (data.info.splitName != null) {
                context = (ContextImpl) context.createContextForSplit(data.info.splitName);
            }
            ClassLoader cl = context.getClassLoader();
            data.intent.setExtrasClassLoader(cl);
            data.intent.prepareToEnterProcess();
            data.setExtrasClassLoader(cl);
            BroadcastReceiver receiver = (BroadcastReceiver) cl.loadClass(component).newInstance();
            try {
                if (DEBUG_BROADCAST) {
                    Slog.v(TAG, "Performing receive of " + data.intent + ": app=" + app + ", appName=" + app.getPackageName() + ", pkg=" + packageInfo.getPackageName() + ", comp=" + data.intent.getComponent().toShortString() + ", dir=" + packageInfo.getAppDir());
                }
                boolean isOrder = data.getOrder();
                if (isOrder) {
                    data.setBroadcastState(data.intent.getFlags(), 2);
                }
                sCurrentBroadcastIntent.set(data.intent);
                receiver.setPendingResult(data);
                receiver.onReceive(context.getReceiverRestrictedContext(), data.intent);
                if (isOrder) {
                    data.setBroadcastState(data.intent.getFlags(), 3);
                }
                sCurrentBroadcastIntent.set(null);
            } catch (Exception e) {
                if (DEBUG_BROADCAST) {
                    Slog.i(TAG, "Finishing failed broadcast to " + data.intent.getComponent());
                }
                data.sendFinished(mgr);
                if (this.mInstrumentation.onException(receiver, e)) {
                    sCurrentBroadcastIntent.set(null);
                } else {
                    throw new RuntimeException("Unable to start receiver " + component + ": " + e.toString(), e);
                }
            } catch (Throwable th) {
                sCurrentBroadcastIntent.set(null);
                throw th;
            }
            if (receiver.getPendingResult() != null) {
                data.finish();
            }
        } catch (Exception e2) {
            if (DEBUG_BROADCAST) {
                Slog.i(TAG, "Finishing failed broadcast to " + data.intent.getComponent());
            }
            data.sendFinished(mgr);
            throw new RuntimeException("Unable to instantiate receiver " + component + ": " + e2.toString(), e2);
        }
    }

    private void handleCreateBackupAgent(CreateBackupAgentData data) {
        try {
            if (getPackageManager().getPackageInfo(data.appInfo.packageName, 0, UserHandle.myUserId()).applicationInfo.uid != Process.myUid()) {
                Slog.w(TAG, "Asked to instantiate non-matching package " + data.appInfo.packageName);
                return;
            }
            unscheduleGcIdler();
            LoadedApk packageInfo = getPackageInfoNoCheck(data.appInfo, data.compatInfo);
            String packageName = packageInfo.mPackageName;
            if (packageName == null) {
                Slog.d(TAG, "Asked to create backup agent for nonexistent package");
                return;
            }
            String classname = data.appInfo.backupAgentName;
            if (classname == null && (data.backupMode == 1 || data.backupMode == 3)) {
                classname = "android.app.backup.FullBackupAgent";
            }
            IBinder binder = null;
            try {
                BackupAgent agent = (BackupAgent) this.mBackupAgents.get(packageName);
                if (agent != null) {
                    binder = agent.onBind();
                } else {
                    agent = (BackupAgent) packageInfo.getClassLoader().loadClass(classname).newInstance();
                    ContextImpl context = ContextImpl.createAppContext(this, packageInfo);
                    context.setOuterContext(agent);
                    agent.attach(context);
                    agent.onCreate();
                    binder = agent.onBind();
                    this.mBackupAgents.put(packageName, agent);
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Exception e2) {
                Slog.e(TAG, "Agent threw during creation: " + e2);
                if (!(data.backupMode == 2 || data.backupMode == 3)) {
                    throw e2;
                }
            } catch (Exception e22) {
                throw new RuntimeException("Unable to create BackupAgent " + classname + ": " + e22.toString(), e22);
            }
            ActivityManager.getService().backupAgentCreated(packageName, binder);
        } catch (RemoteException e3) {
            throw e3.rethrowFromSystemServer();
        }
    }

    private void handleDestroyBackupAgent(CreateBackupAgentData data) {
        String packageName = getPackageInfoNoCheck(data.appInfo, data.compatInfo).mPackageName;
        BackupAgent agent = (BackupAgent) this.mBackupAgents.get(packageName);
        if (agent != null) {
            try {
                agent.onDestroy();
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown in onDestroy by backup agent of " + data.appInfo);
                e.printStackTrace();
            }
            this.mBackupAgents.remove(packageName);
            return;
        }
        Slog.w(TAG, "Attempt to destroy unknown backup agent " + data);
    }

    private void handleCreateService(CreateServiceData data) {
        unscheduleGcIdler();
        LoadedApk packageInfo = getPackageInfoNoCheck(data.info.applicationInfo, data.compatInfo);
        Context service = null;
        try {
            service = (Service) packageInfo.getClassLoader().loadClass(data.info.name).newInstance();
        } catch (Exception e) {
            if (!this.mInstrumentation.onException(service, e)) {
                throw new RuntimeException("Unable to instantiate service " + data.info.name + ": " + e.toString(), e);
            }
        }
        try {
            if (localLOGV) {
                Slog.v(TAG, "Creating service " + data.info.name);
            }
            ContextImpl context = ContextImpl.createAppContext(this, packageInfo);
            context.setOuterContext(service);
            service.attach(context, this, data.info.name, data.token, packageInfo.makeApplication(false, this.mInstrumentation), ActivityManager.getService());
            service.onCreate();
            this.mServices.put(data.token, service);
            ActivityManager.getService().serviceDoneExecuting(data.token, 0, 0, 0);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        } catch (Exception e3) {
            if (!this.mInstrumentation.onException(service, e3)) {
                throw new RuntimeException("Unable to create service " + data.info.name + ": " + e3.toString(), e3);
            }
        }
    }

    private void handleBindService(BindServiceData data) {
        Service s = (Service) this.mServices.get(data.token);
        if (DEBUG_SERVICE) {
            Slog.v(TAG, "handleBindService s=" + s + " rebind=" + data.rebind);
        }
        if (s != null) {
            try {
                data.intent.setExtrasClassLoader(s.getClassLoader());
                data.intent.prepareToEnterProcess();
                if (data.rebind) {
                    s.onRebind(data.intent);
                    ActivityManager.getService().serviceDoneExecuting(data.token, 0, 0, 0);
                } else {
                    ActivityManager.getService().publishService(data.token, data.intent, s.onBind(data.intent));
                }
                ensureJitEnabled();
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            } catch (Exception e) {
                if (!this.mInstrumentation.onException(s, e)) {
                    throw new RuntimeException("Unable to bind to service " + s + " with " + data.intent + ": " + e.toString(), e);
                }
            }
        }
    }

    private void handleUnbindService(BindServiceData data) {
        Service s = (Service) this.mServices.get(data.token);
        if (s != null) {
            try {
                data.intent.setExtrasClassLoader(s.getClassLoader());
                data.intent.prepareToEnterProcess();
                boolean doRebind = s.onUnbind(data.intent);
                if (doRebind) {
                    ActivityManager.getService().unbindFinished(data.token, data.intent, doRebind);
                } else {
                    ActivityManager.getService().serviceDoneExecuting(data.token, 0, 0, 0);
                }
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            } catch (Exception e) {
                if (!this.mInstrumentation.onException(s, e)) {
                    throw new RuntimeException("Unable to unbind to service " + s + " with " + data.intent + ": " + e.toString(), e);
                }
            }
        }
    }

    private void handleDumpService(DumpComponentInfo info) {
        ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            Service s = (Service) this.mServices.get(info.token);
            if (s != null) {
                PrintWriter pw = new FastPrintWriter(new FileOutputStream(info.fd.getFileDescriptor()));
                s.dump(info.fd.getFileDescriptor(), pw, info.args);
                pw.flush();
            }
            IoUtils.closeQuietly(info.fd);
            StrictMode.setThreadPolicy(oldPolicy);
        } catch (Throwable th) {
            IoUtils.closeQuietly(info.fd);
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    private void handleDumpActivity(DumpComponentInfo info) {
        ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(info.token);
            if (!(r == null || r.activity == null)) {
                PrintWriter pw = new FastPrintWriter(new FileOutputStream(info.fd.getFileDescriptor()));
                r.activity.dump(info.prefix, info.fd.getFileDescriptor(), pw, info.args);
                pw.flush();
            }
            IoUtils.closeQuietly(info.fd);
            StrictMode.setThreadPolicy(oldPolicy);
        } catch (Throwable th) {
            IoUtils.closeQuietly(info.fd);
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    private void handleDumpProvider(DumpComponentInfo info) {
        ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            ProviderClientRecord r = (ProviderClientRecord) this.mLocalProviders.get(info.token);
            if (!(r == null || r.mLocalProvider == null)) {
                PrintWriter pw = new FastPrintWriter(new FileOutputStream(info.fd.getFileDescriptor()));
                r.mLocalProvider.dump(info.fd.getFileDescriptor(), pw, info.args);
                pw.flush();
            }
            IoUtils.closeQuietly(info.fd);
            StrictMode.setThreadPolicy(oldPolicy);
        } catch (Throwable th) {
            IoUtils.closeQuietly(info.fd);
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    private void handleServiceArgs(ServiceArgsData data) {
        Service s = (Service) this.mServices.get(data.token);
        if (s != null) {
            try {
                int res;
                if (data.args != null) {
                    data.args.setExtrasClassLoader(s.getClassLoader());
                    data.args.prepareToEnterProcess();
                }
                if (data.taskRemoved) {
                    s.onTaskRemoved(data.args);
                    res = 1000;
                } else {
                    res = s.onStartCommand(data.args, data.flags, data.startId);
                    if (!(res == 0 || res == 1 || res == 2 || res == 3)) {
                        throw new IllegalArgumentException("Unknown service start result: " + res);
                    }
                }
                QueuedWork.waitToFinish();
                ActivityManager.getService().serviceDoneExecuting(data.token, 1, data.startId, res);
                ensureJitEnabled();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Exception e2) {
                if (!this.mInstrumentation.onException(s, e2)) {
                    throw new RuntimeException("Unable to start service " + s + " with " + data.args + ": " + e2.toString(), e2);
                }
            }
        }
    }

    private void handleStopService(IBinder token) {
        Service s = (Service) this.mServices.remove(token);
        if (s != null) {
            try {
                if (localLOGV) {
                    Slog.v(TAG, "Destroying service " + s);
                }
                s.onDestroy();
                s.detachAndCleanUp();
                Context context = s.getBaseContext();
                if (context instanceof ContextImpl) {
                    ((ContextImpl) context).scheduleFinalCleanup(s.getClassName(), "Service");
                }
                QueuedWork.waitToFinish();
                ActivityManager.getService().serviceDoneExecuting(token, 2, 0, 0);
                return;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Exception e2) {
                if (this.mInstrumentation.onException(s, e2)) {
                    Slog.i(TAG, "handleStopService: exception for " + token, e2);
                    return;
                }
                throw new RuntimeException("Unable to stop service " + s + ": " + e2.toString(), e2);
            }
        }
        Slog.i(TAG, "handleStopService: token=" + token + " not found.");
    }

    public final ActivityClientRecord performResumeActivity(IBinder token, boolean clearHide, String reason) {
        Object obj = null;
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (localLOGV) {
            String str = TAG;
            StringBuilder append = new StringBuilder().append("Performing resume of ").append(r).append(" finished= ");
            if (r != null) {
                obj = Boolean.valueOf(r.activity.mFinished);
            }
            Slog.v(str, append.append(obj).toString());
        }
        if (!(r == null || (r.activity.mFinished ^ 1) == 0)) {
            if (clearHide) {
                r.hideForNow = false;
                r.activity.mStartedActivity = false;
            }
            try {
                r.activity.onStateNotSaved();
                r.activity.mFragments.noteStateNotSaved();
                checkAndBlockForNetworkAccess();
                if (r.pendingIntents != null) {
                    deliverNewIntents(r, r.pendingIntents);
                    r.pendingIntents = null;
                }
                if (r.pendingResults != null) {
                    deliverResults(r, r.pendingResults);
                    r.pendingResults = null;
                }
                r.activity.performResume();
                synchronized (this.mResourcesManager) {
                    for (int i = this.mRelaunchingActivities.size() - 1; i >= 0; i--) {
                        ActivityClientRecord relaunching = (ActivityClientRecord) this.mRelaunchingActivities.get(i);
                        if (relaunching.token == r.token && relaunching.onlyLocalRequest && relaunching.startsNotResumed) {
                            relaunching.startsNotResumed = false;
                        }
                    }
                }
                EventLog.writeEvent(LOG_AM_ON_RESUME_CALLED, new Object[]{Integer.valueOf(UserHandle.myUserId()), r.activity.getComponentName().getClassName(), reason});
                r.paused = false;
                r.stopped = false;
                r.state = null;
                r.persistentState = null;
            } catch (Exception e) {
                if (!this.mInstrumentation.onException(r.activity, e)) {
                    throw new RuntimeException("Unable to resume activity " + r.intent.getComponent().toShortString() + ": " + e.toString(), e);
                }
            }
        }
        return r;
    }

    static final void cleanUpPendingRemoveWindows(ActivityClientRecord r, boolean force) {
        if (!r.mPreserveWindow || (force ^ 1) == 0) {
            if (r.mPendingRemoveWindow != null) {
                r.mPendingRemoveWindowManager.removeViewImmediate(r.mPendingRemoveWindow.getDecorView());
                IBinder wtoken = r.mPendingRemoveWindow.getDecorView().getWindowToken();
                if (wtoken != null) {
                    WindowManagerGlobal.getInstance().closeAll(wtoken, r.activity.getClass().getName(), "Activity");
                }
            }
            r.mPendingRemoveWindow = null;
            r.mPendingRemoveWindowManager = null;
        }
    }

    final void handleResumeActivity(IBinder token, boolean clearHide, boolean isForward, boolean reallyResume, int seq, String reason) {
        if (checkAndUpdateLifecycleSeq(seq, (ActivityClientRecord) this.mActivities.get(token), "resumeActivity")) {
            unscheduleGcIdler();
            this.mSomeActivitiesChanged = true;
            if (mHM == null) {
                mHM = HypnusManager.getHypnusManager();
            }
            if (mHM != null) {
                mHM.hypnusSetSignatureAction(11, RunningAppProcessInfo.IMPORTANCE_EMPTY, Hypnus.getLocalSignature());
            }
            ActivityClientRecord r = performResumeActivity(token, clearHide, reason);
            if (r != null) {
                LayoutParams l;
                Activity a = r.activity;
                if (localLOGV) {
                    Slog.v(TAG, "Resume " + r + " started activity: " + a.mStartedActivity + ", hideForNow: " + r.hideForNow + ", finished: " + a.mFinished);
                }
                int forwardBit = isForward ? 256 : 0;
                boolean willBeVisible = a.mStartedActivity ^ 1;
                if (!willBeVisible) {
                    try {
                        willBeVisible = ActivityManager.getService().willActivityBeVisible(a.getActivityToken());
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
                if (r.window == null && (a.mFinished ^ 1) != 0 && willBeVisible) {
                    r.window = r.activity.getWindow();
                    View decor = r.window.getDecorView();
                    decor.setVisibility(4);
                    ViewManager wm = a.getWindowManager();
                    l = r.window.getAttributes();
                    a.mDecor = decor;
                    l.type = 1;
                    l.softInputMode |= forwardBit;
                    if (r.mPreserveWindow) {
                        a.mWindowAdded = true;
                        r.mPreserveWindow = false;
                        ViewRootImpl impl = decor.getViewRootImpl();
                        if (impl != null) {
                            impl.notifyChildRebuilt();
                        }
                    }
                    if (a.mVisibleFromClient) {
                        if (a.mWindowAdded) {
                            a.onWindowAttributesChanged(l);
                        } else {
                            a.mWindowAdded = true;
                            wm.addView(decor, l);
                        }
                    }
                } else if (!willBeVisible) {
                    if (localLOGV) {
                        Slog.v(TAG, "Launch " + r + " mStartedActivity set");
                    }
                    r.hideForNow = true;
                }
                cleanUpPendingRemoveWindows(r, false);
                if (!(r.activity.mFinished || !willBeVisible || r.activity.mDecor == null || (r.hideForNow ^ 1) == 0)) {
                    if (r.newConfig != null) {
                        performConfigurationChangedForActivity(r, r.newConfig);
                        if (DEBUG_CONFIGURATION) {
                            Slog.v(TAG, "Resuming activity " + r.activityInfo.name + " with newConfig " + r.activity.mCurrentConfig);
                        }
                        r.newConfig = null;
                    }
                    if (localLOGV) {
                        Slog.v(TAG, "Resuming " + r + " with isForward=" + isForward);
                    }
                    l = r.window.getAttributes();
                    if ((l.softInputMode & 256) != forwardBit) {
                        l.softInputMode = (l.softInputMode & -257) | forwardBit;
                        if (r.activity.mVisibleFromClient) {
                            a.getWindowManager().updateViewLayout(r.window.getDecorView(), l);
                        }
                    }
                    r.activity.mVisibleFromServer = true;
                    this.mNumVisibleActivities++;
                    if (r.activity.mVisibleFromClient) {
                        r.activity.makeVisible();
                    }
                }
                if (!r.onlyLocalRequest) {
                    r.nextIdle = this.mNewActivities;
                    this.mNewActivities = r;
                    if (localLOGV) {
                        Slog.v(TAG, "Scheduling idle handler for " + r);
                    }
                    Looper.myQueue().addIdleHandler(new Idler(this, null));
                }
                r.onlyLocalRequest = false;
                if (reallyResume) {
                    try {
                        ActivityManager.getService().activityResumed(token);
                    } catch (RemoteException ex) {
                        throw ex.rethrowFromSystemServer();
                    }
                }
            }
            try {
                if (InputLog.DEBUG) {
                    InputLog.d(TAG, "Resume errors cause finishActivity", new Throwable("Kevin_DEBUG"));
                }
                ActivityManager.getService().finishActivity(token, 0, null, 0);
            } catch (RemoteException ex2) {
                throw ex2.rethrowFromSystemServer();
            }
        }
    }

    private Bitmap createThumbnailBitmap(ActivityClientRecord r) {
        Bitmap thumbnail = this.mAvailThumbnailBitmap;
        if (thumbnail == null) {
            try {
                int h;
                int w = this.mThumbnailWidth;
                if (w < 0) {
                    Resources res = r.activity.getResources();
                    w = res.getDimensionPixelSize(R.dimen.thumbnail_width);
                    this.mThumbnailWidth = w;
                    h = res.getDimensionPixelSize(R.dimen.thumbnail_height);
                    this.mThumbnailHeight = h;
                } else {
                    h = this.mThumbnailHeight;
                }
                if (w > 0 && h > 0) {
                    thumbnail = Bitmap.createBitmap(r.activity.getResources().getDisplayMetrics(), w, h, THUMBNAIL_FORMAT);
                    thumbnail.eraseColor(0);
                }
            } catch (Exception e) {
                if (this.mInstrumentation.onException(r.activity, e)) {
                    return null;
                }
                throw new RuntimeException("Unable to create thumbnail of " + r.intent.getComponent().toShortString() + ": " + e.toString(), e);
            }
        }
        if (thumbnail == null) {
            return thumbnail;
        }
        Canvas cv = this.mThumbnailCanvas;
        if (cv == null) {
            cv = new Canvas();
            this.mThumbnailCanvas = cv;
        }
        cv.setBitmap(thumbnail);
        if (!r.activity.onCreateThumbnail(thumbnail, cv)) {
            this.mAvailThumbnailBitmap = thumbnail;
            thumbnail = null;
        }
        cv.setBitmap(null);
        return thumbnail;
    }

    private void handlePauseActivity(IBinder token, boolean finished, boolean userLeaving, int configChanges, boolean dontReport, int seq) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (checkAndUpdateLifecycleSeq(seq, r, "pauseActivity") && r != null) {
            if (userLeaving) {
                performUserLeavingActivity(r);
            }
            Activity activity = r.activity;
            activity.mConfigChangeFlags |= configChanges;
            performPauseActivity(token, finished, r.isPreHoneycomb(), "handlePauseActivity");
            if (r.isPreHoneycomb()) {
                QueuedWork.waitToFinish();
            }
            if (!dontReport) {
                try {
                    ActivityManager.getService().activityPaused(token);
                } catch (RemoteException ex) {
                    throw ex.rethrowFromSystemServer();
                }
            }
            this.mSomeActivitiesChanged = true;
        }
    }

    final void performUserLeavingActivity(ActivityClientRecord r) {
        this.mInstrumentation.callActivityOnUserLeaving(r.activity);
    }

    final Bundle performPauseActivity(IBinder token, boolean finished, boolean saveState, String reason) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r != null) {
            return performPauseActivity(r, finished, saveState, reason);
        }
        return null;
    }

    final Bundle performPauseActivity(ActivityClientRecord r, boolean finished, boolean saveState, String reason) {
        ArrayList<OnActivityPausedListener> listeners;
        if (r.paused) {
            if (r.activity.mFinished) {
                return null;
            }
            RuntimeException e = new RuntimeException("Performing pause of activity that is not resumed: " + r.intent.getComponent().toShortString());
            Slog.e(TAG, e.getMessage(), e);
        }
        if (finished) {
            r.activity.mFinished = true;
        }
        if (!r.activity.mFinished && saveState) {
            callCallActivityOnSaveInstanceState(r);
            r.activity.updateSaveInstanceStateReason(1);
        }
        performPauseActivityIfNeeded(r, reason);
        synchronized (this.mOnPauseListeners) {
            listeners = (ArrayList) this.mOnPauseListeners.remove(r.activity);
        }
        int size = listeners != null ? listeners.size() : 0;
        for (int i = 0; i < size; i++) {
            ((OnActivityPausedListener) listeners.get(i)).onPaused(r.activity);
        }
        Bundle bundle = (r.activity.mFinished || !saveState) ? null : r.state;
        return bundle;
    }

    private void performPauseActivityIfNeeded(ActivityClientRecord r, String reason) {
        if (!r.paused) {
            try {
                r.activity.mCalled = false;
                this.mInstrumentation.callActivityOnPause(r.activity);
                EventLog.writeEvent(LOG_AM_ON_PAUSE_CALLED, new Object[]{Integer.valueOf(UserHandle.myUserId()), r.activity.getComponentName().getClassName(), reason});
                if (!r.activity.mCalled) {
                    throw new SuperNotCalledException("Activity " + safeToComponentShortString(r.intent) + " did not call through to super.onPause()");
                }
            } catch (SuperNotCalledException e) {
                throw e;
            } catch (Exception e2) {
                if (!this.mInstrumentation.onException(r.activity, e2)) {
                    throw new RuntimeException("Unable to pause activity " + safeToComponentShortString(r.intent) + ": " + e2.toString(), e2);
                }
            }
            r.paused = true;
        }
    }

    final void performStopActivity(IBinder token, boolean saveState, String reason) {
        performStopActivityInner((ActivityClientRecord) this.mActivities.get(token), null, false, saveState, reason);
    }

    private void performStopActivityInner(ActivityClientRecord r, StopInfo info, boolean keepShown, boolean saveState, String reason) {
        if (localLOGV) {
            Slog.v(TAG, "Performing stop of " + r);
        }
        if (r != null) {
            if (!keepShown && r.stopped) {
                if (!r.activity.mFinished) {
                    RuntimeException e = new RuntimeException("Performing stop of activity that is already stopped: " + r.intent.getComponent().toShortString());
                    Slog.e(TAG, e.getMessage(), e);
                    Slog.e(TAG, r.getStateString());
                } else {
                    return;
                }
            }
            performPauseActivityIfNeeded(r, reason);
            if (info != null) {
                try {
                    info.description = r.activity.onCreateDescription();
                } catch (Exception e2) {
                    if (!this.mInstrumentation.onException(r.activity, e2)) {
                        throw new RuntimeException("Unable to save state of activity " + r.intent.getComponent().toShortString() + ": " + e2.toString(), e2);
                    }
                }
            }
            if (!r.activity.mFinished && saveState && r.state == null) {
                callCallActivityOnSaveInstanceState(r);
                r.activity.updateSaveInstanceStateReason(2);
            }
            if (!keepShown) {
                try {
                    r.activity.performStop(false);
                } catch (Exception e22) {
                    if (!this.mInstrumentation.onException(r.activity, e22)) {
                        throw new RuntimeException("Unable to stop activity " + r.intent.getComponent().toShortString() + ": " + e22.toString(), e22);
                    }
                }
                r.stopped = true;
                EventLog.writeEvent(LOG_AM_ON_STOP_CALLED, new Object[]{Integer.valueOf(UserHandle.myUserId()), r.activity.getComponentName().getClassName(), reason});
            }
        }
    }

    private void updateVisibility(ActivityClientRecord r, boolean show) {
        View v = r.activity.mDecor;
        if (v == null) {
            return;
        }
        if (show) {
            if (!r.activity.mVisibleFromServer) {
                r.activity.mVisibleFromServer = true;
                this.mNumVisibleActivities++;
                if (r.activity.mVisibleFromClient) {
                    r.activity.makeVisible();
                }
            }
            if (r.newConfig != null) {
                performConfigurationChangedForActivity(r, r.newConfig);
                if (DEBUG_CONFIGURATION) {
                    Slog.v(TAG, "Updating activity vis " + r.activityInfo.name + " with new config " + r.activity.mCurrentConfig);
                }
                r.newConfig = null;
            }
        } else if (r.activity.mVisibleFromServer) {
            r.activity.mVisibleFromServer = false;
            this.mNumVisibleActivities--;
            v.setVisibility(4);
        }
    }

    private void handleStopActivity(IBinder token, boolean show, int configChanges, int seq) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (!checkAndUpdateLifecycleSeq(seq, r, "stopActivity")) {
            return;
        }
        if (r == null) {
            Log.w(TAG, "handleStopActivity: null record found");
            return;
        }
        Activity activity = r.activity;
        activity.mConfigChangeFlags |= configChanges;
        StopInfo info = new StopInfo();
        long timeFirst = SystemClock.uptimeMillis();
        performStopActivityInner(r, info, show, true, "handleStopActivity");
        if (localLOGV) {
            Slog.v(TAG, "Finishing stop of " + r + ": show=" + show + " win=" + r.window);
        }
        boolean update = true;
        if (!(SystemClock.uptimeMillis() - timeFirst <= STOP_TIMEOUT || r == null || (r.needInvisible ^ 1) == 0 || (show ^ 1) == 0)) {
            ComponentName componentName = r.intent != null ? r.intent.getComponent() : null;
            String name = componentName != null ? componentName.toShortString() : null;
            if (name != null && name.contains("com.oppo.camera")) {
                if (localLOGV || this.mDebugOn) {
                    Slog.d(TAG, "not do updateVisibility here " + componentName);
                }
                update = false;
            }
        }
        if (update) {
            updateVisibility(r, show);
        }
        if (!r.isPreHoneycomb()) {
            QueuedWork.waitToFinish();
        }
        info.activity = r;
        info.state = r.state;
        info.persistentState = r.persistentState;
        this.mH.post(info);
        this.mSomeActivitiesChanged = true;
    }

    private static boolean checkAndUpdateLifecycleSeq(int seq, ActivityClientRecord r, String action) {
        if (r == null) {
            return true;
        }
        if (seq < r.lastProcessedSeq) {
            return false;
        }
        r.lastProcessedSeq = seq;
        return true;
    }

    final void performRestartActivity(IBinder token) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r.stopped) {
            r.activity.performRestart();
            r.stopped = false;
        }
    }

    private void handleWindowVisibility(IBinder token, boolean show) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r == null) {
            Log.w(TAG, "handleWindowVisibility: no activity for token " + token);
            return;
        }
        if (!show && (r.stopped ^ 1) != 0) {
            performStopActivityInner(r, null, show, false, "handleWindowVisibility");
        } else if (show && r.stopped) {
            unscheduleGcIdler();
            r.activity.performRestart();
            r.stopped = false;
        }
        if (r.window == null && r.activity.mDecor == null && (r.activity.mFinished ^ 1) != 0 && show) {
            Activity a = r.activity;
            int forwardBit = r.isForward ? 256 : 0;
            r.window = r.activity.getWindow();
            View decor = r.window.getDecorView();
            if (decor != null) {
                decor.setVisibility(4);
            }
            ViewManager wm = a.getWindowManager();
            LayoutParams l = r.window.getAttributes();
            Log.w(TAG, "mDecor is null,has Add decor to it " + decor + " r= " + r);
            a.mDecor = decor;
            l.type = 1;
            l.softInputMode |= forwardBit;
        }
        if (r.activity.mDecor != null) {
            updateVisibility(r, show);
        }
        this.mSomeActivitiesChanged = true;
    }

    private void handleSleeping(IBinder token, boolean sleeping) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r == null) {
            Log.w(TAG, "handleSleeping: no activity for token " + token);
            return;
        }
        if (localLOGV) {
            Slog.d(TAG, "handleSleeping, sleeping:" + sleeping);
        }
        if (sleeping) {
            if (!(r.stopped || (r.isPreHoneycomb() ^ 1) == 0)) {
                if (!r.activity.mFinished && r.state == null) {
                    callCallActivityOnSaveInstanceState(r);
                }
                try {
                    r.activity.performStop(false);
                } catch (Exception e) {
                    if (!this.mInstrumentation.onException(r.activity, e)) {
                        throw new RuntimeException("Unable to stop activity " + r.intent.getComponent().toShortString() + ": " + e.toString(), e);
                    }
                }
                r.stopped = true;
                EventLog.writeEvent(LOG_AM_ON_STOP_CALLED, new Object[]{Integer.valueOf(UserHandle.myUserId()), r.activity.getComponentName().getClassName(), "sleeping"});
            }
            if (!r.isPreHoneycomb()) {
                QueuedWork.waitToFinish();
            }
            try {
                ActivityManager.getService().activitySlept(r.token);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
        if (localLOGV) {
            Slog.d(TAG, "handleSleeping, wakeup, r:" + r + ", r.stopped:" + r.stopped + ", mVisibleFromServer:" + r.activity.mVisibleFromServer);
        }
        if (r.stopped && r.activity.mVisibleFromServer) {
            r.activity.performRestart();
            r.stopped = false;
        }
    }

    private void handleSetCoreSettings(Bundle coreSettings) {
        synchronized (this.mResourcesManager) {
            this.mCoreSettings = coreSettings;
        }
        onCoreSettingsChange();
    }

    private void onCoreSettingsChange() {
        boolean debugViewAttributes = this.mCoreSettings.getInt("debug_view_attributes", 0) != 0;
        if (debugViewAttributes != View.mDebugViewAttributes) {
            View.mDebugViewAttributes = debugViewAttributes;
            requestRelaunchAllActivities();
        }
    }

    private void requestRelaunchAllActivities() {
        for (Entry<IBinder, ActivityClientRecord> entry : this.mActivities.entrySet()) {
            if (!((ActivityClientRecord) entry.getValue()).activity.mFinished) {
                try {
                    ActivityManager.getService().requestActivityRelaunch((IBinder) entry.getKey());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    private void handleUpdatePackageCompatibilityInfo(UpdateCompatibilityData data) {
        LoadedApk apk = peekPackageInfo(data.pkg, false);
        if (apk != null) {
            apk.setCompatibilityInfo(data.info);
        }
        apk = peekPackageInfo(data.pkg, true);
        if (apk != null) {
            apk.setCompatibilityInfo(data.info);
        }
        handleConfigurationChanged(this.mConfiguration, data.info);
        WindowManagerGlobal.getInstance().reportNewConfiguration(this.mConfiguration);
    }

    private void deliverResults(ActivityClientRecord r, List<ResultInfo> results) {
        int N = results.size();
        for (int i = 0; i < N; i++) {
            ResultInfo ri = (ResultInfo) results.get(i);
            try {
                if (ri.mData != null) {
                    ri.mData.setExtrasClassLoader(r.activity.getClassLoader());
                    ri.mData.prepareToEnterProcess();
                }
                r.activity.dispatchActivityResult(ri.mResultWho, ri.mRequestCode, ri.mResultCode, ri.mData);
            } catch (Exception e) {
                if (!this.mInstrumentation.onException(r.activity, e)) {
                    throw new RuntimeException("Failure delivering result " + ri + " to activity " + r.intent.getComponent().toShortString() + ": " + e.toString(), e);
                }
            }
        }
    }

    private void handleSendResult(ResultData res) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(res.token);
        if (r != null) {
            boolean resumed = r.paused ^ 1;
            if (!r.activity.mFinished && r.activity.mDecor != null && r.hideForNow && resumed) {
                updateVisibility(r, true);
            }
            if (resumed) {
                try {
                    r.activity.mCalled = false;
                    r.activity.mTemporaryPause = true;
                    this.mInstrumentation.callActivityOnPause(r.activity);
                    if (!r.activity.mCalled) {
                        throw new SuperNotCalledException("Activity " + r.intent.getComponent().toShortString() + " did not call through to super.onPause()");
                    }
                } catch (SuperNotCalledException e) {
                    throw e;
                } catch (Exception e2) {
                    if (!this.mInstrumentation.onException(r.activity, e2)) {
                        throw new RuntimeException("Unable to pause activity " + r.intent.getComponent().toShortString() + ": " + e2.toString(), e2);
                    }
                }
            }
            checkAndBlockForNetworkAccess();
            deliverResults(r, res.results);
            if (resumed) {
                r.activity.performResume();
                r.activity.mTemporaryPause = false;
            }
        }
    }

    public final ActivityClientRecord performDestroyActivity(IBinder token, boolean finishing) {
        return performDestroyActivity(token, finishing, 0, false);
    }

    private ActivityClientRecord performDestroyActivity(IBinder token, boolean finishing, int configChanges, boolean getNonConfigInstance) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        Class activityClass = null;
        if (localLOGV) {
            Slog.v(TAG, "Performing finish of " + r);
        }
        if (r != null) {
            activityClass = r.activity.getClass();
            Activity activity = r.activity;
            activity.mConfigChangeFlags |= configChanges;
            if (finishing) {
                r.activity.mFinished = true;
            }
            performPauseActivityIfNeeded(r, "destroy");
            if (!r.stopped) {
                try {
                    r.activity.performStop(r.mPreserveWindow);
                } catch (SuperNotCalledException e) {
                    throw e;
                } catch (Exception e2) {
                    if (!this.mInstrumentation.onException(r.activity, e2)) {
                        throw new RuntimeException("Unable to stop activity " + safeToComponentShortString(r.intent) + ": " + e2.toString(), e2);
                    }
                }
                r.stopped = true;
                EventLog.writeEvent(LOG_AM_ON_STOP_CALLED, new Object[]{Integer.valueOf(UserHandle.myUserId()), r.activity.getComponentName().getClassName(), "destroy"});
            }
            if (getNonConfigInstance) {
                try {
                    r.lastNonConfigurationInstances = r.activity.retainNonConfigurationInstances();
                } catch (Exception e22) {
                    if (!this.mInstrumentation.onException(r.activity, e22)) {
                        throw new RuntimeException("Unable to retain activity " + r.intent.getComponent().toShortString() + ": " + e22.toString(), e22);
                    }
                }
            }
            try {
                r.activity.mCalled = false;
                this.mInstrumentation.callActivityOnDestroy(r.activity);
                if (!r.activity.mCalled) {
                    throw new SuperNotCalledException("Activity " + safeToComponentShortString(r.intent) + " did not call through to super.onDestroy()");
                } else if (r.window != null) {
                    r.window.closeAllPanels();
                }
            } catch (SuperNotCalledException e3) {
                throw e3;
            } catch (Exception e222) {
                if (!this.mInstrumentation.onException(r.activity, e222)) {
                    throw new RuntimeException("Unable to destroy activity " + safeToComponentShortString(r.intent) + ": " + e222.toString(), e222);
                }
            }
        }
        this.mActivities.remove(token);
        StrictMode.decrementExpectedActivityCount(activityClass);
        return r;
    }

    private static String safeToComponentShortString(Intent intent) {
        ComponentName component = intent.getComponent();
        return component == null ? "[Unknown]" : component.toShortString();
    }

    private void handleDestroyActivity(IBinder token, boolean finishing, int configChanges, boolean getNonConfigInstance) {
        ActivityClientRecord r = performDestroyActivity(token, finishing, configChanges, getNonConfigInstance);
        if (r != null) {
            cleanUpPendingRemoveWindows(r, finishing);
            WindowManager wm = r.activity.getWindowManager();
            View v = r.activity.mDecor;
            if (v != null) {
                if (r.activity.mVisibleFromServer) {
                    this.mNumVisibleActivities--;
                }
                IBinder wtoken = v.getWindowToken();
                if (r.activity.mWindowAdded) {
                    if (r.mPreserveWindow) {
                        r.mPendingRemoveWindow = r.window;
                        r.mPendingRemoveWindowManager = wm;
                        r.window.clearContentView();
                    } else {
                        wm.removeViewImmediate(v);
                    }
                }
                if (wtoken != null && r.mPendingRemoveWindow == null) {
                    WindowManagerGlobal.getInstance().closeAll(wtoken, r.activity.getClass().getName(), "Activity");
                } else if (r.mPendingRemoveWindow != null) {
                    WindowManagerGlobal.getInstance().closeAllExceptView(token, v, r.activity.getClass().getName(), "Activity");
                }
                r.activity.mDecor = null;
            }
            if (r.mPendingRemoveWindow == null) {
                WindowManagerGlobal.getInstance().closeAll(token, r.activity.getClass().getName(), "Activity");
            }
            Context c = r.activity.getBaseContext();
            if (c instanceof ContextImpl) {
                ((ContextImpl) c).scheduleFinalCleanup(r.activity.getClass().getName(), "Activity");
            }
        }
        if (finishing) {
            try {
                ActivityManager.getService().activityDestroyed(token);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
        this.mSomeActivitiesChanged = true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:56:0x00a4  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0040 A:{SYNTHETIC, Splitter: B:24:0x0040} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0072 A:{Catch:{ RemoteException -> 0x0097, all -> 0x0091 }} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0079 A:{Catch:{ RemoteException -> 0x0097, all -> 0x0091 }} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x007d A:{Catch:{ RemoteException -> 0x0097, all -> 0x0091 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void requestRelaunchActivity(IBinder token, List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents, int configChanges, boolean notResumed, Configuration config, Configuration overrideConfig, boolean fromServer, boolean preserveWindow) {
        Throwable th;
        synchronized (this.mResourcesManager) {
            ActivityClientRecord target;
            ActivityClientRecord target2;
            int i = 0;
            while (i < this.mRelaunchingActivities.size()) {
                try {
                    ActivityClientRecord r = (ActivityClientRecord) this.mRelaunchingActivities.get(i);
                    if (r.token == token) {
                        target = r;
                        if (pendingResults != null) {
                            if (r.pendingResults != null) {
                                r.pendingResults.addAll(pendingResults);
                            } else {
                                r.pendingResults = pendingResults;
                            }
                        }
                        if (pendingNewIntents != null) {
                            if (r.pendingIntents != null) {
                                r.pendingIntents.addAll(pendingNewIntents);
                            } else {
                                r.pendingIntents = pendingNewIntents;
                            }
                        }
                        if (!r.onlyLocalRequest && fromServer) {
                            ActivityManager.getService().activityRelaunched(token);
                        }
                        target2 = target;
                        if (target2 != null) {
                            try {
                                target = new ActivityClientRecord();
                                target.token = token;
                                target.pendingResults = pendingResults;
                                target.pendingIntents = pendingNewIntents;
                                target.mPreserveWindow = preserveWindow;
                                if (!fromServer) {
                                    ActivityClientRecord existing = (ActivityClientRecord) this.mActivities.get(token);
                                    if (existing != null) {
                                        target.startsNotResumed = existing.paused;
                                        target.overrideConfig = existing.overrideConfig;
                                    }
                                    target.onlyLocalRequest = true;
                                }
                                this.mRelaunchingActivities.add(target);
                                sendMessage(126, target);
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        target = target2;
                        if (fromServer) {
                            target.startsNotResumed = notResumed;
                            target.onlyLocalRequest = false;
                        }
                        if (config != null) {
                            target.createdConfig = config;
                        }
                        if (overrideConfig != null) {
                            target.overrideConfig = overrideConfig;
                        }
                        target.pendingConfigChanges |= configChanges;
                        target.relaunchSeq = getLifecycleSeq();
                    }
                    i++;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                } catch (Throwable th3) {
                    th = th3;
                }
            }
            target2 = null;
            if (target2 != null) {
            }
            if (fromServer) {
            }
            if (config != null) {
            }
            if (overrideConfig != null) {
            }
            target.pendingConfigChanges |= configChanges;
            target.relaunchSeq = getLifecycleSeq();
        }
    }

    /* JADX WARNING: Missing block: B:14:0x0043, code:
            return;
     */
    /* JADX WARNING: Missing block: B:24:0x0081, code:
            if (r13.lastProcessedSeq <= r13.relaunchSeq) goto L_0x0152;
     */
    /* JADX WARNING: Missing block: B:25:0x0083, code:
            android.util.Slog.wtf(TAG, "For some reason target: " + r13 + " has lower sequence: " + r13.relaunchSeq + " than current sequence: " + r13.lastProcessedSeq);
     */
    /* JADX WARNING: Missing block: B:27:0x00b9, code:
            if (r13.createdConfig == null) goto L_0x00df;
     */
    /* JADX WARNING: Missing block: B:29:0x00bd, code:
            if (r12.mConfiguration == null) goto L_0x00d3;
     */
    /* JADX WARNING: Missing block: B:31:0x00c7, code:
            if (r13.createdConfig.isOtherSeqNewer(r12.mConfiguration) == false) goto L_0x00df;
     */
    /* JADX WARNING: Missing block: B:33:0x00d1, code:
            if (r12.mConfiguration.diff(r13.createdConfig) == 0) goto L_0x00df;
     */
    /* JADX WARNING: Missing block: B:34:0x00d3, code:
            if (r1 == null) goto L_0x00dd;
     */
    /* JADX WARNING: Missing block: B:36:0x00db, code:
            if (r13.createdConfig.isOtherSeqNewer(r1) == false) goto L_0x00df;
     */
    /* JADX WARNING: Missing block: B:37:0x00dd, code:
            r1 = r13.createdConfig;
     */
    /* JADX WARNING: Missing block: B:39:0x00e1, code:
            if (DEBUG_CONFIGURATION == false) goto L_0x010a;
     */
    /* JADX WARNING: Missing block: B:40:0x00e3, code:
            android.util.Slog.v(TAG, "Relaunching activity " + r13.token + ": changedConfig=" + r1);
     */
    /* JADX WARNING: Missing block: B:41:0x010a, code:
            if (r1 == null) goto L_0x0117;
     */
    /* JADX WARNING: Missing block: B:42:0x010c, code:
            r12.mCurDefaultDisplayDpi = r1.densityDpi;
            updateDefaultDensity();
            handleConfigurationChanged(r1, null);
     */
    /* JADX WARNING: Missing block: B:43:0x0117, code:
            r6 = (android.app.ActivityThread.ActivityClientRecord) r12.mActivities.get(r13.token);
     */
    /* JADX WARNING: Missing block: B:44:0x0123, code:
            if (DEBUG_CONFIGURATION == false) goto L_0x013f;
     */
    /* JADX WARNING: Missing block: B:45:0x0125, code:
            android.util.Slog.v(TAG, "Handling relaunch of " + r6);
     */
    /* JADX WARNING: Missing block: B:46:0x013f, code:
            if (r6 != null) goto L_0x015e;
     */
    /* JADX WARNING: Missing block: B:48:0x0143, code:
            if (r13.onlyLocalRequest != false) goto L_0x014e;
     */
    /* JADX WARNING: Missing block: B:50:?, code:
            android.app.ActivityManager.getService().activityRelaunched(r13.token);
     */
    /* JADX WARNING: Missing block: B:51:0x014e, code:
            return;
     */
    /* JADX WARNING: Missing block: B:55:0x0152, code:
            r13.lastProcessedSeq = r13.relaunchSeq;
     */
    /* JADX WARNING: Missing block: B:56:0x0158, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:58:0x015d, code:
            throw r4.rethrowFromSystemServer();
     */
    /* JADX WARNING: Missing block: B:59:0x015e, code:
            r8 = r6.activity;
            r8.mConfigChangeFlags |= r2;
            r6.onlyLocalRequest = r13.onlyLocalRequest;
            r6.mPreserveWindow = r13.mPreserveWindow;
            r6.lastProcessedSeq = r13.lastProcessedSeq;
            r6.relaunchSeq = r13.relaunchSeq;
            r3 = r6.activity.mIntent;
            r6.activity.mChangingConfigurations = true;
     */
    /* JADX WARNING: Missing block: B:62:0x0180, code:
            if (r6.mPreserveWindow != false) goto L_0x0186;
     */
    /* JADX WARNING: Missing block: B:64:0x0184, code:
            if (r6.onlyLocalRequest == false) goto L_0x0193;
     */
    /* JADX WARNING: Missing block: B:65:0x0186, code:
            android.view.WindowManagerGlobal.getWindowSession().prepareToReplaceWindows(r6.token, r6.onlyLocalRequest ^ 1);
     */
    /* JADX WARNING: Missing block: B:67:0x0195, code:
            if (r6.paused != false) goto L_0x01a4;
     */
    /* JADX WARNING: Missing block: B:68:0x0197, code:
            performPauseActivity(r6.token, false, r6.isPreHoneycomb(), "handleRelaunchActivity");
     */
    /* JADX WARNING: Missing block: B:70:0x01a6, code:
            if (r6.state != null) goto L_0x01bf;
     */
    /* JADX WARNING: Missing block: B:72:0x01ac, code:
            if ((r6.stopped ^ 1) == 0) goto L_0x01bf;
     */
    /* JADX WARNING: Missing block: B:74:0x01b4, code:
            if ((r6.isPreHoneycomb() ^ 1) == 0) goto L_0x01bf;
     */
    /* JADX WARNING: Missing block: B:75:0x01b6, code:
            callCallActivityOnSaveInstanceState(r6);
            r6.activity.updateSaveInstanceStateReason(3);
     */
    /* JADX WARNING: Missing block: B:76:0x01bf, code:
            handleDestroyActivity(r6.token, false, r2, true);
            r6.activity = null;
            r6.window = null;
            r6.hideForNow = false;
            r6.nextIdle = null;
     */
    /* JADX WARNING: Missing block: B:77:0x01d4, code:
            if (r13.pendingResults == null) goto L_0x01de;
     */
    /* JADX WARNING: Missing block: B:79:0x01d8, code:
            if (r6.pendingResults != null) goto L_0x0215;
     */
    /* JADX WARNING: Missing block: B:80:0x01da, code:
            r6.pendingResults = r13.pendingResults;
     */
    /* JADX WARNING: Missing block: B:82:0x01e0, code:
            if (r13.pendingIntents == null) goto L_0x01ea;
     */
    /* JADX WARNING: Missing block: B:84:0x01e4, code:
            if (r6.pendingIntents != null) goto L_0x021d;
     */
    /* JADX WARNING: Missing block: B:85:0x01e6, code:
            r6.pendingIntents = r13.pendingIntents;
     */
    /* JADX WARNING: Missing block: B:86:0x01ea, code:
            r6.startsNotResumed = r13.startsNotResumed;
            r6.overrideConfig = r13.overrideConfig;
            handleLaunchActivity(r6, r3, "handleRelaunchActivity");
     */
    /* JADX WARNING: Missing block: B:87:0x01fa, code:
            if (r13.onlyLocalRequest != false) goto L_0x020e;
     */
    /* JADX WARNING: Missing block: B:89:?, code:
            android.app.ActivityManager.getService().activityRelaunched(r6.token);
     */
    /* JADX WARNING: Missing block: B:90:0x0207, code:
            if (r6.window == null) goto L_0x020e;
     */
    /* JADX WARNING: Missing block: B:91:0x0209, code:
            r6.window.reportActivityRelaunched();
     */
    /* JADX WARNING: Missing block: B:92:0x020e, code:
            return;
     */
    /* JADX WARNING: Missing block: B:93:0x020f, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:95:0x0214, code:
            throw r4.rethrowFromSystemServer();
     */
    /* JADX WARNING: Missing block: B:96:0x0215, code:
            r6.pendingResults.addAll(r13.pendingResults);
     */
    /* JADX WARNING: Missing block: B:97:0x021d, code:
            r6.pendingIntents.addAll(r13.pendingIntents);
     */
    /* JADX WARNING: Missing block: B:98:0x0225, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:100:0x022a, code:
            throw r4.rethrowFromSystemServer();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleRelaunchActivity(ActivityClientRecord tmp) {
        unscheduleGcIdler();
        this.mSomeActivitiesChanged = true;
        Configuration changedConfig = null;
        int configChanges = 0;
        synchronized (this.mResourcesManager) {
            int N = this.mRelaunchingActivities.size();
            IBinder token = tmp.token;
            tmp = null;
            int i = 0;
            while (i < N) {
                ActivityClientRecord r = (ActivityClientRecord) this.mRelaunchingActivities.get(i);
                if (r.token == token) {
                    tmp = r;
                    configChanges |= r.pendingConfigChanges;
                    this.mRelaunchingActivities.remove(i);
                    i--;
                    N--;
                }
                i++;
            }
            if (tmp != null) {
                if (DEBUG_CONFIGURATION) {
                    Slog.v(TAG, "Relaunching activity " + tmp.token + " with configChanges=0x" + Integer.toHexString(configChanges));
                }
                if (this.mPendingConfiguration != null) {
                    changedConfig = this.mPendingConfiguration;
                    this.mPendingConfiguration = null;
                }
            } else if (DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Abort, activity not relaunching!");
            }
        }
    }

    private void callCallActivityOnSaveInstanceState(ActivityClientRecord r) {
        r.state = new Bundle();
        r.state.setAllowFds(false);
        if (r.isPersistable()) {
            r.persistentState = new PersistableBundle();
            this.mInstrumentation.callActivityOnSaveInstanceState(r.activity, r.state, r.persistentState);
            return;
        }
        this.mInstrumentation.callActivityOnSaveInstanceState(r.activity, r.state);
    }

    ArrayList<ComponentCallbacks2> collectComponentCallbacks(boolean allActivities, Configuration newConfig) {
        int i;
        ArrayList<ComponentCallbacks2> callbacks = new ArrayList();
        synchronized (this.mResourcesManager) {
            int NAPP = this.mAllApplications.size();
            for (i = 0; i < NAPP; i++) {
                callbacks.add((ComponentCallbacks2) this.mAllApplications.get(i));
            }
            int NACT = this.mActivities.size();
            for (i = 0; i < NACT; i++) {
                ActivityClientRecord ar = (ActivityClientRecord) this.mActivities.valueAt(i);
                Activity a = ar.activity;
                if (a != null) {
                    Configuration thisConfig = applyConfigCompatMainThread(this.mCurDefaultDisplayDpi, newConfig, ar.packageInfo.getCompatibilityInfo());
                    if (!ar.activity.mFinished && (allActivities || (ar.paused ^ 1) != 0)) {
                        callbacks.add(a);
                    } else if (thisConfig != null) {
                        if (DEBUG_CONFIGURATION) {
                            Slog.v(TAG, "Setting activity " + ar.activityInfo.name + " newConfig=" + thisConfig);
                        }
                        ar.newConfig = thisConfig;
                    }
                }
            }
            int NSVC = this.mServices.size();
            for (i = 0; i < NSVC; i++) {
                callbacks.add((ComponentCallbacks2) this.mServices.valueAt(i));
            }
        }
        synchronized (this.mProviderMap) {
            int NPRV = this.mLocalProviders.size();
            for (i = 0; i < NPRV; i++) {
                callbacks.add(((ProviderClientRecord) this.mLocalProviders.valueAt(i)).mLocalProvider);
            }
        }
        return callbacks;
    }

    private void performConfigurationChangedForActivity(ActivityClientRecord r, Configuration newBaseConfig) {
        performConfigurationChangedForActivity(r, newBaseConfig, r.activity.getDisplay().getDisplayId(), false);
    }

    private Configuration performConfigurationChangedForActivity(ActivityClientRecord r, Configuration newBaseConfig, int displayId, boolean movedToDifferentDisplay) {
        r.tmpConfig.setTo(newBaseConfig);
        if (r.overrideConfig != null) {
            r.tmpConfig.updateFrom(r.overrideConfig);
        }
        Configuration reportedConfig = performActivityConfigurationChanged(r.activity, r.tmpConfig, r.overrideConfig, displayId, movedToDifferentDisplay);
        freeTextLayoutCachesIfNeeded(r.activity.mCurrentConfig.diff(r.tmpConfig));
        return reportedConfig;
    }

    private static Configuration createNewConfigAndUpdateIfNotNull(Configuration base, Configuration override) {
        if (override == null) {
            return base;
        }
        Configuration newConfig = new Configuration(base);
        newConfig.updateFrom(override);
        return newConfig;
    }

    private void performConfigurationChanged(ComponentCallbacks2 cb, Configuration newConfig) {
        Configuration contextThemeWrapperOverrideConfig = null;
        if (cb instanceof ContextThemeWrapper) {
            contextThemeWrapperOverrideConfig = ((ContextThemeWrapper) cb).getOverrideConfiguration();
        }
        cb.onConfigurationChanged(createNewConfigAndUpdateIfNotNull(newConfig, contextThemeWrapperOverrideConfig));
    }

    private Configuration performActivityConfigurationChanged(Activity activity, Configuration newConfig, Configuration amOverrideConfig, int displayId, boolean movedToDifferentDisplay) {
        if (activity == null) {
            throw new IllegalArgumentException("No activity provided.");
        }
        IBinder activityToken = activity.getActivityToken();
        if (activityToken == null) {
            throw new IllegalArgumentException("Activity token not set. Is the activity attached?");
        }
        boolean shouldChangeConfig = false;
        if (activity.mCurrentConfig == null) {
            shouldChangeConfig = true;
        } else {
            int diff = activity.mCurrentConfig.diffPublicOnly(newConfig);
            if (DEBUG_CONFIGURATION) {
                Slog.v(TAG, "diff : " + diff);
            }
            if (!(diff == 0 && (this.mResourcesManager.isSameResourcesOverrideConfig(activityToken, amOverrideConfig) ^ 1) == 0) && (!this.mUpdatingSystemConfig || ((~activity.mActivityInfo.getRealConfigChanged()) & diff) == 0)) {
                shouldChangeConfig = true;
            }
        }
        if (!shouldChangeConfig && (movedToDifferentDisplay ^ 1) != 0) {
            return null;
        }
        Configuration contextThemeWrapperOverrideConfig = activity.getOverrideConfiguration();
        this.mResourcesManager.updateResourcesForActivity(activityToken, createNewConfigAndUpdateIfNotNull(amOverrideConfig, contextThemeWrapperOverrideConfig), displayId, movedToDifferentDisplay);
        activity.mConfigChangeFlags = 0;
        activity.mCurrentConfig = new Configuration(newConfig);
        Configuration configToReport = createNewConfigAndUpdateIfNotNull(newConfig, contextThemeWrapperOverrideConfig);
        if (movedToDifferentDisplay) {
            activity.dispatchMovedToDisplay(displayId, configToReport);
        }
        if (shouldChangeConfig) {
            activity.mCalled = false;
            activity.onConfigurationChanged(configToReport);
            if (!activity.mCalled) {
                throw new SuperNotCalledException("Activity " + activity.getLocalClassName() + " did not call through to super.onConfigurationChanged()");
            }
        }
        return configToReport;
    }

    public final void applyConfigurationToResources(Configuration config) {
        synchronized (this.mResourcesManager) {
            this.mResourcesManager.applyConfigurationToResourcesLocked(config, null);
        }
    }

    final Configuration applyCompatConfiguration(int displayDensity) {
        Configuration config = this.mConfiguration;
        if (this.mCompatConfiguration == null) {
            this.mCompatConfiguration = new Configuration();
        }
        this.mCompatConfiguration.setTo(this.mConfiguration);
        if (this.mResourcesManager.applyCompatConfigurationLocked(displayDensity, this.mCompatConfiguration)) {
            return this.mCompatConfiguration;
        }
        return config;
    }

    /* JADX WARNING: Missing block: B:41:0x00e7, code:
            r4 = collectComponentCallbacks(false, r17);
            freeTextLayoutCachesIfNeeded(r6);
     */
    /* JADX WARNING: Missing block: B:42:0x00f3, code:
            if (r4 == null) goto L_0x012e;
     */
    /* JADX WARNING: Missing block: B:43:0x00f5, code:
            r2 = r4.size();
            r8 = 0;
     */
    /* JADX WARNING: Missing block: B:44:0x00fa, code:
            if (r8 >= r2) goto L_0x012e;
     */
    /* JADX WARNING: Missing block: B:45:0x00fc, code:
            r5 = (android.content.ComponentCallbacks2) r4.get(r8);
     */
    /* JADX WARNING: Missing block: B:46:0x0104, code:
            if ((r5 instanceof android.app.Activity) == false) goto L_0x0124;
     */
    /* JADX WARNING: Missing block: B:47:0x0106, code:
            performConfigurationChangedForActivity((android.app.ActivityThread.ActivityClientRecord) r16.mActivities.get(((android.app.Activity) r5).getActivityToken()), r17);
     */
    /* JADX WARNING: Missing block: B:48:0x011e, code:
            r8 = r8 + 1;
     */
    /* JADX WARNING: Missing block: B:52:0x0124, code:
            if (r7 != false) goto L_0x011e;
     */
    /* JADX WARNING: Missing block: B:53:0x0126, code:
            performConfigurationChanged(r5, r17);
     */
    /* JADX WARNING: Missing block: B:54:0x012e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void handleConfigurationChanged(Configuration config, CompatibilityInfo compat) {
        boolean equivalent = (config == null || this.mConfiguration == null) ? false : this.mConfiguration.diffPublicOnly(config) == 0;
        ContextImpl systemContext = getSystemContext();
        synchronized (this.mResourcesManager) {
            if (this.mPendingConfiguration != null) {
                if (!this.mPendingConfiguration.isOtherSeqNewer(config)) {
                    config = this.mPendingConfiguration;
                    this.mCurDefaultDisplayDpi = config.densityDpi;
                    updateDefaultDensity();
                }
                this.mPendingConfiguration = null;
            }
            if (config == null) {
                return;
            }
            if (DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Handle configuration changed: " + config);
            }
            this.mResourcesManager.applyConfigurationToResourcesLocked(config, compat);
            updateLocaleListFromAppContext(this.mInitialApplication.getApplicationContext(), this.mResourcesManager.getConfiguration().getLocales());
            if (this.mConfiguration == null) {
                this.mConfiguration = new Configuration();
            }
            if (this.mConfiguration.isOtherSeqNewer(config) || compat != null) {
                int configDiff = this.mConfiguration.updateFrom(config);
                config = applyCompatConfiguration(this.mCurDefaultDisplayDpi);
                Theme systemTheme = systemContext.getTheme();
                if ((systemTheme.getChangingConfigurations() & configDiff) != 0) {
                    systemTheme.rebase();
                }
                Theme systemUiTheme = getSystemUiContext().getTheme();
                if ((systemUiTheme.getChangingConfigurations() & configDiff) != 0) {
                    systemUiTheme.rebase();
                }
            }
        }
    }

    void handleApplicationInfoChanged(ApplicationInfo ai) {
        LoadedApk apk;
        LoadedApk resApk;
        ArrayList<String> oldPaths;
        synchronized (this.mResourcesManager) {
            WeakReference<LoadedApk> ref = (WeakReference) this.mPackages.get(ai.packageName);
            apk = ref != null ? (LoadedApk) ref.get() : null;
            ref = (WeakReference) this.mResourcePackages.get(ai.packageName);
            resApk = ref != null ? (LoadedApk) ref.get() : null;
        }
        if (apk != null) {
            oldPaths = new ArrayList();
            LoadedApk.makePaths(this, apk.getApplicationInfo(), oldPaths);
            apk.updateApplicationInfo(ai, oldPaths);
        }
        if (resApk != null) {
            oldPaths = new ArrayList();
            LoadedApk.makePaths(this, resApk.getApplicationInfo(), oldPaths);
            resApk.updateApplicationInfo(ai, oldPaths);
        }
        synchronized (this.mResourcesManager) {
            this.mResourcesManager.applyNewResourceDirsLocked(ai.sourceDir, ai.resourceDirs);
        }
        ApplicationPackageManager.configurationChanged();
        Configuration newConfig = new Configuration();
        newConfig.assetsSeq = (this.mConfiguration != null ? this.mConfiguration.assetsSeq : 0) + 1;
        handleConfigurationChanged(newConfig, null);
        requestRelaunchAllActivities();
    }

    static void freeTextLayoutCachesIfNeeded(int configDiff) {
        if (configDiff != 0) {
            if ((configDiff & 4) != 0) {
                Canvas.freeTextLayoutCaches();
                if (DEBUG_CONFIGURATION) {
                    Slog.v(TAG, "Cleared TextLayout Caches");
                }
            }
        }
    }

    void handleActivityConfigurationChanged(ActivityConfigChangeData data, int displayId) {
        Object obj = null;
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(data.activityToken);
        if (DEBUG_CONFIGURATION) {
            String str = TAG;
            StringBuilder append = new StringBuilder().append("r : ").append(r).append(" r.activity ");
            if (r != null) {
                obj = r.activity;
            }
            Slog.v(str, append.append(obj).toString());
        }
        if (r == null || r.activity == null) {
            if (DEBUG_CONFIGURATION) {
                Slog.w(TAG, "Not found target activity to report to: " + r);
            }
            return;
        }
        boolean movedToDifferentDisplay = displayId != -1 ? displayId != r.activity.getDisplay().getDisplayId() : false;
        r.overrideConfig = data.overrideConfig;
        ViewRootImpl viewRoot = r.activity.mDecor != null ? r.activity.mDecor.getViewRootImpl() : null;
        if (movedToDifferentDisplay) {
            if (DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Handle activity moved to display, activity:" + r.activityInfo.name + ", displayId=" + displayId + ", config=" + data.overrideConfig);
            }
            Configuration reportedConfig = performConfigurationChangedForActivity(r, this.mCompatConfiguration, displayId, true);
            if (viewRoot != null) {
                viewRoot.onMovedToDisplay(displayId, reportedConfig);
            }
        } else {
            if (DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Handle activity config changed: " + r.activityInfo.name + ", config=" + data.overrideConfig);
            }
            performConfigurationChangedForActivity(r, this.mCompatConfiguration);
        }
        if (viewRoot != null) {
            viewRoot.updateConfiguration(displayId);
        }
        this.mSomeActivitiesChanged = true;
    }

    final void handleProfilerControl(boolean start, ProfilerInfo profilerInfo, int profileType) {
        if (start) {
            try {
                this.mProfiler.setProfiler(profilerInfo);
                this.mProfiler.startProfiling();
            } catch (RuntimeException e) {
                Slog.w(TAG, "Profiling failed on path " + profilerInfo.profileFile + " -- can the process access this path?");
            } finally {
                profilerInfo.closeFd();
            }
            return;
        }
        this.mProfiler.stopProfiling();
    }

    public void stopProfiling() {
        if (this.mProfiler != null) {
            this.mProfiler.stopProfiling();
        }
    }

    static void handleDumpHeap(DumpHeapData dhd) {
        if (dhd.runGc) {
            System.gc();
            System.runFinalization();
            System.gc();
        }
        if (dhd.managed) {
            try {
                Debug.dumpHprofData(dhd.path, dhd.fd.getFileDescriptor());
                try {
                    dhd.fd.close();
                } catch (IOException e) {
                    Slog.w(TAG, "Failure closing profile fd", e);
                }
            } catch (IOException e2) {
                Slog.w(TAG, "Managed heap dump failed on path " + dhd.path + " -- can the process access this path?");
                try {
                    dhd.fd.close();
                } catch (IOException e3) {
                    Slog.w(TAG, "Failure closing profile fd", e3);
                }
            } catch (Throwable th) {
                try {
                    dhd.fd.close();
                } catch (IOException e32) {
                    Slog.w(TAG, "Failure closing profile fd", e32);
                }
                throw th;
            }
        } else if (dhd.mallocInfo) {
            Debug.dumpNativeMallocInfo(dhd.fd.getFileDescriptor());
        } else {
            Debug.dumpNativeHeap(dhd.fd.getFileDescriptor());
        }
        try {
            ActivityManager.getService().dumpHeapFinished(dhd.path);
        } catch (RemoteException e4) {
            throw e4.rethrowFromSystemServer();
        }
    }

    /* JADX WARNING: Missing block: B:27:0x0063, code:
            monitor-exit(r14);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void handleDispatchPackageBroadcast(int cmd, String[] packages) {
        boolean hasPkgInfo = false;
        ResourcesManager resourcesManager;
        int i;
        WeakReference<LoadedApk> ref;
        switch (cmd) {
            case 0:
            case 2:
                boolean killApp = cmd == 0;
                if (packages != null) {
                    resourcesManager = this.mResourcesManager;
                    synchronized (resourcesManager) {
                        for (i = packages.length - 1; i >= 0; i--) {
                            if (!hasPkgInfo) {
                                ref = (WeakReference) this.mPackages.get(packages[i]);
                                if (ref == null || ref.get() == null) {
                                    ref = (WeakReference) this.mResourcePackages.get(packages[i]);
                                    if (!(ref == null || ref.get() == null)) {
                                        hasPkgInfo = true;
                                    }
                                } else {
                                    hasPkgInfo = true;
                                }
                            }
                            if (killApp) {
                                this.mPackages.remove(packages[i]);
                                this.mResourcePackages.remove(packages[i]);
                            }
                        }
                    }
                }
                break;
            case 3:
                if (packages != null) {
                    resourcesManager = this.mResourcesManager;
                    synchronized (resourcesManager) {
                        for (i = packages.length - 1; i >= 0; i--) {
                            ref = (WeakReference) this.mPackages.get(packages[i]);
                            LoadedApk pkgInfo = ref != null ? (LoadedApk) ref.get() : null;
                            if (pkgInfo != null) {
                                hasPkgInfo = true;
                            } else {
                                ref = (WeakReference) this.mResourcePackages.get(packages[i]);
                                pkgInfo = ref != null ? (LoadedApk) ref.get() : null;
                                if (pkgInfo != null) {
                                    hasPkgInfo = true;
                                }
                            }
                            if (pkgInfo != null) {
                                try {
                                    String packageName = packages[i];
                                    ApplicationInfo aInfo = sPackageManager.getApplicationInfo(packageName, 1024, UserHandle.myUserId());
                                    if (this.mActivities.size() > 0) {
                                        for (ActivityClientRecord ar : this.mActivities.values()) {
                                            if (ar.activityInfo.applicationInfo.packageName.equals(packageName)) {
                                                ar.activityInfo.applicationInfo = aInfo;
                                                ar.packageInfo = pkgInfo;
                                            }
                                        }
                                    }
                                    pkgInfo.updateApplicationInfo(aInfo, sPackageManager.getPreviousCodePaths(packageName));
                                } catch (RemoteException e) {
                                }
                            }
                        }
                    }
                }
                break;
        }
        ApplicationPackageManager.handlePackageBroadcast(cmd, packages, hasPkgInfo);
    }

    final void handleLowMemory() {
        ArrayList<ComponentCallbacks2> callbacks = collectComponentCallbacks(true, null);
        int N = callbacks.size();
        for (int i = 0; i < N; i++) {
            ((ComponentCallbacks2) callbacks.get(i)).onLowMemory();
        }
        if (Process.myUid() != 1000) {
            EventLog.writeEvent(SQLITE_MEM_RELEASED_EVENT_LOG_TAG, SQLiteDatabase.releaseMemory());
        }
        Canvas.freeCaches();
        Canvas.freeTextLayoutCaches();
        BinderInternal.forceGc("mem");
    }

    final void handleTrimMemory(int level) {
        if (DEBUG_MEMORY_TRIM) {
            Slog.v(TAG, "Trimming memory to level: " + level);
        }
        if (level < 15 || this.mBoundApplication == null || this.mBoundApplication.appInfo == null || (this.mBoundApplication.appInfo.privateFlags & 131072) == 0) {
            ArrayList<ComponentCallbacks2> callbacks = collectComponentCallbacks(true, null);
            int N = callbacks.size();
            for (int i = 0; i < N; i++) {
                ((ComponentCallbacks2) callbacks.get(i)).onTrimMemory(level);
            }
            WindowManagerGlobal.getInstance().trimMemory(level);
            return;
        }
        if (DEBUG_MEMORY_TRIM) {
            Slog.d(TAG, "skip Trimming memory, me=" + this.mBoundApplication.processName + ", level=" + level);
        }
    }

    private void setupGraphicsSupport(Context context) {
        Trace.traceBegin(64, "setupGraphicsSupport");
        if (!"android".equals(context.getPackageName())) {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null) {
                System.setProperty("java.io.tmpdir", cacheDir.getAbsolutePath());
            } else {
                Log.v(TAG, "Unable to initialize \"java.io.tmpdir\" property due to missing cache directory");
            }
            File codeCacheDir = context.createDeviceProtectedStorageContext().getCodeCacheDir();
            if (codeCacheDir != null) {
                try {
                    if (getPackageManager().getPackagesForUid(Process.myUid()) != null) {
                        ThreadedRenderer.setupDiskCache(codeCacheDir);
                        RenderScriptCacheDir.setupDiskCache(codeCacheDir);
                    }
                } catch (RemoteException e) {
                    Trace.traceEnd(64);
                    throw e.rethrowFromSystemServer();
                }
            }
            Log.w(TAG, "Unable to use shader/script cache: missing code-cache directory");
        }
        GraphicsEnvironment.chooseDriver(context);
        Trace.traceEnd(64);
    }

    private void updateDefaultDensity() {
        int densityDpi = this.mCurDefaultDisplayDpi;
        if (!this.mDensityCompatMode && densityDpi != 0 && densityDpi != DisplayMetrics.DENSITY_DEVICE) {
            DisplayMetrics.DENSITY_DEVICE = densityDpi;
            Bitmap.setDefaultDensity(densityDpi);
        }
    }

    private String getInstrumentationLibrary(ApplicationInfo appInfo, InstrumentationInfo insInfo) {
        if (!(appInfo.primaryCpuAbi == null || appInfo.secondaryCpuAbi == null)) {
            String secondaryIsa = VMRuntime.getInstructionSet(appInfo.secondaryCpuAbi);
            String secondaryDexCodeIsa = SystemProperties.get("ro.dalvik.vm.isa." + secondaryIsa);
            if (!secondaryDexCodeIsa.isEmpty()) {
                secondaryIsa = secondaryDexCodeIsa;
            }
            if (VMRuntime.getRuntime().vmInstructionSet().equals(secondaryIsa)) {
                return insInfo.secondaryNativeLibraryDir;
            }
        }
        return insInfo.nativeLibraryDir;
    }

    private void updateLocaleListFromAppContext(Context context, LocaleList newLocaleList) {
        Locale bestLocale;
        try {
            bestLocale = context.getResources().getConfiguration().getLocales().get(0);
        } catch (RuntimeException e) {
            Log.e(TAG, "app get preferred locale failed" + e);
            bestLocale = Locale.getDefault();
        }
        int newLocaleListSize = newLocaleList.size();
        for (int i = 0; i < newLocaleListSize; i++) {
            if (bestLocale.equals(newLocaleList.get(i))) {
                LocaleList.setDefault(newLocaleList, i);
                return;
            }
        }
        LocaleList.setDefault(new LocaleList(bestLocale, newLocaleList));
    }

    /* JADX WARNING: Removed duplicated region for block: B:182:0x063e A:{Splitter: B:51:0x01e2, ExcHandler: java.lang.NoSuchFieldException (e java.lang.NoSuchFieldException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleBindApplication(AppBindData data) {
        InstrumentationInfo ii;
        long st_bindApp = SystemClock.uptimeMillis();
        BoostFramework boostFramework = null;
        if (!(this.enable_uxe == 0 || (Process.isIsolated() ^ 1) == 0)) {
            boostFramework = new BoostFramework();
        }
        VMRuntime.registerSensitiveThread();
        if (data.trackAllocation) {
            DdmVmInternal.enableRecentAllocations(true);
        }
        if ((data.appInfo.privateFlags & 524288) != 0) {
            VMRuntime.getRuntime();
            VMRuntime.setVMRuntimeFlag(1);
        }
        Process.setStartTimes(SystemClock.elapsedRealtime(), SystemClock.uptimeMillis());
        this.mBoundApplication = data;
        this.mConfiguration = new Configuration(data.config);
        this.mCompatConfiguration = new Configuration(data.config);
        this.mProfiler = new Profiler();
        if (data.initProfilerInfo != null) {
            this.mProfiler.profileFile = data.initProfilerInfo.profileFile;
            this.mProfiler.profileFd = data.initProfilerInfo.profileFd;
            this.mProfiler.samplingInterval = data.initProfilerInfo.samplingInterval;
            this.mProfiler.autoStopProfiler = data.initProfilerInfo.autoStopProfiler;
            this.mProfiler.streamingOutput = data.initProfilerInfo.streamingOutput;
        }
        Process.setArgV0(data.processName);
        DdmHandleAppName.setAppName(data.processName, UserHandle.myUserId());
        if (this.mProfiler.profileFd != null) {
            this.mProfiler.startProfiling();
        }
        if (data.appInfo.targetSdkVersion <= 12) {
            AsyncTask.setDefaultExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        Message.updateCheckRecycle(data.appInfo.targetSdkVersion);
        TimeZone.setDefault(null);
        LocaleList.setDefault(data.config.getLocales());
        synchronized (this.mResourcesManager) {
            this.mResourcesManager.applyConfigurationToResourcesLocked(data.config, data.compatInfo);
            this.mCurDefaultDisplayDpi = data.config.densityDpi;
            applyCompatConfiguration(this.mCurDefaultDisplayDpi);
        }
        data.info = getPackageInfoNoCheck(data.appInfo, data.compatInfo);
        if ((data.appInfo.flags & 8192) == 0) {
            this.mDensityCompatMode = true;
            Bitmap.setDefaultDensity(160);
        } else {
            int overrideDensity = data.appInfo.getOverrideDensity();
            if (overrideDensity != 0) {
                Log.d(TAG, "override app density from " + DisplayMetrics.DENSITY_DEVICE + " to " + overrideDensity);
                this.mDensityCompatMode = true;
                Bitmap.setDefaultDensity(overrideDensity);
            }
        }
        updateDefaultDensity();
        String use24HourSetting = this.mCoreSettings.getString("time_12_24");
        Boolean is24Hr = null;
        if (use24HourSetting != null) {
            is24Hr = "24".equals(use24HourSetting) ? Boolean.TRUE : Boolean.FALSE;
        }
        DateFormat.set24HourTimePref(is24Hr);
        View.mDebugViewAttributes = this.mCoreSettings.getInt("debug_view_attributes", 0) != 0;
        if ((data.appInfo.flags & 129) != 0) {
            StrictMode.conditionallyEnableDebugLogging();
        }
        if (data.appInfo.targetSdkVersion >= 11) {
            StrictMode.enableDeathOnNetwork();
        }
        if (!(data.appInfo.targetSdkVersion < 24 || data.info == null || data.info.getPackageName() == null)) {
            if (inCptWhiteList(692, data.info.getPackageName())) {
                Slog.v(TAG, "disableDeathOnFileUriExposure " + data.info.getPackageName());
                StrictMode.disableDeathOnFileUriExposure();
            } else {
                StrictMode.enableDeathOnFileUriExposure();
            }
        }
        try {
            Field field = Build.class.getDeclaredField("SERIAL");
            field.setAccessible(true);
            field.set(Build.class, data.buildSerial);
        } catch (NoSuchFieldException e) {
        }
        if (data.debugMode != 0) {
            Debug.changeDebugPort(8100);
            if (data.debugMode == 2) {
                Slog.w(TAG, "Application " + data.info.getPackageName() + " is waiting for the debugger on port 8100...");
                IActivityManager mgr = ActivityManager.getService();
                try {
                    mgr.showWaitingForDebugger(this.mAppThread, true);
                    Debug.waitForDebugger();
                    try {
                        mgr.showWaitingForDebugger(this.mAppThread, false);
                    } catch (RemoteException ex) {
                        throw ex.rethrowFromSystemServer();
                    }
                } catch (RemoteException ex2) {
                    throw ex2.rethrowFromSystemServer();
                }
            }
            Slog.w(TAG, "Application " + data.info.getPackageName() + " can be debugged on port 8100...");
        }
        boolean isAppDebuggable = (data.appInfo.flags & 2) != 0;
        Trace.setAppTracingAllowed(isAppDebuggable);
        if (isAppDebuggable && data.enableBinderTracking) {
            Binder.enableTracing();
        }
        Trace.traceBegin(64, "Setup proxies");
        IBinder b = ServiceManager.getService(Context.CONNECTIVITY_SERVICE);
        if (b != null) {
            try {
                Proxy.setHttpProxySystemProperty(IConnectivityManager.Stub.asInterface(b).getProxyForNetwork(null));
            } catch (RemoteException e2) {
                Trace.traceEnd(64);
                throw e2.rethrowFromSystemServer();
            }
        }
        Trace.traceEnd(64);
        if (data.instrumentationName != null) {
            try {
                ii = new ApplicationPackageManager(null, getPackageManager()).getInstrumentationInfo(data.instrumentationName, 0);
                this.mInstrumentationPackageName = ii.packageName;
                this.mInstrumentationAppDir = ii.sourceDir;
                this.mInstrumentationSplitAppDirs = ii.splitSourceDirs;
                this.mInstrumentationLibDir = getInstrumentationLibrary(data.appInfo, ii);
                this.mInstrumentedAppDir = data.info.getAppDir();
                this.mInstrumentedSplitAppDirs = data.info.getSplitAppDirs();
                this.mInstrumentedLibDir = data.info.getLibDir();
            } catch (NameNotFoundException e3) {
                throw new RuntimeException("Unable to find instrumentation info for: " + data.instrumentationName);
            }
        }
        ii = null;
        Context appContext = ContextImpl.createAppContext(this, data.info);
        updateLocaleListFromAppContext(appContext, this.mResourcesManager.getConfiguration().getLocales());
        if (!Process.isIsolated()) {
            setupGraphicsSupport(appContext);
        }
        if (SystemProperties.getBoolean("dalvik.vm.usejitprofiles", false)) {
            BaseDexClassLoader.setReporter(DexLoadReporter.getInstance());
        }
        Trace.traceBegin(64, "NetworkSecurityConfigProvider.install");
        NetworkSecurityConfigProvider.install(appContext);
        Trace.traceEnd(64);
        if (ii != null) {
            ApplicationInfo instrApp = new ApplicationInfo();
            ii.copyTo(instrApp);
            instrApp.initForUser(UserHandle.myUserId());
            ContextImpl instrContext = ContextImpl.createAppContext(this, getPackageInfo(instrApp, data.compatInfo, appContext.getClassLoader(), false, true, false));
            try {
                this.mInstrumentation = (Instrumentation) instrContext.getClassLoader().loadClass(data.instrumentationName.getClassName()).newInstance();
                this.mInstrumentation.init(this, instrContext, appContext, new ComponentName(ii.packageName, ii.name), data.instrumentationWatcher, data.instrumentationUiAutomationConnection);
                if (!(this.mProfiler.profileFile == null || (ii.handleProfiling ^ 1) == 0 || this.mProfiler.profileFd != null)) {
                    this.mProfiler.handlingProfiling = true;
                    File file = new File(this.mProfiler.profileFile);
                    file.getParentFile().mkdirs();
                    Debug.startMethodTracing(file.toString(), 8388608);
                }
            } catch (Throwable e4) {
                throw new RuntimeException("Unable to instantiate instrumentation " + data.instrumentationName + ": " + e4.toString(), e4);
            }
        }
        this.mInstrumentation = new Instrumentation();
        if ((data.appInfo.flags & 1048576) != 0) {
            VMRuntime.getRuntime().clearGrowthLimit();
        } else {
            VMRuntime.getRuntime().clampGrowthLimit();
        }
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
        ThreadPolicy writesAllowedPolicy = StrictMode.getThreadPolicy();
        Application app;
        try {
            app = data.info.makeApplication(data.restrictedBackupMode, null);
            this.mInitialApplication = app;
            if (!(data.restrictedBackupMode || ArrayUtils.isEmpty(data.providers))) {
                installContentProviders(app, data.providers);
                this.mH.sendEmptyMessageDelayed(132, JobInfo.MIN_BACKOFF_MILLIS);
            }
            this.mInstrumentation.onCreate(data.instrumentationArgs);
            this.mInstrumentation.callApplicationOnCreate(app);
        } catch (Throwable e42) {
            if (!this.mInstrumentation.onException(app, e42)) {
                throw new RuntimeException("Unable to create application " + app.getClass().getName() + ": " + e42.toString(), e42);
            }
        } catch (Throwable e422) {
            throw new RuntimeException("Exception thrown in onCreate() of " + data.instrumentationName + ": " + e422.toString(), e422);
        } catch (Throwable th) {
            if (data.appInfo.targetSdkVersion <= 26 || StrictMode.getThreadPolicy().equals(writesAllowedPolicy)) {
                StrictMode.setThreadPolicy(savedPolicy);
            }
        }
        if (data.appInfo.targetSdkVersion <= 26 || StrictMode.getThreadPolicy().equals(writesAllowedPolicy)) {
            StrictMode.setThreadPolicy(savedPolicy);
        }
        FontsContract.setApplicationContextForResources(appContext);
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(data.appInfo.packageName, 128, UserHandle.myUserId());
            if (info.metaData != null) {
                int preloadedFontsResource = info.metaData.getInt(ApplicationInfo.METADATA_PRELOADED_FONTS, 0);
                if (preloadedFontsResource != 0) {
                    data.info.getResources().preloadFonts(preloadedFontsResource);
                }
            }
            int bindApp_dur = (int) (SystemClock.uptimeMillis() - st_bindApp);
            String pkg_name = null;
            if (appContext != null) {
                pkg_name = appContext.getPackageName();
            }
            if (boostFramework != null && (Process.isIsolated() ^ 1) != 0 && pkg_name != null) {
                boostFramework.perfUXEngine_events(2, 0, pkg_name, bindApp_dur);
            }
        } catch (RemoteException e22) {
            throw e22.rethrowFromSystemServer();
        }
    }

    final void finishInstrumentation(int resultCode, Bundle results) {
        IActivityManager am = ActivityManager.getService();
        if (this.mProfiler.profileFile != null && this.mProfiler.handlingProfiling && this.mProfiler.profileFd == null) {
            Debug.stopMethodTracing();
        }
        try {
            am.finishInstrumentation(this.mAppThread, resultCode, results);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    private void installContentProviders(Context context, List<ProviderInfo> providers) {
        ArrayList<ContentProviderHolder> results = new ArrayList();
        for (ProviderInfo cpi : providers) {
            if (DEBUG_PROVIDER) {
                StringBuilder buf = new StringBuilder(128);
                buf.append("Pub ");
                buf.append(cpi.authority);
                buf.append(": ");
                buf.append(cpi.name);
                Log.i(TAG, buf.toString());
            }
            ContentProviderHolder cph = installProvider(context, null, cpi, false, true, true);
            if (cph != null) {
                cph.noReleaseNeeded = true;
                results.add(cph);
            }
        }
        try {
            ActivityManager.getService().publishContentProviders(getApplicationThread(), results);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public final IContentProvider acquireProvider(Context c, String auth, int userId, boolean stable) {
        IContentProvider provider = acquireExistingProvider(c, auth, userId, stable);
        if (provider != null) {
            return provider;
        }
        try {
            ContentProviderHolder holder = ActivityManager.getService().getContentProvider(getApplicationThread(), auth, userId, stable);
            if (holder == null) {
                Slog.e(TAG, "Failed to find provider info for " + auth);
                return null;
            }
            return installProvider(c, holder, holder.info, true, holder.noReleaseNeeded, stable).provider;
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    private final void incProviderRefLocked(ProviderRefCount prc, boolean stable) {
        if (stable) {
            prc.stableCount++;
            if (prc.stableCount == 1) {
                int unstableDelta;
                if (prc.removePending) {
                    unstableDelta = -1;
                    if (DEBUG_PROVIDER) {
                        Slog.v(TAG, "incProviderRef: stable snatched provider from the jaws of death");
                    }
                    prc.removePending = false;
                    this.mH.removeMessages(131, prc);
                } else {
                    unstableDelta = 0;
                }
                try {
                    if (DEBUG_PROVIDER) {
                        Slog.v(TAG, "incProviderRef Now stable - " + prc.holder.info.name + ": unstableDelta=" + unstableDelta);
                    }
                    ActivityManager.getService().refContentProvider(prc.holder.connection, 1, unstableDelta);
                    return;
                } catch (RemoteException e) {
                    return;
                }
            }
            return;
        }
        prc.unstableCount++;
        if (prc.unstableCount != 1) {
            return;
        }
        if (prc.removePending) {
            if (DEBUG_PROVIDER) {
                Slog.v(TAG, "incProviderRef: unstable snatched provider from the jaws of death");
            }
            prc.removePending = false;
            this.mH.removeMessages(131, prc);
            return;
        }
        try {
            if (DEBUG_PROVIDER) {
                Slog.v(TAG, "incProviderRef: Now unstable - " + prc.holder.info.name);
            }
            ActivityManager.getService().refContentProvider(prc.holder.connection, 0, 1);
        } catch (RemoteException e2) {
        }
    }

    /* JADX WARNING: Missing block: B:18:0x0061, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final IContentProvider acquireExistingProvider(Context c, String auth, int userId, boolean stable) {
        synchronized (this.mProviderMap) {
            ProviderClientRecord pr = (ProviderClientRecord) this.mProviderMap.get(new ProviderKey(auth, userId));
            if (pr == null) {
                return null;
            }
            IContentProvider provider = pr.mProvider;
            IBinder jBinder = provider.asBinder();
            if (jBinder.isBinderAlive()) {
                ProviderRefCount prc = (ProviderRefCount) this.mProviderRefCountMap.get(jBinder);
                if (prc != null) {
                    incProviderRefLocked(prc, stable);
                }
            } else {
                Log.i(TAG, "Acquiring provider " + auth + " for user " + userId + ": existing object's process dead");
                handleUnstableProviderDiedLocked(jBinder, true);
                return null;
            }
        }
    }

    /* JADX WARNING: Missing block: B:19:0x002d, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:43:0x00b6, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:52:0x00cb, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean releaseProvider(IContentProvider provider, boolean stable) {
        int i = 0;
        if (provider == null) {
            return false;
        }
        IBinder jBinder = provider.asBinder();
        synchronized (this.mProviderMap) {
            ProviderRefCount prc = (ProviderRefCount) this.mProviderRefCountMap.get(jBinder);
            if (prc == null) {
                return false;
            }
            boolean lastRef = false;
            if (stable) {
                if (prc.stableCount != 0) {
                    prc.stableCount--;
                    if (prc.stableCount == 0) {
                        lastRef = prc.unstableCount == 0;
                        try {
                            if (DEBUG_PROVIDER) {
                                Slog.v(TAG, "releaseProvider: No longer stable w/lastRef=" + lastRef + " - " + prc.holder.info.name);
                            }
                            IActivityManager service = ActivityManager.getService();
                            IBinder iBinder = prc.holder.connection;
                            if (lastRef) {
                                i = 1;
                            }
                            service.refContentProvider(iBinder, -1, i);
                        } catch (RemoteException e) {
                        }
                    }
                } else if (DEBUG_PROVIDER) {
                    Slog.v(TAG, "releaseProvider: stable ref count already 0, how?");
                }
            } else if (prc.unstableCount != 0) {
                prc.unstableCount--;
                if (prc.unstableCount == 0) {
                    lastRef = prc.stableCount == 0;
                    if (!lastRef) {
                        try {
                            if (DEBUG_PROVIDER) {
                                Slog.v(TAG, "releaseProvider: No longer unstable - " + prc.holder.info.name);
                            }
                            ActivityManager.getService().refContentProvider(prc.holder.connection, 0, -1);
                        } catch (RemoteException e2) {
                        }
                    }
                }
            } else if (DEBUG_PROVIDER) {
                Slog.v(TAG, "releaseProvider: unstable ref count already 0, how?");
            }
            if (lastRef) {
                if (prc.removePending) {
                    Slog.w(TAG, "Duplicate remove pending of provider " + prc.holder.info.name);
                } else {
                    if (DEBUG_PROVIDER) {
                        Slog.v(TAG, "releaseProvider: Enqueueing pending removal - " + prc.holder.info.name);
                    }
                    prc.removePending = true;
                    this.mH.sendMessage(this.mH.obtainMessage(131, prc));
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            return;
     */
    /* JADX WARNING: Missing block: B:24:0x0055, code:
            if (DEBUG_PROVIDER == false) goto L_0x007e;
     */
    /* JADX WARNING: Missing block: B:25:0x0057, code:
            android.util.Slog.v(TAG, "removeProvider: Invoking ActivityManagerService.removeContentProvider(" + r10.holder.info.name + ")");
     */
    /* JADX WARNING: Missing block: B:26:0x007e, code:
            android.app.ActivityManager.getService().removeContentProvider(r10.holder.connection, false);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void completeRemoveProvider(ProviderRefCount prc) {
        synchronized (this.mProviderMap) {
            if (prc.removePending) {
                prc.removePending = false;
                IBinder jBinder = prc.holder.provider.asBinder();
                if (((ProviderRefCount) this.mProviderRefCountMap.get(jBinder)) == prc) {
                    this.mProviderRefCountMap.remove(jBinder);
                }
                for (int i = this.mProviderMap.size() - 1; i >= 0; i--) {
                    if (((ProviderClientRecord) this.mProviderMap.valueAt(i)).mProvider.asBinder() == jBinder) {
                        this.mProviderMap.removeAt(i);
                    }
                }
            } else if (DEBUG_PROVIDER) {
                Slog.v(TAG, "completeRemoveProvider: lost the race, provider still in use");
            }
        }
    }

    final void handleUnstableProviderDied(IBinder provider, boolean fromClient) {
        synchronized (this.mProviderMap) {
            handleUnstableProviderDiedLocked(provider, fromClient);
        }
    }

    final void handleUnstableProviderDiedLocked(IBinder provider, boolean fromClient) {
        ProviderRefCount prc = (ProviderRefCount) this.mProviderRefCountMap.get(provider);
        if (prc != null) {
            if (DEBUG_PROVIDER) {
                Slog.v(TAG, "Cleaning up dead provider " + provider + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + prc.holder.info.name);
            }
            this.mProviderRefCountMap.remove(provider);
            for (int i = this.mProviderMap.size() - 1; i >= 0; i--) {
                ProviderClientRecord pr = (ProviderClientRecord) this.mProviderMap.valueAt(i);
                if (pr != null && pr.mProvider.asBinder() == provider) {
                    Slog.i(TAG, "Removing dead content provider:" + pr.mProvider.toString());
                    this.mProviderMap.removeAt(i);
                }
            }
            if (fromClient) {
                try {
                    ActivityManager.getService().unstableProviderDied(prc.holder.connection);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public static Boolean needCheckNetwork(String pkgName) {
        if (pkgName != null) {
            try {
                if (getPackageManager().inPmsWhiteList(0, pkgName, Arrays.asList(new String[]{"com.google.android.apps.maps"}))) {
                    return Boolean.valueOf(true);
                }
            } catch (Exception e) {
            } catch (LinkageError e2) {
            }
        }
        return Boolean.valueOf(false);
    }

    public static boolean inCptWhiteList(int type, String verifyStr) {
        try {
            return getPackageManager().inCptWhiteList(type, verifyStr);
        } catch (Exception e) {
        } catch (LinkageError e2) {
        }
        return false;
    }

    public static void sendCptUpload(String pkgName, String point) {
        try {
            getPackageManager().sendCptUpload(pkgName, point);
        } catch (Exception e) {
        } catch (LinkageError e2) {
        }
    }

    public static void appDexOptNotify(long start) {
    }

    final void appNotRespondingViaProvider(IBinder provider) {
        synchronized (this.mProviderMap) {
            ProviderRefCount prc = (ProviderRefCount) this.mProviderRefCountMap.get(provider);
            if (prc != null) {
                try {
                    ActivityManager.getService().appNotRespondingViaProvider(prc.holder.connection);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    private ProviderClientRecord installProviderAuthoritiesLocked(IContentProvider provider, ContentProvider localProvider, ContentProviderHolder holder) {
        String[] auths = holder.info.authority.split(";");
        int userId = UserHandle.getUserId(holder.info.applicationInfo.uid);
        if (provider != null) {
            for (String auth : auths) {
                if (auth.equals("com.android.contacts") || auth.equals("call_log") || auth.equals("call_log_shadow") || auth.equals("com.android.blockednumber") || auth.equals("com.android.calendar") || auth.equals("downloads") || auth.equals("telephony")) {
                    Binder.allowBlocking(provider.asBinder());
                }
            }
        }
        ProviderClientRecord pcr = new ProviderClientRecord(auths, provider, localProvider, holder);
        for (String auth2 : auths) {
            ProviderKey key = new ProviderKey(auth2, userId);
            if (((ProviderClientRecord) this.mProviderMap.get(key)) != null) {
                Slog.w(TAG, "Content provider " + pcr.mHolder.info.name + " already published as " + auth2);
            } else {
                this.mProviderMap.put(key, pcr);
            }
        }
        return pcr;
    }

    /* JADX WARNING: Missing block: B:55:0x01df, code:
            return r17;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ContentProviderHolder installProvider(Context context, ContentProviderHolder holder, ProviderInfo info, boolean noisy, boolean noReleaseNeeded, boolean stable) {
        IContentProvider provider;
        Throwable th;
        ContentProvider localProvider = null;
        if (holder == null || holder.provider == null) {
            if (DEBUG_PROVIDER || noisy) {
                Slog.d(TAG, "Loading provider " + info.authority + ": " + info.name);
            }
            Context c = null;
            ApplicationInfo ai = info.applicationInfo;
            if (context.getPackageName().equals(ai.packageName)) {
                c = context;
            } else if (this.mInitialApplication == null || !this.mInitialApplication.getPackageName().equals(ai.packageName)) {
                try {
                    c = context.createPackageContext(ai.packageName, 1);
                } catch (NameNotFoundException e) {
                }
            } else {
                c = this.mInitialApplication;
            }
            if (c == null) {
                Slog.w(TAG, "Unable to get context for package " + ai.packageName + " while loading content provider " + info.name);
                return null;
            }
            if (info.splitName != null) {
                try {
                    c = c.createContextForSplit(info.splitName);
                } catch (NameNotFoundException e2) {
                    throw new RuntimeException(e2);
                }
            }
            try {
                localProvider = (ContentProvider) c.getClassLoader().loadClass(info.name).newInstance();
                provider = localProvider.getIContentProvider();
                if (provider == null) {
                    Slog.e(TAG, "Failed to instantiate class " + info.name + " from sourceDir " + info.applicationInfo.sourceDir);
                    return null;
                }
                if (DEBUG_PROVIDER) {
                    Slog.v(TAG, "Instantiating local provider " + info.name);
                }
                localProvider.attachInfo(c, info);
            } catch (Exception e3) {
                if (this.mInstrumentation.onException(null, e3)) {
                    return null;
                }
                throw new RuntimeException("Unable to get provider " + info.name + ": " + e3.toString(), e3);
            }
        }
        provider = holder.provider;
        if (DEBUG_PROVIDER) {
            Slog.v(TAG, "Installing external provider " + info.authority + ": " + info.name);
        }
        synchronized (this.mProviderMap) {
            try {
                if (DEBUG_PROVIDER) {
                    Slog.v(TAG, "Checking to add " + provider + " / " + info.name);
                }
                IBinder jBinder = provider.asBinder();
                ContentProviderHolder retHolder;
                if (localProvider != null) {
                    ComponentName cname = new ComponentName(info.packageName, info.name);
                    ProviderClientRecord pr = (ProviderClientRecord) this.mLocalProvidersByName.get(cname);
                    if (pr != null) {
                        if (DEBUG_PROVIDER) {
                            Slog.v(TAG, "installProvider: lost the race, using existing local provider");
                        }
                        provider = pr.mProvider;
                    } else {
                        ContentProviderHolder holder2 = new ContentProviderHolder(info);
                        try {
                            holder2.provider = provider;
                            holder2.noReleaseNeeded = true;
                            pr = installProviderAuthoritiesLocked(provider, localProvider, holder2);
                            this.mLocalProviders.put(jBinder, pr);
                            this.mLocalProvidersByName.put(cname, pr);
                            holder = holder2;
                        } catch (Throwable th2) {
                            th = th2;
                            holder = holder2;
                            throw th;
                        }
                    }
                    retHolder = pr.mHolder;
                } else {
                    ProviderRefCount prc = (ProviderRefCount) this.mProviderRefCountMap.get(jBinder);
                    if (prc != null) {
                        if (DEBUG_PROVIDER) {
                            Slog.v(TAG, "installProvider: lost the race, updating ref count");
                        }
                        if (!noReleaseNeeded) {
                            incProviderRefLocked(prc, stable);
                            try {
                                ActivityManager.getService().removeContentProvider(holder.connection, stable);
                            } catch (RemoteException e4) {
                            }
                        }
                    } else {
                        ProviderClientRecord client = installProviderAuthoritiesLocked(provider, localProvider, holder);
                        if (noReleaseNeeded) {
                            prc = new ProviderRefCount(holder, client, 1000, 1000);
                        } else if (stable) {
                            prc = new ProviderRefCount(holder, client, 1, 0);
                        } else {
                            prc = new ProviderRefCount(holder, client, 0, 1);
                        }
                        this.mProviderRefCountMap.put(jBinder, prc);
                    }
                    retHolder = prc.holder;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private void attach(boolean system) {
        boolean z;
        boolean z2 = true;
        sCurrentActivityThread = this;
        this.mSystemThread = system;
        String open = SystemProperties.get("sys.activity.thread.log");
        if (open != null && open.equals("true")) {
            localLOGV = true;
            DEBUG_BROADCAST = true;
            DEBUG_SERVICE = true;
            DEBUG_MESSAGES = true;
            DEBUG_CONFIGURATION = true;
        } else if (open != null && open.equals("false")) {
            localLOGV = false;
            DEBUG_BROADCAST = false;
            DEBUG_SERVICE = false;
            DEBUG_MESSAGES = false;
            DEBUG_CONFIGURATION = false;
        }
        boolean isUserDebug = "userdebug".equals(SystemProperties.get("ro.build.type"));
        this.mDebugOn = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        if (localLOGV) {
            z = true;
        } else {
            z = isUserDebug;
        }
        localLOGV = z;
        if (DEBUG_MESSAGES) {
            z = true;
        } else {
            z = isUserDebug;
        }
        DEBUG_MESSAGES = z;
        if (DEBUG_BROADCAST) {
            z = true;
        } else {
            z = isUserDebug;
        }
        DEBUG_BROADCAST = z;
        DEBUG_BROADCAST_LIGHT = DEBUG_BROADCAST;
        if (!DEBUG_SERVICE) {
            z2 = isUserDebug;
        }
        DEBUG_SERVICE = z2;
        if (system) {
            DdmHandleAppName.setAppName("system_process", UserHandle.myUserId());
            try {
                this.mInstrumentation = new Instrumentation();
                this.mInitialApplication = ContextImpl.createAppContext(this, getSystemContext().mPackageInfo).mPackageInfo.makeApplication(true, null);
                this.mInitialApplication.onCreate();
            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate Application():" + e.toString(), e);
            }
        }
        ViewRootImpl.addFirstDrawHandler(new Runnable() {
            public void run() {
                ActivityThread.this.ensureJitEnabled();
            }
        });
        DdmHandleAppName.setAppName("<pre-initialized>", UserHandle.myUserId());
        RuntimeInit.setApplicationObject(this.mAppThread.asBinder());
        final IActivityManager mgr = ActivityManager.getService();
        try {
            mgr.attachApplication(this.mAppThread);
            BinderInternal.addGcWatcher(new Runnable() {
                public void run() {
                    if (ActivityThread.this.mSomeActivitiesChanged) {
                        Runtime runtime = Runtime.getRuntime();
                        long dalvikMax = runtime.maxMemory();
                        long dalvikUsed = runtime.totalMemory() - runtime.freeMemory();
                        if (dalvikUsed > (3 * dalvikMax) / 4) {
                            if (ActivityThread.DEBUG_MEMORY_TRIM) {
                                Slog.d(ActivityThread.TAG, "Dalvik max=" + (dalvikMax / 1024) + " total=" + (runtime.totalMemory() / 1024) + " used=" + (dalvikUsed / 1024));
                            }
                            ActivityThread.this.mSomeActivitiesChanged = false;
                            try {
                                mgr.releaseSomeActivities(ActivityThread.this.mAppThread);
                            } catch (RemoteException e) {
                                throw e.rethrowFromSystemServer();
                            }
                        }
                    }
                }
            });
            ColorDisplayOptimizationUtils.getInstance().initData();
            ColorDisplayCompatUtils.getInstance().initData(getSystemContext());
            ColorReflectDataUtils.getInstance().initData();
            ColorFormatterCompatibilityUtils.getInstance().initData();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
        ColorNavigationBarUtil.getInstance().initData(getSystemContext());
        DropBox.setReporter(new DropBoxReporter());
        InputLog.startWatching();
        ViewRootImpl.addConfigCallback(new android.app.-$Lambda$9I5WEMsoBc7l4QrNqZ4wx59yuHU.AnonymousClass1(this));
    }

    /* renamed from: lambda$-android_app_ActivityThread_304037 */
    /* synthetic */ void m4lambda$-android_app_ActivityThread_304037(Configuration globalConfig) {
        synchronized (this.mResourcesManager) {
            if (this.mResourcesManager.applyConfigurationToResourcesLocked(globalConfig, null)) {
                updateLocaleListFromAppContext(this.mInitialApplication.getApplicationContext(), this.mResourcesManager.getConfiguration().getLocales());
                if (this.mPendingConfiguration == null || this.mPendingConfiguration.isOtherSeqNewer(globalConfig)) {
                    this.mPendingConfiguration = globalConfig;
                    sendMessage(118, globalConfig);
                }
            }
        }
    }

    public static ActivityThread systemMain() {
        if (ActivityManager.isHighEndGfx()) {
            ThreadedRenderer.enableForegroundTrimming();
        } else {
            ThreadedRenderer.disable(true);
        }
        ActivityThread thread = new ActivityThread();
        thread.attach(true);
        return thread;
    }

    public final void installSystemProviders(List<ProviderInfo> providers) {
        if (providers != null) {
            installContentProviders(this.mInitialApplication, providers);
        }
    }

    public int getIntCoreSetting(String key, int defaultValue) {
        synchronized (this.mResourcesManager) {
            if (this.mCoreSettings != null) {
                int i = this.mCoreSettings.getInt(key, defaultValue);
                return i;
            }
            return defaultValue;
        }
    }

    public static void main(String[] args) {
        Trace.traceBegin(64, "ActivityThreadMain");
        CloseGuard.setEnabled(false);
        Environment.initForCurrentUser();
        EventLogger.setReporter(new EventLoggingReporter());
        TrustedCertificateStore.setDefaultUserDirectory(Environment.getUserConfigDirectory(UserHandle.myUserId()));
        Process.setArgV0("<pre-initialized>");
        Looper.prepareMainLooper();
        ActivityThread thread = new ActivityThread();
        thread.attach(false);
        if (sMainThreadHandler == null) {
            sMainThreadHandler = thread.getHandler();
        }
        Trace.traceEnd(64);
        Looper.loop();
        throw new RuntimeException("Main thread loop unexpectedly exited");
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YaoJun.Luo@Plf.SDK: Modify for rom theme", property = OppoRomType.ROM)
    Resources getTopLevelResources(String packageName, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, LoadedApk pkgInfo) {
        Resources r = this.mResourcesManager.getResources(null, resDir, splitResDirs, overlayDirs, libDirs, displayId, null, pkgInfo.getCompatibilityInfo(), pkgInfo.getClassLoader());
        if (r != null) {
            r.init(packageName);
        }
        return r;
    }
}

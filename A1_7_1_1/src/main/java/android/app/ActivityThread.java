package android.app;

import android.R;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.IActivityManager.ContentProviderHolder;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.app.backup.BackupAgent;
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
import android.content.pm.IPackageManager.Stub;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.Proxy;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.os.DropBoxManager;
import android.os.Environment;
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
import android.provider.MediaStore;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.renderscript.RenderScriptCacheDir;
import android.security.NetworkSecurityPolicy;
import android.security.net.config.NetworkSecurityConfigProvider;
import android.service.notification.ZenModeConfig;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
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
import com.android.internal.os.SamplingProfilerIntegration;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.org.conscrypt.OpenSSLSocketImpl;
import com.android.org.conscrypt.TrustedCertificateStore;
import com.color.util.ColorDisplayCompatUtils;
import com.color.util.ColorDisplayOptimizationUtils;
import com.google.android.collect.Lists;
import com.mediatek.anrappframeworks.ANRAppFrameworks;
import com.mediatek.anrappmanager.ANRAppManager;
import com.mediatek.anrappmanager.MessageLogger;
import com.oppo.debug.InputLog;
import com.oppo.hypnus.HypnusManager;
import com.oppo.luckymoney.LMManager;
import dalvik.system.CloseGuard;
import dalvik.system.VMDebug;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.text.DateFormat;
import java.util.ArrayList;
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
import oppo.util.OppoMultiLauncherUtil;
import org.apache.harmony.dalvik.ddmc.DdmVmInternal;

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
public final class ActivityThread {
    private static final int ACTIVITY_THREAD_CHECKIN_VERSION = 4;
    static boolean DEBUG_ACTIVITY_THREAD = false;
    private static boolean DEBUG_BACKUP = false;
    public static boolean DEBUG_BROADCAST = false;
    public static boolean DEBUG_BROADCAST_LIGHT = false;
    public static boolean DEBUG_CONFIGURATION = false;
    private static boolean DEBUG_LIFECYCLE = false;
    private static boolean DEBUG_MEMORY_TRIM = false;
    static boolean DEBUG_MESSAGES = false;
    private static boolean DEBUG_ORDER = false;
    private static boolean DEBUG_PROVIDER = false;
    private static boolean DEBUG_RESULTS = false;
    private static boolean DEBUG_SERVICE = false;
    private static final int DONT_REPORT = 2;
    private static final String HEAP_COLUMN = "%13s %8s %8s %8s %8s %8s %8s %8s";
    private static final String HEAP_FULL_COLUMN = "%13s %8s %8s %8s %8s %8s %8s %8s %8s %8s %8s";
    public static final int IN_ENQUEUEING = 1;
    public static final int IN_FINISHING = 3;
    public static final int IN_HANDLING = 2;
    public static final int IN_IDLE = 0;
    static final boolean IS_USER_BUILD = false;
    static final boolean IS_USER_DEBUG_BUILD = false;
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
    private static final int SUPPRESS_ACTION_ALLOWED = 0;
    private static final int SUPPRESS_ACTION_DELAYED = 1;
    private static final int SUPPRESS_TIME = 5000;
    public static final String TAG = "ActivityThread";
    private static final Config THUMBNAIL_FORMAT = null;
    private static final String TWO_COUNT_COLUMNS = "%21s %8d %21s %8d";
    private static final int USER_LEAVING = 1;
    static boolean localLOGV;
    public static int mBgBrState;
    public static int mFgBrState;
    private static HypnusManager mHM;
    private static final boolean mIsEngBuild = false;
    public static int mOppoBgBrState;
    public static int mOppoFgBrState;
    private static volatile ActivityThread sCurrentActivityThread;
    private static final ThreadLocal<Intent> sCurrentBroadcastIntent = null;
    private static boolean sHasCheckBoost;
    static volatile Handler sMainThreadHandler;
    static volatile IPackageManager sPackageManager;
    private static boolean sShoudCheckBoost;
    final ArrayMap<IBinder, ActivityClientRecord> mActivities;
    final ArrayList<Application> mAllApplications;
    final ApplicationThread mAppThread;
    private Bitmap mAvailThumbnailBitmap;
    final ArrayMap<String, BackupAgent> mBackupAgents;
    AppBindData mBoundApplication;
    Configuration mCompatConfiguration;
    Configuration mConfiguration;
    Bundle mCoreSettings;
    int mCurDefaultDisplayDpi;
    boolean mDensityCompatMode;
    final GcIdler mGcIdler;
    boolean mGcIdlerScheduled;
    final H mH;
    Application mInitialApplication;
    Instrumentation mInstrumentation;
    String mInstrumentationAppDir;
    String mInstrumentationLibDir;
    String mInstrumentationPackageName;
    String[] mInstrumentationSplitAppDirs;
    String mInstrumentedAppDir;
    String mInstrumentedLibDir;
    String[] mInstrumentedSplitAppDirs;
    boolean mJitEnabled;
    ArrayList<WeakReference<AssistStructure>> mLastAssistStructures;
    private int mLastSessionId;
    @GuardedBy("mResourcesManager")
    int mLifecycleSeq;
    final ArrayMap<IBinder, ProviderClientRecord> mLocalProviders;
    final ArrayMap<ComponentName, ProviderClientRecord> mLocalProvidersByName;
    final Looper mLooper;
    private Configuration mMainThreadConfig;
    ActivityClientRecord mNewActivities;
    int mNumVisibleActivities;
    final ArrayMap<Activity, ArrayList<OnActivityPausedListener>> mOnPauseListeners;
    final ArrayMap<String, WeakReference<LoadedApk>> mPackages;
    Configuration mPendingConfiguration;
    Profiler mProfiler;
    final ArrayMap<ProviderKey, ProviderClientRecord> mProviderMap;
    final ArrayMap<IBinder, ProviderRefCount> mProviderRefCountMap;
    final ArrayList<ActivityClientRecord> mRelaunchingActivities;
    final ArrayMap<String, WeakReference<LoadedApk>> mResourcePackages;
    private final ResourcesManager mResourcesManager;
    final ArrayMap<IBinder, Service> mServices;
    boolean mSomeActivitiesChanged;
    private ContextImpl mSystemContext;
    boolean mSystemThread;
    private Canvas mThumbnailCanvas;
    private int mThumbnailHeight;
    private int mThumbnailWidth;
    boolean mUpdatingSystemConfig;

    static final class ActivityClientRecord {
        Activity activity;
        ActivityInfo activityInfo;
        CompatibilityInfo compatInfo;
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
                sb.append(", visibleBehind=").append(this.activity.mVisibleBehind);
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

    private class ApplicationThread extends ApplicationThreadNative {
        private static final String DB_INFO_FORMAT = "  %8s %8s %14s %14s  %s";
        private int mLastProcessState;
        final /* synthetic */ ActivityThread this$0;

        /* renamed from: android.app.ActivityThread$ApplicationThread$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ ApplicationThread this$1;
            final /* synthetic */ String[] val$args;
            final /* synthetic */ ParcelFileDescriptor val$dup;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.app.ActivityThread.ApplicationThread.1.<init>(android.app.ActivityThread$ApplicationThread, android.os.ParcelFileDescriptor, java.lang.String[]):void, dex: 
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
            AnonymousClass1(android.app.ActivityThread.ApplicationThread r1, android.os.ParcelFileDescriptor r2, java.lang.String[] r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.app.ActivityThread.ApplicationThread.1.<init>(android.app.ActivityThread$ApplicationThread, android.os.ParcelFileDescriptor, java.lang.String[]):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityThread.ApplicationThread.1.<init>(android.app.ActivityThread$ApplicationThread, android.os.ParcelFileDescriptor, java.lang.String[]):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.app.ActivityThread.ApplicationThread.1.run():void, dex: 
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
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.app.ActivityThread.ApplicationThread.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityThread.ApplicationThread.1.run():void");
            }
        }

        /* synthetic */ ApplicationThread(ActivityThread this$0, ApplicationThread applicationThread) {
            this(this$0);
        }

        private ApplicationThread(ActivityThread this$0) {
            this.this$0 = this$0;
            this.mLastProcessState = -1;
        }

        private void updatePendingConfiguration(Configuration config) {
            synchronized (this.this$0.mResourcesManager) {
                if (this.this$0.mPendingConfiguration == null || this.this$0.mPendingConfiguration.isOtherSeqNewer(config)) {
                    this.this$0.mPendingConfiguration = config;
                }
            }
        }

        public final void schedulePauseActivity(IBinder token, boolean finished, boolean userLeaving, int configChanges, boolean dontReport) {
            int i;
            int i2 = 0;
            int seq = this.this$0.getLifecycleSeq();
            if (ActivityThread.DEBUG_ORDER) {
                Slog.d(ActivityThread.TAG, "pauseActivity " + this.this$0 + " operation received seq: " + seq);
            }
            ActivityThread activityThread = this.this$0;
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
            int seq = this.this$0.getLifecycleSeq();
            if (ActivityThread.DEBUG_ORDER) {
                Slog.d(ActivityThread.TAG, "stopActivity " + this.this$0 + " operation received seq: " + seq);
            }
            this.this$0.sendMessage(showWindow ? 103 : 104, (Object) token, 0, configChanges, seq);
        }

        public final void scheduleWindowVisibility(IBinder token, boolean showWindow) {
            this.this$0.sendMessage(showWindow ? 105 : 106, token);
        }

        public final void scheduleSleeping(IBinder token, boolean sleeping) {
            this.this$0.sendMessage(137, token, sleeping ? 1 : 0);
        }

        public final void scheduleResumeActivity(IBinder token, int processState, boolean isForward, Bundle resumeArgs) {
            int seq = this.this$0.getLifecycleSeq();
            if (ActivityThread.DEBUG_ORDER) {
                Slog.d(ActivityThread.TAG, "resumeActivity " + this.this$0 + " operation received seq: " + seq);
            }
            updateProcessState(processState, false);
            this.this$0.sendMessage(107, (Object) token, isForward ? 1 : 0, 0, seq);
        }

        public final void scheduleSendResult(IBinder token, List<ResultInfo> results) {
            ResultData res = new ResultData();
            res.token = token;
            res.results = results;
            this.this$0.sendMessage(108, res);
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
            this.this$0.sendMessage(100, r);
        }

        public final void scheduleRelaunchActivity(IBinder token, List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents, int configChanges, boolean notResumed, Configuration config, Configuration overrideConfig, boolean preserveWindow) {
            this.this$0.requestRelaunchActivity(token, pendingResults, pendingNewIntents, configChanges, notResumed, config, overrideConfig, true, preserveWindow);
        }

        public final void scheduleNewIntent(List<ReferrerIntent> intents, IBinder token, boolean andPause) {
            NewIntentData data = new NewIntentData();
            data.intents = intents;
            data.token = token;
            data.andPause = andPause;
            this.this$0.sendMessage(112, data);
        }

        public final void scheduleDestroyActivity(IBinder token, boolean finishing, int configChanges) {
            this.this$0.sendMessage(109, token, finishing ? 1 : 0, configChanges);
        }

        public final void scheduleReceiver(Intent intent, ActivityInfo info, CompatibilityInfo compatInfo, int resultCode, String data, Bundle extras, boolean sync, int sendingUser, int processState, int hasCode) {
            updateProcessState(processState, false);
            ReceiverData r = new ReceiverData(intent, resultCode, data, extras, sync, false, this.this$0.mAppThread.asBinder(), sendingUser, hasCode);
            r.info = info;
            r.compatInfo = compatInfo;
            if (sync) {
                r.setBroadcastState(intent.getFlags(), 1);
            }
            if (ActivityThread.DEBUG_BROADCAST_LIGHT) {
                Slog.v(ActivityThread.TAG, "scheduleReceiver info = " + info + " intent = " + intent + " sync = " + sync + " hasCode = " + hasCode);
            }
            this.this$0.sendMessage(113, r);
        }

        public final void scheduleCreateBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo, int backupMode) {
            CreateBackupAgentData d = new CreateBackupAgentData();
            d.appInfo = app;
            d.compatInfo = compatInfo;
            d.backupMode = backupMode;
            this.this$0.sendMessage(128, d);
        }

        public final void scheduleDestroyBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo) {
            CreateBackupAgentData d = new CreateBackupAgentData();
            d.appInfo = app;
            d.compatInfo = compatInfo;
            this.this$0.sendMessage(129, d);
        }

        public final void scheduleCreateService(IBinder token, ServiceInfo info, CompatibilityInfo compatInfo, int processState) {
            updateProcessState(processState, false);
            CreateServiceData s = new CreateServiceData();
            s.token = token;
            s.info = info;
            s.compatInfo = compatInfo;
            this.this$0.sendMessage(114, s);
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
            this.this$0.sendMessage(121, s);
        }

        public final void scheduleUnbindService(IBinder token, Intent intent) {
            BindServiceData s = new BindServiceData();
            s.token = token;
            s.intent = intent;
            this.this$0.sendMessage(122, s);
        }

        public final void scheduleServiceArgs(IBinder token, boolean taskRemoved, int startId, int flags, Intent args) {
            ServiceArgsData s = new ServiceArgsData();
            s.token = token;
            s.taskRemoved = taskRemoved;
            s.startId = startId;
            s.flags = flags;
            s.args = args;
            this.this$0.sendMessage(115, s);
        }

        public final void scheduleStopService(IBinder token) {
            this.this$0.sendMessage(116, token);
        }

        public final void bindApplication(String processName, ApplicationInfo appInfo, List<ProviderInfo> providers, ComponentName instrumentationName, ProfilerInfo profilerInfo, Bundle instrumentationArgs, IInstrumentationWatcher instrumentationWatcher, IUiAutomationConnection instrumentationUiConnection, int debugMode, boolean enableBinderTracking, boolean trackAllocation, boolean isRestrictedBackupMode, boolean persistent, Configuration config, CompatibilityInfo compatInfo, Map<String, IBinder> services, Bundle coreSettings) {
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
            this.this$0.sendMessage(110, data);
        }

        public final void scheduleExit() {
            this.this$0.sendMessage(111, null);
        }

        public final void scheduleSuicide() {
            this.this$0.sendMessage(130, null);
        }

        public void scheduleConfigurationChanged(Configuration config) {
            updatePendingConfiguration(config);
            this.this$0.sendMessage(118, config);
        }

        public void updateTimeZone() {
            TimeZone.setDefault(null);
        }

        public void clearDnsCache() {
            InetAddress.clearDnsCache();
            NetworkEventDispatcher.getInstance().onNetworkConfigurationChanged();
        }

        public void setHttpProxy(String host, String port, String exclList, Uri pacFileUrl) {
            ConnectivityManager cm = ConnectivityManager.from(this.this$0.getSystemContext());
            if (cm.getBoundNetworkForProcess() != null) {
                Proxy.setHttpProxySystemProperty(cm.getDefaultProxy());
            } else {
                Proxy.setHttpProxySystemProperty(host, port, exclList, pacFileUrl);
            }
        }

        public void processInBackground() {
            this.this$0.mH.removeMessages(120);
            this.this$0.mH.sendMessage(this.this$0.mH.obtainMessage(120));
        }

        public void dumpService(FileDescriptor fd, IBinder servicetoken, String[] args) {
            DumpComponentInfo data = new DumpComponentInfo();
            try {
                data.fd = ParcelFileDescriptor.dup(fd);
                data.token = servicetoken;
                data.args = args;
                this.this$0.sendMessage(123, (Object) data, 0, 0, true);
            } catch (IOException e) {
                Slog.w(ActivityThread.TAG, "dumpService failed", e);
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
            this.this$0.sendMessage(124, null);
        }

        public void scheduleActivityConfigurationChanged(IBinder token, Configuration overrideConfig, boolean reportToActivity) {
            this.this$0.sendMessage(125, new ActivityConfigChangeData(token, overrideConfig), reportToActivity ? 1 : 0);
        }

        public void profilerControl(boolean start, ProfilerInfo profilerInfo, int profileType) {
            this.this$0.sendMessage(127, profilerInfo, start ? 1 : 0, profileType);
        }

        public void dumpHeap(boolean managed, String path, ParcelFileDescriptor fd) {
            int i;
            DumpHeapData dhd = new DumpHeapData();
            dhd.path = path;
            dhd.fd = fd;
            ActivityThread activityThread = this.this$0;
            if (managed) {
                i = 1;
            } else {
                i = 0;
            }
            activityThread.sendMessage(135, (Object) dhd, i, 0, true);
        }

        public void setSchedulingGroup(int group) {
            try {
                Process.setProcessGroup(Process.myPid(), group);
            } catch (Exception e) {
                Slog.w(ActivityThread.TAG, "Failed setting process group to " + group, e);
            }
        }

        public void dispatchPackageBroadcast(int cmd, String[] packages) {
            this.this$0.sendMessage(133, packages, cmd);
        }

        public void scheduleCrash(String msg) {
            this.this$0.sendMessage(134, msg);
        }

        public void dumpActivity(FileDescriptor fd, IBinder activitytoken, String prefix, String[] args) {
            DumpComponentInfo data = new DumpComponentInfo();
            try {
                data.fd = ParcelFileDescriptor.dup(fd);
                data.token = activitytoken;
                data.prefix = prefix;
                data.args = args;
                this.this$0.sendMessage(136, (Object) data, 0, 0, true);
            } catch (IOException e) {
                Slog.w(ActivityThread.TAG, "dumpActivity failed", e);
            }
        }

        public void dumpProvider(FileDescriptor fd, IBinder providertoken, String[] args) {
            DumpComponentInfo data = new DumpComponentInfo();
            try {
                data.fd = ParcelFileDescriptor.dup(fd);
                data.token = providertoken;
                data.args = args;
                this.this$0.sendMessage(141, (Object) data, 0, 0, true);
            } catch (IOException e) {
                Slog.w(ActivityThread.TAG, "dumpProvider failed", e);
            }
        }

        public void dumpMemInfo(FileDescriptor fd, MemoryInfo mem, boolean checkin, boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) {
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
            try {
                dumpMemInfo(pw, mem, checkin, dumpFullInfo, dumpDalvik, dumpSummaryOnly, dumpUnreachable);
            } finally {
                pw.flush();
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
            Class[] classesToCount = new Class[4];
            classesToCount[0] = ContextImpl.class;
            classesToCount[1] = Activity.class;
            classesToCount[2] = WebView.class;
            classesToCount[3] = OpenSSLSocketImpl.class;
            long[] instanceCounts = VMDebug.countInstancesOfClasses(classesToCount, true);
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
            ActivityThread.dumpMemInfoTable(pw, memInfo, checkin, dumpFullInfo, dumpDalvik, dumpSummaryOnly, Process.myPid(), this.this$0.mBoundApplication != null ? this.this$0.mBoundApplication.processName : "unknown", nativeMax, nativeAllocated, nativeFree, dalvikMax, dalvikAllocated, dalvikFree);
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
            String str = ActivityThread.TWO_COUNT_COLUMNS;
            Object[] objArr = new Object[4];
            objArr[0] = "Views:";
            objArr[1] = Long.valueOf(viewInstanceCount);
            objArr[2] = "ViewRootImpl:";
            objArr[3] = Long.valueOf(viewRootInstanceCount);
            ActivityThread.printRow(pw, str, objArr);
            str = ActivityThread.TWO_COUNT_COLUMNS;
            objArr = new Object[4];
            objArr[0] = "AppContexts:";
            objArr[1] = Long.valueOf(appContextInstanceCount);
            objArr[2] = "Activities:";
            objArr[3] = Long.valueOf(activityInstanceCount);
            ActivityThread.printRow(pw, str, objArr);
            str = ActivityThread.TWO_COUNT_COLUMNS;
            objArr = new Object[4];
            objArr[0] = "Assets:";
            objArr[1] = Integer.valueOf(globalAssetCount);
            objArr[2] = "AssetManagers:";
            objArr[3] = Integer.valueOf(globalAssetManagerCount);
            ActivityThread.printRow(pw, str, objArr);
            str = ActivityThread.TWO_COUNT_COLUMNS;
            objArr = new Object[4];
            objArr[0] = "Local Binders:";
            objArr[1] = Integer.valueOf(binderLocalObjectCount);
            objArr[2] = "Proxy Binders:";
            objArr[3] = Integer.valueOf(binderProxyObjectCount);
            ActivityThread.printRow(pw, str, objArr);
            str = ActivityThread.TWO_COUNT_COLUMNS;
            objArr = new Object[4];
            objArr[0] = "Parcel memory:";
            objArr[1] = Long.valueOf(parcelSize / 1024);
            objArr[2] = "Parcel count:";
            objArr[3] = Long.valueOf(parcelCount);
            ActivityThread.printRow(pw, str, objArr);
            str = ActivityThread.TWO_COUNT_COLUMNS;
            objArr = new Object[4];
            objArr[0] = "Death Recipients:";
            objArr[1] = Integer.valueOf(binderDeathObjectCount);
            objArr[2] = "OpenSSL Sockets:";
            objArr[3] = Long.valueOf(openSslSocketCount);
            ActivityThread.printRow(pw, str, objArr);
            str = ActivityThread.ONE_COUNT_COLUMN;
            objArr = new Object[2];
            objArr[0] = "WebViews:";
            objArr[1] = Long.valueOf(webviewInstanceCount);
            ActivityThread.printRow(pw, str, objArr);
            pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            pw.println(" SQL");
            str = ActivityThread.ONE_COUNT_COLUMN;
            objArr = new Object[2];
            objArr[0] = "MEMORY_USED:";
            objArr[1] = Integer.valueOf(stats.memoryUsed / 1024);
            ActivityThread.printRow(pw, str, objArr);
            str = ActivityThread.TWO_COUNT_COLUMNS;
            objArr = new Object[4];
            objArr[0] = "PAGECACHE_OVERFLOW:";
            objArr[1] = Integer.valueOf(stats.pageCacheOverflow / 1024);
            objArr[2] = "MALLOC_SIZE:";
            objArr[3] = Integer.valueOf(stats.largestMemAlloc / 1024);
            ActivityThread.printRow(pw, str, objArr);
            pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            int N = stats.dbStats.size();
            if (N > 0) {
                pw.println(" DATABASES");
                str = DB_INFO_FORMAT;
                objArr = new Object[5];
                objArr[0] = "pgsz";
                objArr[1] = "dbsz";
                objArr[2] = "Lookaside(b)";
                objArr[3] = "cache";
                objArr[4] = "Dbname";
                ActivityThread.printRow(pw, str, objArr);
                for (i = 0; i < N; i++) {
                    dbStats = (DbStats) stats.dbStats.get(i);
                    String str2 = DB_INFO_FORMAT;
                    Object[] objArr2 = new Object[5];
                    objArr2[0] = dbStats.pageSize > 0 ? String.valueOf(dbStats.pageSize) : WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
                    objArr2[1] = dbStats.dbSize > 0 ? String.valueOf(dbStats.dbSize) : WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
                    objArr2[2] = dbStats.lookaside > 0 ? String.valueOf(dbStats.lookaside) : WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
                    objArr2[3] = dbStats.cache;
                    objArr2[4] = dbStats.dbName;
                    ActivityThread.printRow(pw, str2, objArr2);
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
                if (this.this$0.mBoundApplication == null || (this.this$0.mBoundApplication.appInfo.flags & 2) == 0) {
                    showContents = Build.IS_DEBUGGABLE;
                } else {
                    showContents = true;
                }
                pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                pw.println(" Unreachable memory");
                pw.print(Debug.getUnreachableMemory(100, showContents));
            }
        }

        public void dumpGfxInfo(FileDescriptor fd, String[] args) {
            this.this$0.dumpGraphicsInfo(fd);
            WindowManagerGlobal.getInstance().dumpGfxInfo(fd, args);
        }

        private void dumpDatabaseInfo(FileDescriptor fd, String[] args) {
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
            SQLiteDebug.dump(new PrintWriterPrinter(pw), args);
            pw.flush();
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
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public void dumpDbInfo(java.io.FileDescriptor r6, java.lang.String[] r7) {
            /*
            r5 = this;
            r2 = r5.this$0;
            r2 = r2.mSystemThread;
            if (r2 == 0) goto L_0x0035;
        L_0x0006:
            r0 = android.os.ParcelFileDescriptor.dup(r6);	 Catch:{ IOException -> 0x0015 }
            r2 = android.os.AsyncTask.THREAD_POOL_EXECUTOR;
            r3 = new android.app.ActivityThread$ApplicationThread$1;
            r3.<init>(r5, r0, r7);
            r2.execute(r3);
        L_0x0014:
            return;
        L_0x0015:
            r1 = move-exception;
            r2 = "ActivityThread";
            r3 = new java.lang.StringBuilder;
            r3.<init>();
            r4 = "Could not dup FD ";
            r3 = r3.append(r4);
            r4 = r6.getInt$();
            r3 = r3.append(r4);
            r3 = r3.toString();
            android.util.Log.w(r2, r3);
            return;
        L_0x0035:
            r5.dumpDatabaseInfo(r6, r7);
            goto L_0x0014;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityThread.ApplicationThread.dumpDbInfo(java.io.FileDescriptor, java.lang.String[]):void");
        }

        public void unstableProviderDied(IBinder provider) {
            this.this$0.sendMessage(142, provider);
        }

        public void requestAssistContextExtras(IBinder activityToken, IBinder requestToken, int requestType, int sessionId) {
            RequestAssistContextExtras cmd = new RequestAssistContextExtras();
            cmd.activityToken = activityToken;
            cmd.requestToken = requestToken;
            cmd.requestType = requestType;
            cmd.sessionId = sessionId;
            this.this$0.sendMessage(143, cmd);
        }

        public void setCoreSettings(Bundle coreSettings) {
            this.this$0.sendMessage(138, coreSettings);
        }

        public void updatePackageCompatibilityInfo(String pkg, CompatibilityInfo info) {
            UpdateCompatibilityData ucd = new UpdateCompatibilityData();
            ucd.pkg = pkg;
            ucd.info = info;
            this.this$0.sendMessage(139, ucd);
        }

        public void scheduleTrimMemory(int level) {
            this.this$0.sendMessage(140, null, level);
        }

        public void scheduleTranslucentConversionComplete(IBinder token, boolean drawComplete) {
            this.this$0.sendMessage(144, token, drawComplete ? 1 : 0);
        }

        public void scheduleOnNewActivityOptions(IBinder token, ActivityOptions options) {
            this.this$0.sendMessage(146, new Pair(token, options));
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

        public void scheduleInstallProvider(ProviderInfo provider) {
            this.this$0.sendMessage(145, provider);
        }

        public final void updateTimePrefs(boolean is24Hour) {
            DateFormat.set24HourTimePref(is24Hour);
        }

        public void scheduleCancelVisibleBehind(IBinder token) {
            this.this$0.sendMessage(147, token);
        }

        public void scheduleBackgroundVisibleBehindChanged(IBinder token, boolean visible) {
            this.this$0.sendMessage(148, token, visible ? 1 : 0);
        }

        public void scheduleEnterAnimationComplete(IBinder token) {
            this.this$0.sendMessage(149, token);
        }

        public void notifyCleartextNetwork(byte[] firstPacket) {
            if (StrictMode.vmCleartextNetworkEnabled()) {
                StrictMode.onCleartextNetworkDetected(firstPacket);
            }
        }

        public void startBinderTracking() {
            this.this$0.sendMessage(150, null);
        }

        public void stopBinderTrackingAndDump(FileDescriptor fd) {
            try {
                this.this$0.sendMessage(151, ParcelFileDescriptor.dup(fd));
            } catch (IOException e) {
            }
        }

        public void scheduleMultiWindowModeChanged(IBinder token, boolean isInMultiWindowMode) throws RemoteException {
            this.this$0.sendMessage(152, token, isInMultiWindowMode ? 1 : 0);
        }

        public void schedulePictureInPictureModeChanged(IBinder token, boolean isInPipMode) throws RemoteException {
            this.this$0.sendMessage(153, token, isInPipMode ? 1 : 0);
        }

        public void scheduleLocalVoiceInteractionStarted(IBinder token, IVoiceInteractor voiceInteractor) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = token;
            args.arg2 = voiceInteractor;
            this.this$0.sendMessage(154, args);
        }

        public void enableLooperLog() {
            ActivityThread.enableLooperLog();
        }

        public void dumpMessageHistory() {
            ANRAppManager.dumpMessageHistory();
        }

        public void dumpAllMessageHistory() {
            ANRAppManager.dumpAllMessageHistory();
        }

        public void configActivityLogTag(String tag, boolean on) {
            this.this$0.configActivityLogTag(tag, on);
        }

        public int getBroadcastState(int flag) {
            int state;
            if ((2097152 & flag) != 0) {
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
            return state;
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

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityThread.CreateBackupAgentData.<init>():void, dex: 
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
        CreateBackupAgentData() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityThread.CreateBackupAgentData.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityThread.CreateBackupAgentData.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.app.ActivityThread.CreateBackupAgentData.toString():java.lang.String, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.app.ActivityThread.CreateBackupAgentData.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityThread.CreateBackupAgentData.toString():java.lang.String");
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
        final /* synthetic */ ActivityThread this$0;

        public DropBoxReporter(ActivityThread this$0) {
            this.this$0 = this$0;
        }

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
                this.dropBox = (DropBoxManager) this.this$0.getSystemContext().getSystemService(Context.DROPBOX_SERVICE);
            }
        }
    }

    static final class DumpComponentInfo {
        String[] args;
        ParcelFileDescriptor fd;
        String prefix;
        IBinder token;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityThread.DumpComponentInfo.<init>():void, dex: 
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
        DumpComponentInfo() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityThread.DumpComponentInfo.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityThread.DumpComponentInfo.<init>():void");
        }
    }

    static final class DumpHeapData {
        ParcelFileDescriptor fd;
        String path;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityThread.DumpHeapData.<init>():void, dex: 
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
        DumpHeapData() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityThread.DumpHeapData.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityThread.DumpHeapData.<init>():void");
        }
    }

    private static class EventLoggingReporter implements EventLogger.Reporter {
        /* synthetic */ EventLoggingReporter(EventLoggingReporter eventLoggingReporter) {
            this();
        }

        private EventLoggingReporter() {
        }

        public void report(int code, Object... list) {
            EventLog.writeEvent(code, list);
        }
    }

    final class GcIdler implements IdleHandler {
        final /* synthetic */ ActivityThread this$0;

        GcIdler(ActivityThread this$0) {
            this.this$0 = this$0;
        }

        public final boolean queueIdle() {
            this.this$0.doGcIfNeeded();
            return false;
        }
    }

    private class H extends Handler {
        public static final int ACTIVITY_CONFIGURATION_CHANGED = 125;
        public static final int BACKGROUND_VISIBLE_BEHIND_CHANGED = 148;
        public static final int BIND_APPLICATION = 110;
        public static final int BIND_SERVICE = 121;
        public static final int CANCEL_VISIBLE_BEHIND = 147;
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
        final /* synthetic */ ActivityThread this$0;

        /* synthetic */ H(ActivityThread this$0, H h) {
            this(this$0);
        }

        private H(ActivityThread this$0) {
            this.this$0 = this$0;
        }

        String codeToString(int code) {
            if (ActivityThread.DEBUG_MESSAGES || isDebuggableMessage(code) || ActivityThread.DEBUG_ACTIVITY_THREAD) {
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
                    case 147:
                        return "CANCEL_VISIBLE_BEHIND";
                    case 148:
                        return "BACKGROUND_VISIBLE_BEHIND_CHANGED";
                    case 149:
                        return "ENTER_ANIMATION_COMPLETE";
                    case 152:
                        return "MULTI_WINDOW_MODE_CHANGED";
                    case 153:
                        return "PICTURE_IN_PICTURE_MODE_CHANGED";
                    case 154:
                        return "LOCAL_VOICE_INTERACTION_STARTED";
                }
            }
            return Integer.toString(code);
        }

        boolean isDebuggableMessage(int code) {
            if (ActivityThread.IS_USER_BUILD) {
                return false;
            }
            switch (code) {
                case 118:
                    return false;
                default:
                    return true;
            }
        }

        public void handleMessage(Message msg) {
            boolean debugSpecial;
            if (ActivityThread.DEBUG_ACTIVITY_THREAD) {
                debugSpecial = !"BIND_APPLICATION".equals(codeToString(msg.what)) ? "INSTALL_PROVIDER".equals(codeToString(msg.what)) : true;
            } else {
                debugSpecial = false;
            }
            if (ActivityThread.DEBUG_MESSAGES || debugSpecial) {
                Slog.v(ActivityThread.TAG, ">>> handling: " + codeToString(msg.what));
            }
            SomeArgs args;
            switch (msg.what) {
                case 100:
                    Trace.traceBegin(64, "activityStart");
                    ActivityClientRecord r = msg.obj;
                    r.packageInfo = this.this$0.getPackageInfoNoCheck(r.activityInfo.applicationInfo, r.compatInfo);
                    this.this$0.handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
                    Trace.traceEnd(64);
                    break;
                case 101:
                    Trace.traceBegin(64, "activityPause");
                    args = msg.obj;
                    this.this$0.handlePauseActivity((IBinder) args.arg1, false, (args.argi1 & 1) != 0, args.argi2, (args.argi1 & 2) != 0, args.argi3);
                    maybeSnapshot();
                    Trace.traceEnd(64);
                    break;
                case 102:
                    Trace.traceBegin(64, "activityPause");
                    args = (SomeArgs) msg.obj;
                    this.this$0.handlePauseActivity((IBinder) args.arg1, true, (args.argi1 & 1) != 0, args.argi2, (args.argi1 & 2) != 0, args.argi3);
                    Trace.traceEnd(64);
                    break;
                case 103:
                    Trace.traceBegin(64, "activityStop");
                    args = (SomeArgs) msg.obj;
                    this.this$0.handleStopActivity((IBinder) args.arg1, true, args.argi2, args.argi3);
                    Trace.traceEnd(64);
                    break;
                case 104:
                    Trace.traceBegin(64, "activityStop");
                    args = (SomeArgs) msg.obj;
                    this.this$0.handleStopActivity((IBinder) args.arg1, false, args.argi2, args.argi3);
                    Trace.traceEnd(64);
                    break;
                case 105:
                    Trace.traceBegin(64, "activityShowWindow");
                    this.this$0.handleWindowVisibility((IBinder) msg.obj, true);
                    Trace.traceEnd(64);
                    break;
                case 106:
                    Trace.traceBegin(64, "activityHideWindow");
                    this.this$0.handleWindowVisibility((IBinder) msg.obj, false);
                    Trace.traceEnd(64);
                    break;
                case 107:
                    Trace.traceBegin(64, "activityResume");
                    args = (SomeArgs) msg.obj;
                    this.this$0.handleResumeActivity((IBinder) args.arg1, true, args.argi1 != 0, true, args.argi3, "RESUME_ACTIVITY");
                    Trace.traceEnd(64);
                    break;
                case 108:
                    Trace.traceBegin(64, "activityDeliverResult");
                    this.this$0.handleSendResult((ResultData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 109:
                    Trace.traceBegin(64, "activityDestroy");
                    this.this$0.handleDestroyActivity((IBinder) msg.obj, msg.arg1 != 0, msg.arg2, false);
                    Trace.traceEnd(64);
                    break;
                case 110:
                    Trace.traceBegin(64, "bindApplication");
                    this.this$0.handleBindApplication(msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 111:
                    if (this.this$0.mInitialApplication != null) {
                        this.this$0.mInitialApplication.onTerminate();
                    }
                    Looper.myLooper().quit();
                    break;
                case 112:
                    Trace.traceBegin(64, "activityNewIntent");
                    this.this$0.handleNewIntent((NewIntentData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 113:
                    Trace.traceBegin(64, "broadcastReceiveComp");
                    this.this$0.handleReceiver((ReceiverData) msg.obj);
                    maybeSnapshot();
                    Trace.traceEnd(64);
                    break;
                case 114:
                    Trace.traceBegin(64, "serviceCreate: " + String.valueOf(msg.obj));
                    this.this$0.handleCreateService((CreateServiceData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 115:
                    Trace.traceBegin(64, "serviceStart: " + String.valueOf(msg.obj));
                    this.this$0.handleServiceArgs((ServiceArgsData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 116:
                    Trace.traceBegin(64, "serviceStop");
                    this.this$0.handleStopService((IBinder) msg.obj);
                    maybeSnapshot();
                    Trace.traceEnd(64);
                    break;
                case 118:
                    Trace.traceBegin(64, "configChanged");
                    this.this$0.mCurDefaultDisplayDpi = ((Configuration) msg.obj).densityDpi;
                    this.this$0.mUpdatingSystemConfig = true;
                    this.this$0.handleConfigurationChanged((Configuration) msg.obj, null);
                    this.this$0.mUpdatingSystemConfig = false;
                    Trace.traceEnd(64);
                    break;
                case 119:
                    ContextCleanupInfo cci = msg.obj;
                    cci.context.performFinalCleanup(cci.who, cci.what);
                    break;
                case 120:
                    this.this$0.scheduleGcIdler();
                    break;
                case 121:
                    Trace.traceBegin(64, "serviceBind");
                    this.this$0.handleBindService((BindServiceData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 122:
                    Trace.traceBegin(64, "serviceUnbind");
                    this.this$0.handleUnbindService((BindServiceData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 123:
                    this.this$0.handleDumpService((DumpComponentInfo) msg.obj);
                    break;
                case 124:
                    Trace.traceBegin(64, "lowMemory");
                    this.this$0.handleLowMemory();
                    Trace.traceEnd(64);
                    break;
                case 125:
                    Trace.traceBegin(64, "activityConfigChanged");
                    this.this$0.handleActivityConfigurationChanged((ActivityConfigChangeData) msg.obj, msg.arg1 == 1);
                    Trace.traceEnd(64);
                    break;
                case 126:
                    Trace.traceBegin(64, "activityRestart");
                    this.this$0.handleRelaunchActivity((ActivityClientRecord) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 127:
                    this.this$0.handleProfilerControl(msg.arg1 != 0, (ProfilerInfo) msg.obj, msg.arg2);
                    break;
                case 128:
                    Trace.traceBegin(64, "backupCreateAgent");
                    this.this$0.handleCreateBackupAgent((CreateBackupAgentData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 129:
                    Trace.traceBegin(64, "backupDestroyAgent");
                    this.this$0.handleDestroyBackupAgent((CreateBackupAgentData) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 130:
                    Process.killProcess(Process.myPid());
                    break;
                case 131:
                    Trace.traceBegin(64, "providerRemove");
                    this.this$0.completeRemoveProvider((ProviderRefCount) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 132:
                    this.this$0.ensureJitEnabled();
                    break;
                case 133:
                    Trace.traceBegin(64, "broadcastPackage");
                    this.this$0.handleDispatchPackageBroadcast(msg.arg1, (String[]) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 134:
                    if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                        String msgStr = msg.obj;
                        if (msgStr != null && msgStr.contains("can't deliver broadcast")) {
                            Log.d(ActivityThread.TAG, "can't deliver broadcast, before died, dump msgs:");
                        }
                    }
                    throw new RemoteServiceException((String) msg.obj);
                case 135:
                    ActivityThread.handleDumpHeap(msg.arg1 != 0, (DumpHeapData) msg.obj);
                    break;
                case 136:
                    this.this$0.handleDumpActivity((DumpComponentInfo) msg.obj);
                    break;
                case 137:
                    Trace.traceBegin(64, "sleeping");
                    this.this$0.handleSleeping((IBinder) msg.obj, msg.arg1 != 0);
                    Trace.traceEnd(64);
                    break;
                case 138:
                    Trace.traceBegin(64, "setCoreSettings");
                    this.this$0.handleSetCoreSettings((Bundle) msg.obj);
                    Trace.traceEnd(64);
                    break;
                case 139:
                    this.this$0.handleUpdatePackageCompatibilityInfo((UpdateCompatibilityData) msg.obj);
                    break;
                case 140:
                    Trace.traceBegin(64, "trimMemory");
                    this.this$0.handleTrimMemory(msg.arg1);
                    Trace.traceEnd(64);
                    break;
                case 141:
                    this.this$0.handleDumpProvider((DumpComponentInfo) msg.obj);
                    break;
                case 142:
                    this.this$0.handleUnstableProviderDied((IBinder) msg.obj, false);
                    break;
                case 143:
                    this.this$0.handleRequestAssistContextExtras((RequestAssistContextExtras) msg.obj);
                    break;
                case 144:
                    this.this$0.handleTranslucentConversionComplete((IBinder) msg.obj, msg.arg1 == 1);
                    break;
                case 145:
                    this.this$0.handleInstallProvider((ProviderInfo) msg.obj);
                    break;
                case 146:
                    Pair<IBinder, ActivityOptions> pair = msg.obj;
                    this.this$0.onNewActivityOptions((IBinder) pair.first, (ActivityOptions) pair.second);
                    break;
                case 147:
                    this.this$0.handleCancelVisibleBehind((IBinder) msg.obj);
                    break;
                case 148:
                    this.this$0.handleOnBackgroundVisibleBehindChanged((IBinder) msg.obj, msg.arg1 > 0);
                    break;
                case 149:
                    this.this$0.handleEnterAnimationComplete((IBinder) msg.obj);
                    break;
                case 150:
                    this.this$0.handleStartBinderTracking();
                    break;
                case 151:
                    this.this$0.handleStopBinderTrackingAndDump((ParcelFileDescriptor) msg.obj);
                    break;
                case 152:
                    this.this$0.handleMultiWindowModeChanged((IBinder) msg.obj, msg.arg1 == 1);
                    break;
                case 153:
                    this.this$0.handlePictureInPictureModeChanged((IBinder) msg.obj, msg.arg1 == 1);
                    break;
                case 154:
                    this.this$0.handleLocalVoiceInteractionStarted((IBinder) ((SomeArgs) msg.obj).arg1, (IVoiceInteractor) ((SomeArgs) msg.obj).arg2);
                    break;
            }
            Object obj = msg.obj;
            if (obj instanceof SomeArgs) {
                ((SomeArgs) obj).recycle();
            }
            if (ActivityThread.DEBUG_MESSAGES) {
                Slog.v(ActivityThread.TAG, "<<< done: " + codeToString(msg.what));
            }
            if (!ActivityThread.DEBUG_MESSAGES) {
                if (!isDebuggableMessage(msg.what)) {
                    return;
                }
            }
            Slog.d(ActivityThread.TAG, codeToString(msg.what) + " handled " + ": " + msg.arg1 + " / " + msg.obj);
        }

        private void maybeSnapshot() {
            if (this.this$0.mBoundApplication != null && SamplingProfilerIntegration.isEnabled()) {
                String packageName = this.this$0.mBoundApplication.info.mPackageName;
                PackageInfo packageInfo = null;
                try {
                    Context context = this.this$0.getSystemContext();
                    if (context == null) {
                        Log.e(ActivityThread.TAG, "cannot get a valid context");
                        return;
                    }
                    PackageManager pm = context.getPackageManager();
                    if (pm == null) {
                        Log.e(ActivityThread.TAG, "cannot get a valid PackageManager");
                    } else {
                        packageInfo = pm.getPackageInfo(packageName, 1);
                        SamplingProfilerIntegration.writeSnapshot(this.this$0.mBoundApplication.processName, packageInfo);
                    }
                } catch (NameNotFoundException e) {
                    Log.e(ActivityThread.TAG, "cannot get package info for " + packageName, e);
                }
            }
        }
    }

    private class Idler implements IdleHandler {
        final /* synthetic */ ActivityThread this$0;

        /* synthetic */ Idler(ActivityThread this$0, Idler idler) {
            this(this$0);
        }

        private Idler(ActivityThread this$0) {
            this.this$0 = this$0;
        }

        public final boolean queueIdle() {
            ActivityClientRecord a = this.this$0.mNewActivities;
            boolean stopProfiling = false;
            if (!(this.this$0.mBoundApplication == null || this.this$0.mProfiler.profileFd == null || !this.this$0.mProfiler.autoStopProfiler)) {
                stopProfiling = true;
            }
            if (a != null) {
                this.this$0.mNewActivities = null;
                IActivityManager am = ActivityManagerNative.getDefault();
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
                    if (!(a.activity == null || a.activity.mFinished)) {
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
                this.this$0.mProfiler.stopProfiling();
            }
            this.this$0.ensureJitEnabled();
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
                    VMDebug.startMethodTracing(str, fileDescriptor, i, 0, z, this.samplingInterval);
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
        final /* synthetic */ ActivityThread this$0;

        ProviderClientRecord(ActivityThread this$0, String[] names, IContentProvider provider, ContentProvider localProvider, ContentProviderHolder holder) {
            this.this$0 = this$0;
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
        IBinder requestToken;
        int requestType;
        int sessionId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityThread.RequestAssistContextExtras.<init>():void, dex: 
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
        RequestAssistContextExtras() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityThread.RequestAssistContextExtras.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityThread.RequestAssistContextExtras.<init>():void");
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

        /* synthetic */ StopInfo(StopInfo stopInfo) {
            this();
        }

        private StopInfo() {
        }

        public void run() {
            try {
                if (ActivityThread.DEBUG_MEMORY_TRIM) {
                    Slog.v(ActivityThread.TAG, "Reporting activity stopped: " + this.activity);
                }
                ActivityManagerNative.getDefault().activityStopped(this.activity.token, this.state, this.persistentState, this.description);
            } catch (RemoteException ex) {
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

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ActivityThread.UpdateCompatibilityData.<init>():void, dex: 
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
        UpdateCompatibilityData() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ActivityThread.UpdateCompatibilityData.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityThread.UpdateCompatibilityData.<init>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.app.ActivityThread.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.app.ActivityThread.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityThread.<clinit>():void");
    }

    private native void dumpGraphicsInfo(FileDescriptor fileDescriptor);

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
        sPackageManager = Stub.asInterface(ServiceManager.getService("package"));
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

    /* JADX WARNING: Missing block: B:32:0x008b, code:
            return r3;
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            r0 = getPackageManager().getApplicationInfo(r10, 268436480, r13);
     */
    /* JADX WARNING: Missing block: B:37:0x0099, code:
            if (r0 == null) goto L_0x00a6;
     */
    /* JADX WARNING: Missing block: B:39:0x009f, code:
            return getPackageInfo(r0, r11, r12);
     */
    /* JADX WARNING: Missing block: B:40:0x00a0, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:42:0x00a5, code:
            throw r2.rethrowFromSystemServer();
     */
    /* JADX WARNING: Missing block: B:43:0x00a6, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final LoadedApk getPackageInfo(String packageName, CompatibilityInfo compatInfo, int flags, int userId) {
        boolean differentUser = UserHandle.myUserId() != userId;
        synchronized (this.mResourcesManager) {
            WeakReference ref;
            LoadedApk packageInfo;
            if (differentUser) {
                ref = null;
            } else if ((flags & 1) != 0) {
                ref = (WeakReference) this.mPackages.get(packageName);
            } else {
                ref = (WeakReference) this.mResourcePackages.get(packageName);
            }
            if (ref != null) {
                packageInfo = (LoadedApk) ref.get();
            } else {
                packageInfo = null;
            }
            if (packageInfo == null || !(packageInfo.mResources == null || packageInfo.mResources.getAssets().isUpToDate())) {
            } else if (packageInfo.isSecurityViolation() && (flags & 2) == 0) {
                throw new SecurityException("Requesting code from " + packageName + " to be run in process " + this.mBoundApplication.processName + "/" + this.mBoundApplication.appInfo.uid);
            }
        }
    }

    public final LoadedApk getPackageInfo(ApplicationInfo ai, CompatibilityInfo compatInfo, int flags) {
        boolean includeCode = false;
        if ((flags & 1) != 0) {
            includeCode = true;
        }
        boolean securityViolation = (!includeCode || ai.uid == 0 || ai.uid == 1000) ? false : this.mBoundApplication != null ? !UserHandle.isSameApp(ai.uid, this.mBoundApplication.appInfo.uid) : true;
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
            if (packageInfo == null || !(packageInfo.mResources == null || packageInfo.mResources.getAssets().isUpToDate())) {
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
                if (this.mSystemThread && ZenModeConfig.SYSTEM_AUTHORITY.equals(aInfo.packageName)) {
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
        this.mAppThread = new ApplicationThread(this, null);
        this.mLooper = Looper.myLooper();
        this.mH = new H(this, null);
        this.mActivities = new ArrayMap();
        this.mNewActivities = null;
        this.mNumVisibleActivities = 0;
        this.mLastAssistStructures = new ArrayList();
        this.mServices = new ArrayMap();
        this.mAllApplications = new ArrayList();
        this.mBackupAgents = new ArrayMap();
        this.mInstrumentationPackageName = null;
        this.mInstrumentationAppDir = null;
        this.mInstrumentationSplitAppDirs = null;
        this.mInstrumentationLibDir = null;
        this.mInstrumentedAppDir = null;
        this.mInstrumentedSplitAppDirs = null;
        this.mInstrumentedLibDir = null;
        this.mSystemThread = false;
        this.mJitEnabled = false;
        this.mSomeActivitiesChanged = false;
        this.mUpdatingSystemConfig = false;
        this.mPackages = new ArrayMap();
        this.mResourcePackages = new ArrayMap();
        this.mRelaunchingActivities = new ArrayList();
        this.mPendingConfiguration = null;
        this.mLifecycleSeq = 0;
        this.mProviderMap = new ArrayMap();
        this.mProviderRefCountMap = new ArrayMap();
        this.mLocalProviders = new ArrayMap();
        this.mLocalProvidersByName = new ArrayMap();
        this.mOnPauseListeners = new ArrayMap();
        this.mGcIdler = new GcIdler(this);
        this.mGcIdlerScheduled = false;
        this.mCoreSettings = null;
        this.mMainThreadConfig = new Configuration();
        this.mThumbnailWidth = -1;
        this.mThumbnailHeight = -1;
        this.mAvailThumbnailBitmap = null;
        this.mThumbnailCanvas = null;
        configActivityLogTag();
        this.mResourcesManager = ResourcesManager.getInstance();
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

    public void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        synchronized (this) {
            getSystemContext().installSystemApplicationInfo(info, classLoader);
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
        String str;
        String[] strArr;
        if (!dumpSummaryOnly) {
            String str2;
            String[] strArr2;
            int i2;
            int myPss;
            int mySwappablePss;
            int mySharedDirty;
            int myPrivateDirty;
            int mySharedClean;
            int myPrivateClean;
            int mySwappedOut;
            int mySwappedOutPss;
            if (dumpFullInfo) {
                str2 = HEAP_FULL_COLUMN;
                strArr2 = new Object[11];
                strArr2[0] = "";
                strArr2[1] = "Pss";
                strArr2[2] = "Pss";
                strArr2[3] = "Shared";
                strArr2[4] = "Private";
                strArr2[5] = "Shared";
                strArr2[6] = "Private";
                strArr2[7] = memInfo.hasSwappedOutPss ? "SwapPss" : "Swap";
                strArr2[8] = "Heap";
                strArr2[9] = "Heap";
                strArr2[10] = "Heap";
                printRow(pw, str2, strArr2);
                str = HEAP_FULL_COLUMN;
                strArr = new Object[11];
                strArr[0] = "";
                strArr[1] = "Total";
                strArr[2] = "Clean";
                strArr[3] = "Dirty";
                strArr[4] = "Dirty";
                strArr[5] = "Clean";
                strArr[6] = "Clean";
                strArr[7] = "Dirty";
                strArr[8] = "Size";
                strArr[9] = "Alloc";
                strArr[10] = "Free";
                printRow(pw, str, strArr);
                str = HEAP_FULL_COLUMN;
                strArr = new Object[11];
                strArr[0] = "";
                strArr[1] = "------";
                strArr[2] = "------";
                strArr[3] = "------";
                strArr[4] = "------";
                strArr[5] = "------";
                strArr[6] = "------";
                strArr[7] = "------";
                strArr[8] = "------";
                strArr[9] = "------";
                strArr[10] = "------";
                printRow(pw, str, strArr);
                str2 = HEAP_FULL_COLUMN;
                strArr2 = new Object[11];
                strArr2[0] = "Native Heap";
                strArr2[1] = Integer.valueOf(memInfo.nativePss);
                strArr2[2] = Integer.valueOf(memInfo.nativeSwappablePss);
                strArr2[3] = Integer.valueOf(memInfo.nativeSharedDirty);
                strArr2[4] = Integer.valueOf(memInfo.nativePrivateDirty);
                strArr2[5] = Integer.valueOf(memInfo.nativeSharedClean);
                strArr2[6] = Integer.valueOf(memInfo.nativePrivateClean);
                strArr2[7] = Integer.valueOf(memInfo.hasSwappedOutPss ? memInfo.nativeSwappedOut : memInfo.nativeSwappedOutPss);
                strArr2[8] = Long.valueOf(nativeMax);
                strArr2[9] = Long.valueOf(nativeAllocated);
                strArr2[10] = Long.valueOf(nativeFree);
                printRow(pw, str2, strArr2);
                str2 = HEAP_FULL_COLUMN;
                strArr2 = new Object[11];
                strArr2[0] = "Dalvik Heap";
                strArr2[1] = Integer.valueOf(memInfo.dalvikPss);
                strArr2[2] = Integer.valueOf(memInfo.dalvikSwappablePss);
                strArr2[3] = Integer.valueOf(memInfo.dalvikSharedDirty);
                strArr2[4] = Integer.valueOf(memInfo.dalvikPrivateDirty);
                strArr2[5] = Integer.valueOf(memInfo.dalvikSharedClean);
                strArr2[6] = Integer.valueOf(memInfo.dalvikPrivateClean);
                strArr2[7] = Integer.valueOf(memInfo.hasSwappedOutPss ? memInfo.dalvikSwappedOut : memInfo.dalvikSwappedOutPss);
                strArr2[8] = Long.valueOf(dalvikMax);
                strArr2[9] = Long.valueOf(dalvikAllocated);
                strArr2[10] = Long.valueOf(dalvikFree);
                printRow(pw, str2, strArr2);
            } else {
                str2 = HEAP_COLUMN;
                strArr2 = new Object[8];
                strArr2[0] = "";
                strArr2[1] = "Pss";
                strArr2[2] = "Private";
                strArr2[3] = "Private";
                strArr2[4] = memInfo.hasSwappedOutPss ? "SwapPss" : "Swap";
                strArr2[5] = "Heap";
                strArr2[6] = "Heap";
                strArr2[7] = "Heap";
                printRow(pw, str2, strArr2);
                str = HEAP_COLUMN;
                strArr = new Object[8];
                strArr[0] = "";
                strArr[1] = "Total";
                strArr[2] = "Dirty";
                strArr[3] = "Clean";
                strArr[4] = "Dirty";
                strArr[5] = "Size";
                strArr[6] = "Alloc";
                strArr[7] = "Free";
                printRow(pw, str, strArr);
                str = HEAP_COLUMN;
                strArr = new Object[9];
                strArr[0] = "";
                strArr[1] = "------";
                strArr[2] = "------";
                strArr[3] = "------";
                strArr[4] = "------";
                strArr[5] = "------";
                strArr[6] = "------";
                strArr[7] = "------";
                strArr[8] = "------";
                printRow(pw, str, strArr);
                str2 = HEAP_COLUMN;
                strArr2 = new Object[8];
                strArr2[0] = "Native Heap";
                strArr2[1] = Integer.valueOf(memInfo.nativePss);
                strArr2[2] = Integer.valueOf(memInfo.nativePrivateDirty);
                strArr2[3] = Integer.valueOf(memInfo.nativePrivateClean);
                if (memInfo.hasSwappedOutPss) {
                    i2 = memInfo.nativeSwappedOutPss;
                } else {
                    i2 = memInfo.nativeSwappedOut;
                }
                strArr2[4] = Integer.valueOf(i2);
                strArr2[5] = Long.valueOf(nativeMax);
                strArr2[6] = Long.valueOf(nativeAllocated);
                strArr2[7] = Long.valueOf(nativeFree);
                printRow(pw, str2, strArr2);
                str2 = HEAP_COLUMN;
                strArr2 = new Object[8];
                strArr2[0] = "Dalvik Heap";
                strArr2[1] = Integer.valueOf(memInfo.dalvikPss);
                strArr2[2] = Integer.valueOf(memInfo.dalvikPrivateDirty);
                strArr2[3] = Integer.valueOf(memInfo.dalvikPrivateClean);
                if (memInfo.hasSwappedOutPss) {
                    i2 = memInfo.dalvikSwappedOutPss;
                } else {
                    i2 = memInfo.dalvikSwappedOut;
                }
                strArr2[4] = Integer.valueOf(i2);
                strArr2[5] = Long.valueOf(dalvikMax);
                strArr2[6] = Long.valueOf(dalvikAllocated);
                strArr2[7] = Long.valueOf(dalvikFree);
                printRow(pw, str2, strArr2);
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
                    str2 = HEAP_FULL_COLUMN;
                    strArr2 = new Object[11];
                    strArr2[0] = MemoryInfo.getOtherLabel(i);
                    strArr2[1] = Integer.valueOf(myPss);
                    strArr2[2] = Integer.valueOf(mySwappablePss);
                    strArr2[3] = Integer.valueOf(mySharedDirty);
                    strArr2[4] = Integer.valueOf(myPrivateDirty);
                    strArr2[5] = Integer.valueOf(mySharedClean);
                    strArr2[6] = Integer.valueOf(myPrivateClean);
                    if (memInfo.hasSwappedOutPss) {
                        i2 = mySwappedOutPss;
                    } else {
                        i2 = mySwappedOut;
                    }
                    strArr2[7] = Integer.valueOf(i2);
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
                    if (memInfo.hasSwappedOutPss) {
                        i2 = mySwappedOutPss;
                    } else {
                        i2 = mySwappedOut;
                    }
                    strArr2[4] = Integer.valueOf(i2);
                    strArr2[5] = "";
                    strArr2[6] = "";
                    strArr2[7] = "";
                    printRow(pw, str2, strArr2);
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
                str = HEAP_FULL_COLUMN;
                strArr = new Object[11];
                strArr[0] = "Unknown";
                strArr[1] = Integer.valueOf(otherPss);
                strArr[2] = Integer.valueOf(otherSwappablePss);
                strArr[3] = Integer.valueOf(otherSharedDirty);
                strArr[4] = Integer.valueOf(otherPrivateDirty);
                strArr[5] = Integer.valueOf(otherSharedClean);
                strArr[6] = Integer.valueOf(otherPrivateClean);
                if (!memInfo.hasSwappedOutPss) {
                    otherSwappedOutPss = otherSwappedOut;
                }
                strArr[7] = Integer.valueOf(otherSwappedOutPss);
                strArr[8] = "";
                strArr[9] = "";
                strArr[10] = "";
                printRow(pw, str, strArr);
                str2 = HEAP_FULL_COLUMN;
                strArr2 = new Object[11];
                strArr2[0] = "TOTAL";
                strArr2[1] = Integer.valueOf(memInfo.getTotalPss());
                strArr2[2] = Integer.valueOf(memInfo.getTotalSwappablePss());
                strArr2[3] = Integer.valueOf(memInfo.getTotalSharedDirty());
                strArr2[4] = Integer.valueOf(memInfo.getTotalPrivateDirty());
                strArr2[5] = Integer.valueOf(memInfo.getTotalSharedClean());
                strArr2[6] = Integer.valueOf(memInfo.getTotalPrivateClean());
                if (memInfo.hasSwappedOutPss) {
                    i2 = memInfo.getTotalSwappedOutPss();
                } else {
                    i2 = memInfo.getTotalSwappedOut();
                }
                strArr2[7] = Integer.valueOf(i2);
                strArr2[8] = Long.valueOf(nativeMax + dalvikMax);
                strArr2[9] = Long.valueOf(nativeAllocated + dalvikAllocated);
                strArr2[10] = Long.valueOf(nativeFree + dalvikFree);
                printRow(pw, str2, strArr2);
            } else {
                str = HEAP_COLUMN;
                strArr = new Object[8];
                strArr[0] = "Unknown";
                strArr[1] = Integer.valueOf(otherPss);
                strArr[2] = Integer.valueOf(otherPrivateDirty);
                strArr[3] = Integer.valueOf(otherPrivateClean);
                if (!memInfo.hasSwappedOutPss) {
                    otherSwappedOutPss = otherSwappedOut;
                }
                strArr[4] = Integer.valueOf(otherSwappedOutPss);
                strArr[5] = "";
                strArr[6] = "";
                strArr[7] = "";
                printRow(pw, str, strArr);
                str2 = HEAP_COLUMN;
                strArr2 = new Object[8];
                strArr2[0] = "TOTAL";
                strArr2[1] = Integer.valueOf(memInfo.getTotalPss());
                strArr2[2] = Integer.valueOf(memInfo.getTotalPrivateDirty());
                strArr2[3] = Integer.valueOf(memInfo.getTotalPrivateClean());
                if (memInfo.hasSwappedOutPss) {
                    i2 = memInfo.getTotalSwappedOutPss();
                } else {
                    i2 = memInfo.getTotalSwappedOut();
                }
                strArr2[4] = Integer.valueOf(i2);
                strArr2[5] = Long.valueOf(nativeMax + dalvikMax);
                strArr2[6] = Long.valueOf(nativeAllocated + dalvikAllocated);
                strArr2[7] = Long.valueOf(nativeFree + dalvikFree);
                printRow(pw, str2, strArr2);
            }
            if (dumpDalvik) {
                pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                pw.println(" Dalvik Details");
                for (i = 17; i < 25; i++) {
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
                        if (!memInfo.hasSwappedOutPss) {
                            mySwappedOutPss = mySwappedOut;
                        }
                        strArr[7] = Integer.valueOf(mySwappedOutPss);
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
                        if (!memInfo.hasSwappedOutPss) {
                            mySwappedOutPss = mySwappedOut;
                        }
                        strArr[4] = Integer.valueOf(mySwappedOutPss);
                        strArr[5] = "";
                        strArr[6] = "";
                        strArr[7] = "";
                        printRow(pw, str, strArr);
                    }
                }
            }
        }
        pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        pw.println(" App Summary");
        str = ONE_COUNT_COLUMN_HEADER;
        strArr = new Object[2];
        strArr[0] = "";
        strArr[1] = "Pss(KB)";
        printRow(pw, str, strArr);
        str = ONE_COUNT_COLUMN_HEADER;
        strArr = new Object[2];
        strArr[0] = "";
        strArr[1] = "------";
        printRow(pw, str, strArr);
        str = ONE_COUNT_COLUMN;
        strArr = new Object[2];
        strArr[0] = "Java Heap:";
        strArr[1] = Integer.valueOf(memInfo.getSummaryJavaHeap());
        printRow(pw, str, strArr);
        str = ONE_COUNT_COLUMN;
        strArr = new Object[2];
        strArr[0] = "Native Heap:";
        strArr[1] = Integer.valueOf(memInfo.getSummaryNativeHeap());
        printRow(pw, str, strArr);
        str = ONE_COUNT_COLUMN;
        strArr = new Object[2];
        strArr[0] = "Code:";
        strArr[1] = Integer.valueOf(memInfo.getSummaryCode());
        printRow(pw, str, strArr);
        str = ONE_COUNT_COLUMN;
        strArr = new Object[2];
        strArr[0] = "Stack:";
        strArr[1] = Integer.valueOf(memInfo.getSummaryStack());
        printRow(pw, str, strArr);
        str = ONE_COUNT_COLUMN;
        strArr = new Object[2];
        strArr[0] = "Graphics:";
        strArr[1] = Integer.valueOf(memInfo.getSummaryGraphics());
        printRow(pw, str, strArr);
        str = ONE_COUNT_COLUMN;
        strArr = new Object[2];
        strArr[0] = "Private Other:";
        strArr[1] = Integer.valueOf(memInfo.getSummaryPrivateOther());
        printRow(pw, str, strArr);
        str = ONE_COUNT_COLUMN;
        strArr = new Object[2];
        strArr[0] = "System:";
        strArr[1] = Integer.valueOf(memInfo.getSummarySystem());
        printRow(pw, str, strArr);
        pw.println(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        if (memInfo.hasSwappedOutPss) {
            str = TWO_COUNT_COLUMNS;
            strArr = new Object[4];
            strArr[0] = "TOTAL:";
            strArr[1] = Integer.valueOf(memInfo.getSummaryTotalPss());
            strArr[2] = "TOTAL SWAP PSS:";
            strArr[3] = Integer.valueOf(memInfo.getSummaryTotalSwapPss());
            printRow(pw, str, strArr);
        } else {
            str = TWO_COUNT_COLUMNS;
            strArr = new Object[4];
            strArr[0] = "TOTAL:";
            strArr[1] = Integer.valueOf(memInfo.getSummaryTotalPss());
            strArr[2] = "TOTAL SWAP (KB):";
            strArr[3] = Integer.valueOf(memInfo.getSummaryTotalSwap());
            printRow(pw, str, strArr);
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
            Instrumentation.checkStartActivityResult(-2, intent);
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
        if (DEBUG_RESULTS) {
            Slog.v(TAG, "sendActivityResult: id=" + id + " req=" + requestCode + " res=" + resultCode + " data=" + data);
        }
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
        boolean debugSpecial = DEBUG_ACTIVITY_THREAD ? !"BIND_APPLICATION".equals(this.mH.codeToString(what)) ? "INSTALL_PROVIDER".equals(this.mH.codeToString(what)) : true : false;
        if (DEBUG_MESSAGES || debugSpecial) {
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
        boolean debugSpecial = DEBUG_ACTIVITY_THREAD ? !"BIND_APPLICATION".equals(this.mH.codeToString(what)) ? "INSTALL_PROVIDER".equals(this.mH.codeToString(what)) : true : false;
        if (DEBUG_MESSAGES || debugSpecial) {
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
        Activity activity = null;
        try {
            ClassLoader cl = r.packageInfo.getClassLoader();
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
                Context appContext = createBaseContextForActivity(r, activity);
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
                activity.attach(appContext, this, getInstrumentation(), r.token, r.ident, app, r.intent, r.activityInfo, title, r.parent, r.embeddedID, r.lastNonConfigurationInstances, config, r.referrer, r.voiceInteractor, window);
                if (customIntent != null) {
                    activity.mIntent = customIntent;
                }
                r.lastNonConfigurationInstances = null;
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

    private Context createBaseContextForActivity(ActivityClientRecord r, Activity activity) {
        try {
            Context appContext = ContextImpl.createActivityContext(this, r.packageInfo, r.token, ActivityManagerNative.getDefault().getActivityDisplayId(r.token), r.overrideConfig);
            appContext.setOuterContext(activity);
            Context baseContext = appContext;
            DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();
            String pkgName = SystemProperties.get("debug.second-display.pkg");
            if (pkgName == null || pkgName.isEmpty() || !r.packageInfo.mPackageName.contains(pkgName)) {
                return baseContext;
            }
            for (int id : dm.getDisplayIds()) {
                if (id != 0) {
                    return appContext.createDisplayContext(dm.getCompatibleDisplay(id, appContext.getDisplayAdjustments(id)));
                }
            }
            return baseContext;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void handleLaunchActivity(ActivityClientRecord r, Intent customIntent, String reason) {
        boolean z = true;
        unscheduleGcIdler();
        this.mSomeActivitiesChanged = true;
        if (r.profilerInfo != null) {
            this.mProfiler.setProfiler(r.profilerInfo);
            this.mProfiler.startProfiling();
        }
        handleConfigurationChanged(null, null);
        if (localLOGV || !IS_USER_BUILD || DEBUG_LIFECYCLE) {
            Slog.d(TAG, "Handling launch of " + r + " reason=" + reason + " startsNotResumed=" + r.startsNotResumed);
        }
        WindowManagerGlobal.initialize();
        if (performLaunchActivity(r, customIntent) != null) {
            r.createdConfig = new Configuration(this.mConfiguration);
            reportSizeConfigurations(r);
            Bundle oldState = r.state;
            IBinder iBinder = r.token;
            boolean z2 = r.isForward;
            if (r.activity.mFinished || r.startsNotResumed) {
                z = false;
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
            ActivityManagerNative.getDefault().finishActivity(r.token, 0, null, 0);
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
                ActivityManagerNative.getDefault().reportSizeConfigurations(r.token, horizontal.copyKeys(), vertical.copyKeys(), smallest.copyKeys());
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
            boolean resumed;
            if (r.paused) {
                resumed = false;
            } else {
                resumed = true;
            }
            if (resumed) {
                r.activity.mTemporaryPause = true;
                this.mInstrumentation.callActivityOnPause(r.activity);
            }
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
        AssistContent content = new AssistContent();
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(cmd.activityToken);
        Uri referrer = null;
        if (r != null) {
            r.activity.getApplication().dispatchOnProvideAssistData(r.activity, data);
            r.activity.onProvideAssistData(data);
            referrer = r.activity.onProvideReferrer();
            if (cmd.requestType == 1) {
                structure = new AssistStructure(r.activity);
                Intent activityIntent = r.activity.getIntent();
                if (activityIntent == null || !(r.window == null || (r.window.getAttributes().flags & 8192) == 0)) {
                    content.setDefaultIntent(new Intent());
                } else {
                    Intent intent = new Intent(activityIntent);
                    intent.setFlags(intent.getFlags() & -67);
                    intent.removeUnsafeExtras();
                    content.setDefaultIntent(intent);
                }
                r.activity.onProvideAssistContent(content);
            }
        }
        if (structure == null) {
            structure = new AssistStructure();
        }
        this.mLastAssistStructures.add(new WeakReference(structure));
        try {
            ActivityManagerNative.getDefault().reportAssistContextExtras(cmd.requestToken, data, structure, content, referrer);
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

    public void handleCancelVisibleBehind(IBinder token) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r != null) {
            this.mSomeActivitiesChanged = true;
            Activity activity = r.activity;
            if (activity.mVisibleBehind) {
                activity.mCalled = false;
                activity.onVisibleBehindCanceled();
                if (activity.mCalled) {
                    activity.mVisibleBehind = false;
                } else {
                    throw new SuperNotCalledException("Activity " + activity.getLocalClassName() + " did not call through to super.onVisibleBehindCanceled()");
                }
            }
        }
        try {
            ActivityManagerNative.getDefault().backgroundResourcesReleased(token);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void handleOnBackgroundVisibleBehindChanged(IBinder token, boolean visible) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r != null) {
            r.activity.onBackgroundVisibleBehindChanged(visible);
        }
    }

    public void handleInstallProvider(ProviderInfo info) {
        ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            Context context = this.mInitialApplication;
            ProviderInfo[] providerInfoArr = new ProviderInfo[1];
            providerInfoArr[0] = info;
            installContentProviders(context, Lists.newArrayList(providerInfoArr));
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

    private void handleMultiWindowModeChanged(IBinder token, boolean isInMultiWindowMode) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r != null) {
            r.activity.dispatchMultiWindowModeChanged(isInMultiWindowMode);
        }
    }

    private void handlePictureInPictureModeChanged(IBinder token, boolean isInPipMode) {
        ActivityClientRecord r = (ActivityClientRecord) this.mActivities.get(token);
        if (r != null) {
            r.activity.dispatchPictureInPictureModeChanged(isInPipMode);
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

    public static Intent getIntentBeingBroadcast() {
        return (Intent) sCurrentBroadcastIntent.get();
    }

    private void handleReceiver(ReceiverData data) {
        unscheduleGcIdler();
        String component = data.intent.getComponent().getClassName();
        LoadedApk packageInfo = getPackageInfoNoCheck(data.info.applicationInfo, data.compatInfo);
        IActivityManager mgr = ActivityManagerNative.getDefault();
        try {
            ClassLoader cl = packageInfo.getClassLoader();
            data.intent.setExtrasClassLoader(cl);
            data.intent.prepareToEnterProcess();
            data.setExtrasClassLoader(cl);
            BroadcastReceiver receiver = (BroadcastReceiver) cl.loadClass(component).newInstance();
            try {
                Application app = packageInfo.makeApplication(false, this.mInstrumentation);
                if (DEBUG_BROADCAST) {
                    Slog.v(TAG, "Performing receive of " + data.intent + ": app=" + app + ", appName=" + app.getPackageName() + ", pkg=" + packageInfo.getPackageName() + ", comp=" + data.intent.getComponent().toShortString() + ", dir=" + packageInfo.getAppDir());
                }
                boolean isOrder = data.getOrder();
                if (isOrder) {
                    data.setBroadcastState(data.intent.getFlags(), 2);
                }
                ContextImpl context = (ContextImpl) app.getBaseContext();
                sCurrentBroadcastIntent.set(data.intent);
                receiver.setPendingResult(data);
                if (DEBUG_BROADCAST) {
                    Slog.d(TAG, "BDC-Calling onReceive: intent=" + data.intent + ", receiver=" + receiver);
                }
                if (!sHasCheckBoost) {
                    sHasCheckBoost = true;
                    if ("com.tencent.mm".equals(currentPackageName())) {
                        LMManager lm = LMManager.getLMManager();
                        sShoudCheckBoost = LMManager.sBoostMode == 2;
                    }
                }
                if (sShoudCheckBoost && LMManager.sMODE_2_VALUE_RECEIVER_CLASS.equals(receiver.getClass().getName())) {
                    LMManager.setNewMsgDetected(this.mH, true);
                }
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
        if (DEBUG_BACKUP) {
            Slog.v(TAG, "handleCreateBackupAgent: " + data);
        }
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
                    if (DEBUG_BACKUP) {
                        Slog.v(TAG, "Reusing existing agent instance");
                    }
                    binder = agent.onBind();
                } else {
                    if (DEBUG_BACKUP) {
                        Slog.v(TAG, "Initializing agent class " + classname);
                    }
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
            ActivityManagerNative.getDefault().backupAgentCreated(packageName, binder);
        } catch (RemoteException e3) {
            throw e3.rethrowFromSystemServer();
        }
    }

    private void handleDestroyBackupAgent(CreateBackupAgentData data) {
        if (DEBUG_BACKUP) {
            Slog.v(TAG, "handleDestroyBackupAgent: " + data);
        }
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
            if (DEBUG_SERVICE) {
                Slog.v(TAG, "SVC-Creating service " + data);
            }
            ContextImpl context = ContextImpl.createAppContext(this, packageInfo);
            context.setOuterContext(service);
            service.attach(context, this, data.info.name, data.token, packageInfo.makeApplication(false, this.mInstrumentation), ActivityManagerNative.getDefault());
            service.onCreate();
            this.mServices.put(data.token, service);
            ActivityManagerNative.getDefault().serviceDoneExecuting(data.token, 0, 0, 0);
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
                    ActivityManagerNative.getDefault().serviceDoneExecuting(data.token, 0, 0, 0);
                } else {
                    ActivityManagerNative.getDefault().publishService(data.token, data.intent, s.onBind(data.intent));
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
                    ActivityManagerNative.getDefault().unbindFinished(data.token, data.intent, doRebind);
                } else {
                    ActivityManagerNative.getDefault().serviceDoneExecuting(data.token, 0, 0, 0);
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
                    if (DEBUG_SERVICE) {
                        Slog.d(TAG, "SVC-Calling onStartCommand: " + s + ", flags=" + data.flags + ", startId=" + data.startId);
                    }
                    res = s.onStartCommand(data.args, data.flags, data.startId);
                    if (!(res == 0 || res == 1 || res == 2 || res == 3)) {
                        throw new IllegalArgumentException("Unknown service start result: " + res);
                    }
                }
                QueuedWork.waitToFinish();
                ActivityManagerNative.getDefault().serviceDoneExecuting(data.token, 1, data.startId, res);
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
                if (DEBUG_SERVICE) {
                    Slog.v(TAG, "SVC-Destroying service " + s);
                }
                s.onDestroy();
                Context context = s.getBaseContext();
                if (context instanceof ContextImpl) {
                    ((ContextImpl) context).scheduleFinalCleanup(s.getClassName(), "Service");
                }
                QueuedWork.waitToFinish();
                ActivityManagerNative.getDefault().serviceDoneExecuting(token, 2, 0, 0);
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
        if (!(r == null || r.activity.mFinished)) {
            if (clearHide) {
                r.hideForNow = false;
                r.activity.mStartedActivity = false;
            }
            try {
                r.activity.onStateNotSaved();
                r.activity.mFragments.noteStateNotSaved();
                if (r.pendingIntents != null) {
                    deliverNewIntents(r, r.pendingIntents);
                    r.pendingIntents = null;
                }
                if (r.pendingResults != null) {
                    deliverResults(r, r.pendingResults);
                    r.pendingResults = null;
                }
                r.activity.performResume();
                for (int i = this.mRelaunchingActivities.size() - 1; i >= 0; i--) {
                    ActivityClientRecord relaunching = (ActivityClientRecord) this.mRelaunchingActivities.get(i);
                    if (relaunching.token == r.token && relaunching.onlyLocalRequest && relaunching.startsNotResumed) {
                        relaunching.startsNotResumed = false;
                    }
                }
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(UserHandle.myUserId());
                objArr[1] = r.activity.getComponentName().getClassName();
                objArr[2] = reason;
                EventLog.writeEvent(LOG_AM_ON_RESUME_CALLED, objArr);
                if (!IS_USER_BUILD || DEBUG_LIFECYCLE) {
                    Slog.d(TAG, "ACT-AM_ON_RESUME_CALLED " + r);
                }
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
        if (!r.mPreserveWindow || force) {
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
                mHM = new HypnusManager();
            }
            if (mHM != null) {
                mHM.hypnusSetAction(11, 500);
            }
            ActivityClientRecord r = performResumeActivity(token, clearHide, reason);
            if (r != null) {
                LayoutParams l;
                Activity a = r.activity;
                if (localLOGV) {
                    Slog.v(TAG, "Resume " + r + " started activity: " + a.mStartedActivity + ", hideForNow: " + r.hideForNow + ", finished: " + a.mFinished);
                }
                int forwardBit = isForward ? 256 : 0;
                boolean willBeVisible = !a.mStartedActivity;
                if (!willBeVisible) {
                    try {
                        willBeVisible = ActivityManagerNative.getDefault().willActivityBeVisible(a.getActivityToken());
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
                if (localLOGV) {
                    Slog.v(TAG, "handleResumeActivity r.window = " + r.window + " !a.mFinished = " + (!a.mFinished) + " willBeVisible = " + willBeVisible);
                }
                if (r.window == null && !a.mFinished && willBeVisible) {
                    r.window = r.activity.getWindow();
                    View decor = r.window.getDecorView();
                    decor.setVisibility(4);
                    ViewManager wm = a.getWindowManager();
                    l = r.window.getAttributes();
                    a.mDecor = decor;
                    l.type = 1;
                    l.softInputMode |= forwardBit;
                    if (localLOGV) {
                        Slog.v(TAG, "handleResumeActivity r.mPreserveWindow = " + r.mPreserveWindow);
                    }
                    if (r.mPreserveWindow) {
                        a.mWindowAdded = true;
                        r.mPreserveWindow = false;
                        ViewRootImpl impl = decor.getViewRootImpl();
                        if (impl != null) {
                            impl.notifyChildRebuilt();
                        }
                    }
                    if (localLOGV) {
                        Slog.v(TAG, "handleResumeActivity r.mVisibleFromClient = " + r.mPreserveWindow + "  !a.mWindowAdded = " + (!a.mWindowAdded));
                    }
                    if (a.mVisibleFromClient && !a.mWindowAdded) {
                        a.mWindowAdded = true;
                        wm.addView(decor, l);
                    }
                } else if (!willBeVisible) {
                    if (localLOGV) {
                        Slog.v(TAG, "Launch " + r + " mStartedActivity set");
                    }
                    r.hideForNow = true;
                }
                cleanUpPendingRemoveWindows(r, false);
                if (!(r.activity.mFinished || !willBeVisible || r.activity.mDecor == null || r.hideForNow)) {
                    if (r.newConfig != null) {
                        performConfigurationChangedForActivity(r, r.newConfig, true);
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
                        ActivityManagerNative.getDefault().activityResumed(token);
                    } catch (RemoteException ex) {
                        throw ex.rethrowFromSystemServer();
                    }
                }
            }
            try {
                if (InputLog.DEBUG) {
                    InputLog.d(TAG, "Resume errors cause finishActivity", new Throwable("Kevin_DEBUG"));
                }
                ActivityManagerNative.getDefault().finishActivity(token, 0, null, 0);
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
        if (DEBUG_ORDER) {
            Slog.d(TAG, "handlePauseActivity " + r + ", seq: " + seq);
        }
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
                    ActivityManagerNative.getDefault().activityPaused(token);
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
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(UserHandle.myUserId());
                objArr[1] = r.activity.getComponentName().getClassName();
                objArr[2] = reason;
                EventLog.writeEvent(LOG_AM_ON_PAUSE_CALLED, objArr);
                if (!IS_USER_BUILD || DEBUG_LIFECYCLE) {
                    Slog.d(TAG, "ACT-AM_ON_PAUSE_CALLED " + r);
                }
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
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(UserHandle.myUserId());
                objArr[1] = r.activity.getComponentName().getClassName();
                objArr[2] = reason;
                EventLog.writeEvent(LOG_AM_ON_STOP_CALLED, objArr);
                if (!IS_USER_BUILD || DEBUG_LIFECYCLE) {
                    Slog.d(TAG, "ACT-AM_ON_STOP_CALLED " + r + " reason:" + reason);
                }
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
                performConfigurationChangedForActivity(r, r.newConfig, true);
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
        if (checkAndUpdateLifecycleSeq(seq, r, "stopActivity")) {
            Activity activity = r.activity;
            activity.mConfigChangeFlags |= configChanges;
            StopInfo info = new StopInfo();
            performStopActivityInner(r, info, show, true, "handleStopActivity");
            if (localLOGV) {
                Slog.v(TAG, "Finishing stop of " + r + ": show=" + show + " win=" + r.window);
            }
            updateVisibility(r, show);
            if (!r.isPreHoneycomb()) {
                QueuedWork.waitToFinish();
            }
            info.activity = r;
            info.state = r.state;
            info.persistentState = r.persistentState;
            this.mH.post(info);
            this.mSomeActivitiesChanged = true;
        }
    }

    private static boolean checkAndUpdateLifecycleSeq(int seq, ActivityClientRecord r, String action) {
        if (r == null) {
            return true;
        }
        if (seq < r.lastProcessedSeq) {
            if (DEBUG_ORDER) {
                Slog.d(TAG, action + " for " + r + " ignored, because seq=" + seq + " < mCurrentLifecycleSeq=" + r.lastProcessedSeq);
            }
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
        if (!show && !r.stopped) {
            performStopActivityInner(r, null, show, false, "handleWindowVisibility");
        } else if (show && r.stopped) {
            unscheduleGcIdler();
            r.activity.performRestart();
            r.stopped = false;
        }
        if (r.window == null && r.activity.mDecor == null && !r.activity.mFinished && show) {
            Activity a = r.activity;
            int forwardBit = r.isForward ? 256 : 0;
            r.window = r.activity.getWindow();
            View decor = r.window.getDecorView();
            if (decor != null) {
                decor.setVisibility(4);
            }
            ViewManager wm = a.getWindowManager();
            LayoutParams l = r.window.getAttributes();
            Log.w(TAG, "mDecor is null,has add decor to it " + decor + " r= " + r);
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
            if (!(r.stopped || r.isPreHoneycomb())) {
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
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(UserHandle.myUserId());
                objArr[1] = r.activity.getComponentName().getClassName();
                objArr[2] = "sleeping";
                EventLog.writeEvent(LOG_AM_ON_STOP_CALLED, objArr);
                if (!IS_USER_BUILD || DEBUG_LIFECYCLE) {
                    Slog.d(TAG, "ACT-AM_ON_STOP_CALLED " + r + " sleeping");
                }
            }
            if (!r.isPreHoneycomb()) {
                QueuedWork.waitToFinish();
            }
            try {
                ActivityManagerNative.getDefault().activitySlept(r.token);
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
        boolean debugViewAttributes = this.mCoreSettings.getInt(Global.DEBUG_VIEW_ATTRIBUTES, 0) != 0;
        if (debugViewAttributes != View.mDebugViewAttributes) {
            View.mDebugViewAttributes = debugViewAttributes;
            for (Entry<IBinder, ActivityClientRecord> entry : this.mActivities.entrySet()) {
                requestRelaunchActivity((IBinder) entry.getKey(), null, null, 0, false, null, null, false, false);
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
                if (DEBUG_RESULTS) {
                    Slog.v(TAG, "Delivering result to activity " + r + " : " + ri);
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
        if (DEBUG_RESULTS) {
            Slog.v(TAG, "Handling send result to " + r);
        }
        if (r != null) {
            boolean resumed = !r.paused;
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
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(UserHandle.myUserId());
                objArr[1] = r.activity.getComponentName().getClassName();
                objArr[2] = "destroy";
                EventLog.writeEvent(LOG_AM_ON_STOP_CALLED, objArr);
                if (!IS_USER_BUILD || DEBUG_LIFECYCLE) {
                    Slog.d(TAG, "ACT-AM_ON_STOP_CALLED " + r + " destroy");
                }
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
                ActivityManagerNative.getDefault().activityDestroyed(token);
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        }
        this.mSomeActivitiesChanged = true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:73:0x018b  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0069 A:{SYNTHETIC, Splitter: B:27:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x011d A:{Catch:{ RemoteException -> 0x017e, all -> 0x0177 }} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0126 A:{Catch:{ RemoteException -> 0x017e, all -> 0x0177 }} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x012c A:{Catch:{ RemoteException -> 0x017e, all -> 0x0177 }} */
    /* JADX WARNING: Missing block: B:58:0x013e, code:
            if (DEBUG_ORDER == false) goto L_?;
     */
    /* JADX WARNING: Missing block: B:59:0x0140, code:
            android.util.Slog.d(TAG, "relaunchActivity " + r11 + ", target " + r5 + " operation received seq: " + r5.relaunchSeq);
     */
    /* JADX WARNING: Missing block: B:77:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:78:?, code:
            return;
     */
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
                    if (DEBUG_ORDER) {
                        Slog.d(TAG, "requestRelaunchActivity: " + this + ", trying: " + r);
                    }
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
                            ActivityManagerNative.getDefault().activityRelaunched(token);
                        }
                        target2 = target;
                        if (target2 != null) {
                            try {
                                if (DEBUG_ORDER) {
                                    Slog.d(TAG, "requestRelaunchActivity: target is null, fromServer:" + fromServer);
                                }
                                target = new ActivityClientRecord();
                                target.token = token;
                                target.pendingResults = pendingResults;
                                target.pendingIntents = pendingNewIntents;
                                target.mPreserveWindow = preserveWindow;
                                if (!fromServer) {
                                    ActivityClientRecord existing = (ActivityClientRecord) this.mActivities.get(token);
                                    if (DEBUG_ORDER) {
                                        Slog.d(TAG, "requestRelaunchActivity: " + existing);
                                    }
                                    if (existing != null) {
                                        if (DEBUG_ORDER) {
                                            Slog.d(TAG, "requestRelaunchActivity: paused= " + existing.paused);
                                        }
                                        target.startsNotResumed = existing.paused;
                                        target.overrideConfig = existing.overrideConfig;
                                        if (!IS_USER_BUILD || DEBUG_LIFECYCLE) {
                                            Slog.d(TAG, "requestRelaunchActivity startsNotResumed=" + target.startsNotResumed);
                                        }
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
                    } else {
                        i++;
                    }
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
            android.app.ActivityManagerNative.getDefault().activityRelaunched(r13.token);
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
            if (r6.onlyLocalRequest == false) goto L_0x0194;
     */
    /* JADX WARNING: Missing block: B:65:0x0186, code:
            r9 = android.view.WindowManagerGlobal.getWindowSession();
            r10 = r6.token;
     */
    /* JADX WARNING: Missing block: B:66:0x018e, code:
            if (r6.onlyLocalRequest == false) goto L_0x01fd;
     */
    /* JADX WARNING: Missing block: B:67:0x0190, code:
            r8 = false;
     */
    /* JADX WARNING: Missing block: B:68:0x0191, code:
            r9.prepareToReplaceWindows(r10, r8);
     */
    /* JADX WARNING: Missing block: B:70:0x0196, code:
            if (r6.paused != false) goto L_0x01a5;
     */
    /* JADX WARNING: Missing block: B:71:0x0198, code:
            performPauseActivity(r6.token, false, r6.isPreHoneycomb(), "handleRelaunchActivity");
     */
    /* JADX WARNING: Missing block: B:73:0x01a7, code:
            if (r6.state != null) goto L_0x01ad;
     */
    /* JADX WARNING: Missing block: B:75:0x01ab, code:
            if (r6.stopped == false) goto L_0x0205;
     */
    /* JADX WARNING: Missing block: B:76:0x01ad, code:
            handleDestroyActivity(r6.token, false, r2, true);
            r6.activity = null;
            r6.window = null;
            r6.hideForNow = false;
            r6.nextIdle = null;
     */
    /* JADX WARNING: Missing block: B:77:0x01c2, code:
            if (r13.pendingResults == null) goto L_0x01cc;
     */
    /* JADX WARNING: Missing block: B:79:0x01c6, code:
            if (r6.pendingResults != null) goto L_0x0215;
     */
    /* JADX WARNING: Missing block: B:80:0x01c8, code:
            r6.pendingResults = r13.pendingResults;
     */
    /* JADX WARNING: Missing block: B:82:0x01ce, code:
            if (r13.pendingIntents == null) goto L_0x01d8;
     */
    /* JADX WARNING: Missing block: B:84:0x01d2, code:
            if (r6.pendingIntents != null) goto L_0x021d;
     */
    /* JADX WARNING: Missing block: B:85:0x01d4, code:
            r6.pendingIntents = r13.pendingIntents;
     */
    /* JADX WARNING: Missing block: B:86:0x01d8, code:
            r6.startsNotResumed = r13.startsNotResumed;
            r6.overrideConfig = r13.overrideConfig;
            handleLaunchActivity(r6, r3, "handleRelaunchActivity");
     */
    /* JADX WARNING: Missing block: B:87:0x01e8, code:
            if (r13.onlyLocalRequest != false) goto L_0x01fc;
     */
    /* JADX WARNING: Missing block: B:89:?, code:
            android.app.ActivityManagerNative.getDefault().activityRelaunched(r6.token);
     */
    /* JADX WARNING: Missing block: B:90:0x01f5, code:
            if (r6.window == null) goto L_0x01fc;
     */
    /* JADX WARNING: Missing block: B:91:0x01f7, code:
            r6.window.reportActivityRelaunched();
     */
    /* JADX WARNING: Missing block: B:92:0x01fc, code:
            return;
     */
    /* JADX WARNING: Missing block: B:93:0x01fd, code:
            r8 = true;
     */
    /* JADX WARNING: Missing block: B:94:0x01ff, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:96:0x0204, code:
            throw r4.rethrowFromSystemServer();
     */
    /* JADX WARNING: Missing block: B:98:0x0209, code:
            if (r6.isPreHoneycomb() != false) goto L_0x01ad;
     */
    /* JADX WARNING: Missing block: B:99:0x020b, code:
            callCallActivityOnSaveInstanceState(r6);
            r6.activity.updateSaveInstanceStateReason(3);
     */
    /* JADX WARNING: Missing block: B:100:0x0215, code:
            r6.pendingResults.addAll(r13.pendingResults);
     */
    /* JADX WARNING: Missing block: B:101:0x021d, code:
            r6.pendingIntents.addAll(r13.pendingIntents);
     */
    /* JADX WARNING: Missing block: B:102:0x0225, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:104:0x022a, code:
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
                    if (!ar.activity.mFinished && (allActivities || !ar.paused)) {
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

    private void performConfigurationChangedForActivity(ActivityClientRecord r, Configuration newBaseConfig, boolean reportToActivity) {
        r.tmpConfig.setTo(newBaseConfig);
        if (r.overrideConfig != null) {
            r.tmpConfig.updateFrom(r.overrideConfig);
        }
        performConfigurationChanged(r.activity, r.token, r.tmpConfig, r.overrideConfig, reportToActivity);
        freeTextLayoutCachesIfNeeded(r.activity.mCurrentConfig.diff(r.tmpConfig));
    }

    private static Configuration createNewConfigAndUpdateIfNotNull(Configuration base, Configuration override) {
        if (override == null) {
            return base;
        }
        Configuration newConfig = new Configuration(base);
        newConfig.updateFrom(override);
        return newConfig;
    }

    private void performConfigurationChanged(ComponentCallbacks2 cb, IBinder activityToken, Configuration newConfig, Configuration amOverrideConfig, boolean reportToActivity) {
        Activity activity = cb instanceof Activity ? (Activity) cb : null;
        if (activity != null) {
            activity.mCalled = false;
        }
        boolean shouldChangeConfig = false;
        if (activity == null || activity.mCurrentConfig == null) {
            shouldChangeConfig = true;
        } else {
            int diff = activity.mCurrentConfig.diff(newConfig);
            if (DEBUG_CONFIGURATION) {
                Slog.v(TAG, "diff : " + diff);
            }
            if (!((diff == 0 && this.mResourcesManager.isSameResourcesOverrideConfig(activityToken, amOverrideConfig)) || (this.mUpdatingSystemConfig && ((~activity.mActivityInfo.getRealConfigChanged()) & diff) != 0 && reportToActivity))) {
                shouldChangeConfig = true;
            }
        }
        if (shouldChangeConfig) {
            Configuration contextThemeWrapperOverrideConfig = null;
            if (cb instanceof ContextThemeWrapper) {
                contextThemeWrapperOverrideConfig = ((ContextThemeWrapper) cb).getOverrideConfiguration();
            }
            if (activityToken != null) {
                Configuration finalOverrideConfig = createNewConfigAndUpdateIfNotNull(amOverrideConfig, contextThemeWrapperOverrideConfig);
                String packageName = null;
                if (cb instanceof Context) {
                    packageName = ((Context) cb).getPackageName();
                }
                this.mResourcesManager.updateResourcesForActivity(packageName, activityToken, finalOverrideConfig);
            }
            if (reportToActivity) {
                cb.onConfigurationChanged(createNewConfigAndUpdateIfNotNull(newConfig, contextThemeWrapperOverrideConfig));
            }
            if (activity == null) {
                return;
            }
            if (!reportToActivity || activity.mCalled) {
                activity.mConfigChangeFlags = 0;
                activity.mCurrentConfig = new Configuration(newConfig);
                return;
            }
            throw new SuperNotCalledException("Activity " + activity.getLocalClassName() + " did not call through to super.onConfigurationChanged()");
        }
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

    /* JADX WARNING: Missing block: B:29:0x008c, code:
            r8 = collectComponentCallbacks(false, r14);
            freeTextLayoutCachesIfNeeded(r9);
     */
    /* JADX WARNING: Missing block: B:30:0x0094, code:
            if (r8 == null) goto L_0x00c9;
     */
    /* JADX WARNING: Missing block: B:31:0x0096, code:
            r6 = r8.size();
            r10 = 0;
     */
    /* JADX WARNING: Missing block: B:32:0x009b, code:
            if (r10 >= r6) goto L_0x00c9;
     */
    /* JADX WARNING: Missing block: B:33:0x009d, code:
            r1 = (android.content.ComponentCallbacks2) r8.get(r10);
     */
    /* JADX WARNING: Missing block: B:34:0x00a5, code:
            if ((r1 instanceof android.app.Activity) == false) goto L_0x00c0;
     */
    /* JADX WARNING: Missing block: B:35:0x00a7, code:
            performConfigurationChangedForActivity((android.app.ActivityThread.ActivityClientRecord) r13.mActivities.get(((android.app.Activity) r1).getActivityToken()), r14, true);
     */
    /* JADX WARNING: Missing block: B:36:0x00ba, code:
            r10 = r10 + 1;
     */
    /* JADX WARNING: Missing block: B:40:0x00c0, code:
            performConfigurationChanged(r1, null, r14, null, true);
     */
    /* JADX WARNING: Missing block: B:41:0x00c9, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void handleConfigurationChanged(Configuration config, CompatibilityInfo compat) {
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
            }
        }
    }

    static void freeTextLayoutCachesIfNeeded(int configDiff) {
        boolean hasLocaleConfigChange = false;
        if (configDiff != 0) {
            if ((configDiff & 4) != 0) {
                hasLocaleConfigChange = true;
            }
            if (hasLocaleConfigChange) {
                Canvas.freeTextLayoutCaches();
                if (DEBUG_CONFIGURATION) {
                    Slog.v(TAG, "Cleared TextLayout Caches");
                }
            }
        }
    }

    final void handleActivityConfigurationChanged(ActivityConfigChangeData data, boolean reportToActivity) {
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
        if (r != null && r.activity != null) {
            if (DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Handle activity config changed: " + r.activityInfo.name + ", with callback=" + reportToActivity);
            }
            r.overrideConfig = data.overrideConfig;
            performConfigurationChangedForActivity(r, this.mCompatConfiguration, reportToActivity);
            this.mSomeActivitiesChanged = true;
        }
    }

    final void handleProfilerControl(boolean start, ProfilerInfo profilerInfo, int profileType) {
        if (start) {
            try {
                this.mProfiler.setProfiler(profilerInfo);
                this.mProfiler.startProfiling();
                try {
                    profilerInfo.profileFd.close();
                    return;
                } catch (IOException e) {
                    Slog.w(TAG, "Failure closing profile fd", e);
                    return;
                }
            } catch (RuntimeException e2) {
                Slog.w(TAG, "Profiling failed on path " + profilerInfo.profileFile + " -- can the process access this path?");
                try {
                    profilerInfo.profileFd.close();
                    return;
                } catch (IOException e3) {
                    Slog.w(TAG, "Failure closing profile fd", e3);
                    return;
                }
            } catch (Throwable th) {
                try {
                    profilerInfo.profileFd.close();
                } catch (IOException e32) {
                    Slog.w(TAG, "Failure closing profile fd", e32);
                }
                throw th;
            }
        }
        this.mProfiler.stopProfiling();
    }

    public void stopProfiling() {
        this.mProfiler.stopProfiling();
    }

    static final void handleDumpHeap(boolean managed, DumpHeapData dhd) {
        if (managed) {
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
        }
        Debug.dumpNativeHeap(dhd.fd.getFileDescriptor());
        try {
            ActivityManagerNative.getDefault().dumpHeapFinished(dhd.path);
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
                                    ApplicationInfo aInfo = sPackageManager.getApplicationInfo(packageName, 0, UserHandle.myUserId());
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
        if (level < 15 || this.mBoundApplication == null || this.mBoundApplication.appInfo == null || (this.mBoundApplication.appInfo.privateFlags & 8192) == 0) {
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

    private void setupGraphicsSupport(LoadedApk info, File cacheDir) {
        if (!Process.isIsolated()) {
            Trace.traceBegin(64, "setupGraphicsSupport");
            try {
                if (getPackageManager().getPackagesForUid(Process.myUid()) != null) {
                    ThreadedRenderer.setupDiskCache(cacheDir);
                    RenderScriptCacheDir.setupDiskCache(cacheDir);
                }
                Trace.traceEnd(64);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                Trace.traceEnd(64);
            }
        }
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
        Locale bestLocale = context.getResources().getConfiguration().getLocales().get(0);
        int newLocaleListSize = newLocaleList.size();
        for (int i = 0; i < newLocaleListSize; i++) {
            if (bestLocale.equals(newLocaleList.get(i))) {
                LocaleList.setDefault(newLocaleList, i);
                return;
            }
        }
        LocaleList.setDefault(new LocaleList(bestLocale, newLocaleList));
    }

    private void handleBindApplication(AppBindData data) {
        InstrumentationInfo ii;
        VMRuntime.registerSensitiveThread();
        if (data.trackAllocation) {
            DdmVmInternal.enableRecentAllocations(true);
        }
        if ((data.appInfo.privateFlags & 32768) != 0) {
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
        }
        Process.setArgV0(data.processName);
        DdmHandleAppName.setAppName(data.processName, UserHandle.myUserId());
        if (!(!data.persistent || ActivityManager.isHighEndGfx() || "com.android.systemui".equals(data.processName))) {
            ThreadedRenderer.disable(false);
        }
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
        }
        updateDefaultDensity();
        DateFormat.set24HourTimePref("24".equals(this.mCoreSettings.getString(System.TIME_12_24)));
        View.mDebugViewAttributes = this.mCoreSettings.getInt(Global.DEBUG_VIEW_ATTRIBUTES, 0) != 0;
        if ((data.appInfo.flags & 129) != 0) {
            StrictMode.conditionallyEnableDebugLogging();
        }
        if (data.appInfo.targetSdkVersion >= 11) {
            StrictMode.enableDeathOnNetwork();
        }
        if (data.appInfo.targetSdkVersion >= 24) {
            StrictMode.enableDeathOnFileUriExposure();
        }
        NetworkSecurityPolicy.getInstance().setCleartextTrafficPermitted((data.appInfo.flags & 134217728) != 0);
        if (data.debugMode != 0) {
            Debug.changeDebugPort(8100);
            if (data.debugMode == 2) {
                Slog.w(TAG, "Application " + data.info.getPackageName() + " is waiting for the debugger on port 8100...");
                IActivityManager mgr = ActivityManagerNative.getDefault();
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
            } catch (RemoteException e) {
                Trace.traceEnd(64);
                throw e.rethrowFromSystemServer();
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
            } catch (NameNotFoundException e2) {
                throw new RuntimeException("Unable to find instrumentation info for: " + data.instrumentationName);
            }
        }
        ii = null;
        Context appContext = ContextImpl.createAppContext(this, data.info);
        updateLocaleListFromAppContext(appContext, this.mResourcesManager.getConfiguration().getLocales());
        if (!(Process.isIsolated() || ZenModeConfig.SYSTEM_AUTHORITY.equals(appContext.getPackageName()))) {
            File cacheDir = appContext.getCacheDir();
            if (cacheDir != null) {
                System.setProperty("java.io.tmpdir", cacheDir.getAbsolutePath());
            } else {
                Log.v(TAG, "Unable to initialize \"java.io.tmpdir\" property due to missing cache directory");
            }
            File codeCacheDir = appContext.createDeviceProtectedStorageContext().getCodeCacheDir();
            if (codeCacheDir != null) {
                setupGraphicsSupport(data.info, codeCacheDir);
            } else {
                Log.e(TAG, "Unable to setupGraphicsSupport due to missing code-cache directory");
            }
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
                if (!(this.mProfiler.profileFile == null || ii.handleProfiling || this.mProfiler.profileFd != null)) {
                    this.mProfiler.handlingProfiling = true;
                    File file = new File(this.mProfiler.profileFile);
                    file.getParentFile().mkdirs();
                    Debug.startMethodTracing(file.toString(), 8388608);
                }
            } catch (Throwable e3) {
                throw new RuntimeException("Unable to instantiate instrumentation " + data.instrumentationName + ": " + e3.toString(), e3);
            }
        }
        this.mInstrumentation = new Instrumentation();
        if ((data.appInfo.flags & 1048576) != 0) {
            VMRuntime.getRuntime().clearGrowthLimit();
        } else {
            VMRuntime.getRuntime().clampGrowthLimit();
        }
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
        Application app;
        try {
            app = data.info.makeApplication(data.restrictedBackupMode, null);
            this.mInitialApplication = app;
            if (DEBUG_ACTIVITY_THREAD) {
                Slog.w(TAG, "data.restrictedBackupMode " + data.restrictedBackupMode + " data.providers " + data.providers + " app " + app);
            }
            if (!(data.restrictedBackupMode || ArrayUtils.isEmpty(data.providers))) {
                installContentProviders(app, data.providers);
                this.mH.sendEmptyMessageDelayed(132, 10000);
            }
            this.mInstrumentation.onCreate(data.instrumentationArgs);
            this.mInstrumentation.callApplicationOnCreate(app);
        } catch (Throwable e32) {
            if (!this.mInstrumentation.onException(app, e32)) {
                throw new RuntimeException("Unable to create application " + app.getClass().getName() + ": " + e32.toString(), e32);
            }
        } catch (Throwable e322) {
            throw new RuntimeException("Exception thrown in onCreate() of " + data.instrumentationName + ": " + e322.toString(), e322);
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(savedPolicy);
        }
        StrictMode.setThreadPolicy(savedPolicy);
    }

    final void finishInstrumentation(int resultCode, Bundle results) {
        IActivityManager am = ActivityManagerNative.getDefault();
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
        if (DEBUG_ACTIVITY_THREAD) {
            Slog.w(TAG, "install provider " + providers);
        }
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
            if (DEBUG_ACTIVITY_THREAD) {
                Slog.w(TAG, "publish provider " + results);
            }
            ActivityManagerNative.getDefault().publishContentProviders(getApplicationThread(), results);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public final IContentProvider acquireProvider(Context c, String auth, int userId, boolean stable) {
        String pkgName = "";
        if (this.mInitialApplication != null) {
            pkgName = this.mInitialApplication.getPackageName();
        }
        if (OppoMultiLauncherUtil.getInstance().isMultiApp(pkgName) && auth != null && MediaStore.AUTHORITY.equals(auth) && userId == 999) {
            userId = 0;
        }
        IContentProvider provider = acquireExistingProvider(c, auth, userId, stable);
        if (provider != null) {
            return provider;
        }
        if (WifiEnterpriseConfig.ENGINE_ENABLE.equals(SystemProperties.get("persist.runningbooster.support")) || WifiEnterpriseConfig.ENGINE_ENABLE.equals(SystemProperties.get("ro.mtk_aws_support"))) {
            try {
                if (ActivityManagerNative.getDefault().readyToGetContentProvider(getApplicationThread(), auth, userId) == 1) {
                    try {
                        Thread.sleep(MIN_TIME_BETWEEN_GCS);
                    } catch (InterruptedException ex) {
                        Slog.e(TAG, "InterruptedException " + ex);
                    }
                }
            } catch (RemoteException ex2) {
                Slog.e(TAG, "RemoteException " + ex2);
            }
        }
        try {
            ContentProviderHolder holder = ActivityManagerNative.getDefault().getContentProvider(getApplicationThread(), auth, userId, stable);
            if (holder == null && OppoMultiLauncherUtil.getInstance().isMultiApp(pkgName) && userId == 999) {
                holder = ActivityManagerNative.getDefault().getContentProvider(getApplicationThread(), auth, 0, stable);
            }
            if (holder == null) {
                Slog.e(TAG, "Failed to find provider info for " + auth);
                return null;
            }
            return installProvider(c, holder, holder.info, true, holder.noReleaseNeeded, stable).provider;
        } catch (RemoteException ex22) {
            throw ex22.rethrowFromSystemServer();
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
                    ActivityManagerNative.getDefault().refContentProvider(prc.holder.connection, 1, unstableDelta);
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
            ActivityManagerNative.getDefault().refContentProvider(prc.holder.connection, 0, 1);
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
                            IActivityManager iActivityManager = ActivityManagerNative.getDefault();
                            IBinder iBinder = prc.holder.connection;
                            if (lastRef) {
                                i = 1;
                            }
                            iActivityManager.refContentProvider(iBinder, -1, i);
                        } catch (RemoteException e) {
                        }
                    }
                } else if (DEBUG_PROVIDER) {
                    Slog.v(TAG, "releaseProvider: stable ref count already 0, how?");
                }
            } else if (prc.unstableCount != 0) {
                prc.unstableCount--;
                if (prc.unstableCount == 0) {
                    if (prc.stableCount == 0) {
                        lastRef = true;
                    } else {
                        lastRef = false;
                    }
                    if (!lastRef) {
                        try {
                            if (DEBUG_PROVIDER) {
                                Slog.v(TAG, "releaseProvider: No longer unstable - " + prc.holder.info.name);
                            }
                            ActivityManagerNative.getDefault().refContentProvider(prc.holder.connection, 0, -1);
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
            android.util.Slog.v(TAG, "removeProvider: Invoking ActivityManagerNative.removeContentProvider(" + r10.holder.info.name + ")");
     */
    /* JADX WARNING: Missing block: B:26:0x007e, code:
            android.app.ActivityManagerNative.getDefault().removeContentProvider(r10.holder.connection, false);
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
                    ActivityManagerNative.getDefault().unstableProviderDied(prc.holder.connection);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public static boolean inCptWhiteList(int type, String verifyStr) {
        try {
            return getPackageManager().inCptWhiteList(type, verifyStr);
        } catch (Exception e) {
        } catch (LinkageError e2) {
        }
        return false;
    }

    public static void appDexOptNotify(long start) {
        try {
            ActivityManagerNative.getDefault().appDexOpt(start > 0);
        } catch (Exception e) {
        } catch (LinkageError e2) {
        }
    }

    final void appNotRespondingViaProvider(IBinder provider) {
        synchronized (this.mProviderMap) {
            ProviderRefCount prc = (ProviderRefCount) this.mProviderRefCountMap.get(provider);
            if (prc != null) {
                try {
                    ActivityManagerNative.getDefault().appNotRespondingViaProvider(prc.holder.connection);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    private ProviderClientRecord installProviderAuthoritiesLocked(IContentProvider provider, ContentProvider localProvider, ContentProviderHolder holder) {
        String[] auths = holder.info.authority.split(";");
        int userId = UserHandle.getUserId(holder.info.applicationInfo.uid);
        ProviderClientRecord pcr = new ProviderClientRecord(this, auths, provider, localProvider, holder);
        for (String auth : auths) {
            ProviderKey key = new ProviderKey(auth, userId);
            if (((ProviderClientRecord) this.mProviderMap.get(key)) != null) {
                Slog.w(TAG, "Content provider " + pcr.mHolder.info.name + " already published as " + auth);
            } else {
                this.mProviderMap.put(key, pcr);
            }
        }
        return pcr;
    }

    /* JADX WARNING: Missing block: B:54:0x01d2, code:
            return r17;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ContentProviderHolder installProvider(Context context, ContentProviderHolder holder, ProviderInfo info, boolean noisy, boolean noReleaseNeeded, boolean stable) {
        IContentProvider provider;
        Throwable th;
        ContentProvider localProvider = null;
        if (holder == null || holder.provider == null) {
            if (DEBUG_PROVIDER || noisy || DEBUG_ACTIVITY_THREAD) {
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
            try {
                localProvider = (ContentProvider) c.getClassLoader().loadClass(info.name).newInstance();
                provider = localProvider.getIContentProvider();
                if (provider == null) {
                    Slog.e(TAG, "Failed to instantiate class " + info.name + " from sourceDir " + info.applicationInfo.sourceDir);
                    return null;
                }
                if (DEBUG_PROVIDER || DEBUG_ACTIVITY_THREAD) {
                    Slog.v(TAG, "Instantiating local provider " + info.name);
                }
                localProvider.attachInfo(c, info);
            } catch (Exception e2) {
                if (this.mInstrumentation.onException(null, e2)) {
                    return null;
                }
                throw new RuntimeException("Unable to get provider " + info.name + ": " + e2.toString(), e2);
            }
        }
        provider = holder.provider;
        if (DEBUG_PROVIDER || DEBUG_ACTIVITY_THREAD) {
            Slog.v(TAG, "Installing external provider " + info.authority + ": " + info.name);
        }
        synchronized (this.mProviderMap) {
            try {
                if (DEBUG_PROVIDER || DEBUG_ACTIVITY_THREAD) {
                    Slog.v(TAG, "Checking to add " + provider + " / " + info.name);
                }
                IBinder jBinder = provider.asBinder();
                ContentProviderHolder retHolder;
                if (localProvider != null) {
                    ComponentName cname = new ComponentName(info.packageName, info.name);
                    ProviderClientRecord pr = (ProviderClientRecord) this.mLocalProvidersByName.get(cname);
                    if (pr != null) {
                        if (DEBUG_PROVIDER || DEBUG_ACTIVITY_THREAD) {
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
                        if (DEBUG_PROVIDER || DEBUG_ACTIVITY_THREAD) {
                            Slog.v(TAG, "installProvider: lost the race, updating ref count");
                        }
                        if (!noReleaseNeeded) {
                            incProviderRefLocked(prc, stable);
                            try {
                                ActivityManagerNative.getDefault().removeContentProvider(holder.connection, stable);
                            } catch (RemoteException e3) {
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
        final IActivityManager mgr = ActivityManagerNative.getDefault();
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
            ColorDisplayCompatUtils.getInstance().initData();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
        DropBox.setReporter(new DropBoxReporter(this));
        InputLog.startWatching();
        ViewRootImpl.addConfigCallback(new ComponentCallbacks2() {
            public void onConfigurationChanged(Configuration newConfig) {
                synchronized (ActivityThread.this.mResourcesManager) {
                    if (ActivityThread.this.mResourcesManager.applyConfigurationToResourcesLocked(newConfig, null)) {
                        ActivityThread.this.updateLocaleListFromAppContext(ActivityThread.this.mInitialApplication.getApplicationContext(), ActivityThread.this.mResourcesManager.getConfiguration().getLocales());
                        if (ActivityThread.this.mPendingConfiguration == null || ActivityThread.this.mPendingConfiguration.isOtherSeqNewer(newConfig)) {
                            ActivityThread.this.mPendingConfiguration = newConfig;
                            ActivityThread.this.sendMessage(118, newConfig);
                        }
                    }
                }
            }

            public void onLowMemory() {
            }

            public void onTrimMemory(int level) {
            }
        });
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
        SamplingProfilerIntegration.start();
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
        if (mIsEngBuild) {
            try {
                Looper.myLooper().setMessageLogging(ANRAppManager.getDefault(new ANRAppFrameworks()).newMessageLogger(false));
            } catch (Exception e) {
                Log.d(TAG, "set ANR debugging mechanism state fair " + e);
            }
        }
        Looper.loop();
        throw new RuntimeException("Main thread loop unexpectedly exited");
    }

    public static void enableLooperLog() {
        Log.d(TAG, "Enable Looper Log");
        MessageLogger.mEnableLooperLog = true;
    }

    private void configActivityLogTag() {
        boolean on = true;
        String activitylog = SystemProperties.get("persist.sys.actthreadlog", null);
        if (activitylog != null && !activitylog.equals("")) {
            if (activitylog.indexOf(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER) + 1 >= activitylog.length() || activitylog.indexOf(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER) == -1) {
                Slog.d(TAG, "Invalid argument: " + activitylog);
                SystemProperties.set("persist.sys.actthreadlog", "");
                return;
            }
            String[] args = new String[2];
            args[0] = activitylog.substring(0, activitylog.indexOf(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER));
            args[1] = activitylog.substring(activitylog.indexOf(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER) + 1, activitylog.length());
            String tag = args[0];
            if (!"on".equals(args[1])) {
                on = false;
            }
            configActivityLogTag(tag, on);
        }
    }

    public void configActivityLogTag(String tag, boolean on) {
        Slog.d(TAG, "configActivityLogTag: " + tag + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + on);
        if (tag.equals("x")) {
            DEBUG_MESSAGES = on;
            DEBUG_LIFECYCLE = on;
            DEBUG_BROADCAST = on;
            DEBUG_SERVICE = on;
            localLOGV = on;
        } else if (tag.equals("all")) {
            localLOGV = on;
            DEBUG_MESSAGES = on;
            DEBUG_BROADCAST = on;
            DEBUG_RESULTS = on;
            DEBUG_BACKUP = on;
            DEBUG_CONFIGURATION = on;
            DEBUG_SERVICE = on;
            DEBUG_MEMORY_TRIM = on;
            DEBUG_PROVIDER = on;
            DEBUG_ORDER = on;
            DEBUG_LIFECYCLE = on;
        } else {
            Slog.d(TAG, "Invalid argument: " + tag + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + on);
            SystemProperties.set("persist.sys.actthreadlog", "");
        }
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

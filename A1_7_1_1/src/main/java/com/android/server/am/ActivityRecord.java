package com.android.server.am;

import android.app.ActivityManager.StackId;
import android.app.ActivityManager.TaskDescription;
import android.app.ActivityOptions;
import android.app.IApplicationThread;
import android.app.PendingIntent;
import android.app.ResultInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.service.voice.IVoiceInteractionSession;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import android.view.AppTransitionAnimationSpec;
import android.view.IApplicationToken.Stub;
import com.android.internal.R;
import com.android.internal.app.ResolverActivity;
import com.android.internal.content.ReferrerIntent;
import com.android.internal.util.XmlUtils;
import com.android.server.AttributeCache;
import com.android.server.AttributeCache.Entry;
import com.android.server.wm.WindowManagerService;
import com.mediatek.am.AMEventHookData.WindowsVisible;
import com.mediatek.server.am.AMEventHook.Event;
import com.mediatek.server.am.BootEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
final class ActivityRecord {
    static final String ACTIVITY_ICON_SUFFIX = "_activity_icon_";
    static final int APPLICATION_ACTIVITY_TYPE = 0;
    private static final String ATTR_COMPONENTSPECIFIED = "component_specified";
    private static final String ATTR_ID = "id";
    private static final String ATTR_LAUNCHEDFROMPACKAGE = "launched_from_package";
    private static final String ATTR_LAUNCHEDFROMUID = "launched_from_uid";
    private static final String ATTR_RESOLVEDTYPE = "resolved_type";
    private static final String ATTR_USERID = "user_id";
    public static final String COLOROS_RECENTS_PACKAGE_NAME = "com.coloros.recents.RecentsActivity";
    public static final String COLOROS_SPLIT_PACKAGE_NAME = "com.coloros.recents.SplitChooserActivity";
    static final int HOME_ACTIVITY_TYPE = 1;
    static final int RECENTS_ACTIVITY_TYPE = 2;
    public static final String RECENTS_PACKAGE_NAME = "com.android.systemui.recents";
    private static final boolean SHOW_ACTIVITY_START_TIME = true;
    static final int STARTING_WINDOW_NOT_SHOWN = 0;
    static final int STARTING_WINDOW_REMOVED = 2;
    static final int STARTING_WINDOW_SHOWN = 1;
    private static final String TAG = null;
    private static final String TAG_INTENT = "intent";
    private static final String TAG_PERSISTABLEBUNDLE = "persistable_bundle";
    private static final String TAG_STATES = null;
    private static final String TAG_SWITCH = null;
    private static final String TAG_THUMBNAILS = null;
    protected int SPLIT_BACEING;
    protected int SPLIT_BACE_INVALID;
    protected int SPLIT_BACE_SHOTED;
    ProcessRecord app;
    final ApplicationInfo appInfo;
    AppTimeTracker appTimeTracker;
    final Stub appToken;
    CompatibilityInfo compat;
    final boolean componentSpecified;
    int configChangeFlags;
    Configuration configuration;
    HashSet<ConnectionRecord> connections;
    long cpuTimeAtResume;
    long createTime;
    boolean deferRelaunchUntilPaused;
    boolean delayedResume;
    long displayStartTime;
    boolean finishing;
    boolean forceNewConfig;
    boolean frontOfTask;
    boolean frozenBeforeDestroy;
    boolean fullscreen;
    long fullyDrawnStartTime;
    boolean hasBeenLaunched;
    boolean haveState;
    Bundle icicle;
    int icon;
    boolean idle;
    boolean immersive;
    private boolean inHistory;
    final ActivityInfo info;
    final Intent intent;
    boolean keysPaused;
    int labelRes;
    long lastLaunchTime;
    long lastVisibleTime;
    int launchCount;
    boolean launchFailed;
    int launchMode;
    long launchTickTime;
    final String launchedFromPackage;
    final int launchedFromUid;
    int logo;
    int mActivityType;
    ArrayList<ActivityContainer> mChildContainers;
    private int[] mHorizontalSizeConfigurations;
    ActivityContainer mInitialActivityContainer;
    boolean mLaunchTaskBehind;
    int mRotationAnimationHint;
    private int[] mSmallestSizeConfigurations;
    protected int mSplitBackState;
    final ActivityStackSupervisor mStackSupervisor;
    int mStartingWindowState;
    boolean mTaskOverlay;
    boolean mUpdateTaskThumbnailWhenHidden;
    private int[] mVerticalSizeConfigurations;
    ArrayList<ReferrerIntent> newIntents;
    final boolean noDisplay;
    CharSequence nonLocalizedLabel;
    boolean nowVisible;
    final String packageName;
    long pauseTime;
    ActivityOptions pendingOptions;
    HashSet<WeakReference<PendingIntentRecord>> pendingResults;
    boolean pendingVoiceInteractionStart;
    PersistableBundle persistentState;
    boolean preserveWindowOnDeferredRelaunch;
    final String processName;
    final ComponentName realActivity;
    int realTheme;
    final int requestCode;
    ComponentName requestedVrComponent;
    final String resolvedType;
    ActivityRecord resultTo;
    final String resultWho;
    ArrayList<ResultInfo> results;
    ActivityOptions returningOptions;
    final boolean rootVoiceInteraction;
    final ActivityManagerService service;
    final String shortComponentName;
    boolean sleeping;
    long startTime;
    ActivityState state;
    final boolean stateNotNeeded;
    boolean stopped;
    String stringName;
    TaskRecord task;
    final String taskAffinity;
    Configuration taskConfigOverride;
    TaskDescription taskDescription;
    int theme;
    UriPermissionOwner uriPermissions;
    final int userId;
    boolean visible;
    IVoiceInteractionSession voiceSession;
    int windowFlags;

    static class Token extends Stub {
        private final ActivityManagerService mService;
        private final WeakReference<ActivityRecord> weakActivity;

        Token(ActivityRecord activity, ActivityManagerService service) {
            this.weakActivity = new WeakReference(activity);
            this.mService = service;
        }

        public void windowsDrawn() {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = tokenToActivityRecordLocked(this);
                    if (r != null) {
                        r.windowsDrawnLocked();
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void windowsVisible() {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = tokenToActivityRecordLocked(this);
                    if (r != null) {
                        r.windowsVisibleLocked();
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void windowsGone() {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = tokenToActivityRecordLocked(this);
                    if (r != null) {
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                            Log.v(ActivityRecord.TAG_SWITCH, "windowsGone(): " + r);
                        }
                        r.nowVisible = false;
                    } else {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        /* JADX WARNING: Missing block: B:12:0x001b, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
        /* JADX WARNING: Missing block: B:13:0x0025, code:
            return r6.mService.inputDispatchingTimedOut(r1, r2, r3, false, r7);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean keyDispatchingTimedOut(String reason) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = tokenToActivityRecordLocked(this);
                    if (r == null) {
                    } else {
                        ActivityRecord anrActivity = r.getWaitingHistoryRecordLocked();
                        ProcessRecord anrApp;
                        if (r != null) {
                            anrApp = r.app;
                        } else {
                            anrApp = null;
                        }
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return false;
        }

        public long getKeyDispatchingTimeout() {
            long j;
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = tokenToActivityRecordLocked(this);
                    if (r == null) {
                        j = 0;
                    } else {
                        j = ActivityManagerService.getInputDispatchingTimeoutLocked(r.getWaitingHistoryRecordLocked());
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return j;
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return j;
        }

        public boolean isHomeActivity() {
            ActivityRecord r = tokenToActivityRecordLocked(this);
            if (r == null) {
                return false;
            }
            return r.isHomeActivity();
        }

        public boolean isRecentsActivity() {
            ActivityRecord r = tokenToActivityRecordLocked(this);
            if (r == null) {
                return false;
            }
            return r.isRecentsActivity();
        }

        private static final ActivityRecord tokenToActivityRecordLocked(Token token) {
            if (token == null) {
                return null;
            }
            ActivityRecord r = (ActivityRecord) token.weakActivity.get();
            if (r == null || r.task == null || r.task.stack == null) {
                return null;
            }
            return r;
        }

        public int getFocusAppPid() throws RemoteException {
            ActivityRecord activity = (ActivityRecord) this.weakActivity.get();
            if (activity != null) {
                return activity.getFocusAppPid();
            }
            return -1;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Token{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            sb.append(this.weakActivity.get());
            sb.append('}');
            return sb.toString();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.ActivityRecord.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.ActivityRecord.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityRecord.<clinit>():void");
    }

    private static String startingWindowStateToString(int state) {
        switch (state) {
            case 0:
                return "STARTING_WINDOW_NOT_SHOWN";
            case 1:
                return "STARTING_WINDOW_SHOWN";
            case 2:
                return "STARTING_WINDOW_REMOVED";
            default:
                return "unknown state=" + state;
        }
    }

    void dump(PrintWriter pw, String prefix) {
        long now = SystemClock.uptimeMillis();
        pw.print(prefix);
        pw.print("packageName=");
        pw.print(this.packageName);
        pw.print(" processName=");
        pw.println(this.processName);
        pw.print(prefix);
        pw.print("launchedFromUid=");
        pw.print(this.launchedFromUid);
        pw.print(" launchedFromPackage=");
        pw.print(this.launchedFromPackage);
        pw.print(" userId=");
        pw.println(this.userId);
        pw.print(prefix);
        pw.print("app=");
        pw.println(this.app);
        pw.print(prefix);
        pw.println(this.intent.toInsecureStringWithClip());
        pw.print(prefix);
        pw.print("frontOfTask=");
        pw.print(this.frontOfTask);
        pw.print(" task=");
        pw.println(this.task);
        pw.print(prefix);
        pw.print("taskAffinity=");
        pw.println(this.taskAffinity);
        pw.print(prefix);
        pw.print("realActivity=");
        pw.println(this.realActivity.flattenToShortString());
        if (this.appInfo != null) {
            pw.print(prefix);
            pw.print("baseDir=");
            pw.println(this.appInfo.sourceDir);
            if (!Objects.equals(this.appInfo.sourceDir, this.appInfo.publicSourceDir)) {
                pw.print(prefix);
                pw.print("resDir=");
                pw.println(this.appInfo.publicSourceDir);
            }
            pw.print(prefix);
            pw.print("dataDir=");
            pw.println(this.appInfo.dataDir);
            if (this.appInfo.splitSourceDirs != null) {
                pw.print(prefix);
                pw.print("splitDir=");
                pw.println(Arrays.toString(this.appInfo.splitSourceDirs));
            }
        }
        pw.print(prefix);
        pw.print("stateNotNeeded=");
        pw.print(this.stateNotNeeded);
        pw.print(" componentSpecified=");
        pw.print(this.componentSpecified);
        pw.print(" mActivityType=");
        pw.println(this.mActivityType);
        if (this.rootVoiceInteraction) {
            pw.print(prefix);
            pw.print("rootVoiceInteraction=");
            pw.println(this.rootVoiceInteraction);
        }
        pw.print(prefix);
        pw.print("compat=");
        pw.print(this.compat);
        pw.print(" labelRes=0x");
        pw.print(Integer.toHexString(this.labelRes));
        pw.print(" icon=0x");
        pw.print(Integer.toHexString(this.icon));
        pw.print(" theme=0x");
        pw.println(Integer.toHexString(this.theme));
        pw.print(prefix);
        pw.print("config=");
        pw.println(this.configuration);
        pw.print(prefix);
        pw.print("taskConfigOverride=");
        pw.println(this.taskConfigOverride);
        if (!(this.resultTo == null && this.resultWho == null)) {
            pw.print(prefix);
            pw.print("resultTo=");
            pw.print(this.resultTo);
            pw.print(" resultWho=");
            pw.print(this.resultWho);
            pw.print(" resultCode=");
            pw.println(this.requestCode);
        }
        if (this.taskDescription != null) {
            String iconFilename = this.taskDescription.getIconFilename();
            if (!(iconFilename == null && this.taskDescription.getLabel() == null && this.taskDescription.getPrimaryColor() == 0)) {
                pw.print(prefix);
                pw.print("taskDescription:");
                pw.print(" iconFilename=");
                pw.print(this.taskDescription.getIconFilename());
                pw.print(" label=\"");
                pw.print(this.taskDescription.getLabel());
                pw.print("\"");
                pw.print(" color=");
                pw.println(Integer.toHexString(this.taskDescription.getPrimaryColor()));
            }
            if (iconFilename == null && this.taskDescription.getIcon() != null) {
                pw.print(prefix);
                pw.println("taskDescription contains Bitmap");
            }
        }
        if (this.results != null) {
            pw.print(prefix);
            pw.print("results=");
            pw.println(this.results);
        }
        if (this.pendingResults != null && this.pendingResults.size() > 0) {
            pw.print(prefix);
            pw.println("Pending Results:");
            for (WeakReference<PendingIntentRecord> wpir : this.pendingResults) {
                PendingIntentRecord pir = wpir != null ? (PendingIntentRecord) wpir.get() : null;
                pw.print(prefix);
                pw.print("  - ");
                if (pir == null) {
                    pw.println("null");
                } else {
                    pw.println(pir);
                    pir.dump(pw, prefix + "    ");
                }
            }
        }
        if (this.newIntents != null && this.newIntents.size() > 0) {
            pw.print(prefix);
            pw.println("Pending New Intents:");
            for (int i = 0; i < this.newIntents.size(); i++) {
                Intent intent = (Intent) this.newIntents.get(i);
                pw.print(prefix);
                pw.print("  - ");
                if (intent == null) {
                    pw.println("null");
                } else {
                    pw.println(intent.toShortString(false, true, false, true));
                }
            }
        }
        if (this.pendingOptions != null) {
            pw.print(prefix);
            pw.print("pendingOptions=");
            pw.println(this.pendingOptions);
        }
        if (this.appTimeTracker != null) {
            this.appTimeTracker.dumpWithHeader(pw, prefix, false);
        }
        if (this.uriPermissions != null) {
            this.uriPermissions.dump(pw, prefix);
        }
        pw.print(prefix);
        pw.print("launchFailed=");
        pw.print(this.launchFailed);
        pw.print(" launchCount=");
        pw.print(this.launchCount);
        pw.print(" lastLaunchTime=");
        if (this.lastLaunchTime == 0) {
            pw.print("0");
        } else {
            TimeUtils.formatDuration(this.lastLaunchTime, now, pw);
        }
        pw.println();
        pw.print(prefix);
        pw.print("haveState=");
        pw.print(this.haveState);
        pw.print(" icicle=");
        pw.println(this.icicle);
        pw.print(prefix);
        pw.print("state=");
        pw.print(this.state);
        pw.print(" stopped=");
        pw.print(this.stopped);
        pw.print(" delayedResume=");
        pw.print(this.delayedResume);
        pw.print(" finishing=");
        pw.println(this.finishing);
        pw.print(prefix);
        pw.print("keysPaused=");
        pw.print(this.keysPaused);
        pw.print(" inHistory=");
        pw.print(this.inHistory);
        pw.print(" visible=");
        pw.print(this.visible);
        pw.print(" sleeping=");
        pw.print(this.sleeping);
        pw.print(" idle=");
        pw.print(this.idle);
        pw.print(" mStartingWindowState=");
        pw.println(startingWindowStateToString(this.mStartingWindowState));
        pw.print(prefix);
        pw.print("fullscreen=");
        pw.print(this.fullscreen);
        pw.print(" noDisplay=");
        pw.print(this.noDisplay);
        pw.print(" immersive=");
        pw.print(this.immersive);
        pw.print(" launchMode=");
        pw.println(this.launchMode);
        pw.print(prefix);
        pw.print("frozenBeforeDestroy=");
        pw.print(this.frozenBeforeDestroy);
        pw.print(" forceNewConfig=");
        pw.println(this.forceNewConfig);
        pw.print(prefix);
        pw.print("mActivityType=");
        pw.println(activityTypeToString(this.mActivityType));
        if (this.requestedVrComponent != null) {
            pw.print(prefix);
            pw.print("requestedVrComponent=");
            pw.println(this.requestedVrComponent);
        }
        if (!(this.displayStartTime == 0 && this.startTime == 0)) {
            pw.print(prefix);
            pw.print("displayStartTime=");
            if (this.displayStartTime == 0) {
                pw.print("0");
            } else {
                TimeUtils.formatDuration(this.displayStartTime, now, pw);
            }
            pw.print(" startTime=");
            if (this.startTime == 0) {
                pw.print("0");
            } else {
                TimeUtils.formatDuration(this.startTime, now, pw);
            }
            pw.println();
        }
        boolean waitingVisible = this.mStackSupervisor.mWaitingVisibleActivities.contains(this);
        if (this.lastVisibleTime != 0 || waitingVisible || this.nowVisible) {
            pw.print(prefix);
            pw.print("waitingVisible=");
            pw.print(waitingVisible);
            pw.print(" nowVisible=");
            pw.print(this.nowVisible);
            pw.print(" lastVisibleTime=");
            if (this.lastVisibleTime == 0) {
                pw.print("0");
            } else {
                TimeUtils.formatDuration(this.lastVisibleTime, now, pw);
            }
            pw.println();
        }
        if (this.deferRelaunchUntilPaused || this.configChangeFlags != 0) {
            pw.print(prefix);
            pw.print("deferRelaunchUntilPaused=");
            pw.print(this.deferRelaunchUntilPaused);
            pw.print(" configChangeFlags=");
            pw.println(Integer.toHexString(this.configChangeFlags));
        }
        if (this.connections != null) {
            pw.print(prefix);
            pw.print("connections=");
            pw.println(this.connections);
        }
        if (this.info != null) {
            pw.println(prefix + "resizeMode=" + ActivityInfo.resizeModeToString(this.info.resizeMode));
        }
    }

    public boolean crossesHorizontalSizeThreshold(int firstDp, int secondDp) {
        return crossesSizeThreshold(this.mHorizontalSizeConfigurations, firstDp, secondDp);
    }

    public boolean crossesVerticalSizeThreshold(int firstDp, int secondDp) {
        return crossesSizeThreshold(this.mVerticalSizeConfigurations, firstDp, secondDp);
    }

    public boolean crossesSmallestSizeThreshold(int firstDp, int secondDp) {
        return crossesSizeThreshold(this.mSmallestSizeConfigurations, firstDp, secondDp);
    }

    private static boolean crossesSizeThreshold(int[] thresholds, int firstDp, int secondDp) {
        if (thresholds == null) {
            return false;
        }
        for (int i = thresholds.length - 1; i >= 0; i--) {
            int threshold = thresholds[i];
            if ((firstDp < threshold && secondDp >= threshold) || (firstDp >= threshold && secondDp < threshold)) {
                return true;
            }
        }
        return false;
    }

    public void setSizeConfigurations(int[] horizontalSizeConfiguration, int[] verticalSizeConfigurations, int[] smallestSizeConfigurations) {
        this.mHorizontalSizeConfigurations = horizontalSizeConfiguration;
        this.mVerticalSizeConfigurations = verticalSizeConfigurations;
        this.mSmallestSizeConfigurations = smallestSizeConfigurations;
    }

    void scheduleConfigurationChanged(Configuration config, boolean reportToActivity) {
        if (this.app != null && this.app.thread != null) {
            try {
                Configuration overrideConfig = new Configuration(config);
                overrideConfig.fontScale = this.service.mConfiguration.fontScale;
                if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG, "Sending new config to " + this + " " + "reportToActivity=" + reportToActivity + " and config: " + overrideConfig);
                }
                this.app.thread.scheduleActivityConfigurationChanged(this.appToken, overrideConfig, reportToActivity);
            } catch (RemoteException e) {
            }
        }
    }

    void scheduleMultiWindowModeChanged() {
        if (this.task != null && this.task.stack != null && this.app != null && this.app.thread != null) {
            try {
                boolean z;
                IApplicationThread iApplicationThread = this.app.thread;
                IBinder iBinder = this.appToken;
                if (this.task.mFullscreen) {
                    z = false;
                } else {
                    z = true;
                }
                iApplicationThread.scheduleMultiWindowModeChanged(iBinder, z);
            } catch (Exception e) {
            }
        }
    }

    void schedulePictureInPictureModeChanged() {
        if (this.task != null && this.task.stack != null && this.app != null && this.app.thread != null) {
            try {
                this.app.thread.schedulePictureInPictureModeChanged(this.appToken, this.task.stack.mStackId == 4);
            } catch (Exception e) {
            }
        }
    }

    boolean isFreeform() {
        if (this.task == null || this.task.stack == null || this.task.stack.mStackId != 2) {
            return false;
        }
        return true;
    }

    static ActivityRecord forTokenLocked(IBinder token) {
        try {
            return Token.tokenToActivityRecordLocked((Token) token);
        } catch (ClassCastException e) {
            Slog.w(TAG, "ActivityRecord forTokenLocked, return null. Bad activity token: " + token);
            return null;
        }
    }

    boolean isResolverActivity() {
        return ResolverActivity.class.getName().equals(this.realActivity.getClassName());
    }

    ActivityRecord(ActivityManagerService _service, ProcessRecord _caller, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityContainer container, ActivityOptions options, ActivityRecord sourceRecord) {
        this.createTime = System.currentTimeMillis();
        this.mChildContainers = new ArrayList();
        this.mStartingWindowState = 0;
        this.mTaskOverlay = false;
        this.SPLIT_BACE_INVALID = -1;
        this.mSplitBackState = this.SPLIT_BACE_INVALID;
        this.SPLIT_BACEING = 1;
        this.SPLIT_BACE_SHOTED = 2;
        this.mRotationAnimationHint = -1;
        this.service = _service;
        this.appToken = new Token(this, this.service);
        this.info = aInfo;
        this.launchedFromUid = _launchedFromUid;
        this.launchedFromPackage = _launchedFromPackage;
        this.userId = UserHandle.getUserId(aInfo.applicationInfo.uid);
        this.intent = _intent;
        this.shortComponentName = _intent.getComponent().flattenToShortString();
        this.resolvedType = _resolvedType;
        this.componentSpecified = _componentSpecified;
        this.rootVoiceInteraction = _rootVoiceInteraction;
        this.configuration = _configuration;
        this.taskConfigOverride = Configuration.EMPTY;
        this.resultTo = _resultTo;
        this.resultWho = _resultWho;
        this.requestCode = _reqCode;
        this.state = ActivityState.INITIALIZING;
        this.frontOfTask = false;
        this.launchFailed = false;
        this.stopped = false;
        this.delayedResume = false;
        this.finishing = false;
        this.deferRelaunchUntilPaused = false;
        this.keysPaused = false;
        this.inHistory = false;
        this.visible = false;
        this.nowVisible = false;
        this.idle = false;
        this.hasBeenLaunched = false;
        this.mStackSupervisor = supervisor;
        this.mInitialActivityContainer = container;
        if (options != null) {
            this.pendingOptions = options;
            this.mLaunchTaskBehind = this.pendingOptions.getLaunchTaskBehind();
            this.mRotationAnimationHint = this.pendingOptions.getRotationAnimationHint();
            PendingIntent usageReport = this.pendingOptions.getUsageTimeReport();
            if (usageReport != null) {
                this.appTimeTracker = new AppTimeTracker(usageReport);
            }
        }
        this.haveState = true;
        if (aInfo != null) {
            if (aInfo.targetActivity == null || (aInfo.targetActivity.equals(_intent.getComponent().getClassName()) && (aInfo.launchMode == 0 || aInfo.launchMode == 1))) {
                this.realActivity = _intent.getComponent();
            } else {
                this.realActivity = new ComponentName(aInfo.packageName, aInfo.targetActivity);
            }
            this.taskAffinity = aInfo.taskAffinity;
            this.stateNotNeeded = (aInfo.flags & 16) != 0;
            this.appInfo = aInfo.applicationInfo;
            this.nonLocalizedLabel = aInfo.nonLocalizedLabel;
            this.labelRes = aInfo.labelRes;
            if (this.nonLocalizedLabel == null && this.labelRes == 0) {
                ApplicationInfo app = aInfo.applicationInfo;
                this.nonLocalizedLabel = app.nonLocalizedLabel;
                this.labelRes = app.labelRes;
            }
            this.icon = aInfo.getIconResource();
            this.logo = aInfo.getLogoResource();
            this.theme = aInfo.getThemeResource();
            this.realTheme = this.theme;
            if (this.realTheme == 0) {
                int i;
                if (aInfo.applicationInfo.targetSdkVersion < 11) {
                    i = 16973829;
                } else {
                    i = 16973931;
                }
                this.realTheme = i;
            }
            if ((aInfo.flags & 512) != 0) {
                this.windowFlags |= 16777216;
            }
            if ((aInfo.flags & 1) == 0 || _caller == null || !(aInfo.applicationInfo.uid == 1000 || aInfo.applicationInfo.uid == _caller.info.uid)) {
                this.processName = aInfo.processName;
            } else {
                this.processName = _caller.processName;
            }
            if (!(this.intent == null || (aInfo.flags & 32) == 0)) {
                this.intent.addFlags(8388608);
            }
            this.packageName = aInfo.applicationInfo.packageName;
            this.launchMode = aInfo.launchMode;
            Entry ent = AttributeCache.instance().get(this.packageName, this.realTheme, R.styleable.Window, this.userId);
            boolean translucent = ent != null ? !ent.array.getBoolean(5, false) ? !ent.array.hasValue(5) ? ent.array.getBoolean(25, false) : false : true : false;
            boolean z = (ent == null || ent.array.getBoolean(4, false)) ? false : !translucent;
            this.fullscreen = z;
            this.noDisplay = ent != null ? ent.array.getBoolean(10, false) : false;
            setActivityType(_componentSpecified, _launchedFromUid, _intent, sourceRecord);
            this.immersive = (aInfo.flags & 2048) != 0;
            this.requestedVrComponent = aInfo.requestedVrComponent == null ? null : ComponentName.unflattenFromString(aInfo.requestedVrComponent);
            return;
        }
        this.realActivity = null;
        this.taskAffinity = null;
        this.stateNotNeeded = false;
        this.appInfo = null;
        this.processName = null;
        this.packageName = null;
        this.fullscreen = true;
        this.noDisplay = false;
        this.mActivityType = 0;
        this.immersive = false;
        this.requestedVrComponent = null;
    }

    private boolean isHomeIntent(Intent intent) {
        if ("android.intent.action.MAIN".equals(intent.getAction()) && intent.hasCategory("android.intent.category.HOME") && intent.getCategories().size() == 1 && intent.getData() == null) {
            return intent.getType() == null;
        } else {
            return false;
        }
    }

    static boolean isMainIntent(Intent intent) {
        if ("android.intent.action.MAIN".equals(intent.getAction()) && intent.hasCategory("android.intent.category.LAUNCHER") && intent.getCategories().size() == 1 && intent.getData() == null) {
            return intent.getType() == null;
        } else {
            return false;
        }
    }

    private boolean canLaunchHomeActivity(int uid, ActivityRecord sourceRecord) {
        boolean z = false;
        if (uid == Process.myUid() || uid == 0) {
            return true;
        }
        if (sourceRecord != null) {
            z = sourceRecord.isResolverActivity();
        }
        return z;
    }

    private void setActivityType(boolean componentSpecified, int launchedFromUid, Intent intent, ActivityRecord sourceRecord) {
        if ((!componentSpecified || canLaunchHomeActivity(launchedFromUid, sourceRecord)) && isHomeIntent(intent) && !isResolverActivity()) {
            this.mActivityType = 1;
        } else if (this.realActivity.getClassName().contains(RECENTS_PACKAGE_NAME) || this.realActivity.getClassName().contains(COLOROS_RECENTS_PACKAGE_NAME) || this.realActivity.getClassName().contains(COLOROS_SPLIT_PACKAGE_NAME)) {
            this.mActivityType = 2;
        } else {
            this.mActivityType = 0;
        }
    }

    void setTask(TaskRecord newTask, TaskRecord taskToAffiliateWith) {
        if (!(this.task == null || !this.task.removeActivity(this) || this.task == newTask || this.task.stack == null)) {
            this.task.stack.removeTask(this.task, "setTask");
        }
        this.task = newTask;
        setTaskToAffiliateWith(taskToAffiliateWith);
    }

    void setTaskToAffiliateWith(TaskRecord taskToAffiliateWith) {
        if (taskToAffiliateWith != null && this.launchMode != 3 && this.launchMode != 2) {
            this.task.setTaskToAffiliateWith(taskToAffiliateWith);
        }
    }

    boolean changeWindowTranslucency(boolean toOpaque) {
        if (this.fullscreen == toOpaque) {
            return false;
        }
        TaskRecord taskRecord = this.task;
        taskRecord.numFullscreen = (toOpaque ? 1 : -1) + taskRecord.numFullscreen;
        this.fullscreen = toOpaque;
        return true;
    }

    void putInHistory() {
        if (!this.inHistory) {
            this.inHistory = true;
        }
    }

    void takeFromHistory() {
        if (this.inHistory) {
            this.inHistory = false;
            if (!(this.task == null || this.finishing)) {
                this.task = null;
            }
            clearOptionsLocked();
        }
    }

    boolean isInHistory() {
        return this.inHistory;
    }

    boolean isInStackLocked() {
        return (this.task == null || this.task.stack == null || this.task.stack.isInStackLocked(this) == null) ? false : true;
    }

    boolean isHomeActivity() {
        return this.mActivityType == 1;
    }

    boolean isRecentsActivity() {
        return this.mActivityType == 2;
    }

    boolean isApplicationActivity() {
        return this.mActivityType == 0;
    }

    boolean isPersistable() {
        boolean z = true;
        if (this.info.persistableMode != 0 && this.info.persistableMode != 2) {
            return false;
        }
        if (!(this.intent == null || (this.intent.getFlags() & 8388608) == 0)) {
            z = false;
        }
        return z;
    }

    boolean isFocusable() {
        return !StackId.canReceiveKeys(this.task.stack.mStackId) ? isAlwaysFocusable() : true;
    }

    boolean isResizeable() {
        return !isHomeActivity() ? ActivityInfo.isResizeableMode(this.info.resizeMode) : false;
    }

    boolean isResizeableOrForced() {
        if (isHomeActivity()) {
            return false;
        }
        return !isResizeable() ? this.service.mForceResizableActivities : true;
    }

    boolean isNonResizableOrForced() {
        if (isHomeActivity() || this.info.resizeMode == 2 || this.info.resizeMode == 3) {
            return false;
        }
        return true;
    }

    boolean supportsPictureInPicture() {
        return !isHomeActivity() && this.info.resizeMode == 3;
    }

    boolean canGoInDockedStack() {
        if (isHomeActivity()) {
            return false;
        }
        return isResizeableOrForced() || this.info.resizeMode == 1;
    }

    boolean isAlwaysFocusable() {
        return (this.info.flags & DumpState.DUMP_DOMAIN_PREFERRED) != 0;
    }

    void makeFinishingLocked() {
        if (!this.finishing) {
            if (!(this.task == null || this.task.stack == null || this != this.task.stack.getVisibleBehindActivity())) {
                this.mStackSupervisor.requestVisibleBehindLocked(this, false);
            }
            this.finishing = true;
            if (this.stopped) {
                clearOptionsLocked();
            }
        }
    }

    UriPermissionOwner getUriPermissionsLocked() {
        if (this.uriPermissions == null) {
            this.uriPermissions = new UriPermissionOwner(this.service, this);
        }
        return this.uriPermissions;
    }

    void addResultLocked(ActivityRecord from, String resultWho, int requestCode, int resultCode, Intent resultData) {
        ActivityResult r = new ActivityResult(from, resultWho, requestCode, resultCode, resultData);
        if (this.results == null) {
            this.results = new ArrayList();
        }
        this.results.add(r);
    }

    void removeResultsLocked(ActivityRecord from, String resultWho, int requestCode) {
        if (this.results != null) {
            for (int i = this.results.size() - 1; i >= 0; i--) {
                ActivityResult r = (ActivityResult) this.results.get(i);
                if (r.mFrom == from) {
                    if (r.mResultWho == null) {
                        if (resultWho != null) {
                        }
                    } else if (!r.mResultWho.equals(resultWho)) {
                    }
                    if (r.mRequestCode == requestCode) {
                        this.results.remove(i);
                    }
                }
            }
        }
    }

    void addNewIntentLocked(ReferrerIntent intent) {
        if (this.newIntents == null) {
            this.newIntents = new ArrayList();
        }
        this.newIntents.add(intent);
    }

    final void deliverNewIntentLocked(int callingUid, Intent intent, String referrer) {
        this.service.grantUriPermissionFromIntentLocked(callingUid, this.packageName, intent, getUriPermissionsLocked(), this.userId);
        ReferrerIntent rintent = new ReferrerIntent(intent, referrer);
        boolean unsent = true;
        ActivityStack stack = this.task.stack;
        boolean isTopActivityInStack = stack != null && stack.topRunningActivityLocked() == this;
        boolean isTopActivityWhileSleeping = this.service.isSleepingLocked() ? isTopActivityInStack : false;
        if (!((this.state != ActivityState.RESUMED && this.state != ActivityState.PAUSED && !isTopActivityWhileSleeping) || this.app == null || this.app.thread == null)) {
            try {
                ArrayList<ReferrerIntent> ar = new ArrayList(1);
                ar.add(rintent);
                this.app.thread.scheduleNewIntent(ar, this.appToken, this.state == ActivityState.PAUSED);
                unsent = false;
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception thrown sending new intent to " + this, e);
            } catch (NullPointerException e2) {
                Slog.w(TAG, "Exception thrown sending new intent to " + this, e2);
            }
        }
        if (unsent) {
            addNewIntentLocked(rintent);
        }
    }

    void updateOptionsLocked(ActivityOptions options) {
        if (options != null) {
            if (this.pendingOptions != null) {
                this.pendingOptions.abort();
            }
            this.pendingOptions = options;
        }
    }

    void applyOptionsLocked() {
        if (this.pendingOptions != null && this.pendingOptions.getAnimationType() != 5) {
            int animationType = this.pendingOptions.getAnimationType();
            switch (animationType) {
                case 1:
                    this.service.mWindowManager.overridePendingAppTransition(this.pendingOptions.getPackageName(), this.pendingOptions.getCustomEnterResId(), this.pendingOptions.getCustomExitResId(), this.pendingOptions.getOnAnimationStartListener());
                    break;
                case 2:
                    this.service.mWindowManager.overridePendingAppTransitionScaleUp(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getWidth(), this.pendingOptions.getHeight());
                    if (this.intent.getSourceBounds() == null) {
                        this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + this.pendingOptions.getWidth(), this.pendingOptions.getStartY() + this.pendingOptions.getHeight()));
                        break;
                    }
                    break;
                case 3:
                case 4:
                    this.service.mWindowManager.overridePendingAppTransitionThumb(this.pendingOptions.getThumbnail(), this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getOnAnimationStartListener(), animationType == 3);
                    if (this.intent.getSourceBounds() == null) {
                        this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + this.pendingOptions.getThumbnail().getWidth(), this.pendingOptions.getStartY() + this.pendingOptions.getThumbnail().getHeight()));
                        break;
                    }
                    break;
                case 8:
                case 9:
                    AppTransitionAnimationSpec[] specs = this.pendingOptions.getAnimSpecs();
                    if (animationType == 9 && specs != null) {
                        this.service.mWindowManager.overridePendingAppTransitionMultiThumb(specs, this.pendingOptions.getOnAnimationStartListener(), this.pendingOptions.getAnimationFinishedListener(), false);
                        break;
                    }
                    boolean z;
                    WindowManagerService windowManagerService = this.service.mWindowManager;
                    Bitmap thumbnail = this.pendingOptions.getThumbnail();
                    int startX = this.pendingOptions.getStartX();
                    int startY = this.pendingOptions.getStartY();
                    int width = this.pendingOptions.getWidth();
                    int height = this.pendingOptions.getHeight();
                    IRemoteCallback onAnimationStartListener = this.pendingOptions.getOnAnimationStartListener();
                    if (animationType == 8) {
                        z = true;
                    } else {
                        z = false;
                    }
                    windowManagerService.overridePendingAppTransitionAspectScaledThumb(thumbnail, startX, startY, width, height, onAnimationStartListener, z);
                    if (this.intent.getSourceBounds() == null) {
                        this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + this.pendingOptions.getWidth(), this.pendingOptions.getStartY() + this.pendingOptions.getHeight()));
                        break;
                    }
                    break;
                case 11:
                    this.service.mWindowManager.overridePendingAppTransitionClipReveal(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getWidth(), this.pendingOptions.getHeight());
                    if (this.intent.getSourceBounds() == null) {
                        this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + this.pendingOptions.getWidth(), this.pendingOptions.getStartY() + this.pendingOptions.getHeight()));
                        break;
                    }
                    break;
                default:
                    Slog.e(TAG, "applyOptionsLocked: Unknown animationType=" + animationType);
                    break;
            }
            this.pendingOptions = null;
        }
    }

    ActivityOptions getOptionsForTargetActivityLocked() {
        return this.pendingOptions != null ? this.pendingOptions.forTargetActivity() : null;
    }

    void clearOptionsLocked() {
        if (this.pendingOptions != null) {
            this.pendingOptions.abort();
            this.pendingOptions = null;
        }
    }

    ActivityOptions takeOptionsLocked() {
        ActivityOptions opts = this.pendingOptions;
        this.pendingOptions = null;
        return opts;
    }

    void removeUriPermissionsLocked() {
        if (this.uriPermissions != null) {
            this.uriPermissions.removeUriPermissionsLocked();
            this.uriPermissions = null;
        }
    }

    void pauseKeyDispatchingLocked() {
        if (!this.keysPaused) {
            this.keysPaused = true;
            this.service.mWindowManager.pauseKeyDispatching(this.appToken);
        }
    }

    void resumeKeyDispatchingLocked() {
        if (this.keysPaused) {
            this.keysPaused = false;
            this.service.mWindowManager.resumeKeyDispatching(this.appToken);
        }
    }

    void updateThumbnailLocked(Bitmap newThumbnail, CharSequence description) {
        if (newThumbnail != null) {
            if (ActivityManagerDebugConfig.DEBUG_THUMBNAILS) {
                Slog.i(TAG_THUMBNAILS, "Setting thumbnail of " + this + " to " + newThumbnail);
            }
            if (this.task.setLastThumbnailLocked(newThumbnail) && isPersistable()) {
                this.mStackSupervisor.mService.notifyTaskPersisterLocked(this.task, false);
            }
        }
        this.task.lastDescription = description;
    }

    void startLaunchTickingLocked() {
        if (!ActivityManagerService.IS_USER_BUILD && this.launchTickTime == 0) {
            this.launchTickTime = SystemClock.uptimeMillis();
            continueLaunchTickingLocked();
        }
    }

    boolean continueLaunchTickingLocked() {
        if (this.launchTickTime == 0) {
            return false;
        }
        ActivityStack stack = this.task.stack;
        if (stack == null) {
            return false;
        }
        Message msg = stack.mHandler.obtainMessage(103, this);
        stack.mHandler.removeMessages(103);
        stack.mHandler.sendMessageDelayed(msg, 500);
        return true;
    }

    void finishLaunchTickingLocked() {
        this.launchTickTime = 0;
        ActivityStack stack = this.task.stack;
        if (stack != null) {
            stack.mHandler.removeMessages(103);
        }
    }

    public boolean mayFreezeScreenLocked(ProcessRecord app) {
        return (app == null || app.crashing || app.notResponding) ? false : true;
    }

    public void startFreezingScreenLocked(ProcessRecord app, int configChanges) {
        if (mayFreezeScreenLocked(app)) {
            this.service.mWindowManager.startAppFreezingScreen(this.appToken, configChanges);
        }
    }

    public void stopFreezingScreenLocked(boolean force) {
        if (force || this.frozenBeforeDestroy) {
            this.frozenBeforeDestroy = false;
            this.service.mWindowManager.stopAppFreezingScreen(this.appToken, force);
        }
    }

    public void reportFullyDrawnLocked() {
        long curTime = SystemClock.uptimeMillis();
        if (this.displayStartTime != 0) {
            reportLaunchTimeLocked(curTime);
        }
        ActivityStack stack = this.task.stack;
        if (!(this.fullyDrawnStartTime == 0 || stack == null)) {
            long thisTime = curTime - this.fullyDrawnStartTime;
            long totalTime = stack.mFullyDrawnStartTime != 0 ? curTime - stack.mFullyDrawnStartTime : thisTime;
            Trace.asyncTraceEnd(64, "drawing", 0);
            Object[] objArr = new Object[5];
            objArr[0] = Integer.valueOf(this.userId);
            objArr[1] = Integer.valueOf(System.identityHashCode(this));
            objArr[2] = this.shortComponentName;
            objArr[3] = Long.valueOf(thisTime);
            objArr[4] = Long.valueOf(totalTime);
            EventLog.writeEvent(EventLogTags.AM_ACTIVITY_FULLY_DRAWN_TIME, objArr);
            StringBuilder sb = this.service.mStringBuilder;
            sb.setLength(0);
            sb.append("Fully drawn ");
            sb.append(this.shortComponentName);
            sb.append(": ");
            TimeUtils.formatDuration(thisTime, sb);
            if (thisTime != totalTime) {
                sb.append(" (total ");
                TimeUtils.formatDuration(totalTime, sb);
                sb.append(")");
            }
            Log.i(TAG, sb.toString());
            if (totalTime > 0) {
            }
            stack.mFullyDrawnStartTime = 0;
        }
        this.fullyDrawnStartTime = 0;
    }

    private void reportLaunchTimeLocked(long curTime) {
        ActivityStack stack = this.task.stack;
        if (stack != null) {
            long thisTime = curTime - this.displayStartTime;
            long totalTime = stack.mLaunchStartTime != 0 ? curTime - stack.mLaunchStartTime : thisTime;
            Trace.asyncTraceEnd(64, "launching: " + this.packageName, 0);
            Object[] objArr = new Object[5];
            objArr[0] = Integer.valueOf(this.userId);
            objArr[1] = Integer.valueOf(System.identityHashCode(this));
            objArr[2] = this.shortComponentName;
            objArr[3] = Long.valueOf(thisTime);
            objArr[4] = Long.valueOf(totalTime);
            EventLog.writeEvent(EventLogTags.AM_ACTIVITY_LAUNCH_TIME, objArr);
            StringBuilder sb = this.service.mStringBuilder;
            sb.setLength(0);
            sb.append("Displayed ");
            sb.append(this.shortComponentName);
            sb.append(": ");
            TimeUtils.formatDuration(thisTime, sb);
            if (thisTime != totalTime) {
                sb.append(" (total ");
                TimeUtils.formatDuration(totalTime, sb);
                sb.append(")");
            }
            Log.i(TAG, sb.toString());
            BootEvent.addBootEvent("AP_Launch: " + this.shortComponentName + " " + thisTime + "ms");
            this.service.reportJunkFromApp("LaunchTime", this.shortComponentName, sb.toString(), false);
            this.mStackSupervisor.reportActivityLaunchedLocked(false, this, thisTime, totalTime);
            if (totalTime > 0) {
            }
            this.displayStartTime = 0;
            stack.mLaunchStartTime = 0;
        }
    }

    void windowsDrawnLocked() {
        this.mStackSupervisor.mActivityMetricsLogger.notifyWindowsDrawn();
        if (this.displayStartTime != 0) {
            reportLaunchTimeLocked(SystemClock.uptimeMillis());
        }
        this.mStackSupervisor.sendWaitingVisibleReportLocked(this);
        this.startTime = 0;
        finishLaunchTickingLocked();
        if (this.task != null) {
            this.task.hasBeenVisible = true;
        }
    }

    void windowsVisibleLocked() {
        this.mStackSupervisor.reportActivityVisibleLocked(this);
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Log.v(TAG_SWITCH, "windowsVisibleLocked(): " + this);
        }
        if (!this.nowVisible) {
            this.nowVisible = true;
            this.lastVisibleTime = SystemClock.uptimeMillis();
            if (this.idle) {
                int size = this.mStackSupervisor.mWaitingVisibleActivities.size();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        ActivityRecord r = (ActivityRecord) this.mStackSupervisor.mWaitingVisibleActivities.get(i);
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                            Log.v(TAG_SWITCH, "Was waiting for visible: " + r);
                        }
                    }
                    this.mStackSupervisor.mWaitingVisibleActivities.clear();
                    this.mStackSupervisor.scheduleIdleLocked();
                }
            } else {
                this.mStackSupervisor.processStoppingActivitiesLocked(false);
            }
            this.service.scheduleAppGcsLocked();
        }
        this.service.getAMEventHook().hook(Event.AM_WindowsVisible, WindowsVisible.createInstance());
    }

    ActivityRecord getWaitingHistoryRecordLocked() {
        if (this.mStackSupervisor.mWaitingVisibleActivities.contains(this) || this.stopped) {
            ActivityStack stack = this.mStackSupervisor.getFocusedStack();
            ActivityRecord r = stack.mResumedActivity;
            if (r == null) {
                r = stack.mPausingActivity;
            }
            if (r != null) {
                return r;
            }
        }
        return this;
    }

    public int getFocusAppPid() {
        if (this == null || this.app == null) {
            return -1;
        }
        return this.app.pid;
    }

    public boolean isInterestingToUserLocked() {
        if (this.visible || this.nowVisible || this.state == ActivityState.PAUSING || this.state == ActivityState.RESUMED) {
            return true;
        }
        return false;
    }

    void setSleeping(boolean _sleeping) {
        setSleeping(_sleeping, false);
    }

    void setSleeping(boolean _sleeping, boolean force) {
        if (!((!force && this.sleeping == _sleeping) || this.app == null || this.app.thread == null)) {
            try {
                this.app.thread.scheduleSleeping(this.appToken, _sleeping);
                if (_sleeping && !this.mStackSupervisor.mGoingToSleepActivities.contains(this)) {
                    this.mStackSupervisor.mGoingToSleepActivities.add(this);
                }
                this.sleeping = _sleeping;
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception thrown when sleeping: " + this.intent.getComponent(), e);
            }
        }
    }

    static int getTaskForActivityLocked(IBinder token, boolean onlyRoot) {
        ActivityRecord r = forTokenLocked(token);
        if (r == null) {
            return -1;
        }
        TaskRecord task = r.task;
        int activityNdx = task.mActivities.indexOf(r);
        if (activityNdx < 0 || (onlyRoot && activityNdx > task.findEffectiveRootIndex())) {
            return -1;
        }
        return task.taskId;
    }

    static ActivityRecord isInStackLocked(IBinder token) {
        ActivityRecord r = forTokenLocked(token);
        if (r != null) {
            return r.task.stack.isInStackLocked(r);
        }
        return null;
    }

    static ActivityStack getStackLocked(IBinder token) {
        ActivityRecord r = isInStackLocked(token);
        if (r != null) {
            return r.task.stack;
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:13:0x0020, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean isDestroyable() {
        if (this.finishing || this.app == null || this.state == ActivityState.DESTROYING || this.state == ActivityState.DESTROYED || this.task == null || this.task.stack == null || this == this.task.stack.mResumedActivity || this == this.task.stack.mPausingActivity || !this.haveState || !this.stopped || this.visible) {
            return false;
        }
        return true;
    }

    private static String createImageFilename(long createTime, int taskId) {
        return String.valueOf(taskId) + ACTIVITY_ICON_SUFFIX + createTime + ".png";
    }

    void setTaskDescription(TaskDescription _taskDescription) {
        if (_taskDescription.getIconFilename() == null) {
            Bitmap icon = _taskDescription.getIcon();
            if (icon != null) {
                String iconFilePath = new File(TaskPersister.getUserImagesDir(this.userId), createImageFilename(this.createTime, this.task.taskId)).getAbsolutePath();
                this.service.mRecentTasks.saveImage(icon, iconFilePath);
                _taskDescription.setIconFilename(iconFilePath);
            }
        }
        this.taskDescription = _taskDescription;
    }

    void setVoiceSessionLocked(IVoiceInteractionSession session) {
        this.voiceSession = session;
        this.pendingVoiceInteractionStart = false;
    }

    void clearVoiceSessionLocked() {
        this.voiceSession = null;
        this.pendingVoiceInteractionStart = false;
    }

    void showStartingWindow(ActivityRecord prev, boolean createIfNeeded) {
        IBinder iBinder = null;
        CompatibilityInfo compatInfo = this.service.compatibilityInfoForPackageLocked(this.info.applicationInfo);
        WindowManagerService windowManagerService = this.service.mWindowManager;
        IBinder iBinder2 = this.appToken;
        String str = this.packageName;
        int i = this.theme;
        CharSequence charSequence = this.nonLocalizedLabel;
        int i2 = this.labelRes;
        int i3 = this.icon;
        int i4 = this.logo;
        int i5 = this.windowFlags;
        if (prev != null) {
            iBinder = prev.appToken;
        }
        if (windowManagerService.setAppStartingWindow(iBinder2, str, i, compatInfo, charSequence, i2, i3, i4, i5, iBinder, createIfNeeded)) {
            this.mStartingWindowState = 1;
        }
    }

    void saveToXml(XmlSerializer out) throws IOException, XmlPullParserException {
        out.attribute(null, "id", String.valueOf(this.createTime));
        out.attribute(null, ATTR_LAUNCHEDFROMUID, String.valueOf(this.launchedFromUid));
        if (this.launchedFromPackage != null) {
            out.attribute(null, ATTR_LAUNCHEDFROMPACKAGE, this.launchedFromPackage);
        }
        if (this.resolvedType != null) {
            out.attribute(null, ATTR_RESOLVEDTYPE, this.resolvedType);
        }
        out.attribute(null, ATTR_COMPONENTSPECIFIED, String.valueOf(this.componentSpecified));
        out.attribute(null, ATTR_USERID, String.valueOf(this.userId));
        if (this.taskDescription != null) {
            this.taskDescription.saveToXml(out);
        }
        out.startTag(null, TAG_INTENT);
        this.intent.saveToXml(out);
        out.endTag(null, TAG_INTENT);
        if (isPersistable() && this.persistentState != null) {
            out.startTag(null, TAG_PERSISTABLEBUNDLE);
            this.persistentState.saveToXml(out);
            out.endTag(null, TAG_PERSISTABLEBUNDLE);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x013d  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0123  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static ActivityRecord restoreFromXml(XmlPullParser in, ActivityStackSupervisor stackSupervisor) throws IOException, XmlPullParserException {
        Intent intent = null;
        PersistableBundle persistentState = null;
        int launchedFromUid = 0;
        String launchedFromPackage = null;
        String resolvedType = null;
        boolean componentSpecified = false;
        int userId = 0;
        long createTime = -1;
        int outerDepth = in.getDepth();
        TaskDescription taskDescription = new TaskDescription();
        for (int attrNdx = in.getAttributeCount() - 1; attrNdx >= 0; attrNdx--) {
            String attrName = in.getAttributeName(attrNdx);
            String attrValue = in.getAttributeValue(attrNdx);
            if ("id".equals(attrName)) {
                createTime = Long.valueOf(attrValue).longValue();
            } else if (ATTR_LAUNCHEDFROMUID.equals(attrName)) {
                launchedFromUid = Integer.parseInt(attrValue);
            } else if (ATTR_LAUNCHEDFROMPACKAGE.equals(attrName)) {
                launchedFromPackage = attrValue;
            } else if (ATTR_RESOLVEDTYPE.equals(attrName)) {
                resolvedType = attrValue;
            } else if (ATTR_COMPONENTSPECIFIED.equals(attrName)) {
                componentSpecified = Boolean.valueOf(attrValue).booleanValue();
            } else if (ATTR_USERID.equals(attrName)) {
                userId = Integer.parseInt(attrValue);
            } else {
                if (attrName.startsWith("task_description_")) {
                    taskDescription.restoreFromXml(attrName, attrValue);
                } else {
                    Log.d(TAG, "Unknown ActivityRecord attribute=" + attrName);
                }
            }
        }
        while (true) {
            int event = in.next();
            if (event == 1 || (event == 3 && in.getDepth() < outerDepth)) {
                if (intent != null) {
                    throw new XmlPullParserException("restoreActivity error intent=" + intent);
                }
                ActivityManagerService service = stackSupervisor.mService;
                ActivityInfo aInfo = stackSupervisor.resolveActivity(intent, resolvedType, 0, null, userId);
                if (aInfo == null) {
                    throw new XmlPullParserException("restoreActivity resolver error. Intent=" + intent + " resolvedType=" + resolvedType);
                }
                ActivityRecord r = new ActivityRecord(service, null, launchedFromUid, launchedFromPackage, intent, resolvedType, aInfo, service.getConfiguration(), null, null, 0, componentSpecified, false, stackSupervisor, null, null, null);
                r.persistentState = persistentState;
                r.taskDescription = taskDescription;
                r.createTime = createTime;
                return r;
            } else if (event == 2) {
                String name = in.getName();
                if (TAG_INTENT.equals(name)) {
                    intent = Intent.restoreFromXml(in);
                } else if (TAG_PERSISTABLEBUNDLE.equals(name)) {
                    persistentState = PersistableBundle.restoreFromXml(in);
                } else {
                    Slog.w(TAG, "restoreActivity: unexpected name=" + name);
                    XmlUtils.skipCurrentTag(in);
                }
            }
        }
        if (intent != null) {
        }
    }

    private static String activityTypeToString(int type) {
        switch (type) {
            case 0:
                return "APPLICATION_ACTIVITY_TYPE";
            case 1:
                return "HOME_ACTIVITY_TYPE";
            case 2:
                return "RECENTS_ACTIVITY_TYPE";
            default:
                return Integer.toString(type);
        }
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName + " t" + (this.task == null ? -1 : this.task.taskId) + (this.finishing ? " f}" : "}");
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ActivityRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" u");
        sb.append(this.userId);
        sb.append(' ');
        sb.append(this.intent.getComponent().flattenToShortString());
        this.stringName = sb.toString();
        return toString();
    }
}

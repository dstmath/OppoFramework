package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityThread;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.app.WaitResult;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.AuxiliaryResolveInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.voice.IVoiceInteractionSession;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Pools;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.HeavyWeightSwitcherActivity;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.SystemService;
import com.android.server.am.EventLogTags;
import com.android.server.am.IColorAbnormalAppManager;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.am.IColorFastAppManager;
import com.android.server.am.IColorGameSpaceManager;
import com.android.server.am.IColorHansManager;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.am.PendingIntentRecord;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.stat.AppBrightnessStat;
import com.android.server.engineer.OppoEngineerFunctionManager;
import com.android.server.pm.CompatibilityHelper;
import com.android.server.pm.DumpState;
import com.android.server.pm.InstantAppResolver;
import com.android.server.pm.PackageManagerService;
import com.android.server.util.ColorZoomWindowManagerHelper;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.ActivityStackSupervisor;
import com.android.server.wm.LaunchParamsController;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.oppo.hypnus.Hypnus;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/* access modifiers changed from: package-private */
public class ActivityStarter extends OppoBaseActivityStarter {
    private static final int INVALID_LAUNCH_MODE = -1;
    private static final String TAG = "ActivityTaskManager";
    private static final String TAG_CONFIGURATION = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_CONFIGURATION);
    private static final String TAG_FOCUS = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_FOCUS);
    private static final String TAG_RESULTS = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_RESULTS);
    private static final String TAG_USER_LEAVING = (TAG + ActivityTaskManagerDebugConfig.POSTFIX_USER_LEAVING);
    private boolean mAddingToTask;
    private boolean mAvoidMoveToFront;
    private int mCallingUid;
    private final ActivityStartController mController;
    private boolean mDoResume;
    public Hypnus mHyp = null;
    private TaskRecord mInTask;
    private Intent mIntent;
    private boolean mIntentDelivered;
    private final ActivityStartInterceptor mInterceptor;
    private boolean mKeepCurTransition;
    private final ActivityRecord[] mLastStartActivityRecord = new ActivityRecord[1];
    private int mLastStartActivityResult;
    private long mLastStartActivityTimeMs;
    private String mLastStartReason;
    private int mLaunchFlags;
    private int mLaunchMode;
    private LaunchParamsController.LaunchParams mLaunchParams = new LaunchParamsController.LaunchParams();
    private boolean mLaunchTaskBehind;
    private boolean mMovedToFront;
    private ActivityInfo mNewTaskInfo;
    private Intent mNewTaskIntent;
    private boolean mNoAnimation;
    private ActivityRecord mNotTop;
    private ActivityOptions mOptions;
    private int mPreferredDisplayId;
    private Request mRequest = new Request();
    private boolean mRestrictedBgActivity;
    private TaskRecord mReuseTask;
    private final RootActivityContainer mRootActivityContainer;
    private final ActivityTaskManagerService mService;
    private ActivityRecord mSourceRecord;
    private ActivityStack mSourceStack;
    private ActivityRecord mStartActivity;
    private int mStartFlags;
    private final ActivityStackSupervisor mSupervisor;
    private ActivityStack mTargetStack;
    private IVoiceInteractor mVoiceInteractor;
    private IVoiceInteractionSession mVoiceSession;

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public interface Factory {
        ActivityStarter obtain();

        void recycle(ActivityStarter activityStarter);

        void setController(ActivityStartController activityStartController);
    }

    /* access modifiers changed from: package-private */
    public static class DefaultFactory implements Factory {
        private final int MAX_STARTER_COUNT = 3;
        private ActivityStartController mController;
        private ActivityStartInterceptor mInterceptor;
        private ActivityTaskManagerService mService;
        private Pools.SynchronizedPool<ActivityStarter> mStarterPool = new Pools.SynchronizedPool<>(3);
        private ActivityStackSupervisor mSupervisor;

        DefaultFactory(ActivityTaskManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
            this.mService = service;
            this.mSupervisor = supervisor;
            this.mInterceptor = interceptor;
        }

        @Override // com.android.server.wm.ActivityStarter.Factory
        public void setController(ActivityStartController controller) {
            this.mController = controller;
        }

        @Override // com.android.server.wm.ActivityStarter.Factory
        public ActivityStarter obtain() {
            ActivityStarter starter = (ActivityStarter) this.mStarterPool.acquire();
            if (starter != null) {
                return starter;
            }
            ActivityTaskManagerService activityTaskManagerService = this.mService;
            return activityTaskManagerService.createActivityStarter(this.mController, activityTaskManagerService, this.mSupervisor, this.mInterceptor);
        }

        @Override // com.android.server.wm.ActivityStarter.Factory
        public void recycle(ActivityStarter starter) {
            starter.reset(true);
            this.mStarterPool.release(starter);
        }
    }

    /* access modifiers changed from: private */
    public static class Request {
        private static final int DEFAULT_CALLING_PID = 0;
        private static final int DEFAULT_CALLING_UID = -1;
        static final int DEFAULT_REAL_CALLING_PID = 0;
        static final int DEFAULT_REAL_CALLING_UID = -1;
        ActivityInfo activityInfo;
        SafeActivityOptions activityOptions;
        boolean allowBackgroundActivityStart;
        boolean allowPendingRemoteAnimationRegistryLookup;
        boolean avoidMoveToFront;
        IApplicationThread caller;
        String callingPackage;
        int callingPid = 0;
        int callingUid = -1;
        boolean componentSpecified;
        Intent ephemeralIntent;
        int filterCallingUid;
        Configuration globalConfig;
        boolean ignoreTargetSecurity;
        TaskRecord inTask;
        Intent intent;
        boolean mayWait;
        PendingIntentRecord originatingPendingIntent;
        ActivityRecord[] outActivity;
        ProfilerInfo profilerInfo;
        int realCallingPid = 0;
        int realCallingUid = -1;
        String reason;
        int requestCode;
        ResolveInfo resolveInfo;
        String resolvedType;
        IBinder resultTo;
        String resultWho;
        int startFlags;
        int userId;
        IVoiceInteractor voiceInteractor;
        IVoiceInteractionSession voiceSession;
        WaitResult waitResult;

        Request() {
            reset();
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.caller = null;
            this.intent = null;
            this.ephemeralIntent = null;
            this.resolvedType = null;
            this.activityInfo = null;
            this.resolveInfo = null;
            this.voiceSession = null;
            this.voiceInteractor = null;
            this.resultTo = null;
            this.resultWho = null;
            this.requestCode = 0;
            this.callingPid = 0;
            this.callingUid = -1;
            this.callingPackage = null;
            this.realCallingPid = 0;
            this.realCallingUid = -1;
            this.startFlags = 0;
            this.activityOptions = null;
            this.ignoreTargetSecurity = false;
            this.componentSpecified = false;
            this.outActivity = null;
            this.inTask = null;
            this.reason = null;
            this.profilerInfo = null;
            this.globalConfig = null;
            this.userId = 0;
            this.waitResult = null;
            this.mayWait = false;
            this.avoidMoveToFront = false;
            this.allowPendingRemoteAnimationRegistryLookup = true;
            this.filterCallingUid = -10000;
            this.originatingPendingIntent = null;
            this.allowBackgroundActivityStart = false;
        }

        /* access modifiers changed from: package-private */
        public void set(Request request) {
            this.caller = request.caller;
            this.intent = request.intent;
            this.ephemeralIntent = request.ephemeralIntent;
            this.resolvedType = request.resolvedType;
            this.activityInfo = request.activityInfo;
            this.resolveInfo = request.resolveInfo;
            this.voiceSession = request.voiceSession;
            this.voiceInteractor = request.voiceInteractor;
            this.resultTo = request.resultTo;
            this.resultWho = request.resultWho;
            this.requestCode = request.requestCode;
            this.callingPid = request.callingPid;
            this.callingUid = request.callingUid;
            this.callingPackage = request.callingPackage;
            this.realCallingPid = request.realCallingPid;
            this.realCallingUid = request.realCallingUid;
            this.startFlags = request.startFlags;
            this.activityOptions = request.activityOptions;
            this.ignoreTargetSecurity = request.ignoreTargetSecurity;
            this.componentSpecified = request.componentSpecified;
            this.outActivity = request.outActivity;
            this.inTask = request.inTask;
            this.reason = request.reason;
            this.profilerInfo = request.profilerInfo;
            this.globalConfig = request.globalConfig;
            this.userId = request.userId;
            this.waitResult = request.waitResult;
            this.mayWait = request.mayWait;
            this.avoidMoveToFront = request.avoidMoveToFront;
            this.allowPendingRemoteAnimationRegistryLookup = request.allowPendingRemoteAnimationRegistryLookup;
            this.filterCallingUid = request.filterCallingUid;
            this.originatingPendingIntent = request.originatingPendingIntent;
            this.allowBackgroundActivityStart = request.allowBackgroundActivityStart;
        }
    }

    ActivityStarter(ActivityStartController controller, ActivityTaskManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
        this.mController = controller;
        this.mService = service;
        this.mRootActivityContainer = service.mRootActivityContainer;
        this.mSupervisor = supervisor;
        this.mInterceptor = interceptor;
        reset(true);
    }

    /* access modifiers changed from: package-private */
    public void set(ActivityStarter starter) {
        this.mStartActivity = starter.mStartActivity;
        this.mIntent = starter.mIntent;
        this.mCallingUid = starter.mCallingUid;
        this.mOptions = starter.mOptions;
        this.mRestrictedBgActivity = starter.mRestrictedBgActivity;
        this.mLaunchTaskBehind = starter.mLaunchTaskBehind;
        this.mLaunchFlags = starter.mLaunchFlags;
        this.mLaunchMode = starter.mLaunchMode;
        this.mLaunchParams.set(starter.mLaunchParams);
        this.mNotTop = starter.mNotTop;
        this.mDoResume = starter.mDoResume;
        this.mStartFlags = starter.mStartFlags;
        this.mSourceRecord = starter.mSourceRecord;
        this.mPreferredDisplayId = starter.mPreferredDisplayId;
        this.mInTask = starter.mInTask;
        this.mAddingToTask = starter.mAddingToTask;
        this.mReuseTask = starter.mReuseTask;
        this.mNewTaskInfo = starter.mNewTaskInfo;
        this.mNewTaskIntent = starter.mNewTaskIntent;
        this.mSourceStack = starter.mSourceStack;
        this.mTargetStack = starter.mTargetStack;
        this.mMovedToFront = starter.mMovedToFront;
        this.mNoAnimation = starter.mNoAnimation;
        this.mKeepCurTransition = starter.mKeepCurTransition;
        this.mAvoidMoveToFront = starter.mAvoidMoveToFront;
        this.mVoiceSession = starter.mVoiceSession;
        this.mVoiceInteractor = starter.mVoiceInteractor;
        this.mIntentDelivered = starter.mIntentDelivered;
        this.mRequest.set(starter.mRequest);
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getStartActivity() {
        return this.mStartActivity;
    }

    /* access modifiers changed from: package-private */
    public boolean relatedToPackage(String packageName) {
        ActivityRecord activityRecord;
        ActivityRecord[] activityRecordArr = this.mLastStartActivityRecord;
        if ((activityRecordArr[0] == null || !packageName.equals(activityRecordArr[0].packageName)) && ((activityRecord = this.mStartActivity) == null || !packageName.equals(activityRecord.packageName))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public int execute() {
        try {
            if (this.mRequest.mayWait) {
                return startActivityMayWait(this.mRequest.caller, this.mRequest.callingUid, this.mRequest.callingPackage, this.mRequest.realCallingPid, this.mRequest.realCallingUid, this.mRequest.intent, this.mRequest.resolvedType, this.mRequest.voiceSession, this.mRequest.voiceInteractor, this.mRequest.resultTo, this.mRequest.resultWho, this.mRequest.requestCode, this.mRequest.startFlags, this.mRequest.profilerInfo, this.mRequest.waitResult, this.mRequest.globalConfig, this.mRequest.activityOptions, this.mRequest.ignoreTargetSecurity, this.mRequest.userId, this.mRequest.inTask, this.mRequest.reason, this.mRequest.allowPendingRemoteAnimationRegistryLookup, this.mRequest.originatingPendingIntent, this.mRequest.allowBackgroundActivityStart);
            }
            int startActivity = startActivity(this.mRequest.caller, this.mRequest.intent, this.mRequest.ephemeralIntent, this.mRequest.resolvedType, this.mRequest.activityInfo, this.mRequest.resolveInfo, this.mRequest.voiceSession, this.mRequest.voiceInteractor, this.mRequest.resultTo, this.mRequest.resultWho, this.mRequest.requestCode, this.mRequest.callingPid, this.mRequest.callingUid, this.mRequest.callingPackage, this.mRequest.realCallingPid, this.mRequest.realCallingUid, this.mRequest.startFlags, this.mRequest.activityOptions, this.mRequest.ignoreTargetSecurity, this.mRequest.componentSpecified, this.mRequest.outActivity, this.mRequest.inTask, this.mRequest.reason, this.mRequest.allowPendingRemoteAnimationRegistryLookup, this.mRequest.originatingPendingIntent, this.mRequest.allowBackgroundActivityStart);
            onExecutionComplete();
            return startActivity;
        } finally {
            onExecutionComplete();
        }
    }

    /* access modifiers changed from: package-private */
    public int startResolvedActivity(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask) {
        try {
            this.mSupervisor.getActivityMetricsLogger().notifyActivityLaunching(r.intent);
            this.mLastStartReason = "startResolvedActivity";
            this.mLastStartActivityTimeMs = System.currentTimeMillis();
            this.mLastStartActivityRecord[0] = r;
            this.mLastStartActivityResult = startActivity(r, sourceRecord, voiceSession, voiceInteractor, startFlags, doResume, options, inTask, this.mLastStartActivityRecord, false);
            this.mSupervisor.getActivityMetricsLogger().notifyActivityLaunched(this.mLastStartActivityResult, this.mLastStartActivityRecord[0]);
            return this.mLastStartActivityResult;
        } finally {
            onExecutionComplete();
        }
    }

    private int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, SafeActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, TaskRecord inTask, String reason, boolean allowPendingRemoteAnimationRegistryLookup, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
        if (!TextUtils.isEmpty(reason)) {
            this.mLastStartReason = reason;
            this.mLastStartActivityTimeMs = System.currentTimeMillis();
            ActivityRecord[] activityRecordArr = this.mLastStartActivityRecord;
            activityRecordArr[0] = null;
            this.mLastStartActivityResult = startActivity(caller, intent, ephemeralIntent, resolvedType, aInfo, rInfo, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, activityRecordArr, inTask, allowPendingRemoteAnimationRegistryLookup, originatingPendingIntent, allowBackgroundActivityStart);
            if (outActivity != null) {
                outActivity[0] = this.mLastStartActivityRecord[0];
            }
            return getExternalResult(this.mLastStartActivityResult);
        }
        throw new IllegalArgumentException("Need to specify a reason.");
    }

    static int getExternalResult(int result) {
        if (result != 102) {
            return result;
        }
        return 0;
    }

    private void onExecutionComplete() {
        this.mController.onExecutionComplete(this);
    }

    /* JADX INFO: Multiple debug info for r1v44 'userId'  int: [D('target' android.content.IIntentSender), D('userId' int)] */
    /* JADX INFO: Multiple debug info for r15v14 'userId'  int: [D('userId' int), D('verificationBundle' android.os.Bundle)] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x02f9  */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0302  */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x03ec A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:184:0x0430  */
    /* JADX WARNING: Removed duplicated region for block: B:185:0x0433  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x043b  */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x0450  */
    /* JADX WARNING: Removed duplicated region for block: B:241:0x05d0 A[SYNTHETIC, Splitter:B:241:0x05d0] */
    /* JADX WARNING: Removed duplicated region for block: B:261:0x0632  */
    /* JADX WARNING: Removed duplicated region for block: B:265:0x065b  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x06af  */
    /* JADX WARNING: Removed duplicated region for block: B:272:0x06d8  */
    /* JADX WARNING: Removed duplicated region for block: B:274:0x06e5  */
    /* JADX WARNING: Removed duplicated region for block: B:281:0x0701  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0881  */
    /* JADX WARNING: Removed duplicated region for block: B:311:0x0884  */
    /* JADX WARNING: Removed duplicated region for block: B:314:0x08ae  */
    /* JADX WARNING: Removed duplicated region for block: B:315:0x08b2  */
    /* JADX WARNING: Removed duplicated region for block: B:328:0x08ee  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0990  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x01e5  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x01ef  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x025f  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x02b9  */
    public int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, SafeActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, TaskRecord inTask, boolean allowPendingRemoteAnimationRegistryLookup, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
        int err;
        int callingPid2;
        int callingUid2;
        WindowProcessController callerApp;
        int err2;
        Bundle verificationBundle;
        int callingUid3;
        int userId;
        boolean z;
        int callingUid4;
        WindowProcessController callerApp2;
        String str;
        int i;
        int callingPid3;
        String str2;
        IBinder iBinder;
        int err3;
        String str3;
        String str4;
        String str5;
        int userId2;
        String str6;
        WindowProcessController callerApp3;
        ActivityRecord resultRecord;
        String tempCls;
        int requestCode2;
        String resultWho2;
        String resultWho3;
        ActivityRecord sourceRecord;
        int launchFlags;
        String resultWho4;
        String callingPackage2;
        ActivityRecord resultRecord2;
        int requestCode3;
        int err4;
        String str7;
        int err5;
        int err6;
        int callingUid5;
        WindowProcessController callerApp4;
        ActivityRecord resultRecord3;
        boolean restrictedBgActivity;
        int callingPid4;
        ActivityInfo activityInfo;
        WindowProcessController callerApp5;
        ActivityOptions checkedOptions;
        ActivityOptions checkedOptions2;
        int callingUid6;
        int callingPid5;
        ResolveInfo rInfo2;
        String resolvedType2;
        ActivityInfo aInfo2;
        int i2;
        int i3;
        String str8;
        Intent intent2;
        boolean abort;
        int userId3;
        Intent checkIntent;
        ActivityInfo aInfo3;
        Intent intent3;
        ActivityOptions checkedOptions3;
        TaskRecord inTask2;
        int callingUid7;
        int callingPid6;
        ResolveInfo rInfo3;
        ActivityInfo aInfo4;
        WindowProcessController callerApp6;
        int requestCode4;
        int callingPid7;
        String resultWho5;
        boolean z2;
        ResolveInfo rInfo4;
        boolean z3;
        int userId4;
        ActivityInfo aInfo5;
        boolean skipCheck;
        String resultWho6;
        int requestCode5;
        ActivityRecord resultRecord4;
        Intent intent4;
        int callingUid8;
        int callingPid8;
        ActivityInfo aInfo6;
        int i4;
        ActivityRecord sourceRecord2;
        String callingPackage3;
        ActivityStarter activityStarter;
        ActivityRecord r;
        boolean z4;
        ActivityStarter activityStarter2;
        int callingPid9;
        WindowProcessController tmpCallerApp;
        ActivityInfo aInfo7;
        int callingPid10;
        ResolveInfo rInfo5;
        ActivityInfo info;
        boolean abort2;
        int callingUid9;
        long j;
        Throwable th;
        RemoteException e;
        int err7;
        int err8;
        RemoteException e2;
        ActivityRecord resultRecord5;
        int err9;
        this.mSupervisor.getActivityMetricsLogger().notifyActivityLaunching(intent);
        if (this.mHyp == null) {
            this.mHyp = Hypnus.getHypnus();
        }
        Hypnus hypnus = this.mHyp;
        if (hypnus != null) {
            hypnus.hypnusSetAction(11, (int) SystemService.PHASE_THIRD_PARTY_APPS_CAN_START);
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "startActivityLocked callingPid " + callingPid);
        }
        if (OppoAmsUtils.getInstance(this.mService).needToControlActivityStartFreq(intent)) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                StringBuilder sb = new StringBuilder();
                sb.append("control start frequency {");
                err = 0;
                sb.append(intent.toShortString(true, true, true, false));
                sb.append("}");
                Slog.i(TAG, sb.toString());
            } else {
                err = 0;
            }
            if (SystemProperties.getBoolean(AlertWindowNotification.PROPERTY_PERMISSION_ENABLE, true)) {
                err = -80;
            }
        } else {
            err = 0;
        }
        Bundle verificationBundle2 = options != null ? options.popAppVerificationBundle() : null;
        WindowProcessController callerApp7 = null;
        if (caller != null) {
            callerApp7 = this.mService.getProcessController(caller);
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                Slog.v(TAG, "startActivityLocked callerApp " + callerApp7);
            }
            if (callerApp7 != null) {
                int callingPid11 = callerApp7.getPid();
                callingUid2 = callerApp7.mInfo.uid;
                callingPid2 = callingPid11;
            } else {
                Slog.w(TAG, "Unable to find app for caller " + caller + " (pid=" + callingPid + ") when starting: " + intent.toString());
                err = -94;
                callingUid2 = callingUid;
                callingPid2 = callingPid;
            }
        } else {
            callingUid2 = callingUid;
            callingPid2 = callingPid;
        }
        int userId5 = (aInfo == null || aInfo.applicationInfo == null) ? 0 : UserHandle.getUserId(aInfo.applicationInfo.uid);
        if (OppoFeatureCache.get(IColorAthenaManager.DEFAULT).startActivityFilter(intent, callingPackage, callingUid2, callingPid2)) {
            err = -94;
        }
        if (OppoFeatureCache.get(IColorAppPhoneManager.DEFAULT).handleAppPhoneComing(aInfo)) {
            SafeActivityOptions.abort(options);
            return 102;
        } else if (OppoFeatureCache.get(IColorAppChildrenSpaceManager.DEFAULT).handleChildrenSpaceAppLaunch(aInfo)) {
            SafeActivityOptions.abort(options);
            return 102;
        } else {
            if (OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).validStartActivity(aInfo)) {
                StringBuilder sb2 = new StringBuilder();
                callerApp = callerApp7;
                sb2.append("UL for activity ");
                sb2.append(aInfo);
                sb2.append(" : is R");
                Slog.i(IColorAbnormalAppManager.TAG, sb2.toString());
                err2 = 100;
            } else {
                callerApp = callerApp7;
                err2 = err;
            }
            if (this.mColorStarterEx != null) {
                verificationBundle = verificationBundle2;
                userId = userId5;
                err2 = this.mColorStarterEx.isProhibitInstallation(err2, intent, userId);
                callingUid3 = callingUid2;
                z = true;
                this.mColorStarterEx.notifyToRemovePkgFromNotLaunchedList(aInfo, true);
            } else {
                callingUid3 = callingUid2;
                verificationBundle = verificationBundle2;
                z = true;
                userId = userId5;
            }
            if (err2 != 100) {
                err9 = err2;
                String str9 = this.mLastStartReason;
                callerApp2 = callerApp;
                i = 100;
                callingUid4 = callingUid3;
                callingPid3 = callingPid2;
                str = TAG;
                str2 = callingPackage;
                iBinder = resultTo;
                if (OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).shouldPreventStartActivity(intent, ephemeralIntent, aInfo, callingPackage, callingUid4, callingPid3, str9, userId)) {
                    err3 = 100;
                    if (OppoEngineerFunctionManager.shouldPreventStartActivity(aInfo, str2, callingUid4, callingPid3)) {
                        err3 = 100;
                    }
                    if (err3 != 0) {
                        Slog.i(str, "START u" + userId + " {" + intent.toShortString(true, true, true, false) + "} from uid " + callingUid4 + " and from pid " + callingPid3);
                        userId2 = userId;
                        str6 = str;
                        str5 = "START u";
                        str4 = " {";
                        str3 = "} from uid ";
                        this.mSupervisor.dataCollectionInfo(caller, callerApp2, callingPid3, callingUid4, callingPackage, intent);
                        if (this.mColorStarterEx != null) {
                            callerApp3 = callerApp2;
                            this.mColorStarterEx.packageInstallInfoCollectForExp(callerApp3, str2, intent);
                        } else {
                            callerApp3 = callerApp2;
                        }
                        if (callerApp3 != null && SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US")) {
                            this.mSupervisor.dataCollectionInfoExp(callerApp3.mInfo.packageName, str2, intent);
                        }
                    } else {
                        str5 = "START u";
                        str4 = " {";
                        str3 = "} from uid ";
                        userId2 = userId;
                        str6 = str;
                        callerApp3 = callerApp2;
                    }
                    ActivityRecord sourceRecord3 = null;
                    resultRecord = null;
                    if (intent != null || intent.getComponent() == null || intent.getComponent().getClassName() == null) {
                        tempCls = null;
                    } else {
                        tempCls = intent.getComponent().getClassName().trim();
                    }
                    if (tempCls == null && tempCls.equals("com.android.internal.app.ChooserActivity") && callingUid4 == 1000) {
                        sourceRecord3 = this.mSupervisor.mOppoSecureProtectUtils.getValue();
                        if (sourceRecord3 != null) {
                            resultRecord = sourceRecord3.resultTo;
                            resultWho2 = sourceRecord3.resultWho;
                            requestCode2 = sourceRecord3.requestCode;
                        } else {
                            resultWho2 = resultWho;
                            requestCode2 = requestCode;
                        }
                    } else {
                        resultWho2 = resultWho;
                        requestCode2 = requestCode;
                    }
                    if (iBinder == null) {
                        ActivityRecord sourceRecord4 = this.mRootActivityContainer.isInAnyStack(iBinder);
                        if (ActivityTaskManagerDebugConfig.DEBUG_RESULTS) {
                            String str10 = TAG_RESULTS;
                            resultRecord5 = resultRecord;
                            StringBuilder sb3 = new StringBuilder();
                            resultWho3 = resultWho2;
                            sb3.append("Will send result to ");
                            sb3.append(iBinder);
                            sb3.append(StringUtils.SPACE);
                            sb3.append(sourceRecord4);
                            Slog.v(str10, sb3.toString());
                        } else {
                            resultRecord5 = resultRecord;
                            resultWho3 = resultWho2;
                        }
                        if (sourceRecord4 == null || requestCode2 < 0 || sourceRecord4.finishing) {
                            resultRecord = resultRecord5;
                            sourceRecord = sourceRecord4;
                        } else {
                            resultRecord = sourceRecord4;
                            sourceRecord = sourceRecord4;
                        }
                    } else {
                        resultWho3 = resultWho2;
                        sourceRecord = sourceRecord3;
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                        Slog.v(str6, "startActivityLocked resultTo " + iBinder + " sourceRecord " + sourceRecord);
                    }
                    launchFlags = intent.getFlags();
                    if ((launchFlags & DumpState.DUMP_APEX) != 0 || sourceRecord == null) {
                        resultRecord2 = resultRecord;
                        requestCode3 = requestCode2;
                        callingPackage2 = str2;
                        resultWho4 = resultWho3;
                    } else if (requestCode2 >= 0) {
                        SafeActivityOptions.abort(options);
                        return -93;
                    } else {
                        ActivityRecord resultRecord6 = sourceRecord.resultTo;
                        if (resultRecord6 != null && !resultRecord6.isInStackLocked()) {
                            resultRecord6 = null;
                        }
                        String resultWho7 = sourceRecord.resultWho;
                        int requestCode6 = sourceRecord.requestCode;
                        sourceRecord.resultTo = null;
                        if (resultRecord6 != null) {
                            resultRecord6.removeResultsLocked(sourceRecord, resultWho7, requestCode6);
                        }
                        if (sourceRecord.launchedFromUid == callingUid4) {
                            callingPackage2 = sourceRecord.launchedFromPackage;
                            resultWho4 = resultWho7;
                            requestCode3 = requestCode6;
                            resultRecord2 = resultRecord6;
                        } else {
                            callingPackage2 = str2;
                            resultWho4 = resultWho7;
                            requestCode3 = requestCode6;
                            resultRecord2 = resultRecord6;
                        }
                    }
                    if (err3 == 0 && intent.getComponent() == null) {
                        err3 = -91;
                    }
                    if (err3 == 0 || aInfo != null) {
                        err4 = err3;
                    } else {
                        err4 = -92;
                    }
                    if (err4 == 0 || sourceRecord == null) {
                        str7 = resolvedType;
                        err8 = err4;
                    } else if (sourceRecord.getTaskRecord() == null || sourceRecord.getTaskRecord().voiceSession == null) {
                        str7 = resolvedType;
                        err8 = err4;
                    } else if ((launchFlags & 268435456) == 0) {
                        err8 = err4;
                        if (sourceRecord.info.applicationInfo.uid != aInfo.applicationInfo.uid) {
                            try {
                                intent.addCategory("android.intent.category.VOICE");
                                str7 = resolvedType;
                                try {
                                    if (!this.mService.getPackageManager().activitySupportsIntent(intent.getComponent(), intent, str7)) {
                                        Slog.w(str6, "Activity being started in current voice task does not support voice: " + intent);
                                        err5 = -97;
                                    } else {
                                        err5 = err8;
                                    }
                                } catch (RemoteException e3) {
                                    e2 = e3;
                                    Slog.w(str6, "Failure checking voice capabilities", e2);
                                    err5 = -97;
                                    if (err5 == 0) {
                                    }
                                    err6 = err5;
                                    if (resultRecord2 == null) {
                                    }
                                    if (err6 != 0) {
                                    }
                                }
                            } catch (RemoteException e4) {
                                e2 = e4;
                                str7 = resolvedType;
                                Slog.w(str6, "Failure checking voice capabilities", e2);
                                err5 = -97;
                                if (err5 == 0) {
                                }
                                err6 = err5;
                                if (resultRecord2 == null) {
                                }
                                if (err6 != 0) {
                                }
                            }
                            if (err5 == 0 || voiceSession == null) {
                                err6 = err5;
                            } else {
                                try {
                                    try {
                                        if (!this.mService.getPackageManager().activitySupportsIntent(intent.getComponent(), intent, str7)) {
                                            Slog.w(str6, "Activity being started in new voice task does not support: " + intent);
                                            err7 = -97;
                                        } else {
                                            err7 = err5;
                                        }
                                        err6 = err7;
                                    } catch (RemoteException e5) {
                                        e = e5;
                                        Slog.w(str6, "Failure checking voice capabilities", e);
                                        err6 = -97;
                                        if (resultRecord2 == null) {
                                        }
                                        if (err6 != 0) {
                                        }
                                    }
                                } catch (RemoteException e6) {
                                    e = e6;
                                    Slog.w(str6, "Failure checking voice capabilities", e);
                                    err6 = -97;
                                    if (resultRecord2 == null) {
                                    }
                                    if (err6 != 0) {
                                    }
                                }
                            }
                            ActivityStack resultStack = resultRecord2 == null ? null : resultRecord2.getActivityStack();
                            if (err6 != 0) {
                                if (resultRecord2 != null) {
                                    resultStack.sendActivityResultLocked(-1, resultRecord2, resultWho4, requestCode3, 0, null);
                                }
                                SafeActivityOptions.abort(options);
                                return err6;
                            }
                            boolean abort3 = (!this.mSupervisor.checkStartAnyActivityPermission(intent, aInfo, resultWho4, requestCode3, callingPid3, callingUid4, callingPackage2, ignoreTargetSecurity, inTask != null, callerApp3, resultRecord2, resultStack)) | (!this.mService.mIntentFirewall.checkStartActivity(intent, callingUid4, callingPid3, resolvedType, aInfo.applicationInfo)) | (!this.mService.getPermissionPolicyInternal().checkStartActivity(intent, callingUid4, callingPackage2));
                            if (!abort3) {
                                try {
                                    Trace.traceBegin(64, "shouldAbortBackgroundActivityStart");
                                    j = 64;
                                    callerApp4 = callerApp3;
                                    callingUid5 = callingUid4;
                                    callingPid4 = callingPid3;
                                    resultRecord3 = resultRecord2;
                                    activityInfo = aInfo;
                                    try {
                                        boolean restrictedBgActivity2 = shouldAbortBackgroundActivityStart(callingUid4, callingPid3, callingPackage2, realCallingUid, realCallingPid, callerApp3, originatingPendingIntent, allowBackgroundActivityStart, intent);
                                        Trace.traceEnd(64);
                                        restrictedBgActivity = restrictedBgActivity2;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        Trace.traceEnd(j);
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    j = 64;
                                    Trace.traceEnd(j);
                                    throw th;
                                }
                            } else {
                                callerApp4 = callerApp3;
                                callingUid5 = callingUid4;
                                callingPid4 = callingPid3;
                                resultRecord3 = resultRecord2;
                                activityInfo = aInfo;
                                restrictedBgActivity = false;
                            }
                            if (options != null) {
                                callerApp5 = callerApp4;
                                checkedOptions = options.getOptions(intent, activityInfo, callerApp5, this.mSupervisor);
                            } else {
                                callerApp5 = callerApp4;
                                checkedOptions = null;
                            }
                            if (allowPendingRemoteAnimationRegistryLookup) {
                                checkedOptions2 = this.mService.getActivityStartController().getPendingRemoteAnimationRegistry().overrideOptionsIfNeeded(callingPackage2, checkedOptions);
                            } else {
                                checkedOptions2 = checkedOptions;
                            }
                            if (!OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).startActivity(aInfo, intent, sourceRecord == null ? null : sourceRecord.mColorArEx, resultRecord3 == null ? null : resultRecord3.mColorArEx, callingPackage2, callingUid5)) {
                                Intent newIntent = OppoFeatureCache.get(IColorIntentInterceptManager.DEFAULT).interceptGPIfNeeded(resolvedType, callingUid5, userId2, callingPackage2, resultRecord3, intent, aInfo);
                                if (newIntent != null) {
                                    i2 = realCallingUid;
                                    callingUid9 = callingUid5;
                                    ResolveInfo tmpRInfo = this.mSupervisor.resolveIntent(newIntent, resolvedType, userId2, 0, computeResolveFilterUid(callingUid5, i2, this.mRequest.filterCallingUid));
                                    i3 = startFlags;
                                    ActivityInfo tmpAInfo = this.mSupervisor.resolveActivity(newIntent, tmpRInfo, i3, null);
                                    if (tmpAInfo != null) {
                                        callingUid6 = realCallingUid;
                                        callingPid5 = realCallingPid;
                                        intent2 = newIntent;
                                        rInfo2 = tmpRInfo;
                                        aInfo2 = tmpAInfo;
                                        str8 = str6;
                                        resolvedType2 = null;
                                        if (this.mService.mController == null) {
                                            try {
                                                userId3 = userId2;
                                                try {
                                                    OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).setOppoUserId(aInfo2, intent2, userId3);
                                                    if (options != null) {
                                                        intent2.setLaunchStackId(options.getLaunchStackId());
                                                    }
                                                    if (this.mService.mOppoActivityControlerScheduler != null) {
                                                        abort2 = abort3 | (!this.mService.mOppoActivityControlerScheduler.scheduleActivityStarting(intent2, aInfo2.applicationInfo.packageName));
                                                    } else if (OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).handleVideoComingNotification(intent2, aInfo2)) {
                                                        abort2 = abort3 | true;
                                                    } else {
                                                        abort2 = abort3 | (!this.mService.mController.activityStarting(intent2, aInfo2.applicationInfo.packageName));
                                                    }
                                                    abort = abort2;
                                                } catch (RemoteException e7) {
                                                    this.mService.mController = null;
                                                    abort = abort3;
                                                    checkIntent = OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).checkStartActivityForAppLock(this.mSupervisor, sourceRecord, aInfo2, intent2, requestCode3, realCallingUid, checkedOptions2);
                                                    if (checkIntent != null) {
                                                    }
                                                    intent3 = intent2;
                                                    aInfo3 = aInfo2;
                                                    this.mInterceptor.setStates(userId3, realCallingPid, realCallingUid, startFlags, callingPackage2);
                                                    if (!this.mInterceptor.intercept(intent3, rInfo2, aInfo3, resolvedType2, inTask, callingPid5, callingUid6, checkedOptions2)) {
                                                    }
                                                    if (!abort) {
                                                    }
                                                }
                                            } catch (RemoteException e8) {
                                                userId3 = userId2;
                                                this.mService.mController = null;
                                                abort = abort3;
                                                checkIntent = OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).checkStartActivityForAppLock(this.mSupervisor, sourceRecord, aInfo2, intent2, requestCode3, realCallingUid, checkedOptions2);
                                                if (checkIntent != null) {
                                                }
                                                intent3 = intent2;
                                                aInfo3 = aInfo2;
                                                this.mInterceptor.setStates(userId3, realCallingPid, realCallingUid, startFlags, callingPackage2);
                                                if (!this.mInterceptor.intercept(intent3, rInfo2, aInfo3, resolvedType2, inTask, callingPid5, callingUid6, checkedOptions2)) {
                                                }
                                                if (!abort) {
                                                }
                                            }
                                        } else {
                                            userId3 = userId2;
                                            abort = abort3;
                                        }
                                        checkIntent = OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).checkStartActivityForAppLock(this.mSupervisor, sourceRecord, aInfo2, intent2, requestCode3, realCallingUid, checkedOptions2);
                                        if (checkIntent != null || (info = this.mSupervisor.resolveActivity(checkIntent, null, 0, this.mRequest.profilerInfo, this.mService.mAmInternal.getCurrentUserId(), Binder.getCallingUid())) == null) {
                                            intent3 = intent2;
                                            aInfo3 = aInfo2;
                                        } else {
                                            intent3 = checkIntent;
                                            aInfo3 = info;
                                        }
                                        this.mInterceptor.setStates(userId3, realCallingPid, realCallingUid, startFlags, callingPackage2);
                                        if (!this.mInterceptor.intercept(intent3, rInfo2, aInfo3, resolvedType2, inTask, callingPid5, callingUid6, checkedOptions2)) {
                                            intent3 = this.mInterceptor.mIntent;
                                            ResolveInfo rInfo6 = this.mInterceptor.mRInfo;
                                            ActivityInfo aInfo8 = this.mInterceptor.mAInfo;
                                            String resolvedType3 = this.mInterceptor.mResolvedType;
                                            TaskRecord inTask3 = this.mInterceptor.mInTask;
                                            callingPid6 = this.mInterceptor.mCallingPid;
                                            callingUid7 = this.mInterceptor.mCallingUid;
                                            resolvedType2 = resolvedType3;
                                            inTask2 = inTask3;
                                            checkedOptions3 = this.mInterceptor.mActivityOptions;
                                            rInfo3 = rInfo6;
                                            aInfo4 = aInfo8;
                                        } else {
                                            inTask2 = inTask;
                                            aInfo4 = aInfo3;
                                            checkedOptions3 = checkedOptions2;
                                            rInfo3 = rInfo2;
                                            callingPid6 = callingPid5;
                                            callingUid7 = callingUid6;
                                        }
                                        if (!abort) {
                                            if (resultRecord3 != null) {
                                                if (this.mSupervisor.mOppoSecureProtectUtils.isNeedReplaceActivityRequest(resultRecord3, resultWho4, requestCode3)) {
                                                    this.mSupervisor.mOppoSecureProtectUtils.setTempValue(resultRecord3, resultWho4, requestCode3);
                                                }
                                            }
                                            ActivityOptions.abort(checkedOptions3);
                                            return 102;
                                        }
                                        if (aInfo4 != null) {
                                            resultWho5 = resultWho4;
                                            if (this.mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(aInfo4.packageName, userId3)) {
                                                IIntentSender target = this.mService.getIntentSenderLocked(2, callingPackage2, callingUid7, userId3, null, null, 0, new Intent[]{intent3}, new String[]{resolvedType2}, 1342177280, null);
                                                Intent newIntent1 = new Intent("android.intent.action.REVIEW_PERMISSIONS");
                                                int flags = intent3.getFlags() | DumpState.DUMP_VOLUMES;
                                                if ((268959744 & flags) != 0) {
                                                    flags |= 134217728;
                                                }
                                                newIntent1.setFlags(flags);
                                                newIntent1.putExtra("android.intent.extra.PACKAGE_NAME", aInfo4.packageName);
                                                newIntent1.putExtra("android.intent.extra.INTENT", new IntentSender(target));
                                                if (resultRecord3 != null) {
                                                    newIntent1.putExtra("android.intent.extra.RESULT_NEEDED", true);
                                                }
                                                callingUid7 = realCallingUid;
                                                callingPid7 = realCallingPid;
                                                requestCode4 = requestCode3;
                                                callerApp6 = callerApp5;
                                                userId4 = userId3;
                                                ResolveInfo rInfo7 = this.mSupervisor.resolveIntent(newIntent1, null, userId3, 0, computeResolveFilterUid(callingUid7, i2, this.mRequest.filterCallingUid));
                                                ActivityInfo aInfo9 = this.mSupervisor.resolveActivity(newIntent1, rInfo7, i3, null);
                                                if (ActivityTaskManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                                                    ActivityStack focusedStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
                                                    StringBuilder sb4 = new StringBuilder();
                                                    sb4.append(str5);
                                                    sb4.append(userId4);
                                                    sb4.append(str4);
                                                    z3 = true;
                                                    z2 = false;
                                                    sb4.append(newIntent1.toShortString(true, true, true, false));
                                                    sb4.append(str3);
                                                    sb4.append(callingUid7);
                                                    sb4.append(" on display ");
                                                    sb4.append(focusedStack == null ? 0 : focusedStack.mDisplayId);
                                                    Slog.i(str8, sb4.toString());
                                                } else {
                                                    z3 = true;
                                                    z2 = false;
                                                }
                                                rInfo4 = rInfo7;
                                                resolvedType2 = null;
                                                intent3 = newIntent1;
                                                aInfo5 = aInfo9;
                                                if (rInfo4 != null || rInfo4.auxiliaryInfo == null) {
                                                    skipCheck = z3;
                                                    i4 = i2;
                                                    callingPackage3 = callingPackage2;
                                                    activityStarter = this;
                                                    resultWho6 = resultWho5;
                                                    requestCode5 = requestCode4;
                                                    sourceRecord2 = sourceRecord;
                                                    resultRecord4 = resultRecord3;
                                                    aInfo6 = aInfo5;
                                                    callingUid8 = callingUid7;
                                                    intent4 = intent3;
                                                    callingPid8 = callingPid7;
                                                } else {
                                                    i4 = i2;
                                                    callingPackage3 = callingPackage2;
                                                    requestCode5 = requestCode4;
                                                    skipCheck = z3;
                                                    resultWho6 = resultWho5;
                                                    sourceRecord2 = sourceRecord;
                                                    activityStarter = this;
                                                    resultRecord4 = resultRecord3;
                                                    Intent intent5 = createLaunchIntent(rInfo4.auxiliaryInfo, ephemeralIntent, callingPackage3, verificationBundle, resolvedType2, userId4);
                                                    callingPid8 = realCallingPid;
                                                    callingUid8 = realCallingUid;
                                                    intent4 = intent5;
                                                    resolvedType2 = null;
                                                    aInfo6 = activityStarter.mSupervisor.resolveActivity(intent5, rInfo4, i3, null);
                                                }
                                                OppoSecureProtectUtils oppoSecureProtectUtils = activityStarter.mSupervisor.mOppoSecureProtectUtils;
                                                ActivityTaskManagerService activityTaskManagerService = activityStarter.mService;
                                                r = oppoSecureProtectUtils.handleStartActivityLocked(activityTaskManagerService, callerApp6, callingPid8, callingUid8, callingPackage3, intent4, resolvedType2, aInfo6, activityTaskManagerService.getGlobalConfiguration(), resultRecord4, resultWho6, requestCode5, componentSpecified, voiceSession == null ? skipCheck : false, activityStarter.mSupervisor, checkedOptions3, sourceRecord2);
                                                if (outActivity == null) {
                                                    outActivity[0] = r;
                                                }
                                                if (r.appTimeTracker == null && sourceRecord2 != null) {
                                                    r.appTimeTracker = sourceRecord2.appTimeTracker;
                                                }
                                                if (aInfo6 == null && aInfo6.applicationInfo != null && !OppoFeatureCache.get(IColorHansManager.DEFAULT).hansActivityIfNeeded(callingUid8, callingPackage3, aInfo6.applicationInfo.uid, aInfo6.packageName, aInfo6.name)) {
                                                    return i;
                                                }
                                                ActivityStack stack = activityStarter.mRootActivityContainer.getTopDisplayFocusedStack();
                                                if (voiceSession == null) {
                                                    z4 = false;
                                                    callingPid9 = userId4;
                                                    activityStarter2 = activityStarter;
                                                } else if (stack.getResumedActivity() == null || stack.getResumedActivity().info.applicationInfo.uid != i4) {
                                                    if (callerApp6 == null) {
                                                        tmpCallerApp = activityStarter.mService.getProcessController(realCallingPid, i4);
                                                    } else {
                                                        tmpCallerApp = callerApp6;
                                                    }
                                                    if (tmpCallerApp == null || !tmpCallerApp.hasOverlayUi() || !ActivityThread.inCptWhiteList((int) CompatibilityHelper.FLOATING_WIN_START, callingPackage3)) {
                                                        skipCheck = false;
                                                    }
                                                    if (skipCheck || OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).isFromControlCenterPkg(callingPackage3)) {
                                                        z4 = false;
                                                        callingPid9 = userId4;
                                                        activityStarter2 = activityStarter;
                                                    } else {
                                                        callingPid9 = userId4;
                                                        z4 = false;
                                                        activityStarter2 = activityStarter;
                                                        if (!activityStarter.mService.checkAppSwitchAllowedLocked(callingPid8, callingUid8, realCallingPid, realCallingUid, "Activity start")) {
                                                            if (!restrictedBgActivity || !activityStarter2.handleBackgroundActivityAbort(r)) {
                                                                activityStarter2.mController.addPendingActivityLaunch(new ActivityStackSupervisor.PendingActivityLaunch(r, sourceRecord2, startFlags, stack, callerApp6));
                                                            }
                                                            ActivityOptions.abort(checkedOptions3);
                                                            return i;
                                                        }
                                                    }
                                                } else {
                                                    z4 = false;
                                                    callingPid9 = userId4;
                                                    activityStarter2 = activityStarter;
                                                }
                                                activityStarter2.mService.onStartActivitySetDidAppSwitch();
                                                activityStarter2.mController.doPendingActivityLaunches(z4);
                                                if (!(aInfo6 == null || aInfo6.packageName == null)) {
                                                    activityStarter2.mService.getOppoPackageManagerInternalLocked().autoUnfreezePackage(aInfo6.packageName, callingPid9, "app before start from AMS");
                                                }
                                                int res = startActivity(r, sourceRecord2, voiceSession, voiceInteractor, startFlags, true, checkedOptions3, inTask2, outActivity, restrictedBgActivity);
                                                ActivityMetricsLogger activityMetricsLogger = activityStarter2.mSupervisor.getActivityMetricsLogger();
                                                char c = z4 ? 1 : 0;
                                                char c2 = z4 ? 1 : 0;
                                                char c3 = z4 ? 1 : 0;
                                                char c4 = z4 ? 1 : 0;
                                                char c5 = z4 ? 1 : 0;
                                                activityMetricsLogger.notifyActivityLaunched(res, outActivity[c]);
                                                return res;
                                            }
                                            callerApp6 = callerApp5;
                                            aInfo7 = aInfo4;
                                            rInfo5 = rInfo3;
                                            callingPid10 = callingPid6;
                                            requestCode4 = requestCode3;
                                            userId4 = userId3;
                                            z3 = true;
                                            z2 = false;
                                        } else {
                                            callerApp6 = callerApp5;
                                            aInfo7 = aInfo4;
                                            rInfo5 = rInfo3;
                                            callingPid10 = callingPid6;
                                            requestCode4 = requestCode3;
                                            resultWho5 = resultWho4;
                                            userId4 = userId3;
                                            z3 = true;
                                            z2 = false;
                                        }
                                        rInfo4 = rInfo5;
                                        callingPid7 = callingPid10;
                                        aInfo5 = aInfo7;
                                        if (rInfo4 != null) {
                                        }
                                        skipCheck = z3;
                                        i4 = i2;
                                        callingPackage3 = callingPackage2;
                                        activityStarter = this;
                                        resultWho6 = resultWho5;
                                        requestCode5 = requestCode4;
                                        sourceRecord2 = sourceRecord;
                                        resultRecord4 = resultRecord3;
                                        aInfo6 = aInfo5;
                                        callingUid8 = callingUid7;
                                        intent4 = intent3;
                                        callingPid8 = callingPid7;
                                        OppoSecureProtectUtils oppoSecureProtectUtils2 = activityStarter.mSupervisor.mOppoSecureProtectUtils;
                                        ActivityTaskManagerService activityTaskManagerService2 = activityStarter.mService;
                                        r = oppoSecureProtectUtils2.handleStartActivityLocked(activityTaskManagerService2, callerApp6, callingPid8, callingUid8, callingPackage3, intent4, resolvedType2, aInfo6, activityTaskManagerService2.getGlobalConfiguration(), resultRecord4, resultWho6, requestCode5, componentSpecified, voiceSession == null ? skipCheck : false, activityStarter.mSupervisor, checkedOptions3, sourceRecord2);
                                        if (outActivity == null) {
                                        }
                                        r.appTimeTracker = sourceRecord2.appTimeTracker;
                                        if (aInfo6 == null) {
                                        }
                                        ActivityStack stack2 = activityStarter.mRootActivityContainer.getTopDisplayFocusedStack();
                                        if (voiceSession == null) {
                                        }
                                        activityStarter2.mService.onStartActivitySetDidAppSwitch();
                                        activityStarter2.mController.doPendingActivityLaunches(z4);
                                        activityStarter2.mService.getOppoPackageManagerInternalLocked().autoUnfreezePackage(aInfo6.packageName, callingPid9, "app before start from AMS");
                                        int res2 = startActivity(r, sourceRecord2, voiceSession, voiceInteractor, startFlags, true, checkedOptions3, inTask2, outActivity, restrictedBgActivity);
                                        ActivityMetricsLogger activityMetricsLogger2 = activityStarter2.mSupervisor.getActivityMetricsLogger();
                                        char c6 = z4 ? 1 : 0;
                                        char c22 = z4 ? 1 : 0;
                                        char c32 = z4 ? 1 : 0;
                                        char c42 = z4 ? 1 : 0;
                                        char c52 = z4 ? 1 : 0;
                                        activityMetricsLogger2.notifyActivityLaunched(res2, outActivity[c6]);
                                        return res2;
                                    }
                                    str8 = str6;
                                    Slog.e(str8, "CII_AMS can NOT parse new intent");
                                } else {
                                    callingUid9 = callingUid5;
                                    i2 = realCallingUid;
                                    i3 = startFlags;
                                    str8 = str6;
                                }
                                resolvedType2 = resolvedType;
                                rInfo2 = rInfo;
                                intent2 = intent;
                                callingUid6 = callingUid9;
                                callingPid5 = callingPid4;
                                aInfo2 = aInfo;
                                if (this.mService.mController == null) {
                                }
                                abort = abort3;
                                checkIntent = OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).checkStartActivityForAppLock(this.mSupervisor, sourceRecord, aInfo2, intent2, requestCode3, realCallingUid, checkedOptions2);
                                if (checkIntent != null) {
                                }
                                intent3 = intent2;
                                aInfo3 = aInfo2;
                                this.mInterceptor.setStates(userId3, realCallingPid, realCallingUid, startFlags, callingPackage2);
                                if (!this.mInterceptor.intercept(intent3, rInfo2, aInfo3, resolvedType2, inTask, callingPid5, callingUid6, checkedOptions2)) {
                                }
                                if (!abort) {
                                }
                            } else if (resultRecord3 == null) {
                                return 0;
                            } else {
                                this.mSupervisor.mOppoSecureProtectUtils.setTempValue(resultRecord3, resultWho4, requestCode3);
                                return 0;
                            }
                        } else {
                            str7 = resolvedType;
                        }
                    } else {
                        str7 = resolvedType;
                        err8 = err4;
                    }
                    err5 = err8;
                    if (err5 == 0) {
                    }
                    err6 = err5;
                    if (resultRecord2 == null) {
                    }
                    if (err6 != 0) {
                    }
                }
            } else {
                callerApp2 = callerApp;
                err9 = err2;
                i = 100;
                callingPid3 = callingPid2;
                str = TAG;
                str2 = callingPackage;
                iBinder = resultTo;
                callingUid4 = callingUid3;
            }
            err3 = err9;
            if (OppoEngineerFunctionManager.shouldPreventStartActivity(aInfo, str2, callingUid4, callingPid3)) {
            }
            if (err3 != 0) {
            }
            ActivityRecord sourceRecord32 = null;
            resultRecord = null;
            if (intent != null) {
            }
            tempCls = null;
            if (tempCls == null) {
            }
            resultWho2 = resultWho;
            requestCode2 = requestCode;
            if (iBinder == null) {
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
            }
            launchFlags = intent.getFlags();
            if ((launchFlags & DumpState.DUMP_APEX) != 0) {
            }
            resultRecord2 = resultRecord;
            requestCode3 = requestCode2;
            callingPackage2 = str2;
            resultWho4 = resultWho3;
            err3 = -91;
            if (err3 == 0) {
            }
            err4 = err3;
            if (err4 == 0) {
            }
            str7 = resolvedType;
            err8 = err4;
            err5 = err8;
            if (err5 == 0) {
            }
            err6 = err5;
            if (resultRecord2 == null) {
            }
            if (err6 != 0) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shouldAbortBackgroundActivityStart(int callingUid, int callingPid, String callingPackage, int realCallingUid, int realCallingPid, WindowProcessController callerApp, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart, Intent intent) {
        int realCallingUidProcState;
        boolean realCallingUidHasAnyVisibleWindow;
        boolean isRealCallingUidForeground;
        boolean isRealCallingUidPersistentSystemProcess;
        boolean z;
        WindowProcessController callerApp2;
        int callerAppUid;
        int callingUserId;
        boolean z2;
        if (!OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).shouldPreventStartActivity(intent, null, null, callingPackage, callingUid, callingPid, "check_white", 0)) {
            return false;
        }
        int callingAppId = UserHandle.getAppId(callingUid);
        if (callingUid == 0 || callingAppId == 1000) {
            return false;
        }
        if (callingAppId == 1027) {
            return false;
        }
        int callingUidProcState = this.mService.getUidState(callingUid);
        boolean callingUidHasAnyVisibleWindow = this.mService.mWindowManager.mRoot.isAnyNonToastWindowVisibleForUid(callingUid);
        boolean isCallingUidForeground = callingUidHasAnyVisibleWindow || callingUidProcState == 2 || callingUidProcState == 4;
        boolean isCallingUidPersistentSystemProcess = callingUidProcState <= 1;
        if (callingUidHasAnyVisibleWindow) {
            return false;
        }
        if (isCallingUidPersistentSystemProcess) {
            return false;
        }
        if (callingUid == realCallingUid) {
            realCallingUidProcState = callingUidProcState;
        } else {
            realCallingUidProcState = this.mService.getUidState(realCallingUid);
        }
        if (callingUid == realCallingUid) {
            realCallingUidHasAnyVisibleWindow = callingUidHasAnyVisibleWindow;
        } else {
            realCallingUidHasAnyVisibleWindow = this.mService.mWindowManager.mRoot.isAnyNonToastWindowVisibleForUid(realCallingUid);
        }
        if (callingUid == realCallingUid) {
            isRealCallingUidForeground = isCallingUidForeground;
        } else {
            isRealCallingUidForeground = realCallingUidHasAnyVisibleWindow || realCallingUidProcState == 2;
        }
        int realCallingAppId = UserHandle.getAppId(realCallingUid);
        if (callingUid == realCallingUid) {
            isRealCallingUidPersistentSystemProcess = isCallingUidPersistentSystemProcess;
        } else {
            isRealCallingUidPersistentSystemProcess = realCallingAppId == 1000 || realCallingUidProcState <= 1;
        }
        if (realCallingUid == callingUid) {
            z = false;
        } else if (realCallingUidHasAnyVisibleWindow) {
            OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).monitorActivityStartInfo("Q_realCallingUidHasAnyVisibleWindow");
            return false;
        } else if (isRealCallingUidPersistentSystemProcess && allowBackgroundActivityStart) {
            return false;
        } else {
            if (this.mService.isAssociatedCompanionApp(UserHandle.getUserId(realCallingUid), realCallingUid)) {
                OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).monitorActivityStartInfo("Q_realCallingUidIsCompanionApp");
                return false;
            }
            z = false;
        }
        ActivityTaskManagerService activityTaskManagerService = this.mService;
        if (ActivityTaskManagerService.checkPermission("android.permission.START_ACTIVITIES_FROM_BACKGROUND", callingPid, callingUid) == 0 || this.mSupervisor.mRecentTasks.isCallerRecents(callingUid) || this.mService.isDeviceOwner(callingUid)) {
            return z;
        }
        int callingUserId2 = UserHandle.getUserId(callingUid);
        if (this.mService.isAssociatedCompanionApp(callingUserId2, callingUid)) {
            OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).monitorActivityStartInfo("Q_callingUidHasCompanionDevice");
            return false;
        }
        int callerAppUid2 = callingUid;
        if (callerApp == null) {
            callerApp2 = this.mService.getProcessController(realCallingPid, realCallingUid);
            callerAppUid2 = realCallingUid;
        } else {
            callerApp2 = callerApp;
        }
        if (callerApp2 == null) {
            callerAppUid = callerAppUid2;
            callingUserId = callingUserId2;
            z2 = true;
        } else if (callerApp2.areBackgroundActivityStartsAllowed()) {
            return false;
        } else {
            callingUserId = callingUserId2;
            ArraySet<WindowProcessController> uidProcesses = this.mService.mProcessMap.getProcesses(callerAppUid2);
            if (uidProcesses != null) {
                z2 = true;
                callerAppUid = callerAppUid2;
                int i = uidProcesses.size() - 1;
                while (i >= 0) {
                    WindowProcessController proc = uidProcesses.valueAt(i);
                    if (proc != callerApp2 && proc.areBackgroundActivityStartsAllowed()) {
                        return false;
                    }
                    i--;
                    uidProcesses = uidProcesses;
                }
            } else {
                callerAppUid = callerAppUid2;
                z2 = true;
            }
        }
        if (this.mService.hasSystemAlertWindowPermission(callingUid, callingPid, callingPackage)) {
            Slog.w(TAG, "Background activity start for " + callingPackage + " allowed because SYSTEM_ALERT_WINDOW permission is granted.");
            OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).monitorActivityStartInfo("Q_callingUidHasSYSTEM_ALERT_WINDOW");
            return false;
        }
        Slog.w(TAG, "Background activity start [callingPackage: " + callingPackage + "; callingUid: " + callingUid + "; isCallingUidForeground: " + isCallingUidForeground + "; isCallingUidPersistentSystemProcess: " + isCallingUidPersistentSystemProcess + "; realCallingUid: " + realCallingUid + "; isRealCallingUidForeground: " + isRealCallingUidForeground + "; isRealCallingUidPersistentSystemProcess: " + isRealCallingUidPersistentSystemProcess + "; originatingPendingIntent: " + originatingPendingIntent + "; isBgStartWhitelisted: " + allowBackgroundActivityStart + "; intent: " + intent + "; callerApp: " + callerApp2 + "]");
        if (!this.mService.isActivityStartsLoggingEnabled()) {
            return z2;
        }
        this.mSupervisor.getActivityMetricsLogger().logAbortedBgActivityStart(intent, callerApp2, callingUid, callingPackage, callingUidProcState, callingUidHasAnyVisibleWindow, realCallingUid, realCallingUidProcState, realCallingUidHasAnyVisibleWindow, originatingPendingIntent != null ? z2 : false);
        return z2;
    }

    private Intent createLaunchIntent(AuxiliaryResolveInfo auxiliaryResponse, Intent originalIntent, String callingPackage, Bundle verificationBundle, String resolvedType, int userId) {
        if (auxiliaryResponse != null && auxiliaryResponse.needsPhaseTwo) {
            this.mService.getPackageManagerInternalLocked().requestInstantAppResolutionPhaseTwo(auxiliaryResponse, originalIntent, resolvedType, callingPackage, verificationBundle, userId);
        }
        Intent sanitizeIntent = InstantAppResolver.sanitizeIntent(originalIntent);
        List list = null;
        Intent intent = auxiliaryResponse == null ? null : auxiliaryResponse.failureIntent;
        ComponentName componentName = auxiliaryResponse == null ? null : auxiliaryResponse.installFailureActivity;
        String str = auxiliaryResponse == null ? null : auxiliaryResponse.token;
        boolean z = auxiliaryResponse != null && auxiliaryResponse.needsPhaseTwo;
        if (auxiliaryResponse != null) {
            list = auxiliaryResponse.filters;
        }
        return InstantAppResolver.buildEphemeralInstallerIntent(originalIntent, sanitizeIntent, intent, callingPackage, verificationBundle, resolvedType, userId, componentName, str, z, list);
    }

    /* access modifiers changed from: package-private */
    public void postStartActivityProcessing(ActivityRecord r, int result, ActivityStack startedActivityStack) {
        ActivityStack homeStack;
        if (!ActivityManager.isStartResultFatalError(result)) {
            this.mSupervisor.reportWaitingActivityLaunchedIfNeeded(r, result);
            if (startedActivityStack != null) {
                boolean clearedTask = (this.mLaunchFlags & 268468224) == 268468224 && this.mReuseTask != null;
                if (result == 2 || result == 3 || clearedTask) {
                    int windowingMode = startedActivityStack.getWindowingMode();
                    if (windowingMode == 2) {
                        this.mService.getTaskChangeNotificationController().notifyPinnedActivityRestartAttempt(clearedTask);
                    } else if (windowingMode == 3 && (homeStack = startedActivityStack.getDisplay().getHomeStack()) != null) {
                        homeStack.shouldBeVisible(null);
                    }
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v31, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r2v97, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r2v102, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r15v2 int: [D('callingPid' int), D('callingUid' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x029b  */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x02c7 A[DONT_GENERATE] */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x02cc  */
    /* JADX WARNING: Removed duplicated region for block: B:206:0x05b0 A[SYNTHETIC, Splitter:B:206:0x05b0] */
    /* JADX WARNING: Removed duplicated region for block: B:218:0x05ed  */
    /* JADX WARNING: Removed duplicated region for block: B:223:0x05ff A[SYNTHETIC, Splitter:B:223:0x05ff] */
    /* JADX WARNING: Removed duplicated region for block: B:272:0x06ad  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x018e  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01e6  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x021e  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0263 A[SYNTHETIC, Splitter:B:92:0x0263] */
    private int startActivityMayWait(IApplicationThread caller, int callingUid, String callingPackage, int requestRealCallingPid, int requestRealCallingUid, Intent intent, String resolvedType, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, WaitResult outResult, Configuration globalConfig, SafeActivityOptions options, boolean ignoreTargetSecurity, int userId, TaskRecord inTask, String reason, boolean allowPendingRemoteAnimationRegistryLookup, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
        int realCallingPid;
        int realCallingUid;
        int callingUid2;
        boolean componentSpecified;
        ResolveInfo rInfo;
        ActivityInfo aInfo;
        int i;
        int realCallingPid2;
        ResolveInfo rInfo2;
        int realCallingUid2;
        boolean componentSpecified2;
        Configuration configuration;
        ActivityInfo aInfo2;
        int i2;
        WindowManagerGlobalLock windowManagerGlobalLock;
        WindowManagerGlobalLock windowManagerGlobalLock2;
        IIntentSender target;
        boolean z;
        ActivityStack stack;
        int realCallingUid3;
        boolean componentSpecified3;
        int callingUid3;
        int callingPid;
        IApplicationThread caller2;
        ResolveInfo rInfo3;
        String resolvedType2;
        ResolveInfo rInfo4;
        int appCallingUid;
        int callingPid2;
        ResolveInfo rInfo5;
        ResolveInfo rInfo6;
        ActivityInfo activityInfo;
        UserInfo userInfo;
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(80, Binder.getCallingUid());
        if (intent == null || !intent.hasFileDescriptors()) {
            this.mSupervisor.getActivityMetricsLogger().notifyActivityLaunching(intent);
            boolean componentSpecified4 = intent.getComponent() != null;
            OppoFeatureCache.get(IColorFastAppManager.DEFAULT).fastWechatPayIfNeeded(intent);
            boolean implicit = !componentSpecified4 && intent.getPackage() == null;
            Intent originIntent = new Intent(intent);
            OppoFeatureCache.get(IColorFastAppManager.DEFAULT).fastWechatPayIfNeeded(intent);
            if (requestRealCallingPid != 0) {
                realCallingPid = requestRealCallingPid;
            } else {
                realCallingPid = Binder.getCallingPid();
            }
            int callingPid3 = -1;
            if (requestRealCallingUid != -1) {
                realCallingUid = requestRealCallingUid;
            } else {
                realCallingUid = Binder.getCallingUid();
            }
            if (callingUid >= 0) {
                callingPid3 = -1;
                callingUid2 = callingUid;
            } else if (caller == null) {
                callingPid3 = realCallingPid;
                callingUid2 = realCallingUid;
            } else {
                callingUid2 = -1;
            }
            Intent ephemeralIntent = new Intent(intent);
            Intent intent2 = new Intent(intent);
            if (!componentSpecified4 || (("android.intent.action.VIEW".equals(intent2.getAction()) && intent2.getData() == null) || "android.intent.action.INSTALL_INSTANT_APP_PACKAGE".equals(intent2.getAction()) || "android.intent.action.RESOLVE_INSTANT_APP_PACKAGE".equals(intent2.getAction()) || !this.mService.getPackageManagerInternalLocked().isInstantAppInstallerComponent(intent2.getComponent()))) {
                componentSpecified = componentSpecified4;
            } else {
                intent2.setComponent(null);
                componentSpecified = false;
            }
            int callingUid4 = callingUid2;
            int userId2 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).checkIntent(checkSpecialApp(OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).checkCategory(userId, intent2), callingPackage, componentSpecified, intent2, this.mRootActivityContainer), callingPackage, componentSpecified, intent2);
            ResolveInfo rInfo7 = this.mSupervisor.resolveIntent(intent2, resolvedType, userId2, 0, computeResolveFilterUid(callingUid4, realCallingUid, this.mRequest.filterCallingUid));
            if (rInfo7 == null && (userInfo = this.mSupervisor.getUserInfo(userId2)) != null && userInfo.isManagedProfile()) {
                UserManager userManager = UserManager.get(this.mService.mContext);
                long token = Binder.clearCallingIdentity();
                try {
                    UserInfo parent = userManager.getProfileParent(userId2);
                    if (parent != null && userManager.isUserUnlockingOrUnlocked(parent.id) && !userManager.isUserUnlockingOrUnlocked(userId2)) {
                        rInfo = this.mSupervisor.resolveIntent(intent2, resolvedType, userId2, 786432, computeResolveFilterUid(callingUid4, realCallingUid, this.mRequest.filterCallingUid));
                        aInfo = this.mSupervisor.resolveActivity(intent2, rInfo, startFlags, profilerInfo);
                        if (aInfo != null) {
                            OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction_startActivityMayWait(Binder.getCallingUid(), intent2.getAction(), intent2.getComponent(), aInfo.packageName, intent2.getData());
                        }
                        if (aInfo != null || aInfo.applicationInfo == null || aInfo.applicationInfo.packageName == null) {
                            i = 0;
                        } else if (aInfo.applicationInfo.packageName.equals("com.youxi.hycs.nearme.gamecenter")) {
                            i = 0;
                            aInfo.screenOrientation = 0;
                        } else {
                            i = 0;
                        }
                        OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).preloadAppSplash(realCallingPid, options, aInfo, reason);
                        if (!shouldCorrectActivityInfo(aInfo, userId2, implicit, callingPackage)) {
                            rInfo2 = rInfo;
                            componentSpecified2 = componentSpecified;
                            i2 = i;
                            realCallingUid2 = realCallingUid;
                            configuration = globalConfig;
                            realCallingPid2 = realCallingPid;
                            aInfo2 = getCorrectActivityInfo(aInfo, 0, this.mRequest.filterCallingUid, originIntent, callingUid4, realCallingUid, this.mSupervisor, resolvedType, startFlags, profilerInfo);
                            intent2 = originIntent;
                            userId2 = 0;
                        } else {
                            i2 = i;
                            rInfo2 = rInfo;
                            componentSpecified2 = componentSpecified;
                            realCallingPid2 = realCallingPid;
                            realCallingUid2 = realCallingUid;
                            configuration = globalConfig;
                            aInfo2 = aInfo;
                        }
                        int userId3 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).correctUserId(userId2, aInfo2);
                        if (aInfo2 != null || !this.mService.mOppoArmyController.isRunningDisallowed(aInfo2.applicationInfo.packageName)) {
                            OppoFeatureCache.get(IColorSplitWindowManager.DEFAULT).adjustActivityResizeMode(aInfo2);
                            windowManagerGlobalLock = this.mService.mGlobalLock;
                            synchronized (windowManagerGlobalLock) {
                                try {
                                    WindowManagerService.boostPriorityForLockedSection();
                                    ActivityStack stack2 = this.mRootActivityContainer.getTopDisplayFocusedStack();
                                    if (configuration != null) {
                                        try {
                                            if (this.mService.getGlobalConfiguration().diff(configuration) != 0) {
                                                z = 1;
                                                stack2.mConfigWillChange = z;
                                                if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                                    Slog.v(TAG_CONFIGURATION, "Starting activity when config will change = " + stack2.mConfigWillChange);
                                                }
                                                long origId = Binder.clearCallingIdentity();
                                                if (!OppoFeatureCache.get(IColorIntentInterceptManager.DEFAULT).interceptSougouSiteIfNeeded(callingPackage, stack2, intent2)) {
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    return i2;
                                                }
                                                if (aInfo2 != null) {
                                                    try {
                                                        if ((aInfo2.applicationInfo.privateFlags & 2) != 0 && this.mService.mHasHeavyWeightFeature) {
                                                            if (aInfo2.processName.equals(aInfo2.applicationInfo.packageName)) {
                                                                WindowProcessController heavy = this.mService.mHeavyWeightProcess;
                                                                if (heavy == null) {
                                                                    caller2 = caller;
                                                                    stack = stack2;
                                                                    windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                                    realCallingUid3 = realCallingUid2;
                                                                } else if (heavy.mInfo.uid != aInfo2.applicationInfo.uid || !heavy.mName.equals(aInfo2.processName)) {
                                                                    if (caller != null) {
                                                                        try {
                                                                            WindowProcessController callerApp = this.mService.getProcessController(caller);
                                                                            if (callerApp != null) {
                                                                                appCallingUid = callerApp.mInfo.uid;
                                                                            } else {
                                                                                StringBuilder sb = new StringBuilder();
                                                                                sb.append("Unable to find app for caller ");
                                                                                sb.append(caller);
                                                                                sb.append(" (pid=");
                                                                                try {
                                                                                    sb.append(callingPid3);
                                                                                    sb.append(") when starting: ");
                                                                                    sb.append(intent2.toString());
                                                                                    Slog.w(TAG, sb.toString());
                                                                                    SafeActivityOptions.abort(options);
                                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                                    return -94;
                                                                                } catch (Throwable th) {
                                                                                    target = th;
                                                                                    windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                                                    while (true) {
                                                                                        try {
                                                                                            break;
                                                                                        } catch (Throwable th2) {
                                                                                            target = th2;
                                                                                        }
                                                                                    }
                                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                                    throw target;
                                                                                }
                                                                            }
                                                                        } catch (Throwable th3) {
                                                                            target = th3;
                                                                            windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            WindowManagerService.resetPriorityAfterLockedSection();
                                                                            throw target;
                                                                        }
                                                                    } else {
                                                                        appCallingUid = callingUid4;
                                                                    }
                                                                    try {
                                                                        ActivityTaskManagerService activityTaskManagerService = this.mService;
                                                                        Intent[] intentArr = new Intent[1];
                                                                        intentArr[i2] = intent2;
                                                                        IIntentSender target2 = activityTaskManagerService.getIntentSenderLocked(2, PackageManagerService.PLATFORM_PACKAGE_NAME, appCallingUid, userId3, null, null, 0, intentArr, new String[]{resolvedType}, 1342177280, null);
                                                                        Intent newIntent = new Intent();
                                                                        if (requestCode >= 0) {
                                                                            newIntent.putExtra("has_result", true);
                                                                        }
                                                                        newIntent.putExtra("intent", new IntentSender(target2));
                                                                        heavy.updateIntentForHeavyWeightActivity(newIntent);
                                                                        newIntent.putExtra("new_app", aInfo2.packageName);
                                                                        newIntent.setFlags(intent2.getFlags());
                                                                        newIntent.setClassName(PackageManagerService.PLATFORM_PACKAGE_NAME, HeavyWeightSwitcherActivity.class.getName());
                                                                        try {
                                                                            callingUid4 = Binder.getCallingUid();
                                                                            callingPid2 = Binder.getCallingPid();
                                                                            try {
                                                                                rInfo5 = this.mSupervisor.resolveIntent(newIntent, null, userId3, 0, computeResolveFilterUid(callingUid4, realCallingUid2, this.mRequest.filterCallingUid));
                                                                            } catch (Throwable th4) {
                                                                                target = th4;
                                                                                windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                                                while (true) {
                                                                                    break;
                                                                                }
                                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                                throw target;
                                                                            }
                                                                        } catch (Throwable th5) {
                                                                            target = th5;
                                                                            windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            WindowManagerService.resetPriorityAfterLockedSection();
                                                                            throw target;
                                                                        }
                                                                        try {
                                                                            if (shouldCorrectResolveInfo(rInfo5, userId3, callingPackage)) {
                                                                                try {
                                                                                    realCallingUid3 = realCallingUid2;
                                                                                    rInfo6 = rInfo5;
                                                                                    stack = stack2;
                                                                                    windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                                                } catch (Throwable th6) {
                                                                                    target = th6;
                                                                                    windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                                                    while (true) {
                                                                                        break;
                                                                                    }
                                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                                    throw target;
                                                                                }
                                                                                try {
                                                                                    getCorrectResolveInfo(rInfo5, this.mSupervisor, newIntent, null, 0, 0, callingUid4, realCallingUid3, this.mRequest.filterCallingUid);
                                                                                    userId3 = 0;
                                                                                } catch (Throwable th7) {
                                                                                    target = th7;
                                                                                    while (true) {
                                                                                        break;
                                                                                    }
                                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                                    throw target;
                                                                                }
                                                                            } else {
                                                                                realCallingUid3 = realCallingUid2;
                                                                                rInfo6 = rInfo5;
                                                                                stack = stack2;
                                                                                windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                                            }
                                                                            rInfo3 = rInfo6;
                                                                            if (rInfo3 != null) {
                                                                                try {
                                                                                    activityInfo = rInfo3.activityInfo;
                                                                                } catch (Throwable th8) {
                                                                                    target = th8;
                                                                                    while (true) {
                                                                                        break;
                                                                                    }
                                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                                    throw target;
                                                                                }
                                                                            } else {
                                                                                activityInfo = null;
                                                                            }
                                                                            aInfo2 = activityInfo;
                                                                            if (aInfo2 != null) {
                                                                                aInfo2 = this.mService.mAmInternal.getActivityInfoForUser(aInfo2, userId3);
                                                                                callingUid3 = callingUid4;
                                                                                componentSpecified3 = true;
                                                                                resolvedType2 = null;
                                                                                caller2 = null;
                                                                                callingPid = callingPid2;
                                                                                intent2 = newIntent;
                                                                            } else {
                                                                                callingUid3 = callingUid4;
                                                                                componentSpecified3 = true;
                                                                                resolvedType2 = null;
                                                                                caller2 = null;
                                                                                callingPid = callingPid2;
                                                                                intent2 = newIntent;
                                                                            }
                                                                            ActivityRecord[] outRecord = new ActivityRecord[1];
                                                                            int res = startActivity(caller2, intent2, ephemeralIntent, resolvedType2, aInfo2, rInfo3, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid3, callingPackage, realCallingPid2, realCallingUid3, startFlags, options, ignoreTargetSecurity, componentSpecified3, outRecord, inTask, reason, allowPendingRemoteAnimationRegistryLookup, originatingPendingIntent, allowBackgroundActivityStart);
                                                                            Binder.restoreCallingIdentity(origId);
                                                                            if (stack.mConfigWillChange) {
                                                                                try {
                                                                                    rInfo4 = rInfo3;
                                                                                    try {
                                                                                        this.mService.mAmInternal.enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateConfiguration()");
                                                                                        stack.mConfigWillChange = false;
                                                                                        if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                                                                            Slog.v(TAG_CONFIGURATION, "Updating to new configuration after starting activity.");
                                                                                        }
                                                                                        this.mService.updateConfigurationLocked(configuration, null, false);
                                                                                    } catch (Throwable th9) {
                                                                                        target = th9;
                                                                                        while (true) {
                                                                                            break;
                                                                                        }
                                                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                                                        throw target;
                                                                                    }
                                                                                } catch (Throwable th10) {
                                                                                    target = th10;
                                                                                    while (true) {
                                                                                        break;
                                                                                    }
                                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                                    throw target;
                                                                                }
                                                                            } else {
                                                                                rInfo4 = rInfo3;
                                                                            }
                                                                            this.mSupervisor.getActivityMetricsLogger().notifyActivityLaunched(res, outRecord[0]);
                                                                            if (outResult != null) {
                                                                                try {
                                                                                    outResult.result = res;
                                                                                    ActivityRecord r = outRecord[0];
                                                                                    if (res != 0) {
                                                                                        int i3 = 3;
                                                                                        if (res == 2) {
                                                                                            try {
                                                                                                if (!r.attachedToProcess()) {
                                                                                                    i3 = 1;
                                                                                                }
                                                                                                outResult.launchState = i3;
                                                                                                if (!r.nowVisible || !r.isState(ActivityStack.ActivityState.RESUMED)) {
                                                                                                    try {
                                                                                                        this.mSupervisor.waitActivityVisible(r.mActivityComponent, outResult, SystemClock.uptimeMillis());
                                                                                                        do {
                                                                                                            try {
                                                                                                                this.mService.mGlobalLock.wait();
                                                                                                            } catch (InterruptedException e) {
                                                                                                            }
                                                                                                            if (outResult.timeout) {
                                                                                                                break;
                                                                                                            }
                                                                                                        } while (outResult.who == null);
                                                                                                    } catch (Throwable th11) {
                                                                                                        target = th11;
                                                                                                        while (true) {
                                                                                                            break;
                                                                                                        }
                                                                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                                                                        throw target;
                                                                                                    }
                                                                                                } else {
                                                                                                    outResult.timeout = false;
                                                                                                    outResult.who = r.mActivityComponent;
                                                                                                    outResult.totalTime = 0;
                                                                                                }
                                                                                            } catch (Throwable th12) {
                                                                                                target = th12;
                                                                                                while (true) {
                                                                                                    break;
                                                                                                }
                                                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                                                throw target;
                                                                                            }
                                                                                        } else if (res == 3) {
                                                                                            try {
                                                                                                outResult.timeout = false;
                                                                                                outResult.who = r.mActivityComponent;
                                                                                                outResult.totalTime = 0;
                                                                                            } catch (Throwable th13) {
                                                                                                target = th13;
                                                                                                while (true) {
                                                                                                    break;
                                                                                                }
                                                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                                                throw target;
                                                                                            }
                                                                                        }
                                                                                    } else {
                                                                                        this.mSupervisor.mWaitingActivityLaunched.add(outResult);
                                                                                        do {
                                                                                            try {
                                                                                                this.mService.mGlobalLock.wait();
                                                                                            } catch (InterruptedException e2) {
                                                                                            }
                                                                                            if (outResult.result == 2 || outResult.timeout) {
                                                                                            }
                                                                                        } while (outResult.who == null);
                                                                                        if (outResult.result == 2) {
                                                                                            res = 2;
                                                                                        }
                                                                                    }
                                                                                } catch (Throwable th14) {
                                                                                    target = th14;
                                                                                    while (true) {
                                                                                        break;
                                                                                    }
                                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                                    throw target;
                                                                                }
                                                                            }
                                                                            WindowManagerService.resetPriorityAfterLockedSection();
                                                                            return res;
                                                                        } catch (Throwable th15) {
                                                                            target = th15;
                                                                            windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            WindowManagerService.resetPriorityAfterLockedSection();
                                                                            throw target;
                                                                        }
                                                                    } catch (Throwable th16) {
                                                                        target = th16;
                                                                        windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                                        while (true) {
                                                                            break;
                                                                        }
                                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                                        throw target;
                                                                    }
                                                                } else {
                                                                    caller2 = caller;
                                                                    stack = stack2;
                                                                    windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                                    realCallingUid3 = realCallingUid2;
                                                                }
                                                            } else {
                                                                caller2 = caller;
                                                                stack = stack2;
                                                                windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                                realCallingUid3 = realCallingUid2;
                                                            }
                                                            callingUid3 = callingUid4;
                                                            componentSpecified3 = componentSpecified2;
                                                            rInfo3 = rInfo2;
                                                            callingPid = callingPid3;
                                                            resolvedType2 = resolvedType;
                                                            ActivityRecord[] outRecord2 = new ActivityRecord[1];
                                                            int res2 = startActivity(caller2, intent2, ephemeralIntent, resolvedType2, aInfo2, rInfo3, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid3, callingPackage, realCallingPid2, realCallingUid3, startFlags, options, ignoreTargetSecurity, componentSpecified3, outRecord2, inTask, reason, allowPendingRemoteAnimationRegistryLookup, originatingPendingIntent, allowBackgroundActivityStart);
                                                            Binder.restoreCallingIdentity(origId);
                                                            if (stack.mConfigWillChange) {
                                                            }
                                                            try {
                                                                this.mSupervisor.getActivityMetricsLogger().notifyActivityLaunched(res2, outRecord2[0]);
                                                                if (outResult != null) {
                                                                }
                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                return res2;
                                                            } catch (Throwable th17) {
                                                                target = th17;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                throw target;
                                                            }
                                                        }
                                                    } catch (Throwable th18) {
                                                        target = th18;
                                                        windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                        while (true) {
                                                            break;
                                                        }
                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                        throw target;
                                                    }
                                                }
                                                caller2 = caller;
                                                stack = stack2;
                                                windowManagerGlobalLock2 = windowManagerGlobalLock;
                                                realCallingUid3 = realCallingUid2;
                                                callingUid3 = callingUid4;
                                                componentSpecified3 = componentSpecified2;
                                                rInfo3 = rInfo2;
                                                callingPid = callingPid3;
                                                resolvedType2 = resolvedType;
                                                try {
                                                    ActivityRecord[] outRecord22 = new ActivityRecord[1];
                                                } catch (Throwable th19) {
                                                    target = th19;
                                                    while (true) {
                                                        break;
                                                    }
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    throw target;
                                                }
                                                try {
                                                    int res22 = startActivity(caller2, intent2, ephemeralIntent, resolvedType2, aInfo2, rInfo3, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid3, callingPackage, realCallingPid2, realCallingUid3, startFlags, options, ignoreTargetSecurity, componentSpecified3, outRecord22, inTask, reason, allowPendingRemoteAnimationRegistryLookup, originatingPendingIntent, allowBackgroundActivityStart);
                                                    Binder.restoreCallingIdentity(origId);
                                                    if (stack.mConfigWillChange) {
                                                    }
                                                    this.mSupervisor.getActivityMetricsLogger().notifyActivityLaunched(res22, outRecord22[0]);
                                                    if (outResult != null) {
                                                    }
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    return res22;
                                                } catch (Throwable th20) {
                                                    target = th20;
                                                    while (true) {
                                                        break;
                                                    }
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    throw target;
                                                }
                                            }
                                        } catch (Throwable th21) {
                                            target = th21;
                                            windowManagerGlobalLock2 = windowManagerGlobalLock;
                                            while (true) {
                                                break;
                                            }
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            throw target;
                                        }
                                    }
                                    z = i2;
                                    stack2.mConfigWillChange = z;
                                    if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                    }
                                    long origId2 = Binder.clearCallingIdentity();
                                    if (!OppoFeatureCache.get(IColorIntentInterceptManager.DEFAULT).interceptSougouSiteIfNeeded(callingPackage, stack2, intent2)) {
                                    }
                                } catch (Throwable th22) {
                                    target = th22;
                                    windowManagerGlobalLock2 = windowManagerGlobalLock;
                                    while (true) {
                                        break;
                                    }
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw target;
                                }
                            }
                        } else {
                            this.mService.mOppoArmyController.showDisallowedRunningAppDialog();
                            return i2;
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
            rInfo = rInfo7;
            aInfo = this.mSupervisor.resolveActivity(intent2, rInfo, startFlags, profilerInfo);
            if (aInfo != null) {
            }
            if (aInfo != null) {
            }
            i = 0;
            OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).preloadAppSplash(realCallingPid, options, aInfo, reason);
            if (!shouldCorrectActivityInfo(aInfo, userId2, implicit, callingPackage)) {
            }
            int userId32 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).correctUserId(userId2, aInfo2);
            if (aInfo2 != null) {
            }
            OppoFeatureCache.get(IColorSplitWindowManager.DEFAULT).adjustActivityResizeMode(aInfo2);
            windowManagerGlobalLock = this.mService.mGlobalLock;
            synchronized (windowManagerGlobalLock) {
            }
        } else {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
    }

    static int computeResolveFilterUid(int customCallingUid, int actualCallingUid, int filterCallingUid) {
        if (filterCallingUid != -10000) {
            return filterCallingUid;
        }
        return customCallingUid >= 0 ? customCallingUid : actualCallingUid;
    }

    /* JADX INFO: finally extract failed */
    private int startActivity(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask, ActivityRecord[] outActivity, boolean restrictedBgActivity) {
        ActivityRecord currentTop;
        ActivityRecord currentTop2;
        try {
            this.mService.mWindowManager.deferSurfaceLayout();
            int result = startActivityUnchecked(r, sourceRecord, voiceSession, voiceInteractor, startFlags, doResume, options, inTask, outActivity, restrictedBgActivity);
            ActivityStack currentStack = r.getActivityStack();
            ActivityStack startedActivityStack = currentStack != null ? currentStack : this.mTargetStack;
            if (!ActivityManager.isStartResultSuccessful(result)) {
                ActivityStack stack = this.mStartActivity.getActivityStack();
                if (stack != null) {
                    stack.finishActivityLocked(this.mStartActivity, 0, null, "startActivity", true);
                }
                if (startedActivityStack != null && startedActivityStack.isAttached() && startedActivityStack.numActivities() == 0 && !startedActivityStack.isActivityTypeHome()) {
                    startedActivityStack.remove();
                }
            } else if (!(startedActivityStack == null || (currentTop2 = startedActivityStack.topRunningActivityLocked()) == null || !currentTop2.shouldUpdateConfigForDisplayChanged())) {
                this.mRootActivityContainer.ensureVisibilityAndConfig(currentTop2, currentTop2.getDisplayId(), true, false);
            }
            this.mService.mWindowManager.continueSurfaceLayout();
            postStartActivityProcessing(r, result, startedActivityStack);
            return result;
        } catch (Throwable th) {
            ActivityStack currentStack2 = r.getActivityStack();
            ActivityStack startedActivityStack2 = currentStack2 != null ? currentStack2 : this.mTargetStack;
            if (!ActivityManager.isStartResultSuccessful(-96)) {
                ActivityStack stack2 = this.mStartActivity.getActivityStack();
                if (stack2 != null) {
                    stack2.finishActivityLocked(this.mStartActivity, 0, null, "startActivity", true);
                }
                if (startedActivityStack2 != null && startedActivityStack2.isAttached() && startedActivityStack2.numActivities() == 0 && !startedActivityStack2.isActivityTypeHome()) {
                    startedActivityStack2.remove();
                }
            } else if (!(startedActivityStack2 == null || (currentTop = startedActivityStack2.topRunningActivityLocked()) == null || !currentTop.shouldUpdateConfigForDisplayChanged())) {
                this.mRootActivityContainer.ensureVisibilityAndConfig(currentTop, currentTop.getDisplayId(), true, false);
            }
            this.mService.mWindowManager.continueSurfaceLayout();
            throw th;
        }
    }

    private boolean handleBackgroundActivityAbort(ActivityRecord r) {
        if (!(!this.mService.isBackgroundActivityStartsEnabled())) {
            return false;
        }
        ActivityRecord resultRecord = r.resultTo;
        String resultWho = r.resultWho;
        int requestCode = r.requestCode;
        if (resultRecord != null) {
            resultRecord.getActivityStack().sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
        }
        ActivityOptions.abort(r.pendingOptions);
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:128:0x0290  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x0297  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x02c5  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00d6  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00f7  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00fc  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0130  */
    private int startActivityUnchecked(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask, ActivityRecord[] outActivity, boolean restrictedBgActivity) {
        ActivityRecord reusedActivity;
        int i;
        ActivityRecord reusedActivity2;
        int result;
        ActivityRecord activityRecord;
        setInitialState(r, options, inTask, doResume, startFlags, sourceRecord, voiceSession, voiceInteractor, restrictedBgActivity);
        int preferredWindowingMode = this.mLaunchParams.mWindowingMode;
        computeLaunchingTaskFlags();
        computeSourceStack();
        this.mIntent.setFlags(this.mLaunchFlags);
        ActivityRecord reusedActivity3 = getReusableIntentActivity();
        if (this.mRootActivityContainer.getTopDisplayFocusedStack() != null && this.mRootActivityContainer.getTopDisplayFocusedStack().inSplitScreenWindowingMode()) {
            if (!(!OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).isAppUnlockPasswordActivity(r) || options == null || options.getLaunchTaskId() == -1)) {
                reusedActivity3 = null;
                this.mInTask = this.mRootActivityContainer.anyTaskForId(this.mOptions.getLaunchTaskId());
                this.mAddingToTask = true;
            }
            if (reusedActivity3 != null && OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).isAppUnlockPasswordActivity(reusedActivity3)) {
                reusedActivity3 = null;
            }
        }
        if (reusedActivity3 != null && reusedActivity3.equals(reusedActivity3.getTaskRecord().getRootActivity()) && OppoFeatureCache.get(IColorAccessControlLocalManager.DEFAULT).isAppUnlockPasswordActivity(reusedActivity3)) {
            reusedActivity3 = null;
        }
        if (reusedActivity3 != null) {
            TaskRecord task = reusedActivity3.getTaskRecord();
            ColorZoomWindowManagerHelper.getInstance();
            if (ColorZoomWindowManagerHelper.getZoomWindowManager().shouldClearReusedActivity(reusedActivity3, options, this.mStartActivity, reusedActivity3.getLastReportedConfiguration())) {
                this.mSourceRecord = null;
                this.mSourceStack = null;
                this.mAddingToTask = true;
                this.mInTask = task;
                reusedActivity = null;
                this.mSupervisor.getLaunchParamsController().calculate(reusedActivity == null ? reusedActivity.getTaskRecord() : this.mInTask, r.info.windowLayout, r, sourceRecord, options, 2, this.mLaunchParams);
                if (!this.mLaunchParams.hasPreferredDisplay()) {
                    i = this.mLaunchParams.mPreferredDisplayId;
                } else {
                    i = 0;
                }
                this.mPreferredDisplayId = i;
                if (r.isActivityTypeHome() || this.mRootActivityContainer.canStartHomeOnDisplay(r.info, this.mPreferredDisplayId, true)) {
                    if (reusedActivity == null) {
                        if (this.mService.getLockTaskController().isLockTaskModeViolation(reusedActivity.getTaskRecord(), (this.mLaunchFlags & 268468224) == 268468224)) {
                            Slog.e(TAG, "startActivityUnchecked: Attempt to violate Lock Task Mode");
                            return 101;
                        }
                        boolean clearTopAndResetStandardLaunchMode = (this.mLaunchFlags & 69206016) == 69206016 && this.mLaunchMode == 0;
                        if (this.mStartActivity.getTaskRecord() == null && !clearTopAndResetStandardLaunchMode) {
                            this.mStartActivity.setTask(reusedActivity.getTaskRecord());
                        }
                        if (reusedActivity.getTaskRecord().intent == null) {
                            reusedActivity.getTaskRecord().setIntent(this.mStartActivity);
                        } else {
                            if ((this.mStartActivity.intent.getFlags() & 16384) != 0) {
                                reusedActivity.getTaskRecord().intent.addFlags(16384);
                            } else {
                                reusedActivity.getTaskRecord().intent.removeFlags(16384);
                            }
                        }
                        int i2 = this.mLaunchFlags;
                        if ((67108864 & i2) != 0 || isDocumentLaunchesIntoExisting(i2) || isLaunchModeOneOf(3, 2)) {
                            TaskRecord task2 = reusedActivity.getTaskRecord();
                            ActivityRecord top = task2.performClearTaskForReuseLocked(this.mStartActivity, this.mLaunchFlags);
                            if (reusedActivity.getTaskRecord() == null) {
                                reusedActivity.setTask(task2);
                            }
                            if (top != null) {
                                if (top.frontOfTask) {
                                    top.getTaskRecord().setIntent(this.mStartActivity);
                                }
                                deliverNewIntent(top);
                            }
                        }
                        OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).updateZoomStack(reusedActivity.getActivityStack(), options, this.mStartActivity, sourceRecord);
                        this.mRootActivityContainer.sendPowerHintForLaunchStartIfNeeded(false, reusedActivity);
                        reusedActivity2 = setTargetStackAndMoveToFrontIfNeeded(reusedActivity);
                        ActivityRecord outResult = (outActivity == null || outActivity.length <= 0) ? null : outActivity[0];
                        if (outResult != null && (outResult.finishing || outResult.noDisplay)) {
                            outActivity[0] = reusedActivity2;
                        }
                        if ((this.mStartFlags & 1) != 0) {
                            resumeTargetStackIfNeeded();
                            return 1;
                        } else if (reusedActivity2 != null) {
                            setTaskFromIntentActivity(reusedActivity2);
                            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d(ActivityStackSupervisor.TAG_TASKS, "mAddingToTask: " + this.mAddingToTask + " mReuseTask " + this.mReuseTask + " r.packageName " + r.packageName);
                            }
                            if (!this.mAddingToTask && this.mReuseTask == null && r.packageName != null && (this.mLaunchFlags & Integer.MIN_VALUE) == 0 && !skipResumeTargetStack(reusedActivity2, r, this.mTargetStack)) {
                                resumeTargetStackIfNeeded();
                                if (outActivity != null && outActivity.length > 0) {
                                    outActivity[0] = reusedActivity2.finishing ? reusedActivity2.getTaskRecord().getTopActivity() : reusedActivity2;
                                }
                                return this.mMovedToFront ? 2 : 3;
                            }
                        }
                    } else {
                        reusedActivity2 = reusedActivity;
                    }
                    if (this.mStartActivity.packageName != null) {
                        ActivityStack sourceStack = this.mStartActivity.resultTo != null ? this.mStartActivity.resultTo.getActivityStack() : null;
                        if (sourceStack != null) {
                            sourceStack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
                        }
                        ActivityOptions.abort(this.mOptions);
                        return -92;
                    }
                    ActivityStack topStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
                    ActivityRecord topFocused = topStack.getTopActivity();
                    ActivityRecord top2 = topStack.topRunningNonDelayedActivityLocked(this.mNotTop);
                    if (top2 != null && this.mStartActivity.resultTo == null && top2.mActivityComponent.equals(this.mStartActivity.mActivityComponent) && top2.mUserId == this.mStartActivity.mUserId && top2.attachedToProcess() && ((this.mLaunchFlags & 536870912) != 0 || isLaunchModeOneOf(1, 2)) && (!top2.isActivityTypeHome() || top2.getDisplayId() == this.mPreferredDisplayId)) {
                        topStack.mLastPausedActivity = null;
                        if (this.mDoResume) {
                            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                        }
                        ActivityOptions.abort(this.mOptions);
                        if ((this.mStartFlags & 1) != 0) {
                            return 1;
                        }
                        deliverNewIntent(top2);
                        this.mSupervisor.handleNonResizableTaskIfNeeded(top2.getTaskRecord(), preferredWindowingMode, this.mPreferredDisplayId, topStack);
                        return 3;
                    }
                    boolean newTask = false;
                    TaskRecord taskToAffiliate = (!this.mLaunchTaskBehind || (activityRecord = this.mSourceRecord) == null) ? null : activityRecord.getTaskRecord();
                    if (this.mStartActivity.resultTo == null && this.mInTask == null && !this.mAddingToTask && (this.mLaunchFlags & 268435456) != 0) {
                        newTask = true;
                        result = setTaskFromReuseOrCreateNewTask(taskToAffiliate);
                    } else if (this.mSourceRecord != null) {
                        result = setTaskFromSourceRecord();
                    } else if (this.mInTask != null) {
                        result = setTaskFromInTask();
                    } else {
                        result = setTaskToCurrentTopOrCreateNewTask();
                    }
                    if (result != 0) {
                        return result;
                    }
                    this.mService.mUgmInternal.grantUriPermissionFromIntent(this.mCallingUid, this.mStartActivity.packageName, this.mIntent, this.mStartActivity.getUriPermissionsLocked(), this.mStartActivity.mUserId);
                    Intent tempIntent = this.mSupervisor.mOppoSecureProtectUtils.secureIntent(this.mCallingUid, sourceRecord, this.mIntent);
                    if (tempIntent == null) {
                        this.mService.mUgmInternal.grantUriPermissionFromIntent(this.mCallingUid, this.mStartActivity.packageName, this.mIntent, this.mStartActivity.getUriPermissionsLocked(), this.mStartActivity.mUserId);
                    } else {
                        this.mService.mUgmInternal.grantUriPermissionFromIntent(this.mCallingUid, this.mStartActivity.packageName, tempIntent, this.mStartActivity.getUriPermissionsLocked(), this.mStartActivity.mUserId);
                    }
                    OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).intentFixUris(this.mCallingUid, this.mIntent, sourceRecord == null ? null : sourceRecord.mColorArEx);
                    this.mService.getPackageManagerInternalLocked().grantEphemeralAccess(this.mStartActivity.mUserId, this.mIntent, UserHandle.getAppId(this.mStartActivity.appInfo.uid), UserHandle.getAppId(this.mCallingUid));
                    if (newTask) {
                        EventLog.writeEvent((int) EventLogTags.AM_CREATE_TASK, Integer.valueOf(this.mStartActivity.mUserId), Integer.valueOf(this.mStartActivity.getTaskRecord().taskId));
                    }
                    ActivityRecord activityRecord2 = this.mStartActivity;
                    ActivityStack.logStartActivity(EventLogTags.AM_CREATE_ACTIVITY, activityRecord2, activityRecord2.getTaskRecord());
                    this.mTargetStack.mLastPausedActivity = null;
                    this.mRootActivityContainer.sendPowerHintForLaunchStartIfNeeded(false, this.mStartActivity);
                    OppoAmsUtils.getInstance(this.mService).speedupSpecialAct(this.mStartActivity);
                    this.mTargetStack.startActivityLocked(this.mStartActivity, topFocused, newTask, this.mKeepCurTransition, this.mOptions);
                    OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).updateZoomStack(this.mStartActivity.getActivityStack(), options, this.mStartActivity, sourceRecord);
                    if (this.mDoResume) {
                        ActivityRecord topTaskActivity = this.mStartActivity.getTaskRecord().topRunningActivityLocked();
                        if (!this.mTargetStack.isFocusable() || !(topTaskActivity == null || !topTaskActivity.mTaskOverlay || this.mStartActivity == topTaskActivity)) {
                            this.mTargetStack.ensureActivitiesVisibleLocked(this.mStartActivity, 0, false);
                            this.mTargetStack.getDisplay().mDisplayContent.executeAppTransition();
                        } else {
                            if (this.mTargetStack.isFocusable() && !this.mRootActivityContainer.isTopDisplayFocusedStack(this.mTargetStack)) {
                                this.mTargetStack.moveToFront("startActivityUnchecked");
                            }
                            this.mRootActivityContainer.resumeFocusedStacksTopActivities(this.mTargetStack, this.mStartActivity, this.mOptions);
                        }
                    } else if (this.mStartActivity != null) {
                        this.mSupervisor.mRecentTasks.add(this.mStartActivity.getTaskRecord());
                    }
                    this.mRootActivityContainer.updateUserStack(this.mStartActivity.mUserId, this.mTargetStack);
                    this.mSupervisor.handleNonResizableTaskIfNeeded(this.mStartActivity.getTaskRecord(), preferredWindowingMode, this.mPreferredDisplayId, this.mTargetStack);
                    if (this.mStartActivity.info == null || this.mStartActivity.info.name == null || this.mService.getColorFreeformManager() == null || !this.mService.getColorFreeformManager().inNextNeedFullscreenCpnList(this.mStartActivity.info.name)) {
                        return 0;
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        Slog.i(TAG, "oppo freeform: startActivityUnchecked t1 " + this.mStartActivity.info.name);
                    }
                    ActivityStack stack = this.mRootActivityContainer.getDefaultDisplay().getStack(5, 1);
                    if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        Slog.i(TAG, "oppo freeform: startActivityUnchecked t1 000 stack = " + stack);
                    }
                    if (stack == null) {
                        return 0;
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        Slog.i(TAG, "oppo freeform: startActivityUnchecked t2 " + this.mStartActivity.info.name);
                    }
                    this.mSupervisor.moveTasksToFullscreenStackLocked(stack, false);
                    return 0;
                }
                Slog.w(TAG, "Cannot launch home on display " + this.mPreferredDisplayId);
                return -96;
            }
        }
        reusedActivity = reusedActivity3;
        this.mSupervisor.getLaunchParamsController().calculate(reusedActivity == null ? reusedActivity.getTaskRecord() : this.mInTask, r.info.windowLayout, r, sourceRecord, options, 2, this.mLaunchParams);
        if (!this.mLaunchParams.hasPreferredDisplay()) {
        }
        this.mPreferredDisplayId = i;
        if (r.isActivityTypeHome()) {
        }
        if (reusedActivity == null) {
        }
        if (this.mStartActivity.packageName != null) {
        }
    }

    /* access modifiers changed from: package-private */
    public void reset(boolean clearRequest) {
        this.mStartActivity = null;
        this.mIntent = null;
        this.mCallingUid = -1;
        this.mOptions = null;
        this.mRestrictedBgActivity = false;
        this.mLaunchTaskBehind = false;
        this.mLaunchFlags = 0;
        this.mLaunchMode = -1;
        this.mLaunchParams.reset();
        this.mNotTop = null;
        this.mDoResume = false;
        this.mStartFlags = 0;
        this.mSourceRecord = null;
        this.mPreferredDisplayId = -1;
        this.mInTask = null;
        this.mAddingToTask = false;
        this.mReuseTask = null;
        this.mNewTaskInfo = null;
        this.mNewTaskIntent = null;
        this.mSourceStack = null;
        this.mTargetStack = null;
        this.mMovedToFront = false;
        this.mNoAnimation = false;
        this.mKeepCurTransition = false;
        this.mAvoidMoveToFront = false;
        this.mVoiceSession = null;
        this.mVoiceInteractor = null;
        this.mIntentDelivered = false;
        if (clearRequest) {
            this.mRequest.reset();
        }
    }

    private void setInitialState(ActivityRecord r, ActivityOptions options, TaskRecord inTask, boolean doResume, int startFlags, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean restrictedBgActivity) {
        int i;
        reset(false);
        this.mStartActivity = r;
        this.mIntent = r.intent;
        this.mOptions = options;
        this.mCallingUid = r.launchedFromUid;
        this.mSourceRecord = sourceRecord;
        this.mVoiceSession = voiceSession;
        this.mVoiceInteractor = voiceInteractor;
        this.mRestrictedBgActivity = restrictedBgActivity;
        this.mLaunchParams.reset();
        this.mSupervisor.getLaunchParamsController().calculate(inTask, r.info.windowLayout, r, sourceRecord, options, 0, this.mLaunchParams);
        if (this.mLaunchParams.hasPreferredDisplay()) {
            i = this.mLaunchParams.mPreferredDisplayId;
        } else {
            i = 0;
        }
        this.mPreferredDisplayId = i;
        this.mLaunchMode = r.launchMode;
        this.mLaunchFlags = adjustLaunchFlagsToDocumentMode(r, 3 == this.mLaunchMode, 2 == this.mLaunchMode, this.mIntent.getFlags());
        this.mLaunchTaskBehind = r.mLaunchTaskBehind && !isLaunchModeOneOf(2, 3) && (this.mLaunchFlags & DumpState.DUMP_FROZEN) != 0;
        sendNewTaskResultRequestIfNeeded();
        if ((this.mLaunchFlags & DumpState.DUMP_FROZEN) != 0 && r.resultTo == null) {
            this.mLaunchFlags |= 268435456;
        }
        if ((this.mLaunchFlags & 268435456) != 0 && (this.mLaunchTaskBehind || r.info.documentLaunchMode == 2)) {
            this.mLaunchFlags |= 134217728;
        }
        this.mSupervisor.mUserLeaving = (this.mLaunchFlags & DumpState.DUMP_DOMAIN_PREFERRED) == 0;
        if (ActivityTaskManagerDebugConfig.DEBUG_USER_LEAVING) {
            Slog.v(TAG_USER_LEAVING, "startActivity() => mUserLeaving=" + this.mSupervisor.mUserLeaving);
        }
        this.mDoResume = doResume;
        if (!doResume || !r.okToShowLocked()) {
            r.delayedResume = true;
            this.mDoResume = false;
        }
        ActivityOptions activityOptions = this.mOptions;
        if (activityOptions != null) {
            if (activityOptions.getLaunchTaskId() != -1 && this.mOptions.getTaskOverlay()) {
                r.mTaskOverlay = true;
                if (!this.mOptions.canTaskOverlayResume()) {
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(this.mOptions.getLaunchTaskId());
                    ActivityRecord top = task != null ? task.getTopActivity() : null;
                    if (top != null && !top.isState(ActivityStack.ActivityState.RESUMED)) {
                        this.mDoResume = false;
                        this.mAvoidMoveToFront = true;
                    }
                }
            } else if (this.mOptions.getAvoidMoveToFront()) {
                this.mDoResume = false;
                this.mAvoidMoveToFront = true;
            }
        }
        this.mNotTop = (this.mLaunchFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0 ? sourceRecord : null;
        this.mInTask = inTask;
        if (inTask != null && !inTask.inRecents) {
            Slog.w(TAG, "Starting activity in task not in recents: " + inTask);
            this.mInTask = null;
        }
        this.mStartFlags = startFlags;
        if ((startFlags & 1) != 0) {
            ActivityRecord checkedCaller = sourceRecord;
            if (checkedCaller == null) {
                checkedCaller = this.mRootActivityContainer.getTopDisplayFocusedStack().topRunningNonDelayedActivityLocked(this.mNotTop);
            }
            if (!checkedCaller.mActivityComponent.equals(r.mActivityComponent)) {
                this.mStartFlags &= -2;
            }
        }
        this.mNoAnimation = (this.mLaunchFlags & 65536) != 0;
        if (this.mRestrictedBgActivity && !this.mService.isBackgroundActivityStartsEnabled()) {
            this.mAvoidMoveToFront = true;
            this.mDoResume = false;
        }
    }

    private void sendNewTaskResultRequestIfNeeded() {
        ActivityStack sourceStack = this.mStartActivity.resultTo != null ? this.mStartActivity.resultTo.getActivityStack() : null;
        if (!(this.mStartActivity.packageName != null && AppBrightnessStat.DEFAULT_LAUNCHER_APP.equals(this.mStartActivity.packageName)) && sourceStack != null) {
            int i = this.mLaunchFlags;
            if ((268435456 & i) != 0 && (i & Integer.MIN_VALUE) == 0) {
                Slog.w(TAG, "Activity is launching as a new task, so cancelling activity result.");
                sourceStack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
                this.mStartActivity.resultTo = null;
            }
        }
    }

    private void computeLaunchingTaskFlags() {
        ActivityRecord activityRecord;
        TaskRecord taskRecord;
        if (this.mSourceRecord != null || (taskRecord = this.mInTask) == null || taskRecord.getStack() == null) {
            this.mInTask = null;
            if ((this.mStartActivity.isResolverActivity() || this.mStartActivity.noDisplay) && (activityRecord = this.mSourceRecord) != null && activityRecord.inFreeformWindowingMode()) {
                this.mAddingToTask = true;
            }
        } else {
            Intent baseIntent = this.mInTask.getBaseIntent();
            ActivityRecord root = this.mInTask.getRootActivity();
            if (baseIntent != null) {
                if (isLaunchModeOneOf(3, 2)) {
                    if (!baseIntent.getComponent().equals(this.mStartActivity.intent.getComponent())) {
                        ActivityOptions.abort(this.mOptions);
                        throw new IllegalArgumentException("Trying to launch singleInstance/Task " + this.mStartActivity + " into different task " + this.mInTask);
                    } else if (root != null) {
                        ActivityOptions.abort(this.mOptions);
                        throw new IllegalArgumentException("Caller with mInTask " + this.mInTask + " has root " + root + " but target is singleInstance/Task");
                    }
                }
                if (root == null) {
                    this.mLaunchFlags = (this.mLaunchFlags & -403185665) | (baseIntent.getFlags() & 403185664);
                    this.mIntent.setFlags(this.mLaunchFlags);
                    this.mInTask.setIntent(this.mStartActivity);
                    this.mAddingToTask = true;
                } else if ((this.mLaunchFlags & 268435456) != 0) {
                    this.mAddingToTask = false;
                } else {
                    this.mAddingToTask = true;
                }
                this.mReuseTask = this.mInTask;
            } else {
                ActivityOptions.abort(this.mOptions);
                throw new IllegalArgumentException("Launching into task without base intent: " + this.mInTask);
            }
        }
        TaskRecord taskRecord2 = this.mInTask;
        if (taskRecord2 == null) {
            ActivityRecord activityRecord2 = this.mSourceRecord;
            if (activityRecord2 == null) {
                if ((this.mLaunchFlags & 268435456) == 0 && taskRecord2 == null) {
                    Slog.w(TAG, "startActivity called from non-Activity context; forcing Intent.FLAG_ACTIVITY_NEW_TASK for: " + this.mIntent);
                    this.mLaunchFlags = this.mLaunchFlags | 268435456;
                }
            } else if (activityRecord2.launchMode == 3) {
                this.mLaunchFlags |= 268435456;
            } else if (isLaunchModeOneOf(3, 2)) {
                this.mLaunchFlags |= 268435456;
            }
        }
    }

    private void computeSourceStack() {
        ActivityRecord activityRecord = this.mSourceRecord;
        if (activityRecord == null) {
            this.mSourceStack = null;
        } else if (!activityRecord.finishing) {
            this.mSourceStack = this.mSourceRecord.getActivityStack();
        } else {
            if ((this.mLaunchFlags & 268435456) == 0) {
                Slog.w(TAG, "startActivity called from finishing " + this.mSourceRecord + "; forcing Intent.FLAG_ACTIVITY_NEW_TASK for: " + this.mIntent);
                this.mLaunchFlags = this.mLaunchFlags | 268435456;
                this.mNewTaskInfo = this.mSourceRecord.info;
                TaskRecord sourceTask = this.mSourceRecord.getTaskRecord();
                this.mNewTaskIntent = sourceTask != null ? sourceTask.intent : null;
            }
            this.mSourceRecord = null;
            this.mSourceStack = null;
        }
    }

    private ActivityRecord getReusableIntentActivity() {
        int i = this.mLaunchFlags;
        boolean z = false;
        boolean putIntoExistingTask = (((268435456 & i) != 0 && (i & 134217728) == 0) || isLaunchModeOneOf(3, 2)) & (this.mInTask == null && this.mStartActivity.resultTo == null);
        ActivityRecord intentActivity = null;
        ActivityOptions activityOptions = this.mOptions;
        if (activityOptions != null && activityOptions.getLaunchTaskId() != -1) {
            TaskRecord task = this.mRootActivityContainer.anyTaskForId(this.mOptions.getLaunchTaskId());
            intentActivity = task != null ? task.getTopActivity() : null;
        } else if (putIntoExistingTask) {
            if (3 == this.mLaunchMode) {
                intentActivity = this.mRootActivityContainer.findActivity(this.mIntent, this.mStartActivity.info, this.mStartActivity.isActivityTypeHome());
            } else if ((this.mLaunchFlags & 4096) != 0) {
                RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
                Intent intent = this.mIntent;
                ActivityInfo activityInfo = this.mStartActivity.info;
                if (2 != this.mLaunchMode) {
                    z = true;
                }
                intentActivity = rootActivityContainer.findActivity(intent, activityInfo, z);
            } else {
                intentActivity = this.mRootActivityContainer.findTask(this.mStartActivity, this.mPreferredDisplayId);
            }
        }
        if (intentActivity == null) {
            return intentActivity;
        }
        if ((this.mStartActivity.isActivityTypeHome() || intentActivity.isActivityTypeHome()) && intentActivity.getDisplayId() != this.mPreferredDisplayId) {
            return null;
        }
        return intentActivity;
    }

    private ActivityRecord setTargetStackAndMoveToFrontIfNeeded(ActivityRecord intentActivity) {
        boolean differentTopTask;
        ActivityRecord activityRecord;
        this.mTargetStack = intentActivity.getActivityStack();
        ActivityStack activityStack = this.mTargetStack;
        activityStack.mLastPausedActivity = null;
        if (this.mPreferredDisplayId == activityStack.mDisplayId) {
            ActivityStack focusStack = this.mTargetStack.getDisplay().getFocusedStack();
            ActivityRecord curTop = focusStack == null ? null : focusStack.topRunningNonDelayedActivityLocked(this.mNotTop);
            TaskRecord topTask = curTop != null ? curTop.getTaskRecord() : null;
            differentTopTask = (topTask == intentActivity.getTaskRecord() && (focusStack == null || topTask == focusStack.topTask())) ? false : true;
        } else {
            differentTopTask = true;
        }
        if (differentTopTask && !this.mAvoidMoveToFront) {
            this.mStartActivity.intent.addFlags(DumpState.DUMP_CHANGES);
            if (this.mSourceRecord == null || (this.mSourceStack.getTopActivity() != null && this.mSourceStack.getTopActivity().getTaskRecord() == this.mSourceRecord.getTaskRecord())) {
                if (this.mLaunchTaskBehind && (activityRecord = this.mSourceRecord) != null) {
                    intentActivity.setTaskToAffiliateWith(activityRecord.getTaskRecord());
                }
                if (!((this.mLaunchFlags & 268468224) == 268468224)) {
                    ActivityRecord activityRecord2 = this.mStartActivity;
                    ActivityStack launchStack = getLaunchStack(activityRecord2, this.mLaunchFlags, activityRecord2.getTaskRecord(), this.mOptions);
                    TaskRecord intentTask = intentActivity.getTaskRecord();
                    if (launchStack == null || launchStack == this.mTargetStack) {
                        this.mTargetStack.moveTaskToFrontLocked(intentTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringingFoundTaskToFront");
                        this.mMovedToFront = true;
                    } else if (launchStack.inSplitScreenWindowingMode()) {
                        if ((this.mLaunchFlags & 4096) != 0) {
                            intentTask.reparent(launchStack, true, 0, true, true, "launchToSide");
                        } else {
                            this.mTargetStack.moveTaskToFrontLocked(intentTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringToFrontInsteadOfAdjacentLaunch");
                        }
                        this.mMovedToFront = launchStack != launchStack.getDisplay().getTopStackInWindowingMode(launchStack.getWindowingMode());
                    } else if (launchStack.mDisplayId != this.mTargetStack.mDisplayId) {
                        intentActivity.getTaskRecord().reparent(launchStack, true, 0, true, true, "reparentToDisplay");
                        this.mMovedToFront = true;
                    } else if (launchStack.isActivityTypeHome() && !this.mTargetStack.isActivityTypeHome()) {
                        intentActivity.getTaskRecord().reparent(launchStack, true, 0, true, true, "reparentingHome");
                        this.mMovedToFront = true;
                    }
                    if (launchStack.inFreeformWindowingMode()) {
                        intentActivity.getTaskRecord().reparent(launchStack, true, 0, true, true, "startActivityForFreeform");
                        this.mMovedToFront = true;
                    }
                    this.mOptions = null;
                    intentActivity.showStartingWindow(null, false, true);
                }
            }
        }
        this.mTargetStack = intentActivity.getActivityStack();
        if (!this.mMovedToFront && this.mDoResume) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                String str = ActivityStackSupervisor.TAG_TASKS;
                Slog.d(str, "Bring to front target: " + this.mTargetStack + " from " + intentActivity);
            }
            this.mTargetStack.moveToFront("intentActivityFound");
        }
        this.mSupervisor.handleNonResizableTaskIfNeeded(intentActivity.getTaskRecord(), 0, 0, this.mTargetStack);
        if ((this.mLaunchFlags & DumpState.DUMP_COMPILER_STATS) != 0) {
            return this.mTargetStack.resetTaskIfNeededLocked(intentActivity, this.mStartActivity);
        }
        return intentActivity;
    }

    private void setTaskFromIntentActivity(ActivityRecord intentActivity) {
        int i = this.mLaunchFlags;
        if ((i & 268468224) == 268468224) {
            TaskRecord task = intentActivity.getTaskRecord();
            task.performClearTaskLocked();
            this.mReuseTask = task;
            this.mReuseTask.setIntent(this.mStartActivity);
        } else if ((i & 67108864) != 0 || isLaunchModeOneOf(3, 2)) {
            if (intentActivity.getTaskRecord().performClearTaskLocked(this.mStartActivity, this.mLaunchFlags) == null) {
                this.mAddingToTask = true;
                this.mStartActivity.setTask(null);
                this.mSourceRecord = intentActivity;
                TaskRecord task2 = this.mSourceRecord.getTaskRecord();
                if (task2 != null && task2.getStack() == null) {
                    this.mTargetStack = computeStackFocus(this.mSourceRecord, false, this.mLaunchFlags, this.mOptions);
                    this.mTargetStack.addTask(task2, true ^ this.mLaunchTaskBehind, "startActivityUnchecked");
                }
            }
        } else if (this.mStartActivity.mActivityComponent.equals(intentActivity.getTaskRecord().realActivity)) {
            if (((this.mLaunchFlags & 536870912) != 0 || 1 == this.mLaunchMode) && intentActivity.mActivityComponent.equals(this.mStartActivity.mActivityComponent)) {
                if (intentActivity.frontOfTask) {
                    intentActivity.getTaskRecord().setIntent(this.mStartActivity);
                }
                deliverNewIntent(intentActivity);
            } else if (!intentActivity.getTaskRecord().isSameIntentFilter(this.mStartActivity)) {
                this.mAddingToTask = true;
                this.mSourceRecord = intentActivity;
            }
        } else if ((this.mLaunchFlags & DumpState.DUMP_COMPILER_STATS) == 0) {
            this.mAddingToTask = true;
            this.mSourceRecord = intentActivity;
        } else if (!intentActivity.getTaskRecord().rootWasReset) {
            intentActivity.getTaskRecord().setIntent(this.mStartActivity);
        }
    }

    private void resumeTargetStackIfNeeded() {
        if (this.mDoResume) {
            this.mRootActivityContainer.resumeFocusedStacksTopActivities(this.mTargetStack, null, this.mOptions);
        } else {
            ActivityOptions.abort(this.mOptions);
        }
        this.mRootActivityContainer.updateUserStack(this.mStartActivity.mUserId, this.mTargetStack);
    }

    private int setTaskFromReuseOrCreateNewTask(TaskRecord taskToAffiliate) {
        TaskRecord taskRecord;
        if (this.mRestrictedBgActivity && (((taskRecord = this.mReuseTask) == null || !taskRecord.containsAppUid(this.mCallingUid)) && handleBackgroundActivityAbort(this.mStartActivity))) {
            return 102;
        }
        this.mTargetStack = computeStackFocus(this.mStartActivity, true, this.mLaunchFlags, this.mOptions);
        TaskRecord taskRecord2 = this.mReuseTask;
        if (taskRecord2 == null) {
            ActivityStack activityStack = this.mTargetStack;
            int nextTaskIdForUserLocked = this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.mUserId);
            ActivityInfo activityInfo = this.mNewTaskInfo;
            if (activityInfo == null) {
                activityInfo = this.mStartActivity.info;
            }
            Intent intent = this.mNewTaskIntent;
            if (intent == null) {
                intent = this.mIntent;
            }
            addOrReparentStartingActivity(activityStack.createTaskRecord(nextTaskIdForUserLocked, activityInfo, intent, this.mVoiceSession, this.mVoiceInteractor, !this.mLaunchTaskBehind, this.mStartActivity, this.mSourceRecord, this.mOptions), "setTaskFromReuseOrCreateNewTask - mReuseTask");
            updateBounds(this.mStartActivity.getTaskRecord(), this.mLaunchParams.mBounds);
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                String str = ActivityStackSupervisor.TAG_TASKS;
                Slog.v(str, "Starting new activity " + this.mStartActivity + " in new task " + this.mStartActivity.getTaskRecord());
            }
        } else {
            addOrReparentStartingActivity(taskRecord2, "setTaskFromReuseOrCreateNewTask");
        }
        if (taskToAffiliate != null) {
            this.mStartActivity.setTaskToAffiliateWith(taskToAffiliate);
        }
        if (this.mService.getLockTaskController().isLockTaskModeViolation(this.mStartActivity.getTaskRecord())) {
            Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        } else if (!this.mDoResume) {
            return 0;
        } else {
            this.mTargetStack.moveToFront("reuseOrNewTask");
            return 0;
        }
    }

    private void deliverNewIntent(ActivityRecord activity) {
        if (!this.mIntentDelivered) {
            ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, activity, activity.getTaskRecord());
            activity.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            this.mIntentDelivered = true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:67:0x015b  */
    private int setTaskFromSourceRecord() {
        int targetDisplayId;
        ActivityRecord top;
        if (this.mService.getLockTaskController().isLockTaskModeViolation(this.mSourceRecord.getTaskRecord())) {
            Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        TaskRecord sourceTask = this.mSourceRecord.getTaskRecord();
        ActivityStack sourceStack = this.mSourceRecord.getActivityStack();
        if (this.mRestrictedBgActivity && !sourceTask.containsAppUid(this.mCallingUid) && handleBackgroundActivityAbort(this.mStartActivity)) {
            return 102;
        }
        ActivityStack activityStack = this.mTargetStack;
        if (activityStack != null) {
            targetDisplayId = activityStack.mDisplayId;
        } else {
            targetDisplayId = sourceStack.mDisplayId;
        }
        if (sourceStack.topTask() != sourceTask || !this.mStartActivity.canBeLaunchedOnDisplay(targetDisplayId)) {
            ActivityRecord activityRecord = this.mStartActivity;
            this.mTargetStack = getLaunchStack(activityRecord, this.mLaunchFlags, activityRecord.getTaskRecord(), this.mOptions);
            if (this.mTargetStack == null && targetDisplayId != sourceStack.mDisplayId) {
                this.mTargetStack = this.mRootActivityContainer.getValidLaunchStackOnDisplay(sourceStack.mDisplayId, this.mStartActivity, this.mOptions, this.mLaunchParams);
            }
            if (this.mTargetStack == null) {
                this.mTargetStack = this.mRootActivityContainer.getNextValidLaunchStack(this.mStartActivity, -1);
            }
        }
        ActivityStack activityStack2 = this.mTargetStack;
        if (activityStack2 == null) {
            this.mTargetStack = sourceStack;
        } else if (activityStack2 != sourceStack) {
            sourceTask.reparent(activityStack2, true, 0, false, true, "launchToSide");
        }
        if (this.mTargetStack.topTask() != sourceTask && !this.mAvoidMoveToFront) {
            this.mTargetStack.moveTaskToFrontLocked(sourceTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "sourceTaskToFront");
        } else if (this.mDoResume) {
            this.mTargetStack.moveToFront("sourceStackToFront");
        }
        if (!this.mAddingToTask) {
            int i = this.mLaunchFlags;
            if ((67108864 & i) != 0) {
                ActivityRecord top2 = sourceTask.performClearTaskLocked(this.mStartActivity, i);
                this.mKeepCurTransition = true;
                if (top2 != null) {
                    ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, top2.getTaskRecord());
                    deliverNewIntent(top2);
                    this.mTargetStack.mLastPausedActivity = null;
                    if (this.mDoResume) {
                        this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                    }
                    ActivityOptions.abort(this.mOptions);
                    return 3;
                }
                addOrReparentStartingActivity(sourceTask, "setTaskFromSourceRecord");
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    String str = ActivityStackSupervisor.TAG_TASKS;
                    Slog.v(str, "Starting new activity " + this.mStartActivity + " in existing task " + this.mStartActivity.getTaskRecord() + " from source " + this.mSourceRecord);
                }
                return 0;
            }
        }
        if (!(this.mAddingToTask || (this.mLaunchFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) == 0 || (top = sourceTask.findActivityInHistoryLocked(this.mStartActivity)) == null)) {
            TaskRecord task = top.getTaskRecord();
            task.moveActivityToFrontLocked(top);
            top.updateOptionsLocked(this.mOptions);
            ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, task);
            deliverNewIntent(top);
            this.mTargetStack.mLastPausedActivity = null;
            if (this.mDoResume) {
                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
            return 3;
        }
        addOrReparentStartingActivity(sourceTask, "setTaskFromSourceRecord");
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
        }
        return 0;
    }

    private int setTaskFromInTask() {
        if (this.mService.getLockTaskController().isLockTaskModeViolation(this.mInTask)) {
            Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        this.mTargetStack = this.mInTask.getStack();
        ActivityRecord top = this.mInTask.getTopActivity();
        if (top != null && top.mActivityComponent.equals(this.mStartActivity.mActivityComponent) && top.mUserId == this.mStartActivity.mUserId && ((this.mLaunchFlags & 536870912) != 0 || isLaunchModeOneOf(1, 2))) {
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            if ((this.mStartFlags & 1) != 0) {
                return 1;
            }
            deliverNewIntent(top);
            return 3;
        } else if (!this.mAddingToTask) {
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            ActivityOptions.abort(this.mOptions);
            return 2;
        } else {
            if (!this.mLaunchParams.mBounds.isEmpty()) {
                ActivityStack stack = this.mRootActivityContainer.getLaunchStack(null, null, this.mInTask, true);
                if (stack != this.mInTask.getStack()) {
                    this.mInTask.reparent(stack, true, 1, false, true, "inTaskToFront");
                    this.mTargetStack = this.mInTask.getStack();
                }
                updateBounds(this.mInTask, this.mLaunchParams.mBounds);
            }
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            addOrReparentStartingActivity(this.mInTask, "setTaskFromInTask");
            if (!ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                return 0;
            }
            String str = ActivityStackSupervisor.TAG_TASKS;
            Slog.v(str, "Starting new activity " + this.mStartActivity + " in explicit task " + this.mStartActivity.getTaskRecord());
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateBounds(TaskRecord task, Rect bounds) {
        if (!bounds.isEmpty()) {
            ActivityStack stack = task.getStack();
            if (stack == null || !stack.resizeStackWithLaunchBounds()) {
                task.updateOverrideConfiguration(bounds);
            } else {
                this.mService.resizeStack(stack.mStackId, bounds, true, false, true, -1);
            }
        }
    }

    private int setTaskToCurrentTopOrCreateNewTask() {
        this.mTargetStack = computeStackFocus(this.mStartActivity, false, this.mLaunchFlags, this.mOptions);
        if (this.mDoResume) {
            this.mTargetStack.moveToFront("addingToTopTask");
        }
        ActivityRecord prev = this.mTargetStack.getTopActivity();
        if (this.mRestrictedBgActivity && prev == null && handleBackgroundActivityAbort(this.mStartActivity)) {
            return 102;
        }
        TaskRecord task = prev != null ? prev.getTaskRecord() : this.mTargetStack.createTaskRecord(this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.mUserId), this.mStartActivity.info, this.mIntent, null, null, true, this.mStartActivity, this.mSourceRecord, this.mOptions);
        if (this.mRestrictedBgActivity && prev != null && !task.containsAppUid(this.mCallingUid) && handleBackgroundActivityAbort(this.mStartActivity)) {
            return 102;
        }
        addOrReparentStartingActivity(task, "setTaskToCurrentTopOrCreateNewTask");
        this.mTargetStack.positionChildWindowContainerAtTop(task);
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            String str = ActivityStackSupervisor.TAG_TASKS;
            Slog.v(str, "Starting new activity " + this.mStartActivity + " in new guessed " + this.mStartActivity.getTaskRecord());
        }
        return 0;
    }

    private void addOrReparentStartingActivity(TaskRecord parent, String reason) {
        if (this.mStartActivity.getTaskRecord() == null || this.mStartActivity.getTaskRecord() == parent) {
            parent.addActivityToTop(this.mStartActivity);
        } else {
            this.mStartActivity.reparent(parent, parent.mActivities.size(), reason);
        }
    }

    private int adjustLaunchFlagsToDocumentMode(ActivityRecord r, boolean launchSingleInstance, boolean launchSingleTask, int launchFlags) {
        if ((launchFlags & DumpState.DUMP_FROZEN) == 0 || (!launchSingleInstance && !launchSingleTask)) {
            int i = r.info.documentLaunchMode;
            if (i == 0) {
                return launchFlags;
            }
            if (i == 1) {
                return launchFlags | DumpState.DUMP_FROZEN;
            }
            if (i == 2) {
                return launchFlags | DumpState.DUMP_FROZEN;
            }
            if (i != 3) {
                return launchFlags;
            }
            return launchFlags & -134217729;
        }
        Slog.i(TAG, "Ignoring FLAG_ACTIVITY_NEW_DOCUMENT, launchMode is \"singleInstance\" or \"singleTask\"");
        return launchFlags & -134742017;
    }

    private ActivityStack computeStackFocus(ActivityRecord r, boolean newTask, int launchFlags, ActivityOptions aOptions) {
        TaskRecord task = r.getTaskRecord();
        ActivityStack stack = getLaunchStack(r, launchFlags, task, aOptions);
        if (stack != null) {
            return stack;
        }
        ActivityStack currentStack = task != null ? task.getStack() : null;
        ActivityStack focusedStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
        if (currentStack != null) {
            if (focusedStack != currentStack) {
                if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                    String str = TAG_FOCUS;
                    Slog.d(str, "computeStackFocus: Setting focused stack to r=" + r + " task=" + task);
                }
            } else if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                String str2 = TAG_FOCUS;
                Slog.d(str2, "computeStackFocus: Focused stack already=" + focusedStack);
            }
            return currentStack;
        } else if (canLaunchIntoFocusedStack(r, newTask)) {
            if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                String str3 = TAG_FOCUS;
                Slog.d(str3, "computeStackFocus: Have a focused stack=" + focusedStack);
            }
            return focusedStack;
        } else {
            int i = this.mPreferredDisplayId;
            if (i != 0 && (stack = this.mRootActivityContainer.getValidLaunchStackOnDisplay(i, r, aOptions, this.mLaunchParams)) == null) {
                if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                    String str4 = TAG_FOCUS;
                    Slog.d(str4, "computeStackFocus: Can't launch on mPreferredDisplayId=" + this.mPreferredDisplayId + ", looking on all displays.");
                }
                stack = this.mRootActivityContainer.getNextValidLaunchStack(r, this.mPreferredDisplayId);
            }
            if (stack == null) {
                stack = this.mRootActivityContainer.getLaunchStack(r, aOptions, task, true);
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                String str5 = TAG_FOCUS;
                Slog.d(str5, "computeStackFocus: New stack r=" + r + " stackId=" + stack.mStackId);
            }
            return stack;
        }
    }

    private boolean canLaunchIntoFocusedStack(ActivityRecord r, boolean newTask) {
        boolean canUseFocusedStack;
        ActivityStack focusedStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
        if (focusedStack.isActivityTypeAssistant()) {
            canUseFocusedStack = r.isActivityTypeAssistant();
        } else {
            int windowingMode = focusedStack.getWindowingMode();
            if (windowingMode == 1) {
                canUseFocusedStack = true;
            } else if (windowingMode == 3 || windowingMode == 4) {
                canUseFocusedStack = r.supportsSplitScreenWindowingMode();
            } else if (windowingMode != 5) {
                canUseFocusedStack = !focusedStack.isOnHomeDisplay() && r.canBeLaunchedOnDisplay(focusedStack.mDisplayId);
            } else {
                canUseFocusedStack = r.supportsFreeform();
            }
        }
        return canUseFocusedStack && !newTask && this.mPreferredDisplayId == focusedStack.mDisplayId;
    }

    private ActivityStack getLaunchStack(ActivityRecord r, int launchFlags, TaskRecord task, ActivityOptions aOptions) {
        TaskRecord taskRecord = this.mReuseTask;
        if (taskRecord != null) {
            return taskRecord.getStack();
        }
        boolean onTop = true;
        if ((launchFlags & 4096) == 0 || this.mPreferredDisplayId != 0) {
            if (aOptions != null && aOptions.getAvoidMoveToFront()) {
                onTop = false;
            }
            return this.mRootActivityContainer.getLaunchStack(r, aOptions, task, onTop, this.mLaunchParams);
        }
        ActivityStack focusedStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
        ActivityStack parentStack = task != null ? task.getStack() : focusedStack;
        if (parentStack != focusedStack) {
            return parentStack;
        }
        if (focusedStack != null && task == focusedStack.topTask()) {
            return focusedStack;
        }
        if (parentStack == null || !parentStack.inSplitScreenPrimaryWindowingMode()) {
            ActivityStack dockedStack = this.mRootActivityContainer.getDefaultDisplay().getSplitScreenPrimaryStack();
            if (dockedStack == null || dockedStack.shouldBeVisible(r)) {
                return dockedStack;
            }
            return this.mRootActivityContainer.getLaunchStack(r, aOptions, task, true);
        }
        return parentStack.getDisplay().getOrCreateStack(4, this.mRootActivityContainer.resolveActivityType(r, this.mOptions, task), true);
    }

    private boolean isLaunchModeOneOf(int mode1, int mode2) {
        int i = this.mLaunchMode;
        return mode1 == i || mode2 == i;
    }

    static boolean isDocumentLaunchesIntoExisting(int flags) {
        return (524288 & flags) != 0 && (134217728 & flags) == 0;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setIntent(Intent intent) {
        this.mRequest.intent = intent;
        return this;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Intent getIntent() {
        return this.mRequest.intent;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getCallingUid() {
        return this.mRequest.callingUid;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setReason(String reason) {
        this.mRequest.reason = reason;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setCaller(IApplicationThread caller) {
        this.mRequest.caller = caller;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setEphemeralIntent(Intent intent) {
        this.mRequest.ephemeralIntent = intent;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setResolvedType(String type) {
        this.mRequest.resolvedType = type;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setActivityInfo(ActivityInfo info) {
        this.mRequest.activityInfo = info;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setResolveInfo(ResolveInfo info) {
        this.mRequest.resolveInfo = info;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setVoiceSession(IVoiceInteractionSession voiceSession) {
        this.mRequest.voiceSession = voiceSession;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setVoiceInteractor(IVoiceInteractor voiceInteractor) {
        this.mRequest.voiceInteractor = voiceInteractor;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setResultTo(IBinder resultTo) {
        this.mRequest.resultTo = resultTo;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setResultWho(String resultWho) {
        this.mRequest.resultWho = resultWho;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setRequestCode(int requestCode) {
        this.mRequest.requestCode = requestCode;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setCallingPid(int pid) {
        this.mRequest.callingPid = pid;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setCallingUid(int uid) {
        this.mRequest.callingUid = uid;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setCallingPackage(String callingPackage) {
        this.mRequest.callingPackage = callingPackage;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setRealCallingPid(int pid) {
        this.mRequest.realCallingPid = pid;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setRealCallingUid(int uid) {
        this.mRequest.realCallingUid = uid;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setStartFlags(int startFlags) {
        this.mRequest.startFlags = startFlags;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setActivityOptions(SafeActivityOptions options) {
        this.mRequest.activityOptions = options;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setActivityOptions(Bundle bOptions) {
        return setActivityOptions(SafeActivityOptions.fromBundle(bOptions));
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setIgnoreTargetSecurity(boolean ignoreTargetSecurity) {
        this.mRequest.ignoreTargetSecurity = ignoreTargetSecurity;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setFilterCallingUid(int filterCallingUid) {
        this.mRequest.filterCallingUid = filterCallingUid;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setComponentSpecified(boolean componentSpecified) {
        this.mRequest.componentSpecified = componentSpecified;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setOutActivity(ActivityRecord[] outActivity) {
        this.mRequest.outActivity = outActivity;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setInTask(TaskRecord inTask) {
        this.mRequest.inTask = inTask;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setWaitResult(WaitResult result) {
        this.mRequest.waitResult = result;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setProfilerInfo(ProfilerInfo info) {
        this.mRequest.profilerInfo = info;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setGlobalConfiguration(Configuration config) {
        this.mRequest.globalConfig = config;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setUserId(int userId) {
        this.mRequest.userId = userId;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setMayWait(int userId) {
        Request request = this.mRequest;
        request.mayWait = true;
        request.userId = userId;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setAllowPendingRemoteAnimationRegistryLookup(boolean allowLookup) {
        this.mRequest.allowPendingRemoteAnimationRegistryLookup = allowLookup;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setOriginatingPendingIntent(PendingIntentRecord originatingPendingIntent) {
        this.mRequest.originatingPendingIntent = originatingPendingIntent;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setAllowBackgroundActivityStart(boolean allowBackgroundActivityStart) {
        this.mRequest.allowBackgroundActivityStart = allowBackgroundActivityStart;
        return this;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        String prefix2 = prefix + "  ";
        pw.print(prefix2);
        pw.print("mCurrentUser=");
        pw.println(this.mRootActivityContainer.mCurrentUser);
        pw.print(prefix2);
        pw.print("mLastStartReason=");
        pw.println(this.mLastStartReason);
        pw.print(prefix2);
        pw.print("mLastStartActivityTimeMs=");
        pw.println(DateFormat.getDateTimeInstance().format(new Date(this.mLastStartActivityTimeMs)));
        pw.print(prefix2);
        pw.print("mLastStartActivityResult=");
        pw.println(this.mLastStartActivityResult);
        boolean z = false;
        ActivityRecord r = this.mLastStartActivityRecord[0];
        if (r != null) {
            pw.print(prefix2);
            pw.println("mLastStartActivityRecord:");
            r.dump(pw, prefix2 + "  ");
        }
        if (this.mStartActivity != null) {
            pw.print(prefix2);
            pw.println("mStartActivity:");
            this.mStartActivity.dump(pw, prefix2 + "  ");
        }
        if (this.mIntent != null) {
            pw.print(prefix2);
            pw.print("mIntent=");
            pw.println(this.mIntent);
        }
        if (this.mOptions != null) {
            pw.print(prefix2);
            pw.print("mOptions=");
            pw.println(this.mOptions);
        }
        pw.print(prefix2);
        pw.print("mLaunchSingleTop=");
        pw.print(1 == this.mLaunchMode);
        pw.print(" mLaunchSingleInstance=");
        pw.print(3 == this.mLaunchMode);
        pw.print(" mLaunchSingleTask=");
        if (2 == this.mLaunchMode) {
            z = true;
        }
        pw.println(z);
        pw.print(prefix2);
        pw.print("mLaunchFlags=0x");
        pw.print(Integer.toHexString(this.mLaunchFlags));
        pw.print(" mDoResume=");
        pw.print(this.mDoResume);
        pw.print(" mAddingToTask=");
        pw.println(this.mAddingToTask);
    }
}

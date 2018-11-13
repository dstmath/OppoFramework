package com.android.server.am;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager.StackId;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IActivityContainer;
import android.app.IActivityManager.WaitResult;
import android.app.IApplicationThread;
import android.app.KeyguardManager;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.secrecy.SecrecyManagerInternal;
import android.service.voice.IVoiceInteractionSession;
import android.util.EventLog;
import android.util.Slog;
import com.android.internal.app.HeavyWeightSwitcherActivity;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.LocalServices;
import com.android.server.display.DisplayTransformManager;
import com.android.server.oppo.IElsaManager;
import com.android.server.pm.ColorPackageManagerHelper;
import com.android.server.wm.WindowManagerService;
import com.oppo.hypnus.Hypnus;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oppo.util.OppoMultiLauncherUtil;

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
class ActivityStarter {
    private static final int BUFFER_SIZE = 128;
    public static boolean DEBUG_COLOROS_AMS = false;
    private static final String OPPO_RECENTS_PACKAGE_NAME = "com.coloros.recents";
    private static final String PKG_OPPO_BROWSER = "com.android.browser";
    private static final String PKG_SOUGOU_INPUTMETHOD = "com.sohu.inputmethod.sogou";
    private static final String REGEX_PATTERN = "keyword=(.*?)&";
    private static final String SITE_SOUGOU = "https://wisd.sogou.com/web/searchList.jsp";
    private static final String TAG = null;
    private static final String TAG_CONFIGURATION = null;
    private static final String TAG_FOCUS = null;
    private static final String TAG_RESULTS = null;
    private static final String TAG_USER_LEAVING = null;
    private static final boolean USE_DEFAULT_EPHEMERAL_LAUNCHER = false;
    private boolean mAddingToTask;
    private boolean mAvoidMoveToFront;
    private int mCallingUid;
    private boolean mDoResume;
    public Hypnus mHyp;
    private TaskRecord mInTask;
    private Intent mIntent;
    private ActivityStartInterceptor mInterceptor;
    private boolean mKeepCurTransition;
    private Rect mLaunchBounds;
    private int mLaunchFlags;
    private boolean mLaunchSingleInstance;
    private boolean mLaunchSingleTask;
    private boolean mLaunchSingleTop;
    private boolean mLaunchTaskBehind;
    private boolean mMovedOtherTask;
    private boolean mMovedToFront;
    private ActivityInfo mNewTaskInfo;
    private Intent mNewTaskIntent;
    private boolean mNoAnimation;
    private ActivityRecord mNotTop;
    private ActivityOptions mOptions;
    final ArrayList<PendingActivityLaunch> mPendingActivityLaunches;
    private boolean mPowerHintSent;
    private TaskRecord mReuseTask;
    private ActivityRecord mReusedActivity;
    SecrecyManagerInternal mSecrecyManagerInternal;
    private final ActivityManagerService mService;
    private ActivityRecord mSourceRecord;
    private ActivityStack mSourceStack;
    private ActivityRecord mStartActivity;
    private int mStartFlags;
    private final ActivityStackSupervisor mSupervisor;
    private ActivityStack mTargetStack;
    private IVoiceInteractor mVoiceInteractor;
    private IVoiceInteractionSession mVoiceSession;
    private WindowManagerService mWindowManager;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.ActivityStarter.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.am.ActivityStarter.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityStarter.<clinit>():void");
    }

    private void reset() {
        this.mStartActivity = null;
        this.mIntent = null;
        this.mCallingUid = -1;
        this.mOptions = null;
        this.mLaunchSingleTop = false;
        this.mLaunchSingleInstance = false;
        this.mLaunchSingleTask = false;
        this.mLaunchTaskBehind = false;
        this.mLaunchFlags = 0;
        this.mLaunchBounds = null;
        this.mNotTop = null;
        this.mDoResume = false;
        this.mStartFlags = 0;
        this.mSourceRecord = null;
        this.mInTask = null;
        this.mAddingToTask = false;
        this.mReuseTask = null;
        this.mNewTaskInfo = null;
        this.mNewTaskIntent = null;
        this.mSourceStack = null;
        this.mTargetStack = null;
        this.mMovedOtherTask = false;
        this.mMovedToFront = false;
        this.mNoAnimation = false;
        this.mKeepCurTransition = false;
        this.mAvoidMoveToFront = false;
        this.mVoiceSession = null;
        this.mVoiceInteractor = null;
    }

    ActivityStarter(ActivityManagerService service, ActivityStackSupervisor supervisor) {
        this.mPendingActivityLaunches = new ArrayList();
        this.mHyp = null;
        this.mService = service;
        this.mSupervisor = supervisor;
        this.mInterceptor = new ActivityStartInterceptor(this.mService, this.mSupervisor);
    }

    /* JADX WARNING: Missing block: B:281:0x0837, code:
            if (r58.endsWith("plugin.accountsync.ui.ContactsSyncUI") != false) goto L_0x0839;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "ZhiYong.Lin@Plf.Framework, modify for secure protectWangLan@Plf.Framework, modify for ROM Data Collection", property = OppoRomType.ROM)
    final int startActivityLocked(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, ActivityContainer container, TaskRecord inTask) {
        int err;
        String str;
        StringBuilder append;
        int i;
        if (this.mHyp == null) {
            this.mHyp = new Hypnus();
        }
        if (this.mHyp != null) {
            this.mHyp.hypnusSetAction(11, 500);
        }
        int err2 = 0;
        if (OppoAmsUtils.getInstance(this.mService).needToControlActivityStartFreq(intent)) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                Slog.i(TAG, "control start frequency {" + intent.toShortString(true, true, true, false) + "}");
            }
            err2 = -15;
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "startActivityLocked callingPid " + callingPid);
        }
        ProcessRecord callerApp = null;
        if (caller != null) {
            callerApp = this.mService.getRecordForAppLocked(caller);
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.v(TAG, "startActivityLocked callerApp " + callerApp);
            }
            if (callerApp != null) {
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.v(TAG, "startActivityLocked callerApp.pid " + callerApp.pid);
                }
                callingPid = callerApp.pid;
                callingUid = callerApp.info.uid;
            } else {
                Slog.w(TAG, "Unable to find app for caller " + caller + " (pid=" + callingPid + ") when starting: " + intent.toString());
                err2 = -4;
            }
        }
        int userId = aInfo != null ? UserHandle.getUserId(aInfo.applicationInfo.uid) : 0;
        if (intent.getData() != null) {
            Uri uri = intent.getData();
            String pkgNameInUri = IElsaManager.EMPTY_PACKAGE;
            try {
                pkgNameInUri = uri.toString().split(":")[1];
            } catch (IndexOutOfBoundsException e) {
            }
            if (ActivityManagerService.DEBUG_COLOROS_AMS) {
                Slog.v(TAG, "pkgName in Uri: " + pkgNameInUri + "callingPkg: " + callingPackage);
            }
            if (OppoProcessWhiteListUtils.getInstance().getProcessWhiteList().contains(pkgNameInUri) && callingUid != 1000) {
                err2 = -4;
            }
        }
        try {
            ComponentName currentComponent = intent.getComponent();
            ComponentName componentName = new ComponentName("com.google.android.packageinstaller", "com.android.packageinstaller.UninstallerActivity");
            componentName = new ComponentName("com.android.packageinstaller", "com.android.packageinstaller.UninstallerActivity");
            if (componentName.equals(currentComponent) || componentName.equals(currentComponent)) {
                Uri packageUri = intent.getData();
                String packageName = null;
                if (packageUri != null) {
                    packageName = packageUri.getEncodedSchemeSpecificPart();
                }
                if (packageName != null && ColorPackageManagerHelper.isForbiddenUninstallApp(AppGlobals.getPackageManager(), packageName, callingUid)) {
                    if (ActivityManagerDebugConfig.DEBUG_PERMISSION) {
                        Slog.d(TAG, "prevent delete hide package:" + packageName + ",callingPackage:" + callingPackage);
                    }
                    ColorPackageManagerHelper.sendDcsPreventUninstallSystemApp(this.mService.mContext, callingPackage, packageName);
                    err2 = 4;
                }
            }
        } catch (Throwable e2) {
            Slog.w(TAG, "Failed to forbidden uninstall Activity", e2);
        }
        if (OppoAbnormalAppManager.getInstance().validStartActivity(aInfo)) {
            Slog.i(OppoAbnormalAppManager.TAG, "UL for activity " + aInfo + " : is R");
            err2 = 4;
        }
        try {
            String className = intent.getComponent().getClassName();
            if ("com.android.packageinstaller.PackageInstallerActivity".equals(className) && AppGlobals.getPackageManager().prohibitChildInstallation(userId, true)) {
                err2 = 4;
            } else if ("com.android.packageinstaller.UninstallerActivity".equals(className) && AppGlobals.getPackageManager().prohibitChildInstallation(userId, false)) {
                err2 = 4;
            }
            err = err2;
        } catch (Throwable re) {
            throw new RuntimeException("Package manager has died", re);
        } catch (Throwable e22) {
            Slog.w(TAG, "failed for check prohibitChildInstallation", e22);
            err = err2;
        }
        if (err == 4) {
            err2 = err;
        } else if (OppoAppStartupManager.getInstance().handleStartActivity(intent, aInfo, callingPackage, callingUid, callingPid, userId)) {
            err2 = 4;
        } else {
            err2 = err;
        }
        if (!(aInfo == null || aInfo.packageName == null)) {
            ColorPackageManagerHelper.removePkgFromNotLaunchedList(aInfo.packageName, true);
        }
        if (this.mSecrecyManagerInternal == null) {
            this.mSecrecyManagerInternal = (SecrecyManagerInternal) LocalServices.getService(SecrecyManagerInternal.class);
        }
        if (this.mSecrecyManagerInternal != null && this.mSecrecyManagerInternal.isInEncryptedAppList(aInfo, callingPackage, callingUid, callingPid)) {
            Slog.e(TAG, aInfo + " is isInEncryptedAppList ");
            err2 = 4;
        }
        if (err2 == 0) {
            str = TAG;
            append = new StringBuilder().append("START u").append(userId).append(" {").append(intent.toShortString(true, true, true, false)).append("} from uid ").append(callingUid).append(" and from pid ").append(callingPid).append(" on display ");
            i = container == null ? this.mSupervisor.mFocusedStack == null ? 0 : this.mSupervisor.mFocusedStack.mDisplayId : container.mActivityDisplay == null ? 0 : container.mActivityDisplay.mDisplayId;
            Slog.i(str, append.append(i).toString());
            this.mSupervisor.dataCollectionInfo(caller, callerApp, callingPid, callingUid, callingPackage, intent);
            if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US")) {
                this.mSupervisor.dataCollectionInfoExp(callerApp, callingPackage, intent);
            }
            this.mSupervisor.collectionStartUrlInfo(caller, callerApp, callingPid, callingUid, callingPackage, intent);
        }
        ActivityRecord sourceRecord = null;
        ActivityRecord resultRecord = null;
        String tempCls = null;
        if (!(intent == null || intent.getComponent() == null || intent.getComponent().getClassName() == null)) {
            tempCls = intent.getComponent().getClassName().trim();
        }
        if (tempCls != null) {
            if (tempCls.endsWith("ChooserActivity") && callingUid == 1000) {
                sourceRecord = this.mSupervisor.mOppoSecureProtectUtils.getValue();
                if (sourceRecord != null) {
                    resultRecord = sourceRecord.resultTo;
                    resultWho = sourceRecord.resultWho;
                    requestCode = sourceRecord.requestCode;
                }
            }
        }
        if (resultTo != null) {
            sourceRecord = this.mSupervisor.isInAnyStackLocked(resultTo);
            if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
                Slog.v(TAG_RESULTS, "Will send result to " + resultTo + " " + sourceRecord);
            }
            if (!(sourceRecord == null || requestCode < 0 || sourceRecord.finishing)) {
                resultRecord = sourceRecord;
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "startActivityLocked resultTo " + resultTo + " sourceRecord " + sourceRecord);
        }
        int launchFlags = intent.getFlags();
        if (!((33554432 & launchFlags) == 0 || sourceRecord == null)) {
            if (requestCode >= 0) {
                ActivityOptions.abort(options);
                return -3;
            }
            resultRecord = sourceRecord.resultTo;
            if (!(resultRecord == null || resultRecord.isInStackLocked())) {
                resultRecord = null;
            }
            resultWho = sourceRecord.resultWho;
            requestCode = sourceRecord.requestCode;
            sourceRecord.resultTo = null;
            if (resultRecord != null) {
                resultRecord.removeResultsLocked(sourceRecord, resultWho, requestCode);
            }
            if (sourceRecord.launchedFromUid == callingUid) {
                callingPackage = sourceRecord.launchedFromPackage;
            }
        }
        if (err2 == 0 && intent.getComponent() == null) {
            Slog.w(TAG, "We couldn't find a class that intent.getComponent() is null");
            err2 = -1;
        }
        if (err2 == 0 && aInfo == null) {
            Slog.w(TAG, "We couldn't find a class that aInfo is null");
            err2 = -2;
        }
        if (!(err2 != 0 || sourceRecord == null || sourceRecord.task.voiceSession == null || (268435456 & launchFlags) != 0 || sourceRecord.info.applicationInfo.uid == aInfo.applicationInfo.uid)) {
            try {
                intent.addCategory("android.intent.category.VOICE");
                if (!AppGlobals.getPackageManager().activitySupportsIntent(intent.getComponent(), intent, resolvedType)) {
                    Slog.w(TAG, "Activity being started in current voice task does not support voice: " + intent);
                    err2 = -7;
                }
            } catch (Throwable e3) {
                Slog.w(TAG, "Failure checking voice capabilities", e3);
                err2 = -7;
            }
        }
        if (err2 == 0 && voiceSession != null) {
            try {
                if (!AppGlobals.getPackageManager().activitySupportsIntent(intent.getComponent(), intent, resolvedType)) {
                    Slog.w(TAG, "Activity being started in new voice task does not support: " + intent);
                    err2 = -7;
                }
            } catch (Throwable e32) {
                Slog.w(TAG, "Failure checking voice capabilities", e32);
                err2 = -7;
            }
        }
        ActivityStack resultStack = resultRecord == null ? null : resultRecord.task.stack;
        if (err2 != 0) {
            if (resultRecord != null) {
                resultStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
            }
            ActivityOptions.abort(options);
            return err2;
        }
        boolean abort = (!this.mSupervisor.checkStartAnyActivityPermission(intent, aInfo, resultWho, requestCode, callingPid, callingUid, callingPackage, ignoreTargetSecurity, callerApp, resultRecord, resultStack, options)) | (this.mService.mIntentFirewall.checkStartActivity(intent, callingUid, callingPid, resolvedType, aInfo.applicationInfo) ? 0 : 1);
        if (!(aInfo == null || aInfo.applicationInfo == null || aInfo.applicationInfo.packageName == null)) {
            if (ActivityManagerService.OPPO_LAUNCHER.equals(aInfo.applicationInfo.packageName) && callingUid != 1000 && OppoSplitWindowAppReader.isInTwoSecond()) {
                Slog.d(TAG, "back_key start home app callingpkg: " + callingPackage);
                this.mService.mHandler.sendMessageDelayed(this.mService.mHandler.obtainMessage(511, UserHandle.getUserId(callingUid), -1, callingPackage), 1000);
            }
        }
        if (!(aInfo == null || aInfo.applicationInfo == null || aInfo.applicationInfo.packageName == null)) {
            String pName = aInfo.applicationInfo.packageName;
            if (!(ActivityManagerService.OPPO_LAUNCHER.equals(callingPackage) || "android".equals(callingPackage) || "com.coloros.safecenter".equals(callingPackage) || OPPO_RECENTS_PACKAGE_NAME.equals(callingPackage) || ((intent != null && intent.getIsFromGameSpace() == 1) || OppoAppStartupManager.getInstance().isTenIntencept(callingPackage, intent) || !OppoMultiLauncherUtil.getInstance().isMultiApp(pName) || pName.equals(callingPackage)))) {
                String tempClass = null;
                if (!(intent == null || intent.getComponent() == null || intent.getComponent().getClassName() == null)) {
                    tempClass = intent.getComponent().getClassName().trim();
                }
                if (tempClass != null) {
                    if (!tempClass.endsWith("wxapi.WXEntryActivity")) {
                        if (!tempClass.endsWith("QQCallbackUI")) {
                        }
                    }
                    Slog.v(TAG, "multi app: startLocked: callback continue");
                }
                this.mService.mHandler.sendMessageAtFrontOfQueue(this.mService.mHandler.obtainMessage(500, intent));
                if (resultRecord != null) {
                    this.mSupervisor.mOppoSecureProtectUtils.setTempValue(resultRecord, resultWho, requestCode);
                }
                return 0;
            }
            if (!(!"android".equals(callingPackage) || intent == null || (intent.getFlags() & 512) == 0)) {
                aInfo.taskAffinity = "coloros_multiapp_chooser";
            }
        }
        if (this.mService.mController != null) {
            try {
                String pkgName = aInfo.applicationInfo.packageName;
                if (pkgName != null && OppoMultiLauncherUtil.getInstance().isMultiApp(pkgName)) {
                    Slog.d(TAG, "multi app: putExtra userId = " + userId + "   pkgName = " + pkgName);
                    intent.setOppoUserId(userId);
                }
                if (this.mService.mOppoActivityControlerScheduler != null) {
                    if (this.mService.mOppoActivityControlerScheduler.scheduleActivityStarting(intent, aInfo.applicationInfo.packageName)) {
                        i = 0;
                    } else {
                        i = 1;
                    }
                    abort |= i;
                } else if (OppoGameSpaceManager.getInstance().handleVideoComingNotification(intent, aInfo)) {
                    abort |= true;
                } else {
                    abort |= this.mService.mController.activityStarting(intent, aInfo.applicationInfo.packageName) ? 0 : 1;
                }
            } catch (RemoteException e4) {
                this.mService.mController = null;
            }
        }
        err2 = realCallingPid;
        this.mInterceptor.setStates(userId, err2, realCallingUid, startFlags, callingPackage);
        this.mInterceptor.intercept(intent, rInfo, aInfo, resolvedType, inTask, callingPid, callingUid, options);
        intent = this.mInterceptor.mIntent;
        rInfo = this.mInterceptor.mRInfo;
        aInfo = this.mInterceptor.mAInfo;
        resolvedType = this.mInterceptor.mResolvedType;
        inTask = this.mInterceptor.mInTask;
        callingPid = this.mInterceptor.mCallingPid;
        callingUid = this.mInterceptor.mCallingUid;
        options = this.mInterceptor.mActivityOptions;
        if (abort) {
            if (resultRecord != null && this.mSupervisor.mOppoSecureProtectUtils.isNeedReplaceActivityRequest(resultRecord, resultWho, requestCode)) {
                this.mSupervisor.mOppoSecureProtectUtils.setTempValue(resultRecord, resultWho, requestCode);
            }
            ActivityOptions.abort(options);
            return 0;
        }
        if (Build.isPermissionReviewRequired() && aInfo != null && this.mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(aInfo.packageName, userId)) {
            ActivityManagerService activityManagerService = this.mService;
            Intent[] intentArr = new Intent[1];
            intentArr[0] = intent;
            String[] strArr = new String[1];
            strArr[0] = resolvedType;
            IIntentSender target = activityManagerService.getIntentSenderLocked(2, callingPackage, callingUid, userId, null, null, 0, intentArr, strArr, 1342177280, null);
            int flags = intent.getFlags();
            Intent intent2 = new Intent("android.intent.action.REVIEW_PERMISSIONS");
            intent2.setFlags(8388608 | flags);
            intent2.putExtra("android.intent.extra.PACKAGE_NAME", aInfo.packageName);
            intent2.putExtra("android.intent.extra.INTENT", new IntentSender(target));
            if (resultRecord != null) {
                intent2.putExtra("android.intent.extra.RESULT_NEEDED", true);
            }
            intent = intent2;
            resolvedType = null;
            callingUid = realCallingUid;
            callingPid = realCallingPid;
            rInfo = this.mSupervisor.resolveIntent(intent2, null, userId);
            aInfo = this.mSupervisor.resolveActivity(intent2, rInfo, startFlags, null);
            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW || !ActivityManagerService.IS_USER_BUILD) {
                str = TAG;
                append = new StringBuilder().append("START u").append(userId).append(" {").append(intent2.toShortString(true, true, true, false)).append("} from uid ").append(realCallingUid).append(" on display ");
                i = container == null ? this.mSupervisor.mFocusedStack == null ? 0 : this.mSupervisor.mFocusedStack.mDisplayId : container.mActivityDisplay == null ? 0 : container.mActivityDisplay.mDisplayId;
                Slog.i(str, append.append(i).toString());
            }
        }
        if (!(rInfo == null || rInfo.ephemeralResolveInfo == null)) {
            err2 = ephemeralIntent;
            intent = buildEphemeralInstallerIntent(intent, err2, rInfo.ephemeralResolveInfo.getPackageName(), callingPackage, resolvedType, userId);
            resolvedType = null;
            callingUid = realCallingUid;
            callingPid = realCallingPid;
            aInfo = this.mSupervisor.resolveActivity(intent, rInfo, startFlags, null);
        }
        ActivityRecord r = this.mSupervisor.mOppoSecureProtectUtils.handleStartActivityLocked(this.mService, callerApp, callingUid, callingPackage, intent, resolvedType, aInfo, resultRecord, resultWho, requestCode, componentSpecified, voiceSession != null, this.mSupervisor, container, options, sourceRecord);
        if (outActivity != null) {
            outActivity[0] = r;
        }
        if (r.appTimeTracker == null && sourceRecord != null) {
            r.appTimeTracker = sourceRecord.appTimeTracker;
        }
        ActivityStack stack = this.mSupervisor.mFocusedStack;
        if (voiceSession == null && ((stack.mResumedActivity == null || stack.mResumedActivity.info.applicationInfo.uid != callingUid) && !OppoAppStartupManager.getInstance().isFromControlCenterPkg(callingPackage))) {
            err2 = callingUid;
            if (!this.mService.checkAppSwitchAllowedLocked(callingPid, err2, realCallingPid, realCallingUid, "Activity start")) {
                this.mPendingActivityLaunches.add(new PendingActivityLaunch(r, sourceRecord, startFlags, stack, callerApp));
                ActivityOptions.abort(options);
                return 4;
            }
        }
        if (this.mService.mDidAppSwitch) {
            this.mService.mAppSwitchesAllowedTime = 0;
        } else {
            this.mService.mDidAppSwitch = true;
        }
        doPendingActivityLaunchesLocked(false);
        try {
            this.mService.mWindowManager.deferSurfaceLayout();
            err2 = startActivityUnchecked(r, sourceRecord, voiceSession, voiceInteractor, startFlags, true, options, inTask);
            postStartActivityUncheckedProcessing(r, err2, stack.mStackId, this.mSourceRecord, this.mTargetStack);
            return err2;
        } finally {
            this.mService.mWindowManager.continueSurfaceLayout();
        }
    }

    private Intent buildEphemeralInstallerIntent(Intent launchIntent, Intent origIntent, String ephemeralPackage, String callingPackage, String resolvedType, int userId) {
        Intent intent = new Intent(origIntent);
        intent.setFlags(intent.getFlags() | 512);
        ActivityManagerService activityManagerService = this.mService;
        int callingUid = Binder.getCallingUid();
        Intent[] intentArr = new Intent[1];
        intentArr[0] = intent;
        String[] strArr = new String[1];
        strArr[0] = resolvedType;
        IIntentSender failureIntentTarget = activityManagerService.getIntentSenderLocked(2, callingPackage, callingUid, userId, null, null, 1, intentArr, strArr, 1409286144, null);
        Intent ephemeralIntent = new Intent(launchIntent);
        activityManagerService = this.mService;
        callingUid = Binder.getCallingUid();
        intentArr = new Intent[1];
        intentArr[0] = ephemeralIntent;
        strArr = new String[1];
        strArr[0] = resolvedType;
        IIntentSender successIntentTarget = activityManagerService.getIntentSenderLocked(2, callingPackage, callingUid, userId, null, null, 0, intentArr, strArr, 1409286144, null);
        int flags = launchIntent.getFlags();
        Intent intent2 = new Intent();
        intent2.setFlags((((268435456 | flags) | 32768) | 1073741824) | 8388608);
        intent2.putExtra("android.intent.extra.PACKAGE_NAME", ephemeralPackage);
        intent2.putExtra("android.intent.extra.EPHEMERAL_FAILURE", new IntentSender(failureIntentTarget));
        intent = intent2;
        intent.putExtra("android.intent.extra.EPHEMERAL_SUCCESS", new IntentSender(successIntentTarget));
        intent2.setData(origIntent.getData().buildUpon().clearQuery().build());
        return intent2;
    }

    void postStartActivityUncheckedProcessing(ActivityRecord r, int result, int prevFocusedStackId, ActivityRecord sourceRecord, ActivityStack targetStack) {
        ActivityRecord topActivityHomeStack = null;
        if (result < 0) {
            this.mSupervisor.notifyActivityDrawnForKeyguard();
            return;
        }
        if (result == 2 && !this.mSupervisor.mWaitingActivityLaunched.isEmpty()) {
            this.mSupervisor.reportTaskToFrontNoLaunch(this.mStartActivity);
        }
        int startedActivityStackId = -1;
        if (r.task != null && r.task.stack != null) {
            startedActivityStackId = r.task.stack.mStackId;
        } else if (this.mTargetStack != null) {
            startedActivityStackId = targetStack.mStackId;
        }
        boolean noDisplayActivityOverHome = (sourceRecord == null || !sourceRecord.noDisplay) ? false : sourceRecord.task.getTaskToReturnTo() == 1;
        if (startedActivityStackId == 3 && (prevFocusedStackId == 0 || noDisplayActivityOverHome)) {
            ActivityStack homeStack = this.mSupervisor.getStack(0);
            if (homeStack != null) {
                topActivityHomeStack = homeStack.topRunningActivityLocked();
            }
            if (topActivityHomeStack == null || topActivityHomeStack.mActivityType != 2) {
                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(TAG, "Scheduling recents launch.");
                }
                this.mWindowManager.showRecentApps(true);
                return;
            }
        }
        if (startedActivityStackId == 4 && (result == 2 || result == 3)) {
            this.mService.notifyPinnedActivityRestartAttemptLocked();
        }
    }

    void startHomeActivityLocked(Intent intent, ActivityInfo aInfo, String reason) {
        this.mSupervisor.moveHomeStackTaskToTop(1, reason);
        startActivityLocked(null, intent, null, null, aInfo, null, null, null, null, null, 0, 0, 0, null, 0, 0, 0, null, false, false, null, null, null);
        if (this.mSupervisor.inResumeTopActivity) {
            this.mSupervisor.scheduleResumeTopActivities();
        }
    }

    void showConfirmDeviceCredential(int userId) {
        ActivityStack targetStack;
        ActivityStack fullscreenStack = this.mSupervisor.getStack(1);
        ActivityStack freeformStack = this.mSupervisor.getStack(2);
        if (fullscreenStack != null && fullscreenStack.getStackVisibilityLocked(null) != 0) {
            targetStack = fullscreenStack;
        } else if (freeformStack == null || freeformStack.getStackVisibilityLocked(null) == 0) {
            targetStack = this.mSupervisor.getStack(0);
        } else {
            targetStack = freeformStack;
        }
        if (targetStack != null) {
            Intent credential = ((KeyguardManager) this.mService.mContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, userId);
            if (credential != null) {
                ActivityRecord activityRecord = targetStack.topRunningActivityLocked();
                if (activityRecord != null) {
                    ActivityManagerService activityManagerService = this.mService;
                    String str = activityRecord.launchedFromPackage;
                    int i = activityRecord.launchedFromUid;
                    int i2 = activityRecord.userId;
                    Intent[] intentArr = new Intent[1];
                    intentArr[0] = activityRecord.intent;
                    String[] strArr = new String[1];
                    strArr[0] = activityRecord.resolvedType;
                    credential.putExtra("android.intent.extra.INTENT", new IntentSender(activityManagerService.getIntentSenderLocked(2, str, i, i2, null, null, 0, intentArr, strArr, 1409286144, null)));
                    startConfirmCredentialIntent(credential);
                }
            }
        }
    }

    void startConfirmCredentialIntent(Intent intent) {
        intent.addFlags(276840448);
        ActivityOptions options = ActivityOptions.makeBasic();
        options.setLaunchTaskId(this.mSupervisor.getHomeActivity().task.taskId);
        this.mService.mContext.startActivityAsUser(intent, options.toBundle(), UserHandle.CURRENT);
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00f9  */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x0578 A:{Catch:{ all -> 0x0814 }} */
    /* JADX WARNING: Removed duplicated region for block: B:196:0x05a3 A:{Catch:{ all -> 0x0814 }} */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x0803 A:{Catch:{ all -> 0x0814 }} */
    /* JADX WARNING: Removed duplicated region for block: B:218:0x0616 A:{Catch:{ all -> 0x0814 }} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x013c  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x011f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final int startActivityMayWait(IApplicationThread caller, int callingUid, String callingPackage, Intent intent, String resolvedType, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, WaitResult outResult, Configuration config, Bundle bOptions, boolean ignoreTargetSecurity, int userId, IActivityContainer iContainer, TaskRecord inTask) {
        Throwable th;
        if (intent == null || !intent.hasFileDescriptors()) {
            ResolveInfo rInfo;
            ActivityInfo aInfo;
            this.mSupervisor.mActivityMetricsLogger.notifyActivityLaunching();
            boolean componentSpecified = intent.getComponent() != null;
            Intent intent2 = new Intent(intent);
            Intent intent3 = new Intent(intent);
            if ((intent3.getFlags() & 2048) != 0) {
                Slog.w(TAG, "startActivityMayWait mService.mIgnoreSleepCheckLater " + this.mService.mIgnoreSleepCheckLater);
                if (this.mService.mIgnoreSleepCheckLater) {
                    this.mService.mHandler.removeMessages(DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR);
                } else {
                    this.mService.mIgnoreSleepCheckLater = true;
                }
                this.mService.mHandler.sendMessageDelayed(this.mService.mHandler.obtainMessage(DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR), 1000);
            }
            if (intent3.getCategories() != null && intent3.getCategories().contains("com.multiple.launcher")) {
                intent3.removeCategory("com.multiple.launcher");
                userId = OppoMultiAppManager.USER_ID;
            }
            ResolveInfo rInfo2 = this.mSupervisor.resolveIntent(intent3, resolvedType, userId);
            if (rInfo2 == null) {
                UserInfo userInfo = this.mSupervisor.getUserInfo(userId);
                if (userInfo == null) {
                    rInfo = rInfo2;
                } else if (userInfo.isManagedProfile()) {
                    UserManager userManager = UserManager.get(this.mService.mContext);
                    long token = Binder.clearCallingIdentity();
                    try {
                        boolean profileLockedAndParentUnlockingOrUnlocked;
                        UserInfo parent = userManager.getProfileParent(userId);
                        if (parent != null) {
                            if (userManager.isUserUnlockingOrUnlocked(parent.id)) {
                                profileLockedAndParentUnlockingOrUnlocked = !userManager.isUserUnlockingOrUnlocked(userId);
                                Binder.restoreCallingIdentity(token);
                                if (profileLockedAndParentUnlockingOrUnlocked) {
                                    rInfo = this.mSupervisor.resolveIntent(intent3, resolvedType, userId, 786432);
                                }
                            }
                        }
                        profileLockedAndParentUnlockingOrUnlocked = false;
                        Binder.restoreCallingIdentity(token);
                        if (profileLockedAndParentUnlockingOrUnlocked) {
                        }
                    } catch (Throwable th2) {
                        Binder.restoreCallingIdentity(token);
                    }
                }
                aInfo = this.mSupervisor.resolveActivity(intent3, rInfo, startFlags, profilerInfo);
                if (!disAllowActivityStartForCustomizeProject(aInfo)) {
                    return 0;
                }
                ActivityInfo aInfo2;
                if (userId != 999 || callingPackage == null) {
                    aInfo2 = aInfo;
                } else if (OppoMultiLauncherUtil.getInstance().isMultiApp(callingPackage) && aInfo == null) {
                    userId = 0;
                    aInfo = this.mSupervisor.resolveActivity(intent3, resolvedType, startFlags, profilerInfo, 0);
                    Slog.i(TAG, "multi app: startActivityMayWait change userId to " + 0 + "  aInfo = " + aInfo);
                    aInfo2 = aInfo;
                } else {
                    aInfo2 = aInfo;
                }
                if (!(userId != 999 || aInfo2 == null || aInfo2.applicationInfo == null)) {
                    if (!OppoMultiLauncherUtil.getInstance().isMultiApp(aInfo2.applicationInfo.packageName)) {
                        userId = this.mService.mUserController.getCurrentUserIdLocked();
                        aInfo2.applicationInfo.uid = UserHandle.getUid(userId, aInfo2.applicationInfo.uid);
                    }
                }
                if (!(aInfo2 == null || aInfo2.applicationInfo == null)) {
                    String pName = aInfo2.applicationInfo.packageName;
                    if (aInfo2.hasResizeModeInit) {
                        aInfo2.resizeMode = aInfo2.resizeModeOriginal;
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            Slog.v(TAG, "split app: " + pName + " resizeMode: " + aInfo2.resizeModeOriginal);
                        }
                    } else {
                        aInfo2.hasResizeModeInit = true;
                        aInfo2.resizeModeOriginal = aInfo2.resizeMode;
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            Slog.v(TAG, "split app2: " + pName + " resizeMode: " + aInfo2.resizeModeOriginal);
                        }
                    }
                    if (OppoSplitWindowAppReader.getInstance().isInBlackList(pName)) {
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            Slog.v(TAG, "split app: " + pName + " in blacklist");
                        }
                        aInfo2.resizeMode = 0;
                    } else if (aInfo2.resizeMode == 4 && !OppoSplitWindowAppReader.getInstance().isInConfigList(pName)) {
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            Slog.v(TAG, "split app: " + pName + " not in configList");
                        }
                        aInfo2.resizeMode = 0;
                    } else if (aInfo2.resizeMode == 0 && OppoSplitWindowAppReader.getInstance().isInConfigList(pName)) {
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            Slog.v(TAG, "not split app: " + pName + " in configlist");
                        }
                        aInfo2.resizeMode = 4;
                    }
                }
                ActivityOptions options = ActivityOptions.fromBundle(bOptions);
                ActivityContainer container = (ActivityContainer) iContainer;
                synchronized (this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        if (container == null || container.mParentActivity == null || container.mParentActivity.state == ActivityState.RESUMED) {
                            int callingPid;
                            ActivityStack stack;
                            ActivityRecord[] outRecord;
                            int res;
                            int realCallingPid = Binder.getCallingPid();
                            int realCallingUid = Binder.getCallingUid();
                            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                Slog.v(TAG, "startActivityMayWait callingUid " + callingUid + " caller " + caller + " realCallingPid " + realCallingPid + " realCallingUid " + realCallingUid);
                            }
                            if (callingUid >= 0) {
                                callingPid = -1;
                            } else if (caller == null) {
                                callingPid = realCallingPid;
                                callingUid = realCallingUid;
                            } else {
                                callingUid = -1;
                                callingPid = -1;
                            }
                            if (container == null || container.mStack.isOnHomeDisplay()) {
                                stack = this.mSupervisor.mFocusedStack;
                            } else {
                                stack = container.mStack;
                            }
                            boolean z = (config == null || this.mService.mConfiguration.diff(config) == 0) ? false : true;
                            stack.mConfigWillChange = z;
                            if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                Slog.v(TAG_CONFIGURATION, "Starting activity when config will change = " + stack.mConfigWillChange);
                            }
                            long origId = Binder.clearCallingIdentity();
                            if (PKG_SOUGOU_INPUTMETHOD.equals(callingPackage) && stack != null) {
                                ActivityRecord ar = stack.topRunningActivityLocked();
                                String keyword = null;
                                if (ar != null && PKG_OPPO_BROWSER.equals(ar.packageName)) {
                                    Uri uri = intent3.getData();
                                    if (uri != null) {
                                        String site = uri.getScheme() + "://" + uri.getHost() + uri.getPath();
                                        Slog.i(TAG, "site::" + site);
                                        List<String> sites = OppoAppStartupManagerUtils.getInstance().getSougouSite();
                                        if (sites != null && sites.isEmpty()) {
                                            sites.add(SITE_SOUGOU);
                                        }
                                        for (String webSite : sites) {
                                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                                Slog.i(TAG, "site in sites::" + webSite);
                                            }
                                        }
                                        if (sites != null && sites.contains(site)) {
                                            String key = uri.getQuery();
                                            if (key != null) {
                                                Matcher m = Pattern.compile(REGEX_PATTERN).matcher(key);
                                                if (m.find()) {
                                                    keyword = m.group(1);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (keyword != null) {
                                    this.mService.mHandler.sendMessageAtFrontOfQueue(this.mService.mHandler.obtainMessage(700, keyword));
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return 0;
                                }
                            }
                            if (aInfo2 != null) {
                                if ((aInfo2.applicationInfo.privateFlags & 2) != 0) {
                                    if (aInfo2.processName.equals(aInfo2.applicationInfo.packageName)) {
                                        ProcessRecord heavy = this.mService.mHeavyWeightProcess;
                                        if (heavy == null || (heavy.info.uid == aInfo2.applicationInfo.uid && heavy.processName.equals(aInfo2.processName))) {
                                            aInfo = aInfo2;
                                            rInfo2 = rInfo;
                                            intent = intent3;
                                        } else {
                                            int appCallingUid = callingUid;
                                            if (caller != null) {
                                                ProcessRecord callerApp = this.mService.getRecordForAppLocked(caller);
                                                if (callerApp != null) {
                                                    appCallingUid = callerApp.info.uid;
                                                } else {
                                                    Slog.w(TAG, "Unable to find app for caller " + caller + " (pid=" + callingPid + ") when starting: " + intent3.toString());
                                                    ActivityOptions.abort(options);
                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                    return -4;
                                                }
                                            }
                                            Intent[] intentArr = new Intent[1];
                                            intentArr[0] = intent3;
                                            String[] strArr = new String[1];
                                            strArr[0] = resolvedType;
                                            IIntentSender target = this.mService.getIntentSenderLocked(2, "android", appCallingUid, userId, null, null, 0, intentArr, strArr, 1342177280, null);
                                            Intent newIntent = new Intent();
                                            if (requestCode >= 0) {
                                                newIntent.putExtra("has_result", true);
                                            }
                                            newIntent.putExtra("intent", new IntentSender(target));
                                            if (heavy.activities.size() > 0) {
                                                ActivityRecord hist = (ActivityRecord) heavy.activities.get(0);
                                                newIntent.putExtra("cur_app", hist.packageName);
                                                newIntent.putExtra("cur_task", hist.task.taskId);
                                            }
                                            newIntent.putExtra("new_app", aInfo2.packageName);
                                            newIntent.setFlags(intent3.getFlags());
                                            newIntent.setClassName("android", HeavyWeightSwitcherActivity.class.getName());
                                            intent = newIntent;
                                            resolvedType = null;
                                            caller = null;
                                            try {
                                                callingUid = Binder.getCallingUid();
                                                callingPid = Binder.getCallingPid();
                                                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                                    Slog.v(TAG, "startActivityMayWait callingUid " + callingUid + " callingPid " + callingPid + " caller = null ");
                                                }
                                                componentSpecified = true;
                                                rInfo2 = this.mSupervisor.resolveIntent(newIntent, null, userId);
                                                if (userId == 999 && callingPackage != null) {
                                                    try {
                                                        if (OppoMultiLauncherUtil.getInstance().isMultiApp(callingPackage) && rInfo2 == null) {
                                                            userId = 0;
                                                            rInfo2 = this.mSupervisor.resolveIntent(newIntent, null, 66560, 0);
                                                            Slog.i(TAG, "multi app: startActivityMayWait change userId to " + 0 + " rInfo = " + rInfo2);
                                                        }
                                                    } catch (Throwable th3) {
                                                        th = th3;
                                                        aInfo = aInfo2;
                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                        throw th;
                                                    }
                                                }
                                                aInfo = rInfo2 != null ? rInfo2.activityInfo : null;
                                                if (aInfo != null) {
                                                    aInfo = this.mService.getActivityInfoForUser(aInfo, userId);
                                                }
                                            } catch (Throwable th4) {
                                                th = th4;
                                                aInfo = aInfo2;
                                                rInfo2 = rInfo;
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                throw th;
                                            }
                                        }
                                    }
                                    aInfo = aInfo2;
                                    rInfo2 = rInfo;
                                    intent = intent3;
                                    outRecord = new ActivityRecord[1];
                                    res = startActivityLocked(caller, intent, intent2, resolvedType, aInfo, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, outRecord, container, inTask);
                                    Binder.restoreCallingIdentity(origId);
                                    if (stack.mConfigWillChange) {
                                        this.mService.enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateConfiguration()");
                                        stack.mConfigWillChange = false;
                                        if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                            Slog.v(TAG_CONFIGURATION, "Updating to new configuration after starting activity.");
                                        }
                                        this.mService.updateConfigurationLocked(config, null, false);
                                    }
                                    if (outResult != null) {
                                        outResult.result = res;
                                        if (res == 0) {
                                            this.mSupervisor.mWaitingActivityLaunched.add(outResult);
                                            while (true) {
                                                try {
                                                    this.mService.wait();
                                                } catch (InterruptedException e) {
                                                }
                                                try {
                                                    if (outResult.result != 2 && !outResult.timeout) {
                                                        if (outResult.who != null) {
                                                            break;
                                                        }
                                                    } else {
                                                        break;
                                                    }
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                }
                                            }
                                            if (outResult.result == 2) {
                                                res = 2;
                                            }
                                        }
                                        if (res == 2) {
                                            ActivityRecord r = stack.topRunningActivityLocked();
                                            if (!r.nowVisible || r.state != ActivityState.RESUMED) {
                                                outResult.thisTime = SystemClock.uptimeMillis();
                                                this.mSupervisor.mWaitingActivityVisible.add(outResult);
                                                while (true) {
                                                    try {
                                                        this.mService.wait();
                                                    } catch (InterruptedException e2) {
                                                    }
                                                    if (outResult.timeout || outResult.who != null) {
                                                        break;
                                                    }
                                                }
                                            } else {
                                                outResult.timeout = false;
                                                outResult.who = new ComponentName(r.info.packageName, r.info.name);
                                                outResult.totalTime = 0;
                                                outResult.thisTime = 0;
                                            }
                                        }
                                    }
                                    this.mSupervisor.mActivityMetricsLogger.notifyActivityLaunched(res, this.mReusedActivity == null ? this.mReusedActivity : outRecord[0]);
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return res;
                                }
                            }
                            aInfo = aInfo2;
                            rInfo2 = rInfo;
                            intent = intent3;
                            outRecord = new ActivityRecord[1];
                            res = startActivityLocked(caller, intent, intent2, resolvedType, aInfo, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, outRecord, container, inTask);
                            Binder.restoreCallingIdentity(origId);
                            if (stack.mConfigWillChange) {
                            }
                            if (outResult != null) {
                            }
                            if (this.mReusedActivity == null) {
                            }
                            this.mSupervisor.mActivityMetricsLogger.notifyActivityLaunched(res, this.mReusedActivity == null ? this.mReusedActivity : outRecord[0]);
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return res;
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return -6;
                    } catch (Throwable th6) {
                        th = th6;
                        aInfo = aInfo2;
                        rInfo2 = rInfo;
                        intent = intent3;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            rInfo = rInfo2;
            aInfo = this.mSupervisor.resolveActivity(intent3, rInfo, startFlags, profilerInfo);
            if (!disAllowActivityStartForCustomizeProject(aInfo)) {
            }
        } else {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
    }

    final int startActivities(IApplicationThread caller, int callingUid, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle bOptions, int userId) {
        if (intents == null) {
            throw new NullPointerException("intents is null");
        } else if (resolvedTypes == null) {
            throw new NullPointerException("resolvedTypes is null");
        } else if (intents.length != resolvedTypes.length) {
            throw new IllegalArgumentException("intents are length different than resolvedTypes");
        } else {
            int callingPid;
            int realCallingPid = Binder.getCallingPid();
            int realCallingUid = Binder.getCallingUid();
            if (callingUid >= 0) {
                callingPid = -1;
            } else if (caller == null) {
                callingPid = realCallingPid;
                callingUid = realCallingUid;
            } else {
                callingUid = -1;
                callingPid = -1;
            }
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mService) {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord[] outActivity = new ActivityRecord[1];
                    int i = 0;
                    while (i < intents.length) {
                        Intent intent = intents[i];
                        if (intent == null) {
                        } else if (intent == null || !intent.hasFileDescriptors()) {
                            boolean componentSpecified = intent.getComponent() != null;
                            Intent intent2 = new Intent(intent);
                            ActivityInfo aInfo = this.mService.getActivityInfoForUser(this.mSupervisor.resolveActivity(intent2, resolvedTypes[i], 0, null, userId), userId);
                            if (aInfo == null || (aInfo.applicationInfo.privateFlags & 2) == 0) {
                                int res = startActivityLocked(caller, intent2, null, resolvedTypes[i], aInfo, null, null, null, resultTo, null, -1, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, 0, ActivityOptions.fromBundle(i == intents.length + -1 ? bOptions : null), false, componentSpecified, outActivity, null, null);
                                if (res < 0) {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    Binder.restoreCallingIdentity(origId);
                                    return res;
                                } else if (outActivity[0] != null) {
                                    Object resultTo2 = outActivity[0].appToken;
                                } else {
                                    resultTo2 = null;
                                }
                            } else {
                                throw new IllegalArgumentException("FLAG_CANT_SAVE_STATE not supported here");
                            }
                        } else {
                            throw new IllegalArgumentException("File descriptors passed in Intent");
                        }
                        i++;
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return 0;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    void sendPowerHintForLaunchStartIfNeeded(boolean forceSend) {
        ActivityStack focusStack = this.mSupervisor.getFocusedStack();
        ActivityRecord curTop = focusStack == null ? null : focusStack.topRunningNonDelayedActivityLocked(this.mNotTop);
        if ((forceSend || !(this.mPowerHintSent || curTop == null || curTop.task == null || this.mStartActivity == null || curTop.task == this.mStartActivity.task)) && this.mService.mLocalPowerManager != null) {
            this.mService.mLocalPowerManager.powerHint(8, 1);
            this.mPowerHintSent = true;
        }
    }

    void sendPowerHintForLaunchEndIfNeeded() {
        if (this.mPowerHintSent && this.mService.mLocalPowerManager != null) {
            this.mService.mLocalPowerManager.powerHint(8, 0);
            this.mPowerHintSent = false;
        }
    }

    private int startActivityUnchecked(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask) {
        ActivityRecord top;
        setInitialState(r, options, inTask, doResume, startFlags, sourceRecord, voiceSession, voiceInteractor);
        computeLaunchingTaskFlags();
        computeSourceStack();
        if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "launchFlags(update): 0x" + Integer.toHexString(this.mLaunchFlags));
        }
        this.mIntent.setFlags(this.mLaunchFlags);
        this.mReusedActivity = getReusableIntentActivity();
        int preferredLaunchStackId = this.mOptions != null ? this.mOptions.getLaunchStackId() : -1;
        if (this.mReusedActivity != null) {
            if (this.mSupervisor.isLockTaskModeViolation(this.mReusedActivity.task, (this.mLaunchFlags & 268468224) == 268468224)) {
                this.mSupervisor.showLockTaskToast();
                Slog.e(TAG, "startActivityUnchecked: Attempt to violate Lock Task Mode");
                return 5;
            }
            if (this.mStartActivity.task == null) {
                this.mStartActivity.task = this.mReusedActivity.task;
            }
            if (this.mReusedActivity.task.intent == null) {
                this.mReusedActivity.task.setIntent(this.mStartActivity);
            }
            if ((this.mLaunchFlags & 67108864) != 0 || this.mLaunchSingleInstance || this.mLaunchSingleTask) {
                top = this.mReusedActivity.task.performClearTaskForReuseLocked(this.mStartActivity, this.mLaunchFlags);
                if (top != null) {
                    if (top.frontOfTask) {
                        top.task.setIntent(this.mStartActivity);
                    }
                    ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, top.task);
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG, "ACT-AM_NEW_INTENT " + this.mStartActivity + " " + top.task);
                    }
                    top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
                }
            }
            sendPowerHintForLaunchStartIfNeeded(false);
            this.mReusedActivity = setTargetStackAndMoveToFrontIfNeeded(this.mReusedActivity);
            if ((this.mStartFlags & 1) != 0) {
                resumeTargetStackIfNeeded();
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG, "START_RETURN_INTENT_TO_CALLER");
                }
                return 1;
            }
            setTaskFromIntentActivity(this.mReusedActivity);
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(ActivityStackSupervisor.TAG_TASKS, "mAddingToTask: " + this.mAddingToTask + " mReuseTask " + this.mReuseTask + " r.packageName " + r.packageName + " " + (this.mLaunchFlags & Integer.MIN_VALUE));
            }
            if (!this.mAddingToTask && this.mReuseTask == null && r.packageName != null && (this.mLaunchFlags & Integer.MIN_VALUE) == 0) {
                ActivityRecord topActivity = null;
                if (!(this.mReusedActivity == null || this.mReusedActivity.task == null)) {
                    topActivity = this.mReusedActivity.task.getTopActivity();
                }
                boolean skip = false;
                if (topActivity != null) {
                    if (ActivityManagerDebugConfig.DEBUG_PERMISSION) {
                        Slog.i(TAG, "topActivity.packageName = " + topActivity.packageName);
                    }
                    if (!(topActivity.packageName == null || r.packageName == null || !r.packageName.equals("com.coloros.safecenter") || r.packageName.equals(topActivity.packageName) || topActivity.packageName.equals("com.coloros.securitypermission"))) {
                        this.mTargetStack.destroyActivityLocked(topActivity, true, "secure protect");
                        this.mTargetStack.removeActivityFromHistoryLocked(topActivity, inTask, "secure protect");
                        if (this.mReusedActivity.task.mActivities.isEmpty()) {
                            this.mTargetStack.removeTask(this.mReusedActivity.task, "secure protect");
                        }
                        skip = true;
                        if (ActivityManagerDebugConfig.DEBUG_PERMISSION) {
                            Slog.i(TAG, "secure protect remove!  r.packageName = " + r.packageName + "  topActivity.packageName = " + topActivity.packageName);
                        }
                    }
                }
                if (!skip) {
                    resumeTargetStackIfNeeded();
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG, "START_TASK_TO_FRONT");
                    }
                    return 2;
                }
            }
        }
        if (this.mStartActivity.packageName == null) {
            if (!(this.mStartActivity.resultTo == null || this.mStartActivity.resultTo.task.stack == null)) {
                this.mStartActivity.resultTo.task.stack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
            }
            ActivityOptions.abort(this.mOptions);
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "START_CLASS_NOT_FOUND");
            }
            return -2;
        }
        boolean dontStart;
        ActivityStack topStack = this.mSupervisor.mFocusedStack;
        top = topStack.topRunningNonDelayedActivityLocked(this.mNotTop);
        if (top == null || this.mStartActivity.resultTo != null || !top.realActivity.equals(this.mStartActivity.realActivity) || top.userId != this.mStartActivity.userId || top.app == null || top.app.thread == null) {
            dontStart = false;
        } else {
            boolean z;
            if ((this.mLaunchFlags & 536870912) != 0 || this.mLaunchSingleTop) {
                z = true;
            } else {
                z = this.mLaunchSingleTask;
            }
            dontStart = z;
        }
        if (dontStart) {
            ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, top, top.task);
            topStack.mLastPausedActivity = null;
            if (this.mDoResume) {
                this.mSupervisor.resumeFocusedStackTopActivityLocked();
            }
            ActivityOptions.abort(this.mOptions);
            if ((this.mStartFlags & 1) != 0) {
                return 1;
            }
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "ACT-AM_NEW_INTENT " + this.mStartActivity + " " + top.task);
            }
            top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            this.mSupervisor.handleNonResizableTaskIfNeeded(top.task, preferredLaunchStackId, topStack.mStackId);
            return 3;
        }
        boolean newTask = false;
        TaskRecord taskToAffiliate = (!this.mLaunchTaskBehind || this.mSourceRecord == null) ? null : this.mSourceRecord.task;
        int result;
        if (this.mStartActivity.resultTo == null && this.mInTask == null && !this.mAddingToTask && (this.mLaunchFlags & 268435456) != 0) {
            newTask = true;
            setTaskFromReuseOrCreateNewTask(taskToAffiliate);
            if (this.mSupervisor.isLockTaskModeViolation(this.mStartActivity.task)) {
                this.mSupervisor.showLockTaskToast();
                Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
                return 5;
            } else if (!this.mMovedOtherTask) {
                TaskRecord taskRecord = this.mStartActivity.task;
                int i = this.mLaunchFlags;
                if (preferredLaunchStackId != -1) {
                    topStack = this.mTargetStack;
                }
                updateTaskReturnToType(taskRecord, i, topStack);
            }
        } else if (this.mSourceRecord != null) {
            if (this.mSupervisor.isLockTaskModeViolation(this.mSourceRecord.task)) {
                this.mSupervisor.showLockTaskToast();
                Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
                return 5;
            }
            result = setTaskFromSourceRecord();
            if (result != 0) {
                return result;
            }
        } else if (this.mInTask == null) {
            setTaskToCurrentTopOrCreateNewTask();
        } else if (this.mSupervisor.isLockTaskModeViolation(this.mInTask)) {
            this.mSupervisor.showLockTaskToast();
            Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 5;
        } else {
            result = setTaskFromInTask();
            if (result != 0) {
                return result;
            }
        }
        this.mService.grantUriPermissionFromIntentLocked(this.mCallingUid, this.mStartActivity.packageName, this.mIntent, this.mStartActivity.getUriPermissionsLocked(), this.mStartActivity.userId);
        if (!(UserHandle.getUserId(this.mCallingUid) != OppoMultiAppManager.USER_ID || this.mIntent == null || this.mIntent.getAction() == null)) {
            String pkgName = null;
            if (sourceRecord == null || sourceRecord.packageName == null) {
                try {
                    pkgName = AppGlobals.getPackageManager().getNameForUid(this.mCallingUid);
                } catch (RemoteException e) {
                }
            } else {
                pkgName = sourceRecord.packageName;
            }
            String action = this.mIntent.getAction();
            String clipdatastring = null;
            if (this.mIntent.getClipData() != null) {
                clipdatastring = this.mIntent.getClipData().toString();
            }
            String data = this.mIntent.getDataString();
            if (pkgName != null && OppoMultiLauncherUtil.getInstance().isMultiApp(pkgName) && (((clipdatastring != null && clipdatastring.contains(pkgName)) || ((data != null && data.contains(pkgName)) || (clipdatastring != null && clipdatastring.contains("com.instagram.fileprovider")))) && (action.equals("android.intent.action.SEND") || action.equals("android.intent.action.VIEW") || action.equals("android.media.action.IMAGE_CAPTURE") || action.equals("android.media.action.VIDEO_CAPTURE") || action.equals("android.intent.action.SEND_MULTIPLE")))) {
                this.mIntent.fixUris(OppoMultiAppManager.USER_ID);
            }
        }
        if (this.mSourceRecord != null && this.mSourceRecord.isRecentsActivity()) {
            this.mStartActivity.task.setTaskToReturnTo(2);
        }
        if (newTask) {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(this.mStartActivity.userId);
            objArr[1] = Integer.valueOf(this.mStartActivity.task.taskId);
            EventLog.writeEvent(EventLogTags.AM_CREATE_TASK, objArr);
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "ACT-AM_CREATE_TASK " + this.mStartActivity + " " + this.mStartActivity.task);
            }
        }
        ActivityStack.logStartActivity(EventLogTags.AM_CREATE_ACTIVITY, this.mStartActivity, this.mStartActivity.task);
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "ACT-AM_CREATE_ACTIVITY " + this.mStartActivity + " " + this.mStartActivity.task);
        }
        this.mTargetStack.mLastPausedActivity = null;
        sendPowerHintForLaunchStartIfNeeded(false);
        this.mTargetStack.startActivityLocked(this.mStartActivity, newTask, this.mKeepCurTransition, this.mOptions);
        if (this.mDoResume) {
            if (!this.mLaunchTaskBehind) {
                this.mService.setFocusedActivityLocked(this.mStartActivity, "startedActivity");
            }
            ActivityRecord topTaskActivity = this.mStartActivity.task.topRunningActivityLocked();
            if (this.mTargetStack.isFocusable() && (topTaskActivity == null || !topTaskActivity.mTaskOverlay || this.mStartActivity == topTaskActivity)) {
                this.mSupervisor.resumeFocusedStackTopActivityLocked(this.mTargetStack, this.mStartActivity, this.mOptions);
            } else {
                this.mTargetStack.ensureActivitiesVisibleLocked(null, 0, false);
                this.mWindowManager.executeAppTransition();
            }
        } else {
            this.mTargetStack.addRecentActivityLocked(this.mStartActivity);
        }
        this.mSupervisor.updateUserStackLocked(this.mStartActivity.userId, this.mTargetStack);
        this.mSupervisor.handleNonResizableTaskIfNeeded(this.mStartActivity.task, preferredLaunchStackId, this.mTargetStack.mStackId);
        return 0;
    }

    private void setInitialState(ActivityRecord r, ActivityOptions options, TaskRecord inTask, boolean doResume, int startFlags, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
        reset();
        this.mStartActivity = r;
        this.mIntent = r.intent;
        this.mOptions = options;
        this.mCallingUid = r.launchedFromUid;
        this.mSourceRecord = sourceRecord;
        this.mVoiceSession = voiceSession;
        this.mVoiceInteractor = voiceInteractor;
        this.mLaunchBounds = getOverrideBounds(r, options, inTask);
        this.mLaunchSingleTop = r.launchMode == 1;
        this.mLaunchSingleInstance = r.launchMode == 3;
        this.mLaunchSingleTask = r.launchMode == 2;
        this.mLaunchFlags = adjustLaunchFlagsToDocumentMode(r, this.mLaunchSingleInstance, this.mLaunchSingleTask, this.mIntent.getFlags());
        boolean z = (!r.mLaunchTaskBehind || this.mLaunchSingleTask || this.mLaunchSingleInstance) ? false : (this.mLaunchFlags & DumpState.DUMP_FROZEN) != 0;
        this.mLaunchTaskBehind = z;
        sendNewTaskResultRequestIfNeeded();
        if ((this.mLaunchFlags & DumpState.DUMP_FROZEN) != 0 && r.resultTo == null) {
            this.mLaunchFlags |= 268435456;
        }
        if ((this.mLaunchFlags & 268435456) != 0 && (this.mLaunchTaskBehind || r.info.documentLaunchMode == 2)) {
            this.mLaunchFlags |= 134217728;
        }
        if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "ACT-launchFlags(mLaunchFlags): 0x" + Integer.toHexString(this.mLaunchFlags) + ", launchMode:" + r.launchMode + ", startFlags: " + startFlags + ", doResume:" + doResume);
        }
        this.mSupervisor.mUserLeaving = (this.mLaunchFlags & DumpState.DUMP_DOMAIN_PREFERRED) == 0;
        if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
            Slog.v(TAG_USER_LEAVING, "startActivity() => mUserLeaving=" + this.mSupervisor.mUserLeaving);
        }
        this.mDoResume = doResume;
        if (!(doResume && this.mSupervisor.okToShowLocked(r))) {
            r.delayedResume = true;
            this.mDoResume = false;
        }
        if (!(this.mOptions == null || this.mOptions.getLaunchTaskId() == -1 || !this.mOptions.getTaskOverlay())) {
            r.mTaskOverlay = true;
            TaskRecord task = this.mSupervisor.anyTaskForIdLocked(this.mOptions.getLaunchTaskId());
            ActivityRecord top = task != null ? task.getTopActivity() : null;
            if (!(top == null || top.visible)) {
                this.mDoResume = false;
                this.mAvoidMoveToFront = true;
            }
        }
        this.mNotTop = (this.mLaunchFlags & 16777216) != 0 ? r : null;
        this.mInTask = inTask;
        if (!(inTask == null || inTask.inRecents)) {
            Slog.w(TAG, "Starting activity in task not in recents: " + inTask);
            this.mInTask = null;
        }
        this.mStartFlags = startFlags;
        if ((startFlags & 1) != 0) {
            ActivityRecord checkedCaller = sourceRecord;
            if (sourceRecord == null) {
                checkedCaller = this.mSupervisor.mFocusedStack.topRunningNonDelayedActivityLocked(this.mNotTop);
            }
            if (!checkedCaller.realActivity.equals(r.realActivity)) {
                this.mStartFlags &= -2;
            }
        }
        if ((this.mLaunchFlags & DumpState.DUMP_INSTALLS) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mNoAnimation = z;
    }

    private void sendNewTaskResultRequestIfNeeded() {
        boolean isOppoLauncher;
        if (this.mStartActivity.packageName != null) {
            isOppoLauncher = ActivityManagerService.OPPO_LAUNCHER.equals(this.mStartActivity.packageName);
        } else {
            isOppoLauncher = false;
        }
        if (!isOppoLauncher && this.mStartActivity.resultTo != null && (this.mLaunchFlags & 268435456) != 0 && this.mStartActivity.resultTo.task.stack != null && (this.mLaunchFlags & Integer.MIN_VALUE) == 0) {
            Slog.w(TAG, "Activity is launching as a new task, so cancelling activity result.");
            this.mStartActivity.resultTo.task.stack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
            this.mStartActivity.resultTo = null;
        }
    }

    private void computeLaunchingTaskFlags() {
        if (this.mSourceRecord != null || this.mInTask == null || this.mInTask.stack == null) {
            this.mInTask = null;
            if ((this.mStartActivity.isResolverActivity() || this.mStartActivity.noDisplay) && this.mSourceRecord != null && this.mSourceRecord.isFreeform()) {
                this.mAddingToTask = true;
            }
        } else {
            Intent baseIntent = this.mInTask.getBaseIntent();
            ActivityRecord root = this.mInTask.getRootActivity();
            if (baseIntent == null) {
                ActivityOptions.abort(this.mOptions);
                throw new IllegalArgumentException("Launching into task without base intent: " + this.mInTask);
            }
            if (this.mLaunchSingleInstance || this.mLaunchSingleTask) {
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
        }
        if (this.mInTask != null) {
            return;
        }
        if (this.mSourceRecord == null) {
            if ((this.mLaunchFlags & 268435456) == 0 && this.mInTask == null) {
                Slog.w(TAG, "startActivity called from non-Activity context; forcing Intent.FLAG_ACTIVITY_NEW_TASK for: " + this.mIntent);
                this.mLaunchFlags |= 268435456;
            }
        } else if (this.mSourceRecord.launchMode == 3) {
            this.mLaunchFlags |= 268435456;
        } else if (this.mLaunchSingleInstance || this.mLaunchSingleTask) {
            this.mLaunchFlags |= 268435456;
        }
    }

    private void computeSourceStack() {
        if (this.mSourceRecord == null) {
            this.mSourceStack = null;
        } else if (this.mSourceRecord.finishing) {
            if ((this.mLaunchFlags & 268435456) == 0) {
                Slog.w(TAG, "startActivity called from finishing " + this.mSourceRecord + "; forcing " + "Intent.FLAG_ACTIVITY_NEW_TASK for: " + this.mIntent);
                this.mLaunchFlags |= 268435456;
                this.mNewTaskInfo = this.mSourceRecord.info;
                this.mNewTaskIntent = this.mSourceRecord.task.intent;
            }
            this.mSourceRecord = null;
            this.mSourceStack = null;
        } else {
            this.mSourceStack = this.mSourceRecord.task.stack;
        }
    }

    private ActivityRecord getReusableIntentActivity() {
        boolean putIntoExistingTask;
        int i;
        boolean z = false;
        if (((this.mLaunchFlags & 268435456) == 0 || (this.mLaunchFlags & 134217728) != 0) && !this.mLaunchSingleInstance) {
            putIntoExistingTask = this.mLaunchSingleTask;
        } else {
            putIntoExistingTask = true;
        }
        if (this.mInTask == null && this.mStartActivity.resultTo == null) {
            i = 1;
        } else {
            i = 0;
        }
        putIntoExistingTask &= i;
        if (this.mOptions != null && this.mOptions.getLaunchTaskId() != -1) {
            TaskRecord task = this.mSupervisor.anyTaskForIdLocked(this.mOptions.getLaunchTaskId());
            if (task != null) {
                return task.getTopActivity();
            }
            return null;
        } else if (!putIntoExistingTask) {
            return null;
        } else {
            if (this.mLaunchSingleInstance) {
                return this.mSupervisor.findActivityLocked(this.mIntent, this.mStartActivity.info, false);
            }
            if ((this.mLaunchFlags & 4096) == 0) {
                return this.mSupervisor.findTaskLocked(this.mStartActivity);
            }
            ActivityStackSupervisor activityStackSupervisor = this.mSupervisor;
            Intent intent = this.mIntent;
            ActivityInfo activityInfo = this.mStartActivity.info;
            if (!this.mLaunchSingleTask) {
                z = true;
            }
            return activityStackSupervisor.findActivityLocked(intent, activityInfo, z);
        }
    }

    private ActivityRecord setTargetStackAndMoveToFrontIfNeeded(ActivityRecord intentActivity) {
        ActivityRecord curTop;
        boolean willClearTask = false;
        this.mTargetStack = intentActivity.task.stack;
        this.mTargetStack.mLastPausedActivity = null;
        ActivityStack focusStack = this.mSupervisor.getFocusedStack();
        if (focusStack == null) {
            curTop = null;
        } else {
            curTop = focusStack.topRunningNonDelayedActivityLocked(this.mNotTop);
        }
        if (!(curTop == null || ((curTop.task == intentActivity.task && curTop.task == focusStack.topTask()) || this.mAvoidMoveToFront))) {
            this.mStartActivity.intent.addFlags(4194304);
            if (this.mSourceRecord == null || (this.mSourceStack.topActivity() != null && this.mSourceStack.topActivity().task == this.mSourceRecord.task)) {
                if (this.mLaunchTaskBehind && this.mSourceRecord != null) {
                    intentActivity.setTaskToAffiliateWith(this.mSourceRecord.task);
                }
                this.mMovedOtherTask = true;
                if ((this.mLaunchFlags & 268468224) == 268468224) {
                    willClearTask = true;
                }
                if (!willClearTask) {
                    ActivityStack launchStack = getLaunchStack(this.mStartActivity, this.mLaunchFlags, this.mStartActivity.task, this.mOptions);
                    if (launchStack == null || launchStack == this.mTargetStack) {
                        this.mTargetStack.moveTaskToFrontLocked(intentActivity.task, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringingFoundTaskToFront");
                        this.mMovedToFront = true;
                    } else if (launchStack.mStackId == 3 || launchStack.mStackId == 1) {
                        if ((this.mLaunchFlags & 4096) != 0) {
                            this.mSupervisor.moveTaskToStackLocked(intentActivity.task.taskId, launchStack.mStackId, true, true, "launchToSide", true);
                        } else {
                            this.mTargetStack.moveTaskToFrontLocked(intentActivity.task, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringToFrontInsteadOfAdjacentLaunch");
                        }
                        this.mMovedToFront = true;
                    }
                    this.mOptions = null;
                }
                updateTaskReturnToType(intentActivity.task, this.mLaunchFlags, focusStack);
            }
        }
        if (!this.mMovedToFront && this.mDoResume) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(ActivityStackSupervisor.TAG_TASKS, "Bring to front target: " + this.mTargetStack + " from " + intentActivity);
            }
            this.mTargetStack.moveToFront("intentActivityFound");
        }
        this.mSupervisor.handleNonResizableTaskIfNeeded(intentActivity.task, -1, this.mTargetStack.mStackId);
        if ((this.mLaunchFlags & DumpState.DUMP_COMPILER_STATS) != 0) {
            return this.mTargetStack.resetTaskIfNeededLocked(intentActivity, this.mStartActivity);
        }
        return intentActivity;
    }

    private void updateTaskReturnToType(TaskRecord task, int launchFlags, ActivityStack focusedStack) {
        if ((launchFlags & 268451840) == 268451840) {
            task.setTaskToReturnTo(1);
        } else if (focusedStack == null || focusedStack.mStackId == 0) {
            task.setTaskToReturnTo(1);
        } else {
            task.setTaskToReturnTo(0);
        }
    }

    private void setTaskFromIntentActivity(ActivityRecord intentActivity) {
        boolean z = false;
        if ((this.mLaunchFlags & 268468224) == 268468224) {
            intentActivity.task.performClearTaskLocked();
            intentActivity.task.setIntent(this.mStartActivity);
            this.mReuseTask = intentActivity.task;
            this.mMovedOtherTask = true;
        } else if ((this.mLaunchFlags & 67108864) != 0 || this.mLaunchSingleInstance || this.mLaunchSingleTask) {
            if (intentActivity.task.performClearTaskLocked(this.mStartActivity, this.mLaunchFlags) == null) {
                this.mAddingToTask = true;
                this.mSourceRecord = intentActivity;
                TaskRecord task = this.mSourceRecord.task;
                if (task != null && task.stack == null) {
                    this.mTargetStack = computeStackFocus(this.mSourceRecord, false, null, this.mLaunchFlags, this.mOptions);
                    ActivityStack activityStack = this.mTargetStack;
                    if (!this.mLaunchTaskBehind) {
                        z = true;
                    }
                    activityStack.addTask(task, z, "startActivityUnchecked");
                }
                if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG, "special case: the activity is not currently running");
                }
            }
        } else if (this.mStartActivity.realActivity.equals(intentActivity.task.realActivity)) {
            if (((this.mLaunchFlags & 536870912) != 0 || this.mLaunchSingleTop) && intentActivity.realActivity.equals(this.mStartActivity.realActivity)) {
                ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, intentActivity.task);
                if (intentActivity.frontOfTask) {
                    intentActivity.task.setIntent(this.mStartActivity);
                }
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG, "ACT-AM_NEW_INTENT " + this.mStartActivity + " " + intentActivity.task);
                }
                intentActivity.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            } else if (!intentActivity.task.isSameIntentFilter(this.mStartActivity)) {
                this.mAddingToTask = true;
                this.mSourceRecord = intentActivity;
                if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG, "since different intents, start new activity...");
                }
            }
        } else if ((this.mLaunchFlags & DumpState.DUMP_COMPILER_STATS) == 0) {
            this.mAddingToTask = true;
            this.mSourceRecord = intentActivity;
            if (!ActivityManagerService.IS_USER_BUILD || ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "place the new activity on top of the current task...");
            }
        } else if (!intentActivity.task.rootWasReset) {
            intentActivity.task.setIntent(this.mStartActivity);
        }
    }

    private void resumeTargetStackIfNeeded() {
        if (this.mDoResume) {
            this.mSupervisor.resumeFocusedStackTopActivityLocked(this.mTargetStack, null, this.mOptions);
            if (!this.mMovedToFront) {
                this.mSupervisor.notifyActivityDrawnForKeyguard();
            }
        } else {
            ActivityOptions.abort(this.mOptions);
        }
        this.mSupervisor.updateUserStackLocked(this.mStartActivity.userId, this.mTargetStack);
    }

    private void setTaskFromReuseOrCreateNewTask(TaskRecord taskToAffiliate) {
        this.mTargetStack = computeStackFocus(this.mStartActivity, true, this.mLaunchBounds, this.mLaunchFlags, this.mOptions);
        if (this.mReuseTask == null) {
            boolean z;
            ActivityStack activityStack = this.mTargetStack;
            int nextTaskIdForUserLocked = this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.userId);
            ActivityInfo activityInfo = this.mNewTaskInfo != null ? this.mNewTaskInfo : this.mStartActivity.info;
            Intent intent = this.mNewTaskIntent != null ? this.mNewTaskIntent : this.mIntent;
            IVoiceInteractionSession iVoiceInteractionSession = this.mVoiceSession;
            IVoiceInteractor iVoiceInteractor = this.mVoiceInteractor;
            if (this.mLaunchTaskBehind) {
                z = false;
            } else {
                z = true;
            }
            this.mStartActivity.setTask(activityStack.createTaskRecord(nextTaskIdForUserLocked, activityInfo, intent, iVoiceInteractionSession, iVoiceInteractor, z), taskToAffiliate);
            if (this.mLaunchBounds != null) {
                nextTaskIdForUserLocked = this.mTargetStack.mStackId;
                if (StackId.resizeStackWithLaunchBounds(nextTaskIdForUserLocked)) {
                    this.mService.resizeStack(nextTaskIdForUserLocked, this.mLaunchBounds, true, false, true, -1);
                } else {
                    this.mStartActivity.task.updateOverrideConfiguration(this.mLaunchBounds);
                }
            }
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in new task " + this.mStartActivity.task);
                return;
            }
            return;
        }
        this.mStartActivity.setTask(this.mReuseTask, taskToAffiliate);
    }

    private int setTaskFromSourceRecord() {
        TaskRecord sourceTask = this.mSourceRecord.task;
        if (sourceTask.stack.topTask() != sourceTask) {
            this.mTargetStack = getLaunchStack(this.mStartActivity, this.mLaunchFlags, this.mStartActivity.task, this.mOptions);
        }
        if (this.mTargetStack == null) {
            this.mTargetStack = sourceTask.stack;
        } else if (this.mTargetStack != sourceTask.stack) {
            this.mSupervisor.moveTaskToStackLocked(sourceTask.taskId, this.mTargetStack.mStackId, true, true, "launchToSide", false);
        }
        if (this.mDoResume) {
            this.mTargetStack.moveToFront("sourceStackToFront");
        }
        if (!(this.mTargetStack.topTask() == sourceTask || this.mAvoidMoveToFront)) {
            this.mTargetStack.moveTaskToFrontLocked(sourceTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "sourceTaskToFront");
        }
        ActivityRecord top;
        if (!this.mAddingToTask && (this.mLaunchFlags & 67108864) != 0) {
            top = sourceTask.performClearTaskLocked(this.mStartActivity, this.mLaunchFlags);
            this.mKeepCurTransition = true;
            if (top != null) {
                ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, top.task);
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG, "ACT-AM_NEW_INTENT " + this.mStartActivity + " " + top.task);
                }
                top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
                this.mTargetStack.mLastPausedActivity = null;
                if (this.mDoResume) {
                    this.mSupervisor.resumeFocusedStackTopActivityLocked();
                }
                ActivityOptions.abort(this.mOptions);
                return 3;
            }
        } else if (!(this.mAddingToTask || (this.mLaunchFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) == 0)) {
            top = sourceTask.findActivityInHistoryLocked(this.mStartActivity);
            if (top != null) {
                TaskRecord task = top.task;
                task.moveActivityToFrontLocked(top);
                top.updateOptionsLocked(this.mOptions);
                ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, task);
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG, "ACT-AM_NEW_INTENT " + this.mStartActivity + " " + top.task);
                }
                top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
                this.mTargetStack.mLastPausedActivity = null;
                if (this.mDoResume) {
                    this.mSupervisor.resumeFocusedStackTopActivityLocked();
                }
                return 3;
            }
        }
        this.mStartActivity.setTask(sourceTask, null);
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in existing task " + this.mStartActivity.task + " from source " + this.mSourceRecord);
        }
        return 0;
    }

    private int setTaskFromInTask() {
        if (this.mLaunchBounds != null) {
            this.mInTask.updateOverrideConfiguration(this.mLaunchBounds);
            int stackId = this.mInTask.getLaunchStackId();
            if (stackId != this.mInTask.stack.mStackId) {
                stackId = this.mSupervisor.moveTaskToStackUncheckedLocked(this.mInTask, stackId, true, false, "inTaskToFront").mStackId;
            }
            if (StackId.resizeStackWithLaunchBounds(stackId)) {
                this.mService.resizeStack(stackId, this.mLaunchBounds, true, false, true, -1);
            }
        }
        this.mTargetStack = this.mInTask.stack;
        this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
        ActivityRecord top = this.mInTask.getTopActivity();
        if (top != null && top.realActivity.equals(this.mStartActivity.realActivity) && top.userId == this.mStartActivity.userId && ((this.mLaunchFlags & 536870912) != 0 || this.mLaunchSingleTop || this.mLaunchSingleTask)) {
            ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, top, top.task);
            if ((this.mStartFlags & 1) != 0) {
                return 1;
            }
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "ACT-AM_NEW_INTENT " + this.mStartActivity + " " + top.task);
            }
            top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            return 3;
        } else if (this.mAddingToTask) {
            this.mStartActivity.setTask(this.mInTask, null);
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in explicit task " + this.mStartActivity.task);
            }
            return 0;
        } else {
            ActivityOptions.abort(this.mOptions);
            return 2;
        }
    }

    private void setTaskToCurrentTopOrCreateNewTask() {
        this.mTargetStack = computeStackFocus(this.mStartActivity, false, null, this.mLaunchFlags, this.mOptions);
        if (this.mDoResume) {
            this.mTargetStack.moveToFront("addingToTopTask");
        }
        ActivityRecord prev = this.mTargetStack.topActivity();
        this.mStartActivity.setTask(prev != null ? prev.task : this.mTargetStack.createTaskRecord(this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.userId), this.mStartActivity.info, this.mIntent, null, null, true), null);
        this.mWindowManager.moveTaskToTop(this.mStartActivity.task.taskId);
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in new guessed " + this.mStartActivity.task);
        }
    }

    private int adjustLaunchFlagsToDocumentMode(ActivityRecord r, boolean launchSingleInstance, boolean launchSingleTask, int launchFlags) {
        if ((launchFlags & DumpState.DUMP_FROZEN) == 0 || !(launchSingleInstance || launchSingleTask)) {
            switch (r.info.documentLaunchMode) {
                case 1:
                    return launchFlags | DumpState.DUMP_FROZEN;
                case 2:
                    return launchFlags | DumpState.DUMP_FROZEN;
                case 3:
                    return launchFlags & -134217729;
                default:
                    return launchFlags;
            }
        }
        Slog.i(TAG, "Ignoring FLAG_ACTIVITY_NEW_DOCUMENT, launchMode is \"singleInstance\" or \"singleTask\"");
        return launchFlags & -134742017;
    }

    final void doPendingActivityLaunchesLocked(boolean doResume) {
        while (!this.mPendingActivityLaunches.isEmpty()) {
            PendingActivityLaunch pal = (PendingActivityLaunch) this.mPendingActivityLaunches.remove(0);
            try {
                postStartActivityUncheckedProcessing(pal.r, startActivityUnchecked(pal.r, pal.sourceRecord, null, null, pal.startFlags, doResume ? this.mPendingActivityLaunches.isEmpty() : false, null, null), this.mSupervisor.mFocusedStack.mStackId, this.mSourceRecord, this.mTargetStack);
            } catch (Exception e) {
                Slog.e(TAG, "Exception during pending activity launch pal=" + pal, e);
                pal.sendErrorResult(e.getMessage());
            }
        }
    }

    private ActivityStack computeStackFocus(ActivityRecord r, boolean newTask, Rect bounds, int launchFlags, ActivityOptions aOptions) {
        TaskRecord task = r.task;
        boolean isApplicationTask = !r.isApplicationActivity() ? task != null ? task.isApplicationTask() : false : true;
        if (!isApplicationTask) {
            return this.mSupervisor.mHomeStack;
        }
        ActivityStack stack = getLaunchStack(r, launchFlags, task, aOptions);
        if (stack != null) {
            return stack;
        }
        if (task == null || task.stack == null) {
            ActivityContainer container = r.mInitialActivityContainer;
            if (container != null) {
                r.mInitialActivityContainer = null;
                return container.mStack;
            }
            int focusedStackId = this.mSupervisor.mFocusedStack.mStackId;
            boolean canUseFocusedStack = (focusedStackId == 1 || (focusedStackId == 3 && r.canGoInDockedStack())) ? true : focusedStackId == 2 ? r.isResizeableOrForced() : false;
            if (!canUseFocusedStack || (newTask && !this.mSupervisor.mFocusedStack.mActivityContainer.isEligibleForNewTasks())) {
                int stackId;
                ArrayList<ActivityStack> homeDisplayStacks = this.mSupervisor.mHomeStack.mStacks;
                int stackNdx = homeDisplayStacks.size() - 1;
                while (stackNdx >= 0) {
                    stack = (ActivityStack) homeDisplayStacks.get(stackNdx);
                    if (StackId.isStaticStack(stack.mStackId)) {
                        stackNdx--;
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                            Slog.d(TAG_FOCUS, "computeStackFocus: Setting focused stack=" + stack);
                        }
                        return stack;
                    }
                }
                if (task != null) {
                    stackId = task.getLaunchStackId();
                } else if (bounds != null) {
                    stackId = 2;
                } else {
                    stackId = 1;
                }
                stack = this.mSupervisor.getStack(stackId, true, true);
                if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(TAG_FOCUS, "computeStackFocus: New stack r=" + r + " stackId=" + stack.mStackId);
                }
                return stack;
            }
            if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG_FOCUS, "computeStackFocus: Have a focused stack=" + this.mSupervisor.mFocusedStack);
            }
            return this.mSupervisor.mFocusedStack;
        }
        stack = task.stack;
        if (stack.isOnHomeDisplay()) {
            if (this.mSupervisor.mFocusedStack != stack) {
                if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(TAG_FOCUS, "computeStackFocus: Setting focused stack to r=" + r + " task=" + task);
                }
            } else if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG_FOCUS, "computeStackFocus: Focused stack already=" + this.mSupervisor.mFocusedStack);
            }
        }
        return stack;
    }

    private ActivityStack getLaunchStack(ActivityRecord r, int launchFlags, TaskRecord task, ActivityOptions aOptions) {
        if (this.mReuseTask != null) {
            return this.mReuseTask.stack;
        }
        int launchStackId = aOptions != null ? aOptions.getLaunchStackId() : -1;
        if (isValidLaunchStackId(launchStackId, r)) {
            return this.mSupervisor.getStack(launchStackId, true, true);
        }
        if (launchStackId == 3) {
            return this.mSupervisor.getStack(1, true, true);
        }
        if ((launchFlags & 4096) == 0) {
            return null;
        }
        ActivityStack parentStack;
        if (task != null) {
            parentStack = task.stack;
        } else if (r.mInitialActivityContainer != null) {
            parentStack = r.mInitialActivityContainer.mStack;
        } else {
            parentStack = this.mSupervisor.mFocusedStack;
        }
        if (parentStack != this.mSupervisor.mFocusedStack) {
            return parentStack;
        }
        if (this.mSupervisor.mFocusedStack != null && task == this.mSupervisor.mFocusedStack.topTask()) {
            return this.mSupervisor.mFocusedStack;
        }
        if (parentStack != null && parentStack.mStackId == 3) {
            return this.mSupervisor.getStack(1, true, true);
        }
        ActivityStack dockedStack = this.mSupervisor.getStack(3);
        if (dockedStack == null || dockedStack.getStackVisibilityLocked(r) != 0) {
            return dockedStack;
        }
        return null;
    }

    private boolean isValidLaunchStackId(int stackId, ActivityRecord r) {
        if (stackId == -1 || stackId == 0 || !StackId.isStaticStack(stackId)) {
            return false;
        }
        if (stackId != 1 && (!this.mService.mSupportsMultiWindow || !r.isResizeableOrForced())) {
            return false;
        }
        if (stackId == 3 && r.canGoInDockedStack()) {
            return true;
        }
        if (stackId == 2 && !this.mService.mSupportsFreeformWindowManagement) {
            return false;
        }
        boolean supportsPip = this.mService.mSupportsPictureInPicture ? !r.supportsPictureInPicture() ? this.mService.mForceResizableActivities : true : false;
        return stackId != 4 || supportsPip;
    }

    Rect getOverrideBounds(ActivityRecord r, ActivityOptions options, TaskRecord inTask) {
        if (options == null) {
            return null;
        }
        if ((r.isResizeable() || (inTask != null && inTask.isResizeable())) && this.mSupervisor.canUseActivityOptionsLaunchBounds(options, options.getLaunchStackId())) {
            return TaskRecord.validateBounds(options.getLaunchBounds());
        }
        return null;
    }

    void setWindowManager(WindowManagerService wm) {
        this.mWindowManager = wm;
    }

    void removePendingActivityLaunchesLocked(ActivityStack stack) {
        for (int palNdx = this.mPendingActivityLaunches.size() - 1; palNdx >= 0; palNdx--) {
            if (((PendingActivityLaunch) this.mPendingActivityLaunches.get(palNdx)).stack == stack) {
                this.mPendingActivityLaunches.remove(palNdx);
            }
        }
    }

    private boolean disAllowActivityStartForCustomizeProject(ActivityInfo aInfo) {
        if (aInfo == null || aInfo.applicationInfo == null || aInfo.applicationInfo.packageName == null || !this.mService.isInDisallowedRunningAppList(aInfo.applicationInfo.packageName)) {
            return false;
        }
        this.mService.showDisallowedRunningAppDialog();
        return true;
    }
}

package com.android.server.am;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.app.WaitResult;
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
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.secrecy.SecrecyManagerInternal;
import android.service.voice.IVoiceInteractionSession;
import android.text.TextUtils;
import android.util.BoostFramework;
import android.util.EventLog;
import android.util.Slog;
import com.android.internal.app.HeavyWeightSwitcherActivity;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.LocalServices;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.DisplayTransformManager;
import com.android.server.face.FaceDaemonWrapper;
import com.android.server.pm.ColorPackageManagerHelper;
import com.android.server.pm.CompatibilityHelper;
import com.android.server.pm.InstantAppResolver;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.wm.WindowManagerService;
import com.oppo.hypnus.Hypnus;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ActivityStarter {
    private static final int BUFFER_SIZE = 128;
    private static final String OPPO_RECENTS_PACKAGE_NAME = "com.coloros.recents";
    private static final String PKG_OPPO_BROWSER = "com.android.browser";
    private static final String PKG_SOUGOU_INPUTMETHOD = "com.sohu.inputmethod.sogou";
    private static final String REGEX_PATTERN = "keyword=(.*?)&";
    private static final String SITE_SOUGOU = "https://wisd.sogou.com/web/searchList.jsp";
    private static final String TAG = "ActivityManager";
    private static final String TAG_CONFIGURATION = (TAG + ActivityManagerDebugConfig.POSTFIX_CONFIGURATION);
    private static final String TAG_FOCUS = (TAG + ActivityManagerDebugConfig.POSTFIX_FOCUS);
    private static final String TAG_RESULTS = (TAG + ActivityManagerDebugConfig.POSTFIX_RESULTS);
    private static final String TAG_USER_LEAVING = (TAG + ActivityManagerDebugConfig.POSTFIX_USER_LEAVING);
    private boolean mAddingToTask;
    private boolean mAvoidMoveToFront;
    private int mCallingUid;
    private boolean mDoResume;
    public Hypnus mHyp = null;
    private TaskRecord mInTask;
    private Intent mIntent;
    private boolean mIntentDelivered;
    private ActivityStartInterceptor mInterceptor;
    private boolean mKeepCurTransition;
    private final ActivityRecord[] mLastHomeActivityStartRecord = new ActivityRecord[1];
    private int mLastHomeActivityStartResult;
    private final ActivityRecord[] mLastStartActivityRecord = new ActivityRecord[1];
    private int mLastStartActivityResult;
    private long mLastStartActivityTimeMs;
    private String mLastStartReason;
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
    final ArrayList<PendingActivityLaunch> mPendingActivityLaunches = new ArrayList();
    public BoostFramework mPerf = null;
    private boolean mPowerHintSent;
    private TaskRecord mReuseTask;
    SecrecyManagerInternal mSecrecyManagerInternal;
    private final ActivityManagerService mService;
    private int mSourceDisplayId;
    private ActivityRecord mSourceRecord;
    private ActivityStack mSourceStack;
    private ActivityRecord mStartActivity;
    private int mStartFlags;
    private boolean mStartFromSystemuiOrGesture;
    private final ActivityStackSupervisor mSupervisor;
    private ActivityStack mTargetStack;
    private boolean mUsingVr2dDisplay;
    private IVoiceInteractor mVoiceInteractor;
    private IVoiceInteractionSession mVoiceSession;
    private WindowManagerService mWindowManager;

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
        this.mSourceDisplayId = -1;
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
        this.mUsingVr2dDisplay = false;
        this.mIntentDelivered = false;
    }

    ActivityStarter(ActivityManagerService service, ActivityStackSupervisor supervisor) {
        this.mService = service;
        this.mSupervisor = supervisor;
        this.mInterceptor = new ActivityStartInterceptor(this.mService, this.mSupervisor);
        this.mUsingVr2dDisplay = false;
        if (this.mPerf == null) {
            this.mPerf = new BoostFramework();
        }
    }

    int startActivityLocked(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, TaskRecord inTask, String reason) {
        if (TextUtils.isEmpty(reason)) {
            throw new IllegalArgumentException("Need to specify a reason.");
        }
        this.mLastStartReason = reason;
        this.mLastStartActivityTimeMs = System.currentTimeMillis();
        this.mLastStartActivityRecord[0] = null;
        this.mLastStartActivityResult = startActivity(caller, intent, ephemeralIntent, resolvedType, aInfo, rInfo, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, this.mLastStartActivityRecord, inTask);
        if (outActivity != null) {
            outActivity[0] = this.mLastStartActivityRecord[0];
        }
        return this.mLastStartActivityResult != 102 ? this.mLastStartActivityResult : 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:70:0x0227  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0257  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x029a  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x034d  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x037c  */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x03c4  */
    /* JADX WARNING: Removed duplicated region for block: B:134:0x0433  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x03fa  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "ZhiYong.Lin@Plf.Framework, modify for secure protectWangLan@Plf.Framework, modify for ROM Data Collection", property = OppoRomType.ROM)
    final int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, TaskRecord inTask) {
        if (this.mHyp == null) {
            this.mHyp = Hypnus.getHypnus();
        }
        if (this.mHyp != null) {
            this.mHyp.hypnusSetAction(11, 500);
        }
        int err = 0;
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "startActivityLocked callingPid " + callingPid);
        }
        if (OppoAmsUtils.getInstance(this.mService).needToControlActivityStartFreq(intent)) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                Slog.i(TAG, "control start frequency {" + intent.toShortString(true, true, true, false) + "}");
            }
            if (SystemProperties.getBoolean(AlertWindowNotification.PROPERTY_PERMISSION_ENABLE, true)) {
                err = -80;
            }
        }
        Bundle verificationBundle = options != null ? options.popAppVerificationBundle() : null;
        ProcessRecord callerApp = null;
        if (caller != null) {
            callerApp = this.mService.getRecordForAppLocked(caller);
            if (callerApp.processName.equals(OppoFreeFormManagerService.FREEFORM_CALLER_PKG) || callerApp.processName.equals("com.coloros.gesture")) {
                this.mStartFromSystemuiOrGesture = true;
            }
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
                err = -94;
            }
        }
        int userId = aInfo != null ? UserHandle.getUserId(aInfo.applicationInfo.uid) : 0;
        if (intent.getData() != null) {
            String pkgNameInUri = "";
            try {
                pkgNameInUri = intent.getData().toString().split(":")[1];
            } catch (IndexOutOfBoundsException e) {
            }
            if (ActivityManagerService.DEBUG_COLOROS_AMS) {
                Slog.v(TAG, "pkgName in Uri: " + pkgNameInUri + "callingPkg: " + callingPackage);
            }
            if (OppoProcessWhiteListUtils.getInstance().getProcessWhiteList().contains(pkgNameInUri) && callingUid != 1000) {
                err = -94;
            }
        }
        if (OppoAppPhoneManager.getInstance().handleAppPhoneComing(aInfo)) {
            ActivityOptions.abort(options);
            return 102;
        }
        if (OppoAbnormalAppManager.getInstance().validStartActivity(aInfo)) {
            Slog.i(OppoAbnormalAppManager.TAG, "UL for activity " + aInfo + " : is R");
            err = 100;
        }
        try {
            String className = "";
            ComponentName componentName = intent.getComponent();
            if (componentName != null) {
                className = componentName.getClassName();
            }
            ActivityRecord sourceRecord;
            ActivityRecord resultRecord;
            String tempCls;
            int launchFlags;
            if ("com.android.packageinstaller.PackageInstallerActivity".equals(className) && AppGlobals.getPackageManager().prohibitChildInstallation(userId, true)) {
                err = 100;
                if (err != 100) {
                }
                ColorPackageManagerHelper.removePkgFromNotLaunchedList(aInfo.packageName, true);
                if (this.mSecrecyManagerInternal == null) {
                }
                Slog.e(TAG, aInfo + " is isInEncryptedAppList ");
                err = 100;
                if (err == 0) {
                }
                sourceRecord = null;
                resultRecord = null;
                tempCls = null;
                tempCls = intent.getComponent().getClassName().trim();
                if (tempCls != null) {
                }
                if (resultTo != null) {
                }
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                }
                launchFlags = intent.getFlags();
                if (requestCode < 0) {
                }
            } else {
                if ("com.android.packageinstaller.UninstallerActivity".equals(className) && AppGlobals.getPackageManager().prohibitChildInstallation(userId, false)) {
                    err = 100;
                }
                if (err != 100) {
                    if (OppoAppStartupManager.getInstance().handleStartActivity(intent, aInfo, callingPackage, callingUid, callingPid, this.mLastStartReason, userId)) {
                        err = 100;
                    }
                }
                if (!(aInfo == null || aInfo.packageName == null)) {
                    ColorPackageManagerHelper.removePkgFromNotLaunchedList(aInfo.packageName, true);
                }
                if (this.mSecrecyManagerInternal == null) {
                    this.mSecrecyManagerInternal = (SecrecyManagerInternal) LocalServices.getService(SecrecyManagerInternal.class);
                }
                if (this.mSecrecyManagerInternal != null && this.mSecrecyManagerInternal.isInEncryptedAppList(aInfo, callingPackage, callingUid, callingPid)) {
                    Slog.e(TAG, aInfo + " is isInEncryptedAppList ");
                    err = 100;
                }
                if (err == 0) {
                    Slog.i(TAG, "START u" + userId + " {" + intent.toShortString(true, true, true, false) + "} from uid " + callingUid + " and from pid " + callingPid);
                    this.mSupervisor.dataCollectionInfo(caller, callerApp, callingPid, callingUid, callingPackage, intent);
                    if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US")) {
                        this.mSupervisor.dataCollectionInfoExp(callerApp, callingPackage, intent);
                    }
                    this.mSupervisor.collectionStartUrlInfo(caller, callerApp, callingPid, callingUid, callingPackage, intent);
                }
                sourceRecord = null;
                resultRecord = null;
                tempCls = null;
                if (!(intent == null || intent.getComponent() == null || intent.getComponent().getClassName() == null)) {
                    tempCls = intent.getComponent().getClassName().trim();
                }
                if (tempCls != null) {
                    if (tempCls.equals("com.android.internal.app.ChooserActivity") && callingUid == 1000) {
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
                    if (!(sourceRecord == null || requestCode < 0 || (sourceRecord.finishing ^ 1) == 0)) {
                        resultRecord = sourceRecord;
                    }
                }
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.v(TAG, "startActivityLocked resultTo " + resultTo + " sourceRecord " + sourceRecord);
                }
                launchFlags = intent.getFlags();
                if (!((PhoneWindowManager.SYSTEM_UI_FLAG_APP_CUSTOM_NAVIGATION_BAR & launchFlags) == 0 || sourceRecord == null)) {
                    if (requestCode < 0) {
                        ActivityOptions.abort(options);
                        return -93;
                    }
                    resultRecord = sourceRecord.resultTo;
                    if (!(resultRecord == null || (resultRecord.isInStackLocked() ^ 1) == 0)) {
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
                if (err == 0 && intent.getComponent() == null) {
                    err = -91;
                }
                if (err == 0 && aInfo == null) {
                    err = -92;
                }
                if (sourceRecord != null && sourceRecord.getTask() == null) {
                    Slog.e(TAG, "sourceRecord.getTask() is null!!!");
                }
                if (!(err != 0 || sourceRecord == null || sourceRecord.getTask() == null || sourceRecord.getTask().voiceSession == null || (268435456 & launchFlags) != 0 || sourceRecord.info.applicationInfo.uid == aInfo.applicationInfo.uid)) {
                    try {
                        intent.addCategory("android.intent.category.VOICE");
                        if (!AppGlobals.getPackageManager().activitySupportsIntent(intent.getComponent(), intent, resolvedType)) {
                            Slog.w(TAG, "Activity being started in current voice task does not support voice: " + intent);
                            err = -97;
                        }
                    } catch (Throwable e2) {
                        Slog.w(TAG, "Failure checking voice capabilities", e2);
                        err = -97;
                    }
                }
                if (err == 0 && voiceSession != null) {
                    try {
                        if (!AppGlobals.getPackageManager().activitySupportsIntent(intent.getComponent(), intent, resolvedType)) {
                            Slog.w(TAG, "Activity being started in new voice task does not support: " + intent);
                            err = -97;
                        }
                    } catch (Throwable e22) {
                        Slog.w(TAG, "Failure checking voice capabilities", e22);
                        err = -97;
                    }
                }
                ActivityStack resultStack = resultRecord == null ? null : resultRecord.getStack();
                if (err != 0) {
                    if (resultRecord != null) {
                        resultStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
                    }
                    ActivityOptions.abort(options);
                    return err;
                }
                boolean abort = (this.mSupervisor.checkStartAnyActivityPermission(intent, aInfo, resultWho, requestCode, callingPid, callingUid, callingPackage, ignoreTargetSecurity, callerApp, resultRecord, resultStack, options) ^ 1) | (this.mService.mIntentFirewall.checkStartActivity(intent, callingUid, callingPid, resolvedType, aInfo.applicationInfo) ^ 1);
                if (!(aInfo == null || aInfo.applicationInfo == null || aInfo.applicationInfo.packageName == null)) {
                    if (ActivityManagerService.OPPO_LAUNCHER.equals(aInfo.applicationInfo.packageName) && callingUid != 1000 && OppoSplitWindowAppReader.isInTwoSecond()) {
                        Slog.d(TAG, "back_key start home app callingpkg: " + callingPackage);
                        this.mService.mHandler.removeMessages(511, callingPackage);
                        this.mService.mHandler.sendMessageDelayed(this.mService.mHandler.obtainMessage(511, UserHandle.getUserId(callingUid), -1, callingPackage), 1000);
                    }
                }
                if (!(aInfo == null || aInfo.applicationInfo == null || aInfo.applicationInfo.packageName == null)) {
                    String pName = aInfo.applicationInfo.packageName;
                    if (!(ActivityManagerService.OPPO_LAUNCHER.equals(callingPackage) || ("android".equals(callingPackage) ^ 1) == 0 || ("com.coloros.safecenter".equals(callingPackage) ^ 1) == 0 || (OPPO_RECENTS_PACKAGE_NAME.equals(callingPackage) ^ 1) == 0 || ((intent != null && intent.getIsFromGameSpace() == 1) || (OppoAppStartupManager.getInstance().isTenIntencept(callingPackage, intent) ^ 1) == 0 || ((intent != null && intent.getLaunchStackId() == 1) || !OppoMultiAppManagerUtil.getInstance().isMultiApp(pName) || (pName.equals(callingPackage) ^ 1) == 0)))) {
                        String tempClass = null;
                        if (!(intent == null || intent.getComponent() == null || intent.getComponent().getClassName() == null)) {
                            tempClass = intent.getComponent().getClassName().trim();
                        }
                        boolean isSkip = false;
                        if (intent.getAction() != null && "com.sina.weibo.sdk.action.ACTION_WEIBO_ACTIVITY".equals(intent.getAction())) {
                            isSkip = true;
                        }
                        if ("com.facebook.orca".equals(callingPackage) && tempClass != null && "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias".equals(tempClass) && intent.getType() != null && "application/instant-games".equals(intent.getType())) {
                            isSkip = true;
                        }
                        if ("com.coloros.speechassist".equals(callingPackage) && intent.getCategories() != null) {
                            for (String str : intent.getCategories()) {
                                if (str != null) {
                                    if (str.contains("USERID")) {
                                        isSkip = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (OppoMultiAppManagerUtil.getInstance().isInFilter(tempClass) || isSkip) {
                            Slog.v(TAG, "multi app: startLocked: callback continue");
                        } else {
                            this.mService.mHandler.sendMessageAtFrontOfQueue(this.mService.mHandler.obtainMessage(500, intent));
                            if (resultRecord != null) {
                                this.mSupervisor.mOppoSecureProtectUtils.setTempValue(resultRecord, resultWho, requestCode);
                            }
                            return 0;
                        }
                    }
                    if (!(!"android".equals(callingPackage) || intent == null || (intent.getFlags() & 512) == 0)) {
                        aInfo.taskAffinity = "coloros_multiapp_chooser";
                    }
                }
                if (this.mService.mController != null) {
                    try {
                        String pkgName = aInfo.applicationInfo.packageName;
                        if (pkgName != null && OppoMultiAppManagerUtil.getInstance().isMultiApp(pkgName)) {
                            Slog.d(TAG, "multi app: putExtra userId = " + userId + "   pkgName = " + pkgName);
                            intent.setOppoUserId(userId);
                        }
                        if (!(options == null || intent == null)) {
                            intent.setLaunchStackId(options.getLaunchStackId());
                        }
                        if (this.mService.mOppoActivityControlerScheduler != null) {
                            abort |= this.mService.mOppoActivityControlerScheduler.scheduleActivityStarting(intent, aInfo.applicationInfo.packageName) ^ 1;
                        } else if (OppoGameSpaceManager.getInstance().handleVideoComingNotification(intent, aInfo)) {
                            abort |= true;
                        } else {
                            abort |= this.mService.mController.activityStarting(intent, aInfo.applicationInfo.packageName) ^ 1;
                        }
                    } catch (RemoteException e3) {
                        this.mService.mController = null;
                    }
                }
                this.mInterceptor.setStates(userId, realCallingPid, realCallingUid, startFlags, callingPackage);
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
                    return 102;
                }
                if (this.mService.mPermissionReviewRequired && aInfo != null && this.mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(aInfo.packageName, userId)) {
                    IIntentSender target = this.mService.getIntentSenderLocked(2, callingPackage, callingUid, userId, null, null, 0, new Intent[]{intent}, new String[]{resolvedType}, 1342177280, null);
                    int flags = intent.getFlags();
                    Intent intent2 = new Intent("android.intent.action.REVIEW_PERMISSIONS");
                    intent2.setFlags(DumpState.DUMP_VOLUMES | flags);
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
                    if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                        int i;
                        String str2 = TAG;
                        StringBuilder append = new StringBuilder().append("START u").append(userId).append(" {").append(intent2.toShortString(true, true, true, false)).append("} from uid ").append(realCallingUid).append(" on display ");
                        if (this.mSupervisor.mFocusedStack == null) {
                            i = 0;
                        } else {
                            i = this.mSupervisor.mFocusedStack.mDisplayId;
                        }
                        Slog.i(str2, append.append(i).toString());
                    }
                }
                if (!(rInfo == null || rInfo.auxiliaryInfo == null)) {
                    intent = createLaunchIntent(rInfo.auxiliaryInfo, ephemeralIntent, callingPackage, verificationBundle, resolvedType, userId);
                    resolvedType = null;
                    callingUid = realCallingUid;
                    callingPid = realCallingPid;
                    aInfo = this.mSupervisor.resolveActivity(intent, rInfo, startFlags, null);
                }
                ActivityRecord r = this.mSupervisor.mOppoSecureProtectUtils.handleStartActivityLocked(this.mService, callerApp, callingPid, callingUid, callingPackage, intent, resolvedType, aInfo, this.mService.getGlobalConfiguration(), resultRecord, resultWho, requestCode, componentSpecified, voiceSession != null, this.mSupervisor, options, sourceRecord);
                r.callingPkg = callingPackage;
                if (outActivity != null) {
                    outActivity[0] = r;
                }
                if (r.appTimeTracker == null && sourceRecord != null) {
                    r.appTimeTracker = sourceRecord.appTimeTracker;
                }
                ActivityStack stack = this.mSupervisor.mFocusedStack;
                if (voiceSession == null && ((stack.mResumedActivity == null || stack.mResumedActivity.info.applicationInfo.uid != callingUid) && !OppoAppStartupManager.getInstance().isFromControlCenterPkg(callingPackage))) {
                    if ((this.mService.checkAppSwitchAllowedLocked(callingPid, callingUid, realCallingPid, realCallingUid, "Activity start") ^ 1) != 0) {
                        this.mPendingActivityLaunches.add(new PendingActivityLaunch(r, sourceRecord, startFlags, stack, callerApp));
                        ActivityOptions.abort(options);
                        return 100;
                    }
                }
                if (this.mService.mDidAppSwitch) {
                    this.mService.mAppSwitchesAllowedTime = 0;
                } else {
                    this.mService.mDidAppSwitch = true;
                }
                doPendingActivityLaunchesLocked(false);
                if (!(aInfo == null || aInfo.packageName == null || ColorPackageManagerHelper.getOppoFreezePackageState(aInfo.packageName, userId) != 2)) {
                    Slog.w(TAG, "oppo-enable " + aInfo.packageName + " before start from AMS ");
                    try {
                        this.mService.getPackageManagerInternalLocked().oppoUnFreezePackageInternal(aInfo.packageName, userId, 1, 0, "android");
                    } catch (Exception e4) {
                    }
                }
                return startActivity(r, sourceRecord, voiceSession, voiceInteractor, startFlags, true, options, inTask, outActivity);
            }
        } catch (Throwable re) {
            throw new RuntimeException("Package manager has died", re);
        } catch (Throwable e5) {
            Slog.w(TAG, "failed for check prohibitChildInstallation", e5);
        }
    }

    private Intent createLaunchIntent(AuxiliaryResolveInfo auxiliaryResponse, Intent originalIntent, String callingPackage, Bundle verificationBundle, String resolvedType, int userId) {
        if (auxiliaryResponse.needsPhaseTwo) {
            this.mService.getPackageManagerInternalLocked().requestInstantAppResolutionPhaseTwo(auxiliaryResponse, originalIntent, resolvedType, callingPackage, verificationBundle, userId);
        }
        return InstantAppResolver.buildEphemeralInstallerIntent("android.intent.action.INSTALL_INSTANT_APP_PACKAGE", originalIntent, auxiliaryResponse.failureIntent, callingPackage, verificationBundle, resolvedType, userId, auxiliaryResponse.packageName, auxiliaryResponse.splitName, auxiliaryResponse.installFailureActivity, auxiliaryResponse.versionCode, auxiliaryResponse.token, auxiliaryResponse.needsPhaseTwo);
    }

    void postStartActivityProcessing(ActivityRecord r, int result, int prevFocusedStackId, ActivityRecord sourceRecord, ActivityStack targetStack) {
        if (!ActivityManager.isStartResultFatalError(result)) {
            if (result == 2 && (this.mSupervisor.mWaitingActivityLaunched.isEmpty() ^ 1) != 0) {
                this.mSupervisor.reportTaskToFrontNoLaunch(this.mStartActivity);
            }
            int startedActivityStackId = -1;
            ActivityStack currentStack = r.getStack();
            if (currentStack != null) {
                startedActivityStackId = currentStack.mStackId;
            } else if (this.mTargetStack != null) {
                startedActivityStackId = targetStack.mStackId;
            }
            if (startedActivityStackId == 3) {
                ActivityStack homeStack = this.mSupervisor.getStack(0);
                if (homeStack != null ? homeStack.isVisible() : false) {
                    if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                        Slog.d(TAG, "Scheduling recents launch.");
                    }
                    this.mWindowManager.showRecentApps(true);
                }
                return;
            }
            boolean clearedTask = (this.mLaunchFlags & 268468224) == 268468224 ? this.mReuseTask != null : false;
            if (startedActivityStackId == 4 && (result == 2 || result == 3 || clearedTask)) {
                this.mService.mTaskChangeNotificationController.notifyPinnedActivityRestartAttempt(clearedTask);
            }
        }
    }

    void startHomeActivityLocked(Intent intent, ActivityInfo aInfo, String reason) {
        this.mSupervisor.moveHomeStackTaskToTop(reason);
        this.mLastHomeActivityStartResult = startActivityLocked(null, intent, null, null, aInfo, null, null, null, null, null, 0, 0, 0, null, 0, 0, 0, null, false, false, this.mLastHomeActivityStartRecord, null, "startHomeActivity: " + reason);
        if (this.mSupervisor.inResumeTopActivity) {
            this.mSupervisor.scheduleResumeTopActivities();
        }
    }

    void startConfirmCredentialIntent(Intent intent, Bundle optionsBundle) {
        ActivityOptions options;
        intent.addFlags(276840448);
        if (optionsBundle != null) {
            options = new ActivityOptions(optionsBundle);
        } else {
            options = ActivityOptions.makeBasic();
        }
        options.setLaunchTaskId(this.mSupervisor.getHomeActivity().getTask().taskId);
        this.mService.mContext.startActivityAsUser(intent, options.toBundle(), UserHandle.CURRENT);
    }

    /* JADX WARNING: Removed duplicated region for block: B:107:0x029e  */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x0846 A:{Catch:{ all -> 0x0974 }} */
    /* JADX WARNING: Removed duplicated region for block: B:281:0x0871 A:{Catch:{ all -> 0x0974 }} */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x0846 A:{Catch:{ all -> 0x0974 }} */
    /* JADX WARNING: Removed duplicated region for block: B:281:0x0871 A:{Catch:{ all -> 0x0974 }} */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x02e6  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x02c4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final int startActivityMayWait(IApplicationThread caller, int callingUid, String callingPackage, Intent intent, String resolvedType, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, WaitResult outResult, Configuration globalConfig, Bundle bOptions, boolean ignoreTargetSecurity, int userId, TaskRecord inTask, String reason) {
        Throwable th;
        if (intent == null || !intent.hasFileDescriptors()) {
            ArrayList<TaskRecord> mTaskHistory;
            int taskNdx;
            TaskRecord task;
            ResolveInfo rInfo;
            ActivityInfo aInfo;
            this.mSupervisor.mActivityMetricsLogger.notifyActivityLaunching();
            boolean componentSpecified = intent.getComponent() != null;
            if (!(intent == null || intent.getComponent() == null)) {
                String cpnClassName = intent.getComponent().getClassName();
                if (OppoListManager.getInstance().isFastAppWechatPayCpn(cpnClassName)) {
                    intent.setComponent(OppoListManager.getInstance().replaceFastAppWechatPayCpn(cpnClassName));
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.i(TAG, "startActivityMayWait intent = " + intent);
                    }
                }
            }
            Intent intent2 = new Intent(intent);
            Intent intent3 = new Intent(intent);
            if (this.mService.isSleepingLocked() && (intent3.getFlags() & 2048) != 0) {
                Slog.w(TAG, "startActivityMayWait mService.mIgnoreSleepCheckLater " + this.mService.mIgnoreSleepCheckLater);
                if (this.mService.mIgnoreSleepCheckLater) {
                    this.mService.mHandler.removeMessages(DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR);
                } else {
                    this.mService.mIgnoreSleepCheckLater = true;
                }
                this.mService.mHandler.sendMessageDelayed(this.mService.mHandler.obtainMessage(DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR), FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK);
            }
            if (componentSpecified && intent3.getData() != null && "android.intent.action.VIEW".equals(intent3.getAction()) && this.mService.getPackageManagerInternalLocked().isInstantAppInstallerComponent(intent3.getComponent())) {
                intent3.setComponent(null);
                componentSpecified = false;
            }
            if (intent3.getCategories() != null && intent3.getCategories().contains("com.multiple.launcher")) {
                intent3.removeCategory("com.multiple.launcher");
                userId = OppoMultiAppManager.USER_ID;
            }
            if (callingPackage != null && "com.tencent.mm".equals(callingPackage) && componentSpecified && intent3.getComponent().getClassName() != null && "com.sina.weibo.wxapi.WXEntryActivity".equals(intent3.getComponent().getClassName())) {
                mTaskHistory = this.mSupervisor.mFocusedStack.getTaskHistory();
                for (taskNdx = mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                    task = (TaskRecord) mTaskHistory.get(taskNdx);
                    if (task.affinity != null && "com.sina.weibo".equals(task.affinity)) {
                        userId = task.userId;
                        break;
                    }
                }
            }
            if (callingPackage != null && "com.eg.android.AlipayGphone".equals(callingPackage) && componentSpecified && intent3.getComponent().getClassName() != null && ("com.taobao.login4android.activity.AlipaySSOResultActivity".equals(intent3.getComponent().getClassName()) || "com.taobao.taobao.apshare.ShareEntryActivity".equals(intent3.getComponent().getClassName()))) {
                mTaskHistory = this.mSupervisor.mFocusedStack.getTaskHistory();
                for (taskNdx = mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                    task = (TaskRecord) mTaskHistory.get(taskNdx);
                    if (task.affinity != null && "com.taobao.taobao".equals(task.affinity)) {
                        userId = task.userId;
                        break;
                    }
                }
            }
            if (999 == userId && (componentSpecified ^ 1) != 0 && intent3.getPackage() == null && intent3.getType() == null && intent3.getCategories() == null && callingPackage != null && "com.taobao.taobao".equals(callingPackage) && intent3.getAction() != null && "android.intent.action.VIEW".equals(intent3.getAction()) && intent3.getData() != null && intent3.getDataString().startsWith("http")) {
                userId = 0;
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
                                profileLockedAndParentUnlockingOrUnlocked = userManager.isUserUnlockingOrUnlocked(userId) ^ 1;
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
                } else if (OppoMultiAppManagerUtil.getInstance().isMultiApp(callingPackage) && aInfo == null) {
                    userId = 0;
                    aInfo = this.mSupervisor.resolveActivity(intent3, resolvedType, startFlags, profilerInfo, 0);
                    Slog.i(TAG, "multi app: startActivityMayWait change userId to " + 0 + "  aInfo = " + aInfo);
                    aInfo2 = aInfo;
                } else {
                    aInfo2 = aInfo;
                }
                if (!(userId != 999 || aInfo2 == null || aInfo2.applicationInfo == null)) {
                    if (!OppoMultiAppManagerUtil.getInstance().isMultiApp(aInfo2.applicationInfo.packageName)) {
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
                    } else if (OppoSplitWindowAppReader.getInstance().isInConfigList(pName)) {
                        if (aInfo2.resizeMode != 2 && aInfo2.resizeMode != 3 && aInfo2.resizeMode != 1) {
                            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                Slog.v(TAG, "split app: " + pName + " in configList");
                            }
                            aInfo2.resizeMode = 4;
                        } else if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            Slog.w(TAG, "split app: " + pName + "already resizable but in configList!");
                        }
                    }
                }
                ActivityOptions options = ActivityOptions.fromBundle(bOptions);
                synchronized (this.mService) {
                    try {
                        int callingPid;
                        ActivityRecord[] outRecord;
                        int res;
                        ActivityManagerService.boostPriorityForLockedSection();
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
                        ActivityStack stack = this.mSupervisor.mFocusedStack;
                        boolean z = globalConfig != null ? this.mService.getGlobalConfiguration().diff(globalConfig) != 0 : false;
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
                                    if (sites != null) {
                                        if (sites.contains(site)) {
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
                            }
                            if (keyword != null) {
                                this.mService.mHandler.sendMessageAtFrontOfQueue(this.mService.mHandler.obtainMessage(CompatibilityHelper.FORCE_DELAY_TO_USE_POST, keyword));
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return 0;
                            }
                        }
                        if (aInfo2 != null) {
                            if ((aInfo2.applicationInfo.privateFlags & 2) != 0) {
                                if (aInfo2.processName.equals(aInfo2.applicationInfo.packageName)) {
                                    ProcessRecord heavy = this.mService.mHeavyWeightProcess;
                                    if (heavy == null) {
                                        aInfo = aInfo2;
                                        rInfo2 = rInfo;
                                        intent = intent3;
                                    } else if (!(heavy.info.uid == aInfo2.applicationInfo.uid && (heavy.processName.equals(aInfo2.processName) ^ 1) == 0)) {
                                        int appCallingUid = callingUid;
                                        if (caller != null) {
                                            ProcessRecord callerApp = this.mService.getRecordForAppLocked(caller);
                                            if (callerApp != null) {
                                                appCallingUid = callerApp.info.uid;
                                            } else {
                                                Slog.w(TAG, "Unable to find app for caller " + caller + " (pid=" + callingPid + ") when starting: " + intent3.toString());
                                                ActivityOptions.abort(options);
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                return -94;
                                            }
                                        }
                                        IIntentSender target = this.mService.getIntentSenderLocked(2, "android", appCallingUid, userId, null, null, 0, new Intent[]{intent3}, new String[]{resolvedType}, 1342177280, null);
                                        Intent newIntent = new Intent();
                                        if (requestCode >= 0) {
                                            newIntent.putExtra("has_result", true);
                                        }
                                        newIntent.putExtra("intent", new IntentSender(target));
                                        if (heavy.activities.size() > 0) {
                                            ActivityRecord hist = (ActivityRecord) heavy.activities.get(0);
                                            newIntent.putExtra("cur_app", hist.packageName);
                                            newIntent.putExtra("cur_task", hist.getTask().taskId);
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
                                                    if (OppoMultiAppManagerUtil.getInstance().isMultiApp(callingPackage) && rInfo2 == null) {
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
                                            rInfo2 = rInfo;
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            throw th;
                                        }
                                    }
                                    outRecord = new ActivityRecord[1];
                                    res = startActivityLocked(caller, intent, intent2, resolvedType, aInfo, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, outRecord, inTask, reason);
                                    Binder.restoreCallingIdentity(origId);
                                    if (stack.mConfigWillChange) {
                                        this.mService.enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateConfiguration()");
                                        stack.mConfigWillChange = false;
                                        if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                            Slog.v(TAG_CONFIGURATION, "Updating to new configuration after starting activity.");
                                        }
                                        this.mService.updateConfigurationLocked(globalConfig, null, false);
                                    }
                                    if (outResult != null) {
                                        outResult.result = res;
                                        if (res == 0) {
                                            this.mSupervisor.mWaitingActivityLaunched.add(outResult);
                                            do {
                                                try {
                                                    this.mService.wait();
                                                } catch (InterruptedException e) {
                                                }
                                                try {
                                                    if (outResult.result == 2 || (outResult.timeout ^ 1) == 0) {
                                                    }
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                    throw th;
                                                }
                                            } while (outResult.who == null);
                                            if (outResult.result == 2) {
                                                res = 2;
                                            }
                                        }
                                        if (res == 2) {
                                            ActivityRecord r = outRecord[0];
                                            if (!r.nowVisible || r.state != ActivityState.RESUMED) {
                                                outResult.thisTime = SystemClock.uptimeMillis();
                                                this.mSupervisor.waitActivityVisible(r.realActivity, outResult);
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
                                                outResult.who = r.realActivity;
                                                outResult.totalTime = 0;
                                                outResult.thisTime = 0;
                                            }
                                        }
                                    }
                                    this.mSupervisor.mActivityMetricsLogger.notifyActivityLaunched(res, outRecord[0]);
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return res;
                                }
                                aInfo = aInfo2;
                                rInfo2 = rInfo;
                                intent = intent3;
                                outRecord = new ActivityRecord[1];
                                res = startActivityLocked(caller, intent, intent2, resolvedType, aInfo, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, outRecord, inTask, reason);
                                Binder.restoreCallingIdentity(origId);
                                if (stack.mConfigWillChange) {
                                }
                                if (outResult != null) {
                                }
                                this.mSupervisor.mActivityMetricsLogger.notifyActivityLaunched(res, outRecord[0]);
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return res;
                            }
                        }
                        aInfo = aInfo2;
                        rInfo2 = rInfo;
                        intent = intent3;
                        outRecord = new ActivityRecord[1];
                        res = startActivityLocked(caller, intent, intent2, resolvedType, aInfo, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, outRecord, inTask, reason);
                        Binder.restoreCallingIdentity(origId);
                        if (stack.mConfigWillChange) {
                        }
                        if (outResult != null) {
                        }
                        this.mSupervisor.mActivityMetricsLogger.notifyActivityLaunched(res, outRecord[0]);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return res;
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

    final int startActivities(IApplicationThread caller, int callingUid, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle bOptions, int userId, String reason) {
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
                                int res = startActivityLocked(caller, intent2, null, resolvedTypes[i], aInfo, null, null, null, resultTo, null, -1, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, 0, ActivityOptions.fromBundle(i == intents.length + -1 ? bOptions : null), false, componentSpecified, outActivity, null, reason);
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

    void sendPowerHintForLaunchStartIfNeeded(boolean forceSend, ActivityRecord targetActivity) {
        boolean sendHint = forceSend;
        if (!forceSend) {
            ActivityRecord resumedActivity = this.mSupervisor.getResumedActivityLocked();
            if (resumedActivity == null || resumedActivity.app == null) {
                sendHint = true;
            } else {
                sendHint = resumedActivity.app.equals(targetActivity.app) ^ 1;
            }
        }
        if (sendHint && this.mService.mLocalPowerManager != null) {
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

    private int startActivity(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask, ActivityRecord[] outActivity) {
        int result = -96;
        try {
            this.mService.mWindowManager.deferSurfaceLayout();
            result = startActivityUnchecked(r, sourceRecord, voiceSession, voiceInteractor, startFlags, doResume, options, inTask, outActivity);
            if (!(ActivityManager.isStartResultSuccessful(result) || this.mStartActivity.getTask() == null)) {
                this.mStartActivity.getTask().removeActivity(this.mStartActivity);
            }
            this.mService.mWindowManager.continueSurfaceLayout();
            postStartActivityProcessing(r, result, this.mSupervisor.getLastStack().mStackId, this.mSourceRecord, this.mTargetStack);
            return result;
        } catch (Throwable th) {
            if (!(ActivityManager.isStartResultSuccessful(result) || this.mStartActivity.getTask() == null)) {
                this.mStartActivity.getTask().removeActivity(this.mStartActivity);
            }
            this.mService.mWindowManager.continueSurfaceLayout();
        }
    }

    /* JADX WARNING: Missing block: B:221:0x058d, code:
            if (r18.contains("com.instagram.fileprovider") != false) goto L_0x058f;
     */
    /* JADX WARNING: Missing block: B:231:0x05c4, code:
            if (r17.equals("android.intent.action.SEND_MULTIPLE") != false) goto L_0x05c6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int startActivityUnchecked(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask, ActivityRecord[] outActivity) {
        ActivityRecord top;
        TaskRecord freeformTask = null;
        if (options != null && options.getLaunchStackId() == 2) {
            freeformTask = inTask;
            inTask = null;
        }
        setInitialState(r, options, inTask, doResume, startFlags, sourceRecord, voiceSession, voiceInteractor);
        computeLaunchingTaskFlags();
        computeSourceStack();
        this.mIntent.setFlags(this.mLaunchFlags);
        ActivityRecord reusedActivity = getReusableIntentActivity();
        if (options != null) {
            int launchStackId = options.getLaunchStackId();
            if (launchStackId == 2) {
                if (!(reusedActivity == null || reusedActivity.getTask() == null)) {
                    Slog.v(TAG, "oppo freeform inTask: " + inTask + ", getTask:" + reusedActivity.getTask() + " ,reusedActivity:" + reusedActivity);
                    inTask = reusedActivity.getTask();
                }
                if (inTask != null) {
                    inTask.reparent(launchStackId, true, 0, true, true, "startActivityForFreeform");
                } else if (freeformTask != null) {
                    freeformTask.reparent(launchStackId, true, 0, true, true, "startActivityForFreeform");
                }
            }
        }
        if (this.mStartFromSystemuiOrGesture && reusedActivity != null && reusedActivity.shortComponentName.equals("com.oppo.camera/.Camera")) {
            reusedActivity.setShowWhenLocked(true);
        }
        this.mStartFromSystemuiOrGesture = false;
        int preferredLaunchStackId = this.mOptions != null ? this.mOptions.getLaunchStackId() : -1;
        int preferredLaunchDisplayId = this.mOptions != null ? this.mOptions.getLaunchDisplayId() : 0;
        if (reusedActivity != null) {
            if (this.mSupervisor.isLockTaskModeViolation(reusedActivity.getTask(), (this.mLaunchFlags & 268468224) == 268468224)) {
                this.mSupervisor.showLockTaskToast();
                Slog.e(TAG, "startActivityUnchecked: Attempt to violate Lock Task Mode");
                return 101;
            }
            if (this.mStartActivity.getTask() == null) {
                this.mStartActivity.setTask(reusedActivity.getTask());
            }
            if (reusedActivity.getTask().intent == null) {
                reusedActivity.getTask().setIntent(this.mStartActivity);
            }
            if ((this.mLaunchFlags & 67108864) != 0 || isDocumentLaunchesIntoExisting(this.mLaunchFlags) || this.mLaunchSingleInstance || this.mLaunchSingleTask) {
                TaskRecord task = reusedActivity.getTask();
                top = task.performClearTaskForReuseLocked(this.mStartActivity, this.mLaunchFlags);
                if (reusedActivity.getTask() == null) {
                    reusedActivity.setTask(task);
                }
                if (top != null) {
                    if (top.frontOfTask) {
                        top.getTask().setIntent(this.mStartActivity);
                    }
                    deliverNewIntent(top);
                }
            }
            sendPowerHintForLaunchStartIfNeeded(false, reusedActivity);
            reusedActivity = setTargetStackAndMoveToFrontIfNeeded(reusedActivity);
            ActivityRecord outResult = (outActivity == null || outActivity.length <= 0) ? null : outActivity[0];
            if (outResult != null && (outResult.finishing || outResult.noDisplay)) {
                outActivity[0] = reusedActivity;
            }
            if ((this.mStartFlags & 1) != 0) {
                resumeTargetStackIfNeeded();
                return 1;
            }
            setTaskFromIntentActivity(reusedActivity);
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(ActivityStackSupervisor.TAG_TASKS, "mAddingToTask: " + this.mAddingToTask + " mReuseTask " + this.mReuseTask + " r.packageName " + r.packageName);
            }
            if (!this.mAddingToTask && this.mReuseTask == null && r.packageName != null && (this.mLaunchFlags & Integer.MIN_VALUE) == 0) {
                ActivityRecord topActivity = null;
                if (!(reusedActivity == null || reusedActivity.task == null)) {
                    topActivity = reusedActivity.task.getTopActivity();
                }
                boolean skip = false;
                if (topActivity != null) {
                    if (ActivityManagerDebugConfig.DEBUG_PERMISSION) {
                        Slog.i(TAG, "topActivity.packageName = " + topActivity.packageName);
                    }
                    if (!(topActivity.packageName == null || r.packageName == null || !r.packageName.equals("com.coloros.safecenter") || (r.packageName.equals(topActivity.packageName) ^ 1) == 0 || (topActivity.packageName.equals("com.coloros.securitypermission") ^ 1) == 0)) {
                        this.mTargetStack.destroyActivityLocked(topActivity, true, "secure protect");
                        this.mTargetStack.removeActivityFromHistoryLocked(topActivity, "secure protect");
                        if (reusedActivity.task.mActivities.isEmpty()) {
                            this.mTargetStack.removeTask(reusedActivity.task, "secure protect");
                        }
                        skip = true;
                        if (ActivityManagerDebugConfig.DEBUG_PERMISSION) {
                            Slog.i(TAG, "secure protect remove!  r.packageName = " + r.packageName + "  topActivity.packageName = " + topActivity.packageName);
                        }
                    }
                }
                if (!skip) {
                    resumeTargetStackIfNeeded();
                    if (outActivity != null && outActivity.length > 0) {
                        outActivity[0] = reusedActivity;
                    }
                    if (ActivityManagerDebugConfig.DEBUG_OPPO_FREEFORM) {
                        Slog.v(TAG, "oppo freeform startActivityUnchecked: " + reusedActivity + " , mReuseTask: " + this.mReuseTask + " ,r: " + r + " , mTargetStack: " + this.mTargetStack);
                    }
                    return 2;
                }
            }
        }
        if (this.mStartActivity.packageName == null) {
            ActivityStack sourceStack = this.mStartActivity.resultTo != null ? this.mStartActivity.resultTo.getStack() : null;
            if (sourceStack != null) {
                sourceStack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
            }
            ActivityOptions.abort(this.mOptions);
            return -92;
        }
        boolean dontStart;
        ActivityStack topStack = this.mSupervisor.mFocusedStack;
        ActivityRecord topFocused = topStack.topActivity();
        top = topStack.topRunningNonDelayedActivityLocked(this.mNotTop);
        if (top == null || this.mStartActivity.resultTo != null || !top.realActivity.equals(this.mStartActivity.realActivity) || top.userId != this.mStartActivity.userId || top.app == null || top.app.thread == null) {
            dontStart = false;
        } else if ((this.mLaunchFlags & 536870912) != 0 || this.mLaunchSingleTop) {
            dontStart = true;
        } else {
            dontStart = this.mLaunchSingleTask;
        }
        if (dontStart) {
            topStack.mLastPausedActivity = null;
            if (this.mDoResume) {
                this.mSupervisor.resumeFocusedStackTopActivityLocked();
            }
            ActivityOptions.abort(this.mOptions);
            if ((this.mStartFlags & 1) != 0) {
                return 1;
            }
            deliverNewIntent(top);
            this.mSupervisor.handleNonResizableTaskIfNeeded(top.getTask(), preferredLaunchStackId, preferredLaunchDisplayId, topStack.mStackId);
            return 3;
        }
        boolean newTask = false;
        TaskRecord taskToAffiliate = (!this.mLaunchTaskBehind || this.mSourceRecord == null) ? null : this.mSourceRecord.getTask();
        int result = 0;
        if (this.mStartActivity.resultTo == null && this.mInTask == null && (this.mAddingToTask ^ 1) != 0 && (this.mLaunchFlags & 268435456) != 0) {
            newTask = true;
            String packageName = this.mService.mContext.getPackageName();
            if (this.mPerf != null) {
                this.mStartActivity.perfActivityBoostHandler = this.mPerf.perfHint(4225, packageName, -1, 1);
            }
            result = setTaskFromReuseOrCreateNewTask(taskToAffiliate, preferredLaunchStackId, topStack);
        } else if (this.mSourceRecord != null) {
            result = setTaskFromSourceRecord();
        } else if (this.mInTask != null) {
            result = setTaskFromInTask();
        } else {
            setTaskToCurrentTopOrCreateNewTask();
        }
        if (result != 0) {
            return result;
        }
        Intent tempIntent = this.mSupervisor.mOppoSecureProtectUtils.secureIntent(this.mCallingUid, sourceRecord, this.mIntent);
        if (tempIntent == null) {
            this.mService.grantUriPermissionFromIntentLocked(this.mCallingUid, this.mStartActivity.packageName, this.mIntent, this.mStartActivity.getUriPermissionsLocked(), this.mStartActivity.userId);
        } else {
            this.mService.grantUriPermissionFromIntentLocked(this.mCallingUid, this.mStartActivity.packageName, tempIntent, this.mStartActivity.getUriPermissionsLocked(), this.mStartActivity.userId);
        }
        if (!(UserHandle.getUserId(this.mCallingUid) != OppoMultiAppManager.USER_ID || this.mIntent == null || this.mIntent.getAction() == null)) {
            String pkgName;
            if (sourceRecord == null || sourceRecord.packageName == null) {
                pkgName = this.mService.getPackageManagerInternalLocked().getNameForUid(this.mCallingUid);
            } else {
                pkgName = sourceRecord.packageName;
            }
            String action = this.mIntent.getAction();
            String clipdatastring = null;
            if (this.mIntent.getClipData() != null) {
                clipdatastring = this.mIntent.getClipData().toString();
            }
            String data = this.mIntent.getDataString();
            if (pkgName != null && OppoMultiAppManagerUtil.getInstance().isMultiApp(pkgName)) {
                if ((clipdatastring == null || !clipdatastring.contains(pkgName)) && (data == null || !data.contains(pkgName))) {
                    if (clipdatastring != null) {
                    }
                }
                if (!action.equals("android.intent.action.SEND")) {
                    if (!action.equals("android.intent.action.VIEW")) {
                        if (!action.equals("android.media.action.IMAGE_CAPTURE")) {
                            if (!action.equals("android.media.action.VIDEO_CAPTURE")) {
                            }
                        }
                    }
                }
                this.mIntent.fixUris(OppoMultiAppManager.USER_ID);
            }
        }
        this.mService.grantEphemeralAccessLocked(this.mStartActivity.userId, this.mIntent, this.mStartActivity.appInfo.uid, UserHandle.getAppId(this.mCallingUid));
        if (this.mSourceRecord != null) {
            this.mStartActivity.getTask().setTaskToReturnTo(this.mSourceRecord);
        }
        if (newTask) {
            EventLog.writeEvent(EventLogTags.AM_CREATE_TASK, new Object[]{Integer.valueOf(this.mStartActivity.userId), Integer.valueOf(this.mStartActivity.getTask().taskId)});
        }
        ActivityStack.logStartActivity(EventLogTags.AM_CREATE_ACTIVITY, this.mStartActivity, this.mStartActivity.getTask());
        this.mTargetStack.mLastPausedActivity = null;
        sendPowerHintForLaunchStartIfNeeded(false, this.mStartActivity);
        OppoAmsUtils.getInstance(this.mService).speedupSpecialAct(this.mStartActivity);
        this.mTargetStack.startActivityLocked(this.mStartActivity, topFocused, newTask, this.mKeepCurTransition, this.mOptions);
        if (this.mDoResume) {
            ActivityRecord topTaskActivity = this.mStartActivity.getTask().topRunningActivityLocked();
            if (this.mTargetStack.isFocusable() && (topTaskActivity == null || !topTaskActivity.mTaskOverlay || this.mStartActivity == topTaskActivity)) {
                if (this.mTargetStack.isFocusable() && (this.mSupervisor.isFocusedStack(this.mTargetStack) ^ 1) != 0) {
                    this.mTargetStack.moveToFront("startActivityUnchecked");
                }
                this.mSupervisor.resumeFocusedStackTopActivityLocked(this.mTargetStack, this.mStartActivity, this.mOptions);
            } else {
                this.mTargetStack.ensureActivitiesVisibleLocked(null, 0, false);
                this.mWindowManager.executeAppTransition();
            }
        } else {
            this.mTargetStack.addRecentActivityLocked(this.mStartActivity);
        }
        this.mSupervisor.updateUserStackLocked(this.mStartActivity.userId, this.mTargetStack);
        this.mSupervisor.handleNonResizableTaskIfNeeded(this.mStartActivity.getTask(), preferredLaunchStackId, preferredLaunchDisplayId, this.mTargetStack.mStackId);
        if (this.mTargetStack.mStackId == 2 && this.mStartActivity.info != null && this.mStartActivity.info.name != null && OppoFreeFormManagerService.getInstance().inFullscreenCpnList(this.mStartActivity.info.name)) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.i(TAG, "oppo freeform: startActivityUnchecked " + this.mStartActivity.info.name);
            }
            this.mStartActivity.mIsFreeformFullscreen = true;
            this.mSupervisor.moveTasksToFullscreenStackLocked(2, true);
        }
        if (!(this.mStartActivity.info == null || this.mStartActivity.info.name == null || !OppoFreeFormManagerService.getInstance().inNextNeedFullscreenCpnList(this.mStartActivity.info.name))) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.i(TAG, "oppo freeform: startActivityUnchecked t1 " + this.mStartActivity.info.name);
            }
            ActivityStack stack = this.mSupervisor.getStack(2);
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.i(TAG, "oppo freeform: startActivityUnchecked t1 000 stack = " + stack);
            }
            if (stack != null) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG, "oppo freeform: startActivityUnchecked t2 " + this.mStartActivity.info.name);
                }
                this.mSupervisor.moveTasksToFullscreenStackLocked(2, false);
            }
        }
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
        this.mSourceDisplayId = getSourceDisplayId(this.mSourceRecord, this.mStartActivity);
        this.mLaunchBounds = getOverrideBounds(r, options, inTask);
        this.mLaunchSingleTop = r.launchMode == 1;
        this.mLaunchSingleInstance = r.launchMode == 3;
        this.mLaunchSingleTask = r.launchMode == 2;
        this.mLaunchFlags = adjustLaunchFlagsToDocumentMode(r, this.mLaunchSingleInstance, this.mLaunchSingleTask, this.mIntent.getFlags());
        boolean z = (!r.mLaunchTaskBehind || (this.mLaunchSingleTask ^ 1) == 0 || (this.mLaunchSingleInstance ^ 1) == 0) ? false : (this.mLaunchFlags & DumpState.DUMP_FROZEN) != 0;
        this.mLaunchTaskBehind = z;
        sendNewTaskResultRequestIfNeeded();
        if ((this.mLaunchFlags & DumpState.DUMP_FROZEN) != 0 && r.resultTo == null) {
            this.mLaunchFlags |= 268435456;
        }
        if (r.shortComponentName.equals("com.google.android.calendar/.launch.oobe.WhatsNewFullScreen")) {
            this.mLaunchFlags |= 603979776;
        }
        if ((this.mLaunchFlags & 268435456) != 0 && (this.mLaunchTaskBehind || r.info.documentLaunchMode == 2)) {
            this.mLaunchFlags |= 134217728;
        }
        this.mSupervisor.mUserLeaving = (this.mLaunchFlags & DumpState.DUMP_DOMAIN_PREFERRED) == 0;
        if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
            Slog.v(TAG_USER_LEAVING, "startActivity() => mUserLeaving=" + this.mSupervisor.mUserLeaving);
        }
        this.mDoResume = doResume;
        if (!(doResume && (r.okToShowLocked() ^ 1) == 0)) {
            r.delayedResume = true;
            this.mDoResume = false;
        }
        if (!(this.mOptions == null || this.mOptions.getLaunchTaskId() == -1 || !this.mOptions.getTaskOverlay())) {
            r.mTaskOverlay = true;
            if (!this.mOptions.canTaskOverlayResume()) {
                TaskRecord task = this.mSupervisor.anyTaskForIdLocked(this.mOptions.getLaunchTaskId());
                ActivityRecord top = task != null ? task.getTopActivity() : null;
                if (!(top == null || top.state == ActivityState.RESUMED)) {
                    this.mDoResume = false;
                    this.mAvoidMoveToFront = true;
                }
            }
        }
        this.mNotTop = (this.mLaunchFlags & 16777216) != 0 ? r : null;
        this.mInTask = inTask;
        if (!(inTask == null || (inTask.inRecents ^ 1) == 0)) {
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
        this.mNoAnimation = (this.mLaunchFlags & 65536) != 0;
    }

    private void sendNewTaskResultRequestIfNeeded() {
        boolean isOppoLauncher = this.mStartActivity.packageName != null ? ActivityManagerService.OPPO_LAUNCHER.equals(this.mStartActivity.packageName) : false;
        ActivityStack sourceStack = this.mStartActivity.resultTo != null ? this.mStartActivity.resultTo.getStack() : null;
        if (!isOppoLauncher && sourceStack != null && (this.mLaunchFlags & 268435456) != 0 && (this.mLaunchFlags & Integer.MIN_VALUE) == 0) {
            Slog.w(TAG, "Activity is launching as a new task, so cancelling activity result.");
            sourceStack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
            this.mStartActivity.resultTo = null;
        }
    }

    private void computeLaunchingTaskFlags() {
        if (this.mSourceRecord != null || this.mInTask == null || this.mInTask.getStack() == null) {
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
                Intent intent;
                Slog.w(TAG, "startActivity called from finishing " + this.mSourceRecord + "; forcing " + "Intent.FLAG_ACTIVITY_NEW_TASK for: " + this.mIntent);
                this.mLaunchFlags |= 268435456;
                this.mNewTaskInfo = this.mSourceRecord.info;
                TaskRecord sourceTask = this.mSourceRecord.getTask();
                if (sourceTask != null) {
                    intent = sourceTask.intent;
                } else {
                    intent = null;
                }
                this.mNewTaskIntent = intent;
            }
            this.mSourceRecord = null;
            this.mSourceStack = null;
        } else {
            this.mSourceStack = this.mSourceRecord.getStack();
        }
    }

    private ActivityRecord getReusableIntentActivity() {
        boolean putIntoExistingTask;
        int i = 0;
        if (((this.mLaunchFlags & 268435456) == 0 || (this.mLaunchFlags & 134217728) != 0) && !this.mLaunchSingleInstance) {
            putIntoExistingTask = this.mLaunchSingleTask;
        } else {
            putIntoExistingTask = true;
        }
        if (this.mInTask == null && this.mStartActivity.resultTo == null) {
            i = 1;
        }
        putIntoExistingTask &= i;
        ActivityRecord intentActivity = null;
        if (this.mOptions != null && this.mOptions.getLaunchTaskId() != -1) {
            TaskRecord task = this.mSupervisor.anyTaskForIdLocked(this.mOptions.getLaunchTaskId());
            intentActivity = task != null ? task.getTopActivity() : null;
            if (ActivityManagerDebugConfig.DEBUG_OPPO_FREEFORM) {
                Slog.v(TAG, "oppo freeform getReusableIntentActivity task: " + task + " , getLaunchTaskId: " + this.mOptions.getLaunchTaskId() + " ,intentActivity: " + intentActivity + " ,mStartActivity:" + this.mStartActivity);
            }
        } else if (putIntoExistingTask) {
            if (this.mLaunchSingleInstance) {
                intentActivity = this.mSupervisor.findActivityLocked(this.mIntent, this.mStartActivity.info, this.mStartActivity.isHomeActivity());
            } else if ((this.mLaunchFlags & 4096) != 0) {
                intentActivity = this.mSupervisor.findActivityLocked(this.mIntent, this.mStartActivity.info, this.mLaunchSingleTask ^ 1);
            } else {
                intentActivity = this.mSupervisor.findTaskLocked(this.mStartActivity, this.mSourceDisplayId);
            }
            if (ActivityManagerDebugConfig.DEBUG_OPPO_FREEFORM) {
                Slog.v(TAG, "oppo freeform getReusableIntentActivity mLaunchSingleInstance: " + this.mLaunchSingleInstance + " ,intentActivity: " + intentActivity + " , mLaunchFlags: " + this.mLaunchFlags + " ,mStartActivity:" + this.mStartActivity);
            }
        }
        return intentActivity;
    }

    private int getSourceDisplayId(ActivityRecord sourceRecord, ActivityRecord startingActivity) {
        if (startingActivity != null && startingActivity.requestedVrComponent != null) {
            return 0;
        }
        int displayId = this.mService.mVr2dDisplayId;
        if (displayId != -1) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG, "getSourceDisplayId :" + displayId);
            }
            this.mUsingVr2dDisplay = true;
            return displayId;
        }
        displayId = sourceRecord != null ? sourceRecord.getDisplayId() : -1;
        if (displayId != -1) {
            return displayId;
        }
        return 0;
    }

    private ActivityRecord setTargetStackAndMoveToFrontIfNeeded(ActivityRecord intentActivity) {
        this.mTargetStack = intentActivity.getStack();
        this.mTargetStack.mLastPausedActivity = null;
        ActivityStack focusStack = this.mSupervisor.getFocusedStack();
        ActivityRecord curTop = focusStack == null ? null : focusStack.topRunningNonDelayedActivityLocked(this.mNotTop);
        TaskRecord topTask = curTop != null ? curTop.getTask() : null;
        if (ActivityManagerDebugConfig.DEBUG_OPPO_FREEFORM) {
            Slog.v(TAG, "oppo freeform setTargetStackAndMoveToFrontIfNeeded: " + intentActivity + " , mTargetStack: " + this.mTargetStack + " ,topTask: " + topTask + " , focusStack: " + focusStack);
        }
        if (!(topTask == null || ((topTask == intentActivity.getTask() && topTask == focusStack.topTask()) || (this.mAvoidMoveToFront ^ 1) == 0))) {
            this.mStartActivity.intent.addFlags(DumpState.DUMP_CHANGES);
            if (this.mSourceRecord == null || (this.mSourceStack.topActivity() != null && this.mSourceStack.topActivity().getTask() == this.mSourceRecord.getTask())) {
                if (this.mLaunchTaskBehind && this.mSourceRecord != null) {
                    intentActivity.setTaskToAffiliateWith(this.mSourceRecord.getTask());
                }
                this.mMovedOtherTask = true;
                if (!((this.mLaunchFlags & 268468224) == 268468224)) {
                    ActivityStack launchStack = getLaunchStack(this.mStartActivity, this.mLaunchFlags, this.mStartActivity.getTask(), this.mOptions);
                    TaskRecord intentTask = intentActivity.getTask();
                    if (launchStack == null || launchStack == this.mTargetStack) {
                        this.mTargetStack.moveTaskToFrontLocked(intentTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringingFoundTaskToFront");
                        this.mMovedToFront = true;
                    } else if (launchStack.mStackId == 3 || launchStack.mStackId == 1) {
                        if ((this.mLaunchFlags & 4096) != 0) {
                            intentTask.reparent(launchStack.mStackId, true, 0, true, true, "launchToSide");
                        } else {
                            this.mTargetStack.moveTaskToFrontLocked(intentTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringToFrontInsteadOfAdjacentLaunch");
                        }
                        this.mMovedToFront = true;
                    } else if (launchStack.mDisplayId != this.mTargetStack.mDisplayId) {
                        intentActivity.getTask().reparent(launchStack.mStackId, true, 0, true, true, "reparentToDisplay");
                        this.mMovedToFront = true;
                    } else if (launchStack.getStackId() == 0 && this.mTargetStack.getStackId() != 0) {
                        intentActivity.getTask().reparent(launchStack.mStackId, true, 0, true, true, "reparentingHome");
                        this.mMovedToFront = true;
                    }
                    this.mOptions = null;
                    intentActivity.showStartingWindow(null, false, true);
                }
                updateTaskReturnToType(intentActivity.getTask(), this.mLaunchFlags, focusStack);
            }
        }
        if (!this.mMovedToFront && this.mDoResume) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(ActivityStackSupervisor.TAG_TASKS, "Bring to front target: " + this.mTargetStack + " from " + intentActivity);
            }
            this.mTargetStack.moveToFront("intentActivityFound");
        }
        this.mSupervisor.handleNonResizableTaskIfNeeded(intentActivity.getTask(), -1, 0, this.mTargetStack.mStackId);
        if ((this.mLaunchFlags & DumpState.DUMP_COMPILER_STATS) != 0) {
            return this.mTargetStack.resetTaskIfNeededLocked(intentActivity, this.mStartActivity);
        }
        return intentActivity;
    }

    private void updateTaskReturnToType(TaskRecord task, int launchFlags, ActivityStack focusedStack) {
        if ((launchFlags & 268451840) == 268451840) {
            task.setTaskToReturnTo(1);
        } else if (focusedStack == null || focusedStack.isHomeStack()) {
            task.setTaskToReturnTo(1);
        } else if (focusedStack == null || focusedStack == task.getStack() || !focusedStack.isAssistantStack()) {
            task.setTaskToReturnTo(0);
        } else {
            task.setTaskToReturnTo(3);
        }
    }

    private void setTaskFromIntentActivity(ActivityRecord intentActivity) {
        TaskRecord task;
        if ((this.mLaunchFlags & 268468224) == 268468224) {
            task = intentActivity.getTask();
            task.performClearTaskLocked();
            this.mReuseTask = task;
            this.mReuseTask.setIntent(this.mStartActivity);
            this.mMovedOtherTask = true;
        } else if ((this.mLaunchFlags & 67108864) != 0 || this.mLaunchSingleInstance || this.mLaunchSingleTask) {
            if (intentActivity.getTask().performClearTaskLocked(this.mStartActivity, this.mLaunchFlags) == null) {
                this.mAddingToTask = true;
                this.mStartActivity.setTask(null);
                this.mSourceRecord = intentActivity;
                task = this.mSourceRecord.getTask();
                if (task != null && task.getStack() == null) {
                    this.mTargetStack = computeStackFocus(this.mSourceRecord, false, null, this.mLaunchFlags, this.mOptions);
                    this.mTargetStack.addTask(task, this.mLaunchTaskBehind ^ 1, "startActivityUnchecked");
                }
            }
        } else if (this.mStartActivity.realActivity.equals(intentActivity.getTask().realActivity)) {
            if (((this.mLaunchFlags & 536870912) != 0 || this.mLaunchSingleTop) && intentActivity.realActivity.equals(this.mStartActivity.realActivity)) {
                if (intentActivity.frontOfTask) {
                    intentActivity.getTask().setIntent(this.mStartActivity);
                }
                deliverNewIntent(intentActivity);
            } else if (!intentActivity.getTask().isSameIntentFilter(this.mStartActivity)) {
                this.mAddingToTask = true;
                this.mSourceRecord = intentActivity;
            }
        } else if ((this.mLaunchFlags & DumpState.DUMP_COMPILER_STATS) == 0) {
            this.mAddingToTask = true;
            this.mSourceRecord = intentActivity;
        } else if (!intentActivity.getTask().rootWasReset) {
            intentActivity.getTask().setIntent(this.mStartActivity);
        }
    }

    private void resumeTargetStackIfNeeded() {
        if (this.mDoResume) {
            this.mSupervisor.resumeFocusedStackTopActivityLocked(this.mTargetStack, null, this.mOptions);
        } else {
            ActivityOptions.abort(this.mOptions);
        }
        this.mSupervisor.updateUserStackLocked(this.mStartActivity.userId, this.mTargetStack);
    }

    private int setTaskFromReuseOrCreateNewTask(TaskRecord taskToAffiliate, int preferredLaunchStackId, ActivityStack topStack) {
        this.mTargetStack = computeStackFocus(this.mStartActivity, true, this.mLaunchBounds, this.mLaunchFlags, this.mOptions);
        if (this.mReuseTask == null) {
            addOrReparentStartingActivity(this.mTargetStack.createTaskRecord(this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.userId), this.mNewTaskInfo != null ? this.mNewTaskInfo : this.mStartActivity.info, this.mNewTaskIntent != null ? this.mNewTaskIntent : this.mIntent, this.mVoiceSession, this.mVoiceInteractor, this.mLaunchTaskBehind ^ 1, this.mStartActivity.mActivityType), "setTaskFromReuseOrCreateNewTask - mReuseTask");
            if (this.mLaunchBounds != null) {
                int stackId = this.mTargetStack.mStackId;
                if (StackId.resizeStackWithLaunchBounds(stackId)) {
                    this.mService.resizeStack(stackId, this.mLaunchBounds, true, false, true, -1);
                } else {
                    this.mStartActivity.getTask().updateOverrideConfiguration(this.mLaunchBounds);
                }
            }
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in new task " + this.mStartActivity.getTask());
            }
        } else {
            addOrReparentStartingActivity(this.mReuseTask, "setTaskFromReuseOrCreateNewTask");
        }
        if (taskToAffiliate != null) {
            this.mStartActivity.setTaskToAffiliateWith(taskToAffiliate);
        }
        if (this.mSupervisor.isLockTaskModeViolation(this.mStartActivity.getTask())) {
            this.mSupervisor.showLockTaskToast();
            Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        if (!this.mMovedOtherTask) {
            TaskRecord task = this.mStartActivity.getTask();
            int i = this.mLaunchFlags;
            if (preferredLaunchStackId != -1) {
                topStack = this.mTargetStack;
            }
            updateTaskReturnToType(task, i, topStack);
        }
        if (this.mDoResume) {
            this.mTargetStack.moveToFront("reuseOrNewTask");
        }
        return 0;
    }

    private void deliverNewIntent(ActivityRecord activity) {
        if (!this.mIntentDelivered) {
            ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, activity, activity.getTask());
            activity.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            this.mIntentDelivered = true;
        }
    }

    private int setTaskFromSourceRecord() {
        if (this.mSupervisor.isLockTaskModeViolation(this.mSourceRecord.getTask())) {
            this.mSupervisor.showLockTaskToast();
            Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        int targetDisplayId;
        int moveStackAllowed;
        String packageName = this.mService.mContext.getPackageName();
        if (this.mPerf != null) {
            this.mStartActivity.perfActivityBoostHandler = this.mPerf.perfHint(4225, packageName, -1, 1);
        }
        TaskRecord sourceTask = this.mSourceRecord.getTask();
        ActivityStack sourceStack = this.mSourceRecord.getStack();
        if (this.mTargetStack != null) {
            targetDisplayId = this.mTargetStack.mDisplayId;
        } else {
            targetDisplayId = sourceStack.mDisplayId;
        }
        if (sourceStack.topTask() == sourceTask) {
            moveStackAllowed = this.mStartActivity.canBeLaunchedOnDisplay(targetDisplayId) ^ 1;
        } else {
            moveStackAllowed = 1;
        }
        if (moveStackAllowed != 0) {
            this.mTargetStack = getLaunchStack(this.mStartActivity, this.mLaunchFlags, this.mStartActivity.getTask(), this.mOptions);
            if (this.mTargetStack == null && targetDisplayId != sourceStack.mDisplayId) {
                this.mTargetStack = this.mService.mStackSupervisor.getValidLaunchStackOnDisplay(sourceStack.mDisplayId, this.mStartActivity);
            }
            if (this.mTargetStack == null) {
                this.mTargetStack = this.mService.mStackSupervisor.getNextValidLaunchStackLocked(this.mStartActivity, -1);
            }
        }
        if (this.mTargetStack == null) {
            this.mTargetStack = sourceStack;
        } else if (this.mTargetStack != sourceStack) {
            sourceTask.reparent(this.mTargetStack.mStackId, true, 0, false, true, "launchToSide");
        }
        if (this.mTargetStack.topTask() != sourceTask && (this.mAvoidMoveToFront ^ 1) != 0) {
            this.mTargetStack.moveTaskToFrontLocked(sourceTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "sourceTaskToFront");
        } else if (this.mDoResume) {
            this.mTargetStack.moveToFront("sourceStackToFront");
        }
        ActivityRecord top;
        if (!this.mAddingToTask && (this.mLaunchFlags & 67108864) != 0) {
            top = sourceTask.performClearTaskLocked(this.mStartActivity, this.mLaunchFlags);
            this.mKeepCurTransition = true;
            if (top != null) {
                ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, top.getTask());
                deliverNewIntent(top);
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
                TaskRecord task = top.getTask();
                task.moveActivityToFrontLocked(top);
                top.updateOptionsLocked(this.mOptions);
                ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, task);
                deliverNewIntent(top);
                this.mTargetStack.mLastPausedActivity = null;
                if (this.mDoResume) {
                    this.mSupervisor.resumeFocusedStackTopActivityLocked();
                }
                return 3;
            }
        }
        addOrReparentStartingActivity(sourceTask, "setTaskFromSourceRecord");
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in existing task " + this.mStartActivity.getTask() + " from source " + this.mSourceRecord);
        }
        return 0;
    }

    private int setTaskFromInTask() {
        if (this.mSupervisor.isLockTaskModeViolation(this.mInTask)) {
            this.mSupervisor.showLockTaskToast();
            Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        this.mTargetStack = this.mInTask.getStack();
        ActivityRecord top = this.mInTask.getTopActivity();
        if (top != null && top.realActivity.equals(this.mStartActivity.realActivity) && top.userId == this.mStartActivity.userId && ((this.mLaunchFlags & 536870912) != 0 || this.mLaunchSingleTop || this.mLaunchSingleTask)) {
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            if ((this.mStartFlags & 1) != 0) {
                return 1;
            }
            deliverNewIntent(top);
            return 3;
        } else if (this.mAddingToTask) {
            if (this.mLaunchBounds != null) {
                this.mInTask.updateOverrideConfiguration(this.mLaunchBounds);
                int stackId = this.mInTask.getLaunchStackId();
                if (stackId != this.mInTask.getStackId()) {
                    this.mInTask.reparent(stackId, true, 1, false, true, "inTaskToFront");
                    stackId = this.mInTask.getStackId();
                    this.mTargetStack = this.mInTask.getStack();
                }
                if (StackId.resizeStackWithLaunchBounds(stackId)) {
                    this.mService.resizeStack(stackId, this.mLaunchBounds, true, false, true, -1);
                }
            }
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            addOrReparentStartingActivity(this.mInTask, "setTaskFromInTask");
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in explicit task " + this.mStartActivity.getTask());
            }
            return 0;
        } else {
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            ActivityOptions.abort(this.mOptions);
            return 2;
        }
    }

    private void setTaskToCurrentTopOrCreateNewTask() {
        TaskRecord task;
        this.mTargetStack = computeStackFocus(this.mStartActivity, false, null, this.mLaunchFlags, this.mOptions);
        if (this.mDoResume) {
            this.mTargetStack.moveToFront("addingToTopTask");
        }
        ActivityRecord prev = this.mTargetStack.topActivity();
        if (prev != null) {
            task = prev.getTask();
        } else {
            task = this.mTargetStack.createTaskRecord(this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.userId), this.mStartActivity.info, this.mIntent, null, null, true, this.mStartActivity.mActivityType);
        }
        addOrReparentStartingActivity(task, "setTaskToCurrentTopOrCreateNewTask");
        this.mTargetStack.positionChildWindowContainerAtTop(task);
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in new guessed " + this.mStartActivity.getTask());
        }
    }

    private void addOrReparentStartingActivity(TaskRecord parent, String reason) {
        if (this.mStartActivity.getTask() == null || this.mStartActivity.getTask() == parent) {
            parent.addActivityToTop(this.mStartActivity);
        } else {
            this.mStartActivity.reparent(parent, parent.mActivities.size(), reason);
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
                startActivity(pal.r, pal.sourceRecord, null, null, pal.startFlags, doResume ? this.mPendingActivityLaunches.isEmpty() : false, null, null, null);
            } catch (Exception e) {
                Slog.e(TAG, "Exception during pending activity launch pal=" + pal, e);
                pal.sendErrorResult(e.getMessage());
            }
        }
    }

    private ActivityStack computeStackFocus(ActivityRecord r, boolean newTask, Rect bounds, int launchFlags, ActivityOptions aOptions) {
        TaskRecord task = r.getTask();
        ActivityStack stack = getLaunchStack(r, launchFlags, task, aOptions);
        if (stack != null) {
            return stack;
        }
        ActivityStack currentStack = task != null ? task.getStack() : null;
        if (currentStack != null) {
            if (this.mSupervisor.mFocusedStack != currentStack) {
                if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(TAG_FOCUS, "computeStackFocus: Setting focused stack to r=" + r + " task=" + task);
                }
            } else if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG_FOCUS, "computeStackFocus: Focused stack already=" + this.mSupervisor.mFocusedStack);
            }
            return currentStack;
        } else if (canLaunchIntoFocusedStack(r, newTask)) {
            if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG_FOCUS, "computeStackFocus: Have a focused stack=" + this.mSupervisor.mFocusedStack);
            }
            return this.mSupervisor.mFocusedStack;
        } else {
            if (this.mSourceDisplayId != 0) {
                stack = this.mSupervisor.getValidLaunchStackOnDisplay(this.mSourceDisplayId, r);
                if (stack == null) {
                    if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.d(TAG_FOCUS, "computeStackFocus: Can't launch on mSourceDisplayId=" + this.mSourceDisplayId + ", looking on all displays.");
                    }
                    stack = this.mSupervisor.getNextValidLaunchStackLocked(r, this.mSourceDisplayId);
                }
            }
            if (stack == null) {
                int stackId;
                ArrayList<ActivityStack> homeDisplayStacks = this.mSupervisor.mHomeStack.mStacks;
                for (int stackNdx = homeDisplayStacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    stack = (ActivityStack) homeDisplayStacks.get(stackNdx);
                    if (StackId.isDynamicStack(stack.mStackId)) {
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
            }
            if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG_FOCUS, "computeStackFocus: New stack r=" + r + " stackId=" + stack.mStackId);
            }
            return stack;
        }
    }

    private boolean canLaunchIntoFocusedStack(ActivityRecord r, boolean newTask) {
        boolean canUseFocusedStack;
        ActivityStack focusedStack = this.mSupervisor.mFocusedStack;
        int focusedStackId = this.mSupervisor.mFocusedStack.mStackId;
        boolean canDockedMode = false;
        switch (focusedStackId) {
            case 1:
                canUseFocusedStack = true;
                break;
            case 2:
                canUseFocusedStack = r.supportsFreeform();
                break;
            case 3:
                canUseFocusedStack = r.supportsSplitScreen();
                ActivityStack recentStack = this.mSupervisor.getStack(5, false, false);
                if (recentStack != null && recentStack.mHasRunningActivity) {
                    canDockedMode = true;
                    break;
                }
            case 6:
                canUseFocusedStack = r.isAssistantActivity();
                break;
            default:
                if (!StackId.isDynamicStack(focusedStackId)) {
                    canUseFocusedStack = false;
                    break;
                }
                canUseFocusedStack = r.canBeLaunchedOnDisplay(focusedStack.mDisplayId);
                break;
        }
        if (!canUseFocusedStack) {
            return false;
        }
        if ((!newTask || canDockedMode) && this.mSourceDisplayId == focusedStack.mDisplayId) {
            return true;
        }
        return false;
    }

    private ActivityStack getLaunchStack(ActivityRecord r, int launchFlags, TaskRecord task, ActivityOptions aOptions) {
        if (this.mReuseTask != null) {
            return this.mReuseTask.getStack();
        }
        if (r.isHomeActivity()) {
            return this.mSupervisor.mHomeStack;
        }
        if (r.isRecentsActivity()) {
            return this.mSupervisor.getStack(5, true, true);
        }
        if (r.isAssistantActivity()) {
            return this.mSupervisor.getStack(6, true, true);
        }
        int launchDisplayId = aOptions != null ? aOptions.getLaunchDisplayId() : -1;
        int launchStackId = aOptions != null ? aOptions.getLaunchStackId() : -1;
        if (launchStackId != -1 && launchDisplayId != -1) {
            throw new IllegalArgumentException("Stack and display id can't be set at the same time.");
        } else if (isValidLaunchStackId(launchStackId, launchDisplayId, r)) {
            return this.mSupervisor.getStack(launchStackId, true, true);
        } else {
            if (launchStackId == 3) {
                return this.mSupervisor.getStack(1, true, true);
            }
            if (launchDisplayId != -1) {
                return this.mSupervisor.getValidLaunchStackOnDisplay(launchDisplayId, r);
            }
            if (this.mUsingVr2dDisplay) {
                ActivityStack as = this.mSupervisor.getValidLaunchStackOnDisplay(this.mSourceDisplayId, r);
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.v(TAG, "Launch stack for app: " + r.toString() + ", on virtual display stack:" + as.toString());
                }
                return as;
            } else if ((launchFlags & 4096) == 0 || this.mSourceDisplayId != 0) {
                return null;
            } else {
                ActivityStack parentStack = task != null ? task.getStack() : this.mSupervisor.mFocusedStack;
                if (parentStack != this.mSupervisor.mFocusedStack) {
                    return parentStack;
                }
                if (this.mSupervisor.mFocusedStack != null && task == this.mSupervisor.mFocusedStack.topTask()) {
                    return this.mSupervisor.mFocusedStack;
                }
                if (parentStack != null && parentStack.isDockedStack()) {
                    return this.mSupervisor.getStack(1, true, true);
                }
                ActivityStack dockedStack = this.mSupervisor.getStack(3);
                if (dockedStack == null || dockedStack.shouldBeVisible(r) != 0) {
                    return dockedStack;
                }
                return null;
            }
        }
    }

    boolean isValidLaunchStackId(int stackId, int displayId, ActivityRecord r) {
        switch (stackId) {
            case -1:
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return r.supportsFreeform();
            case 3:
                return r.supportsSplitScreen();
            case 4:
                return r.supportsPictureInPicture();
            case 5:
                return r.isRecentsActivity();
            case 6:
                return r.isAssistantActivity();
            default:
                if (StackId.isDynamicStack(stackId)) {
                    return r.canBeLaunchedOnDisplay(displayId);
                }
                Slog.e(TAG, "isValidLaunchStackId: Unexpected stackId=" + stackId);
                return false;
        }
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

    static boolean isDocumentLaunchesIntoExisting(int flags) {
        if ((DumpState.DUMP_FROZEN & flags) == 0 || (134217728 & flags) != 0) {
            return false;
        }
        return true;
    }

    boolean clearPendingActivityLaunchesLocked(String packageName) {
        boolean didSomething = false;
        for (int palNdx = this.mPendingActivityLaunches.size() - 1; palNdx >= 0; palNdx--) {
            ActivityRecord r = ((PendingActivityLaunch) this.mPendingActivityLaunches.get(palNdx)).r;
            if (r != null && r.packageName.equals(packageName)) {
                this.mPendingActivityLaunches.remove(palNdx);
                didSomething = true;
            }
        }
        return didSomething;
    }

    void dump(PrintWriter pw, String prefix, String dumpPackage) {
        prefix = prefix + "  ";
        if (dumpPackage == null || ((this.mLastStartActivityRecord[0] != null && (dumpPackage.equals(this.mLastHomeActivityStartRecord[0].packageName) ^ 1) == 0) || ((this.mLastHomeActivityStartRecord[0] != null && (dumpPackage.equals(this.mLastHomeActivityStartRecord[0].packageName) ^ 1) == 0) || (this.mStartActivity != null && (dumpPackage.equals(this.mStartActivity.packageName) ^ 1) == 0)))) {
            pw.print(prefix);
            pw.print("mCurrentUser=");
            pw.println(this.mSupervisor.mCurrentUser);
            pw.print(prefix);
            pw.print("mLastStartReason=");
            pw.println(this.mLastStartReason);
            pw.print(prefix);
            pw.print("mLastStartActivityTimeMs=");
            pw.println(DateFormat.getDateTimeInstance().format(new Date(this.mLastStartActivityTimeMs)));
            pw.print(prefix);
            pw.print("mLastStartActivityResult=");
            pw.println(this.mLastStartActivityResult);
            ActivityRecord r = this.mLastStartActivityRecord[0];
            if (r != null) {
                pw.print(prefix);
                pw.println("mLastStartActivityRecord:");
                r.dump(pw, prefix + "  ");
            }
            pw.print(prefix);
            pw.print("mLastHomeActivityStartResult=");
            pw.println(this.mLastHomeActivityStartResult);
            r = this.mLastHomeActivityStartRecord[0];
            if (r != null) {
                pw.print(prefix);
                pw.println("mLastHomeActivityStartRecord:");
                r.dump(pw, prefix + "  ");
            }
            if (this.mStartActivity != null) {
                pw.print(prefix);
                pw.println("mStartActivity:");
                this.mStartActivity.dump(pw, prefix + "  ");
            }
            if (this.mIntent != null) {
                pw.print(prefix);
                pw.print("mIntent=");
                pw.println(this.mIntent);
            }
            if (this.mOptions != null) {
                pw.print(prefix);
                pw.print("mOptions=");
                pw.println(this.mOptions);
            }
            pw.print(prefix);
            pw.print("mLaunchSingleTop=");
            pw.print(this.mLaunchSingleTop);
            pw.print(" mLaunchSingleInstance=");
            pw.print(this.mLaunchSingleInstance);
            pw.print(" mLaunchSingleTask=");
            pw.println(this.mLaunchSingleTask);
            pw.print(prefix);
            pw.print("mLaunchFlags=0x");
            pw.print(Integer.toHexString(this.mLaunchFlags));
            pw.print(" mDoResume=");
            pw.print(this.mDoResume);
            pw.print(" mAddingToTask=");
            pw.println(this.mAddingToTask);
            return;
        }
        pw.print(prefix);
        pw.println("(nothing)");
    }
}

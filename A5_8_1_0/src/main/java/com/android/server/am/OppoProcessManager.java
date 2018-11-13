package com.android.server.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManagerInternal;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.view.inputmethod.InputMethodInfo;
import com.android.server.OppoBPMHelper;
import com.android.server.OppoBPMUtils;
import com.android.server.coloros.OppoListManager;
import com.android.server.oppo.ElsaManagerProxy;
import com.android.server.oppo.IElsaManager;
import com.android.server.wm.WindowManagerService;
import com.color.util.ColorPackageFreezeData;
import com.oppo.app.IOppoAppFreezeController;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OppoProcessManager {
    public static final int FREEZING1 = 1;
    public static final int FREEZING2 = 2;
    public static final int FREEZING3 = 3;
    public static final int GET_FREEZE_FAILED = 4;
    public static final int MSG_APP_CHANGE_NOTIF_ELSA = 300;
    public static final int MSG_APP_DIED = 182;
    public static final int MSG_READY_ENTER_STRICTMODE = 150;
    public static final int MSG_RECORD_RESUME_REASON = 140;
    public static final int MSG_REMOVE_RECORD = 181;
    public static final int MSG_RESUME_PROCESS = 200;
    public static final int MSG_SCREEN_OFF = 121;
    public static final int MSG_SCREEN_ON = 120;
    public static final int MSG_UPDATE_APPWIDGET = 102;
    public static final int MSG_UPDATE_BPM = 101;
    public static final int MSG_UPDATE_DISPLAYDEVICE = 110;
    public static final int MSG_UPDATE_ELSA_CONFIG_FILE = 100;
    public static final int MSG_UPDATE_POWER_CONN_STS = 103;
    public static final int MSG_UPDATE_PROC_STATE = 130;
    public static final int MSG_UPLOAD = 180;
    public static final int MSG_WINDOW_STATE_CHANGE = 111;
    private static final String NOTIFY_ELSA_CURRENT_STATUS = "notifyElsa";
    private static final int NOTIFY_ELSA_POWER_CONNECT = -2;
    private static final int NOTIFY_ELSA_POWER_DISCONNECT = -3;
    private static final int NOTIFY_ELSA_SCREEN_OFF = -1;
    private static final int NOTIFY_ELSA_SCREEN_ON = -4;
    public static final long RECORD_RESUME_REASON_DELAY = 10000;
    public static final int RESUME_REASON_APPWIDGET_CHANGE = 9;
    public static final String RESUME_REASON_APPWIDGET_CHANGE_STR = "appwidget";
    public static final int RESUME_REASON_BLUETOOTH = 6;
    public static final String RESUME_REASON_BLUETOOTH_STR = "bluetooth";
    public static final int RESUME_REASON_BROADCAST = 1;
    public static final String RESUME_REASON_BROADCAST_STR = "broadcast";
    public static final int RESUME_REASON_BROADCAST_TIMEOUT = 10;
    public static final String RESUME_REASON_BROADCAST_TIMEOUT_STR = "b_timeout";
    public static final int RESUME_REASON_MEDIA = 7;
    public static final String RESUME_REASON_MEDIA_STR = "media";
    public static final int RESUME_REASON_MOUNT = 5;
    public static final String RESUME_REASON_MOUNT_STR = "mount";
    public static final String RESUME_REASON_NEW_POLICY_BROADCAST_STR = "brd_resume_freeze";
    public static final String RESUME_REASON_NEW_POLICY_BROADCAST_WHITE_STR = "brd_white";
    public static final int RESUME_REASON_NOTIFY = 13;
    public static final String RESUME_REASON_NOTIFY_STR = "notity";
    public static final int RESUME_REASON_PROVIDER = 3;
    public static final String RESUME_REASON_PROVIDER_STR = "provider";
    public static final int RESUME_REASON_SERVICE = 2;
    public static final String RESUME_REASON_SERVICE_STR = "service";
    public static final int RESUME_REASON_SERVICE_TIMEOUT = 11;
    public static final String RESUME_REASON_SERVICE_TIMEOUT_STR = "s_timeout";
    public static final int RESUME_REASON_STOP_STRICTMODE = 14;
    public static final String RESUME_REASON_STOP_STRICTMODE_STR = "stop_smode";
    public static final int RESUME_REASON_SWITCH_CHANGE = 8;
    public static final String RESUME_REASON_SWITCH_CHANGE_STR = "switch";
    public static final int RESUME_REASON_SYSTEM_CALL = 12;
    public static final String RESUME_REASON_SYSTEM_CALL_STR = "s_call";
    public static final int RESUME_REASON_TOPAPP = 4;
    public static final String RESUME_REASON_TOPAPP_STR = "top_app";
    public static final int RESUME_REASON_VISIBLE_WINDOW = 15;
    public static final String RESUME_REASON_VISIBLE_WINDOW_STR = "window";
    public static final int RESUME_SINGNAL = 18;
    public static final int SET_FREEZE_FAILED = -1;
    public static final int STATUS_GAME = 1;
    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_PAYSAFE = 2;
    private static final long SUSPEND_DELAY = 5000;
    private static final long SUSPEND_DELAY_LONG = 180000;
    public static final int SUSPEND_SINGNAL = 19;
    public static final String TAG = "OppoProcessManager";
    public static final int TASK_PID = 1;
    public static final int TASK_TID = 2;
    public static final int TASK_UID = 4;
    private static final long THREAD_SLEEP_TIME = 20000;
    public static final int TIMEOUT_ENTER_FREEZING2 = 0;
    public static final int TIMEOUT_NOT_FREEZE = -1;
    public static final int UNFREEZE_STATE = 0;
    private static final String UPDATE_DISPLAY_DEVICE_ACTION = "oppo.intent.action.UPDATE_DISPLAY_DEVICE";
    private static final String UPDATE_DISPLAY_DEVICE_KEY = "displayList";
    private static final long UPLOAD_INTERVAL_TIME = 21600000;
    private static final int UPLOAD_NORMAL = 1;
    private static final int UPLOAD_POWER_CONN = 2;
    private static final String WINDOW_STATE_CHANGE_ACTION = "oppo.intent.action.WINDOW_STATE_CHANGE";
    private static final String WINDOW_STATE_KEY = "windowList";
    public static boolean sDebugDetail = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static OppoProcessManager sOppoProcessManager = null;
    private ActivityManagerService mActivityManager = null;
    private ArrayMap<String, Integer> mAllAppRecordMap = new ArrayMap();
    private long mAppChangeCheckTime = 30000;
    private List<String> mAppWidgetList = new ArrayList();
    private final Object mAppWidgetLock = new Object();
    private ArrayList<String> mAssociateKeyList = new ArrayList();
    private ArrayMap<String, String> mAssociateKeyMap = new ArrayMap();
    private AudioManager mAudioManager;
    private boolean mBPMSwitch = true;
    private List<String> mBlackAppBrdList = new ArrayList();
    private final Object mBlackAppBrdLock = new Object();
    private List<String> mBlackAppList = new ArrayList();
    private final Object mBlackAppLock = new Object();
    private List<String> mBlackSysAppList = new ArrayList();
    private final Object mBlackSysAppLock = new Object();
    private BPMHandler mBpmHandler = null;
    private List<String> mBpmList = new ArrayList();
    private final Object mBpmLock = new Object();
    private List<String> mBrdList = new ArrayList();
    private final Object mBrdLock = new Object();
    private ArrayList<String> mBrdWhiteKeyList = new ArrayList();
    private IOppoAppFreezeController mController = null;
    private List<String> mCprList = new ArrayList();
    private final Object mCprLock = new Object();
    private List<String> mCustomizeAppList = new ArrayList();
    private final Object mCustomizeAppLock = new Object();
    boolean mDebugSwitch = (sDebugDetail | this.mDynamicDebug);
    private String mDefaultInputMethod = null;
    private ContentObserver mDefaultInputMethodObserver = new ContentObserver(this.mBpmHandler) {
        public void onChange(boolean selfChange) {
            OppoProcessManager.this.mDefaultInputMethod = OppoProcessManager.this.getDefaultInputMethod();
        }
    };
    private ArrayList<String> mDisplayDeviceList = new ArrayList();
    private final Object mDisplayDeviceListLock = new Object();
    boolean mDynamicDebug = false;
    private boolean mFeatureExpRom = false;
    private boolean mFreezeSwitch = true;
    private List<String> mGlobalWhiteList = new ArrayList();
    private IElsaManager mIElsaManager = null;
    private boolean mIsInOffHook = false;
    private boolean mIsScreenOn = true;
    private int mModeStatus = 0;
    private List<String> mNewPolicyBrdActionWhiteList = new ArrayList();
    private boolean mNewPolicyBrdEnable = false;
    private List<String> mNewPolicyBrdPkgWhiteList = new ArrayList();
    private long mPayModeEnterTime = 5000;
    private boolean mPaySafeSwitch = false;
    private long mPeriodCheckTime = 180000;
    private List<String> mPkgList = new ArrayList();
    private final Object mPkgLock = new Object();
    private boolean mPowerConnStatus = false;
    private PowerManagerInternal mPowerManagerInternal = null;
    private int mRecentNum = 0;
    private int mRecentStore = 0;
    boolean mRecordSwitch = false;
    private long mScreenOnCheckTime = 30000;
    private long mStartFromNotityTime = 10000;
    private boolean mStatisticsSwitch = false;
    private boolean mStrictMode = false;
    private long mStrictModeEnterTime = 60000;
    private boolean mStrictModeSwitch = true;
    private List<String> mStrictWhitePkgList = new ArrayList();
    private final Object mStrictWhitePkgListLock = new Object();
    private ArrayList<Integer> mVisibleWindowList = new ArrayList();
    private ArrayMap<Integer, List<Integer>> mVisibleWindowMap = new ArrayMap();
    private WindowManagerService mWindowManager = null;

    private class BPMHandler extends Handler {
        public BPMHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    OppoProcessManager.this.handleElsaConfigFileMsg();
                    return;
                case 101:
                    OppoProcessManager.this.handleUpdateBpmFileMsg();
                    return;
                case 102:
                    OppoProcessManager.this.handleAppWidgetUpdateMsg();
                    return;
                case 103:
                    OppoProcessManager.this.handlePowerConnStsUpdateMsg();
                    return;
                case 110:
                    OppoProcessManager.this.handleDisplayDeviceUpdateMsg();
                    return;
                case 111:
                    OppoProcessManager.this.handleWindowStateChangeMsg(msg);
                    return;
                case 120:
                    OppoProcessManager.this.handleScreenOnMsg();
                    return;
                case 121:
                    OppoProcessManager.this.handleScreenOffMsg();
                    return;
                case 140:
                    OppoProcessManager.this.handleRecordResumeReasonMsg(msg);
                    return;
                case OppoProcessManager.MSG_READY_ENTER_STRICTMODE /*150*/:
                    OppoProcessManager.this.handleReadyStrictModeMsg();
                    return;
                case 200:
                    OppoProcessManager.this.handleResumeMsg(msg);
                    return;
                case 300:
                    OppoProcessManager.this.handleAppChangeNofiElsa(msg);
                    return;
                default:
                    return;
            }
        }
    }

    public static final synchronized OppoProcessManager getInstance() {
        OppoProcessManager oppoProcessManager;
        synchronized (OppoProcessManager.class) {
            if (sOppoProcessManager == null) {
                sOppoProcessManager = new OppoProcessManager();
            }
            oppoProcessManager = sOppoProcessManager;
        }
        return oppoProcessManager;
    }

    public void init(ActivityManagerService ams) {
        HandlerThread thread = new HandlerThread("BpmThread");
        thread.start();
        this.mBpmHandler = new BPMHandler(thread.getLooper());
        this.mActivityManager = ams;
        initData();
        initStateReceiver();
        registerLogModule();
        this.mDefaultInputMethod = getDefaultInputMethod();
        isExpVersion();
    }

    private void initData() {
        OppoBPMUtils.getInstance().initData(this);
        updateElsaConfigFile();
        updateBpmList();
        updateAppWidgetList();
        updatePowerConnStatus();
        updateCustomizeAppList();
        updateDisplayDeviceList();
    }

    private void updateElsaConfigFile() {
        updatePkgList();
        updateStrictWhitePkgList();
        updateBrdList();
        updateBrdBlackList();
        updateBlackSysAppList();
        updateBlackAppList();
        updateNewPolicyBrdPkgWhiteList();
        updateNewPolicyBrdActionWhiteList();
        updateBpmConfig();
        updateBrdWhiteKeyList();
        updateAssociateKeyList();
    }

    private void isExpVersion() {
        try {
            if (this.mActivityManager != null && this.mActivityManager.mContext != null) {
                this.mFeatureExpRom = this.mActivityManager.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resumeProcess(ProcessRecord app, int reason) {
        if (app != null) {
            if (reason == 8 || reason == 13 || reason == 9 || reason == 4 || !checkWhiteProcessRecord(app)) {
                if (this.mDynamicDebug) {
                    Slog.i(TAG, "resumeProcess enter app " + app.processName + "  reason is " + reason);
                }
                try {
                    if (isFrozingByApp(app)) {
                        if (this.mDynamicDebug) {
                            Slog.i(TAG, Log.getStackTraceString(new Throwable()));
                        }
                        sendBpmMessage(app, 200, 0, reason);
                    }
                } catch (NullPointerException e) {
                    Slog.w(TAG, "resume Process failed " + e);
                }
            }
        }
    }

    private void updateProcessStateForWidgetChanged(String pkg) {
        if (pkg != null) {
            updateWidget(pkg);
        }
    }

    private void updateProcessStateForStopStrictMode() {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "updateProcessStateForStopStrictMode");
        }
        synchronized (this.mActivityManager) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                for (int i = this.mActivityManager.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord rec = (ProcessRecord) this.mActivityManager.mLruProcesses.get(i);
                    if (!(rec == null || rec.thread == null || !isProcessInWhiteList(rec))) {
                        resumeProcess(rec, 8);
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean checkProcessCanRestart(ProcessRecord app) {
        if (isInBlackList(app)) {
            Slog.i(TAG, "checkProcessCanRestart isInBlackList + " + app);
            return false;
        } else if (!isEnable() || checkWhiteProcessRecord(app)) {
            return true;
        } else {
            String wallpaperPkg = OppoBPMHelper.getLivePackageForLiveWallPaper();
            if (wallpaperPkg != null && isInclude(wallpaperPkg, app.getPackageList())) {
                return true;
            }
            try {
                return isHomeProcess(app);
            } catch (ArrayIndexOutOfBoundsException e) {
                Slog.i(TAG, "catch exception: " + e);
            }
        }
    }

    public boolean skipBroadcast(ProcessRecord app, BroadcastRecord r, boolean ordered) {
        if (app == null) {
            return false;
        }
        boolean isInBlackList = isInBlackList(app);
        if (!isEnable() && (isInBlackList ^ 1) != 0) {
            return false;
        }
        if (!isInBlackList) {
            if (checkWhiteProcessRecord(app)) {
                if (isFrozingByApp(app)) {
                    setPackageResume(app.uid, app.processName, "broadcast");
                }
                return false;
            }
            ActivityRecord topAr = this.mActivityManager.mStackSupervisor.getTopRunningActivityLocked();
            if (topAr == null || !isInclude(topAr.packageName, app.getPackageList())) {
                if (this.mNewPolicyBrdEnable) {
                    if (isInNewPolicyBrdPkgWhiteList(app) || isInNewPolicyBrdActionWhiteList(r)) {
                        if (isFrozingByApp(app)) {
                            setPackageResume(app.uid, app.processName, RESUME_REASON_NEW_POLICY_BROADCAST_WHITE_STR);
                        }
                        return false;
                    } else if (r.callingUid != app.uid && r.callingUid >= 10000 && (isInBlackAppBrdList(r) ^ 1) != 0 && (isStrictMode() ^ 1) != 0) {
                        if (this.mDynamicDebug) {
                            Slog.i(TAG, "Not skip broadcast callingUid " + r.callingUid + " pid " + r.callingPid + " action " + r.intent.getAction() + " app " + app);
                        }
                        return false;
                    } else if (r.callingUid == app.uid) {
                        if (!isFrozingByApp(app)) {
                            return false;
                        }
                        if (r.callingPid == -1 && r.intent != null && r.intent.getAction() != null && (r.intent.getAction().contains("push") ^ 1) != 0 && isInBrdWhiteKey(app)) {
                            setPackageResume(app.uid, app.processName, RESUME_REASON_NEW_POLICY_BROADCAST_WHITE_STR);
                            if (this.mDynamicDebug) {
                                Slog.i(TAG, "Not skip broadcast because uid is the same  " + app.uid + "  action is " + r.intent.getAction());
                            }
                            return false;
                        } else if (r.callingPid != -1 || r.intent == null || r.intent.getComponent() == null || !isInBrdWhiteKey(app)) {
                            if (this.mDebugSwitch) {
                                Slog.i(TAG, "skip broadcast because uid is the same  " + app.uid + "  action is " + r.intent.getAction());
                            }
                            return true;
                        } else {
                            setPackageResume(app.uid, app.processName, RESUME_REASON_NEW_POLICY_BROADCAST_WHITE_STR);
                            if (this.mDebugSwitch) {
                                Slog.i(TAG, "Not skip broadcast because uid is the same  " + app.uid + "  component is " + r.intent.getComponent());
                            }
                            return false;
                        }
                    } else if (ordered) {
                        if (isFrozingByApp(app)) {
                            if (this.mDebugSwitch) {
                                Slog.i(TAG, "skip order broadcast callingUid " + r.callingUid + " pid " + r.callingPid + " action " + r.intent.getAction() + " app " + app);
                            }
                            return true;
                        }
                        if (this.mDynamicDebug) {
                            Slog.i(TAG, "Do not skip order broadcast " + r.intent.getAction() + "  " + app);
                        }
                        return false;
                    }
                } else if (isInBrdList(r)) {
                    if (isFrozingByApp(app)) {
                        setPackageResume(app.uid, app.processName, "broadcast");
                    }
                    return false;
                } else if (r.callingUid >= 10000 && (isInBlackAppBrdList(r) ^ 1) != 0 && (isStrictMode() ^ 1) != 0) {
                    if (isFrozingByApp(app)) {
                        setPackageResume(app.uid, app.processName, "broadcast");
                    }
                    if (this.mDebugSwitch) {
                        Slog.i(TAG, "Not skip callingUid " + r.callingUid + " pid: " + r.callingPid + " action: " + r.intent.getAction());
                    }
                    return false;
                } else if (ordered) {
                    if (isFrozingByApp(app)) {
                        return true;
                    }
                    if (this.mDebugSwitch) {
                        Slog.i(TAG, "Do not skip order broadcast " + r.intent.getAction() + "  " + app);
                    }
                    return false;
                } else if (r.callingUid == app.uid) {
                    if (isFrozingByApp(app)) {
                        setPackageResume(app.uid, app.processName, "broadcast");
                    }
                    if (this.mDebugSwitch) {
                        Slog.i(TAG, "Not skip broadcast because uid is the same  " + app.uid + "  action is " + r.intent.getAction());
                    }
                    return false;
                }
                if (isMultiWindowTopPkg(app)) {
                    if (this.mDynamicDebug) {
                        Slog.i(TAG, "Not skip broadcast because is multi window app!  " + r);
                    }
                    return false;
                }
            }
            if (this.mDynamicDebug) {
                Slog.i(TAG, "Not skip top app's broadcast uid is  " + app.uid + "  action is " + r.intent.getAction());
            }
            return false;
        }
        return true;
    }

    public boolean checkWhiteProcessRecord(ProcessRecord app) {
        return (app == null || isInBlackList(app) || (app.uid >= 10000 && !isProcessInWhiteList(app) && !isSystemProcess(app) && !OppoListManager.getInstance().isCtaPackage(app.info.packageName))) ? false : true;
    }

    private boolean isProcessInWhiteList(ProcessRecord app) {
        if (app == null) {
            return false;
        }
        if (isInBpmWhiteList(app)) {
            if (this.mDynamicDebug) {
                Slog.i(TAG, app + "  is in white list");
            }
            return true;
        } else if (isInCustomizeAppList(app) || isInAppWidgetList(app) || isInDisplayDeviceList(app) || isInGlobalWhiteList(app)) {
            if (this.mDynamicDebug) {
                Slog.i(TAG, app + "  is in white list for other");
            }
            return true;
        } else if (!isInFromNotityPkgList(app)) {
            return false;
        } else {
            if (this.mDynamicDebug) {
                Slog.i(TAG, app + "  is from notify");
            }
            return true;
        }
    }

    private boolean isSystemProcess(ProcessRecord app) {
        boolean result = false;
        if (!(app == null || app.info == null || (app.info.flags & 1) == 0 || isInBlackSysAppList(app))) {
            result = true;
        }
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isSystemProcess app " + app + ", result " + result);
        }
        return result;
    }

    private boolean checkWhitePackage(String pkgName) {
        if (isInBpmWhiteList(pkgName)) {
            if (this.mDynamicDebug) {
                Slog.i(TAG, pkgName + "  is in white list");
            }
            return true;
        } else if (isInCustomizeAppList(pkgName) || isInAppWidgetList(pkgName) || isInDisplayDeviceList(pkgName) || isInGlobalWhiteList(pkgName) || OppoListManager.getInstance().isCtaPackage(pkgName)) {
            if (this.mDynamicDebug) {
                Slog.i(TAG, pkgName + "  is in white list for other");
            }
            return true;
        } else if (isInFromNotityPkgList(pkgName)) {
            if (this.mDynamicDebug) {
                Slog.i(TAG, pkgName + "  is from notify");
            }
            return true;
        } else {
            PackageInfo pkgInfo = null;
            try {
                pkgInfo = this.mActivityManager.mContext.getPackageManager().getPackageInfo(pkgName, 0);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            if (pkgInfo != null && pkgInfo.applicationInfo != null) {
                return ((pkgInfo.applicationInfo.flags & 1) == 0 || isInBlackSysAppList(pkgName)) ? false : true;
            } else {
                Slog.i(TAG, pkgName + " does not exits!");
                return true;
            }
        }
    }

    private void cancelNotifications(final ProcessRecord app) {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "cancelNotifications  " + app.processName);
        }
        synchronized (this.mActivityManager) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (app != null && app.getPackageList().length > 0) {
                    for (final String pkg : app.getPackageList()) {
                        this.mActivityManager.mHandler.post(new Runnable() {
                            public void run() {
                                OppoBPMHelper.cancelNotificationsWithPkg(pkg, app.userId);
                            }
                        });
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void cancelNotification(final ProcessRecord app) {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "cancelNotification  " + app.processName);
        }
        synchronized (this.mActivityManager) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (!(app == null || app.info == null)) {
                    this.mActivityManager.mHandler.post(new Runnable() {
                        public void run() {
                            OppoBPMHelper.cancelNotificationsWithPkg(app.info.packageName, app.userId);
                        }
                    });
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0007, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isAppMainProcess(ProcessRecord app) {
        if (app == null || app.info == null || app.processName == null || app.info.packageName == null || !app.processName.equals(app.info.packageName)) {
            return false;
        }
        return true;
    }

    private boolean isHomeProcess(ProcessRecord app) {
        if (app == null || app.info == null) {
            return false;
        }
        return OppoBPMHelper.isHomeProcess(this.mActivityManager.mContext, app.info.packageName);
    }

    private void updateWidgets() {
        OppoBPMHelper.updateProviders(this.mAppWidgetList);
    }

    private void updateWidget(String pkg) {
        OppoBPMHelper.updateProvider(pkg);
    }

    private boolean isInputMethodApplication(ProcessRecord app) {
        if (app == null) {
            return false;
        }
        List<InputMethodInfo> imList = OppoBPMHelper.getInputMethodList();
        if (!(imList == null || (imList.isEmpty() ^ 1) == 0)) {
            for (InputMethodInfo im : imList) {
                if (app.pkgList.containsKey(im.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private ProcessRecord getProcessForPid(String pid) {
        ProcessRecord app;
        synchronized (this.mActivityManager) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                int i = this.mActivityManager.mLruProcesses.size() - 1;
                while (i >= 0) {
                    app = (ProcessRecord) this.mActivityManager.mLruProcesses.get(i);
                    if (app == null || app.thread == null || !pid.equals(Integer.toString(app.pid))) {
                        i--;
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
                return null;
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return app;
    }

    private ArrayList<ProcessRecord> getProcessForUid(int uid) {
        ArrayList<ProcessRecord> list;
        synchronized (this.mActivityManager) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                list = new ArrayList();
                for (int i = this.mActivityManager.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord app = (ProcessRecord) this.mActivityManager.mLruProcesses.get(i);
                    if (app.thread != null && app.uid == uid) {
                        list.add(app);
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return list;
    }

    public boolean isUidGroupHasSuspended(ProcessRecord app) {
        if (app != null) {
            if (this.mDynamicDebug) {
                Slog.i(TAG, "isUidGroupHasSuspended app'uid  " + app.uid);
            }
            for (ProcessRecord appProc : getProcessForUid(app.uid)) {
                if (this.mDynamicDebug) {
                    Slog.i(TAG, "isUidGroupHasSuspended app is   " + appProc);
                }
                if (appProc != null && isFrozingByUid(appProc.pid)) {
                    Slog.i(TAG, "isUidGroupHasSuspended Suspended   " + appProc.processName);
                    return true;
                }
            }
        }
        return false;
    }

    public void setPhoneState(String phoneState) {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "setPhoneState phoneState is " + phoneState);
        }
        if ("IDLE".equals(phoneState)) {
            this.mIsInOffHook = false;
        } else if ("OFFHOOK".equals(phoneState)) {
            this.mIsInOffHook = true;
        } else if ("RINGING".equals(phoneState)) {
            this.mIsInOffHook = true;
        }
    }

    private void initStateReceiver() {
        new Thread() {
            public void run() {
                do {
                    try {
                        Thread.sleep(OppoProcessManager.THREAD_SLEEP_TIME);
                    } catch (InterruptedException e) {
                    }
                } while (!OppoProcessManager.this.mActivityManager.mSystemReady);
                Slog.i(OppoProcessManager.TAG, "initStateReceiver SystemReady!");
                OppoPhoneStateReceiver oppoPhoneStateReceiver = new OppoPhoneStateReceiver(OppoProcessManager.this.mActivityManager.mContext);
                OppoScreenStateReceiver oppoScreenStateReceiver = new OppoScreenStateReceiver(OppoProcessManager.this.mActivityManager.mContext);
                OppoProcessManager.this.registerInputMethodObserver();
            }
        }.start();
    }

    private void registerInputMethodObserver() {
        this.mActivityManager.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("default_input_method"), true, this.mDefaultInputMethodObserver);
    }

    private String getDefaultInputMethod() {
        String defaultInput = null;
        if (this.mActivityManager != null) {
            try {
                String inputMethod = Secure.getString(this.mActivityManager.mContext.getContentResolver(), "default_input_method");
                if (inputMethod != null) {
                    defaultInput = inputMethod.substring(0, inputMethod.indexOf("/"));
                }
            } catch (Exception e) {
                Slog.e(TAG, "Failed to get default input method");
            }
        }
        if (this.mDebugSwitch) {
            Slog.i(TAG, "defaultInputMethod " + defaultInput);
        }
        return defaultInput;
    }

    private void displayListToSendBroadcastLocked() {
        if (this.mActivityManager != null) {
            Intent intent = new Intent(UPDATE_DISPLAY_DEVICE_ACTION);
            intent.putExtra(UPDATE_DISPLAY_DEVICE_KEY, this.mDisplayDeviceList);
            this.mActivityManager.mContext.sendBroadcast(intent);
        }
    }

    private void windowStateChangeToSendBroadcastLocked() {
        if (this.mActivityManager != null) {
            Intent intent = new Intent(WINDOW_STATE_CHANGE_ACTION);
            intent.putExtra(WINDOW_STATE_KEY, this.mVisibleWindowList);
            this.mActivityManager.mContext.sendBroadcast(intent);
        }
    }

    public void sendBpmMessage(ProcessRecord app, int what, long delay, int reason) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = app.uid;
        msg.arg2 = reason;
        msg.obj = app.processName;
        this.mBpmHandler.sendMessageDelayed(msg, delay);
    }

    public void sendBpmMessage(ProcessRecord app, int what, long delay) {
        sendBpmMessage(app, what, delay, 0);
    }

    public void sendBpmEmptyMessage(int what, long delay) {
        this.mBpmHandler.removeMessages(what);
        this.mBpmHandler.sendEmptyMessageDelayed(what, delay);
    }

    public void sendBpmMessage(int what, long delay, Bundle data) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.setData(data);
        this.mBpmHandler.sendMessageDelayed(msg, delay);
    }

    private void sendBpmMessage(int what, int uid, int arg2, boolean visible, long delay) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = uid;
        msg.arg2 = arg2;
        msg.obj = Boolean.valueOf(visible);
        this.mBpmHandler.sendMessageDelayed(msg, delay);
    }

    private void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.mDebugSwitch = sDebugDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        Slog.i(TAG, "mDynamicDebug == " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug == " + getInstance().mDynamicDebug);
    }

    private void registerLogModule() {
        try {
            Slog.i(TAG, "invoke com.android.server.OppoDynamicLogManager");
            Class cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", new Class[]{String.class});
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), new Object[]{OppoProcessManager.class.getName()});
            Slog.i(TAG, "invoke end!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isEnable() {
        if (!this.mFreezeSwitch) {
            return false;
        }
        if (!this.mPowerConnStatus) {
            return this.mBPMSwitch;
        }
        if (isNormalStaus()) {
            return false;
        }
        return true;
    }

    private boolean isInclude(String value, String[] list) {
        if (list == null || list.length <= 0 || value == null) {
            return false;
        }
        try {
            for (String str : list) {
                if (str.equals(value)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Slog.w(TAG, "isInclude has exception! ", e);
            return true;
        }
    }

    private boolean isContains(String value, String[] list) {
        if (list == null || list.length <= 0 || value == null) {
            return false;
        }
        try {
            for (String str : list) {
                if (str.contains(value)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Slog.w(TAG, "isInclude has exception! ", e);
            return true;
        }
    }

    public boolean isInBpmWhiteList(String pkgName) {
        if (isStrictMode()) {
            return isInStrictWhitePkgList(pkgName);
        }
        return !isInBpmList(pkgName) ? isInPkgList(pkgName) : true;
    }

    public boolean isInBpmWhiteList(ProcessRecord app) {
        if (isStrictMode()) {
            return isInStrictWhitePkgList(app);
        }
        return !isInBpmList(app) ? isInPkgList(app) : true;
    }

    public boolean isInBpmList(String pkgName) {
        synchronized (this.mBpmLock) {
            if (this.mBpmList.contains(pkgName)) {
                return true;
            }
            return false;
        }
    }

    public boolean isInBpmList(ProcessRecord app) {
        boolean result = false;
        synchronized (this.mBpmLock) {
            for (String pkg : this.mBpmList) {
                if (isInclude(pkg, app.getPackageList())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isInPkgList(String pkgName) {
        synchronized (this.mPkgLock) {
            if (this.mPkgList.contains(pkgName)) {
                return true;
            }
            return false;
        }
    }

    public boolean isInPkgList(ProcessRecord app) {
        boolean result = false;
        synchronized (this.mPkgLock) {
            for (String pkg : this.mPkgList) {
                if (isInclude(pkg, app.getPackageList())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isInAppWidgetList(String pkgName) {
        synchronized (this.mAppWidgetLock) {
            if (this.mAppWidgetList.contains(pkgName)) {
                return true;
            }
            return false;
        }
    }

    public boolean isInAppWidgetList(ProcessRecord app) {
        boolean result = false;
        synchronized (this.mAppWidgetLock) {
            for (String pkg : this.mAppWidgetList) {
                if (isInclude(pkg, app.getPackageList())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isInDisplayDeviceList(String pkgName) {
        synchronized (this.mDisplayDeviceListLock) {
            if (this.mDisplayDeviceList.contains(pkgName)) {
                return true;
            }
            return false;
        }
    }

    public boolean isInDisplayDeviceList(ProcessRecord app) {
        boolean result = false;
        synchronized (this.mDisplayDeviceListLock) {
            for (String pkg : this.mDisplayDeviceList) {
                if (isInclude(pkg, app.getPackageList())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isInCustomizeAppList(String pkgName) {
        synchronized (this.mCustomizeAppLock) {
            if (this.mCustomizeAppList.contains(pkgName)) {
                return true;
            }
            return false;
        }
    }

    public boolean isInCustomizeAppList(ProcessRecord app) {
        boolean result = false;
        synchronized (this.mCustomizeAppLock) {
            for (String pkg : this.mCustomizeAppList) {
                if (isInclude(pkg, app.getPackageList())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isInGlobalWhiteList(String pkgName) {
        if (this.mGlobalWhiteList.contains(pkgName)) {
            return true;
        }
        return false;
    }

    public boolean isInGlobalWhiteList(ProcessRecord app) {
        for (String pkg : this.mGlobalWhiteList) {
            if (isInclude(pkg, app.getPackageList())) {
                return true;
            }
        }
        return false;
    }

    public boolean isInFromNotityPkgList(String pkgName) {
        if (OppoListManager.getInstance().isFromNotifyPkg(pkgName)) {
            return true;
        }
        return false;
    }

    public boolean isInFromNotityPkgList(ProcessRecord app) {
        boolean result = false;
        if (app == null) {
            return false;
        }
        String[] pkgList = app.getPackageList();
        if (pkgList == null || pkgList.length <= 0) {
            return false;
        }
        try {
            for (String str : pkgList) {
                if (OppoListManager.getInstance().isFromNotifyPkg(str)) {
                    result = true;
                    break;
                }
            }
            return result;
        } catch (Exception e) {
            Slog.w(TAG, "isInFromNotityPkgList has exception! ", e);
            return true;
        }
    }

    public boolean isInBlackSysAppList(String pkgName) {
        synchronized (this.mBlackSysAppLock) {
            if (this.mBlackSysAppList.contains(pkgName)) {
                return true;
            }
            return false;
        }
    }

    public boolean isInBlackSysAppList(ProcessRecord app) {
        boolean result = false;
        synchronized (this.mBlackSysAppLock) {
            for (String pkg : this.mBlackSysAppList) {
                if (isInclude(pkg, app.getPackageList())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isInBlackList(String pkgName) {
        synchronized (this.mBlackAppLock) {
            if (this.mBlackAppList.contains(pkgName)) {
                return true;
            }
            return false;
        }
    }

    public boolean isInBlackList(ProcessRecord app) {
        boolean result = false;
        synchronized (this.mBlackAppLock) {
            for (String pkg : this.mBlackAppList) {
                if (isInclude(pkg, app.getPackageList())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isInBrdList(BroadcastRecord r) {
        if (r == null || r.intent == null) {
            return false;
        }
        synchronized (this.mBrdLock) {
            if (this.mBrdList.contains(r.intent.getAction())) {
                return true;
            }
            return false;
        }
    }

    public boolean isInNewPolicyBrdPkgWhiteList(ProcessRecord app) {
        boolean result = false;
        synchronized (this.mNewPolicyBrdPkgWhiteList) {
            for (String pkg : this.mNewPolicyBrdPkgWhiteList) {
                if (isInclude(pkg, app.getPackageList())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isInNewPolicyBrdActionWhiteList(BroadcastRecord r) {
        if (r == null || r.intent == null) {
            return false;
        }
        synchronized (this.mNewPolicyBrdActionWhiteList) {
            if (this.mNewPolicyBrdActionWhiteList.contains(r.intent.getAction())) {
                return true;
            }
            return false;
        }
    }

    public boolean isInContentProviderWhiteList(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        synchronized (this.mCprLock) {
            if (this.mCprList.size() <= 0) {
                return false;
            }
            for (String cprName : this.mCprList) {
                if (pkgName.equals(cprName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isInBlackAppBrdList(BroadcastRecord r) {
        if (r == null || r.intent == null) {
            return false;
        }
        synchronized (this.mBlackAppBrdLock) {
            if (this.mBlackAppBrdList.contains(r.intent.getAction())) {
                if (this.mDebugSwitch) {
                    Log.d(TAG, "isInBlackAppBrdList " + r.intent.getAction());
                }
                return true;
            }
            return false;
        }
    }

    public boolean isInStrictWhitePkgList(String pkg) {
        if (pkg == null) {
            return false;
        }
        synchronized (this.mStrictWhitePkgListLock) {
            if (this.mStrictWhitePkgList.contains(pkg)) {
                if (this.mDebugSwitch) {
                    Log.d(TAG, "isInStrictWhitePkgList " + pkg);
                }
                return true;
            }
            return false;
        }
    }

    public boolean isInStrictWhitePkgList(ProcessRecord app) {
        boolean result = false;
        synchronized (this.mStrictWhitePkgListLock) {
            for (String pkg : this.mStrictWhitePkgList) {
                if (isInclude(pkg, app.getPackageList())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isInBrdWhiteKey(ProcessRecord app) {
        boolean result = false;
        synchronized (this.mBrdWhiteKeyList) {
            for (String key : this.mBrdWhiteKeyList) {
                if (isContains(key, app.getPackageList())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public void updateBpmList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateBpmList!");
        }
        synchronized (this.mBpmLock) {
            this.mBpmList.clear();
            this.mBpmList.addAll(OppoBPMUtils.getInstance().getVisibleList());
        }
    }

    public void updatePkgList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updatePkgList!");
        }
        synchronized (this.mPkgLock) {
            this.mPkgList.clear();
            this.mPkgList.addAll(OppoBPMUtils.getInstance().getInVisibleList());
        }
    }

    public void updateBrdList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateBrdList!");
        }
        synchronized (this.mBrdLock) {
            this.mBrdList.clear();
            this.mBrdList.addAll(OppoBPMUtils.getInstance().getBrdWhiteList());
        }
    }

    public void updateCprList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateCprList!");
        }
    }

    public void updateBlackSysAppList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateBlackSysAppList!");
        }
        synchronized (this.mBlackSysAppLock) {
            this.mBlackSysAppList.clear();
            this.mBlackSysAppList.addAll(OppoBPMUtils.getInstance().getSysBlackList());
        }
    }

    public void updateAppWidgetList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateAppWidgetList!");
        }
        synchronized (this.mAppWidgetLock) {
            this.mAppWidgetList.clear();
            this.mAppWidgetList.addAll(OppoBPMUtils.getInstance().getAppWidgetList());
        }
    }

    public void updateBlackAppList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateBlackAppList!");
        }
        synchronized (this.mBlackAppLock) {
            this.mBlackAppList.clear();
            this.mBlackAppList.addAll(OppoBPMUtils.getInstance().getThirdAppBlackList());
        }
    }

    public void updateStrictWhitePkgList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateStrictWhitePkgList!");
        }
        synchronized (this.mStrictWhitePkgListLock) {
            this.mStrictWhitePkgList.clear();
            this.mStrictWhitePkgList.addAll(OppoBPMUtils.getInstance().getStrictModeList());
        }
    }

    public void updateCustomizeAppList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateCustomizeAppList!");
        }
        synchronized (this.mCustomizeAppLock) {
            this.mCustomizeAppList.clear();
            this.mCustomizeAppList.addAll(OppoBPMUtils.getInstance().getCustomizeAppList());
        }
    }

    public void updateBrdBlackList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateBrdBlackList!");
        }
        synchronized (this.mBlackAppBrdLock) {
            this.mBlackAppBrdList.clear();
            this.mBlackAppBrdList.addAll(OppoBPMUtils.getInstance().getBrdBlackList());
        }
    }

    public void updateNewPolicyBrdPkgWhiteList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateNewPolicyBrdPkgWhiteList!");
        }
        synchronized (this.mNewPolicyBrdPkgWhiteList) {
            this.mNewPolicyBrdPkgWhiteList.clear();
            this.mNewPolicyBrdPkgWhiteList.addAll(OppoBPMUtils.getInstance().getNewPolicyBrdPkgWhiteList());
        }
    }

    public void updateNewPolicyBrdActionWhiteList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateNewPolicyBrdActionWhiteList!");
        }
        synchronized (this.mNewPolicyBrdActionWhiteList) {
            this.mNewPolicyBrdActionWhiteList.clear();
            this.mNewPolicyBrdActionWhiteList.addAll(OppoBPMUtils.getInstance().getNewPolicyBrdActionWhiteList());
        }
    }

    public void updateGlobalWhiteList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateGlobalWhiteList!");
        }
        this.mGlobalWhiteList = OppoListManager.getInstance().getGlobalWhiteList(this.mActivityManager.mContext);
    }

    public void updatePowerConnStatus() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updatePowerConnStatus!");
        }
        this.mPowerConnStatus = OppoBPMUtils.getInstance().getPowerConnectStatus();
        if (this.mPowerConnStatus) {
            elsaAppChange(-2, NOTIFY_ELSA_CURRENT_STATUS, -2, NOTIFY_ELSA_CURRENT_STATUS);
        } else {
            elsaAppChange(-3, NOTIFY_ELSA_CURRENT_STATUS, -3, NOTIFY_ELSA_CURRENT_STATUS);
        }
    }

    public void updateDisplayDeviceList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateDisplayDeviceList!");
        }
        synchronized (this.mDisplayDeviceListLock) {
            this.mDisplayDeviceList.clear();
            this.mDisplayDeviceList.addAll(OppoBPMUtils.getInstance().getDisplayDeviceList());
        }
    }

    public void updateBrdWhiteKeyList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateBrdWhiteKeyList!");
        }
        synchronized (this.mBrdWhiteKeyList) {
            this.mBrdWhiteKeyList.clear();
            this.mBrdWhiteKeyList.addAll(OppoBPMUtils.getInstance().getBrdWhiteKeyList());
        }
    }

    public void updateAssociateKeyList() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateAssociateKeyList!");
        }
        synchronized (this.mAssociateKeyList) {
            this.mAssociateKeyList.clear();
            this.mAssociateKeyList.addAll(OppoBPMUtils.getInstance().getAssociateKeyList());
        }
        synchronized (this.mAssociateKeyMap) {
            this.mAssociateKeyMap.clear();
            this.mAssociateKeyMap.putAll(OppoBPMUtils.getInstance().getAssociateKeyMap());
        }
    }

    public void updateBpmConfig() {
        if (this.mDebugSwitch) {
            Log.d(TAG, "updateBpmConfig!");
        }
        this.mRecordSwitch = OppoBPMUtils.getInstance().isForumVersion();
        this.mRecentNum = OppoBPMUtils.getInstance().getRecentTaskNum();
        this.mRecentStore = OppoBPMUtils.getInstance().getRecentTaskStore();
        this.mScreenOnCheckTime = (long) OppoBPMUtils.getInstance().getScreenOffCheckTime();
        this.mAppChangeCheckTime = (long) OppoBPMUtils.getInstance().getAppChangeCheckTime();
        this.mPeriodCheckTime = (long) OppoBPMUtils.getInstance().getPeriodCheckTime();
        this.mStrictModeSwitch = OppoBPMUtils.getInstance().getStrictModeSwitch();
        this.mStrictModeEnterTime = (long) OppoBPMUtils.getInstance().getStrictModeEnterTime();
        this.mPaySafeSwitch = OppoBPMUtils.getInstance().getPayModeSwitch();
        this.mPayModeEnterTime = (long) OppoBPMUtils.getInstance().getPayModeEnterTime();
        this.mStartFromNotityTime = OppoBPMUtils.getInstance().getStartFromNotityTime();
        this.mFreezeSwitch = OppoBPMUtils.getInstance().getFreezeSwitch();
        this.mStatisticsSwitch = OppoBPMUtils.getInstance().getStatisticsSwitch();
        this.mNewPolicyBrdEnable = OppoBPMUtils.getInstance().getNewPolicyBrdEnable();
    }

    public void handleElsaConfigFileMsg() {
        OppoBPMUtils.getInstance().readElsaConfigFile();
        updatePkgList();
        updateStrictWhitePkgList();
        updateBrdList();
        updateBrdBlackList();
        updateBlackSysAppList();
        updateBlackAppList();
        updateNewPolicyBrdActionWhiteList();
        updateNewPolicyBrdPkgWhiteList();
        updateBpmConfig();
        updateBrdWhiteKeyList();
        updateAssociateKeyList();
    }

    public void handleUpdateBpmFileMsg() {
        OppoBPMUtils.getInstance().readBpmFile();
        updateBpmList();
    }

    public void handleAppWidgetUpdateMsg() {
        String widgetPkgName = null;
        List<String> oldAppWidgetList = new ArrayList();
        oldAppWidgetList.addAll(this.mAppWidgetList);
        updateAppWidgetList();
        if (this.mAppWidgetList.size() > oldAppWidgetList.size()) {
            for (String pkg : this.mAppWidgetList) {
                if (!oldAppWidgetList.contains(pkg)) {
                    widgetPkgName = pkg;
                    break;
                }
            }
        }
        if (isEnable()) {
            updateProcessStateForWidgetChanged(widgetPkgName);
        }
        oldAppWidgetList.clear();
    }

    public void handlePowerConnStsUpdateMsg() {
        OppoBPMUtils.getInstance().readPowerStateFile();
        updatePowerConnStatus();
        Slog.i(TAG, "handlePowerConnStsUpdateMsg " + this.mPowerConnStatus);
    }

    public void handleDisplayDeviceUpdateMsg() {
        updateDisplayDeviceList();
        displayListToSendBroadcastLocked();
    }

    public void handleResumeMsg(Message msg) {
        int uid = msg.arg1;
        int reason = msg.arg2;
        String processName = msg.obj;
        if (this.mDynamicDebug) {
            Slog.i(TAG, "bpm debug log: resume msg enter:  " + processName);
        }
        setPackageResume(uid, processName, codeToReasonStr(reason));
    }

    public void handleScreenOnMsg() {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "handleScreenOnMsg!");
        }
        this.mIsScreenOn = true;
        elsaAppChange(-4, NOTIFY_ELSA_CURRENT_STATUS, -4, NOTIFY_ELSA_CURRENT_STATUS);
    }

    public void handleScreenOffMsg() {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "handleScreenOffMsg!");
        }
        this.mIsScreenOn = false;
        elsaAppChange(-1, NOTIFY_ELSA_CURRENT_STATUS, -1, NOTIFY_ELSA_CURRENT_STATUS);
        if (isStrictMode()) {
            stopStrictMode();
        } else {
            this.mBpmHandler.removeMessages(MSG_READY_ENTER_STRICTMODE);
        }
        setBpmStatus(0);
    }

    public void handleRecordResumeReasonMsg(Message msg) {
        if (this.mRecordSwitch) {
            int pid = msg.arg1;
            int reason = msg.arg2;
            ProcessRecord app = getProcessForPid(Integer.toString(pid));
            if (!(app == null || checkWhiteProcessRecord(app) || !this.mDebugSwitch)) {
                Slog.i(TAG, "bpmhandle: resume " + app.processName + " reason is " + codeToReasonStr(reason));
            }
        }
    }

    public void sendReadyStrictModeMsg() {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "sendReadyStrictModeMsg!");
        }
        if (isStrictMode()) {
            Slog.i(TAG, "sendReadyStrictModeMsg already in strictmode");
            return;
        }
        if (this.mBpmHandler.hasMessages(MSG_READY_ENTER_STRICTMODE)) {
            this.mBpmHandler.removeMessages(MSG_READY_ENTER_STRICTMODE);
        }
        if (isPaySafeStaus()) {
            sendBpmEmptyMessage(MSG_READY_ENTER_STRICTMODE, this.mPayModeEnterTime);
        } else {
            sendBpmEmptyMessage(MSG_READY_ENTER_STRICTMODE, this.mStrictModeEnterTime);
        }
    }

    public void handleApplicationSwitch(int prePkgUid, String prePkgName, int nextPkgUid, String nextPkgName) {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "handleApplicationSwitch mStrictModeSwitch = " + this.mStrictModeSwitch + " prePkgUid " + prePkgUid + " prePkgName " + prePkgName + " nextPkgUid " + nextPkgUid + "  nextPkgName " + nextPkgName);
        }
        if (this.mBpmHandler.hasMessages(300)) {
            this.mBpmHandler.removeMessages(300);
        }
        Bundle bundle = new Bundle();
        bundle.putString("pre", prePkgName);
        bundle.putString("next", nextPkgName);
        bundle.putInt("preUid", prePkgUid);
        bundle.putInt("nextUid", nextPkgUid);
        sendBpmMessage(300, 0, bundle);
        if (this.mStrictModeSwitch) {
            if (this.mPaySafeSwitch) {
                if (OppoGameSpaceManager.getInstance().inGameSpacePkgList(nextPkgName)) {
                    if (OppoGameSpaceManager.getInstance().isBpmEnable()) {
                        if (isStrictMode()) {
                            stopStrictMode();
                        }
                        setBpmStatus(1);
                        sendReadyStrictModeMsg();
                        return;
                    }
                } else if (OppoListManager.getInstance().inPaySafePkgList(nextPkgName)) {
                    setBpmStatus(2);
                    sendReadyStrictModeMsg();
                    return;
                }
            } else if (OppoGameSpaceManager.getInstance().inGameSpacePkgList(nextPkgName) && OppoGameSpaceManager.getInstance().isBpmEnable()) {
                if (isStrictMode()) {
                    stopStrictMode();
                }
                setBpmStatus(1);
                sendReadyStrictModeMsg();
                return;
            }
        }
        if (isStrictMode()) {
            stopStrictMode();
        } else {
            this.mBpmHandler.removeMessages(MSG_READY_ENTER_STRICTMODE);
        }
        setBpmStatus(0);
    }

    public void handleReadyStrictModeMsg() {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "handleReadyStrictModeMsg!");
        }
        enterStrictMode();
    }

    public boolean isStrictMode() {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isStrictMode " + this.mStrictMode);
        }
        return this.mStrictMode;
    }

    private void setStrictMode(boolean mode) {
        this.mStrictMode = mode;
    }

    public boolean isPaySafeStaus() {
        return this.mModeStatus == 2;
    }

    public boolean isNormalStaus() {
        return this.mModeStatus == 0;
    }

    private void setBpmStatus(int status) {
        this.mModeStatus = status;
    }

    private void notifyStopStrictMode() {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "notifyStopStrictMode!");
        }
        OppoBPMHelper.notifyStopStrictMode();
    }

    public void enterStrictMode() {
        if (this.mStrictModeSwitch) {
            if (this.mDynamicDebug) {
                Slog.i(TAG, "enterStrictMode!");
            }
            setStrictMode(true);
        }
    }

    public void stopStrictMode() {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "stopStrictMode!");
        }
        setStrictMode(false);
        notifyStopStrictMode();
        this.mBpmHandler.removeMessages(MSG_READY_ENTER_STRICTMODE);
        updateProcessStateForStopStrictMode();
    }

    public boolean isDelayAppAlarm(int callerPid, int callerUid, int calledUid, String calledPkg) {
        boolean isDelay = false;
        if (!isStrictMode() && (isEnable() ^ 1) != 0) {
            return false;
        }
        if (calledUid < 10000) {
            if (this.mDebugSwitch) {
                Slog.i(TAG, "isDelayAppAlarm return for uid < 10000!");
            }
            return false;
        }
        if (callerUid == calledUid) {
            if (isFrozingByUid(callerPid)) {
                isDelay = true;
            }
        } else if (!checkWhitePackage(calledPkg)) {
            ActivityRecord topAr = this.mActivityManager.mStackSupervisor.getTopRunningActivityLocked();
            if (!(topAr == null || calledPkg == null || topAr.packageName == null || (topAr.packageName.equals(calledPkg) ^ 1) == 0)) {
                isDelay = true;
            }
            if (isMultiWindowTopPkg(calledPkg)) {
                if (this.mDebugSwitch) {
                    Slog.i(TAG, "isDelayAppAlarm return for mutil window app");
                }
                isDelay = true;
            }
        }
        if (this.mDebugSwitch) {
            Slog.i(TAG, "isDelayAppAlarm return " + isDelay);
        }
        return isDelay;
    }

    public boolean isDelayAppSync(int uid, String pkg) {
        boolean isDelay = false;
        if (!isStrictMode()) {
            return false;
        }
        if (uid < 10000) {
            if (this.mDebugSwitch) {
                Slog.i(TAG, "isDelayAppSync return for uid < 10000!");
            }
            return false;
        }
        if (!checkWhitePackage(pkg)) {
            isDelay = true;
        }
        if (this.mDebugSwitch) {
            Slog.i(TAG, "isDelayAppSync return " + isDelay);
        }
        return isDelay;
    }

    public boolean isDelayAppJob(int uid, String pkg) {
        boolean isDelay = false;
        if (!isStrictMode()) {
            return false;
        }
        if (uid < 10000) {
            if (this.mDebugSwitch) {
                Slog.i(TAG, "isDelayAppJob return for uid < 10000!");
            }
            return false;
        }
        if (!checkWhitePackage(pkg)) {
            ActivityRecord topAr = this.mActivityManager.mStackSupervisor.getTopRunningActivityLocked();
            if (!(topAr == null || pkg == null || topAr.packageName == null || (topAr.packageName.equals(pkg) ^ 1) == 0)) {
                isDelay = true;
            }
            if (isMultiWindowTopPkg(pkg)) {
                if (this.mDebugSwitch) {
                    Slog.i(TAG, "isDelayAppJob return for mutil window app");
                }
                isDelay = true;
            }
        }
        if (this.mDebugSwitch) {
            Slog.i(TAG, "isDelayAppJob return " + isDelay);
        }
        return isDelay;
    }

    public String codeToReasonStr(int code) {
        switch (code) {
            case 1:
                return "broadcast";
            case 2:
                return "service";
            case 3:
                return "provider";
            case 4:
                return RESUME_REASON_TOPAPP_STR;
            case 5:
                return RESUME_REASON_MOUNT_STR;
            case 6:
                return RESUME_REASON_BLUETOOTH_STR;
            case 7:
                return RESUME_REASON_MEDIA_STR;
            case 8:
                return RESUME_REASON_SWITCH_CHANGE_STR;
            case 9:
                return RESUME_REASON_SWITCH_CHANGE_STR;
            case 10:
                return RESUME_REASON_BROADCAST_TIMEOUT_STR;
            case 11:
                return RESUME_REASON_SERVICE_TIMEOUT_STR;
            case 12:
                return RESUME_REASON_SYSTEM_CALL_STR;
            case 13:
                return RESUME_REASON_NOTIFY_STR;
            case 14:
                return RESUME_REASON_STOP_STRICTMODE_STR;
            case 15:
                return RESUME_REASON_VISIBLE_WINDOW_STR;
            default:
                return Shell.NIGHT_MODE_STR_UNKNOWN;
        }
    }

    public int strToReasonCode(String reason) {
        int code = 0;
        if (reason == null) {
            return 0;
        }
        if (reason.equals(RESUME_REASON_MOUNT_STR)) {
            code = 5;
        } else if (reason.equals(RESUME_REASON_BLUETOOTH_STR)) {
            code = 6;
        } else if (reason.equals(RESUME_REASON_MEDIA_STR)) {
            code = 7;
        } else if (reason.equals(RESUME_REASON_NOTIFY_STR)) {
            code = 13;
        }
        return code;
    }

    public boolean isFrozingByApp(ProcessRecord app) {
        if (app == null) {
            return false;
        }
        return isFrozingByUid(app.uid);
    }

    public boolean isFrozingByUid(int uid) {
        if (uid == 0) {
            return false;
        }
        int freezeState = elsaGetPackageFreezing(uid);
        boolean state = (freezeState == 1 || freezeState == 2) ? true : freezeState == 3;
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isFrozingByUid uid " + uid + " state " + state);
        }
        return state;
    }

    public boolean isResumeByUid(int uid) {
        if (uid == 0) {
            return false;
        }
        boolean state = elsaGetPackageFreezing(uid) <= 0;
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isResumeByUid uid " + uid + " state " + state);
        }
        return state;
    }

    public void setPackageResumeAndEnterFreeze(int uid, String packageName, String reason) {
        elsaResume(uid, packageName, 0, 1, reason);
        Slog.i(TAG, uid + " " + packageName + " resume!!" + " state " + 0 + " reason " + reason);
    }

    public void setPackageResume(int uid, String packageName, String reason) {
        if (RESUME_REASON_BROADCAST_TIMEOUT_STR.equals(reason) || RESUME_REASON_SERVICE_TIMEOUT_STR.equals(reason)) {
            setPackageResumeAndEnterFreeze(uid, packageName, reason);
            return;
        }
        elsaResume(uid, packageName, -1, 0, reason);
        Slog.i(TAG, uid + " " + packageName + " resume!!" + " reason " + reason);
    }

    public void setPackageResume(int uid, String packageName, int timeout, int isTargetFreeze, String reason) {
        elsaResume(uid, packageName, timeout, isTargetFreeze, reason);
        Slog.i(TAG, uid + " " + packageName + " resume!!" + " isTargetFreeze " + isTargetFreeze + " reason " + reason);
    }

    private IElsaManager getIElsaManager() {
        try {
            IBinder binder = ServiceManager.checkService(IElsaManager.DESCRIPTOR);
            if (binder != null) {
                this.mIElsaManager = new ElsaManagerProxy(binder);
            }
        } catch (Exception e) {
            this.mIElsaManager = null;
            Slog.i(TAG, "getIElsaManager failed!");
        }
        return this.mIElsaManager;
    }

    private boolean ensureGetIElsaManager() {
        return getIElsaManager() != null;
    }

    public int elsaSetPackageFreezing(int uid, String pkgName, int freezeLevel) {
        int state = -1;
        if (TextUtils.isEmpty(pkgName) || uid <= 0) {
            return state;
        }
        if (ensureGetIElsaManager()) {
            try {
                state = this.mIElsaManager.elsaSetPackageFreezing(uid, pkgName, freezeLevel, 4);
            } catch (Exception e) {
                Slog.e(TAG, "elsaSetPackageFreezing failed!");
            }
        }
        if (this.mDynamicDebug) {
            Slog.d(TAG, "elsaSet uid " + uid + " name " + pkgName + " state " + state);
        }
        return state;
    }

    public int elsaGetPackageFreezing(int uid) {
        int state = 4;
        if (ensureGetIElsaManager()) {
            try {
                state = this.mIElsaManager.elsaGetPackageFreezing(uid, 4);
            } catch (Exception e) {
                Slog.e(TAG, "elsaGetPackageFreezing failed!");
            }
        }
        if (this.mDynamicDebug) {
            Slog.d(TAG, "elsaGet uid " + uid + " state " + state);
        }
        return state;
    }

    public void elsaResume(int uid, String packageName, int timeout, int isTargetFreeze, String reason) {
        if (ensureGetIElsaManager() && uid > 0) {
            try {
                this.mIElsaManager.elsaResume(uid, timeout, isTargetFreeze, 4, reason);
                if (-1 == timeout && isTargetFreeze == 0 && (reason.equals("top-activity") ^ 1) != 0) {
                    notifyPackageResume(uid, packageName, reason);
                }
                if (this.mDynamicDebug) {
                    Slog.d(TAG, "elsaResume uid " + uid + " reason " + reason);
                }
            } catch (Exception e) {
                Slog.e(TAG, "elsaResume failed!");
            }
        }
    }

    public void elsaAppChange(int preUid, String prePkgName, int nextUid, String nextPkgName) {
        if (ensureGetIElsaManager()) {
            try {
                this.mIElsaManager.elsaNotifyAppSwitch(preUid, prePkgName, nextUid, nextPkgName);
                if (this.mDynamicDebug) {
                    Slog.d(TAG, "elsaAppChange preUid " + preUid + " prePkgName " + prePkgName + " nextUid " + nextUid + " nextPkgName " + nextPkgName);
                }
            } catch (Exception e) {
                Slog.e(TAG, "elsaAppChange failed!");
            }
        }
    }

    public void noteWindowStateChange(int uid, int pid, int windowId, int windowType, boolean isVisible, boolean shown) {
        if (this.mDynamicDebug) {
            Slog.v(TAG, "noteWindowStateChange uid = " + uid + "  pid = " + pid + "  windowId = " + windowId + "  isVisible = " + isVisible + "  shown = " + shown + "  windowType = " + windowType);
        }
        if (windowType == 2005 || windowType == 2003 || windowType == 2002 || windowType == 2007 || windowType == 2038 || windowType == 2006 || windowType == 2010) {
            sendBpmMessage(111, uid, windowId, isVisible & shown, 0);
            return;
        }
        if (this.mDynamicDebug) {
            Slog.v(TAG, "noteWindowStateChange return!");
        }
    }

    /* JADX WARNING: Missing block: B:45:0x0127, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleWindowStateChangeMsg(Message msg) {
        if (this.mDynamicDebug) {
            Slog.v(TAG, "handleWindowStateChangeMsg msg = " + msg);
        }
        int uid = msg.arg1;
        int windowId = msg.arg2;
        boolean isVisible = ((Boolean) msg.obj).booleanValue();
        synchronized (this.mVisibleWindowMap) {
            boolean inVisibleMap = this.mVisibleWindowMap.containsKey(Integer.valueOf(uid));
            Integer windowid = new Integer(windowId);
            List<Integer> windowIdList;
            if (isVisible) {
                if (inVisibleMap) {
                    windowIdList = (List) this.mVisibleWindowMap.get(Integer.valueOf(uid));
                    if (!windowIdList.contains(windowid)) {
                        windowIdList.add(windowid);
                        this.mVisibleWindowMap.put(Integer.valueOf(uid), windowIdList);
                    }
                } else {
                    windowIdList = new ArrayList();
                    windowIdList.add(windowid);
                    this.mVisibleWindowMap.put(Integer.valueOf(uid), windowIdList);
                }
            } else if (inVisibleMap) {
                windowIdList = (List) this.mVisibleWindowMap.get(Integer.valueOf(uid));
                if (windowIdList.contains(windowid)) {
                    windowIdList.remove(windowid);
                }
                if (windowIdList.size() > 0) {
                    this.mVisibleWindowMap.put(Integer.valueOf(uid), windowIdList);
                } else {
                    this.mVisibleWindowMap.remove(Integer.valueOf(uid));
                }
            }
            int i;
            if (this.mVisibleWindowList.isEmpty() && this.mVisibleWindowMap.isEmpty()) {
            } else if (this.mVisibleWindowList.size() == this.mVisibleWindowMap.size()) {
                ArrayList<Integer> tmpList = new ArrayList();
                boolean diff = false;
                for (i = 0; i < this.mVisibleWindowMap.size(); i++) {
                    Integer widnowUid = (Integer) this.mVisibleWindowMap.keyAt(i);
                    tmpList.add(widnowUid);
                    if (null == null && (this.mVisibleWindowList.contains(widnowUid) ^ 1) != 0) {
                        diff = true;
                        break;
                    }
                }
                if (diff) {
                    this.mVisibleWindowList.clear();
                    this.mVisibleWindowList.addAll(tmpList);
                    windowStateChangeToSendBroadcastLocked();
                }
            } else {
                this.mVisibleWindowList.clear();
                for (i = 0; i < this.mVisibleWindowMap.size(); i++) {
                    this.mVisibleWindowList.add((Integer) this.mVisibleWindowMap.keyAt(i));
                }
                windowStateChangeToSendBroadcastLocked();
            }
        }
    }

    private boolean isMultiWindowTopPkg(String pkgName) {
        if (this.mActivityManager == null || pkgName == null || !this.mActivityManager.getAllTopPkgName().contains(pkgName)) {
            return false;
        }
        return true;
    }

    private boolean isMultiWindowTopPkg(ProcessRecord app) {
        if (this.mActivityManager == null || app == null || app.info == null || app.info.packageName == null || !this.mActivityManager.getAllTopPkgName().contains(app.info.packageName)) {
            return false;
        }
        return true;
    }

    private void handleAppChangeNofiElsa(Message msg) {
        Bundle data = msg.getData();
        if (data != null) {
            String preName = data.getString("pre");
            int preUid = data.getInt("preUid", -1);
            String nextName = data.getString("next");
            int nextUid = data.getInt("nextUid", -1);
            if (!TextUtils.isEmpty(preName) && !TextUtils.isEmpty(nextName)) {
                preUid = getAppRecordUid(preUid, preName);
                nextUid = getAppRecordUid(nextUid, nextName);
                if (preUid != -1 && nextUid != -1) {
                    elsaAppChange(preUid, preName, nextUid, nextName);
                    if (nextUid > 10000 && (checkWhitePackage(nextName) ^ 1) != 0 && isFrozingByUid(nextUid)) {
                        setPackageResume(nextUid, nextName, "top-activity");
                    }
                }
            }
        }
    }

    private int getAppRecordUid(int uid, String packageName) {
        int result;
        if (uid != -1) {
            result = uid;
            if (this.mAllAppRecordMap.containsKey(packageName)) {
                return result;
            }
            this.mAllAppRecordMap.put(packageName, Integer.valueOf(uid));
            return result;
        } else if (this.mAllAppRecordMap.containsKey(packageName)) {
            return ((Integer) this.mAllAppRecordMap.get(packageName)).intValue();
        } else {
            PackageInfo info = getPackageInfo(packageName);
            if (info == null || info.applicationInfo == null) {
                return -1;
            }
            result = info.applicationInfo.uid;
            this.mAllAppRecordMap.put(packageName, Integer.valueOf(result));
            return result;
        }
    }

    public PackageInfo getPackageInfo(String pkgName) {
        PackageInfo info = null;
        if (this.mActivityManager == null || (TextUtils.isEmpty(pkgName) ^ 1) == 0) {
            return info;
        }
        try {
            return this.mActivityManager.mContext.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (Exception e) {
            return info;
        }
    }

    public void resumeTopApp(ProcessRecord app) {
        if (app != null && app.adjType != null && app.uid > 10000 && "top-activity".equals(app.adjType) && app.curAdj < 200 && app.setSchedGroup == app.curSchedGroup && isFrozingByUid(app.uid)) {
            setPackageResume(app.uid, app.processName, "top-activity");
        }
    }

    public List<ColorPackageFreezeData> getRunningProcesses() {
        List<ColorPackageFreezeData> appFreezeList = new ArrayList();
        synchronized (this.mActivityManager) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                for (int i = this.mActivityManager.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord app = (ProcessRecord) this.mActivityManager.mLruProcesses.get(i);
                    if (app != null && app.uid > 10000) {
                        ColorPackageFreezeData data = new ColorPackageFreezeData();
                        data.setPid(app.pid);
                        data.setUid(app.uid);
                        data.setCurAdj(app.curAdj);
                        data.setUserId(app.userId);
                        data.setProcessName(app.processName);
                        String[] pkgList = app.getPackageList();
                        if (pkgList != null) {
                            data.setPackageList(Arrays.asList(pkgList));
                        } else {
                            data.setPackageList(null);
                        }
                        appFreezeList.add(data);
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return appFreezeList;
    }

    /* JADX WARNING: Missing block: B:20:0x0041, code:
            return;
     */
    /* JADX WARNING: Missing block: B:22:0x0043, code:
            r3 = r4.mAssociateKeyMap;
     */
    /* JADX WARNING: Missing block: B:23:0x0045, code:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:26:0x004c, code:
            if (r4.mAssociateKeyMap.containsKey(r0) == false) goto L_0x0067;
     */
    /* JADX WARNING: Missing block: B:28:0x0054, code:
            if (r4.mAssociateKeyMap.containsValue(r1) == false) goto L_0x0067;
     */
    /* JADX WARNING: Missing block: B:30:0x005a, code:
            if (isFrozingByUid(r5) == false) goto L_0x0062;
     */
    /* JADX WARNING: Missing block: B:31:0x005c, code:
            setPackageResume(r5, r1, "assocationKey");
     */
    /* JADX WARNING: Missing block: B:32:0x0062, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:33:0x0063, code:
            return;
     */
    /* JADX WARNING: Missing block: B:37:0x0067, code:
            monitor-exit(r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void resumeAppAssociated(int calledUid, ComponentName calledName) {
        if (calledName != null) {
            String calledPkg = calledName.getPackageName();
            String calledCpn = calledName.getClassName();
            if (!(TextUtils.isEmpty(calledPkg) || (TextUtils.isEmpty(calledCpn) ^ 1) == 0)) {
                if (this.mFeatureExpRom) {
                    if (isFrozingByUid(calledUid)) {
                        setPackageResume(calledUid, calledPkg, "assocationKey_exp");
                    }
                    return;
                }
                synchronized (this.mBrdWhiteKeyList) {
                    if (this.mBrdWhiteKeyList.contains(calledPkg)) {
                        if (isFrozingByUid(calledUid)) {
                            setPackageResume(calledUid, calledPkg, "assocationKey");
                        }
                    }
                }
            }
        }
    }

    public void setAppFreezeController(IOppoAppFreezeController controller) {
        this.mController = controller;
    }

    public void notifyPackageResume(int uid, String pkgName, String reason) {
        if (this.mController != null) {
            try {
                this.mController.notifyPackageResume(uid, pkgName, reason);
            } catch (Exception e) {
                this.mController = null;
                Slog.e(TAG, "notifyPackageResume failed!!");
            }
        }
    }
}

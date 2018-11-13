package com.android.server.am;

import android.app.ActivityManager.RecentTaskInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.util.Slog;
import android.view.inputmethod.InputMethodInfo;
import com.android.server.LocalServices;
import com.android.server.OppoBPMHelper;
import com.android.server.OppoBPMUtils;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.DisplayTransformManager;
import com.android.server.wm.WindowManagerService;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
public class OppoProcessManager {
    public static final int MSG_APP_DIED = 182;
    public static final int MSG_READY_ENTER_STRICTMODE = 150;
    public static final int MSG_RECORD_RESUME_REASON = 140;
    public static final int MSG_REMOVE_RECORD = 181;
    public static final int MSG_RESUME_PROCESS = 318;
    public static final int MSG_SCREEN_OFF = 121;
    public static final int MSG_SCREEN_ON = 120;
    public static final int MSG_SUSPEND_PROCESS = 319;
    public static final int MSG_SUSPEND_PROCESS_DELAY = 320;
    public static final int MSG_UPDATE_APPWIDGET = 105;
    public static final int MSG_UPDATE_BLACK_APP = 106;
    public static final int MSG_UPDATE_BLACK_SYS_APP = 104;
    public static final int MSG_UPDATE_BPM = 101;
    public static final int MSG_UPDATE_BRD = 103;
    public static final int MSG_UPDATE_CPR = 112;
    public static final int MSG_UPDATE_DISPLAYDEVICE = 111;
    public static final int MSG_UPDATE_LOW_POWER = 109;
    public static final int MSG_UPDATE_PKG = 102;
    public static final int MSG_UPDATE_POWER_CONN_STS = 107;
    public static final int MSG_UPDATE_PROC_STATE = 130;
    public static final int MSG_UPDATE_SMART_LOW_POWER = 108;
    public static final int MSG_UPDATE_STS = 100;
    public static final int MSG_UPDATE_SYS_PURE_BKG_CONFIG = 110;
    public static final int MSG_UPLOAD = 180;
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
    public static final int RESUME_SINGNAL = 18;
    public static final int STATUS_GAME = 1;
    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_PAYSAFE = 2;
    private static final long SUSPEND_DELAY = 5000;
    private static final long SUSPEND_DELAY_LONG = 180000;
    public static final int SUSPEND_SINGNAL = 19;
    public static final String TAG = "OppoProcessManager";
    private static final long THREAD_SLEEP_TIME = 20000;
    public static final int UNSUSPEND_REASON_ADJ = 4;
    public static final int UNSUSPEND_REASON_AUDIO = 7;
    public static final int UNSUSPEND_REASON_AUDIO_ASS = 15;
    public static final int UNSUSPEND_REASON_DISPLAY = 16;
    public static final int UNSUSPEND_REASON_HOME = 11;
    public static final int UNSUSPEND_REASON_IM = 6;
    public static final int UNSUSPEND_REASON_KILL_GPS = 13;
    public static final int UNSUSPEND_REASON_KILL_WAKELOCK = 14;
    public static final int UNSUSPEND_REASON_ORDER_BRD = 5;
    public static final int UNSUSPEND_REASON_RECENT_TASK = 12;
    public static final int UNSUSPEND_REASON_SYSTEM_ASS = 3;
    public static final int UNSUSPEND_REASON_TOAST = 10;
    public static final int UNSUSPEND_REASON_TOP = 1;
    public static final int UNSUSPEND_REASON_TOP_ASS = 2;
    public static final int UNSUSPEND_REASON_TOUCH_WIN = 8;
    public static final int UNSUSPEND_REASON_WAKELOCK = 9;
    private static final long UPLOAD_INTERVAL_TIME = 21600000;
    private static final int UPLOAD_NORMAL = 1;
    private static final int UPLOAD_POWER_CONN = 2;
    private static OppoProcessManager mOppoProcessManager;
    public static boolean sDebugDetail;
    private ContentObserver dufaultInputMethodObserver;
    private ActivityManagerService mActivityManager;
    private List<String> mAppWidgetList;
    private final Object mAppWidgetLock;
    private AudioManager mAudioManager;
    private boolean mBPMSwitch;
    private List<String> mBlackAppBrdList;
    private final Object mBlackAppBrdLock;
    private List<String> mBlackAppList;
    private final Object mBlackAppLock;
    private List<String> mBlackSysAppList;
    private final Object mBlackSysAppLock;
    private BPMHandler mBpmHandler;
    private List<String> mBpmList;
    private final Object mBpmLock;
    private List<String> mBrdList;
    private final Object mBrdLock;
    private List<String> mCprList;
    private final Object mCprLock;
    private List<String> mCustomizeAppList;
    private final Object mCustomizeAppLock;
    boolean mDebugSwitch;
    private String mDefaultInputMethod;
    private List<String> mDisplayDeviceList;
    private final Object mDisplayDeviceListLock;
    boolean mDynamicDebug;
    private List<String> mGlobalWhiteList;
    private boolean mIsInOffHook;
    private boolean mIsScreenOn;
    private boolean mLowPower;
    private int mModeStatus;
    private long mPayModeEnterTime;
    private boolean mPaySafeSwitch;
    private List<String> mPkgList;
    private final Object mPkgLock;
    private boolean mPowerConnStatus;
    private PowerManagerInternal mPowerManagerInternal;
    private int mRecentNum;
    private int mRecentStore;
    private boolean mRecordSwitch;
    private long mScreenOffCheckTime;
    private long mScreenOnCheckTime;
    private boolean mSmartLowPower;
    private long mStartFromNotityTime;
    private boolean mStrictMode;
    private long mStrictModeEnterTime;
    private boolean mStrictModeSwitch;
    private List<String> mStrictWhitePkgList;
    private final Object mStrictWhitePkgListLock;
    private long mUploadTime;
    private WindowManagerService mWindowManager;

    private class BPMHandler extends Handler {
        public BPMHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    OppoProcessManager.this.handleBpmUpdateMsg();
                    return;
                case 102:
                    OppoProcessManager.this.handlePkgUpdateMsg();
                    return;
                case 103:
                    OppoProcessManager.this.handleBrdUpdateMsg();
                    return;
                case 104:
                    OppoProcessManager.this.handleBlackSysAppUpdateMsg();
                    return;
                case 105:
                    OppoProcessManager.this.handleAppWidgetUpdateMsg();
                    return;
                case 106:
                    OppoProcessManager.this.handleBlackAppUpdateMsg();
                    return;
                case 107:
                    OppoProcessManager.this.handlePowerConnStsUpdateMsg();
                    return;
                case 108:
                    OppoProcessManager.this.handleSmartLowPowerUpdateMsg();
                    return;
                case 109:
                    OppoProcessManager.this.handlePowerConnStsUpdateMsg();
                    return;
                case 110:
                    OppoProcessManager.this.handleBpmConfigUpdateMsg();
                    return;
                case 111:
                    OppoProcessManager.this.handleDisplayDeviceUpdateMsg();
                    return;
                case 112:
                    OppoProcessManager.this.handleCprUpdateMsg();
                    return;
                case 120:
                    OppoProcessManager.this.handleScreenOnMsg();
                    return;
                case 121:
                    OppoProcessManager.this.handleScreenOffMsg();
                    return;
                case 130:
                    OppoProcessManager.this.handleUpdateProcStateMsg();
                    return;
                case 140:
                    OppoProcessManager.this.handleRecordResumeReasonMsg(msg);
                    return;
                case OppoProcessManager.MSG_READY_ENTER_STRICTMODE /*150*/:
                    OppoProcessManager.this.handleReadyStrictModeMsg();
                    return;
                case OppoProcessManager.MSG_UPLOAD /*180*/:
                    OppoProcessManager.this.handleDataUploadMsg(msg);
                    return;
                case OppoProcessManager.MSG_REMOVE_RECORD /*181*/:
                    OppoProcessManager.this.handleRemoveRecordInfoMsg(msg);
                    return;
                case OppoProcessManager.MSG_APP_DIED /*182*/:
                    OppoProcessManager.this.handleAppDiedMsg(msg);
                    return;
                case OppoProcessManager.MSG_RESUME_PROCESS /*318*/:
                    OppoProcessManager.this.handleResumeMsg(msg);
                    return;
                case OppoProcessManager.MSG_SUSPEND_PROCESS /*319*/:
                    OppoProcessManager.this.handleSuspendMsg(msg);
                    return;
                case OppoProcessManager.MSG_SUSPEND_PROCESS_DELAY /*320*/:
                    OppoProcessManager.this.handleSuspendDelayMsg(msg);
                    return;
                default:
                    return;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoProcessManager.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoProcessManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoProcessManager.<clinit>():void");
    }

    public OppoProcessManager() {
        this.mUploadTime = 0;
        this.mDynamicDebug = false;
        this.mDebugSwitch = sDebugDetail | this.mDynamicDebug;
        this.mBpmLock = new Object();
        this.mPkgLock = new Object();
        this.mBrdLock = new Object();
        this.mCprLock = new Object();
        this.mBlackSysAppLock = new Object();
        this.mAppWidgetLock = new Object();
        this.mBlackAppLock = new Object();
        this.mBlackAppBrdLock = new Object();
        this.mCustomizeAppLock = new Object();
        this.mDisplayDeviceListLock = new Object();
        this.mStrictWhitePkgListLock = new Object();
        this.mBPMSwitch = true;
        this.mIsInOffHook = false;
        this.mIsScreenOn = true;
        this.mPowerConnStatus = false;
        this.mSmartLowPower = false;
        this.mLowPower = false;
        this.mRecordSwitch = false;
        this.mStrictMode = false;
        this.mModeStatus = 0;
        this.mRecentNum = 0;
        this.mRecentStore = 0;
        this.mScreenOnCheckTime = 60000;
        this.mScreenOffCheckTime = 60000;
        this.mStrictModeSwitch = true;
        this.mStrictModeEnterTime = 60000;
        this.mPayModeEnterTime = 5000;
        this.mPaySafeSwitch = false;
        this.mStartFromNotityTime = 10000;
        this.mBpmList = new ArrayList();
        this.mPkgList = new ArrayList();
        this.mBrdList = new ArrayList();
        this.mCprList = new ArrayList();
        this.mBlackSysAppList = new ArrayList();
        this.mAppWidgetList = new ArrayList();
        this.mBlackAppList = new ArrayList();
        this.mBlackAppBrdList = new ArrayList();
        this.mCustomizeAppList = new ArrayList();
        this.mDisplayDeviceList = new ArrayList();
        this.mStrictWhitePkgList = new ArrayList();
        this.mGlobalWhiteList = new ArrayList();
        this.mWindowManager = null;
        this.mActivityManager = null;
        this.mPowerManagerInternal = null;
        this.mBpmHandler = null;
        this.mDefaultInputMethod = null;
        this.dufaultInputMethodObserver = new ContentObserver(this.mBpmHandler) {
            public void onChange(boolean selfChange) {
                OppoProcessManager.this.mDefaultInputMethod = OppoProcessManager.this.getDefaultInputMethod();
            }
        };
    }

    public static final OppoProcessManager getInstance() {
        if (mOppoProcessManager == null) {
            mOppoProcessManager = new OppoProcessManager();
        }
        return mOppoProcessManager;
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
    }

    private void initData() {
        OppoBPMUtils.getInstance().init(this);
        updateBpmList();
        updatePkgList();
        updateBrdList();
        updateCprList();
        updateBlackSysAppList();
        updateAppWidgetList();
        updateBlackAppList();
        updatePowerConnStatus();
        updateSmartLowPower();
        updateLowPower();
        updateBpmConfig();
        updateCustomizeAppList();
        updateDisplayDeviceList();
    }

    private void suspendProcess(ProcessRecord app) {
        if (app != null && !checkWhiteProcessRecord(app)) {
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", "suspendProcess enter app " + app.processName);
            }
            if (this.mIsInOffHook) {
                if (this.mDynamicDebug) {
                    Slog.i("OppoProcessManager", "return for phone!");
                }
                return;
            }
            try {
                if (app.curReceiver == null || !app.curReceiver.ordered) {
                    if (this.mDynamicDebug) {
                        Slog.i("OppoProcessManager", Log.getStackTraceString(new Throwable()));
                        Slog.i("OppoProcessManager", app.processName + "  curAdj  ==  " + app.curAdj + "  setAdj  ==  " + app.setAdj);
                    }
                    if (app.curAdj > app.setAdj) {
                        this.mBpmHandler.removeMessages(MSG_RESUME_PROCESS, app.processName);
                    } else if (this.mDynamicDebug && this.mBpmHandler.hasMessages(MSG_RESUME_PROCESS, app.processName)) {
                        Slog.i("OppoProcessManager", "hasMessages MSG_RESUME_PROCESS  " + app.processName);
                    }
                    if (!isProcessSuspended(app) && !this.mBpmHandler.hasMessages(MSG_SUSPEND_PROCESS, app.processName)) {
                        sendBpmMessage(app, (int) MSG_SUSPEND_PROCESS, 5000);
                        return;
                    }
                    return;
                }
                if (this.mDynamicDebug) {
                    Slog.i("OppoProcessManager", Log.getStackTraceString(new Throwable()));
                }
                sendBpmMessage(app, (int) MSG_SUSPEND_PROCESS, 5000);
            } catch (NullPointerException e) {
                Slog.w("OppoProcessManager", "suspend Process failed " + e);
            }
        }
    }

    public void resumeProcess(ProcessRecord app, int reason) {
        if (app != null) {
            if (reason == 8 || reason == 13 || reason == 9 || reason == 4 || !checkWhiteProcessRecord(app)) {
                if (this.mDynamicDebug) {
                    Slog.i("OppoProcessManager", "resumeProcess enter app " + app.processName + "  reason is " + reason);
                }
                try {
                    this.mBpmHandler.removeMessages(MSG_SUSPEND_PROCESS, app.processName);
                    if (isProcessSuspended(app) && !this.mBpmHandler.hasMessages(MSG_RESUME_PROCESS, app.processName)) {
                        if (this.mDynamicDebug) {
                            Slog.i("OppoProcessManager", Log.getStackTraceString(new Throwable()));
                        }
                        sendBpmMessage(app, (int) MSG_RESUME_PROCESS, 0, reason);
                    }
                } catch (NullPointerException e) {
                    Slog.w("OppoProcessManager", "resume Process failed " + e);
                }
            }
        }
    }

    public void resumeProcessByUID(int uid, int reason) {
        if (uid >= 10000) {
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", "resumeProcessByUID enter app'uid  " + uid + "  reason is " + reason);
            }
            for (ProcessRecord app : getProcessForUid(uid)) {
                resumeProcess(app, reason);
            }
        }
    }

    private void resumeTopProcess(ProcessRecord app) {
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", "resumeTopProcess enter app  " + app.processName);
        }
        resumeProcessByUID(app.uid, 4);
        if (app.connections.size() > 0) {
            for (ConnectionRecord cr : app.connections) {
                if (this.mDynamicDebug) {
                    Slog.i("OppoProcessManager", "resumeTopProcess connections app  " + cr.binding.service.app);
                }
                resumeProcess(cr.binding.service.app, 4);
            }
        }
        if (app.conProviders.size() > 0) {
            for (ContentProviderConnection cc : app.conProviders) {
                if (this.mDynamicDebug) {
                    Slog.i("OppoProcessManager", "resumeTopProcess ContentProviderConnection app  " + cc.provider.proc);
                }
                resumeProcess(cc.provider.proc, 4);
            }
        }
        if (app.adjSource instanceof ProcessRecord) {
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", "resumeTopProcess adjSource app  " + ((ProcessRecord) app.adjSource));
            }
            resumeProcess((ProcessRecord) app.adjSource, 4);
        }
    }

    public void updateProcessState(ProcessRecord app) {
        boolean isInBlackList = isInBlackList(app);
        if (isInBlackList || !checkWhiteProcessRecord(app)) {
            if (app != null) {
                if (this.mDynamicDebug && app.curAdj == DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE) {
                    Slog.i("OppoProcessManager", "updateProcessState app=" + app + " curAdj=" + app.curAdj);
                }
                if (app.curAdj < DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE) {
                    resumeTopProcess(app);
                    return;
                }
            }
            ActivityRecord r = this.mActivityManager.mStackSupervisor.getTopRunningActivityLocked();
            if (!(r == null || r.app == null)) {
                resumeTopProcess(r.app);
                if (!r.processName.equals(app.processName) && (isEnable() || isInBlackList)) {
                    suspendProcess(app);
                }
            }
        }
    }

    private void updateProcessStateForChanged(boolean enable) {
        if (this.mDebugSwitch) {
            Slog.i("OppoProcessManager", "updateProcessStateForChanged ebable == " + enable);
        }
        synchronized (this.mActivityManager) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                int i;
                ProcessRecord rec;
                if (enable) {
                    for (i = this.mActivityManager.mLruProcesses.size() - 1; i >= 0; i--) {
                        rec = (ProcessRecord) this.mActivityManager.mLruProcesses.get(i);
                        if (!(rec == null || rec.thread == null)) {
                            if (isProcessInWhiteList(rec)) {
                                resumeProcess(rec, 8);
                            } else {
                                suspendProcess(rec);
                            }
                        }
                    }
                } else {
                    for (i = this.mActivityManager.mLruProcesses.size() - 1; i >= 0; i--) {
                        rec = (ProcessRecord) this.mActivityManager.mLruProcesses.get(i);
                        if (!(rec == null || rec.thread == null)) {
                            resumeProcess(rec, 8);
                        }
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void updateProcessStateForWidgetChanged(String pkg) {
        if (pkg != null) {
            synchronized (this.mActivityManager) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    for (int i = this.mActivityManager.mLruProcesses.size() - 1; i >= 0; i--) {
                        ProcessRecord app = (ProcessRecord) this.mActivityManager.mLruProcesses.get(i);
                        if (app != null && isInclude(pkg, app.getPackageList())) {
                            resumeProcessByUID(app.uid, 9);
                            removeRecordAppInfo(pkg);
                            break;
                        }
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            updateWidget(pkg);
        }
    }

    private void updateProcessStateForSuspend() {
        if (isEnable()) {
            if (this.mDebugSwitch) {
                Slog.i("OppoProcessManager", "updateProcessStateForSuspend endter!");
            }
            synchronized (this.mActivityManager) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    for (int i = this.mActivityManager.mLruProcesses.size() - 1; i >= 0; i--) {
                        ProcessRecord proc = (ProcessRecord) this.mActivityManager.mLruProcesses.get(i);
                        if (!(proc == null || proc.thread == null || checkWhiteProcessRecord(proc))) {
                            suspendProcess(proc);
                        }
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    private void updateProcessStateForStopStrictMode() {
        if (this.mDebugSwitch) {
            Slog.i("OppoProcessManager", "updateProcessStateForStopStrictMode");
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
            Slog.i("OppoProcessManager", "checkProcessCanRestart isInBlackList + " + app);
            return false;
        } else if (!isEnable() || checkWhiteProcessRecord(app)) {
            return true;
        } else {
            String wallpaperPkg = OppoBPMHelper.getLivePackageForLiveWallPaper();
            if (wallpaperPkg != null && isInclude(wallpaperPkg, app.getPackageList())) {
                return true;
            }
            try {
                if (isHomeProcess(app)) {
                    return true;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Slog.i("OppoProcessManager", "catch exception: " + e);
            }
            if (isAppMainProcess(app)) {
                cancelNotification(app);
            }
            return false;
        }
    }

    public boolean skipBroadcast(ProcessRecord app, BroadcastRecord r, boolean ordered) {
        if (app == null) {
            return false;
        }
        boolean isInBlackList = isInBlackList(app);
        if (!isEnable() && !isInBlackList) {
            return false;
        }
        if (!isInBlackList) {
            if (isInBrdList(r)) {
                resumeProcessByUID(app.uid, 1);
                return false;
            } else if (checkWhiteProcessRecord(app)) {
                resumeProcessByUID(app.uid, 1);
                return false;
            } else if (r.callingUid >= 10000 && !isInBlackAppBrdList(r) && !isStrictMode()) {
                if (this.mDebugSwitch) {
                    Slog.i("OppoProcessManager", "Not skip callingUid " + r.callingUid + " pid: " + r.callingPid + " action: " + r.intent.getAction());
                }
                resumeProcessByUID(app.uid, 1);
                return false;
            } else if (!ordered) {
                ActivityRecord topAr = this.mActivityManager.mStackSupervisor.getTopRunningActivityLocked();
                if (topAr != null && isInclude(topAr.packageName, app.getPackageList())) {
                    if (this.mDebugSwitch) {
                        Slog.i("OppoProcessManager", "Not skip top app's broadcast uid is  " + app.uid + "  action is " + r.intent.getAction());
                    }
                    return false;
                } else if (r.callingUid == app.uid) {
                    if (this.mDebugSwitch) {
                        Slog.i("OppoProcessManager", "Not skip broadcast because uid is the same  " + app.uid + "  action is " + r.intent.getAction());
                    }
                    if (isStrictMode()) {
                        resumeProcess(app, 1);
                    } else {
                        resumeProcessByUID(app.uid, 1);
                    }
                    return false;
                } else if (isInRecentTask(app)) {
                    if (this.mDynamicDebug) {
                        Slog.i("OppoProcessManager", "Not skip broadcast because isInRecentTask!  " + r);
                    }
                    return false;
                }
            } else if (isProcessSuspended(app)) {
                return true;
            } else {
                if (this.mDebugSwitch) {
                    Slog.i("OppoProcessManager", "Do not skip order broadcast " + r.intent.getAction() + "  " + app);
                }
                return false;
            }
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
                Slog.i("OppoProcessManager", app + "  is in white list");
            }
            return true;
        } else if (isInCustomizeAppList(app) || isInAppWidgetList(app) || isInDisplayDeviceList(app) || isInGlobalWhiteList(app)) {
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", app + "  is in white list for other");
            }
            return true;
        } else if (!isInFromNotityPkgList(app)) {
            return false;
        } else {
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", app + "  is from notify");
            }
            return true;
        }
    }

    private boolean isSystemProcess(ProcessRecord app) {
        if (app == null || app.info == null || (app.info.flags & 1) == 0 || isInBlackSysAppList(app)) {
            return false;
        }
        return true;
    }

    private boolean checkWhitePackage(String pkgName) {
        if (isInBpmWhiteList(pkgName)) {
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", pkgName + "  is in white list");
            }
            return true;
        } else if (isInCustomizeAppList(pkgName) || isInAppWidgetList(pkgName) || isInDisplayDeviceList(pkgName) || isInGlobalWhiteList(pkgName) || OppoListManager.getInstance().isCtaPackage(pkgName)) {
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", pkgName + "  is in white list for other");
            }
            return true;
        } else if (isInFromNotityPkgList(pkgName)) {
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", pkgName + "  is from notify");
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
                Slog.i("OppoProcessManager", pkgName + " does not exits!");
                return true;
            }
        }
    }

    private boolean checkProcessAdj(ProcessRecord app) {
        if (app.setAdj > 100) {
            return false;
        }
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", app.processName + "  's adj is  " + app.setAdj);
        }
        return true;
    }

    private boolean checkSystemAssociatedProcess(ProcessRecord app) {
        boolean result = false;
        if (app == null) {
            return false;
        }
        ProcessRecord systemApp = null;
        synchronized (this.mActivityManager) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                for (int i = this.mActivityManager.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord proc = (ProcessRecord) this.mActivityManager.mLruProcesses.get(i);
                    if (proc != null && proc.thread != null && proc.uid == 1000 && "system".equals(proc.processName)) {
                        systemApp = proc;
                        break;
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        List<ProcessRecord> associatedAppList = getAppAssociatedProcess(systemApp);
        if (associatedAppList == null || associatedAppList.isEmpty()) {
            return false;
        }
        for (ProcessRecord pro : associatedAppList) {
            if (pro != null && pro.processName != null && pro.processName.equals(app.processName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean checkAppAssociatedProcess(ProcessRecord topApp, ProcessRecord app) {
        boolean result = false;
        if (app == null || topApp == null) {
            return false;
        }
        List<ProcessRecord> associatedAppList = getAppAssociatedProcess(topApp);
        if (associatedAppList == null || associatedAppList.isEmpty()) {
            return false;
        }
        for (ProcessRecord pro : associatedAppList) {
            if (pro != null && pro.processName != null && pro.processName.equals(app.processName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private List<ProcessRecord> getAppAssociatedProcess(ProcessRecord app) {
        List<ProcessRecord> associatedAppList = new ArrayList();
        if (app == null) {
            return associatedAppList;
        }
        try {
            ProcessRecord proc;
            if (app.connections.size() > 0) {
                for (ConnectionRecord cr : app.connections) {
                    proc = cr.binding.service.app;
                    if (this.mDynamicDebug) {
                        Slog.i("OppoProcessManager", "app connections proc  " + proc);
                    }
                    if (proc != null) {
                        associatedAppList.add(proc);
                    }
                }
            }
            if (app.conProviders.size() > 0) {
                for (ContentProviderConnection cc : app.conProviders) {
                    proc = cc.provider.proc;
                    if (this.mDynamicDebug) {
                        Slog.i("OppoProcessManager", "app ContentProviderConnection proc  " + proc);
                    }
                    if (proc != null) {
                        associatedAppList.add(proc);
                    }
                }
            }
            if (app.adjSource instanceof ProcessRecord) {
                proc = (ProcessRecord) app.adjSource;
                if (this.mDynamicDebug) {
                    Slog.i("OppoProcessManager", "systemApp adjSource proc  " + proc);
                }
                if (proc != null) {
                    associatedAppList.add(proc);
                }
            }
        } catch (Exception e) {
            Slog.w("OppoProcessManager", "getAppAssociatedProcess    " + e);
        }
        return associatedAppList;
    }

    private int[] getTouchedWindowsPids() {
        if (this.mWindowManager == null) {
            this.mWindowManager = this.mActivityManager.mWindowManager;
        }
        return OppoBPMHelper.getTouchedWindowPids(this.mWindowManager);
    }

    private String[] getActiveAudioPids() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mActivityManager.mContext.getSystemService("audio");
        }
        return getActiveAudioPids(this.mAudioManager.getParameters("get_pid"));
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String[] getActiveAudioPids(String pids) {
        if (pids == null || pids.length() == 0 || !pids.contains(":")) {
            return null;
        }
        return pids.split(":");
    }

    private int[] getWakeLockedPids() {
        if (this.mPowerManagerInternal == null) {
            this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        }
        return this.mPowerManagerInternal.getWakeLockedPids();
    }

    private boolean isLocationApplication(int uid) {
        int[] uids = OppoBPMHelper.getLocationListenersUid();
        if (uids == null) {
            return false;
        }
        for (int i : uids) {
            if (uid == i) {
                return true;
            }
        }
        return false;
    }

    private boolean isInRecentTask(ProcessRecord app) {
        if (!this.mIsScreenOn) {
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", "isInRecentTask is not ScreenOn!");
            }
            return false;
        } else if (isStrictMode()) {
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", "isInRecentTask isStrictMode!");
            }
            return false;
        } else {
            int i;
            List<RecentTaskInfo> recentTasks = this.mActivityManager.getRecentTasks(this.mRecentStore, 15, UserHandle.myUserId()).getList();
            ArrayList<String> recentPkgList = new ArrayList();
            for (i = 0; i < recentTasks.size(); i++) {
                RecentTaskInfo info = (RecentTaskInfo) recentTasks.get(i);
                if (!(info == null || info.baseIntent == null || info.baseIntent.getComponent() == null)) {
                    String pkg = info.baseIntent.getComponent().getPackageName();
                    boolean isExcluded = (info.baseIntent.getFlags() & 8388608) == 8388608;
                    if (!(pkg == null || isExcluded)) {
                        recentPkgList.add(pkg);
                    }
                }
            }
            int n = recentPkgList.size() < this.mRecentNum ? recentPkgList.size() : this.mRecentNum;
            for (i = 0; i < n; i++) {
                if (isInclude((String) recentPkgList.get(i), app.getPackageList())) {
                    return true;
                }
            }
            return false;
        }
    }

    private void cancelNotifications(final ProcessRecord app) {
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", "cancelNotifications  " + app.processName);
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
            Slog.i("OppoProcessManager", "cancelNotification  " + app.processName);
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

    private boolean checkProcessToast(int pid) {
        return OppoBPMHelper.checkProcessToast(pid);
    }

    private boolean isHomeProcess(ProcessRecord app) {
        if (app == null || app.info == null) {
            return false;
        }
        return OppoBPMHelper.isHomeProcess(this.mActivityManager.mContext, app.info.packageName);
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
        if (!(imList == null || imList.isEmpty())) {
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

    public boolean isProcessSuspended(ProcessRecord app) {
        if (app != null) {
            return Process.isProcessSuspend(app.pid);
        }
        return false;
    }

    public boolean isUidGroupHasSuspended(ProcessRecord app) {
        if (app != null) {
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", "isUidGroupHasSuspended app'uid  " + app.uid);
            }
            for (ProcessRecord appProc : getProcessForUid(app.uid)) {
                if (this.mDynamicDebug) {
                    Slog.i("OppoProcessManager", "isUidGroupHasSuspended app is   " + appProc);
                }
                if (appProc != null && Process.isProcessSuspend(appProc.pid)) {
                    Slog.i("OppoProcessManager", "isUidGroupHasSuspended Suspended   " + appProc.processName);
                    return true;
                }
            }
        }
        return false;
    }

    public void setPhoneState(String phoneState) {
        if (this.mDebugSwitch) {
            Slog.i("OppoProcessManager", "setPhoneState phoneState is " + phoneState);
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
                Slog.i("OppoProcessManager", "initStateReceiver SystemReady!");
                OppoPhoneStateReceiver oppoPhoneStateReceiver = new OppoPhoneStateReceiver(OppoProcessManager.this.mActivityManager.mContext);
                OppoScreenStateReceiver oppoScreenStateReceiver = new OppoScreenStateReceiver(OppoProcessManager.this.mActivityManager.mContext);
                OppoBpmReceiver oppoBpmReceiver = new OppoBpmReceiver(OppoProcessManager.this.mActivityManager.mContext);
                OppoProcessManager.this.registerInputMethodObserver();
            }
        }.start();
    }

    private void registerInputMethodObserver() {
        this.mActivityManager.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("default_input_method"), true, this.dufaultInputMethodObserver);
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
                Slog.e("OppoProcessManager", "Failed to get default input method");
            }
        }
        if (this.mDebugSwitch) {
            Slog.i("OppoProcessManager", "defaultInputMethod " + defaultInput);
        }
        return defaultInput;
    }

    public void sendBpmMessage(ProcessRecord app, int what, long delay, int reason) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = app.pid;
        msg.arg2 = reason;
        msg.obj = app.processName;
        this.mBpmHandler.sendMessageDelayed(msg, delay);
    }

    public void sendBpmMessage(int pid, int what, long delay, int reason) {
        if (this.mRecordSwitch) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = pid;
            msg.arg2 = reason;
            this.mBpmHandler.sendMessageDelayed(msg, delay);
        }
    }

    public void sendBpmMessage(ProcessRecord app, int what, long delay) {
        sendBpmMessage(app, what, delay, 0);
    }

    public void sendBpmEmptyMessage(int what, long delay) {
        this.mBpmHandler.removeMessages(what);
        this.mBpmHandler.sendEmptyMessageDelayed(what, delay);
    }

    public void sendBpmMessage(int what, long delay, int reason) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = reason;
        this.mBpmHandler.sendMessageDelayed(msg, delay);
    }

    public void sendBpmMessage(int what, long delay, String packageName) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = packageName;
        this.mBpmHandler.sendMessageDelayed(msg, delay);
    }

    private void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.mDebugSwitch = sDebugDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i("OppoProcessManager", "#####openlog####");
        Slog.i("OppoProcessManager", "mDynamicDebug == " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i("OppoProcessManager", "mDynamicDebug == " + getInstance().mDynamicDebug);
    }

    private void registerLogModule() {
        try {
            Slog.i("OppoProcessManager", "invoke com.android.server.OppoDynamicLogManager");
            Class cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i("OppoProcessManager", "invoke " + cls);
            Class[] clsArr = new Class[1];
            clsArr[0] = String.class;
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", clsArr);
            Slog.i("OppoProcessManager", "invoke " + m);
            Object newInstance = cls.newInstance();
            Object[] objArr = new Object[1];
            objArr[0] = OppoProcessManager.class.getName();
            m.invoke(newInstance, objArr);
            Slog.i("OppoProcessManager", "invoke end!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isEnable() {
        if (this.mPowerConnStatus) {
            if (isNormalStaus()) {
                return false;
            }
            return true;
        } else if (this.mSmartLowPower) {
            return this.mLowPower;
        } else {
            return this.mBPMSwitch;
        }
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
            Slog.w("OppoProcessManager", "isInclude has exception! ", e);
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
            Slog.w("OppoProcessManager", "isInFromNotityPkgList has exception! ", e);
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
                    Log.d("OppoProcessManager", "isInBlackAppBrdList " + r.intent.getAction());
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
                    Log.d("OppoProcessManager", "isInStrictWhitePkgList " + pkg);
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

    public void updateBpmList() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updateBpmList!");
        }
        synchronized (this.mBpmLock) {
            this.mBpmList.clear();
            this.mBpmList.addAll(OppoBPMUtils.getInstance().getBpmList());
        }
    }

    public void updatePkgList() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updatePkgList!");
        }
        synchronized (this.mPkgLock) {
            this.mPkgList.clear();
            this.mPkgList.addAll(OppoBPMUtils.getInstance().getPkgList());
        }
    }

    public void updateBrdList() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updateBrdList!");
        }
        synchronized (this.mBrdLock) {
            this.mBrdList.clear();
            this.mBrdList.addAll(OppoBPMUtils.getInstance().getBrdList());
        }
    }

    public void updateCprList() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updateCprList!");
        }
        synchronized (this.mCprLock) {
            this.mCprList.clear();
            this.mCprList.addAll(OppoBPMUtils.getInstance().getCprList());
        }
    }

    public void updateBlackSysAppList() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updateBlackSysAppList!");
        }
        synchronized (this.mBlackSysAppLock) {
            this.mBlackSysAppList.clear();
            this.mBlackSysAppList.addAll(OppoBPMUtils.getInstance().getBlackSysAppList());
        }
    }

    public void updateAppWidgetList() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updateAppWidgetList!");
        }
        synchronized (this.mAppWidgetLock) {
            this.mAppWidgetList = new ArrayList(OppoBPMUtils.getInstance().getAppWidgetList());
        }
    }

    public void updateBlackAppList() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updateBlackAppList!");
        }
        synchronized (this.mBlackAppLock) {
            this.mBlackAppList.clear();
            this.mBlackAppList.addAll(OppoBPMUtils.getInstance().getBlackAppList());
        }
    }

    public void updateCustomizeAppList() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updateCustomizeAppList!");
        }
        synchronized (this.mCustomizeAppLock) {
            this.mCustomizeAppList.clear();
            this.mCustomizeAppList.addAll(OppoBPMUtils.getInstance().getCustomizeAppList());
        }
    }

    public void updateGlobalWhiteList() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updateGlobalWhiteList!");
        }
        this.mGlobalWhiteList = OppoListManager.getInstance().getGlobalWhiteList(this.mActivityManager.mContext);
    }

    public void updatePowerConnStatus() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updatePowerConnStatus!");
        }
        this.mPowerConnStatus = OppoBPMUtils.getInstance().getPowerConnStatus();
        handleDateUpload(this.mPowerConnStatus);
    }

    public void updateSmartLowPower() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updateSmartLowPower!");
        }
        this.mSmartLowPower = OppoBPMUtils.getInstance().getSmartLowPower();
    }

    public void updateLowPower() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updateLowPower!");
        }
        this.mLowPower = OppoBPMUtils.getInstance().getLowPower();
    }

    public void updateBpmConfig() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updateBpmConfig!");
        }
        this.mRecordSwitch = OppoBPMUtils.getInstance().isForumVersion();
        this.mRecentNum = OppoBPMUtils.getInstance().getRecentTaskNum();
        this.mRecentStore = OppoBPMUtils.getInstance().getRecentTaskStore();
        this.mScreenOnCheckTime = OppoBPMUtils.getInstance().getScreenOnCheckTime();
        this.mScreenOffCheckTime = OppoBPMUtils.getInstance().getScreenOffCheckTime();
        this.mStrictModeSwitch = OppoBPMUtils.getInstance().getStrictModeSwitch();
        this.mStrictModeEnterTime = OppoBPMUtils.getInstance().getStrictModeEnterTime();
        this.mPayModeEnterTime = OppoBPMUtils.getInstance().getPayModeEnterTime();
        this.mStartFromNotityTime = OppoBPMUtils.getInstance().getStartFromNotityTime();
        this.mPaySafeSwitch = OppoBPMUtils.getInstance().getPaySafeSwitch();
        synchronized (this.mBlackAppBrdLock) {
            this.mBlackAppBrdList.clear();
            this.mBlackAppBrdList.addAll(OppoBPMUtils.getInstance().getBlackAppBrdList());
        }
        synchronized (this.mStrictWhitePkgListLock) {
            this.mStrictWhitePkgList.clear();
            this.mStrictWhitePkgList.addAll(OppoBPMUtils.getInstance().getStrictWhitePkgList());
        }
    }

    public void updateDisplayDeviceList() {
        if (this.mDebugSwitch) {
            Log.d("OppoProcessManager", "updateDisplayDeviceList!");
        }
        synchronized (this.mDisplayDeviceListLock) {
            this.mDisplayDeviceList = new ArrayList(OppoBPMUtils.getInstance().getDisplayDeviceList());
        }
    }

    public void handleBpmUpdateMsg() {
        OppoBPMUtils.getInstance().reLoadStatusAndListFile(101);
        List<String> oldBpmList = new ArrayList();
        oldBpmList.addAll(this.mBpmList);
        updateBpmList();
        updateProcessStateForChanged(isEnable());
        synchronized (this.mBpmLock) {
            if (this.mBpmList.size() > oldBpmList.size()) {
                for (int i = oldBpmList.size(); i < this.mBpmList.size(); i++) {
                    removeRecordAppInfo((String) this.mBpmList.get(i));
                }
            }
        }
        oldBpmList.clear();
    }

    public void handlePkgUpdateMsg() {
        OppoBPMUtils.getInstance().reLoadStatusAndListFile(102);
        List<String> oldPkgList = new ArrayList();
        oldPkgList.addAll(this.mPkgList);
        updatePkgList();
        updateProcessStateForChanged(isEnable());
        synchronized (this.mBpmLock) {
            if (this.mPkgList.size() > oldPkgList.size()) {
                for (int i = oldPkgList.size(); i < this.mPkgList.size(); i++) {
                    removeRecordAppInfo((String) this.mPkgList.get(i));
                }
            }
        }
        oldPkgList.clear();
    }

    public void handleBrdUpdateMsg() {
        OppoBPMUtils.getInstance().reLoadStatusAndListFile(103);
        updateBrdList();
    }

    public void handleCprUpdateMsg() {
        OppoBPMUtils.getInstance().reLoadStatusAndListFile(112);
        updateCprList();
    }

    public void handleBlackSysAppUpdateMsg() {
        OppoBPMUtils.getInstance().reLoadStatusAndListFile(104);
        updateBlackSysAppList();
    }

    public void handleAppWidgetUpdateMsg() {
        String widgetPkgName = null;
        List<String> oldAppWidgetList = this.mAppWidgetList;
        OppoBPMUtils.getInstance().reLoadStatusAndListFile(105);
        updateAppWidgetList();
        if (this.mAppWidgetList.size() > oldAppWidgetList.size()) {
            for (String pkg : this.mAppWidgetList) {
                if (!oldAppWidgetList.contains(pkg)) {
                    widgetPkgName = pkg;
                    Slog.i("OppoProcessManager", "add new widget = " + pkg);
                    break;
                }
            }
        }
        if (isEnable()) {
            updateProcessStateForWidgetChanged(widgetPkgName);
        }
        oldAppWidgetList.clear();
    }

    public void handleBlackAppUpdateMsg() {
        OppoBPMUtils.getInstance().reLoadStatusAndListFile(106);
        updateBlackAppList();
    }

    public void handlePowerConnStsUpdateMsg() {
        OppoBPMUtils.getInstance().reLoadStatusAndListFile(107);
        updatePowerConnStatus();
        Slog.i("OppoProcessManager", "handlePowerConnStsUpdateMsg " + this.mPowerConnStatus);
        updateProcessStateForChanged(isEnable());
    }

    public void handleSmartLowPowerUpdateMsg() {
        Slog.i("OppoProcessManager", "MSG_UPDATE_SMART_LOW_POWER");
        boolean tempValue = this.mSmartLowPower;
        OppoBPMUtils.getInstance().reLoadStatusAndListFile(108);
        updateSmartLowPower();
        Slog.i("OppoProcessManager", "mSmartLowPower = " + this.mSmartLowPower);
        if (this.mSmartLowPower != tempValue) {
            Slog.i("OppoProcessManager", "MSG_UPDATE_SMART_LOW_POWER updateProcessStateForChanged");
            updateProcessStateForChanged(isEnable());
        }
    }

    public void handleLowPowerUpdateMsg() {
        Slog.i("OppoProcessManager", "MSG_UPDATE_LOW_POWER");
        OppoBPMUtils.getInstance().reLoadStatusAndListFile(109);
        updateLowPower();
        Slog.i("OppoProcessManager", "mLowPower = " + this.mLowPower);
        if (this.mSmartLowPower) {
            Slog.i("OppoProcessManager", "MSG_UPDATE_LOW_POWER updateProcessStateForChanged");
            updateProcessStateForChanged(isEnable());
        }
    }

    public void handleBpmConfigUpdateMsg() {
        OppoBPMUtils.getInstance().reLoadBpmConfigFile();
        updateBpmConfig();
    }

    public void handleDisplayDeviceUpdateMsg() {
        updateDisplayDeviceList();
    }

    public void handleSuspendMsg(Message msg) {
        int pid = msg.arg1;
        ProcessRecord app = getProcessForPid(Integer.toString(pid));
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", "bpm debug log: syspend msg enter:  " + app);
        }
        if (app != null) {
            boolean isBlackApp = isInBlackList(app);
            if (!isEnable() && !isBlackApp) {
                if (this.mDebugSwitch) {
                    Slog.i("OppoProcessManager", "bpmhandle: return for disable!");
                }
            } else if (this.mIsInOffHook) {
                if (this.mDebugSwitch) {
                    Slog.i("OppoProcessManager", "return for phone!");
                }
            } else {
                ActivityRecord topActivity = this.mActivityManager.mStackSupervisor.getTopRunningActivityLocked();
                if (topActivity != null) {
                    if (app.pkgList.containsKey(topActivity.packageName)) {
                        if (this.mDebugSwitch) {
                            Slog.i("OppoProcessManager", "bpmhandle: return for top activity");
                        }
                        increaseUnSuspendInfo(app, 1);
                        return;
                    }
                    if (checkAppAssociatedProcess(topActivity.app, app)) {
                        if (this.mDebugSwitch) {
                            Slog.i("OppoProcessManager", "bpmhandle: return for topAssociatedProc");
                        }
                        increaseUnSuspendInfo(app, 2);
                        return;
                    }
                }
                int[] wakeLocks;
                int i;
                if (isBlackApp) {
                    wakeLocks = getWakeLockedPids();
                    if (wakeLocks != null) {
                        for (int i2 : wakeLocks) {
                            if (pid == i2) {
                                Slog.i("OppoProcessManager", "bpmhandle: kill proc hold wakeLock which is  " + app.pid + " " + app.processName);
                                increaseUnSuspendInfo(app, 14);
                                Process.sendSignal(app.pid, 9);
                                return;
                            }
                        }
                    }
                } else if (checkProcessAdj(app)) {
                    if (this.mDebugSwitch) {
                        Slog.i("OppoProcessManager", "bpmhandle: return for adj");
                    }
                    increaseUnSuspendInfo(app, 4);
                    return;
                } else if (app != null && app.curReceiver != null && app.curReceiver.ordered) {
                    if (this.mDebugSwitch) {
                        Slog.i("OppoProcessManager", "bpmhandle: try suspend again for order brd");
                    }
                    increaseUnSuspendInfo(app, 5);
                    suspendProcess(app);
                    return;
                } else if (isInputMethodApplication(app)) {
                    if (this.mDebugSwitch) {
                        Slog.i("OppoProcessManager", "bpmhandle: return for im");
                    }
                    increaseUnSuspendInfo(app, 6);
                    return;
                } else if (isInRecentTask(app)) {
                    if (this.mDebugSwitch) {
                        Slog.i("OppoProcessManager", "bpmhandle: return for recent task");
                    }
                    increaseUnSuspendInfo(app, 12);
                    sendBpmMessage(app, MSG_SUSPEND_PROCESS_DELAY, 180000);
                    return;
                } else {
                    String[] mTrackPids = getActiveAudioPids();
                    if (mTrackPids != null) {
                        for (i = 0; i < mTrackPids.length; i++) {
                            if (!mTrackPids[i].isEmpty()) {
                                if (this.mDynamicDebug) {
                                    Slog.i("OppoProcessManager", "mTrackPids[i] = " + mTrackPids[i]);
                                }
                                if (mTrackPids[i].equals(Integer.toString(pid))) {
                                    if (this.mDebugSwitch) {
                                        Slog.i("OppoProcessManager", "bpmhandle: return for audio track 1");
                                    }
                                    increaseUnSuspendInfo(app, 7);
                                    return;
                                }
                                ProcessRecord trackApp = getProcessForPid(mTrackPids[i]);
                                if (trackApp != null && trackApp.uid == app.uid) {
                                    if (this.mDebugSwitch) {
                                        Slog.i("OppoProcessManager", "bpmhandle: return for audio track 2");
                                    }
                                    increaseUnSuspendInfo(app, 7);
                                    return;
                                } else if (checkAppAssociatedProcess(app, trackApp)) {
                                    if (this.mDebugSwitch) {
                                        Slog.i("OppoProcessManager", "bpmhandle: return for audioAssociatedProc");
                                    }
                                    increaseUnSuspendInfo(app, 15);
                                    return;
                                }
                            }
                        }
                    }
                    int[] mTouchWindowPids = getTouchedWindowsPids();
                    if (mTouchWindowPids != null) {
                        List<ProcessRecord> apps = getProcessForUid(app.uid);
                        i = 0;
                        while (i < mTouchWindowPids.length) {
                            for (ProcessRecord procApp : apps) {
                                if (procApp != null && procApp.pid == mTouchWindowPids[i]) {
                                    if (this.mDebugSwitch) {
                                        Slog.i("OppoProcessManager", "bpmhandle: return for touch window");
                                    }
                                    increaseUnSuspendInfo(app, 8);
                                    return;
                                }
                            }
                            i++;
                        }
                    }
                    wakeLocks = getWakeLockedPids();
                    if (wakeLocks != null) {
                        for (int i22 : wakeLocks) {
                            if (pid == i22) {
                                if (this.mDebugSwitch) {
                                    Slog.i("OppoProcessManager", "bpmhandle: return for wakelock");
                                }
                                increaseUnSuspendInfo(app, 9);
                                return;
                            }
                        }
                    }
                    if (checkProcessToast(pid)) {
                        if (this.mDebugSwitch) {
                            Slog.i("OppoProcessManager", "bpmhandle: return for toast");
                        }
                        increaseUnSuspendInfo(app, 10);
                        return;
                    } else if (isHomeProcess(app)) {
                        if (this.mDebugSwitch) {
                            Slog.i("OppoProcessManager", "bpmhandle: return for home app");
                        }
                        increaseUnSuspendInfo(app, 11);
                        return;
                    }
                }
                if (isInDisplayDeviceList(app)) {
                    if (this.mDebugSwitch) {
                        Slog.i("OppoProcessManager", "bpmhandle: return for display device app");
                    }
                    increaseUnSuspendInfo(app, 16);
                    return;
                }
                if (isLocationApplication(app.uid)) {
                    Slog.i("OppoProcessManager", "bpmhandle: kill proc hold gps which is  " + app.pid + " " + app.processName);
                    increaseUnSuspendInfo(app, 13);
                    Process.sendSignal(app.pid, 9);
                    return;
                }
                Process.sendSignal(pid, 19);
                increaseSuspendInfo(app);
                Slog.i("OppoProcessManager", app + " suspend!!!");
            }
        }
    }

    public void handleSuspendDelayMsg(Message msg) {
        suspendProcess(getProcessForPid(Integer.toString(msg.arg1)));
    }

    public void handleResumeMsg(Message msg) {
        int pid = msg.arg1;
        int reason = msg.arg2;
        String processName = msg.obj;
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", "bpm debug log: resume msg enter:  " + processName);
        }
        String reasonStr = codeToReasonStr(reason);
        if (this.mDebugSwitch) {
            Slog.i("OppoProcessManager", "bpmhandle: resume " + processName + " reason is " + reasonStr);
        }
        Process.sendSignal(pid, 18);
        increaseResumeInfo(processName, reason);
        increaseResumeTime(processName, false);
        Slog.i("OppoProcessManager", processName + " resume!!!");
    }

    public void handleScreenOnMsg() {
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", "handleScreenOnMsg!");
        }
        this.mIsScreenOn = true;
        if (this.mBpmHandler.hasMessages(130)) {
            this.mBpmHandler.removeMessages(130);
        }
    }

    public void handleScreenOffMsg() {
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", "handleScreenOffMsg!");
        }
        this.mIsScreenOn = false;
        if (this.mBpmHandler.hasMessages(130)) {
            this.mBpmHandler.removeMessages(130);
        }
        sendBpmEmptyMessage(130, this.mScreenOffCheckTime);
        if (isStrictMode()) {
            stopStrictMode();
        } else {
            this.mBpmHandler.removeMessages(MSG_READY_ENTER_STRICTMODE);
        }
        setBpmStatus(0);
    }

    public void handleUpdateProcStateMsg() {
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", "handleUpdateProcStateMsg!");
        }
        updateProcessStateForSuspend();
    }

    public void handleRecordResumeReasonMsg(Message msg) {
        if (this.mRecordSwitch) {
            int pid = msg.arg1;
            int reason = msg.arg2;
            ProcessRecord app = getProcessForPid(Integer.toString(pid));
            if (app != null && !checkWhiteProcessRecord(app)) {
                if (this.mDebugSwitch) {
                    Slog.i("OppoProcessManager", "bpmhandle: resume " + app.processName + " reason is " + codeToReasonStr(reason));
                }
                increaseResumeInfo(app.processName, reason);
                increaseResumeTime(app.processName, true);
            }
        }
    }

    public void sendScreenOnUpdateProcStateMsg() {
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", "SendBpmUpdateProcStateMsg!");
        }
        if (this.mBpmHandler.hasMessages(130)) {
            this.mBpmHandler.removeMessages(130);
        }
        sendBpmEmptyMessage(130, this.mScreenOnCheckTime);
    }

    public void sendReadyStrictModeMsg() {
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", "sendReadyStrictModeMsg!");
        }
        if (isStrictMode()) {
            Slog.i("OppoProcessManager", "sendReadyStrictModeMsg already in strictmode");
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

    public void handleApplicationSwitch(String prePkgName, String nextPkgName) {
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", "handleApplicationSwitch mStrictModeSwitch = " + this.mStrictModeSwitch + " prePkgName " + prePkgName + "  nextPkgName " + nextPkgName);
        }
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
        sendScreenOnUpdateProcStateMsg();
    }

    public void handleReadyStrictModeMsg() {
        if (this.mDebugSwitch) {
            Slog.i("OppoProcessManager", "handleReadyStrictModeMsg!");
        }
        enterStrictMode();
    }

    public boolean isStrictMode() {
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", "isStrictMode " + this.mStrictMode);
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
            Slog.i("OppoProcessManager", "notifyStopStrictMode!");
        }
        OppoBPMHelper.notifyStopStrictMode();
    }

    public void enterStrictMode() {
        if (this.mStrictModeSwitch) {
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", "enterStrictMode!");
            }
            setStrictMode(true);
            updateProcessStateForSuspend();
        }
    }

    public void stopStrictMode() {
        if (this.mDynamicDebug) {
            Slog.i("OppoProcessManager", "stopStrictMode!");
        }
        setStrictMode(false);
        notifyStopStrictMode();
        this.mBpmHandler.removeMessages(MSG_READY_ENTER_STRICTMODE);
        updateProcessStateForStopStrictMode();
    }

    public boolean isDelayAppAlarm(int callerPid, int callerUid, int calledUid, String calledPkg) {
        boolean isDelay = false;
        if (!isStrictMode() && !isEnable()) {
            return false;
        }
        if (calledUid < 10000) {
            if (this.mDebugSwitch) {
                Slog.i("OppoProcessManager", "isDelayAppAlarm return for uid < 10000!");
            }
            return false;
        }
        if (callerUid == calledUid) {
            if (Process.isProcessSuspend(callerPid)) {
                isDelay = true;
            }
        } else if (!checkWhitePackage(calledPkg)) {
            ActivityRecord topAr = this.mActivityManager.mStackSupervisor.getTopRunningActivityLocked();
            if (!(topAr == null || calledPkg == null || topAr.packageName == null || topAr.packageName.equals(calledPkg))) {
                isDelay = true;
            }
        }
        if (this.mDebugSwitch) {
            Slog.i("OppoProcessManager", "isDelayAppAlarm return " + isDelay);
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
                Slog.i("OppoProcessManager", "isDelayAppSync return for uid < 10000!");
            }
            return false;
        }
        if (!checkWhitePackage(pkg)) {
            isDelay = true;
        }
        if (this.mDebugSwitch) {
            Slog.i("OppoProcessManager", "isDelayAppSync return " + isDelay);
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
                Slog.i("OppoProcessManager", "isDelayAppJob return for uid < 10000!");
            }
            return false;
        }
        if (!checkWhitePackage(pkg)) {
            ActivityRecord topAr = this.mActivityManager.mStackSupervisor.getTopRunningActivityLocked();
            if (!(topAr == null || pkg == null || topAr.packageName == null || topAr.packageName.equals(pkg))) {
                isDelay = true;
            }
        }
        if (this.mDebugSwitch) {
            Slog.i("OppoProcessManager", "isDelayAppJob return " + isDelay);
        }
        return isDelay;
    }

    public String codeToReasonStr(int code) {
        switch (code) {
            case 1:
                return RESUME_REASON_BROADCAST_STR;
            case 2:
                return RESUME_REASON_SERVICE_STR;
            case 3:
                return RESUME_REASON_PROVIDER_STR;
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
            default:
                return "unknown";
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

    private void increaseUnSuspendInfo(ProcessRecord app, int reason) {
        if (this.mRecordSwitch && app != null && app.processName != null) {
            OppoBpmMonitorManager.getInstance().increaseUnSuspendInfo(app.processName, Integer.valueOf(reason));
            OppoBpmMonitorManager.getInstance().increaseUnSuspendTime(app.processName);
        }
    }

    private void increaseSuspendInfo(ProcessRecord app) {
        if (this.mRecordSwitch && app != null && app.info != null) {
            OppoBpmMonitorManager.getInstance().increaseSuspendCount(app.processName);
            OppoBpmMonitorManager.getInstance().increaseSuspendTime(app.processName);
        }
    }

    private void increaseResumeInfo(ProcessRecord app, int reason) {
        if (this.mRecordSwitch && app != null && app.info != null) {
            increaseResumeInfo(app.processName, reason);
        }
    }

    protected void increaseResumeInfo(String processName, int reason) {
        if (this.mRecordSwitch && processName != null) {
            OppoBpmMonitorManager.getInstance().increaseResumeInfo(processName, Integer.valueOf(reason));
        }
    }

    protected void increaseResumeTime(String processName, boolean isDelay) {
        if (this.mRecordSwitch && processName != null) {
            OppoBpmMonitorManager.getInstance().increaseResumeTime(processName, isDelay);
        }
    }

    public void handleUploadInfo() {
        if (this.mRecordSwitch) {
            this.mBpmHandler.removeMessages(MSG_UPLOAD);
            sendBpmMessage((int) MSG_UPLOAD, 0, 1);
        }
    }

    private void handleDateUpload(boolean isPowerConn) {
        if (this.mRecordSwitch && isPowerConn) {
            this.mBpmHandler.removeMessages(MSG_UPLOAD);
            sendBpmMessage((int) MSG_UPLOAD, 0, 2);
        }
    }

    private void handleDataUploadMsg(Message msg) {
        int reason = msg.arg1;
        long currentTime = OppoBpmMonitorManager.getInstance().getCurrentTime();
        if (currentTime - this.mUploadTime >= UPLOAD_INTERVAL_TIME || reason != 2) {
            this.mUploadTime = currentTime;
            if (this.mDynamicDebug) {
                Slog.i("OppoProcessManager", "handleDataUpload state " + reason + " uploadTime " + this.mUploadTime);
            }
            if (reason == 1) {
                OppoBpmMonitorManager.getInstance().handleUploadInfo(this.mActivityManager, false);
            } else if (reason == 2) {
                OppoBpmMonitorManager.getInstance().handleUploadInfo(this.mActivityManager, true);
            }
            return;
        }
        OppoBpmMonitorManager.getInstance().resetUploadInfo();
    }

    /* JADX WARNING: Missing block: B:9:0x0010, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleAppDied(ProcessRecord app) {
        if (this.mRecordSwitch && !this.mPowerConnStatus && app != null && app.processName != null && app.info != null && !isSystemProcess(app) && (app.info.packageName == null || !isInPkgList(app.info.packageName))) {
            sendBpmMessage((int) MSG_APP_DIED, 0, app.processName);
        }
    }

    private void handleAppDiedMsg(Message msg) {
        String processName = msg.obj;
        if (processName != null) {
            OppoBpmMonitorManager.getInstance().handleAppDied(processName);
        }
    }

    private void removeRecordAppInfo(String packageName) {
        if (this.mRecordSwitch && !this.mPowerConnStatus) {
            sendBpmMessage((int) MSG_REMOVE_RECORD, 0, packageName);
        }
    }

    private void handleRemoveRecordInfoMsg(Message msg) {
        String packageName = msg.obj;
        if (packageName != null) {
            OppoBpmMonitorManager.getInstance().removeRecordInfo(packageName);
        }
    }
}

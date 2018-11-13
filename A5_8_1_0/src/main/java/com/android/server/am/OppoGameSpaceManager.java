package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.database.ContentObserver;
import android.net.INetworkPolicyManager;
import android.net.INetworkPolicyManager.Stub;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.Slog;
import com.android.server.face.FaceDaemonWrapper;
import com.oppo.app.IOppoGameSpaceController;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class OppoGameSpaceManager {
    private static final String ACTION_AUDIO_RECORD_START = "android.media.ACTION_AUDIO_RECORD_START";
    private static final String ACTION_AUDIO_RECORD_STOP = "android.media.ACTION_AUDIO_RECORD_STOP";
    private static final String ACTION_GAME_ENTER = "oppo.intent.action.GAMESPACE_ENTER";
    private static final String ACTION_GAME_STOP = "oppo.intent.action.GAMESPACE_STOP";
    private static final String EXTRA_RECORD_ACTION_PID = "android.media.EXTRA_RECORD_ACTION_PID";
    private static final long MSG_DELAY_TIME = 200;
    public static final int MSG_DEVICE_UPDATE = 130;
    public static final int MSG_READY_ENTER_GAMEMODE = 100;
    public static final int MSG_SCREEN_OFF = 121;
    public static final int MSG_SCREEN_ON = 120;
    public static final int MSG_SEND_GAME_ENTER = 140;
    public static final int MSG_SEND_GAME_STOP = 141;
    public static final int MSG_STOP_GAMEMODE = 101;
    public static final int STATUS_CHANGE_TO_GAME = 1;
    public static final int STATUS_CHANGE_TO_NORMAL = 2;
    public static final int STATUS_NORMAL = 0;
    public static final String TAG = "OppoGameSpaceManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static OppoGameSpaceManager sOppoGameSpaceManager = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private ActivityManagerService mAms = null;
    private ContentObserver mDufaultInputMethodObserver = new ContentObserver(this.mGsHandler) {
        public void onChange(boolean selfChange) {
            OppoGameSpaceManager.this.handleDefatultInputMethodAppId();
        }
    };
    boolean mDynamicDebug = false;
    private boolean mGameMode = false;
    private long mGameModeEnterTime = 0;
    private IOppoGameSpaceController mGameSpaceController = null;
    private GameSpaceHandler mGsHandler = null;
    private int mModeStatus = 0;
    private INetworkPolicyManager mNetworkPolicyManager = null;
    private final BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int uid = intent.getIntExtra("android.intent.extra.UID", -1);
            if (uid != -1) {
                if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    if (OppoGameSpaceManager.this.DEBUG_SWITCH) {
                        Slog.v(OppoGameSpaceManager.TAG, "package change for uid = " + uid);
                    }
                    OppoGameSpaceManagerUtils.getInstance().updateNetWhiteAppIdList();
                }
            }
        }
    };
    private final BroadcastReceiver mRadioRecordReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int pid;
            if (action != null && OppoGameSpaceManager.ACTION_AUDIO_RECORD_START.equals(action)) {
                pid = intent.getIntExtra(OppoGameSpaceManager.EXTRA_RECORD_ACTION_PID, 0);
                if (OppoGameSpaceManager.this.DEBUG_SWITCH) {
                    Slog.v(OppoGameSpaceManager.TAG, "receive ACTION_AUDIO_RECORD_START pid = " + pid);
                }
                if (pid != 0) {
                    OppoGameSpaceManagerUtils.getInstance().addRadioRecordingList(pid);
                }
            } else if (action != null && OppoGameSpaceManager.ACTION_AUDIO_RECORD_STOP.equals(action)) {
                pid = intent.getIntExtra(OppoGameSpaceManager.EXTRA_RECORD_ACTION_PID, 0);
                if (OppoGameSpaceManager.this.DEBUG_SWITCH) {
                    Slog.v(OppoGameSpaceManager.TAG, "receive ACTION_AUDIO_RECORD_STOP pid = " + pid);
                }
                if (pid != 0) {
                    OppoGameSpaceManagerUtils.getInstance().removeRadioRecordingList(pid);
                }
            }
        }
    };
    private long mSendBroadcastDelayTime = FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK;

    private class GameSpaceHandler extends Handler {
        public GameSpaceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    if (msg.obj != null) {
                        OppoGameSpaceManager.this.handleReadyGameModeMsg(msg.obj);
                        return;
                    }
                    return;
                case 101:
                    OppoGameSpaceManager.this.handleStopGameModeMsg();
                    return;
                case 121:
                    OppoGameSpaceManager.this.handleScreenOffMsg();
                    return;
                case 130:
                    OppoGameSpaceManager.this.handleDeviceUpdateMsg();
                    return;
                case 140:
                    OppoGameSpaceManager.this.handleGameSpaceEnterBroadcast();
                    return;
                case OppoGameSpaceManager.MSG_SEND_GAME_STOP /*141*/:
                    OppoGameSpaceManager.this.handleGameSpaceStopBroadcast();
                    return;
                default:
                    return;
            }
        }
    }

    public static OppoGameSpaceManager getInstance() {
        if (sOppoGameSpaceManager == null) {
            sOppoGameSpaceManager = new OppoGameSpaceManager();
        }
        return sOppoGameSpaceManager;
    }

    public void init(ActivityManagerService ams) {
        this.mAms = ams;
        OppoGameSpaceManagerUtils.getInstance().init();
        HandlerThread thread = new HandlerThread("gameSpaceThread");
        thread.start();
        this.mGsHandler = new GameSpaceHandler(thread.getLooper());
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        this.mAms.mContext.registerReceiver(this.mPackageReceiver, packageFilter, null, this.mGsHandler);
        registerLogModule();
        handleDefatultInputMethodAppId();
        this.mAms.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("default_input_method"), true, this.mDufaultInputMethodObserver);
        IntentFilter radioRecordFilter = new IntentFilter();
        radioRecordFilter.addAction(ACTION_AUDIO_RECORD_START);
        radioRecordFilter.addAction(ACTION_AUDIO_RECORD_STOP);
        this.mAms.mContext.registerReceiver(this.mRadioRecordReceiver, radioRecordFilter, null, this.mGsHandler);
    }

    public void setGameSpaceController(IOppoGameSpaceController controller) {
        this.mGameSpaceController = controller;
    }

    public void handleApplicationSwitch(String prePkgName, String nextPkgName, String prevActivity, String nextActivity, boolean isPreMultiApp, boolean isNextMultiApp, boolean isPreForFreeForm, boolean isNextForFreeForm) {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "handleApplicationSwitch   prePkgName " + prePkgName + "  nextPkgName " + nextPkgName);
        }
        if (!OppoGameSpaceManagerUtils.getInstance().isGameSpaceSwtichEnable()) {
            if (isGameSpaceMode()) {
                restoreNormalModeDefault();
                notityApkRestoreNormalMode("swithc disable");
            }
            if (this.mDynamicDebug) {
                Slog.i(TAG, "handleApplicationSwitch return");
            }
        } else if ((inGameSpacePkgList(prePkgName) && isSpecialActivity(nextActivity)) || (inGameSpacePkgList(nextPkgName) && isSpecialActivity(prevActivity))) {
            if (this.mDynamicDebug) {
                Slog.i(TAG, "filter the permission or vpn activity change");
            }
        } else if ((inGameSpacePkgList(prePkgName) && isSecureActivity(nextActivity) && isNextForFreeForm) || (inGameSpacePkgList(nextPkgName) && isSecureActivity(prevActivity) && isPreForFreeForm)) {
            if (this.mDynamicDebug) {
                Slog.i(TAG, "filter the appunlock activity change");
            }
        } else {
            if (inGameSpacePkgList(nextPkgName) && (isNextMultiApp ^ 1) != 0) {
                this.mModeStatus = 1;
            } else if (((isPreMultiApp || !inGameSpacePkgList(prePkgName)) && !prePkgName.isEmpty()) || (inGameSpacePkgList(nextPkgName) && !isNextMultiApp)) {
                this.mModeStatus = 0;
            } else if (this.mModeStatus == 1) {
                this.mModeStatus = 2;
            } else {
                this.mModeStatus = 0;
            }
            if (this.mDynamicDebug) {
                Slog.i(TAG, "handleApplicationSwitch mModeStatus = " + this.mModeStatus);
            }
            if (!(this.mModeStatus == 0 || this.mGameSpaceController == null)) {
                try {
                    if (this.mModeStatus == 1) {
                        boolean isAppExist = checkAppProcessExist(nextPkgName);
                        if (this.DEBUG_SWITCH) {
                            Slog.i(TAG, "gameStarting begin isAppExist = " + isAppExist);
                        }
                        setGameMode(true);
                        OppoGameSpaceManagerUtils.getInstance().handleDozeRuleWhite(true, nextPkgName);
                        sendReadyGameModeMsg(nextPkgName);
                        this.mGameSpaceController.gameStarting(null, nextPkgName, isAppExist);
                        return;
                    } else if (this.mModeStatus == 2) {
                        if (this.DEBUG_SWITCH) {
                            Slog.i(TAG, "gameExiting begin ");
                        }
                        setGameMode(false);
                        sendStopGameModeMsg();
                        this.mGameSpaceController.gameExiting(nextPkgName);
                        return;
                    }
                } catch (RemoteException e) {
                    restoreNormalModeDefault();
                    this.mGameSpaceController = null;
                    Slog.w(TAG, e);
                }
            }
            if (isGameSpaceMode()) {
                restoreNormalModeDefault();
                notityApkRestoreNormalMode("ensure other path");
            }
        }
    }

    public boolean isGameSpaceMode() {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isGameSpaceMode mMode = " + this.mGameMode);
        }
        return this.mGameMode;
    }

    private void setGameMode(boolean mode) {
        this.mGameMode = mode;
    }

    private void sendReadyGameModeMsg(String gamePkg) {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "sendReadyGameModeMsg!");
        }
        if (this.mGsHandler.hasMessages(100)) {
            this.mGsHandler.removeMessages(100);
        }
        sendGameSpaceMessage(100, this.mGameModeEnterTime, gamePkg);
    }

    private void sendStopGameModeMsg() {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "sendStopGameModeMsg!");
        }
        if (this.mGsHandler.hasMessages(101)) {
            this.mGsHandler.removeMessages(101);
        }
        sendGameSpaceEmptyMessage(101, 0);
    }

    public void sendGameSpaceEmptyMessage(int what, long delay) {
        this.mGsHandler.sendEmptyMessageDelayed(what, delay);
    }

    public void sendGameSpaceMessage(int what, long delay, String gamePkg) {
        Message msg = this.mGsHandler.obtainMessage(what);
        msg.obj = gamePkg;
        this.mGsHandler.sendMessageDelayed(msg, delay);
    }

    public void handleReadyGameModeMsg(String gamePkg) {
        if (this.DEBUG_SWITCH) {
            Slog.i(TAG, "handleReadyGameModeMsg!");
        }
        if (isGameSpaceMode()) {
            OppoGameSpaceManagerUtils.getInstance().handleDozeRuleWhite(true);
            if (OppoGameSpaceManagerUtils.getInstance().isNetProtectEnable(gamePkg)) {
                setGameModeNetworkPoicy(true);
            }
            if (OppoGameSpaceManagerUtils.getInstance().isPerformanceEnable()) {
                setElsaManagerPolicy(true);
            }
            OppoGameSpaceManagerUtils.getInstance().setGameSpaceModeProp(true);
        }
        if (this.mGsHandler.hasMessages(140)) {
            this.mGsHandler.removeMessages(140);
        }
        sendGameSpaceEmptyMessage(140, this.mSendBroadcastDelayTime);
    }

    public void handleStopGameModeMsg() {
        if (this.DEBUG_SWITCH) {
            Slog.i(TAG, "handleStopGameModeMsg!");
        }
        OppoGameSpaceManagerUtils.getInstance().handleDozeRuleWhite(false);
        if (OppoGameSpaceManagerUtils.getInstance().isNetProtectEnable("")) {
            setGameModeNetworkPoicy(false);
        }
        if (OppoGameSpaceManagerUtils.getInstance().isPerformanceEnable()) {
            setElsaManagerPolicy(false);
        }
        OppoGameSpaceManagerUtils.getInstance().setGameSpaceModeProp(false);
        if (this.mGsHandler.hasMessages(MSG_SEND_GAME_STOP)) {
            this.mGsHandler.removeMessages(MSG_SEND_GAME_STOP);
        }
        sendGameSpaceEmptyMessage(MSG_SEND_GAME_STOP, this.mSendBroadcastDelayTime);
    }

    public void handleScreenOffMsg() {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "handleScreenOffMsg!");
        }
        if (isGameSpaceMode()) {
            restoreNormalModeDefault();
            notityApkRestoreNormalMode("ScreenOff");
            return;
        }
        this.mGsHandler.removeMessages(100);
    }

    public void handleDeviceUpdateMsg() {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "handleDeviceUpdateMsg!");
        }
        OppoGameSpaceManagerUtils.getInstance().updateNetWhiteAppIdList();
    }

    private void restoreNormalModeDefault() {
        setGameMode(false);
        sendStopGameModeMsg();
    }

    private void notityApkRestoreNormalMode(String reason) {
        if (this.mGameSpaceController != null) {
            try {
                if (this.DEBUG_SWITCH) {
                    Slog.d(TAG, "gameExiting for " + reason);
                }
                this.mGameSpaceController.gameExiting("");
            } catch (RemoteException e) {
                this.mGameSpaceController = null;
                Slog.w(TAG, e);
            }
        }
    }

    private void setGameModeNetworkPoicy(boolean isGameMode) {
        if (this.mNetworkPolicyManager == null) {
            this.mNetworkPolicyManager = Stub.asInterface(ServiceManager.getService("netpolicy"));
        }
        try {
            if (this.mNetworkPolicyManager.getGameSpaceMode() != isGameMode) {
                if (this.DEBUG_SWITCH) {
                    Slog.i(TAG, "setGameModeNetworkPoicy " + isGameMode);
                }
                this.mNetworkPolicyManager.setGameSpaceMode(isGameMode);
            }
        } catch (RemoteException e) {
        }
    }

    private void setElsaManagerPolicy(boolean isGameMode) {
        if (OppoGameSpaceManagerUtils.getInstance().isElsaSwitchEnable()) {
            if (this.mDynamicDebug) {
                Slog.i(TAG, "setElsaManagerPolicy " + isGameMode);
            }
            return;
        }
        Slog.i(TAG, "setElsaManagerPolicy return!");
    }

    private boolean checkAppProcessExist(String pkg) {
        boolean result = false;
        if (this.mAms == null || pkg == null || pkg.isEmpty()) {
            return false;
        }
        synchronized (this.mAms) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                for (int i = this.mAms.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord proc = (ProcessRecord) this.mAms.mLruProcesses.get(i);
                    if (proc != null && proc.thread != null && pkg.equals(proc.processName)) {
                        result = true;
                        break;
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return result;
    }

    private boolean isVideoActivity(String cpn) {
        return OppoGameSpaceManagerUtils.getInstance().isVideoCpn(cpn);
    }

    private boolean isSecureActivity(String cpn) {
        return OppoGameSpaceManagerUtils.getInstance().isSecureCpn(cpn);
    }

    private boolean isSpecialActivity(String cpn) {
        return OppoGameSpaceManagerUtils.getInstance().isSpecialCpn(cpn);
    }

    public boolean inGameSpacePkgList(String pkg) {
        return OppoGameSpaceManagerUtils.getInstance().inGameSpacePkgList(pkg);
    }

    public boolean inNetWhitePkgList(String pkg) {
        return OppoGameSpaceManagerUtils.getInstance().inNetWhitePkgList(pkg);
    }

    public boolean inNetWhiteAppIdList(int appId) {
        return OppoGameSpaceManagerUtils.getInstance().inNetWhiteAppIdList(appId);
    }

    public List<Integer> getNetWhiteAppIdlist() {
        return OppoGameSpaceManagerUtils.getInstance().getNetWhiteAppIdlist();
    }

    public boolean isBpmEnable() {
        return OppoGameSpaceManagerUtils.getInstance().isBpmEnable();
    }

    public boolean isVideoInterceptEnable() {
        return OppoGameSpaceManagerUtils.getInstance().isVideoInterceptEnable();
    }

    public int getDefaultInputMethodAppId() {
        return OppoGameSpaceManagerUtils.getInstance().getDefatultInputMethodAppId();
    }

    public boolean isDefaultInputMethodAppId(int appId) {
        return OppoGameSpaceManagerUtils.getInstance().getDefatultInputMethodAppId() == appId;
    }

    public void sendDeviceUpdateMessage() {
        if (this.mGsHandler.hasMessages(130)) {
            this.mGsHandler.removeMessages(130);
        }
        sendGameSpaceEmptyMessage(130, MSG_DELAY_TIME);
    }

    public void handleGameSpaceEnterBroadcast() {
        if (this.mDynamicDebug) {
            Slog.v(TAG, "sendGameSpaceEnterBroadcast!");
        }
        Intent intent = new Intent(ACTION_GAME_ENTER);
        if (this.mAms != null) {
            this.mAms.mContext.sendBroadcast(intent);
        }
    }

    public void handleGameSpaceStopBroadcast() {
        if (this.mDynamicDebug) {
            Slog.v(TAG, "sendGameSpaceStopBroadcast!");
        }
        Intent intent = new Intent(ACTION_GAME_STOP);
        if (this.mAms != null) {
            this.mAms.mContext.sendBroadcast(intent);
        }
    }

    public boolean handleVideoComingNotification(Intent intent, ActivityInfo aInfo) {
        boolean result = false;
        if (isVideoInterceptEnable()) {
            if (this.mDynamicDebug) {
                Slog.v(TAG, "handleVideoComingNotification intent = " + intent);
                Slog.v(TAG, "handleVideoComingNotification aInfo = " + aInfo);
            }
            if (!(aInfo == null || aInfo.name == null || !isVideoActivity(aInfo.name))) {
                if (intent != null && intent.getIsFromGameSpace() == 1) {
                    Slog.v(TAG, "IsFromGameSpace retrun!");
                    return false;
                } else if (this.mGameSpaceController != null && isGameSpaceMode()) {
                    if (isVideoShowInFreeform(aInfo)) {
                        Slog.v(TAG, "isVideoShowInFreeform retrun!");
                        return false;
                    }
                    try {
                        Slog.v(TAG, "videoStarting");
                        this.mGameSpaceController.videoStarting(intent, aInfo.packageName);
                        result = true;
                    } catch (RemoteException e) {
                        Slog.w(TAG, e);
                    }
                }
            }
            if (this.mDynamicDebug) {
                Slog.v(TAG, "handleVideoComingNotification result = " + result);
            }
            return result;
        }
        if (this.mDynamicDebug) {
            Slog.v(TAG, "handleVideoComingNotification return for switch.");
        }
        return false;
    }

    private boolean isVideoShowInFreeform(ActivityInfo aInfo) {
        if (this.mAms == null) {
            return false;
        }
        ApplicationInfo appInfo = this.mAms.getFreeFormAppInfo();
        if (appInfo == null || aInfo.applicationInfo == null || appInfo.uid != aInfo.applicationInfo.uid) {
            return false;
        }
        return true;
    }

    private void handleDefatultInputMethodAppId() {
        String defaultInput = null;
        if (this.mAms != null) {
            try {
                String inputMethod = Secure.getString(this.mAms.mContext.getContentResolver(), "default_input_method");
                if (inputMethod != null) {
                    defaultInput = inputMethod.substring(0, inputMethod.indexOf("/"));
                }
            } catch (Exception e) {
                Slog.e(TAG, "Failed to get default input method");
            }
        }
        if (this.DEBUG_SWITCH) {
            Slog.v(TAG, "defaultInputMethod " + defaultInput);
        }
        if (defaultInput != null) {
            OppoGameSpaceManagerUtils.getInstance().handleDefatultInputMethodAppId(defaultInput);
        }
    }

    public List<Integer> getDozeRuleWhiteAppIdlist() {
        return OppoGameSpaceManagerUtils.getInstance().getDozeRuleWhiteAppIdlist();
    }

    public boolean inDozeRuleAppIdList(int appId) {
        return OppoGameSpaceManagerUtils.getInstance().inDozeRuleAppIdList(appId);
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
        OppoGameSpaceManagerUtils.getInstance().setDynamicDebugSwitch();
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", new Class[]{String.class});
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), new Object[]{OppoGameSpaceManager.class.getName()});
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }
}

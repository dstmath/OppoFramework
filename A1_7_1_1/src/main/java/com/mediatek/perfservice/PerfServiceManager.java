package com.mediatek.perfservice;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.util.Log;
import dalvik.system.VMRuntime;
import java.util.ArrayList;
import java.util.List;

public class PerfServiceManager implements IPerfServiceManager {
    private static final int APP_LAUNCH_DURATION = 3;
    private static final int GAME_LAUNCH_DURATION = 10;
    private static final float HEAP_UTILIZATION_DURING_FRAME_UPDATE = 0.5f;
    private static final int RENDER_AWARE_DURATION_MS = 3000;
    private static final int RENDER_BIT = 8388608;
    private static final String TAG = "PerfServiceManager";
    private static final int UI_UPDATE_DURATION_MS = 300;
    private boolean bDuringTouch;
    private boolean bRenderAwareValid;
    private Context mContext;
    private String mCurrPack;
    private float mDefaultUtilization;
    private int mDisplayType;
    private PerfServiceThreadHandler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread(TAG, -2);
    private PackageManager mPm;
    private VMRuntime mRuntime;
    private SmartObserver mSmartObserver;
    final List<Integer> mTimeList;

    public class PerfServiceAppState {
        private String mClassName;
        private String mPackName;
        private int mPid;
        private int mState;

        PerfServiceAppState(String packName, String className, int state, int pid) {
            this.mPackName = packName;
            this.mClassName = className;
            this.mState = state;
            this.mPid = pid;
        }
    }

    private class PerfServiceThreadHandler extends Handler {
        private static final int MESSAGE_BOOST_DISABLE = 1;
        private static final int MESSAGE_BOOST_ENABLE = 0;
        private static final int MESSAGE_BOOST_ENABLE_TIMEOUT = 2;
        private static final int MESSAGE_BOOST_ENABLE_TIMEOUT_MS = 3;
        private static final int MESSAGE_DUMP_ALL = 24;
        private static final int MESSAGE_GET_PACK_NAME = 29;
        private static final int MESSAGE_NOTIFY_APP_STATE = 4;
        private static final int MESSAGE_NOTIFY_FRAME_UPDATE = 26;
        private static final int MESSAGE_SET_FAVOR_PID = 25;
        private static final int MESSAGE_SET_UEVENT_INDEX = 30;
        private static final int MESSAGE_START_DETECT = 31;
        private static final int MESSAGE_SW_FRAME_UPDATE_TIMEOUT = 27;
        private static final int MESSAGE_TIMER_RENDER_AWARE_DURATION = 40;
        private static final int MESSAGE_TIMER_SCN_BASE = 100;
        private static final int MESSAGE_TIMER_SCN_USER_BASE = 200;
        private static final int MESSAGE_TOUCH_BOOST_DURATION = 28;
        private static final int MESSAGE_USER_DISABLE = 20;
        private static final int MESSAGE_USER_DISABLE_ALL = 22;
        private static final int MESSAGE_USER_ENABLE = 17;
        private static final int MESSAGE_USER_ENABLE_TIMEOUT = 18;
        private static final int MESSAGE_USER_ENABLE_TIMEOUT_MS = 19;
        private static final int MESSAGE_USER_GET_CAPABILITY = 13;
        private static final int MESSAGE_USER_REG = 10;
        private static final int MESSAGE_USER_REG_BIG_LITTLE = 11;
        private static final int MESSAGE_USER_REG_SCN = 14;
        private static final int MESSAGE_USER_REG_SCN_CONFIG = 15;
        private static final int MESSAGE_USER_RESET_ALL = 21;
        private static final int MESSAGE_USER_RESTORE_ALL = 23;
        private static final int MESSAGE_USER_UNREG = 12;
        private static final int MESSAGE_USER_UNREG_SCN = 16;

        public PerfServiceThreadHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 0:
                        PerfServiceManager.nativePerfBoostEnable(msg.arg1);
                        return;
                    case 1:
                        PerfServiceManager.nativePerfBoostDisable(msg.arg1);
                        return;
                    case 2:
                        PerfServiceManager.nativePerfBoostEnable(msg.arg1);
                        startCheckTimer(msg.arg1, msg.arg2);
                        return;
                    case 3:
                        if (msg.arg1 == 7) {
                            if (PerfServiceManager.this.bRenderAwareValid) {
                                PerfServiceManager.this.mRuntime.setTargetHeapUtilization(0.5f);
                            } else {
                                return;
                            }
                        }
                        PerfServiceManager.nativePerfBoostEnable(msg.arg1);
                        startCheckTimerMs(msg.arg1, msg.arg2);
                        return;
                    case 4:
                        PerfServiceAppState passedObject = msg.obj;
                        PerfServiceManager.nativePerfNotifyAppState(passedObject.mPackName, passedObject.mClassName, passedObject.mState, passedObject.mPid);
                        if (passedObject.mState == 1 && PerfServiceManager.this.mCurrPack != passedObject.mPackName) {
                            try {
                                PerfServiceManager.this.mCurrPack = passedObject.mPackName;
                                PerfServiceManager.nativePerfSetPackAttr(PerfServiceManager.this.mPm.getApplicationInfo(PerfServiceManager.this.mCurrPack, 0).isSystemApp() ? 1 : 0, 0);
                                PerfServiceManager.this.boostDisable(3);
                                if (PerfServiceManager.nativePerfUserGetCapability(12) == 1) {
                                    PerfServiceManager.this.boostEnableTimeout(3, 10);
                                }
                                Message msg1 = obtainMessage();
                                msg1.what = 31;
                                removeMessages(msg1.what);
                                sendMessageDelayed(msg1, 3000);
                            } catch (NameNotFoundException e) {
                                PerfServiceManager.this.log("PackageManager exp:" + e);
                            }
                        }
                        msg.obj = null;
                        return;
                    case 12:
                        PerfServiceManager.this.log("MESSAGE_USER_UNREG: " + msg.arg1);
                        PerfServiceManager.nativePerfUserScnUnreg(msg.arg1);
                        return;
                    case 16:
                        PerfServiceManager.this.log("MESSAGE_USER_UNREG_SCN: " + msg.arg1);
                        PerfServiceManager.nativePerfUserUnregScn(msg.arg1);
                        return;
                    case 17:
                        PerfServiceManager.this.log("MESSAGE_USER_ENABLE: " + msg.arg1);
                        PerfServiceManager.nativePerfUserScnEnable(msg.arg1);
                        return;
                    case 18:
                        PerfServiceManager.this.log("MESSAGE_USER_ENABLE_TIMEOUT: " + msg.arg1 + ", " + msg.arg2);
                        PerfServiceManager.nativePerfUserScnEnable(msg.arg1);
                        startCheckUserTimer(msg.arg1, msg.arg2);
                        return;
                    case 19:
                        PerfServiceManager.this.log("MESSAGE_USER_ENABLE_TIMEOUT_MS: " + msg.arg1 + ", " + msg.arg2);
                        PerfServiceManager.nativePerfUserScnEnable(msg.arg1);
                        startCheckUserTimerMs(msg.arg1, msg.arg2);
                        return;
                    case 20:
                        PerfServiceManager.this.log("MESSAGE_USER_DISABLE: " + msg.arg1);
                        PerfServiceManager.nativePerfUserScnDisable(msg.arg1);
                        return;
                    case 21:
                        PerfServiceManager.this.log("MESSAGE_USER_RESET_ALL");
                        stopAllUserTimer();
                        removeAllUserTimerList();
                        PerfServiceManager.nativePerfUserScnResetAll();
                        return;
                    case 22:
                        PerfServiceManager.this.log("MESSAGE_USER_DISABLE_ALL");
                        PerfServiceManager.nativePerfUserScnDisableAll();
                        return;
                    case 23:
                        PerfServiceManager.this.log("MESSAGE_USER_RESTORE_ALL");
                        PerfServiceManager.nativePerfUserScnRestoreAll();
                        return;
                    case 25:
                        PerfServiceManager.nativePerfSetFavorPid(msg.arg1);
                        return;
                    case 30:
                        PerfServiceManager.this.log("MESSAGE_SET_UEVENT_INDEX: " + msg.arg1 + ", " + msg.arg2);
                        PerfServiceManager.nativePerfSetUeventIndex(msg.arg1, msg.arg2);
                        return;
                    case 31:
                        PerfServiceManager.nativePerfNotifyUserStatus(8, 1);
                        return;
                    case 40:
                        PerfServiceManager.this.log("MESSAGE_TIMER_RENDER_AWARE_DURATION timeout");
                        PerfServiceManager.this.bRenderAwareValid = false;
                        PerfServiceManager.nativePerfBoostDisable(7);
                        return;
                    default:
                        int msgId = msg.what;
                        PerfServiceManager.this.log("MESSAGE_TIMEOUT:" + msgId);
                        if (msgId >= 100 && msgId < 200) {
                            int scenario = msgId - 100;
                            if (6 != scenario || PerfServiceManager.this.touchDisable()) {
                                PerfServiceManager.nativePerfBoostDisable(scenario);
                                switch (scenario) {
                                    case 7:
                                        if (!PerfServiceManager.this.bDuringTouch) {
                                            PerfServiceManager.this.bRenderAwareValid = false;
                                        }
                                        PerfServiceManager.this.mRuntime.setTargetHeapUtilization(PerfServiceManager.this.mDefaultUtilization);
                                        PerfServiceManager.this.log("set utilization:" + PerfServiceManager.this.mRuntime.getTargetHeapUtilization());
                                        return;
                                    default:
                                        return;
                                }
                            }
                            return;
                        } else if (msgId >= 200) {
                            PerfServiceManager.nativePerfUserScnDisable(msg.arg1);
                            return;
                        } else {
                            return;
                        }
                }
            } catch (NullPointerException e2) {
                PerfServiceManager.this.loge("Exception in PerfServiceThreadHandler.handleMessage: " + e2);
            }
            PerfServiceManager.this.loge("Exception in PerfServiceThreadHandler.handleMessage: " + e2);
        }

        private void startCheckTimer(int scenario, int timeout) {
            if (scenario > 0 && scenario < 20) {
                Message msg = obtainMessage();
                msg.what = scenario + 100;
                msg.arg1 = scenario;
                sendMessageDelayed(msg, (long) (timeout * 1000));
                if (!PerfServiceManager.this.mTimeList.contains(Integer.valueOf(scenario))) {
                    PerfServiceManager.this.mTimeList.add(Integer.valueOf(scenario));
                }
            }
        }

        private void startCheckTimerMs(int scenario, int timeout_ms) {
            if (scenario > 0 && scenario < 20) {
                Message msg = obtainMessage();
                msg.what = scenario + 100;
                msg.arg1 = scenario;
                sendMessageDelayed(msg, (long) timeout_ms);
                if (!PerfServiceManager.this.mTimeList.contains(Integer.valueOf(scenario))) {
                    PerfServiceManager.this.mTimeList.add(Integer.valueOf(scenario));
                }
            }
        }

        private void stopCheckTimer(int scenario) {
            removeMessages(scenario + 100);
        }

        private void startCheckRenderAwareTimerMs(int timeout_ms) {
            Message msg = obtainMessage();
            msg.what = 40;
            sendMessageDelayed(msg, (long) timeout_ms);
        }

        private void stopCheckRenderAwareTimer() {
            removeMessages(40);
        }

        private void startCheckUserTimer(int handle, int timeout) {
            Message msg = obtainMessage();
            msg.what = handle + 200;
            msg.arg1 = handle;
            sendMessageDelayed(msg, (long) (timeout * 1000));
            if (!PerfServiceManager.this.mTimeList.contains(Integer.valueOf(handle))) {
                PerfServiceManager.this.mTimeList.add(Integer.valueOf(handle));
            }
        }

        private void startCheckUserTimerMs(int handle, int timeout_ms) {
            Message msg = obtainMessage();
            msg.what = handle + 200;
            msg.arg1 = handle;
            sendMessageDelayed(msg, (long) timeout_ms);
            if (!PerfServiceManager.this.mTimeList.contains(Integer.valueOf(handle))) {
                PerfServiceManager.this.mTimeList.add(Integer.valueOf(handle));
            }
        }

        private void stopCheckUserTimer(int handle) {
            removeMessages(handle + 200);
        }

        private void stopAllUserTimer() {
            for (int i = 0; i < PerfServiceManager.this.mTimeList.size(); i++) {
                int timer;
                int handle = ((Integer) PerfServiceManager.this.mTimeList.get(i)).intValue();
                if (handle < 20) {
                    timer = handle + 100;
                } else {
                    timer = handle + 200;
                }
                removeMessages(timer);
            }
        }

        private void removeAllUserTimerList() {
            for (int i = PerfServiceManager.this.mTimeList.size() - 1; i >= 0; i--) {
                PerfServiceManager.this.mTimeList.remove(i);
            }
        }
    }

    private class SmartObserver extends UEventObserver {
        private static final String SMART_UEVENT = "DEVPATH=/devices/virtual/misc/m_smart_misc";

        public void startObserve() {
            startObserving(SMART_UEVENT);
        }

        public void stopObserve() {
            stopObserving();
        }

        public void onUEvent(UEvent event) {
            String index_name = event.get("DETECT");
            String action_name = event.get("ACTION");
            PerfServiceManager.this.setUeventIndex(Integer.parseInt(index_name), Integer.parseInt(action_name));
        }
    }

    public static native int nativePerfBoostDisable(int i);

    public static native int nativePerfBoostEnable(int i);

    public static native int nativePerfDumpAll();

    public static native int nativePerfGetClusterInfo(int i, int i2);

    public static native String nativePerfGetGiftAttr(String str, String str2);

    public static native int nativePerfGetLastBoostPid();

    public static native int nativePerfGetPackAttr(String str, int i);

    public static native int nativePerfLevelBoost(int i);

    public static native int nativePerfNotifyAppState(String str, String str2, int i, int i2);

    public static native int nativePerfNotifyDisplayType(int i);

    public static native int nativePerfNotifyUserStatus(int i, int i2);

    public static native int nativePerfReloadWhiteList();

    public static native int nativePerfRestorePolicy(int i);

    public static native int nativePerfSetExclusiveCore(int i, int i2);

    public static native int nativePerfSetFavorPid(int i);

    public static native int nativePerfSetPackAttr(int i, int i2);

    public static native int nativePerfSetUeventIndex(int i, int i2);

    public static native int nativePerfSetUidInfo(int i, int i2);

    public static native int nativePerfUserGetCapability(int i);

    public static native int nativePerfUserRegScn(int i, int i2);

    public static native int nativePerfUserRegScnConfig(int i, int i2, int i3, int i4, int i5, int i6);

    public static native int nativePerfUserScnDisable(int i);

    public static native int nativePerfUserScnDisableAll();

    public static native int nativePerfUserScnEnable(int i);

    public static native int nativePerfUserScnReg(int i, int i2, int i3, int i4);

    public static native int nativePerfUserScnRegBigLittle(int i, int i2, int i3, int i4, int i5, int i6);

    public static native int nativePerfUserScnResetAll();

    public static native int nativePerfUserScnRestoreAll();

    public static native int nativePerfUserScnUnreg(int i);

    public static native int nativePerfUserUnregScn(int i);

    public PerfServiceManager(Context context) {
        this.mContext = context;
        this.mHandlerThread.start();
        Looper looper = this.mHandlerThread.getLooper();
        if (looper != null) {
            this.mHandler = new PerfServiceThreadHandler(looper);
        }
        this.mTimeList = new ArrayList();
        this.bDuringTouch = false;
        this.bRenderAwareValid = false;
        this.mDisplayType = 1;
        this.mRuntime = VMRuntime.getRuntime();
        this.mDefaultUtilization = this.mRuntime.getTargetHeapUtilization();
        this.mPm = this.mContext.getPackageManager();
        this.mSmartObserver = new SmartObserver();
        this.mSmartObserver.startObserve();
        log("Created and started PerfService thread");
    }

    public void systemReady() {
        log("systemReady, register ACTION_BOOT_COMPLETED");
    }

    public void boostEnable(int scenario) {
        if (6 != scenario || touchEnable()) {
            this.mHandler.stopCheckTimer(scenario);
            Message msg = this.mHandler.obtainMessage();
            msg.what = 0;
            msg.arg1 = scenario;
            msg.sendToTarget();
        }
    }

    public void boostDisable(int scenario) {
        if (6 != scenario || touchDisable()) {
            this.mHandler.stopCheckTimer(scenario);
            Message msg = this.mHandler.obtainMessage();
            msg.what = 1;
            msg.arg1 = scenario;
            msg.sendToTarget();
        }
    }

    public void boostEnableTimeout(int scenario, int timeout) {
        if (6 != scenario || touchEnable()) {
            this.mHandler.stopCheckTimer(scenario);
            Message msg = this.mHandler.obtainMessage();
            msg.what = 2;
            msg.arg1 = scenario;
            msg.arg2 = timeout;
            msg.sendToTarget();
        }
    }

    public void boostEnableTimeoutMs(int scenario, int timeout_ms) {
        if (6 != scenario || touchEnable()) {
            this.mHandler.stopCheckTimer(scenario);
            Message msg = this.mHandler.obtainMessage();
            msg.what = 3;
            msg.arg1 = scenario;
            msg.arg2 = timeout_ms;
            msg.sendToTarget();
        }
    }

    public void notifyAppState(String packName, String className, int state, int pid) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 4;
        msg.obj = new PerfServiceAppState(packName, className, state, pid);
        msg.sendToTarget();
    }

    public int userReg(int scn_core, int scn_freq, int pid, int tid) {
        return nativePerfUserScnReg(scn_core, scn_freq, pid, tid);
    }

    public int userRegBigLittle(int scn_core_big, int scn_freq_big, int scn_core_little, int scn_freq_little, int pid, int tid) {
        return nativePerfUserScnRegBigLittle(scn_core_big, scn_freq_big, scn_core_little, scn_freq_little, pid, tid);
    }

    public void userUnreg(int handle) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 12;
        msg.arg1 = handle;
        msg.sendToTarget();
    }

    public int userGetCapability(int cmd) {
        return nativePerfUserGetCapability(cmd);
    }

    public int userRegScn(int pid, int tid) {
        return nativePerfUserRegScn(pid, tid);
    }

    public void userRegScnConfig(int handle, int cmd, int param_1, int param_2, int param_3, int param_4) {
        nativePerfUserRegScnConfig(handle, cmd, param_1, param_2, param_3, param_4);
    }

    public void userUnregScn(int handle) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 16;
        msg.arg1 = handle;
        msg.sendToTarget();
    }

    public void userEnable(int handle) {
        this.mHandler.stopCheckUserTimer(handle);
        nativePerfUserScnEnable(handle);
    }

    public void userEnableTimeout(int handle, int timeout) {
        this.mHandler.stopCheckUserTimer(handle);
        nativePerfUserScnEnable(handle);
        this.mHandler.startCheckUserTimer(handle, timeout);
    }

    public void userEnableTimeoutMs(int handle, int timeout_ms) {
        this.mHandler.stopCheckUserTimer(handle);
        nativePerfUserScnEnable(handle);
        this.mHandler.startCheckUserTimerMs(handle, timeout_ms);
    }

    public void userEnableAsync(int handle) {
        this.mHandler.stopCheckUserTimer(handle);
        Message msg = this.mHandler.obtainMessage();
        msg.what = 17;
        msg.arg1 = handle;
        msg.sendToTarget();
    }

    public void userEnableTimeoutAsync(int handle, int timeout) {
        this.mHandler.stopCheckUserTimer(handle);
        Message msg = this.mHandler.obtainMessage();
        msg.what = 18;
        msg.arg1 = handle;
        msg.arg2 = timeout;
        msg.sendToTarget();
    }

    public void userEnableTimeoutMsAsync(int handle, int timeout_ms) {
        this.mHandler.stopCheckUserTimer(handle);
        Message msg = this.mHandler.obtainMessage();
        msg.what = 19;
        msg.arg1 = handle;
        msg.arg2 = timeout_ms;
        msg.sendToTarget();
    }

    public void userDisable(int handle) {
        this.mHandler.stopCheckUserTimer(handle);
        Message msg = this.mHandler.obtainMessage();
        msg.what = 20;
        msg.arg1 = handle;
        msg.sendToTarget();
    }

    public void userResetAll() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 21;
        msg.sendToTarget();
    }

    public void userDisableAll() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 22;
        msg.sendToTarget();
    }

    public void userRestoreAll() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 23;
        msg.sendToTarget();
    }

    public void dumpAll() {
        nativePerfDumpAll();
    }

    public void setFavorPid(int pid) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 25;
        msg.arg1 = pid;
        msg.sendToTarget();
    }

    public void restorePolicy(int pid) {
        nativePerfRestorePolicy(pid);
    }

    public void notifyFrameUpdate(int level) {
        if (this.bRenderAwareValid) {
            this.mHandler.stopCheckTimer(7);
            Message msg = this.mHandler.obtainMessage();
            msg.what = 3;
            msg.arg1 = 7;
            msg.arg2 = 300;
            msg.sendToTarget();
        }
    }

    public void notifyDisplayType(int type) {
        log("notifyDisplayType:" + type);
        this.mDisplayType = type;
        nativePerfNotifyDisplayType(type);
    }

    public int getLastBoostPid() {
        return nativePerfGetLastBoostPid();
    }

    public void appBoostEnable(String packName) {
        int timeout = nativePerfGetPackAttr(packName, 2);
        int mode = nativePerfGetPackAttr(packName, 1);
        if (timeout > 0 && mode != -1) {
            boostEnableTimeout(mode, timeout);
        }
    }

    public void notifyUserStatus(int type, int status) {
        nativePerfNotifyUserStatus(type, status);
    }

    public int getClusterInfo(int cmd, int id) {
        return nativePerfGetClusterInfo(cmd, id);
    }

    public boolean touchEnable() {
        nativePerfNotifyUserStatus(3, 6);
        if (this.mDisplayType == 0 || this.mDisplayType == 2) {
            this.bRenderAwareValid = false;
            return false;
        }
        this.bDuringTouch = true;
        this.bRenderAwareValid = true;
        this.mHandler.stopCheckRenderAwareTimer();
        return true;
    }

    public int getPackAttr(String packName, int cmd) {
        return nativePerfGetPackAttr(packName, cmd);
    }

    public String getGiftAttr(String packName, String attrName) {
        return nativePerfGetGiftAttr(packName, attrName);
    }

    public void setExclusiveCore(int pid, int cpu_mask) {
        nativePerfSetExclusiveCore(pid, cpu_mask);
    }

    public void setUidInfo(int uid, int fromUid) {
        nativePerfSetUidInfo(uid, fromUid);
    }

    public boolean touchDisable() {
        nativePerfNotifyUserStatus(4, 6);
        this.bDuringTouch = false;
        if (this.mDisplayType == 0 || this.mDisplayType == 2) {
            this.mHandler.stopCheckTimer(6);
            this.bRenderAwareValid = false;
            return false;
        }
        this.mHandler.startCheckRenderAwareTimerMs(3000);
        return true;
    }

    public void setUeventIndex(int index, int action) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 30;
        msg.arg1 = index;
        msg.arg2 = action;
        if (index == 6) {
            nativePerfNotifyUserStatus(5, 8);
            this.mHandler.sendMessageDelayed(msg, 100);
        } else if (index == 10) {
            nativePerfNotifyUserStatus(5, 10);
            log("cpuset L");
        } else if (index == 11) {
            nativePerfNotifyUserStatus(5, 11);
            log("cpuset LL");
        } else if (index == 12) {
            nativePerfNotifyUserStatus(5, 12);
            log("cpuset L+LL");
        } else if (index == 13) {
            nativePerfNotifyUserStatus(5, 13);
            log("idle_prefer 1");
        } else if (index == 14) {
            nativePerfNotifyUserStatus(5, 14);
            log("idle_prefer 0");
        } else {
            msg.sendToTarget();
        }
    }

    public void levelBoost(int level) {
        nativePerfLevelBoost(level);
    }

    public int reloadWhiteList() {
        return nativePerfReloadWhiteList();
    }

    private void log(String info) {
        Log.d("@M_PerfServiceManager", "[PerfService] " + info + " ");
    }

    private void loge(String info) {
        Log.e("@M_PerfServiceManager", "[PerfService] ERR: " + info + " ");
    }
}

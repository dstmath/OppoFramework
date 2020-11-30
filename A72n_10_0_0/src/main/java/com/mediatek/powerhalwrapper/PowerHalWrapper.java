package com.mediatek.powerhalwrapper;

import android.os.Binder;
import android.os.Build;
import android.os.Process;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PowerHalWrapper {
    private static final int AMS_ACT_SWITCH_BOOST_TIME = 2000;
    private static boolean AMS_BOOST_ACT_SWITCH = true;
    private static boolean AMS_BOOST_PACK_SWITCH = true;
    private static boolean AMS_BOOST_PROCESS_CREATE = true;
    private static boolean AMS_BOOST_PROCESS_CREATE_BOOST = true;
    private static final int AMS_BOOST_TIME = 10000;
    public static final int CMD_SET_APP_CRASH = 174;
    public static final int CMD_SET_CERT_PID = 175;
    public static final int CMD_SET_GPU_LOW_LATENCY = 126;
    public static final int CMD_SET_MD_LOW_LATENCY = 120;
    public static final int CMD_SET_NETD_BOOST_UID = 101;
    public static final int CMD_SET_SCREEN_OFF_STATE = 11;
    public static final int CMD_SET_UX_PREDICT_LOW_LATENCY = 172;
    public static final int CMD_SET_WIFI_SMART_PREDICT = 171;
    private static final boolean ENG = "eng".equals(Build.TYPE);
    private static boolean EXT_PEAK_PERF_MODE = ENG;
    public static final int MAX_NETD_IP_FILTER_COUNT = 3;
    public static final int MTKPOWER_CMD_GET_POWER_SCN_TYPE = 105;
    public static final int MTKPOWER_CMD_GET_RILD_CAP = 40;
    private static final int MTKPOWER_HINT_ACT_SWITCH = 33;
    private static final int MTKPOWER_HINT_ALWAYS_ENABLE = 268435455;
    private static final int MTKPOWER_HINT_APP_ROTATE = 35;
    private static final int MTKPOWER_HINT_APP_TOUCH = 36;
    private static final int MTKPOWER_HINT_BASE = 30;
    private static final int MTKPOWER_HINT_EXT_LAUNCH = 45;
    private static final int MTKPOWER_HINT_GALLERY_BOOST = 39;
    private static final int MTKPOWER_HINT_GALLERY_STEREO_BOOST = 40;
    private static final int MTKPOWER_HINT_GAME_LAUNCH = 34;
    private static final int MTKPOWER_HINT_GAMING = 38;
    private static final int MTKPOWER_HINT_NUM = 49;
    private static final int MTKPOWER_HINT_PACK_SWITCH = 32;
    private static final int MTKPOWER_HINT_PMS_INSTALL = 44;
    private static final int MTKPOWER_HINT_PROCESS_CREATE = 31;
    private static final int MTKPOWER_HINT_SDN = 48;
    private static final int MTKPOWER_HINT_SPORTS = 41;
    private static final int MTKPOWER_HINT_TEST_MODE = 42;
    private static final int MTKPOWER_HINT_WFD = 43;
    private static final int MTKPOWER_HINT_WHITELIST_LAUNCH = 46;
    private static final int MTKPOWER_HINT_WIPHY_SPEED_DL = 47;
    private static final int MTKPOWER_STATE_DEAD = 3;
    private static final int MTKPOWER_STATE_DESTORYED = 2;
    private static final int MTKPOWER_STATE_PAUSED = 0;
    private static final int MTKPOWER_STATE_RESUMED = 1;
    private static final int MTKPOWER_STATE_STOPPED = 4;
    public static final int PERF_RES_NET_MD_CRASH_PID = 41992960;
    public static final int PERF_RES_NET_WIFI_SMART_PREDICT = 41959680;
    public static final int PERF_RES_POWERHAL_SCREEN_OFF_STATE = 54525952;
    public static final int POWER_HIDL_SET_SYS_INFO = 0;
    public static final int SCN_PERF_LOCK_HINT = 3;
    public static final int SCN_USER_HINT = 2;
    public static final int SCREEN_OFF_DISABLE = 0;
    public static final int SCREEN_OFF_ENABLE = 1;
    public static final int SCREEN_OFF_WAIT_RESTORE = 2;
    public static final int SETSYS_FOREGROUND_SPORTS = 3;
    public static final int SETSYS_INTERNET_STATUS = 5;
    public static final int SETSYS_MANAGEMENT_PERIODIC = 4;
    public static final int SETSYS_MANAGEMENT_PREDICT = 1;
    public static final int SETSYS_NETD_DUPLICATE_PACKET_LINK = 8;
    public static final int SETSYS_NETD_STATUS = 6;
    public static final int SETSYS_PACKAGE_VERSION_NAME = 9;
    public static final int SETSYS_PREDICT_INFO = 7;
    public static final int SETSYS_RELOAD_WHITELIST = 10;
    public static final int SETSYS_SPORTS_APK = 2;
    private static final String TAG = "PowerHalWrapper";
    private static final int USER_DURATION_MAX = 30000;
    private static int graphic_low_lat_now = 0;
    private static int graphic_user_hdl = 0;
    private static int graphic_user_pid = 0;
    private static Object lock = new Object();
    private static String mProcessCreatePack = null;
    private static PowerHalWrapper sInstance = null;
    private Lock mLock = new ReentrantLock();
    public List<ScnList> scnlist = new ArrayList();

    public static native int nativeMtkCusPowerHint(int i, int i2);

    public static native int nativeMtkPowerHint(int i, int i2);

    public static native int nativeNotifyAppState(String str, String str2, int i, int i2, int i3);

    public static native int nativePerfLockAcq(int i, int i2, int... iArr);

    public static native int nativePerfLockRel(int i);

    public static native int nativeQuerySysInfo(int i, int i2);

    public static native int nativeScnConfig(int i, int i2, int i3, int i4, int i5, int i6);

    public static native int nativeScnDisable(int i);

    public static native int nativeScnEnable(int i, int i2);

    public static native int nativeScnReg();

    public static native int nativeScnUltraCfg(int i, int i2, int i3, int i4, int i5, int i6);

    public static native int nativeScnUnreg(int i);

    public static native int nativeSetSysInfo(String str, int i);

    public static native int nativeSetSysInfoAsync(String str, int i);

    static {
        System.loadLibrary("powerhalwrap_jni");
    }

    public static PowerHalWrapper getInstance() {
        log("PowerHalWrapper.getInstance");
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    sInstance = new PowerHalWrapper();
                }
            }
        }
        return sInstance;
    }

    private PowerHalWrapper() {
    }

    private void mtkPowerHint(int hint, int data) {
        nativeMtkPowerHint(hint, data);
    }

    public void mtkCusPowerHint(int hint, int data) {
        nativeMtkCusPowerHint(hint, data);
    }

    public int perfLockAcquire(int handle, int duration, int... list) {
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        int new_hdl = nativePerfLockAcq(handle, duration, list);
        if (new_hdl > 0 && new_hdl != handle && (duration > USER_DURATION_MAX || duration == 0)) {
            this.mLock.lock();
            this.scnlist.add(new ScnList(new_hdl, pid, uid));
            this.mLock.unlock();
        }
        return new_hdl;
    }

    public void perfLockRelease(int handle) {
        this.mLock.lock();
        List<ScnList> list = this.scnlist;
        if (list != null && list.size() > 0) {
            Iterator<ScnList> iter = this.scnlist.iterator();
            while (iter.hasNext()) {
                if (iter.next().gethandle() == handle) {
                    iter.remove();
                }
            }
        }
        this.mLock.unlock();
        nativePerfLockRel(handle);
    }

    public int scnReg() {
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        int handle = nativeScnReg();
        if (handle > 0) {
            this.mLock.lock();
            this.scnlist.add(new ScnList(handle, pid, uid));
            this.mLock.unlock();
        }
        return handle;
    }

    public int scnConfig(int hdl, int cmd, int param_1, int param_2, int param_3, int param_4) {
        int pid = Binder.getCallingPid();
        if (cmd == 126) {
            graphic_user_pid = pid;
            graphic_user_hdl = hdl;
            log("<scnConfig> pid:" + pid + " hdl:" + hdl + " cmd:" + cmd);
        }
        nativeScnConfig(hdl, cmd, param_1, param_2, param_3, param_4);
        return 0;
    }

    public int scnUnreg(int hdl) {
        if (hdl == graphic_user_hdl) {
            graphic_user_pid = 0;
            graphic_user_hdl = 0;
        }
        this.mLock.lock();
        List<ScnList> list = this.scnlist;
        if (list != null && list.size() > 0) {
            Iterator<ScnList> iter = this.scnlist.iterator();
            while (iter.hasNext()) {
                if (iter.next().gethandle() == hdl) {
                    iter.remove();
                }
            }
        }
        this.mLock.unlock();
        nativeScnUnreg(hdl);
        return 0;
    }

    public int scnEnable(int hdl, int timeout) {
        int pid = Binder.getCallingPid();
        if (pid == graphic_user_pid && hdl == graphic_user_hdl) {
            SystemProperties.getInt("debug.graphic.lowlatencypid", -1);
            if (-1 == -1) {
                SystemProperties.set("debug.graphic.lowlatencypid", Integer.toString(pid));
            }
            graphic_low_lat_now = 1;
            log("<scnEnable> pid:" + graphic_user_pid + " hdl:" + graphic_user_hdl + " low_lat:" + graphic_low_lat_now);
        }
        nativeScnEnable(hdl, timeout);
        return 0;
    }

    public int scnDisable(int hdl) {
        int pid = Binder.getCallingPid();
        if (graphic_low_lat_now == 1 && pid == graphic_user_pid && hdl == graphic_user_hdl) {
            SystemProperties.set("debug.graphic.lowlatencypid", "-1");
            graphic_low_lat_now = 0;
        }
        log("<scnDisable> pid:" + graphic_user_pid + " hdl:" + graphic_user_hdl + " low_lat" + graphic_low_lat_now);
        nativeScnDisable(hdl);
        return 0;
    }

    public int scnUltraCfg(int hdl, int ultracmd, int param_1, int param_2, int param_3, int param_4) {
        nativeScnUltraCfg(hdl, ultracmd, param_1, param_2, param_3, param_4);
        return 0;
    }

    public void getCpuCap() {
        log("getCpuCap");
    }

    public void getGpuCap() {
        log("mGpuCap");
    }

    public void getGpuRTInfo() {
        log("getGpuCap");
    }

    public void getCpuRTInfo() {
        log("mCpuRTInfo");
    }

    public void UpdateManagementPkt(int type, String packet) {
        logd("<UpdateManagementPkt> type:" + type + ", packet:" + packet);
        if (type == 1) {
            nativeSetSysInfo(packet, 1);
        } else if (type == 4) {
            nativeSetSysInfo(packet, 4);
        }
    }

    public int setSysInfo(int type, String data) {
        logd("<setSysInfo> type:" + type + " data:" + data);
        return nativeSetSysInfo(data, type);
    }

    public void setSysInfoAsync(int type, String data) {
        logd("<setSysInfoAsync> type:" + type + " data:" + data);
        nativeSetSysInfoAsync(data, type);
    }

    public int querySysInfo(int cmd, int param) {
        logd("<querySysInfo> cmd:" + cmd + " param:" + param);
        return nativeQuerySysInfo(cmd, param);
    }

    public void galleryBoostEnable(int timeoutMs) {
        log("<galleryBoostEnable> do boost with " + timeoutMs + "ms");
        nativeMtkPowerHint(MTKPOWER_HINT_GALLERY_BOOST, timeoutMs);
    }

    public void setRotationBoost(int boostTime) {
        log("<setRotation> do boost with " + boostTime + "ms");
        nativeMtkPowerHint(MTKPOWER_HINT_APP_ROTATE, boostTime);
    }

    public void setSpeedDownload(int timeoutMs) {
        log("<setSpeedDownload> do boost with " + timeoutMs + "ms");
        nativeMtkPowerHint(MTKPOWER_HINT_WIPHY_SPEED_DL, timeoutMs);
    }

    public void setWFD(boolean enable) {
        log("<setWFD> enable:" + enable);
        if (enable) {
            nativeMtkPowerHint(MTKPOWER_HINT_WFD, MTKPOWER_HINT_ALWAYS_ENABLE);
        } else {
            nativeMtkPowerHint(MTKPOWER_HINT_WFD, 0);
        }
    }

    public void setSportsApk(String pack) {
        log("<setSportsApk> pack:" + pack);
        nativeSetSysInfo(pack, 2);
    }

    public void NotifyAppCrash(int pid, int uid, String packageName) {
        int found = 0;
        int myPid = Process.myPid();
        if (myPid == pid) {
            log("<NotifyAppCrash> pack:" + packageName + " ,pid:" + packageName + " == myPid:" + myPid);
            return;
        }
        nativeNotifyAppState(packageName, packageName, pid, 3, uid);
        this.mLock.lock();
        List<ScnList> list = this.scnlist;
        if (list != null && list.size() > 0) {
            Iterator<ScnList> iter = this.scnlist.iterator();
            while (iter.hasNext()) {
                ScnList item = iter.next();
                if (item.getpid() == pid) {
                    if (graphic_low_lat_now == 1 && pid == graphic_user_pid && item.gethandle() == graphic_user_hdl) {
                        graphic_user_pid = 0;
                        graphic_user_hdl = 0;
                        graphic_low_lat_now = 0;
                        log("<NotifyAppCrash> pid:" + graphic_user_pid + " hdl:" + graphic_user_hdl + " low_lat" + graphic_low_lat_now);
                        SystemProperties.set("debug.graphic.lowlatencypid", "-1");
                    }
                    int type = nativeQuerySysInfo(105, item.gethandle());
                    log("<NotifyAppCrash> handle:" + item.gethandle() + ", type:" + type);
                    if (type == 2 || type == 3) {
                        if (type == 2) {
                            nativeScnDisable(item.gethandle());
                            nativeScnUnreg(item.gethandle());
                        } else if (type == 3) {
                            nativePerfLockRel(item.gethandle());
                        }
                        log("<NotifyAppCrash> pid:" + item.getpid() + " uid:" + item.getuid() + " handle:" + item.gethandle());
                    }
                    iter.remove();
                    found++;
                }
            }
        }
        this.mLock.unlock();
    }

    public boolean getRildCap(int uid) {
        if (nativeQuerySysInfo(40, uid) == 1) {
            return true;
        }
        return ENG;
    }

    public void setInstallationBoost(boolean enable) {
        log("<setInstallationBoost> enable:" + enable);
        if (enable) {
            nativeMtkPowerHint(MTKPOWER_HINT_PMS_INSTALL, 15000);
        } else {
            nativeMtkPowerHint(MTKPOWER_HINT_PMS_INSTALL, 0);
        }
    }

    public void amsBoostResume(String lastResumedPackageName, String nextResumedPackageName) {
        logd("<amsBoostResume> last:" + lastResumedPackageName + ", next:" + nextResumedPackageName);
        Trace.asyncTraceBegin(64, "amPerfBoost", 0);
        nativeMtkPowerHint(MTKPOWER_HINT_EXT_LAUNCH, 0);
        if (lastResumedPackageName == null || !lastResumedPackageName.equalsIgnoreCase(nextResumedPackageName)) {
            AMS_BOOST_PACK_SWITCH = true;
            nativeMtkPowerHint(32, AMS_BOOST_TIME);
            return;
        }
        AMS_BOOST_ACT_SWITCH = true;
        nativeMtkPowerHint(MTKPOWER_HINT_ACT_SWITCH, AMS_ACT_SWITCH_BOOST_TIME);
    }

    public void amsBoostProcessCreate(String hostingType, String packageName) {
        if (hostingType != null && hostingType.compareTo("activity") == 0) {
            log("amsBoostProcessCreate package:" + packageName);
            Trace.asyncTraceBegin(64, "amPerfBoost", 0);
            AMS_BOOST_PROCESS_CREATE = true;
            AMS_BOOST_PROCESS_CREATE_BOOST = true;
            mProcessCreatePack = packageName;
            nativeMtkPowerHint(MTKPOWER_HINT_EXT_LAUNCH, 0);
            nativeMtkPowerHint(MTKPOWER_HINT_PROCESS_CREATE, AMS_BOOST_TIME);
        }
    }

    public void amsBoostStop() {
        logd("amsBoostStop AMS_BOOST_PACK_SWITCH:" + AMS_BOOST_PACK_SWITCH + ", AMS_BOOST_ACT_SWITCH:" + AMS_BOOST_ACT_SWITCH + ", AMS_BOOST_PROCESS_CREATE:" + AMS_BOOST_PROCESS_CREATE);
        if (AMS_BOOST_PACK_SWITCH) {
            AMS_BOOST_PACK_SWITCH = ENG;
            nativeMtkPowerHint(32, 0);
        }
        if (AMS_BOOST_ACT_SWITCH) {
            AMS_BOOST_ACT_SWITCH = ENG;
            nativeMtkPowerHint(MTKPOWER_HINT_ACT_SWITCH, 0);
        }
        if (AMS_BOOST_PROCESS_CREATE) {
            AMS_BOOST_PROCESS_CREATE = ENG;
            nativeMtkPowerHint(MTKPOWER_HINT_PROCESS_CREATE, 0);
        }
        Trace.asyncTraceEnd(64, "amPerfBoost", 0);
    }

    public void amsBoostNotify(int pid, String activityName, String packageName, int uid) {
        logd("amsBoostNotify pid:" + pid + ",activity:" + activityName + ", package:" + packageName + ", mProcessCreatePack" + mProcessCreatePack);
        nativeNotifyAppState(packageName, activityName, pid, 1, uid);
        if (!packageName.equalsIgnoreCase(mProcessCreatePack)) {
            logd("amsBoostNotify AMS_BOOST_PROCESS_CREATE_BOOST:" + AMS_BOOST_PROCESS_CREATE_BOOST);
            if (AMS_BOOST_PROCESS_CREATE_BOOST) {
                nativeMtkPowerHint(MTKPOWER_HINT_PROCESS_CREATE, 1);
            }
            AMS_BOOST_PROCESS_CREATE_BOOST = ENG;
        }
    }

    private static void log(String info) {
        Log.i(TAG, info + " ");
    }

    private static void logd(String info) {
        if (ENG) {
            Log.d(TAG, info + " ");
        }
    }

    private static void loge(String info) {
        Log.e(TAG, "ERR: " + info + " ");
    }
}

package com.android.server.theia;

import android.app.ActivityManager;
import android.app.OppoActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.am.ActivityManagerService;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import oppo.util.OppoStatistics;

public class TheiaUtil {
    private static final int PRESSDURATION = 1000;
    private static final int PRESSTIME = 4;
    private static final String TAG = "TheiaManager";
    private static volatile TheiaUtil mTheiaUtil;
    private long[] backPress = new long[4];
    public Map<String, String> logMap = new ConcurrentHashMap();

    public static TheiaUtil getInstance() {
        if (mTheiaUtil == null) {
            synchronized (TheiaXMLParser.class) {
                if (mTheiaUtil == null) {
                    mTheiaUtil = new TheiaUtil();
                }
            }
        }
        return mTheiaUtil;
    }

    private String getUuid() {
        String btmac = SystemProperties.get("persist.vendor.service.bdroid.bdaddr", "na").replace(":", "");
        long curTime = System.currentTimeMillis();
        String str = Long.toHexString(8388065809250910208L).concat(btmac).concat(Long.toHexString(curTime));
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            Log.e(TAG, "md5 encryption error!");
            e.printStackTrace();
            return Long.toHexString(8388065809250910208L).concat("badmd5").concat(Long.toHexString(curTime));
        }
    }

    private void getFocusProcessLog(String pkgName, int categoryId) {
        int pid = 0;
        try {
            for (ActivityManager.RunningAppProcessInfo appProcess : ActivityManager.getService().getRunningAppProcesses()) {
                if (pkgName.equals(appProcess.processName)) {
                    Log.d(TAG, "PID = " + appProcess.pid);
                    ArrayList<Integer> pids = new ArrayList<>();
                    pids.add(Integer.valueOf(appProcess.pid));
                    pid = appProcess.pid;
                    ActivityManagerService.dumpStackTraces(pids, (ProcessCpuTracker) null, (SparseArray<Boolean>) null, (ArrayList<Integer>) null);
                }
            }
            SystemProperties.set("sys.theia.log_type", String.valueOf(categoryId));
            SystemProperties.set("sys.theia.target_pids", "" + pid);
            SystemProperties.set("sys.theia.target_uuid", getUuid());
            Log.e(TAG, "[tagL]: starting dumpstate_theia via ctl.start ...");
            SystemProperties.set("ctl.start", "dumpstate_theia");
        } catch (Exception e) {
            Log.d(TAG, "getFocusProcessLog Error: " + e.toString());
        }
    }

    private void sendEvent(String pkgName, long category, String categoryString, Boolean logEnable, Context mContext) {
        Exception ex;
        try {
            this.logMap.clear();
            StringBuilder event0Info = new StringBuilder();
            event0Info.append("packageName:" + pkgName);
            this.logMap.put("mFocusedPackage", pkgName);
            this.logMap.put("event0", categoryString);
            this.logMap.put("event0info", event0Info.toString());
            this.logMap.put("otaVersion", SystemProperties.get("ro.build.version.ota", ""));
            if (category == TheiaConst.THEIA_ST_NFW) {
                this.logMap.put("deadMoment", String.valueOf(System.currentTimeMillis()));
            }
            if (logEnable.booleanValue()) {
                getFocusProcessLog(pkgName, getSimpleId(category));
            }
            TheiaSocket instance = TheiaSocket.getInstance();
            instance.sendMessage(category + ":" + categoryString + ":" + System.currentTimeMillis());
            if (category != TheiaConst.THEIA_ST_NFW) {
                try {
                    OppoStatistics.onCommon(mContext, "CriticalLog", "Theia", this.logMap, false);
                } catch (Exception e) {
                    ex = e;
                }
            }
        } catch (Exception e2) {
            ex = e2;
            Log.d(TAG, StringUtils.SPACE + ex);
        }
    }

    public void sendTheiaEvent(String pkgName, long category, Context mContext) {
        sendEvent(pkgName, category, getSimpleName(category), Boolean.valueOf(getEventEnable(category, mContext)), mContext);
    }

    public String getForegroundPackage() {
        ComponentName cn;
        try {
            cn = new OppoActivityManager().getTopActivityComponentName();
        } catch (Exception e) {
            Log.w(TAG, "getTopActivityComponentName exception");
            cn = null;
        }
        if (cn != null) {
            return cn.getPackageName();
        }
        return null;
    }

    public void onBackPressedOnTheiaMonitor(long pressNow, Context mContext) {
        TheiaXMLParser.getInstance(mContext);
        mTheiaUtil.backPress[3] = pressNow;
        long[] jArr = this.backPress;
        if (jArr[3] - jArr[0] < 1000) {
            Log.d("TheiaBackMonitor", "KEY_BACK has been pressed over 4 times in 1000ms");
            Arrays.fill(this.backPress, 0L);
        }
        long[] jArr2 = this.backPress;
        System.arraycopy(jArr2, 1, jArr2, 0, jArr2.length - 1);
    }

    public boolean getEventEnable(long eventNum, Context context) {
        if (eventNum == TheiaConst.THEIA_ST_ANR) {
            return TheiaXMLParser.getInstance(context).getAppNotRespondingEnable();
        }
        if (eventNum == TheiaConst.THEIA_ST_NFW) {
            return TheiaXMLParser.getInstance(context).getNoFocusWindowEnable();
        }
        if (eventNum == TheiaConst.THEIA_ST_BTF) {
            return TheiaXMLParser.getInstance(context).getBootFailedEnable();
        }
        if (eventNum == TheiaConst.THEIA_ST_CRS) {
            return TheiaXMLParser.getInstance(context).getUICrashEnable();
        }
        if (eventNum == TheiaConst.THEIA_ST_UTO) {
            return TheiaXMLParser.getInstance(context).getUITimeoutEnable();
        }
        if (eventNum == TheiaConst.THEIA_ST_BCK) {
            return TheiaXMLParser.getInstance(context).getBackKeyEnable();
        }
        return false;
    }

    public String getSimpleName(long eventNum) {
        if (eventNum == TheiaConst.THEIA_ST_ANR) {
            return "ApplicationNotRespond";
        }
        if (eventNum == TheiaConst.THEIA_ST_NFW) {
            return "NoFocusWindow";
        }
        if (eventNum == TheiaConst.THEIA_ST_BTF) {
            return "BootFailed";
        }
        if (eventNum == TheiaConst.THEIA_ST_CRS) {
            return "SystemUICrash";
        }
        if (eventNum == TheiaConst.THEIA_ST_UTO) {
            return "UITimeout";
        }
        if (eventNum == TheiaConst.THEIA_ST_BCK) {
            return "BackKeyOverTime";
        }
        return "";
    }

    public int getSimpleId(long eventNum) {
        if (eventNum == TheiaConst.THEIA_ST_ANR) {
            return 1;
        }
        if (eventNum == TheiaConst.THEIA_ST_NFW) {
            return 2;
        }
        if (eventNum == TheiaConst.THEIA_ST_BTF) {
            return 3;
        }
        if (eventNum == TheiaConst.THEIA_ST_CRS) {
            return 4;
        }
        if (eventNum == TheiaConst.THEIA_ST_UTO) {
            return 5;
        }
        if (eventNum == TheiaConst.THEIA_ST_BCK) {
            return 6;
        }
        return 0;
    }
}

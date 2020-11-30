package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.OppoManager;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.location.interfaces.IPswGnssDiagnosticTool;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OppoGnssDiagnosticTool implements IPswGnssDiagnosticTool {
    private static final String ACTION_DATE_CHANGED = "android.intent.action.DATE_CHANGED";
    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
    private static final int BACKGROUND_ALLOWED = 2;
    private static final int BAD_CN0_STANDARD_FOR_ALL = 30;
    private static final int BAD_CN0_STANDARD_FOR_DRIVING = 30;
    private static final int DENY_ALWAYS = 0;
    private static final int FOREGROUND_ALLOWED = 1;
    private static final String GNSS_DIAGNOSTIC_EVENT_ID = "060101";
    private static final int GOOD_CN0_STANDARD_FOR_ALL = 35;
    private static final String KEY_APP_USING_GPS_REC = "DailyAppUsingGpsRec";
    private static final String KEY_GNSS_REQUEST_TIMER = "gnssTimer";
    private static final String KEY_LOC_MODE_CHANGE_REC = "LocationModeChangeRec";
    private static final String KEY_MAX_CN0 = "maxCn0";
    private static final String KEY_MAX_SV_COUNT = "maxSvCount";
    private static final String KEY_MAX_USED_COUNT = "maxUsedSvCount";
    private static final int MAX_LIST_SIZE = 10000;
    private static final String OPPO_LBS_LIST_FULL_ACTION = "com.android.location.oppo.diagnosticTool.fullList";
    private static final float SPEED_FOR_JUDGING_DRIVING = 11.11f;
    private static final String TAG = "OppoGnssDiagnosticTool";
    private static final String[] TARGET_APPS = {"com.baidu.BaiduMap", "com.tencent.map", "com.autonavi.minimap", "com.sogou.map.android.maps", "cld.navi.mainframe", "com.google.android.apps.maps", "com.sdu.didi.gsui"};
    private static boolean mDebug = false;
    private static OppoGnssDiagnosticTool mGnssDiagnosticTool = null;
    private static OppoGnssDiagnosticTool mInstall = null;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.OppoGnssDiagnosticTool.AnonymousClass1 */

        /* JADX WARNING: Removed duplicated region for block: B:24:0x005a A[ADDED_TO_REGION] */
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            OppoGnssDiagnosticTool.this.logD("receive broadcast intent, action: " + action);
            if (action != null) {
                synchronized (OppoGnssDiagnosticTool.this.mLock) {
                    char c = 65535;
                    int hashCode = action.hashCode();
                    if (hashCode != -589718437) {
                        if (hashCode != 1041332296) {
                            if (hashCode == 1947666138 && action.equals(OppoGnssDiagnosticTool.ACTION_SHUTDOWN)) {
                                c = 0;
                                if (c != 0 || c == 1 || c == 2) {
                                    OppoGnssDiagnosticTool.this.updateGnssSvDataByDay();
                                    OppoGnssDiagnosticTool.this.resetValue();
                                }
                            }
                        } else if (action.equals(OppoGnssDiagnosticTool.ACTION_DATE_CHANGED)) {
                            c = 1;
                            if (c != 0) {
                            }
                            OppoGnssDiagnosticTool.this.updateGnssSvDataByDay();
                            OppoGnssDiagnosticTool.this.resetValue();
                        }
                    } else if (action.equals(OppoGnssDiagnosticTool.OPPO_LBS_LIST_FULL_ACTION)) {
                        c = 2;
                        if (c != 0) {
                        }
                        OppoGnssDiagnosticTool.this.updateGnssSvDataByDay();
                        OppoGnssDiagnosticTool.this.resetValue();
                    }
                    if (c != 0) {
                    }
                    OppoGnssDiagnosticTool.this.updateGnssSvDataByDay();
                    OppoGnssDiagnosticTool.this.resetValue();
                }
            }
        }
    };
    private Context mContext = null;
    private List<String> mDailyAppSvRec = new ArrayList();
    private int mGnssRequestTimer = 0;
    @GuardedBy({"mLock"})
    private ConcurrentHashMap<String, AppGpsUsingRec> mLivingAppSvRec = new ConcurrentHashMap<>();
    private OppoLocModeRecord mLocModeRec = null;
    private final Object mLock = new Object();
    private int mMaxCN0 = 0;
    private int mMaxSvCount = 0;
    private int mMaxUsedSvCount = 0;
    private PackageManager mPackageManager;

    public static OppoGnssDiagnosticTool getInstall(Context context) {
        if (mInstall == null) {
            mInstall = new OppoGnssDiagnosticTool(context);
        }
        return mInstall;
    }

    private OppoGnssDiagnosticTool(Context context) {
        this.mContext = context;
        initValue();
    }

    private void initValue() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(OPPO_LBS_LIST_FULL_ACTION);
        filter.addAction(ACTION_DATE_CHANGED);
        filter.addAction(ACTION_SHUTDOWN);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, null);
        this.mPackageManager = this.mContext.getPackageManager();
        this.mLocModeRec = OppoLocModeRecord.getInstall(this.mContext);
    }

    public void storeSatellitesInfo(int svCount, int usedSvcount, int cn0) {
        synchronized (this.mLock) {
            if (this.mMaxSvCount < svCount) {
                logD("Change SvCount to : " + this.mMaxSvCount);
                this.mMaxSvCount = svCount;
            }
            if (this.mMaxUsedSvCount < usedSvcount) {
                logD("Change usedSvCount to : " + usedSvcount);
                this.mMaxUsedSvCount = usedSvcount;
            }
            if (this.mMaxCN0 < cn0) {
                logD("Change maxCn0 to : " + this.mMaxCN0);
                this.mMaxCN0 = cn0;
            }
        }
    }

    @GuardedBy({"mLock"})
    public void storeAppSvInfo(int maxCn0, float speed) {
        synchronized (this.mLock) {
            for (String key : this.mLivingAppSvRec.keySet()) {
                this.mLivingAppSvRec.get(key).incSvSta(0);
                if (GOOD_CN0_STANDARD_FOR_ALL < maxCn0) {
                    this.mLivingAppSvRec.get(key).incSvSta(1);
                } else if (30 > maxCn0) {
                    this.mLivingAppSvRec.get(key).incSvSta(2);
                }
                if (SPEED_FOR_JUDGING_DRIVING < speed) {
                    this.mLivingAppSvRec.get(key).incSvSta(3);
                    if (30 > maxCn0) {
                        this.mLivingAppSvRec.get(key).incSvSta(4);
                    }
                }
            }
        }
    }

    public void refreshRequestTimer() {
        synchronized (this.mLock) {
            this.mGnssRequestTimer++;
        }
    }

    private int getPermCode(String pkg) {
        if (this.mPackageManager.checkPermission("android.permission.ACCESS_BACKGROUND_LOCATION", pkg) == 0) {
            return 2;
        }
        if (this.mPackageManager.checkPermission("android.permission.ACCESS_FINE_LOCATION", pkg) == 0 || this.mPackageManager.checkPermission("android.permission.ACCESS_COARSE_LOCATION", pkg) == 0) {
            return 1;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateGnssSvDataByDay() {
        Map<String, String> map = getDailyData();
        OppoManager.onStamp(GNSS_DIAGNOSTIC_EVENT_ID, map);
        logD("Data Stall statistics for GNSS DATA : " + map);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetValue() {
        synchronized (this.mLock) {
            this.mMaxCN0 = 0;
            this.mMaxSvCount = 0;
            this.mMaxUsedSvCount = 0;
            this.mGnssRequestTimer = 0;
            this.mDailyAppSvRec.clear();
            this.mLocModeRec.resetRec();
        }
    }

    @GuardedBy({"mLock"})
    private Map<String, String> getDailyData() {
        Map<String, String> map = new LinkedHashMap<>();
        StringBuilder sb = new StringBuilder();
        String str = StringUtils.EMPTY;
        sb.append(str);
        sb.append(this.mMaxCN0);
        map.put(KEY_MAX_CN0, sb.toString());
        map.put(KEY_MAX_SV_COUNT, str + this.mMaxSvCount);
        map.put(KEY_MAX_USED_COUNT, str + this.mMaxUsedSvCount);
        map.put(KEY_GNSS_REQUEST_TIMER, str + this.mGnssRequestTimer);
        map.put(KEY_LOC_MODE_CHANGE_REC, this.mLocModeRec.getAllRec().isEmpty() ? str : String.join(",", this.mLocModeRec.getAllRec()));
        if (!this.mDailyAppSvRec.isEmpty()) {
            str = String.join("|", this.mDailyAppSvRec);
        }
        map.put(KEY_APP_USING_GPS_REC, str);
        return map;
    }

    @GuardedBy({"mLock"})
    public void incomingNewGpsUsingApp(String providerName, String apkName) {
        if (isValidApp(apkName) && providerName != null && "gps".equals(providerName)) {
            synchronized (this.mLock) {
                if (this.mLivingAppSvRec != null && !this.mLivingAppSvRec.containsKey(apkName)) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM-dd_HH:mm:ss");
                    AppGpsUsingRec agur = new AppGpsUsingRec();
                    agur.setBeginTime(simpleDateFormat.format(new Date(System.currentTimeMillis())));
                    agur.setLocMode(this.mLocModeRec.getLocMode());
                    agur.setLocPerm(getPermCode(apkName));
                    this.mLivingAppSvRec.put(apkName, agur);
                }
            }
        }
    }

    @GuardedBy({"mLock"})
    public void removingGpsUsingApp(String providerName, String apkName) {
        if (providerName != null && "gps".equals(providerName)) {
            synchronized (this.mLock) {
                if (this.mLivingAppSvRec != null && this.mLivingAppSvRec.containsKey(apkName)) {
                    this.mLivingAppSvRec.get(apkName).setFinishTime(new SimpleDateFormat("yyyy_MM-dd_HH:mm:ss").format(new Date(System.currentTimeMillis())));
                    if (!this.mLivingAppSvRec.get(apkName).isVain()) {
                        List<String> list = this.mDailyAppSvRec;
                        list.add(apkName + StringUtils.SPACE + this.mLivingAppSvRec.get(apkName).toString());
                    }
                    this.mLivingAppSvRec.remove(apkName);
                }
            }
            if (this.mDailyAppSvRec.size() >= MAX_LIST_SIZE) {
                Intent intent = new Intent();
                intent.setAction(OPPO_LBS_LIST_FULL_ACTION);
                this.mContext.sendBroadcast(intent);
            }
        }
    }

    private boolean isValidApp(String apkName) {
        for (String name : TARGET_APPS) {
            if (name.equals(apkName)) {
                return true;
            }
        }
        return false;
    }

    private class AppGpsUsingRec {
        private String mBeginTime = null;
        private String mFinishTime = null;
        private int mLocMode = 0;
        private int mLocPerm = 0;
        private int[] mSvSta = {0, 0, 0, 0, 0};

        public AppGpsUsingRec() {
        }

        public void setBeginTime(String time) {
            this.mBeginTime = time;
        }

        public void setFinishTime(String time) {
            this.mFinishTime = time;
        }

        public void setLocMode(int mode) {
            this.mLocMode = mode;
        }

        public void setLocPerm(int perm) {
            this.mLocPerm = perm;
        }

        public void incSvSta(int index) {
            int[] iArr = this.mSvSta;
            iArr[index] = iArr[index] + 1;
        }

        public boolean isVain() {
            return this.mSvSta[0] == 0;
        }

        public String toString() {
            return this.mBeginTime + StringUtils.SPACE + this.mFinishTime + StringUtils.SPACE + this.mLocMode + StringUtils.SPACE + this.mLocPerm + StringUtils.SPACE + Arrays.toString(this.mSvSta);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logD(String sencent) {
        if (mDebug) {
            Log.d(TAG, sencent);
        }
    }

    public static void setDebug(boolean debug) {
        mDebug = debug;
    }
}

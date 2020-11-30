package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.wifi.V1_3.IWifiChip;
import android.net.Uri;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.OppoAssertTip;
import android.os.OppoManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.rtt.RttServiceImpl;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import oppo.util.OppoStatistics;

public class OppoWifiSwitchStats {
    private static final String ACTION_CLEAR_WIFI_SWITCH_LOG_COUNT = "oppo.intent.action.CLEAR_WIFI_LOG_COUNT";
    private static final String ACTION_WIFI_SWITCH_LOG_TESTING = "oppo.intent.action.TRIGGER_WIFI_SWITCH_LOG";
    private static final int CHECK_FW_STATUS_DELAY = 5000;
    private static final int CHECK_WIFI_STATE_INTERVAL = 8000;
    private static boolean DEBUG = true;
    private static final int DEFAULT_BOOT_STAGE_RETRY_LIMIT = 3;
    private static final int DEFAULT_BOOT_STAGE_TIME = 120000;
    private static final String DRIVER_LOADED = "driver_loaded";
    private static final String DRIVER_UNLOADED = "driver_unloaded";
    private static final String EVENT_ID_WIFI_SWITCH_STAMP = "060201";
    private static final String EVENT_ID_WIFI_TURN_ON_FAILED = "wifi_turn_on_failed";
    private static final String EVENT_ID_WIFI_TURN_ON_SUCCESS = "wifi_turn_on_success";
    private static final String FW_NODE = "/sys/bus/platform/drivers/icnss/firmware_ready";
    private static final int LOG_CAPTURE_DURATION = 40000;
    private static final int LOG_CAPTURE_PEACE_INTERVAL = 72000000;
    private static final int LOG_COLLECT_STAGE_ALL = 4;
    private static final int LOG_COLLECT_STAGE_MAJORITY = 3;
    private static final int LOG_COLLECT_STAGE_MEDIUM = 2;
    private static final int LOG_COLLECT_STAGE_MINORITY = 1;
    private static final String MODULE_FILE = "/proc/modules";
    private static final int MSG_CHECK_FW_STATUS = 7;
    private static final int MSG_CHECK_WIFI_DISABLE_STATE_TO_SCANONLY = 8;
    private static final int MSG_CHECK_WIFI_ENABLE_STATE = 6;
    private static final int MSG_STOP_LOGGING = 4;
    private static final int MSG_TRIGGER_LOG_UPLOAD = 3;
    private static final int MSG_UPLOAD_VIA_CELLULAR = 5;
    private static final int MSG_WIFI_TURN_OFF_FAILED = 10;
    private static final int MSG_WIFI_TURN_ON_FAILED = 0;
    private static final int MSG_WIFI_TURN_ON_FAILED_COLLECT_LOG = 2;
    private static final int MSG_WIFI_TURN_ON_SUCCESS = 1;
    public static final int REASON_WIFI_FW_RELOAD_FAIL = 5;
    public static final int REASON_WIFI_MANUAL_TRIGGER = 3;
    public static final int REASON_WIFI_SCANONLY_TRIGGER = 4;
    public static final int REASON_WIFI_SERVICE_CHECK = 1;
    public static final int REASON_WIFI_STATE_UNKNOWN = 2;
    public static final int REASON_WIFI_TURN_OFF_FAILED = 6;
    public static final int REASON_WIFI_TURN_ON_SUCCESS = 0;
    private static final String TAG = "OppoWifiSwitchStats";
    private static final String UNKNOWN = "unknown";
    private static final int WAIT_FOR_LOG_STOP_INTERVAL = 5000;
    private static final String WIFI_FTM_DISABLED = "0";
    private static final String WIFI_FTM_ENABLED = "1";
    private static final String WIFI_FTM_PROP = "oppo.wifi.ftmtest";
    private static final String WIFI_SWITCH_LOG_COUNT_VALUE = "oppo.wifi.switch.log.count";
    private static final String WLAN_MODULE = "wlan";
    private static OppoWifiSwitchStats sInstance = null;
    private WifiSwitchStatsBroadcastReceiver mBroadcastReceiver;
    private boolean mCanCollectLog = false;
    private PendingIntent mClearWifiLogCountIntent;
    private Context mContext;
    private int mDisableStateRetryCount = 0;
    private boolean mFwNotReady = false;
    private Handler mHandler;
    private boolean mIsInBootStage = false;
    private boolean mIsInUnknownState = false;
    private boolean mIsLogging = false;
    private boolean mIsRetry = false;
    private Looper mLooper;
    private int mMaxRetryLimit = 3;
    private int mRetryCount = 0;
    private final WifiSettingsStore mSettingsStore;
    private String mStoredPkgName = UNKNOWN;
    private WifiInjector mWifiInjector;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;
    private Queue<String> mWifiSwitchCallerPkgs;

    private OppoWifiSwitchStats(Context context) {
        this.mContext = context;
        this.mWifiInjector = WifiInjector.getInstance();
        this.mLooper = this.mWifiInjector.getWifiServiceHandlerThread().getLooper();
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        this.mHandler = new WifiSwitchStatsHandler(this.mLooper);
        this.mBroadcastReceiver = new WifiSwitchStatsBroadcastReceiver();
        this.mWifiSwitchCallerPkgs = new LinkedList();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CLEAR_WIFI_SWITCH_LOG_COUNT);
        filter.addAction(ACTION_WIFI_SWITCH_LOG_TESTING);
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        this.mSettingsStore = this.mWifiInjector.getWifiSettingsStore();
        this.mWifiRomUpdateHelper.getIntegerValue("OPPO_BASIC_WIFI_SWITCH_RETRY_LIMIT", 3).intValue();
    }

    public static synchronized OppoWifiSwitchStats getInstance(Context context) {
        OppoWifiSwitchStats oppoWifiSwitchStats;
        synchronized (OppoWifiSwitchStats.class) {
            if (sInstance == null) {
                synchronized (OppoWifiSwitchStats.class) {
                    if (sInstance == null) {
                        sInstance = new OppoWifiSwitchStats(context);
                    }
                }
            }
            oppoWifiSwitchStats = sInstance;
        }
        return oppoWifiSwitchStats;
    }

    public static OppoWifiSwitchStats getInstanceOrNull() {
        return sInstance;
    }

    public void informWifiTurnOnResult(int reason) {
        Message msg;
        if (reason == 0) {
            this.mRetryCount = 0;
            msg = Message.obtain(this.mHandler, 1);
        } else {
            msg = Message.obtain(this.mHandler, 0);
        }
        String pkg = this.mWifiSwitchCallerPkgs.poll();
        if (pkg == null) {
            pkg = UNKNOWN;
        }
        msg.arg1 = reason;
        msg.obj = pkg;
        msg.sendToTarget();
    }

    public void informWifiTurnOffResult(int reason) {
        clearWifiStateCheckMsg();
        Message msg = Message.obtain(this.mHandler, 10);
        msg.arg1 = reason;
        msg.sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void enqueueCallerPkg(String pkgName) {
        this.mWifiSwitchCallerPkgs.offer(pkgName);
    }

    /* access modifiers changed from: protected */
    public void checkFwStatusInBoot() {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(7), RttServiceImpl.HAL_RANGING_TIMEOUT_MS);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkFwStatusInBootInternal() {
        Iterator<String> it = readFromFile(FW_NODE).iterator();
        if (it.hasNext()) {
            String str = it.next();
            Log.d(TAG, "firmware status in boot: " + str);
            if ("not_ready".equals(str)) {
                this.mFwNotReady = true;
                informWifiTurnOnResult(5);
                return;
            }
            this.mFwNotReady = false;
        }
    }

    private boolean isBootStage() {
        if (SystemClock.elapsedRealtime() < ((long) this.mWifiRomUpdateHelper.getIntegerValue("OPPO_BASIC_WIFI_SWITCH_LOG_BOOT_TIME_LIMIT", 120000).intValue())) {
            this.mIsInBootStage = true;
        } else {
            this.mIsInBootStage = false;
        }
        if (this.mIsInBootStage) {
            return true;
        }
        if (WIFI_FTM_ENABLED.equals(SystemProperties.get("sys.boot_completed"))) {
            this.mIsInBootStage = false;
        } else {
            this.mIsInBootStage = true;
        }
        return this.mIsInBootStage;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryToEnableAgain(int expectedState, int reason) {
        WifiController controller;
        this.mIsInBootStage = isBootStage();
        WifiInjector wifiInjector = this.mWifiInjector;
        if (wifiInjector == null || expectedState != 3 || this.mRetryCount > this.mMaxRetryLimit || !this.mIsInBootStage || (controller = wifiInjector.getWifiController()) == null) {
            reportWifiSwitchFoolProof();
            String pkg = this.mWifiSwitchCallerPkgs.poll();
            if (pkg == null) {
                pkg = UNKNOWN;
            }
            setStatistics(collectSysInfo(reason, pkg), EVENT_ID_WIFI_TURN_ON_FAILED);
            this.mIsRetry = false;
            return;
        }
        Log.d(TAG, "wifi open fail at boot stage ...but we want to try again");
        controller.sendMessage(155665);
        this.mIsRetry = true;
        this.mRetryCount++;
        monitorWifiSwitchIssue(true);
    }

    /* access modifiers changed from: protected */
    public void sendScanolyStateReCheck() {
        clearWifiScanonlyStateCheckMsg();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(8), 8000);
    }

    /* access modifiers changed from: protected */
    public void clearWifiScanonlyStateCheckMsg() {
        if (this.mHandler.hasMessages(8)) {
            this.mHandler.removeMessages(8);
        }
    }

    public void scanonlySuccessStatus() {
        clearWifiScanonlyStateCheckMsg();
        this.mDisableStateRetryCount = 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkScanOnlyModeAvailable() {
        return this.mSettingsStore.isScanAlwaysAvailable();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readFWStateAgain(HashMap<String, String> data) {
        this.mIsInBootStage = isBootStage();
        if (this.mDisableStateRetryCount > this.mMaxRetryLimit * 2 || !this.mIsInBootStage) {
            Log.d(TAG, "retry " + this.mDisableStateRetryCount + "times,still failed");
            setStatistics(data, EVENT_ID_WIFI_TURN_ON_FAILED);
            return;
        }
        checkFwStatusInBoot();
        this.mDisableStateRetryCount++;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean tryToStartScanonlyAgain() {
        if (this.mHandler.hasMessages(8)) {
            Log.e(TAG, "already have scan retry message count=" + this.mDisableStateRetryCount);
            return true;
        }
        this.mIsInBootStage = isBootStage();
        WifiInjector wifiInjector = this.mWifiInjector;
        if (wifiInjector == null || this.mDisableStateRetryCount > this.mMaxRetryLimit || !this.mIsInBootStage || wifiInjector.getWifiController() == null) {
            return false;
        }
        ActiveModeWarden activeModeWarden = this.mWifiInjector.getActiveModeWarden();
        Log.d(TAG, "wifi open fail at boot stage for scanonlymode failed  try again");
        activeModeWarden.enterScanOnlyMode();
        this.mDisableStateRetryCount++;
        sendScanolyStateReCheck();
        return true;
    }

    /* access modifiers changed from: protected */
    public void reportWifiSwitchFoolProof() {
        Log.d(TAG, "reporting wifi switch fool proof...");
        String ftm = SystemProperties.get(WIFI_FTM_PROP, WIFI_FTM_DISABLED);
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.cta.support") || WIFI_FTM_ENABLED.equals(ftm)) {
            Log.d(TAG, " CTA version or wifi ftm mode don't reportFoolProofException");
            return;
        }
        IllegalStateException exception = new IllegalStateException("Please send this log to Connectivity team ,thank you!");
        exception.fillInStackTrace();
        OppoAssertTip.getInstance().requestShowAssertMessage(Log.getStackTraceString(exception));
    }

    /* access modifiers changed from: protected */
    public void uploadLogByCellular() {
        if (DEBUG) {
            Log.d(TAG, "wifi broken, try to cp log to cellular path");
        }
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 5), this.mIsLogging ? 60000 : 0);
    }

    /* access modifiers changed from: protected */
    public void monitorWifiSwitchIssue(boolean enable) {
        if (WifiRomUpdateHelper.getInstance(this.mContext).getBooleanValue("OPPO_BASIC_WIFI_FRAMEWORK_CHECK_WIFI_SWITCH", false)) {
            clearWifiStateCheckMsg();
            int wifiState = WifiInjector.getInstance().getClientModeImpl().syncGetWifiState();
            int i = 3;
            if (!enable ? 1 != wifiState : 3 != wifiState) {
                Message msg = this.mHandler.obtainMessage(6);
                if (!enable) {
                    i = 1;
                }
                msg.arg1 = i;
                this.mHandler.sendMessageDelayed(msg, 8000);
                return;
            }
            Log.d(TAG, "state already match");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isStateMatch(int expectedState, int realState) {
        Log.d(TAG, "checking wifi state, expected state: " + expectedState + "real wifi state is " + realState);
        return realState == expectedState;
    }

    /* access modifiers changed from: protected */
    public void clearWifiStateCheckMsg() {
        if (this.mHandler.hasMessages(6)) {
            this.mHandler.removeMessages(6);
        }
    }

    private class WifiSwitchStatsBroadcastReceiver extends BroadcastReceiver {
        private WifiSwitchStatsBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(OppoWifiSwitchStats.ACTION_CLEAR_WIFI_SWITCH_LOG_COUNT)) {
                Log.d(OppoWifiSwitchStats.TAG, "receive reset action");
                OppoWifiSwitchStats.this.resetWifiSwitchLogCount();
            } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                int wifiState = intent.getIntExtra("wifi_state", 1);
                if (wifiState == 4) {
                    OppoWifiSwitchStats.this.informWifiTurnOnResult(2);
                } else if (wifiState == 3) {
                    OppoWifiSwitchStats.this.mFwNotReady = false;
                    OppoWifiSwitchStats.this.mRetryCount = 0;
                    OppoWifiSwitchStats.this.clearWifiStateCheckMsg();
                } else if (wifiState == 1) {
                    OppoWifiSwitchStats.this.clearWifiStateCheckMsg();
                }
            } else if (action.equals(OppoWifiSwitchStats.ACTION_WIFI_SWITCH_LOG_TESTING)) {
                if (OppoWifiSwitchStats.DEBUG) {
                    Log.d(OppoWifiSwitchStats.TAG, "manually collect log.");
                }
                OppoWifiSwitchStats.this.informWifiTurnOnResult(3);
            } else if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                if (OppoWifiSwitchStats.DEBUG) {
                    Log.d(OppoWifiSwitchStats.TAG, "Receiver device shutdown broadcast, cancel ongoing timer");
                }
                OppoWifiSwitchStats.this.clearWifiStateCheckMsg();
            }
        }
    }

    private class WifiSwitchStatsHandler extends Handler {
        public WifiSwitchStatsHandler(Looper looper) {
            super(looper);
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    int reason = msg.arg1;
                    HashMap<String, String> data = OppoWifiSwitchStats.this.collectSysInfo(reason, (String) msg.obj);
                    if (!OppoWifiSwitchStats.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (reason != 5 || OppoWifiSwitchStats.this.checkScanOnlyModeAvailable()) {
                            if (OppoWifiSwitchStats.this.checkScanOnlyModeAvailable() && !OppoWifiSwitchStats.this.tryToStartScanonlyAgain()) {
                                OppoWifiSwitchStats.this.setStatistics(data, OppoWifiSwitchStats.EVENT_ID_WIFI_TURN_ON_FAILED);
                                break;
                            }
                        } else {
                            OppoWifiSwitchStats.this.readFWStateAgain(data);
                            break;
                        }
                    } else if (!OppoWifiSwitchStats.this.mHandler.hasMessages(6)) {
                        OppoWifiSwitchStats.this.setStatistics(data, OppoWifiSwitchStats.EVENT_ID_WIFI_TURN_ON_FAILED);
                        break;
                    } else {
                        Log.d(OppoWifiSwitchStats.TAG, "renable was on going not need to collect status");
                        break;
                    }
                    break;
                case 1:
                    HashMap<String, String> succMap = new HashMap<>();
                    succMap.put("callerPkg", (String) msg.obj);
                    OppoWifiSwitchStats.this.setStatistics(succMap, OppoWifiSwitchStats.EVENT_ID_WIFI_TURN_ON_SUCCESS);
                    return;
                case 2:
                    break;
                case 3:
                    OppoWifiSwitchStats.this.triggerLogUpload();
                    return;
                case 4:
                    int reason2 = msg.arg1;
                    String svcStatus = SystemProperties.get("init.svc.collectWifiSwitchLog", OppoWifiSwitchStats.UNKNOWN);
                    Log.d(OppoWifiSwitchStats.TAG, "stop log, svcStatus: " + svcStatus);
                    SystemProperties.set(SupplicantStaIfaceHal.INIT_STOP_PROPERTY, "collectWifiSwitchLog");
                    OppoWifiSwitchStats.this.mIsLogging = false;
                    OppoWifiSwitchStats.this.mHandler.sendEmptyMessageDelayed(3, RttServiceImpl.HAL_RANGING_TIMEOUT_MS);
                    return;
                case 5:
                    SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, "mvWifiSwitchLog");
                    return;
                case 6:
                    int currentWifiState = WifiInjector.getInstance().getClientModeImpl().syncGetWifiState();
                    int expectedState = msg.arg1;
                    if (!OppoWifiSwitchStats.this.isStateMatch(expectedState, currentWifiState)) {
                        Log.d(OppoWifiSwitchStats.TAG, "state mismatch!");
                        if (expectedState == 1) {
                            OppoWifiSwitchStats.this.informWifiTurnOffResult(6);
                        }
                        OppoWifiSwitchStats.this.tryToEnableAgain(expectedState, 2);
                        return;
                    }
                    Log.d(OppoWifiSwitchStats.TAG, "state match!");
                    if (OppoWifiSwitchStats.this.mIsRetry) {
                        String pkg = (String) OppoWifiSwitchStats.this.mWifiSwitchCallerPkgs.poll();
                        if (pkg == null) {
                            pkg = OppoWifiSwitchStats.UNKNOWN;
                        }
                        HashMap<String, String> succMaps = new HashMap<>();
                        succMaps.put("callerPkg", pkg);
                        succMaps.put("isRetryed", "yes");
                        OppoWifiSwitchStats.this.mIsRetry = false;
                        OppoWifiSwitchStats.this.setStatistics(succMaps, OppoWifiSwitchStats.EVENT_ID_WIFI_TURN_ON_SUCCESS);
                        return;
                    }
                    return;
                case 7:
                    OppoWifiSwitchStats.this.checkFwStatusInBootInternal();
                    return;
                case 8:
                    OppoWifiSwitchStats.this.tryToStartScanonlyAgain();
                    return;
                case 9:
                default:
                    if (OppoWifiSwitchStats.DEBUG) {
                        Log.d(OppoWifiSwitchStats.TAG, "ignored unknown msg: " + msg.what);
                        return;
                    }
                    return;
                case 10:
                    return;
            }
            if (OppoWifiSwitchStats.this.mCanCollectLog) {
                OppoWifiSwitchStats.this.tryToCollectLog(msg.arg1);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setStatistics(HashMap<String, String> data, String eventId) {
        if (DEBUG) {
            Log.d(TAG, "fool-proof, onCommon eventId == " + eventId);
        }
        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", eventId, data, false);
    }

    private String getImei() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager == null) {
            return "null";
        }
        String imei = telephonyManager.getImei();
        if (!TextUtils.isEmpty(imei)) {
            return imei;
        }
        return "null";
    }

    private boolean isSerialNumAccepted() {
        String serialNumber = SystemProperties.get("ro.vold.serialno", "not_found");
        int stage = 1;
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            stage = wifiRomUpdateHelper.getIntegerValue("OPPO_BASIC_WIFI_SWITCH_LOG_STAGE", 1).intValue();
        }
        Log.d(TAG, "current stage: " + stage);
        if (stage != 1) {
            if (stage != 2) {
                if (stage != 3) {
                    if (stage != 4) {
                        return false;
                    }
                    return true;
                } else if (checkEnddingCharacter(serialNumber, "12345678acbf")) {
                    return true;
                } else {
                    return false;
                }
            } else if (checkEnddingCharacter(serialNumber, "3468cf")) {
                return true;
            } else {
                return false;
            }
        } else if (checkEnddingCharacter(serialNumber, "84f2")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkEnddingCharacter(String serialNumber, String includeEnd) {
        if (includeEnd == null || serialNumber == null) {
            Log.e(TAG, "a null object, cannot judge!");
            return false;
        }
        String[] characters = includeEnd.split("|");
        for (String s : characters) {
            if (!s.equals("") && serialNumber.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private String getWlanDriverStatus() {
        ArrayList<String> modules = readFromFile(MODULE_FILE);
        if (modules == null || modules.size() == 0) {
            return UNKNOWN;
        }
        Iterator<String> it = modules.iterator();
        while (it.hasNext()) {
            String singleModules = it.next();
            if (singleModules != null && singleModules.contains(WLAN_MODULE)) {
                return DRIVER_LOADED;
            }
        }
        return DRIVER_UNLOADED;
    }

    private String pkgNameToAppLabel(String pkgName) {
        Log.d(TAG, "pkgName = " + pkgName);
        PackageManager mPackageManager = this.mContext.getPackageManager();
        try {
            ApplicationInfo applicationInfo = mPackageManager.getApplicationInfo(pkgName, 0);
            if (applicationInfo != null) {
                return mPackageManager.getApplicationLabel(applicationInfo).toString();
            }
            return pkgName;
        } catch (PackageManager.NameNotFoundException e) {
            return pkgName;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private HashMap<String, String> collectSysInfo(int reason, String pkg) {
        String str;
        int i;
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> stampMap = new HashMap<>();
        map.put("reason", reasonToString(reason));
        String packageName = pkgNameToAppLabel(pkg);
        map.put("callerPkg", packageName);
        stampMap.put("callerPkg", packageName);
        String driverLoadStatus = getWlanDriverStatus();
        map.put("driver_status", driverLoadStatus);
        stampMap.put("driver_status", driverLoadStatus);
        String insmod_result = SystemProperties.get("oppo.wifi.driver.insmod", "property_not_found");
        map.put("insmod_result", insmod_result);
        stampMap.put("insmod_result", insmod_result);
        map.put("ota_version", SystemProperties.get("ro.build.version.ota", "property_not_found"));
        Iterator<String> it = readFromFile("/proc/version", 1).iterator();
        if (it.hasNext()) {
            map.put("linux_version", it.next());
        }
        File ini = new File("/mnt/vendor/persist/WCNSS_qcom_cfg.ini");
        int isIniWriteable = 0;
        if (!ini.exists()) {
            Log.e(TAG, "ini file not exists!");
            isIniWriteable = 1;
        } else if (ini.canWrite()) {
            isIniWriteable = 2;
        }
        String str2 = "file_not_found";
        if (isIniWriteable == 0) {
            str = "true";
        } else {
            str = isIniWriteable == 1 ? str2 : "false";
        }
        map.put("persist_ro", str);
        if (isIniWriteable == 0) {
            str2 = "true";
        } else if (isIniWriteable != 1) {
            str2 = "false";
        }
        stampMap.put("persist_ro", str2);
        Iterator<String> it2 = readFromFile(FW_NODE).iterator();
        if (it2.hasNext()) {
            String str3 = it2.next();
            map.put("firmware_status", str3);
            stampMap.put("firmware_status", str3);
            if ("not_ready".equals(str3)) {
                i = 1;
                this.mFwNotReady = true;
            } else {
                i = 1;
                this.mFwNotReady = false;
            }
        } else {
            i = 1;
        }
        if (reason == i) {
            Log.d(TAG, "service check collecting extra wc and wsm state");
            map.put("WC_State", this.mWifiInjector.getWifiController().getCurrentState().getName());
            map.put("WSM_State", this.mWifiInjector.getClientModeImpl().getCurrentState().getName());
        }
        this.mCanCollectLog = canCollectLog(reason == 3);
        if (this.mCanCollectLog) {
            if (!this.mIsLogging) {
                String uuid = generateUUID();
                map.put("log_fid", uuid);
                SystemProperties.set("oppo.wifi.switch.log.fid", uuid);
            } else {
                Log.e(TAG, "logging is running, do not override the fid");
            }
        }
        long bootTime = SystemClock.elapsedRealtime();
        this.mIsInBootStage = isBootStage();
        if (this.mIsRetry) {
            map.put("isRetryed", "yes");
        } else {
            map.put("isRetryed", "no");
        }
        map.put("boot_up_time", new Long(bootTime).toString());
        map.put("is_in_boot_stage", new Boolean(this.mIsInBootStage).toString());
        stampMap.put("boot_up_time", new Long(bootTime).toString());
        stampMap.put("issue_time", new Long(System.currentTimeMillis()).toString());
        OppoManager.onStamp(EVENT_ID_WIFI_SWITCH_STAMP, stampMap);
        return map;
    }

    private String generateUUID() {
        String fid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        if (DEBUG) {
            Log.d(TAG, "log fid is: " + fid);
        }
        return fid;
    }

    private boolean isWifiSwitchLogCollectEnable() {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getBooleanValue("OPPO_BASIC_WIFI_SWITCH_LOG_COLLECT_ENABLED", false);
        }
        return false;
    }

    private boolean canCollectLog(boolean isTesting) {
        if (isTesting) {
            return true;
        }
        if (isWifiSwitchLogCollectEnable() && isSerialNumAccepted() && !reachDailyLimits()) {
            return true;
        }
        Log.d(TAG, "do not collect log.");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryToCollectLog(int reason) {
        if (this.mIsLogging) {
            Log.e(TAG, "log is running, aborted...");
            return;
        }
        String ftm = SystemProperties.get(WIFI_FTM_PROP, WIFI_FTM_DISABLED);
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.cta.support") || WIFI_FTM_ENABLED.equals(ftm)) {
            Log.d(TAG, " CTA version or wifi ftm mode don't collect wifi switch log");
            return;
        }
        SystemProperties.set("oppo.wifi.switch.log.reason", reasonToString(reason));
        SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, "collectWifiSwitchLog");
        this.mIsLogging = true;
        updateDailyLimits();
        Message msg = Message.obtain(this.mHandler, 4);
        msg.arg1 = reason;
        this.mHandler.sendMessageDelayed(msg, 40000);
    }

    private boolean reachDailyLimits() {
        int dailyAvailableCount = 1;
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            dailyAvailableCount = wifiRomUpdateHelper.getIntegerValue("OPPO_BASIC_WIFI_SWITCH_LOG_LIMIT", 1).intValue();
        }
        int currentCount = Settings.System.getInt(this.mContext.getContentResolver(), WIFI_SWITCH_LOG_COUNT_VALUE, 0);
        if (DEBUG) {
            Log.d(TAG, "currentCount: " + currentCount + " dailyAvailableCount: " + dailyAvailableCount);
        }
        if (currentCount < dailyAvailableCount) {
            return false;
        }
        Log.e(TAG, "reach daily limits, cannot generate new log");
        return true;
    }

    private void updateDailyLimits() {
        int dailyAvailableCount = 1;
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            dailyAvailableCount = wifiRomUpdateHelper.getIntegerValue("OPPO_BASIC_WIFI_SWITCH_LOG_LIMIT", 1).intValue();
        }
        int currentCount = Settings.System.getInt(this.mContext.getContentResolver(), WIFI_SWITCH_LOG_COUNT_VALUE, 0);
        Log.d(TAG, "updateDailyLimits currentCount: " + currentCount + " dailyAvailableCount: " + dailyAvailableCount);
        int currentCount2 = currentCount + 1;
        if (!Settings.System.putInt(this.mContext.getContentResolver(), WIFI_SWITCH_LOG_COUNT_VALUE, currentCount2)) {
            Log.e(TAG, "failed to put value to settings!");
        }
        if (currentCount2 == dailyAvailableCount) {
            startClearWifiLogCountAlarm();
        }
    }

    private void startCaptureLogs(String reason, int sequence) {
        Intent intent = new Intent("oppo.intent.log.customer");
        intent.addFlags(536870912);
        intent.putExtra("logtype", 63);
        intent.putExtra("duration", LOG_CAPTURE_DURATION);
        intent.putExtra("name", reason);
        intent.putExtra("sequence", sequence);
        intent.addFlags(16777216);
        intent.setPackage("com.oppo.logkit");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        Log.d(TAG, "startCaptureLogs reason=" + reason + " seq=" + sequence);
    }

    private void retainLogs(int sequence) {
        Intent intent = new Intent("oppo.intent.log.customer.retain");
        intent.addFlags(536870912);
        intent.putExtra("sequence", sequence);
        intent.addFlags(16777216);
        intent.setPackage("com.oppo.logkit");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        Log.d(TAG, "retainLogs seq=" + sequence);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void triggerLogUpload() {
        Log.d(TAG, "trigger log upload");
        SystemProperties.set("sys.oppo.wifi.switch.log.stop", WIFI_FTM_ENABLED);
    }

    private String reasonToString(int reasonInt) {
        if (reasonInt == 1) {
            return new String("wifi_service_check");
        }
        if (reasonInt == 2) {
            return new String("wifi_state_unknown");
        }
        if (reasonInt == 3) {
            return new String("wifi_manual_trigger");
        }
        if (reasonInt == 4) {
            return new String("wifi_scanonly_trigger");
        }
        if (reasonInt != 6) {
            return new String("default");
        }
        return new String("wifi_turn_off_failed");
    }

    private void startClearWifiLogCountAlarm() {
        if (DEBUG) {
            Log.d(TAG, "start clear wifi switch log count");
        }
        this.mClearWifiLogCountIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_CLEAR_WIFI_SWITCH_LOG_COUNT, (Uri) null), IWifiChip.ChipCapabilityMask.DUAL_BAND_DUAL_CHANNEL);
        ((AlarmManager) this.mContext.getSystemService("alarm")).set(0, System.currentTimeMillis() + 72000000, this.mClearWifiLogCountIntent);
    }

    /* access modifiers changed from: protected */
    public void resetWifiSwitchLogCount() {
        Settings.System.putInt(this.mContext.getContentResolver(), WIFI_SWITCH_LOG_COUNT_VALUE, 0);
    }

    private ArrayList<String> readFromFile(String filePath) {
        return readFromFile(filePath, 0);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r4.close();
     */
    private ArrayList<String> readFromFile(String filePath, int size) {
        FileInputStream fis = null;
        ArrayList<String> retArr = new ArrayList<>();
        int lines_count = 0;
        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            Log.e(TAG, "file " + file + " does not exist or cannot be read");
            retArr.add("file_not_found");
            return retArr;
        }
        try {
            FileInputStream fis2 = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fis2)));
            while (true) {
                String line = br.readLine();
                if (line != null && line.length() != 0) {
                    if (size != 0 && lines_count >= size) {
                        Log.d(TAG, "reach size: " + size + " lines_count: " + lines_count);
                        break;
                    }
                    if (DEBUG) {
                        Log.d(TAG, "readline: " + line);
                    }
                    retArr.add(line);
                    lines_count++;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            if (0 != 0) {
                fis.close();
            }
        } catch (Exception e) {
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e2) {
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        return retArr;
    }
}

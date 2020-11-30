package com.android.server.wifi.scanner;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiRomUpdateHelper;
import android.net.wifi.p2p.IWifiP2pManager;
import android.os.Handler;
import android.os.OppoAssertTip;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.android.server.wifi.Clock;
import com.android.server.wifi.WifiServiceImpl;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.rtt.RttServiceImpl;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import oppo.util.OppoStatistics;

public class OppoScanFoolProof {
    private static final int DELAY = 5000;
    private static final String MAP_KEY = "mapKey-";
    private static final int SCAN_DELTA = 15000;
    private static final int SCAN_RESULT_TIMEOUT_DELTA = 20000;
    private static final int SCAN_RESULT_TIMEOUT_REPORT = 30000;
    public static final String SCAN_RESULT_TIMEOUT_TAG = "Fp Scan result timer";
    private static final int SECOND_DELTA = 180;
    private static final String TAG = "OppoScanFoolProof";
    private static OppoScanFoolProof sInstance;
    protected final AlarmManager mAlarmManager;
    private OppoAssertTip mAssertProxy = null;
    private final BroadcastReceiver mBroadcastReciever = new BroadcastReceiver() {
        /* class com.android.server.wifi.scanner.OppoScanFoolProof.AnonymousClass3 */

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(OppoScanFoolProof.TAG, "intent is null");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                Log.e(OppoScanFoolProof.TAG, "action is null");
            } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                NetworkInfo.DetailedState state = ((NetworkInfo) intent.getParcelableExtra("networkInfo")).getDetailedState();
                OppoScanFoolProof.this.mIsConnecting = OppoScanFoolProof.isConnectingState(state);
            } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                if (intent.getIntExtra("wifi_state", 4) == 3) {
                    OppoScanFoolProof.this.resetSFState();
                }
            } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                if (intent.getBooleanExtra("state", false)) {
                    OppoScanFoolProof.this.logd("am 1.");
                    OppoScanFoolProof.this.resetSFState();
                }
            } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED") && intent.getIntExtra("wifi_state", 11) == 13) {
                OppoScanFoolProof.this.resetSFState();
            }
        }
    };
    private Clock mClock;
    private Context mContext;
    private boolean mDbg = false;
    private boolean mDisableRejectRestore = false;
    protected final Handler mEventHandler;
    private long mFirstRejectTime = 0;
    private Object mFpsLock = new Object();
    private boolean mIsConnecting = false;
    private int mIsWifiScanAlwaysOn = 1;
    private long mLastRejectTime = 0;
    private boolean mRestartForScanReject = false;
    AlarmManager.OnAlarmListener mRestartWifiListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.wifi.scanner.OppoScanFoolProof.AnonymousClass2 */

        public void onAlarm() {
            synchronized (OppoScanFoolProof.this.mFpsLock) {
                OppoScanFoolProof.this.restartWifi();
            }
        }
    };
    private int mScanCmdSetDownCount = 0;
    private int mScanRejectCount = 0;
    private int mScanResultTimeOutCnt = 0;
    AlarmManager.OnAlarmListener mScanResultTimeoutListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.wifi.scanner.OppoScanFoolProof.AnonymousClass1 */

        public void onAlarm() {
            synchronized (OppoScanFoolProof.this.mFpsLock) {
                OppoScanFoolProof.this.scanResultTimeOut();
            }
        }
    };
    private int mVerboseLoggingLevel = 0;
    private WifiManager mWifiManager;
    private WifiP2pServiceImpl mWifiP2pServiceImpl;
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetSFState() {
        logd("resetSFState");
        this.mScanCmdSetDownCount = 0;
        this.mScanResultTimeOutCnt = 0;
        this.mScanRejectCount = 0;
        this.mFirstRejectTime = 0;
        AlarmManager alarmManager = this.mAlarmManager;
        if (alarmManager != null) {
            alarmManager.cancel(this.mScanResultTimeoutListener);
        }
    }

    public OppoScanFoolProof(Handler handler, Clock clock, Context context) {
        this.mContext = context;
        this.mWifiP2pServiceImpl = IWifiP2pManager.Stub.asInterface(ServiceManager.getService("wifip2p"));
        this.mVerboseLoggingLevel = SystemProperties.getBoolean(WifiServiceImpl.DEBUG_PROPERTY, false) ? 1 : 0;
        this.mEventHandler = handler;
        this.mClock = clock;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            wifiRomUpdateHelper.enableVerboseLogging(getVerboseLoggingLevel());
        }
        this.mIsConnecting = false;
        this.mAssertProxy = OppoAssertTip.getInstance();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        this.mContext.registerReceiver(this.mBroadcastReciever, intentFilter, null, this.mEventHandler);
        enableVerboseLogging(this.mVerboseLoggingLevel);
    }

    public static synchronized OppoScanFoolProof getInstance(Handler handler, Clock clock, Context context) {
        OppoScanFoolProof oppoScanFoolProof;
        synchronized (OppoScanFoolProof.class) {
            if (sInstance == null) {
                synchronized (OppoScanFoolProof.class) {
                    if (sInstance == null) {
                        sInstance = new OppoScanFoolProof(handler, clock, context);
                    }
                }
            }
            oppoScanFoolProof = sInstance;
        }
        return oppoScanFoolProof;
    }

    /* access modifiers changed from: package-private */
    public int getVerboseLoggingLevel() {
        this.mVerboseLoggingLevel = SystemProperties.getBoolean(WifiServiceImpl.DEBUG_PROPERTY, false) ? 1 : 0;
        return this.mVerboseLoggingLevel;
    }

    /* access modifiers changed from: package-private */
    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mDbg = true;
        } else {
            this.mDbg = false;
        }
    }

    public String getRomUpdateValue(String key, String defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Double getRomUpdateFloatValue(String key, Double defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getFloatValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Long getRomUpdateLongValue(String key, Long defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getLongValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Boolean getRomUpdateBooleanValue(String key, Boolean defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return Boolean.valueOf(wifiRomUpdateHelper.getBooleanValue(key, defaultVal.booleanValue()));
        }
        return defaultVal;
    }

    public void setStatistics(String mapValue, String eventId) {
        HashMap<String, String> map = new HashMap<>();
        map.put(MAP_KEY, mapValue);
        Log.d(TAG, " onCommon eventId = " + eventId);
        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", eventId, map, false);
    }

    public static boolean isConnectingState(NetworkInfo.DetailedState state) {
        if (state != NetworkInfo.DetailedState.CONNECTING && state != NetworkInfo.DetailedState.AUTHENTICATING && state != NetworkInfo.DetailedState.DISCONNECTING && state != NetworkInfo.DetailedState.OBTAINING_IPADDR && state != NetworkInfo.DetailedState.BLOCKED && state != NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK && state != NetworkInfo.DetailedState.VERIFYING_POOR_LINK && state != NetworkInfo.DetailedState.CONNECTED) {
            return false;
        }
        Log.d(TAG, "isConnectingState DetailedState is " + state);
        return true;
    }

    private boolean isHandshakeState() {
        return this.mIsConnecting;
    }

    public void scanResultGot() {
        logd("scanResultGot, count=" + this.mScanCmdSetDownCount);
        if (this.mScanCmdSetDownCount > 0) {
            this.mScanCmdSetDownCount = 0;
            this.mScanResultTimeOutCnt = 0;
            this.mAlarmManager.cancel(this.mScanResultTimeoutListener);
        }
        this.mScanRejectCount = 0;
    }

    public void scanResultFail() {
        logd("scanResultFail");
        this.mScanResultTimeOutCnt = 0;
        this.mAlarmManager.cancel(this.mScanResultTimeoutListener);
    }

    public void scanCmdSetSuccess() {
        logd("scanCmdSetSuccess count=" + this.mScanCmdSetDownCount);
        this.mScanCmdSetDownCount = this.mScanCmdSetDownCount + 1;
        if (this.mScanCmdSetDownCount == 1) {
            this.mAlarmManager.set(2, 30000 + this.mClock.getElapsedSinceBootMillis(), SCAN_RESULT_TIMEOUT_TAG, this.mScanResultTimeoutListener, this.mEventHandler);
        }
        this.mScanRejectCount = 0;
        this.mFirstRejectTime = 0;
        if (this.mRestartForScanReject) {
            Log.d(TAG, "has restarted for scan rejection");
            this.mDisableRejectRestore = false;
            this.mRestartForScanReject = false;
            setStatistics("reject", "wifi_scan_reject_restored");
        }
    }

    public void scanResultTimeOut() {
        int romUpdateSCSDC = getRomUpdateIntegerValue("BASIC_SCAN_CMD_DOWN_COUNT", 6).intValue();
        if (this.mScanCmdSetDownCount < romUpdateSCSDC || this.mWifiP2pServiceImpl.isP2pGroupNegotiationState()) {
            int i = this.mScanResultTimeOutCnt;
            if (i < romUpdateSCSDC) {
                this.mScanResultTimeOutCnt = i + 1;
                this.mAlarmManager.set(2, (this.mClock.getElapsedSinceBootMillis() + 30000) - 20000, SCAN_RESULT_TIMEOUT_TAG, this.mScanResultTimeoutListener, this.mEventHandler);
                return;
            }
            return;
        }
        setStatistics("unreport", "unreport_scan_results");
        Log.d(TAG, "Supplicant did not report scan results after " + this.mScanCmdSetDownCount + " SCAN cmds and 30+10*n secs");
        this.mScanCmdSetDownCount = 0;
        this.mScanResultTimeOutCnt = 0;
        reportFoolProofException();
        sheduleRestartWifi();
    }

    public void scanCmdRejcet() {
        logd("scanCmdRejcet mDisableRejectRestore=" + this.mDisableRejectRestore);
        if (!this.mDisableRejectRestore) {
            if (Settings.System.getInt(this.mContext.getContentResolver(), "vendor.wifi.ftm", 0) == 1) {
                logd("in ftm mode!!");
                return;
            }
            long now = System.currentTimeMillis();
            int i = this.mScanRejectCount;
            if (i == 0) {
                this.mScanRejectCount = i + 1;
                this.mFirstRejectTime = now;
                this.mLastRejectTime = now;
            }
            if (now - this.mLastRejectTime >= 15000 && !isHandshakeState() && !isInEngineermode()) {
                this.mScanRejectCount++;
                this.mLastRejectTime = now;
                int secs = ((int) (now - this.mFirstRejectTime)) / 1000;
                int romUpdateSRC = getRomUpdateIntegerValue("BASIC_SCAN_REJECT_COUNT", 6).intValue();
                logd("fool-proof,mScanRejectCount = " + this.mScanRejectCount + " reject last " + secs + "romUpdateSRC = " + romUpdateSRC);
                if (this.mScanRejectCount > romUpdateSRC && secs > SECOND_DELTA) {
                    Log.d(TAG, "fool-proof,Scan rejection >6 times and last >3 mins,maybe something bad with wpa or driver happened,restore wifi!");
                    if (this.mRestartForScanReject || isGameMode()) {
                        this.mDisableRejectRestore = true;
                        setStatistics("reject", "wifi_scan_reject_restore_failed");
                    } else {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("last_time", Integer.toString(secs));
                        map.put("reject_count", Integer.toString(this.mScanRejectCount));
                        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", "wifi_scan_reject_restore_going", map, false);
                        Log.d(TAG, " wifi_scan_reject_restore_going..");
                        reportFoolProofException();
                        sheduleRestartWifi();
                        this.mRestartForScanReject = true;
                    }
                    this.mScanRejectCount = 0;
                    this.mFirstRejectTime = 0;
                }
            }
        }
    }

    public boolean isInEngineermode() {
        return SystemProperties.getBoolean("sys.eng.mod", false);
    }

    public boolean isGameMode() {
        int gas = 0;
        FileInputStream inputStream = null;
        BufferedReader reader = null;
        try {
            FileInputStream inputStream2 = new FileInputStream("/d/ged/hal/event_notify");
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(inputStream2));
            reader2.readLine();
            while (true) {
                String gasString = reader2.readLine();
                if (gasString != null && gasString.length() != 0) {
                    if (gasString.startsWith("GAS:")) {
                        String gasFlg = gasString.substring("GAS:".length()).trim();
                        Log.d(TAG, "getGasFlag gasFlg " + gasFlg + " gasFlg length " + gasFlg.length());
                        gas = Integer.parseInt(gasFlg);
                        break;
                    }
                }
            }
            try {
                reader2.close();
            } catch (Exception e) {
            }
            try {
                inputStream2.close();
                break;
            } catch (Exception e2) {
            }
        } catch (FileNotFoundException notFound) {
            notFound.printStackTrace();
            if (0 != 0) {
                try {
                    reader.close();
                } catch (Exception e3) {
                }
            }
            if (0 != 0) {
                inputStream.close();
            }
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
            if (0 != 0) {
                try {
                    reader.close();
                } catch (Exception e4) {
                }
            }
            if (0 != 0) {
                inputStream.close();
            }
        } catch (NumberFormatException foramtException) {
            foramtException.printStackTrace();
            if (0 != 0) {
                try {
                    reader.close();
                } catch (Exception e5) {
                }
            }
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (Exception e6) {
                }
            }
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (Exception e7) {
                }
            }
            throw th;
        }
        if (gas == 1) {
            return true;
        }
        return false;
    }

    public void reportFoolProofException() {
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.cta.support")) {
            Log.d(TAG, " CTA version don't reportFoolProofException");
            return;
        }
        if (getVerboseLoggingLevel() == 0) {
            enableVerboseLogging(1);
        }
        RuntimeException excp = new RuntimeException("Please send this log to Connectivity team ,thank you!");
        excp.fillInStackTrace();
        this.mAssertProxy.requestShowAssertMessage(Log.getStackTraceString(excp));
    }

    public void handleScanFailEvent(WifiManager wifiManager) {
        SupplicantState supplicantState = SupplicantState.DISCONNECTED;
        WifiInfo wifiInfo = null;
        if (wifiManager != null) {
            wifiInfo = wifiManager.getConnectionInfo();
        }
        if (wifiInfo != null) {
            supplicantState = wifiInfo.getSupplicantState();
        }
        if (!SupplicantState.isHandshakeState(supplicantState)) {
            scanCmdRejcet();
        }
    }

    private void sheduleRestartWifi() {
        if (getRomUpdateIntegerValue("BASIC_FOOL_PROOF_ON", 1).intValue() != 1) {
            Log.d(TAG, "foolProofOn != 1, don't restart!");
            return;
        }
        this.mIsWifiScanAlwaysOn = getPersistedScanAlwaysAvailable();
        setPersistedScanAlwaysAvailable(0);
        this.mWifiManager.setWifiEnabled(false);
        this.mAlarmManager.set(2, RttServiceImpl.HAL_RANGING_TIMEOUT_MS + this.mClock.getElapsedSinceBootMillis(), "Scan Fool-Proof restart", this.mRestartWifiListener, this.mEventHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restartWifi() {
        setPersistedScanAlwaysAvailable(this.mIsWifiScanAlwaysOn);
        this.mWifiManager.setWifiEnabled(true);
    }

    private int getPersistedScanAlwaysAvailable() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0);
    }

    private void setPersistedScanAlwaysAvailable(int value) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", value);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String msg) {
        if (this.mDbg) {
            Log.d(TAG, "" + msg);
        }
    }
}

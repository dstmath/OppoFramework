package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiRomUpdateHelper;
import android.net.wifi.p2p.WifiP2pGroup;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.wifi.rtt.RttServiceImpl;
import java.util.HashMap;
import java.util.UUID;
import oppo.util.OppoStatistics;

public class OppoWifiP2pStats {
    public static final String IPC_PROVISIONING_FAILURE_STATS = "IPC_PROVISIONING_FAILURE";
    private static final int LOG_CAPTURE_DURATION = 40000;
    private static final int MSG_STOP_LOGGING = 4;
    private static final int MSG_TRIGGER_LOG_UPLOAD = 3;
    public static final String P2P_GO_NEGOTIATION_FAILURE_STATS = "P2P_GO_NEGOTIATION_FAILURE_EVENT";
    public static final String P2P_GROUP_CREATED_SUCCESS_STATS = "P2P_GROUP_CREATED_SUCCESS_EVENT";
    public static final String P2P_GROUP_FORMATION_FAILURE_STATS = "P2P_GROUP_FORMATION_FAILURE_EVENT";
    public static final String P2P_PROV_DISC_FAILURE_STATS = "P2P_PROV_DISC_FAILURE_EVENT";
    private static final String TAG = "OppoWifiP2pStats";
    private static final int WAIT_FOR_LOG_STOP_INTERVAL = 5000;
    private boolean DEBUG = true;
    private final String OSHARE_CONNECT_FAIL_DCS = "/data/oppo/log/DCS/de/network_logs/oshare_connect_fail";
    private final String WFD_CONNECT_FAIL_BOARDCAST = "oppo.intent.state.wfdconnectfail";
    private final String WFD_CONNECT_FAIL_DCS = "/data/oppo/log/DCS/de/network_logs/wfd_connect_fail";
    private final String WFD_CONNECT_FAIL_EXTRA = "wfd_connect_fail_extra";
    private Context mContext;
    private Handler mHandler;
    private boolean mIsLogging = false;
    private boolean mIsOshareEnable = false;
    private Looper mLooper;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.OppoWifiP2pStats.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("oppo.intent.state.wfdconnectfail")) {
                OppoWifiP2pStats.this.mWfdConnectfailReasion = (String) intent.getExtra("wfd_connect_fail_extra", null);
            }
            if (OppoWifiP2pStats.this.mWfdConnectfailReasion == null) {
                OppoWifiP2pStats.this.mWfdConnectfailReasion = "WFD_FAIL_CONNECT_UNKOWN";
            }
            Log.e(OppoWifiP2pStats.TAG, "receive wfd connecr fail broadcast reason = " + OppoWifiP2pStats.this.mWfdConnectfailReasion);
            if (OppoWifiP2pStats.this.canCollectWfdLog()) {
                SystemProperties.set("oppo.wifip2p.connectfail", "/data/oppo/log/DCS/de/network_logs/wfd_connect_fail");
                SystemProperties.set("oppo.wifi.p2p.log.fid", OppoWifiP2pStats.this.generateUUID());
                OppoWifiP2pStats oppoWifiP2pStats = OppoWifiP2pStats.this;
                oppoWifiP2pStats.tryToCollectLog(oppoWifiP2pStats.mWfdConnectfailReasion);
            }
        }
    };
    private String mWfdConnectfailReasion = null;
    private WifiInjector mWifiInjector;
    private WifiManager mWifiManager;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;

    private void registerForBroadcasts() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.state.wfdconnectfail");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private WifiManager getWifiManager() {
        return (WifiManager) this.mContext.getSystemService("wifi");
    }

    public OppoWifiP2pStats(Context context, Looper looper) {
        this.mContext = context;
        this.mLooper = looper;
        this.mHandler = new WifiP2pStatsHandler(this.mLooper);
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        registerForBroadcasts();
        this.mWifiManager = getWifiManager();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean canCollectWfdLog() {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getBooleanValue("OPPO_BASIC_WIFI_P2P_WFD_LOG_COLLECT_ENBALE", false);
        }
        return false;
    }

    private void setP2pStats(HashMap<String, String> data, String eventId) {
        Log.d(TAG, "fool-proof, onCommon eventId == " + eventId);
        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", eventId, data, false);
    }

    public void setP2pSuccessStats(WifiP2pGroup group) {
        HashMap<String, String> map = new HashMap<>();
        if (group != null) {
            map.put("GO", Boolean.toString(group.isGroupOwner()));
            map.put("freq", String.valueOf(group.getFrequency()));
            map.put("GO-name", group.getNetworkName());
            map.put("IS-OSHARE", Boolean.toString(isOshareEnable()));
        }
        setP2pStats(map, P2P_GROUP_CREATED_SUCCESS_STATS);
    }

    public void setP2pFailStats(WifiP2pGroup group, String eventId) {
        HashMap<String, String> map = new HashMap<>();
        if (group != null) {
            map.put("GO", Boolean.toString(group.isGroupOwner()));
            map.put("freq", String.valueOf(group.getFrequency()));
            map.put("GO-name", group.getNetworkName());
            map.put("IS-OSHARE", Boolean.toString(isOshareEnable()));
        }
        if (needCollectOshareLog()) {
            SystemProperties.set("oppo.wifip2p.connectfail", "/data/oppo/log/DCS/de/network_logs/oshare_connect_fail");
            SystemProperties.set("oppo.wifi.p2p.log.fid", generateUUID());
            tryToCollectLog(eventId);
        }
        setP2pStats(map, eventId);
    }

    private void startCaptureLogs(String reason, int sequence) {
        Intent intent = new Intent("oppo.intent.log.customer");
        intent.addFlags(536870912);
        intent.putExtra("logtype", 63);
        intent.putExtra("duration", LOG_CAPTURE_DURATION);
        intent.putExtra("name", reason);
        intent.putExtra("sequence", sequence);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        Log.d(TAG, "startCaptureLogs reason=" + reason + " seq=" + sequence);
    }

    private void retainLogs(int sequence) {
        Intent intent = new Intent("oppo.intent.log.customer.retain");
        intent.addFlags(536870912);
        intent.putExtra("sequence", sequence);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        Log.d(TAG, "retainLogs seq=" + sequence);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryToCollectLog(String reason) {
        if (this.mIsLogging) {
            Log.e(TAG, "log is running, aborted...");
            return;
        }
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager != null) {
            wifiManager.enableVerboseLogging(1);
        }
        SystemProperties.set("oppo.wifi.p2p.log.reason", reason);
        SystemProperties.set(SupplicantStaIfaceHal.INIT_START_PROPERTY, "collectWifiP2pLog");
        this.mIsLogging = true;
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 4), 40000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void triggerLogUpload() {
        Log.d(TAG, "trigger log upload");
        SystemProperties.set("sys.oppo.wifi.p2p.log.stop", "1");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String generateUUID() {
        String fid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        if (this.DEBUG) {
            Log.d(TAG, "log fid is: " + fid);
        }
        return fid;
    }

    public void setOshareEnable(boolean enable) {
        this.mIsOshareEnable = enable;
    }

    private boolean isOshareEnable() {
        return this.mIsOshareEnable;
    }

    private boolean needCollectOshareLog() {
        boolean isRomupdateEnable = false;
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            isRomupdateEnable = wifiRomUpdateHelper.getBooleanValue("OPPO_BASIC_WIFI_P2P_OSHARE_LOG_COLLECT_ENBALE", false);
        }
        if (!this.mIsOshareEnable || !isRomupdateEnable) {
            return false;
        }
        return true;
    }

    private class WifiP2pStatsHandler extends Handler {
        public WifiP2pStatsHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i == 3) {
                OppoWifiP2pStats.this.triggerLogUpload();
            } else if (i == 4) {
                SystemProperties.set(SupplicantStaIfaceHal.INIT_STOP_PROPERTY, "collectWifiP2pLog");
                OppoWifiP2pStats.this.mIsLogging = false;
                if (OppoWifiP2pStats.this.mWifiManager != null) {
                    OppoWifiP2pStats.this.mWifiManager.enableVerboseLogging(0);
                }
                OppoWifiP2pStats.this.mHandler.sendEmptyMessageDelayed(3, RttServiceImpl.HAL_RANGING_TIMEOUT_MS);
            } else if (OppoWifiP2pStats.this.DEBUG) {
                Log.d(OppoWifiP2pStats.TAG, "ignored unknown msg: " + msg.what);
            }
        }
    }
}

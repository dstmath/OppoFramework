package com.android.server.connectivity.networkrecovery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.connectivity.DnsManager;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.usage.UnixCalendar;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import oppo.util.OppoStatistics;

public class OPPODnsSelfrecoveryEngine {
    private static final String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
    private static final String ACTION_WIFI_NETWORK_INTERNET_INVAILD = "android.net.wifi.OPPO_WIFI_NET_INTERNET_INVAILD";
    private static final boolean DBG = true;
    private static final String DEFAULT_HOST_NAME = "conn1.oppomobile.com,conn2.oppomobile.com,m.baidu.com,info.3g.qq.com";
    private static final String DEFAULT_HOST_NAME_EXP = "www.google.com,www.bing.com";
    private static final String DEFAULT_NETWORK_BACKUP_DNS = "223.5.5.5,180.76.76.76";
    private static final String DEFAULT_NETWORK_BACKUP_DNS_EXP = "8.8.8.8,8.8.4.4";
    private static final String DNS_SELF_RECOVERY = "event_network_self_recovery";
    private static final int MSG_START_DNS_DECTOR = 1;
    private static final int MSG_UPLOAD_STATSTICS = 2;
    private static final String TAG = "OPPODnsSelfrecoveryEngine";
    private static final int TRIGER_TYPE_UPDATE_DNS = 3;
    private static final int TRIGER_TYPE_WIFI_CONNTED = 1;
    private static final int TRIGER_TYPE_WIFI_INTERNET_DETECTED_INVAILED = 2;
    private static final String TYPE_DNS_SELF_RECOVERY = "type_dns_self_recovery";
    private static final String TYPE_DNS_SERVER_RESOLVE_INFO = "dns_server_resolve_info";
    private static final int UPLOAD_STATSTICS_CYCLE = 604800000;
    private static final String WIFI_NETWROK_SELF_RECOVERY_ENGINE = "wifi_network_self_recovery";
    private static final String WIFI_STATISTIC_KEY = "wifi_fool_proof";
    private Collection<InetAddress> mBackupDnses = new ArrayList();
    private DNSBackupServerUpdate mBackupSeraverUpdate;
    private DnsBrodcast mBroadcastReciever;
    private Context mContext;
    private final DnsManager mDnsManager;
    private Handler mHandler;
    private String[] mHostnames = null;
    private NetworkAgentInfo mNai;
    private INetworkManagementService mNetd;
    private DnsStatisticsResult mStatsticsResult = null;
    private int mTrigerType;
    private boolean mVerboseLoggingEnabled = false;
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;

    private class DnsEngineCallBack implements Handler.Callback {
        private DnsEngineCallBack() {
        }

        public boolean handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                try {
                    if (!OPPODnsSelfrecoveryEngine.this.isWifiNetworkAvailable() || !OPPODnsSelfrecoveryEngine.this.isDnsRecoveryEngineEnable()) {
                        OPPODnsSelfrecoveryEngine.this.logd("wifi not available\n");
                    } else {
                        OPPODnsSelfrecoveryEngine.this.executeDNSSelfRecovery();
                    }
                } catch (Exception e) {
                    OPPODnsSelfrecoveryEngine oPPODnsSelfrecoveryEngine = OPPODnsSelfrecoveryEngine.this;
                    oPPODnsSelfrecoveryEngine.logd("MSG_START_DNS_DECTOR:" + e);
                }
            } else if (i == 2) {
                OPPODnsSelfrecoveryEngine.this.setDnsStatistics(OPPODnsSelfrecoveryEngine.DNS_SELF_RECOVERY, OPPODnsSelfrecoveryEngine.TYPE_DNS_SELF_RECOVERY);
                OPPODnsSelfrecoveryEngine.this.mHandler.sendEmptyMessageDelayed(2, UnixCalendar.WEEK_IN_MILLIS);
            }
            return true;
        }
    }

    private class DnsBrodcast extends BroadcastReceiver {
        private DnsBrodcast() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action.equals(OPPODnsSelfrecoveryEngine.ACTION_SCREEN_ON)) {
                    OPPODnsSelfrecoveryEngine.this.logd("ACTION_SCREEN_ON, DO  nothing\n");
                } else if (action.equals(OPPODnsSelfrecoveryEngine.ACTION_WIFI_NETWORK_INTERNET_INVAILD)) {
                    OPPODnsSelfrecoveryEngine.this.logd("ACTION_WIFI_NETWORK_INTERNET_INVAILD, start dns recovery engine\n");
                    OPPODnsSelfrecoveryEngine.this.mTrigerType = 2;
                    OPPODnsSelfrecoveryEngine.this.mHandler.removeMessages(1);
                    OPPODnsSelfrecoveryEngine.this.mHandler.sendEmptyMessage(1);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDnsRecoveryEngineEnable() {
        if (!this.mWifiRomUpdateHelper.getBooleanValue("NETWORK_DNS_RECOVERY_ENGINE_ENABLE", false)) {
            logd("oppo_dns_recovery_engine: disabled by xml.");
            return false;
        }
        logd("oppo_dns_recovery_engine: enabled by xml.");
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDnsStatistics(String eventId, String type) {
        if (eventId != null && type != null) {
            HashMap<String, String> map = new HashMap<>();
            map.put(eventId, type);
            map.put("totalTriger", Integer.toString(this.mStatsticsResult.mDnsSelfRecoveryTotalCount));
            map.put("connectTriger", Integer.toString(this.mStatsticsResult.mDnsSelfRecoveryConnectedCount));
            map.put("InternetInvailedTriger", Integer.toString(this.mStatsticsResult.mDnsSelfRecoveryInternetInvailedCount));
            map.put("updateDnsTriger", Integer.toString(this.mStatsticsResult.mDnsSelfRecoveryDnsupdateCount));
            map.put("oneDnsInvailed", Integer.toString(this.mStatsticsResult.mFristDnsInvailed));
            map.put("twoDnsInvailed", Integer.toString(this.mStatsticsResult.mDoubleDnsInvailed));
            map.put("threeDnsInvailed", Integer.toString(this.mStatsticsResult.mTripleDnsInvailed));
            map.put("fourDnsInvailed", Integer.toString(this.mStatsticsResult.mQuadraDnsInvailed));
            this.mStatsticsResult.resetDnsStatistics();
            OppoStatistics.onCommon(this.mContext, WIFI_STATISTIC_KEY, WIFI_NETWROK_SELF_RECOVERY_ENGINE, map, false);
        }
    }

    public OPPODnsSelfrecoveryEngine(Context context, INetworkManagementService netManager, DnsManager dnsManager) {
        this.mContext = context;
        this.mNetd = netManager;
        this.mDnsManager = dnsManager;
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            wifiRomUpdateHelper.enableVerboseLogging(this.mVerboseLoggingEnabled ? 1 : 0);
        }
        initBackupDnsServeres();
        initHostnames();
        this.mStatsticsResult = new DnsStatisticsResult();
        IntentFilter intentFilter = new IntentFilter();
        this.mBroadcastReciever = new DnsBrodcast();
        intentFilter.addAction(ACTION_WIFI_NETWORK_INTERNET_INVAILD);
        this.mContext.registerReceiver(this.mBroadcastReciever, intentFilter);
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper(), new DnsEngineCallBack());
        this.mHandler.sendEmptyMessageDelayed(2, UnixCalendar.WEEK_IN_MILLIS);
    }

    public void onNetworkConnected(NetworkAgentInfo nai, int trigerType) {
        this.mNai = nai;
        try {
            if (!isWifiNetworkAvailable() || !isDnsRecoveryEngineEnable()) {
                logd("executeDNSSelfRecovery not start,triger type = " + trigerType);
                return;
            }
            logd("executeDNSSelfRecovery start,triger type = " + trigerType);
            this.mTrigerType = trigerType;
            executeDNSSelfRecovery();
        } catch (Exception e) {
            logd("onNetworConnected:" + e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isWifiNetworkAvailable() {
        NetworkInfo ni = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(1);
        if (ni == null) {
            return false;
        }
        logd("networkInfo.DetailedState = " + ni.getDetailedState() + StringUtils.LF);
        return ni.isConnected();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void executeDNSSelfRecovery() {
        if (DNSBackupServerUpdate.mIsDetecting) {
            logd("already indetecting\n");
            return;
        }
        DNSBackupServerUpdate dNSBackupServerUpdate = this.mBackupSeraverUpdate;
        if (dNSBackupServerUpdate == null) {
            this.mBackupSeraverUpdate = new DNSBackupServerUpdate(this.mNai, this.mNetd, this.mContext, this.mBackupDnses, this.mHostnames, this.mTrigerType, this.mStatsticsResult, this.mDnsManager);
        } else {
            dNSBackupServerUpdate.updateNetworkInfo(this.mNai, this.mNetd, this.mContext, this.mBackupDnses, this.mHostnames, this.mTrigerType, this.mStatsticsResult);
        }
        this.mHandler.post(this.mBackupSeraverUpdate);
    }

    private String getRomUpdateValue(String key, String defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    private boolean isExpOperator() {
        String mcc = SystemProperties.get("android.telephony.mcc_change", "");
        String mcc2 = SystemProperties.get("android.telephony.mcc_change2", "");
        if (TextUtils.isEmpty(mcc) && TextUtils.isEmpty(mcc2)) {
            return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
        }
        if ("460".equals(mcc) || "460".equals(mcc2)) {
            return false;
        }
        return true;
    }

    private String[] getBackupServer() {
        String value;
        boolean isExp = isExpOperator();
        if (isExp) {
            value = getRomUpdateValue("NETWORK_BACKUP_DNS_EXP", null);
        } else {
            value = getRomUpdateValue("NETWORK_BACKUP_DNS", null);
        }
        if (value == null) {
            logd("get from rom update null\n");
            value = isExp ? DEFAULT_NETWORK_BACKUP_DNS_EXP : DEFAULT_NETWORK_BACKUP_DNS;
        }
        return value.split(",");
    }

    private void initBackupDnsServeres() {
        this.mBackupDnses.clear();
        String[] backupDnsServer = getBackupServer();
        for (int i = 0; i < backupDnsServer.length; i++) {
            try {
                InetAddress backupDns = NetworkUtils.numericToInetAddress(backupDnsServer[i]);
                logd("add the backup dns servers " + backupDnsServer[i] + StringUtils.LF);
                this.mBackupDnses.add(backupDns);
            } catch (IllegalArgumentException e) {
                logd("Error setting defaultDns using " + backupDnsServer[i]);
            }
        }
    }

    private void initHostnames() {
        String value;
        Boolean isExp = Boolean.valueOf(isExpOperator());
        if (isExp.booleanValue()) {
            value = getRomUpdateValue("NETWORK_DNS_QUERY_SERVER_EXP", null);
        } else {
            value = getRomUpdateValue("NETWORK_DNS_QUERY_SERVER", null);
        }
        if (value == null) {
            logd("get from rom update null\n");
            value = isExp.booleanValue() ? DEFAULT_HOST_NAME_EXP : DEFAULT_HOST_NAME;
        }
        this.mHostnames = value.split(",");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String message) {
        Log.d(TAG, message);
    }
}

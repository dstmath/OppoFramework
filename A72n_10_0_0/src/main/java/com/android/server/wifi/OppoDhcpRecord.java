package com.android.server.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.DhcpResults;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import java.util.HashMap;
import oppo.util.OppoStatistics;

public class OppoDhcpRecord {
    private static boolean DBG = false;
    private static int DHCP_RECORD_SIZE_MAX = 100;
    private static String DUP_DHCP_EVENTID = "dup_dhcp_check";
    private static String DUP_DHCP_MAP_KEY = "dup_dhcp_info";
    private static String EVENT_FIND_DUP_DHCP_SERVER = "find_dup_dhcp_server";
    private static String EVENT_FIX_DHCP_SERVER_FAILURE = "fix_dhcp_server_failure";
    private static String EVENT_START_SWITCH_DHCP_SERVER = "start_switch_dhcp_server";
    private static String EVENT_SWITCH_DHCP_SERVER_FAILURE = "switch_dhcp_server_failure";
    public static final int START_TYPE_FIX_SERVER = 2;
    public static final int START_TYPE_NORMAL = 0;
    public static final int START_TYPE_RENEW = 1;
    public static final int START_TYPE_SWITCH_SERVER = 3;
    private static final String TAG = "OppoDhcpRecord";
    private static boolean VDBG = true;
    private static AlertDialog mIpAddrConflictDialog = null;
    private OppoClientModeImplUtil mClientModeImplUtil = null;
    private String mConfigKey = null;
    private Context mContext;
    private DhcpResults mDhcpResults = null;
    private final Object mDhcpResultsLock = new Object();
    private HashMap<String, DhcpResults> mDhcpResultsMap = new HashMap<>();
    private final Object mDhcpResultsMapLock = new Object();
    private int mDhcpStartType = 0;
    private SwitchState mSwitchState = SwitchState.INIT;
    WifiRomUpdateHelper mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);

    public enum SwitchState {
        INIT,
        DOING,
        DONE
    }

    public OppoDhcpRecord(Context context) {
        this.mContext = context;
    }

    public void setSwitchState(SwitchState state) {
        log("setDhcpSwitch to " + state);
        this.mSwitchState = state;
    }

    public boolean isDoingSwitch() {
        return this.mSwitchState == SwitchState.DOING;
    }

    public boolean needSwitchDhcpServer() {
        log("needSwitchDhcpServer");
        if (this.mSwitchState != SwitchState.INIT) {
            log("mSwitchState isn't INIT, mSwitchState=" + this.mSwitchState);
            return false;
        } else if (!isDupDhcp()) {
            return false;
        } else {
            String msg = EVENT_START_SWITCH_DHCP_SERVER;
            if (!ClientModeImpl.isNotChineseOperator()) {
                msg = " Ap Info: " + this.mConfigKey + " dhcpInfo: " + this.mDhcpResults.toString();
            }
            if (DBG) {
                showDiaglog(EVENT_START_SWITCH_DHCP_SERVER, msg);
            }
            dupDhcpStatistics(EVENT_START_SWITCH_DHCP_SERVER, msg);
            setSwitchState(SwitchState.DOING);
            return true;
        }
    }

    public void handleFindDupDhcpServer(String server) {
        log("handleFindDupDhcpServer");
        if (DBG) {
            showDiaglog(EVENT_FIND_DUP_DHCP_SERVER, server);
        }
        boolean needStatistics = false;
        synchronized (this.mDhcpResultsLock) {
            if (isValidDhcpResults(this.mDhcpResults) && !this.mDhcpResults.dupServer) {
                this.mDhcpResults.dupServer = true;
                needStatistics = true;
            }
        }
        if (needStatistics) {
            String msg = EVENT_FIND_DUP_DHCP_SERVER;
            if (!ClientModeImpl.isNotChineseOperator()) {
                msg = " AP info: " + this.mConfigKey + " DHCP server: " + server;
            }
            dupDhcpStatistics(EVENT_FIND_DUP_DHCP_SERVER, msg);
        }
    }

    public void handleSwitchDhcpServerFailure(String server) {
        log("handleSwitchDhcpServerFailure");
        if (DBG) {
            showDiaglog(EVENT_SWITCH_DHCP_SERVER_FAILURE, server);
        }
        setSwitchState(SwitchState.DONE);
        rmDhcpRecord(this.mConfigKey);
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults != null) {
                this.mDhcpResults.dupServer = false;
            }
        }
        String msg = EVENT_SWITCH_DHCP_SERVER_FAILURE;
        if (!ClientModeImpl.isNotChineseOperator()) {
            msg = " AP info: " + this.mConfigKey + " DHCP server: " + server;
        }
        dupDhcpStatistics(EVENT_SWITCH_DHCP_SERVER_FAILURE, msg);
    }

    public void handleFixDhcpServerFailure(String server) {
        log("handleFixDhcpServerFailure");
        if (DBG) {
            showDiaglog(EVENT_FIX_DHCP_SERVER_FAILURE, server);
        }
        rmDhcpRecord(this.mConfigKey);
        String msg = EVENT_FIX_DHCP_SERVER_FAILURE;
        if (!ClientModeImpl.isNotChineseOperator()) {
            msg = " AP info: " + this.mConfigKey + " DHCP server: " + server;
        }
        dupDhcpStatistics(EVENT_FIX_DHCP_SERVER_FAILURE, msg);
    }

    private void showDiaglog(String title, String msg) {
        AlertDialog alertDialog = mIpAddrConflictDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
            log("dismiss dialog");
        }
        mIpAddrConflictDialog = new AlertDialog.Builder(this.mContext).setTitle(title).setMessage(msg).setPositiveButton("ok", new DialogInterface.OnClickListener() {
            /* class com.android.server.wifi.OppoDhcpRecord.AnonymousClass2 */

            public void onClick(DialogInterface d, int w) {
                if (OppoDhcpRecord.mIpAddrConflictDialog != null) {
                    OppoDhcpRecord.mIpAddrConflictDialog.dismiss();
                }
            }
        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            /* class com.android.server.wifi.OppoDhcpRecord.AnonymousClass1 */

            public void onClick(DialogInterface d, int w) {
                if (OppoDhcpRecord.mIpAddrConflictDialog != null) {
                    OppoDhcpRecord.mIpAddrConflictDialog.dismiss();
                }
            }
        }).create();
        mIpAddrConflictDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.android.server.wifi.OppoDhcpRecord.AnonymousClass3 */

            public void onDismiss(DialogInterface dialog) {
                AlertDialog unused = OppoDhcpRecord.mIpAddrConflictDialog = null;
            }
        });
        mIpAddrConflictDialog.getWindow().setType(2003);
        mIpAddrConflictDialog.show();
        TextView textView = (TextView) mIpAddrConflictDialog.findViewById(16908299);
        if (textView != null) {
            textView.setGravity(17);
        } else {
            log("textview is null");
        }
    }

    private void dupDhcpStatistics(String event, String msg) {
        String dupInfo = event + " " + msg;
        log("fool-proof, dupDhcpStatistics : " + dupInfo);
        HashMap<String, String> map = new HashMap<>();
        map.put(DUP_DHCP_MAP_KEY, dupInfo);
        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", DUP_DHCP_EVENTID, map, false);
    }

    public void handleUpdateDhcpLeaseExpiry(long leaseExpiry) {
        log("receive DhcpClient.handleUpdateDhcpLeaseExpiry " + leaseExpiry);
        synchronized (this.mDhcpResultsLock) {
            if (isValidDhcpResults(this.mDhcpResults)) {
                this.mDhcpResults.leaseExpiry = leaseExpiry;
                new DhcpResults(this.mDhcpResults);
            }
        }
    }

    public DhcpResults getDhcpRecord(String configKey) {
        DhcpResults dhcpResults;
        if (configKey == null) {
            return null;
        }
        synchronized (this.mDhcpResultsMapLock) {
            dhcpResults = new DhcpResults(this.mDhcpResultsMap.get(configKey));
        }
        return dhcpResults;
    }

    public int getStartType(DhcpResults dhcpResult) {
        if (!isValidDhcpResults(dhcpResult)) {
            return 0;
        }
        if (this.mSwitchState == SwitchState.DOING) {
            return 3;
        }
        if (dhcpResult.dupServer) {
            if (dhcpResult.internetAccess && dhcpResult.leaseExpiry > SystemClock.elapsedRealtime()) {
                return 1;
            }
            if (dhcpResult.internetAccess) {
                return 2;
            }
            return 0;
        } else if (dhcpResult.leaseExpiry > SystemClock.elapsedRealtime()) {
            return 1;
        } else {
            return 0;
        }
    }

    public void saveDhcpRecord() {
        DhcpResults dhcpResults;
        if (this.mConfigKey == null || !isValidDhcpResults(this.mDhcpResults)) {
            log("saveDhcpRecord : configKey is null or dhcpResults is Invalid");
            return;
        }
        log("saveDhcpRecord : " + this.mConfigKey + ", dhcpResults " + this.mDhcpResults);
        synchronized (this.mDhcpResultsLock) {
            dhcpResults = new DhcpResults(this.mDhcpResults);
        }
        synchronized (this.mDhcpResultsMapLock) {
            if (this.mDhcpResultsMap.size() > DHCP_RECORD_SIZE_MAX) {
                log("mDhcpResultsMap size is over DHCP_RECORD_SIZE_MAX.");
            } else {
                this.mDhcpResultsMap.put(new String(this.mConfigKey), dhcpResults);
            }
        }
    }

    public void rmDhcpRecord(String configKey) {
        log("rmDhcpRecord " + configKey);
        if (configKey != null) {
            synchronized (this.mDhcpResultsMapLock) {
                this.mDhcpResultsMap.remove(configKey);
            }
        }
    }

    private boolean isValidDhcpResults(DhcpResults dhcpResults) {
        return (dhcpResults == null || dhcpResults.serverAddress == null || dhcpResults.ipAddress == null) ? false : true;
    }

    public int getDhcpStartType(DhcpResults dhcpResult, SwitchState mSwitchState2) {
        if (!isValidDhcpResults(dhcpResult)) {
            return 0;
        }
        if (mSwitchState2 == SwitchState.DOING) {
            return 3;
        }
        if (dhcpResult.dupServer) {
            if (dhcpResult.internetAccess && dhcpResult.leaseExpiry > SystemClock.elapsedRealtime()) {
                return 1;
            }
            if (dhcpResult.internetAccess) {
                return 2;
            }
        }
        if (dhcpResult.leaseExpiry > SystemClock.elapsedRealtime()) {
            return 1;
        }
        return 0;
    }

    public boolean isDupDhcp() {
        if (!this.mWifiRomUpdateHelper.getBooleanValue("OPPO_NETWORK_DUP_DHCP_CHECK", true)) {
            log("romupdate disable switch DHCP.");
            return false;
        }
        log("SwitchState is " + this.mSwitchState);
        if (this.mSwitchState == SwitchState.DOING) {
            return true;
        }
        if (this.mSwitchState == SwitchState.INIT) {
            synchronized (this.mDhcpResultsLock) {
                if (isValidDhcpResults(this.mDhcpResults)) {
                    return this.mDhcpResults.dupServer;
                }
            }
        }
        return false;
    }

    public void clearDhcpResults() {
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults != null) {
                this.mDhcpResults.clear();
            }
        }
        this.mConfigKey = null;
    }

    public void syncDhcpResults(DhcpResults dhcpResults, String configKey) {
        if (dhcpResults == null) {
            log("syncDhcpResults(): dhcpResults is null.");
        } else if (configKey == null) {
            log("syncDhcpResults(): configKey is null.");
        } else {
            this.mConfigKey = new String(configKey);
            DhcpResults record = getDhcpRecord(configKey);
            if (record != null && record.dupServer) {
                dhcpResults.dupServer = true;
            }
            synchronized (this.mDhcpResultsLock) {
                this.mDhcpResults = new DhcpResults(dhcpResults);
            }
        }
    }

    public void setNetworkValidated(boolean validated) {
        synchronized (this.mDhcpResultsLock) {
            if (isValidDhcpResults(this.mDhcpResults)) {
                this.mDhcpResults.internetAccess = validated;
            }
        }
    }

    public void enableVerboseLogging(boolean enable) {
        VDBG = enable;
    }

    protected static void log(String s) {
        if (VDBG) {
            Log.d(TAG, "[feature#1131400] " + s);
        }
    }
}

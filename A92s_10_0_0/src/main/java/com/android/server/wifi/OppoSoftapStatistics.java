package com.android.server.wifi;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import com.android.server.wifi.OppoHotspotClientInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import oppo.util.OppoStatistics;

public class OppoSoftapStatistics {
    private static final String BACKUP_RESTORE_HOTSPOT = "backup_restore_hotspot";
    private static boolean DBG = true;
    private static final String KEY_HOTSPOT_STATISTIC = "wifi_hotspot_statistics";
    private static final String KEY_SHARING_STATISTIC = "wifi_sharing_statistics";
    private static final String KEY_WIFI_SOFTAP_FOOL_PROOF = "wifi_softap_fool_proof";
    private static final long MILLSECOND_PER_MINUTE = 60000;
    private static final int SOFTAP_DISABLED = 0;
    private static final int SOFTAP_FAILURE = -1;
    private static final int SOFTAP_HOTSPOT_ENABLED = 1;
    private static final int SOFTAP_SHARING_ENABLED = 2;
    private static final String TAG = "OppoSoftapStatistics";
    private static final String WIFI_STATISTIC = "wifi_fool_proof";
    private List<OppoHotspotClientInfo.WifiDevice> mConnectedClients = new ArrayList();
    private Context mContext;
    private long mEnableDuration = 0;
    private int mEnableState = 0;
    private String mMapKey;
    private long mStartTime;
    private OppoWifiSharingManager mWifiSharingManager = WifiInjector.getInstance().getOppoWifiSharingManager();

    public static class EventIdStrings {
        public static final int CLIENT_NUMBER = 2;
        public static final int DURATION = 1;
        public static final String EVENTID_SOFTAP_START_FAILED = "wifi_softap_start_failed";
        public static final String[] Hotspot_EventId = {"wifi_hotspot_enabled_time", "wifi_hotspot_enabled_duration", "wifi_hotspot_connected_clients"};
        public static final int START_TIME = 0;
        public static final String[] Sharing_EventId = {"wifi_sharing_enabled_time", "wifi_sharing_enabled_duration", "wifi_sharing_connected_clients"};
    }

    public OppoSoftapStatistics(Context context) {
        this.mContext = context;
    }

    public void setStatistics(String mapKey, String mapValue, String eventId) {
        HashMap<String, String> map = new HashMap<>();
        map.put(mapKey, mapValue);
        if (DBG) {
            Log.d(TAG, " onCommon eventId = " + eventId);
        }
        OppoStatistics.onCommon(this.mContext, WIFI_STATISTIC, eventId, map, false);
    }

    private void setStatistics() {
        String[] eventIdStrings;
        String str;
        int i = this.mEnableState;
        if (i == 1) {
            eventIdStrings = EventIdStrings.Hotspot_EventId;
        } else if (i == 2) {
            eventIdStrings = EventIdStrings.Sharing_EventId;
        } else if (i != 0) {
            Log.e(TAG, "hotspot failed state!");
            setStatistics(this.mMapKey, "softap_bad_state", EventIdStrings.EVENTID_SOFTAP_START_FAILED);
            return;
        } else if (DBG) {
            Log.d(TAG, "no need to set data.");
            return;
        } else {
            return;
        }
        if (eventIdStrings == null || (str = this.mMapKey) == null) {
            Log.e(TAG, " not eventId Strings or mapKey, stop statistics");
            return;
        }
        setStatistics(str, String.valueOf(this.mEnableDuration), eventIdStrings[1]);
        setStatistics(this.mMapKey, String.valueOf(this.mConnectedClients.size()), eventIdStrings[2]);
    }

    public void informStaConnected(OppoHotspotClientInfo.WifiDevice wifiDevice) {
        if (this.mEnableState != 0 || !DBG) {
            List<OppoHotspotClientInfo.WifiDevice> list = this.mConnectedClients;
            if (list != null && !list.contains(wifiDevice)) {
                this.mConnectedClients.add(wifiDevice);
                return;
            }
            return;
        }
        Log.e(TAG, "sta connected but enableState mismatch.");
    }

    public void informSoftApState(int state) {
        if (DBG) {
            Log.d(TAG, "inform state: " + state);
        }
        if (state == 11) {
            this.mEnableDuration = (System.currentTimeMillis() - this.mStartTime) / 60000;
            setStatistics();
            cleanStateAndData();
        } else if (state == 13) {
            if (this.mWifiSharingManager.isWifiSharingTethering()) {
                this.mEnableState = 2;
                this.mMapKey = KEY_SHARING_STATISTIC;
            } else if (isPhoneCloneRunning()) {
                Log.d(TAG, "phone clone running, do not collect data.");
                return;
            } else {
                this.mEnableState = 1;
                this.mMapKey = KEY_HOTSPOT_STATISTIC;
            }
            this.mStartTime = System.currentTimeMillis();
        } else if (state == 14) {
            this.mEnableState = -1;
            this.mMapKey = KEY_WIFI_SOFTAP_FOOL_PROOF;
            setStatistics();
            cleanStateAndData();
        }
    }

    private boolean isPhoneCloneRunning() {
        String phoneCloneStatus = Settings.Global.getString(this.mContext.getContentResolver(), BACKUP_RESTORE_HOTSPOT);
        if (DBG) {
            Log.d(TAG, "phone clone status: " + phoneCloneStatus);
        }
        if ("true".equals(phoneCloneStatus)) {
            return true;
        }
        return false;
    }

    private void cleanPhoneCloneStatus() {
        if (isPhoneCloneRunning()) {
            Settings.Global.putString(this.mContext.getContentResolver(), BACKUP_RESTORE_HOTSPOT, "false");
        }
    }

    private void cleanStateAndData() {
        this.mEnableState = 0;
        this.mEnableDuration = 0;
        this.mStartTime = 0;
        this.mMapKey = null;
        this.mConnectedClients.clear();
        cleanPhoneCloneStatus();
    }

    public void enableVerboseLogging(boolean enable) {
        Log.d(TAG, "enable logging: " + enable);
        DBG = enable;
    }
}

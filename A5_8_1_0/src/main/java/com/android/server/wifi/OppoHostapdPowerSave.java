package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;
import java.util.ArrayList;

public class OppoHostapdPowerSave {
    private static final String ACTION_STOP_HOTSPOT = "com.android.server.WifiManager.action.STOP_HOTSPOT";
    private static final long HOTSPOT_DISABLE_MS = 600000;
    private static final String TAG = "OppoHostapdPowerSave";
    private AlarmManager mAlarmManager = ((AlarmManager) this.mContext.getSystemService("alarm"));
    private Context mContext;
    private PendingIntent mIntentStopHotspot;
    private int mWifiApState;
    private WifiController mWifiController;

    OppoHostapdPowerSave(Context conext, WifiController controller) {
        this.mContext = conext;
        this.mWifiController = controller;
        initializeHotspotExtra();
    }

    private void initializeHotspotExtra() {
        this.mIntentStopHotspot = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_STOP_HOTSPOT), 0);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STOP_HOTSPOT);
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intentFilter.addAction("codeaurora.net.conn.TETHER_CONNECT_STATE_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(OppoHostapdPowerSave.TAG, "Got receive , action:" + action);
                if (action.equals(OppoHostapdPowerSave.ACTION_STOP_HOTSPOT)) {
                    OppoHostapdPowerSave.this.stopSoftAp();
                } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                    OppoHostapdPowerSave.this.mWifiApState = intent.getIntExtra("wifi_state", 11);
                    if (OppoHostapdPowerSave.this.mWifiApState == 13) {
                        OppoHostapdPowerSave.this.startPowerSaveAlarm();
                    } else if (OppoHostapdPowerSave.this.mWifiApState == 11) {
                        OppoHostapdPowerSave.this.canclePowerSaveAlarm(false);
                    } else {
                        Log.e(OppoHostapdPowerSave.TAG, "other state no need do someting , mWifiApState:" + OppoHostapdPowerSave.this.mWifiApState);
                    }
                } else if (!action.equals("codeaurora.net.conn.TETHER_CONNECT_STATE_CHANGED") || OppoHostapdPowerSave.this.mWifiApState != 13) {
                } else {
                    if (OppoHostapdPowerSave.this.getSoftApConnectedNum() > 0) {
                        OppoHostapdPowerSave.this.canclePowerSaveAlarm(true);
                    } else {
                        OppoHostapdPowerSave.this.startPowerSaveAlarm();
                    }
                }
            }
        }, intentFilter);
    }

    private void canclePowerSaveAlarm(boolean needCheck) {
        if (!needCheck) {
            Log.d(TAG, "canclePowerSaveAlarm direct ");
            this.mAlarmManager.cancel(this.mIntentStopHotspot);
        } else if (getSoftApConnectedNum() > 0) {
            this.mAlarmManager.cancel(this.mIntentStopHotspot);
            Log.d(TAG, "canclePowerSaveAlarm ");
        } else {
            Log.e(TAG, "canclePowerSaveAlarm failed hava ap connected num is = " + getSoftApConnectedNum());
        }
    }

    private void startPowerSaveAlarm() {
        if (getSoftApConnectedNum() == 0) {
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + HOTSPOT_DISABLE_MS, this.mIntentStopHotspot);
        }
    }

    private int getSoftApConnectedNum() {
        return ((ArrayList) OppoHotspotClientInfo.getInstance(this.mContext).getConnectedStations()).size();
    }

    private void stopSoftAp() {
        Log.d(TAG, "stopSoftAp----ten min softap not use,so stop ");
        this.mWifiController.sendMessage(155658, 0, 0);
    }
}

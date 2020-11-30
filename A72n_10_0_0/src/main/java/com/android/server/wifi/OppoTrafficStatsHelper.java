package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.NetworkStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.wifi.V1_3.IWifiChip;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import oppo.util.OppoStatistics;

public class OppoTrafficStatsHelper {
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_WIFI_NETWORK_STATS = "oppo.intent.action.WIFI_NETWORK_STATS";
    private static final String STATS_DATE = "date";
    private static final String STATS_EVENT = "wifi_daily_traffic_stats";
    private static final int STATS_TIME_HOURS = 3;
    private static final String TAG = "OppoTrafficStatsHelper";
    private static final String WIFI_RX_BYTES = "wlan_rx_bytes";
    private static final String WIFI_TX_BYTES = "wlan_tx_bytes";
    private AlarmManager mAlarmManager;
    private Context mContext;
    private NetworkStatsManager mNetworkStatsManager = null;
    private PendingIntent mStatsIntent;

    public OppoTrafficStatsHelper(Context context) {
        this.mContext = context;
        this.mNetworkStatsManager = (NetworkStatsManager) context.getSystemService("netstats");
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mStatsIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_WIFI_NETWORK_STATS, (Uri) null), IWifiChip.ChipCapabilityMask.DUAL_BAND_DUAL_CHANNEL);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_WIFI_NETWORK_STATS);
        filter.addAction(ACTION_BOOT_COMPLETED);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoTrafficStatsHelper.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    Log.d(OppoTrafficStatsHelper.TAG, "Recv action: " + action);
                    if (action.equals(OppoTrafficStatsHelper.ACTION_BOOT_COMPLETED)) {
                        OppoTrafficStatsHelper.this.startStatsTrigger();
                    } else if (action.equals(OppoTrafficStatsHelper.ACTION_WIFI_NETWORK_STATS)) {
                        OppoTrafficStatsHelper.this.startNetworkStats();
                        OppoTrafficStatsHelper.this.startStatsTrigger();
                    }
                }
            }
        }, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startStatsTrigger() {
        Log.d(TAG, "startStatsTrigger to: " + Long.toString(caculateNextStatsTime()));
        this.mAlarmManager.set(0, caculateNextStatsTime(), this.mStatsIntent);
    }

    private void cancelStatsTrigger() {
        Log.d(TAG, "cancelStatsTrigger");
        this.mAlarmManager.cancel(this.mStatsIntent);
    }

    private long caculateNextStatsTime() {
        long currentTime = System.currentTimeMillis();
        long todayStatsTime = getTodayStatsTime();
        Calendar cal = Calendar.getInstance();
        if (currentTime >= todayStatsTime) {
            cal.add(5, 1);
        }
        cal.set(11, 3);
        cal.set(12, 0);
        cal.set(13, 0);
        return cal.getTimeInMillis();
    }

    public long getRxBytesWifi(long startTime, long endTime) {
        try {
            return this.mNetworkStatsManager.querySummaryForDevice(1, "", startTime, endTime).getRxBytes();
        } catch (RemoteException e) {
            return -1;
        }
    }

    public long getTxBytesWifi(long startTime, long endTime) {
        try {
            return this.mNetworkStatsManager.querySummaryForDevice(1, "", startTime, endTime).getTxBytes();
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static long getYesterdayTimesMorning() {
        Calendar cal = Calendar.getInstance();
        cal.add(5, -1);
        cal.set(11, 0);
        cal.set(13, 0);
        cal.set(12, 0);
        cal.set(14, 0);
        return cal.getTimeInMillis();
    }

    public static long getTimesMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(11, 0);
        cal.set(13, 0);
        cal.set(12, 0);
        cal.set(14, 0);
        return cal.getTimeInMillis();
    }

    public static long getTodayStatsTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(11, 3);
        cal.set(13, 0);
        cal.set(12, 0);
        cal.set(14, 0);
        return cal.getTimeInMillis();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startNetworkStats() {
        HashMap<String, String> map = new HashMap<>();
        long tx_byte = getTxBytesWifi(getYesterdayTimesMorning(), getTimesMorning());
        long rx_byte = getRxBytesWifi(getYesterdayTimesMorning(), getTimesMorning());
        Calendar cal = Calendar.getInstance();
        cal.add(5, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        map.put(STATS_DATE, yesterday);
        map.put(WIFI_TX_BYTES, Long.toString(tx_byte));
        map.put(WIFI_RX_BYTES, Long.toString(rx_byte));
        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", STATS_EVENT, map, false);
        Log.d(TAG, "date:" + yesterday + "  tx_byte:" + tx_byte + "  rx_byte:" + rx_byte);
    }
}

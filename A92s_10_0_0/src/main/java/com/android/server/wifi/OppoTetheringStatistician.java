package com.android.server.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.ColorOSTelephonyManager;
import android.telephony.SubscriptionManager;
import android.util.Log;
import java.util.Calendar;

public class OppoTetheringStatistician {
    private static final String OPPO_WIFI_AP_TRAFFIC_LIMIT = "oppo_wifi_ap_traffic_limit";
    private static final int SLOT_MAX = 2;
    private static final String TAG = "OppoTetheringStatistician";
    private static final Long TRAFFIC_100M = 104857600L;
    private static final Long TRAFFIC_10G = 10737418240L;
    private static final Long TRAFFIC_10M = 10485760L;
    private static final Long TRAFFIC_1G = 1073741824L;
    private static final Long TRAFFIC_200M = 209715200L;
    private static final Long TRAFFIC_500M = 524288000L;
    private static final Long TRAFFIC_50M = 52428800L;
    private static final Long TRAFFIC_5G = 5368709120L;
    private static final int TRAFFIC_LIMIT_100M = 3;
    private static final int TRAFFIC_LIMIT_10G = 8;
    private static final int TRAFFIC_LIMIT_10M = 1;
    private static final int TRAFFIC_LIMIT_1G = 6;
    private static final int TRAFFIC_LIMIT_200M = 4;
    private static final int TRAFFIC_LIMIT_500M = 5;
    private static final int TRAFFIC_LIMIT_50M = 2;
    private static final int TRAFFIC_LIMIT_5G = 7;
    private static final int TRAFFIC_LIMIT_NONE = 9;
    private final long[] mBaseTraffic = new long[2];
    private Context mContext;
    private Handler mHandler = new Handler();
    private OnStartTetheringCallback mStartTetheringCallback;
    private long mStartTime;
    private boolean mStarted;
    private OnTetheringStaticsCallback mStaticsCallback;
    private INetworkStatsService mStatsService;
    private SubscriptionManager mSubscriptionManager;
    private final long[] mTraffic = new long[2];

    public OppoTetheringStatistician(Context context) {
        this.mContext = context;
    }

    public synchronized void start(OnTetheringStaticsCallback callback) {
        if (!this.mStarted) {
            this.mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
            this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
            this.mStartTime = getTodayStart();
            this.mStarted = true;
            this.mStartTetheringCallback = new OnStartTetheringCallback();
            this.mStaticsCallback = callback;
        }
    }

    public synchronized void stop() {
        if (this.mStarted) {
            this.mStatsService = null;
            this.mSubscriptionManager = null;
            for (int i = 0; i < 2; i++) {
                this.mBaseTraffic[i] = 0;
                this.mTraffic[i] = 0;
            }
            this.mStarted = false;
            this.mStartTetheringCallback = null;
        }
    }

    public synchronized long getTotalTraffic() {
        long result;
        if (this.mStarted) {
            try {
                this.mStatsService.forceUpdate();
                INetworkStatsSession session = this.mStatsService.openSession();
                long end = System.currentTimeMillis();
                for (int i = 0; i < 2; i++) {
                    long traffic = getSlotTetheringTraffic(session, i, this.mStartTime, end);
                    if (this.mBaseTraffic[i] == 0) {
                        this.mBaseTraffic[i] = traffic;
                    } else {
                        long traffic2 = traffic - this.mBaseTraffic[i];
                        if (traffic2 > 0) {
                            this.mTraffic[i] = traffic2;
                        }
                    }
                }
                if (session != null) {
                    session.close();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "tethering statistician is not started");
        }
        result = 0;
        for (int i2 = 0; i2 < 2; i2++) {
            result += this.mTraffic[i2];
        }
        setSoftAp(result);
        return result;
    }

    private long getSlotTetheringTraffic(INetworkStatsSession session, int slot, long start, long end) {
        long result = 0;
        try {
            SubscriptionManager subscriptionManager = this.mSubscriptionManager;
            int[] subId = SubscriptionManager.getSubId(slot);
            if (subId == null || subId.length <= 0) {
                return result;
            }
            try {
                NetworkStats stats = session.getSummaryForAllUid(getNetworkTemplate(session, slot, subId[0]), start, end, false);
                NetworkStats.Entry entry = null;
                for (int i = 0; i < stats.size(); i++) {
                    entry = stats.getValues(i, entry);
                    if (entry.uid == -5) {
                        result += entry.rxBytes + entry.txBytes;
                        Log.d(TAG, "get traffic, slot: " + slot + "; entry: " + i + "; r: " + entry.rxBytes + "; t: " + entry.txBytes);
                    }
                }
            } catch (Exception e) {
                e = e;
                e.printStackTrace();
                return result;
            }
            return result;
        } catch (Exception e2) {
            e = e2;
            e.printStackTrace();
            return result;
        }
    }

    private NetworkTemplate getNetworkTemplate(INetworkStatsSession mStatsSession, int slotId, int subId) {
        return NetworkTemplate.buildTemplateMobileAll(ColorOSTelephonyManager.getDefault(this.mContext).getSubscriberIdGemini(slotId));
    }

    private long getTodayStart() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(11, 0);
        todayStart.set(12, 0);
        todayStart.set(13, 0);
        return todayStart.getTimeInMillis();
    }

    private void setSoftAp(long traffic) {
        int trafficLimit = Settings.System.getInt(this.mContext.getContentResolver(), OPPO_WIFI_AP_TRAFFIC_LIMIT, 9);
        if (trafficLimit >= 1 && trafficLimit < 9) {
            switch (trafficLimit) {
                case 1:
                    if (traffic > TRAFFIC_10M.longValue()) {
                        Log.d(TAG, "turn off soft AP for greater than 10M, traffic is " + traffic);
                        setTethering(this.mContext, 0, false, this.mStartTetheringCallback, this.mHandler);
                        return;
                    }
                    return;
                case 2:
                    if (traffic > TRAFFIC_50M.longValue()) {
                        Log.d(TAG, "turn off soft AP for greater than 50M, traffic is " + traffic);
                        setTethering(this.mContext, 0, false, this.mStartTetheringCallback, this.mHandler);
                        return;
                    }
                    return;
                case 3:
                    if (traffic > TRAFFIC_100M.longValue()) {
                        Log.d(TAG, "turn off soft AP for greater than 100M, traffic is " + traffic);
                        setTethering(this.mContext, 0, false, this.mStartTetheringCallback, this.mHandler);
                        return;
                    }
                    return;
                case 4:
                    if (traffic > TRAFFIC_200M.longValue()) {
                        Log.d(TAG, "turn off soft AP for greater than 200M, traffic is " + traffic);
                        setTethering(this.mContext, 0, false, this.mStartTetheringCallback, this.mHandler);
                        return;
                    }
                    return;
                case 5:
                    if (traffic > TRAFFIC_500M.longValue()) {
                        Log.d(TAG, "turn off soft AP for greater than 500M, traffic is " + traffic);
                        setTethering(this.mContext, 0, false, this.mStartTetheringCallback, this.mHandler);
                        return;
                    }
                    return;
                case 6:
                    if (traffic > TRAFFIC_1G.longValue()) {
                        Log.d(TAG, "turn off soft AP for greater than 1G, traffic is " + traffic);
                        setTethering(this.mContext, 0, false, this.mStartTetheringCallback, this.mHandler);
                        return;
                    }
                    return;
                case 7:
                    if (traffic > TRAFFIC_5G.longValue()) {
                        Log.d(TAG, "turn off soft AP for greater than 5G, traffic is " + traffic);
                        setTethering(this.mContext, 0, false, this.mStartTetheringCallback, this.mHandler);
                        return;
                    }
                    return;
                case 8:
                    if (traffic > TRAFFIC_10G.longValue()) {
                        Log.d(TAG, "turn off soft AP for greater than 10G, traffic is " + traffic);
                        setTethering(this.mContext, 0, false, this.mStartTetheringCallback, this.mHandler);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private void setTethering(Context context, int choice, boolean enabled, OnStartTetheringCallback callback, Handler handler) {
        if (context == null) {
            Log.e(TAG, "setTethering: context is null.");
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (enabled) {
            cm.startTethering(choice, true, callback, handler);
            OnTetheringStaticsCallback onTetheringStaticsCallback = this.mStaticsCallback;
            if (onTetheringStaticsCallback != null) {
                onTetheringStaticsCallback.onTetheringStarted();
                return;
            }
            return;
        }
        cm.stopTethering(choice);
        OnTetheringStaticsCallback onTetheringStaticsCallback2 = this.mStaticsCallback;
        if (onTetheringStaticsCallback2 != null) {
            onTetheringStaticsCallback2.onTetheringStoped();
        }
    }

    private final class OnStartTetheringCallback extends ConnectivityManager.OnStartTetheringCallback {
        public OnStartTetheringCallback() {
        }

        public void onTetheringStarted() {
            OppoTetheringStatistician.super.onTetheringStarted();
        }

        public void onTetheringFailed() {
            OppoTetheringStatistician.super.onTetheringFailed();
        }
    }

    public static class OnTetheringStaticsCallback {
        public void onTetheringStarted() {
        }

        public void onTetheringStoped() {
        }
    }
}

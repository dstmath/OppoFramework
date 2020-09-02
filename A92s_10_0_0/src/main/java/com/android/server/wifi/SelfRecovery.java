package com.android.server.wifi;

import android.util.Log;
import java.util.Iterator;
import java.util.LinkedList;

public class SelfRecovery {
    public static final long MAX_RESTARTS_IN_TIME_WINDOW = 6;
    public static final long MAX_RESTARTS_TIME_WINDOW_MILLIS = 3600000;
    public static final int REASON_LAST_RESORT_WATCHDOG = 0;
    public static final int REASON_STA_IFACE_DOWN = 2;
    protected static final String[] REASON_STRINGS = {"Last Resort Watchdog", "WifiNative Failure", "Sta Interface Down"};
    public static final int REASON_WIFINATIVE_FAILURE = 1;
    private static final String TAG = "WifiSelfRecovery";
    private final Clock mClock;
    private final LinkedList<Long> mPastRestartTimes = new LinkedList<>();
    private final WifiController mWifiController;

    public SelfRecovery(WifiController wifiController, Clock clock) {
        this.mWifiController = wifiController;
        this.mClock = clock;
    }

    public void trigger(int reason) {
        if (reason != 0 && reason != 1 && reason != 2) {
            Log.e(TAG, "Invalid trigger reason. Ignoring...");
        } else if (reason == 2) {
            Log.e(TAG, "STA interface down, disable wifi");
            this.mWifiController.sendMessage(155665, reason);
        } else {
            Log.e(TAG, "Triggering recovery for reason: " + REASON_STRINGS[reason]);
            if (reason == 1) {
                trimPastRestartTimes();
                if (((long) this.mPastRestartTimes.size()) >= 6) {
                    Log.e(TAG, "Already restarted wifi (6) times in last (3600000ms ). Disabling wifi");
                    this.mWifiController.sendMessage(155667);
                    return;
                }
                this.mPastRestartTimes.add(Long.valueOf(this.mClock.getElapsedSinceBootMillis()));
            }
            this.mWifiController.sendMessage(155665, reason);
        }
    }

    private void trimPastRestartTimes() {
        Iterator<Long> iter = this.mPastRestartTimes.iterator();
        long now = this.mClock.getElapsedSinceBootMillis();
        while (iter.hasNext() && now - iter.next().longValue() > 3600000) {
            iter.remove();
        }
    }
}

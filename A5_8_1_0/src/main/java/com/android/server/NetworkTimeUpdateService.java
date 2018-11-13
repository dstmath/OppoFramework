package com.android.server;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.Log;
import android.util.NtpTrustedTime;
import android.util.TimeUtils;
import android.util.TrustedTime;
import com.android.internal.util.DumpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Calendar;

public class NetworkTimeUpdateService extends Binder {
    private static final String ACTION_POLL = "com.android.server.NetworkTimeUpdateService.action.POLL";
    private static final long ALIGNTIME_DEALY = 40000;
    private static final boolean DBG = false;
    private static final int EVENT_AUTO_TIME_CHANGED = 1;
    private static final int EVENT_NETWORK_CHANGED = 3;
    private static final int EVENT_POLL_NETWORK_TIME = 2;
    private static final String GAME_PACKAGE_NAME = "com.tencent.tmgp.sgame";
    private static final int NETWORK_CHANGE_EVENT_DELAY_MS = 1000;
    private static final long NOT_SET = -1;
    private static int POLL_REQUEST = 0;
    private static final String TAG = "NetworkTimeUpdateService";
    private static final String WIFI_AVAILABLE_ACTION = "EVENT_NETWORK_AVAILABLE";
    private AlarmManager mAlarmManager;
    private boolean mBootCompleted = false;
    private ConnectivityManager mCM;
    private Context mContext;
    private Network mDefaultNetwork = null;
    private boolean mFirstBoot = false;
    private Handler mHandler;
    private long mLastNtpFetchTime = -1;
    private NetworkTimeUpdateCallback mNetworkTimeUpdateCallback;
    private BroadcastReceiver mNitzReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.NETWORK_SET_TIME".equals(action)) {
                NetworkTimeUpdateService.this.mNitzTimeSetTime = SystemClock.elapsedRealtime();
            } else if ("android.intent.action.NETWORK_SET_TIMEZONE".equals(action)) {
                NetworkTimeUpdateService.this.mNitzZoneSetTime = SystemClock.elapsedRealtime();
            }
        }
    };
    private long mNitzTimeSetTime = -1;
    private long mNitzZoneSetTime = -1;
    private PendingIntent mPendingPollIntent;
    private final long mPollingIntervalMs;
    private final long mPollingIntervalShorterMs;
    private SettingsObserver mSettingsObserver;
    private TrustedTime mTime;
    private final int mTimeErrorThresholdMs;
    private int mTryAgainCounter;
    private final int mTryAgainTimesMax;
    private final WakeLock mWakeLock;

    private class MyHandler extends Handler {
        public MyHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 2:
                case 3:
                    NetworkTimeUpdateService.this.onPollNetworkTime(msg.what);
                    return;
                default:
                    return;
            }
        }
    }

    private class NetworkTimeUpdateCallback extends NetworkCallback {
        /* synthetic */ NetworkTimeUpdateCallback(NetworkTimeUpdateService this$0, NetworkTimeUpdateCallback -this1) {
            this();
        }

        private NetworkTimeUpdateCallback() {
        }

        public void onAvailable(Network network) {
            Log.d(NetworkTimeUpdateService.TAG, String.format("New default network %s; checking time.", new Object[]{network}));
            NetworkTimeUpdateService.this.mDefaultNetwork = network;
            NetworkTimeUpdateService.this.onPollNetworkTime(3);
        }

        public void onLost(Network network) {
            if (network.equals(NetworkTimeUpdateService.this.mDefaultNetwork)) {
                NetworkTimeUpdateService.this.mDefaultNetwork = null;
            }
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private Handler mHandler;
        private int mMsg;

        SettingsObserver(Handler handler, int msg) {
            super(handler);
            this.mHandler = handler;
            this.mMsg = msg;
        }

        void observe(Context context) {
            context.getContentResolver().registerContentObserver(Global.getUriFor("auto_time"), false, this);
        }

        public void onChange(boolean selfChange) {
            this.mHandler.obtainMessage(this.mMsg).sendToTarget();
        }
    }

    private boolean isNeedSkipPollNetworkTime() {
        Object packageName = null;
        try {
            packageName = ActivityManagerNative.getDefault().getTopAppName().getPackageName();
        } catch (Exception e) {
        }
        if (GAME_PACKAGE_NAME.equals(packageName)) {
            return true;
        }
        return false;
    }

    public NetworkTimeUpdateService(Context context) {
        this.mContext = context;
        this.mTime = NtpTrustedTime.getInstance(context);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mPendingPollIntent = PendingIntent.getBroadcast(this.mContext, POLL_REQUEST, new Intent(ACTION_POLL, null), 0);
        this.mPollingIntervalMs = (long) this.mContext.getResources().getInteger(17694833);
        this.mPollingIntervalShorterMs = (long) this.mContext.getResources().getInteger(17694834);
        this.mTryAgainTimesMax = this.mContext.getResources().getInteger(17694835);
        this.mTimeErrorThresholdMs = this.mContext.getResources().getInteger(17694836);
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG);
    }

    public void systemRunning() {
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new MyHandler(thread.getLooper());
        registerForTelephonyIntents();
        registerForAlarms();
        checkSystemTime();
        Log.d(TAG, "NetworkTimeUpdateService systemReady");
        this.mNetworkTimeUpdateCallback = new NetworkTimeUpdateCallback(this, null);
        this.mCM.registerDefaultNetworkCallback(this.mNetworkTimeUpdateCallback, this.mHandler);
        this.mSettingsObserver = new SettingsObserver(this.mHandler, 1);
        this.mSettingsObserver.observe(this.mContext);
    }

    private void checkSystemTime() {
        String key = "persist.sys.device_first_boot";
        boolean isFirstBoot = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.sys.device_first_boot", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON));
        long cur = System.currentTimeMillis();
        this.mFirstBoot = isFirstBoot;
        if (isFirstBoot || cur < 50000000) {
            SystemProperties.set("persist.sys.device_first_boot", "0");
            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(SystemProperties.get("ro.build.date.YmdHM", "2018").substring(0, 4)), 0, 1, 0, 0, 0);
            long destinationTime = c.getTimeInMillis();
            Log.w(TAG, "cur [" + cur + "]" + "  destinationTime [" + destinationTime + "]");
            if (cur < destinationTime) {
                SystemClock.setCurrentTimeMillis(SystemClock.elapsedRealtime() + destinationTime);
            }
            Log.w(TAG, "reset system time here, isFirstBoot = [" + isFirstBoot + "]");
        }
    }

    private void registerForTelephonyIntents() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.NETWORK_SET_TIME");
        intentFilter.addAction("android.intent.action.NETWORK_SET_TIMEZONE");
        this.mContext.registerReceiver(this.mNitzReceiver, intentFilter);
    }

    private void registerForAlarms() {
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkTimeUpdateService.this.mHandler.obtainMessage(2).sendToTarget();
            }
        }, new IntentFilter(ACTION_POLL));
    }

    private void onPollNetworkTime(int event) {
        if (!isAutomaticTimeRequested()) {
            Log.d(TAG, "Settings.Global.AUTO_TIME = 0");
        } else if (isNeedSkipPollNetworkTime()) {
            Log.d(TAG, "Skip PollNetworkTime");
        } else {
            this.mWakeLock.acquire();
            try {
                onPollNetworkTimeUnderWakeLock(event);
            } finally {
                this.mWakeLock.release();
            }
        }
    }

    private void onPollNetworkTimeUnderWakeLock(int event) {
        long refTime = SystemClock.elapsedRealtime();
        if (this.mNitzTimeSetTime == -1 || refTime - this.mNitzTimeSetTime >= this.mPollingIntervalMs || event == 1) {
            long currentTime = System.currentTimeMillis();
            if (this.mLastNtpFetchTime == -1 || refTime >= this.mLastNtpFetchTime + this.mPollingIntervalMs || event == 1) {
                if (!((this.mTime.getCacheAge() < this.mPollingIntervalMs && event != 1) || this.mDefaultNetwork == null || (this.mTime.forceRefresh() ^ 1) == 0)) {
                    Log.d(TAG, "forceRefresh failed !");
                }
                if (this.mTime.getCacheAge() < this.mPollingIntervalMs) {
                    long ntp = this.mTime.currentTimeMillis();
                    this.mTryAgainCounter = 0;
                    if ((Math.abs(ntp - currentTime) > ((long) this.mTimeErrorThresholdMs) || this.mLastNtpFetchTime == -1) && ntp / 1000 < 2147483647L) {
                        SystemClock.setCurrentTimeMillis(ntp);
                    }
                    this.mLastNtpFetchTime = SystemClock.elapsedRealtime();
                } else {
                    this.mTryAgainCounter++;
                    if (this.mTryAgainTimesMax < 0 || this.mTryAgainCounter <= this.mTryAgainTimesMax) {
                        resetAlarm(this.mPollingIntervalShorterMs);
                    } else {
                        this.mTryAgainCounter = 0;
                        resetAlarm(this.mPollingIntervalMs);
                    }
                    return;
                }
            }
            resetAlarm(this.mPollingIntervalMs);
            return;
        }
        Log.d(TAG, "resetAlarm(mPollingIntervalMs),  mPollingIntervalMs=" + this.mPollingIntervalMs);
        resetAlarm(this.mPollingIntervalMs);
    }

    private void resetAlarm(long interval) {
        this.mAlarmManager.cancel(this.mPendingPollIntent);
        this.mAlarmManager.set(3, SystemClock.elapsedRealtime() + interval, this.mPendingPollIntent);
    }

    private boolean isAutomaticTimeRequested() {
        if (Global.getInt(this.mContext.getContentResolver(), "auto_time", 0) != 0) {
            return true;
        }
        return false;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.print("PollingIntervalMs: ");
            TimeUtils.formatDuration(this.mPollingIntervalMs, pw);
            pw.print("\nPollingIntervalShorterMs: ");
            TimeUtils.formatDuration(this.mPollingIntervalShorterMs, pw);
            pw.println("\nTryAgainTimesMax: " + this.mTryAgainTimesMax);
            pw.print("TimeErrorThresholdMs: ");
            TimeUtils.formatDuration((long) this.mTimeErrorThresholdMs, pw);
            pw.println("\nTryAgainCounter: " + this.mTryAgainCounter);
            pw.print("LastNtpFetchTime: ");
            TimeUtils.formatDuration(this.mLastNtpFetchTime, pw);
            pw.println();
        }
    }
}

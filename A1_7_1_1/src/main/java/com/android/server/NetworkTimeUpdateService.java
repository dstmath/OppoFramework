package com.android.server;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Log;
import android.util.NtpTrustedTime;
import android.util.TimeUtils;
import android.util.TrustedTime;
import android.widget.Toast;
import com.android.server.display.OppoBrightUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class NetworkTimeUpdateService extends Binder {
    private static final String ACTION_POLL = "com.android.server.NetworkTimeUpdateService.action.POLL";
    private static final long ALIGNTIME_DEALY = 40000;
    private static final String BOOT_SYS_PROPERTY = "persist.sys.first_time_boot";
    private static final boolean DBG = true;
    private static final String DECRYPT_STATE = "trigger_restart_framework";
    private static final int EVENT_AUTO_TIME_CHANGED = 1;
    private static final int EVENT_GPS_TIME_SYNC_CHANGED = 4;
    private static final int EVENT_NETWORK_CHANGED = 3;
    private static final int EVENT_POLL_NETWORK_TIME = 2;
    private static final String GAME_PACKAGE_NAME = "com.tencent.tmgp.sgame";
    private static final int NETWORK_CHANGE_EVENT_DELAY_MS = 1000;
    private static final long NOT_SET = -1;
    private static int POLL_REQUEST = 0;
    private static final String[] SERVERLIST = null;
    private static final String TAG = "NetworkTimeUpdateService";
    private static final String WIFI_AVAILABLE_ACTION = "EVENT_NETWORK_AVAILABLE";
    private static int mDefaultYear;
    private AlarmManager mAlarmManager;
    private boolean mBootCompleted;
    private BroadcastReceiver mConnectivityReceiver;
    private Context mContext;
    private String mDefaultServer;
    private boolean mFirstBoot;
    private Handler mGpsHandler;
    private HandlerThread mGpsThread;
    private GpsTimeSyncObserver mGpsTimeSyncObserver;
    private Thread mGpsTimerThread;
    private Handler mGpsToastHandler;
    private Handler mHandler;
    private BroadcastReceiver mIPOReceiver;
    private boolean mIsGpsTimeSyncRunning;
    private long mLastNtpFetchTime;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private BroadcastReceiver mNitzReceiver;
    private long mNitzTimeSetTime;
    private long mNitzZoneSetTime;
    private ArrayList<String> mNtpServers;
    private PendingIntent mPendingPollIntent;
    private final long mPollingIntervalMs;
    private final long mPollingIntervalShorterMs;
    private SettingsObserver mSettingsObserver;
    private TrustedTime mTime;
    private final int mTimeErrorThresholdMs;
    private int mTryAgainCounter;
    private final int mTryAgainTimesMax;
    private final WakeLock mWakeLock;

    private static class GpsTimeSyncObserver extends ContentObserver {
        private Handler mHandler;
        private int mMsg;

        GpsTimeSyncObserver(Handler handler, int msg) {
            super(handler);
            this.mHandler = handler;
            this.mMsg = msg;
        }

        void observe(Context context) {
            context.getContentResolver().registerContentObserver(System.getUriFor("auto_time_gps"), false, this);
        }

        public void onChange(boolean selfChange) {
            this.mHandler.obtainMessage(this.mMsg).sendToTarget();
        }
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 2:
                case 3:
                    Log.d(NetworkTimeUpdateService.TAG, "MyHandler::handleMessage what = " + msg.what);
                    NetworkTimeUpdateService.this.onPollNetworkTime(msg.what);
                    return;
                case 4:
                    boolean gpsTimeSyncStatus = NetworkTimeUpdateService.this.getGpsTimeSyncState();
                    Log.d(NetworkTimeUpdateService.TAG, "GPS Time sync is changed to " + gpsTimeSyncStatus);
                    NetworkTimeUpdateService.this.onGpsTimeChanged(gpsTimeSyncStatus);
                    return;
                default:
                    return;
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.NetworkTimeUpdateService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.NetworkTimeUpdateService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.NetworkTimeUpdateService.<clinit>():void");
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
        this.mNitzTimeSetTime = -1;
        this.mNitzZoneSetTime = -1;
        this.mLastNtpFetchTime = -1;
        this.mBootCompleted = false;
        this.mFirstBoot = false;
        this.mNtpServers = new ArrayList();
        this.mNitzReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(NetworkTimeUpdateService.TAG, "Received " + action);
                if ("android.intent.action.NETWORK_SET_TIME".equals(action)) {
                    Log.d(NetworkTimeUpdateService.TAG, "mNitzReceiver Receive ACTION_NETWORK_SET_TIME");
                    NetworkTimeUpdateService.this.mNitzTimeSetTime = SystemClock.elapsedRealtime();
                } else if ("android.intent.action.NETWORK_SET_TIMEZONE".equals(action)) {
                    Log.d(NetworkTimeUpdateService.TAG, "mNitzReceiver Receive ACTION_NETWORK_SET_TIMEZONE");
                    NetworkTimeUpdateService.this.mNitzZoneSetTime = SystemClock.elapsedRealtime();
                }
            }
        };
        this.mConnectivityReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    Log.d(NetworkTimeUpdateService.TAG, "Received CONNECTIVITY_ACTION ");
                    NetworkTimeUpdateService.this.mHandler.sendMessageDelayed(NetworkTimeUpdateService.this.mHandler.obtainMessage(3), 1000);
                }
                if (NetworkTimeUpdateService.WIFI_AVAILABLE_ACTION.equals(action)) {
                    Log.d(NetworkTimeUpdateService.TAG, "WIFI_AVAILABLE_ACTION");
                    NetworkTimeUpdateService.this.mHandler.obtainMessage(3).sendToTarget();
                }
            }
        };
        this.mIPOReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(NetworkTimeUpdateService.TAG, "Received ACTION_SHUTDOWN_IPO ");
                NetworkTimeUpdateService.this.mNitzTimeSetTime = -1;
                NetworkTimeUpdateService.this.mNitzZoneSetTime = -1;
            }
        };
        this.mIsGpsTimeSyncRunning = false;
        this.mGpsToastHandler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(NetworkTimeUpdateService.this.mContext, msg.obj, 1).show();
            }
        };
        this.mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                NetworkTimeUpdateService.this.mGpsTimerThread.interrupt();
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
        this.mContext = context;
        this.mTime = NtpTrustedTime.getInstance(context);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mPendingPollIntent = PendingIntent.getBroadcast(this.mContext, POLL_REQUEST, new Intent(ACTION_POLL, null), 0);
        this.mPollingIntervalMs = (long) this.mContext.getResources().getInteger(17694848);
        this.mPollingIntervalShorterMs = (long) this.mContext.getResources().getInteger(17694849);
        this.mTryAgainTimesMax = this.mContext.getResources().getInteger(17694850);
        this.mTimeErrorThresholdMs = this.mContext.getResources().getInteger(17694851);
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG);
        this.mDefaultServer = ((NtpTrustedTime) this.mTime).getServer();
        this.mNtpServers.add(this.mDefaultServer);
        for (String str : SERVERLIST) {
            this.mNtpServers.add(str);
        }
        this.mTryAgainCounter = 0;
    }

    public void systemRunning() {
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new MyHandler(thread.getLooper());
        registerForTelephonyIntents();
        registerForAlarms();
        registerForConnectivityIntents();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        this.mContext.registerReceiver(this.mIPOReceiver, intentFilter);
        checkSystemTime();
        Log.d(TAG, "NetworkTimeUpdateService systemReady");
        if (this.mFirstBoot) {
            this.mHandler.obtainMessage(2).sendToTarget();
        } else {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), ALIGNTIME_DEALY);
        }
        this.mSettingsObserver = new SettingsObserver(this.mHandler, 1);
        this.mSettingsObserver.observe(this.mContext);
        Log.d(TAG, "add GPS time sync handler and looper");
        this.mGpsThread = new HandlerThread(TAG);
        this.mGpsThread.start();
        this.mGpsHandler = new MyHandler(this.mGpsThread.getLooper());
        this.mGpsTimeSyncObserver = new GpsTimeSyncObserver(this.mGpsHandler, 4);
        this.mGpsTimeSyncObserver.observe(this.mContext);
    }

    private void checkSystemTime() {
        String key = "persist.sys.device_first_boot";
        boolean isFirstBoot = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.sys.device_first_boot", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON));
        long cur = System.currentTimeMillis();
        this.mFirstBoot = isFirstBoot;
        if (isFirstBoot || cur < 50000000) {
            SystemProperties.set("persist.sys.device_first_boot", "0");
            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(SystemProperties.get("ro.build.date.YmdHM", "2017").substring(0, 4)), 0, 1, 0, 0, 0);
            long destinationTime = c.getTimeInMillis();
            Log.w(TAG, "cur [" + cur + "]" + "  destinationTime [" + destinationTime + "]");
            SystemClock.setCurrentTimeMillis(SystemClock.elapsedRealtime() + destinationTime);
            this.mAlarmManager.setTime(System.currentTimeMillis());
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

    private void registerForConnectivityIntents() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction(WIFI_AVAILABLE_ACTION);
        this.mContext.registerReceiver(this.mConnectivityReceiver, intentFilter);
    }

    private void onPollNetworkTime(int event) {
        Log.d(TAG, "onPollNetworkTime start");
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
            Log.d(TAG, "System time = " + currentTime + " event = " + event);
            if (this.mLastNtpFetchTime == -1 || refTime >= this.mLastNtpFetchTime + this.mPollingIntervalMs || event == 1) {
                Log.d(TAG, "Before Ntp fetch");
                if (this.mTime.getCacheAge() >= this.mPollingIntervalMs || event == 1) {
                    int index = this.mTryAgainCounter % this.mNtpServers.size();
                    Log.d(TAG, "mTryAgainCounter = " + this.mTryAgainCounter + ";mNtpServers.size() = " + this.mNtpServers.size() + ";index = " + index + ";mNtpServers = " + ((String) this.mNtpServers.get(index)));
                    if (this.mTime instanceof NtpTrustedTime) {
                        ((NtpTrustedTime) this.mTime).setServer((String) this.mNtpServers.get(index));
                        this.mTime.forceRefresh();
                        ((NtpTrustedTime) this.mTime).setServer(this.mDefaultServer);
                    } else if (!this.mTime.forceRefresh()) {
                        Log.d(TAG, "forceRefresh failed !");
                    }
                }
                if (this.mTime.getCacheAge() < this.mPollingIntervalMs) {
                    long ntp = this.mTime.currentTimeMillis();
                    this.mTryAgainCounter = 0;
                    if (Math.abs(ntp - currentTime) > ((long) this.mTimeErrorThresholdMs) || this.mLastNtpFetchTime == -1) {
                        if (this.mLastNtpFetchTime == -1 && Math.abs(ntp - currentTime) <= ((long) this.mTimeErrorThresholdMs)) {
                            Log.d(TAG, "For initial setup, rtc = " + currentTime);
                        }
                        Log.d(TAG, "Ntp time to be set = " + ntp);
                        if (ntp / 1000 < 2147483647L) {
                            SystemClock.setCurrentTimeMillis(ntp);
                        }
                    } else {
                        Log.d(TAG, "Ntp time is close enough = " + ntp);
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
        resetAlarm(this.mPollingIntervalMs);
    }

    private void resetAlarm(long interval) {
        this.mAlarmManager.cancel(this.mPendingPollIntent);
        this.mAlarmManager.setExact(3, SystemClock.elapsedRealtime() + interval, this.mPendingPollIntent);
    }

    private boolean isAutomaticTimeRequested() {
        if (Global.getInt(this.mContext.getContentResolver(), "auto_time", 0) != 0) {
            return true;
        }
        return false;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump NetworkTimeUpdateService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
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

    private boolean getGpsTimeSyncState() {
        boolean z = false;
        try {
            if (System.getInt(this.mContext.getContentResolver(), "auto_time_gps") > 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    public void onGpsTimeChanged(boolean enable) {
        if (enable) {
            startUsingGpsWithTimeout(180000, this.mContext.getString(134545594));
        } else if (this.mGpsTimerThread != null) {
            this.mGpsTimerThread.interrupt();
        }
    }

    public void startUsingGpsWithTimeout(final int milliseconds, final String timeoutMsg) {
        if (this.mIsGpsTimeSyncRunning) {
            Log.d(TAG, "WARNING: Gps Time Sync is already run");
            return;
        }
        this.mIsGpsTimeSyncRunning = true;
        Log.d(TAG, "start using GPS for GPS time sync timeout=" + milliseconds + " timeoutMsg=" + timeoutMsg);
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mLocationManager.requestLocationUpdates("gps", 1000, OppoBrightUtils.MIN_LUX_LIMITI, this.mLocationListener);
        this.mGpsTimerThread = new Thread() {
            public void run() {
                boolean isTimeout = false;
                try {
                    Thread.sleep((long) milliseconds);
                    isTimeout = true;
                } catch (InterruptedException e) {
                }
                Log.d(NetworkTimeUpdateService.TAG, "isTimeout=" + isTimeout);
                if (isTimeout) {
                    Message m = new Message();
                    m.obj = timeoutMsg;
                    NetworkTimeUpdateService.this.mGpsToastHandler.sendMessage(m);
                }
                NetworkTimeUpdateService.this.mLocationManager.removeUpdates(NetworkTimeUpdateService.this.mLocationListener);
                NetworkTimeUpdateService.this.mIsGpsTimeSyncRunning = false;
            }
        };
        this.mGpsTimerThread.start();
    }
}

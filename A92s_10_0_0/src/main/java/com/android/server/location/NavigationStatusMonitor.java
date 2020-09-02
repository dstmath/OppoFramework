package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.server.location.OppoMotionDetector;

public class NavigationStatusMonitor extends OppoNavigationStatusMonitor implements OppoMotionDetector.DeviceIdleCallback {
    private static final int ACTION_AR_MOVING = 1;
    private static final int ACTION_AR_STILL = 0;
    private static final long DO_MONITOR_DELAY_TIME = 1000;
    private static final int MIN_DIS_NAV_DIST_NUM = 300;
    private static final int MIN_INDOOR_DIST_NUM = 60;
    private static final int MIN_INDOOR_RESET_NUM = 4;
    private static final int MIN_NAV_DIST_NUM = 20;
    private static final int MIN_NAV_RESET_NUM = 5;
    private static final int MIN_OUTDOOR_DIST_NUM = 10;
    private static final float MIN_OUTDOOR_SNR = 8.0f;
    private static final int MIN_OUTDOOR_SNR_NUM = 4;
    private static final int MIN_USED_SATELLITES_NUM = 4;
    private static final float MIN_USED_SATELLITES_SNR = 18.0f;
    private static final float MIN_VEHICLE_SPEED = 20.0f;
    private static final float MIN_WALK_SPEED = 2.0f;
    private static final int MSG_DOING_MONITOR = 103;
    private static final int MSG_START_MONITOR = 101;
    private static final int MSG_STOP_MONITOR = 102;
    public static final int NAVIGATION_STATUS_OFF = 2;
    public static final int NAVIGATION_STATUS_ON = 1;
    private static final String PASSIVE_PROVIDER = "passive";
    private static final int PRINT_LOG_FREQ = 10;
    private static final String TAG = "NavigationStatusMonitor";
    /* access modifiers changed from: private */
    public static final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private static int sCurNavigationStatus = 1;
    private static boolean sIsNavNow = false;
    private static volatile NavigationStatusMonitor sNavigationStatusMonitor;
    private boolean DEBUG = false;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.NavigationStatusMonitor.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    NavigationStatusMonitor.this.doScreenOn();
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    NavigationStatusMonitor.this.doScreenOff();
                }
            }
        }
    };
    private Context mContext = null;
    private int mCurrArStatus = 0;
    private int mDisNavTimer = 0;
    /* access modifiers changed from: private */
    public GpsStatus mGpsStatus = null;
    private GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {
        /* class com.android.server.location.NavigationStatusMonitor.AnonymousClass1 */

        public void onGpsStatusChanged(int event) {
            NavigationStatusMonitor navigationStatusMonitor = NavigationStatusMonitor.this;
            GpsStatus unused = navigationStatusMonitor.mGpsStatus = navigationStatusMonitor.mLocMgr.getGpsStatus(null);
        }
    };
    private Handler mHandler = new Handler() {
        /* class com.android.server.location.NavigationStatusMonitor.AnonymousClass3 */

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NavigationStatusMonitor.MSG_START_MONITOR /*{ENCODED_INT: 101}*/:
                    if (hasMessages(NavigationStatusMonitor.MSG_DOING_MONITOR)) {
                        removeMessages(NavigationStatusMonitor.MSG_DOING_MONITOR);
                    }
                    sendEmptyMessage(NavigationStatusMonitor.MSG_DOING_MONITOR);
                    return;
                case NavigationStatusMonitor.MSG_STOP_MONITOR /*{ENCODED_INT: 102}*/:
                    removeMessages(NavigationStatusMonitor.MSG_DOING_MONITOR);
                    return;
                case NavigationStatusMonitor.MSG_DOING_MONITOR /*{ENCODED_INT: 103}*/:
                    NavigationStatusMonitor.this.doMonitor();
                    sendEmptyMessageDelayed(NavigationStatusMonitor.MSG_DOING_MONITOR, NavigationStatusMonitor.DO_MONITOR_DELAY_TIME);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHasStart = false;
    private int mIndoorTimer = 0;
    private int mLastArStatus = 0;
    /* access modifiers changed from: private */
    public LocationManager mLocMgr = null;
    private LocationListener mLocationListener = new LocationListener() {
        /* class com.android.server.location.NavigationStatusMonitor.AnonymousClass2 */

        public void onLocationChanged(Location location) {
            if (location.hasSpeed()) {
                synchronized (NavigationStatusMonitor.mLock) {
                    float unused = NavigationStatusMonitor.this.mSpeed = (float) (((double) location.getSpeed()) * 3.6d);
                }
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
    private int mNavTimer = 0;
    private NavigationStatusListener mNavigationListener = null;
    private OppoMotionDetector mOppoMotionDetector;
    private int mOutdoorTimer = 0;
    private SensorManager mSensorManager = null;
    /* access modifiers changed from: private */
    public float mSpeed = 0.0f;

    private NavigationStatusMonitor(Context context, NavigationStatusListener listener) {
        this.mContext = context;
        this.mNavigationListener = listener;
    }

    public static NavigationStatusMonitor getInstance(Context context, NavigationStatusListener listener) {
        if (sNavigationStatusMonitor == null) {
            synchronized (NavigationStatusLightMonitor.class) {
                if (sNavigationStatusMonitor == null) {
                    sNavigationStatusMonitor = new NavigationStatusMonitor(context, listener);
                }
            }
        }
        return sNavigationStatusMonitor;
    }

    @Override // com.android.server.location.OppoNavigationStatusMonitor
    public void startMonitor() {
        synchronized (mLock) {
            this.mHasStart = true;
        }
        resetStatus();
        this.mOppoMotionDetector.startMotion();
        this.mHandler.sendEmptyMessage(MSG_START_MONITOR);
    }

    @Override // com.android.server.location.OppoNavigationStatusMonitor
    public void stopMonitor() {
        synchronized (mLock) {
            this.mHasStart = false;
        }
        this.mHandler.sendEmptyMessage(MSG_STOP_MONITOR);
        this.mOppoMotionDetector.stopMotion();
        tearDown();
        OppoLocationStatistics.getInstance().recordGnssPowerSaveStopped(2);
    }

    @Override // com.android.server.location.OppoNavigationStatusMonitor
    public void resetStatus() {
        this.mIndoorTimer = 0;
        this.mOutdoorTimer = 0;
        this.mNavTimer = 0;
        this.mDisNavTimer = 0;
        sIsNavNow = false;
        synchronized (mLock) {
            sCurNavigationStatus = 1;
        }
        this.mLocMgr.requestLocationUpdates(PASSIVE_PROVIDER, DO_MONITOR_DELAY_TIME, 0.0f, this.mLocationListener);
    }

    private void tearDown() {
        this.mIndoorTimer = 0;
        this.mOutdoorTimer = 0;
        this.mNavTimer = 0;
        this.mDisNavTimer = 0;
        sIsNavNow = false;
        synchronized (mLock) {
            sCurNavigationStatus = 2;
        }
        this.mLocMgr.removeUpdates(this.mLocationListener);
    }

    @Override // com.android.server.location.OppoNavigationStatusMonitor
    public int getNavigateMode() {
        int navigationStatus;
        synchronized (mLock) {
            navigationStatus = sCurNavigationStatus;
        }
        if (sIsNavNow) {
            return 1;
        }
        return navigationStatus;
    }

    @Override // com.android.server.location.OppoNavigationStatusMonitor
    public void setDebug(boolean isDebug) {
        this.DEBUG = isDebug;
        if (this.mOppoMotionDetector != null) {
            OppoMotionDetector.setDebug(this.DEBUG);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.OppoNavigationStatusMonitor
    public void init(OppoMotionConfig motionConfig) {
        this.mLocMgr = (LocationManager) this.mContext.getSystemService("location");
        this.mLocMgr.addGpsStatusListener(this.mGpsStatusListener);
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mOppoMotionDetector = new OppoMotionDetector(this.mSensorManager, this, this.mHandler, motionConfig, DO_MONITOR_DELAY_TIME);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        return;
     */
    public void doMonitor() {
        synchronized (mLock) {
            if (1 == sCurNavigationStatus) {
                isNavigateMode(this.mSpeed);
                if (isGnssDataCredible()) {
                    if ((this.mSpeed > MIN_WALK_SPEED) && this.mCurrArStatus == 0) {
                        this.mCurrArStatus = 1;
                        printLog("change to curr status " + this.mCurrArStatus);
                    }
                }
                this.mSpeed = 0.0f;
                if (1 == this.mCurrArStatus) {
                    this.mCurrArStatus = 1;
                    if (this.mLastArStatus == 0) {
                        this.mIndoorTimer = 0;
                        this.mLastArStatus = 1;
                    }
                } else if (isIndoorMode()) {
                    printLog("change navigation status to NAVIGATION_STATUS_OFF <--");
                    sCurNavigationStatus = 2;
                    this.mNavigationListener.onNavigationStatusChanged(2);
                    OppoLocationStatistics.getInstance().recordGnssPowerSaveStarted(1);
                }
            } else if (2 == sCurNavigationStatus && this.mHasStart && 1 == this.mCurrArStatus) {
                printLog("change navigation status to NAVIGATION_STATUS_ON -->");
                sCurNavigationStatus = 1;
                this.mNavigationListener.onNavigationStatusChanged(1);
                OppoLocationStatistics.getInstance().recordGnssPowerSaveStopped(1);
            }
        }
    }

    /* access modifiers changed from: private */
    public void doScreenOn() {
        synchronized (mLock) {
            if (this.mHasStart && this.mCurrArStatus == 0 && 2 == sCurNavigationStatus) {
                printLog("SCREEN_ON : change to NAVIGATION_STATUS_ON <--");
                resetStatus();
                this.mNavigationListener.onNavigationStatusChanged(1);
            }
        }
    }

    /* access modifiers changed from: private */
    public void doScreenOff() {
        synchronized (mLock) {
            if (!sIsNavNow && this.mHasStart && this.mCurrArStatus == 0 && 1 == sCurNavigationStatus && !isGnssDataCredible()) {
                printLog("SCREEN_OFF : change to NAVIGATION_STATUS_OFF <--");
                sCurNavigationStatus = 2;
                this.mNavigationListener.onNavigationStatusChanged(2);
            }
        }
    }

    public void doFlpSessionOn() {
        synchronized (mLock) {
            if (this.mHasStart && this.mCurrArStatus == 0 && 2 == sCurNavigationStatus) {
                printLog("FLP_SESSION_ON : change to NAVIGATION_STATUS_ON <--");
                resetStatus();
                this.mNavigationListener.onNavigationStatusChanged(1);
            }
        }
    }

    private boolean isGnssDataCredible() {
        GpsStatus gpsStatus = this.mGpsStatus;
        if (gpsStatus == null) {
            return false;
        }
        int usedNum = 0;
        for (GpsSatellite satellite : gpsStatus.getSatellites()) {
            if (satellite.usedInFix() && MIN_USED_SATELLITES_SNR < satellite.getSnr() && 4 <= (usedNum = usedNum + 1)) {
                return true;
            }
        }
        return false;
    }

    private boolean isIndoorStatus() {
        boolean isIndoorMode = true;
        GpsStatus gpsStatus = this.mGpsStatus;
        if (gpsStatus != null) {
            int outNum = 0;
            for (GpsSatellite satellite : gpsStatus.getSatellites()) {
                if (MIN_OUTDOOR_SNR < satellite.getSnr() && 4 <= (outNum = outNum + 1)) {
                    isIndoorMode = false;
                }
            }
        }
        return isIndoorMode;
    }

    private boolean isIndoorMode() {
        int i;
        boolean isIndoor = false;
        if (isIndoorStatus()) {
            this.mIndoorTimer++;
            int i2 = this.mIndoorTimer;
            if (4 <= i2) {
                this.mOutdoorTimer = 0;
                if (MIN_INDOOR_DIST_NUM <= i2) {
                    isIndoor = true;
                }
            }
        } else {
            this.mOutdoorTimer++;
            int i3 = this.mOutdoorTimer;
            if (4 <= i3) {
                this.mIndoorTimer = 0;
                if (10 <= i3) {
                    isIndoor = false;
                }
            }
        }
        int i4 = this.mIndoorTimer;
        if ((i4 > 0 && i4 % 10 == 0) || ((i = this.mOutdoorTimer) > 0 && i % 10 == 0)) {
            printLog("isIndoorMode  mIndoorTimer = " + this.mIndoorTimer + ", mOutdoorTimer = " + this.mOutdoorTimer);
        }
        return isIndoor;
    }

    private boolean isNavigateMode(float speed) {
        int i;
        boolean isNavigate = sIsNavNow;
        if (isNavigateStatus(speed)) {
            this.mNavTimer++;
            int i2 = this.mNavTimer;
            if (MIN_NAV_RESET_NUM <= i2) {
                this.mDisNavTimer = 0;
                if (!sIsNavNow && MIN_NAV_DIST_NUM <= i2) {
                    printLog("Change to Navigation status!!");
                    isNavigate = true;
                    sIsNavNow = true;
                }
            }
        } else {
            synchronized (mLock) {
                if (!sIsNavNow || 1 != this.mCurrArStatus) {
                    this.mDisNavTimer++;
                    if (MIN_NAV_RESET_NUM <= this.mDisNavTimer) {
                        this.mNavTimer = 0;
                    }
                    if (sIsNavNow && MIN_DIS_NAV_DIST_NUM <= this.mDisNavTimer) {
                        printLog("Change to disNavigation status!!");
                        isNavigate = false;
                        sIsNavNow = false;
                    }
                } else if (this.mDisNavTimer != 0) {
                    printLog("Maybe in a tunnel!!");
                    this.mDisNavTimer = 0;
                }
            }
        }
        int i3 = this.mNavTimer;
        if ((i3 > 0 && i3 % 10 == 0 && MIN_NAV_DIST_NUM >= i3) || ((i = this.mDisNavTimer) > 0 && i % 10 == 0)) {
            printLog("isNavigateMode NavTime = " + this.mNavTimer + ", disNavTime = " + this.mDisNavTimer + ", mode = " + isNavigate);
        }
        return isNavigate;
    }

    private boolean isNavigateStatus(float speed) {
        return MIN_VEHICLE_SPEED < speed;
    }

    private void printLog(String log) {
        if (this.DEBUG) {
            Log.d(TAG, log);
        }
    }

    @Override // com.android.server.location.OppoMotionDetector.DeviceIdleCallback
    public void onAnyMotionResult(int result) {
        synchronized (mLock) {
            if (result == 0) {
                if (1 == this.mCurrArStatus) {
                    this.mLastArStatus = 1;
                    this.mCurrArStatus = 0;
                    if (this.DEBUG) {
                        printLog("Switch to still status!!");
                    }
                }
            } else if (this.mCurrArStatus == 0) {
                this.mLastArStatus = 0;
                this.mCurrArStatus = 1;
                if (this.DEBUG) {
                    printLog("Switch to moving status!!");
                }
            }
        }
    }
}

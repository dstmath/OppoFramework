package com.android.server.location;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.server.location.OppoMotionDetector;

public class NavigationStatusLightMonitor extends OppoNavigationStatusMonitor implements OppoMotionDetector.DeviceIdleCallback {
    private static final int ACTION_AR_MOVING = 1;
    private static final int ACTION_AR_STILL = 0;
    private static final int DEFAULT_INT_ZERO = 0;
    private static final long DEFAULT_LONG_ZERO = 0;
    private static final long DO_MONITOR_DELAY_TIME = 1000;
    private static final String KEY_HARDWARE_PROPERTIES = "ro.hardware";
    private static final long MAX_ONTRIGGER_DELAY_MTK = 8000;
    private static final long MAX_ONTRIGGER_DELAY_QCOM = 6900;
    private static final int MIN_INDOOR_DIST_NUM = 60;
    private static final int MIN_INDOOR_RESET_NUM = 4;
    private static final int MIN_OUTDOOR_RESET_NUM = 2;
    private static final float MIN_OUTDOOR_SNR = 8.0f;
    private static final int MIN_OUTDOOR_SNR_NUM = 4;
    private static final long MOTION_DETECTOR_DELAY_TIME = 1000;
    private static final int MSG_DOING_MONITOR = 103;
    private static final int MSG_ON_MOTION_MOVING = 104;
    private static final int MSG_ON_SENSOR_TRIGGER = 105;
    private static final int MSG_START_MONITOR = 101;
    private static final int MSG_STOP_MONITOR = 102;
    private static final String MTK_PLATFORM = "mtk";
    public static final int NAVIGATION_STATUS_OFF = 2;
    public static final int NAVIGATION_STATUS_ON = 1;
    private static final int PRINT_LOG_FREQ = 10;
    private static final String QCOM_PLATFORM = "qcom";
    private static final int STATIONARY_DETECT_DELAY_MTK = 6;
    private static final int STATIONARY_DETECT_DELAY_QCOM = 5;
    private static final int STOP_NAVIGATE_TRIGGER_NUM_MTK = 10;
    private static final int STOP_NAVIGATE_TRIGGER_NUM_QCOM = 12;
    private static final String TAG = "NavigationStatusMonitor";
    private static final String UNKNOWN_PLATFORM = "unknown";
    private static final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private static int sCurNavigationStatus = 2;
    private static boolean sHasStart = false;
    private static volatile NavigationStatusLightMonitor sNavigationStatusLightMonitor;
    private boolean DEBUG = false;
    private Context mContext = null;
    private int mCurrArStatus = 0;
    private GpsStatus mGpsStatus = null;
    private GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {
        /* class com.android.server.location.NavigationStatusLightMonitor.AnonymousClass1 */

        public void onGpsStatusChanged(int event) {
            NavigationStatusLightMonitor navigationStatusLightMonitor = NavigationStatusLightMonitor.this;
            navigationStatusLightMonitor.mGpsStatus = navigationStatusLightMonitor.mLocMgr.getGpsStatus(null);
        }
    };
    private Handler mHandler = new Handler() {
        /* class com.android.server.location.NavigationStatusLightMonitor.AnonymousClass2 */

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NavigationStatusLightMonitor.MSG_START_MONITOR /* 101 */:
                    boolean unused = NavigationStatusLightMonitor.sHasStart = true;
                    NavigationStatusLightMonitor.this.resetStatus();
                    if (hasMessages(NavigationStatusLightMonitor.MSG_DOING_MONITOR)) {
                        removeMessages(NavigationStatusLightMonitor.MSG_DOING_MONITOR);
                    }
                    sendEmptyMessage(NavigationStatusLightMonitor.MSG_DOING_MONITOR);
                    return;
                case NavigationStatusLightMonitor.MSG_STOP_MONITOR /* 102 */:
                    removeMessages(NavigationStatusLightMonitor.MSG_DOING_MONITOR);
                    boolean unused2 = NavigationStatusLightMonitor.sHasStart = false;
                    if (NavigationStatusLightMonitor.this.mMotionDetectorHasStart) {
                        NavigationStatusLightMonitor.this.mOppoMotionDetector.stopMotion();
                    }
                    if (NavigationStatusLightMonitor.this.mSensorHasRegistered) {
                        NavigationStatusLightMonitor.this.mStationaryListener.unregisterListener();
                    }
                    NavigationStatusLightMonitor.this.tearDown();
                    OppoLocationStatistics.getInstance().recordGnssPowerSaveStopped(2);
                    return;
                case NavigationStatusLightMonitor.MSG_DOING_MONITOR /* 103 */:
                    NavigationStatusLightMonitor.this.doMonitor();
                    sendEmptyMessageDelayed(NavigationStatusLightMonitor.MSG_DOING_MONITOR, 1000);
                    return;
                case NavigationStatusLightMonitor.MSG_ON_MOTION_MOVING /* 104 */:
                    if (NavigationStatusLightMonitor.this.mMotionDetectorHasStart) {
                        NavigationStatusLightMonitor.this.printLog("Status now is not stationary, restart to navigate");
                        NavigationStatusLightMonitor.this.mCurrArStatus = 1;
                        NavigationStatusLightMonitor.this.mOppoMotionDetector.stopMotion();
                        NavigationStatusLightMonitor.this.mMotionDetectorHasStart = false;
                        return;
                    }
                    return;
                case NavigationStatusLightMonitor.MSG_ON_SENSOR_TRIGGER /* 105 */:
                    NavigationStatusLightMonitor.this.onStationaryTrigger();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHasFirstRegistered = false;
    private int mIndoorTimer = 0;
    private long mLastStationarySensorRegiterTime = DEFAULT_LONG_ZERO;
    private LocationManager mLocMgr = null;
    private boolean mMotionDetectorHasStart = false;
    private NavigationStatusListener mNavigationListener = null;
    private OppoMotionDetector mOppoMotionDetector = null;
    private int mOutdoorTimer = 0;
    private String mPlatformRecord = null;
    private boolean mSensorHasRegistered = false;
    private SensorManager mSensorManager = null;
    private Sensor mStationaryDetectSensor = null;
    private StationaryListener mStationaryListener = new StationaryListener();
    private int mStationaryTriggerNum = 0;

    private NavigationStatusLightMonitor(Context context, NavigationStatusListener listener) {
        this.mContext = context;
        this.mNavigationListener = listener;
    }

    public static NavigationStatusLightMonitor getInstance(Context context, NavigationStatusListener listener) {
        if (sNavigationStatusLightMonitor == null) {
            synchronized (NavigationStatusLightMonitor.class) {
                if (sNavigationStatusLightMonitor == null) {
                    sNavigationStatusLightMonitor = new NavigationStatusLightMonitor(context, listener);
                }
            }
        }
        return sNavigationStatusLightMonitor;
    }

    @Override // com.android.server.location.OppoNavigationStatusMonitor
    public void startMonitor() {
        this.mHandler.sendEmptyMessage(MSG_START_MONITOR);
    }

    @Override // com.android.server.location.OppoNavigationStatusMonitor
    public void stopMonitor() {
        this.mHandler.sendEmptyMessage(MSG_STOP_MONITOR);
    }

    @Override // com.android.server.location.OppoNavigationStatusMonitor
    public void resetStatus() {
        synchronized (mLock) {
            sCurNavigationStatus = 1;
        }
        this.mIndoorTimer = 0;
        this.mOutdoorTimer = 0;
        resetToMonitor();
    }

    private void resetToMonitor() {
        printLog("resetToMonitor");
        this.mStationaryTriggerNum = 0;
        this.mCurrArStatus = 0;
        if (this.mSensorHasRegistered || this.mHasFirstRegistered) {
            if (this.mStationaryListener != null) {
                printLog("Reset : unregister listener");
                this.mStationaryListener.unregisterListener();
            }
            this.mSensorHasRegistered = false;
            this.mHasFirstRegistered = false;
        }
        if (this.mMotionDetectorHasStart) {
            if (this.mOppoMotionDetector != null) {
                printLog("Reset : stop motion detector");
                this.mOppoMotionDetector.stopMotion();
            }
            this.mMotionDetectorHasStart = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tearDown() {
        synchronized (mLock) {
            sCurNavigationStatus = 2;
        }
        this.mIndoorTimer = 0;
        this.mOutdoorTimer = 0;
        resetToMonitor();
    }

    @Override // com.android.server.location.OppoNavigationStatusMonitor
    public int getNavigateMode() {
        int navigationStatus = 1;
        if (sHasStart) {
            synchronized (mLock) {
                navigationStatus = sCurNavigationStatus;
            }
        }
        return navigationStatus;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.OppoNavigationStatusMonitor
    public void init(OppoMotionConfig motionConfig) {
        printLog("NavigationStatusLightMonitor : init");
        this.mLocMgr = (LocationManager) this.mContext.getSystemService("location");
        this.mLocMgr.addGpsStatusListener(this.mGpsStatusListener);
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        String platfRecord = getPlatform();
        if (!(platfRecord == null || platfRecord.length() == 0)) {
            if (MTK_PLATFORM.equals(platfRecord)) {
                for (Sensor sensor : this.mSensorManager.getSensorList(-1)) {
                    if (sensor.getName().equals("STATIONARY_DETECT")) {
                        printLog("get stationary detect sensor success !!");
                        this.mStationaryDetectSensor = sensor;
                    }
                }
            } else if (QCOM_PLATFORM.equals(platfRecord)) {
                printLog("get stationary detect sensor success !");
                this.mStationaryDetectSensor = this.mSensorManager.getDefaultSensor(29, false);
            }
        }
        this.mOppoMotionDetector = new OppoMotionDetector(this.mSensorManager, this, this.mHandler, motionConfig, 1000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doMonitor() {
        synchronized (mLock) {
            if (!sHasStart || 1 != sCurNavigationStatus) {
                if (sHasStart && 2 == sCurNavigationStatus && 1 == this.mCurrArStatus) {
                    printLog("Is moving now, start to navigate");
                    startNavigate();
                    resetToMonitor();
                }
            } else if (isIndoorMode()) {
                if (!this.mHasFirstRegistered && this.mStationaryDetectSensor != null && !this.mSensorHasRegistered) {
                    this.mSensorHasRegistered = this.mStationaryListener.registerListener();
                    this.mHasFirstRegistered = true;
                }
            } else if (2 == this.mOutdoorTimer) {
                resetToMonitor();
            }
        }
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
            if (4 <= this.mIndoorTimer) {
                isIndoor = true;
                this.mOutdoorTimer = 0;
            }
        } else {
            this.mOutdoorTimer++;
            if (2 <= this.mOutdoorTimer) {
                isIndoor = false;
                this.mIndoorTimer = 0;
            }
        }
        int i2 = this.mIndoorTimer;
        if ((i2 > 0 && i2 % 10 == 0) || ((i = this.mOutdoorTimer) > 0 && i % 10 == 0)) {
            printLog("isIndoorMode  mIndoorTimer = " + this.mIndoorTimer + ", mOutdoorTimer = " + this.mOutdoorTimer);
        }
        return isIndoor;
    }

    private void startNavigate() {
        printLog("change navigation status to NAVIGATION_STATUS_ON -->");
        synchronized (mLock) {
            sCurNavigationStatus = 1;
        }
        this.mNavigationListener.onNavigationStatusChanged(1);
        OppoLocationStatistics.getInstance().recordGnssPowerSaveStopped(2);
    }

    private void stopNavigate() {
        if (!this.mMotionDetectorHasStart) {
            printLog("change navigation status to NAVIGATION_STATUS_OFF <--");
            synchronized (mLock) {
                sCurNavigationStatus = 2;
            }
            this.mNavigationListener.onNavigationStatusChanged(2);
            OppoLocationStatistics.getInstance().recordGnssPowerSaveStarted(2);
            this.mOppoMotionDetector.startMotion();
            this.mMotionDetectorHasStart = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onStationaryTrigger() {
        this.mSensorHasRegistered = false;
        if (this.mLastStationarySensorRegiterTime <= DEFAULT_LONG_ZERO) {
            printLog("Timing failure");
            return;
        }
        int i = this.mStationaryTriggerNum;
        if (i > 0) {
            long onTriggerDuration = SystemClock.elapsedRealtime() - this.mLastStationarySensorRegiterTime;
            String platfRecord = getPlatform();
            if (platfRecord != null && platfRecord.length() != 0) {
                if (MTK_PLATFORM.equals(platfRecord)) {
                    if (onTriggerDuration < MAX_ONTRIGGER_DELAY_MTK) {
                        this.mStationaryTriggerNum++;
                        printLog("Sensor trigger num is : " + this.mStationaryTriggerNum);
                        if (10 == this.mStationaryTriggerNum) {
                            printLog("Sensor trigger num equals stop times, stop navigate now");
                            stopNavigate();
                            this.mCurrArStatus = 0;
                            this.mStationaryTriggerNum = 0;
                            return;
                        }
                        this.mSensorHasRegistered = this.mStationaryListener.registerListener();
                        printLog("Restart to register a stationary sensor");
                        return;
                    }
                    printLog("Trigger time is greater than MAX_ONTRIGGER_DELAY, restore the sensor monitor");
                    resetToMonitor();
                } else if (!QCOM_PLATFORM.equals(platfRecord)) {
                } else {
                    if (onTriggerDuration < MAX_ONTRIGGER_DELAY_QCOM) {
                        this.mStationaryTriggerNum++;
                        printLog("Sensor trigger num is : " + this.mStationaryTriggerNum);
                        if (STOP_NAVIGATE_TRIGGER_NUM_QCOM == this.mStationaryTriggerNum) {
                            printLog("Sensor trigger num equals stop times, stop navigate now");
                            stopNavigate();
                            this.mCurrArStatus = 0;
                            this.mStationaryTriggerNum = 0;
                            return;
                        }
                        this.mSensorHasRegistered = this.mStationaryListener.registerListener();
                        printLog("Restart to register a stationary sensor");
                        return;
                    }
                    printLog("Trigger time is greater than MAX_ONTRIGGER_DELAY, restore the sensor monitor");
                    resetToMonitor();
                }
            }
        } else if (i == 0) {
            this.mStationaryTriggerNum = i + 1;
            this.mSensorHasRegistered = this.mStationaryListener.registerListener();
            printLog("Firstly registered sensor is on trigger");
        }
    }

    /* access modifiers changed from: private */
    public final class StationaryListener extends TriggerEventListener {
        private StationaryListener() {
        }

        public void onTrigger(TriggerEvent event) {
            NavigationStatusLightMonitor.this.printLog("Stationary_detect sensor is on trigger");
            NavigationStatusLightMonitor.this.mHandler.sendEmptyMessage(NavigationStatusLightMonitor.MSG_ON_SENSOR_TRIGGER);
        }

        public boolean registerListener() {
            boolean registerSuccess = false;
            if (NavigationStatusLightMonitor.this.mSensorManager != null) {
                registerSuccess = NavigationStatusLightMonitor.this.mSensorManager.requestTriggerSensor(NavigationStatusLightMonitor.this.mStationaryListener, NavigationStatusLightMonitor.this.mStationaryDetectSensor);
            }
            if (registerSuccess) {
                NavigationStatusLightMonitor.this.mLastStationarySensorRegiterTime = SystemClock.elapsedRealtime();
                NavigationStatusLightMonitor.this.printLog("Stationary_detect sensor register success");
            }
            return registerSuccess;
        }

        public boolean unregisterListener() {
            boolean unregisterSuccess = false;
            if (NavigationStatusLightMonitor.this.mSensorManager != null) {
                unregisterSuccess = NavigationStatusLightMonitor.this.mSensorManager.cancelTriggerSensor(NavigationStatusLightMonitor.this.mStationaryListener, NavigationStatusLightMonitor.this.mStationaryDetectSensor);
            }
            if (unregisterSuccess) {
                NavigationStatusLightMonitor.this.mLastStationarySensorRegiterTime = NavigationStatusLightMonitor.DEFAULT_LONG_ZERO;
                NavigationStatusLightMonitor.this.printLog("Stationary_detect sensor unregister");
            }
            return unregisterSuccess;
        }
    }

    @Override // com.android.server.location.OppoMotionDetector.DeviceIdleCallback
    public void onAnyMotionResult(int result) {
        if (result != 0) {
            this.mHandler.sendEmptyMessage(MSG_ON_MOTION_MOVING);
        }
    }

    @Override // com.android.server.location.OppoNavigationStatusMonitor
    public void setDebug(boolean isDebug) {
        this.DEBUG = isDebug;
        if (this.mOppoMotionDetector != null) {
            OppoMotionDetector.setDebug(this.DEBUG);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void printLog(String log) {
        if (this.DEBUG) {
            Log.d(TAG, log);
        }
    }

    private String getPlatform() {
        this.mPlatformRecord = SystemProperties.get(KEY_HARDWARE_PROPERTIES, "0");
        String str = this.mPlatformRecord;
        if (str == null) {
            printLog("get platform failed !");
            return null;
        } else if (str.startsWith("mt")) {
            return MTK_PLATFORM;
        } else {
            if (this.mPlatformRecord.startsWith(QCOM_PLATFORM)) {
                return QCOM_PLATFORM;
            }
            printLog("unknown platform !!");
            return UNKNOWN_PLATFORM;
        }
    }
}

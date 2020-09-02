package com.android.server.location;

import android.content.Context;
import android.location.GnssStatus;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import com.android.server.location.interfaces.IPswNavigationStatusController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class NavigationStatusController implements IPswNavigationStatusController {
    private static boolean DEBUG = false;
    public static final int DUMP_DISABLE_GNSS_POWER_SAVE = 1;
    public static final int DUMP_ENABLE_GNSS_POWER_SAVE = 2;
    private static final String ENG_VERSION_TYPE_PATH = "/proc/oppoVersion/engVersion";
    private static final int FIRST_OPERATOR_VERSION_TYPE = 5;
    private static final String GPS_OPCUSTOM_POWERSAVE_OFF_FEATURE = "oppo.customize.function.gps_powersave_off";
    private static final int GPS_POWER_INTERVAL = 1000;
    private static final String KEY_GNSS_POWER_SAVER_ENABLED = "config_gnssPowerSaverEnabled";
    private static final String KEY_GPS_STATUS_MONITOR_TYPE = "gps_status_monitor_type";
    private static final String KEY_MOTION_INTERVAL = "config_motionInterval";
    private static final String KEY_MOTION_SAMPLE_NUMBER = "config_motionSampleNumber";
    private static final String KEY_MOTION_THRESHOLD_ANGLE = "config_motionThresholdAngle";
    private static final String KEY_MOTION_THRESHOLD_ENERGY = "config_motionThresholdEnergy";
    private static final int LAST_OPERATOR_VERSION_TYPE = 10;
    private static final int LIGHT_MONITOR_TYPE = 2;
    private static final int MSG_CONTROL_START = 103;
    private static final int MSG_CONTROL_STOP = 105;
    private static final int MSG_READ_ENG_VERSION = 104;
    private static final int NAVIGATION_STATUS_OFF = 2;
    private static final int NAVIGATION_STATUS_ON = 1;
    private static final int ORIGINAL_MONITOR_TYPE = 1;
    private static final String TAG = "NavigationStatusController";
    private static final long TIME_DELAY_STOP_GPS = 20000;
    private static NavigationStatusController mInstance = null;
    private final Context mContext;
    private int mDumpDisablePowerSaveType = 0;
    private int mEngVersionType = 0;
    private final GnssLocationProvider mGnssLocationProvider;
    private boolean mGnssPowerEnable = true;
    private GnssStatus.Callback mGnssStatusCallback = new GnssStatus.Callback() {
        /* class com.android.server.location.NavigationStatusController.AnonymousClass3 */

        public void onStarted() {
            NavigationStatusController.this.startController();
        }

        public void onStopped() {
            NavigationStatusController.this.stopController();
        }
    };
    private Handler mHandler = null;
    private Handler.Callback mHandlerCallback = new Handler.Callback() {
        /* class com.android.server.location.NavigationStatusController.AnonymousClass1 */

        public boolean handleMessage(Message msg) {
            int i = msg.what;
            if (i == NavigationStatusController.MSG_CONTROL_START) {
                NavigationStatusController.this.mNavigationMonitor.startMonitor();
                return true;
            } else if (i != NavigationStatusController.MSG_CONTROL_STOP) {
                return true;
            } else {
                NavigationStatusController.this.mNavigationMonitor.stopMonitor();
                return true;
            }
        }
    };
    private boolean mHasStart = false;
    private int mMonitorType = 2;
    private OppoMotionConfig mMotionConfig = null;
    /* access modifiers changed from: private */
    public OppoNavigationStatusMonitor mNavigationMonitor = null;
    /* access modifiers changed from: private */
    public int mNavigationStatus = 1;
    private NavigationStatusListener mNavigationStatusListener = new NavigationStatusListener() {
        /* class com.android.server.location.NavigationStatusController.AnonymousClass2 */

        @Override // com.android.server.location.NavigationStatusListener
        public void onNavigationStatusChanged(int status) {
            if (status == 1) {
                NavigationStatusController.this.printLog("GPL.status NAVIGATION_STATUS_ON");
                NavigationStatusController.this.exitPowerSavingMode();
                int unused = NavigationStatusController.this.mNavigationStatus = 1;
            } else if (status == 2) {
                NavigationStatusController.this.printLog("GPL.status NAVIGATION_STATUS_OFF");
                NavigationStatusController.this.enterPowerSavingMode();
                int unused2 = NavigationStatusController.this.mNavigationStatus = 2;
            }
        }
    };
    private OppoLbsRomUpdateUtil mRomUpdateUtil = null;

    private NavigationStatusController(Context context, GnssLocationProvider provider) {
        this.mContext = context;
        this.mGnssLocationProvider = provider;
        HandlerThread localThread = new HandlerThread(TAG);
        localThread.start();
        this.mHandler = new Handler(localThread.getLooper(), this.mHandlerCallback);
        initValue();
    }

    public static NavigationStatusController getInstance(Context context, GnssLocationProvider provider) {
        Log.d(TAG, "on get NavigationStatusController!");
        if (mInstance == null) {
            Log.d(TAG, "will truely get NavigationStatusController!!");
            mInstance = new NavigationStatusController(context, provider);
        }
        return mInstance;
    }

    private void initValue() {
        this.mRomUpdateUtil = OppoLbsRomUpdateUtil.getInstall(this.mContext);
        this.mGnssPowerEnable = !this.mContext.getPackageManager().hasSystemFeature(GPS_OPCUSTOM_POWERSAVE_OFF_FEATURE);
        if (this.mGnssPowerEnable) {
            this.mGnssPowerEnable = this.mRomUpdateUtil.getBoolean(KEY_GNSS_POWER_SAVER_ENABLED);
        }
        this.mMonitorType = this.mRomUpdateUtil.getInt(KEY_GPS_STATUS_MONITOR_TYPE);
        this.mMotionConfig = new OppoMotionConfig();
        this.mMotionConfig.setSampleNum(this.mRomUpdateUtil.getInt(KEY_MOTION_SAMPLE_NUMBER));
        this.mMotionConfig.setInterval(this.mRomUpdateUtil.getInt(KEY_MOTION_INTERVAL));
        this.mMotionConfig.setThresholdEnergy(this.mRomUpdateUtil.getFloat(KEY_MOTION_THRESHOLD_ENERGY));
        this.mMotionConfig.setThresholdAngle(this.mRomUpdateUtil.getFloat(KEY_MOTION_THRESHOLD_ANGLE));
        checkStatusMonitor();
        this.mHandler.sendEmptyMessage(MSG_READ_ENG_VERSION);
    }

    public void init() {
        OppoNavigationStatusMonitor oppoNavigationStatusMonitor = this.mNavigationMonitor;
        if (oppoNavigationStatusMonitor != null) {
            oppoNavigationStatusMonitor.init(this.mMotionConfig);
            setUp();
        }
    }

    private void checkStatusMonitor() {
        printLog("checkStatusMonitor: status monitor is " + this.mMonitorType);
        int i = this.mMonitorType;
        if (i == 1) {
            this.mNavigationMonitor = NavigationStatusMonitor.getInstance(this.mContext, this.mNavigationStatusListener);
        } else if (i != 2) {
            this.mNavigationMonitor = NavigationStatusLightMonitor.getInstance(this.mContext, this.mNavigationStatusListener);
        } else {
            this.mNavigationMonitor = NavigationStatusLightMonitor.getInstance(this.mContext, this.mNavigationStatusListener);
        }
        this.mNavigationMonitor.init(this.mMotionConfig);
    }

    public void startController() {
        printLog("----startController----");
        if (powerSaveEnabled()) {
            printLog("Status monitor is " + this.mMonitorType);
            setUp();
            this.mHasStart = true;
            this.mHandler.sendEmptyMessage(MSG_CONTROL_START);
        } else if (DEBUG) {
            printLog("----power save disable-----mDumpDisablePowerSaveType:" + this.mDumpDisablePowerSaveType + "mEngVersionType:" + this.mEngVersionType);
        }
    }

    public void stopController() {
        printLog("----stopController----");
        tearDown();
        this.mHasStart = false;
        this.mHandler.sendEmptyMessage(MSG_CONTROL_STOP);
    }

    public void setUp() {
        printLog("Set up the running environment!");
        this.mNavigationStatus = 1;
        this.mNavigationMonitor.resetStatus();
    }

    private void tearDown() {
        printLog("Tear down the running environment!");
        this.mNavigationStatus = 2;
    }

    public void setDebug(boolean isDebug) {
        DEBUG = isDebug;
        OppoNavigationStatusMonitor oppoNavigationStatusMonitor = this.mNavigationMonitor;
        if (oppoNavigationStatusMonitor != null) {
            oppoNavigationStatusMonitor.setDebug(isDebug);
        }
    }

    public boolean resistStartGps() {
        printLog("running resistStartGps mNavigationStatus: " + this.mNavigationStatus);
        return this.mHasStart && 2 == this.mNavigationStatus;
    }

    public int getNavigateMode() {
        return this.mNavigationMonitor.getNavigateMode();
    }

    public void setPowerSaveForDump(int powerSaveType) {
        if (1 == powerSaveType || 2 == powerSaveType) {
            this.mDumpDisablePowerSaveType = powerSaveType;
        }
    }

    public boolean powerSaveEnabled() {
        int i = this.mDumpDisablePowerSaveType;
        if (i == 0) {
            return this.mGnssPowerEnable;
        }
        if (1 == i) {
            return false;
        }
        if (2 == i) {
            return true;
        }
        return true;
    }

    private boolean disablePowerSaveForOperator() {
        int engVersionInt;
        String engVersionString = getSoftwareVersion();
        if (engVersionString == null) {
            return false;
        }
        try {
            engVersionInt = Integer.parseInt(engVersionString);
        } catch (NumberFormatException e) {
            engVersionInt = 0;
            Log.e(TAG, "parse engVersion to int fail :" + 0);
        }
        this.mEngVersionType = engVersionInt;
        if (engVersionInt < FIRST_OPERATOR_VERSION_TYPE || engVersionInt > LAST_OPERATOR_VERSION_TYPE) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0074  */
    private String getSoftwareVersion() {
        StringBuilder sb;
        String engVersionType = null;
        File file = new File(ENG_VERSION_TYPE_PATH);
        BufferedReader reader = null;
        if (!file.exists()) {
            Log.w(TAG, "file not exists : /proc/oppoVersion/engVersion");
            return null;
        }
        try {
            reader = new BufferedReader(new FileReader(file));
            engVersionType = reader.readLine();
            try {
                reader.close();
            } catch (IOException e) {
                e1 = e;
                sb = new StringBuilder();
            }
        } catch (IOException e2) {
            Log.e(TAG, "readFile io exception:" + e2.getMessage());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    e1 = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    Log.e(TAG, "readFile io close exception :" + e1.getMessage());
                }
            }
            throw th;
        }
        if (DEBUG) {
            Log.v(TAG, "readFile path:/proc/oppoVersion/engVersion, result:" + engVersionType);
        }
        return engVersionType;
        sb.append("readFile io close exception :");
        sb.append(e1.getMessage());
        Log.e(TAG, sb.toString());
        if (DEBUG) {
        }
        return engVersionType;
    }

    /* access modifiers changed from: private */
    public void enterPowerSavingMode() {
        GnssLocationProviderWrapper.enterPSMode(this.mGnssLocationProvider);
    }

    /* access modifiers changed from: private */
    public void exitPowerSavingMode() {
        printLog("user wake mode : " + this.mNavigationStatus);
        this.mNavigationStatus = 1;
        GnssLocationProviderWrapper.wakeGps(this.mGnssLocationProvider);
    }

    /* access modifiers changed from: private */
    public void printLog(String log) {
        if (DEBUG) {
            Log.d(TAG, log);
        }
    }
}

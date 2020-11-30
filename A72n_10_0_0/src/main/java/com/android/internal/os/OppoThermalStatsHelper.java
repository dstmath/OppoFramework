package com.android.internal.os;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.OppoBaseBatteryStats;
import android.os.OppoManager;
import android.os.OppoThermalManager;
import android.os.OppoThermalState;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.CalendarContract;
import android.provider.SettingsStringUtil;
import android.telecom.ParcelableCallAnalytics;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Slog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/* access modifiers changed from: package-private */
public class OppoThermalStatsHelper {
    private static final int ALARM_TEMP_UPLOAD = 56;
    private static final String ALARM_WAKEUP = "oppo.android.internal.thermalupload.ALARM_WAKEUP";
    private static final int AUDIOON_CHECK = 52;
    public static final int BATTERY_PLUGGED_NONE = 0;
    private static final int CAMERAON_CHECK = 51;
    private static final String CHARGE_MAP = "id_charge_map";
    private static final int COMMON_WRITE = 63;
    private static final int CONNECTTYPE_CHECK = 58;
    private static final int CPU_IDLE_CHECK_ENVI_COUNT = 3;
    private static final int CPU_IDLE_LESS_LOADING = 100;
    static boolean DEBUG_THERMAL_TEMP = false;
    private static final int DELAY_CHECK = 4000;
    private static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    private static final String EXTRA_VOLUME_STREAM_VALUE = "android.media.EXTRA_VOLUME_STREAM_VALUE";
    private static final int FLASHLIGHTON_CHECK = 55;
    private static final int GPSON_CHECK = 54;
    private static final String HEAT_LOG_ID = "040201";
    private static final int HEAT_REASON_ANALIZY = 59;
    private static final int INIT_THERMAL_PAR = 62;
    private static final int INVALID_DATA = -1023;
    private static final int MAX_HEAT_ANALIZY_SIZE = 400;
    private static final int MAX_HISTORY_BUFFER = 131072;
    static final int MSG_REPORT_UPDATE_CPU = 5;
    private static final int RESET_ALARM = 60;
    private static final int SYNC_TO_THERMAL_FILE = 64;
    public static final String TAG = "OppoThermalStats";
    public static final int THERMAL_EVENT_AUDIO = 3;
    public static final int THERMAL_EVENT_BASE = 1;
    public static final int THERMAL_EVENT_CAMERA = 2;
    public static final int THERMAL_EVENT_FLASH_LIGHT = 6;
    public static final int THERMAL_EVENT_GPS = 5;
    public static final int THERMAL_EVENT_VIDEO = 4;
    private static final String THERMAL_HEAT_EVENT = "id_thermal_heat";
    private static final String THERMAL_INFO_DCS = "/data/oppo/psw/dcs/";
    private static final String THERMAL_MONITOR_APP = "id_thermal_monitor_app";
    private static final String THERMAL_TAG = "20139";
    private static final String THERMAL_TEMP_EVENT = "id_thermal_temp";
    private static final int UPDATE_BRIGHTNESS = 57;
    private static final int UPDATE_VOLUME = 61;
    private static final String UPLOAD_ACTIVITY_BATTERY_RECORD = "activity_battery_record";
    private static final String UPLOAD_LOGTAG = "20089";
    private static final int VIDEOON_CHECK = 53;
    private static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private static final String WAKELOCK_KEY = "thermalUpload";
    private OppoCpuFreqReader cpuFreqReader = new OppoCpuFreqReader();
    private boolean isThermalFreatureOn;
    private AlarmManager mAlarmManager;
    private Map<String, Long> mBatTempMap = new ArrayMap();
    int mBatteryFcc = 0;
    private boolean mBatteryStatsReady = false;
    private long mCaptureCpuFeqElapsRealtime;
    private long mCaptureCpuFeqInterVal = 120000;
    private Map<String, String> mChargeUploadMap = new ArrayMap();
    private Context mContext;
    private String mCpuFreqValues;
    private String mCpuFreqValuesNeedUpload;
    int mCpuIdleCheckCount = 0;
    int mCpuLoadRecInterv = 50;
    int mCpuLoadRecThreshold = 200;
    private int mEndBatteryLevel;
    int mGlobalBatTemp = 0;
    int mGlobalBatteryCurrent;
    int mGlobalBatteryRealtimeCapacity = 0;
    int mGlobalBatteryVoltage = 0;
    int mGlobalChargeId = 0;
    int mGlobalFast2Normal = 0;
    boolean mGlobalFastCharge = false;
    boolean mGlobalFastCharger = false;
    int mGlobalMaxBatTemp = INVALID_DATA;
    int mGlobalMaxPhoneTemp = INVALID_DATA;
    int mGlobalPlugType = 0;
    boolean mGlobalScreenBrightnessMode = false;
    int mGlobalVolumeLevel;
    private Handler mHandler;
    private boolean mHaveCaptured = false;
    int mHeatHoldTimeThreshold = 1800000;
    int mHeatHoldUploadTime = Process.FIRST_APP_ZYGOTE_ISOLATED_UID;
    long mHeatIncRatioStartTime = 0;
    int mHeatIncRatioThreshold = 10;
    private HeatReasonDetails mHeatReasonDetails = new HeatReasonDetails();
    int mHeatRecInterv = 2;
    int mHeatThreshold = DisplayMetrics.DENSITY_450;
    private boolean mHoldHeat = false;
    long mHoldHeatElapsedRealtime = 0;
    int mHoldHeatTime = -1;
    private boolean mIteratingThermalHistory;
    int mLastFast2Normal = 0;
    int mLastPhoneTemp = INVALID_DATA;
    int mLastPhoneTemp1 = INVALID_DATA;
    int mLastPhoneTemp2 = INVALID_DATA;
    int mLastPhoneTemp3 = INVALID_DATA;
    int mLessHeatThreshold = DisplayMetrics.DENSITY_420;
    private final Object mLock = new Object();
    private boolean mMonitorAppAll = true;
    private int mMonitorAppLimitTime = 2400000;
    private Map<String, String> mMonitorAppMap = new ArrayMap();
    int mMoreHeatThreshold = 500;
    private int mNetConnectType = -1;
    int mNumThermalHistoryItems;
    private PackageManager mPackageManger;
    private PowerManager mPowerManager;
    int mPreHeatThreshold = 400;
    private ThermalReceiver mReceiver = null;
    boolean mRecordThermalHistory;
    private int mScreenBrightness;
    private long mSimpleTopProInterVal = 120000;
    private String mSimpleTopProcesses;
    private String mSimpleTopProcessesNeedUpload = "invalid";
    private boolean mStartAnalizyHeat = false;
    private int mStartBatteryLevel;
    private final OppoBaseBatteryStatsImpl mStats;
    private File mSystemDir = null;
    private Map<String, Long> mTempChargeUploadMap = new ArrayMap();
    private Map<String, Long> mTempMonitorAppMap = new ArrayMap();
    boolean mThermalBatteryTemp = true;
    private final StringBuilder mThermalBuilder = new StringBuilder();
    boolean mThermalCaptureLog;
    int mThermalCaptureLogThreshold;
    boolean mThermalFeatureOn;
    private OppoBaseBatteryStats.ThermalItem mThermalHistory;
    private final Parcel mThermalHistoryBuffer = Parcel.obtain();
    int mThermalHistoryBufferLastPos = -1;
    final OppoBaseBatteryStats.ThermalItem mThermalHistoryCur = new OppoBaseBatteryStats.ThermalItem();
    private final OppoBaseBatteryStats.ThermalItem mThermalHistoryLastLastRead = new OppoBaseBatteryStats.ThermalItem();
    private final OppoBaseBatteryStats.ThermalItem mThermalHistoryLastLastWritten = new OppoBaseBatteryStats.ThermalItem();
    private final OppoBaseBatteryStats.ThermalItem mThermalHistoryLastRead = new OppoBaseBatteryStats.ThermalItem();
    private final OppoBaseBatteryStats.ThermalItem mThermalHistoryLastWritten = new OppoBaseBatteryStats.ThermalItem();
    private Map<String, Integer> mThermalHourMap = new ArrayMap();
    ArrayList<String> mThermalMonitorApp = new ArrayList<>();
    private final File mThermalRecFile;
    private Map<String, Long> mThermalTempMap = new ArrayMap();
    boolean mThermalUploadDcs;
    boolean mThermalUploadErrLog;
    boolean mThermalUploadLog;
    int mTopCpuRecInterv = 20;
    int mTopCpuRecThreshold = 50;
    private PowerManager.WakeLock mWakeLock;
    private PendingIntent mWakeupIntent;

    public OppoThermalStatsHelper(OppoBaseBatteryStatsImpl stats, Handler handler, File thermalRecFile, File systemDir) {
        this.mStats = stats;
        this.mThermalRecFile = thermalRecFile;
        this.mSystemDir = systemDir;
        this.mHandler = new WorkHandler(handler.getLooper());
    }

    public void setBatteryStatsReady(boolean ready) {
        this.mBatteryStatsReady = ready;
    }

    public boolean getBatteryStatsReadyStatus() {
        return this.mBatteryStatsReady;
    }

    public void onSystemReady(Context context) {
        Slog.d(TAG, "onSystemReady.....");
        this.mContext = context;
    }

    public void setScreenBrightness(int value) {
        this.mScreenBrightness = value;
    }

    public int getScreenBrightness() {
        return this.mScreenBrightness;
    }

    public void setConnectyType(int type) {
        this.mNetConnectType = type;
    }

    public int getConnectyType() {
        return this.mNetConnectType;
    }

    /* access modifiers changed from: private */
    public class HeatReasonDetails {
        private OppoBaseBatteryStats.ThermalItem[] mAnalizyHeatArray = new OppoBaseBatteryStats.ThermalItem[400];
        public int mAnalizyPosition = 0;
        private int mAudioTime = 0;
        private int mBackLight = 0;
        private int mBackLightCount = 0;
        private int mBatRm0 = -1;
        private int mBatRm1 = -1;
        public int mBatTemp = 0;
        private int mCameraTime = 0;
        private int mCpuLoading = 0;
        private int mCpuLoadingCount = 0;
        private int mEnviTemp = OppoThermalStatsHelper.INVALID_DATA;
        private int mFlashlightTime = 0;
        private HashMap<String, Integer> mForeProcTimeMap = new HashMap<>();
        private int mGpsTime = 0;
        private int mHeartReason = -1;
        private int mHeatRatio = OppoThermalStatsHelper.INVALID_DATA;
        private boolean mIsUploadHeat = false;
        private HashMap<String, Integer> mJobProcTimeMap = new HashMap<>();
        private int mJobTime = 0;
        private OppoBaseBatteryStats.ThermalItem mLastAnalizyItem = new OppoBaseBatteryStats.ThermalItem();
        private String mLongTopCpuProc = "null";
        private int mLongTopCpuTime = 0;
        private String mMaxForeProc = "null";
        private int mMaxForeProcTime = 0;
        private String mMaxJobProc = "null";
        private int mMaxJobTime = 0;
        private String mMaxSyncProc = "null";
        private int mMaxSyncTime = 0;
        private String mMaxTopCpuProc = "null";
        private int mMaxTopCpuRatio = 0;
        private int mNet2GTime = 0;
        private int mNet3GTime = 0;
        private int mNet4GTime = 0;
        private int mPhoneOnTime = 0;
        private int mPhoneSignal = 0;
        private int mPhoneSignalCount = 0;
        public int mPhoneTemp = 0;
        private boolean mPlug = false;
        private int mPlugAcTime = 0;
        private int mPlugNoneTime = 0;
        private HashMap<Integer, Integer> mPlugTimeMap = new HashMap<>();
        private int mPlugUsbTime = 0;
        private int mPlugWireTime = 0;
        private HashMap<String, Integer> mSyncProcTimeMap = new HashMap<>();
        private int mSyncTime = 0;
        private int mTemp0 = OppoThermalStatsHelper.INVALID_DATA;
        private int mTemp1 = OppoThermalStatsHelper.INVALID_DATA;
        private HashMap<String, Integer> mTopCpuProcRatioMap = new HashMap<>();
        private HashMap<String, Integer> mTopCpuProcTimeMap = new HashMap<>();
        private int mTotalTime = -1;
        Map<String, String> mUpLoadMap = new HashMap();
        private String mVersionName = "";
        private int mVideoTime = 0;
        private int mWifiStrenth = 0;
        private int mWifiStrenthCount = 0;
        private int mWifiTime = 0;

        public HeatReasonDetails() {
            if (this.mAnalizyHeatArray == null) {
                this.mAnalizyHeatArray = new OppoBaseBatteryStats.ThermalItem[400];
            }
            for (int i = 0; i < this.mAnalizyHeatArray.length; i++) {
                this.mAnalizyHeatArray[i] = new OppoBaseBatteryStats.ThermalItem();
            }
        }

        public void addToHeatItem(OppoBaseBatteryStats.ThermalItem item) {
            if (OppoThermalStatsHelper.DEBUG_THERMAL_TEMP) {
                Slog.d(OppoThermalStatsHelper.TAG, "addToHeatItem:" + ((int) item.cmd) + " mAnalizyPosition=" + this.mAnalizyPosition);
            }
            int i = this.mAnalizyPosition;
            int res = i % 400;
            if (i > 399 || i < 0 || res < 0) {
                Slog.d(OppoThermalStatsHelper.TAG, "AnalizyPosition reach max limit, mAnalizyPosition=" + this.mAnalizyPosition + " res=" + res);
                this.mAnalizyPosition = 0;
                res = 0;
            }
            try {
                this.mAnalizyHeatArray[res].setTo(item);
                this.mAnalizyPosition++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void clear() {
            this.mPlug = false;
            this.mTotalTime = 0;
            this.mAudioTime = 0;
            this.mCameraTime = 0;
            this.mVideoTime = 0;
            this.mGpsTime = 0;
            this.mFlashlightTime = 0;
            this.mPhoneSignal = 0;
            this.mPhoneSignalCount = 0;
            this.mWifiTime = 0;
            this.mWifiStrenth = 0;
            this.mWifiStrenthCount = 0;
            this.mNet2GTime = 0;
            this.mNet3GTime = 0;
            this.mNet4GTime = 0;
            this.mCpuLoadingCount = 0;
            this.mCpuLoading = 0;
            this.mBackLight = 0;
            this.mBackLightCount = 0;
            this.mJobTime = 0;
            this.mMaxJobTime = 0;
            this.mMaxJobProc = "null";
            this.mSyncTime = 0;
            this.mMaxSyncTime = 0;
            this.mMaxSyncProc = "null";
            this.mMaxForeProcTime = 0;
            this.mMaxForeProc = "null";
            this.mMaxTopCpuRatio = 0;
            this.mMaxTopCpuProc = "null";
            this.mLongTopCpuTime = 0;
            this.mLongTopCpuProc = "null";
            this.mHeartReason = -1;
            this.mTemp0 = OppoThermalStatsHelper.INVALID_DATA;
            this.mTemp1 = OppoThermalStatsHelper.INVALID_DATA;
            this.mHeatRatio = -127;
            this.mEnviTemp = OppoThermalStatsHelper.INVALID_DATA;
            this.mBatRm0 = -1;
            this.mBatRm1 = -1;
            this.mPlugNoneTime = 0;
            this.mPlugUsbTime = 0;
            this.mPlugAcTime = 0;
            this.mPlugWireTime = 0;
            this.mVersionName = "";
            this.mPlugTimeMap.clear();
            this.mJobProcTimeMap.clear();
            this.mSyncProcTimeMap.clear();
            this.mForeProcTimeMap.clear();
            this.mTopCpuProcTimeMap.clear();
            this.mTopCpuProcRatioMap.clear();
            this.mLastAnalizyItem.clear();
            this.mUpLoadMap.clear();
            this.mIsUploadHeat = false;
        }

        public boolean hasCode() {
            Map<String, String> map = this.mUpLoadMap;
            if (map == null || map.size() <= 0) {
                return false;
            }
            return true;
        }

        private void addTotalTime(int time) {
            this.mTotalTime += time;
        }

        private void addAudioTime(int time) {
            this.mAudioTime += time;
        }

        private int getAudioTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mAudioTime * 1000) / i;
        }

        private void addCameraTime(int time) {
            this.mCameraTime += time;
        }

        private int getCameraTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mCameraTime * 1000) / i;
        }

        private void addVideoTime(int time) {
            this.mVideoTime += time;
        }

        private int getVideoTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mVideoTime * 1000) / i;
        }

        private void addGpsTime(int time) {
            this.mGpsTime += time;
        }

        private int getGpsTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mGpsTime * 1000) / i;
        }

        private void addFlashlightTime(int time) {
            this.mFlashlightTime += time;
        }

        private int getFlashlightTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mFlashlightTime * 1000) / i;
        }

        private void addPhoneOnTime(int time) {
            this.mPhoneOnTime += time;
        }

        private int getPhoneOnTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mPhoneOnTime * 1000) / i;
        }

        private void addPhoneSignal(int signal) {
            this.mPhoneSignal += signal;
            this.mPhoneSignalCount++;
        }

        private int getAvgPhoneSignal() {
            int i = this.mPhoneSignalCount;
            if (i == 0) {
                return 0;
            }
            return this.mPhoneSignal / i;
        }

        private void addWifiTime(int time) {
            this.mWifiTime += time;
        }

        private int getWifiTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mWifiTime * 1000) / i;
        }

        private void addWifiStrenth(int strenth) {
            this.mWifiStrenth += strenth;
            this.mWifiStrenthCount++;
        }

        private int getAvgWifiStrenth() {
            int i = this.mWifiStrenthCount;
            if (i == 0) {
                return 0;
            }
            return this.mWifiStrenth / i;
        }

        private void addNet2GTime(int time) {
            this.mNet2GTime += time;
        }

        private int getNet2GTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mNet2GTime * 1000) / i;
        }

        private void addNet3GTime(int time) {
            this.mNet3GTime += time;
        }

        private int getNet3GTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mNet3GTime * 1000) / i;
        }

        private void addNet4GTime(int time) {
            this.mNet4GTime += time;
        }

        private int getNet4GTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mNet4GTime * 1000) / i;
        }

        private void addCpuLoading(int cpuLoading) {
            this.mCpuLoadingCount++;
            this.mCpuLoading += cpuLoading;
        }

        private int getAvgCpuLoading() {
            int i = this.mCpuLoadingCount;
            if (i == 0) {
                return 0;
            }
            return this.mCpuLoading / i;
        }

        private void addBackLight(int backLight) {
            this.mBackLight += backLight;
            this.mBackLightCount++;
        }

        private int getAvgBackLight() {
            int i = this.mBackLightCount;
            if (i == 0) {
                return 0;
            }
            return this.mBackLight / i;
        }

        private void setEnviTmep(int temp) {
            this.mEnviTemp = temp;
        }

        private int getEnviTmep() {
            return this.mEnviTemp;
        }

        private void setBatRm0(int batrm) {
            this.mBatRm0 = batrm;
        }

        private void setBatRm1(int batrm) {
            this.mBatRm1 = batrm;
        }

        private int getCurrent() {
            int i;
            int i2;
            int i3 = this.mBatRm0;
            if (i3 == -1 || (i = this.mBatRm1) == -1 || i3 <= i || getPlugNoneTimeThousandths() <= 950 || (i2 = this.mTotalTime) == 0) {
                return 9999;
            }
            return ((this.mBatRm0 - this.mBatRm1) * 3600000) / i2;
        }

        private void setTemp0(int temp) {
            this.mTemp0 = temp;
        }

        private void setTemp1(int temp) {
            this.mTemp1 = temp;
        }

        private int getHeatRatio() {
            int i;
            int i2;
            int i3 = this.mTemp1;
            if (i3 == OppoThermalStatsHelper.INVALID_DATA || (i = this.mTemp0) == OppoThermalStatsHelper.INVALID_DATA || i3 <= i || (i2 = this.mTotalTime) == 0) {
                return 9999;
            }
            return ((i3 - i) * MediaPlayer.ProvisioningThread.TIMEOUT_MS) / i2;
        }

        private void putPlugTypeAndTime(int type, int time) {
            if (!(type == 1 || type == 2 || type == 4)) {
                type = 0;
            }
            if (this.mPlugTimeMap.containsKey(Integer.valueOf(type))) {
                this.mPlugTimeMap.put(Integer.valueOf(type), Integer.valueOf(time + this.mPlugTimeMap.get(Integer.valueOf(type)).intValue()));
                return;
            }
            this.mPlugTimeMap.put(Integer.valueOf(type), Integer.valueOf(time));
        }

        private int getPlugNoneTimeThousandths() {
            int tempTime = 0;
            if (this.mPlugTimeMap.containsKey(0)) {
                tempTime = this.mPlugTimeMap.get(0).intValue();
            }
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (tempTime * 1000) / i;
        }

        private int getPlugUsbTimeThousandths() {
            int tempTime = 0;
            if (this.mPlugTimeMap.containsKey(2)) {
                tempTime = this.mPlugTimeMap.get(2).intValue();
            }
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (tempTime * 1000) / i;
        }

        private int getPlugAcTimeThousandths() {
            int tempTime = 0;
            if (this.mPlugTimeMap.containsKey(1)) {
                tempTime = this.mPlugTimeMap.get(1).intValue();
            }
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (tempTime * 1000) / i;
        }

        private int getPlugWireTimeThousandths() {
            int tempTime = 0;
            if (this.mPlugTimeMap.containsKey(4)) {
                tempTime = this.mPlugTimeMap.get(4).intValue();
            }
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (tempTime * 1000) / i;
        }

        private void putJobProcAndTime(String proc, int time) {
            if (this.mJobProcTimeMap.containsKey(proc)) {
                this.mJobProcTimeMap.put(proc, Integer.valueOf(time + this.mJobProcTimeMap.get(proc).intValue()));
            } else {
                this.mJobProcTimeMap.put(proc, Integer.valueOf(time));
            }
            this.mJobTime += time;
            try {
                for (Map.Entry<String, Integer> entry : this.mJobProcTimeMap.entrySet()) {
                    String jobProc = entry.getKey();
                    int jobTime = entry.getValue().intValue();
                    if (this.mMaxJobTime < jobTime) {
                        this.mMaxJobTime = jobTime;
                        this.mMaxJobProc = jobProc;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        private int getJobTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mJobTime * 1000) / i;
        }

        private int getMaxJobTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mMaxJobTime * 1000) / i;
        }

        private String getMaxJobProc() {
            return this.mMaxJobProc;
        }

        private void putSyncProcAndTime(String proc, int time) {
            if (this.mSyncProcTimeMap.containsKey(proc)) {
                this.mSyncProcTimeMap.put(proc, Integer.valueOf(time + this.mSyncProcTimeMap.get(proc).intValue()));
            } else {
                this.mSyncProcTimeMap.put(proc, Integer.valueOf(time));
            }
            this.mSyncTime += time;
            try {
                for (Map.Entry<String, Integer> entry : this.mSyncProcTimeMap.entrySet()) {
                    String syncProc = entry.getKey();
                    int syncTime = entry.getValue().intValue();
                    if (this.mMaxSyncTime < syncTime) {
                        this.mMaxSyncTime = syncTime;
                        this.mMaxSyncProc = syncProc;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        private int getSyncTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mSyncTime * 1000) / i;
        }

        private int getMaxSyncTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mMaxSyncTime * 1000) / i;
        }

        private String getMaxSyncProc() {
            return this.mMaxSyncProc;
        }

        private void putForeProcAndTime(String versionName, String proc, int time) {
            if (this.mForeProcTimeMap.containsKey(proc)) {
                this.mForeProcTimeMap.put(proc, Integer.valueOf(time + this.mForeProcTimeMap.get(proc).intValue()));
            } else {
                this.mForeProcTimeMap.put(proc, Integer.valueOf(time));
            }
            try {
                for (Map.Entry<String, Integer> entry : this.mForeProcTimeMap.entrySet()) {
                    String foreProc = entry.getKey();
                    int foreTime = entry.getValue().intValue();
                    if (this.mMaxForeProcTime < foreTime) {
                        this.mMaxForeProcTime = foreTime;
                        this.mMaxForeProc = foreProc;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            this.mVersionName = versionName;
        }

        private int getMaxForeTimeThousandths() {
            int i = this.mTotalTime;
            if (i == 0) {
                return 0;
            }
            return (this.mMaxForeProcTime * 1000) / i;
        }

        private String getMaxForeProc() {
            return this.mMaxForeProc;
        }

        private void putTopCpuProcAndTime(String proc, int time) {
            if (this.mTopCpuProcTimeMap.containsKey(proc)) {
                this.mTopCpuProcTimeMap.put(proc, Integer.valueOf(time + this.mTopCpuProcTimeMap.get(proc).intValue()));
                return;
            }
            this.mTopCpuProcTimeMap.put(proc, Integer.valueOf(time));
        }

        private int getTopCpuTimeThousandths() {
            int i = 0;
            try {
                if (!this.mMaxTopCpuProc.equals("null") && this.mTopCpuProcTimeMap.containsKey(this.mMaxTopCpuProc) && this.mTopCpuProcTimeMap.get(this.mMaxTopCpuProc) != null) {
                    if (this.mTotalTime != 0) {
                        i = (this.mTopCpuProcTimeMap.get(this.mMaxTopCpuProc).intValue() * 1000) / this.mTotalTime;
                    }
                    return i;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        private void putTopCpuProcAndRatio(String proc, int ratio) {
            if (this.mTopCpuProcRatioMap.containsKey(proc)) {
                int temp = this.mTopCpuProcRatioMap.get(proc).intValue();
                this.mTopCpuProcRatioMap.put(proc, Integer.valueOf((ratio + (temp & 65535)) | (((65535 & (temp >> 16)) + 1) << 16)));
                return;
            }
            this.mTopCpuProcRatioMap.put(proc, Integer.valueOf(65536 | ratio));
        }

        private int getMaxTopCpuRatio() {
            try {
                if (!this.mMaxTopCpuProc.equals("null") && this.mTopCpuProcRatioMap.containsKey(this.mMaxTopCpuProc) && this.mTopCpuProcRatioMap.get(this.mMaxTopCpuProc) != null) {
                    int temp = this.mTopCpuProcRatioMap.get(this.mMaxTopCpuProc).intValue();
                    this.mMaxTopCpuRatio = (temp & 65535) / (65535 & (temp >> 16));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this.mMaxTopCpuRatio;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getMaxTopCpuProc() {
            int maxRatio = -1;
            try {
                for (Map.Entry<String, Integer> entry : this.mTopCpuProcRatioMap.entrySet()) {
                    String topRatioProc = entry.getKey();
                    int temp = entry.getValue().intValue();
                    int i = 65535 & (temp >> 16);
                    int avgratio = (temp & 65535) / this.mCpuLoadingCount;
                    if (maxRatio < avgratio) {
                        maxRatio = avgratio;
                        this.mMaxTopCpuProc = topRatioProc;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return this.mMaxTopCpuProc;
        }

        public boolean analizyHeatRecItem(long thermaldumpStart, int pos) {
            if (pos < 0 || pos < this.mAnalizyPosition - 400) {
                return false;
            }
            try {
                OppoBaseBatteryStats.ThermalItem item = this.mAnalizyHeatArray[pos % 400];
                if (item == null || item.elapsedRealtime < thermaldumpStart || item.phoneTemp < OppoThermalStatsHelper.this.mHeatThreshold - 70) {
                    return false;
                }
                if (this.mLastAnalizyItem.elapsedRealtime > 0) {
                    int relTime = (int) (this.mLastAnalizyItem.elapsedRealtime - item.elapsedRealtime);
                    addTotalTime(relTime);
                    if (item.audioOn) {
                        addAudioTime(relTime);
                    }
                    if (item.cameraOn) {
                        addCameraTime(relTime);
                    }
                    if (item.videoOn) {
                        addVideoTime(relTime);
                    }
                    if (item.gpsOn) {
                        addGpsTime(relTime);
                    }
                    if (item.flashlightOn) {
                        addFlashlightTime(relTime);
                    }
                    if (item.phoneOnff) {
                        addPhoneOnTime(relTime);
                    }
                    if (item.connectNetType == 1) {
                        addWifiTime(relTime);
                    } else if (item.connectNetType == 2) {
                        addNet2GTime(relTime);
                    } else if (item.connectNetType == 3) {
                        addNet3GTime(relTime);
                    } else if (item.connectNetType == 4) {
                        addNet4GTime(relTime);
                    }
                    putPlugTypeAndTime(item.chargePlug, relTime);
                    if (item.jobSchedule != null && !item.jobSchedule.equals("null")) {
                        putJobProcAndTime(item.jobSchedule, relTime);
                    }
                    if (item.netSync != null && !item.netSync.equals("null")) {
                        putSyncProcAndTime(item.netSync, relTime);
                    }
                    if (item.foreProc != null && !item.foreProc.equals("null")) {
                        putForeProcAndTime(item.versionName, item.foreProc, relTime);
                    }
                    if (item.topProc != null && !item.topProc.equals("null")) {
                        putTopCpuProcAndTime(item.topProc, relTime);
                    }
                }
                if (item.connectNetType == 1) {
                    addWifiStrenth(item.wifiSignal);
                }
                addPhoneSignal(item.phoneSignal);
                addCpuLoading(item.cpuLoading);
                addBackLight(item.backlight);
                if (getEnviTmep() == OppoThermalStatsHelper.INVALID_DATA) {
                    setEnviTmep(item.enviTemp);
                }
                if (pos == this.mAnalizyPosition - 1) {
                    if (item.phoneTemp != OppoThermalStatsHelper.INVALID_DATA) {
                        setBatRm1(item.batRm);
                        setTemp1(item.phoneTemp);
                    }
                } else if (item.phoneTemp != OppoThermalStatsHelper.INVALID_DATA) {
                    setBatRm0(item.batRm);
                    setTemp0(item.phoneTemp);
                }
                if (item.topProc != null) {
                    putTopCpuProcAndRatio(item.topProc, item.topCpu);
                }
                this.mLastAnalizyItem.setTo(item);
                return true;
            } catch (Exception e) {
                Slog.e(OppoThermalStatsHelper.TAG, "analizyHeatRecItem pos error");
                return false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getHeatReson() {
            boolean isMultiReson = false;
            boolean isCharge = getPlugNoneTimeThousandths() < 250;
            if (getAudioTimeThousandths() + getJobTimeThousandths() + getVideoTimeThousandths() + getCameraTimeThousandths() + getSyncTimeThousandths() + getPhoneOnTimeThousandths() + getFlashlightTimeThousandths() >= 750) {
                isMultiReson = true;
            }
            if (!getMaxTopCpuProc().equals(getMaxForeProc()) || getMaxForeTimeThousandths() < 600 || getHeatRatio() < OppoThermalStatsHelper.this.mHeatIncRatioThreshold) {
                if (getMaxTopCpuProc().equals(getMaxForeProc()) || getMaxForeTimeThousandths() < 600 || getHeatRatio() < OppoThermalStatsHelper.this.mHeatIncRatioThreshold) {
                    if (isMultiReson) {
                        if (!isCharge) {
                            return 5;
                        }
                        return 6;
                    } else if (getHeatRatio() > 500 || getAvgCpuLoading() > 150) {
                        return 9999;
                    } else {
                        if (isCharge && getCurrent() < -200) {
                            return 7;
                        }
                        if (isCharge || getCurrent() >= 500) {
                            return 9999;
                        }
                        return 8;
                    }
                } else if (!isCharge) {
                    return 1;
                } else {
                    return 2;
                }
            } else if (!isCharge) {
                return 3;
            } else {
                return 4;
            }
        }

        public void putHeatMaxTemp(int maxTemp, int maxBatTemp) {
            if (hasCode()) {
                if (this.mUpLoadMap.containsKey("maxPhoneTemp")) {
                    try {
                        if (Integer.parseInt(this.mUpLoadMap.get("maxPhoneTemp")) < maxTemp) {
                            this.mUpLoadMap.put("maxPhoneTemp", Integer.toString(maxTemp));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    this.mUpLoadMap.put("maxPhoneTemp", Integer.toString(maxTemp));
                }
                if (this.mUpLoadMap.containsKey("maxBatTemp")) {
                    try {
                        if (Integer.parseInt(this.mUpLoadMap.get("maxBatTemp")) < maxBatTemp) {
                            this.mUpLoadMap.put("maxBatTemp", Integer.toString(maxBatTemp));
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                } else {
                    this.mUpLoadMap.put("maxBatTemp", Integer.toString(maxBatTemp));
                }
            }
        }

        public void getHeatReasonDetails() {
            this.mUpLoadMap.put("heatTime", DateFormat.format("yyyy-MM-dd-HH-mm-ss", System.currentTimeMillis()).toString());
            this.mUpLoadMap.put("heatReason", Integer.toString(getHeatReson()));
            this.mUpLoadMap.put("totalTime", Integer.toString(this.mTotalTime / 1000));
            this.mUpLoadMap.put("audioTimeRatio", Float.toString(((float) getAudioTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("cameraTimeRatio", Float.toString(((float) getCameraTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("gpsTimeRatio", Float.toString(((float) getCameraTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("flashlightTimeRatio", Float.toString(((float) getFlashlightTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("phoneOnTimeRatio", Float.toString(((float) getPhoneOnTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("phoneSignal", Integer.toString(getAvgPhoneSignal()));
            this.mUpLoadMap.put("wifiTimeRatio", Float.toString(((float) getWifiTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("wifiSignal", Integer.toString(getAvgWifiStrenth()));
            this.mUpLoadMap.put("2GTimeRatio", Float.toString(((float) getNet2GTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("3GTimeRatio", Float.toString(((float) getNet3GTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("4GTimeRatio", Float.toString(((float) getNet4GTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("cpuLoading", Float.toString(((float) getAvgCpuLoading()) / 10.0f));
            this.mUpLoadMap.put("backlight", Integer.toString(getAvgBackLight()));
            this.mUpLoadMap.put("jobProc", getMaxJobProc());
            this.mUpLoadMap.put("jobTimeRatio", Float.toString(((float) getJobTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("syncProcc", getMaxSyncProc());
            this.mUpLoadMap.put("syncTimeRatio", Float.toString(((float) getSyncTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("foreProc", getMaxForeProc());
            this.mUpLoadMap.put("foreProcTimeRatio", Float.toString(((float) getMaxForeTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("topcpuproc", getMaxTopCpuProc());
            this.mUpLoadMap.put("topcpuTimeRatio", Float.toString(((float) getTopCpuTimeThousandths()) / 10.0f));
            this.mUpLoadMap.put("topcpuRatio", Float.toString(((float) getMaxTopCpuRatio()) / 10.0f));
            this.mUpLoadMap.put("heatRatio", Float.toString(((float) getHeatRatio()) / 10.0f));
            this.mUpLoadMap.put("current", Integer.toString(getCurrent()));
            this.mUpLoadMap.put("enviTemp", Integer.toString(getEnviTmep()));
            this.mUpLoadMap.put("temp", Integer.toString(this.mPhoneTemp));
            this.mUpLoadMap.put("batTemp", Integer.toString(this.mBatTemp));
            this.mUpLoadMap.put("maxPhoneTemp", Integer.toString(this.mPhoneTemp));
            this.mUpLoadMap.put("maxBatTemp", Integer.toString(this.mBatTemp));
            this.mUpLoadMap.put("heatThreshold", Integer.toString(OppoThermalStatsHelper.this.mHeatThreshold));
            this.mUpLoadMap.put("foreProcVersion", this.mVersionName);
            try {
                for (Map.Entry<String, String> entry : this.mUpLoadMap.entrySet()) {
                    Slog.i(OppoThermalStatsHelper.TAG, "getHeatReasonDetails " + entry.getKey() + SettingsStringUtil.DELIMITER + entry.getValue());
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        public void uploadHeatEvent() {
            if (hasCode()) {
                try {
                    for (Map.Entry<String, String> entry : this.mUpLoadMap.entrySet()) {
                        Slog.i(OppoThermalStatsHelper.TAG, "uploadHeatEvent " + entry.getKey() + SettingsStringUtil.DELIMITER + entry.getValue());
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                new Thread(new Runnable() {
                    /* class com.android.internal.os.OppoThermalStatsHelper.HeatReasonDetails.AnonymousClass1 */

                    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0152, code lost:
                        com.android.internal.os.OppoThermalStatsHelper.ThermalStatistics.onCommon(r10.this$1.this$0.mContext, com.android.internal.os.OppoThermalStatsHelper.THERMAL_TAG, com.android.internal.os.OppoThermalStatsHelper.THERMAL_HEAT_EVENT, r2, false);
                        r1 = 0;
                        r2 = false;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0168, code lost:
                        if (com.android.internal.os.OppoThermalStatsHelper.ThermalStatistics.getOnCommon() == false) goto L_0x018f;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:26:0x016c, code lost:
                        if (r1 >= 50) goto L_0x018f;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
                        java.lang.Thread.sleep(100);
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0173, code lost:
                        r1 = r1 + 1;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0176, code lost:
                        r3 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0177, code lost:
                        android.util.Slog.w(com.android.internal.os.OppoThermalStatsHelper.TAG, "sleep 100 ms is Interrupted because of " + r3);
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:33:0x018f, code lost:
                        r10.this$1.this$0.writeThermalRecFile();
                        r3 = new android.content.Intent("oppo.intent.action.ACTION_THERMAL_SCENE");
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:34:0x01a4, code lost:
                        if (r2.containsKey("heatReason") == false) goto L_0x01b5;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:35:0x01a6, code lost:
                        r3.putExtra("reason", r2.get("heatReason"));
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:36:0x01b5, code lost:
                        r3.putExtra("reason", "9999");
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:38:0x01c3, code lost:
                        if (r2.containsKey("current") == false) goto L_0x01d3;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:39:0x01c5, code lost:
                        r3.putExtra("current", r2.get("current"));
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:40:0x01d3, code lost:
                        r3.putExtra("current", "9999");
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:42:0x01e1, code lost:
                        if (r2.containsKey("maxPhoneTemp") == false) goto L_0x01f3;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:43:0x01e3, code lost:
                        r3.putExtra("temp", r2.get("maxPhoneTemp"));
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:44:0x01f3, code lost:
                        r3.putExtra("temp", "9999");
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0202, code lost:
                        if (r2.containsKey("maxBatTemp") == false) goto L_0x0213;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0204, code lost:
                        r3.putExtra("batTemp", r2.get("maxBatTemp"));
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0213, code lost:
                        r3.putExtra("batTemp", "9999");
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0220, code lost:
                        if (r2.containsKey("cpuLoading") == false) goto L_0x0230;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0222, code lost:
                        r3.putExtra("cpuloading", r2.get("cpuloading"));
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0230, code lost:
                        r3.putExtra("cpuloading", "9999");
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0237, code lost:
                        r3.putExtra("package", r10.this$1.getMaxTopCpuProc());
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:54:0x024a, code lost:
                        if (r10.this$1.this$0.mThermalUploadLog != false) goto L_0x0265;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0252, code lost:
                        if (r10.this$1.this$0.mThermalUploadErrLog == false) goto L_0x0266;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:58:0x025a, code lost:
                        if (r10.this$1.getHeatReson() == 1) goto L_0x0265;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0263, code lost:
                        if (r10.this$1.getHeatReson() != 2) goto L_0x0266;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0265, code lost:
                        r2 = true;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0266, code lost:
                        r3.putExtra("uploadLog", r2);
                        r3.setPackage("com.oppo.oppopowermonitor");
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0277, code lost:
                        if (r10.this$1.this$0.mThermalCaptureLog == false) goto L_0x02a4;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0285, code lost:
                        if (r10.this$1.this$0.mGlobalMaxPhoneTemp < r10.this$1.this$0.mThermalCaptureLogThreshold) goto L_0x02a4;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:67:0x028f, code lost:
                        if (r10.this$1.this$0.mContext == null) goto L_0x029e;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0291, code lost:
                        r10.this$1.this$0.mContext.sendBroadcastAsUser(r3, android.os.UserHandle.CURRENT);
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:69:0x029e, code lost:
                        r10.this$1.mIsUploadHeat = true;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:70:0x02a4, code lost:
                        android.util.Slog.d(com.android.internal.os.OppoThermalStatsHelper.TAG, "CaptureLog=" + r10.this$1.this$0.mThermalCaptureLog + " ,skip capture log");
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:71:0x02c5, code lost:
                        r4 = r10.this$1.this$0.getStampThermalHeat(r2);
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:73:?, code lost:
                        r5 = r4.entrySet().iterator();
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:75:0x02d9, code lost:
                        if (r5.hasNext() == false) goto L_0x0313;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:76:0x02db, code lost:
                        r6 = r5.next();
                        android.util.Slog.i(com.android.internal.os.OppoThermalStatsHelper.TAG, "uploadStampHeat " + r6.getKey() + android.provider.SettingsStringUtil.DELIMITER + r6.getValue());
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:77:0x030f, code lost:
                        r5 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0310, code lost:
                        r5.printStackTrace();
                     */
                    public void run() {
                        Map<String, String> stampMap;
                        synchronized (OppoThermalStatsHelper.this.mLock) {
                            if (!HeatReasonDetails.this.mIsUploadHeat) {
                                if (!HeatReasonDetails.this.mUpLoadMap.isEmpty()) {
                                    if (HeatReasonDetails.this.mUpLoadMap.containsKey("heatReason")) {
                                        if (OppoThermalStatsHelper.this.mContext == null) {
                                            Slog.w(OppoThermalStatsHelper.TAG, "upload heat event failed for context uninit!");
                                            return;
                                        }
                                        HeatReasonDetails.this.mUpLoadMap.put("simpleTopPro", OppoThermalStatsHelper.this.mSimpleTopProcessesNeedUpload);
                                        HeatReasonDetails.this.mUpLoadMap.put("cpuFreq", OppoThermalStatsHelper.this.mCpuFreqValuesNeedUpload);
                                        Map<String, String> map = HeatReasonDetails.this.mUpLoadMap;
                                        map.put("fcc", "" + OppoThermalStatsHelper.this.mBatteryFcc);
                                        Map<String, String> map2 = HeatReasonDetails.this.mUpLoadMap;
                                        map2.put("batteryRm", "" + OppoThermalStatsHelper.this.mGlobalBatteryRealtimeCapacity);
                                        Map<String, String> map3 = HeatReasonDetails.this.mUpLoadMap;
                                        map3.put("plugType", "" + OppoThermalStatsHelper.this.mGlobalPlugType);
                                        Map<String, String> map4 = HeatReasonDetails.this.mUpLoadMap;
                                        map4.put("fastCharge", "" + OppoThermalStatsHelper.this.mGlobalFastCharger);
                                        Map<String, String> map5 = HeatReasonDetails.this.mUpLoadMap;
                                        map5.put("batteryCurrent", "" + OppoThermalStatsHelper.this.mGlobalBatteryCurrent);
                                        Map<String, String> map6 = HeatReasonDetails.this.mUpLoadMap;
                                        map6.put("batteryVoltage", "" + OppoThermalStatsHelper.this.mGlobalBatteryVoltage);
                                        Map<String, String> map7 = HeatReasonDetails.this.mUpLoadMap;
                                        map7.put("volumeLevel", "" + OppoThermalStatsHelper.this.mGlobalVolumeLevel);
                                        Map<String, String> tempUpLoadMap = new HashMap<>(HeatReasonDetails.this.mUpLoadMap);
                                    } else {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            } else {
                                return;
                            }
                        }
                        if (stampMap.size() > 0) {
                            OppoManager.onStamp(OppoThermalStatsHelper.HEAT_LOG_ID, stampMap);
                        }
                    }
                }).start();
            }
        }

        public void dumpThemalHeatDetailLocked(PrintWriter pw) {
            if (this.mUpLoadMap.size() > 0) {
                try {
                    for (Map.Entry<String, String> entry : this.mUpLoadMap.entrySet()) {
                        pw.println("uploadHeatReasonDetails " + entry.getKey() + SettingsStringUtil.DELIMITER + entry.getValue());
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } else {
                pw.print("no heat record");
            }
        }
    }

    public void setThermalState(OppoThermalState thermalState) {
        if (thermalState != null) {
            this.mGlobalPlugType = thermalState.getPlugType();
            this.mBatteryFcc = thermalState.getFcc();
            this.mGlobalChargeId = thermalState.getChargeId();
            this.mGlobalFast2Normal = thermalState.getFast2Normal();
            this.mGlobalFastCharger = thermalState.getIsFastCharge();
            this.mGlobalBatteryRealtimeCapacity = thermalState.getBatteryRm();
            int thermalHeat = thermalState.getThermalHeat(0);
            int thermalHeat1 = thermalState.getThermalHeat(1);
            int thermalHeat2 = thermalState.getThermalHeat(2);
            int thermalHeat3 = thermalState.getThermalHeat(3);
            setThermalInfo(this.mGlobalPlugType, thermalState.getBatteryTemperature(), thermalState.getBatteryLevel(), this.mGlobalBatteryRealtimeCapacity, thermalHeat, thermalHeat1, thermalHeat2, thermalHeat3);
        }
    }

    public void setThermalInfoInternal(Context context, int status, int health, int plugType, int level, int temp, int volt, int chargeUAh, int chargeFullUAh, int fcc, int batteryRm, int thermalHeat, int thermalHeat1, int thermalHeat2, int thermalHeat3, int mFast2Normal, int mChargeIdVoltage, boolean fastCharge, int batteryCurrent) {
        if (DEBUG_THERMAL_TEMP) {
            Slog.d(TAG, "setThermalInfo batteryVoltage:" + volt + " batteryCurrent:" + batteryCurrent + " batteryTemp:" + temp + " plugType:" + plugType + " fastCharge:" + fastCharge);
        }
        this.mGlobalBatTemp = temp;
        this.mBatteryFcc = fcc;
        this.mGlobalChargeId = mChargeIdVoltage;
        this.mGlobalFast2Normal = mFast2Normal;
        this.mGlobalFastCharger = fastCharge;
        this.mGlobalPlugType = plugType;
        this.mGlobalBatteryRealtimeCapacity = batteryRm;
        this.mGlobalBatteryVoltage = volt;
        this.mGlobalBatteryCurrent = batteryCurrent;
        setThermalInfo(plugType, temp, level, batteryRm, thermalHeat, thermalHeat1, thermalHeat2, thermalHeat3);
    }

    private void setThermalInfo(int plug, int batTemp, int level, int batRm, int phoneTemp, int phoneTemp1, int phoneTemp2, int phoneTemp3) {
        int i;
        String str;
        int phoneTemp4;
        String str2;
        String str3;
        boolean addBatInfo;
        int i2;
        String str4;
        boolean addBatInfo2;
        if (DEBUG_THERMAL_TEMP) {
            StringBuilder sb = new StringBuilder();
            sb.append("setThermalInfo plug:");
            sb.append(plug);
            sb.append("  phoneTemp:");
            i = phoneTemp;
            sb.append(i);
            sb.append("  batTemp:");
            sb.append(batTemp);
            sb.append("  level:");
            sb.append(level);
            sb.append("  mThermalFeatureOn=");
            sb.append(this.mThermalFeatureOn);
            sb.append("  mHeatThreshold:");
            sb.append(this.mHeatThreshold);
            Slog.d(TAG, sb.toString());
        } else {
            i = phoneTemp;
        }
        if (this.mThermalFeatureOn) {
            long currentTime = System.currentTimeMillis();
            long elapsedRealtime = SystemClock.elapsedRealtime();
            if (checkCurrentTimeChanged(currentTime, this.mThermalHistoryCur.currentTime)) {
                if (this.mHandler.hasMessages(60)) {
                    this.mHandler.removeMessages(60);
                }
                Handler handler = this.mHandler;
                str = TAG;
                handler.sendEmptyMessageDelayed(60, 4000);
            } else {
                str = TAG;
            }
            if (Math.abs(currentTime - this.mThermalHistoryCur.currentTime) > AlarmManager.INTERVAL_FIFTEEN_MINUTES) {
                addThermalHistoryBufferLocked(OppoBaseBatteryStats.ThermalItem.CMD_UPDATE_TIME, this.mThermalHistoryCur, true);
            }
            if (this.mThermalBatteryTemp) {
                phoneTemp4 = batTemp;
            } else {
                phoneTemp4 = i;
            }
            OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
            thermalItem.chargePlug = plug;
            thermalItem.batTemp = batTemp;
            thermalItem.batPercent = level;
            thermalItem.batRm = batRm;
            thermalItem.phoneTemp = phoneTemp4;
            thermalItem.phoneTemp1 = phoneTemp1;
            thermalItem.phoneTemp2 = phoneTemp2;
            thermalItem.phoneTemp3 = phoneTemp3;
            boolean addBatInfo3 = this.mThermalHistoryLastWritten.phoneTemp == INVALID_DATA || this.mThermalHistoryLastWritten.chargePlug != plug || Math.abs(this.mThermalHistoryLastWritten.phoneTemp - phoneTemp4) > this.mHeatRecInterv || Math.abs(this.mThermalHistoryLastWritten.phoneTemp1 - phoneTemp1) > this.mHeatRecInterv || Math.abs(this.mThermalHistoryLastWritten.phoneTemp2 - phoneTemp2) > this.mHeatRecInterv || Math.abs(this.mThermalHistoryLastWritten.phoneTemp3 - phoneTemp3) > this.mHeatRecInterv || this.mThermalHistoryLastWritten.batPercent != level;
            boolean addThermalRatio = elapsedRealtime - this.mHeatIncRatioStartTime >= 60000;
            if (DEBUG_THERMAL_TEMP) {
                str2 = str;
                Slog.d(str2, "addThermalRatio:" + addThermalRatio + "  mHeatIncRatioStartTime:" + this.mHeatIncRatioStartTime);
            } else {
                str2 = str;
            }
            if (addThermalRatio) {
                int i3 = this.mLastPhoneTemp;
                if (i3 != INVALID_DATA) {
                    OppoBaseBatteryStats.ThermalItem thermalItem2 = this.mThermalHistoryCur;
                    addBatInfo2 = addBatInfo3;
                    str4 = str2;
                    thermalItem2.thermalRatio = (byte) ((int) (((long) ((phoneTemp4 - i3) * MediaPlayer.ProvisioningThread.TIMEOUT_MS)) / (elapsedRealtime - this.mHeatIncRatioStartTime)));
                    addThermalHistoryBufferLocked(OppoBaseBatteryStats.ThermalItem.CMD_THERMALRATIO, thermalItem2, true);
                } else {
                    str4 = str2;
                    addBatInfo2 = addBatInfo3;
                }
                this.mLastPhoneTemp = phoneTemp4;
                int i4 = this.mLastPhoneTemp1;
                if (i4 != INVALID_DATA) {
                    OppoBaseBatteryStats.ThermalItem thermalItem3 = this.mThermalHistoryCur;
                    long j = (long) ((phoneTemp1 - i4) * MediaPlayer.ProvisioningThread.TIMEOUT_MS);
                    addBatInfo = addBatInfo2;
                    thermalItem3.thermalRatio1 = (byte) ((int) (j / (elapsedRealtime - this.mHeatIncRatioStartTime)));
                    addThermalHistoryBufferLocked(OppoBaseBatteryStats.ThermalItem.CMD_THERMALRATIO1, thermalItem3, true);
                } else {
                    addBatInfo = addBatInfo2;
                }
                this.mLastPhoneTemp1 = phoneTemp1;
                int i5 = this.mLastPhoneTemp2;
                if (i5 != INVALID_DATA) {
                    OppoBaseBatteryStats.ThermalItem thermalItem4 = this.mThermalHistoryCur;
                    thermalItem4.thermalRatio2 = (byte) ((int) (((long) ((phoneTemp2 - i5) * MediaPlayer.ProvisioningThread.TIMEOUT_MS)) / (elapsedRealtime - this.mHeatIncRatioStartTime)));
                    addThermalHistoryBufferLocked(OppoBaseBatteryStats.ThermalItem.CMD_THERMALRATIO2, thermalItem4, true);
                }
                this.mLastPhoneTemp2 = phoneTemp2;
                int i6 = this.mLastPhoneTemp3;
                if (i6 != INVALID_DATA) {
                    OppoBaseBatteryStats.ThermalItem thermalItem5 = this.mThermalHistoryCur;
                    thermalItem5.thermalRatio3 = (byte) ((int) (((long) ((phoneTemp3 - i6) * MediaPlayer.ProvisioningThread.TIMEOUT_MS)) / (elapsedRealtime - this.mHeatIncRatioStartTime)));
                    addThermalHistoryBufferLocked(OppoBaseBatteryStats.ThermalItem.CMD_THERMALRATIO3, thermalItem5, true);
                }
                this.mLastPhoneTemp3 = phoneTemp3;
                this.mHeatIncRatioStartTime = elapsedRealtime;
                if (this.mThermalHistoryCur.thermalRatio >= 4 || this.mThermalHistoryCur.phoneTemp > this.mPreHeatThreshold) {
                    str3 = str4;
                    Slog.d(str3, "REPORT_UPDATE_CPU  ->  phoneTemp:" + this.mThermalHistoryCur.phoneTemp + "  preHeatThreshold:" + this.mPreHeatThreshold);
                    this.mStats.schedulerUpdateCpu(0);
                } else {
                    str3 = str4;
                }
            } else {
                str3 = str2;
                addBatInfo = addBatInfo3;
            }
            if (addBatInfo) {
                addThermalHistoryBufferLocked((byte) 1, this.mThermalHistoryCur, true);
            }
            if (phoneTemp4 >= this.mHeatThreshold && elapsedRealtime > ParcelableCallAnalytics.MILLIS_IN_5_MINUTES) {
                if (DEBUG_THERMAL_TEMP) {
                    Slog.d(str3, "thermal monitoring ...");
                }
                int i7 = this.mHoldHeatTime;
                if (i7 < 0) {
                    this.mHoldHeatTime = 0;
                } else {
                    long j2 = this.mHoldHeatElapsedRealtime;
                    if (elapsedRealtime - j2 < AlarmManager.INTERVAL_HALF_HOUR && j2 > 0) {
                        this.mHoldHeatTime = (int) (((long) i7) + (elapsedRealtime - j2));
                    }
                }
                this.mHoldHeatElapsedRealtime = elapsedRealtime;
                if (DEBUG_THERMAL_TEMP) {
                    Slog.i(str3, "mHoldHeatTime = " + this.mHoldHeatTime + "  mStartAnalizyHeat=" + this.mStartAnalizyHeat + "  mHeatHoldUploadTime:" + this.mHeatHoldUploadTime);
                }
                if (this.mStartAnalizyHeat || this.mHoldHeat || this.mHoldHeatTime <= this.mHeatHoldUploadTime) {
                    i2 = batTemp;
                } else {
                    this.mHoldHeat = true;
                    this.mStartAnalizyHeat = true;
                    this.mGlobalMaxPhoneTemp = phoneTemp4;
                    i2 = batTemp;
                    this.mGlobalMaxBatTemp = i2;
                    this.mHeatReasonDetails.clear();
                    Message msg = new Message();
                    msg.what = 59;
                    msg.obj = Long.valueOf(elapsedRealtime - 720000);
                    msg.arg1 = this.mHeatReasonDetails.mAnalizyPosition - 1;
                    HeatReasonDetails heatReasonDetails = this.mHeatReasonDetails;
                    heatReasonDetails.mPhoneTemp = phoneTemp4;
                    heatReasonDetails.mBatTemp = i2;
                    this.mHandler.sendMessage(msg);
                }
                if (this.mGlobalMaxPhoneTemp < phoneTemp4) {
                    this.mGlobalMaxPhoneTemp = phoneTemp4;
                }
                if (this.mGlobalMaxBatTemp < i2) {
                    this.mGlobalMaxBatTemp = i2;
                }
                if (this.mHoldHeat && !this.mStartAnalizyHeat) {
                    this.mHeatReasonDetails.putHeatMaxTemp(this.mGlobalMaxPhoneTemp, this.mGlobalMaxBatTemp);
                }
            } else if (elapsedRealtime - this.mHoldHeatElapsedRealtime > ((long) this.mHeatHoldTimeThreshold)) {
                if (DEBUG_THERMAL_TEMP) {
                    Slog.i(str3, "phoneTemp monitor exit, phone temp:" + phoneTemp4 + " mHeatHoldTimeThreshold:" + this.mHeatHoldTimeThreshold);
                }
                this.mHoldHeat = false;
                this.mHoldHeatTime = -1;
                this.mHoldHeatElapsedRealtime = 0;
                this.mHaveCaptured = false;
            } else {
                if (DEBUG_THERMAL_TEMP) {
                    Slog.i(str3, "phoneTemp is decreasing, phoneTemp:" + phoneTemp4);
                }
                if (this.mHoldHeat && !this.mStartAnalizyHeat && this.mHoldHeatTime > 0) {
                    if (DEBUG_THERMAL_TEMP) {
                        Slog.i(str3, "uploadHeatEvent now");
                    }
                    this.mHeatReasonDetails.uploadHeatEvent();
                }
                this.mHoldHeatTime = -1;
            }
        }
    }

    private boolean checkCurrentTimeChanged(long currentTime, long lastCurrentTime) {
        if (lastCurrentTime - currentTime <= 7200000 || lastCurrentTime <= 1471228928) {
            return false;
        }
        return true;
    }

    public void setThermalConfig() {
        if (OppoThermalManager.mThermalFeatureOn != this.mThermalFeatureOn) {
            if (OppoThermalManager.mThermalFeatureOn) {
                this.mThermalHistoryCur.clear();
                OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
                thermalItem.cmd = 0;
                addThermalHistoryBufferLocked((byte) 0, thermalItem, true);
                this.mThermalFeatureOn = OppoThermalManager.mThermalFeatureOn;
                this.mThermalUploadDcs = OppoThermalManager.mThermalUploadDcs;
                this.mThermalUploadLog = OppoThermalManager.mThermalUploadLog;
                this.mThermalCaptureLog = OppoThermalManager.mThermalCaptureLog;
                this.mRecordThermalHistory = OppoThermalManager.mRecordThermalHistory;
                this.mThermalCaptureLogThreshold = OppoThermalManager.mThermalCaptureLogThreshold;
                this.mThermalUploadErrLog = OppoThermalManager.mThermalUploadErrLog;
                this.mMonitorAppLimitTime = OppoThermalManager.mMonitorAppLimitTime;
                this.mHeatHoldTimeThreshold = OppoThermalManager.mHeatHoldTimeThreshold;
                this.mThermalBatteryTemp = OppoThermalManager.mThermalBatteryTemp;
                this.mThermalMonitorApp.clear();
                this.mThermalMonitorApp.addAll(OppoThermalManager.mMonitorAppList);
                this.mMonitorAppAll = OppoThermalManager.mMonitorAppAll;
                if (this.mHandler.hasMessages(60)) {
                    this.mHandler.removeMessages(60);
                }
                this.mHandler.sendEmptyMessageDelayed(60, 4000);
                if (this.mHandler.hasMessages(62)) {
                    this.mHandler.removeMessages(62);
                }
                this.mHandler.sendEmptyMessageDelayed(62, 15000);
            } else {
                this.mThermalFeatureOn = OppoThermalManager.mThermalFeatureOn;
                this.mThermalUploadDcs = false;
                this.mThermalUploadLog = false;
                this.mThermalCaptureLog = false;
                this.mRecordThermalHistory = false;
                this.mThermalUploadErrLog = false;
                this.mThermalMonitorApp.clear();
                clearThermalStatsBuffer();
                cancleUploadAlarm();
            }
        }
        this.mMoreHeatThreshold = OppoThermalManager.mMoreHeatThreshold;
        this.mHeatThreshold = OppoThermalManager.mHeatThreshold;
        this.mLessHeatThreshold = OppoThermalManager.mLessHeatThreshold;
        this.mPreHeatThreshold = OppoThermalManager.mPreHeatThreshold;
        this.mHeatIncRatioThreshold = OppoThermalManager.mHeatIncRatioThreshold;
        this.mHeatHoldTimeThreshold = OppoThermalManager.mHeatHoldTimeThreshold;
        this.mHeatHoldUploadTime = OppoThermalManager.mHeatHoldUploadTime;
        this.mHeatRecInterv = OppoThermalManager.mHeatRecInterv;
        this.mCpuLoadRecThreshold = OppoThermalManager.mCpuLoadRecThreshold;
        this.mCpuLoadRecInterv = OppoThermalManager.mCpuLoadRecInterv;
        this.mTopCpuRecThreshold = OppoThermalManager.mTopCpuRecThreshold;
        this.mTopCpuRecInterv = OppoThermalManager.mTopCpuRecInterv;
        this.mCaptureCpuFeqInterVal = ((long) OppoThermalManager.mHeatTopProInterval) * 1000;
        this.mSimpleTopProInterVal = ((long) OppoThermalManager.mHeatTopProInterval) * 1000;
    }

    public void resetThermalHistory() {
        OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
        thermalItem.cmd = OppoBaseBatteryStats.ThermalItem.CMD_RESET;
        addThermalHistoryBufferLocked(OppoBaseBatteryStats.ThermalItem.CMD_RESET, thermalItem, true);
    }

    public void setThermalHeatThreshold(PrintWriter pw, int threshold) {
        this.mHeatThreshold = threshold;
        pw.println("Battery set heatthreshold mHeatThreshold = " + Integer.toString(threshold));
    }

    public void toggleThermalDebugSwith(PrintWriter pw, int on) {
        if (on == 1) {
            DEBUG_THERMAL_TEMP = true;
        } else {
            DEBUG_THERMAL_TEMP = false;
        }
        pw.println("Battery set debug switch = " + DEBUG_THERMAL_TEMP);
    }

    public void setHeatBetweenTime(PrintWriter pw, int time) {
        this.mHeatHoldTimeThreshold = time;
        pw.println("Battery set heatBetweenTime = " + Integer.toString(this.mHeatHoldTimeThreshold));
    }

    public void setMonitorAppLimitTime(PrintWriter pw, int limitTime) {
        this.mMonitorAppLimitTime = limitTime;
        pw.println("Battery set setMonitorAppLimitTime = " + Integer.toString(limitTime));
    }

    public void getMonitorAppLocked(PrintWriter pw) {
        Iterator<String> it = this.mThermalMonitorApp.iterator();
        while (it.hasNext()) {
            pw.println("getMonitorApp:" + it.next());
        }
    }

    public void printChargeMapLocked(PrintWriter pw) {
        try {
            for (Map.Entry<String, String> entry : this.mChargeUploadMap.entrySet()) {
                pw.println("mChargeUploadMap " + entry.getKey() + SettingsStringUtil.DELIMITER + entry.getValue());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void printThermalHeatThreshold(PrintWriter pw) {
        pw.println("Battery get heatthreshold mHeatThreshold = " + Integer.toString(this.mHeatThreshold));
    }

    public void printThermalUploadTemp(PrintWriter pw) {
        Map<String, String> upLoadMap = getUploadThermalTemp();
        if (upLoadMap.size() > 0) {
            try {
                for (Map.Entry<String, String> entry : upLoadMap.entrySet()) {
                    pw.println("uploadThermalTemp " + entry.getKey() + SettingsStringUtil.DELIMITER + entry.getValue());
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else {
            pw.println("no upload message");
        }
    }

    public void setThermalCpuLoading(int load1, int load5, int load15, int cpuLoading, int maxCpu, String cpuProc, String simpleTopProc) {
        if (DEBUG_THERMAL_TEMP) {
            Slog.d(TAG, "setThermalCpuLoading: mThermalFeatureOn" + this.mThermalFeatureOn + " load1:" + load1 + " load5:" + load5 + " load15:" + load15 + " cpuLoading:" + cpuLoading + " maxCpu:" + maxCpu + " cpuProc:" + cpuProc + "  phoneTemp:" + this.mThermalHistoryCur.phoneTemp + "  simpleTopProc:" + simpleTopProc);
        }
        int currentPhoneTemp = this.mThermalHistoryCur.phoneTemp;
        if (this.mThermalHistoryCur.phoneTemp >= this.mPreHeatThreshold - 20) {
            Slog.d(TAG, "SimpleTopProcesses: " + simpleTopProc);
        }
        this.mSimpleTopProcesses = simpleTopProc;
        if (currentPhoneTemp >= this.mPreHeatThreshold && !this.mHaveCaptured) {
            this.mSimpleTopProcessesNeedUpload = this.mSimpleTopProcesses;
            Slog.d(TAG, "mSimpleTopProcessesNeedUpload: " + this.mSimpleTopProcessesNeedUpload);
            if (this.cpuFreqReader != null && SystemClock.elapsedRealtime() - this.mCaptureCpuFeqElapsRealtime > this.mCaptureCpuFeqInterVal) {
                this.mCaptureCpuFeqElapsRealtime = SystemClock.elapsedRealtime();
                this.mCpuFreqValues = this.cpuFreqReader.getSimpleCpuFreqInfor();
                if (this.mCpuFreqValues != null) {
                    this.mCpuFreqValues += "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis())) + "]";
                    this.mCpuFreqValuesNeedUpload = this.mCpuFreqValues;
                    Slog.d(TAG, "cpuFreqs: " + this.mCpuFreqValues);
                }
            }
            this.mHaveCaptured = true;
        } else if (currentPhoneTemp < this.mPreHeatThreshold) {
            Slog.d(TAG, "reset mHaveCaptured:" + this.mHaveCaptured);
            this.mHaveCaptured = false;
        }
        if (this.mThermalFeatureOn) {
            this.mThermalHistoryCur.cpuLoading = cpuLoading;
            calculateEnviTemp(load5, load15);
            if (maxCpu > this.mTopCpuRecThreshold || Math.abs(maxCpu - this.mThermalHistoryCur.topCpu) > this.mTopCpuRecInterv || !(this.mThermalHistoryCur.topProc == null || cpuProc == null || this.mThermalHistoryCur.topProc.equals(cpuProc))) {
                OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
                thermalItem.topCpu = maxCpu;
                thermalItem.topProc = cpuProc;
                addThermalHistoryBufferLocked(OppoBaseBatteryStats.ThermalItem.CMD_TOPPROCINFO, thermalItem, true);
            }
        }
    }

    private void calculateEnviTemp(int load5, int load15) {
        if (this.mThermalHistoryCur.chargePlug > 0 || this.mThermalHistoryCur.flashlightOn) {
            this.mCpuIdleCheckCount = 0;
        }
        if (SystemClock.elapsedRealtime() < AlarmManager.INTERVAL_FIFTEEN_MINUTES) {
            this.mCpuIdleCheckCount = 0;
        }
        if (load5 > 100 || load15 > 100) {
            this.mCpuIdleCheckCount = 0;
        } else {
            this.mCpuIdleCheckCount++;
        }
        if (this.mCpuIdleCheckCount >= 3) {
            this.mCpuIdleCheckCount = 0;
            SystemClock.uptimeMillis();
            OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
            thermalItem.enviTemp = this.mGlobalBatTemp;
            addThermalHistoryBufferLocked(OppoBaseBatteryStats.ThermalItem.CMD_ENVITEMP, thermalItem, true);
        }
    }

    public void addThermalHistoryBufferLocked(byte cmd, int backlight, OppoBaseBatteryStats.ThermalItem cur, boolean isAdd) {
        synchronized (this.mLock) {
            if (!this.mIteratingThermalHistory) {
                cur.cmd = cmd;
                cur.upTime = SystemClock.uptimeMillis();
                cur.elapsedRealtime = SystemClock.elapsedRealtime();
                cur.backlight = backlight;
                if (cmd == 0 || cmd == 19 || cmd == 25) {
                    cur.currentTime = System.currentTimeMillis();
                    cur.baseElapsedRealtime = cur.elapsedRealtime;
                }
                this.mThermalHistoryBufferLastPos = this.mThermalHistoryBuffer.dataPosition();
                this.mThermalHistoryLastLastWritten.setTo(this.mThermalHistoryLastWritten);
                this.mThermalHistoryLastWritten.setTo(cur);
                writeThermalHistoryDelta(cmd, this.mThermalHistoryBuffer, this.mThermalHistoryLastWritten, this.mThermalHistoryLastLastWritten, isAdd);
            }
        }
    }

    public void addThermalHistoryBufferLocked(byte cmd, OppoBaseBatteryStats.ThermalItem cur, boolean isAdd) {
        int backlight = this.mStats.isScreenOn() ? this.mScreenBrightness : 0;
        synchronized (this.mLock) {
            if (!this.mIteratingThermalHistory || cur.cmd == 19) {
                cur.cmd = cmd;
                cur.backlight = backlight;
                cur.upTime = SystemClock.uptimeMillis();
                cur.elapsedRealtime = SystemClock.elapsedRealtime();
                if (cmd == 0 || cmd == 19 || cmd == 25) {
                    cur.currentTime = System.currentTimeMillis();
                    cur.baseElapsedRealtime = cur.elapsedRealtime;
                }
                this.mThermalHistoryBufferLastPos = this.mThermalHistoryBuffer.dataPosition();
                this.mThermalHistoryLastLastWritten.setTo(this.mThermalHistoryLastWritten);
                this.mThermalHistoryLastWritten.setTo(cur);
                if (this.mThermalUploadDcs) {
                    collectThermalTempMap(this.mThermalHistoryLastWritten, this.mThermalHistoryLastLastWritten);
                }
                if (!this.mStartAnalizyHeat && isAdd) {
                    this.mHeatReasonDetails.addToHeatItem(this.mThermalHistoryLastWritten);
                }
                collectMoinitAppMap(this.mThermalHistoryLastWritten, this.mThermalHistoryLastLastWritten);
                collectChargeMap(this.mThermalHistoryLastWritten, this.mThermalHistoryLastLastWritten);
                writeThermalHistoryDelta(cmd, this.mThermalHistoryBuffer, this.mThermalHistoryLastWritten, this.mThermalHistoryLastLastWritten, isAdd);
            }
        }
    }

    public void writeThermalHistoryDelta(byte cmd, Parcel dest, OppoBaseBatteryStats.ThermalItem cur, OppoBaseBatteryStats.ThermalItem last, boolean isAdd) {
        StringBuilder sb;
        if (!(!DEBUG_THERMAL_TEMP || this.mThermalHistoryBuffer == null || this.mThermalBuilder == null)) {
            Slog.d(TAG, "mThermalBuilder size=" + this.mThermalBuilder.length() + " mThermalHistoryBuffer size=" + this.mThermalHistoryBuffer.dataSize() + "  mRecordThermalHistory=" + this.mRecordThermalHistory);
        }
        if (this.mRecordThermalHistory && this.mThermalBuilder != null) {
            if (last == null || cmd == 0) {
                int tempInt = (-16777216 & (cur.cmd << OppoBaseBatteryStats.ThermalItem.CMD_ENVITEMP)) | (16760832 & (cur.cpuLoading << 14)) | (cur.backlight & 16383);
                dest.writeInt(tempInt);
                addThermalDetalToStringBuilder(tempInt, isAdd, false);
                long tempLong = (cur.elapsedRealtime << 5) | ((long) ((cur.cameraOn ? 1 : 0) << 4)) | ((long) ((cur.audioOn ? 1 : 0) << 3)) | ((long) ((cur.videoOn ? 1 : 0) << 2)) | ((long) ((cur.gpsOn ? 1 : 0) << 1)) | (cur.flashlightOn ? 1 : 0);
                dest.writeLong(tempLong);
                addThermalDetalToStringBuilder(tempLong, isAdd, false);
                dest.writeLong((cur.upTime << 8) | ((long) (cur.volume & 255)));
                addThermalDetalToStringBuilder((cur.upTime << 8) | ((long) (cur.volume & 127)) | ((long) (cur.isAutoBrightness ? 128 : 0)), isAdd, false);
                dest.writeLong(cur.currentTime);
                addThermalDetalToStringBuilder(cur.currentTime, isAdd, false);
                dest.writeLong(cur.baseElapsedRealtime);
                addThermalDetalToStringBuilder(cur.baseElapsedRealtime, isAdd, true);
            } else if (cur.phoneTemp != INVALID_DATA) {
                int tempInt2 = (-16777216 & (cur.cmd << OppoBaseBatteryStats.ThermalItem.CMD_ENVITEMP)) | (16760832 & (cur.cpuLoading << 14)) | (cur.backlight & 16383);
                dest.writeInt(tempInt2);
                addThermalDetalToStringBuilder(tempInt2, isAdd, false);
                long tempLong2 = (cur.elapsedRealtime << 5) | ((long) ((cur.cameraOn ? 1 : 0) << 4)) | ((long) ((cur.audioOn ? 1 : 0) << 3)) | ((long) ((cur.videoOn ? 1 : 0) << 2)) | ((long) ((cur.gpsOn ? 1 : 0) << 1)) | (cur.flashlightOn ? 1 : 0);
                dest.writeLong(tempLong2);
                addThermalDetalToStringBuilder(tempLong2, isAdd, false);
                dest.writeLong(((long) (cur.volume & 127)) | (cur.upTime << 8) | ((long) (cur.isAutoBrightness ? 128 : 0)));
                addThermalDetalToStringBuilder((cur.upTime << 8) | ((long) (cur.volume & 127)) | ((long) (cur.isAutoBrightness ? 128 : 0)), isAdd, false);
                if (cmd != 0) {
                    if (cmd == 1) {
                        int batInfo = buildThermalBatteryInfo(cur);
                        dest.writeInt(batInfo);
                        addThermalDetalToStringBuilder(batInfo, isAdd, false);
                        long thermalInfo = buildThermalTempInfo(cur);
                        dest.writeLong(thermalInfo);
                        addThermalDetalToStringBuilder(thermalInfo, isAdd, true);
                    } else if (cmd == 3) {
                        addThermalDetalToStringBuilder("", isAdd, true);
                    } else if (cmd == 4) {
                        int wifiInfo = ((cur.wifiStats << 16) & -65536) | (cur.wifiSignal & 65535);
                        dest.writeInt(wifiInfo);
                        addThermalDetalToStringBuilder(wifiInfo, isAdd, true);
                    } else if (cmd == 5) {
                        dest.writeBoolean(cur.phoneOnff);
                        addThermalDetalToStringBuilder(cur.phoneOnff, isAdd, true);
                    } else if (cmd == 6) {
                        dest.writeByte(cur.phoneState);
                        addThermalDetalToStringBuilder(cur.phoneState, isAdd, true);
                    } else if (cmd == 7) {
                        dest.writeByte(cur.phoneSignal);
                        addThermalDetalToStringBuilder(cur.phoneSignal, isAdd, true);
                    } else if (cmd == 8) {
                        dest.writeBoolean(cur.dataNetStatus);
                        addThermalDetalToStringBuilder(cur.dataNetStatus, isAdd, true);
                    } else if (cmd == 9) {
                        dest.writeByte(cur.connectNetType);
                        addThermalDetalToStringBuilder(cur.connectNetType, isAdd, true);
                    } else if (cmd == 10) {
                        dest.writeBoolean(cur.cameraOn);
                        addThermalDetalToStringBuilder(cur.cameraOn, isAdd, true);
                    } else if (cmd == 11) {
                        dest.writeBoolean(cur.audioOn);
                        addThermalDetalToStringBuilder(cur.audioOn, isAdd, true);
                    } else if (cmd == 12) {
                        dest.writeBoolean(cur.videoOn);
                        addThermalDetalToStringBuilder(cur.videoOn, isAdd, true);
                    } else if (cmd == 13) {
                        dest.writeBoolean(cur.gpsOn);
                        addThermalDetalToStringBuilder(cur.gpsOn, isAdd, true);
                    } else if (cmd == 14) {
                        dest.writeBoolean(cur.flashlightOn);
                        addThermalDetalToStringBuilder(cur.flashlightOn, isAdd, true);
                    } else if (cmd == 15) {
                        dest.writeString(cur.jobSchedule);
                        addThermalDetalToStringBuilder(cur.jobSchedule, isAdd, true);
                    } else if (cmd == 16) {
                        dest.writeString(cur.netSync);
                        addThermalDetalToStringBuilder(cur.netSync, isAdd, true);
                    } else if (cmd == 17) {
                        dest.writeString(cur.foreProc);
                        addThermalDetalToStringBuilder(cur.foreProc, isAdd, false);
                        dest.writeString(cur.versionName);
                        addThermalDetalToStringBuilder(cur.versionName, isAdd, true);
                    } else if (cmd == 18) {
                        dest.writeString(cur.topProc);
                        addThermalDetalToStringBuilder(cur.topProc, isAdd, false);
                        dest.writeInt(cur.topCpu);
                        addThermalDetalToStringBuilder(cur.topCpu, isAdd, true);
                    } else if (cmd == 19) {
                        dest.writeLong(cur.currentTime);
                        addThermalDetalToStringBuilder(cur.currentTime, isAdd, false);
                        dest.writeLong(cur.baseElapsedRealtime);
                        addThermalDetalToStringBuilder(cur.baseElapsedRealtime, isAdd, false);
                        int batInfo2 = buildThermalBatteryInfo(cur);
                        dest.writeInt(batInfo2);
                        addThermalDetalToStringBuilder(batInfo2, isAdd, false);
                        long thermalInfo2 = buildThermalTempInfo(cur);
                        dest.writeLong(thermalInfo2);
                        addThermalDetalToStringBuilder(thermalInfo2, isAdd, false);
                        dest.writeByte(cur.thermalRatio);
                        addThermalDetalToStringBuilder(cur.thermalRatio, isAdd, false);
                        dest.writeByte(cur.thermalRatio1);
                        addThermalDetalToStringBuilder(cur.thermalRatio1, isAdd, false);
                        dest.writeByte(cur.thermalRatio2);
                        addThermalDetalToStringBuilder(cur.thermalRatio2, isAdd, false);
                        dest.writeByte(cur.thermalRatio3);
                        addThermalDetalToStringBuilder(cur.thermalRatio3, isAdd, false);
                        dest.writeInt(cur.enviTemp);
                        addThermalDetalToStringBuilder(cur.enviTemp, isAdd, false);
                        int wifiInfo2 = ((cur.wifiStats << 16) & -65536) | (cur.wifiSignal & 65535);
                        dest.writeInt(wifiInfo2);
                        addThermalDetalToStringBuilder(wifiInfo2, isAdd, false);
                        dest.writeBoolean(cur.phoneOnff);
                        addThermalDetalToStringBuilder(cur.phoneOnff, isAdd, false);
                        dest.writeByte(cur.phoneState);
                        addThermalDetalToStringBuilder(cur.phoneState, isAdd, false);
                        dest.writeByte(cur.phoneSignal);
                        addThermalDetalToStringBuilder(cur.phoneSignal, isAdd, false);
                        dest.writeBoolean(cur.dataNetStatus);
                        addThermalDetalToStringBuilder(cur.dataNetStatus, isAdd, false);
                        dest.writeByte(cur.connectNetType);
                        addThermalDetalToStringBuilder(cur.connectNetType, isAdd, false);
                        dest.writeString(cur.jobSchedule);
                        addThermalDetalToStringBuilder(cur.jobSchedule, isAdd, false);
                        dest.writeString(cur.netSync);
                        addThermalDetalToStringBuilder(cur.netSync, isAdd, false);
                        dest.writeString(cur.foreProc);
                        addThermalDetalToStringBuilder(cur.foreProc, isAdd, false);
                        dest.writeString(cur.versionName);
                        addThermalDetalToStringBuilder(cur.versionName, isAdd, false);
                        dest.writeString(cur.topProc);
                        addThermalDetalToStringBuilder(cur.topProc, isAdd, false);
                        dest.writeInt(cur.topCpu);
                        addThermalDetalToStringBuilder(cur.topCpu, isAdd, true);
                    } else if (cmd == 20) {
                        dest.writeByte(cur.thermalRatio);
                        addThermalDetalToStringBuilder(cur.thermalRatio, isAdd, true);
                    } else if (cmd == 21) {
                        dest.writeByte(cur.thermalRatio1);
                        addThermalDetalToStringBuilder(cur.thermalRatio1, isAdd, true);
                    } else if (cmd == 22) {
                        dest.writeByte(cur.thermalRatio2);
                        addThermalDetalToStringBuilder(cur.thermalRatio2, isAdd, true);
                    } else if (cmd == 23) {
                        dest.writeByte(cur.thermalRatio3);
                        addThermalDetalToStringBuilder(cur.thermalRatio3, isAdd, true);
                    } else if (cmd == 24) {
                        dest.writeInt(cur.enviTemp);
                        addThermalDetalToStringBuilder(cur.enviTemp, isAdd, true);
                    } else if (cmd == 25) {
                        dest.writeLong(cur.currentTime);
                        addThermalDetalToStringBuilder(cur.currentTime, isAdd, false);
                        dest.writeLong(cur.baseElapsedRealtime);
                        addThermalDetalToStringBuilder(cur.baseElapsedRealtime, isAdd, true);
                    } else if (cmd == 26) {
                        addThermalDetalToStringBuilder("", isAdd, true);
                    }
                }
                if (this.mThermalHistoryBuffer != null && (sb = this.mThermalBuilder) != null) {
                    if ((sb.length() >= 65536 || this.mThermalHistoryBuffer.dataSize() >= 131072) && !this.mHandler.hasMessages(64)) {
                        this.mHandler.sendEmptyMessageDelayed(64, 0);
                    }
                }
            }
        }
    }

    public void addThermalDetalToStringBuilder(int info, boolean isAdd, boolean isEnd) {
        if (isAdd) {
            this.mThermalBuilder.append(info);
            if (isEnd) {
                this.mThermalBuilder.append(System.getProperty("line.separator"));
            } else {
                this.mThermalBuilder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
        }
    }

    public void addThermalDetalToStringBuilder(long info, boolean isAdd, boolean isEnd) {
        if (isAdd) {
            this.mThermalBuilder.append(info);
            if (isEnd) {
                this.mThermalBuilder.append(System.getProperty("line.separator"));
            } else {
                this.mThermalBuilder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
        }
    }

    public void addThermalDetalToStringBuilder(byte info, boolean isAdd, boolean isEnd) {
        if (isAdd) {
            this.mThermalBuilder.append((int) info);
            if (isEnd) {
                this.mThermalBuilder.append(System.getProperty("line.separator"));
            } else {
                this.mThermalBuilder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
        }
    }

    public void addThermalDetalToStringBuilder(String info, boolean isAdd, boolean isEnd) {
        if (isAdd) {
            this.mThermalBuilder.append(info);
            if (isEnd) {
                this.mThermalBuilder.append(System.getProperty("line.separator"));
            } else {
                this.mThermalBuilder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
        }
    }

    public void addThermalDetalToStringBuilder(boolean info, boolean isAdd, boolean isEnd) {
        if (isAdd) {
            this.mThermalBuilder.append(info);
            if (isEnd) {
                this.mThermalBuilder.append(System.getProperty("line.separator"));
            } else {
                this.mThermalBuilder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
        }
    }

    private void collectMoinitAppMap(OppoBaseBatteryStats.ThermalItem cur, OppoBaseBatteryStats.ThermalItem last) {
        long tempTime;
        long tempTime2;
        long tempTime3;
        if (this.mThermalMonitorApp.contains(last.foreProc) || this.mMonitorAppAll) {
            long deltaTime = cur.elapsedRealtime - last.elapsedRealtime;
            if (deltaTime < 0) {
                deltaTime = 0;
            } else if (deltaTime > 10800000) {
                deltaTime = 0;
            }
            long tempTime4 = 0;
            if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--Time")) {
                try {
                    tempTime4 = this.mTempMonitorAppMap.get(last.foreProc + "--Time").longValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.mTempMonitorAppMap.put(last.foreProc + "--Time", Long.valueOf(deltaTime + tempTime4));
            if (last.flashlightOn) {
                tempTime4 = 0;
                if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--FlashOn")) {
                    try {
                        tempTime4 = this.mTempMonitorAppMap.get(last.foreProc + "--FlashOn").longValue();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                this.mTempMonitorAppMap.put(last.foreProc + "--FlashOn", Long.valueOf(deltaTime + tempTime4));
            }
            if (last.chargePlug != 0) {
                tempTime4 = 0;
                if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--Charge")) {
                    try {
                        tempTime4 = this.mTempMonitorAppMap.get(last.foreProc + "--Charge").longValue();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
                this.mTempMonitorAppMap.put(last.foreProc + "--Charge", Long.valueOf(deltaTime + tempTime4));
            }
            if (!this.mTempMonitorAppMap.containsKey(last.foreProc + "--StartBatRm")) {
                this.mTempMonitorAppMap.put(last.foreProc + "--StartBatRm", Long.valueOf((long) cur.batRm));
            }
            this.mTempMonitorAppMap.put(last.foreProc + "--EndBatRm", Long.valueOf((long) cur.batRm));
            if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--MaxPhoneTemp")) {
                long phoneTemp = 0;
                try {
                    phoneTemp = this.mTempMonitorAppMap.get(last.foreProc + "--MaxPhoneTemp").longValue();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
                if (((long) cur.phoneTemp) > phoneTemp) {
                    this.mTempMonitorAppMap.put(last.foreProc + "--MaxPhoneTemp", Long.valueOf((long) cur.phoneTemp));
                }
            } else {
                this.mTempMonitorAppMap.put(last.foreProc + "--MaxPhoneTemp", Long.valueOf((long) cur.phoneTemp));
            }
            if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--MaxBatTemp")) {
                long batTemp = 0;
                try {
                    batTemp = this.mTempMonitorAppMap.get(last.foreProc + "--MaxBatTemp").longValue();
                } catch (Exception e5) {
                    e5.printStackTrace();
                }
                if (((long) cur.batTemp) > batTemp) {
                    this.mTempMonitorAppMap.put(last.foreProc + "--MaxBatTemp", Long.valueOf((long) cur.batTemp));
                }
            } else {
                this.mTempMonitorAppMap.put(last.foreProc + "--MaxBatTemp", Long.valueOf((long) cur.batTemp));
            }
            if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--AppCpu")) {
                long tempAppCpu = 0;
                try {
                    tempAppCpu = this.mTempMonitorAppMap.get(last.foreProc + "--AppCpu").longValue();
                } catch (Exception e6) {
                    e6.printStackTrace();
                }
                this.mTempMonitorAppMap.put(last.foreProc + "--AppCpu", Long.valueOf((((long) last.topCpu) * deltaTime) + tempAppCpu));
            } else {
                this.mTempMonitorAppMap.put(last.foreProc + "--AppCpu", Long.valueOf(((long) last.topCpu) * deltaTime));
            }
            if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--Cpu")) {
                long tempAppCpu2 = 0;
                try {
                    tempAppCpu2 = this.mTempMonitorAppMap.get(last.foreProc + "--Cpu").longValue();
                } catch (Exception e7) {
                    e7.printStackTrace();
                }
                this.mTempMonitorAppMap.put(last.foreProc + "--Cpu", Long.valueOf((((long) last.cpuLoading) * deltaTime) + tempAppCpu2));
            } else {
                this.mTempMonitorAppMap.put(last.foreProc + "--Cpu", Long.valueOf(((long) last.cpuLoading) * deltaTime));
            }
            if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--Backlight")) {
                long tempBacklight = 0;
                try {
                    tempBacklight = this.mTempMonitorAppMap.get(last.foreProc + "--Backlight").longValue();
                } catch (Exception e8) {
                    e8.printStackTrace();
                }
                this.mTempMonitorAppMap.put(last.foreProc + "--Backlight", Long.valueOf((((long) last.backlight) * deltaTime) + tempBacklight));
            } else {
                this.mTempMonitorAppMap.put(last.foreProc + "--Backlight", Long.valueOf(((long) last.backlight) * deltaTime));
            }
            if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--Volume")) {
                long tempBacklight2 = 0;
                try {
                    tempBacklight2 = this.mTempMonitorAppMap.get(last.foreProc + "--Volume").longValue();
                } catch (Exception e9) {
                    e9.printStackTrace();
                }
                this.mTempMonitorAppMap.put(last.foreProc + "--Volume", Long.valueOf((((long) last.volume) * deltaTime) + tempBacklight2));
            } else {
                this.mTempMonitorAppMap.put(last.foreProc + "--Volume", Long.valueOf(((long) last.volume) * deltaTime));
            }
            if (last.connectNetType == 0) {
                long tempTime5 = 0;
                if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--NetMobile")) {
                    try {
                        tempTime5 = this.mTempMonitorAppMap.get(last.foreProc + "--NetMobile").longValue();
                    } catch (Exception e10) {
                        e10.printStackTrace();
                    }
                }
                this.mTempMonitorAppMap.put(last.foreProc + "--NetMobile", Long.valueOf(tempTime5 + deltaTime));
            }
            if (last.connectNetType == 1) {
                if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--NetWifi")) {
                    try {
                        tempTime3 = this.mTempMonitorAppMap.get(last.foreProc + "--NetWifi").longValue();
                    } catch (Exception e11) {
                        e11.printStackTrace();
                    }
                    this.mTempMonitorAppMap.put(last.foreProc + "--NetWifi", Long.valueOf(tempTime3 + deltaTime));
                }
                tempTime3 = 0;
                this.mTempMonitorAppMap.put(last.foreProc + "--NetWifi", Long.valueOf(tempTime3 + deltaTime));
            }
            if (last.connectNetType == -1) {
                if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--NetNone")) {
                    try {
                        tempTime2 = this.mTempMonitorAppMap.get(last.foreProc + "--NetNone").longValue();
                    } catch (Exception e12) {
                        e12.printStackTrace();
                    }
                    this.mTempMonitorAppMap.put(last.foreProc + "--NetNone", Long.valueOf(tempTime2 + deltaTime));
                }
                tempTime2 = 0;
                this.mTempMonitorAppMap.put(last.foreProc + "--NetNone", Long.valueOf(tempTime2 + deltaTime));
            }
            if (last.isAutoBrightness) {
                if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--AutoBrightness")) {
                    try {
                        tempTime = this.mTempMonitorAppMap.get(last.foreProc + "--AutoBrightness").longValue();
                    } catch (Exception e13) {
                        e13.printStackTrace();
                    }
                    this.mTempMonitorAppMap.put(last.foreProc + "--AutoBrightness", Long.valueOf(tempTime + deltaTime));
                }
                tempTime = 0;
                this.mTempMonitorAppMap.put(last.foreProc + "--AutoBrightness", Long.valueOf(tempTime + deltaTime));
            }
            if (!cur.foreProc.equals(last.foreProc)) {
                this.mStartBatteryLevel = cur.batPercent;
                if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--Time")) {
                    long totalTime = this.mTempMonitorAppMap.get(last.foreProc + "--Time").longValue();
                    boolean isValidateMap = true;
                    if (totalTime >= ((long) this.mMonitorAppLimitTime)) {
                        this.mEndBatteryLevel = cur.batPercent;
                        if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--FlashOn")) {
                            if ((100 * this.mTempMonitorAppMap.get(last.foreProc + "--FlashOn").longValue()) / totalTime > 20) {
                                isValidateMap = false;
                            }
                        }
                        if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--Cpu")) {
                            if (this.mTempMonitorAppMap.containsKey(last.foreProc + "--AppCpu")) {
                                long cpu = this.mTempMonitorAppMap.get(last.foreProc + "--Cpu").longValue();
                                long appCpu = this.mTempMonitorAppMap.get(last.foreProc + "--AppCpu").longValue();
                                if (cpu <= 0) {
                                    isValidateMap = false;
                                } else if ((100 * appCpu) / cpu < 10) {
                                    isValidateMap = false;
                                }
                                if (isValidateMap) {
                                    this.mMonitorAppMap = getMonitorAppMap(last.foreProc, last.versionName);
                                    startUploadMonitorApp();
                                }
                            }
                        }
                        try {
                            for (Map.Entry<String, Long> entry : this.mTempMonitorAppMap.entrySet()) {
                                Slog.i(TAG, "mTempMonitorAppMap " + entry.getKey() + SettingsStringUtil.DELIMITER + entry.getValue());
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }
                this.mTempMonitorAppMap.clear();
            }
        }
    }

    private void collectChargeMap(OppoBaseBatteryStats.ThermalItem cur, OppoBaseBatteryStats.ThermalItem last) {
        long deltaTime = cur.elapsedRealtime - last.elapsedRealtime;
        if (deltaTime < 0) {
            deltaTime = 0;
        } else if (deltaTime > 10800000) {
            deltaTime = 0;
        }
        if (cur.chargePlug != last.chargePlug) {
            this.mLastFast2Normal = 0;
            if (last.chargePlug == 0) {
                this.mTempChargeUploadMap.clear();
                int hour = Calendar.getInstance().get(11);
                this.mTempChargeUploadMap.put("startTemp", Long.valueOf((long) cur.batTemp));
                this.mTempChargeUploadMap.put("startBatrm", Long.valueOf((long) cur.batRm));
                this.mTempChargeUploadMap.put("startLevel", Long.valueOf((long) cur.batPercent));
                this.mTempChargeUploadMap.put("startTime", Long.valueOf((cur.elapsedRealtime - cur.baseElapsedRealtime) + cur.currentTime));
                this.mTempChargeUploadMap.put("startHour", Long.valueOf((long) hour));
                this.mTempChargeUploadMap.put("minTemp", Long.valueOf((long) cur.batTemp));
                this.mTempChargeUploadMap.put("maxTemp", Long.valueOf((long) cur.batTemp));
                this.mTempChargeUploadMap.put("chargeTime", Long.valueOf(cur.elapsedRealtime));
                return;
            }
            if (this.mTempChargeUploadMap.containsKey("chargeTime") && this.mTempChargeUploadMap.containsKey("startTime")) {
                long chargeTime = cur.elapsedRealtime - this.mTempChargeUploadMap.get("chargeTime").longValue();
                if (last.backlight > 0) {
                    if (!this.mTempChargeUploadMap.containsKey("screenOnTime")) {
                        this.mTempChargeUploadMap.put("screenOnTime", Long.valueOf(deltaTime));
                    } else {
                        this.mTempChargeUploadMap.put("screenOnTime", Long.valueOf(deltaTime + this.mTempChargeUploadMap.get("screenOnTime").longValue()));
                    }
                }
                if (chargeTime >= 120000) {
                    this.mTempChargeUploadMap.put("chargeTime", Long.valueOf(chargeTime));
                    this.mTempChargeUploadMap.put("endTemp", Long.valueOf((long) cur.batTemp));
                    this.mTempChargeUploadMap.put("endBatrm", Long.valueOf((long) cur.batRm));
                    this.mTempChargeUploadMap.put("endLevel", Long.valueOf((long) cur.batPercent));
                    this.mTempChargeUploadMap.put(CalendarContract.EXTRA_EVENT_END_TIME, Long.valueOf((cur.elapsedRealtime - cur.baseElapsedRealtime) + cur.currentTime));
                    this.mTempChargeUploadMap.put("chargePlug", Long.valueOf((long) last.chargePlug));
                    if (this.mTempChargeUploadMap.containsKey("minTemp")) {
                        if (((long) cur.batTemp) < this.mTempChargeUploadMap.get("minTemp").longValue()) {
                            this.mTempChargeUploadMap.put("minTemp", Long.valueOf((long) cur.batTemp));
                        }
                    }
                    if (this.mTempChargeUploadMap.containsKey("maxTemp")) {
                        if (((long) cur.batTemp) > this.mTempChargeUploadMap.get("maxTemp").longValue()) {
                            this.mTempChargeUploadMap.put("maxTemp", Long.valueOf((long) cur.batTemp));
                        }
                    }
                    this.mChargeUploadMap = getUploadChargeMap();
                    startUploadChargeMap();
                }
            }
            this.mTempChargeUploadMap.clear();
        } else if (last.chargePlug != 0) {
            if (!this.mTempChargeUploadMap.containsKey("startTime")) {
                this.mTempChargeUploadMap.clear();
                int hour2 = Calendar.getInstance().get(11);
                this.mTempChargeUploadMap.put("startTemp", Long.valueOf((long) cur.batTemp));
                this.mTempChargeUploadMap.put("startBatrm", Long.valueOf((long) cur.batRm));
                this.mTempChargeUploadMap.put("startLevel", Long.valueOf((long) cur.batPercent));
                this.mTempChargeUploadMap.put("startTime", Long.valueOf((cur.elapsedRealtime - cur.baseElapsedRealtime) + cur.currentTime));
                this.mTempChargeUploadMap.put("startHour", Long.valueOf((long) hour2));
                this.mTempChargeUploadMap.put("minTemp", Long.valueOf((long) cur.batTemp));
                this.mTempChargeUploadMap.put("maxTemp", Long.valueOf((long) cur.batTemp));
                this.mTempChargeUploadMap.put("chargeTime", Long.valueOf(cur.elapsedRealtime));
            }
            if (last.backlight > 0) {
                if (!this.mTempChargeUploadMap.containsKey("screenOnTime")) {
                    this.mTempChargeUploadMap.put("screenOnTime", Long.valueOf(deltaTime));
                } else {
                    this.mTempChargeUploadMap.put("screenOnTime", Long.valueOf(deltaTime + this.mTempChargeUploadMap.get("screenOnTime").longValue()));
                }
            }
            if (this.mTempChargeUploadMap.containsKey("minTemp")) {
                if (((long) cur.batTemp) < this.mTempChargeUploadMap.get("minTemp").longValue()) {
                    this.mTempChargeUploadMap.put("minTemp", Long.valueOf((long) cur.batTemp));
                }
            } else if (this.mTempChargeUploadMap.containsKey("maxTemp")) {
                if (((long) cur.batTemp) > this.mTempChargeUploadMap.get("maxTemp").longValue()) {
                    this.mTempChargeUploadMap.put("maxTemp", Long.valueOf((long) cur.batTemp));
                }
            }
            if (this.mLastFast2Normal != this.mGlobalFast2Normal) {
                this.mTempChargeUploadMap.put("f2nTemp", Long.valueOf((long) cur.batTemp));
                this.mTempChargeUploadMap.put("f2nLevel", Long.valueOf((long) cur.batPercent));
                this.mTempChargeUploadMap.put("f2nbatRm", Long.valueOf((long) cur.batRm));
                this.mTempChargeUploadMap.put("f2nTime", Long.valueOf(cur.currentTime));
            }
            this.mLastFast2Normal = this.mGlobalFast2Normal;
        }
    }

    private void collectThermalTempMap(OppoBaseBatteryStats.ThermalItem cur, OppoBaseBatteryStats.ThermalItem last) {
        String batTemp;
        String phoneTemp;
        long deltaTime = cur.elapsedRealtime - last.elapsedRealtime;
        if (deltaTime < 0) {
            deltaTime = 0;
        } else if (deltaTime > 10800000) {
            deltaTime = 0;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        long currentTime = (cur.elapsedRealtime - cur.baseElapsedRealtime) + cur.currentTime;
        long lastcurrentTime = (last.elapsedRealtime - last.baseElapsedRealtime) + last.currentTime;
        try {
            Integer hour = Integer.valueOf(Integer.parseInt(sdf.format(new Date(currentTime))));
            int lasthour = Integer.parseInt(sdf.format(new Date(lastcurrentTime)));
            int intPhoneTemp = 360;
            if (cur.phoneTemp > 360) {
                intPhoneTemp = cur.phoneTemp;
            }
            if (!(hour.intValue() == lasthour || hour.intValue() == -1 || lasthour == -1)) {
                Map<String, Integer> map = this.mThermalHourMap;
                if (!map.containsKey("hour" + Integer.toString(hour.intValue()))) {
                    Map<String, Integer> map2 = this.mThermalHourMap;
                    map2.put("hour" + Integer.toString(hour.intValue()), Integer.valueOf(intPhoneTemp));
                }
            }
            if (!(hour.intValue() != lasthour || hour.intValue() == -1 || lasthour == -1)) {
                Map<String, Integer> map3 = this.mThermalHourMap;
                if (!map3.containsKey("hour" + Integer.toString(hour.intValue()))) {
                    Map<String, Integer> map4 = this.mThermalHourMap;
                    map4.put("hour" + Integer.toString(hour.intValue()), Integer.valueOf(intPhoneTemp));
                } else {
                    Map<String, Integer> map5 = this.mThermalHourMap;
                    if (map5.get("hour" + Integer.toString(hour.intValue())).intValue() < intPhoneTemp) {
                        Map<String, Integer> map6 = this.mThermalHourMap;
                        map6.put("hour" + Integer.toString(hour.intValue()), Integer.valueOf(intPhoneTemp));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (last.phoneTemp != INVALID_DATA) {
            if (last.elapsedRealtime > JobInfo.MIN_BACKOFF_MILLIS) {
                if (last.phoneTemp / 10 <= 36) {
                    phoneTemp = "36";
                } else if (last.phoneTemp / 10 >= 60) {
                    phoneTemp = "60";
                } else {
                    phoneTemp = Integer.toString(last.phoneTemp / 10);
                }
                if (this.mThermalTempMap.containsKey(phoneTemp)) {
                    long tempTime = this.mThermalTempMap.get(phoneTemp).longValue();
                    this.mThermalTempMap.remove(phoneTemp);
                    this.mThermalTempMap.put(phoneTemp, Long.valueOf(deltaTime + tempTime));
                } else {
                    this.mThermalTempMap.put(phoneTemp, Long.valueOf(deltaTime));
                }
            }
        }
        if (last.batTemp != INVALID_DATA && last.elapsedRealtime > JobInfo.MIN_BACKOFF_MILLIS) {
            if (last.batTemp / 10 <= 36) {
                batTemp = "36";
            } else if (last.batTemp / 10 >= 60) {
                batTemp = "60";
            } else {
                batTemp = Integer.toString(last.batTemp / 10);
            }
            if (this.mBatTempMap.containsKey(batTemp)) {
                long tempTime2 = this.mBatTempMap.get(batTemp).longValue();
                this.mBatTempMap.remove(batTemp);
                this.mBatTempMap.put(batTemp, Long.valueOf(deltaTime + tempTime2));
                return;
            }
            this.mBatTempMap.put(batTemp, Long.valueOf(deltaTime));
        }
    }

    private int buildThermalBatteryInfo(OppoBaseBatteryStats.ThermalItem t) {
        return ((t.batPercent << 25) & -33554432) | ((t.chargePlug << 15) & 33521664) | ((t.batRm << 1) & 32766);
    }

    private long buildThermalTempInfo(OppoBaseBatteryStats.ThermalItem t) {
        return ((((long) t.phoneTemp3) & 4095) << 48) | ((((long) t.phoneTemp2) & 4095) << 36) | ((((long) t.phoneTemp1) & 4095) << 24) | ((4095 & ((long) t.phoneTemp)) << 12) | ((long) t.batTemp);
    }

    private void readThermalBatteryInfo(int batteryLevelInt, OppoBaseBatteryStats.ThermalItem out) {
        out.batPercent = (-33554432 & batteryLevelInt) >>> 25;
        out.chargePlug = (33521664 & batteryLevelInt) >>> 15;
        out.batRm = (batteryLevelInt & 32766) >>> 1;
    }

    private void readThermalTempInfo(long templong, OppoBaseBatteryStats.ThermalItem out) {
        out.phoneTemp3 = symbolInt((int) ((templong >> 48) & 4095));
        out.phoneTemp2 = symbolInt((int) ((templong >> 36) & 4095));
        out.phoneTemp1 = symbolInt((int) ((templong >> 24) & 4095));
        out.phoneTemp = symbolInt((int) ((templong >> 12) & 4095));
        out.batTemp = symbolInt((int) (templong & 4095));
    }

    public void clearThermalStatsBuffer() {
        synchronized (this.mLock) {
            this.mThermalHistoryBuffer.setDataPosition(0);
            this.mThermalHistoryBuffer.setDataSize(0);
            this.mThermalHistoryBuffer.setDataCapacity(this.mStats.getHistoryBufferSize());
            this.mThermalTempMap.clear();
            this.mBatTempMap.clear();
            this.mThermalHourMap.clear();
            if (this.mThermalHistoryLastLastWritten != null) {
                this.mThermalHistoryLastLastWritten.clear();
            }
        }
    }

    public void clearHistoryBuffer() {
        synchronized (this.mLock) {
            this.mThermalHistoryBuffer.setDataPosition(0);
            this.mThermalHistoryBuffer.setDataSize(0);
            this.mThermalHistoryBuffer.setDataCapacity(this.mStats.getHistoryBufferSize());
            this.mThermalHistoryBufferLastPos = -1;
            if (this.mThermalHistoryLastLastWritten != null) {
                this.mThermalHistoryLastLastWritten.clear();
            }
        }
    }

    public void addThermalScreenBrightnessEvent(long elapsedRealtime, long uptime, int backlight, int delayTime) {
        if (this.mThermalFeatureOn) {
            if (delayTime <= 0) {
                addThermalScreenBrightness(elapsedRealtime, uptime, backlight);
            } else {
                addThermalScreenBrightnessDelayed(elapsedRealtime, uptime, backlight, delayTime);
            }
        }
    }

    private void addThermalScreenBrightnessDelayed(long elapsedRealtime, long uptime, int backlight, int delayTime) {
        this.mHandler.removeMessages(57);
        Message msg = new Message();
        msg.what = 57;
        msg.arg1 = backlight;
        this.mHandler.sendMessageDelayed(msg, (long) delayTime);
    }

    private void addThermalScreenBrightness(long elapsedRealtime, long uptime, int backlight) {
        if (this.mHandler.hasMessages(57)) {
            this.mHandler.removeMessages(57);
        }
        addThermalHistoryBufferLocked((byte) 3, backlight, this.mThermalHistoryCur, true);
    }

    public void addThermalWifiStatus(long elapsedRealtime, long uptime, int status) {
        if (this.mThermalFeatureOn) {
            OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
            thermalItem.wifiStats = status;
            if (status == 0 || status == 3) {
                this.mThermalHistoryCur.wifiSignal = 0;
            } else {
                thermalItem.wifiSignal = this.mStats.getWifiSignalStrengthBin();
            }
            addThermalHistoryBufferLocked((byte) 4, this.mThermalHistoryCur, true);
        }
    }

    public void addThermalWifiRssi(long elapsedRealtime, long uptime, int wifiSignalStrengthBin) {
        if (this.mThermalFeatureOn && this.mThermalHistoryCur.wifiSignal != wifiSignalStrengthBin) {
            OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
            thermalItem.wifiSignal = wifiSignalStrengthBin;
            addThermalHistoryBufferLocked((byte) 4, thermalItem, true);
        }
    }

    public void addThermalPhoneOnOff(long elapsedRealtime, long uptime, boolean onOff) {
        if (this.mThermalFeatureOn) {
            OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
            thermalItem.phoneOnff = onOff;
            addThermalHistoryBufferLocked((byte) 5, thermalItem, true);
        }
    }

    public void addThermalPhoneState(long elapsedRealtime, long uptime, byte state) {
        if (this.mThermalFeatureOn && state != this.mThermalHistoryCur.phoneState) {
            OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
            thermalItem.phoneState = state;
            addThermalHistoryBufferLocked((byte) 6, thermalItem, true);
        }
    }

    public void addThermalPhoneSignal(long elapsedRealtime, long uptime, byte signal) {
        if (this.mThermalFeatureOn && signal >= 0 && signal <= 4) {
            OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
            thermalItem.phoneSignal = signal;
            addThermalHistoryBufferLocked((byte) 7, thermalItem, true);
        }
    }

    public void addThermalNetState(long elapsedRealtime, long uptime, boolean netState) {
        if (this.mThermalFeatureOn) {
            OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
            thermalItem.dataNetStatus = netState;
            addThermalHistoryBufferLocked((byte) 8, thermalItem, true);
        }
    }

    public void addThermalConnectType(long elapsedRealtime, long uptime, byte type) {
        if (this.mThermalFeatureOn) {
            this.mHandler.removeMessages(58);
            Message msg = new Message();
            msg.what = 58;
            msg.arg1 = type;
            this.mHandler.sendMessageDelayed(msg, 4000);
        }
    }

    public void addThermalCameraOnff(long elapsedRealtime, long uptime, boolean onOff) {
        if (this.mThermalFeatureOn) {
            this.mHandler.removeMessages(51);
            Message msg = new Message();
            msg.what = 51;
            msg.arg1 = onOff ? 1 : 0;
            this.mHandler.sendMessageDelayed(msg, 4000);
        }
    }

    public void addThermalAudioOnff(long elapsedRealtime, long uptime, boolean onOff) {
        if (this.mThermalFeatureOn) {
            this.mHandler.removeMessages(52);
            Message msg = new Message();
            msg.what = 52;
            msg.arg1 = onOff ? 1 : 0;
            this.mHandler.sendMessageDelayed(msg, 4000);
        }
    }

    public void addThermalVideoOnff(long elapsedRealtime, long uptime, boolean onOff) {
        if (this.mThermalFeatureOn) {
            this.mHandler.removeMessages(53);
            Message msg = new Message();
            msg.what = 53;
            msg.arg1 = onOff ? 1 : 0;
            this.mHandler.sendMessageDelayed(msg, 4000);
        }
    }

    public void addThermalGpsOnff(long elapsedRealtime, long uptime, boolean onOff) {
        if (this.mThermalFeatureOn) {
            this.mHandler.removeMessages(54);
            Message msg = new Message();
            msg.what = 54;
            msg.arg1 = onOff ? 1 : 0;
            this.mHandler.sendMessageDelayed(msg, 4000);
        }
    }

    public void addThermalFlashLightOnff(long elapsedRealtime, long uptime, boolean onOff) {
        if (this.mThermalFeatureOn) {
            this.mHandler.removeMessages(55);
            Message msg = new Message();
            msg.what = 55;
            msg.arg1 = onOff ? 1 : 0;
            this.mHandler.sendMessageDelayed(msg, 4000);
        }
    }

    private int eventTypeToMessageID(int thermalEventType) {
        if (thermalEventType == 2) {
            return 51;
        }
        if (thermalEventType == 3) {
            return 52;
        }
        if (thermalEventType == 4) {
            return 53;
        }
        if (thermalEventType == 5) {
            return 54;
        }
        if (thermalEventType != 6) {
            return -1;
        }
        return 55;
    }

    public void addThermalOnOffEvent(int eventType, long elapsedRealtime, long uptime, boolean on) {
        int messageId = eventTypeToMessageID(eventType);
        if (-1 == messageId) {
            Slog.e(TAG, "addThermalOnOffEvent, unsupport event type!");
        } else if (this.mThermalFeatureOn) {
            this.mHandler.removeMessages(messageId);
            Message msg = new Message();
            msg.what = messageId;
            msg.arg1 = on ? 1 : 0;
            this.mHandler.sendMessageDelayed(msg, 4000);
        }
    }

    public void addThermalJobProc(long elapsedRealtime, long uptime, String proc) {
        if (this.mThermalFeatureOn) {
            if ((this.mThermalHistoryCur.jobSchedule == null && proc != null && !proc.equals("null")) || (this.mThermalHistoryCur.jobSchedule != null && !this.mThermalHistoryCur.jobSchedule.equals(proc))) {
                OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
                thermalItem.jobSchedule = proc;
                addThermalHistoryBufferLocked((byte) 15, thermalItem, true);
            }
        }
    }

    public void addThermalnetSyncProc(long elapsedRealtime, long uptime, String proc) {
        if (this.mThermalFeatureOn) {
            if ((this.mThermalHistoryCur.netSync == null && proc != null && !proc.equals("null")) || (this.mThermalHistoryCur.netSync != null && !this.mThermalHistoryCur.netSync.equals(proc))) {
                OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
                thermalItem.netSync = proc;
                addThermalHistoryBufferLocked((byte) 16, thermalItem, true);
            }
        }
    }

    public void addThermalForeProc(long elapsedRealtime, long uptime, String proc, int uid) {
        if (this.mThermalFeatureOn) {
            if ((this.mThermalHistoryCur.foreProc == null && proc != null && !proc.equals("null")) || (this.mThermalHistoryCur.foreProc != null && !this.mThermalHistoryCur.foreProc.equals(proc))) {
                String versionName = "0000";
                String[] procString = proc.split(SettingsStringUtil.DELIMITER);
                if (procString[0] != null) {
                    try {
                        PackageInfo info = this.mPackageManger.getPackageInfoAsUser(procString[0], 0, -2);
                        if (info != null) {
                            versionName = info.versionName;
                        }
                    } catch (Exception e) {
                        Slog.e(TAG, "Error getting package info: " + procString[0]);
                    }
                }
                OppoBaseBatteryStats.ThermalItem thermalItem = this.mThermalHistoryCur;
                thermalItem.foreProc = proc;
                thermalItem.versionName = versionName;
                addThermalHistoryBufferLocked(OppoBaseBatteryStats.ThermalItem.CMD_FOREPRCINFO, thermalItem, true);
            }
        }
    }

    public OppoBaseBatteryStats.ThermalItem getThermalHistoryFromFile(BufferedReader reader, PrintWriter pw, OppoBaseBatteryStats.ThermalHistoryPrinter printer) throws IOException {
        OppoBaseBatteryStats.ThermalItem cur = new OppoBaseBatteryStats.ThermalItem();
        new OppoBaseBatteryStats.ThermalItem();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                return cur;
            }
            String[] tmp = line.split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            try {
                int tempInt = Integer.parseInt(tmp[0]);
                cur.backlight = tempInt & 16383;
                cur.cpuLoading = (tempInt >> 14) & 1023;
                cur.cmd = (byte) ((tempInt >> 24) & 255);
                byte cmd = cur.cmd;
                long tempLong = Long.parseLong(tmp[1]);
                cur.elapsedRealtime = tempLong >> 5;
                cur.cameraOn = ((31 & tempLong) >> 4) == 1;
                cur.audioOn = ((15 & tempLong) >> 3) == 1;
                cur.videoOn = ((7 & tempLong) >> 2) == 1;
                cur.gpsOn = ((3 & tempLong) >> 1) == 1;
                cur.flashlightOn = (tempLong & 1) == 1;
                long tempLong2 = Long.parseLong(tmp[2]);
                cur.upTime = tempLong2 >> 8;
                cur.volume = (int) (tempLong2 & 127);
                cur.isAutoBrightness = (tempLong2 & 128) == 128;
                if (cmd == 0) {
                    cur.currentTime = Long.parseLong(tmp[3]);
                    cur.baseElapsedRealtime = Long.parseLong(tmp[4]);
                } else if (cmd == 1) {
                    readThermalBatteryInfo(Integer.parseInt(tmp[3]), cur);
                    readThermalTempInfo(Long.parseLong(tmp[4]), cur);
                } else if (cmd == 2) {
                    readThermalTempInfo(Long.parseLong(tmp[3]), cur);
                } else if (cmd == 4) {
                    int wifiInfoInt = Integer.parseInt(tmp[3]);
                    cur.wifiStats = symbolInt((wifiInfoInt >> 16) & 65535);
                    cur.wifiSignal = symbolInt(65535 & wifiInfoInt);
                } else if (cmd == 5) {
                    cur.phoneOnff = Boolean.parseBoolean(tmp[3]);
                } else if (cmd == 6) {
                    cur.phoneState = Byte.parseByte(tmp[3]);
                } else if (cmd == 7) {
                    cur.phoneSignal = Byte.parseByte(tmp[3]);
                } else if (cmd == 8) {
                    cur.dataNetStatus = Boolean.parseBoolean(tmp[3]);
                } else if (cmd == 9) {
                    cur.connectNetType = Byte.parseByte(tmp[3]);
                } else if (cmd == 10) {
                    cur.cameraOn = Boolean.parseBoolean(tmp[3]);
                } else if (cmd == 11) {
                    cur.audioOn = Boolean.parseBoolean(tmp[3]);
                } else if (cmd == 12) {
                    cur.videoOn = Boolean.parseBoolean(tmp[3]);
                } else if (cmd == 13) {
                    cur.gpsOn = Boolean.parseBoolean(tmp[3]);
                } else if (cmd == 14) {
                    cur.flashlightOn = Boolean.parseBoolean(tmp[3]);
                } else if (cmd == 15) {
                    cur.jobSchedule = tmp[3];
                } else if (cmd == 16) {
                    cur.netSync = tmp[3];
                } else if (cmd == 17) {
                    cur.foreProc = tmp[3];
                    cur.versionName = tmp[4];
                } else if (cmd == 18) {
                    cur.topProc = tmp[3];
                    cur.topCpu = Integer.parseInt(tmp[4]);
                } else if (cmd == 19) {
                    cur.currentTime = Long.parseLong(tmp[3]);
                    cur.baseElapsedRealtime = Long.parseLong(tmp[4]);
                    readThermalBatteryInfo(Integer.parseInt(tmp[5]), cur);
                    readThermalTempInfo(Long.parseLong(tmp[6]), cur);
                    cur.thermalRatio = Byte.parseByte(tmp[7]);
                    cur.thermalRatio1 = Byte.parseByte(tmp[8]);
                    cur.thermalRatio2 = Byte.parseByte(tmp[9]);
                    cur.thermalRatio3 = Byte.parseByte(tmp[10]);
                    cur.enviTemp = Integer.parseInt(tmp[11]);
                    int wifiInfoInt2 = Integer.parseInt(tmp[12]);
                    cur.wifiStats = symbolInt((wifiInfoInt2 >> 16) & 65535);
                    cur.wifiSignal = symbolInt(65535 & wifiInfoInt2);
                    cur.phoneOnff = Boolean.parseBoolean(tmp[13]);
                    cur.phoneState = Byte.parseByte(tmp[14]);
                    cur.phoneSignal = Byte.parseByte(tmp[15]);
                    cur.dataNetStatus = Boolean.parseBoolean(tmp[16]);
                    cur.connectNetType = Byte.parseByte(tmp[17]);
                    cur.jobSchedule = tmp[18];
                    cur.netSync = tmp[19];
                    cur.foreProc = tmp[20];
                    cur.versionName = tmp[21];
                    cur.topProc = tmp[22];
                    cur.topCpu = Integer.parseInt(tmp[23]);
                } else if (cmd == 20) {
                    cur.thermalRatio = Byte.parseByte(tmp[3]);
                } else if (cmd == 21) {
                    cur.thermalRatio1 = Byte.parseByte(tmp[3]);
                } else if (cmd == 22) {
                    cur.thermalRatio2 = Byte.parseByte(tmp[3]);
                } else if (cmd == 23) {
                    cur.thermalRatio3 = Byte.parseByte(tmp[3]);
                } else if (cmd == 24) {
                    cur.enviTemp = Integer.parseInt(tmp[3]);
                } else if (cmd == 25) {
                    cur.currentTime = Long.parseLong(tmp[3]);
                    cur.elapsedRealtime = Long.parseLong(tmp[4]);
                }
                if (pw == null || printer == null) {
                    this.mThermalHistoryLastLastRead.setTo(this.mThermalHistoryLastRead);
                    this.mThermalHistoryLastRead.setTo(cur);
                    writeThermalHistoryDelta(cmd, this.mThermalHistoryBuffer, this.mThermalHistoryLastRead, this.mThermalHistoryLastLastRead, false);
                } else {
                    printer.printNextItem(pw, cur);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void readThermalHistoryDelta(Parcel src, OppoBaseBatteryStats.ThermalItem cur, long histStart) {
        int tempInt = src.readInt();
        cur.backlight = tempInt & 16383;
        cur.cpuLoading = (tempInt >> 14) & 1023;
        cur.cmd = (byte) ((tempInt >> 24) & 255);
        byte cmd = cur.cmd;
        long tempLong = src.readLong();
        cur.elapsedRealtime = tempLong >> 5;
        boolean z = false;
        cur.cameraOn = ((31 & tempLong) >> 4) == 1;
        cur.audioOn = ((15 & tempLong) >> 3) == 1;
        cur.videoOn = ((7 & tempLong) >> 2) == 1;
        cur.gpsOn = ((3 & tempLong) >> 1) == 1;
        cur.flashlightOn = (tempLong & 1) == 1;
        long tempLong2 = src.readLong();
        cur.upTime = tempLong2 >> 8;
        cur.volume = (int) (127 & tempLong2);
        if ((tempLong2 & 128) == 128) {
            z = true;
        }
        cur.isAutoBrightness = z;
        if (cmd == 0) {
            cur.currentTime = src.readLong();
            cur.baseElapsedRealtime = src.readLong();
        } else if (cmd == 1) {
            readThermalBatteryInfo(src.readInt(), cur);
            readThermalTempInfo(src.readLong(), cur);
        } else if (cmd == 2) {
            readThermalTempInfo(src.readLong(), cur);
        } else if (cmd == 4) {
            int wifiInfoInt = src.readInt();
            cur.wifiStats = symbolInt((wifiInfoInt >> 16) & 65535);
            cur.wifiSignal = symbolInt(65535 & wifiInfoInt);
        } else if (cmd == 5) {
            cur.phoneOnff = src.readBoolean();
        } else if (cmd == 6) {
            cur.phoneState = src.readByte();
        } else if (cmd == 7) {
            cur.phoneSignal = src.readByte();
        } else if (cmd == 8) {
            cur.dataNetStatus = src.readBoolean();
        } else if (cmd == 9) {
            cur.connectNetType = src.readByte();
        } else if (cmd == 10) {
            cur.cameraOn = src.readBoolean();
        } else if (cmd == 11) {
            cur.audioOn = src.readBoolean();
        } else if (cmd == 12) {
            cur.videoOn = src.readBoolean();
        } else if (cmd == 13) {
            cur.gpsOn = src.readBoolean();
        } else if (cmd == 14) {
            cur.flashlightOn = src.readBoolean();
        } else if (cmd == 15) {
            cur.jobSchedule = src.readString();
        } else if (cmd == 16) {
            cur.netSync = src.readString();
        } else if (cmd == 17) {
            cur.foreProc = src.readString();
            cur.versionName = src.readString();
        } else if (cmd == 18) {
            cur.topProc = src.readString();
            cur.topCpu = src.readInt();
        } else if (cmd == 19) {
            cur.currentTime = src.readLong();
            cur.baseElapsedRealtime = src.readLong();
            readThermalBatteryInfo(src.readInt(), cur);
            readThermalTempInfo(src.readLong(), cur);
            cur.thermalRatio = src.readByte();
            cur.thermalRatio1 = src.readByte();
            cur.thermalRatio2 = src.readByte();
            cur.thermalRatio3 = src.readByte();
            cur.enviTemp = src.readInt();
            int wifiInfoInt2 = src.readInt();
            cur.wifiStats = symbolInt((wifiInfoInt2 >> 16) & 65535);
            cur.wifiSignal = symbolInt(65535 & wifiInfoInt2);
            cur.phoneOnff = src.readBoolean();
            cur.phoneState = src.readByte();
            cur.phoneSignal = src.readByte();
            cur.dataNetStatus = src.readBoolean();
            cur.connectNetType = src.readByte();
            cur.jobSchedule = src.readString();
            cur.netSync = src.readString();
            cur.foreProc = src.readString();
            cur.versionName = src.readString();
            cur.topProc = src.readString();
            cur.topCpu = src.readInt();
        } else if (cmd == 20) {
            cur.thermalRatio = src.readByte();
        } else if (cmd == 21) {
            cur.thermalRatio1 = src.readByte();
        } else if (cmd == 22) {
            cur.thermalRatio2 = src.readByte();
        } else if (cmd == 23) {
            cur.thermalRatio3 = src.readByte();
        } else if (cmd == 24) {
            cur.enviTemp = src.readInt();
        } else if (cmd == 25) {
            cur.currentTime = src.readLong();
            cur.baseElapsedRealtime = src.readLong();
        }
    }

    public void startUploadTemp() {
        BackgroundThread.getHandler().post(new Runnable() {
            /* class com.android.internal.os.OppoThermalStatsHelper.AnonymousClass1 */

            public void run() {
                OppoThermalStatsHelper.this.startIteratingThermalHistoryLocked();
                OppoThermalStatsHelper.this.uploadThermalTemp();
                OppoThermalStatsHelper.this.clearThermalStatsBuffer();
                if (!(OppoThermalStatsHelper.this.mSystemDir == null || OppoThermalStatsHelper.this.mThermalRecFile == null || !OppoThermalStatsHelper.this.mThermalRecFile.exists())) {
                    SimpleDateFormat mDateFormat = new SimpleDateFormat("yy-MM-dd-HH-mm", Locale.US);
                    File thermalDir = new File(OppoThermalStatsHelper.this.mSystemDir.toString() + "/thermal/dcs");
                    if (!thermalDir.isDirectory() || !thermalDir.exists()) {
                        thermalDir.mkdirs();
                    }
                    File backupFile = new File(OppoThermalStatsHelper.this.mSystemDir.toString() + "/thermal/dcs/thermalstats.bin");
                    if (backupFile.exists()) {
                        backupFile.delete();
                    }
                    OppoThermalStatsHelper.this.mThermalRecFile.renameTo(new File(thermalDir, "thermalstats_" + mDateFormat.format(Long.valueOf(System.currentTimeMillis())) + ".bin"));
                    File[] files = thermalDir.listFiles();
                    if (files.length > 14) {
                        File rmfile = files[0];
                        for (File file : files) {
                            if (file.lastModified() < rmfile.lastModified()) {
                                rmfile = file;
                            }
                        }
                        rmfile.delete();
                    }
                }
                OppoThermalStatsHelper.this.mThermalHistoryCur.cmd = OppoBaseBatteryStats.ThermalItem.CMD_RESET;
                OppoThermalStatsHelper oppoThermalStatsHelper = OppoThermalStatsHelper.this;
                oppoThermalStatsHelper.addThermalHistoryBufferLocked(OppoBaseBatteryStats.ThermalItem.CMD_RESET, oppoThermalStatsHelper.mThermalHistoryCur, true);
                OppoThermalStatsHelper.this.finishIteratingThermalHistoryLocked();
                if (OppoThermalStatsHelper.this.mHandler.hasMessages(60)) {
                    OppoThermalStatsHelper.this.mHandler.removeMessages(60);
                }
                OppoThermalStatsHelper.this.mHandler.sendEmptyMessageDelayed(60, 4000);
                Intent fccIntent = new Intent("oppo.intent.action.ACTION_OPPO_UPLOADFCC");
                fccIntent.putExtra("fcc", OppoThermalStatsHelper.this.mBatteryFcc);
                if (OppoThermalStatsHelper.this.mContext != null) {
                    OppoThermalStatsHelper.this.mContext.sendBroadcastAsUser(fccIntent, UserHandle.CURRENT);
                }
            }
        });
    }

    public void startUploadMonitorApp() {
        BackgroundThread.getHandler().post(new Runnable() {
            /* class com.android.internal.os.OppoThermalStatsHelper.AnonymousClass2 */

            public void run() {
                OppoThermalStatsHelper.this.uploadMonitorApp();
            }
        });
    }

    public void startUploadChargeMap() {
        BackgroundThread.getHandler().post(new Runnable() {
            /* class com.android.internal.os.OppoThermalStatsHelper.AnonymousClass3 */

            public void run() {
                OppoThermalStatsHelper.this.uploadChargeMap();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean copyFile(File srcFile, File destFile) {
        try {
            FileInputStream fis = new FileInputStream(srcFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(destFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            byte[] readByte = new byte[4096];
            while (bis.read(readByte) != -1) {
                bos.write(readByte);
            }
            bos.flush();
            fis.close();
            bis.close();
            fos.close();
            bos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void writeThermalRecFile() {
        synchronized (this.mLock) {
            if (this.mThermalFeatureOn && !this.mIteratingThermalHistory) {
                try {
                    FileOutputStream stream = new FileOutputStream(this.mThermalRecFile, true);
                    BufferedOutputStream bos = new BufferedOutputStream(stream);
                    bos.write(this.mThermalBuilder.toString().getBytes());
                    bos.flush();
                    stream.close();
                    bos.close();
                    this.mThermalBuilder.delete(0, this.mThermalBuilder.length());
                } catch (IOException e) {
                    Slog.w("BatteryStats", "Error writing thermal record file battery statistics", e);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initUploadAlarm() {
        BackgroundThread.getHandler().post(new Runnable() {
            /* class com.android.internal.os.OppoThermalStatsHelper.AnonymousClass4 */

            public void run() {
                if (OppoThermalStatsHelper.this.mContext != null && OppoThermalStatsHelper.this.mThermalFeatureOn) {
                    if (OppoThermalStatsHelper.this.mReceiver == null) {
                        OppoThermalStatsHelper oppoThermalStatsHelper = OppoThermalStatsHelper.this;
                        oppoThermalStatsHelper.mReceiver = new ThermalReceiver();
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(OppoThermalStatsHelper.ALARM_WAKEUP);
                        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
                        OppoThermalStatsHelper.this.mContext.registerReceiverAsUser(OppoThermalStatsHelper.this.mReceiver, UserHandle.CURRENT, intentFilter, null, null);
                    }
                    if (OppoThermalStatsHelper.this.mAlarmManager == null || OppoThermalStatsHelper.this.mWakeupIntent == null) {
                        OppoThermalStatsHelper oppoThermalStatsHelper2 = OppoThermalStatsHelper.this;
                        oppoThermalStatsHelper2.mAlarmManager = (AlarmManager) oppoThermalStatsHelper2.mContext.getSystemService("alarm");
                        OppoThermalStatsHelper oppoThermalStatsHelper3 = OppoThermalStatsHelper.this;
                        oppoThermalStatsHelper3.mWakeupIntent = PendingIntent.getBroadcast(oppoThermalStatsHelper3.mContext, 0, new Intent(OppoThermalStatsHelper.ALARM_WAKEUP), 0);
                    } else {
                        OppoThermalStatsHelper.this.mAlarmManager.cancel(OppoThermalStatsHelper.this.mWakeupIntent);
                    }
                    if (OppoThermalStatsHelper.this.mPowerManager == null || OppoThermalStatsHelper.this.mWakeLock == null) {
                        OppoThermalStatsHelper oppoThermalStatsHelper4 = OppoThermalStatsHelper.this;
                        oppoThermalStatsHelper4.mPowerManager = (PowerManager) oppoThermalStatsHelper4.mContext.getSystemService(Context.POWER_SERVICE);
                        OppoThermalStatsHelper oppoThermalStatsHelper5 = OppoThermalStatsHelper.this;
                        oppoThermalStatsHelper5.mWakeLock = oppoThermalStatsHelper5.mPowerManager.newWakeLock(1, OppoThermalStatsHelper.WAKELOCK_KEY);
                    } else if (OppoThermalStatsHelper.this.mWakeLock.isHeld()) {
                        OppoThermalStatsHelper.this.mWakeLock.release();
                    }
                    if (OppoThermalStatsHelper.this.mPackageManger == null) {
                        OppoThermalStatsHelper oppoThermalStatsHelper6 = OppoThermalStatsHelper.this;
                        oppoThermalStatsHelper6.mPackageManger = oppoThermalStatsHelper6.mContext.getPackageManager();
                    }
                    Slog.i(OppoThermalStatsHelper.TAG, " initUploadAlarm ");
                    Calendar instance = Calendar.getInstance();
                    int day = instance.get(6);
                    int year = instance.get(1);
                    if (day >= 365) {
                        instance.set(1, year + 1);
                        instance.set(6, 1);
                    } else {
                        instance.set(6, day + 1);
                    }
                    instance.set(11, 0);
                    new Random();
                    instance.set(12, 10);
                    instance.set(13, 0);
                    OppoThermalStatsHelper.this.mAlarmManager.setExact(0, instance.getTimeInMillis(), OppoThermalStatsHelper.this.mWakeupIntent);
                }
            }
        });
    }

    private void cancleUploadAlarm() {
        BackgroundThread.getHandler().post(new Runnable() {
            /* class com.android.internal.os.OppoThermalStatsHelper.AnonymousClass5 */

            public void run() {
                if (!(OppoThermalStatsHelper.this.mAlarmManager == null || OppoThermalStatsHelper.this.mWakeupIntent == null)) {
                    OppoThermalStatsHelper.this.mAlarmManager.cancel(OppoThermalStatsHelper.this.mWakeupIntent);
                }
                if (!(OppoThermalStatsHelper.this.mPowerManager == null || OppoThermalStatsHelper.this.mWakeLock == null || !OppoThermalStatsHelper.this.mWakeLock.isHeld())) {
                    OppoThermalStatsHelper.this.mWakeLock.release();
                }
                if (OppoThermalStatsHelper.this.mReceiver != null && OppoThermalStatsHelper.this.mContext != null) {
                    try {
                        OppoThermalStatsHelper.this.mContext.unregisterReceiver(OppoThermalStatsHelper.this.mReceiver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void clearThermalAllHistory() {
        BackgroundThread.getHandler().post(new Runnable() {
            /* class com.android.internal.os.OppoThermalStatsHelper.AnonymousClass6 */

            public void run() {
                OppoThermalStatsHelper.this.writeThermalRecFile();
                OppoThermalStatsHelper.this.startIteratingThermalHistoryLocked();
                OppoThermalStatsHelper.this.mThermalRecFile.delete();
                OppoThermalStatsHelper.this.clearThermalStatsBuffer();
                File thermalDir = new File(OppoThermalStatsHelper.this.mSystemDir.toString() + "/thermal/dcs");
                if (thermalDir.isDirectory() && thermalDir.exists()) {
                    for (File file : thermalDir.listFiles()) {
                        file.delete();
                    }
                }
                OppoThermalStatsHelper.this.finishIteratingThermalHistoryLocked();
                OppoThermalStatsHelper.this.resetThermalHistory();
            }
        });
    }

    public void backupThermalStatsFile() {
        BackgroundThread.getHandler().post(new Runnable() {
            /* class com.android.internal.os.OppoThermalStatsHelper.AnonymousClass7 */

            public void run() {
                OppoThermalStatsHelper.this.writeThermalRecFile();
                File thermalDir = new File(OppoThermalStatsHelper.this.mSystemDir.toString() + "/thermal/dcs");
                if (!thermalDir.isDirectory() || !thermalDir.exists()) {
                    thermalDir.mkdirs();
                }
                OppoThermalStatsHelper oppoThermalStatsHelper = OppoThermalStatsHelper.this;
                File file = oppoThermalStatsHelper.mThermalRecFile;
                oppoThermalStatsHelper.copyFile(file, new File(OppoThermalStatsHelper.this.mSystemDir + "/thermal/dcs/" + OppoThermalStatsHelper.this.mThermalRecFile.getName()));
                Intent saveFileIntent = new Intent("oppo.intent.action.ACTION_OPPO_SAVE_THERMAL_HISTORY");
                saveFileIntent.putExtra("save_path", OppoThermalStatsHelper.this.mSystemDir + "/thermal/dcs/");
                saveFileIntent.putExtra("save_to_path", "thermalrec");
                saveFileIntent.setPackage("com.oppo.oppopowermonitor");
                if (OppoThermalStatsHelper.this.mContext != null) {
                    OppoThermalStatsHelper.this.mContext.sendBroadcastAsUser(saveFileIntent, UserHandle.CURRENT);
                }
            }
        });
    }

    public void backupThermalLogFile() {
        BackgroundThread.getHandler().post(new Runnable() {
            /* class com.android.internal.os.OppoThermalStatsHelper.AnonymousClass8 */

            public void run() {
                Intent saveFileIntent = new Intent("oppo.intent.action.ACTION_OPPO_SAVE_THERMAL_HISTORY");
                saveFileIntent.putExtra("save_path", "data/oppo/psw/thermal_backup/");
                saveFileIntent.putExtra("save_to_path", "thermallog");
                saveFileIntent.setPackage("com.oppo.oppopowermonitor");
                if (OppoThermalStatsHelper.this.mContext != null) {
                    OppoThermalStatsHelper.this.mContext.sendBroadcastAsUser(saveFileIntent, UserHandle.CURRENT);
                }
            }
        });
    }

    public void dumpThemalHeatDetailLocked(PrintWriter pw) {
        this.mHeatReasonDetails.dumpThemalHeatDetailLocked(pw);
    }

    public void uploadThermalTemp() {
        Context context;
        if (this.mThermalUploadDcs) {
            Map<String, String> upLoadMap = getUploadThermalTemp();
            try {
                for (Map.Entry<String, String> entry : upLoadMap.entrySet()) {
                    Slog.i(TAG, "uploadThermalTemp " + entry.getKey() + SettingsStringUtil.DELIMITER + entry.getValue());
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            if (upLoadMap.size() > 0 && (context = this.mContext) != null) {
                ThermalStatistics.onCommon(context, THERMAL_TAG, THERMAL_TEMP_EVENT, upLoadMap, false);
            }
            Map<String, String> stampMap = getStampThermalTemp();
            try {
                for (Map.Entry<String, String> entry2 : stampMap.entrySet()) {
                    Slog.i(TAG, "uploadStampTemp " + entry2.getKey() + SettingsStringUtil.DELIMITER + entry2.getValue());
                }
            } catch (Exception exception2) {
                exception2.printStackTrace();
            }
            if (stampMap.size() > 0) {
                OppoManager.onStamp(HEAT_LOG_ID, stampMap);
            }
        }
    }

    public void uploadMonitorApp() {
        synchronized (this.mLock) {
            if (this.mMonitorAppMap != null) {
                try {
                    for (Map.Entry<String, String> entry : this.mMonitorAppMap.entrySet()) {
                        Slog.i(TAG, "uploadMonitorApp " + entry.getKey() + SettingsStringUtil.DELIMITER + entry.getValue());
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (this.mMonitorAppMap.size() > 0 && this.mContext != null) {
                    ThermalStatistics.onCommon(this.mContext, THERMAL_TAG, THERMAL_MONITOR_APP, this.mMonitorAppMap, false);
                    ThermalStatistics.onCommon(this.mContext, UPLOAD_LOGTAG, UPLOAD_ACTIVITY_BATTERY_RECORD, this.mMonitorAppMap, false);
                }
            }
        }
    }

    public void uploadChargeMap() {
        synchronized (this.mLock) {
            if (this.mChargeUploadMap != null) {
                try {
                    for (Map.Entry<String, String> entry : this.mChargeUploadMap.entrySet()) {
                        Slog.i(TAG, "mChargeUploadMap " + entry.getKey() + SettingsStringUtil.DELIMITER + entry.getValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (this.mChargeUploadMap.size() > 0 && this.mContext != null) {
                    ThermalStatistics.onCommon(this.mContext, THERMAL_TAG, CHARGE_MAP, this.mChargeUploadMap, false);
                }
            }
        }
    }

    public void getPhoneTemp(PrintWriter pw) {
        pw.println(this.mThermalHistoryCur.phoneTemp);
    }

    private Map<String, String> getUploadThermalTemp() {
        synchronized (this.mLock) {
            Map<String, String> upLoadMap = new HashMap<>();
            StringBuilder uploadHourtempStr = new StringBuilder();
            int allTime = 0;
            int maxPhoneTemp = 36;
            int maxBatTemp = 36;
            try {
                for (Map.Entry<String, Long> entry : this.mThermalTempMap.entrySet()) {
                    int tmpValue = (int) (entry.getValue().longValue() / 60000);
                    int tmpTemp = 0;
                    allTime += tmpValue;
                    if (tmpValue > 0 && tmpValue < 2000) {
                        upLoadMap.put("phonetemp" + entry.getKey(), Integer.toString(tmpValue));
                        try {
                            tmpTemp = Integer.parseInt(entry.getKey());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (maxPhoneTemp < tmpTemp) {
                            maxPhoneTemp = tmpTemp;
                        }
                    }
                }
                upLoadMap.put("maxPhoneTemp", Integer.toString(maxPhoneTemp));
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                for (Map.Entry<String, Long> entry2 : this.mBatTempMap.entrySet()) {
                    int tmpValue2 = (int) (entry2.getValue().longValue() / 60000);
                    int tmpTemp2 = 0;
                    allTime += tmpValue2;
                    if (tmpValue2 > 0 && tmpValue2 < 2000) {
                        upLoadMap.put("battemp" + entry2.getKey(), Integer.toString(tmpValue2));
                        try {
                            tmpTemp2 = Integer.parseInt(entry2.getKey());
                        } catch (Exception e3) {
                            e3.printStackTrace();
                        }
                        if (maxBatTemp < tmpTemp2) {
                            maxBatTemp = tmpTemp2;
                        }
                    }
                }
                upLoadMap.put("maxBatTemp", Integer.toString(maxBatTemp));
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            try {
                for (Map.Entry<String, Integer> entry3 : this.mThermalHourMap.entrySet()) {
                    uploadHourtempStr.append(entry3.getKey() + SettingsStringUtil.DELIMITER + Integer.toString(entry3.getValue().intValue()) + ";");
                }
                upLoadMap.put("hourtempMap", uploadHourtempStr.toString());
                Slog.d("Upload hourtemp", "put hourtemp in upLoadMap");
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            if (allTime > 4000) {
                upLoadMap.clear();
                return upLoadMap;
            }
            upLoadMap.put("holdtimeThreshold", Integer.toString(this.mHeatHoldTimeThreshold));
            upLoadMap.put("moreHeatThreshold", Integer.toString(this.mMoreHeatThreshold / 10));
            upLoadMap.put("heatThreshold", Integer.toString(this.mHeatThreshold / 10));
            upLoadMap.put("lessHeatThreshold", Integer.toString(this.mLessHeatThreshold / 10));
            return upLoadMap;
        }
    }

    private Map<String, String> getUploadChargeMap() {
        Map<String, String> chargeMap;
        synchronized (this.mLock) {
            chargeMap = new HashMap<>();
            try {
                for (Map.Entry<String, Long> entry : this.mTempChargeUploadMap.entrySet()) {
                    if (entry.getKey().equals("chargeTime")) {
                        chargeMap.put("chargeTime", Long.toString(entry.getValue().longValue() / 60000));
                    } else if (entry.getKey().equals("startTime")) {
                        chargeMap.put("startTime", DateFormat.format("yyyy-MM-dd-HH-mm-ss", entry.getValue().longValue()).toString());
                    } else if (entry.getKey().equals(CalendarContract.EXTRA_EVENT_END_TIME)) {
                        chargeMap.put(CalendarContract.EXTRA_EVENT_END_TIME, DateFormat.format("yyyy-MM-dd-HH-mm-ss", entry.getValue().longValue()).toString());
                    } else if (entry.getKey().equals("chargePlug")) {
                        int plug = (int) (entry.getValue().longValue() & 65535);
                        if (plug != 1) {
                            if (plug == 2) {
                                chargeMap.put("chargePlug", Context.USB_SERVICE);
                            } else if (plug != 4) {
                                chargeMap.put("chargePlug", "none");
                            } else {
                                chargeMap.put("chargePlug", "wireless");
                            }
                        } else if (this.mGlobalFastCharger) {
                            chargeMap.put("chargePlug", "ac_fast");
                        } else {
                            chargeMap.put("chargePlug", "ac_normal");
                        }
                    } else if (entry.getKey().equals("screenOnTime")) {
                        chargeMap.put("screenOnTime", Long.toString(entry.getValue().longValue() / 60000));
                    } else if (entry.getKey().equals("f2nTime")) {
                        chargeMap.put("f2nTime", Long.toString(entry.getValue().longValue() / 60000));
                    } else {
                        chargeMap.put(entry.getKey(), Long.toString(entry.getValue().longValue()));
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            chargeMap.put("chargeId", Integer.toString(this.mGlobalChargeId));
        }
        return chargeMap;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00d4 A[Catch:{ Exception -> 0x011d }] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0133 A[Catch:{ Exception -> 0x019a }] */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x01cc  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x01d1 A[DONT_GENERATE] */
    private Map<String, String> getStampThermalTemp() {
        StringBuilder stampHourtempStr;
        StringBuilder battempStr;
        StringBuilder battempStr2;
        Exception exception;
        Exception e;
        Exception e2;
        synchronized (this.mLock) {
            Map<String, String> stampMap = new HashMap<>();
            int allTime = 0;
            int maxPhoneTemp = 36;
            int maxBatTemp = 36;
            StringBuilder phonetempStr = new StringBuilder();
            StringBuilder battempStr3 = new StringBuilder();
            StringBuilder stampHourtempStr2 = new StringBuilder();
            stampMap.put("key", "TempMap");
            int i = 2000;
            try {
                for (Map.Entry<String, Long> entry : this.mThermalTempMap.entrySet()) {
                    battempStr = battempStr3;
                    stampHourtempStr = stampHourtempStr2;
                    try {
                        int tmpValue = (int) (entry.getValue().longValue() / 60000);
                        int tmpTemp = 0;
                        allTime += tmpValue;
                        if (tmpValue > 0 && tmpValue < 2000) {
                            phonetempStr.append("phonetemp" + entry.getKey() + SettingsStringUtil.DELIMITER + Integer.toString(tmpValue) + ";");
                            try {
                                tmpTemp = Integer.parseInt(entry.getKey());
                            } catch (Exception e3) {
                                e3.printStackTrace();
                            }
                            if (maxPhoneTemp < tmpTemp) {
                                maxPhoneTemp = tmpTemp;
                            }
                        }
                        battempStr3 = battempStr;
                        stampHourtempStr2 = stampHourtempStr;
                    } catch (Exception e4) {
                        e2 = e4;
                        e2.printStackTrace();
                        stampMap.put("phoneTempMap", phonetempStr.toString());
                        stampMap.put("maxPhoneTemp", Integer.toString(maxPhoneTemp));
                        while (r0.hasNext()) {
                        }
                        stampMap.put("hourtempMap", stampHourtempStr.toString());
                        while (r8.hasNext()) {
                        }
                        battempStr2 = battempStr;
                        stampMap.put("batTempMap", battempStr2.toString());
                        stampMap.put("maxBatTemp", Integer.toString(maxBatTemp));
                        stampMap.put("tempTime", DateFormat.format("yyyy-MM-dd-HH-mm-ss", System.currentTimeMillis()).toString());
                        if (allTime <= 4000) {
                        }
                    }
                }
                battempStr = battempStr3;
                stampHourtempStr = stampHourtempStr2;
            } catch (Exception e5) {
                e2 = e5;
                battempStr = battempStr3;
                stampHourtempStr = stampHourtempStr2;
                e2.printStackTrace();
                stampMap.put("phoneTempMap", phonetempStr.toString());
                stampMap.put("maxPhoneTemp", Integer.toString(maxPhoneTemp));
                while (r0.hasNext()) {
                }
                stampMap.put("hourtempMap", stampHourtempStr.toString());
                while (r8.hasNext()) {
                }
                battempStr2 = battempStr;
                stampMap.put("batTempMap", battempStr2.toString());
                stampMap.put("maxBatTemp", Integer.toString(maxBatTemp));
                stampMap.put("tempTime", DateFormat.format("yyyy-MM-dd-HH-mm-ss", System.currentTimeMillis()).toString());
                if (allTime <= 4000) {
                }
            }
            stampMap.put("phoneTempMap", phonetempStr.toString());
            stampMap.put("maxPhoneTemp", Integer.toString(maxPhoneTemp));
            try {
                for (Map.Entry<String, Integer> entry2 : this.mThermalHourMap.entrySet()) {
                    try {
                        stampHourtempStr.append(entry2.getKey() + SettingsStringUtil.DELIMITER + Integer.toString(entry2.getValue().intValue()) + ";");
                        stampHourtempStr = stampHourtempStr;
                    } catch (Exception e6) {
                        e = e6;
                        e.printStackTrace();
                        while (r8.hasNext()) {
                        }
                        battempStr2 = battempStr;
                        stampMap.put("batTempMap", battempStr2.toString());
                        stampMap.put("maxBatTemp", Integer.toString(maxBatTemp));
                        stampMap.put("tempTime", DateFormat.format("yyyy-MM-dd-HH-mm-ss", System.currentTimeMillis()).toString());
                        if (allTime <= 4000) {
                        }
                    }
                }
                stampMap.put("hourtempMap", stampHourtempStr.toString());
            } catch (Exception e7) {
                e = e7;
                e.printStackTrace();
                while (r8.hasNext()) {
                }
                battempStr2 = battempStr;
                stampMap.put("batTempMap", battempStr2.toString());
                stampMap.put("maxBatTemp", Integer.toString(maxBatTemp));
                stampMap.put("tempTime", DateFormat.format("yyyy-MM-dd-HH-mm-ss", System.currentTimeMillis()).toString());
                if (allTime <= 4000) {
                }
            }
            try {
                for (Map.Entry<String, Long> entry3 : this.mBatTempMap.entrySet()) {
                    int tmpValue2 = (int) (entry3.getValue().longValue() / 60000);
                    int tmpTemp2 = 0;
                    allTime += tmpValue2;
                    if (tmpValue2 <= 0 || tmpValue2 >= i) {
                        battempStr2 = battempStr;
                    } else {
                        battempStr2 = battempStr;
                        try {
                            battempStr2.append("battemp" + entry3.getKey() + SettingsStringUtil.DELIMITER + Integer.toString(tmpValue2) + ";");
                            try {
                                tmpTemp2 = Integer.parseInt(entry3.getKey());
                            } catch (Exception e8) {
                                e8.printStackTrace();
                            }
                            if (maxBatTemp < tmpTemp2) {
                                maxBatTemp = tmpTemp2;
                            }
                        } catch (Exception e9) {
                            exception = e9;
                            exception.printStackTrace();
                            stampMap.put("batTempMap", battempStr2.toString());
                            stampMap.put("maxBatTemp", Integer.toString(maxBatTemp));
                            stampMap.put("tempTime", DateFormat.format("yyyy-MM-dd-HH-mm-ss", System.currentTimeMillis()).toString());
                            if (allTime <= 4000) {
                            }
                        }
                    }
                    battempStr = battempStr2;
                    i = 2000;
                }
                battempStr2 = battempStr;
            } catch (Exception e10) {
                exception = e10;
                battempStr2 = battempStr;
                exception.printStackTrace();
                stampMap.put("batTempMap", battempStr2.toString());
                stampMap.put("maxBatTemp", Integer.toString(maxBatTemp));
                stampMap.put("tempTime", DateFormat.format("yyyy-MM-dd-HH-mm-ss", System.currentTimeMillis()).toString());
                if (allTime <= 4000) {
                }
            }
            stampMap.put("batTempMap", battempStr2.toString());
            stampMap.put("maxBatTemp", Integer.toString(maxBatTemp));
            stampMap.put("tempTime", DateFormat.format("yyyy-MM-dd-HH-mm-ss", System.currentTimeMillis()).toString());
            if (allTime <= 4000) {
                return stampMap;
            }
            stampMap.clear();
            return stampMap;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Map<String, String> getStampThermalHeat(Map<String, String> mHeatMap) {
        Map<String, String> stampMap = new HashMap<>();
        StringBuilder heatStr = new StringBuilder();
        stampMap.put("key", "HeatMap");
        try {
            for (Map.Entry<String, String> entry : mHeatMap.entrySet()) {
                heatStr.append(entry.getKey() + SettingsStringUtil.DELIMITER + entry.getValue() + ";");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        stampMap.put("phoneHeatMap", heatStr.toString());
        stampMap.put("heatTime", DateFormat.format("yyyy-MM-dd-HH-mm-ss", System.currentTimeMillis()).toString());
        return stampMap;
    }

    private Map<String, String> getMonitorAppMap(String proc, String versionName) {
        Map<String, String> monitorAppMap = new HashMap<>();
        long chargeTime = 0;
        long backlight = 0;
        long startBatRm = 0;
        long phoneTemp = 0;
        long batTemp = 0;
        long netMobileTime = 0;
        long netWifiTime = 0;
        long autoBrightTime = 0;
        long time = 0;
        Map<String, Long> map = this.mTempMonitorAppMap;
        StringBuilder sb = new StringBuilder();
        sb.append(proc);
        long volume = 0;
        sb.append("--Time");
        if (map.containsKey(sb.toString())) {
            time = this.mTempMonitorAppMap.get(proc + "--Time").longValue();
        }
        if (this.mTempMonitorAppMap.containsKey(proc + "--Charge")) {
            chargeTime = this.mTempMonitorAppMap.get(proc + "--Charge").longValue();
        }
        Map<String, Long> map2 = this.mTempMonitorAppMap;
        StringBuilder sb2 = new StringBuilder();
        sb2.append(proc);
        long endBatRm = 0;
        sb2.append("--Volume");
        if (map2.containsKey(sb2.toString())) {
            volume = this.mTempMonitorAppMap.get(proc + "--Volume").longValue();
        }
        if (this.mTempMonitorAppMap.containsKey(proc + "--Backlight")) {
            backlight = this.mTempMonitorAppMap.get(proc + "--Backlight").longValue();
        }
        if (this.mTempMonitorAppMap.containsKey(proc + "--StartBatRm")) {
            startBatRm = this.mTempMonitorAppMap.get(proc + "--StartBatRm").longValue();
        }
        if (this.mTempMonitorAppMap.containsKey(proc + "--EndBatRm")) {
            endBatRm = this.mTempMonitorAppMap.get(proc + "--EndBatRm").longValue();
        }
        if (this.mTempMonitorAppMap.containsKey(proc + "--MaxPhoneTemp")) {
            phoneTemp = this.mTempMonitorAppMap.get(proc + "--MaxPhoneTemp").longValue();
        }
        if (this.mTempMonitorAppMap.containsKey(proc + "--MaxBatTemp")) {
            batTemp = this.mTempMonitorAppMap.get(proc + "--MaxBatTemp").longValue();
        }
        if (this.mTempMonitorAppMap.containsKey(proc + "--NetWifi")) {
            netWifiTime = this.mTempMonitorAppMap.get(proc + "--NetWifi").longValue();
        }
        if (this.mTempMonitorAppMap.containsKey(proc + "--NetMobile")) {
            netMobileTime = this.mTempMonitorAppMap.get(proc + "--NetMobile").longValue();
        }
        if (this.mTempMonitorAppMap.containsKey(proc + "--AutoBrightness")) {
            autoBrightTime = this.mTempMonitorAppMap.get(proc + "--AutoBrightness").longValue();
        }
        if (time <= 0) {
            return null;
        }
        if ((chargeTime * 100) / time > 80) {
            monitorAppMap.put(proc + "--Current", "9999");
            monitorAppMap.put(proc + "--Charge", "True");
        } else if ((chargeTime * 100) / time < 20) {
            monitorAppMap.put(proc + "--Current", Long.toString(((startBatRm - endBatRm) * 3600000) / time));
            monitorAppMap.put(proc + "--Charge", "False");
        } else {
            monitorAppMap.put(proc + "--Current", "9999");
            monitorAppMap.put(proc + "--Charge", "Unknown");
        }
        monitorAppMap.put(proc + "--Time", Long.toString(time / 60000));
        if ((netMobileTime * 100) / time > 80) {
            monitorAppMap.put(proc + "--NetType", "Mobile");
        } else if ((netWifiTime * 100) / time > 80) {
            monitorAppMap.put(proc + "--NetType", "Wifi");
        } else if ((0 * 100) / time > 80) {
            monitorAppMap.put(proc + "--NetType", "None");
        } else {
            monitorAppMap.put(proc + "--NetType", "Unkown");
        }
        if ((autoBrightTime * 100) / time > 80) {
            monitorAppMap.put(proc + "--AutoBright", "True");
        } else if ((100 * autoBrightTime) / time < 20) {
            monitorAppMap.put(proc + "--AutoBright", "False");
        } else {
            monitorAppMap.put(proc + "--AutoBright", "Unkown");
        }
        monitorAppMap.put(proc + "--Volume", Long.toString(volume / time));
        monitorAppMap.put(proc + "--BackLight", Long.toString(backlight / time));
        monitorAppMap.put(proc + "--MaxPhoneTemp", Long.toString(phoneTemp));
        monitorAppMap.put(proc + "--MaxBatTemp", Long.toString(batTemp));
        monitorAppMap.put(proc + "--VerisonName", versionName);
        monitorAppMap.put("appname", proc);
        monitorAppMap.put("duration", Long.toString(time / 60000));
        monitorAppMap.put("startlevel", String.valueOf(this.mStartBatteryLevel));
        monitorAppMap.put("endlevel", String.valueOf(this.mEndBatteryLevel));
        monitorAppMap.put("deltaBC", String.valueOf(startBatRm - endBatRm));
        return monitorAppMap;
    }

    /* access modifiers changed from: private */
    public class ThermalReceiver extends BroadcastReceiver {
        private ThermalReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(OppoThermalStatsHelper.ALARM_WAKEUP)) {
                if (!OppoThermalStatsHelper.this.mRecordThermalHistory || OppoThermalStatsHelper.this.getThermalHistoryUsedSize() >= 2048) {
                    Message msg = new Message();
                    msg.what = 56;
                    OppoThermalStatsHelper.this.mHandler.sendMessageDelayed(msg, 1000);
                    return;
                }
                if (OppoThermalStatsHelper.this.mHandler.hasMessages(60)) {
                    OppoThermalStatsHelper.this.mHandler.removeMessages(60);
                }
                OppoThermalStatsHelper.this.mHandler.sendEmptyMessageDelayed(60, 4000);
            } else if ("android.media.VOLUME_CHANGED_ACTION".equals(action)) {
                int type = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                int volLevel = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0);
                if (type == 3) {
                    if (OppoThermalStatsHelper.this.mHandler.hasMessages(61)) {
                        OppoThermalStatsHelper.this.mHandler.removeMessages(61);
                    }
                    Message msg2 = new Message();
                    msg2.what = 61;
                    msg2.arg1 = volLevel;
                    OppoThermalStatsHelper oppoThermalStatsHelper = OppoThermalStatsHelper.this;
                    oppoThermalStatsHelper.mGlobalVolumeLevel = volLevel;
                    oppoThermalStatsHelper.mHandler.sendMessageDelayed(msg2, 2000);
                }
            }
        }
    }

    private void addhistorySizeValue(StringBuilder sb, long size) {
        float result = (float) size;
        String suffix = "";
        if (result >= 10240.0f) {
            suffix = "KB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "MB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "GB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "TB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "PB";
            result /= 1024.0f;
        }
        sb.append((int) result);
        sb.append(suffix);
    }

    private int symbolInt(int tempInt) {
        if ((tempInt & 32768) == 32768) {
            return (tempInt - 65535) - 1;
        }
        if ((tempInt & 2048) == 2048) {
            return (tempInt - 4095) - 1;
        }
        return tempInt;
    }

    public static byte getNetWorkClass(int mobieNetworkType) {
        switch (mobieNetworkType) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11:
                return 2;
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15:
                return 3;
            case 13:
                return 4;
            default:
                return 0;
        }
    }

    public void notePhoneDataConnectionStateLocked(long elapsedTime, long upTime, int dataType) {
        byte connectType;
        if (getConnectyType() == 0 && (connectType = getNetWorkClass(dataType)) != this.mThermalHistoryCur.connectNetType) {
            addThermalConnectType(elapsedTime, upTime, connectType);
        }
    }

    public void noteScreenBrightnessModeChangedLock(boolean isAuto) {
        this.mGlobalScreenBrightnessMode = isAuto;
        if (this.mThermalFeatureOn && isAuto != this.mThermalHistoryCur.isAutoBrightness) {
            this.mThermalHistoryCur.isAutoBrightness = isAuto;
            if (this.mHandler.hasMessages(63)) {
                this.mHandler.removeMessages(63);
            }
            this.mHandler.sendEmptyMessageDelayed(63, 1000);
        }
    }

    public boolean startIteratingThermalHistoryLocked() {
        if (this.mThermalHistoryBuffer.dataSize() <= 0) {
            return false;
        }
        this.mThermalHistoryBuffer.setDataPosition(0);
        this.mIteratingThermalHistory = true;
        return true;
    }

    public void finishIteratingThermalHistoryLocked() {
        Parcel parcel = this.mThermalHistoryBuffer;
        parcel.setDataPosition(parcel.dataSize());
        this.mIteratingThermalHistory = false;
    }

    public int getThermalHistoryUsedSize() {
        return this.mThermalHistoryBuffer.dataSize();
    }

    public boolean getNextThermalHistoryLocked(OppoBaseBatteryStats.ThermalItem out, long histStart) {
        int pos = this.mThermalHistoryBuffer.dataPosition();
        if (pos == 0) {
            out.clear();
        }
        if (pos >= this.mThermalHistoryBuffer.dataSize()) {
            return false;
        }
        readThermalHistoryDelta(this.mThermalHistoryBuffer, out, histStart);
        return true;
    }

    public static class ThermalStatistics {
        private static final String APP_ID = "appId";
        private static final String APP_NAME = "appName";
        private static final String APP_PACKAGE = "appPackage";
        private static final String APP_VERSION = "appVersion";
        private static final int COMMON = 1006;
        private static final int COMMON_LIST = 1010;
        private static final String DATA_TYPE = "dataType";
        private static final String EVENT_ID = "eventID";
        private static final String LOG_MAP = "logMap";
        private static final String LOG_TAG = "logTag";
        private static final String MAP_LIST = "mapList";
        private static final String SSOID = "ssoid";
        private static final String TAG = "ThermalStatistics--";
        private static final String UPLOAD_NOW = "uploadNow";
        private static int appId = 20139;
        private static boolean mIsOnCommon = false;
        private static ExecutorService sSingleThreadExecutor = Executors.newSingleThreadExecutor();

        public static void onCommon(final Context context, final String logTag, final String eventId, Map<String, String> logMap, final boolean uploadNow) {
            final Map<String, String> cloneMap;
            mIsOnCommon = true;
            if (context == null) {
                Slog.w("common_test", "context is null!");
                mIsOnCommon = false;
                return;
            }
            if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                Slog.d("common_test", "onCommon begin: logTag=" + logTag + ", eventId=" + eventId + ", logMap=" + logMap + ", uploadNow=" + uploadNow);
            }
            if (TextUtils.isEmpty(logTag)) {
                mIsOnCommon = false;
                return;
            }
            if (logMap != null) {
                cloneMap = new HashMap<>(logMap);
            } else {
                cloneMap = new HashMap<>();
            }
            Slog.i("common_test", "context is startservice");
            sSingleThreadExecutor.execute(new Runnable() {
                /* class com.android.internal.os.OppoThermalStatsHelper.ThermalStatistics.AnonymousClass1 */

                public void run() {
                    Intent intent = new Intent();
                    intent.setClassName("com.nearme.statistics.rom", "com.nearme.statistics.rom.service.ReceiverService");
                    intent.putExtra(ThermalStatistics.APP_PACKAGE, "system");
                    intent.putExtra(ThermalStatistics.APP_NAME, "system");
                    intent.putExtra(ThermalStatistics.APP_VERSION, "system");
                    intent.putExtra(ThermalStatistics.SSOID, "system");
                    intent.putExtra(ThermalStatistics.APP_ID, ThermalStatistics.appId);
                    intent.putExtra(ThermalStatistics.EVENT_ID, eventId);
                    intent.putExtra(ThermalStatistics.UPLOAD_NOW, uploadNow);
                    intent.putExtra(ThermalStatistics.LOG_TAG, logTag);
                    intent.putExtra(ThermalStatistics.LOG_MAP, ThermalStatistics.getCommonObject(cloneMap).toString());
                    intent.putExtra(ThermalStatistics.DATA_TYPE, 1006);
                    Context context = context;
                    if (context != null) {
                        context.startServiceAsUser(intent, UserHandle.CURRENT);
                    }
                    cloneMap.clear();
                    boolean unused = ThermalStatistics.mIsOnCommon = false;
                }
            });
        }

        public static boolean getOnCommon() {
            return mIsOnCommon;
        }

        /* access modifiers changed from: private */
        public static JSONObject getCommonObject(Map<String, String> logMap) {
            JSONObject jsonObject = new JSONObject();
            if (logMap != null && !logMap.isEmpty()) {
                try {
                    for (String key : logMap.keySet()) {
                        jsonObject.put(key, logMap.get(key));
                    }
                } catch (Exception e) {
                    Slog.w(TAG, "Exception: " + e);
                }
            }
            return jsonObject;
        }
    }

    public void startAnalyzeBatteryStats() {
        BackgroundThread.getHandler().post(new Runnable() {
            /* class com.android.internal.os.OppoThermalStatsHelper.AnonymousClass9 */

            public void run() {
                Intent intent = new Intent("oppo.intent.action.PARSE_BATTERYSTATS_START");
                intent.setPackage("com.oppo.oppopowermonitor");
                if (OppoThermalStatsHelper.this.mContext != null) {
                    OppoThermalStatsHelper.this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
                }
            }
        });
    }

    private class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean onOff = false;
            switch (msg.what) {
                case 51:
                    if (msg.arg1 == 1) {
                        onOff = true;
                    }
                    if (onOff != OppoThermalStatsHelper.this.mThermalHistoryCur.cameraOn) {
                        OppoThermalStatsHelper.this.mThermalHistoryCur.cameraOn = onOff;
                        if (OppoThermalStatsHelper.this.mHandler.hasMessages(63)) {
                            OppoThermalStatsHelper.this.mHandler.removeMessages(63);
                        }
                        OppoThermalStatsHelper.this.mHandler.sendEmptyMessageDelayed(63, 1000);
                        return;
                    }
                    return;
                case 52:
                    if (msg.arg1 == 1) {
                        onOff = true;
                    }
                    if (onOff != OppoThermalStatsHelper.this.mThermalHistoryCur.audioOn) {
                        OppoThermalStatsHelper.this.mThermalHistoryCur.audioOn = onOff;
                        if (OppoThermalStatsHelper.this.mHandler.hasMessages(63)) {
                            OppoThermalStatsHelper.this.mHandler.removeMessages(63);
                        }
                        OppoThermalStatsHelper.this.mHandler.sendEmptyMessageDelayed(63, 1000);
                        return;
                    }
                    return;
                case 53:
                    if (msg.arg1 == 1) {
                        onOff = true;
                    }
                    if (onOff != OppoThermalStatsHelper.this.mThermalHistoryCur.videoOn) {
                        OppoThermalStatsHelper.this.mThermalHistoryCur.videoOn = onOff;
                        if (OppoThermalStatsHelper.this.mHandler.hasMessages(63)) {
                            OppoThermalStatsHelper.this.mHandler.removeMessages(63);
                        }
                        OppoThermalStatsHelper.this.mHandler.sendEmptyMessageDelayed(63, 1000);
                        return;
                    }
                    return;
                case 54:
                    if (msg.arg1 == 1) {
                        onOff = true;
                    }
                    if (onOff != OppoThermalStatsHelper.this.mThermalHistoryCur.gpsOn) {
                        OppoThermalStatsHelper.this.mThermalHistoryCur.gpsOn = onOff;
                        if (OppoThermalStatsHelper.this.mHandler.hasMessages(63)) {
                            OppoThermalStatsHelper.this.mHandler.removeMessages(63);
                        }
                        OppoThermalStatsHelper.this.mHandler.sendEmptyMessageDelayed(63, 1000);
                        return;
                    }
                    return;
                case 55:
                    if (msg.arg1 == 1) {
                        onOff = true;
                    }
                    if (onOff != OppoThermalStatsHelper.this.mThermalHistoryCur.flashlightOn) {
                        OppoThermalStatsHelper.this.mThermalHistoryCur.flashlightOn = onOff;
                        if (OppoThermalStatsHelper.this.mHandler.hasMessages(63)) {
                            OppoThermalStatsHelper.this.mHandler.removeMessages(63);
                        }
                        OppoThermalStatsHelper.this.mHandler.sendEmptyMessageDelayed(63, 1000);
                        return;
                    }
                    return;
                case 56:
                    if (!OppoThermalStatsHelper.this.mWakeLock.isHeld()) {
                        OppoThermalStatsHelper.this.mWakeLock.acquire(25000);
                    }
                    OppoThermalStatsHelper.this.writeThermalRecFile();
                    OppoThermalStatsHelper.this.startUploadTemp();
                    OppoThermalStatsHelper.this.startAnalyzeBatteryStats();
                    return;
                case 57:
                    int backlight = msg.arg1;
                    if (backlight != OppoThermalStatsHelper.this.mThermalHistoryCur.backlight) {
                        OppoThermalStatsHelper oppoThermalStatsHelper = OppoThermalStatsHelper.this;
                        oppoThermalStatsHelper.addThermalHistoryBufferLocked((byte) 3, backlight, oppoThermalStatsHelper.mThermalHistoryCur, true);
                        return;
                    }
                    return;
                case 58:
                    int type = msg.arg1;
                    if (type != OppoThermalStatsHelper.this.mThermalHistoryCur.connectNetType) {
                        OppoThermalStatsHelper.this.mThermalHistoryCur.connectNetType = (byte) type;
                        OppoThermalStatsHelper oppoThermalStatsHelper2 = OppoThermalStatsHelper.this;
                        oppoThermalStatsHelper2.addThermalHistoryBufferLocked((byte) 9, oppoThermalStatsHelper2.mThermalHistoryCur, true);
                        return;
                    }
                    return;
                case 59:
                    try {
                        if (OppoThermalStatsHelper.this.mWakeLock != null && !OppoThermalStatsHelper.this.mWakeLock.isHeld()) {
                            OppoThermalStatsHelper.this.mWakeLock.acquire(60000);
                        }
                        long thermaldumpStart = ((Long) msg.obj).longValue();
                        int pos = msg.arg1;
                        if (OppoThermalStatsHelper.this.mHeatReasonDetails.analizyHeatRecItem(thermaldumpStart, pos)) {
                            Message myMsg = new Message();
                            myMsg.what = 59;
                            myMsg.obj = Long.valueOf(thermaldumpStart);
                            myMsg.arg1 = pos - 1;
                            OppoThermalStatsHelper.this.mHandler.sendMessageDelayed(myMsg, 1);
                            return;
                        }
                        OppoThermalStatsHelper.this.mHeatReasonDetails.getHeatReasonDetails();
                        OppoThermalStatsHelper.this.mHeatReasonDetails.mAnalizyPosition = 0;
                        OppoThermalStatsHelper.this.mStartAnalizyHeat = false;
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                case 60:
                    OppoThermalStatsHelper.this.initUploadAlarm();
                    return;
                case 61:
                    int volume = msg.arg1;
                    if (volume != OppoThermalStatsHelper.this.mThermalHistoryCur.volume) {
                        OppoThermalStatsHelper.this.mThermalHistoryCur.volume = volume;
                        if (OppoThermalStatsHelper.this.mHandler.hasMessages(63)) {
                            OppoThermalStatsHelper.this.mHandler.removeMessages(63);
                        }
                        OppoThermalStatsHelper.this.mHandler.sendEmptyMessageDelayed(63, 1000);
                        return;
                    }
                    return;
                case 62:
                    if (OppoThermalStatsHelper.this.mContext != null) {
                        AudioManager audioManager = (AudioManager) OppoThermalStatsHelper.this.mContext.getSystemService("audio");
                        if (audioManager != null) {
                            OppoThermalStatsHelper.this.mThermalHistoryCur.volume = audioManager.getStreamVolume(3);
                            OppoThermalStatsHelper.this.mThermalHistoryCur.isAutoBrightness = OppoThermalStatsHelper.this.mGlobalScreenBrightnessMode;
                            return;
                        }
                        Slog.w(OppoThermalStatsHelper.TAG, "INIT_THERMAL_PAR: failed to get audioManager!!");
                        return;
                    }
                    return;
                case 63:
                    OppoThermalStatsHelper oppoThermalStatsHelper3 = OppoThermalStatsHelper.this;
                    oppoThermalStatsHelper3.addThermalHistoryBufferLocked(OppoBaseBatteryStats.ThermalItem.CMD_COMMON_UPDATE, oppoThermalStatsHelper3.mThermalHistoryCur, true);
                    return;
                case 64:
                    Slog.d(OppoThermalStatsHelper.TAG, "SYNC_TO_THERMAL_FILE");
                    OppoThermalStatsHelper.this.writeThermalRecFile();
                    OppoThermalStatsHelper.this.clearHistoryBuffer();
                    return;
                default:
                    return;
            }
        }
    }
}

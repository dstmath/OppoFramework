package com.android.server.oppo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocationManagerService;
import com.android.server.am.EventLogTags;
import com.oppo.RomUpdateHelper;
import com.oppo.rutils.RUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public final class OppoLogService {
    private static final int CHECK_INTERVAL = 1800000;
    private static final int CHECK_LOG_SIZE = 1;
    private static final String DATA_FILE_DIR = "data/system/server_log_config.xml";
    private static final boolean DEBUG = true;
    public static final String FILTER_NAME = "oppo_log_kit_config";
    private static final int LOG_MAX_SIZE = 921600;
    private static int MAXUPLOADTIMES = 10;
    private static final int MB_BYTES = 1024;
    private static final String SYS_FILE_DIR = "system/etc/server_log_config.xml";
    private static final String TAG = "OppoLogService";
    private static boolean mIsPanic = false;
    public static final Map<Integer, String> mOppoInfoStringMap = new HashMap();
    final int ALSPS_APDS9922;
    final int ALSPS_CM36286;
    final int ALSPS_LOG_ALGO_GET = 5;
    final int ALSPS_LOG_ALGO_SET = 4;
    final int ALSPS_LOG_ALSPS_CONF_ITEM = 9;
    final int ALSPS_LOG_ALSPS_ESD_HANDLE = 8;
    final int ALSPS_LOG_ALSPS_RESET = 0;
    final int ALSPS_LOG_ALS_CALIBRATION = 7;
    final int ALSPS_LOG_ENABLE_SCHED_DATA = 1;
    final int ALSPS_LOG_MAX = 17;
    final int ALSPS_LOG_PROX_ATTITUDE_CHANGED = 11;
    final int ALSPS_LOG_PROX_AUTO_CALI = 17;
    final int ALSPS_LOG_PROX_CALIBRATION = 6;
    final int ALSPS_LOG_PROX_HIGH_LIGHT = 10;
    final int ALSPS_LOG_PROX_HIGH_LIGHT_ALGO_SET = 16;
    final int ALSPS_LOG_PROX_HIGH_LIGHT_DECADENT = 14;
    final int ALSPS_LOG_PROX_HIGH_LIGHT_DISAPPEAR = 15;
    final int ALSPS_LOG_PROX_PAD_PASTING = 12;
    final int ALSPS_LOG_PROX_UPRIGHT_THD = 13;
    final int ALSPS_LOG_REPORT_PS_STATE = 3;
    final int ALSPS_LOG_RPOX_ALGO_TRIGGER = 2;
    final int ALSPS_STK3210;
    final int ALSPS_TMD2725;
    final int ALSPS_TMD2745;
    long FILE_MAX_LEN;
    final int G_LOG_ENABLE_SCHED_DATA = 0;
    final int G_LOG_MAX = 9;
    final int G_LOG_STEP_COUNT = 1;
    final int G_LOG_STEP_COUNT_AFTER_SLEEP = 2;
    final int INTERVAL_TIME = 1800000;
    int MAX_FILE_NUM;
    private final int MSG_UPLOAD_DCS = EventLogTags.AM_FINISH_ACTIVITY;
    final int QC_SENSOR_TYPE_BASE = 33171000;
    final int SENSOR_TYPE_LOGGER = 33171024;
    final int SNS_DDF_SENSOR_ACCEL = 1;
    final int SNS_DDF_SENSOR_AMBIENT = 6;
    final int SNS_DDF_SENSOR_GYRO = 3;
    final int SNS_DDF_SENSOR_MAG = 2;
    final int SNS_DDF_SENSOR_PROXIMITY = 5;
    final int SNS_DDF_SENSOR_STEP_COUNT = 24;
    String[] alsps_type_string;
    String[][] flow_log_string;
    String kernelLogPath;
    private Context mContext;
    Handler mDcsHandler;
    HandlerThread mDcsHandlerThread;
    List<Map<String, String>> mDcsRecordHistory = new ArrayList();
    Looper mDcsgLooper;
    String mDirPath;
    private WorkerHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsLogCoreServiceRunning;
    private boolean mIsUpadteConfig;
    Sensor mLogSensor;
    Handler mMainHandler;
    File mOutPutingLog;
    Object mRecordMutex = new Object();
    Handler mSensorLogHandler;
    HandlerThread mSensorLogHandlerThread;
    SensorEventListener mSensorLogListener;
    Looper mSensorLogLooper;
    FileWriter mSensorLogWriter;
    SensorManager mSensorManager;
    private ServerLogConfigUpdateHelper mServerLogConfigHelper;
    private LocalServerSocket mServerSocket;
    private ServiceConnection mServiceConnection;
    Map<String, Integer> mUploadTimes = new HashMap();
    String[] ps_state_source;

    class CritiCalLogParserRunnable implements Runnable {
        static final String Prefix = "CriticalLog";
        StringBuffer logBuf = new StringBuffer();
        Context mContext;
        String mCriticalLog;
        String mIssue = "";
        String mLogType = "";
        String mModule = "";
        String mOtaVersion = "";
        String mTime = "";
        String mUploadLog = "";

        CritiCalLogParserRunnable(Context c, String log) {
            this.mCriticalLog = log;
            this.mContext = c;
        }

        public void run() {
            try {
                boolean isLogMsg = false;
                for (String line : this.mCriticalLog.split("\n")) {
                    String line2 = line2.trim();
                    if (line2.endsWith("-begin>")) {
                        parserHeadInfo(line2);
                    } else if (line2.endsWith("-end>")) {
                        this.mUploadLog = this.logBuf.toString();
                    } else if (line2.contains("LOGTYPE:")) {
                        parserLogType(line2);
                    } else if (line2.startsWith("log-time:")) {
                        parserLogTime(line2);
                    } else if (line2.startsWith("log-colorOS:")) {
                        isLogMsg = true;
                    } else if (line2.startsWith("log-buildTime:")) {
                        parserLogBuidTime(line2);
                    } else if (isLogMsg) {
                        this.logBuf.append(line2);
                    }
                }
                uploadToDCS();
            } catch (Exception e) {
                Slog.v(OppoLogService.TAG, "error in run e = " + e.toString());
            }
        }

        void parserHeadInfo(String info) {
            try {
                String[] msg = info.split("-");
                if (msg.length >= 3) {
                    this.mModule = Prefix + captureName(msg[0].substring(1));
                    for (int i = 1; i < msg.length - 1; i++) {
                        this.mIssue += msg[i];
                    }
                }
                Slog.v(OppoLogService.TAG, "mModule = " + this.mModule + " mIssue = " + this.mIssue);
            } catch (Exception e) {
                Slog.v(OppoLogService.TAG, "parserHeadInfo error: " + e.toString());
            }
        }

        void parserLogType(String info) {
            try {
                String[] msg = info.split(":");
                if (msg.length == 2) {
                    this.mLogType = msg[1].trim();
                } else {
                    this.mLogType = Shell.NIGHT_MODE_STR_UNKNOWN;
                }
                Slog.v(OppoLogService.TAG, "mLogType = " + this.mLogType);
            } catch (Exception e) {
                Slog.v(OppoLogService.TAG, "parserLogType error :" + e.toString());
            }
        }

        void parserLogTime(String info) {
            try {
                this.mTime = info.substring(new String("log-time:").length() + 1).trim();
                Slog.v(OppoLogService.TAG, "mTime = " + this.mTime);
            } catch (Exception e) {
                Slog.v(OppoLogService.TAG, "parserLogTime error: " + e.toString());
            }
        }

        void parserLogBuidTime(String info) {
            try {
                this.mOtaVersion = info.substring(new String("log-buildTime:").length() + 1).trim();
                Slog.v(OppoLogService.TAG, "mOtaVersion = " + this.mOtaVersion);
            } catch (Exception e) {
                Slog.v(OppoLogService.TAG, "parserLogBuidTime error: " + e.toString());
            }
        }

        void parserNetworkLog(String info, Map<String, String> logMap) {
            try {
                for (String msg : info.split(",")) {
                    String[] local = msg.trim().split(":");
                    if (local != null && local.length == 2) {
                        Slog.v(OppoLogService.TAG, "key = " + local[0].trim() + " value = " + local[1].trim());
                        logMap.put(local[0].trim(), local[1].trim());
                    }
                }
            } catch (Exception e) {
                Slog.v(OppoLogService.TAG, "parserNetworkLog e = " + e.getMessage());
            }
        }

        void uploadToDCS() {
            try {
                Map<String, String> logMap = new HashMap();
                logMap.put("logType", this.mLogType);
                logMap.put("logTime", this.mTime);
                logMap.put("module", this.mModule);
                logMap.put("log", this.mUploadLog);
                logMap.put("otaVersion", this.mOtaVersion);
                logMap.put("issue", this.mIssue);
                if (this.mModule.contains("Network")) {
                    parserNetworkLog(this.mUploadLog, logMap);
                }
                if (!isUploadImmediately(logMap)) {
                    OppoLogService.this.addDcsRecord(logMap);
                }
            } catch (Exception e) {
                Slog.v(OppoLogService.TAG, "uploadToDCS : " + e.getMessage());
            }
        }

        boolean isUploadImmediately(Map<String, String> logMap) {
            try {
                String type = (String) logMap.get("logType");
                String issue = (String) logMap.get("issue");
                Integer times = (Integer) OppoLogService.this.mUploadTimes.get(type);
                if (times == null) {
                    Slog.v(OppoLogService.TAG, "first upload type : " + type);
                    times = new Integer(0);
                }
                if (times.intValue() < OppoLogService.MAXUPLOADTIMES) {
                    times = Integer.valueOf(times.intValue() + 1);
                    Slog.v(OppoLogService.TAG, "upload type : " + type + " times " + times);
                    OppoLogService.this.mUploadTimes.remove(type);
                    OppoLogService.this.mUploadTimes.put(type, times);
                    logMap.put("count", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                    logMap.remove("issue");
                    OppoStatistics.onCommon(this.mContext, Prefix, issue, logMap, false);
                    return true;
                }
            } catch (Exception e) {
                Slog.e(OppoLogService.TAG, "check error : " + e.toString());
            }
            return false;
        }

        String captureName(String name) {
            char[] cs = name.toCharArray();
            cs[0] = (char) (cs[0] - 32);
            return String.valueOf(cs);
        }
    }

    class DcsHandler extends Handler {
        DcsHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EventLogTags.AM_FINISH_ACTIVITY /*30001*/:
                    synchronized (OppoLogService.this.mRecordMutex) {
                        for (Map<String, String> element : OppoLogService.this.mDcsRecordHistory) {
                            String issue = (String) element.get("issue");
                            element.remove("issue");
                            Slog.v(OppoLogService.TAG, "update criticallog  to DCS, type = " + ((String) element.get("logType")) + " count = " + ((String) element.get("count")));
                            OppoStatistics.onCommon(OppoLogService.this.mContext, "CriticalLog", issue, element, false);
                        }
                        OppoLogService.this.mDcsRecordHistory.clear();
                        OppoLogService.this.mUploadTimes.clear();
                    }
                    OppoLogService.this.mDcsHandler.sendEmptyMessageDelayed(EventLogTags.AM_FINISH_ACTIVITY, 1800000);
                    return;
                default:
                    return;
            }
        }
    }

    public class DeleteThread extends Thread {
        String mPath = "";

        public DeleteThread(String name) {
            super(name);
            this.mPath = name;
        }

        public void run() {
            Log.d(OppoLogService.TAG, " delete dir " + this.mPath);
            OppoLogService.deleteDir(this.mPath);
        }
    }

    class LogInfo {
        public int argu3;
        public int argu4;
        public int argu5;
        public int argu6;
        public int argu7;
        public int sensor_id;
        public int string_id;
        public long timestamp;
    }

    class SensorLogHandler extends Handler {
        SensorLogHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    private class ServerLogConfigUpdateHelper extends RomUpdateHelper {
        public ServerLogConfigUpdateHelper(Context context, String filterName, String systemFile, String dataFile) {
            super(context, filterName, systemFile, dataFile);
        }

        public void getUpdateFromProvider() {
            super.getUpdateFromProvider();
            Log.v(OppoLogService.TAG, "update log config mIsLogCoreServiceRunning : " + OppoLogService.this.mIsLogCoreServiceRunning);
            if (OppoLogService.this.mIsLogCoreServiceRunning) {
                OppoLogService.this.sendConfigUpdateConfig();
                return;
            }
            OppoLogService.this.mIsUpadteConfig = true;
            OppoLogService.this.initLogCoreService();
        }
    }

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                OppoLogService.this.checkLogSize();
            }
        }
    }

    static {
        mOppoInfoStringMap.put(Integer.valueOf(1), "*##*8110#");
        mOppoInfoStringMap.put(Integer.valueOf(2), "DESede");
        mOppoInfoStringMap.put(Integer.valueOf(3), "121234528238452452345289");
        mOppoInfoStringMap.put(Integer.valueOf(4), "sys.oppo.logkit.testtemp");
        mOppoInfoStringMap.put(Integer.valueOf(5), "12345678");
    }

    public OppoLogService(Context context) {
        r0 = new String[2][];
        r0[0] = new String[]{"ALS/PS reset", "enable_sched_data", "Prox ALGO trigger", "report ps state", "algo_set", "algo_get", "Prox calibration", "Als calibration", "ALS/PS ESD handle", "ALS/PS conf item", "high light", "attitude changed", "pad pasting", "upright thd", "high light decadent", "high light disappear", "high light algo set", "ps auto cali"};
        r0[1] = new String[]{"enable_sched_data", "Step count", "Step count after sleep"};
        this.flow_log_string = r0;
        this.ALSPS_APDS9922 = 0;
        this.ALSPS_STK3210 = 1;
        this.ALSPS_CM36286 = 2;
        this.ALSPS_TMD2725 = 3;
        this.ALSPS_TMD2745 = 4;
        this.ps_state_source = new String[]{"Int", "Timer"};
        this.alsps_type_string = new String[]{"APDS9922", "STK3210", "CM36286", "TMD2725", "TMD2745"};
        this.mSensorLogListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                LogInfo logInfo = new LogInfo();
                logInfo.sensor_id = (int) event.values[0];
                logInfo.string_id = (int) event.values[1];
                logInfo.argu3 = (int) event.values[2];
                logInfo.argu4 = (int) event.values[3];
                logInfo.argu5 = (int) event.values[4];
                logInfo.argu6 = (int) event.values[5];
                logInfo.argu7 = (int) event.values[6];
                logInfo.timestamp = event.timestamp;
                OppoLogService.this.writeSensorLogToFile(OppoLogService.this.log_library(logInfo));
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mLogSensor = null;
        this.mSensorLogWriter = null;
        this.FILE_MAX_LEN = 20971520;
        this.MAX_FILE_NUM = 3;
        this.kernelLogPath = "/data";
        this.mSensorLogHandlerThread = null;
        this.mServerSocket = null;
        this.mMainHandler = new Handler();
        this.mIsLogCoreServiceRunning = false;
        this.mIsUpadteConfig = false;
        this.mContext = context;
        initCriticallogSocket();
        initServerLogConfigHelper(this.mContext);
    }

    public void startSensorLog(boolean isOutPutFile) {
        registerLogSensor(this.mContext);
    }

    public void stopSensorLog() {
        ungisterLogSensor();
    }

    String log_library(LogInfo logInfo) {
        String res = "";
        String string_id_res = "";
        String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Timestamp(Long.valueOf(System.currentTimeMillis()).longValue()));
        switch (logInfo.sensor_id) {
            case 1:
            case 3:
            case 24:
                if (logInfo.string_id <= 9) {
                    string_id_res = String.format("%s, ", new Object[]{this.flow_log_string[1][logInfo.string_id]});
                    if (logInfo.string_id != 0) {
                        if (logInfo.string_id == 1 || logInfo.string_id == 2) {
                            res = String.format("num = %d", new Object[]{Integer.valueOf(logInfo.argu4 | (logInfo.argu5 << 16))});
                            break;
                        }
                    }
                    res = String.format("sensor = %d, enable = %d", new Object[]{Integer.valueOf(logInfo.sensor_id), Integer.valueOf(logInfo.argu3)});
                    break;
                }
                return res;
            case 5:
            case 6:
                if (logInfo.string_id <= 17) {
                    string_id_res = String.format("%s, ", new Object[]{this.flow_log_string[0][logInfo.string_id]});
                    if (logInfo.string_id != 1) {
                        if (logInfo.string_id != 2) {
                            if (logInfo.string_id != 3) {
                                if (logInfo.string_id != 4) {
                                    if (logInfo.string_id != 5) {
                                        if (logInfo.string_id != 6) {
                                            if (logInfo.string_id != 7) {
                                                if (logInfo.string_id != 8) {
                                                    if (logInfo.string_id != 9) {
                                                        if (logInfo.string_id != 10) {
                                                            if (logInfo.string_id != 11) {
                                                                if (logInfo.string_id != 12) {
                                                                    if (logInfo.string_id != 13) {
                                                                        if (logInfo.string_id != 14) {
                                                                            if (logInfo.string_id != 15) {
                                                                                if (logInfo.string_id != 16) {
                                                                                    if (logInfo.string_id == 17) {
                                                                                        res = String.format("offset_l = %4d, offset_h = %4d, prox_after_auto_cal = %4d, first_prox = %d, als_enable = %d", new Object[]{Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu5), Integer.valueOf(logInfo.argu3), Integer.valueOf(logInfo.argu6), Integer.valueOf(logInfo.argu7)});
                                                                                        break;
                                                                                    }
                                                                                }
                                                                                res = String.format("last_ps_min = %4d, high_thd = %4d, low_thd = %4d", new Object[]{Integer.valueOf(logInfo.argu5), Integer.valueOf(logInfo.argu6), Integer.valueOf(logInfo.argu7)});
                                                                                break;
                                                                            }
                                                                            res = String.format("last_ps_min = %4d, high_thd = %4d, low_thd = %4d", new Object[]{Integer.valueOf(logInfo.argu5), Integer.valueOf(logInfo.argu6), Integer.valueOf(logInfo.argu7)});
                                                                            break;
                                                                        }
                                                                        res = String.format("upright_flag = %4d, last_ps_min = %4d, decadent_ps = %4d, high_thd = %4d, low_thd = %4d", new Object[]{Integer.valueOf(logInfo.argu3), Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu5), Integer.valueOf(logInfo.argu6), Integer.valueOf(logInfo.argu7)});
                                                                        break;
                                                                    }
                                                                    res = String.format("prox = %4d, ps_min = %4d, high_thd = %4d, low_thd = %4d", new Object[]{Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu5), Integer.valueOf(logInfo.argu6), Integer.valueOf(logInfo.argu7)});
                                                                    break;
                                                                }
                                                                res = String.format("prox = %4d, ps_min = %4d, high_thd = %4d, low_thd = %4d", new Object[]{Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu5), Integer.valueOf(logInfo.argu6), Integer.valueOf(logInfo.argu7)});
                                                                break;
                                                            }
                                                            res = String.format("upright_flag = %4d, low_thd = %4d, high_thd = %4d", new Object[]{Integer.valueOf(logInfo.argu5), Integer.valueOf(logInfo.argu3), Integer.valueOf(logInfo.argu4)});
                                                            break;
                                                        }
                                                        res = String.format("prox = %4d, last_ps_not_highlight = %4d, ps_min = %4d, thresh_near = %4d, thresh_far = %4d", new Object[]{Integer.valueOf(logInfo.argu3), Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu6), Integer.valueOf(logInfo.argu7)});
                                                        break;
                                                    }
                                                    res = String.format("%s : als_factor = %4d, als_ratio = %3d, poffset = %3d, prox_crosstalk = %4d", new Object[]{this.alsps_type_string[logInfo.argu7], Integer.valueOf(logInfo.argu3), Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu5), Integer.valueOf(logInfo.argu6)});
                                                    break;
                                                }
                                                res = String.format("esd_err_time = 0x%x, als_enable = %d, ps_enable = %d", new Object[]{Integer.valueOf(logInfo.argu3), Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu5)});
                                                break;
                                            }
                                            res = String.format("als = %5d, als_factor = %4d", new Object[]{Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu5)});
                                            break;
                                        }
                                        res = String.format("current_prox = %4d, ps_offset = %4d, prox_crosstalk = %4d", new Object[]{Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu5), Integer.valueOf(logInfo.argu6)});
                                        break;
                                    }
                                    res = String.format("als = %5d, ps_state = %4d, prox = %4d", new Object[]{Integer.valueOf(logInfo.argu3), Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu5)});
                                    break;
                                }
                                res = String.format("ALOG state = %d, ps_min = %d, prox = %d, near = %4d, far = %4d", new Object[]{Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu5), Integer.valueOf(logInfo.argu3), Integer.valueOf(logInfo.argu6), Integer.valueOf(logInfo.argu7)});
                                break;
                            }
                            res = String.format("%s, ps_state = %d, prox = %4d, near = %4d, far = %4d, timestamp = %d", new Object[]{this.ps_state_source[logInfo.argu3], Integer.valueOf(logInfo.argu4), Integer.valueOf(logInfo.argu5), Integer.valueOf(logInfo.argu6), Integer.valueOf(logInfo.argu7), Long.valueOf(logInfo.timestamp)});
                            break;
                        }
                        String str = "%s";
                        Object[] objArr = new Object[1];
                        objArr[0] = logInfo.argu3 > 0 ? "enable" : "disable";
                        res = String.format(str, objArr);
                        break;
                    }
                    res = String.format("sensor = %d, enable = %d", new Object[]{Integer.valueOf(logInfo.sensor_id), Integer.valueOf(logInfo.argu3)});
                    break;
                }
                return res;
                break;
        }
        return datetime + " " + string_id_res + res;
    }

    void registerLogSensor(Context c) {
        if (this.mLogSensor == null) {
            Slog.d(TAG, "mLogSensor == null registerListener");
            this.mSensorManager = (SensorManager) c.getSystemService("sensor");
            this.mLogSensor = this.mSensorManager.getDefaultSensor(33171024);
            if (this.mLogSensor == null) {
                Slog.d(TAG, "mLogSensor == null registerListener");
                return;
            }
            startSensorLogHandlerThread();
            this.mSensorManager.registerListener(this.mSensorLogListener, this.mLogSensor, 0, this.mSensorLogHandler);
            return;
        }
        Slog.v(TAG, "logSensor is running");
    }

    void ungisterLogSensor() {
        if (this.mSensorManager != null) {
            this.mSensorManager.unregisterListener(this.mSensorLogListener, this.mLogSensor);
            this.mLogSensor = null;
            this.mSensorLogWriter = null;
        }
        stopSensorLogHandlerThread();
        closeSensorLogFile();
    }

    private void writeSensorLogToFile(String logString) {
        try {
            if (this.mSensorLogWriter == null) {
                this.mDirPath = "";
                int retryTimes = 0;
                do {
                    this.kernelLogPath = SystemProperties.get("sys.oppo.logkit.kernellog", "/data/oppo_log");
                    Slog.v(TAG, "get kernel log path = " + this.kernelLogPath + " retry time = " + retryTimes);
                    retryTimes++;
                    if (!this.kernelLogPath.equals("/data/oppo_log") && !this.kernelLogPath.trim().isEmpty()) {
                        RUtils.RUtilsCmd(String.format("chmod 777 %s", new Object[]{this.kernelLogPath}));
                        break;
                    }
                    Thread.sleep(1000);
                } while (retryTimes < 10);
                this.mOutPutingLog = new File(String.format(this.kernelLogPath + "/sensor_log0.txt", new Object[0]));
                this.mSensorLogWriter = new FileWriter(this.mOutPutingLog, true);
            }
            if (this.mOutPutingLog != null && this.mSensorLogWriter != null) {
                this.mSensorLogWriter.write(logString + "\r\n");
                if (this.mOutPutingLog.length() >= this.FILE_MAX_LEN) {
                    File file;
                    this.mSensorLogWriter.close();
                    this.mSensorLogWriter = null;
                    for (int i = this.MAX_FILE_NUM - 1; i >= 0; i--) {
                        file = new File(String.format("%s/sensor_log%d.txt", new Object[]{this.kernelLogPath, Integer.valueOf(i)}));
                        if (file.exists()) {
                            File newFile = new File(String.format("%s/sensor_log%d.txt", new Object[]{this.kernelLogPath, Integer.valueOf(i + 1)}));
                            Slog.v(TAG, "rename " + file + " to " + newFile);
                            file.renameTo(newFile);
                        }
                    }
                    file = new File(String.format("%s/sensor_log%d.txt", new Object[]{this.kernelLogPath, Integer.valueOf(this.MAX_FILE_NUM)}));
                    if (file.exists()) {
                        Slog.v(TAG, "delete " + file);
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Slog.v(TAG, "writeSensorLogToFile e =" + e.toString());
        }
    }

    void closeSensorLogFile() {
        try {
            if (this.mSensorLogWriter != null) {
                this.mSensorLogWriter.flush();
                this.mSensorLogWriter.close();
                this.mSensorLogWriter = null;
            }
        } catch (Exception e) {
            Slog.e(TAG, "closeSensorLogFile e = " + e.toString());
            try {
                if (this.mSensorLogWriter != null) {
                    this.mSensorLogWriter.close();
                    this.mSensorLogWriter = null;
                }
            } catch (Exception e2) {
                Slog.e(TAG, "closeSensorLogFile close e = " + e2.toString());
            }
        }
    }

    void startSensorLogHandlerThread() {
        if (this.mSensorLogHandlerThread == null) {
            this.mSensorLogHandlerThread = new HandlerThread("SensorLogThread");
            this.mSensorLogHandlerThread.start();
            this.mSensorLogLooper = this.mSensorLogHandlerThread.getLooper();
            this.mSensorLogHandler = new SensorLogHandler(this.mSensorLogLooper);
        }
    }

    void stopSensorLogHandlerThread() {
        try {
            if (this.mSensorLogLooper != null) {
                this.mSensorLogLooper.quitSafely();
                this.mSensorLogLooper = null;
            }
            if (this.mSensorLogHandlerThread != null) {
                this.mSensorLogHandlerThread.quitSafely();
                this.mSensorLogHandlerThread = null;
            }
        } catch (Exception e) {
            Slog.v(TAG, "stop thread error e = " + e.toString());
        }
    }

    void initCriticallogSocket() {
        try {
            this.mServerSocket = new LocalServerSocket("criticallog_socket");
            new Thread() {
                public void run() {
                    while (true) {
                        try {
                            Slog.v(OppoLogService.TAG, "Waiting for criticallog_socket connection...");
                            LocalSocket socket = OppoLogService.this.mServerSocket.accept();
                            if (socket != null) {
                                OppoLogService.this.startEchoThread(socket);
                            } else {
                                return;
                            }
                        } catch (Exception e) {
                            Slog.v(OppoLogService.TAG, "in accept: " + e);
                        }
                    }
                }
            }.start();
            startDcsHandlerThread();
        } catch (Exception e) {
            Slog.v(TAG, "critical_debug, e = " + e.toString());
        }
    }

    private void startEchoThread(final LocalSocket socket) {
        new Thread() {
            public void run() {
                try {
                    char[] data = new char[1024];
                    int ret = new InputStreamReader(socket.getInputStream()).read(data);
                    Slog.v(OppoLogService.TAG, "read len len = " + ret);
                    if (ret >= 0) {
                        data[ret] = 0;
                        new Thread(new CritiCalLogParserRunnable(OppoLogService.this.mContext, new String(data).substring(0, ret))).start();
                    }
                } catch (Exception e) {
                    Slog.v(OppoLogService.TAG, "in echo thread loop: " + e.getMessage());
                }
            }
        }.start();
    }

    void startDcsHandlerThread() {
        Slog.v(TAG, "startDcsHandlerThread");
        this.mDcsHandlerThread = new HandlerThread("DcsThread");
        this.mDcsHandlerThread.start();
        this.mDcsgLooper = this.mDcsHandlerThread.getLooper();
        this.mDcsHandler = new DcsHandler(this.mDcsgLooper);
        this.mDcsHandler.sendEmptyMessageDelayed(EventLogTags.AM_FINISH_ACTIVITY, 1800000);
    }

    void stopDcsHandlerThread() {
        try {
            if (this.mDcsgLooper != null) {
                this.mDcsgLooper.quitSafely();
                this.mDcsgLooper = null;
            }
            if (this.mDcsHandlerThread != null) {
                this.mDcsHandlerThread.quitSafely();
                this.mDcsHandlerThread = null;
            }
        } catch (Exception e) {
            Slog.v(TAG, "stop thread error e = " + e.toString());
        }
    }

    void addDcsRecord(Map<String, String> logMap) {
        Map record = null;
        try {
            synchronized (this.mRecordMutex) {
                for (Map<String, String> element : this.mDcsRecordHistory) {
                    if (((String) element.get("logType")).equals(logMap.get("logType"))) {
                        if (((String) element.get("module")).equals("Network")) {
                            if (checkCidInfo(element, logMap, "MCC") && checkCidInfo(element, logMap, "MNC") && checkCidInfo(element, logMap, "LAC") && checkCidInfo(element, logMap, "CID") && checkCidInfo(element, logMap, "SID") && checkCidInfo(element, logMap, "NID") && checkCidInfo(element, logMap, "BID")) {
                                record = element;
                                break;
                            }
                        }
                        Map<String, String> record2 = element;
                        break;
                    }
                }
                if (record2 == null) {
                    logMap.put("count", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                    this.mDcsRecordHistory.add(logMap);
                    Slog.v(TAG, "log add type = " + ((String) logMap.get("logType")) + " count = 1");
                } else {
                    increase(record2, (String) logMap.get("log"));
                }
            }
        } catch (Exception e) {
            Slog.v(TAG, "error in addDcsRecord: " + e.toString());
        }
    }

    void increase(Map<String, String> logMap, String log) {
        int count = 0;
        try {
            count = Integer.valueOf((String) logMap.get("count")).intValue();
        } catch (Exception e) {
            Slog.v(TAG, "can not parser the number: " + e.toString());
        }
        logMap.put("count", String.valueOf(count + 1));
    }

    boolean checkCidInfo(Map<String, String> record, Map<String, String> logMap, String cidType) {
        if (record.get(cidType) == null && logMap.get(cidType) == null) {
            return true;
        }
        if (record.get(cidType) == null || logMap.get(cidType) == null || !((String) record.get(cidType)).equals(logMap.get(cidType))) {
            return false;
        }
        return true;
    }

    private void checkLogSize() {
        try {
            SystemProperties.set("sys.calcute.finished", "0");
            SystemProperties.set("ctl.start", "calcutelogsize");
            while (SystemProperties.getInt("sys.calcute.finished", 0) != 1) {
                Thread.sleep(100);
            }
            int logSize = SystemProperties.getInt("sys.calcute.logsize", 0);
            Log.d(TAG, "logsize =" + logSize + "KB");
            if (logSize > LOG_MAX_SIZE) {
                if (this.mHandler != null) {
                    this.mHandler.removeMessages(1);
                }
                stopLogSizeMonitor();
                sendPopUpMsg();
            } else if (this.mHandler != null) {
                this.mHandler.sendEmptyMessageDelayed(1, 1800000);
            }
        } catch (Exception e) {
            Slog.v(TAG, "in checkLogSize error e: " + e.toString());
        }
    }

    public void startLogSizeMonitor() {
        try {
            if (this.mHandlerThread == null) {
                this.mHandlerThread = new HandlerThread("LogSizeMonitor");
                this.mHandlerThread.start();
                this.mHandler = new WorkerHandler(this.mHandlerThread.getLooper());
                this.mHandler.sendEmptyMessage(1);
                return;
            }
            this.mHandler.sendEmptyMessageDelayed(1, 1800000);
            Slog.v(TAG, "log size monotor mHandlerThread is exists send CHECK_LOG_SIZE");
        } catch (Exception e) {
            Slog.v(TAG, "startLogSizeMonitor error: " + e.toString());
        }
    }

    void stopLogSizeMonitor() {
        try {
            if (this.mHandler != null) {
                this.mHandler.removeMessages(1);
                this.mHandler = null;
            }
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
                this.mHandlerThread = null;
            }
        } catch (Exception e) {
            Slog.v(TAG, "stopLogSizeMonitor error: " + e.toString());
        }
    }

    private void sendPopUpMsg() {
        Slog.v(TAG, "send popup broadcast");
        this.mContext.sendBroadcast(new Intent("com.oppo.Stethoscope.popup"));
    }

    public void initLogCoreService() {
        bindService();
    }

    public boolean isLogCoreServiceRunning() {
        return this.mIsLogCoreServiceRunning;
    }

    private void bindService() {
        this.mServiceConnection = new ServiceConnection() {
            public void onServiceDisconnected(ComponentName name) {
                OppoLogService.this.mIsLogCoreServiceRunning = false;
                Slog.v(OppoLogService.TAG, "LogCoreService, onServiceDisconnected");
                OppoLogService.this.mContext.unbindService(this);
                OppoLogService.this.mMainHandler.postDelayed(new Runnable() {
                    public void run() {
                        Log.v(OppoLogService.TAG, "int handler initLogCoreService");
                        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                            OppoLogService.this.initLogCoreService();
                        }
                    }
                }, 500);
            }

            public void onServiceConnected(ComponentName arg0, IBinder arg1) {
                Slog.v(OppoLogService.TAG, "LogCoreService, connection");
                OppoLogService.this.mIsLogCoreServiceRunning = true;
                if (OppoLogService.this.mIsUpadteConfig) {
                    OppoLogService.this.sendConfigUpdateConfig();
                    OppoLogService.this.mIsUpadteConfig = false;
                }
            }
        };
        Slog.v(TAG, "statr Service");
        Intent in = new Intent();
        in.setClassName("com.oppo.logkit", "com.oppo.logkit.service.LogCoreService");
        in.setPackage("com.oppo.logkit");
        in.setAction("com.oppo.logkit.service.LogCoreService");
        boolean ret = this.mContext.bindService(in, this.mServiceConnection, 1);
    }

    public void unbindService() {
        if (this.mServiceConnection != null) {
            this.mContext.unbindService(this.mServiceConnection);
            Slog.v(TAG, "LogCoreService, unbindService");
            this.mIsLogCoreServiceRunning = false;
        }
    }

    public void assertKernelPanic() {
        try {
            Slog.e(TAG, "force assertKernelPanic");
            FileWriter sysrq_trigger = new FileWriter("/proc/sysrq-trigger");
            sysrq_trigger.write(99);
            sysrq_trigger.close();
        } catch (IOException e) {
            Slog.w(TAG, "Failed to write to /proc/sysrq-trigger", e);
        }
    }

    public String getOppoLogInfoString(int index) {
        String value = (String) mOppoInfoStringMap.get(Integer.valueOf(index));
        if (value == null) {
            return null;
        }
        return value;
    }

    public void deleteSystemLogFile() {
        DeleteThread deleteAnrPath = new DeleteThread("/data/anr/");
        DeleteThread deletedropBoxPath = new DeleteThread("/data/tombstones/");
        DeleteThread deleteSystemPath = new DeleteThread("/data/system/dropbox/");
        deleteAnrPath.start();
        deletedropBoxPath.start();
        deleteSystemPath.start();
    }

    private static void deleteFile(File file) {
        if (file != null && (file.exists() ^ 1) == 0) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File subFile : files) {
                        deleteFile(subFile);
                    }
                }
            }
            file.delete();
        }
    }

    private static boolean deleteDir(String dir) {
        int i = 0;
        File fileDir = new File(dir);
        Log.v(TAG, "deleteDir() : dir=" + fileDir.getAbsolutePath());
        if (!fileDir.exists() || (fileDir.isDirectory() ^ 1) != 0) {
            return false;
        }
        File[] files = fileDir.listFiles();
        if (files == null) {
            return false;
        }
        int length = files.length;
        while (i < length) {
            deleteFile(files[i]);
            i++;
        }
        return true;
    }

    private void sendConfigUpdateConfig() {
        this.mContext.sendBroadcast(new Intent("com.oppo.logkit.config.update"));
    }

    private void initServerLogConfigHelper(Context content) {
        Log.v(TAG, "initServerLogConfigHelper");
        this.mServerLogConfigHelper = new ServerLogConfigUpdateHelper(content, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        this.mServerLogConfigHelper.init();
        this.mServerLogConfigHelper.initUpdateBroadcastReceiver();
    }

    void iotop() {
        SystemProperties.set("ctl.start", "getiotop");
    }
}

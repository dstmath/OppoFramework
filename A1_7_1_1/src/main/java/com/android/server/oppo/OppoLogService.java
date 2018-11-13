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
import com.android.server.ColorOSDeviceIdleHelper;
import com.android.server.LocationManagerService;
import com.oppo.RomUpdateHelper;
import com.oppo.rutils.RUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import oppo.util.OppoStatistics;

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
public final class OppoLogService {
    private static final int CHECK_INTERVAL = 1800000;
    private static final int CHECK_LOG_SIZE = 1;
    private static final String DATA_FILE_DIR = "data/system/server_log_config.xml";
    private static final boolean DEBUG = true;
    public static final String FILTER_NAME = "oppo_log_kit_config";
    private static final int LOG_MAX_SIZE = 921600;
    private static final int MB_BYTES = 1024;
    private static final String SYS_FILE_DIR = "system/etc/server_log_config.xml";
    private static final String TAG = "OppoLogService";
    private static boolean mIsPanic;
    public static final Map<Integer, String> mOppoInfoStringMap = null;
    final int ALSPS_APDS9922;
    final int ALSPS_CM36286;
    final int ALSPS_LOG_ALGO_GET;
    final int ALSPS_LOG_ALGO_SET;
    final int ALSPS_LOG_ALSPS_CONF_ITEM;
    final int ALSPS_LOG_ALSPS_ESD_HANDLE;
    final int ALSPS_LOG_ALSPS_RESET;
    final int ALSPS_LOG_ALS_CALIBRATION;
    final int ALSPS_LOG_ENABLE_SCHED_DATA;
    final int ALSPS_LOG_MAX;
    final int ALSPS_LOG_PROX_CALIBRATION;
    final int ALSPS_LOG_PROX_HIGH_LIGHT;
    final int ALSPS_LOG_PROX_TILT;
    final int ALSPS_LOG_REPORT_PS_STATE;
    final int ALSPS_LOG_RPOX_ALGO_TRIGGER;
    final int ALSPS_STK3210;
    final int ALSPS_TMD2745;
    long FILE_MAX_LEN;
    final int G_LOG_ENABLE_SCHED_DATA;
    final int G_LOG_MAX;
    final int G_LOG_STEP_COUNT;
    final int G_LOG_STEP_COUNT_AFTER_SLEEP;
    int MAX_FILE_NUM;
    final int QC_SENSOR_TYPE_BASE;
    final int SENSOR_TYPE_LOGGER;
    final int SNS_DDF_SENSOR_ACCEL;
    final int SNS_DDF_SENSOR_AMBIENT;
    final int SNS_DDF_SENSOR_GYRO;
    final int SNS_DDF_SENSOR_MAG;
    final int SNS_DDF_SENSOR_PROXIMITY;
    final int SNS_DDF_SENSOR_STEP_COUNT;
    String[] alsps_type_string;
    String[][] flow_log_string;
    String kernelLogPath;
    private Context mContext;
    String mDirPath;
    private WorkerHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsLogCoreServiceRunning;
    private boolean mIsUpadteConfig;
    Sensor mLogSensor;
    Handler mMainHandler;
    File mOutPutingLog;
    Handler mSensorLogHandler;
    HandlerThread mSensorLogHandlerThread;
    SensorEventListener mSensorLogListener;
    Looper mSensorLogLooper;
    FileWriter mSensorLogWriter;
    SensorManager mSensorManager;
    private ServerLogConfigUpdateHelper mServerLogConfigHelper;
    private LocalServerSocket mServerSocket;
    private ServiceConnection mServiceConnection;
    String[] ps_state_source;

    class CritiCalLogParserRunnable implements Runnable {
        static final String Prefix = "CriticalLog";
        StringBuffer logBuf = new StringBuffer();
        Context mContext;
        String mCriticalLog;
        String mIssue = IElsaManager.EMPTY_PACKAGE;
        String mLogType = IElsaManager.EMPTY_PACKAGE;
        String mModule = IElsaManager.EMPTY_PACKAGE;
        String mOtaVersion = IElsaManager.EMPTY_PACKAGE;
        String mTime = IElsaManager.EMPTY_PACKAGE;
        String mUploadLog = IElsaManager.EMPTY_PACKAGE;

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
                    this.mLogType = "unknown";
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
                if (info.contains("MNC:") && info.contains("MCC")) {
                    for (String msg : info.split(",")) {
                        if (msg.contains("MCC") || msg.contains("MNC") || msg.contains("LAC") || msg.contains("CID") || msg.contains("SID") || msg.contains("NID") || msg.contains("BID")) {
                            String[] local = msg.trim().split(":");
                            if (local.length == 2) {
                                Slog.v(OppoLogService.TAG, "key = " + local[0].trim() + " value = " + local[1].trim());
                                logMap.put(local[0].trim(), local[1].trim());
                            }
                        }
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
                if (this.mModule.contains("Network")) {
                    parserNetworkLog(this.mUploadLog, logMap);
                }
                OppoStatistics.onCommon(this.mContext, Prefix, this.mIssue, logMap, false);
            } catch (Exception e) {
                Slog.v(OppoLogService.TAG, "uploadToDCS : " + e.getMessage());
            }
        }

        String captureName(String name) {
            char[] cs = name.toCharArray();
            cs[0] = (char) (cs[0] - 32);
            return String.valueOf(cs);
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.OppoLogService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.OppoLogService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.oppo.OppoLogService.<clinit>():void");
    }

    public OppoLogService(Context context) {
        this.QC_SENSOR_TYPE_BASE = 33171000;
        this.SENSOR_TYPE_LOGGER = 33171024;
        this.SNS_DDF_SENSOR_ACCEL = 1;
        this.SNS_DDF_SENSOR_MAG = 2;
        this.SNS_DDF_SENSOR_GYRO = 3;
        this.SNS_DDF_SENSOR_PROXIMITY = 5;
        this.SNS_DDF_SENSOR_AMBIENT = 6;
        this.SNS_DDF_SENSOR_STEP_COUNT = 24;
        this.ALSPS_LOG_ALSPS_RESET = 0;
        this.ALSPS_LOG_ENABLE_SCHED_DATA = 1;
        this.ALSPS_LOG_RPOX_ALGO_TRIGGER = 2;
        this.ALSPS_LOG_REPORT_PS_STATE = 3;
        this.ALSPS_LOG_ALGO_SET = 4;
        this.ALSPS_LOG_ALGO_GET = 5;
        this.ALSPS_LOG_PROX_CALIBRATION = 6;
        this.ALSPS_LOG_ALS_CALIBRATION = 7;
        this.ALSPS_LOG_ALSPS_ESD_HANDLE = 8;
        this.ALSPS_LOG_ALSPS_CONF_ITEM = 9;
        this.ALSPS_LOG_PROX_HIGH_LIGHT = 10;
        this.ALSPS_LOG_PROX_TILT = 11;
        this.ALSPS_LOG_MAX = 11;
        this.G_LOG_ENABLE_SCHED_DATA = 0;
        this.G_LOG_STEP_COUNT = 1;
        this.G_LOG_STEP_COUNT_AFTER_SLEEP = 2;
        this.G_LOG_MAX = 9;
        String[][] strArr = new String[2][];
        String[] strArr2 = new String[12];
        strArr2[0] = "ALS/PS reset";
        strArr2[1] = "enable_sched_data";
        strArr2[2] = "Prox ALGO trigger";
        strArr2[3] = "report ps state";
        strArr2[4] = "algo_set";
        strArr2[5] = "algo_get";
        strArr2[6] = "Prox calibration";
        strArr2[7] = "Als calibration";
        strArr2[8] = "ALS/PS ESD handle";
        strArr2[9] = "ALS/PS conf item";
        strArr2[10] = "PS high light";
        strArr2[11] = "PS tilt";
        strArr[0] = strArr2;
        strArr2 = new String[3];
        strArr2[0] = "enable_sched_data";
        strArr2[1] = "Step count";
        strArr2[2] = "Step count after sleep";
        strArr[1] = strArr2;
        this.flow_log_string = strArr;
        this.ALSPS_APDS9922 = 0;
        this.ALSPS_STK3210 = 1;
        this.ALSPS_CM36286 = 2;
        this.ALSPS_TMD2745 = 3;
        String[] strArr3 = new String[2];
        strArr3[0] = "Int";
        strArr3[1] = "Timer";
        this.ps_state_source = strArr3;
        strArr3 = new String[4];
        strArr3[0] = "APDS9922";
        strArr3[1] = "STK3210";
        strArr3[2] = "CM36286";
        strArr3[3] = "TMD2745";
        this.alsps_type_string = strArr3;
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
        this.mSensorLogWriter = null;
        this.FILE_MAX_LEN = 20971520;
        this.MAX_FILE_NUM = 3;
        this.kernelLogPath = "/data";
        this.mServerSocket = null;
        this.mMainHandler = new Handler();
        this.mIsLogCoreServiceRunning = false;
        this.mIsUpadteConfig = false;
        this.mContext = context;
        initCriticallogSocket();
        initMTKLogger();
    }

    public void startSensorLog(boolean isOutPutFile) {
        registerLogSensor(this.mContext);
    }

    public void stopSensorLog() {
        ungisterLogSensor();
    }

    String log_library(LogInfo logInfo) {
        String res = IElsaManager.EMPTY_PACKAGE;
        String string_id_res = IElsaManager.EMPTY_PACKAGE;
        String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Timestamp(Long.valueOf(System.currentTimeMillis()).longValue()));
        Object[] objArr;
        switch (logInfo.sensor_id) {
            case 1:
            case 3:
            case 24:
                if (logInfo.string_id <= 9) {
                    objArr = new Object[1];
                    objArr[0] = this.flow_log_string[1][logInfo.string_id];
                    string_id_res = String.format("%s, ", objArr);
                    if (logInfo.string_id != 0) {
                        if (logInfo.string_id == 1 || logInfo.string_id == 2) {
                            objArr = new Object[1];
                            objArr[0] = Integer.valueOf(logInfo.argu4 | (logInfo.argu5 << 16));
                            res = String.format("num = %d", objArr);
                            break;
                        }
                    }
                    objArr = new Object[2];
                    objArr[0] = Integer.valueOf(logInfo.sensor_id);
                    objArr[1] = Integer.valueOf(logInfo.argu3);
                    res = String.format("sensor = %d, enable = %d", objArr);
                    break;
                }
                return res;
            case 5:
            case 6:
                if (logInfo.string_id <= 11) {
                    objArr = new Object[1];
                    objArr[0] = this.flow_log_string[0][logInfo.string_id];
                    string_id_res = String.format("%s, ", objArr);
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
                                                            if (logInfo.string_id == 11) {
                                                                objArr = new Object[3];
                                                                objArr[0] = Integer.valueOf(logInfo.argu5);
                                                                objArr[1] = Integer.valueOf(logInfo.argu3);
                                                                objArr[2] = Integer.valueOf(logInfo.argu4);
                                                                res = String.format("upright_flag = %4d, low_thd = %4d, high_thd = %4d", objArr);
                                                                break;
                                                            }
                                                        }
                                                        objArr = new Object[5];
                                                        objArr[0] = Integer.valueOf(logInfo.argu3);
                                                        objArr[1] = Integer.valueOf(logInfo.argu4);
                                                        objArr[2] = Integer.valueOf(logInfo.argu4);
                                                        objArr[3] = Integer.valueOf(logInfo.argu6);
                                                        objArr[4] = Integer.valueOf(logInfo.argu7);
                                                        res = String.format("prox = %4d, last_ps_not_highlight = %4d, ps_min = %4d, thresh_near = %4d, thresh_far = %4d", objArr);
                                                        break;
                                                    }
                                                    objArr = new Object[5];
                                                    objArr[0] = this.alsps_type_string[logInfo.argu7];
                                                    objArr[1] = Integer.valueOf(logInfo.argu3);
                                                    objArr[2] = Integer.valueOf(logInfo.argu4);
                                                    objArr[3] = Integer.valueOf(logInfo.argu5);
                                                    objArr[4] = Integer.valueOf(logInfo.argu6);
                                                    res = String.format("%s : als_factor = %4d, als_ratio = %3d, poffset = %3d, prox_crosstalk = %4d", objArr);
                                                    break;
                                                }
                                                objArr = new Object[3];
                                                objArr[0] = Integer.valueOf(logInfo.argu3);
                                                objArr[1] = Integer.valueOf(logInfo.argu4);
                                                objArr[2] = Integer.valueOf(logInfo.argu5);
                                                res = String.format("esd_err_time = 0x%x, als_enable = %d, ps_enable = %d", objArr);
                                                break;
                                            }
                                            objArr = new Object[2];
                                            objArr[0] = Integer.valueOf(logInfo.argu4);
                                            objArr[1] = Integer.valueOf(logInfo.argu5);
                                            res = String.format("als = %5d, als_factor = %4d", objArr);
                                            break;
                                        }
                                        objArr = new Object[3];
                                        objArr[0] = Integer.valueOf(logInfo.argu4);
                                        objArr[1] = Integer.valueOf(logInfo.argu5);
                                        objArr[2] = Integer.valueOf(logInfo.argu6);
                                        res = String.format("current_prox = %4d, ps_offset = %4d, prox_crosstalk = %4d", objArr);
                                        break;
                                    }
                                    objArr = new Object[3];
                                    objArr[0] = Integer.valueOf(logInfo.argu3);
                                    objArr[1] = Integer.valueOf(logInfo.argu4);
                                    objArr[2] = Integer.valueOf(logInfo.argu5);
                                    res = String.format("als = %5d, ps_state = %4d, prox = %4d", objArr);
                                    break;
                                }
                                objArr = new Object[5];
                                objArr[0] = Integer.valueOf(logInfo.argu4);
                                objArr[1] = Integer.valueOf(logInfo.argu5);
                                objArr[2] = Integer.valueOf(logInfo.argu3);
                                objArr[3] = Integer.valueOf(logInfo.argu6);
                                objArr[4] = Integer.valueOf(logInfo.argu7);
                                res = String.format("ALOG state = %d, ps_min = %d, prox = %d, near = %4d, far = %4d", objArr);
                                break;
                            }
                            objArr = new Object[6];
                            objArr[0] = this.ps_state_source[logInfo.argu3];
                            objArr[1] = Integer.valueOf(logInfo.argu4);
                            objArr[2] = Integer.valueOf(logInfo.argu5);
                            objArr[3] = Integer.valueOf(logInfo.argu6);
                            objArr[4] = Integer.valueOf(logInfo.argu7);
                            objArr[5] = Long.valueOf(logInfo.timestamp);
                            res = String.format("%s, ps_state = %d, prox = %4d, near = %4d, far = %4d, timestamp = %d", objArr);
                            break;
                        }
                        String str = "%s";
                        Object[] objArr2 = new Object[1];
                        objArr2[0] = logInfo.argu3 > 0 ? "enable" : "disable";
                        res = String.format(str, objArr2);
                        break;
                    }
                    objArr = new Object[2];
                    objArr[0] = Integer.valueOf(logInfo.sensor_id);
                    objArr[1] = Integer.valueOf(logInfo.argu3);
                    res = String.format("sensor = %d, enable = %d", objArr);
                    break;
                }
                return res;
                break;
        }
        return datetime + " " + string_id_res + res;
    }

    void registerLogSensor(Context c) {
        this.mSensorManager = (SensorManager) c.getSystemService("sensor");
        this.mLogSensor = this.mSensorManager.getDefaultSensor(33171024);
        if (this.mLogSensor == null) {
            Slog.d("sensor_log_debug", "mLogSensor == null");
            return;
        }
        startSensorLogHandlerThread();
        this.mSensorManager.registerListener(this.mSensorLogListener, this.mLogSensor, 0, this.mSensorLogHandler);
    }

    void ungisterLogSensor() {
        if (this.mSensorManager != null) {
            this.mSensorManager.unregisterListener(this.mSensorLogListener, this.mLogSensor);
        }
        stopSensorLogHandlerThread();
        closeSensorLogFile();
    }

    private void writeSensorLogToFile(String logString) {
        try {
            Object[] objArr;
            if (this.mSensorLogWriter == null) {
                this.mDirPath = IElsaManager.EMPTY_PACKAGE;
                int retryTimes = 0;
                do {
                    this.kernelLogPath = SystemProperties.get("sys.oppo.logkit.kernellog", "/data/oppo_log");
                    Slog.v(TAG, "get kernel log path = " + this.kernelLogPath + " retry time = " + retryTimes);
                    retryTimes++;
                    if (!this.kernelLogPath.equals("/data/oppo_log") && !this.kernelLogPath.trim().isEmpty()) {
                        objArr = new Object[1];
                        objArr[0] = this.kernelLogPath;
                        RUtils.RUtilsCmd(String.format("chmod 777 %s", objArr));
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
                        objArr = new Object[2];
                        objArr[0] = this.kernelLogPath;
                        objArr[1] = Integer.valueOf(i);
                        file = new File(String.format("%s/sensor_log%d.txt", objArr));
                        if (file.exists()) {
                            objArr = new Object[2];
                            objArr[0] = this.kernelLogPath;
                            objArr[1] = Integer.valueOf(i + 1);
                            File newFile = new File(String.format("%s/sensor_log%d.txt", objArr));
                            Slog.v(TAG, "rename " + file + " to " + newFile);
                            file.renameTo(newFile);
                        }
                    }
                    objArr = new Object[2];
                    objArr[0] = this.kernelLogPath;
                    objArr[1] = Integer.valueOf(this.MAX_FILE_NUM);
                    file = new File(String.format("%s/sensor_log%d.txt", objArr));
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
                this.mSensorLogWriter.close();
                this.mSensorLogWriter = null;
            }
        } catch (Exception e) {
            Slog.e(TAG, "closeSensorLogFile e = " + e.toString());
        }
    }

    void startSensorLogHandlerThread() {
        this.mSensorLogHandlerThread = new HandlerThread("SensorLogThread");
        this.mSensorLogHandlerThread.start();
        this.mSensorLogLooper = this.mSensorLogHandlerThread.getLooper();
        this.mSensorLogHandler = new SensorLogHandler(this.mSensorLogLooper);
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
                this.mHandler.sendEmptyMessageDelayed(1, ColorOSDeviceIdleHelper.DEFAULT_TOTAL_INTERVAL_TO_IDLE);
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
            this.mHandler.sendEmptyMessageDelayed(1, ColorOSDeviceIdleHelper.DEFAULT_TOTAL_INTERVAL_TO_IDLE);
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

    public String getOppoLogInfoString(int index) {
        String value = (String) mOppoInfoStringMap.get(Integer.valueOf(index));
        if (value == null) {
            return null;
        }
        return value;
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

    /* JADX WARNING: Removed duplicated region for block: B:65:0x0171 A:{SYNTHETIC, Splitter: B:65:0x0171} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initMTKLogger() {
        IOException e;
        Throwable th;
        String LOGCONFIGPATH = "system/vendor/etc/mtklog-config.prop";
        String MTKLOGTYPE = "persist.sys.mtk.logtype";
        String MTKLOGUSER = "persist.sys.log.user";
        int mtklogConfig = 0;
        String mtkInitValue = SystemProperties.get(MTKLOGTYPE, IElsaManager.EMPTY_PACKAGE);
        if (mtkInitValue.isEmpty()) {
            Slog.v(TAG, "init the mtk logtype");
            File file = new File(LOGCONFIGPATH);
            if (file == null) {
                Log.v(TAG, "could not find the LOGCONFIGPATH");
                return;
            } else if (file.exists()) {
                BufferedReader bufferedReader = null;
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    while (true) {
                        try {
                            String tempString = reader.readLine();
                            if (tempString == null) {
                                break;
                            }
                            Slog.v(TAG, "mtk-log-prop, line : " + tempString);
                            if (tempString.contains("com.mediatek.log.mobile.enabled")) {
                                if (tempString.contains("true")) {
                                    mtklogConfig++;
                                }
                            } else if (tempString.contains("com.mediatek.log.modem.enabled")) {
                                if (tempString.contains("true")) {
                                    mtklogConfig += 2;
                                }
                            } else if (tempString.contains("com.mediatek.log.net.enabled")) {
                                if (tempString.contains("true")) {
                                    mtklogConfig += 4;
                                }
                            } else if (tempString.contains("com.mediatek.log.gps.enabled") && tempString.contains("true")) {
                                mtklogConfig += 16;
                            }
                        } catch (IOException e2) {
                            e = e2;
                            bufferedReader = reader;
                        } catch (Throwable th2) {
                            th = th2;
                            bufferedReader = reader;
                            if (bufferedReader != null) {
                            }
                            throw th;
                        }
                    }
                    Slog.v(TAG, "mtklogConfig : " + mtklogConfig);
                    SystemProperties.set(MTKLOGTYPE, String.valueOf(mtklogConfig));
                    if (mtklogConfig == 0) {
                        SystemProperties.set(MTKLOGUSER, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                    } else if (SystemProperties.get("ro.build.version.ota", IElsaManager.EMPTY_PACKAGE).contains("Pre")) {
                        Slog.v(TAG, "pre version");
                        SystemProperties.set(MTKLOGUSER, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                    } else {
                        SystemProperties.set(MTKLOGUSER, "0");
                    }
                    reader.close();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e3) {
                        }
                    }
                    bufferedReader = reader;
                } catch (IOException e4) {
                    e = e4;
                    try {
                        Log.v(TAG, "initMTKLogger fali : " + e.toString());
                        SystemProperties.set(MTKLOGUSER, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                        SystemProperties.set(MTKLOGTYPE, "23");
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e5) {
                            }
                        }
                        return;
                    } catch (Throwable th3) {
                        th = th3;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e6) {
                            }
                        }
                        throw th;
                    }
                }
                return;
            } else {
                Log.v(TAG, "mtklog-config.prop is not exists() ");
                return;
            }
        }
        Slog.v(TAG, "init the mtk logtype : " + mtkInitValue);
    }
}

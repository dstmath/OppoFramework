package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OppoBatteryStatsInternal;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.OppoThermalManager;
import android.util.Slog;
import android.util.Xml;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;

public final class OppoThermalService {
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final Uri CONTENT_URI_WHITE_LIST = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final String DATA_CONFIG_FILE_PATH = "/data/oppo/psw/sys_thermal_config.xml";
    private static final String FILTER_NAME = "sys_thermal_config";
    private static final int INVALID_DATA = -1023;
    /* access modifiers changed from: private */
    public static final Object LOCK = new Object();
    private static final int SLEEP_TIME = 100;
    private static final String SYS_CONFIG_FILE_PATH = "/system/etc/sys_thermal_config.xml";
    /* access modifiers changed from: private */
    public static final String TAG = OppoThermalService.class.getSimpleName();
    private static final String TAG_CAPTURE_LOG = "is_capture_log";
    private static final String TAG_CAPTURE_LOG_THRESHOLD = "capture_log_threshold";
    private static final String TAG_CPULOAD_REC_INTERV = "cpuload_rec_interv";
    private static final String TAG_CPULOAD_REC_THRESHOLD = "cpuload_rec_threshold";
    private static final String TAG_FEATUREON = "is_feature_on";
    private static final String TAG_HEAT1_ALIGN = "heat1_align";
    private static final String TAG_HEAT2_ALIGN = "heat2_align";
    private static final String TAG_HEAT3_ALIGN = "heat3_align";
    private static final String TAG_HEATHOLD_TIME_THRESHOLD = "heathold_time_threshold";
    private static final String TAG_HEATINC_RATIO_THRESHOLD = "topcpu_rec_threshold";
    private static final String TAG_HEAT_ALIGN = "heat_align";
    private static final String TAG_HEAT_HOLD_UPLOAD_TIME = "heat_hold_upload_time";
    private static final String TAG_HEAT_REC_INTERV = "heat_rec_interv";
    private static final String TAG_HEAT_THRESHOLD = "heat_threshold";
    private static final String TAG_LESS_HEAT_THRESHOLD = "less_heat_threshold";
    private static final String TAG_MONITOR_APP = "thermal_monitor_application";
    private static final String TAG_MONITOR_APP_LIMIT_TIME = "thermal_monitor_app_limit_time";
    private static final String TAG_MORE_HEAT_THRESHOLD = "more_heat_threshold";
    private static final String TAG_PREHEAT_DEX_OAT_THRESHOLD = "preheat_dex_oat_threshold";
    private static final String TAG_PREHEAT_THRESHOLD = "preheat_threshold";
    private static final String TAG_RECORD_HISTORY = "is_record_history";
    private static final String TAG_THERMAL_BATTERY_TEMP = "thermal_battery_temp";
    private static final String TAG_THERMAL_CAT_CPU_Freq_TNTERVAL = "thermal_cat_cpu__freq_interval";
    private static final String TAG_THERMAL_HEAT_PATH = "thermal_heat_path";
    private static final String TAG_THERMAL_HEAT_PATH1 = "thermal_heat_path1";
    private static final String TAG_THERMAL_HEAT_PATH2 = "thermal_heat_path2";
    private static final String TAG_THERMAL_HEAT_PATH3 = "thermal_heat_path3";
    private static final String TAG_THERMAL_TOP_PROP_SWITCH = "thermal_top_pro_switch";
    private static final String TAG_THERMAL_TOP_PROP_TNTERVAL = "thermal_top_pro_interval";
    private static final String TAG_THERMAL_TOP_RPO_COUNTS = "thermal_top_pro_counts";
    private static final String TAG_TOPCPU_REC_INTERV = "topcpu_rec_interv";
    private static final String TAG_TOPCPU_REC_THRESHOLD = "topcpu_rec_threshold";
    private static final String TAG_UPLOADDCS = "is_upload_dcs";
    private static final String TAG_UPLOADLOG = "is_upload_log";
    private static final String TAG_UPLOAD_ERRLOG = "is_upload_errlog";
    private static final String TAG_VERSION = "version";
    public boolean DEBUG = true;
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mIsSystemReady = false;
    private OppoBatteryStatsInternal mOppoBatteryStatsInt = null;
    private UpdateReceiver mReceiver = new UpdateReceiver();
    public int mThermalTemp = INVALID_DATA;
    public int mThermalTemp1 = INVALID_DATA;
    public int mThermalTemp2 = INVALID_DATA;
    public int mThermalTemp3 = INVALID_DATA;

    public native void native_update(String str, String str2, String str3, String str4, int i, int i2, int i3, int i4);

    private class ThermalHandler extends Handler {
        public ThermalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (OppoThermalService.this.DEBUG) {
                String access$000 = OppoThermalService.TAG;
                Slog.i(access$000, "handleMessage : " + message.what);
            }
            int i = message.what;
        }
    }

    public void init(Context context) {
        synchronized (LOCK) {
            this.mContext = context;
            initRomUpdateBroadcast(context);
        }
    }

    private void initRomUpdateBroadcast(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
        this.mContext.registerReceiver(this.mReceiver, filter);
        new Thread(new GetDataFromProviderRunnable(), "ThermalRomUpdate").start();
    }

    public void systemReady() {
        Slog.d(TAG, "systemReady.....");
        this.mIsSystemReady = true;
    }

    private class UpdateReceiver extends BroadcastReceiver {
        private UpdateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            ArrayList<String> changeList;
            if (intent.getAction().equals("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS") && (changeList = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST")) != null && changeList.contains(OppoThermalService.FILTER_NAME)) {
                new Thread(new GetDataFromProviderRunnable(), "ThermalRomUpdate").start();
                Slog.d(OppoThermalService.TAG, "ACTION_ROM_UPDATE_CONFIG_SUCCES");
            }
        }
    }

    private class GetDataFromProviderRunnable implements Runnable {
        public GetDataFromProviderRunnable() {
        }

        public void run() {
            if (OppoThermalService.this.DEBUG) {
                Slog.d(OppoThermalService.TAG, "start run ");
            }
            while (!OppoThermalService.this.mIsSystemReady) {
                try {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        String access$000 = OppoThermalService.TAG;
                        Slog.w(access$000, "sleep 100 ms is Interrupted because of " + e);
                    }
                    if (OppoThermalService.this.DEBUG) {
                        Slog.d(OppoThermalService.TAG, "sleep 100 ms ");
                    }
                } catch (Exception e2) {
                }
            }
            synchronized (OppoThermalService.LOCK) {
                if (!OppoThermalService.this.getConfigFromProvider() && !OppoThermalService.this.getLocalConfig(OppoThermalService.DATA_CONFIG_FILE_PATH)) {
                    boolean unused = OppoThermalService.this.getLocalConfig(OppoThermalService.SYS_CONFIG_FILE_PATH);
                }
            }
            Slog.d(OppoThermalService.TAG, "isSystemReady is true  !!!!! ");
            OppoThermalService.this.setThermalConfig();
        }
    }

    /* access modifiers changed from: protected */
    public OppoBatteryStatsInternal getBatteryStatsInternal() {
        if (this.mOppoBatteryStatsInt == null) {
            this.mOppoBatteryStatsInt = (OppoBatteryStatsInternal) LocalServices.getService(OppoBatteryStatsInternal.class);
        }
        return this.mOppoBatteryStatsInt;
    }

    public OppoThermalService(Context context) {
        this.mContext = context;
        init(context);
    }

    public void update() {
        if (OppoThermalManager.mThermalFeatureOn) {
            native_update(OppoThermalManager.mThermalHeatPath, OppoThermalManager.mThermalHeatPath1, OppoThermalManager.mThermalHeatPath2, OppoThermalManager.mThermalHeatPath3, OppoThermalManager.mHeatAlign, OppoThermalManager.mHeat1Align, OppoThermalManager.mHeat2Align, OppoThermalManager.mHeat3Align);
        }
    }

    public int getQuietThermTemp() {
        if (this.DEBUG) {
            String str = TAG;
            Slog.d(str, "Quiet Therm temp:" + this.mThermalTemp1);
        }
        return this.mThermalTemp1;
    }

    public int getPhoneTemp(int tempNo) {
        int tmpTemp;
        if (tempNo == 0) {
            tmpTemp = this.mThermalTemp;
        } else if (tempNo == 1) {
            tmpTemp = this.mThermalTemp1;
        } else if (tempNo == 2) {
            tmpTemp = this.mThermalTemp2;
        } else if (tempNo != 3) {
            tmpTemp = this.mThermalTemp;
        } else {
            tmpTemp = this.mThermalTemp3;
        }
        if (tmpTemp == INVALID_DATA) {
            return INVALID_DATA;
        }
        if (tmpTemp / 10 <= 40) {
            return tmpTemp + 15;
        }
        if (tmpTemp / 10 <= 48) {
            return tmpTemp + 20;
        }
        if (tmpTemp / 10 > 52) {
            return tmpTemp - 20;
        }
        int tmpTemp2 = 500;
        if (tmpTemp <= 500) {
            tmpTemp2 = tmpTemp;
        }
        return tmpTemp2;
    }

    public boolean isFeatureOn() {
        return OppoThermalManager.mThermalFeatureOn;
    }

    private boolean parseXml(XmlPullParser parser) {
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String strName = parser.getName();
                        parser.next();
                        parserConfigTag(strName, parser.getText());
                    }
                }
                eventType = parser.next();
            }
            return true;
        } catch (Exception e) {
            Slog.i(TAG, "parseXml: Got execption. ", e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0061, code lost:
        if (r3 == null) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0065, code lost:
        if (r4 != null) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0067, code lost:
        android.util.Slog.i(com.android.server.OppoThermalService.TAG, "getDataFromProvider: failed");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x006e, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0071, code lost:
        if (r5 >= android.os.OppoThermalManager.mConfigVersion) goto L_0x0094;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0073, code lost:
        r0 = com.android.server.OppoThermalService.TAG;
        android.util.Slog.i(r0, "getDataFromProvider:newVer = " + r5 + " oldVer = " + android.os.OppoThermalManager.mConfigVersion);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0093, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0094, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r2 = android.util.Xml.newPullParser();
        r1 = new java.io.StringReader(r4);
        r2.setInput(r1);
        parseXml(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a6, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00aa, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00ac, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        android.util.Slog.i(com.android.server.OppoThermalService.TAG, "getDataFromProvider: Got execption. ", r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b3, code lost:
        if (r1 == null) goto L_0x00b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00b6, code lost:
        return r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00b7, code lost:
        if (r1 != null) goto L_0x00b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b9, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00bc, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0045, code lost:
        if (r3 != null) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0047, code lost:
        r3.close();
     */
    public boolean getConfigFromProvider() {
        Cursor cursor = null;
        String strConfigList = null;
        strConfigList = null;
        strConfigList = null;
        strConfigList = null;
        int configVersion = 0;
        configVersion = 0;
        configVersion = 0;
        configVersion = 0;
        boolean isSuccess = true;
        isSuccess = true;
        Slog.i(TAG, "getConfigFromProvider FILTER_NAME =sys_thermal_config");
        try {
            cursor = this.mContext.getContentResolver().query(CONTENT_URI_WHITE_LIST, new String[]{"version", COLUMN_NAME_2}, "filtername=\"sys_thermal_config\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int versioncolumnIndex = cursor.getColumnIndex("version");
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                cursor.moveToNext();
                configVersion = cursor.getInt(versioncolumnIndex);
                strConfigList = cursor.getString(xmlcolumnIndex);
            }
        } catch (Exception e) {
            String str = TAG;
            Slog.i(str, "getDataFromProvider: Got execption. " + e);
            isSuccess = false;
            isSuccess = false;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public boolean getLocalConfig(String path) {
        boolean isSuccess;
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        String str = TAG;
        Slog.i(str, "getLocalConfig path=" + path);
        FileReader xmlReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            xmlReader = new FileReader(file);
            parser.setInput(xmlReader);
            isSuccess = parseXml(parser);
            try {
                xmlReader.close();
            } catch (IOException e) {
                Slog.i(TAG, "getLocalSavedConfig: Got execption close xmlReader. ", e);
            }
        } catch (Exception e2) {
            Slog.i(TAG, "getLocalSavedConfig: Got execption. ", e2);
            isSuccess = false;
            isSuccess = false;
            if (xmlReader != null) {
                xmlReader.close();
            }
        } catch (Throwable th) {
            if (xmlReader != null) {
                try {
                    xmlReader.close();
                } catch (IOException e3) {
                    Slog.i(TAG, "getLocalSavedConfig: Got execption close xmlReader. ", e3);
                }
            }
            throw th;
        }
        return isSuccess;
    }

    private void parserConfigTag(String tag, String value) {
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        boolean z4 = false;
        boolean z5 = false;
        boolean z6 = false;
        boolean z7 = false;
        int i = 0;
        boolean z8 = false;
        if (tag.equals(TAG_FEATUREON)) {
            try {
                if (Integer.parseInt(value) != 0) {
                    z2 = true;
                }
                OppoThermalManager.mThermalFeatureOn = z2;
            } catch (NumberFormatException e) {
                Slog.i(TAG, "mIsFeatureOn ", e);
            }
        } else if (tag.equals(TAG_UPLOADDCS)) {
            try {
                if (Integer.parseInt(value) != 0) {
                    z3 = true;
                }
                OppoThermalManager.mThermalUploadDcs = z3;
            } catch (NumberFormatException e2) {
                Slog.i(TAG, "mIsUploadDcs ", e2);
            }
        } else if (tag.equals(TAG_UPLOADLOG)) {
            try {
                if (Integer.parseInt(value) != 0) {
                    z4 = true;
                }
                OppoThermalManager.mThermalUploadLog = z4;
            } catch (NumberFormatException e3) {
                Slog.i(TAG, "TAG_UPLOADLOG ", e3);
            }
        } else if (tag.equals("version")) {
            try {
                OppoThermalManager.mConfigVersion = Integer.parseInt(value);
            } catch (NumberFormatException e4) {
                Slog.i(TAG, "mConfigVersion ", e4);
            }
        } else if (tag.equals(TAG_CAPTURE_LOG)) {
            try {
                if (Integer.parseInt(value) != 0) {
                    z5 = true;
                }
                OppoThermalManager.mThermalCaptureLog = z5;
            } catch (NumberFormatException e5) {
                Slog.i(TAG, "TAG_CAPTURE_LOG ", e5);
            }
        } else if (tag.equals(TAG_CAPTURE_LOG_THRESHOLD)) {
            try {
                OppoThermalManager.mThermalCaptureLogThreshold = Integer.parseInt(value);
            } catch (NumberFormatException e6) {
                Slog.i(TAG, "TAG_CAPTURE_LOG_THRESHOLD ", e6);
            }
        } else if (tag.equals(TAG_RECORD_HISTORY)) {
            try {
                if (Integer.parseInt(value) != 0) {
                    z6 = true;
                }
                OppoThermalManager.mRecordThermalHistory = z6;
            } catch (NumberFormatException e7) {
                Slog.i(TAG, "TAG_RECORD_HISTORY ", e7);
            }
        } else if (tag.equals(TAG_THERMAL_HEAT_PATH)) {
            try {
                OppoThermalManager.mThermalHeatPath = value;
            } catch (Exception e8) {
                Slog.i(TAG, "mThermalHeatPath ", e8);
            }
        } else if (tag.equals(TAG_THERMAL_HEAT_PATH1)) {
            try {
                OppoThermalManager.mThermalHeatPath1 = value;
            } catch (Exception e9) {
                Slog.i(TAG, "mThermalHeatPath ", e9);
            }
        } else if (tag.equals(TAG_THERMAL_HEAT_PATH2)) {
            try {
                OppoThermalManager.mThermalHeatPath2 = value;
            } catch (Exception e10) {
                Slog.i(TAG, "mThermalHeatPath ", e10);
            }
        } else if (tag.equals(TAG_THERMAL_HEAT_PATH3)) {
            try {
                OppoThermalManager.mThermalHeatPath3 = value;
            } catch (Exception e11) {
                Slog.i(TAG, "mThermalHeatPath ", e11);
            }
        } else if (tag.equals(TAG_MORE_HEAT_THRESHOLD)) {
            try {
                OppoThermalManager.mMoreHeatThreshold = Integer.parseInt(value);
            } catch (Exception e12) {
                Slog.i(TAG, "mHeatThreshold ", e12);
            }
        } else if (tag.equals(TAG_HEAT_THRESHOLD)) {
            try {
                OppoThermalManager.mHeatThreshold = Integer.parseInt(value);
            } catch (Exception e13) {
                Slog.i(TAG, "mHeatThreshold ", e13);
            }
        } else if (tag.equals(TAG_LESS_HEAT_THRESHOLD)) {
            try {
                OppoThermalManager.mLessHeatThreshold = Integer.parseInt(value);
            } catch (Exception e14) {
                Slog.i(TAG, "mHeatThreshold ", e14);
            }
        } else if (tag.equals(TAG_PREHEAT_THRESHOLD)) {
            try {
                OppoThermalManager.mPreHeatThreshold = Integer.parseInt(value);
            } catch (Exception e15) {
                Slog.i(TAG, "mPreHeatThreshold ", e15);
            }
        } else if (tag.equals(TAG_HEAT_HOLD_UPLOAD_TIME)) {
            try {
                OppoThermalManager.mHeatHoldUploadTime = Integer.parseInt(value);
            } catch (Exception e16) {
                Slog.i(TAG, "mHeatHoldUploadTime ", e16);
            }
        } else if (tag.equals(TAG_PREHEAT_THRESHOLD)) {
            try {
                OppoThermalManager.mPreHeatThreshold = Integer.parseInt(value);
            } catch (Exception e17) {
                Slog.i(TAG, "mPreHeatThreshold ", e17);
            }
        } else if (tag.equals(TAG_HEAT_ALIGN)) {
            try {
                OppoThermalManager.mHeatAlign = Integer.parseInt(value);
            } catch (Exception e18) {
                Slog.i(TAG, "mHeatAlign ", e18);
            }
        } else if (tag.equals(TAG_HEAT1_ALIGN)) {
            try {
                OppoThermalManager.mHeat1Align = Integer.parseInt(value);
            } catch (Exception e19) {
                Slog.i(TAG, "mHeat1Align ", e19);
            }
        } else if (tag.equals(TAG_HEAT2_ALIGN)) {
            try {
                OppoThermalManager.mHeat2Align = Integer.parseInt(value);
            } catch (Exception e20) {
                Slog.i(TAG, "mHeat2Align ", e20);
            }
        } else if (tag.equals(TAG_HEAT3_ALIGN)) {
            try {
                OppoThermalManager.mHeat3Align = Integer.parseInt(value);
            } catch (Exception e21) {
                Slog.i(TAG, "mHeat3Align ", e21);
            }
        } else if (tag.equals(TAG_HEAT3_ALIGN)) {
            try {
                OppoThermalManager.mHeat3Align = Integer.parseInt(value);
            } catch (Exception e22) {
                Slog.i(TAG, "mHeat3Align ", e22);
            }
        } else if (tag.equals(TAG_HEAT_REC_INTERV)) {
            try {
                OppoThermalManager.mHeatRecInterv = Integer.parseInt(value);
            } catch (Exception e23) {
                Slog.i(TAG, "mHeatRecInterv ", e23);
            }
        } else if (tag.equals(TAG_CPULOAD_REC_THRESHOLD)) {
            try {
                OppoThermalManager.mCpuLoadRecThreshold = Integer.parseInt(value);
            } catch (Exception e24) {
                Slog.i(TAG, "mCpuLoadRecThreshold ", e24);
            }
        } else if (tag.equals(TAG_CPULOAD_REC_INTERV)) {
            try {
                OppoThermalManager.mCpuLoadRecInterv = Integer.parseInt(value);
            } catch (Exception e25) {
                Slog.i(TAG, "mCpuLoadRecInterv ", e25);
            }
        } else if (tag.equals("topcpu_rec_threshold")) {
            try {
                OppoThermalManager.mCpuLoadRecThreshold = Integer.parseInt(value);
            } catch (Exception e26) {
                Slog.i(TAG, "mCpuLoadRecThreshold ", e26);
            }
        } else if (tag.equals(TAG_TOPCPU_REC_INTERV)) {
            try {
                OppoThermalManager.mTopCpuRecInterv = Integer.parseInt(value);
            } catch (Exception e27) {
                Slog.i(TAG, "mTopCpuRecInterv ", e27);
            }
        } else if (tag.equals("topcpu_rec_threshold")) {
            try {
                OppoThermalManager.mHeatIncRatioThreshold = Integer.parseInt(value);
            } catch (Exception e28) {
                Slog.i(TAG, "mHeatIncRatioThreshold ", e28);
            }
        } else if (tag.equals(TAG_HEATHOLD_TIME_THRESHOLD)) {
            try {
                OppoThermalManager.mHeatHoldTimeThreshold = Integer.parseInt(value);
            } catch (Exception e29) {
                Slog.i(TAG, "mHeatHoldTimeThreshold ", e29);
            }
        } else if (tag.equals(TAG_UPLOAD_ERRLOG)) {
            try {
                if (Integer.parseInt(value) != 0) {
                    z7 = true;
                }
                OppoThermalManager.mThermalUploadErrLog = z7;
            } catch (Exception e30) {
                Slog.i(TAG, "mThermalUploaderrLog ", e30);
            }
        } else if (tag.equals(TAG_MONITOR_APP)) {
            try {
                String[] applist = value.split(",");
                if (applist.length > 0) {
                    OppoThermalManager.mMonitorAppList.clear();
                    for (i++; i < applist.length; i++) {
                        OppoThermalManager.mMonitorAppList.add(applist[i].trim());
                    }
                }
            } catch (Exception e31) {
                Slog.i(TAG, "mMonitorAppList ", e31);
            }
        } else if (tag.equals(TAG_MONITOR_APP_LIMIT_TIME)) {
            try {
                OppoThermalManager.mMonitorAppLimitTime = Integer.parseInt(value);
            } catch (Exception e32) {
                Slog.i(TAG, "mMonitorAppLimitTime ", e32);
            }
        } else if (tag.equals(TAG_THERMAL_BATTERY_TEMP)) {
            try {
                if (Integer.parseInt(value) == 1) {
                    z8 = true;
                }
                OppoThermalManager.mThermalBatteryTemp = z8;
            } catch (Exception e33) {
                Slog.i(TAG, "mThermalBatteryTemp ", e33);
            }
        } else if (tag.equals(TAG_THERMAL_TOP_PROP_TNTERVAL)) {
            try {
                OppoThermalManager.mHeatTopProInterval = Integer.parseInt(value);
            } catch (Exception e34) {
                Slog.i(TAG, "mHeatTopProInterval ", e34);
            }
        } else if (tag.equals(TAG_THERMAL_CAT_CPU_Freq_TNTERVAL)) {
            try {
                OppoThermalManager.mHeatCaptureCpuFreqInterval = Integer.parseInt(value);
            } catch (Exception e35) {
                Slog.i(TAG, "mHeatCaptureCpuFreqInterval ", e35);
            }
        } else if (tag.equals(TAG_THERMAL_TOP_RPO_COUNTS)) {
            try {
                OppoThermalManager.mHeatTopProCounts = Integer.parseInt(value);
            } catch (Exception e36) {
                Slog.i(TAG, "mHeatTopProCounts ", e36);
            }
        } else if (tag.equals(TAG_PREHEAT_DEX_OAT_THRESHOLD)) {
            try {
                OppoThermalManager.mPreHeatDexOatThreshold = Integer.parseInt(value);
            } catch (Exception e37) {
                Slog.i(TAG, "mPreHeatDexOatThreshold ", e37);
            }
        } else if (tag.equals(TAG_THERMAL_TOP_PROP_SWITCH)) {
            try {
                if (Integer.parseInt(value) == 1) {
                    z = true;
                }
                OppoThermalManager.mHeatTopProFeatureOn = z;
            } catch (Exception e38) {
                Slog.i(TAG, "mHeatTopProCounts ", e38);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setThermalConfig() {
        try {
            getBatteryStatsInternal().setThermalConfigImpl();
        } catch (Exception e) {
            Slog.e(TAG, "setThermalConfig");
        }
    }
}

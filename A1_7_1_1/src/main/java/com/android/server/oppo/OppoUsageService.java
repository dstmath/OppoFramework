package com.android.server.oppo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IOppoUsageService.Stub;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Slog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class OppoUsageService extends Stub {
    private static final int DATA_TYPE_APK_DEL_EVENT = 22;
    private static final int DATA_TYPE_APK_INSTALL_EVENT = 23;
    private static final int DATA_TYPE_APP_USAGE = 9;
    private static final int DATA_TYPE_BOOT_TIME = 1;
    private static final int DATA_TYPE_DIAL_OUT_DURATION = 6;
    private static final int DATA_TYPE_IMEI_NO = 2;
    private static final int DATA_TYPE_INCOMING_DURATION = 7;
    private static final int DATA_TYPE_MAX = 23;
    private static final int DATA_TYPE_MAX_CHARGE_CURRENT_CONFIG = 17;
    private static final int DATA_TYPE_MAX_CHARGE_TEMPERATURE_CONFIG = 19;
    private static final int DATA_TYPE_MIN_CHARGE_TEMPERATURE_CONFIG = 18;
    private static final int DATA_TYPE_MOS_CONFIG = 16;
    private static final int DATA_TYPE_MSG_RECEIVE = 5;
    private static final int DATA_TYPE_MSG_SEND = 4;
    private static final int DATA_TYPE_PCBA_NO = 3;
    private static final int DATA_TYPE_PHONE_CALL_RECORD = 8;
    private static final int DATA_TYPE_PRODUCTLINE_LAST_TEST_FLAG = 21;
    private static final int DATA_TYPE_SECRECY_CONFIG = 20;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_D = false;
    private static final boolean DEBUG_E = false;
    private static final boolean DEBUG_I = false;
    private static boolean DEBUG_SCORE_M = false;
    private static final boolean DEBUG_W = false;
    private static final int MAX_BATCH_COUNT = 10;
    private static final int MSG_GET_IMEI_NO = 1;
    private static final int MSG_GET_PCBA_NO = 2;
    private static final int MSG_SAVE_APK_INSTALL = 4;
    private static final int MSG_SAVE_BOOT_TIME = 3;
    private static final int NORMAL_MSG_DELAY = 10000;
    private static final String PROP_NAME_PCBA_NO = "gsm.serial";
    private static final String TAG = "OppoUsageService";
    private static final String mConnectorForPkgNameAndTime = "|";
    private Context mContext;
    private int mCurrentCountOfReceivedMsg;
    private int mCurrentCountOfSendedMsg;
    private int mCurrentDialOutDuration;
    private String mCurrentImeiNO;
    private int mCurrentIncomingDuration;
    private String mCurrentPcbaNO;
    private int mGetImeiNORetry;
    private int mGetPcbaNORetry;
    private final Handler mHandler;
    private boolean mHasGotDialOutDuration;
    private boolean mHasGotHistoryCountOfReceivedMsg;
    private boolean mHasGotHistoryCountOfSendedMsg;
    private boolean mHasGotIncomingDuration;
    private BroadcastReceiver mPkgMsgReceiver;
    private boolean mRawPartionInitOk;
    private String mRecordStrSlitter;
    private ScoreMonitor mScoreMonitor;
    private Time mTimeObj;

    private class ScoreMonitor {
        private static final String ACTION_MONITOR_TIMER = "com.oppo.ScoreAppMonitor.MONITOR_TIMER";
        private static final String BROADCAST_ACTION_INFO_UPLOAD = "com.oppo.ScoreAppMonitor.UPLOAD";
        private static final int DEFAULT_START_TIME = 17;
        private static final String EXTRA_KEY_FILE_NAME = "filename";
        private static final String PERMISSION_START_MONITOR = "com.oppo.ScoreAppMonitor.permission.START_MONITOR";
        private static final String PROP_CONTROL_VERSION = "persist.version.confidential";
        private static final String SCORE_MONITOR_PACKAGE = "com.oppo.ScoreAppMonitor";
        private static final String SCORE_MONITOR_SERVICE = "com.oppo.ScoreAppMonitor.MonitorService";
        private AlarmManager mAlarmManager = null;
        private boolean mIsControlVersion = false;
        private Context mLocalContext = null;
        private BroadcastReceiver mScoreMonitorReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (OppoUsageService.DEBUG_SCORE_M) {
                    Slog.d(OppoUsageService.TAG, "mScoreMonitorReceiver onReceive:" + action);
                }
                if (action != null) {
                    if (action.equals(ScoreMonitor.BROADCAST_ACTION_INFO_UPLOAD)) {
                        String uploadFileName = intent.getStringExtra(ScoreMonitor.EXTRA_KEY_FILE_NAME);
                        if (OppoUsageService.DEBUG_SCORE_M) {
                            Slog.d(OppoUsageService.TAG, "mScoreMonitorReceiver onReceive:" + uploadFileName);
                        }
                        if (uploadFileName != null) {
                            ScoreMonitor.this.onFileUpload(uploadFileName);
                        }
                    } else if (action.equals(ScoreMonitor.ACTION_MONITOR_TIMER)) {
                        ScoreMonitor.this.startScoreMonitor();
                    }
                }
            }
        };

        public ScoreMonitor(Context context) {
            this.mLocalContext = context;
            initScoreMonitor();
        }

        private void initScoreMonitor() {
            this.mIsControlVersion = SystemProperties.getBoolean(PROP_CONTROL_VERSION, false);
            if (OppoUsageService.DEBUG_SCORE_M) {
                Slog.d(OppoUsageService.TAG, "initScoreMonitor, mIsControlVersion:" + this.mIsControlVersion);
            }
            if (this.mIsControlVersion) {
                regMonitorReceiver();
                initMonitorTimer();
            }
        }

        private void regMonitorReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_MONITOR_TIMER);
            intentFilter.addAction(BROADCAST_ACTION_INFO_UPLOAD);
            this.mLocalContext.registerReceiver(this.mScoreMonitorReceiver, intentFilter);
        }

        private void initMonitorTimer() {
            this.mAlarmManager = (AlarmManager) this.mLocalContext.getSystemService("alarm");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mLocalContext, 0, new Intent(ACTION_MONITOR_TIMER, null), 0);
            int startTimeHour = 17;
            int startTimeMinute = 0;
            long interval = 86400000;
            try {
                if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                    startTimeHour = SystemProperties.getInt("persist.sys.sc.h", 17);
                    startTimeMinute = SystemProperties.getInt("persist.sys.sc.m", 0);
                    interval = Long.valueOf(SystemProperties.getLong("persist.sys.sc.i", 86400000)).longValue();
                }
            } catch (Exception e) {
            }
            if (OppoUsageService.DEBUG_SCORE_M) {
                Slog.d(OppoUsageService.TAG, "initMonitorTimer, startTimeHour:" + startTimeHour + ", startTimeMinute:" + startTimeMinute + ", interval:" + interval);
            }
            this.mAlarmManager.setRepeating(0, getTime(startTimeHour, startTimeMinute), interval, pendingIntent);
            if (OppoUsageService.DEBUG_SCORE_M) {
                Slog.d(OppoUsageService.TAG, "initMonitorTimer, start timer.");
            }
        }

        public boolean isControlVersion() {
            return this.mIsControlVersion;
        }

        void emulateScoreMonitorStart() {
            if (OppoUsageService.DEBUG_SCORE_M) {
                Slog.d(OppoUsageService.TAG, "emulateScoreMonitorStart ...");
            }
            this.mLocalContext.sendBroadcast(new Intent(ACTION_MONITOR_TIMER, null));
            if (OppoUsageService.DEBUG_SCORE_M) {
                Slog.d(OppoUsageService.TAG, "emulateScoreMonitorStart end.");
            }
        }

        private void startScoreMonitor() {
            if (OppoUsageService.DEBUG_SCORE_M) {
                Slog.d(OppoUsageService.TAG, "startScoreMonitor, enable:" + this.mIsControlVersion);
            }
            if (this.mIsControlVersion) {
                ComponentName scoreMonitorServiceComponent = new ComponentName(SCORE_MONITOR_PACKAGE, SCORE_MONITOR_SERVICE);
                try {
                    Intent serviceIntent = new Intent();
                    serviceIntent.setComponent(scoreMonitorServiceComponent);
                    this.mLocalContext.startService(serviceIntent);
                } catch (Exception e) {
                    Slog.w(OppoUsageService.TAG, "startScoreMonitor failed!", e);
                }
                if (OppoUsageService.DEBUG_SCORE_M) {
                    Slog.d(OppoUsageService.TAG, "startScoreMonitor, send start action.");
                }
            }
        }

        private long getTime(int hourOfDay, int minute) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(11, hourOfDay);
            calendar.set(12, minute);
            calendar.set(13, 0);
            calendar.set(14, 0);
            long time = calendar.getTimeInMillis();
            if (System.currentTimeMillis() <= time) {
                return time;
            }
            calendar.add(5, 1);
            return calendar.getTimeInMillis();
        }

        private void onFileUpload(String fileName) {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.OppoUsageService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.OppoUsageService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.oppo.OppoUsageService.<clinit>():void");
    }

    private native int native_export_config();

    private native void native_finalizeRawPartition();

    private native String native_get_download_status(int i);

    private native int native_import_config_done();

    private native int native_import_config_init();

    private native int native_import_config_loop(String str);

    private native boolean native_initUsageRawPartition();

    private native int native_readDataRecordCount(int i);

    private native String native_readDataStrContent(int i, int i2, int i3);

    private native String native_readDataStrContentForSingleRecord(int i);

    private native int native_writeStringContentData(int i, String str, int i2);

    public OppoUsageService(Context context) {
        this.mContext = null;
        this.mRecordStrSlitter = Pattern.quote("#");
        this.mRawPartionInitOk = false;
        this.mCurrentImeiNO = null;
        this.mCurrentPcbaNO = null;
        this.mHasGotHistoryCountOfSendedMsg = false;
        this.mHasGotHistoryCountOfReceivedMsg = false;
        this.mCurrentCountOfSendedMsg = 0;
        this.mCurrentCountOfReceivedMsg = 0;
        this.mHasGotDialOutDuration = false;
        this.mHasGotIncomingDuration = false;
        this.mCurrentDialOutDuration = 0;
        this.mCurrentIncomingDuration = 0;
        this.mGetImeiNORetry = 7;
        this.mGetPcbaNORetry = 7;
        this.mTimeObj = new Time();
        this.mScoreMonitor = null;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                OppoUsageService oppoUsageService;
                switch (msg.what) {
                    case 1:
                        if (OppoUsageService.this.mGetImeiNORetry != 0) {
                            if (!OppoUsageService.this.getImeiNoFromPhone()) {
                                oppoUsageService = OppoUsageService.this;
                                oppoUsageService.mGetImeiNORetry = oppoUsageService.mGetImeiNORetry - 1;
                                sendMessageDelayed(obtainMessage(1), 10000);
                                break;
                            }
                            OppoUsageService.this.mGetImeiNORetry = 0;
                            OppoUsageService.this.saveImeiOrPcbaNoIfNew(OppoUsageService.this.mCurrentImeiNO, 2);
                            sendMessageDelayed(obtainMessage(2), 10000);
                            break;
                        }
                        sendMessageDelayed(obtainMessage(2), 10000);
                        return;
                    case 2:
                        if (OppoUsageService.this.mGetPcbaNORetry != 0) {
                            if (!OppoUsageService.this.getPcbaNoFromPhone()) {
                                oppoUsageService = OppoUsageService.this;
                                oppoUsageService.mGetPcbaNORetry = oppoUsageService.mGetPcbaNORetry - 1;
                                sendMessageDelayed(obtainMessage(2), 10000);
                                break;
                            }
                            OppoUsageService.this.mGetPcbaNORetry = 0;
                            OppoUsageService.this.saveImeiOrPcbaNoIfNew(OppoUsageService.this.mCurrentPcbaNO, 3);
                            break;
                        }
                        return;
                    case 3:
                        OppoUsageService.this.saveCurrentBootTime("startUp:" + OppoUsageService.this.getCurrentDateStr());
                        break;
                    case 4:
                        String pkgName = msg.obj;
                        if (pkgName != null) {
                            OppoUsageService.this.saveApkInstallEvent(pkgName);
                            break;
                        }
                        break;
                }
            }
        };
        this.mPkgMsgReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                    Uri data = intent.getData();
                    if (data != null) {
                        String pkgName = data.getSchemeSpecificPart();
                        if (pkgName != null) {
                            Message apkInstallMsg = OppoUsageService.this.mHandler.obtainMessage(4);
                            apkInstallMsg.obj = pkgName;
                            OppoUsageService.this.mHandler.sendMessageDelayed(apkInstallMsg, 20);
                        }
                    }
                }
            }
        };
        this.mContext = context;
        this.mRawPartionInitOk = native_initUsageRawPartition();
    }

    public void systemReady() {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 20000);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 30000);
        initPkgBroadcastReceive();
        this.mScoreMonitor = new ScoreMonitor(this.mContext);
    }

    private void saveCurrentBootTime(String bootTimeDateStr) {
        if (bootTimeDateStr != null && !bootTimeDateStr.isEmpty()) {
            int saveRes = native_writeStringContentData(1, bootTimeDateStr, 0);
        }
    }

    private String getCurrentDateStr() {
        this.mTimeObj.setToNow();
        return this.mTimeObj.format("%Y-%m-%d %H:%M:%S");
    }

    private List<String> getAllHistoryRecordData(int dataType) {
        int recordCount = native_readDataRecordCount(dataType);
        if (recordCount <= 0) {
            return null;
        }
        ArrayList<String> recordList = new ArrayList();
        int readBatchCount = recordCount / 10;
        for (int index = 0; index < readBatchCount; index++) {
            splitStr(native_readDataStrContent(dataType, (index * 10) + 1, (index + 1) * 10), this.mRecordStrSlitter, recordList);
        }
        if (recordCount - (readBatchCount * 10) > 0) {
            splitStr(native_readDataStrContent(dataType, (readBatchCount * 10) + 1, recordCount), this.mRecordStrSlitter, recordList);
        }
        return recordList;
    }

    private List<String> getHistoryRecordByIndex(int startIndex, int endIndex, int dataType, String logTag) {
        if (startIndex < 1 || endIndex < 1 || startIndex > endIndex) {
            return null;
        }
        int recordCount = native_readDataRecordCount(dataType);
        if (startIndex > recordCount) {
            return null;
        }
        if (endIndex > recordCount) {
            endIndex = recordCount;
        }
        ArrayList<String> recordList = new ArrayList();
        int attemptReadCount = (endIndex - startIndex) + 1;
        int readBatchCount = attemptReadCount / 10;
        for (int index = 0; index < readBatchCount; index++) {
            int tmpStartIndex = startIndex + (index * 10);
            splitStr(native_readDataStrContent(dataType, tmpStartIndex, (tmpStartIndex + 10) - 1), this.mRecordStrSlitter, recordList);
        }
        if (attemptReadCount - (readBatchCount * 10) > 0) {
            splitStr(native_readDataStrContent(dataType, (readBatchCount * 10) + startIndex, endIndex), this.mRecordStrSlitter, recordList);
        }
        return recordList;
    }

    public void testSaveSomeData(int dataType, String dataContent) {
        if (isValidDataType(dataType)) {
            String dataStr;
            switch (dataType) {
                case 1:
                    dataStr = dataContent;
                    if (dataContent == null || dataContent.isEmpty()) {
                        dataStr = getCurrentDateStr();
                    }
                    saveCurrentBootTime(dataStr);
                    break;
                case 2:
                    dataStr = dataContent;
                    if ((dataContent != null && !dataContent.isEmpty()) || !getImeiNoFromPhone()) {
                        saveImeiOrPcbaNoIfNew(dataContent, 2);
                        break;
                    } else {
                        saveImeiOrPcbaNoIfNew(this.mCurrentImeiNO, 2);
                        return;
                    }
                    break;
                case 3:
                    dataStr = dataContent;
                    if ((dataContent != null && !dataContent.isEmpty()) || !getPcbaNoFromPhone()) {
                        saveImeiOrPcbaNoIfNew(dataContent, 3);
                        break;
                    } else {
                        saveImeiOrPcbaNoIfNew(this.mCurrentPcbaNO, 3);
                        return;
                    }
                    break;
            }
        }
    }

    public List<String> getHistoryBootTime() {
        return getAllHistoryRecordData(1);
    }

    public List<String> getHistoryImeiNO() {
        return getAllHistoryRecordData(2);
    }

    public List<String> getHistoryPcbaNO() {
        return getAllHistoryRecordData(3);
    }

    public int getAppUsageHistoryRecordCount() {
        return native_readDataRecordCount(9);
    }

    private boolean writeHistoryRecord(String contentStr, String dateTimeStr, int dataType, int isSingleRecord, String logTag) {
        boolean z = true;
        if ((isSingleRecord != 0 && isSingleRecord != 1) || !isValidDataType(dataType) || contentStr == null || contentStr.isEmpty()) {
            return false;
        }
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            dateTimeStr = getCurrentDateStr();
            if (dateTimeStr == null || dateTimeStr.isEmpty()) {
                return false;
            }
        }
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(contentStr).append(mConnectorForPkgNameAndTime).append(dateTimeStr);
        String lastContentStr = strBuilder.toString();
        if (lastContentStr == null || lastContentStr.isEmpty()) {
            return false;
        }
        if (native_writeStringContentData(dataType, lastContentStr, isSingleRecord) <= 0) {
            z = false;
        }
        return z;
    }

    public List<String> getAppUsageHistoryRecords(int startIndex, int endIndex) {
        return getHistoryRecordByIndex(startIndex, endIndex, 9, "AppUsage");
    }

    public boolean writeAppUsageHistoryRecord(String appName, String dateTime) {
        return writeHistoryRecord(appName, dateTime, 9, 0, "AppUsage");
    }

    public int getHistoryCountOfSendedMsg() {
        return strValueToIntValue(native_readDataStrContentForSingleRecord(4), 0);
    }

    public int getHistoryCountOfReceivedMsg() {
        return strValueToIntValue(native_readDataStrContentForSingleRecord(5), 0);
    }

    public boolean accumulateHistoryCountOfSendedMsg(int newCountIncrease) {
        if (newCountIncrease <= 0) {
            return false;
        }
        if (!this.mHasGotHistoryCountOfSendedMsg) {
            this.mCurrentCountOfSendedMsg = getHistoryCountOfSendedMsg();
            this.mHasGotHistoryCountOfSendedMsg = true;
        }
        this.mCurrentCountOfSendedMsg += newCountIncrease;
        return doSaveHistoryCount(4, this.mCurrentCountOfSendedMsg, true, "SendedMsg");
    }

    public boolean accumulateHistoryCountOfReceivedMsg(int newCountIncrease) {
        if (newCountIncrease <= 0) {
            return false;
        }
        if (!this.mHasGotHistoryCountOfReceivedMsg) {
            this.mCurrentCountOfReceivedMsg = getHistoryCountOfReceivedMsg();
            this.mHasGotHistoryCountOfReceivedMsg = true;
        }
        this.mCurrentCountOfReceivedMsg += newCountIncrease;
        return doSaveHistoryCount(5, this.mCurrentCountOfReceivedMsg, true, "ReceivedMsg");
    }

    private boolean doSaveHistoryCount(int dataType, int saveValue, boolean isSingleRecord, String logTag) {
        boolean z = true;
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(saveValue);
        String contentStr = strBuilder.toString();
        if (contentStr == null || contentStr.isEmpty()) {
            return false;
        }
        int i;
        if (isSingleRecord) {
            i = 1;
        } else {
            i = 0;
        }
        if (native_writeStringContentData(dataType, contentStr, i) <= 0) {
            z = false;
        }
        return z;
    }

    public int getDialOutDuration() {
        return strValueToIntValue(native_readDataStrContentForSingleRecord(6), 0);
    }

    public int getInComingCallDuration() {
        return strValueToIntValue(native_readDataStrContentForSingleRecord(7), 0);
    }

    private int strValueToIntValue(String strValue, int defaultValue) {
        if (strValue == null || strValue.isEmpty()) {
            return defaultValue;
        }
        try {
            int intValue = Integer.parseInt(strValue);
            if (intValue < 0) {
                intValue = 0;
            }
            return intValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean accumulateDialOutDuration(int durationInMinute) {
        if (!this.mHasGotDialOutDuration) {
            this.mCurrentDialOutDuration = getDialOutDuration();
            this.mHasGotDialOutDuration = true;
        }
        this.mCurrentDialOutDuration += durationInMinute;
        return doSaveHistoryCount(6, this.mCurrentDialOutDuration, true, "DialOutDuration");
    }

    public boolean accumulateInComingCallDuration(int durationInMinute) {
        if (!this.mHasGotIncomingDuration) {
            this.mCurrentIncomingDuration = getInComingCallDuration();
            this.mHasGotIncomingDuration = true;
        }
        this.mCurrentIncomingDuration += durationInMinute;
        return doSaveHistoryCount(7, this.mCurrentIncomingDuration, true, "IncomingDuration");
    }

    public int getHistoryRecordsCountOfPhoneCalls() {
        return native_readDataRecordCount(8);
    }

    public List<String> getPhoneCallHistoryRecords(int startIndex, int endIndex) {
        return getHistoryRecordByIndex(startIndex, endIndex, 8, "PhoneCall");
    }

    public boolean writePhoneCallHistoryRecord(String phoneNoStr, String dateTime) {
        return writeHistoryRecord(phoneNoStr, dateTime, 8, 0, "PhoneCall");
    }

    public void shutDown() {
        saveCurrentBootTime("shutDown:" + getCurrentDateStr());
    }

    private boolean isValidDataType(int dataType) {
        if (dataType < 1 || dataType > 23) {
            return false;
        }
        return true;
    }

    private boolean getImeiNoFromPhone() {
        boolean result;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            if (telephonyManager == null) {
                return false;
            }
            this.mCurrentImeiNO = telephonyManager.getDeviceId();
            result = true;
            return result;
        } catch (Exception e) {
            result = false;
        }
    }

    private void saveImeiOrPcbaNoIfNew(String numberStr, int dataType) {
        if (numberStr != null && !numberStr.isEmpty()) {
            boolean isNewNumber = true;
            if (native_readDataRecordCount(dataType) > 0) {
                List<String> numberList = null;
                if (2 == dataType) {
                    numberList = getHistoryImeiNO();
                } else if (3 == dataType) {
                    numberList = getHistoryPcbaNO();
                }
                if (numberList != null && numberList.size() > 0) {
                    for (String numberInList : numberList) {
                        if (numberStr.equals(numberInList)) {
                            isNewNumber = false;
                            break;
                        }
                    }
                }
                return;
            }
            isNewNumber = true;
            if (isNewNumber) {
                int native_writeStringContentData = native_writeStringContentData(dataType, numberStr, 0);
            }
        }
    }

    private boolean getPcbaNoFromPhone() {
        String pcbaNOStr = SystemProperties.get(PROP_NAME_PCBA_NO);
        if (pcbaNOStr == null || pcbaNOStr.isEmpty()) {
            return false;
        }
        this.mCurrentPcbaNO = pcbaNOStr;
        return true;
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean splitStr(String contentStr, String strSlitter, ArrayList<String> recordList) {
        if (contentStr == null || contentStr.isEmpty() || strSlitter == null || recordList == null) {
            return false;
        }
        String[] tmpResArray = contentStr.split(strSlitter);
        if (tmpResArray != null && tmpResArray.length > 0) {
            for (Object add : tmpResArray) {
                recordList.add(add);
            }
        }
        return true;
    }

    protected void finalize() throws Throwable {
        native_finalizeRawPartition();
        super.finalize();
    }

    public int import_config_init() {
        Slog.d(TAG, "import_config_init");
        return native_import_config_init();
    }

    public int import_config_loop(String content) {
        return native_import_config_loop(content);
    }

    public int import_config_done() {
        Slog.d(TAG, "import_config_done");
        return native_import_config_done();
    }

    public int export_mos_config() {
        return native_export_config();
    }

    private boolean updateChargeInfomation(int dataType, int value) {
        boolean z = true;
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(value);
        String contentStr = strBuilder.toString();
        if (contentStr == null || contentStr.isEmpty()) {
            return false;
        }
        if (native_writeStringContentData(dataType, contentStr, 1) <= 0) {
            z = false;
        }
        return z;
    }

    public boolean updateMaxChargeCurrent(int current) {
        return updateChargeInfomation(17, current);
    }

    public boolean updateMaxChargeTemperature(int temp) {
        return updateChargeInfomation(19, temp);
    }

    public boolean updateMinChargeTemperature(int temp) {
        return updateChargeInfomation(18, temp);
    }

    private int getChargeInformation(int dataType, int default_value) {
        return strValueToIntValue(native_readDataStrContentForSingleRecord(dataType), default_value);
    }

    public int getMaxChargeCurrent() {
        return getChargeInformation(17, Integer.MIN_VALUE);
    }

    public int getMaxChargeTemperature() {
        return getChargeInformation(19, Integer.MIN_VALUE);
    }

    public int getMinChargeTemperature() {
        return getChargeInformation(18, Integer.MAX_VALUE);
    }

    public String getDownloadStatusString(int part) {
        return native_get_download_status(part);
    }

    public String loadSecrecyConfig() {
        return native_readDataStrContentForSingleRecord(20);
    }

    public int saveSecrecyConfig(String content) {
        return native_writeStringContentData(20, content, 1);
    }

    public int getProductLineLastTestFlag() {
        return strValueToIntValue(native_readDataStrContentForSingleRecord(21), -1);
    }

    public boolean setProductLineLastTestFlag(int flag) {
        if (native_writeStringContentData(21, Integer.toString(flag), 1) > 0) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean recordApkDeleteEvent(String deleteAppPkgName, String callerAppPkgName, String dateTime) {
        if (deleteAppPkgName == null || deleteAppPkgName.isEmpty() || callerAppPkgName == null || callerAppPkgName.isEmpty()) {
            return false;
        }
        return writeHistoryRecord(deleteAppPkgName + "/" + callerAppPkgName, dateTime, 22, 0, "ApkDelEvent");
    }

    public int getApkDeleteEventRecordCount() {
        return native_readDataRecordCount(22);
    }

    public List<String> getApkDeleteEventRecords(int startIndex, int endIndex) {
        return getHistoryRecordByIndex(startIndex, endIndex, 22, "ApkDelEvent");
    }

    private void initPkgBroadcastReceive() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(this.mPkgMsgReceiver, intentFilter);
    }

    private void saveApkInstallEvent(String pkgName) {
        boolean saveRes = recordApkInstallEvent(pkgName, "installer", null);
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean recordApkInstallEvent(String installAppPkgName, String callerAppPkgName, String dateTime) {
        if (installAppPkgName == null || installAppPkgName.isEmpty() || callerAppPkgName == null || callerAppPkgName.isEmpty()) {
            return false;
        }
        return writeHistoryRecord(installAppPkgName + "/" + callerAppPkgName, dateTime, 23, 0, "ApkInstallEvent");
    }

    public int getApkInstallEventRecordCount() {
        return native_readDataRecordCount(23);
    }

    public List<String> getApkInstallEventRecords(int startIndex, int endIndex) {
        return getHistoryRecordByIndex(startIndex, endIndex, 23, "ApkInstallEvent");
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        HashSet<String> argSet = new HashSet();
        for (String arg : args) {
            argSet.add(arg);
        }
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            if (argSet.contains("--openScLog") && this.mScoreMonitor != null) {
                pw.println("oppo usage state:open sc log");
                DEBUG_SCORE_M = true;
            }
            if (argSet.contains("--emulateSc") && this.mScoreMonitor != null) {
                pw.println("oppo usage state:emulateSc");
                this.mScoreMonitor.emulateScoreMonitorStart();
            }
        }
    }
}

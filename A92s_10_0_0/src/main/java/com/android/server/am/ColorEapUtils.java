package com.android.server.am;

import android.app.ApplicationErrorReport;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.FileObserver;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.oppo.OppoJunkRecorder;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.wm.IColorActivityRecordEx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;

public class ColorEapUtils {
    private static final String BINDER_FILE_NAME = "binder_info.txt";
    private static final String BINDER_PATH = "/sys/kernel/debug/binder/state";
    private static final String BINDER_ZIP_FILE_NAME = "binder_info.zip";
    private static final String CMD_GET_PERMISSION = "chmod 666 ";
    private static final String CMD_LOGCAT_LOG = "logcat -b events -b system -b radio -b main -b crash -d";
    private static final String CODE = "20120";
    private static final int CORE_POOL_SIZE = 1;
    private static final String DMESG_FILE_NAME = "dmesg.txt";
    private static final String DROPBOX_PATH = "/data/system/dropbox/";
    private static final String EAP_CONFIG_FILE_NAME = "/data/oppo/coloros/eap_moa/config/eap_config_list.xml";
    private static final String EAP_CONFIG_MOA_ANR_DCS_PATH = "/data/oppo/log/DCS/en/eap_moa/anr/";
    private static final String EAP_CONFIG_MOA_ANR_PATH = "/data/oppo/coloros/eap_moa/anr/";
    private static final String EAP_CONFIG_MOA_CRASH_DCS_PATH = "/data/oppo/log/DCS/en/eap_moa/crash/";
    private static final String EAP_CONFIG_MOA_CRASH_PATH = "/data/oppo/coloros/eap_moa/crash/";
    private static final String EAP_CONFIG_PATH = "/data/oppo/coloros/eap_moa/config/";
    private static final String EAP_DCS_PATH = "/data/oppo/log/DCS/en/eap/";
    private static final String EAP_FILE_ID = "eap_log_name";
    private static final String EAP_PATH = "/data/oppo/coloros/eap/";
    private static final String FUNCTION_COPY_EAP_BINDER_INFO = "copyEapBinderInfo";
    private static final String FUNCTION_DMESG = "opmgetdmesg";
    private static final String ID_EAP_LOG_CREATE = "eap_log_create";
    private static final String ID_EAP_LOG_DELETE = "eap_log_delete";
    private static final String INTENT_EAP = "oppo.intent.action.EAP_APP_ERROR";
    private static final int KEEP_ALIVE_SECONDS = 3;
    private static final String KEY_FUNCTION = "ctl.start";
    private static final String KEY_LOG_EAP_BINDER_INFO_PATH = "sys.eap.binderinfo.path";
    private static final String KEY_LOG_PATH = "sys.opm.logpath";
    private static final String KEY_SPECIAL_VERSION = "SPECIAL_OPPO_CONFIG";
    private static final String LOGCAT_FILE_NAME = "android.txt";
    private static final String LOG_ENABLE_ALL = "15";
    private static final String LOG_ENABLE_ANR = "8";
    private static final String LOG_ENABLE_CRASH = "4";
    private static final int LOW_STORAGE_SIZE = 209715200;
    private static final String MAP_FILE_ID = "fileId";
    private static final String MAP_REASON = "reason";
    private static final String MAP_RESULT = "result";
    private static final int MAXIMUM_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_EAP_FILE_NUM = 50;
    private static final int MAX_LOG_DELETE_TIME = 432000000;
    private static final int MAX_TRACE_LENGTH = 10240;
    private static final int ONE_DAY_TIME = 86400000;
    private static final String PROP_EAP_STATE = "debug.eap.state";
    private static final String PROP_MM_COMPATIBILITY = "oppo.debug.mm.compatibility.logs.level";
    private static final String QCOM_NAME = "qcom";
    private static final int RESULT_FAIL_1 = -1;
    private static final int RESULT_FAIL_2 = -2;
    private static final int RESULT_FAIL_3 = -3;
    private static final int RESULT_FAIL_4 = -4;
    private static final int RESULT_FAIL_5 = -5;
    private static final int RESULT_FAIL_6 = -6;
    private static final int RESULT_SUCCESS = 0;
    private static final String ROM_HARDWARE = "ro.hardware";
    private static final int SLEEP_TIME = 1000;
    private static final String TAG = "ColorEapUtils";
    private static final String TAG_MM_COMPATIBILITY_ANR = "mm.compatibility.tag.anr";
    private static final String TAG_MM_COMPATIBILITY_CRASH = "mm.compatibility.tag.crash";
    private static final String TAG_OTHER_JAVA_CRASH_LOG_SWITCH = "otherJavaCrashLogSwitch";
    private static final String TAG_OWN_JAVA_CRASH_LOG_SWITCH = "ownJavaCrashLogSwitch";
    private static final String TAG_WHITE_PKG = "whitePkg";
    private static final String TAG_WHITE_PKG_KEY = "whitePkgKey";
    /* access modifiers changed from: private */
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(1, MAXIMUM_POOL_SIZE, 3, TimeUnit.SECONDS, new LinkedBlockingQueue(), new ThreadFactory() {
        /* class com.android.server.am.ColorEapUtils.AnonymousClass1 */
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ColorEapTask #" + this.mCount.getAndIncrement());
        }
    });
    private static final String ZIP = ".zip";
    private static ColorEapUtils sEapUtils = null;
    private Context mContext;
    private Map<String, Integer> mCrashInfoMap = new HashMap();
    private String mDcsPath = EAP_DCS_PATH;
    private boolean mDebugSwitch = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private EapConfigObserver mEapConfigObserver;
    private String mEapPath = EAP_PATH;
    private boolean mIsSpecialVersion;
    private long mLastTimeMillis = 0;
    private boolean mOtherJavaCrashLogSwitch = true;
    private boolean mOwnJavaCrashLogSwitch = true;
    /* access modifiers changed from: private */
    public String mStackTrace;
    private ArrayList<String> mWhiteList = new ArrayList<>(Arrays.asList("android.hardware.camera.provider", "cameraserver", "camerahalserver", "oiface", "neo"));
    private ArrayList<String> mWhitePkgKeyList = new ArrayList<>();
    private ArrayList<String> mWhitePkgList = new ArrayList<>();

    private ColorEapUtils() {
        init();
    }

    public static ColorEapUtils getInstance() {
        if (sEapUtils == null) {
            sEapUtils = new ColorEapUtils();
        }
        return sEapUtils;
    }

    private void init() {
        this.mLastTimeMillis = System.currentTimeMillis();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r6.close();
        r6.close();
     */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x010f  */
    /* JADX WARNING: Removed duplicated region for block: B:54:? A[RETURN, SYNTHETIC] */
    public static String getStackTrace(File dataFile, int pid) {
        String result;
        StringBuilder sb;
        if (dataFile == null) {
            return "";
        }
        BufferedReader reader = null;
        StringBuilder info = new StringBuilder();
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(dataFile);
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(stream2));
            boolean reading = true;
            boolean isFindText = false;
            Log.d(TAG, "start to get stack trace: ");
            while (true) {
                String line = reader2.readLine();
                if (line == null || !reading) {
                    try {
                        break;
                    } catch (Exception e) {
                        e = e;
                        sb = new StringBuilder();
                    }
                } else {
                    if (line.contains("| sysTid=" + pid)) {
                        isFindText = true;
                        Log.d(TAG, ">>>" + line);
                    }
                    if (line.endsWith("sysTid=" + pid)) {
                        isFindText = true;
                        Log.d(TAG, ">>>" + line);
                    }
                    if (isFindText) {
                        if (line.startsWith("  at ") || line.startsWith("  native:") || line.startsWith("  #")) {
                            info.append(line);
                            Log.d(TAG, line + "");
                        } else if (line.equals("")) {
                            reading = false;
                        }
                    }
                }
            }
        } catch (Exception e2) {
            Log.d(TAG, "fail to get stack trace, " + e2);
            try {
                reader.close();
                stream.close();
            } catch (Exception e3) {
                e = e3;
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            try {
                reader.close();
                stream.close();
            } catch (Exception e4) {
                Log.d(TAG, "fail to get stack trace, " + e4);
            }
            throw th;
        }
        result = info.toString();
        if (result.length() <= MAX_TRACE_LENGTH) {
            return result.substring(0, MAX_TRACE_LENGTH);
        }
        return result;
        sb.append("fail to get stack trace, ");
        sb.append(e);
        Log.d(TAG, sb.toString());
        result = info.toString();
        if (result.length() <= MAX_TRACE_LENGTH) {
        }
    }

    public static void collectErrorInfo(Context context, String dropboxTag, String eventType, ProcessRecord process, String packageName, String timeInfo, IColorActivityRecordEx activityRecordEx, String subject, File dataFile, ApplicationErrorReport.CrashInfo crashInfo) {
    }

    public void collectInfo(final Context context, final String dropboxTag, final String eventType, final ProcessRecord process, final String packageName, final String timeInfo, final IColorActivityRecordEx activityRecordEx, final String subject, final File dataFile, final ApplicationErrorReport.CrashInfo crashInfo) {
        this.mContext = context;
        if (!isEapEnable()) {
            Log.d(TAG, "collectInfo, eap is disable!");
        } else {
            THREAD_POOL_EXECUTOR.execute(new Runnable() {
                /* class com.android.server.am.ColorEapUtils.AnonymousClass2 */

                public void run() {
                    try {
                        if (dropboxTag.contains("app_crash") || dropboxTag.contains("app_anr")) {
                            Intent errorIntent = new Intent(ColorEapUtils.INTENT_EAP);
                            errorIntent.setFlags(67108864);
                            errorIntent.putExtra("eventType", eventType);
                            errorIntent.putExtra("packageName", packageName);
                            errorIntent.putExtra(ColorEapUtils.MAP_FILE_ID, timeInfo);
                            if (process != null) {
                                errorIntent.putExtra("processName", process.processName);
                                errorIntent.putExtra("userId", process.userId);
                                errorIntent.putExtra("foreground", process.isInterestingToUserLocked());
                            }
                            if (activityRecordEx != null) {
                                errorIntent.putExtra(IColorAppStartupManager.TYPE_ACTIVITY, activityRecordEx.getshortComponentName());
                            }
                            if ("anr".equals(eventType)) {
                                errorIntent.putExtra("message", subject);
                                if (process != null) {
                                    String unused = ColorEapUtils.this.mStackTrace = ColorEapUtils.getStackTrace(dataFile, process.pid);
                                    errorIntent.putExtra("stackTrace", ColorEapUtils.this.mStackTrace);
                                }
                                ColorEapUtils.this.collectAnrInfo(packageName, process, subject, ColorEapUtils.this.mStackTrace);
                            }
                            if ("crash".equals(eventType) && crashInfo != null) {
                                errorIntent.putExtra("subtype", "java_crash");
                                errorIntent.putExtra("className", crashInfo.exceptionClassName);
                                errorIntent.putExtra("message", crashInfo.exceptionMessage);
                                String unused2 = ColorEapUtils.this.mStackTrace = crashInfo.stackTrace;
                                errorIntent.putExtra("stackTrace", ColorEapUtils.this.mStackTrace);
                            }
                            context.sendBroadcastAsUser(errorIntent, UserHandle.SYSTEM);
                        }
                    } catch (Exception e) {
                        Log.e(ColorEapUtils.TAG, "fail to collect app error info, " + e);
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static String getTimeInfo() {
        String uuid = UUID.randomUUID().toString();
        return uuid.substring(0, 8) + uuid.substring(9, 13) + uuid.substring(14, 18);
    }

    public void setCrashProcessRecord(ProcessRecord processRecord) {
        if (processRecord.pkgList != null && processRecord.pkgList.size() != 0) {
            String name = processRecord.pkgList.keyAt(0);
            Log.d(TAG, "setCrashProcessRecord: " + name);
        }
    }

    public String readTombstoneFile(Context context, InputStream stream, String fileId) {
        return null;
    }

    public String readTombstoneFile(Context context, String fileName, InputStream stream, String fileId, int userId) {
        BufferedReader reader;
        Throwable th;
        String pidLine;
        String signalLine;
        String pidLine2;
        String messageLine;
        String signalLine2;
        if (!isEapEnable()) {
            Log.d(TAG, "readTombstoneFile, eap is disable!");
            return null;
        } else if (stream == null) {
            return null;
        } else {
            StringBuilder info = new StringBuilder();
            String packageName = null;
            try {
                reader = new BufferedReader(new InputStreamReader(stream));
                String pidLine3 = "";
                String messageLine2 = "";
                String signalLine3 = "";
                boolean isFindText = false;
                while (true) {
                    try {
                        String line = reader.readLine();
                        if (line == null) {
                            pidLine2 = pidLine3;
                            messageLine = messageLine2;
                            signalLine2 = signalLine3;
                            break;
                        }
                        try {
                            if (line.startsWith("pid:")) {
                                pidLine3 = line;
                                Log.d(TAG, "pidLine: " + pidLine3);
                            }
                            if (line.startsWith("Abort message:")) {
                                messageLine2 = line;
                                Log.d(TAG, "messageLine: " + messageLine2);
                            }
                            if (line.startsWith("signal")) {
                                signalLine3 = line;
                                Log.d(TAG, "signalLine: " + signalLine3);
                            }
                            if (isFindText) {
                                if (line.equals("")) {
                                    pidLine2 = pidLine3;
                                    messageLine = messageLine2;
                                    signalLine2 = signalLine3;
                                    break;
                                }
                                info.append(line);
                            }
                            if (line.contains("backtrace:")) {
                                isFindText = true;
                            }
                        } catch (Exception e) {
                            e = e;
                            signalLine = TAG;
                            pidLine = "fail to close, ";
                            try {
                                Log.d(signalLine, "fail to get stack trace, " + e);
                                try {
                                    reader.close();
                                    stream.close();
                                } catch (Exception e2) {
                                    Log.d(signalLine, pidLine + e2);
                                }
                                return packageName;
                            } catch (Throwable th2) {
                                th = th2;
                                try {
                                    reader.close();
                                    stream.close();
                                } catch (Exception e3) {
                                    Log.d(signalLine, pidLine + e3);
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            signalLine = TAG;
                            pidLine = "fail to close, ";
                            th = th3;
                            reader.close();
                            stream.close();
                            throw th;
                        }
                    } catch (Exception e4) {
                        e = e4;
                        signalLine = TAG;
                        pidLine = "fail to close, ";
                        Log.d(signalLine, "fail to get stack trace, " + e);
                        reader.close();
                        stream.close();
                        return packageName;
                    } catch (Throwable th4) {
                        signalLine = TAG;
                        pidLine = "fail to close, ";
                        th = th4;
                        reader.close();
                        stream.close();
                        throw th;
                    }
                }
                if (TextUtils.isEmpty(pidLine2)) {
                    try {
                        reader.close();
                        stream.close();
                    } catch (Exception e5) {
                        Log.d(TAG, "fail to close, " + e5);
                    }
                    return null;
                }
                packageName = getName(pidLine2, context);
                try {
                    String signal = getSignal(signalLine2);
                    String message = getMessage(messageLine);
                    String trace = info.toString();
                    Log.d(TAG, "readTombstoneFile, packageName: " + packageName);
                    try {
                        collectCrashInfo(fileName, packageName, signal, message, trace);
                        if (!TextUtils.isEmpty(packageName)) {
                            this.mStackTrace = trace;
                            signalLine = TAG;
                            pidLine = "fail to close, ";
                            try {
                                collectNativeCrashInfo(context, packageName, message, signal, trace, fileId, userId);
                                collectFile(context, fileName, packageName, "app_native_crash", fileId, userId);
                            } catch (Exception e6) {
                                e = e6;
                                packageName = packageName;
                            } catch (Throwable th5) {
                                th = th5;
                                reader.close();
                                stream.close();
                                throw th;
                            }
                        } else {
                            signalLine = TAG;
                            pidLine = "fail to close, ";
                        }
                        try {
                            reader.close();
                            stream.close();
                            return packageName;
                        } catch (Exception e7) {
                            Log.d(signalLine, pidLine + e7);
                            return packageName;
                        }
                    } catch (Exception e8) {
                        e = e8;
                        signalLine = TAG;
                        pidLine = "fail to close, ";
                        packageName = packageName;
                        Log.d(signalLine, "fail to get stack trace, " + e);
                        reader.close();
                        stream.close();
                        return packageName;
                    } catch (Throwable th6) {
                        signalLine = TAG;
                        pidLine = "fail to close, ";
                        th = th6;
                        reader.close();
                        stream.close();
                        throw th;
                    }
                } catch (Exception e9) {
                    e = e9;
                    signalLine = TAG;
                    pidLine = "fail to close, ";
                    Log.d(signalLine, "fail to get stack trace, " + e);
                    reader.close();
                    stream.close();
                    return packageName;
                } catch (Throwable th7) {
                    signalLine = TAG;
                    pidLine = "fail to close, ";
                    th = th7;
                    reader.close();
                    stream.close();
                    throw th;
                }
            } catch (Exception e10) {
                e = e10;
                signalLine = TAG;
                pidLine = "fail to close, ";
                reader = null;
                Log.d(signalLine, "fail to get stack trace, " + e);
                reader.close();
                stream.close();
                return packageName;
            } catch (Throwable th8) {
                signalLine = TAG;
                pidLine = "fail to close, ";
                reader = null;
                th = th8;
                reader.close();
                stream.close();
                throw th;
            }
        }
    }

    private String getName(String pidLine, Context context) {
        try {
            String nameString = pidLine.substring(pidLine.indexOf(">>> ") + 4, pidLine.indexOf(" <<<"));
            Log.d(TAG, "tombstone name: " + nameString);
            if (!nameString.contains(".") || nameString.contains(SliceClientPermissions.SliceAuthority.DELIMITER)) {
                return getWhiteListProcessName(nameString);
            }
            if (!nameString.contains(":")) {
                return nameString;
            }
            try {
                return nameString.substring(0, nameString.indexOf(":"));
            } catch (Exception e) {
                e.printStackTrace();
                return nameString;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            Log.d(TAG, "fail to getName, " + e2);
            return null;
        }
    }

    private String getMessage(String messageLine) {
        if (TextUtils.isEmpty(messageLine)) {
            return "";
        }
        try {
            return messageLine.substring("Abort message: '".length(), messageLine.length() - 1);
        } catch (Exception e) {
            Log.d(TAG, "fail to getMessage, " + e);
            return "";
        }
    }

    private String getSignal(String signalLine) {
        if (TextUtils.isEmpty(signalLine)) {
            return "";
        }
        try {
            return signalLine.substring(0, signalLine.indexOf(","));
        } catch (Exception e) {
            Log.d(TAG, "fail to getSignal, " + e);
            return "";
        }
    }

    private void collectNativeCrashInfo(Context context, String packageName, String message, String signal, String trace, String fileId, int userId) {
        Intent errorIntent = new Intent(INTENT_EAP);
        errorIntent.setFlags(67108864);
        errorIntent.putExtra("eventType", "crash");
        errorIntent.putExtra("subtype", "native_crash");
        errorIntent.putExtra("processName", packageName);
        errorIntent.putExtra(MAP_FILE_ID, fileId);
        errorIntent.putExtra("message", message);
        errorIntent.putExtra("className", signal);
        errorIntent.putExtra("signal", signal);
        errorIntent.putExtra("stackTrace", trace);
        errorIntent.putExtra("userId", userId);
        context.sendBroadcastAsUser(errorIntent, UserHandle.SYSTEM);
    }

    private String getWhiteListProcessName(String nameString) {
        if (TextUtils.isEmpty(nameString)) {
            return null;
        }
        for (int i = 0; i < this.mWhiteList.size(); i++) {
            String itemName = this.mWhiteList.get(i);
            if (nameString.contains(itemName)) {
                return itemName;
            }
        }
        return null;
    }

    private boolean isOverCrashLimit(String stackTrace) {
        if (!this.mCrashInfoMap.containsKey(stackTrace)) {
            return false;
        }
        Integer index = this.mCrashInfoMap.get(stackTrace);
        boolean overLimit = false;
        if (index == null) {
            return false;
        }
        if (index.intValue() >= 5) {
            overLimit = true;
        }
        return overLimit;
    }

    private boolean isOverEapLimit() {
        int maxLength = 50;
        if (checkSpecialVersion()) {
            maxLength = 500;
        }
        File eapDir = new File(this.mEapPath);
        if (eapDir.exists() && getEapFileLength(eapDir) >= maxLength + 5) {
            return true;
        }
        return false;
    }

    private void refreshCrashInfoMap(String tag) {
        if (this.mCrashInfoMap.containsKey(this.mStackTrace)) {
            int value = this.mCrashInfoMap.get(this.mStackTrace).intValue() + 1;
            this.mCrashInfoMap.put(this.mStackTrace, Integer.valueOf(value));
            Log.d(TAG, "mCrashInfoMap: " + value);
            return;
        }
        this.mCrashInfoMap.put(this.mStackTrace, 1);
        Log.d(TAG, "mCrashInfoMap: 1");
    }

    private void clearCrashInfoMap() {
        int diffDay = (int) ((System.currentTimeMillis() - this.mLastTimeMillis) / 86400000);
        Log.d(TAG, "diffDay: " + diffDay);
        if (diffDay >= 1) {
            this.mCrashInfoMap.clear();
            Log.d(TAG, "over one day, clear mCrashInfoMap");
        }
        this.mLastTimeMillis = System.currentTimeMillis();
    }

    public void collectFile(Context context, String fileName, String packageName, String tag, String fileId) {
        collectFile(context, fileName, packageName, tag, fileId, context.getUserId());
    }

    public synchronized void collectFile(Context context, String fileName, String packageName, String tag, String fileId, int userId) {
        try {
            this.mIsSpecialVersion = checkSpecialVersion();
            if (!isShouldCollectLogFile(tag, packageName)) {
                Log.d(TAG, "eap should not collect the " + tag + " log file!");
                reportResult(fileId, -6, packageName);
            } else if (!isEapEnable()) {
                reportResult(fileId, -1);
                Log.d(TAG, "collectFile, eap is disable!");
            } else if (isLowStorage()) {
                reportResult(fileId, -5);
                Log.d(TAG, "low storage, stop collect file");
            } else {
                setEapDcsPath(packageName, tag);
                trimToFit();
                if (isOverEapLimit()) {
                    reportResult(fileId, -2);
                    Log.d(TAG, "over eap log limit, stop collect file");
                    return;
                }
                clearCrashInfoMap();
                if (isOverCrashLimit(this.mStackTrace)) {
                    reportResult(fileId, -3);
                    Log.d(TAG, "over crash limit, stop collect file");
                    return;
                }
                refreshCrashInfoMap(tag);
                String scr = DROPBOX_PATH + fileName;
                String destDirPath = this.mEapPath + fileId;
                File destDir = new File(destDirPath);
                String dest = destDirPath + SliceClientPermissions.SliceAuthority.DELIMITER + fileName;
                Log.d(TAG, "collectFile: " + packageName + ", " + fileId + ", " + tag);
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                copyFile(scr, dest);
                if (tag != null && tag.contains("anr")) {
                    collectBinderInfo(destDirPath);
                }
                collectKernelInfo(destDirPath + SliceClientPermissions.SliceAuthority.DELIMITER);
                collectLogcatInfo(destDirPath);
                File[] listFiles = destDir.listFiles();
                for (File file : listFiles) {
                    Log.d(TAG, "eapFile: " + file.getName());
                }
                try {
                    String versionName = getVersionName(context, packageName, userId);
                    zipFile(destDirPath, (this.mEapPath + getZipFileName(packageName, versionName, tag, fileId)) + ZIP);
                    deleteDirAndFile(destDir);
                    statisticsData(fileId);
                    reportResult(fileId, 0);
                } catch (Exception e) {
                    e = e;
                }
            }
        } catch (Exception e2) {
            e = e2;
            String info = Log.getStackTraceString(e);
            reportResult(fileId, -4, info);
            Log.d(TAG, "fail to collectFile, " + info);
        }
    }

    private void collectBinderInfo(String binderDestPath) {
        if (QCOM_NAME.equals(SystemProperties.get(ROM_HARDWARE))) {
            try {
                String binderInfoTxtPath = binderDestPath + SliceClientPermissions.SliceAuthority.DELIMITER + BINDER_FILE_NAME;
                File binderInfoFile = new File(binderInfoTxtPath);
                if (!binderInfoFile.exists()) {
                    binderInfoFile.createNewFile();
                }
                SystemProperties.set(KEY_LOG_EAP_BINDER_INFO_PATH, binderInfoTxtPath);
                SystemProperties.set(KEY_FUNCTION, FUNCTION_COPY_EAP_BINDER_INFO);
                Thread.sleep(2000);
                zipFile(binderInfoTxtPath, binderDestPath + SliceClientPermissions.SliceAuthority.DELIMITER + BINDER_ZIP_FILE_NAME);
                if (binderInfoFile.exists()) {
                    binderInfoFile.delete();
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            zipFile(BINDER_PATH, binderDestPath + SliceClientPermissions.SliceAuthority.DELIMITER + BINDER_ZIP_FILE_NAME);
        }
    }

    private void collectKernelInfo(String destDirPath) {
        Log.d(TAG, "collect kernel log");
        SystemProperties.set(KEY_LOG_PATH, destDirPath);
        SystemProperties.set(KEY_FUNCTION, FUNCTION_DMESG);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    private void collectLogcatInfo(String destDirPath) {
        String filePath = destDirPath + SliceClientPermissions.SliceAuthority.DELIMITER + LOGCAT_FILE_NAME;
        startFileCommand(CMD_LOGCAT_LOG, filePath);
        startCommand(CMD_GET_PERMISSION + filePath);
        startCommand("chmod 666 dmesg.txt");
    }

    private void zipFile(String src, String dest) {
        File[] entries;
        Log.d(TAG, "zipFile: " + src + " to " + dest);
        ZipOutputStream out = null;
        try {
            File outFile = new File(dest);
            File fileOrDirectory = new File(src);
            if (!fileOrDirectory.exists()) {
                Log.d(TAG, src + " is not exists");
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                ZipOutputStream out2 = new ZipOutputStream(new FileOutputStream(outFile));
                if (fileOrDirectory.isFile()) {
                    startZipFile(out2, fileOrDirectory, "");
                } else {
                    for (File file : fileOrDirectory.listFiles()) {
                        startZipFile(out2, file, "");
                    }
                }
                try {
                    out2.close();
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
            }
        } catch (IOException ex3) {
            ex3.printStackTrace();
            Log.d(TAG, "error: " + ex3);
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex4) {
                    ex4.printStackTrace();
                }
            }
            throw th;
        }
    }

    private void startZipFile(ZipOutputStream out, File fileOrDirectory, String curPath) {
        FileInputStream in = null;
        try {
            if (!fileOrDirectory.isDirectory()) {
                byte[] buffer = new byte[4096];
                in = new FileInputStream(fileOrDirectory);
                out.putNextEntry(new ZipEntry(curPath + fileOrDirectory.getName()));
                while (true) {
                    int readBytes = in.read(buffer);
                    if (readBytes == -1) {
                        break;
                    }
                    out.write(buffer, 0, readBytes);
                }
                out.closeEntry();
            } else {
                File[] entries = fileOrDirectory.listFiles();
                for (File file : entries) {
                    startZipFile(out, file, curPath + fileOrDirectory.getName() + SliceClientPermissions.SliceAuthority.DELIMITER);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex2) {
            ex2.printStackTrace();
            Log.d(TAG, "error: " + ex2);
            if (in != null) {
                in.close();
            }
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex3) {
                    ex3.printStackTrace();
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:106:0x016c  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0114 A[SYNTHETIC, Splitter:B:65:0x0114] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0138  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0148 A[SYNTHETIC, Splitter:B:88:0x0148] */
    private boolean copyFile(String srcDir, String destPath) {
        Throwable th;
        FileChannel dstChannel;
        Log.d(TAG, "copyFile " + srcDir + " to " + destPath);
        boolean result = false;
        File src = new File(srcDir);
        if (destPath == null) {
            return false;
        }
        if (!src.exists()) {
            Log.d(TAG, srcDir + " is not exists");
            return false;
        }
        File dest = new File(destPath);
        if (!dest.getParentFile().exists()) {
            if (dest.getParentFile().mkdirs()) {
                Log.d(TAG, "success to mkdirs");
            } else {
                Log.d(TAG, "fail to mkdirs");
            }
        }
        if (dest.exists()) {
            dest.delete();
        }
        try {
            dest.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "error: " + e);
        }
        FileChannel dstChannel2 = null;
        FileChannel dstChannel3 = null;
        try {
            FileChannel srcChannel = new FileInputStream(src).getChannel();
            try {
                dstChannel = new FileOutputStream(dest).getChannel();
            } catch (Exception e2) {
                e = e2;
                dstChannel2 = srcChannel;
                try {
                    e.printStackTrace();
                    Log.d(TAG, "error: " + e);
                    if (dstChannel2 != null) {
                    }
                    if (dstChannel3 != null) {
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (dstChannel2 != null) {
                    }
                    if (dstChannel3 != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                dstChannel2 = srcChannel;
                if (dstChannel2 != null) {
                }
                if (dstChannel3 != null) {
                }
                throw th;
            }
            try {
                srcChannel.transferTo(0, srcChannel.size(), dstChannel);
                result = true;
                try {
                    srcChannel.close();
                    if (dstChannel != null) {
                        try {
                            dstChannel.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                } catch (IOException e4) {
                    e4.printStackTrace();
                    if (dstChannel != null) {
                        dstChannel.close();
                    }
                } catch (Throwable th4) {
                    if (dstChannel != null) {
                        try {
                            dstChannel.close();
                        } catch (IOException e5) {
                            e5.printStackTrace();
                        }
                    }
                    throw th4;
                }
            } catch (Exception e6) {
                e = e6;
                dstChannel3 = dstChannel;
                dstChannel2 = srcChannel;
                e.printStackTrace();
                Log.d(TAG, "error: " + e);
                if (dstChannel2 != null) {
                }
                if (dstChannel3 != null) {
                }
                return result;
            } catch (Throwable th5) {
                th = th5;
                dstChannel3 = dstChannel;
                dstChannel2 = srcChannel;
                if (dstChannel2 != null) {
                    try {
                        dstChannel2.close();
                    } catch (IOException e7) {
                        e7.printStackTrace();
                        if (dstChannel3 != null) {
                            try {
                                dstChannel3.close();
                            } catch (IOException e8) {
                                e8.printStackTrace();
                            }
                        }
                    } catch (Throwable th6) {
                        if (dstChannel3 != null) {
                            try {
                                dstChannel3.close();
                            } catch (IOException e9) {
                                e9.printStackTrace();
                            }
                        }
                        throw th6;
                    }
                }
                if (dstChannel3 != null) {
                    dstChannel3.close();
                }
                throw th;
            }
        } catch (Exception e10) {
            e = e10;
            e.printStackTrace();
            Log.d(TAG, "error: " + e);
            if (dstChannel2 != null) {
                try {
                    dstChannel2.close();
                } catch (IOException e11) {
                    e11.printStackTrace();
                    if (dstChannel3 != null) {
                        try {
                            dstChannel3.close();
                        } catch (IOException e12) {
                            e12.printStackTrace();
                        }
                    }
                } catch (Throwable th7) {
                    if (dstChannel3 != null) {
                        try {
                            dstChannel3.close();
                        } catch (IOException e13) {
                            e13.printStackTrace();
                        }
                    }
                    throw th7;
                }
            }
            if (dstChannel3 != null) {
                dstChannel3.close();
            }
            return result;
        }
        return result;
    }

    private String getZipFileName(String pkgName, String versionName, String tag, String fileId) {
        String otaVersion = SystemProperties.get("ro.build.version.ota");
        return pkgName + "_" + versionName + "_" + tag + "@" + fileId + "@" + otaVersion + "@" + getDate();
    }

    private String getDate() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(new Date());
    }

    private long dateToStamp(String s) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").parse(s).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void deleteDirAndFile(File dir) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] listFiles = dir.listFiles();
            for (File file : listFiles) {
                if (file.isFile()) {
                    file.delete();
                } else if (file.isDirectory()) {
                    deleteDirAndFile(file);
                }
            }
            dir.delete();
        }
    }

    private void trimToFit() {
        File eapFile = new File(this.mEapPath);
        if (eapFile.exists()) {
            File oldFile = deleteObsoleteFile(eapFile, System.currentTimeMillis());
            int maxLength = 50;
            if (this.mIsSpecialVersion) {
                maxLength = 500;
            }
            int length = getEapFileLength(eapFile);
            if (oldFile != null && length >= maxLength) {
                reportDeleteResult(-2, oldFile);
                Log.d(TAG, "over number, delete: " + oldFile.getName());
                oldFile.delete();
            }
        }
    }

    private void statisticsData(String fileId) {
        File eapFile = new File(this.mEapPath);
        File dcsFile = new File(this.mDcsPath);
        StringBuilder eapInfo = new StringBuilder();
        StringBuilder dcsInfo = new StringBuilder();
        if (eapFile.exists()) {
            for (File file : eapFile.listFiles()) {
                eapInfo.append(file.getName());
                eapInfo.append(";");
            }
        }
        if (dcsFile.exists()) {
            for (File file2 : dcsFile.listFiles()) {
                dcsInfo.append(file2.getName());
                dcsInfo.append(";");
            }
        }
        HashMap<String, String> map = new HashMap<>();
        map.put(MAP_FILE_ID, fileId);
        map.put("eapInfo", ((Object) eapInfo) + "");
        map.put("dcsInfo", ((Object) dcsInfo) + "");
        OppoStatistics.onCommon(this.mContext, "20120", EAP_FILE_ID, map, false);
    }

    private void reportResult(String fileId, int result) {
        reportResult(fileId, result, null);
    }

    private void reportResult(String fileId, int result, String attach) {
        HashMap<String, String> map = new HashMap<>();
        map.put(MAP_FILE_ID, fileId);
        map.put(MAP_RESULT, result + "");
        switch (result) {
            case -6:
                map.put("reason", attach + "config should not colloct the log");
                break;
            case -5:
                map.put("reason", "low storage");
                break;
            case -4:
                map.put("reason", "fail to collectFile:" + attach);
                break;
            case -3:
                map.put("reason", "over crash limit");
                break;
            case -2:
                map.put("reason", "over eap log limit");
                break;
            case -1:
                map.put("reason", "eap is disable");
                break;
            case 0:
                map.put("reason", "create success");
                break;
        }
        OppoStatistics.onCommon(this.mContext, "20120", ID_EAP_LOG_CREATE, map, false);
    }

    private void reportDeleteResult(int reason, File file) {
        HashMap<String, String> map = new HashMap<>();
        map.put("reason", reason + "");
        map.put(MAP_RESULT, file.getName());
        OppoStatistics.onCommon(this.mContext, "20120", ID_EAP_LOG_DELETE, map, false);
    }

    private void startCommand(String command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            Log.e(TAG, "fail to startCommand : " + e);
        }
    }

    private void startFileCommand(String command, String path) {
        BufferedReader bufferedReader = null;
        FileOutputStream out = null;
        try {
            BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream()));
            FileOutputStream out2 = new FileOutputStream(path);
            while (true) {
                String line = bufferedReader2.readLine();
                if (line != null) {
                    out2.write(line.getBytes());
                    out2.write(StringUtils.LF.getBytes());
                } else {
                    try {
                        break;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            bufferedReader2.close();
            try {
                out2.close();
            } catch (IOException e12) {
                e12.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e13) {
                    e13.printStackTrace();
                }
            }
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e14) {
                    e14.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e15) {
                    e15.printStackTrace();
                }
            }
            throw th;
        }
    }

    private boolean checkSpecialVersion() {
        if ("1".equals(SystemProperties.get(KEY_SPECIAL_VERSION))) {
            return true;
        }
        return false;
    }

    private boolean isEapEnable() {
        if ("0".equals(SystemProperties.get(PROP_EAP_STATE))) {
            return false;
        }
        return true;
    }

    private boolean isLowStorage() {
        try {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            if (statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong() < 209715200) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.d(TAG, "failed to get storage avail size");
            e.printStackTrace();
            return false;
        }
    }

    private class EapConfigObserver extends FileObserver {
        private String mPath;

        protected EapConfigObserver(String path) {
            super(path, 8);
            this.mPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && ColorEapUtils.EAP_CONFIG_FILE_NAME.equals(this.mPath)) {
                ColorEapUtils.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    /* class com.android.server.am.ColorEapUtils.EapConfigObserver.AnonymousClass1 */

                    public void run() {
                        ColorEapUtils.this.readEapConfigFile();
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0177 A[SYNTHETIC, Splitter:B:63:0x0177] */
    public synchronized void readEapConfigFile() {
        String str;
        String str2;
        int type;
        File eapConfigFile = new File(EAP_CONFIG_FILE_NAME);
        if (!eapConfigFile.exists()) {
            Log.e(TAG, "readEapConfigFile: eap config file doesn't exist!");
            return;
        }
        this.mWhitePkgList.clear();
        this.mWhitePkgKeyList.clear();
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(eapConfigFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2) {
                    String tag = parser.getName();
                    if (TAG_OWN_JAVA_CRASH_LOG_SWITCH.equals(tag)) {
                        String value = parser.nextText();
                        if (!TextUtils.isEmpty(value)) {
                            this.mOwnJavaCrashLogSwitch = Boolean.parseBoolean(value);
                            if (this.mDebugSwitch) {
                                Log.i(TAG, "eap config file key: " + tag + " value: " + value);
                            }
                        }
                    } else if (TAG_OTHER_JAVA_CRASH_LOG_SWITCH.equals(tag)) {
                        String value2 = parser.nextText();
                        if (!TextUtils.isEmpty(value2)) {
                            this.mOtherJavaCrashLogSwitch = Boolean.parseBoolean(value2);
                            if (this.mDebugSwitch) {
                                Log.i(TAG, "eap config file key: " + tag + " value: " + value2);
                            }
                        }
                    } else if (TAG_WHITE_PKG.equals(tag)) {
                        String value3 = parser.nextText();
                        if (!TextUtils.isEmpty(value3)) {
                            this.mWhitePkgList.add(value3);
                            if (this.mDebugSwitch) {
                                Log.i(TAG, "eap config file key: " + tag + " value: " + value3);
                            }
                        }
                    } else if (TAG_WHITE_PKG_KEY.equals(tag)) {
                        String value4 = parser.nextText();
                        if (!TextUtils.isEmpty(value4)) {
                            this.mWhitePkgKeyList.add(value4);
                            if (this.mDebugSwitch) {
                                Log.i(TAG, "eap config file key: " + tag + " value: " + value4);
                            }
                        }
                    }
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (IOException e) {
                str = TAG;
                str2 = "failed to close state FileInputStream " + e;
            }
        } catch (Exception e2) {
            Log.e(TAG, "failed parsing eap config file ", e2);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    str = TAG;
                    str2 = "failed to close state FileInputStream " + e3;
                }
            }
        } catch (Throwable th) {
            th = th;
            if (stream != null) {
            }
            throw th;
        }
        Log.e(str, str2);
    }

    public void initEapConfig() {
        try {
            createDirs(EAP_CONFIG_PATH, EAP_CONFIG_MOA_ANR_PATH, EAP_CONFIG_MOA_CRASH_PATH, EAP_DCS_PATH, EAP_CONFIG_MOA_CRASH_DCS_PATH, EAP_CONFIG_MOA_ANR_DCS_PATH);
            File eapConfigFile = new File(EAP_CONFIG_FILE_NAME);
            if (!eapConfigFile.exists() && !eapConfigFile.createNewFile()) {
                Log.e(TAG, "create eap config file failed!!!");
            }
            if (eapConfigFile.length() > 0) {
                readEapConfigFile();
            }
            this.mEapConfigObserver = new EapConfigObserver(EAP_CONFIG_FILE_NAME);
            this.mEapConfigObserver.startWatching();
        } catch (Exception e) {
            Log.e(TAG, "initEapConfig failed!!!");
        }
    }

    private void createDirs(String... dirsPath) {
        for (String dirPath : dirsPath) {
            File dirs = new File(dirPath);
            if (!dirs.exists()) {
                dirs.mkdirs();
            }
        }
    }

    private void setEapDcsPath(String packageName, String tag) {
        if (!isOwnApp(packageName)) {
            this.mEapPath = EAP_PATH;
            this.mDcsPath = EAP_DCS_PATH;
        } else if (tag.contains("app_anr")) {
            this.mEapPath = EAP_CONFIG_MOA_ANR_PATH;
            this.mDcsPath = EAP_CONFIG_MOA_ANR_DCS_PATH;
        } else {
            this.mEapPath = EAP_CONFIG_MOA_CRASH_PATH;
            this.mDcsPath = EAP_CONFIG_MOA_CRASH_DCS_PATH;
        }
        createDirs(this.mEapPath, this.mDcsPath);
        if (this.mDebugSwitch) {
            Log.d(TAG, "log file dest path : " + this.mEapPath + " tag : " + tag + " Dcs path : " + this.mDcsPath);
        }
    }

    private boolean isShouldCollectLogFile(String tag, String packageName) {
        if (!tag.contains("app_crash")) {
            return true;
        }
        if (isOwnApp(packageName)) {
            return this.mOwnJavaCrashLogSwitch;
        }
        return this.mOtherJavaCrashLogSwitch;
    }

    private boolean isOwnApp(String packageName) {
        if (this.mWhitePkgList.contains(packageName)) {
            return true;
        }
        int pkgKeyNum = this.mWhitePkgKeyList.size();
        for (int i = 0; i < pkgKeyNum; i++) {
            if (packageName.startsWith(this.mWhitePkgKeyList.get(i))) {
                return true;
            }
        }
        return false;
    }

    private int getEapFileLength(File eapDir) {
        int fileNum = 0;
        File[] files = eapDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                fileNum += getEapFileLength(file);
            } else {
                fileNum++;
            }
        }
        if (this.mDebugSwitch) {
            Log.i(TAG, eapDir.getName() + " file num is " + fileNum);
        }
        return fileNum;
    }

    private File deleteObsoleteFile(File eapFile, long min) {
        ColorEapUtils colorEapUtils = this;
        File oldFile = null;
        File[] listFiles = eapFile.listFiles();
        int length = listFiles.length;
        int i = 0;
        long min2 = min;
        while (i < length) {
            File file = listFiles[i];
            if (file.isDirectory()) {
                colorEapUtils.deleteObsoleteFile(file, min2);
            } else {
                String name = file.getName();
                int index = name.lastIndexOf(64);
                if (index == -1) {
                    Log.d(TAG, "trimToFit: continue");
                } else {
                    long fileTime = colorEapUtils.dateToStamp(name.substring(index + 1, name.length() - 4));
                    if (fileTime < System.currentTimeMillis() - OppoJunkRecorder.JUNK_RECORD_DELETE_AGE && !colorEapUtils.mIsSpecialVersion) {
                        colorEapUtils.reportDeleteResult(-1, file);
                        Log.d(TAG, "over time, delete: " + file.getName());
                        file.delete();
                    }
                    if (fileTime < min2) {
                        min2 = fileTime;
                        oldFile = file;
                    }
                }
            }
            i++;
            colorEapUtils = this;
        }
        return oldFile;
    }

    private String getVersionName(Context context, String pkgName, int userId) {
        try {
            return context.getPackageManager().getPackageInfoAsUser(pkgName, 0, userId).versionName;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get versionName: " + e.getMessage());
            return "null";
        }
    }

    private void collectCrashInfo(String fileName, String pkgName, String signal, String msg, String trace) {
        String logSwitch = SystemProperties.get(PROP_MM_COMPATIBILITY);
        if (LOG_ENABLE_CRASH.equals(logSwitch) || LOG_ENABLE_ALL.equals(logSwitch)) {
            Log.d(TAG_MM_COMPATIBILITY_CRASH, "{" + "\"crash_tombstone\"" + ":\"" + fileName + "\"," + "\"crash_proc\"" + ":\"" + pkgName + "\"," + "\"crash_signal\"" + ":\"" + signal + "\"," + "\"crash_message\"" + ":\"" + msg + "\"," + "\"crash_trace\"" + ":\"" + trace + "\"" + "}");
        }
    }

    /* access modifiers changed from: private */
    public void collectAnrInfo(String pkgName, ProcessRecord process, String msg, String trace) {
        String logSwitch = SystemProperties.get(PROP_MM_COMPATIBILITY);
        if (LOG_ENABLE_ANR.equals(logSwitch) || LOG_ENABLE_ALL.equals(logSwitch)) {
            int processId = process != null ? process.pid : -1;
            String processName = process != null ? process.processName : "unkown";
            Log.d(TAG_MM_COMPATIBILITY_ANR, "{" + "\"anr_package\"" + ":\"" + pkgName + "\"," + "\"anr_proc_name\"" + ":\"" + processName + "\"," + "\"anr_proc_id\"" + ":\"" + processId + "\"," + "\"anr_message\"" + ":\"" + msg + "\"," + "\"anr_trace\"" + ":\"" + trace + "\"" + "}");
        }
    }
}

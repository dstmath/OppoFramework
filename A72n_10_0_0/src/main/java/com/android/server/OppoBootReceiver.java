package com.android.server;

import android.content.Context;
import android.content.res.OppoThemeResources;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.DropBoxManager;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.OppoManager;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.TimedRemoteCaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import oppo.util.OppoStatistics;

public class OppoBootReceiver {
    public static final int LOG_SIZE = 4194304;
    static final String MINI_DUMP = "/data/system/dropbox/minidump.bin";
    private static final String OCP = "/proc/oppoVersion/ocp";
    private static final String TAG = "OppoBootReceiver";
    private static final String UNKNOW_REBOOT_PFF = "/sys/power/poff_reason";
    private static final String UNKNOW_REBOOT_PON = "/sys/power/pon_reason";
    public final int MSG_UPDATESTATE = 1001;
    private Context mContext;
    Handler mGetStateHandler = new Handler() {
        /* class com.android.server.OppoBootReceiver.AnonymousClass5 */
        int mImeiCounter = 0;

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what != 1001) {
                Slog.v(OppoBootReceiver.TAG, "invalid msg");
            } else {
                updatePhoneState(OppoBootReceiver.this.mContext);
            }
        }

        private void updatePhoneState(Context context) {
            String device = Build.MODEL;
            String androidVer = OppoManager.getVersionFOrAndroid();
            String buildVer = OppoManager.getBuildVersion();
            String recDevice = OppoManager.readCriticalData(OppoManager.TYEP_DEVICE, 512);
            String recAndroidVer = OppoManager.readCriticalData(OppoManager.TYEP_Android_VER, 512);
            String recBuildVer = OppoManager.readCriticalData(OppoManager.TYEP_BUILD_VER, 512);
            if (!device.equals(recDevice)) {
                int res = OppoManager.writeCriticalData(OppoManager.TYEP_DEVICE, device);
                Slog.v(OppoBootReceiver.TAG, "device res = " + res);
            }
            if (!androidVer.equals(recAndroidVer)) {
                int res2 = OppoManager.writeCriticalData(OppoManager.TYEP_Android_VER, androidVer);
                Slog.v(OppoBootReceiver.TAG, "androidver res = " + res2);
            }
            if (!buildVer.equals(recBuildVer)) {
                int res3 = OppoManager.writeCriticalData(OppoManager.TYEP_BUILD_VER, buildVer);
                Slog.v(OppoBootReceiver.TAG, "buildVer res = " + res3);
            }
            String historyVer = OppoManager.readCriticalData(OppoManager.TYEP_BUILD_VER + 1024, 512);
            if (historyVer == null) {
                return;
            }
            if (historyVer.equals("null") || historyVer.isEmpty()) {
                Slog.v(OppoBootReceiver.TAG, "record new vesion to history ");
                OppoManager.writeCriticalData(OppoManager.TYEP_DEVICE + 1024, device);
                OppoManager.writeCriticalData(OppoManager.TYEP_Android_VER + 1024, androidVer);
                OppoManager.writeCriticalData(OppoManager.TYEP_BUILD_VER + 1024, buildVer);
            }
        }
    };
    private final String mLastExceptionProc = "/proc/sys/kernel/hung_task_oppo_kill";
    private final String mLastExceptionProperty = "persist.hungtask.oppo.kill";
    private OppoBootReceiverCallback mOppoBootReceiverCallback = null;

    public interface OppoBootReceiverCallback {
        void onAddFileToDropBox(DropBoxManager dropBoxManager, HashMap<String, Long> hashMap, String str, String str2, int i, String str3) throws IOException;
    }

    public void init(Context context) {
        this.mContext = context;
    }

    public void incrementCriticalDataAndRecordRebootBlocked() {
        Slog.v(TAG, "send delayed message");
        this.mGetStateHandler.sendEmptyMessageDelayed(1001, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
        if (OppoManager.incrementCriticalData(OppoManager.TYPE_REBOOT, this.mContext.getResources().getString(201653512)) == -2) {
            Slog.e(TAG, "increment reboot times failed!!");
        }
        String lastReboot = isRebootExceptionFromBolckException();
        if (lastReboot != null) {
            OppoManager.writeLogToPartition(OppoManager.TYPE_REBOOT_FROM_BLOCKED, lastReboot, OppoManager.ANDROID_TAG, OppoManager.ISSUE_ANDROID_REBOOT_FROM_BLOCKED, this.mContext.getResources().getString(201653517));
            OppoManager.sendQualityDCSEvent(OppoManager.QualityEventId.EV_STABILITY_REBOOT_FROM_BLOCKED, null);
        }
    }

    public void syncCacheToEmmc() {
        try {
            OppoManager.syncCacheToEmmc();
            Slog.v(TAG, "syncCacheToEmmc");
        } catch (Exception e) {
            Slog.e(TAG, "sync criticallog failed e + " + e.toString());
        }
    }

    public void disableBlackMonitor() {
        new Thread() {
            /* class com.android.server.OppoBootReceiver.AnonymousClass1 */

            public void run() {
                while (true) {
                    try {
                        String count = FileUtils.readTextFile(new File("/proc/blackCheckStatus"), 10, null);
                        Slog.i(OppoBootReceiver.TAG, "/proc/blackCheckStatus shows " + count);
                        Map<String, String> logMap = new HashMap<>();
                        logMap.put("count", count);
                        OppoStatistics.onCommon(OppoBootReceiver.this.mContext, "20120", "black_screen_monitor", logMap, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        SystemClock.sleep(60000);
                    }
                }
            }
        }.start();
        new Thread() {
            /* class com.android.server.OppoBootReceiver.AnonymousClass2 */

            public void run() {
                while (true) {
                    try {
                        String count = FileUtils.readTextFile(new File("/proc/brightCheckStatus"), 10, null);
                        Slog.i(OppoBootReceiver.TAG, "/proc/brightCheckStatus  shows " + count);
                        if (!WifiEnterpriseConfig.ENGINE_DISABLE.equals(count)) {
                            Map<String, String> logMap = new HashMap<>();
                            logMap.put("count", count);
                            OppoStatistics.onCommon(OppoBootReceiver.this.mContext, "20120", "bright_screen_monitor", logMap, false);
                        } else {
                            SystemClock.sleep(60000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        SystemClock.sleep(60000);
                    }
                }
            }
        }.start();
    }

    public String getOTAVersionString() {
        return SystemProperties.get("ro.build.version.ota", "");
    }

    public void printIsPanic() {
        boolean isPanic = isKernelPanic();
        Slog.d(TAG, "Aha! Boot reason is kernel panic " + isPanic + "!!!");
    }

    public String setLastKmsgFooter() {
        if (isKernelPanic()) {
            StringBuilder sb = new StringBuilder(512);
            sb.append("\n");
            sb.append("Boot info:\n");
            sb.append("Last boot reason: ");
            sb.append(OppoManager.ISSUE_KERNEL_PANIC);
            sb.append("\n");
            return sb.toString();
        }
        StringBuilder sb2 = new StringBuilder(512);
        sb2.append("\n");
        sb2.append("Boot info:\n");
        sb2.append("Last boot reason: ");
        sb2.append("normal");
        sb2.append("\n");
        return sb2.toString();
    }

    public void writeLogToPartitionAndDeleteFolderFilesThread(DropBoxManager db, HashMap<String, Long> hashMap, String headers) throws IOException {
        String str;
        String str2;
        String str3;
        String rebootValue;
        Exception e;
        final boolean isPanic = isKernelPanic();
        if (isPanic) {
            OppoManager.writeLogToPartition(OppoManager.TYPE_PANIC, "kernel_panic", OppoManager.KERNEL_TAG, OppoManager.ISSUE_KERNEL_PANIC, this.mContext.getResources().getString(201653515));
            Map<String, String> logMap = new HashMap<>();
            logMap.put("reason", "kernel panic");
            OppoManager.onStamp(OppoManager.StampId.AD_KE, logMap);
            OppoManager.sendQualityDCSEvent(OppoManager.QualityEventId.EV_STABILITY_KERNEL_PANIC, null);
        } else {
            String lastReboot = isRebootExceptionFromBolckException();
            boolean release_version = SystemProperties.getBoolean("ro.build.release_type", false);
            if (!(lastReboot == null || !release_version || db == null)) {
                db.addText(OppoManager.KERNEL_PANIC_TAG, "Kernel reboot from Block Exception!  lastReboot = " + lastReboot);
            }
        }
        SystemProperties.set("persist.sys.systemserver.pid", Integer.toString(Process.myPid()));
        final String rebootValue2 = SystemProperties.get("persist.sys.oppo.reboot", "");
        final String fatalValue = SystemProperties.get("persist.sys.oppo.fatal", "");
        final String firstValue = SystemProperties.get("oppo.device.firstboot", "");
        Slog.v(TAG, "persist.sys.oppo.reboot = " + rebootValue2);
        Slog.v(TAG, "persist.sys.oppo.fatal = " + fatalValue);
        Slog.v(TAG, "oppo.device.firstboot = " + firstValue);
        int type = OppoBootAeeLogUtil.checkMtkHwtState(this.mContext);
        if (isPanic || type != -1) {
            str2 = WifiEnterpriseConfig.ENGINE_ENABLE;
            str = "persist.sys.oppo.fb_upgraded";
        } else if (!fatalValue.equals(WifiEnterpriseConfig.ENGINE_ENABLE)) {
            str2 = WifiEnterpriseConfig.ENGINE_ENABLE;
            str = "persist.sys.oppo.fb_upgraded";
        } else if (firstValue.equals(WifiEnterpriseConfig.ENGINE_ENABLE)) {
            str2 = WifiEnterpriseConfig.ENGINE_ENABLE;
            str = "persist.sys.oppo.fb_upgraded";
        } else if (rebootValue2.equals("normal")) {
            str2 = WifiEnterpriseConfig.ENGINE_ENABLE;
            str = "persist.sys.oppo.fb_upgraded";
        } else if (db == null) {
            str2 = WifiEnterpriseConfig.ENGINE_ENABLE;
            str = "persist.sys.oppo.fb_upgraded";
        } else if (SystemProperties.get("persist.sys.oppo.fb_upgraded", "").equals(WifiEnterpriseConfig.ENGINE_ENABLE)) {
            int i = OppoManager.TYPE_ANDROID_UNKNOWN_REBOOT;
            str2 = WifiEnterpriseConfig.ENGINE_ENABLE;
            str = "persist.sys.oppo.fb_upgraded";
            OppoManager.writeLogToPartition(i, "unknow reboot", OppoManager.KERNEL_TAG, OppoManager.ISSUE_KERNEL_PANIC, this.mContext.getResources().getString(201653521));
            OppoManager.sendQualityDCSEvent(OppoManager.QualityEventId.EV_STABILITY_UNKNOWN_REBOOT, null);
        } else {
            str2 = WifiEnterpriseConfig.ENGINE_ENABLE;
            str = "persist.sys.oppo.fb_upgraded";
        }
        try {
            str3 = str;
            rebootValue = TAG;
            try {
                new Thread(new Runnable() {
                    /* class com.android.server.OppoBootReceiver.AnonymousClass3 */

                    public void run() {
                        if (OppoBootReceiver.isMtkPlatform()) {
                            OppoBootReceiver.this.readUnknowRebootStatusforMTK();
                            if (!WifiEnterpriseConfig.ENGINE_ENABLE.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"))) {
                                OppoBootAeeLogUtil.prepareMtkLog(false, null);
                            }
                        } else {
                            OppoBootReceiver.this.readUnknowRebootStatus(isPanic, rebootValue2, fatalValue, firstValue);
                        }
                        OppoBootReceiver.this.checkPwkSt();
                        OppoBootReceiver.this.updateDeviceInfo();
                    }
                }).start();
            } catch (Exception e2) {
                e = e2;
            }
        } catch (Exception e3) {
            e = e3;
            str3 = str;
            rebootValue = TAG;
            Slog.e(rebootValue, "read pmic  e: " + e.toString());
            SystemProperties.set("persist.sys.oppo.reboot", "");
            SystemProperties.set("persist.sys.oppo.fatal", "");
            deleteFolderFilesThread("data/oppo_log/junk_logs/kernel");
            deleteFolderFilesThread("data/oppo_log/junk_logs/ftrace");
            SystemProperties.set(str3, str2);
        }
        SystemProperties.set("persist.sys.oppo.reboot", "");
        SystemProperties.set("persist.sys.oppo.fatal", "");
        deleteFolderFilesThread("data/oppo_log/junk_logs/kernel");
        deleteFolderFilesThread("data/oppo_log/junk_logs/ftrace");
        SystemProperties.set(str3, str2);
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0148  */
    public void addFile(DropBoxManager db, HashMap<String, Long> timestamps, final String headers, Context ctx) throws IOException {
        Exception e;
        String lastSystemReboot = isLastSystemServerRebootFormBolckException();
        final int system_server_current_pid = Process.myPid();
        int system_server_previous_pid = SystemProperties.getInt("persist.sys.systemserver.pid", -1);
        if (system_server_current_pid == system_server_previous_pid) {
            Slog.e(TAG, "system_server_current_pid == system_server_previous_pid");
            return;
        }
        if (isMtkPlatform()) {
            if (db != null) {
                db.addText(OppoManager.ANDROID_PANIC_TAG, headers);
            }
            try {
                try {
                    new Thread(new Runnable() {
                        /* class com.android.server.OppoBootReceiver.AnonymousClass4 */

                        public void run() {
                            if (!WifiEnterpriseConfig.ENGINE_ENABLE.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"))) {
                                OppoBootAeeLogUtil.prepareMtkLog(true, headers);
                            }
                            SystemProperties.set("persist.sys.systemserver.pid", Integer.toString(system_server_current_pid));
                        }
                    }).start();
                } catch (Exception e2) {
                    e = e2;
                }
            } catch (Exception e3) {
                e = e3;
                Slog.e(TAG, "addFile prepareMtkLog  e: " + e.toString());
                if (lastSystemReboot != null) {
                }
                Map<String, String> logMap = new HashMap<>();
                logMap.put("reason", "system server crash");
                OppoManager.onStamp(OppoManager.StampId.AD_JE, logMap);
            }
        } else {
            String systemcrashFile = SystemProperties.get("persist.sys.panic.file", "");
            Slog.e(TAG, "system_server crashed! --- systemcrashFile =" + systemcrashFile);
            File sysFile = new File(systemcrashFile);
            if (sysFile.exists()) {
                SystemProperties.set("persist.sys.send.file", systemcrashFile);
                if (sysFile.getName().startsWith("system_server_watchdog", 0)) {
                    requestAddFileToDropBox(db, timestamps, headers, systemcrashFile, 4194304, "SYSTEM_SERVER_WATCHDOG");
                } else if (sysFile.getName().startsWith("tombstone", 0)) {
                    requestAddFileToDropBox(db, timestamps, headers, systemcrashFile, 4194304, "SYSTEM_TOMBSTONE_CRASH");
                } else if (!systemcrashFile.endsWith(".gz")) {
                    requestAddFileToDropBox(db, timestamps, headers, systemcrashFile, 4194304, OppoManager.ANDROID_PANIC_TAG);
                } else if (db != null) {
                    db.addText("SYSTEM_SERVER_GZ", "LOG FOR GZ");
                }
            } else {
                dumpEnvironment();
                if (lastSystemReboot != null) {
                    if (db != null) {
                        db.addText(OppoManager.ANDROID_PANIC_TAG, headers + "system_Server reboot from Block Exception! system_server_current_pid = " + system_server_current_pid + ",system_server_previous_pid = " + system_server_previous_pid + " lastSystemReboot = " + lastSystemReboot);
                    }
                } else if (db != null) {
                    db.addText(OppoManager.ANDROID_PANIC_TAG, headers + "system_Server crash but can not get efficacious log! system_server_current_pid = " + system_server_current_pid + ",system_server_previous_pid = " + system_server_previous_pid);
                }
            }
            SystemProperties.set("persist.sys.systemserver.pid", Integer.toString(system_server_current_pid));
        }
        if (lastSystemReboot != null) {
            OppoManager.writeLogToPartition(OppoManager.TYPE_ANDROID_SYSTEM_REBOOT_FROM_BLOCKED, lastSystemReboot, OppoManager.ANDROID_TAG, OppoManager.ISSUE_ANDROID_REBOOT_FROM_BLOCKED, ctx.getResources().getString(201653518));
            OppoManager.sendQualityDCSEvent(OppoManager.QualityEventId.EV_STABILITY_ANDROID_REBOOT_FROM_BLOCKED, null);
        }
        Map<String, String> logMap2 = new HashMap<>();
        logMap2.put("reason", "system server crash");
        OppoManager.onStamp(OppoManager.StampId.AD_JE, logMap2);
    }

    public void resetPanicFile() {
        SystemProperties.set("persist.sys.panic.file", "");
    }

    private static boolean isKernelPanic() {
        if (!isMtkPlatform()) {
            return "true".equals(SystemProperties.get("sys.oppo.panic", "false"));
        }
        String bootReason = SystemProperties.get("ro.boot.bootreason", null);
        return "kernel_panic".equals(bootReason) || "modem".equals(bootReason);
    }

    static void resetPanicState() {
        SystemProperties.set("sys.oppo.panic", "false");
        SystemProperties.set("persist.sys.oppo.fb_upgraded", WifiEnterpriseConfig.ENGINE_ENABLE);
    }

    /* access modifiers changed from: private */
    public static boolean isMtkPlatform() {
        return SystemProperties.get("ro.board.platform", OppoThemeResources.OPPO_PACKAGE).toLowerCase().startsWith("mt");
    }

    private void dumpEnvironment() {
        SystemProperties.set("sys.dumpenvironment.finished", WifiEnterpriseConfig.ENGINE_DISABLE);
        SystemProperties.set("ctl.start", "dumpenvironment");
        long begin = SystemClock.elapsedRealtime();
        while (SystemProperties.getInt("sys.dumpenvironment.finished", 0) != 1 && SystemClock.elapsedRealtime() - begin < ((long) SystemProperties.getInt("ro.dumpenvironment.time", 4000))) {
            SystemClock.sleep(100);
        }
    }

    private String isLastSystemServerRebootFormBolckException() {
        if (!new File("/proc/sys/kernel/hung_task_oppo_kill").exists()) {
            Slog.v(TAG, "reboot file is not exists");
            return null;
        }
        try {
            BufferedReader in = new BufferedReader(new FileReader("/proc/sys/kernel/hung_task_oppo_kill"));
            String strSend = in.readLine();
            in.close();
            if (strSend == null || strSend.trim().isEmpty()) {
                return null;
            }
            return strSend;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String isRebootExceptionFromBolckException() {
        String strSend = SystemProperties.get("persist.hungtask.oppo.kill");
        if (strSend == null || strSend.isEmpty()) {
            return null;
        }
        return strSend;
    }

    /* JADX INFO: Multiple debug info for r15v5 byte[]: [D('lengthPff' int), D('bufferOcp' byte[])] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String readUnknowRebootStatus(boolean isPanic, String reboot, String fatal, String firstBoot) {
        String str;
        String res;
        Exception e;
        FileInputStream finPff;
        FileInputStream ocpFile;
        byte[] bufferPon;
        byte[] bufferPff;
        byte[] bufferOcp;
        int ponHigh;
        int pffHigh;
        int ocpHigh;
        Map<String, String> logQualMap;
        try {
            FileInputStream finPon = new FileInputStream(UNKNOW_REBOOT_PON);
            String res2 = "";
            try {
                finPff = new FileInputStream(UNKNOW_REBOOT_PFF);
                ocpFile = new FileInputStream(OCP);
            } catch (Exception e2) {
                e = e2;
                str = TAG;
                res = res2;
                Slog.v(str, "readUnknowRebootStatus:" + e.toString());
                e.printStackTrace();
                return res;
            }
            try {
                bufferPon = new byte[finPon.available()];
                bufferPff = new byte[finPon.available()];
                bufferOcp = new byte[30];
                finPon.read(bufferPon);
                finPff.read(bufferPff);
                ocpFile.read(bufferOcp);
                ponHigh = 0;
                for (byte b : bufferPon) {
                    try {
                        if (b == 0) {
                            break;
                        }
                        ponHigh++;
                    } catch (Exception e3) {
                        e = e3;
                        str = TAG;
                        res = res2;
                        Slog.v(str, "readUnknowRebootStatus:" + e.toString());
                        e.printStackTrace();
                        return res;
                    }
                }
                int length = bufferPff.length;
                int i = 0;
                pffHigh = 0;
                while (i < length && bufferPff[i] != 0) {
                    pffHigh++;
                    i++;
                }
                int length2 = bufferOcp.length;
                int i2 = 0;
                ocpHigh = 0;
                while (i2 < length2 && bufferOcp[i2] != 0) {
                    ocpHigh++;
                    i2++;
                }
                Slog.v(TAG, "ponHigh = " + ponHigh + " pffHigh=" + pffHigh + " ocpHigh = " + ocpHigh);
                StringBuffer sb = new StringBuffer();
                StringBuilder sb2 = new StringBuilder();
                sb2.append(new String(bufferPon, 0, ponHigh).trim());
                sb2.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                sb2.append(new String(bufferPff, 0, pffHigh).trim());
                sb.append(sb2.toString());
                res = sb.toString();
            } catch (Exception e4) {
                e = e4;
                str = TAG;
                res = res2;
                Slog.v(str, "readUnknowRebootStatus:" + e.toString());
                e.printStackTrace();
                return res;
            }
            try {
                finPon.close();
                finPff.close();
                ocpFile.close();
                String poffString = new String(bufferPff, 0, pffHigh).trim();
                String ponString = new String(bufferPon, 0, ponHigh).trim();
                String ocpString = new String(bufferOcp, 0, ocpHigh).trim();
                res2 = res;
                String poffCode = poffString.substring(poffString.indexOf(91) + 1, poffString.indexOf(93));
                String ponCode = ponString.substring(ponString.indexOf(91) + 1, ponString.indexOf(93));
                Slog.v(TAG, "poffCode = " + poffCode + " ponCode " + ponCode + " ocp = " + ocpString);
                if (!"ocp: 0 0 0 0".equals(ocpString) && !"ocp: 0 0x0 0 0x0".equals(ocpString)) {
                    Map<String, String> logMap = new HashMap<>();
                    logMap.put("reason", ocpString);
                    OppoManager.onStamp(OppoManager.StampId.AD_OCP, logMap);
                }
                Map<String, String> logMap2 = new HashMap<>();
                logMap2.put("logType", "21");
                logMap2.put("module", "Android");
                logMap2.put("poff", poffString);
                logMap2.put("pon", ponString);
                logMap2.put("poffCode", poffCode);
                logMap2.put("ponCode", ponCode);
                logMap2.put("otaVersion", SystemProperties.get("ro.build.version.ota", ""));
                logMap2.put("issue", "reboot");
                logMap2.put("count", WifiEnterpriseConfig.ENGINE_ENABLE);
                logMap2.put("ocp", ocpString);
                logMap2.put("isPanic", Boolean.toString(isPanic));
                try {
                    logMap2.put("reboot", reboot);
                    str = TAG;
                } catch (Exception e5) {
                    e = e5;
                    str = TAG;
                    res = res2;
                    Slog.v(str, "readUnknowRebootStatus:" + e.toString());
                    e.printStackTrace();
                    return res;
                }
                try {
                    logMap2.put("fatal", fatal);
                    try {
                        logMap2.put("firstBoot", firstBoot);
                        try {
                            try {
                                OppoStatistics.onCommon(this.mContext, "CriticalLog", "reboot", logMap2, false);
                                logQualMap = new HashMap<>();
                                logQualMap.put("poff", poffString);
                                logQualMap.put("pon", ponString);
                                logQualMap.put("poffCode", poffCode);
                                logQualMap.put("ponCode", ponCode);
                                logQualMap.put("ocp", ocpString);
                                logQualMap.put("isPanic", Boolean.toString(isPanic));
                                try {
                                    logQualMap.put("reboot", reboot);
                                } catch (Exception e6) {
                                    e = e6;
                                    res = res2;
                                    Slog.v(str, "readUnknowRebootStatus:" + e.toString());
                                    e.printStackTrace();
                                    return res;
                                }
                            } catch (Exception e7) {
                                e = e7;
                                res = res2;
                                Slog.v(str, "readUnknowRebootStatus:" + e.toString());
                                e.printStackTrace();
                                return res;
                            }
                        } catch (Exception e8) {
                            e = e8;
                            res = res2;
                            Slog.v(str, "readUnknowRebootStatus:" + e.toString());
                            e.printStackTrace();
                            return res;
                        }
                    } catch (Exception e9) {
                        e = e9;
                        res = res2;
                        Slog.v(str, "readUnknowRebootStatus:" + e.toString());
                        e.printStackTrace();
                        return res;
                    }
                    try {
                        logQualMap.put("fatal", fatal);
                        try {
                            logQualMap.put("firstBoot", firstBoot);
                            OppoManager.sendQualityDCSEvent(OppoManager.QualityEventId.EV_STABILITY_REBOOT, logQualMap);
                            return res2;
                        } catch (Exception e10) {
                            e = e10;
                        }
                    } catch (Exception e11) {
                        e = e11;
                        res = res2;
                        Slog.v(str, "readUnknowRebootStatus:" + e.toString());
                        e.printStackTrace();
                        return res;
                    }
                } catch (Exception e12) {
                    e = e12;
                    res = res2;
                    Slog.v(str, "readUnknowRebootStatus:" + e.toString());
                    e.printStackTrace();
                    return res;
                }
            } catch (Exception e13) {
                e = e13;
                str = TAG;
                Slog.v(str, "readUnknowRebootStatus:" + e.toString());
                e.printStackTrace();
                return res;
            }
        } catch (Exception e14) {
            e = e14;
            str = TAG;
            res = "";
            Slog.v(str, "readUnknowRebootStatus:" + e.toString());
            e.printStackTrace();
            return res;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String readUnknowRebootStatusforMTK() {
        try {
            Map<String, String> logMap = new HashMap<>();
            logMap.put("logType", "21");
            logMap.put("module", "Android");
            logMap.put("otaVersion", SystemProperties.get("ro.build.version.ota", ""));
            logMap.put("issue", "reboot");
            logMap.put("count", WifiEnterpriseConfig.ENGINE_ENABLE);
            OppoStatistics.onCommon(this.mContext, "CriticalLog", "reboot", logMap, false);
            OppoManager.sendQualityDCSEvent(OppoManager.QualityEventId.EV_STABILITY_REBOOT, null);
        } catch (Exception e) {
            Slog.v(TAG, "readUnknowRebootStatusforMTK:" + e.toString());
            e.printStackTrace();
        }
        return "";
    }

    private void deleteFolderFilesThread(final String path) {
        try {
            new Thread(new Runnable() {
                /* class com.android.server.OppoBootReceiver.AnonymousClass6 */

                public void run() {
                    File folder = new File(path);
                    if (!folder.exists() || !folder.isDirectory()) {
                        Slog.v(OppoBootReceiver.TAG, "" + path + " not exists");
                        return;
                    }
                    File[] files = folder.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.exists() && f.isFile()) {
                                f.delete();
                            }
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            Slog.e(TAG, "deleteFolderFilesThread e: " + e.toString());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkPwkSt() {
        String pwkts = SystemProperties.get("persist.sys.oppo.longpwkts");
        if (pwkts != null && !pwkts.isEmpty()) {
            Map<String, String> logMap2 = new HashMap<>();
            logMap2.put("pwkts", pwkts);
            OppoManager.onStamp(OppoManager.StampId.AD_BATTERYOFF, logMap2);
            SystemProperties.set("persist.sys.oppo.longpwkts", "");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDeviceInfo() {
        OppoManager.onDeleteStampId(OppoManager.StampId.AD_DEVICE);
        Map<String, String> logMap = new HashMap<>();
        String model = SystemProperties.get("ro.product.model", "null");
        String oppoID = getOppoID();
        String otaVersion = SystemProperties.get("ro.build.version.ota", "null");
        String androidVer = SystemProperties.get("ro.product.androidver", "null");
        String root = "no";
        if (!"enforcing".equals(SystemProperties.get("ro.boot.veritymode", ""))) {
            root = "yes";
        }
        logMap.put("modle", model);
        logMap.put("oppoID", oppoID);
        logMap.put("otaVersion", otaVersion);
        logMap.put("androidVer", androidVer);
        logMap.put("memInfo", "memInfo");
        logMap.put("root", root);
        OppoManager.onStamp(OppoManager.StampId.AD_DEVICE, logMap);
    }

    private String getOppoID() {
        Object result;
        try {
            Class<?> tClass = Class.forName("com.android.id.impl.IdProviderImpl");
            Object tIdProivderImpl = tClass.newInstance();
            Method tGetGUID = tClass.getMethod("getGUID", Context.class);
            if (tIdProivderImpl == null || tGetGUID == null || (result = tGetGUID.invoke(tIdProivderImpl, this.mContext)) == null) {
                return "";
            }
            return (String) result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setOppoBootReceiverCallback(OppoBootReceiverCallback callback) {
        this.mOppoBootReceiverCallback = callback;
    }

    private void requestAddFileToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String filename, int maxSize, String tag) {
        OppoBootReceiverCallback oppoBootReceiverCallback = this.mOppoBootReceiverCallback;
        if (oppoBootReceiverCallback != null) {
            try {
                oppoBootReceiverCallback.onAddFileToDropBox(db, timestamps, headers, filename, maxSize, tag);
            } catch (Exception e) {
                Slog.e(TAG, "requestAddFileToDropBox failed!", e);
            }
        } else {
            Slog.e(TAG, "requestAddFileToDropBox failed for callback uninit!");
        }
    }
}

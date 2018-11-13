package com.android.server;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager.Stub;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.OppoManager;
import android.os.Process;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Downloads;
import android.provider.SettingsStringUtil;
import android.system.ErrnoException;
import android.system.Os;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.TimedRemoteCaller;
import android.util.Xml;
import com.android.internal.R;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.oppo.debug.ASSERT;
import com.oppo.ota.OppoOtaUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class BootReceiver extends BroadcastReceiver {
    private static final String BOOT_REASON_FILE = "/sys/power/app_boot";
    private static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', DateFormat.AM_PM, 'b', 'c', DateFormat.DATE, 'e', 'f', 'g', DateFormat.HOUR, 'i', 'j', DateFormat.HOUR_OF_DAY, 'l', DateFormat.MINUTE, 'n', 'o', 'p', 'q', 'r', DateFormat.SECONDS, 't', 'u', 'v', 'w', StateProperty.TARGET_X, 'y', DateFormat.TIME_ZONE};
    private static final String FSCK_FS_MODIFIED = "FILE SYSTEM WAS MODIFIED";
    private static final String FSCK_PASS_PATTERN = "Pass ([1-9]E?):";
    private static final String FSCK_TREE_OPTIMIZATION_PATTERN = "Inode [0-9]+ extent tree.*could be shorter";
    private static final int FS_STAT_FS_FIXED = 1024;
    private static final String FS_STAT_PATTERN = "fs_stat,[^,]*/([^/,]+),(0x[0-9a-fA-F]+)";
    private static final int FS_STAT_UNCLEAN_SHUTDOWN = 8;
    private static final String LAST_HEADER_FILE = "last-header.txt";
    private static final String[] LAST_KMSG_FILES = new String[]{"/sys/fs/pstore/console-ramoops", "/proc/last_kmsg"};
    private static final String LAST_SHUTDOWN_TIME_PATTERN = "powerctl_shutdown_time_ms:([0-9]+):([0-9]+)";
    private static final String LOG_FILES_FILE = "log-files.xml";
    private static final int LOG_SIZE = 2097152;
    static final String MINI_DUMP = "/data/system/dropbox/minidump.bin";
    private static final String[] MOUNT_DURATION_PROPS_POSTFIX = new String[]{"early", PhoneConstants.APN_TYPE_DEFAULT, "late"};
    private static final String OLD_UPDATER_CLASS = "com.google.android.systemupdater.SystemUpdateReceiver";
    private static final String OLD_UPDATER_PACKAGE = "com.google.android.systemupdater";
    private static final String SHUTDOWN_METRICS_FILE = "/data/system/shutdown-metrics.txt";
    private static final String SHUTDOWN_TRON_METRICS_PREFIX = "shutdown_";
    private static final String TAG = "BootReceiver";
    private static final File TOMBSTONE_DIR = new File("/data/tombstones");
    private static final int UMOUNT_STATUS_NOT_AVAILABLE = 4;
    private static final String UNKNOW_REBOOT_PFF = "/sys/power/poff_reason";
    private static final String UNKNOW_REBOOT_PON = "/sys/power/pon_reason";
    private static final char[] UPPER_CASE_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', DateFormat.CAPITAL_AM_PM, 'B', 'C', 'D', DateFormat.DAY, 'F', 'G', 'H', 'I', 'J', 'K', DateFormat.STANDALONE_MONTH, DateFormat.MONTH, PhoneNumberUtils.WILD, 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final File lastHeaderFile = new File(Environment.getDataSystemDirectory(), LAST_HEADER_FILE);
    private static final AtomicFile sFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), LOG_FILES_FILE));
    private static FileObserver sTombstoneObserver = null;
    public final int MSG_UPDATEIMEI = 1002;
    public final int MSG_UPDATESTATE = 1001;
    private Context mContext;
    Handler mGetStateHandler = new Handler() {
        int mImeiCounter = 0;

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    updatePhoneState(BootReceiver.this.mContext);
                    return;
                case 1002:
                    this.mImeiCounter++;
                    Slog.v(BootReceiver.TAG, "in handleMessage MSG_UPDATEIMEI " + this.mImeiCounter);
                    if (!updateIMEI(BootReceiver.this.mContext)) {
                        sendEmptyMessageDelayed(1002, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
                        return;
                    }
                    return;
                default:
                    Slog.v(BootReceiver.TAG, "invalid msg");
                    return;
            }
        }

        private void updatePhoneState(Context context) {
            String device = Build.DEVICE;
            String androidVer = OppoManager.getVersionFOrAndroid();
            String IMEI = OppoManager.getIMEINums(context);
            String buildVer = OppoManager.getBuildVersion();
            String recDevice = OppoManager.readCriticalData(OppoManager.TYEP_DEVICE, 512);
            String recAndroidVer = OppoManager.readCriticalData(OppoManager.TYEP_Android_VER, 512);
            String recIMEI = OppoManager.readCriticalData(OppoManager.TYEP_PHONE_IMEI, 512);
            String recBuildVer = OppoManager.readCriticalData(OppoManager.TYEP_BUILD_VER, 512);
            Slog.v(BootReceiver.TAG, "record device is " + recDevice + " androidVer = " + recAndroidVer + " imei = " + recIMEI + " build = " + recBuildVer);
            if (!device.equals(recDevice)) {
                Slog.v(BootReceiver.TAG, "device res = " + OppoManager.writeCriticalData(OppoManager.TYEP_DEVICE, device));
            }
            if (!androidVer.equals(recAndroidVer)) {
                Slog.v(BootReceiver.TAG, "androidver res = " + OppoManager.writeCriticalData(OppoManager.TYEP_Android_VER, androidVer));
            }
            if (IMEI.equals("null")) {
                sendEmptyMessageDelayed(1002, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
            } else if (!IMEI.equals(recIMEI)) {
                Slog.v(BootReceiver.TAG, "imei res = " + OppoManager.writeCriticalData(OppoManager.TYEP_PHONE_IMEI, IMEI));
            }
            if (!buildVer.equals(recBuildVer)) {
                Slog.v(BootReceiver.TAG, "buildVer res = " + OppoManager.writeCriticalData(OppoManager.TYEP_BUILD_VER, buildVer));
            }
            String historyVer = OppoManager.readCriticalData(OppoManager.TYEP_BUILD_VER + 1024, 512);
            if (historyVer == null) {
                return;
            }
            if (historyVer.equals("null") || historyVer.isEmpty()) {
                Slog.v(BootReceiver.TAG, "record new vesion to history ");
                OppoManager.writeCriticalData(OppoManager.TYEP_DEVICE + 1024, device);
                OppoManager.writeCriticalData(OppoManager.TYEP_Android_VER + 1024, androidVer);
                OppoManager.writeCriticalData(OppoManager.TYEP_PHONE_IMEI + 1024, IMEI);
                OppoManager.writeCriticalData(OppoManager.TYEP_BUILD_VER + 1024, buildVer);
            }
        }

        boolean updateIMEI(Context context) {
            String IMEI = OppoManager.getIMEINums(context);
            String recIMEI = OppoManager.readCriticalData(OppoManager.TYEP_PHONE_IMEI, 512);
            if (IMEI.equals("null") && this.mImeiCounter <= 5) {
                return false;
            }
            if (this.mImeiCounter > 5) {
                if ("".equals(recIMEI)) {
                    int res = OppoManager.writeCriticalData(OppoManager.TYEP_PHONE_IMEI, IMEI);
                    Slog.v(BootReceiver.TAG, "imei record imie  " + IMEI);
                    this.mImeiCounter = 0;
                    return true;
                }
            } else if (!("null".equals(IMEI) || (IMEI.equals(recIMEI) ^ 1) == 0)) {
                Slog.v(BootReceiver.TAG, "imei res = " + OppoManager.writeCriticalData(OppoManager.TYEP_PHONE_IMEI, IMEI));
                this.mImeiCounter = 0;
                return true;
            }
            return true;
        }
    };
    private final String mLastExceptionProc = "/proc/sys/kernel/hung_task_oppo_kill";
    private final String mLastExceptionProperty = "persist.hungtask.oppo.kill";

    public void onReceive(final Context context, Intent intent) {
        this.mContext = context;
        if (!"trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt"))) {
            new Thread() {
                public void run() {
                    Slog.v(BootReceiver.TAG, "send delayed message");
                    BootReceiver.this.mGetStateHandler.sendEmptyMessageDelayed(1001, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
                    if (OppoManager.incrementCriticalData(OppoManager.TYPE_REBOOT, BootReceiver.this.mContext.getResources().getString(R.string.type_issue_reboot)) == -2) {
                        Slog.e(BootReceiver.TAG, "increment reboot times failed!!");
                    }
                    String lastReboot = BootReceiver.this.isRebootExceptionFromBolckException();
                    if (lastReboot != null) {
                        OppoManager.writeLogToPartition(OppoManager.TYPE_REBOOT_FROM_BLOCKED, lastReboot, "ANDROID", "reboot_from_blocked", BootReceiver.this.mContext.getResources().getString(R.string.type_issue_reboot_blocked));
                    }
                    BootReceiver.this.writeSpecialNVtoData(context);
                    OppoOtaUtils.notifyOTAUpdateResult(context);
                    try {
                        BootReceiver.this.logBootEvents(context);
                    } catch (Exception e) {
                        Slog.e(BootReceiver.TAG, "Can't log boot events", e);
                    }
                    boolean onlyCore = false;
                    try {
                        onlyCore = Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
                    } catch (RemoteException e2) {
                    }
                    if (!onlyCore) {
                        try {
                            BootReceiver.this.removeOldUpdatePackages(context);
                        } catch (Exception e3) {
                            Slog.e(BootReceiver.TAG, "Can't remove old update packages", e3);
                        }
                    }
                    try {
                        OppoManager.syncCacheToEmmc();
                        Slog.v(BootReceiver.TAG, "syncCacheToEmmc");
                    } catch (Exception e32) {
                        Slog.e(BootReceiver.TAG, "sync criticallog failed e + " + e32.toString());
                    }
                }
            }.start();
        }
    }

    public static String bytesToHexString(byte[] bytes, boolean upperCase) {
        char[] digits = upperCase ? UPPER_CASE_DIGITS : DIGITS;
        char[] buf = new char[(bytes.length * 2)];
        int c = 0;
        for (byte b : bytes) {
            int i = c + 1;
            buf[c] = digits[(b >> 4) & 15];
            c = i + 1;
            buf[i] = digits[b & 15];
        }
        return new String(buf);
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x001d  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x003e A:{SYNTHETIC, Splitter: B:23:0x003e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String getEngresult(String path) {
        IOException e;
        Throwable th;
        String prop = "00";
        String result = "00";
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(path));
            try {
                prop = reader2.readLine();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                reader = reader2;
            } catch (IOException e3) {
                e2 = e3;
                reader = reader2;
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e2 = e4;
            try {
                e2.printStackTrace();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
                if (prop == null) {
                }
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
                throw th;
            }
        }
        if (prop == null) {
            return bytesToHexString(prop.getBytes(), true);
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x01c2 A:{SYNTHETIC, Splitter: B:44:0x01c2} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x01c7 A:{Catch:{ IOException -> 0x01cb }} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x01c2 A:{SYNTHETIC, Splitter: B:44:0x01c2} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x01c7 A:{Catch:{ IOException -> 0x01cb }} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x01d3 A:{SYNTHETIC, Splitter: B:52:0x01d3} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01d8 A:{Catch:{ IOException -> 0x01dc }} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x01d3 A:{SYNTHETIC, Splitter: B:52:0x01d3} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01d8 A:{Catch:{ IOException -> 0x01dc }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeSpecialNVtoData(Context context) {
        IOException e;
        Throwable th;
        if (SystemProperties.getInt("oppo.device.firstboot", 0) == 0) {
            Slog.d(TAG, "writeSpecialNVtoData not firstboot return");
            return;
        }
        Slog.d(TAG, "writeSpecialNVtoData Entry");
        String productCmd = "/data/system/indicate";
        String carrierName = SystemProperties.get("sys.oppo.carrier_version", "null");
        String mmiResult = getEngresult("/persist/engineermode/ENG_RESULT");
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        String address = bluetooth.getAddress() == null ? "00" : bluetooth.getAddress();
        WifiInfo wifiInfo = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo();
        String macAddress = wifiInfo == null ? "00" : wifiInfo.getMacAddress();
        String pcba = SystemProperties.get("gsm.serial", "null");
        String imeicache = SystemProperties.get("oppo.device.imeicache", "null");
        Slog.d(TAG, "imeicache: " + imeicache);
        String[] imei = imeicache.split(",");
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            BufferedWriter bw2;
            FileWriter fw2 = new FileWriter(productCmd, true);
            try {
                bw2 = new BufferedWriter(fw2);
            } catch (IOException e2) {
                e = e2;
                fw = fw2;
                try {
                    e.printStackTrace();
                    if (bw != null) {
                    }
                    if (fw != null) {
                    }
                    Os.chmod(productCmd, 438);
                    Slog.d(TAG, "writeSpecialNVtoData Exit");
                } catch (Throwable th2) {
                    th = th2;
                    if (bw != null) {
                    }
                    if (fw != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fw = fw2;
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                        throw th;
                    }
                }
                if (fw != null) {
                    fw.close();
                }
                throw th;
            }
            try {
                bw2.write("WIFI: " + macAddress);
                bw2.newLine();
                bw2.write("BT: " + address);
                bw2.newLine();
                bw2.write("PCBA: " + pcba);
                bw2.newLine();
                bw2.write("MMI: " + mmiResult);
                bw2.newLine();
                bw2.write("Carrier: " + carrierName);
                if (imei != null) {
                    if (imei.length > 0) {
                        bw2.newLine();
                        bw2.write("IMEI1: " + imei[0]);
                    }
                    if (imei.length > 1) {
                        bw2.newLine();
                        bw2.write("IMEI2: " + imei[1]);
                    }
                }
                bw2.flush();
                if (bw2 != null) {
                    try {
                        bw2.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                if (fw2 != null) {
                    fw2.close();
                }
                fw = fw2;
            } catch (IOException e4) {
                e32 = e4;
                bw = bw2;
                fw = fw2;
                e32.printStackTrace();
                if (bw != null) {
                }
                if (fw != null) {
                }
                Os.chmod(productCmd, 438);
                Slog.d(TAG, "writeSpecialNVtoData Exit");
            } catch (Throwable th4) {
                th = th4;
                bw = bw2;
                fw = fw2;
                if (bw != null) {
                }
                if (fw != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            e32 = e5;
            e32.printStackTrace();
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
            if (fw != null) {
                fw.close();
            }
            Os.chmod(productCmd, 438);
            Slog.d(TAG, "writeSpecialNVtoData Exit");
        }
        try {
            Os.chmod(productCmd, 438);
        } catch (ErrnoException e6) {
            e6.printStackTrace();
        }
        Slog.d(TAG, "writeSpecialNVtoData Exit");
    }

    private void removeOldUpdatePackages(Context context) {
        Downloads.removeAllDownloadsByPackage(context, OLD_UPDATER_PACKAGE, OLD_UPDATER_CLASS);
    }

    private String getPreviousBootHeaders() {
        try {
            return FileUtils.readTextFile(lastHeaderFile, 0, null);
        } catch (IOException e) {
            return null;
        }
    }

    private String getCurrentBootHeaders() throws IOException {
        return "Build: " + Build.FINGERPRINT + "\n" + "Hardware: " + Build.BOARD + "\n" + "Revision: " + SystemProperties.get("ro.revision", "") + "\n" + "OTA_Version: " + SystemProperties.get("ro.build.version.ota", "") + "\n" + "Bootloader: " + Build.BOOTLOADER + "\n" + "Radio: " + Build.RADIO + "\n" + "Kernel: " + FileUtils.readTextFile(new File("/proc/version"), 1024, "...\n") + "\n";
    }

    private String getBootHeadersToLogAndUpdate() throws IOException {
        String oldHeaders = getPreviousBootHeaders();
        String newHeaders = getCurrentBootHeaders();
        try {
            FileUtils.stringToFile(lastHeaderFile, newHeaders);
        } catch (IOException e) {
            Slog.e(TAG, "Error writing " + lastHeaderFile, e);
        }
        if (oldHeaders == null) {
            return "isPrevious: false\n" + newHeaders;
        }
        return "isPrevious: true\n" + oldHeaders;
    }

    private void logBootEvents(Context ctx) throws IOException {
        DropBoxManager db = (DropBoxManager) ctx.getSystemService("dropbox");
        String headers = getBootHeadersToLogAndUpdate();
        boolean isPanic = isKernelPanic();
        Slog.d(TAG, "Aha! Boot reason is kernel panic " + isPanic + "!!!");
        String recovery = RecoverySystem.handleAftermath(ctx);
        if (!(recovery == null || db == null)) {
            db.addText("SYSTEM_RECOVERY_LOG", headers + recovery);
        }
        String lastKmsgFooter = "";
        if (isPanic) {
            lastKmsgFooter = "\n" + "Boot info:\n" + "Last boot reason: " + "panic" + "\n";
        } else {
            lastKmsgFooter = "\n" + "Boot info:\n" + "Last boot reason: " + "normal" + "\n";
        }
        HashMap<String, Long> timestamps = readTimestamps();
        if (SystemProperties.getLong("ro.runtime.firstboot", 0) == 0) {
            if (!StorageManager.inCryptKeeperBounce()) {
                SystemProperties.set("ro.runtime.firstboot", Long.toString(System.currentTimeMillis()));
            }
            if (db != null) {
                db.addText("SYSTEM_BOOT", headers);
            }
            if (isPanic) {
                OppoManager.writeLogToPartition(OppoManager.TYPE_PANIC, "kernel_panic", "KERNEL", "panic", this.mContext.getResources().getString(R.string.type_issue_Kernelpanic));
                addFileWithFootersToDropBox(db, timestamps, headers, lastKmsgFooter, "/proc/last_kmsg", -2097152, "SYSTEM_LAST_KMSG");
                addFileWithFootersToDropBox(db, timestamps, headers, lastKmsgFooter, "/sys/fs/pstore/dmesg-ramoops-0", -2097152, "SYSTEM_LAST_KMSG");
                addFileToDropBox(db, timestamps, headers, "/cache/recovery/log", -2097152, "SYSTEM_RECOVERY_LOG");
                addFileToDropBox(db, timestamps, headers, "/cache/recovery/last_kmsg", -2097152, "SYSTEM_RECOVERY_KMSG");
                addAuditErrorsToDropBox(db, timestamps, headers, -2097152, "SYSTEM_AUDIT");
            } else {
                String lastReboot = isRebootExceptionFromBolckException();
                boolean release_version = SystemProperties.getBoolean("ro.build.release_type", false);
                if (!(lastReboot == null || !release_version || db == null)) {
                    db.addText("SYSTEM_LAST_KMSG", "Kernel reboot from Block Exception!  lastReboot = " + lastReboot);
                }
            }
            SystemProperties.set("persist.sys.systemserver.pid", Integer.toString(Process.myPid()));
            Slog.v(TAG, "persist.sys.oppo.reboot = " + SystemProperties.get("persist.sys.oppo.reboot", ""));
            Slog.v(TAG, "persist.sys.oppo.fatal = " + SystemProperties.get("persist.sys.oppo.fatal", ""));
            Slog.v(TAG, "oppo.device.firstboot = " + SystemProperties.get("oppo.device.firstboot", ""));
            if (!(isPanic || !SystemProperties.get("persist.sys.oppo.fatal", "").equals("1") || (SystemProperties.get("oppo.device.firstboot", "").equals("1") ^ 1) == 0 || db == null || !SystemProperties.get("persist.sys.oppo.fb_upgraded", "").equals("1"))) {
                OppoManager.writeLogToPartition(OppoManager.TYPE_ANDROID_UNKNOWN_REBOOT, "unknow reboot" + readUnknowRebootStatus(), "KERNEL", "panic", this.mContext.getResources().getString(R.string.type_issue_unknown_reboot));
            }
            SystemProperties.set("persist.sys.oppo.reboot", "");
            SystemProperties.set("persist.sys.oppo.fatal", "");
            deleteFolderFilesThread("data/oppo_log/junk_logs/kernel");
            deleteFolderFilesThread("data/oppo_log/junk_logs/ftrace");
        } else {
            String systemcrashFile = SystemProperties.get("persist.sys.panic.file", "");
            int system_server_current_pid = Process.myPid();
            int system_server_previous_pid = SystemProperties.getInt("persist.sys.systemserver.pid", -1);
            Slog.e(TAG, "system_server crashed! --- systemcrashFile =" + systemcrashFile);
            String lastSystemReboot = isLastSystemServerRebootFormBolckException();
            File file = new File(systemcrashFile);
            if (file == null || !file.exists()) {
                dumpEnvironment();
                if (lastSystemReboot != null) {
                    if (db != null) {
                        db.addText("SYSTEM_SERVER", headers + "system_Server reboot from Block Exception! system_server_current_pid = " + system_server_current_pid + ",system_server_previous_pid = " + system_server_previous_pid + " lastSystemReboot = " + lastSystemReboot);
                    }
                } else if (db != null) {
                    db.addText("SYSTEM_SERVER", headers + "system_Server crash but can not get efficacious log! system_server_current_pid = " + system_server_current_pid + ",system_server_previous_pid = " + system_server_previous_pid);
                }
            } else {
                SystemProperties.set("persist.sys.send.file", systemcrashFile);
                if (file.getName().startsWith("system_server_watchdog", 0)) {
                    addFileToDropBox(db, timestamps, headers, systemcrashFile, 2097152, "SYSTEM_SERVER_WATCHDOG");
                } else if (file.getName().startsWith("tombstone", 0)) {
                    addFileToDropBox(db, timestamps, headers, systemcrashFile, 2097152, "SYSTEM_TOMBSTONE_CRASH");
                } else if (!systemcrashFile.endsWith(".gz")) {
                    addFileToDropBox(db, timestamps, headers, systemcrashFile, 2097152, "SYSTEM_SERVER");
                } else if (db != null) {
                    db.addText("SYSTEM_SERVER_GZ", "LOG FOR GZ");
                }
            }
            SystemProperties.set("persist.sys.systemserver.pid", Integer.toString(system_server_current_pid));
            if (lastSystemReboot != null) {
                OppoManager.writeLogToPartition(OppoManager.TYPE_ANDROID_SYSTEM_REBOOT_FROM_BLOCKED, lastSystemReboot, "ANDROID", "reboot_from_blocked", ctx.getResources().getString(R.string.type_issue_system_server_blocked));
            }
        }
        SystemProperties.set("persist.sys.panic.file", "");
        logFsShutdownTime();
        logFsMountTime();
        addFsckErrorsToDropBoxAndLogFsStat(db, timestamps, headers, -2097152, "SYSTEM_FSCK");
        logSystemServerShutdownTimeMetrics();
        File[] tombstoneFiles = TOMBSTONE_DIR.listFiles();
        int i = 0;
        while (tombstoneFiles != null && i < tombstoneFiles.length) {
            if (tombstoneFiles[i].isFile()) {
                addFileToDropBox(db, timestamps, headers, tombstoneFiles[i].getPath(), 2097152, "SYSTEM_TOMBSTONE");
            }
            i++;
        }
        writeTimestamps(timestamps);
        final DropBoxManager dropBoxManager = db;
        final String str = headers;
        sTombstoneObserver = new FileObserver(TOMBSTONE_DIR.getPath(), 8) {
            public void onEvent(int event, String path) {
                HashMap<String, Long> timestamps = BootReceiver.readTimestamps();
                try {
                    File file = new File(BootReceiver.TOMBSTONE_DIR, path);
                    if (file.isFile()) {
                        BootReceiver.addFileToDropBox(dropBoxManager, timestamps, str, file.getPath(), 2097152, "SYSTEM_TOMBSTONE");
                        ASSERT.CopyTombstone(file.getPath());
                    }
                } catch (IOException e) {
                    Slog.e(BootReceiver.TAG, "Can't log tombstone", e);
                }
                BootReceiver.this.writeTimestamps(timestamps);
            }
        };
        sTombstoneObserver.startWatching();
        resetPanicState();
    }

    private static boolean isKernelPanic() {
        if ("kernel".equals(readBootReason())) {
            return true;
        }
        return false;
    }

    private static void resetPanicState() {
        SystemProperties.set("sys.oppo.panic", "false");
    }

    private static String readBootReason() {
        String res = "";
        try {
            FileInputStream fin = new FileInputStream(BOOT_REASON_FILE);
            byte[] buffer = new byte[fin.available()];
            fin.read(buffer);
            res = new StringBuffer().append(new String(buffer)).toString().trim();
            fin.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return res;
        }
    }

    private void dumpEnvironment() {
        SystemProperties.set("sys.dumpenvironment.finished", "0");
        SystemProperties.set("ctl.start", "dumpenvironment");
        long begin = SystemClock.elapsedRealtime();
        while (SystemProperties.getInt("sys.dumpenvironment.finished", 0) != 1 && SystemClock.elapsedRealtime() - begin < ((long) SystemProperties.getInt("ro.dumpenvironment.time", 4000))) {
            SystemClock.sleep(100);
        }
    }

    private static void addFileToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String filename, int maxSize, String tag) throws IOException {
        addFileWithFootersToDropBox(db, timestamps, headers, "", filename, maxSize, tag);
    }

    private static void addFileWithFootersToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String footers, String filename, int maxSize, String tag) throws IOException {
        if (db != null && (db.isTagEnabled(tag) ^ 1) == 0) {
            File file = new File(filename);
            long fileTime = file.lastModified();
            if (fileTime > 0) {
                if (!timestamps.containsKey(filename) || ((Long) timestamps.get(filename)).longValue() != fileTime) {
                    timestamps.put(filename, Long.valueOf(fileTime));
                    Slog.i(TAG, "Copying " + filename + " to DropBox (" + tag + ")");
                    db.addText(tag, headers + FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n") + footers);
                }
            }
        }
    }

    private static void addAuditErrorsToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, int maxSize, String tag) throws IOException {
        if (db != null && (db.isTagEnabled(tag) ^ 1) == 0) {
            Slog.i(TAG, "Copying audit failures to DropBox");
            File file = new File("/proc/last_kmsg");
            long fileTime = file.lastModified();
            if (fileTime <= 0) {
                file = new File("/sys/fs/pstore/console-ramoops");
                fileTime = file.lastModified();
                if (fileTime <= 0) {
                    file = new File("/sys/fs/pstore/console-ramoops-0");
                    fileTime = file.lastModified();
                }
            }
            if (fileTime > 0) {
                if (!timestamps.containsKey(tag) || ((Long) timestamps.get(tag)).longValue() != fileTime) {
                    timestamps.put(tag, Long.valueOf(fileTime));
                    String log = FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n");
                    StringBuilder sb = new StringBuilder();
                    for (String line : log.split("\n")) {
                        if (line.contains("audit")) {
                            sb.append(line).append("\n");
                        }
                    }
                    Slog.i(TAG, "Copied " + sb.toString().length() + " worth of audits to DropBox");
                    db.addText(tag, headers + sb.toString());
                }
            }
        }
    }

    private static void addFsckErrorsToDropBoxAndLogFsStat(DropBoxManager db, HashMap<String, Long> timestamps, String headers, int maxSize, String tag) throws IOException {
        boolean uploadEnabled = true;
        if (db == null || (db.isTagEnabled(tag) ^ 1) != 0) {
            uploadEnabled = false;
        }
        boolean uploadNeeded = false;
        Slog.i(TAG, "Checking for fsck errors");
        File file = new File("/dev/fscklogs/log");
        if (file.lastModified() > 0) {
            String log = FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n");
            Pattern pattern = Pattern.compile(FS_STAT_PATTERN);
            String[] lines = log.split("\n");
            int lineNumber = 0;
            int lastFsStatLineNumber = 0;
            for (String line : lines) {
                if (line.contains(FSCK_FS_MODIFIED)) {
                    uploadNeeded = true;
                } else if (line.contains("fs_stat")) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        int stat = Integer.decode(matcher.group(2)).intValue();
                        if ((stat & 8) != 0) {
                            Slog.i(TAG, "file system unclean shutdown, fs_stat:0x" + Integer.toHexString(stat));
                            uploadNeeded = true;
                        }
                        handleFsckFsStat(matcher, lines, lastFsStatLineNumber, lineNumber);
                        lastFsStatLineNumber = lineNumber;
                    } else {
                        Slog.w(TAG, "cannot parse fs_stat:" + line);
                    }
                }
                lineNumber++;
            }
            if (uploadEnabled && uploadNeeded) {
                addFileToDropBox(db, timestamps, headers, "/dev/fscklogs/log", maxSize, tag);
            }
            file.delete();
        }
    }

    private static void logFsMountTime() {
        for (String propPostfix : MOUNT_DURATION_PROPS_POSTFIX) {
            int duration = SystemProperties.getInt("ro.boottime.init.mount_all." + propPostfix, 0);
            if (duration != 0) {
                MetricsLogger.histogram(null, "boot_mount_all_duration_" + propPostfix, duration);
            }
        }
    }

    private static void logSystemServerShutdownTimeMetrics() {
        File metricsFile = new File(SHUTDOWN_METRICS_FILE);
        String metricsStr = null;
        if (metricsFile.exists()) {
            try {
                metricsStr = FileUtils.readTextFile(metricsFile, 0, null);
            } catch (IOException e) {
                Slog.e(TAG, "Problem reading " + metricsFile, e);
            }
        }
        if (!TextUtils.isEmpty(metricsStr)) {
            for (String keyValueStr : metricsStr.split(",")) {
                String[] keyValue = keyValueStr.split(SettingsStringUtil.DELIMITER);
                if (keyValue.length != 2) {
                    Slog.e(TAG, "Wrong format of shutdown metrics - " + metricsStr);
                } else if (keyValue[0].startsWith(SHUTDOWN_TRON_METRICS_PREFIX)) {
                    logTronShutdownMetric(keyValue[0], keyValue[1]);
                }
            }
        }
        metricsFile.delete();
    }

    private static void logTronShutdownMetric(String metricName, String valueStr) {
        try {
            int value = Integer.parseInt(valueStr);
            if (value >= 0) {
                MetricsLogger.histogram(null, metricName, value);
            }
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Cannot parse metric " + metricName + " int value - " + valueStr);
        }
    }

    private static void logFsShutdownTime() {
        File f = null;
        for (String fileName : LAST_KMSG_FILES) {
            File file = new File(fileName);
            if (file.exists()) {
                f = file;
                break;
            }
        }
        if (f != null) {
            try {
                Matcher matcher = Pattern.compile(LAST_SHUTDOWN_TIME_PATTERN, 8).matcher(FileUtils.readTextFile(f, -16384, null));
                if (matcher.find()) {
                    MetricsLogger.histogram(null, "boot_fs_shutdown_duration", Integer.parseInt(matcher.group(1)));
                    MetricsLogger.histogram(null, "boot_fs_shutdown_umount_stat", Integer.parseInt(matcher.group(2)));
                    Slog.i(TAG, "boot_fs_shutdown," + matcher.group(1) + "," + matcher.group(2));
                } else {
                    MetricsLogger.histogram(null, "boot_fs_shutdown_umount_stat", 4);
                    Slog.w(TAG, "boot_fs_shutdown, string not found");
                }
            } catch (IOException e) {
                Slog.w(TAG, "cannot read last msg", e);
            }
        }
    }

    public static int fixFsckFsStat(String partition, int statOrg, String[] lines, int startLineNumber, int endLineNumber) {
        int stat = statOrg;
        if ((statOrg & 1024) == 0) {
            return stat;
        }
        Pattern passPattern = Pattern.compile(FSCK_PASS_PATTERN);
        Pattern treeOptPattern = Pattern.compile(FSCK_TREE_OPTIMIZATION_PATTERN);
        String currentPass = "";
        boolean foundTreeOptimization = false;
        boolean foundQuotaFix = false;
        boolean foundTimestampAdjustment = false;
        boolean foundOtherFix = false;
        String otherFixLine = null;
        int i = startLineNumber;
        while (i < endLineNumber) {
            String line = lines[i];
            if (line.contains(FSCK_FS_MODIFIED)) {
                break;
            }
            if (line.startsWith("Pass ")) {
                Matcher matcher = passPattern.matcher(line);
                if (matcher.find()) {
                    currentPass = matcher.group(1);
                }
            } else if (line.startsWith("Inode ")) {
                if (!treeOptPattern.matcher(line).find() || !currentPass.equals("1")) {
                    foundOtherFix = true;
                    otherFixLine = line;
                    break;
                }
                foundTreeOptimization = true;
                Slog.i(TAG, "fs_stat, partition:" + partition + " found tree optimization:" + line);
            } else if (line.startsWith("[QUOTA WARNING]") && currentPass.equals("5")) {
                Slog.i(TAG, "fs_stat, partition:" + partition + " found quota warning:" + line);
                foundQuotaFix = true;
                if (!foundTreeOptimization) {
                    otherFixLine = line;
                    break;
                }
            } else if (!line.startsWith("Update quota info") || !currentPass.equals("5")) {
                if (!line.startsWith("Timestamp(s) on inode") || !line.contains("beyond 2310-04-04 are likely pre-1970") || !currentPass.equals("1")) {
                    line = line.trim();
                    if (!(line.isEmpty() || (currentPass.isEmpty() ^ 1) == 0)) {
                        foundOtherFix = true;
                        otherFixLine = line;
                        break;
                    }
                }
                Slog.i(TAG, "fs_stat, partition:" + partition + " found timestamp adjustment:" + line);
                if (lines[i + 1].contains("Fix? yes")) {
                    i++;
                }
                foundTimestampAdjustment = true;
            }
            i++;
        }
        if (foundOtherFix) {
            if (otherFixLine == null) {
                return stat;
            }
            Slog.i(TAG, "fs_stat, partition:" + partition + " fix:" + otherFixLine);
            return stat;
        } else if (foundQuotaFix && (foundTreeOptimization ^ 1) != 0) {
            Slog.i(TAG, "fs_stat, got quota fix without tree optimization, partition:" + partition);
            return stat;
        } else if ((!foundTreeOptimization || !foundQuotaFix) && !foundTimestampAdjustment) {
            return stat;
        } else {
            Slog.i(TAG, "fs_stat, partition:" + partition + " fix ignored");
            return statOrg & -1025;
        }
    }

    private static void handleFsckFsStat(Matcher match, String[] lines, int startLineNumber, int endLineNumber) {
        String partition = match.group(1);
        try {
            int stat = fixFsckFsStat(partition, Integer.decode(match.group(2)).intValue(), lines, startLineNumber, endLineNumber);
            MetricsLogger.histogram(null, "boot_fs_stat_" + partition, stat);
            Slog.i(TAG, "fs_stat, partition:" + partition + " stat:0x" + Integer.toHexString(stat));
        } catch (NumberFormatException e) {
            Slog.w(TAG, "cannot parse fs_stat: partition:" + partition + " stat:" + match.group(2));
        }
    }

    private static HashMap<String, Long> readTimestamps() {
        HashMap<String, Long> timestamps;
        boolean success;
        Throwable th;
        FileInputStream stream;
        Throwable th2;
        synchronized (sFile) {
            timestamps = new HashMap();
            success = false;
            th = null;
            stream = null;
            try {
                int type;
                stream = sFile.openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    throw new IllegalStateException("no start tag found");
                }
                int outerDepth = parser.getDepth();
                while (true) {
                    type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        success = true;
                    } else if (!(type == 3 || type == 4)) {
                        if (parser.getName().equals("log")) {
                            timestamps.put(parser.getAttributeValue(null, "filename"), Long.valueOf(Long.valueOf(parser.getAttributeValue(null, StreamItemsColumns.TIMESTAMP)).longValue()));
                        } else {
                            Slog.w(TAG, "Unknown tag: " + parser.getName());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                }
                success = true;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Throwable th3) {
                        th = th3;
                    }
                }
                if (th != null) {
                    throw th;
                } else {
                    if (1 == null) {
                        timestamps.clear();
                    }
                }
            } catch (Throwable th4) {
                Throwable th5 = th4;
                th4 = th2;
                th2 = th5;
            }
        }
        return timestamps;
        if (stream != null) {
            try {
                stream.close();
            } catch (Throwable th6) {
                if (th4 == null) {
                    th4 = th6;
                } else if (th4 != th6) {
                    th4.addSuppressed(th6);
                }
            }
        }
        if (th4 != null) {
            try {
                throw th4;
            } catch (FileNotFoundException e) {
                Slog.i(TAG, "No existing last log timestamp file " + sFile.getBaseFile() + "; starting empty");
                if (!success) {
                    timestamps.clear();
                }
            } catch (IOException e2) {
                Slog.w(TAG, "Failed parsing " + e2);
                if (!success) {
                    timestamps.clear();
                }
            } catch (IllegalStateException e3) {
                Slog.w(TAG, "Failed parsing " + e3);
                if (!success) {
                    timestamps.clear();
                }
            } catch (NullPointerException e4) {
                Slog.w(TAG, "Failed parsing " + e4);
                if (!success) {
                    timestamps.clear();
                }
            } catch (XmlPullParserException e5) {
                Slog.w(TAG, "Failed parsing " + e5);
                if (!success) {
                    timestamps.clear();
                }
            } catch (Throwable th7) {
                if (!success) {
                    timestamps.clear();
                }
            }
        } else {
            throw th2;
        }
    }

    private void writeTimestamps(HashMap<String, Long> timestamps) {
        synchronized (sFile) {
            try {
                FileOutputStream stream = sFile.startWrite();
                try {
                    XmlSerializer out = new FastXmlSerializer();
                    out.setOutput(stream, StandardCharsets.UTF_8.name());
                    out.startDocument(null, Boolean.valueOf(true));
                    out.startTag(null, "log-files");
                    for (String filename : timestamps.keySet()) {
                        out.startTag(null, "log");
                        out.attribute(null, "filename", filename);
                        out.attribute(null, StreamItemsColumns.TIMESTAMP, ((Long) timestamps.get(filename)).toString());
                        out.endTag(null, "log");
                    }
                    out.endTag(null, "log-files");
                    out.endDocument();
                    sFile.finishWrite(stream);
                } catch (IOException e) {
                    Slog.w(TAG, "Failed to write timestamp file, using the backup: " + e);
                    sFile.failWrite(stream);
                }
            } catch (IOException e2) {
                Slog.w(TAG, "Failed to write timestamp file: " + e2);
                return;
            }
        }
        return;
    }

    private String isLastSystemServerRebootFormBolckException() {
        Exception e;
        if (new File("/proc/sys/kernel/hung_task_oppo_kill").exists()) {
            try {
                BufferedReader in = new BufferedReader(new FileReader("/proc/sys/kernel/hung_task_oppo_kill"));
                try {
                    String strSend = in.readLine();
                    if (in != null) {
                        in.close();
                    }
                    if (!strSend.trim().isEmpty()) {
                        return strSend;
                    }
                    BufferedReader bufferedReader = in;
                    return null;
                } catch (Exception e2) {
                    e = e2;
                    e.printStackTrace();
                    return null;
                }
            } catch (Exception e3) {
                e = e3;
                e.printStackTrace();
                return null;
            }
        }
        Slog.v(TAG, "reboot file is not exists");
        return null;
    }

    private String isRebootExceptionFromBolckException() {
        String strSend = SystemProperties.get("persist.hungtask.oppo.kill");
        if (strSend == null || (strSend.isEmpty() ^ 1) == 0) {
            return null;
        }
        return strSend;
    }

    private static String readUnknowRebootStatus() {
        int i = 0;
        String res = "";
        try {
            FileInputStream finPon = new FileInputStream(UNKNOW_REBOOT_PON);
            FileInputStream finPff = new FileInputStream(UNKNOW_REBOOT_PFF);
            byte[] bufferPon = new byte[finPon.available()];
            byte[] bufferPff = new byte[finPon.available()];
            finPon.read(bufferPon);
            finPff.read(bufferPff);
            int ponHigh = 0;
            int pffHigh = 0;
            int length = bufferPon.length;
            int i2 = 0;
            while (i2 < length && bufferPon[i2] != (byte) 0) {
                ponHigh++;
                i2++;
            }
            i2 = bufferPff.length;
            while (i < i2 && bufferPff[i] != (byte) 0) {
                pffHigh++;
                i++;
            }
            Slog.v(TAG, "ponHigh = " + ponHigh + " pffHigh=" + pffHigh);
            res = new StringBuffer().append(new String(bufferPon, 0, ponHigh).trim() + " " + new String(bufferPff, 0, pffHigh).trim()).toString();
            finPon.close();
            finPff.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return res;
        }
    }

    private void deleteFolderFilesThread(final String path) {
        try {
            new Thread(new Runnable() {
                public void run() {
                    File folder = new File(path);
                    if (folder != null && folder.exists() && folder.isDirectory()) {
                        File[] files = folder.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                if (f.exists() && f.isFile()) {
                                    f.delete();
                                }
                            }
                            return;
                        }
                        return;
                    }
                    Slog.v(BootReceiver.TAG, "" + path + " not exists");
                }
            }).start();
        } catch (Exception e) {
            Slog.e(TAG, "deleteFolderFilesThread e: " + e.toString());
        }
    }
}

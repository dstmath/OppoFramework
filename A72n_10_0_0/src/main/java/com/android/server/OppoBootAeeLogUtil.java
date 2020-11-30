package com.android.server;

import android.content.Context;
import android.content.res.OppoThemeResources;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.OppoManager;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.util.Slog;
import com.android.internal.content.NativeLibraryHelper;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class OppoBootAeeLogUtil {
    private static final String TAG = "OppoBootReceiver_OppoBootAeeLogUtil";
    private static final String mLastExceptionProc = "/proc/sys/kernel/hung_task_oppo_kill";
    private static final String mLastExceptionProperty = "persist.hungtask.oppo.kill";

    private static String isLastSystemServerRebootFormBolckException() {
        if (!new File(mLastExceptionProc).exists()) {
            Slog.v(TAG, "reboot file is not exists");
            return null;
        }
        try {
            BufferedReader in = new BufferedReader(new FileReader(mLastExceptionProc));
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

    private static boolean isMtkPlatform() {
        return SystemProperties.get("ro.board.platform", OppoThemeResources.OPPO_PACKAGE).toLowerCase().startsWith("mt");
    }

    public static int checkMtkHwtState(Context ctx) {
        int type = -1;
        if (!isMtkPlatform()) {
            return -1;
        }
        String bootAeeDB = SystemProperties.get("vendor.debug.mtk.aeev.db", null);
        Slog.d(TAG, "aee db path is " + bootAeeDB);
        String issue = "";
        if (bootAeeDB != null && (bootAeeDB.contains(OppoManager.ISSUE_KERNEL_HWT) || bootAeeDB.contains("HW_Reboot") || bootAeeDB.contains(OppoManager.ISSUE_KERNEL_HANG))) {
            if (bootAeeDB.contains(OppoManager.ISSUE_KERNEL_HWT)) {
                type = OppoManager.TYPE_ANDROID_REBOOT_HWT;
                issue = OppoManager.ISSUE_KERNEL_HWT;
            } else if (bootAeeDB.contains("HW_Reboot")) {
                type = OppoManager.TYPE_ANDROID_REBOOT_HARDWARE_REBOOT;
                issue = OppoManager.ISSUE_KERNEL_HARDWARE_REBOOT;
            } else if (bootAeeDB.contains(OppoManager.ISSUE_KERNEL_HANG)) {
                type = OppoManager.TYPE_ANDROID_REBOOT_HANG;
                issue = OppoManager.ISSUE_KERNEL_HANG;
            }
            Slog.d(TAG, "aee db type is " + type + ", issue is " + issue);
            if (type != -1 && !issue.isEmpty()) {
                OppoManager.writeLogToPartition(type, "HWT_HardwareReboot_HANG", OppoManager.ANDROID_TAG, issue, ctx.getResources().getString(201653515));
            }
        }
        return type;
    }

    public static void prepareMtkLog(boolean isAndroidReboot, String headers) {
        String unknownCrashReason;
        if (isMtkPlatform()) {
            String java_uuid = UUID.randomUUID().toString().replace(NativeLibraryHelper.CLEAR_ABI_OVERRIDE, "").substring(0, 15);
            String aeePath = parseAeeLogPath(isAndroidReboot);
            String aeeType = parseAeeTag(isAndroidReboot, aeePath);
            if (isAndroidReboot) {
                int system_server_current_pid = Process.myPid();
                int system_server_previous_pid = SystemProperties.getInt("persist.sys.systemserver.pid", -1);
                if (system_server_current_pid == system_server_previous_pid) {
                    Slog.e(TAG, "may not crash, system_server_current_pid == system_server_previous_pid = " + system_server_current_pid);
                } else {
                    Slog.d(TAG, "android restart maybe crash or killed, system_server_current_pid = " + system_server_current_pid + " system_server_previous_pid = " + system_server_previous_pid);
                }
                if (aeePath != null) {
                    if (new File(aeePath + "/ZZ_INTERNAL").exists()) {
                        SystemProperties.set("sys.mtk.last.aee.db", aeePath);
                        packageAeeLogs(aeeType, aeePath, java_uuid);
                        return;
                    }
                }
                String lastSystemReboot = isLastSystemServerRebootFormBolckException();
                if (lastSystemReboot != null) {
                    unknownCrashReason = headers + "system_Server reboot from Block Exception! system_server_current_pid = " + system_server_current_pid + ",system_server_previous_pid = " + system_server_previous_pid + " lastSystemReboot = " + lastSystemReboot;
                } else {
                    unknownCrashReason = headers + "system_Server crash but can not get efficacious log! system_server_current_pid = " + system_server_current_pid + ",system_server_previous_pid = " + system_server_previous_pid;
                }
                generateSystemCrashLog(unknownCrashReason);
                waitForStringPropertyReady("vendor.debug.mtk.aee.status", "free", "free", 60);
                waitForStringPropertyReady("vendor.debug.mtk.aee.status64", "free", "free", 60);
                String aeePath2 = parseAeeLogPath(isAndroidReboot);
                String aeeType2 = parseAeeTag(isAndroidReboot, aeePath2);
                if (aeePath2 == null || aeeType2 == null) {
                    Slog.e(TAG, "prepareMtkLog failed for aeePath or aeeType illegal!");
                } else {
                    packageAeeLogs(aeeType2, aeePath2, java_uuid);
                }
            } else if (aeePath == null || aeeType == null) {
                Slog.e(TAG, "prepareMtkLog is not unnormal reboot. aeePath is " + aeePath + " aeeType is " + aeeType + " isAndroidReboot = " + isAndroidReboot);
            } else {
                packageAeeLogs(aeeType, aeePath, java_uuid);
            }
        }
    }

    private static void packageAeeLogs(String aeeType, String aeePath, String uuid) {
        String aeeGzFile = "/data/oppo/log/DCS/de/AEE_DB/" + aeeType + "@" + uuid + "@" + SystemProperties.get("ro.build.version.ota") + "@" + System.currentTimeMillis() + ".dat.gz";
        Slog.v(TAG, "prepare zip! aeeType is " + aeeType + " aeePath is " + aeePath);
        try {
            zipFolder(aeePath, "/data/oppo/log/DCS/de/AEE_DB/aee.zip");
            gzipFile("/data/oppo/log/DCS/de/AEE_DB/aee.zip", aeeGzFile);
            new File("/data/oppo/log/DCS/de/AEE_DB/aee.zip").delete();
            if (new File(aeeGzFile).exists()) {
                Slog.v(TAG, "package end, delete file " + aeePath);
                deleteDir(new File(aeePath).getAbsoluteFile());
            }
            SystemProperties.set("sys.backup.minidump.tag", aeeType);
            SystemProperties.set("ctl.start", "generate_runtime_prop");
        } catch (Exception e) {
            Slog.e(TAG, "dumpEnvironmentGzFile failed!");
            e.printStackTrace();
        }
    }

    private static void waitForIntPropertyReady(String prop, int failValue, int expectValue, int maxTime) {
        Slog.d(TAG, "waitForPropertyReady!int " + prop);
        SystemClock.sleep(2000);
        int loopCount = maxTime * 2;
        for (int i = 0; i < loopCount && SystemProperties.getInt(prop, failValue) != expectValue; i++) {
            SystemClock.sleep(500);
        }
        SystemClock.sleep(1000);
        Slog.d(TAG, "waitForPropertyReady end!int " + prop);
    }

    private static void waitForStringPropertyReady(String prop, String failValue, String expectValue, int maxTime) {
        Slog.d(TAG, "waitForPropertyReady!String " + prop);
        SystemClock.sleep(2000);
        int i = maxTime * 2;
        for (int i2 = 0; i2 < 40; i2++) {
            SystemClock.sleep(500);
            if (SystemProperties.get(prop, failValue).equals(expectValue)) {
                break;
            }
        }
        SystemClock.sleep(1000);
        Slog.d(TAG, "waitForPropertyReady end!String " + prop);
    }

    private static String parseAeeTag(boolean isAndroidReboot, String aeePath) {
        int lastIndex;
        if (aeePath == null || (lastIndex = aeePath.lastIndexOf(".")) == -1) {
            return null;
        }
        String endStr = aeePath.substring(lastIndex + 1);
        if (endStr.equals("NE")) {
            return "AEE_SYSTEM_TOMBSTONE_CRASH";
        }
        if (endStr.equals("JE")) {
            return "AEE_SYSTEM_SERVER";
        }
        if (endStr.equals("SWT")) {
            return "AEE_SYSTEM_SERVER_WATCHDOG";
        }
        if (endStr.equals("KE") || endStr.equals(OppoManager.ISSUE_KERNEL_HWT) || endStr.equals(OppoManager.ISSUE_KERNEL_HARDWARE_REBOOT) || endStr.equals(OppoManager.ISSUE_KERNEL_HANG) || !isAndroidReboot) {
            return "AEE_SYSTEM_LAST_KMSG";
        }
        return "AEE_SYSTEM_SERVER";
    }

    private static String parseAeeLogPath(boolean isAndroidReboot) {
        String aeeDBProp = SystemProperties.get(isAndroidReboot ? "vendor.debug.mtk.aee.db" : "vendor.debug.mtk.aeev.db");
        if (aeeDBProp == null || aeeDBProp.equals("")) {
            Slog.i(TAG, " parserAeeLogPath aeeDBProp is null");
            return null;
        } else if (aeeDBProp.indexOf(SettingsStringUtil.DELIMITER) == -1) {
            Slog.w(TAG, "parserAeeLogPath aeeDBProp " + aeeDBProp + " is not null but inavailable");
            return null;
        } else {
            String aeePath = aeeDBProp.substring(aeeDBProp.indexOf(SettingsStringUtil.DELIMITER) + 1);
            if (isAndroidReboot) {
                return aeePath;
            }
            SystemProperties.set("ctl.start", "checkAeeLogs");
            waitForIntPropertyReady("sys.move.aeevendor.ready", 0, 1, 20);
            return aeePath.replaceFirst("/data/vendor", "/data");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0074, code lost:
        r12 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r10.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0079, code lost:
        r13 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007a, code lost:
        r11.addSuppressed(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007d, code lost:
        throw r12;
     */
    private static void zipFolder(String inputFolderPath, String outZipPath) {
        File[] files = new File(inputFolderPath).listFiles();
        Slog.d(TAG, "Zip directory: " + inputFolderPath + " to " + outZipPath);
        ZipOutputStream zos = null;
        try {
            ZipOutputStream zos2 = new ZipOutputStream(new FileOutputStream(outZipPath));
            byte[] buf = new byte[1024];
            for (File file : files) {
                if (file != null && file.canRead()) {
                    try {
                        InputStream is = new BufferedInputStream(new FileInputStream(file));
                        zos2.putNextEntry(new ZipEntry(file.getName()));
                        while (true) {
                            int len = is.read(buf, 0, 1024);
                            if (len <= 0) {
                                break;
                            }
                            zos2.write(buf, 0, len);
                        }
                        zos2.closeEntry();
                        is.close();
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                zos2.close();
            } catch (IOException e2) {
            }
        } catch (IOException e3) {
            Slog.e(TAG, "error zipping up profile data", e3);
            if (0 != 0) {
                try {
                    zos.close();
                } catch (IOException e4) {
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    zos.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
    }

    public static void gzipFile(String source_filepath, String destinaton_zip_filepath) {
        byte[] buffer = new byte[1024];
        try {
            GZIPOutputStream gzipOuputStream = new GZIPOutputStream(new FileOutputStream(destinaton_zip_filepath));
            FileInputStream fileInput = new FileInputStream(source_filepath);
            while (true) {
                int bytes_read = fileInput.read(buffer);
                if (bytes_read > 0) {
                    gzipOuputStream.write(buffer, 0, bytes_read);
                } else {
                    fileInput.close();
                    gzipOuputStream.finish();
                    gzipOuputStream.close();
                    Slog.d(TAG, "The file was compressed successfully!");
                    return;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void deleteDir(File dir) {
        if (dir.exists()) {
            File[] fileList = dir.listFiles();
            if (!dir.isDirectory() || fileList == null || fileList.length <= 0) {
                deleteFile(dir);
                return;
            }
            for (File file : fileList) {
                deleteDir(file);
            }
            deleteFile(dir);
        }
    }

    private static void deleteFile(File dir) {
        if (dir.delete()) {
            Slog.w(TAG, "file: " + dir + " delete succeed");
            return;
        }
        Slog.e(TAG, "file: " + dir + " delete failed");
    }

    private static void generateSystemCrashLog(String unknownCrashReason) {
        Slog.w(TAG, "system_server unknown reboot call");
        try {
            if (SystemProperties.get("ro.vendor.have_aee_feature").equals(WifiEnterpriseConfig.ENGINE_ENABLE)) {
                try {
                    if (OppoMirrorMtkExceptionLogHelper.generateSystemCrashLog != null) {
                        OppoMirrorMtkExceptionLogHelper.generateSystemCrashLog.call(unknownCrashReason, new Object[0]);
                    } else {
                        Slog.e(TAG, "generateSystemCrashLog failed for method empty.");
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "generateSystemCrashLog failed!", e);
                }
            }
        } catch (Exception e2) {
            Slog.e(TAG, "generateSystemCrashLog :" + e2.toString());
        }
        Slog.w(TAG, "system_server unknown reboot call end");
    }
}

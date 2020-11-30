package com.android.server;

import android.app.OppoActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.DropBoxManager;
import android.os.OppoManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.pm.PackageManagerService;
import com.oppo.debug.ASSERT;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class OppoDropBoxManagerService {
    public static final String OPPO_CTA_USER_ECPERIENCE = "oppo_cta_user_experience";
    private static final String TAG = "DropBoxManagerService";
    private Context mContext = null;
    private OppoActivityManager mOppoActMgr = null;

    public OppoDropBoxManagerService(Context context) {
        this.mContext = context;
    }

    private Context getContext() {
        return this.mContext;
    }

    public void addSystemLogFile(DropBoxManager.Entry entry, String tag) {
        boolean agreeUserExperience = getNetworkAccess(getContext());
        String str = "SYSTEM_SERVER";
        if (tag.equals("SYSTEM_SERVER_GZ") || tag.equals("SYSTEM_SERVER_WATCHDOG") || tag.equals(str) || tag.equals("SYSTEM_TOMBSTONE_CRASH")) {
            int i = OppoManager.TYPE_ANDROID_CRASH;
            OppoManager.writeLogToPartition(i, "system_restart_" + tag, "ANDROID", "crash", getContext().getResources().getString(201653511));
            Map<String, String> logQualMap = new HashMap<>();
            if (!tag.equals("SYSTEM_SERVER_GZ") && !tag.equals(str)) {
                str = tag;
            }
            logQualMap.put("crashType", str);
            OppoManager.sendQualityDCSEvent(OppoManager.QualityEventId.EV_STABILITY_SYSTEM_CRASH, logQualMap);
        } else if (tag.equals("SYSTEM_LAST_KMSG")) {
            String header = entry.getText(32);
            Slog.v(TAG, "SYSTEM_LAST_KMSG text = " + header);
            if (header == null || !header.startsWith("unknown reboot")) {
                OppoManager.writeLogToPartition(OppoManager.TYPE_PANIC, "kernel_panic", "KERNEL", "panic", getContext().getResources().getString(201653515));
                OppoManager.sendQualityDCSEvent(OppoManager.QualityEventId.EV_STABILITY_KERNEL_PANIC, (Map) null);
            } else {
                OppoManager.writeLogToPartition(OppoManager.TYPE_ANDROID_UNKNOWN_REBOOT, "unknow reboot", "KERNEL", "panic", getContext().getResources().getString(201653521));
                OppoManager.sendQualityDCSEvent(OppoManager.QualityEventId.EV_STABILITY_UNKNOWN_REBOOT, (Map) null);
            }
        }
        if (!agreeUserExperience) {
            return;
        }
        if (tag.equals("SYSTEM_SERVER_GZ") || tag.equals("SYSTEM_SERVER_WATCHDOG")) {
            String systemcrashFile = SystemProperties.get("persist.sys.send.file", "null");
            if (!systemcrashFile.equals("null")) {
                ArrayList<String> gzFiles = new ArrayList<>();
                gzFiles.add(systemcrashFile);
                Slog.d(TAG, "send feedback broadcast!,tag =" + tag);
                sendFeedbackBroadcast(gzFiles, tag);
                SystemProperties.set("persist.sys.send.file", "null");
            }
        }
    }

    public void addDropBoxFile(long time, String tag, int flags) {
        File[] logFiles = new File("/data/system/dropbox").listFiles();
        int i = 0;
        while (logFiles != null && i < logFiles.length) {
            String name = logFiles[i].getName();
            if (name.endsWith(PackageManagerService.COMPRESSED_EXTENSION)) {
                name = name.substring(0, name.length() - 3);
            }
            if (name.endsWith(".lost")) {
                name = name.substring(0, name.length() - 5);
            } else if (name.endsWith(".txt")) {
                name = name.substring(0, name.length() - 4);
            } else if (name.endsWith(".dat")) {
                name = name.substring(0, name.length() - 4);
            }
            if (name.contains(String.valueOf(time)) && name.contains(tag)) {
                ArrayList<String> mFiles = new ArrayList<>();
                mFiles.add("/data/system/dropbox/" + logFiles[i].getName());
                Slog.d(TAG, "file :: /data/system/dropbox/" + logFiles[i].getName());
                if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                    if (tag.equals("system_server_lowmem")) {
                        Slog.d(TAG, "the tag is  :: " + tag);
                    } else if ((tag.startsWith("system_server", 0) && !tag.startsWith("system_server_wtf", 0) && !tag.startsWith("system_server_strictmode", 0)) || tag.equals("system_app_crash") || tag.equals("system_app_anr") || tag.equals("data_app_crash") || tag.equals("data_app_native_crash") || tag.equals("data_app_anr")) {
                        Slog.d(TAG, "assert append,the tag is  :: " + tag);
                        ASSERT.epitaph(logFiles[i], tag, flags, getContext());
                    }
                }
                if (getNetworkAccess(getContext())) {
                    if (tag.startsWith("system_server", 0) && !tag.startsWith("system_server_wtf", 0) && !tag.startsWith("system_server_lowmem", 0) && !tag.startsWith("system_server_strictmode", 0)) {
                        Slog.d(TAG, "save crash file!");
                        SystemProperties.set("persist.sys.panic.file", "/data/system/dropbox/" + logFiles[i].getName());
                    } else if (tag.equals("SYSTEM_SERVER") || tag.equals("SYSTEM_LAST_KMSG") || tag.equals("SYSTEM_TOMBSTONE_CRASH")) {
                        Slog.d(TAG, "send feedback broadcast!,tag =" + tag);
                        if (!getContext().getPackageManager().hasSystemFeature("oppo.cta.support")) {
                            sendFeedbackBroadcast(mFiles, tag);
                        }
                    }
                }
            }
            i++;
        }
    }

    private static String addHashcodeSuffixToFileName(String path) {
        int dot;
        String[] suffixes = {".txt", ".txt.gz", ".dat.gz"};
        String hashcode = SystemProperties.get("persist.sys.dcs.hash", "null");
        StringBuilder fileNamStringBuilder = new StringBuilder();
        if (path == null) {
            return null;
        }
        for (String suffix : suffixes) {
            if (path.endsWith(suffix) && (dot = path.lastIndexOf(suffix)) > -1 && dot < path.length()) {
                fileNamStringBuilder.append(path.substring(0, dot));
                fileNamStringBuilder.append("@" + hashcode.replaceAll("\r|\n", ""));
                fileNamStringBuilder.append(suffix + ".en");
                return fileNamStringBuilder.toString();
            }
        }
        return path;
    }

    private File getEncodeFile(boolean hasFile, File dir, String fileName) {
        if (hasFile) {
            return new File(dir, addHashcodeSuffixToFileName(fileName));
        }
        return null;
    }

    public void deleteFile(boolean hasFile, File dir, String fileName) {
        if (getEncodeFile(hasFile, dir, fileName) != null) {
            getEncodeFile(hasFile, dir, fileName).delete();
        }
    }

    private void sendFeedbackBroadcast(ArrayList<String> files, String tag) {
        String packageName = null;
        try {
            if (this.mOppoActMgr == null) {
                this.mOppoActMgr = new OppoActivityManager();
            }
            ComponentName topCompName = this.mOppoActMgr.getTopActivityComponentName();
            if (topCompName != null) {
                packageName = topCompName.getPackageName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!"com.nearme.feedback".equals(packageName)) {
            try {
                String dumpEnvironmentGzFile = "/data/oppo/log/DCS/de/minidump/" + tag + "@" + UUID.randomUUID().toString().replace("-", "").substring(0, 15) + "@" + SystemProperties.get("ro.build.version.ota") + "@" + System.currentTimeMillis() + ".dat.gz";
                String currentFile = files.get(0);
                Slog.d(TAG, "prepare zip dumpEnvironmentGzFile!" + currentFile);
                File dumpDir = new File("/cache/environment");
                if (!tag.equals("SYSTEM_LAST_KMSG") && dumpDir.exists() && dumpDir.isDirectory()) {
                    Slog.d(TAG, "start zip dumpEnvironmentGzFile!");
                    zipFolder("/cache/environment", currentFile, "/data/oppo/log/DCS/de/minidump/environment.zip");
                    gzipFile("/data/oppo/log/DCS/de/minidump/environment.zip", "/data/oppo/log/DCS/de/minidump/feedbacktempfile.dat.gz");
                    if (new File("/data/oppo/log/DCS/de/minidump/feedbacktempfile.dat.gz").exists() && new File("/data/oppo/log/DCS/de/minidump/feedbacktempfile.dat.gz").renameTo(new File(dumpEnvironmentGzFile))) {
                        Slog.d(TAG, "start send dumpEnvironmentGzFile!");
                        files.clear();
                        files.add(dumpEnvironmentGzFile);
                        new File(currentFile).delete();
                        SystemProperties.set("sys.backup.minidump.tag", tag);
                        SystemProperties.set("ctl.start", "generate_runtime_prop");
                    }
                    new File("/data/oppo/log/DCS/de/minidump/environment.zip").delete();
                    deleteFolder("/cache/environment");
                    deleteFolder("/data/system/dropbox/extra_log");
                }
            } catch (Exception e2) {
                Slog.e(TAG, "dumpEnvironmentGzFile failed!");
                e2.printStackTrace();
            }
            Intent dropboxIntent = new Intent("com.nearme.feedback.feedback");
            dropboxIntent.addFlags(32);
            dropboxIntent.putStringArrayListExtra("filePath", files);
            dropboxIntent.putExtra("boot_reason", "system");
            getContext().sendBroadcastAsUser(dropboxIntent, UserHandle.CURRENT);
        }
    }

    public static boolean getNetworkAccess(Context ctx) {
        int val = -1;
        try {
            val = Settings.System.getInt(ctx.getContentResolver(), OPPO_CTA_USER_ECPERIENCE);
        } catch (Settings.SettingNotFoundException e) {
            Slog.e(TAG, "get oppo_cta_user_experience FAIL!!");
        }
        return val == 1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0088 A[Catch:{ IOException -> 0x00ba, all -> 0x00b8 }, LOOP:1: B:15:0x0081->B:17:0x0088, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x008c A[EDGE_INSN: B:52:0x008c->B:18:0x008c ?: BREAK  , SYNTHETIC] */
    private static void zipFolder(String inputFolderPath, String currentFilePath, String outZipPath) {
        StringBuilder sb;
        FileInputStream fis;
        int length;
        ZipOutputStream zos = null;
        FileInputStream fis2 = null;
        try {
            ZipOutputStream zos2 = new ZipOutputStream(new FileOutputStream(outZipPath));
            File srcFile = new File(inputFolderPath);
            File[] files = srcFile.listFiles();
            Slog.d(TAG, "Zip directory: " + srcFile.getName());
            for (int i = 0; i < files.length + 1; i++) {
                byte[] buffer = new byte[1024];
                if (i == files.length) {
                    File currentFile = new File(currentFilePath);
                    if (currentFile.canRead()) {
                        fis = new FileInputStream(currentFile);
                        zos2.putNextEntry(new ZipEntry(currentFile.getName()));
                        while (true) {
                            length = fis.read(buffer);
                            if (length > 0) {
                                break;
                            }
                            zos2.write(buffer, 0, length);
                        }
                        zos2.closeEntry();
                        fis.close();
                        fis2 = null;
                    }
                } else if (files[i].canRead()) {
                    fis = new FileInputStream(files[i]);
                    zos2.putNextEntry(new ZipEntry(files[i].getName()));
                    while (true) {
                        length = fis.read(buffer);
                        if (length > 0) {
                        }
                        zos2.write(buffer, 0, length);
                    }
                    zos2.closeEntry();
                    fis.close();
                    fis2 = null;
                }
            }
            try {
                zos2.close();
                if (fis2 != null) {
                    fis2.close();
                }
            } catch (Exception e) {
                e = e;
                sb = new StringBuilder();
                sb.append("zos.close() error ");
                sb.append(e.getMessage());
                Slog.e(TAG, sb.toString());
            }
        } catch (IOException ioe) {
            Slog.e(TAG, ioe.getMessage());
            if (0 != 0) {
                try {
                    zos.close();
                } catch (Exception e2) {
                    e = e2;
                    sb = new StringBuilder();
                    sb.append("zos.close() error ");
                    sb.append(e.getMessage());
                    Slog.e(TAG, sb.toString());
                }
            }
            if (0 != 0) {
                fis2.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    zos.close();
                } catch (Exception e3) {
                    Slog.e(TAG, "zos.close() error " + e3.getMessage());
                    throw th;
                }
            }
            if (0 != 0) {
                fis2.close();
            }
            throw th;
        }
    }

    public void gzipFile(String source_filepath, String destinaton_zip_filepath) {
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

    private void deleteFolder(String path) {
        File temp;
        try {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                String[] fileList = file.list();
                for (int i = 0; i < fileList.length; i++) {
                    if (path.endsWith(File.separator)) {
                        temp = new File(path + fileList[i]);
                    } else {
                        temp = new File(path + File.separator + fileList[i]);
                    }
                    if (temp.isFile()) {
                        temp.delete();
                    }
                }
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

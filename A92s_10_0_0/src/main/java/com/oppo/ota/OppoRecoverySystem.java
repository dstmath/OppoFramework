package com.oppo.ota;

import android.Manifest;
import android.content.Context;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class OppoRecoverySystem {
    private static final int REAL_PATH_START_NUM = 4;
    private static final String TAG = "OppoRecoverySystem";
    private static File sCOMMANDFILE = new File(sRECOVERYDIR, "command");
    private static File sLOGFILE = new File(sRECOVERYDIR, "log");
    private static File sRECOVERYDIR = new File("/cache/recovery");

    public static void installOppoOtaPackage(Context context, ArrayList<File> packageFileList) throws IOException {
        if (packageFileList == null) {
            Log.e(TAG, "installOppoOtaPackage failed before reboot, packageFileList is null!!!");
        } else {
            RecoverySystem.installPackage(context, packageFileList.get(0));
        }
    }

    /* JADX INFO: finally extract failed */
    public static void installOppoOtaPackageSpecial(Context context, ArrayList<File> packageFileList) throws IOException {
        context.enforceCallingOrSelfPermission(Manifest.permission.RECOVERY, null);
        if (packageFileList == null) {
            Log.e(TAG, "installOppoOtaPackage failed before reboot, packageFileList is null!!!");
            return;
        }
        sRECOVERYDIR.mkdirs();
        sCOMMANDFILE.delete();
        sLOGFILE.delete();
        FileWriter command = new FileWriter(sCOMMANDFILE);
        boolean bWipeData = false;
        try {
            Iterator<File> it = packageFileList.iterator();
            while (it.hasNext()) {
                String filename = it.next().getCanonicalPath();
                if (filename == null || !filename.equals("/--wipe_data")) {
                    Log.i(TAG, "filename=" + filename);
                    if (filename != null && filename.startsWith("/mnt")) {
                        filename = filename.substring(4);
                    }
                    Log.w(TAG, "!!! REBOOT TO INSTALL " + filename + " !!!");
                    if (filename != null) {
                        command.write("--special_update_package=" + filename);
                        command.write("\n");
                    }
                } else {
                    bWipeData = true;
                }
            }
            if (bWipeData) {
                Log.w(TAG, "!!!WIPE DATA FOR OTA!!!");
                command.write("--wipe_data");
                command.write("\n");
            }
            command.close();
            ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot("recovery");
            throw new IOException("Reboot failed (no permissions?)");
        } catch (Throwable th) {
            command.close();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public static void oppoInstallPackageSpecial(Context context, File packageFile) throws IOException {
        context.enforceCallingOrSelfPermission(Manifest.permission.RECOVERY, null);
        String filename = packageFile.getCanonicalPath();
        FileWriter uncryptFile = new FileWriter(new File(sRECOVERYDIR, "uncrypt_file"));
        try {
            uncryptFile.write(filename + "\n");
            uncryptFile.close();
            Log.w(TAG, "!!! REBOOTING TO INSTALL " + filename + " !!!");
            StringBuilder sb = new StringBuilder();
            sb.append("--special_update_package=");
            sb.append(filename);
            String filenameArg = sb.toString();
            bootCommand(context, filenameArg, "--locale=" + Locale.getDefault().toString());
        } catch (Throwable th) {
            uncryptFile.close();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public static void oppoInstallPackageList(Context context, ArrayList<File> packageFileList) throws IOException {
        if (packageFileList == null) {
            Log.e(TAG, "oppoInstallPackageList failed before reboot, packageFileList is null!!!");
            return;
        }
        sRECOVERYDIR.mkdirs();
        sCOMMANDFILE.delete();
        sLOGFILE.delete();
        FileWriter command = new FileWriter(sCOMMANDFILE);
        try {
            Iterator<File> it = packageFileList.iterator();
            while (it.hasNext()) {
                String filename = it.next().getCanonicalPath();
                Log.w(TAG, "!!! REBOOT TO INSTALL " + filename + " !!!");
                if (!TextUtils.isEmpty(filename)) {
                    command.write("--special_update_package=" + filename);
                    command.write("\n");
                }
                String localeArg = "--locale=" + Locale.getDefault().toString();
                if (!TextUtils.isEmpty(localeArg)) {
                    command.write(localeArg);
                    command.write("\n");
                }
            }
            command.close();
            ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot("recovery");
            throw new IOException("Reboot failed (no permissions?)");
        } catch (Throwable th) {
            command.close();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    private static void bootCommand(Context context, String... args) throws IOException {
        context.enforceCallingOrSelfPermission(Manifest.permission.RECOVERY, null);
        sRECOVERYDIR.mkdirs();
        sCOMMANDFILE.delete();
        sLOGFILE.delete();
        FileWriter command = new FileWriter(sCOMMANDFILE);
        try {
            for (String arg : args) {
                if (!TextUtils.isEmpty(arg)) {
                    command.write(arg);
                    command.write("\n");
                }
            }
            command.close();
            ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot("recovery");
            throw new IOException("Reboot failed (no permissions?)");
        } catch (Throwable th) {
            command.close();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public static void installOppoSauPackageSpecial(Context context, ArrayList<File> packageFileList) throws IOException {
        context.enforceCallingOrSelfPermission(Manifest.permission.RECOVERY, null);
        if (packageFileList == null) {
            Log.e(TAG, "installOppoSauPackageSpecial failed before reboot, packageFileList is null!!!");
            return;
        }
        sRECOVERYDIR.mkdirs();
        sCOMMANDFILE.delete();
        sLOGFILE.delete();
        FileWriter command = new FileWriter(sCOMMANDFILE);
        boolean bWipeData = false;
        try {
            Iterator<File> it = packageFileList.iterator();
            while (it.hasNext()) {
                String filename = it.next().getCanonicalPath();
                if (filename == null || !filename.equals("/--wipe_data")) {
                    Log.i(TAG, "filename=" + filename);
                    if (filename != null && filename.startsWith("/mnt")) {
                        filename = filename.substring(4);
                    }
                    Log.w(TAG, "!!! REBOOT TO INSTALL " + filename + " !!!");
                    if (filename != null) {
                        command.write("--special_update_package=" + filename);
                        command.write("\n");
                    }
                } else {
                    bWipeData = true;
                }
            }
            if (bWipeData) {
                Log.w(TAG, "!!!WIPE DATA FOR OTA!!!");
                command.write("--wipe_data");
                command.write("\n");
            }
            command.close();
            ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot("sau");
            throw new IOException("Reboot failed (no permissions?)");
        } catch (Throwable th) {
            command.close();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public static void installOppoSauPackage(Context context, ArrayList<File> packageFileList) throws IOException {
        context.enforceCallingOrSelfPermission(Manifest.permission.RECOVERY, null);
        if (packageFileList == null) {
            Log.e(TAG, "installOppoSauPackage failed before reboot, packageFileList is null!!!");
            return;
        }
        sRECOVERYDIR.mkdirs();
        sCOMMANDFILE.delete();
        sLOGFILE.delete();
        FileWriter command = new FileWriter(sCOMMANDFILE);
        boolean bWipeData = false;
        try {
            Iterator<File> it = packageFileList.iterator();
            while (it.hasNext()) {
                String filename = it.next().getCanonicalPath();
                if (filename == null || !filename.equals("/--wipe_data")) {
                    Log.i(TAG, "installOppoSauPackage filename=" + filename);
                    if (filename != null && filename.startsWith("/mnt")) {
                        filename = filename.substring(4);
                    }
                    Log.w(TAG, "!!! REBOOT TO INSTALL " + filename + " !!!");
                    if (filename != null) {
                        command.write("--update_package=" + filename);
                        command.write("\n");
                    }
                } else {
                    bWipeData = true;
                }
            }
            if (bWipeData) {
                Log.w(TAG, "!!!WIPE DATA FOR OTA!!!");
                command.write("--wipe_data");
                command.write("\n");
            }
            command.close();
            ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot("sau");
            throw new IOException("Reboot failed (no permissions?)");
        } catch (Throwable th) {
            command.close();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public static void restoreOppoModem(Context context, String imagePath, int type) throws IOException {
        context.enforceCallingOrSelfPermission(Manifest.permission.RECOVERY, null);
        if (imagePath == null) {
            Log.e(TAG, "restoreOppoModem failed before reboot, packageFileList is null!!!");
            return;
        }
        sRECOVERYDIR.mkdirs();
        sCOMMANDFILE.delete();
        sLOGFILE.delete();
        FileWriter command = new FileWriter(sCOMMANDFILE);
        try {
            String filename = new File(imagePath).getCanonicalPath();
            if (filename != null && filename.startsWith("/mnt")) {
                filename = filename.substring(4);
            }
            Log.w(TAG, "!!! REBOOT TO RESTORE " + filename + " !!!");
            if (filename != null) {
                command.write("--restore_modem=" + filename);
                command.write("\n");
            }
            command.close();
            ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot("recovery");
            throw new IOException("Reboot failed (no permissions?)");
        } catch (Throwable th) {
            command.close();
            throw th;
        }
    }
}

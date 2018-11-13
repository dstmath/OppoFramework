package com.oppo.ota;

import android.content.Context;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class OppoRecoverySystem extends RecoverySystem {
    private static final int REAL_PATH_START_NUM = 4;
    private static final String TAG = "OppoRecoverySystem";
    private static File sCOMMANDFILE = new File(sRECOVERYDIR, "command");
    private static File sLOGFILE = new File(sRECOVERYDIR, "log");
    private static File sRECOVERYDIR = new File("/cache/recovery");

    public static void installOppoOtaPackage(Context context, ArrayList<File> packageFileList) throws IOException {
        if (packageFileList == null) {
            Log.e(TAG, "installOppoOtaPackage failed before reboot, packageFileList is null!!!");
        } else {
            installPackage(context, (File) packageFileList.get(0));
        }
    }

    public static void installOppoOtaPackageSpecial(Context context, ArrayList<File> packageFileList) throws IOException {
        context.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
        if (packageFileList == null) {
            Log.e(TAG, "installOppoOtaPackage failed before reboot, packageFileList is null!!!");
            return;
        }
        sRECOVERYDIR.mkdirs();
        sCOMMANDFILE.delete();
        sLOGFILE.delete();
        FileWriter command = new FileWriter(sCOMMANDFILE);
        String strWipeData = "--wipe_data";
        boolean bWipeData = false;
        try {
            for (File packageFile : packageFileList) {
                String filename = packageFile.getCanonicalPath();
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
                command.write(strWipeData);
                command.write("\n");
            }
            command.close();
            ((PowerManager) context.getSystemService("power")).reboot("recovery");
            throw new IOException("Reboot failed (no permissions?)");
        } catch (Throwable th) {
            command.close();
        }
    }

    public static void oppoInstallPackageSpecial(Context context, File packageFile) throws IOException {
        context.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
        String filename = packageFile.getCanonicalPath();
        FileWriter uncryptFile = new FileWriter(new File(sRECOVERYDIR, "uncrypt_file"));
        try {
            uncryptFile.write(filename + "\n");
            Log.w(TAG, "!!! REBOOTING TO INSTALL " + filename + " !!!");
            String filenameArg = "--special_update_package=" + filename;
            String localeArg = "--locale=" + Locale.getDefault().toString();
            bootCommand(context, filenameArg, localeArg);
        } finally {
            uncryptFile.close();
        }
    }

    private static void bootCommand(Context context, String... args) throws IOException {
        context.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
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
            ((PowerManager) context.getSystemService("power")).reboot("recovery");
            throw new IOException("Reboot failed (no permissions?)");
        } finally {
            command.close();
        }
    }

    public static void installOppoSauPackageSpecial(Context context, ArrayList<File> packageFileList) throws IOException {
        context.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
        if (packageFileList == null) {
            Log.e(TAG, "installOppoSauPackageSpecial failed before reboot, packageFileList is null!!!");
            return;
        }
        sRECOVERYDIR.mkdirs();
        sCOMMANDFILE.delete();
        sLOGFILE.delete();
        FileWriter command = new FileWriter(sCOMMANDFILE);
        String strWipeData = "--wipe_data";
        boolean bWipeData = false;
        try {
            for (File packageFile : packageFileList) {
                String filename = packageFile.getCanonicalPath();
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
                command.write(strWipeData);
                command.write("\n");
            }
            command.close();
            ((PowerManager) context.getSystemService("power")).reboot("sau");
            throw new IOException("Reboot failed (no permissions?)");
        } catch (Throwable th) {
            command.close();
        }
    }

    public static void installOppoSauPackage(Context context, ArrayList<File> packageFileList) throws IOException {
        context.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
        if (packageFileList == null) {
            Log.e(TAG, "installOppoSauPackage failed before reboot, packageFileList is null!!!");
            return;
        }
        sRECOVERYDIR.mkdirs();
        sCOMMANDFILE.delete();
        sLOGFILE.delete();
        FileWriter command = new FileWriter(sCOMMANDFILE);
        String strWipeData = "--wipe_data";
        boolean bWipeData = false;
        try {
            for (File packageFile : packageFileList) {
                String filename = packageFile.getCanonicalPath();
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
                command.write(strWipeData);
                command.write("\n");
            }
            command.close();
            ((PowerManager) context.getSystemService("power")).reboot("sau");
            throw new IOException("Reboot failed (no permissions?)");
        } catch (Throwable th) {
            command.close();
        }
    }

    public static void restoreOppoModem(Context context, String imagePath, int type) throws IOException {
        context.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
        if (imagePath == null) {
            Log.e(TAG, "restoreOppoModem failed before reboot, packageFileList is null!!!");
            return;
        }
        sRECOVERYDIR.mkdirs();
        sCOMMANDFILE.delete();
        sLOGFILE.delete();
        FileWriter command = new FileWriter(sCOMMANDFILE);
        String strWipeData = "--wipe_data";
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
            ((PowerManager) context.getSystemService("power")).reboot("recovery");
            throw new IOException("Reboot failed (no permissions?)");
        } catch (Throwable th) {
            command.close();
        }
    }
}

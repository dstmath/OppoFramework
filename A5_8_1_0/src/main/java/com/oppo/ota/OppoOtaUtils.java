package com.oppo.ota;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Slog;
import com.oppo.content.OppoIntent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@OppoHook(level = OppoHookType.NEW_CLASS, note = "ZhiYong.Lin@Plf.Framework add for ota update result ", property = OppoRomType.ROM)
public class OppoOtaUtils {
    private static final String OTA_UPDATE_FAILED = "1";
    private static final String OTA_UPDATE_OK = "0";
    private static final String RECOVER_UPDATE_FAILED = "3";
    private static final String RECOVER_UPDATE_OK = "2";
    private static final String TAG = "OppoOtaUtils";

    public static void notifyOTAUpdateResult(Context context) {
        String otaResultStr;
        boolean isSauUpdate = false;
        String lastInstallPath = "/cache/recovery/last_install";
        if (new File(lastInstallPath).exists()) {
            otaResultStr = readOTAUpdateResult(lastInstallPath);
            if (otaResultStr != null && otaResultStr.contains("/.SAU/zip/")) {
                isSauUpdate = true;
            }
        }
        String otaFilePath = "/cache/recovery/intent";
        File file = new File(otaFilePath);
        Slog.d(TAG, "check /cache/recovery/intent");
        if (file.exists()) {
            Slog.i(TAG, "/cache/recovery/intent file is exist!!!");
            otaResultStr = readOTAUpdateResult(otaFilePath);
            Intent otaIntent;
            if (OTA_UPDATE_OK.equals(otaResultStr)) {
                Slog.i(TAG, "OTA update successed!!!");
                otaIntent = new Intent(isSauUpdate ? OppoIntent.ACTION_SAU_UPDATE_SUCCESSED : OppoIntent.ACTION_OPPO_OTA_UPDATE_SUCCESSED);
                otaIntent.addFlags(16777216);
                context.sendBroadcast(otaIntent);
                SystemProperties.set("persist.sys.panictime", Integer.toString(0));
            } else if (OTA_UPDATE_FAILED.equals(otaResultStr)) {
                Slog.i(TAG, "OTA update failed!!!");
                otaIntent = new Intent(isSauUpdate ? OppoIntent.ACTION_SAU_UPDATE_FAILED : OppoIntent.ACTION_OPPO_OTA_UPDATE_FAILED);
                otaIntent.addFlags(16777216);
                sendOTAFailLogIntent(context, otaIntent);
            } else if (RECOVER_UPDATE_OK.equals(otaResultStr)) {
                Slog.i(TAG, "Recover update ok!!!");
                otaIntent = new Intent(OppoIntent.ACTION_OPPO_RECOVER_UPDATE_SUCCESSED);
                otaIntent.addFlags(16777216);
                context.sendBroadcast(otaIntent);
            } else if (RECOVER_UPDATE_FAILED.equals(otaResultStr)) {
                Slog.i(TAG, "Recover update failed!!!");
                otaIntent = new Intent(OppoIntent.ACTION_OPPO_RECOVER_UPDATE_FAILED);
                otaIntent.addFlags(16777216);
                sendOTAFailLogIntent(context, otaIntent);
            } else {
                Slog.i(TAG, "OTA update file's date is invalid!!!");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x021a A:{SYNTHETIC, Splitter: B:38:0x021a} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0232 A:{SYNTHETIC, Splitter: B:44:0x0232} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void sendOTAFailLogIntent(Context context, Intent otaIntent) {
        IOException e;
        Throwable th;
        String updatePackageNotExist = "ERROR: Open file";
        String signVerifiFailed = "signature verification failed";
        String fileNotMatch = "has unexpected contents";
        String cacheNotEnough = "Not enough free space on";
        String decryptFail = "decryptFile file fail, stop install";
        File otaLogfile = new File("/cache/recovery/last_log");
        Slog.d(TAG, "check /cache/recovery/last_log");
        if (otaLogfile.exists()) {
            Slog.i(TAG, "/cache/recovery/last_log file is exist!!!");
            BufferedReader reader = null;
            boolean hasSendIntent = false;
            try {
                String resultStr;
                BufferedReader bufferedReader = new BufferedReader(new FileReader(otaLogfile));
                do {
                    try {
                        resultStr = bufferedReader.readLine();
                        if (resultStr == null) {
                            break;
                        } else if (resultStr.contains(updatePackageNotExist)) {
                            Slog.i(TAG, "update package not found!!!!!!");
                            otaIntent.putExtra("errType", -1);
                            otaIntent.putExtra("errLine", resultStr);
                            context.sendBroadcast(otaIntent);
                            Slog.i(TAG, "error log is \"" + resultStr + "\"");
                            hasSendIntent = true;
                            break;
                        } else if (resultStr.contains(signVerifiFailed)) {
                            Slog.i(TAG, "signature verification failed!!!!!!");
                            otaIntent.putExtra("errType", -2);
                            otaIntent.putExtra("errLine", resultStr);
                            context.sendBroadcast(otaIntent);
                            Slog.i(TAG, "error log is \"" + resultStr + "\"");
                            hasSendIntent = true;
                            break;
                        } else if (resultStr.contains(fileNotMatch)) {
                            Slog.i(TAG, "some file not match original!!!!!!");
                            otaIntent.putExtra("errType", -3);
                            otaIntent.putExtra("errLine", resultStr);
                            context.sendBroadcast(otaIntent);
                            Slog.i(TAG, "error log is \"" + resultStr + "\"");
                            hasSendIntent = true;
                            break;
                        } else if (resultStr.contains(cacheNotEnough)) {
                            Slog.i(TAG, "cache have no enough space!!!!!!");
                            otaIntent.putExtra("errType", -4);
                            otaIntent.putExtra("errLine", resultStr);
                            context.sendBroadcast(otaIntent);
                            Slog.i(TAG, "error log is \"" + resultStr + "\"");
                            hasSendIntent = true;
                            break;
                        }
                    } catch (IOException e2) {
                        e = e2;
                        reader = bufferedReader;
                        try {
                            Slog.e(TAG, "get OTA error message failed!!!", e);
                            if (reader != null) {
                            }
                            Slog.d(TAG, "deal ota log pass!!!");
                        } catch (Throwable th2) {
                            th = th2;
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e1) {
                                    Slog.e(TAG, "read_last_log close the reader failed!!!", e1);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        reader = bufferedReader;
                        if (reader != null) {
                        }
                        throw th;
                    }
                } while (!resultStr.contains(decryptFail));
                Slog.i(TAG, "package decrypt fail!!!!!!");
                otaIntent.putExtra("errType", -5);
                otaIntent.putExtra("errLine", resultStr);
                context.sendBroadcast(otaIntent);
                Slog.i(TAG, "error log is \"" + resultStr + "\"");
                hasSendIntent = true;
                if (!hasSendIntent) {
                    context.sendBroadcast(otaIntent);
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e12) {
                        Slog.e(TAG, "read_last_log close the reader failed!!!", e12);
                    }
                }
            } catch (IOException e3) {
                e = e3;
                Slog.e(TAG, "get OTA error message failed!!!", e);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.e(TAG, "read_last_log close the reader failed!!!", e122);
                    }
                }
                Slog.d(TAG, "deal ota log pass!!!");
            }
        }
        Slog.d(TAG, "deal ota log pass!!!");
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0045 A:{SYNTHETIC, Splitter: B:22:0x0045} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String readOTAUpdateResult(String fileName) {
        IOException e;
        Throwable th;
        String resultStr = null;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            try {
                resultStr = reader2.readLine();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e1) {
                        Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e1);
                    }
                }
                reader = reader2;
            } catch (IOException e2) {
                e = e2;
                reader = reader2;
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            try {
                Slog.e(TAG, "readOTAUpdateResult failed!!!", e);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e12);
                    }
                }
                return resultStr;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e122);
                    }
                }
                throw th;
            }
        }
        return resultStr;
    }
}

package com.oppo.ota;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Slog;
import com.oppo.content.OppoIntent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class OppoOtaUtils {
    private static final String OTA_UPDATE_FAILED = "1";
    private static final String OTA_UPDATE_OK = "0";
    private static final String RECOVER_UPDATE_FAILED = "3";
    private static final String RECOVER_UPDATE_OK = "2";
    private static final String TAG = "OppoOtaUtils";

    public static void notifyOTAUpdateResult(Context context) {
        String otaResultStr;
        boolean isSauUpdate = false;
        if (new File("/cache/recovery/last_install").exists() && (otaResultStr = readOTAUpdateResult("/cache/recovery/last_install")) != null && otaResultStr.contains("/.SAU/zip/")) {
            isSauUpdate = true;
        }
        File file = new File("/cache/recovery/intent");
        Slog.d(TAG, "check /cache/recovery/intent");
        if (file.exists()) {
            Slog.i(TAG, "/cache/recovery/intent file is exist!!!");
            String otaResultStr2 = readOTAUpdateResult("/cache/recovery/intent");
            if ("0".equals(otaResultStr2)) {
                Slog.i(TAG, "OTA update successed!!!");
                Intent otaIntent = new Intent(isSauUpdate ? OppoIntent.ACTION_SAU_UPDATE_SUCCESSED : OppoIntent.ACTION_OPPO_OTA_UPDATE_SUCCESSED);
                otaIntent.addFlags(16777216);
                context.sendBroadcast(otaIntent);
                SystemProperties.set("persist.sys.panictime", Integer.toString(0));
            } else if ("1".equals(otaResultStr2)) {
                Slog.i(TAG, "OTA update failed!!!");
                Intent otaIntent2 = new Intent(isSauUpdate ? OppoIntent.ACTION_SAU_UPDATE_FAILED : OppoIntent.ACTION_OPPO_OTA_UPDATE_FAILED);
                otaIntent2.addFlags(16777216);
                sendOTAFailLogIntent(context, otaIntent2);
            } else if (RECOVER_UPDATE_OK.equals(otaResultStr2)) {
                Slog.i(TAG, "Recover update ok!!!");
                Intent otaIntent3 = new Intent(OppoIntent.ACTION_OPPO_RECOVER_UPDATE_SUCCESSED);
                otaIntent3.addFlags(16777216);
                context.sendBroadcast(otaIntent3);
            } else if (RECOVER_UPDATE_FAILED.equals(otaResultStr2)) {
                Slog.i(TAG, "Recover update failed!!!");
                Intent otaIntent4 = new Intent(OppoIntent.ACTION_OPPO_RECOVER_UPDATE_FAILED);
                otaIntent4.addFlags(16777216);
                sendOTAFailLogIntent(context, otaIntent4);
            } else {
                Slog.i(TAG, "OTA update file's date is invalid!!!");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        android.util.Slog.i(com.oppo.ota.OppoOtaUtils.TAG, "update package not found!!!!!!");
        r26.putExtra("errType", -1);
        r26.putExtra("errLine", r0);
        r25.sendBroadcast(r26);
        android.util.Slog.i(com.oppo.ota.OppoOtaUtils.TAG, "error log is \"" + r0 + "\"");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0084, code lost:
        r18 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x008d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x008e, code lost:
        r3 = r0;
        r2 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0097, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0098, code lost:
        r16 = r0;
        r2 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0140, code lost:
        android.util.Slog.i(com.oppo.ota.OppoOtaUtils.TAG, "package decrypt fail!!!!!!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        r26.putExtra("errType", r12);
        r26.putExtra("errLine", r0);
        r25.sendBroadcast(r26);
        android.util.Slog.i(com.oppo.ota.OppoOtaUtils.TAG, "error log is \"" + r0 + "\"");
        r18 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x01b8, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x01b9, code lost:
        r3 = r0;
        r2 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x01be, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x01bf, code lost:
        r16 = r0;
        r2 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:?, code lost:
        r17.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x020d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x020e, code lost:
        android.util.Slog.e(com.oppo.ota.OppoOtaUtils.TAG, r2, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:?, code lost:
        r17.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x021e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x021f, code lost:
        android.util.Slog.e(com.oppo.ota.OppoOtaUtils.TAG, r2, r0);
     */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0209 A[SYNTHETIC, Splitter:B:70:0x0209] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x021a A[SYNTHETIC, Splitter:B:77:0x021a] */
    private static void sendOTAFailLogIntent(Context context, Intent otaIntent) {
        Throwable th;
        String str;
        String str2 = "read_last_log close the reader failed!!!";
        String updatePackageNotExist = "ERROR: Open file";
        String signVerifiFailed = "signature verification failed";
        int failTypeDecryptFail = -5;
        File otaLogfile = new File("/cache/recovery/last_log");
        Slog.d(TAG, "check /cache/recovery/last_log");
        if (otaLogfile.exists()) {
            Slog.i(TAG, "/cache/recovery/last_log file is exist!!!");
            String resultStr = null;
            BufferedReader reader = null;
            boolean hasSendIntent = false;
            try {
                try {
                    reader = new BufferedReader(new FileReader(otaLogfile));
                    while (true) {
                        String resultStr2 = reader.readLine();
                        if (resultStr2 == null) {
                            str = str2;
                            break;
                        }
                        try {
                            str = str2;
                            if (resultStr2.contains(updatePackageNotExist)) {
                                break;
                            }
                            try {
                                if (resultStr2.contains(signVerifiFailed)) {
                                    Slog.i(TAG, "signature verification failed!!!!!!");
                                    otaIntent.putExtra("errType", -2);
                                    otaIntent.putExtra("errLine", resultStr2);
                                    context.sendBroadcast(otaIntent);
                                    Slog.i(TAG, "error log is \"" + resultStr2 + "\"");
                                    hasSendIntent = true;
                                    break;
                                } else if (resultStr2.contains("has unexpected contents")) {
                                    Slog.i(TAG, "some file not match original!!!!!!");
                                    otaIntent.putExtra("errType", -3);
                                    otaIntent.putExtra("errLine", resultStr2);
                                    context.sendBroadcast(otaIntent);
                                    Slog.i(TAG, "error log is \"" + resultStr2 + "\"");
                                    hasSendIntent = true;
                                    break;
                                } else if (resultStr2.contains("Not enough free space on")) {
                                    Slog.i(TAG, "cache have no enough space!!!!!!");
                                    otaIntent.putExtra("errType", -4);
                                    otaIntent.putExtra("errLine", resultStr2);
                                    context.sendBroadcast(otaIntent);
                                    Slog.i(TAG, "error log is \"" + resultStr2 + "\"");
                                    hasSendIntent = true;
                                    break;
                                } else if (resultStr2.contains("decryptFile file fail, stop install")) {
                                    break;
                                } else {
                                    failTypeDecryptFail = failTypeDecryptFail;
                                    updatePackageNotExist = updatePackageNotExist;
                                    otaLogfile = otaLogfile;
                                    str2 = str;
                                    signVerifiFailed = signVerifiFailed;
                                }
                            } catch (IOException e) {
                                e = e;
                                resultStr = resultStr2;
                                str2 = str;
                                try {
                                    Slog.e(TAG, "get OTA error message failed!!!", e);
                                    if (reader != null) {
                                    }
                                    Slog.d(TAG, "deal ota log pass!!!");
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                str2 = str;
                                if (reader != null) {
                                }
                                throw th;
                            }
                        } catch (IOException e2) {
                            e = e2;
                            resultStr = resultStr2;
                            Slog.e(TAG, "get OTA error message failed!!!", e);
                            if (reader != null) {
                            }
                            Slog.d(TAG, "deal ota log pass!!!");
                        } catch (Throwable th4) {
                            th = th4;
                            if (reader != null) {
                            }
                            throw th;
                        }
                    }
                    if (!hasSendIntent) {
                        context.sendBroadcast(otaIntent);
                    }
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        Slog.e(TAG, str, e1);
                    }
                } catch (IOException e3) {
                    e = e3;
                    Slog.e(TAG, "get OTA error message failed!!!", e);
                    if (reader != null) {
                    }
                    Slog.d(TAG, "deal ota log pass!!!");
                } catch (Throwable th5) {
                    th = th5;
                    if (reader != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e = e4;
                Slog.e(TAG, "get OTA error message failed!!!", e);
                if (reader != null) {
                }
                Slog.d(TAG, "deal ota log pass!!!");
            } catch (Throwable th6) {
                th = th6;
                if (reader != null) {
                }
                throw th;
            }
        }
        Slog.d(TAG, "deal ota log pass!!!");
    }

    private static String readOTAUpdateResult(String fileName) {
        String resultStr = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(fileName)));
            resultStr = reader.readLine();
            try {
                reader.close();
            } catch (IOException e1) {
                Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e1);
            }
        } catch (IOException e) {
            Slog.e(TAG, "readOTAUpdateResult failed!!!", e);
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e12) {
                    Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e12);
                }
            }
            throw th;
        }
        return resultStr;
    }
}

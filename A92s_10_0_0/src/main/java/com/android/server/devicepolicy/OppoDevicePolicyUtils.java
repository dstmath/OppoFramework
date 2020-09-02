package com.android.server.devicepolicy;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;
import vendor.oppo.hardware.nfc.V1_0.OppoNfcChipVersion;

public class OppoDevicePolicyUtils {
    private static final int CODE_ADB_NOT_ALLOWED = 99;
    /* access modifiers changed from: private */
    public static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "DevicePolicyManager";
    private static final String UPLOAD_LOGTAG = "20120";
    private static final String UPLOAD_LOG_EVENTID = "set_device_owner";
    private static boolean sFunSwitch = true;
    private static List<String> sSignatureMd5WhiteList = new ArrayList();

    static {
        sSignatureMd5WhiteList.add("cde9f6208d672b54b1dacc0b7029f5eb");
        sSignatureMd5WhiteList.add("e89b158e4bcf988ebd09eb83f5378e87");
        sSignatureMd5WhiteList.add("02c3b91ecb5304b2de41a6be56abf60a");
    }

    public static int checkPakcageState(Context context, String packageName) {
        int result = 0;
        if (context == null || context.getPackageManager() == null || !context.getPackageManager().hasSystemFeature("oppo.deviceowner.support")) {
            if (sFunSwitch) {
                String md5 = getAppSignatureMd5(context, packageName);
                synchronized (sSignatureMd5WhiteList) {
                    if (!sSignatureMd5WhiteList.contains(md5)) {
                        Slog.d(TAG, "set owner from adb : " + packageName + " , " + md5);
                        result = 99;
                    }
                }
                uploadStatistics(context, packageName, md5);
            }
            return result;
        }
        if (DEBUG) {
            Slog.d(TAG, "set owner from adb success");
        }
        return 0;
    }

    public static void updateWhiteList(List<String> whiteList) {
        if (whiteList != null) {
            synchronized (sSignatureMd5WhiteList) {
                if (!whiteList.contains("")) {
                    sSignatureMd5WhiteList.addAll(whiteList);
                } else {
                    sSignatureMd5WhiteList.clear();
                }
                if (DEBUG) {
                    Slog.d(TAG, "update list. , " + sSignatureMd5WhiteList);
                }
            }
        }
    }

    public static void updateSwitchState(boolean state) {
        sFunSwitch = state;
        if (DEBUG) {
            Slog.d(TAG, "update state " + state);
        }
    }

    private static String getAppSignatureMd5(Context context, String packageName) {
        try {
            return bytesToMD5(((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(context.getPackageManager().getPackageInfo(packageName, 64).signatures[0].toByteArray()))).getEncoded());
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    private static String bytesToMD5(byte[] bytes) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(bytes);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & OppoNfcChipVersion.NONE) < 16) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & OppoNfcChipVersion.NONE));
            }
            return hex.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void uploadStatistics(final Context context, final String packageName, final String md5) {
        if (context != null && packageName != null && md5 != null) {
            new Thread(new Runnable() {
                /* class com.android.server.devicepolicy.OppoDevicePolicyUtils.AnonymousClass1 */

                public void run() {
                    Map<String, String> statisticsMap = new HashMap<>();
                    statisticsMap.put("package_name", packageName);
                    statisticsMap.put("md5", md5);
                    OppoStatistics.onCommon(context, "20120", OppoDevicePolicyUtils.UPLOAD_LOG_EVENTID, statisticsMap, false);
                    if (OppoDevicePolicyUtils.DEBUG) {
                        Slog.d(OppoDevicePolicyUtils.TAG, "STS");
                    }
                }
            }).start();
        }
    }
}

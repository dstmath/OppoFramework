package com.android.server.biometrics.fingerprint.util;

import android.content.Context;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SupportUtil {
    public static final String BACK_TOUCH_SENSOR = "oppo.back.touch.fingerprint.sensor";
    private static final String FINGERPRINT_MANAGER_PACKAGE_NAME = "com.coloros.fingerprint";
    private static final String FINGERPRINT_UNLOCK = "fingerprint_unlock";
    public static final int FINGER_PROTECT_NOTREADY = 2;
    public static final int FINGER_PROTECT_TOUCH_DOWN = 1;
    public static final int FINGER_PROTECT_TOUCH_UP = 0;
    public static final String FRONT_PRESS_SENSOR = "oppo.front.press.fingerprint.sensor";
    private static final String FRONT_TCOUH_TPPROTECT = "oppo.front.touch.fingerprint.tpprotect";
    public static final String FRONT_TOUCH_SENSOR = "oppo.front.touch.fingerprint.sensor";
    public static final int MAX_TP_PROTECT_RETRYCOUNTER = 15;
    private static String TAG = "FingerprintService.SupportUtil";
    private static final String TP_PROTECT_RESULT_FILE = "/proc/touchpanel/finger_protect_result";
    private static final Uri sFingerprintUnlockSwitchUri = Uri.withAppendedPath(Uri.parse("content://com.coloros.fingerprint"), "fingerprint_preference");

    public static String getSensorType(Context context) {
        if (context.getPackageManager().hasSystemFeature(BACK_TOUCH_SENSOR)) {
            return BACK_TOUCH_SENSOR;
        }
        if (context.getPackageManager().hasSystemFeature(FRONT_TOUCH_SENSOR)) {
            return FRONT_TOUCH_SENSOR;
        }
        if (context.getPackageManager().hasSystemFeature(FRONT_PRESS_SENSOR)) {
            return FRONT_PRESS_SENSOR;
        }
        return BACK_TOUCH_SENSOR;
    }

    public static boolean isFrontTouchFingerprintTpProtect(Context context) {
        return context.getPackageManager().hasSystemFeature(FRONT_TCOUH_TPPROTECT);
    }

    public static int getTpProtectResult() {
        String resultValue = readValueFromFile(TP_PROTECT_RESULT_FILE);
        if (resultValue == null || resultValue.equals("")) {
            return 2;
        }
        return Integer.parseInt(resultValue);
    }

    private static String readValueFromFile(String filePath) {
        BufferedReader reader = null;
        String resString = "";
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(filePath)));
            while (true) {
                String tempString = reader2.readLine();
                if (tempString == null) {
                    break;
                }
                resString = resString + tempString;
            }
            reader2.close();
            try {
                reader2.close();
            } catch (IOException e) {
                LogUtil.d(TAG, "readValueFromFile failed(2) ");
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            LogUtil.d(TAG, "readValueFromFile failed(1) ");
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    LogUtil.d(TAG, "readValueFromFile failed(2) ");
                }
            }
            throw th;
        }
        return resString;
    }

    public static Uri getFingerprintUnlockSwitchUri() {
        return sFingerprintUnlockSwitchUri;
    }
}

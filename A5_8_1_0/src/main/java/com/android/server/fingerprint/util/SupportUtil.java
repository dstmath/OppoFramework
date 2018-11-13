package com.android.server.fingerprint.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import com.android.server.fingerprint.setting.FingerprintSettings;
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
        return null;
    }

    public static boolean isFrontTouchFingerprintTpProtect(Context context) {
        return context.getPackageManager().hasSystemFeature(FRONT_TCOUH_TPPROTECT);
    }

    public static int getTpProtectResult() {
        String resultValue = readValueFromFile(TP_PROTECT_RESULT_FILE);
        if (resultValue == null || (resultValue.equals("") ^ 1) == 0) {
            return 2;
        }
        return Integer.parseInt(resultValue);
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0050 A:{SYNTHETIC, Splitter: B:19:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0061 A:{SYNTHETIC, Splitter: B:25:0x0061} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String readValueFromFile(String filePath) {
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        String tempString = "";
        String resString = "";
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(filePath)));
            while (true) {
                try {
                    tempString = reader2.readLine();
                    if (tempString == null) {
                        break;
                    }
                    resString = resString + tempString;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                    try {
                        e.printStackTrace();
                        LogUtil.d(TAG, "readValueFromFile failed(1) ");
                        if (reader != null) {
                        }
                        return resString;
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e3) {
                                LogUtil.d(TAG, "readValueFromFile failed(2) ");
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            reader2.close();
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e4) {
                    LogUtil.d(TAG, "readValueFromFile failed(2) ");
                }
            }
            reader = reader2;
        } catch (IOException e5) {
            e = e5;
            e.printStackTrace();
            LogUtil.d(TAG, "readValueFromFile failed(1) ");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e6) {
                    LogUtil.d(TAG, "readValueFromFile failed(2) ");
                }
            }
            return resString;
        }
        return resString;
    }

    public static boolean isFingerprintUnlockEnabled(Context context) {
        boolean z = false;
        Context fingerAppContext = null;
        try {
            fingerAppContext = context.createPackageContext(FINGERPRINT_MANAGER_PACKAGE_NAME, 2);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (fingerAppContext == null) {
            return false;
        }
        SharedPreferences sharedPreferences = fingerAppContext.getSharedPreferences("fingerprint_preferences", 5);
        if (sharedPreferences.getBoolean(FINGERPRINT_UNLOCK, false) && sharedPreferences.getInt("fingerprint_count", 0) > 0) {
            z = true;
        }
        return z;
    }

    public static boolean isFingerprintUnlockEnabledByUri(Context context) {
        Cursor cursor = null;
        boolean isUnlockOpen = true;
        try {
            cursor = context.getContentResolver().query(sFingerprintUnlockSwitchUri, new String[]{FingerprintSettings.FINGERPRINT_UNLOCK_SWITCH}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                isUnlockOpen = cursor.getInt(0) == 1;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isUnlockOpen;
    }

    public static Uri getFingerprintUnlockSwitchUri() {
        return sFingerprintUnlockSwitchUri;
    }
}

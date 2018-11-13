package com.android.server.fingerprint.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import com.android.server.fingerprint.setting.FingerprintSettings;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SupportUtil {
    public static String BACK_TOUCH_SENSOR;
    private static String FINGERPRINT_MANAGER_PACKAGE_NAME;
    private static String FINGERPRINT_UNLOCK;
    public static int FINGER_PROTECT_NOTREADY;
    public static int FINGER_PROTECT_TOUCH_DOWN;
    public static int FINGER_PROTECT_TOUCH_UP;
    public static String FRONT_PRESS_SENSOR;
    private static String FRONT_TCOUH_TPPROTECT;
    public static String FRONT_TOUCH_SENSOR;
    public static int MAX_TP_PROTECT_RETRYCOUNTER;
    private static String TAG;
    private static String TP_PROTECT_RESULT_FILE;
    private static final Uri sFingerprintUnlockSwitchUri = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.fingerprint.util.SupportUtil.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.fingerprint.util.SupportUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.util.SupportUtil.<clinit>():void");
    }

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
        if (resultValue == null || resultValue.equals(IElsaManager.EMPTY_PACKAGE)) {
            return FINGER_PROTECT_NOTREADY;
        }
        return Integer.parseInt(resultValue);
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0050 A:{SYNTHETIC, Splitter: B:19:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0061 A:{SYNTHETIC, Splitter: B:25:0x0061} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String readValueFromFile(String filePath) {
        IOException e;
        Throwable th;
        File file = new File(filePath);
        BufferedReader reader = null;
        String tempString = IElsaManager.EMPTY_PACKAGE;
        String resString = IElsaManager.EMPTY_PACKAGE;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(file));
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
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = sFingerprintUnlockSwitchUri;
            String[] strArr = new String[1];
            strArr[0] = FingerprintSettings.FINGERPRINT_UNLOCK_SWITCH;
            cursor = contentResolver.query(uri, strArr, null, null, null);
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

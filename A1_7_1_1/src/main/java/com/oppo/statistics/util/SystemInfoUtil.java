package com.oppo.statistics.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.color.os.ColorBuild;
import java.util.regex.Pattern;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
@SuppressLint({"DefaultLocale"})
public class SystemInfoUtil {
    private static final Pattern MTK_PATTERN = null;
    private static int STATISTICS_PLATFORM_MTK = 0;
    private static int STATISTICS_PLATFORM_QUALCOMM = 0;
    public static final String SYSTEM_NAME = "Android";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.statistics.util.SystemInfoUtil.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.statistics.util.SystemInfoUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.statistics.util.SystemInfoUtil.<clinit>():void");
    }

    public static String getModel() {
        String model = AccountUtil.SSOID_DEFAULT;
        if (!isEmpty(Build.MODEL)) {
            return Build.MODEL.toUpperCase();
        }
        LogUtil.w("NearMeStatistics", "No MODEL.");
        return model;
    }

    public static int getPlatForm() {
        if (getHardware().equals("QCOM")) {
            return STATISTICS_PLATFORM_QUALCOMM;
        }
        if (MTK_PATTERN.matcher(getHardware()).find()) {
            return STATISTICS_PLATFORM_MTK;
        }
        return 0;
    }

    public static String getHardware() {
        if (!isEmpty(Build.HARDWARE)) {
            return Build.HARDWARE.toUpperCase();
        }
        LogUtil.w("NearMeStatistics", "No HARDWARE INFO.");
        return AccountUtil.SSOID_DEFAULT;
    }

    public static int getSDKVersion() {
        return VERSION.SDK_INT;
    }

    public static String getRomVersion() {
        return "" + ColorBuild.getColorOSVERSION();
    }

    public static String getLocalPhoneNO(Context context) {
        String phoneNo = AccountUtil.SSOID_DEFAULT;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
            if (isEmpty(tm.getLine1Number())) {
                return phoneNo;
            }
            return tm.getLine1Number();
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return phoneNo;
        }
    }

    public static String getOperator(Context context) {
        String operator = "";
        try {
            return ((TelephonyManager) context.getSystemService("phone")).getNetworkOperatorName();
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return operator;
        }
    }

    public static int getOperatorId(Context context) {
        String operator = getOperator(context).toLowerCase();
        if (operator.equals("中国移动") || operator.equals("china mobile") || operator.equals("chinamobile")) {
            return 0;
        }
        if (operator.equals("中国联通") || operator.equals("china unicom") || operator.equals("chinaunicom")) {
            return 1;
        }
        if (operator.equals("中国电信") || operator.equals("china net") || operator.equals("chinanet")) {
            return 2;
        }
        return 99;
    }

    public static String getMacAddress(Context context) {
        String macAddress = AccountUtil.SSOID_DEFAULT;
        try {
            WifiInfo info = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo();
            if (!isEmpty(info.getMacAddress())) {
                return info.getMacAddress();
            }
            LogUtil.w("NearMeStatistics", "NO MAC ADDRESS.");
            return macAddress;
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
            return macAddress;
        }
    }

    public static String getMobile(Context context) {
        String result = AccountUtil.SSOID_DEFAULT;
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        if (!tm.hasIccCard()) {
            return result;
        }
        result = tm.getLine1Number();
        if (TextUtils.isEmpty(result) || result.equals("null")) {
            return AccountUtil.SSOID_DEFAULT;
        }
        return result;
    }

    private static boolean isEmpty(String str) {
        if (TextUtils.isEmpty(str) || "null".equals(str)) {
            return true;
        }
        return false;
    }
}

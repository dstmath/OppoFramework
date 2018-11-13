package com.oppo.rutils;

import android.app.IActivityManager;
import android.app.OppoActivityManager;
import android.util.Log;

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
public class RUtils {
    private static String RUTILS_USED_COUNT;
    private static String TAG;
    private static IActivityManager mActivitymanager;
    private static OppoActivityManager mOppoAm;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.rutils.RUtils.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.rutils.RUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.rutils.RUtils.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.rutils.RUtils.<init>():void, dex: 
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
    public RUtils() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.rutils.RUtils.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.rutils.RUtils.<init>():void");
    }

    private static native int NativeGetPidByName(String str);

    private static native int NativeOppoRUtilsCompareSystemMD5();

    private static native int NativeOppoRutilsTestValue();

    private static native int NativeRUtilsChmod(String str, int i);

    private static native int NativeRUtilsCmd(String str);

    private static native int NativeRUtilsCmdForExternal(String str);

    private static native int NativeRUtilsModifyFileTime(String str, long j);

    private static native int NativeSetSystemProperties(String str, String str2);

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.RUtilsChmod(java.lang.String, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static int RUtilsChmod(java.lang.String r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.RUtilsChmod(java.lang.String, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.rutils.RUtils.RUtilsChmod(java.lang.String, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.RUtilsCmdForExternal(java.lang.String):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static int RUtilsCmdForExternal(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.RUtilsCmdForExternal(java.lang.String):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.rutils.RUtils.RUtilsCmdForExternal(java.lang.String):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.decreaseRutilsUsedCount():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private static void decreaseRutilsUsedCount() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.decreaseRutilsUsedCount():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.rutils.RUtils.decreaseRutilsUsedCount():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.increaseRutilsUsedCount():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private static void increaseRutilsUsedCount() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.increaseRutilsUsedCount():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.rutils.RUtils.increaseRutilsUsedCount():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.isRUtilsEnable():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private static boolean isRUtilsEnable() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.isRUtilsEnable():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.rutils.RUtils.isRUtilsEnable():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.setRutilsEnable():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private static void setRutilsEnable() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.setRutilsEnable():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.rutils.RUtils.setRutilsEnable():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.waitForRUtilsEnable():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private static boolean waitForRUtilsEnable() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.rutils.RUtils.waitForRUtilsEnable():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.rutils.RUtils.waitForRUtilsEnable():boolean");
    }

    public static int RUtilsCmd(String cmd) {
        increaseRutilsUsedCount();
        if (waitForRUtilsEnable()) {
            int result = NativeRUtilsCmd(cmd);
            decreaseRutilsUsedCount();
            return result;
        }
        decreaseRutilsUsedCount();
        return -1;
    }

    private static String getLocalSignature() {
        return "308203633082024ba00302010202040875ec17300d06092a864886f70d01010b05003062310b300906035504061302383631123010060355040813096775616e67646f6e673111300f060355040713087368656e7a68656e310e300c060355040a13056368696e61310e300c060355040b13056368696e61310c300a06035504031303726f6d301e170d3135303130373036343930325a170d3235303130343036343930325a3062310b300906035504061302383631123010060355040813096775616e67646f6e673111300f060355040713087368656e7a68656e310e300c060355040a13056368696e61310e300c060355040b13056368696e61310c300a06035504031303726f6d30820122300d06092a864886f70d01010105000382010f003082010a0282010100a4677dd7cdd8f0066c813f78e6782aaa42c0b019984d5f7ac6e69bc4ed2d128ed0b88dde7cb6ac94a1c218ec8ab62f626fb35b2cb3306ea70e277fd3a8fa4d9602db220000e724433a0b66010bce499a5d9d70849f92a9594eaf39394ba13e18b0ac882f4c6e4254e8da03446d972a0e82ffb0b84ceb97aeecbeec79762155600fa08a1d4be9643169cd8a8661ae0d86049ceda147e6ab1880c3cc8292a26fa12aac2db1da46fef1b971360c6c35a6d7e22a37d2becbf2fa69ec1d6f154f7adc348e885bf2e7cc2c3174b06fb6b751fb31fa5633316cd8fef160cf930a625de865825feb9303e81656757b5eb43047bf4617ac094266f93fb1d312f94866e5270203010001a321301f301d0603551d0e04160414bbfa177b2f1423144ab1d1c9f9c8e74a048f0319300d06092a864886f70d01010b050003820101007cc9a375d39ca81864de289ed31d97a983db62175f36f2c4d2e332086daae50a2e6df83084b78f182519e5a7c3cff6250b76f382982a9adcc3094ee1a4d5790aca709b0df3a09c9c9f38d2a2bab96150812f564a7dba13c842b46619bb59b0957508045487b7d1fccddaaedb5e93d590a22147027822d6524a2af08336c3292d10cbf69959a024118e3686238649603ed88d7156a6c76429a8bad2bb446a70f06e7b7af6be347b7d898890143c782a17d13af243855035b38630a8f0f4c2367734880b30e9ba570d739fbcecc7947432fe653be187a280a4fc0bd36261a696b84970ea17a921936a6c6feb129c0b75959549930654f48eb823dbd842b767d4a4";
    }

    public static int RUtilsGetPidByName(String processName) {
        increaseRutilsUsedCount();
        if (waitForRUtilsEnable()) {
            int result = NativeGetPidByName(processName);
            decreaseRutilsUsedCount();
            return result;
        }
        decreaseRutilsUsedCount();
        return -1;
    }

    public static int RUtilsSetSystemPropertiesStringInt(String key, int value) {
        increaseRutilsUsedCount();
        if (!waitForRUtilsEnable()) {
            decreaseRutilsUsedCount();
            return -1;
        } else if (key == null) {
            decreaseRutilsUsedCount();
            return -1;
        } else {
            int result = NativeSetSystemProperties(key, String.valueOf(value));
            decreaseRutilsUsedCount();
            return result;
        }
    }

    public static int RUtilsSetSystemPropertiesString(String key, String value) {
        increaseRutilsUsedCount();
        if (!waitForRUtilsEnable()) {
            decreaseRutilsUsedCount();
            return -1;
        } else if (key == null || value == null) {
            decreaseRutilsUsedCount();
            return -1;
        } else {
            int result = NativeSetSystemProperties(key, value);
            decreaseRutilsUsedCount();
            return result;
        }
    }

    public static int oppoRutilsTestValue() {
        increaseRutilsUsedCount();
        if (waitForRUtilsEnable()) {
            Log.e(TAG, "RUtils oppoRutilsTestValue enter!!!");
            int result = NativeOppoRutilsTestValue();
            decreaseRutilsUsedCount();
            return result;
        }
        decreaseRutilsUsedCount();
        return -1;
    }

    public static int OppoRUtilsCompareSystemMD5() {
        increaseRutilsUsedCount();
        if (waitForRUtilsEnable()) {
            Log.e(TAG, "RUtils CompareSystemMD5 enter!!!");
            int ret = 0;
            for (int i = 0; i < 2; i++) {
                ret = NativeOppoRUtilsCompareSystemMD5();
                if (ret != -1) {
                    break;
                }
            }
            decreaseRutilsUsedCount();
            return ret;
        }
        decreaseRutilsUsedCount();
        return 0;
    }

    public static int RUtilsModifyFileTime(String path, long time) {
        increaseRutilsUsedCount();
        if (waitForRUtilsEnable()) {
            int result = NativeRUtilsModifyFileTime(path, time);
            decreaseRutilsUsedCount();
            return result;
        }
        decreaseRutilsUsedCount();
        return -1;
    }
}

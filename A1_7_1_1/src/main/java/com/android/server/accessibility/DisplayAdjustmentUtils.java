package com.android.server.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.opengl.Matrix;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.android.server.LocalServices;
import com.android.server.display.DisplayTransformManager;
import com.android.server.display.OppoBrightUtils;
import com.mediatek.pq.PictureQuality;

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
class DisplayAdjustmentUtils {
    private static final int DEFAULT_DISPLAY_DALTONIZER = 12;
    private static final float[] DEFAULT_MATRIX_GRAYSCALE = null;
    private static final String DISPLAY_ADJUST_URI = "color_dispaly_adjust";
    private static final String DISPLAY_ADJUST_URI_TMP = "color_dispaly_adjust_tmp";
    private static final String EYEPROTECT_ENABLE = "color_eyeprotect_enable";
    private static final String EYEPROTECT_GRAY_ENABLE = "gray_scale_on";
    private static final String EYEPROTECT_INVERSE_ENABLE = "inverse_on";
    private static final String EYEPROTECT_NORMAL_ENABLE = "normal_on";
    private static final float[] MATRIX_GRAYSCALE = null;
    private static final float[] MATRIX_INVERT_COLOR = null;
    private static final float[] OPPO_MATRIX_INVERT_COLOR = null;
    private static final boolean PANIC = false;
    private static int SWITCH_OFF;
    private static int SWITCH_ON;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.accessibility.DisplayAdjustmentUtils.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.accessibility.DisplayAdjustmentUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.accessibility.DisplayAdjustmentUtils.<clinit>():void");
    }

    DisplayAdjustmentUtils() {
    }

    private static float[] multiply(float[] matrix, float[] other) {
        if (matrix == null) {
            return other;
        }
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, matrix, 0, other, 0);
        return result;
    }

    public static void applyDaltonizerSetting(Context context, int userId) {
        ContentResolver cr = context.getContentResolver();
        DisplayTransformManager dtm = (DisplayTransformManager) LocalServices.getService(DisplayTransformManager.class);
        int daltonizerMode = -1;
        if (Secure.getIntForUser(cr, "accessibility_display_daltonizer_enabled", 0, userId) != 0) {
            daltonizerMode = Secure.getIntForUser(cr, "accessibility_display_daltonizer", 12, userId);
        }
        float[] grayscaleMatrix = null;
        if (daltonizerMode == 0) {
            grayscaleMatrix = MATRIX_GRAYSCALE;
            daltonizerMode = -1;
        }
        dtm.setColorMatrix(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE, grayscaleMatrix);
        dtm.setDaltonizerMode(daltonizerMode);
    }

    public static void applyInversionSetting(Context context, int userId) {
        boolean invertColors = false;
        DisplayTransformManager dtm = (DisplayTransformManager) LocalServices.getService(DisplayTransformManager.class);
        if (Secure.getIntForUser(context.getContentResolver(), "accessibility_display_inversion_enabled", 0, userId) != 0) {
            invertColors = true;
        }
        dtm.setColorMatrix(300, invertColors ? MATRIX_INVERT_COLOR : null);
    }

    public static void applyAdjustValues(Context context, int userId) {
        ContentResolver cr = context.getContentResolver();
        IWindowManager windowManager = Stub.asInterface(ServiceManager.getService("window"));
        if (SWITCH_ON == System.getIntForUser(context.getContentResolver(), EYEPROTECT_ENABLE, SWITCH_OFF, userId) && SWITCH_ON == System.getInt(context.getContentResolver(), EYEPROTECT_INVERSE_ENABLE, SWITCH_OFF)) {
            try {
                windowManager.setAnimationScale(0, OppoBrightUtils.MIN_LUX_LIMITI);
                windowManager.setAnimationScale(1, OppoBrightUtils.MIN_LUX_LIMITI);
            } catch (RemoteException e) {
                Log.e("DisplayAdjustmentUtils", "RemoteException" + e);
            }
        } else {
            try {
                windowManager.setAnimationScale(0, 1.0f);
                windowManager.setAnimationScale(1, 1.0f);
            } catch (RemoteException e2) {
                Log.e("DisplayAdjustmentUtils", "RemoteException" + e2);
            }
        }
        DisplayTransformManager dtm = (DisplayTransformManager) LocalServices.getService(DisplayTransformManager.class);
        String sColorMatrix = System.getString(cr, DISPLAY_ADJUST_URI);
        String[] tempStrings = new String[16];
        float[] tempColorMatrix = new float[16];
        if (sColorMatrix != null) {
            tempStrings = sColorMatrix.split(",");
            for (int i = 0; i < tempStrings.length; i++) {
                tempColorMatrix[i] = Float.valueOf(tempStrings[i]).floatValue();
                Log.e("DisplayAdjustmentUtils", "applyAdjustValues tempStrings.values == " + tempColorMatrix[i]);
            }
            boolean enbale = SWITCH_ON == System.getIntForUser(cr, EYEPROTECT_ENABLE, SWITCH_OFF, userId);
            boolean inverseEnbale = System.getInt(cr, EYEPROTECT_INVERSE_ENABLE, SWITCH_OFF) == SWITCH_ON;
            boolean grayEnable = System.getInt(cr, EYEPROTECT_GRAY_ENABLE, SWITCH_OFF) == SWITCH_ON;
            boolean normalEnable = System.getInt(cr, EYEPROTECT_NORMAL_ENABLE, SWITCH_OFF) == SWITCH_ON;
            if (enbale) {
                if (inverseEnbale) {
                    dtm.setColorMatrix(DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR, OPPO_MATRIX_INVERT_COLOR);
                }
                if (grayEnable) {
                    dtm.setColorMatrix(DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR, DEFAULT_MATRIX_GRAYSCALE);
                }
                if (normalEnable) {
                    dtm.setColorMatrix(DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR, null);
                }
            } else {
                dtm.setColorMatrix(DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR, null);
            }
            try {
                double r_gain = (double) Math.abs(tempColorMatrix[0]);
                double g_gain = (double) Math.abs(tempColorMatrix[5]);
                double b_gain = (double) Math.abs(tempColorMatrix[10]);
                boolean b = PictureQuality.setRGBGain(r_gain, g_gain, b_gain, 1);
                System.putString(cr, DISPLAY_ADJUST_URI_TMP, sColorMatrix);
                if (PANIC) {
                    Log.i("DisplayAdjustmentUtils", "applyAdjustValues b = " + b + "  rgb(" + r_gain + "," + g_gain + "," + b_gain + ")");
                    return;
                }
                return;
            } catch (Exception e3) {
                Log.e("DisplayAdjustmentUtils", "applyAdjustValues  ERROR ERROR : " + e3.toString());
                return;
            }
        }
        PictureQuality.setRGBGain(1.0d, 1.0d, 1.0d, 1);
        System.putString(cr, DISPLAY_ADJUST_URI_TMP, "1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0");
        if (PANIC) {
            Log.i("DisplayAdjustmentUtils", "sColorMatrix == null   rgb(1, 1, 1)");
        }
        dtm.setColorMatrix(DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR, null);
    }

    public static void applyAdjustValuesRGB(Context context, int userId) {
        String sColorMatrix = System.getString(context.getContentResolver(), DISPLAY_ADJUST_URI_TMP);
        String[] tempStrings = new String[16];
        float[] tempColorMatrix = new float[16];
        if (sColorMatrix != null) {
            tempStrings = sColorMatrix.split(",");
            for (int i = 0; i < tempStrings.length; i++) {
                tempColorMatrix[i] = Float.valueOf(tempStrings[i]).floatValue();
            }
            try {
                double r_gain = (double) Math.abs(tempColorMatrix[0]);
                double g_gain = (double) Math.abs(tempColorMatrix[5]);
                double b_gain = (double) Math.abs(tempColorMatrix[10]);
                PictureQuality.setRGBGain(r_gain, g_gain, b_gain, 1);
                if (PANIC) {
                    Log.i("DisplayAdjustmentUtils", "applyAdjustValuesRGB rgb(" + r_gain + "," + g_gain + "," + b_gain + ")  sColorMatrix = " + sColorMatrix);
                    return;
                }
                return;
            } catch (Exception e) {
                Log.e("DisplayAdjustmentUtils", "applyAdjustValues  ERROR ERROR : " + e.toString());
                return;
            }
        }
        PictureQuality.setRGBGain(1.0d, 1.0d, 1.0d, 1);
    }
}

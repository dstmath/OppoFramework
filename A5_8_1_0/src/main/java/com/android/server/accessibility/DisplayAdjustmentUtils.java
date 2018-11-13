package com.android.server.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.opengl.Matrix;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.android.server.LocalServices;
import com.android.server.am.OppoProcessManager;
import com.android.server.display.DisplayTransformManager;
import com.android.server.display.OppoBrightUtils;

class DisplayAdjustmentUtils {
    private static final int DEFAULT_DISPLAY_DALTONIZER = 12;
    private static final String DISPLAY_ADJUST_URI = "color_dispaly_adjust";
    private static final String EYEPROTECT_ENABLE = "color_eyeprotect_enable";
    private static final String EYEPROTECT_INVERSE_ENABLE = "inverse_on";
    private static final float[] MATRIX_GRAYSCALE = new float[]{0.2126f, 0.2126f, 0.2126f, OppoBrightUtils.MIN_LUX_LIMITI, 0.7152f, 0.7152f, 0.7152f, OppoBrightUtils.MIN_LUX_LIMITI, 0.0722f, 0.0722f, 0.0722f, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, 1.0f};
    private static final float[] MATRIX_INVERT_COLOR = new float[]{0.402f, -0.598f, -0.599f, OppoBrightUtils.MIN_LUX_LIMITI, -1.174f, -0.174f, -1.175f, OppoBrightUtils.MIN_LUX_LIMITI, -0.228f, -0.228f, 0.772f, OppoBrightUtils.MIN_LUX_LIMITI, 1.0f, 1.0f, 1.0f, 1.0f};
    private static int SWITCH_OFF = 0;
    private static int SWITCH_ON = 1;

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
        long identity = Binder.clearCallingIdentity();
        try {
            if (Secure.getIntForUser(cr, "accessibility_display_daltonizer_enabled", 0, userId) != 0) {
                daltonizerMode = Secure.getIntForUser(cr, "accessibility_display_daltonizer", 12, userId);
            }
            Binder.restoreCallingIdentity(identity);
            float[] grayscaleMatrix = null;
            if (daltonizerMode == 0) {
                grayscaleMatrix = MATRIX_GRAYSCALE;
                daltonizerMode = -1;
            }
            dtm.setColorMatrix(200, grayscaleMatrix);
            dtm.setDaltonizerMode(daltonizerMode);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public static void applyInversionSetting(Context context, int userId) {
        ContentResolver cr = context.getContentResolver();
        DisplayTransformManager dtm = (DisplayTransformManager) LocalServices.getService(DisplayTransformManager.class);
        long identity = Binder.clearCallingIdentity();
        try {
            dtm.setColorMatrix(300, Secure.getIntForUser(cr, "accessibility_display_inversion_enabled", 0, userId) != 0 ? MATRIX_INVERT_COLOR : null);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public static void applyAdjustValues(Context context, int userId) {
        ContentResolver cr = context.getContentResolver();
        IWindowManager windowManager = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR));
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
            dtm.setColorMatrix(DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR, tempColorMatrix);
            return;
        }
        dtm.setColorMatrix(DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR, null);
    }
}

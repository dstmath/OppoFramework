package com.android.server.display.color;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.LocalServices;
import com.android.server.display.ColorDummyEyeProtectManager;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.Array;
import java.util.Arrays;

public class ColorEyeProtectManager extends ColorDummyEyeProtectManager {
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String DISPLAY_ADJUST_URI = "color_dispaly_adjust";
    private static final String EYEPROTECT_ENABLE = "color_eyeprotect_enable";
    private static final String EYEPROTECT_INVERSE_ENABLE = "inverse_on";
    private static int SWITCH_OFF = 0;
    private static int SWITCH_ON = 1;
    private static final String TAG = "ColorEyeProtectManager";
    private static volatile ColorEyeProtectManager sInstance = null;
    private final Uri mDisplayAdjustValuesUri = Settings.System.getUriFor(DISPLAY_ADJUST_URI);

    public static ColorEyeProtectManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorEyeProtectManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorEyeProtectManager();
                }
            }
        }
        return sInstance;
    }

    public boolean needResetAnimationScaleSetting(Context context, int userId) {
        if (1 == Settings.System.getIntForUser(context.getContentResolver(), EYEPROTECT_ENABLE, 0, -2) && 1 == Settings.System.getInt(context.getContentResolver(), EYEPROTECT_INVERSE_ENABLE, 0)) {
            return true;
        }
        return DEBUG;
    }

    public void setColorMatrix(int level, float[] value, Context context, DisplayTransformManager dtm) {
        if (value == null || value.length == 16) {
            SparseArray<float[]> mColorMatrix = (SparseArray) OppoMirrorDisplayTransformManager.mColorMatrix.get(dtm);
            synchronized (mColorMatrix) {
                float[] oldValue = mColorMatrix.get(level);
                if (!Arrays.equals(oldValue, value)) {
                    if (value == null) {
                        mColorMatrix.remove(level);
                    } else if (oldValue == null) {
                        mColorMatrix.put(level, Arrays.copyOf(value, value.length));
                    } else {
                        System.arraycopy(value, 0, oldValue, 0, value.length);
                    }
                    float[] computeColorResult = computeColorMatrixLocked(context, mColorMatrix);
                    if (DEBUG) {
                        if (computeColorResult == null) {
                            Log.e(TAG, "setColorMatrix: null");
                        } else {
                            StringBuilder logs = new StringBuilder();
                            int length = computeColorResult.length;
                            for (int i = 0; i < length; i++) {
                                logs.append(computeColorResult[i]);
                                if (i != length - 1) {
                                    logs.append(",");
                                }
                            }
                            Log.e(TAG, "setColorMatrix: " + logs.toString());
                        }
                    }
                    if (OppoMirrorDisplayTransformManager.applyColorMatrix != null) {
                        OppoMirrorDisplayTransformManager.applyColorMatrix.call(new Object[]{computeColorResult});
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException("Expected length: 16 (4x4 matrix), actual length: " + value.length);
    }

    private float[] computeColorMatrixLocked(Context context, SparseArray<float[]> mColorMatrix) {
        int count = mColorMatrix.size();
        if (count == 0) {
            return null;
        }
        float[][] result = (float[][]) Array.newInstance(float.class, 2, 16);
        Matrix.setIdentityM(result[0], 0);
        for (int i = 0; i < count; i++) {
            float[] rhs = mColorMatrix.valueAt(i);
            if (i == count - 1) {
                Matrix.multiplyMM(result[(i + 1) % 2], 0, rhs, 0, result[i % 2], 0);
            } else {
                Matrix.multiplyMM(result[(i + 1) % 2], 0, result[i % 2], 0, rhs, 0);
            }
        }
        return result[count % 2];
    }

    public void registerContentObserverForEyeProtect(ContentResolver contentResolver, boolean notifyForDescendants, ContentObserver observer, int userHandle) {
        if (contentResolver != null) {
            contentResolver.registerContentObserver(this.mDisplayAdjustValuesUri, notifyForDescendants, observer, userHandle);
        }
    }

    public boolean handleEyeProtectStateChange(Context context, int userId, boolean selfChange, Uri uri) {
        if (uri == null || !this.mDisplayAdjustValuesUri.equals(uri)) {
            return DEBUG;
        }
        if (DEBUG) {
            Log.e("DispalyAdjut", "display adjust the values is changed!!");
        }
        applyAdjustValues(context, userId);
        return true;
    }

    private static float[] multiply(float[] matrix, float[] other) {
        if (matrix == null) {
            return other;
        }
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, matrix, 0, other, 0);
        return result;
    }

    public void applyAdjustValues(Context context, int userId) {
        ContentResolver cr = context.getContentResolver();
        DisplayTransformManager dtm = (DisplayTransformManager) LocalServices.getService(DisplayTransformManager.class);
        String sColorMatrix = Settings.System.getStringForUser(cr, DISPLAY_ADJUST_URI, userId);
        String[] strArr = new String[16];
        float[] tempColorMatrix = new float[16];
        if (sColorMatrix != null) {
            String[] tempStrings = sColorMatrix.split(",");
            for (int i = 0; i < tempStrings.length; i++) {
                tempColorMatrix[i] = Float.valueOf(tempStrings[i]).floatValue();
            }
            setColorMatrix(400, tempColorMatrix, context, dtm);
            return;
        }
        setColorMatrix(400, null, context, dtm);
    }
}

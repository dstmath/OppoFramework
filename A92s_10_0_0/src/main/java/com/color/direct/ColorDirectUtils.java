package com.color.direct;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.color.util.ColorLog;

public class ColorDirectUtils {
    public static final boolean DBG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String TAG = "DirectService";

    public static void onFindSuccess(IColorDirectFindCallback callback, Bundle data) {
        if (callback != null) {
            try {
                callback.onDirectInfoFound(createSuccessResult(data));
            } catch (RemoteException e) {
                ColorLog.e(TAG, Log.getStackTraceString(e));
            } catch (Exception e2) {
                ColorLog.e(TAG, Log.getStackTraceString(e2));
            }
        }
    }

    public static void onFindFailed(IColorDirectFindCallback callback, String error) {
        if (callback != null) {
            try {
                callback.onDirectInfoFound(createFailedResult(error));
            } catch (RemoteException e) {
                ColorLog.e(TAG, Log.getStackTraceString(e));
            } catch (Exception e2) {
                ColorLog.e(TAG, Log.getStackTraceString(e2));
            }
        }
    }

    private static ColorDirectFindResult createSuccessResult(Bundle data) {
        ColorDirectFindResult result = new ColorDirectFindResult();
        if (data != null && !data.isEmpty()) {
            result.getBundle().putAll(data);
        }
        return result;
    }

    private static ColorDirectFindResult createFailedResult(String error) {
        ColorDirectFindResult result = new ColorDirectFindResult();
        result.getBundle().putString(ColorDirectFindResult.EXTRA_ERROR, error);
        return result;
    }
}

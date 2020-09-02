package com.android.server.biometrics.fingerprint.optical;

import android.content.Context;
import com.android.server.biometrics.fingerprint.util.LogUtil;

public class OpticalFingerprintManager {
    private static final String TAG = "FingerprintService.OpticalFingerprintManager";
    private static Object sMutex = new Object();
    private static OpticalFingerprintManager sSingleInstance;
    private Context mContext;
    private IOpticalFingerprintEventListener mListener;

    public OpticalFingerprintManager(Context context, IOpticalFingerprintEventListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public static void initOFM(Context c, IOpticalFingerprintEventListener listener) {
        getOpticalFingerprintManager(c, listener);
    }

    public static OpticalFingerprintManager getOpticalFingerprintManager(Context c, IOpticalFingerprintEventListener listener) {
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new OpticalFingerprintManager(c, listener);
            }
        }
        return sSingleInstance;
    }

    public static OpticalFingerprintManager getOpticalFingerprintManager() {
        return sSingleInstance;
    }

    public void onAppSwitch(String packageName) {
        LogUtil.d(TAG, "onAppSwitch " + packageName);
        this.mListener.onAppSwitch(packageName);
    }

    public void onAppSwitch(String prevPackageName, String nextPackageName) {
        LogUtil.d(TAG, "onAppSwitch prevPackageName:" + prevPackageName + " nextPackageName:" + nextPackageName);
        if (!"com.coloros.screenrecorder".equals(nextPackageName)) {
            if (prevPackageName == null || nextPackageName == null || !prevPackageName.equals(nextPackageName)) {
                this.mListener.onAppSwitch(prevPackageName);
            }
        }
    }
}

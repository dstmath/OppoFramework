package com.android.server.location;

import android.content.Context;
import android.util.Log;
import com.android.server.location.interfaces.IPswCoarseToFine;

public class OppoCoarseToFine implements IPswCoarseToFine {
    private static boolean DEBUG = false;
    private static final String KEY_COARSE_TO_FINE_ENABLED = "config_coarseToFineLocationEnabled";
    private static final String KEY_COARSE_TO_FINE_LIST = "config_coarseToFineLocationList";
    private static final String TAG = "OppoCoarseToFine";
    private static OppoCoarseToFine mInstall = null;
    private final Context mContext;
    private OppoLbsRomUpdateUtil mRomUpdateUtil = null;

    private OppoCoarseToFine(Context context) {
        this.mContext = context;
        this.mRomUpdateUtil = OppoLbsRomUpdateUtil.getInstall(this.mContext);
    }

    public static OppoCoarseToFine getInstall(Context context) {
        if (mInstall == null) {
            mInstall = new OppoCoarseToFine(context);
        }
        return mInstall;
    }

    public boolean isAllowCoarseToFine(String packageName) {
        if (!this.mRomUpdateUtil.getBoolean(KEY_COARSE_TO_FINE_ENABLED) || !this.mRomUpdateUtil.getStringArray(KEY_COARSE_TO_FINE_LIST).contains(packageName)) {
            return false;
        }
        if (!DEBUG) {
            return true;
        }
        Log.d(TAG, "Package : " + packageName + " is allowed to report!!");
        return true;
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }
}

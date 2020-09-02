package com.color.edgetouch;

import android.app.ColorActivityTaskManager;
import android.content.Context;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import java.util.List;
import java.util.Map;

public class ColorEdgeTouchManager {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static int EDGE_TOUCH_VERSION = 1;
    private static final String TAG = "ColorEdgeTouchManager";
    private static ColorEdgeTouchManager sInstance;
    private ColorActivityTaskManager mOAms = new ColorActivityTaskManager();

    public static ColorEdgeTouchManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorEdgeTouchManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorEdgeTouchManager();
                }
            }
        }
        return sInstance;
    }

    private ColorEdgeTouchManager() {
    }

    public boolean isSupport() {
        if (DEBUG) {
            Log.i(TAG, "isSupport");
        }
        try {
            if (this.mOAms != null) {
                return this.mOAms.isSupportEdgeTouchPrevent();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "isSupport remoteException " + e);
            e.printStackTrace();
        }
        return false;
    }

    public boolean writeParam(Context context, String scenePkg, List<String> paramCmdList) {
        if (context == null) {
            return false;
        }
        if (DEBUG) {
            Log.i(TAG, "setParam  callPkg = " + context.getPackageName() + "  scenePkg = " + scenePkg + " paramCmdList = \n" + paramCmdList);
        }
        if (paramCmdList == null || paramCmdList.isEmpty()) {
            return false;
        }
        try {
            if (this.mOAms != null) {
                return this.mOAms.writeEdgeTouchPreventParam(context.getPackageName(), scenePkg, paramCmdList);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "setParam remoteException " + e);
            e.printStackTrace();
        }
        return false;
    }

    public void setDefaultParam(Context context, List<String> paramCmdList) {
        if (context != null) {
            if (DEBUG) {
                Log.i(TAG, "setDefaultParam  callPkg = " + context.getPackageName() + "  paramCmdList = \n" + paramCmdList);
            }
            if (paramCmdList != null && !paramCmdList.isEmpty()) {
                try {
                    if (this.mOAms != null) {
                        this.mOAms.setDefaultEdgeTouchPreventParam(context.getPackageName(), paramCmdList);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "setDefaultParam remoteException " + e);
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean resetDefaultParam(Context context) {
        if (context == null) {
            return false;
        }
        if (DEBUG) {
            Log.i(TAG, "resetDefaultParam ");
        }
        try {
            if (this.mOAms != null) {
                return this.mOAms.resetDefaultEdgeTouchPreventParam(context.getPackageName());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "resetDefaultParam remoteException ");
            e.printStackTrace();
        }
        return false;
    }

    public void setRules(Context context, Map<String, List<String>> rules) {
        if (context != null) {
            if (DEBUG) {
                Log.i(TAG, "setRules " + rules);
            }
            if (rules != null && !rules.isEmpty()) {
                try {
                    if (this.mOAms != null) {
                        this.mOAms.setEdgeTouchCallRules(context.getPackageName(), rules);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "resetDefaultParam remoteException ");
                    e.printStackTrace();
                }
            }
        }
    }
}

package com.color.app;

import android.app.ColorActivityManager;
import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import com.color.app.IColorHansListener;
import java.util.ArrayList;

public class ColorHansFreezeManager {
    private static final String BUNDLE_KEY_LIST = "list";
    private static final String BUNDLE_KEY_TYPE = "type";
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String FREEZE_LEVEL = "level";
    public static final int FREEZE_LEVEL_FOUR = 4;
    public static final int FREEZE_LEVEL_ONE = 1;
    public static final int FREEZE_LEVEL_THREE = 3;
    public static final int FREEZE_LEVEL_TWO = 2;
    public static final String FREEZE_TYPE_ADD = "add";
    public static final String FREEZE_TYPE_RM = "rm";
    public static final String PACKAGE = "pkg";
    private static final String TAG = "ColorHansFreezeManager";
    public static final String UID = "uid";
    private static ColorHansFreezeManager sInstance;
    private final ArrayMap<ColorHansCallBack, ColorHansListenerDelegate> mColorHansCallBackMap = new ArrayMap<>();
    private ColorActivityManager mOAms = new ColorActivityManager();

    public interface ColorHansCallBack {
        void notifyRecordData(Bundle bundle, String str);
    }

    public static ColorHansFreezeManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorHansFreezeManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorHansFreezeManager();
                }
            }
        }
        return sInstance;
    }

    private ColorHansFreezeManager() {
    }

    public boolean registerHansListener(Context context, ColorHansCallBack callBack) {
        if (callBack == null || context == null) {
            return false;
        }
        if (DEBUG) {
            Log.i(TAG, "registerHansListener callBack = " + callBack);
        }
        synchronized (this.mColorHansCallBackMap) {
            if (this.mColorHansCallBackMap.get(callBack) != null) {
                Log.e(TAG, "already register before");
                return false;
            }
            ColorHansListenerDelegate delegate = new ColorHansListenerDelegate(callBack);
            try {
                if (this.mOAms != null) {
                    boolean result = this.mOAms.registerHansListener(context.getPackageName(), delegate);
                    if (result) {
                        this.mColorHansCallBackMap.put(callBack, delegate);
                    }
                    return result;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "registerHansListener remoteException " + e);
            }
            return true;
        }
    }

    public boolean unregisterHansListener(Context context, ColorHansCallBack callBack) {
        if (context == null || callBack == null) {
            return false;
        }
        if (DEBUG) {
            Log.i(TAG, "unregisterHansListener callBack = " + callBack);
        }
        synchronized (this.mColorHansCallBackMap) {
            ColorHansListenerDelegate delegate = this.mColorHansCallBackMap.get(callBack);
            if (delegate != null) {
                try {
                    if (this.mOAms != null) {
                        this.mColorHansCallBackMap.remove(delegate);
                        return this.mOAms.unregisterHansListener(context.getPackageName(), delegate);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "unregisterHansListener remoteException " + e);
                }
            }
            return true;
        }
    }

    public boolean setAppFreeze(Context context, ArrayList<Bundle> list, String type) {
        if (context == null || list == null || type == null) {
            return false;
        }
        if (DEBUG) {
            Log.i(TAG, "setAppFreeze list = " + list + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + type);
        }
        try {
            if (this.mOAms != null) {
                Bundle data = new Bundle();
                data.putString("type", type);
                data.putParcelableArrayList("list", list);
                return this.mOAms.setAppFreeze(context.getPackageName(), data);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "registerHansListener remoteException " + e);
        }
        return false;
    }

    private class ColorHansListenerDelegate extends IColorHansListener.Stub {
        private final ColorHansCallBack mCallBack;

        public ColorHansListenerDelegate(ColorHansCallBack callBack) {
            this.mCallBack = callBack;
        }

        @Override // com.color.app.IColorHansListener
        public void notifyRecordData(Bundle data, String configName) {
            this.mCallBack.notifyRecordData(data, configName);
        }
    }
}

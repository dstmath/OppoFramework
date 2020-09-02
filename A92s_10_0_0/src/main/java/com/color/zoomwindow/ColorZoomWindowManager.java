package com.color.zoomwindow;

import android.app.AppGlobals;
import android.app.OppoActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import com.color.app.IColorZoomWindowConfigChangedListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public class ColorZoomWindowManager {
    public static final String EXTRA_WINDOW_MODE = "extra_window_mode";
    public static final int FLAG_BUBBLE_ZOOM_WINDOW = 4;
    public static final int FLAG_CLICKED_FULL_SCREEN_BUTTON = 7;
    public static final int FLAG_CLICK_OUTSIDE_ZOOM = 5;
    public static final int FLAG_EXIT_ZOOM_BY_OTHERS = 8;
    public static final int FLAG_HIDE_ZOOM_WINDOW = 2;
    public static final int FLAG_SHOW_ZOOM_WINDOW = 1;
    public static final int FLAG_UNSUPPORT_ZOOM = 6;
    private static final String STARTSTYLE = "startStyle";
    private static final String TAG = "ColorZoomWindowManager";
    public static final int TYPE_ZOOM_APP_BLACK_LIST = 2;
    public static final int TYPE_ZOOM_APP_REPLY_LIST = 3;
    public static final int TYPE_ZOOM_APP_SUPPORT_LIST = 1;
    public static int WINDOWING_MODE_FULLSCREEN = 1;
    public static int WINDOWING_MODE_ZOOM = 100;
    public static int WINDOWING_MODE_ZOOM_LEGACY = 6;
    public static int WINDOWING_MODE_ZOOM_TO_FULLSCREEN = 101;
    private static final String ZOOM_WINDOW_APPID = "20126";
    private static final String ZOOM_WINDOW_EVENT = "zoom_window_start";
    private static final String ZOOM_WINDOW_TAG = "20126003";
    private static volatile ColorZoomWindowManager sInstance;
    private final Map<OnConfigChangedListener, IColorZoomWindowConfigChangedListener> mConfigListeners = new ArrayMap();
    private OppoActivityManager mOAms = new OppoActivityManager();

    public interface OnConfigChangedListener {
        void onConfigSwitchChanged(boolean z);

        void onConfigTypeChanged(int i);
    }

    public static ColorZoomWindowManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorZoomWindowManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorZoomWindowManager();
                }
            }
        }
        return sInstance;
    }

    private ColorZoomWindowManager() {
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void
     arg types: [android.app.Application, java.lang.String, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, int]
     candidates:
      oppo.util.OppoStatistics.onCommon(android.content.Context, int, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean, int):void
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void */
    public int startZoomWindow(Intent intent, Bundle bOptions, int userId, String callPkg) {
        try {
            Log.v(TAG, "startZoomWindow: " + intent + " callPkg: " + callPkg);
            if (bOptions != null && bOptions.getInt(EXTRA_WINDOW_MODE) == WINDOWING_MODE_ZOOM) {
                Map<String, String> enventMap = new HashMap<>();
                enventMap.put(STARTSTYLE, callPkg);
                OppoStatistics.onCommon((Context) AppGlobals.getInitialApplication(), ZOOM_WINDOW_APPID, ZOOM_WINDOW_TAG, ZOOM_WINDOW_EVENT, enventMap, false);
            }
            return this.mOAms.startZoomWindow(intent, bOptions, userId, callPkg);
        } catch (RemoteException e) {
            Log.e(TAG, "startZoomWindow remoteException ");
            e.printStackTrace();
            return -1;
        }
    }

    public boolean registerZoomWindowObserver(IColorZoomWindowObserver observer) {
        try {
            return this.mOAms.registerZoomWindowObserver(observer);
        } catch (RemoteException e) {
            Log.e(TAG, "registerZoomWindowObserver remoteException ");
            e.printStackTrace();
            return false;
        }
    }

    public boolean unregisterZoomWindowObserver(IColorZoomWindowObserver observer) {
        try {
            return this.mOAms.unregisterZoomWindowObserver(observer);
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterZoomWindowObserver remoteException ");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isSupportZoomWindowMode() {
        try {
            return this.mOAms.isSupportZoomWindowMode();
        } catch (RemoteException e) {
            Log.e(TAG, "isSupportZoomWindowMode remoteException ");
            e.printStackTrace();
            return true;
        }
    }

    public ColorZoomWindowInfo getCurrentZoomWindowState() {
        try {
            return this.mOAms.getCurrentZoomWindowState();
        } catch (RemoteException e) {
            Log.e(TAG, "getCurrentZoomWindowState remoteException ");
            e.printStackTrace();
            return null;
        }
    }

    public void setBubbleMode(boolean inBubbleMode) {
        try {
            this.mOAms.setBubbleMode(inBubbleMode);
        } catch (RemoteException e) {
            Log.e(TAG, "setBubbleMode remoteException ");
            e.printStackTrace();
        }
    }

    public void hideZoomWindow(int flag) {
        try {
            this.mOAms.hideZoomWindow(flag);
        } catch (RemoteException e) {
            Log.e(TAG, "hideZoomWindow remoteException ");
            e.printStackTrace();
        }
    }

    public List<String> getZoomAppConfigList(int type) {
        try {
            return this.mOAms.getZoomAppConfigList(type);
        } catch (RemoteException e) {
            Log.e(TAG, "hideZoomWindow remoteException ");
            e.printStackTrace();
            return new ArrayList(0);
        }
    }

    public ColorZoomWindowRUSConfig getZoomWindowConfig() {
        Log.i(TAG, "getZoomWindowConfig start");
        try {
            return this.mOAms.getZoomWindowConfig();
        } catch (RemoteException e) {
            Log.e(TAG, "getZoomWindowConfig remoteException ");
            e.printStackTrace();
            return new ColorZoomWindowRUSConfig();
        }
    }

    public void setZoomWindowConfig(ColorZoomWindowRUSConfig config) {
        Log.i(TAG, "setZoomWindowConfig start");
        try {
            this.mOAms.setZoomWindowConfig(config);
        } catch (RemoteException e) {
            Log.e(TAG, "setZoomWindowConfig remoteException ");
            e.printStackTrace();
        }
    }

    public boolean addOnConfigChangedListener(OnConfigChangedListener listener) {
        Log.i(TAG, "addOnConfigChangedListener listener = " + listener);
        synchronized (this.mConfigListeners) {
            if (this.mConfigListeners.get(listener) != null) {
                Log.i(TAG, "addOnConfigChangedListener already added before");
                return false;
            }
            OnConfigChangeListenerDelegate delegate = new OnConfigChangeListenerDelegate(listener, Looper.getMainLooper());
            try {
                if (this.mOAms != null) {
                    this.mConfigListeners.put(listener, delegate);
                    boolean addZoomWindowConfigChangedListener = this.mOAms.addZoomWindowConfigChangedListener(delegate);
                    return addZoomWindowConfigChangedListener;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "addOnConfigChangedListener remoteException ");
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean removeOnConfigChangedListener(OnConfigChangedListener listener) {
        Log.i(TAG, "removeOnConfigChangedListener listener = " + listener);
        synchronized (this.mConfigListeners) {
            IColorZoomWindowConfigChangedListener delegate = this.mConfigListeners.get(listener);
            if (delegate != null) {
                try {
                    if (this.mOAms != null) {
                        this.mConfigListeners.remove(listener);
                        boolean removeZoomWindowConfigChangedListener = this.mOAms.removeZoomWindowConfigChangedListener(delegate);
                        return removeZoomWindowConfigChangedListener;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "removeOnConfigChangedListener remoteException ");
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private class OnConfigChangeListenerDelegate extends IColorZoomWindowConfigChangedListener.Stub implements Handler.Callback {
        private static final int MSG_CONFIG_SWITCH_CHANGED = 2;
        private static final int MSG_CONFIG_TYPE_CHANGED = 1;
        private final Handler mHandler;
        private final OnConfigChangedListener mListener;

        public OnConfigChangeListenerDelegate(OnConfigChangedListener listener, Looper looper) {
            this.mListener = listener;
            this.mHandler = new Handler(looper, this);
        }

        @Override // com.color.app.IColorZoomWindowConfigChangedListener
        public void onConfigTypeChanged(int type) {
            this.mHandler.obtainMessage(1, type, 0).sendToTarget();
        }

        @Override // com.color.app.IColorZoomWindowConfigChangedListener
        public void onConfigSwitchChanged(boolean enable) {
            this.mHandler.obtainMessage(2, enable ? 1 : 0, 0).sendToTarget();
        }

        /* JADX INFO: Multiple debug info for r0v1 int: [D('enable' boolean), D('type' int)] */
        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            int i = msg.what;
            if (i != 1) {
                boolean enable = false;
                if (i != 2) {
                    return false;
                }
                if (msg.arg1 != 0) {
                    enable = true;
                }
                this.mListener.onConfigSwitchChanged(enable);
                return true;
            }
            this.mListener.onConfigTypeChanged(msg.arg1);
            return true;
        }
    }
}

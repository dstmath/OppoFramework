package com.color.app;

import android.app.ColorActivityTaskManager;
import android.content.Context;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import com.color.app.IColorAppSwitchObserver;
import java.util.Map;

public class ColorAppSwitchManager {
    public static int APP_SWITCH_VERSION = 1;
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String EXTRA_NOTIFY_TYPE = "extra_notify_type";
    public static final String EXTRA_SWITCH_INFO = "extyra_switch_info";
    public static final String INTENT_OPPO_APP_SWITCH = "oppo.intent.action.APP_SWITCH";
    public static final int NOTIFY_TYPE_ACTIVITY_ENTER = 3;
    public static final int NOTIFY_TYPE_ACTIVITY_EXIT = 4;
    public static final int NOTIFY_TYPE_APP_ENTER = 1;
    public static final int NOTIFY_TYPE_APP_EXIT = 2;
    public static final String OPPO_APP_SWITCH_SAFE_PERMISSIONS = "oppo.permission.OPPO_COMPONENT_SAFE";
    private static final String TAG = "ColorAppSwitchManager";
    private static ColorAppSwitchManager sInstance;
    private final Map<OnAppSwitchObserver, IColorAppSwitchObserver> mAppSwitchObservers = new ArrayMap();
    private ColorActivityTaskManager mOAms = new ColorActivityTaskManager();

    public interface OnAppSwitchObserver {
        void onActivityEnter(ColorAppEnterInfo colorAppEnterInfo);

        void onActivityExit(ColorAppExitInfo colorAppExitInfo);

        void onAppEnter(ColorAppEnterInfo colorAppEnterInfo);

        void onAppExit(ColorAppExitInfo colorAppExitInfo);
    }

    public static ColorAppSwitchManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorAppSwitchManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorAppSwitchManager();
                }
            }
        }
        return sInstance;
    }

    private ColorAppSwitchManager() {
    }

    public boolean registerAppSwitchObserver(Context context, OnAppSwitchObserver observer, ColorAppSwitchConfig config) {
        if (DEBUG) {
            Log.i(TAG, "registerAppSwitchObserver observer = " + observer + " config = " + config);
        }
        if (observer == null || context == null) {
            return false;
        }
        synchronized (this.mAppSwitchObservers) {
            if (this.mAppSwitchObservers.get(observer) != null) {
                Log.e(TAG, "already register before");
                return false;
            }
            if (config == null) {
                config = new ColorAppSwitchConfig();
            }
            config.observerFingerPrint = observer.hashCode();
            OnAppSwitchObserverDelegate delegate = new OnAppSwitchObserverDelegate(observer);
            try {
                if (this.mOAms != null) {
                    boolean result = this.mOAms.registerAppSwitchObserver(context.getPackageName(), delegate, config);
                    if (result) {
                        this.mAppSwitchObservers.put(observer, delegate);
                    }
                    return result;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "registerAppSwitchObserver remoteException ");
                e.printStackTrace();
            }
            return false;
        }
    }

    public boolean unregisterAppSwitchObserver(Context context, OnAppSwitchObserver observer) {
        if (DEBUG) {
            Log.i(TAG, "unRegisterAppSwitchObserver observer = " + observer);
        }
        if (observer == null || context == null) {
            return false;
        }
        synchronized (this.mAppSwitchObservers) {
            if (this.mAppSwitchObservers.get(observer) != null) {
                try {
                    if (this.mOAms != null) {
                        this.mAppSwitchObservers.remove(observer);
                        ColorAppSwitchConfig config = new ColorAppSwitchConfig();
                        config.observerFingerPrint = observer.hashCode();
                        return this.mOAms.unregisterAppSwitchObserver(context.getPackageName(), config);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "removeOnConfigChangedListener remoteException ");
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    private class OnAppSwitchObserverDelegate extends IColorAppSwitchObserver.Stub {
        private final OnAppSwitchObserver mObserver;

        public OnAppSwitchObserverDelegate(OnAppSwitchObserver observer) {
            this.mObserver = observer;
        }

        @Override // com.color.app.IColorAppSwitchObserver
        public void onActivityEnter(ColorAppEnterInfo info) {
            if (ColorAppSwitchManager.DEBUG) {
                Log.d(ColorAppSwitchManager.TAG, "onActivityEnter info = " + info);
            }
            this.mObserver.onActivityEnter(info);
        }

        @Override // com.color.app.IColorAppSwitchObserver
        public void onActivityExit(ColorAppExitInfo info) {
            if (ColorAppSwitchManager.DEBUG) {
                Log.d(ColorAppSwitchManager.TAG, "onActivityExit info = " + info);
            }
            this.mObserver.onActivityExit(info);
        }

        @Override // com.color.app.IColorAppSwitchObserver
        public void onAppEnter(ColorAppEnterInfo info) {
            if (ColorAppSwitchManager.DEBUG) {
                Log.d(ColorAppSwitchManager.TAG, "onAppEnter info = " + info);
            }
            this.mObserver.onAppEnter(info);
        }

        @Override // com.color.app.IColorAppSwitchObserver
        public void onAppExit(ColorAppExitInfo info) {
            if (ColorAppSwitchManager.DEBUG) {
                Log.d(ColorAppSwitchManager.TAG, "onAppExit info = " + info);
            }
            this.mObserver.onAppExit(info);
        }
    }
}

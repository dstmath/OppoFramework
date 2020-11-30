package android.os;

import android.app.ColorActivityTaskManager;
import android.content.Context;
import android.os.IColorKeyEventObserver;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import java.util.Map;

public class ColorKeyEventManager {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final int LISTEN_ALL_KEY_EVENT = 0;
    public static final int LISTEN_APP_SWITCH_KEY_EVENT = 4096;
    public static final int LISTEN_BACK_KEY_EVENT = 32;
    public static final int LISTEN_BRIGHTNESS_DOWN_KEY_EVENT = 32768;
    public static final int LISTEN_BRIGHTNESS_UP_KEY_EVENT = 16384;
    public static final int LISTEN_CAMERA_KEY_EVENT = 128;
    public static final int LISTEN_ENDCALL_KEY_EVENT = 65536;
    public static final int LISTEN_F4_KEY_EVENT = 64;
    public static final int LISTEN_HEADSETHOOK_KEY_EVENT = 1024;
    public static final int LISTEN_HOME_KEY_EVENT = 16;
    public static final int LISTEN_MENU_KEY_EVENT = 8;
    public static final int LISTEN_POWER_KEY_EVENT = 1;
    public static final int LISTEN_SLEEP_KEY_EVENT = 131072;
    public static final int LISTEN_VOLUME_DOWN_KEY_EVENT = 4;
    public static final int LISTEN_VOLUME_MUTE_KEY_EVENT = 2048;
    public static final int LISTEN_VOLUME_UP_KEY_EVENT = 2;
    public static final int LISTEN_WAKEUP_KEY_EVENT = 8192;
    public static final String TAG = "ColorKeyEventManager";
    private static volatile ColorKeyEventManager sInstance;
    private final Map<OnKeyEventObserver, IColorKeyEventObserver> mKeyEventObservers = new ArrayMap();
    private ColorActivityTaskManager mOAms = new ColorActivityTaskManager();
    public int mVersion = 1;

    public interface OnKeyEventObserver {
        void onKeyEvent(KeyEvent keyEvent);
    }

    public static ColorKeyEventManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorKeyEventManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorKeyEventManager();
                }
            }
        }
        return sInstance;
    }

    private ColorKeyEventManager() {
    }

    public boolean registerKeyEventObserver(Context context, OnKeyEventObserver observer, int listenFlag) {
        if (observer == null || context == null) {
            Log.e(TAG, "context is null or observer is null, registerKeyEventObserver failed.");
            return false;
        }
        if (DEBUG) {
            Log.i(TAG, "start registerKeyEventObserver, pkg: " + context.getPackageName());
        }
        synchronized (this.mKeyEventObservers) {
            if (this.mKeyEventObservers.get(observer) != null) {
                Log.e(TAG, "already registered before");
                return false;
            }
            OnKeyEventObserverDelegate delegate = new OnKeyEventObserverDelegate(observer);
            try {
                if (this.mOAms != null) {
                    boolean result = this.mOAms.registerKeyEventObserver(observer.hashCode() + context.getPackageName(), delegate, listenFlag);
                    if (result) {
                        this.mKeyEventObservers.put(observer, delegate);
                    }
                    return result;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "registerKeyEventObserver RemoteException, err: " + e);
            }
            return false;
        }
    }

    public boolean unregisterKeyEventObserver(Context context, OnKeyEventObserver observer) {
        if (observer == null || context == null) {
            Log.e(TAG, "context is null or observer is null, unregisterKeyEventObserver failed.");
            return false;
        }
        if (DEBUG) {
            Log.i(TAG, "start unregisterKeyEventObserver, pkg: " + context.getPackageName());
        }
        synchronized (this.mKeyEventObservers) {
            if (this.mKeyEventObservers.get(observer) != null) {
                try {
                    if (this.mOAms != null) {
                        this.mKeyEventObservers.remove(observer);
                        return this.mOAms.unregisterKeyEventObserver(observer.hashCode() + context.getPackageName());
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "unregisterKeyEventObserver RemoteException, err: " + e);
                }
            }
            return false;
        }
    }

    public int getVersion() {
        return this.mVersion;
    }

    private class OnKeyEventObserverDelegate extends IColorKeyEventObserver.Stub {
        private final OnKeyEventObserver mObserver;

        public OnKeyEventObserverDelegate(OnKeyEventObserver observer) {
            this.mObserver = observer;
        }

        @Override // android.os.IColorKeyEventObserver
        public void onKeyEvent(KeyEvent event) {
            this.mObserver.onKeyEvent(event);
        }
    }
}

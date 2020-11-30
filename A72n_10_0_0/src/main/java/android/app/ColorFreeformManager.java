package android.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import android.view.IWindowManager;
import com.color.app.IColorFreeformConfigChangedListener;
import java.util.List;
import java.util.Map;

public class ColorFreeformManager {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "ColorFreeformManager";
    public static final int TYPE_BLACK_PKG = 64;
    public static final int TYPE_FULLSCREEN_CPN = 16;
    public static final int TYPE_NEXT_FULLSCREEN_CPN = 32;
    public static final int TYPE_ROOT_PKG = 2;
    public static final int TYPE_SECURE_CPN = 4;
    public static final int TYPE_SPECIAL_CPN = 8;
    public static final int TYPE_SUPPORT_PKG = 1;
    private static ColorFreeformManager sColorFreeformManager;
    private final Map<OnConfigChangedListener, IColorFreeformConfigChangedListener> mConfigListeners = new ArrayMap();
    private OppoActivityManager mOAms = new OppoActivityManager();
    private IWindowManager mWms = IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));

    public interface OnConfigChangedListener {
        void onConfigSwitchChanged(boolean z);

        void onConfigTypeChanged(int i);
    }

    public static ColorFreeformManager getInstance() {
        if (sColorFreeformManager == null) {
            synchronized (ColorFreeformManager.class) {
                if (sColorFreeformManager == null) {
                    sColorFreeformManager = new ColorFreeformManager();
                }
            }
        }
        return sColorFreeformManager;
    }

    private ColorFreeformManager() {
    }

    public List<String> getFreeformConfigList(int type) {
        if (DEBUG) {
            Log.d(TAG, "getFreeformConfigList type = " + type);
        }
        OppoActivityManager oppoActivityManager = this.mOAms;
        if (oppoActivityManager == null) {
            return null;
        }
        try {
            return oppoActivityManager.getFreeformConfigList(type);
        } catch (RemoteException e) {
            Log.e(TAG, "getFreeformConfigList remote exception");
            e.printStackTrace();
            return null;
        }
    }

    public boolean isFreeformEnabled() {
        if (DEBUG) {
            Log.d(TAG, "isFreeformEnabled");
        }
        OppoActivityManager oppoActivityManager = this.mOAms;
        if (oppoActivityManager == null) {
            return false;
        }
        try {
            return oppoActivityManager.isFreeformEnabled();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addOnConfigChangedListener(OnConfigChangedListener listener) {
        if (DEBUG) {
            Log.d(TAG, "addOnConfigChangedListener listener = " + listener);
        }
        synchronized (this.mConfigListeners) {
            if (this.mConfigListeners.get(listener) != null) {
                Log.i(TAG, "addOnConfigChangedListener already added before");
                return false;
            }
            OnConfigChangeListenerDelegate delegate = new OnConfigChangeListenerDelegate(listener, Looper.getMainLooper());
            try {
                if (this.mOAms != null) {
                    this.mConfigListeners.put(listener, delegate);
                    return this.mOAms.addFreeformConfigChangedListener(delegate);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "addOnConfigChangedListener remoteException ");
                e.printStackTrace();
            }
            return false;
        }
    }

    public boolean removeOnConfigChangedListener(OnConfigChangedListener listener) {
        if (DEBUG) {
            Log.d(TAG, "removeOnConfigChangedListener listener = " + listener);
        }
        synchronized (this.mConfigListeners) {
            IColorFreeformConfigChangedListener delegate = this.mConfigListeners.get(listener);
            if (delegate != null) {
                try {
                    if (this.mOAms != null) {
                        this.mConfigListeners.remove(listener);
                        return this.mOAms.removeFreeformConfigChangedListener(delegate);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "removeOnConfigChangedListener remoteException ");
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    private class OnConfigChangeListenerDelegate extends IColorFreeformConfigChangedListener.Stub implements Handler.Callback {
        private static final int MSG_CONFIG_SWITCH_CHANGED = 2;
        private static final int MSG_CONFIG_TYPE_CHANGED = 1;
        private final Handler mHandler;
        private final OnConfigChangedListener mListener;

        public OnConfigChangeListenerDelegate(OnConfigChangedListener listener, Looper looper) {
            this.mListener = listener;
            this.mHandler = new Handler(looper, this);
        }

        @Override // com.color.app.IColorFreeformConfigChangedListener
        public void onConfigTypeChanged(int type) {
            this.mHandler.obtainMessage(1, type, 0).sendToTarget();
        }

        @Override // com.color.app.IColorFreeformConfigChangedListener
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

package android.secrecy;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.secrecy.ISecrecyServiceReceiver;
import android.util.Log;
import android.util.Slog;
import java.util.Map;

public class SecrecyManager {
    public static final int ADB_TYPE = 4;
    public static final int ALL_TYPE = 255;
    public static final int APP_TYPE = 2;
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final int LOG_TYPE = 1;
    private static final int MSG_SECRECY_STATE_CHANGED = 1;
    private static final String TAG = "SecrecyManager";
    private static final String TAG_LOG = "SecrecyLog";
    private Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private boolean mIsEncryptAdb = false;
    private boolean mIsEncryptApp = false;
    private boolean mIsEncryptLog = false;
    private Object mLock = new Object();
    private ISecrecyServiceReceiver mReceiver = new ISecrecyServiceReceiver.Stub() {
        /* class android.secrecy.SecrecyManager.AnonymousClass1 */

        @Override // android.secrecy.ISecrecyServiceReceiver
        public void onSecrecyStateChanged(Map map) {
            if (map != null) {
                SecrecyManager.this.mHandler.obtainMessage(1, 0, 0, map).sendToTarget();
            }
        }
    };
    private SecrecyStateListener mSecrecyStateListener;
    private ISecrecyService mService;

    public boolean getSecrecyState(int type) {
        if (type == 1) {
            return this.mIsEncryptLog;
        }
        if (type == 2) {
            return this.mIsEncryptApp;
        }
        if (type != 4) {
            return false;
        }
        return this.mIsEncryptAdb;
    }

    /* access modifiers changed from: private */
    public void updateSecrecyState(Map map) {
        boolean adbSecrecyState;
        boolean appSecrecyState;
        boolean logSecrecyState;
        if (!(map.get(1) == null || this.mIsEncryptLog == (logSecrecyState = ((Boolean) map.get(1)).booleanValue()))) {
            this.mIsEncryptLog = logSecrecyState;
            notifySecrecyStateChanged(1, this.mIsEncryptLog);
            Log.d(TAG, "updateSecrecyState LOG_TYPE = " + this.mIsEncryptLog);
        }
        if (!(map.get(2) == null || this.mIsEncryptApp == (appSecrecyState = ((Boolean) map.get(2)).booleanValue()))) {
            this.mIsEncryptApp = appSecrecyState;
            notifySecrecyStateChanged(2, this.mIsEncryptApp);
            Log.d(TAG, "updateSecrecyState APP_TYPE = " + this.mIsEncryptApp);
        }
        if (map.get(4) != null && this.mIsEncryptAdb != (adbSecrecyState = ((Boolean) map.get(4)).booleanValue())) {
            this.mIsEncryptAdb = adbSecrecyState;
            notifySecrecyStateChanged(4, this.mIsEncryptAdb);
            Log.d(TAG, "updateSecrecyState ADB_TYPE = " + this.mIsEncryptAdb);
        }
    }

    private class MyHandler extends Handler {
        private MyHandler(Context context) {
            super(context.getMainLooper());
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                SecrecyManager.this.updateSecrecyState((Map) msg.obj);
            }
        }
    }

    private void getSecrecyStateFromService() {
        try {
            boolean encrypt = this.mService.getSecrecyState(1);
            if (encrypt != this.mIsEncryptLog) {
                this.mIsEncryptLog = encrypt;
                notifySecrecyStateChanged(1, this.mIsEncryptLog);
            }
        } catch (RemoteException e) {
        }
        try {
            boolean encrypt2 = this.mService.getSecrecyState(2);
            if (encrypt2 != this.mIsEncryptApp) {
                this.mIsEncryptApp = encrypt2;
                notifySecrecyStateChanged(2, this.mIsEncryptApp);
            }
        } catch (RemoteException e2) {
        }
        try {
            boolean encrypt3 = this.mService.getSecrecyState(4);
            if (encrypt3 != this.mIsEncryptAdb) {
                this.mIsEncryptAdb = encrypt3;
                notifySecrecyStateChanged(4, this.mIsEncryptAdb);
            }
        } catch (RemoteException e3) {
        }
    }

    public String generateTokenFromKey() {
        try {
            return this.mService.generateTokenFromKey();
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean isKeyImported() {
        try {
            return this.mService.isKeyImported();
        } catch (RemoteException e) {
            return false;
        }
    }

    public byte[] generateCipherFromKey(int cipherLength) {
        try {
            return this.mService.generateCipherFromKey(cipherLength);
        } catch (RemoteException e) {
            return null;
        }
    }

    public SecrecyManager(Context context, ISecrecyService service) {
        this.mContext = context;
        this.mService = service;
        if (service == null) {
            Slog.e(TAG, "SecrecyService was null!");
            return;
        }
        getSecrecyStateFromService();
        try {
            service.registerSecrecyServiceReceiver(this.mReceiver);
        } catch (RemoteException e) {
        }
        this.mHandler = new MyHandler(context);
    }

    public static abstract class SecrecyStateListener {
        public void onSecrecyStateChanged(int type, boolean value) {
        }
    }

    public void setSecrecyStateListener(SecrecyStateListener listener) {
        this.mSecrecyStateListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void notifySecrecyStateChanged(int type, boolean value) {
        SecrecyStateListener secrecyStateListener = this.mSecrecyStateListener;
        if (secrecyStateListener != null) {
            secrecyStateListener.onSecrecyStateChanged(type, value);
        }
    }
}

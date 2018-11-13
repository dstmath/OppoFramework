package android.secrecy;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.secrecy.ISecrecyServiceReceiver.Stub;
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
    private static final int RC4_BOX_SIZE = 256;
    private static final String TAG = "SecrecyManager";
    private static final String TAG_LOG = "SecrecyLog";
    private Context mContext;
    private Handler mHandler;
    private boolean mIsEncryptAdb = false;
    private boolean mIsEncryptApp = false;
    private boolean mIsEncryptLog = false;
    private Object mLock = new Object();
    private byte[] mRC4Sbox;
    private byte[] mRC4SboxCopy;
    private ISecrecyServiceReceiver mReceiver = new Stub() {
        public void onSecrecyStateChanged(Map map) {
            if (map != null) {
                SecrecyManager.this.mHandler.obtainMessage(1, 0, 0, map).sendToTarget();
            }
        }
    };
    private SecrecyStateListener mSecrecyStateListener;
    private ISecrecyService mService;

    private class MyHandler extends Handler {
        /* synthetic */ MyHandler(SecrecyManager this$0, Context context, MyHandler -this2) {
            this(context);
        }

        private MyHandler(Context context) {
            super(context.getMainLooper());
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SecrecyManager.this.updateSecrecyState((Map) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public static abstract class SecrecyStateListener {
        public void onSecrecyStateChanged(int type, boolean value) {
        }
    }

    public boolean getSecrecyState(int type) {
        switch (type) {
            case 1:
                return this.mIsEncryptLog;
            case 2:
                return this.mIsEncryptApp;
            case 4:
                return this.mIsEncryptAdb;
            default:
                return false;
        }
    }

    public int logv(String tag, String msg) {
        if (this.mIsEncryptLog) {
            return Log.v(TAG_LOG, encryptMsg(tag, msg));
        }
        return Log.v(tag, msg);
    }

    public int logv(String tag, String msg, Throwable tr) {
        if (!this.mIsEncryptLog) {
            return Log.v(tag, msg, tr);
        }
        int ret = Log.v(TAG_LOG, encryptMsg(tag, msg));
        for (String s : Log.getStackTraceString(tr).split("\n")) {
            ret = Log.v(TAG_LOG, encryptMsg(tag, s));
        }
        return ret;
    }

    public int logd(String tag, String msg) {
        if (this.mIsEncryptLog) {
            return Log.d(TAG_LOG, encryptMsg(tag, msg));
        }
        return Log.d(tag, msg);
    }

    public int logd(String tag, String msg, Throwable tr) {
        if (!this.mIsEncryptLog) {
            return Log.d(tag, msg, tr);
        }
        int ret = Log.d(TAG_LOG, encryptMsg(tag, msg));
        for (String s : Log.getStackTraceString(tr).split("\n")) {
            ret = Log.d(TAG_LOG, encryptMsg(tag, s));
        }
        return ret;
    }

    public int logi(String tag, String msg) {
        if (this.mIsEncryptLog) {
            return Log.i(TAG_LOG, encryptMsg(tag, msg));
        }
        return Log.i(tag, msg);
    }

    public int logi(String tag, String msg, Throwable tr) {
        if (!this.mIsEncryptLog) {
            return Log.i(tag, msg, tr);
        }
        int ret = Log.i(TAG_LOG, encryptMsg(tag, msg));
        for (String s : Log.getStackTraceString(tr).split("\n")) {
            ret = Log.i(TAG_LOG, encryptMsg(tag, s));
        }
        return ret;
    }

    public int logw(String tag, String msg) {
        if (this.mIsEncryptLog) {
            return Log.w(TAG_LOG, encryptMsg(tag, msg));
        }
        return Log.w(tag, msg);
    }

    public int logw(String tag, String msg, Throwable tr) {
        if (!this.mIsEncryptLog) {
            return Log.w(tag, msg, tr);
        }
        int ret = Log.w(TAG_LOG, encryptMsg(tag, msg));
        for (String s : Log.getStackTraceString(tr).split("\n")) {
            ret = Log.w(TAG_LOG, encryptMsg(tag, s));
        }
        return ret;
    }

    public int logw(String tag, Throwable tr) {
        if (!this.mIsEncryptLog) {
            return Log.w(tag, tr);
        }
        int ret = 0;
        for (String s : Log.getStackTraceString(tr).split("\n")) {
            ret = Log.w(TAG_LOG, encryptMsg(tag, s));
        }
        return ret;
    }

    public int loge(String tag, String msg) {
        if (this.mIsEncryptLog) {
            return Log.e(TAG_LOG, encryptMsg(tag, msg));
        }
        return Log.e(tag, msg);
    }

    public int loge(String tag, String msg, Throwable tr) {
        if (!this.mIsEncryptLog) {
            return Log.e(tag, msg, tr);
        }
        int ret = Log.e(TAG_LOG, encryptMsg(tag, msg));
        for (String s : Log.getStackTraceString(tr).split("\n")) {
            ret = Log.e(TAG_LOG, encryptMsg(tag, s));
        }
        return ret;
    }

    public int logwtf(String tag, String msg) {
        if (this.mIsEncryptLog) {
            return Log.wtf(TAG_LOG, encryptMsg(tag, msg));
        }
        return Log.wtf(tag, msg);
    }

    public int logwtfStack(String tag, String msg) {
        if (this.mIsEncryptLog) {
            return Log.wtfStack(TAG_LOG, encryptMsg(tag, msg));
        }
        return Log.wtfStack(tag, msg);
    }

    public int logwtf(String tag, Throwable tr) {
        if (!this.mIsEncryptLog) {
            return Log.wtf(tag, tr);
        }
        int ret = 0;
        for (String s : Log.getStackTraceString(tr).split("\n")) {
            ret = Log.wtf(TAG_LOG, encryptMsg(tag, s));
        }
        return ret;
    }

    public int logwtf(String tag, String msg, Throwable tr) {
        if (!this.mIsEncryptLog) {
            return Log.wtf(tag, msg, tr);
        }
        int ret = Log.wtf(TAG_LOG, encryptMsg(tag, msg));
        for (String s : Log.getStackTraceString(tr).split("\n")) {
            ret = Log.wtf(TAG_LOG, encryptMsg(tag, s));
        }
        return ret;
    }

    String encryptMsg(String tag, String msg) {
        byte[] tagData = tag.getBytes();
        byte[] colon = new byte[]{(byte) 58, (byte) 32};
        byte[] msgData = msg.getBytes();
        synchronized (this.mLock) {
            RC4.encryptLog(this.mRC4Sbox, tagData, colon, msgData);
            System.arraycopy(this.mRC4SboxCopy, 0, this.mRC4Sbox, 0, this.mRC4Sbox.length);
        }
        return RC4.encodeLog(tagData, colon, msgData);
    }

    private void updateSecrecyState(Map map) {
        if (map.get(Integer.valueOf(1)) != null) {
            boolean logSecrecyState = ((Boolean) map.get(Integer.valueOf(1))).booleanValue();
            if (this.mIsEncryptLog != logSecrecyState) {
                this.mIsEncryptLog = logSecrecyState;
                notifySecrecyStateChanged(1, this.mIsEncryptLog);
                Log.d(TAG, "updateSecrecyState LOG_TYPE = " + this.mIsEncryptLog);
            }
        }
        if (map.get(Integer.valueOf(2)) != null) {
            boolean appSecrecyState = ((Boolean) map.get(Integer.valueOf(2))).booleanValue();
            if (this.mIsEncryptApp != appSecrecyState) {
                this.mIsEncryptApp = appSecrecyState;
                notifySecrecyStateChanged(2, this.mIsEncryptApp);
                Log.d(TAG, "updateSecrecyState APP_TYPE = " + this.mIsEncryptApp);
            }
        }
        if (map.get(Integer.valueOf(4)) != null) {
            boolean adbSecrecyState = ((Boolean) map.get(Integer.valueOf(4))).booleanValue();
            if (this.mIsEncryptAdb != adbSecrecyState) {
                this.mIsEncryptAdb = adbSecrecyState;
                notifySecrecyStateChanged(4, this.mIsEncryptAdb);
                Log.d(TAG, "updateSecrecyState ADB_TYPE = " + this.mIsEncryptAdb);
            }
        }
    }

    private void getSecrecyStateFromService() {
        boolean encrypt;
        try {
            encrypt = this.mService.getSecrecyState(1);
            if (encrypt != this.mIsEncryptLog) {
                this.mIsEncryptLog = encrypt;
                notifySecrecyStateChanged(1, this.mIsEncryptLog);
            }
        } catch (RemoteException e) {
        }
        try {
            encrypt = this.mService.getSecrecyState(2);
            if (encrypt != this.mIsEncryptApp) {
                this.mIsEncryptApp = encrypt;
                notifySecrecyStateChanged(2, this.mIsEncryptApp);
            }
        } catch (RemoteException e2) {
        }
        try {
            encrypt = this.mService.getSecrecyState(4);
            if (encrypt != this.mIsEncryptAdb) {
                this.mIsEncryptAdb = encrypt;
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

    public boolean getSecrecyKey(byte[] key) {
        System.arraycopy(this.mRC4SboxCopy, 0, key, 0, this.mRC4SboxCopy.length);
        return true;
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
        this.mRC4Sbox = new byte[256];
        try {
            service.getSecrecyKey(this.mRC4Sbox);
        } catch (RemoteException e) {
        }
        this.mRC4SboxCopy = new byte[256];
        System.arraycopy(this.mRC4Sbox, 0, this.mRC4SboxCopy, 0, this.mRC4Sbox.length);
        try {
            service.registerSecrecyServiceReceiver(this.mReceiver);
        } catch (RemoteException e2) {
        }
        this.mHandler = new MyHandler(this, context, null);
    }

    public void setSecrecyStateListener(SecrecyStateListener listener) {
        this.mSecrecyStateListener = listener;
    }

    void notifySecrecyStateChanged(int type, boolean value) {
        if (this.mSecrecyStateListener != null) {
            this.mSecrecyStateListener.onSecrecyStateChanged(type, value);
        }
    }
}

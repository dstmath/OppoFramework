package android.hardware.cabc;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.cabc.ICabcManager;

public class CabcManager {
    private static boolean DEBUG = false;
    public static final int LOCK_MODE = 9;
    public static final int OFF_MODE = 0;
    public static final int PIC_MODE = 2;
    private static final String PROP_LOG_CABC = "persist.sys.assert.panic";
    private static final String TAG = "CabcManager";
    public static final int UI_MODE = 1;
    public static final int UNLOCK_MODE = 8;
    public static final int VIDEO_MODE = 3;
    private static CabcManager mCabcManagerInstance = null;
    private int mMode = 3;
    private ICabcManager sService;

    private CabcManager() {
        init();
    }

    private void init() {
        DEBUG = SystemProperties.getBoolean(PROP_LOG_CABC, false);
        if (this.sService == null) {
            this.sService = ICabcManager.Stub.asInterface(ServiceManager.getService(Context.CABC_SERVICE));
        }
    }

    public static CabcManager getCabcManagerInstance() {
        if (mCabcManagerInstance == null) {
            mCabcManagerInstance = new CabcManager();
        }
        return mCabcManagerInstance;
    }

    public void setMode(int mode) {
        if (DEBUG) {
            Log.d(TAG, "setMode, new mode:" + mode);
        }
        try {
            if (this.sService != null) {
                this.sService.setMode(mode);
            } else {
                Log.e(TAG, "setMode failed: sService is null!");
            }
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead ?", e);
        }
    }

    public int getMode() {
        try {
            if (this.sService != null) {
                return this.sService.getMode();
            }
            Log.e(TAG, "getMode failed: sService is null!");
            return 3;
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead ?", e);
        }
    }

    public void closeCabc() {
        if (DEBUG) {
            Log.d(TAG, "closeCabc.");
        }
        try {
            if (this.sService != null) {
                this.sService.closeCabc();
            } else {
                Log.e(TAG, "closeCabc failed: sService is null!");
            }
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead ?", e);
        }
    }

    public void openCabc() {
        if (DEBUG) {
            Log.d(TAG, "openCabc.");
        }
        try {
            if (this.sService != null) {
                this.sService.openCabc();
            } else {
                Log.e(TAG, "openCabc failed: sService is null!");
            }
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead ?", e);
        }
    }

    public void isSunnyBrightnessMode(boolean enable) {
        if (DEBUG) {
            Log.d(TAG, "isSunnyBrightnessMode = " + enable);
        }
        try {
            if (this.sService != null) {
                this.sService.isSunnyBrightnessMode(enable);
            } else {
                Log.e(TAG, "lockCabcMode failed: sService is null!");
            }
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead ?", e);
        }
    }
}

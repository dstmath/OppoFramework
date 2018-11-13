package com.oppo.oiface;

import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

public class OifaceProxyUtils {
    private static final String DESCRIPTOR = "com.oppo.oiface.IOIfaceService";
    public static final int NET_OFF_WLAN = 1;
    public static final int NET_OFF_WWLAN = 3;
    public static final int NET_ON_WLAN = 0;
    public static final int NET_ON_WWLAN = 2;
    private static int NET_STATUS = 1;
    private static final int OIFACE_ON = 0;
    private static final String TAG = "OifaceUtil";
    private static final int TRANSACTION_currentNetwork = 1;
    private static final int TRANSACTION_currentPackage = 2;
    private static OifaceProxyUtils sInstance;
    private static int sOifaceProp = SystemProperties.getInt("persist.sys.oiface.enable", 0);
    private DeathRecipient mDeathRecipient = new DeathRecipient() {
        public void binderDied() {
            Log.d(OifaceProxyUtils.TAG, "OifaceProxyUtils binderDied");
            OifaceProxyUtils.this.mRemote = null;
        }
    };
    private IBinder mRemote;

    public static synchronized OifaceProxyUtils getInstance() {
        OifaceProxyUtils oifaceProxyUtils;
        synchronized (OifaceProxyUtils.class) {
            if (sInstance == null) {
                sInstance = new OifaceProxyUtils();
            }
            oifaceProxyUtils = sInstance;
        }
        return oifaceProxyUtils;
    }

    private OifaceProxyUtils() {
        connectOifaceDataService();
    }

    private IBinder connectOifaceDataService() {
        this.mRemote = ServiceManager.checkService("oiface");
        if (this.mRemote != null) {
            try {
                this.mRemote.linkToDeath(this.mDeathRecipient, 0);
            } catch (RemoteException e) {
                this.mRemote = null;
            }
        }
        if (this.mRemote != null) {
            currentNetwork(NET_STATUS);
        }
        return this.mRemote;
    }

    public void currentNetwork(int status) {
        if (oifaceEnable()) {
            NET_STATUS = status;
            Parcel _data = Parcel.obtain();
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(status);
                this.mRemote.transact(1, _data, null, 1);
            } catch (Exception e) {
                Log.d(TAG, "currentNetwork has Exception : " + e);
            } finally {
                _data.recycle();
            }
        }
    }

    public void currentPackage(String packageNmae, int uid, int pid) {
        if (oifaceEnable()) {
            Parcel _data = Parcel.obtain();
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(packageNmae);
                _data.writeInt(uid);
                _data.writeInt(pid);
                this.mRemote.transact(2, _data, null, 1);
            } catch (Exception e) {
                Log.d(TAG, "currentPackage has Exception : " + e);
            } finally {
                _data.recycle();
            }
        }
    }

    private boolean oifaceEnable() {
        if (this.mRemote != null || connectOifaceDataService() != null) {
            return true;
        }
        Log.e(TAG, "Cannot connect to OifaceService");
        return false;
    }
}

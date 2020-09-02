package com.oppo.oiface;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

public class OifaceProxyUtils {
    private static final String DESCRIPTOR = "com.oppo.oiface.IOIfaceService";
    /* access modifiers changed from: private */
    public static Boolean INIT_NET_STATS = true;
    public static final int NET_OFF_WLAN = 1;
    public static final int NET_OFF_WWLAN = 3;
    public static final int NET_ON_WLAN = 0;
    public static final int NET_ON_WWLAN = 2;
    private static volatile int NET_STATUS = -1;
    private static final int OIFACE_ON = 0;
    private static final String TAG = "OifaceUtil";
    private static final int TRANSACTION_CURRENT_NETWORK = 1;
    private static final int TRANSACTION_CURRENT_PACKAGE = 2;
    private static OifaceProxyUtils sInstance;
    private static int sOifaceProp = SystemProperties.getInt("persist.sys.oiface.enable", 0);
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.oppo.oiface.OifaceProxyUtils.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.d(OifaceProxyUtils.TAG, "OifaceProxyUtils binderDied");
            IBinder unused = OifaceProxyUtils.this.mRemote = null;
            Boolean unused2 = OifaceProxyUtils.INIT_NET_STATS = false;
        }
    };
    /* access modifiers changed from: private */
    public IBinder mRemote;

    public static OifaceProxyUtils getInstance() {
        if (sInstance == null) {
            synchronized (OifaceProxyUtils.class) {
                if (sInstance == null) {
                    sInstance = new OifaceProxyUtils();
                }
            }
        }
        return sInstance;
    }

    private OifaceProxyUtils() {
        connectOifaceDataService();
    }

    private IBinder connectOifaceDataService() {
        this.mRemote = ServiceManager.checkService("oiface");
        IBinder iBinder = this.mRemote;
        if (iBinder != null) {
            try {
                iBinder.linkToDeath(this.mDeathRecipient, 0);
            } catch (RemoteException e) {
                this.mRemote = null;
            }
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
            } catch (Throwable th) {
                _data.recycle();
                throw th;
            }
            _data.recycle();
        }
    }

    public void currentPackage(String packageNmae, int uid, int pid) {
        if (oifaceEnable()) {
            if (!INIT_NET_STATS.booleanValue()) {
                INIT_NET_STATS = true;
                currentNetwork(NET_STATUS);
            }
            Parcel _data = Parcel.obtain();
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(packageNmae);
                _data.writeInt(uid);
                _data.writeInt(pid);
                this.mRemote.transact(2, _data, null, 1);
            } catch (Exception e) {
                Log.d(TAG, "currentPackage has Exception : " + e);
            } catch (Throwable th) {
                _data.recycle();
                throw th;
            }
            _data.recycle();
        }
    }

    private boolean oifaceEnable() {
        if (this.mRemote != null || connectOifaceDataService() != null) {
            return true;
        }
        Log.e(TAG, "Cannot connect to OifaceService");
        return false;
    }

    public void initNetworkState(Context context) {
        if (NET_STATUS == -1) {
            int status = getNetworkState(context);
            Log.d(TAG, "initNetworkState:" + NET_STATUS);
            currentNetwork(status);
        }
    }

    public int getNetworkState(Context context) {
        if (isWifiConn(context)) {
            NET_STATUS = 0;
        } else if (isMobileConn(context)) {
            NET_STATUS = 2;
        } else {
            NET_STATUS = 1;
        }
        return NET_STATUS;
    }

    public static boolean isWifiConn(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(1);
        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        }
        return true;
    }

    public static boolean isMobileConn(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(0);
        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        }
        return true;
    }
}

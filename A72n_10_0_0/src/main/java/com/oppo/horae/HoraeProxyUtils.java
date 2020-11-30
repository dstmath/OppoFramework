package com.oppo.horae;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

public class HoraeProxyUtils {
    private static final String DESCRIPTOR = "com.oppo.horae.IHoraeService";
    private static final int HORAE_ON = 1;
    private static final String TAG = "HoraeUtil";
    private static final int TRANSACTION_CURRENT_PACKAGE = 4;
    private static int sHoraeProp = SystemProperties.getInt("persist.sys.horae.enable", 1);
    private IBinder.DeathRecipient mDeathRecipient;
    private IBinder mRemote;

    private static class SingletonHolder {
        private static HoraeProxyUtils instance = new HoraeProxyUtils();

        private SingletonHolder() {
        }
    }

    public static HoraeProxyUtils getInstance() {
        return SingletonHolder.instance;
    }

    private HoraeProxyUtils() {
        this.mDeathRecipient = new IBinder.DeathRecipient() {
            /* class com.oppo.horae.HoraeProxyUtils.AnonymousClass1 */

            @Override // android.os.IBinder.DeathRecipient
            public void binderDied() {
                Log.d(HoraeProxyUtils.TAG, "HoraeProxyUtils binderDied");
                HoraeProxyUtils.this.mRemote = null;
            }
        };
        connectHoraeService();
    }

    private synchronized IBinder connectHoraeService() {
        this.mRemote = ServiceManager.checkService("horae");
        if (this.mRemote != null) {
            try {
                this.mRemote.linkToDeath(this.mDeathRecipient, 0);
            } catch (RemoteException e) {
                this.mRemote = null;
            }
        }
        return this.mRemote;
    }

    public void currentPackage(String pkgName, String activityName, int uid, int pid) {
        if (horaeEnable()) {
            Parcel _data = Parcel.obtain();
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(pkgName);
                _data.writeString(activityName);
                _data.writeInt(uid);
                _data.writeInt(pid);
                this.mRemote.transact(4, _data, null, 1);
            } catch (Exception e) {
                Log.d(TAG, "get currentPackage has Exception : " + e);
            } catch (Throwable th) {
                _data.recycle();
                throw th;
            }
            _data.recycle();
        }
    }

    private boolean horaeEnable() {
        if (sHoraeProp == 0) {
            Log.e(TAG, "horae is not open");
            return false;
        } else if (this.mRemote != null || connectHoraeService() != null) {
            return true;
        } else {
            Log.e(TAG, "Cannot connect to HoraeService");
            return false;
        }
    }
}

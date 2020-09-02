package com.oppo.neuron;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;

public class NeoServiceProxy implements INeoService {
    private static final boolean DBG = false;
    private static final String TAG = "NeoServiceProxy";
    private static NeoServiceProxy sInstance;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.oppo.neuron.NeoServiceProxy.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (NeoServiceProxy.this) {
                IBinder unused = NeoServiceProxy.this.mRemote = null;
            }
        }
    };
    /* access modifiers changed from: private */
    public IBinder mRemote;

    public static synchronized NeoServiceProxy getInstance() {
        NeoServiceProxy neoServiceProxy;
        synchronized (NeoServiceProxy.class) {
            if (sInstance == null) {
                sInstance = new NeoServiceProxy();
            }
            neoServiceProxy = sInstance;
        }
        return neoServiceProxy;
    }

    private NeoServiceProxy() {
        connectToNeoService();
    }

    private IBinder connectToNeoService() {
        this.mRemote = ServiceManager.getService(INeoService.DESCRIPTOR);
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

    @Override // com.oppo.neuron.INeoService
    public synchronized String[] appPreloadPredict() {
        if (this.mRemote == null && connectToNeoService() == null) {
            return null;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(INeoService.DESCRIPTOR);
            this.mRemote.transact(302, _data, _reply, 0);
            int num = _reply.readInt();
            String[] apps = new String[num];
            for (int i = 0; i < num; i++) {
                apps[i] = _reply.readString();
            }
            _reply.readException();
            _reply.readInt();
            return apps;
        } catch (RemoteException e) {
            return null;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }
}

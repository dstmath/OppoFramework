package com.mediatek.anr;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import java.lang.reflect.Method;

public abstract class AnrManagerNative extends Binder implements IAnrManager {
    private static final Singleton<IAnrManager> gDefault = new Singleton<IAnrManager>() {
        /* class com.mediatek.anr.AnrManagerNative.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // com.mediatek.anr.AnrManagerNative.Singleton
        public IAnrManager create() {
            IBinder binder = null;
            try {
                binder = (IBinder) AnrManagerNative.sGetService.invoke(null, "anrmanager");
            } catch (Exception e) {
            }
            return AnrManagerNative.asInterface(binder);
        }
    };
    private static Method sGetService = getServiceManagerMethod("getService", new Class[]{String.class});

    private static Method getServiceManagerMethod(String func, Class[] cls) {
        try {
            return Class.forName("android.os.ServiceManager").getDeclaredMethod(func, cls);
        } catch (Exception e) {
            return null;
        }
    }

    public static IAnrManager asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IAnrManager in = (IAnrManager) obj.queryLocalInterface(IAnrManager.descriptor);
        if (in != null) {
            return in;
        }
        return new AnrManagerProxy(obj);
    }

    public static IAnrManager getDefault() {
        return gDefault.get();
    }

    public AnrManagerNative() {
        attachInterface(this, IAnrManager.descriptor);
    }

    @Override // android.os.Binder
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code != 2) {
            return super.onTransact(code, data, reply, flags);
        }
        data.enforceInterface(IAnrManager.descriptor);
        informMessageDump(data.readString(), data.readInt());
        return true;
    }

    public IBinder asBinder() {
        return this;
    }

    /* access modifiers changed from: package-private */
    public static abstract class Singleton<T> {
        private T mInstance;

        /* access modifiers changed from: protected */
        public abstract T create();

        Singleton() {
        }

        public final T get() {
            T t;
            synchronized (this) {
                if (this.mInstance == null) {
                    this.mInstance = create();
                }
                t = this.mInstance;
            }
            return t;
        }
    }
}

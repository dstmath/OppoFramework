package androidx.test.runner.internal.deps.aidl;

import android.os.Binder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public abstract class BaseStub extends Binder implements IInterface {
    private static TransactionInterceptor globalInterceptor = null;

    /* access modifiers changed from: protected */
    public boolean routeToSuperOrEnforceInterface(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code > 16777215) {
            return super.onTransact(code, data, reply, flags);
        }
        data.enforceInterface(getInterfaceDescriptor());
        return false;
    }

    @Override // android.os.Binder
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (routeToSuperOrEnforceInterface(code, data, reply, flags)) {
            return true;
        }
        if (globalInterceptor == null) {
            return dispatchTransaction(code, data, reply, flags);
        }
        return globalInterceptor.interceptTransaction(this, code, data, reply, flags);
    }

    /* access modifiers changed from: protected */
    public boolean dispatchTransaction(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return false;
    }
}

package androidx.test.runner.internal.deps.aidl;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public abstract class BaseProxy implements IInterface {
    private final String mDescriptor;
    private final IBinder mRemote;

    protected BaseProxy(IBinder remote, String descriptor) {
        this.mRemote = remote;
        this.mDescriptor = descriptor;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    /* access modifiers changed from: protected */
    public Parcel obtainAndWriteInterfaceToken() {
        Parcel parcel = Parcel.obtain();
        parcel.writeInterfaceToken(this.mDescriptor);
        return parcel;
    }

    /* access modifiers changed from: protected */
    public void transactAndReadExceptionReturnVoid(int code, Parcel in) throws RemoteException {
        Parcel out = Parcel.obtain();
        try {
            this.mRemote.transact(code, in, out, 0);
            out.readException();
        } finally {
            in.recycle();
            out.recycle();
        }
    }
}

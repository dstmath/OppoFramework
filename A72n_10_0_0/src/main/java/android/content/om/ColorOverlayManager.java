package android.content.om;

import android.os.Parcel;
import android.os.RemoteException;

public class ColorOverlayManager extends ColorBaseOverlayManager implements IColorOverlayManager {
    @Override // android.content.om.IColorOverlayManager
    public void setLanguageEnable(String path, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseOverlayManager.DESCRIPTOR);
            data.writeString(path);
            data.writeInt(userId);
            this.mRemote.transact(10002, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}

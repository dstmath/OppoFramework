package android.hardware.display;

import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;

public class ColorosDisplayManager extends ColorBaseDisplayManager implements IColorosDisplayManager {
    public static final String KEY_GAME_LOCK_SWITCH = "game_lock_switch";
    public static final int MSG_GAME_SPACE = 0;

    @Override // android.hardware.display.IColorosDisplayManager
    public boolean setStateChanged(int msgId, Bundle extraData) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseDisplayManager.DESCRIPTOR);
            data.writeInt(msgId);
            data.writeBundle(extraData);
            this.mRemote.transact(IColorosDisplayManager.SET_AI_BRIGHT_SCENE_STATE_CHANGED_, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}

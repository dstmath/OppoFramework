package android.content.om;

import android.os.RemoteException;

public interface IColorOverlayManager extends IColorBaseOverlayManager {
    public static final int SET_LANGUAGE_ENABLE_TRANSACTION = 10002;

    void setLanguageEnable(String str, int i) throws RemoteException;
}

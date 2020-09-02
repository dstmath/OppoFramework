package android.os;

import com.color.os.IColorScreenStatusListener;

public interface IColorCommonPowerManager extends IColorBasePowerManager {
    void registerScreenStatusListener(IColorScreenStatusListener iColorScreenStatusListener) throws RemoteException;

    void unregisterScreenStatusListener(IColorScreenStatusListener iColorScreenStatusListener) throws RemoteException;
}

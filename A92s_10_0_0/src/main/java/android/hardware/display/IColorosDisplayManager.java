package android.hardware.display;

import android.os.Bundle;
import android.os.RemoteException;

public interface IColorosDisplayManager extends IColorBaseDisplayManager {
    public static final int SET_AI_BRIGHT_SCENE_STATE_CHANGED_ = 10111;

    boolean setStateChanged(int i, Bundle bundle) throws RemoteException;
}

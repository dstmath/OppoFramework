package com.android.server.display;

import android.os.Bundle;
import android.os.RemoteException;

public class ColorDummyAIBrightManager implements IColorAIBrightManager {
    @Override // com.android.server.display.IColorAIBrightManager
    public void init(DisplayManagerService dms) {
    }

    @Override // com.android.server.display.IColorAIBrightManager
    public boolean setStateChanged(int msgId, Bundle extraData) throws RemoteException {
        return false;
    }
}

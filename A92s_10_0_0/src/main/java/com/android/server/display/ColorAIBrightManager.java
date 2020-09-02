package com.android.server.display;

import android.os.Bundle;
import android.os.RemoteException;
import com.color.util.ColorTypeCastingHelper;

public class ColorAIBrightManager implements IColorAIBrightManager {
    private static volatile ColorAIBrightManager sInstance = null;
    private OppoBaseDisplayManagerService mBaseDms;
    private DisplayManagerService mDms;

    private ColorAIBrightManager() {
    }

    public static ColorAIBrightManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorAIBrightManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorAIBrightManager();
                }
            }
        }
        return sInstance;
    }

    public void init(DisplayManagerService dms) {
        this.mDms = dms;
        this.mBaseDms = typeCasting(dms);
    }

    public boolean setStateChanged(int msgId, Bundle extraData) throws RemoteException {
        OppoBaseDisplayManagerService oppoBaseDisplayManagerService = this.mBaseDms;
        if (oppoBaseDisplayManagerService != null) {
            return oppoBaseDisplayManagerService.setStateChanged(msgId, extraData);
        }
        return false;
    }

    private static OppoBaseDisplayManagerService typeCasting(DisplayManagerService dms) {
        if (dms != null) {
            return (OppoBaseDisplayManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseDisplayManagerService.class, dms);
        }
        return null;
    }
}

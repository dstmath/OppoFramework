package android.os;

import android.os.IOppoService;
import android.util.Log;

public final class OppoFlashLightManager {
    private static final boolean DEBUG = true;
    public static final String SERVICE_NAME = "OPPO";
    private static final String TAG = "OppoFlashLightManager";
    private static OppoFlashLightManager mInstance = null;
    private static IOppoService sService;

    private OppoFlashLightManager() {
        sService = IOppoService.Stub.asInterface(ServiceManager.getService("OPPO"));
        StringBuilder sb = new StringBuilder();
        sb.append("get service res:");
        sb.append(sService != null);
        Log.d(TAG, sb.toString());
    }

    public static OppoFlashLightManager getOppoFlashLightManager() {
        if (mInstance == null) {
            mInstance = new OppoFlashLightManager();
        }
        return mInstance;
    }

    public boolean openFlashLight() {
        try {
            if (sService != null) {
                return sService.openFlashLight();
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "openFlashLight failed.", e);
            return false;
        }
    }

    public boolean closeFlashLight() {
        try {
            if (sService != null) {
                return sService.closeFlashLight();
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "closeFlashLight failed.", e);
            return false;
        }
    }

    public String getFlashLightState() {
        try {
            return sService != null ? sService.getFlashLightState() : "";
        } catch (RemoteException e) {
            Log.e(TAG, "getFlashLightState failed.", e);
            return null;
        }
    }
}

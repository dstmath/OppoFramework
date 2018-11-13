package android.os;

import android.os.IOppoService.Stub;
import android.util.Log;

public final class OppoFlashLightManager {
    private static final boolean DEBUG = true;
    public static final String SERVICE_NAME = "OPPO";
    private static final String TAG = "OppoFlashLightManager";
    private static OppoFlashLightManager mInstance = null;
    private static IOppoService sService;

    private OppoFlashLightManager() {
        sService = Stub.asInterface(ServiceManager.getService("OPPO"));
    }

    public static OppoFlashLightManager getOppoFlashLightManager() {
        if (mInstance == null) {
            mInstance = new OppoFlashLightManager();
        }
        return mInstance;
    }

    public boolean openFlashLight() {
        try {
            return sService.openFlashLight();
        } catch (RemoteException e) {
            Log.e(TAG, "openFlashLight failed.", e);
            return false;
        }
    }

    public boolean closeFlashLight() {
        try {
            return sService.closeFlashLight();
        } catch (RemoteException e) {
            Log.e(TAG, "closeFlashLight failed.", e);
            return false;
        }
    }

    public String getFlashLightState() {
        try {
            return sService.getFlashLightState();
        } catch (RemoteException e) {
            Log.e(TAG, "getFlashLightState failed.", e);
            return null;
        }
    }
}

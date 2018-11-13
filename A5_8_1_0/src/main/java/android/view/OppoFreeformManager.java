package android.view;

import android.graphics.Rect;
import android.os.ServiceManager;
import android.util.Log;
import android.view.IWindowManager.Stub;

public final class OppoFreeformManager {
    private static final String TAG = "OppoFreeformManager";
    private static OppoFreeformManager sDefaultFreeforManager;
    private static IWindowManager sWindowManagerService;

    private OppoFreeformManager() {
        getWindowManagerService();
    }

    public static OppoFreeformManager getInstance() {
        OppoFreeformManager oppoFreeformManager;
        synchronized (OppoFreeformManager.class) {
            if (sDefaultFreeforManager == null) {
                sDefaultFreeforManager = new OppoFreeformManager();
            }
            oppoFreeformManager = sDefaultFreeforManager;
        }
        return oppoFreeformManager;
    }

    public static IWindowManager getWindowManagerService() {
        IWindowManager iWindowManager;
        synchronized (OppoFreeformManager.class) {
            if (sWindowManagerService == null) {
                sWindowManagerService = Stub.asInterface(ServiceManager.getService("window"));
            }
            iWindowManager = sWindowManagerService;
        }
        return iWindowManager;
    }

    public boolean isInFreeformMode() {
        try {
            return sWindowManagerService.isInFreeformMode();
        } catch (Exception e) {
            Log.w(TAG, "isInFreeformMode e = " + e);
            return false;
        }
    }

    public void getFreeformStackBounds(Rect outBounds) {
        try {
            sWindowManagerService.getFreeformStackBounds(outBounds);
        } catch (Exception e) {
            Log.w(TAG, "getFreeformStackBounds e = " + e);
        }
    }
}

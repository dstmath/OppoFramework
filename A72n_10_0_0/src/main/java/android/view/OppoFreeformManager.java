package android.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;

public final class OppoFreeformManager {
    private static final String TAG = "OppoFreeformManager";
    private static OppoFreeformManager sDefaultFreeforManager;
    private static IWindowManager sWindowManagerService;
    private OppoWindowManager mOWm = new OppoWindowManager();

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

    private static IWindowManager getWindowManagerService() {
        IWindowManager iWindowManager;
        synchronized (OppoFreeformManager.class) {
            if (sWindowManagerService == null) {
                sWindowManagerService = IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));
            }
            iWindowManager = sWindowManagerService;
        }
        return iWindowManager;
    }

    public boolean isInFreeformMode() {
        try {
            return this.mOWm.isInFreeformMode();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void getFreeformStackBounds(Rect outBounds) {
        try {
            this.mOWm.getFreeformStackBounds(outBounds);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

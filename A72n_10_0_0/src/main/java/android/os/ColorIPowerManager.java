package android.os;

import android.util.Log;
import com.color.os.IColorScreenStatusListener;
import com.color.util.ColorLog;

public class ColorIPowerManager extends ColorBaseIPowerManager {
    private static final String TAG = "ColorIPowerManager";
    private ColorCommonPowerManager mCommonManager;

    /* access modifiers changed from: protected */
    @Override // android.os.ColorBaseIPowerManager, com.color.util.ColorBaseServiceManager
    public void init(IBinder remote) {
        super.init(remote);
        this.mCommonManager = new ColorCommonPowerManager(remote);
    }

    public void registerScreenStatusListener(IColorScreenStatusListener listener) {
        try {
            this.mCommonManager.registerScreenStatusListener(listener);
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, TAG, "registerScreenStatusListener : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, TAG, Log.getStackTraceString(e2));
        }
    }

    public void unregisterScreenStatusListener(IColorScreenStatusListener listener) {
        try {
            this.mCommonManager.unregisterScreenStatusListener(listener);
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, TAG, "unregisterScreenStatusListener : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, TAG, Log.getStackTraceString(e2));
        }
    }
}

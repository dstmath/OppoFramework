package com.android.server.wifi;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.statusbar.IStatusBarService;

/* access modifiers changed from: package-private */
public class OppoWifiCommonUtil {
    private static final String TAG = "OppoWifiCommonUtil";
    private static IBinder mToken = new Binder();

    public static void disableStatusBar(Context context, boolean disable) {
        IStatusBarService mStatusBarService;
        if (context != null && (mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"))) != null) {
            int state = 0;
            if (disable) {
                state = 0 | 65536;
            }
            try {
                mStatusBarService.disable(state, mToken, context.getPackageName());
            } catch (RemoteException e) {
                Log.d(TAG, "disableStatusBar error:" + e);
            }
        }
    }
}

package com.color.inner.net;

import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.os.Handler;
import android.os.ServiceManager;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.List;

public class ConnectivityManagerWrapper {
    private static final String TAG = "ConnManagerWrapper";

    public interface OnStartTetheringCallbackWrapper {
        void onTetheringFailed();

        void onTetheringStarted();
    }

    private ConnectivityManagerWrapper() {
    }

    public static void stopTethering(ConnectivityManager connectivityManager, int type) {
        try {
            connectivityManager.stopTethering(type);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void startTethering(ConnectivityManager connectivityManager, int type, boolean showProvisioningUi, final OnStartTetheringCallbackWrapper callback, Handler handler) {
        ConnectivityManager.OnStartTetheringCallback onStartTetheringCallback = null;
        if (callback != null) {
            try {
                onStartTetheringCallback = new ConnectivityManager.OnStartTetheringCallback() {
                    /* class com.color.inner.net.ConnectivityManagerWrapper.AnonymousClass1 */

                    public void onTetheringFailed() {
                        OnStartTetheringCallbackWrapper.this.onTetheringStarted();
                    }

                    public void onTetheringStarted() {
                        OnStartTetheringCallbackWrapper.this.onTetheringFailed();
                    }
                };
            } catch (Throwable e) {
                Log.e(TAG, e.toString());
                return;
            }
        }
        connectivityManager.startTethering(type, showProvisioningUi, onStartTetheringCallback, handler);
    }

    public static void setVpnPackageAuthorization(String packageName, int userId, boolean allowed) {
        try {
            IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity")).setVpnPackageAuthorization(packageName, userId, allowed);
        } catch (Exception e) {
            Log.e(TAG, "setVpnPackageAuthorization error", e);
        }
    }

    public static List<String> readArpFile(ConnectivityManager connectivityManager) {
        try {
            Method method = connectivityManager.getClass().getDeclaredMethod("readArpFile", new Class[0]);
            if (method != null) {
                return (List) method.invoke(connectivityManager, new Object[0]);
            }
            Log.d(TAG, "not found method readArpFile.");
            return null;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}

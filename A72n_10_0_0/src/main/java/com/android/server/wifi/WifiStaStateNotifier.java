package com.android.server.wifi;

import android.net.wifi.IStaStateCallback;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.wifi.util.ExternalCallbackTracker;

public class WifiStaStateNotifier {
    private static final String TAG = "WifiStaStateNotifier";
    private static WifiInjector mWifiInjector;
    private final ExternalCallbackTracker<IStaStateCallback> mRegisteredCallbacks;

    WifiStaStateNotifier(Looper looper, WifiInjector wifiInjector) {
        this.mRegisteredCallbacks = new ExternalCallbackTracker<>(new Handler(looper));
        mWifiInjector = wifiInjector;
    }

    public void addCallback(IBinder binder, IStaStateCallback callback, int callbackIdentifier) {
        Log.d(TAG, "addCallback");
        if (this.mRegisteredCallbacks.getNumCallbacks() > 0) {
            Log.e(TAG, "Failed to add callback, only support single request!");
        } else if (!this.mRegisteredCallbacks.add(binder, callback, callbackIdentifier)) {
            Log.e(TAG, "Failed to add callback");
        } else {
            mWifiInjector.getActiveModeWarden().registerStaEventCallback();
        }
    }

    public void removeCallback(int callbackIdentifier) {
        Log.d(TAG, "removeCallback");
        this.mRegisteredCallbacks.remove(callbackIdentifier);
        mWifiInjector.getActiveModeWarden().unregisterStaEventCallback();
    }

    public void onStaToBeOff() {
        Log.d(TAG, "onStaToBeOff");
        for (IStaStateCallback callback : this.mRegisteredCallbacks.getCallbacks()) {
            try {
                Log.d(TAG, "callback onStaToBeOff");
                callback.onStaToBeOff();
            } catch (RemoteException e) {
            }
        }
    }
}

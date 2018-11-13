package com.android.server.fingerprint.sensor;

public interface IProximitySensorEventListener {
    void onRegisterStateChanged(boolean z);

    void onSensorChanged(boolean z);
}

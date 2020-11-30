package com.android.server.biometrics.fingerprint.sensor;

public interface IProximitySensorEventListener {
    void onRegisterStateChanged(boolean z);

    void onSensorChanged(boolean z);
}

package com.android.server.biometrics.face;

import android.hardware.face.ClientMode;
import android.os.RemoteException;
import java.util.ArrayList;

public interface IRecognitionCallback {
    void blockScreenOn();

    int getFailedAttempts();

    void onAcquired(long j, int i, int i2, int i3);

    void onAuthenticated(long j, int i, int i2, ArrayList<Byte> arrayList);

    void onEnrollResult(long j, int i, int i2, int i3);

    void onEnumerate(long j, ArrayList<Integer> arrayList, int i) throws RemoteException;

    void onError(long j, int i, int i2, int i3);

    int onFaceFilterSucceeded(long j, byte[] bArr, ClientMode clientMode);

    void onLockoutChanged(long j);

    void onPreviewStarted();

    void onRemoved(long j, ArrayList<Integer> arrayList, int i);

    void unblockScreenOn(String str);
}

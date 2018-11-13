package com.android.server.face;

import android.hardware.face.ClientMode;
import android.hardware.face.CommandResult;
import com.sensetime.faceapi.model.FaceInfo;

public interface IRecognitionCallback {
    void onAcquired(long j, int i);

    void onAuthenticated(long j, int i, int i2);

    void onCommandResult(CommandResult commandResult);

    void onEnrollResult(long j, int i, int i2, int i3);

    void onEnumerate(long j, int[] iArr, int i);

    void onError(long j, int i);

    int onFaceFilterSucceeded(byte[] bArr, int i, int i2, FaceInfo faceInfo, ClientMode clientMode);

    void onPreviewStarted();

    void onRemoved(long j, int i, int i2);

    void onSlightError(long j, int i);

    void updateScreenOnBlockedState(boolean z, boolean z2, long j);
}

package com.android.server.face;

import android.hardware.face.ClientMode;
import android.hardware.face.CommandResult;

public interface IRecognitionCallback {
    void onAcquired(long j, int i);

    void onAuthenticated(long j, int i, int i2);

    void onCommandResult(CommandResult commandResult);

    void onEnrollResult(long j, int i, int i2, int i3);

    void onEnumerate(long j, int[] iArr, int i);

    void onError(long j, int i);

    int onFaceFilterSucceeded(long j, byte[] bArr, ClientMode clientMode);

    void onPreviewStarted();

    void onRemoved(long j, int i, int i2);

    void unblockScreenOn(String str);
}

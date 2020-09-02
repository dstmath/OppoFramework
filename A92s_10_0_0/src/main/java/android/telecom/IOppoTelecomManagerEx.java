package android.telecom;

import android.content.Intent;
import android.os.Bundle;

public interface IOppoTelecomManagerEx {
    void addNewOutgoingCall(Intent intent);

    String colorInteractWithTelecomService(int i, String str);

    void oppoCancelMissedCallsNotification(Bundle bundle);
}

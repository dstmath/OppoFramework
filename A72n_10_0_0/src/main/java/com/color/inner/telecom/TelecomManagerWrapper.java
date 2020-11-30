package com.color.inner.telecom;

import android.content.Intent;
import android.os.Bundle;
import android.telecom.IOppoTelecomManagerEx;
import android.telecom.TelecomManager;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class TelecomManagerWrapper {
    private static final String TAG = "TelecomManagerWrapper";

    private TelecomManagerWrapper() {
    }

    public static void addNewOutgoingCall(TelecomManager telecomManager, Intent intent) {
        try {
            typeCasting(telecomManager).addNewOutgoingCall(intent);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void oppoCancelMissedCallsNotification(TelecomManager telecomManager, Bundle bundle) {
        try {
            typeCasting(telecomManager).oppoCancelMissedCallsNotification(bundle);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public static String colorInteractWithTelecomService(TelecomManager telecomManager, int command, String ext) {
        try {
            return typeCasting(telecomManager).colorInteractWithTelecomService(command, ext);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    private static IOppoTelecomManagerEx typeCasting(TelecomManager telecomManager) {
        return (IOppoTelecomManagerEx) ColorTypeCastingHelper.typeCasting(IOppoTelecomManagerEx.class, telecomManager);
    }
}

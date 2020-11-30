package com.color.inner.nfc.cardemulation;

import android.nfc.cardemulation.OppoBaseApduServiceInfo;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class ApduServiceInfoWrapper {
    private static final String TAG = "ApduServiceInfoWrapper";

    private ApduServiceInfoWrapper() {
    }

    public static boolean isServiceEnabled(Object apduServiceInfoObject, String category) {
        try {
            OppoBaseApduServiceInfo baseApduServiceInfo = typeCasting(apduServiceInfoObject);
            if (baseApduServiceInfo != null) {
                return baseApduServiceInfo.isServiceEnabled(category);
            }
            return false;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    private static OppoBaseApduServiceInfo typeCasting(Object apduServiceInfoObject) {
        return (OppoBaseApduServiceInfo) ColorTypeCastingHelper.typeCasting(OppoBaseApduServiceInfo.class, apduServiceInfoObject);
    }
}

package com.color.inner.telephony;

import android.util.Log;
import com.android.internal.telephony.MccTable;

public class MccTableWrapper {
    private static final String TAG = "MccTableWrapper";

    private MccTableWrapper() {
    }

    public static String countryCodeForMcc(int mcc) {
        try {
            return MccTable.countryCodeForMcc(mcc);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }

    public static String defaultLanguageForMcc(int mcc) {
        try {
            return MccTable.defaultLanguageForMcc(mcc);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }
}

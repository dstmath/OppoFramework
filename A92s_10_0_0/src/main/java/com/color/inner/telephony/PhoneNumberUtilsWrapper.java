package com.color.inner.telephony;

import android.telephony.PhoneNumberUtils;
import android.util.Log;

public class PhoneNumberUtilsWrapper {
    private static final String TAG = "PhoneNumberUtilsWrapper";

    private PhoneNumberUtilsWrapper() {
    }

    public static boolean isVoiceMailNumber(int subId, String number) {
        try {
            return PhoneNumberUtils.isVoiceMailNumber(subId, number);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static String cdmaCheckAndProcessPlusCode(String dialStr) {
        try {
            return PhoneNumberUtils.cdmaCheckAndProcessPlusCode(dialStr);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}

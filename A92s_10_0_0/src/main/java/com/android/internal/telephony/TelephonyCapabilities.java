package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.SystemProperties;
import android.telephony.Rlog;

public class TelephonyCapabilities {
    private static final String LOG_TAG = "TelephonyCapabilities";

    private TelephonyCapabilities() {
    }

    public static boolean supportsEcm(Phone phone) {
        if (SystemProperties.get("ro.vendor.mtk_telephony_add_on_policy", OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK).equals(OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK)) {
            try {
                return ((Boolean) Class.forName("com.mediatek.internal.telephony.MtkTelephonyCapabilities").getDeclaredMethod("supportsEcm", Phone.class).invoke(null, phone)).booleanValue();
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(LOG_TAG, "supportsEcm invoke redirect fails. Use AOSP instead.");
            }
        }
        Rlog.d(LOG_TAG, "supportsEcm: Phone type = " + phone.getPhoneType() + " Ims Phone = " + phone.getImsPhone());
        return phone.getPhoneType() == 2 || phone.getImsPhone() != null;
    }

    public static boolean supportsOtasp(Phone phone) {
        return phone.getPhoneType() == 2;
    }

    public static boolean supportsVoiceMessageCount(Phone phone) {
        return phone.getVoiceMessageCount() != -1;
    }

    public static boolean supportsNetworkSelection(Phone phone) {
        return phone.getPhoneType() == 1;
    }

    public static int getDeviceIdLabel(Phone phone) {
        if (phone.getPhoneType() == 1) {
            return 17040129;
        }
        if (phone.getPhoneType() == 2) {
            return 17040403;
        }
        Rlog.w(LOG_TAG, "getDeviceIdLabel: no known label for phone " + phone.getPhoneName());
        return 0;
    }

    public static boolean supportsConferenceCallManagement(Phone phone) {
        if (phone.getPhoneType() == 1 || phone.getPhoneType() == 3) {
            return true;
        }
        return false;
    }

    public static boolean supportsHoldAndUnhold(Phone phone) {
        if (phone.getPhoneType() == 1 || phone.getPhoneType() == 3 || phone.getPhoneType() == 5) {
            return true;
        }
        return false;
    }

    public static boolean supportsAnswerAndHold(Phone phone) {
        if (phone.getPhoneType() == 1 || phone.getPhoneType() == 3) {
            return true;
        }
        return false;
    }

    @UnsupportedAppUsage
    public static boolean supportsAdn(int phoneType) {
        return phoneType == 1;
    }

    public static boolean canDistinguishDialingAndConnected(int phoneType) {
        return phoneType == 1;
    }
}

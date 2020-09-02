package com.color.inner.telephony;

import android.telephony.IOppoSmsMessageEx;
import android.telephony.SmsMessage;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class SmsMessageWrapper {
    public static final int ENCODING_UNKNOWN = 0;
    private static final String TAG = "SmsMessageWrapper";

    private SmsMessageWrapper() {
    }

    public static int getSubId(SmsMessage smsMessage) {
        try {
            return smsMessage.getSubId();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static void setSubId(SmsMessage smsMessage, int subId) {
        try {
            smsMessage.setSubId(subId);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static boolean isWrappedSmsMessageNull(SmsMessage smsMessage) {
        try {
            return smsMessage.mWrappedSmsMessage == null;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return true;
        }
    }

    public static String getDestinationAddress(SmsMessage smsMessage) {
        try {
            return typeCasting(smsMessage).getDestinationAddress();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, int subId) {
        try {
            return SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, subId);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static boolean hasEmsSupport() {
        try {
            return SmsMessage.hasEmsSupport();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static int getEncodingType(SmsMessage smsMessage) {
        try {
            return typeCasting(smsMessage).getEncodingType();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    private static IOppoSmsMessageEx typeCasting(SmsMessage smsMessage) {
        return (IOppoSmsMessageEx) ColorTypeCastingHelper.typeCasting(IOppoSmsMessageEx.class, smsMessage);
    }
}

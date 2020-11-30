package com.color.inner.telephony;

import android.app.PendingIntent;
import android.telephony.IOppoSmsManagerEx;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;

public class SmsManagerWrapper {
    private static final String TAG = "SmsManagerWrapper";

    private SmsManagerWrapper() {
    }

    public static ArrayList<SmsMessage> getAllMessagesFromIcc(SmsManager smsManager) {
        try {
            return smsManager.getAllMessagesFromIcc();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static boolean copyMessageToIcc(SmsManager smsManager, byte[] smsc, byte[] pdu, int status) {
        try {
            return smsManager.copyMessageToIcc(smsc, pdu, status);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static boolean deleteMessageFromIcc(SmsManager smsManager, int messageIndex) {
        try {
            return smsManager.deleteMessageFromIcc(messageIndex);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static void sendMultipartTextMessageOem(SmsManager smsManager, String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        try {
            typeCasting(smsManager).sendMultipartTextMessageOem(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, priority, expectMore, validityPeriod, encodingType);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static ArrayList<String> divideMessageOem(SmsManager smsManager, String text, int encodingType) {
        try {
            return typeCasting(smsManager).divideMessageOem(text, encodingType);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    private static IOppoSmsManagerEx typeCasting(SmsManager smsManager) {
        return (IOppoSmsManagerEx) ColorTypeCastingHelper.typeCasting(IOppoSmsManagerEx.class, smsManager);
    }
}

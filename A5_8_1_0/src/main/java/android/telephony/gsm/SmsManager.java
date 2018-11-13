package android.telephony.gsm;

import android.app.PendingIntent;
import android.telephony.SmsMessage;
import android.util.SeempLog;
import java.util.ArrayList;

@Deprecated
public final class SmsManager {
    @Deprecated
    public static final int RESULT_ERROR_GENERIC_FAILURE = 1;
    @Deprecated
    public static final int RESULT_ERROR_NO_SERVICE = 4;
    @Deprecated
    public static final int RESULT_ERROR_NULL_PDU = 3;
    @Deprecated
    public static final int RESULT_ERROR_RADIO_OFF = 2;
    @Deprecated
    public static final int STATUS_ON_SIM_FREE = 0;
    @Deprecated
    public static final int STATUS_ON_SIM_READ = 1;
    @Deprecated
    public static final int STATUS_ON_SIM_SENT = 5;
    @Deprecated
    public static final int STATUS_ON_SIM_UNREAD = 3;
    @Deprecated
    public static final int STATUS_ON_SIM_UNSENT = 7;
    private static SmsManager sInstance;
    private android.telephony.SmsManager mSmsMgrProxy = android.telephony.SmsManager.getDefault();

    @Deprecated
    public static final SmsManager getDefault() {
        if (sInstance == null) {
            sInstance = new SmsManager();
        }
        return sInstance;
    }

    @Deprecated
    private SmsManager() {
    }

    @Deprecated
    public final void sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        SeempLog.record_str(75, destinationAddress);
        this.mSmsMgrProxy.sendTextMessage(destinationAddress, scAddress, text, sentIntent, deliveryIntent);
    }

    @Deprecated
    public final ArrayList<String> divideMessage(String text) {
        return this.mSmsMgrProxy.divideMessage(text);
    }

    @Deprecated
    public final void sendMultipartTextMessage(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        SeempLog.record_str(77, destinationAddress);
        this.mSmsMgrProxy.sendMultipartTextMessage(destinationAddress, scAddress, parts, sentIntents, deliveryIntents);
    }

    @Deprecated
    public final void sendDataMessage(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        SeempLog.record_str(73, destinationAddress);
        this.mSmsMgrProxy.sendDataMessage(destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent);
    }

    @Deprecated
    public final boolean copyMessageToSim(byte[] smsc, byte[] pdu, int status) {
        SeempLog.record(82);
        return this.mSmsMgrProxy.copyMessageToIcc(smsc, pdu, status);
    }

    @Deprecated
    public final boolean deleteMessageFromSim(int messageIndex) {
        SeempLog.record(83);
        return this.mSmsMgrProxy.deleteMessageFromIcc(messageIndex);
    }

    @Deprecated
    public final boolean updateMessageOnSim(int messageIndex, int newStatus, byte[] pdu) {
        SeempLog.record(84);
        return this.mSmsMgrProxy.updateMessageOnIcc(messageIndex, newStatus, pdu);
    }

    @Deprecated
    public final ArrayList<SmsMessage> getAllMessagesFromSim() {
        SeempLog.record(85);
        return android.telephony.SmsManager.getDefault().getAllMessagesFromIcc();
    }
}

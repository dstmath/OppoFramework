package com.android.internal.telephony;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.SmsMessage;
import android.telephony.SmsMessage.MessageClass;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.SMSDispatcher.SmsTracker;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ImsSMSDispatcher extends SMSDispatcher {
    private static final String TAG = "RIL_ImsSms";
    private SMSDispatcher mCdmaDispatcher;
    private CdmaInboundSmsHandler mCdmaInboundSmsHandler;
    private SMSDispatcher mGsmDispatcher;
    private GsmInboundSmsHandler mGsmInboundSmsHandler;
    private boolean mIms = false;
    private String mImsSmsFormat = "unknown";

    public ImsSMSDispatcher(Phone phone, SmsStorageMonitor storageMonitor, SmsUsageMonitor usageMonitor) {
        super(phone, usageMonitor, null);
        Rlog.d(TAG, "ImsSMSDispatcher created");
        this.mCdmaDispatcher = new CdmaSMSDispatcher(phone, usageMonitor, this);
        this.mGsmInboundSmsHandler = GsmInboundSmsHandler.makeInboundSmsHandler(phone.getContext(), storageMonitor, phone);
        this.mCdmaInboundSmsHandler = CdmaInboundSmsHandler.makeInboundSmsHandler(phone.getContext(), storageMonitor, phone, (CdmaSMSDispatcher) this.mCdmaDispatcher);
        this.mGsmDispatcher = new GsmSMSDispatcher(phone, usageMonitor, this, this.mGsmInboundSmsHandler);
        SmsBroadcastUndelivered.initialize(phone.getContext(), this.mGsmInboundSmsHandler, this.mCdmaInboundSmsHandler, phone);
        InboundSmsHandler.registerNewMessageNotificationActionHandler(phone.getContext());
        this.mCi.registerForOn(this, 11, null);
        this.mCi.registerForImsNetworkStateChanged(this, 12, null);
        updatePhoneObject(phone);
        if (phone != null && phone.mCi != null) {
            Rlog.d(TAG, "Trigger to send cached short message if any");
            phone.mCi.notifyCachedSms();
            phone.mCi.notifyCachedCdmaSms();
            phone.mCi.notifyCachedSimSms();
            phone.mCi.notifyCachedStatusSms();
        }
    }

    protected void updatePhoneObject(Phone phone) {
        Rlog.d(TAG, "In IMS updatePhoneObject ");
        super.updatePhoneObject(phone);
        this.mCdmaDispatcher.updatePhoneObject(phone);
        this.mGsmDispatcher.updatePhoneObject(phone);
        this.mGsmInboundSmsHandler.updatePhoneObject(phone);
        this.mCdmaInboundSmsHandler.updatePhoneObject(phone);
    }

    public void dispose() {
        this.mCi.unregisterForOn(this);
        this.mCi.unregisterForImsNetworkStateChanged(this);
        this.mGsmDispatcher.dispose();
        this.mCdmaDispatcher.dispose();
        this.mGsmInboundSmsHandler.dispose();
        this.mCdmaInboundSmsHandler.dispose();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 11:
            case 12:
                this.mCi.getImsRegistrationState(obtainMessage(13));
                return;
            case 13:
                AsyncResult ar = msg.obj;
                if (ar.exception == null) {
                    updateImsInfo(ar);
                    return;
                } else {
                    Rlog.e(TAG, "IMS State query failed with exp " + ar.exception);
                    return;
                }
            default:
                super.handleMessage(msg);
                return;
        }
    }

    private void setImsSmsFormat(int format) {
        switch (format) {
            case 1:
                this.mImsSmsFormat = SmsMessage.FORMAT_3GPP;
                return;
            case 2:
                this.mImsSmsFormat = SmsMessage.FORMAT_3GPP2;
                return;
            default:
                this.mImsSmsFormat = "unknown";
                return;
        }
    }

    private void updateImsInfo(AsyncResult ar) {
        int[] responseArray = ar.result;
        this.mIms = false;
        if (responseArray[0] == 1) {
            Rlog.d(TAG, "IMS is registered!");
            this.mIms = true;
        } else {
            Rlog.d(TAG, "IMS is NOT registered!");
        }
        setImsSmsFormat(responseArray[1]);
        if ("unknown".equals(this.mImsSmsFormat)) {
            Rlog.e(TAG, "IMS format was unknown!");
            this.mIms = false;
        }
    }

    public void sendData(String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (isCdmaMo()) {
            this.mCdmaDispatcher.sendData(destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
        } else {
            this.mGsmDispatcher.sendData(destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
        }
    }

    public void sendMultipartText(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage) {
        if (isCdmaMo()) {
            this.mCdmaDispatcher.sendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage);
        } else {
            this.mGsmDispatcher.sendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage);
        }
    }

    protected void sendSms(SmsTracker tracker) {
        Rlog.e(TAG, "sendSms should never be called from here!");
    }

    protected void sendSmsByPstn(SmsTracker tracker) {
        Rlog.e(TAG, "sendSmsByPstn should never be called from here!");
    }

    public void sendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage) {
        Rlog.d(TAG, "sendText");
        if (isCdmaMo()) {
            this.mCdmaDispatcher.sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage);
        } else {
            this.mGsmDispatcher.sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage);
        }
    }

    public void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        Rlog.d(TAG, "ImsSMSDispatcher:injectSmsPdu");
        try {
            SmsMessage msg = SmsMessage.createFromPdu(pdu, format);
            if (msg == null || msg.getMessageClass() != MessageClass.CLASS_1) {
                if (msg == null) {
                    Rlog.e(TAG, "injectSmsPdu: createFromPdu returned null");
                }
                if (receivedIntent != null) {
                    receivedIntent.send(2);
                }
                return;
            }
            AsyncResult ar = new AsyncResult(receivedIntent, msg, null);
            if (format.equals(SmsMessage.FORMAT_3GPP)) {
                Rlog.i(TAG, "ImsSMSDispatcher:injectSmsText Sending msg=" + msg + ", format=" + format + "to mGsmInboundSmsHandler");
                this.mGsmInboundSmsHandler.sendMessage(8, ar);
            } else if (format.equals(SmsMessage.FORMAT_3GPP2)) {
                Rlog.i(TAG, "ImsSMSDispatcher:injectSmsText Sending msg=" + msg + ", format=" + format + "to mCdmaInboundSmsHandler");
                this.mCdmaInboundSmsHandler.sendMessage(8, ar);
            } else {
                Rlog.e(TAG, "Invalid pdu format: " + format);
                if (receivedIntent != null) {
                    receivedIntent.send(2);
                }
            }
        } catch (Exception e) {
            Rlog.e(TAG, "injectSmsPdu failed: ", e);
            if (receivedIntent != null) {
                try {
                    receivedIntent.send(2);
                } catch (CanceledException e2) {
                }
            }
        }
    }

    public void sendRetrySms(SmsTracker tracker) {
        String newFormat;
        String oldFormat = tracker.mFormat;
        if (2 == this.mPhone.getPhoneType()) {
            newFormat = this.mCdmaDispatcher.getFormat();
        } else {
            newFormat = this.mGsmDispatcher.getFormat();
        }
        if (!oldFormat.equals(newFormat)) {
            HashMap map = tracker.getData();
            boolean containsKey = (map.containsKey("scAddr") && map.containsKey("destAddr")) ? !map.containsKey("text") ? map.containsKey("data") ? map.containsKey("destPort") : false : true : false;
            if (containsKey) {
                String scAddr = (String) map.get("scAddr");
                String destAddr = (String) map.get("destAddr");
                SubmitPduBase pdu = null;
                if (map.containsKey("text")) {
                    Rlog.d(TAG, "sms failed was text");
                    String text = (String) map.get("text");
                    if (isCdmaFormat(newFormat)) {
                        Rlog.d(TAG, "old format (gsm) ==> new format (cdma)");
                        pdu = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddr, destAddr, text, tracker.mDeliveryIntent != null, null);
                    } else {
                        Rlog.d(TAG, "old format (cdma) ==> new format (gsm)");
                        pdu = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddr, destAddr, text, tracker.mDeliveryIntent != null, null);
                    }
                } else if (map.containsKey("data")) {
                    Rlog.d(TAG, "sms failed was data");
                    byte[] data = (byte[]) map.get("data");
                    Integer destPort = (Integer) map.get("destPort");
                    if (isCdmaFormat(newFormat)) {
                        Rlog.d(TAG, "old format (gsm) ==> new format (cdma)");
                        pdu = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddr, destAddr, destPort.intValue(), data, tracker.mDeliveryIntent != null);
                    } else {
                        Rlog.d(TAG, "old format (cdma) ==> new format (gsm)");
                        pdu = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddr, destAddr, destPort.intValue(), data, tracker.mDeliveryIntent != null);
                    }
                }
                map.put("smsc", pdu.encodedScAddress);
                map.put("pdu", pdu.encodedMessage);
                SMSDispatcher dispatcher = isCdmaFormat(newFormat) ? this.mCdmaDispatcher : this.mGsmDispatcher;
                tracker.mFormat = dispatcher.getFormat();
                dispatcher.sendSms(tracker);
                return;
            }
            Rlog.e(TAG, "sendRetrySms failed to re-encode per missing fields!");
            tracker.onFailed(this.mContext, 1, 0);
        } else if (isCdmaFormat(newFormat)) {
            Rlog.d(TAG, "old format matched new format (cdma)");
            this.mCdmaDispatcher.sendSms(tracker);
        } else {
            Rlog.d(TAG, "old format matched new format (gsm)");
            this.mGsmDispatcher.sendSms(tracker);
        }
    }

    protected void sendSubmitPdu(SmsTracker tracker) {
        sendRawPdu(tracker);
    }

    protected String getFormat() {
        Rlog.e(TAG, "getFormat should never be called from here!");
        return "unknown";
    }

    protected TextEncodingDetails calculateLength(CharSequence messageBody, boolean use7bitOnly) {
        Rlog.e(TAG, "Error! Not implemented for IMS.");
        return null;
    }

    protected SmsTracker getNewSubmitPduTracker(String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int format, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText) {
        Rlog.e(TAG, "Error! Not implemented for IMS.");
        return null;
    }

    public boolean isIms() {
        return this.mIms;
    }

    public String getImsSmsFormat() {
        return this.mImsSmsFormat;
    }

    private boolean isCdmaMo() {
        if (isIms()) {
            return isCdmaFormat(this.mImsSmsFormat);
        }
        return 2 == this.mPhone.getPhoneType();
    }

    private boolean isCdmaFormat(String format) {
        return this.mCdmaDispatcher.getFormat().equals(format);
    }

    protected void sendData(String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (isCdmaMo()) {
            this.mCdmaDispatcher.sendData(destAddr, scAddr, destPort, originalPort, data, sentIntent, deliveryIntent);
        } else {
            this.mGsmDispatcher.sendData(destAddr, scAddr, destPort, originalPort, data, sentIntent, deliveryIntent);
        }
    }

    protected void sendMultipartData(String destAddr, String scAddr, int destPort, ArrayList<SmsRawData> data, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        if (isCdmaMo()) {
            this.mCdmaDispatcher.sendMultipartData(destAddr, scAddr, destPort, data, sentIntents, deliveryIntents);
        } else {
            this.mGsmDispatcher.sendMultipartData(destAddr, scAddr, destPort, data, sentIntents, deliveryIntents);
        }
    }

    public int copyTextMessageToIccCard(String scAddress, String address, List<String> text, int status, long timestamp) {
        if (isCdmaMo()) {
            return this.mCdmaDispatcher.copyTextMessageToIccCard(scAddress, address, text, status, timestamp);
        }
        return this.mGsmDispatcher.copyTextMessageToIccCard(scAddress, address, text, status, timestamp);
    }

    protected void sendTextWithEncodingType(String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage) {
        if (isCdmaMo()) {
            this.mCdmaDispatcher.sendTextWithEncodingType(destAddr, scAddr, text, encodingType, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage);
        } else {
            this.mGsmDispatcher.sendTextWithEncodingType(destAddr, scAddr, text, encodingType, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage);
        }
    }

    protected void sendMultipartTextWithEncodingType(String destAddr, String scAddr, ArrayList<String> parts, int encodingType, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage) {
        if (isCdmaMo()) {
            this.mCdmaDispatcher.sendMultipartTextWithEncodingType(destAddr, scAddr, parts, encodingType, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage);
        } else {
            this.mGsmDispatcher.sendMultipartTextWithEncodingType(destAddr, scAddr, parts, encodingType, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage);
        }
    }

    public void sendTextWithExtraParams(String destAddr, String scAddr, String text, Bundle extraParams, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage) {
        if (isCdmaMo()) {
            this.mCdmaDispatcher.sendTextWithExtraParams(destAddr, scAddr, text, extraParams, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage);
        } else {
            this.mGsmDispatcher.sendTextWithExtraParams(destAddr, scAddr, text, extraParams, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage);
        }
    }

    public void sendMultipartTextWithExtraParams(String destAddr, String scAddr, ArrayList<String> parts, Bundle extraParams, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage) {
        if (isCdmaMo()) {
            this.mCdmaDispatcher.sendMultipartTextWithExtraParams(destAddr, scAddr, parts, extraParams, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage);
        } else {
            this.mGsmDispatcher.sendMultipartTextWithExtraParams(destAddr, scAddr, parts, extraParams, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage);
        }
    }

    protected void handleIccFull() {
        if (isCdmaMo()) {
            this.mCdmaDispatcher.handleIccFull();
        } else {
            this.mGsmDispatcher.handleIccFull();
        }
    }

    protected void setSmsMemoryStatus(boolean status) {
        if (isCdmaMo()) {
            this.mCdmaDispatcher.setSmsMemoryStatus(status);
        } else {
            this.mGsmDispatcher.setSmsMemoryStatus(status);
        }
    }
}

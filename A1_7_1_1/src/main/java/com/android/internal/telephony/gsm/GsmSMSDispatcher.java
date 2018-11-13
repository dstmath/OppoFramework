package com.android.internal.telephony.gsm;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.ImsSMSDispatcher;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SMSDispatcher.SmsTracker;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsHeader.ConcatRef;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.SmsRawData;
import com.android.internal.telephony.SmsUsageMonitor;
import com.android.internal.telephony.gsm.SmsMessage.DeliverPdu;
import com.android.internal.telephony.gsm.SmsMessage.SubmitPdu;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class GsmSMSDispatcher extends SMSDispatcher {
    private static final int EVENT_NEW_SMS_STATUS_REPORT = 100;
    private static final String TAG = "GsmSMSDispatcher";
    private static final boolean VDBG = false;
    private GsmInboundSmsHandler mGsmInboundSmsHandler;
    private AtomicReference<IccRecords> mIccRecords = new AtomicReference();
    private AtomicReference<UiccCardApplication> mUiccApplication = new AtomicReference();
    protected UiccController mUiccController = null;

    public GsmSMSDispatcher(Phone phone, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher, GsmInboundSmsHandler gsmInboundSmsHandler) {
        super(phone, usageMonitor, imsSMSDispatcher);
        this.mCi.setOnSmsStatus(this, 100, null);
        this.mGsmInboundSmsHandler = gsmInboundSmsHandler;
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 15, new Integer(this.mPhone.getPhoneId()));
        Rlog.d(TAG, "GsmSMSDispatcher created");
    }

    public void dispose() {
        super.dispose();
        this.mCi.unSetOnSmsStatus(this);
        this.mUiccController.unregisterForIccChanged(this);
    }

    protected String getFormat() {
        return SmsMessage.FORMAT_3GPP;
    }

    public void handleMessage(Message msg) {
        try {
            switch (msg.what) {
                case 14:
                    this.mGsmInboundSmsHandler.sendMessage(1, msg.obj);
                    return;
                case 15:
                    Integer phoneId = getUiccControllerPhoneId(msg);
                    if (phoneId.intValue() != this.mPhone.getPhoneId()) {
                        Rlog.d(TAG, "Wrong phone id event coming, PhoneId: " + phoneId);
                        return;
                    } else {
                        onUpdateIccAvailability();
                        return;
                    }
                case 100:
                    handleStatusReport((AsyncResult) msg.obj);
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        e.printStackTrace();
    }

    private void handleStatusReport(AsyncResult ar) {
        String pduString = ar.result;
        SmsMessage sms = SmsMessage.newFromCDS(pduString);
        if (sms != null) {
            int tpStatus = sms.getStatus();
            int messageRef = sms.mMessageRef;
            int i = 0;
            int count = this.deliveryPendingList.size();
            while (i < count) {
                SmsTracker tracker = (SmsTracker) this.deliveryPendingList.get(i);
                if (tracker.mMessageRef == messageRef) {
                    if (tpStatus >= 64 || tpStatus < 32) {
                        this.deliveryPendingList.remove(i);
                        tracker.updateSentMessageStatus(this.mContext, tpStatus);
                    }
                    PendingIntent intent = tracker.mDeliveryIntent;
                    Intent fillIn = new Intent();
                    fillIn.putExtra("pdu", IccUtils.hexStringToBytes(pduString));
                    fillIn.putExtra("format", getFormat());
                    try {
                        intent.send(this.mContext, -1, fillIn);
                    } catch (CanceledException e) {
                    }
                } else {
                    i++;
                }
            }
        }
        this.mCi.acknowledgeLastIncomingGsmSms(true, 1, null);
    }

    protected void sendData(String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (isDmLock) {
            Rlog.d(TAG, "DM status: lock-on");
            return;
        }
        SubmitPdu pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, destPort, data, deliveryIntent != null);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, destPort, data, pdu), sentIntent, deliveryIntent, getFormat(), null, false, null, false, true);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender dataSmsSender = new DataSmsSender(this, tracker);
                dataSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(this, dataSmsSender));
            } else {
                Rlog.v(TAG, "No carrier package.");
                sendRawPdu(tracker);
            }
        } else {
            Rlog.e(TAG, "GsmSMSDispatcher.sendData(): getSubmitPdu() returned null");
            if (!(sentIntent == null || this.mContext == null)) {
                try {
                    Intent fillIn = new Intent();
                    fillIn.putExtra("errorCode", 1);
                    sentIntent.send(this.mContext, 1, fillIn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage) {
        if (isDmLock) {
            Rlog.d(TAG, "DM status: lock-on");
            return;
        }
        SubmitPduBase pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, text, deliveryIntent != null);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, text, pdu), sentIntent, deliveryIntent, getFormat(), messageUri, false, text, true, persistMessage);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender textSmsSender = new TextSmsSender(this, tracker);
                textSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(this, textSmsSender));
            } else {
                Rlog.v(TAG, "No carrier package.");
                sendRawPdu(tracker);
            }
        } else {
            Rlog.e(TAG, "GsmSMSDispatcher.sendText(): getSubmitPdu() returned null");
            if (!(sentIntent == null || this.mContext == null)) {
                try {
                    Intent fillIn = new Intent();
                    fillIn.putExtra("errorCode", 1);
                    sentIntent.send(this.mContext, 1, fillIn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void injectSmsPdu(byte[] pdu, String format, PendingIntent receivedIntent) {
        throw new IllegalStateException("This method must be called only on ImsSMSDispatcher");
    }

    protected TextEncodingDetails calculateLength(CharSequence messageBody, boolean use7bitOnly) {
        return SmsMessage.calculateLength(messageBody, use7bitOnly);
    }

    protected SmsTracker getNewSubmitPduTracker(String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText) {
        SubmitPduBase pdu = SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, deliveryIntent != null, SmsHeader.toByteArray(smsHeader), encoding, smsHeader.languageTable, smsHeader.languageShiftTable);
        if (pdu != null) {
            return getSmsTracker(getSmsTrackerMap(destinationAddress, scAddress, message, pdu), sentIntent, deliveryIntent, getFormat(), unsentPartCount, anyPartFailed, messageUri, smsHeader, !lastPart, fullMessageText, true, true);
        }
        Rlog.e(TAG, "GsmSMSDispatcher.sendNewSubmitPdu(): getSubmitPdu() returned null");
        return null;
    }

    protected void sendSubmitPdu(SmsTracker tracker) {
        sendRawPdu(tracker);
    }

    protected SmsTracker getNewSubmitPduTracker(String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText, int validityPeriod) {
        SubmitPduBase pdu = SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, deliveryIntent != null, SmsHeader.toByteArray(smsHeader), encoding, smsHeader.languageTable, smsHeader.languageShiftTable, validityPeriod);
        if (pdu != null) {
            return getSmsTracker(getSmsTrackerMap(destinationAddress, scAddress, message, pdu), sentIntent, deliveryIntent, getFormat(), unsentPartCount, anyPartFailed, messageUri, smsHeader, !lastPart, fullMessageText, true, true);
        }
        Rlog.e(TAG, "GsmSMSDispatcher.getNewSubmitPduTracker(): getSubmitPdu() returned null");
        return null;
    }

    protected void sendSms(SmsTracker tracker) {
        byte[] pdu = (byte[]) tracker.getData().get("pdu");
        boolean isReadySend = false;
        synchronized (this.mSTrackersQueue) {
            if (this.mSTrackersQueue.isEmpty() || this.mSTrackersQueue.get(0) != tracker) {
                Rlog.d(TAG, "Add tracker into the list: " + tracker);
                this.mSTrackersQueue.add(tracker);
            }
            if (this.mSTrackersQueue.get(0) == tracker) {
                isReadySend = true;
            }
        }
        if (isReadySend) {
            if (tracker.mRetryCount > 0) {
                Rlog.d(TAG, "sendSms:  mRetryCount=" + tracker.mRetryCount + " mMessageRef=" + tracker.mMessageRef + " SS=" + this.mPhone.getServiceState().getState());
                if ((pdu[0] & 1) == 1) {
                    pdu[0] = (byte) (pdu[0] | 4);
                    pdu[1] = (byte) tracker.mMessageRef;
                }
            }
            Rlog.d(TAG, "sendSms:  isIms()=" + isIms() + " mRetryCount=" + tracker.mRetryCount + " mImsRetry=" + tracker.mImsRetry + " mMessageRef=" + tracker.mMessageRef + " SS=" + this.mPhone.getServiceState().getState());
            sendSmsByPstn(tracker);
            return;
        }
        Rlog.d(TAG, "There is another tracker in-queue and is sending");
    }

    protected void sendSmsByPstn(SmsTracker tracker) {
        try {
            boolean isPolicyMessageSendEnable = OemConstant.isSmsSendEnable(this.mPhone);
            Rlog.d("sms", "isPolicyMessageSendEnable=" + isPolicyMessageSendEnable);
            if (!(this.mContext == null || isPolicyMessageSendEnable)) {
                Rlog.d(TAG, "gsm--sendSmsByPstn, stop tracker.");
                tracker.onFailed(this.mContext, 1, 0);
                sendMessageDelayed(obtainMessage(Phone.OEM_PRODUCT_17373, tracker), 10);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0 || this.mTelephonyManager.isWifiCallingAvailable()) {
            HashMap<String, Object> map = tracker.getData();
            byte[] smsc = (byte[]) map.get("smsc");
            byte[] pdu = (byte[]) map.get("pdu");
            Message reply = obtainMessage(2, tracker);
            if (tracker.mImsRetry != 0 || isIms()) {
                this.mCi.sendImsGsmSms(IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), tracker.mImsRetry, tracker.mMessageRef, reply);
                tracker.mImsRetry++;
            } else {
                if (tracker.mRetryCount > 0 && (pdu[0] & 1) == 1) {
                    pdu[0] = (byte) (pdu[0] | 4);
                    pdu[1] = (byte) tracker.mMessageRef;
                }
                if (tracker.mRetryCount == 0 && tracker.mExpectMore) {
                    this.mCi.sendSMSExpectMore(IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), reply);
                } else {
                    this.mCi.sendSMS(IccUtils.bytesToHexString(smsc), IccUtils.bytesToHexString(pdu), reply);
                }
            }
            return;
        }
        tracker.onFailed(this.mContext, SMSDispatcher.getNotInServiceError(ss), 0);
        sendMessageDelayed(obtainMessage(Phone.OEM_PRODUCT_17373, tracker), 10);
    }

    protected UiccCardApplication getUiccCardApplication() {
        Rlog.d(TAG, "GsmSMSDispatcher: subId = " + this.mPhone.getSubId() + " slotId = " + this.mPhone.getPhoneId());
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 1);
    }

    private void onUpdateIccAvailability() {
        if (this.mUiccController != null) {
            UiccCardApplication newUiccApplication = getUiccCardApplication();
            UiccCardApplication app = (UiccCardApplication) this.mUiccApplication.get();
            if (app != newUiccApplication) {
                if (app != null) {
                    Rlog.d(TAG, "Removing stale icc objects.");
                    if (this.mIccRecords.get() != null) {
                        ((IccRecords) this.mIccRecords.get()).unregisterForNewSms(this);
                    }
                    this.mIccRecords.set(null);
                    this.mUiccApplication.set(null);
                }
                if (newUiccApplication != null) {
                    Rlog.d(TAG, "New Uicc application found");
                    this.mUiccApplication.set(newUiccApplication);
                    this.mIccRecords.set(newUiccApplication.getIccRecords());
                    if (this.mIccRecords.get() != null) {
                        ((IccRecords) this.mIccRecords.get()).registerForNewSms(this, 14, null);
                    }
                }
            }
        }
    }

    private Integer getUiccControllerPhoneId(Message msg) {
        Integer phoneId = new Integer(-1);
        AsyncResult ar = msg.obj;
        if (ar == null || !(ar.result instanceof Integer)) {
            return phoneId;
        }
        return ar.result;
    }

    protected void sendData(String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        Rlog.d(TAG, "GsmSmsDispatcher.sendData: enter");
        if (isDmLock) {
            Rlog.d(TAG, "DM status: lock-on");
            return;
        }
        SubmitPdu pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, destPort, originalPort, data, deliveryIntent != null ? true : null);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, destPort, data, pdu), sentIntent, deliveryIntent, getFormat(), null, false, null, false, true);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender dataSmsSender = new DataSmsSender(this, tracker);
                dataSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(this, dataSmsSender));
            } else {
                Rlog.v(TAG, "No carrier package.");
                sendRawPdu(tracker);
            }
        } else {
            Rlog.e(TAG, "GsmSMSDispatcher.sendData(): getSubmitPdu() returned null");
        }
    }

    protected void sendMultipartData(String destAddr, String scAddr, int destPort, ArrayList<SmsRawData> data, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        if (isDmLock) {
            Rlog.d(TAG, "DM status: lock-on");
            return;
        }
        int refNumber = SMSDispatcher.getNextConcatenatedRef() & 255;
        int msgCount = data.size();
        Object trackers = new SmsTracker[msgCount];
        int i = 0;
        while (i < msgCount) {
            byte[] smsHeader = SmsHeader.getSubmitPduHeader(destPort, refNumber, i + 1, msgCount);
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            PendingIntent pendingIntent2 = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
            }
            trackers[i] = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, destPort, ((SmsRawData) data.get(i)).getBytes(), SmsMessage.getSubmitPdu(scAddr, destAddr, ((SmsRawData) data.get(i)).getBytes(), smsHeader, pendingIntent2 != null)), pendingIntent, pendingIntent2, getFormat(), null, false, null, false, true);
            i++;
        }
        if (data == null || trackers == null || trackers.length == 0 || trackers[0] == null) {
            Rlog.e(TAG, "Cannot send multipart data. parts=" + data + " trackers=" + trackers);
            return;
        }
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            Rlog.d(TAG, "Found carrier package.");
            SmsSender dataSmsSender = new DataSmsSender(this, trackers[0]);
            dataSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(this, dataSmsSender));
        } else {
            Rlog.v(TAG, "No carrier package.");
            for (SmsTracker tracker : trackers) {
                if (tracker != null) {
                    sendRawPdu(tracker);
                } else {
                    Rlog.e(TAG, "Null tracker.");
                }
            }
        }
    }

    protected void activateCellBroadcastSms(int activate, Message response) {
        boolean z = false;
        Message reply = obtainMessage(101, response);
        CommandsInterface commandsInterface = this.mCi;
        if (activate == 0) {
            z = true;
        }
        commandsInterface.setGsmBroadcastActivation(z, reply);
    }

    protected void getCellBroadcastSmsConfig(Message response) {
        this.mCi.getGsmBroadcastConfig(obtainMessage(102, response));
    }

    protected void setCellBroadcastConfig(int[] configValuesArray, Message response) {
        Rlog.e(TAG, "Error! The functionality cell broadcast sms is not implemented for GSM.");
        response.recycle();
    }

    protected void setCellBroadcastConfig(ArrayList<SmsBroadcastConfigInfo> chIdList, ArrayList<SmsBroadcastConfigInfo> langList, Message response) {
        Message reply = obtainMessage(Phone.OEM_PRODUCT_16391, response);
        chIdList.addAll(langList);
        this.mCi.setGsmBroadcastConfig((SmsBroadcastConfigInfo[]) chIdList.toArray(new SmsBroadcastConfigInfo[1]), reply);
    }

    protected void queryCellBroadcastActivation(Message response) {
        this.mCi.getGsmBroadcastConfig(obtainMessage(104, response));
    }

    public int copyTextMessageToIccCard(String scAddress, String address, List<String> text, int status, long timestamp) {
        Rlog.d(TAG, "GsmSMSDispatcher: copy text message to icc card");
        if (!checkPhoneNumber(scAddress)) {
            Rlog.d(TAG, "[copyText invalid sc address");
            scAddress = null;
        }
        if (checkPhoneNumber(address)) {
            boolean isDeliverPdu;
            int i;
            this.mSuccess = true;
            int msgCount = text.size();
            Rlog.d(TAG, "[copyText storage available");
            if (status == 1 || status == 3) {
                Rlog.d(TAG, "[copyText to encode deliver pdu");
                isDeliverPdu = true;
            } else if (status == 5 || status == 7) {
                isDeliverPdu = false;
                Rlog.d(TAG, "[copyText to encode submit pdu");
            } else {
                Rlog.d(TAG, "[copyText invalid status, default is deliver pdu");
                return 1;
            }
            Rlog.d(TAG, "[copyText msgCount " + msgCount);
            if (msgCount > 1) {
                Rlog.d(TAG, "[copyText multi-part message");
            } else if (msgCount == 1) {
                Rlog.d(TAG, "[copyText single-part message");
            } else {
                Rlog.d(TAG, "[copyText invalid message count");
                return 1;
            }
            int refNumber = SMSDispatcher.getNextConcatenatedRef() & 255;
            int encoding = 0;
            TextEncodingDetails[] details = new TextEncodingDetails[msgCount];
            for (i = 0; i < msgCount; i++) {
                details[i] = SmsMessage.calculateLength((CharSequence) text.get(i), false);
                if (encoding != details[i].codeUnitSize && (encoding == 0 || encoding == 1)) {
                    encoding = details[i].codeUnitSize;
                }
            }
            i = 0;
            while (i < msgCount) {
                if (this.mSuccess) {
                    int singleShiftId = -1;
                    int lockingShiftId = -1;
                    int language = details[i].shiftLangId;
                    int encoding_method = encoding;
                    if (encoding == 1) {
                        Rlog.d(TAG, "Detail: " + i + " ted" + details[i]);
                        if (details[i].useLockingShift && details[i].useSingleShift) {
                            singleShiftId = language;
                            lockingShiftId = language;
                            encoding_method = 13;
                        } else if (details[i].useLockingShift) {
                            lockingShiftId = language;
                            encoding_method = 12;
                        } else if (details[i].useSingleShift) {
                            singleShiftId = language;
                            encoding_method = 11;
                        }
                    }
                    byte[] smsHeader = null;
                    if (msgCount > 1) {
                        Rlog.d(TAG, "[copyText get pdu header for multi-part message");
                        smsHeader = SmsHeader.getSubmitPduHeaderWithLang(-1, refNumber, i + 1, msgCount, singleShiftId, lockingShiftId);
                    }
                    if (isDeliverPdu) {
                        DeliverPdu pdu = SmsMessage.getDeliverPduWithLang(scAddress, address, (String) text.get(i), smsHeader, timestamp, encoding, language);
                        if (pdu != null) {
                            Rlog.d(TAG, "[copyText write deliver pdu into SIM");
                            this.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(pdu.encodedScAddress), IccUtils.bytesToHexString(pdu.encodedMessage), obtainMessage(106));
                        }
                    } else {
                        SubmitPdu pdu2 = SmsMessage.getSubmitPduWithLang(scAddress, address, (String) text.get(i), false, smsHeader, encoding_method, language);
                        if (pdu2 != null) {
                            Rlog.d(TAG, "[copyText write submit pdu into SIM");
                            this.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(pdu2.encodedScAddress), IccUtils.bytesToHexString(pdu2.encodedMessage), obtainMessage(106));
                        }
                    }
                    synchronized (this.mLock) {
                        try {
                            Rlog.d(TAG, "[copyText wait until the message be wrote in SIM");
                            this.mLock.wait();
                        } catch (InterruptedException e) {
                            Rlog.d(TAG, "[copyText interrupted while trying to copy text message into SIM");
                            return 1;
                        }
                    }
                    Rlog.d(TAG, "[copyText thread is waked up");
                    i++;
                } else {
                    Rlog.d(TAG, "[copyText Exception happened when copy message");
                    return 1;
                }
            }
            if (this.mSuccess) {
                Rlog.d(TAG, "[copyText all messages have been copied into SIM");
                return 0;
            }
            Rlog.d(TAG, "[copyText copy failed");
            return 1;
        }
        Rlog.d(TAG, "[copyText invalid dest address");
        return 8;
    }

    private boolean isValidSmsAddress(String address) {
        String encodedAddress = PhoneNumberUtils.extractNetworkPortion(address);
        if (encodedAddress == null || encodedAddress.length() == address.length()) {
            return true;
        }
        return false;
    }

    private boolean checkPhoneNumber(char c) {
        if ((c >= '0' && c <= '9') || c == '*' || c == '+' || c == '#' || c == 'N' || c == ' ' || c == '-') {
            return true;
        }
        return false;
    }

    private boolean checkPhoneNumber(String address) {
        if (address == null) {
            return true;
        }
        Rlog.d(TAG, "checkPhoneNumber: " + address);
        int n = address.length();
        for (int i = 0; i < n; i++) {
            if (!checkPhoneNumber(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    protected void sendTextWithEncodingType(String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage) {
        if (isDmLock) {
            Rlog.d(TAG, "DM status: lock-on");
            return;
        }
        int encoding = encodingType;
        TextEncodingDetails details = SmsMessage.calculateLength(text, false);
        if (encodingType != details.codeUnitSize && (encodingType == 0 || encodingType == 1)) {
            Rlog.d(TAG, "[enc conflict between details[" + details.codeUnitSize + "] and encoding " + encodingType);
            details.codeUnitSize = encodingType;
        }
        SubmitPduBase pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, text, deliveryIntent != null, null, encodingType, details.languageTable, details.languageShiftTable);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, text, pdu), sentIntent, deliveryIntent, getFormat(), messageUri, false, text, true, persistMessage);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender textSmsSender = new TextSmsSender(this, tracker);
                textSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(this, textSmsSender));
            } else {
                Rlog.v(TAG, "No carrier package.");
                sendRawPdu(tracker);
            }
        } else {
            Rlog.e(TAG, "GsmSMSDispatcher.sendTextWithEncodingType(): getSubmitPdu() returned null");
            if (sentIntent != null) {
                try {
                    sentIntent.send(3);
                } catch (CanceledException e) {
                    Rlog.e(TAG, "failed to send back RESULT_ERROR_NULL_PDU");
                }
            }
        }
    }

    protected void sendMultipartTextWithEncodingType(String destAddr, String scAddr, ArrayList<String> parts, int encodingType, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage) {
        if (isDmLock) {
            Rlog.d(TAG, "DM status: lock-on");
            return;
        }
        int i;
        String fullMessageText = getMultipartMessageText(parts);
        int refNumber = SMSDispatcher.getNextConcatenatedRef() & 255;
        int msgCount = parts.size();
        int encoding = encodingType;
        TextEncodingDetails[] encodingForParts = new TextEncodingDetails[msgCount];
        for (i = 0; i < msgCount; i++) {
            TextEncodingDetails details = SmsMessage.calculateLength((CharSequence) parts.get(i), false);
            if (encodingType != details.codeUnitSize && (encodingType == 0 || encodingType == 1)) {
                Rlog.d(TAG, "[enc conflict between details[" + details.codeUnitSize + "] and encoding " + encodingType);
                details.codeUnitSize = encodingType;
            }
            encodingForParts[i] = details;
        }
        Object trackers = new SmsTracker[msgCount];
        AtomicInteger unsentPartCount = new AtomicInteger(msgCount);
        AtomicBoolean anyPartFailed = new AtomicBoolean(false);
        i = 0;
        while (i < msgCount) {
            ConcatRef concatRef = new ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = i + 1;
            concatRef.msgCount = msgCount;
            concatRef.isEightBits = true;
            SmsHeader smsHeader = new SmsHeader();
            smsHeader.concatRef = concatRef;
            if (encodingType == 1) {
                smsHeader.languageTable = encodingForParts[i].languageTable;
                smsHeader.languageShiftTable = encodingForParts[i].languageShiftTable;
            }
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            PendingIntent pendingIntent2 = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
            }
            trackers[i] = getNewSubmitPduTracker(destAddr, scAddr, (String) parts.get(i), smsHeader, encodingType, pendingIntent, pendingIntent2, i == msgCount + -1, unsentPartCount, anyPartFailed, messageUri, fullMessageText);
            trackers[i].mPersistMessage = persistMessage;
            i++;
        }
        if (parts == null || trackers == null || trackers.length == 0 || trackers[0] == null) {
            Rlog.e(TAG, "Cannot send multipart text. parts=" + parts + " trackers=" + trackers);
            return;
        }
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            Rlog.d(TAG, "Found carrier package.");
            MultipartSmsSender multipartSmsSender = new MultipartSmsSender(this, parts, trackers);
            multipartSmsSender.sendSmsByCarrierApp(carrierPackage, new MultipartSmsSenderCallback(this, multipartSmsSender));
        } else {
            Rlog.v(TAG, "No carrier package.");
            for (SmsTracker tracker : trackers) {
                if (tracker != null) {
                    sendSubmitPdu(tracker);
                } else {
                    Rlog.e(TAG, "Null tracker.");
                }
            }
        }
    }

    protected void handleIccFull() {
        this.mGsmInboundSmsHandler.mStorageMonitor.handleIccFull();
    }

    protected void handleQueryCbActivation(AsyncResult ar) {
        Object result = null;
        if (ar.exception == null) {
            ArrayList<SmsBroadcastConfigInfo> list = ar.result;
            if (list.size() == 0) {
                result = new Boolean(false);
            } else {
                SmsBroadcastConfigInfo cbConfig = (SmsBroadcastConfigInfo) list.get(0);
                Rlog.d(TAG, "cbConfig: " + cbConfig.toString());
                if (cbConfig.getFromCodeScheme() == -1 && cbConfig.getToCodeScheme() == -1 && cbConfig.getFromServiceId() == -1 && cbConfig.getToServiceId() == -1 && !cbConfig.isSelected()) {
                    result = new Boolean(false);
                } else {
                    result = new Boolean(true);
                }
            }
        }
        Rlog.d(TAG, "queryCbActivation: " + result);
        AsyncResult.forMessage((Message) ar.userObj, result, ar.exception);
        ((Message) ar.userObj).sendToTarget();
    }

    public void sendTextWithExtraParams(String destAddr, String scAddr, String text, Bundle extraParams, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage) {
        Rlog.d(TAG, "sendTextWithExtraParams");
        if (isDmLock) {
            Rlog.d(TAG, "DM status: lock-on");
            return;
        }
        int validityPeriod = extraParams.getInt(SmsManager.EXTRA_PARAMS_VALIDITY_PERIOD, -1);
        Rlog.d(TAG, "validityPeriod is " + validityPeriod);
        SubmitPduBase pdu = SmsMessage.getSubmitPdu(scAddr, destAddr, text, deliveryIntent != null, null, 0, 0, 0, validityPeriod);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, text, pdu), sentIntent, deliveryIntent, getFormat(), messageUri, false, text, true, persistMessage);
            String carrierPackage = getCarrierAppPackageName();
            if (carrierPackage != null) {
                Rlog.d(TAG, "Found carrier package.");
                SmsSender textSmsSender = new TextSmsSender(this, tracker);
                textSmsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(this, textSmsSender));
            } else {
                Rlog.v(TAG, "No carrier package.");
                sendRawPdu(tracker);
            }
        } else {
            Rlog.e(TAG, "GsmSMSDispatcher.sendTextWithExtraParams(): getSubmitPdu() returned null");
            if (sentIntent != null) {
                try {
                    sentIntent.send(3);
                } catch (CanceledException e) {
                    Rlog.e(TAG, "failed to send back RESULT_ERROR_NULL_PDU");
                }
            }
        }
    }

    public void sendMultipartTextWithExtraParams(String destAddr, String scAddr, ArrayList<String> parts, Bundle extraParams, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage) {
        Rlog.d(TAG, "sendMultipartTextWithExtraParams");
        if (isDmLock) {
            Rlog.d(TAG, "DM status: lock-on");
            return;
        }
        int i;
        int validityPeriod = extraParams.getInt(SmsManager.EXTRA_PARAMS_VALIDITY_PERIOD, -1);
        Rlog.d(TAG, "validityPeriod is " + validityPeriod);
        String fullMessageText = getMultipartMessageText(parts);
        int refNumber = SMSDispatcher.getNextConcatenatedRef() & 255;
        int msgCount = parts.size();
        int encoding = 0;
        TextEncodingDetails[] encodingForParts = new TextEncodingDetails[msgCount];
        for (i = 0; i < msgCount; i++) {
            TextEncodingDetails details = calculateLength((CharSequence) parts.get(i), false);
            if (encoding != details.codeUnitSize && (encoding == 0 || encoding == 1)) {
                encoding = details.codeUnitSize;
            }
            encodingForParts[i] = details;
        }
        Object trackers = new SmsTracker[msgCount];
        AtomicInteger unsentPartCount = new AtomicInteger(msgCount);
        AtomicBoolean anyPartFailed = new AtomicBoolean(false);
        i = 0;
        while (i < msgCount) {
            ConcatRef concatRef = new ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = i + 1;
            concatRef.msgCount = msgCount;
            concatRef.isEightBits = true;
            SmsHeader smsHeader = new SmsHeader();
            smsHeader.concatRef = concatRef;
            if (encoding == 1) {
                smsHeader.languageTable = encodingForParts[i].languageTable;
                smsHeader.languageShiftTable = encodingForParts[i].languageShiftTable;
            }
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            PendingIntent pendingIntent2 = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
            }
            trackers[i] = getNewSubmitPduTracker(destAddr, scAddr, (String) parts.get(i), smsHeader, encoding, pendingIntent, pendingIntent2, i == msgCount + -1, unsentPartCount, anyPartFailed, messageUri, fullMessageText, validityPeriod);
            trackers[i].mPersistMessage = persistMessage;
            i++;
        }
        if (parts == null || trackers == null || trackers.length == 0 || trackers[0] == null) {
            Rlog.e(TAG, "Cannot send multipart text. parts=" + parts + " trackers=" + trackers);
            return;
        }
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            Rlog.d(TAG, "Found carrier package.");
            MultipartSmsSender multipartSmsSender = new MultipartSmsSender(this, parts, trackers);
            multipartSmsSender.sendSmsByCarrierApp(carrierPackage, new MultipartSmsSenderCallback(this, multipartSmsSender));
        } else {
            Rlog.v(TAG, "No carrier package.");
            for (SmsTracker tracker : trackers) {
                if (tracker != null) {
                    sendSubmitPdu(tracker);
                } else {
                    Rlog.e(TAG, "Null tracker.");
                }
            }
        }
    }
}

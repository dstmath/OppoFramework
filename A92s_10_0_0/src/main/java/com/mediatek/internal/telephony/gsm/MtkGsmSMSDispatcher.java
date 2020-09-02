package com.mediatek.internal.telephony.gsm;

import android.app.PendingIntent;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsRawData;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SmsMessage;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.util.SMSDispatcherUtil;
import com.mediatek.internal.telephony.MtkSmsHeader;
import com.mediatek.internal.telephony.gsm.MtkSmsMessage;
import com.mediatek.internal.telephony.util.MtkSMSDispatcherUtil;
import com.mediatek.internal.telephony.util.MtkSmsCommonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MtkGsmSMSDispatcher extends GsmSMSDispatcher {
    private static final boolean ENG = "eng".equals(Build.TYPE);
    protected static final int EVENT_ADD_DELIVER_PENDING_LIST = 107;
    protected static final int EVENT_COPY_TEXT_MESSAGE_DONE = 106;
    protected static String MSG_REF_NUM = "msg_ref_num";
    protected static String PDU_SIZE = "pdu_size";
    private static final String TAG = "MtkGsmSMSDispatcher";
    private ThreadLocal<Integer> mEncodingType = new ThreadLocal<Integer>() {
        /* class com.mediatek.internal.telephony.gsm.MtkGsmSMSDispatcher.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Integer initialValue() {
            return 0;
        }
    };
    protected Object mLock = new Object();
    private boolean mStorageAvailable = true;
    private boolean mSuccess = true;
    protected int messageCountNeedCopy = 0;

    public MtkGsmSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController, GsmInboundSmsHandler gsmInboundSmsHandler) {
        super(phone, smsDispatchersController, gsmInboundSmsHandler);
        this.mUiccController.unregisterForIccChanged(this);
        this.mUiccController.registerForIccChanged(this, 15, new Integer(this.mPhone.getPhoneId()));
        Rlog.d(TAG, "MtkGsmSMSDispatcher created");
    }

    public void dispose() {
        MtkGsmSMSDispatcher.super.dispose();
        this.mCi.unSetOnSmsStatus(this);
        this.mUiccController.unregisterForIccChanged(this);
    }

    public String getFormat() {
        return "3gpp";
    }

    public void handleMessage(Message msg) {
        try {
            int i = msg.what;
            if (i == 15) {
                Integer phoneId = getUiccControllerPhoneId(msg);
                if (phoneId.intValue() != this.mPhone.getPhoneId()) {
                    Rlog.d(TAG, "Wrong phone id event coming, PhoneId: " + phoneId);
                    return;
                }
                Rlog.d(TAG, "EVENT_ICC_CHANGED, PhoneId: " + phoneId + " match exactly.");
                onUpdateIccAvailability();
            } else if (i == EVENT_COPY_TEXT_MESSAGE_DONE) {
                AsyncResult ar = (AsyncResult) msg.obj;
                synchronized (this.mLock) {
                    this.mSuccess = ar.exception == null;
                    if (this.mSuccess) {
                        Rlog.d(TAG, "[copyText success to copy one");
                        this.messageCountNeedCopy--;
                    } else {
                        Rlog.d(TAG, "[copyText fail to copy one");
                        this.messageCountNeedCopy = 0;
                    }
                    this.mLock.notifyAll();
                }
            } else if (i != EVENT_ADD_DELIVER_PENDING_LIST) {
                MtkGsmSMSDispatcher.super.handleMessage(msg);
            } else {
                SMSDispatcher.SmsTracker tracker = (SMSDispatcher.SmsTracker) msg.obj;
                Rlog.d(TAG, "EVENT_ADD_DELIVER_PENDING_LIST mMessageRef=" + tracker.mMessageRef);
                this.deliveryPendingList.add(tracker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public SMSDispatcher.SmsTracker getNewSubmitPduTracker(String callingPackage, String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText, int priority, boolean expectMore, int validityPeriod) {
        if (ENG) {
            Rlog.d(TAG, "getNewSubmitPduTracker w/ validity");
        }
        SmsMessage.SubmitPdu pdu = MtkSmsMessage.getSubmitPdu(scAddress, destinationAddress, message, deliveryIntent != null, MtkSmsHeader.toByteArray(smsHeader), encoding, smsHeader.languageTable, smsHeader.languageShiftTable, validityPeriod);
        if (pdu != null) {
            return getSmsTracker(callingPackage, getSmsTrackerMap(destinationAddress, scAddress, message, pdu), sentIntent, deliveryIntent, getFormat(), unsentPartCount, anyPartFailed, messageUri, smsHeader, !lastPart || expectMore, fullMessageText, true, true, priority, validityPeriod, false);
        }
        Rlog.e(TAG, "GsmSMSDispatcher.getNewSubmitPduTracker(): getSubmitPdu() returned null");
        return null;
    }

    private Integer getUiccControllerPhoneId(Message msg) {
        Integer phoneId = new Integer(-1);
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null || !(ar.result instanceof Integer)) {
            return phoneId;
        }
        return (Integer) ar.result;
    }

    public void sendData(String callingPackage, String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        Rlog.d(TAG, "MtkGsmSmsDispatcher.sendData: enter");
        SmsMessage.SubmitPdu pdu = MtkSmsMessage.getSubmitPdu(scAddr, destAddr, destPort, originalPort, data, deliveryIntent != null);
        if (pdu != null) {
            SMSDispatcher.SmsTracker tracker = getSmsTracker(callingPackage, getSmsTrackerMap(destAddr, scAddr, destPort, data, pdu), sentIntent, deliveryIntent, getFormat(), null, false, null, false, true, false);
            if (!sendSmsByCarrierApp(true, tracker)) {
                sendSubmitPdu(tracker);
                return;
            }
            return;
        }
        Rlog.e(TAG, "GsmSMSDispatcher.sendData(): getSubmitPdu() returned null");
    }

    public void sendMultipartData(String callingPackage, String destAddr, String scAddr, int destPort, ArrayList<SmsRawData> data, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        int i;
        PendingIntent sentIntent;
        PendingIntent deliveryIntent;
        ArrayList<SmsRawData> arrayList = data;
        String str = TAG;
        if (arrayList == null) {
            Rlog.e(str, "Cannot send multipart data when data is null!");
            return;
        }
        int refNumber = getNextConcatenatedRef() & 255;
        int msgCount = data.size();
        SMSDispatcher.SmsTracker[] trackers = new SMSDispatcher.SmsTracker[msgCount];
        int i2 = 0;
        while (true) {
            boolean z = false;
            if (i2 >= msgCount) {
                break;
            }
            byte[] smsHeader = MtkSmsHeader.getSubmitPduHeader(destPort, refNumber, i2 + 1, msgCount);
            if (sentIntents == null || sentIntents.size() <= i2) {
                sentIntent = null;
            } else {
                sentIntent = sentIntents.get(i2);
            }
            if (deliveryIntents == null || deliveryIntents.size() <= i2) {
                deliveryIntent = null;
            } else {
                deliveryIntent = deliveryIntents.get(i2);
            }
            byte[] bytes = arrayList.get(i2).getBytes();
            if (deliveryIntent != null) {
                z = true;
            }
            trackers[i2] = getSmsTracker(callingPackage, getSmsTrackerMap(destAddr, scAddr, destPort, arrayList.get(i2).getBytes(), MtkSmsMessage.getSubmitPdu(scAddr, destAddr, bytes, smsHeader, z)), sentIntent, deliveryIntent, getFormat(), null, false, null, false, true, false);
            i2++;
            trackers = trackers;
            str = str;
            msgCount = msgCount;
            refNumber = refNumber;
            arrayList = data;
        }
        if (trackers.length == 0 || trackers[0] == null) {
            Rlog.e(str, "Cannot send multipart data. trackers length = " + trackers.length);
            return;
        }
        for (SMSDispatcher.SmsTracker tracker : trackers) {
            if (tracker == null) {
                Rlog.e(str, "Null tracker.");
            } else if (!sendSmsByCarrierApp(true, tracker)) {
                sendSubmitPdu(tracker);
            }
        }
    }

    /* JADX INFO: Multiple debug info for r13v6 'i'  int: [D('singleShiftId' int), D('i' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0137  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0157  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0166  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01c3  */
    public int copyTextMessageToIccCard(String scAddress, String address, List<String> text, int status, long timestamp) {
        String scAddress2;
        boolean isDeliverPdu;
        int encoding_method;
        int encoding_method2;
        int singleShiftId;
        byte[] smsHeader;
        GsmAlphabet.TextEncodingDetails[] details;
        int i;
        int language;
        int encoding;
        int refNumber;
        int singleShiftId2;
        Rlog.d(TAG, "GsmSMSDispatcher: copy text message to icc card");
        if (!checkPhoneNumber(scAddress)) {
            Rlog.d(TAG, "[copyText invalid sc address");
            scAddress2 = null;
        } else {
            scAddress2 = scAddress;
        }
        int i2 = 1;
        this.mSuccess = true;
        int msgCount = text.size();
        Rlog.d(TAG, "[copyText storage available");
        if (status == 1 || status == 3) {
            Rlog.d(TAG, "[copyText to encode deliver pdu");
            isDeliverPdu = true;
        } else if (status == 5 || status == 7) {
            Rlog.d(TAG, "[copyText to encode submit pdu");
            isDeliverPdu = false;
        } else {
            Rlog.d(TAG, "[copyText invalid status, default is deliver pdu");
            return 1;
        }
        if (!isDeliverPdu) {
            if (!checkPhoneNumber(address)) {
                Rlog.d(TAG, "[copyText invalid dest address");
                return 8;
            }
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
        int refNumber2 = getNextConcatenatedRef() & 255;
        GsmAlphabet.TextEncodingDetails[] details2 = new GsmAlphabet.TextEncodingDetails[msgCount];
        int encoding2 = 0;
        for (int i3 = 0; i3 < msgCount; i3++) {
            details2[i3] = MtkSmsMessage.calculateLength(text.get(i3), false);
            if (encoding2 != details2[i3].codeUnitSize && (encoding2 == 0 || encoding2 == 1)) {
                encoding2 = details2[i3].codeUnitSize;
            }
        }
        int i4 = 0;
        while (i4 < msgCount) {
            if (!this.mSuccess) {
                Rlog.d(TAG, "[copyText Exception happened when copy message");
                return i2;
            }
            int language2 = details2[i4].shiftLangId;
            if (encoding2 == i2) {
                Rlog.d(TAG, "Detail: " + i4 + " ted" + details2[i4]);
                if (!details2[i4].useLockingShift || !details2[i4].useSingleShift) {
                    if (details2[i4].useLockingShift) {
                        encoding_method2 = language2;
                        singleShiftId = -1;
                        encoding_method = 12;
                    } else if (details2[i4].useSingleShift) {
                        singleShiftId = language2;
                        encoding_method = 11;
                        encoding_method2 = -1;
                    }
                    if (msgCount > 1) {
                        Rlog.d(TAG, "[copyText get pdu header for multi-part message");
                        language = language2;
                        i = i4;
                        encoding = encoding2;
                        details = details2;
                        smsHeader = MtkSmsHeader.getSubmitPduHeaderWithLang(-1, refNumber2, i4 + 1, msgCount, singleShiftId, encoding_method2);
                    } else {
                        language = language2;
                        i = i4;
                        encoding = encoding2;
                        details = details2;
                        smsHeader = null;
                    }
                    if (isDeliverPdu) {
                        singleShiftId2 = i;
                        refNumber = refNumber2;
                        MtkSmsMessage.DeliverPdu pdu = MtkSmsMessage.getDeliverPduWithLang(scAddress2, address, text.get(i), smsHeader, timestamp, encoding, language);
                        if (pdu != null) {
                            Rlog.d(TAG, "[copyText write deliver pdu into SIM");
                            this.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(pdu.encodedScAddress), IccUtils.bytesToHexString(pdu.encodedMessage), obtainMessage(EVENT_COPY_TEXT_MESSAGE_DONE));
                            synchronized (this.mLock) {
                                try {
                                    Rlog.d(TAG, "[copyText wait until the message be wrote in SIM");
                                    this.mLock.wait();
                                } catch (InterruptedException e) {
                                    Rlog.d(TAG, "Fail to copy text message into SIM");
                                    return 1;
                                } catch (Throwable th) {
                                    throw th;
                                }
                            }
                        }
                    } else {
                        refNumber = refNumber2;
                        singleShiftId2 = i;
                        SmsMessage.SubmitPdu pdu2 = MtkSmsMessage.getSubmitPduWithLang(scAddress2, address, text.get(singleShiftId2), false, smsHeader, encoding_method, language, -1);
                        if (pdu2 != null) {
                            Rlog.d(TAG, "[copyText write submit pdu into SIM");
                            this.mCi.writeSmsToSim(status, IccUtils.bytesToHexString(pdu2.encodedScAddress), IccUtils.bytesToHexString(pdu2.encodedMessage), obtainMessage(EVENT_COPY_TEXT_MESSAGE_DONE));
                            synchronized (this.mLock) {
                                try {
                                    Rlog.d(TAG, "[copyText wait until the message be wrote in SIM");
                                    this.mLock.wait();
                                } catch (InterruptedException e2) {
                                    Rlog.d(TAG, "fail to copy text message into SIM");
                                    return 1;
                                } catch (Throwable th2) {
                                    throw th2;
                                }
                            }
                        }
                    }
                    Rlog.d(TAG, "[copyText thread is waked up");
                    i4 = singleShiftId2 + 1;
                    encoding2 = encoding;
                    details2 = details;
                    refNumber2 = refNumber;
                    i2 = 1;
                } else {
                    singleShiftId = language2;
                    encoding_method2 = language2;
                    encoding_method = 13;
                    if (msgCount > 1) {
                    }
                    if (isDeliverPdu) {
                    }
                    Rlog.d(TAG, "[copyText thread is waked up");
                    i4 = singleShiftId2 + 1;
                    encoding2 = encoding;
                    details2 = details;
                    refNumber2 = refNumber;
                    i2 = 1;
                }
            }
            singleShiftId = -1;
            encoding_method = encoding2;
            encoding_method2 = -1;
            if (msgCount > 1) {
            }
            if (isDeliverPdu) {
            }
            Rlog.d(TAG, "[copyText thread is waked up");
            i4 = singleShiftId2 + 1;
            encoding2 = encoding;
            details2 = details;
            refNumber2 = refNumber;
            i2 = 1;
        }
        if (this.mSuccess) {
            Rlog.d(TAG, "[copyText all messages have been copied into SIM");
            return 0;
        }
        Rlog.d(TAG, "[copyText copy failed");
        return 1;
    }

    private boolean isValidSmsAddress(String address) {
        String encodedAddress = PhoneNumberUtils.extractNetworkPortion(address);
        return encodedAddress == null || encodedAddress.length() == address.length();
    }

    private boolean checkPhoneNumber(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '+' || c == '#' || c == 'N' || c == ' ' || c == '-';
    }

    private boolean checkPhoneNumber(String address) {
        if (address == null) {
            return true;
        }
        Rlog.d(TAG, "checkPhoneNumber");
        int n = address.length();
        for (int i = 0; i < n; i++) {
            if (!checkPhoneNumber(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public void sendTextWithEncodingType(String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        Rlog.d(TAG, "sendTextWithEncodingType encoding = " + encodingType);
        this.mEncodingType.set(Integer.valueOf(encodingType));
        sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, false);
        this.mEncodingType.remove();
    }

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase onSendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        int encodingType = this.mEncodingType.get().intValue();
        if (encodingType == 0) {
            return MtkGsmSMSDispatcher.super.onSendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
        }
        boolean z = false;
        GsmAlphabet.TextEncodingDetails details = SMSDispatcherUtil.calculateLength(false, text, false);
        if (deliveryIntent != null) {
            z = true;
        }
        return SmsMessage.getSubmitPdu(scAddr, destAddr, text, z, (byte[]) null, encodingType, details.languageTable, details.languageShiftTable, validityPeriod);
    }

    public void sendMultipartTextWithEncodingType(String destAddr, String scAddr, ArrayList<String> parts, int encodingType, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        Rlog.d(TAG, "sendMultipartTextWithEncodingType encoding = " + encodingType);
        this.mEncodingType.set(Integer.valueOf(encodingType));
        sendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
        this.mEncodingType.remove();
    }

    /* JADX DEBUG: Additional 1 move instruction added to help type inference */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v2 */
    /* JADX WARN: Type inference failed for: r6v3, types: [int] */
    public void sendMultipartText(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        PendingIntent sentIntent;
        PendingIntent deliveryIntent;
        ArrayList<PendingIntent> arrayList = sentIntents;
        String fullMessageText = getMultipartMessageText(parts);
        int refNumber = getNextConcatenatedRef() & 255;
        int msgCount = parts.size();
        if (msgCount < 1) {
            triggerSentIntentForFailure(arrayList);
            return;
        }
        GsmAlphabet.TextEncodingDetails[] encodingForParts = new GsmAlphabet.TextEncodingDetails[msgCount];
        int msgCount2 = msgCount;
        int refNumber2 = refNumber;
        int encoding = onSendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingForParts);
        SMSDispatcher.SmsTracker[] trackers = new SMSDispatcher.SmsTracker[msgCount2];
        AtomicInteger unsentPartCount = new AtomicInteger(msgCount2);
        boolean z = false;
        AtomicBoolean anyPartFailed = new AtomicBoolean(false);
        int i = 0;
        while (i < msgCount2) {
            SmsHeader.ConcatRef concatRef = new SmsHeader.ConcatRef();
            concatRef.refNumber = refNumber2;
            concatRef.seqNumber = i + 1;
            concatRef.msgCount = msgCount2;
            concatRef.isEightBits = true;
            SmsHeader smsHeader = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName()).makeSmsHeader();
            smsHeader.concatRef = concatRef;
            if (encoding == 1) {
                smsHeader.languageTable = encodingForParts[i].languageTable;
                smsHeader.languageShiftTable = encodingForParts[i].languageShiftTable;
            }
            if (arrayList == null || sentIntents.size() <= i) {
                sentIntent = null;
            } else {
                sentIntent = arrayList.get(i);
            }
            if (deliveryIntents == null || deliveryIntents.size() <= i) {
                deliveryIntent = null;
            } else {
                deliveryIntent = deliveryIntents.get(i);
            }
            trackers[i] = getNewSubmitPduTracker(callingPkg, destAddr, scAddr, parts.get(i), smsHeader, encoding, sentIntent, deliveryIntent, i == msgCount2 + -1 ? true : z, unsentPartCount, anyPartFailed, messageUri, fullMessageText, priority, expectMore, validityPeriod);
            if (trackers[i] == null) {
                triggerSentIntentForFailure(sentIntents);
                return;
            }
            i++;
            trackers = trackers;
            arrayList = sentIntents;
            refNumber2 = refNumber2;
            encoding = encoding;
            z = z;
            msgCount2 = msgCount2;
        }
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            Rlog.d(TAG, "Found carrier package.");
            SMSDispatcher.MultipartSmsSender smsSender = new SMSDispatcher.MultipartSmsSender(this, parts, trackers);
            smsSender.sendSmsByCarrierApp(carrierPackage, new SMSDispatcher.MultipartSmsSenderCallback(this, smsSender));
            return;
        }
        Rlog.v(TAG, "No carrier package.");
        int length = trackers.length;
        for (int i2 = z; i2 < length; i2++) {
            sendSubmitPdu(trackers[i2]);
        }
    }

    /* access modifiers changed from: protected */
    public int onSendMultipartText(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, GsmAlphabet.TextEncodingDetails[] encodingForParts) {
        int encodingType = this.mEncodingType.get().intValue();
        if (encodingType == 0) {
            int encodingType2 = MtkGsmSMSDispatcher.super.onSendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingForParts);
            Rlog.d(TAG, "onSendMultipartText encoding = " + encodingType2);
            return encodingType2;
        }
        int msgCount = parts.size();
        for (int i = 0; i < msgCount; i++) {
            GsmAlphabet.TextEncodingDetails details = SMSDispatcherUtil.calculateLength(false, parts.get(i), false);
            if (encodingType != details.codeUnitSize && (encodingType == 0 || encodingType == 1)) {
                Rlog.d(TAG, "[enc conflict between details[" + details.codeUnitSize + "] and encoding " + encodingType);
                details.codeUnitSize = encodingType;
            }
            encodingForParts[i] = details;
        }
        return encodingType;
    }

    public void handleIccFull() {
        this.mGsmInboundSmsHandler.mStorageMonitor.handleIccFull();
    }

    public void handleQueryCbActivation(AsyncResult ar) {
        Boolean result = null;
        if (ar.exception == null) {
            ArrayList<SmsBroadcastConfigInfo> list = (ArrayList) ar.result;
            if (list.size() == 0) {
                result = new Boolean(false);
            } else {
                SmsBroadcastConfigInfo cbConfig = list.get(0);
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

    public void setSmsMemoryStatus(boolean status) {
        if (status != this.mStorageAvailable) {
            this.mStorageAvailable = status;
            this.mCi.reportSmsMemoryStatus(status, (Message) null);
        }
    }

    public boolean isSmsReady() {
        return this.mSmsDispatchersController.isSmsReady();
    }

    /* access modifiers changed from: protected */
    public String getPackageNameViaProcessId(String[] packageNames) {
        return MtkSMSDispatcherUtil.getPackageNameViaProcessId(this.mContext, packageNames);
    }

    /* access modifiers changed from: protected */
    public void sendMultipartSms(SMSDispatcher.SmsTracker tracker) {
        HashMap<String, Object> map = tracker.getData();
        ArrayList<String> parts = (ArrayList) map.get("parts");
        ArrayList<PendingIntent> sentIntents = (ArrayList) map.get("sentIntents");
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0 || this.mTelephonyManager.isWifiCallingAvailable()) {
            MtkGsmSMSDispatcher.super.sendMultipartSms(tracker);
            return;
        }
        int count = parts.size();
        for (int i = 0; i < count; i++) {
            PendingIntent sentIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                sentIntent = sentIntents.get(i);
            }
            handleNotInService(ss, sentIntent);
        }
    }

    /* access modifiers changed from: protected */
    public SMSDispatcher.SmsTracker getSmsTracker(String callingPackage, HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean isExpectMore, String fullMessageText, boolean isText, boolean persistMessage, boolean isForVvm) {
        SMSDispatcher.SmsTracker tracker = MtkGsmSMSDispatcher.super.getSmsTracker(callingPackage, data, sentIntent, deliveryIntent, format, unsentPartCount, anyPartFailed, messageUri, smsHeader, isExpectMore, fullMessageText, isText, persistMessage, -1, -1, isForVvm);
        MtkSmsCommonUtil.filterOutByPpl(this.mContext, tracker);
        return tracker;
    }

    /* access modifiers changed from: protected */
    public SMSDispatcher.SmsTracker getSmsTracker(String callingPackage, HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean expectMore, String fullMessageText, boolean isText, boolean persistMessage, int priority, int validityPeriod, boolean isForVvm) {
        SMSDispatcher.SmsTracker tracker = MtkGsmSMSDispatcher.super.getSmsTracker(callingPackage, data, sentIntent, deliveryIntent, format, unsentPartCount, anyPartFailed, messageUri, smsHeader, expectMore, fullMessageText, isText, persistMessage, priority, validityPeriod, isForVvm);
        MtkSmsCommonUtil.filterOutByPpl(this.mContext, tracker);
        return tracker;
    }

    public void addToGsmDeliverPendingList(SMSDispatcher.SmsTracker tracker) {
        if (tracker.mDeliveryIntent != null) {
            Rlog.d(TAG, "addToGsmDeliverPendingList sendMessage");
            sendMessage(obtainMessage(EVENT_ADD_DELIVER_PENDING_LIST, tracker));
        }
    }
}

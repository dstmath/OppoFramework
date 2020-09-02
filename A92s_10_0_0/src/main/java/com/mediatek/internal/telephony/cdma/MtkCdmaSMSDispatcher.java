package com.mediatek.internal.telephony.cdma;

import android.app.PendingIntent;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsResponse;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.cdma.SmsMessage;
import com.android.internal.telephony.uicc.IccUtils;
import com.mediatek.internal.telephony.util.MtkSMSDispatcherUtil;
import com.mediatek.internal.telephony.util.MtkSmsCommonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MtkCdmaSMSDispatcher extends CdmaSMSDispatcher {
    private static final boolean ENG = "eng".equals(Build.TYPE);
    private static final int EVENT_COPY_TEXT_MESSAGE_DONE = 106;
    private static final int RESULT_ERROR_RUIM_PLUG_OUT = 107;
    private static final int RESULT_ERROR_SUCCESS = 0;
    private static final String TAG = "MtkCdmaSMSDispatcher";
    private static final boolean VDBG = false;
    private static final int WAKE_LOCK_TIMEOUT = 500;
    private boolean mCopied = false;
    private ThreadLocal<Integer> mEncodingType = new ThreadLocal<Integer>() {
        /* class com.mediatek.internal.telephony.cdma.MtkCdmaSMSDispatcher.AnonymousClass2 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Integer initialValue() {
            return 0;
        }
    };
    protected Object mLock = new Object();
    private ThreadLocal<Integer> mOriginalPort = new ThreadLocal<Integer>() {
        /* class com.mediatek.internal.telephony.cdma.MtkCdmaSMSDispatcher.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Integer initialValue() {
            return -1;
        }
    };
    protected boolean mSuccess = true;

    public MtkCdmaSMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        super(phone, smsDispatchersController);
        Rlog.d(TAG, "MtkCdmaSMSDispatcher created");
    }

    public void sendSms(SMSDispatcher.SmsTracker tracker) {
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0) {
            MtkCdmaSMSDispatcher.super.sendSms(tracker);
        } else if (isSimAbsent()) {
            tracker.onFailed(this.mContext, 1, 0);
        } else {
            tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
        }
    }

    /* access modifiers changed from: protected */
    public SMSDispatcher.SmsTracker getSmsTracker(String callingPackage, HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean expectMore, String fullMessageText, boolean isText, boolean persistMessage, int priority, int validityPeriod, boolean isForVvm) {
        SMSDispatcher.SmsTracker tracker = MtkCdmaSMSDispatcher.super.getSmsTracker(callingPackage, data, sentIntent, deliveryIntent, format, unsentPartCount, anyPartFailed, messageUri, smsHeader, expectMore, fullMessageText, isText, persistMessage, priority, validityPeriod, isForVvm);
        MtkSmsCommonUtil.filterOutByPpl(this.mContext, tracker);
        return tracker;
    }

    /* access modifiers changed from: protected */
    public String getPackageNameViaProcessId(String[] packageNames) {
        return MtkSMSDispatcherUtil.getPackageNameViaProcessId(this.mContext, packageNames);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: SimpleMethodDetails{com.mediatek.internal.telephony.cdma.MtkCdmaSMSDispatcher.sendData(java.lang.String, java.lang.String, java.lang.String, int, byte[], android.app.PendingIntent, android.app.PendingIntent, boolean):void}
     arg types: [java.lang.String, java.lang.String, java.lang.String, int, byte[], android.app.PendingIntent, android.app.PendingIntent, int]
     candidates:
      com.mediatek.internal.telephony.cdma.MtkCdmaSMSDispatcher.sendData(java.lang.String, java.lang.String, java.lang.String, int, int, byte[], android.app.PendingIntent, android.app.PendingIntent):void
      SimpleMethodDetails{com.mediatek.internal.telephony.cdma.MtkCdmaSMSDispatcher.sendData(java.lang.String, java.lang.String, java.lang.String, int, byte[], android.app.PendingIntent, android.app.PendingIntent, boolean):void} */
    public void sendData(String callingPackage, String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        this.mOriginalPort.set(Integer.valueOf(originalPort));
        sendData(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent, false);
        this.mOriginalPort.remove();
    }

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase onSendData(String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (this.mOriginalPort.get().intValue() == -1) {
            return MtkCdmaSMSDispatcher.super.onSendData(destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
        }
        return MtkSmsMessage.getSubmitPdu(scAddr, destAddr, destPort, this.mOriginalPort.get().intValue(), data, deliveryIntent != null);
    }

    public int copyTextMessageToIccCard(String scAddress, String address, List<String> text, int status, long timestamp) {
        SmsMessage.SubmitPdu pdu;
        this.mSuccess = true;
        int msgCount = text.size();
        Rlog.d(TAG, "copyTextMessageToIccCard status = " + status + ", msgCount = " + msgCount);
        if ((status != 1 && status != 3 && status != 5 && status != 7) || msgCount < 1) {
            return 1;
        }
        for (int i = 0; i < msgCount; i++) {
            if (!this.mSuccess || (pdu = MtkSmsMessage.createEfPdu(address, text.get(i), timestamp)) == null) {
                return 1;
            }
            this.mCi.writeSmsToRuim(status, IccUtils.bytesToHexString(pdu.encodedMessage), obtainMessage(EVENT_COPY_TEXT_MESSAGE_DONE));
            synchronized (this.mLock) {
                this.mCopied = false;
                while (!this.mCopied) {
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                        return 1;
                    }
                }
            }
        }
        return 1 ^ this.mSuccess;
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
        boolean z = true;
        if (encodingType != 1 && encodingType != 2 && encodingType != 3) {
            return MtkCdmaSMSDispatcher.super.onSendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
        }
        if (deliveryIntent == null) {
            z = false;
        }
        return MtkSmsMessage.getSubmitPdu(scAddr, destAddr, text, z, (SmsHeader) null, encodingType, validityPeriod, priority, true);
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
        if (encodingType != 1 && encodingType != 2 && encodingType != 3) {
            return MtkCdmaSMSDispatcher.super.onSendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingForParts);
        }
        int msgCount = parts.size();
        for (int i = 0; i < msgCount; i++) {
            GsmAlphabet.TextEncodingDetails details = MtkSmsMessage.calculateLength(parts.get(i), false, encodingType);
            details.codeUnitSize = encodingType;
            encodingForParts[i] = details;
        }
        return encodingType;
    }

    public void handleMessage(Message msg) {
        if (msg.what != EVENT_COPY_TEXT_MESSAGE_DONE) {
            MtkCdmaSMSDispatcher.super.handleMessage(msg);
            return;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        synchronized (this.mLock) {
            this.mSuccess = ar.exception == null;
            this.mCopied = true;
            this.mLock.notifyAll();
        }
    }

    /* access modifiers changed from: protected */
    public void handleSendComplete(AsyncResult ar) {
        SMSDispatcher.SmsTracker tracker = (SMSDispatcher.SmsTracker) ar.userObj;
        if (!(ar.exception == null || ar.result == null)) {
            int errorCode = ((SmsResponse) ar.result).mErrorCode;
            if (errorCode == RESULT_ERROR_RUIM_PLUG_OUT) {
                Rlog.d(TAG, "RUIM card is plug out");
                tracker.onFailed(this.mContext, 1, errorCode);
                return;
            }
            int ss = this.mPhone.getServiceState().getState();
            if (!isIms() && ss != 0 && isSimAbsent()) {
                tracker.onFailed(this.mContext, 1, errorCode);
                return;
            }
        }
        MtkCdmaSMSDispatcher.super.handleSendComplete(ar);
    }

    private boolean isSimAbsent() {
        IccCardConstants.State state;
        IccCard card = PhoneFactory.getPhone(this.mPhone.getPhoneId()).getIccCard();
        if (card == null) {
            state = IccCardConstants.State.UNKNOWN;
        } else {
            state = card.getState();
        }
        boolean ret = state == IccCardConstants.State.ABSENT || state == IccCardConstants.State.NOT_READY;
        Rlog.d(TAG, "isSimAbsent state = " + state + " ret=" + ret);
        return ret;
    }
}

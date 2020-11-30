package com.mediatek.internal.telephony;

import android.app.PendingIntent;
import android.net.Uri;
import android.os.Build;
import android.telephony.Rlog;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.ImsSmsDispatcher;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsRawData;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.util.SMSDispatcherUtil;
import com.mediatek.internal.telephony.ppl.PplSmsFilterExtension;
import com.mediatek.internal.telephony.util.MtkSMSDispatcherUtil;
import com.mediatek.internal.telephony.util.MtkSmsCommonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MtkImsSmsDispatcher extends ImsSmsDispatcher {
    private static final boolean ENG = "eng".equals(Build.TYPE);
    private static final String TAG = "MtkImsSmsDispacher";
    private ThreadLocal<Integer> mEncodingType = new ThreadLocal<Integer>() {
        /* class com.mediatek.internal.telephony.MtkImsSmsDispatcher.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Integer initialValue() {
            return 0;
        }
    };
    private PplSmsFilterExtension mPplSmsFilter = null;

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase getSubmitPdu(String scAddr, String destAddr, int destinationPort, int originalPort, byte[] data, boolean statusReportRequested) {
        return MtkSMSDispatcherUtil.getSubmitPdu(isCdmaMo(), scAddr, destAddr, destinationPort, originalPort, data, statusReportRequested);
    }

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase getSubmitPdu(String scAddr, String destAddr, byte[] data, byte[] smsHeader, boolean statusReportRequested) {
        return MtkSMSDispatcherUtil.getSubmitPdu(isCdmaMo(), scAddr, destAddr, data, smsHeader, statusReportRequested);
    }

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase getSubmitPdu(boolean isCdma, String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header, int encoding, int languageTable, int languageShiftTable, int validityPeriod) {
        return MtkSMSDispatcherUtil.getSubmitPdu(isCdmaMo(), scAddress, destinationAddress, message, statusReportRequested, header, encoding, languageTable, languageShiftTable, validityPeriod);
    }

    public MtkImsSmsDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        super(phone, smsDispatchersController);
        Rlog.d(TAG, "Created!");
    }

    /* access modifiers changed from: protected */
    public void sendData(String callingPackage, String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        if (!isCdmaMo()) {
            sendDataGsm(callingPackage, destAddr, scAddr, destPort, originalPort, data, sentIntent, deliveryIntent);
        } else {
            MtkImsSmsDispatcher.super.sendData(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent, false);
        }
    }

    public void sendMultipartData(String callingPackage, String destAddr, String scAddr, int destPort, ArrayList<SmsRawData> data, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        if (!isCdmaMo()) {
            sendMultipartDataGsm(callingPackage, destAddr, scAddr, destPort, data, sentIntents, deliveryIntents);
        } else {
            Rlog.d(TAG, "Don't support sendMultipartData for CDMA");
        }
    }

    public void sendTextWithEncodingType(String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        if (!isCdmaMo()) {
            sendTextWithEncodingTypeGsm(destAddr, scAddr, text, encodingType, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
        } else {
            sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, false);
        }
    }

    public void sendMultipartTextWithEncodingType(String destAddr, String scAddr, ArrayList<String> parts, int encodingType, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        if (!isCdmaMo()) {
            sendMultipartTextWithEncodingTypeGsm(destAddr, scAddr, parts, encodingType, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
        } else {
            sendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
        }
    }

    /* access modifiers changed from: protected */
    public SMSDispatcher.SmsTracker getNewSubmitPduTracker(String callingPackage, String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText, int priority, boolean expectMore, int validityPeriod) {
        if (ENG) {
            Rlog.d(TAG, "getNewSubmitPduTracker w/ validity");
        }
        if (isCdmaMo()) {
            return MtkImsSmsDispatcher.super.getNewSubmitPduTracker(callingPackage, destinationAddress, scAddress, message, smsHeader, encoding, sentIntent, deliveryIntent, lastPart, unsentPartCount, anyPartFailed, messageUri, fullMessageText, priority, expectMore, validityPeriod);
        }
        return getNewSubmitPduTrackerGsm(callingPackage, destinationAddress, scAddress, message, smsHeader, encoding, sentIntent, deliveryIntent, lastPart, unsentPartCount, anyPartFailed, messageUri, fullMessageText, priority, expectMore, validityPeriod);
    }

    /* access modifiers changed from: protected */
    public void sendDataGsm(String callingPackage, String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        Rlog.d(TAG, "sendData: enter");
        SmsMessageBase.SubmitPduBase pdu = getSubmitPdu(scAddr, destAddr, destPort, originalPort, data, deliveryIntent != null);
        if (pdu != null) {
            SMSDispatcher.SmsTracker tracker = getSmsTracker(callingPackage, getSmsTrackerMap(destAddr, scAddr, destPort, data, pdu), sentIntent, deliveryIntent, getFormat(), null, false, null, false, true, false);
            if (!sendSmsByCarrierApp(true, tracker)) {
                sendSubmitPdu(tracker);
                return;
            }
            return;
        }
        Rlog.e(TAG, "sendData(): getSubmitPdu() returned null");
    }

    public void sendMultipartDataGsm(String callingPackage, String destAddr, String scAddr, int destPort, ArrayList<SmsRawData> data, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        int i;
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
            if (i2 >= msgCount) {
                break;
            }
            byte[] smsHeader = MtkSmsHeader.getSubmitPduHeader(destPort, refNumber, i2 + 1, msgCount);
            PendingIntent sentIntent = (sentIntents == null || sentIntents.size() <= i2) ? null : sentIntents.get(i2);
            PendingIntent deliveryIntent = (deliveryIntents == null || deliveryIntents.size() <= i2) ? null : deliveryIntents.get(i2);
            trackers[i2] = getSmsTracker(callingPackage, getSmsTrackerMap(destAddr, scAddr, destPort, arrayList.get(i2).getBytes(), getSubmitPdu(scAddr, destAddr, arrayList.get(i2).getBytes(), smsHeader, deliveryIntent != null)), sentIntent, deliveryIntent, getFormat(), null, false, null, false, true, false);
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

    public void sendTextWithEncodingTypeGsm(String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        Rlog.d(TAG, "sendTextWithEncodingTypeGsm encoding = " + encodingType);
        this.mEncodingType.set(Integer.valueOf(encodingType));
        sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, false);
        this.mEncodingType.remove();
    }

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase onSendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        int encodingType = this.mEncodingType.get().intValue();
        if (encodingType == 0) {
            return MtkImsSmsDispatcher.super.onSendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
        }
        boolean z = false;
        GsmAlphabet.TextEncodingDetails details = SMSDispatcherUtil.calculateLength(false, text, false);
        if (deliveryIntent != null) {
            z = true;
        }
        return getSubmitPdu(false, scAddr, destAddr, text, z, null, encodingType, details.languageTable, details.languageShiftTable, validityPeriod);
    }

    public void sendMultipartTextWithEncodingTypeGsm(String destAddr, String scAddr, ArrayList<String> parts, int encodingType, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        Rlog.d(TAG, "sendMultipartTextWithEncodingType encoding = " + encodingType);
        this.mEncodingType.set(Integer.valueOf(encodingType));
        sendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
        this.mEncodingType.remove();
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v2 */
    /* JADX WARN: Type inference failed for: r6v3 */
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
        int i2 = z;
        while (i2 < length) {
            sendSubmitPdu(trackers[i2 == true ? 1 : 0]);
            i2 = (i2 == true ? 1 : 0) + 1;
        }
    }

    /* access modifiers changed from: protected */
    public int onSendMultipartText(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, GsmAlphabet.TextEncodingDetails[] encodingForParts) {
        int encodingType = this.mEncodingType.get().intValue();
        if (encodingType == 0) {
            int encodingType2 = MtkImsSmsDispatcher.super.onSendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingForParts);
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

    /* access modifiers changed from: protected */
    public SMSDispatcher.SmsTracker getSmsTracker(String callingPackage, HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, Uri messageUri, boolean expectMore, String fullMessageText, boolean isText, boolean persistMessage, boolean isForVvm) {
        return getSmsTracker(callingPackage, data, sentIntent, deliveryIntent, format, null, null, messageUri, null, expectMore, fullMessageText, isText, persistMessage, -1, -1, isForVvm);
    }

    /* access modifiers changed from: protected */
    public SMSDispatcher.SmsTracker getSmsTracker(String callingPackage, HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean expectMore, String fullMessageText, boolean isText, boolean persistMessage, int priority, int validityPeriod, boolean isForVvm) {
        SMSDispatcher.SmsTracker tracker = MtkImsSmsDispatcher.super.getSmsTracker(callingPackage, data, sentIntent, deliveryIntent, format, unsentPartCount, anyPartFailed, messageUri, smsHeader, expectMore, fullMessageText, isText, persistMessage, priority, validityPeriod, isForVvm);
        MtkSmsCommonUtil.filterOutByPpl(this.mContext, tracker);
        return tracker;
    }

    /* access modifiers changed from: protected */
    public void sendMultipartSms(SMSDispatcher.SmsTracker tracker) {
        HashMap<String, Object> map = tracker.getData();
        ArrayList<String> parts = (ArrayList) map.get("parts");
        ArrayList<PendingIntent> sentIntents = (ArrayList) map.get("sentIntents");
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0 || this.mTelephonyManager.isWifiCallingAvailable()) {
            MtkImsSmsDispatcher.super.sendMultipartSms(tracker);
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

    private SMSDispatcher.SmsTracker getNewSubmitPduTrackerGsm(String callingPackage, String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText, int priority, boolean expectMore, int validityPeriod) {
        SmsMessageBase.SubmitPduBase pdu = getSubmitPdu(false, scAddress, destinationAddress, message, deliveryIntent != null, MtkSmsHeader.toByteArray(smsHeader), encoding, smsHeader.languageTable, smsHeader.languageShiftTable, validityPeriod);
        if (pdu != null) {
            return getSmsTracker(callingPackage, getSmsTrackerMap(destinationAddress, scAddress, message, pdu), sentIntent, deliveryIntent, getFormat(), unsentPartCount, anyPartFailed, messageUri, smsHeader, !lastPart || expectMore, fullMessageText, true, true, priority, validityPeriod, false);
        }
        Rlog.e(TAG, "getNewSubmitPduTrackerGsm: getSubmitPdu() returned null");
        return null;
    }

    /* access modifiers changed from: protected */
    public String getPackageNameViaProcessId(String[] packageNames) {
        return MtkSMSDispatcherUtil.getPackageNameViaProcessId(this.mContext, packageNames);
    }
}

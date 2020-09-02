package com.oppo.internal.telephony;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncResult;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.IOppoSMSDispatcher;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsResponse;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseUtils;
import com.oppo.internal.telephony.utils.OppoPolicyController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class OppoSMSDispatcherReference implements IOppoSMSDispatcher {
    private static final String TAG = "OppoSMSDispatcherReference";
    private Object mOppoUsageManager = null;
    private SMSDispatcher mRef;

    public OppoSMSDispatcherReference(SMSDispatcher ref) {
        this.mRef = ref;
    }

    public void sendTextOem(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, boolean isForVvm, int encodingType) {
        OppoRlog.Rlog.d(TAG, "sendTextOem, encodingType=" + encodingType + " priority=" + priority + " expectMore=" + expectMore + " isForVvm=" + isForVvm + " validityPeriod=" + validityPeriod);
        SmsMessageBase.SubmitPduBase pdu = sendTextOemHookForEncoding(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingType);
        if (pdu != null) {
            SMSDispatcher.SmsTracker tracker = (SMSDispatcher.SmsTracker) ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SMSDispatcher", "getSmsTrackerPublic", new Class[]{String.class, new HashMap().getClass(), PendingIntent.class, PendingIntent.class, String.class, Uri.class, Boolean.TYPE, String.class, Boolean.TYPE, Boolean.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE}, new Object[]{callingPkg, (HashMap) ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SMSDispatcher", "getSmsTrackerMapPublic", new Class[]{String.class, String.class, String.class, SmsMessageBase.SubmitPduBase.class}, new Object[]{destAddr, scAddr, text, pdu}), sentIntent, deliveryIntent, (String) ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SMSDispatcher", "getFormatPublic", new Class[0], new Object[0]), messageUri, Boolean.valueOf(expectMore), text, true, Boolean.valueOf(persistMessage), Integer.valueOf(priority), Integer.valueOf(validityPeriod), Boolean.valueOf(isForVvm)});
            if (!((Boolean) ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SMSDispatcher", "sendSmsByCarrierAppPublic", new Class[]{Boolean.TYPE, SMSDispatcher.SmsTracker.class}, new Object[]{false, tracker})).booleanValue()) {
                ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SMSDispatcher", "sendSubmitPduPublic", new Class[]{SMSDispatcher.SmsTracker.class}, new Object[]{tracker});
                return;
            }
            return;
        }
        OppoRlog.Rlog.e(TAG, "SmsDispatcher.sendTextOem(): getSubmitPdu() returned null");
        ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SMSDispatcher", "triggerSentIntentForFailurePublic", new Class[]{PendingIntent.class}, new Object[]{sentIntent});
    }

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase sendTextOemHookForEncoding(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        SMSDispatcher sMSDispatcher = this.mRef;
        boolean z = false;
        Class[] clsArr = {String.class, String.class, String.class, Boolean.TYPE, SmsHeader.class, Integer.TYPE, Integer.TYPE};
        Object[] objArr = new Object[7];
        objArr[0] = scAddr;
        objArr[1] = destAddr;
        objArr[2] = text;
        if (deliveryIntent != null) {
            z = true;
        }
        objArr[3] = Boolean.valueOf(z);
        objArr[4] = null;
        objArr[5] = Integer.valueOf(priority);
        objArr[6] = Integer.valueOf(validityPeriod);
        return (SmsMessageBase.SubmitPduBase) ReflectionHelper.callMethod(sMSDispatcher, "com.android.internal.telephony.SMSDispatcher", "getSubmitPduPublic", clsArr, objArr);
    }

    public void sendMultipartTextOem(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        OppoRlog.Rlog.d(TAG, "sendMultipartTextOem, encodingType=" + encodingType + " priority=" + priority + " expectMore=" + expectMore + " validityPeriod=" + validityPeriod);
        String fullMessageText = (String) ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SMSDispatcher", "getMultipartMessageTextPublic", new Class[]{new ArrayList().getClass()}, new Object[]{parts});
        int refNumber = ((Integer) ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SMSDispatcher", "getNextConcatenatedRefPublic", new Class[0], new Object[0])).intValue() & OppoRIL.MAX_MODEM_CRASH_CAUSE_LEN;
        int msgCount = parts.size();
        if (msgCount < 1) {
            ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SMSDispatcher", "triggerSentIntentForFailurePublic", new Class[]{new ArrayList().getClass()}, new Object[]{sentIntents});
            return;
        }
        GsmAlphabet.TextEncodingDetails[] encodingForParts = new GsmAlphabet.TextEncodingDetails[msgCount];
        String str = "triggerSentIntentForFailurePublic";
        int msgCount2 = msgCount;
        String str2 = "com.android.internal.telephony.SMSDispatcher";
        int refNumber2 = refNumber;
        int encoding = sendMultipartTextOemHookForEncoding(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingType, encodingForParts);
        SMSDispatcher.SmsTracker[] trackers = new SMSDispatcher.SmsTracker[msgCount2];
        AtomicInteger unsentPartCount = new AtomicInteger(msgCount2);
        AtomicBoolean anyPartFailed = new AtomicBoolean(false);
        int i = 0;
        while (i < msgCount2) {
            SmsHeader.ConcatRef concatRef = new SmsHeader.ConcatRef();
            concatRef.refNumber = refNumber2;
            concatRef.seqNumber = i + 1;
            concatRef.msgCount = msgCount2;
            concatRef.isEightBits = true;
            SmsHeader smsHeader = new SmsHeader();
            smsHeader.concatRef = concatRef;
            if (encoding == 1) {
                smsHeader.languageTable = encodingForParts[i].languageTable;
                smsHeader.languageShiftTable = encodingForParts[i].languageShiftTable;
            }
            PendingIntent sentIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                sentIntent = sentIntents.get(i);
            }
            PendingIntent deliveryIntent = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                deliveryIntent = deliveryIntents.get(i);
            }
            SMSDispatcher sMSDispatcher = this.mRef;
            Class[] clsArr = {String.class, String.class, String.class, String.class, SmsHeader.class, Integer.TYPE, PendingIntent.class, PendingIntent.class, Boolean.TYPE, AtomicInteger.class, AtomicBoolean.class, Uri.class, String.class, Integer.TYPE, Boolean.TYPE, Integer.TYPE};
            Object[] objArr = new Object[16];
            objArr[0] = callingPkg;
            objArr[1] = destAddr;
            objArr[2] = scAddr;
            objArr[3] = parts.get(i);
            objArr[4] = smsHeader;
            objArr[5] = Integer.valueOf(encoding);
            objArr[6] = sentIntent;
            objArr[7] = deliveryIntent;
            objArr[8] = Boolean.valueOf(i == msgCount2 + -1);
            objArr[9] = unsentPartCount;
            objArr[10] = anyPartFailed;
            objArr[11] = messageUri;
            objArr[12] = fullMessageText;
            objArr[13] = Integer.valueOf(priority);
            objArr[14] = Boolean.valueOf(expectMore);
            objArr[15] = Integer.valueOf(validityPeriod);
            trackers[i] = (SMSDispatcher.SmsTracker) ReflectionHelper.callMethod(sMSDispatcher, str2, "getNewSubmitPduTrackerPublic", clsArr, objArr);
            if (trackers[i] == null) {
                ReflectionHelper.callMethod(this.mRef, str2, str, new Class[]{new ArrayList().getClass()}, new Object[]{sentIntents});
                return;
            }
            ReflectionHelper.setDeclaredField(trackers[i], "com.android.internal.telephony.SMSDispatcher$SmsTracker", "mPersistMessage", Boolean.valueOf(persistMessage));
            i++;
            str2 = str2;
            refNumber2 = refNumber2;
            anyPartFailed = anyPartFailed;
            msgCount2 = msgCount2;
            encoding = encoding;
            unsentPartCount = unsentPartCount;
            str = str;
        }
        String carrierPackage = (String) ReflectionHelper.callMethod(this.mRef, str2, "getCarrierAppPackageNamePublic", new Class[0], new Object[0]);
        if (carrierPackage != null) {
            OppoRlog.Rlog.d(TAG, "Found carrier package.");
            Object smsSender = ReflectionHelper.callMethod(this.mRef, str2, "newMultipartSmsSender", new Class[]{new ArrayList().getClass(), new SMSDispatcher.SmsTracker[0].getClass()}, new Object[]{parts, trackers});
            if (smsSender == null) {
                OppoRlog.Rlog.e(TAG, "smsSender == null");
                return;
            }
            Object mRef_newMultipartSmsSenderCallback = ReflectionHelper.callMethod(this.mRef, str2, "newMultipartSmsSenderCallback", new Class[]{smsSender.getClass()}, new Object[]{smsSender});
            if (mRef_newMultipartSmsSenderCallback == null) {
                OppoRlog.Rlog.e(TAG, "mRef_newMultipartSmsSenderCallback == null");
            } else {
                ReflectionHelper.callMethod(smsSender, "com.android.internal.telephony.SMSDispatcher$MultipartSmsSender", "sendSmsByCarrierAppPublic", new Class[]{String.class, mRef_newMultipartSmsSenderCallback.getClass()}, new Object[]{carrierPackage, mRef_newMultipartSmsSenderCallback});
            }
        } else {
            OppoRlog.Rlog.v(TAG, "No carrier package.");
            for (SMSDispatcher.SmsTracker tracker : trackers) {
                if (tracker != null) {
                    ReflectionHelper.callMethod(this.mRef, str2, "sendSubmitPduPublic", new Class[]{SMSDispatcher.SmsTracker.class}, new Object[]{tracker});
                } else {
                    OppoRlog.Rlog.e(TAG, "Null tracker.");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int sendMultipartTextOemHookForEncoding(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> arrayList, ArrayList<PendingIntent> arrayList2, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType, GsmAlphabet.TextEncodingDetails[] encodingForParts) {
        int encoding = 0;
        int msgCount = parts.size();
        for (int i = 0; i < msgCount; i++) {
            GsmAlphabet.TextEncodingDetails details = (GsmAlphabet.TextEncodingDetails) ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SMSDispatcher", "calculateLengthPublic", new Class[]{CharSequence.class, Boolean.TYPE}, new Object[]{parts.get(i), false});
            if (encoding != details.codeUnitSize && (encoding == 0 || encoding == 1)) {
                encoding = details.codeUnitSize;
            }
            encodingForParts[i] = details;
        }
        return encoding;
    }

    public boolean handleSmsSendControl(SMSDispatcher.SmsTracker smsTracker, Phone phone, Context context, int event) {
        try {
            if (OppoPolicyController.isPoliceVersion(phone)) {
                boolean isPolicyMessageSendEnable = OppoPolicyController.isSmsSendEnable(phone);
                OppoRlog.Rlog.d(NetworkDiagnoseUtils.INFO_OTHER_SMS, "isPolicyMessageSendEnable=" + isPolicyMessageSendEnable);
                if (context != null && !isPolicyMessageSendEnable) {
                    smsTracker.onFailed(context, event, 0);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean oemMoImsErrorCode30(AsyncResult ar, SMSDispatcher.SmsTracker tracker) {
        if (tracker != null) {
            try {
                if (tracker.mImsRetry > 0 && tracker.mRetryCount == 0 && ar != null && ((ar.exception.getCommandError() == CommandException.Error.GENERIC_FAILURE || ar.exception.getCommandError() == CommandException.Error.MODEM_ERR) && ar.result != null && ((SmsResponse) ar.result).mErrorCode == 30)) {
                    OppoRlog.Rlog.d(TAG, "oppo ims retry, for ims error code 30...");
                    tracker.mRetryCount++;
                    Object mRef_get_EVENT_SEND_RETRY = ReflectionHelper.callMethod(this.mRef, "com.android.internal.telephony.SMSDispatcher", "get_EVENT_SEND_RETRY", new Class[0], new Object[0]);
                    if (mRef_get_EVENT_SEND_RETRY != null) {
                        this.mRef.sendMessageDelayed(this.mRef.obtainMessage(((Integer) mRef_get_EVENT_SEND_RETRY).intValue(), tracker), (long) ((Integer) mRef_get_EVENT_SEND_RETRY).intValue());
                    }
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public void oemMoSmsCount(SMSDispatcher.SmsTracker tracker) {
        try {
            String packageName = tracker.mAppInfo.packageName;
            OppoRlog.Rlog.d(TAG, "countSendSms," + packageName);
            if (this.mOppoUsageManager == null) {
                this.mOppoUsageManager = ReflectionHelper.callMethod((Object) null, "android.os.OppoUsageManager", "getOppoUsageManager", new Class[0], new Object[0]);
            }
            if (this.mOppoUsageManager == null) {
                OppoRlog.Rlog.d(TAG, "oemMoSmsCount, null");
            } else if ("com.android.mms".equals(packageName)) {
                OppoRlog.Rlog.d(TAG, "default--shouldCount");
                ReflectionHelper.callMethod(this.mOppoUsageManager, "android.os.OppoUsageManager", "accumulateHistoryCountOfReceivedMsg", new Class[]{Integer.TYPE}, new Object[]{1});
            } else {
                boolean shouldCount = true;
                String[] countSmsFilterPackages = {"com.android", "com.mediatek", "com.oppo", "com.qualcomm", "com.nxp", "com.nearme"};
                if (packageName != null) {
                    int length = countSmsFilterPackages.length;
                    int i = 0;
                    while (true) {
                        if (i < length) {
                            String smsPackage = countSmsFilterPackages[i];
                            if (smsPackage != null && packageName.startsWith(smsPackage)) {
                                shouldCount = false;
                                break;
                            }
                            i++;
                        } else {
                            break;
                        }
                    }
                }
                OppoRlog.Rlog.d(TAG, "shouldCount=" + shouldCount);
                if (shouldCount) {
                    OppoRlog.Rlog.d(TAG, "accumulate the count of the send sms");
                    if (this.mOppoUsageManager != null) {
                        ReflectionHelper.callMethod(this.mOppoUsageManager, "android.os.OppoUsageManager", "accumulateHistoryCountOfReceivedMsg", new Class[]{Integer.TYPE}, new Object[]{1});
                    }
                }
            }
        } catch (Exception e) {
            OppoRlog.Rlog.e(TAG, "countSendSms--exception");
        }
    }
}

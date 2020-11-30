package com.oppo.internal.telephony.gsm;

import android.app.PendingIntent;
import android.net.Uri;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import com.android.internal.telephony.gsm.SmsMessage;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.OppoSMSDispatcherReference;
import java.util.ArrayList;

public class OppoGsmSMSDispatcherReference extends OppoSMSDispatcherReference {
    private static final String TAG = "OppoGsmSMSDispatcherReference";
    private GsmSMSDispatcher mRefGsm = null;

    public OppoGsmSMSDispatcherReference(GsmSMSDispatcher ref) {
        super(ref);
        this.mRefGsm = ref;
        OppoRlog.Rlog.d(TAG, "mRefGsm=" + this.mRefGsm);
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.OppoSMSDispatcherReference
    public int sendMultipartTextOemHookForEncoding(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType, GsmAlphabet.TextEncodingDetails[] encodingForParts) {
        OppoRlog.Rlog.d(TAG, "sendMultipartTextOemHookForEncoding,encodingType=" + encodingType);
        if (encodingType == 0) {
            int encoding = super.sendMultipartTextOemHookForEncoding(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingType, encodingForParts);
            OppoRlog.Rlog.d(TAG, "gsm-onSendMultipartText encoding = " + encoding);
            return encoding;
        }
        int msgCount = parts.size();
        OppoRlog.Rlog.d(TAG, "msgCount=" + msgCount);
        for (int i = 0; i < msgCount; i++) {
            GsmAlphabet.TextEncodingDetails details = null;
            Object obj = ReflectionHelper.callMethod((Object) null, "com.android.internal.telephony.gsm.OppoSmsMessage", "calculateLengthOem", new Class[]{CharSequence.class, Boolean.TYPE, Integer.TYPE}, new Object[]{parts.get(i), false, Integer.valueOf(encodingType)});
            if (obj != null) {
                details = (GsmAlphabet.TextEncodingDetails) obj;
            }
            if (!(details == null || encodingType == details.codeUnitSize || !(encodingType == 0 || encodingType == 1))) {
                OppoRlog.Rlog.d(TAG, "[gsm enc conflict between details[" + details.codeUnitSize + "] and encoding " + encodingType);
                details.codeUnitSize = encodingType;
            }
            encodingForParts[i] = details;
        }
        return encodingType;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.OppoSMSDispatcherReference
    public SmsMessageBase.SubmitPduBase sendTextOemHookForEncoding(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        OppoRlog.Rlog.d(TAG, "sendTextOemHookForEncoding,encodingType=" + encodingType);
        if (encodingType == 0) {
            return super.sendTextOemHookForEncoding(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingType);
        }
        boolean z = false;
        Object obj = ReflectionHelper.callMethod((Object) null, "com.android.internal.telephony.gsm.OppoSmsMessage", "calculateLengthOem", new Class[]{CharSequence.class, Boolean.TYPE, Integer.TYPE}, new Object[]{text, false, Integer.valueOf(encodingType)});
        GsmAlphabet.TextEncodingDetails details = obj != null ? (GsmAlphabet.TextEncodingDetails) obj : null;
        if (details == null) {
            return null;
        }
        if (deliveryIntent != null) {
            z = true;
        }
        return SmsMessage.getSubmitPdu(scAddr, destAddr, text, z, (byte[]) null, encodingType, details.languageTable, details.languageShiftTable, validityPeriod);
    }
}

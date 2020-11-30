package com.oppo.internal.telephony;

import android.app.PendingIntent;
import android.net.Uri;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.ImsSmsDispatcher;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.gsm.SmsMessage;
import com.android.internal.telephony.util.ReflectionHelper;
import java.util.ArrayList;

public class OppoImsSmsDispatcherReference extends OppoSMSDispatcherReference {
    private static final String TAG = "OppoImsSmsDispatcherReference";
    private ImsSmsDispatcher mRefIms;

    public OppoImsSmsDispatcherReference(ImsSmsDispatcher ref) {
        super(ref);
        this.mRefIms = ref;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.OppoSMSDispatcherReference
    public int sendMultipartTextOemHookForEncoding(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType, GsmAlphabet.TextEncodingDetails[] encodingForParts) {
        OppoRlog.Rlog.d(TAG, "sendMultipartTextOemHookForEncoding, encodingType=" + encodingType);
        Object obj = ReflectionHelper.callMethod(this.mRefIms, "com.android.internal.telephony.SMSDispatcher", "isCdmaMoPublic", new Class[0], new Object[0]);
        boolean isCdma = obj != null && ((Boolean) obj).booleanValue();
        OppoRlog.Rlog.d(TAG, "ims-mult-isCdmaMo=" + isCdma);
        if (!isCdma) {
            if (encodingType != 0) {
                int msgCount = parts.size();
                for (int i = 0; i < msgCount; i++) {
                    GsmAlphabet.TextEncodingDetails details = null;
                    Object obj2 = ReflectionHelper.callMethod((Object) null, "com.android.internal.telephony.gsm.OppoSmsMessage", "calculateLengthOem", new Class[]{CharSequence.class, Boolean.TYPE, Integer.TYPE}, new Object[]{parts.get(i), false, Integer.valueOf(encodingType)});
                    if (obj2 != null) {
                        details = (GsmAlphabet.TextEncodingDetails) obj2;
                    }
                    if (!(details == null || encodingType == details.codeUnitSize || !(encodingType == 0 || encodingType == 1))) {
                        OppoRlog.Rlog.d(TAG, "[ims enc conflict between details[" + details.codeUnitSize + "] and encoding " + encodingType);
                        details.codeUnitSize = encodingType;
                    }
                    encodingForParts[i] = details;
                }
                return encodingType;
            }
        }
        int encoding = super.sendMultipartTextOemHookForEncoding(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingType, encodingForParts);
        OppoRlog.Rlog.d(TAG, "ims-onSendMultipartText encoding = " + encoding);
        return encoding;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.OppoSMSDispatcherReference
    public SmsMessageBase.SubmitPduBase sendTextOemHookForEncoding(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        OppoRlog.Rlog.d(TAG, "sendTextOemHookForEncoding,encodingType=" + encodingType);
        Object obj = ReflectionHelper.callMethod(this.mRefIms, "com.android.internal.telephony.SMSDispatcher", "isCdmaMoPublic", new Class[0], new Object[0]);
        boolean z = true;
        boolean isCdma = obj != null && ((Boolean) obj).booleanValue();
        OppoRlog.Rlog.d(TAG, "ims-tex-isCdmaMo=" + isCdma);
        if (isCdma || encodingType == 0) {
            return super.sendTextOemHookForEncoding(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingType);
        }
        Object obj2 = ReflectionHelper.callMethod((Object) null, "com.android.internal.telephony.gsm.OppoSmsMessage", "calculateLengthOem", new Class[]{CharSequence.class, Boolean.TYPE, Integer.TYPE}, new Object[]{text, false, Integer.valueOf(encodingType)});
        GsmAlphabet.TextEncodingDetails details = obj2 != null ? (GsmAlphabet.TextEncodingDetails) obj2 : null;
        if (details == null) {
            return null;
        }
        if (deliveryIntent == null) {
            z = false;
        }
        return SmsMessage.getSubmitPdu(scAddr, destAddr, text, z, (byte[]) null, encodingType, details.languageTable, details.languageShiftTable, validityPeriod);
    }
}

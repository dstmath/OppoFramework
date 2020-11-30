package com.oppo.internal.telephony.cdma;

import android.app.PendingIntent;
import android.net.Uri;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.OppoSMSDispatcherReference;
import java.util.ArrayList;

public class OppoCdmaSMSDispatcherReference extends OppoSMSDispatcherReference {
    private static final String TAG = "OppoCdmaSMSDispatcherReference";
    private CdmaSMSDispatcher mRefCdma = null;

    public OppoCdmaSMSDispatcherReference(CdmaSMSDispatcher ref) {
        super(ref);
        this.mRefCdma = ref;
        OppoRlog.Rlog.d(TAG, "mRefCdma=" + this.mRefCdma);
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.OppoSMSDispatcherReference
    public int sendMultipartTextOemHookForEncoding(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType, GsmAlphabet.TextEncodingDetails[] encodingForParts) {
        OppoRlog.Rlog.d(TAG, "sendMultipartTextOemHookForEncoding,encodingType=" + encodingType);
        if (encodingType == 1 || encodingType == 2 || encodingType == 3) {
            int msgCount = parts.size();
            for (int i = 0; i < msgCount; i++) {
                GsmAlphabet.TextEncodingDetails details = null;
                Object obj = ReflectionHelper.callMethod((Object) null, "com.android.internal.telephony.cdma.OppoSmsMessage", "calculateLengthOem", new Class[]{CharSequence.class, Boolean.TYPE, Boolean.TYPE, Integer.TYPE}, new Object[]{parts.get(i), false, false, Integer.valueOf(encodingType)});
                if (obj != null) {
                    details = (GsmAlphabet.TextEncodingDetails) obj;
                }
                if (details != null) {
                    details.codeUnitSize = encodingType;
                    encodingForParts[i] = details;
                }
            }
            return encodingType;
        }
        int encoding = super.sendMultipartTextOemHookForEncoding(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingType, encodingForParts);
        OppoRlog.Rlog.d(TAG, "cdma-onSendMultipartText encoding = " + encoding);
        return encoding;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.OppoSMSDispatcherReference
    public SmsMessageBase.SubmitPduBase sendTextOemHookForEncoding(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        OppoRlog.Rlog.d(TAG, "sendTextOemHookForEncoding,encodingType=" + encodingType);
        if (encodingType != 1 && encodingType != 2 && encodingType != 3) {
            return super.sendTextOemHookForEncoding(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingType);
        }
        boolean z = false;
        Class[] clsArr = {String.class, String.class, String.class, Boolean.TYPE, SmsHeader.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE};
        Object[] objArr = new Object[9];
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
        objArr[7] = Integer.valueOf(encodingType);
        objArr[8] = true;
        return (SmsMessageBase.SubmitPduBase) ReflectionHelper.callMethod((Object) null, "com.android.internal.telephony.cdma.OppoSmsMessage", "getSubmitPduOem", clsArr, objArr);
    }
}

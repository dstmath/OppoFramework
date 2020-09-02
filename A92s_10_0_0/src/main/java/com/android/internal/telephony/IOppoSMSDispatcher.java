package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncResult;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import java.util.ArrayList;

public interface IOppoSMSDispatcher extends IOppoCommonFeature {
    public static final IOppoSMSDispatcher DEFAULT = new IOppoSMSDispatcher() {
        /* class com.android.internal.telephony.IOppoSMSDispatcher.AnonymousClass1 */
    };
    public static final String TAG = "IOppoSMSDispatcher";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoSMSDispatcher;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoSMSDispatcher getDefault() {
        return DEFAULT;
    }

    default void sendTextOem(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, boolean isForVvm, int encodingType) {
    }

    default void sendMultipartTextOem(String destAddr, String scAddr, ArrayList<String> arrayList, ArrayList<PendingIntent> arrayList2, ArrayList<PendingIntent> arrayList3, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType) {
    }

    default boolean handleSmsSendControl(SMSDispatcher.SmsTracker smsTracker, Phone phone, Context context, int event) {
        return false;
    }

    default boolean oemMoImsErrorCode30(AsyncResult ar, SMSDispatcher.SmsTracker tracker) {
        return false;
    }

    default void oemMoSmsCount(SMSDispatcher.SmsTracker tracker) {
    }
}

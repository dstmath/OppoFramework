package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.content.Intent;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;

public interface IOppoImsPhone extends IOppoCommonFeature {
    public static final IOppoImsPhone DEFAULT = new IOppoImsPhone() {
        /* class com.android.internal.telephony.imsphone.IOppoImsPhone.AnonymousClass1 */
    };
    public static final String TAG = "IOppoImsPhone";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoImsPhone;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoImsPhone getDefault() {
        return DEFAULT;
    }

    default boolean handle1xInCallMmiCode(String dialString, ImsPhoneCall call) {
        return false;
    }

    default int setCallForwardingTimer(Phone phone, int commandInterfaceCFReason, int timerSeconds) {
        return 0;
    }

    default int setCallForwardingServiceClass(int commandInterfaceServiceClass) {
        return 0;
    }

    default void dispose() {
    }

    default void handleCarrerConfigChanged(int subId, Intent intent) {
    }

    default void handleInCallMmiForSpecificOp(ImsPhone mPhone, ImsPhoneMmiCode imsPhoneMmiCode, MmiCode.State mState, CharSequence mMessage, String mccMnc, String mDialingNumber, boolean isUssiEnabled, Context mContext) throws CallStateException {
    }
}

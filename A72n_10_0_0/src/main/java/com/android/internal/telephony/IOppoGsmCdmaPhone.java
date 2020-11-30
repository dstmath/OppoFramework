package com.android.internal.telephony;

import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import java.util.concurrent.atomic.AtomicReference;

public interface IOppoGsmCdmaPhone extends IOppoCommonFeature {
    public static final IOppoGsmCdmaPhone DEFAULT = new IOppoGsmCdmaPhone() {
        /* class com.android.internal.telephony.IOppoGsmCdmaPhone.AnonymousClass1 */
    };

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoGsmCdmaPhone;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoGsmCdmaPhone getDefault() {
        return DEFAULT;
    }

    default boolean handleCalloutControl(boolean isEmergencyNumber) {
        return false;
    }

    default boolean handleImsForUtCheck(boolean useImsForUt, String dialString, AtomicReference<UiccCardApplication> atomicReference) {
        return false;
    }

    default boolean isOemInCall() {
        return false;
    }

    default boolean isManualSelectNetworksAllowed(ServiceStateTracker mSST) {
        return false;
    }

    default String oemGetFullIccSerialNumber(IccRecords mIccRecords, UiccController mUiccController) {
        return PhoneConfigurationManager.SSSS;
    }

    default String colorGetIccCardType() {
        return PhoneConfigurationManager.SSSS;
    }

    default void resetImsSS(Phone imsPhone) {
    }

    default boolean isUssdEnabledInVolteCall() {
        return false;
    }

    default boolean isInImsCall() {
        return false;
    }

    default String getDefaultVMByImsi(String imsi) {
        return PhoneConfigurationManager.SSSS;
    }

    default void updateLteWifiCoexist(boolean enabled) {
    }

    default void getLteWifiCoexistStatus() {
    }
}

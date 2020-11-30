package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import java.util.Map;

public interface IOppoSubscriptionController extends IOppoCommonFeature {
    public static final IOppoSubscriptionController DEFAULT = new IOppoSubscriptionController() {
        /* class com.android.internal.telephony.IOppoSubscriptionController.AnonymousClass1 */
    };
    public static final String TAG = "IOppoSubscriptionController";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoSubscriptionController;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoSubscriptionController getDefault() {
        return DEFAULT;
    }

    default boolean isCTCCard(int slotId) {
        return false;
    }

    default boolean isHasSoftSimCard() {
        return false;
    }

    default int getSoftSimCardSlotId() {
        return -1;
    }

    default void activateSubId(int subId) {
    }

    default void deactivateSubId(int subId) {
    }

    default int getSubState(int subId) {
        return 0;
    }

    default boolean isUsimWithCsim(int slotId) {
        return false;
    }

    default String getCarrierName(Context context, String name, String imsi, String iccid, int slotid) {
        return PhoneConfigurationManager.SSSS;
    }

    default String getOemOperator(Context context, String plmn) {
        return PhoneConfigurationManager.SSSS;
    }

    default String getExportSimDefaultName(int slotId) {
        return "SIM1";
    }

    default String getOperatorNumericForData(int phoneId) {
        return PhoneConfigurationManager.SSSS;
    }

    default int setDisplayNumber(String number, int subId, boolean writeToSim) {
        return 0;
    }

    default void broadcastSubInfoUpdateIntent(String slotid, String subid, String simstate) {
    }

    default boolean informNewSimCardLoaded(int slotIndex) {
        return false;
    }

    default void updateMapValue(Map<Integer, Integer> map, int slotIndex, int value) {
    }

    default boolean isSoftSimCardSubId(int subId) {
        return false;
    }
}

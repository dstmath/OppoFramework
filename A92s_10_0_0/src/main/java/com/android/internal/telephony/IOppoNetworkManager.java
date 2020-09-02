package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;

public interface IOppoNetworkManager extends IOppoCommonFeature {
    public static final IOppoNetworkManager DEFAULT = new IOppoNetworkManager() {
        /* class com.android.internal.telephony.IOppoNetworkManager.AnonymousClass1 */
    };
    public static final String TAG = "IOppoNetworkManager";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoNetworkManager;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoNetworkManager getDefault() {
        return DEFAULT;
    }

    default void oppoResetOosDelayState(Phone phone) {
    }

    default boolean isMvnoPlmn(String plmn) {
        return false;
    }

    default int calculatePreferredNetworkTypeWithPhoneId(Context context, int phoneSubId, int phoneid) {
        return 0;
    }

    default void oppoProcessUnsolOemKeyLogErrMsg(Context context, int phoneId, Object ret) {
    }

    default void oppoCountUnsolMsg(int response) {
    }

    default void oppoCountGetCellInfo(int getCellUid, int getCellPid, String getCellPackage) {
    }
}

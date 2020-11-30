package com.android.internal.telephony;

import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;

public interface IOppoIccPhoneBookInterfaceManager extends IOppoCommonFeature {
    public static final IOppoIccPhoneBookInterfaceManager DEFAULT = new IOppoIccPhoneBookInterfaceManager() {
        /* class com.android.internal.telephony.IOppoIccPhoneBookInterfaceManager.AnonymousClass1 */
    };
    public static final String TAG = "IOppoIccPhoneBookInterfaceManager";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoIccPhoneBookInterfaceManager;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoIccPhoneBookInterfaceManager getDefault() {
        return DEFAULT;
    }

    default int oppoGetAdnEmailLen() {
        return -1;
    }

    default int oppoGetSimPhonebookAllSpace() {
        return -1;
    }

    default int oppoGetSimPhonebookUsedSpace() {
        return -1;
    }

    default int oppoGetSimPhonebookNameLength() {
        return -1;
    }

    default boolean isPhoneBookReady() {
        return false;
    }

    default void broadcastIccPhoneBookReadyIntent(String value, String reason) {
    }

    default void resetSimNameLength() {
    }
}

package com.android.internal.telephony;

import android.app.PendingIntent;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import java.util.List;

public interface IOppoIccSmsInterfaceManager extends IOppoCommonFeature {
    public static final IOppoIccSmsInterfaceManager DEFAULT = new IOppoIccSmsInterfaceManager() {
        /* class com.android.internal.telephony.IOppoIccSmsInterfaceManager.AnonymousClass1 */
    };
    public static final String TAG = "IOppoIccSmsInterfaceManager";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoIccSmsInterfaceManager;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoIccSmsInterfaceManager getDefault() {
        return DEFAULT;
    }

    default void sendTextWithOptionsOem(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int encodingType) {
    }

    default void sendMultipartTextWithOptionsOem(String callingPackage, String destAddr, String scAddr, List<String> list, List<PendingIntent> list2, List<PendingIntent> list3, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int encodingType) {
    }

    default boolean oemSetCellBroadcastActivation(boolean activate, int what) {
        return false;
    }
}

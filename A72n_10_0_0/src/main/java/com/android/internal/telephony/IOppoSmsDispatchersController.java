package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import java.util.ArrayList;

public interface IOppoSmsDispatchersController extends IOppoCommonFeature {
    public static final IOppoSmsDispatchersController DEFAULT = new IOppoSmsDispatchersController() {
        /* class com.android.internal.telephony.IOppoSmsDispatchersController.AnonymousClass1 */
    };
    public static final String TAG = "IOppoSmsDispatchersController";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoSmsDispatchersController;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoSmsDispatchersController getDefault() {
        return DEFAULT;
    }

    default void sendTextOem(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, boolean isForVvm, int encodingType) {
    }

    default void sendMultipartTextOem(String destAddr, String scAddr, ArrayList<String> arrayList, ArrayList<PendingIntent> arrayList2, ArrayList<PendingIntent> arrayList3, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType) {
    }

    default void oemSendData(Context context, String callingPackage) {
    }
}

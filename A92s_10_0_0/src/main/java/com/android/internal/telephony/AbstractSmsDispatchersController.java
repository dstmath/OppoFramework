package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import com.android.internal.telephony.OppoRlog;
import java.util.ArrayList;

public abstract class AbstractSmsDispatchersController extends Handler {
    private static final String LOG_TAG = "AbstractSmsDispatchersController";
    IOppoSmsDispatchersController mReference;

    /* access modifiers changed from: protected */
    public abstract void sendMultipartText(String str, String str2, ArrayList<String> arrayList, ArrayList<PendingIntent> arrayList2, ArrayList<PendingIntent> arrayList3, Uri uri, String str3, boolean z, int i, boolean z2, int i2);

    /* access modifiers changed from: protected */
    public abstract void sendText(String str, String str2, String str3, PendingIntent pendingIntent, PendingIntent pendingIntent2, Uri uri, String str4, boolean z, int i, boolean z2, int i2, boolean z3);

    public AbstractSmsDispatchersController() {
        this.mReference = null;
        this.mReference = (IOppoSmsDispatchersController) OppoTelephonyFactory.getInstance().getFeature(IOppoSmsDispatchersController.DEFAULT, this);
        OppoRlog.Rlog.d(LOG_TAG, "mReference=" + this.mReference);
    }

    public void sendTextOem(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, boolean isForVvm, int encodingType) {
        IOppoSmsDispatchersController iOppoSmsDispatchersController = this.mReference;
        if (iOppoSmsDispatchersController != null) {
            iOppoSmsDispatchersController.sendTextOem(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, isForVvm, encodingType);
            return;
        }
        OppoRlog.Rlog.e(LOG_TAG, "sendTextOem--error");
        sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, isForVvm);
    }

    public void sendMultipartTextOem(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        IOppoSmsDispatchersController iOppoSmsDispatchersController = this.mReference;
        if (iOppoSmsDispatchersController != null) {
            iOppoSmsDispatchersController.sendMultipartTextOem(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, encodingType);
            return;
        }
        OppoRlog.Rlog.e(LOG_TAG, "sendMultipartTextOem--error");
        sendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
    }

    public void oemSendData(Context context, String callingPackage) {
        IOppoSmsDispatchersController iOppoSmsDispatchersController = this.mReference;
        if (iOppoSmsDispatchersController != null) {
            iOppoSmsDispatchersController.oemSendData(context, callingPackage);
        } else {
            OppoRlog.Rlog.e(LOG_TAG, "oemSendData--disable");
        }
    }
}

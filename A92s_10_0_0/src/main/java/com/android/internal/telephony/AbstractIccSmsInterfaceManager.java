package com.android.internal.telephony;

import android.app.PendingIntent;
import com.android.internal.telephony.OppoRlog;
import java.util.List;

public abstract class AbstractIccSmsInterfaceManager {
    private String LOG_TAG = "AbstractIccSmsInterfaceManager";
    private IOppoIccSmsInterfaceManager mReference = null;

    public abstract void sendMultipartTextWithOptions(String str, String str2, String str3, List<String> list, List<PendingIntent> list2, List<PendingIntent> list3, boolean z, int i, boolean z2, int i2);

    public abstract void sendTextWithOptions(String str, String str2, String str3, String str4, PendingIntent pendingIntent, PendingIntent pendingIntent2, boolean z, int i, boolean z2, int i2);

    public AbstractIccSmsInterfaceManager(Phone phone) {
        if (phone != null) {
            this.LOG_TAG = "AbstractIccSmsInterfaceManager[" + phone.getPhoneId() + "]";
        }
        this.mReference = (IOppoIccSmsInterfaceManager) OppoTelephonyFactory.getInstance().getFeature(IOppoIccSmsInterfaceManager.DEFAULT, this);
        String str = this.LOG_TAG;
        OppoRlog.Rlog.d(str, "mReference=" + this.mReference);
    }

    public void sendTextWithOptionsOem(String callingPackage, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        IOppoIccSmsInterfaceManager iOppoIccSmsInterfaceManager = this.mReference;
        if (iOppoIccSmsInterfaceManager != null) {
            iOppoIccSmsInterfaceManager.sendTextWithOptionsOem(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod, encodingType);
            return;
        }
        OppoRlog.Rlog.e(this.LOG_TAG, "sendTextWithOptionsOem--disable");
        sendTextWithOptions(callingPackage, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
    }

    public void sendMultipartTextWithOptionsOem(String callingPackage, String destAddr, String scAddr, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int encodingType) {
        IOppoIccSmsInterfaceManager iOppoIccSmsInterfaceManager = this.mReference;
        if (iOppoIccSmsInterfaceManager != null) {
            iOppoIccSmsInterfaceManager.sendMultipartTextWithOptionsOem(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod, encodingType);
            return;
        }
        OppoRlog.Rlog.e(this.LOG_TAG, "sendMultipartTextWithOptionsOem--disable");
        sendMultipartTextWithOptions(callingPackage, destAddr, scAddr, parts, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
    }

    /* access modifiers changed from: protected */
    public boolean oemSetCellBroadcastActivation(boolean activate, int what) {
        IOppoIccSmsInterfaceManager iOppoIccSmsInterfaceManager = this.mReference;
        if (iOppoIccSmsInterfaceManager == null) {
            return false;
        }
        iOppoIccSmsInterfaceManager.oemSetCellBroadcastActivation(activate, what);
        return true;
    }
}

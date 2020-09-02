package com.android.internal.telephony.cdma;

import android.annotation.UnsupportedAppUsage;
import android.telephony.Rlog;

public class CdmaCallWaitingNotification {
    static final String LOG_TAG = "CdmaCallWaitingNotification";
    public int alertPitch = 0;
    public int isPresent = 0;
    public String name = null;
    public int namePresentation = 0;
    @UnsupportedAppUsage
    public String number = null;
    public int numberPlan = 0;
    public int numberPresentation = 0;
    public int numberType = 0;
    public int signal = 0;
    public int signalType = 0;

    public String toString() {
        return super.toString() + "Call Waiting Notification   number: " + this.number + " numberPresentation: " + this.numberPresentation + " name: " + this.name + " namePresentation: " + this.namePresentation + " numberType: " + this.numberType + " numberPlan: " + this.numberPlan + " isPresent: " + this.isPresent + " signalType: " + this.signalType + " alertPitch: " + this.alertPitch + " signal: " + this.signal;
    }

    public static int presentationFromCLIP(int cli) {
        if (cli == 0) {
            return 1;
        }
        if (cli == 1) {
            return 2;
        }
        if (cli == 2) {
            return 3;
        }
        Rlog.d(LOG_TAG, "Unexpected presentation " + cli);
        return 3;
    }
}

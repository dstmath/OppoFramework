package com.mediatek.internal.telephony.gsm;

import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.mediatek.internal.telephony.datasub.DataSubConstants;

public class MtkSuppCrssNotification {
    public static final int CRSS_CALLED_LINE_ID_PREST = 1;
    public static final int CRSS_CALLING_LINE_ID_PREST = 2;
    public static final int CRSS_CALL_WAITING = 0;
    public static final int CRSS_CONNECTED_LINE_ID_PREST = 3;
    private static final boolean SDBG = (!SystemProperties.get("ro.build.type").equals(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER));
    public String alphaid;
    public int cli_validity;
    public int code;
    public String number;
    public int type;

    public String toString() {
        return super.toString() + " CRSS Notification: code: " + this.code + " \"" + Rlog.pii(SDBG, PhoneNumberUtils.stringFromStringAndTOA(this.number, this.type)) + "\" " + this.alphaid + " cli_validity: " + this.cli_validity;
    }
}

package com.android.internal.telephony.cdma.sms;

import android.telephony.Rlog;
import com.android.internal.telephony.GsmAlphabet;

public final class OppoBearerData {
    private static final String LOG_TAG = "OppoBearerData";

    public static GsmAlphabet.TextEncodingDetails calcTextEncodingDetailsOem(CharSequence msg, boolean force7BitEncoding, boolean isEntireMsg, int encodingType) {
        int septets = BearerData.countAsciiSeptetsPublic(msg, force7BitEncoding);
        if (encodingType == 3) {
            Rlog.d(LOG_TAG, "16bit in cdma");
            septets = -1;
        }
        if (septets == -1 || septets > 160) {
            Rlog.d(LOG_TAG, "gsm can understand the control character, but cdma ignore it(<0x20)");
            GsmAlphabet.TextEncodingDetails ted = BearerData.calcTextEncodingDetails(msg, force7BitEncoding, true);
            if (ted.msgCount != 1 || ted.codeUnitSize != 1) {
                return ted;
            }
            ted.codeUnitCount = msg.length();
            int octets = ted.codeUnitCount * 2;
            if (octets > 140) {
                ted.msgCount = (octets + 133) / 134;
                ted.codeUnitsRemaining = ((ted.msgCount * 134) - octets) / 2;
            } else {
                ted.msgCount = 1;
                ted.codeUnitsRemaining = (140 - octets) / 2;
            }
            ted.codeUnitSize = 3;
            return ted;
        }
        GsmAlphabet.TextEncodingDetails ted2 = new GsmAlphabet.TextEncodingDetails();
        ted2.msgCount = 1;
        ted2.codeUnitCount = septets;
        ted2.codeUnitsRemaining = 160 - septets;
        ted2.codeUnitSize = 1;
        return ted2;
    }
}

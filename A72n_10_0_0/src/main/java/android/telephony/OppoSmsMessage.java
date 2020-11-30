package android.telephony;

import android.content.res.Resources;
import android.text.TextUtils;
import com.android.internal.R;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.cdma.SmsMessage;
import java.util.ArrayList;

public abstract class OppoSmsMessage implements IOppoSmsMessageEx {
    private static final String LOG_TAG = "OppoSmsMessage";

    @Override // android.telephony.IOppoSmsMessageEx
    public abstract int getEncodingType();

    @Override // android.telephony.IOppoSmsMessageEx
    public abstract String getRecipientAddress();

    public static ArrayList<String> oemFragmentText(String text, int subid) {
        return SmsMessage.fragmentText(text, subid);
    }

    public static ArrayList<String> oemFragmentText(String text, int subid, int encodingType) {
        GsmAlphabet.TextEncodingDetails ted;
        int udhLength;
        int nextPos;
        int udhLength2;
        boolean isCdma = SmsMessage.useCdmaFormatForMoSmsPublic(subid);
        if (isCdma) {
            ted = SmsMessage.calculateLengthOem(text, false, true, encodingType);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLengthOem(text, false, encodingType);
        }
        if (ted != null) {
            Rlog.d("sms", "ted.codeUnitSize=" + ted.codeUnitSize + " isCdma=" + isCdma + " subid=" + subid);
        }
        if (ted.codeUnitSize == 1) {
            if (ted.languageTable != 0 && ted.languageShiftTable != 0) {
                udhLength2 = 7;
            } else if (ted.languageTable == 0 && ted.languageShiftTable == 0) {
                udhLength2 = 0;
            } else {
                udhLength2 = 4;
            }
            if (ted.msgCount > 1) {
                udhLength2 += 6;
            }
            if (udhLength2 != 0) {
                udhLength2++;
            }
            udhLength = 160 - udhLength2;
        } else if (ted.msgCount > 1) {
            udhLength = 134;
            if (!SmsMessage.hasEmsSupport() && ted.msgCount < 10) {
                udhLength = 134 - 2;
            }
        } else {
            udhLength = 140;
        }
        String newMsgBody = null;
        if (Resources.getSystem().getBoolean(R.bool.config_sms_force_7bit_encoding)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(text, isCdma);
        }
        if (TextUtils.isEmpty(newMsgBody)) {
            newMsgBody = text;
        }
        int pos = 0;
        int textLen = newMsgBody.length();
        ArrayList<String> result = new ArrayList<>(ted.msgCount);
        while (true) {
            if (pos >= textLen) {
                break;
            }
            if (ted.codeUnitSize != 1) {
                nextPos = SmsMessageBase.findNextUnicodePosition(pos, udhLength, newMsgBody);
            } else if (!isCdma || ted.msgCount != 1) {
                nextPos = GsmAlphabet.findGsmSeptetLimitIndex(newMsgBody, pos, udhLength, ted.languageTable, ted.languageShiftTable);
            } else {
                nextPos = Math.min(udhLength, textLen - pos) + pos;
            }
            if (nextPos <= pos || nextPos > textLen) {
                Rlog.e(LOG_TAG, "fragmentText failed (" + pos + " >= " + nextPos + " or " + nextPos + " >= " + textLen + ")");
            } else {
                result.add(newMsgBody.substring(pos, nextPos));
                pos = nextPos;
            }
        }
        Rlog.e(LOG_TAG, "fragmentText failed (" + pos + " >= " + nextPos + " or " + nextPos + " >= " + textLen + ")");
        return result;
    }

    public static int[] calculateLengthOem(String messageBody, boolean use7bitOnly, int encodingType) {
        return calculateLengthOem((CharSequence) messageBody, use7bitOnly, encodingType);
    }

    public static int[] calculateLengthOem(String messageBody, boolean use7bitOnly, int subId, int encodingType) {
        return calculateLengthOem((CharSequence) messageBody, use7bitOnly, subId, encodingType);
    }

    public static int[] calculateLengthOem(CharSequence msgBody, boolean use7bitOnly, int encodingType) {
        GsmAlphabet.TextEncodingDetails ted;
        if (encodingType != 1 && encodingType != 3) {
            return SmsMessage.calculateLength(msgBody, use7bitOnly);
        }
        if (SmsMessage.useCdmaFormatForMoSmsPublic()) {
            ted = SmsMessage.calculateLengthOem(msgBody, use7bitOnly, true, encodingType);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLengthOem(msgBody, use7bitOnly, encodingType);
        }
        return new int[]{ted.msgCount, ted.codeUnitCount, ted.codeUnitsRemaining, ted.codeUnitSize};
    }

    public static int[] calculateLengthOem(CharSequence msgBody, boolean use7bitOnly, int subId, int encodingType) {
        GsmAlphabet.TextEncodingDetails ted;
        if (encodingType != 1 && encodingType != 3) {
            return SmsMessage.calculateLength(msgBody, use7bitOnly, subId);
        }
        if (SmsMessage.useCdmaFormatForMoSmsPublic(subId)) {
            ted = SmsMessage.calculateLengthOem(msgBody, use7bitOnly, true, encodingType);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLengthOem(msgBody, use7bitOnly, encodingType);
        }
        return new int[]{ted.msgCount, ted.codeUnitCount, ted.codeUnitsRemaining, ted.codeUnitSize};
    }

    @Override // android.telephony.IOppoSmsMessageEx
    public String getDestinationAddress() {
        return getRecipientAddress();
    }
}

package mediatek.telephony;

import android.content.res.Resources;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import java.util.ArrayList;

public class MtkSmsMessage extends SmsMessage {
    private static final String LOG_TAG = "MtkSmsMessage";
    public static final int MWI_EMAIL = 2;
    public static final int MWI_FAX = 1;
    public static final int MWI_OTHER = 3;
    public static final int MWI_VIDEO = 7;
    public static final int MWI_VOICEMAIL = 0;
    private String mFormat;

    private MtkSmsMessage(SmsMessageBase smb) {
        super(smb);
    }

    @Deprecated
    public static MtkSmsMessage createFromPdu(byte[] pdu) {
        String format;
        int activePhone = TelephonyManager.getDefault().getCurrentPhoneType();
        String format2 = "3gpp2";
        if (2 == activePhone) {
            format = format2;
        } else {
            format = "3gpp";
        }
        MtkSmsMessage message = createFromPdu(pdu, format);
        if (message != null && message.mWrappedSmsMessage != null) {
            return message;
        }
        if (2 == activePhone) {
            format2 = "3gpp";
        }
        return createFromPdu(pdu, format2);
    }

    public static MtkSmsMessage createFromPdu(byte[] pdu, String format) {
        SmsMessageBase wrappedMessage;
        if ("3gpp2".equals(format)) {
            wrappedMessage = com.mediatek.internal.telephony.cdma.MtkSmsMessage.newMtkSmsMessage(com.android.internal.telephony.cdma.SmsMessage.createFromPdu(pdu));
        } else if ("3gpp".equals(format)) {
            wrappedMessage = com.mediatek.internal.telephony.gsm.MtkSmsMessage.createFromPdu(pdu);
        } else {
            Rlog.e(LOG_TAG, "createFromPdu(): unsupported message format " + format);
            return null;
        }
        if (wrappedMessage != null) {
            MtkSmsMessage msg = new MtkSmsMessage(wrappedMessage);
            msg.mFormat = format;
            return msg;
        }
        Rlog.e(LOG_TAG, "createFromPdu(): wrappedMessage is null");
        return null;
    }

    public static MtkSmsMessage newFromCMT(String[] lines) {
        SmsMessageBase wrappedMessage = com.mediatek.internal.telephony.gsm.MtkSmsMessage.newFromCMT(lines);
        if (wrappedMessage != null) {
            MtkSmsMessage msg = new MtkSmsMessage(wrappedMessage);
            msg.mFormat = "3gpp";
            return msg;
        }
        Rlog.e(LOG_TAG, "newFromCMT(): wrappedMessage is null");
        return null;
    }

    public static MtkSmsMessage createFromEfRecord(int index, byte[] data) {
        SmsMessageBase wrappedMessage;
        if (isCdmaVoice()) {
            wrappedMessage = com.mediatek.internal.telephony.cdma.MtkSmsMessage.createFromEfRecord(index, data);
        } else {
            wrappedMessage = com.mediatek.internal.telephony.gsm.MtkSmsMessage.createFromEfRecord(index, data);
        }
        if (wrappedMessage != null) {
            MtkSmsMessage msg = new MtkSmsMessage(wrappedMessage);
            if (isCdmaVoice()) {
                msg.mFormat = "3gpp2";
            } else {
                msg.mFormat = "3gpp";
            }
            return msg;
        }
        Rlog.e(LOG_TAG, "createFromEfRecord(): wrappedMessage is null");
        return null;
    }

    public static int getTPLayerLengthForPDU(String pdu) {
        if (isCdmaVoice()) {
            return com.android.internal.telephony.cdma.SmsMessage.getTPLayerLengthForPDU(pdu);
        }
        return com.mediatek.internal.telephony.gsm.MtkSmsMessage.getTPLayerLengthForPDU(pdu);
    }

    @Override // android.telephony.SmsMessage
    public static int[] calculateLength(CharSequence msgBody, boolean use7bitOnly) {
        GsmAlphabet.TextEncodingDetails ted;
        if (useCdmaFormatForMoSms()) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLength(msgBody, use7bitOnly, true);
        } else {
            ted = com.mediatek.internal.telephony.gsm.MtkSmsMessage.calculateLength(msgBody, use7bitOnly);
        }
        return new int[]{ted.msgCount, ted.codeUnitCount, ted.codeUnitsRemaining, ted.codeUnitSize};
    }

    public static ArrayList<String> fragmentText(String text) {
        GsmAlphabet.TextEncodingDetails ted;
        int udhLength;
        int nextPos;
        int udhLength2;
        boolean isCdma = useCdmaFormatForMoSms();
        if (isCdma) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLength(text, false, true);
        } else {
            ted = com.mediatek.internal.telephony.gsm.MtkSmsMessage.calculateLength(text, false);
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
            udhLength = MtkServiceState.RIL_RADIO_TECHNOLOGY_DC_UPA;
            if (!hasEmsSupport() && ted.msgCount < 10) {
                udhLength = MtkServiceState.RIL_RADIO_TECHNOLOGY_DC_UPA - 2;
            }
        } else {
            udhLength = 140;
        }
        String newMsgBody = null;
        if (Resources.getSystem().getBoolean(17891524)) {
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
            } else if (!useCdmaFormatForMoSms() || ted.msgCount != 1) {
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

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: mediatek.telephony.MtkSmsMessage.calculateLength(java.lang.CharSequence, boolean):int[]
     arg types: [java.lang.String, boolean]
     candidates:
      mediatek.telephony.MtkSmsMessage.calculateLength(java.lang.String, boolean):int[]
      ClspMth{android.telephony.SmsMessage.calculateLength(java.lang.String, boolean):int[]}
      mediatek.telephony.MtkSmsMessage.calculateLength(java.lang.CharSequence, boolean):int[] */
    @Override // android.telephony.SmsMessage
    public static int[] calculateLength(String messageBody, boolean use7bitOnly) {
        return calculateLength((CharSequence) messageBody, use7bitOnly);
    }

    public static SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested) {
        SmsMessageBase.SubmitPduBase spb;
        if (useCdmaFormatForMoSms()) {
            spb = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, (SmsHeader) null);
        } else {
            spb = com.mediatek.internal.telephony.gsm.MtkSmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested);
        }
        return new SmsMessage.SubmitPdu(spb);
    }

    public static SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, short destinationPort, byte[] data, boolean statusReportRequested) {
        SmsMessageBase.SubmitPduBase spb;
        if (useCdmaFormatForMoSms()) {
            spb = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddress, destinationAddress, destinationPort, data, statusReportRequested);
        } else {
            spb = com.mediatek.internal.telephony.gsm.MtkSmsMessage.getSubmitPdu(scAddress, destinationAddress, destinationPort, data, statusReportRequested);
        }
        return new SmsMessage.SubmitPdu(spb);
    }

    private static final SmsMessageBase getSmsFacility() {
        if (isCdmaVoice()) {
            return new com.mediatek.internal.telephony.cdma.MtkSmsMessage();
        }
        return new com.mediatek.internal.telephony.gsm.MtkSmsMessage();
    }

    public MtkSmsMessage() {
        this(getSmsFacility());
        if (isCdmaVoice()) {
            this.mFormat = "3gpp2";
        } else {
            this.mFormat = "3gpp";
        }
    }

    public static MtkSmsMessage newFromCDS(byte[] pdu) {
        MtkSmsMessage msg = new MtkSmsMessage(com.mediatek.internal.telephony.gsm.MtkSmsMessage.newFromCDS(pdu));
        msg.mFormat = "3gpp";
        return msg;
    }

    public static SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header) {
        SmsMessageBase.SubmitPduBase spb;
        if (useCdmaFormatForMoSms()) {
            spb = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, SmsHeader.fromByteArray(header));
        } else {
            spb = com.mediatek.internal.telephony.gsm.MtkSmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, header);
        }
        return new SmsMessage.SubmitPdu(spb);
    }

    public static SmsMessage.SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, short destinationPort, short originalPort, byte[] data, boolean statusReportRequested) {
        SmsMessageBase.SubmitPduBase spb;
        Rlog.d(LOG_TAG, "[xj android.telephony.SmsMessage getSubmitPdu");
        if (useCdmaFormatForMoSms()) {
            spb = com.mediatek.internal.telephony.cdma.MtkSmsMessage.getSubmitPdu(scAddress, destinationAddress, destinationPort, data, statusReportRequested);
        } else {
            spb = com.mediatek.internal.telephony.gsm.MtkSmsMessage.getSubmitPdu(scAddress, destinationAddress, destinationPort, originalPort, data, statusReportRequested);
        }
        if (spb != null) {
            return new SmsMessage.SubmitPdu(spb);
        }
        return null;
    }

    public String getDestinationAddress() {
        if ("3gpp2".equals(this.mFormat)) {
            return this.mWrappedSmsMessage.getDestinationAddress();
        }
        return this.mWrappedSmsMessage.getDestinationAddress();
    }

    public SmsHeader getUserDataHeader() {
        return this.mWrappedSmsMessage.getUserDataHeader();
    }

    public byte[] getSmsc() {
        Rlog.d(LOG_TAG, "getSmsc");
        byte[] pdu = getPdu();
        if (isCdma()) {
            Rlog.d(LOG_TAG, "getSmsc with CDMA and return null");
            return null;
        } else if (pdu == null) {
            Rlog.d(LOG_TAG, "pdu is null");
            return null;
        } else {
            byte[] smsc = new byte[((pdu[0] & 255) + 1)];
            try {
                System.arraycopy(pdu, 0, smsc, 0, smsc.length);
                return smsc;
            } catch (ArrayIndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "Out of boudns");
                return null;
            }
        }
    }

    public byte[] getTpdu() {
        Rlog.d(LOG_TAG, "getTpdu");
        byte[] pdu = getPdu();
        if (isCdma()) {
            Rlog.d(LOG_TAG, "getSmsc with CDMA and return null");
            return pdu;
        } else if (pdu == null) {
            Rlog.d(LOG_TAG, "pdu is null");
            return null;
        } else {
            int smscLen = (pdu[0] & 255) + 1;
            byte[] tpdu = new byte[(pdu.length - smscLen)];
            try {
                System.arraycopy(pdu, smscLen, tpdu, 0, tpdu.length);
                return tpdu;
            } catch (ArrayIndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "Out of boudns");
                return null;
            }
        }
    }

    public static int[] calculateLength(CharSequence msgBody, boolean use7bitOnly, int encodingType) {
        GsmAlphabet.TextEncodingDetails ted;
        if (useCdmaFormatForMoSms()) {
            ted = com.mediatek.internal.telephony.cdma.MtkSmsMessage.calculateLength(msgBody, use7bitOnly, encodingType);
        } else {
            ted = com.mediatek.internal.telephony.gsm.MtkSmsMessage.calculateLength(msgBody, use7bitOnly, encodingType);
        }
        return new int[]{ted.msgCount, ted.codeUnitCount, ted.codeUnitsRemaining, ted.codeUnitSize};
    }

    public static ArrayList<String> fragmentText(String text, int encodingType) {
        GsmAlphabet.TextEncodingDetails ted;
        int udhLength;
        int nextPos;
        int udhLength2;
        boolean isCdma = useCdmaFormatForMoSms();
        if (isCdma) {
            ted = com.mediatek.internal.telephony.cdma.MtkSmsMessage.calculateLength(text, false, encodingType);
        } else {
            ted = com.mediatek.internal.telephony.gsm.MtkSmsMessage.calculateLength(text, false, encodingType);
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
            udhLength = MtkServiceState.RIL_RADIO_TECHNOLOGY_DC_UPA;
            if (!hasEmsSupport() && ted.msgCount < 10) {
                udhLength = MtkServiceState.RIL_RADIO_TECHNOLOGY_DC_UPA - 2;
            }
        } else {
            udhLength = 140;
        }
        String newMsgBody = null;
        if (Resources.getSystem().getBoolean(17891524)) {
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
            } else if (!useCdmaFormatForMoSms() || ted.msgCount != 1) {
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

    public ArrayList<String> fragmentTextUsingTed(int subId, String text, GsmAlphabet.TextEncodingDetails ted) {
        boolean useCdmaFormat;
        int udhLength;
        int nextPos;
        int udhLength2;
        if (!SmsManager.getSmsManagerForSubscriptionId(subId).isImsSmsSupported()) {
            useCdmaFormat = TelephonyManager.getDefault().getCurrentPhoneType() == 2;
        } else {
            useCdmaFormat = "3gpp2".equals(SmsManager.getSmsManagerForSubscriptionId(subId).getImsSmsFormat());
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
            udhLength = MtkServiceState.RIL_RADIO_TECHNOLOGY_DC_UPA;
            if (!hasEmsSupport() && ted.msgCount < 10) {
                udhLength = MtkServiceState.RIL_RADIO_TECHNOLOGY_DC_UPA - 2;
            }
        } else {
            udhLength = 140;
        }
        String newMsgBody = null;
        if (Resources.getSystem().getBoolean(17891524)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(text, useCdmaFormat);
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
            } else if (!useCdmaFormat || ted.msgCount != 1) {
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
        return result;
    }

    public static MtkSmsMessage createFromEfRecord(int index, byte[] data, String format) {
        SmsMessageBase wrappedMessage;
        Rlog.d(LOG_TAG, "createFromEfRecord(): format " + format);
        if ("3gpp2".equals(format)) {
            wrappedMessage = com.mediatek.internal.telephony.cdma.MtkSmsMessage.createFromEfRecord(index, data);
        } else if ("3gpp".equals(format)) {
            wrappedMessage = com.mediatek.internal.telephony.gsm.MtkSmsMessage.createFromEfRecord(index, data);
        } else {
            Rlog.e(LOG_TAG, "createFromEfRecord(): unsupported message format " + format);
            return null;
        }
        if (wrappedMessage == null) {
            return null;
        }
        MtkSmsMessage msg = new MtkSmsMessage(wrappedMessage);
        msg.mFormat = format;
        return msg;
    }

    private boolean isCdma() {
        return 2 == TelephonyManager.getDefault().getCurrentPhoneType(this.mSubId);
    }

    public int getEncodingType() {
        if ("3gpp2".equals(this.mFormat)) {
            return 0;
        }
        return this.mWrappedSmsMessage.getEncodingType();
    }

    protected static boolean useCdmaFormatForMoSms() {
        if (!MtkSmsManager.getDefault().isImsSmsSupported()) {
            return isCdmaVoice();
        }
        return "3gpp2".equals(MtkSmsManager.getDefault().getImsSmsFormat());
    }
}

package android.telephony;

import android.content.res.Resources;
import android.os.Binder;
import android.text.TextUtils;
import com.android.internal.R;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import java.util.ArrayList;
import java.util.Arrays;

public class SmsMessage {
    /* renamed from: -com-android-internal-telephony-SmsConstants$MessageClassSwitchesValues */
    private static final /* synthetic */ int[] f47xe0552cb5 = null;
    public static final int ENCODING_16BIT = 3;
    public static final int ENCODING_7BIT = 1;
    public static final int ENCODING_8BIT = 2;
    public static final int ENCODING_KSC5601 = 4;
    public static final int ENCODING_UNKNOWN = 0;
    public static final String FORMAT_3GPP = "3gpp";
    public static final String FORMAT_3GPP2 = "3gpp2";
    private static final String LOG_TAG = "SmsMessage";
    public static final int MAX_USER_DATA_BYTES = 140;
    public static final int MAX_USER_DATA_BYTES_WITH_HEADER = 134;
    public static final int MAX_USER_DATA_SEPTETS = 160;
    public static final int MAX_USER_DATA_SEPTETS_WITH_HEADER = 153;
    private static boolean mIsNoEmsSupportConfigListLoaded = false;
    private static NoEmsSupportConfig[] mNoEmsSupportConfigList = null;
    private int mSubId = 0;
    public SmsMessageBase mWrappedSmsMessage;

    public enum MessageClass {
        UNKNOWN,
        CLASS_0,
        CLASS_1,
        CLASS_2,
        CLASS_3
    }

    private static class NoEmsSupportConfig {
        String mGid1;
        boolean mIsPrefix;
        String mOperatorNumber;

        public NoEmsSupportConfig(String[] config) {
            this.mOperatorNumber = config[0];
            this.mIsPrefix = "prefix".equals(config[1]);
            this.mGid1 = config.length > 2 ? config[2] : null;
        }

        public String toString() {
            return "NoEmsSupportConfig { mOperatorNumber = " + this.mOperatorNumber + ", mIsPrefix = " + this.mIsPrefix + ", mGid1 = " + this.mGid1 + " }";
        }
    }

    public static class SubmitPdu {
        public byte[] encodedMessage;
        public byte[] encodedScAddress;

        public String toString() {
            return "SubmitPdu: encodedScAddress = " + Arrays.toString(this.encodedScAddress) + ", encodedMessage = " + Arrays.toString(this.encodedMessage);
        }

        protected SubmitPdu(SubmitPduBase spb) {
            this.encodedMessage = spb.encodedMessage;
            this.encodedScAddress = spb.encodedScAddress;
        }
    }

    /* renamed from: -getcom-android-internal-telephony-SmsConstants$MessageClassSwitchesValues */
    private static /* synthetic */ int[] m26x492fb91() {
        if (f47xe0552cb5 != null) {
            return f47xe0552cb5;
        }
        int[] iArr = new int[com.android.internal.telephony.SmsConstants.MessageClass.values().length];
        try {
            iArr[com.android.internal.telephony.SmsConstants.MessageClass.CLASS_0.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[com.android.internal.telephony.SmsConstants.MessageClass.CLASS_1.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[com.android.internal.telephony.SmsConstants.MessageClass.CLASS_2.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[com.android.internal.telephony.SmsConstants.MessageClass.CLASS_3.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[com.android.internal.telephony.SmsConstants.MessageClass.UNKNOWN.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        f47xe0552cb5 = iArr;
        return iArr;
    }

    public void setSubId(int subId) {
        this.mSubId = subId;
    }

    public int getSubId() {
        return this.mSubId;
    }

    public SmsMessage(SmsMessageBase smb) {
        this.mWrappedSmsMessage = smb;
    }

    @Deprecated
    public static SmsMessage createFromPdu(byte[] pdu) {
        try {
            int activePhone = TelephonyManager.getDefault().getCurrentPhoneType();
            SmsMessage message = createFromPdu(pdu, 2 == activePhone ? "3gpp2" : "3gpp");
            if (message == null || message.mWrappedSmsMessage == null) {
                message = createFromPdu(pdu, 2 == activePhone ? "3gpp" : "3gpp2");
            }
            return message;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static SmsMessage createFromPdu(byte[] pdu, String format) {
        try {
            SmsMessageBase wrappedMessage;
            if ("3gpp2".equals(format)) {
                wrappedMessage = com.android.internal.telephony.cdma.SmsMessage.createFromPdu(pdu);
            } else if ("3gpp".equals(format)) {
                wrappedMessage = com.android.internal.telephony.gsm.SmsMessage.createFromPdu(pdu);
            } else {
                Rlog.e(LOG_TAG, "createFromPdu(): unsupported message format " + format);
                return null;
            }
            if (wrappedMessage != null) {
                return new SmsMessage(wrappedMessage);
            }
            Rlog.e(LOG_TAG, "createFromPdu(): wrappedMessage is null");
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static SmsMessage newFromCMT(byte[] pdu) {
        try {
            SmsMessageBase wrappedMessage = com.android.internal.telephony.gsm.SmsMessage.newFromCMT(pdu);
            if (wrappedMessage != null) {
                return new SmsMessage(wrappedMessage);
            }
            Rlog.e(LOG_TAG, "newFromCMT(): wrappedMessage is null");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SmsMessage createFromEfRecord(int index, byte[] data) {
        SmsMessageBase wrappedMessage;
        if (isCdmaVoice()) {
            wrappedMessage = com.android.internal.telephony.cdma.SmsMessage.createFromEfRecord(index, data);
        } else {
            wrappedMessage = com.android.internal.telephony.gsm.SmsMessage.createFromEfRecord(index, data);
        }
        if (wrappedMessage != null) {
            return new SmsMessage(wrappedMessage);
        }
        Rlog.e(LOG_TAG, "createFromEfRecord(): wrappedMessage is null");
        return null;
    }

    public static SmsMessage createFromEfRecord(int index, byte[] data, int subId) {
        SmsMessageBase wrappedMessage;
        if (isCdmaVoice(subId)) {
            wrappedMessage = com.android.internal.telephony.cdma.SmsMessage.createFromEfRecord(index, data);
        } else {
            wrappedMessage = com.android.internal.telephony.gsm.SmsMessage.createFromEfRecord(index, data);
        }
        if (wrappedMessage != null) {
            return new SmsMessage(wrappedMessage);
        }
        return null;
    }

    public static int getTPLayerLengthForPDU(String pdu) {
        if (isCdmaVoice()) {
            return com.android.internal.telephony.cdma.SmsMessage.getTPLayerLengthForPDU(pdu);
        }
        return com.android.internal.telephony.gsm.SmsMessage.getTPLayerLengthForPDU(pdu);
    }

    public static int[] calculateLength(CharSequence msgBody, boolean use7bitOnly) {
        TextEncodingDetails ted;
        if (useCdmaFormatForMoSms()) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLength(msgBody, use7bitOnly, true);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLength(msgBody, use7bitOnly);
        }
        return new int[]{ted.msgCount, ted.codeUnitCount, ted.codeUnitsRemaining, ted.codeUnitSize};
    }

    public static int[] calculateLengthOem(CharSequence msgBody, boolean use7bitOnly, int encodingType) {
        if (encodingType != 1 && encodingType != 3) {
            return calculateLength(msgBody, use7bitOnly);
        }
        TextEncodingDetails ted;
        if (useCdmaFormatForMoSms()) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLengthOem(msgBody, use7bitOnly, true, encodingType);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLengthOem(msgBody, use7bitOnly, encodingType);
        }
        return new int[]{ted.msgCount, ted.codeUnitCount, ted.codeUnitsRemaining, ted.codeUnitSize};
    }

    public static int[] calculateLengthOem(CharSequence msgBody, boolean use7bitOnly, int subId, int encodingType) {
        if (encodingType != 1 && encodingType != 3) {
            return calculateLength(msgBody, use7bitOnly);
        }
        TextEncodingDetails ted;
        if (useCdmaFormatForMoSms(subId)) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLengthOem(msgBody, use7bitOnly, true, encodingType);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLengthOem(msgBody, use7bitOnly, encodingType);
        }
        return new int[]{ted.msgCount, ted.codeUnitCount, ted.codeUnitsRemaining, ted.codeUnitSize};
    }

    public static ArrayList<String> fragmentText(String text) {
        TextEncodingDetails ted;
        int limit;
        if (useCdmaFormatForMoSms()) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLength(text, false, true);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLength(text, false);
        }
        if (ted.codeUnitSize == 1) {
            int udhLength;
            if (ted.languageTable != 0 && ted.languageShiftTable != 0) {
                udhLength = 7;
            } else if (ted.languageTable == 0 && ted.languageShiftTable == 0) {
                udhLength = 0;
            } else {
                udhLength = 4;
            }
            if (ted.msgCount > 1) {
                udhLength += 6;
            }
            if (udhLength != 0) {
                udhLength++;
            }
            limit = 160 - udhLength;
        } else if (ted.msgCount > 1) {
            limit = 134;
            if (!hasEmsSupport() && ted.msgCount < 10) {
                limit = 132;
            }
        } else {
            limit = 140;
        }
        CharSequence newMsgBody = null;
        if (Resources.getSystem().getBoolean(R.bool.config_sms_force_7bit_encoding)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(text);
        }
        if (TextUtils.isEmpty(newMsgBody)) {
            newMsgBody = text;
        }
        int pos = 0;
        int textLen = newMsgBody.length();
        ArrayList<String> result = new ArrayList(ted.msgCount);
        while (pos < textLen) {
            int nextPos;
            if (ted.codeUnitSize != 1) {
                nextPos = SmsMessageBase.findNextUnicodePosition(pos, limit, newMsgBody);
            } else if (useCdmaFormatForMoSms() && ted.msgCount == 1) {
                nextPos = pos + Math.min(limit, textLen - pos);
            } else {
                nextPos = GsmAlphabet.findGsmSeptetLimitIndex(newMsgBody, pos, limit, ted.languageTable, ted.languageShiftTable);
            }
            if (nextPos <= pos || nextPos > textLen) {
                Rlog.e(LOG_TAG, "fragmentText failed (" + pos + " >= " + nextPos + " or " + nextPos + " >= " + textLen + ")");
                break;
            }
            result.add(newMsgBody.substring(pos, nextPos));
            pos = nextPos;
        }
        return result;
    }

    public static ArrayList<String> oemFragmentText(String text, int subid, int encodingType) {
        TextEncodingDetails ted;
        int limit;
        if (useCdmaFormatForMoSms(subid)) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLengthOem(text, false, true, encodingType);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLengthOem(text, false, encodingType);
        }
        if (ted != null) {
            Rlog.d("sms", "ted.codeUnitSize=" + ted.codeUnitSize);
        }
        if (ted.codeUnitSize == 1) {
            int udhLength;
            if (ted.languageTable != 0 && ted.languageShiftTable != 0) {
                udhLength = 7;
            } else if (ted.languageTable == 0 && ted.languageShiftTable == 0) {
                udhLength = 0;
            } else {
                udhLength = 4;
            }
            if (ted.msgCount > 1) {
                udhLength += 6;
            }
            if (udhLength != 0) {
                udhLength++;
            }
            limit = 160 - udhLength;
        } else if (ted.msgCount > 1) {
            limit = 134;
            if (!hasEmsSupport() && ted.msgCount < 10) {
                limit = 132;
            }
        } else {
            limit = 140;
        }
        CharSequence newMsgBody = null;
        if (Resources.getSystem().getBoolean(R.bool.config_sms_force_7bit_encoding)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(text);
        }
        if (TextUtils.isEmpty(newMsgBody)) {
            newMsgBody = text;
        }
        int pos = 0;
        int textLen = newMsgBody.length();
        ArrayList<String> result = new ArrayList(ted.msgCount);
        while (pos < textLen) {
            int nextPos;
            if (ted.codeUnitSize != 1) {
                nextPos = SmsMessageBase.findNextUnicodePosition(pos, limit, newMsgBody);
            } else if (useCdmaFormatForMoSms(subid) && ted.msgCount == 1) {
                nextPos = pos + Math.min(limit, textLen - pos);
            } else {
                nextPos = GsmAlphabet.findGsmSeptetLimitIndex(newMsgBody, pos, limit, ted.languageTable, ted.languageShiftTable);
            }
            if (nextPos <= pos || nextPos > textLen) {
                Rlog.e(LOG_TAG, "fragmentText failed (" + pos + " >= " + nextPos + " or " + nextPos + " >= " + textLen + ")");
                break;
            }
            result.add(newMsgBody.substring(pos, nextPos));
            pos = nextPos;
        }
        return result;
    }

    public static int[] calculateLength(String messageBody, boolean use7bitOnly) {
        return calculateLength((CharSequence) messageBody, use7bitOnly);
    }

    public static int[] calculateLengthOem(String messageBody, boolean use7bitOnly, int encodingType) {
        return calculateLengthOem((CharSequence) messageBody, use7bitOnly, encodingType);
    }

    public static int[] calculateLengthOem(String messageBody, boolean use7bitOnly, int subId, int encodingType) {
        return calculateLengthOem((CharSequence) messageBody, use7bitOnly, subId, encodingType);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, SubscriptionManager.getDefaultSmsSubscriptionId());
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, int subId) {
        SubmitPduBase spb;
        if (useCdmaFormatForMoSms(subId)) {
            spb = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, null);
        } else {
            spb = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested);
        }
        return new SubmitPdu(spb);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, short destinationPort, byte[] data, boolean statusReportRequested) {
        SubmitPduBase spb;
        if (useCdmaFormatForMoSms()) {
            spb = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddress, destinationAddress, (int) destinationPort, data, statusReportRequested);
        } else {
            spb = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddress, destinationAddress, (int) destinationPort, data, statusReportRequested);
        }
        return new SubmitPdu(spb);
    }

    public String getDestinationAddress() {
        return getRecipientAddress();
    }

    public String getServiceCenterAddress() {
        return this.mWrappedSmsMessage.getServiceCenterAddress();
    }

    public String getOriginatingAddress() {
        return this.mWrappedSmsMessage.getOriginatingAddress();
    }

    public String getDisplayOriginatingAddress() {
        return this.mWrappedSmsMessage.getDisplayOriginatingAddress();
    }

    public String getMessageBody() {
        return this.mWrappedSmsMessage.getMessageBody();
    }

    public MessageClass getMessageClass() {
        switch (m26x492fb91()[this.mWrappedSmsMessage.getMessageClass().ordinal()]) {
            case 1:
                return MessageClass.CLASS_0;
            case 2:
                return MessageClass.CLASS_1;
            case 3:
                return MessageClass.CLASS_2;
            case 4:
                return MessageClass.CLASS_3;
            default:
                return MessageClass.UNKNOWN;
        }
    }

    public String getDisplayMessageBody() {
        return this.mWrappedSmsMessage.getDisplayMessageBody();
    }

    public String getPseudoSubject() {
        return this.mWrappedSmsMessage.getPseudoSubject();
    }

    public long getTimestampMillis() {
        return this.mWrappedSmsMessage.getTimestampMillis();
    }

    public boolean isEmail() {
        return this.mWrappedSmsMessage.isEmail();
    }

    public String getEmailBody() {
        return this.mWrappedSmsMessage.getEmailBody();
    }

    public String getEmailFrom() {
        return this.mWrappedSmsMessage.getEmailFrom();
    }

    public int getProtocolIdentifier() {
        return this.mWrappedSmsMessage.getProtocolIdentifier();
    }

    public boolean isReplace() {
        return this.mWrappedSmsMessage.isReplace();
    }

    public boolean isCphsMwiMessage() {
        return this.mWrappedSmsMessage.isCphsMwiMessage();
    }

    public boolean isMWIClearMessage() {
        return this.mWrappedSmsMessage.isMWIClearMessage();
    }

    public boolean isMWISetMessage() {
        return this.mWrappedSmsMessage.isMWISetMessage();
    }

    public boolean isMwiDontStore() {
        return this.mWrappedSmsMessage.isMwiDontStore();
    }

    public byte[] getUserData() {
        return this.mWrappedSmsMessage.getUserData();
    }

    public byte[] getPdu() {
        return this.mWrappedSmsMessage.getPdu();
    }

    @Deprecated
    public int getStatusOnSim() {
        return this.mWrappedSmsMessage.getStatusOnIcc();
    }

    public int getStatusOnIcc() {
        return this.mWrappedSmsMessage.getStatusOnIcc();
    }

    @Deprecated
    public int getIndexOnSim() {
        return this.mWrappedSmsMessage.getIndexOnIcc();
    }

    public int getIndexOnIcc() {
        return this.mWrappedSmsMessage.getIndexOnIcc();
    }

    public int getStatus() {
        return this.mWrappedSmsMessage.getStatus();
    }

    public boolean isStatusReportMessage() {
        return this.mWrappedSmsMessage.isStatusReportMessage();
    }

    public boolean isReplyPathPresent() {
        return this.mWrappedSmsMessage.isReplyPathPresent();
    }

    private static boolean useCdmaFormatForMoSms() {
        return useCdmaFormatForMoSms(SubscriptionManager.getDefaultSmsSubscriptionId());
    }

    private static boolean useCdmaFormatForMoSms(int subId) {
        SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(subId);
        if (smsManager.isImsSmsSupported()) {
            return "3gpp2".equals(smsManager.getImsSmsFormat());
        }
        return isCdmaVoice(subId);
    }

    private static boolean isCdmaVoice() {
        return isCdmaVoice(SubscriptionManager.getDefaultSmsSubscriptionId());
    }

    private static boolean isCdmaVoice(int subId) {
        return 2 == TelephonyManager.getDefault().getCurrentPhoneType(subId);
    }

    public static boolean hasEmsSupport() {
        if (!isNoEmsSupportConfigListExisted()) {
            return true;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            String simOperator = TelephonyManager.getDefault().getSimOperatorNumeric();
            String gid = TelephonyManager.getDefault().getGroupIdLevel1();
            if (!TextUtils.isEmpty(simOperator)) {
                for (NoEmsSupportConfig currentConfig : mNoEmsSupportConfigList) {
                    if (simOperator.startsWith(currentConfig.mOperatorNumber) && (TextUtils.isEmpty(currentConfig.mGid1) || (!TextUtils.isEmpty(currentConfig.mGid1) && currentConfig.mGid1.equalsIgnoreCase(gid)))) {
                        return false;
                    }
                }
            }
            return true;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public static boolean shouldAppendPageNumberAsPrefix() {
        if (!isNoEmsSupportConfigListExisted()) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            String simOperator = TelephonyManager.getDefault().getSimOperatorNumeric();
            String gid = TelephonyManager.getDefault().getGroupIdLevel1();
            for (NoEmsSupportConfig currentConfig : mNoEmsSupportConfigList) {
                if (simOperator.startsWith(currentConfig.mOperatorNumber) && (TextUtils.isEmpty(currentConfig.mGid1) || (!TextUtils.isEmpty(currentConfig.mGid1) && currentConfig.mGid1.equalsIgnoreCase(gid)))) {
                    return currentConfig.mIsPrefix;
                }
            }
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private static boolean isNoEmsSupportConfigListExisted() {
        if (!mIsNoEmsSupportConfigListLoaded) {
            Resources r = Resources.getSystem();
            if (r != null) {
                String[] listArray = r.getStringArray(R.array.no_ems_support_sim_operators);
                if (listArray != null && listArray.length > 0) {
                    mNoEmsSupportConfigList = new NoEmsSupportConfig[listArray.length];
                    for (int i = 0; i < listArray.length; i++) {
                        mNoEmsSupportConfigList[i] = new NoEmsSupportConfig(listArray[i].split(";"));
                    }
                }
                mIsNoEmsSupportConfigListLoaded = true;
            }
        }
        return (mNoEmsSupportConfigList == null || mNoEmsSupportConfigList.length == 0) ? false : true;
    }

    public String getRecipientAddress() {
        return this.mWrappedSmsMessage.getRecipientAddress();
    }

    public static ArrayList<String> oemFragmentText(String text, int subid) {
        TextEncodingDetails ted;
        int limit;
        if (useCdmaFormatForMoSms(subid)) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLength(text, false, true);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLength(text, false);
        }
        if (ted != null) {
            Rlog.d("sms", "ted.codeUnitSize=" + ted.codeUnitSize);
        }
        if (ted.codeUnitSize == 1) {
            int udhLength;
            if (ted.languageTable != 0 && ted.languageShiftTable != 0) {
                udhLength = 7;
            } else if (ted.languageTable == 0 && ted.languageShiftTable == 0) {
                udhLength = 0;
            } else {
                udhLength = 4;
            }
            if (ted.msgCount > 1) {
                udhLength += 6;
            }
            if (udhLength != 0) {
                udhLength++;
            }
            limit = 160 - udhLength;
        } else if (ted.msgCount > 1) {
            limit = 134;
            if (!hasEmsSupport() && ted.msgCount < 10) {
                limit = 132;
            }
        } else {
            limit = 140;
        }
        CharSequence newMsgBody = null;
        if (Resources.getSystem().getBoolean(R.bool.config_sms_force_7bit_encoding)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(text);
        }
        if (TextUtils.isEmpty(newMsgBody)) {
            newMsgBody = text;
        }
        int pos = 0;
        int textLen = newMsgBody.length();
        ArrayList<String> result = new ArrayList(ted.msgCount);
        while (pos < textLen) {
            int nextPos;
            if (ted.codeUnitSize != 1) {
                nextPos = SmsMessageBase.findNextUnicodePosition(pos, limit, newMsgBody);
            } else if (useCdmaFormatForMoSms(subid) && ted.msgCount == 1) {
                nextPos = pos + Math.min(limit, textLen - pos);
            } else {
                nextPos = GsmAlphabet.findGsmSeptetLimitIndex(newMsgBody, pos, limit, ted.languageTable, ted.languageShiftTable);
            }
            if (nextPos <= pos || nextPos > textLen) {
                Rlog.e(LOG_TAG, "fragmentText failed (" + pos + " >= " + nextPos + " or " + nextPos + " >= " + textLen + ")");
                break;
            }
            result.add(newMsgBody.substring(pos, nextPos));
            pos = nextPos;
        }
        return result;
    }

    public static int[] calculateLength(CharSequence msgBody, boolean use7bitOnly, int subId) {
        TextEncodingDetails ted;
        if (useCdmaFormatForMoSms(subId)) {
            ted = com.android.internal.telephony.cdma.SmsMessage.calculateLength(msgBody, use7bitOnly, true);
        } else {
            ted = com.android.internal.telephony.gsm.SmsMessage.calculateLength(msgBody, use7bitOnly);
        }
        return new int[]{ted.msgCount, ted.codeUnitCount, ted.codeUnitsRemaining, ted.codeUnitSize};
    }

    public int getEncodingType() {
        return this.mWrappedSmsMessage.getEncodingType();
    }
}

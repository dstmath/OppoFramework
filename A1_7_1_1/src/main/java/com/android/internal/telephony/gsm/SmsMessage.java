package com.android.internal.telephony.gsm;

import android.content.res.Resources;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.text.format.Time;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.Sms7BitEncodingTranslator;
import com.android.internal.telephony.SmsConstants.MessageClass;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsHeader.PortAddrs;
import com.android.internal.telephony.SmsHeader.SpecialSmsMsg;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsMessageBase.DeliverPduBase;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause;
import com.android.internal.telephony.uicc.IccUtils;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SmsMessage extends SmsMessageBase {
    public static final int ENCODING_7BIT_LOCKING = 12;
    public static final int ENCODING_7BIT_LOCKING_SINGLE = 13;
    public static final int ENCODING_7BIT_SINGLE = 11;
    static final String LOG_TAG = "SmsMessage";
    public static final int MASK_MESSAGE_TYPE_INDICATOR = 3;
    public static final int MASK_USER_DATA_HEADER_INDICATOR = 64;
    public static final int MASK_VALIDITY_PERIOD_FORMAT = 24;
    public static final int MASK_VALIDITY_PERIOD_FORMAT_ABSOLUTE = 24;
    public static final int MASK_VALIDITY_PERIOD_FORMAT_ENHANCED = 8;
    public static final int MASK_VALIDITY_PERIOD_FORMAT_NONE = 0;
    public static final int MASK_VALIDITY_PERIOD_FORMAT_RELATIVE = 16;
    private static final boolean VDBG = false;
    private int mDataCodingScheme;
    private int mEncodingType;
    private boolean mIsStatusReportMessage;
    private int mMti;
    private int mProtocolIdentifier;
    private GsmSmsAddress mRecipientAddress;
    private boolean mReplyPathPresent;
    private int mStatus;
    private int mVoiceMailCount;
    private MessageClass messageClass;

    public static class DeliverPdu extends DeliverPduBase {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.gsm.SmsMessage.DeliverPdu.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public DeliverPdu() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.gsm.SmsMessage.DeliverPdu.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.SmsMessage.DeliverPdu.<init>():void");
        }
    }

    private static class PduParser {
        int mCur;
        byte[] mPdu;
        byte[] mUserData;
        SmsHeader mUserDataHeader;
        int mUserDataSeptetPadding;

        PduParser(byte[] pdu) {
            this.mPdu = pdu;
            this.mCur = 0;
            this.mUserDataSeptetPadding = 0;
        }

        String getSCAddress() {
            String ret;
            int len = getByte();
            if (len == 0) {
                ret = null;
            } else {
                try {
                    ret = PhoneNumberUtils.calledPartyBCDToString(this.mPdu, this.mCur, len);
                } catch (RuntimeException tr) {
                    Rlog.d(SmsMessage.LOG_TAG, "invalid SC address: ", tr);
                    ret = null;
                }
            }
            this.mCur += len;
            return ret;
        }

        int getByte() {
            byte[] bArr = this.mPdu;
            int i = this.mCur;
            this.mCur = i + 1;
            return bArr[i] & 255;
        }

        GsmSmsAddress getAddress() {
            int lengthBytes = (((this.mPdu[this.mCur] & 255) + 1) / 2) + 2;
            try {
                GsmSmsAddress ret = new GsmSmsAddress(this.mPdu, this.mCur, lengthBytes);
                this.mCur += lengthBytes;
                return ret;
            } catch (ParseException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        long getSCTimestampMillis() {
            byte[] bArr = this.mPdu;
            int i = this.mCur;
            this.mCur = i + 1;
            int year = IccUtils.gsmBcdByteToInt(bArr[i]);
            bArr = this.mPdu;
            i = this.mCur;
            this.mCur = i + 1;
            int month = IccUtils.gsmBcdByteToInt(bArr[i]);
            bArr = this.mPdu;
            i = this.mCur;
            this.mCur = i + 1;
            int day = IccUtils.gsmBcdByteToInt(bArr[i]);
            bArr = this.mPdu;
            i = this.mCur;
            this.mCur = i + 1;
            int hour = IccUtils.gsmBcdByteToInt(bArr[i]);
            bArr = this.mPdu;
            i = this.mCur;
            this.mCur = i + 1;
            int minute = IccUtils.gsmBcdByteToInt(bArr[i]);
            bArr = this.mPdu;
            i = this.mCur;
            this.mCur = i + 1;
            int second = IccUtils.gsmBcdByteToInt(bArr[i]);
            bArr = this.mPdu;
            i = this.mCur;
            this.mCur = i + 1;
            byte tzByte = bArr[i];
            int timezoneOffset = IccUtils.gsmBcdByteToInt((byte) (tzByte & -9));
            if ((tzByte & 8) != 0) {
                timezoneOffset = -timezoneOffset;
            }
            Time time = new Time("UTC");
            time.year = year >= 90 ? year + 1900 : year + ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT;
            time.month = month - 1;
            time.monthDay = day;
            time.hour = hour;
            time.minute = minute;
            time.second = second;
            return time.toMillis(true) - ((long) (((timezoneOffset * 15) * 60) * 1000));
        }

        int constructUserData(boolean hasUserDataHeader, boolean dataInSeptets) {
            int i;
            int bufferLen;
            int i2 = 0;
            int offset = this.mCur;
            int offset2 = offset + 1;
            int userDataLength = this.mPdu[offset] & 255;
            int headerSeptets = 0;
            int userDataHeaderLength = 0;
            if (hasUserDataHeader) {
                offset = offset2 + 1;
                userDataHeaderLength = this.mPdu[offset2] & 255;
                byte[] udh = new byte[userDataHeaderLength];
                System.arraycopy(this.mPdu, offset, udh, 0, userDataHeaderLength);
                this.mUserDataHeader = SmsHeader.fromByteArray(udh);
                offset += userDataHeaderLength;
                int headerBits = (userDataHeaderLength + 1) * 8;
                headerSeptets = headerBits / 7;
                if (headerBits % 7 > 0) {
                    i = 1;
                } else {
                    i = 0;
                }
                headerSeptets += i;
                this.mUserDataSeptetPadding = (headerSeptets * 7) - headerBits;
            } else {
                offset = offset2;
            }
            if (dataInSeptets) {
                bufferLen = this.mPdu.length - offset;
            } else {
                if (hasUserDataHeader) {
                    i = userDataHeaderLength + 1;
                } else {
                    i = 0;
                }
                bufferLen = userDataLength - i;
                if (bufferLen < 0) {
                    bufferLen = 0;
                }
            }
            this.mUserData = new byte[bufferLen];
            System.arraycopy(this.mPdu, offset, this.mUserData, 0, this.mUserData.length);
            this.mCur = offset;
            if (!dataInSeptets) {
                return this.mUserData.length;
            }
            int count = userDataLength - headerSeptets;
            if (count >= 0) {
                i2 = count;
            }
            return i2;
        }

        byte[] getUserData() {
            return this.mUserData;
        }

        SmsHeader getUserDataHeader() {
            return this.mUserDataHeader;
        }

        String getUserDataGSM7Bit(int septetCount, int languageTable, int languageShiftTable) {
            String ret = GsmAlphabet.gsm7BitPackedToString(this.mPdu, this.mCur, septetCount, this.mUserDataSeptetPadding, languageTable, languageShiftTable);
            this.mCur += (septetCount * 7) / 8;
            return ret;
        }

        String getUserDataGSM8bit(int byteCount) {
            String ret = GsmAlphabet.gsm8BitUnpackedToString(this.mPdu, this.mCur, byteCount);
            this.mCur += byteCount;
            return ret;
        }

        String getUserDataUCS2(int byteCount) {
            String ret;
            try {
                ret = new String(this.mPdu, this.mCur, byteCount, CharacterSets.MIMENAME_UTF_16);
            } catch (UnsupportedEncodingException ex) {
                ret = UsimPBMemInfo.STRING_NOT_SET;
                Rlog.e(SmsMessage.LOG_TAG, "implausible UnsupportedEncodingException", ex);
            }
            this.mCur += byteCount;
            return ret;
        }

        String getUserDataKSC5601(int byteCount) {
            String ret;
            try {
                ret = new String(this.mPdu, this.mCur, byteCount, "KSC5601");
            } catch (UnsupportedEncodingException ex) {
                ret = UsimPBMemInfo.STRING_NOT_SET;
                Rlog.e(SmsMessage.LOG_TAG, "implausible UnsupportedEncodingException", ex);
            }
            this.mCur += byteCount;
            return ret;
        }

        boolean moreDataPresent() {
            return this.mPdu.length > this.mCur;
        }
    }

    public static class SubmitPdu extends SubmitPduBase {
    }

    public SmsMessage() {
        this.mReplyPathPresent = false;
        this.mIsStatusReportMessage = false;
        this.mVoiceMailCount = 0;
        this.mEncodingType = 0;
    }

    public static SmsMessage createFromPdu(byte[] pdu) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.parsePdu(pdu);
            return msg;
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        } catch (OutOfMemoryError e) {
            Rlog.e(LOG_TAG, "SMS PDU parsing failed with out of memory: ", e);
            return null;
        }
    }

    public boolean isTypeZero() {
        return this.mProtocolIdentifier == 64;
    }

    public static SmsMessage newFromCMT(String[] lines) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.parsePdu(IccUtils.hexStringToBytes(lines[1]));
            return msg;
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        }
    }

    public static SmsMessage newFromCDS(String line) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.parsePdu(IccUtils.hexStringToBytes(line));
            return msg;
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "CDS SMS PDU parsing failed: ", ex);
            return null;
        }
    }

    public static SmsMessage createFromEfRecord(int index, byte[] data) {
        try {
            SmsMessage msg = new SmsMessage();
            msg.mIndexOnIcc = index;
            if ((data[0] & 1) == 0) {
                Rlog.w(LOG_TAG, "SMS parsing failed: Trying to parse a free record");
                return null;
            }
            msg.mStatusOnIcc = data[0] & 7;
            int size = data.length - 1;
            byte[] pdu = new byte[size];
            System.arraycopy(data, 1, pdu, 0, size);
            msg.parsePdu(pdu);
            return msg;
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "SMS PDU parsing failed: ", ex);
            return null;
        }
    }

    public static int getTPLayerLengthForPDU(String pdu) {
        return ((pdu.length() / 2) - Integer.parseInt(pdu.substring(0, 2), 16)) - 1;
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, header, 0, 0, 0);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header, int encoding, int languageTable, int languageShiftTable) {
        if (message == null || destinationAddress == null) {
            return null;
        }
        byte[] userData;
        if (encoding == 0) {
            TextEncodingDetails ted = calculateLength(message, false);
            encoding = ted.codeUnitSize;
            languageTable = ted.languageTable;
            languageShiftTable = ted.languageShiftTable;
            if (encoding == 1 && !(languageTable == 0 && languageShiftTable == 0)) {
                SmsHeader smsHeader;
                if (header != null) {
                    smsHeader = SmsHeader.fromByteArray(header);
                    if (!(smsHeader.languageTable == languageTable && smsHeader.languageShiftTable == languageShiftTable)) {
                        Rlog.w(LOG_TAG, "Updating language table in SMS header: " + smsHeader.languageTable + " -> " + languageTable + ", " + smsHeader.languageShiftTable + " -> " + languageShiftTable);
                        smsHeader.languageTable = languageTable;
                        smsHeader.languageShiftTable = languageShiftTable;
                        header = SmsHeader.toByteArray(smsHeader);
                    }
                } else {
                    smsHeader = new SmsHeader();
                    smsHeader.languageTable = languageTable;
                    smsHeader.languageShiftTable = languageShiftTable;
                    header = SmsHeader.toByteArray(smsHeader);
                }
            }
        }
        SubmitPdu ret = new SubmitPdu();
        ByteArrayOutputStream bo = getSubmitPduHead(scAddress, destinationAddress, (byte) ((header != null ? 64 : 0) | 1), statusReportRequested, ret);
        if (encoding == 1) {
            try {
                userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, languageTable, languageShiftTable);
            } catch (EncodeException e) {
                try {
                    userData = encodeUCS2(message, header);
                    encoding = 3;
                } catch (UnsupportedEncodingException uex) {
                    Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex);
                    return null;
                }
            }
        }
        try {
            userData = encodeUCS2(message, header);
        } catch (UnsupportedEncodingException uex2) {
            Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex2);
            return null;
        }
        if (encoding == 1) {
            if ((userData[0] & 255) > 160) {
                Rlog.e(LOG_TAG, "Message too long (" + (userData[0] & 255) + " septets)");
                return null;
            }
            bo.write(0);
        } else if ((userData[0] & 255) > 140) {
            Rlog.e(LOG_TAG, "Message too long (" + (userData[0] & 255) + " bytes)");
            return null;
        } else {
            bo.write(8);
        }
        bo.write(userData, 0, userData.length);
        ret.encodedMessage = bo.toByteArray();
        return ret;
    }

    private static byte[] encodeUCS2(String message, byte[] header) throws UnsupportedEncodingException {
        byte[] userData;
        byte[] textPart = message.getBytes("utf-16be");
        if (header != null) {
            userData = new byte[((header.length + textPart.length) + 1)];
            userData[0] = (byte) header.length;
            System.arraycopy(header, 0, userData, 1, header.length);
            System.arraycopy(textPart, 0, userData, header.length + 1, textPart.length);
        } else {
            userData = textPart;
        }
        byte[] ret = new byte[(userData.length + 1)];
        ret[0] = (byte) (userData.length & 255);
        System.arraycopy(userData, 0, ret, 1, userData.length);
        return ret;
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested) {
        return getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, null);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, int destinationPort, byte[] data, boolean statusReportRequested) {
        PortAddrs portAddrs = new PortAddrs();
        portAddrs.destPort = destinationPort;
        portAddrs.origPort = 0;
        portAddrs.areEightBits = false;
        SmsHeader smsHeader = new SmsHeader();
        smsHeader.portAddrs = portAddrs;
        byte[] smsHeaderData = SmsHeader.toByteArray(smsHeader);
        if ((data.length + smsHeaderData.length) + 1 > 140) {
            Rlog.e(LOG_TAG, "SMS data message may only contain " + ((140 - smsHeaderData.length) - 1) + " bytes");
            return null;
        }
        SubmitPdu ret = new SubmitPdu();
        ByteArrayOutputStream bo = getSubmitPduHead(scAddress, destinationAddress, (byte) 65, statusReportRequested, ret);
        bo.write(4);
        bo.write((data.length + smsHeaderData.length) + 1);
        bo.write(smsHeaderData.length);
        bo.write(smsHeaderData, 0, smsHeaderData.length);
        bo.write(data, 0, data.length);
        ret.encodedMessage = bo.toByteArray();
        return ret;
    }

    private static ByteArrayOutputStream getSubmitPduHead(String scAddress, String destinationAddress, byte mtiByte, boolean statusReportRequested, SubmitPdu ret) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream(PduHeaders.RECOMMENDED_RETRIEVAL_MODE);
        if (scAddress == null) {
            ret.encodedScAddress = null;
        } else {
            ret.encodedScAddress = PhoneNumberUtils.networkPortionToCalledPartyBCDWithLength(scAddress);
        }
        if (statusReportRequested) {
            mtiByte = (byte) (mtiByte | 32);
        }
        bo.write(mtiByte);
        bo.write(0);
        byte[] daBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(destinationAddress);
        if (daBytes != null) {
            int i;
            int length = (daBytes.length - 1) * 2;
            if ((daBytes[daBytes.length - 1] & CallFailCause.CALL_BARRED) == CallFailCause.CALL_BARRED) {
                i = 1;
            } else {
                i = 0;
            }
            bo.write(length - i);
            bo.write(daBytes, 0, daBytes.length);
        } else {
            Rlog.d(LOG_TAG, "write an empty address for submit pdu");
            bo.write(0);
            bo.write(129);
        }
        bo.write(0);
        return bo;
    }

    public static TextEncodingDetails calculateLength(CharSequence msgBody, boolean use7bitOnly) {
        CharSequence newMsgBody = null;
        if (Resources.getSystem().getBoolean(17957019)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(msgBody);
        }
        if (TextUtils.isEmpty(newMsgBody)) {
            newMsgBody = msgBody;
        }
        TextEncodingDetails ted = GsmAlphabet.countGsmSeptets(newMsgBody, use7bitOnly);
        if (ted == null) {
            return SmsMessageBase.calcUnicodeEncodingDetails(newMsgBody);
        }
        return ted;
    }

    public int getProtocolIdentifier() {
        return this.mProtocolIdentifier;
    }

    int getDataCodingScheme() {
        return this.mDataCodingScheme;
    }

    public boolean isReplace() {
        if ((this.mProtocolIdentifier & 192) != 64 || (this.mProtocolIdentifier & 63) <= 0 || (this.mProtocolIdentifier & 63) >= 8) {
            return false;
        }
        return true;
    }

    public boolean isCphsMwiMessage() {
        if (((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageClear()) {
            return true;
        }
        return ((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageSet();
    }

    public boolean isMWIClearMessage() {
        if (this.mIsMwi && !this.mMwiSense) {
            return true;
        }
        boolean isCphsVoiceMessageClear;
        if (this.mOriginatingAddress != null) {
            isCphsVoiceMessageClear = ((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageClear();
        } else {
            isCphsVoiceMessageClear = false;
        }
        return isCphsVoiceMessageClear;
    }

    public boolean isMWISetMessage() {
        if (this.mIsMwi && this.mMwiSense) {
            return true;
        }
        boolean isCphsVoiceMessageSet;
        if (this.mOriginatingAddress != null) {
            isCphsVoiceMessageSet = ((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageSet();
        } else {
            isCphsVoiceMessageSet = false;
        }
        return isCphsVoiceMessageSet;
    }

    public boolean isMwiDontStore() {
        if (this.mIsMwi && this.mMwiDontStore) {
            return true;
        }
        if (isCphsMwiMessage() && " ".equals(getMessageBody())) {
            return true;
        }
        return false;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public boolean isStatusReportMessage() {
        return this.mIsStatusReportMessage;
    }

    public boolean isReplyPathPresent() {
        return this.mReplyPathPresent;
    }

    private void parsePdu(byte[] pdu) {
        this.mPdu = pdu;
        PduParser p = new PduParser(pdu);
        this.mScAddress = p.getSCAddress();
        if (this.mScAddress != null) {
        }
        int firstByte = p.getByte();
        this.mMti = firstByte & 3;
        switch (this.mMti) {
            case 0:
            case 3:
                parseSmsDeliver(p, firstByte);
                return;
            case 1:
                parseSmsSubmit(p, firstByte);
                return;
            case 2:
                parseSmsStatusReport(p, firstByte);
                return;
            default:
                throw new RuntimeException("Unsupported message type");
        }
    }

    private void parseSmsStatusReport(PduParser p, int firstByte) {
        boolean hasUserDataHeader = true;
        this.mIsStatusReportMessage = true;
        this.mMessageRef = p.getByte();
        this.mRecipientAddress = p.getAddress();
        this.mScTimeMillis = p.getSCTimestampMillis();
        p.getSCTimestampMillis();
        this.mStatus = p.getByte();
        this.mMessageBody = UsimPBMemInfo.STRING_NOT_SET;
        if (p.moreDataPresent()) {
            int extraParams = p.getByte();
            int moreExtraParams = extraParams;
            while ((moreExtraParams & 128) != 0 && p.moreDataPresent()) {
                moreExtraParams = p.getByte();
            }
            if ((extraParams & RilDataCallFailCause.PDP_FAIL_IFACE_AND_POL_FAMILY_MISMATCH) == 0) {
                if ((extraParams & 1) != 0) {
                    this.mProtocolIdentifier = p.getByte();
                }
                if ((extraParams & 2) != 0) {
                    this.mDataCodingScheme = p.getByte();
                }
                if ((extraParams & 4) != 0) {
                    if ((firstByte & 64) != 64) {
                        hasUserDataHeader = false;
                    }
                    parseUserData(p, hasUserDataHeader);
                }
            }
        }
    }

    private void parseSmsDeliver(PduParser p, int firstByte) {
        boolean z;
        if ((firstByte & 128) == 128) {
            z = true;
        } else {
            z = false;
        }
        this.mReplyPathPresent = z;
        this.mOriginatingAddress = p.getAddress();
        if (this.mOriginatingAddress != null) {
        }
        this.mProtocolIdentifier = p.getByte();
        this.mDataCodingScheme = p.getByte();
        this.mScTimeMillis = p.getSCTimestampMillis();
        parseUserData(p, (firstByte & 64) == 64);
    }

    private void parseSmsSubmit(PduParser p, int firstByte) {
        boolean z;
        int validityPeriodLength;
        boolean hasUserDataHeader;
        if ((firstByte & 128) == 128) {
            z = true;
        } else {
            z = false;
        }
        this.mReplyPathPresent = z;
        this.mMessageRef = p.getByte();
        this.mRecipientAddress = p.getAddress();
        this.destinationAddress = this.mRecipientAddress;
        if (this.mRecipientAddress != null) {
        }
        this.mProtocolIdentifier = p.getByte();
        this.mDataCodingScheme = p.getByte();
        int validityPeriodFormat = (firstByte >> 3) & 3;
        if (validityPeriodFormat == 0) {
            validityPeriodLength = 0;
        } else if (2 == validityPeriodFormat) {
            validityPeriodLength = 1;
        } else {
            validityPeriodLength = 7;
        }
        while (true) {
            int validityPeriodLength2 = validityPeriodLength;
            validityPeriodLength = validityPeriodLength2 - 1;
            if (validityPeriodLength2 <= 0) {
                break;
            }
            p.getByte();
        }
        if ((firstByte & 64) == 64) {
            hasUserDataHeader = true;
        } else {
            hasUserDataHeader = false;
        }
        parseUserData(p, hasUserDataHeader);
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseUserData(PduParser p, boolean hasUserDataHeader) {
        boolean hasMessageClass = false;
        int encodingType = 0;
        if ((this.mDataCodingScheme & 128) == 0) {
            boolean userDataCompressed = (this.mDataCodingScheme & 32) != 0;
            hasMessageClass = (this.mDataCodingScheme & 16) != 0;
            if (!userDataCompressed) {
                switch ((this.mDataCodingScheme >> 2) & 3) {
                    case 0:
                        encodingType = 1;
                        break;
                    case 1:
                        if (Resources.getSystem().getBoolean(17957014)) {
                            encodingType = 2;
                            break;
                        }
                    case 2:
                        encodingType = 3;
                        break;
                    case 3:
                        Rlog.w(LOG_TAG, "1 - Unsupported SMS data coding scheme " + (this.mDataCodingScheme & 255));
                        encodingType = 2;
                        break;
                }
            }
            Rlog.w(LOG_TAG, "4 - Unsupported SMS data coding scheme (compression) " + (this.mDataCodingScheme & 255));
        } else if ((this.mDataCodingScheme & CallFailCause.CALL_BARRED) == CallFailCause.CALL_BARRED) {
            hasMessageClass = true;
            encodingType = (this.mDataCodingScheme & 4) == 0 ? 1 : 2;
        } else if ((this.mDataCodingScheme & CallFailCause.CALL_BARRED) == 192 || (this.mDataCodingScheme & CallFailCause.CALL_BARRED) == BerTlv.BER_PROACTIVE_COMMAND_TAG || (this.mDataCodingScheme & CallFailCause.CALL_BARRED) == 224) {
            if ((this.mDataCodingScheme & CallFailCause.CALL_BARRED) == 224) {
                encodingType = 3;
            } else {
                encodingType = 1;
            }
            boolean active = (this.mDataCodingScheme & 8) == 8;
            if ((this.mDataCodingScheme & 3) == 0) {
                this.mIsMwi = true;
                this.mMwiSense = active;
                this.mMwiDontStore = (this.mDataCodingScheme & CallFailCause.CALL_BARRED) == 192;
                if (active) {
                    this.mVoiceMailCount = -1;
                } else {
                    this.mVoiceMailCount = 0;
                }
                Rlog.w(LOG_TAG, "MWI in DCS for Vmail. DCS = " + (this.mDataCodingScheme & 255) + " Dont store = " + this.mMwiDontStore + " vmail count = " + this.mVoiceMailCount);
            } else {
                this.mIsMwi = false;
                Rlog.w(LOG_TAG, "MWI in DCS for fax/email/other: " + (this.mDataCodingScheme & 255));
            }
        } else if ((this.mDataCodingScheme & 192) != 128) {
            Rlog.w(LOG_TAG, "3 - Unsupported SMS data coding scheme " + (this.mDataCodingScheme & 255));
        } else if (this.mDataCodingScheme == 132) {
            encodingType = 4;
        } else {
            Rlog.w(LOG_TAG, "5 - Unsupported SMS data coding scheme " + (this.mDataCodingScheme & 255));
        }
        int count = p.constructUserData(hasUserDataHeader, encodingType == 1);
        this.mUserData = p.getUserData();
        this.mUserDataHeader = p.getUserDataHeader();
        this.mEncodingType = encodingType;
        if (hasUserDataHeader && this.mUserDataHeader.specialSmsMsgList.size() != 0) {
            for (SpecialSmsMsg msg : this.mUserDataHeader.specialSmsMsgList) {
                int msgInd = msg.msgIndType & 255;
                if (msgInd == 0 || msgInd == 128) {
                    this.mIsMwi = true;
                    if (msgInd == 128) {
                        this.mMwiDontStore = false;
                    } else if (!(this.mMwiDontStore || (((this.mDataCodingScheme & CallFailCause.CALL_BARRED) == BerTlv.BER_PROACTIVE_COMMAND_TAG || (this.mDataCodingScheme & CallFailCause.CALL_BARRED) == 224) && (this.mDataCodingScheme & 3) == 0))) {
                        this.mMwiDontStore = true;
                    }
                    this.mVoiceMailCount = msg.msgCount & 255;
                    if (this.mVoiceMailCount > 0) {
                        this.mMwiSense = true;
                    } else {
                        this.mMwiSense = false;
                    }
                    Rlog.w(LOG_TAG, "MWI in TP-UDH for Vmail. Msg Ind = " + msgInd + " Dont store = " + this.mMwiDontStore + " Vmail count = " + this.mVoiceMailCount);
                } else {
                    Rlog.w(LOG_TAG, "TP_UDH fax/email/extended msg/multisubscriber profile. Msg Ind = " + msgInd);
                }
            }
        }
        switch (encodingType) {
            case 0:
                this.mMessageBody = null;
                break;
            case 1:
                this.mMessageBody = p.getUserDataGSM7Bit(count, hasUserDataHeader ? this.mUserDataHeader.languageTable : 0, hasUserDataHeader ? this.mUserDataHeader.languageShiftTable : 0);
                break;
            case 2:
                if (!Resources.getSystem().getBoolean(17957014)) {
                    this.mMessageBody = null;
                    break;
                } else {
                    this.mMessageBody = p.getUserDataGSM8bit(count);
                    break;
                }
            case 3:
                this.mMessageBody = p.getUserDataUCS2(count);
                break;
            case 4:
                this.mMessageBody = p.getUserDataKSC5601(count);
                break;
        }
        if (this.mMessageBody != null) {
            parseMessageBody();
        }
        if (hasMessageClass) {
            switch (this.mDataCodingScheme & 3) {
                case 0:
                    this.messageClass = MessageClass.CLASS_0;
                    return;
                case 1:
                    this.messageClass = MessageClass.CLASS_1;
                    return;
                case 2:
                    this.messageClass = MessageClass.CLASS_2;
                    return;
                case 3:
                    this.messageClass = MessageClass.CLASS_3;
                    return;
                default:
                    return;
            }
        }
        this.messageClass = MessageClass.UNKNOWN;
    }

    public MessageClass getMessageClass() {
        return this.messageClass;
    }

    boolean isUsimDataDownload() {
        if (this.messageClass == MessageClass.CLASS_2) {
            return this.mProtocolIdentifier == CallFailCause.INTERWORKING_UNSPECIFIED || this.mProtocolIdentifier == 124;
        } else {
            return false;
        }
    }

    public int getNumOfVoicemails() {
        if (!this.mIsMwi && isCphsMwiMessage()) {
            if (this.mOriginatingAddress == null || !((GsmSmsAddress) this.mOriginatingAddress).isCphsVoiceMessageSet()) {
                this.mVoiceMailCount = 0;
            } else {
                this.mVoiceMailCount = 255;
            }
            Rlog.v(LOG_TAG, "CPHS voice mail message");
        }
        return this.mVoiceMailCount;
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, int destinationPort, int originalPort, byte[] data, boolean statusReportRequested) {
        byte[] smsHeaderData = SmsHeader.getSubmitPduHeader(destinationPort, originalPort);
        if (smsHeaderData == null) {
            return null;
        }
        return getSubmitPdu(scAddress, destinationAddress, data, smsHeaderData, statusReportRequested);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, int destPort, boolean statusReportRequested) {
        int encoding;
        int language = getCurrentSysLanguage();
        int singleId = -1;
        int lockingId = -1;
        TextEncodingDetails ted = new TextEncodingDetails();
        if (!encodeStringWithSpecialLang(message, language, ted)) {
            encoding = 3;
        } else if (ted.useLockingShift && ted.useSingleShift) {
            encoding = 13;
            lockingId = language;
            singleId = language;
        } else if (ted.useLockingShift) {
            encoding = 12;
            lockingId = language;
        } else if (ted.useSingleShift) {
            encoding = 11;
            singleId = language;
        } else {
            encoding = 1;
            language = -1;
        }
        return getSubmitPduWithLang(scAddress, destinationAddress, message, statusReportRequested, SmsHeader.getSubmitPduHeaderWithLang(destPort, singleId, lockingId), encoding, language);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, byte[] data, byte[] smsHeaderData, boolean statusReportRequested) {
        if ((data.length + smsHeaderData.length) + 1 > 140) {
            Rlog.e(LOG_TAG, "SMS data message may only contain " + ((140 - smsHeaderData.length) - 1) + " bytes");
            return null;
        }
        SubmitPdu ret = new SubmitPdu();
        ByteArrayOutputStream bo = getSubmitPduHead(scAddress, destinationAddress, (byte) 65, statusReportRequested, ret);
        bo.write(4);
        bo.write((data.length + smsHeaderData.length) + 1);
        bo.write(smsHeaderData.length);
        bo.write(smsHeaderData, 0, smsHeaderData.length);
        bo.write(data, 0, data.length);
        ret.encodedMessage = bo.toByteArray();
        return ret;
    }

    public static SubmitPdu getSubmitPduWithLang(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header, int encoding, int language) {
        Rlog.d(LOG_TAG, "SmsMessage: get submit pdu");
        if (message == null || destinationAddress == null) {
            return null;
        }
        byte[] userData;
        SubmitPdu ret = new SubmitPdu();
        Rlog.d(LOG_TAG, "SmsMessage: UDHI = " + (header != null));
        ByteArrayOutputStream bo = getSubmitPduHead(scAddress, destinationAddress, (byte) ((header != null ? 64 : 0) | 1), statusReportRequested, ret);
        if (encoding == 0) {
            encoding = 1;
        }
        try {
            Rlog.d(LOG_TAG, "Get SubmitPdu with Lang " + encoding + " " + language);
            if (encoding == 1) {
                userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, 0, 0);
            } else if (language <= 0 || encoding == 3) {
                try {
                    userData = encodeUCS2(message, header);
                } catch (UnsupportedEncodingException uex) {
                    Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex);
                    return null;
                }
            } else {
                if (encoding == 12) {
                    userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, 0, language);
                } else if (encoding == 11) {
                    userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, language, 0);
                } else if (encoding == 13) {
                    userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, language, language);
                } else {
                    userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, 0, 0);
                }
                encoding = 1;
            }
        } catch (EncodeException e) {
            try {
                userData = encodeUCS2(message, header);
                encoding = 3;
            } catch (UnsupportedEncodingException uex2) {
                Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex2);
                return null;
            }
        }
        if (encoding == 1) {
            if ((userData[0] & 255) > 160) {
                return null;
            }
            bo.write(0);
        } else if ((userData[0] & 255) > 140) {
            return null;
        } else {
            bo.write(8);
        }
        bo.write(userData, 0, userData.length);
        ret.encodedMessage = bo.toByteArray();
        return ret;
    }

    public static DeliverPdu getDeliverPduWithLang(String scAddress, String originalAddress, String message, byte[] header, long timestamp, int encoding, int language) {
        Rlog.d(LOG_TAG, "SmsMessage: get deliver pdu");
        if (message == null || originalAddress == null) {
            return null;
        }
        byte[] userData;
        DeliverPdu ret = new DeliverPdu();
        Rlog.d(LOG_TAG, "SmsMessage: UDHI = " + (header != null));
        ByteArrayOutputStream bo = getDeliverPduHead(scAddress, originalAddress, (byte) ((header != null ? 64 : 0) | 0), ret);
        if (encoding == 0) {
            encoding = 1;
        }
        try {
            Rlog.d(LOG_TAG, "Get SubmitPdu with Lang " + encoding + " " + language);
            if (encoding == 1) {
                userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, 0, 0);
            } else if (language <= 0 || encoding == 3) {
                try {
                    userData = encodeUCS2(message, header);
                } catch (UnsupportedEncodingException uex) {
                    Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex);
                    return null;
                }
            } else {
                if (encoding == 12) {
                    userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, 0, language);
                } else if (encoding == 11) {
                    userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, language, 0);
                } else if (encoding == 13) {
                    userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, language, language);
                } else {
                    userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, 0, 0);
                }
                encoding = 1;
            }
        } catch (EncodeException e) {
            try {
                userData = encodeUCS2(message, header);
                encoding = 3;
            } catch (UnsupportedEncodingException uex2) {
                Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex2);
                return null;
            }
        }
        if (userData == null || (userData[0] & 255) <= 160) {
            if (encoding == 1) {
                bo.write(0);
            } else {
                bo.write(8);
            }
            byte[] scts = parseSCTimestamp(timestamp);
            if (scts != null) {
                bo.write(scts, 0, scts.length);
            } else {
                for (int i = 0; i < 7; i++) {
                    bo.write(0);
                }
            }
            bo.write(userData, 0, userData.length);
            ret.encodedMessage = bo.toByteArray();
            return ret;
        }
        Rlog.d(LOG_TAG, "SmsMessage: message is too long");
        return null;
    }

    private static byte[] parseSCTimestamp(long millis) {
        new Time("UTC").set(millis);
        return new byte[]{intToGsmBCDByte(t.year), intToGsmBCDByte(t.month + 1), intToGsmBCDByte(t.monthDay), intToGsmBCDByte(t.hour), intToGsmBCDByte(t.minute), intToGsmBCDByte(t.second), intToGsmBCDByte(0)};
    }

    private static byte intToGsmBCDByte(int value) {
        if (value < 0) {
            Rlog.d(LOG_TAG, "[time invalid value: " + value);
            return (byte) 0;
        }
        value %= 100;
        Rlog.d(LOG_TAG, "[time value: " + value);
        byte b = (byte) (((value / 10) & 15) | (((value % 10) << 4) & CallFailCause.CALL_BARRED));
        Rlog.d(LOG_TAG, "[time bcd value: " + b);
        return b;
    }

    private static ByteArrayOutputStream getDeliverPduHead(String scAddress, String originalAddress, byte mtiByte, DeliverPdu ret) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream(PduHeaders.RECOMMENDED_RETRIEVAL_MODE);
        if (scAddress == null) {
            ret.encodedScAddress = null;
        } else {
            ret.encodedScAddress = PhoneNumberUtils.networkPortionToCalledPartyBCDWithLength(scAddress);
        }
        bo.write(mtiByte);
        byte[] oaBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(originalAddress);
        if (oaBytes != null) {
            int i;
            int length = (oaBytes.length - 1) * 2;
            if ((oaBytes[oaBytes.length - 1] & CallFailCause.CALL_BARRED) == CallFailCause.CALL_BARRED) {
                i = 1;
            } else {
                i = 0;
            }
            bo.write(length - i);
            bo.write(oaBytes, 0, oaBytes.length);
        } else {
            Rlog.d(LOG_TAG, "write a empty address for deliver pdu");
            bo.write(0);
            bo.write(145);
        }
        bo.write(0);
        return bo;
    }

    private static boolean encodeStringWithSpecialLang(CharSequence msgBody, int language, TextEncodingDetails ted) {
        int septets = GsmAlphabet.countGsmSeptetsUsingTables(msgBody, true, 0, 0);
        if (septets != -1) {
            ted.codeUnitCount = septets;
            if (septets > 160) {
                ted.msgCount = (septets / 153) + 1;
                ted.codeUnitsRemaining = 153 - (septets % 153);
            } else {
                ted.msgCount = 1;
                ted.codeUnitsRemaining = 160 - septets;
            }
            ted.codeUnitSize = 1;
            ted.shiftLangId = -1;
            Rlog.d(LOG_TAG, "Try Default: " + language + " " + ted);
            return true;
        }
        septets = GsmAlphabet.countGsmSeptetsUsingTables(msgBody, true, 0, language);
        int[] headerElt;
        int maxLength;
        if (septets != -1) {
            headerElt = new int[]{37, 65535};
            maxLength = computeRemainUserDataLength(true, headerElt);
            ted.codeUnitCount = septets;
            if (septets > maxLength) {
                headerElt[1] = 0;
                maxLength = computeRemainUserDataLength(true, headerElt);
                ted.msgCount = (septets / maxLength) + 1;
                ted.codeUnitsRemaining = maxLength - (septets % maxLength);
            } else {
                ted.msgCount = 1;
                ted.codeUnitsRemaining = maxLength - septets;
            }
            ted.codeUnitSize = 1;
            ted.useLockingShift = true;
            ted.shiftLangId = language;
            Rlog.d(LOG_TAG, "Try Locking Shift: " + language + " " + ted);
            return true;
        }
        septets = GsmAlphabet.countGsmSeptetsUsingTables(msgBody, true, language, 0);
        if (septets != -1) {
            headerElt = new int[]{36, 65535};
            maxLength = computeRemainUserDataLength(true, headerElt);
            ted.codeUnitCount = septets;
            if (septets > maxLength) {
                headerElt[1] = 0;
                maxLength = computeRemainUserDataLength(true, headerElt);
                ted.msgCount = (septets / maxLength) + 1;
                ted.codeUnitsRemaining = maxLength - (septets % maxLength);
            } else {
                ted.msgCount = 1;
                ted.codeUnitsRemaining = maxLength - septets;
            }
            ted.codeUnitSize = 1;
            ted.useSingleShift = true;
            ted.shiftLangId = language;
            Rlog.d(LOG_TAG, "Try Single Shift: " + language + " " + ted);
            return true;
        }
        septets = GsmAlphabet.countGsmSeptetsUsingTables(msgBody, true, language, language);
        if (septets != -1) {
            headerElt = new int[]{37, 36, 65535};
            maxLength = computeRemainUserDataLength(true, headerElt);
            ted.codeUnitCount = septets;
            if (septets > maxLength) {
                headerElt[2] = 0;
                maxLength = computeRemainUserDataLength(true, headerElt);
                ted.msgCount = (septets / maxLength) + 1;
                ted.codeUnitsRemaining = maxLength - (septets % maxLength);
            } else {
                ted.msgCount = 1;
                ted.codeUnitsRemaining = maxLength - septets;
            }
            ted.codeUnitSize = 1;
            ted.useLockingShift = true;
            ted.useSingleShift = true;
            ted.shiftLangId = language;
            Rlog.d(LOG_TAG, "Try Locking & Single Shift: " + language + " " + ted);
            return true;
        }
        Rlog.d(LOG_TAG, "Use UCS2" + language + " " + ted);
        return false;
    }

    private static int getCurrentSysLanguage() {
        String language = SystemProperties.get("persist.sys.language", null);
        if (language == null) {
            language = SystemProperties.get("ro.product.locale.language", null);
        }
        if (language.equals("tr")) {
            return -1;
        }
        return -1;
    }

    public static int computeRemainUserDataLength(boolean inSeptets, int[] headerElt) {
        int headerBytes = 0;
        for (int i : headerElt) {
            switch (i) {
                case 0:
                    headerBytes += 5;
                    break;
                case 36:
                    headerBytes += 3;
                    break;
                case 37:
                    headerBytes += 3;
                    break;
                default:
                    break;
            }
        }
        if (headerBytes != 0) {
            headerBytes++;
        }
        int count = 140 - headerBytes;
        if (inSeptets) {
            return (count * 8) / 7;
        }
        return count;
    }

    public static TextEncodingDetails calculateLength(CharSequence msgBody, boolean use7bitOnly, int encodingType) {
        CharSequence newMsgBody = null;
        if (Resources.getSystem().getBoolean(17957019)) {
            newMsgBody = Sms7BitEncodingTranslator.translate(msgBody);
        }
        if (TextUtils.isEmpty(newMsgBody)) {
            newMsgBody = msgBody;
        }
        TextEncodingDetails ted = GsmAlphabet.countGsmSeptets(newMsgBody, use7bitOnly);
        if (encodingType == 3) {
            Rlog.d(LOG_TAG, "input mode is unicode");
            ted = null;
        }
        if (ted != null) {
            return ted;
        }
        Rlog.d(LOG_TAG, "7-bit encoding fail");
        return SmsMessageBase.calcUnicodeEncodingDetails(newMsgBody);
    }

    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header, int encoding, int languageTable, int languageShiftTable, int validityPeriod) {
        if (message == null || destinationAddress == null) {
            return null;
        }
        byte[] userData;
        if (encoding == 0) {
            TextEncodingDetails ted = calculateLength(message, false);
            encoding = ted.codeUnitSize;
            languageTable = ted.languageTable;
            languageShiftTable = ted.languageShiftTable;
            if (encoding == 1 && !(languageTable == 0 && languageShiftTable == 0)) {
                SmsHeader smsHeader;
                if (header != null) {
                    smsHeader = SmsHeader.fromByteArray(header);
                    if (!(smsHeader.languageTable == languageTable && smsHeader.languageShiftTable == languageShiftTable)) {
                        Rlog.w(LOG_TAG, "Updating language table in SMS header: " + smsHeader.languageTable + " -> " + languageTable + ", " + smsHeader.languageShiftTable + " -> " + languageShiftTable);
                        smsHeader.languageTable = languageTable;
                        smsHeader.languageShiftTable = languageShiftTable;
                        header = SmsHeader.toByteArray(smsHeader);
                    }
                } else {
                    smsHeader = new SmsHeader();
                    smsHeader.languageTable = languageTable;
                    smsHeader.languageShiftTable = languageShiftTable;
                    header = SmsHeader.toByteArray(smsHeader);
                }
            }
        }
        SubmitPdu ret = new SubmitPdu();
        byte mtiByte = (byte) ((header != null ? 64 : 0) | 1);
        if (validityPeriod < 0 || validityPeriod > 255) {
            Rlog.d(LOG_TAG, "invalid VP: " + validityPeriod);
        } else {
            mtiByte = (byte) (mtiByte | 16);
        }
        ByteArrayOutputStream bo = getSubmitPduHead(scAddress, destinationAddress, mtiByte, statusReportRequested, ret);
        if (encoding == 1) {
            try {
                userData = GsmAlphabet.stringToGsm7BitPackedWithHeader(message, header, languageTable, languageShiftTable);
            } catch (EncodeException e) {
                try {
                    userData = encodeUCS2(message, header);
                    encoding = 3;
                } catch (UnsupportedEncodingException uex) {
                    Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex);
                    return null;
                }
            }
        }
        try {
            userData = encodeUCS2(message, header);
        } catch (UnsupportedEncodingException uex2) {
            Rlog.e(LOG_TAG, "Implausible UnsupportedEncodingException ", uex2);
            return null;
        }
        if (encoding == 1) {
            if ((userData[0] & 255) > 160) {
                Rlog.e(LOG_TAG, "Message too long (" + (userData[0] & 255) + " septets)");
                return null;
            }
            bo.write(0);
        } else if ((userData[0] & 255) > 140) {
            Rlog.e(LOG_TAG, "Message too long (" + (userData[0] & 255) + " bytes)");
            return null;
        } else {
            bo.write(8);
        }
        if (validityPeriod >= 0 && validityPeriod <= 255) {
            Rlog.d(LOG_TAG, "write validity period into pdu: " + validityPeriod);
            bo.write(validityPeriod);
        }
        bo.write(userData, 0, userData.length);
        ret.encodedMessage = bo.toByteArray();
        return ret;
    }

    public int getEncodingType() {
        return this.mEncodingType;
    }
}

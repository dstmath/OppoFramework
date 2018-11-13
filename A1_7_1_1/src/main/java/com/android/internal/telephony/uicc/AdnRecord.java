package com.android.internal.telephony.uicc;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.OppoGsmAlphabet;
import com.android.internal.telephony.cat.BipUtils;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
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
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public class AdnRecord implements Parcelable {
    static final int ADN_BCD_NUMBER_LENGTH = 0;
    static final int ADN_CAPABILITY_ID = 12;
    static final int ADN_DIALING_NUMBER_END = 11;
    static final int ADN_DIALING_NUMBER_START = 2;
    static final int ADN_EXTENSION_ID = 13;
    static final int ADN_TON_AND_NPI = 1;
    public static final Creator<AdnRecord> CREATOR = null;
    protected static final boolean DBG = false;
    static final int EXT_RECORD_LENGTH_BYTES = 13;
    static final int EXT_RECORD_TYPE_ADDITIONAL_DATA = 2;
    static final int EXT_RECORD_TYPE_MASK = 3;
    public static final int FOOTER_SIZE_BYTES = 14;
    static final String LOG_TAG = "AdnRecord";
    static final int MAX_EXT_CALLED_PARTY_LENGTH = 10;
    static final int MAX_NUMBER_SIZE_BYTES = 11;
    private static final String SIM_NUM_PATTERN = "[+]?[[0-9][*#pw,;]]+[[0-9][*#pw,;]]*";
    int EmailRecIndex;
    int EmailTagNumberInIap;
    int IapBufferSize;
    int aas;
    String additionalNumber;
    String additionalNumber2;
    String additionalNumber3;
    int emailefid;
    int emaillen;
    String grpIds;
    byte[] iap;
    int iapefid;
    String mAlphaTag;
    int mEfid;
    String[] mEmails;
    int mExtRecord;
    int mNameLength;
    String mNumber;
    int mNumberLength;
    int mRecordNumber;
    int mResult;
    String number2;
    int number2efid;
    int sfi;
    String sne;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.AdnRecord.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.AdnRecord.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.AdnRecord.<clinit>():void");
    }

    public AdnRecord(byte[] record) {
        this(0, 0, record);
    }

    public AdnRecord(int efid, int recordNumber, byte[] record) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.additionalNumber = null;
        this.additionalNumber2 = null;
        this.additionalNumber3 = null;
        this.mExtRecord = 255;
        this.aas = 0;
        this.sne = null;
        this.mResult = 1;
        this.number2 = null;
        this.number2efid = -1;
        this.emailefid = -1;
        this.iapefid = -1;
        this.emaillen = -1;
        this.sfi = -1;
        this.EmailRecIndex = -1;
        this.EmailTagNumberInIap = -1;
        this.IapBufferSize = -1;
        this.iap = null;
        this.mNameLength = 0;
        this.mNumberLength = 0;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        parseRecord(record);
    }

    public AdnRecord(String alphaTag, String number) {
        this(0, 0, alphaTag, number);
    }

    public AdnRecord(String alphaTag, String number, String anr) {
        this(0, 0, alphaTag, number, anr);
    }

    public AdnRecord(String alphaTag, String number, String[] emails) {
        this(0, 0, alphaTag, number, emails);
    }

    public AdnRecord(int efid, int recordNumber, String alphaTag, String number, String[] emails) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.additionalNumber = null;
        this.additionalNumber2 = null;
        this.additionalNumber3 = null;
        this.mExtRecord = 255;
        this.aas = 0;
        this.sne = null;
        this.mResult = 1;
        this.number2 = null;
        this.number2efid = -1;
        this.emailefid = -1;
        this.iapefid = -1;
        this.emaillen = -1;
        this.sfi = -1;
        this.EmailRecIndex = -1;
        this.EmailTagNumberInIap = -1;
        this.IapBufferSize = -1;
        this.iap = null;
        this.mNameLength = 0;
        this.mNumberLength = 0;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = emails;
        this.additionalNumber = UsimPBMemInfo.STRING_NOT_SET;
        this.additionalNumber2 = UsimPBMemInfo.STRING_NOT_SET;
        this.additionalNumber3 = UsimPBMemInfo.STRING_NOT_SET;
        this.grpIds = null;
    }

    public AdnRecord(int efid, int recordNumber, String alphaTag, String number) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.additionalNumber = null;
        this.additionalNumber2 = null;
        this.additionalNumber3 = null;
        this.mExtRecord = 255;
        this.aas = 0;
        this.sne = null;
        this.mResult = 1;
        this.number2 = null;
        this.number2efid = -1;
        this.emailefid = -1;
        this.iapefid = -1;
        this.emaillen = -1;
        this.sfi = -1;
        this.EmailRecIndex = -1;
        this.EmailTagNumberInIap = -1;
        this.IapBufferSize = -1;
        this.iap = null;
        this.mNameLength = 0;
        this.mNumberLength = 0;
        if (alphaTag == null) {
            alphaTag = UsimPBMemInfo.STRING_NOT_SET;
        }
        if (number == null) {
            number = UsimPBMemInfo.STRING_NOT_SET;
        }
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = null;
        this.additionalNumber = UsimPBMemInfo.STRING_NOT_SET;
        this.additionalNumber2 = UsimPBMemInfo.STRING_NOT_SET;
        this.additionalNumber3 = UsimPBMemInfo.STRING_NOT_SET;
        this.grpIds = null;
    }

    public AdnRecord(int efid, int recordNumber, String alphaTag, String number, String anr) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.additionalNumber = null;
        this.additionalNumber2 = null;
        this.additionalNumber3 = null;
        this.mExtRecord = 255;
        this.aas = 0;
        this.sne = null;
        this.mResult = 1;
        this.number2 = null;
        this.number2efid = -1;
        this.emailefid = -1;
        this.iapefid = -1;
        this.emaillen = -1;
        this.sfi = -1;
        this.EmailRecIndex = -1;
        this.EmailTagNumberInIap = -1;
        this.IapBufferSize = -1;
        this.iap = null;
        this.mNameLength = 0;
        this.mNumberLength = 0;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = null;
        this.additionalNumber = anr;
        this.additionalNumber2 = UsimPBMemInfo.STRING_NOT_SET;
        this.additionalNumber3 = UsimPBMemInfo.STRING_NOT_SET;
        this.grpIds = null;
    }

    public AdnRecord(int efid, int recordNumber, String alphaTag, String number, String anr, String[] emails, String grps) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.additionalNumber = null;
        this.additionalNumber2 = null;
        this.additionalNumber3 = null;
        this.mExtRecord = 255;
        this.aas = 0;
        this.sne = null;
        this.mResult = 1;
        this.number2 = null;
        this.number2efid = -1;
        this.emailefid = -1;
        this.iapefid = -1;
        this.emaillen = -1;
        this.sfi = -1;
        this.EmailRecIndex = -1;
        this.EmailTagNumberInIap = -1;
        this.IapBufferSize = -1;
        this.iap = null;
        this.mNameLength = 0;
        this.mNumberLength = 0;
        if (alphaTag == null) {
            alphaTag = UsimPBMemInfo.STRING_NOT_SET;
        }
        if (number == null) {
            number = UsimPBMemInfo.STRING_NOT_SET;
        }
        if (anr == null) {
            anr = UsimPBMemInfo.STRING_NOT_SET;
        }
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = emails;
        this.additionalNumber = anr;
        this.additionalNumber2 = UsimPBMemInfo.STRING_NOT_SET;
        this.additionalNumber3 = UsimPBMemInfo.STRING_NOT_SET;
        this.grpIds = grps;
    }

    public AdnRecord(int efid, int recordNumber, String alphaTag, String number, String anr, String anr2, String anr3, String[] emails, String grps) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.additionalNumber = null;
        this.additionalNumber2 = null;
        this.additionalNumber3 = null;
        this.mExtRecord = 255;
        this.aas = 0;
        this.sne = null;
        this.mResult = 1;
        this.number2 = null;
        this.number2efid = -1;
        this.emailefid = -1;
        this.iapefid = -1;
        this.emaillen = -1;
        this.sfi = -1;
        this.EmailRecIndex = -1;
        this.EmailTagNumberInIap = -1;
        this.IapBufferSize = -1;
        this.iap = null;
        this.mNameLength = 0;
        this.mNumberLength = 0;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = emails;
        this.additionalNumber = anr;
        this.additionalNumber2 = anr2;
        this.additionalNumber3 = anr3;
        this.grpIds = grps;
    }

    public int getRecId() {
        return this.mRecordNumber;
    }

    public String getAlphaTag() {
        return this.mAlphaTag;
    }

    public int getEfid() {
        return this.mEfid;
    }

    public String getNumber() {
        return this.mNumber;
    }

    public String getAdditionalNumber() {
        return this.additionalNumber;
    }

    public String getAdditionalNumber(int index) {
        if (index == 0) {
            return this.additionalNumber;
        }
        if (index == 1) {
            return this.additionalNumber2;
        }
        if (index == 2) {
            return this.additionalNumber3;
        }
        Rlog.e(LOG_TAG, "getAdditionalNumber Error:" + index);
        return null;
    }

    public int getAasIndex() {
        return this.aas;
    }

    public String getSne() {
        return this.sne;
    }

    public String[] getEmails() {
        return this.mEmails;
    }

    public String getGrpIds() {
        return this.grpIds;
    }

    public void setNumber(String number) {
        this.mNumber = number;
    }

    public void setAnr(String anr) {
        this.additionalNumber = anr;
    }

    public void setAnr(String anr, int index) {
        if (index == 0) {
            this.additionalNumber = anr;
        } else if (index == 1) {
            this.additionalNumber2 = anr;
        } else if (index == 2) {
            this.additionalNumber3 = anr;
        } else {
            Rlog.e(LOG_TAG, "setAnr Error:" + index);
        }
    }

    public void setAasIndex(int aas) {
        this.aas = aas;
    }

    public void setSne(String sne) {
        this.sne = sne;
    }

    public void setGrpIds(String grps) {
        this.grpIds = grps;
    }

    public void setEmails(String[] emails) {
        this.mEmails = emails;
    }

    public void setRecordIndex(int nIndex) {
        this.mRecordNumber = nIndex;
    }

    public String toString() {
        return "ADN Record:" + this.mRecordNumber + ",alphaTag:" + this.mAlphaTag + ",number:" + Rlog.pii(LOG_TAG, this.mNumber) + ",anr:" + Rlog.pii(LOG_TAG, this.additionalNumber) + ",anr2:" + Rlog.pii(LOG_TAG, this.additionalNumber2) + ",anr3:" + Rlog.pii(LOG_TAG, this.additionalNumber3) + ",aas:" + this.aas + ",emails:" + Rlog.pii(LOG_TAG, this.mEmails) + ",grpIds:" + this.grpIds + ",sne:" + this.sne;
    }

    public boolean isEmpty() {
        if (TextUtils.isEmpty(this.mAlphaTag) && TextUtils.isEmpty(this.mNumber) && TextUtils.isEmpty(this.additionalNumber) && this.mEmails == null) {
            return true;
        }
        return false;
    }

    public boolean hasExtendedRecord() {
        return (this.mExtRecord == 0 || this.mExtRecord == 255) ? false : true;
    }

    private static boolean stringCompareNullEqualsEmpty(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        Object s22;
        if (s1 == null) {
            s1 = UsimPBMemInfo.STRING_NOT_SET;
        }
        if (s22 == null) {
            s22 = UsimPBMemInfo.STRING_NOT_SET;
        }
        return s1.equals(s22);
    }

    public boolean isEqual(AdnRecord adn) {
        if (stringCompareNullEqualsEmpty(this.mAlphaTag, adn.mAlphaTag)) {
            return stringCompareNullEqualsEmpty(this.mNumber, adn.mNumber);
        }
        return false;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEfid);
        dest.writeInt(this.mRecordNumber);
        dest.writeString(this.mAlphaTag);
        dest.writeString(this.mNumber);
        dest.writeStringArray(this.mEmails);
        dest.writeString(this.additionalNumber);
        dest.writeString(this.additionalNumber2);
        dest.writeString(this.additionalNumber3);
        dest.writeString(this.grpIds);
        dest.writeInt(this.aas);
        dest.writeString(this.sne);
    }

    public byte[] buildAdnString(int recordSize) {
        Rlog.i(LOG_TAG, "in BuildAdnString");
        int footerOffset = recordSize - 14;
        int alphaIdLength = 0;
        byte[] adnString = new byte[recordSize];
        for (int i = 0; i < recordSize; i++) {
            adnString[i] = (byte) -1;
        }
        if (isPhoneNumberInvaild(this.mNumber)) {
            Rlog.w(LOG_TAG, "[buildAdnString] invaild number");
            this.mResult = -15;
            return null;
        }
        if (TextUtils.isEmpty(this.mNumber)) {
            Rlog.w(LOG_TAG, "[buildAdnString] Empty dialing number");
            this.mResult = 1;
        } else if (this.mNumber.length() > 20) {
            this.mResult = -1;
            Rlog.w(LOG_TAG, "[buildAdnString] Max length of dialing number is 20");
            return null;
        } else if (this.mAlphaTag == null || this.mAlphaTag.length() <= footerOffset) {
            this.mResult = 1;
            try {
                byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(this.mNumber);
                System.arraycopy(bcdNumber, 0, adnString, footerOffset + 1, bcdNumber.length);
                adnString[footerOffset + 0] = (byte) bcdNumber.length;
                adnString[footerOffset + 12] = (byte) -1;
                adnString[footerOffset + 13] = (byte) -1;
            } catch (RuntimeException e) {
                throw new RuntimeException("invalid number for BCD ", new CommandException(Error.INVALID_PARAMETER));
            }
        } else {
            this.mResult = -2;
            Rlog.w(LOG_TAG, "[buildAdnString] Max length of tag is " + footerOffset);
            return null;
        }
        if (!TextUtils.isEmpty(this.mAlphaTag)) {
            byte[] byteTag;
            if (isContainChineseChar(this.mAlphaTag)) {
                Rlog.i(LOG_TAG, "[buildAdnString] getBytes,alphaTag:" + this.mAlphaTag);
                try {
                    Rlog.i(LOG_TAG, "call getBytes");
                    byteTag = this.mAlphaTag.getBytes("utf-16be");
                    if (DBG) {
                        Rlog.d(LOG_TAG, "byteTag," + IccUtils.bytesToHexString(byteTag));
                    }
                    byte[] header = new byte[1];
                    header[0] = BipUtils.TCP_STATUS_ESTABLISHED;
                    System.arraycopy(header, 0, adnString, 0, 1);
                    if (byteTag.length > adnString.length - 1) {
                        this.mResult = -2;
                        Rlog.w(LOG_TAG, "[buildAdnString] after getBytes byteTag.length:" + byteTag.length + " adnString.length:" + adnString.length);
                        return null;
                    }
                    System.arraycopy(byteTag, 0, adnString, 1, byteTag.length);
                    alphaIdLength = byteTag.length + 1;
                    if (DBG) {
                        Rlog.d(LOG_TAG, "arrarString" + IccUtils.bytesToHexString(adnString));
                    }
                } catch (UnsupportedEncodingException e2) {
                    Rlog.w(LOG_TAG, "[buildAdnString] getBytes exception");
                    return null;
                }
            }
            byteTag = IccUtils.stringTo0x81(this.mAlphaTag);
            if (byteTag == null) {
                byteTag = IccUtils.stringTo0x82(this.mAlphaTag);
            }
            if (byteTag == null) {
                Rlog.i(LOG_TAG, "[buildAdnString] stringToGsm8BitPacked");
                byteTag = GsmAlphabet.stringToGsm8BitPacked(this.mAlphaTag);
                alphaIdLength = byteTag.length;
                if (alphaIdLength > adnString.length) {
                    this.mResult = -2;
                    Rlog.w(LOG_TAG, "[buildAdnString] after byteTag.length:" + byteTag.length + " adnString.length:" + adnString.length);
                    return null;
                }
            }
            alphaIdLength = byteTag.length;
            System.arraycopy(byteTag, 0, adnString, 0, byteTag.length);
        }
        if (this.mAlphaTag == null || alphaIdLength <= footerOffset) {
            return adnString;
        }
        this.mResult = -2;
        Rlog.w(LOG_TAG, "[buildAdnString] Max length of tag is " + footerOffset + ",alphaIdLength:" + alphaIdLength);
        return null;
    }

    public int getErrorNumber() {
        return this.mResult;
    }

    public void appendExtRecord(byte[] extRecord) {
        try {
            if (extRecord.length == 13 && (extRecord[0] & 3) == 2 && (extRecord[1] & 255) <= 10) {
                this.mNumber += PhoneNumberUtils.calledPartyBCDFragmentToString(extRecord, 2, extRecord[1] & 255);
            }
        } catch (RuntimeException ex) {
            Rlog.w(LOG_TAG, "Error parsing AdnRecord ext record", ex);
        }
    }

    private void parseRecord(byte[] record) {
        try {
            this.mAlphaTag = IccUtils.adnStringFieldToString(record, 0, record.length - 14);
            int footerOffset = record.length - 14;
            int numberLength = record[footerOffset] & 255;
            this.mNameLength = record.length - 14;
            this.mNumberLength = numberLength;
            if (numberLength > 11) {
                this.mNumber = UsimPBMemInfo.STRING_NOT_SET;
                return;
            }
            this.mNumber = PhoneNumberUtils.calledPartyBCDToString(record, footerOffset + 1, numberLength);
            this.mExtRecord = record[record.length - 1] & 255;
            this.mEmails = null;
            this.additionalNumber = UsimPBMemInfo.STRING_NOT_SET;
            this.additionalNumber2 = UsimPBMemInfo.STRING_NOT_SET;
            this.additionalNumber3 = UsimPBMemInfo.STRING_NOT_SET;
            this.grpIds = null;
        } catch (RuntimeException ex) {
            Rlog.w(LOG_TAG, "Error parsing AdnRecord", ex);
            this.mNumber = UsimPBMemInfo.STRING_NOT_SET;
            this.mAlphaTag = UsimPBMemInfo.STRING_NOT_SET;
            this.mEmails = null;
            this.additionalNumber = UsimPBMemInfo.STRING_NOT_SET;
            this.additionalNumber2 = UsimPBMemInfo.STRING_NOT_SET;
            this.additionalNumber3 = UsimPBMemInfo.STRING_NOT_SET;
            this.grpIds = null;
        }
    }

    private boolean isContainChineseChar(String alphTag) {
        int length = alphTag.length();
        for (int i = 0; i < length; i++) {
            if (Pattern.matches("[一-龥]", alphTag.substring(i, i + 1))) {
                return true;
            }
        }
        return false;
    }

    private boolean isPhoneNumberInvaild(String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            if (!Pattern.matches(SIM_NUM_PATTERN, PhoneNumberUtils.extractCLIRPortion(PhoneNumberUtils.stripSeparators(phoneNumber)))) {
                return true;
            }
        }
        return false;
    }

    public int oppoGetRecordNumber() {
        return this.mRecordNumber;
    }

    public void oppoSetRecordNumber(int recordIdx) {
        this.mRecordNumber = recordIdx;
    }

    public void setNumber2(String num) {
        if (num != null) {
            this.number2 = num;
        }
    }

    public String getNumber2() {
        return this.number2;
    }

    public void setEmailEfid(int efid) {
        this.emailefid = efid;
    }

    public void setIAPEfid(int efid) {
        this.iapefid = efid;
    }

    public void setNumber2Efid(int efid) {
        this.number2efid = efid;
    }

    public void setEmailLen(int len) {
        this.emaillen = len;
    }

    public int getEmailLen() {
        return this.emaillen;
    }

    public int getSFI() {
        return this.sfi;
    }

    public void setSFI(int sfi) {
        this.sfi = sfi;
    }

    public void setEmailRecIndex(int Index) {
        this.EmailRecIndex = Index;
    }

    public void setEmailTagNumberInIap(int num) {
        this.EmailTagNumberInIap = num;
    }

    public int getEmailTagNumberInIap(int num) {
        this.EmailTagNumberInIap = num;
        return num;
    }

    public void setIap(byte[] iap, int len) {
        if (len > 0 && len <= 4 && iap != null) {
            this.IapBufferSize = len;
            System.arraycopy(iap, 0, this.iap, 0, len);
        }
    }

    public byte[] oppobuildAdnString(int recordSize) {
        byte[] adnString = null;
        int footerOffset = recordSize - 14;
        int i;
        byte[] byteTag;
        if (this.mNumber == null || this.mNumber.equals(UsimPBMemInfo.STRING_NOT_SET)) {
            adnString = new byte[recordSize];
            for (i = 0; i < recordSize; i++) {
                adnString[i] = (byte) -1;
            }
            byteTag = null;
            if (!(this.mAlphaTag == null || this.mAlphaTag.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                byteTag = OppoGsmAlphabet.stringToGsm8BitOrUCSPackedForADN(this.mAlphaTag);
                if (byteTag != null && byteTag.length > footerOffset) {
                    return null;
                }
            }
            if (byteTag != null) {
                System.arraycopy(byteTag, 0, adnString, 0, byteTag.length);
            }
        } else if (this.mNumber.length() <= 20 && (this.mAlphaTag == null || this.mAlphaTag.length() <= footerOffset)) {
            byteTag = null;
            if (!(this.mAlphaTag == null || this.mAlphaTag.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                byteTag = OppoGsmAlphabet.stringToGsm8BitOrUCSPackedForADN(this.mAlphaTag);
                if (byteTag != null && byteTag.length > footerOffset) {
                    return null;
                }
            }
            adnString = new byte[recordSize];
            for (i = 0; i < recordSize; i++) {
                adnString[i] = (byte) -1;
            }
            byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(this.mNumber);
            System.arraycopy(bcdNumber, 0, adnString, footerOffset + 1, bcdNumber.length);
            adnString[footerOffset + 0] = (byte) bcdNumber.length;
            adnString[footerOffset + 12] = (byte) -1;
            adnString[footerOffset + 13] = (byte) -1;
            if (byteTag != null) {
                System.arraycopy(byteTag, 0, adnString, 0, byteTag.length);
            }
        }
        return adnString;
    }

    public int getAdnNameLength() {
        return this.mNameLength;
    }
}

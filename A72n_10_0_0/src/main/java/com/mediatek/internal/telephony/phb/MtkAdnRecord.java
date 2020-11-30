package com.mediatek.internal.telephony.phb;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.IccUtils;
import com.mediatek.internal.telephony.MtkPhoneNumberUtils;
import com.mediatek.internal.telephony.cat.BipUtils;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

public class MtkAdnRecord extends AdnRecord {
    public static final Parcelable.Creator<MtkAdnRecord> CREATOR = new Parcelable.Creator<MtkAdnRecord>() {
        /* class com.mediatek.internal.telephony.phb.MtkAdnRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkAdnRecord createFromParcel(Parcel source) {
            int efid = source.readInt();
            int recordNumber = source.readInt();
            String alphaTag = source.readString();
            String number = source.readString();
            String[] emails = source.readStringArray();
            String anr = source.readString();
            String anr2 = source.readString();
            String anr3 = source.readString();
            String grpIds = source.readString();
            int aas = source.readInt();
            String sne = source.readString();
            MtkAdnRecord adn = new MtkAdnRecord(efid, recordNumber, alphaTag, number, anr, anr2, anr3, emails, grpIds);
            adn.setAasIndex(aas);
            adn.setSne(sne);
            return adn;
        }

        @Override // android.os.Parcelable.Creator
        public MtkAdnRecord[] newArray(int size) {
            return new MtkAdnRecord[size];
        }
    };
    static final String LOG_TAG = "MtkAdnRecord";
    private static final String SIM_NUM_PATTERN = "[+]?[[0-9][*#pw,;]]+[[0-9][*#pw,;]]*";
    int mAas;
    String mAdditionalNumber;
    String mAdditionalNumber2;
    String mAdditionalNumber3;
    String mGrpIds;
    int mResult;
    String mSne;
    String number2;

    public MtkAdnRecord(byte[] record) {
        super(record);
        this.mAdditionalNumber = null;
        this.mAdditionalNumber2 = null;
        this.mAdditionalNumber3 = null;
        this.mAas = 0;
        this.mSne = null;
        this.mResult = 1;
        this.number2 = null;
    }

    public MtkAdnRecord(int efid, int recordNumber, byte[] record) {
        super(efid, recordNumber, record);
        this.mAdditionalNumber = null;
        this.mAdditionalNumber2 = null;
        this.mAdditionalNumber3 = null;
        this.mAas = 0;
        this.mSne = null;
        this.mResult = 1;
        this.number2 = null;
    }

    public MtkAdnRecord(String alphaTag, String number) {
        super(alphaTag, number);
        this.mAdditionalNumber = null;
        this.mAdditionalNumber2 = null;
        this.mAdditionalNumber3 = null;
        this.mAas = 0;
        this.mSne = null;
        this.mResult = 1;
        this.number2 = null;
    }

    public MtkAdnRecord(String alphaTag, String number, String anr) {
        this(0, 0, alphaTag, number, anr);
    }

    public MtkAdnRecord(String alphaTag, String number, String[] emails) {
        super(alphaTag, number, emails);
        this.mAdditionalNumber = null;
        this.mAdditionalNumber2 = null;
        this.mAdditionalNumber3 = null;
        this.mAas = 0;
        this.mSne = null;
        this.mResult = 1;
        this.number2 = null;
    }

    public MtkAdnRecord(int efid, int recordNumber, String alphaTag, String number, String[] emails) {
        super(efid, recordNumber, alphaTag, number, emails);
        this.mAdditionalNumber = null;
        this.mAdditionalNumber2 = null;
        this.mAdditionalNumber3 = null;
        this.mAas = 0;
        this.mSne = null;
        this.mResult = 1;
        this.number2 = null;
        this.mAdditionalNumber = "";
        this.mAdditionalNumber2 = "";
        this.mAdditionalNumber3 = "";
        this.mGrpIds = null;
    }

    public MtkAdnRecord(int efid, int recordNumber, String alphaTag, String number) {
        super(efid, recordNumber, alphaTag, number, (String[]) null);
        this.mAdditionalNumber = null;
        this.mAdditionalNumber2 = null;
        this.mAdditionalNumber3 = null;
        this.mAas = 0;
        this.mSne = null;
        this.mResult = 1;
        this.number2 = null;
        this.mAdditionalNumber = "";
        this.mAdditionalNumber2 = "";
        this.mAdditionalNumber3 = "";
        this.mGrpIds = null;
        if (alphaTag == null) {
            this.mAlphaTag = "";
        }
        if (number == null) {
            this.mNumber = "";
        }
    }

    public MtkAdnRecord(int efid, int recordNumber, String alphaTag, String number, String anr) {
        super(efid, recordNumber, alphaTag, number, (String[]) null);
        this.mAdditionalNumber = null;
        this.mAdditionalNumber2 = null;
        this.mAdditionalNumber3 = null;
        this.mAas = 0;
        this.mSne = null;
        this.mResult = 1;
        this.number2 = null;
        this.mAdditionalNumber = anr;
        this.mAdditionalNumber2 = "";
        this.mAdditionalNumber3 = "";
        this.mGrpIds = null;
    }

    public MtkAdnRecord(int efid, int recordNumber, String alphaTag, String number, String anr, String[] emails, String grps) {
        super(efid, recordNumber, alphaTag, number, emails);
        this.mAdditionalNumber = null;
        this.mAdditionalNumber2 = null;
        this.mAdditionalNumber3 = null;
        this.mAas = 0;
        this.mSne = null;
        this.mResult = 1;
        this.number2 = null;
        this.mAdditionalNumber = anr;
        this.mAdditionalNumber2 = "";
        this.mAdditionalNumber3 = "";
        this.mGrpIds = grps;
        if (alphaTag == null) {
            this.mAlphaTag = "";
        }
        if (number == null) {
            this.mNumber = "";
        }
        if (anr == null) {
            this.mAdditionalNumber = "";
        }
    }

    public MtkAdnRecord(int efid, int recordNumber, String alphaTag, String number, String anr, String anr2, String anr3, String[] emails, String grps) {
        super(efid, recordNumber, alphaTag, number, emails);
        this.mAdditionalNumber = null;
        this.mAdditionalNumber2 = null;
        this.mAdditionalNumber3 = null;
        this.mAas = 0;
        this.mSne = null;
        this.mResult = 1;
        this.number2 = null;
        this.mAdditionalNumber = anr;
        this.mAdditionalNumber2 = anr2;
        this.mAdditionalNumber3 = anr3;
        this.mGrpIds = grps;
    }

    public String getAdditionalNumber() {
        return this.mAdditionalNumber;
    }

    public String getAdditionalNumber(int index) {
        if (index == 0) {
            return this.mAdditionalNumber;
        }
        if (index == 1) {
            return this.mAdditionalNumber2;
        }
        if (index == 2) {
            return this.mAdditionalNumber3;
        }
        Rlog.e(LOG_TAG, "getAdditionalNumber Error:" + index);
        return null;
    }

    public int getAasIndex() {
        return this.mAas;
    }

    public String getSne() {
        return this.mSne;
    }

    public String getGrpIds() {
        return this.mGrpIds;
    }

    public void setAnr(String anr) {
        this.mAdditionalNumber = anr;
    }

    public void setAnr(String anr, int index) {
        if (index == 0) {
            this.mAdditionalNumber = anr;
        } else if (index == 1) {
            this.mAdditionalNumber2 = anr;
        } else if (index == 2) {
            this.mAdditionalNumber3 = anr;
        } else {
            Rlog.e(LOG_TAG, "setAnr Error:" + index);
        }
    }

    public void setAasIndex(int aas) {
        this.mAas = aas;
    }

    public void setSne(String sne) {
        this.mSne = sne;
    }

    public void setGrpIds(String grps) {
        this.mGrpIds = grps;
    }

    public void setRecordIndex(int nIndex) {
        this.mRecordNumber = nIndex;
    }

    private String getMaskString(String str) {
        if (str == null) {
            return "null";
        }
        if (str.length() <= 2) {
            return "xx";
        }
        return str.substring(0, str.length() >> 1) + "xxxxx";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ADN Record:");
        sb.append(this.mRecordNumber);
        sb.append(",alphaTag:");
        sb.append(getMaskString(this.mAlphaTag));
        sb.append(",number:");
        sb.append(getMaskString(this.mNumber));
        sb.append(",aas:");
        sb.append(this.mAas);
        sb.append(",emails:");
        sb.append(this.mEmails == null ? "null" : getMaskString(this.mEmails[0]));
        sb.append(",grpIds:");
        sb.append(this.mGrpIds);
        sb.append(",sne:");
        sb.append(this.mSne);
        return sb.toString();
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(this.mAlphaTag) && TextUtils.isEmpty(this.mNumber) && TextUtils.isEmpty(this.mAdditionalNumber) && this.mEmails == null;
    }

    public boolean isEqual(MtkAdnRecord adn) {
        return stringCompareNullEqualsEmpty(this.mAlphaTag, adn.mAlphaTag) && stringCompareNullEqualsEmpty(this.mNumber, adn.mNumber);
    }

    public void writeToParcel(Parcel dest, int flags) {
        MtkAdnRecord.super.writeToParcel(dest, flags);
        dest.writeString(this.mAdditionalNumber);
        dest.writeString(this.mAdditionalNumber2);
        dest.writeString(this.mAdditionalNumber3);
        dest.writeString(this.mGrpIds);
        dest.writeInt(this.mAas);
        dest.writeString(this.mSne);
    }

    public byte[] buildAdnString(int recordSize) {
        Rlog.i(LOG_TAG, "in BuildAdnString");
        int footerOffset = recordSize - 14;
        int alphaIdLength = 0;
        byte[] adnString = new byte[recordSize];
        for (int i = 0; i < recordSize; i++) {
            adnString[i] = -1;
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
                byte[] bcdNumber = MtkPhoneNumberUtils.numberToCalledPartyBCD(this.mNumber);
                if (bcdNumber == null) {
                    return null;
                }
                System.arraycopy(bcdNumber, 0, adnString, footerOffset + 1, bcdNumber.length);
                adnString[footerOffset + 0] = (byte) bcdNumber.length;
                adnString[footerOffset + 12] = -1;
                adnString[footerOffset + 13] = -1;
            } catch (RuntimeException e) {
                throw new RuntimeException("invalid number for BCD ", new CommandException(CommandException.Error.OEM_ERROR_12));
            }
        } else {
            this.mResult = -2;
            Rlog.w(LOG_TAG, "[buildAdnString] Max length of tag is " + footerOffset);
            return null;
        }
        if (!TextUtils.isEmpty(this.mAlphaTag)) {
            if (isContainChineseChar(this.mAlphaTag)) {
                Rlog.i(LOG_TAG, "[buildAdnString] getBytes,alphaTag:" + this.mAlphaTag);
                try {
                    Rlog.i(LOG_TAG, "call getBytes");
                    byte[] byteTag = this.mAlphaTag.getBytes("utf-16be");
                    Rlog.i(LOG_TAG, "byteTag," + IccUtils.bytesToHexString(byteTag));
                    System.arraycopy(new byte[]{BipUtils.TCP_STATUS_ESTABLISHED}, 0, adnString, 0, 1);
                    if (byteTag.length > adnString.length - 1) {
                        this.mResult = -2;
                        Rlog.w(LOG_TAG, "[buildAdnString] after getBytes byteTag.length:" + byteTag.length + " adnString.length:" + adnString.length);
                        return null;
                    }
                    System.arraycopy(byteTag, 0, adnString, 1, byteTag.length);
                    alphaIdLength = byteTag.length + 1;
                    Rlog.i(LOG_TAG, "arrarString" + IccUtils.bytesToHexString(adnString));
                } catch (UnsupportedEncodingException e2) {
                    Rlog.w(LOG_TAG, "[buildAdnString] getBytes exception");
                    return null;
                }
            } else {
                Rlog.i(LOG_TAG, "[buildAdnString] stringToGsm8BitPacked");
                byte[] byteTag2 = GsmAlphabet.stringToGsm8BitPacked(this.mAlphaTag);
                alphaIdLength = byteTag2.length;
                if (alphaIdLength > adnString.length) {
                    this.mResult = -2;
                    Rlog.w(LOG_TAG, "[buildAdnString] after stringToGsm8BitPacked byteTag.length:" + byteTag2.length + " adnString.length:" + adnString.length);
                    return null;
                }
                System.arraycopy(byteTag2, 0, adnString, 0, byteTag2.length);
            }
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
            if (extRecord.length == 13 && (extRecord[0] & 3) == 2 && (extRecord[1] & PplMessageManager.Type.INVALID) <= 10) {
                this.mNumber += MtkPhoneNumberUtils.calledPartyBCDFragmentToString(extRecord, 2, extRecord[1] & PplMessageManager.Type.INVALID);
            }
        } catch (RuntimeException ex) {
            Rlog.w(LOG_TAG, "Error parsing AdnRecordEx ext record", ex);
        }
    }

    private void parseRecord(byte[] record) {
        try {
            this.mAlphaTag = IccUtils.adnStringFieldToString(record, 0, record.length - 14);
            int footerOffset = record.length - 14;
            int numberLength = record[footerOffset] & PplMessageManager.Type.INVALID;
            if (numberLength > 11) {
                this.mNumber = "";
                return;
            }
            this.mNumber = MtkPhoneNumberUtils.calledPartyBCDToString(record, footerOffset + 1, numberLength);
            this.mExtRecord = record[record.length - 1] & PplMessageManager.Type.INVALID;
            this.mEmails = null;
            this.mAdditionalNumber = "";
            this.mAdditionalNumber2 = "";
            this.mAdditionalNumber3 = "";
            this.mGrpIds = null;
        } catch (RuntimeException ex) {
            Rlog.w(LOG_TAG, "Error parsing AdnRecordEx", ex);
            this.mNumber = "";
            this.mAlphaTag = "";
            this.mEmails = null;
            this.mAdditionalNumber = "";
            this.mAdditionalNumber2 = "";
            this.mAdditionalNumber3 = "";
            this.mGrpIds = null;
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
        if (TextUtils.isEmpty(phoneNumber) || Pattern.matches(SIM_NUM_PATTERN, MtkPhoneNumberUtils.extractCLIRPortion(MtkPhoneNumberUtils.stripSeparators(phoneNumber)))) {
            return false;
        }
        return true;
    }

    public void setNumber2(String num) {
        if (num != null) {
            this.number2 = num;
        }
    }
}

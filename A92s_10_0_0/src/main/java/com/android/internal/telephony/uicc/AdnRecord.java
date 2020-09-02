package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.PhoneConfigurationManager;
import java.util.Arrays;

public class AdnRecord implements Parcelable {
    protected static final int ADN_BCD_NUMBER_LENGTH = 0;
    protected static final int ADN_CAPABILITY_ID = 12;
    protected static final int ADN_DIALING_NUMBER_END = 11;
    protected static final int ADN_DIALING_NUMBER_START = 2;
    protected static final int ADN_EXTENSION_ID = 13;
    protected static final int ADN_TON_AND_NPI = 1;
    @UnsupportedAppUsage
    public static final Parcelable.Creator<AdnRecord> CREATOR = new Parcelable.Creator<AdnRecord>() {
        /* class com.android.internal.telephony.uicc.AdnRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AdnRecord createFromParcel(Parcel source) {
            return new AdnRecord(source.readInt(), source.readInt(), source.readString(), source.readString(), source.readStringArray());
        }

        @Override // android.os.Parcelable.Creator
        public AdnRecord[] newArray(int size) {
            return new AdnRecord[size];
        }
    };
    protected static final int EXT_RECORD_LENGTH_BYTES = 13;
    protected static final int EXT_RECORD_TYPE_ADDITIONAL_DATA = 2;
    protected static final int EXT_RECORD_TYPE_MASK = 3;
    public static final int FOOTER_SIZE_BYTES = 14;
    static final String LOG_TAG = "AdnRecord";
    protected static final int MAX_EXT_CALLED_PARTY_LENGTH = 10;
    protected static final int MAX_NUMBER_SIZE_BYTES = 11;
    @UnsupportedAppUsage
    public String mAlphaTag;
    @UnsupportedAppUsage
    public int mEfid;
    @UnsupportedAppUsage
    public String[] mEmails;
    @UnsupportedAppUsage
    public int mExtRecord;
    @UnsupportedAppUsage
    public String mNumber;
    @UnsupportedAppUsage
    public int mRecordNumber;

    @UnsupportedAppUsage
    public AdnRecord(byte[] record) {
        this(0, 0, record);
    }

    @UnsupportedAppUsage
    public AdnRecord(int efid, int recordNumber, byte[] record) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.mExtRecord = 255;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        parseRecord(record);
    }

    @UnsupportedAppUsage
    public AdnRecord(String alphaTag, String number) {
        this(0, 0, alphaTag, number);
    }

    @UnsupportedAppUsage
    public AdnRecord(String alphaTag, String number, String[] emails) {
        this(0, 0, alphaTag, number, emails);
    }

    @UnsupportedAppUsage
    public AdnRecord(int efid, int recordNumber, String alphaTag, String number, String[] emails) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.mExtRecord = 255;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = emails;
    }

    @UnsupportedAppUsage
    public AdnRecord(int efid, int recordNumber, String alphaTag, String number) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.mExtRecord = 255;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = null;
    }

    public String getAlphaTag() {
        return this.mAlphaTag;
    }

    public int getEfid() {
        return this.mEfid;
    }

    public int getRecId() {
        return this.mRecordNumber;
    }

    @UnsupportedAppUsage
    public String getNumber() {
        return this.mNumber;
    }

    public void setNumber(String number) {
        this.mNumber = number;
    }

    @UnsupportedAppUsage
    public String[] getEmails() {
        return this.mEmails;
    }

    @UnsupportedAppUsage
    public void setEmails(String[] emails) {
        this.mEmails = emails;
    }

    public String toString() {
        return "ADN Record '" + this.mAlphaTag + "' '" + Rlog.pii(LOG_TAG, this.mNumber) + " " + Rlog.pii(LOG_TAG, this.mEmails) + "'";
    }

    @UnsupportedAppUsage
    public boolean isEmpty() {
        return TextUtils.isEmpty(this.mAlphaTag) && TextUtils.isEmpty(this.mNumber) && this.mEmails == null;
    }

    public boolean hasExtendedRecord() {
        int i = this.mExtRecord;
        return (i == 0 || i == 255) ? false : true;
    }

    protected static boolean stringCompareNullEqualsEmpty(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null) {
            s1 = PhoneConfigurationManager.SSSS;
        }
        if (s2 == null) {
            s2 = PhoneConfigurationManager.SSSS;
        }
        return s1.equals(s2);
    }

    public boolean isEqual(AdnRecord adn) {
        return stringCompareNullEqualsEmpty(this.mAlphaTag, adn.mAlphaTag) && stringCompareNullEqualsEmpty(this.mNumber, adn.mNumber) && Arrays.equals(this.mEmails, adn.mEmails);
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
    }

    @UnsupportedAppUsage
    public byte[] buildAdnString(int recordSize) {
        byte[] byteTag;
        int footerOffset = recordSize - 14;
        byte[] adnString = new byte[recordSize];
        for (int i = 0; i < recordSize; i++) {
            adnString[i] = -1;
        }
        if (TextUtils.isEmpty(this.mNumber)) {
            Rlog.w(LOG_TAG, "[buildAdnString] Empty dialing number");
            return adnString;
        } else if (this.mNumber.length() > 20) {
            Rlog.w(LOG_TAG, "[buildAdnString] Max length of dialing number is 20");
            return null;
        } else {
            if (!TextUtils.isEmpty(this.mAlphaTag)) {
                byteTag = GsmAlphabet.stringToGsm8BitPacked(this.mAlphaTag);
            } else {
                byteTag = new byte[0];
            }
            if (byteTag.length > footerOffset) {
                Rlog.w(LOG_TAG, "[buildAdnString] Max length of tag is " + footerOffset);
                return null;
            }
            byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(this.mNumber, 1);
            System.arraycopy(bcdNumber, 0, adnString, footerOffset + 1, bcdNumber.length);
            adnString[footerOffset + 0] = (byte) bcdNumber.length;
            adnString[footerOffset + 12] = -1;
            adnString[footerOffset + 13] = -1;
            if (byteTag.length > 0) {
                System.arraycopy(byteTag, 0, adnString, 0, byteTag.length);
            }
            return adnString;
        }
    }

    public void appendExtRecord(byte[] extRecord) {
        try {
            if (extRecord.length == 13 && (extRecord[0] & 3) == 2 && (extRecord[1] & 255) <= 10) {
                this.mNumber += PhoneNumberUtils.calledPartyBCDFragmentToString(extRecord, 2, extRecord[1] & 255, 1);
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
            if (numberLength > 11) {
                this.mNumber = PhoneConfigurationManager.SSSS;
                return;
            }
            this.mNumber = PhoneNumberUtils.calledPartyBCDToString(record, footerOffset + 1, numberLength, 1);
            this.mExtRecord = record[record.length - 1] & 255;
            this.mEmails = null;
        } catch (RuntimeException ex) {
            Rlog.w(LOG_TAG, "Error parsing AdnRecord", ex);
            this.mNumber = PhoneConfigurationManager.SSSS;
            this.mAlphaTag = PhoneConfigurationManager.SSSS;
            this.mEmails = null;
        }
    }
}

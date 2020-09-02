package mediatek.telephony;

import android.os.Bundle;
import android.os.Parcel;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import com.mediatek.internal.telephony.MtkPhoneNumberFormatUtil;
import java.util.Arrays;

public class MtkServiceState extends ServiceState {
    static final boolean DBG = false;
    static final String LOG_TAG = "MTKSS";
    public static final int REGISTRATION_STATE_NOT_REGISTERED_AND_NOT_SEARCHING_EMERGENCY_CALL_ENABLED = 10;
    public static final int REGISTRATION_STATE_NOT_REGISTERED_AND_SEARCHING_EMERGENCY_CALL_ENABLED = 12;
    public static final int REGISTRATION_STATE_REGISTRATION_DENIED_EMERGENCY_CALL_ENABLED = 13;
    public static final int REGISTRATION_STATE_UNKNOWN_EMERGENCY_CALL_ENABLED = 14;
    public static final int RIL_RADIO_TECHNOLOGY_DC_DPA = 133;
    public static final int RIL_RADIO_TECHNOLOGY_DC_HSDPAP = 135;
    public static final int RIL_RADIO_TECHNOLOGY_DC_HSDPAP_DPA = 137;
    public static final int RIL_RADIO_TECHNOLOGY_DC_HSDPAP_UPA = 136;
    public static final int RIL_RADIO_TECHNOLOGY_DC_HSPAP = 138;
    public static final int RIL_RADIO_TECHNOLOGY_DC_UPA = 134;
    public static final int RIL_RADIO_TECHNOLOGY_HSDPAP = 129;
    public static final int RIL_RADIO_TECHNOLOGY_HSDPAP_UPA = 130;
    public static final int RIL_RADIO_TECHNOLOGY_HSUPAP = 131;
    public static final int RIL_RADIO_TECHNOLOGY_HSUPAP_DPA = 132;
    public static final int RIL_RADIO_TECHNOLOGY_MTK = 128;
    private int mCellularDataNetworkType = 0;
    private int mDataRejectCause = -1;
    private int mProprietaryDataRadioTechnology;
    private int mRilCellularDataRegState = 0;
    private int mRilDataRegState = 0;
    private int mRilVoiceRegState = 0;
    private int mVoiceRejectCause = -1;

    public static ServiceState newFromBundle(Bundle m) {
        MtkServiceState ret = new MtkServiceState();
        ret.setFromNotifierBundle(m);
        return ret;
    }

    public MtkServiceState() {
        setStateOutOfService();
    }

    public MtkServiceState(MtkServiceState s) {
        copyFrom(s);
    }

    public MtkServiceState(ServiceState s) {
        copyFrom((MtkServiceState) s);
    }

    /* access modifiers changed from: protected */
    public void copyFrom(MtkServiceState s) {
        int[] iArr;
        this.mVoiceRegState = s.mVoiceRegState;
        this.mDataRegState = s.mDataRegState;
        this.mVoiceOperatorAlphaLong = s.mVoiceOperatorAlphaLong;
        this.mVoiceOperatorAlphaShort = s.mVoiceOperatorAlphaShort;
        this.mVoiceOperatorNumeric = s.mVoiceOperatorNumeric;
        this.mDataOperatorAlphaLong = s.mDataOperatorAlphaLong;
        this.mDataOperatorAlphaShort = s.mDataOperatorAlphaShort;
        this.mDataOperatorNumeric = s.mDataOperatorNumeric;
        this.mIsManualNetworkSelection = s.mIsManualNetworkSelection;
        this.mCssIndicator = s.mCssIndicator;
        this.mNetworkId = s.mNetworkId;
        this.mSystemId = s.mSystemId;
        this.mCdmaRoamingIndicator = s.mCdmaRoamingIndicator;
        this.mCdmaDefaultRoamingIndicator = s.mCdmaDefaultRoamingIndicator;
        this.mCdmaEriIconIndex = s.mCdmaEriIconIndex;
        this.mCdmaEriIconMode = s.mCdmaEriIconMode;
        this.mIsEmergencyOnly = s.mIsEmergencyOnly;
        this.mChannelNumber = s.mChannelNumber;
        if (s.mCellBandwidths == null) {
            iArr = null;
        } else {
            iArr = Arrays.copyOf(s.mCellBandwidths, s.mCellBandwidths.length);
        }
        this.mCellBandwidths = iArr;
        this.mLteEarfcnRsrpBoost = s.mLteEarfcnRsrpBoost;
        synchronized (this.mNetworkRegistrationInfos) {
            this.mNetworkRegistrationInfos.clear();
            this.mNetworkRegistrationInfos.addAll(s.getNetworkRegistrationInfoList());
        }
        this.mNrFrequencyRange = s.mNrFrequencyRange;
        this.mOperatorAlphaLongRaw = s.mOperatorAlphaLongRaw;
        this.mOperatorAlphaShortRaw = s.mOperatorAlphaShortRaw;
        this.mIsIwlanPreferred = s.mIsIwlanPreferred;
        this.mRilVoiceRegState = s.mRilVoiceRegState;
        this.mRilDataRegState = s.mRilDataRegState;
        this.mProprietaryDataRadioTechnology = s.mProprietaryDataRadioTechnology;
        this.mVoiceRejectCause = s.mVoiceRejectCause;
        this.mDataRejectCause = s.mDataRejectCause;
        this.mRilCellularDataRegState = s.mRilCellularDataRegState;
        this.mCellularDataNetworkType = s.mCellularDataNetworkType;
    }

    public MtkServiceState(Parcel in) {
        boolean z = false;
        this.mVoiceRegState = in.readInt();
        this.mDataRegState = in.readInt();
        this.mVoiceOperatorAlphaLong = in.readString();
        this.mVoiceOperatorAlphaShort = in.readString();
        this.mVoiceOperatorNumeric = in.readString();
        this.mDataOperatorAlphaLong = in.readString();
        this.mDataOperatorAlphaShort = in.readString();
        this.mDataOperatorNumeric = in.readString();
        this.mIsManualNetworkSelection = in.readInt() != 0;
        this.mCssIndicator = in.readInt() != 0;
        this.mNetworkId = in.readInt();
        this.mSystemId = in.readInt();
        this.mCdmaRoamingIndicator = in.readInt();
        this.mCdmaDefaultRoamingIndicator = in.readInt();
        this.mCdmaEriIconIndex = in.readInt();
        this.mCdmaEriIconMode = in.readInt();
        this.mIsEmergencyOnly = in.readInt() != 0 ? true : z;
        this.mLteEarfcnRsrpBoost = in.readInt();
        synchronized (this.mNetworkRegistrationInfos) {
            in.readList(this.mNetworkRegistrationInfos, NetworkRegistrationInfo.class.getClassLoader());
        }
        this.mChannelNumber = in.readInt();
        this.mCellBandwidths = in.createIntArray();
        this.mNrFrequencyRange = in.readInt();
        this.mOperatorAlphaLongRaw = in.readString();
        this.mOperatorAlphaShortRaw = in.readString();
        this.mIsIwlanPreferred = in.readBoolean();
        this.mRilVoiceRegState = in.readInt();
        this.mRilDataRegState = in.readInt();
        this.mProprietaryDataRadioTechnology = in.readInt();
        this.mVoiceRejectCause = in.readInt();
        this.mDataRejectCause = in.readInt();
        this.mRilCellularDataRegState = in.readInt();
        this.mCellularDataNetworkType = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mVoiceRegState);
        out.writeInt(this.mDataRegState);
        out.writeString(this.mVoiceOperatorAlphaLong);
        out.writeString(this.mVoiceOperatorAlphaShort);
        out.writeString(this.mVoiceOperatorNumeric);
        out.writeString(this.mDataOperatorAlphaLong);
        out.writeString(this.mDataOperatorAlphaShort);
        out.writeString(this.mDataOperatorNumeric);
        out.writeInt(this.mIsManualNetworkSelection ? 1 : 0);
        out.writeInt(this.mCssIndicator ? 1 : 0);
        out.writeInt(this.mNetworkId);
        out.writeInt(this.mSystemId);
        out.writeInt(this.mCdmaRoamingIndicator);
        out.writeInt(this.mCdmaDefaultRoamingIndicator);
        out.writeInt(this.mCdmaEriIconIndex);
        out.writeInt(this.mCdmaEriIconMode);
        out.writeInt(this.mIsEmergencyOnly ? 1 : 0);
        out.writeInt(this.mLteEarfcnRsrpBoost);
        synchronized (this.mNetworkRegistrationInfos) {
            out.writeList(this.mNetworkRegistrationInfos);
        }
        out.writeInt(this.mChannelNumber);
        out.writeIntArray(this.mCellBandwidths);
        out.writeInt(this.mNrFrequencyRange);
        out.writeString(this.mOperatorAlphaLongRaw);
        out.writeString(this.mOperatorAlphaShortRaw);
        out.writeBoolean(this.mIsIwlanPreferred);
        out.writeInt(this.mRilVoiceRegState);
        out.writeInt(this.mRilDataRegState);
        out.writeInt(this.mProprietaryDataRadioTechnology);
        out.writeInt(this.mVoiceRejectCause);
        out.writeInt(this.mDataRejectCause);
        out.writeInt(this.mRilCellularDataRegState);
        out.writeInt(this.mCellularDataNetworkType);
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            MtkServiceState s = (MtkServiceState) o;
            if (o == null) {
                return false;
            }
            synchronized (this.mNetworkRegistrationInfos) {
                if (this.mVoiceRegState == s.mVoiceRegState && this.mDataRegState == s.mDataRegState && this.mIsManualNetworkSelection == s.mIsManualNetworkSelection && this.mChannelNumber == s.mChannelNumber && Arrays.equals(this.mCellBandwidths, s.mCellBandwidths) && equalsHandlesNulls(this.mVoiceOperatorAlphaLong, s.mVoiceOperatorAlphaLong) && equalsHandlesNulls(this.mVoiceOperatorAlphaShort, s.mVoiceOperatorAlphaShort) && equalsHandlesNulls(this.mVoiceOperatorNumeric, s.mVoiceOperatorNumeric) && equalsHandlesNulls(this.mDataOperatorAlphaLong, s.mDataOperatorAlphaLong) && equalsHandlesNulls(this.mDataOperatorAlphaShort, s.mDataOperatorAlphaShort) && equalsHandlesNulls(this.mDataOperatorNumeric, s.mDataOperatorNumeric) && equalsHandlesNulls(Boolean.valueOf(this.mCssIndicator), Boolean.valueOf(s.mCssIndicator)) && equalsHandlesNulls(Integer.valueOf(this.mNetworkId), Integer.valueOf(s.mNetworkId)) && equalsHandlesNulls(Integer.valueOf(this.mSystemId), Integer.valueOf(s.mSystemId)) && equalsHandlesNulls(Integer.valueOf(this.mCdmaRoamingIndicator), Integer.valueOf(s.mCdmaRoamingIndicator)) && equalsHandlesNulls(Integer.valueOf(this.mCdmaDefaultRoamingIndicator), Integer.valueOf(s.mCdmaDefaultRoamingIndicator)) && this.mIsEmergencyOnly == s.mIsEmergencyOnly && equalsHandlesNulls(this.mOperatorAlphaLongRaw, s.mOperatorAlphaLongRaw) && equalsHandlesNulls(this.mOperatorAlphaShortRaw, s.mOperatorAlphaShortRaw) && this.mNetworkRegistrationInfos.size() == s.mNetworkRegistrationInfos.size() && this.mNetworkRegistrationInfos.containsAll(s.mNetworkRegistrationInfos) && this.mNrFrequencyRange == s.mNrFrequencyRange && this.mIsIwlanPreferred == s.mIsIwlanPreferred && this.mRilVoiceRegState == s.mRilVoiceRegState && this.mRilDataRegState == s.mRilDataRegState && equalsHandlesNulls(Integer.valueOf(this.mProprietaryDataRadioTechnology), Integer.valueOf(s.mProprietaryDataRadioTechnology)) && this.mVoiceRejectCause == s.mVoiceRejectCause && this.mDataRejectCause == s.mDataRejectCause && this.mRilCellularDataRegState == s.mRilCellularDataRegState && this.mCellularDataNetworkType == s.mCellularDataNetworkType) {
                    z = true;
                }
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        String sb;
        synchronized (this.mNetworkRegistrationInfos) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("{mVoiceRegState=");
            sb2.append(this.mVoiceRegState);
            sb2.append("(" + rilServiceStateToString(this.mVoiceRegState) + ")");
            sb2.append(", mDataRegState=");
            sb2.append(this.mDataRegState);
            sb2.append("(" + rilServiceStateToString(this.mDataRegState) + ")");
            sb2.append(", mChannelNumber=");
            sb2.append(this.mChannelNumber);
            sb2.append(", duplexMode()=");
            sb2.append(getDuplexMode());
            sb2.append(", mCellBandwidths=");
            sb2.append(Arrays.toString(this.mCellBandwidths));
            sb2.append(", mVoiceOperatorAlphaLong=");
            sb2.append(this.mVoiceOperatorAlphaLong);
            sb2.append(", mVoiceOperatorAlphaShort=");
            sb2.append(this.mVoiceOperatorAlphaShort);
            sb2.append(", mDataOperatorAlphaLong=");
            sb2.append(this.mDataOperatorAlphaLong);
            sb2.append(", mDataOperatorAlphaShort=");
            sb2.append(this.mDataOperatorAlphaShort);
            sb2.append(", isManualNetworkSelection=");
            sb2.append(this.mIsManualNetworkSelection);
            sb2.append(this.mIsManualNetworkSelection ? "(manual)" : "(automatic)");
            sb2.append(", getRilVoiceRadioTechnology=");
            sb2.append(getRilVoiceRadioTechnology());
            sb2.append("(" + rilRadioTechnologyToString(getRilVoiceRadioTechnology()) + ")");
            sb2.append(", getRilDataRadioTechnology=");
            sb2.append(getRilDataRadioTechnology());
            sb2.append("(" + rilRadioTechnologyToString(getRilDataRadioTechnology()) + ")");
            sb2.append(", mCssIndicator=");
            sb2.append(this.mCssIndicator ? "supported" : "unsupported");
            sb2.append(", mNetworkId=");
            sb2.append(this.mNetworkId);
            sb2.append(", mSystemId=");
            sb2.append(this.mSystemId);
            sb2.append(", mCdmaRoamingIndicator=");
            sb2.append(this.mCdmaRoamingIndicator);
            sb2.append(", mCdmaDefaultRoamingIndicator=");
            sb2.append(this.mCdmaDefaultRoamingIndicator);
            sb2.append(", mIsEmergencyOnly=");
            sb2.append(this.mIsEmergencyOnly);
            sb2.append(", isUsingCarrierAggregation=");
            sb2.append(isUsingCarrierAggregation());
            sb2.append(", mLteEarfcnRsrpBoost=");
            sb2.append(this.mLteEarfcnRsrpBoost);
            sb2.append(", mNetworkRegistrationInfos=");
            sb2.append(this.mNetworkRegistrationInfos);
            sb2.append(", mNrFrequencyRange=");
            sb2.append(this.mNrFrequencyRange);
            sb2.append(", mOperatorAlphaLongRaw=");
            sb2.append(this.mOperatorAlphaLongRaw);
            sb2.append(", mOperatorAlphaShortRaw=");
            sb2.append(this.mOperatorAlphaShortRaw);
            sb2.append(", mIsIwlanPreferred=");
            sb2.append(this.mIsIwlanPreferred);
            sb2.append(", Ril Voice Regist state=");
            sb2.append(this.mRilVoiceRegState);
            sb2.append(", Ril Data Regist state=");
            sb2.append(this.mRilDataRegState);
            sb2.append(", mProprietaryDataRadioTechnology=");
            sb2.append(this.mProprietaryDataRadioTechnology);
            sb2.append(", VoiceRejectCause=");
            sb2.append(this.mVoiceRejectCause);
            sb2.append(", DataRejectCause=");
            sb2.append(this.mDataRejectCause);
            sb2.append(", IwlanRegState=");
            sb2.append(getIwlanRegState());
            sb2.append(", CellularVoiceRegState=");
            sb2.append(getCellularVoiceRegState());
            sb2.append(", CellularDataRegState=");
            sb2.append(getCellularDataRegState());
            sb2.append(", RilCellularDataRegState=");
            sb2.append(getRilCellularDataRegState());
            sb2.append(", CellularDataRoamingType=");
            sb2.append(getCellularDataRoamingType());
            sb2.append(", CellularDataNetworkType=");
            sb2.append(getCellularDataNetworkType());
            sb2.append("}");
            sb = sb2.toString();
        }
        return sb;
    }

    /* access modifiers changed from: protected */
    public void init() {
        super.init();
        this.mRilVoiceRegState = 0;
        this.mRilDataRegState = 0;
        this.mProprietaryDataRadioTechnology = 0;
        this.mVoiceRejectCause = -1;
        this.mDataRejectCause = -1;
        this.mRilCellularDataRegState = 0;
        this.mCellularDataNetworkType = 0;
    }

    /* access modifiers changed from: protected */
    public void setFromNotifierBundle(Bundle m) {
        MtkServiceState ssFromBundle = (MtkServiceState) m.getParcelable("android.intent.extra.SERVICE_STATE");
        if (ssFromBundle != null) {
            copyFrom(ssFromBundle);
        }
    }

    public void fillInNotifierBundle(Bundle m) {
        m.putParcelable("android.intent.extra.SERVICE_STATE", this);
        m.putInt("voiceRegState", this.mVoiceRegState);
        m.putInt("dataRegState", this.mDataRegState);
        m.putInt("dataRoamingType", getDataRoamingType());
        m.putInt("voiceRoamingType", getVoiceRoamingType());
        m.putString("operator-alpha-long", this.mVoiceOperatorAlphaLong);
        m.putString("operator-alpha-short", this.mVoiceOperatorAlphaShort);
        m.putString("operator-numeric", this.mVoiceOperatorNumeric);
        m.putString("data-operator-alpha-long", this.mDataOperatorAlphaLong);
        m.putString("data-operator-alpha-short", this.mDataOperatorAlphaShort);
        m.putString("data-operator-numeric", this.mDataOperatorNumeric);
        m.putBoolean("manual", this.mIsManualNetworkSelection);
        m.putInt("radioTechnology", getRilVoiceRadioTechnology());
        m.putInt("dataRadioTechnology", getRadioTechnology());
        m.putBoolean("cssIndicator", this.mCssIndicator);
        m.putInt("networkId", this.mNetworkId);
        m.putInt("systemId", this.mSystemId);
        m.putInt("cdmaRoamingIndicator", this.mCdmaRoamingIndicator);
        m.putInt("cdmaDefaultRoamingIndicator", this.mCdmaDefaultRoamingIndicator);
        m.putBoolean("emergencyOnly", this.mIsEmergencyOnly);
        m.putBoolean("isDataRoamingFromRegistration", getDataRoamingFromRegistration());
        m.putBoolean("isUsingCarrierAggregation", isUsingCarrierAggregation());
        m.putInt("LteEarfcnRsrpBoost", this.mLteEarfcnRsrpBoost);
        m.putInt("ChannelNumber", this.mChannelNumber);
        m.putIntArray("CellBandwidths", this.mCellBandwidths);
        m.putInt("mNrFrequencyRange", this.mNrFrequencyRange);
        m.putInt("RilVoiceRegState", this.mRilVoiceRegState);
        m.putInt("RilDataRegState", this.mRilDataRegState);
        m.putInt("proprietaryDataRadioTechnology", this.mProprietaryDataRadioTechnology);
        m.putInt("VoiceRejectCause", this.mVoiceRejectCause);
        m.putInt("DataRejectCause", this.mDataRejectCause);
        m.putInt("RilCellularDataRegState", this.mRilCellularDataRegState);
        m.putInt("CellularDataNetworkType", this.mCellularDataNetworkType);
    }

    public static MtkServiceState mergeMtkServiceStates(MtkServiceState baseSs, MtkServiceState voiceSs) {
        if ((baseSs.mVoiceRegState == 1 && baseSs.mDataRegState == 1) || voiceSs.mVoiceRegState != 0) {
            return baseSs;
        }
        MtkServiceState newSs = new MtkServiceState(baseSs);
        newSs.mVoiceRegState = voiceSs.mVoiceRegState;
        newSs.mIsEmergencyOnly = false;
        return newSs;
    }

    public int getVoiceRejectCause() {
        return this.mVoiceRejectCause;
    }

    public int getDataRejectCause() {
        return this.mDataRejectCause;
    }

    public void setVoiceRejectCause(int cause) {
        this.mVoiceRejectCause = cause;
    }

    public void setDataRejectCause(int cause) {
        this.mDataRejectCause = cause;
    }

    public int getProprietaryDataRadioTechnology() {
        return this.mProprietaryDataRadioTechnology;
    }

    public void setProprietaryDataRadioTechnology(int rt) {
        this.mProprietaryDataRadioTechnology = rt;
    }

    public int rilRadioTechnologyToNetworkTypeEx(int rt) {
        return rilRadioTechnologyToNetworkType(rt);
    }

    public int getRilVoiceRegState() {
        return this.mRilVoiceRegState;
    }

    public int getRilDataRegState() {
        return this.mRilDataRegState;
    }

    public void setRilVoiceRegState(int nRegState) {
        this.mRilVoiceRegState = nRegState;
    }

    public void setRilDataRegState(int nDataRegState) {
        this.mRilDataRegState = nDataRegState;
    }

    public boolean compareTwoRadioTechnology(int nRadioTechnology1, int nRadioTechnology2) {
        if (nRadioTechnology1 == nRadioTechnology2) {
            return false;
        }
        if (nRadioTechnology1 == 14) {
            return true;
        }
        if (nRadioTechnology2 == 14) {
            return false;
        }
        if (nRadioTechnology1 == 16) {
            if (nRadioTechnology2 == 0) {
                return true;
            }
            return false;
        } else if (nRadioTechnology2 == 16) {
            if (nRadioTechnology1 == 0) {
                return false;
            }
            return true;
        } else if (nRadioTechnology1 > nRadioTechnology2) {
            return true;
        } else {
            return false;
        }
    }

    public boolean getCellularDataRoaming() {
        NetworkRegistrationInfo regState = getNetworkRegistrationInfo(2, 1);
        if (regState == null) {
            return false;
        }
        if (regState.getRoamingType() != 0) {
            return true;
        }
        return false;
    }

    public int getCellularDataNetworkType() {
        return this.mCellularDataNetworkType;
    }

    public int getCellularRegState() {
        if (getCellularVoiceRegState() == 3) {
            return 3;
        }
        if (getCellularDataRegState() == 0) {
            return 0;
        }
        return getCellularVoiceRegState();
    }

    public int getCellularVoiceRegState() {
        NetworkRegistrationInfo regCsState = getNetworkRegistrationInfo(1, 1);
        if (this.mVoiceRegState == 3) {
            return this.mVoiceRegState;
        }
        if (regCsState == null || !regCsState.isInService()) {
            return 1;
        }
        return 0;
    }

    public int getCellularDataRegState() {
        if (this.mVoiceRegState == 3) {
            return this.mVoiceRegState;
        }
        int i = this.mRilCellularDataRegState;
        if (i == 1 || i == 5) {
            return 0;
        }
        return 1;
    }

    public int getRilCellularDataRegState() {
        return this.mRilCellularDataRegState;
    }

    public int getCellularDataRoamingType() {
        NetworkRegistrationInfo regPsState = getNetworkRegistrationInfo(2, 1);
        if (this.mVoiceRegState == 3 || regPsState == null) {
            return 0;
        }
        return regPsState.getRoamingType();
    }

    public boolean isUsingCellularCarrierAggregation() {
        return isUsingCarrierAggregation();
    }

    public int getIwlanRegState() {
        NetworkRegistrationInfo regIwlanState = getNetworkRegistrationInfo(2, 2);
        if (regIwlanState == null || !regIwlanState.isInService()) {
            return 1;
        }
        return 0;
    }

    public void keepCellularDataServiceState() {
        NetworkRegistrationInfo regPsState = getNetworkRegistrationInfo(2, 1);
        if (regPsState != null) {
            this.mRilCellularDataRegState = regPsState.getRegistrationState();
            this.mCellularDataNetworkType = regPsState.getAccessNetworkTechnology();
        }
    }

    public int getRilDataRadioTechnology() {
        return mtkNetworkTypeToRilRadioTechnology(getDataNetworkType());
    }

    public int getRilVoiceRadioTechnology() {
        NetworkRegistrationInfo wwanRegInfo = getNetworkRegistrationInfo(1, 1);
        if (wwanRegInfo != null) {
            return mtkNetworkTypeToRilRadioTechnology(wwanRegInfo.getAccessNetworkTechnology());
        }
        return 0;
    }

    private static int mtkNetworkTypeToRilRadioTechnology(int networkType) {
        switch (networkType) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 7;
            case 6:
                return 8;
            case 7:
                return 6;
            case 8:
                return 9;
            case 9:
                return 10;
            case 10:
                return 11;
            case 11:
            default:
                return 0;
            case 12:
                return 12;
            case 13:
                return 14;
            case 14:
                return 13;
            case 15:
                return 15;
            case 16:
                return 16;
            case MtkPhoneNumberFormatUtil.FORMAT_THAILAND:
                return 17;
            case MtkPhoneNumberFormatUtil.FORMAT_VIETNAM:
                return 18;
            case MtkPhoneNumberFormatUtil.FORMAT_PORTUGAL:
                return 19;
            case MtkPhoneNumberFormatUtil.FORMAT_POLAND:
                return 20;
        }
    }

    public NetworkRegistrationInfo getNetworkRegistrationInfo(int domain, int transportType) {
        synchronized (this.mNetworkRegistrationInfos) {
            for (NetworkRegistrationInfo networkRegistrationInfo : this.mNetworkRegistrationInfos) {
                if (networkRegistrationInfo == null) {
                    Rlog.e(LOG_TAG, "getNetworkRegistrationInfo find null nris=" + this.mNetworkRegistrationInfos);
                } else if (networkRegistrationInfo.getTransportType() == transportType && networkRegistrationInfo.getDomain() == domain) {
                    NetworkRegistrationInfo networkRegistrationInfo2 = new NetworkRegistrationInfo(networkRegistrationInfo);
                    return networkRegistrationInfo2;
                }
            }
            return null;
        }
    }
}

package android.telephony;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;

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
public class ServiceState implements Parcelable {
    public static final Creator<ServiceState> CREATOR = null;
    static final boolean DBG = false;
    static final String LOG_TAG = "PHONE";
    public static final int REGISTRATION_STATE_HOME_NETWORK = 1;
    public static final int REGISTRATION_STATE_NOT_REGISTERED_AND_NOT_SEARCHING = 0;
    public static final int REGISTRATION_STATE_NOT_REGISTERED_AND_NOT_SEARCHING_EMERGENCY_CALL_ENABLED = 10;
    public static final int REGISTRATION_STATE_NOT_REGISTERED_AND_SEARCHING = 2;
    public static final int REGISTRATION_STATE_NOT_REGISTERED_AND_SEARCHING_EMERGENCY_CALL_ENABLED = 12;
    public static final int REGISTRATION_STATE_REGISTRATION_DENIED = 3;
    public static final int REGISTRATION_STATE_REGISTRATION_DENIED_EMERGENCY_CALL_ENABLED = 13;
    public static final int REGISTRATION_STATE_ROAMING = 5;
    public static final int REGISTRATION_STATE_UNKNOWN = 4;
    public static final int REGISTRATION_STATE_UNKNOWN_EMERGENCY_CALL_ENABLED = 14;
    public static final int RIL_RADIO_CDMA_TECHNOLOGY_BITMASK = 6392;
    public static final int RIL_RADIO_TECHNOLOGY_1xRTT = 6;
    public static final int RIL_RADIO_TECHNOLOGY_DC_DPA = 133;
    public static final int RIL_RADIO_TECHNOLOGY_DC_HSDPAP = 135;
    public static final int RIL_RADIO_TECHNOLOGY_DC_HSDPAP_DPA = 137;
    public static final int RIL_RADIO_TECHNOLOGY_DC_HSDPAP_UPA = 136;
    public static final int RIL_RADIO_TECHNOLOGY_DC_HSPAP = 138;
    public static final int RIL_RADIO_TECHNOLOGY_DC_UPA = 134;
    public static final int RIL_RADIO_TECHNOLOGY_EDGE = 2;
    public static final int RIL_RADIO_TECHNOLOGY_EHRPD = 13;
    public static final int RIL_RADIO_TECHNOLOGY_EVDO_0 = 7;
    public static final int RIL_RADIO_TECHNOLOGY_EVDO_A = 8;
    public static final int RIL_RADIO_TECHNOLOGY_EVDO_B = 12;
    public static final int RIL_RADIO_TECHNOLOGY_GPRS = 1;
    public static final int RIL_RADIO_TECHNOLOGY_GSM = 16;
    public static final int RIL_RADIO_TECHNOLOGY_HSDPA = 9;
    public static final int RIL_RADIO_TECHNOLOGY_HSDPAP = 129;
    public static final int RIL_RADIO_TECHNOLOGY_HSDPAP_UPA = 130;
    public static final int RIL_RADIO_TECHNOLOGY_HSPA = 11;
    public static final int RIL_RADIO_TECHNOLOGY_HSPAP = 15;
    public static final int RIL_RADIO_TECHNOLOGY_HSUPA = 10;
    public static final int RIL_RADIO_TECHNOLOGY_HSUPAP = 131;
    public static final int RIL_RADIO_TECHNOLOGY_HSUPAP_DPA = 132;
    public static final int RIL_RADIO_TECHNOLOGY_IS95A = 4;
    public static final int RIL_RADIO_TECHNOLOGY_IS95B = 5;
    public static final int RIL_RADIO_TECHNOLOGY_IWLAN = 18;
    public static final int RIL_RADIO_TECHNOLOGY_LTE = 14;
    public static final int RIL_RADIO_TECHNOLOGY_LTEA = 139;
    public static final int RIL_RADIO_TECHNOLOGY_LTE_CA = 19;
    public static final int RIL_RADIO_TECHNOLOGY_MTK = 128;
    public static final int RIL_RADIO_TECHNOLOGY_TD_SCDMA = 17;
    public static final int RIL_RADIO_TECHNOLOGY_UMTS = 3;
    public static final int RIL_RADIO_TECHNOLOGY_UNKNOWN = 0;
    public static final int RIL_REG_STATE_DENIED = 3;
    public static final int RIL_REG_STATE_DENIED_EMERGENCY_CALL_ENABLED = 13;
    public static final int RIL_REG_STATE_HOME = 1;
    public static final int RIL_REG_STATE_NOT_REG = 0;
    public static final int RIL_REG_STATE_NOT_REG_EMERGENCY_CALL_ENABLED = 10;
    public static final int RIL_REG_STATE_ROAMING = 5;
    public static final int RIL_REG_STATE_SEARCHING = 2;
    public static final int RIL_REG_STATE_SEARCHING_EMERGENCY_CALL_ENABLED = 12;
    public static final int RIL_REG_STATE_UNKNOWN = 4;
    public static final int RIL_REG_STATE_UNKNOWN_EMERGENCY_CALL_ENABLED = 14;
    public static final int ROAMING_TYPE_DOMESTIC = 2;
    public static final int ROAMING_TYPE_INTERNATIONAL = 3;
    public static final int ROAMING_TYPE_NOT_ROAMING = 0;
    public static final int ROAMING_TYPE_UNKNOWN = 1;
    public static final int STATE_EMERGENCY_ONLY = 2;
    public static final int STATE_IN_SERVICE = 0;
    public static final int STATE_OUT_OF_SERVICE = 1;
    public static final int STATE_POWER_OFF = 3;
    static final boolean VDBG = false;
    private int mCaDlCc;
    private int mCaUlCc;
    private int mCdmaDefaultRoamingIndicator;
    private int mCdmaEriIconIndex;
    private int mCdmaEriIconMode;
    private int mCdmaRoamingIndicator;
    private boolean mCssIndicator;
    private String mDataOperatorAlphaLong;
    private String mDataOperatorAlphaShort;
    private String mDataOperatorNumeric;
    private int mDataRegState;
    private int mDataRejectCause;
    private int mDataRoamingType;
    private boolean mIsDataRoamingFromRegistration;
    private boolean mIsEmergencyOnly;
    private boolean mIsManualNetworkSelection;
    private boolean mIsUsingCarrierAggregation;
    private int mNetworkId;
    private int mProprietaryDataRadioTechnology;
    private int mRilDataRadioTechnology;
    private int mRilDataRegState;
    private int mRilVoiceRadioTechnology;
    private int mRilVoiceRegState;
    private int mSystemId;
    private String mVoiceOperatorAlphaLong;
    private String mVoiceOperatorAlphaShort;
    private String mVoiceOperatorNumeric;
    private int mVoiceRegState;
    private int mVoiceRejectCause;
    private int mVoiceRoamingType;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.ServiceState.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.ServiceState.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.ServiceState.<clinit>():void");
    }

    public static final String getRoamingLogString(int roamingType) {
        switch (roamingType) {
            case 0:
                return "home";
            case 1:
                return "roaming";
            case 2:
                return "Domestic Roaming";
            case 3:
                return "International Roaming";
            default:
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
    }

    public static ServiceState newFromBundle(Bundle m) {
        ServiceState ret = new ServiceState();
        ret.setFromNotifierBundle(m);
        return ret;
    }

    public ServiceState() {
        this.mVoiceRegState = 1;
        this.mDataRegState = 1;
        this.mCdmaRoamingIndicator = -1;
        this.mCdmaDefaultRoamingIndicator = -1;
        this.mCdmaEriIconIndex = -1;
        this.mCdmaEriIconMode = -1;
        this.mRilVoiceRegState = 0;
        this.mRilDataRegState = 0;
        this.mVoiceRejectCause = -1;
        this.mDataRejectCause = -1;
        this.mCaDlCc = 0;
        this.mCaUlCc = 0;
    }

    public ServiceState(ServiceState s) {
        this.mVoiceRegState = 1;
        this.mDataRegState = 1;
        this.mCdmaRoamingIndicator = -1;
        this.mCdmaDefaultRoamingIndicator = -1;
        this.mCdmaEriIconIndex = -1;
        this.mCdmaEriIconMode = -1;
        this.mRilVoiceRegState = 0;
        this.mRilDataRegState = 0;
        this.mVoiceRejectCause = -1;
        this.mDataRejectCause = -1;
        this.mCaDlCc = 0;
        this.mCaUlCc = 0;
        copyFrom(s);
    }

    protected void copyFrom(ServiceState s) {
        this.mVoiceRegState = s.mVoiceRegState;
        this.mDataRegState = s.mDataRegState;
        this.mVoiceRoamingType = s.mVoiceRoamingType;
        this.mDataRoamingType = s.mDataRoamingType;
        this.mVoiceOperatorAlphaLong = s.mVoiceOperatorAlphaLong;
        this.mVoiceOperatorAlphaShort = s.mVoiceOperatorAlphaShort;
        this.mVoiceOperatorNumeric = s.mVoiceOperatorNumeric;
        this.mDataOperatorAlphaLong = s.mDataOperatorAlphaLong;
        this.mDataOperatorAlphaShort = s.mDataOperatorAlphaShort;
        this.mDataOperatorNumeric = s.mDataOperatorNumeric;
        this.mIsManualNetworkSelection = s.mIsManualNetworkSelection;
        this.mRilVoiceRadioTechnology = s.mRilVoiceRadioTechnology;
        this.mRilDataRadioTechnology = s.mRilDataRadioTechnology;
        this.mCssIndicator = s.mCssIndicator;
        this.mNetworkId = s.mNetworkId;
        this.mSystemId = s.mSystemId;
        this.mCdmaRoamingIndicator = s.mCdmaRoamingIndicator;
        this.mCdmaDefaultRoamingIndicator = s.mCdmaDefaultRoamingIndicator;
        this.mCdmaEriIconIndex = s.mCdmaEriIconIndex;
        this.mCdmaEriIconMode = s.mCdmaEriIconMode;
        this.mIsEmergencyOnly = s.mIsEmergencyOnly;
        this.mRilVoiceRegState = s.mRilVoiceRegState;
        this.mRilDataRegState = s.mRilDataRegState;
        this.mProprietaryDataRadioTechnology = s.mProprietaryDataRadioTechnology;
        this.mVoiceRejectCause = s.mVoiceRejectCause;
        this.mDataRejectCause = s.mDataRejectCause;
        this.mIsDataRoamingFromRegistration = s.mIsDataRoamingFromRegistration;
        this.mIsUsingCarrierAggregation = s.mIsUsingCarrierAggregation;
    }

    public ServiceState(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mVoiceRegState = 1;
        this.mDataRegState = 1;
        this.mCdmaRoamingIndicator = -1;
        this.mCdmaDefaultRoamingIndicator = -1;
        this.mCdmaEriIconIndex = -1;
        this.mCdmaEriIconMode = -1;
        this.mRilVoiceRegState = 0;
        this.mRilDataRegState = 0;
        this.mVoiceRejectCause = -1;
        this.mDataRejectCause = -1;
        this.mCaDlCc = 0;
        this.mCaUlCc = 0;
        this.mVoiceRegState = in.readInt();
        this.mDataRegState = in.readInt();
        this.mVoiceRoamingType = in.readInt();
        this.mDataRoamingType = in.readInt();
        this.mVoiceOperatorAlphaLong = in.readString();
        this.mVoiceOperatorAlphaShort = in.readString();
        this.mVoiceOperatorNumeric = in.readString();
        this.mDataOperatorAlphaLong = in.readString();
        this.mDataOperatorAlphaShort = in.readString();
        this.mDataOperatorNumeric = in.readString();
        this.mIsManualNetworkSelection = in.readInt() != 0;
        this.mRilVoiceRadioTechnology = in.readInt();
        this.mRilDataRadioTechnology = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mCssIndicator = z;
        this.mNetworkId = in.readInt();
        this.mSystemId = in.readInt();
        this.mCdmaRoamingIndicator = in.readInt();
        this.mCdmaDefaultRoamingIndicator = in.readInt();
        this.mCdmaEriIconIndex = in.readInt();
        this.mCdmaEriIconMode = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsEmergencyOnly = z;
        this.mRilVoiceRegState = in.readInt();
        this.mRilDataRegState = in.readInt();
        this.mProprietaryDataRadioTechnology = in.readInt();
        this.mVoiceRejectCause = in.readInt();
        this.mDataRejectCause = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsDataRoamingFromRegistration = z;
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.mIsUsingCarrierAggregation = z2;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        out.writeInt(this.mVoiceRegState);
        out.writeInt(this.mDataRegState);
        out.writeInt(this.mVoiceRoamingType);
        out.writeInt(this.mDataRoamingType);
        out.writeString(this.mVoiceOperatorAlphaLong);
        out.writeString(this.mVoiceOperatorAlphaShort);
        out.writeString(this.mVoiceOperatorNumeric);
        out.writeString(this.mDataOperatorAlphaLong);
        out.writeString(this.mDataOperatorAlphaShort);
        out.writeString(this.mDataOperatorNumeric);
        out.writeInt(this.mIsManualNetworkSelection ? 1 : 0);
        out.writeInt(this.mRilVoiceRadioTechnology);
        out.writeInt(this.mRilDataRadioTechnology);
        if (this.mCssIndicator) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        out.writeInt(this.mNetworkId);
        out.writeInt(this.mSystemId);
        out.writeInt(this.mCdmaRoamingIndicator);
        out.writeInt(this.mCdmaDefaultRoamingIndicator);
        out.writeInt(this.mCdmaEriIconIndex);
        out.writeInt(this.mCdmaEriIconMode);
        if (this.mIsEmergencyOnly) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        out.writeInt(this.mRilVoiceRegState);
        out.writeInt(this.mRilDataRegState);
        out.writeInt(this.mProprietaryDataRadioTechnology);
        out.writeInt(this.mVoiceRejectCause);
        out.writeInt(this.mDataRejectCause);
        if (this.mIsDataRoamingFromRegistration) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.mIsUsingCarrierAggregation) {
            i2 = 0;
        }
        out.writeInt(i2);
    }

    public int describeContents() {
        return 0;
    }

    public int getState() {
        return getVoiceRegState();
    }

    public int getVoiceRegState() {
        return this.mVoiceRegState;
    }

    public int getDataRegState() {
        return this.mDataRegState;
    }

    public boolean getRoaming() {
        return !getVoiceRoaming() ? getDataRoaming() : true;
    }

    public boolean getVoiceRoaming() {
        return this.mVoiceRoamingType != 0;
    }

    public int getVoiceRoamingType() {
        return this.mVoiceRoamingType;
    }

    public boolean getDataRoaming() {
        return this.mDataRoamingType != 0;
    }

    public void setDataRoamingFromRegistration(boolean dataRoaming) {
        this.mIsDataRoamingFromRegistration = dataRoaming;
    }

    public boolean getDataRoamingFromRegistration() {
        return this.mIsDataRoamingFromRegistration;
    }

    public int getDataRoamingType() {
        return this.mDataRoamingType;
    }

    public boolean isEmergencyOnly() {
        return this.mIsEmergencyOnly;
    }

    public int getCdmaRoamingIndicator() {
        return this.mCdmaRoamingIndicator;
    }

    public int getCdmaDefaultRoamingIndicator() {
        return this.mCdmaDefaultRoamingIndicator;
    }

    public int getCdmaEriIconIndex() {
        return this.mCdmaEriIconIndex;
    }

    public int getCdmaEriIconMode() {
        return this.mCdmaEriIconMode;
    }

    public String getOperatorAlphaLong() {
        return this.mVoiceOperatorAlphaLong;
    }

    public String getVoiceOperatorAlphaLong() {
        return this.mVoiceOperatorAlphaLong;
    }

    public String getDataOperatorAlphaLong() {
        return this.mDataOperatorAlphaLong;
    }

    public String getOperatorAlphaShort() {
        return this.mVoiceOperatorAlphaShort;
    }

    public String getVoiceOperatorAlphaShort() {
        return this.mVoiceOperatorAlphaShort;
    }

    public String getDataOperatorAlphaShort() {
        return this.mDataOperatorAlphaShort;
    }

    public String getOperatorNumeric() {
        return this.mVoiceOperatorNumeric;
    }

    public String getVoiceOperatorNumeric() {
        return this.mVoiceOperatorNumeric;
    }

    public String getDataOperatorNumeric() {
        return this.mDataOperatorNumeric;
    }

    public boolean getIsManualSelection() {
        return this.mIsManualNetworkSelection;
    }

    public int getVoiceRejectCause() {
        return this.mVoiceRejectCause;
    }

    public int getDataRejectCause() {
        return this.mDataRejectCause;
    }

    public int hashCode() {
        int i;
        int i2 = 1;
        int i3 = this.mDataRoamingType + (((this.mVoiceRegState * 31) + (this.mDataRegState * 37)) + this.mVoiceRoamingType);
        if (this.mIsManualNetworkSelection) {
            i = 1;
        } else {
            i = 0;
        }
        i3 += i;
        if (this.mVoiceOperatorAlphaLong == null) {
            i = 0;
        } else {
            i = this.mVoiceOperatorAlphaLong.hashCode();
        }
        i3 += i;
        if (this.mVoiceOperatorAlphaShort == null) {
            i = 0;
        } else {
            i = this.mVoiceOperatorAlphaShort.hashCode();
        }
        i3 += i;
        if (this.mVoiceOperatorNumeric == null) {
            i = 0;
        } else {
            i = this.mVoiceOperatorNumeric.hashCode();
        }
        i3 += i;
        if (this.mDataOperatorAlphaLong == null) {
            i = 0;
        } else {
            i = this.mDataOperatorAlphaLong.hashCode();
        }
        i3 += i;
        if (this.mDataOperatorAlphaShort == null) {
            i = 0;
        } else {
            i = this.mDataOperatorAlphaShort.hashCode();
        }
        i3 += i;
        if (this.mDataOperatorNumeric == null) {
            i = 0;
        } else {
            i = this.mDataOperatorNumeric.hashCode();
        }
        i3 = this.mCdmaDefaultRoamingIndicator + ((i + i3) + this.mCdmaRoamingIndicator);
        if (this.mIsEmergencyOnly) {
            i = 1;
        } else {
            i = 0;
        }
        i += i3;
        if (!this.mIsDataRoamingFromRegistration) {
            i2 = 0;
        }
        return i + i2;
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            ServiceState s = (ServiceState) o;
            if (o == null) {
                return false;
            }
            if (this.mVoiceRegState == s.mVoiceRegState && this.mDataRegState == s.mDataRegState && this.mIsManualNetworkSelection == s.mIsManualNetworkSelection && this.mVoiceRoamingType == s.mVoiceRoamingType && this.mDataRoamingType == s.mDataRoamingType && equalsHandlesNulls(this.mVoiceOperatorAlphaLong, s.mVoiceOperatorAlphaLong) && equalsHandlesNulls(this.mVoiceOperatorAlphaShort, s.mVoiceOperatorAlphaShort) && equalsHandlesNulls(this.mVoiceOperatorNumeric, s.mVoiceOperatorNumeric) && equalsHandlesNulls(this.mDataOperatorAlphaLong, s.mDataOperatorAlphaLong) && equalsHandlesNulls(this.mDataOperatorAlphaShort, s.mDataOperatorAlphaShort) && equalsHandlesNulls(this.mDataOperatorNumeric, s.mDataOperatorNumeric) && equalsHandlesNulls(Integer.valueOf(this.mRilVoiceRadioTechnology), Integer.valueOf(s.mRilVoiceRadioTechnology)) && equalsHandlesNulls(Integer.valueOf(this.mRilDataRadioTechnology), Integer.valueOf(s.mRilDataRadioTechnology)) && equalsHandlesNulls(Boolean.valueOf(this.mCssIndicator), Boolean.valueOf(s.mCssIndicator)) && equalsHandlesNulls(Integer.valueOf(this.mNetworkId), Integer.valueOf(s.mNetworkId)) && equalsHandlesNulls(Integer.valueOf(this.mSystemId), Integer.valueOf(s.mSystemId)) && equalsHandlesNulls(Integer.valueOf(this.mCdmaRoamingIndicator), Integer.valueOf(s.mCdmaRoamingIndicator)) && equalsHandlesNulls(Integer.valueOf(this.mCdmaDefaultRoamingIndicator), Integer.valueOf(s.mCdmaDefaultRoamingIndicator)) && this.mIsEmergencyOnly == s.mIsEmergencyOnly && this.mRilVoiceRegState == s.mRilVoiceRegState && this.mRilDataRegState == s.mRilDataRegState && equalsHandlesNulls(Integer.valueOf(this.mProprietaryDataRadioTechnology), Integer.valueOf(s.mProprietaryDataRadioTechnology)) && this.mVoiceRejectCause == s.mVoiceRejectCause && this.mDataRejectCause == s.mDataRejectCause && this.mIsDataRoamingFromRegistration == s.mIsDataRoamingFromRegistration && this.mIsUsingCarrierAggregation == s.mIsUsingCarrierAggregation) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public static String rilRadioTechnologyToString(int rt) {
        switch (rt) {
            case 0:
                return "Unknown";
            case 1:
                return "GPRS";
            case 2:
                return "EDGE";
            case 3:
                return "UMTS";
            case 4:
                return "CDMA-IS95A";
            case 5:
                return "CDMA-IS95B";
            case 6:
                return "1xRTT";
            case 7:
                return "EvDo-rev.0";
            case 8:
                return "EvDo-rev.A";
            case 9:
                return "HSDPA";
            case 10:
                return "HSUPA";
            case 11:
                return "HSPA";
            case 12:
                return "EvDo-rev.B";
            case 13:
                return "eHRPD";
            case 14:
                return "LTE";
            case 15:
                return "HSPAP";
            case 16:
                return "GSM";
            case 17:
                return "TD-SCDMA";
            case 18:
                return "IWLAN";
            case 19:
                return "LTE_CA";
            default:
                String rtString = "Unexpected";
                Rlog.w(LOG_TAG, "Unexpected radioTechnology=" + rt);
                return rtString;
        }
    }

    public String toString() {
        String str;
        String radioTechnology = rilRadioTechnologyToString(this.mRilVoiceRadioTechnology);
        String dataRadioTechnology = rilRadioTechnologyToString(this.mRilDataRadioTechnology);
        StringBuilder append = new StringBuilder().append(this.mVoiceRegState).append(" ").append(this.mDataRegState).append(" ").append("voice ").append(getRoamingLogString(this.mVoiceRoamingType)).append(" ").append("data ").append(getRoamingLogString(this.mDataRoamingType)).append(" ").append(this.mVoiceOperatorAlphaLong).append(" ").append(this.mVoiceOperatorAlphaShort).append(" ").append(this.mVoiceOperatorNumeric).append(" ").append(this.mDataOperatorAlphaLong).append(" ").append(this.mDataOperatorAlphaShort).append(" ").append(this.mDataOperatorNumeric).append(" ");
        if (this.mIsManualNetworkSelection) {
            str = "(manual)";
        } else {
            str = PhoneConstants.MVNO_TYPE_NONE;
        }
        append = append.append(str).append(" ").append(radioTechnology).append(" ").append(dataRadioTechnology).append(" ");
        if (this.mCssIndicator) {
            str = "CSS supported";
        } else {
            str = "CSS not supported";
        }
        return append.append(str).append(" ").append(this.mNetworkId).append(" ").append(this.mSystemId).append(" RoamInd=").append(this.mCdmaRoamingIndicator).append(" DefRoamInd=").append(this.mCdmaDefaultRoamingIndicator).append(" EmergOnly=").append(this.mIsEmergencyOnly).append(" Ril Voice Regist state: ").append(this.mRilVoiceRegState).append(" Ril Data Regist state: ").append(this.mRilDataRegState).append(" mProprietaryDataRadioTechnology: ").append(this.mProprietaryDataRadioTechnology).append(" VoiceRejectCause: ").append(this.mVoiceRejectCause).append(" DataRejectCause: ").append(this.mDataRejectCause).append(" IsDataRoamingFromRegistration=").append(this.mIsDataRoamingFromRegistration).append(" IsUsingCarrierAggregation=").append(this.mIsUsingCarrierAggregation).toString();
    }

    private void setNullState(int state) {
        this.mVoiceRegState = state;
        this.mDataRegState = state;
        this.mVoiceRoamingType = 0;
        this.mDataRoamingType = 0;
        this.mVoiceOperatorAlphaLong = null;
        this.mVoiceOperatorAlphaShort = null;
        this.mVoiceOperatorNumeric = null;
        this.mDataOperatorAlphaLong = null;
        this.mDataOperatorAlphaShort = null;
        this.mDataOperatorNumeric = null;
        this.mIsManualNetworkSelection = false;
        this.mRilVoiceRadioTechnology = 0;
        this.mRilDataRadioTechnology = 0;
        this.mCssIndicator = false;
        this.mNetworkId = -1;
        this.mSystemId = -1;
        this.mCdmaRoamingIndicator = -1;
        this.mCdmaDefaultRoamingIndicator = -1;
        this.mCdmaEriIconIndex = -1;
        this.mCdmaEriIconMode = -1;
        this.mIsEmergencyOnly = false;
        this.mRilVoiceRegState = 0;
        this.mRilDataRegState = 0;
        this.mProprietaryDataRadioTechnology = 0;
        this.mVoiceRejectCause = -1;
        this.mDataRejectCause = -1;
        this.mIsDataRoamingFromRegistration = false;
        this.mIsUsingCarrierAggregation = false;
    }

    public void setStateOutOfService() {
        setNullState(1);
    }

    public void setStateOff() {
        setNullState(3);
    }

    public void setState(int state) {
        setVoiceRegState(state);
    }

    public void setVoiceRegState(int state) {
        this.mVoiceRegState = state;
    }

    public void setDataRegState(int state) {
        this.mDataRegState = state;
    }

    public void setRoaming(boolean roaming) {
        this.mVoiceRoamingType = roaming ? 1 : 0;
        this.mDataRoamingType = this.mVoiceRoamingType;
    }

    public void setVoiceRoaming(boolean roaming) {
        this.mVoiceRoamingType = roaming ? 1 : 0;
    }

    public void setVoiceRoamingType(int type) {
        this.mVoiceRoamingType = type;
    }

    public void setDataRoaming(boolean dataRoaming) {
        this.mDataRoamingType = dataRoaming ? 1 : 0;
    }

    public void setDataRoamingType(int type) {
        this.mDataRoamingType = type;
    }

    public void setEmergencyOnly(boolean emergencyOnly) {
        this.mIsEmergencyOnly = emergencyOnly;
    }

    public void setCdmaRoamingIndicator(int roaming) {
        this.mCdmaRoamingIndicator = roaming;
    }

    public void setCdmaDefaultRoamingIndicator(int roaming) {
        this.mCdmaDefaultRoamingIndicator = roaming;
    }

    public void setCdmaEriIconIndex(int index) {
        this.mCdmaEriIconIndex = index;
    }

    public void setCdmaEriIconMode(int mode) {
        this.mCdmaEriIconMode = mode;
    }

    public void setOperatorName(String longName, String shortName, String numeric) {
        this.mVoiceOperatorAlphaLong = longName;
        this.mVoiceOperatorAlphaShort = shortName;
        this.mVoiceOperatorNumeric = numeric;
        this.mDataOperatorAlphaLong = longName;
        this.mDataOperatorAlphaShort = shortName;
        this.mDataOperatorNumeric = numeric;
    }

    public void setVoiceOperatorName(String longName, String shortName, String numeric) {
        this.mVoiceOperatorAlphaLong = longName;
        this.mVoiceOperatorAlphaShort = shortName;
        this.mVoiceOperatorNumeric = numeric;
    }

    public void setDataOperatorName(String longName, String shortName, String numeric) {
        this.mDataOperatorAlphaLong = longName;
        this.mDataOperatorAlphaShort = shortName;
        this.mDataOperatorNumeric = numeric;
    }

    public void setOperatorAlphaLong(String longName) {
        this.mVoiceOperatorAlphaLong = longName;
        this.mDataOperatorAlphaLong = longName;
    }

    public void setVoiceOperatorAlphaLong(String longName) {
        this.mVoiceOperatorAlphaLong = longName;
    }

    public void setDataOperatorAlphaLong(String longName) {
        this.mDataOperatorAlphaLong = longName;
    }

    public void setIsManualSelection(boolean isManual) {
        this.mIsManualNetworkSelection = isManual;
    }

    public void setVoiceRejectCause(int cause) {
        this.mVoiceRejectCause = cause;
    }

    public void setDataRejectCause(int cause) {
        this.mDataRejectCause = cause;
    }

    private static boolean equalsHandlesNulls(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }

    private void setFromNotifierBundle(Bundle m) {
        this.mVoiceRegState = m.getInt("voiceRegState");
        this.mDataRegState = m.getInt("dataRegState");
        this.mVoiceRoamingType = m.getInt("voiceRoamingType");
        this.mDataRoamingType = m.getInt("dataRoamingType");
        this.mVoiceOperatorAlphaLong = m.getString("operator-alpha-long");
        this.mVoiceOperatorAlphaShort = m.getString("operator-alpha-short");
        this.mVoiceOperatorNumeric = m.getString("operator-numeric");
        this.mDataOperatorAlphaLong = m.getString("data-operator-alpha-long");
        this.mDataOperatorAlphaShort = m.getString("data-operator-alpha-short");
        this.mDataOperatorNumeric = m.getString("data-operator-numeric");
        this.mIsManualNetworkSelection = m.getBoolean("manual");
        this.mRilVoiceRadioTechnology = m.getInt("radioTechnology");
        this.mRilDataRadioTechnology = m.getInt("dataRadioTechnology");
        this.mCssIndicator = m.getBoolean("cssIndicator");
        this.mNetworkId = m.getInt("networkId");
        this.mSystemId = m.getInt("systemId");
        this.mCdmaRoamingIndicator = m.getInt("cdmaRoamingIndicator");
        this.mCdmaDefaultRoamingIndicator = m.getInt("cdmaDefaultRoamingIndicator");
        this.mIsEmergencyOnly = m.getBoolean("emergencyOnly");
        this.mRilVoiceRegState = m.getInt("RilVoiceRegState");
        this.mRilDataRegState = m.getInt("RilDataRegState");
        this.mProprietaryDataRadioTechnology = m.getInt("proprietaryDataRadioTechnology");
        this.mVoiceRejectCause = m.getInt("VoiceRejectCause");
        this.mDataRejectCause = m.getInt("DataRejectCause");
        this.mIsDataRoamingFromRegistration = m.getBoolean("isDataRoamingFromRegistration");
        this.mIsUsingCarrierAggregation = m.getBoolean("isUsingCarrierAggregation");
    }

    public void fillInNotifierBundle(Bundle m) {
        m.putInt("voiceRegState", this.mVoiceRegState);
        m.putInt("dataRegState", this.mDataRegState);
        m.putInt("voiceRoamingType", this.mVoiceRoamingType);
        m.putInt("dataRoamingType", this.mDataRoamingType);
        m.putString("operator-alpha-long", this.mVoiceOperatorAlphaLong);
        m.putString("operator-alpha-short", this.mVoiceOperatorAlphaShort);
        m.putString("operator-numeric", this.mVoiceOperatorNumeric);
        m.putString("data-operator-alpha-long", this.mDataOperatorAlphaLong);
        m.putString("data-operator-alpha-short", this.mDataOperatorAlphaShort);
        m.putString("data-operator-numeric", this.mDataOperatorNumeric);
        m.putBoolean("manual", Boolean.valueOf(this.mIsManualNetworkSelection).booleanValue());
        m.putInt("radioTechnology", this.mRilVoiceRadioTechnology);
        m.putInt("dataRadioTechnology", this.mRilDataRadioTechnology);
        m.putBoolean("cssIndicator", this.mCssIndicator);
        m.putInt("networkId", this.mNetworkId);
        m.putInt("systemId", this.mSystemId);
        m.putInt("cdmaRoamingIndicator", this.mCdmaRoamingIndicator);
        m.putInt("cdmaDefaultRoamingIndicator", this.mCdmaDefaultRoamingIndicator);
        m.putBoolean("emergencyOnly", Boolean.valueOf(this.mIsEmergencyOnly).booleanValue());
        m.putInt("RilVoiceRegState", this.mRilVoiceRegState);
        m.putInt("RilDataRegState", this.mRilDataRegState);
        m.putInt("proprietaryDataRadioTechnology", this.mProprietaryDataRadioTechnology);
        m.putInt("VoiceRejectCause", this.mVoiceRejectCause);
        m.putInt("DataRejectCause", this.mDataRejectCause);
        m.putBoolean("isDataRoamingFromRegistration", Boolean.valueOf(this.mIsDataRoamingFromRegistration).booleanValue());
        m.putBoolean("isUsingCarrierAggregation", Boolean.valueOf(this.mIsUsingCarrierAggregation).booleanValue());
    }

    public void setRilVoiceRadioTechnology(int rt) {
        if (rt == 19) {
            rt = 14;
        }
        this.mRilVoiceRadioTechnology = rt;
    }

    public void setRilDataRadioTechnology(int rt) {
        if (rt == 19) {
            rt = 14;
            this.mIsUsingCarrierAggregation = true;
        } else {
            this.mIsUsingCarrierAggregation = false;
        }
        this.mRilDataRadioTechnology = rt;
    }

    public boolean isUsingCarrierAggregation() {
        return this.mIsUsingCarrierAggregation;
    }

    public void setIsUsingCarrierAggregation(boolean ca) {
        this.mIsUsingCarrierAggregation = ca;
    }

    public void setCssIndicator(int css) {
        boolean z = false;
        if (css != 0) {
            z = true;
        }
        this.mCssIndicator = z;
    }

    public void setSystemAndNetworkId(int systemId, int networkId) {
        this.mSystemId = systemId;
        this.mNetworkId = networkId;
    }

    public int getRilVoiceRadioTechnology() {
        return this.mRilVoiceRadioTechnology;
    }

    public int getRilDataRadioTechnology() {
        return this.mRilDataRadioTechnology;
    }

    public int getProprietaryDataRadioTechnology() {
        return this.mProprietaryDataRadioTechnology;
    }

    public void setProprietaryDataRadioTechnology(int rt) {
        if (rt > 128) {
            if ((rt & 139) != 0) {
                this.mCaDlCc = (rt >> 28) & 15;
                this.mCaUlCc = (rt >> 24) & 15;
                rt = (rt & (~(this.mCaDlCc << 28))) & (~(this.mCaUlCc << 24));
            }
            this.mProprietaryDataRadioTechnology = rt;
            if (rt == 139) {
                rt = 14;
            } else {
                rt = 15;
            }
        } else {
            this.mProprietaryDataRadioTechnology = 0;
        }
        setRilDataRadioTechnology(rt);
    }

    public int getDataUpLinkCarrierCount() {
        return this.mCaUlCc;
    }

    public int getDataDownLinkCarrierCount() {
        return this.mCaDlCc;
    }

    public int getRadioTechnology() {
        Rlog.e(LOG_TAG, "ServiceState.getRadioTechnology() DEPRECATED will be removed *******");
        return getRilDataRadioTechnology();
    }

    public int rilRadioTechnologyToNetworkTypeEx(int rt) {
        return rilRadioTechnologyToNetworkType(rt);
    }

    private int rilRadioTechnologyToNetworkType(int rt) {
        switch (rt) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
            case 5:
                return 4;
            case 6:
                return 7;
            case 7:
                return 5;
            case 8:
                return 6;
            case 9:
                return 8;
            case 10:
                return 9;
            case 11:
                return 10;
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
            case 17:
                return 17;
            case 18:
                return 18;
            case 19:
                return 19;
            default:
                return 0;
        }
    }

    public int getNetworkType() {
        Rlog.e(LOG_TAG, "ServiceState.getNetworkType() DEPRECATED will be removed *******");
        return rilRadioTechnologyToNetworkType(this.mRilVoiceRadioTechnology);
    }

    public int getDataNetworkType() {
        return rilRadioTechnologyToNetworkType(this.mRilDataRadioTechnology);
    }

    public int getVoiceNetworkType() {
        return rilRadioTechnologyToNetworkType(this.mRilVoiceRadioTechnology);
    }

    public int getCssIndicator() {
        return this.mCssIndicator ? 1 : 0;
    }

    public int getNetworkId() {
        return this.mNetworkId;
    }

    public int getSystemId() {
        return this.mSystemId;
    }

    public static boolean isGsm(int radioTechnology) {
        if (radioTechnology == 1 || radioTechnology == 2 || radioTechnology == 3 || radioTechnology == 9 || radioTechnology == 10 || radioTechnology == 11 || radioTechnology == 14 || radioTechnology == 15 || radioTechnology == 16 || radioTechnology == 17 || radioTechnology == 18 || radioTechnology == 19) {
            return true;
        }
        return false;
    }

    public static boolean isCdma(int radioTechnology) {
        if (radioTechnology == 4 || radioTechnology == 5 || radioTechnology == 6 || radioTechnology == 7 || radioTechnology == 8 || radioTechnology == 12 || radioTechnology == 13) {
            return true;
        }
        return false;
    }

    public static boolean isLte(int radioTechnology) {
        if (radioTechnology == 14 || radioTechnology == 19) {
            return true;
        }
        return false;
    }

    public static boolean bearerBitmapHasCdma(int radioTechnologyBitmap) {
        return (radioTechnologyBitmap & RIL_RADIO_CDMA_TECHNOLOGY_BITMASK) != 0;
    }

    public static boolean bitmaskHasTech(int bearerBitmask, int radioTech) {
        boolean z = true;
        if (bearerBitmask == 0) {
            return true;
        }
        if (radioTech < 1) {
            return false;
        }
        if (((1 << (radioTech - 1)) & bearerBitmask) == 0) {
            z = false;
        }
        return z;
    }

    public static int getBitmaskForTech(int radioTech) {
        if (radioTech >= 1) {
            return 1 << (radioTech - 1);
        }
        return 0;
    }

    public static int getBitmaskFromString(String bearerList) {
        String[] bearers = bearerList.split("\\|");
        int bearerBitmask = 0;
        int length = bearers.length;
        int i = 0;
        while (i < length) {
            try {
                int bearerInt = Integer.parseInt(bearers[i].trim());
                if (bearerInt == 0) {
                    return 0;
                }
                bearerBitmask |= getBitmaskForTech(bearerInt);
                i++;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return bearerBitmask;
    }

    public static ServiceState mergeServiceStates(ServiceState baseSs, ServiceState voiceSs) {
        if (voiceSs.mVoiceRegState != 0) {
            return baseSs;
        }
        ServiceState newSs = new ServiceState(baseSs);
        newSs.mVoiceRegState = voiceSs.mVoiceRegState;
        newSs.mIsEmergencyOnly = false;
        return newSs;
    }

    public int getRegState() {
        return getRilVoiceRegState();
    }

    public int getRilVoiceRegState() {
        return this.mRilVoiceRegState;
    }

    public int getRilDataRegState() {
        return this.mRilDataRegState;
    }

    public void setRegState(int nRegState) {
        setRilVoiceRegState(nRegState);
    }

    public void setRilVoiceRegState(int nRegState) {
        this.mRilVoiceRegState = nRegState;
    }

    public void setRilDataRegState(int nDataRegState) {
        this.mRilDataRegState = nDataRegState;
    }

    public boolean isVoiceRadioTechnologyHigher(int nRadioTechnology) {
        return compareTwoRadioTechnology(this.mRilVoiceRadioTechnology, nRadioTechnology);
    }

    public boolean isDataRadioTechnologyHigher(int nRadioTechnology) {
        return compareTwoRadioTechnology(this.mRilDataRadioTechnology, nRadioTechnology);
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
        return nRadioTechnology1 == 16 ? nRadioTechnology2 == 0 : nRadioTechnology2 == 16 ? nRadioTechnology1 != 0 : nRadioTechnology1 > nRadioTechnology2;
    }
}

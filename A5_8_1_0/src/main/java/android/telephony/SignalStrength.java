package android.telephony;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import com.android.internal.os.PowerProfile;

public class SignalStrength implements Parcelable {
    public static final Creator<SignalStrength> CREATOR = new Creator() {
        public SignalStrength createFromParcel(Parcel in) {
            return new SignalStrength(in);
        }

        public SignalStrength[] newArray(int size) {
            return new SignalStrength[size];
        }
    };
    private static final boolean DBG = false;
    public static final int INVALID = Integer.MAX_VALUE;
    private static final String LOG_TAG = "SignalStrength";
    public static final int NT_CDMA = 5;
    public static final int NT_EVDO = 6;
    public static final int NT_GSM = 1;
    public static final int NT_LTE = 3;
    public static final int NT_TDS = 2;
    public static final int NT_UNKNOWN = 0;
    public static final int NT_WCDMA = 4;
    public static final int NUM_SIGNAL_STRENGTH_BINS = 5;
    public static int OEM_LEVLE = 0;
    public static final int SIGNAL_STRENGTH_GOOD = 3;
    public static final int SIGNAL_STRENGTH_GREAT = 4;
    public static final int SIGNAL_STRENGTH_MODERATE = 2;
    public static final String[] SIGNAL_STRENGTH_NAMES = new String[]{PowerProfile.POWER_NONE, "poor", "moderate", "good", "great"};
    public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int SIGNAL_STRENGTH_POOR = 1;
    private static final int[] TelstraAsuCriterion = new int[]{2, 7, 9, 14};
    private boolean isGsm;
    private int mCdmaDbm;
    private int mCdmaEcio;
    private int mEvdoDbm;
    private int mEvdoEcio;
    private int mEvdoSnr;
    private int mGsmBitErrorRate;
    private int mGsmSignalStrength;
    private int mLteCqi;
    private int mLteRsrp;
    private int mLteRsrpBoost;
    private int mLteRsrq;
    private int mLteRssnr;
    private int mLteSignalStrength;
    public int mOEMLevel_0;
    public int mOEMLevel_1;
    private int mTdScdmaRscp;
    private int[] mThreshRsrp;

    static {
        int i = 1;
        if (!SystemProperties.get("persist.sys.oem_smooth", "0").equals("1")) {
            i = 0;
        }
        OEM_LEVLE = i;
    }

    public static SignalStrength newFromBundle(Bundle m) {
        SignalStrength ret = new SignalStrength();
        ret.setFromNotifierBundle(m);
        return ret;
    }

    public SignalStrength() {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        this.mGsmSignalStrength = 99;
        this.mGsmBitErrorRate = -1;
        this.mCdmaDbm = -1;
        this.mCdmaEcio = -1;
        this.mEvdoDbm = -1;
        this.mEvdoEcio = -1;
        this.mEvdoSnr = -1;
        this.mLteSignalStrength = 99;
        this.mLteRsrp = Integer.MAX_VALUE;
        this.mLteRsrq = Integer.MAX_VALUE;
        this.mLteRssnr = Integer.MAX_VALUE;
        this.mLteCqi = Integer.MAX_VALUE;
        this.mLteRsrpBoost = 0;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.isGsm = true;
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
    }

    public SignalStrength(boolean gsmFlag) {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        this.mGsmSignalStrength = 99;
        this.mGsmBitErrorRate = -1;
        this.mCdmaDbm = -1;
        this.mCdmaEcio = -1;
        this.mEvdoDbm = -1;
        this.mEvdoEcio = -1;
        this.mEvdoSnr = -1;
        this.mLteSignalStrength = 99;
        this.mLteRsrp = Integer.MAX_VALUE;
        this.mLteRsrq = Integer.MAX_VALUE;
        this.mLteRssnr = Integer.MAX_VALUE;
        this.mLteCqi = Integer.MAX_VALUE;
        this.mLteRsrpBoost = 0;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.isGsm = gsmFlag;
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int lteRsrpBoost, int tdScdmaRscp, boolean gsmFlag) {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, lteRsrpBoost, gsmFlag);
        this.mTdScdmaRscp = tdScdmaRscp;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int tdScdmaRscp, boolean gsmFlag) {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, 0, gsmFlag);
        this.mTdScdmaRscp = tdScdmaRscp;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, boolean gsmFlag) {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, 0, gsmFlag);
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, boolean gsmFlag) {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, 99, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, gsmFlag);
    }

    public SignalStrength(SignalStrength s) {
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        copyFrom(s);
    }

    public void initialize(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, boolean gsm) {
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, 99, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, gsm);
    }

    public void initialize(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int lteRsrpBoost, boolean gsm) {
        this.mGsmSignalStrength = gsmSignalStrength;
        this.mGsmBitErrorRate = gsmBitErrorRate;
        this.mCdmaDbm = cdmaDbm;
        this.mCdmaEcio = cdmaEcio;
        this.mEvdoDbm = evdoDbm;
        this.mEvdoEcio = evdoEcio;
        this.mEvdoSnr = evdoSnr;
        this.mLteSignalStrength = lteSignalStrength;
        this.mLteRsrp = lteRsrp;
        this.mLteRsrq = lteRsrq;
        this.mLteRssnr = lteRssnr;
        this.mLteCqi = lteCqi;
        this.mLteRsrpBoost = lteRsrpBoost;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.isGsm = gsm;
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
    }

    protected void copyFrom(SignalStrength s) {
        this.mGsmSignalStrength = s.mGsmSignalStrength;
        this.mGsmBitErrorRate = s.mGsmBitErrorRate;
        this.mCdmaDbm = s.mCdmaDbm;
        this.mCdmaEcio = s.mCdmaEcio;
        this.mEvdoDbm = s.mEvdoDbm;
        this.mEvdoEcio = s.mEvdoEcio;
        this.mEvdoSnr = s.mEvdoSnr;
        this.mLteSignalStrength = s.mLteSignalStrength;
        this.mLteRsrp = s.mLteRsrp;
        this.mLteRsrq = s.mLteRsrq;
        this.mLteRssnr = s.mLteRssnr;
        this.mLteCqi = s.mLteCqi;
        this.mLteRsrpBoost = s.mLteRsrpBoost;
        this.mTdScdmaRscp = s.mTdScdmaRscp;
        this.isGsm = s.isGsm;
        this.mOEMLevel_0 = s.mOEMLevel_0;
        this.mOEMLevel_1 = s.mOEMLevel_1;
    }

    public SignalStrength(Parcel in) {
        boolean z = false;
        this.mOEMLevel_0 = OEM_LEVLE;
        this.mOEMLevel_1 = OEM_LEVLE;
        this.mGsmSignalStrength = in.readInt();
        this.mGsmBitErrorRate = in.readInt();
        this.mCdmaDbm = in.readInt();
        this.mCdmaEcio = in.readInt();
        this.mEvdoDbm = in.readInt();
        this.mEvdoEcio = in.readInt();
        this.mEvdoSnr = in.readInt();
        this.mLteSignalStrength = in.readInt();
        this.mLteRsrp = in.readInt();
        this.mLteRsrq = in.readInt();
        this.mLteRssnr = in.readInt();
        this.mLteCqi = in.readInt();
        this.mLteRsrpBoost = in.readInt();
        this.mTdScdmaRscp = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        }
        this.isGsm = z;
        this.mOEMLevel_0 = in.readInt();
        this.mOEMLevel_1 = in.readInt();
    }

    public static SignalStrength makeSignalStrengthFromRilParcel(Parcel in) {
        SignalStrength ss = new SignalStrength();
        ss.mGsmSignalStrength = in.readInt();
        ss.mGsmBitErrorRate = in.readInt();
        ss.mCdmaDbm = in.readInt();
        ss.mCdmaEcio = in.readInt();
        ss.mEvdoDbm = in.readInt();
        ss.mEvdoEcio = in.readInt();
        ss.mEvdoSnr = in.readInt();
        ss.mLteSignalStrength = in.readInt();
        ss.mLteRsrp = in.readInt();
        ss.mLteRsrq = in.readInt();
        ss.mLteRssnr = in.readInt();
        ss.mLteCqi = in.readInt();
        ss.mTdScdmaRscp = in.readInt();
        ss.mOEMLevel_0 = in.readInt();
        ss.mOEMLevel_1 = in.readInt();
        return ss;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mGsmSignalStrength);
        out.writeInt(this.mGsmBitErrorRate);
        out.writeInt(this.mCdmaDbm);
        out.writeInt(this.mCdmaEcio);
        out.writeInt(this.mEvdoDbm);
        out.writeInt(this.mEvdoEcio);
        out.writeInt(this.mEvdoSnr);
        out.writeInt(this.mLteSignalStrength);
        out.writeInt(this.mLteRsrp);
        out.writeInt(this.mLteRsrq);
        out.writeInt(this.mLteRssnr);
        out.writeInt(this.mLteCqi);
        out.writeInt(this.mLteRsrpBoost);
        out.writeInt(this.mTdScdmaRscp);
        out.writeInt(this.isGsm ? 1 : 0);
        out.writeInt(this.mOEMLevel_0);
        out.writeInt(this.mOEMLevel_1);
    }

    public int describeContents() {
        return 0;
    }

    public void validateInput() {
        int i;
        int i2 = 99;
        int i3 = -1;
        int i4 = -120;
        int i5 = Integer.MAX_VALUE;
        this.mGsmSignalStrength = this.mGsmSignalStrength >= 0 ? this.mGsmSignalStrength : 99;
        if (this.mCdmaDbm > 0) {
            i = -this.mCdmaDbm;
        } else {
            i = -120;
        }
        this.mCdmaDbm = i;
        this.mCdmaEcio = this.mCdmaEcio > 0 ? -this.mCdmaEcio : -160;
        if (this.mEvdoDbm > 0) {
            i4 = -this.mEvdoDbm;
        }
        this.mEvdoDbm = i4;
        if (this.mEvdoEcio >= 0) {
            i = -this.mEvdoEcio;
        } else {
            i = -1;
        }
        this.mEvdoEcio = i;
        if (this.mEvdoSnr > 0 && this.mEvdoSnr <= 8) {
            i3 = this.mEvdoSnr;
        }
        this.mEvdoSnr = i3;
        if (this.mLteSignalStrength >= 0) {
            i2 = this.mLteSignalStrength;
        }
        this.mLteSignalStrength = i2;
        if (this.mLteRsrp < 44 || this.mLteRsrp > 140) {
            i = Integer.MAX_VALUE;
        } else {
            i = -this.mLteRsrp;
        }
        this.mLteRsrp = i;
        if (this.mLteRsrq < 3 || this.mLteRsrq > 20) {
            i = Integer.MAX_VALUE;
        } else {
            i = -this.mLteRsrq;
        }
        this.mLteRsrq = i;
        if (this.mLteRssnr < -200 || this.mLteRssnr > 300) {
            i = Integer.MAX_VALUE;
        } else {
            i = this.mLteRssnr;
        }
        this.mLteRssnr = i;
        if (this.mTdScdmaRscp >= 25 && this.mTdScdmaRscp <= 120) {
            i5 = -this.mTdScdmaRscp;
        }
        this.mTdScdmaRscp = i5;
    }

    public void setGsm(boolean gsmFlag) {
        this.isGsm = gsmFlag;
    }

    public void setThreshRsrp(int[] threshRsrp) {
        this.mThreshRsrp = threshRsrp;
    }

    public void setLteRsrpBoost(int lteRsrpBoost) {
        this.mLteRsrpBoost = lteRsrpBoost;
    }

    public int getGsmSignalStrength() {
        if (getTdScdmaDbm() == Integer.MAX_VALUE || this.mGsmSignalStrength != 99) {
            return this.mGsmSignalStrength;
        }
        int tdsValue = (getTdScdmaDbm() + 113) / 2;
        if (tdsValue <= 0) {
            tdsValue = 0;
        }
        return tdsValue;
    }

    public int getGsmBitErrorRate() {
        return this.mGsmBitErrorRate;
    }

    public int getCdmaDbm() {
        return this.mCdmaDbm;
    }

    public int getCdmaEcio() {
        return this.mCdmaEcio;
    }

    public int getEvdoDbm() {
        return this.mEvdoDbm;
    }

    public int getEvdoEcio() {
        return this.mEvdoEcio;
    }

    public int getEvdoSnr() {
        return this.mEvdoSnr;
    }

    public int getLteSignalStrength() {
        return this.mLteSignalStrength;
    }

    public int getLteRsrp() {
        return this.mLteRsrp;
    }

    public int getLteRsrq() {
        return this.mLteRsrq;
    }

    public int getLteRssnr() {
        return this.mLteRssnr;
    }

    public int getLteCqi() {
        return this.mLteCqi;
    }

    public int getLteRsrpBoost() {
        return this.mLteRsrpBoost;
    }

    public int getLevel() {
        int level = OEM_LEVLE;
        if (this.isGsm) {
            level = getLteLevel();
            if (level != 0) {
                return level;
            }
            level = getTdScdmaLevel();
            if (level == 0) {
                return getGsmLevel();
            }
            return level;
        }
        int cdmaLevel = getCdmaLevel();
        int evdoLevel = getEvdoLevel();
        if (evdoLevel == 0) {
            return cdmaLevel;
        }
        if (cdmaLevel == 0) {
            return evdoLevel;
        }
        return cdmaLevel < evdoLevel ? cdmaLevel : evdoLevel;
    }

    public int getAsuLevel() {
        if (!this.isGsm) {
            int cdmaAsuLevel = getCdmaAsuLevel();
            int evdoAsuLevel = getEvdoAsuLevel();
            if (evdoAsuLevel == 0) {
                return cdmaAsuLevel;
            }
            if (cdmaAsuLevel == 0) {
                return evdoAsuLevel;
            }
            return cdmaAsuLevel < evdoAsuLevel ? cdmaAsuLevel : evdoAsuLevel;
        } else if (getLteLevel() != 0) {
            return getLteAsuLevel();
        } else {
            if (getTdScdmaLevel() == 0) {
                return getGsmAsuLevel();
            }
            return getTdScdmaAsuLevel();
        }
    }

    public int getDbm() {
        if (isGsm()) {
            int dBm = getLteDbm();
            if (dBm == Integer.MAX_VALUE) {
                if (getTdScdmaLevel() == 0) {
                    dBm = getGsmDbm();
                } else {
                    dBm = getTdScdmaDbm();
                }
            }
            return dBm;
        }
        int cdmaDbm = getCdmaDbm();
        int evdoDbm = getEvdoDbm();
        if (evdoDbm != -120) {
            if (cdmaDbm == -120) {
                cdmaDbm = evdoDbm;
            } else if (cdmaDbm >= evdoDbm) {
                cdmaDbm = evdoDbm;
            }
        }
        return cdmaDbm;
    }

    public int getGsmDbm() {
        int gsmSignalStrength = getGsmSignalStrength();
        int asu = gsmSignalStrength == 99 ? -1 : gsmSignalStrength;
        if (asu != -1) {
            return (asu * 2) - 113;
        }
        return -1;
    }

    public int getGsmLevel() {
        int asu = getGsmSignalStrength();
        if (asu <= 2 || asu == 99) {
            return 0;
        }
        if (asu >= 13) {
            return 4;
        }
        if (asu >= 10) {
            return 3;
        }
        if (asu >= 7) {
            return 2;
        }
        return 1;
    }

    public int getGsmAsuLevel() {
        return getGsmSignalStrength();
    }

    public int getCdmaLevel() {
        int levelDbm;
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
        if (cdmaDbm >= -89) {
            levelDbm = 4;
        } else if (cdmaDbm >= -100) {
            levelDbm = 3;
        } else if (cdmaDbm >= -106) {
            levelDbm = 2;
        } else if (cdmaDbm >= -109) {
            levelDbm = 1;
        } else {
            levelDbm = 0;
        }
        int level = levelDbm;
        return levelDbm;
    }

    public int getCdmaAsuLevel() {
        int cdmaAsuLevel;
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
        if (cdmaDbm >= -75) {
            cdmaAsuLevel = 16;
        } else if (cdmaDbm >= -82) {
            cdmaAsuLevel = 8;
        } else if (cdmaDbm >= -90) {
            cdmaAsuLevel = 4;
        } else if (cdmaDbm >= -95) {
            cdmaAsuLevel = 2;
        } else if (cdmaDbm >= -100) {
            cdmaAsuLevel = 1;
        } else {
            cdmaAsuLevel = 99;
        }
        int level = cdmaAsuLevel;
        return cdmaAsuLevel;
    }

    public int getEvdoLevel() {
        int levelEvdoDbm;
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
        if (evdoDbm >= -89) {
            levelEvdoDbm = 4;
        } else if (evdoDbm >= -100) {
            levelEvdoDbm = 3;
        } else if (evdoDbm >= -106) {
            levelEvdoDbm = 2;
        } else if (evdoDbm >= -110) {
            levelEvdoDbm = 1;
        } else {
            levelEvdoDbm = 0;
        }
        int level = levelEvdoDbm;
        return levelEvdoDbm;
    }

    public int getEvdoAsuLevel() {
        int levelEvdoDbm;
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
        if (evdoDbm >= -65) {
            levelEvdoDbm = 16;
        } else if (evdoDbm >= -75) {
            levelEvdoDbm = 8;
        } else if (evdoDbm >= -85) {
            levelEvdoDbm = 4;
        } else if (evdoDbm >= -95) {
            levelEvdoDbm = 2;
        } else if (evdoDbm >= -105) {
            levelEvdoDbm = 1;
        } else {
            levelEvdoDbm = 99;
        }
        int level = levelEvdoDbm;
        return levelEvdoDbm;
    }

    public int getLteDbm() {
        return this.mLteRsrp;
    }

    public int getLteLevel() {
        int rsrpIconLevel;
        int rssiIconLevel = 0;
        int snrIconLevel = -1;
        int threshLevel3 = OEM_LEVLE == 0 ? -105 : -110;
        int threshLevel2 = OEM_LEVLE == 0 ? -113 : -115;
        if (this.mLteRsrp > -44) {
            rsrpIconLevel = -1;
        } else if (this.mLteRsrp >= -97) {
            rsrpIconLevel = 4;
        } else if (this.mLteRsrp >= threshLevel3) {
            rsrpIconLevel = 3;
        } else if (this.mLteRsrp >= threshLevel2) {
            rsrpIconLevel = 2;
        } else if (this.mLteRsrp >= -120) {
            rsrpIconLevel = 1;
        } else {
            rsrpIconLevel = 0;
        }
        log("getLTELevel - rsrp = " + rsrpIconLevel);
        if (rsrpIconLevel != -1) {
            return rsrpIconLevel;
        }
        if (this.mLteRssnr > 300) {
            snrIconLevel = -1;
        } else if (this.mLteRssnr >= 130) {
            snrIconLevel = 4;
        } else if (this.mLteRssnr >= 45) {
            snrIconLevel = 3;
        } else if (this.mLteRssnr >= 10) {
            snrIconLevel = 2;
        } else if (this.mLteRssnr >= -30) {
            snrIconLevel = 1;
        } else if (this.mLteRssnr >= -200) {
            snrIconLevel = 0;
        }
        if (snrIconLevel != -1 && rsrpIconLevel != -1) {
            if (rsrpIconLevel >= snrIconLevel) {
                rsrpIconLevel = snrIconLevel;
            }
            return rsrpIconLevel;
        } else if (snrIconLevel != -1) {
            return snrIconLevel;
        } else {
            if (rsrpIconLevel != -1) {
                return rsrpIconLevel;
            }
            if (this.mLteSignalStrength > 63) {
                rssiIconLevel = 0;
            } else if (this.mLteSignalStrength >= 12) {
                rssiIconLevel = 4;
            } else if (this.mLteSignalStrength >= 8) {
                rssiIconLevel = 3;
            } else if (this.mLteSignalStrength >= 5) {
                rssiIconLevel = 2;
            } else if (this.mLteSignalStrength >= 0) {
                rssiIconLevel = 1;
            }
            return rssiIconLevel;
        }
    }

    public int getLteAsuLevel() {
        int lteDbm = getLteDbm();
        if (lteDbm == Integer.MAX_VALUE) {
            return 255;
        }
        return lteDbm + 140;
    }

    public boolean isGsm() {
        return this.isGsm;
    }

    public int getTdScdmaDbm() {
        return this.mTdScdmaRscp;
    }

    public int getTdScdmaLevel() {
        int tdScdmaDbm = getTdScdmaDbm();
        if (tdScdmaDbm > -25 || tdScdmaDbm == Integer.MAX_VALUE) {
            return 0;
        }
        if (tdScdmaDbm >= -91) {
            return 4;
        }
        if (tdScdmaDbm >= -97) {
            return 3;
        }
        if (tdScdmaDbm >= -99) {
            return 2;
        }
        if (tdScdmaDbm >= -120) {
            return 1;
        }
        return 0;
    }

    public int getTdScdmaAsuLevel() {
        int tdScdmaDbm = getTdScdmaDbm();
        if (tdScdmaDbm == Integer.MAX_VALUE) {
            return 255;
        }
        return tdScdmaDbm + 120;
    }

    public int hashCode() {
        int i;
        int i2 = (this.mTdScdmaRscp * 31) + (((((((((((((this.mGsmSignalStrength * 31) + (this.mGsmBitErrorRate * 31)) + (this.mCdmaDbm * 31)) + (this.mCdmaEcio * 31)) + (this.mEvdoDbm * 31)) + (this.mEvdoEcio * 31)) + (this.mEvdoSnr * 31)) + (this.mLteSignalStrength * 31)) + (this.mLteRsrp * 31)) + (this.mLteRsrq * 31)) + (this.mLteRssnr * 31)) + (this.mLteCqi * 31)) + (this.mLteRsrpBoost * 31));
        if (this.isGsm) {
            i = 1;
        } else {
            i = 0;
        }
        return i + i2;
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            SignalStrength s = (SignalStrength) o;
            if (o == null) {
                return false;
            }
            if (this.mGsmSignalStrength == s.mGsmSignalStrength && this.mGsmBitErrorRate == s.mGsmBitErrorRate && this.mCdmaDbm == s.mCdmaDbm && this.mCdmaEcio == s.mCdmaEcio && this.mEvdoDbm == s.mEvdoDbm && this.mEvdoEcio == s.mEvdoEcio && this.mEvdoSnr == s.mEvdoSnr && this.mLteSignalStrength == s.mLteSignalStrength && this.mLteRsrp == s.mLteRsrp && this.mLteRsrq == s.mLteRsrq && this.mLteRssnr == s.mLteRssnr && this.mLteCqi == s.mLteCqi && this.mLteRsrpBoost == s.mLteRsrpBoost && this.mTdScdmaRscp == s.mTdScdmaRscp && this.isGsm == s.isGsm) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("SignalStrength: ").append(this.mGsmSignalStrength).append(" ").append(this.mGsmBitErrorRate).append(" ").append(this.mCdmaDbm).append(" ").append(this.mCdmaEcio).append(" ").append(this.mEvdoDbm).append(" ").append(this.mEvdoEcio).append(" ").append(this.mEvdoSnr).append(" ").append(this.mLteSignalStrength).append(" ").append(this.mLteRsrp).append(" ").append(this.mLteRsrq).append(" ").append(this.mLteRssnr).append(" ").append(this.mLteCqi).append(" ").append(this.mLteRsrpBoost).append(" ").append(this.mTdScdmaRscp).append(" ").append(this.mOEMLevel_0).append(" ").append(this.mOEMLevel_1).append(" ");
        if (this.isGsm) {
            str = "gsm|lte";
        } else {
            str = "cdma";
        }
        return append.append(str).toString();
    }

    private void setFromNotifierBundle(Bundle m) {
        this.mGsmSignalStrength = m.getInt("GsmSignalStrength");
        this.mGsmBitErrorRate = m.getInt("GsmBitErrorRate");
        this.mCdmaDbm = m.getInt("CdmaDbm");
        this.mCdmaEcio = m.getInt("CdmaEcio");
        this.mEvdoDbm = m.getInt("EvdoDbm");
        this.mEvdoEcio = m.getInt("EvdoEcio");
        this.mEvdoSnr = m.getInt("EvdoSnr");
        this.mLteSignalStrength = m.getInt("LteSignalStrength");
        this.mLteRsrp = m.getInt("LteRsrp");
        this.mLteRsrq = m.getInt("LteRsrq");
        this.mLteRssnr = m.getInt("LteRssnr");
        this.mLteCqi = m.getInt("LteCqi");
        this.mLteRsrpBoost = m.getInt("lteRsrpBoost");
        this.mTdScdmaRscp = m.getInt("TdScdma");
        this.isGsm = m.getBoolean("isGsm");
        this.mOEMLevel_0 = m.getInt("OEMLevel_0");
        this.mOEMLevel_1 = m.getInt("OEMLevel_1");
    }

    public void fillInNotifierBundle(Bundle m) {
        m.putInt("GsmSignalStrength", this.mGsmSignalStrength);
        m.putInt("GsmBitErrorRate", this.mGsmBitErrorRate);
        m.putInt("CdmaDbm", this.mCdmaDbm);
        m.putInt("CdmaEcio", this.mCdmaEcio);
        m.putInt("EvdoDbm", this.mEvdoDbm);
        m.putInt("EvdoEcio", this.mEvdoEcio);
        m.putInt("EvdoSnr", this.mEvdoSnr);
        m.putInt("LteSignalStrength", this.mLteSignalStrength);
        m.putInt("LteRsrp", this.mLteRsrp);
        m.putInt("LteRsrq", this.mLteRsrq);
        m.putInt("LteRssnr", this.mLteRssnr);
        m.putInt("LteCqi", this.mLteCqi);
        m.putInt("lteRsrpBoost", this.mLteRsrpBoost);
        m.putInt("TdScdma", this.mTdScdmaRscp);
        m.putBoolean("isGsm", this.isGsm);
        m.putInt("OEMLevel_0", this.mOEMLevel_0);
        m.putInt("OEMLevel_1", this.mOEMLevel_1);
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }

    public int[] getColorOSLevel() {
        return new int[]{this.mOEMLevel_0, this.mOEMLevel_1};
    }

    public int getOEMLevel(int networktype) {
        int level = OEM_LEVLE;
        if (networktype == 1) {
            level = getGsmLevel(false);
            if (level > 0) {
                return level;
            }
            return OEM_LEVLE;
        } else if (networktype == 2) {
            level = getTdScdmaLevel();
            if (OEM_LEVLE != 1 || level > 1) {
                return level;
            }
            return 2;
        } else if (networktype == 3) {
            level = getLteLevel();
            if (level <= 0) {
                return OEM_LEVLE;
            }
            return level;
        } else if (networktype == 4) {
            level = getGsmLevel(true);
            if (OEM_LEVLE != 1) {
                return level;
            }
            if ((SystemProperties.get("ro.oppo.operator", "ex").equals("TELSTRA")) || level > 1) {
                return level;
            }
            return 2;
        } else if (networktype == 5) {
            level = getCdmaLevel();
            if (level <= 0) {
                return OEM_LEVLE;
            }
            return level;
        } else if (networktype == 6) {
            level = getEvdoLevel();
            if (level <= 0) {
                return OEM_LEVLE;
            }
            return level;
        } else if (networktype == 0) {
            return 0;
        } else {
            return level;
        }
    }

    public int getGsmLevel(boolean wcdma) {
        int asu = getGsmSignalStrength();
        int level;
        if (SystemProperties.get("ro.oppo.operator", "ex").equals("TELSTRA")) {
            if (asu < TelstraAsuCriterion[0] || asu == 99) {
                level = 0;
            } else if (wcdma ? asu >= TelstraAsuCriterion[3] : asu >= 13) {
                level = 4;
            } else if (wcdma ? asu >= TelstraAsuCriterion[2] : asu >= 8) {
                level = 3;
            } else if (wcdma ? asu >= TelstraAsuCriterion[1] : asu >= 5) {
                level = 2;
            } else {
                level = 1;
            }
            return level;
        }
        if (asu < 0 || asu == 99) {
            level = 0;
        } else if (wcdma ? asu >= 11 : asu >= 13) {
            level = 4;
        } else if (asu >= 8) {
            level = 3;
        } else if (wcdma ? asu >= 7 : asu >= 5) {
            level = 2;
        } else {
            level = 1;
        }
        return level;
    }
}

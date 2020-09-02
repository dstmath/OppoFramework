package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.hardware.radio.V1_0.WcdmaSignalStrength;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.text.TextUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public final class CellSignalStrengthWcdma extends CellSignalStrength implements Parcelable {
    public static final Parcelable.Creator<CellSignalStrengthWcdma> CREATOR = new Parcelable.Creator<CellSignalStrengthWcdma>() {
        /* class android.telephony.CellSignalStrengthWcdma.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellSignalStrengthWcdma createFromParcel(Parcel in) {
            return new CellSignalStrengthWcdma(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellSignalStrengthWcdma[] newArray(int size) {
            return new CellSignalStrengthWcdma[size];
        }
    };
    private static final boolean DBG = false;
    private static final String DEFAULT_LEVEL_CALCULATION_METHOD = "rssi";
    public static final String LEVEL_CALCULATION_METHOD_RSCP = "rscp";
    public static final String LEVEL_CALCULATION_METHOD_RSSI = "rssi";
    private static final String LOG_TAG = "CellSignalStrengthWcdma";
    private static final int WCDMA_RSCP_GOOD = -95;
    private static final int WCDMA_RSCP_GREAT = -85;
    private static final int WCDMA_RSCP_MAX = -24;
    private static final int WCDMA_RSCP_MIN = -120;
    private static final int WCDMA_RSCP_MODERATE = -105;
    private static final int WCDMA_RSCP_POOR = -115;
    private static final int WCDMA_RSSI_GOOD = -87;
    private static final int WCDMA_RSSI_GREAT = -77;
    private static final int WCDMA_RSSI_MAX = -51;
    private static final int WCDMA_RSSI_MIN = -113;
    private static final int WCDMA_RSSI_MODERATE = -97;
    private static final int WCDMA_RSSI_POOR = -107;
    private static final CellSignalStrengthWcdma sInvalid = new CellSignalStrengthWcdma();
    private static final int[] sRscpThresholds = {-115, -105, -95, WCDMA_RSCP_GREAT};
    private static final int[] sRssiThresholds = {-107, -97, WCDMA_RSSI_GOOD, WCDMA_RSSI_GREAT};
    @UnsupportedAppUsage
    private int mBitErrorRate;
    private int mEcNo;
    private int mLevel;
    private int mRscp;
    private int mRssi;

    @Retention(RetentionPolicy.SOURCE)
    public @interface LevelCalculationMethod {
    }

    public CellSignalStrengthWcdma() {
        setDefaultValues();
    }

    public CellSignalStrengthWcdma(int rssi, int ber, int rscp, int ecno) {
        this.mRssi = inRangeOrUnavailable(rssi, -113, -51);
        this.mBitErrorRate = inRangeOrUnavailable(ber, 0, 7, 99);
        this.mRscp = inRangeOrUnavailable(rscp, -120, -24);
        this.mEcNo = inRangeOrUnavailable(ecno, -24, 1);
        updateLevel(null, null);
    }

    public CellSignalStrengthWcdma(WcdmaSignalStrength wcdma) {
        this(getRssiDbmFromAsu(wcdma.signalStrength), wcdma.bitErrorRate, Integer.MAX_VALUE, Integer.MAX_VALUE);
        if (this.mRssi == Integer.MAX_VALUE && this.mRscp == Integer.MAX_VALUE) {
            setDefaultValues();
        }
    }

    public CellSignalStrengthWcdma(android.hardware.radio.V1_2.WcdmaSignalStrength wcdma) {
        this(getRssiDbmFromAsu(wcdma.base.signalStrength), wcdma.base.bitErrorRate, getRscpDbmFromAsu(wcdma.rscp), getEcNoDbFromAsu(wcdma.ecno));
        if (this.mRssi == Integer.MAX_VALUE && this.mRscp == Integer.MAX_VALUE) {
            setDefaultValues();
        }
    }

    public CellSignalStrengthWcdma(CellSignalStrengthWcdma s) {
        copyFrom(s);
    }

    /* access modifiers changed from: protected */
    public void copyFrom(CellSignalStrengthWcdma s) {
        this.mRssi = s.mRssi;
        this.mBitErrorRate = s.mBitErrorRate;
        this.mRscp = s.mRscp;
        this.mEcNo = s.mEcNo;
        this.mLevel = s.mLevel;
    }

    @Override // android.telephony.CellSignalStrength
    public CellSignalStrengthWcdma copy() {
        return new CellSignalStrengthWcdma(this);
    }

    @Override // android.telephony.CellSignalStrength
    public void setDefaultValues() {
        this.mRssi = Integer.MAX_VALUE;
        this.mBitErrorRate = Integer.MAX_VALUE;
        this.mRscp = Integer.MAX_VALUE;
        this.mEcNo = Integer.MAX_VALUE;
        this.mLevel = 0;
    }

    @Override // android.telephony.CellSignalStrength
    public int getLevel() {
        return this.mLevel;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0052  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0088  */
    @Override // android.telephony.CellSignalStrength
    public void updateLevel(PersistableBundle cc, ServiceState ss) {
        int[] rscpThresholds;
        String calcMethod;
        char c;
        if (cc == null) {
            calcMethod = "rssi";
            rscpThresholds = sRscpThresholds;
        } else {
            calcMethod = cc.getString(CarrierConfigManager.KEY_WCDMA_DEFAULT_SIGNAL_STRENGTH_MEASUREMENT_STRING, "rssi");
            if (TextUtils.isEmpty(calcMethod)) {
                calcMethod = "rssi";
            }
            rscpThresholds = cc.getIntArray(CarrierConfigManager.KEY_WCDMA_RSCP_THRESHOLDS_INT_ARRAY);
            if (rscpThresholds == null || rscpThresholds.length != 4) {
                rscpThresholds = sRscpThresholds;
            }
        }
        int level = 4;
        int hashCode = calcMethod.hashCode();
        if (hashCode != 3509870) {
            if (hashCode == 3510359 && calcMethod.equals("rssi")) {
                c = 2;
                if (c != 0) {
                    if (c != 2) {
                        loge("Invalid Level Calculation Method for CellSignalStrengthWcdma = " + calcMethod);
                    }
                    int i = this.mRssi;
                    if (i < -113 || i > -51) {
                        this.mLevel = 0;
                        return;
                    }
                    while (level > 0 && this.mRssi < sRssiThresholds[level - 1]) {
                        level--;
                    }
                    this.mLevel = level;
                    return;
                }
                int i2 = this.mRscp;
                if (i2 < -120 || i2 > -24) {
                    this.mLevel = 0;
                    return;
                }
                while (level > 0 && this.mRscp < rscpThresholds[level - 1]) {
                    level--;
                }
                this.mLevel = level;
                return;
            }
        } else if (calcMethod.equals(LEVEL_CALCULATION_METHOD_RSCP)) {
            c = 0;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    @Override // android.telephony.CellSignalStrength
    public int getDbm() {
        int i = this.mRscp;
        if (i != Integer.MAX_VALUE) {
            return i;
        }
        return this.mRssi;
    }

    @Override // android.telephony.CellSignalStrength
    public int getAsuLevel() {
        int i = this.mRscp;
        if (i != Integer.MAX_VALUE) {
            return getAsuFromRscpDbm(i);
        }
        int i2 = this.mRssi;
        if (i2 != Integer.MAX_VALUE) {
            return getAsuFromRssiDbm(i2);
        }
        return getAsuFromRscpDbm(Integer.MAX_VALUE);
    }

    public int getRssi() {
        return this.mRssi;
    }

    public int getRscp() {
        return this.mRscp;
    }

    public int getEcNo() {
        return this.mEcNo;
    }

    public int getBitErrorRate() {
        return this.mBitErrorRate;
    }

    @Override // android.telephony.CellSignalStrength
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mRssi), Integer.valueOf(this.mBitErrorRate), Integer.valueOf(this.mRscp), Integer.valueOf(this.mEcNo), Integer.valueOf(this.mLevel));
    }

    @Override // android.telephony.CellSignalStrength
    public boolean isValid() {
        return !equals(sInvalid);
    }

    @Override // android.telephony.CellSignalStrength
    public boolean equals(Object o) {
        if (!(o instanceof CellSignalStrengthWcdma)) {
            return false;
        }
        CellSignalStrengthWcdma s = (CellSignalStrengthWcdma) o;
        if (this.mRssi == s.mRssi && this.mBitErrorRate == s.mBitErrorRate && this.mRscp == s.mRscp && this.mEcNo == s.mEcNo && this.mLevel == s.mLevel) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "CellSignalStrengthWcdma: ss=" + this.mRssi + " ber=" + this.mBitErrorRate + " rscp=" + this.mRscp + " ecno=" + this.mEcNo + " level=" + this.mLevel;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mBitErrorRate);
        dest.writeInt(this.mRscp);
        dest.writeInt(this.mEcNo);
        dest.writeInt(this.mLevel);
    }

    private CellSignalStrengthWcdma(Parcel in) {
        this.mRssi = in.readInt();
        this.mBitErrorRate = in.readInt();
        this.mRscp = in.readInt();
        this.mEcNo = in.readInt();
        this.mLevel = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }

    private static void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }
}

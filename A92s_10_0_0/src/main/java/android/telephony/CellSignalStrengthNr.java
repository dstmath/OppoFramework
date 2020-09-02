package android.telephony;

import android.hardware.radio.V1_4.NrSignalStrength;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import java.util.Objects;

public final class CellSignalStrengthNr extends CellSignalStrength implements Parcelable {
    public static final Parcelable.Creator<CellSignalStrengthNr> CREATOR = new Parcelable.Creator<CellSignalStrengthNr>() {
        /* class android.telephony.CellSignalStrengthNr.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CellSignalStrengthNr createFromParcel(Parcel in) {
            return new CellSignalStrengthNr(in);
        }

        @Override // android.os.Parcelable.Creator
        public CellSignalStrengthNr[] newArray(int size) {
            return new CellSignalStrengthNr[size];
        }
    };
    private static final int SIGNAL_GOOD_THRESHOLD = -105;
    private static final int SIGNAL_GREAT_THRESHOLD = -95;
    private static final int SIGNAL_MODERATE_THRESHOLD = -115;
    private static final String TAG = "CellSignalStrengthNr";
    public static final int UNKNOWN_ASU_LEVEL = 99;
    private static final CellSignalStrengthNr sInvalid = new CellSignalStrengthNr();
    private int mCsiRsrp;
    private int mCsiRsrq;
    private int mCsiSinr;
    private int mLevel;
    private int mSsRsrp;
    private int mSsRsrq;
    private int mSsSinr;

    public CellSignalStrengthNr() {
        setDefaultValues();
    }

    public CellSignalStrengthNr(int csiRsrp, int csiRsrq, int csiSinr, int ssRsrp, int ssRsrq, int ssSinr) {
        this.mCsiRsrp = inRangeOrUnavailable(csiRsrp, -140, -44);
        this.mCsiRsrq = inRangeOrUnavailable(csiRsrq, -20, -3);
        this.mCsiSinr = inRangeOrUnavailable(csiSinr, -23, 23);
        this.mSsRsrp = inRangeOrUnavailable(ssRsrp, -140, -44);
        this.mSsRsrq = inRangeOrUnavailable(ssRsrq, -20, -3);
        this.mSsSinr = inRangeOrUnavailable(ssSinr, -23, 40);
        updateLevel(null, null);
    }

    public CellSignalStrengthNr(NrSignalStrength ss) {
        this(ss.csiRsrp, ss.csiRsrq, ss.csiSinr, ss.ssRsrp, ss.ssRsrq, ss.ssSinr);
    }

    public int getSsRsrp() {
        return this.mSsRsrp;
    }

    public int getSsRsrq() {
        return this.mSsRsrq;
    }

    public int getSsSinr() {
        return this.mSsSinr;
    }

    public int getCsiRsrp() {
        return this.mCsiRsrp;
    }

    public int getCsiRsrq() {
        return this.mCsiRsrq;
    }

    public int getCsiSinr() {
        return this.mCsiSinr;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCsiRsrp);
        dest.writeInt(this.mCsiRsrq);
        dest.writeInt(this.mCsiSinr);
        dest.writeInt(this.mSsRsrp);
        dest.writeInt(this.mSsRsrq);
        dest.writeInt(this.mSsSinr);
        dest.writeInt(this.mLevel);
    }

    private CellSignalStrengthNr(Parcel in) {
        this.mCsiRsrp = in.readInt();
        this.mCsiRsrq = in.readInt();
        this.mCsiSinr = in.readInt();
        this.mSsRsrp = in.readInt();
        this.mSsRsrq = in.readInt();
        this.mSsSinr = in.readInt();
        this.mLevel = in.readInt();
    }

    @Override // android.telephony.CellSignalStrength
    public void setDefaultValues() {
        this.mCsiRsrp = Integer.MAX_VALUE;
        this.mCsiRsrq = Integer.MAX_VALUE;
        this.mCsiSinr = Integer.MAX_VALUE;
        this.mSsRsrp = Integer.MAX_VALUE;
        this.mSsRsrq = Integer.MAX_VALUE;
        this.mSsSinr = Integer.MAX_VALUE;
        this.mLevel = 0;
    }

    @Override // android.telephony.CellSignalStrength
    public int getLevel() {
        return this.mLevel;
    }

    @Override // android.telephony.CellSignalStrength
    public void updateLevel(PersistableBundle cc, ServiceState ss) {
        int i = this.mSsRsrp;
        if (i == Integer.MAX_VALUE) {
            this.mLevel = 0;
        } else if (i >= -95) {
            this.mLevel = 4;
        } else if (i >= -105) {
            this.mLevel = 3;
        } else if (i >= -115) {
            this.mLevel = 2;
        } else {
            this.mLevel = 1;
        }
    }

    @Override // android.telephony.CellSignalStrength
    public int getAsuLevel() {
        int nrDbm = getDbm();
        if (nrDbm == Integer.MAX_VALUE) {
            return 99;
        }
        if (nrDbm <= -140) {
            return 0;
        }
        if (nrDbm >= -43) {
            return 97;
        }
        return nrDbm + 140;
    }

    @Override // android.telephony.CellSignalStrength
    public int getDbm() {
        return this.mSsRsrp;
    }

    public CellSignalStrengthNr(CellSignalStrengthNr s) {
        this.mCsiRsrp = s.mCsiRsrp;
        this.mCsiRsrq = s.mCsiRsrq;
        this.mCsiSinr = s.mCsiSinr;
        this.mSsRsrp = s.mSsRsrp;
        this.mSsRsrq = s.mSsRsrq;
        this.mSsSinr = s.mSsSinr;
        this.mLevel = s.mLevel;
    }

    @Override // android.telephony.CellSignalStrength
    public CellSignalStrengthNr copy() {
        return new CellSignalStrengthNr(this);
    }

    @Override // android.telephony.CellSignalStrength
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mCsiRsrp), Integer.valueOf(this.mCsiRsrq), Integer.valueOf(this.mCsiSinr), Integer.valueOf(this.mSsRsrp), Integer.valueOf(this.mSsRsrq), Integer.valueOf(this.mSsSinr), Integer.valueOf(this.mLevel));
    }

    @Override // android.telephony.CellSignalStrength
    public boolean isValid() {
        return !equals(sInvalid);
    }

    @Override // android.telephony.CellSignalStrength
    public boolean equals(Object obj) {
        if (!(obj instanceof CellSignalStrengthNr)) {
            return false;
        }
        CellSignalStrengthNr o = (CellSignalStrengthNr) obj;
        if (this.mCsiRsrp == o.mCsiRsrp && this.mCsiRsrq == o.mCsiRsrq && this.mCsiSinr == o.mCsiSinr && this.mSsRsrp == o.mSsRsrp && this.mSsRsrq == o.mSsRsrq && this.mSsSinr == o.mSsSinr && this.mLevel == o.mLevel) {
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CellSignalStrengthNr:{");
        sb.append(" csiRsrp = " + this.mCsiRsrp);
        sb.append(" csiRsrq = " + this.mCsiRsrq);
        sb.append(" csiSinr = " + this.mCsiSinr);
        sb.append(" ssRsrp = " + this.mSsRsrp);
        sb.append(" ssRsrq = " + this.mSsRsrq);
        sb.append(" ssSinr = " + this.mSsSinr);
        sb.append(" level = " + this.mLevel);
        sb.append(" }");
        return sb.toString();
    }
}

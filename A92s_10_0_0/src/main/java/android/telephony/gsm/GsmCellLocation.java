package android.telephony.gsm;

import android.annotation.UnsupportedAppUsage;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.SmsManager;

public class GsmCellLocation extends CellLocation {
    private int mCid;
    private int mLac;
    private int mPsc;

    public GsmCellLocation() {
        this.mLac = -1;
        this.mCid = -1;
        this.mPsc = -1;
    }

    public GsmCellLocation(Bundle bundle) {
        this.mLac = bundle.getInt("lac", -1);
        this.mCid = bundle.getInt("cid", -1);
        this.mPsc = bundle.getInt("psc", -1);
    }

    public int getLac() {
        return this.mLac;
    }

    public int getCid() {
        return this.mCid;
    }

    public int getPsc() {
        return this.mPsc;
    }

    @Override // android.telephony.CellLocation
    public void setStateInvalid() {
        this.mLac = -1;
        this.mCid = -1;
        this.mPsc = -1;
    }

    public void setLacAndCid(int lac, int cid) {
        this.mLac = lac;
        this.mCid = cid;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public void setPsc(int psc) {
        this.mPsc = psc;
    }

    public int hashCode() {
        return this.mLac ^ this.mCid;
    }

    public boolean equals(Object o) {
        try {
            GsmCellLocation s = (GsmCellLocation) o;
            if (o != null && equalsHandlesNulls(Integer.valueOf(this.mLac), Integer.valueOf(s.mLac)) && equalsHandlesNulls(Integer.valueOf(this.mCid), Integer.valueOf(s.mCid)) && equalsHandlesNulls(Integer.valueOf(this.mPsc), Integer.valueOf(s.mPsc))) {
                return true;
            }
            return false;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        return "[" + this.mLac + SmsManager.REGEX_PREFIX_DELIMITER + this.mCid + SmsManager.REGEX_PREFIX_DELIMITER + this.mPsc + "]";
    }

    private static boolean equalsHandlesNulls(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    @Override // android.telephony.CellLocation
    public void fillInNotifierBundle(Bundle m) {
        m.putInt("lac", this.mLac);
        m.putInt("cid", this.mCid);
        m.putInt("psc", this.mPsc);
    }

    @Override // android.telephony.CellLocation
    public boolean isEmpty() {
        return this.mLac == -1 && this.mCid == -1 && this.mPsc == -1;
    }
}

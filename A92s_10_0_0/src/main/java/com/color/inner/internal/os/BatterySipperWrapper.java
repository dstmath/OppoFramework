package com.color.inner.internal.os;

import android.batterySipper.OppoBaseBatterySipper;
import com.android.internal.os.BatterySipper;
import com.color.util.ColorTypeCastingHelper;

public class BatterySipperWrapper {
    private static final String TAG = "BatterySipperWrapper";
    private BatterySipper mSipper;

    BatterySipperWrapper(BatterySipper batterySipper) {
        this.mSipper = batterySipper;
    }

    private BatterySipperWrapper() {
    }

    public double sumPower() {
        return this.mSipper.sumPower();
    }

    public String getPkgName() {
        OppoBaseBatterySipper baseBatterySipper = typeCasting(this.mSipper);
        if (baseBatterySipper != null) {
            return baseBatterySipper.pkgName;
        }
        return null;
    }

    public double getScreenPowerMah() {
        return this.mSipper.screenPowerMah;
    }

    public double getTotalSmearedPowerMah() {
        return this.mSipper.totalSmearedPowerMah;
    }

    private static OppoBaseBatterySipper typeCasting(BatterySipper batterySipper) {
        return (OppoBaseBatterySipper) ColorTypeCastingHelper.typeCasting(OppoBaseBatterySipper.class, batterySipper);
    }

    public int getUid() {
        return this.mSipper.getUid();
    }
}

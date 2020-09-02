package com.android.internal.os;

import android.batterySipper.OppoBaseBatterySipper;
import android.content.pm.PackageManager;
import android.os.BatteryStats;
import android.os.OppoBaseBatteryStats;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.os.BatterySipper;
import com.color.util.ColorTypeCastingHelper;

public class ScreenPowerCalculator {
    private static final boolean DEBUG = false;
    private static final long MILLISEC_IN_HR = 3600000;
    public static final String PKGNAME_NO_PKG = "noPkg";
    private static final String TAG = "ScreenPowerCalculator";
    private PackageManager mPackageManager;
    private final double[] mPowerScreenFull;
    private final double mPowerScreenOn;
    public long mTotalForgActivieyTime = 0;
    public double mTotalScreenPower = 0.0d;

    public ScreenPowerCalculator(PowerProfile profile, PackageManager packageManager) {
        this.mPowerScreenOn = profile.getAveragePower(PowerProfile.POWER_SCREEN_ON);
        double powerScreenFull = profile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
        Log.d(TAG, "ScreenPowerCalculator: powerScreenFull=" + powerScreenFull + ", mPowerScreenOn=" + this.mPowerScreenOn);
        this.mPowerScreenFull = new double[5];
        for (int i = 0; i < 5; i++) {
            this.mPowerScreenFull[i] = (((double) (((float) i) + 0.5f)) * powerScreenFull) / 5.0d;
        }
        this.mPackageManager = packageManager;
    }

    public void calculateApps(SparseArray<ArrayMap<String, BatterySipper>> listSippers, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        ArrayMap<String, BatterySipper> uidSippers;
        ArrayMap<String, ? extends BatteryStats.Uid.Pkg> packageStats;
        BatteryStats.Uid uid = u;
        if (listSippers == null) {
            Log.d(TAG, "calculateApps: listSippers is null!!!");
            return;
        }
        ArrayMap<String, BatterySipper> uidSippers2 = listSippers.get(u.getUid());
        if (uidSippers2 == null) {
            uidSippers2 = new ArrayMap<>();
            listSippers.put(u.getUid(), uidSippers2);
        }
        ArrayMap<String, ? extends BatteryStats.Uid.Pkg> packageStats2 = u.getPackageStats();
        double d = 0.0d;
        if (packageStats2.size() == 0) {
            BatterySipper app = new BatterySipper(BatterySipper.DrainType.APP, uid, 0.0d);
            OppoBaseBatterySipper baseApp = typeCasting(app);
            String pkgName = this.mPackageManager.getNameForUid(u.getUid());
            if (pkgName == null) {
                pkgName = PKGNAME_NO_PKG;
            }
            app.packageWithHighestDrain = pkgName;
            if (baseApp != null) {
                baseApp.pkgName = pkgName;
            }
            uidSippers2.put(pkgName, app);
            return;
        }
        int ipks = 0;
        while (ipks < packageStats2.size()) {
            String pkgName2 = packageStats2.keyAt(ipks);
            OppoBaseBatteryStats.OppoBaseUid.OppoBasePkg basePkg = typeCasting((BatteryStats.Uid.Pkg) packageStats2.valueAt(ipks));
            BatterySipper app2 = new BatterySipper(BatterySipper.DrainType.APP, uid, d);
            OppoBaseBatterySipper baseApp2 = typeCasting(app2);
            app2.packageWithHighestDrain = pkgName2;
            if (baseApp2 != null) {
                baseApp2.pkgName = pkgName2;
            }
            uidSippers2.put(pkgName2, app2);
            int i = 0;
            long totalScreenTime = 0;
            double power = 0.0d;
            while (i < 5) {
                long brightnessTime = 0;
                if (basePkg != null) {
                    uidSippers = uidSippers2;
                    brightnessTime = basePkg.getScreenBrightnessTime(i, rawRealtimeUs, statsType) / 1000;
                } else {
                    uidSippers = uidSippers2;
                }
                if (brightnessTime <= 0) {
                    packageStats = packageStats2;
                } else {
                    packageStats = packageStats2;
                    power += this.mPowerScreenFull[i] * ((double) brightnessTime);
                    totalScreenTime += brightnessTime;
                }
                i++;
                uidSippers2 = uidSippers;
                packageStats2 = packageStats;
            }
            if (totalScreenTime > 0) {
                power += this.mPowerScreenOn * ((double) totalScreenTime);
            }
            double power2 = power / 3600000.0d;
            app2.screenPowerMah = power2;
            if (baseApp2 != null) {
                baseApp2.screenHoldTimeMs = totalScreenTime;
            }
            this.mTotalScreenPower += power2;
            this.mTotalForgActivieyTime += totalScreenTime;
            ipks++;
            uid = u;
            uidSippers2 = uidSippers2;
            packageStats2 = packageStats2;
            d = 0.0d;
        }
    }

    private OppoBaseBatterySipper typeCasting(BatterySipper bs) {
        if (bs != null) {
            return (OppoBaseBatterySipper) ColorTypeCastingHelper.typeCasting(OppoBaseBatterySipper.class, bs);
        }
        return null;
    }

    private OppoBaseBatteryStats.OppoBaseUid.OppoBasePkg typeCasting(BatteryStats.Uid.Pkg pkg) {
        if (pkg != null) {
            return (OppoBaseBatteryStats.OppoBaseUid.OppoBasePkg) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.OppoBasePkg.class, pkg);
        }
        return null;
    }
}

package android.batterySipper;

import android.os.Bundle;

public class OppoBaseBatterySipper {
    public static final String BundleCameraBgPowerMah = "cameraBgPowerMah";
    public static final String BundleCameraBgTimeMs = "cameraBgTimeMs";
    public static final String BundleCpuBgPowerMah = "cpuBgPowerMah";
    public static final String BundleGpsBgPowerMah = "gpsBgPowerMah";
    public static final String BundleGpsBgTimeMs = "gpsBgTimeMs";
    public static final String BundleIsSharedUid = "isSharedUid";
    public static final String BundleIsSharedUidHighestDrain = "isSharedUidHighestDrain";
    public static final String BundleMobileBgTtafficBytes = "mobileBgTtafficBytes";
    public static final String BundleMobileRadioBgPowerMah = "mobileRadioBgPowerMah";
    public static final String BundlePkgName = "pkgName";
    public static final String BundleScreenHoldTimeMs = "screenHoldTimeMs";
    public static final String BundleSensorBgPowerMah = "sensorBgPowerMah";
    public static final String BundleSensorBgTimeMs = "sensorBgTimeMs";
    public static final String BundleSensorTimeMs = "sensorTimeMs";
    public static final String BundleWifiBgPowerMah = "wifiBgPowerMah";
    public static final String BundleWifiBgTtafficBytes = "wifiBgTtafficBytes";
    public double cameraBgPowerMah;
    public long cameraBgTimeMs;
    public double cpuBgPowerMah;
    public double gpsBgPowerMah;
    public long gpsBgTimeMs;
    public boolean isSharedUid = false;
    public boolean isSharedUidHighestDrain = false;
    public Bundle mSipperInfo = new Bundle();
    public long mobileBgTtafficBytes;
    public double mobileRadioBgPowerMah;
    public String pkgName;
    public long screenHoldTimeMs;
    public double sensorBgPowerMah;
    public long sensorBgTimeMs;
    public long sensorTimeMs;
    public double wifiBgPowerMah;
    public long wifiBgTtafficBytes;

    public Bundle getBundleData() {
        this.mSipperInfo.putLong(BundleGpsBgTimeMs, this.gpsBgTimeMs);
        this.mSipperInfo.putLong(BundleSensorTimeMs, this.sensorTimeMs);
        this.mSipperInfo.putLong(BundleSensorBgTimeMs, this.sensorBgTimeMs);
        this.mSipperInfo.putLong(BundleScreenHoldTimeMs, this.screenHoldTimeMs);
        this.mSipperInfo.putLong(BundleCameraBgTimeMs, this.cameraBgTimeMs);
        this.mSipperInfo.putLong(BundleWifiBgTtafficBytes, this.wifiBgTtafficBytes);
        this.mSipperInfo.putLong(BundleMobileBgTtafficBytes, this.mobileBgTtafficBytes);
        this.mSipperInfo.putDouble(BundleCpuBgPowerMah, this.cpuBgPowerMah);
        this.mSipperInfo.putDouble(BundleWifiBgPowerMah, this.wifiBgPowerMah);
        this.mSipperInfo.putDouble(BundleMobileRadioBgPowerMah, this.mobileRadioBgPowerMah);
        this.mSipperInfo.putDouble(BundleGpsBgPowerMah, this.gpsBgPowerMah);
        this.mSipperInfo.putDouble(BundleSensorBgPowerMah, this.sensorBgPowerMah);
        this.mSipperInfo.putDouble(BundleCameraBgPowerMah, this.cameraBgPowerMah);
        this.mSipperInfo.putString(BundlePkgName, this.pkgName);
        this.mSipperInfo.putBoolean(BundleIsSharedUid, this.isSharedUid);
        this.mSipperInfo.putBoolean(BundleIsSharedUidHighestDrain, this.isSharedUidHighestDrain);
        return this.mSipperInfo;
    }

    public void setBundleData(Bundle bundle) {
        if (bundle != null) {
            this.gpsBgTimeMs = bundle.getLong(BundleGpsBgTimeMs);
            this.sensorTimeMs = bundle.getLong(BundleSensorTimeMs);
            this.sensorBgTimeMs = bundle.getLong(BundleSensorBgTimeMs);
            this.screenHoldTimeMs = bundle.getLong(BundleScreenHoldTimeMs);
            this.cameraBgTimeMs = bundle.getLong(BundleCameraBgTimeMs);
            this.wifiBgTtafficBytes = bundle.getLong(BundleWifiBgTtafficBytes);
            this.mobileBgTtafficBytes = bundle.getLong(BundleMobileBgTtafficBytes);
            this.cpuBgPowerMah = bundle.getDouble(BundleCpuBgPowerMah);
            this.wifiBgPowerMah = bundle.getDouble(BundleWifiBgPowerMah);
            this.mobileRadioBgPowerMah = bundle.getDouble(BundleMobileRadioBgPowerMah);
            this.gpsBgPowerMah = bundle.getDouble(BundleGpsBgPowerMah);
            this.sensorBgPowerMah = bundle.getDouble(BundleSensorBgPowerMah);
            this.cameraBgPowerMah = bundle.getDouble(BundleCameraBgPowerMah);
            this.pkgName = bundle.getString(BundlePkgName);
            this.isSharedUid = bundle.getBoolean(BundleIsSharedUid);
            this.isSharedUidHighestDrain = bundle.getBoolean(BundleIsSharedUidHighestDrain);
        }
    }
}

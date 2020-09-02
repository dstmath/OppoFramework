package com.android.internal.os;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.BatteryStats;
import android.util.SparseArray;
import java.util.List;

public class SensorPowerCalculator extends PowerCalculator {
    private final double mGpsPower;
    private final List<Sensor> mSensors;

    public SensorPowerCalculator(PowerProfile profile, SensorManager sensorManager, BatteryStats stats, long rawRealtimeUs, int statsType) {
        this.mSensors = sensorManager.getSensorList(-1);
        this.mGpsPower = getAverageGpsPower(profile, stats, rawRealtimeUs, statsType);
    }

    @Override // com.android.internal.os.PowerCalculator
    public void calculateApp(BatterySipper app, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        long sensorBgTime;
        int NSE;
        SparseArray<? extends BatteryStats.Uid.Sensor> sensorStats;
        long j = rawRealtimeUs;
        int i = statsType;
        SparseArray<? extends BatteryStats.Uid.Sensor> sensorStats2 = u.getSensorStats();
        int NSE2 = sensorStats2.size();
        int ise = 0;
        while (ise < NSE2) {
            BatteryStats.Uid.Sensor sensor = (BatteryStats.Uid.Sensor) sensorStats2.valueAt(ise);
            int sensorHandle = sensorStats2.keyAt(ise);
            long sensorTime = sensor.getSensorTime().getTotalTimeLocked(j, i) / 1000;
            BatteryStats.Timer timerBg = sensor.getSensorBackgroundTime();
            if (timerBg != null) {
                sensorBgTime = timerBg.getTotalTimeLocked(j, i) / 1000;
            } else {
                sensorBgTime = 0;
            }
            if (sensorHandle != -10000) {
                int sensorsCount = this.mSensors.size();
                int i2 = 0;
                while (true) {
                    if (i2 >= sensorsCount) {
                        sensorStats = sensorStats2;
                        NSE = NSE2;
                        break;
                    }
                    Sensor s = this.mSensors.get(i2);
                    if (s.getHandle() == sensorHandle) {
                        sensorStats = sensorStats2;
                        NSE = NSE2;
                        app.sensorPowerMah += (double) ((((float) sensorTime) * s.getPower()) / 3600000.0f);
                        app.sensorTimeMs += sensorTime;
                        app.sensorBgTimeMs += sensorBgTime;
                        app.sensorBgPowerMah += (double) ((((float) sensorBgTime) * s.getPower()) / 3600000.0f);
                        break;
                    }
                    i2++;
                    sensorsCount = sensorsCount;
                }
            } else {
                sensorStats = sensorStats2;
                NSE = NSE2;
                app.gpsTimeMs = sensorTime;
                app.gpsPowerMah = (((double) app.gpsTimeMs) * this.mGpsPower) / 3600000.0d;
                app.gpsBgTimeMs = sensorBgTime;
                app.gpsBgPowerMah = (((double) app.gpsBgTimeMs) * this.mGpsPower) / 3600000.0d;
            }
            ise++;
            j = rawRealtimeUs;
            i = statsType;
            sensorStats2 = sensorStats;
            NSE2 = NSE;
        }
    }

    private double getAverageGpsPower(PowerProfile profile, BatteryStats stats, long rawRealtimeUs, int statsType) {
        PowerProfile powerProfile = profile;
        double averagePower = powerProfile.getAveragePowerOrDefault(PowerProfile.POWER_GPS_ON, -1.0d);
        if (averagePower != -1.0d) {
            return averagePower;
        }
        double averagePower2 = 0.0d;
        long totalTime = 0;
        double totalPower = 0.0d;
        int i = 0;
        while (i < 2) {
            long timePerLevel = stats.getGpsSignalQualityTime(i, rawRealtimeUs, statsType);
            totalTime += timePerLevel;
            totalPower += powerProfile.getAveragePower(PowerProfile.POWER_GPS_SIGNAL_QUALITY_BASED, i) * ((double) timePerLevel);
            i++;
            powerProfile = profile;
            averagePower2 = averagePower2;
        }
        if (totalTime != 0) {
            return totalPower / ((double) totalTime);
        }
        return averagePower2;
    }
}

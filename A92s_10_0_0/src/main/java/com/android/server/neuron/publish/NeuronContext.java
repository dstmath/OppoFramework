package com.android.server.neuron.publish;

import android.hardware.broadcastradio.V2_0.Constants;

public final class NeuronContext {
    private static NeoConfig sNeoConfig = new NeoConfig();
    private static SystemStatus sSystemStatus = new SystemStatus();

    public static NeoConfig getNeoConfig() {
        return sNeoConfig;
    }

    public static SystemStatus getSystemStatus() {
        return sSystemStatus;
    }

    public static final class NeoConfig {
        private static final int MAX_PERIOD = 3600000;
        private static final int MIN_PERIOD = 60000;
        private int mGpsUpdatePeriod = 10000;
        private int mRssiUpdatePeriod = 5000;
        private int mSensorUpdatePeriod = Constants.LIST_COMPLETE_TIMEOUT_MS;

        private boolean checkPeriod(int period) {
            return period >= 60000 && period <= 3600000;
        }

        public synchronized void setRssiUpdatePeriod(int period) {
            if (checkPeriod(period)) {
                this.mRssiUpdatePeriod = period;
            }
        }

        public synchronized int getRssiUpdatePeriod() {
            return this.mRssiUpdatePeriod;
        }

        public synchronized void setGpsUpdatePeriod(int period) {
            if (checkPeriod(period)) {
                this.mGpsUpdatePeriod = period;
            }
        }

        public synchronized int getGpsUpdatePeriod() {
            return this.mGpsUpdatePeriod;
        }

        public synchronized void setSensorUpdatePeriod(int period) {
            if (checkPeriod(period)) {
                this.mSensorUpdatePeriod = period;
            }
        }

        public synchronized int getSensorUpdatePeriod() {
            return this.mSensorUpdatePeriod;
        }
    }

    public static final class SystemStatus {
        private int mBatteryLevel = 100;
        private boolean mCharging = false;
        private String mForegroundApp = "";
        private String mIfaceName = "";
        private int mNetworkType = -1;
        private boolean mScreenOn = false;
        private String mWifibssid = "";
        private String mWifissid = "";

        public synchronized void setNetworkType(int type) {
            this.mNetworkType = type;
        }

        public synchronized int getNetworkType() {
            return this.mNetworkType;
        }

        public synchronized void setForegroundApp(String pkg) {
            this.mForegroundApp = pkg;
        }

        public synchronized String getForegroundApp() {
            return this.mForegroundApp;
        }

        public synchronized void setWifissid(String ssid) {
            this.mWifissid = ssid;
        }

        public synchronized String getWifissid() {
            return this.mWifissid;
        }

        public synchronized void setWifiBssid(String bssid) {
            this.mWifibssid = bssid;
        }

        public synchronized String getWifiBssid() {
            return this.mWifibssid;
        }

        public synchronized void setIfaceName(String name) {
            this.mIfaceName = name;
        }

        public synchronized String getIfaceName() {
            return this.mIfaceName;
        }

        public synchronized boolean isCharging() {
            return this.mCharging;
        }

        public synchronized void setChargingState(boolean charging) {
            this.mCharging = charging;
        }

        public synchronized int getBatteryLevel() {
            return this.mBatteryLevel;
        }

        public synchronized void setBatteryLevel(int level) {
            this.mBatteryLevel = level;
        }

        public synchronized boolean getScreenOn() {
            return this.mScreenOn;
        }

        public synchronized void setScreenOn(boolean state) {
            this.mScreenOn = state;
        }
    }
}

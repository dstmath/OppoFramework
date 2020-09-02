package com.android.server.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WifiSettingsStore {
    static final int WIFI_DISABLED = 0;
    private static final int WIFI_DISABLED_AIRPLANE_ON = 3;
    static final int WIFI_ENABLED = 1;
    private static final int WIFI_ENABLED_AIRPLANE_OVERRIDE = 2;
    private boolean mAirplaneModeOn = false;
    private boolean mCheckSavedStateAtBoot = false;
    private final Context mContext;
    private int mPersistWifiState = 0;
    private boolean mScanAlwaysAvailable;

    WifiSettingsStore(Context context) {
        this.mContext = context;
        this.mAirplaneModeOn = getPersistedAirplaneModeOn();
        this.mPersistWifiState = getPersistedWifiState();
        this.mScanAlwaysAvailable = getPersistedScanAlwaysAvailable();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001d, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0025, code lost:
        return r1;
     */
    public synchronized boolean isWifiToggleEnabled() {
        boolean z = true;
        if (!this.mCheckSavedStateAtBoot) {
            this.mCheckSavedStateAtBoot = true;
            if (testAndClearWifiSavedState()) {
                return true;
            }
        }
        if (this.mAirplaneModeOn) {
            if (this.mPersistWifiState != 2) {
                z = false;
            }
        } else if (this.mPersistWifiState == 0) {
            z = false;
        }
    }

    public synchronized boolean isAirplaneModeOn() {
        return this.mAirplaneModeOn;
    }

    public synchronized boolean isScanAlwaysAvailable() {
        return !this.mAirplaneModeOn && this.mScanAlwaysAvailable;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0022, code lost:
        return true;
     */
    public synchronized boolean handleWifiToggled(boolean wifiEnabled) {
        if (this.mAirplaneModeOn && !isAirplaneToggleable()) {
            return false;
        }
        if (!wifiEnabled) {
            persistWifiState(0);
        } else if (this.mAirplaneModeOn) {
            persistWifiState(2);
        } else {
            persistWifiState(1);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0031, code lost:
        return true;
     */
    public synchronized boolean handleAirplaneModeToggled() {
        if (!isAirplaneSensitive()) {
            return false;
        }
        this.mAirplaneModeOn = getPersistedAirplaneModeOn();
        if (this.mAirplaneModeOn) {
            if (this.mPersistWifiState == 1) {
                persistWifiState(3);
            }
        } else if (testAndClearWifiSavedState() || this.mPersistWifiState == 2 || this.mPersistWifiState == 3) {
            persistWifiState(1);
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void handleWifiScanAlwaysAvailableToggled() {
        this.mScanAlwaysAvailable = getPersistedScanAlwaysAvailable();
    }

    /* access modifiers changed from: package-private */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mPersistWifiState " + this.mPersistWifiState);
        pw.println("mAirplaneModeOn " + this.mAirplaneModeOn);
    }

    private void persistWifiState(int state) {
        ContentResolver cr = this.mContext.getContentResolver();
        this.mPersistWifiState = state;
        Settings.Global.putInt(cr, "wifi_on", state);
    }

    private boolean isAirplaneSensitive() {
        String airplaneModeRadios = Settings.Global.getString(this.mContext.getContentResolver(), "airplane_mode_radios");
        if ("1".equals(SystemProperties.get("oppo.nfc.test", "0"))) {
            Log.i("WifiSettingsStore", "oppo.nfc.test is running,directly running");
            return false;
        } else if (airplaneModeRadios == null || airplaneModeRadios.contains("wifi")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isAirplaneToggleable() {
        String toggleableRadios = Settings.Global.getString(this.mContext.getContentResolver(), "airplane_mode_toggleable_radios");
        return toggleableRadios != null && toggleableRadios.contains("wifi");
    }

    private boolean testAndClearWifiSavedState() {
        int wifiSavedState = getWifiSavedState();
        if (wifiSavedState == 1) {
            setWifiSavedState(0);
        }
        return wifiSavedState == 1;
    }

    public void setWifiSavedState(int state) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "wifi_saved_state", state);
    }

    public int getWifiSavedState() {
        try {
            return Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_saved_state");
        } catch (Settings.SettingNotFoundException e) {
            return 0;
        }
    }

    private int getPersistedWifiState() {
        ContentResolver cr = this.mContext.getContentResolver();
        try {
            return Settings.Global.getInt(cr, "wifi_on");
        } catch (Settings.SettingNotFoundException e) {
            Settings.Global.putInt(cr, "wifi_on", 0);
            return 0;
        }
    }

    private boolean getPersistedAirplaneModeOn() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    private boolean getPersistedScanAlwaysAvailable() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) == 1;
    }
}

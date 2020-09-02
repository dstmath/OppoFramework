package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WifiCountryCode {
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
    private static final String PROPERTY_OPERATOR_OPPO_REGION = "persist.sys.oppo.region";
    private static final String TAG = "WifiCountryCode";
    private boolean DBG;
    private Context mContext;
    private String mDefaultCountryCode;
    private String mDriverCountryCode;
    private String mDriverCountryTimestamp;
    private boolean mReady;
    private String mReadyTimestamp;
    private boolean mRevertCountryCodeOnCellularLoss;
    private String mTelephonyCountryCode;
    private String mTelephonyCountryTimestamp;
    private final WifiNative mWifiNative;

    public WifiCountryCode(WifiNative wifiNative, String oemDefaultCountryCode, boolean revertCountryCodeOnCellularLoss) {
        this.DBG = false;
        this.mReady = false;
        this.mDefaultCountryCode = null;
        this.mTelephonyCountryCode = null;
        this.mDriverCountryCode = null;
        this.mTelephonyCountryTimestamp = null;
        this.mDriverCountryTimestamp = null;
        this.mReadyTimestamp = null;
        this.mContext = null;
        this.mWifiNative = wifiNative;
        this.mRevertCountryCodeOnCellularLoss = revertCountryCodeOnCellularLoss;
        if (!TextUtils.isEmpty(oemDefaultCountryCode)) {
            this.mDefaultCountryCode = oemDefaultCountryCode.toUpperCase(Locale.US);
        } else if (this.mRevertCountryCodeOnCellularLoss) {
            Log.w(TAG, "config_wifi_revert_country_code_on_cellular_loss is set, but there is no default country code.");
            this.mRevertCountryCodeOnCellularLoss = false;
        }
        Log.d(TAG, "mDefaultCountryCode " + this.mDefaultCountryCode + " mRevertCountryCodeOnCellularLoss " + this.mRevertCountryCodeOnCellularLoss);
    }

    public WifiCountryCode(WifiNative wifiNative, String oemDefaultCountryCode, boolean revertCountryCodeOnCellularLoss, Context context) {
        this(wifiNative, oemDefaultCountryCode, revertCountryCodeOnCellularLoss);
        this.mContext = context;
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.DBG = true;
        } else {
            this.DBG = false;
        }
    }

    public synchronized void airplaneModeEnabled() {
        Log.d(TAG, "Airplane Mode Enabled");
        this.mTelephonyCountryCode = null;
        sendCountryCodeChangedBroadcast();
    }

    public synchronized void setReadyForChange(boolean ready) {
        this.mReady = ready;
        this.mReadyTimestamp = FORMATTER.format(new Date(System.currentTimeMillis()));
        if (this.mReady) {
            updateCountryCode();
        }
    }

    public synchronized boolean setCountryCode(String countryCode) {
        Log.d(TAG, "Receive set country code request: " + countryCode);
        this.mTelephonyCountryTimestamp = FORMATTER.format(new Date(System.currentTimeMillis()));
        if (!TextUtils.isEmpty(countryCode)) {
            this.mTelephonyCountryCode = countryCode.toUpperCase(Locale.US);
        } else if (this.mRevertCountryCodeOnCellularLoss) {
            Log.d(TAG, "Received empty country code, reset to default country code");
            this.mTelephonyCountryCode = null;
        }
        if (this.mReady) {
            updateCountryCode();
        } else {
            Log.d(TAG, "skip update supplicant not ready yet");
        }
        sendCountryCodeChangedBroadcast();
        return true;
    }

    public synchronized String getCountryCodeSentToDriver() {
        return this.mDriverCountryCode;
    }

    public synchronized String getCountryCode() {
        return pickCountryCode();
    }

    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mRevertCountryCodeOnCellularLoss: " + this.mRevertCountryCodeOnCellularLoss);
        pw.println("mDefaultCountryCode: " + this.mDefaultCountryCode);
        pw.println("mDriverCountryCode: " + this.mDriverCountryCode);
        pw.println("mTelephonyCountryCode: " + this.mTelephonyCountryCode);
        pw.println("mTelephonyCountryTimestamp: " + this.mTelephonyCountryTimestamp);
        pw.println("mDriverCountryTimestamp: " + this.mDriverCountryTimestamp);
        pw.println("mReadyTimestamp: " + this.mReadyTimestamp);
        pw.println("mReady: " + this.mReady);
    }

    private void updateCountryCode() {
        String country = pickCountryCode();
        Log.d(TAG, "updateCountryCode to " + country);
        if (country != null) {
            setCountryCodeNative(country);
        }
    }

    private String pickCountryCode() {
        String str = this.mTelephonyCountryCode;
        if (str != null) {
            return str;
        }
        String str2 = this.mDefaultCountryCode;
        if (str2 != null) {
            return str2;
        }
        String countryCode = SystemProperties.get(PROPERTY_OPERATOR_OPPO_REGION);
        if (!TextUtils.isEmpty(countryCode)) {
            return countryCode;
        }
        return null;
    }

    private boolean setCountryCodeNative(String country) {
        this.mDriverCountryTimestamp = FORMATTER.format(new Date(System.currentTimeMillis()));
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative.setCountryCode(wifiNative.getClientInterfaceName(), country)) {
            Log.d(TAG, "Succeeded to set country code to: " + country);
            this.mDriverCountryCode = country;
            return true;
        }
        Log.d(TAG, "Failed to set country code to: " + country);
        return false;
    }

    private void sendCountryCodeChangedBroadcast() {
        if (this.mContext != null) {
            Log.d(TAG, "sending WIFI_COUNTRY_CODE_CHANGED_ACTION");
            Intent intent = new Intent("android.net.wifi.COUNTRY_CODE_CHANGED");
            intent.addFlags(67108864);
            intent.putExtra("country_code", getCountryCode());
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }
}

package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

public class WifiCountryCode {
    static final String PROPERTY_OPERATOR_ISO_COUNTRY = "gsm.operator.iso-country";
    private static final String TAG = "WifiCountryCode";
    private boolean DBG = false;
    private Context mContext;
    private String mCurrentCountryCode = null;
    private String mDefaultCountryCode = null;
    private boolean mReady = false;
    private boolean mRevertCountryCodeOnCellularLoss;
    private String mTelephonyCountryCode = null;
    private final WifiNative mWifiNative;

    public WifiCountryCode(Context context, WifiNative wifiNative, String oemDefaultCountryCode, boolean revertCountryCodeOnCellularLoss) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mRevertCountryCodeOnCellularLoss = revertCountryCodeOnCellularLoss;
        if (!TextUtils.isEmpty(oemDefaultCountryCode)) {
            this.mDefaultCountryCode = oemDefaultCountryCode.toUpperCase();
        } else if (this.mRevertCountryCodeOnCellularLoss) {
            Log.w(TAG, "config_wifi_revert_country_code_on_cellular_loss is set, but there is no default country code.");
            this.mRevertCountryCodeOnCellularLoss = false;
            return;
        }
        if (this.mRevertCountryCodeOnCellularLoss) {
            Log.d(TAG, "Country code will be reverted to " + this.mDefaultCountryCode + " on MCC loss");
        }
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.DBG = true;
        } else {
            this.DBG = false;
        }
    }

    private void sendCountryCodeChangedBroadcast() {
        Log.d(TAG, "sending WIFI_COUNTRY_CODE_CHANGED_ACTION");
        Intent intent = new Intent("android.net.wifi.COUNTRY_CODE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("country_code", getCountryCode());
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public synchronized void simCardRemoved() {
        if (this.DBG) {
            Log.d(TAG, "SIM Card Removed");
        }
        this.mTelephonyCountryCode = null;
        if (this.mReady) {
            updateCountryCode();
        }
        sendCountryCodeChangedBroadcast();
    }

    public synchronized void airplaneModeEnabled() {
        if (this.DBG) {
            Log.d(TAG, "Airplane Mode Enabled");
        }
        this.mTelephonyCountryCode = null;
        sendCountryCodeChangedBroadcast();
    }

    public synchronized void setReadyForChange(boolean ready) {
        if (this.DBG) {
            Log.d(TAG, "Set ready: " + ready);
        }
        this.mReady = ready;
        if (this.mReady) {
            updateCountryCode();
        }
    }

    public synchronized boolean setCountryCode(String countryCode) {
        if (this.DBG) {
            Log.d(TAG, "Receive set country code request: " + countryCode);
        }
        if (!TextUtils.isEmpty(countryCode)) {
            this.mTelephonyCountryCode = countryCode.toUpperCase();
        } else if (this.mRevertCountryCodeOnCellularLoss) {
            if (this.DBG) {
                Log.d(TAG, "Received empty country code, reset to default country code");
            }
            this.mTelephonyCountryCode = null;
        }
        if (this.mReady) {
            updateCountryCode();
        }
        sendCountryCodeChangedBroadcast();
        return true;
    }

    public synchronized String getCountryCodeSentToDriver() {
        return this.mCurrentCountryCode;
    }

    public synchronized String getCountryCode() {
        return pickCountryCode();
    }

    private void updateCountryCode() {
        if (this.DBG) {
            Log.d(TAG, "Update country code");
        }
        String country = pickCountryCode();
        if (country != null) {
            setCountryCodeNative(country);
        }
    }

    private String pickCountryCode() {
        if (this.mTelephonyCountryCode != null) {
            return this.mTelephonyCountryCode;
        }
        if (this.mDefaultCountryCode != null) {
            return this.mDefaultCountryCode;
        }
        String countryCode = getCountryCodeProperty();
        if (countryCode != null) {
            return countryCode;
        }
        return null;
    }

    private boolean setCountryCodeNative(String country) {
        if (this.mWifiNative.setCountryCode(country)) {
            Log.d(TAG, "Succeeded to set country code to: " + country);
            this.mCurrentCountryCode = country;
            return true;
        }
        Log.d(TAG, "Failed to set country code to: " + country);
        return false;
    }

    private synchronized String getCountryCodeProperty() {
        String country;
        country = null;
        String property = SystemProperties.get(PROPERTY_OPERATOR_ISO_COUNTRY);
        if (!TextUtils.isEmpty(property)) {
            String[] str = property.split(",");
            for (int i = 0; i < str.length; i++) {
                if (!TextUtils.isEmpty(str[i])) {
                    country = str[i].toUpperCase();
                    Log.d(TAG, "Get telephony country code in property: " + country);
                    break;
                }
            }
        }
        return country;
    }
}

package com.mediatek.internal.telephony.dataconnection;

import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.os.Bundle;
import android.telephony.data.ApnSetting;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import java.util.ArrayList;
import vendor.mediatek.hardware.mtkradioex.V1_0.MtkApnTypes;

public class MtkApnContext extends ApnContext {
    private static final int NETWORK_TYPE_MCX = 1001;
    private static final String SLOG_TAG = "MtkApnContext";
    private DataConnection mDataConnectionSscMode3;
    private boolean mNeedNotify;
    private ArrayList<ApnSetting> mWifiApns = null;

    public MtkApnContext(Phone phone, String apnType, String logTag, NetworkConfig config, DcTracker tracker) {
        super(phone, apnType, logTag, config, tracker);
        this.mNeedNotify = needNotifyType(apnType);
    }

    public synchronized void setWifiApns(ArrayList<ApnSetting> wifiApns) {
        this.mWifiApns = wifiApns;
    }

    public synchronized ArrayList<ApnSetting> getWifiApns() {
        return this.mWifiApns;
    }

    public ApnSetting getNextApnSetting() {
        if (MtkGsmCdmaPhone.REASON_DATA_SETUP_SSC_MODE3.equals(getReason())) {
            return getApnSetting();
        }
        return MtkApnContext.super.getNextApnSetting();
    }

    public synchronized DataConnection getDataConnectionSscMode3() {
        return this.mDataConnectionSscMode3;
    }

    public synchronized void setDataConnectionSscMode3(DataConnection dc) {
        this.mDataConnectionSscMode3 = dc;
    }

    public void setEnabled(boolean enabled) {
        MtkApnContext.super.setEnabled(enabled);
        this.mNeedNotify = true;
    }

    private static int getApnTypeFromNetworkTypeEx(int networkType) {
        if (networkType == 21) {
            return MtkApnTypes.WAP;
        }
        if (networkType == 1001) {
            return 1024;
        }
        switch (networkType) {
            case 25:
                return MtkApnTypes.XCAP;
            case 26:
                return MtkApnTypes.RCS;
            case 27:
                return MtkApnTypes.BIP;
            case 28:
                return 32768;
            default:
                return 0;
        }
    }

    private static Bundle getApnTypeFromNetworkRequestEx(NetworkCapabilities nc, int apnType, boolean error) {
        if (nc.hasCapability(25)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = MtkApnTypes.WAP;
        }
        if (nc.hasCapability(9)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = MtkApnTypes.XCAP;
        }
        if (nc.hasCapability(8)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = MtkApnTypes.RCS;
        }
        if (nc.hasCapability(27)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = MtkApnTypes.BIP;
        }
        if (nc.hasCapability(26)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = 32768;
        }
        Bundle b = new Bundle();
        b.putInt("apnType", apnType);
        b.putBoolean("error", error);
        return b;
    }

    private boolean needNotifyType(String apnTypes) {
        if (apnTypes.equals("wap") || apnTypes.equals("xcap") || apnTypes.equals("rcs") || apnTypes.equals("bip") || apnTypes.equals("vsim")) {
            return false;
        }
        return true;
    }

    public boolean isNeedNotify() {
        return this.mNeedNotify;
    }

    public synchronized String toString() {
        return MtkApnContext.super.toString() + " mWifiApns={" + this.mWifiApns + "}";
    }
}

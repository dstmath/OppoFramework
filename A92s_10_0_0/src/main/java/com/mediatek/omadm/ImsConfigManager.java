package com.mediatek.omadm;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Slog;
import com.android.ims.ImsConfig;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.mediatek.dm.DmManager;

public class ImsConfigManager {
    public static boolean DEBUG = true;
    private static final String TAG = ImsConfigManager.class.getSimpleName();
    private final Context mContext;
    private DmManager mDmManager = null;

    private int imsRslt_PalRslt(int imsStatus) {
        if (imsStatus == 0) {
            return 0;
        }
        if (imsStatus == 1) {
            return 1;
        }
        if (imsStatus == 2 || imsStatus == 3) {
            return 11;
        }
        if (imsStatus != 4) {
            return 8;
        }
        return 10;
    }

    private ImsConfig getImsConfig() {
        ImsConfig imsConfig = null;
        try {
            imsConfig = ImsManager.getInstance(this.mContext, getMainCapabilityPhoneId()).getConfigInterface();
        } catch (ImsException e) {
            e.printStackTrace();
            PalConstDefs.throwEcxeption(1);
        }
        if (imsConfig == null) {
            PalConstDefs.throwEcxeption(11);
        }
        return imsConfig;
    }

    private int getMainCapabilityPhoneId() {
        int phoneId = SystemProperties.getInt("persist.radio.simswitch", 1) - 1;
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            phoneId = -1;
        }
        String str = TAG;
        Slog.d(str, "getMainCapabilityPhoneId = " + phoneId);
        return phoneId;
    }

    public ImsConfigManager(Context context) {
        this.mContext = context;
        this.mDmManager = DmManager.getDefaultDmManager(this.mContext);
    }

    public void setProvisionedStringValue(int item, String value) {
        int status;
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "setProvisionedStringValue, item = " + item + ", value = " + value);
        }
        try {
            status = imsRslt_PalRslt(getImsConfig().setProvisionedStringValue(item, value));
        } catch (ImsException e) {
            e.printStackTrace();
            status = 10;
        }
        PalConstDefs.throwEcxeption(status);
    }

    public String getProvisionedStringValue(int item) {
        String result = null;
        int status = 0;
        try {
            result = getImsConfig().getProvisionedStringValue(item);
            if (DEBUG) {
                String str = TAG;
                Slog.d(str, "getProvisionedStringValue, item = " + item + ", result = " + result);
            }
            if (result == null || result.length() == 0) {
                status = 10;
            }
        } catch (ImsException e) {
            e.printStackTrace();
            status = 10;
        }
        PalConstDefs.throwEcxeption(status);
        return result;
    }

    public void setProvisionedIntValue(int item, int value) {
        int status;
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "setProvisionedIntValue, item = " + item + ", value = " + value);
        }
        try {
            status = imsRslt_PalRslt(getImsConfig().setProvisionedValue(item, value));
        } catch (ImsException e) {
            e.printStackTrace();
            status = 10;
        }
        PalConstDefs.throwEcxeption(status);
    }

    public int getProvisionedIntValue(int item) {
        if (DEBUG) {
            String str = TAG;
            Slog.d(str, "getProvisionedIntValue, item = " + item);
        }
        int result = -1;
        int status = 0;
        try {
            result = getImsConfig().getProvisionedValue(item);
            if (DEBUG) {
                String str2 = TAG;
                Slog.d(str2, "getProvisionedStringValue, item = " + item + ", result = " + result);
            }
        } catch (ImsException e) {
            e.printStackTrace();
            status = 10;
        }
        PalConstDefs.throwEcxeption(status);
        return result;
    }

    public int isImcPvsInfoSupport() {
        Slog.d(TAG, "check isImcPvsInfoSupport");
        if (this.mDmManager == null) {
            this.mDmManager = DmManager.getDefaultDmManager(this.mContext);
        }
        DmManager dmManager = this.mDmManager;
        if (dmManager != null) {
            return dmManager.getDmSupported();
        }
        Slog.d(TAG, "setImcProvisioned, failed since cant get  mDmManager");
        PalConstDefs.throwEcxeption(0);
        return 0;
    }

    public void setImcProvisioned(int item, int value) {
        String str = TAG;
        Slog.d(str, "setImcProvisioned, item = " + item + ", value = " + value);
        if (this.mDmManager == null) {
            this.mDmManager = DmManager.getDefaultDmManager(this.mContext);
        }
        DmManager dmManager = this.mDmManager;
        if (dmManager != null) {
            dmManager.setImcProvision(getMainCapabilityPhoneId(), item, value);
            return;
        }
        Slog.d(TAG, "setImcProvisioned, failed since cant get  mDmManager");
        PalConstDefs.throwEcxeption(0);
    }

    public int getImcProvisioned(int item) {
        String str = TAG;
        Slog.d(str, "getImcProvisioned, item = " + item);
        if (this.mDmManager == null) {
            this.mDmManager = DmManager.getDefaultDmManager(this.mContext);
        }
        DmManager dmManager = this.mDmManager;
        if (dmManager != null) {
            int result = 1;
            if (!dmManager.getImcProvision(getMainCapabilityPhoneId(), item)) {
                result = 0;
            }
            String str2 = TAG;
            Slog.d(str2, "getImcProvisioned, item = " + item + ", result = " + result);
            return result;
        }
        Slog.d(TAG, "getImcProvisioned, failed since cant get  mDmManager");
        PalConstDefs.throwEcxeption(0);
        return -1;
    }
}

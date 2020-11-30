package com.mediatek.internal.telephony.datasub;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.ProxyController;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;

public class CapabilitySwitch {
    private static boolean DBG = true;
    private static final String LOG_TAG = "CapaSwitch";
    private static final int capability_switch_policy = SystemProperties.getInt(DataSubConstants.PROPERTY_CAPABILITY_SWITCH_POLICY, 1);
    private static Context mContext = null;
    private static DataSubSelector mDataSubSelector = null;
    private static CapabilitySwitch mInstance = null;

    public static CapabilitySwitch getInstance(Context context, DataSubSelector dataSubSelector) {
        if (mInstance == null) {
            mInstance = new CapabilitySwitch(context, dataSubSelector);
        }
        return mInstance;
    }

    public CapabilitySwitch(Context context, DataSubSelector dataSubSelector) {
        mContext = context;
        mDataSubSelector = dataSubSelector;
    }

    public boolean isCanSwitch() {
        if (!mDataSubSelector.getAirPlaneModeOn()) {
            return isSimUnLocked();
        }
        log("DataSubselector,isCanSwitch AirplaneModeOn = " + mDataSubSelector.getAirPlaneModeOn());
        return false;
    }

    public boolean isSimUnLocked() {
        int simNum = mDataSubSelector.getPhoneNum();
        for (int i = 0; i < simNum; i++) {
            int simState = TelephonyManager.from(mContext).getSimState(i);
            if (simState == 2 || simState == 3 || simState == 4 || simState == 6 || simState == 0) {
                log("isSimUnLocked, sim locked, simState = " + simState + ", slot=" + i);
                if (simState != 6 || RadioCapabilitySwitchUtil.isSimOn(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isNeedWaitUnlock() {
        return SystemProperties.getBoolean(DataSubConstants.NEED_TO_WAIT_UNLOCKED, false);
    }

    public static boolean isNeedWaitUnlockRoaming() {
        return SystemProperties.getBoolean(DataSubConstants.NEED_TO_WAIT_UNLOCKED_ROAMING, false);
    }

    public static void setNeedWaitUnlock(String value) {
        SystemProperties.set(DataSubConstants.NEED_TO_WAIT_UNLOCKED, value);
    }

    public static void setNeedWaitUnlockRoaming(String value) {
        SystemProperties.set(DataSubConstants.NEED_TO_WAIT_UNLOCKED_ROAMING, value);
    }

    public static boolean isNeedWaitImsi() {
        return SystemProperties.getBoolean(DataSubConstants.NEED_TO_WAIT_IMSI, false);
    }

    public static boolean isNeedWaitImsiRoaming() {
        return SystemProperties.getBoolean(DataSubConstants.NEED_TO_WAIT_IMSI_ROAMING, false);
    }

    public static void setNeedWaitImsi(String value) {
        SystemProperties.set(DataSubConstants.NEED_TO_WAIT_IMSI, value);
    }

    public static void setNeedWaitImsiRoaming(String value) {
        SystemProperties.set(DataSubConstants.NEED_TO_WAIT_IMSI_ROAMING, value);
    }

    public static void setSimStatus(Intent intent) {
        if (intent == null) {
            log("setSimStatus, intent is null => return");
            return;
        }
        log("setSimStatus");
        int detectedType = intent.getIntExtra("simDetectStatus", 0);
        if (detectedType != getSimStatus()) {
            SystemProperties.set(DataSubConstants.SIM_STATUS, Integer.toString(detectedType));
        }
    }

    public static void resetSimStatus() {
        log("resetSimStatus");
        SystemProperties.set(DataSubConstants.SIM_STATUS, "");
    }

    public static int getSimStatus() {
        log("getSimStatus");
        return SystemProperties.getInt(DataSubConstants.SIM_STATUS, 0);
    }

    public boolean setCapability(int phoneId) {
        int phoneNum = mDataSubSelector.getPhoneNum();
        int[] phoneRat = new int[phoneNum];
        String curr3GSim = SystemProperties.get("persist.vendor.radio.simswitch", "");
        log("setCapability: " + phoneId + ", current 3G Sim = " + curr3GSim);
        ProxyController proxyController = ProxyController.getInstance();
        RadioAccessFamily[] rat = new RadioAccessFamily[phoneNum];
        for (int i = 0; i < phoneNum; i++) {
            if (phoneId == i) {
                phoneRat[i] = proxyController.getMaxRafSupported();
            } else {
                phoneRat[i] = proxyController.getMinRafSupported();
            }
            rat[i] = new RadioAccessFamily(i, phoneRat[i]);
        }
        if (proxyController.setRadioCapability(rat)) {
            return true;
        }
        log("Set phone rat fail!!! MaxPhoneRat=" + phoneRat[phoneId]);
        return false;
    }

    public boolean setCapabilityIfNeeded(int phoneId) {
        if (phoneId != RadioCapabilitySwitchUtil.getMainCapabilityPhoneId()) {
            return setCapability(phoneId);
        }
        return true;
    }

    public int getCapabilitySwitchPolicy() {
        return capability_switch_policy;
    }

    public void handleSimImsiStatus(Intent intent) {
        int simStatus = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
        int slotId = intent.getIntExtra("phone", 0);
        if (simStatus == 10) {
            RadioCapabilitySwitchUtil.updateSimImsiStatus(slotId, "1");
        } else if (simStatus == 6) {
            RadioCapabilitySwitchUtil.updateSimImsiStatus(slotId, "0");
        }
    }

    private static void log(String txt) {
        if (DBG) {
            Rlog.d(LOG_TAG, txt);
        }
    }

    private static void loge(String txt) {
        if (DBG) {
            Rlog.e(LOG_TAG, txt);
        }
    }
}

package com.mediatek.internal.telephony;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.mediatek.internal.telephony.IMtkSub;

public class MtkSubscriptionManager {
    private static final boolean DBG = false;
    public static final int EXTRA_VALUE_NEW_SIM = 1;
    public static final int EXTRA_VALUE_NOCHANGE = 4;
    public static final int EXTRA_VALUE_REMOVE_SIM = 2;
    public static final int EXTRA_VALUE_REPOSITION_SIM = 3;
    public static final String INTENT_KEY_DETECT_STATUS = "simDetectStatus";
    public static final String INTENT_KEY_PROP_KEY = "simPropKey";
    public static final String INTENT_KEY_SIM_COUNT = "simCount";
    private static final String LOG_TAG = "MtkSubscriptionManager";
    private static final boolean VDBG = false;

    public static MtkSubscriptionInfo getSubInfo(String callingPackage, int subId) {
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            Rlog.d(LOG_TAG, "[getSubInfo]- invalid subId, subId = " + subId);
            return null;
        }
        try {
            IMtkSub iSub = IMtkSub.Stub.asInterface(ServiceManager.getService("isubstub"));
            if (iSub != null) {
                return iSub.getSubInfo(callingPackage, subId);
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public static MtkSubscriptionInfo getSubInfoForIccId(String callingPackage, String iccId) {
        if (iccId == null) {
            Rlog.d(LOG_TAG, "[getSubInfoForIccId]- null iccid");
            return null;
        }
        try {
            IMtkSub iSub = IMtkSub.Stub.asInterface(ServiceManager.getService("isubstub"));
            if (iSub != null) {
                return iSub.getSubInfoForIccId(callingPackage, iccId);
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public static int getSubIdUsingPhoneId(int phoneId) {
        try {
            IMtkSub iSub = IMtkSub.Stub.asInterface(ServiceManager.getService("isubstub"));
            if (iSub != null) {
                return iSub.getSubIdUsingPhoneId(phoneId);
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static void setDefaultSubId(int subId) {
        if (subId <= 0) {
            printStackTrace("setDefaultSubId subId 0");
        }
        try {
            IMtkSub iSub = IMtkSub.Stub.asInterface(ServiceManager.getService("isubstub"));
            if (iSub != null) {
                iSub.setDefaultFallbackSubId(subId, 0);
            }
        } catch (RemoteException e) {
        }
    }

    public static void setDefaultDataSubIdWithoutCapabilitySwitch(int subId) {
        if (subId <= 0) {
            printStackTrace("setDefaultDataSubIdWithoutCapabilitySwitch subId 0");
        }
        try {
            IMtkSub iSub = IMtkSub.Stub.asInterface(ServiceManager.getService("isubstub"));
            if (iSub != null) {
                iSub.setDefaultDataSubIdWithoutCapabilitySwitch(subId);
            }
        } catch (RemoteException e) {
        }
    }

    private static void printStackTrace(String msg) {
        RuntimeException re = new RuntimeException();
        Rlog.d(LOG_TAG, "StackTrace - " + msg);
        for (StackTraceElement ste : re.getStackTrace()) {
            Rlog.d(LOG_TAG, ste.toString());
        }
    }
}

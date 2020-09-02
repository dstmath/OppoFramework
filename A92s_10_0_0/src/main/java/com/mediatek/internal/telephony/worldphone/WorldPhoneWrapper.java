package com.mediatek.internal.telephony.worldphone;

import android.os.SystemProperties;
import android.telephony.Rlog;
import com.mediatek.internal.telephony.datasub.DataSubConstants;

public class WorldPhoneWrapper implements IWorldPhone {
    private static int sOperatorSpec = -1;
    private static IWorldPhone sWorldPhoneInstance = null;
    private static WorldPhoneUtil sWorldPhoneUtil = null;

    public static IWorldPhone getWorldPhoneInstance() {
        if (sWorldPhoneInstance == null) {
            String optr = SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR);
            if (optr == null || !optr.equals(DataSubConstants.OPERATOR_OP01)) {
                sOperatorSpec = 0;
            } else {
                sOperatorSpec = 1;
            }
            sWorldPhoneUtil = new WorldPhoneUtil();
            int i = sOperatorSpec;
            if (i == 1) {
                sWorldPhoneInstance = new WorldPhoneOp01();
            } else if (i == 0) {
                sWorldPhoneInstance = new WorldPhoneOm();
            }
        }
        logd("sOperatorSpec: " + sOperatorSpec + ", isLteSupport: " + WorldPhoneUtil.isLteSupport());
        return sWorldPhoneInstance;
    }

    @Override // com.mediatek.internal.telephony.worldphone.IWorldPhone
    public void setModemSelectionMode(int mode, int modemType) {
        int i = sOperatorSpec;
        if (i == 1 || i == 0) {
            sWorldPhoneInstance.setModemSelectionMode(mode, modemType);
        } else {
            logd("Unknown World Phone Spec");
        }
    }

    @Override // com.mediatek.internal.telephony.worldphone.IWorldPhone
    public void notifyRadioCapabilityChange(int capailitySimId) {
    }

    private static void logd(String msg) {
        Rlog.d(IWorldPhone.LOG_TAG, "[WPO_WRAPPER]" + msg);
    }
}

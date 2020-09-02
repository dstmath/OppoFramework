package com.mediatek.internal.telephony;

import android.telephony.Rlog;
import mediatek.telephony.ISignalStrengthExt;

public class SignalStrengthExt implements ISignalStrengthExt {
    private static final boolean DBG = true;
    static final String TAG = "SignalStrengthExt";

    @Override // mediatek.telephony.ISignalStrengthExt
    public int mapUmtsSignalLevel(int phoneId, int UmtsRscp) {
        int level;
        log("mapUmtsSignalLevel, phoneId=" + phoneId);
        if (UmtsRscp > -25 || UmtsRscp == Integer.MAX_VALUE) {
            level = 0;
        } else if (UmtsRscp >= -72) {
            level = 4;
        } else if (UmtsRscp >= -88) {
            level = 3;
        } else if (UmtsRscp >= -104) {
            level = 2;
        } else if (UmtsRscp >= -120) {
            level = 1;
        } else {
            level = 0;
        }
        log("mapUmtsSignalLevel, level=" + level);
        return level;
    }

    @Override // mediatek.telephony.ISignalStrengthExt
    public int mapLteSignalLevel(int mLteRsrp, int mLteRssnr, int mLteSignalStrength) {
        int rssiIconLevel = 0;
        int rsrpIconLevel = -1;
        int snrIconLevel = -1;
        if (mLteRsrp > -44) {
            rsrpIconLevel = -1;
        } else if (mLteRsrp >= -85) {
            rsrpIconLevel = 4;
        } else if (mLteRsrp >= -95) {
            rsrpIconLevel = 3;
        } else if (mLteRsrp >= -105) {
            rsrpIconLevel = 2;
        } else if (mLteRsrp >= -115) {
            rsrpIconLevel = 1;
        } else if (mLteRsrp >= -140) {
            rsrpIconLevel = 0;
        }
        if (mLteRssnr > 300) {
            snrIconLevel = -1;
        } else if (mLteRssnr >= 130) {
            snrIconLevel = 4;
        } else if (mLteRssnr >= 45) {
            snrIconLevel = 3;
        } else if (mLteRssnr >= 10) {
            snrIconLevel = 2;
        } else if (mLteRssnr >= -30) {
            snrIconLevel = 1;
        } else if (mLteRssnr >= -200) {
            snrIconLevel = 0;
        }
        Rlog.i(TAG, "getLTELevel - rsrp:" + mLteRsrp + " snr:" + mLteRssnr + " rsrpIconLevel:" + rsrpIconLevel + " snrIconLevel:" + snrIconLevel);
        if (snrIconLevel != -1 && rsrpIconLevel != -1) {
            return rsrpIconLevel < snrIconLevel ? rsrpIconLevel : snrIconLevel;
        }
        if (snrIconLevel != -1) {
            return snrIconLevel;
        }
        if (rsrpIconLevel != -1) {
            return rsrpIconLevel;
        }
        if (mLteSignalStrength > 63) {
            rssiIconLevel = 0;
        } else if (mLteSignalStrength >= 12) {
            rssiIconLevel = 4;
        } else if (mLteSignalStrength >= 8) {
            rssiIconLevel = 3;
        } else if (mLteSignalStrength >= 5) {
            rssiIconLevel = 2;
        } else if (mLteSignalStrength >= 0) {
            rssiIconLevel = 1;
        }
        Rlog.i(TAG, "getLTELevel - rssi:" + mLteSignalStrength + " rssiIconLevel:" + rssiIconLevel);
        return rssiIconLevel;
    }

    public static void log(String text) {
        Rlog.d(TAG, text);
    }

    private void loge(String txt) {
        Rlog.e(TAG, txt);
    }
}

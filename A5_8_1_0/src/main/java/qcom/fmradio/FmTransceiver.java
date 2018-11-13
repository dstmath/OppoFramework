package qcom.fmradio;

import android.util.Log;
import java.io.File;

public class FmTransceiver {
    public static final int ERROR = -1;
    public static int FMState = 0;
    public static final int FMState_Rx_Turned_On = 1;
    public static final int FMState_Srch_InProg = 3;
    public static final int FMState_Turned_Off = 0;
    public static final int FMState_Tx_Turned_On = 2;
    public static final int FM_CHSPACE_100_KHZ = 1;
    public static final int FM_CHSPACE_200_KHZ = 0;
    public static final int FM_CHSPACE_50_KHZ = 2;
    public static final int FM_DE_EMP50 = 1;
    public static final int FM_DE_EMP75 = 0;
    public static final int FM_EU_BAND = 1;
    public static final int FM_JAPAN_STANDARD_BAND = 2;
    public static final int FM_JAPAN_WIDE_BAND = 3;
    public static final int FM_RDS_STD_NONE = 2;
    public static final int FM_RDS_STD_RBDS = 0;
    public static final int FM_RDS_STD_RDS = 1;
    protected static final int FM_RX = 1;
    protected static final int FM_TX = 2;
    public static final int FM_USER_DEFINED_BAND = 4;
    public static final int FM_US_BAND = 0;
    private static final int V4L2_CID_PRIVATE_BASE = 134217728;
    private static final int V4L2_CID_PRIVATE_TAVARUA_ANTENNA = 134217746;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_MASK = 134217734;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SET_NOTCH_FILTER = 134217768;
    protected static int sFd = 0;
    public static final int subPwrLevel_FMRx_Starting = 4;
    public static final int subPwrLevel_FMTurning_Off = 6;
    public static final int subPwrLevel_FMTx_Starting = 5;
    public static final int subSrchLevel_NoSearch = -1;
    public static final int subSrchLevel_ScanInProg = 1;
    public static final int subSrchLevel_SeekInPrg = 0;
    public static final int subSrchLevel_SrchAbort = 4;
    public static final int subSrchLevel_SrchComplete = 3;
    public static final int subSrchLevel_SrchListInProg = 2;
    private final int MUTE_EVENT = 4;
    private final int RDS_EVENT = 8;
    private final int READY_EVENT = 1;
    private final int SEEK_COMPLETE_EVENT = 3;
    private final String TAG = "FmTransceiver";
    private final int TUNE_EVENT = 2;
    private final String V4L2_DEVICE = "/dev/radio0";
    protected FmRxControls mControl;
    protected int mPowerMode;
    protected FmRxRdsData mRdsData;
    protected FmRxEventListner mRxEvents;
    protected FmTxEventListner mTxEvents;

    protected boolean acquire(String device) {
        if (sFd <= 0) {
            sFd = FmReceiverJNI.acquireFdNative("/dev/radio0");
            if (sFd > 0) {
                Log.d("FmTransceiver", "Opened " + sFd);
                return true;
            }
            Log.d("FmTransceiver", "Fail to Open " + sFd);
            return false;
        }
        Log.d("FmTransceiver", "Already Opened:" + sFd);
        return false;
    }

    static boolean release(String device) {
        if (sFd != 0) {
            FmReceiverJNI.closeFdNative(sFd);
            sFd = 0;
            Log.d("FmTransceiver", "Turned off: " + sFd);
        } else {
            Log.d("FmTransceiver", "Error turning off");
        }
        return true;
    }

    public boolean registerClient(FmRxEvCallbacks callback) {
        if (callback != null) {
            this.mRxEvents.startListner(sFd, callback);
            return true;
        }
        Log.d("FmTransceiver", "Null, do nothing");
        return false;
    }

    public boolean unregisterClient() {
        this.mRxEvents.stopListener();
        return true;
    }

    public boolean registerTransmitClient(FmTransmitterCallbacks callback) {
        if (callback != null) {
            this.mTxEvents.startListner(sFd, callback);
            return true;
        }
        Log.d("FmTransceiver", "Null, do nothing");
        return false;
    }

    public boolean unregisterTransmitClient() {
        this.mTxEvents.stopListener();
        return true;
    }

    public boolean enable(FmConfig configSettings, int device) {
        if (!FmReceiver.isCherokeeChip() && !acquire("/dev/radio0")) {
            return false;
        }
        if (new File("/etc/fm/SpurTableFile.txt").isFile()) {
            Log.d("FmTransceiver", "Send Spur roation table");
            FmConfig.fmSpurConfig(sFd);
        } else {
            Log.d("FmTransceiver", "No existing file to do spur configuration");
        }
        Log.d("FmTransceiver", "turning on " + device);
        if (this.mControl.fmOn(sFd, device) < 0) {
            Log.d("FmTransceiver", "turning on failed");
            FmReceiverJNI.closeFdNative(sFd);
            sFd = 0;
            return false;
        }
        Log.d("FmTransceiver", "Calling fmConfigure");
        boolean status = FmConfig.fmConfigure(sFd, configSettings);
        if (!status) {
            Log.d("FmTransceiver", "fmConfigure failed");
            FmReceiverJNI.closeFdNative(sFd);
            sFd = 0;
        }
        return status;
    }

    public boolean disable() {
        this.mControl.fmOff(sFd);
        return true;
    }

    public boolean configure(FmConfig configSettings) {
        int lowerFreq = configSettings.getLowerLimit();
        Log.d("FmTransceiver", "fmConfigure");
        boolean status = FmConfig.fmConfigure(sFd, configSettings);
        return setStation(lowerFreq);
    }

    public boolean setStation(int frequencyKHz) {
        this.mControl.setFreq(frequencyKHz);
        if (this.mControl.setStation(sFd) < 0) {
            return false;
        }
        return true;
    }

    public void setNotchFilter(boolean value) {
        FmReceiverJNI.setNotchFilterNative(sFd, V4L2_CID_PRIVATE_TAVARUA_SET_NOTCH_FILTER, value);
    }

    public boolean setAnalogMode(boolean value) {
        int audioPath = this.mControl.setAudioPath(sFd, value);
        if (FmReceiverJNI.setAnalogModeNative(value) == 1) {
            return true;
        }
        return false;
    }

    public boolean getInternalAntenna() {
        if (FmReceiverJNI.getControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_ANTENNA) == 1) {
            return true;
        }
        return false;
    }

    public boolean setInternalAntenna(boolean intAnt) {
        int iAntenna;
        if (intAnt) {
            iAntenna = 1;
        } else {
            iAntenna = 0;
        }
        if (FmReceiverJNI.setControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_ANTENNA, iAntenna) == 0) {
            return true;
        }
        return false;
    }

    static void setFMPowerState(int state) {
        FMState = state;
    }

    public static int getFMPowerState() {
        return FMState;
    }

    public static boolean setRDSGrpMask(int mask) {
        if (FmReceiverJNI.setControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_MASK, mask) == 0) {
            return true;
        }
        return false;
    }
}

package qcom.fmradio;

import android.util.Log;

public class FmTransmitter extends FmTransceiver {
    public static final int FM_TX_LOW_POWER_MODE = 1;
    public static final int FM_TX_MAX_PS_LEN = 97;
    public static final int FM_TX_MAX_RT_LEN = 63;
    public static final int FM_TX_NORMAL_POWER_MODE = 0;
    public static final int FM_TX_PWR_LEVEL_0 = 0;
    public static final int FM_TX_PWR_LEVEL_1 = 1;
    public static final int FM_TX_PWR_LEVEL_2 = 2;
    public static final int FM_TX_PWR_LEVEL_3 = 3;
    public static final int FM_TX_PWR_LEVEL_4 = 4;
    public static final int FM_TX_PWR_LEVEL_5 = 5;
    public static final int FM_TX_PWR_LEVEL_6 = 6;
    public static final int FM_TX_PWR_LEVEL_7 = 7;
    private static final int MAX_PS_CHARS = 97;
    private static final int MAX_PS_REP_COUNT = 15;
    private static final int MAX_RDS_GROUP_BUF_SIZE = 62;
    public static final int RDS_GRPS_TX_PAUSE = 0;
    public static final int RDS_GRPS_TX_RESUME = 1;
    public static final int RDS_GRPS_TX_STOP = 2;
    private static final int V4L2_CID_PRIVATE_BASE = 134217728;
    private static final int V4L2_CID_PRIVATE_TAVARUA_ANTENNA = 134217746;
    private final String TAG;
    private boolean mPSStarted;
    private boolean mRTStarted;
    private FmTransmitterCallbacksAdaptor mTxCallbacks;

    public class FmPSFeatures {
        public int maxPSCharacters;
        public int maxPSStringRepeatCount;
    }

    public FmTransmitter(String path, FmTransmitterCallbacksAdaptor callbacks) throws InstantiationException {
        this.TAG = "FmTransmitter";
        this.mPSStarted = false;
        this.mRTStarted = false;
        this.mTxEvents = new FmTxEventListner();
        this.mControl = new FmRxControls();
        this.mTxCallbacks = callbacks;
    }

    public boolean enable(FmConfig configSettings) {
        int state = getFMState();
        if (state == 2) {
            Log.d("FmTransmitter", "enable: FM Tx already turned On and running");
            return false;
        } else if (state == 6) {
            Log.v("FmTransmitter", "FM is in the process of turning off.Pls wait for sometime.");
            return false;
        } else if (state == 5 || state == 4) {
            Log.v("FmTransmitter", "FM is in the process of turning On.Pls wait for sometime.");
            return false;
        } else if (state == 3 || state == 1) {
            Log.v("FmTransmitter", "FM Rx is turned on");
            return false;
        } else {
            FmTransceiver.setFMPowerState(5);
            Log.v("FmTransmitter", "enable: CURRENT-STATE : FMOff ---> NEW-STATE : FMTxStarting");
            boolean status = super.enable(configSettings, 2);
            if (status) {
                registerTransmitClient(this.mTxCallbacks);
                this.mRdsData = new FmRxRdsData(sFd);
            } else {
                status = false;
                Log.e("FmTransmitter", "enable: failed to turn On FM TX");
                Log.e("FmTransmitter", "enable: CURRENT-STATE : FMTxStarting ---> NEW-STATE : FMOff");
                FmTransceiver.setFMPowerState(0);
            }
            return status;
        }
    }

    public boolean setRdsOn() {
        return this.mRdsData != null && this.mRdsData.rdsOn(true) == 0;
    }

    public boolean disable() {
        switch (getFMState()) {
            case 0:
                Log.d("FmTransmitter", "FM already tuned Off.");
                return true;
            case 5:
                Log.d("FmTransmitter", "disable: FM not yet turned On...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (getFMState() == 5) {
                    Log.e("FmTransmitter", "disable: FM in bad state");
                    return false;
                }
                break;
            case 6:
                Log.v("FmTransmitter", "disable: FM is getting turned Off.");
                return false;
        }
        FmTransceiver.setFMPowerState(6);
        Log.v("FmTransmitter", "disable: CURRENT-STATE : FMTxOn ---> NEW-STATE : FMTurningOff");
        if (this.mPSStarted && !stopPSInfo()) {
            Log.d("FmTransmitter", "FmTrasmitter:stopPSInfo failed\n");
        }
        if (this.mRTStarted && !stopRTInfo()) {
            Log.d("FmTransmitter", "FmTrasmitter:stopRTInfo failed\n");
        }
        if (!transmitRdsGroupControl(2)) {
            Log.d("FmTransmitter", "FmTrasmitter:transmitRdsGroupControl failed\n");
        }
        super.disable();
        return true;
    }

    public boolean reset() {
        if (getFMState() == 0) {
            Log.d("FmTransmitter", "FM already turned Off.");
            return false;
        }
        FmTransceiver.setFMPowerState(0);
        Log.v("FmTransmitter", "reset: NEW-STATE : FMState_Turned_Off");
        boolean status = unregisterTransmitClient();
        FmTransceiver.release("/dev/radio0");
        return status;
    }

    public boolean setStation(int frequencyKHz) {
        if (this.mPSStarted) {
            Log.d("FmTransmitter", "FmTransmitter:setStation mPSStarted");
            if (!stopPSInfo()) {
                return false;
            }
        }
        if (this.mRTStarted) {
            Log.d("FmTransmitter", "FmTransmitter:setStation mRTStarted");
            if (!stopRTInfo()) {
                return false;
            }
        }
        if (!transmitRdsGroupControl(2)) {
            return false;
        }
        Log.d("FmTransmitter", "FmTrasmitter:SetStation\n");
        return super.setStation(frequencyKHz);
    }

    public boolean setPowerMode(int powerMode) {
        int re;
        if (powerMode == 1) {
            re = this.mControl.setLowPwrMode(sFd, true);
        } else {
            re = this.mControl.setLowPwrMode(sFd, false);
        }
        return re == 0;
    }

    public FmPSFeatures getPSFeatures() {
        FmPSFeatures psFeatures = new FmPSFeatures();
        psFeatures.maxPSCharacters = 97;
        psFeatures.maxPSStringRepeatCount = MAX_PS_REP_COUNT;
        return psFeatures;
    }

    public boolean startPSInfo(String psStr, int pty, int pi, int repeatCount) {
        if (pty < 0 || pty > 31) {
            Log.d("FmTransmitter", "pTy is expected from 0 to 31");
            return false;
        } else if (FmReceiverJNI.setPTYNative(sFd, pty) < 0) {
            Log.d("FmTransmitter", "setPTYNative is failure");
            return false;
        } else if (pi < 0 || pi > 65535) {
            Log.d("FmTransmitter", "pi is expected from 0 to 65535");
            return false;
        } else if (FmReceiverJNI.setPINative(sFd, pi) < 0) {
            Log.d("FmTransmitter", "setPINative is failure");
            return false;
        } else if (repeatCount < 0 || repeatCount > MAX_PS_REP_COUNT) {
            Log.d("FmTransmitter", "repeat count is expected from 0 to 15");
            return false;
        } else if (FmReceiverJNI.setPSRepeatCountNative(sFd, repeatCount) < 0) {
            Log.d("FmTransmitter", "setPSRepeatCountNative is failure");
            return false;
        } else {
            if (psStr.length() > 97) {
                psStr = psStr.substring(0, 97);
            }
            int err = FmReceiverJNI.startPSNative(sFd, psStr, psStr.length());
            Log.d("FmTransmitter", "return for startPS is " + err);
            if (err < 0) {
                Log.d("FmTransmitter", "FmReceiverJNI.startPSNative returned false\n");
                return false;
            }
            Log.d("FmTransmitter", "startPSNative is successful");
            this.mPSStarted = true;
            return true;
        }
    }

    public boolean stopPSInfo() {
        int err = FmReceiverJNI.stopPSNative(sFd);
        if (err < 0) {
            Log.d("FmTransmitter", "return for startPS is " + err);
            return false;
        }
        Log.d("FmTransmitter", "stopPSNative is successful");
        this.mPSStarted = false;
        return true;
    }

    public boolean startRTInfo(String rtStr, int pty, int pi) {
        if (pty < 0 || pty > 31) {
            Log.d("FmTransmitter", "pTy is expected from 0 to 31");
            return false;
        } else if (FmReceiverJNI.setPTYNative(sFd, pty) < 0) {
            Log.d("FmTransmitter", "setPTYNative is failure");
            return false;
        } else if (pi < 0 || pi > 65535) {
            Log.d("FmTransmitter", "pi is expected from 0 to 65535");
            return false;
        } else if (FmReceiverJNI.setPINative(sFd, pi) < 0) {
            Log.d("FmTransmitter", "setPINative is failure");
            return false;
        } else {
            if (rtStr.length() > 63) {
                rtStr = rtStr.substring(0, 63);
            }
            if (FmReceiverJNI.startRTNative(sFd, rtStr, rtStr.length()) < 0) {
                Log.d("FmTransmitter", "FmReceiverJNI.startRTNative returned false\n");
                return false;
            }
            Log.d("FmTransmitter", "mRTStarted is true");
            this.mRTStarted = true;
            return true;
        }
    }

    public boolean stopRTInfo() {
        if (FmReceiverJNI.stopRTNative(sFd) < 0) {
            Log.d("FmTransmitter", "stopRTNative is failure");
            return false;
        }
        Log.d("FmTransmitter", "mRTStarted is false");
        this.mRTStarted = false;
        return true;
    }

    public int getRdsGroupBufSize() {
        return MAX_RDS_GROUP_BUF_SIZE;
    }

    public int transmitRdsGroups(byte[] rdsGroups, long numGroupsToTransmit) {
        return -1;
    }

    public int transmitRdsContGroups(byte[] rdsGroups, long numGroupsToTransmit) {
        return -1;
    }

    public boolean transmitRdsGroupControl(int ctrlCmd) {
        switch (ctrlCmd) {
            case 0:
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }

    public boolean setTxPowerLevel(int powLevel) {
        if (FmReceiverJNI.setTxPowerLevelNative(sFd, powLevel) >= 0) {
            return true;
        }
        Log.d("FmTransmitter", "setTxPowerLevel is failure");
        return false;
    }

    public int getFMState() {
        return FmTransceiver.getFMPowerState();
    }
}

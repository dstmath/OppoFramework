package qcom.fmradio;

import android.util.Log;
import java.util.Arrays;

class FmReceiverJNI {
    static final int FM_JNI_FAILURE = -1;
    static final int FM_JNI_SUCCESS = 0;
    private static final int STD_BUF_SIZE = 256;
    private static final String TAG = "FmReceiverJNI";
    private static byte[] mRdsBuffer = new byte[STD_BUF_SIZE];
    private FmRxEvCallbacks mCallback;

    static native int SetCalibrationNative(int i);

    static native int acquireFdNative(String str);

    static native int audioControlNative(int i, int i2, int i3);

    static native int cancelSearchNative(int i);

    static native void classInitNative();

    static native void cleanupNative();

    static native int closeFdNative(int i);

    static native void configurePerformanceParams(int i);

    static native int configureSpurTable(int i);

    static native int enableSlimbus(int i, int i2);

    static native int enableSoftMute(int i, int i2);

    static native int getBufferNative(int i, byte[] bArr, int i2);

    static native int getControlNative(int i, int i2);

    static native int getFreqNative(int i);

    static native int getLowerBandNative(int i);

    static native int getRSSINative(int i);

    static native int getRawRdsNative(int i, byte[] bArr, int i2);

    static native int getUpperBandNative(int i);

    static native void initNative();

    static native int setAnalogModeNative(boolean z);

    static native int setBandNative(int i, int i2, int i3);

    static native int setControlNative(int i, int i2, int i3);

    static native int setFreqNative(int i, int i2);

    static native int setMonoStereoNative(int i, int i2);

    static native int setNotchFilterNative(int i, int i2, boolean z);

    static native int setPINative(int i, int i2);

    static native int setPSRepeatCountNative(int i, int i2);

    static native int setPTYNative(int i, int i2);

    static native int setSpurDataNative(int i, short[] sArr, int i2);

    static native int setTxPowerLevelNative(int i, int i2);

    static native int startPSNative(int i, String str, int i2);

    static native int startRTNative(int i, String str, int i2);

    static native int startSearchNative(int i, int i2);

    static native int stopPSNative(int i);

    static native int stopRTNative(int i);

    static {
        Log.d(TAG, "classinit native called");
        classInitNative();
    }

    public static byte[] getPsBuffer(byte[] buff) {
        Log.d(TAG, "getPsBuffer enter");
        buff = Arrays.copyOf(mRdsBuffer, mRdsBuffer.length);
        Log.d(TAG, "getPsBuffer exit");
        return buff;
    }

    public void AflistCallback(byte[] aflist) {
        Log.e(TAG, "AflistCallback enter ");
        if (aflist == null) {
            Log.e(TAG, "aflist null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(aflist, aflist.length);
        FmReceiver.mCallback.FmRxEvRdsAfInfo();
        Log.e(TAG, "AflistCallback exit ");
    }

    public void getSigThCallback(int val, int status) {
        Log.d(TAG, "get Signal Threshold callback");
        FmReceiver.mCallback.FmRxEvGetSignalThreshold(val, status);
    }

    public void getChDetThCallback(int val, int status) {
        FmReceiver.mCallback.FmRxEvGetChDetThreshold(val, status);
    }

    public void setChDetThCallback(int status) {
        FmReceiver.mCallback.FmRxEvSetChDetThreshold(status);
    }

    public void DefDataRdCallback(int val, int status) {
        FmReceiver.mCallback.FmRxEvDefDataRead(val, status);
    }

    public void DefDataWrtCallback(int status) {
        FmReceiver.mCallback.FmRxEvDefDataWrite(status);
    }

    public void getBlendCallback(int val, int status) {
        FmReceiver.mCallback.FmRxEvGetBlend(val, status);
    }

    public void setBlendCallback(int status) {
        FmReceiver.mCallback.FmRxEvSetBlend(status);
    }

    public void getStnParamCallback(int val, int status) {
        FmReceiver.mCallback.FmRxGetStationParam(val, status);
    }

    public void getStnDbgParamCallback(int val, int status) {
        FmReceiver.mCallback.FmRxGetStationDbgParam(val, status);
    }

    public void enableSlimbusCallback(int status) {
        Log.d(TAG, "++enableSlimbusCallback");
        FmReceiver.mCallback.FmRxEvEnableSlimbus(status);
        Log.d(TAG, "--enableSlimbusCallback");
    }

    public void enableSoftMuteCallback(int status) {
        Log.d(TAG, "++enableSoftMuteCallback");
        FmReceiver.mCallback.FmRxEvEnableSoftMute(status);
        Log.d(TAG, "--enableSoftMuteCallback");
    }

    public void RtPlusCallback(byte[] rtplus) {
        Log.d(TAG, "RtPlusCallback enter ");
        if (rtplus == null) {
            Log.e(TAG, "psInfo null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(rtplus, rtplus.length);
        FmReceiver.mCallback.FmRxEvRTPlus();
        Log.d(TAG, "RtPlusCallback exit ");
    }

    public void RtCallback(byte[] rt) {
        Log.d(TAG, "RtCallback enter ");
        if (rt == null) {
            Log.e(TAG, "psInfo null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(rt, rt.length);
        FmReceiver.mCallback.FmRxEvRdsRtInfo();
        Log.d(TAG, "RtCallback exit ");
    }

    public void ErtCallback(byte[] ert) {
        Log.d(TAG, "ErtCallback enter ");
        if (ert == null) {
            Log.e(TAG, "ERT null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(ert, ert.length);
        FmReceiver.mCallback.FmRxEvERTInfo();
        Log.d(TAG, "RtCallback exit ");
    }

    public void EccCallback(byte[] ecc) {
        Log.i(TAG, "EccCallback enter ");
        if (ecc == null) {
            Log.e(TAG, "ECC null return  ");
            return;
        }
        mRdsBuffer = Arrays.copyOf(ecc, ecc.length);
        FmReceiver.mCallback.FmRxEvECCInfo();
        Log.i(TAG, "EccCallback exit ");
    }

    public void PsInfoCallback(byte[] psInfo) {
        Log.d(TAG, "PsInfoCallback enter ");
        if (psInfo == null) {
            Log.e(TAG, "psInfo null return  ");
            return;
        }
        Log.e(TAG, "length =  " + psInfo.length);
        mRdsBuffer = Arrays.copyOf(psInfo, psInfo.length);
        FmReceiver.mCallback.FmRxEvRdsPsInfo();
        Log.d(TAG, "PsInfoCallback exit");
    }

    public void enableCallback() {
        Log.d(TAG, "enableCallback enter");
        FmTransceiver.setFMPowerState(1);
        Log.v(TAG, "RxEvtList: CURRENT-STATE : FMRxStarting ---> NEW-STATE : FMRxOn");
        FmReceiver.mCallback.FmRxEvEnableReceiver();
        Log.d(TAG, "enableCallback exit");
    }

    public void tuneCallback(int freq) {
        Log.d(TAG, "tuneCallback enter");
        int state = FmReceiver.getSearchState();
        switch (state) {
            case 0:
                break;
            case 4:
                Log.v(TAG, "Current state is SRCH_ABORTED");
                Log.v(TAG, "Aborting on-going search command...");
                break;
            default:
                if (freq <= 0) {
                    Log.e(TAG, "get frequency command failed");
                    break;
                } else {
                    FmReceiver.mCallback.FmRxEvRadioTuneStatus(freq);
                    break;
                }
        }
        Log.v(TAG, "Current state is " + state);
        FmReceiver.setSearchState(3);
        Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
        FmReceiver.mCallback.FmRxEvSearchComplete(freq);
        Log.d(TAG, "tuneCallback exit");
    }

    public void seekCmplCallback(int freq) {
        Log.d(TAG, "seekCmplCallback enter");
        int state = FmReceiver.getSearchState();
        switch (state) {
            case 1:
                Log.v(TAG, "Current state is " + state);
                FmReceiver.setSearchState(3);
                Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE :FMRxOn");
                FmReceiver.mCallback.FmRxEvSearchComplete(freq);
                break;
            case 4:
                Log.v(TAG, "Current state is SRCH_ABORTED");
                Log.v(TAG, "Aborting on-going search command...");
                FmReceiver.setSearchState(3);
                Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
                FmReceiver.mCallback.FmRxEvSearchComplete(freq);
                break;
        }
        Log.d(TAG, "seekCmplCallback exit");
    }

    public void srchListCallback(byte[] scan_tbl) {
        switch (FmReceiver.getSearchState()) {
            case 2:
                Log.v(TAG, "FmRxEventListener: Current state is AUTO_PRESET_INPROGRESS");
                FmReceiver.setSearchState(3);
                Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
                FmReceiver.mCallback.FmRxEvSearchListComplete();
                return;
            case 4:
                Log.v(TAG, "Current state is SRCH_ABORTED");
                Log.v(TAG, "Aborting on-going SearchList command...");
                FmReceiver.setSearchState(3);
                Log.v(TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
                FmReceiver.mCallback.FmRxEvSearchCancelled();
                return;
            default:
                return;
        }
    }

    public void scanNxtCallback() {
        Log.d(TAG, "scanNxtCallback enter");
        FmReceiver.mCallback.FmRxEvSearchInProgress();
        Log.d(TAG, "scanNxtCallback exit");
    }

    public void stereostsCallback(boolean stereo) {
        Log.d(TAG, "stereostsCallback enter");
        FmReceiver.mCallback.FmRxEvStereoStatus(stereo);
        Log.d(TAG, "stereostsCallback exit");
    }

    public void rdsAvlStsCallback(boolean rdsAvl) {
        Log.d(TAG, "rdsAvlStsCallback enter");
        FmReceiver.mCallback.FmRxEvRdsLockStatus(rdsAvl);
        Log.d(TAG, "rdsAvlStsCallback exit");
    }

    public void disableCallback() {
        Log.d(TAG, "disableCallback enter");
        if (FmTransceiver.getFMPowerState() == 6) {
            FmTransceiver.setFMPowerState(0);
            Log.v(TAG, "RxEvtList: CURRENT-STATE : FMTurningOff ---> NEW-STATE : FMOff");
            FmReceiver.mCallback.FmRxEvDisableReceiver();
            return;
        }
        FmTransceiver.setFMPowerState(0);
        Log.d(TAG, "Unexpected RADIO_DISABLED recvd");
        Log.v(TAG, "RxEvtList: CURRENT-STATE : FMRxOn ---> NEW-STATE : FMOff");
        FmReceiver.mCallback.FmRxEvRadioReset();
        Log.d(TAG, "disableCallback exit");
    }

    public FmReceiverJNI(FmRxEvCallbacks callback) {
        this.mCallback = callback;
        if (this.mCallback == null) {
            Log.e(TAG, "mCallback is null in JNI");
        }
        Log.d(TAG, "init native called");
        initNative();
    }

    public FmReceiverJNI() {
        Log.d(TAG, "FmReceiverJNI constructor called");
    }
}

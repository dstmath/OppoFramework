package qcom.fmradio;

import android.util.Log;
import java.util.Arrays;

class FmRxEventListner {
    private static final String TAG = "FMRadio";
    private final int EVENT_LISTEN = 1;
    private final int STD_BUF_SIZE = 256;
    private Thread mThread;

    private enum FmRxEvents {
        READY_EVENT,
        TUNE_EVENT,
        SEEK_COMPLETE_EVENT,
        SCAN_NEXT_EVENT,
        RAW_RDS_EVENT,
        RT_EVENT,
        PS_EVENT,
        ERROR_EVENT,
        BELOW_TH_EVENT,
        ABOVE_TH_EVENT,
        STEREO_EVENT,
        MONO_EVENT,
        RDS_AVAL_EVENT,
        RDS_NOT_AVAL_EVENT,
        TAVARUA_EVT_NEW_SRCH_LIST,
        TAVARUA_EVT_NEW_AF_LIST
    }

    FmRxEventListner() {
    }

    public void startListner(final int fd, final FmRxEvCallbacks cb) {
        this.mThread = new Thread() {
            public void run() {
                byte[] buff = new byte[256];
                Log.d(FmRxEventListner.TAG, "Starting listener " + fd);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Arrays.fill(buff, (byte) 0);
                        int eventCount = FmReceiverJNI.getBufferNative(fd, buff, 1);
                        if (eventCount >= 0) {
                            Log.d(FmRxEventListner.TAG, "Received event. Count: " + eventCount);
                        }
                        for (int index = 0; index < eventCount; index++) {
                            Log.d(FmRxEventListner.TAG, "Received <" + buff[index] + ">");
                            int state;
                            switch (buff[index]) {
                                case (byte) 0:
                                    Log.d(FmRxEventListner.TAG, "Got READY_EVENT");
                                    if (FmTransceiver.getFMPowerState() != 4) {
                                        if (FmTransceiver.getFMPowerState() != 6) {
                                            break;
                                        }
                                        FmTransceiver.setFMPowerState(0);
                                        Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : FMTurningOff ---> NEW-STATE : FMOff");
                                        FmTransceiver.release("/dev/radio0");
                                        cb.FmRxEvDisableReceiver();
                                        Thread.currentThread().interrupt();
                                        break;
                                    }
                                    FmTransceiver.setFMPowerState(1);
                                    Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : FMRxStarting ---> NEW-STATE : FMRxOn");
                                    cb.FmRxEvEnableReceiver();
                                    FmReceiverJNI.configurePerformanceParams(fd);
                                    break;
                                case (byte) 1:
                                    Log.d(FmRxEventListner.TAG, "Got TUNE_EVENT");
                                    int freq = FmReceiverJNI.getFreqNative(fd);
                                    state = FmReceiver.getSearchState();
                                    switch (state) {
                                        case 0:
                                            break;
                                        case 4:
                                            Log.v(FmRxEventListner.TAG, "Current state is SRCH_ABORTED");
                                            Log.v(FmRxEventListner.TAG, "Aborting on-going search command...");
                                            break;
                                        default:
                                            if (freq <= 0) {
                                                Log.e(FmRxEventListner.TAG, "get frequency command failed");
                                                break;
                                            } else {
                                                cb.FmRxEvRadioTuneStatus(freq);
                                                break;
                                            }
                                    }
                                    Log.v(FmRxEventListner.TAG, "Current state is " + state);
                                    FmReceiver.setSearchState(3);
                                    Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
                                    cb.FmRxEvSearchComplete(freq);
                                    break;
                                case (byte) 2:
                                    Log.d(FmRxEventListner.TAG, "Got SEEK_COMPLETE_EVENT");
                                    state = FmReceiver.getSearchState();
                                    switch (state) {
                                        case 1:
                                            Log.v(FmRxEventListner.TAG, "Current state is " + state);
                                            FmReceiver.setSearchState(3);
                                            Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE :FMRxOn");
                                            cb.FmRxEvSearchComplete(FmReceiverJNI.getFreqNative(fd));
                                            break;
                                        case 4:
                                            Log.v(FmRxEventListner.TAG, "Current state is SRCH_ABORTED");
                                            Log.v(FmRxEventListner.TAG, "Aborting on-going search command...");
                                            FmReceiver.setSearchState(3);
                                            Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
                                            cb.FmRxEvSearchComplete(FmReceiverJNI.getFreqNative(fd));
                                            break;
                                        default:
                                            break;
                                    }
                                case (byte) 3:
                                    Log.d(FmRxEventListner.TAG, "Got SCAN_NEXT_EVENT");
                                    cb.FmRxEvSearchInProgress();
                                    break;
                                case (byte) 4:
                                    Log.d(FmRxEventListner.TAG, "Got RAW_RDS_EVENT");
                                    cb.FmRxEvRdsGroupData();
                                    break;
                                case (byte) 5:
                                    Log.d(FmRxEventListner.TAG, "Got RT_EVENT");
                                    cb.FmRxEvRdsRtInfo();
                                    break;
                                case (byte) 6:
                                    Log.d(FmRxEventListner.TAG, "Got PS_EVENT");
                                    cb.FmRxEvRdsPsInfo();
                                    break;
                                case (byte) 7:
                                    Log.d(FmRxEventListner.TAG, "Got ERROR_EVENT");
                                    break;
                                case (byte) 8:
                                    Log.d(FmRxEventListner.TAG, "Got BELOW_TH_EVENT");
                                    cb.FmRxEvServiceAvailable(false);
                                    break;
                                case FmReceiver.FM_RX_SRCHLIST_MODE_WEAKEST /*9*/:
                                    Log.d(FmRxEventListner.TAG, "Got ABOVE_TH_EVENT");
                                    cb.FmRxEvServiceAvailable(true);
                                    break;
                                case (byte) 10:
                                    Log.d(FmRxEventListner.TAG, "Got STEREO_EVENT");
                                    cb.FmRxEvStereoStatus(true);
                                    break;
                                case (byte) 11:
                                    Log.d(FmRxEventListner.TAG, "Got MONO_EVENT");
                                    cb.FmRxEvStereoStatus(false);
                                    break;
                                case FmReceiver.FM_RX_SRCHLIST_MAX_STATIONS /*12*/:
                                    Log.d(FmRxEventListner.TAG, "Got RDS_AVAL_EVENT");
                                    cb.FmRxEvRdsLockStatus(true);
                                    break;
                                case (byte) 13:
                                    Log.d(FmRxEventListner.TAG, "Got RDS_NOT_AVAL_EVENT");
                                    cb.FmRxEvRdsLockStatus(false);
                                    break;
                                case (byte) 14:
                                    Log.d(FmRxEventListner.TAG, "Got NEW_SRCH_LIST");
                                    switch (FmReceiver.getSearchState()) {
                                        case 2:
                                            Log.v(FmRxEventListner.TAG, "FmRxEventListener: Current state is AUTO_PRESET_INPROGRESS");
                                            FmReceiver.setSearchState(3);
                                            Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
                                            cb.FmRxEvSearchListComplete();
                                            break;
                                        case 4:
                                            Log.v(FmRxEventListner.TAG, "Current state is SRCH_ABORTED");
                                            Log.v(FmRxEventListner.TAG, "Aborting on-going SearchList command...");
                                            FmReceiver.setSearchState(3);
                                            Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : Search ---> NEW-STATE : FMRxOn");
                                            cb.FmRxEvSearchCancelled();
                                            break;
                                        default:
                                            break;
                                    }
                                case (byte) 15:
                                    Log.d(FmRxEventListner.TAG, "Got NEW_AF_LIST");
                                    cb.FmRxEvRdsAfInfo();
                                    break;
                                case (byte) 18:
                                    Log.d(FmRxEventListner.TAG, "Got RADIO_DISABLED");
                                    if (FmTransceiver.getFMPowerState() != 6) {
                                        Log.d(FmRxEventListner.TAG, "Unexpected RADIO_DISABLED recvd");
                                        FmTransceiver.release("/dev/radio0");
                                        cb.FmRxEvRadioReset();
                                        FmTransceiver.setFMPowerState(0);
                                        Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : FMRxOn ---> NEW-STATE : FMOff");
                                        Thread.currentThread().interrupt();
                                        break;
                                    }
                                    FmTransceiver.release("/dev/radio0");
                                    FmTransceiver.setFMPowerState(0);
                                    cb.FmRxEvDisableReceiver();
                                    Log.v(FmRxEventListner.TAG, "RxEvtList: CURRENT-STATE : FMTurningOff ---> NEW-STATE : FMOff");
                                    Thread.currentThread().interrupt();
                                    break;
                                case (byte) 19:
                                    FmTransceiver.setRDSGrpMask(0);
                                    break;
                                case (byte) 20:
                                    Log.d(FmRxEventListner.TAG, "got RT plus event");
                                    cb.FmRxEvRTPlus();
                                    break;
                                case (byte) 21:
                                    Log.d(FmRxEventListner.TAG, "got eRT event");
                                    cb.FmRxEvERTInfo();
                                    break;
                                case (byte) 22:
                                    Log.d(FmRxEventListner.TAG, "got IRIS_EVT_SPUR_TBL event");
                                    FmReceiver.getSpurTableData();
                                    break;
                                default:
                                    Log.d(FmRxEventListner.TAG, "Unknown event");
                                    break;
                            }
                        }
                    } catch (Exception ex) {
                        Log.d(FmRxEventListner.TAG, "RunningThread InterruptedException");
                        ex.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        this.mThread.start();
    }

    public void stopListener() {
        Log.d(TAG, "stopping the Listener\n");
        if (this.mThread != null) {
            this.mThread.interrupt();
        }
    }
}

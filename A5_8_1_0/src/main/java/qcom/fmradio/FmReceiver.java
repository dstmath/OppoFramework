package qcom.fmradio;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class FmReceiver extends FmTransceiver {
    private static final int BUF_ERT = 12;
    private static final int BUF_RTPLUS = 11;
    static final int DISABLE_LPF = 0;
    static final int ENABLE_LPF = 1;
    private static final int ENCODE_TYPE_IND = 1;
    private static final int ERT_DIR_IND = 2;
    public static final int FM_RX_AUDIO_MODE_MONO = 1;
    public static final int FM_RX_AUDIO_MODE_STEREO = 0;
    public static final int FM_RX_DWELL_PERIOD_0S = 0;
    public static final int FM_RX_DWELL_PERIOD_1S = 1;
    public static final int FM_RX_DWELL_PERIOD_2S = 2;
    public static final int FM_RX_DWELL_PERIOD_3S = 3;
    public static final int FM_RX_DWELL_PERIOD_4S = 4;
    public static final int FM_RX_DWELL_PERIOD_5S = 5;
    public static final int FM_RX_DWELL_PERIOD_6S = 6;
    public static final int FM_RX_DWELL_PERIOD_7S = 7;
    public static final int FM_RX_LOW_POWER_MODE = 1;
    public static final int FM_RX_MUTE = 1;
    public static final int FM_RX_NORMAL_POWER_MODE = 0;
    public static final int FM_RX_RDS_GRP_AF_EBL = 8;
    public static final int FM_RX_RDS_GRP_ECC_EBL = 32;
    public static final int FM_RX_RDS_GRP_PS_EBL = 2;
    public static final int FM_RX_RDS_GRP_PS_SIMPLE_EBL = 4;
    public static final int FM_RX_RDS_GRP_PTYN_EBL = 64;
    public static final int FM_RX_RDS_GRP_RT_EBL = 1;
    public static final int FM_RX_RDS_GRP_RT_PLUS_EBL = 128;
    private static final int FM_RX_RSSI_LEVEL_STRONG = -96;
    private static final int FM_RX_RSSI_LEVEL_VERY_STRONG = -90;
    private static final int FM_RX_RSSI_LEVEL_VERY_WEAK = -105;
    private static final int FM_RX_RSSI_LEVEL_WEAK = -100;
    public static final int FM_RX_SEARCHDIR_DOWN = 0;
    public static final int FM_RX_SEARCHDIR_UP = 1;
    public static final int FM_RX_SIGNAL_STRENGTH_STRONG = 2;
    public static final int FM_RX_SIGNAL_STRENGTH_VERY_STRONG = 3;
    public static final int FM_RX_SIGNAL_STRENGTH_VERY_WEAK = 0;
    public static final int FM_RX_SIGNAL_STRENGTH_WEAK = 1;
    public static final int FM_RX_SRCHLIST_MAX_STATIONS = 12;
    public static final int FM_RX_SRCHLIST_MODE_STRONG = 2;
    public static final int FM_RX_SRCHLIST_MODE_STRONGEST = 8;
    public static final int FM_RX_SRCHLIST_MODE_WEAK = 3;
    public static final int FM_RX_SRCHLIST_MODE_WEAKEST = 9;
    public static final int FM_RX_SRCHRDS_MODE_SCAN_PTY = 5;
    public static final int FM_RX_SRCHRDS_MODE_SEEK_AF = 7;
    public static final int FM_RX_SRCHRDS_MODE_SEEK_PI = 6;
    public static final int FM_RX_SRCHRDS_MODE_SEEK_PTY = 4;
    public static final int FM_RX_SRCH_MODE_SCAN = 1;
    public static final int FM_RX_SRCH_MODE_SEEK = 0;
    public static final int FM_RX_UNMUTE = 0;
    static final int GRP_3A = 64;
    private static final int LEN_IND = 0;
    private static final int RT_OR_ERT_IND = 1;
    private static final int SEARCH_MPXDCC = 0;
    private static final int SEARCH_SINR_INT = 1;
    static final int STD_BUF_SIZE = 256;
    private static final String TAG = "FMRadio";
    private static final int TAVARUA_BUF_AF_LIST = 5;
    private static final int TAVARUA_BUF_EVENTS = 1;
    private static final int TAVARUA_BUF_MAX = 6;
    private static final int TAVARUA_BUF_PS_RDS = 3;
    private static final int TAVARUA_BUF_RAW_RDS = 4;
    private static final int TAVARUA_BUF_RT_RDS = 2;
    private static final int TAVARUA_BUF_SRCH_LIST = 0;
    private static final int V4L2_CID_PRIVATE_BASE = 134217728;
    private static final int V4L2_CID_PRIVATE_IRIS_GET_SPUR_TBL = 9963822;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH = 134217736;
    private static final int V4L2_CTRL_CLASS_USER = 9961472;
    static FmRxEvCallbacks callback;
    public static FmRxEvCallbacksAdaptor mCallback;
    private static int mEnableLpf1xRtt = 64;
    private static int mEnableLpfCdma = 8;
    private static int mEnableLpfEdge = 2;
    private static int mEnableLpfEhrpd = 8192;
    private static int mEnableLpfEvdo0 = 16;
    private static int mEnableLpfEvdoA = 32;
    private static int mEnableLpfEvdoB = 2048;
    private static int mEnableLpfGprs = 1;
    private static int mEnableLpfGsm = 32768;
    private static int mEnableLpfHsdpa = FM_RX_RDS_GRP_RT_PLUS_EBL;
    private static int mEnableLpfHspa = 512;
    private static int mEnableLpfHspap = 16384;
    private static int mEnableLpfHsupa = STD_BUF_SIZE;
    private static int mEnableLpfIden = 1024;
    private static int mEnableLpfIwlan = 131072;
    private static int mEnableLpfLte = 4096;
    private static int mEnableLpfLteCa = 262144;
    private static int mEnableLpfScdma = 65536;
    private static int mEnableLpfUmts = 4;
    static FmReceiverJNI mFmReceiverJNI;
    private static int mIsBtLpfEnabled = 1;
    private static int mIsWlanLpfEnabled = 2;
    public static int mSearchState = -1;
    private IntentFilter mBtIntentFilter;
    private final BroadcastReceiver mBtReceiver;
    public PhoneStateListener mDataConnectionStateListener;
    private IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver;

    public boolean isSmdTransportLayer() {
        if (SystemProperties.get("ro.qualcomm.bt.hci_transport").equals("smd")) {
            return true;
        }
        return false;
    }

    public static boolean isRomeChip() {
        if (SystemProperties.get("qcom.bluetooth.soc").equals("rome")) {
            return true;
        }
        return false;
    }

    public static boolean isCherokeeChip() {
        if (SystemProperties.get("qcom.bluetooth.soc").equals("cherokee")) {
            return true;
        }
        return false;
    }

    public void registerDataConnectionStateListener(Context mContext) {
        Log.d(TAG, "registerDataConnectionStateListener");
        ((TelephonyManager) mContext.getSystemService("phone")).listen(this.mDataConnectionStateListener, 64);
    }

    public void unregisterDataConnectionStateListener(Context mContext) {
        Log.d(TAG, "unregisterDataConnectionStateListener: ");
        ((TelephonyManager) mContext.getSystemService("phone")).listen(this.mDataConnectionStateListener, 0);
    }

    public FmReceiver() {
        this.mDataConnectionStateListener = new PhoneStateListener() {
            public void onDataConnectionStateChanged(int state, int networkType) {
                Log.d(FmReceiver.TAG, "state: " + Integer.toString(state) + " networkType: " + Integer.toString(networkType));
                if (state == 2) {
                    FmReceiver.this.FMcontrolLowPassFilter(state, networkType, 1);
                } else if (state == 0) {
                    FmReceiver.this.FMcontrolLowPassFilter(state, networkType, 0);
                }
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d(FmReceiver.TAG, "onReceive: Wifi State change intent");
                if ("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                    int newState = intent.getIntExtra("wifi_state", 4);
                    int mBtWlanLpf = SystemProperties.getInt("persist.btwlan.lpfenabler", 0);
                    if (newState == 3) {
                        Log.d(FmReceiver.TAG, "enable LPF on wifi enabled " + newState);
                        if ((FmReceiver.mIsWlanLpfEnabled & mBtWlanLpf) == FmReceiver.mIsWlanLpfEnabled) {
                            FmReceiver.this.mControl.enableLPF(FmReceiver.sFd, 1);
                            return;
                        }
                        return;
                    } else if ((FmReceiver.mIsWlanLpfEnabled & mBtWlanLpf) == FmReceiver.mIsWlanLpfEnabled) {
                        Log.d(FmReceiver.TAG, "Disable LPF on wifi state other than enabled " + newState);
                        FmReceiver.this.mControl.enableLPF(FmReceiver.sFd, 0);
                        return;
                    } else {
                        return;
                    }
                }
                Log.d(FmReceiver.TAG, "WIFI_STATE_CHANGED_ACTION failed");
            }
        };
        this.mBtReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d(FmReceiver.TAG, "onReceive: Bluetooth State change intent");
                if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                    int newState = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE);
                    int mBtWlanLpf = SystemProperties.getInt("persist.btwlan.lpfenabler", 0);
                    if (newState == 12) {
                        Log.d(FmReceiver.TAG, "enable LPF on BT enabled " + newState);
                        if ((FmReceiver.mIsBtLpfEnabled & mBtWlanLpf) == FmReceiver.mIsBtLpfEnabled) {
                            FmReceiver.this.mControl.enableLPF(FmReceiver.sFd, 1);
                            return;
                        }
                        return;
                    } else if ((FmReceiver.mIsBtLpfEnabled & mBtWlanLpf) == FmReceiver.mIsBtLpfEnabled) {
                        Log.d(FmReceiver.TAG, "Disable LPF on BT state other than enabled " + newState);
                        FmReceiver.this.mControl.enableLPF(FmReceiver.sFd, 0);
                        return;
                    } else {
                        return;
                    }
                }
                Log.d(FmReceiver.TAG, "ACTION_STATE_CHANGED failed");
            }
        };
        this.mControl = new FmRxControls();
        this.mRdsData = new FmRxRdsData(sFd);
        this.mRxEvents = new FmRxEventListner();
    }

    public FmReceiver(String devicePath, FmRxEvCallbacksAdaptor callback) throws InstantiationException {
        this.mDataConnectionStateListener = /* anonymous class already generated */;
        this.mReceiver = /* anonymous class already generated */;
        this.mBtReceiver = /* anonymous class already generated */;
        this.mControl = new FmRxControls();
        this.mRxEvents = new FmRxEventListner();
        Log.e(TAG, "FmReceiver constructor");
        mCallback = callback;
        if (isCherokeeChip()) {
            mFmReceiverJNI = new FmReceiverJNI(mCallback);
        }
    }

    public boolean registerClient(FmRxEvCallbacks callback) {
        return super.registerClient(callback);
    }

    public boolean unregisterClient() {
        return super.unregisterClient();
    }

    public boolean enable(FmConfig configSettings, Context app_context) {
        int state = getFMState();
        this.mIntentFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        this.mBtIntentFilter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
        if (state == 1 || state == 3) {
            Log.d(TAG, "enable: FM already turned On and running");
            return false;
        } else if (state == 6) {
            Log.v(TAG, "FM is in the process of turning off.Pls wait for sometime.");
            return false;
        } else if (state == 4) {
            Log.v(TAG, "FM is in the process of turning On.Pls wait for sometime.");
            return false;
        } else if (state == 2 || state == 5) {
            Log.v(TAG, "FM Tx is turned on or in the process of turning on.");
            return false;
        } else {
            FmTransceiver.setFMPowerState(4);
            Log.v(TAG, "enable: CURRENT-STATE : FMOff ---> NEW-STATE : FMRxStarting");
            boolean status = super.enable(configSettings, 1);
            if (status) {
                if (!isCherokeeChip()) {
                    status = registerClient(mCallback);
                }
                this.mRdsData = new FmRxRdsData(sFd);
                registerDataConnectionStateListener(app_context);
                app_context.registerReceiver(this.mReceiver, this.mIntentFilter);
                int mBtWlanLpf = SystemProperties.getInt("persist.btwlan.lpfenabler", 0);
                if (((WifiManager) app_context.getSystemService("wifi")).getWifiState() == 3 && (mIsWlanLpfEnabled & mBtWlanLpf) == mIsWlanLpfEnabled) {
                    Log.d(TAG, "enable LPF if WIFI is already on");
                    this.mControl.enableLPF(sFd, 1);
                }
                app_context.registerReceiver(this.mBtReceiver, this.mBtIntentFilter);
                if (BluetoothAdapter.getDefaultAdapter() != null && (mIsWlanLpfEnabled & mBtWlanLpf) == mIsWlanLpfEnabled) {
                    Log.d(TAG, "enable LPF if BT is already on");
                    this.mControl.enableLPF(sFd, 1);
                }
            } else {
                status = false;
                Log.e(TAG, "enable: Error while turning FM On");
                Log.e(TAG, "enable: CURRENT-STATE : FMRxStarting ---> NEW-STATE : FMOff");
                FmTransceiver.setFMPowerState(0);
            }
            return status;
        }
    }

    public boolean reset() {
        if (getFMState() == 0) {
            Log.d(TAG, "FM already turned Off.");
            return false;
        }
        FmTransceiver.setFMPowerState(0);
        Log.v(TAG, "reset: NEW-STATE : FMState_Turned_Off");
        boolean status = unregisterClient();
        FmTransceiver.release("/dev/radio0");
        return status;
    }

    public boolean disable(Context app_context) {
        switch (getFMState()) {
            case 0:
                Log.d(TAG, "FM already tuned Off.");
                return false;
            case 3:
                Log.v(TAG, "disable: Cancelling the on going search operation prior to disabling FM");
                setSearchState(4);
                cancelSearch();
                Log.v(TAG, "disable: Wait for the state to change from : Search ---> FMRxOn");
                try {
                    Thread.sleep(50);
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            case 4:
                Log.d(TAG, "disable: FM not yet turned On...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                if (getFMState() == 4) {
                    Log.e(TAG, "disable: FM in bad state");
                    return false;
                }
                break;
            case 6:
                Log.v(TAG, "disable: FM is getting turned Off.");
                return false;
        }
        FmTransceiver.setFMPowerState(6);
        Log.v(TAG, "disable: CURRENT-STATE : FMRxOn ---> NEW-STATE : FMTurningOff");
        super.disable();
        unregisterDataConnectionStateListener(app_context);
        app_context.unregisterReceiver(this.mBtReceiver);
        app_context.unregisterReceiver(this.mReceiver);
        return true;
    }

    static int getSearchState() {
        return mSearchState;
    }

    static void setSearchState(int state) {
        mSearchState = state;
        switch (mSearchState) {
            case 0:
            case 1:
            case 2:
                FmTransceiver.setFMPowerState(3);
                return;
            case 3:
                mSearchState = -1;
                FmTransceiver.setFMPowerState(1);
                return;
            case 4:
                return;
            default:
                mSearchState = -1;
                return;
        }
    }

    public boolean searchStations(int mode, int dwellPeriod, int direction) {
        int state = getFMState();
        boolean bStatus = true;
        if (state == 0 || state == 3) {
            Log.d(TAG, "searchStations: Device currently busy in executing another command.");
            return false;
        }
        Log.d(TAG, "Basic search...");
        if (!(mode == 0 || mode == 1)) {
            Log.d(TAG, "Invalid search mode: " + mode);
            bStatus = false;
        }
        if (dwellPeriod < 0 || dwellPeriod > 7) {
            Log.d(TAG, "Invalid dwelling time: " + dwellPeriod);
            bStatus = false;
        }
        if (!(direction == 0 || direction == 1)) {
            Log.d(TAG, "Invalid search direction: " + direction);
            bStatus = false;
        }
        if (bStatus) {
            Log.d(TAG, "searchStations: mode " + mode + "direction:  " + direction);
            if (mode == 0) {
                setSearchState(0);
            } else if (mode == 1) {
                setSearchState(1);
            }
            Log.v(TAG, "searchStations: CURRENT-STATE : FMRxOn ---> NEW-STATE : SearchInProg");
            if (this.mControl.searchStations(sFd, mode, dwellPeriod, direction, 0, 0) != 0) {
                Log.e(TAG, "search station failed");
                if (getFMState() == 3) {
                    setSearchState(3);
                }
                return false;
            } else if (getFMState() == 0) {
                Log.d(TAG, "searchStations: CURRENT-STATE : FMState_Off (unexpected)");
                return false;
            }
        }
        return bStatus;
    }

    public boolean searchStations(int mode, int dwellPeriod, int direction, int pty, int pi) {
        boolean bStatus = true;
        int state = getFMState();
        if (state == 0 || state == 3) {
            Log.d(TAG, "searchStations: Device currently busy in executing another command.");
            return false;
        }
        Log.d(TAG, "RDS search...");
        if (!(mode == 4 || mode == 5 || mode == 6 || mode == 7)) {
            Log.d(TAG, "Invalid search mode: " + mode);
            bStatus = false;
        }
        if (dwellPeriod < 1 || dwellPeriod > 7) {
            Log.d(TAG, "Invalid dwelling time: " + dwellPeriod);
            bStatus = false;
        }
        if (!(direction == 0 || direction == 1)) {
            Log.d(TAG, "Invalid search direction: " + direction);
            bStatus = false;
        }
        if (bStatus) {
            Log.d(TAG, "searchStations: mode " + mode);
            Log.d(TAG, "searchStations: dwellPeriod " + dwellPeriod);
            Log.d(TAG, "searchStations: direction " + direction);
            Log.d(TAG, "searchStations: pty " + pty);
            Log.d(TAG, "searchStations: pi " + pi);
            setSearchState(1);
            if (this.mControl.searchStations(sFd, mode, dwellPeriod, direction, pty, pi) != 0) {
                Log.e(TAG, "scan station failed");
                if (getFMState() == 3) {
                    setSearchState(3);
                }
                bStatus = false;
            }
        }
        return bStatus;
    }

    public boolean searchStationList(int mode, int direction, int maximumStations, int pty) {
        int state = getFMState();
        boolean bStatus = true;
        if (state == 0 || state == 3) {
            Log.d(TAG, "searchStationList: Device currently busy in executing another command.");
            return false;
        }
        Log.d(TAG, "searchStations: mode " + mode);
        Log.d(TAG, "searchStations: direction " + direction);
        Log.d(TAG, "searchStations: maximumStations " + maximumStations);
        Log.d(TAG, "searchStations: pty " + pty);
        if (!(mode == 2 || mode == 3 || mode == 8 || mode == 9)) {
            bStatus = false;
        }
        if (maximumStations < 0 || maximumStations > 12) {
            bStatus = false;
        }
        if (!(direction == 0 || direction == 1)) {
            bStatus = false;
        }
        if (bStatus) {
            int re;
            setSearchState(2);
            Log.v(TAG, "searchStationList: CURRENT-STATE : FMRxOn ---> NEW-STATE : SearchInProg");
            if (mode == 8 || mode == 9) {
                re = this.mControl.searchStationList(sFd, mode == 8 ? 2 : 3, 0, direction, pty);
            } else {
                re = this.mControl.searchStationList(sFd, mode, maximumStations, direction, pty);
            }
            if (re != 0) {
                Log.e(TAG, "search station list failed");
                if (getFMState() == 3) {
                    setSearchState(3);
                }
                bStatus = false;
            }
        }
        return bStatus;
    }

    public boolean cancelSearch() {
        if (getFMState() == 3) {
            Log.v(TAG, "cancelSearch: Cancelling the on going search operation");
            setSearchState(4);
            this.mControl.cancelSearch(sFd);
            return true;
        }
        Log.d(TAG, "cancelSearch: No on going search operation to cancel");
        return false;
    }

    public boolean setMuteMode(int mode) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            Log.d(TAG, "setMuteMode: Device currently busy in executing another command.");
            return false;
        }
        switch (mode) {
            case 0:
                this.mControl.muteControl(sFd, false);
                break;
            case 1:
                this.mControl.muteControl(sFd, true);
                break;
        }
        return true;
    }

    public boolean setStereoMode(boolean stereoEnable) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            Log.d(TAG, "setStereoMode: Device currently busy in executing another command.");
            return false;
        } else if (this.mControl.stereoControl(sFd, stereoEnable) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean setSignalThreshold(int threshold) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            Log.d(TAG, "setSignalThreshold: Device currently busy in executing another command.");
            return false;
        }
        boolean bStatus = true;
        Log.d(TAG, "Signal Threshhold input: " + threshold);
        int rssiLev;
        switch (threshold) {
            case 0:
                rssiLev = FM_RX_RSSI_LEVEL_VERY_WEAK;
                break;
            case 1:
                rssiLev = FM_RX_RSSI_LEVEL_WEAK;
                break;
            case 2:
                rssiLev = FM_RX_RSSI_LEVEL_STRONG;
                break;
            case 3:
                rssiLev = FM_RX_RSSI_LEVEL_VERY_STRONG;
                break;
            default:
                Log.d(TAG, "Invalid threshold: " + threshold);
                return false;
        }
        if (!(1 == null || FmReceiverJNI.setControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH, rssiLev) == 0)) {
            bStatus = false;
        }
        return bStatus;
    }

    public int getTunedFrequency() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            Log.d(TAG, "getTunedFrequency: Device currently busy in executing another command.");
            return -1;
        }
        int frequency = FmReceiverJNI.getFreqNative(sFd);
        Log.d(TAG, "getFrequency: " + frequency);
        return frequency;
    }

    public FmRxRdsData getPSInfo() {
        byte[] buff = new byte[STD_BUF_SIZE];
        if (isCherokeeChip()) {
            buff = FmReceiverJNI.getPsBuffer(buff);
        } else {
            FmReceiverJNI.getBufferNative(sFd, buff, 3);
        }
        int pi = ((buff[2] & 255) << 8) | (buff[3] & 255);
        Log.d(TAG, "PI= " + pi);
        this.mRdsData.setPrgmId(pi);
        this.mRdsData.setPrgmType(buff[1] & 31);
        int numOfPs = buff[0] & 15;
        Log.d(TAG, "numofpsI= " + numOfPs);
        try {
            this.mRdsData.setPrgmServices(new String(buff, 5, numOfPs * 8));
        } catch (StringIndexOutOfBoundsException e) {
            Log.d(TAG, "Number of PS names " + numOfPs);
        }
        return this.mRdsData;
    }

    public FmRxRdsData getRTInfo() {
        byte[] buff = new byte[STD_BUF_SIZE];
        if (isCherokeeChip()) {
            buff = FmReceiverJNI.getPsBuffer(buff);
        } else {
            FmReceiverJNI.getBufferNative(sFd, buff, 2);
        }
        String rdsStr = new String(buff);
        this.mRdsData.setPrgmId(((buff[2] & 255) << 8) | (buff[3] & 255));
        this.mRdsData.setPrgmType(buff[1] & 31);
        try {
            this.mRdsData.setRadioText(rdsStr.substring(5, buff[0] + 5));
        } catch (StringIndexOutOfBoundsException e) {
            Log.d(TAG, "StringIndexOutOfBoundsException ...");
        }
        return this.mRdsData;
    }

    public FmRxRdsData getRTPlusInfo() {
        byte[] rt_plus = new byte[STD_BUF_SIZE];
        String rt = "";
        byte j = (byte) 2;
        if (isCherokeeChip()) {
            rt_plus = FmReceiverJNI.getPsBuffer(rt_plus);
        } else {
            int bufferNative = FmReceiverJNI.getBufferNative(sFd, rt_plus, BUF_RTPLUS);
        }
        if (rt_plus[0] > 0) {
            if (rt_plus[1] == (byte) 0) {
                rt = this.mRdsData.getRadioText();
            } else {
                rt = this.mRdsData.getERadioText();
            }
            if (rt == "" || rt == null) {
                this.mRdsData.setTagNums(0);
            } else {
                int rt_len = rt.length();
                this.mRdsData.setTagNums(0);
                int i = 1;
                while (true) {
                    byte j2 = j;
                    if (i > 2 || j2 >= rt_plus[0]) {
                    } else {
                        int j3 = j2 + 1;
                        byte tag_code = rt_plus[j2];
                        int j4 = j3 + 1;
                        byte tag_start_pos = rt_plus[j3];
                        j = j4 + 1;
                        byte tag_len = rt_plus[j4];
                        if (tag_len + tag_start_pos <= rt_len && tag_code > (byte) 0) {
                            this.mRdsData.setTagValue(rt.substring(tag_start_pos, tag_len + tag_start_pos), i);
                            this.mRdsData.setTagCode(tag_code, i);
                        }
                        i++;
                    }
                }
            }
        } else {
            this.mRdsData.setTagNums(0);
        }
        return this.mRdsData;
    }

    public FmRxRdsData getERTInfo() {
        byte[] raw_ert = new byte[STD_BUF_SIZE];
        String s = "";
        String encoding_type = "UCS-2";
        if (isCherokeeChip()) {
            raw_ert = FmReceiverJNI.getPsBuffer(raw_ert);
        } else {
            int bufferNative = FmReceiverJNI.getBufferNative(sFd, raw_ert, 12);
        }
        if (raw_ert[0] > 0) {
            byte[] ert_text = new byte[raw_ert[0]];
            for (int i = 3; i - 3 < raw_ert[0]; i++) {
                ert_text[i - 3] = raw_ert[i];
            }
            if (raw_ert[1] == (byte) 1) {
                encoding_type = "UTF-8";
            }
            try {
                s = new String(ert_text, encoding_type);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mRdsData.setERadioText(s);
            if (raw_ert[2] == (byte) 0) {
                this.mRdsData.setFormatDir(false);
            } else {
                this.mRdsData.setFormatDir(true);
            }
            Log.d(TAG, "eRT: " + s + "dir: " + raw_ert[2]);
        }
        return this.mRdsData;
    }

    public FmRxRdsData getECCInfo() {
        byte[] raw_ecc = FmReceiverJNI.getPsBuffer(new byte[STD_BUF_SIZE]);
        int bytes_read = raw_ecc[0];
        Log.d(TAG, "bytes_read = " + bytes_read);
        if (bytes_read > 0) {
            int ecc_code = raw_ecc[9] & 255;
            this.mRdsData.setECountryCode(ecc_code);
            Log.d(TAG, "ECC code: " + ecc_code);
        }
        return this.mRdsData;
    }

    public int[] getAFInfo() {
        byte[] buff = new byte[STD_BUF_SIZE];
        int[] AfList = new int[50];
        if (isCherokeeChip()) {
            buff = FmReceiverJNI.getPsBuffer(buff);
        } else {
            FmReceiverJNI.getBufferNative(sFd, buff, 5);
        }
        if (isSmdTransportLayer() || isRomeChip() || isCherokeeChip()) {
            Log.d(TAG, "SMD transport layer or Rome chip");
            Log.d(TAG, "tunedFreq = " + ((((buff[0] & 255) | ((buff[1] & 255) << 8)) | ((buff[2] & 255) << 16)) | ((buff[3] & 255) << 24)));
            Log.d(TAG, "PI: " + ((buff[4] & 255) | ((buff[5] & 255) << 8)));
            int size_AFLIST = buff[6] & 255;
            Log.d(TAG, "size_AFLIST : " + size_AFLIST);
            for (int i = 0; i < size_AFLIST; i++) {
                AfList[i] = (((buff[((i * 4) + 6) + 1] & 255) | ((buff[((i * 4) + 6) + 2] & 255) << 8)) | ((buff[((i * 4) + 6) + 3] & 255) << 16)) | ((buff[((i * 4) + 6) + 4] & 255) << 24);
                Log.d(TAG, "AF: " + AfList[i]);
            }
        } else if (buff[4] <= (byte) 0 || buff[4] > (byte) 25) {
            return null;
        } else {
            int lowerBand = FmReceiverJNI.getLowerBandNative(sFd);
            Log.d(TAG, "Low band " + lowerBand);
            Log.d(TAG, "AF_buff 0: " + (buff[0] & 255));
            Log.d(TAG, "AF_buff 1: " + (buff[1] & 255));
            Log.d(TAG, "AF_buff 2: " + (buff[2] & 255));
            Log.d(TAG, "AF_buff 3: " + (buff[3] & 255));
            Log.d(TAG, "AF_buff 4: " + (buff[4] & 255));
            for (byte i2 = (byte) 0; i2 < buff[4]; i2++) {
                AfList[i2] = ((buff[i2 + 4] & 255) * 1000) + lowerBand;
                Log.d(TAG, "AF : " + AfList[i2]);
            }
        }
        return AfList;
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

    public int getPowerMode() {
        return this.mControl.getPwrMode(sFd);
    }

    public int[] getRssiLimit() {
        return new int[]{0, 100};
    }

    public int getSignalThreshold() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            Log.d(TAG, "getSignalThreshold: Device currently busy in executing another command.");
            return -1;
        }
        int signalStrength;
        int threshold = 0;
        int rmssiThreshold = FmReceiverJNI.getControlNative(sFd, V4L2_CID_PRIVATE_TAVARUA_SIGNAL_TH);
        Log.d(TAG, "Signal Threshhold: " + rmssiThreshold);
        if (FM_RX_RSSI_LEVEL_VERY_WEAK < rmssiThreshold && rmssiThreshold <= FM_RX_RSSI_LEVEL_WEAK) {
            signalStrength = FM_RX_RSSI_LEVEL_WEAK;
        } else if (FM_RX_RSSI_LEVEL_WEAK < rmssiThreshold && rmssiThreshold <= FM_RX_RSSI_LEVEL_STRONG) {
            signalStrength = FM_RX_RSSI_LEVEL_STRONG;
        } else if (FM_RX_RSSI_LEVEL_STRONG < rmssiThreshold) {
            signalStrength = FM_RX_RSSI_LEVEL_VERY_STRONG;
        } else {
            signalStrength = FM_RX_RSSI_LEVEL_VERY_WEAK;
        }
        switch (signalStrength) {
            case FM_RX_RSSI_LEVEL_VERY_WEAK /*-105*/:
                threshold = 0;
                break;
            case FM_RX_RSSI_LEVEL_WEAK /*-100*/:
                threshold = 1;
                break;
            case FM_RX_RSSI_LEVEL_STRONG /*-96*/:
                threshold = 2;
                break;
            case FM_RX_RSSI_LEVEL_VERY_STRONG /*-90*/:
                threshold = 3;
                break;
        }
        return threshold;
    }

    public int getAFJumpRmssiTh() {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.getAFJumpRmssiTh(sFd);
        }
        Log.d(TAG, "getAFJumpThreshold: Device currently busy in executing another command.");
        return -1;
    }

    public boolean setAFJumpRmssiTh(int th) {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.setAFJumpRmssiTh(sFd, th);
        }
        Log.d(TAG, "setAFJumpThreshold: Device currently busy in executing another command.");
        return false;
    }

    public int getAFJumpRmssiSamples() {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.getAFJumpRmssiSamples(sFd);
        }
        Log.d(TAG, "getAFJumpRmssiSamples: Device currently busy in executing another command.");
        return -1;
    }

    public boolean setAFJumpRmssiSamples(int samples) {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.setAFJumpRmssiSamples(sFd, samples);
        }
        Log.d(TAG, "setAFJumpRmssiSamples: Device currently busy in executing another command.");
        return false;
    }

    public int getGdChRmssiTh() {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.getGdChRmssiTh(sFd);
        }
        Log.d(TAG, "getGdChRmssiTh: Device currently busy in executing another command.");
        return -1;
    }

    public boolean setGdChRmssiTh(int th) {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.setGdChRmssiTh(sFd, th);
        }
        Log.d(TAG, "setGdChRmssiTh: Device currently busy in executing another command.");
        return false;
    }

    public int getSearchAlgoType() {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.getSearchAlgoType(sFd);
        }
        Log.d(TAG, "getSearchAlgoType: Device currently busy in executing another command.");
        return Integer.MAX_VALUE;
    }

    public boolean setSearchAlgoType(int searchType) {
        int state = getFMState();
        if (state == 0 || state == 3) {
            Log.d(TAG, "setSearchAlgoType: Device currently busy in executing another command.");
            return false;
        } else if (searchType == 0 || searchType == 1) {
            return this.mControl.setSearchAlgoType(sFd, searchType);
        } else {
            Log.d(TAG, "Search Algo is invalid");
            return false;
        }
    }

    public int getSinrFirstStage() {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.getSinrFirstStage(sFd);
        }
        Log.d(TAG, "getSinrFirstStage: Device currently busy in executing another command.");
        return Integer.MAX_VALUE;
    }

    public boolean setSinrFirstStage(int sinr) {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.setSinrFirstStage(sFd, sinr);
        }
        Log.d(TAG, "setSinrFirstStage: Device currently busy in executing another command.");
        return false;
    }

    public int getRmssiFirstStage() {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.getRmssiFirstStage(sFd);
        }
        Log.d(TAG, "getRmssiFirstStage: Device currently busy in executing another command.");
        return Integer.MAX_VALUE;
    }

    public boolean setRmssiFirstStage(int rmssi) {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.setRmssiFirstStage(sFd, rmssi);
        }
        Log.d(TAG, "setRmssiFirstStage: Device currently busy in executing another command.");
        return false;
    }

    public int getCFOMeanTh() {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.getCFOMeanTh(sFd);
        }
        Log.d(TAG, "getCF0Th12: Device currently busy in executing another command.");
        return Integer.MAX_VALUE;
    }

    public boolean setCFOMeanTh(int th) {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.setCFOMeanTh(sFd, th);
        }
        Log.d(TAG, "setRmssiFirstStage: Device currently busy in executing another command.");
        return false;
    }

    public boolean setPSRxRepeatCount(int count) {
        if (getFMState() != 0) {
            return this.mControl.setPSRxRepeatCount(sFd, count);
        }
        Log.d(TAG, "setRxRepeatcount failed");
        return false;
    }

    public boolean getPSRxRepeatCount() {
        if (getFMState() != 0) {
            return this.mControl.getPSRxRepeatCount(sFd);
        }
        Log.d(TAG, "setRxRepeatcount failed");
        return false;
    }

    public byte getBlendSinr() {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.getBlendSinr(sFd);
        }
        Log.d(TAG, "getBlendSinr: Device currently busy in executing another command.");
        return Byte.MAX_VALUE;
    }

    public boolean setBlendSinr(int sinrHi) {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.setBlendSinr(sFd, sinrHi);
        }
        Log.d(TAG, "setBlendSinr: Device currently busy in executing another command.");
        return false;
    }

    public byte getBlendRmssi() {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.getBlendRmssi(sFd);
        }
        Log.d(TAG, "getBlendRmssi: Device currently busy in executing another command.");
        return Byte.MAX_VALUE;
    }

    public boolean setBlendRmssi(int rmssiHi) {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mControl.setBlendRmssi(sFd, rmssiHi);
        }
        Log.d(TAG, "setBlendRmssi: Device currently busy in executing another command.");
        return false;
    }

    public boolean setRdsGroupOptions(int enRdsGrpsMask, int rdsBuffSize, boolean enRdsChangeFilter) {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mRdsData.rdsOn(true) == 0 && this.mRdsData.rdsGrpOptions(enRdsGrpsMask, rdsBuffSize, enRdsChangeFilter) == 0;
        } else {
            Log.d(TAG, "setRdsGroupOptions: Device currently busy in executing another command.");
            return false;
        }
    }

    public boolean setRawRdsGrpMask() {
        return FmTransceiver.setRDSGrpMask(64);
    }

    public boolean registerRdsGroupProcessing(int fmGrpsToProc) {
        if (this.mRdsData == null) {
            return false;
        }
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mRdsData.rdsOn(true) == 0 && this.mRdsData.rdsOptions(fmGrpsToProc) == 0;
        } else {
            Log.d(TAG, "registerRdsGroupProcessing: Device currently busy in executing another command.");
            return false;
        }
    }

    public boolean enableAFjump(boolean enable) {
        int state = getFMState();
        if (state != 0 && state != 3) {
            return this.mRdsData.rdsOn(true) == 0 && this.mRdsData.enableAFjump(enable) == 0;
        } else {
            Log.d(TAG, "enableAFjump: Device currently busy in executing another command.");
            return false;
        }
    }

    public int[] getStationList() {
        int state = getFMState();
        if (state == 0 || state == 3) {
            Log.d(TAG, "getStationList: Device currently busy in executing another command.");
            return null;
        }
        int[] stnList = new int[100];
        return this.mControl.stationList(sFd);
    }

    public int getRssi() {
        return FmReceiverJNI.getRSSINative(sFd);
    }

    public int getIoverc() {
        return this.mControl.IovercControl(sFd);
    }

    public int getIntDet() {
        return this.mControl.IntDet(sFd);
    }

    public int getMpxDcc() {
        return this.mControl.Mpx_Dcc(sFd);
    }

    public void setHiLoInj(int inj) {
        int re = this.mControl.setHiLoInj(sFd, inj);
    }

    public int getRmssiDelta() {
        int re = this.mControl.getRmssiDelta(sFd);
        Log.d(TAG, "The value of RMSSI Delta is " + re);
        return re;
    }

    public void setRmssiDel(int delta) {
        int re = this.mControl.setRmssiDel(sFd, delta);
    }

    public byte[] getRawRDS(int numBlocks) {
        byte[] rawRds = new byte[(numBlocks * 3)];
        int re = FmReceiverJNI.getRawRdsNative(sFd, rawRds, numBlocks * 3);
        if (re == numBlocks * 3) {
            return rawRds;
        }
        if (re <= 0) {
            return null;
        }
        byte[] buff = new byte[re];
        System.arraycopy(rawRds, 0, buff, 0, re);
        return buff;
    }

    public int getFMState() {
        return FmTransceiver.getFMPowerState();
    }

    public boolean setOnChannelThreshold(int data) {
        if (this.mControl.setOnChannelThreshold(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public boolean getOnChannelThreshold() {
        if (this.mControl.getOnChannelThreshold(sFd) != 0) {
            return false;
        }
        return true;
    }

    public boolean setOffChannelThreshold(int data) {
        if (this.mControl.setOffChannelThreshold(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public boolean getOffChannelThreshold() {
        if (this.mControl.getOffChannelThreshold(sFd) != 0) {
            return false;
        }
        return true;
    }

    public int getSINR() {
        int re = this.mControl.getSINR(sFd);
        Log.d(TAG, "The value of SINR is " + re);
        return re;
    }

    public boolean setSINRThreshold(int data) {
        if (this.mControl.setSINRThreshold(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public int getSINRThreshold() {
        return this.mControl.getSINRThreshold(sFd);
    }

    public boolean setRssiThreshold(int data) {
        if (this.mControl.setRssiThreshold(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public int getRssiThreshold() {
        return this.mControl.getRssiThreshold(sFd);
    }

    public boolean setAfJumpRssiThreshold(int data) {
        if (this.mControl.setAfJumpRssiThreshold(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public int getAfJumpRssiThreshold() {
        return this.mControl.getAfJumpRssiThreshold(sFd);
    }

    public boolean setRdsFifoCnt(int data) {
        if (this.mControl.setRdsFifoCnt(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public int getRdsFifoCnt() {
        return this.mControl.getRdsFifoCnt(sFd);
    }

    public boolean setSINRsamples(int data) {
        if (this.mControl.setSINRsamples(sFd, data) < 0) {
            return false;
        }
        return true;
    }

    public int getSINRsamples() {
        return this.mControl.getSINRsamples(sFd);
    }

    public int updateSpurFreq(int freq, int rmssi, boolean enable) {
        return this.mControl.updateSpurTable(sFd, freq, rmssi, enable);
    }

    public int configureSpurTable() {
        return this.mControl.configureSpurTable(sFd);
    }

    public static int getSpurConfiguration(int freq) {
        int retval = FmReceiverJNI.setControlNative(sFd, V4L2_CID_PRIVATE_IRIS_GET_SPUR_TBL, freq);
        if (retval != 0) {
            Log.d(TAG, "Failed/No Spurs for " + freq);
        }
        return retval;
    }

    public static void getSpurTableData() {
        byte[] buff = new byte[STD_BUF_SIZE];
        FmReceiverJNI.getBufferNative(sFd, buff, 13);
        Log.d(TAG, "freq = " + (((buff[0] & 255) | ((buff[1] & 255) << 8)) | ((buff[2] & 255) << 16)));
        Log.d(TAG, "no_of_spurs = " + buff[3]);
        for (int i = 0; i < 3; i++) {
            Log.d(TAG, "rotation_value = " + (((buff[(i * 4) + 4] & 255) | ((buff[(i * 4) + 5] & 255) << 8)) | ((buff[(i * 4) + 6] & 15) << 12)));
            Log.d(TAG, "lsbOfLen = " + ((byte) (((buff[(i * 4) + 6] & 240) >> 4) & 1)));
            Log.d(TAG, "filterCoe = " + ((byte) (((buff[(i * 4) + 6] & 240) >> 5) & 3)));
            byte isEnbale = (byte) (((buff[(i * 4) + 6] & 240) >> 7) & 1);
            Log.d(TAG, "spur level: " + buff[(i * 4) + 7]);
        }
    }

    public void FMcontrolLowPassFilter(int state, int net_type, int enable) {
        int RatConf = SystemProperties.getInt("persist.fm_wan.ratconf", 0);
        Log.v(TAG, "FMcontrolLowPassFilter " + RatConf);
        switch (net_type) {
            case 1:
                if ((mEnableLpfGprs & RatConf) == mEnableLpfGprs) {
                    Log.v(TAG, "set LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 2:
                if ((mEnableLpfEdge & RatConf) == mEnableLpfEdge) {
                    Log.v(TAG, "set LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 3:
                if ((mEnableLpfUmts & RatConf) == mEnableLpfUmts) {
                    Log.v(TAG, "set LPF for net_type: " + Integer.toString(net_type));
                    Log.v(TAG, "enable:" + enable);
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 4:
                if ((mEnableLpfCdma & RatConf) == mEnableLpfCdma) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 5:
                if ((mEnableLpfEvdo0 & RatConf) == mEnableLpfEvdo0) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 6:
                if ((mEnableLpfEvdoA & RatConf) == mEnableLpfEvdoA) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 7:
                if ((mEnableLpf1xRtt & RatConf) == mEnableLpf1xRtt) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 8:
                if ((mEnableLpfHsdpa & RatConf) == mEnableLpfHsdpa) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case FM_RX_SRCHLIST_MODE_WEAKEST /*9*/:
                if ((mEnableLpfHsupa & RatConf) == mEnableLpfHsupa) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 10:
                if ((mEnableLpfHspa & RatConf) == mEnableLpfHspa) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case BUF_RTPLUS /*11*/:
                if ((mEnableLpfIden & RatConf) == mEnableLpfIden) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 12:
                if ((mEnableLpfEvdoB & RatConf) == mEnableLpfEvdoB) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 13:
                if ((mEnableLpfLte & RatConf) == mEnableLpfLte) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 14:
                if ((mEnableLpfEhrpd & RatConf) == mEnableLpfEhrpd) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 15:
                if ((mEnableLpfHspap & RatConf) == mEnableLpfHspap) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 16:
                if ((mEnableLpfGsm & RatConf) == mEnableLpfGsm) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 17:
                if ((mEnableLpfScdma & RatConf) == mEnableLpfScdma) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 18:
                if ((mEnableLpfIwlan & RatConf) == mEnableLpfIwlan) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            case 139:
                if ((mEnableLpfLteCa & RatConf) == mEnableLpfLteCa) {
                    Log.d(TAG, "enabling LPF for net_type: " + Integer.toString(net_type));
                    this.mControl.enableLPF(sFd, enable);
                    return;
                }
                return;
            default:
                Log.d(TAG, "net_type " + Integer.toString(net_type) + " doesn't need LPF enabling");
                return;
        }
    }

    public void EnableSlimbus(int enable) {
        Log.d(TAG, "EnableSlimbus :enable =" + enable);
        this.mControl.enableSlimbus(sFd, enable);
    }

    public void EnableSoftMute(int enable) {
        Log.d(TAG, "enableSoftMute :enable =" + enable);
        this.mControl.enableSoftMute(sFd, enable);
    }
}

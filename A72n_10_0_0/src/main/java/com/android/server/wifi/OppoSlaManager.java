package com.android.server.wifi;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.INetd;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.StructNlMsgHdr;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.system.NetlinkSocketAddress;
import android.system.Os;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.server.pm.PackageManagerService;
import com.android.server.wifi.OppoSlaManager;
import com.android.server.wifi.hotspot2.anqp.NAIRealmData;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.scanner.OppoWiFiScanBlockPolicy;
import java.io.FileDescriptor;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import libcore.io.IoUtils;
import oppo.util.OppoStatistics;

/* access modifiers changed from: package-private */
public class OppoSlaManager {
    public static final String ACTION_GAME_START = "coloros.intent.action.gamestart";
    public static final String ACTION_GAME_STOP = "coloros.intent.action.gamestop";
    public static final String ACTION_KING_CARD_RESULT = "oppo.intent.action.ACTION_KING_CARD_RESULT";
    public static final String ACTION_MCC_CHANGE = "android.telephony.action.mcc_change";
    public static final String ACTION_NETWORK_CONTROL = "oppo.intent.action.ACTION_NETWORK_CONTROL";
    public static final String APP_NORMAL_TRAFFIC_PREFIX = "sla_app_normal_";
    public static final String APP_TRAFFIC_PREFIX = "sla_app_";
    public static final String CELL_CONNECTION_CHANGE = "android.net.cell.CONNECTION_CHANGE";
    private static final int CHAR = 2;
    private static final long DISABLE_SLA_TIME_MS = 120000;
    private static final int DOUBLE = 8;
    private static final int DUAL_STA_ACTIVE_INIT = 0;
    private static final int DUAL_STA_LOW_DL_SPEED = 3;
    private static final int DUAL_STA_LOW_SPEED_HIGH_RTT = 1;
    private static final int DUAL_STA_LOW_WLAN_SCORE = 2;
    private static final int DUAL_STA_WLAN_DOWNLOAD = 4;
    private static final int FLOAT = 4;
    private static final int FOREGROUND_IMPORTANCE_CUTOFF = 100;
    public static final int GAME_APP_STRUCT_SIZE = 48;
    public static final String GAME_STATS_PREFIX = "sla_game_";
    private static final int INT = 4;
    private static final String INVALID_MCC = "000";
    private static final int INVALID_PARAM = -99;
    private static final long IO_TIMEOUT = 300;
    public static final String KEY_DUAL_STA_SWITCH = "dual_sta_switch_on";
    public static final String KEY_SLA_SWITCH = "wifi_sla_switch_on";
    public static final String KING_CARD_KEY = "IS_KING_CARD";
    public static final int KING_CARD_NO = 0;
    public static final int KING_CARD_UNKNOWN = 3;
    public static final int KING_CARD_YES = 1;
    private static final String LAST_CELL_TRAFFIC_TIME = "LAST_CELL_TRAFFIC_TIME";
    private static final String LAST_CELL_TRAFFIC_USAGE = "LAST_CELL_TRAFFIC_USAGE";
    private static final int LONG = 8;
    private static final int LTE_BAD_RSSI = -105;
    private static final int LTE_GOOD_RSSI = -95;
    private static final int MAIN_WIFI_GOOD_SPEED = 6144;
    private static final int MSG_CELL_CONNECTION_CHANGE = 17;
    private static final int MSG_CELL_QUALITY_CHANGED = 12;
    private static final int MSG_DISABLE_DUAL_STA = 23;
    private static final int MSG_DISABLE_SLA = 3;
    private static final int MSG_ENABLE_DUAL_STA = 22;
    private static final int MSG_ENABLE_SLA = 4;
    private static final int MSG_ENABLE_SLA_TIMEOUT = 20;
    private static final int MSG_GAME_STATE_CHANGED = 19;
    private static final int MSG_KING_CARD_RESULT = 10;
    private static final int MSG_NOTIFY_SHOW_DIALOG = 13;
    private static final int MSG_PACKAGE_CHANGED = 15;
    private static final int MSG_SCREEN_STATE_CHANGE = 11;
    private static final int MSG_SEND_GAME_APPS = 6;
    private static final int MSG_SEND_PID_AND_LISTEN = 7;
    private static final int MSG_SEND_SLA_DISABLED = 16;
    private static final int MSG_SEND_SLA_GAME_PARAMS = 21;
    private static final int MSG_SEND_SLA_PARAMS = 18;
    private static final int MSG_SEND_SMART_BW_PARAMS = 24;
    private static final int MSG_SEND_WHITE_LIST_APPS = 5;
    private static final int MSG_SHOW_DIALOG_NOW = 14;
    private static final int MSG_SHOW_TOAST = 1;
    private static final int MSG_STOP_LISTENING = 8;
    private static final int MSG_SWITCH_STATE_CHANGE = 9;
    private static final int MSG_TOP_UID_CHANGED = 2;
    private static final int OPPO_NETLINK_OPPO_SLA = 33;
    private static final short OPPO_SLA_CTRL_MAX = 58;
    private static final short OPPO_SLA_DISABLE = 20;
    private static final short OPPO_SLA_DISABLED = 30;
    private static final short OPPO_SLA_DISABLE_GAME_RTT = 32;
    private static final short OPPO_SLA_ENABLE = 19;
    private static final short OPPO_SLA_ENABLED = 29;
    private static final short OPPO_SLA_ENABLE_GAME_RTT = 31;
    private static final String OPPO_SLA_ENABLE_STATUS = "oppo_sla_enable_status";
    private static final short OPPO_SLA_GET_SPEED_UP_APP = 43;
    private static final short OPPO_SLA_GET_SYN_RETRAN_INFO = 42;
    private static final short OPPO_SLA_IF_CHANGED = 21;
    private static final String OPPO_SLA_LAST_MCC = "OPPO_SLA_LAST_MCC";
    private static final short OPPO_SLA_NOTIFY_CELL_QUALITY = 37;
    private static final short OPPO_SLA_NOTIFY_DEFAULT_NETWORK = 45;
    private static final short OPPO_SLA_NOTIFY_DOWNLOAD_APP = 55;
    private static final short OPPO_SLA_NOTIFY_DUAL_STA_APP = 52;
    private static final short OPPO_SLA_NOTIFY_GAME_APP_UID = 26;
    private static final short OPPO_SLA_NOTIFY_GAME_IN_FRONT = 50;
    private static final short OPPO_SLA_NOTIFY_GAME_RTT = 27;
    private static final short OPPO_SLA_NOTIFY_GAME_RTT_PARAMS = 48;
    private static final short OPPO_SLA_NOTIFY_GAME_RX_PKT = 49;
    private static final short OPPO_SLA_NOTIFY_GAME_STATE = 47;
    private static final short OPPO_SLA_NOTIFY_PARAMS = 46;
    private static final short OPPO_SLA_NOTIFY_PID = 18;
    private static final short OPPO_SLA_NOTIFY_PRIMARY_WIFI = 51;
    private static final short OPPO_SLA_NOTIFY_SCREEN_STATE = 36;
    private static final short OPPO_SLA_NOTIFY_SHOW_DIALOG = 39;
    private static final short OPPO_SLA_NOTIFY_SPEED_RTT = 34;
    private static final short OPPO_SLA_NOTIFY_SWITCH_STATE = 33;
    private static final short OPPO_SLA_NOTIFY_VEDIO_APP = 56;
    private static final short OPPO_SLA_NOTIFY_VPN_CONNECTED = 54;
    private static final short OPPO_SLA_NOTIFY_WHITE_LIST_APP = 28;
    private static final short OPPO_SLA_NOTIFY_WIFI_SCORE = 17;
    private static final short OPPO_SLA_SEND_APP_TRAFFIC = 40;
    private static final short OPPO_SLA_SEND_GAME_APP_STATISTIC = 41;
    private static final short OPPO_SLA_SET_DEBUG = 44;
    private static final short OPPO_SLA_SHOW_DIALOG_NOW = 38;
    private static final short OPPO_SLA_SWITCH_GAME_NETWORK = 35;
    private static final short OPPO_SLA_WEIGHT_BY_WLAN_ASSIST = 53;
    private static final short OPPO_SMART_BW_SET_PARAMS = 57;
    private static final String[] PROJECTION = {" pkg_name "};
    private static final int RECORD_SIZE = 60;
    public static final int RTT_SPEED_LEN = 6;
    private static final String SELECTION = "data_state != 1";
    private static final int SHORT = 2;
    private static final String SIMSETTING_LOG_SWITCH_DB = "opposimsettings.log.switch";
    private static final String SLA_APP_STATE_PREFIX = "SLA_STATE_";
    private static final String SLA_AUTO_ENABLED = "SLA_AUTO_ENABLED";
    private static final int SLA_GAME_CJZC = 2;
    private static final int SLA_GAME_WZRY = 1;
    private static final String SLA_MCC_CHINA = "460";
    private static final int SLA_MCC_LENGTH = 3;
    private static final int SLA_MODE_DUAL_WIFI = 1;
    private static final int SLA_MODE_INIT = 0;
    private static final int SLA_MODE_WIFI_CELL = 2;
    private static final int SLA_SOCKET_TCP = 1;
    private static final int SLA_SOCKET_TCP_UDP = 0;
    private static final int SLA_SOCKET_UDP = 2;
    private static final String TAG = "OppoSlaManager";
    private static final long TEN_GB_IN_BYTES = 10737418240L;
    private static final String[] TYPE_STRINGS = {"OPPO_SLA_NOTIFY_WIFI_SCORE", "OPPO_SLA_NOTIFY_PID", "OPPO_SLA_ENABLE", "OPPO_SLA_DISABLE", "OPPO_SLA_IF_CHANGED", "", "", "", "", "OPPO_SLA_NOTIFY_GAME_APP_UID", "OPPO_SLA_NOTIFY_GAME_RTT", "OPPO_SLA_NOTIFY_WHITE_LIST_APP", "OPPO_SLA_ENABLED", "OPPO_SLA_DISABLED", "OPPO_SLA_ENABLE_GAME_RTT", "OPPO_SLA_DISABLE_GAME_RTT", "OPPO_SLA_NOTIFY_SWITCH_STATE", "OPPO_SLA_NOTIFY_SPEED_RTT", "OPPO_SLA_SWITCH_GAME_NETWORK", "OPPO_SLA_NOTIFY_SCREEN_STATE", "OPPO_SLA_NOTIFY_CELL_QUALITY", "OPPO_SLA_SHOW_DIALOG_NOW", "OPPO_SLA_NOTIFY_SHOW_DIALOG", "OPPO_SLA_SEND_APP_TRAFFIC", "OPPO_SLA_SEND_GAME_APP_STATISTIC", "OPPO_SLA_GET_SYN_RETRAN_INFO", "OPPO_SLA_GET_SPEED_UP_APP", "OPPO_SLA_SET_DEBUG", "OPPO_SLA_NOTIFY_DEFAULT_NETWORK", "OPPO_SLA_NOTIFY_PARAMS", "OPPO_SLA_NOTIFY_GAME_STATE", "OPPO_SLA_NOTIFY_GAME_RTT_PARAMS", "OPPO_SLA_NOTIFY_GAME_RX_PKT", "OPPO_SLA_NOTIFY_GAME_IN_FRONT", "OPPO_SLA_NOTIFY_PRIMARY_WIFI", "OPPO_SLA_NOTIFY_DUAL_STA_APP", "OPPO_SLA_WEIGHT_BY_WLAN_ASSIST", "OPPO_SLA_NOTIFY_VPN_CONNECTED", "OPPO_SLA_NOTIFY_DOWNLOAD_APP", "OPPO_SLA_NOTIFY_VEDIO_APP", "OPPO_SMART_BW_SET_PARAMS"};
    private static final String URI = "content://com.coloros.datamonitorprovider.OppoDataMonitorProvider/tm_network_control";
    public static final int WHITE_APP_MAX = 64;
    public static final int WHITE_APP_STRUCT_SIZE = 1284;
    private static boolean mDebug = false;
    private static ArrayList<Integer> mDownloadApps = new ArrayList<>();
    private static ArrayList<Integer> mDualStaApps = new ArrayList<>();
    private static ArrayList<Integer> mGameAppUids = new ArrayList<>();
    private static FileDescriptor mNlfd;
    private static ArrayList<Integer> mSkipDestroySocketApps = new ArrayList<>();
    private static ArrayList<Integer> mVideoApps = new ArrayList<>();
    private static ArrayList<Integer> mWhiteListAppUids = new ArrayList<>();
    private static OppoSlaManager sInstance;
    private static OppoSlaCallback sSlaCallback = null;
    private ActivityManager mActivityManager;
    private Handler mAsyncHandler;
    private BroadcastReceiver mBroadcastReceiver;
    private int mCellAvgRtt;
    private int mCellAvgSpeed;
    private int mCellCongestCount;
    private boolean mCellConnected;
    private boolean mCellEnabled;
    private boolean mCellQualityGood;
    private int mCellRtt0_100;
    private int mCellRtt100_200;
    private int mCellRtt200plus;
    private ArrayList<Integer> mCellRttRecord;
    private int mCellScore;
    private int mCellServiceState = -1;
    private ArrayList<Integer> mCellSpeedRecord;
    private int mCellSumRtt;
    private int mCellSumSpeed;
    private Context mContext;
    private boolean mDestoryScoket;
    private int mDualStaActiveType;
    DualStaBuriedPoint mDualStaBuriedPoint;
    private boolean mDualStaEnabled;
    private boolean mGameInFront;
    private boolean mGameNetworkSwitched;
    private Handler mHandler;
    private int mKernelWorkMode;
    private int mKingCardResult;
    private int mLastCellRtt;
    private int mLastWifiRtt;
    private int mLteRsrp;
    private String mMCC;
    private boolean mMainWifiGood;
    private Thread mNetlinkThread = null;
    private PackageManagerService mPMS;
    private HashMap<String, Integer> mPackagesAndUids;
    private PhoneStateListener mPhoneStateListener;
    private int mRecordIndex;
    private Object mRttSpeedLock;
    private boolean mScreenOn;
    private boolean mShowDialog;
    private boolean mSlaEnabled;
    private boolean mSlaEnabling;
    private ArrayList<SlaGameStats> mSlaGameStats;
    private final INetd mSlaNetd;
    private HashMap<String, Integer> mSlaPackagesAndUids;
    private boolean mSmartBWParamsSet;
    private int mSpeedRttIndex;
    private TelephonyManager mTelephonyManager;
    private ActivityManager.OnUidImportanceListener mUidImportanceListener;
    private int mUserId;
    private boolean mUsingLTE;
    private boolean mVpnConnected;
    private boolean mWifi2Connected;
    private int mWifiAvgRtt;
    private int mWifiAvgSpeed;
    private int mWifiCongestCount;
    private boolean mWifiConnected;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;
    private int mWifiRtt0_100;
    private int mWifiRtt100_200;
    private int mWifiRtt200plus;
    private ArrayList<Integer> mWifiRttRecord;
    private ArrayList<Integer> mWifiRttRecord2;
    private ArrayList<Integer> mWifiSpeedRecord;
    private ArrayList<Integer> mWifiSpeedRecord2;
    private int mWifiSumRtt;
    private int mWifiSumSpeed;
    private long mWlan0RxPkts;
    private long mWlan0TxPkts;
    private long mWlan1RxPkts;
    private long mWlan1TxPkts;

    private String getMessageName(short msg) {
        if (msg >= 17) {
            int i = msg - 17;
            String[] strArr = TYPE_STRINGS;
            if (i <= strArr.length) {
                return strArr[msg - 17];
            }
        }
        return "invalid message:" + ((int) msg);
    }

    private OppoSlaManager(Context context) {
        boolean z = true;
        this.mScreenOn = true;
        this.mSlaEnabling = false;
        this.mSlaEnabled = false;
        this.mCellEnabled = false;
        this.mCellConnected = false;
        this.mWifiConnected = false;
        this.mWifi2Connected = false;
        this.mVpnConnected = false;
        this.mDualStaEnabled = false;
        this.mSmartBWParamsSet = false;
        this.mWlan0RxPkts = 0;
        this.mWlan0TxPkts = 0;
        this.mWlan1RxPkts = 0;
        this.mWlan1TxPkts = 0;
        this.mDualStaActiveType = 0;
        this.mKernelWorkMode = 0;
        this.mDestoryScoket = true;
        this.mRecordIndex = -1;
        this.mRttSpeedLock = new Object();
        this.mWifiRttRecord = new ArrayList<>(60);
        this.mWifiSpeedRecord = new ArrayList<>(60);
        this.mWifiRttRecord2 = new ArrayList<>(60);
        this.mWifiSpeedRecord2 = new ArrayList<>(60);
        this.mCellRttRecord = new ArrayList<>(60);
        this.mCellSpeedRecord = new ArrayList<>(60);
        this.mSlaPackagesAndUids = new HashMap<>();
        this.mPackagesAndUids = new HashMap<>();
        this.mLteRsrp = 0;
        this.mCellQualityGood = true;
        this.mCellScore = 50;
        this.mMainWifiGood = false;
        this.mGameInFront = false;
        this.mGameNetworkSwitched = false;
        this.mUserId = 0;
        this.mMCC = "";
        this.mKingCardResult = 3;
        this.mSlaGameStats = new ArrayList<>();
        this.mDualStaBuriedPoint = new DualStaBuriedPoint();
        this.mWifiSumSpeed = 0;
        this.mWifiAvgSpeed = 0;
        this.mWifiSumRtt = 0;
        this.mWifiAvgRtt = 0;
        this.mCellSumSpeed = 0;
        this.mCellAvgSpeed = 0;
        this.mCellSumRtt = 0;
        this.mCellAvgRtt = 0;
        this.mSpeedRttIndex = 0;
        this.mLastWifiRtt = 0;
        this.mLastCellRtt = 0;
        this.mWifiRtt0_100 = 0;
        this.mWifiRtt100_200 = 0;
        this.mWifiRtt200plus = 0;
        this.mCellRtt0_100 = 0;
        this.mCellRtt100_200 = 0;
        this.mCellRtt200plus = 0;
        this.mWifiCongestCount = 0;
        this.mCellCongestCount = 0;
        this.mContext = context;
        this.mPMS = ServiceManager.getService("package");
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mUsingLTE = this.mTelephonyManager.getNetworkType() == 13;
        this.mSlaNetd = INetd.Stub.asInterface(ServiceManager.getService("netd"));
        this.mHandler = new Handler() {
            /* class com.android.server.wifi.OppoSlaManager.AnonymousClass1 */

            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    if (OppoSlaManager.mDebug) {
                        Log.d(OppoSlaManager.TAG, "MSG_SHOW_TOAST:" + msg.obj);
                    }
                    OppoSlaManager.this.showMyToast((String) msg.obj, 500);
                } else if (i != 14) {
                    Log.d(OppoSlaManager.TAG, "Unknow message:" + msg.what);
                } else {
                    boolean slaSupported = OppoSlaManager.this.isSlaSupported();
                    if (OppoSlaManager.mDebug) {
                        Log.d(OppoSlaManager.TAG, "MSG_SHOW_DIALOG_NOW slaSupported=" + slaSupported);
                    }
                    if (slaSupported && OppoSlaManager.sSlaCallback != null) {
                        OppoSlaManager.sSlaCallback.showSlaDialog();
                    }
                }
            }
        };
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mAsyncHandler = new AsyncHandler(handlerThread.getLooper());
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mUidImportanceListener = new ActivityManager.OnUidImportanceListener() {
            /* class com.android.server.wifi.OppoSlaManager.AnonymousClass2 */

            public void onUidImportance(int uid, int importance) {
                OppoSlaManager.this.mAsyncHandler.sendMessage(OppoSlaManager.this.mAsyncHandler.obtainMessage(2, uid, importance));
            }
        };
        startCheckingTopApp();
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        this.mPhoneStateListener = new PhoneStateListener() {
            /* class com.android.server.wifi.OppoSlaManager.AnonymousClass3 */

            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                if (OppoSlaManager.this.mUsingLTE) {
                    int oldRsrp = OppoSlaManager.this.mLteRsrp;
                    int newRsrp = signalStrength.getLteRsrp();
                    if ((OppoSlaManager.this.mLteRsrp > OppoSlaManager.LTE_GOOD_RSSI && newRsrp < OppoSlaManager.LTE_BAD_RSSI) || (OppoSlaManager.this.mLteRsrp < OppoSlaManager.LTE_BAD_RSSI && newRsrp > OppoSlaManager.LTE_GOOD_RSSI)) {
                        OppoSlaManager.this.mLteRsrp = newRsrp;
                        OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(12);
                    }
                    if (OppoSlaManager.mDebug) {
                        Log.d("OppoSlaManager_RSRP", "onSignalStrengthsChanged oldRsrp=" + oldRsrp + " newRsrp=" + newRsrp);
                    }
                }
            }

            public void onServiceStateChanged(ServiceState state) {
                super.onServiceStateChanged(state);
                boolean usingLTE = OppoSlaManager.this.mTelephonyManager.getNetworkType() == 13;
                if (OppoSlaManager.this.mUsingLTE != usingLTE) {
                    OppoSlaManager.this.mUsingLTE = usingLTE;
                    OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(12);
                }
                if (OppoSlaManager.this.mCellServiceState != state.getState()) {
                    OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(9);
                    OppoSlaManager.this.mAsyncHandler.post(new Runnable() {
                        /* class com.android.server.wifi.$$Lambda$OppoSlaManager$3$84UUq2OgH_9siGFolf7oX6JKA */

                        public final void run() {
                            OppoSlaManager.AnonymousClass3.this.lambda$onServiceStateChanged$0$OppoSlaManager$3();
                        }
                    });
                    OppoSlaManager.this.mCellServiceState = state.getState();
                }
                if (OppoSlaManager.mDebug) {
                    Log.d(OppoSlaManager.TAG, "onServiceStateChanged mUsingLTE=" + OppoSlaManager.this.mUsingLTE + " mCellServiceState=" + OppoSlaManager.this.mCellServiceState);
                }
            }

            public /* synthetic */ void lambda$onServiceStateChanged$0$OppoSlaManager$3() {
                OppoSlaManager.this.updateMCC();
            }
        };
        this.mTelephonyManager.listen(this.mPhoneStateListener, OppoDataStallHelper.DATA_STALL_QCOM_HOST_SOFTAP_TX_TIMEOUT);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(KEY_SLA_SWITCH), true, new SlaSwitchObserver(this.mAsyncHandler));
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("dual_sta_switch_on"), true, new DualStaSwitchObserver(this.mAsyncHandler));
        this.mShowDialog = Settings.System.getInt(this.mContext.getContentResolver(), "SLA_DIALOG_COUNT", 0) >= 3 ? false : z;
        this.mAsyncHandler.sendEmptyMessage(7);
        initBroadcastRecriver();
        updateMCC();
        updateSlaPackagesAndUids();
        registerKernelLogging();
    }

    public static void setSlaCallback(OppoSlaCallback callback) {
        if (callback == null) {
            Log.e(TAG, "setSlaCallback() invalid arguments!!");
        } else if (sSlaCallback != null) {
            Log.w(TAG, "setSlaCallback() called multiple times!!");
        } else {
            sSlaCallback = callback;
        }
    }

    public static OppoSlaManager getInstance(Context context) {
        synchronized (OppoSlaManager.class) {
            if (sInstance == null) {
                sInstance = new OppoSlaManager(context);
            }
        }
        return sInstance;
    }

    private class AsyncHandler extends Handler {
        public AsyncHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean start = false;
            switch (msg.what) {
                case 2:
                    OppoSlaManager.this.handleTopUidChanged(msg.arg1, msg.arg2);
                    return;
                case 3:
                    Log.d(OppoSlaManager.TAG, "MSG_DISABLE_SLA mSlaEnabled=" + OppoSlaManager.this.mSlaEnabled);
                    if (OppoSlaManager.this.mSlaEnabled) {
                        Log.d(OppoSlaManager.TAG, "disableSla..");
                        if (OppoSlaManager.sSlaCallback != null) {
                            OppoSlaManager.sSlaCallback.disableSla();
                        }
                        OppoSlaManager.this.setOppoSlaDisable(2);
                        OppoSlaManager.this.mHandler.sendMessage(OppoSlaManager.this.mHandler.obtainMessage(1, "disableSla..."));
                        return;
                    }
                    return;
                case 4:
                    Log.d(OppoSlaManager.TAG, "MSG_ENABLE_SLA");
                    OppoSlaManager.this.maybeEnableSla();
                    return;
                case 5:
                    Log.d(OppoSlaManager.TAG, "MSG_SEND_WHITE_LIST_APPS");
                    OppoSlaManager.this.mAsyncHandler.removeMessages(5);
                    OppoSlaManager.this.sendWhiteListAppUid();
                    OppoSlaManager.this.sendDualStaAppUid();
                    OppoSlaManager.this.sendVideoAppUid();
                    OppoSlaManager.this.sendDownloadAppUid();
                    return;
                case 6:
                    Log.d(OppoSlaManager.TAG, "MSG_SEND_GAME_APPS");
                    OppoSlaManager.this.sendGameAppUid();
                    return;
                case 7:
                    Log.d(OppoSlaManager.TAG, "MSG_SEND_PID_AND_LISTEN");
                    OppoSlaManager.this.sendPidAndListen();
                    return;
                case 8:
                    return;
                case 9:
                    Log.d(OppoSlaManager.TAG, "MSG_SWITCH_STATE_CHANGE");
                    OppoSlaManager.this.sendSwitchStateToKernel();
                    return;
                case 10:
                    Log.d(OppoSlaManager.TAG, "MSG_KING_CARD_RESULT");
                    OppoSlaManager.this.setKingCardResult(msg.arg1);
                    return;
                case 11:
                    Log.d(OppoSlaManager.TAG, "MSG_SCREEN_STATE_CHANGE");
                    OppoSlaManager oppoSlaManager = OppoSlaManager.this;
                    oppoSlaManager.sendScreenStateToKernel(oppoSlaManager.mScreenOn);
                    return;
                case 12:
                    Log.d(OppoSlaManager.TAG, "MSG_CELL_QUALITY_CHANGED");
                    OppoSlaManager.this.handleCellQualityChange();
                    return;
                case 13:
                    Log.d(OppoSlaManager.TAG, "MSG_NOTIFY_SHOW_DIALOG");
                    OppoSlaManager.this.handleNotifyShowDialog();
                    return;
                case 14:
                default:
                    Log.d(OppoSlaManager.TAG, "Unknow message:" + msg.what);
                    return;
                case 15:
                    Log.d(OppoSlaManager.TAG, "MSG_PACKAGE_CHANGED");
                    OppoSlaManager.this.updateSlaPackagesAndUids();
                    OppoSlaManager.this.sendWhiteListAppUid();
                    OppoSlaManager.this.sendDualStaAppUid();
                    OppoSlaManager.this.sendGameAppUid();
                    return;
                case 16:
                    Log.d(OppoSlaManager.TAG, "MSG_SEND_SLA_DISABLED mSlaEnabled=" + OppoSlaManager.this.mSlaEnabled);
                    if (msg.arg1 == 1 || msg.arg1 == 0) {
                        OppoSlaManager.this.setOppoSlaDisable(1);
                        OppoSlaManager.this.sendToKernel((OppoSlaManager) 20, (short) 1);
                        OppoSlaManager.this.mDualStaEnabled = false;
                    }
                    if (msg.arg1 == 2 || msg.arg1 == 0) {
                        OppoSlaManager.this.setOppoSlaDisable(2);
                        OppoSlaManager.this.sendToKernel((OppoSlaManager) 20, (short) 2);
                        OppoSlaManager.this.mSlaEnabled = false;
                        return;
                    }
                    return;
                case 17:
                    Log.d(OppoSlaManager.TAG, "MSG_CELL_CONNECTION_CHANGE mCellConnected=" + OppoSlaManager.this.mCellConnected);
                    if (OppoSlaManager.this.mCellConnected) {
                        OppoSlaManager oppoSlaManager2 = OppoSlaManager.this;
                        if (oppoSlaManager2.mTelephonyManager.getNetworkType() == 13) {
                            start = true;
                        }
                        oppoSlaManager2.mUsingLTE = start;
                        OppoSlaManager.this.mTelephonyManager.listen(OppoSlaManager.this.mPhoneStateListener, OppoDataStallHelper.DATA_STALL_QCOM_HOST_SOFTAP_TX_TIMEOUT);
                        OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(12);
                        if (OppoSlaManager.this.mSlaEnabling) {
                            OppoSlaManager.this.setOppoSlaEnable(2);
                            return;
                        }
                        return;
                    }
                    OppoSlaManager.this.setOppoSlaDisable(2);
                    return;
                case 18:
                    Log.d(OppoSlaManager.TAG, "MSG_SEND_SLA_PARAMS");
                    OppoSlaManager.this.sendSlaParams();
                    return;
                case 19:
                    String pkgName = (String) msg.obj;
                    if (msg.arg1 == 1) {
                        start = true;
                    }
                    Log.d(OppoSlaManager.TAG, "MSG_GAME_STATE_CHANGED pkgName=" + pkgName + " start=" + start);
                    OppoSlaManager.this.handleGameStateChange(pkgName, start);
                    return;
                case 20:
                    Log.e(OppoSlaManager.TAG, "MSG_ENABLE_SLA_TIMEOUT");
                    OppoSlaManager.this.mSlaEnabling = false;
                    return;
                case 21:
                    Log.d(OppoSlaManager.TAG, "MSG_SEND_SLA_GAME_PARAMS");
                    OppoSlaManager.this.sendSlaGameParams();
                    return;
                case 22:
                    Log.e(OppoSlaManager.TAG, "MSG_ENABLE_DUAL_STA");
                    OppoSlaManager.this.maybeEnableDualSta();
                    return;
                case 23:
                    Log.d(OppoSlaManager.TAG, "MSG_DISABLE_DUAL_STA");
                    OppoWifiAssistantUtils.getInstance(OppoSlaManager.this.mContext).disableDualSta();
                    OppoSlaManager.this.setOppoSlaDisable(1);
                    OppoSlaManager.this.mHandler.sendMessage(OppoSlaManager.this.mHandler.obtainMessage(1, "disableDualSta..."));
                    return;
                case 24:
                    Log.d(OppoSlaManager.TAG, "MSG_SEND_SMART_BW_PARAMS");
                    if (!OppoSlaManager.this.mSmartBWParamsSet) {
                        OppoSlaManager oppoSlaManager3 = OppoSlaManager.this;
                        oppoSlaManager3.mSmartBWParamsSet = oppoSlaManager3.sendToKernel((OppoSlaManager) OppoSlaManager.OPPO_SMART_BW_SET_PARAMS, (short) oppoSlaManager3.mWifiRomUpdateHelper.getSmartBWParams());
                        Log.d(OppoSlaManager.TAG, "After sendSmartBWParams:" + Arrays.toString(OppoSlaManager.this.mWifiRomUpdateHelper.getSmartBWParams()) + ", mSmartBWParamsSet=" + OppoSlaManager.this.mSmartBWParamsSet);
                        return;
                    }
                    return;
            }
        }
    }

    public static class OppoSlaCallback {
        public void enableSla() {
        }

        public void disableSla() {
        }

        public void showSlaDialog() {
        }

        public boolean getWifiInterResult() {
            return false;
        }
    }

    private void initBroadcastRecriver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NETWORK_CONTROL);
        filter.addAction(ACTION_KING_CARD_RESULT);
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction(CELL_CONNECTION_CHANGE);
        filter.addAction(ACTION_MCC_CHANGE);
        filter.addAction(ACTION_GAME_START);
        filter.addAction(ACTION_GAME_STOP);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("oppo.intent.action.WIFI_ROM_UPDATE_CHANGED");
        filter.addAction("android.intent.action.USER_SWITCHED");
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoSlaManager.AnonymousClass4 */

            public void onReceive(Context context, Intent intent) {
                boolean connected;
                String action = intent.getAction();
                Log.d(OppoSlaManager.TAG, "Received broadcast:" + action);
                if (action.equals(OppoSlaManager.ACTION_NETWORK_CONTROL)) {
                    String[] slaApps = OppoSlaManager.this.getSlaWhiteListApps();
                    if (slaApps == null) {
                        Log.w(OppoSlaManager.TAG, "ACTION_NETWORK_CONTROL slaApps == null");
                        return;
                    }
                    ArrayList<String> slaAppsArray = new ArrayList<>(Arrays.asList(slaApps));
                    String pkgName = intent.getStringExtra("pkg_name");
                    int enabled = intent.getIntExtra("data_state", 0);
                    if (!TextUtils.isEmpty(pkgName) && slaAppsArray.contains(pkgName)) {
                        Log.d(OppoSlaManager.TAG, "[" + pkgName + "] cell enable:" + enabled);
                        OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(5);
                    }
                } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                    OppoSlaManager.this.mAsyncHandler.sendEmptyMessageDelayed(5, RttServiceImpl.HAL_RANGING_TIMEOUT_MS);
                    OppoSlaManager.this.mAsyncHandler.post(new Runnable() {
                        /* class com.android.server.wifi.$$Lambda$OppoSlaManager$4$uDT8n5b_ad0SSHY0TaP_8_N464w */

                        public final void run() {
                            OppoSlaManager.AnonymousClass4.this.lambda$onReceive$0$OppoSlaManager$4();
                        }
                    });
                } else if (action.equals(OppoSlaManager.ACTION_MCC_CHANGE)) {
                    OppoSlaManager.this.mAsyncHandler.post(new Runnable() {
                        /* class com.android.server.wifi.$$Lambda$OppoSlaManager$4$T0TPkZpm0TwpSoD5M0p633FxUig */

                        public final void run() {
                            OppoSlaManager.AnonymousClass4.this.lambda$onReceive$1$OppoSlaManager$4();
                        }
                    });
                } else if (action.equals(OppoSlaManager.ACTION_KING_CARD_RESULT)) {
                    OppoSlaManager.this.mAsyncHandler.sendMessage(OppoSlaManager.this.mAsyncHandler.obtainMessage(10, intent.getIntExtra(OppoSlaManager.KING_CARD_KEY, 3), 0));
                } else if (action.equals("android.intent.action.SCREEN_ON")) {
                    OppoSlaManager.this.mScreenOn = true;
                    OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(11);
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    OppoSlaManager.this.mScreenOn = false;
                    OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(11);
                } else if (action.equals("android.intent.action.PACKAGE_ADDED") || action.equals("android.intent.action.PACKAGE_REPLACED") || action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    String pkgName2 = intent.getData().getSchemeSpecificPart();
                    synchronized (OppoSlaManager.this.mPackagesAndUids) {
                        if (!TextUtils.isEmpty(pkgName2) && OppoSlaManager.this.mPackagesAndUids.get(pkgName2) != null) {
                            Log.d(OppoSlaManager.TAG, "SLA or Dual STA app changed, pkg=" + pkgName2);
                            OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(15);
                        }
                    }
                } else if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                    String simState = intent.getStringExtra("ss");
                    Log.d(OppoSlaManager.TAG, "ACTION_SIM_STATE_CHANGED simState=" + simState);
                    if ("LOADED".equals(simState)) {
                        Log.d(OppoSlaManager.TAG, "ACTION_SIM_STATE_CHANGED slot=" + intent.getIntExtra("slot", -1));
                        OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(9);
                    }
                } else if (action.equals(OppoSlaManager.CELL_CONNECTION_CHANGE)) {
                    boolean cellConnected = intent.getBooleanExtra("cell_connected", false);
                    Log.d(OppoSlaManager.TAG, action + " mCellConnected:" + OppoSlaManager.this.mCellConnected + " -> " + cellConnected);
                    if (OppoSlaManager.this.mCellConnected != cellConnected) {
                        OppoSlaManager.this.mCellConnected = cellConnected;
                        OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(17);
                    }
                    OppoSlaManager.this.mCellScore = 50;
                    OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(12);
                } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && info.getType() == 17 && OppoSlaManager.this.mVpnConnected != (connected = info.isConnected())) {
                        OppoSlaManager.this.mVpnConnected = connected;
                        OppoSlaManager.this.mAsyncHandler.post(new Runnable() {
                            /* class com.android.server.wifi.$$Lambda$OppoSlaManager$4$5G6oFCH5Q_Wfl1YU4iEXAHIazh0 */

                            public final void run() {
                                OppoSlaManager.AnonymousClass4.this.lambda$onReceive$2$OppoSlaManager$4();
                            }
                        });
                    }
                } else if (action.equals(OppoSlaManager.ACTION_GAME_START)) {
                    String pkgName3 = intent.getStringExtra("packageName");
                    Log.d(OppoSlaManager.TAG, action + " pkgName=" + pkgName3);
                    OppoSlaManager.this.mAsyncHandler.sendMessage(OppoSlaManager.this.mAsyncHandler.obtainMessage(19, 1, 0, pkgName3));
                } else if (action.equals(OppoSlaManager.ACTION_GAME_STOP)) {
                    String pkgName4 = intent.getStringExtra("packageName");
                    Log.d(OppoSlaManager.TAG, action + " pkgName=" + pkgName4);
                    OppoSlaManager.this.mAsyncHandler.sendMessage(OppoSlaManager.this.mAsyncHandler.obtainMessage(19, 0, 0, pkgName4));
                } else if (action.equals("oppo.intent.action.WIFI_ROM_UPDATE_CHANGED")) {
                    if (OppoSlaManager.this.mWifiRomUpdateHelper.getBooleanValue("OPPO_SLA_SET_DEBUG", false)) {
                        OppoSlaManager.this.mAsyncHandler.post(new Runnable() {
                            /* class com.android.server.wifi.$$Lambda$OppoSlaManager$4$zh2sf9wSFDLa_tijLHhe1x7KfvM */

                            public final void run() {
                                OppoSlaManager.AnonymousClass4.this.lambda$onReceive$3$OppoSlaManager$4();
                            }
                        });
                    }
                } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                    OppoSlaManager.this.mUserId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    Log.d(OppoSlaManager.TAG, "userHandleId=" + OppoSlaManager.this.mUserId);
                    OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(15);
                }
            }

            public /* synthetic */ void lambda$onReceive$0$OppoSlaManager$4() {
                OppoSlaManager.this.updateMCC();
            }

            public /* synthetic */ void lambda$onReceive$1$OppoSlaManager$4() {
                OppoSlaManager.this.updateMCC();
            }

            public /* synthetic */ void lambda$onReceive$2$OppoSlaManager$4() {
                Log.w(OppoSlaManager.TAG, "Notify VPN connection changed, mVpnConnected=" + OppoSlaManager.this.mVpnConnected);
                OppoSlaManager oppoSlaManager = OppoSlaManager.this;
                oppoSlaManager.sendToKernel((OppoSlaManager) OppoSlaManager.OPPO_SLA_NOTIFY_VPN_CONNECTED, (short) (oppoSlaManager.mVpnConnected ? 1 : 0));
            }

            public /* synthetic */ void lambda$onReceive$3$OppoSlaManager$4() {
                OppoSlaManager.this.sendToKernel((OppoSlaManager) OppoSlaManager.OPPO_SLA_SET_DEBUG, (short) 1);
            }
        };
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, packageFilter, null, null);
    }

    private void notifySlaStatusToLimitSpeedModule(int flag) {
        Log.d(TAG, "notifySlaStatusToLimitSpeedModule flag =" + flag);
        try {
            Settings.Global.putInt(this.mContext.getContentResolver(), OPPO_SLA_ENABLE_STATUS, flag);
        } catch (Exception e) {
            Log.e(TAG, "[setLimitSpeedStatus] > " + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setOppoSlaEnable(int mode) {
        boolean result = false;
        if (2 == mode) {
            this.mSlaEnabling = false;
            this.mAsyncHandler.removeMessages(20);
            if ((this.mWifiConnected || this.mWifi2Connected) && this.mCellConnected && !this.mSlaEnabled) {
                result = sendToKernel((short) 19, 2);
            }
        } else if (1 == mode && this.mWifiConnected && this.mWifi2Connected && !this.mDualStaEnabled) {
            notifySlaStatusToLimitSpeedModule(1);
            result = sendToKernel((short) 19, 1);
        }
        Log.d(TAG, "setOppoSlaEnable mode=" + mode + ", result=" + result + " mWifiConnected=" + this.mWifiConnected + " mWifi2Connected=" + this.mWifi2Connected + " mCellConnected=" + this.mCellConnected + " mSlaEnabled=" + this.mSlaEnabled + " mDualStaEnabled=" + this.mDualStaEnabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setOppoSlaDisable(int mode) {
        boolean result = false;
        if (2 == mode) {
            if (this.mSlaEnabled) {
                result = sendToKernel((short) 20, 2);
            }
            this.mAsyncHandler.removeMessages(20);
            this.mSlaEnabling = false;
            this.mGameNetworkSwitched = false;
        } else if (1 == mode && this.mDualStaEnabled) {
            notifySlaStatusToLimitSpeedModule(0);
            this.mDualStaBuriedPoint.setTotalDisableCount();
            result = sendToKernel((short) 20, 1);
        }
        this.mDestoryScoket = true;
        Log.d(TAG, "setOppoSlaDisable mode=" + mode + " result=" + result + " mSlaEnabled=" + this.mSlaEnabled + " mDualStaEnabled=" + this.mDualStaEnabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getSlaEnableState() {
        return (Settings.Global.getInt(this.mContext.getContentResolver(), KEY_SLA_SWITCH, 0) == 1) && this.mWifiRomUpdateHelper.getBooleanValue("NETWORK_SLA_ENABLED", true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMCC() {
        String mcc = null;
        String netOper = this.mTelephonyManager.getNetworkOperator();
        if (TextUtils.isEmpty(netOper)) {
            List<CellInfo> cellInfoList = this.mTelephonyManager.getAllCellInfo();
            if (cellInfoList != null) {
                Iterator<CellInfo> it = cellInfoList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    CellInfo ci = it.next();
                    if (ci instanceof CellInfoLte) {
                        mcc = ((CellInfoLte) ci).getCellIdentity().getMccString();
                    } else if (ci instanceof CellInfoWcdma) {
                        mcc = ((CellInfoWcdma) ci).getCellIdentity().getMccString();
                    } else if (ci instanceof CellInfoGsm) {
                        mcc = ((CellInfoGsm) ci).getCellIdentity().getMccString();
                    } else {
                        boolean z = ci instanceof CellInfoCdma;
                    }
                    if (mcc != null && mcc.length() == 3 && !INVALID_MCC.equals(mcc)) {
                        Log.d(TAG, "updateMCC from getAllCellInfo, curMCC=" + mcc);
                        break;
                    }
                }
            }
            if (TextUtils.isEmpty(mcc)) {
                mcc = Settings.System.getString(this.mContext.getContentResolver(), OPPO_SLA_LAST_MCC);
                Log.d(TAG, "updateMCC from Settings, curMCC=" + mcc);
            }
            Log.d(TAG, "updateMCC failed to getNetworkOperator, curMCC=" + mcc);
        } else if (netOper.length() >= 3) {
            mcc = netOper.substring(0, 3);
        }
        if (TextUtils.isEmpty(mcc)) {
            Log.w(TAG, "updateMCC curMCC empty!");
            return;
        }
        String oldMcc = this.mMCC;
        if (!INVALID_MCC.equals(mcc)) {
            Settings.System.putString(this.mContext.getContentResolver(), OPPO_SLA_LAST_MCC, mcc);
            this.mMCC = mcc;
        } else {
            this.mMCC = Settings.System.getString(this.mContext.getContentResolver(), OPPO_SLA_LAST_MCC);
        }
        if (oldMcc != null && !oldMcc.equals(this.mMCC)) {
            updateSlaPackagesAndUids();
        }
        Log.d(TAG, "updateMCC end, mMCC=" + this.mMCC);
    }

    public String[] getSlaWhiteListApps() {
        if (!isSlaSupported()) {
            if (!mDebug) {
                return null;
            }
            Log.d(TAG, "getSlaWhiteListApps isSlaSupported() == false.");
            return null;
        } else if (SLA_MCC_CHINA.equals(this.mMCC)) {
            String[] whiteListApps = this.mWifiRomUpdateHelper.getSlaWhiteListApps();
            if (mDebug) {
                Log.d(TAG, "curMCC=" + this.mMCC + " whiteList:" + Arrays.toString(whiteListApps));
            }
            return whiteListApps;
        } else {
            String[] whiteListApps2 = this.mWifiRomUpdateHelper.getSlaWhiteListAppsExp();
            if (mDebug) {
                Log.d(TAG, "curMCC=" + this.mMCC + " whiteListExp:" + Arrays.toString(whiteListApps2));
            }
            return whiteListApps2;
        }
    }

    public String[] getSlaGameApps() {
        if (!isSlaSupported()) {
            if (!mDebug) {
                return null;
            }
            Log.d(TAG, "getSlaGameApps isSlaSupported() == false.");
            return null;
        } else if (SLA_MCC_CHINA.equals(this.mMCC)) {
            String[] gameApps = this.mWifiRomUpdateHelper.getSlaGameApps();
            if (mDebug) {
                Log.d(TAG, "getSlaGameApps curMCC=" + this.mMCC + " gameApps:" + Arrays.toString(gameApps));
            }
            return gameApps;
        } else {
            String[] gameApps2 = this.mWifiRomUpdateHelper.getSlaGameAppsExp();
            if (mDebug) {
                Log.d(TAG, "getSlaGameApps curMCC=" + this.mMCC + " gameAppsExp:" + Arrays.toString(gameApps2));
            }
            return gameApps2;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSlaPackagesAndUids() {
        String[] whiteListApps = getSlaWhiteListApps();
        String[] gameApps = getSlaGameApps();
        String[] dualStaApps = getAllDualStaApps();
        synchronized (this.mPackagesAndUids) {
            if (whiteListApps != null) {
                try {
                    if (whiteListApps.length > 0) {
                        for (int i = 0; i < whiteListApps.length; i++) {
                            int uid = this.mPMS.getPackageUid(whiteListApps[i], 65536, this.mUserId);
                            if (UserHandle.getAppId(uid) <= 1000) {
                                this.mPackagesAndUids.put(whiteListApps[i], new Integer(-1));
                            } else {
                                this.mPackagesAndUids.put(whiteListApps[i], new Integer(uid));
                            }
                        }
                    }
                } finally {
                }
            }
            if (gameApps != null && gameApps.length > 0) {
                for (int i2 = 1; i2 < gameApps.length; i2++) {
                    int uid2 = this.mPMS.getPackageUid(gameApps[i2], 65536, this.mUserId);
                    if (UserHandle.getAppId(uid2) <= 1000) {
                        this.mPackagesAndUids.put(gameApps[i2], new Integer(-1));
                    } else {
                        this.mPackagesAndUids.put(gameApps[i2], new Integer(uid2));
                    }
                }
            }
            if (dualStaApps != null && dualStaApps.length > 0) {
                for (int i3 = 1; i3 < dualStaApps.length; i3++) {
                    int uid3 = this.mPMS.getPackageUid(dualStaApps[i3], 65536, 0);
                    if (uid3 <= 1000) {
                        this.mPackagesAndUids.put(dualStaApps[i3], new Integer(-1));
                    } else {
                        this.mPackagesAndUids.put(dualStaApps[i3], new Integer(uid3));
                    }
                }
            }
            Log.d(TAG, "updateSlaPackagesAndUids mPackagesAndUids:" + this.mPackagesAndUids);
        }
        sendVideoAppUid();
        sendDownloadAppUid();
        updateSkipDestroySocketList();
    }

    private String getPkgNameWithUid(int uid) {
        synchronized (this.mPackagesAndUids) {
            if (this.mPackagesAndUids != null && this.mPackagesAndUids.size() > 0) {
                for (String pkgName : this.mPackagesAndUids.keySet()) {
                    if (this.mPackagesAndUids.get(pkgName).intValue() == uid) {
                        Log.d(TAG, "getPkgNameWithUid " + uid + "->" + pkgName);
                        return pkgName;
                    }
                }
            }
            return null;
        }
    }

    private void uploadFeedbackData(String key, String value) {
        HashMap<String, String> map = new HashMap<>();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        map.put(key, formatter.format(date) + "   " + value);
        OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", key, map, false);
    }

    private boolean isCellUsageExceeded() {
        Date now;
        String[] params = SystemProperties.get("persist.sys.sla.traffic", "").split("-");
        if (params == null || params.length != 6) {
            Log.w(TAG, "isCellUsageExceeded Failed to get cell traffic data!");
            return false;
        }
        long rawCellTraffic = Long.parseLong(params[4]) + Long.parseLong(params[5]);
        long cellTrafficThreshold = this.mWifiRomUpdateHelper.getLongValue("NETWORK_SLA_CELL_USAGE_THRESHOLD", 21474836480L).longValue();
        long lastDataUsage = Settings.Global.getLong(this.mContext.getContentResolver(), LAST_CELL_TRAFFIC_USAGE, -1);
        long lastTimestamp = Settings.Global.getLong(this.mContext.getContentResolver(), LAST_CELL_TRAFFIC_TIME, -1);
        Date lastDate = new Date(lastTimestamp);
        Date now2 = new Date();
        if (mDebug) {
            StringBuilder sb = new StringBuilder();
            sb.append("isCellUsageExceeded lastDataUsage=");
            sb.append(lastDataUsage);
            sb.append(" rawCellTraffic=");
            sb.append(rawCellTraffic);
            sb.append(" lastDate=");
            sb.append(lastDate);
            sb.append(" now=");
            now = now2;
            sb.append(now);
            Log.d(TAG, sb.toString());
        } else {
            now = now2;
        }
        if (lastTimestamp == -1 || now.getMonth() != lastDate.getMonth()) {
            Settings.Global.putLong(this.mContext.getContentResolver(), LAST_CELL_TRAFFIC_TIME, System.currentTimeMillis());
            Settings.Global.putLong(this.mContext.getContentResolver(), LAST_CELL_TRAFFIC_USAGE, rawCellTraffic);
            if (lastTimestamp != -1 && now.getMonth() != lastDate.getMonth()) {
                long rawCellLastMonth = rawCellTraffic - lastDataUsage;
                uploadFeedbackData("cell_traffic_per_month", "" + rawCellLastMonth);
                Log.d(TAG, "isCellUsageExceeded report monthly cell traffic:" + ((rawCellLastMonth / 1024) / 1024) + "MB");
                boolean slaAutoEnabled = Settings.Global.getInt(this.mContext.getContentResolver(), SLA_AUTO_ENABLED, 0) == 1;
                if (rawCellLastMonth > this.mWifiRomUpdateHelper.getLongValue("NETWORK_SLA_AUTO_ENABLE_THRESHOLD", Long.valueOf((long) TEN_GB_IN_BYTES)).longValue() && !getSlaEnableState() && !slaAutoEnabled) {
                    Log.d(TAG, "isCellUsageExceeded auto ENABLE SLA switch...");
                    Settings.Global.putInt(this.mContext.getContentResolver(), KEY_SLA_SWITCH, 1);
                    Settings.Global.putInt(this.mContext.getContentResolver(), SLA_AUTO_ENABLED, 1);
                    uploadFeedbackData("sla_auto_enabled", "");
                    Log.d(TAG, "isCellUsageExceeded report sla auto enabled!!");
                }
            }
            Log.w(TAG, "isCellUsageExceeded Beginning of a new round!");
            return false;
        } else if (rawCellTraffic - lastDataUsage > cellTrafficThreshold) {
            Log.w(TAG, "isCellUsageExceeded == true!!");
            return true;
        } else if (!mDebug) {
            return false;
        } else {
            Log.w(TAG, "isCellUsageExceeded == false.");
            return false;
        }
    }

    private boolean isNetAndSimSupported() {
        String netOper = this.mTelephonyManager.getNetworkOperator();
        String simOper = this.mTelephonyManager.getSimOperator();
        String[] slaEnabledMCC = this.mWifiRomUpdateHelper.getSlaEnabledMcc();
        if (netOper == null || netOper.length() < 3 || simOper == null || simOper.length() < 3 || slaEnabledMCC == null || slaEnabledMCC.length == 0) {
            if (mDebug) {
                Log.d(TAG, "isNetAndSimSupported NOT supported net or sim or no configured MCC");
            }
            return false;
        }
        String netMCC = netOper.substring(0, 3);
        String simMCC = simOper.substring(0, 3);
        if (mDebug) {
            Log.d(TAG, "isNetAndSimSupported netMCC:" + netMCC + " simMCC:" + simMCC);
        }
        for (String mccFromXml : slaEnabledMCC) {
            if (mccFromXml != null && mccFromXml.contains(netMCC) && mccFromXml.contains(simMCC)) {
                if (!mDebug) {
                    return true;
                } else {
                    Log.d(TAG, "isSlaSupported found matched MCC from xml:" + mccFromXml);
                    return true;
                }
            }
        }
        if (mDebug) {
            Log.d(TAG, "isNetAndSimSupported NOT supported!");
        }
        return false;
    }

    private int getKingCardResult() {
        int isKingCard = Settings.Global.getInt(this.mContext.getContentResolver(), KING_CARD_KEY, 3);
        Log.d(TAG, "getKingCardResult isKingCard:" + isKingCard);
        return isKingCard;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setKingCardResult(int result) {
        Log.d(TAG, "setKingCardResult result:" + result);
        Settings.Global.putInt(this.mContext.getContentResolver(), KING_CARD_KEY, result);
    }

    private class SlaSwitchObserver extends ContentObserver {
        public SlaSwitchObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            if (OppoSlaManager.this.getSlaEnableState()) {
                if (OppoSlaManager.this.mWifiConnected || OppoSlaManager.this.mWifi2Connected) {
                    Log.d(OppoSlaManager.TAG, "SLA enabled by Settings, sendPidAndListen()..");
                    OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(7);
                    OppoSlaManager.this.mAsyncHandler.sendEmptyMessageDelayed(11, 1000);
                }
                OppoSlaManager oppoSlaManager = OppoSlaManager.this;
                oppoSlaManager.mUsingLTE = oppoSlaManager.mTelephonyManager.getNetworkType() == 13;
                OppoSlaManager.this.mTelephonyManager.listen(OppoSlaManager.this.mPhoneStateListener, OppoDataStallHelper.DATA_STALL_QCOM_HOST_SOFTAP_TX_TIMEOUT);
                OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(12);
                Settings.System.putInt(OppoSlaManager.this.mContext.getContentResolver(), "SLA_DIALOG_COUNT", 3);
                OppoSlaManager.this.setShowDialog(false);
            } else {
                if (OppoSlaManager.this.mSlaEnabled) {
                    Log.d(OppoSlaManager.TAG, "SLA disabled by Settings, disableSla()..");
                    OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(3);
                }
                OppoSlaManager.this.clearSlaTrafficStats();
            }
            OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(9);
        }
    }

    private class DualStaSwitchObserver extends ContentObserver {
        public DualStaSwitchObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            if (!OppoSlaManager.this.getDualStaEnableState()) {
                Log.d(OppoSlaManager.TAG, "SLA disabled by Settings, disableSla()..");
                OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(23);
            } else if (OppoSlaManager.this.mWifiConnected || OppoSlaManager.this.mWifi2Connected) {
                Log.d(OppoSlaManager.TAG, "SLA enabled by Settings, sendPidAndListen()..");
                OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(7);
                OppoSlaManager.this.mAsyncHandler.sendEmptyMessageDelayed(11, 1000);
            }
            OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(9);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendPidAndListen() {
        if (!this.mWifiRomUpdateHelper.getBooleanValue("NETWORK_SLA_ENABLED", true)) {
            Log.d(TAG, "SLA disabled by xml.");
        } else if (mNlfd == null && this.mNetlinkThread == null) {
            this.mNetlinkThread = new Thread() {
                /* class com.android.server.wifi.OppoSlaManager.AnonymousClass5 */

                /* JADX DEBUG: Multi-variable search result rejected for r11v45, resolved type: android.os.Handler */
                /* JADX DEBUG: Multi-variable search result rejected for r15v9, resolved type: android.os.Handler */
                /* JADX WARN: Multi-variable type inference failed */
                /* JADX WARN: Type inference failed for: r4v4 */
                /* JADX WARN: Type inference failed for: r4v5, types: [int, boolean] */
                /* JADX WARN: Type inference failed for: r4v6 */
                /* JADX WARNING: Unknown variable types count: 1 */
                public void run() {
                    Log.d(OppoSlaManager.TAG, "sendPidAndListen tid=" + Thread.currentThread().getId());
                    try {
                        FileDescriptor unused = OppoSlaManager.mNlfd = NetlinkSocket.forProto(33);
                        ?? r4 = 0;
                        Os.connect(OppoSlaManager.mNlfd, new NetlinkSocketAddress(0, 0));
                        boolean result = OppoSlaManager.this.sendToKernel((OppoSlaManager) 18, (short) 0);
                        Log.d(OppoSlaManager.TAG, "After sending pid:result=" + result);
                        OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(9);
                        OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(13);
                        OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(6);
                        OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(5);
                        OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(24);
                        while (true) {
                            ByteBuffer bytes = NetlinkSocket.recvMessage(OppoSlaManager.mNlfd, 8192, 0);
                            StructNlMsgHdr nlmsghdr = StructNlMsgHdr.parse(bytes);
                            if (!(nlmsghdr == null || OppoSlaManager.sSlaCallback == null)) {
                                short s = nlmsghdr.nlmsg_type;
                                boolean z = true;
                                if (s == 19) {
                                    int[] payload = new int[2];
                                    for (int i = 0; i < 2; i++) {
                                        try {
                                            bytes.position((i * 4) + 16);
                                            payload[i] = bytes.getInt();
                                        } catch (Exception e) {
                                            Log.d(OppoSlaManager.TAG, "Exception when read speed or rtt " + e);
                                        }
                                    }
                                    char c = r4 == true ? 1 : 0;
                                    char c2 = r4 == true ? 1 : 0;
                                    char c3 = r4 == true ? 1 : 0;
                                    int enableMode = payload[c];
                                    int activeType = payload[1];
                                    if (enableMode == OppoSlaManager.INVALID_PARAM || activeType == OppoSlaManager.INVALID_PARAM) {
                                        Log.e(OppoSlaManager.TAG, "Received message:OPPO_SLA_ENABLE invalid enableMode:" + enableMode + " ,activeType:" + activeType);
                                    } else {
                                        Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_ENABLE, mode=" + enableMode + " ,activeType=" + activeType);
                                        if (1 == enableMode) {
                                            boolean dualStaAppOnFocus = OppoSlaManager.this.isDualStaAppOnFocus();
                                            Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_ENABLE, dualStaAppOnFocus: " + dualStaAppOnFocus);
                                            if (dualStaAppOnFocus) {
                                                if (1 == activeType) {
                                                    OppoSlaManager.this.mDualStaBuriedPoint.setLowSpeedAcCount();
                                                } else if (2 == activeType) {
                                                    OppoSlaManager.this.mDualStaBuriedPoint.setLowScoreAcCount();
                                                } else if (3 == activeType) {
                                                    if (OppoSlaManager.this.isVideoAppOnFocus() || OppoSlaManager.this.isDownloadAppOnFocus()) {
                                                        OppoSlaManager.this.mDualStaBuriedPoint.setLowSpeedAcCount();
                                                    }
                                                } else if (4 == activeType) {
                                                    OppoSlaManager.this.mDualStaBuriedPoint.setDownloadAcCount();
                                                }
                                                OppoSlaManager.this.mDualStaActiveType = activeType;
                                                OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(22);
                                            }
                                        } else if (2 == enableMode) {
                                            boolean slaAppOnFocus = OppoSlaManager.this.isSlaAppOnFocus();
                                            Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_ENABLE, slaAppOnFocus: " + slaAppOnFocus);
                                            if (slaAppOnFocus) {
                                                OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(4);
                                            }
                                        }
                                    }
                                } else if (s == 20) {
                                    int disableMode = OppoSlaManager.this.getIntParam(nlmsghdr, bytes);
                                    if (disableMode == OppoSlaManager.INVALID_PARAM) {
                                        Log.e(OppoSlaManager.TAG, "Received message:OPPO_SLA_DISABLE invalid disableMode:" + disableMode);
                                    } else {
                                        Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_DISABLE, mode=" + disableMode);
                                        if (1 == disableMode && !OppoSlaManager.this.isVideoAppOnFocus()) {
                                            OppoSlaManager.this.mDualStaBuriedPoint.setKernelDisableCount();
                                            OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(23);
                                        } else if (2 == disableMode) {
                                            OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(3);
                                        }
                                    }
                                } else if (s == 27) {
                                    int rtt = OppoSlaManager.this.getIntParam(nlmsghdr, bytes);
                                    if (rtt == OppoSlaManager.INVALID_PARAM) {
                                        Log.e(OppoSlaManager.TAG, "Received message:OPPO_SLA_NOTIFY_GAME_RTT invalid rtt:" + rtt);
                                    } else {
                                        if (OppoSlaManager.mDebug) {
                                            Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_NOTIFY_GAME_RTT RTT=" + rtt);
                                        }
                                        Handler handler = OppoSlaManager.this.mHandler;
                                        Handler handler2 = OppoSlaManager.this.mHandler;
                                        handler.sendMessage(handler2.obtainMessage(1, "RTT=" + rtt));
                                    }
                                } else if (s == 38) {
                                    boolean whiteAppOnFocus = OppoSlaManager.this.isWhiteListAppOnFocus();
                                    OppoSlaManager oppoSlaManager = OppoSlaManager.this;
                                    if (OppoSlaManager.this.mTelephonyManager.getNetworkType() != 13) {
                                        boolean z2 = r4 == true ? 1 : 0;
                                        Object[] objArr = r4 == true ? 1 : 0;
                                        Object[] objArr2 = r4 == true ? 1 : 0;
                                        z = z2;
                                    }
                                    oppoSlaManager.mUsingLTE = z;
                                    if (OppoSlaManager.mDebug) {
                                        Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_SHOW_DIALOG_NOW whiteAppOnFocus=" + whiteAppOnFocus + " mUsingLTE=" + OppoSlaManager.this.mUsingLTE + " mCellEnabled=" + OppoSlaManager.this.mCellEnabled);
                                    }
                                    if (whiteAppOnFocus && OppoSlaManager.this.mUsingLTE && OppoSlaManager.this.mCellEnabled) {
                                        OppoSlaManager.this.mHandler.sendEmptyMessage(14);
                                    }
                                } else if (s == 49) {
                                    int count = OppoSlaManager.this.getIntParam(nlmsghdr, bytes);
                                    if (count == OppoSlaManager.INVALID_PARAM) {
                                        Log.e(OppoSlaManager.TAG, "Received message:OPPO_SLA_NOTIFY_GAME_RX_PKT invalid count:" + count);
                                    } else {
                                        if (OppoSlaManager.mDebug) {
                                            Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_NOTIFY_GAME_RX_PKT count=" + count);
                                        }
                                        Handler handler3 = OppoSlaManager.this.mHandler;
                                        Handler handler4 = OppoSlaManager.this.mHandler;
                                        handler3.sendMessage(handler4.obtainMessage(1, "Rx Count=" + count));
                                    }
                                } else if (s == 29) {
                                    boolean appOnFocus = false;
                                    int enabledMode = OppoSlaManager.this.getIntParam(nlmsghdr, bytes);
                                    if (enabledMode == OppoSlaManager.INVALID_PARAM) {
                                        Log.e(OppoSlaManager.TAG, "Received message:OPPO_SLA_ENABLED invalid enabledMode:" + enabledMode);
                                    } else {
                                        if (1 == enabledMode) {
                                            OppoSlaManager.this.mDualStaEnabled = true;
                                            appOnFocus = OppoSlaManager.this.isDualStaAppOnFocus();
                                            OppoSlaManager.this.mDualStaBuriedPoint.setSuccessAcCount();
                                            Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_ENABLED, dualStaAppOnFocus: " + appOnFocus);
                                            OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(3);
                                        } else if (2 == enabledMode) {
                                            OppoSlaManager.this.mSlaEnabled = true;
                                            appOnFocus = OppoSlaManager.this.isWhiteListAppOnFocus();
                                            Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_ENABLED, slaAppOnFocus: " + appOnFocus);
                                            OppoSlaManager.this.mAsyncHandler.sendEmptyMessage(23);
                                        }
                                        Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_ENABLED, mDestoryScoket= " + OppoSlaManager.this.mDestoryScoket);
                                        if (appOnFocus) {
                                            if (2 != enabledMode) {
                                                OppoSlaManager.this.destroyTopAppSockets();
                                            } else if (OppoSlaManager.this.mDestoryScoket) {
                                                OppoSlaManager.this.destroyTopAppSockets();
                                            } else {
                                                OppoSlaManager.this.mDestoryScoket = true;
                                                OppoSlaManager oppoSlaManager2 = OppoSlaManager.this;
                                                int i2 = r4 == true ? 1 : 0;
                                                int i3 = r4 == true ? 1 : 0;
                                                int i4 = r4 == true ? 1 : 0;
                                                oppoSlaManager2.sendToKernel((OppoSlaManager) OppoSlaManager.OPPO_SLA_WEIGHT_BY_WLAN_ASSIST, (short) i2);
                                            }
                                        }
                                        Handler handler5 = OppoSlaManager.this.mHandler;
                                        Handler handler6 = OppoSlaManager.this.mHandler;
                                        handler5.sendMessage(handler6.obtainMessage(1, "OPPO_SLA_ENABLED Mode=" + enabledMode));
                                    }
                                } else if (s == 30) {
                                    boolean appOnFocus2 = false;
                                    int disabledMode = OppoSlaManager.this.getIntParam(nlmsghdr, bytes);
                                    if (disabledMode == OppoSlaManager.INVALID_PARAM) {
                                        Log.e(OppoSlaManager.TAG, "Received message:OPPO_SLA_DISABLED invalid disabledMode:" + disabledMode);
                                    } else {
                                        if (1 == disabledMode) {
                                            OppoSlaManager.this.mDualStaEnabled = r4;
                                            OppoSlaManager.this.mDualStaActiveType = r4;
                                            appOnFocus2 = OppoSlaManager.this.isDualStaAppOnFocus();
                                            Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_DISABLED, dualStaAppOnFocus: " + appOnFocus2);
                                        } else if (2 == disabledMode) {
                                            OppoSlaManager.this.mSlaEnabled = r4;
                                            appOnFocus2 = OppoSlaManager.this.isWhiteListAppOnFocus();
                                            Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_DISABLED, slaAppOnFocus: " + appOnFocus2);
                                        }
                                        if (appOnFocus2) {
                                            OppoSlaManager.this.destroyTopAppSockets();
                                        }
                                        Handler handler7 = OppoSlaManager.this.mHandler;
                                        Handler handler8 = OppoSlaManager.this.mHandler;
                                        handler7.sendMessage(handler8.obtainMessage(1, "OPPO_SLA_DISABLED Mode=" + disabledMode));
                                    }
                                } else if (s == 34) {
                                    int[] data = new int[8];
                                    if (nlmsghdr.nlmsg_len < 48) {
                                        Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_NOTIFY_SPEED_RTT invalid length.");
                                    } else {
                                        int i5 = 0;
                                        for (int i6 = 8; i5 < i6; i6 = 8) {
                                            try {
                                                bytes.position((i5 * 4) + 16);
                                                data[i5] = bytes.getInt();
                                                i5++;
                                            } catch (Exception e2) {
                                                Log.d(OppoSlaManager.TAG, "Exception when read speed or rtt " + e2);
                                            }
                                        }
                                        synchronized (OppoSlaManager.this.mRttSpeedLock) {
                                            OppoSlaManager.this.mRecordIndex = (OppoSlaManager.this.mRecordIndex + 1) % 60;
                                            if (OppoSlaManager.this.mWifiSpeedRecord.size() >= 60) {
                                                OppoSlaManager.this.mWifiSpeedRecord.set(OppoSlaManager.this.mRecordIndex, Integer.valueOf(data[r4]));
                                                OppoSlaManager.this.mWifiRttRecord.set(OppoSlaManager.this.mRecordIndex, Integer.valueOf(data[1]));
                                                OppoSlaManager.this.mWifiSpeedRecord2.set(OppoSlaManager.this.mRecordIndex, Integer.valueOf(data[2]));
                                                OppoSlaManager.this.mWifiRttRecord2.set(OppoSlaManager.this.mRecordIndex, Integer.valueOf(data[3]));
                                                OppoSlaManager.this.mCellSpeedRecord.set(OppoSlaManager.this.mRecordIndex, Integer.valueOf(data[4]));
                                                OppoSlaManager.this.mCellRttRecord.set(OppoSlaManager.this.mRecordIndex, Integer.valueOf(data[5]));
                                            } else {
                                                OppoSlaManager.this.mWifiSpeedRecord.add(OppoSlaManager.this.mRecordIndex, Integer.valueOf(data[r4]));
                                                OppoSlaManager.this.mWifiRttRecord.add(OppoSlaManager.this.mRecordIndex, Integer.valueOf(data[1]));
                                                OppoSlaManager.this.mWifiSpeedRecord2.add(OppoSlaManager.this.mRecordIndex, Integer.valueOf(data[2]));
                                                OppoSlaManager.this.mWifiRttRecord2.add(OppoSlaManager.this.mRecordIndex, Integer.valueOf(data[3]));
                                                OppoSlaManager.this.mCellSpeedRecord.add(OppoSlaManager.this.mRecordIndex, Integer.valueOf(data[4]));
                                                OppoSlaManager.this.mCellRttRecord.add(OppoSlaManager.this.mRecordIndex, Integer.valueOf(data[5]));
                                            }
                                            OppoSlaManager.this.updateSpeedAndRtt(data[r4], data[1], data[2], data[3], data[4], data[5]);
                                        }
                                        OppoSlaManager.this.mKernelWorkMode = data[6];
                                        int mainWifiGoodSpeed = OppoSlaManager.this.mWifiRomUpdateHelper.getIntegerValue("OPPO_DUAL_STA_MAX_SPEED", Integer.valueOf((int) OppoSlaManager.MAIN_WIFI_GOOD_SPEED)).intValue();
                                        OppoSlaManager.this.mMainWifiGood = data[7] > mainWifiGoodSpeed ? true : r4;
                                        OppoSlaManager.this.mWlan0RxPkts = bytes.getLong(48);
                                        OppoSlaManager.this.mWlan0TxPkts = bytes.getLong(56);
                                        OppoSlaManager.this.mWlan1RxPkts = bytes.getLong(64);
                                        OppoSlaManager.this.mWlan1TxPkts = bytes.getLong(72);
                                        if (OppoSlaManager.this.mKernelWorkMode == 1 && ((!OppoSlaManager.this.mWifiConnected || !OppoSlaManager.this.mWifi2Connected) && !OppoSlaManager.this.mAsyncHandler.hasMessages(16))) {
                                            Log.e(OppoSlaManager.TAG, "Detected SLA_MODE_DUAL_WIFI when NOT both WiFi connected!!");
                                            OppoSlaManager.this.mAsyncHandler.sendMessageDelayed(OppoSlaManager.this.mAsyncHandler.obtainMessage(16, 1, r4), 1500);
                                        }
                                        if (OppoSlaManager.this.mKernelWorkMode != 2 || ((OppoSlaManager.this.mWifiConnected || OppoSlaManager.this.mWifi2Connected) && OppoSlaManager.this.mCellConnected)) {
                                            OppoSlaManager.this.mAsyncHandler.removeMessages(16);
                                        } else if (!OppoSlaManager.this.mAsyncHandler.hasMessages(16)) {
                                            Log.e(OppoSlaManager.TAG, "Detected SLA_MODE_WIFI_CELL when no wifi or cell connected!!");
                                            OppoSlaManager.this.mAsyncHandler.sendMessageDelayed(OppoSlaManager.this.mAsyncHandler.obtainMessage(16, 2, r4), 1500);
                                        }
                                    }
                                } else if (s == 35) {
                                    int gameType = OppoSlaManager.this.getIntParam(nlmsghdr, bytes);
                                    if (gameType == OppoSlaManager.INVALID_PARAM) {
                                        Log.e(OppoSlaManager.TAG, "Received message:OPPO_SLA_SWITCH_GAME_NETWORK invalid gameType:" + gameType);
                                    } else if (gameType == OppoSlaManager.INVALID_PARAM) {
                                        Log.e(OppoSlaManager.TAG, "Received message:OPPO_SLA_SWITCH_GAME_NETWORK invalid disabledMode:" + gameType);
                                    } else {
                                        synchronized (OppoSlaManager.this.mSlaGameStats) {
                                            if (gameType > 0) {
                                                if (gameType < OppoSlaManager.this.mSlaGameStats.size()) {
                                                    OppoSlaManager.this.mGameNetworkSwitched = true;
                                                    SlaGameStats gameStats = (SlaGameStats) OppoSlaManager.this.mSlaGameStats.get(gameType);
                                                    if (gameStats != null) {
                                                        gameStats.updateSwitchStats(1);
                                                        Log.d(OppoSlaManager.TAG, "OPPO_SLA_SWITCH_GAME_NETWORK after:" + gameStats);
                                                    } else {
                                                        Log.w(OppoSlaManager.TAG, "OPPO_SLA_SWITCH_GAME_NETWORK gameStats == null!!");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (s == 40) {
                                    if (OppoSlaManager.mDebug) {
                                        Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_SEND_APP_TRAFFIC");
                                    }
                                    OppoSlaManager.this.handleUpdateAppTraffic(nlmsghdr, bytes);
                                } else if (s != 41) {
                                    Log.w(OppoSlaManager.TAG, "Received unknow message:type=" + ((int) nlmsghdr.nlmsg_type));
                                } else {
                                    if (OppoSlaManager.mDebug) {
                                        Log.d(OppoSlaManager.TAG, "Received message:OPPO_SLA_SEND_GAME_APP_STATISTIC");
                                    }
                                    OppoSlaManager.this.handleUpdateGameAppStats(nlmsghdr, bytes);
                                }
                            }
                            r4 = 0;
                        }
                    } catch (Exception e3) {
                        Log.e(OppoSlaManager.TAG, "Fatal Exception when sendPidAndListen:tid=" + Thread.currentThread().getId(), e3);
                        OppoSlaManager.this.stopListening();
                    }
                }
            };
            this.mNetlinkThread.start();
        } else {
            Log.w(TAG, "Already listening!!");
        }
    }

    public void stopListening() {
        Log.d(TAG, "stopListening...");
        FileDescriptor fileDescriptor = mNlfd;
        if (fileDescriptor != null) {
            IoUtils.closeQuietly(fileDescriptor);
            mNlfd = null;
            this.mNetlinkThread = null;
        }
        setOppoSlaDisable(2);
        setOppoSlaDisable(1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getIntParam(StructNlMsgHdr nlmsghdr, ByteBuffer bytes) {
        if (nlmsghdr == null || bytes == null) {
            Log.e(TAG, "getIntParam invalid ByteBuffer");
            return INVALID_PARAM;
        } else if (nlmsghdr.nlmsg_len < 20) {
            Log.e(TAG, "getIntParam invalid length.");
            return INVALID_PARAM;
        } else {
            bytes.position(16);
            try {
                return bytes.getInt();
            } catch (BufferUnderflowException e) {
                Log.e(TAG, "getIntParam Exception " + e);
                return INVALID_PARAM;
            }
        }
    }

    private void startCheckingTopApp() {
        try {
            this.mActivityManager.addOnUidImportanceListener(this.mUidImportanceListener, 100);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "startCheckingTopApps already regisgered!");
        }
    }

    private void stopCheckingTopApp() {
        try {
            this.mActivityManager.removeOnUidImportanceListener(this.mUidImportanceListener);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "startCheckingTopApps already regisgered!");
        }
    }

    public void setWifiConnectionState(int ifIndex, boolean connected) {
        Log.d(TAG, "setWifiConnectionState(" + ifIndex + ", " + connected + ")");
        if (!this.mWifiConnected && !this.mWifi2Connected && connected) {
            this.mAsyncHandler.sendEmptyMessage(13);
            this.mAsyncHandler.sendEmptyMessage(9);
            this.mAsyncHandler.sendEmptyMessage(6);
            this.mAsyncHandler.sendEmptyMessage(5);
            this.mAsyncHandler.sendEmptyMessage(7);
            this.mAsyncHandler.sendEmptyMessage(18);
            this.mAsyncHandler.sendEmptyMessage(21);
        }
        if (ifIndex == 0) {
            this.mWifiConnected = connected;
        } else {
            this.mWifi2Connected = connected;
        }
        if (!connected && this.mDualStaEnabled) {
            setOppoSlaDisable(1);
        }
        if (!this.mWifiConnected && !this.mWifi2Connected && this.mSlaEnabled) {
            setOppoSlaDisable(2);
            clearRttAndSpeedRecords();
        }
        if (this.mWifiConnected && this.mWifi2Connected) {
            setOppoSlaEnable(1);
        }
        if (mDebug) {
            Log.d(TAG, "After setWifiConnectionState(" + ifIndex + ", " + connected + ")");
        }
    }

    public void setCellState(boolean enabled) {
        Log.d(TAG, "setCellState(" + enabled + ")");
        this.mCellEnabled = enabled;
        if (this.mCellEnabled) {
            this.mAsyncHandler.sendEmptyMessage(7);
        }
    }

    public void maybeEnableSla() {
        boolean switchState = !isCellUsageExceeded() && getSlaEnableState() && isNetAndSimSupported();
        boolean simCardReady = 5 == this.mTelephonyManager.getSimState();
        boolean usingLTE = this.mTelephonyManager.isDataEnabled() && this.mTelephonyManager.getNetworkType() == 13;
        boolean wifiInterResult = sSlaCallback.getWifiInterResult();
        Log.d(TAG, "maybeEnableSla switchState:" + switchState + " mSlaEnabled:" + this.mSlaEnabled + " mWifiConnected:" + this.mWifiConnected + " mWifi2Connected:" + this.mWifi2Connected + " mCellEnabled:" + this.mCellEnabled + " mCellQualityGood:" + this.mCellQualityGood + " simCardReady:" + simCardReady + " usingLTE:" + usingLTE + " mCellConnected:" + this.mCellConnected + " mSlaEnabling:" + this.mSlaEnabling + " wifiInterResult:" + wifiInterResult);
        this.mAsyncHandler.removeMessages(3);
        if (!switchState || this.mSlaEnabled || ((!this.mWifiConnected && !this.mWifi2Connected) || !wifiInterResult || !this.mCellEnabled || !this.mCellQualityGood || !simCardReady || !usingLTE)) {
            this.mAsyncHandler.removeMessages(20);
            this.mSlaEnabling = false;
        } else if (this.mSlaEnabling) {
        } else {
            if (this.mCellConnected) {
                setOppoSlaEnable(2);
                return;
            }
            Log.d(TAG, "enableSla...");
            OppoSlaCallback oppoSlaCallback = sSlaCallback;
            if (oppoSlaCallback != null) {
                oppoSlaCallback.enableSla();
                this.mSlaEnabling = true;
            }
            this.mAsyncHandler.sendEmptyMessageDelayed(20, 20000);
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(1, "enableSla..."));
        }
    }

    private String[] getEnabledWhiteListApps() {
        String[] whiteListApps = getSlaWhiteListApps();
        if (whiteListApps == null || whiteListApps.length == 0) {
            return null;
        }
        ArrayList<String> blackList = getAllBlackListApps();
        ArrayList<String> cellDisabledApps = getAllCellDisabledApps();
        ArrayList<String> enabledApps = new ArrayList<>();
        for (int i = 0; i < whiteListApps.length; i++) {
            if (getSlaAppState(whiteListApps[i]) && ((blackList == null || !blackList.contains(whiteListApps[i])) && (cellDisabledApps == null || !cellDisabledApps.contains(whiteListApps[i])))) {
                enabledApps.add(whiteListApps[i]);
            }
        }
        if (mDebug) {
            Log.d(TAG, "getEnabledWhiteListApps:" + Arrays.toString(enabledApps.toArray()));
        }
        return (String[]) enabledApps.toArray(new String[enabledApps.size()]);
    }

    private ArrayList<String> getAllBlackListApps() {
        ArrayList<String> blackList;
        String[] blackListApps = this.mWifiRomUpdateHelper.getSlaBlackListApps();
        if (blackListApps == null || blackListApps.length == 0) {
            blackList = null;
        } else {
            blackList = new ArrayList<>(Arrays.asList(blackListApps));
        }
        if (mDebug) {
            StringBuilder sb = new StringBuilder();
            sb.append("getAllBlackListApps:");
            sb.append(blackList == null ? "null" : Arrays.toString(blackList.toArray()));
            Log.d(TAG, sb.toString());
        }
        return blackList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x007c, code lost:
        if (0 == 0) goto L_0x007f;
     */
    private ArrayList<String> getAllCellDisabledApps() {
        ArrayList<String> apps = new ArrayList<>();
        Cursor c = null;
        try {
            c = this.mContext.getContentResolver().query(Uri.parse(URI), PROJECTION, SELECTION, null, null);
            if (c == null) {
                Log.e(TAG, "getAllCellDisabledApps failed to query!!");
                if (c != null) {
                    c.close();
                }
                return null;
            } else if (c.getCount() == 0) {
                if (mDebug) {
                    Log.d(TAG, "getAllCellDisabledApps no cell disabled app found..");
                }
                c.close();
                return null;
            } else {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    String pkgName = c.getString(0);
                    if (!TextUtils.isEmpty(pkgName)) {
                        apps.add(pkgName);
                    }
                    c.moveToNext();
                }
                c.close();
                if (mDebug) {
                    Log.d(TAG, "getAllCellDisabledApps:" + Arrays.toString(apps.toArray()));
                }
                if (apps.size() == 0) {
                    return null;
                }
                return apps;
            }
        } catch (Exception e) {
            Log.d(TAG, "getAllCellDisabledApps Exception:" + e);
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean sendToKernel(short type, int[] data) {
        if (mNlfd != null) {
            try {
                StructNlMsgHdr nlMsgHdr = new StructNlMsgHdr();
                nlMsgHdr.nlmsg_len = 16 + ((data == null ? 0 : data.length) * 4);
                nlMsgHdr.nlmsg_type = type;
                nlMsgHdr.nlmsg_flags = 1;
                nlMsgHdr.nlmsg_pid = Process.myPid();
                byte[] msg = new byte[nlMsgHdr.nlmsg_len];
                ByteBuffer byteBuffer = ByteBuffer.wrap(msg);
                byteBuffer.order(ByteOrder.nativeOrder());
                nlMsgHdr.pack(byteBuffer);
                if (data != null) {
                    for (int i : data) {
                        byteBuffer.putInt(i);
                    }
                }
                if (msg.length == NetlinkSocket.sendMessage(mNlfd, msg, 0, msg.length, (long) IO_TIMEOUT)) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Exception when sendToKernel:" + e);
            }
        } else {
            if (type < 17 || type >= 58) {
                Log.e(TAG, "sendToKernel invalid message type:" + ((int) type));
            } else {
                Log.e(TAG, "sendToKernel [" + getMessageName(type) + "] failed, mNlSock=null !!!");
            }
            return false;
        }
    }

    private boolean sendToKernel(short type, byte[] data) {
        if (mNlfd != null) {
            try {
                StructNlMsgHdr nlMsgHdr = new StructNlMsgHdr();
                nlMsgHdr.nlmsg_len = 16 + (data == null ? 0 : data.length);
                nlMsgHdr.nlmsg_type = type;
                nlMsgHdr.nlmsg_flags = 1;
                nlMsgHdr.nlmsg_pid = Process.myPid();
                byte[] msg = new byte[nlMsgHdr.nlmsg_len];
                ByteBuffer byteBuffer = ByteBuffer.wrap(msg);
                byteBuffer.order(ByteOrder.nativeOrder());
                nlMsgHdr.pack(byteBuffer);
                if (data != null) {
                    byteBuffer.put(data);
                }
                if (msg.length == NetlinkSocket.sendMessage(mNlfd, msg, 0, msg.length, (long) IO_TIMEOUT)) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Exception when sendToKernel:" + e);
            }
        } else {
            if (type < 17 || type >= 58) {
                Log.e(TAG, "sendToKernel invalid message type:" + ((int) type));
            } else {
                Log.e(TAG, "sendToKernel [" + getMessageName(type) + "] failed, mNlSock=null !!!");
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean sendToKernel(short type, int data) {
        if (mNlfd != null) {
            try {
                StructNlMsgHdr nlMsgHdr = new StructNlMsgHdr();
                nlMsgHdr.nlmsg_len = 20;
                nlMsgHdr.nlmsg_type = type;
                nlMsgHdr.nlmsg_flags = 1;
                nlMsgHdr.nlmsg_pid = Process.myPid();
                byte[] msg = new byte[nlMsgHdr.nlmsg_len];
                ByteBuffer byteBuffer = ByteBuffer.wrap(msg);
                byteBuffer.order(ByteOrder.nativeOrder());
                nlMsgHdr.pack(byteBuffer);
                byteBuffer.putInt(data);
                if (msg.length == NetlinkSocket.sendMessage(mNlfd, msg, 0, msg.length, (long) IO_TIMEOUT)) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Exception when sendToKernel:" + e);
            }
        } else {
            if (type < 17 || type >= 58) {
                Log.e(TAG, "sendToKernel invalid message type:" + ((int) type));
            } else {
                Log.e(TAG, "sendToKernel [" + getMessageName(type) + "] failed, mNlSock=null !!!");
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSwitchStateToKernel() {
        boolean slaEnable = !isCellUsageExceeded() && getSlaEnableState() && isNetAndSimSupported();
        boolean dualStaEnable = getDualStaEnableState();
        int[] data = new int[2];
        data[0] = slaEnable ? 1 : 0;
        data[1] = dualStaEnable ? 1 : 0;
        boolean result = sendToKernel(OPPO_SLA_NOTIFY_SWITCH_STATE, data);
        Log.d(TAG, "After sendSwitchStateToKernel:slaEnable=" + slaEnable + " dualStaEnable=" + dualStaEnable + " result=" + result);
    }

    public void sendWifiScoreToKernel(int ifIndex, int score) {
        boolean result = sendToKernel((short) 17, new int[]{ifIndex, score});
        if (mDebug) {
            Log.d("OppoSlaManager_score", "After sendWifiScoreToKernel:result=" + result + " ifIndex=" + ifIndex + " score=" + score);
        }
    }

    public void setCellScore(int score) {
        if (mDebug) {
            Log.d("OppoSlaManager_score", "setCellScore: mCellScore=" + this.mCellScore + " -> newScore=" + score + " mCellConnected=" + this.mCellConnected);
        }
        if (this.mCellScore != score) {
            this.mCellScore = score;
            this.mAsyncHandler.sendEmptyMessage(12);
        }
        if (this.mSlaEnabled && this.mCellScore != 50) {
            Log.d(TAG, "Cellular has no Internet access, disableSla()..");
            this.mAsyncHandler.sendEmptyMessage(3);
        }
    }

    private boolean sendSingleGameAppUid(int gameIndex, int uid) {
        boolean result = sendToKernel(OPPO_SLA_NOTIFY_GAME_APP_UID, new int[]{gameIndex, uid});
        if (mDebug) {
            Log.d(TAG, "After sendSingleGameAppUid:result=" + result + " gameIndex=" + gameIndex + " uid=" + uid);
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendGameAppUid() {
        if (this.mPMS != null) {
            String[] gameApps = getSlaGameApps();
            if (gameApps == null || gameApps.length == 0) {
                Log.w(TAG, "sendGameAppUid no game app found!!");
                return;
            }
            synchronized (this.mSlaGameStats) {
                if (this.mSlaGameStats.isEmpty()) {
                    this.mSlaGameStats.add(0, new SlaGameStats(0));
                    for (int i = 1; i < gameApps.length; i++) {
                        SlaGameStats gameStats = new SlaGameStats(getGameStats(i));
                        if (gameStats.game_type == i) {
                            if (mDebug) {
                                Log.d(TAG, "sendGameAppUid adding game stats:" + gameStats);
                            }
                            this.mSlaGameStats.add(i, gameStats);
                        } else {
                            Log.e(TAG, "sendGameAppUid invalid game stats:" + gameStats);
                            this.mSlaGameStats.add(i, new SlaGameStats(i));
                            setGameStats(i, this.mSlaGameStats.get(i).toString());
                        }
                    }
                }
            }
            ArrayList<String> blackList = getAllBlackListApps();
            synchronized (mGameAppUids) {
                mGameAppUids.clear();
                boolean honorOifaceState = this.mWifiRomUpdateHelper.getBooleanValue("NETWORK_SLA_HONOR_OIFACE_STATE", true);
                mGameAppUids.add(0, -1);
                for (int i2 = 1; i2 < gameApps.length; i2++) {
                    int uid = this.mPMS.getPackageUid(gameApps[i2], 65536, this.mUserId);
                    mGameAppUids.add(i2, Integer.valueOf(uid));
                    if (UserHandle.getAppId(uid) <= 1000) {
                        if (mDebug) {
                            Log.w(TAG, "sendGameAppUid invalid uid for game:" + gameApps[i2]);
                        }
                    } else if (blackList == null || !blackList.contains(gameApps[i2])) {
                        if (getSlaAppState(gameApps[i2])) {
                            sendSingleGameAppUid(i2, uid);
                            if (!honorOifaceState) {
                                Log.w(TAG, "sendGameAppUid not honor Oiface game state:" + gameApps[i2] + " result=" + sendToKernel(OPPO_SLA_NOTIFY_GAME_STATE, new int[]{i2, 1}));
                            }
                        } else if (mDebug) {
                            Log.w(TAG, "sendGameAppUid game app disabled:" + gameApps[i2]);
                        }
                    } else if (mDebug) {
                        Log.w(TAG, "sendGameAppUid game in black list:" + gameApps[i2]);
                    }
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendScreenStateToKernel(boolean screenState) {
        boolean result = sendToKernel(OPPO_SLA_NOTIFY_SCREEN_STATE, screenState ? 1 : 0);
        Log.d(TAG, "After sendScreenStateToKernel:result=" + result + " screenState=" + ((int) screenState));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendWhiteListAppUid() {
        if (mNlfd == null) {
            Log.w(TAG, "sendWhiteListAppUid mNlSock == null!");
            return;
        }
        boolean result = false;
        if (this.mPMS != null) {
            int index = 1;
            String[] whiteListApps = getEnabledWhiteListApps();
            if (whiteListApps == null || whiteListApps.length == 0) {
                Log.w(TAG, "sendWhiteListAppUid no enabled app found...");
                return;
            }
            int[] data = new int[(whiteListApps.length + 1)];
            synchronized (mWhiteListAppUids) {
                mWhiteListAppUids.clear();
                for (int i = 0; i < whiteListApps.length; i++) {
                    int uid = this.mPMS.getPackageUid(whiteListApps[i], 65536, this.mUserId);
                    if (UserHandle.getAppId(uid) > 1000) {
                        mWhiteListAppUids.add(Integer.valueOf(uid));
                        data[index] = uid;
                        if (mDebug) {
                            Log.d(TAG, "sendWhiteListAppUid data[" + index + "]=" + uid);
                        }
                        index++;
                    } else if (mDebug) {
                        Log.w(TAG, "sendWhiteListAppUid invalid uid for app:" + whiteListApps[i]);
                    }
                }
            }
            data[0] = index - 1;
            result = sendToKernel(OPPO_SLA_NOTIFY_WHITE_LIST_APP, data);
        }
        Log.d(TAG, "After sendWhiteListAppUid:result=" + result);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendVideoAppUid() {
        if (!isDualStaSupported()) {
            if (mDebug) {
                Log.d(TAG, "sendVideoAppUid isDualStaSupported() == false.");
            }
        } else if (mNlfd == null) {
            Log.w(TAG, "sendDownloadAppUid mNlSock == null!");
        } else if (this.mPMS != null) {
            int index = 1;
            String[] videoApps = this.mWifiRomUpdateHelper.getAllVideoApps();
            if (videoApps == null || videoApps.length == 0) {
                Log.w(TAG, "sendVideoAppUid no video app found...");
                return;
            }
            int[] data = new int[(videoApps.length + 1)];
            synchronized (mVideoApps) {
                mVideoApps.clear();
                for (int i = 0; i < videoApps.length; i++) {
                    int uid = this.mPMS.getPackageUid(videoApps[i], 65536, 0);
                    if (uid > 1000) {
                        mVideoApps.add(Integer.valueOf(uid));
                        if (mDebug) {
                            Log.d(TAG, "sendVideoAppUid ptkName:" + videoApps[i] + " ,uid:" + uid);
                        }
                        data[index] = uid;
                        index++;
                    } else if (mDebug) {
                        Log.w(TAG, "sendVideoAppUid invalid uid for app:" + videoApps[i]);
                    }
                }
            }
            data[0] = index - 1;
            sendToKernel(OPPO_SLA_NOTIFY_VEDIO_APP, data);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendDownloadAppUid() {
        if (!isDualStaSupported()) {
            if (mDebug) {
                Log.d(TAG, "sendVideoAppUid isDualStaSupported() == false.");
            }
        } else if (mNlfd == null) {
            Log.w(TAG, "sendDownloadAppUid mNlSock == null!");
        } else {
            boolean result = false;
            if (this.mPMS != null) {
                int index = 1;
                String[] downloadtApps = this.mWifiRomUpdateHelper.getDownloadApps();
                if (downloadtApps == null || downloadtApps.length == 0) {
                    Log.w(TAG, "sendDownloadAppUid no app found...");
                    return;
                }
                int[] data = new int[(downloadtApps.length + 1)];
                synchronized (mDownloadApps) {
                    mDownloadApps.clear();
                    for (int i = 0; i < downloadtApps.length; i++) {
                        int uid = this.mPMS.getPackageUid(downloadtApps[i], 65536, 0);
                        if (uid > 1000) {
                            mDownloadApps.add(Integer.valueOf(uid));
                            data[index] = uid;
                            if (mDebug) {
                                Log.d(TAG, "sendDownloadAppUid data[" + index + "]=" + uid);
                            }
                            index++;
                        } else if (mDebug) {
                            Log.w(TAG, "sendDownloadAppUid invalid uid for app:" + downloadtApps[i]);
                        }
                    }
                }
                data[0] = index - 1;
                result = sendToKernel(OPPO_SLA_NOTIFY_DOWNLOAD_APP, data);
            }
            Log.d(TAG, "After sendDownloadAppUid:result=" + result);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSlaParams() {
        SlaParams slaParams = new SlaParams(this.mWifiRomUpdateHelper.getSlaParams());
        boolean result = sendToKernel(OPPO_SLA_NOTIFY_PARAMS, slaParams.getParams());
        Log.d(TAG, "After sendSlaParams:" + slaParams + ", result=" + result);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSlaGameParams() {
        String[] gameParams = this.mWifiRomUpdateHelper.getSlaGameParams();
        if (gameParams == null || gameParams.length == 0) {
            Log.d(TAG, "sendSlaGameParams NO game params configured!!");
            return;
        }
        for (int i = 0; i + 1 < gameParams.length; i += 2) {
            SlaGameParams param = new SlaGameParams();
            if (!param.setGameParams((i / 2) + 1, gameParams[i].split("#"), gameParams[i + 1].split("#"))) {
                Log.d(TAG, "sendSlaGameParams failed to set params for game:" + (i / 2));
            } else {
                boolean result = sendToKernel(OPPO_SLA_NOTIFY_GAME_RTT_PARAMS, param.getGameParams());
                Log.d(TAG, "After sendSlaGameParams:" + param + ", result=" + result);
            }
        }
    }

    private void enableGameRttDetect(boolean enable) {
        boolean result = sendToKernel(enable ? OPPO_SLA_ENABLE_GAME_RTT : 32, 0);
        Log.d(TAG, "After enableGameRttDetect(" + enable + "):result=" + result);
    }

    public boolean isGameInFront() {
        return this.mGameInFront;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTopUidChanged(int uid, int impt) {
        int index;
        synchronized (mGameAppUids) {
            index = mGameAppUids.indexOf(new Integer(uid));
        }
        if (index > 0) {
            int[] data = new int[2];
            data[0] = index;
            if (impt <= 100) {
                data[1] = 1;
            } else {
                data[1] = 0;
            }
            this.mGameInFront = data[1] > 0;
            boolean result = sendToKernel(OPPO_SLA_NOTIFY_GAME_IN_FRONT, data);
            Log.d(TAG, "handleTopUidChanged notify game in fromt, index=" + data[0] + ", in front=" + data[1] + ", result=" + result);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCellQualityChange() {
        boolean z = true;
        this.mUsingLTE = this.mTelephonyManager.getNetworkType() == 13;
        if (!this.mUsingLTE || this.mLteRsrp <= LTE_BAD_RSSI || (this.mCellScore != 50 && this.mCellConnected)) {
            z = false;
        }
        this.mCellQualityGood = z;
        Log.d(TAG, "handleCellQualityChange mCellQualityGood=" + this.mCellQualityGood + ", result=" + sendToKernel(OPPO_SLA_NOTIFY_CELL_QUALITY, this.mCellQualityGood ? 1 : 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyShowDialog() {
        boolean slaSupported = isSlaSupported();
        boolean result = sendToKernel(OPPO_SLA_NOTIFY_SHOW_DIALOG, (!slaSupported || !this.mShowDialog) ? 0 : 1);
        Log.d(TAG, "handleNotifyShowDialog slaSupported=" + slaSupported + " mShowDialog=" + this.mShowDialog + ", result=" + result);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGameStateChange(String pkgName, boolean start) {
        String[] gameApps = getSlaGameApps();
        if (gameApps != null && gameApps.length > 0) {
            for (int i = 1; i < gameApps.length; i++) {
                if (gameApps[i] != null && gameApps[i].equals(pkgName)) {
                    Log.d(TAG, "handleGameStateChange gameType=" + i + " pkgName=" + pkgName + " start=" + start);
                    boolean honorOifaceState = this.mWifiRomUpdateHelper.getBooleanValue("NETWORK_SLA_HONOR_OIFACE_STATE", true);
                    int[] data = new int[2];
                    int i2 = 0;
                    data[0] = i;
                    if (start || !honorOifaceState) {
                        i2 = 1;
                    }
                    data[1] = i2;
                    Log.d(TAG, "After handleGameStateChange result=" + sendToKernel(OPPO_SLA_NOTIFY_GAME_STATE, data));
                    Handler handler = this.mHandler;
                    StringBuilder sb = new StringBuilder();
                    sb.append("game ");
                    sb.append(start ? "start" : "stop");
                    sb.append(" pkgName=");
                    sb.append(pkgName);
                    handler.sendMessage(handler.obtainMessage(1, sb.toString()));
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateAppTraffic(StructNlMsgHdr nlmsghdr, ByteBuffer bytes) {
        String str;
        try {
            if (nlmsghdr.nlmsg_len < 1300) {
                Log.e(TAG, "handleUpdateAppTraffic invalid data len=" + nlmsghdr.nlmsg_len + " bytes=" + bytes);
                return;
            }
            int i = 16;
            int count = bytes.getInt(16);
            if (mDebug) {
                Log.d(TAG, "handleUpdateAppTraffic count=" + count);
            }
            int i2 = 0;
            while (i2 < count) {
                int uid = bytes.getInt(((i2 + 1) * 4) + i);
                if (UserHandle.getAppId(uid) <= 1000) {
                    Log.w(TAG, "handleUpdateAppTraffic invalid uid:" + uid);
                } else {
                    String pkgName = getPkgNameWithUid(uid);
                    if (TextUtils.isEmpty(pkgName)) {
                        Log.w(TAG, "handleUpdateAppTraffic invalid pkgName, uid=" + uid);
                    } else {
                        long traffic = bytes.getLong((i2 * 8) + 276);
                        if (traffic <= 0 && mDebug) {
                            Log.d(TAG, "handleUpdateAppTraffic uid=" + uid + " traffic=" + traffic);
                        }
                        if (traffic > 0) {
                            if (mDebug) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("handleUpdateAppTraffic getAppTraffic(");
                                sb.append(pkgName);
                                sb.append(")=");
                                str = "handleUpdateAppTraffic uid=";
                                sb.append(getAppTraffic(pkgName));
                                Log.d(TAG, sb.toString());
                            } else {
                                str = "handleUpdateAppTraffic uid=";
                            }
                            long sum = getAppTraffic(pkgName) + traffic;
                            setAppTraffic(pkgName, sum);
                            if (mDebug) {
                                Log.d(TAG, "handleUpdateAppTraffic setAppTraffic(" + pkgName + ", " + sum + ")");
                            }
                        } else {
                            str = "handleUpdateAppTraffic uid=";
                        }
                        long normalTraffic = bytes.getLong((i2 * 8) + 788);
                        if (normalTraffic <= 0 && mDebug) {
                            Log.d(TAG, str + uid + " normalTraffic=" + normalTraffic);
                        }
                        if (normalTraffic > 0) {
                            if (mDebug) {
                                Log.d(TAG, "handleUpdateAppTraffic getAppNormalTraffic(" + pkgName + ")=" + getAppNormalTraffic(pkgName));
                            }
                            long sum2 = getAppNormalTraffic(pkgName) + normalTraffic;
                            setAppNormalTraffic(pkgName, sum2);
                            if (mDebug) {
                                Log.d(TAG, "handleUpdateAppTraffic setAppNormalTraffic(" + pkgName + ", " + sum2 + ")");
                            }
                        }
                    }
                }
                i2++;
                i = 16;
            }
            Log.d(TAG, "handleUpdateAppTraffic successful..");
        } catch (Exception e) {
            Log.e(TAG, "Exception when handleUpdateAppTraffic:" + e);
        }
    }

    /* access modifiers changed from: private */
    public class SlaGameStats {
        static final int GAME_STATS_COUNT = 7;
        int avg_rtt;
        int game_type;
        int rtt150Num;
        int rtt200Num;
        int rtt250Num;
        long rttNormal;
        int switch_count;

        public SlaGameStats(int index) {
            this.game_type = index;
            this.avg_rtt = 0;
            this.rttNormal = 0;
            this.rtt150Num = 0;
            this.rtt200Num = 0;
            this.rtt250Num = 0;
            this.switch_count = 0;
        }

        public SlaGameStats(String stats) {
            if (!TextUtils.isEmpty(stats)) {
                String[] params = stats.split(",");
                if (params == null || params.length != 7) {
                    Log.e(OppoSlaManager.TAG, "Failed Constructing SlaGameStats:" + stats);
                    this.game_type = -1;
                    return;
                }
                try {
                    this.game_type = Integer.parseInt(params[0]);
                    this.avg_rtt = Integer.parseInt(params[1]);
                    this.rttNormal = Long.parseLong(params[2]);
                    this.rtt150Num = Integer.parseInt(params[3]);
                    this.rtt200Num = Integer.parseInt(params[4]);
                    this.rtt250Num = Integer.parseInt(params[5]);
                    this.switch_count = Integer.parseInt(params[6]);
                } catch (Exception e) {
                    Log.e(OppoSlaManager.TAG, "Exception when Constructing SlaGameStats:" + stats);
                    this.game_type = -1;
                }
            } else {
                Log.e(OppoSlaManager.TAG, "Failed Constructing SlaGameStats:" + stats);
                this.game_type = -1;
            }
        }

        public void updateRttStats(int rtt, long rttLess150, int rtt150, int rtt200, int rtt250) {
            if (rtt > 0) {
                this.avg_rtt = (this.avg_rtt + rtt) / 2;
            }
            this.rttNormal += rttLess150;
            this.rtt150Num += rtt150;
            this.rtt200Num += rtt200;
            this.rtt250Num += rtt250;
        }

        public void updateSwitchStats(int switchCount) {
            this.switch_count += switchCount;
        }

        public String toString() {
            return this.game_type + "," + this.avg_rtt + "," + this.rttNormal + "," + this.rtt150Num + "," + this.rtt200Num + "," + this.rtt250Num + "," + this.switch_count;
        }
    }

    /* access modifiers changed from: private */
    public class SlaParams {
        final int PARAMS_COUNT = 13;
        int cell_speed;
        int cjzc_rtt;
        int dual_wifi_rtt;
        int dual_wlan_bad_score;
        int dual_wlan_download_speed;
        int second_wlan_speed;
        int sla_rtt;
        int sla_speed;
        int wifi_bad_score;
        int wifi_good_score;
        int wlan_little_score_speed;
        int wlan_speed;
        int wzry_rtt;

        public SlaParams() {
            setDefaultParams();
        }

        public SlaParams(String[] params) {
            if (params == null || params.length != 13) {
                Log.w(OppoSlaManager.TAG, "invalid SLA param:" + params);
                setDefaultParams();
                return;
            }
            try {
                this.sla_speed = Integer.parseInt(params[0]);
                this.cell_speed = Integer.parseInt(params[1]);
                this.wlan_speed = Integer.parseInt(params[2]);
                this.wlan_little_score_speed = Integer.parseInt(params[3]);
                this.sla_rtt = Integer.parseInt(params[4]);
                this.wzry_rtt = Integer.parseInt(params[5]);
                this.cjzc_rtt = Integer.parseInt(params[6]);
                this.wifi_bad_score = Integer.parseInt(params[7]);
                this.wifi_good_score = Integer.parseInt(params[8]);
                this.second_wlan_speed = Integer.parseInt(params[9]);
                this.dual_wlan_download_speed = Integer.parseInt(params[10]);
                this.dual_wifi_rtt = Integer.parseInt(params[11]);
                this.dual_wlan_bad_score = Integer.parseInt(params[12]);
            } catch (Exception e) {
                Log.e(OppoSlaManager.TAG, "Exception when Constructing SlaParams:" + Arrays.toString(params));
                setDefaultParams();
            }
        }

        private void setDefaultParams() {
            this.sla_speed = ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS;
            this.cell_speed = 500;
            this.wlan_speed = 1000;
            this.wlan_little_score_speed = 500;
            this.sla_rtt = 230;
            this.wzry_rtt = ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS;
            this.cjzc_rtt = 220;
            this.wifi_bad_score = 55;
            this.wifi_good_score = 75;
            this.second_wlan_speed = OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL;
            this.dual_wlan_download_speed = 2200;
            this.dual_wifi_rtt = ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS;
            this.dual_wlan_bad_score = 55;
        }

        public int[] getParams() {
            int[] params = {13, this.sla_speed, this.cell_speed, this.wlan_speed, this.wlan_little_score_speed, this.sla_rtt, this.wzry_rtt, this.cjzc_rtt, this.wifi_bad_score, this.wifi_good_score, this.second_wlan_speed, this.dual_wlan_download_speed, this.dual_wifi_rtt, this.dual_wlan_bad_score};
            if (OppoSlaManager.mDebug) {
                Log.d(OppoSlaManager.TAG, "SlaParams.getParams() -> " + Arrays.toString(params));
            }
            return params;
        }

        public String toString() {
            return "SlaParams[sla_speed=" + this.sla_speed + ", cell_speed=" + this.cell_speed + ", wlan_speed=" + this.wlan_speed + ", wlan_little_score_speed=" + this.wlan_little_score_speed + ", sla_rtt=" + this.sla_rtt + ", wzry_rtt=" + this.wzry_rtt + ", cjzc_rtt=" + this.cjzc_rtt + ", wifi_bad_score=" + this.wifi_bad_score + ", wifi_good_score=" + this.wifi_good_score + ", second_wlan_speed=" + this.second_wlan_speed + ", dual_wlan_download_speed=" + this.dual_wlan_download_speed + ", dual_wifi_rtt=" + this.dual_wifi_rtt + ", dual_wlan_bad_score=" + this.dual_wlan_bad_score + "]";
        }
    }

    public class DualStaBuriedPoint {
        public long activeLastTime;
        public int badCount;
        public int downloadActiveCount;
        public int freq24gAnd24gCount;
        public int freq24gAnd5gCount;
        public int freq5gAnd24gCount;
        public int freq5gAnd5gCount;
        public int goodCunt;
        public int kernelDisableCount;
        public int lowScoreActiveCount;
        public int lowSpeedActiveCount;
        public int manualActiveCount;
        public int manualDisableCount;
        public int requestActiveCount;
        public int requestDisableCount;
        public int rusSwitchEnableCount;
        public boolean rusSwitchSate;
        public int screenOffDisableCount;
        public int settingSwitchEnableCount;
        public boolean settingSwitchSate;
        public int successActiveCount;
        public long totalActiveTime;
        public int totalDisableCount;
        public int totalWlan1Traffic;

        public DualStaBuriedPoint() {
        }

        public void setSwitchSate(boolean settingState, boolean rusState) {
            if (!this.settingSwitchSate && settingState) {
                this.settingSwitchEnableCount++;
            }
            if (!this.rusSwitchSate && rusState) {
                this.rusSwitchEnableCount++;
            }
            this.settingSwitchSate = settingState;
            this.rusSwitchSate = rusState;
        }

        public void setDownloadAcCount() {
            this.downloadActiveCount++;
        }

        public void setLowScoreAcCount() {
            this.lowScoreActiveCount++;
        }

        public void setLowSpeedAcCount() {
            this.lowSpeedActiveCount++;
        }

        public void setKernelDisableCount() {
            this.kernelDisableCount++;
        }

        public void setSuccessAcCount() {
            this.successActiveCount++;
            this.activeLastTime = System.currentTimeMillis();
        }

        public void setTotalDisableCount() {
            this.totalDisableCount++;
            if (this.activeLastTime > 0) {
                this.activeLastTime = 0;
                this.totalActiveTime += (System.currentTimeMillis() - this.activeLastTime) / 1000;
            }
        }
    }

    public void setFreq24gAnd24gCount() {
        this.mDualStaBuriedPoint.freq24gAnd24gCount++;
    }

    public void setFreq24gAnd5gCount() {
        this.mDualStaBuriedPoint.freq24gAnd5gCount++;
    }

    public void setFreq5gAnd24gCount() {
        this.mDualStaBuriedPoint.freq5gAnd24gCount++;
    }

    public void setFreq5gAnd5gCount() {
        this.mDualStaBuriedPoint.freq5gAnd5gCount++;
    }

    public void setManualAcCount() {
        this.mDualStaBuriedPoint.manualActiveCount++;
    }

    public void setRquestAcCount() {
        this.mDualStaBuriedPoint.requestActiveCount++;
    }

    public void setRquestDisableCount() {
        this.mDualStaBuriedPoint.requestDisableCount++;
    }

    public void setManualDisableCount() {
        this.mDualStaBuriedPoint.manualDisableCount++;
    }

    public void setScreenOffDisableCount() {
        this.mDualStaBuriedPoint.screenOffDisableCount++;
    }

    public String getFreq24gAnd24gCount() {
        int count = this.mDualStaBuriedPoint.freq24gAnd24gCount;
        this.mDualStaBuriedPoint.freq24gAnd24gCount = 0;
        return "" + count;
    }

    public String getFreq24gAnd5gCount() {
        int count = this.mDualStaBuriedPoint.freq24gAnd5gCount;
        this.mDualStaBuriedPoint.freq24gAnd5gCount = 0;
        return "" + count;
    }

    public String getFreq5gAnd24gCount() {
        int count = this.mDualStaBuriedPoint.freq5gAnd24gCount;
        this.mDualStaBuriedPoint.freq5gAnd24gCount = 0;
        return "" + count;
    }

    public String getFreq5gAnd5gCount() {
        int count = this.mDualStaBuriedPoint.freq5gAnd5gCount;
        this.mDualStaBuriedPoint.freq5gAnd5gCount = 0;
        return "" + count;
    }

    public String getTotalActiveTime() {
        long activeTime = this.mDualStaBuriedPoint.totalActiveTime;
        this.mDualStaBuriedPoint.totalActiveTime = 0;
        return "" + activeTime;
    }

    public String getRusSwitchEnableState() {
        return "" + this.mDualStaBuriedPoint.rusSwitchSate;
    }

    public String getSettingSwitchEnableState() {
        return "" + this.mDualStaBuriedPoint.settingSwitchSate;
    }

    public String getRusSwitchEnableCount() {
        int count = this.mDualStaBuriedPoint.rusSwitchEnableCount;
        this.mDualStaBuriedPoint.rusSwitchEnableCount = 0;
        return "" + count;
    }

    public String getSettingSwitchEnableCount() {
        int count = this.mDualStaBuriedPoint.settingSwitchEnableCount;
        this.mDualStaBuriedPoint.settingSwitchEnableCount = 0;
        return "" + count;
    }

    public String getDownloadActiveCount() {
        int count = this.mDualStaBuriedPoint.downloadActiveCount;
        this.mDualStaBuriedPoint.downloadActiveCount = 0;
        return "" + count;
    }

    public String getLowScoreActiveCount() {
        int count = this.mDualStaBuriedPoint.lowScoreActiveCount;
        this.mDualStaBuriedPoint.lowScoreActiveCount = 0;
        return "" + count;
    }

    public String getLowSpeedActiveCount() {
        int count = this.mDualStaBuriedPoint.lowSpeedActiveCount;
        this.mDualStaBuriedPoint.lowSpeedActiveCount = 0;
        return "" + count;
    }

    public String getRequestActiveCount() {
        int count = this.mDualStaBuriedPoint.requestActiveCount;
        this.mDualStaBuriedPoint.requestActiveCount = 0;
        return "" + count;
    }

    public String getManualActiveCount() {
        int count = this.mDualStaBuriedPoint.manualActiveCount;
        this.mDualStaBuriedPoint.manualActiveCount = 0;
        return "" + count;
    }

    public String getSuccessActiveCount() {
        int count = this.mDualStaBuriedPoint.successActiveCount;
        this.mDualStaBuriedPoint.successActiveCount = 0;
        return "" + count;
    }

    public String getFailAcCount() {
        return "" + (((((this.mDualStaBuriedPoint.downloadActiveCount + this.mDualStaBuriedPoint.lowScoreActiveCount) + this.mDualStaBuriedPoint.lowSpeedActiveCount) + this.mDualStaBuriedPoint.requestActiveCount) + this.mDualStaBuriedPoint.manualActiveCount) - this.mDualStaBuriedPoint.successActiveCount);
    }

    public String getKernelDisableCount() {
        int count = this.mDualStaBuriedPoint.kernelDisableCount;
        this.mDualStaBuriedPoint.kernelDisableCount = 0;
        return "" + count;
    }

    public String getManualDisableCount() {
        int count = this.mDualStaBuriedPoint.manualDisableCount;
        this.mDualStaBuriedPoint.manualDisableCount = 0;
        return "" + count;
    }

    public String getScreenOffDisableCount() {
        int count = this.mDualStaBuriedPoint.screenOffDisableCount;
        this.mDualStaBuriedPoint.screenOffDisableCount = 0;
        return "" + count;
    }

    public String getRequestDisableCount() {
        int count = this.mDualStaBuriedPoint.requestDisableCount;
        this.mDualStaBuriedPoint.requestDisableCount = 0;
        return "" + count;
    }

    public String getOtherDisableCount() {
        int totalCount = this.mDualStaBuriedPoint.kernelDisableCount + this.mDualStaBuriedPoint.manualDisableCount + this.mDualStaBuriedPoint.screenOffDisableCount + this.mDualStaBuriedPoint.requestDisableCount;
        this.mDualStaBuriedPoint.totalDisableCount = 0;
        return "" + (this.mDualStaBuriedPoint.totalDisableCount - totalCount);
    }

    /* access modifiers changed from: private */
    public class SlaGameParams {
        public static final int ALLIGNED_LEN = 60;
        public static final int MAX_FIXED_VALUE_LEN = 20;
        public int gameType;
        public byte[] rxFixedValue = new byte[20];
        public int rxLen;
        public int rxOffset;
        public byte[] txFixedValue = new byte[20];
        public int txLen;
        public int txOffset;

        public SlaGameParams() {
        }

        public boolean setGameParams(int gameIndex, String[] txParams, String[] rxParams) {
            this.gameType = gameIndex;
            if (txParams == null || txParams.length != 3 || rxParams == null || rxParams.length != 3) {
                Log.e(OppoSlaManager.TAG, "setGameParams failed... invalid params! txParams:" + Arrays.toString(txParams) + " rxParams:" + Arrays.toString(rxParams));
                return false;
            }
            try {
                this.txOffset = Integer.parseInt(txParams[0]);
                this.txLen = Integer.parseInt(txParams[1]);
                String tFixedValue = txParams[2];
                if (TextUtils.isEmpty(tFixedValue) || tFixedValue.length() > 40) {
                    Log.w(OppoSlaManager.TAG, "setGameParams invalid Tx fixed value:" + tFixedValue);
                }
                if (tFixedValue.length() != this.txLen * 2) {
                    Log.d(OppoSlaManager.TAG, "setGameParams invalid Tx fixed value length, len=" + this.txLen + " tFixedValue.length()=" + tFixedValue.length());
                    return false;
                }
                for (int i = 0; i + 1 < tFixedValue.length(); i += 2) {
                    this.txFixedValue[i / 2] = (byte) Integer.parseInt(tFixedValue.substring(i, i + 2), 16);
                }
                Log.d(OppoSlaManager.TAG, "setGameParams Tx fixed value:" + OppoSlaManager.bytesToHex(this.txFixedValue));
                this.rxOffset = Integer.parseInt(rxParams[0]);
                this.rxLen = Integer.parseInt(rxParams[1]);
                String rFixedValue = rxParams[2];
                if (TextUtils.isEmpty(rFixedValue) || rFixedValue.length() > 20) {
                    Log.w(OppoSlaManager.TAG, "setGameParams invalid Rx fixed value:" + rFixedValue);
                }
                if (rFixedValue.length() != this.rxLen * 2) {
                    Log.d(OppoSlaManager.TAG, "setGameParams invalid Rx fixed value length, len=" + this.rxLen + " rFixedValue.length()=" + rFixedValue.length());
                    return false;
                }
                for (int i2 = 0; i2 + 1 < rFixedValue.length(); i2 += 2) {
                    this.rxFixedValue[i2 / 2] = (byte) Integer.parseInt(rFixedValue.substring(i2, i2 + 2), 16);
                }
                Log.d(OppoSlaManager.TAG, "setGameParams Rx fixed value:" + OppoSlaManager.bytesToHex(this.rxFixedValue));
                Log.d(OppoSlaManager.TAG, "setGameParams succeeded:" + toString());
                return true;
            } catch (Exception e) {
                Log.e(OppoSlaManager.TAG, "setGameParams failed...", e);
                return false;
            }
        }

        public byte[] getGameParams() {
            byte[] params = new byte[60];
            ByteBuffer byteBuffer = ByteBuffer.wrap(params);
            byteBuffer.order(ByteOrder.nativeOrder());
            byteBuffer.putInt(this.gameType);
            byteBuffer.putInt(this.txOffset);
            byteBuffer.putInt(this.txLen);
            for (int i = 0; i < 20; i++) {
                byteBuffer.put(this.txFixedValue[i]);
            }
            byteBuffer.putInt(this.rxOffset);
            byteBuffer.putInt(this.rxLen);
            for (int i2 = 0; i2 < 20; i2++) {
                byteBuffer.put(this.rxFixedValue[i2]);
            }
            Log.d(OppoSlaManager.TAG, "getGameParams params -> " + toString() + " bytes -> " + OppoSlaManager.bytesToHex(params));
            return params;
        }

        public void pack(ByteBuffer byteBuffer) {
            byteBuffer.putInt(this.gameType);
            byteBuffer.putInt(this.txOffset);
            byteBuffer.putInt(this.txLen);
            for (int i = 0; i < 20; i++) {
                byteBuffer.put(this.txFixedValue[i]);
            }
            byteBuffer.putInt(this.rxOffset);
            byteBuffer.putInt(this.rxLen);
            for (int i2 = 0; i2 < 20; i2++) {
                byteBuffer.put(this.rxFixedValue[i2]);
            }
        }

        public String toString() {
            return "gameType:" + this.gameType + ", txOffset:" + this.txOffset + ", txLen:" + this.txLen + ", txFixedValue:" + OppoSlaManager.bytesToHex(this.txFixedValue) + ", rxOffset:" + this.rxOffset + ", rxLen:" + this.rxLen + ", rxFixedValue:" + OppoSlaManager.bytesToHex(this.rxFixedValue);
        }
    }

    /* JADX INFO: Multiple debug info for r7v7 'traffic'  long: [D('uid' int), D('traffic' long)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x026e  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x02a0  */
    private void handleUpdateGameAppStats(StructNlMsgHdr nlmsghdr, ByteBuffer bytes) {
        int gameNum;
        ArrayList<SlaGameStats> arrayList;
        Throwable th;
        String pkgName;
        long traffic;
        int realRtt;
        StringBuilder sb;
        ByteBuffer byteBuffer = bytes;
        try {
            if (nlmsghdr.nlmsg_len < (mGameAppUids.size() * 48) + 16) {
                Log.e(TAG, "handleUpdateGameAppStats invalid data len=" + nlmsghdr.nlmsg_len + " bytes=" + byteBuffer);
                return;
            }
            int gameNum2 = mGameAppUids.size();
            if (mDebug) {
                Log.d(TAG, "handleUpdateGameAppStats gameNum=" + gameNum2);
            }
            int i = 1;
            while (i < gameNum2) {
                int game_type = byteBuffer.getInt((i * 48) + 16);
                if (game_type < 0) {
                    gameNum = gameNum2;
                } else if (game_type > gameNum2) {
                    gameNum = gameNum2;
                } else {
                    int uid = byteBuffer.getInt((i * 48) + 16 + 4);
                    if (UserHandle.getAppId(uid) <= 1000) {
                        Log.w(TAG, "handleUpdateGameAppStats invalid uid:" + uid);
                        gameNum = gameNum2;
                    } else {
                        String pkgName2 = getPkgNameWithUid(uid);
                        if (TextUtils.isEmpty(pkgName2)) {
                            Log.w(TAG, "handleUpdateGameAppStats invalid pkgName, uid=" + uid);
                            gameNum = gameNum2;
                        } else {
                            int avg_rtt = byteBuffer.getInt((i * 48) + 16 + 8);
                            int rtt150Num = byteBuffer.getInt((i * 48) + 16 + 20);
                            int rtt200Num = byteBuffer.getInt((i * 48) + 16 + 24);
                            int rtt250Num = byteBuffer.getInt((i * 48) + 16 + 28);
                            long rttNormal = byteBuffer.getLong((i * 48) + 16 + 32);
                            long traffic2 = byteBuffer.getLong((i * 48) + 16 + 40);
                            ArrayList<SlaGameStats> arrayList2 = this.mSlaGameStats;
                            synchronized (arrayList2) {
                                try {
                                    SlaGameStats gameStats = this.mSlaGameStats.get(game_type);
                                    if (gameStats != null) {
                                        try {
                                            if (gameStats.game_type > 0) {
                                                if (gameStats.game_type == 1) {
                                                    try {
                                                        realRtt = avg_rtt / 4;
                                                    } catch (Throwable th2) {
                                                        th = th2;
                                                        arrayList = arrayList2;
                                                    }
                                                } else if (gameStats.game_type == 2) {
                                                    realRtt = avg_rtt / 8;
                                                } else {
                                                    realRtt = 0;
                                                }
                                                gameNum = gameNum2;
                                                try {
                                                    sb = new StringBuilder();
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                    arrayList = arrayList2;
                                                    while (true) {
                                                        try {
                                                            break;
                                                        } catch (Throwable th4) {
                                                            th = th4;
                                                        }
                                                    }
                                                    throw th;
                                                }
                                                try {
                                                    sb.append("handleUpdateGameAppStats game_type=");
                                                    sb.append(game_type);
                                                    sb.append(" uid=");
                                                    sb.append(uid);
                                                    sb.append(" pkgName=");
                                                    sb.append(pkgName2);
                                                    sb.append(" avg_rtt=");
                                                    sb.append(realRtt);
                                                    sb.append(" rttNormal=");
                                                    sb.append(rttNormal);
                                                    sb.append(" rtt150Num=");
                                                    sb.append(rtt150Num);
                                                    sb.append(" rtt200Num=");
                                                    sb.append(rtt200Num);
                                                    sb.append(" rtt250Num=");
                                                    sb.append(rtt250Num);
                                                    sb.append(" traffic=");
                                                    pkgName = pkgName2;
                                                    traffic = traffic2;
                                                    try {
                                                        sb.append(traffic);
                                                        Log.d(TAG, sb.toString());
                                                        arrayList = arrayList2;
                                                    } catch (Throwable th5) {
                                                        th = th5;
                                                        arrayList = arrayList2;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                    try {
                                                        gameStats.updateRttStats(realRtt, rttNormal, rtt150Num, rtt200Num, rtt250Num);
                                                        if (mDebug) {
                                                            Log.d(TAG, "handleUpdateGameAppStats after updated RTT:" + gameStats);
                                                        }
                                                        setGameStats(game_type, gameStats.toString());
                                                        if (mDebug) {
                                                            Log.d(TAG, "handleUpdateGameAppStats after setGameStats:" + getGameStats(game_type));
                                                        }
                                                        if (traffic <= 0) {
                                                            long sum = getAppTraffic(pkgName) + traffic;
                                                            setAppTraffic(pkgName, sum);
                                                            if (mDebug) {
                                                                Log.d(TAG, "handleUpdateGameAppStats setAppTraffic(" + pkgName + ", " + sum + ")");
                                                            }
                                                        } else {
                                                            Log.e(TAG, "handleUpdateGameAppStats invalid traffic!! pkgName=" + pkgName + ", traffic=" + traffic);
                                                        }
                                                    } catch (Throwable th6) {
                                                        th = th6;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                } catch (Throwable th7) {
                                                    th = th7;
                                                    arrayList = arrayList2;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            }
                                        } catch (Throwable th8) {
                                            th = th8;
                                            arrayList = arrayList2;
                                            while (true) {
                                                break;
                                            }
                                            throw th;
                                        }
                                    }
                                    gameNum = gameNum2;
                                    pkgName = pkgName2;
                                    traffic = traffic2;
                                    arrayList = arrayList2;
                                    try {
                                        Log.w(TAG, "handleUpdateGameAppStats error!!! gameStats=" + gameStats);
                                        if (traffic <= 0) {
                                        }
                                    } catch (Throwable th9) {
                                        th = th9;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th10) {
                                    th = th10;
                                    arrayList = arrayList2;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            }
                        }
                    }
                    i++;
                    byteBuffer = bytes;
                    gameNum2 = gameNum;
                }
                Log.w(TAG, "handleUpdateGameAppStats game type:" + game_type);
                i++;
                byteBuffer = bytes;
                gameNum2 = gameNum;
            }
            Log.d(TAG, "handleUpdateGameAppStats successful..");
        } catch (Exception e) {
            Log.e(TAG, "Exception when handleUpdateGameAppStats:" + e);
        }
    }

    private long getAppTraffic(String pkgName) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        return Settings.Global.getLong(contentResolver, APP_TRAFFIC_PREFIX + pkgName, 0);
    }

    private void setAppTraffic(String pkgName, long traffic) {
        if (traffic < 0) {
            Log.e(TAG, "setAppTraffic invalid traffic:" + traffic + " pkgName=" + pkgName);
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Global.putLong(contentResolver, APP_TRAFFIC_PREFIX + pkgName, traffic);
    }

    private long getAppNormalTraffic(String pkgName) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        return Settings.Global.getLong(contentResolver, APP_NORMAL_TRAFFIC_PREFIX + pkgName, 0);
    }

    private void setAppNormalTraffic(String pkgName, long traffic) {
        if (traffic < 0) {
            Log.e(TAG, "setAppNormalTraffic invalid traffic:" + traffic + " pkgName=" + pkgName);
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Global.putLong(contentResolver, APP_NORMAL_TRAFFIC_PREFIX + pkgName, traffic);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearSlaTrafficStats() {
        Log.d(TAG, "clearSlaTrafficStats()");
        String[] whiteListApps = getSlaWhiteListApps();
        if (whiteListApps != null && whiteListApps.length > 0) {
            for (int i = 0; i < whiteListApps.length; i++) {
                setAppTraffic(whiteListApps[i], 0);
                setAppNormalTraffic(whiteListApps[i], 0);
            }
        }
        String[] gameApps = getSlaGameApps();
        if (gameApps != null && gameApps.length > 0) {
            for (int i2 = 1; i2 < gameApps.length; i2++) {
                setAppTraffic(gameApps[i2], 0);
            }
        }
    }

    private String getGameStats(int gameIndex) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        String stats = Settings.Global.getString(contentResolver, GAME_STATS_PREFIX + gameIndex);
        if (TextUtils.isEmpty(stats)) {
            return new SlaGameStats(gameIndex).toString();
        }
        return stats;
    }

    private void setGameStats(int gameIndex, String stats) {
        if (gameIndex < 0) {
            Log.e(TAG, "setGameStats invalid gameIndex:" + gameIndex);
            return;
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Global.putString(contentResolver, GAME_STATS_PREFIX + gameIndex, stats);
    }

    private void clearRttAndSpeedRecords() {
        synchronized (this.mRttSpeedLock) {
            this.mWifiRttRecord.clear();
            this.mWifiSpeedRecord.clear();
            this.mWifiRttRecord2.clear();
            this.mWifiSpeedRecord2.clear();
            this.mCellRttRecord.clear();
            this.mCellSpeedRecord.clear();
            this.mRecordIndex = -1;
        }
    }

    public int[] getRttAndSpeed(int count) {
        int c = count;
        if (count > 60) {
            if (mDebug) {
                Log.w(TAG, "getRttAndSpeed count too large:" + count);
            }
            c = 60;
        } else if (count > this.mWifiSpeedRecord.size()) {
            if (!mDebug) {
                return null;
            }
            Log.w(TAG, "getRttAndSpeed count > mWifiSpeedRecord.size()");
            return null;
        }
        int[] result = new int[(c * 6)];
        synchronized (this.mRttSpeedLock) {
            int index = this.mRecordIndex;
            for (int i = 0; i < c; i++) {
                result[i * 6] = this.mWifiSpeedRecord.get(index).intValue();
                result[(i * 6) + 1] = this.mWifiRttRecord.get(index).intValue();
                result[(i * 6) + 2] = this.mWifiSpeedRecord2.get(index).intValue();
                result[(i * 6) + 3] = this.mWifiRttRecord2.get(index).intValue();
                result[(i * 6) + 4] = this.mCellSpeedRecord.get(index).intValue();
                result[(i * 6) + 5] = this.mCellRttRecord.get(index).intValue();
                index = ((index - 1) + 60) % 60;
            }
        }
        if (mDebug) {
            Log.w("OppoSlaManager_RTT", "getRttAndSpeed(" + c + "):" + Arrays.toString(result));
        }
        return result;
    }

    public boolean setSlaAppState(String pkgName, boolean enabled) {
        if (TextUtils.isEmpty(pkgName)) {
            Log.e(TAG, "setSlaAppState invalid pkgName:" + pkgName);
            return false;
        }
        String[] slaApps = getAllSlaAppsAndStates();
        if (slaApps == null || slaApps.length == 0) {
            Log.e(TAG, "setSlaAppState NO SLA app configured!");
            return false;
        }
        ArrayList<String> appList = new ArrayList<>(Arrays.asList(slaApps));
        int uid = this.mPMS.getPackageUid(pkgName, 65536, this.mUserId);
        if (UserHandle.getAppId(uid) <= 1000 || !appList.contains(pkgName)) {
            Log.e(TAG, "setSlaAppState invalid uid or pkgName:" + pkgName + " uid:" + uid);
            return false;
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.System.putInt(contentResolver, SLA_APP_STATE_PREFIX + pkgName, enabled ? 1 : 0);
        this.mAsyncHandler.sendEmptyMessage(5);
        if (!mDebug) {
            return true;
        }
        Log.d(TAG, "setSlaAppState(" + pkgName + ") ->" + enabled);
        return true;
    }

    public boolean getSlaAppState(String pkgName) {
        boolean enabled = true;
        boolean booleanValue = this.mWifiRomUpdateHelper.getBooleanValue("NETWORK_SLA_APPS_DEFAULT_STATE", true);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        if (Settings.System.getInt(contentResolver, SLA_APP_STATE_PREFIX + pkgName, booleanValue ? 1 : 0) != 1) {
            enabled = false;
        }
        if (mDebug) {
            Log.d(TAG, "getSlaAppState(" + pkgName + ") ->" + enabled);
        }
        return enabled;
    }

    public String[] getAllSlaAppsAndStates() {
        String[] whiteListApps = getSlaWhiteListApps();
        String[] gameApps = getSlaGameApps();
        String states = "";
        ArrayList<String> result = new ArrayList<>();
        if (whiteListApps != null && whiteListApps.length > 0) {
            for (int i = 0; i < whiteListApps.length; i++) {
                result.add(whiteListApps[i]);
                boolean appState = getSlaAppState(whiteListApps[i]);
                StringBuilder sb = new StringBuilder();
                sb.append(states);
                sb.append(appState ? "1" : "0");
                states = sb.toString();
            }
        }
        if (gameApps != null && gameApps.length > 0) {
            for (int i2 = 0; i2 < gameApps.length; i2++) {
                if (!"not.defined".equals(gameApps[i2])) {
                    result.add(gameApps[i2]);
                    boolean appState2 = getSlaAppState(gameApps[i2]);
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(states);
                    sb2.append(appState2 ? "1" : "0");
                    states = sb2.toString();
                }
            }
        }
        if (!result.isEmpty()) {
            result.add(states);
        }
        if (mDebug) {
            Log.d(TAG, "getAllSlaAppsAndStates:" + Arrays.toString(result.toArray()));
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public String[] getAllSlaAcceleratedApps() {
        if (mDebug) {
            Log.d(TAG, "getAllSlaAcceleratedApps");
        }
        ArrayList<String> apps = new ArrayList<>();
        synchronized (mWhiteListAppUids) {
            Iterator<Integer> it = mWhiteListAppUids.iterator();
            while (it.hasNext()) {
                String pkgName = getPkgNameWithUid(it.next().intValue());
                if (TextUtils.isEmpty(pkgName)) {
                    Log.w(TAG, "getAllSlaAcceleratedApps invalid pkgName!!");
                } else if (getAppTraffic(pkgName) > 0) {
                    apps.add(pkgName);
                }
            }
        }
        synchronized (mGameAppUids) {
            Iterator<Integer> it2 = mGameAppUids.iterator();
            while (it2.hasNext()) {
                Integer uid = it2.next();
                String pkgName2 = getPkgNameWithUid(uid.intValue());
                if (TextUtils.isEmpty(pkgName2)) {
                    Log.w(TAG, "getAllSlaAcceleratedApps invalid pkgName, uid=" + uid);
                } else if (getAppTraffic(pkgName2) > 0 && uid.intValue() > 0) {
                    apps.add(pkgName2);
                }
            }
        }
        if (mDebug) {
            Log.d(TAG, "getAllSlaAcceleratedApps:" + Arrays.toString(apps.toArray()));
        }
        if (apps.size() > 0) {
            return (String[]) apps.toArray(new String[apps.size()]);
        }
        return null;
    }

    public boolean isSlaSupported() {
        if (TextUtils.isEmpty(this.mMCC)) {
            Log.d(TAG, "isSlaSupported mMCC empty!");
            return false;
        }
        String[] slaEnabledMCC = this.mWifiRomUpdateHelper.getSlaEnabledMcc();
        if (slaEnabledMCC == null || slaEnabledMCC.length == 0) {
            Log.w(TAG, "isSlaSupported NO MCC configured...");
            return false;
        }
        for (String mccFromXml : slaEnabledMCC) {
            if (mccFromXml != null && mccFromXml.contains(this.mMCC)) {
                if (!mDebug) {
                    return true;
                } else {
                    Log.d(TAG, "isSlaSupported found matched MCC from xml:" + mccFromXml);
                    return true;
                }
            }
        }
        if (mDebug) {
            Log.d(TAG, "isSlaSupported unsupported MCC:" + this.mMCC);
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showMyToast(String text, int cnt) {
        if (SystemProperties.getBoolean("sys.sla.debug.show_toast", false)) {
            final Toast toast = Toast.makeText(this.mContext, text, 1);
            new Timer().schedule(new TimerTask() {
                /* class com.android.server.wifi.OppoSlaManager.AnonymousClass6 */

                public void run() {
                    toast.show();
                }
            }, 0);
            new Timer().schedule(new TimerTask() {
                /* class com.android.server.wifi.OppoSlaManager.AnonymousClass7 */

                public void run() {
                    toast.cancel();
                }
            }, (long) cnt);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] buf = new char[(bytes.length * 2)];
        int index = 0;
        for (byte b : bytes) {
            int index2 = index + 1;
            buf[index] = HEX_CHAR[(b >>> 4) & 15];
            index = index2 + 1;
            buf[index2] = HEX_CHAR[b & 15];
        }
        return new String(buf);
    }

    private int getTopPackageUid(ComponentName com2) {
        if (com2 == null) {
            return -1;
        }
        return this.mPMS.getPackageUid(com2.getPackageName(), 65536, this.mUserId);
    }

    private ComponentName getTopActivity() {
        ActivityManager.RunningTaskInfo taskInfo;
        List<ActivityManager.RunningTaskInfo> taskInfoList = this.mActivityManager.getRunningTasks(1);
        if (taskInfoList == null || taskInfoList.isEmpty() || (taskInfo = taskInfoList.get(0)) == null) {
            return null;
        }
        return taskInfo.topActivity;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void destroyTopAppSockets() {
        int uid = getTopPackageUid(getTopActivity());
        if (UserHandle.getAppId(uid) <= 1000) {
            return;
        }
        if (isSkipDestroySocketApp(uid)) {
            if (mDebug) {
                Log.d(TAG, "Do not destroy uid [ " + uid + "] sockets");
            }
        } else if (!isDownloadApp(uid) || this.mDualStaActiveType != 0) {
            if (!isVideoApp(uid) || 4 != this.mDualStaActiveType) {
                try {
                    this.mSlaNetd.destroySockets(uid, 0);
                } catch (Exception e) {
                    Log.d(TAG, "Exception OPPO_SLA_ENABLED CLEAR TOP UID: " + e);
                }
            } else if (mDebug) {
                Log.d(TAG, "mDualStaActiveType download,avoid destroy vedio socket");
            }
        } else if (mDebug) {
            Log.d(TAG, "mDualStaActiveType manual,avoid destroy download socket");
        }
    }

    private void destroyGameAppSockets(int socketType) {
        int uid = getTopPackageUid(getTopActivity());
        if (UserHandle.getAppId(uid) > 1000 && socketType >= 0 && socketType <= 2 && isGameApp(uid)) {
            try {
                Log.d(TAG, "destroyGameAppSockets(" + uid + ", " + socketType + ")");
                this.mSlaNetd.destroySockets(uid, socketType);
            } catch (Exception e) {
                Log.w(TAG, "Exception destroyGameAppSockets: " + e);
            }
        }
    }

    private boolean isWhiteListApp(int uid) {
        boolean contains;
        synchronized (mWhiteListAppUids) {
            contains = mWhiteListAppUids.contains(new Integer(uid));
        }
        return contains;
    }

    private boolean isGameApp(int uid) {
        boolean contains;
        synchronized (mGameAppUids) {
            contains = mGameAppUids.contains(new Integer(uid));
        }
        return contains;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSlaAppOnFocus() {
        int uid = getTopPackageUid(getTopActivity());
        if (UserHandle.getAppId(uid) <= 1000) {
            return false;
        }
        boolean isWhiteListApp = isWhiteListApp(uid);
        boolean isGameApp = isGameApp(uid);
        if (isWhiteListApp || isGameApp) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isWhiteListAppOnFocus() {
        int uid = getTopPackageUid(getTopActivity());
        if (UserHandle.getAppId(uid) <= 1000 || !isWhiteListApp(uid)) {
            return false;
        }
        return true;
    }

    public String getSlaAppsTraffic() {
        String[] whiteListApps = getSlaWhiteListApps();
        String[] gameApps = getSlaGameApps();
        ArrayList<String> allAppTraffic = new ArrayList<>();
        if (whiteListApps != null && whiteListApps.length > 0) {
            allAppTraffic.add("WhiteListApps SLA:");
            for (int i = 0; i < whiteListApps.length; i++) {
                allAppTraffic.add("" + getAppTraffic(whiteListApps[i]));
            }
        }
        if (whiteListApps != null && whiteListApps.length > 0) {
            allAppTraffic.add("WhiteListApps Normal:");
            for (int i2 = 0; i2 < whiteListApps.length; i2++) {
                allAppTraffic.add("" + getAppNormalTraffic(whiteListApps[i2]));
            }
        }
        if (gameApps != null && gameApps.length > 0) {
            allAppTraffic.add("GameApps:");
            for (int i3 = 1; i3 < gameApps.length; i3++) {
                allAppTraffic.add("" + getAppTraffic(gameApps[i3]));
            }
        }
        if (mDebug) {
            Log.d(TAG, "getSlaAppsTraffic:" + Arrays.toString(allAppTraffic.toArray()));
        }
        clearSlaTrafficStats();
        if (allAppTraffic.size() <= 0) {
            return null;
        }
        return "SlaAppTraffic:" + Arrays.toString(allAppTraffic.toArray());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSpeedAndRtt(int wifiSpeed, int wifiRtt, int wifiSpeed2, int wifiRtt2, int cellSpeed, int cellRtt) {
        if ((wifiSpeed > 0 && wifiRtt > 0) || (cellSpeed > 0 && cellRtt > 0)) {
            this.mSpeedRttIndex++;
            if (this.mSpeedRttIndex == 0) {
                clearSpeedAndRtt();
                this.mSpeedRttIndex++;
            }
            if (wifiSpeed > 0) {
                this.mWifiSumSpeed += wifiSpeed;
                this.mWifiAvgSpeed = this.mWifiSumSpeed / this.mSpeedRttIndex;
            }
            if (wifiRtt > 0) {
                this.mWifiSumRtt += wifiRtt;
                this.mWifiAvgRtt = this.mWifiSumRtt / this.mSpeedRttIndex;
                int i = this.mLastWifiRtt;
                if (i == 0 || i != wifiRtt) {
                    if (wifiRtt > 200) {
                        this.mWifiRtt200plus++;
                    } else if (wifiRtt > 100) {
                        this.mWifiRtt100_200++;
                    } else {
                        this.mWifiRtt0_100++;
                    }
                    if (wifiSpeed < 100 && wifiRtt > 250) {
                        this.mWifiCongestCount++;
                    }
                }
                this.mLastWifiRtt = wifiRtt;
            }
            if (cellSpeed > 0) {
                this.mCellSumSpeed += cellSpeed;
                this.mCellAvgSpeed = this.mCellSumSpeed / this.mSpeedRttIndex;
            }
            if (cellRtt > 0) {
                this.mCellSumRtt += cellRtt;
                this.mCellAvgRtt = this.mCellSumRtt / this.mSpeedRttIndex;
                int i2 = this.mLastCellRtt;
                if (i2 == 0 || i2 != cellRtt) {
                    if (cellRtt > 200) {
                        this.mCellRtt200plus++;
                    } else if (cellRtt > 100) {
                        this.mCellRtt100_200++;
                    } else {
                        this.mCellRtt0_100++;
                    }
                    if (cellSpeed < 100 && cellRtt > 250) {
                        this.mCellCongestCount++;
                    }
                }
                this.mLastCellRtt = cellRtt;
            }
        }
    }

    public String getAvgSpeedAndRtt() {
        String result = "Speed-Rtt:WiFi[" + this.mWifiAvgSpeed + "," + this.mWifiAvgRtt + "," + this.mWifiRtt0_100 + "," + this.mWifiRtt100_200 + "," + this.mWifiRtt200plus + "," + this.mWifiCongestCount + "] Cell[" + this.mCellAvgSpeed + "," + this.mCellAvgRtt + "," + this.mCellRtt0_100 + "," + this.mCellRtt100_200 + "," + this.mCellRtt200plus + "," + this.mCellCongestCount + "]";
        clearSpeedAndRtt();
        return result;
    }

    private void clearSpeedAndRtt() {
        synchronized (this.mRttSpeedLock) {
            this.mWifiSumSpeed = 0;
            this.mWifiAvgSpeed = 0;
            this.mWifiSumRtt = 0;
            this.mWifiAvgRtt = 0;
            this.mCellSumSpeed = 0;
            this.mCellAvgSpeed = 0;
            this.mCellSumRtt = 0;
            this.mCellAvgRtt = 0;
            this.mSpeedRttIndex = 0;
            this.mLastWifiRtt = 0;
            this.mLastCellRtt = 0;
            this.mWifiRtt0_100 = 0;
            this.mWifiRtt100_200 = 0;
            this.mWifiRtt200plus = 0;
            this.mCellRtt0_100 = 0;
            this.mCellRtt100_200 = 0;
            this.mCellRtt200plus = 0;
            this.mWifiCongestCount = 0;
            this.mCellCongestCount = 0;
        }
    }

    public String getAllGameStats() {
        StringBuilder result = new StringBuilder();
        result.append("GameStats:[");
        synchronized (this.mSlaGameStats) {
            int count = this.mSlaGameStats.size();
            for (int i = 1; i < count; i++) {
                SlaGameStats gameStats = this.mSlaGameStats.get(i);
                if (gameStats != null && gameStats.game_type > 0) {
                    result.append(gameStats.toString() + NAIRealmData.NAI_REALM_STRING_SEPARATOR);
                }
            }
            this.mSlaGameStats.clear();
            for (int i2 = 0; i2 < count; i2++) {
                this.mSlaGameStats.add(i2, new SlaGameStats(i2));
                setGameStats(i2, this.mSlaGameStats.get(i2).toString());
            }
        }
        result.append("]");
        return result.toString();
    }

    public void notifyDefaultNetwork(boolean defaultCell) {
        this.mAsyncHandler.post(new Runnable(defaultCell ? 2 : 0) {
            /* class com.android.server.wifi.$$Lambda$OppoSlaManager$6SfphePTvxlRltQPMQGBpNebdA */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                OppoSlaManager.this.lambda$notifyDefaultNetwork$0$OppoSlaManager(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$notifyDefaultNetwork$0$OppoSlaManager(int defaultNetwork) {
        boolean result = sendToKernel(OPPO_SLA_NOTIFY_DEFAULT_NETWORK, defaultNetwork);
        Log.d(TAG, "notifyDefaultNetwork -> " + defaultNetwork + ", result=" + result);
    }

    public void notifyPrimaryWifi(String ifaceName) {
        int primaryWifiIndex;
        if (!TextUtils.isEmpty(ifaceName)) {
            if (OppoWifiAssistantUtils.IFACE_NAME_WLAN0.equals(ifaceName)) {
                primaryWifiIndex = 0;
            } else {
                primaryWifiIndex = 1;
            }
            this.mAsyncHandler.post(new Runnable(primaryWifiIndex) {
                /* class com.android.server.wifi.$$Lambda$OppoSlaManager$xZM8cjl0iLtdPzJZ8B8DvO2J5Cs */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    OppoSlaManager.this.lambda$notifyPrimaryWifi$1$OppoSlaManager(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$notifyPrimaryWifi$1$OppoSlaManager(int primaryWifiIndex) {
        boolean result = sendToKernel(OPPO_SLA_NOTIFY_PRIMARY_WIFI, primaryWifiIndex);
        Log.d(TAG, "notifyPrimaryWifi -> " + primaryWifiIndex + ", result=" + result);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendDualStaAppUid() {
        ArrayList<String> blackList;
        if (mNlfd == null) {
            Log.w(TAG, "sendDualStaAppUid mNlSock == null!");
            return;
        }
        boolean result = false;
        if (this.mPMS != null) {
            int index = 1;
            String[] whiteListApps = getAllDualStaApps();
            if (whiteListApps == null || whiteListApps.length == 0) {
                Log.w(TAG, "sendDualStaAppUid no white list app found...");
                return;
            }
            String[] blackListApps = this.mWifiRomUpdateHelper.getDualStaBlackListApps();
            if (blackListApps == null || blackListApps.length == 0) {
                blackList = null;
            } else {
                blackList = new ArrayList<>(Arrays.asList(blackListApps));
            }
            int[] data = new int[(whiteListApps.length + 1)];
            synchronized (mDualStaApps) {
                mDualStaApps.clear();
                for (int i = 0; i < whiteListApps.length; i++) {
                    int uid = this.mPMS.getPackageUid(whiteListApps[i], 65536, 0);
                    if (uid <= 1000) {
                        if (mDebug) {
                            Log.w(TAG, "sendDualStaAppUid invalid uid for app:" + whiteListApps[i]);
                        }
                    } else if (blackList == null || !blackList.contains(whiteListApps[i])) {
                        mDualStaApps.add(Integer.valueOf(uid));
                        data[index] = uid;
                        if (mDebug) {
                            Log.d(TAG, "sendDualStaAppUid data[" + index + "]=" + uid);
                        }
                        index++;
                    } else if (mDebug) {
                        Log.w(TAG, "sendDualStaAppUid black list app:" + whiteListApps[i]);
                    }
                }
            }
            data[0] = index - 1;
            result = sendToKernel(OPPO_SLA_NOTIFY_DUAL_STA_APP, data);
        }
        Log.d(TAG, "After sendDualStaAppUid:result=" + result);
    }

    public boolean isDualStaSupported() {
        String[] dualstaDisabledMcc = this.mWifiRomUpdateHelper.getDualStaDisabledMcc();
        if (TextUtils.isEmpty(this.mMCC)) {
            return false;
        }
        if (dualstaDisabledMcc != null && dualstaDisabledMcc.length > 0) {
            for (String mccFromXml : dualstaDisabledMcc) {
                if (mccFromXml != null && mccFromXml.length() > 0 && !mccFromXml.contains("-") && mccFromXml.contains(this.mMCC)) {
                    return false;
                }
                if (mccFromXml != null && mccFromXml.length() > 0 && mccFromXml.contains("-")) {
                    String[] DisabledMccRange = mccFromXml.split("-");
                    if (DisabledMccRange.length >= 2) {
                        Integer lowofRange = Integer.valueOf(DisabledMccRange[0]);
                        Integer highofRange = Integer.valueOf(DisabledMccRange[1]);
                        Integer intMcc = Integer.valueOf(this.mMCC);
                        if (intMcc.intValue() <= highofRange.intValue() && intMcc.intValue() >= lowofRange.intValue()) {
                            return false;
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        return this.mContext.getPackageManager().hasSystemFeature("oppo.wlan.dualsta.support");
    }

    public boolean isMainWifiGoodEnough() {
        return this.mMainWifiGood;
    }

    public String[] getAllDualStaApps() {
        if (!isDualStaSupported()) {
            if (!mDebug) {
                return null;
            }
            Log.d(TAG, "getAllDualStaApps isDualStaSupported() == false.");
            return null;
        } else if (SLA_MCC_CHINA.equals(this.mMCC)) {
            String[] whiteListApps = this.mWifiRomUpdateHelper.getDualStaWhiteListApps();
            if (mDebug) {
                Log.d(TAG, "curMCC=" + this.mMCC + "Dual STA whiteList:" + Arrays.toString(whiteListApps));
            }
            return whiteListApps;
        } else {
            String[] whiteListApps2 = this.mWifiRomUpdateHelper.getDualStaWhiteListAppsExp();
            if (mDebug) {
                Log.d(TAG, "curMCC=" + this.mMCC + "Dual STA whiteListExp:" + Arrays.toString(whiteListApps2));
            }
            return whiteListApps2;
        }
    }

    private boolean isDualStaApp(int uid) {
        boolean contains;
        synchronized (mDualStaApps) {
            contains = mDualStaApps.contains(new Integer(uid));
        }
        return contains;
    }

    private boolean isVideoApp(int uid) {
        boolean contains;
        synchronized (mVideoApps) {
            contains = mVideoApps.contains(new Integer(uid));
        }
        return contains;
    }

    private boolean isDownloadApp(int uid) {
        boolean contains;
        synchronized (mDownloadApps) {
            contains = mDownloadApps.contains(new Integer(uid));
        }
        return contains;
    }

    private void updateSkipDestroySocketList() {
        if (this.mPMS != null) {
            String[] skipDestroySocketsApps = this.mWifiRomUpdateHelper.getSkipDestroySocketApps();
            if (skipDestroySocketsApps == null || skipDestroySocketsApps.length == 0) {
                Log.w(TAG, "updateSkipDestroySocketList no skipDestroySockets app found...");
                return;
            }
            synchronized (mSkipDestroySocketApps) {
                mSkipDestroySocketApps.clear();
                for (int i = 0; i < skipDestroySocketsApps.length; i++) {
                    int uid = this.mPMS.getPackageUid(skipDestroySocketsApps[i], 65536, 0);
                    if (uid > 1000) {
                        mSkipDestroySocketApps.add(Integer.valueOf(uid));
                        if (mDebug) {
                            Log.d(TAG, "updateSkipDestroySocketList ptkName:" + skipDestroySocketsApps[i] + " ,uid:" + uid);
                        }
                    } else if (mDebug) {
                        Log.w(TAG, "updateSkipDestroySocketList invalid uid for app:" + skipDestroySocketsApps[i]);
                    }
                }
            }
        }
    }

    private boolean isSkipDestroySocketApp(int uid) {
        boolean contains;
        synchronized (mSkipDestroySocketApps) {
            contains = mSkipDestroySocketApps.contains(new Integer(uid));
        }
        return contains;
    }

    public boolean isSystemUidAppOnFocus() {
        if (getTopPackageUid(getTopActivity()) == 1000) {
            return true;
        }
        return false;
    }

    public boolean isDualStaAppOnFocus() {
        int uid = getTopPackageUid(getTopActivity());
        if (uid > 1000) {
            return isDualStaApp(uid);
        }
        return false;
    }

    public boolean isVideoAppOnFocus() {
        int uid = getTopPackageUid(getTopActivity());
        if (uid > 1000) {
            return isVideoApp(uid);
        }
        return false;
    }

    public boolean isDownloadAppOnFocus() {
        int uid = getTopPackageUid(getTopActivity());
        if (uid > 1000) {
            return isDownloadApp(uid);
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getDualStaEnableState() {
        boolean settingsEnabled = Settings.Global.getInt(this.mContext.getContentResolver(), "dual_sta_switch_on", 1) == 1;
        boolean rusEnabled = this.mWifiRomUpdateHelper.getBooleanValue("NETWORK_DUAL_STA_ENABLED", true);
        boolean dualStaSupported = isDualStaSupported();
        this.mDualStaBuriedPoint.setSwitchSate(settingsEnabled, rusEnabled);
        return dualStaSupported && settingsEnabled && rusEnabled;
    }

    public void enableSlaByWlanAssistant() {
        if (!this.mSlaEnabled) {
            Log.d(TAG, "Sla Not Runing, Need enableSlaByWlanAssistant");
            this.mAsyncHandler.sendEmptyMessage(4);
            this.mDestoryScoket = false;
            return;
        }
        Log.d(TAG, "Sla Runing, Don't enableSlaByWlanAssistant");
        sendToKernel(OPPO_SLA_WEIGHT_BY_WLAN_ASSIST, 0);
    }

    public boolean getSlaEnableStateByWlanAssistant() {
        return getSlaEnableState() && isWhiteListAppOnFocus();
    }

    public boolean isSlaRuning() {
        return this.mSlaEnabled;
    }

    public boolean isNotUpdateRecordBySla() {
        return this.mSlaEnabled && isSlaAppOnFocus();
    }

    private void registerKernelLogging() {
        if (this.mAsyncHandler != null) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(SIMSETTING_LOG_SWITCH_DB), false, new ContentObserver(this.mAsyncHandler) {
                /* class com.android.server.wifi.OppoSlaManager.AnonymousClass8 */

                public void onChange(boolean selfChange) {
                    int i = 0;
                    int mVerbose = Settings.System.getInt(OppoSlaManager.this.mContext.getContentResolver(), OppoSlaManager.SIMSETTING_LOG_SWITCH_DB, 0);
                    OppoSlaManager oppoSlaManager = OppoSlaManager.this;
                    if (mVerbose > 0) {
                        i = 1;
                    }
                    oppoSlaManager.sendToKernel((OppoSlaManager) OppoSlaManager.OPPO_SLA_SET_DEBUG, (short) i);
                    Log.d(OppoSlaManager.TAG, "onChange mVerbose= " + mVerbose);
                }
            });
        }
    }

    public void maybeEnableDualSta() {
        if (!isDualStaSupported()) {
            Log.d(TAG, "MCC disallow dualsta");
            return;
        }
        boolean switchState = getDualStaEnableState();
        Log.d(TAG, "maybeEnableDualSta switchState:" + switchState + " mDualStaEnabled:" + this.mDualStaEnabled + " mWifiConnected:" + this.mWifiConnected + " mWifi2Connected:" + this.mWifi2Connected);
        if (switchState && !this.mDualStaEnabled) {
            if (this.mWifiConnected || this.mWifi2Connected) {
                int result = OppoWifiAssistantUtils.getInstance(this.mContext).enableDualSta(false);
                Log.d(TAG, "After enableDualSta, result=" + result);
            }
        }
    }

    public void enableVerboseLogging(int verbose) {
        int i = 1;
        if (verbose > 0) {
            mDebug = true;
        } else {
            mDebug = false;
        }
        if (1 == Settings.System.getInt(this.mContext.getContentResolver(), SIMSETTING_LOG_SWITCH_DB, 0)) {
            if (verbose <= 0) {
                i = 0;
            }
            sendToKernel(OPPO_SLA_SET_DEBUG, i);
        }
    }

    public void setShowDialog(boolean showDialog) {
        if (this.mShowDialog != showDialog) {
            this.mShowDialog = showDialog;
            Log.d(TAG, "setShowDialog(" + this.mShowDialog + ")");
            this.mAsyncHandler.sendEmptyMessage(13);
        }
    }

    public long getWlanTcpTxPackets(int ifaceIndex) {
        if (ifaceIndex == 0) {
            return this.mWlan0TxPkts;
        }
        if (ifaceIndex == 1) {
            return this.mWlan1TxPkts;
        }
        Log.e(TAG, "getWlanTcpTxPackets invalid ifaceIndex:" + ifaceIndex);
        return -1;
    }

    public long getWlanTcpRxPackets(int ifaceIndex) {
        if (ifaceIndex == 0) {
            return this.mWlan0RxPkts;
        }
        if (ifaceIndex == 1) {
            return this.mWlan1RxPkts;
        }
        Log.e(TAG, "getWlanTcpRxPackets invalid ifaceIndex:" + ifaceIndex);
        return -1;
    }
}

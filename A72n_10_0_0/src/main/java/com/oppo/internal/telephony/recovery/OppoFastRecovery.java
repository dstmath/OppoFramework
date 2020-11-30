package com.oppo.internal.telephony.recovery;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.IIpConnectivityMetrics;
import android.net.INetdEventCallback;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityLte;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.AbstractServiceStateTracker;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DataServiceManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.telephony.util.ReflectionHelper;
import com.android.server.net.BaseNetdEventCallback;
import com.oppo.internal.telephony.OppoNewNitzStateMachine;
import com.oppo.internal.telephony.dataconnection.OppoDataCommonUtils;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseService;
import com.oppo.internal.telephony.nwdiagnose.OppoPhoneStateMonitor;
import com.oppo.internal.telephony.recovery.OppoDorecoveryStatistics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class OppoFastRecovery {
    private static final int CODE_DNS_TIMEOUT = 255;
    private static final boolean DEBUG_TOAST_DEFAULT = false;
    private static final String DEFAULT_DEBUG_CONFIG = "0,0,0,0,0,0,1,0,0";
    private static final String EXIT_RECOVERY_REASON_AIR_PLANE_ON = "air-plane-on";
    private static final String EXIT_RECOVERY_REASON_CALL_IN = "call-in";
    private static final String EXIT_RECOVERY_REASON_DATAENABLE_OFF = "data_enable_off";
    private static final String EXIT_RECOVERY_REASON_DDS_CHANGE = "dds-change";
    private static final String EXIT_RECOVERY_REASON_FEATURE_DISABLE = "feature-disable";
    private static final String EXIT_RECOVERY_REASON_NETWORK_ERCOVER = "network-recover";
    private static final String EXIT_RECOVERY_REASON_SCREEN_OFF = "screen-off";
    private static final String EXIT_RECOVERY_REASON_TIMEOUT = "timeout";
    private static final String EXIT_RECOVERY_REASON_TXRX_OVER_FLOW = "txrx_over_flow";
    private static final String EXIT_RECOVERY_REASON_UNKNOWN = "unknown";
    private static final String EXIT_RECOVERY_REASON_WIFI_ON = "wifi_on";
    private static final String PERSIST_DEBUG_KEY = "persist.oppo.network.pdp_recovery.debug";
    private static final String PERSIST_DEBUG_TOAST = "persist.oppo.network.pdp_recovery.toast";
    private static final String TAG = "OppoFastRecovery";
    private static OppoFastRecovery mInstance = null;
    private static final Object mLock = new Object();
    private static INetworkStatsService sStatsService;
    private int m5GReocveryNetworkOk = 0;
    private boolean mCidCountDebug = false;
    private ConnectivityManager mConnectivityManager;
    public Context mContext;
    public boolean mDataConnected = false;
    private int mDataPhoneId = 0;
    private boolean mDayCountDebug = false;
    private int mDnsCheckCount = 0;
    private boolean mDnsDebug = false;
    private int mDnsTestCount = 0;
    private long mDorecoveryDoneTime = 0;
    private int mEnter5GRecoveryCount = 0;
    private int mEnter5GTryRecoveryCount = 0;
    private int mEnterTryDorecoveryCount = 0;
    private boolean mGetToastFlag = false;
    private boolean mIpDebug = false;
    private boolean mIs5GNotRestrictDebug = false;
    public boolean mIsAirplane = false;
    public boolean mIsDataEnabled = false;
    private boolean mIsIn5gAnchorCell = false;
    private boolean mIsScreenOn = true;
    private boolean mIsSet5gRatHere = false;
    public boolean mIsVoiceOn = false;
    public boolean mIsWifiOn = false;
    private final INetdEventCallback mNetdEventCallback = new BaseNetdEventCallback() {
        /* class com.oppo.internal.telephony.recovery.OppoFastRecovery.AnonymousClass1 */

        public void onDnsEvent(int netId, int eventType, int returnCode, String hostname, String[] ipAddresses, int ipAddressesCount, long timestamp, int uid) {
            try {
                if (OppoFastRecovery.this.mDnsDebug) {
                    OppoFastRecovery.access$108(OppoFastRecovery.this);
                    OppoFastRecovery.this.mNetworkCheckHandler.obtainMessage(6, 1, OppoFastRecovery.this.mDnsTestCount).sendToTarget();
                } else if (255 == returnCode) {
                    OppoFastRecovery.this.mNetworkCheckHandler.obtainMessage(6, 1, uid).sendToTarget();
                } else {
                    OppoFastRecovery.this.mNetworkCheckHandler.obtainMessage(6, 0, 0).sendToTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "INetdEventCallback callback invalid!");
            }
        }
    };
    private NetworkCheckHandler mNetworkCheckHandler;
    private int mNetworkRecoveryCount = 0;
    private OFastRecoveryHandler mOFastRecoveryHandler;
    private Oppo5GCellBlacklistMonitor mOppo5gCellBlacklistMonitor;
    private OppoTxRxCheck mOppoTxRxCheck;
    public Phone mPhone;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class com.oppo.internal.telephony.recovery.OppoFastRecovery.AnonymousClass5 */

        public void onCallStateChanged(int state, String phoneNumber) {
            try {
                Rlog.d(OppoFastRecovery.TAG, "onCallStateChanged state:" + state + " phoneNumber:" + phoneNumber);
                if (OppoFastRecovery.this.mIsVoiceOn && state == 0) {
                    OppoFastRecovery.this.mIsVoiceOn = false;
                    OppoFastRecovery.this.mNetworkCheckHandler.obtainMessage(8).sendToTarget();
                } else if (!OppoFastRecovery.this.mIsVoiceOn && state != 0) {
                    OppoFastRecovery.this.mIsVoiceOn = true;
                    OppoFastRecovery.this.mNetworkCheckHandler.obtainMessage(7).sendToTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "onCallStateChanged call exception :" + e.getMessage());
            }
        }
    };
    private boolean mPingCheck = true;
    private int mRatDebug = 0;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.oppo.internal.telephony.recovery.OppoFastRecovery.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            try {
                OppoFastRecovery.this.broadcastReceiverAction(context, intent);
            } catch (Exception e) {
                Rlog.e(OppoFastRecovery.TAG, "BroadcastReceiver failed" + e.getMessage());
                e.printStackTrace();
            }
        }
    };
    public OFastRecoveryConfig mRecoveryConfig;
    private int mRecoveryCount = 0;
    private boolean mSSDebug = false;
    private ContentObserver mSettingObserver = new ContentObserver(new Handler()) {
        /* class com.oppo.internal.telephony.recovery.OppoFastRecovery.AnonymousClass3 */

        public void onChange(boolean selfChange) {
            try {
                OFastRecoveryConfig cfg = OFastRecoveryConfig.parseConfig(Settings.Global.getString(OppoFastRecovery.this.mContext.getContentResolver(), OFastRecoveryConfig.SYS_CONFIG_URI));
                if (cfg != null) {
                    OppoFastRecovery.this.mRecoveryConfig = cfg;
                    SystemProperties.set(OFastRecoveryConfig.PERSIST_CONFIG_KEY, cfg.genPersistConfigStr());
                    Rlog.d(OppoFastRecovery.TAG, "update config from rus: " + cfg + "; save cfg:" + cfg.genPersistConfigStr());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "update rus config failed!" + e.getMessage());
            }
        }
    };
    private ContentObserver mSettingTacObserver = new ContentObserver(new Handler()) {
        /* class com.oppo.internal.telephony.recovery.OppoFastRecovery.AnonymousClass4 */

        public void onChange(boolean selfChange) {
            OppoRecoveryTacConfig.updateSettingConfig(OppoFastRecovery.this.mContext);
        }
    };
    private TelephonyManager mTelephonyManager;
    private boolean mToastEnable = false;
    private int mTxrxCheckCount = 0;
    private int mUserCfgRatDebug = 0;

    static /* synthetic */ int access$108(OppoFastRecovery x0) {
        int i = x0.mDnsTestCount;
        x0.mDnsTestCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$508(OppoFastRecovery x0) {
        int i = x0.mEnterTryDorecoveryCount;
        x0.mEnterTryDorecoveryCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$608(OppoFastRecovery x0) {
        int i = x0.mDnsCheckCount;
        x0.mDnsCheckCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$708(OppoFastRecovery x0) {
        int i = x0.mTxrxCheckCount;
        x0.mTxrxCheckCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$808(OppoFastRecovery x0) {
        int i = x0.mRecoveryCount;
        x0.mRecoveryCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$908(OppoFastRecovery x0) {
        int i = x0.mNetworkRecoveryCount;
        x0.mNetworkRecoveryCount = i + 1;
        return i;
    }

    /* access modifiers changed from: private */
    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: public */
    private void broadcastReceiverAction(Context context, Intent intent) {
        char c;
        String action = intent.getAction();
        Rlog.d(TAG, "onReceive:" + action);
        switch (action.hashCode()) {
            case -2128145023:
                if (action.equals("android.intent.action.SCREEN_OFF")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1454123155:
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1172645946:
                if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1076576821:
                if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -25388475:
                if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1041332296:
                if (action.equals("android.intent.action.DATE_CHANGED")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            this.mIsScreenOn = true;
            trigerStateChange();
        } else if (c == 1) {
            this.mIsScreenOn = false;
            trigerStateChange();
        } else if (c == 2) {
            try {
                int dataPhoneId = getDataPhoneId();
                if (dataPhoneId != this.mDataPhoneId) {
                    Rlog.d(TAG, "dds change " + this.mDataPhoneId + " to " + dataPhoneId);
                    this.mDataPhoneId = dataPhoneId;
                    this.mPhone = PhoneFactory.getPhone(this.mDataPhoneId);
                    this.mNetworkCheckHandler.sendEmptyMessage(5);
                }
                Rlog.d(TAG, "mDataPhoneId.." + this.mDataPhoneId);
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(TAG, "ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED process exception:" + e.getMessage());
            }
        } else if (c == 3) {
            try {
                this.mIsWifiOn = isWifiConnect();
                if (this.mPhone != null) {
                    this.mIsDataEnabled = this.mPhone.getDataEnabledSettings().isUserDataEnabled();
                } else {
                    this.mIsDataEnabled = false;
                }
                this.mNetworkCheckHandler.sendEmptyMessage(11);
                Rlog.d(TAG, "CONNECTIVITY_ACTION: mIsWifiOn:" + this.mIsWifiOn + ",mIsDataEnabled:" + this.mIsDataEnabled);
            } catch (Exception e2) {
                e2.printStackTrace();
                Rlog.e(TAG, "CONNECTIVITY_ACTION process exception:" + e2.getMessage());
            }
        } else if (c == 4) {
            this.mIsAirplane = isAirPlaneModeOn();
            Rlog.d(TAG, "ACTION_AIRPLANE_MODE_CHANGED mIsAirplane:" + this.mIsAirplane);
            trigerStateChange();
            resetRadioSmooth();
        } else if (c == 5) {
            Rlog.d(TAG, "recv ACTION_DATE_CHANGED ->");
            this.mNetworkCheckHandler.sendEmptyMessage(12);
        }
    }

    private boolean getToastShowEnabled() {
        try {
            if (this.mGetToastFlag) {
                return this.mToastEnable;
            }
            this.mToastEnable = SystemProperties.getBoolean(PERSIST_DEBUG_TOAST, false);
            this.mGetToastFlag = true;
            return this.mToastEnable;
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.d(TAG, "getToastShowEnabled failed!");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showToastMsg(String msg) {
        if (getToastShowEnabled()) {
            Toast.makeText(this.mContext, msg, 1).show();
        }
    }

    private OppoFastRecovery(Context context) {
        try {
            Rlog.d(TAG, "new Instance...");
            this.mContext = context;
            initDebug();
            this.mDataPhoneId = getDataPhoneId();
            this.mPhone = PhoneFactory.getPhone(this.mDataPhoneId);
            this.mTelephonyManager = TelephonyManager.from(this.mContext);
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            initRecoveryConfig(context);
            this.mIsWifiOn = isWifiConnect();
            this.mIsScreenOn = isScreenOn(context);
            this.mIsVoiceOn = isInVoiceCall();
            if (this.mPhone != null) {
                this.mIsDataEnabled = this.mPhone.getDataEnabledSettings().isUserDataEnabled();
            }
            this.mIsAirplane = isAirPlaneModeOn();
            this.mOppoTxRxCheck = new OppoTxRxCheck(this);
            HandlerThread mNetCheckThread = new HandlerThread("mNetCheckThread");
            mNetCheckThread.start();
            this.mNetworkCheckHandler = new NetworkCheckHandler(mNetCheckThread.getLooper());
            HandlerThread mDoRecoveryThread = new HandlerThread("mDoRecoveryThread");
            mDoRecoveryThread.start();
            this.mOFastRecoveryHandler = new OFastRecoveryHandler(mDoRecoveryThread.getLooper());
            this.mOppo5gCellBlacklistMonitor = new Oppo5GCellBlacklistMonitor(this, mNetCheckThread.getLooper());
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
            intentFilter.addAction("android.intent.action.DATE_CHANGED");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
            networkMonitorRegister();
            phoneCallStateInit();
            Rlog.d(TAG, "init over, mIsScreenOn:" + this.mIsScreenOn + ",mIsWifiOn:" + this.mIsWifiOn + ",mDataConnected:" + this.mDataConnected + ",mIsVoiceOn:" + this.mIsVoiceOn + ",mIsDataEnabled:" + this.mIsDataEnabled + ",mIsAirplane:" + this.mIsAirplane);
            trigerStateChange();
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "init failed:" + e.getMessage());
        }
    }

    public static OppoFastRecovery make(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new OppoFastRecovery(context);
            }
        }
        return mInstance;
    }

    /* access modifiers changed from: private */
    public static synchronized INetworkStatsService getStatsService() {
        INetworkStatsService iNetworkStatsService;
        synchronized (OppoFastRecovery.class) {
            if (sStatsService == null) {
                sStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
            }
            iNetworkStatsService = sStatsService;
        }
        return iNetworkStatsService;
    }

    public void initRecoveryConfig(Context context) {
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor(OFastRecoveryConfig.SYS_CONFIG_URI), true, this.mSettingObserver);
        this.mRecoveryConfig = OFastRecoveryConfig.getSystemConfig(context);
        Rlog.d(TAG, "initRecoveryConfig:" + this.mRecoveryConfig);
        OppoRecoveryTacConfig.initTacConfig(context, this.mSettingTacObserver);
    }

    private void initDebug() {
        try {
            String str = SystemProperties.get(PERSIST_DEBUG_KEY, DEFAULT_DEBUG_CONFIG);
            if (str != null) {
                String[] split = str.split(",");
                this.mIpDebug = Integer.parseInt(split[0]) == 1;
                this.mDnsDebug = Integer.parseInt(split[1]) == 1;
                this.mSSDebug = Integer.parseInt(split[2]) == 1;
                this.mDayCountDebug = Integer.parseInt(split[3]) == 1;
                this.mCidCountDebug = Integer.parseInt(split[4]) == 1;
                this.mRatDebug = Integer.parseInt(split[5]);
                this.mPingCheck = Integer.parseInt(split[6]) == 1;
                this.mIs5GNotRestrictDebug = Integer.parseInt(split[7]) == 1;
                this.mUserCfgRatDebug = Integer.parseInt(split[8]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.d(TAG, "initDebug config failed!");
            this.mIpDebug = false;
            this.mDnsDebug = false;
            this.mSSDebug = false;
            this.mDayCountDebug = false;
            this.mCidCountDebug = false;
            this.mRatDebug = 0;
            this.mPingCheck = true;
            this.mIs5GNotRestrictDebug = false;
            this.mUserCfgRatDebug = 0;
        }
    }

    private void trigerStateChange() {
        this.mNetworkCheckHandler.sendEmptyMessage(2);
    }

    public boolean isInVoiceCall() {
        return this.mTelephonyManager.getCallState() != 0;
    }

    private boolean isAirPlaneModeOn() {
        int mode = 0;
        try {
            mode = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on");
        } catch (Exception e) {
            Rlog.e(TAG, "isAirPlaneModeOn get failed:" + e.getMessage());
            e.printStackTrace();
        }
        return mode == 1;
    }

    private void phoneCallStateInit() {
        try {
            Rlog.d(TAG, "phoneCallStateInit start");
            this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "phoneCallStateInit failed!" + e.getMessage());
        }
    }

    private boolean networkMonitorRegister() {
        IIpConnectivityMetrics ipconn = IIpConnectivityMetrics.Stub.asInterface(ServiceManager.getService("connmetrics"));
        Rlog.d(TAG, "Starting network ." + ipconn);
        if (ipconn == null) {
            return false;
        }
        try {
            return ipconn.addNetdEventCallback(1, this.mNetdEventCallback);
        } catch (Exception re) {
            re.printStackTrace();
            Rlog.d(TAG, "Failed to make remote calls to register the callback" + re);
            return false;
        }
    }

    public int getDataPhoneId() {
        SubscriptionController instance = SubscriptionController.getInstance();
        Rlog.d(TAG, "getDataPhoneId default subid:" + instance.getDefaultDataSubId());
        return instance.getPhoneId(instance.getDefaultDataSubId());
    }

    private boolean isWifiConnect() {
        NetworkInfo wifiInfo = this.mConnectivityManager.getNetworkInfo(1);
        if (wifiInfo != null) {
            return wifiInfo.isConnected();
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDataConnect() {
        NetworkInfo ni = this.mConnectivityManager.getNetworkInfo(0);
        if (ni != null) {
            return ni.isConnected();
        }
        return false;
    }

    public boolean isScreenOn(Context context) {
        PowerManager manager = (PowerManager) context.getSystemService("power");
        if (manager != null) {
            return manager.isInteractive();
        }
        return false;
    }

    public boolean isMobileDataEnabled() {
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager == null) {
            return false;
        }
        return telephonyManager.isDataEnabled();
    }

    /* access modifiers changed from: private */
    public static class CellInfoLte {
        public int mCid;
        public int mNrState;
        public int mPci;
        public int mTac;

        private CellInfoLte() {
            this.mCid = -1;
            this.mTac = -1;
            this.mPci = -1;
            this.mNrState = 0;
        }

        public String toString() {
            return "CellInfoLte{mCid=" + this.mCid + ", mTac=" + this.mTac + ", mPci=" + this.mPci + ", mNrState=" + this.mNrState + '}';
        }
    }

    /* access modifiers changed from: private */
    public static class MsgWapper {
        public CellInfoLte cellInfo;
        public String reason = "";

        public MsgWapper(CellInfoLte cellInfo2, String reason2) {
            this.cellInfo = cellInfo2;
            this.reason = reason2;
        }
    }

    private CellIdentityLte getLteCellIdentity(Phone phone) {
        CellIdentity id;
        if (phone == null) {
            Rlog.e(TAG, "phone is null!");
            return null;
        }
        ServiceState ss = phone.getServiceState();
        if (ss == null) {
            Rlog.e(TAG, "getLteCellIdentity ss is null");
            return null;
        }
        NetworkRegistrationInfo regInfo = ss.getNetworkRegistrationInfo(2, 1);
        if (regInfo == null || regInfo.getCellIdentity() == null) {
            regInfo = ss.getNetworkRegistrationInfo(1, 1);
        }
        if (regInfo == null || (id = regInfo.getCellIdentity()) == null || id.getType() != 3) {
            return null;
        }
        return (CellIdentityLte) id;
    }

    public CellInfoLte getLetCellInfo() {
        CellInfoLte info = new CellInfoLte();
        try {
            CellIdentityLte id = getLteCellIdentity(this.mPhone);
            if (id == null) {
                Rlog.e(TAG, "CellIdentityLte is null");
                return info;
            }
            info.mCid = id.getCi();
            info.mTac = id.getTac();
            info.mPci = id.getPci();
            info.mNrState = get5GNrState(this.mPhone);
            Rlog.d(TAG, "getLetCellInfo:" + info);
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "getLetCellInfo failed!" + e.getMessage());
            return info;
        }
    }

    public int getLteCellid() {
        int cid = -1;
        try {
            CellIdentityLte id = getLteCellIdentity(this.mPhone);
            if (id != null) {
                cid = id.getCi();
            }
            Rlog.d(TAG, "getLteCellid cid=" + cid);
            return cid;
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "getLteCellid " + e.getMessage());
            return -1;
        }
    }

    public int getLteCellInfoTac() {
        int tac = -1;
        try {
            CellIdentityLte id = getLteCellIdentity(this.mPhone);
            if (id != null) {
                tac = id.getTac();
            }
            Rlog.d(TAG, "getLteCellInfoTac tac=" + tac);
            return tac;
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "getLteCellInfoTac " + e.getMessage());
            return -1;
        }
    }

    public int getLteCellInfoPci() {
        int pci = -1;
        try {
            CellIdentityLte id = getLteCellIdentity(this.mPhone);
            if (id != null) {
                pci = id.getPci();
            }
            Rlog.d(TAG, "getLteCellInfoPci pci=" + pci);
            return pci;
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "getLteCellInfoPci " + e.getMessage());
            return -1;
        }
    }

    private boolean isPsTech(int tech) {
        return tech == 14 || tech == 19 || tech == 20;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getIsLteOrNr() {
        try {
            if (this.mRatDebug != 0) {
                Rlog.d(TAG, "getIsLteOrNr test mode " + this.mRatDebug);
                return isPsTech(this.mRatDebug);
            } else if (this.mPhone == null) {
                Rlog.e(TAG, "mPhone is null!");
                return false;
            } else {
                ServiceState ss = this.mPhone.getServiceState();
                if (ss == null) {
                    return false;
                }
                if (isPsTech(ss.getRilDataRadioTechnology()) || isPsTech(ss.getRilVoiceRadioTechnology())) {
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "getIsLteOrNr failed!" + e.getMessage());
            return false;
        }
    }

    private int getRilDataRadioTechnology() {
        try {
            if (this.mPhone == null) {
                Rlog.e(TAG, "mPhone is null!");
                return -1;
            }
            ServiceState ss = this.mPhone.getServiceState();
            if (ss == null) {
                return -1;
            }
            return ss.getRilDataRadioTechnology();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getSignalStrengthRsrp() {
        try {
            NetworkDiagnoseService diagnoseService = NetworkDiagnoseService.getInstance();
            if (diagnoseService == null) {
                return -1;
            }
            if (this.mPhone == null) {
                Rlog.e(TAG, "mPhone is null!");
                return -1;
            }
            OppoPhoneStateMonitor stateMonitor = diagnoseService.getPhoneStateMonitor(this.mPhone.getPhoneId());
            if (stateMonitor != null) {
                SignalStrength signalStrength = stateMonitor.getSignalStrength();
                if (signalStrength != null) {
                    return signalStrength.getLteDbm();
                }
                throw new Exception("signalStrength is null");
            }
            throw new Exception("stateMonitor is null");
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSignalStrengthValid() {
        if (this.mSSDebug) {
            Rlog.d(TAG, "isSignalStrengthValid test mode return true");
            return true;
        }
        try {
            NetworkDiagnoseService diagnoseService = NetworkDiagnoseService.getInstance();
            if (diagnoseService == null) {
                return false;
            }
            if (this.mPhone == null) {
                Rlog.e(TAG, "mPhone is null!");
                return false;
            }
            OppoPhoneStateMonitor stateMonitor = diagnoseService.getPhoneStateMonitor(this.mPhone.getPhoneId());
            if (stateMonitor != null) {
                SignalStrength signalStrength = stateMonitor.getSignalStrength();
                if (signalStrength != null) {
                    int rsrp = signalStrength.getLteDbm();
                    Rlog.d(TAG, "signalStrength.getLteDbm " + rsrp);
                    if (((long) rsrp) <= this.mRecoveryConfig.mMinRsrp || rsrp >= -44) {
                        return false;
                    }
                    return true;
                }
                throw new Exception("signalStrength is null");
            }
            throw new Exception("stateMonitor is null");
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "isSignalStrengthValid failed " + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNrNetworkModeType(int type) {
        if (type == 23 || type == 24 || type == 25 || type == 26 || type == 27 || type == 28 || type == 29 || type == 30 || type == 31 || type == 32 || type == 33) {
            return true;
        }
        return false;
    }

    public int getUserPreferredNetworkTypeCfg(Phone phone) {
        try {
            NetworkDiagnoseService diagnoseService = NetworkDiagnoseService.getInstance();
            if (diagnoseService == null) {
                diagnoseService = NetworkDiagnoseService.make(this.mContext);
            }
            OppoPhoneStateMonitor stateMonitor = diagnoseService.getPhoneStateMonitor(phone.getPhoneId());
            if (stateMonitor != null) {
                return stateMonitor.getPreferredNetworkType();
            }
            Rlog.d(TAG, "stateMonitor is null");
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "getUserPreferredNetworkTypeCfg failed " + e.getMessage());
            return -1;
        }
    }

    private boolean checkIn5gAnchorCell(int tac) {
        Rlog.d(TAG, "checkIn5gAnchorCell " + tac + " mode: " + this.mRecoveryConfig.mUseTacListMode);
        int i = this.mRecoveryConfig.mUseTacListMode;
        if (i == 0) {
            return this.mIsIn5gAnchorCell;
        }
        if (i == 1) {
            return OppoRecoveryTacConfig.isSpecial5gTac(tac);
        }
        if (i != 2) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean is5GSpecialTac() {
        try {
            int tac = getLteCellInfoTac();
            Rlog.d(TAG, "is5GSpecialTac get tac:" + tac);
            if (!checkIn5gAnchorCell(tac)) {
                return false;
            }
            if (this.mUserCfgRatDebug != 0) {
                Rlog.d(TAG, "is5GSpecialTac mUserCfgRatDebug:" + this.mUserCfgRatDebug);
                return isNrNetworkModeType(this.mUserCfgRatDebug);
            }
            int mNetworkMode = getUserPreferredNetworkTypeCfg(this.mPhone);
            if (mNetworkMode == -1) {
                Rlog.e(TAG, "is5GSpecialTac mNetworkMode invalid!");
                return false;
            }
            Rlog.d(TAG, "is5GSpecialTac mNetworkMode:" + mNetworkMode);
            if (isNrNetworkModeType(mNetworkMode)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Rlog.e(TAG, "is5GSpecialTac  failed!" + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSmart5gEnable(Phone phone) {
        int sw = 0;
        if (phone != null) {
            try {
                int mSubId = this.mPhone.getSubId();
                if (SubscriptionManager.isValidSubscriptionId(mSubId)) {
                    ContentResolver contentResolver = this.mContext.getContentResolver();
                    sw = Settings.Global.getInt(contentResolver, "smart_fiveg" + mSubId, 0);
                    Rlog.d(TAG, "isSmart5gEnable subid " + mSubId + "smart 5g:" + sw);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(TAG, "isSmart5gEnable failed:" + e.getMessage());
                return false;
            }
        } else {
            sw = Settings.Global.getInt(this.mContext.getContentResolver(), "smart_fiveg", 0);
            Rlog.d(TAG, "isSmart5gEnable global smart 5g:" + sw);
        }
        Rlog.d(TAG, "isSmart5gEnable: " + sw);
        if (sw == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int get5GNrState(Phone phone) {
        if (phone != null) {
            try {
                return phone.getServiceState().getNrState();
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(TAG, "get5GNrState failed:" + e.getMessage());
                return -1;
            }
        } else {
            throw new Exception("phone is null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetRadioSmooth() {
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            try {
                Phone phone = PhoneFactory.getPhone(i);
                if (!(phone == null || phone.getServiceStateTracker() == null)) {
                    Rlog.d(TAG, "setOemRadioReseting false");
                    ((AbstractServiceStateTracker) OemTelephonyUtils.typeCasting(AbstractServiceStateTracker.class, phone.getServiceStateTracker())).setOemRadioReseting(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(TAG, "resetRadioSmooth failed! " + e.getMessage());
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public class NetworkCheckHandler extends Handler {
        public static final int EVENT_CHECK_INTVL = 1;
        public static final int EVENT_CONFIG_UPDATE = 10;
        public static final int EVENT_DATE_CHANGE = 12;
        public static final int EVENT_DDS_CHANGE = 5;
        public static final int EVENT_DNS_RESULT = 6;
        public static final int EVENT_NETWORK_CHANGE = 11;
        public static final int EVENT_NETWORK_CHECK_RESULT = 9;
        public static final int EVENT_RESTART_CHECK = 4;
        public static final int EVENT_STATE_CHNAGE = 2;
        public static final int EVENT_VOICE_CALL_ACTIVE = 7;
        public static final int EVENT_VOICE_CALL_END = 8;
        private static final int TYPE_TCP_RX_PACKETS = 4;
        private static final int TYPE_TCP_TX_PACKETS = 5;
        private long ONE_DAY_MS = OppoNewNitzStateMachine.NITZ_NTP_INTERVAL_OEM;
        private int countxx = 0;
        private long mCheckingCount = 0;
        private boolean mCidDoRecovery = false;
        private ArrayList<Long> mDayCountList = new ArrayList<>();
        private ArrayList<DnsFailInfo> mDnsFailList = new ArrayList<>();
        private long mEnterLoopCount = 0;
        private boolean mIsChecking = false;
        private boolean mIsLastCellNetActive = false;
        private boolean mIsLogicNetOk = true;
        private int mLastCid = -1;
        private String mLastIfName = "";
        private long mLastMobileRxBytes = -1;
        private long mLastMobileTxBytes = -1;
        private long mLastTcpRxPackets = -1;
        private long mLastTcpTxPackets = -1;
        private int mR0CountGE12 = 0;
        private int mR0CountGE16 = 0;
        private int mR0CountGE20 = 0;
        private int mR0CountGE4 = 0;
        private int mR0CountGE8 = 0;
        private long mRealLoopCount = 0;
        private int mRx0ValueKpiCount = 0;
        public long mRxBytes = -1;
        private long mTcpRxPackets = -1;
        private long mTcpTxPackets = -1;
        public long mTxBytes = -1;

        public NetworkCheckHandler(Looper looper) {
            super(looper);
            resetData();
        }

        public void handleMessage(Message message) {
            Exception e;
            try {
                int i = message.what;
                boolean z = true;
                if (i != 1) {
                    if (i != 2) {
                        switch (i) {
                            case 5:
                                ddsCheck();
                                return;
                            case 6:
                                if (message.arg1 <= 0) {
                                    z = false;
                                }
                                dnsFaileUpdate(z, message.arg2);
                                return;
                            case 7:
                            case 8:
                                break;
                            case 9:
                                long runCount = ((Long) message.obj).longValue();
                                Rlog.d(OppoFastRecovery.TAG, "EVENT_NETWORK_CHECK_RESULT:" + runCount);
                                if (message.arg1 == 0) {
                                    networkRecover(runCount, OppoFastRecovery.EXIT_RECOVERY_REASON_NETWORK_ERCOVER);
                                    return;
                                }
                                return;
                            case 10:
                                Rlog.d(OppoFastRecovery.TAG, "EVENT_CONFIG_UPDATE! " + this.mIsChecking + " " + this.mIsLogicNetOk);
                                resetData();
                                stateChange();
                                return;
                            case 11:
                                stateChange();
                                networkChangeProc();
                                return;
                            case 12:
                                try {
                                    Rlog.d(OppoFastRecovery.TAG, "recv EVENT_DATE_CHANGE");
                                    if (OppoFastRecovery.this.mRecoveryConfig.enable) {
                                        OppoDorecoveryStatistics.eventRecoveryKPI(new OppoDorecoveryStatistics.PdpRecoveryStatisticsKpi(OppoFastRecovery.this.mEnterTryDorecoveryCount, OppoFastRecovery.this.mDnsCheckCount, OppoFastRecovery.this.mTxrxCheckCount, OppoFastRecovery.this.mRecoveryCount, OppoFastRecovery.this.mNetworkRecoveryCount, OppoFastRecovery.this.mEnter5GTryRecoveryCount, OppoFastRecovery.this.mEnter5GRecoveryCount, OppoFastRecovery.this.m5GReocveryNetworkOk, this.mR0CountGE4, this.mR0CountGE8, this.mR0CountGE12, this.mR0CountGE16, this.mR0CountGE20));
                                    }
                                    OppoDorecoveryStatistics.uploadLog(OppoFastRecovery.this.mContext);
                                    resetKpiStatistics();
                                    return;
                                } catch (Exception e2) {
                                    e = e2;
                                    e.printStackTrace();
                                    Rlog.e(OppoFastRecovery.TAG, "NetworkCheckHandler handle msg failed" + e.getMessage());
                                }
                            default:
                                return;
                        }
                    }
                    stateChange();
                    return;
                }
                updateDataStat();
                sendEmptyMessageDelayed(1, OppoFastRecovery.this.mRecoveryConfig.mCheckIntval * 1000);
            } catch (Exception e3) {
                e = e3;
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "NetworkCheckHandler handle msg failed" + e.getMessage());
            }
        }

        private void resetKpiStatistics() {
            OppoFastRecovery.this.mEnterTryDorecoveryCount = 0;
            OppoFastRecovery.this.mRecoveryCount = 0;
            OppoFastRecovery.this.mNetworkRecoveryCount = 0;
            OppoFastRecovery.this.mDnsCheckCount = 0;
            OppoFastRecovery.this.mTxrxCheckCount = 0;
            this.mR0CountGE4 = 0;
            this.mR0CountGE8 = 0;
            this.mR0CountGE12 = 0;
            this.mR0CountGE16 = 0;
            this.mR0CountGE20 = 0;
        }

        private boolean needCheck() {
            Rlog.d(OppoFastRecovery.TAG, "needCheck -> mIsWifiOn:" + OppoFastRecovery.this.mIsWifiOn + ",mIsVoiceOn:" + OppoFastRecovery.this.mIsVoiceOn + ",mIsScreenOn:" + OppoFastRecovery.this.mIsScreenOn + ",mIsDataEnabled:" + OppoFastRecovery.this.mIsDataEnabled + ",mIsAirplane:" + OppoFastRecovery.this.mIsAirplane + ",mRecoveryConfig.enable:" + OppoFastRecovery.this.mRecoveryConfig.enable);
            return !OppoFastRecovery.this.mIsWifiOn && !OppoFastRecovery.this.mIsVoiceOn && OppoFastRecovery.this.mIsScreenOn && OppoFastRecovery.this.mIsDataEnabled && !OppoFastRecovery.this.mIsAirplane && OppoFastRecovery.this.mRecoveryConfig.enable;
        }

        private void resetData() {
            Rlog.d(OppoFastRecovery.TAG, "resetData");
            this.mCheckingCount++;
            this.mLastMobileRxBytes = -1;
            this.mLastMobileTxBytes = -1;
            this.mLastTcpRxPackets = -1;
            this.mLastTcpTxPackets = -1;
            this.mRxBytes = -1;
            this.mTxBytes = -1;
            this.mTcpRxPackets = -1;
            this.mTcpTxPackets = -1;
            this.mDnsFailList.clear();
            OppoFastRecovery.this.mOppoTxRxCheck.clearRx0countList();
        }

        public class DnsFailInfo {
            public long mRxSpeed;
            public long mTime;
            public long mTxSpeed;
            public int mUid;

            public DnsFailInfo(int uid, long time, long rxSpeed, long txSpeed) {
                this.mUid = uid;
                this.mTime = time;
                this.mRxSpeed = rxSpeed;
                this.mTxSpeed = txSpeed;
            }
        }

        private void addDnsFailCount(int uid, long time) {
            DnsFailInfo tmp = null;
            Iterator<DnsFailInfo> it = this.mDnsFailList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                DnsFailInfo info = it.next();
                if (info.mUid == uid) {
                    tmp = info;
                    break;
                }
            }
            if (tmp != null) {
                this.mDnsFailList.remove(tmp);
            }
            this.mDnsFailList.add(new DnsFailInfo(uid, time, this.mRxBytes / OppoFastRecovery.this.mRecoveryConfig.mCheckIntval, this.mTxBytes / OppoFastRecovery.this.mRecoveryConfig.mCheckIntval));
        }

        private boolean dnsCountFailCheck(long curTime) {
            long maxRxSpeed = 0;
            long maxTxSpeed = 0;
            if (this.mDnsFailList.size() < OppoFastRecovery.this.mRecoveryConfig.mDnsFailCount) {
                return false;
            }
            Iterator<DnsFailInfo> inter = this.mDnsFailList.iterator();
            while (inter.hasNext() && curTime - inter.next().mTime > ((long) OppoFastRecovery.this.mRecoveryConfig.mDnsEffectiveTime) * 1000) {
                inter.remove();
            }
            Iterator<DnsFailInfo> it = this.mDnsFailList.iterator();
            while (it.hasNext()) {
                DnsFailInfo info = it.next();
                if (info.mRxSpeed >= maxRxSpeed && info.mTxSpeed >= maxTxSpeed) {
                    maxRxSpeed = info.mRxSpeed;
                    maxTxSpeed = info.mTxSpeed;
                }
            }
            if (this.mDnsFailList.size() < OppoFastRecovery.this.mRecoveryConfig.mDnsFailCount || (maxRxSpeed >= OppoFastRecovery.this.mRecoveryConfig.mRxThresholdSpeed && maxTxSpeed >= OppoFastRecovery.this.mRecoveryConfig.mTxThresholdSpeed)) {
                return false;
            }
            Rlog.w(OppoFastRecovery.TAG, "real in effect time dns faile count " + this.mDnsFailList.size() + ", speed:" + maxRxSpeed + "," + maxTxSpeed);
            return true;
        }

        private void dnsFaileUpdate(boolean isFail, int uid) {
            if (this.mIsChecking && recoveryPreCheck()) {
                long curTime = System.currentTimeMillis();
                if (isFail && !OppoFastRecovery.this.mOFastRecoveryHandler.isDoingRecovery()) {
                    addDnsFailCount(uid, curTime);
                    dnsFailCheck(curTime);
                } else if (this.mDnsFailList.size() != 0) {
                    Rlog.d(OppoFastRecovery.TAG, "dnsFaileUpdate mDnsFailCount reset to 0");
                    this.mDnsFailList.clear();
                }
            }
        }

        private String getStopCheckReason() {
            if (OppoFastRecovery.this.mIsWifiOn) {
                return OppoFastRecovery.EXIT_RECOVERY_REASON_WIFI_ON;
            }
            if (OppoFastRecovery.this.mIsVoiceOn) {
                return OppoFastRecovery.EXIT_RECOVERY_REASON_CALL_IN;
            }
            if (!OppoFastRecovery.this.mIsScreenOn) {
                return OppoFastRecovery.EXIT_RECOVERY_REASON_SCREEN_OFF;
            }
            if (!OppoFastRecovery.this.mIsDataEnabled) {
                return OppoFastRecovery.EXIT_RECOVERY_REASON_DATAENABLE_OFF;
            }
            if (OppoFastRecovery.this.mIsAirplane) {
                return OppoFastRecovery.EXIT_RECOVERY_REASON_AIR_PLANE_ON;
            }
            if (!OppoFastRecovery.this.mRecoveryConfig.enable) {
                return OppoFastRecovery.EXIT_RECOVERY_REASON_FEATURE_DISABLE;
            }
            return OppoFastRecovery.EXIT_RECOVERY_REASON_UNKNOWN;
        }

        private void stateChange() {
            boolean needcheck = needCheck();
            CellInfoLte cif = OppoFastRecovery.this.getLetCellInfo();
            Rlog.d(OppoFastRecovery.TAG, "stateChange " + this.mIsChecking + " needCheck:" + needcheck + ", cif:" + cif);
            if (this.mIsChecking && !needcheck) {
                resetData();
                this.mIsChecking = false;
                this.mIsLogicNetOk = true;
                OppoFastRecovery.this.mOFastRecoveryHandler.obtainMessage(2, 0, 0, new MsgWapper(cif, getStopCheckReason())).sendToTarget();
                removeMessages(1);
            } else if (!this.mIsChecking && needcheck) {
                resetData();
                this.mIsChecking = true;
                this.mIsLogicNetOk = true;
                sendEmptyMessageDelayed(1, OppoFastRecovery.this.mRecoveryConfig.mCheckIntval * 1000);
            }
        }

        private void networkChangeProc() {
            boolean isCellNetActive = false;
            NetworkInfo ni = OppoFastRecovery.this.mConnectivityManager.getActiveNetworkInfo();
            if (ni != null && ni.isConnected() && ni.getType() == 0) {
                isCellNetActive = true;
            }
            if (this.mIsLastCellNetActive != isCellNetActive) {
                this.mIsLastCellNetActive = isCellNetActive;
            }
            Rlog.d(OppoFastRecovery.TAG, "networkChangeProc " + this.mIsChecking + "," + isCellNetActive + "," + this.mIsLogicNetOk);
            if (this.mIsChecking && !this.mIsLogicNetOk && isCellNetActive) {
                Rlog.d(OppoFastRecovery.TAG, "networkChangeProc cell network active");
                startPingNetwork();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startPingNetwork() {
            if (!OppoFastRecovery.this.mPingCheck) {
                Rlog.d(OppoFastRecovery.TAG, "startPingNetwork donot check");
            } else {
                new NetworkCheckRecovery(this, 9, Long.valueOf(this.mCheckingCount)).start();
            }
        }

        private void ddsCheck() {
            CellInfoLte cif = OppoFastRecovery.this.getLetCellInfo();
            Rlog.d(OppoFastRecovery.TAG, "ddsCheck then reset data");
            resetData();
            this.mIsLogicNetOk = true;
            OppoFastRecovery.this.mOFastRecoveryHandler.obtainMessage(2, 0, 0, new MsgWapper(cif, OppoFastRecovery.EXIT_RECOVERY_REASON_DDS_CHANGE)).sendToTarget();
            OppoFastRecovery.this.mOppoTxRxCheck.clearRx0countList();
            OppoFastRecovery.this.mOppo5gCellBlacklistMonitor.clearCellidBlacklist();
            OppoFastRecovery.this.restorePreferredNetworkType();
        }

        private void networkRecover(long runCount, String reason) {
            Rlog.d(OppoFastRecovery.TAG, "networkRecover: " + this.mIsLogicNetOk + ", runCount:" + runCount);
            CellInfoLte cif = OppoFastRecovery.this.getLetCellInfo();
            if (runCount != this.mCheckingCount) {
                Rlog.w(OppoFastRecovery.TAG, "networkRecover not matchrunCount:" + runCount + " mCheckingCount:" + this.mCheckingCount);
                return;
            }
            if (!this.mIsLogicNetOk) {
                this.mIsLogicNetOk = true;
                resetData();
            }
            OppoFastRecovery.this.mOFastRecoveryHandler.obtainMessage(2, 0, 0, new MsgWapper(cif, reason)).sendToTarget();
        }

        private long getMobileTestValue(int index) {
            if (index == 1) {
                return 0;
            }
            if (index == 2) {
                return ((long) this.countxx) * 1500;
            }
            if (index == 3) {
                return ((long) this.countxx) * 5;
            }
            if (index != 4) {
                return 0;
            }
            return ((long) this.countxx) * 15;
        }

        private long getIfaceTcpRxPackets(String ifname) {
            try {
                return OppoFastRecovery.getStatsService().getIfaceStats(ifname, 4);
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "getIfaceTcpRxPackets failed: " + e.getMessage());
                return -1;
            }
        }

        private long getIfaceTcpTxPackets(String ifname) {
            try {
                return OppoFastRecovery.getStatsService().getIfaceStats(ifname, 5);
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "getIfaceTcpRxPackets get data failed!" + e.getMessage());
                return -1;
            }
        }

        private boolean checkVpnRunning() {
            Network[] networks = OppoFastRecovery.this.mConnectivityManager.getAllNetworks();
            if (networks == null) {
                return false;
            }
            boolean isvpn = false;
            for (Network network : networks) {
                NetworkCapabilities networkCapabilities = OppoFastRecovery.this.mConnectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null) {
                    Rlog.d(OppoFastRecovery.TAG, "network:" + network + " NetworkCapabilities:" + networkCapabilities);
                    if (networkCapabilities.hasTransport(4)) {
                        isvpn = true;
                    }
                }
            }
            return isvpn;
        }

        private boolean checkCellularNetwork() {
            Network network = OppoFastRecovery.this.mConnectivityManager.getActiveNetwork();
            if (network == null) {
                Rlog.d(OppoFastRecovery.TAG, "updateDataStat network is null");
                return false;
            }
            NetworkCapabilities networkcap = OppoFastRecovery.this.mConnectivityManager.getNetworkCapabilities(network);
            if (networkcap == null) {
                Rlog.d(OppoFastRecovery.TAG, "updateDataStat networkcap is null");
                return false;
            }
            Rlog.d(OppoFastRecovery.TAG, "checkCellularNetwork: networkcap:" + networkcap);
            if (networkcap.hasTransport(0) && networkcap.hasCapability(12)) {
                return true;
            }
            Rlog.d(OppoFastRecovery.TAG, "updateDataStat network type invalid!");
            return false;
        }

        private boolean recoveryPreCheck() {
            AbstractPhone tmpPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, OppoFastRecovery.this.mPhone);
            if (tmpPhone != null && tmpPhone.is_test_card()) {
                Rlog.d(OppoFastRecovery.TAG, "test card do not check!!!");
                return false;
            } else if (checkVpnRunning()) {
                return false;
            } else {
                return checkCellularNetwork();
            }
        }

        private String getActiveIfName() {
            LinkProperties linkProperties = OppoFastRecovery.this.mConnectivityManager.getActiveLinkProperties();
            if (linkProperties != null) {
                return linkProperties.getInterfaceName();
            }
            Rlog.d(OppoFastRecovery.TAG, "updateDataStat linkProperties is null");
            return null;
        }

        private void updateDataStat() {
            if (!updateDataStatInner()) {
                OppoFastRecovery.this.mOppoTxRxCheck.addNewRx0count(0);
                checkUpdateRx0Value(false);
            }
        }

        private boolean updateDataStatInner() {
            String str;
            boolean z;
            long mobileTcpRxPackets;
            CellInfoLte cellInfo = OppoFastRecovery.this.getLetCellInfo();
            updateCidInfo(cellInfo.mCid);
            this.mEnterLoopCount++;
            if (!recoveryPreCheck()) {
                return false;
            }
            String ifname = getActiveIfName();
            if (TextUtils.isEmpty(ifname)) {
                Rlog.d(OppoFastRecovery.TAG, "updateDataStat ifname is null");
                return false;
            }
            if (!this.mLastIfName.equals(ifname)) {
                Rlog.w(OppoFastRecovery.TAG, "ifname change: " + this.mLastIfName + " -> " + ifname);
                this.mLastIfName = ifname;
            }
            long mobileRxBytes = TrafficStats.getRxBytes(ifname);
            long mobileTxBytes = TrafficStats.getTxBytes(ifname);
            if (OppoFastRecovery.this.mIpDebug) {
                this.countxx++;
                mobileRxBytes = getMobileTestValue(1);
                mobileTxBytes = getMobileTestValue(2);
            }
            this.mRxBytes = -1;
            this.mTxBytes = -1;
            this.mTcpRxPackets = -1;
            this.mTcpTxPackets = -1;
            long j = this.mLastMobileRxBytes;
            if (j >= 0 && j <= mobileRxBytes) {
                long j2 = this.mLastMobileTxBytes;
                if (j2 >= 0 && j2 <= mobileTxBytes) {
                    this.mRxBytes = mobileRxBytes - j;
                    this.mTxBytes = mobileTxBytes - j2;
                }
            }
            this.mLastMobileRxBytes = mobileRxBytes;
            this.mLastMobileTxBytes = mobileTxBytes;
            long mobileTcpRxPackets2 = getIfaceTcpRxPackets(ifname);
            long mobileTcpTxPackets = getIfaceTcpTxPackets(ifname);
            if (mobileTcpTxPackets == -1) {
                str = OppoFastRecovery.TAG;
                z = false;
            } else if (mobileTcpRxPackets2 == -1) {
                str = OppoFastRecovery.TAG;
                z = false;
            } else {
                if (OppoFastRecovery.this.mIpDebug) {
                    mobileTcpRxPackets2 = getMobileTestValue(3);
                    mobileTcpTxPackets = getMobileTestValue(4);
                }
                long j3 = this.mLastTcpRxPackets;
                if (j3 < 0 || j3 > mobileTcpRxPackets2) {
                    mobileTcpRxPackets = mobileTcpRxPackets2;
                } else {
                    mobileTcpRxPackets = mobileTcpRxPackets2;
                    long j4 = this.mLastTcpTxPackets;
                    if (j4 >= 0 && j4 <= mobileTcpTxPackets) {
                        this.mTcpRxPackets = mobileTcpRxPackets - j3;
                        this.mTcpTxPackets = mobileTcpTxPackets - j4;
                    }
                }
                this.mLastTcpRxPackets = mobileTcpRxPackets;
                this.mLastTcpTxPackets = mobileTcpTxPackets;
                long j5 = this.mRealLoopCount;
                long j6 = this.mEnterLoopCount;
                if (j5 != j6 - 1) {
                    Rlog.d(OppoFastRecovery.TAG, "read loop count not match enter loop count");
                    this.mRealLoopCount = this.mEnterLoopCount;
                    return false;
                }
                this.mRealLoopCount = j6;
                Rlog.d(OppoFastRecovery.TAG, "updateDataStat: " + this.mRxBytes + "," + this.mTxBytes + "; " + this.mTcpRxPackets + "," + this.mTcpTxPackets + ",mIsLogicNetOk:" + this.mIsLogicNetOk + " ifname:" + ifname);
                if (this.mRxBytes < 0 || this.mTxBytes < 0) {
                    return false;
                }
                return networkCheckInner(cellInfo);
            }
            Rlog.e(str, "updateDataStat tcp message failed");
            return z;
        }

        private boolean networkCheckInner(CellInfoLte cellInfo) {
            int rx0Value;
            long rxSpeed = this.mRxBytes / OppoFastRecovery.this.mRecoveryConfig.mCheckIntval;
            long txSpeed = this.mTxBytes / OppoFastRecovery.this.mRecoveryConfig.mCheckIntval;
            long tcpTxSpeed = this.mTcpTxPackets / OppoFastRecovery.this.mRecoveryConfig.mCheckIntval;
            long tcpRxSpeed = this.mTcpRxPackets / OppoFastRecovery.this.mRecoveryConfig.mCheckIntval;
            boolean tcpInvalid = this.mTcpRxPackets == 0 && tcpTxSpeed >= OppoFastRecovery.this.mRecoveryConfig.mTcpMinTxPacketSpeed;
            boolean ipSpeedInvalid = rxSpeed < OppoFastRecovery.this.mRecoveryConfig.mRxSlowSpeed && txSpeed > OppoFastRecovery.this.mRecoveryConfig.mRxSlowSpeed && txSpeed < OppoFastRecovery.this.mRecoveryConfig.mTxSlowSpeed && ((long) OppoFastRecovery.this.mRecoveryConfig.mIpSpeedMultiple) * rxSpeed < txSpeed;
            boolean iprx0invalid = rxSpeed == 0 && txSpeed > OppoFastRecovery.this.mRecoveryConfig.mRxSlowSpeed && txSpeed < OppoFastRecovery.this.mRecoveryConfig.mTxSlowSpeed;
            boolean z = this.mIsLogicNetOk;
            if (z) {
                if (tcpInvalid) {
                    rx0Value = 2;
                } else if (iprx0invalid) {
                    rx0Value = 1;
                } else {
                    rx0Value = 0;
                }
                Object[] objArr = new Object[10];
                objArr[0] = Integer.valueOf(this.mIsLogicNetOk ? 1 : 0);
                objArr[1] = Long.valueOf(rxSpeed);
                objArr[2] = Long.valueOf(txSpeed);
                objArr[3] = Long.valueOf(tcpRxSpeed);
                objArr[4] = Long.valueOf(tcpTxSpeed);
                objArr[5] = Integer.valueOf(tcpInvalid ? 1 : 0);
                objArr[6] = Integer.valueOf(ipSpeedInvalid ? 1 : 0);
                objArr[7] = Integer.valueOf(iprx0invalid ? 1 : 0);
                objArr[8] = Integer.valueOf(rx0Value);
                objArr[9] = cellInfo;
                Rlog.d(OppoFastRecovery.TAG, String.format("networkCheckInner: logicOK:%d,rxSpeed:%6d,txSpeed:%6d,tcpRxSpeed:%2d,tcpTxSpeed:%2d,tcpInvalid:%d,ipInvalid:%d,ipRx0:%d,rx0Value:%d,%s", objArr));
                OppoFastRecovery.this.mOppoTxRxCheck.addNewRx0count(rx0Value);
                if (OppoFastRecovery.this.mOppoTxRxCheck.checkRx0Invalid()) {
                    Rlog.d(OppoFastRecovery.TAG, "networkCheckInner txrx check failed!");
                    checkUpdateRx0Value(true);
                    OppoFastRecovery.this.mOppoTxRxCheck.clearRx0countList();
                    OppoDorecoveryStatistics.updateLastResult(true);
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - OppoFastRecovery.this.mDorecoveryDoneTime < ((long) OppoFastRecovery.this.mRecoveryConfig.mTxrxCheckIntvl) * 1000) {
                        Rlog.d(OppoFastRecovery.TAG, "networkCheckInner dorecvery time check invalid! " + currentTime + ", " + OppoFastRecovery.this.mDorecoveryDoneTime);
                        return true;
                    }
                    tryDoRecovery(cellInfo, "txrxfail", true);
                    return true;
                }
                checkUpdateRx0Value(false);
                return true;
            }
            int i = 1;
            Object[] objArr2 = new Object[10];
            objArr2[0] = Integer.valueOf(z ? 1 : 0);
            objArr2[1] = Long.valueOf(rxSpeed);
            objArr2[2] = Long.valueOf(txSpeed);
            objArr2[3] = Long.valueOf(tcpRxSpeed);
            objArr2[4] = Long.valueOf(tcpTxSpeed);
            objArr2[5] = Integer.valueOf(tcpInvalid ? 1 : 0);
            objArr2[6] = Integer.valueOf(ipSpeedInvalid ? 1 : 0);
            if (!iprx0invalid) {
                i = 0;
            }
            objArr2[7] = Integer.valueOf(i);
            objArr2[8] = 0;
            objArr2[9] = cellInfo;
            Rlog.d(OppoFastRecovery.TAG, String.format("networkCheckInner: logicOK:%d,rxSpeed:%6d,txSpeed:%6d,tcpRxSpeed:%2d,tcpTxSpeed:%2d,tcpInvalid:%d,ipInvalid:%d,ipRx0:%d,rx0Value:%d,%s", objArr2));
            if (rxSpeed >= OppoFastRecovery.this.mRecoveryConfig.mRxThresholdSpeed && txSpeed >= OppoFastRecovery.this.mRecoveryConfig.mTxThresholdSpeed) {
                Rlog.d(OppoFastRecovery.TAG, "txrx match large data check, so network recovery");
                networkRecover(this.mCheckingCount, OppoFastRecovery.EXIT_RECOVERY_REASON_TXRX_OVER_FLOW);
                return false;
            } else if (this.mRxBytes <= 0 || this.mTxBytes <= 0) {
                return false;
            } else {
                Rlog.d(OppoFastRecovery.TAG, "updateDataStat network check start");
                startPingNetwork();
                return false;
            }
        }

        private void checkUpdateRx0Value(boolean forceUpdate) {
            this.mRx0ValueKpiCount++;
            if (this.mRx0ValueKpiCount >= OppoFastRecovery.this.mRecoveryConfig.mTxRxCheckCount || forceUpdate) {
                this.mRx0ValueKpiCount = 0;
                Rlog.d(OppoFastRecovery.TAG, "checkUpdateRx0Value calc value!! " + forceUpdate);
                updateR0CountStatistics(OppoFastRecovery.this.mOppoTxRxCheck.getRx0CountTotal());
            }
        }

        private void updateR0CountStatistics(int r0Count) {
            if (r0Count >= 20) {
                this.mR0CountGE20++;
            } else if (r0Count >= 16) {
                this.mR0CountGE16++;
            } else if (r0Count >= 12) {
                this.mR0CountGE12++;
            } else if (r0Count >= 8) {
                this.mR0CountGE8++;
            } else if (r0Count >= 4) {
                this.mR0CountGE4++;
            }
        }

        private void dnsFailCheck(long curTime) {
            CellInfoLte cellInfo = OppoFastRecovery.this.getLetCellInfo();
            Rlog.d(OppoFastRecovery.TAG, "dnsFailCheck : mDnsFailCount" + this.mDnsFailList.size() + ", cellInfo:" + cellInfo);
            if (dnsCountFailCheck(curTime) && this.mIsChecking) {
                this.mDnsFailList.clear();
                if (curTime - OppoFastRecovery.this.mDorecoveryDoneTime < ((long) OppoFastRecovery.this.mRecoveryConfig.mDnsCheckIntvl) * 1000) {
                    Rlog.d(OppoFastRecovery.TAG, "dnsFailCheck time invalid! " + curTime + ", " + OppoFastRecovery.this.mDorecoveryDoneTime);
                    return;
                }
                tryDoRecovery(cellInfo, "dnsFail", false);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isIn5g(Phone phone) {
            try {
                if (OppoFastRecovery.this.mIs5GNotRestrictDebug) {
                    Rlog.d(OppoFastRecovery.TAG, "isIn5g debug, return true");
                    return true;
                } else if (phone == null) {
                    Rlog.e(OppoFastRecovery.TAG, "mPhone is null!");
                    return false;
                } else {
                    int nrstate = OppoFastRecovery.this.get5GNrState(phone);
                    Rlog.d(OppoFastRecovery.TAG, "nrstate=" + nrstate);
                    return nrstate == 2 || nrstate == 3;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean checkDorecoveryEnv(String reason, CellInfoLte cellInfo) {
            boolean isLteOrNr = OppoFastRecovery.this.getIsLteOrNr();
            boolean isSSOk = OppoFastRecovery.this.isSignalStrengthValid();
            boolean isDataConnected = OppoFastRecovery.this.isDataConnect();
            boolean isDataEnabled = OppoFastRecovery.this.isMobileDataEnabled();
            Rlog.d(OppoFastRecovery.TAG, "checkDorecoveryEnv ,isLteOrNr:" + isLteOrNr + ",isSSOk:" + isSSOk + ",isDataConnected:" + isDataConnected + ",mIsLogicNetOk:" + this.mIsLogicNetOk + ",isDataEnabled:" + isDataEnabled + ",reason:" + reason);
            if (isLteOrNr && isSSOk && isDataConnected && isDataEnabled && this.mIsLogicNetOk) {
                return true;
            }
            Rlog.d(OppoFastRecovery.TAG, "tryDoRecovery condition is not meet");
            return false;
        }

        private void tryDoRecovery(CellInfoLte cellInfo, String reason, boolean isTxrx) {
            long curMs = System.currentTimeMillis();
            if (checkDorecoveryEnv(reason, cellInfo)) {
                Rlog.d(OppoFastRecovery.TAG, "tryDoRecovery: count:" + this.mDayCountList.size() + " curms:" + curMs + " reason:" + reason);
                OppoFastRecovery.access$508(OppoFastRecovery.this);
                if (isTxrx) {
                    OppoFastRecovery.access$708(OppoFastRecovery.this);
                } else {
                    OppoFastRecovery.access$608(OppoFastRecovery.this);
                }
                if (!checkCidDorecoveryInvalid(cellInfo.mCid)) {
                    Rlog.w(OppoFastRecovery.TAG, "checkCidDorecoveryInvalid invalid!!" + reason);
                } else if (!checkDayCount(curMs)) {
                    Rlog.w(OppoFastRecovery.TAG, "do recovery checkDayCount invalid!!");
                } else {
                    updateDayCount(curMs);
                    updateCidRecovery(cellInfo.mCid);
                    this.mIsLogicNetOk = false;
                    OppoFastRecovery.this.mOFastRecoveryHandler.obtainMessage(1, 0, 0, new MsgWapper(cellInfo, reason)).sendToTarget();
                }
            }
        }

        private void updateCidInfo(int cid) {
            if (cid != -1) {
                if (OppoFastRecovery.this.mOppo5gCellBlacklistMonitor.isCellidInBlacklist(cid) && (isIn5g(OppoFastRecovery.this.mPhone) || OppoFastRecovery.this.is5GSpecialTac())) {
                    OppoFastRecovery.this.mOFastRecoveryHandler.getPreferredNetworkType(true);
                    Rlog.d(OppoFastRecovery.TAG, "updateCidInfo cellid " + cid + " is in blacklist, so close 5g");
                }
                if (cid != this.mLastCid) {
                    Rlog.d(OppoFastRecovery.TAG, "update cid " + this.mLastCid + " -> " + cid);
                    OppoFastRecovery.this.mOppo5gCellBlacklistMonitor.updateCellidChange(this.mLastCid, cid, OppoFastRecovery.this.mIsSet5gRatHere);
                    this.mLastCid = cid;
                    this.mCidDoRecovery = false;
                }
            }
        }

        private boolean checkCidDorecoveryInvalid(int cid) {
            if (OppoFastRecovery.this.mCidCountDebug) {
                Rlog.d(OppoFastRecovery.TAG, "checkCidDorecoveryInvalid test mode return true");
                return true;
            } else if (cid == -1) {
                return false;
            } else {
                Rlog.d(OppoFastRecovery.TAG, "checkCidDorecoveryInvalid: cid:" + cid + ",mLastCid" + this.mLastCid + ",mCidDoRecovery:" + this.mCidDoRecovery);
                if (cid == this.mLastCid) {
                    return !this.mCidDoRecovery;
                }
                this.mLastCid = cid;
                this.mCidDoRecovery = false;
                return true;
            }
        }

        private void updateCidRecovery(int cid) {
            if (this.mCidDoRecovery) {
                Rlog.w(OppoFastRecovery.TAG, "updateCidRecovery invalid mCidDoRecovery");
            }
            this.mCidDoRecovery = true;
        }

        private boolean checkDayCount(long curTime) {
            if (OppoFastRecovery.this.mDayCountDebug) {
                Rlog.d(OppoFastRecovery.TAG, "checkDayCount test mode return true");
                return true;
            } else if (this.mDayCountList.size() >= OppoFastRecovery.this.mRecoveryConfig.mMaxCountPerDay && curTime - this.mDayCountList.get(0).longValue() <= this.ONE_DAY_MS) {
                return false;
            } else {
                return true;
            }
        }

        private void updateDayCount(long curTime) {
            if (this.mDayCountList.size() > OppoFastRecovery.this.mRecoveryConfig.mMaxCountPerDay) {
                Rlog.e(OppoFastRecovery.TAG, "fatal updateDayCount size invalid!" + this.mDayCountList.size() + "," + OppoFastRecovery.this.mRecoveryConfig.mMaxCountPerDay);
                return;
            }
            if (this.mDayCountList.size() == OppoFastRecovery.this.mRecoveryConfig.mMaxCountPerDay) {
                this.mDayCountList.remove(0);
            }
            this.mDayCountList.add(Long.valueOf(curTime));
        }
    }

    /* access modifiers changed from: private */
    public class OFastRecoveryHandler extends Handler {
        public static final String ACTION_STEP_AIR_PLANE = "AIR_PLANE_MODE";
        public static final String ACTION_STEP_CLEAN_DATA_CALL = "CLEAN_DATA_CALL";
        public static final String ACTION_STEP_CLOSE_5G = "disable_5g";
        public static final String ACTION_STEP_DETACH_ATTACH = "DETACH_ATTACH";
        public static final String ACTION_STEP_GET_DATACALL_LIST = "GET_DATA_CALL_LIST";
        public static final String ACTION_STEP_NONE = "none";
        public static final String ACTION_STEP_RM_5GNR = "RM_5GNR";
        public static final int EVENT_DO_NEXT_STEP = 3;
        public static final int EVENT_GET_5G_ANCHOR_CELLINFO_DONE = 11;
        public static final int EVENT_GET_PREFERRED_NETWORK_TYPE_DONE = 13;
        public static final int EVENT_PS_DETACH_ATTACH_DONE = 10;
        public static final int EVENT_RESET_RADIO_SMOOTH = 4;
        public static final int EVENT_SET_PREFERRED_NETWORKTYPE_DONE = 12;
        public static final int EVENT_START_DO_RECOVER = 1;
        public static final int EVENT_STOP_DO_RECOVER = 2;
        int EVENT_GET_5G_ANCHOR_CELLINFO = 53;
        private boolean isDoingRecovery = false;
        private ArrayList<ActionBase> mActionList = new ArrayList<>();
        private CellInfoLte mBeforeCellInfo = new CellInfoLte();
        private int mBeforeRealPfNWType = 0;
        private int mBeforeRsrp = 0;
        private int mBeforeUserPfNWType = 0;
        private String mCurStep = ACTION_STEP_NONE;
        private int mCurrentRealNWType = 0;
        private long mLastRadioPower = 0;
        private long mLastStartDorecoveryTime = 0;
        private String mReason = "";
        private int mStepCount = 0;

        /* access modifiers changed from: private */
        public abstract class ActionBase {
            public String mStepName;

            public abstract boolean action(Phone phone, CellInfoLte cellInfoLte);

            public abstract long getActionDelay();

            public abstract long getNoActionDelay();

            public abstract boolean needDoAction();

            private ActionBase() {
                this.mStepName = "";
            }
        }

        public OFastRecoveryHandler(Looper looper) {
            super(looper);
            this.mActionList.add(new GetDatacallListAction());
            this.mActionList.add(new Remove5gNrAction());
            this.mActionList.add(new Close5gAction());
            this.mActionList.add(new CleanAllConnectionsAction());
            this.mActionList.add(new DetachAttachAction());
            this.mActionList.add(new AirPlaneAction());
        }

        public void handleMessage(Message message) {
            Rlog.d(OppoFastRecovery.TAG, "OFastRecoveryHandler recv msg:" + message.what);
            try {
                int i = message.what;
                if (i == 1) {
                    doRecovery((MsgWapper) message.obj);
                } else if (i == 2) {
                    stopDoRecovery((MsgWapper) message.obj);
                } else if (i == 3) {
                    doRealRecovery(OppoFastRecovery.this.getLetCellInfo());
                } else if (i != 4) {
                    switch (i) {
                        case 11:
                            get5GAnchorCellInfoDone(message);
                            return;
                        case 12:
                            Rlog.d(OppoFastRecovery.TAG, "recv EVENT_SET_PREFERRED_NETWORKTYPE_DONE");
                            return;
                        case 13:
                            getPreferredNetworkTypeDone(message);
                            return;
                        default:
                            return;
                    }
                } else {
                    OppoFastRecovery.this.resetRadioSmooth();
                }
            } catch (Exception e) {
                Rlog.e(OppoFastRecovery.TAG, "OFastRecoveryHandler handle msg failed " + e.getMessage());
                e.printStackTrace();
            }
        }

        private Location getCurrLocation() {
            Location location = null;
            try {
                LocationManager locationManager = (LocationManager) OppoFastRecovery.this.mContext.getSystemService("location");
                if (locationManager.isProviderEnabled("network")) {
                    location = locationManager.getLastKnownLocation("network");
                } else {
                    Rlog.e(OppoFastRecovery.TAG, "getCurrLocation do not support gps");
                }
                Rlog.d(OppoFastRecovery.TAG, "getCurrLocation:" + location);
                return location;
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "updateRecoveryResultStatistics get gps info failed" + e.getMessage());
                return null;
            }
        }

        private void updateRecoveryResultStatistics(CellInfoLte cinfo, String exitReason, long currTime) {
            boolean succ;
            int rsrp = OppoFastRecovery.this.getSignalStrengthRsrp();
            if (OppoFastRecovery.EXIT_RECOVERY_REASON_NETWORK_ERCOVER.equals(exitReason) || OppoFastRecovery.EXIT_RECOVERY_REASON_TXRX_OVER_FLOW.equals(exitReason)) {
                OppoFastRecovery.access$908(OppoFastRecovery.this);
                succ = true;
            } else {
                succ = false;
            }
            String str = this.mReason;
            int i = this.mBeforeRsrp;
            int i2 = this.mBeforeCellInfo.mCid;
            int i3 = this.mBeforeCellInfo.mTac;
            int i4 = this.mBeforeCellInfo.mPci;
            int i5 = this.mBeforeUserPfNWType;
            int i6 = this.mBeforeRealPfNWType;
            int i7 = cinfo.mCid;
            int i8 = cinfo.mTac;
            int i9 = cinfo.mPci;
            OppoFastRecovery oppoFastRecovery = OppoFastRecovery.this;
            int userPreferredNetworkTypeCfg = oppoFastRecovery.getUserPreferredNetworkTypeCfg(oppoFastRecovery.mPhone);
            int i10 = this.mCurrentRealNWType;
            String str2 = this.mCurStep;
            long j = currTime - this.mLastStartDorecoveryTime;
            Location currLocation = getCurrLocation();
            OppoFastRecovery oppoFastRecovery2 = OppoFastRecovery.this;
            OppoDorecoveryStatistics.eventRecoveryResult(new OppoDorecoveryStatistics.PdpRecoveryStatisticsResult(false, str, i, i2, i3, i4, i5, i6, rsrp, i7, i8, i9, userPreferredNetworkTypeCfg, i10, str2, exitReason, succ, j, currLocation, oppoFastRecovery2.isSmart5gEnable(oppoFastRecovery2.mPhone), this.mBeforeCellInfo.mNrState));
        }

        private void stopDoRecovery(MsgWapper msgWapper) {
            Rlog.d(OppoFastRecovery.TAG, "stopDoRecovery " + this.isDoingRecovery + ", mCurStep" + this.mCurStep + ", exitReason:" + msgWapper.reason);
            if (this.isDoingRecovery) {
                long currTime = System.currentTimeMillis();
                OppoFastRecovery oppoFastRecovery = OppoFastRecovery.this;
                oppoFastRecovery.showToastMsg("stop recovery! " + msgWapper.reason);
                updateRecoveryResultStatistics(msgWapper.cellInfo, msgWapper.reason, currTime);
                OppoFastRecovery.this.mDorecoveryDoneTime = currTime;
                this.isDoingRecovery = false;
                this.mStepCount = 0;
                this.mCurStep = ACTION_STEP_NONE;
                removeMessages(3);
            }
        }

        private void doRecovery(MsgWapper msgWapper) {
            Rlog.d(OppoFastRecovery.TAG, "doRecovery " + OppoFastRecovery.this.mRecoveryCount + ", mCurStep" + this.mCurStep + ",reason:" + msgWapper.reason);
            if (this.isDoingRecovery) {
                Rlog.w(OppoFastRecovery.TAG, "doRecovery is running!");
                return;
            }
            OppoFastRecovery oppoFastRecovery = OppoFastRecovery.this;
            oppoFastRecovery.showToastMsg("start recovery! " + msgWapper.reason);
            this.mReason = msgWapper.reason;
            this.mCurStep = ACTION_STEP_NONE;
            this.isDoingRecovery = true;
            OppoFastRecovery.access$808(OppoFastRecovery.this);
            this.mBeforeCellInfo = msgWapper.cellInfo;
            this.mBeforeRsrp = OppoFastRecovery.this.getSignalStrengthRsrp();
            OppoFastRecovery oppoFastRecovery2 = OppoFastRecovery.this;
            this.mBeforeUserPfNWType = oppoFastRecovery2.getUserPreferredNetworkTypeCfg(oppoFastRecovery2.mPhone);
            this.mBeforeRealPfNWType = this.mCurrentRealNWType;
            this.mLastStartDorecoveryTime = System.currentTimeMillis();
            OppoFastRecovery.this.mIsIn5gAnchorCell = false;
            if (!OppoFastRecovery.this.mNetworkCheckHandler.isIn5g(OppoFastRecovery.this.mPhone)) {
                get5gAnchorCellInfo(OppoFastRecovery.this.mPhone);
            }
            doRealRecovery(msgWapper.cellInfo);
        }

        private void doRealRecovery(CellInfoLte cellinfo) {
            long delay;
            Rlog.d(OppoFastRecovery.TAG, "doRealRecovery " + this.mCurStep + ", " + this.mStepCount + "," + this.isDoingRecovery + " mRecoveryCount:" + OppoFastRecovery.this.mRecoveryCount + ", reason:" + this.mReason + ", cellinfo:" + cellinfo);
            if (!this.isDoingRecovery) {
                Rlog.d(OppoFastRecovery.TAG, "doRealRecovery stopped, curr step:" + this.mCurStep + " all count:" + OppoFastRecovery.this.mRecoveryCount);
            } else if (this.mStepCount < this.mActionList.size()) {
                ActionBase actionBase = this.mActionList.get(this.mStepCount);
                if (!actionBase.needDoAction() || !actionBase.action(OppoFastRecovery.this.mPhone, cellinfo)) {
                    delay = actionBase.getNoActionDelay();
                } else {
                    this.mCurStep = actionBase.mStepName;
                    delay = actionBase.getActionDelay();
                }
                this.mStepCount++;
                sendEmptyMessageDelayed(3, delay);
            } else if (this.mStepCount == this.mActionList.size()) {
                stopDoRecovery(new MsgWapper(cellinfo, OppoFastRecovery.EXIT_RECOVERY_REASON_TIMEOUT));
            }
        }

        private class GetDatacallListAction extends ActionBase {
            public GetDatacallListAction() {
                super();
                this.mStepName = OFastRecoveryHandler.ACTION_STEP_GET_DATACALL_LIST;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public long getNoActionDelay() {
                return 200;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public long getActionDelay() {
                return ((long) OppoFastRecovery.this.mRecoveryConfig.m5GActionIntvl) * 1000;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public boolean needDoAction() {
                return true;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public boolean action(Phone phone, CellInfoLte cellInfoLte) {
                boolean ret = OFastRecoveryHandler.this.getDatacallList(phone);
                if (ret) {
                    OppoFastRecovery.this.mNetworkCheckHandler.startPingNetwork();
                }
                return ret;
            }
        }

        private class Remove5gNrAction extends ActionBase {
            public Remove5gNrAction() {
                super();
                this.mStepName = OFastRecoveryHandler.ACTION_STEP_RM_5GNR;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public long getNoActionDelay() {
                return 0;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public long getActionDelay() {
                return ((long) OppoFastRecovery.this.mRecoveryConfig.m5GActionIntvl) * 1000;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public boolean needDoAction() {
                OFastRecoveryHandler oFastRecoveryHandler = OFastRecoveryHandler.this;
                return oFastRecoveryHandler.isNRConnected(OppoFastRecovery.this.mPhone);
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public boolean action(Phone phone, CellInfoLte cellInfoLte) {
                boolean ret = OFastRecoveryHandler.this.oemCloseNr(phone);
                if (ret) {
                    OppoFastRecovery.this.mNetworkCheckHandler.startPingNetwork();
                }
                return ret;
            }
        }

        private class Close5gAction extends ActionBase {
            public Close5gAction() {
                super();
                this.mStepName = OFastRecoveryHandler.ACTION_STEP_CLOSE_5G;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public long getNoActionDelay() {
                return 0;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public long getActionDelay() {
                return ((long) (OppoFastRecovery.this.mRecoveryConfig.mActionIntvl / 2)) * 1000;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public boolean needDoAction() {
                OFastRecoveryHandler oFastRecoveryHandler = OFastRecoveryHandler.this;
                return oFastRecoveryHandler.isIn5GNotRestricted(OppoFastRecovery.this.mPhone) || OppoFastRecovery.this.is5GSpecialTac();
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public boolean action(Phone phone, CellInfoLte cellInfoLte) {
                boolean ret = OFastRecoveryHandler.this.doClose5G(cellInfoLte);
                if (ret) {
                    OppoFastRecovery.this.mNetworkCheckHandler.startPingNetwork();
                }
                return ret;
            }
        }

        private class CleanAllConnectionsAction extends ActionBase {
            public CleanAllConnectionsAction() {
                super();
                this.mStepName = OFastRecoveryHandler.ACTION_STEP_CLEAN_DATA_CALL;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public long getNoActionDelay() {
                return 0;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public long getActionDelay() {
                return ((long) (OppoFastRecovery.this.mRecoveryConfig.mActionIntvl / 2)) * 1000;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public boolean needDoAction() {
                return true;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public boolean action(Phone phone, CellInfoLte cellInfoLte) {
                return OFastRecoveryHandler.this.oemSwithOffOnDataSwitch();
            }
        }

        private class DetachAttachAction extends ActionBase {
            public DetachAttachAction() {
                super();
                this.mStepName = OFastRecoveryHandler.ACTION_STEP_DETACH_ATTACH;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public long getNoActionDelay() {
                return 0;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public long getActionDelay() {
                return ((long) OppoFastRecovery.this.mRecoveryConfig.mActionIntvl) * 1000;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public boolean needDoAction() {
                return true;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public boolean action(Phone phone, CellInfoLte cellInfoLte) {
                return OFastRecoveryHandler.this.oemPsDetachAttach();
            }
        }

        private class AirPlaneAction extends ActionBase {
            public AirPlaneAction() {
                super();
                this.mStepName = OFastRecoveryHandler.ACTION_STEP_AIR_PLANE;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public long getNoActionDelay() {
                return 0;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public long getActionDelay() {
                return ((long) OppoFastRecovery.this.mRecoveryConfig.mActionIntvl) * 1000;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public boolean needDoAction() {
                return true;
            }

            @Override // com.oppo.internal.telephony.recovery.OppoFastRecovery.OFastRecoveryHandler.ActionBase
            public boolean action(Phone phone, CellInfoLte cellInfoLte) {
                return OFastRecoveryHandler.this.oemRadiopower();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean oemPsDetachAttach() {
            if (OppoFastRecovery.this.mRecoveryConfig.mActionByPass || !OppoFastRecovery.this.mRecoveryConfig.mDetachAttachEnable) {
                Rlog.d(OppoFastRecovery.TAG, "oemPsDetachAttach by pass mRecoveryConfig.mActionByPass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass + ", mRecoveryConfig.mDetachAttachEnable:" + OppoFastRecovery.this.mRecoveryConfig.mDetachAttachEnable);
                return false;
            }
            try {
                Rlog.d(OppoFastRecovery.TAG, "oemPsDetachAttach start");
                if (OppoFastRecovery.this.mPhone == null) {
                    Rlog.e(OppoFastRecovery.TAG, "mPhone is null!");
                    return false;
                }
                OppoFastRecovery.this.showToastMsg("oemPsDetachAttach start!");
                ReflectionHelper.callMethod(OppoFastRecovery.this.mPhone.mCi, "com.mediatek.internal.telephony.MtkRIL", "dataConnectionDetach", new Class[]{Integer.TYPE, Message.class}, new Object[]{0, null});
                ReflectionHelper.callMethod(OppoFastRecovery.this.mPhone.mCi, "com.mediatek.internal.telephony.MtkRIL", "dataConnectionAttach", new Class[]{Integer.TYPE, Message.class}, new Object[]{0, null});
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "oemPsDetachAttach failed!:" + e.getMessage());
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean oemSwithOffOnDataSwitch() {
            try {
                if (!OppoFastRecovery.this.mRecoveryConfig.mActionByPass) {
                    if (OppoFastRecovery.this.mRecoveryConfig.mCleanAllConnectionEnable) {
                        Rlog.d(OppoFastRecovery.TAG, "oemSwithOffOnDataSwitch start");
                        if (OppoFastRecovery.this.mPhone == null) {
                            Rlog.e(OppoFastRecovery.TAG, "mPhone is null!");
                            return false;
                        } else if (!OppoFastRecovery.this.mPhone.getDataEnabledSettings().isUserDataEnabled()) {
                            Rlog.d(OppoFastRecovery.TAG, "setUserDataEnabled user data is already disabled");
                            return false;
                        } else {
                            OppoFastRecovery.this.showToastMsg("oemSwithOffOnDataSwitch start!");
                            DcTracker dcTracker = OppoFastRecovery.this.mPhone.getDcTracker(1);
                            ReflectionHelper.callMethod(dcTracker, "com.android.internal.telephony.dataconnection.DcTracker", "cleanUpConnection", new Class[]{ApnContext.class}, new Object[]{((ConcurrentHashMap) ReflectionHelper.getDeclaredField(dcTracker, "com.android.internal.telephony.dataconnection.DcTracker", "mApnContexts")).get(ApnSetting.getApnTypeString(17))});
                            return true;
                        }
                    }
                }
                Rlog.d(OppoFastRecovery.TAG, "oemSwithOffOnDataSwitch by pass mRecoveryConfig.mActionByPass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass + ",mRecoveryConfig.mCleanAllConnectionEnable:" + OppoFastRecovery.this.mRecoveryConfig.mCleanAllConnectionEnable);
                return false;
            } catch (Exception e) {
                Rlog.e(OppoFastRecovery.TAG, "exception " + e.getMessage());
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean oemRadiopower() {
            if (OppoFastRecovery.this.mRecoveryConfig.mActionByPass || !OppoFastRecovery.this.mRecoveryConfig.mRadioPowerEnable) {
                Rlog.d(OppoFastRecovery.TAG, "oemRadiopower by pass mRecoveryConfig.mActionByPass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass + ",mRecoveryConfig.mRadioPowerEnable:" + OppoFastRecovery.this.mRecoveryConfig.mRadioPowerEnable);
                return false;
            }
            long currTime = System.currentTimeMillis();
            long j = this.mLastRadioPower;
            if (j == 0 || currTime - j > ((long) OppoFastRecovery.this.mRecoveryConfig.mRadioPowerIntvl) * 1000) {
                this.mLastRadioPower = currTime;
                try {
                    Rlog.d(OppoFastRecovery.TAG, "oemRadiopower start!");
                    OppoFastRecovery.this.showToastMsg("oemRadiopower start!");
                    for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                        Phone phone = PhoneFactory.getPhone(i);
                        if (phone != null) {
                            Rlog.d(OppoFastRecovery.TAG, "oemRadiopower sendRestartRadio !" + i);
                            startRadioSmooth(phone);
                            ReflectionHelper.callMethod(phone.getDcTracker(1), "com.android.internal.telephony.dataconnection.DcTracker", "sendRestartRadio", new Class[0], new Object[0]);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Rlog.e(OppoFastRecovery.TAG, "oemRadiopower call exception " + e.getMessage());
                }
                return true;
            }
            Rlog.w(OppoFastRecovery.TAG, "oemRadiopower last do is " + this.mLastRadioPower + ", and intvl:" + OppoFastRecovery.this.mRecoveryConfig.mRadioPowerIntvl);
            return false;
        }

        private void startRadioSmooth(Phone phone) {
            if (phone.getServiceStateTracker() != null) {
                Rlog.d(OppoFastRecovery.TAG, "setOemRadioReseting true");
                ((AbstractServiceStateTracker) OemTelephonyUtils.typeCasting(AbstractServiceStateTracker.class, phone.getServiceStateTracker())).setOemRadioReseting(true);
                removeMessages(4);
                sendEmptyMessageDelayed(4, 10000);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isNRConnected(Phone phone) {
            try {
                if (OppoFastRecovery.this.mRatDebug != 0) {
                    Rlog.d(OppoFastRecovery.TAG, "mRatDebug is not 0, " + OppoFastRecovery.this.mRatDebug);
                    return OppoFastRecovery.this.mRatDebug == 20;
                } else if (phone == null) {
                    Rlog.e(OppoFastRecovery.TAG, "mPhone is null!");
                    return false;
                } else {
                    int nrstate = OppoFastRecovery.this.get5GNrState(phone);
                    Rlog.d(OppoFastRecovery.TAG, "nrstate=" + nrstate);
                    return nrstate == 3;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isIn5GNotRestricted(Phone phone) {
            try {
                if (OppoFastRecovery.this.mIs5GNotRestrictDebug) {
                    Rlog.d(OppoFastRecovery.TAG, "mIs5GNotRestrictDebug, " + OppoFastRecovery.this.mIs5GNotRestrictDebug);
                    return true;
                } else if (phone == null) {
                    Rlog.e(OppoFastRecovery.TAG, "mPhone is null!");
                    return false;
                } else {
                    int nrstate = OppoFastRecovery.this.get5GNrState(phone);
                    Rlog.d(OppoFastRecovery.TAG, "nrstate=" + nrstate);
                    return nrstate == 2;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean oemCloseNr(Phone phone) {
            if (OppoFastRecovery.this.mRecoveryConfig.mActionByPass || !OppoFastRecovery.this.mRecoveryConfig.mRemove5GNrEnable) {
                Rlog.d(OppoFastRecovery.TAG, "oemCloseNr by pass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass + "," + OppoFastRecovery.this.mRecoveryConfig.mRemove5GNrEnable);
                return false;
            } else if (phone == null) {
                try {
                    Rlog.e(OppoFastRecovery.TAG, "mPhone is null!");
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    Rlog.e(OppoFastRecovery.TAG, "oemCloseNr failed:" + e.getMessage());
                    return true;
                }
            } else {
                Rlog.d(OppoFastRecovery.TAG, "oemCloseNr start");
                OppoFastRecovery.this.showToastMsg("oemCloseNr start!");
                OppoDataCommonUtils.oemCloseNr(phone);
                return true;
            }
        }

        public boolean isDoingRecovery() {
            return this.isDoingRecovery;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean getDatacallList(Phone phone) {
            if (OppoFastRecovery.this.mRecoveryConfig.mActionByPass || !OppoFastRecovery.this.mRecoveryConfig.mGetDataCallListEnable) {
                Rlog.d(OppoFastRecovery.TAG, "getDatacallList by pass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass + ", mRecoveryConfig.mCloset5gEnable:" + OppoFastRecovery.this.mRecoveryConfig.mGetDataCallListEnable);
                return false;
            } else if (phone == null) {
                try {
                    Rlog.w(OppoFastRecovery.TAG, "getDatacallList phone is null");
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    Rlog.e(OppoFastRecovery.TAG, "getDatacallList failed:" + e.getMessage());
                }
            } else {
                Rlog.d(OppoFastRecovery.TAG, "getDatacallList start");
                OppoFastRecovery.this.showToastMsg("getDatacallList start!");
                ((DataServiceManager) ReflectionHelper.getDeclaredField(phone.getDcTracker(1), "com.android.internal.telephony.dataconnection.DcTracker", "mDataServiceManager")).requestDataCallList(obtainMessage());
                return true;
            }
        }

        private void get5gAnchorCellInfo(Phone phone) {
            if (phone == null) {
                try {
                    Rlog.w(OppoFastRecovery.TAG, "get5gAnchorCellInfo phone is null");
                } catch (Exception e) {
                    e.printStackTrace();
                    Rlog.e(OppoFastRecovery.TAG, "disable5GEndc failed:" + e.getMessage());
                }
            } else {
                Rlog.d(OppoFastRecovery.TAG, "get5gAnchorCellInfo start!");
                phone.invokeOemRilRequestStrings(new String[]{"AT+ECELL=8", "+ECELL:"}, OppoFastRecovery.this.mOFastRecoveryHandler.obtainMessage(11));
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean doClose5G(CellInfoLte cellInfoLte) {
            Rlog.d(OppoFastRecovery.TAG, "doClose5G cellid:" + cellInfoLte);
            if (!close5gAction()) {
                return false;
            }
            OppoFastRecovery.this.mOppo5gCellBlacklistMonitor.setCellidToBlacklist(cellInfoLte.mCid);
            return true;
        }

        public boolean close5gAction() {
            if (OppoFastRecovery.this.mRecoveryConfig.mActionByPass || !OppoFastRecovery.this.mRecoveryConfig.mCloset5gEnable) {
                Rlog.d(OppoFastRecovery.TAG, "close5gAction by pass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass + ", mRecoveryConfig.mCloset5gEnable:" + OppoFastRecovery.this.mRecoveryConfig.mCloset5gEnable);
                return false;
            }
            try {
                if (OppoFastRecovery.this.mPhone == null) {
                    Rlog.w(OppoFastRecovery.TAG, "close5gAction phone is null");
                    return false;
                }
                Rlog.d(OppoFastRecovery.TAG, "close5gAction start!");
                OppoFastRecovery.this.showToastMsg("close5gAction start!");
                OppoFastRecovery.this.mPhone.mCi.setPreferredNetworkType(22, obtainMessage(12, null));
                OppoFastRecovery.this.mIsSet5gRatHere = true;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "close5gAction failed:" + e.getMessage());
            }
        }

        public void open5gAction() {
            if (OppoFastRecovery.this.mRecoveryConfig.mActionByPass) {
                Rlog.d(OppoFastRecovery.TAG, "open5gAction by pass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass);
                return;
            }
            try {
                if (OppoFastRecovery.this.mPhone == null) {
                    Rlog.w(OppoFastRecovery.TAG, "open5gAction phone is null");
                    return;
                }
                Rlog.d(OppoFastRecovery.TAG, "open5gAction start!");
                OppoFastRecovery.this.mPhone.mCi.setPreferredNetworkType(33, obtainMessage(12, null));
                OppoFastRecovery.this.mIsSet5gRatHere = true;
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "open5gAction failed:" + e.getMessage());
            }
        }

        public void restoreConfigPreferredNetworkType() {
            if (OppoFastRecovery.this.mRecoveryConfig.mActionByPass) {
                Rlog.d(OppoFastRecovery.TAG, "restoreConfigPreferredNetworkType by pass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass);
                return;
            }
            try {
                if (OppoFastRecovery.this.mPhone == null) {
                    Rlog.w(OppoFastRecovery.TAG, "restoreConfigPreferredNetworkType phone is null");
                } else if (!OppoFastRecovery.this.mIsSet5gRatHere) {
                    Rlog.d(OppoFastRecovery.TAG, "restoreConfigPreferredNetworkType no set 5g here!");
                } else {
                    int mNetworkMode = OppoFastRecovery.this.getUserPreferredNetworkTypeCfg(OppoFastRecovery.this.mPhone);
                    Rlog.d(OppoFastRecovery.TAG, "restoreConfigPreferredNetworkType set userconfig :" + mNetworkMode);
                    OppoFastRecovery.this.mPhone.mCi.setPreferredNetworkType(mNetworkMode, obtainMessage(12, null));
                    OppoFastRecovery.this.mIsSet5gRatHere = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "restoreConfigPreferredNetworkType failed:" + e.getMessage());
            }
        }

        public void getPreferredNetworkType(boolean doAction) {
            try {
                if (OppoFastRecovery.this.mPhone == null) {
                    Rlog.w(OppoFastRecovery.TAG, "getPreferredNetworkType phone is null");
                    return;
                }
                Rlog.d(OppoFastRecovery.TAG, "getPreferredNetworkType set userconfig");
                OppoFastRecovery.this.mPhone.mCi.getPreferredNetworkType(obtainMessage(13, Boolean.valueOf(doAction)));
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "get preferred type failed!");
            }
        }

        private void getPreferredNetworkTypeDone(Message msg) {
            int type;
            try {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    type = ((int[]) ar.result)[0];
                } else {
                    type = 7;
                }
                boolean action = ((Boolean) ar.userObj).booleanValue();
                boolean in5gRat = OppoFastRecovery.this.isNrNetworkModeType(type);
                Rlog.d(OppoFastRecovery.TAG, "getPreferredNetworkTypeDone return :" + type + " action:" + action + " isnr:" + in5gRat);
                this.mCurrentRealNWType = type;
                if (action && in5gRat) {
                    close5gAction();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "getPreferredNetworkTypeDone error:" + e.getMessage());
            }
        }

        private void get5GAnchorCellInfoDone(Message message) {
            try {
                AsyncResult ar = (AsyncResult) message.obj;
                if (ar.exception == null) {
                    String[] eCellInfo = (String[]) ar.result;
                    if (eCellInfo == null) {
                        Rlog.d(OppoFastRecovery.TAG, "get5GAnchorCellInfoDone eCellInfo null");
                        return;
                    }
                    boolean z = false;
                    try {
                        if (eCellInfo[0].split(",").length > 1) {
                            String is5gCapQueried = eCellInfo[0].split(",")[16];
                            String is5gPdcpBearer = eCellInfo[0].split(",")[17];
                            OppoFastRecovery oppoFastRecovery = OppoFastRecovery.this;
                            if ("1".equals(is5gCapQueried) || "1".equals(is5gPdcpBearer)) {
                                z = true;
                            }
                            oppoFastRecovery.mIsIn5gAnchorCell = z;
                            Rlog.d(OppoFastRecovery.TAG, "get5GAnchorCellInfoDone is5gCapQueried=" + is5gCapQueried + " is5gPdcpBearer=" + is5gPdcpBearer + " mIsIn5gAnchorCell=" + OppoFastRecovery.this.mIsIn5gAnchorCell);
                            return;
                        }
                        Rlog.d(OppoFastRecovery.TAG, "get5GAnchorCellInfoDone no LTE cell attached");
                    } catch (Exception e) {
                        Rlog.e(OppoFastRecovery.TAG, "handleMessage get5GAnchorCellInfoDone exception:" + e.getMessage());
                    }
                } else {
                    Rlog.e(OppoFastRecovery.TAG, "get5GAnchorCellInfoDone get exception:" + ar.exception.getMessage());
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "get5GAnchorCellInfoDone exception:" + e2.getMessage());
            }
        }
    }

    public void restorePreferredNetworkType() {
        this.mOFastRecoveryHandler.restoreConfigPreferredNetworkType();
    }

    public boolean isCellinBlackList(int cellid) {
        Oppo5GCellBlacklistMonitor oppo5GCellBlacklistMonitor = this.mOppo5gCellBlacklistMonitor;
        if (oppo5GCellBlacklistMonitor != null) {
            return oppo5GCellBlacklistMonitor.isCellidInBlacklist(cellid);
        }
        return false;
    }

    public boolean getIsSet5gRatHere() {
        return this.mIsSet5gRatHere;
    }
}

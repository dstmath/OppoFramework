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
import com.android.internal.telephony.AbstractPhone;
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
    private static final int ACTION_LAST_TIME_NETWORK_CHECK = 30;
    private static final int CODE_DNS_TIMEOUT = 255;
    private static final String DEFAULT_DEBUG_CONFIG = "0,0,0,0,0,0,1,0,0";
    private static final String EXIT_RECOREY_REASON_NETWORK_ERCOVER = "network-recover";
    private static final String EXIT_RECOREY_REASON_NEW_RECOVERY = "new-recovery";
    private static final String EXIT_RECOREY_REASON_STOP_CHECK = "stop-check";
    private static final String PERSIST_DEBUG_KEY = "persist.oppo.pdp_recovery.debug";
    private static final String TAG = "OppoFastRecovery";
    private static OppoFastRecovery mInstance = null;
    private static final Object mLock = new Object();
    private static INetworkStatsService sStatsService;
    /* access modifiers changed from: private */
    public int m5GReocveryNetworkOk = 0;
    /* access modifiers changed from: private */
    public boolean mCidCountDebug = false;
    /* access modifiers changed from: private */
    public ConnectivityManager mConnectivityManager;
    public Context mContext;
    public boolean mDataConnected = false;
    private int mDataPhoneId = 0;
    /* access modifiers changed from: private */
    public boolean mDayCountDebug = false;
    /* access modifiers changed from: private */
    public int mDnsCheckCount = 0;
    /* access modifiers changed from: private */
    public boolean mDnsDebug = false;
    /* access modifiers changed from: private */
    public int mDnsTestCount = 0;
    /* access modifiers changed from: private */
    public long mDorecoveryDoneTime = 0;
    /* access modifiers changed from: private */
    public int mEnter5GRecoveryCount = 0;
    /* access modifiers changed from: private */
    public int mEnter5GTryRecoveryCount = 0;
    /* access modifiers changed from: private */
    public int mEnterTryDorecoveryCount = 0;
    /* access modifiers changed from: private */
    public boolean mIpDebug = false;
    /* access modifiers changed from: private */
    public boolean mIs5GNotRestrictDebug = false;
    public boolean mIsAirplane = false;
    public boolean mIsDataEnabled = false;
    /* access modifiers changed from: private */
    public boolean mIsIn5gAnchorCell = false;
    /* access modifiers changed from: private */
    public boolean mIsScreenOn = true;
    /* access modifiers changed from: private */
    public boolean mIsSet5gRatHere = false;
    public boolean mIsVoiceOn = false;
    public boolean mIsWifiOn = false;
    /* access modifiers changed from: private */
    public long mLastActionTime = 0;
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
    /* access modifiers changed from: private */
    public NetworkCheckHandler mNetworkCheckHandler;
    /* access modifiers changed from: private */
    public int mNetworkRecoveryCount = 0;
    /* access modifiers changed from: private */
    public OFastRecoveryHandler mOFastRecoveryHandler;
    /* access modifiers changed from: private */
    public Oppo5GCellBlacklistMonitor mOppo5gCellBlacklistMonitor;
    /* access modifiers changed from: private */
    public OppoTxRxCheck mOppoTxRxCheck;
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
    /* access modifiers changed from: private */
    public boolean mPingCheck = true;
    /* access modifiers changed from: private */
    public int mRatDebug = 0;
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
    /* access modifiers changed from: private */
    public int mRecoveryCount = 0;
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
    /* access modifiers changed from: private */
    public int mTxrxCheckCount = 0;
    private int mUserCfgRatDebug = 0;

    static /* synthetic */ int access$1008(OppoFastRecovery x0) {
        int i = x0.mEnter5GTryRecoveryCount;
        x0.mEnter5GTryRecoveryCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$108(OppoFastRecovery x0) {
        int i = x0.mDnsTestCount;
        x0.mDnsTestCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$1108(OppoFastRecovery x0) {
        int i = x0.mEnter5GRecoveryCount;
        x0.mEnter5GRecoveryCount = i + 1;
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: private */
    public void broadcastReceiverAction(Context context, Intent intent) {
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
        } else if (c == 5) {
            Rlog.d(TAG, "recv ACTION_DATE_CHANGED ->");
            this.mNetworkCheckHandler.sendEmptyMessage(12);
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
    public boolean isDataConnect() {
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

    private static class CellInfoLte {
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

    private static class MsgWapper {
        public CellInfoLte cellInfo;
        public String reason;

        public MsgWapper(CellInfoLte cellInfo2, String reason2) {
            this.cellInfo = cellInfo2;
            this.reason = reason2;
        }
    }

    private CellIdentityLte getLteCellIdentity(Phone phone) {
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
        if (regInfo != null) {
            CellIdentity id = regInfo.getCellIdentity();
            if (id.getType() == 3) {
                return (CellIdentityLte) id;
            }
        }
        return null;
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
    public boolean getIsLteOrNr() {
        try {
            if (this.mRatDebug != 0) {
                Rlog.d(TAG, "getIsLteOrNr test mode " + this.mRatDebug);
                return isPsTech(this.mRatDebug);
            } else if (this.mPhone == null) {
                Rlog.e(TAG, "mPhone is null!");
                return false;
            } else {
                ServiceState ss = this.mPhone.getServiceState();
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
            if (this.mPhone != null) {
                return this.mPhone.getServiceState().getRilDataRadioTechnology();
            }
            Rlog.e(TAG, "mPhone is null!");
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public int getSignalStrengthRsrp() {
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
    public boolean isSignalStrengthValid() {
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
    public boolean isNrNetworkModeType(int type) {
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
    public boolean is5GSpecialTac() {
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
    public boolean isSmart5gEnable(Phone phone) {
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
    public int get5GNrState(Phone phone) {
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
        private ArrayList<Long> m5GDayCountList = new ArrayList<>();
        private long mCheckingCount = 0;
        private boolean mCidDoRecovery = false;
        private ArrayList<Long> mDayCountList = new ArrayList<>();
        private ArrayList<DnsFailInfo> mDnsFailList = new ArrayList<>();
        private boolean mEnter5GFlag = false;
        private boolean mIsChecking = false;
        private boolean mIsLastCellNetActive = false;
        private boolean mIsLogicNetOk = true;
        private int mLastCid = -1;
        private long mLastMobileRxBytes = -1;
        private long mLastMobileTxBytes = -1;
        private long mLastTcpRxPackets = -1;
        private long mLastTcpTxPackets = -1;
        private int mR0CountGE12 = 0;
        private int mR0CountGE16 = 0;
        private int mR0CountGE20 = 0;
        private int mR0CountGE4 = 0;
        private int mR0CountGE8 = 0;
        private int mRx0Count = 0;
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
                                    networkRecover(runCount);
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
                                } catch (Exception e) {
                                    e = e;
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
            } catch (Exception e2) {
                e = e2;
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "NetworkCheckHandler handle msg failed" + e.getMessage());
            }
        }

        private void resetKpiStatistics() {
            int unused = OppoFastRecovery.this.mEnterTryDorecoveryCount = 0;
            int unused2 = OppoFastRecovery.this.mRecoveryCount = 0;
            int unused3 = OppoFastRecovery.this.mNetworkRecoveryCount = 0;
            int unused4 = OppoFastRecovery.this.mDnsCheckCount = 0;
            int unused5 = OppoFastRecovery.this.mTxrxCheckCount = 0;
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
            this.mRx0Count = 0;
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
            if (this.mIsChecking) {
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

        private void stateChange() {
            boolean needcheck = needCheck();
            CellInfoLte cif = OppoFastRecovery.this.getLetCellInfo();
            Rlog.d(OppoFastRecovery.TAG, "stateChange " + this.mIsChecking + " needCheck:" + needcheck + ", cif:" + cif);
            if (this.mIsChecking && !needcheck) {
                resetData();
                this.mIsChecking = false;
                this.mIsLogicNetOk = true;
                OppoFastRecovery.this.mOFastRecoveryHandler.obtainMessage(2, 0, 0, new MsgWapper(cif, OppoFastRecovery.EXIT_RECOREY_REASON_STOP_CHECK)).sendToTarget();
                removeMessages(1);
            } else if (!this.mIsChecking && needcheck) {
                resetData();
                this.mIsChecking = true;
                this.mIsLogicNetOk = true;
                sendEmptyMessageDelayed(1, OppoFastRecovery.this.mRecoveryConfig.mCheckIntval * 1000);
            }
        }

        private void networkChangeProc() {
            long curTime = System.currentTimeMillis();
            boolean isCellNetActive = false;
            NetworkInfo ni = OppoFastRecovery.this.mConnectivityManager.getActiveNetworkInfo();
            if (ni != null && ni.isConnected() && ni.getType() == 0) {
                isCellNetActive = true;
            }
            if (this.mIsLastCellNetActive != isCellNetActive) {
                this.mIsLastCellNetActive = isCellNetActive;
            }
            Rlog.d(OppoFastRecovery.TAG, "networkChangeProc " + this.mIsChecking + isCellNetActive + this.mIsLogicNetOk);
            if (this.mIsChecking && curTime - OppoFastRecovery.this.mLastActionTime < ((long) OppoFastRecovery.this.mRecoveryConfig.mActionIntvl) * 1000 && !this.mIsLogicNetOk && isCellNetActive) {
                Rlog.d(OppoFastRecovery.TAG, "networkChangeProc cell network active");
                startPingNetwork();
            }
        }

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
            OppoFastRecovery.this.mOFastRecoveryHandler.obtainMessage(2, 0, 0, new MsgWapper(cif, OppoFastRecovery.EXIT_RECOREY_REASON_STOP_CHECK)).sendToTarget();
            OppoFastRecovery.this.mOppoTxRxCheck.clearRx0countList();
            OppoFastRecovery.this.mOppo5gCellBlacklistMonitor.clearCellidBlacklist();
            OppoFastRecovery.this.restorePreferredNetworkType();
        }

        private void networkRecover(long runCount) {
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
            OppoFastRecovery.this.mOFastRecoveryHandler.obtainMessage(2, 0, 0, new MsgWapper(cif, OppoFastRecovery.EXIT_RECOREY_REASON_NETWORK_ERCOVER)).sendToTarget();
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

        private void updateDataStat() {
            CellInfoLte cellInfo = OppoFastRecovery.this.getLetCellInfo();
            updateCidInfo(cellInfo.mCid);
            AbstractPhone tmpPhone = (AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, OppoFastRecovery.this.mPhone);
            if (tmpPhone == null || !tmpPhone.is_test_card()) {
                Network network = OppoFastRecovery.this.mConnectivityManager.getActiveNetwork();
                if (network == null) {
                    Rlog.d(OppoFastRecovery.TAG, "updateDataStat network is null");
                    return;
                }
                NetworkCapabilities networkcap = OppoFastRecovery.this.mConnectivityManager.getNetworkCapabilities(network);
                if (networkcap == null) {
                    Rlog.d(OppoFastRecovery.TAG, "updateDataStat networkcap is null");
                    return;
                }
                if (networkcap.hasTransport(0)) {
                    if (networkcap.hasCapability(12)) {
                        LinkProperties linkProperties = OppoFastRecovery.this.mConnectivityManager.getActiveLinkProperties();
                        if (linkProperties == null) {
                            Rlog.d(OppoFastRecovery.TAG, "updateDataStat linkProperties is null");
                            return;
                        }
                        String ifname = linkProperties.getInterfaceName();
                        if (TextUtils.isEmpty(ifname)) {
                            Rlog.d(OppoFastRecovery.TAG, "updateDataStat ifname is null");
                            return;
                        }
                        long mobileRxBytes = TrafficStats.getMobileRxBytes();
                        long mobileTxBytes = TrafficStats.getMobileTxBytes();
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
                        long mobileTcpRxPackets = getIfaceTcpRxPackets(ifname);
                        long mobileTcpTxPackets = getIfaceTcpTxPackets(ifname);
                        if (mobileTcpTxPackets != -1) {
                            if (mobileTcpRxPackets != -1) {
                                if (OppoFastRecovery.this.mIpDebug) {
                                    mobileTcpRxPackets = getMobileTestValue(3);
                                    mobileTcpTxPackets = getMobileTestValue(4);
                                }
                                long j3 = this.mLastTcpRxPackets;
                                if (j3 >= 0 && j3 <= mobileTcpRxPackets) {
                                    long j4 = this.mLastTcpTxPackets;
                                    if (j4 >= 0 && j4 <= mobileTcpTxPackets) {
                                        this.mTcpRxPackets = mobileTcpRxPackets - j3;
                                        this.mTcpTxPackets = mobileTcpTxPackets - j4;
                                    }
                                }
                                this.mLastTcpRxPackets = mobileTcpRxPackets;
                                this.mLastTcpTxPackets = mobileTcpTxPackets;
                                Rlog.d(OppoFastRecovery.TAG, "updateDataStat: " + this.mRxBytes + "," + this.mTxBytes + "; " + this.mTcpRxPackets + "," + this.mTcpTxPackets + ",mIsLogicNetOk:" + this.mIsLogicNetOk + " ifname:" + ifname);
                                long j5 = this.mRxBytes;
                                if (j5 >= 0) {
                                    long j6 = this.mTxBytes;
                                    if (j6 < 0) {
                                        return;
                                    }
                                    if (this.mIsLogicNetOk) {
                                        networkCheckInner(cellInfo);
                                        return;
                                    } else if (j5 > 0 && j6 > 0) {
                                        Rlog.d(OppoFastRecovery.TAG, "updateDataStat network check start");
                                        startPingNetwork();
                                        return;
                                    } else {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            }
                        }
                        Rlog.e(OppoFastRecovery.TAG, "updateDataStat tcp message failed");
                        return;
                    }
                }
                Rlog.d(OppoFastRecovery.TAG, "updateDataStat network type invalid:" + networkcap.getTransportTypes() + ", " + networkcap.getCapabilities());
                return;
            }
            Rlog.d(OppoFastRecovery.TAG, "test card do not check!!!");
        }

        /* JADX WARN: Type inference failed for: r3v2, types: [boolean, int] */
        /* JADX WARN: Type inference failed for: r3v3 */
        /* JADX WARN: Type inference failed for: r3v7 */
        private void networkCheckInner(CellInfoLte cellInfo) {
            String str;
            String str2;
            String str3;
            String str4;
            String str5;
            boolean ipSpeedInvalid;
            String str6;
            int rx0Value;
            ? r3;
            long rxSpeed = this.mRxBytes / OppoFastRecovery.this.mRecoveryConfig.mCheckIntval;
            long txSpeed = this.mTxBytes / OppoFastRecovery.this.mRecoveryConfig.mCheckIntval;
            long tcpTxSpeed = this.mTcpTxPackets / OppoFastRecovery.this.mRecoveryConfig.mCheckIntval;
            boolean tcpInvalid = this.mTcpRxPackets == 0 && tcpTxSpeed >= OppoFastRecovery.this.mRecoveryConfig.mTcpMinTxPacketSpeed;
            boolean ipSpeedInvalid2 = rxSpeed < OppoFastRecovery.this.mRecoveryConfig.mRxSlowSpeed && txSpeed > OppoFastRecovery.this.mRecoveryConfig.mRxSlowSpeed && txSpeed < OppoFastRecovery.this.mRecoveryConfig.mTxSlowSpeed && ((long) OppoFastRecovery.this.mRecoveryConfig.mIpSpeedMultiple) * rxSpeed < txSpeed;
            boolean iprx0invalid = rxSpeed == 0 && txSpeed > OppoFastRecovery.this.mRecoveryConfig.mRxSlowSpeed && txSpeed < OppoFastRecovery.this.mRecoveryConfig.mTxSlowSpeed;
            if (tcpInvalid) {
                str4 = ",mRx0Count:";
                str = ",cellInfo:";
                str5 = ",tcpInvalid:";
                ipSpeedInvalid = ipSpeedInvalid2;
                str2 = OppoFastRecovery.TAG;
                str3 = ", rx0Value:";
            } else if (iprx0invalid) {
                str4 = ",mRx0Count:";
                str = ",cellInfo:";
                str5 = ",tcpInvalid:";
                ipSpeedInvalid = ipSpeedInvalid2;
                str2 = OppoFastRecovery.TAG;
                str3 = ", rx0Value:";
            } else {
                OppoFastRecovery.this.mOppoTxRxCheck.addNewRx0count(0);
                checkUpdateRx0Value(false);
                this.mRx0Count = 0;
                this.mEnter5GFlag = false;
                Rlog.d(OppoFastRecovery.TAG, "networkCheckInner: rxSpeed:" + rxSpeed + ",txSpeed" + txSpeed + ",tcpTxSpeed" + tcpTxSpeed + ",tcpInvalid:" + tcpInvalid + ",ipSpeedInvalid:" + ipSpeedInvalid2 + ",mRx0Count:" + this.mRx0Count + ", rx0Value:" + 0 + ",cellInfo:" + cellInfo);
                return;
            }
            if (tcpInvalid) {
                str6 = str3;
                this.mRx0Count += 2;
                rx0Value = 2;
            } else {
                str6 = str3;
                this.mRx0Count++;
                rx0Value = 1;
            }
            if ((this.mRx0Count == OppoFastRecovery.this.mRecoveryConfig.m5GRx0Count || this.mRx0Count == OppoFastRecovery.this.mRecoveryConfig.m5GRx0Count + 1) && !this.mEnter5GFlag) {
                this.mEnter5GFlag = true;
                try5GDoRecovery(cellInfo);
            }
            Rlog.d(str2, "networkCheckInner: rxSpeed:" + rxSpeed + ",txSpeed" + txSpeed + ",tcpTxSpeed" + tcpTxSpeed + str5 + tcpInvalid + ",ipSpeedInvalid:" + ipSpeedInvalid + str4 + this.mRx0Count + str6 + rx0Value + str + cellInfo);
            OppoFastRecovery.this.mOppoTxRxCheck.addNewRx0count(rx0Value);
            if (OppoFastRecovery.this.mOppoTxRxCheck.checkRx0Invalid()) {
                Rlog.d(str2, "networkCheckInner txrx check failed!");
                checkUpdateRx0Value(true);
                OppoFastRecovery.this.mOppoTxRxCheck.clearRx0countList();
                this.mRx0Count = 0;
                this.mEnter5GFlag = false;
                long currentTime = System.currentTimeMillis();
                if (currentTime - OppoFastRecovery.this.mDorecoveryDoneTime < ((long) OppoFastRecovery.this.mRecoveryConfig.mTxrxCheckIntvl) * 1000) {
                    Rlog.d(str2, "networkCheckInner dorecvery time check invalid! " + currentTime + ", " + OppoFastRecovery.this.mDorecoveryDoneTime);
                } else {
                    tryDoRecovery(cellInfo, "txrxfail", true);
                }
                r3 = 0;
            } else {
                r3 = 0;
                checkUpdateRx0Value(false);
            }
            if (this.mRx0Count >= OppoFastRecovery.this.mRecoveryConfig.mRx0CountThreshold) {
                this.mRx0Count = r3;
                this.mEnter5GFlag = r3;
            }
        }

        private void checkUpdateRx0Value(boolean forceUpdate) {
            this.mRx0ValueKpiCount++;
            if (this.mRx0ValueKpiCount >= OppoFastRecovery.this.mRecoveryConfig.mRx0CountInvalidTh || forceUpdate) {
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
            if (!isLteOrNr || !isSSOk || !isDataConnected || !isDataEnabled || !this.mIsLogicNetOk) {
                Rlog.d(OppoFastRecovery.TAG, "tryDoRecovery condition is not meet");
                return false;
            } else if (checkCidDorecoveryInvalid(cellInfo.mCid)) {
                return true;
            } else {
                Rlog.w(OppoFastRecovery.TAG, "checkCidDorecoveryInvalid invalid!!" + reason);
                return false;
            }
        }

        private void try5GDorecoveryInner(CellInfoLte cellInfo) {
            long curMs = System.currentTimeMillis();
            OppoFastRecovery.access$1008(OppoFastRecovery.this);
            update5GDayCount(curMs);
            if (OppoFastRecovery.this.mOFastRecoveryHandler.isNRConnected(OppoFastRecovery.this.mPhone)) {
                OppoFastRecovery.access$1108(OppoFastRecovery.this);
                OppoFastRecovery.this.mOFastRecoveryHandler.oemCloseNr(OppoFastRecovery.this.mPhone);
            }
        }

        private void try5GDoRecovery(CellInfoLte cellInfo) {
            long curMs = System.currentTimeMillis();
            Rlog.d(OppoFastRecovery.TAG, "try5GDoRecovery start! cellInfo:" + cellInfo + ",curMs:" + curMs);
            if (checkDorecoveryEnv("5Grecover", cellInfo)) {
                if (!isIn5g(OppoFastRecovery.this.mPhone)) {
                    Rlog.d(OppoFastRecovery.TAG, "is not in 5G");
                } else if (!check5GDayCount(curMs)) {
                    Rlog.w(OppoFastRecovery.TAG, "do recovery check5GDayCount invalid!!");
                } else {
                    try5GDorecoveryInner(cellInfo);
                }
            }
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
                if (!checkDayCount(curMs)) {
                    Rlog.w(OppoFastRecovery.TAG, "do recovery checkDayCount invalid!!");
                    return;
                }
                updateDayCount(curMs);
                updateCidRecovery(cellInfo.mCid);
                this.mIsLogicNetOk = false;
                OppoFastRecovery.this.mOFastRecoveryHandler.obtainMessage(1, 0, 0, new MsgWapper(cellInfo, reason)).sendToTarget();
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

        private boolean check5GDayCount(long curTime) {
            if (OppoFastRecovery.this.mDayCountDebug) {
                Rlog.d(OppoFastRecovery.TAG, "check5GDayCount test mode return true");
                return true;
            } else if (this.m5GDayCountList.size() >= OppoFastRecovery.this.mRecoveryConfig.m5GRecoveryMaxCount && curTime - this.m5GDayCountList.get(0).longValue() <= this.ONE_DAY_MS) {
                return false;
            } else {
                return true;
            }
        }

        private void update5GDayCount(long curTime) {
            if (this.m5GDayCountList.size() > OppoFastRecovery.this.mRecoveryConfig.m5GRecoveryMaxCount) {
                Rlog.e(OppoFastRecovery.TAG, "fatal update5GDayCount size invalid!" + this.m5GDayCountList.size() + "," + OppoFastRecovery.this.mRecoveryConfig.m5GRecoveryMaxCount);
                return;
            }
            if (this.m5GDayCountList.size() == OppoFastRecovery.this.mRecoveryConfig.m5GRecoveryMaxCount) {
                this.m5GDayCountList.remove(0);
            }
            this.m5GDayCountList.add(Long.valueOf(curTime));
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
        public static final int EVENT_NETWORKCHECK_DELAY = 5;
        public static final int EVENT_PS_DETACH_ATTACH_DONE = 10;
        public static final int EVENT_SET_PREFERRED_NETWORKTYPE_DONE = 12;
        public static final int EVENT_START_DO_RECOVER = 1;
        public static final int EVENT_STOP_DO_RECOVER = 2;
        int EVENT_GET_5G_ANCHOR_CELLINFO = 53;
        private boolean isDoingRecovery = false;
        private CellInfoLte mBeforeCellInfo = new CellInfoLte();
        private int mBeforeRealPfNWType = 0;
        private int mBeforeRsrp = 0;
        private int mBeforeUserPfNWType = 0;
        private String mCurStep = ACTION_STEP_NONE;
        private int mCurrentRealNWType = 0;
        private long mLastRadioPower = 0;
        private long mLastStartDorecoveryTime = 0;
        private long mLastStepTime = 0;
        private String mReason = "";

        public OFastRecoveryHandler(Looper looper) {
            super(looper);
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
                } else if (i != 5) {
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
                    eventNetworkCheckDelay();
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
            int rsrp = OppoFastRecovery.this.getSignalStrengthRsrp();
            if (OppoFastRecovery.EXIT_RECOREY_REASON_NETWORK_ERCOVER.equals(exitReason)) {
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
                OppoDorecoveryStatistics.eventRecoveryResult(new OppoDorecoveryStatistics.PdpRecoveryStatisticsResult(false, str, i, i2, i3, i4, i5, i6, rsrp, i7, i8, i9, userPreferredNetworkTypeCfg, i10, str2, "", true, j, currLocation, oppoFastRecovery2.isSmart5gEnable(oppoFastRecovery2.mPhone), this.mBeforeCellInfo.mNrState));
                OppoFastRecovery.access$908(OppoFastRecovery.this);
                return;
            }
            String str3 = this.mReason;
            int i11 = this.mBeforeRsrp;
            int i12 = this.mBeforeCellInfo.mCid;
            int i13 = this.mBeforeCellInfo.mTac;
            int i14 = this.mBeforeCellInfo.mPci;
            int i15 = this.mBeforeUserPfNWType;
            int i16 = this.mBeforeRealPfNWType;
            int i17 = cinfo.mCid;
            int i18 = cinfo.mTac;
            int i19 = cinfo.mPci;
            OppoFastRecovery oppoFastRecovery3 = OppoFastRecovery.this;
            int userPreferredNetworkTypeCfg2 = oppoFastRecovery3.getUserPreferredNetworkTypeCfg(oppoFastRecovery3.mPhone);
            int i20 = this.mCurrentRealNWType;
            String str4 = this.mCurStep;
            long j2 = currTime - this.mLastStartDorecoveryTime;
            Location currLocation2 = getCurrLocation();
            OppoFastRecovery oppoFastRecovery4 = OppoFastRecovery.this;
            OppoDorecoveryStatistics.eventRecoveryResult(new OppoDorecoveryStatistics.PdpRecoveryStatisticsResult(false, str3, i11, i12, i13, i14, i15, i16, rsrp, i17, i18, i19, userPreferredNetworkTypeCfg2, i20, str4, exitReason, false, j2, currLocation2, oppoFastRecovery4.isSmart5gEnable(oppoFastRecovery4.mPhone), this.mBeforeCellInfo.mNrState));
        }

        private void stopDoRecoveryAfterLastAction(CellInfoLte cinfo, String exitReason, long currTime) {
            long j = this.mLastStepTime;
            if (j != 0 && currTime - j < 30000) {
                updateRecoveryResultStatistics(cinfo, exitReason, currTime);
            }
            this.mLastStepTime = 0;
            removeMessages(5);
        }

        private void stopDoRecovery(MsgWapper msgWapper) {
            long currTime = System.currentTimeMillis();
            Rlog.d(OppoFastRecovery.TAG, "stopDoRecovery " + this.isDoingRecovery + ", mCurStep" + this.mCurStep + ", exitReason:" + msgWapper.reason);
            if (!this.isDoingRecovery) {
                stopDoRecoveryAfterLastAction(msgWapper.cellInfo, msgWapper.reason, currTime);
                return;
            }
            updateRecoveryResultStatistics(msgWapper.cellInfo, msgWapper.reason, currTime);
            long unused = OppoFastRecovery.this.mDorecoveryDoneTime = currTime;
            this.isDoingRecovery = false;
            this.mCurStep = ACTION_STEP_NONE;
            removeMessages(3);
            this.mLastStepTime = 0;
            removeMessages(5);
        }

        private void doRecovery(MsgWapper msgWapper) {
            Rlog.d(OppoFastRecovery.TAG, "doRecovery " + OppoFastRecovery.this.mRecoveryCount + ", mCurStep" + this.mCurStep + ",reason:" + msgWapper.reason);
            if (this.isDoingRecovery) {
                Rlog.w(OppoFastRecovery.TAG, "doRecovery is running!");
                return;
            }
            stopDoRecoveryAfterLastAction(msgWapper.cellInfo, OppoFastRecovery.EXIT_RECOREY_REASON_NEW_RECOVERY, System.currentTimeMillis());
            this.mReason = msgWapper.reason;
            this.mCurStep = ACTION_STEP_NONE;
            this.isDoingRecovery = true;
            OppoFastRecovery.access$808(OppoFastRecovery.this);
            this.mBeforeCellInfo = msgWapper.cellInfo;
            this.mBeforeRsrp = OppoFastRecovery.this.getSignalStrengthRsrp();
            OppoFastRecovery oppoFastRecovery = OppoFastRecovery.this;
            this.mBeforeUserPfNWType = oppoFastRecovery.getUserPreferredNetworkTypeCfg(oppoFastRecovery.mPhone);
            this.mBeforeRealPfNWType = this.mCurrentRealNWType;
            this.mLastStartDorecoveryTime = System.currentTimeMillis();
            doRealRecovery(msgWapper.cellInfo);
        }

        private void doRealRecovery(CellInfoLte cellinfo) {
            Rlog.d(OppoFastRecovery.TAG, "doRealRecovery " + this.mCurStep + ", " + this.isDoingRecovery + " mRecoveryCount:" + OppoFastRecovery.this.mRecoveryCount + ", reason:" + this.mReason + ", cellinfo:" + cellinfo);
            if (!this.isDoingRecovery) {
                Rlog.d(OppoFastRecovery.TAG, "doRealRecovery stopped, curr step:" + this.mCurStep + " all count:" + OppoFastRecovery.this.mRecoveryCount);
                return;
            }
            String str = this.mCurStep;
            char c = 65535;
            switch (str.hashCode()) {
                case -1848617037:
                    if (str.equals(ACTION_STEP_GET_DATACALL_LIST)) {
                        c = 1;
                        break;
                    }
                    break;
                case -1618903159:
                    if (str.equals(ACTION_STEP_CLOSE_5G)) {
                        c = 3;
                        break;
                    }
                    break;
                case 3387192:
                    if (str.equals(ACTION_STEP_NONE)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1724536637:
                    if (str.equals(ACTION_STEP_CLEAN_DATA_CALL)) {
                        c = 4;
                        break;
                    }
                    break;
                case 1764124667:
                    if (str.equals(ACTION_STEP_AIR_PLANE)) {
                        c = 6;
                        break;
                    }
                    break;
                case 1774139505:
                    if (str.equals(ACTION_STEP_DETACH_ATTACH)) {
                        c = 5;
                        break;
                    }
                    break;
                case 2054686586:
                    if (str.equals(ACTION_STEP_RM_5GNR)) {
                        c = 2;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    this.mCurStep = ACTION_STEP_GET_DATACALL_LIST;
                    get5gAnchorCellInfo(OppoFastRecovery.this.mPhone);
                    getDatacallList(OppoFastRecovery.this.mPhone);
                    sendEmptyMessageDelayed(3, ((long) OppoFastRecovery.this.mRecoveryConfig.m5GActionIntvl) * 1000);
                    return;
                case 1:
                    if (isNRConnected(OppoFastRecovery.this.mPhone)) {
                        oemCloseNr(OppoFastRecovery.this.mPhone);
                        this.mCurStep = ACTION_STEP_RM_5GNR;
                        sendEmptyMessageDelayed(3, (long) (OppoFastRecovery.this.mRecoveryConfig.m5GActionIntvl * 1000));
                        return;
                    } else if (isIn5GNotRestricted(OppoFastRecovery.this.mPhone) || OppoFastRecovery.this.is5GSpecialTac()) {
                        doClose5G(cellinfo);
                        this.mCurStep = ACTION_STEP_CLOSE_5G;
                        sendEmptyMessageDelayed(3, (long) ((OppoFastRecovery.this.mRecoveryConfig.mActionIntvl / 2) * 1000));
                        return;
                    } else {
                        this.mCurStep = ACTION_STEP_CLEAN_DATA_CALL;
                        oemSwithOffOnDataSwitch();
                        sendEmptyMessageDelayed(3, ((long) (OppoFastRecovery.this.mRecoveryConfig.mActionIntvl / 2)) * 1000);
                        return;
                    }
                case 2:
                    doClose5G(cellinfo);
                    this.mCurStep = ACTION_STEP_CLOSE_5G;
                    sendEmptyMessageDelayed(3, (long) ((OppoFastRecovery.this.mRecoveryConfig.mActionIntvl / 2) * 1000));
                    return;
                case 3:
                    this.mCurStep = ACTION_STEP_CLEAN_DATA_CALL;
                    oemSwithOffOnDataSwitch();
                    sendEmptyMessageDelayed(3, ((long) (OppoFastRecovery.this.mRecoveryConfig.mActionIntvl / 2)) * 1000);
                    return;
                case 4:
                    this.mCurStep = ACTION_STEP_DETACH_ATTACH;
                    oemPsDetachAttach();
                    sendEmptyMessageDelayed(3, ((long) OppoFastRecovery.this.mRecoveryConfig.mActionIntvl) * 1000);
                    return;
                case 5:
                    this.mCurStep = ACTION_STEP_AIR_PLANE;
                    if (!OppoFastRecovery.this.isInVoiceCall()) {
                        oemRadiopower();
                    }
                    sendEmptyMessage(3);
                    return;
                case 6:
                    this.isDoingRecovery = false;
                    this.mLastStepTime = System.currentTimeMillis();
                    long unused = OppoFastRecovery.this.mDorecoveryDoneTime = System.currentTimeMillis();
                    sendEmptyMessageDelayed(5, 30000);
                    Rlog.d(OppoFastRecovery.TAG, "doRealRecovery over current count:" + OppoFastRecovery.this.mRecoveryCount);
                    return;
                default:
                    return;
            }
        }

        private void eventNetworkCheckDelay() {
            CellInfoLte cinfo = OppoFastRecovery.this.getLetCellInfo();
            int rsrp = OppoFastRecovery.this.getSignalStrengthRsrp();
            long currTime = System.currentTimeMillis();
            this.mLastStepTime = 0;
            removeMessages(5);
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
            OppoDorecoveryStatistics.eventRecoveryResult(new OppoDorecoveryStatistics.PdpRecoveryStatisticsResult(false, str, i, i2, i3, i4, i5, i6, rsrp, i7, i8, i9, userPreferredNetworkTypeCfg, i10, str2, "", true, j, currLocation, oppoFastRecovery2.isSmart5gEnable(oppoFastRecovery2.mPhone), this.mBeforeCellInfo.mNrState));
        }

        private void oemPsDetachAttach() {
            if (OppoFastRecovery.this.mRecoveryConfig.mActionByPass || !OppoFastRecovery.this.mRecoveryConfig.mDetachAttachEnable) {
                Rlog.d(OppoFastRecovery.TAG, "oemPsDetachAttach by pass mRecoveryConfig.mActionByPass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass + ", mRecoveryConfig.mDetachAttachEnable:" + OppoFastRecovery.this.mRecoveryConfig.mDetachAttachEnable);
                return;
            }
            try {
                Rlog.d(OppoFastRecovery.TAG, "oemPsDetachAttach start");
                if (OppoFastRecovery.this.mPhone == null) {
                    Rlog.e(OppoFastRecovery.TAG, "mPhone is null!");
                    return;
                }
                ReflectionHelper.callMethod(OppoFastRecovery.this.mPhone.mCi, "com.mediatek.internal.telephony.MtkRIL", "dataConnectionDetach", new Class[]{Integer.TYPE, Message.class}, new Object[]{0, null});
                ReflectionHelper.callMethod(OppoFastRecovery.this.mPhone.mCi, "com.mediatek.internal.telephony.MtkRIL", "dataConnectionAttach", new Class[]{Integer.TYPE, Message.class}, new Object[]{0, null});
                long unused = OppoFastRecovery.this.mLastActionTime = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "oemPsDetachAttach failed!:" + e.getMessage());
            }
        }

        private void oemSwithOffOnDataSwitch() {
            try {
                if (!OppoFastRecovery.this.mRecoveryConfig.mActionByPass) {
                    if (OppoFastRecovery.this.mRecoveryConfig.mCleanAllConnectionEnable) {
                        Rlog.d(OppoFastRecovery.TAG, "oemSwithOffOnDataSwitch start");
                        if (OppoFastRecovery.this.mPhone == null) {
                            Rlog.e(OppoFastRecovery.TAG, "mPhone is null!");
                            return;
                        } else if (!OppoFastRecovery.this.mPhone.getDataEnabledSettings().isUserDataEnabled()) {
                            Rlog.d(OppoFastRecovery.TAG, "setUserDataEnabled user data is already disabled");
                            return;
                        } else {
                            DcTracker dcTracker = OppoFastRecovery.this.mPhone.getDcTracker(1);
                            ReflectionHelper.callMethod(dcTracker, "com.android.internal.telephony.dataconnection.DcTracker", "cleanUpConnection", new Class[]{ApnContext.class}, new Object[]{((ConcurrentHashMap) ReflectionHelper.getDeclaredField(dcTracker, "com.android.internal.telephony.dataconnection.DcTracker", "mApnContexts")).get(ApnSetting.getApnTypeString(17))});
                            long unused = OppoFastRecovery.this.mLastActionTime = System.currentTimeMillis();
                            return;
                        }
                    }
                }
                Rlog.d(OppoFastRecovery.TAG, "oemSwithOffOnDataSwitch by pass mRecoveryConfig.mActionByPass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass + ",mRecoveryConfig.mCleanAllConnectionEnable:" + OppoFastRecovery.this.mRecoveryConfig.mCleanAllConnectionEnable);
            } catch (Exception e) {
                Rlog.e(OppoFastRecovery.TAG, "exception " + e.getMessage());
            }
        }

        private void oemRadiopower() {
            if (OppoFastRecovery.this.mRecoveryConfig.mActionByPass || !OppoFastRecovery.this.mRecoveryConfig.mRadioPowerEnable) {
                Rlog.d(OppoFastRecovery.TAG, "oemRadiopower by pass mRecoveryConfig.mActionByPass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass + ",mRecoveryConfig.mRadioPowerEnable:" + OppoFastRecovery.this.mRecoveryConfig.mRadioPowerEnable);
                return;
            }
            long currTime = System.currentTimeMillis();
            long j = this.mLastRadioPower;
            if (j == 0 || currTime - j > ((long) OppoFastRecovery.this.mRecoveryConfig.mRadioPowerIntvl) * 1000) {
                this.mLastRadioPower = currTime;
                try {
                    Rlog.d(OppoFastRecovery.TAG, "oemRadiopower start!");
                    for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                        Phone phone = PhoneFactory.getPhone(i);
                        if (phone != null) {
                            Rlog.d(OppoFastRecovery.TAG, "oemRadiopower sendRestartRadio !" + i);
                            ReflectionHelper.callMethod(phone.getDcTracker(1), "com.android.internal.telephony.dataconnection.DcTracker", "sendRestartRadio", new Class[0], new Object[0]);
                            long unused = OppoFastRecovery.this.mLastActionTime = System.currentTimeMillis();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Rlog.e(OppoFastRecovery.TAG, "oemRadiopower call exception " + e.getMessage());
                }
            } else {
                Rlog.w(OppoFastRecovery.TAG, "oemRadiopower last do is " + this.mLastRadioPower + ", and intvl:" + OppoFastRecovery.this.mRecoveryConfig.mRadioPowerIntvl);
            }
        }

        /* access modifiers changed from: private */
        public boolean isNRConnected(Phone phone) {
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
        public void oemCloseNr(Phone phone) {
            if (OppoFastRecovery.this.mRecoveryConfig.mActionByPass || !OppoFastRecovery.this.mRecoveryConfig.mRemove5GNrEnable) {
                Rlog.d(OppoFastRecovery.TAG, "oemCloseNr by pass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass + "," + OppoFastRecovery.this.mRecoveryConfig.mRemove5GNrEnable);
            } else if (phone == null) {
                try {
                    Rlog.e(OppoFastRecovery.TAG, "mPhone is null!");
                } catch (Exception e) {
                    e.printStackTrace();
                    Rlog.e(OppoFastRecovery.TAG, "oemCloseNr failed:" + e.getMessage());
                }
            } else {
                Rlog.d(OppoFastRecovery.TAG, "oemCloseNr start");
                OppoDataCommonUtils.oemCloseNr(phone);
                long unused = OppoFastRecovery.this.mLastActionTime = System.currentTimeMillis();
            }
        }

        public boolean isDoingRecovery() {
            return this.isDoingRecovery;
        }

        private void getDatacallList(Phone phone) {
            if (OppoFastRecovery.this.mRecoveryConfig.mActionByPass || !OppoFastRecovery.this.mRecoveryConfig.mGetDataCallListEnable) {
                Rlog.d(OppoFastRecovery.TAG, "getDatacallList by pass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass + ", mRecoveryConfig.mCloset5gEnable:" + OppoFastRecovery.this.mRecoveryConfig.mGetDataCallListEnable);
            } else if (phone == null) {
                try {
                    Rlog.w(OppoFastRecovery.TAG, "getDatacallList phone is null");
                } catch (Exception e) {
                    e.printStackTrace();
                    Rlog.e(OppoFastRecovery.TAG, "getDatacallList failed:" + e.getMessage());
                }
            } else {
                Rlog.d(OppoFastRecovery.TAG, "getDatacallList start");
                ((DataServiceManager) ReflectionHelper.getDeclaredField(phone.getDcTracker(1), "com.android.internal.telephony.dataconnection.DcTracker", "mDataServiceManager")).requestDataCallList(obtainMessage());
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

        private void doClose5G(CellInfoLte cellInfoLte) {
            Rlog.d(OppoFastRecovery.TAG, "doClose5G cellid:" + cellInfoLte);
            close5gAction();
            OppoFastRecovery.this.mOppo5gCellBlacklistMonitor.setCellidToBlacklist(cellInfoLte.mCid);
        }

        public void close5gAction() {
            if (OppoFastRecovery.this.mRecoveryConfig.mActionByPass || !OppoFastRecovery.this.mRecoveryConfig.mCloset5gEnable) {
                Rlog.d(OppoFastRecovery.TAG, "close5gAction by pass:" + OppoFastRecovery.this.mRecoveryConfig.mActionByPass + ", mRecoveryConfig.mCloset5gEnable:" + OppoFastRecovery.this.mRecoveryConfig.mCloset5gEnable);
                return;
            }
            try {
                if (OppoFastRecovery.this.mPhone == null) {
                    Rlog.w(OppoFastRecovery.TAG, "close5gAction phone is null");
                    return;
                }
                Rlog.d(OppoFastRecovery.TAG, "close5gAction start!");
                OppoFastRecovery.this.mPhone.mCi.setPreferredNetworkType(22, obtainMessage(12, null));
                boolean unused = OppoFastRecovery.this.mIsSet5gRatHere = true;
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
                boolean unused = OppoFastRecovery.this.mIsSet5gRatHere = true;
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
                    boolean unused = OppoFastRecovery.this.mIsSet5gRatHere = false;
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
            Exception e;
            String is5gAnchor;
            try {
                AsyncResult ar = (AsyncResult) message.obj;
                if (ar.exception == null) {
                    String[] eCellInfo = (String[]) ar.result;
                    if (eCellInfo == null) {
                        Rlog.d(OppoFastRecovery.TAG, "get5GAnchorCellInfoDone eCellInfo null");
                        return;
                    }
                    try {
                        if (eCellInfo[0].split(",").length > 1) {
                            is5gAnchor = eCellInfo[0].split(",")[16];
                            try {
                                boolean unused = OppoFastRecovery.this.mIsIn5gAnchorCell = "1".equals(is5gAnchor);
                                Rlog.d(OppoFastRecovery.TAG, "get5GAnchorCellInfoDone mIsIn5gAnchorCell:" + OppoFastRecovery.this.mIsIn5gAnchorCell);
                            } catch (Exception e2) {
                                e = e2;
                                Rlog.e(OppoFastRecovery.TAG, "handleMessage get5GAnchorCellInfoDone exception:" + e.getMessage());
                            }
                        } else {
                            Rlog.d(OppoFastRecovery.TAG, "get5GAnchorCellInfoDone no LTE cell attached");
                        }
                    } catch (Exception e3) {
                        e = e3;
                        is5gAnchor = "";
                        Rlog.e(OppoFastRecovery.TAG, "handleMessage get5GAnchorCellInfoDone exception:" + e.getMessage());
                    }
                } else {
                    Rlog.e(OppoFastRecovery.TAG, "get5GAnchorCellInfoDone get exception:" + ar.exception.getMessage());
                }
            } catch (Exception e4) {
                e4.printStackTrace();
                Rlog.e(OppoFastRecovery.TAG, "get5GAnchorCellInfoDone exception:" + e4.getMessage());
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

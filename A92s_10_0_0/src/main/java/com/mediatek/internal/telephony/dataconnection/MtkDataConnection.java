package com.mediatek.internal.telephony.dataconnection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.INetworkManagementEventObserver;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.ProxyInfo;
import android.net.StringNetworkSpecifier;
import android.os.AsyncResult;
import android.os.Build;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.system.OsConstants;
import android.telephony.CarrierConfigManager;
import android.telephony.DataFailCause;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.telephony.data.DataCallResponse;
import android.telephony.data.DataProfile;
import android.text.TextUtils;
import android.util.Pair;
import android.util.StatsLog;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.TelephonyDevController;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.ApnSettingUtils;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.dataconnection.DataServiceManager;
import com.android.internal.telephony.dataconnection.DcController;
import com.android.internal.telephony.dataconnection.DcNetworkAgent;
import com.android.internal.telephony.dataconnection.DcTesterFailBringUpAll;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.util.AsyncChannel;
import com.android.server.net.BaseNetworkObserver;
import com.mediatek.internal.telephony.EndcBearController;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.MtkTelephonyDevController;
import com.mediatek.internal.telephony.OpTelephonyCustomizationFactoryBase;
import com.mediatek.internal.telephony.OpTelephonyCustomizationUtils;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.telephony.internal.telephony.vsim.ExternalSimConstants;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import vendor.mediatek.hardware.mtkradioex.V1_0.MtkApnTypes;

public class MtkDataConnection extends DataConnection {
    /* access modifiers changed from: private */
    public static final boolean DBG = Build.IS_DEBUGGABLE;
    static final int EVENT_ADDRESS_REMOVED = 262173;
    static final int EVENT_FALLBACK_RETRY_CONNECTION = 262175;
    static final int EVENT_GET_DATA_CALL_LIST = 262177;
    static final int EVENT_IPV4_ADDRESS_REMOVED = 262171;
    static final int EVENT_IPV6_ADDRESS_REMOVED = 262172;
    static final int EVENT_IPV6_ADDRESS_UPDATED = 262176;
    static final int EVENT_MTK_NR_STATE_CHANGED = 262179;
    public static final int EVENT_SET_LINGERING_TIME = 532484;
    static final int EVENT_UPDATE_NETWORKAGENT_SSC_MODE3 = 262178;
    static final int EVENT_VOICE_CALL = 262174;
    private static final String INTENT_RETRY_ALARM_TAG = "tag";
    private static final String INTENT_RETRY_ALARM_WHAT = "what";
    private static final String PROP_RIL_DATA_CDMA_IMSI = "vendor.ril.data.cdma_imsi";
    private static final String PROP_RIL_DATA_GSM_IMSI = "vendor.ril.data.gsm_imsi";
    private static final int RA_GET_IPV6_VALID_FAIL = -1000;
    private static final int RA_INITIAL_FAIL = -1;
    private static final int RA_REFRESH_FAIL = -2;
    /* access modifiers changed from: private */
    public static final boolean VDBG = Build.IS_ENG;
    private static Method methodQueueOrSendMessage;
    private static final String[] sAuTelstraOperator = {"50501", "50571", "50572", "50511"};
    /* access modifiers changed from: private */
    public String mActionRetry;
    private AlarmManager mAlarmManager;
    private INetworkManagementEventObserver mAlertObserver;
    /* access modifiers changed from: private */
    public IDataConnectionExt mDataConnectionExt = null;
    protected DcFailCauseManager mDcFcMgr;
    /* access modifiers changed from: private */
    public AddressInfo mGlobalV6AddrInfo;
    /* access modifiers changed from: private */
    public BroadcastReceiver mIntentReceiver;
    /* access modifiers changed from: private */
    public String mInterfaceName;
    /* access modifiers changed from: private */
    public boolean mIsInVoiceCall;
    /* access modifiers changed from: private */
    public boolean mIsOp20;
    /* access modifiers changed from: private */
    public boolean mIsSetupDataCallByCi;
    /* access modifiers changed from: private */
    public boolean mIsSupportConcurrent;
    private NetworkCapabilities mNetworkCapabilities;
    private final INetworkManagementService mNetworkManager;
    private int mRat;
    /* access modifiers changed from: private */
    public int mRetryCount;
    private SubscriptionController mSubController = SubscriptionController.getInstance();
    /* access modifiers changed from: private */
    public TelephonyDevController mTelDevController;
    private OpTelephonyCustomizationFactoryBase mTelephonyCustomizationFactory = null;
    /* access modifiers changed from: private */
    public long mValid;

    static /* synthetic */ int access$16472(MtkDataConnection x0, int x1) {
        int i = x0.mDisabledApnTypeBitMask & x1;
        x0.mDisabledApnTypeBitMask = i;
        return i;
    }

    static /* synthetic */ int access$18676(MtkDataConnection x0, int x1) {
        int i = x0.mDisabledApnTypeBitMask | x1;
        x0.mDisabledApnTypeBitMask = i;
        return i;
    }

    static /* synthetic */ int access$20708(MtkDataConnection x0) {
        int i = x0.mRetryCount;
        x0.mRetryCount = i + 1;
        return i;
    }

    static {
        Class<?> clz = null;
        try {
            clz = Class.forName("android.net.NetworkAgent");
        } catch (Exception e) {
            Rlog.d("MtkDataConnection", e.toString());
        }
        if (clz != null) {
            try {
                methodQueueOrSendMessage = clz.getDeclaredMethod("queueOrSendMessage", Integer.TYPE, Integer.TYPE, Integer.TYPE);
                methodQueueOrSendMessage.setAccessible(true);
            } catch (Exception e2) {
                Rlog.d("MtkDataConnection", e2.toString());
            }
        }
        sCmdToString = new String[36];
        sCmdToString[0] = "EVENT_CONNECT";
        sCmdToString[1] = "EVENT_SETUP_DATA_CONNECTION_DONE";
        sCmdToString[3] = "EVENT_DEACTIVATE_DONE";
        sCmdToString[4] = "EVENT_DISCONNECT";
        sCmdToString[5] = "EVENT_RIL_CONNECTED";
        sCmdToString[6] = "EVENT_DISCONNECT_ALL";
        sCmdToString[7] = "EVENT_DATA_STATE_CHANGED";
        sCmdToString[8] = "EVENT_TEAR_DOWN_NOW";
        sCmdToString[9] = "EVENT_LOST_CONNECTION";
        sCmdToString[11] = "EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED";
        sCmdToString[12] = "EVENT_DATA_CONNECTION_ROAM_ON";
        sCmdToString[13] = "EVENT_DATA_CONNECTION_ROAM_OFF";
        sCmdToString[14] = "EVENT_BW_REFRESH_RESPONSE";
        sCmdToString[15] = "EVENT_DATA_CONNECTION_VOICE_CALL_STARTED";
        sCmdToString[16] = "EVENT_DATA_CONNECTION_VOICE_CALL_ENDED";
        sCmdToString[17] = "EVENT_DATA_CONNECTION_OVERRIDE_CHANGED";
        sCmdToString[18] = "EVENT_KEEPALIVE_STATUS";
        sCmdToString[19] = "EVENT_KEEPALIVE_STARTED";
        sCmdToString[20] = "EVENT_KEEPALIVE_STOPPED";
        sCmdToString[21] = "EVENT_KEEPALIVE_START_REQUEST";
        sCmdToString[22] = "EVENT_KEEPALIVE_STOP_REQUEST";
        sCmdToString[23] = "EVENT_LINK_CAPACITY_CHANGED";
        sCmdToString[24] = "EVENT_RESET";
        sCmdToString[25] = "EVENT_REEVALUATE_RESTRICTED_STATE";
        sCmdToString[26] = "EVENT_REEVALUATE_DATA_CONNECTION_PROPERTIES";
        sCmdToString[27] = "EVENT_IPV4_ADDRESS_REMOVED";
        sCmdToString[28] = "EVENT_IPV6_ADDRESS_REMOVED";
        sCmdToString[29] = "EVENT_ADDRESS_REMOVED";
        sCmdToString[30] = "EVENT_VOICE_CALL";
        sCmdToString[31] = "EVENT_FALLBACK_RETRY_CONNECTION";
        sCmdToString[32] = "EVENT_IPV6_ADDRESS_UPDATED";
        sCmdToString[33] = "EVENT_GET_DATA_CALL_LIST";
        sCmdToString[34] = "EVENT_UPDATE_NETWORKAGENT_SSC_MODE3";
        sCmdToString[35] = "EVENT_MTK_NR_STATE_CHANGED";
    }

    public MtkDataConnection(Phone phone, String tagSuffix, int id, DcTracker dct, DataServiceManager dataServiceManager, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        super(phone, tagSuffix, id, dct, dataServiceManager, failBringUpAll, dcc);
        boolean z = false;
        this.mRetryCount = 0;
        this.mInterfaceName = null;
        this.mIsInVoiceCall = false;
        this.mIsSupportConcurrent = false;
        this.mGlobalV6AddrInfo = null;
        this.mTelDevController = MtkTelephonyDevController.getInstance();
        this.mIsSetupDataCallByCi = false;
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.mediatek.internal.telephony.dataconnection.MtkDataConnection.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (TextUtils.isEmpty(action)) {
                    MtkDataConnection mtkDataConnection = MtkDataConnection.this;
                    mtkDataConnection.log("onReceive: ignore empty action='" + action + "'");
                } else if (TextUtils.equals(action, MtkDataConnection.this.mActionRetry)) {
                    if (!intent.hasExtra(MtkDataConnection.INTENT_RETRY_ALARM_WHAT)) {
                        throw new RuntimeException(MtkDataConnection.this.mActionRetry + " has no INTENT_RETRY_ALRAM_WHAT");
                    } else if (intent.hasExtra(MtkDataConnection.INTENT_RETRY_ALARM_TAG)) {
                        int what = intent.getIntExtra(MtkDataConnection.INTENT_RETRY_ALARM_WHAT, EndcBearController.INVALID_INT);
                        int tag = intent.getIntExtra(MtkDataConnection.INTENT_RETRY_ALARM_TAG, EndcBearController.INVALID_INT);
                        if (MtkDataConnection.DBG) {
                            MtkDataConnection mtkDataConnection2 = MtkDataConnection.this;
                            mtkDataConnection2.log("onReceive: action=" + action + " sendMessage(what:" + MtkDataConnection.this.getWhatToString(what) + ", tag:" + tag + ")");
                        }
                        MtkDataConnection mtkDataConnection3 = MtkDataConnection.this;
                        mtkDataConnection3.sendMessage(mtkDataConnection3.obtainMessage(what, tag, 0));
                    } else {
                        throw new RuntimeException(MtkDataConnection.this.mActionRetry + " has no INTENT_RETRY_ALRAM_TAG");
                    }
                } else if (TextUtils.equals(action, "com.mediatek.common.carrierexpress.operator_config_changed")) {
                    boolean unused = MtkDataConnection.this.mIsOp20 = "OP20".equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, ""));
                } else if (MtkDataConnection.DBG) {
                    MtkDataConnection mtkDataConnection4 = MtkDataConnection.this;
                    mtkDataConnection4.log("onReceive: unknown action=" + action);
                }
            }
        };
        if ("OP20".equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, "")) || ("OP20".equals(SystemProperties.get("ril.fwk.test.optr", "")) && "eng".equals(SystemProperties.get("ro.build.type", "")))) {
            z = true;
        }
        this.mIsOp20 = z;
        this.mAlertObserver = new BaseNetworkObserver() {
            /* class com.mediatek.internal.telephony.dataconnection.MtkDataConnection.AnonymousClass2 */

            public void addressRemoved(String iface, LinkAddress address) {
                MtkDataConnection.this.sendMessageForSM(MtkDataConnection.this.getEventByAddress(false, address), iface, address);
            }

            public void addressUpdated(String iface, LinkAddress address) {
                MtkDataConnection.this.sendMessageForSM(MtkDataConnection.this.getEventByAddress(true, address), iface, address);
            }
        };
        setConnectionRat(1, "construct instance");
        try {
            this.mTelephonyCustomizationFactory = OpTelephonyCustomizationUtils.getOpFactory(this.mPhone.getContext());
            this.mDataConnectionExt = this.mTelephonyCustomizationFactory.makeDataConnectionExt(this.mPhone.getContext());
        } catch (Exception e) {
            if (DBG) {
                log("mDataConnectionExt init fail");
            }
            e.printStackTrace();
        }
        this.mDcFcMgr = DcFailCauseManager.getInstance(this.mPhone);
        log("get INetworkManagementService");
        this.mNetworkManager = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        this.mAlarmManager = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        this.mActionRetry = getClass().getCanonicalName() + "." + getName() + ".action_retry";
        resetRetryCount();
    }

    /* access modifiers changed from: protected */
    public int connect(DataConnection.ConnectionParams cp) {
        log("connect: carrier='" + this.mApnSetting.getEntryName() + "' APN='" + this.mApnSetting.getApnName() + "' proxy='" + this.mApnSetting.getProxyAddressAsString() + "' port='" + this.mApnSetting.getProxyPort() + "'");
        if (cp.mApnContext != null) {
            cp.mApnContext.requestLog("MtkDataConnection.connect");
        }
        if (this.mDcTesterFailBringUpAll.getDcFailBringUp().mCounter > 0) {
            DataCallResponse response = new DataCallResponse(this.mDcTesterFailBringUpAll.getDcFailBringUp().mFailCause, this.mDcTesterFailBringUpAll.getDcFailBringUp().mSuggestedRetryTime, 0, 0, 0, "", (List) null, (List) null, (List) null, (List) null, 0);
            Message msg = obtainMessage(262145, cp);
            AsyncResult.forMessage(msg, response, (Throwable) null);
            sendMessage(msg);
            if (DBG) {
                log("connect: FailBringUpAll=" + this.mDcTesterFailBringUpAll.getDcFailBringUp() + " send error response=" + response);
            }
            this.mDcTesterFailBringUpAll.getDcFailBringUp().mCounter--;
            return 0;
        }
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = 0;
        Message msg2 = obtainMessage(262145, cp);
        msg2.obj = cp;
        DataProfile dp = DcTracker.createDataProfile(this.mApnSetting, cp.mProfileId, this.mApnSetting.equals(this.mDct.getPreferredApn()));
        boolean isModemRoaming = this.mPhone.getServiceState().getDataRoamingFromRegistration();
        boolean allowRoaming = this.mPhone.getDataRoamingEnabled() || (isModemRoaming && !this.mPhone.getServiceState().getDataRoaming());
        LinkProperties linkProperties = null;
        int reason = 1;
        if (cp.mRequestType == 2) {
            DcTracker dcTracker = this.mPhone.getDcTracker(getHandoverSourceTransport());
            if (dcTracker == null || cp.mApnContext == null) {
                loge("connect: Handover failed. dcTracker=" + dcTracker + ", apnContext=" + cp.mApnContext);
                return 65542;
            }
            DataConnection dc = dcTracker.getDataConnectionByApnType(cp.mApnContext.getApnType());
            if (dc == null) {
                loge("connect: Can't find data connection for handover.");
                return 65542;
            }
            linkProperties = dc.getLinkProperties();
            this.mHandoverSourceNetworkAgent = dc.getNetworkAgent();
            log("Get the handover source network agent: " + this.mHandoverSourceNetworkAgent);
            dc.setHandoverState(2);
            if (linkProperties == null) {
                loge("connect: Can't find link properties of handover data connection. dc=" + dc);
                return 65542;
            }
            reason = 3;
        }
        int reason2 = mtkReplaceReason(reason, cp.mApnContext);
        if (!this.mDct.isDataServiceBound()) {
            this.mIsSetupDataCallByCi = true;
            this.mPhone.mCi.setupDataCall(ServiceState.rilRadioTechnologyToAccessNetworkType(cp.mRilRat), dp, isModemRoaming, allowRoaming, reason2, linkProperties, msg2);
        } else {
            this.mDataServiceManager.setupDataCall(ServiceState.rilRadioTechnologyToAccessNetworkType(cp.mRilRat), dp, isModemRoaming, allowRoaming, reason2, linkProperties, msg2);
        }
        TelephonyMetrics.getInstance().writeSetupDataCall(this.mPhone.getPhoneId(), cp.mRilRat, dp.getProfileId(), dp.getApn(), dp.getProtocolType());
        return 0;
    }

    /* access modifiers changed from: protected */
    public void tearDownData(Object o) {
        int discReason = 1;
        ApnContext apnContext = null;
        if (o != null && (o instanceof DataConnection.DisconnectParams)) {
            DataConnection.DisconnectParams dp = (DataConnection.DisconnectParams) o;
            apnContext = dp.mApnContext;
            if (TextUtils.equals(dp.mReason, "radioTurnedOff")) {
                discReason = 2;
            } else if (dp.mReleaseType == 3) {
                discReason = 3;
            } else if (TextUtils.equals(dp.mReason, MtkGsmCdmaPhone.REASON_RA_FAILED)) {
                long j = this.mValid;
                if (j == -1) {
                    discReason = ExternalSimConstants.MSG_ID_CAPABILITY_SWITCH_DONE;
                } else if (j == -2) {
                    discReason = MtkGsmCdmaPhone.EVENT_GET_CLIR_COMPLETE;
                }
            } else if (TextUtils.equals(dp.mReason, MtkGsmCdmaPhone.REASON_PCSCF_ADDRESS_FAILED)) {
                discReason = 2003;
            } else if (TextUtils.equals(dp.mReason, "apnChanged")) {
                discReason = MtkGsmCdmaPhone.EVENT_SET_CALL_BARRING_COMPLETE;
            }
        }
        String str = "tearDownData. mCid=" + this.mCid + ", reason=" + discReason;
        if (DBG) {
            log(str);
        }
        if (apnContext != null) {
            apnContext.requestLog(str);
        }
        this.mDataServiceManager.deactivateDataCall(this.mCid, discReason, obtainMessage(262147, this.mTag, 0, o));
    }

    /* access modifiers changed from: protected */
    public void clearSettings() {
        MtkDataConnection.super.clearSettings();
        if (DBG) {
            log("clearSettings");
        }
        this.mGlobalV6AddrInfo = null;
        resetRetryCount();
        setConnectionRat(1, "clear setting");
    }

    /* access modifiers changed from: protected */
    public DataConnection.SetupResult onSetupConnectionCompleted(int resultCode, DataCallResponse response, DataConnection.ConnectionParams cp) {
        DataConnection.SetupResult result;
        log("onSetupConnectionCompleted: resultCode=" + resultCode + ", response=" + response);
        if (cp.mTag != this.mTag) {
            if (DBG) {
                log("onSetupConnectionCompleted stale cp.tag=" + cp.mTag + ", mtag=" + this.mTag);
            }
            result = DataConnection.SetupResult.ERROR_STALE;
        } else if (resultCode == 4) {
            result = DataConnection.SetupResult.ERROR_RADIO_NOT_AVAILABLE;
            result.mFailCause = 65537;
        } else if (response.getCause() == 0) {
            if (DBG) {
                log("onSetupConnectionCompleted received successful DataCallResponse");
            }
            this.mCid = response.getId();
            this.mPcscfAddr = (String[]) response.getPcscfAddresses().stream().map($$Lambda$XZAGhHrbkIDyusER4MAM6luKcT0.INSTANCE).toArray($$Lambda$MtkDataConnection$d5DGPjmgYOgwzy7IwiRu2rUAiI.INSTANCE);
            setConnectionRat(MtkDcHelper.decodeRat(response.getLinkStatus()), "data call response");
            result = updateLinkProperty(response).setupResult;
            this.mInterfaceName = response.getInterfaceName();
            log("onSetupConnectionCompleted: ifname-" + this.mInterfaceName);
        } else if (response.getCause() == 65537) {
            result = DataConnection.SetupResult.ERROR_RADIO_NOT_AVAILABLE;
            result.mFailCause = 65537;
        } else {
            result = DataConnection.SetupResult.ERROR_DATA_SERVICE_SPECIFIC_ERROR;
            result.mFailCause = DataFailCause.getFailCause(response.getCause());
        }
        this.mPhone.getServiceStateTracker().oppoAddDataCallCount();
        return result;
    }

    static /* synthetic */ String[] lambda$onSetupConnectionCompleted$0(int x$0) {
        return new String[x$0];
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public NetworkCapabilities getNetworkCapabilities() {
        boolean z;
        char c;
        ArrayList<ApnSetting> wifiApnList;
        NetworkCapabilities result = new NetworkCapabilities();
        boolean z2 = false;
        result.addTransportType(0);
        ApnSetting apnSetting = this.mApnSetting;
        char c2 = 2;
        if (!(this.mConnectionParams == null || this.mConnectionParams.mApnContext == null || this.mRat != 2 || (wifiApnList = this.mConnectionParams.mApnContext.getWifiApns()) == null)) {
            Iterator<ApnSetting> it = wifiApnList.iterator();
            while (it.hasNext()) {
                ApnSetting tApnSetting = it.next();
                if (tApnSetting != null && !tApnSetting.getApnName().equals("")) {
                    log("makeNetworkCapabilities: apn: " + tApnSetting.getApnName());
                    apnSetting = tApnSetting;
                }
            }
        }
        if (apnSetting != null) {
            String[] types = ApnSetting.getApnTypesStringFromBitmask(apnSetting.getApnTypeBitmask() & (~this.mDisabledApnTypeBitMask)).split(",");
            int length = types.length;
            int i = 0;
            while (i < length) {
                String type = types[i];
                if (this.mRestrictedNetworkOverride || !this.mUnmeteredUseOnly || !ApnSettingUtils.isMeteredApnType(ApnSetting.getApnTypesBitmaskFromString(type), this.mPhone)) {
                    switch (type.hashCode()) {
                        case 42:
                            if (type.equals("*")) {
                                c = 0;
                                break;
                            }
                            c = 65535;
                            break;
                        case 3352:
                            if (type.equals("ia")) {
                                c = 8;
                                break;
                            }
                            c = 65535;
                            break;
                        case 97545:
                            if (type.equals("bip")) {
                                c = 14;
                                break;
                            }
                            c = 65535;
                            break;
                        case 98292:
                            if (type.equals("cbs")) {
                                c = 7;
                                break;
                            }
                            c = 65535;
                            break;
                        case 99837:
                            if (type.equals("dun")) {
                                c = 4;
                                break;
                            }
                            c = 65535;
                            break;
                        case 104399:
                            if (type.equals("ims")) {
                                c = 6;
                                break;
                            }
                            c = 65535;
                            break;
                        case 107938:
                            if (type.equals("mcx")) {
                                c = 10;
                                break;
                            }
                            c = 65535;
                            break;
                        case 108243:
                            if (type.equals("mms")) {
                                c = c2;
                                break;
                            }
                            c = 65535;
                            break;
                        case 112738:
                            if (type.equals("rcs")) {
                                c = 13;
                                break;
                            }
                            c = 65535;
                            break;
                        case 117478:
                            if (type.equals("wap")) {
                                c = 11;
                                break;
                            }
                            c = 65535;
                            break;
                        case 3149046:
                            if (type.equals("fota")) {
                                c = 5;
                                break;
                            }
                            c = 65535;
                            break;
                        case 3541982:
                            if (type.equals("supl")) {
                                c = 3;
                                break;
                            }
                            c = 65535;
                            break;
                        case 3629217:
                            if (type.equals("vsim")) {
                                c = 15;
                                break;
                            }
                            c = 65535;
                            break;
                        case 3673178:
                            if (type.equals("xcap")) {
                                c = 12;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1544803905:
                            if (type.equals("default")) {
                                c = 1;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1629013393:
                            if (type.equals("emergency")) {
                                c = 9;
                                break;
                            }
                            c = 65535;
                            break;
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            int dcTag = this.mDcController.getActiveDcTagSupportInternet();
                            if (MtkDcHelper.isPreferredDataPhone(this.mPhone) && (dcTag == 0 || dcTag == this.mTag)) {
                                result.addCapability(12);
                            }
                            z = false;
                            result.addCapability(0);
                            result.addCapability(1);
                            result.addCapability(3);
                            result.addCapability(5);
                            result.addCapability(7);
                            result.addCapability(2);
                            result.addCapability(25);
                            result.addCapability(9);
                            result.addCapability(8);
                            result.addCapability(27);
                            result.addCapability(26);
                            continue;
                        case 1:
                            int dcTag2 = this.mDcController.getActiveDcTagSupportInternet();
                            if (!MtkDcHelper.isPreferredDataPhone(this.mPhone) || !(dcTag2 == 0 || dcTag2 == this.mTag)) {
                                z = false;
                                break;
                            } else {
                                result.addCapability(12);
                                z = false;
                                continue;
                            }
                        case 2:
                            result.addCapability(0);
                            z = false;
                            continue;
                        case 3:
                            result.addCapability(1);
                            z = false;
                            continue;
                        case 4:
                            result.addCapability(2);
                            z = false;
                            continue;
                        case 5:
                            result.addCapability(3);
                            z = false;
                            continue;
                        case 6:
                            result.addCapability(4);
                            z = false;
                            continue;
                        case 7:
                            result.addCapability(5);
                            z = false;
                            continue;
                        case 8:
                            result.addCapability(7);
                            z = false;
                            continue;
                        case 9:
                            result.addCapability(10);
                            z = false;
                            continue;
                        case 10:
                            result.addCapability(23);
                            z = false;
                            continue;
                        case 11:
                            result.addCapability(25);
                            z = false;
                            continue;
                        case 12:
                            result.addCapability(9);
                            z = false;
                            continue;
                        case 13:
                            result.addCapability(8);
                            z = false;
                            continue;
                        case 14:
                            result.addCapability(27);
                            z = false;
                            continue;
                        case 15:
                            result.addCapability(26);
                            z = false;
                            continue;
                        default:
                            z = false;
                            continue;
                    }
                } else {
                    log("Dropped the metered " + type + " for the unmetered data call.");
                    z = z2;
                }
                i++;
                z2 = z;
                c2 = 2;
            }
            addInternetCapForDunOnlyType(apnSetting, result);
            if ((!this.mUnmeteredUseOnly || this.mRestrictedNetworkOverride) && ApnSettingUtils.isMetered(apnSetting, this.mPhone)) {
                result.removeCapability(11);
            } else {
                result.addCapability(11);
            }
            result.maybeMarkCapabilitiesRestricted();
        }
        if (this.mRestrictedNetworkOverride) {
            result.removeCapability(13);
            result.removeCapability(2);
        }
        int up = 14;
        int down = 14;
        int i2 = this.mRilRat;
        if (i2 != 19) {
            switch (i2) {
                case 1:
                    up = 80;
                    down = 80;
                    break;
                case 2:
                    up = 59;
                    down = 236;
                    break;
                case 3:
                    up = 384;
                    down = 384;
                    break;
                case 4:
                case 5:
                    up = 14;
                    down = 14;
                    break;
                case 6:
                    up = 100;
                    down = 100;
                    break;
                case 7:
                    up = 153;
                    down = 2457;
                    break;
                case 8:
                    up = 1843;
                    down = 3174;
                    break;
                case 9:
                    up = MtkApnTypes.WAP;
                    down = 14336;
                    break;
                case 10:
                    up = 5898;
                    down = 14336;
                    break;
                case 11:
                    up = 5898;
                    down = 14336;
                    break;
                case 12:
                    up = 1843;
                    down = 5017;
                    break;
                case 13:
                    up = 153;
                    down = 2516;
                    break;
                case 14:
                    up = 51200;
                    down = 102400;
                    break;
                case 15:
                    up = 11264;
                    down = 43008;
                    break;
            }
        } else {
            up = 51200;
            down = 102400;
        }
        result.setLinkUpstreamBandwidthKbps(up);
        result.setLinkDownstreamBandwidthKbps(down);
        result.setNetworkSpecifier(new StringNetworkSpecifier(Integer.toString(this.mPhone.getSubId())));
        result.setCapability(18, !this.mPhone.getServiceState().getDataRoaming());
        result.addCapability(20);
        if ((this.mSubscriptionOverride & 1) != 0) {
            result.addCapability(11);
        }
        if ((this.mSubscriptionOverride & 2) != 0) {
            result.removeCapability(20);
        }
        this.mNetworkCapabilities = result;
        return result;
    }

    private void addInternetCapForDunOnlyType(ApnSetting apn, NetworkCapabilities nc) {
        if (this.mIsOp20) {
            boolean isDunApn = true;
            boolean isDunOnly = (apn.getApnTypeBitmask() | 8) == 8;
            if (apn.getApnName() == null || !apn.getApnName().toLowerCase().contains("pam")) {
                isDunApn = false;
            }
            if (isDunOnly && isDunApn) {
                int i = this.mRilRat;
                if (i != 12) {
                    switch (i) {
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            break;
                        default:
                            return;
                    }
                }
                nc.addCapability(12);
            }
        }
    }

    private class MtkDcDefaultState extends DataConnection.DcDefaultState {
        private MtkDcDefaultState() {
            super(MtkDataConnection.this);
        }

        public void enter() {
            if (MtkDataConnection.DBG) {
                MtkDataConnection.this.log("DcDefaultState: enter");
            }
            MtkDataConnection.this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(MtkDataConnection.this.mTransportType, MtkDataConnection.this.getHandler(), 262155, (Object) null);
            MtkDataConnection.this.mPhone.getServiceStateTracker().registerForDataRoamingOn(MtkDataConnection.this.getHandler(), 262156, (Object) null);
            MtkDataConnection.this.mPhone.getServiceStateTracker().registerForDataRoamingOff(MtkDataConnection.this.getHandler(), 262157, (Object) null, true);
            MtkDataConnection.this.mPhone.getServiceStateTracker().registerForMtkNrStateChanged(MtkDataConnection.this.getHandler(), MtkDataConnection.EVENT_MTK_NR_STATE_CHANGED, null);
            if (!(MtkDataConnection.this.mTelDevController == null || MtkDataConnection.this.mTelDevController.getModem(0) == null || MtkDataConnection.this.mTelDevController.getModem(0).hasRaCapability())) {
                MtkDataConnection.this.registerNetworkAlertObserver();
            }
            MtkDataConnection.this.mDcController.addDc(MtkDataConnection.this);
            IntentFilter filter = new IntentFilter();
            filter.addAction(MtkDataConnection.this.mActionRetry);
            if (MtkDataConnection.DBG) {
                MtkDataConnection mtkDataConnection = MtkDataConnection.this;
                mtkDataConnection.log("DcDefaultState: register for intent action=" + MtkDataConnection.this.mActionRetry);
            }
            filter.addAction("com.mediatek.common.carrierexpress.operator_config_changed");
            MtkDataConnection.this.mPhone.getContext().registerReceiver(MtkDataConnection.this.mIntentReceiver, filter, null, MtkDataConnection.this.getHandler());
        }

        public void exit() {
            if (MtkDataConnection.DBG) {
                MtkDataConnection.this.log("DcDefaultState: exit");
            }
            MtkDataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(MtkDataConnection.this.mTransportType, MtkDataConnection.this.getHandler());
            MtkDataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(MtkDataConnection.this.getHandler());
            MtkDataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(MtkDataConnection.this.getHandler());
            MtkDataConnection.this.mPhone.getServiceStateTracker().unregisterForMtkNrStateChanged(MtkDataConnection.this.getHandler());
            MtkDataConnection.this.mDcController.removeDc(MtkDataConnection.this);
            if (MtkDataConnection.this.mAc != null) {
                MtkDataConnection.this.mAc.disconnected();
                AsyncChannel unused = MtkDataConnection.this.mAc = null;
            }
            MtkDataConnection.this.mApnContexts.clear();
            PendingIntent unused2 = MtkDataConnection.this.mReconnectIntent = null;
            DcTracker unused3 = MtkDataConnection.this.mDct = null;
            ApnSetting unused4 = MtkDataConnection.this.mApnSetting = null;
            Phone unused5 = MtkDataConnection.this.mPhone = null;
            DataServiceManager unused6 = MtkDataConnection.this.mDataServiceManager = null;
            LinkProperties unused7 = MtkDataConnection.this.mLinkProperties = null;
            int unused8 = MtkDataConnection.this.mLastFailCause = 0;
            Object unused9 = MtkDataConnection.this.mUserData = null;
            DcController unused10 = MtkDataConnection.this.mDcController = null;
            DcTesterFailBringUpAll unused11 = MtkDataConnection.this.mDcTesterFailBringUpAll = null;
            if (!(MtkDataConnection.this.mTelDevController == null || MtkDataConnection.this.mTelDevController.getModem(0) == null || MtkDataConnection.this.mTelDevController.getModem(0).hasRaCapability())) {
                MtkDataConnection.this.unregisterNetworkAlertObserver();
            }
            MtkDataConnection.this.mPhone.getContext().unregisterReceiver(MtkDataConnection.this.mIntentReceiver);
        }

        public boolean processMessage(Message msg) {
            if (MtkDataConnection.VDBG) {
                MtkDataConnection.this.log("DcDefault msg=" + MtkDataConnection.this.getWhatToString(msg.what) + " RefCount=" + MtkDataConnection.this.mApnContexts.size());
            }
            boolean z = true;
            switch (msg.what) {
                case 262155:
                    if (MtkDataConnection.this.mIsInVoiceCall) {
                        boolean unused = MtkDataConnection.this.mIsSupportConcurrent = MtkDcHelper.getInstance().isDataAllowedForConcurrent(MtkDataConnection.this.mPhone.getPhoneId());
                    }
                    return MtkDataConnection.super.processMessage(msg);
                case MtkDataConnection.EVENT_IPV4_ADDRESS_REMOVED /*{ENCODED_INT: 262171}*/:
                    if (!MtkDataConnection.VDBG) {
                        return true;
                    }
                    MtkDataConnection.this.log("DcDefaultState: ignore EVENT_IPV4_ADDRESS_REMOVED not in ActiveState");
                    return true;
                case MtkDataConnection.EVENT_IPV6_ADDRESS_REMOVED /*{ENCODED_INT: 262172}*/:
                    if (!MtkDataConnection.VDBG) {
                        return true;
                    }
                    MtkDataConnection.this.log("DcDefaultState: ignore EVENT_IPV6_ADDRESS_REMOVED not in ActiveState");
                    return true;
                case MtkDataConnection.EVENT_ADDRESS_REMOVED /*{ENCODED_INT: 262173}*/:
                    if (!MtkDataConnection.VDBG) {
                        return true;
                    }
                    MtkDataConnection.this.log("DcDefaultState: " + MtkDataConnection.this.getWhatToString(msg.what));
                    return true;
                case MtkDataConnection.EVENT_VOICE_CALL /*{ENCODED_INT: 262174}*/:
                    boolean unused2 = MtkDataConnection.this.mIsInVoiceCall = msg.arg1 != 0;
                    MtkDataConnection mtkDataConnection = MtkDataConnection.this;
                    if (msg.arg2 == 0) {
                        z = false;
                    }
                    boolean unused3 = mtkDataConnection.mIsSupportConcurrent = z;
                    return true;
                case MtkDataConnection.EVENT_IPV6_ADDRESS_UPDATED /*{ENCODED_INT: 262176}*/:
                    if (!MtkDataConnection.VDBG) {
                        return true;
                    }
                    MtkDataConnection.this.log("DcDefaultState: ignore EVENT_IPV6_ADDRESS_UPDATED not in ActiveState");
                    return true;
                case MtkDataConnection.EVENT_UPDATE_NETWORKAGENT_SSC_MODE3 /*{ENCODED_INT: 262178}*/:
                    if (MtkDataConnection.DBG) {
                        MtkDataConnection.this.log("EVENT_UPDATE_NETWORKAGENT_SSC_MODE3: lifetime=" + msg.arg1 + ", new score=" + msg.arg2);
                    }
                    MtkDataConnection.this.setSscMode3LingeringTime(msg.arg1);
                    MtkDataConnection.this.sendNetworkScore(msg.arg2);
                    return true;
                case MtkDataConnection.EVENT_MTK_NR_STATE_CHANGED /*{ENCODED_INT: 262179}*/:
                    if (!MtkDataConnection.this.canSendNetworkCapabilities()) {
                        return true;
                    }
                    MtkDataConnection mtkDataConnection2 = MtkDataConnection.this;
                    mtkDataConnection2.updateTcpBufferSizes(mtkDataConnection2.mRilRat);
                    if (MtkDataConnection.DBG) {
                        MtkDataConnection.this.log("DcDefaultState: EVENT_MTK_NR_STATE_CHANGED mRilRat=" + MtkDataConnection.this.mRilRat);
                    }
                    MtkDataConnection.this.updateNetworkInfo();
                    MtkDataConnection.this.updateNetworkInfoSuspendState();
                    if (MtkDataConnection.this.mNetworkAgent == null) {
                        return true;
                    }
                    MtkDataConnection.this.mNetworkAgent.sendNetworkCapabilities(MtkDataConnection.this.getNetworkCapabilities(), MtkDataConnection.this);
                    MtkDataConnection.this.mNetworkAgent.sendNetworkInfo(MtkDataConnection.this.mNetworkInfo, MtkDataConnection.this);
                    MtkDataConnection.this.mNetworkAgent.sendLinkProperties(MtkDataConnection.this.mtkGetLinkProperties(), MtkDataConnection.this);
                    return true;
                default:
                    return MtkDataConnection.super.processMessage(msg);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateNetworkInfoSuspendState() {
        NetworkInfo.DetailedState oldState = this.mNetworkInfo.getDetailedState();
        if (this.mNetworkAgent == null) {
            Rlog.e(getName(), "Setting suspend state without a NetworkAgent");
        }
        ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
        boolean bNwNeedSuspended = isNwNeedSuspended();
        if (DBG) {
            log("updateNetworkInfoSuspendState: oldState = " + oldState + ", currentDataConnectionState = " + sst.getCurrentDataConnectionState() + ", bNwNeedSuspended = " + bNwNeedSuspended);
        }
        if (sst.getCurrentDataConnectionState() != 0) {
            if (this.mIsInVoiceCall || MtkDcHelper.isImsOrEmergencyApn(getApnType()) || MtkDcHelper.hasVsimApn(getApnType())) {
                this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, this.mNetworkInfo.getExtraInfo());
            } else {
                this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
            }
        } else if (bNwNeedSuspended) {
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
        } else {
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, this.mNetworkInfo.getExtraInfo());
        }
    }

    private class MtkDcActivatingState extends DataConnection.DcActivatingState {
        private MtkDcActivatingState() {
            super(MtkDataConnection.this);
        }

        public void enter() {
            if (MtkDataConnection.DBG) {
                MtkDataConnection mtkDataConnection = MtkDataConnection.this;
                mtkDataConnection.log("DcActivatingState: enter dc=" + MtkDataConnection.this);
            }
            MtkDataConnection.super.enter();
        }

        public void exit() {
            if (MtkDataConnection.DBG) {
                MtkDataConnection mtkDataConnection = MtkDataConnection.this;
                mtkDataConnection.log("DcActivatingState: exit dc=" + MtkDataConnection.this);
            }
            MtkDataConnection.super.exit();
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* JADX WARNING: Code restructure failed: missing block: B:107:?, code lost:
            return true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0148, code lost:
            if (0 != 0) goto L_0x014a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x014a, code lost:
            r5 = (com.android.internal.telephony.dataconnection.DataConnection.ConnectionParams) ((android.os.AsyncResult) r11.obj).userObj;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x0160, code lost:
            if (1 == 0) goto L_0x0163;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x0163, code lost:
            if (r5 != null) goto L_0x0169;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x0169, code lost:
            r0 = r11.getData().getParcelable("data_call_response");
            r3 = r11.arg1;
         */
        public boolean processMessage(Message msg) {
            DataConnection.ConnectionParams cp;
            DataCallResponse dataCallResponse;
            DataConnection.SetupResult result;
            if (MtkDataConnection.DBG) {
                MtkDataConnection.this.log("DcActivatingState: msg=" + MtkDataConnection.msgToString(msg));
            }
            switch (msg.what) {
                case 262144:
                    DataConnection.ConnectionParams cp2 = (DataConnection.ConnectionParams) msg.obj;
                    MtkDataConnection.this.mApnContexts.put(cp2.mApnContext, cp2);
                    if (MtkDataConnection.DBG) {
                        MtkDataConnection.this.log("DcActivatingState: mApnContexts size=" + MtkDataConnection.this.mApnContexts.size());
                        break;
                    }
                    break;
                case 262145:
                    int resultCode = 0;
                    if (MtkDataConnection.this.mIsSetupDataCallByCi) {
                        boolean unused = MtkDataConnection.this.mIsSetupDataCallByCi = false;
                        AsyncResult ar = (AsyncResult) msg.obj;
                        dataCallResponse = (DataCallResponse) ar.result;
                        cp = (DataConnection.ConnectionParams) ar.userObj;
                    } else {
                        cp = null;
                        try {
                            cp = (DataConnection.ConnectionParams) msg.obj;
                            break;
                        } catch (Exception e) {
                            MtkDataConnection.this.log("msg.obj can not be cast to ConnectionParams");
                            break;
                        } catch (Throwable th) {
                            if (1 != 0) {
                                DataConnection.ConnectionParams cp3 = (DataConnection.ConnectionParams) ((AsyncResult) msg.obj).userObj;
                            }
                            throw th;
                        }
                    }
                    if (dataCallResponse == null) {
                        result = DataConnection.SetupResult.ERROR_INVALID_ARG;
                    } else {
                        result = MtkDataConnection.this.onSetupConnectionCompleted(resultCode, dataCallResponse, cp);
                    }
                    if (!(result == DataConnection.SetupResult.ERROR_STALE || MtkDataConnection.this.mConnectionParams == cp)) {
                        MtkDataConnection.this.loge("DcActivatingState: WEIRD mConnectionsParams:" + MtkDataConnection.this.mConnectionParams + " != cp:" + cp);
                    }
                    if (MtkDataConnection.DBG) {
                        MtkDataConnection.this.log("DcActivatingState onSetupConnectionCompleted result=" + result + " dc=" + MtkDataConnection.this);
                    }
                    if (cp.mApnContext != null) {
                        cp.mApnContext.requestLog("onSetupConnectionCompleted result=" + result);
                    }
                    int i = AnonymousClass3.$SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[result.ordinal()];
                    if (i == 1) {
                        int unused2 = MtkDataConnection.this.mDcFailCause = 0;
                        MtkDataConnection.this.resetRetryCount();
                        MtkDataConnection mtkDataConnection = MtkDataConnection.this;
                        mtkDataConnection.transitionTo(mtkDataConnection.mActiveState);
                    } else if (i == 2) {
                        MtkDataConnection.this.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                        MtkDataConnection mtkDataConnection2 = MtkDataConnection.this;
                        mtkDataConnection2.transitionTo(mtkDataConnection2.mInactiveState);
                    } else if (i == 3) {
                        MtkDataConnection.this.tearDownData(cp);
                        MtkDataConnection mtkDataConnection3 = MtkDataConnection.this;
                        mtkDataConnection3.transitionTo(mtkDataConnection3.mDisconnectingErrorCreatingConnection);
                    } else if (i == 4) {
                        long delay = MtkDataConnection.this.getSuggestedRetryDelay(dataCallResponse);
                        if (result.mFailCause == 26 || result.mFailCause == 31 || result.mFailCause == 34 || result.mFailCause == 38) {
                            if (MtkDataConnection.DBG) {
                                MtkDataConnection.this.log("DcActivatingState: resetDelayTimeForAuTelstraOperator");
                            }
                            delay = MtkDataConnection.this.resetDelayTimeForAuTelstraOperator(delay);
                        }
                        cp.mApnContext.setModemSuggestedDelay(delay);
                        String str = "DcActivatingState: ERROR_DATA_SERVICE_SPECIFIC_ERROR  delay=" + delay + " result=" + result + " result.isRadioRestartFailure=" + DataFailCause.isRadioRestartFailure(MtkDataConnection.this.mPhone.getContext(), result.mFailCause, MtkDataConnection.this.mPhone.getSubId()) + " isPermanentFailure=" + MtkDataConnection.this.mDct.isPermanentFailure(result.mFailCause);
                        if (MtkDataConnection.DBG) {
                            MtkDataConnection.this.log(str);
                        }
                        if (cp.mApnContext != null) {
                            cp.mApnContext.requestLog(str);
                        }
                        if (result.mFailCause == MtkDataConnection.RA_GET_IPV6_VALID_FAIL) {
                            MtkDataConnection.this.onSetupFallbackConnection(dataCallResponse, cp);
                            int unused3 = MtkDataConnection.this.mDcFailCause = MtkDataConnection.RA_GET_IPV6_VALID_FAIL;
                            MtkDataConnection mtkDataConnection4 = MtkDataConnection.this;
                            mtkDataConnection4.deferMessage(mtkDataConnection4.obtainMessage(MtkDataConnection.EVENT_FALLBACK_RETRY_CONNECTION, mtkDataConnection4.mTag));
                            MtkDataConnection mtkDataConnection5 = MtkDataConnection.this;
                            mtkDataConnection5.transitionTo(mtkDataConnection5.mActiveState);
                        } else {
                            MtkDataConnection.this.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                            MtkDataConnection mtkDataConnection6 = MtkDataConnection.this;
                            mtkDataConnection6.transitionTo(mtkDataConnection6.mInactiveState);
                        }
                    } else if (i == 5) {
                        MtkDataConnection.this.loge("DcActivatingState: stale EVENT_SETUP_DATA_CONNECTION_DONE tag:" + cp.mTag + " != mTag:" + MtkDataConnection.this.mTag);
                    } else {
                        throw new RuntimeException("Unknown SetupResult, should not happen");
                    }
                    return true;
                case 262148:
                    DataConnection.DisconnectParams dp = (DataConnection.DisconnectParams) msg.obj;
                    if (!MtkDataConnection.this.mApnContexts.containsKey(dp.mApnContext)) {
                        MtkDataConnection.this.log("DcActivatingState ERROR no such apnContext=" + dp.mApnContext + " in this dc=" + MtkDataConnection.this);
                        MtkDataConnection.this.notifyDisconnectCompleted(dp, false);
                    } else {
                        MtkDataConnection.this.deferMessage(msg);
                    }
                    return true;
                case 262155:
                    break;
                case MtkDataConnection.EVENT_IPV4_ADDRESS_REMOVED /*{ENCODED_INT: 262171}*/:
                case MtkDataConnection.EVENT_IPV6_ADDRESS_REMOVED /*{ENCODED_INT: 262172}*/:
                case MtkDataConnection.EVENT_IPV6_ADDRESS_UPDATED /*{ENCODED_INT: 262176}*/:
                    if (MtkDataConnection.DBG) {
                        MtkDataConnection.this.log("DcActivatingState deferMsg: " + MtkDataConnection.this.getWhatToString(msg.what) + ", address info: " + ((AddressInfo) msg.obj));
                    }
                    MtkDataConnection.this.deferMessage(msg);
                    return true;
                case MtkDataConnection.EVENT_MTK_NR_STATE_CHANGED /*{ENCODED_INT: 262179}*/:
                    if (!MtkDataConnection.this.canSendNetworkCapabilities()) {
                        return true;
                    }
                    MtkDataConnection mtkDataConnection7 = MtkDataConnection.this;
                    mtkDataConnection7.updateTcpBufferSizes(mtkDataConnection7.mRilRat);
                    if (MtkDataConnection.DBG) {
                        MtkDataConnection.this.log("DcActivatingState: EVENT_MTK_NR_STATE_CHANGED mRilRat=" + MtkDataConnection.this.mRilRat);
                    }
                    MtkDataConnection.this.updateNetworkInfo();
                    MtkDataConnection.this.updateNetworkInfoSuspendState();
                    if (MtkDataConnection.this.mNetworkAgent != null) {
                        MtkDataConnection.this.mNetworkAgent.sendNetworkCapabilities(MtkDataConnection.this.getNetworkCapabilities(), MtkDataConnection.this);
                        MtkDataConnection.this.mNetworkAgent.sendNetworkInfo(MtkDataConnection.this.mNetworkInfo, MtkDataConnection.this);
                        MtkDataConnection.this.mNetworkAgent.sendLinkProperties(MtkDataConnection.this.mtkGetLinkProperties(), MtkDataConnection.this);
                    }
                    return true;
                default:
                    return MtkDataConnection.super.processMessage(msg);
            }
            MtkDataConnection.this.deferMessage(msg);
            return true;
        }
    }

    /* renamed from: com.mediatek.internal.telephony.dataconnection.MtkDataConnection$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult = new int[DataConnection.SetupResult.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[DataConnection.SetupResult.SUCCESS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[DataConnection.SetupResult.ERROR_RADIO_NOT_AVAILABLE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[DataConnection.SetupResult.ERROR_INVALID_ARG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[DataConnection.SetupResult.ERROR_DATA_SERVICE_SPECIFIC_ERROR.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[DataConnection.SetupResult.ERROR_STALE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    private class MtkDcActiveState extends DataConnection.DcActiveState {
        private MtkDcActiveState() {
            super(MtkDataConnection.this);
        }

        public void enter() {
            if (MtkDataConnection.DBG) {
                MtkDataConnection.this.log("DcActiveState: enter dc=" + MtkDataConnection.this);
            }
            boolean z = false;
            StatsLog.write(75, 3, MtkDataConnection.this.mPhone.getPhoneId(), MtkDataConnection.this.mId, MtkDataConnection.this.mApnSetting != null ? (long) MtkDataConnection.this.mApnSetting.getApnTypeBitmask() : 0, MtkDataConnection.this.mApnSetting != null ? MtkDataConnection.this.mApnSetting.canHandleType(17) : false);
            Iterator it = MtkDataConnection.this.mApnContexts.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ApnContext apnContext = ((DataConnection.ConnectionParams) it.next()).mApnContext;
                if (TextUtils.equals(apnContext.getApnType(), "default")) {
                    MtkDataConnection.this.log("DcActiveState: notifyDataConnection in advance for default apn type.");
                    apnContext.setReason("connected");
                    apnContext.setState(DctConstants.State.CONNECTED);
                    MtkDataConnection.this.mPhone.notifyDataConnection(apnContext.getApnType());
                    break;
                }
            }
            int factorySerialNumber = -1;
            String[] strArr = null;
            if (MtkDataConnection.this.mApnSetting != null && !TextUtils.isEmpty(MtkDataConnection.this.mApnSetting.getProxyAddressAsString())) {
                try {
                    int port = MtkDataConnection.this.mApnSetting.getProxyPort();
                    if (port == -1) {
                        port = 8080;
                    }
                    MtkDataConnection.this.mLinkProperties.setHttpProxy(new ProxyInfo(MtkDataConnection.this.mApnSetting.getProxyAddressAsString(), port, null));
                } catch (NumberFormatException e) {
                    MtkDataConnection.this.loge("DcActiveState: NumberFormatException making ProxyProperties (" + MtkDataConnection.this.mApnSetting.getProxyPort() + "): " + e);
                }
            }
            MtkDataConnection.this.updateNetworkInfo();
            MtkDataConnection.this.notifyAllWithEvent(null, 270336, "connected");
            MtkDataConnection.this.mDcController.addActiveDcByCid(MtkDataConnection.this);
            if (MtkDataConnection.this.isNwNeedSuspended()) {
                MtkDataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.SUSPENDED, MtkDataConnection.this.mNetworkInfo.getReason(), null);
            } else {
                MtkDataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, MtkDataConnection.this.mNetworkInfo.getReason(), null);
            }
            MtkDataConnection.this.mNetworkInfo.setExtraInfo(MtkDataConnection.this.mApnSetting.getApnName());
            MtkDataConnection mtkDataConnection = MtkDataConnection.this;
            mtkDataConnection.updateTcpBufferSizes(mtkDataConnection.mRilRat);
            NetworkMisc misc = new NetworkMisc();
            if (MtkDataConnection.this.mPhone.getCarrierSignalAgent().hasRegisteredReceivers("com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED")) {
                misc.provisioningNotificationDisabled = true;
            }
            misc.subscriberId = MtkDataConnection.this.mPhone.getSubscriberId();
            String str = "";
            if (misc.subscriberId == null) {
                if (MtkDataConnection.this.mPhone.getPhoneType() == 2) {
                    misc.subscriberId = SystemProperties.get(MtkDataConnection.PROP_RIL_DATA_CDMA_IMSI + MtkDataConnection.this.mPhone.getPhoneId(), str);
                } else {
                    misc.subscriberId = SystemProperties.get(MtkDataConnection.PROP_RIL_DATA_GSM_IMSI + MtkDataConnection.this.mPhone.getPhoneId(), str);
                }
            }
            misc.skip464xlat = MtkDataConnection.this.shouldSkip464Xlat();
            MtkDataConnection mtkDataConnection2 = MtkDataConnection.this;
            if (mtkDataConnection2.shouldRestrictNetwork() && !MtkDataConnection.this.mDct.haveVsimIgnoreUserDataSetting()) {
                z = true;
            }
            boolean unused = mtkDataConnection2.mRestrictedNetworkOverride = z;
            MtkDataConnection mtkDataConnection3 = MtkDataConnection.this;
            boolean unused2 = mtkDataConnection3.mUnmeteredUseOnly = mtkDataConnection3.isUnmeteredUseOnly();
            if (MtkDataConnection.DBG) {
                MtkDataConnection.this.log("mRestrictedNetworkOverride = " + MtkDataConnection.this.mRestrictedNetworkOverride + ", mUnmeteredUseOnly = " + MtkDataConnection.this.mUnmeteredUseOnly);
            }
            if (MtkDataConnection.this.mConnectionParams == null || MtkDataConnection.this.mConnectionParams.mRequestType != 2) {
                DcTracker dcTracker = MtkDataConnection.this;
                int unused3 = dcTracker.mScore = dcTracker.calculateScore();
                NetworkFactory factory = PhoneFactory.getNetworkFactory(MtkDataConnection.this.mPhone.getPhoneId());
                if (factory != null) {
                    factorySerialNumber = factory.getSerialNumber();
                }
                MtkDataConnection mtkDataConnection4 = MtkDataConnection.this;
                DcNetworkAgent unused4 = mtkDataConnection4.mNetworkAgent = DcNetworkAgent.createDcNetworkAgent(mtkDataConnection4, mtkDataConnection4.mPhone, MtkDataConnection.this.mNetworkInfo, MtkDataConnection.this.mScore, misc, factorySerialNumber, MtkDataConnection.this.mTransportType);
            } else {
                DataConnection dc = MtkDataConnection.this.mPhone.getDcTracker(MtkDataConnection.this.getHandoverSourceTransport()).getDataConnectionByApnType(MtkDataConnection.this.mConnectionParams.mApnContext.getApnType());
                if (dc != null) {
                    dc.setHandoverState(3);
                }
                if (MtkDataConnection.this.mHandoverSourceNetworkAgent != null) {
                    MtkDataConnection.this.log("Transfer network agent successfully.");
                    MtkDataConnection mtkDataConnection5 = MtkDataConnection.this;
                    DcNetworkAgent unused5 = mtkDataConnection5.mNetworkAgent = mtkDataConnection5.mHandoverSourceNetworkAgent;
                    DcNetworkAgent access$13500 = MtkDataConnection.this.mNetworkAgent;
                    MtkDataConnection mtkDataConnection6 = MtkDataConnection.this;
                    access$13500.acquireOwnership(mtkDataConnection6, mtkDataConnection6.mTransportType);
                    MtkDataConnection.this.mNetworkAgent.sendNetworkCapabilities(MtkDataConnection.this.getNetworkCapabilities(), MtkDataConnection.this);
                    MtkDataConnection.this.mNetworkAgent.sendLinkProperties(MtkDataConnection.this.mLinkProperties, MtkDataConnection.this);
                    DcNetworkAgent unused6 = MtkDataConnection.this.mHandoverSourceNetworkAgent = null;
                } else {
                    MtkDataConnection.this.loge("Failed to get network agent from original data connection");
                    return;
                }
            }
            if (MtkDataConnection.this.mTransportType == 1) {
                MtkDataConnection.this.mPhone.mCi.registerForNattKeepaliveStatus(MtkDataConnection.this.getHandler(), 262162, (Object) null);
                MtkDataConnection.this.mPhone.mCi.registerForLceInfo(MtkDataConnection.this.getHandler(), 262167, (Object) null);
            }
            TelephonyMetrics.getInstance().writeRilDataCallEvent(MtkDataConnection.this.mPhone.getPhoneId(), MtkDataConnection.this.mCid, MtkDataConnection.this.mApnSetting.getApnTypeBitmask(), 1);
            try {
                IDataConnectionExt access$15700 = MtkDataConnection.this.mDataConnectionExt;
                if (MtkDataConnection.this.mApnSetting != null) {
                    strArr = ApnSetting.getApnTypesStringFromBitmask(MtkDataConnection.this.mApnSetting.getApnTypeBitmask()).split(",");
                }
                if (MtkDataConnection.this.mLinkProperties != null) {
                    str = MtkDataConnection.this.mLinkProperties.getInterfaceName();
                }
                access$15700.onDcActivated(strArr, str);
            } catch (Exception e2) {
                MtkDataConnection.this.loge("onDcActivated fail!");
                e2.printStackTrace();
            }
        }

        public void exit() {
            String[] strArr;
            try {
                IDataConnectionExt access$15700 = MtkDataConnection.this.mDataConnectionExt;
                if (MtkDataConnection.this.mApnSetting == null) {
                    strArr = null;
                } else {
                    strArr = ApnSetting.getApnTypesStringFromBitmask(MtkDataConnection.this.mApnSetting.getApnTypeBitmask()).split(",");
                }
                access$15700.onDcDeactivated(strArr, MtkDataConnection.this.mLinkProperties == null ? "" : MtkDataConnection.this.mLinkProperties.getInterfaceName());
            } catch (Exception e) {
                MtkDataConnection.this.loge("onDcDeactivated fail!");
                e.printStackTrace();
            }
            MtkDataConnection.super.exit();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:107:0x0395, code lost:
            if (0 != 0) goto L_0x0397;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:108:0x0397, code lost:
            r0 = (com.android.internal.telephony.dataconnection.DataConnection.ConnectionParams) ((android.os.AsyncResult) r13.obj).userObj;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:114:0x03ad, code lost:
            if (1 == 0) goto L_0x03b0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:115:0x03b0, code lost:
            if (r0 != null) goto L_0x03b5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:116:0x03b2, code lost:
            return true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:117:0x03b5, code lost:
            r5 = r12.this$0.onSetupConnectionCompleted(r13.arg1, r13.getData().getParcelable("data_call_response"), r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:118:0x03cc, code lost:
            if (r5 == com.android.internal.telephony.dataconnection.DataConnection.SetupResult.ERROR_STALE) goto L_0x03fa;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:120:0x03d4, code lost:
            if (com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$20100(r12.this$0) == r0) goto L_0x03fa;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:121:0x03d6, code lost:
            r12.this$0.loge("DcActiveState_FALLBACK_Retry: WEIRD mConnectionsParams:" + com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$20200(r12.this$0) + " != cp:" + r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:123:0x03fe, code lost:
            if (com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$100() == false) goto L_0x041e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:124:0x0400, code lost:
            r12.this$0.log("DcActiveState_FALLBACK_Retry onSetupConnectionCompleted result=" + r5 + " dc=" + r12.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:125:0x041e, code lost:
            r4 = com.mediatek.internal.telephony.dataconnection.MtkDataConnection.AnonymousClass3.$SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[r5.ordinal()];
         */
        /* JADX WARNING: Code restructure failed: missing block: B:126:0x0426, code lost:
            if (r4 == 1) goto L_0x051c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:128:0x0429, code lost:
            if (r4 == 4) goto L_0x0466;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:130:0x042c, code lost:
            if (r4 == 5) goto L_0x043d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:132:0x0432, code lost:
            if (com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$100() == false) goto L_0x0527;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:133:0x0434, code lost:
            r12.this$0.log("DcActiveState_FALLBACK_Retry: Another error cause, Not retry anymore");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:134:0x043d, code lost:
            r12.this$0.loge("DcActiveState_FALLBACK_Retry: stale EVENT_SETUP_DATA_CONNECTION_DONE tag:" + r0.mTag + " != mTag:" + r12.this$0.mTag + " Not retry anymore");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:135:0x0466, code lost:
            r1 = "DcActiveState_FALLBACK_Retry: ERROR_DATA_SERVICE_SPECIFIC_ERROR result=" + r5 + " result.isRadioRestartFailure=" + android.telephony.DataFailCause.isRadioRestartFailure(com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$20400(r12.this$0).getContext(), r5.mFailCause, com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$20500(r12.this$0).getSubId()) + " result.isPermanentFailure=" + com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$20600(r12.this$0).isPermanentFailure(r5.mFailCause);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:136:0x04b1, code lost:
            if (com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$100() == false) goto L_0x04b8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:137:0x04b3, code lost:
            r12.this$0.log(r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:139:0x04bc, code lost:
            if (r5.mFailCause != com.mediatek.internal.telephony.dataconnection.MtkDataConnection.RA_GET_IPV6_VALID_FAIL) goto L_0x050e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:141:0x04c2, code lost:
            if (r12.this$0.mDcFcMgr == null) goto L_0x0527;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:143:0x04ce, code lost:
            if (r12.this$0.mDcFcMgr.isSpecificNetworkAndSimOperator(com.mediatek.internal.telephony.dataconnection.DcFailCauseManager.Operator.OP19) == false) goto L_0x0527;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:144:0x04d0, code lost:
            com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$20708(r12.this$0);
            r8 = r12.this$0.mDcFcMgr.getRetryTimeByIndex(com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$20700(r12.this$0), com.mediatek.internal.telephony.dataconnection.DcFailCauseManager.Operator.OP19);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:145:0x04e9, code lost:
            if (r8 >= 0) goto L_0x04fe;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:147:0x04ef, code lost:
            if (com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$100() == false) goto L_0x04f8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:148:0x04f1, code lost:
            r12.this$0.log("DcActiveState_FALLBACK_Retry: No retry but at least one IPv4 or IPv6 is accepted");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:149:0x04f8, code lost:
            com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$20802(r12.this$0, 0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:150:0x04fe, code lost:
            com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$20902(r12.this$0, com.mediatek.internal.telephony.dataconnection.MtkDataConnection.RA_GET_IPV6_VALID_FAIL);
            r4 = r12.this$0;
            r4.startRetryAlarm(com.mediatek.internal.telephony.dataconnection.MtkDataConnection.EVENT_FALLBACK_RETRY_CONNECTION, r4.mTag, r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:152:0x0512, code lost:
            if (com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$100() == false) goto L_0x0527;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:153:0x0514, code lost:
            r12.this$0.log("DcActiveState_FALLBACK_Retry: ERROR_DATA_SERVICE_SPECIFIC_ERROR Not retry anymore");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:154:0x051c, code lost:
            com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$20302(r12.this$0, 0);
            com.mediatek.internal.telephony.dataconnection.MtkDataConnection.access$6400(r12.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:155:0x0527, code lost:
            return true;
         */
        public boolean processMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 262144:
                    DataConnection.ConnectionParams cp = (DataConnection.ConnectionParams) msg.obj;
                    MtkDataConnection.this.mApnContexts.put(cp.mApnContext, cp);
                    if (MtkDataConnection.DBG) {
                        MtkDataConnection.this.log("DcActiveState: EVENT_CONNECT cp=" + cp + " dc=" + MtkDataConnection.this);
                    }
                    if (MtkDataConnection.this.mNetworkAgent != null) {
                        MtkDataConnection.access$16472(MtkDataConnection.this, ~cp.mApnContext.getApnTypeBitmask());
                        NetworkCapabilities cap = MtkDataConnection.this.getNetworkCapabilities();
                        MtkDataConnection.this.mNetworkAgent.sendNetworkCapabilities(cap, MtkDataConnection.this);
                        if (MtkDataConnection.DBG) {
                            MtkDataConnection.this.log("DcActiveState: update Capabilities=" + cap);
                        }
                        if (TextUtils.equals(cp.mApnContext.getApnType(), "default") && MtkDataConnection.this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                            MtkDataConnection.this.log("DcActiveState: inform UI the added INTERNET capability.");
                            MtkDataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.SUSPENDED, null, MtkDataConnection.this.mNetworkInfo.getExtraInfo());
                            MtkDataConnection.this.mNetworkAgent.sendNetworkInfo(MtkDataConnection.this.mNetworkInfo);
                            MtkDataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, MtkDataConnection.this.mNetworkInfo.getExtraInfo());
                            MtkDataConnection.this.mNetworkAgent.sendNetworkInfo(MtkDataConnection.this.mNetworkInfo);
                        }
                    }
                    MtkDataConnection.this.checkIfDefaultApnReferenceCountChanged();
                    MtkDataConnection.this.notifyConnectCompleted(cp, 0, false);
                    return true;
                case 262145:
                    DataConnection.ConnectionParams cp2 = null;
                    try {
                        cp2 = (DataConnection.ConnectionParams) msg.obj;
                        break;
                    } catch (Exception e) {
                        MtkDataConnection.this.log("msg.obj can not be cast to ConnectionParams");
                        break;
                    } catch (Throwable th) {
                        if (1 != 0) {
                            DataConnection.ConnectionParams cp3 = (DataConnection.ConnectionParams) ((AsyncResult) msg.obj).userObj;
                        }
                        throw th;
                    }
                case 262148:
                    DataConnection.DisconnectParams dp = (DataConnection.DisconnectParams) msg.obj;
                    if (MtkDataConnection.DBG) {
                        MtkDataConnection.this.log("DcActiveState: EVENT_DISCONNECT dp=" + dp + " dc=" + MtkDataConnection.this);
                    }
                    if (MtkDataConnection.this.mApnContexts.containsKey(dp.mApnContext)) {
                        if (MtkDataConnection.DBG) {
                            MtkDataConnection.this.log("DcActiveState msg.what=EVENT_DISCONNECT RefCount=" + MtkDataConnection.this.mApnContexts.size());
                        }
                        if (MtkDataConnection.this.mApnContexts.size() == 1) {
                            if (!MtkDataConnection.this.hasMdAutoSetupImsCapability()) {
                                MtkDataConnection.this.handlePcscfErrorCause(dp);
                            }
                            MtkDataConnection.this.mApnContexts.clear();
                            DataConnection.DisconnectParams unused = MtkDataConnection.this.mDisconnectParams = dp;
                            DataConnection.ConnectionParams unused2 = MtkDataConnection.this.mConnectionParams = null;
                            dp.mTag = MtkDataConnection.this.mTag;
                            MtkDataConnection.this.tearDownData(dp);
                            MtkDataConnection mtkDataConnection = MtkDataConnection.this;
                            mtkDataConnection.transitionTo(mtkDataConnection.mDisconnectingState);
                        } else {
                            MtkDataConnection.this.mApnContexts.remove(dp.mApnContext);
                            if (MtkDataConnection.this.mNetworkAgent != null) {
                                MtkDataConnection.access$18676(MtkDataConnection.this, dp.mApnContext.getApnTypeBitmask());
                                NetworkCapabilities cap2 = MtkDataConnection.this.getNetworkCapabilities();
                                MtkDataConnection.this.mNetworkAgent.sendNetworkCapabilities(cap2, MtkDataConnection.this);
                                MtkDataConnection.this.log("DcActiveState update Capabilities:" + cap2);
                            }
                            MtkDataConnection.this.checkIfDefaultApnReferenceCountChanged();
                            MtkDataConnection.this.notifyDisconnectCompleted(dp, false);
                        }
                    } else {
                        MtkDataConnection.this.log("DcActiveState ERROR no such apnContext=" + dp.mApnContext + " in this dc=" + MtkDataConnection.this);
                        MtkDataConnection.this.notifyDisconnectCompleted(dp, false);
                    }
                    return true;
                case 262167:
                    if (!MtkDataConnection.this.canSendNetworkCapabilities()) {
                        return true;
                    }
                    return MtkDataConnection.super.processMessage(msg);
                case 262169:
                    if (MtkDataConnection.this.mNetworkAgent != null) {
                        return MtkDataConnection.super.processMessage(msg);
                    }
                    MtkDataConnection.this.log("EVENT_REEVALUATE_RESTRICTED_STATE: mNetworkAgent is null");
                    return true;
                case MtkDataConnection.EVENT_IPV4_ADDRESS_REMOVED /*{ENCODED_INT: 262171}*/:
                    AddressInfo addrV4Info = (AddressInfo) msg.obj;
                    if (MtkDataConnection.DBG) {
                        MtkDataConnection.this.log("DcActiveState: " + MtkDataConnection.this.getWhatToString(msg.what) + ": " + addrV4Info);
                    }
                    return true;
                case MtkDataConnection.EVENT_IPV6_ADDRESS_REMOVED /*{ENCODED_INT: 262172}*/:
                    AddressInfo addrV6Info = (AddressInfo) msg.obj;
                    if (MtkDataConnection.DBG) {
                        MtkDataConnection.this.log("DcActiveState: " + MtkDataConnection.this.getWhatToString(msg.what) + ": " + addrV6Info);
                    }
                    if (MtkDataConnection.this.mInterfaceName != null && MtkDataConnection.this.mInterfaceName.equals(addrV6Info.mIntfName)) {
                        String strAddress = addrV6Info.mLinkAddr.getAddress().getHostAddress();
                        if (MtkDataConnection.DBG) {
                            MtkDataConnection.this.log("strAddress: " + strAddress);
                        }
                        if (strAddress.equalsIgnoreCase("FE80::5A:5A:5A:23")) {
                            long unused3 = MtkDataConnection.this.mValid = -1;
                        } else if (strAddress.equalsIgnoreCase("FE80::5A:5A:5A:22")) {
                            long unused4 = MtkDataConnection.this.mValid = -2;
                        } else {
                            long unused5 = MtkDataConnection.this.mValid = -1000;
                        }
                        if (MtkDataConnection.this.mValid == -1 || MtkDataConnection.this.mValid == -2) {
                            MtkDataConnection.this.log("DcActiveState: RA initial or refresh fail, valid:" + MtkDataConnection.this.mValid);
                            MtkDataConnection.this.onAddressRemoved();
                        }
                    }
                    if (MtkDataConnection.this.mGlobalV6AddrInfo != null && MtkDataConnection.this.mGlobalV6AddrInfo.mIntfName.equals(addrV6Info.mIntfName)) {
                        AddressInfo unused6 = MtkDataConnection.this.mGlobalV6AddrInfo = null;
                    }
                    return true;
                case MtkDataConnection.EVENT_VOICE_CALL /*{ENCODED_INT: 262174}*/:
                    boolean unused7 = MtkDataConnection.this.mIsInVoiceCall = msg.arg1 != 0;
                    MtkDataConnection mtkDataConnection2 = MtkDataConnection.this;
                    if (msg.arg2 == 0) {
                        z = false;
                    }
                    boolean unused8 = mtkDataConnection2.mIsSupportConcurrent = z;
                    MtkDataConnection.this.updateNetworkInfoSuspendState();
                    if (MtkDataConnection.this.mNetworkAgent != null) {
                        MtkDataConnection.this.mNetworkAgent.sendNetworkInfo(MtkDataConnection.this.mNetworkInfo);
                    }
                    return true;
                case MtkDataConnection.EVENT_FALLBACK_RETRY_CONNECTION /*{ENCODED_INT: 262175}*/:
                    if (msg.arg1 == MtkDataConnection.this.mTag) {
                        if (MtkDataConnection.this.mDataRegState == 0) {
                            if (MtkDataConnection.DBG) {
                                MtkDataConnection.this.log("DcActiveState EVENT_FALLBACK_RETRY_CONNECTION mConnectionParams=" + MtkDataConnection.this.mConnectionParams);
                            }
                            MtkDataConnection mtkDataConnection3 = MtkDataConnection.this;
                            mtkDataConnection3.connect(mtkDataConnection3.mConnectionParams);
                        } else if (MtkDataConnection.DBG) {
                            MtkDataConnection.this.log("DcActiveState: EVENT_FALLBACK_RETRY_CONNECTION not in service");
                        }
                    } else if (MtkDataConnection.DBG) {
                        MtkDataConnection.this.log("DcActiveState stale EVENT_FALLBACK_RETRY_CONNECTION tag:" + msg.arg1 + " != mTag:" + MtkDataConnection.this.mTag);
                    }
                    return true;
                case MtkDataConnection.EVENT_IPV6_ADDRESS_UPDATED /*{ENCODED_INT: 262176}*/:
                    AddressInfo addrV6Info2 = (AddressInfo) msg.obj;
                    if (MtkDataConnection.this.mInterfaceName != null && MtkDataConnection.this.mInterfaceName.equals(addrV6Info2.mIntfName)) {
                        int scope = addrV6Info2.mLinkAddr.getScope();
                        int flag = addrV6Info2.mLinkAddr.getFlags();
                        MtkDataConnection.this.log("EVENT_IPV6_ADDRESS_UPDATED, scope: " + scope + ", flag: " + flag);
                        if (OsConstants.RT_SCOPE_UNIVERSE != scope || (flag & 1) == OsConstants.IFA_F_TEMPORARY || MtkDataConnection.this.mNetworkAgent == null) {
                            MtkDataConnection.this.log("EVENT_IPV6_ADDRESS_UPDATED, not notify global ipv6 address update");
                        } else {
                            AddressInfo unused9 = MtkDataConnection.this.mGlobalV6AddrInfo = addrV6Info2;
                            MtkDataConnection.this.mNetworkAgent.sendLinkProperties(MtkDataConnection.this.mtkGetLinkProperties(), MtkDataConnection.this);
                            MtkDataConnection.this.log("EVENT_IPV6_ADDRESS_UPDATED, notify global ipv6 address update");
                        }
                    }
                    return true;
                default:
                    return MtkDataConnection.super.processMessage(msg);
            }
        }
    }

    /* access modifiers changed from: protected */
    public long getSuggestedRetryDelay(DataCallResponse response) {
        if (response.getSuggestedRetryTime() < 0) {
            if (DBG) {
                log("No suggested retry delay.");
            }
            DcFailCauseManager dcFailCauseManager = this.mDcFcMgr;
            if (dcFailCauseManager != null) {
                return dcFailCauseManager.getSuggestedRetryDelayByOp(response.getLinkStatus());
            }
            return -2;
        } else if (response.getSuggestedRetryTime() != Integer.MAX_VALUE) {
            return (long) response.getSuggestedRetryTime();
        } else {
            if (!DBG) {
                return -1;
            }
            log("Modem suggested not retrying.");
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public String[] getApnType() {
        if (VDBG) {
            log("getApnType: mApnContexts.size() = " + this.mApnContexts.size());
        }
        if (this.mApnContexts.size() == 0) {
            return null;
        }
        String[] aryApnType = new String[this.mApnContexts.values().size()];
        int i = 0;
        for (DataConnection.ConnectionParams cp : this.mApnContexts.values()) {
            String apnType = cp.mApnContext.getApnType();
            if (VDBG) {
                log("getApnType: apnType = " + apnType);
            }
            aryApnType[i] = new String(apnType);
            i++;
        }
        return aryApnType;
    }

    private void notifyDefaultApnReferenceCountChanged(int refCount, int event) {
        Message msg = this.mDct.obtainMessage(event);
        msg.arg1 = refCount;
        AsyncResult.forMessage(msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: private */
    public void onSetupFallbackConnection(DataCallResponse response, DataConnection.ConnectionParams cp) {
        if (cp.mTag != this.mTag) {
            if (DBG) {
                log("onSetupFallbackConnection stale cp.tag=" + cp.mTag + ", mtag=" + this.mTag);
            }
            DataConnection.SetupResult setupResult = DataConnection.SetupResult.ERROR_STALE;
            return;
        }
        if (DBG) {
            log("onSetupFallbackConnection received successful DataCallResponse");
        }
        this.mCid = response.getId();
        this.mPcscfAddr = (String[]) response.getPcscfAddresses().toArray(new String[response.getPcscfAddresses().size()]);
        setConnectionRat(MtkDcHelper.decodeRat(response.getLinkStatus()), "setup fallback");
        DataConnection.SetupResult setupResult2 = updateLinkProperty(new DataCallResponse(0, response.getSuggestedRetryTime(), response.getId(), response.getLinkStatus(), response.getProtocolType(), response.getInterfaceName(), response.getAddresses(), response.getDnsAddresses(), response.getGatewayAddresses(), response.getPcscfAddresses(), response.getMtu())).setupResult;
        this.mInterfaceName = response.getInterfaceName();
        log("onSetupFallbackConnection: ifname-" + this.mInterfaceName);
    }

    private boolean isAddCapabilityByDataOption() {
        boolean isUserDataEnabled = this.mPhone.isUserDataEnabled();
        boolean isDataRoamingEnabled = this.mDct.getDataRoamingEnabled();
        log("addCapabilityByDataOption");
        if (!isUserDataEnabled) {
            return false;
        }
        if (!this.mPhone.getServiceState().getDataRoaming() || isDataRoamingEnabled) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isNwNeedSuspended() {
        boolean bImsOrEmergencyApn = MtkDcHelper.isImsOrEmergencyApn(getApnType());
        boolean bWifiCallingEnabled = this.mIsInVoiceCall ? MtkDcHelper.getInstance().isWifiCallingEnabled() : false;
        this.mIsSupportConcurrent = MtkDcHelper.getInstance().isDataSupportConcurrent(this.mPhone.getPhoneId());
        if (DBG) {
            log("isNwNeedSuspended: mIsInVoiceCall = " + this.mIsInVoiceCall + ", mIsSupportConcurrent = " + this.mIsSupportConcurrent + ", bImsOrEmergencyApn = " + bImsOrEmergencyApn + ", bWifiCallingEnabled = " + bWifiCallingEnabled);
        }
        if (!this.mIsInVoiceCall || this.mIsSupportConcurrent || bImsOrEmergencyApn || bWifiCallingEnabled) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public int getEventByAddress(boolean bUpdated, LinkAddress linkAddr) {
        InetAddress addr = linkAddr.getAddress();
        if (!bUpdated) {
            if (addr instanceof Inet6Address) {
                return EVENT_IPV6_ADDRESS_REMOVED;
            }
            if (addr instanceof Inet4Address) {
                return EVENT_IPV4_ADDRESS_REMOVED;
            }
            if (!DBG) {
                return -1;
            }
            loge("unknown address type, linkAddr: " + linkAddr);
            return -1;
        } else if (addr instanceof Inet6Address) {
            return EVENT_IPV6_ADDRESS_UPDATED;
        } else {
            if (!DBG) {
                return -1;
            }
            loge("unknown address type, linkAddr: " + linkAddr);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public void sendMessageForSM(int event, String iface, LinkAddress address) {
        if (event < 0) {
            loge("sendMessageForSM: Skip notify!!!");
            return;
        }
        AddressInfo addrInfo = new AddressInfo(iface, address);
        if (DBG) {
            log("sendMessageForSM: " + cmdToString(event) + ", addressInfo: " + addrInfo);
        }
        sendMessage(obtainMessage(event, addrInfo));
    }

    /* access modifiers changed from: private */
    public void onAddressRemoved() {
        if ((1 == this.mApnSetting.getProtocol() || 2 == this.mApnSetting.getProtocol()) && !isIpv4Connected()) {
            log("onAddressRemoved: IPv6 RA failed and didn't connect with IPv4");
            if (this.mApnContexts != null) {
                log("onAddressRemoved: mApnContexts size: " + this.mApnContexts.size());
                for (DataConnection.ConnectionParams cp : this.mApnContexts.values()) {
                    ApnContext apnContext = cp.mApnContext;
                    apnContext.getApnType();
                    if (apnContext.getState() == DctConstants.State.CONNECTED) {
                        log("onAddressRemoved: send message EVENT_DISCONNECT_ALL");
                        sendMessage(obtainMessage(262150, new DataConnection.DisconnectParams(apnContext, MtkGsmCdmaPhone.REASON_RA_FAILED, 2, this.mDct.obtainMessage(270351, new Pair<>(apnContext, Integer.valueOf(cp.mConnectionGeneration))))));
                        return;
                    }
                }
            }
        } else if (DBG) {
            log("onAddressRemoved: no need to remove");
        }
    }

    /* access modifiers changed from: package-private */
    public void checkIfDefaultApnReferenceCountChanged() {
        boolean isDefaultExisted = false;
        int sizeOfOthers = 0;
        for (DataConnection.ConnectionParams cp : this.mApnContexts.values()) {
            ApnContext apnContext = cp.mApnContext;
            if (TextUtils.equals("default", apnContext.getApnType()) && DctConstants.State.CONNECTED.equals(apnContext.getState())) {
                isDefaultExisted = true;
            } else if (DctConstants.State.CONNECTED.equals(apnContext.getState())) {
                sizeOfOthers++;
            }
        }
        if (isDefaultExisted) {
            if (DBG) {
                log("refCount = " + this.mApnContexts.size() + ", non-default refCount = " + sizeOfOthers);
            }
            notifyDefaultApnReferenceCountChanged(sizeOfOthers + 1, 270848);
        }
    }

    /* access modifiers changed from: private */
    public void registerNetworkAlertObserver() {
        if (this.mNetworkManager != null) {
            log("registerNetworkAlertObserver X");
            try {
                this.mNetworkManager.registerObserver(this.mAlertObserver);
                log("registerNetworkAlertObserver E");
            } catch (RemoteException e) {
                loge("registerNetworkAlertObserver failed E");
            }
        }
    }

    /* access modifiers changed from: private */
    public void unregisterNetworkAlertObserver() {
        if (this.mNetworkManager != null) {
            log("unregisterNetworkAlertObserver X");
            try {
                this.mNetworkManager.unregisterObserver(this.mAlertObserver);
                log("unregisterNetworkAlertObserver E");
            } catch (RemoteException e) {
                loge("unregisterNetworkAlertObserver failed E");
            }
            this.mInterfaceName = null;
        }
    }

    /* access modifiers changed from: private */
    public class AddressInfo {
        String mIntfName;
        LinkAddress mLinkAddr;

        public AddressInfo(String intfName, LinkAddress linkAddr) {
            this.mIntfName = intfName;
            this.mLinkAddr = linkAddr;
        }

        public String toString() {
            return "interfaceName: " + this.mIntfName + "/" + this.mLinkAddr;
        }
    }

    public void startRetryAlarm(int what, int tag, long delay) {
        Intent intent = new Intent(this.mActionRetry);
        intent.putExtra(INTENT_RETRY_ALARM_WHAT, what);
        intent.putExtra(INTENT_RETRY_ALARM_TAG, tag);
        if (DBG) {
            log("startRetryAlarm: next attempt in " + (delay / 1000) + "s what=" + what + " tag=" + tag);
        }
        this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + delay, PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728));
    }

    public void startRetryAlarmExact(int what, int tag, long delay) {
        Intent intent = new Intent(this.mActionRetry);
        intent.addFlags(268435456);
        intent.putExtra(INTENT_RETRY_ALARM_WHAT, what);
        intent.putExtra(INTENT_RETRY_ALARM_TAG, tag);
        if (DBG) {
            log("startRetryAlarmExact: next attempt in " + (delay / 1000) + "s what=" + what + " tag=" + tag);
        }
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728));
    }

    /* access modifiers changed from: private */
    public void resetRetryCount() {
        this.mRetryCount = 0;
        if (DBG) {
            log("resetRetryCount: " + this.mRetryCount);
        }
    }

    public void handlePcscfErrorCause(DataConnection.DisconnectParams dp) {
        CarrierConfigManager configMgr = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        int subId = this.mPhone.getSubId();
        if (configMgr == null) {
            loge("handlePcscfErrorCause() null configMgr!");
            return;
        }
        PersistableBundle b = configMgr.getConfigForSubId(subId);
        if (b == null) {
            loge("handlePcscfErrorCause() null config!");
            return;
        }
        boolean syncFailCause = b.getBoolean("ims_pdn_sync_fail_cause_to_modem_bool");
        log("handlePcscfErrorCause() syncFailCause: " + syncFailCause + ", subId: " + subId);
        if (syncFailCause && TextUtils.equals(dp.mApnContext.getApnType(), "ims")) {
            if (this.mPcscfAddr == null || this.mPcscfAddr.length <= 0) {
                dp.mReason = MtkGsmCdmaPhone.REASON_PCSCF_ADDRESS_FAILED;
                log("Disconnect with empty P-CSCF address");
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean hasMdAutoSetupImsCapability() {
        TelephonyDevController telephonyDevController = this.mTelDevController;
        if (telephonyDevController == null || telephonyDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasMdAutoSetupImsCapability()) {
            return false;
        }
        log("hasMdAutoSetupImsCapability: true");
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setConnectionRat(int newRat, String reason) {
        if (newRat + 1 > 4 || newRat < 0) {
            loge("setConnectionRat invalid newRat: " + newRat);
            return;
        }
        log("setConnectionRat newRat: " + newRat + " mRat: " + this.mRat + " reason: " + reason);
        this.mRat = newRat;
    }

    public void notifyVoiceCallEvent(boolean bInVoiceCall, boolean bSupportConcurrent) {
        sendMessage(EVENT_VOICE_CALL, bInVoiceCall ? 1 : 0, bSupportConcurrent ? 1 : 0);
    }

    /* access modifiers changed from: protected */
    public void mtkReplaceStates() {
        this.mDefaultState = new MtkDcDefaultState();
        this.mActivatingState = new MtkDcActivatingState();
        this.mActiveState = new MtkDcActiveState();
    }

    /* access modifiers changed from: protected */
    public LinkProperties mtkGetLinkProperties() {
        if (this.mGlobalV6AddrInfo == null) {
            return this.mLinkProperties;
        }
        LinkProperties linkProperties = new LinkProperties(this.mLinkProperties);
        Iterator<LinkAddress> it = linkProperties.getLinkAddresses().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            LinkAddress linkAddr = it.next();
            if (linkAddr.getAddress() instanceof Inet6Address) {
                linkProperties.removeLinkAddress(linkAddr);
                break;
            }
        }
        linkProperties.addLinkAddress(this.mGlobalV6AddrInfo.mLinkAddr);
        return linkProperties;
    }

    /* access modifiers changed from: protected */
    public void mtkSetApnContextReason(ApnContext alreadySent, String reason) {
        for (DataConnection.ConnectionParams cp : this.mApnContexts.values()) {
            ApnContext apnContext = cp.mApnContext;
            if (apnContext == alreadySent && MtkGsmCdmaPhone.REASON_RA_FAILED.equals(reason)) {
                log("set reason:" + reason);
                apnContext.setReason(reason);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void mtkCheckDefaultApnRefCount(ApnContext apnContext) {
        if (!this.mApnContexts.containsKey(apnContext)) {
            checkIfDefaultApnReferenceCountChanged();
        }
    }

    public void updateNetworkAgentSscMode3(int lifetime, int score) {
        sendMessage(EVENT_UPDATE_NETWORKAGENT_SSC_MODE3, lifetime, score);
    }

    /* access modifiers changed from: private */
    public void setSscMode3LingeringTime(int lifetime) {
        if (this.mNetworkAgent == null) {
            loge("Setting ssc mode3 lingering time without a NetworkAgent");
        } else if (lifetime >= 0) {
            try {
                if (methodQueueOrSendMessage != null) {
                    methodQueueOrSendMessage.invoke(this.mNetworkAgent, Integer.valueOf((int) EVENT_SET_LINGERING_TIME), Integer.valueOf(lifetime), 0);
                }
            } catch (Exception e) {
                loge("setSscMode3LingeringTime fail! " + e.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendNetworkScore(int score) {
        if (this.mNetworkAgent == null) {
            loge("Setting network score without a NetworkAgent");
        } else if (score >= 0) {
            this.mNetworkAgent.sendNetworkScore(score);
        }
    }

    private int mtkReplaceReason(int reason, ApnContext apnContext) {
        if (reason == 1 && TextUtils.equals(apnContext.getReason(), MtkGsmCdmaPhone.REASON_DATA_SETUP_SSC_MODE3)) {
            return ExternalSimConstants.MSG_ID_INITIALIZATION_RESPONSE;
        }
        return reason;
    }

    public void fakeNetworkAgent(ApnContext apnContext) {
        updateNetworkInfo();
        if (TextUtils.equals(apnContext.getApnType(), "mms")) {
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, this.mNetworkInfo.getReason(), null);
            this.mNetworkInfo.setExtraInfo("invalid_apn");
            this.mApnSetting = ApnSetting.makeApnSetting(0, "44010", "Fake APN", "fake_apn", null, -1, null, null, -1, "", "", -1, 2, 0, 0, true, 0, 0, false, 0, 0, 0, 0, -1, "");
        }
        NetworkMisc misc = new NetworkMisc();
        misc.subscriberId = this.mPhone.getSubscriberId();
        NetworkFactory factory = PhoneFactory.getNetworkFactory(this.mPhone.getPhoneId());
        this.mNetworkAgent = DcNetworkAgent.createDcNetworkAgent(this, this.mPhone, this.mNetworkInfo, 50, misc, factory == null ? -1 : factory.getSerialNumber(), this.mTransportType);
        clearSettings();
    }

    /* access modifiers changed from: private */
    public boolean canSendNetworkCapabilities() {
        if (MtkDcHelper.isPreferredDataPhone(this.mPhone)) {
            return true;
        }
        for (DataConnection.ConnectionParams cp : this.mApnContexts.values()) {
            if (TextUtils.equals(cp.mApnContext.getApnType(), "default")) {
                log("Not update network capabilities of default PDN for non-preferred data phone");
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        String name = getName();
        Rlog.d(name, "[Mtk] " + s);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        String name = getName();
        Rlog.d(name, "[Mtk] " + s);
    }

    /* access modifiers changed from: protected */
    public void logv(String s) {
        String name = getName();
        Rlog.v(name, "[Mtk] " + s);
    }

    /* access modifiers changed from: protected */
    public void logi(String s) {
        String name = getName();
        Rlog.i(name, "[Mtk] " + s);
    }

    /* access modifiers changed from: protected */
    public void logw(String s) {
        String name = getName();
        Rlog.w(name, "[Mtk] " + s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        String name = getName();
        Rlog.e(name, "[Mtk] " + s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s, Throwable e) {
        String name = getName();
        Rlog.e(name, "[Mtk] " + s, e);
    }

    public String toStringSimple() {
        return getName() + ": State=" + getCurrentState().getName() + " mApnSetting=" + this.mApnSetting + " RefCount=" + this.mApnContexts.size() + " mCid=" + this.mCid + " mCreateTime=" + this.mCreateTime + " mLastastFailTime=" + this.mLastFailTime + " mLastFailCause=" + this.mLastFailCause + " mTag=" + this.mTag + " mLinkProperties=" + this.mLinkProperties + " networkCapabilities=" + this.mNetworkCapabilities + " mRestrictedNetworkOverride=" + this.mRestrictedNetworkOverride;
    }

    /* access modifiers changed from: private */
    public long resetDelayTimeForAuTelstraOperator(long delay) {
        boolean isAuTelstraOperator = false;
        String operatorNumeric = TelephonyManager.getDefault().getNetworkOperatorForPhone(this.mPhone.getPhoneId());
        if (DBG) {
            log("DcActivatingState: operatorNumeric = " + operatorNumeric);
        }
        if (operatorNumeric != null) {
            int i = 0;
            while (true) {
                String[] strArr = sAuTelstraOperator;
                if (i >= strArr.length) {
                    break;
                } else if (operatorNumeric.startsWith(strArr[i])) {
                    isAuTelstraOperator = true;
                    if (DBG) {
                        log("isAuTelstraOperator");
                    }
                } else {
                    i++;
                }
            }
        }
        if (isAuTelstraOperator) {
            return 720000;
        }
        return delay;
    }
}

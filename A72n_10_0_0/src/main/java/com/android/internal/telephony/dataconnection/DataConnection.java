package com.android.internal.telephony.dataconnection;

import android.app.PendingIntent;
import android.net.KeepalivePacketData;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkRequest;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StringNetworkSpecifier;
import android.os.AsyncResult;
import android.os.Message;
import android.os.OppoManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.AccessNetworkConstants;
import android.telephony.DataFailCause;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.telephony.data.DataCallResponse;
import android.telephony.data.DataProfile;
import android.text.TextUtils;
import android.util.Pair;
import android.util.StatsLog;
import android.util.TimeUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.IOppoDataManager;
import com.android.internal.telephony.IOppoSmsManager;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.LinkCapacityEstimate;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.google.android.mms.pdu.CharacterSets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DataConnection extends StateMachine {
    protected static final int BASE = 262144;
    protected static final int CMD_TO_STRING_COUNT = 27;
    private static final String DATA_EVENT_ID = "050401";
    private static final boolean DBG = true;
    private static final int DEFAULT_INTERNET_CONNECTION_SCORE = 50;
    protected static final int EVENT_BW_REFRESH_RESPONSE = 262158;
    protected static final int EVENT_CONNECT = 262144;
    protected static final int EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED = 262155;
    protected static final int EVENT_DATA_CONNECTION_OVERRIDE_CHANGED = 262161;
    protected static final int EVENT_DATA_CONNECTION_ROAM_OFF = 262157;
    protected static final int EVENT_DATA_CONNECTION_ROAM_ON = 262156;
    protected static final int EVENT_DATA_CONNECTION_VOICE_CALL_ENDED = 262160;
    protected static final int EVENT_DATA_CONNECTION_VOICE_CALL_STARTED = 262159;
    public static final int EVENT_DATA_STATE_CHANGED = 262151;
    protected static final int EVENT_DEACTIVATE_DONE = 262147;
    protected static final int EVENT_DISCONNECT = 262148;
    protected static final int EVENT_DISCONNECT_ALL = 262150;
    protected static final int EVENT_KEEPALIVE_STARTED = 262163;
    protected static final int EVENT_KEEPALIVE_START_REQUEST = 262165;
    protected static final int EVENT_KEEPALIVE_STATUS = 262162;
    protected static final int EVENT_KEEPALIVE_STOPPED = 262164;
    protected static final int EVENT_KEEPALIVE_STOP_REQUEST = 262166;
    protected static final int EVENT_LINK_CAPACITY_CHANGED = 262167;
    public static final int EVENT_LOST_CONNECTION = 262153;
    protected static final int EVENT_REEVALUATE_DATA_CONNECTION_PROPERTIES = 262170;
    protected static final int EVENT_REEVALUATE_RESTRICTED_STATE = 262169;
    protected static final int EVENT_RESET = 262168;
    public static final int EVENT_RIL_CONNECTED = 262149;
    protected static final int EVENT_SETUP_DATA_CONNECTION_DONE = 262145;
    protected static final int EVENT_TEAR_DOWN_NOW = 262152;
    protected static final int HANDOVER_STATE_BEING_TRANSFERRED = 2;
    public static final int HANDOVER_STATE_COMPLETED = 3;
    private static final int HANDOVER_STATE_IDLE = 1;
    private static final String NETWORK_TYPE = "MOBILE";
    private static final String NULL_IP = "0.0.0.0";
    private static final int OTHER_CONNECTION_SCORE = 9;
    private static final String RAT_NAME_5G = "nr";
    private static final String RAT_NAME_EVDO = "evdo";
    private static final String TCP_BUFFER_SIZES_1XRTT = "16384,32768,131072,4096,16384,102400";
    private static final String TCP_BUFFER_SIZES_EDGE = "4093,26280,70800,4096,16384,70800";
    private static final String TCP_BUFFER_SIZES_EHRPD = "131072,262144,1048576,4096,16384,524288";
    private static final String TCP_BUFFER_SIZES_EVDO = "4094,87380,262144,4096,16384,262144";
    private static final String TCP_BUFFER_SIZES_GPRS = "4092,8760,48000,4096,8760,48000";
    private static final String TCP_BUFFER_SIZES_HSDPA = "61167,367002,1101005,8738,52429,262114";
    private static final String TCP_BUFFER_SIZES_HSPA = "40778,244668,734003,16777,100663,301990";
    private static final String TCP_BUFFER_SIZES_HSPAP = "122334,734003,2202010,32040,192239,576717";
    public static String TCP_BUFFER_SIZES_LTE = "524288,1048576,2097152,262144,524288,1048576";
    private static final String TCP_BUFFER_SIZES_NR = "2097152,6291456,16777216,512000,2097152,8388608";
    private static final String TCP_BUFFER_SIZES_UMTS = "58254,349525,1048576,58254,349525,1048576";
    private static final boolean VDBG = true;
    private static AtomicInteger mInstanceNumber = new AtomicInteger(0);
    protected static String[] sCmdToString = new String[27];
    protected AsyncChannel mAc;
    protected DcActivatingState mActivatingState = new DcActivatingState();
    protected DcActiveState mActiveState = new DcActiveState();
    protected final Map<ApnContext, ConnectionParams> mApnContexts = new ConcurrentHashMap();
    protected ApnSetting mApnSetting;
    public int mCid;
    protected ConnectionParams mConnectionParams;
    protected long mCreateTime;
    protected int mDataRegState = KeepaliveStatus.INVALID_HANDLE;
    protected DataServiceManager mDataServiceManager;
    protected DcController mDcController;
    protected int mDcFailCause;
    protected DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    protected DcTracker mDct = null;
    protected DcDefaultState mDefaultState = new DcDefaultState();
    protected int mDisabledApnTypeBitMask = 0;
    protected DisconnectParams mDisconnectParams;
    protected DcDisconnectionErrorCreatingConnection mDisconnectingErrorCreatingConnection = new DcDisconnectionErrorCreatingConnection();
    protected DcDisconnectingState mDisconnectingState = new DcDisconnectingState();
    protected DcNetworkAgent mHandoverSourceNetworkAgent;
    protected int mHandoverState;
    protected int mId;
    protected DcInactiveState mInactiveState = new DcInactiveState();
    protected int mLastFailCause;
    protected long mLastFailTime;
    protected LinkProperties mLinkProperties = new LinkProperties();
    protected DcNetworkAgent mNetworkAgent;
    protected NetworkInfo mNetworkInfo;
    protected String[] mPcscfAddr;
    protected Phone mPhone;
    protected PendingIntent mReconnectIntent = null;
    protected boolean mRestrictedNetworkOverride = false;
    protected int mRilRat = KeepaliveStatus.INVALID_HANDLE;
    protected int mScore;
    private int mSubId;
    protected int mSubscriptionOverride;
    public int mTag;
    private final String mTagSuffix;
    protected final int mTransportType;
    protected boolean mUnmeteredUseOnly = false;
    protected Object mUserData;

    @Retention(RetentionPolicy.SOURCE)
    public @interface HandoverState {
    }

    static {
        String[] strArr = sCmdToString;
        strArr[0] = "EVENT_CONNECT";
        strArr[1] = "EVENT_SETUP_DATA_CONNECTION_DONE";
        strArr[3] = "EVENT_DEACTIVATE_DONE";
        strArr[4] = "EVENT_DISCONNECT";
        strArr[5] = "EVENT_RIL_CONNECTED";
        strArr[6] = "EVENT_DISCONNECT_ALL";
        strArr[7] = "EVENT_DATA_STATE_CHANGED";
        strArr[8] = "EVENT_TEAR_DOWN_NOW";
        strArr[9] = "EVENT_LOST_CONNECTION";
        strArr[11] = "EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED";
        strArr[12] = "EVENT_DATA_CONNECTION_ROAM_ON";
        strArr[13] = "EVENT_DATA_CONNECTION_ROAM_OFF";
        strArr[14] = "EVENT_BW_REFRESH_RESPONSE";
        strArr[15] = "EVENT_DATA_CONNECTION_VOICE_CALL_STARTED";
        strArr[16] = "EVENT_DATA_CONNECTION_VOICE_CALL_ENDED";
        strArr[17] = "EVENT_DATA_CONNECTION_OVERRIDE_CHANGED";
        strArr[18] = "EVENT_KEEPALIVE_STATUS";
        strArr[19] = "EVENT_KEEPALIVE_STARTED";
        strArr[20] = "EVENT_KEEPALIVE_STOPPED";
        strArr[21] = "EVENT_KEEPALIVE_START_REQUEST";
        strArr[22] = "EVENT_KEEPALIVE_STOP_REQUEST";
        strArr[23] = "EVENT_LINK_CAPACITY_CHANGED";
        strArr[24] = "EVENT_RESET";
        strArr[25] = "EVENT_REEVALUATE_RESTRICTED_STATE";
        strArr[26] = "EVENT_REEVALUATE_DATA_CONNECTION_PROPERTIES";
    }

    public static class ConnectionParams {
        public ApnContext mApnContext;
        public final int mConnectionGeneration;
        Message mOnCompletedMsg;
        public int mProfileId;
        public final int mRequestType;
        public int mRilRat;
        public final int mSubId;
        public int mTag;

        public ApnContext getApnContext() {
            return this.mApnContext;
        }

        public ConnectionParams(ApnContext apnContext, int profileId, int rilRadioTechnology, Message onCompletedMsg, int connectionGeneration, int requestType, int subId) {
            this.mApnContext = apnContext;
            this.mProfileId = profileId;
            this.mRilRat = rilRadioTechnology;
            this.mOnCompletedMsg = onCompletedMsg;
            this.mConnectionGeneration = connectionGeneration;
            this.mRequestType = requestType;
            this.mSubId = subId;
        }

        public String toString() {
            return "{mTag=" + this.mTag + " mApnContext=" + this.mApnContext + " mProfileId=" + this.mProfileId + " mRat=" + this.mRilRat + " mOnCompletedMsg=" + DataConnection.msgToString(this.mOnCompletedMsg) + " mRequestType=" + DcTracker.requestTypeToString(this.mRequestType) + " mSubId=" + this.mSubId + "}";
        }
    }

    public static class DisconnectParams {
        public ApnContext mApnContext;
        Message mOnCompletedMsg;
        public String mReason;
        public final int mReleaseType;
        public int mTag;

        public DisconnectParams(ApnContext apnContext, String reason, int releaseType, Message onCompletedMsg) {
            this.mApnContext = apnContext;
            this.mReason = reason;
            this.mReleaseType = releaseType;
            this.mOnCompletedMsg = onCompletedMsg;
        }

        public String toString() {
            return "{mTag=" + this.mTag + " mApnContext=" + this.mApnContext + " mReason=" + this.mReason + " mReleaseType=" + DcTracker.releaseTypeToString(this.mReleaseType) + " mOnCompletedMsg=" + DataConnection.msgToString(this.mOnCompletedMsg) + "}";
        }
    }

    public static String cmdToString(int cmd) {
        String value = null;
        int cmd2 = cmd - InboundSmsTracker.DEST_PORT_FLAG_3GPP2;
        if (cmd2 >= 0) {
            String[] strArr = sCmdToString;
            if (cmd2 < strArr.length) {
                value = strArr[cmd2];
            }
        }
        if (value != null) {
            return value;
        }
        return "0x" + Integer.toHexString(InboundSmsTracker.DEST_PORT_FLAG_3GPP2 + cmd2);
    }

    public static DataConnection makeDataConnection(Phone phone, int id, DcTracker dct, DataServiceManager dataServiceManager, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        String transportType;
        if (dataServiceManager.getTransportType() == 1) {
            transportType = "C";
        } else {
            transportType = "I";
        }
        TelephonyComponentFactory inject = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName());
        DataConnection dc = inject.makeDataConnection(phone, transportType + "-" + mInstanceNumber.incrementAndGet(), id, dct, dataServiceManager, failBringUpAll, dcc);
        dc.start();
        dc.log("Made " + dc.getName());
        return dc;
    }

    /* access modifiers changed from: package-private */
    public void dispose() {
        log("dispose: call quiteNow()");
        quitNow();
    }

    public LinkProperties getLinkProperties() {
        return new LinkProperties(this.mLinkProperties);
    }

    public boolean isInactive() {
        return getCurrentState() == this.mInactiveState;
    }

    public boolean isDisconnecting() {
        return getCurrentState() == this.mDisconnectingState;
    }

    public boolean isActive() {
        return getCurrentState() == this.mActiveState;
    }

    public boolean isActivating() {
        return getCurrentState() == this.mActivatingState;
    }

    public boolean hasBeenTransferred() {
        return this.mHandoverState == 3;
    }

    public int getCid() {
        return this.mCid;
    }

    /* access modifiers changed from: package-private */
    public ApnSetting getApnSetting() {
        return this.mApnSetting;
    }

    /* access modifiers changed from: package-private */
    public void setLinkPropertiesHttpProxy(ProxyInfo proxy) {
        DcNetworkAgent dcNetworkAgent;
        ProxyInfo oldProxy = this.mLinkProperties.getHttpProxy();
        this.mLinkProperties.setHttpProxy(proxy);
        log("setLinkPropertiesHttpProxy, oldProxy:" + oldProxy + ", newproxy:" + proxy + ", mNetworkAgent:" + this.mNetworkAgent);
        if (((oldProxy == null && proxy != null) || (oldProxy != null && !oldProxy.equals(proxy))) && (dcNetworkAgent = this.mNetworkAgent) != null) {
            dcNetworkAgent.sendLinkProperties(mtkGetLinkProperties(), this);
        }
    }

    public static class UpdateLinkPropertyResult {
        public LinkProperties newLp;
        public LinkProperties oldLp;
        public SetupResult setupResult = SetupResult.SUCCESS;

        public UpdateLinkPropertyResult(LinkProperties curLp) {
            this.oldLp = curLp;
            this.newLp = curLp;
        }
    }

    public enum SetupResult {
        SUCCESS,
        ERROR_RADIO_NOT_AVAILABLE,
        ERROR_INVALID_ARG,
        ERROR_STALE,
        ERROR_DATA_SERVICE_SPECIFIC_ERROR;
        
        public int mFailCause = DataFailCause.getFailCause(0);

        private SetupResult() {
        }

        public String toString() {
            return name() + "  SetupResult.mFailCause=" + this.mFailCause;
        }
    }

    public boolean isIpv4Connected() {
        for (InetAddress addr : this.mLinkProperties.getAddresses()) {
            if (addr instanceof Inet4Address) {
                Inet4Address i4addr = (Inet4Address) addr;
                if (!i4addr.isAnyLocalAddress() && !i4addr.isLinkLocalAddress() && !i4addr.isLoopbackAddress() && !i4addr.isMulticastAddress()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isIpv6Connected() {
        for (InetAddress addr : this.mLinkProperties.getAddresses()) {
            if (addr instanceof Inet6Address) {
                Inet6Address i6addr = (Inet6Address) addr;
                if (!i6addr.isAnyLocalAddress() && !i6addr.isLinkLocalAddress() && !i6addr.isLoopbackAddress() && !i6addr.isMulticastAddress()) {
                    return true;
                }
            }
        }
        return false;
    }

    @VisibleForTesting
    public UpdateLinkPropertyResult updateLinkProperty(DataCallResponse newState) {
        DcNetworkAgent dcNetworkAgent;
        UpdateLinkPropertyResult result = new UpdateLinkPropertyResult(this.mLinkProperties);
        if (newState == null) {
            return result;
        }
        result.newLp = new LinkProperties();
        result.setupResult = setLinkProperties(newState, result.newLp);
        if (result.setupResult != SetupResult.SUCCESS) {
            log("updateLinkProperty failed : " + result.setupResult);
            return result;
        }
        result.newLp.setHttpProxy(this.mLinkProperties.getHttpProxy());
        checkSetMtu(this.mApnSetting, result.newLp);
        this.mLinkProperties = result.newLp;
        updateTcpBufferSizes(this.mRilRat);
        if (!result.oldLp.equals(result.newLp)) {
            log("updateLinkProperty old LP=" + result.oldLp);
            log("updateLinkProperty new LP=" + result.newLp);
        }
        if (!result.newLp.equals(result.oldLp) && (dcNetworkAgent = this.mNetworkAgent) != null) {
            dcNetworkAgent.sendLinkProperties(mtkGetLinkProperties(), this);
        }
        return result;
    }

    private void checkSetMtu(ApnSetting apn, LinkProperties lp) {
        if (!((IOppoDataManager) OppoTelephonyFactory.getInstance().getFeature(IOppoDataManager.DEFAULT, new Object[0])).oemCheckSetMtu(apn, lp, this.mPhone) && lp != null && apn != null) {
            if (lp.getMtu() != 0) {
                log("MTU set by call response to: " + lp.getMtu());
            } else if (apn.getMtu() != 0) {
                lp.setMtu(apn.getMtu());
                log("MTU set by APN to: " + apn.getMtu());
            } else {
                int mtu = this.mPhone.getContext().getResources().getInteger(17694846);
                if (mtu != 0) {
                    lp.setMtu(mtu);
                    log("MTU set by config resource to: " + mtu);
                }
            }
        }
    }

    public DataConnection(Phone phone, String tagSuffix, int id, DcTracker dct, DataServiceManager dataServiceManager, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        super("DC-" + tagSuffix, dcc.getHandler());
        this.mTagSuffix = tagSuffix;
        setLogRecSize(300);
        setLogOnlyTransitions(true);
        log("DataConnection created");
        this.mPhone = phone;
        this.mDct = dct;
        this.mDataServiceManager = dataServiceManager;
        this.mTransportType = dataServiceManager.getTransportType();
        this.mDcTesterFailBringUpAll = failBringUpAll;
        this.mDcController = dcc;
        this.mId = id;
        this.mCid = -1;
        ServiceState ss = this.mPhone.getServiceState();
        this.mRilRat = ss.getRilDataRadioTechnology();
        this.mDataRegState = this.mPhone.getServiceState().getDataRegState();
        int networkType = ss.getDataNetworkType();
        this.mNetworkInfo = new NetworkInfo(0, networkType, NETWORK_TYPE, TelephonyManager.getNetworkTypeName(networkType));
        this.mNetworkInfo.setRoaming(ss.getDataRoaming());
        this.mNetworkInfo.setIsAvailable(true);
        mtkReplaceStates();
        addState(this.mDefaultState);
        addState(this.mInactiveState, this.mDefaultState);
        addState(this.mActivatingState, this.mDefaultState);
        addState(this.mActiveState, this.mDefaultState);
        addState(this.mDisconnectingState, this.mDefaultState);
        addState(this.mDisconnectingErrorCreatingConnection, this.mDefaultState);
        setInitialState(this.mInactiveState);
    }

    /* access modifiers changed from: protected */
    public int getHandoverSourceTransport() {
        if (this.mTransportType == 1) {
            return 2;
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public int connect(ConnectionParams cp) {
        log("connect: carrier='" + this.mApnSetting.getEntryName() + "' APN='" + this.mApnSetting.getApnName() + "' proxy='" + this.mApnSetting.getProxyAddressAsString() + "' port='" + this.mApnSetting.getProxyPort() + "'");
        if (cp.mApnContext != null) {
            cp.mApnContext.requestLog("DataConnection.connect");
        }
        if (((IOppoDataManager) OppoTelephonyFactory.getInstance().getFeature(IOppoDataManager.DEFAULT, new Object[0])).handleDataBlockControl(this.mPhone, cp, cp.mApnContext, this, EVENT_SETUP_DATA_CONNECTION_DONE)) {
            return 0;
        }
        boolean allowRoaming = true;
        if (this.mDcTesterFailBringUpAll.getDcFailBringUp().mCounter > 0) {
            DataCallResponse response = new DataCallResponse(this.mDcTesterFailBringUpAll.getDcFailBringUp().mFailCause, this.mDcTesterFailBringUpAll.getDcFailBringUp().mSuggestedRetryTime, 0, 0, 0, PhoneConfigurationManager.SSSS, (List) null, (List) null, (List) null, (List) null, 0);
            Message msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
            AsyncResult.forMessage(msg, response, (Throwable) null);
            if (msg != null) {
                msg.obj = cp;
                msg.getData().putParcelable(DataServiceManager.DATA_CALL_RESPONSE, response);
            }
            sendMessage(msg);
            log("connect: FailBringUpAll=" + this.mDcTesterFailBringUpAll.getDcFailBringUp() + " send error response=" + response);
            DcFailBringUp dcFailBringUp = this.mDcTesterFailBringUpAll.getDcFailBringUp();
            dcFailBringUp.mCounter = dcFailBringUp.mCounter - 1;
            return 0;
        }
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = 0;
        Message msg2 = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
        msg2.obj = cp;
        DataProfile dp = DcTracker.createDataProfile(this.mApnSetting, cp.mProfileId, this.mApnSetting.equals(this.mDct.getPreferredApn()));
        boolean isModemRoaming = this.mPhone.getServiceState().getDataRoamingFromRegistration();
        if (!this.mPhone.getDataRoamingEnabled() && (!isModemRoaming || this.mPhone.getServiceState().getDataRoaming())) {
            allowRoaming = false;
        }
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
        IOppoSmsManager manager = (IOppoSmsManager) OppoTelephonyFactory.getInstance().getFeature(IOppoSmsManager.DEFAULT, new Object[0]);
        if (manager != null) {
            allowRoaming = manager.oemAllowMmsWhenDataDisableInRoamingForDataConnection(cp, isModemRoaming, allowRoaming);
        }
        this.mDataServiceManager.setupDataCall(ServiceState.rilRadioTechnologyToAccessNetworkType(cp.mRilRat), dp, isModemRoaming, allowRoaming, reason, linkProperties, msg2);
        TelephonyMetrics.getInstance().writeSetupDataCall(this.mPhone.getPhoneId(), cp.mRilRat, dp.getProfileId(), dp.getApn(), dp.getProtocolType());
        return 0;
    }

    public void onSubscriptionOverride(int overrideMask, int overrideValue) {
        this.mSubscriptionOverride = (this.mSubscriptionOverride & (~overrideMask)) | (overrideValue & overrideMask);
        sendMessage(obtainMessage(EVENT_DATA_CONNECTION_OVERRIDE_CHANGED));
    }

    /* access modifiers changed from: protected */
    public void tearDownData(Object o) {
        int discReason = 1;
        ApnContext apnContext = null;
        if (o != null && (o instanceof DisconnectParams)) {
            DisconnectParams dp = (DisconnectParams) o;
            apnContext = dp.mApnContext;
            if (TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_RADIO_TURNED_OFF) || TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_PDP_RESET)) {
                discReason = 2;
            } else if (dp.mReleaseType == 3) {
                discReason = 3;
            }
        }
        String str = "tearDownData. mCid=" + this.mCid + ", reason=" + discReason;
        log(str);
        if (apnContext != null) {
            apnContext.requestLog(str);
        }
        this.mDataServiceManager.deactivateDataCall(this.mCid, discReason, obtainMessage(EVENT_DEACTIVATE_DONE, this.mTag, 0, o));
    }

    /* access modifiers changed from: protected */
    public void notifyAllWithEvent(ApnContext alreadySent, int event, String reason) {
        NetworkInfo networkInfo = this.mNetworkInfo;
        networkInfo.setDetailedState(networkInfo.getDetailedState(), reason, this.mNetworkInfo.getExtraInfo());
        for (ConnectionParams cp : this.mApnContexts.values()) {
            ApnContext apnContext = cp.mApnContext;
            if (apnContext != alreadySent) {
                if (reason != null) {
                    apnContext.setReason(reason);
                }
                Message msg = this.mDct.obtainMessage(event, this.mCid, cp.mRequestType, new Pair<>(apnContext, Integer.valueOf(cp.mConnectionGeneration)));
                AsyncResult.forMessage(msg);
                msg.sendToTarget();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyConnectCompleted(ConnectionParams cp, int cause, boolean sendAll) {
        ApnContext alreadySent = null;
        if (!(cp == null || cp.mOnCompletedMsg == null)) {
            Message connectionCompletedMsg = cp.mOnCompletedMsg;
            cp.mOnCompletedMsg = null;
            alreadySent = cp.mApnContext;
            long timeStamp = System.currentTimeMillis();
            connectionCompletedMsg.arg1 = this.mCid;
            connectionCompletedMsg.arg2 = cp.mRequestType;
            if (cause == 0) {
                this.mCreateTime = timeStamp;
                AsyncResult.forMessage(connectionCompletedMsg);
            } else {
                this.mLastFailCause = cause;
                this.mLastFailTime = timeStamp;
                if (cause == 0) {
                    cause = InboundSmsTracker.DEST_PORT_FLAG_NO_PORT;
                }
                AsyncResult.forMessage(connectionCompletedMsg, Integer.valueOf(cause), new Throwable(DataFailCause.toString(cause)));
            }
            log("notifyConnectCompleted at " + timeStamp + " cause=" + cause + " connectionCompletedMsg=" + msgToString(connectionCompletedMsg));
            connectionCompletedMsg.sendToTarget();
        }
        if (sendAll) {
            log("Send to all. " + alreadySent + " " + DataFailCause.toString(cause));
            notifyAllWithEvent(alreadySent, 270371, DataFailCause.toString(cause));
        }
    }

    /* access modifiers changed from: protected */
    public void notifyDisconnectCompleted(DisconnectParams dp, boolean sendAll) {
        log("NotifyDisconnectCompleted");
        ApnContext alreadySent = null;
        String reason = null;
        if (!(dp == null || dp.mOnCompletedMsg == null)) {
            Message msg = dp.mOnCompletedMsg;
            dp.mOnCompletedMsg = null;
            if (msg.obj instanceof ApnContext) {
                alreadySent = (ApnContext) msg.obj;
                mtkSetApnContextReason(alreadySent, dp.mReason);
            }
            reason = dp.mReason;
            Object[] objArr = new Object[2];
            objArr[0] = msg.toString();
            objArr[1] = msg.obj instanceof String ? (String) msg.obj : "<no-reason>";
            log(String.format("msg=%s msg.obj=%s", objArr));
            AsyncResult.forMessage(msg);
            msg.sendToTarget();
        }
        if (sendAll) {
            if (reason == null) {
                reason = DataFailCause.toString(InboundSmsTracker.DEST_PORT_FLAG_NO_PORT);
            }
            notifyAllWithEvent(alreadySent, 270351, reason);
        }
        log("NotifyDisconnectCompleted DisconnectParams=" + dp);
    }

    public int getDataConnectionId() {
        return this.mId;
    }

    /* access modifiers changed from: protected */
    public void clearSettings() {
        log("clearSettings");
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = 0;
        this.mCid = -1;
        this.mPcscfAddr = new String[5];
        this.mLinkProperties = new LinkProperties();
        this.mApnContexts.clear();
        this.mApnSetting = null;
        this.mUnmeteredUseOnly = false;
        this.mRestrictedNetworkOverride = false;
        this.mDcFailCause = 0;
        this.mDisabledApnTypeBitMask = 0;
        this.mSubId = -1;
    }

    /* access modifiers changed from: protected */
    public SetupResult onSetupConnectionCompleted(int resultCode, DataCallResponse response, ConnectionParams cp) {
        SetupResult result;
        log("onSetupConnectionCompleted: resultCode=" + resultCode + ", response=" + response);
        if (cp.mTag != this.mTag) {
            log("onSetupConnectionCompleted stale cp.tag=" + cp.mTag + ", mtag=" + this.mTag);
            result = SetupResult.ERROR_STALE;
        } else if (resultCode == 4) {
            result = SetupResult.ERROR_RADIO_NOT_AVAILABLE;
            result.mFailCause = 65537;
        } else if (response.getCause() == 0) {
            log("onSetupConnectionCompleted received successful DataCallResponse");
            this.mCid = response.getId();
            this.mPcscfAddr = (String[]) response.getPcscfAddresses().stream().map($$Lambda$XZAGhHrbkIDyusER4MAM6luKcT0.INSTANCE).toArray($$Lambda$DataConnection$tFSpFGzTv_UdpzJlTMOvg8VO98.INSTANCE);
            result = updateLinkProperty(response).setupResult;
        } else if (response.getCause() == 65537) {
            result = SetupResult.ERROR_RADIO_NOT_AVAILABLE;
            result.mFailCause = 65537;
        } else {
            result = SetupResult.ERROR_DATA_SERVICE_SPECIFIC_ERROR;
            result.mFailCause = DataFailCause.getFailCause(response.getCause());
        }
        this.mPhone.getServiceStateTracker().oppoAddDataCallCount();
        return result;
    }

    static /* synthetic */ String[] lambda$onSetupConnectionCompleted$0(int x$0) {
        return new String[x$0];
    }

    private boolean isDnsOk(String[] domainNameServers) {
        if (!NULL_IP.equals(domainNameServers[0]) || !NULL_IP.equals(domainNameServers[1]) || this.mPhone.isDnsCheckDisabled() || isIpAddress(this.mApnSetting.getMmsProxyAddressAsString())) {
            return true;
        }
        log(String.format("isDnsOk: return false apn.types=%d APN_TYPE_MMS=%s isIpAddress(%s)=%s", Integer.valueOf(this.mApnSetting.getApnTypeBitmask()), "mms", this.mApnSetting.getMmsProxyAddressAsString(), Boolean.valueOf(isIpAddress(this.mApnSetting.getMmsProxyAddressAsString()))));
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateTcpBufferSizes(int rilRat) {
        String sizes = null;
        if (rilRat == 19) {
            rilRat = 14;
        }
        String ratName = ServiceState.rilRadioTechnologyToString(rilRat).toLowerCase(Locale.ROOT);
        if (rilRat == 7 || rilRat == 8 || rilRat == 12) {
            ratName = RAT_NAME_EVDO;
        }
        if (rilRat == 14 && isNRConnected()) {
            ratName = RAT_NAME_5G;
        }
        String[] configOverride = this.mPhone.getContext().getResources().getStringArray(17236047);
        int i = 0;
        while (true) {
            if (i >= configOverride.length) {
                break;
            }
            String[] split = configOverride[i].split(":");
            if (ratName.equals(split[0]) && split.length == 2) {
                sizes = split[1];
                break;
            }
            i++;
        }
        if (sizes == null) {
            if (rilRat == 1) {
                sizes = TCP_BUFFER_SIZES_GPRS;
            } else if (rilRat == 2) {
                sizes = TCP_BUFFER_SIZES_EDGE;
            } else if (rilRat != 3) {
                if (rilRat != 19) {
                    if (rilRat != 20) {
                        switch (rilRat) {
                            case 6:
                                sizes = TCP_BUFFER_SIZES_1XRTT;
                                break;
                            case 7:
                            case 8:
                            case 12:
                                sizes = TCP_BUFFER_SIZES_EVDO;
                                break;
                            case 9:
                                sizes = TCP_BUFFER_SIZES_HSDPA;
                                break;
                            case 10:
                            case 11:
                                sizes = TCP_BUFFER_SIZES_HSPA;
                                break;
                            case 13:
                                sizes = TCP_BUFFER_SIZES_EHRPD;
                                break;
                            case 15:
                                sizes = TCP_BUFFER_SIZES_HSPAP;
                                break;
                        }
                    } else {
                        sizes = TCP_BUFFER_SIZES_NR;
                    }
                }
                if (isNRConnected()) {
                    sizes = TCP_BUFFER_SIZES_NR;
                } else {
                    sizes = TCP_BUFFER_SIZES_LTE;
                }
            } else {
                sizes = TCP_BUFFER_SIZES_UMTS;
            }
        }
        this.mLinkProperties.setTcpBufferSizes(sizes);
    }

    /* access modifiers changed from: protected */
    public boolean shouldRestrictNetwork() {
        boolean isAnyRestrictedRequest = false;
        Iterator<ApnContext> it = this.mApnContexts.keySet().iterator();
        while (true) {
            if (it.hasNext()) {
                if (it.next().hasRestrictedRequests(true)) {
                    isAnyRestrictedRequest = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (!isAnyRestrictedRequest || !ApnSettingUtils.isMetered(this.mApnSetting, this.mPhone)) {
            return false;
        }
        if (!this.mPhone.getDataEnabledSettings().isDataEnabled()) {
            return true;
        }
        if (this.mDct.getDataRoamingEnabled() || !this.mPhone.getServiceState().getDataRoaming()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isUnmeteredUseOnly() {
        if (this.mTransportType == 2 || this.mPhone.getDataEnabledSettings().isDataEnabled()) {
            return false;
        }
        if (this.mDct.getDataRoamingEnabled() && this.mPhone.getServiceState().getDataRoaming()) {
            return false;
        }
        for (ApnContext apnContext : this.mApnContexts.keySet()) {
            if (ApnSettingUtils.isMeteredApnType(apnContext.getApnTypeBitmask(), this.mPhone)) {
                return false;
            }
        }
        return true;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public NetworkCapabilities getNetworkCapabilities() {
        char c;
        NetworkCapabilities result = new NetworkCapabilities();
        result.addTransportType(0);
        ApnSetting apnSetting = this.mApnSetting;
        if (apnSetting != null) {
            String[] types = ApnSetting.getApnTypesStringFromBitmask(apnSetting.getApnTypeBitmask() & (~this.mDisabledApnTypeBitMask)).split(",");
            for (String type : types) {
                if (this.mRestrictedNetworkOverride || !this.mUnmeteredUseOnly || !ApnSettingUtils.isMeteredApnType(ApnSetting.getApnTypesBitmaskFromString(type), this.mPhone)) {
                    switch (type.hashCode()) {
                        case 42:
                            if (type.equals(CharacterSets.MIMENAME_ANY_CHARSET)) {
                                c = 0;
                                break;
                            }
                            c = 65535;
                            break;
                        case 3352:
                            if (type.equals("ia")) {
                                c = '\b';
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
                                c = '\n';
                                break;
                            }
                            c = 65535;
                            break;
                        case 108243:
                            if (type.equals("mms")) {
                                c = 2;
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
                        case 1544803905:
                            if (type.equals(TransportManager.IWLAN_OPERATION_MODE_DEFAULT)) {
                                c = 1;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1629013393:
                            if (type.equals("emergency")) {
                                c = '\t';
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
                            result.addCapability(12);
                            result.addCapability(0);
                            result.addCapability(1);
                            result.addCapability(3);
                            result.addCapability(4);
                            result.addCapability(5);
                            result.addCapability(7);
                            result.addCapability(2);
                            continue;
                        case 1:
                            result.addCapability(12);
                            continue;
                        case 2:
                            result.addCapability(0);
                            continue;
                        case 3:
                            result.addCapability(1);
                            continue;
                        case 4:
                            result.addCapability(2);
                            continue;
                        case 5:
                            result.addCapability(3);
                            continue;
                        case 6:
                            result.addCapability(4);
                            continue;
                        case 7:
                            result.addCapability(5);
                            continue;
                        case '\b':
                            result.addCapability(7);
                            continue;
                        case '\t':
                            result.addCapability(10);
                            continue;
                        case '\n':
                            result.addCapability(23);
                            continue;
                    }
                } else {
                    log("Dropped the metered " + type + " for the unmetered data call.");
                }
            }
            if ((!this.mUnmeteredUseOnly || this.mRestrictedNetworkOverride) && ApnSettingUtils.isMetered(this.mApnSetting, this.mPhone)) {
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
        int i = this.mRilRat;
        if (i != 19) {
            switch (i) {
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
                    up = TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_TRAT_SWAP_FAILED;
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
        result.setNetworkSpecifier(new StringNetworkSpecifier(Integer.toString(this.mSubId)));
        result.setCapability(18, !this.mPhone.getServiceState().getDataRoaming());
        result.addCapability(20);
        if ((1 & this.mSubscriptionOverride) != 0) {
            result.addCapability(11);
        }
        if ((2 & this.mSubscriptionOverride) != 0) {
            result.removeCapability(20);
        }
        return result;
    }

    @VisibleForTesting
    public boolean shouldSkip464Xlat() {
        int skip464Xlat = this.mApnSetting.getSkip464Xlat();
        if (skip464Xlat == 0) {
            return false;
        }
        if (skip464Xlat == 1) {
            return true;
        }
        NetworkCapabilities nc = getNetworkCapabilities();
        return nc.hasCapability(4) && !nc.hasCapability(12);
    }

    @VisibleForTesting
    public static boolean isIpAddress(String address) {
        if (address == null) {
            return false;
        }
        return InetAddress.isNumeric(address);
    }

    private SetupResult setLinkProperties(DataCallResponse response, LinkProperties linkProperties) {
        SetupResult result;
        String propertyPrefix = "net." + response.getInterfaceName() + ".";
        String[] dnsServers = {SystemProperties.get(propertyPrefix + "dns1"), SystemProperties.get(propertyPrefix + "dns2")};
        boolean okToUseSystemPropertyDns = isDnsOk(dnsServers);
        linkProperties.clear();
        if (response.getCause() == 0) {
            try {
                linkProperties.setInterfaceName(response.getInterfaceName());
                if (response.getAddresses().size() > 0) {
                    for (LinkAddress la : response.getAddresses()) {
                        if (!la.getAddress().isAnyLocalAddress()) {
                            log("addr/pl=" + la.getAddress() + "/" + la.getNetworkPrefixLength());
                            linkProperties.addLinkAddress(la);
                        }
                    }
                    if (response.getDnsAddresses().size() > 0) {
                        for (InetAddress dns : response.getDnsAddresses()) {
                            if (!dns.isAnyLocalAddress()) {
                                linkProperties.addDnsServer(dns);
                            }
                        }
                    } else if (okToUseSystemPropertyDns) {
                        for (String dnsAddr : dnsServers) {
                            String dnsAddr2 = dnsAddr.trim();
                            if (!dnsAddr2.isEmpty()) {
                                try {
                                    InetAddress ia = NetworkUtils.numericToInetAddress(dnsAddr2);
                                    if (!ia.isAnyLocalAddress()) {
                                        linkProperties.addDnsServer(ia);
                                    }
                                } catch (IllegalArgumentException e) {
                                    throw new UnknownHostException("Non-numeric dns addr=" + dnsAddr2);
                                }
                            }
                        }
                    } else {
                        throw new UnknownHostException("Empty dns response and no system default dns");
                    }
                    if (response.getPcscfAddresses().size() > 0) {
                        for (InetAddress pcscf : response.getPcscfAddresses()) {
                            linkProperties.addPcscfServer(pcscf);
                        }
                    }
                    for (InetAddress gateway : response.getGatewayAddresses()) {
                        linkProperties.addRoute(new RouteInfo(gateway));
                    }
                    linkProperties.setMtu(response.getMtu());
                    result = SetupResult.SUCCESS;
                } else {
                    throw new UnknownHostException("no address for ifname=" + response.getInterfaceName());
                }
            } catch (UnknownHostException e2) {
                log("setLinkProperties: UnknownHostException " + e2);
                result = SetupResult.ERROR_INVALID_ARG;
            }
        } else {
            result = SetupResult.ERROR_DATA_SERVICE_SPECIFIC_ERROR;
        }
        if (result != SetupResult.SUCCESS) {
            log("setLinkProperties: error clearing LinkProperties status=" + response.getCause() + " result=" + result);
            linkProperties.clear();
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean initConnection(ConnectionParams cp) {
        ApnContext apnContext = cp.mApnContext;
        if (this.mApnSetting == null) {
            this.mApnSetting = apnContext.getApnSetting();
        }
        ApnSetting apnSetting = this.mApnSetting;
        if (apnSetting == null || !apnSetting.canHandleType(apnContext.getApnTypeBitmask())) {
            log("initConnection: incompatible apnSetting in ConnectionParams cp=" + cp + " dc=" + this);
            return false;
        }
        this.mTag++;
        this.mConnectionParams = cp;
        this.mConnectionParams.mTag = this.mTag;
        mtkCheckDefaultApnRefCount(apnContext);
        this.mApnContexts.put(apnContext, cp);
        log("initConnection:  RefCount=" + this.mApnContexts.size() + " mApnList=" + this.mApnContexts + " mConnectionParams=" + this.mConnectionParams);
        return true;
    }

    protected class DcDefaultState extends State {
        protected DcDefaultState() {
        }

        public void enter() {
            DataConnection.this.log("DcDefaultState: enter");
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(DataConnection.this.mTransportType, DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED, null);
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRoamingOn(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_ROAM_ON, null);
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRoamingOff(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF, null, true);
            DataConnection.this.mDcController.addDc(DataConnection.this);
        }

        public void exit() {
            DataConnection.this.log("DcDefaultState: exit");
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(DataConnection.this.mTransportType, DataConnection.this.getHandler());
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(DataConnection.this.getHandler());
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(DataConnection.this.getHandler());
            DataConnection.this.mDcController.removeDc(DataConnection.this);
            if (DataConnection.this.mAc != null) {
                DataConnection.this.mAc.disconnected();
                DataConnection.this.mAc = null;
            }
            DataConnection.this.mApnContexts.clear();
            DataConnection dataConnection = DataConnection.this;
            dataConnection.mReconnectIntent = null;
            dataConnection.mDct = null;
            dataConnection.mApnSetting = null;
            dataConnection.mPhone = null;
            dataConnection.mDataServiceManager = null;
            dataConnection.mLinkProperties = null;
            dataConnection.mLastFailCause = 0;
            dataConnection.mUserData = null;
            dataConnection.mDcController = null;
            dataConnection.mDcTesterFailBringUpAll = null;
        }

        public boolean processMessage(Message msg) {
            DataConnection dataConnection = DataConnection.this;
            dataConnection.log("DcDefault msg=" + DataConnection.this.getWhatToString(msg.what) + " RefCount=" + DataConnection.this.mApnContexts.size());
            switch (msg.what) {
                case InboundSmsTracker.DEST_PORT_FLAG_3GPP2 /* 262144 */:
                    DataConnection.this.log("DcDefaultState: msg.what=EVENT_CONNECT, fail not expected");
                    DataConnection.this.notifyConnectCompleted((ConnectionParams) msg.obj, InboundSmsTracker.DEST_PORT_FLAG_NO_PORT, false);
                    break;
                case DataConnection.EVENT_DISCONNECT /* 262148 */:
                case DataConnection.EVENT_DISCONNECT_ALL /* 262150 */:
                case DataConnection.EVENT_REEVALUATE_RESTRICTED_STATE /* 262169 */:
                    DataConnection dataConnection2 = DataConnection.this;
                    dataConnection2.log("DcDefaultState deferring msg.what=" + DataConnection.this.getWhatToString(msg.what) + " RefCount=" + DataConnection.this.mApnContexts.size());
                    DataConnection.this.deferMessage(msg);
                    break;
                case DataConnection.EVENT_TEAR_DOWN_NOW /* 262152 */:
                    DataConnection.this.log("DcDefaultState EVENT_TEAR_DOWN_NOW");
                    DataConnection.this.mDataServiceManager.deactivateDataCall(DataConnection.this.mCid, 1, null);
                    break;
                case DataConnection.EVENT_LOST_CONNECTION /* 262153 */:
                    DataConnection.this.logAndAddLogRec("DcDefaultState ignore EVENT_LOST_CONNECTION tag=" + msg.arg1 + ":mTag=" + DataConnection.this.mTag);
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED /* 262155 */:
                    Pair<Integer, Integer> drsRatPair = (Pair) ((AsyncResult) msg.obj).result;
                    DataConnection.this.mDataRegState = ((Integer) drsRatPair.first).intValue();
                    if (DataConnection.this.mRilRat != ((Integer) drsRatPair.second).intValue()) {
                        DataConnection.this.updateTcpBufferSizes(((Integer) drsRatPair.second).intValue());
                    }
                    DataConnection.this.mRilRat = ((Integer) drsRatPair.second).intValue();
                    DataConnection dataConnection3 = DataConnection.this;
                    dataConnection3.log("DcDefaultState: EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED drs=" + DataConnection.this.mDataRegState + " mRilRat=" + DataConnection.this.mRilRat);
                    DataConnection.this.updateNetworkInfo();
                    DataConnection.this.updateNetworkInfoSuspendState();
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo, DataConnection.this);
                        DataConnection.this.mNetworkAgent.sendLinkProperties(DataConnection.this.mtkGetLinkProperties(), DataConnection.this);
                        break;
                    }
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_ON /* 262156 */:
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF /* 262157 */:
                case DataConnection.EVENT_DATA_CONNECTION_OVERRIDE_CHANGED /* 262161 */:
                    DataConnection.this.updateNetworkInfo();
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo, DataConnection.this);
                        break;
                    }
                    break;
                case DataConnection.EVENT_KEEPALIVE_START_REQUEST /* 262165 */:
                case DataConnection.EVENT_KEEPALIVE_STOP_REQUEST /* 262166 */:
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.onSocketKeepaliveEvent(msg.arg1, -20);
                        break;
                    }
                    break;
                case DataConnection.EVENT_RESET /* 262168 */:
                    DataConnection.this.log("DcDefaultState: msg.what=REQ_RESET");
                    DataConnection dataConnection4 = DataConnection.this;
                    dataConnection4.transitionTo(dataConnection4.mInactiveState);
                    break;
                default:
                    DataConnection dataConnection5 = DataConnection.this;
                    dataConnection5.log("DcDefaultState: shouldn't happen but ignore msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    break;
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void updateNetworkInfo() {
        ServiceState state = this.mPhone.getServiceState();
        int subtype = state.getDataNetworkType();
        this.mNetworkInfo.setSubtype(subtype, TelephonyManager.getNetworkTypeName(subtype));
        this.mNetworkInfo.setRoaming(state.getDataRoaming());
    }

    /* access modifiers changed from: protected */
    public void updateNetworkInfoSuspendState() {
        if (this.mNetworkAgent == null) {
            Rlog.e(getName(), "Setting suspend state without a NetworkAgent");
        }
        ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
        if (sst.getCurrentDataConnectionState() != 0) {
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
        } else if (sst.isConcurrentVoiceAndDataAllowed() || this.mPhone.getCallTracker().getState() == PhoneConstants.State.IDLE) {
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, this.mNetworkInfo.getExtraInfo());
        } else {
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
        }
    }

    /* access modifiers changed from: protected */
    public class DcInactiveState extends State {
        protected DcInactiveState() {
        }

        public void setEnterNotificationParams(ConnectionParams cp, int cause) {
            DataConnection.this.log("DcInactiveState: setEnterNotificationParams cp,cause");
            DataConnection dataConnection = DataConnection.this;
            dataConnection.mConnectionParams = cp;
            dataConnection.mDisconnectParams = null;
            dataConnection.mDcFailCause = cause;
        }

        public void setEnterNotificationParams(DisconnectParams dp) {
            DataConnection.this.log("DcInactiveState: setEnterNotificationParams dp");
            DataConnection dataConnection = DataConnection.this;
            dataConnection.mConnectionParams = null;
            dataConnection.mDisconnectParams = dp;
            dataConnection.mDcFailCause = 0;
        }

        public void setEnterNotificationParams(int cause) {
            DataConnection dataConnection = DataConnection.this;
            dataConnection.mConnectionParams = null;
            dataConnection.mDisconnectParams = null;
            dataConnection.mDcFailCause = cause;
        }

        public void enter() {
            DataConnection.this.mTag++;
            DataConnection.this.log("DcInactiveState: enter() mTag=" + DataConnection.this.mTag);
            StatsLog.write(75, 1, DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mId, DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.getApnTypeBitmask() : 0, DataConnection.this.mApnSetting != null ? DataConnection.this.mApnSetting.canHandleType(17) : false);
            if (DataConnection.this.mHandoverState == 2) {
                DataConnection.this.mHandoverState = 3;
            }
            if (DataConnection.this.mConnectionParams != null) {
                DataConnection.this.log("DcInactiveState: enter notifyConnectCompleted +ALL failCause=" + DataConnection.this.mDcFailCause);
                DataConnection dataConnection = DataConnection.this;
                dataConnection.notifyConnectCompleted(dataConnection.mConnectionParams, DataConnection.this.mDcFailCause, true);
            }
            if (DataConnection.this.mDisconnectParams != null) {
                DataConnection.this.log("DcInactiveState: enter notifyDisconnectCompleted +ALL failCause=" + DataConnection.this.mDcFailCause);
                DataConnection dataConnection2 = DataConnection.this;
                dataConnection2.notifyDisconnectCompleted(dataConnection2.mDisconnectParams, true);
            }
            if (DataConnection.this.mDisconnectParams == null && DataConnection.this.mConnectionParams == null && DataConnection.this.mDcFailCause != 0) {
                DataConnection.this.log("DcInactiveState: enter notifyAllDisconnectCompleted failCause=" + DataConnection.this.mDcFailCause);
                DataConnection dataConnection3 = DataConnection.this;
                dataConnection3.notifyAllWithEvent(null, 270351, DataFailCause.toString(dataConnection3.mDcFailCause));
            }
            DataConnection.this.mDcController.removeActiveDcByCid(DataConnection.this);
            DataConnection.this.clearSettings();
        }

        public void exit() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case InboundSmsTracker.DEST_PORT_FLAG_3GPP2 /* 262144 */:
                    DataConnection.this.log("DcInactiveState: mag.what=EVENT_CONNECT");
                    ConnectionParams cp = (ConnectionParams) msg.obj;
                    if (!DataConnection.this.initConnection(cp)) {
                        DataConnection.this.log("DcInactiveState: msg.what=EVENT_CONNECT initConnection failed");
                        DataConnection.this.notifyConnectCompleted(cp, 65538, false);
                        DataConnection dataConnection = DataConnection.this;
                        dataConnection.transitionTo(dataConnection.mInactiveState);
                        return true;
                    }
                    int cause = DataConnection.this.connect(cp);
                    if (cause != 0) {
                        DataConnection.this.log("DcInactiveState: msg.what=EVENT_CONNECT connect failed");
                        DataConnection.this.notifyConnectCompleted(cp, cause, false);
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.transitionTo(dataConnection2.mInactiveState);
                        return true;
                    }
                    if (DataConnection.this.mSubId == -1) {
                        DataConnection.this.mSubId = cp.mSubId;
                    }
                    DataConnection dataConnection3 = DataConnection.this;
                    dataConnection3.transitionTo(dataConnection3.mActivatingState);
                    return true;
                case DataConnection.EVENT_DISCONNECT /* 262148 */:
                    DataConnection.this.log("DcInactiveState: msg.what=EVENT_DISCONNECT");
                    DataConnection.this.notifyDisconnectCompleted((DisconnectParams) msg.obj, false);
                    return true;
                case DataConnection.EVENT_DISCONNECT_ALL /* 262150 */:
                    DataConnection.this.log("DcInactiveState: msg.what=EVENT_DISCONNECT_ALL");
                    DataConnection.this.notifyDisconnectCompleted((DisconnectParams) msg.obj, false);
                    return true;
                case DataConnection.EVENT_RESET /* 262168 */:
                case DataConnection.EVENT_REEVALUATE_RESTRICTED_STATE /* 262169 */:
                    DataConnection dataConnection4 = DataConnection.this;
                    dataConnection4.log("DcInactiveState: msg.what=" + DataConnection.this.getWhatToString(msg.what) + ", ignore we're already done");
                    return true;
                default:
                    DataConnection dataConnection5 = DataConnection.this;
                    dataConnection5.log("DcInactiveState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public class DcActivatingState extends State {
        protected DcActivatingState() {
        }

        public void enter() {
            StatsLog.write(75, 2, DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mId, DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.getApnTypeBitmask() : 0, DataConnection.this.mApnSetting != null ? DataConnection.this.mApnSetting.canHandleType(17) : false);
            DataConnection.this.setHandoverState(1);
        }

        public boolean processMessage(Message msg) {
            DataConnection.this.log("DcActivatingState: msg=" + DataConnection.msgToString(msg));
            int i = msg.what;
            if (i != 262144) {
                if (i == DataConnection.EVENT_SETUP_DATA_CONNECTION_DONE) {
                    ConnectionParams cp = (ConnectionParams) msg.obj;
                    DataCallResponse dataCallResponse = msg.getData().getParcelable(DataServiceManager.DATA_CALL_RESPONSE);
                    SetupResult result = DataConnection.this.onSetupConnectionCompleted(msg.arg1, dataCallResponse, cp);
                    if (!(result == SetupResult.ERROR_STALE || DataConnection.this.mConnectionParams == cp)) {
                        DataConnection.this.loge("DcActivatingState: WEIRD mConnectionsParams:" + DataConnection.this.mConnectionParams + " != cp:" + cp);
                    }
                    DataConnection.this.log("DcActivatingState onSetupConnectionCompleted result=" + result + " dc=" + DataConnection.this);
                    if (cp.mApnContext != null) {
                        cp.mApnContext.requestLog("onSetupConnectionCompleted result=" + result);
                    }
                    int i2 = AnonymousClass2.$SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[result.ordinal()];
                    if (i2 == 1) {
                        DataConnection dataConnection = DataConnection.this;
                        dataConnection.mDcFailCause = 0;
                        dataConnection.transitionTo(dataConnection.mActiveState);
                    } else if (i2 == 2) {
                        DataConnection.this.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.transitionTo(dataConnection2.mInactiveState);
                    } else if (i2 == 3) {
                        DataConnection.this.tearDownData(cp);
                        DataConnection dataConnection3 = DataConnection.this;
                        dataConnection3.transitionTo(dataConnection3.mDisconnectingErrorCreatingConnection);
                    } else if (i2 == 4) {
                        long delay = DataConnection.this.getSuggestedRetryDelay(dataCallResponse);
                        cp.mApnContext.setModemSuggestedDelay(delay);
                        String str = "DcActivatingState: ERROR_DATA_SERVICE_SPECIFIC_ERROR  delay=" + delay + " result=" + result + " result.isRadioRestartFailure=" + DataFailCause.isRadioRestartFailure(DataConnection.this.mPhone.getContext(), result.mFailCause, DataConnection.this.mPhone.getSubId()) + " isPermanentFailure=" + DataConnection.this.mDct.isPermanentFailure(result.mFailCause);
                        DataConnection.this.log(str);
                        if (cp.mApnContext != null) {
                            cp.mApnContext.requestLog(str);
                        }
                        DataConnection.this.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                        DataConnection dataConnection4 = DataConnection.this;
                        dataConnection4.transitionTo(dataConnection4.mInactiveState);
                    } else if (i2 != 5) {
                        DataConnection.this.loge("Unknown SetupResult, should not happen");
                    } else {
                        DataConnection.this.loge("DcActivatingState: stale EVENT_SETUP_DATA_CONNECTION_DONE tag:" + cp.mTag + " != mTag:" + DataConnection.this.mTag);
                    }
                    return true;
                } else if (i != DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED) {
                    DataConnection.this.log("DcActivatingState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what) + " RefCount=" + DataConnection.this.mApnContexts.size());
                    return false;
                }
            }
            DataConnection.this.deferMessage(msg);
            return true;
        }
    }

    /* renamed from: com.android.internal.telephony.dataconnection.DataConnection$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult = new int[SetupResult.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[SetupResult.SUCCESS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[SetupResult.ERROR_RADIO_NOT_AVAILABLE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[SetupResult.ERROR_INVALID_ARG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[SetupResult.ERROR_DATA_SERVICE_SPECIFIC_ERROR.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[SetupResult.ERROR_STALE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void printLostdata(String strinfo) {
        if (this.mPhone.getServiceState().getDataRegState() == 0) {
            String error_info = "[DcFail:" + strinfo + "]";
            int log_type = -1;
            String log_desc = PhoneConfigurationManager.SSSS;
            try {
                String[] log_array = OemTelephonyUtils.getOemRes(this.mPhone.getContext(), "zz_oppo_critical_log_113", PhoneConfigurationManager.SSSS).split(",");
                log_type = Integer.valueOf(log_array[0]).intValue();
                log_desc = log_array[1];
            } catch (Exception e) {
                log("printLostdata: " + e);
            }
            OppoManager.writeLogToPartition(log_type, error_info, "NETWORK", "data_stall_error", log_desc);
            HashMap<String, String> mLostDataMap = new HashMap<>();
            mLostDataMap.put(String.valueOf("data_stall_error"), "DcFail");
            OppoManager.onStamp(DATA_EVENT_ID, mLostDataMap);
        }
    }

    /* access modifiers changed from: protected */
    public class DcActiveState extends State {
        protected DcActiveState() {
        }

        public void enter() {
            DataConnection dataConnection = DataConnection.this;
            dataConnection.log("DcActiveState: enter dc=" + DataConnection.this);
            StatsLog.write(75, 3, DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mId, DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.getApnTypeBitmask() : 0, DataConnection.this.mApnSetting != null ? DataConnection.this.mApnSetting.canHandleType(17) : false);
            DataConnection.this.updateNetworkInfo();
            DataConnection.this.notifyAllWithEvent(null, 270336, PhoneInternalInterface.REASON_CONNECTED);
            DataConnection.this.mPhone.getCallTracker().registerForVoiceCallStarted(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_STARTED, null);
            DataConnection.this.mPhone.getCallTracker().registerForVoiceCallEnded(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_ENDED, null);
            DataConnection.this.mDcController.addActiveDcByCid(DataConnection.this);
            DataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, DataConnection.this.mNetworkInfo.getReason(), null);
            DataConnection.this.mNetworkInfo.setExtraInfo(DataConnection.this.mApnSetting.getApnName());
            DataConnection dataConnection2 = DataConnection.this;
            dataConnection2.updateTcpBufferSizes(dataConnection2.mRilRat);
            NetworkMisc misc = new NetworkMisc();
            if (DataConnection.this.mPhone.getCarrierSignalAgent().hasRegisteredReceivers("com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED")) {
                misc.provisioningNotificationDisabled = true;
            }
            misc.subscriberId = DataConnection.this.mPhone.getSubscriberId();
            misc.skip464xlat = DataConnection.this.shouldSkip464Xlat();
            DataConnection dataConnection3 = DataConnection.this;
            dataConnection3.mRestrictedNetworkOverride = dataConnection3.shouldRestrictNetwork();
            DataConnection dataConnection4 = DataConnection.this;
            dataConnection4.mUnmeteredUseOnly = dataConnection4.isUnmeteredUseOnly();
            DataConnection dataConnection5 = DataConnection.this;
            dataConnection5.log("mRestrictedNetworkOverride = " + DataConnection.this.mRestrictedNetworkOverride + ", mUnmeteredUseOnly = " + DataConnection.this.mUnmeteredUseOnly);
            if (DataConnection.this.mConnectionParams == null || DataConnection.this.mConnectionParams.mRequestType != 2) {
                DataConnection dataConnection6 = DataConnection.this;
                dataConnection6.mScore = dataConnection6.calculateScore();
                NetworkFactory factory = PhoneFactory.getNetworkFactory(DataConnection.this.mPhone.getPhoneId());
                int factorySerialNumber = factory == null ? -1 : factory.getSerialNumber();
                DataConnection dataConnection7 = DataConnection.this;
                dataConnection7.mNetworkAgent = DcNetworkAgent.createDcNetworkAgent(dataConnection7, dataConnection7.mPhone, DataConnection.this.mNetworkInfo, DataConnection.this.mScore, misc, factorySerialNumber, DataConnection.this.mTransportType);
            } else {
                DataConnection dc = DataConnection.this.mPhone.getDcTracker(DataConnection.this.getHandoverSourceTransport()).getDataConnectionByApnType(DataConnection.this.mConnectionParams.mApnContext.getApnType());
                if (dc != null) {
                    dc.setHandoverState(3);
                }
                if (DataConnection.this.mHandoverSourceNetworkAgent != null) {
                    DataConnection.this.log("Transfer network agent successfully.");
                    DataConnection dataConnection8 = DataConnection.this;
                    dataConnection8.mNetworkAgent = dataConnection8.mHandoverSourceNetworkAgent;
                    DcNetworkAgent dcNetworkAgent = DataConnection.this.mNetworkAgent;
                    DataConnection dataConnection9 = DataConnection.this;
                    dcNetworkAgent.acquireOwnership(dataConnection9, dataConnection9.mTransportType);
                    DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                    DataConnection.this.mNetworkAgent.sendLinkProperties(DataConnection.this.mLinkProperties, DataConnection.this);
                    DataConnection.this.mHandoverSourceNetworkAgent = null;
                } else {
                    DataConnection.this.loge("Failed to get network agent from original data connection");
                    return;
                }
            }
            if (DataConnection.this.mTransportType == 1) {
                DataConnection.this.mPhone.mCi.registerForNattKeepaliveStatus(DataConnection.this.getHandler(), DataConnection.EVENT_KEEPALIVE_STATUS, null);
                DataConnection.this.mPhone.mCi.registerForLceInfo(DataConnection.this.getHandler(), DataConnection.EVENT_LINK_CAPACITY_CHANGED, null);
            }
            TelephonyMetrics.getInstance().writeRilDataCallEvent(DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mCid, DataConnection.this.mApnSetting.getApnTypeBitmask(), 1);
        }

        public void exit() {
            String reason;
            DataConnection dataConnection = DataConnection.this;
            dataConnection.log("DcActiveState: exit dc=" + this);
            DataConnection.this.mNetworkInfo.getReason();
            if (DataConnection.this.mDcController.isExecutingCarrierChange()) {
                reason = PhoneInternalInterface.REASON_CARRIER_CHANGE;
            } else if (DataConnection.this.mDisconnectParams == null || DataConnection.this.mDisconnectParams.mReason == null) {
                reason = DataFailCause.toString(DataConnection.this.mDcFailCause);
            } else {
                reason = DataConnection.this.mDisconnectParams.mReason;
            }
            DataConnection.this.mPhone.getCallTracker().unregisterForVoiceCallStarted(DataConnection.this.getHandler());
            DataConnection.this.mPhone.getCallTracker().unregisterForVoiceCallEnded(DataConnection.this.getHandler());
            if (DataConnection.this.mHandoverState != 2) {
                DataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, reason, DataConnection.this.mNetworkInfo.getExtraInfo());
            }
            if (DataConnection.this.mTransportType == 1) {
                DataConnection.this.mPhone.mCi.unregisterForNattKeepaliveStatus(DataConnection.this.getHandler());
                DataConnection.this.mPhone.mCi.unregisterForLceInfo(DataConnection.this.getHandler());
            }
            if (DataConnection.this.mNetworkAgent != null) {
                DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo, DataConnection.this);
                DataConnection.this.mNetworkAgent.releaseOwnership(DataConnection.this);
            }
            DataConnection.this.mNetworkAgent = null;
            TelephonyMetrics.getInstance().writeRilDataCallEvent(DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mCid, DataConnection.this.mApnSetting.getApnTypeBitmask(), 2);
        }

        /* JADX INFO: Multiple debug info for r0v48 int: [D('slotId' int), D('ar' android.os.AsyncResult)] */
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case InboundSmsTracker.DEST_PORT_FLAG_3GPP2 /* 262144 */:
                    ConnectionParams cp = (ConnectionParams) msg.obj;
                    DataConnection.this.mApnContexts.put(cp.mApnContext, cp);
                    DataConnection.this.mDisabledApnTypeBitMask &= ~cp.mApnContext.getApnTypeBitmask();
                    DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                    DataConnection.this.log("DcActiveState: EVENT_CONNECT cp=" + cp + " dc=" + DataConnection.this);
                    DataConnection.this.notifyConnectCompleted(cp, 0, false);
                    return true;
                case DataConnection.EVENT_SETUP_DATA_CONNECTION_DONE /* 262145 */:
                case 262146:
                case DataConnection.EVENT_DEACTIVATE_DONE /* 262147 */:
                case DataConnection.EVENT_RIL_CONNECTED /* 262149 */:
                case DataConnection.EVENT_DATA_STATE_CHANGED /* 262151 */:
                case DataConnection.EVENT_TEAR_DOWN_NOW /* 262152 */:
                case 262154:
                case DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED /* 262155 */:
                case DataConnection.EVENT_RESET /* 262168 */:
                default:
                    DataConnection.this.log("DcActiveState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    return false;
                case DataConnection.EVENT_DISCONNECT /* 262148 */:
                    DisconnectParams dp = (DisconnectParams) msg.obj;
                    DataConnection.this.log("DcActiveState: EVENT_DISCONNECT dp=" + dp + " dc=" + DataConnection.this);
                    if (DataConnection.this.mApnContexts.containsKey(dp.mApnContext)) {
                        DataConnection.this.log("DcActiveState msg.what=EVENT_DISCONNECT RefCount=" + DataConnection.this.mApnContexts.size());
                        if (DataConnection.this.mApnContexts.size() == 1) {
                            DataConnection.this.mApnContexts.clear();
                            DataConnection dataConnection = DataConnection.this;
                            dataConnection.mDisconnectParams = dp;
                            dataConnection.mConnectionParams = null;
                            dp.mTag = dataConnection.mTag;
                            DataConnection.this.tearDownData(dp);
                            DataConnection dataConnection2 = DataConnection.this;
                            dataConnection2.transitionTo(dataConnection2.mDisconnectingState);
                        } else {
                            DataConnection.this.mApnContexts.remove(dp.mApnContext);
                            DataConnection.this.mDisabledApnTypeBitMask |= dp.mApnContext.getApnTypeBitmask();
                            DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                            DataConnection.this.notifyDisconnectCompleted(dp, false);
                        }
                    } else {
                        DataConnection.this.log("DcActiveState ERROR no such apnContext=" + dp.mApnContext + " in this dc=" + DataConnection.this);
                        DataConnection.this.notifyDisconnectCompleted(dp, false);
                    }
                    return true;
                case DataConnection.EVENT_DISCONNECT_ALL /* 262150 */:
                    DataConnection.this.log("DcActiveState EVENT_DISCONNECT clearing apn contexts, dc=" + DataConnection.this);
                    DisconnectParams dp2 = (DisconnectParams) msg.obj;
                    DataConnection dataConnection3 = DataConnection.this;
                    dataConnection3.mDisconnectParams = dp2;
                    dataConnection3.mConnectionParams = null;
                    dp2.mTag = dataConnection3.mTag;
                    DataConnection.this.tearDownData(dp2);
                    DataConnection dataConnection4 = DataConnection.this;
                    dataConnection4.transitionTo(dataConnection4.mDisconnectingState);
                    return true;
                case DataConnection.EVENT_LOST_CONNECTION /* 262153 */:
                    DataConnection.this.log("DcActiveState EVENT_LOST_CONNECTION dc=" + DataConnection.this);
                    DataConnection.this.mInactiveState.setEnterNotificationParams(65540);
                    DataConnection dataConnection5 = DataConnection.this;
                    dataConnection5.transitionTo(dataConnection5.mInactiveState);
                    try {
                        if (DataConnection.this.mPhone.getSubId() != SubscriptionManager.getDefaultDataSubscriptionId() || DataConnection.this.mApnContexts == null) {
                            return true;
                        }
                        for (ConnectionParams cp2 : DataConnection.this.mApnContexts.values()) {
                            ApnContext apnContext = cp2.mApnContext;
                            if (apnContext != null && TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(apnContext.getApnType())) {
                                ApnSetting apn = apnContext.getApnSetting();
                                String apninfo = apn == null ? "unknown" : apn.getApnName();
                                DataConnection.this.printLostdata(apninfo);
                                DataConnection.this.log("printLostdata EVENT_LOST_CONNECTION dc= " + apninfo);
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        DataConnection.this.log("EVENT_LOST_CONNECTION+ " + e.toString());
                        return true;
                    }
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_ON /* 262156 */:
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF /* 262157 */:
                case DataConnection.EVENT_DATA_CONNECTION_OVERRIDE_CHANGED /* 262161 */:
                    DataConnection.this.updateNetworkInfo();
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo, DataConnection.this);
                    }
                    return true;
                case DataConnection.EVENT_BW_REFRESH_RESPONSE /* 262158 */:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        DataConnection.this.log("EVENT_BW_REFRESH_RESPONSE: error ignoring, e=" + ar.exception);
                    } else {
                        LinkCapacityEstimate lce = (LinkCapacityEstimate) ar.result;
                        NetworkCapabilities nc = DataConnection.this.getNetworkCapabilities();
                        if (DataConnection.this.mPhone.getLceStatus() == 1 && lce.downlinkCapacityKbps > 0) {
                            nc.setLinkDownstreamBandwidthKbps(lce.downlinkCapacityKbps);
                            if (DataConnection.this.mNetworkAgent != null) {
                                DataConnection.this.mNetworkAgent.sendNetworkCapabilities(nc, DataConnection.this);
                            }
                        }
                    }
                    return true;
                case DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_STARTED /* 262159 */:
                case DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_ENDED /* 262160 */:
                    DataConnection.this.updateNetworkInfo();
                    DataConnection.this.updateNetworkInfoSuspendState();
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo, DataConnection.this);
                    }
                    return true;
                case DataConnection.EVENT_KEEPALIVE_STATUS /* 262162 */:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.exception != null) {
                        DataConnection.this.loge("EVENT_KEEPALIVE_STATUS: error in keepalive, e=" + ar2.exception);
                    }
                    if (ar2.result != null) {
                        DataConnection.this.mNetworkAgent.keepaliveTracker.handleKeepaliveStatus((KeepaliveStatus) ar2.result);
                    }
                    return true;
                case DataConnection.EVENT_KEEPALIVE_STARTED /* 262163 */:
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    int slot = msg.arg1;
                    if (ar3.exception != null || ar3.result == null) {
                        DataConnection.this.loge("EVENT_KEEPALIVE_STARTED: error starting keepalive, e=" + ar3.exception);
                        DataConnection.this.mNetworkAgent.onSocketKeepaliveEvent(slot, -31);
                    } else {
                        KeepaliveStatus ks = (KeepaliveStatus) ar3.result;
                        if (ks == null) {
                            DataConnection.this.loge("Null KeepaliveStatus received!");
                        } else {
                            DataConnection.this.mNetworkAgent.keepaliveTracker.handleKeepaliveStarted(slot, ks);
                        }
                    }
                    return true;
                case DataConnection.EVENT_KEEPALIVE_STOPPED /* 262164 */:
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    int handle = msg.arg1;
                    int i = msg.arg2;
                    if (ar4.exception != null) {
                        DataConnection.this.loge("EVENT_KEEPALIVE_STOPPED: error stopping keepalive for handle=" + handle + " e=" + ar4.exception);
                        DataConnection.this.mNetworkAgent.keepaliveTracker.handleKeepaliveStatus(new KeepaliveStatus(3));
                    } else {
                        DataConnection.this.log("Keepalive Stop Requested for handle=" + handle);
                        DataConnection.this.mNetworkAgent.keepaliveTracker.handleKeepaliveStatus(new KeepaliveStatus(handle, 1));
                    }
                    return true;
                case DataConnection.EVENT_KEEPALIVE_START_REQUEST /* 262165 */:
                    KeepalivePacketData pkt = (KeepalivePacketData) msg.obj;
                    int slotId = msg.arg1;
                    int intervalMillis = msg.arg2 * 1000;
                    if (DataConnection.this.mTransportType == 1) {
                        DataConnection.this.mPhone.mCi.startNattKeepalive(DataConnection.this.mCid, pkt, intervalMillis, DataConnection.this.obtainMessage(DataConnection.EVENT_KEEPALIVE_STARTED, slotId, 0, null));
                    } else if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.onSocketKeepaliveEvent(msg.arg1, -20);
                    }
                    return true;
                case DataConnection.EVENT_KEEPALIVE_STOP_REQUEST /* 262166 */:
                    int slotId2 = msg.arg1;
                    int handle2 = DataConnection.this.mNetworkAgent.keepaliveTracker.getHandleForSlot(slotId2);
                    if (handle2 < 0) {
                        DataConnection.this.loge("No slot found for stopSocketKeepalive! " + slotId2);
                        return true;
                    }
                    DataConnection.this.logd("Stopping keepalive with handle: " + handle2);
                    DataConnection.this.mPhone.mCi.stopNattKeepalive(handle2, DataConnection.this.obtainMessage(DataConnection.EVENT_KEEPALIVE_STOPPED, handle2, slotId2, null));
                    return true;
                case DataConnection.EVENT_LINK_CAPACITY_CHANGED /* 262167 */:
                    AsyncResult ar5 = (AsyncResult) msg.obj;
                    if (ar5.exception != null) {
                        DataConnection.this.loge("EVENT_LINK_CAPACITY_CHANGED e=" + ar5.exception);
                    } else {
                        LinkCapacityEstimate lce2 = (LinkCapacityEstimate) ar5.result;
                        NetworkCapabilities nc2 = DataConnection.this.getNetworkCapabilities();
                        if (lce2.downlinkCapacityKbps != -1) {
                            nc2.setLinkDownstreamBandwidthKbps(lce2.downlinkCapacityKbps);
                        }
                        if (lce2.uplinkCapacityKbps != -1) {
                            nc2.setLinkUpstreamBandwidthKbps(lce2.uplinkCapacityKbps);
                        }
                        if (DataConnection.this.mNetworkAgent != null) {
                            DataConnection.this.mNetworkAgent.sendNetworkCapabilities(nc2, DataConnection.this);
                        }
                    }
                    return true;
                case DataConnection.EVENT_REEVALUATE_RESTRICTED_STATE /* 262169 */:
                    if (DataConnection.this.mRestrictedNetworkOverride && !DataConnection.this.shouldRestrictNetwork()) {
                        DataConnection.this.log("Data connection becomes not-restricted. dc=" + this);
                        DataConnection dataConnection6 = DataConnection.this;
                        dataConnection6.mRestrictedNetworkOverride = false;
                        dataConnection6.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                    }
                    if (DataConnection.this.mUnmeteredUseOnly && !DataConnection.this.isUnmeteredUseOnly()) {
                        DataConnection dataConnection7 = DataConnection.this;
                        dataConnection7.mUnmeteredUseOnly = false;
                        dataConnection7.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                    }
                    return true;
                case DataConnection.EVENT_REEVALUATE_DATA_CONNECTION_PROPERTIES /* 262170 */:
                    DataConnection.this.updateScore();
                    return true;
            }
        }
    }

    /* access modifiers changed from: protected */
    public class DcDisconnectingState extends State {
        protected DcDisconnectingState() {
        }

        public void enter() {
            StatsLog.write(75, 4, DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mId, DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.getApnTypeBitmask() : 0, DataConnection.this.mApnSetting != null ? DataConnection.this.mApnSetting.canHandleType(17) : false);
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 262144) {
                DataConnection.this.log("DcDisconnectingState msg.what=EVENT_CONNECT. Defer. RefCount = " + DataConnection.this.mApnContexts.size());
                DataConnection.this.deferMessage(msg);
                return true;
            } else if (i != DataConnection.EVENT_DEACTIVATE_DONE) {
                DataConnection.this.log("DcDisconnectingState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                return false;
            } else {
                DisconnectParams dp = (DisconnectParams) msg.obj;
                String str = "DcDisconnectingState msg.what=EVENT_DEACTIVATE_DONE RefCount=" + DataConnection.this.mApnContexts.size();
                DataConnection.this.log(str);
                if (dp.mApnContext != null) {
                    dp.mApnContext.requestLog(str);
                }
                if (dp.mTag == DataConnection.this.mTag) {
                    DataConnection.this.mInactiveState.setEnterNotificationParams(dp);
                    DataConnection dataConnection = DataConnection.this;
                    dataConnection.transitionTo(dataConnection.mInactiveState);
                } else {
                    DataConnection.this.log("DcDisconnectState stale EVENT_DEACTIVATE_DONE dp.tag=" + dp.mTag + " mTag=" + DataConnection.this.mTag);
                }
                return true;
            }
        }
    }

    protected class DcDisconnectionErrorCreatingConnection extends State {
        protected DcDisconnectionErrorCreatingConnection() {
        }

        public void enter() {
            StatsLog.write(75, 5, DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mId, DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.getApnTypeBitmask() : 0, DataConnection.this.mApnSetting != null ? DataConnection.this.mApnSetting.canHandleType(17) : false);
        }

        public boolean processMessage(Message msg) {
            if (msg.what != DataConnection.EVENT_DEACTIVATE_DONE) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.log("DcDisconnectionErrorCreatingConnection not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                return false;
            }
            ConnectionParams cp = (ConnectionParams) msg.obj;
            if (cp.mTag == DataConnection.this.mTag) {
                DataConnection.this.log("DcDisconnectionErrorCreatingConnection msg.what=EVENT_DEACTIVATE_DONE");
                if (cp.mApnContext != null) {
                    cp.mApnContext.requestLog("DcDisconnectionErrorCreatingConnection msg.what=EVENT_DEACTIVATE_DONE");
                }
                DataConnection.this.mInactiveState.setEnterNotificationParams(cp, 65538);
                DataConnection dataConnection2 = DataConnection.this;
                dataConnection2.transitionTo(dataConnection2.mInactiveState);
            } else {
                DataConnection dataConnection3 = DataConnection.this;
                dataConnection3.log("DcDisconnectionErrorCreatingConnection stale EVENT_DEACTIVATE_DONE dp.tag=" + cp.mTag + ", mTag=" + DataConnection.this.mTag);
            }
            return true;
        }
    }

    public void bringUp(ApnContext apnContext, int profileId, int rilRadioTechnology, Message onCompletedMsg, int connectionGeneration, int requestType, int subId) {
        log("bringUp: apnContext=" + apnContext + " onCompletedMsg=" + onCompletedMsg);
        sendMessage(InboundSmsTracker.DEST_PORT_FLAG_3GPP2, new ConnectionParams(apnContext, profileId, rilRadioTechnology, onCompletedMsg, connectionGeneration, requestType, subId));
    }

    public void tearDown(ApnContext apnContext, String reason, Message onCompletedMsg) {
        log("tearDown: apnContext=" + apnContext + " reason=" + reason + " onCompletedMsg=" + onCompletedMsg);
        sendMessage(EVENT_DISCONNECT, new DisconnectParams(apnContext, reason, 2, onCompletedMsg));
    }

    /* access modifiers changed from: package-private */
    public void tearDownNow() {
        log("tearDownNow()");
        sendMessage(obtainMessage(EVENT_TEAR_DOWN_NOW));
    }

    public void tearDownAll(String reason, int releaseType, Message onCompletedMsg) {
        log("tearDownAll: reason=" + reason + ", releaseType=" + releaseType);
        sendMessage(EVENT_DISCONNECT_ALL, new DisconnectParams(null, reason, releaseType, onCompletedMsg));
    }

    public void reset() {
        sendMessage(EVENT_RESET);
        log("reset");
    }

    /* access modifiers changed from: package-private */
    public void reevaluateRestrictedState() {
        sendMessage(EVENT_REEVALUATE_RESTRICTED_STATE);
        log("reevaluate restricted state");
    }

    /* access modifiers changed from: package-private */
    public void reevaluateDataConnectionProperties() {
        sendMessage(EVENT_REEVALUATE_DATA_CONNECTION_PROPERTIES);
        log("reevaluate data connection properties");
    }

    public ConnectionParams getConnectionParams() {
        return this.mConnectionParams;
    }

    public String[] getPcscfAddresses() {
        return this.mPcscfAddr;
    }

    /* access modifiers changed from: protected */
    public long getSuggestedRetryDelay(DataCallResponse response) {
        if (response.getSuggestedRetryTime() < 0) {
            log("No suggested retry delay.");
            return -2;
        } else if (response.getSuggestedRetryTime() != Integer.MAX_VALUE) {
            return (long) response.getSuggestedRetryTime();
        } else {
            log("Modem suggested not retrying.");
            return -1;
        }
    }

    public List<ApnContext> getApnContexts() {
        return new ArrayList(this.mApnContexts.keySet());
    }

    public DcNetworkAgent getNetworkAgent() {
        return this.mNetworkAgent;
    }

    public void setHandoverState(int state) {
        this.mHandoverState = state;
    }

    /* access modifiers changed from: protected */
    public String getWhatToString(int what) {
        return cmdToString(what);
    }

    /* JADX INFO: Multiple debug info for r0v1 java.lang.String: [D('b' java.lang.StringBuilder), D('retVal' java.lang.String)] */
    protected static String msgToString(Message msg) {
        if (msg == null) {
            return "null";
        }
        StringBuilder b = new StringBuilder();
        b.append("{what=");
        b.append(cmdToString(msg.what));
        b.append(" when=");
        TimeUtils.formatDuration(msg.getWhen() - SystemClock.uptimeMillis(), b);
        if (msg.arg1 != 0) {
            b.append(" arg1=");
            b.append(msg.arg1);
        }
        if (msg.arg2 != 0) {
            b.append(" arg2=");
            b.append(msg.arg2);
        }
        if (msg.obj != null) {
            b.append(" obj=");
            b.append(msg.obj);
        }
        b.append(" target=");
        b.append(msg.getTarget());
        b.append(" replyTo=");
        b.append(msg.replyTo);
        b.append("}");
        return b.toString();
    }

    static void slog(String s) {
        Rlog.d("DC", s);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        Rlog.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void logv(String s) {
        Rlog.v(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void logi(String s) {
        Rlog.i(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void logw(String s) {
        Rlog.w(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s, Throwable e) {
        Rlog.e(getName(), s, e);
    }

    public String toStringSimple() {
        try {
            return getName() + ": State=" + getCurrentState().getName() + " mApnSetting=" + this.mApnSetting + " RefCount=" + this.mApnContexts.size() + " mCid=" + this.mCid + " mCreateTime=" + this.mCreateTime + " mLastastFailTime=" + this.mLastFailTime + " mLastFailCause=" + this.mLastFailCause + " mTag=" + this.mTag + " mLinkProperties=" + this.mLinkProperties + " linkCapabilities=" + getNetworkCapabilities() + " mRestrictedNetworkOverride=" + this.mRestrictedNetworkOverride;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "--toStringSimple--error--";
        }
    }

    public String toString() {
        return "{" + toStringSimple() + " mApnContexts=" + this.mApnContexts + "}";
    }

    private boolean isNRConnected() {
        return this.mPhone.getServiceState().getNrState() == 3;
    }

    private void dumpToLog() {
        dump(null, new PrintWriter(new StringWriter(0)) {
            /* class com.android.internal.telephony.dataconnection.DataConnection.AnonymousClass1 */

            @Override // java.io.PrintWriter
            public void println(String s) {
                DataConnection.this.logd(s);
            }

            @Override // java.io.Writer, java.io.Flushable
            public void flush() {
            }
        }, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateScore() {
        int oldScore = this.mScore;
        this.mScore = calculateScore();
        if (oldScore != this.mScore && this.mNetworkAgent != null) {
            log("Updating score from " + oldScore + " to " + this.mScore);
            this.mNetworkAgent.sendNetworkScore(this.mScore, this);
        }
    }

    /* access modifiers changed from: protected */
    public int calculateScore() {
        int score = 9;
        for (ApnContext apnContext : this.mApnContexts.keySet()) {
            Iterator<NetworkRequest> it = apnContext.getNetworkRequests().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                NetworkRequest networkRequest = it.next();
                if (networkRequest.hasCapability(12) && networkRequest.networkCapabilities.getNetworkSpecifier() == null) {
                    score = 50;
                    if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext())) {
                        score = 10;
                    }
                }
            }
        }
        return score;
    }

    public void dump(FileDescriptor fd, PrintWriter printWriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printWriter, " ");
        pw.print("DataConnection ");
        DataConnection.super.dump(fd, pw, args);
        pw.flush();
        pw.increaseIndent();
        pw.println("transport type=" + AccessNetworkConstants.transportTypeToString(this.mTransportType));
        pw.println("mApnContexts.size=" + this.mApnContexts.size());
        pw.println("mApnContexts=" + this.mApnContexts);
        pw.println("mApnSetting=" + this.mApnSetting);
        pw.println("mTag=" + this.mTag);
        pw.println("mCid=" + this.mCid);
        pw.println("mConnectionParams=" + this.mConnectionParams);
        pw.println("mDisconnectParams=" + this.mDisconnectParams);
        pw.println("mDcFailCause=" + this.mDcFailCause);
        pw.println("mPhone=" + this.mPhone);
        pw.println("mSubId=" + this.mSubId);
        pw.println("mLinkProperties=" + this.mLinkProperties);
        pw.flush();
        pw.println("mDataRegState=" + this.mDataRegState);
        pw.println("mHandoverState=" + this.mHandoverState);
        pw.println("mRilRat=" + this.mRilRat);
        pw.println("mNetworkCapabilities=" + getNetworkCapabilities());
        pw.println("mCreateTime=" + TimeUtils.logTimeOfDay(this.mCreateTime));
        pw.println("mLastFailTime=" + TimeUtils.logTimeOfDay(this.mLastFailTime));
        pw.println("mLastFailCause=" + this.mLastFailCause);
        pw.println("mUserData=" + this.mUserData);
        pw.println("mSubscriptionOverride=" + Integer.toHexString(this.mSubscriptionOverride));
        pw.println("mRestrictedNetworkOverride=" + this.mRestrictedNetworkOverride);
        pw.println("mUnmeteredUseOnly=" + this.mUnmeteredUseOnly);
        pw.println("mInstanceNumber=" + mInstanceNumber);
        pw.println("mAc=" + this.mAc);
        pw.println("mScore=" + this.mScore);
        DcNetworkAgent dcNetworkAgent = this.mNetworkAgent;
        if (dcNetworkAgent != null) {
            dcNetworkAgent.dump(fd, pw, args);
        }
        pw.decreaseIndent();
        pw.println();
        pw.flush();
    }

    /* access modifiers changed from: protected */
    public void mtkReplaceStates() {
    }

    /* access modifiers changed from: protected */
    public LinkProperties mtkGetLinkProperties() {
        return this.mLinkProperties;
    }

    /* access modifiers changed from: protected */
    public void mtkSetApnContextReason(ApnContext alreadySent, String reason) {
    }

    /* access modifiers changed from: protected */
    public void mtkCheckDefaultApnRefCount(ApnContext apnContext) {
    }
}

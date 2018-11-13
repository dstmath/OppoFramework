package com.android.internal.telephony.dataconnection;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.radio.V1_0.RadioAccessFamily;
import android.hardware.radio.V1_0.RadioError;
import android.net.LinkProperties;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkMisc;
import android.net.ProxyInfo;
import android.net.StringNetworkSpecifier;
import android.os.AsyncResult;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Pair;
import android.util.Patterns;
import android.util.TimeUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.dataconnection.DataCallResponse.SetupResult;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.google.android.mms.pdu.CharacterSets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class DataConnection extends StateMachine {
    private static final String ACTION_DDS_SWITCH_DONE = "org.codeaurora.intent.action.ACTION_DDS_SWITCH_DONE";
    static final int BASE = 262144;
    private static final int CMD_TO_STRING_COUNT = 18;
    private static final boolean DBG = true;
    static final int EVENT_BW_REFRESH_RESPONSE = 262158;
    static final int EVENT_CONNECT = 262144;
    static final int EVENT_DATA_CONNECTION_DDS_SWITCHED = 262161;
    static final int EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED = 262155;
    static final int EVENT_DATA_CONNECTION_ROAM_OFF = 262157;
    static final int EVENT_DATA_CONNECTION_ROAM_ON = 262156;
    static final int EVENT_DATA_CONNECTION_VOICE_CALL_ENDED = 262160;
    static final int EVENT_DATA_CONNECTION_VOICE_CALL_STARTED = 262159;
    static final int EVENT_DATA_STATE_CHANGED = 262151;
    static final int EVENT_DEACTIVATE_DONE = 262147;
    static final int EVENT_DISCONNECT = 262148;
    static final int EVENT_DISCONNECT_ALL = 262150;
    static final int EVENT_GET_LAST_FAIL_DONE = 262146;
    static final int EVENT_LOST_CONNECTION = 262153;
    static final int EVENT_RIL_CONNECTED = 262149;
    static final int EVENT_SETUP_DATA_CONNECTION_DONE = 262145;
    static final int EVENT_TEAR_DOWN_NOW = 262152;
    private static final String NETWORK_TYPE = "MOBILE";
    private static final String NULL_IP = "0.0.0.0";
    private static final String TCP_BUFFER_SIZES_1XRTT = "16384,32768,131072,4096,16384,102400";
    private static final String TCP_BUFFER_SIZES_EDGE = "4093,26280,70800,4096,16384,70800";
    private static final String TCP_BUFFER_SIZES_EHRPD = "131072,262144,1048576,4096,16384,524288";
    private static final String TCP_BUFFER_SIZES_EVDO = "4094,87380,262144,4096,16384,262144";
    private static final String TCP_BUFFER_SIZES_GPRS = "4092,8760,48000,4096,8760,48000";
    private static final String TCP_BUFFER_SIZES_HSDPA = "61167,367002,1101005,8738,52429,262114";
    private static final String TCP_BUFFER_SIZES_HSPA = "40778,244668,734003,16777,100663,301990";
    private static final String TCP_BUFFER_SIZES_HSPAP = "122334,734003,2202010,32040,192239,576717";
    private static final String TCP_BUFFER_SIZES_LTE = "524288,1048576,2097152,262144,524288,1048576";
    private static final String TCP_BUFFER_SIZES_LTE_CA = "2097152,4194304,8388608,4096,1048576,2097152";
    private static final String TCP_BUFFER_SIZES_UMTS = "58254,349525,1048576,58254,349525,1048576";
    private static final boolean VDBG = true;
    private static AtomicInteger mInstanceNumber = new AtomicInteger(0);
    private static final String[] sAuTelstraOperator = new String[]{"50501", "50571", "50572", "50511"};
    private static String[] sCmdToString = new String[18];
    private static final String[] sKDDIOperator = new String[]{"44050", "44051", "44052", "44053", "44054", "44070", "44071", "44072", "44073", "44074", "44075", "44076"};
    private AsyncChannel mAc;
    private DcActivatingState mActivatingState = new DcActivatingState(this, null);
    private DcActiveState mActiveState = new DcActiveState(this, null);
    public HashMap<ApnContext, ConnectionParams> mApnContexts = null;
    private ApnSetting mApnSetting;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            DataConnection.this.log("mBroadcastReceiver - " + action);
            if (action.equals(DataConnection.ACTION_DDS_SWITCH_DONE)) {
                int ddsSubId = intent.getIntExtra("subscription", -1);
                DataConnection.this.log("got ACTION_DDS_SWITCH_DONE, new DDS = " + ddsSubId + "update network score");
                if (DataConnection.this.mNetworkAgent != null && DataConnection.this.mPhone.getSubId() != ddsSubId) {
                    DataConnection.this.sendMessage(DataConnection.this.obtainMessage(DataConnection.EVENT_DATA_CONNECTION_DDS_SWITCHED));
                }
            }
        }
    };
    public int mCid;
    private ConnectionParams mConnectionParams;
    private long mCreateTime;
    private int mDataRegState = Integer.MAX_VALUE;
    private DcController mDcController;
    private DcFailCause mDcFailCause;
    private DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    private DcTracker mDct = null;
    private DcDefaultState mDefaultState = new DcDefaultState(this, null);
    private DisconnectParams mDisconnectParams;
    private DcDisconnectionErrorCreatingConnection mDisconnectingErrorCreatingConnection = new DcDisconnectionErrorCreatingConnection(this, null);
    private DcDisconnectingState mDisconnectingState = new DcDisconnectingState(this, null);
    private int mId;
    private DcInactiveState mInactiveState = new DcInactiveState(this, null);
    private DcFailCause mLastFailCause;
    private long mLastFailTime;
    private LinkProperties mLinkProperties = new LinkProperties();
    private NetworkAgent mNetworkAgent;
    private NetworkInfo mNetworkInfo;
    protected String[] mPcscfAddr;
    private Phone mPhone;
    PendingIntent mReconnectIntent = null;
    private boolean mRegistered = false;
    private boolean mRestrictedNetworkOverride = false;
    private int mRilRat = Integer.MAX_VALUE;
    int mTag;
    private Object mUserData;

    public static class ConnectionParams {
        ApnContext mApnContext;
        final int mConnectionGeneration;
        Message mOnCompletedMsg;
        int mProfileId;
        int mRilRat;
        int mTag;
        final boolean mUnmeteredUseOnly;

        ConnectionParams(ApnContext apnContext, int profileId, int rilRadioTechnology, boolean unmeteredUseOnly, Message onCompletedMsg, int connectionGeneration) {
            this.mApnContext = apnContext;
            this.mProfileId = profileId;
            this.mRilRat = rilRadioTechnology;
            this.mUnmeteredUseOnly = unmeteredUseOnly;
            this.mOnCompletedMsg = onCompletedMsg;
            this.mConnectionGeneration = connectionGeneration;
        }

        public String toString() {
            return "{mTag=" + this.mTag + " mApnContext=" + this.mApnContext + " mProfileId=" + this.mProfileId + " mRat=" + this.mRilRat + " mUnmeteredUseOnly=" + this.mUnmeteredUseOnly + " mOnCompletedMsg=" + DataConnection.msgToString(this.mOnCompletedMsg) + "}";
        }
    }

    private class DcActivatingState extends State {
        /* renamed from: -com-android-internal-telephony-dataconnection-DataCallResponse$SetupResultSwitchesValues */
        private static final /* synthetic */ int[] f31x5f2cdfda = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$internal$telephony$dataconnection$DataCallResponse$SetupResult;

        /* renamed from: -getcom-android-internal-telephony-dataconnection-DataCallResponse$SetupResultSwitchesValues */
        private static /* synthetic */ int[] m33xa73f2fb6() {
            if (f31x5f2cdfda != null) {
                return f31x5f2cdfda;
            }
            int[] iArr = new int[SetupResult.values().length];
            try {
                iArr[SetupResult.ERR_BadCommand.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[SetupResult.ERR_GetLastErrorFromRil.ordinal()] = 6;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[SetupResult.ERR_RilError.ordinal()] = 2;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[SetupResult.ERR_Stale.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[SetupResult.ERR_UnacceptableParameter.ordinal()] = 4;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[SetupResult.SUCCESS.ordinal()] = 5;
            } catch (NoSuchFieldError e6) {
            }
            f31x5f2cdfda = iArr;
            return iArr;
        }

        /* synthetic */ DcActivatingState(DataConnection this$0, DcActivatingState -this1) {
            this();
        }

        private DcActivatingState() {
        }

        public boolean processMessage(Message msg) {
            DataConnection.this.log("DcActivatingState: msg=" + DataConnection.msgToString(msg));
            AsyncResult ar;
            ConnectionParams cp;
            switch (msg.what) {
                case InboundSmsTracker.DEST_PORT_FLAG_3GPP2 /*262144*/:
                case DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED /*262155*/:
                    DataConnection.this.deferMessage(msg);
                    return true;
                case DataConnection.EVENT_SETUP_DATA_CONNECTION_DONE /*262145*/:
                    ar = msg.obj;
                    cp = ar.userObj;
                    SetupResult result = DataConnection.this.onSetupConnectionCompleted(ar);
                    if (!(result == SetupResult.ERR_Stale || DataConnection.this.mConnectionParams == cp)) {
                        DataConnection.this.loge("DcActivatingState: WEIRD mConnectionsParams:" + DataConnection.this.mConnectionParams + " != cp:" + cp);
                    }
                    DataConnection.this.log("DcActivatingState onSetupConnectionCompleted result=" + result + " dc=" + DataConnection.this);
                    if (cp.mApnContext != null) {
                        cp.mApnContext.requestLog("onSetupConnectionCompleted result=" + result);
                    }
                    switch (m33xa73f2fb6()[result.ordinal()]) {
                        case 1:
                            DataConnection.this.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                            DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                            break;
                        case 2:
                            long delay = DataConnection.this.getSuggestedRetryDelay(ar);
                            if (result.mFailCause.isTelstraEsmCause()) {
                                DataConnection.this.log("DcActivatingState: resetDelayTimeForAuTelstraOperator");
                                delay = DataConnection.this.resetDelayTimeForAuTelstraOperator(delay);
                            }
                            cp.mApnContext.setModemSuggestedDelay(delay);
                            String str = "DcActivatingState: ERR_RilError  delay=" + delay + " result=" + result + " result.isRestartRadioFail=" + result.mFailCause.isRestartRadioFail(DataConnection.this.mPhone.getContext(), DataConnection.this.mPhone.getSubId()) + " isPermanentFailure=" + DataConnection.this.mDct.isPermanentFailure(result.mFailCause);
                            DataConnection.this.log(str);
                            if (cp.mApnContext != null) {
                                cp.mApnContext.requestLog(str);
                            }
                            DataConnection.this.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                            DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                            break;
                        case 3:
                            DataConnection.this.loge("DcActivatingState: stale EVENT_SETUP_DATA_CONNECTION_DONE tag:" + cp.mTag + " != mTag:" + DataConnection.this.mTag);
                            break;
                        case 4:
                            DataConnection.this.tearDownData(cp);
                            DataConnection.this.transitionTo(DataConnection.this.mDisconnectingErrorCreatingConnection);
                            break;
                        case 5:
                            DataConnection.this.mDcFailCause = DcFailCause.NONE;
                            DataConnection.this.transitionTo(DataConnection.this.mActiveState);
                            break;
                        default:
                            DataConnection.this.loge("Unknown SetupResult, should not happen");
                            break;
                    }
                    return true;
                case DataConnection.EVENT_GET_LAST_FAIL_DONE /*262146*/:
                    ar = (AsyncResult) msg.obj;
                    cp = (ConnectionParams) ar.userObj;
                    if (cp.mTag == DataConnection.this.mTag) {
                        if (DataConnection.this.mConnectionParams != cp) {
                            DataConnection.this.loge("DcActivatingState: WEIRD mConnectionsParams:" + DataConnection.this.mConnectionParams + " != cp:" + cp);
                        }
                        DcFailCause cause = DcFailCause.UNKNOWN;
                        if (ar.exception == null) {
                            cause = DcFailCause.fromInt(((int[]) ar.result)[0]);
                            if (cause == DcFailCause.NONE) {
                                DataConnection.this.log("DcActivatingState msg.what=EVENT_GET_LAST_FAIL_DONE BAD: error was NONE, change to UNKNOWN");
                                cause = DcFailCause.UNKNOWN;
                            }
                        }
                        DataConnection.this.mDcFailCause = cause;
                        DataConnection.this.log("DcActivatingState msg.what=EVENT_GET_LAST_FAIL_DONE cause=" + cause + " dc=" + DataConnection.this);
                        DataConnection.this.mInactiveState.setEnterNotificationParams(cp, cause);
                        DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                    } else {
                        DataConnection.this.loge("DcActivatingState: stale EVENT_GET_LAST_FAIL_DONE tag:" + cp.mTag + " != mTag:" + DataConnection.this.mTag);
                    }
                    return true;
                default:
                    DataConnection.this.log("DcActivatingState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what) + " RefCount=" + DataConnection.this.mApnContexts.size());
                    return false;
            }
        }
    }

    private class DcActiveState extends State {
        /* synthetic */ DcActiveState(DataConnection this$0, DcActiveState -this1) {
            this();
        }

        private DcActiveState() {
        }

        public void enter() {
            DataConnection.this.log("DcActiveState: enter dc=" + DataConnection.this);
            ServiceState ss = DataConnection.this.mPhone.getServiceState();
            int networkType = ss.getDataNetworkType();
            if (DataConnection.this.mNetworkInfo.getSubtype() != networkType) {
                DataConnection.this.log("DcActiveState with incorrect subtype (" + DataConnection.this.mNetworkInfo.getSubtype() + ", " + networkType + "), updating.");
            }
            DataConnection.this.mNetworkInfo.setSubtype(networkType, TelephonyManager.getNetworkTypeName(networkType));
            boolean roaming = ss.getDataRoaming();
            if (roaming != DataConnection.this.mNetworkInfo.isRoaming()) {
                DataConnection.this.log("DcActiveState with incorrect roaming (" + DataConnection.this.mNetworkInfo.isRoaming() + ", " + roaming + "), updating.");
            }
            DataConnection.this.mNetworkInfo.setRoaming(roaming);
            DataConnection.this.notifyAllOfConnected(PhoneInternalInterface.REASON_CONNECTED);
            DataConnection.this.mPhone.getCallTracker().registerForVoiceCallStarted(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_STARTED, null);
            DataConnection.this.mPhone.getCallTracker().registerForVoiceCallEnded(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_ENDED, null);
            DataConnection.this.mDcController.addActiveDcByCid(DataConnection.this);
            DataConnection.this.mNetworkInfo.setDetailedState(DetailedState.CONNECTED, DataConnection.this.mNetworkInfo.getReason(), null);
            DataConnection.this.mNetworkInfo.setExtraInfo(DataConnection.this.mApnSetting.apn);
            DataConnection.this.updateTcpBufferSizes(DataConnection.this.mRilRat);
            NetworkMisc misc = new NetworkMisc();
            if (DataConnection.this.mPhone.getCarrierSignalAgent().hasRegisteredReceivers("com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED")) {
                misc.provisioningNotificationDisabled = true;
            }
            misc.subscriberId = DataConnection.this.mPhone.getSubscriberId();
            DataConnection.this.setNetworkRestriction();
            if (OemConstant.getWlanAssistantEnable(DataConnection.this.mPhone.getContext())) {
                DataConnection.this.mNetworkAgent = new DcNetworkAgent(DataConnection.this.getHandler().getLooper(), DataConnection.this.mPhone.getContext(), "DcNetworkAgent", DataConnection.this.mNetworkInfo, DataConnection.this.getNetworkCapabilities(), DataConnection.this.mLinkProperties, 10, misc);
            } else {
                DataConnection.this.mNetworkAgent = new DcNetworkAgent(DataConnection.this.getHandler().getLooper(), DataConnection.this.mPhone.getContext(), "DcNetworkAgent", DataConnection.this.mNetworkInfo, DataConnection.this.getNetworkCapabilities(), DataConnection.this.mLinkProperties, 50, misc);
            }
            if (DataConnection.this.isApnTypeDefault() && (DataConnection.this.mRegistered ^ 1) != 0) {
                DataConnection.this.mPhone.getContext().registerReceiver(DataConnection.this.mBroadcastReceiver, new IntentFilter(DataConnection.ACTION_DDS_SWITCH_DONE));
                DataConnection.this.mRegistered = true;
            }
        }

        public void exit() {
            DataConnection.this.log("DcActiveState: exit dc=" + this);
            String reason = DataConnection.this.mNetworkInfo.getReason();
            if (DataConnection.this.mDcController.isExecutingCarrierChange()) {
                reason = PhoneInternalInterface.REASON_CARRIER_CHANGE;
            } else if (DataConnection.this.mDisconnectParams != null && DataConnection.this.mDisconnectParams.mReason != null) {
                reason = DataConnection.this.mDisconnectParams.mReason;
            } else if (DataConnection.this.mDcFailCause != null) {
                reason = DataConnection.this.mDcFailCause.toString();
            }
            DataConnection.this.mPhone.getCallTracker().unregisterForVoiceCallStarted(DataConnection.this.getHandler());
            DataConnection.this.mPhone.getCallTracker().unregisterForVoiceCallEnded(DataConnection.this.getHandler());
            DataConnection.this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, reason, DataConnection.this.mNetworkInfo.getExtraInfo());
            if (DataConnection.this.mRegistered) {
                DataConnection.this.mPhone.getContext().unregisterReceiver(DataConnection.this.mBroadcastReceiver);
                DataConnection.this.mRegistered = false;
            }
            if (DataConnection.this.mNetworkAgent != null) {
                DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                DataConnection.this.mNetworkAgent = null;
            }
        }

        public boolean processMessage(Message msg) {
            DisconnectParams dp;
            switch (msg.what) {
                case InboundSmsTracker.DEST_PORT_FLAG_3GPP2 /*262144*/:
                    ConnectionParams cp = msg.obj;
                    DataConnection.this.mApnContexts.put(cp.mApnContext, cp);
                    DataConnection.this.log("DcActiveState: EVENT_CONNECT cp=" + cp + " dc=" + DataConnection.this);
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.log("DcActiveState: EVENT_CONNECT OPPO send Capabilitise to ConnectivityService");
                    }
                    DataConnection.this.notifyConnectCompleted(cp, DcFailCause.NONE, false);
                    return true;
                case DataConnection.EVENT_DISCONNECT /*262148*/:
                    dp = msg.obj;
                    DataConnection.this.log("DcActiveState: EVENT_DISCONNECT dp=" + dp + " dc=" + DataConnection.this);
                    if (DataConnection.this.mApnContexts.containsKey(dp.mApnContext)) {
                        DataConnection.this.log("DcActiveState msg.what=EVENT_DISCONNECT RefCount=" + DataConnection.this.mApnContexts.size());
                        if (DataConnection.this.mApnContexts.size() == 1) {
                            DataConnection.this.mApnContexts.clear();
                            DataConnection.this.mDisconnectParams = dp;
                            DataConnection.this.mConnectionParams = null;
                            dp.mTag = DataConnection.this.mTag;
                            DataConnection.this.tearDownData(dp);
                            DataConnection.this.transitionTo(DataConnection.this.mDisconnectingState);
                        } else {
                            DataConnection.this.mApnContexts.remove(dp.mApnContext);
                            if (DataConnection.this.mNetworkAgent != null) {
                                DataConnection.this.log("DcActiveState: EVENT_DISCONNECT OPPO send Capabilitise to ConnectivityService");
                            }
                            DataConnection.this.notifyDisconnectCompleted(dp, false);
                        }
                    } else {
                        DataConnection.this.log("DcActiveState ERROR no such apnContext=" + dp.mApnContext + " in this dc=" + DataConnection.this);
                        DataConnection.this.notifyDisconnectCompleted(dp, false);
                    }
                    return true;
                case DataConnection.EVENT_DISCONNECT_ALL /*262150*/:
                    DataConnection.this.log("DcActiveState EVENT_DISCONNECT clearing apn contexts, dc=" + DataConnection.this);
                    dp = (DisconnectParams) msg.obj;
                    DataConnection.this.mDisconnectParams = dp;
                    DataConnection.this.mConnectionParams = null;
                    dp.mTag = DataConnection.this.mTag;
                    DataConnection.this.tearDownData(dp);
                    DataConnection.this.transitionTo(DataConnection.this.mDisconnectingState);
                    return true;
                case DataConnection.EVENT_LOST_CONNECTION /*262153*/:
                    DataConnection.this.log("DcActiveState EVENT_LOST_CONNECTION dc=" + DataConnection.this);
                    DataConnection.this.mInactiveState.setEnterNotificationParams(DcFailCause.LOST_CONNECTION);
                    DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                    return true;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_ON /*262156*/:
                    DataConnection.this.mNetworkInfo.setRoaming(true);
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                    }
                    return true;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF /*262157*/:
                    DataConnection.this.mNetworkInfo.setRoaming(false);
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                    }
                    return true;
                case DataConnection.EVENT_BW_REFRESH_RESPONSE /*262158*/:
                    AsyncResult ar = msg.obj;
                    if (ar.exception != null) {
                        DataConnection.this.log("EVENT_BW_REFRESH_RESPONSE: error ignoring, e=" + ar.exception);
                    } else {
                        int lceBwDownKbps = ((Integer) ar.result.get(0)).intValue();
                        NetworkCapabilities nc = DataConnection.this.getNetworkCapabilities();
                        if (DataConnection.this.mPhone.getLceStatus() == 1 && lceBwDownKbps > 0) {
                            nc.setLinkDownstreamBandwidthKbps(lceBwDownKbps);
                            if (DataConnection.this.mNetworkAgent != null) {
                                DataConnection.this.mNetworkAgent.sendNetworkCapabilities(nc);
                            }
                        }
                    }
                    return true;
                case DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_STARTED /*262159*/:
                case DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_ENDED /*262160*/:
                    if (DataConnection.this.updateNetworkInfoSuspendState() && DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                    }
                    return true;
                default:
                    DataConnection.this.log("DcActiveState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    return false;
            }
        }
    }

    private class DcDefaultState extends State {
        /* synthetic */ DcDefaultState(DataConnection this$0, DcDefaultState -this1) {
            this();
        }

        private DcDefaultState() {
        }

        public void enter() {
            DataConnection.this.log("DcDefaultState: enter");
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED, null);
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRoamingOn(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_ROAM_ON, null);
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRoamingOff(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF, null, true);
            DataConnection.this.mDcController.addDc(DataConnection.this);
        }

        public void exit() {
            DataConnection.this.log("DcDefaultState: exit");
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(DataConnection.this.getHandler());
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(DataConnection.this.getHandler());
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(DataConnection.this.getHandler());
            DataConnection.this.mDcController.removeDc(DataConnection.this);
            if (DataConnection.this.mAc != null) {
                DataConnection.this.mAc.disconnected();
                DataConnection.this.mAc = null;
            }
            DataConnection.this.mApnContexts = null;
            DataConnection.this.mReconnectIntent = null;
            DataConnection.this.mDct = null;
            DataConnection.this.mApnSetting = null;
            DataConnection.this.mPhone = null;
            DataConnection.this.mLinkProperties = null;
            DataConnection.this.mLastFailCause = null;
            DataConnection.this.mUserData = null;
            DataConnection.this.mDcController = null;
            DataConnection.this.mDcTesterFailBringUpAll = null;
        }

        public boolean processMessage(Message msg) {
            DataConnection.this.log("DcDefault msg=" + DataConnection.this.getWhatToString(msg.what) + " RefCount=" + DataConnection.this.mApnContexts.size());
            switch (msg.what) {
                case 69633:
                    if (DataConnection.this.mAc == null) {
                        DataConnection.this.mAc = new AsyncChannel();
                        DataConnection.this.mAc.connected(null, DataConnection.this.getHandler(), msg.replyTo);
                        DataConnection.this.log("DcDefaultState: FULL_CONNECTION reply connected");
                        DataConnection.this.mAc.replyToMessage(msg, 69634, 0, DataConnection.this.mId, "hi");
                        break;
                    }
                    DataConnection.this.log("Disconnecting to previous connection mAc=" + DataConnection.this.mAc);
                    DataConnection.this.mAc.replyToMessage(msg, 69634, 3);
                    break;
                case 69636:
                    DataConnection.this.log("DcDefault: CMD_CHANNEL_DISCONNECTED before quiting call dump");
                    DataConnection.this.dumpToLog();
                    DataConnection.this.quit();
                    break;
                case InboundSmsTracker.DEST_PORT_FLAG_3GPP2 /*262144*/:
                    DataConnection.this.log("DcDefaultState: msg.what=EVENT_CONNECT, fail not expected");
                    DataConnection.this.notifyConnectCompleted(msg.obj, DcFailCause.UNKNOWN, false);
                    break;
                case DataConnection.EVENT_DISCONNECT /*262148*/:
                    DataConnection.this.log("DcDefaultState deferring msg.what=EVENT_DISCONNECT RefCount=" + DataConnection.this.mApnContexts.size());
                    DataConnection.this.deferMessage(msg);
                    break;
                case DataConnection.EVENT_DISCONNECT_ALL /*262150*/:
                    DataConnection.this.log("DcDefaultState deferring msg.what=EVENT_DISCONNECT_ALL RefCount=" + DataConnection.this.mApnContexts.size());
                    DataConnection.this.deferMessage(msg);
                    break;
                case DataConnection.EVENT_TEAR_DOWN_NOW /*262152*/:
                    DataConnection.this.log("DcDefaultState EVENT_TEAR_DOWN_NOW");
                    DataConnection.this.mPhone.mCi.deactivateDataCall(DataConnection.this.mCid, 0, null);
                    break;
                case DataConnection.EVENT_LOST_CONNECTION /*262153*/:
                    DataConnection.this.logAndAddLogRec("DcDefaultState ignore EVENT_LOST_CONNECTION tag=" + msg.arg1 + ":mTag=" + DataConnection.this.mTag);
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED /*262155*/:
                    Pair<Integer, Integer> drsRatPair = msg.obj.result;
                    DataConnection.this.mDataRegState = ((Integer) drsRatPair.first).intValue();
                    if (DataConnection.this.mRilRat != ((Integer) drsRatPair.second).intValue()) {
                        DataConnection.this.updateTcpBufferSizes(((Integer) drsRatPair.second).intValue());
                    }
                    DataConnection.this.mRilRat = ((Integer) drsRatPair.second).intValue();
                    DataConnection.this.log("DcDefaultState: EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED drs=" + DataConnection.this.mDataRegState + " mRilRat=" + DataConnection.this.mRilRat);
                    int networkType = DataConnection.this.mPhone.getServiceState().getDataNetworkType();
                    DataConnection.this.mNetworkInfo.setSubtype(networkType, TelephonyManager.getNetworkTypeName(networkType));
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.updateNetworkInfoSuspendState();
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities());
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                        DataConnection.this.mNetworkAgent.sendLinkProperties(DataConnection.this.mLinkProperties);
                        break;
                    }
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_ON /*262156*/:
                    DataConnection.this.mNetworkInfo.setRoaming(true);
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF /*262157*/:
                    DataConnection.this.mNetworkInfo.setRoaming(false);
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_DDS_SWITCHED /*262161*/:
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkScore(50);
                        break;
                    }
                    break;
                case 266240:
                    boolean val = DataConnection.this.getIsInactive();
                    DataConnection.this.log("REQ_IS_INACTIVE  isInactive=" + val);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_IS_INACTIVE, val ? 1 : 0);
                    break;
                case DcAsyncChannel.REQ_GET_CID /*266242*/:
                    int cid = DataConnection.this.getCid();
                    DataConnection.this.log("REQ_GET_CID  cid=" + cid);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_CID, cid);
                    break;
                case DcAsyncChannel.REQ_GET_APNSETTING /*266244*/:
                    ApnSetting apnSetting = DataConnection.this.getApnSetting();
                    DataConnection.this.log("REQ_GET_APNSETTING  mApnSetting=" + apnSetting);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_APNSETTING, apnSetting);
                    break;
                case DcAsyncChannel.REQ_GET_LINK_PROPERTIES /*266246*/:
                    LinkProperties lp = DataConnection.this.getCopyLinkProperties();
                    DataConnection.this.log("REQ_GET_LINK_PROPERTIES linkProperties" + lp);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_LINK_PROPERTIES, lp);
                    break;
                case DcAsyncChannel.REQ_SET_LINK_PROPERTIES_HTTP_PROXY /*266248*/:
                    ProxyInfo proxy = msg.obj;
                    DataConnection.this.log("REQ_SET_LINK_PROPERTIES_HTTP_PROXY proxy=" + proxy);
                    DataConnection.this.setLinkPropertiesHttpProxy(proxy);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_SET_LINK_PROPERTIES_HTTP_PROXY);
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendLinkProperties(DataConnection.this.mLinkProperties);
                        break;
                    }
                    break;
                case DcAsyncChannel.REQ_GET_NETWORK_CAPABILITIES /*266250*/:
                    NetworkCapabilities nc = DataConnection.this.getNetworkCapabilities();
                    DataConnection.this.log("REQ_GET_NETWORK_CAPABILITIES networkCapabilities" + nc);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_NETWORK_CAPABILITIES, nc);
                    break;
                case DcAsyncChannel.REQ_RESET /*266252*/:
                    DataConnection.this.log("DcDefaultState: msg.what=REQ_RESET");
                    DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                    break;
                default:
                    DataConnection.this.log("DcDefaultState: shouldn't happen but ignore msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    break;
            }
            return true;
        }
    }

    private class DcDisconnectingState extends State {
        /* synthetic */ DcDisconnectingState(DataConnection this$0, DcDisconnectingState -this1) {
            this();
        }

        private DcDisconnectingState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case InboundSmsTracker.DEST_PORT_FLAG_3GPP2 /*262144*/:
                    DataConnection.this.log("DcDisconnectingState msg.what=EVENT_CONNECT. Defer. RefCount = " + DataConnection.this.mApnContexts.size());
                    DataConnection.this.deferMessage(msg);
                    return true;
                case DataConnection.EVENT_DEACTIVATE_DONE /*262147*/:
                    AsyncResult ar = msg.obj;
                    DisconnectParams dp = ar.userObj;
                    String str = "DcDisconnectingState msg.what=EVENT_DEACTIVATE_DONE RefCount=" + DataConnection.this.mApnContexts.size();
                    DataConnection.this.log(str);
                    if (dp.mApnContext != null) {
                        dp.mApnContext.requestLog(str);
                    }
                    if (dp.mTag == DataConnection.this.mTag) {
                        DataConnection.this.mInactiveState.setEnterNotificationParams((DisconnectParams) ar.userObj);
                        DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                    } else {
                        DataConnection.this.log("DcDisconnectState stale EVENT_DEACTIVATE_DONE dp.tag=" + dp.mTag + " mTag=" + DataConnection.this.mTag);
                    }
                    return true;
                default:
                    DataConnection.this.log("DcDisconnectingState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    return false;
            }
        }
    }

    private class DcDisconnectionErrorCreatingConnection extends State {
        /* synthetic */ DcDisconnectionErrorCreatingConnection(DataConnection this$0, DcDisconnectionErrorCreatingConnection -this1) {
            this();
        }

        private DcDisconnectionErrorCreatingConnection() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case DataConnection.EVENT_DEACTIVATE_DONE /*262147*/:
                    ConnectionParams cp = msg.obj.userObj;
                    if (cp.mTag == DataConnection.this.mTag) {
                        String str = "DcDisconnectionErrorCreatingConnection msg.what=EVENT_DEACTIVATE_DONE";
                        DataConnection.this.log(str);
                        if (cp.mApnContext != null) {
                            cp.mApnContext.requestLog(str);
                        }
                        DataConnection.this.mInactiveState.setEnterNotificationParams(cp, DcFailCause.UNACCEPTABLE_NETWORK_PARAMETER);
                        DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                    } else {
                        DataConnection.this.log("DcDisconnectionErrorCreatingConnection stale EVENT_DEACTIVATE_DONE dp.tag=" + cp.mTag + ", mTag=" + DataConnection.this.mTag);
                    }
                    return true;
                default:
                    DataConnection.this.log("DcDisconnectionErrorCreatingConnection not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    return false;
            }
        }
    }

    private class DcInactiveState extends State {
        /* synthetic */ DcInactiveState(DataConnection this$0, DcInactiveState -this1) {
            this();
        }

        private DcInactiveState() {
        }

        public void setEnterNotificationParams(ConnectionParams cp, DcFailCause cause) {
            DataConnection.this.log("DcInactiveState: setEnterNotificationParams cp,cause");
            DataConnection.this.mConnectionParams = cp;
            DataConnection.this.mDisconnectParams = null;
            DataConnection.this.mDcFailCause = cause;
        }

        public void setEnterNotificationParams(DisconnectParams dp) {
            DataConnection.this.log("DcInactiveState: setEnterNotificationParams dp");
            DataConnection.this.mConnectionParams = null;
            DataConnection.this.mDisconnectParams = dp;
            DataConnection.this.mDcFailCause = DcFailCause.NONE;
        }

        public void setEnterNotificationParams(DcFailCause cause) {
            DataConnection.this.mConnectionParams = null;
            DataConnection.this.mDisconnectParams = null;
            DataConnection.this.mDcFailCause = cause;
        }

        public void enter() {
            DataConnection dataConnection = DataConnection.this;
            dataConnection.mTag++;
            DataConnection.this.log("DcInactiveState: enter() mTag=" + DataConnection.this.mTag);
            if (DataConnection.this.mConnectionParams != null) {
                DataConnection.this.log("DcInactiveState: enter notifyConnectCompleted +ALL failCause=" + DataConnection.this.mDcFailCause);
                DataConnection.this.notifyConnectCompleted(DataConnection.this.mConnectionParams, DataConnection.this.mDcFailCause, true);
            }
            if (DataConnection.this.mDisconnectParams != null) {
                DataConnection.this.log("DcInactiveState: enter notifyDisconnectCompleted +ALL failCause=" + DataConnection.this.mDcFailCause);
                DataConnection.this.notifyDisconnectCompleted(DataConnection.this.mDisconnectParams, true);
            }
            if (DataConnection.this.mDisconnectParams == null && DataConnection.this.mConnectionParams == null && DataConnection.this.mDcFailCause != null) {
                DataConnection.this.log("DcInactiveState: enter notifyAllDisconnectCompleted failCause=" + DataConnection.this.mDcFailCause);
                DataConnection.this.notifyAllDisconnectCompleted(DataConnection.this.mDcFailCause);
            }
            DataConnection.this.mDcController.removeActiveDcByCid(DataConnection.this);
            DataConnection.this.clearSettings();
        }

        public void exit() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case InboundSmsTracker.DEST_PORT_FLAG_3GPP2 /*262144*/:
                    DataConnection.this.log("DcInactiveState: mag.what=EVENT_CONNECT");
                    ConnectionParams cp = msg.obj;
                    if (DataConnection.this.initConnection(cp)) {
                        DataConnection.this.onConnect(DataConnection.this.mConnectionParams);
                        DataConnection.this.transitionTo(DataConnection.this.mActivatingState);
                    } else {
                        DataConnection.this.log("DcInactiveState: msg.what=EVENT_CONNECT initConnection failed");
                        DataConnection.this.notifyConnectCompleted(cp, DcFailCause.UNACCEPTABLE_NETWORK_PARAMETER, false);
                    }
                    return true;
                case DataConnection.EVENT_DISCONNECT /*262148*/:
                    DataConnection.this.log("DcInactiveState: msg.what=EVENT_DISCONNECT");
                    DataConnection.this.notifyDisconnectCompleted((DisconnectParams) msg.obj, false);
                    return true;
                case DataConnection.EVENT_DISCONNECT_ALL /*262150*/:
                    DataConnection.this.log("DcInactiveState: msg.what=EVENT_DISCONNECT_ALL");
                    DataConnection.this.notifyDisconnectCompleted((DisconnectParams) msg.obj, false);
                    return true;
                case DcAsyncChannel.REQ_RESET /*266252*/:
                    DataConnection.this.log("DcInactiveState: msg.what=RSP_RESET, ignore we're already reset");
                    return true;
                default:
                    DataConnection.this.log("DcInactiveState nothandled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    return false;
            }
        }
    }

    private class DcNetworkAgent extends NetworkAgent {
        public DcNetworkAgent(Looper l, Context c, String TAG, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, TAG, ni, nc, lp, score, misc);
        }

        protected void unwanted() {
            if (DataConnection.this.mNetworkAgent != this) {
                log("DcNetworkAgent: unwanted found mNetworkAgent=" + DataConnection.this.mNetworkAgent + ", which isn't me.  Aborting unwanted");
            } else if (DataConnection.this.mApnContexts != null) {
                for (ConnectionParams cp : DataConnection.this.mApnContexts.values()) {
                    ApnContext apnContext = cp.mApnContext;
                    Pair<ApnContext, Integer> pair = new Pair(apnContext, Integer.valueOf(cp.mConnectionGeneration));
                    log("DcNetworkAgent: [unwanted]: disconnect apnContext=" + apnContext);
                    DataConnection.this.sendMessage(DataConnection.this.obtainMessage(DataConnection.EVENT_DISCONNECT, new DisconnectParams(apnContext, apnContext.getReason(), DataConnection.this.mDct.obtainMessage(270351, pair))));
                }
            }
        }

        protected void pollLceData() {
            if (DataConnection.this.mPhone.getLceStatus() == 1) {
                DataConnection.this.mPhone.mCi.pullLceData(DataConnection.this.obtainMessage(DataConnection.EVENT_BW_REFRESH_RESPONSE));
            }
        }

        protected void networkStatus(int status, String redirectUrl) {
            if (!TextUtils.isEmpty(redirectUrl)) {
                log("validation status: " + status + " with redirection URL: " + redirectUrl);
                DataConnection.this.mDct.obtainMessage(270380, redirectUrl).sendToTarget();
            }
        }
    }

    public static class DisconnectParams {
        public ApnContext mApnContext;
        Message mOnCompletedMsg;
        String mReason;
        int mTag;

        DisconnectParams(ApnContext apnContext, String reason, Message onCompletedMsg) {
            this.mApnContext = apnContext;
            this.mReason = reason;
            this.mOnCompletedMsg = onCompletedMsg;
        }

        public String toString() {
            return "{mTag=" + this.mTag + " mApnContext=" + this.mApnContext + " mReason=" + this.mReason + " mOnCompletedMsg=" + DataConnection.msgToString(this.mOnCompletedMsg) + "}";
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

    static {
        sCmdToString[0] = "EVENT_CONNECT";
        sCmdToString[1] = "EVENT_SETUP_DATA_CONNECTION_DONE";
        sCmdToString[2] = "EVENT_GET_LAST_FAIL_DONE";
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
        sCmdToString[17] = "EVENT_DATA_CONNECTION_DDS_SWITCHED";
    }

    static String cmdToString(int cmd) {
        String value;
        cmd -= InboundSmsTracker.DEST_PORT_FLAG_3GPP2;
        if (cmd < 0 || cmd >= sCmdToString.length) {
            value = DcAsyncChannel.cmdToString(cmd + InboundSmsTracker.DEST_PORT_FLAG_3GPP2);
        } else {
            value = sCmdToString[cmd];
        }
        if (value == null) {
            return "0x" + Integer.toHexString(cmd + InboundSmsTracker.DEST_PORT_FLAG_3GPP2);
        }
        return value;
    }

    public static DataConnection makeDataConnection(Phone phone, int id, DcTracker dct, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        DataConnection dc = new DataConnection(phone, "DC-" + mInstanceNumber.incrementAndGet(), id, dct, failBringUpAll, dcc);
        dc.start();
        dc.log("Made " + dc.getName());
        return dc;
    }

    void dispose() {
        log("dispose: call quiteNow()");
        quitNow();
    }

    LinkProperties getCopyLinkProperties() {
        return new LinkProperties(this.mLinkProperties);
    }

    boolean getIsInactive() {
        return getCurrentState() == this.mInactiveState;
    }

    int getCid() {
        return this.mCid;
    }

    ApnSetting getApnSetting() {
        return this.mApnSetting;
    }

    void setLinkPropertiesHttpProxy(ProxyInfo proxy) {
        this.mLinkProperties.setHttpProxy(proxy);
    }

    public boolean isIpv4Connected() {
        for (InetAddress addr : this.mLinkProperties.getAddresses()) {
            if (addr instanceof Inet4Address) {
                Inet4Address i4addr = (Inet4Address) addr;
                if (!(i4addr.isAnyLocalAddress() || (i4addr.isLinkLocalAddress() ^ 1) == 0 || (i4addr.isLoopbackAddress() ^ 1) == 0 || (i4addr.isMulticastAddress() ^ 1) == 0)) {
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
                if (!(i6addr.isAnyLocalAddress() || (i6addr.isLinkLocalAddress() ^ 1) == 0 || (i6addr.isLoopbackAddress() ^ 1) == 0 || (i6addr.isMulticastAddress() ^ 1) == 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    public UpdateLinkPropertyResult updateLinkProperty(DataCallResponse newState) {
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
        if ((result.oldLp.equals(result.newLp) ^ 1) != 0) {
            log("updateLinkProperty old LP=" + result.oldLp);
            log("updateLinkProperty new LP=" + result.newLp);
        }
        if (!(result.newLp.equals(result.oldLp) || this.mNetworkAgent == null)) {
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
        return result;
    }

    private void checkSetMtu(ApnSetting apn, LinkProperties lp) {
        if (lp != null && apn != null && lp != null) {
            if (lp.getMtu() != 0 && lp.getMtu() != 1500) {
                log("MTU set by call response to: " + lp.getMtu());
            } else if (apn == null || apn.mtu == 0) {
                int mtu = this.mPhone.getContext().getResources().getInteger(17694814);
                try {
                    int oppoMtu = SystemProperties.getInt("persist.sys.oppo.mtu", 0);
                    if (oppoMtu != 0) {
                        mtu = oppoMtu;
                    } else if (this.mPhone != null) {
                        String defaultMccMnc = TelephonyManager.getDefault().getSimOperatorNumeric(this.mPhone.getSubId());
                        if (!TextUtils.isEmpty(defaultMccMnc)) {
                            int mcc = Integer.parseInt(defaultMccMnc.substring(0, 3));
                            int mnc = Integer.parseInt(defaultMccMnc.substring(3));
                            if (mcc == 460) {
                                mtu = 1410;
                                if (mnc == 3 || mnc == 5 || mnc == 11) {
                                    mtu = 1460;
                                }
                            } else if (mcc == 440) {
                                for (String equals : sKDDIOperator) {
                                    if (equals.equals(defaultMccMnc)) {
                                        mtu = 1440;
                                        break;
                                    }
                                }
                            } else if (mcc == RadioError.OEM_ERROR_5) {
                                for (String equals2 : sAuTelstraOperator) {
                                    if (equals2.equals(defaultMccMnc)) {
                                        mtu = 1358;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log("MTU set error");
                }
                if (mtu != 0) {
                    lp.setMtu(mtu);
                    if (mtu > 1280) {
                        SystemProperties.set("persist.data_netmgrd_mtu", SpnOverride.MVNO_TYPE_NONE + mtu);
                    }
                    log("MTU set by config resource to: " + mtu);
                }
            } else {
                lp.setMtu(apn.mtu);
                log("MTU set by APN to: " + apn.mtu);
            }
        }
    }

    private boolean isApnTypeDefault() {
        for (String type : this.mApnSetting.types) {
            if (type.equals("default")) {
                return true;
            }
        }
        return false;
    }

    private DataConnection(Phone phone, String name, int id, DcTracker dct, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        super(name, dcc.getHandler());
        setLogRecSize(300);
        setLogOnlyTransitions(true);
        log("DataConnection created");
        this.mPhone = phone;
        this.mDct = dct;
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
        addState(this.mDefaultState);
        addState(this.mInactiveState, this.mDefaultState);
        addState(this.mActivatingState, this.mDefaultState);
        addState(this.mActiveState, this.mDefaultState);
        addState(this.mDisconnectingState, this.mDefaultState);
        addState(this.mDisconnectingErrorCreatingConnection, this.mDefaultState);
        setInitialState(this.mInactiveState);
        this.mApnContexts = new HashMap();
    }

    private void onConnect(ConnectionParams cp) {
        DataCallResponse response;
        Message msg;
        log("onConnect: carrier='" + this.mApnSetting.carrier + "' APN='" + this.mApnSetting.apn + "' proxy='" + this.mApnSetting.proxy + "' port='" + this.mApnSetting.port + "'");
        if (cp.mApnContext != null) {
            cp.mApnContext.requestLog("DataConnection.onConnect");
        }
        try {
            if (OemConstant.isPoliceVersion(this.mPhone) || OemConstant.isDeviceLockVersion()) {
                boolean isDataAllow = OemConstant.isDataAllow(this.mPhone);
                String apntype = SpnOverride.MVNO_TYPE_NONE;
                if (!(cp == null || cp.mApnContext == null)) {
                    apntype = cp.mApnContext.getApnType();
                }
                boolean isSpecialApn = !TextUtils.isEmpty(apntype) ? !"ims".equals(apntype) ? "emergency".equals(apntype) : true : false;
                Rlog.d("data", "onConnect:isDataAllow=" + isDataAllow + " apntype=" + apntype + "  isSpecialApn=" + isSpecialApn);
                if (!(isDataAllow || (isSpecialApn ^ 1) == 0)) {
                    response = new DataCallResponse(65535, -1, 0, 0, SpnOverride.MVNO_TYPE_NONE, SpnOverride.MVNO_TYPE_NONE, SpnOverride.MVNO_TYPE_NONE, SpnOverride.MVNO_TYPE_NONE, SpnOverride.MVNO_TYPE_NONE, SpnOverride.MVNO_TYPE_NONE, 0);
                    msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
                    AsyncResult.forMessage(msg, response, null);
                    sendMessage(msg);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mDcTesterFailBringUpAll.getDcFailBringUp().mCounter > 0) {
            response = new DataCallResponse(this.mDcTesterFailBringUpAll.getDcFailBringUp().mFailCause.getErrorCode(), this.mDcTesterFailBringUpAll.getDcFailBringUp().mSuggestedRetryTime, 0, 0, SpnOverride.MVNO_TYPE_NONE, SpnOverride.MVNO_TYPE_NONE, SpnOverride.MVNO_TYPE_NONE, SpnOverride.MVNO_TYPE_NONE, SpnOverride.MVNO_TYPE_NONE, SpnOverride.MVNO_TYPE_NONE, 0);
            msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
            AsyncResult.forMessage(msg, response, null);
            sendMessage(msg);
            log("onConnect: FailBringUpAll=" + this.mDcTesterFailBringUpAll.getDcFailBringUp() + " send error response=" + response);
            DcFailBringUp dcFailBringUp = this.mDcTesterFailBringUpAll.getDcFailBringUp();
            dcFailBringUp.mCounter--;
            return;
        }
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = DcFailCause.NONE;
        msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
        msg.obj = cp;
        DataProfile dp = new DataProfile(this.mApnSetting, cp.mProfileId);
        boolean isModemRoaming = this.mPhone.getServiceState().getDataRoamingFromRegistration();
        boolean allowRoaming = !this.mPhone.getDataRoamingEnabled() ? isModemRoaming ? this.mPhone.getServiceState().getDataRoaming() ^ 1 : false : true;
        this.mPhone.mCi.setupDataCall(cp.mRilRat, dp, isModemRoaming, allowRoaming, msg);
    }

    private void tearDownData(Object o) {
        int discReason = 0;
        ApnContext apnContext = null;
        if (o != null && (o instanceof DisconnectParams)) {
            DisconnectParams dp = (DisconnectParams) o;
            apnContext = dp.mApnContext;
            if (TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_RADIO_TURNED_OFF)) {
                discReason = 1;
            } else if (TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_PDP_RESET)) {
                discReason = 2;
            }
        }
        String str = "tearDownData. mCid=" + this.mCid + ", reason=" + discReason;
        log(str);
        if (apnContext != null) {
            apnContext.requestLog(str);
        }
        this.mPhone.mCi.deactivateDataCall(this.mCid, discReason, obtainMessage(EVENT_DEACTIVATE_DONE, this.mTag, 0, o));
    }

    private void notifyAllWithEvent(ApnContext alreadySent, int event, String reason) {
        this.mNetworkInfo.setDetailedState(this.mNetworkInfo.getDetailedState(), reason, this.mNetworkInfo.getExtraInfo());
        for (ConnectionParams cp : this.mApnContexts.values()) {
            ApnContext apnContext = cp.mApnContext;
            if (apnContext != alreadySent) {
                if (reason != null) {
                    apnContext.setReason(reason);
                }
                Message msg = this.mDct.obtainMessage(event, new Pair(apnContext, Integer.valueOf(cp.mConnectionGeneration)));
                AsyncResult.forMessage(msg);
                msg.sendToTarget();
            }
        }
    }

    private void notifyAllOfConnected(String reason) {
        notifyAllWithEvent(null, 270336, reason);
    }

    private void notifyAllOfDisconnectDcRetrying(String reason) {
        notifyAllWithEvent(null, 270370, reason);
    }

    private void notifyAllDisconnectCompleted(DcFailCause cause) {
        notifyAllWithEvent(null, 270351, cause.toString());
    }

    private void notifyConnectCompleted(ConnectionParams cp, DcFailCause cause, boolean sendAll) {
        ApnContext alreadySent = null;
        if (!(cp == null || cp.mOnCompletedMsg == null)) {
            Message connectionCompletedMsg = cp.mOnCompletedMsg;
            cp.mOnCompletedMsg = null;
            alreadySent = cp.mApnContext;
            long timeStamp = System.currentTimeMillis();
            connectionCompletedMsg.arg1 = this.mCid;
            if (cause == DcFailCause.NONE) {
                this.mCreateTime = timeStamp;
                AsyncResult.forMessage(connectionCompletedMsg);
            } else {
                this.mLastFailCause = cause;
                this.mLastFailTime = timeStamp;
                if (cause == null) {
                    cause = DcFailCause.UNKNOWN;
                }
                AsyncResult.forMessage(connectionCompletedMsg, cause, new Throwable(cause.toString()));
            }
            log("notifyConnectCompleted at " + timeStamp + " cause=" + cause + " connectionCompletedMsg=" + msgToString(connectionCompletedMsg));
            connectionCompletedMsg.sendToTarget();
        }
        if (sendAll) {
            log("Send to all. " + alreadySent + " " + cause.toString());
            notifyAllWithEvent(alreadySent, 270371, cause.toString());
        }
    }

    private void notifyDisconnectCompleted(DisconnectParams dp, boolean sendAll) {
        log("NotifyDisconnectCompleted");
        ApnContext apnContext = null;
        String reason = null;
        if (!(dp == null || dp.mOnCompletedMsg == null)) {
            Message msg = dp.mOnCompletedMsg;
            dp.mOnCompletedMsg = null;
            if (msg.obj instanceof ApnContext) {
                apnContext = msg.obj;
            }
            reason = dp.mReason;
            String str = "msg=%s msg.obj=%s";
            Object[] objArr = new Object[2];
            objArr[0] = msg.toString();
            objArr[1] = msg.obj instanceof String ? (String) msg.obj : "<no-reason>";
            log(String.format(str, objArr));
            AsyncResult.forMessage(msg);
            msg.sendToTarget();
        }
        if (sendAll) {
            if (reason == null) {
                reason = DcFailCause.UNKNOWN.toString();
            }
            notifyAllWithEvent(apnContext, 270351, reason);
        }
        log("NotifyDisconnectCompleted DisconnectParams=" + dp);
    }

    public int getDataConnectionId() {
        return this.mId;
    }

    private void clearSettings() {
        log("clearSettings");
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = DcFailCause.NONE;
        this.mCid = -1;
        this.mPcscfAddr = new String[5];
        this.mLinkProperties = new LinkProperties();
        this.mApnContexts.clear();
        this.mApnSetting = null;
        this.mDcFailCause = null;
    }

    private SetupResult onSetupConnectionCompleted(AsyncResult ar) {
        SetupResult result;
        DataCallResponse response = ar.result;
        ConnectionParams cp = ar.userObj;
        if (cp.mTag != this.mTag) {
            log("onSetupConnectionCompleted stale cp.tag=" + cp.mTag + ", mtag=" + this.mTag);
            result = SetupResult.ERR_Stale;
        } else if (ar.exception != null) {
            log("onSetupConnectionCompleted failed, ar.exception=" + ar.exception + " response=" + response);
            if ((ar.exception instanceof CommandException) && ((CommandException) ar.exception).getCommandError() == Error.RADIO_NOT_AVAILABLE) {
                result = SetupResult.ERR_BadCommand;
                result.mFailCause = DcFailCause.RADIO_NOT_AVAILABLE;
            } else {
                result = SetupResult.ERR_RilError;
                result.mFailCause = DcFailCause.fromInt(response.status);
            }
        } else if (response.status != 0) {
            result = SetupResult.ERR_RilError;
            result.mFailCause = DcFailCause.fromInt(response.status);
        } else {
            log("onSetupConnectionCompleted received successful DataCallResponse");
            this.mCid = response.cid;
            this.mPcscfAddr = response.pcscf;
            result = updateLinkProperty(response).setupResult;
        }
        if (OemConstant.getPowerCenterEnable(this.mPhone.getContext())) {
            ServiceStateTracker.mDataCallCount++;
            log("[POWERSTATE]mDataCallCount:" + ServiceStateTracker.mDataCallCount);
        }
        return result;
    }

    private boolean isDnsOk(String[] domainNameServers) {
        if (!NULL_IP.equals(domainNameServers[0]) || !NULL_IP.equals(domainNameServers[1]) || (this.mPhone.isDnsCheckDisabled() ^ 1) == 0 || (this.mApnSetting.types[0].equals("mms") && (isIpAddress(this.mApnSetting.mmsProxy) ^ 1) == 0)) {
            return true;
        }
        log(String.format("isDnsOk: return false apn.types[0]=%s APN_TYPE_MMS=%s isIpAddress(%s)=%s", new Object[]{this.mApnSetting.types[0], "mms", this.mApnSetting.mmsProxy, Boolean.valueOf(isIpAddress(this.mApnSetting.mmsProxy))}));
        return false;
    }

    private void updateTcpBufferSizes(int rilRat) {
        String sizes = null;
        ServiceState ss = this.mPhone.getServiceState();
        if (rilRat == 14 && ss.isUsingCarrierAggregation()) {
            rilRat = 19;
        }
        String ratName = ServiceState.rilRadioTechnologyToString(rilRat).toLowerCase(Locale.ROOT);
        if (rilRat == 7 || rilRat == 8 || rilRat == 12) {
            ratName = "evdo";
        }
        String[] configOverride = this.mPhone.getContext().getResources().getStringArray(17236020);
        for (String split : configOverride) {
            String[] split2 = split.split(":");
            if (ratName.equals(split2[0]) && split2.length == 2) {
                sizes = split2[1];
                break;
            }
        }
        if (sizes == null) {
            switch (rilRat) {
                case 1:
                    sizes = TCP_BUFFER_SIZES_GPRS;
                    break;
                case 2:
                    sizes = TCP_BUFFER_SIZES_EDGE;
                    break;
                case 3:
                    sizes = TCP_BUFFER_SIZES_UMTS;
                    break;
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
                case 14:
                    sizes = TCP_BUFFER_SIZES_LTE;
                    break;
                case 15:
                    sizes = TCP_BUFFER_SIZES_HSPAP;
                    break;
                case 19:
                    break;
            }
            sizes = TCP_BUFFER_SIZES_LTE_CA;
        }
        this.mLinkProperties.setTcpBufferSizes(sizes);
    }

    private void setNetworkRestriction() {
        boolean z = true;
        this.mRestrictedNetworkOverride = false;
        boolean noRestrictedRequests = true;
        for (ApnContext apnContext : this.mApnContexts.keySet()) {
            noRestrictedRequests &= apnContext.hasNoRestrictedRequests(true);
        }
        if (!noRestrictedRequests && this.mApnSetting.isMetered(this.mPhone)) {
            this.mRestrictedNetworkOverride = this.mDct.isDataEnabled() ^ 1;
            if (!this.mRestrictedNetworkOverride) {
                z = false;
            } else if (this.mDct != null) {
                z = this.mDct.haveVsimIgnoreUserDataSetting() ^ 1;
            }
            this.mRestrictedNetworkOverride = z;
        }
    }

    NetworkCapabilities getNetworkCapabilities() {
        boolean isDataEnable;
        NetworkCapabilities result = new NetworkCapabilities();
        result.addTransportType(0);
        try {
            isDataEnable = !(this.mDct != null ? this.mDct.getDataEnabled() : true) ? this.mDct != null ? this.mDct.haveVsimIgnoreUserDataSetting() : false : true;
            log("makeNetworkCapabilities: check data enable:" + isDataEnable);
        } catch (Exception e) {
            isDataEnable = true;
            e.printStackTrace();
        }
        if (this.mApnSetting != null) {
            ApnSetting securedDunApn = this.mDct.fetchDunApn();
            for (String type : this.mApnSetting.types) {
                if (!this.mRestrictedNetworkOverride && this.mConnectionParams != null && this.mConnectionParams.mUnmeteredUseOnly && ApnSetting.isMeteredApnType(type, this.mPhone)) {
                    log("Dropped the metered " + type + " for the unmetered data call.");
                } else if (type.equals(CharacterSets.MIMENAME_ANY_CHARSET)) {
                    if (isDataEnable) {
                        result.addCapability(12);
                    }
                    result.addCapability(0);
                    result.addCapability(1);
                    result.addCapability(3);
                    result.addCapability(4);
                    result.addCapability(5);
                    result.addCapability(7);
                    if (this.mApnSetting.equals(securedDunApn)) {
                        result.addCapability(2);
                    }
                } else if (type.equals("default")) {
                    if (isDataEnable) {
                        result.addCapability(12);
                    }
                } else if (type.equals("mms")) {
                    result.addCapability(0);
                } else if (type.equals("supl")) {
                    result.addCapability(1);
                } else if (type.equals("dun")) {
                    if (securedDunApn == null || securedDunApn.equals(this.mApnSetting)) {
                        result.addCapability(2);
                    }
                } else if (type.equals("fota")) {
                    result.addCapability(3);
                } else if (type.equals("ims")) {
                    result.addCapability(4);
                } else if (type.equals("cbs")) {
                    result.addCapability(5);
                } else if (type.equals("ia")) {
                    result.addCapability(7);
                } else if (type.equals("emergency")) {
                    result.addCapability(10);
                }
            }
            if ((this.mConnectionParams == null || !this.mConnectionParams.mUnmeteredUseOnly || (this.mRestrictedNetworkOverride ^ 1) == 0) && (this.mApnSetting.isMetered(this.mPhone) ^ 1) == 0) {
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
        switch (this.mRilRat) {
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
                up = RadioAccessFamily.HSPA;
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
            case 19:
                up = 51200;
                down = 102400;
                break;
        }
        result.setLinkUpstreamBandwidthKbps(up);
        result.setLinkDownstreamBandwidthKbps(down);
        result.setNetworkSpecifier(new StringNetworkSpecifier(Integer.toString(this.mPhone.getSubId())));
        return result;
    }

    private boolean isIpAddress(String address) {
        if (address == null) {
            return false;
        }
        return Patterns.IP_ADDRESS.matcher(address).matches();
    }

    private SetupResult setLinkProperties(DataCallResponse response, LinkProperties lp) {
        String propertyPrefix = "net." + response.ifname + ".";
        return response.setLinkProperties(lp, isDnsOk(new String[]{SystemProperties.get(propertyPrefix + "dns1"), SystemProperties.get(propertyPrefix + "dns2")}));
    }

    private boolean initConnection(ConnectionParams cp) {
        ApnContext apnContext = cp.mApnContext;
        if (this.mApnSetting == null) {
            this.mApnSetting = apnContext.getApnSetting();
        }
        if (this.mApnSetting == null || (this.mApnSetting.canHandleType(apnContext.getApnType()) ^ 1) != 0) {
            log("initConnection: incompatible apnSetting in ConnectionParams cp=" + cp + " dc=" + this);
            return false;
        }
        this.mTag++;
        this.mConnectionParams = cp;
        this.mConnectionParams.mTag = this.mTag;
        this.mApnContexts.put(apnContext, cp);
        log("initConnection:  RefCount=" + this.mApnContexts.size() + " mApnList=" + this.mApnContexts + " mConnectionParams=" + this.mConnectionParams);
        return true;
    }

    private boolean updateNetworkInfoSuspendState() {
        boolean z = true;
        DetailedState oldState = this.mNetworkInfo.getDetailedState();
        if (this.mNetworkAgent == null) {
            Rlog.e(getName(), "Setting suspend state without a NetworkAgent");
        }
        ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
        if (sst.getCurrentDataConnectionState() != 0) {
            this.mNetworkInfo.setDetailedState(DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
        } else if (sst.isConcurrentVoiceAndDataAllowed() || this.mPhone.getCallTracker().getState() == PhoneConstants.State.IDLE) {
            this.mNetworkInfo.setDetailedState(DetailedState.CONNECTED, null, this.mNetworkInfo.getExtraInfo());
        } else {
            this.mNetworkInfo.setDetailedState(DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
            if (oldState == DetailedState.SUSPENDED) {
                z = false;
            }
            return z;
        }
        if (oldState == this.mNetworkInfo.getDetailedState()) {
            z = false;
        }
        return z;
    }

    public final boolean hasMessages(int what, Object object) {
        return getHandler().hasMessages(what, object);
    }

    void tearDownNow() {
        log("tearDownNow()");
        sendMessage(obtainMessage(EVENT_TEAR_DOWN_NOW));
    }

    private long getSuggestedRetryDelay(AsyncResult ar) {
        DataCallResponse response = ar.result;
        if (response.suggestedRetryTime < 0) {
            log("No suggested retry delay.");
            return -2;
        } else if (response.suggestedRetryTime != Integer.MAX_VALUE) {
            return (long) response.suggestedRetryTime;
        } else {
            log("Modem suggested not retrying.");
            return -1;
        }
    }

    protected String getWhatToString(int what) {
        return cmdToString(what);
    }

    private static String msgToString(Message msg) {
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

    protected void log(String s) {
        Rlog.d(getName(), s);
    }

    protected void logd(String s) {
        Rlog.d(getName(), s);
    }

    protected void logv(String s) {
        Rlog.v(getName(), s);
    }

    protected void logi(String s) {
        Rlog.i(getName(), s);
    }

    protected void logw(String s) {
        Rlog.w(getName(), s);
    }

    protected void loge(String s) {
        Rlog.e(getName(), s);
    }

    protected void loge(String s, Throwable e) {
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

    private void dumpToLog() {
        dump(null, new PrintWriter(new StringWriter(0)) {
            public void println(String s) {
                DataConnection.this.logd(s);
            }

            public void flush() {
            }
        }, null);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("DataConnection ");
        super.dump(fd, pw, args);
        pw.println(" mApnContexts.size=" + this.mApnContexts.size());
        pw.println(" mApnContexts=" + this.mApnContexts);
        pw.flush();
        pw.println(" mDataConnectionTracker=" + this.mDct);
        pw.println(" mApnSetting=" + this.mApnSetting);
        pw.println(" mTag=" + this.mTag);
        pw.println(" mCid=" + this.mCid);
        pw.println(" mConnectionParams=" + this.mConnectionParams);
        pw.println(" mDisconnectParams=" + this.mDisconnectParams);
        pw.println(" mDcFailCause=" + this.mDcFailCause);
        pw.flush();
        pw.println(" mPhone=" + this.mPhone);
        pw.flush();
        pw.println(" mLinkProperties=" + this.mLinkProperties);
        pw.flush();
        pw.println(" mDataRegState=" + this.mDataRegState);
        pw.println(" mRilRat=" + this.mRilRat);
        pw.println(" mNetworkCapabilities=" + getNetworkCapabilities());
        pw.println(" mCreateTime=" + TimeUtils.logTimeOfDay(this.mCreateTime));
        pw.println(" mLastFailTime=" + TimeUtils.logTimeOfDay(this.mLastFailTime));
        pw.println(" mLastFailCause=" + this.mLastFailCause);
        pw.flush();
        pw.println(" mUserData=" + this.mUserData);
        pw.println(" mInstanceNumber=" + mInstanceNumber);
        pw.println(" mAc=" + this.mAc);
        pw.flush();
    }

    private long resetDelayTimeForAuTelstraOperator(long delay) {
        boolean isAuTelstraOperator = false;
        String operatorNumeric = TelephonyManager.getDefault().getNetworkOperatorForPhone(this.mPhone.getPhoneId());
        log("DcActivatingState: operatorNumeric = " + operatorNumeric);
        if (operatorNumeric != null) {
            for (String startsWith : sAuTelstraOperator) {
                if (operatorNumeric.startsWith(startsWith)) {
                    isAuTelstraOperator = true;
                    log("isAuTelstraOperator");
                    break;
                }
            }
        }
        if (isAuTelstraOperator) {
            return 720000;
        }
        return delay;
    }
}

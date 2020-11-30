package com.mediatek.internal.telephony.imsphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.NetworkStats;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telecom.ConferenceParticipant;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.ImsSuppServiceNotification;
import android.telephony.ims.feature.ImsFeature;
import android.text.TextUtils;
import com.android.ims.ImsCall;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsUtInterface;
import com.android.ims.internal.IImsCallSession;
import com.android.internal.os.SomeArgs;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.SettingsObserver;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCall;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.imsphone.ImsPhoneConnection;
import com.android.internal.telephony.imsphone.ImsPullCall;
import com.mediatek.android.mms.pdu.MtkCharacterSets;
import com.mediatek.ims.MtkImsCall;
import com.mediatek.ims.MtkImsConnectionStateListener;
import com.mediatek.ims.internal.MtkImsManager;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.MtkIncomingCallChecker;
import com.mediatek.internal.telephony.OpTelephonyCustomizationUtils;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.digits.DigitsUtil;
import com.mediatek.telephony.internal.telephony.vsim.ExternalSimConstants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mediatek.telecom.FormattedLog;

public class MtkImsPhoneCallTracker extends ImsPhoneCallTracker implements ImsPullCall {
    private static final int EVENT_RESUME_BACKROUND_CALL = 104;
    private static final int EVENT_RETRY_DATA_ENABLED_CHANGED = 105;
    private static final int EVENT_ROAMING_OFF = 102;
    private static final int EVENT_ROAMING_ON = 101;
    private static final int EVENT_ROAMING_SETTING_CHANGE = 103;
    private static final int IMS_RTT_CALL_TYPE_CS = 0;
    private static final int IMS_RTT_CALL_TYPE_CS_NO_TTY = 3;
    private static final int IMS_RTT_CALL_TYPE_PS = 2;
    private static final int IMS_RTT_CALL_TYPE_RTT = 1;
    public static final int IMS_SESSION_MODIFY_OPERATION_FLAG = 32768;
    private static final int IMS_VIDEO_CALL = 21;
    private static final int IMS_VIDEO_CONF = 23;
    private static final int IMS_VIDEO_CONF_PARTS = 25;
    private static final int IMS_VOICE_CALL = 20;
    private static final int IMS_VOICE_CONF = 22;
    private static final int IMS_VOICE_CONF_PARTS = 24;
    private static final int INVALID_CALL_MODE = 255;
    static final String LOG_TAG = "MtkImsPhoneCallTracker";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER);
    private static final boolean TELDBG;
    private RegistrantList mCallsDisconnectedDuringSrvccRegistrants = new RegistrantList();
    private boolean mCarrierSwitchWfcModeRequired = false;
    private boolean mDialAsECC = false;
    private DigitsUtil mDigitsUtil = OpTelephonyCustomizationUtils.getOpFactory(this.mPhone.getContext()).makeDigitsUtil();
    private boolean mIgnoreDataRoaming = false;
    protected BroadcastReceiver mImsBaseReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.imsphone.MtkImsPhoneCallTracker.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.mediatek.ims.MTK_IMS_SERVICE_UP")) {
                try {
                    MtkImsManager imsMgr = ImsManager.getInstance(MtkImsPhoneCallTracker.this.mPhone.getContext(), MtkImsPhoneCallTracker.this.mPhone.getPhoneId());
                    imsMgr.removeImsConnectionStateListener(MtkImsPhoneCallTracker.this.mImsStateListener);
                    imsMgr.addImsConnectionStateListener(MtkImsPhoneCallTracker.this.mImsStateListener);
                    MtkImsPhoneCallTracker mtkImsPhoneCallTracker = MtkImsPhoneCallTracker.this;
                    mtkImsPhoneCallTracker.loge("ACTION_MTK_IMS_SERVICE_UP: register ims succeed, " + MtkImsPhoneCallTracker.this.mImsStateListener);
                } catch (ImsException e) {
                    MtkImsPhoneCallTracker.this.loge("ACTION_MTK_IMS_SERVICE_UP: register ims fail!");
                }
            }
        }
    };
    private int mImsRegistrationErrorCode;
    private int mImsRttCallType = 2;
    private MtkImsConnectionStateListener mImsStateListener = new MtkImsConnectionStateListener() {
        /* class com.mediatek.internal.telephony.imsphone.MtkImsPhoneCallTracker.AnonymousClass2 */

        public void onImsEmergencyCapabilityChanged(boolean eccSupport) {
            MtkImsPhoneCallTracker mtkImsPhoneCallTracker = MtkImsPhoneCallTracker.this;
            mtkImsPhoneCallTracker.log("onImsEmergencyCapabilityChanged: " + eccSupport);
            MtkImsPhoneCallTracker.this.mPhone.onFeatureCapabilityChanged();
            MtkImsPhoneCallTracker.this.mIsImsEccSupported = eccSupport;
            MtkImsPhoneCallTracker.this.mPhone.updateIsEmergencyOnly();
        }

        public void onWifiPdnOOSStateChanged(int oosState) {
            MtkImsPhoneCallTracker mtkImsPhoneCallTracker = MtkImsPhoneCallTracker.this;
            mtkImsPhoneCallTracker.log("onWifiPdnOOSStateChanged: " + oosState);
            MtkImsPhoneCallTracker.this.mWifiPdnOOSState = oosState;
        }

        public void onCapabilitiesStatusChanged(ImsFeature.Capabilities capabilities) {
            MtkImsPhoneCallTracker mtkImsPhoneCallTracker = MtkImsPhoneCallTracker.this;
            mtkImsPhoneCallTracker.log("onCapabilitiesStatusChanged: " + capabilities);
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = capabilities;
            MtkImsPhoneCallTracker.this.removeMessages(26);
            MtkImsPhoneCallTracker.this.obtainMessage(26, args).sendToTarget();
        }
    };
    private MtkIncomingCallChecker mIncomingCallCheker = null;
    private IncomingCallEventRecevier mIndicationReceiver = new IncomingCallEventRecevier();
    private boolean mIsDataRoaming = false;
    private boolean mIsDataRoamingSettingEnabled = false;
    private boolean mIsImsEccSupported = false;
    private boolean mIsOnCallResumed = false;
    private boolean mIsRttCallMergeSupported = false;
    private int mLastDataEnabledReason;
    private ImsCall.Listener mMtkImsCallListener = new MtkImsCall.Listener() {
        /* class com.mediatek.internal.telephony.imsphone.MtkImsPhoneCallTracker.AnonymousClass3 */

        public void onCallProgressing(ImsCall imsCall) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallProgressing(imsCall);
            ImsPhoneConnection conn = MtkImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                conn.onConnectionEvent("mediatek.telecom.event.EVENT_CALL_ALERTING_NOTIFICATION", (Bundle) null);
            }
        }

        public void onCallStarted(ImsCall imsCall) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallStarted(imsCall);
        }

        public void onCallUpdated(ImsCall imsCall) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallUpdated(imsCall);
        }

        public void onCallStartFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallStartFailed(imsCall, reasonInfo);
            if (MtkImsPhoneCallTracker.this.mBackgroundCall.getState() == Call.State.HOLDING) {
                MtkImsPhoneCallTracker.this.log("auto resume holding call");
                MtkImsPhoneCallTracker.this.sendEmptyMessage(104);
            }
        }

        public void onCallTerminated(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallTerminated(imsCall, reasonInfo);
        }

        public void onCallHeld(ImsCall imsCall) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallHeld(imsCall);
        }

        public void onCallHoldFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallHoldFailed(imsCall, reasonInfo);
        }

        public void onCallResumed(ImsCall imsCall) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallResumed(imsCall);
            if (MtkImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.ACTIVE && MtkImsPhoneCallTracker.this.mPendingMO != null && MtkImsPhoneCallTracker.this.mPendingMO.getState() != Call.State.DISCONNECTING) {
                MtkImsPhoneCallTracker.this.log("onCallResumed : dialPendingMO");
                MtkImsPhoneCallTracker.this.dialPendingMO();
            }
        }

        public void onCallResumeFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            MtkImsPhoneCallTracker mtkImsPhoneCallTracker = MtkImsPhoneCallTracker.this;
            mtkImsPhoneCallTracker.log("onCallResumeFailed reasonCode=" + reasonInfo.getCode());
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallResumeFailed(imsCall, reasonInfo);
        }

        public void onCallResumeReceived(ImsCall imsCall) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallResumeReceived(imsCall);
        }

        public void onCallHoldReceived(ImsCall imsCall) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallHoldReceived(imsCall);
        }

        public void onCallSuppServiceReceived(ImsCall call, ImsSuppServiceNotification suppServiceInfo) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallSuppServiceReceived(call, suppServiceInfo);
        }

        public void onCallMerged(ImsCall call, ImsCall peerCall, boolean swapCalls) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallMerged(call, peerCall, swapCalls);
            ImsPhoneConnection hostConn = MtkImsPhoneCallTracker.this.findConnection(call);
            if (hostConn != null && (hostConn instanceof MtkImsPhoneConnection)) {
                MtkImsPhoneConnection hostConnExt = (MtkImsPhoneConnection) hostConn;
                FormattedLog formattedLog = new FormattedLog.Builder().setCategory("CC").setServiceName("ImsPhone").setOpType(FormattedLog.OpType.DUMP).setCallNumber(MtkImsPhoneCallTracker.sensitiveEncode(hostConn.getAddress())).setCallId(MtkImsPhoneCallTracker.this.getConnectionCallId(hostConnExt)).setStatusInfo("state", "disconnected").setStatusInfo("isConfCall", "No").setStatusInfo("isConfChildCall", "No").setStatusInfo("parent", hostConnExt.getParentCallName()).buildDumpInfo();
                if (formattedLog == null) {
                    return;
                }
                if (!MtkImsPhoneCallTracker.SENLOG || MtkImsPhoneCallTracker.TELDBG) {
                    MtkImsPhoneCallTracker.this.log(formattedLog.toString());
                }
            }
        }

        public void onCallMergeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallMergeFailed(call, reasonInfo);
        }

        public void onConferenceParticipantsStateChanged(ImsCall call, List<ConferenceParticipant> participants) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onConferenceParticipantsStateChanged(call, participants);
        }

        public void onCallSessionTtyModeReceived(ImsCall call, int mode) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallSessionTtyModeReceived(call, mode);
        }

        public void onCallHandover(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallHandover(imsCall, srcAccessTech, targetAccessTech, reasonInfo);
        }

        public void onCallHandoverFailed(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onCallHandoverFailed(imsCall, srcAccessTech, targetAccessTech, reasonInfo);
        }

        public void onRttModifyRequestReceived(ImsCall imsCall) {
            MtkImsPhoneCallTracker.this.log("onRttModifyRequestReceived");
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onRttModifyRequestReceived(imsCall);
        }

        public void onRttModifyResponseReceived(ImsCall imsCall, int status) {
            MtkImsPhoneCallTracker.this.log("onRttModifyResponseReceived");
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onRttModifyResponseReceived(imsCall, status);
        }

        public void onRttMessageReceived(ImsCall imsCall, String message) {
            MtkImsPhoneCallTracker.this.log("onRttMessageReceived");
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onRttMessageReceived(imsCall, message);
        }

        public void onRttAudioIndicatorChanged(ImsCall imsCall, ImsStreamMediaProfile profile) {
            MtkImsPhoneCallTracker.this.log("onRttAudioIndicatorChanged");
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onRttAudioIndicatorChanged(imsCall, profile);
        }

        public void onMultipartyStateChanged(ImsCall imsCall, boolean isMultiParty) {
            ((ImsPhoneCallTracker) MtkImsPhoneCallTracker.this).mImsCallListener.onMultipartyStateChanged(imsCall, isMultiParty);
        }

        public void onCallInviteParticipantsRequestDelivered(ImsCall call) {
            MtkImsPhoneCallTracker.this.log("onCallInviteParticipantsRequestDelivered");
            ImsPhoneConnection conn = MtkImsPhoneCallTracker.this.findConnection(call);
            if (conn != null && (conn instanceof MtkImsPhoneConnection)) {
                ((MtkImsPhoneConnection) conn).notifyConferenceParticipantsInvited(true);
            }
        }

        public void onCallInviteParticipantsRequestFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            MtkImsPhoneCallTracker mtkImsPhoneCallTracker = MtkImsPhoneCallTracker.this;
            mtkImsPhoneCallTracker.log("onCallInviteParticipantsRequestFailed reasonCode=" + reasonInfo.getCode());
            ImsPhoneConnection conn = MtkImsPhoneCallTracker.this.findConnection(call);
            if (conn != null && (conn instanceof MtkImsPhoneConnection)) {
                ((MtkImsPhoneConnection) conn).notifyConferenceParticipantsInvited(false);
            }
        }

        public void onCallTransferred(ImsCall imsCall) {
            MtkImsPhoneCallTracker.this.log("onCallTransferred");
        }

        public void onCallTransferFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            MtkImsPhoneCallTracker.this.log("onCallTransferFailed");
            MtkImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(PhoneInternalInterface.SuppService.TRANSFER);
        }

        public void onTextCapabilityChanged(ImsCall call, int localCapability, int remoteCapability, int localTextStatus, int realRemoteCapability) {
            ImsPhoneConnection conn = MtkImsPhoneCallTracker.this.findConnection(call);
            boolean rttSupportRemote = false;
            boolean rttStatusLocal = localTextStatus == 1;
            boolean rttStatusRemote = remoteCapability == 1;
            boolean rttSupportLocal = localCapability == 1;
            if (realRemoteCapability == 1) {
                rttSupportRemote = true;
            }
            MtkImsPhoneCallTracker.this.log("onTextCapabilityChanged localCapability: " + localCapability + " remote status: " + remoteCapability + " localTextStatus" + localTextStatus + " RemoteCapability: " + realRemoteCapability);
            Bundle bundle = new Bundle();
            bundle.putBoolean("mediatek.telecom.extra.RTT_STATUS_LOCAL", rttStatusLocal);
            bundle.putBoolean("mediatek.telecom.extra.RTT_STATUS_REMOTE", rttStatusRemote);
            bundle.putBoolean("mediatek.telecom.extra.RTT_SUPPORT_LOCAL", rttSupportLocal);
            bundle.putBoolean("mediatek.telecom.extra.RTT_SUPPORT_REMOTE", rttSupportRemote);
            if (conn != null) {
                conn.onConnectionEvent("mediatek.telecom.event.RTT_SUPPORT_CHANGED", bundle);
                MtkImsPhoneCallTracker.this.log("onTextCapabilityChanged update to conn");
            }
        }

        public void onRttEventReceived(ImsCall call, int event) {
            ImsPhoneConnection conn = MtkImsPhoneCallTracker.this.findConnection(call);
            if (conn != null) {
                conn.onConnectionEvent("mediatek.telecom.event.EVENT_RTT_EMERGENCY_REDIAL", (Bundle) null);
            }
        }

        public void onCallDeviceSwitched(ImsCall call) {
            MtkImsPhoneCallTracker.this.log("onCallDeviceSwitched");
            ImsPhoneConnection conn = MtkImsPhoneCallTracker.this.findConnection(call);
            if (conn != null && (conn instanceof MtkImsPhoneConnection)) {
                ((MtkImsPhoneConnection) conn).notifyDeviceSwitched(true);
            }
        }

        public void onCallDeviceSwitchFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            MtkImsPhoneCallTracker.this.log("onCallDeviceSwitchFailed");
            ImsPhoneConnection conn = MtkImsPhoneCallTracker.this.findConnection(call);
            if (conn != null && (conn instanceof MtkImsPhoneConnection)) {
                ((MtkImsPhoneConnection) conn).notifyDeviceSwitched(false);
            }
        }

        public void onCallRedialEcc(ImsCall call, boolean isNeedUserConfirm) {
            MtkImsPhoneCallTracker.this.log("onCallRedialEcc");
            ImsPhoneConnection conn = MtkImsPhoneCallTracker.this.findConnection(call);
            if (conn != null && (conn instanceof MtkImsPhoneConnection)) {
                ((MtkImsPhoneConnection) conn).notifyRedialEcc(isNeedUserConfirm);
            }
        }
    };
    protected final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangedListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.mediatek.internal.telephony.imsphone.MtkImsPhoneCallTracker.AnonymousClass1 */

        public void onSubscriptionsChanged() {
            MtkImsPhoneCallTracker mtkImsPhoneCallTracker = MtkImsPhoneCallTracker.this;
            mtkImsPhoneCallTracker.log("SubscriptionListener.onSubscriptionInfoChanged, subId=" + MtkImsPhoneCallTracker.this.mPhone.getSubId());
            if (SubscriptionManager.isValidSubscriptionId(MtkImsPhoneCallTracker.this.mPhone.getSubId())) {
                if (!MtkImsPhoneCallTracker.this.mRoamingVariablesInited) {
                    MtkImsPhoneCallTracker.this.mRoamingVariablesInited = true;
                    MtkImsPhoneCallTracker.this.initRoamingAndRoamingSetting();
                }
                MtkImsPhoneCallTracker.this.registerSettingsObserver();
            }
        }
    };
    private boolean mRoamingVariablesInited = false;
    private RttEmcGuardTimerUtil mRttEmcGuardTimerUtil = new RttEmcGuardTimerUtil(this.mPhone.getContext());
    private final SettingsObserver mSettingsObserver;
    private SubscriptionManager mSubscriptionManager;
    private int mWifiPdnOOSState = 2;

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    public boolean isSupportImsEcc() {
        return this.mIsImsEccSupported;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: com.mediatek.internal.telephony.imsphone.MtkImsPhoneCallTracker */
    /* JADX WARN: Multi-variable type inference failed */
    public MtkImsPhoneCallTracker(ImsPhone phone) {
        super(phone);
        this.mRingingCall = new MtkImsPhoneCall(this, "RG");
        this.mForegroundCall = new MtkImsPhoneCall(this, "FG");
        this.mBackgroundCall = new MtkImsPhoneCall(this, "BG");
        this.mHandoverCall = new MtkImsPhoneCall(this, "HO");
        registerIndicationReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("com.mediatek.ims.MTK_IMS_SERVICE_UP");
        this.mPhone.getContext().registerReceiver(this.mImsBaseReceiver, intentfilter);
        this.mSettingsObserver = new SettingsObserver(this.mPhone.getContext(), this);
        registerSettingsObserver();
        this.mSubscriptionManager = SubscriptionManager.from(this.mPhone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mPhone.getDefaultPhone().getServiceStateTracker().registerForDataRoamingOn(this, 101, (Object) null);
        this.mPhone.getDefaultPhone().getServiceStateTracker().registerForDataRoamingOff(this, 102, (Object) null, true);
        this.mRttEmcGuardTimerUtil.initRttEmcGuardTimer();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.mediatek.internal.telephony.imsphone.MtkImsPhoneCallTracker */
    /* JADX WARN: Multi-variable type inference failed */
    public void dispose() {
        log("dispose");
        this.mRingingCall.dispose();
        this.mBackgroundCall.dispose();
        this.mForegroundCall.dispose();
        this.mHandoverCall.dispose();
        clearDisconnected();
        if (this.mUtInterface != null) {
            this.mUtInterface.unregisterForSuppServiceIndication(this);
        }
        this.mPhone.getContext().unregisterReceiver(this.mReceiver);
        this.mPhone.getContext().unregisterReceiver(this.mImsBaseReceiver);
        unregisterIndicationReceiver();
        this.mPhone.setServiceState(1);
        this.mPhone.setImsRegistered(false);
        resetImsCapabilities();
        this.mPhone.onFeatureCapabilityChanged();
        this.mPhone.getDefaultPhone().getDataEnabledSettings().unregisterForDataEnabledChanged(this);
        this.mImsManagerConnector.disconnect();
        this.mPhone.getDefaultPhone().getServiceStateTracker().unregisterForDataRoamingOn(this);
        this.mPhone.getDefaultPhone().getServiceStateTracker().unregisterForDataRoamingOff(this);
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mSettingsObserver.unobserve();
        this.mRttEmcGuardTimerUtil.disposeRttEmcGuardTimer();
        checkRttCallType();
        if (this.mImsManager != null) {
            try {
                this.mImsManager.removeImsConnectionStateListener(this.mImsStateListener);
            } catch (ImsException e) {
                loge("dispose() : removeRegistrationListener failed: " + e);
            }
        }
    }

    public synchronized Connection dial(String dialString, ImsPhone.ImsDialArgs dialArgs) throws CallStateException {
        if (this.mSrvccState == Call.SrvccState.STARTED || this.mSrvccState == Call.SrvccState.COMPLETED) {
            throw new CallStateException(3, "cannot dial call: SRVCC");
        }
        return MtkImsPhoneCallTracker.super.dial(dialString, dialArgs);
    }

    /* access modifiers changed from: protected */
    public void dialInternal(ImsPhoneConnection conn, int clirMode, int videoState, Bundle intentExtras) {
        int serviceType;
        ImsException e;
        String[] callees;
        if (conn != null) {
            boolean isOneKeyConf = (conn instanceof MtkImsPhoneConnection) && ((MtkImsPhoneConnection) conn).getConfDialStrings() != null;
            if (isOneKeyConf || !(conn.getAddress() == null || conn.getAddress().length() == 0 || conn.getAddress().indexOf(78) >= 0)) {
                setMute(false);
                int serviceType2 = isEmergencyNumber(conn.getAddress()) ? 2 : 1;
                if (this.mDialAsECC) {
                    serviceType2 = 2;
                    log("Dial as ECC: conn.getAddress(): " + conn.getAddress());
                    this.mDialAsECC = false;
                }
                String region = SystemProperties.get("persist.sys.oppo.region", "CN");
                String mccMnc = TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "gsm.sim.operator.numeric", "");
                if (!region.equalsIgnoreCase("MY") || !mccMnc.equalsIgnoreCase("502153") || this.mPhone.getServiceState().getState() != 0) {
                    serviceType = serviceType2;
                } else {
                    serviceType = 1;
                }
                int callType = ImsCallProfile.getCallTypeFromVideoState(videoState);
                conn.setVideoState(videoState);
                if (isOneKeyConf) {
                    try {
                        ArrayList<String> dialStrings = ((MtkImsPhoneConnection) conn).getConfDialStrings();
                        callees = (String[]) dialStrings.toArray(new String[dialStrings.size()]);
                    } catch (ImsException e2) {
                        e = e2;
                        loge("dialInternal : " + e);
                        conn.setDisconnectCause(36);
                        sendEmptyMessageDelayed(18, 500);
                        retryGetImsService();
                    } catch (RemoteException e3) {
                        return;
                    }
                } else {
                    callees = new String[]{conn.getAddress()};
                }
                ImsCallProfile profile = this.mImsManager.createCallProfile(serviceType, callType);
                try {
                    profile.setCallExtraInt("oir", clirMode);
                    if (isOneKeyConf) {
                        profile.setCallExtraBoolean("conference", true);
                    }
                    if (intentExtras != null) {
                        if (intentExtras.containsKey("android.telecom.extra.CALL_SUBJECT")) {
                            intentExtras.putString("DisplayText", cleanseInstantLetteringMessage(intentExtras.getString("android.telecom.extra.CALL_SUBJECT")));
                        }
                        if (conn.hasRttTextStream()) {
                            profile.mMediaProfile.mRttMode = 1;
                        }
                        if (intentExtras.containsKey("CallPull")) {
                            profile.mCallExtras.putBoolean("CallPull", intentExtras.getBoolean("CallPull"));
                            int dialogId = intentExtras.getInt("android.telephony.ImsExternalCallTracker.extra.EXTERNAL_CALL_ID");
                            conn.setIsPulledCall(true);
                            conn.setPulledDialogId(dialogId);
                        }
                        profile.mCallExtras.putBundle("OemCallExtras", intentExtras);
                        this.mDigitsUtil.putDialFrom(intentExtras, profile);
                    }
                    if (callees != null && callees.length == 1 && !profile.getCallExtraBoolean("conference")) {
                        profile.setCallExtra("oi", callees[0]);
                    }
                    synchronized (this) {
                        ImsCall imsCall = this.mImsManager.makeCall(profile, callees, this.mMtkImsCallListener);
                        conn.setImsCall(imsCall);
                        this.mMetrics.writeOnImsCallStart(this.mPhone.getPhoneId(), imsCall.getSession());
                        setVideoCallProvider(conn, imsCall);
                        conn.setAllowAddCallDuringVideoCall(this.mAllowAddCallDuringVideoCall);
                    }
                } catch (ImsException e4) {
                    e = e4;
                    loge("dialInternal : " + e);
                    conn.setDisconnectCause(36);
                    sendEmptyMessageDelayed(18, 500);
                    retryGetImsService();
                } catch (RemoteException e5) {
                }
            } else {
                conn.setDisconnectCause(7);
                sendEmptyMessageDelayed(18, 500);
            }
        }
    }

    public void acceptCall(int videoState) throws CallStateException {
        if (this.mSrvccState == Call.SrvccState.STARTED || this.mSrvccState == Call.SrvccState.COMPLETED) {
            throw new CallStateException(3, "cannot accept call: SRVCC");
        }
        int videoStateAfterCheckingData = videoState;
        if (!isDataAvailableForViLTE() && !this.mRingingCall.getImsCall().isWifiCall()) {
            videoStateAfterCheckingData = 0;
            log("Data is off, answer as voice call");
        }
        logDebugMessagesWithOpFormat("CC", "Answer", this.mRingingCall.getFirstConnection(), "");
        MtkImsPhoneCallTracker.super.acceptCall(videoStateAfterCheckingData);
    }

    public void rejectCall() throws CallStateException {
        logDebugMessagesWithOpFormat("CC", "Reject", this.mRingingCall.getFirstConnection(), "");
        MtkImsPhoneCallTracker.super.rejectCall();
    }

    public void conference() {
        ImsPhoneConnection firstConnection = this.mForegroundCall.getFirstConnection();
        logDebugMessagesWithOpFormat("CC", "Conference", firstConnection, " merge with " + this.mBackgroundCall.getFirstConnection());
        if (this.mHoldSwitchingState == ImsPhoneCallTracker.HoldSwapState.SWAPPING_ACTIVE_AND_HELD) {
            log("Can't merge during swap call.");
        } else if (!isRttCallInvolved(this.mForegroundCall.getImsCall(), this.mBackgroundCall.getImsCall()) || isRttCallMergeSupported()) {
            MtkImsPhoneCallTracker.super.conference();
        }
    }

    private boolean isRttCallInvolved(ImsCall fgImsCall, ImsCall bgImsCall) {
        boolean ret = false;
        if (isRttCall(fgImsCall) || isRttCall(bgImsCall)) {
            ret = true;
        }
        log("isRttCallInvolved: " + ret);
        return ret;
    }

    private boolean isRttCall(ImsCall call) {
        if (call != null) {
            return call.getCallProfile().mMediaProfile.isRttCall();
        }
        return false;
    }

    private boolean isRttCallMergeSupported() {
        log("isRttCallMergeSupported: " + this.mIsRttCallMergeSupported);
        return this.mIsRttCallMergeSupported;
    }

    public void explicitCallTransfer() {
        log("explicitCallTransfer");
        ImsCall fgImsCall = this.mForegroundCall.getImsCall();
        if (fgImsCall == null) {
            log("explicitCallTransfer no foreground ims call");
        } else if (this.mBackgroundCall.getImsCall() == null) {
            log("explicitCallTransfer no background ims call");
        } else if (this.mForegroundCall.getState() == Call.State.ACTIVE && this.mBackgroundCall.getState() == Call.State.HOLDING) {
            try {
                ((MtkImsCall) fgImsCall).explicitCallTransfer();
            } catch (ImsException e) {
                log("explicitCallTransfer " + e.getMessage());
            }
        } else {
            log("annot transfer call");
        }
    }

    public void unattendedCallTransfer(String number, int type) {
        log("unattendedCallTransfer number : " + sensitiveEncode(number) + ", type : " + type);
        ImsCall fgImsCall = this.mForegroundCall.getImsCall();
        if (fgImsCall == null) {
            log("explicitCallTransfer no foreground ims call");
            return;
        }
        try {
            ((MtkImsCall) fgImsCall).unattendedCallTransfer(number, type);
        } catch (ImsException e) {
            log("explicitCallTransfer " + e.getMessage());
        }
    }

    public void deviceSwitch(String number, String deviceId) {
        log("deviceSwitch number : " + sensitiveEncode(number) + ", deviceId : " + deviceId);
        ImsCall fgImsCall = this.mForegroundCall.getImsCall();
        if (fgImsCall == null) {
            log("deviceSwitch no foreground ims call");
            return;
        }
        try {
            ((MtkImsCall) fgImsCall).deviceSwitch(number, deviceId);
        } catch (ImsException e) {
            log("deviceSwitch " + e.getMessage());
        }
    }

    public void cancelDeviceSwitch() {
        log("cancelDeviceSwitch");
        ImsCall fgImsCall = this.mForegroundCall.getImsCall();
        if (fgImsCall == null) {
            log("cancelDeviceSwitch no foreground ims call");
            return;
        }
        try {
            ((MtkImsCall) fgImsCall).cancelDeviceSwitch();
        } catch (ImsException e) {
            log("cancelDeviceSwitch " + e.getMessage());
        }
    }

    public void checkForDialIssues() throws CallStateException {
        if (this.mPhone == null || !(this.mPhone.getDefaultPhone() instanceof MtkGsmCdmaPhone) || !this.mPhone.getDefaultPhone().shouldProcessSelfActivation()) {
            MtkImsPhoneCallTracker.super.checkForDialIssues();
        } else {
            log("IMS: checkForDialIssues(), bypass checkForDialIssues for self activation");
        }
    }

    public void hangup(ImsPhoneCall call) throws CallStateException {
        if (this.mSrvccState == Call.SrvccState.STARTED || this.mSrvccState == Call.SrvccState.COMPLETED) {
            throw new CallStateException(3, "cannot hangup call: SRVCC");
        }
        MtkImsPhoneCallTracker.super.hangup(call);
    }

    public void hangup(ImsPhoneCall call, int reason) throws CallStateException {
        log("hangup call with reason: " + reason);
        if (this.mSrvccState == Call.SrvccState.STARTED || this.mSrvccState == Call.SrvccState.COMPLETED) {
            throw new CallStateException(3, "cannot hangup call: SRVCC");
        } else if (call.getConnections().size() != 0) {
            ImsCall imsCall = call.getImsCall();
            boolean rejectCall = false;
            if (call == this.mRingingCall) {
                log("(ringing) hangup incoming");
                rejectCall = true;
            } else if (call == this.mForegroundCall) {
                if (call.isDialingOrAlerting()) {
                    log("(foregnd) hangup dialing or alerting...");
                } else {
                    log("(foregnd) hangup foreground");
                }
            } else if (call == this.mBackgroundCall) {
                log("(backgnd) hangup waiting or background");
            } else {
                throw new CallStateException("ImsPhoneCall " + call + "does not belong to ImsPhoneCallTracker " + this);
            }
            call.onHangupLocal();
            if (imsCall != null) {
                if (rejectCall) {
                    try {
                        imsCall.reject(getHangupReasionInfo(reason));
                        this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 3);
                    } catch (ImsException e) {
                        throw new CallStateException(e.getMessage());
                    }
                } else {
                    imsCall.terminate(getHangupReasionInfo(reason));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 4);
                }
            } else if (this.mPendingMO != null && call == this.mForegroundCall) {
                this.mPendingMO.update((ImsCall) null, Call.State.DISCONNECTED);
                this.mPendingMO.onDisconnect();
                removeConnection(this.mPendingMO);
                this.mPendingMO = null;
                updatePhoneState();
                removeMessages(20);
            }
            this.mPhone.notifyPreciseCallStateChanged();
        } else {
            throw new CallStateException("no connections");
        }
    }

    /* access modifiers changed from: protected */
    public void callEndCleanupHandOverCallIfAny() {
        if (this.mHandoverCall.mConnections.size() > 0) {
            log("callEndCleanupHandOverCallIfAny, mHandoverCall.mConnections=" + this.mHandoverCall.mConnections);
            Iterator it = this.mHandoverCall.mConnections.iterator();
            while (it.hasNext()) {
                Connection conn = (Connection) it.next();
                log("SRVCC: remove connection=" + conn);
                removeConnection((ImsPhoneConnection) conn);
            }
            this.mHandoverCall.mConnections.clear();
            this.mConnections.clear();
            this.mState = PhoneConstants.State.IDLE;
            if (this.mPhone != null && this.mPhone.mDefaultPhone != null && this.mPhone.mDefaultPhone.getState() == PhoneConstants.State.IDLE) {
                log("SRVCC: notify ImsPhone state as idle.");
                this.mPhone.notifyPhoneStateChanged();
                this.mCallsDisconnectedDuringSrvccRegistrants.notifyRegistrants(getCallStateChangeAsyncResult());
            }
        }
    }

    public void sendUSSD(String ussdString, Message response) {
        try {
            log("sendUSSD, putDialFrom");
            ImsCallProfile profile = this.mImsManager.createCallProfile(1, 2);
            profile.setCallExtraInt("dialstring", 2);
            this.mDigitsUtil.putDialFrom(OpTelephonyCustomizationUtils.getOpFactory(this.mPhone.getContext()).makeDigitsUssdManager().getUssdExtra(), profile);
            MtkImsPhoneCallTracker.super.sendUSSD(ussdString, response);
        } catch (ImsException e) {
            loge("sendUSSD : " + e);
            this.mPhone.sendErrorResponse(response, e);
            retryGetImsService();
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void addConnection(ImsPhoneConnection conn) {
        MtkImsPhoneCallTracker.super.addConnection(conn);
        if (conn.isEmergency()) {
            this.mRttEmcGuardTimerUtil.stopRttEmcGuardTimer();
        }
    }

    /* access modifiers changed from: protected */
    public void processCallStateChange(ImsCall imsCall, Call.State state, int cause, boolean ignoreState) {
        MtkImsPhoneCallTracker.super.processCallStateChange(imsCall, state, cause, ignoreState);
        logDebugMessagesWithDumpFormat("CC", findConnection(imsCall), "");
        checkRttCallType();
        if (!this.mIsDataEnabled && state == Call.State.ACTIVE) {
            log("ImsCall updated to video call but data off, retry onDataEnabledChanged");
            sendEmptyMessage(105);
        }
    }

    public int getDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo, Call.State callState) {
        int code = maybeRemapReasonCode(reasonInfo);
        switch (code) {
            case 1600:
                return 1500;
            case 1601:
                return 1501;
            case 1602:
                return 1502;
            case 1603:
                return 1503;
            case 1604:
                return 1504;
            case 1605:
                return 1505;
            case 1606:
                return 1506;
            case 1607:
                return 1507;
            case 1608:
                return 1508;
            case 1609:
                return 1509;
            case 1610:
                return 1510;
            case 1611:
                return 1511;
            case 1612:
                return 1512;
            case 1613:
                return 1513;
            case 1614:
                return 1514;
            case 1615:
                return 1515;
            case 1616:
                return 1516;
            case 1617:
                return 1517;
            case 1618:
                return 1518;
            case 1619:
                return 1519;
            case 1620:
                return 1520;
            case 1621:
                return 1521;
            case 1622:
                return 1522;
            case 1623:
                return 1523;
            case 1624:
                return 1524;
            case 1625:
                return 1525;
            case 1626:
                return 1526;
            case 1627:
                return 1527;
            case 1628:
                return 1528;
            case 1629:
                return 1529;
            case 1630:
                return 1530;
            case 1631:
                return 1531;
            case 1632:
                return 1532;
            case 1633:
                return 1533;
            case 1634:
                return 1534;
            case 1635:
                return 1535;
            case 1636:
                return 1536;
            case 1637:
                return 1537;
            case 1638:
                return 1538;
            case 1639:
                return 1539;
            case 1640:
                return ExternalSimConstants.MSG_ID_UICC_AUTHENTICATION_DONE_IND;
            case 1641:
                return ExternalSimConstants.MSG_ID_FINALIZATION_RESPONSE;
            case 1642:
                return ExternalSimConstants.MSG_ID_UICC_AUTHENTICATION_ABORT_IND;
            case 1643:
                return MtkCharacterSets.SCSU;
            default:
                switch (code) {
                    case 61442:
                        return ExternalSimConstants.MSG_ID_UICC_APDU_REQUEST;
                    case 61443:
                        return ExternalSimConstants.MSG_ID_UICC_POWER_DOWN_REQUEST;
                    case 61444:
                        return ExternalSimConstants.MSG_ID_GET_PLATFORM_CAPABILITY_RESPONSE;
                    case 61445:
                        return ExternalSimConstants.MSG_ID_EVENT_RESPONSE;
                    default:
                        switch (code) {
                            case 61451:
                                return 400;
                            case 61452:
                                return 401;
                            case 61453:
                                return 402;
                            case 61454:
                                return 403;
                            default:
                                return MtkImsPhoneCallTracker.super.getDisconnectCauseFromReasonInfo(reasonInfo, callState);
                        }
                }
        }
    }

    /* access modifiers changed from: protected */
    public void notifySrvccState(Call.SrvccState state) {
        if (state == Call.SrvccState.COMPLETED) {
            sendRttSrvccOrCsfbEvent(this.mForegroundCall);
            sendRttSrvccOrCsfbEvent(this.mBackgroundCall);
            sendRttSrvccOrCsfbEvent(this.mRingingCall);
        }
        MtkImsPhoneCallTracker.super.notifySrvccState(state);
        if (this.mSrvccState == Call.SrvccState.COMPLETED) {
            updateForSrvccCompleted();
        } else if (this.mSrvccState == Call.SrvccState.FAILED) {
            this.mSrvccState = Call.SrvccState.NONE;
        }
        if (this.mSrvccState == Call.SrvccState.COMPLETED) {
            checkRttCallType();
        }
    }

    /* access modifiers changed from: protected */
    public void releasePendingMOIfRequired() {
        if (this.mPendingMO != null) {
            this.mPendingMO.setDisconnectCause(36);
            sendEmptyMessageDelayed(18, 500);
        }
    }

    /* access modifiers changed from: protected */
    public void transferHandoverConnections(ImsPhoneCall call) {
        log("transferHandoverConnections mSrvccState:" + this.mSrvccState);
        if (this.mSrvccState == Call.SrvccState.COMPLETED && call.mConnections != null) {
            Iterator it = call.mConnections.iterator();
            while (it.hasNext()) {
                Connection conn = (Connection) it.next();
                if (this.mOnHoldToneStarted && conn != null && this.mOnHoldToneId == System.identityHashCode(conn)) {
                    log("transferHandoverConnections reset the hold tone.");
                    this.mPhone.stopOnHoldTone(conn);
                    this.mOnHoldToneStarted = false;
                    this.mOnHoldToneId = -1;
                }
            }
        }
        MtkImsPhoneCallTracker.super.transferHandoverConnections(call);
    }

    public void handleMessage(Message msg) {
        log("handleMessage what=" + msg.what);
        switch (msg.what) {
            case 101:
                onDataRoamingOn();
                return;
            case 102:
                onDataRoamingOff();
                return;
            case EVENT_ROAMING_SETTING_CHANGE /* 103 */:
                onRoamingSettingsChanged();
                return;
            case 104:
                try {
                    this.mPhone.unholdHeldCall();
                    return;
                } catch (CallStateException e) {
                    loge("handleMessage EVENT_RESUME_BACKROUND_CALL exception=" + e);
                    return;
                }
            case 105:
                onDataEnabledChanged(this.mIsDataEnabled, this.mLastDataEnabledReason);
                return;
            default:
                MtkImsPhoneCallTracker.super.handleMessage(msg);
                return;
        }
    }

    public class IncomingCallEventRecevier extends BroadcastReceiver implements MtkIncomingCallChecker.OnCheckCompleteListener {
        public IncomingCallEventRecevier() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.android.ims.IMS_INCOMING_CALL_INDICATION")) {
                MtkImsPhoneCallTracker.this.log("onReceive() indication call intent");
                if (MtkImsPhoneCallTracker.this.mImsManager == null) {
                    MtkImsPhoneCallTracker.this.log("onReceive() no ims manager");
                    return;
                }
                int phoneId = intent.getIntExtra("android:phoneId", -1);
                int subId = MtkImsPhoneCallTracker.this.mPhone.getSubId();
                String number = intent.getStringExtra("android:imsDialString");
                MtkImsPhoneCallTracker mtkImsPhoneCallTracker = MtkImsPhoneCallTracker.this;
                mtkImsPhoneCallTracker.log("onReceive() : subId = " + subId + ", number =" + number + ", phoneId = " + phoneId);
                if (phoneId == MtkImsPhoneCallTracker.this.mPhone.getPhoneId()) {
                    MtkImsPhoneCallTracker.this.mIncomingCallCheker = new MtkIncomingCallChecker("ims_call_pre_check", intent);
                    if (MtkImsPhoneCallTracker.this.mIncomingCallCheker.startIncomingCallNumberCheck(MtkImsPhoneCallTracker.this.mPhone.getContext(), subId, number, this)) {
                        MtkImsPhoneCallTracker.this.log("onReceive() startIncomingCallNumberCheck true. start check ");
                        return;
                    }
                    MtkImsPhoneCallTracker.this.log("onReceive() startIncomingCallNumberCheck false, and flow continues");
                    MtkImsPhoneCallTracker mtkImsPhoneCallTracker2 = MtkImsPhoneCallTracker.this;
                    mtkImsPhoneCallTracker2.log("setCallIndication : intent = " + intent + ", isAllow = true, cause = 1");
                    try {
                        if (MtkImsPhoneCallTracker.this.mImsManager instanceof MtkImsManager) {
                            MtkImsPhoneCallTracker.this.mImsManager.setCallIndication(phoneId, intent, true, 1);
                        }
                    } catch (ImsException e) {
                        MtkImsPhoneCallTracker mtkImsPhoneCallTracker3 = MtkImsPhoneCallTracker.this;
                        mtkImsPhoneCallTracker3.loge("setCallIndication ImsException " + e);
                    }
                }
            }
        }

        @Override // com.mediatek.internal.telephony.MtkIncomingCallChecker.OnCheckCompleteListener
        public void onCheckComplete(boolean result, Object obj) {
            int rejectCause = 1;
            boolean isAllow = true;
            Intent intent = (Intent) obj;
            int phoneId = -1;
            if (intent != null) {
                phoneId = intent.getIntExtra("android:phoneId", -1);
            }
            if (result) {
                rejectCause = 16;
                isAllow = false;
            }
            MtkImsPhoneCallTracker mtkImsPhoneCallTracker = MtkImsPhoneCallTracker.this;
            mtkImsPhoneCallTracker.log("onCheckComplete(): intent = " + intent + ", isAllow = " + isAllow + ", cause = " + rejectCause);
            try {
                if (MtkImsPhoneCallTracker.this.mImsManager instanceof MtkImsManager) {
                    MtkImsPhoneCallTracker.this.mImsManager.setCallIndication(phoneId, intent, isAllow, rejectCause);
                }
            } catch (ImsException e) {
                MtkImsPhoneCallTracker mtkImsPhoneCallTracker2 = MtkImsPhoneCallTracker.this;
                mtkImsPhoneCallTracker2.loge("onCheckComplete() ImsException " + e);
            }
        }
    }

    private void registerIndicationReceiver() {
        log("registerIndicationReceiver");
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("com.android.ims.IMS_INCOMING_CALL_INDICATION");
        this.mPhone.getContext().registerReceiver(this.mIndicationReceiver, intentfilter);
    }

    private void unregisterIndicationReceiver() {
        log("unregisterIndicationReceiver");
        this.mPhone.getContext().unregisterReceiver(this.mIndicationReceiver);
    }

    /* access modifiers changed from: package-private */
    public Connection dial(List<String> numbers, int videoState) throws CallStateException {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext());
        return dial(numbers, sp.getInt("clir_key" + this.mPhone.getPhoneId(), 0), videoState);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00b1, code lost:
        addConnection(r12.mPendingMO);
        r4 = new java.lang.StringBuilder();
        r6 = r13.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00c3, code lost:
        if (r6.hasNext() == false) goto L_0x00d5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c5, code lost:
        r4.append(r6.next());
        r4.append(", ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00d5, code lost:
        r8 = r12.mPendingMO;
        logDebugMessagesWithOpFormat("CC", "DialConf", r8, " numbers=" + sensitiveEncode(r4.toString()));
        logDebugMessagesWithDumpFormat("CC", r12.mPendingMO, "");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0108, code lost:
        if (r12.mBackgroundCall.getState() != com.android.internal.telephony.Call.State.HOLDING) goto L_0x0131;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0114, code lost:
        if (r12.mBackgroundCall.getImsCall().isPendingResume() == false) goto L_0x0131;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0120, code lost:
        if (r12.mForegroundCall.getState().isAlive() != false) goto L_0x0129;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0122, code lost:
        log("dial waitforRusume = true ");
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0130, code lost:
        throw new com.android.internal.telephony.CallStateException("cannot dial in current state");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0131, code lost:
        if (r0 != false) goto L_0x013b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0133, code lost:
        if (r1 != false) goto L_0x013b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0135, code lost:
        dialInternal(r12.mPendingMO, r14, r15, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x013b, code lost:
        updatePhoneState();
        r12.mPhone.notifyPreciseCallStateChanged();
     */
    public synchronized Connection dial(List<String> numbers, int clirMode, int videoState) throws CallStateException {
        ImsPhoneConnection conn;
        ImsPhoneConnection conn2;
        log("dial clirMode=" + clirMode);
        clearDisconnected();
        if (this.mImsManager != null) {
            checkForDialIssues();
            boolean holdBeforeDial = false;
            boolean waitforRusume = false;
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                if (this.mBackgroundCall.getState() == Call.State.IDLE) {
                    holdBeforeDial = true;
                    holdActiveCallForPendingMo();
                } else {
                    throw new CallStateException(6, "Already too many ongoing calls.");
                }
            }
            Call.State state = Call.State.IDLE;
            Call.State state2 = Call.State.IDLE;
            this.mClirMode = clirMode;
            synchronized (this.mSyncHold) {
                if (holdBeforeDial) {
                    try {
                        Call.State fgState = this.mForegroundCall.getState();
                        Call.State bgState = this.mBackgroundCall.getState();
                        if (fgState != Call.State.ACTIVE) {
                            try {
                                if (bgState == Call.State.HOLDING) {
                                    holdBeforeDial = false;
                                }
                            } catch (Throwable th) {
                                conn2 = th;
                                throw conn2;
                            }
                        } else {
                            throw new CallStateException("cannot dial in current state");
                        }
                    } catch (Throwable th2) {
                        conn2 = th2;
                        throw conn2;
                    }
                }
                conn = new MtkImsPhoneConnection((Phone) this.mPhone, "", (ImsPhoneCallTracker) this, this.mForegroundCall, false);
                this.mPendingMO = conn;
                ArrayList<String> dialStrings = new ArrayList<>();
                for (String str : numbers) {
                    if (!PhoneNumberUtils.isUriNumber(str)) {
                        dialStrings.add(PhoneNumberUtils.extractNetworkPortionAlt(str));
                    } else {
                        dialStrings.add(str);
                    }
                }
                this.mPendingMO.setConfDialStrings(dialStrings);
            }
        } else {
            throw new CallStateException("service not available");
        }
        return conn;
    }

    /* access modifiers changed from: package-private */
    public void hangupAll() throws CallStateException {
        log("hangupAll");
        if (this.mImsManager == null || !(this.mImsManager instanceof MtkImsManager)) {
            throw new CallStateException("No MtkImsManager Instance");
        }
        try {
            this.mImsManager.hangupAllCall(this.mPhone.getPhoneId());
            if (!this.mRingingCall.isIdle()) {
                setCallTerminationFlag(this.mRingingCall);
                this.mRingingCall.onHangupLocal();
            }
            if (!this.mForegroundCall.isIdle()) {
                setCallTerminationFlag(this.mForegroundCall);
                this.mForegroundCall.onHangupLocal();
            }
            if (!this.mBackgroundCall.isIdle()) {
                setCallTerminationFlag(this.mBackgroundCall);
                this.mBackgroundCall.onHangupLocal();
            }
        } catch (ImsException e) {
            throw new CallStateException(e.getMessage());
        }
    }

    private void setCallTerminationFlag(ImsPhoneCall imsPhoneCall) {
        log("setCallTerminationFlag");
        ImsCall imsCall = imsPhoneCall.getImsCall();
        if (imsCall == null) {
            log("setCallTerminationFlag " + imsPhoneCall + " no ims call");
            return;
        }
        ((MtkImsCall) imsCall).setTerminationRequestFlag(true);
    }

    /* access modifiers changed from: protected */
    public void logDebugMessagesWithOpFormat(String category, String action, ImsPhoneConnection conn, String msg) {
        FormattedLog formattedLog;
        if (category != null && action != null && conn != null && (formattedLog = new FormattedLog.Builder().setCategory(category).setServiceName("ImsPhone").setOpType(FormattedLog.OpType.OPERATION).setActionName(action).setCallNumber(sensitiveEncode(getCallNumber(conn))).setCallId(getConnectionCallId((MtkImsPhoneConnection) conn)).setExtraMessage(msg).buildDebugMsg()) != null) {
            if (!SENLOG || TELDBG) {
                log(formattedLog.toString());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void logDebugMessagesWithDumpFormat(String category, ImsPhoneConnection conn, String msg) {
        if (category != null && conn != null && (conn instanceof MtkImsPhoneConnection)) {
            MtkImsPhoneConnection connExt = (MtkImsPhoneConnection) conn;
            FormattedLog formattedLog = new FormattedLog.Builder().setCategory("CC").setServiceName("ImsPhone").setOpType(FormattedLog.OpType.DUMP).setCallNumber(sensitiveEncode(getCallNumber(conn))).setCallId(getConnectionCallId(connExt)).setExtraMessage(msg).setStatusInfo("state", conn.getState().toString()).setStatusInfo("isConfCall", conn.isMultiparty() ? "Yes" : "No").setStatusInfo("isConfChildCall", "No").setStatusInfo("parent", connExt.getParentCallName()).buildDumpInfo();
            if (formattedLog == null) {
                return;
            }
            if (!SENLOG || TELDBG) {
                log(formattedLog.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getConnectionCallId(MtkImsPhoneConnection conn) {
        if (conn == null) {
            return "";
        }
        int callId = conn.getCallId();
        if (callId == -1 && (callId = conn.getCallIdBeforeDisconnected()) == -1) {
            return "";
        }
        return String.valueOf(callId);
    }

    private String getCallNumber(ImsPhoneConnection conn) {
        if (conn == null) {
            return null;
        }
        if (conn.isMultiparty()) {
            return "conferenceCall";
        }
        return conn.getAddress();
    }

    private boolean isVendorDisconnectCauseNeeded(ImsReasonInfo reasonInfo) {
        if (reasonInfo == null) {
            return false;
        }
        reasonInfo.getCode();
        if (reasonInfo.getExtraMessage() == null) {
            log("isVendorDisconnectCauseNeeded = no due to empty errorMsg");
            return false;
        }
        log("isVendorDisconnectCauseNeeded = no, no matched case");
        return false;
    }

    public ImsUtInterface getUtInterface() throws ImsException {
        if (this.mImsManager != null) {
            return this.mImsManager.getSupplementaryServiceConfiguration();
        }
        throw getImsManagerIsNullException();
    }

    /* access modifiers changed from: protected */
    public ImsPhoneConnection makeImsPhoneConnectionForMO(String dialString, boolean isEmergencyNumber) {
        return new MtkImsPhoneConnection((Phone) this.mPhone, checkForTestEmergencyNumber(dialString), (ImsPhoneCallTracker) this, this.mForegroundCall, isEmergencyNumber);
    }

    /* access modifiers changed from: protected */
    public ImsPhoneConnection makeImsPhoneConnectionForMT(ImsCall imsCall, boolean isUnknown) {
        ImsPhoneCall imsPhoneCall;
        ImsPhone imsPhone = this.mPhone;
        if (isUnknown) {
            imsPhoneCall = this.mForegroundCall;
        } else {
            imsPhoneCall = this.mRingingCall;
        }
        return new MtkImsPhoneConnection((Phone) imsPhone, imsCall, (ImsPhoneCallTracker) this, imsPhoneCall, isUnknown);
    }

    /* access modifiers changed from: protected */
    public ImsCall takeCall(IImsCallSession c, Bundle extras) throws ImsException {
        return this.mImsManager.takeCall(c, extras, this.mMtkImsCallListener);
    }

    /* access modifiers changed from: protected */
    public boolean isEmergencyNumber(String dialString) {
        return MtkLocalPhoneNumberUtils.getIsEmergencyNumber();
    }

    /* access modifiers changed from: protected */
    public void checkforCsfb() throws CallStateException {
        if (this.mHandoverCall.mConnections.size() > 0) {
            log("SRVCC: there are connections during handover, trigger CSFB!");
            throw new CallStateException("cs_fallback");
        } else if (this.mPhone != null && this.mPhone.getDefaultPhone() != null && this.mPhone.getDefaultPhone().getState() != PhoneConstants.State.IDLE && getState() == PhoneConstants.State.IDLE) {
            log("There are CS connections, trigger CSFB!");
            throw new CallStateException("cs_fallback");
        }
    }

    /* access modifiers changed from: protected */
    public boolean canDailOnCallTerminated() {
        return this.mPendingMO != null && !hasMessages(18);
    }

    /* access modifiers changed from: protected */
    public void setRedialAsEcc(int cause) {
        if (!SystemProperties.get("ro.vendor.md_auto_setup_ims").equals("1") && cause == 71) {
            this.mDialAsECC = true;
        }
    }

    /* access modifiers changed from: protected */
    public void setVendorDisconnectCause(ImsPhoneConnection conn, ImsReasonInfo reasonInfo) {
        if (conn instanceof MtkImsPhoneConnection) {
            ((MtkImsPhoneConnection) conn).setVendorDisconnectCause(reasonInfo.getExtraMessage());
        }
    }

    /* access modifiers changed from: protected */
    public int updateDisconnectCause(int cause, ImsPhoneConnection conn) {
        if (cause != 36 || conn == null || !conn.getImsCall().isMerged()) {
            return cause;
        }
        return 45;
    }

    /* access modifiers changed from: protected */
    public void setMultiPartyState(Connection c) {
        if (c instanceof MtkImsPhoneConnection) {
            ((MtkImsPhoneConnection) c).mWasMultiparty = c.isMultiparty();
            ((MtkImsPhoneConnection) c).mWasPreMultipartyHost = c.isConferenceHost();
            log("SRVCC: Connection isMultiparty is " + ((MtkImsPhoneConnection) c).mWasMultiparty + "and isConfHost is " + ((MtkImsPhoneConnection) c).mWasPreMultipartyHost + " before handover");
        }
    }

    /* access modifiers changed from: protected */
    public void resetRingBackTone(ImsPhoneCall call) {
        if (call instanceof MtkImsPhoneCall) {
            ((MtkImsPhoneCall) call).resetRingbackTone();
        }
    }

    /* access modifiers changed from: protected */
    public void updateForSrvccCompleted() {
        if (this.mPendingMO != null) {
            log("SRVCC: reset mPendingMO");
            removeConnection(this.mPendingMO);
            this.mPendingMO = null;
        }
        this.mSrvccState = Call.SrvccState.NONE;
        if (this.mHoldSwitchingState != ImsPhoneCallTracker.HoldSwapState.INACTIVE) {
            this.mHoldSwitchingState = ImsPhoneCallTracker.HoldSwapState.INACTIVE;
        }
    }

    /* access modifiers changed from: protected */
    public AsyncResult getCallStateChangeAsyncResult() {
        return new AsyncResult((Object) null, this.mSrvccState, (Throwable) null);
    }

    /* access modifiers changed from: protected */
    public void checkIncomingCallInRttEmcGuardTime(ImsPhoneConnection conn) {
        this.mRttEmcGuardTimerUtil.checkIncomingCallInRttEmcGuardTime(conn);
    }

    /* JADX INFO: Multiple debug info for r6v1 int: [D('profile' android.telephony.ims.ImsCallProfile), D('preImsRttCallType' int)] */
    /* access modifiers changed from: protected */
    public void checkRttCallType() {
        ImsCallProfile profile;
        ImsStreamMediaProfile mediaProfile;
        log("checkRttCallType phone: " + this.mPhone + "srvccState " + this.mSrvccState);
        if (this.mPhone != null && this.mForegroundCall != null) {
            boolean imsRegistered = this.mPhone.getServiceState().getState() == 0;
            ImsCall imscall = this.mForegroundCall.getImsCall();
            boolean isRttCall = false;
            boolean isSrvcc = this.mSrvccState == Call.SrvccState.STARTED || this.mSrvccState == Call.SrvccState.COMPLETED;
            if (!(imscall == null || (profile = imscall.getCallProfile()) == null || (mediaProfile = profile.getMediaProfile()) == null)) {
                isRttCall = mediaProfile.isRttCall();
            }
            int preImsRttCallType = this.mImsRttCallType;
            if (isRttCall && !isSrvcc) {
                this.mImsRttCallType = 1;
            } else if (!imsRegistered) {
                this.mImsRttCallType = 0;
            } else if (preImsRttCallType == 1 && isSrvcc) {
                this.mImsRttCallType = 3;
            } else {
                this.mImsRttCallType = 1;
            }
            log("checkRttCallType : old" + preImsRttCallType + " new: " + this.mImsRttCallType);
            if (preImsRttCallType != this.mImsRttCallType) {
                log("set to audioManager " + this.mImsRttCallType);
                ((AudioManager) this.mPhone.getContext().getSystemService("audio")).setParameters("rtt_call_type=" + this.mImsRttCallType);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startRttEmcGuardTimer() {
        this.mRttEmcGuardTimerUtil.startRttEmcGuardTimer();
    }

    /* access modifiers changed from: protected */
    public void startListeningForCalls() throws ImsException {
        MtkImsPhoneCallTracker.super.startListeningForCalls();
        try {
            this.mImsManager.addImsConnectionStateListener(this.mImsStateListener);
            log("startListeningForCalls() : register ims succeed, " + this.mImsStateListener);
        } catch (ImsException e) {
            log("startListeningForCalls() : register ims fail!");
        }
    }

    /* access modifiers changed from: protected */
    public void modifyVideoCall(ImsCall imsCall, int newVideoState) {
        ImsPhoneConnection conn = findConnection(imsCall);
        int newVideoState2 = newVideoState | 32768;
        if (conn != null) {
            int oldVideoState = conn.getVideoState();
            if (conn.getVideoProvider() != null) {
                conn.getVideoProvider().onSendSessionModifyRequest(new VideoProfile(oldVideoState), new VideoProfile(newVideoState2));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void switchWfcModeIfRequired(ImsManager imsManager, boolean isWfcEnabled, boolean isEmergencyNumber) {
        if (imsManager == null || !isWfcEnabled || !isEmergencyNumber || !this.mCarrierSwitchWfcModeRequired) {
            log("Do not switch WFC mode, isWfcEnabled:" + isWfcEnabled + ", isEmergencyNumber:" + isEmergencyNumber + ", mCarrierSwitchWfcModeRequired:" + this.mCarrierSwitchWfcModeRequired);
        } else if (imsManager.getWfcMode() == 0) {
            imsManager.setWfcMode(1);
        }
    }

    /* access modifiers changed from: protected */
    public String getVtInterface() {
        String subscriberId = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSubscriberId(this.mPhone.getSubId());
        String vtIf = new String("vt_data0" + subscriberId);
        log("[SubId=" + this.mPhone.getSubId() + "] getVtInterface(): " + sensitiveEncode(vtIf));
        return vtIf;
    }

    public static String sensitiveEncode(String input) {
        if (!SENLOG || TELDBG) {
            return Rlog.pii(LOG_TAG, input);
        }
        return "[hidden]";
    }

    /* access modifiers changed from: protected */
    public boolean isCarrierPauseAllowed(ImsCall imsCall) {
        if (imsCall == null || imsCall.getState() != 3) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean ignoreCarrierPauseSupport() {
        return true;
    }

    public boolean isWifiPdnOutOfService() {
        int i = this.mWifiPdnOOSState;
        return i == 1 || i == 0;
    }

    public void registerForCallsDisconnectedDuringSrvcc(Handler h, int what, Object obj) {
        this.mCallsDisconnectedDuringSrvccRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallsDisconnectedDuringSrvcc(Handler h) {
        this.mCallsDisconnectedDuringSrvccRegistrants.remove(h);
    }

    public void registerSettingsObserver() {
        this.mSettingsObserver.unobserve();
        String simSuffix = "";
        if (TelephonyManager.getDefault().getSimCount() > 1) {
            simSuffix = Integer.toString(this.mPhone.getDefaultPhone().getSubId());
        }
        SettingsObserver settingsObserver = this.mSettingsObserver;
        settingsObserver.observe(Settings.Global.getUriFor("data_roaming" + simSuffix), (int) EVENT_ROAMING_SETTING_CHANGE);
    }

    public void initRoamingAndRoamingSetting() {
        this.mIsDataRoaming = this.mPhone.getDefaultPhone().getServiceState().getDataRoaming();
        this.mIsDataRoamingSettingEnabled = this.mPhone.getDefaultPhone().getDataRoamingEnabled();
        ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId()).setDataRoamingSettingsEnabled(this.mIsDataRoamingSettingEnabled);
        log("initRoamingAndRoamingSetting, mIsDataRoaming = " + this.mIsDataRoaming + ", mIsDataRoamingSettingEnabled = " + this.mIsDataRoamingSettingEnabled);
    }

    /* access modifiers changed from: protected */
    public void onDataRoamingOn() {
        log("onDataRoamingOn");
        if (this.mIsDataRoaming) {
            log("onDataRoamingOn: device already in roaming. ignored the update.");
            return;
        }
        this.mIsDataRoaming = this.mPhone.getDefaultPhone().getServiceState().getDataRoaming();
        if (this.mIsDataRoamingSettingEnabled) {
            log("onDataRoamingOn: setup data on roaming");
            onDataRoamingEnabledChanged(true);
            return;
        }
        log("onDataRoamingOn: Tear down data connection on roaming.");
        onDataRoamingEnabledChanged(false);
    }

    /* access modifiers changed from: protected */
    public void onDataRoamingOff() {
        log("onDataRoamingOff");
        if (!this.mIsDataRoaming) {
            log("onDataRoamingOff: device already not roaming. ignored the update.");
            return;
        }
        this.mIsDataRoaming = this.mPhone.getDefaultPhone().getServiceState().getDataRoaming();
        if (!this.mIsDataRoamingSettingEnabled) {
            onDataRoamingEnabledChanged(true);
        }
    }

    /* access modifiers changed from: protected */
    public void onRoamingSettingsChanged() {
        log("onRoamingSettingsChanged");
        this.mIsDataRoamingSettingEnabled = this.mPhone.getDefaultPhone().getDataRoamingEnabled();
        ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId()).setDataRoamingSettingsEnabled(this.mIsDataRoamingSettingEnabled);
        log("onRoamingSettingsChanged: mIsDataRoaming = " + this.mIsDataRoaming + ", mIsDataRoamingSettingEnabled = " + this.mIsDataRoamingSettingEnabled);
        if (!this.mIsDataRoaming) {
            log("onRoamingSettingsChanged: device is not roaming. ignored the request.");
        } else if (this.mIsDataRoamingSettingEnabled) {
            log("onRoamingSettingsChanged: setup data on roaming");
            onDataRoamingEnabledChanged(true);
        } else {
            log("onRoamingSettingsChanged: Tear down data connection on roaming.");
            onDataRoamingEnabledChanged(false);
        }
    }

    private void onDataRoamingEnabledChanged(boolean enabled) {
        log("onDataRoamingEnabledChanged: enabled=" + enabled);
        String str = "enabled";
        if (!this.mIsViLteDataMetered) {
            StringBuilder sb = new StringBuilder();
            sb.append("onDataRoamingEnabledChanged: Ignore data ");
            if (!enabled) {
                str = "disabled";
            }
            sb.append(str);
            sb.append(" - carrier policy indicates that data is not metered for ViLTE calls.");
            log(sb.toString());
        } else if (this.mIgnoreDataRoaming) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("onDataRoaming: Ignore data ");
            if (!enabled) {
                str = "disabled";
            }
            sb2.append(str);
            sb2.append(" - carrier policy indicates that ignore data roaming");
            log(sb2.toString());
        } else if (!enabled || this.mIsDataEnabled) {
            Iterator it = this.mConnections.iterator();
            while (true) {
                boolean isLocalVideoCapable = true;
                if (!it.hasNext()) {
                    break;
                }
                ImsPhoneConnection conn = (ImsPhoneConnection) it.next();
                ImsCall imsCall = conn.getImsCall();
                if (!enabled && (imsCall == null || !imsCall.isWifiCall())) {
                    isLocalVideoCapable = false;
                }
                conn.setLocalVideoCapable(isLocalVideoCapable);
            }
            maybeNotifyDataDisabled(enabled, 1406);
            handleDataEnabledChange(enabled, 1406);
            if (!this.mShouldUpdateImsConfigOnDisconnect && 2 != 0) {
                ImsManager.updateImsServiceConfig(this.mPhone.getContext(), this.mPhone.getPhoneId(), true);
            }
        } else {
            log("onDataRoamingEnabledChanged: Ignore on when data off");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRoamingOnAndRoamingSettingOff() {
        return this.mIsDataRoaming && !this.mIsDataRoamingSettingEnabled && !this.mIgnoreDataRoaming;
    }

    /* access modifiers changed from: protected */
    public void onDataEnabledChanged(boolean enabled, int reason) {
        this.mLastDataEnabledReason = reason;
        MtkImsPhoneCallTracker.super.onDataEnabledChanged(enabled, reason);
    }

    /* access modifiers changed from: protected */
    public void cacheCarrierConfiguration(int subId) {
        MtkImsPhoneCallTracker.super.cacheCarrierConfiguration(subId);
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (carrierConfigManager == null) {
            loge("cacheCarrierConfiguration: No carrier config service found.");
            return;
        }
        PersistableBundle carrierConfig = carrierConfigManager.getConfigForSubId(subId);
        if (carrierConfig == null) {
            loge("cacheCarrierConfiguration: Empty carrier config.");
            return;
        }
        this.mIgnoreDataRoaming = carrierConfig.getBoolean("mtk_ignore_data_roaming_for_video_calls");
        this.mIsRttCallMergeSupported = carrierConfig.getBoolean("mtk_rtt_call_merge_supported_bool");
        this.mRttEmcGuardTimerUtil.setRttEmcGuardTimerSupported(carrierConfig.getBoolean("mtk_emc_rtt_guard_timer_bool"));
        this.mCarrierSwitchWfcModeRequired = carrierConfig.getBoolean("mtk_carrier_switch_wfc_mode_required_bool");
        if (isTestSim()) {
            this.mIsViLteDataMetered = isVTDataMeteredByOpid(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, "OM"));
            this.mIgnoreDataRoaming = this.mIsViLteDataMetered;
            log("cacheCarrierConfiguration: For test sim, mIsViLteDataMetered = " + this.mIsViLteDataMetered);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isDataAvailableForViLTE() {
        return !this.mIsViLteDataMetered || (this.mIsDataEnabled && !isRoamingOnAndRoamingSettingOff());
    }

    public NetworkStats getVtDataUsage(boolean perUidStats) {
        log("getVtDataUsage: perUidStats=" + perUidStats + ", mState=" + this.mState);
        return MtkImsPhoneCallTracker.super.getVtDataUsage(perUidStats);
    }

    private void sendRttSrvccOrCsfbEvent(ImsPhoneCall call) {
        log("sendRttSrvccOrCsfbEvent: " + call);
        if (call == null) {
            loge("sendRttSrvccOrCsfbEvent no call");
        } else if (!isRttCall(call.getImsCall())) {
            log("sendRttSrvccOrCsfbEvent: not for RTT call");
        } else if (call.hasConnections()) {
            ImsCall activeCall = call.getFirstConnection().getImsCall();
            ImsPhoneConnection conn = call.getFirstConnection();
            if (activeCall != null && conn != null) {
                if (call.getState() == Call.State.DIALING) {
                    conn.onConnectionEvent("mediatek.telecom.event.EVENT_CSFB", (Bundle) null);
                } else {
                    conn.onConnectionEvent("mediatek.telecom.event.EVENT_SRVCC", (Bundle) null);
                }
            }
        }
    }

    private int getHangupReasionInfo(int disconnectCause) {
        if (disconnectCause == 1009) {
            return 1640;
        }
        if (disconnectCause == 1010) {
            return 1642;
        }
        if (disconnectCause == 1008) {
            return 1641;
        }
        if (disconnectCause == 1011) {
            return 1643;
        }
        return 504;
    }

    /* access modifiers changed from: protected */
    public boolean ignoreConference(ImsCall fgImsCall, ImsCall bgImsCall) {
        if (fgImsCall == null || fgImsCall.getState() == 8) {
            log("conference: skip; foreground call state terminated");
            return true;
        } else if (bgImsCall != null && bgImsCall.getState() != 8) {
            return false;
        } else {
            log("conference: skip; background call state terminated");
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isVTDataMeteredByOpid(String optr) {
        if (DataSubConstants.OPERATOR_OP01.equals(optr) || DataSubConstants.OPERATOR_OP02.equals(optr) || DataSubConstants.OPERATOR_OP09.equals(optr) || "OP17".equals(optr) || "OP50".equals(optr) || "OP149".equals(optr) || "OP149".equals(optr)) {
            return false;
        }
        return true;
    }

    private boolean isTestSim() {
        boolean isTestSim = SystemProperties.get("vendor.gsm.sim.ril.testsim").equals("1") || SystemProperties.get("vendor.gsm.sim.ril.testsim.2").equals("1") || SystemProperties.get("vendor.gsm.sim.ril.testsim.3").equals("1") || SystemProperties.get("vendor.gsm.sim.ril.testsim.4").equals("1");
        log("isTestSim: " + isTestSim);
        return isTestSim;
    }

    /* access modifiers changed from: protected */
    public boolean shouldResumeBackgroundCall() {
        if (this.mForegroundCall.getState() != Call.State.IDLE || this.mBackgroundCall.getState() != Call.State.HOLDING) {
            return false;
        }
        this.mForegroundCall.switchWith(this.mBackgroundCall);
        return true;
    }
}

package com.android.internal.telephony.imsphone;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.provider.Settings.Secure;
import android.telecom.ConferenceParticipant;
import android.telecom.Connection.VideoProvider;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Pair;
import com.android.ims.ImsCall;
import com.android.ims.ImsCall.Listener;
import com.android.ims.ImsCallProfile;
import com.android.ims.ImsConfigListener.Stub;
import com.android.ims.ImsConnectionStateListener;
import com.android.ims.ImsEcbm;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsMultiEndpoint;
import com.android.ims.ImsReasonInfo;
import com.android.ims.ImsSuppServiceNotification;
import com.android.ims.ImsUtInterface;
import com.android.ims.internal.IImsVideoCallProvider;
import com.android.ims.internal.ImsVideoCallProviderWrapper;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Call.SrvccState;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.Phone.FeatureType;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.TelephonyDevController;
import com.android.internal.telephony.TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.mediatek.internal.telephony.ConferenceCallMessageHandler;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import com.mediatek.telecom.FormattedLog;
import com.mediatek.telecom.FormattedLog.Builder;
import com.mediatek.telecom.FormattedLog.OpType;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ImsPhoneCallTracker extends CallTracker implements ImsPullCall {
    private static final boolean DBG = true;
    private static final int EVENT_CHECK_FOR_WIFI_HANDOVER = 25;
    private static final int EVENT_DATA_ENABLED_CHANGED = 23;
    private static final int EVENT_DIAL_PENDINGMO = 20;
    private static final int EVENT_EXIT_ECBM_BEFORE_PENDINGMO = 21;
    private static final int EVENT_GET_IMS_SERVICE = 24;
    private static final int EVENT_HANGUP_PENDINGMO = 18;
    private static final int EVENT_OPPO_PENGDING_HANGUP = 101;
    private static final int EVENT_RESUME_BACKGROUND = 19;
    private static final int EVENT_RETRY_DATA_ENABLED_CHANGED = 26;
    private static final int EVENT_VT_DATA_USAGE_UPDATE = 22;
    private static final boolean FORCE_VERBOSE_STATE_LOGGING = false;
    private static final int HANDOVER_TO_WIFI_TIMEOUT_MS = 60000;
    private static final int IMS_VIDEO_CALL = 21;
    private static final int IMS_VIDEO_CONF = 23;
    private static final int IMS_VIDEO_CONF_PARTS = 25;
    private static final int IMS_VOICE_CALL = 20;
    private static final int IMS_VOICE_CONF = 22;
    private static final int IMS_VOICE_CONF_PARTS = 24;
    private static final String IMS_VOLTE_ENABLE = "volte";
    private static final String IMS_VOWIFI_ENABLE = "vowifi";
    private static final int INVALID_CALL_MODE = 255;
    static final String LOG_TAG = "ImsPhoneCallTracker";
    static final int MAX_CONNECTIONS = 7;
    static final int MAX_CONNECTIONS_PER_CALL = 5;
    private static final int NUM_IMS_SERVICE_RETRIES = 10;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    private static final String PRO_IMS_TYPE = "gsm.ims.type";
    private static final boolean SENLOG = false;
    private static final boolean TELDBG = false;
    private static final int TIMEOUT_HANGUP_PENDINGMO = 500;
    private static final int TIME_BETWEEN_IMS_SERVICE_RETRIES_MS = 400;
    private static final int TIME_OPPO_PENGDING_HANGUP = 350;
    private static final boolean VERBOSE_STATE_LOGGING = false;
    static final String VERBOSE_STATE_TAG = "IPCTState";
    private boolean mAllowAddCallDuringVideoCall;
    private boolean mAllowEmergencyVideoCalls;
    public ImsPhoneCall mBackgroundCall;
    private ImsCall mCallExpectedToResume;
    private int mClirMode;
    private ArrayList<ImsPhoneConnection> mConnections;
    private boolean mDesiredMute;
    private boolean mDialAsECC;
    private boolean mDropVideoCallWhenAnsweringAudioCall;
    public ImsPhoneCall mForegroundCall;
    public ImsPhoneCall mHandoverCall;
    private boolean mHasPendingResumeRequest;
    private Listener mImsCallListener;
    private Stub mImsConfigListener;
    private ImsConnectionStateListener mImsConnectionStateListener;
    private boolean[] mImsFeatureEnabled;
    private final String[] mImsFeatureStrings;
    private ImsManager mImsManager;
    private Map<Pair<Integer, String>, Integer> mImsReasonCodeMap;
    private int mImsRegistrationErrorCode;
    private int mImsServiceRetryCount;
    private boolean mImsSupportEcc;
    private Listener mImsUssdListener;
    private BroadcastReceiver mIndicationReceiver;
    private boolean mIsInEmergencyCall;
    private boolean mIsNonDepOnData;
    private boolean mIsOnCallResumed;
    private boolean mLastDataEnabled;
    private int mLastDataEnabledReason;
    private TelephonyMetrics mMetrics;
    private boolean mNotifyHandoverVideoFromWifiToLTE;
    private boolean mNotifyVtHandoverToWifiFail;
    private int mOnHoldToneId;
    private boolean mOnHoldToneStarted;
    private int mPendingCallVideoState;
    private Bundle mPendingIntentExtras;
    private ImsPhoneConnection mPendingMO;
    private Message mPendingUssd;
    ImsPhone mPhone;
    private List<PhoneStateListener> mPhoneStateListeners;
    private BroadcastReceiver mReceiver;
    public ImsPhoneCall mRingingCall;
    private int mServiceId;
    private SrvccState mSrvccState;
    private State mState;
    private boolean mSupportDowngradeVtToAudio;
    private boolean mSwitchingFgAndBgCalls;
    private Object mSyncHold;
    private final Object mSyncLock;
    TelephonyDevController mTelDevController;
    private volatile long mTotalVtDataUsage;
    private boolean mTreatDowngradedVideoCallsAsVideoCalls;
    private ImsCall mUssdSession;
    private RegistrantList mVoiceCallEndedRegistrants;
    private RegistrantList mVoiceCallStartedRegistrants;
    private final HashMap<Integer, Long> mVtDataUsageMap;
    private int pendingCallClirMode;
    private boolean pendingCallInEcm;

    public interface PhoneStateListener {
        void onPhoneStateChanged(State state, State state2);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.imsphone.ImsPhoneCallTracker.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.imsphone.ImsPhoneCallTracker.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsPhoneCallTracker.<clinit>():void");
    }

    private boolean hasC2kOverImsModem() {
        if (this.mTelDevController == null || this.mTelDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasC2kOverImsModem()) {
            return false;
        }
        return true;
    }

    public ImsPhoneCallTracker(ImsPhone phone) {
        this.mImsFeatureEnabled = new boolean[]{false, false, false, false, false, false};
        String[] strArr = new String[6];
        strArr[0] = "VoLTE";
        strArr[1] = "ViLTE";
        strArr[2] = "VoWiFi";
        strArr[3] = "ViWiFi";
        strArr[4] = "UTLTE";
        strArr[5] = "UTWiFi";
        this.mImsFeatureStrings = strArr;
        this.mTelDevController = TelephonyDevController.getInstance();
        this.mReceiver = new BroadcastReceiver() {
            /* JADX WARNING: Removed duplicated region for block: B:50:0x023e A:{ExcHandler: android.os.RemoteException (e android.os.RemoteException), Splitter: B:8:0x002d} */
            /* JADX WARNING: Failed to process nested try/catch */
            /* JADX WARNING: Missing block: B:41:0x01ec, code:
            r11 = move-exception;
     */
            /* JADX WARNING: Missing block: B:42:0x01ed, code:
            r18.this$0.loge("onReceive : exception " + r11);
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.android.ims.IMS_INCOMING_CALL")) {
                    ImsPhoneCallTracker.this.log("onReceive : incoming call intent");
                    if (ImsPhoneCallTracker.this.mImsManager != null && ImsPhoneCallTracker.this.mServiceId >= 0) {
                        try {
                            if (intent.getBooleanExtra("android:ussd", false)) {
                                ImsPhoneCallTracker.this.log("onReceive : USSD");
                                ImsPhoneCallTracker.this.mUssdSession = ImsPhoneCallTracker.this.mImsManager.takeCall(ImsPhoneCallTracker.this.mServiceId, intent, ImsPhoneCallTracker.this.mImsUssdListener);
                                if (ImsPhoneCallTracker.this.mUssdSession != null) {
                                    ImsPhoneCallTracker.this.mUssdSession.accept(2);
                                }
                                return;
                            }
                            ImsPhoneCall imsPhoneCall;
                            boolean isUnknown = intent.getBooleanExtra("android:isUnknown", false);
                            ImsPhoneCallTracker.this.log("onReceive : isUnknown = " + isUnknown + " fg = " + ImsPhoneCallTracker.this.mForegroundCall.getState() + " bg = " + ImsPhoneCallTracker.this.mBackgroundCall.getState());
                            ImsCall imsCall = ImsPhoneCallTracker.this.mImsManager.takeCall(ImsPhoneCallTracker.this.mServiceId, intent, ImsPhoneCallTracker.this.mImsCallListener);
                            if (ImsPhoneCallTracker.this.mPhone != null && ImsPhoneCallTracker.this.-wrap3(ImsPhoneCallTracker.this.mPhone.getDefaultPhone())) {
                                ImsPhoneCallTracker.this.sendEmptyMessageDelayed(900, 500);
                            }
                            Phone phone = ImsPhoneCallTracker.this.mPhone;
                            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                            if (isUnknown) {
                                imsPhoneCall = ImsPhoneCallTracker.this.mForegroundCall;
                            } else {
                                imsPhoneCall = ImsPhoneCallTracker.this.mRingingCall;
                            }
                            ImsPhoneConnection conn = new ImsPhoneConnection(phone, imsCall, imsPhoneCallTracker, imsPhoneCall, isUnknown);
                            if (ImsPhoneCallTracker.this.mForegroundCall.hasConnections()) {
                                conn.setActiveCallDisconnectedOnAnswer(ImsPhoneCallTracker.this.shouldDisconnectActiveCallOnAnswer(ImsPhoneCallTracker.this.mForegroundCall.getFirstConnection().getImsCall(), imsCall));
                            }
                            conn.setAllowAddCallDuringVideoCall(ImsPhoneCallTracker.this.mAllowAddCallDuringVideoCall);
                            ImsPhoneCallTracker.this.addConnection(conn);
                            if (!OemConstant.isCallInEnable(ImsPhoneCallTracker.this.mPhone.getDefaultPhone())) {
                                ImsPhoneCallTracker.this.log("ctmm vi block");
                                imsCall.reject(504);
                            }
                            ImsPhoneCallTracker.this.setVideoCallProvider(conn, imsCall);
                            ImsPhoneCallTracker.this.logDebugMessagesWithDumpFormat("CC", conn, UsimPBMemInfo.STRING_NOT_SET);
                            TelephonyMetrics.getInstance().writeOnImsCallReceive(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getSession());
                            if (isUnknown) {
                                ImsPhoneCallTracker.this.mPhone.notifyUnknownConnection(conn);
                            } else {
                                if (!(ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.IDLE && ImsPhoneCallTracker.this.mBackgroundCall.getState() == Call.State.IDLE)) {
                                    conn.update(imsCall, Call.State.WAITING);
                                }
                                ImsPhoneCallTracker.this.mPhone.notifyNewRingingConnection(conn);
                                ImsPhoneCallTracker.this.mPhone.notifyIncomingRing();
                            }
                            ImsPhoneCallTracker.this.mPhone.getDefaultPhone().getCallTracker().oemClearConn();
                            ImsPhoneCallTracker.this.updatePhoneState();
                            ImsPhoneCallTracker.this.mPhone.notifyPreciseCallStateChanged();
                        } catch (ImsException e) {
                            ImsPhoneCallTracker.this.log("Exception in terminate call");
                        } catch (RemoteException e2) {
                        }
                    }
                } else if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    int subId = intent.getIntExtra("subscription", -1);
                    if (subId == ImsPhoneCallTracker.this.mPhone.getSubId()) {
                        ImsPhoneCallTracker.this.cacheCarrierConfiguration(subId);
                        ImsPhoneCallTracker.this.log("onReceive : Updating mAllowEmergencyVideoCalls = " + ImsPhoneCallTracker.this.mAllowEmergencyVideoCalls);
                    }
                }
            }
        };
        this.mConnections = new ArrayList();
        this.mVoiceCallEndedRegistrants = new RegistrantList();
        this.mVoiceCallStartedRegistrants = new RegistrantList();
        this.mRingingCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_RINGING);
        this.mForegroundCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_FOREGROUND);
        this.mBackgroundCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_BACKGROUND);
        this.mHandoverCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_HANDOVER);
        this.mVtDataUsageMap = new HashMap();
        this.mTotalVtDataUsage = 0;
        this.mClirMode = 0;
        this.mSyncHold = new Object();
        this.mUssdSession = null;
        this.mPendingUssd = null;
        this.mDesiredMute = false;
        this.mOnHoldToneStarted = false;
        this.mOnHoldToneId = -1;
        this.mState = State.IDLE;
        this.mServiceId = -1;
        this.mSrvccState = SrvccState.NONE;
        this.mIsInEmergencyCall = false;
        this.pendingCallInEcm = false;
        this.mSwitchingFgAndBgCalls = false;
        this.mCallExpectedToResume = null;
        this.mAllowEmergencyVideoCalls = false;
        this.mIsNonDepOnData = false;
        this.mDialAsECC = false;
        this.mHasPendingResumeRequest = false;
        this.mIsOnCallResumed = false;
        this.mSyncLock = new Object();
        this.mImsSupportEcc = false;
        this.mPhoneStateListeners = new ArrayList();
        this.mTreatDowngradedVideoCallsAsVideoCalls = false;
        this.mDropVideoCallWhenAnsweringAudioCall = false;
        this.mAllowAddCallDuringVideoCall = true;
        this.mNotifyVtHandoverToWifiFail = false;
        this.mSupportDowngradeVtToAudio = false;
        this.mNotifyHandoverVideoFromWifiToLTE = false;
        this.mImsReasonCodeMap = new ArrayMap();
        this.mImsCallListener = new Listener() {
            public void onCallProgressing(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallProgressing");
                ImsPhoneCallTracker.this.mPendingMO = null;
                ImsPhoneCallTracker.this.processPendingHangup("onCallProgressing");
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.ALERTING, 0);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallProgressing(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
            }

            public void onCallStarted(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallStarted");
                if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mCallExpectedToResume != null && ImsPhoneCallTracker.this.mCallExpectedToResume == imsCall) {
                    ImsPhoneCallTracker.this.log("onCallStarted: starting a call as a result of a switch.");
                    ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                }
                ImsPhoneCallTracker.this.mPendingMO = null;
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.ACTIVE, 0);
                if (ImsPhoneCallTracker.this.mNotifyVtHandoverToWifiFail && !imsCall.isWifiCall() && imsCall.isVideoCall() && ImsPhoneCallTracker.this.isWifiConnected()) {
                    ImsPhoneCallTracker.this.sendMessageDelayed(ImsPhoneCallTracker.this.obtainMessage(25, imsCall), 60000);
                }
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallStarted(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
            }

            public void onCallUpdated(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallUpdated");
                if (imsCall != null) {
                    ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                    if (conn != null) {
                        ImsPhoneCallTracker.this.processCallStateChange(imsCall, conn.getCall().mState, 0, true);
                        ImsPhoneCallTracker.this.mMetrics.writeImsCallState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), conn.getCall().mState);
                    }
                }
            }

            public void onCallStartFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallStartFailed reasonCode=" + reasonInfo.getCode());
                if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mCallExpectedToResume != null && ImsPhoneCallTracker.this.mCallExpectedToResume == imsCall) {
                    ImsPhoneCallTracker.this.log("onCallStarted: starting a call as a result of a switch.");
                    ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                }
                ImsPhoneCallTracker.this.processPendingHangup("onCallStartFailed");
                if (ImsPhoneCallTracker.this.mPendingMO != null) {
                    if (reasonInfo.getCode() == 146 && ImsPhoneCallTracker.this.mBackgroundCall.getState() == Call.State.IDLE && ImsPhoneCallTracker.this.mRingingCall.getState() == Call.State.IDLE) {
                        ImsPhoneCallTracker.this.mForegroundCall.detach(ImsPhoneCallTracker.this.mPendingMO);
                        ImsPhoneCallTracker.this.removeConnection(ImsPhoneCallTracker.this.mPendingMO);
                        ImsPhoneCallTracker.this.mPendingMO.finalize();
                        ImsPhoneCallTracker.this.mPendingMO = null;
                        ImsPhoneCallTracker.this.mPhone.initiateSilentRedial();
                        return;
                    }
                    ImsPhoneCallTracker.this.mPendingMO = null;
                    int cause = ImsPhoneCallTracker.this.getDisconnectCauseFromReasonInfo(reasonInfo);
                    if (cause == CallFailCause.IMS_EMERGENCY_REREG) {
                        ImsPhoneCallTracker.this.mDialAsECC = true;
                    }
                    ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                    if (conn != null) {
                        conn.setVendorDisconnectCause(reasonInfo.getExtraMessage());
                    }
                    ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.DISCONNECTED, cause);
                    ImsPhoneCallTracker.this.mMetrics.writeOnImsCallStartFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
                }
            }

            public void onCallTerminated(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallTerminated reasonCode=" + reasonInfo.getCode());
                int cause = ImsPhoneCallTracker.this.getDisconnectCauseFromReasonInfo(reasonInfo);
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                ImsPhoneCallTracker.this.log("cause = " + cause + " conn = " + conn);
                if (conn != null) {
                    VideoProvider videoProvider = conn.getVideoProvider();
                    if (videoProvider instanceof ImsVideoCallProviderWrapper) {
                        ((ImsVideoCallProviderWrapper) videoProvider).removeImsVideoProviderCallback(conn);
                    }
                }
                if (ImsPhoneCallTracker.this.mOnHoldToneId == System.identityHashCode(conn)) {
                    if (conn != null && ImsPhoneCallTracker.this.mOnHoldToneStarted) {
                        ImsPhoneCallTracker.this.mPhone.stopOnHoldTone(conn);
                    }
                    ImsPhoneCallTracker.this.mOnHoldToneStarted = false;
                    ImsPhoneCallTracker.this.mOnHoldToneId = -1;
                }
                if (conn != null) {
                    if (conn.isPulledCall() && ((reasonInfo.getCode() == CharacterSets.UTF_16 || reasonInfo.getCode() == 336 || reasonInfo.getCode() == 332) && ImsPhoneCallTracker.this.mPhone != null && ImsPhoneCallTracker.this.mPhone.getExternalCallTracker() != null)) {
                        ImsPhoneCallTracker.this.log("Call pull failed.");
                        conn.onCallPullFailed(ImsPhoneCallTracker.this.mPhone.getExternalCallTracker().getConnectionById(conn.getPulledDialogId()));
                        cause = 0;
                    } else if (conn.isIncoming() && conn.getConnectTime() == 0 && cause != 52) {
                        if (cause == 2) {
                            cause = 1;
                        } else {
                            cause = 16;
                        }
                        ImsPhoneCallTracker.this.log("Incoming connection of 0 connect time detected - translated cause = " + cause);
                    }
                }
                if (cause == 36 && conn != null && conn.getImsCall().isMerged()) {
                    cause = 45;
                }
                if (cause == CallFailCause.IMS_EMERGENCY_REREG) {
                    ImsPhoneCallTracker.this.mDialAsECC = true;
                }
                if (conn != null && ImsPhoneCallTracker.this.isVendorDisconnectCauseNeeded(reasonInfo)) {
                    conn.setVendorDisconnectCause(reasonInfo.getExtraMessage());
                }
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallTerminated(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.DISCONNECTED, cause);
                if (ImsPhoneCallTracker.this.mForegroundCall.getState() != Call.State.ACTIVE) {
                    if (ImsPhoneCallTracker.this.mRingingCall.getState().isRinging()) {
                        ImsPhoneCallTracker.this.mPendingMO = null;
                    } else if (ImsPhoneCallTracker.this.mPendingMO != null) {
                        ImsPhoneCallTracker.this.sendEmptyMessage(20);
                    }
                }
                if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls) {
                    String str;
                    ImsPhoneCallTracker.this.log("onCallTerminated: Call terminated in the midst of Switching Fg and Bg calls.");
                    if (imsCall == ImsPhoneCallTracker.this.mCallExpectedToResume) {
                        ImsPhoneCallTracker.this.log("onCallTerminated: switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                    }
                    ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                    StringBuilder append = new StringBuilder().append("onCallTerminated: foreground call in state ").append(ImsPhoneCallTracker.this.mForegroundCall.getState()).append(" and ringing call in state ");
                    if (ImsPhoneCallTracker.this.mRingingCall == null) {
                        str = "null";
                    } else {
                        str = ImsPhoneCallTracker.this.mRingingCall.getState().toString();
                    }
                    imsPhoneCallTracker.log(append.append(str).toString());
                    if (ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING || ImsPhoneCallTracker.this.mRingingCall.getState() == Call.State.WAITING) {
                        ImsPhoneCallTracker.this.sendEmptyMessage(19);
                        ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                        ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                    }
                }
            }

            public void onCallHeld(ImsCall imsCall) {
                if (ImsPhoneCallTracker.this.mForegroundCall.getImsCall() == imsCall) {
                    ImsPhoneCallTracker.this.log("onCallHeld (fg) " + imsCall);
                } else if (ImsPhoneCallTracker.this.mBackgroundCall.getImsCall() == imsCall) {
                    ImsPhoneCallTracker.this.log("onCallHeld (bg) " + imsCall);
                }
                synchronized (ImsPhoneCallTracker.this.mSyncHold) {
                    Call.State oldState = ImsPhoneCallTracker.this.mBackgroundCall.getState();
                    ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.HOLDING, 0);
                    if (oldState == Call.State.ACTIVE) {
                        if (ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING || ImsPhoneCallTracker.this.mRingingCall.getState() == Call.State.WAITING) {
                            ImsPhoneCallTracker.this.sendEmptyMessage(19);
                        } else {
                            if (ImsPhoneCallTracker.this.mPendingMO != null) {
                                ImsPhoneCallTracker.this.log("onCallHeld mPendingMO state is : " + ImsPhoneCallTracker.this.mPendingMO.getState());
                                if (ImsPhoneCallTracker.this.mPendingMO.getState() != Call.State.DISCONNECTING) {
                                    ImsPhoneCallTracker.this.dialPendingMO();
                                } else {
                                    ImsPhoneCallTracker.this.mPendingMO.update(null, Call.State.DISCONNECTED);
                                    ImsPhoneCallTracker.this.mPendingMO.onDisconnect();
                                    ImsPhoneCallTracker.this.mConnections.remove(ImsPhoneCallTracker.this.mPendingMO);
                                    ImsPhoneCallTracker.this.mPendingMO = null;
                                    ImsPhoneCallTracker.this.updatePhoneState();
                                    ImsPhoneCallTracker.this.removeMessages(20);
                                    if (ImsPhoneCallTracker.this.isNeedToResumeHoldCall(false)) {
                                        ImsPhoneCallTracker.this.resumeBackgroundCall();
                                    }
                                }
                            }
                            ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                        }
                    } else if (oldState == Call.State.IDLE && ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING) {
                        ImsPhoneCallTracker.this.sendEmptyMessage(19);
                        ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                        ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                    }
                }
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHeld(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
            }

            public void onCallHoldFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallHoldFailed reasonCode=" + reasonInfo.getCode());
                synchronized (ImsPhoneCallTracker.this.mSyncHold) {
                    Call.State bgState = ImsPhoneCallTracker.this.mBackgroundCall.getState();
                    if (reasonInfo.getCode() == 148) {
                        if (ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING || ImsPhoneCallTracker.this.mRingingCall.getState() == Call.State.WAITING) {
                            ImsPhoneCallTracker.this.log("onCallHoldFailed resume background");
                            ImsPhoneCallTracker.this.sendEmptyMessage(19);
                        } else if (ImsPhoneCallTracker.this.mPendingMO != null) {
                            ImsPhoneCallTracker.this.dialPendingMO();
                        }
                    } else if (bgState == Call.State.ACTIVE) {
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                        if (ImsPhoneCallTracker.this.mPendingMO != null) {
                            ImsPhoneCallTracker.this.mPendingMO.setDisconnectCause(36);
                            ImsPhoneCallTracker.this.sendEmptyMessageDelayed(18, 500);
                        }
                    }
                    ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.HOLD);
                }
                if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls) {
                    ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                }
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHoldFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
            }

            public void onCallResumed(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallResumed");
                ImsPhoneCallTracker.this.mIsOnCallResumed = true;
                if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls) {
                    if (imsCall != ImsPhoneCallTracker.this.mCallExpectedToResume) {
                        ImsPhoneCallTracker.this.log("onCallResumed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                    } else {
                        ImsPhoneCallTracker.this.log("onCallResumed : expected call resumed.");
                    }
                    ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                }
                ImsPhoneCallTracker.this.mHasPendingResumeRequest = false;
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.ACTIVE, 0);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
                ImsPhoneCallTracker.this.mIsOnCallResumed = false;
            }

            public void onCallResumeFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallResumeFailed");
                if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls) {
                    if (imsCall == ImsPhoneCallTracker.this.mCallExpectedToResume) {
                        ImsPhoneCallTracker.this.log("onCallResumeFailed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                        if (ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING) {
                            ImsPhoneCallTracker.this.sendEmptyMessage(19);
                        }
                    }
                    ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                    ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                }
                ImsPhoneCallTracker.this.mHasPendingResumeRequest = false;
                ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.SWITCH);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumeFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
            }

            public void onCallResumeReceived(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallResumeReceived");
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    if (ImsPhoneCallTracker.this.mOnHoldToneStarted) {
                        ImsPhoneCallTracker.this.mPhone.stopOnHoldTone(conn);
                        ImsPhoneCallTracker.this.mOnHoldToneStarted = false;
                    }
                    conn.onConnectionEvent("android.telecom.event.CALL_REMOTELY_UNHELD", null);
                }
                if (conn != null) {
                    conn.notifyRemoteHeld(false);
                }
                SuppServiceNotification supp = new SuppServiceNotification();
                supp.notificationType = 1;
                supp.code = 3;
                ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumeReceived(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
            }

            public void onCallHoldReceived(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallHoldReceived");
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    if (!ImsPhoneCallTracker.this.mOnHoldToneStarted && ImsPhoneCall.isLocalTone(imsCall) && conn.getState() == Call.State.ACTIVE) {
                        ImsPhoneCallTracker.this.mPhone.startOnHoldTone(conn);
                        ImsPhoneCallTracker.this.mOnHoldToneStarted = true;
                        ImsPhoneCallTracker.this.mOnHoldToneId = System.identityHashCode(conn);
                    }
                    conn.onConnectionEvent("android.telecom.event.CALL_REMOTELY_HELD", null);
                }
                if (conn != null) {
                    conn.notifyRemoteHeld(true);
                }
                SuppServiceNotification supp = new SuppServiceNotification();
                supp.notificationType = 1;
                supp.code = 2;
                ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHoldReceived(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
            }

            public void onCallSuppServiceReceived(ImsCall call, ImsSuppServiceNotification suppServiceInfo) {
                ImsPhoneCallTracker.this.log("onCallSuppServiceReceived: suppServiceInfo=" + suppServiceInfo);
                SuppServiceNotification supp = new SuppServiceNotification();
                supp.notificationType = suppServiceInfo.notificationType;
                supp.code = suppServiceInfo.code;
                supp.index = suppServiceInfo.index;
                supp.number = suppServiceInfo.number;
                supp.history = suppServiceInfo.history;
                ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
            }

            public void onCallMerged(ImsCall call, ImsCall peerCall, boolean swapCalls) {
                ImsPhoneCall peerImsPhoneCall;
                ImsPhoneCallTracker.this.log("onCallMerged");
                ImsPhoneCall foregroundImsPhoneCall = ImsPhoneCallTracker.this.findConnection(call).getCall();
                ImsPhoneConnection peerConnection = ImsPhoneCallTracker.this.findConnection(peerCall);
                if (peerConnection == null) {
                    peerImsPhoneCall = null;
                } else {
                    peerImsPhoneCall = peerConnection.getCall();
                }
                if (swapCalls) {
                    ImsPhoneCallTracker.this.switchAfterConferenceSuccess();
                }
                foregroundImsPhoneCall.merge(peerImsPhoneCall, Call.State.ACTIVE);
                try {
                    ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                    ImsPhoneCallTracker.this.log("onCallMerged: ImsPhoneConnection=" + conn);
                    ImsPhoneCallTracker.this.log("onCallMerged: CurrentVideoProvider=" + conn.getVideoProvider());
                    ImsPhoneCallTracker.this.setVideoCallProvider(conn, call);
                    ImsPhoneCallTracker.this.log("onCallMerged: CurrentVideoProvider=" + conn.getVideoProvider());
                } catch (Exception e) {
                    ImsPhoneCallTracker.this.loge("onCallMerged: exception " + e);
                }
                ImsPhoneCallTracker.this.processCallStateChange(ImsPhoneCallTracker.this.mForegroundCall.getImsCall(), Call.State.ACTIVE, 0);
                if (peerConnection != null) {
                    ImsPhoneCallTracker.this.processCallStateChange(ImsPhoneCallTracker.this.mBackgroundCall.getImsCall(), Call.State.HOLDING, 0);
                }
                if (call.isMergeRequestedByConf()) {
                    ImsPhoneCallTracker.this.log("onCallMerged :: Merge requested by existing conference.");
                    call.resetIsMergeRequestedByConf(false);
                } else {
                    ImsPhoneCallTracker.this.log("onCallMerged :: calling onMultipartyStateChanged()");
                    onMultipartyStateChanged(call, true);
                }
                ImsPhoneCallTracker.this.logState();
                ImsPhoneConnection hostConn = ImsPhoneCallTracker.this.findConnection(call);
                if (hostConn != null) {
                    FormattedLog formattedLog = new Builder().setCategory("CC").setServiceName("ImsPhone").setOpType(OpType.DUMP).setCallNumber(hostConn.getAddress()).setCallId(ImsPhoneCallTracker.this.getConnectionCallId(hostConn)).setStatusInfo("state", ConferenceCallMessageHandler.STATUS_DISCONNECTED).setStatusInfo("isConfCall", "No").setStatusInfo("isConfChildCall", "No").setStatusInfo("parent", hostConn.getParentCallName()).buildDumpInfo();
                    if (formattedLog != null) {
                        ImsPhoneCallTracker.this.log(formattedLog.toString());
                    }
                }
            }

            public void onCallMergeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallMergeFailed reasonInfo=" + reasonInfo);
                ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.CONFERENCE);
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                if (conn != null) {
                    conn.onConferenceMergeFailed();
                }
                if (ImsPhoneCallTracker.this.isNeedToResumeHoldCall(false)) {
                    ImsPhoneCallTracker.this.log("onCallMergeFailed : resumeBackgroundCall");
                    ImsPhoneCallTracker.this.resumeBackgroundCall();
                }
            }

            public void onConferenceParticipantsStateChanged(ImsCall call, List<ConferenceParticipant> participants) {
                ImsPhoneCallTracker.this.log("onConferenceParticipantsStateChanged");
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                if (conn != null) {
                    conn.updateConferenceParticipants(participants);
                }
            }

            public void onCallSessionTtyModeReceived(ImsCall call, int mode) {
                ImsPhoneCallTracker.this.mPhone.onTtyModeReceived(mode);
            }

            public void onCallHandover(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
                boolean isHandoverToWifi = false;
                ImsPhoneCallTracker.this.log("onCallHandover ::  srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
                if (srcAccessTech != 18 && targetAccessTech == 18) {
                    isHandoverToWifi = true;
                }
                if (isHandoverToWifi) {
                    ImsPhoneCallTracker.this.removeMessages(25);
                }
                boolean isHandoverFromWifi = srcAccessTech == 18 ? targetAccessTech != 18 : false;
                if (ImsPhoneCallTracker.this.mNotifyHandoverVideoFromWifiToLTE && isHandoverFromWifi && imsCall.isVideoCall()) {
                    ImsPhoneCallTracker.this.log("onCallHandover :: notifying of WIFI to LTE handover.");
                    ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                    if (conn != null) {
                        conn.onConnectionEvent("android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_WIFI_TO_LTE", null);
                    } else {
                        ImsPhoneCallTracker.this.loge("onCallHandover :: failed to notify of handover; connection is null.");
                    }
                }
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHandoverEvent(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 18, imsCall.getCallSession(), srcAccessTech, targetAccessTech, reasonInfo);
            }

            public void onCallHandoverFailed(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallHandoverFailed :: srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHandoverEvent(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 19, imsCall.getCallSession(), srcAccessTech, targetAccessTech, reasonInfo);
                boolean isHandoverToWifi = srcAccessTech != 18 ? targetAccessTech == 18 : false;
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null && isHandoverToWifi) {
                    ImsPhoneCallTracker.this.log("onCallHandoverFailed - handover to WIFI Failed");
                    ImsPhoneCallTracker.this.removeMessages(25);
                    if (ImsPhoneCallTracker.this.mNotifyVtHandoverToWifiFail) {
                        conn.onHandoverToWifiFailed();
                    }
                }
            }

            public void onMultipartyStateChanged(ImsCall imsCall, boolean isMultiParty) {
                ImsPhoneCallTracker.this.log("onMultipartyStateChanged to " + (isMultiParty ? "Y" : "N"));
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    conn.updateMultipartyState(isMultiParty);
                }
            }

            public void onCallInviteParticipantsRequestDelivered(ImsCall call) {
                ImsPhoneCallTracker.this.log("onCallInviteParticipantsRequestDelivered");
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                if (conn != null) {
                    conn.notifyConferenceParticipantsInvited(true);
                }
            }

            public void onCallInviteParticipantsRequestFailed(ImsCall call, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallInviteParticipantsRequestFailed reasonCode=" + reasonInfo.getCode());
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                if (conn != null) {
                    conn.notifyConferenceParticipantsInvited(false);
                }
            }

            public void onCallTransferred(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallTransferred");
            }

            public void onCallTransferFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("onCallTransferFailed");
                ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(SuppService.TRANSFER);
            }
        };
        this.mImsUssdListener = new Listener() {
            public void onCallStarted(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("mImsUssdListener onCallStarted");
                if (imsCall == ImsPhoneCallTracker.this.mUssdSession && ImsPhoneCallTracker.this.mPendingUssd != null) {
                    AsyncResult.forMessage(ImsPhoneCallTracker.this.mPendingUssd);
                    ImsPhoneCallTracker.this.mPendingUssd.sendToTarget();
                    ImsPhoneCallTracker.this.mPendingUssd = null;
                }
            }

            public void onCallStartFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("mImsUssdListener onCallStartFailed reasonCode=" + reasonInfo.getCode());
                onCallTerminated(imsCall, reasonInfo);
            }

            public void onCallTerminated(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker.this.log("mImsUssdListener onCallTerminated reasonCode=" + reasonInfo.getCode());
                ImsPhoneCallTracker.this.removeMessages(25);
                if (imsCall == ImsPhoneCallTracker.this.mUssdSession) {
                    ImsPhoneCallTracker.this.mUssdSession = null;
                    if (ImsPhoneCallTracker.this.mPendingUssd != null) {
                        AsyncResult.forMessage(ImsPhoneCallTracker.this.mPendingUssd, null, new CommandException(Error.GENERIC_FAILURE));
                        ImsPhoneCallTracker.this.mPendingUssd.sendToTarget();
                        ImsPhoneCallTracker.this.mPendingUssd = null;
                    }
                }
                imsCall.close();
            }

            public void onCallUssdMessageReceived(ImsCall call, int mode, String ussdMessage) {
                ImsPhoneCallTracker.this.log("mImsUssdListener onCallUssdMessageReceived mode=" + mode);
                int ussdMode = -1;
                switch (mode) {
                    case 0:
                        ussdMode = 0;
                        if (call == ImsPhoneCallTracker.this.mUssdSession) {
                            ImsPhoneCallTracker.this.mUssdSession = null;
                            call.close();
                            break;
                        }
                        break;
                    case 1:
                        ussdMode = 1;
                        break;
                    default:
                        if (call == ImsPhoneCallTracker.this.mUssdSession) {
                            ImsPhoneCallTracker.this.log("invalid mode: " + mode + ", clear ussi session");
                            ImsPhoneCallTracker.this.mUssdSession = null;
                            call.close();
                            break;
                        }
                        break;
                }
                ImsPhoneCallTracker.this.mPhone.onIncomingUSSD(ussdMode, ussdMessage);
            }
        };
        this.mImsConnectionStateListener = new ImsConnectionStateListener() {
            public void onImsConnected() {
                ImsPhoneCallTracker.this.log("onImsConnected");
                ImsPhoneCallTracker.this.mPhone.setServiceState(0);
                ImsPhoneCallTracker.this.mPhone.setImsRegistered(true);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 1, null);
            }

            public void onImsDisconnected(ImsReasonInfo imsReasonInfo) {
                ImsPhoneCallTracker.this.log("onImsDisconnected imsReasonInfo=" + imsReasonInfo);
                ImsPhoneCallTracker.this.mPhone.setServiceState(1);
                ImsPhoneCallTracker.this.mPhone.setImsRegistered(false);
                ImsPhoneCallTracker.this.mPhone.processDisconnectReason(imsReasonInfo);
                if (!(imsReasonInfo == null || imsReasonInfo.getExtraMessage() == null || imsReasonInfo.getExtraMessage().equals(UsimPBMemInfo.STRING_NOT_SET))) {
                    ImsPhoneCallTracker.this.mImsRegistrationErrorCode = Integer.parseInt(imsReasonInfo.getExtraMessage());
                }
                ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 3, imsReasonInfo);
            }

            public void onImsProgressing() {
                ImsPhoneCallTracker.this.log("onImsProgressing");
                ImsPhoneCallTracker.this.mPhone.setServiceState(1);
                ImsPhoneCallTracker.this.mPhone.setImsRegistered(false);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 2, null);
            }

            public void onImsResumed() {
                ImsPhoneCallTracker.this.log("onImsResumed");
                ImsPhoneCallTracker.this.mPhone.setServiceState(0);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 4, null);
            }

            public void onImsSuspended() {
                ImsPhoneCallTracker.this.log("onImsSuspended");
                ImsPhoneCallTracker.this.mPhone.setServiceState(1);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 5, null);
            }

            public void onFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) {
                if (serviceClass == 1) {
                    boolean tmpIsVideoCallEnabled = ImsPhoneCallTracker.this.isVideoCallEnabled();
                    StringBuilder sb = new StringBuilder(RilDataCallFailCause.PDP_FAIL_IFACE_AND_POL_FAMILY_MISMATCH);
                    sb.append("onFeatureCapabilityChanged: ");
                    int i = 0;
                    while (i <= 5 && i < enabledFeatures.length) {
                        if (enabledFeatures[i] == i) {
                            sb.append(ImsPhoneCallTracker.this.mImsFeatureStrings[i]);
                            sb.append(":true ");
                            ImsPhoneCallTracker.this.mImsFeatureEnabled[i] = true;
                        } else if (enabledFeatures[i] == -1) {
                            sb.append(ImsPhoneCallTracker.this.mImsFeatureStrings[i]);
                            sb.append(":false ");
                            ImsPhoneCallTracker.this.mImsFeatureEnabled[i] = false;
                        } else {
                            ImsPhoneCallTracker.this.loge("onFeatureCapabilityChanged(" + i + ", " + ImsPhoneCallTracker.this.mImsFeatureStrings[i] + "): unexpectedValue=" + enabledFeatures[i]);
                        }
                        i++;
                    }
                    ImsPhoneCallTracker.this.log(sb.toString());
                    if (tmpIsVideoCallEnabled != ImsPhoneCallTracker.this.isVideoCallEnabled()) {
                        ImsPhoneCallTracker.this.mPhone.notifyForVideoCapabilityChanged(ImsPhoneCallTracker.this.isVideoCallEnabled());
                    }
                    ImsPhoneCallTracker.this.log("onFeatureCapabilityChanged: isVolteEnabled=" + ImsPhoneCallTracker.this.isVolteEnabled() + ", isVideoCallEnabled=" + ImsPhoneCallTracker.this.isVideoCallEnabled() + ", isVowifiEnabled=" + ImsPhoneCallTracker.this.isVowifiEnabled() + ", isUtEnabled=" + ImsPhoneCallTracker.this.isUtEnabled());
                    for (ImsPhoneConnection connection : ImsPhoneCallTracker.this.mConnections) {
                        connection.updateWifiState();
                    }
                    ImsPhoneCallTracker.this.mPhone.onFeatureCapabilityChanged();
                    ImsPhoneCallTracker.this.broadcastImsStatusChange();
                    ImsPhoneCallTracker.this.mMetrics.writeOnImsCapabilities(ImsPhoneCallTracker.this.mPhone.getPhoneId(), ImsPhoneCallTracker.this.mImsFeatureEnabled);
                }
            }

            public void onVoiceMessageCountChanged(int count) {
                ImsPhoneCallTracker.this.log("onVoiceMessageCountChanged :: count=" + count);
                ImsPhoneCallTracker.this.mPhone.mDefaultPhone.setVoiceMessageCount(count);
            }

            public void registrationAssociatedUriChanged(Uri[] uris) {
                ImsPhoneCallTracker.this.log("registrationAssociatedUriChanged");
                ImsPhoneCallTracker.this.mPhone.setCurrentSubscriberUris(uris);
            }

            public void onImsEmergencyCapabilityChanged(boolean eccSupport) {
                ImsPhoneCallTracker.this.log("onImsEmergencyCapabilityChanged :: eccSupport=" + eccSupport);
                ImsPhoneCallTracker.this.mPhone.onFeatureCapabilityChanged();
                ImsPhoneCallTracker.this.mImsSupportEcc = eccSupport;
            }
        };
        this.mImsConfigListener = new Stub() {
            public void onGetFeatureResponse(int feature, int network, int value, int status) {
            }

            public void onSetFeatureResponse(int feature, int network, int value, int status) {
                ImsPhoneCallTracker.this.mMetrics.writeImsSetFeatureValue(ImsPhoneCallTracker.this.mPhone.getPhoneId(), feature, network, value, status);
            }

            public void onGetVideoQuality(int status, int quality) {
            }

            public void onSetVideoQuality(int status) {
            }
        };
        this.mIndicationReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.android.ims.IMS_INCOMING_CALL_INDICATION")) {
                    ImsPhoneCallTracker.this.log("onReceive : indication call intent");
                    if (ImsPhoneCallTracker.this.mImsManager == null) {
                        ImsPhoneCallTracker.this.log("no ims manager");
                        return;
                    }
                    boolean isAllow = true;
                    int serviceId = intent.getIntExtra("android:imsServiceId", -1);
                    if (serviceId == ImsPhoneCallTracker.this.mServiceId) {
                        if (TelephonyManager.getTelephonyProperty(ImsPhoneCallTracker.this.mPhone.getPhoneId(), "persist.radio.terminal-based.cw", "disabled_tbcw").equals("enabled_tbcw_off") && ImsPhoneCallTracker.this.mPhone.isInCall()) {
                            ImsPhoneCallTracker.this.log("PROPERTY_TERMINAL_BASED_CALL_WAITING_MODE = TERMINAL_BASED_CALL_WAITING_ENABLED_OFF. Reject the call as UDUB ");
                            isAllow = false;
                        }
                        if (ImsPhoneCallTracker.this.isEccExist()) {
                            ImsPhoneCallTracker.this.log("there is an ECC call, dis-allow this incoming call!");
                            isAllow = false;
                        }
                        if (ImsPhoneCallTracker.this.hasVideoCallRestriction(context, intent)) {
                            isAllow = false;
                        }
                        ImsPhoneCallTracker.this.log("setCallIndication : serviceId = " + serviceId + ", intent = " + intent + ", isAllow = " + isAllow);
                        try {
                            ImsPhoneCallTracker.this.mImsManager.setCallIndication(ImsPhoneCallTracker.this.mServiceId, intent, isAllow);
                        } catch (ImsException e) {
                            ImsPhoneCallTracker.this.loge("setCallIndication ImsException " + e);
                        }
                    }
                }
            }
        };
        this.mPhone = phone;
        this.mMetrics = TelephonyMetrics.getInstance();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("com.android.ims.IMS_INCOMING_CALL");
        intentfilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        this.mPhone.getContext().registerReceiver(this.mReceiver, intentfilter);
        cacheCarrierConfiguration(this.mPhone.getSubId());
        registerIndicationReceiver();
        this.mPhone.getDefaultPhone().registerForDataEnabledChanged(this, 23, null);
        this.mImsServiceRetryCount = 0;
        sendEmptyMessage(24);
    }

    private PendingIntent createIncomingCallPendingIntent() {
        Intent intent = new Intent("com.android.ims.IMS_INCOMING_CALL");
        intent.addFlags(268435456);
        return PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
    }

    private void getImsService() throws ImsException {
        log("getImsService");
        synchronized (this.mSyncLock) {
            try {
                this.mImsManager = ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId());
                this.mServiceId = this.mImsManager.open(1, createIncomingCallPendingIntent(), this.mImsConnectionStateListener);
                this.mImsManager.setImsConfigListener(this.mImsConfigListener);
                getEcbmInterface().setEcbmStateListener(this.mPhone.getImsEcbmStateListener());
                if (this.mPhone.isInEcm()) {
                    this.mPhone.exitEmergencyCallbackMode();
                }
                this.mImsManager.setUiTTYMode(this.mPhone.getContext(), this.mServiceId, Secure.getInt(this.mPhone.getContext().getContentResolver(), "preferred_tty_mode", 0), null);
                ImsMultiEndpoint multiEndpoint = getMultiEndpointInterface();
                if (multiEndpoint != null) {
                    multiEndpoint.setExternalCallStateListener(this.mPhone.getExternalCallTracker().getExternalCallStateListener());
                }
            } catch (Exception e) {
                loge("getImsService: " + e);
                this.mImsManager = null;
            }
        }
        return;
    }

    public void dispose() {
        log("dispose");
        this.mRingingCall.dispose();
        this.mBackgroundCall.dispose();
        this.mForegroundCall.dispose();
        this.mHandoverCall.dispose();
        clearDisconnected();
        this.mPhone.getContext().unregisterReceiver(this.mReceiver);
        unregisterIndicationReceiver();
        synchronized (this.mSyncLock) {
            if (!(this.mImsManager == null || this.mServiceId == -1)) {
                try {
                    this.mImsManager.close(this.mServiceId);
                } catch (ImsException e) {
                    loge("getImsService: " + e);
                }
                this.mServiceId = -1;
                this.mImsManager = null;
            }
        }
        this.mPhone.setServiceState(1);
        this.mPhone.setImsRegistered(false);
        for (int i = 0; i <= 3; i++) {
            this.mImsFeatureEnabled[i] = false;
        }
        this.mPhone.onFeatureCapabilityChanged();
        broadcastImsStatusChange();
        this.mPhone.getDefaultPhone().unregisterForDataEnabledChanged(this);
        removeMessages(24);
        return;
    }

    protected void finalize() {
        log("ImsPhoneCallTracker finalized");
    }

    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        this.mVoiceCallStartedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceCallStarted(Handler h) {
        this.mVoiceCallStartedRegistrants.remove(h);
    }

    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        this.mVoiceCallEndedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceCallEnded(Handler h) {
        this.mVoiceCallEndedRegistrants.remove(h);
    }

    public Connection dial(String dialString, int videoState, Bundle intentExtras) throws CallStateException {
        return dial(dialString, PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).getInt(Phone.CLIR_KEY + this.mPhone.getPhoneId(), 0), videoState, intentExtras);
    }

    synchronized Connection dial(String dialString, int clirMode, int videoState, Bundle intentExtras) throws CallStateException {
        boolean isEmergencyNumber;
        boolean isPhoneInEcmMode = isPhoneInEcbMode();
        if (!hasC2kOverImsModem() || TelephonyManager.getDefault().hasIccCard(this.mPhone.getPhoneId())) {
            isEmergencyNumber = PhoneNumberUtils.isEmergencyNumber(this.mPhone.getSubId(), dialString);
        } else {
            isEmergencyNumber = PhoneNumberUtils.isEmergencyNumber(dialString);
        }
        log("dial clirMode=" + clirMode);
        clearDisconnected();
        if (this.mImsManager == null) {
            throw new CallStateException("service not available");
        } else if (this.mHandoverCall.mConnections.size() > 0) {
            log("SRVCC: there are connections during handover, trigger CSFB!");
            throw new CallStateException(Phone.CS_FALLBACK);
        } else if (this.mPhone != null && this.mPhone.getDefaultPhone() != null && this.mPhone.getDefaultPhone().getState() != State.IDLE && getState() == State.IDLE) {
            log("There are CS connections, trigger CSFB!");
            throw new CallStateException(Phone.CS_FALLBACK);
        } else if (canDial()) {
            if (isPhoneInEcmMode && isEmergencyNumber) {
                handleEcmTimer(1);
            }
            if (isEmergencyNumber && VideoProfile.isVideo(videoState) && !this.mAllowEmergencyVideoCalls) {
                loge("dial: carrier does not support video emergency calls; downgrade to audio-only");
                videoState = 0;
            }
            boolean holdBeforeDial = false;
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                if (this.mBackgroundCall.getState() != Call.State.IDLE) {
                    throw new CallStateException("cannot dial in current state");
                }
                holdBeforeDial = true;
                this.mPendingCallVideoState = videoState;
                this.mPendingIntentExtras = intentExtras;
                switchWaitingOrHoldingAndActive();
            }
            Call.State fgState = Call.State.IDLE;
            Call.State bgState = Call.State.IDLE;
            this.mClirMode = clirMode;
            synchronized (this.mSyncHold) {
                if (holdBeforeDial) {
                    fgState = this.mForegroundCall.getState();
                    bgState = this.mBackgroundCall.getState();
                    if (fgState == Call.State.ACTIVE) {
                        throw new CallStateException("cannot dial in current state");
                    } else if (bgState == Call.State.HOLDING) {
                        holdBeforeDial = false;
                    }
                }
                this.mPendingMO = new ImsPhoneConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, isEmergencyNumber);
                this.mPendingMO.setVideoState(videoState);
            }
            addConnection(this.mPendingMO);
            log("IMS: dial() holdBeforeDial = " + holdBeforeDial + " isPhoneInEcmMode = " + isPhoneInEcmMode + " isEmergencyNumber = " + isEmergencyNumber);
            logDebugMessagesWithOpFormat("CC", "Dial", this.mPendingMO, UsimPBMemInfo.STRING_NOT_SET);
            logDebugMessagesWithDumpFormat("CC", this.mPendingMO, UsimPBMemInfo.STRING_NOT_SET);
            if (!holdBeforeDial) {
                if (!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyNumber)) {
                    dialInternal(this.mPendingMO, clirMode, videoState, intentExtras);
                } else {
                    try {
                        getEcbmInterface().exitEmergencyCallbackMode();
                        this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                        this.pendingCallClirMode = clirMode;
                        this.mPendingCallVideoState = videoState;
                        this.pendingCallInEcm = true;
                    } catch (ImsException e) {
                        e.printStackTrace();
                        throw new CallStateException("service not available");
                    }
                }
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
        } else {
            throw new CallStateException("cannot dial in current state");
        }
        return this.mPendingMO;
    }

    private void cacheCarrierConfiguration(int subId) {
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
        this.mAllowEmergencyVideoCalls = carrierConfig.getBoolean("allow_emergency_video_calls_bool");
        this.mTreatDowngradedVideoCallsAsVideoCalls = carrierConfig.getBoolean("treat_downgraded_video_calls_as_video_calls_bool");
        this.mDropVideoCallWhenAnsweringAudioCall = carrierConfig.getBoolean("drop_video_call_when_answering_audio_call_bool");
        this.mAllowAddCallDuringVideoCall = carrierConfig.getBoolean("allow_add_call_during_video_call");
        this.mNotifyVtHandoverToWifiFail = carrierConfig.getBoolean("notify_vt_handover_to_wifi_failure_bool");
        this.mSupportDowngradeVtToAudio = carrierConfig.getBoolean("support_downgrade_vt_to_audio_bool");
        this.mNotifyHandoverVideoFromWifiToLTE = carrierConfig.getBoolean("notify_vt_handover_to_wifi_failure_bool");
        this.mIsNonDepOnData = carrierConfig.getBoolean("vilte_enable_not_dependent_on_data_enable_bool");
        String[] mappings = carrierConfig.getStringArray("ims_reasoninfo_mapping_string_array");
        if (mappings == null || mappings.length <= 0) {
            log("No carrier ImsReasonInfo mappings defined.");
        } else {
            for (String mapping : mappings) {
                String[] values = mapping.split(Pattern.quote("|"));
                if (values.length == 3) {
                    try {
                        int fromCode = Integer.parseInt(values[0]);
                        String message = values[1];
                        int toCode = Integer.parseInt(values[2]);
                        this.mImsReasonCodeMap.put(new Pair(Integer.valueOf(fromCode), message), Integer.valueOf(toCode));
                        log("Loaded ImsReasonInfo mapping : fromCode = " + fromCode + " ; message = " + message + " ; toCode = " + toCode);
                    } catch (NumberFormatException e) {
                        loge("Invalid ImsReasonInfo mapping found: " + mapping);
                    }
                }
            }
        }
    }

    private void handleEcmTimer(int action) {
        this.mPhone.handleTimerInEmergencyCallbackMode(action);
        switch (action) {
            case 0:
            case 1:
                return;
            default:
                log("handleEcmTimer, unsupported action " + action);
                return;
        }
    }

    private void dialInternal(ImsPhoneConnection conn, int clirMode, int videoState, Bundle intentExtras) {
        if (conn != null) {
            if (conn.getConfDialStrings() == null && (conn.getAddress() == null || conn.getAddress().length() == 0 || conn.getAddress().indexOf(78) >= 0)) {
                conn.setDisconnectCause(7);
                sendEmptyMessageDelayed(18, 500);
                return;
            }
            boolean isEmergencyNumber;
            setMute(false);
            if (!hasC2kOverImsModem() || TelephonyManager.getDefault().hasIccCard(this.mPhone.getPhoneId())) {
                isEmergencyNumber = PhoneNumberUtils.isEmergencyNumber(this.mPhone.getSubId(), conn.getAddress());
            } else {
                isEmergencyNumber = PhoneNumberUtils.isEmergencyNumber(conn.getAddress());
            }
            int serviceType = isEmergencyNumber ? 2 : 1;
            if (serviceType == 2 && PhoneNumberUtils.isSpecialEmergencyNumber(this.mPhone.getSubId(), conn.getAddress()) && !SubscriptionManager.isVsimEnabled(this.mPhone.getSubId())) {
                serviceType = 1;
            }
            if (this.mDialAsECC) {
                serviceType = 2;
                log("Dial as ECC: conn.getAddress(): " + conn.getAddress());
                this.mDialAsECC = false;
            }
            int callType = ImsCallProfile.getCallTypeFromVideoState(videoState);
            conn.setVideoState(videoState);
            try {
                String[] callees;
                if (conn.getConfDialStrings() != null) {
                    ArrayList<String> dialStrings = conn.getConfDialStrings();
                    callees = (String[]) dialStrings.toArray(new String[dialStrings.size()]);
                } else {
                    String[] callees2 = new String[1];
                    callees2[0] = conn.getAddress();
                    callees = callees2;
                }
                ImsCallProfile profile = this.mImsManager.createCallProfile(this.mServiceId, serviceType, callType);
                profile.setCallExtraInt("oir", clirMode);
                if (conn.getConfDialStrings() != null) {
                    profile.setCallExtraBoolean("conference", true);
                }
                if (intentExtras != null) {
                    if (intentExtras.containsKey("android.telecom.extra.CALL_SUBJECT")) {
                        intentExtras.putString("DisplayText", cleanseInstantLetteringMessage(intentExtras.getString("android.telecom.extra.CALL_SUBJECT")));
                    }
                    if (intentExtras.containsKey("CallPull")) {
                        profile.mCallExtras.putBoolean("CallPull", intentExtras.getBoolean("CallPull"));
                        int dialogId = intentExtras.getInt(ImsExternalCallTracker.EXTRA_IMS_EXTERNAL_CALL_ID);
                        conn.setIsPulledCall(true);
                        conn.setPulledDialogId(dialogId);
                    }
                    profile.mCallExtras.putBundle("OemCallExtras", intentExtras);
                }
                if (!(callees == null || callees.length != 1 || profile.getCallExtraBoolean("conference"))) {
                    profile.setCallExtra("oi", callees[0]);
                }
                ImsCall imsCall = this.mImsManager.makeCall(this.mServiceId, profile, callees, this.mImsCallListener);
                conn.setImsCall(imsCall);
                this.mMetrics.writeOnImsCallStart(this.mPhone.getPhoneId(), imsCall.getSession());
                setVideoCallProvider(conn, imsCall);
                conn.setAllowAddCallDuringVideoCall(this.mAllowAddCallDuringVideoCall);
            } catch (ImsException e) {
                loge("dialInternal : " + e);
                conn.setDisconnectCause(36);
                sendEmptyMessageDelayed(18, 500);
            } catch (RemoteException e2) {
            }
        }
    }

    public void acceptCall(int videoState) throws CallStateException {
        if (this.mSrvccState == SrvccState.STARTED) {
            throw new CallStateException(2, "cannot accept call: SRVCC");
        }
        logDebugMessagesWithOpFormat("CC", "Answer", this.mRingingCall.getFirstConnection(), UsimPBMemInfo.STRING_NOT_SET);
        log("acceptCall");
        if (this.mForegroundCall.getState().isAlive() && this.mBackgroundCall.getState().isAlive()) {
            throw new CallStateException("cannot accept call");
        } else if (this.mRingingCall.getState() == Call.State.WAITING && this.mForegroundCall.getState().isAlive()) {
            setMute(false);
            boolean answeringWillDisconnect = false;
            ImsCall activeCall = this.mForegroundCall.getImsCall();
            ImsCall ringingCall = this.mRingingCall.getImsCall();
            if (this.mForegroundCall.hasConnections() && this.mRingingCall.hasConnections()) {
                answeringWillDisconnect = shouldDisconnectActiveCallOnAnswer(activeCall, ringingCall);
            }
            this.mPendingCallVideoState = videoState;
            if (answeringWillDisconnect) {
                this.mForegroundCall.hangup();
                try {
                    ringingCall.accept(ImsCallProfile.getCallTypeFromVideoState(videoState));
                    return;
                } catch (ImsException e) {
                    throw new CallStateException("cannot accept call");
                }
            }
            switchWaitingOrHoldingAndActive();
        } else if (this.mRingingCall.getState().isRinging()) {
            log("acceptCall: incoming...");
            setMute(false);
            try {
                ImsCall imsCall = this.mRingingCall.getImsCall();
                if (imsCall != null) {
                    imsCall.accept(ImsCallProfile.getCallTypeFromVideoState(videoState));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 2);
                    return;
                }
                throw new CallStateException("no valid ims call");
            } catch (ImsException e2) {
                throw new CallStateException("cannot accept call");
            }
        } else {
            throw new CallStateException("phone not ringing");
        }
    }

    public void rejectCall() throws CallStateException {
        logDebugMessagesWithOpFormat("CC", "Reject", this.mRingingCall.getFirstConnection(), UsimPBMemInfo.STRING_NOT_SET);
        log("rejectCall");
        if (this.mRingingCall.getState().isRinging()) {
            hangup(this.mRingingCall);
            return;
        }
        throw new CallStateException("phone not ringing");
    }

    private void switchAfterConferenceSuccess() {
        log("switchAfterConferenceSuccess fg =" + this.mForegroundCall.getState() + ", bg = " + this.mBackgroundCall.getState());
        if (this.mBackgroundCall.getState() == Call.State.HOLDING) {
            log("switchAfterConferenceSuccess");
            this.mForegroundCall.switchWith(this.mBackgroundCall);
        }
    }

    public void switchWaitingOrHoldingAndActive() throws CallStateException {
        if (this.mSrvccState == SrvccState.STARTED) {
            throw new CallStateException(2, "cannot hold/unhold call: SRVCC");
        }
        ImsPhoneConnection conn;
        String msg;
        log("switchWaitingOrHoldingAndActive");
        if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
            conn = this.mForegroundCall.getFirstConnection();
            if (this.mBackgroundCall.getState().isAlive()) {
                msg = "switch with background connection:" + this.mBackgroundCall.getFirstConnection();
            } else {
                msg = "hold to background";
            }
        } else {
            conn = this.mBackgroundCall.getFirstConnection();
            msg = "unhold to foreground";
        }
        logDebugMessagesWithOpFormat("CC", "Swap", conn, msg);
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
            ImsCall imsCall = this.mForegroundCall.getImsCall();
            if (imsCall == null) {
                throw new CallStateException("no ims call");
            }
            boolean switchingWithWaitingCall = (this.mBackgroundCall.getImsCall() != null || this.mRingingCall == null) ? false : this.mRingingCall.getState() == Call.State.WAITING;
            this.mSwitchingFgAndBgCalls = true;
            if (switchingWithWaitingCall) {
                this.mCallExpectedToResume = this.mRingingCall.getImsCall();
            } else {
                this.mCallExpectedToResume = this.mBackgroundCall.getImsCall();
            }
            this.mForegroundCall.switchWith(this.mBackgroundCall);
            try {
                imsCall.hold();
                this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 5);
                if (this.mCallExpectedToResume == null) {
                    log("mCallExpectedToResume is null");
                    this.mSwitchingFgAndBgCalls = false;
                }
            } catch (ImsException e) {
                this.mForegroundCall.switchWith(this.mBackgroundCall);
                this.mSwitchingFgAndBgCalls = false;
                this.mCallExpectedToResume = null;
                throw new CallStateException(e.getMessage());
            }
        } else if (this.mBackgroundCall.getState() == Call.State.HOLDING) {
            resumeWaitingOrHolding();
        }
    }

    public void conference() {
        logDebugMessagesWithOpFormat("CC", "Conference", this.mForegroundCall.getFirstConnection(), " merge with " + this.mBackgroundCall.getFirstConnection());
        log("conference");
        ImsCall fgImsCall = this.mForegroundCall.getImsCall();
        if (fgImsCall == null) {
            log("conference no foreground ims call");
            return;
        }
        ImsCall bgImsCall = this.mBackgroundCall.getImsCall();
        if (bgImsCall == null) {
            log("conference no background ims call");
            return;
        }
        long conferenceConnectTime;
        long foregroundConnectTime = this.mForegroundCall.getEarliestConnectTime();
        long backgroundConnectTime = this.mBackgroundCall.getEarliestConnectTime();
        if (foregroundConnectTime > 0 && backgroundConnectTime > 0) {
            conferenceConnectTime = Math.min(this.mForegroundCall.getEarliestConnectTime(), this.mBackgroundCall.getEarliestConnectTime());
            log("conference - using connect time = " + conferenceConnectTime);
        } else if (foregroundConnectTime > 0) {
            log("conference - bg call connect time is 0; using fg = " + foregroundConnectTime);
            conferenceConnectTime = foregroundConnectTime;
        } else {
            log("conference - fg call connect time is 0; using bg = " + backgroundConnectTime);
            conferenceConnectTime = backgroundConnectTime;
        }
        ImsPhoneConnection foregroundConnection = this.mForegroundCall.getFirstConnection();
        if (foregroundConnection != null) {
            foregroundConnection.setConferenceConnectTime(conferenceConnectTime);
        }
        try {
            fgImsCall.merge(bgImsCall);
        } catch (ImsException e) {
            log("conference " + e.getMessage());
        }
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
                fgImsCall.explicitCallTransfer();
            } catch (ImsException e) {
                log("explicitCallTransfer " + e.getMessage());
            }
        } else {
            log("annot transfer call");
        }
    }

    public void unattendedCallTransfer(String number, int type) {
        if (!SENLOG || TELDBG) {
            log("unattendedCallTransfer number : " + number + ", type : " + type);
        } else {
            log("unattendedCallTransfer number : [hidden], type : " + type);
        }
        ImsCall fgImsCall = this.mForegroundCall.getImsCall();
        if (fgImsCall == null) {
            log("explicitCallTransfer no foreground ims call");
            return;
        }
        try {
            fgImsCall.unattendedCallTransfer(number, type);
        } catch (ImsException e) {
            log("explicitCallTransfer " + e.getMessage());
        }
    }

    public void clearDisconnected() {
        log("clearDisconnected");
        internalClearDisconnected();
        updatePhoneState();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public boolean canConference() {
        if (this.mForegroundCall.getState() != Call.State.ACTIVE || this.mBackgroundCall.getState() != Call.State.HOLDING || this.mBackgroundCall.isFull() || this.mForegroundCall.isFull()) {
            return false;
        }
        return true;
    }

    public boolean canDial() {
        int serviceState = this.mPhone.getServiceState().getState();
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        boolean ret = (serviceState == 3 || this.mPendingMO != null || this.mRingingCall.isRinging() || disableCall.equals("true")) ? false : this.mForegroundCall.getState().isAlive() ? !this.mBackgroundCall.getState().isAlive() : true;
        log("IMS: canDial() serviceState = " + serviceState + ", disableCall = " + disableCall + ", mPendingMO = " + this.mPendingMO + ", Is mRingingCall ringing = " + this.mRingingCall.isRinging() + ", Is mForegroundCall alive = " + this.mForegroundCall.getState().isAlive() + ", Is mBackgroundCall alive = " + this.mBackgroundCall.getState().isAlive());
        return ret;
    }

    public boolean canTransfer() {
        if (this.mForegroundCall.getState() == Call.State.ACTIVE && this.mBackgroundCall.getState() == Call.State.HOLDING) {
            return true;
        }
        return false;
    }

    private void internalClearDisconnected() {
        this.mRingingCall.clearDisconnected();
        this.mForegroundCall.clearDisconnected();
        this.mBackgroundCall.clearDisconnected();
        this.mHandoverCall.clearDisconnected();
    }

    private void updatePhoneState() {
        Object obj;
        State oldState = this.mState;
        boolean isPendingMOIdle = this.mPendingMO == null || !this.mPendingMO.getState().isAlive();
        if (this.mRingingCall.isRinging()) {
            this.mState = State.RINGING;
        } else if (isPendingMOIdle && this.mForegroundCall.isIdle() && this.mBackgroundCall.isIdle()) {
            this.mState = State.IDLE;
        } else {
            this.mState = State.OFFHOOK;
        }
        if (this.mState == State.IDLE && oldState != this.mState) {
            this.mVoiceCallEndedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        } else if (oldState == State.IDLE && oldState != this.mState) {
            this.mVoiceCallStartedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
        StringBuilder append = new StringBuilder().append("updatePhoneState pendingMo = ");
        if (this.mPendingMO == null) {
            obj = "null";
        } else {
            obj = this.mPendingMO.getState();
        }
        log(append.append(obj).append(", fg= ").append(this.mForegroundCall.getState()).append("(").append(this.mForegroundCall.getConnections().size()).append("), bg= ").append(this.mBackgroundCall.getState()).append("(").append(this.mBackgroundCall.getConnections().size()).append(")").toString());
        log("updatePhoneState oldState=" + oldState + ", newState=" + this.mState);
        if (this.mState != oldState) {
            OemConstant.setOemCallState(OemConstant.OEM_CHECK_CALL_STATE, oldState, this.mState);
            this.mPhone.notifyPhoneStateChanged();
            this.mMetrics.writePhoneState(this.mPhone.getPhoneId(), this.mState);
            notifyPhoneStateChanged(oldState, this.mState);
        }
    }

    private void handleRadioNotAvailable() {
        pollCallsWhenSafe();
    }

    private void dumpState() {
        int i;
        log("Phone State:" + this.mState);
        log("Ringing call: " + this.mRingingCall.toString());
        List l = this.mRingingCall.getConnections();
        int s = l.size();
        for (i = 0; i < s; i++) {
            log(l.get(i).toString());
        }
        log("Foreground call: " + this.mForegroundCall.toString());
        l = this.mForegroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i++) {
            log(l.get(i).toString());
        }
        log("Background call: " + this.mBackgroundCall.toString());
        l = this.mBackgroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i++) {
            log(l.get(i).toString());
        }
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        if (this.mImsManager == null) {
            this.mPhone.sendErrorResponse(onComplete, getImsManagerIsNullException());
            return;
        }
        try {
            this.mImsManager.setUiTTYMode(this.mPhone.getContext(), this.mServiceId, uiTtyMode, onComplete);
        } catch (ImsException e) {
            loge("setTTYMode : " + e);
            this.mPhone.sendErrorResponse(onComplete, e);
        }
    }

    public void setMute(boolean mute) {
        this.mDesiredMute = mute;
        this.mForegroundCall.setMute(mute);
    }

    public boolean getMute() {
        return this.mDesiredMute;
    }

    public void sendDtmf(char c, Message result) {
        log("sendDtmf");
        ImsCall imscall = this.mForegroundCall.getImsCall();
        if (imscall != null) {
            imscall.sendDtmf(c, result);
        }
    }

    public void startDtmf(char c) {
        log("startDtmf");
        ImsCall imscall = this.mForegroundCall.getImsCall();
        if (imscall != null) {
            imscall.startDtmf(c);
        } else {
            loge("startDtmf : no foreground call");
        }
    }

    public void stopDtmf() {
        log("stopDtmf");
        ImsCall imscall = this.mForegroundCall.getImsCall();
        if (imscall != null) {
            imscall.stopDtmf();
        } else {
            loge("stopDtmf : no foreground call");
        }
    }

    public void hangup(ImsPhoneConnection conn) throws CallStateException {
        if (this.mSrvccState == SrvccState.STARTED) {
            throw new CallStateException(2, "cannot hangup call: SRVCC");
        }
        log("hangup connection");
        if (conn.getOwner() != this) {
            throw new CallStateException("ImsPhoneConnection " + conn + "does not belong to ImsPhoneCallTracker " + this);
        }
        hangup(conn.getCall());
    }

    public void hangup(ImsPhoneCall call) throws CallStateException {
        if (this.mSrvccState == SrvccState.STARTED) {
            throw new CallStateException(2, "cannot hangup call: SRVCC");
        }
        log("hangup call");
        if (call.getConnections().size() == 0) {
            throw new CallStateException("no connections");
        }
        ImsCall imsCall = call.getImsCall();
        boolean rejectCall = false;
        if (call == this.mRingingCall) {
            log("(ringing) hangup incoming");
            rejectCall = true;
        } else if (call == this.mForegroundCall) {
            if (call.isDialingOrAlerting()) {
                log("(foregnd) hangup dialing or alerting...");
                if (this.mPendingHangupCall == null && this.mPendingMO != null && this.mPendingMO.getState().isDialing()) {
                    log("(foregnd) hangup dialing or alerting pending...");
                    this.mPendingHangupCall = call;
                    this.mPendingHangupAddr = this.mPendingMO.getAddress();
                    call.onHangupLocal();
                    sendEmptyMessageDelayed(101, 350);
                    return;
                }
            }
            log("(foregnd) hangup foreground");
        } else if (call == this.mBackgroundCall) {
            log("(backgnd) hangup waiting or background");
        } else {
            throw new CallStateException("ImsPhoneCall " + call + "does not belong to ImsPhoneCallTracker " + this);
        }
        call.onHangupLocal();
        if (imsCall != null) {
            if (rejectCall) {
                try {
                    imsCall.reject(501);
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 3);
                } catch (ImsException e) {
                    throw new CallStateException(e.getMessage());
                }
            }
            imsCall.terminate(501);
            this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 4);
        } else if (this.mPendingMO != null && call == this.mForegroundCall) {
            this.mPendingMO.update(null, Call.State.DISCONNECTED);
            this.mPendingMO.onDisconnect();
            removeConnection(this.mPendingMO);
            this.mPendingMO = null;
            updatePhoneState();
            removeMessages(20);
        }
        this.mPhone.notifyPreciseCallStateChanged();
    }

    void callEndCleanupHandOverCallIfAny() {
        if (this.mHandoverCall.mConnections.size() > 0) {
            log("callEndCleanupHandOverCallIfAny, mHandoverCall.mConnections=" + this.mHandoverCall.mConnections);
            for (Connection conn : this.mHandoverCall.mConnections) {
                log("SRVCC: remove connection=" + conn);
                removeConnection((ImsPhoneConnection) conn);
            }
            this.mHandoverCall.mConnections.clear();
            this.mConnections.clear();
            this.mState = State.IDLE;
            if (!(this.mPhone == null || this.mPhone.mDefaultPhone == null || this.mPhone.mDefaultPhone.getState() != State.IDLE)) {
                log("SRVCC: notify ImsPhone state as idle.");
                this.mPhone.notifyPhoneStateChanged();
            }
        }
        log("callEndCleanupHOCall, reset mSrvccState.");
        this.mSrvccState = SrvccState.NONE;
    }

    void resumeWaitingOrHolding() throws CallStateException {
        log("resumeWaitingOrHolding");
        try {
            ImsCall imsCall;
            if (this.mForegroundCall.getState().isAlive()) {
                imsCall = this.mForegroundCall.getImsCall();
                if (imsCall != null) {
                    imsCall.resume();
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 6);
                }
            } else if (this.mRingingCall.getState() != Call.State.WAITING) {
                imsCall = this.mBackgroundCall.getImsCall();
                if (imsCall != null) {
                    imsCall.resume();
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 6);
                }
            } else if (this.mHasPendingResumeRequest) {
                log("there is a pending resume background request, ignore accept()!");
            } else {
                imsCall = this.mRingingCall.getImsCall();
                if (imsCall != null) {
                    imsCall.accept(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 2);
                }
            }
        } catch (ImsException e) {
            throw new CallStateException(e.getMessage());
        }
    }

    public void sendUSSD(String ussdString, Message response) {
        log("sendUSSD");
        try {
            if (this.mUssdSession != null) {
                this.mUssdSession.sendUssd(ussdString);
                AsyncResult.forMessage(response, null, null);
                response.sendToTarget();
            } else if (this.mImsManager == null) {
                this.mPhone.sendErrorResponse(response, getImsManagerIsNullException());
            } else {
                String[] callees = new String[1];
                callees[0] = ussdString;
                ImsCallProfile profile = this.mImsManager.createCallProfile(this.mServiceId, 1, 2);
                profile.setCallExtraInt("dialstring", 2);
                this.mPendingUssd = response;
                this.mUssdSession = this.mImsManager.makeCall(this.mServiceId, profile, callees, this.mImsUssdListener);
            }
        } catch (ImsException e) {
            loge("sendUSSD : " + e);
            this.mPhone.sendErrorResponse(response, e);
        }
    }

    public void cancelUSSD() {
        if (this.mUssdSession != null) {
            try {
                this.mUssdSession.terminate(501);
            } catch (ImsException e) {
            }
        }
    }

    public void cancelUSSD(Message response) {
        if (this.mUssdSession != null) {
            this.mPendingUssd = response;
            try {
                this.mUssdSession.terminate(501);
            } catch (ImsException e) {
            }
        }
    }

    private synchronized ImsPhoneConnection findConnection(ImsCall imsCall) {
        for (ImsPhoneConnection conn : this.mConnections) {
            if (conn.getImsCall() == imsCall) {
                return conn;
            }
        }
        return null;
    }

    private synchronized void removeConnection(ImsPhoneConnection conn) {
        this.mConnections.remove(conn);
        if (this.mIsInEmergencyCall) {
            boolean isEmergencyCallInList = false;
            for (ImsPhoneConnection imsPhoneConnection : this.mConnections) {
                if (imsPhoneConnection != null && imsPhoneConnection.isEmergency()) {
                    isEmergencyCallInList = true;
                    break;
                }
            }
            if (!isEmergencyCallInList) {
                this.mIsInEmergencyCall = false;
                this.mPhone.sendEmergencyCallStateChange(false);
            }
        }
    }

    private synchronized void addConnection(ImsPhoneConnection conn) {
        this.mConnections.add(conn);
        if (conn.isEmergency()) {
            this.mIsInEmergencyCall = true;
            this.mPhone.sendEmergencyCallStateChange(true);
        }
    }

    private void processCallStateChange(ImsCall imsCall, Call.State state, int cause) {
        log("processCallStateChange " + imsCall + " state=" + state + " cause=" + cause);
        processCallStateChange(imsCall, state, cause, false);
    }

    private void processCallStateChange(ImsCall imsCall, Call.State state, int cause, boolean ignoreState) {
        log("processCallStateChange state=" + state + " cause=" + cause + " ignoreState=" + ignoreState);
        if (imsCall != null) {
            ImsPhoneConnection conn = findConnection(imsCall);
            if (conn != null) {
                conn.updateMediaCapabilities(imsCall);
                if (imsCall.isVideoCall() && !this.mLastDataEnabled) {
                    log("ImsCall updated to video call, retry onDataEnabledChanged");
                    sendEmptyMessage(26);
                }
                if (ignoreState) {
                    conn.updateAddressDisplay(imsCall);
                    conn.updateExtras(imsCall);
                    maybeSetVideoCallProvider(conn, imsCall);
                    return;
                }
                boolean changed = conn.update(imsCall, state);
                if (state == Call.State.DISCONNECTED) {
                    if (conn.onDisconnect(cause)) {
                        changed = true;
                    }
                    conn.getCall().detach(conn);
                    removeConnection(conn);
                    if (isNeedToResumeHoldCall(false)) {
                        resumeBackgroundCall();
                    }
                }
                logDebugMessagesWithDumpFormat("CC", conn, UsimPBMemInfo.STRING_NOT_SET);
                if (changed && conn.getCall() != this.mHandoverCall) {
                    updatePhoneState();
                    this.mPhone.notifyPreciseCallStateChanged();
                }
            }
        }
    }

    private void maybeSetVideoCallProvider(ImsPhoneConnection conn, ImsCall imsCall) {
        if (conn.getVideoProvider() == null && imsCall.getCallSession().getVideoCallProvider() != null) {
            try {
                setVideoCallProvider(conn, imsCall);
            } catch (RemoteException e) {
                loge("maybeSetVideoCallProvider: exception " + e);
            }
        }
    }

    private int maybeRemapReasonCode(ImsReasonInfo reasonInfo) {
        int code = reasonInfo.getCode();
        Pair<Integer, String> toCheck = new Pair(Integer.valueOf(code), reasonInfo.getExtraMessage());
        if (!this.mImsReasonCodeMap.containsKey(toCheck)) {
            return code;
        }
        int toCode = ((Integer) this.mImsReasonCodeMap.get(toCheck)).intValue();
        log("maybeRemapReasonCode : fromCode = " + reasonInfo.getCode() + " ; message = " + reasonInfo.getExtraMessage() + " ; toCode = " + toCode);
        return toCode;
    }

    private int getDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo) {
        switch (maybeRemapReasonCode(reasonInfo)) {
            case 0:
            case 339:
            case 510:
            case CharacterSets.UTF_16LE /*1014*/:
                return 2;
            case 106:
            case RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED_INFINITE_RETRY /*121*/:
            case RilDataCallFailCause.PDP_FAIL_AUTH_FAILURE_ON_EMERGENCY_CALL /*122*/:
            case 123:
            case 124:
            case 131:
            case 132:
            case 144:
                return 18;
            case 111:
            case 112:
                return 17;
            case 143:
                return 3;
            case PduHeaders.DATE_SENT /*201*/:
            case 202:
            case 203:
            case 335:
                return 13;
            case CallFailCause.FDN_BLOCKED /*241*/:
                return 21;
            case 321:
            case 331:
            case 340:
            case 361:
            case 362:
                return 12;
            case 329:
                return CallFailCause.IMS_EMERGENCY_REREG;
            case 332:
                return 12;
            case 333:
            case 352:
            case 354:
                return 9;
            case 337:
            case 341:
                return 8;
            case 338:
                return 4;
            case 501:
                return 3;
            case 905:
                return TIME_BETWEEN_IMS_SERVICE_RETRIES_MS;
            case 906:
                return 401;
            case 907:
                return 402;
            case 908:
                return 403;
            case CharacterSets.CESU_8 /*1016*/:
                return 51;
            case 1403:
                return 53;
            case 1404:
                return 16;
            case 1405:
                return 55;
            case 1406:
                return 54;
            case 1500:
                return 404;
            case 1501:
                return 405;
            default:
                return 36;
        }
    }

    private boolean isPhoneInEcbMode() {
        if (this.mPhone != null) {
            return Boolean.parseBoolean(TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "ril.cdma.inecmmode", "false"));
        }
        log("get isPhoneInEcbMode failed: mPhone = null");
        return false;
    }

    private void dialPendingMO() {
        boolean isPhoneInEcmMode = isPhoneInEcbMode();
        boolean isEmergencyNumber = this.mPendingMO.isEmergency();
        if (!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyNumber)) {
            sendEmptyMessage(20);
        } else {
            sendEmptyMessage(21);
        }
    }

    public boolean isSupportLteEcc() {
        return this.mImsSupportEcc;
    }

    public ImsUtInterface getUtInterface() throws ImsException {
        if (this.mImsManager != null) {
            return this.mImsManager.getSupplementaryServiceConfiguration(this.mServiceId);
        }
        throw getImsManagerIsNullException();
    }

    private void transferHandoverConnections(ImsPhoneCall call) {
        if (call.mConnections != null) {
            for (Connection c : call.mConnections) {
                boolean isConferenceHost;
                c.mPreHandoverState = call.mState;
                log("Connection state before handover is " + c.getStateBeforeHandover());
                c.mPreMultipartyState = c.isMultiparty();
                if (c instanceof ImsPhoneConnection) {
                    isConferenceHost = ((ImsPhoneConnection) c).isConferenceHost();
                } else {
                    isConferenceHost = false;
                }
                c.mPreMultipartyHostState = isConferenceHost;
                log("SRVCC: Connection isMultiparty is " + c.mPreMultipartyState + "and isConfHost is " + c.mPreMultipartyHostState + " before handover");
            }
        }
        if (this.mHandoverCall.mConnections == null) {
            this.mHandoverCall.mConnections = call.mConnections;
        } else {
            this.mHandoverCall.mConnections.addAll(call.mConnections);
        }
        if (this.mHandoverCall.mConnections != null) {
            if (call.getImsCall() != null) {
                call.getImsCall().close();
            }
            for (Connection c2 : this.mHandoverCall.mConnections) {
                ((ImsPhoneConnection) c2).changeParent(this.mHandoverCall);
                ((ImsPhoneConnection) c2).releaseWakeLock();
            }
        }
        if (call.getState().isAlive()) {
            log("Call is alive and state is " + call.mState);
            this.mHandoverCall.mState = call.mState;
        }
        call.mConnections.clear();
        call.mState = Call.State.IDLE;
        call.resetRingbackTone();
    }

    void notifySrvccState(SrvccState state) {
        log("notifySrvccState state=" + state);
        this.mSrvccState = state;
        if (this.mSrvccState == SrvccState.COMPLETED) {
            transferHandoverConnections(this.mForegroundCall);
            transferHandoverConnections(this.mBackgroundCall);
            transferHandoverConnections(this.mRingingCall);
            if (this.mPendingMO != null) {
                log("SRVCC: reset mPendingMO");
                removeConnection(this.mPendingMO);
                this.mPendingMO = null;
            }
            if (isImsCallHangupPending()) {
                log("SRVCC: there is still an pending hangup after SRVCC,remove the msg and hangup in GSM.");
                removeMessages(101);
            }
            updatePhoneState();
        }
    }

    public void handleMessage(Message msg) {
        log("handleMessage what=" + msg.what);
        AsyncResult ar;
        switch (msg.what) {
            case 14:
                if (this.pendingCallInEcm) {
                    dialInternal(this.mPendingMO, this.pendingCallClirMode, this.mPendingCallVideoState, this.mPendingIntentExtras);
                    this.mPendingIntentExtras = null;
                    this.pendingCallInEcm = false;
                }
                this.mPhone.unsetOnEcbModeExitResponse(this);
                return;
            case 18:
                if (this.mPendingMO != null) {
                    this.mPendingMO.onDisconnect();
                    removeConnection(this.mPendingMO);
                    this.mPendingMO = null;
                }
                this.mPendingIntentExtras = null;
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
                return;
            case 19:
                try {
                    resumeWaitingOrHolding();
                    return;
                } catch (CallStateException e) {
                    loge("handleMessage EVENT_RESUME_BACKGROUND exception=" + e);
                    return;
                }
            case 20:
                if (this.mPendingMO != null && this.mPendingMO.getImsCall() == null) {
                    dialInternal(this.mPendingMO, this.mClirMode, this.mPendingCallVideoState, this.mPendingIntentExtras);
                    this.mPendingIntentExtras = null;
                    return;
                }
                return;
            case 21:
                if (this.mPendingMO != null) {
                    try {
                        getEcbmInterface().exitEmergencyCallbackMode();
                        this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                        this.pendingCallClirMode = this.mClirMode;
                        this.pendingCallInEcm = true;
                        return;
                    } catch (ImsException e2) {
                        e2.printStackTrace();
                        this.mPendingMO.setDisconnectCause(36);
                        sendEmptyMessageDelayed(18, 500);
                        return;
                    }
                }
                return;
            case 22:
                ar = msg.obj;
                ImsCall call = ar.userObj;
                Long usage = Long.valueOf(((Long) ar.result).longValue());
                log("VT data usage update. usage = " + usage + ", imsCall = " + call);
                Long oldUsage = Long.valueOf(0);
                if (this.mVtDataUsageMap.containsKey(Integer.valueOf(call.uniqueId))) {
                    oldUsage = (Long) this.mVtDataUsageMap.get(Integer.valueOf(call.uniqueId));
                }
                this.mTotalVtDataUsage += usage.longValue() - oldUsage.longValue();
                this.mVtDataUsageMap.put(Integer.valueOf(call.uniqueId), usage);
                return;
            case 23:
                ar = (AsyncResult) msg.obj;
                if (ar.result instanceof Pair) {
                    Pair<Boolean, Integer> p = ar.result;
                    onDataEnabledChanged(((Boolean) p.first).booleanValue(), ((Integer) p.second).intValue());
                    return;
                }
                return;
            case 24:
                try {
                    getImsService();
                    return;
                } catch (ImsException e22) {
                    loge("getImsService: " + e22);
                    this.mImsManager = null;
                    if (this.mImsServiceRetryCount < 10) {
                        loge("getImsService: Retrying getting ImsService...");
                        sendEmptyMessageDelayed(24, 400);
                        this.mImsServiceRetryCount++;
                        return;
                    }
                    loge("getImsService: ImsService retrieval timeout... ImsService is unavailable.");
                    return;
                }
            case 25:
                if (msg.obj instanceof ImsCall) {
                    ImsCall imsCall = msg.obj;
                    if (!imsCall.isWifiCall()) {
                        ImsPhoneConnection conn = findConnection(imsCall);
                        if (conn != null) {
                            conn.onHandoverToWifiFailed();
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            case 26:
                onDataEnabledChanged(this.mLastDataEnabled, this.mLastDataEnabledReason);
                return;
            case 101:
                processPendingHangup("handler");
                return;
            case 900:
                loge("EVENT_AUTO_ANSWER:");
                try {
                    acceptCall(0);
                    return;
                } catch (Exception e3) {
                    loge("EVENT_AUTO_ANSWER: e " + e3);
                    return;
                }
            default:
                return;
        }
    }

    protected void log(String msg) {
        Rlog.d(LOG_TAG, "[ImsPhoneCallTracker] " + msg);
    }

    protected void loge(String msg) {
        Rlog.e(LOG_TAG, "[ImsPhoneCallTracker] " + msg);
    }

    void logState() {
        if (VERBOSE_STATE_LOGGING) {
            StringBuilder sb = new StringBuilder();
            sb.append("Current IMS PhoneCall State:\n");
            sb.append(" Foreground: ");
            sb.append(this.mForegroundCall);
            sb.append("\n");
            sb.append(" Background: ");
            sb.append(this.mBackgroundCall);
            sb.append("\n");
            sb.append(" Ringing: ");
            sb.append(this.mRingingCall);
            sb.append("\n");
            sb.append(" Handover: ");
            sb.append(this.mHandoverCall);
            sb.append("\n");
            Rlog.v(LOG_TAG, sb.toString());
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("ImsPhoneCallTracker extends:");
        super.dump(fd, pw, args);
        pw.println(" mVoiceCallEndedRegistrants=" + this.mVoiceCallEndedRegistrants);
        pw.println(" mVoiceCallStartedRegistrants=" + this.mVoiceCallStartedRegistrants);
        pw.println(" mRingingCall=" + this.mRingingCall);
        pw.println(" mForegroundCall=" + this.mForegroundCall);
        pw.println(" mBackgroundCall=" + this.mBackgroundCall);
        pw.println(" mHandoverCall=" + this.mHandoverCall);
        pw.println(" mPendingMO=" + this.mPendingMO);
        pw.println(" mPhone=" + this.mPhone);
        pw.println(" mDesiredMute=" + this.mDesiredMute);
        pw.println(" mState=" + this.mState);
        for (i = 0; i < this.mImsFeatureEnabled.length; i++) {
            pw.println(" " + this.mImsFeatureStrings[i] + ": " + (this.mImsFeatureEnabled[i] ? "enabled" : "disabled"));
        }
        pw.println(" mTotalVtDataUsage=" + this.mTotalVtDataUsage);
        for (Entry<Integer, Long> entry : this.mVtDataUsageMap.entrySet()) {
            pw.println("    id=" + entry.getKey() + " ,usage=" + entry.getValue());
        }
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        try {
            if (this.mImsManager != null) {
                this.mImsManager.dump(fd, pw, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mConnections != null && this.mConnections.size() > 0) {
            pw.println("mConnections:");
            for (i = 0; i < this.mConnections.size(); i++) {
                pw.println("  [" + i + "]: " + this.mConnections.get(i));
            }
        }
    }

    protected void handlePollCalls(AsyncResult ar) {
    }

    ImsEcbm getEcbmInterface() throws ImsException {
        if (this.mImsManager != null) {
            return this.mImsManager.getEcbmInterface(this.mServiceId);
        }
        throw getImsManagerIsNullException();
    }

    ImsMultiEndpoint getMultiEndpointInterface() throws ImsException {
        if (this.mImsManager == null) {
            throw getImsManagerIsNullException();
        }
        try {
            return this.mImsManager.getMultiEndpointInterface(this.mServiceId);
        } catch (ImsException e) {
            if (e.getCode() == 902) {
                return null;
            }
            throw e;
        }
    }

    public boolean isInEmergencyCall() {
        return this.mIsInEmergencyCall;
    }

    public boolean isVolteEnabled() {
        return this.mImsFeatureEnabled[0];
    }

    public boolean isVowifiEnabled() {
        return this.mImsFeatureEnabled[2];
    }

    public boolean isVideoCallEnabled() {
        if (this.mImsFeatureEnabled[1]) {
            return true;
        }
        return this.mImsFeatureEnabled[3];
    }

    public State getState() {
        return this.mState;
    }

    private void setVideoCallProvider(ImsPhoneConnection conn, ImsCall imsCall) throws RemoteException {
        IImsVideoCallProvider imsVideoCallProvider = imsCall.getCallSession().getVideoCallProvider();
        ImsVideoCallProviderWrapper oldImsVideoCallProvider = null;
        VideoProvider videoProvider = conn.getVideoProvider();
        if (videoProvider instanceof ImsVideoCallProviderWrapper) {
            oldImsVideoCallProvider = (ImsVideoCallProviderWrapper) videoProvider;
        }
        if (oldImsVideoCallProvider != null) {
            IImsVideoCallProvider oldIImsVideoCallProvider = oldImsVideoCallProvider.getProvider();
            if (!(oldIImsVideoCallProvider == null || imsVideoCallProvider == null)) {
                int oldProviderId = oldIImsVideoCallProvider.getProviderId();
                int newProviderId = imsVideoCallProvider.getProviderId();
                log("odl provider id = " + oldProviderId + ", new provider id = " + newProviderId);
                if (oldProviderId == newProviderId) {
                    loge("setVideoCallProvider(), not changed, ignore!!!");
                    return;
                }
            }
        }
        if (imsVideoCallProvider != null) {
            ImsVideoCallProviderWrapper imsVideoCallProviderWrapper = new ImsVideoCallProviderWrapper(imsVideoCallProvider);
            conn.setVideoProvider(imsVideoCallProviderWrapper);
            imsVideoCallProviderWrapper.registerForDataUsageUpdate(this, 22, imsCall);
            imsVideoCallProviderWrapper.addImsVideoProviderCallback(conn);
        }
    }

    public boolean isUtEnabled() {
        if (this.mImsFeatureEnabled[4]) {
            return true;
        }
        return this.mImsFeatureEnabled[5];
    }

    private String cleanseInstantLetteringMessage(String callSubject) {
        if (TextUtils.isEmpty(callSubject)) {
            return callSubject;
        }
        CarrierConfigManager configMgr = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configMgr == null) {
            return callSubject;
        }
        PersistableBundle carrierConfig = configMgr.getConfigForSubId(this.mPhone.getSubId());
        if (carrierConfig == null) {
            return callSubject;
        }
        String invalidCharacters = carrierConfig.getString("carrier_instant_lettering_invalid_chars_string");
        if (!TextUtils.isEmpty(invalidCharacters)) {
            callSubject = callSubject.replaceAll(invalidCharacters, UsimPBMemInfo.STRING_NOT_SET);
        }
        String escapedCharacters = carrierConfig.getString("carrier_instant_lettering_escaped_chars_string");
        if (!TextUtils.isEmpty(escapedCharacters)) {
            callSubject = escapeChars(escapedCharacters, callSubject);
        }
        return callSubject;
    }

    private String escapeChars(String toEscape, String source) {
        StringBuilder escaped = new StringBuilder();
        for (char c : source.toCharArray()) {
            if (toEscape.contains(Character.toString(c))) {
                escaped.append("\\");
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    public void pullExternalCall(String number, int videoState, int dialogId) {
        Bundle extras = new Bundle();
        extras.putBoolean("CallPull", true);
        extras.putInt(ImsExternalCallTracker.EXTRA_IMS_EXTERNAL_CALL_ID, dialogId);
        try {
            this.mPhone.notifyUnknownConnection(dial(number, videoState, extras));
        } catch (CallStateException e) {
            loge("pullExternalCall failed - " + e);
        }
    }

    private ImsException getImsManagerIsNullException() {
        return new ImsException("no ims manager", 102);
    }

    private boolean shouldDisconnectActiveCallOnAnswer(ImsCall activeCall, ImsCall incomingCall) {
        boolean z = false;
        if (!this.mDropVideoCallWhenAnsweringAudioCall) {
            return false;
        }
        boolean isVoWifiEnabled;
        boolean isActiveCallVideo = !activeCall.isVideoCall() ? this.mTreatDowngradedVideoCallsAsVideoCalls ? activeCall.wasVideoCall() : false : true;
        boolean isActiveCallOnWifi = activeCall.isWifiCall();
        ImsManager imsManager = this.mImsManager;
        if (ImsManager.isWfcEnabledByPlatform(this.mPhone.getContext())) {
            imsManager = this.mImsManager;
            isVoWifiEnabled = ImsManager.isWfcEnabledByUser(this.mPhone.getContext());
        } else {
            isVoWifiEnabled = false;
        }
        boolean isIncomingCallAudio = !incomingCall.isVideoCall();
        log("shouldDisconnectActiveCallOnAnswer : isActiveCallVideo=" + isActiveCallVideo + " isActiveCallOnWifi=" + isActiveCallOnWifi + " isIncomingCallAudio=" + isIncomingCallAudio + " isVowifiEnabled=" + isVoWifiEnabled);
        if (isActiveCallVideo && isActiveCallOnWifi && isIncomingCallAudio && !isVoWifiEnabled) {
            z = true;
        }
        return z;
    }

    public long getVtDataUsage() {
        if (this.mState != State.IDLE) {
            for (ImsPhoneConnection conn : this.mConnections) {
                VideoProvider videoProvider = conn.getVideoProvider();
                if (videoProvider != null) {
                    videoProvider.onRequestConnectionDataUsage();
                }
            }
        }
        return this.mTotalVtDataUsage;
    }

    public void registerPhoneStateListener(PhoneStateListener listener) {
        this.mPhoneStateListeners.add(listener);
    }

    public void unregisterPhoneStateListener(PhoneStateListener listener) {
        this.mPhoneStateListeners.remove(listener);
    }

    private void notifyPhoneStateChanged(State oldState, State newState) {
        for (PhoneStateListener listener : this.mPhoneStateListeners) {
            listener.onPhoneStateChanged(oldState, newState);
        }
    }

    private void modifyVideoCall(ImsCall imsCall, int newVideoState) {
        ImsPhoneConnection conn = findConnection(imsCall);
        if (conn != null) {
            int oldVideoState = conn.getVideoState();
            if (conn.getVideoProvider() != null) {
                conn.getVideoProvider().onSendSessionModifyRequest(new VideoProfile(oldVideoState), new VideoProfile(newVideoState));
            }
        }
    }

    private void onDataEnabledChanged(boolean enabled, int reason) {
        log("onDataEnabledChanged: enabled=" + enabled + ", reason=" + reason);
        ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId()).setDataEnabled(enabled);
        this.mLastDataEnabled = enabled;
        this.mLastDataEnabledReason = reason;
        if (this.mIsNonDepOnData) {
            loge("ignore onDataEnabledChanged");
            return;
        }
        if (!enabled) {
            int reasonCode;
            if (reason == 3) {
                reasonCode = 1405;
            } else if (reason == 2) {
                reasonCode = 1406;
            } else {
                reasonCode = 1406;
            }
            for (ImsPhoneConnection conn : this.mConnections) {
                ImsCall imsCall = conn.getImsCall();
                if (!(imsCall == null || !imsCall.isVideoCall() || imsCall.isWifiCall())) {
                    if (conn.hasCapabilities(3)) {
                        if (reasonCode == 1406) {
                            conn.onConnectionEvent("android.telephony.event.EVENT_DOWNGRADE_DATA_DISABLED", null);
                        } else if (reasonCode == 1405) {
                            conn.onConnectionEvent("android.telephony.event.EVENT_DOWNGRADE_DATA_LIMIT_REACHED", null);
                        }
                        modifyVideoCall(imsCall, 0);
                    } else {
                        try {
                            imsCall.terminate(501, reasonCode);
                        } catch (ImsException e) {
                            loge("Couldn't terminate call " + imsCall);
                        }
                    }
                }
            }
        }
        ImsManager.updateImsServiceConfig(this.mPhone.getContext(), this.mPhone.getPhoneId(), true);
    }

    private boolean isWifiConnected() {
        boolean z = true;
        ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnected()) {
                if (ni.getType() != 1) {
                    z = false;
                }
                return z;
            }
        }
        return false;
    }

    public boolean isCarrierDowngradeOfVtCallSupported() {
        return this.mSupportDowngradeVtToAudio;
    }

    private void registerIndicationReceiver() {
        log("registerIndicationReceiver");
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("com.android.ims.IMS_INCOMING_CALL_INDICATION");
        this.mPhone.getContext().registerReceiver(this.mIndicationReceiver, intentfilter);
    }

    private void unregisterIndicationReceiver() {
        log("unregisterIndicationReceiver");
        this.mPhone.setServiceState(1);
        this.mPhone.setImsRegistered(false);
        this.mImsRegistrationErrorCode = 0;
        broadcastImsStatusChange();
        this.mPhone.getContext().unregisterReceiver(this.mIndicationReceiver);
    }

    private boolean isEccExist() {
        ImsPhoneCall[] allCalls = new ImsPhoneCall[4];
        allCalls[0] = this.mForegroundCall;
        allCalls[1] = this.mBackgroundCall;
        allCalls[2] = this.mRingingCall;
        allCalls[3] = this.mHandoverCall;
        for (int i = 0; i < allCalls.length; i++) {
            if (allCalls[i].getState().isAlive()) {
                ImsCall imsCall = allCalls[i].getImsCall();
                if (imsCall != null) {
                    ImsCallProfile callProfile = imsCall.getCallProfile();
                    if (callProfile != null && callProfile.mServiceType == 2) {
                        return true;
                    }
                }
                continue;
            }
        }
        log("isEccExist(): no ECC!");
        return false;
    }

    private boolean hasVideoCallRestriction(Context context, Intent intent) {
        if (this.mPhone == null || !this.mPhone.isFeatureSupported(FeatureType.VIDEO_RESTRICTION)) {
            return false;
        }
        if (this.mForegroundCall.isIdle() && this.mBackgroundCall.isIdle()) {
            return false;
        }
        int hasVideoCall = 0;
        ImsPhoneConnection fgConn = this.mForegroundCall.getFirstConnection();
        ImsPhoneConnection bgConn = this.mBackgroundCall.getFirstConnection();
        if (fgConn != null) {
            hasVideoCall = VideoProfile.isVideo(fgConn.getVideoState());
        }
        if (bgConn != null) {
            hasVideoCall |= VideoProfile.isVideo(bgConn.getVideoState());
        }
        return hasVideoCall | isIncomingVideoCall(intent);
    }

    private void addCallLog(Context context, Intent intent) {
        int presentationMode;
        PhoneAccountHandle phoneAccountHandle = null;
        Iterator<PhoneAccountHandle> phoneAccounts = TelecomManager.from(context).getCallCapablePhoneAccounts().listIterator();
        while (phoneAccounts.hasNext()) {
            PhoneAccountHandle handle = (PhoneAccountHandle) phoneAccounts.next();
            String id = handle.getId();
            if (id != null && id.equals(this.mPhone.getDefaultPhone().getFullIccSerialNumber())) {
                log("iccid matches");
                phoneAccountHandle = handle;
                break;
            }
        }
        String number = intent.getStringExtra("android:imsDialString");
        if (number == null) {
            number = UsimPBMemInfo.STRING_NOT_SET;
        }
        if (number == null || number.equals(UsimPBMemInfo.STRING_NOT_SET)) {
            presentationMode = 2;
        } else {
            presentationMode = 1;
        }
        int features = 0;
        if (isIncomingVideoCall(intent)) {
            features = 1;
        }
        Calls.addCall(null, context, number, presentationMode, 3, features, phoneAccountHandle, new Date().getTime(), 0, new Long(0));
    }

    private boolean isIncomingVideoCall(Intent intent) {
        if (intent == null) {
            return false;
        }
        int callMode = intent.getIntExtra("android:imsCallMode", 0);
        if (callMode == 21 || callMode == 23 || callMode == 25) {
            return true;
        }
        return false;
    }

    Connection dial(List<String> numbers, int videoState) throws CallStateException {
        return dial((List) numbers, PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).getInt(Phone.CLIR_KEY + this.mPhone.getPhoneId(), 0), videoState);
    }

    synchronized Connection dial(List<String> numbers, int clirMode, int videoState) throws CallStateException {
        log("dial clirMode=" + clirMode);
        clearDisconnected();
        if (this.mImsManager == null) {
            throw new CallStateException("service not available");
        } else if (canDial()) {
            boolean holdBeforeDial = false;
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                if (this.mBackgroundCall.getState() != Call.State.IDLE) {
                    throw new CallStateException("cannot dial in current state");
                }
                holdBeforeDial = true;
                switchWaitingOrHoldingAndActive();
            }
            Call.State fgState = Call.State.IDLE;
            Call.State bgState = Call.State.IDLE;
            this.mClirMode = clirMode;
            synchronized (this.mSyncHold) {
                if (holdBeforeDial) {
                    fgState = this.mForegroundCall.getState();
                    bgState = this.mBackgroundCall.getState();
                    if (fgState == Call.State.ACTIVE) {
                        throw new CallStateException("cannot dial in current state");
                    } else if (bgState == Call.State.HOLDING) {
                        holdBeforeDial = false;
                    }
                }
                this.mPendingMO = new ImsPhoneConnection(this.mPhone, UsimPBMemInfo.STRING_NOT_SET, this, this.mForegroundCall, false);
                ArrayList<String> dialStrings = new ArrayList();
                for (String str : numbers) {
                    dialStrings.add(PhoneNumberUtils.extractNetworkPortionAlt(str));
                }
                this.mPendingMO.setConfDialStrings(dialStrings);
            }
            addConnection(this.mPendingMO);
            StringBuilder sb = new StringBuilder();
            for (String number : numbers) {
                sb.append(number);
                sb.append(", ");
            }
            logDebugMessagesWithOpFormat("CC", "DialConf", this.mPendingMO, " numbers=" + sb.toString());
            logDebugMessagesWithDumpFormat("CC", this.mPendingMO, UsimPBMemInfo.STRING_NOT_SET);
            if (!holdBeforeDial) {
                dialInternal(this.mPendingMO, clirMode, videoState, null);
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
        } else {
            throw new CallStateException("cannot dial in current state");
        }
        return this.mPendingMO;
    }

    void hangupAll() throws CallStateException {
        log("hangupAll");
        if (this.mImsManager == null) {
            throw new CallStateException("No ImsManager Instance");
        }
        try {
            this.mImsManager.hangupAllCall();
            if (!this.mRingingCall.isIdle()) {
                this.mRingingCall.onHangupLocal();
            }
            if (!this.mForegroundCall.isIdle()) {
                this.mForegroundCall.onHangupLocal();
            }
            if (!this.mBackgroundCall.isIdle()) {
                this.mBackgroundCall.onHangupLocal();
            }
        } catch (ImsException e) {
            throw new CallStateException(e.getMessage());
        }
    }

    private void broadcastImsStatusChange() {
        if (this.mPhone != null) {
            Intent intent = new Intent("com.android.ims.IMS_STATE_CHANGED");
            int serviceState = this.mPhone.getServiceState().getState();
            int errorCode = this.mImsRegistrationErrorCode;
            boolean[] enabledFeatures = this.mImsFeatureEnabled;
            log("broadcastImsStateChange state= " + serviceState + " errorCode= " + errorCode + " enabledFeatures= " + enabledFeatures);
            intent.putExtra("android:regState", serviceState);
            if (serviceState != 0 && errorCode > 0) {
                intent.putExtra("android:regError", errorCode);
            }
            String registerImsType = " ";
            if (isVolteEnabled()) {
                registerImsType = IMS_VOLTE_ENABLE;
            } else if (isVowifiEnabled()) {
                registerImsType = IMS_VOWIFI_ENABLE;
            }
            log("broadcastImsStatusChange: registerImsType = " + registerImsType + ",phoneId = " + this.mPhone.getPhoneId());
            if (SystemProperties.getInt("persist.radio.simswitch", 1) - 1 == this.mPhone.getPhoneId()) {
                SystemProperties.set(PRO_IMS_TYPE, registerImsType);
            }
            intent.putExtra("android:enablecap", enabledFeatures);
            intent.putExtra("android:phone_id", this.mPhone.getPhoneId());
            this.mPhone.getContext().sendBroadcast(intent);
        }
    }

    void logDebugMessagesWithOpFormat(String category, String action, ImsPhoneConnection conn, String msg) {
        if (category != null && action != null && conn != null) {
            FormattedLog formattedLog = new Builder().setCategory(category).setServiceName("ImsPhone").setOpType(OpType.OPERATION).setActionName(action).setCallNumber(getCallNumber(conn)).setCallId(getConnectionCallId(conn)).setExtraMessage(msg).buildDebugMsg();
            if (formattedLog != null && (!SENLOG || TELDBG)) {
                log(formattedLog.toString());
            }
        }
    }

    void logDebugMessagesWithDumpFormat(String category, ImsPhoneConnection conn, String msg) {
        if (category != null && conn != null) {
            String str;
            Builder statusInfo = new Builder().setCategory("CC").setServiceName("ImsPhone").setOpType(OpType.DUMP).setCallNumber(getCallNumber(conn)).setCallId(getConnectionCallId(conn)).setExtraMessage(msg).setStatusInfo("state", conn.getState().toString());
            String str2 = "isConfCall";
            if (conn.isMultiparty()) {
                str = "Yes";
            } else {
                str = "No";
            }
            FormattedLog formattedLog = statusInfo.setStatusInfo(str2, str).setStatusInfo("isConfChildCall", "No").setStatusInfo("parent", conn.getParentCallName()).buildDumpInfo();
            if (formattedLog != null && (!SENLOG || TELDBG)) {
                log(formattedLog.toString());
            }
        }
    }

    private String getConnectionCallId(ImsPhoneConnection conn) {
        if (conn == null) {
            return UsimPBMemInfo.STRING_NOT_SET;
        }
        int callId = conn.getCallId();
        if (callId == -1) {
            callId = conn.getCallIdBeforeDisconnected();
            if (callId == -1) {
                return UsimPBMemInfo.STRING_NOT_SET;
            }
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

    void unhold(ImsPhoneConnection conn) throws CallStateException {
        log("unhold connection");
        if (conn.getOwner() != this) {
            throw new CallStateException("ImsPhoneConnection " + conn + "does not belong to ImsPhoneCallTracker " + this);
        }
        unhold(conn.getCall());
    }

    private void unhold(ImsPhoneCall call) throws CallStateException {
        log("unhold call");
        if (call.getConnections().size() == 0) {
            throw new CallStateException("no connections");
        } else if (this.mIsOnCallResumed) {
            log("unhold call: drop unhold, an call is processing onCallResumed");
        } else {
            try {
                if (call == this.mBackgroundCall) {
                    log("unhold call: it is bg call, swap fg and bg");
                    this.mSwitchingFgAndBgCalls = true;
                    this.mCallExpectedToResume = this.mBackgroundCall.getImsCall();
                    this.mForegroundCall.switchWith(this.mBackgroundCall);
                } else if (call != this.mForegroundCall) {
                    log("unhold call which is neither background nor foreground call");
                    return;
                }
                if (this.mForegroundCall.getState().isAlive()) {
                    log("unhold call: foreground call is alive; try to resume it");
                    ImsCall imsCall = this.mForegroundCall.getImsCall();
                    if (imsCall != null) {
                        imsCall.resume();
                    }
                }
            } catch (ImsException e) {
                throw new CallStateException(e.getMessage());
            }
        }
    }

    private boolean isVendorDisconnectCauseNeeded(ImsReasonInfo reasonInfo) {
        if (reasonInfo == null) {
            return false;
        }
        int errorCode = reasonInfo.getCode();
        if (reasonInfo.getExtraMessage() == null) {
            log("isVendorDisconnectCauseNeeded = no due to empty errorMsg");
            return false;
        } else if (errorCode == 1500 || errorCode == 1501) {
            log("isVendorDisconnectCauseNeeded = yes, OP07 503/403 cases");
            return true;
        } else {
            log("isVendorDisconnectCauseNeeded = no, no matched case");
            return false;
        }
    }

    private boolean isNeedToResumeHoldCall(boolean handleWaitingCall) {
        Call.State foregroundCallState = this.mForegroundCall.getState();
        Call.State ringCallState = this.mRingingCall.getState();
        Call.State backCallState = this.mBackgroundCall.getState();
        log("isNeedToResumeHoldCall:" + backCallState);
        log("isNeedToResumeHoldCall, foregroundCallState" + foregroundCallState);
        log("ringCallState: " + ringCallState + ", mBackgroundCall.isIdle: " + this.mBackgroundCall.isIdle());
        if (handleWaitingCall) {
            if (this.mForegroundCall.isIdle() && foregroundCallState != Call.State.DISCONNECTING && !this.mBackgroundCall.isIdle() && backCallState == Call.State.HOLDING) {
                log("need to resume background Call automatically!");
                return true;
            }
        } else if (this.mForegroundCall.isIdle() && foregroundCallState != Call.State.DISCONNECTING && this.mRingingCall.isIdle() && ringCallState != Call.State.DISCONNECTING && !this.mBackgroundCall.isIdle() && backCallState == Call.State.HOLDING) {
            log("need to resume background Call automatically!");
            return true;
        }
        return false;
    }

    private void resumeBackgroundCall() {
        ImsCall imsCall = this.mBackgroundCall.getImsCall();
        log("resumeBackgroundCall: " + imsCall);
        if (imsCall != null) {
            try {
                imsCall.resume();
                return;
            } catch (ImsException e) {
                loge("resumeBackgroundCall: " + e);
                return;
            }
        }
        log("not backgound call need to be resume!!!");
    }

    private synchronized void processPendingHangup(String msg) {
        if (this.mPendingHangupCall != null) {
            log("processPendingHangup. for " + msg);
            removeMessages(101);
            try {
                ImsCall imsCall = this.mPendingHangupCall.getImsCall();
                if (imsCall != null) {
                    imsCall.terminate(501);
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 4);
                }
            } catch (Exception ex) {
                log("processPendingHangup. ex:" + ex.getMessage());
            }
            this.mPendingHangupCall = null;
            this.mPendingHangupAddr = null;
        }
        return;
    }
}

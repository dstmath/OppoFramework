package com.android.internal.telephony.imsphone;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telecom.ConferenceParticipant;
import android.telecom.Connection;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.telephony.CallQuality;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.emergency.EmergencyNumber;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsMmTelManager;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.ImsSuppServiceNotification;
import android.telephony.ims.ProvisioningManager;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.MmTelFeature;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import com.android.ims.ImsCall;
import com.android.ims.ImsConfigListener;
import com.android.ims.ImsEcbm;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsMultiEndpoint;
import com.android.ims.ImsUtInterface;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsVideoCallProvider;
import com.android.ims.internal.ImsVideoCallProviderWrapper;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.IOppoCallManager;
import com.android.internal.telephony.IOppoCallTracker;
import com.android.internal.telephony.LocaleTracker;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.metrics.CallQualityMetrics;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.uicc.AbstractSIMRecords;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.google.android.mms.pdu.CharacterSets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ImsPhoneCallTracker extends CallTracker implements ImsPullCall {
    protected static final boolean DBG = true;
    protected static final int EVENT_ANSWER_WAITING_CALL = 30;
    protected static final int EVENT_CHECK_FOR_WIFI_HANDOVER = 25;
    protected static final int EVENT_DATA_ENABLED_CHANGED = 23;
    protected static final int EVENT_DIAL_PENDINGMO = 20;
    private static final int EVENT_EXIT_ECBM_BEFORE_PENDINGMO = 21;
    protected static final int EVENT_HANGUP_PENDINGMO = 18;
    protected static final int EVENT_ON_FEATURE_CAPABILITY_CHANGED = 26;
    private static final int EVENT_REDIAL_WIFI_E911_CALL = 28;
    private static final int EVENT_REDIAL_WIFI_E911_TIMEOUT = 29;
    protected static final int EVENT_RESUME_NOW_FOREGROUND_CALL = 31;
    private static final int EVENT_SUPP_SERVICE_INDICATION = 27;
    private static final int EVENT_VT_DATA_USAGE_UPDATE = 22;
    private static final boolean FORCE_VERBOSE_STATE_LOGGING = false;
    protected static final int HANDOVER_TO_WIFI_TIMEOUT_MS = 60000;
    private static final String IMS_VOLTE_ENABLE = "volte";
    private static final String IMS_VOWIFI_ENABLE = "vowifi";
    static final String LOG_TAG = "ImsPhoneCallTracker";
    private static final int MAX_CALL_QUALITY_HISTORY = 10;
    static final int MAX_CONNECTIONS = 7;
    static final int MAX_CONNECTIONS_PER_CALL = 5;
    private static final String PRO_IMS_TYPE = "gsm.ims.type";
    protected static final int TIMEOUT_HANGUP_PENDINGMO = 500;
    private static final int TIMEOUT_PARTICIPANT_CONNECT_TIME_CACHE_MS = 60000;
    private static final int TIMEOUT_REDIAL_WIFI_E911_MS = 10000;
    /* access modifiers changed from: private */
    public static int TYPE_CALLIN = 0;
    private static int TYPE_CALLOUT = 1;
    private static int TYPE_PS = 4;
    private static int TYPE_SMSIN = 2;
    private static int TYPE_SMSOUT = 3;
    private static final boolean VERBOSE_STATE_LOGGING = Rlog.isLoggable(VERBOSE_STATE_TAG, 2);
    static final String VERBOSE_STATE_TAG = "IPCTState";
    private boolean isVowifiRegistered;
    protected boolean mAllowAddCallDuringVideoCall;
    @UnsupportedAppUsage
    protected boolean mAllowEmergencyVideoCalls;
    private boolean mAlwaysPlayRemoteHoldTone;
    /* access modifiers changed from: private */
    public boolean mAutoRetryFailedWifiEmergencyCall;
    @UnsupportedAppUsage
    public ImsPhoneCall mBackgroundCall;
    protected ImsCall mCallExpectedToResume;
    /* access modifiers changed from: private */
    public final Map<String, CallQualityMetrics> mCallQualityMetrics;
    /* access modifiers changed from: private */
    public final ConcurrentLinkedQueue<CallQualityMetrics> mCallQualityMetricsHistory;
    private boolean mCarrierConfigLoaded;
    protected int mClirMode;
    private final ProvisioningManager.Callback mConfigCallback;
    @UnsupportedAppUsage
    protected ArrayList<ImsPhoneConnection> mConnections;
    /* access modifiers changed from: private */
    public final AtomicInteger mDefaultDialerUid;
    private boolean mDesiredMute;
    private boolean mDropVideoCallWhenAnsweringAudioCall;
    @UnsupportedAppUsage
    public ImsPhoneCall mForegroundCall;
    @UnsupportedAppUsage
    public ImsPhoneCall mHandoverCall;
    /* access modifiers changed from: private */
    public boolean mHasAttemptedStartOfCallHandover;
    protected HoldSwapState mHoldSwitchingState;
    private boolean mIgnoreDataEnabledChangedForVideoCalls;
    protected ImsCall.Listener mImsCallListener;
    private final ImsMmTelManager.CapabilityCallback mImsCapabilityCallback;
    private ImsConfigListener.Stub mImsConfigListener;
    protected ImsManager mImsManager;
    protected final ImsManager.Connector mImsManagerConnector;
    private Map<Pair<Integer, String>, Integer> mImsReasonCodeMap;
    private final ImsMmTelManager.RegistrationCallback mImsRegistrationCallback;
    protected ImsCall.Listener mImsUssdListener;
    protected boolean mIsDataEnabled;
    private boolean mIsFirstDataEnabledChanged;
    private boolean mIsInEmergencyCall;
    private boolean mIsMonitoringConnectivity;
    protected boolean mIsViLteDataMetered;
    private boolean mIsWaitingCallExistedWhileHoldingStart;
    private PhoneInternalInterface.DialArgs mLastDialArgs;
    private String mLastDialString;
    protected TelephonyMetrics mMetrics;
    private MmTelFeature.MmTelCapabilities mMmTelCapabilities;
    private final MmTelFeatureListener mMmTelFeatureListener;
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    /* access modifiers changed from: private */
    public boolean mNotifyHandoverVideoFromLTEToWifi;
    protected boolean mNotifyHandoverVideoFromWifiToLTE;
    protected boolean mNotifyVtHandoverToWifiFail;
    @UnsupportedAppUsage
    protected int mOnHoldToneId;
    @UnsupportedAppUsage
    protected boolean mOnHoldToneStarted;
    protected int mPendingCallVideoState;
    protected Bundle mPendingIntentExtras;
    @UnsupportedAppUsage
    protected ImsPhoneConnection mPendingMO;
    @UnsupportedAppUsage
    protected Message mPendingUssd;
    @UnsupportedAppUsage
    public ImsPhone mPhone;
    private final Map<String, CacheEntry> mPhoneNumAndConnTime;
    private PhoneNumberUtilsProxy mPhoneNumberUtilsProxy;
    private List<PhoneStateListener> mPhoneStateListeners;
    protected BroadcastReceiver mReceiver;
    @UnsupportedAppUsage
    public ImsPhoneCall mRingingCall;
    private SharedPreferenceProxy mSharedPreferenceProxy;
    protected boolean mShouldUpdateImsConfigOnDisconnect;
    protected Call.SrvccState mSrvccState;
    protected PhoneConstants.State mState;
    private boolean mSupportDowngradeVtToAudio;
    /* access modifiers changed from: private */
    public boolean mSupportPauseVideo;
    @UnsupportedAppUsage
    protected boolean mSwitchingFgAndBgCalls;
    @UnsupportedAppUsage
    protected Object mSyncHold;
    private boolean mTreatDowngradedVideoCallsAsVideoCalls;
    private final Queue<CacheEntry> mUnknownPeerConnTime;
    protected ImsCall mUssdSession;
    protected ImsUtInterface mUtInterface;
    private RegistrantList mVoiceCallEndedRegistrants;
    private RegistrantList mVoiceCallStartedRegistrants;
    private final HashMap<Integer, Long> mVtDataUsageMap;
    private volatile NetworkStats mVtDataUsageSnapshot;
    private volatile NetworkStats mVtDataUsageUidSnapshot;
    protected int pendingCallClirMode;
    protected boolean pendingCallInEcm;

    protected enum HoldSwapState {
        INACTIVE,
        PENDING_SINGLE_CALL_HOLD,
        PENDING_SINGLE_CALL_UNHOLD,
        SWAPPING_ACTIVE_AND_HELD,
        HOLDING_TO_ANSWER_INCOMING,
        PENDING_RESUME_FOREGROUND_AFTER_FAILURE,
        HOLDING_TO_DIAL_OUTGOING
    }

    public interface PhoneNumberUtilsProxy {
        boolean isEmergencyNumber(String str);
    }

    public interface PhoneStateListener {
        void onPhoneStateChanged(PhoneConstants.State state, PhoneConstants.State state2);
    }

    public interface SharedPreferenceProxy {
        SharedPreferences getDefaultSharedPreferences(Context context);
    }

    private class MmTelFeatureListener extends MmTelFeature.Listener {
        private MmTelFeatureListener() {
        }

        public void onIncomingCall(IImsCallSession c, Bundle extras) {
            ImsCall activeCall;
            ImsPhoneCallTracker.this.log("onReceive : incoming call intent");
            if (ImsPhoneCallTracker.this.mImsManager != null) {
                try {
                    if (extras.getBoolean("android:ussd", false)) {
                        ImsPhoneCallTracker.this.log("onReceive : USSD");
                        ImsPhoneCallTracker.this.mUssdSession = ImsPhoneCallTracker.this.mImsManager.takeCall(c, extras, ImsPhoneCallTracker.this.mImsUssdListener);
                        if (ImsPhoneCallTracker.this.mUssdSession != null) {
                            ImsPhoneCallTracker.this.mUssdSession.accept(2);
                            return;
                        }
                        return;
                    }
                    boolean isUnknown = extras.getBoolean("android:isUnknown", false);
                    ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker.log("onReceive : isUnknown = " + isUnknown + " fg = " + ImsPhoneCallTracker.this.mForegroundCall.getState() + " bg = " + ImsPhoneCallTracker.this.mBackgroundCall.getState());
                    ImsCall imsCall = ImsPhoneCallTracker.this.takeCall(c, extras);
                    ImsPhoneCallTracker.this.handleAutoAnswer(ImsPhoneCallTracker.this.mPhone);
                    ImsPhoneCallTracker.this.handleCallinControl(imsCall);
                    synchronized (this) {
                        ImsPhoneConnection conn = ImsPhoneCallTracker.this.makeImsPhoneConnectionForMT(imsCall, isUnknown);
                        if (!(!ImsPhoneCallTracker.this.mForegroundCall.hasConnections() || (activeCall = ImsPhoneCallTracker.this.mForegroundCall.getFirstConnection().getImsCall()) == null || imsCall == null)) {
                            conn.setActiveCallDisconnectedOnAnswer(ImsPhoneCallTracker.this.shouldDisconnectActiveCallOnAnswer(activeCall, imsCall));
                        }
                        conn.setAllowAddCallDuringVideoCall(((ImsPhoneCallTracker) ImsPhoneCallTracker.this).mAllowAddCallDuringVideoCall);
                        ImsPhoneCallTracker.this.addConnection(conn);
                        ImsPhoneCallTracker.this.setVideoCallProvider(conn, imsCall);
                        ImsPhoneCallTracker.this.checkIncomingCallInRttEmcGuardTime(conn);
                        TelephonyMetrics.getInstance().writeOnImsCallReceive(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getSession());
                        IOppoCallManager oppoCallManager = (IOppoCallManager) OppoTelephonyFactory.getInstance().getFeature(IOppoCallManager.DEFAULT, new Object[0]);
                        if (!isUnknown) {
                            if (!(ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.IDLE && ImsPhoneCallTracker.this.mBackgroundCall.getState() == Call.State.IDLE)) {
                                conn.update(imsCall, Call.State.WAITING);
                            }
                            if (!oppoCallManager.isRestricted(ImsPhoneCallTracker.TYPE_CALLIN, ImsPhoneCallTracker.this.mPhone.getPhoneId())) {
                                ImsPhoneCallTracker.this.mPhone.notifyNewRingingConnection(conn);
                                ImsPhoneCallTracker.this.mPhone.notifyIncomingRing();
                            }
                        } else if (!oppoCallManager.isRestricted(ImsPhoneCallTracker.TYPE_CALLIN, ImsPhoneCallTracker.this.mPhone.getPhoneId())) {
                            ImsPhoneCallTracker.this.mPhone.notifyUnknownConnection(conn);
                        }
                        ImsPhoneCallTracker.this.updatePhoneState();
                        ImsPhoneCallTracker.this.mPhone.notifyPreciseCallStateChanged();
                    }
                    ImsPhoneCallTracker.this.mPhone.getDefaultPhone().getCallTracker().oemClearConn();
                } catch (ImsException e) {
                    ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker2.loge("onReceive : exception " + e);
                } catch (RemoteException e2) {
                }
            }
        }

        public void onVoiceMessageCountUpdate(int count) {
            if (ImsPhoneCallTracker.this.mPhone == null || ImsPhoneCallTracker.this.mPhone.mDefaultPhone == null) {
                ImsPhoneCallTracker.this.loge("onVoiceMessageCountUpdate: null phone");
                return;
            }
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onVoiceMessageCountChanged :: count=" + count);
            ImsPhoneCallTracker.this.mPhone.mDefaultPhone.setVoiceMessageCount(count);
        }
    }

    /* access modifiers changed from: private */
    public static class CacheEntry {
        /* access modifiers changed from: private */
        public long mCachedTime;
        /* access modifiers changed from: private */
        public int mCallDirection;
        /* access modifiers changed from: private */
        public long mConnectElapsedTime;
        /* access modifiers changed from: private */
        public long mConnectTime;

        CacheEntry(long cachedTime, long connectTime, long connectElapsedTime, int callDirection) {
            this.mCachedTime = cachedTime;
            this.mConnectTime = connectTime;
            this.mConnectElapsedTime = connectElapsedTime;
            this.mCallDirection = callDirection;
        }
    }

    public ImsPhoneCallTracker(ImsPhone phone) {
        this(phone, phone.getContext().getMainExecutor());
    }

    @VisibleForTesting
    public ImsPhoneCallTracker(ImsPhone phone, Executor executor) {
        this.mIsFirstDataEnabledChanged = true;
        this.mIsWaitingCallExistedWhileHoldingStart = true;
        this.mMmTelCapabilities = new MmTelFeature.MmTelCapabilities();
        this.isVowifiRegistered = false;
        this.mCallQualityMetrics = new ConcurrentHashMap();
        this.mCallQualityMetricsHistory = new ConcurrentLinkedQueue<>();
        this.mCarrierConfigLoaded = false;
        this.mMmTelFeatureListener = new MmTelFeatureListener();
        this.mReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.imsphone.ImsPhoneCallTracker.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    int subId = intent.getIntExtra("subscription", -1);
                    if (subId == ImsPhoneCallTracker.this.mPhone.getSubId()) {
                        ImsPhoneCallTracker.this.cacheCarrierConfiguration(subId);
                        ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                        imsPhoneCallTracker.log("onReceive : Updating mAllowEmergencyVideoCalls = " + ImsPhoneCallTracker.this.mAllowEmergencyVideoCalls);
                    }
                } else if ("android.telecom.action.CHANGE_DEFAULT_DIALER".equals(intent.getAction())) {
                    ImsPhoneCallTracker.this.mDefaultDialerUid.set(ImsPhoneCallTracker.this.getPackageUid(context, intent.getStringExtra("android.telecom.extra.CHANGE_DEFAULT_DIALER_PACKAGE_NAME")));
                }
            }
        };
        this.mIsMonitoringConnectivity = false;
        this.mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            /* class com.android.internal.telephony.imsphone.ImsPhoneCallTracker.AnonymousClass2 */

            public void onAvailable(Network network) {
                Rlog.i(ImsPhoneCallTracker.LOG_TAG, "Network available: " + network);
                ImsPhoneCallTracker.this.scheduleHandoverCheck();
            }
        };
        this.mConnections = new ArrayList<>();
        this.mVoiceCallEndedRegistrants = new RegistrantList();
        this.mVoiceCallStartedRegistrants = new RegistrantList();
        this.mRingingCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_RINGING);
        this.mForegroundCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_FOREGROUND);
        this.mBackgroundCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_BACKGROUND);
        this.mHandoverCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_HANDOVER);
        this.mVtDataUsageMap = new HashMap<>();
        this.mPhoneNumAndConnTime = new ConcurrentHashMap();
        this.mUnknownPeerConnTime = new LinkedBlockingQueue();
        this.mVtDataUsageSnapshot = null;
        this.mVtDataUsageUidSnapshot = null;
        this.mDefaultDialerUid = new AtomicInteger(-1);
        this.mClirMode = 0;
        this.mSyncHold = new Object();
        this.mUssdSession = null;
        this.mPendingUssd = null;
        this.mDesiredMute = false;
        this.mOnHoldToneStarted = false;
        this.mOnHoldToneId = -1;
        this.mState = PhoneConstants.State.IDLE;
        this.mSrvccState = Call.SrvccState.NONE;
        this.mIsInEmergencyCall = false;
        this.mIsDataEnabled = false;
        this.pendingCallInEcm = false;
        this.mSwitchingFgAndBgCalls = false;
        this.mCallExpectedToResume = null;
        this.mAllowEmergencyVideoCalls = false;
        this.mIgnoreDataEnabledChangedForVideoCalls = false;
        this.mIsViLteDataMetered = false;
        this.mAlwaysPlayRemoteHoldTone = false;
        this.mAutoRetryFailedWifiEmergencyCall = false;
        this.mHoldSwitchingState = HoldSwapState.INACTIVE;
        this.mLastDialString = null;
        this.mLastDialArgs = null;
        this.mPhoneStateListeners = new ArrayList();
        this.mTreatDowngradedVideoCallsAsVideoCalls = false;
        this.mDropVideoCallWhenAnsweringAudioCall = false;
        this.mAllowAddCallDuringVideoCall = true;
        this.mNotifyVtHandoverToWifiFail = false;
        this.mSupportDowngradeVtToAudio = false;
        this.mNotifyHandoverVideoFromWifiToLTE = false;
        this.mNotifyHandoverVideoFromLTEToWifi = false;
        this.mHasAttemptedStartOfCallHandover = false;
        this.mSupportPauseVideo = false;
        this.mImsReasonCodeMap = new ArrayMap();
        this.mShouldUpdateImsConfigOnDisconnect = false;
        this.mSharedPreferenceProxy = $$Lambda$ImsPhoneCallTracker$Zw03itjXT6LrhiYuD9nKFg2Wg.INSTANCE;
        this.mPhoneNumberUtilsProxy = $$Lambda$ImsPhoneCallTracker$QlPVd_3u4_verjHUDnkn6zaSe54.INSTANCE;
        this.mImsCallListener = new ImsCall.Listener() {
            /* class com.android.internal.telephony.imsphone.ImsPhoneCallTracker.AnonymousClass4 */

            public void onCallProgressing(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallProgressing");
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.mPendingMO = null;
                imsPhoneCallTracker.log("oppo set mPendingMO = null 2");
                ImsPhoneCallTracker.this.processPendingHangup("onCallProgressing");
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.ALERTING, 0);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallProgressing(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
            }

            public void onCallStarted(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallStarted");
                if (ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.HOLDING_TO_ANSWER_INCOMING && ImsPhoneCallTracker.this.mCallExpectedToResume != null && ImsPhoneCallTracker.this.mCallExpectedToResume == imsCall) {
                    ImsPhoneCallTracker.this.log("onCallStarted: starting a call as a result of a switch.");
                    ImsPhoneCallTracker.this.mHoldSwitchingState = HoldSwapState.INACTIVE;
                    ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker.mCallExpectedToResume = null;
                    imsPhoneCallTracker.logHoldSwapState("onCallStarted");
                }
                ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
                imsPhoneCallTracker2.mPendingMO = null;
                imsPhoneCallTracker2.processCallStateChange(imsCall, Call.State.ACTIVE, 0);
                if (ImsPhoneCallTracker.this.mNotifyVtHandoverToWifiFail && ImsPhoneCallTracker.this.isVideoCall(imsCall) && !imsCall.isWifiCall()) {
                    if (ImsPhoneCallTracker.this.isWifiConnected()) {
                        ImsPhoneCallTracker imsPhoneCallTracker3 = ImsPhoneCallTracker.this;
                        imsPhoneCallTracker3.sendMessageDelayed(imsPhoneCallTracker3.obtainMessage(25, imsCall), 60000);
                        boolean unused = ImsPhoneCallTracker.this.mHasAttemptedStartOfCallHandover = false;
                    } else {
                        ImsPhoneCallTracker.this.registerForConnectivityChanges();
                        boolean unused2 = ImsPhoneCallTracker.this.mHasAttemptedStartOfCallHandover = true;
                    }
                }
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallStarted(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
            }

            public void onCallUpdated(ImsCall imsCall) {
                ImsPhoneConnection conn;
                ImsPhoneCallTracker.this.log("onCallUpdated");
                if (imsCall != null && (conn = ImsPhoneCallTracker.this.findConnection(imsCall)) != null) {
                    ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker.log("onCallUpdated: profile is " + imsCall.getCallProfile());
                    ImsPhoneCallTracker.this.processCallStateChange(imsCall, conn.getCall().mState, 0, true);
                    ImsPhoneCallTracker.this.mMetrics.writeImsCallState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), conn.getCall().mState);
                }
            }

            public void onCallStartFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onCallStartFailed reasonCode=" + reasonInfo.getCode());
                if (ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.HOLDING_TO_ANSWER_INCOMING && ImsPhoneCallTracker.this.mCallExpectedToResume != null && ImsPhoneCallTracker.this.mCallExpectedToResume == imsCall) {
                    ImsPhoneCallTracker.this.log("onCallStarted: starting a call as a result of a switch.");
                    ImsPhoneCallTracker.this.mHoldSwitchingState = HoldSwapState.INACTIVE;
                    ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker2.mCallExpectedToResume = null;
                    imsPhoneCallTracker2.logHoldSwapState("onCallStartFailed");
                }
                ImsPhoneCallTracker.this.processPendingHangup("onCallStartFailed");
                if (ImsPhoneCallTracker.this.mPendingMO == null) {
                    return;
                }
                if (reasonInfo.getCode() == 146 && ImsPhoneCallTracker.this.mBackgroundCall.getState() == Call.State.IDLE && ImsPhoneCallTracker.this.mRingingCall.getState() == Call.State.IDLE) {
                    ImsPhoneCallTracker.this.mForegroundCall.detach(ImsPhoneCallTracker.this.mPendingMO);
                    ImsPhoneCallTracker imsPhoneCallTracker3 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker3.removeConnection(imsPhoneCallTracker3.mPendingMO);
                    ImsPhoneCallTracker.this.mPendingMO.finalize();
                    ImsPhoneCallTracker imsPhoneCallTracker4 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker4.mPendingMO = null;
                    imsPhoneCallTracker4.mPhone.initiateSilentRedial();
                    return;
                }
                ImsPhoneCallTracker.this.sendCallStartFailedDisconnect(imsCall, reasonInfo);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallStartFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
            }

            public void onCallTerminated(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                Call.State callState;
                String str;
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onCallTerminated reasonCode=" + reasonInfo.getCode());
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    callState = conn.getState();
                } else {
                    callState = Call.State.ACTIVE;
                }
                int cause = ImsPhoneCallTracker.this.getDisconnectCauseFromReasonInfo(reasonInfo, callState);
                ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
                imsPhoneCallTracker2.log("cause = " + cause + " conn = " + conn);
                if (conn != null) {
                    Connection.VideoProvider videoProvider = conn.getVideoProvider();
                    if (videoProvider instanceof ImsVideoCallProviderWrapper) {
                        ImsVideoCallProviderWrapper wrapper = (ImsVideoCallProviderWrapper) videoProvider;
                        wrapper.unregisterForDataUsageUpdate(ImsPhoneCallTracker.this);
                        wrapper.removeImsVideoProviderCallback(conn);
                    }
                }
                if (ImsPhoneCallTracker.this.mOnHoldToneId == System.identityHashCode(conn)) {
                    if (conn != null && ImsPhoneCallTracker.this.mOnHoldToneStarted) {
                        ImsPhoneCallTracker.this.mPhone.stopOnHoldTone(conn);
                    }
                    ImsPhoneCallTracker imsPhoneCallTracker3 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker3.mOnHoldToneStarted = false;
                    imsPhoneCallTracker3.mOnHoldToneId = -1;
                }
                if (conn != null) {
                    if (conn.isPulledCall() && ((reasonInfo.getCode() == 1015 || reasonInfo.getCode() == 336 || reasonInfo.getCode() == 332) && ImsPhoneCallTracker.this.mPhone != null && ImsPhoneCallTracker.this.mPhone.getExternalCallTracker() != null)) {
                        ImsPhoneCallTracker.this.log("Call pull failed.");
                        conn.onCallPullFailed(ImsPhoneCallTracker.this.mPhone.getExternalCallTracker().getConnectionById(conn.getPulledDialogId()));
                        cause = 0;
                    } else if (conn.isIncoming() && conn.getConnectTime() == 0 && cause != 52) {
                        if (cause == 2) {
                            cause = 1;
                        } else {
                            cause = 16;
                        }
                        ImsPhoneCallTracker imsPhoneCallTracker4 = ImsPhoneCallTracker.this;
                        imsPhoneCallTracker4.log("Incoming connection of 0 connect time detected - translated cause = " + cause);
                    }
                }
                if (cause == 2 && conn != null && conn.getImsCall().isMerged()) {
                    cause = 45;
                }
                int cause2 = ImsPhoneCallTracker.this.updateDisconnectCause(cause, conn);
                ImsPhoneCallTracker.this.setRedialAsEcc(cause2);
                ImsPhoneCallTracker.this.setVendorDisconnectCause(conn, reasonInfo);
                String callId = imsCall.getSession().getCallId();
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallTerminated(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo, (CallQualityMetrics) ImsPhoneCallTracker.this.mCallQualityMetrics.get(callId), conn.getEmergencyNumberInfo(), ImsPhoneCallTracker.this.getNetworkCountryIso());
                CallQualityMetrics lastCallMetrics = (CallQualityMetrics) ImsPhoneCallTracker.this.mCallQualityMetrics.remove(callId);
                if (lastCallMetrics != null) {
                    ImsPhoneCallTracker.this.mCallQualityMetricsHistory.add(lastCallMetrics);
                }
                ImsPhoneCallTracker.this.pruneCallQualityMetricsHistory();
                ImsPhoneCallTracker.this.mPhone.notifyImsReason(reasonInfo);
                if (reasonInfo.getCode() != 1514 || !ImsPhoneCallTracker.this.mAutoRetryFailedWifiEmergencyCall) {
                    ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.DISCONNECTED, cause2);
                    if (ImsPhoneCallTracker.this.mForegroundCall.getState() != Call.State.ACTIVE && ImsPhoneCallTracker.this.mRingingCall.getState().isRinging()) {
                        ImsPhoneCallTracker.this.mPendingMO = null;
                    }
                    if (ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.SWAPPING_ACTIVE_AND_HELD) {
                        ImsPhoneCallTracker.this.log("onCallTerminated: Call terminated in the midst of Switching Fg and Bg calls.");
                        if (imsCall == ImsPhoneCallTracker.this.mCallExpectedToResume) {
                            ImsPhoneCallTracker imsPhoneCallTracker5 = ImsPhoneCallTracker.this;
                            imsPhoneCallTracker5.log("onCallTerminated: switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                            ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                        }
                        ImsPhoneCallTracker imsPhoneCallTracker6 = ImsPhoneCallTracker.this;
                        StringBuilder sb = new StringBuilder();
                        sb.append("onCallTerminated: foreground call in state ");
                        sb.append(ImsPhoneCallTracker.this.mForegroundCall.getState());
                        sb.append(" and ringing call in state ");
                        if (ImsPhoneCallTracker.this.mRingingCall == null) {
                            str = "null";
                        } else {
                            str = ImsPhoneCallTracker.this.mRingingCall.getState().toString();
                        }
                        sb.append(str);
                        imsPhoneCallTracker6.log(sb.toString());
                        if (ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING) {
                            ImsPhoneCallTracker.this.sendEmptyMessage(31);
                            ImsPhoneCallTracker.this.mHoldSwitchingState = HoldSwapState.INACTIVE;
                            ImsPhoneCallTracker imsPhoneCallTracker7 = ImsPhoneCallTracker.this;
                            imsPhoneCallTracker7.mCallExpectedToResume = null;
                            imsPhoneCallTracker7.logHoldSwapState("onCallTerminated swap active and hold case");
                        }
                    } else if (ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.PENDING_SINGLE_CALL_UNHOLD || ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.PENDING_SINGLE_CALL_HOLD) {
                        ImsPhoneCallTracker imsPhoneCallTracker8 = ImsPhoneCallTracker.this;
                        imsPhoneCallTracker8.mCallExpectedToResume = null;
                        imsPhoneCallTracker8.mHoldSwitchingState = HoldSwapState.INACTIVE;
                        ImsPhoneCallTracker.this.logHoldSwapState("onCallTerminated single call case");
                    } else if (ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.HOLDING_TO_ANSWER_INCOMING) {
                        if (imsCall == ImsPhoneCallTracker.this.mCallExpectedToResume) {
                            ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                            ImsPhoneCallTracker imsPhoneCallTracker9 = ImsPhoneCallTracker.this;
                            imsPhoneCallTracker9.mCallExpectedToResume = null;
                            imsPhoneCallTracker9.mHoldSwitchingState = HoldSwapState.INACTIVE;
                            ImsPhoneCallTracker.this.logHoldSwapState("onCallTerminated hold to answer case");
                            ImsPhoneCallTracker.this.sendEmptyMessage(31);
                        }
                    } else if (ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.HOLDING_TO_DIAL_OUTGOING) {
                        if (ImsPhoneCallTracker.this.mPendingMO == null || ImsPhoneCallTracker.this.mPendingMO.getDisconnectCause() != 0) {
                            ImsPhoneCallTracker.this.mHoldSwitchingState = HoldSwapState.INACTIVE;
                            ImsPhoneCallTracker.this.logHoldSwapState("onCallTerminated hold to dial but no pendingMo");
                        } else if (ImsPhoneCallTracker.this.canDailOnCallTerminated() && imsCall != ImsPhoneCallTracker.this.mPendingMO.getImsCall()) {
                            ImsPhoneCallTracker.this.sendEmptyMessage(20);
                            ImsPhoneCallTracker.this.mHoldSwitchingState = HoldSwapState.INACTIVE;
                            ImsPhoneCallTracker.this.logHoldSwapState("onCallTerminated hold to dial, dial pendingMo");
                        }
                    }
                    if (ImsPhoneCallTracker.this.mShouldUpdateImsConfigOnDisconnect) {
                        if (ImsPhoneCallTracker.this.mImsManager != null) {
                            ImsPhoneCallTracker.this.mImsManager.updateImsServiceConfig(true);
                        }
                        ImsPhoneCallTracker.this.mShouldUpdateImsConfigOnDisconnect = false;
                        return;
                    }
                    return;
                }
                Pair<ImsCall, ImsReasonInfo> callInfo = new Pair<>(imsCall, reasonInfo);
                ImsPhoneCallTracker.this.mPhone.getDefaultPhone().getServiceStateTracker().registerForNetworkAttached(ImsPhoneCallTracker.this, 28, callInfo);
                ImsPhoneCallTracker imsPhoneCallTracker10 = ImsPhoneCallTracker.this;
                imsPhoneCallTracker10.sendMessageDelayed(imsPhoneCallTracker10.obtainMessage(29, callInfo), 10000);
                ((ConnectivityManager) ImsPhoneCallTracker.this.mPhone.getContext().getSystemService("connectivity")).setAirplaneMode(false);
            }

            public void onCallHeld(ImsCall imsCall) {
                if (ImsPhoneCallTracker.this.mForegroundCall.getImsCall() == imsCall) {
                    ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker.log("onCallHeld (fg) " + imsCall);
                } else if (ImsPhoneCallTracker.this.mBackgroundCall.getImsCall() == imsCall) {
                    ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker2.log("onCallHeld (bg) " + imsCall);
                }
                synchronized (ImsPhoneCallTracker.this.mSyncHold) {
                    Call.State oldState = ImsPhoneCallTracker.this.mBackgroundCall.getState();
                    ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.HOLDING, 0);
                    if (oldState == Call.State.ACTIVE) {
                        if (ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING && ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.SWAPPING_ACTIVE_AND_HELD) {
                            ImsPhoneCallTracker.this.sendEmptyMessage(31);
                        } else if (ImsPhoneCallTracker.this.mRingingCall.getState() == Call.State.WAITING && ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.HOLDING_TO_ANSWER_INCOMING) {
                            ImsPhoneCallTracker.this.sendEmptyMessage(30);
                        } else if (ImsPhoneCallTracker.this.mPendingMO == null || ImsPhoneCallTracker.this.mHoldSwitchingState != HoldSwapState.HOLDING_TO_DIAL_OUTGOING) {
                            ImsPhoneCallTracker.this.mHoldSwitchingState = HoldSwapState.INACTIVE;
                            ImsPhoneCallTracker.this.logHoldSwapState("onCallHeld normal case");
                        } else {
                            ImsPhoneCallTracker.this.dialPendingMO();
                            ImsPhoneCallTracker.this.mHoldSwitchingState = HoldSwapState.INACTIVE;
                            ImsPhoneCallTracker.this.logHoldSwapState("onCallHeld hold to dial");
                        }
                    } else if (oldState == Call.State.IDLE && ((ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.SWAPPING_ACTIVE_AND_HELD || ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.HOLDING_TO_ANSWER_INCOMING) && ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING)) {
                        ImsPhoneCallTracker.this.sendEmptyMessage(31);
                        ImsPhoneCallTracker.this.mHoldSwitchingState = HoldSwapState.INACTIVE;
                        ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                        ImsPhoneCallTracker.this.logHoldSwapState("onCallHeld premature termination of other call");
                    }
                }
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHeld(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
            }

            public void onCallHoldFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onCallHoldFailed reasonCode=" + reasonInfo.getCode());
                synchronized (ImsPhoneCallTracker.this.mSyncHold) {
                    Call.State bgState = ImsPhoneCallTracker.this.mBackgroundCall.getState();
                    if (reasonInfo.getCode() == 148) {
                        if (ImsPhoneCallTracker.this.mPendingMO != null) {
                            ImsPhoneCallTracker.this.dialPendingMO();
                        } else if (ImsPhoneCallTracker.this.mRingingCall.getState() == Call.State.WAITING && ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.HOLDING_TO_ANSWER_INCOMING) {
                            ImsPhoneCallTracker.this.sendEmptyMessage(30);
                        } else if (ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING && ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.SWAPPING_ACTIVE_AND_HELD) {
                            ImsPhoneCallTracker.this.sendEmptyMessage(31);
                        }
                        ImsPhoneCallTracker.this.mHoldSwitchingState = HoldSwapState.INACTIVE;
                    } else if (ImsPhoneCallTracker.this.mPendingMO != null && ImsPhoneCallTracker.this.mPendingMO.isEmergency()) {
                        ImsPhoneCallTracker.this.mBackgroundCall.getImsCall().terminate(0);
                        if (imsCall != ImsPhoneCallTracker.this.mCallExpectedToResume) {
                            ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                        }
                    } else if (ImsPhoneCallTracker.this.mRingingCall.getState() == Call.State.WAITING && ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.HOLDING_TO_ANSWER_INCOMING) {
                        ImsPhoneCallTracker.this.mHoldSwitchingState = HoldSwapState.INACTIVE;
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                        ImsPhoneCallTracker.this.logHoldSwapState("onCallHoldFailed unable to answer waiting call");
                    } else if (bgState == Call.State.ACTIVE) {
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                        if (ImsPhoneCallTracker.this.mPendingMO != null) {
                            ImsPhoneCallTracker.this.mPendingMO.setDisconnectCause(36);
                            ImsPhoneCallTracker.this.sendEmptyMessageDelayed(18, 500);
                        }
                        if (imsCall != ImsPhoneCallTracker.this.mCallExpectedToResume) {
                            ImsPhoneCallTracker.this.mCallExpectedToResume = null;
                        }
                        ImsPhoneCallTracker.this.mHoldSwitchingState = HoldSwapState.INACTIVE;
                    }
                    ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                    if (conn != null) {
                        conn.onConnectionEvent("android.telecom.event.CALL_HOLD_FAILED", null);
                    }
                    ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(PhoneInternalInterface.SuppService.HOLD);
                }
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHoldFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
            }

            public void onCallResumed(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("onCallResumed");
                if (ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.SWAPPING_ACTIVE_AND_HELD || ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.PENDING_RESUME_FOREGROUND_AFTER_FAILURE || ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.PENDING_SINGLE_CALL_UNHOLD) {
                    if (imsCall != ImsPhoneCallTracker.this.mCallExpectedToResume) {
                        ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                        imsPhoneCallTracker.log("onCallResumed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                    } else {
                        ImsPhoneCallTracker.this.log("onCallResumed : expected call resumed.");
                    }
                    ImsPhoneCallTracker.this.mHoldSwitchingState = HoldSwapState.INACTIVE;
                    ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker2.mCallExpectedToResume = null;
                    imsPhoneCallTracker2.logHoldSwapState("onCallResumed");
                }
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.ACTIVE, 0);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
            }

            public void onCallResumeFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                if (ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.SWAPPING_ACTIVE_AND_HELD || ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.PENDING_RESUME_FOREGROUND_AFTER_FAILURE) {
                    if (imsCall == ImsPhoneCallTracker.this.mCallExpectedToResume) {
                        ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                        imsPhoneCallTracker.log("onCallResumeFailed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                        if (ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING) {
                            ImsPhoneCallTracker.this.sendEmptyMessage(31);
                        }
                    }
                    ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker2.mCallExpectedToResume = null;
                    imsPhoneCallTracker2.mHoldSwitchingState = HoldSwapState.INACTIVE;
                    ImsPhoneCallTracker.this.logHoldSwapState("onCallResumeFailed: multi calls");
                } else if (ImsPhoneCallTracker.this.mHoldSwitchingState == HoldSwapState.PENDING_SINGLE_CALL_UNHOLD) {
                    if (imsCall == ImsPhoneCallTracker.this.mCallExpectedToResume) {
                        ImsPhoneCallTracker.this.log("onCallResumeFailed: single call unhold case");
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                        ImsPhoneCallTracker imsPhoneCallTracker3 = ImsPhoneCallTracker.this;
                        imsPhoneCallTracker3.mCallExpectedToResume = null;
                        imsPhoneCallTracker3.mHoldSwitchingState = HoldSwapState.INACTIVE;
                        ImsPhoneCallTracker.this.logHoldSwapState("onCallResumeFailed: single call");
                    } else {
                        Rlog.w(ImsPhoneCallTracker.LOG_TAG, "onCallResumeFailed: got a resume failed for a different call in the single call unhold case");
                    }
                }
                ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(PhoneInternalInterface.SuppService.SWITCH);
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
                if (ImsPhoneCallTracker.this.mPhone.getContext().getResources().getBoolean(17891563) && ImsPhoneCallTracker.this.mSupportPauseVideo && VideoProfile.isVideo(conn.getVideoState())) {
                    conn.changeToUnPausedState();
                }
                SuppServiceNotification supp = new SuppServiceNotification();
                supp.notificationType = 1;
                supp.code = 3;
                ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumeReceived(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
            }

            public void onCallHoldReceived(ImsCall imsCall) {
                ImsPhoneCallTracker.this.onCallHoldReceived(imsCall);
            }

            public void onCallSuppServiceReceived(ImsCall call, ImsSuppServiceNotification suppServiceInfo) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onCallSuppServiceReceived: suppServiceInfo=" + suppServiceInfo);
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
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                try {
                    ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker.log("onCallMerged: ImsPhoneConnection=" + conn);
                    ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker2.log("onCallMerged: CurrentVideoProvider=" + conn.getVideoProvider());
                    ImsPhoneCallTracker.this.setVideoCallProvider(conn, call);
                    ImsPhoneCallTracker imsPhoneCallTracker3 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker3.log("onCallMerged: CurrentVideoProvider=" + conn.getVideoProvider());
                } catch (Exception e) {
                    ImsPhoneCallTracker imsPhoneCallTracker4 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker4.loge("onCallMerged: exception " + e);
                }
                ImsPhoneCallTracker imsPhoneCallTracker5 = ImsPhoneCallTracker.this;
                imsPhoneCallTracker5.processCallStateChange(imsPhoneCallTracker5.mForegroundCall.getImsCall(), Call.State.ACTIVE, 0);
                conn.handleMergeComplete();
                if (peerConnection != null) {
                    ImsPhoneCallTracker imsPhoneCallTracker6 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker6.processCallStateChange(imsPhoneCallTracker6.mBackgroundCall.getImsCall(), Call.State.HOLDING, 0);
                    peerConnection.handleMergeComplete();
                }
                if (!call.isMergeRequestedByConf()) {
                    ImsPhoneCallTracker.this.log("onCallMerged :: calling onMultipartyStateChanged()");
                    onMultipartyStateChanged(call, true);
                } else {
                    ImsPhoneCallTracker.this.log("onCallMerged :: Merge requested by existing conference.");
                    call.resetIsMergeRequestedByConf(false);
                }
                ImsPhoneCallTracker.this.logState();
            }

            public void onCallMergeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onCallMergeFailed reasonInfo=" + reasonInfo);
                ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(PhoneInternalInterface.SuppService.CONFERENCE);
                call.resetIsMergeRequestedByConf(false);
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                if (conn != null) {
                    conn.onConferenceMergeFailed();
                }
                ImsPhoneConnection foregroundConnection = ImsPhoneCallTracker.this.mForegroundCall.getFirstConnection();
                if (foregroundConnection != null) {
                    foregroundConnection.handleMergeComplete();
                }
                ImsCall bgImsCall = ImsPhoneCallTracker.this.mBackgroundCall.getImsCall();
                if (bgImsCall == null) {
                    ImsPhoneCallTracker.this.log("onCallMergeFailed: conference no background ims call");
                    return;
                }
                if (ImsPhoneCallTracker.this.isNeedToResumeHoldCall(false)) {
                    ImsPhoneCallTracker.this.log("onCallMergeFailed : resumeBackgroundCall");
                    ImsPhoneCallTracker.this.resumeBackgroundCall();
                }
                ImsPhoneConnection backgroundConnection = ImsPhoneCallTracker.this.findConnection(bgImsCall);
                if (backgroundConnection != null) {
                    backgroundConnection.handleMergeComplete();
                }
            }

            private void updateConferenceParticipantsTiming(List<ConferenceParticipant> participants) {
                for (ConferenceParticipant participant : participants) {
                    CacheEntry cachedConnectTime = ImsPhoneCallTracker.this.findConnectionTimeUsePhoneNumber(participant);
                    if (cachedConnectTime != null) {
                        participant.setConnectTime(cachedConnectTime.mConnectTime);
                        participant.setConnectElapsedTime(cachedConnectTime.mConnectElapsedTime);
                        participant.setCallDirection(cachedConnectTime.mCallDirection);
                    }
                }
            }

            public void onConferenceParticipantsStateChanged(ImsCall call, List<ConferenceParticipant> participants) {
                ImsPhoneCallTracker.this.log("onConferenceParticipantsStateChanged");
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                if (conn != null) {
                    updateConferenceParticipantsTiming(participants);
                    conn.updateConferenceParticipants(participants);
                }
            }

            public void onCallSessionTtyModeReceived(ImsCall call, int mode) {
                ImsPhoneCallTracker.this.mPhone.onTtyModeReceived(mode);
            }

            public void onCallHandover(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
                boolean isDataEnabled = ImsPhoneCallTracker.this.mPhone.getDefaultPhone().getDataEnabledSettings().isDataEnabled();
                ImsPhoneCallTracker.this.log("onCallHandover ::  srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo + ", dataEnabled=" + ImsPhoneCallTracker.this.mIsDataEnabled + "/" + isDataEnabled + ", dataMetered=" + ImsPhoneCallTracker.this.mIsViLteDataMetered);
                if (ImsPhoneCallTracker.this.mIsDataEnabled != isDataEnabled) {
                    ImsPhoneCallTracker.this.loge("onCallHandover: data enabled state doesn't match! (was=" + ImsPhoneCallTracker.this.mIsDataEnabled + ", actually=" + isDataEnabled);
                    ImsPhoneCallTracker.this.mIsDataEnabled = isDataEnabled;
                }
                boolean isHandoverFromWifi = false;
                boolean isHandoverToWifi = (srcAccessTech == 0 || srcAccessTech == 18 || targetAccessTech != 18) ? false : true;
                if (!(srcAccessTech != 18 || targetAccessTech == 0 || targetAccessTech == 18)) {
                    isHandoverFromWifi = true;
                }
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    ImsPhoneCall imsPhoneCall = conn.getCall();
                    if (imsPhoneCall != null) {
                        imsPhoneCall.maybeStopRingback();
                    }
                    if (conn.getDisconnectCause() == 0) {
                        if (isHandoverToWifi) {
                            ImsPhoneCallTracker.this.removeMessages(25);
                            if (ImsPhoneCallTracker.this.mNotifyHandoverVideoFromLTEToWifi && ImsPhoneCallTracker.this.mHasAttemptedStartOfCallHandover) {
                                conn.onConnectionEvent("android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_LTE_TO_WIFI", null);
                            }
                            ImsPhoneCallTracker.this.unregisterForConnectivityChanges();
                        } else if (isHandoverFromWifi && ImsPhoneCallTracker.this.isVideoCall(imsCall)) {
                            ImsPhoneCallTracker.this.registerForConnectivityChanges();
                        }
                    }
                    if (isHandoverToWifi && ImsPhoneCallTracker.this.mIsViLteDataMetered) {
                        conn.setLocalVideoCapable(true);
                    }
                    if (isHandoverFromWifi && ImsPhoneCallTracker.this.isVideoCall(imsCall)) {
                        if (ImsPhoneCallTracker.this.mIsViLteDataMetered) {
                            conn.setLocalVideoCapable(ImsPhoneCallTracker.this.mIsDataEnabled);
                        }
                        if (ImsPhoneCallTracker.this.mNotifyHandoverVideoFromWifiToLTE && ImsPhoneCallTracker.this.mIsDataEnabled) {
                            if (conn.getDisconnectCause() == 0) {
                                ImsPhoneCallTracker.this.log("onCallHandover :: notifying of WIFI to LTE handover.");
                                conn.onConnectionEvent("android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_WIFI_TO_LTE", null);
                            } else {
                                ImsPhoneCallTracker.this.log("onCallHandover :: skip notify of WIFI to LTE handover for disconnected call.");
                            }
                        }
                        if (!ImsPhoneCallTracker.this.mIsDataEnabled && ImsPhoneCallTracker.this.mIsViLteDataMetered) {
                            ImsPhoneCallTracker.this.log("onCallHandover :: data is not enabled; attempt to downgrade.");
                            ImsPhoneCallTracker.this.downgradeVideoCall(1407, conn);
                        }
                    }
                } else {
                    ImsPhoneCallTracker.this.loge("onCallHandover :: connection null.");
                }
                if (!ImsPhoneCallTracker.this.mHasAttemptedStartOfCallHandover) {
                    boolean unused = ImsPhoneCallTracker.this.mHasAttemptedStartOfCallHandover = true;
                }
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHandoverEvent(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 18, imsCall.getCallSession(), srcAccessTech, targetAccessTech, reasonInfo);
            }

            public void onCallHandoverFailed(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onCallHandoverFailed :: srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHandoverEvent(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 19, imsCall.getCallSession(), srcAccessTech, targetAccessTech, reasonInfo);
                boolean isHandoverToWifi = srcAccessTech != 18 && targetAccessTech == 18;
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null && isHandoverToWifi) {
                    ImsPhoneCallTracker.this.log("onCallHandoverFailed - handover to WIFI Failed");
                    ImsPhoneCallTracker.this.removeMessages(25);
                    if (ImsPhoneCallTracker.this.isVideoCall(imsCall) && conn.getDisconnectCause() == 0) {
                        ImsPhoneCallTracker.this.registerForConnectivityChanges();
                    }
                    if (ImsPhoneCallTracker.this.mNotifyVtHandoverToWifiFail) {
                        conn.onHandoverToWifiFailed();
                    }
                }
                if (!ImsPhoneCallTracker.this.mHasAttemptedStartOfCallHandover) {
                    boolean unused = ImsPhoneCallTracker.this.mHasAttemptedStartOfCallHandover = true;
                }
            }

            public void onRttModifyRequestReceived(ImsCall imsCall) {
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    conn.onRttModifyRequestReceived();
                }
            }

            public void onRttModifyResponseReceived(ImsCall imsCall, int status) {
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    conn.onRttModifyResponseReceived(status);
                }
            }

            public void onRttMessageReceived(ImsCall imsCall, String message) {
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    conn.onRttMessageReceived(message);
                }
            }

            public void onRttAudioIndicatorChanged(ImsCall imsCall, ImsStreamMediaProfile profile) {
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    conn.onRttAudioIndicatorChanged(profile);
                }
            }

            public void onMultipartyStateChanged(ImsCall imsCall, boolean isMultiParty) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                StringBuilder sb = new StringBuilder();
                sb.append("onMultipartyStateChanged to ");
                sb.append(isMultiParty ? "Y" : "N");
                imsPhoneCallTracker.log(sb.toString());
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    conn.updateMultipartyState(isMultiParty);
                }
            }

            public void onCallQualityChanged(ImsCall imsCall, CallQuality callQuality) {
                ImsPhoneCallTracker.this.mPhone.onCallQualityChanged(callQuality, ServiceState.rilRadioTechnologyToNetworkType(imsCall.getRadioTechnology()));
                String callId = imsCall.getSession().getCallId();
                CallQualityMetrics cqm = (CallQualityMetrics) ImsPhoneCallTracker.this.mCallQualityMetrics.get(callId);
                if (cqm == null) {
                    cqm = new CallQualityMetrics(ImsPhoneCallTracker.this.mPhone);
                }
                cqm.saveCallQuality(callQuality);
                ImsPhoneCallTracker.this.mCallQualityMetrics.put(callId, cqm);
            }
        };
        this.mImsUssdListener = new ImsCall.Listener() {
            /* class com.android.internal.telephony.imsphone.ImsPhoneCallTracker.AnonymousClass5 */

            public void onCallStarted(ImsCall imsCall) {
                ImsPhoneCallTracker.this.log("mImsUssdListener onCallStarted");
                if (imsCall == ImsPhoneCallTracker.this.mUssdSession && ImsPhoneCallTracker.this.mPendingUssd != null) {
                    AsyncResult.forMessage(ImsPhoneCallTracker.this.mPendingUssd);
                    ImsPhoneCallTracker.this.mPendingUssd.sendToTarget();
                    ImsPhoneCallTracker.this.mPendingUssd = null;
                }
            }

            public void onCallStartFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("mImsUssdListener onCallStartFailed reasonCode=" + reasonInfo.getCode());
                onCallTerminated(imsCall, reasonInfo);
            }

            public void onCallTerminated(ImsCall imsCall, ImsReasonInfo reasonInfo) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("mImsUssdListener onCallTerminated reasonCode=" + reasonInfo.getCode());
                ImsPhoneCallTracker.this.removeMessages(25);
                boolean unused = ImsPhoneCallTracker.this.mHasAttemptedStartOfCallHandover = false;
                ImsPhoneCallTracker.this.unregisterForConnectivityChanges();
                if (imsCall == ImsPhoneCallTracker.this.mUssdSession) {
                    ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker2.mUssdSession = null;
                    if (imsPhoneCallTracker2.mPendingUssd != null) {
                        AsyncResult.forMessage(ImsPhoneCallTracker.this.mPendingUssd, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                        ImsPhoneCallTracker.this.mPendingUssd.sendToTarget();
                        ImsPhoneCallTracker.this.mPendingUssd = null;
                    }
                }
                imsCall.close();
            }

            public void onCallUssdMessageReceived(ImsCall call, int mode, String ussdMessage) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("mImsUssdListener onCallUssdMessageReceived mode=" + mode);
                int ussdMode = -1;
                if (mode == 0) {
                    ussdMode = 0;
                } else if (mode == 1) {
                    ussdMode = 1;
                }
                ImsPhoneCallTracker.this.mPhone.onIncomingUSSD(ussdMode, ussdMessage);
            }
        };
        this.mImsRegistrationCallback = new ImsMmTelManager.RegistrationCallback() {
            /* class com.android.internal.telephony.imsphone.ImsPhoneCallTracker.AnonymousClass6 */

            public void onRegistered(int imsRadioTech) {
                ImsPhoneCallTracker.this.log("onImsConnected imsRadioTech=" + imsRadioTech);
                boolean z = false;
                ImsPhoneCallTracker.this.mPhone.setServiceState(0);
                ImsPhone imsPhone = ImsPhoneCallTracker.this.mPhone;
                if (ImsPhoneCallTracker.this.isVolteEnabled() || ImsPhoneCallTracker.this.isVowifiEnabled()) {
                    z = true;
                }
                imsPhone.setImsRegistered(z);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 1, null);
            }

            public void onRegistering(int imsRadioTech) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onImsProgressing imsRadioTech=" + imsRadioTech);
                ImsPhoneCallTracker.this.mPhone.setServiceState(1);
                ImsPhoneCallTracker.this.mPhone.setImsRegistered(false);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 2, null);
            }

            public void onUnregistered(ImsReasonInfo imsReasonInfo) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onImsDisconnected imsReasonInfo=" + imsReasonInfo);
                ImsPhoneCallTracker.this.mPhone.setServiceState(1);
                ImsPhoneCallTracker.this.mPhone.setImsRegistered(false);
                ImsPhoneCallTracker.this.mPhone.processDisconnectReason(imsReasonInfo);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 3, imsReasonInfo);
            }

            public void onSubscriberAssociatedUriChanged(Uri[] uris) {
                ImsPhoneCallTracker.this.log("registrationAssociatedUriChanged");
                ImsPhoneCallTracker.this.mPhone.setCurrentSubscriberUris(uris);
            }
        };
        this.mImsCapabilityCallback = new ImsMmTelManager.CapabilityCallback() {
            /* class com.android.internal.telephony.imsphone.ImsPhoneCallTracker.AnonymousClass7 */

            public void onCapabilitiesStatusChanged(MmTelFeature.MmTelCapabilities capabilities) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onCapabilitiesStatusChanged: " + capabilities);
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = capabilities;
                ImsPhoneCallTracker.this.removeMessages(26);
                ImsPhoneCallTracker.this.obtainMessage(26, args).sendToTarget();
                ImsPhoneCallTracker.this.checkRttCallType();
            }
        };
        this.mImsConfigListener = new ImsConfigListener.Stub() {
            /* class com.android.internal.telephony.imsphone.ImsPhoneCallTracker.AnonymousClass8 */

            public void onGetFeatureResponse(int feature, int network, int value, int status) {
            }

            public void onSetFeatureResponse(int feature, int network, int value, int status) {
                ImsPhoneCallTracker.this.mMetrics.writeImsSetFeatureValue(ImsPhoneCallTracker.this.mPhone.getPhoneId(), feature, network, value);
            }

            public void onGetVideoQuality(int status, int quality) {
            }

            public void onSetVideoQuality(int status) {
            }
        };
        this.mConfigCallback = new ProvisioningManager.Callback() {
            /* class com.android.internal.telephony.imsphone.ImsPhoneCallTracker.AnonymousClass9 */

            public void onProvisioningIntChanged(int item, int value) {
                sendConfigChangedIntent(item, Integer.toString(value));
            }

            public void onProvisioningStringChanged(int item, String value) {
                sendConfigChangedIntent(item, value);
            }

            private void sendConfigChangedIntent(int item, String value) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("sendConfigChangedIntent - [" + item + ", " + value + "]");
                Intent configChangedIntent = new Intent("com.android.intent.action.IMS_CONFIG_CHANGED");
                configChangedIntent.putExtra("item", item);
                configChangedIntent.putExtra("value", value);
                if (ImsPhoneCallTracker.this.mPhone != null && ImsPhoneCallTracker.this.mPhone.getContext() != null) {
                    ImsPhoneCallTracker.this.mPhone.getContext().sendBroadcast(configChangedIntent);
                }
            }
        };
        this.mPhone = phone;
        this.mMetrics = TelephonyMetrics.getInstance();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        intentfilter.addAction("android.telecom.action.CHANGE_DEFAULT_DIALER");
        this.mPhone.getContext().registerReceiver(this.mReceiver, intentfilter);
        cacheCarrierConfiguration(this.mPhone.getSubId());
        this.mPhone.getDefaultPhone().getDataEnabledSettings().registerForDataEnabledChanged(this, 23, null);
        this.mDefaultDialerUid.set(getPackageUid(this.mPhone.getContext(), ((TelecomManager) this.mPhone.getContext().getSystemService("telecom")).getDefaultDialerPackage()));
        long currentTime = SystemClock.elapsedRealtime();
        this.mVtDataUsageSnapshot = new NetworkStats(currentTime, 1);
        this.mVtDataUsageUidSnapshot = new NetworkStats(currentTime, 1);
        this.mImsManagerConnector = new ImsManager.Connector(phone.getContext(), phone.getPhoneId(), new ImsManager.Connector.Listener() {
            /* class com.android.internal.telephony.imsphone.ImsPhoneCallTracker.AnonymousClass3 */

            public void connectionReady(ImsManager manager) throws ImsException {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.mImsManager = manager;
                imsPhoneCallTracker.startListeningForCalls();
            }

            public void connectionUnavailable() {
                ImsPhoneCallTracker.this.stopListeningForCalls();
            }
        }, executor);
        this.mImsManagerConnector.connect();
        this.mReference = (IOppoCallTracker) OppoTelephonyFactory.getInstance().getFeature(IOppoCallTracker.DEFAULT, this);
    }

    @VisibleForTesting
    public void setSharedPreferenceProxy(SharedPreferenceProxy sharedPreferenceProxy) {
        this.mSharedPreferenceProxy = sharedPreferenceProxy;
    }

    @VisibleForTesting
    public void setPhoneNumberUtilsProxy(PhoneNumberUtilsProxy phoneNumberUtilsProxy) {
        this.mPhoneNumberUtilsProxy = phoneNumberUtilsProxy;
    }

    @VisibleForTesting
    public void setRetryTimeout(ImsManager.Connector.RetryTimeout retryTimeout) {
        this.mImsManagerConnector.mRetryTimeout = retryTimeout;
    }

    /* access modifiers changed from: private */
    public int getPackageUid(Context context, String pkg) {
        if (pkg == null) {
            return -1;
        }
        try {
            return context.getPackageManager().getPackageUid(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            loge("Cannot find package uid. pkg = " + pkg);
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public void startListeningForCalls() throws ImsException {
        log("startListeningForCalls");
        this.mImsManager.open(this.mMmTelFeatureListener);
        this.mImsManager.addRegistrationCallback(this.mImsRegistrationCallback);
        this.mImsManager.addCapabilitiesCallback(this.mImsCapabilityCallback);
        this.mImsManager.setConfigListener(this.mImsConfigListener);
        this.mImsManager.getConfigInterface().addConfigCallback(this.mConfigCallback);
        getEcbmInterface().setEcbmStateListener(this.mPhone.getImsEcbmStateListener());
        if (this.mPhone.isInEcm()) {
            this.mPhone.exitEmergencyCallbackMode();
        }
        this.mImsManager.setUiTTYMode(this.mPhone.getContext(), Settings.Secure.getInt(this.mPhone.getContext().getContentResolver(), "preferred_tty_mode", 0), (Message) null);
        ImsMultiEndpoint multiEndpoint = getMultiEndpointInterface();
        if (multiEndpoint != null) {
            multiEndpoint.setExternalCallStateListener(this.mPhone.getExternalCallTracker().getExternalCallStateListener());
        }
        this.mUtInterface = getUtInterface();
        ImsUtInterface imsUtInterface = this.mUtInterface;
        if (imsUtInterface != null) {
            imsUtInterface.registerForSuppServiceIndication(this, 27, (Object) null);
        }
        if (this.mCarrierConfigLoaded) {
            this.mImsManager.updateImsServiceConfig(true);
        }
        sendImsServiceStateIntent("com.android.ims.IMS_SERVICE_UP");
    }

    /* access modifiers changed from: private */
    public void stopListeningForCalls() {
        log("stopListeningForCalls");
        resetImsCapabilities();
        ImsManager imsManager = this.mImsManager;
        if (imsManager != null) {
            try {
                imsManager.getConfigInterface().removeConfigCallback(this.mConfigCallback.getBinder());
            } catch (ImsException e) {
                Log.w(LOG_TAG, "stopListeningForCalls: unable to remove config callback.");
            }
            this.mImsManager.close();
        }
        sendImsServiceStateIntent("com.android.ims.IMS_SERVICE_DOWN");
    }

    private void sendImsServiceStateIntent(String intentAction) {
        Intent intent = new Intent(intentAction);
        intent.putExtra("android:phone_id", this.mPhone.getPhoneId());
        ImsPhone imsPhone = this.mPhone;
        if (imsPhone != null && imsPhone.getContext() != null) {
            this.mPhone.getContext().sendBroadcast(intent);
        }
    }

    public void dispose() {
        log("dispose");
        this.mRingingCall.dispose();
        this.mBackgroundCall.dispose();
        this.mForegroundCall.dispose();
        this.mHandoverCall.dispose();
        clearDisconnected();
        ImsUtInterface imsUtInterface = this.mUtInterface;
        if (imsUtInterface != null) {
            imsUtInterface.unregisterForSuppServiceIndication(this);
        }
        this.mPhone.getContext().unregisterReceiver(this.mReceiver);
        this.mPhone.getDefaultPhone().getDataEnabledSettings().unregisterForDataEnabledChanged(this);
        this.mImsManagerConnector.disconnect();
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        log("ImsPhoneCallTracker finalized");
    }

    @Override // com.android.internal.telephony.CallTracker
    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        this.mVoiceCallStartedRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CallTracker
    public void unregisterForVoiceCallStarted(Handler h) {
        this.mVoiceCallStartedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CallTracker
    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        this.mVoiceCallEndedRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CallTracker
    public void unregisterForVoiceCallEnded(Handler h) {
        this.mVoiceCallEndedRegistrants.remove(h);
    }

    public int getClirMode() {
        if (this.mSharedPreferenceProxy == null || this.mPhone.getDefaultPhone() == null) {
            loge("dial; could not get default CLIR mode.");
            return 0;
        }
        SharedPreferences sp = this.mSharedPreferenceProxy.getDefaultSharedPreferences(this.mPhone.getContext());
        return sp.getInt(Phone.CLIR_KEY + this.mPhone.getDefaultPhone().getPhoneId(), 0);
    }

    @UnsupportedAppUsage
    public com.android.internal.telephony.Connection dial(String dialString, int videoState, Bundle intentExtras) throws CallStateException {
        return dial(dialString, ((ImsPhone.ImsDialArgs.Builder) ((ImsPhone.ImsDialArgs.Builder) new ImsPhone.ImsDialArgs.Builder().setIntentExtras(intentExtras)).setVideoState(videoState)).setClirMode(getClirMode()).build());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:50:0x011c, code lost:
        addConnection(r16.mPendingMO);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0121, code lost:
        if (r8 != false) goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0123, code lost:
        if (r0 == false) goto L_0x014e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0125, code lost:
        if (r0 == false) goto L_0x012a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0127, code lost:
        if (r0 == false) goto L_0x012a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:?, code lost:
        getEcbmInterface().exitEmergencyCallbackMode();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0131, code lost:
        r16.mPhone.setOnEcbModeExitResponse(r16, 14, null);
        r16.pendingCallClirMode = r7;
        r16.mPendingCallVideoState = r6;
        r16.pendingCallInEcm = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0142, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0143, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x014d, code lost:
        throw new com.android.internal.telephony.CallStateException("service not available");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x014e, code lost:
        dialInternal(r16.mPendingMO, r7, r6, r18.intentExtras);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0155, code lost:
        updatePhoneState();
        r16.mPhone.notifyPreciseCallStateChanged();
        switchWfcModeIfRequired(r16.mImsManager, isVowifiEnabled(), r0);
     */
    public synchronized com.android.internal.telephony.Connection dial(String dialString, ImsPhone.ImsDialArgs dialArgs) throws CallStateException {
        int clirMode;
        ImsPhoneConnection conn;
        boolean isPhoneInEcmMode = isPhoneInEcbMode();
        boolean isEmergencyNumber = isEmergencyNumber(dialString);
        if (shouldNumberBePlacedOnIms(isEmergencyNumber, dialString)) {
            int clirMode2 = dialArgs.clirMode;
            int videoState = dialArgs.videoState;
            log("dial clirMode=" + clirMode2);
            if (isEmergencyNumber) {
                log("dial emergency call, set clirModIe=" + 2);
                clirMode = 2;
            } else {
                clirMode = clirMode2;
            }
            clearDisconnected();
            if (this.mImsManager != null) {
                checkforCsfb();
                checkForDialIssues();
                if (isPhoneInEcmMode && isEmergencyNumber) {
                    handleEcmTimer(1);
                }
                if (isEmergencyNumber && VideoProfile.isVideo(videoState) && !this.mAllowEmergencyVideoCalls) {
                    loge("dial: carrier does not support video emergency calls; downgrade to audio-only");
                    videoState = 0;
                }
                boolean holdBeforeDial = false;
                if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                    if (this.mBackgroundCall.getState() == Call.State.IDLE) {
                        holdBeforeDial = true;
                        this.mPendingCallVideoState = videoState;
                        this.mPendingIntentExtras = dialArgs.intentExtras;
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
                                    th = th;
                                    throw th;
                                }
                            } else {
                                throw new CallStateException("cannot dial in current state");
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                    this.mLastDialString = dialString;
                    this.mLastDialArgs = dialArgs;
                    this.mPendingMO = makeImsPhoneConnectionForMO(dialString, isEmergencyNumber);
                    if (isEmergencyNumber && dialArgs.intentExtras != null) {
                        Rlog.i(LOG_TAG, "dial ims emergency dialer: " + dialArgs.intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
                        this.mPendingMO.setHasKnownUserIntentEmergency(dialArgs.intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
                    }
                    conn = this.mPendingMO;
                    this.mPendingMO.setVideoState(videoState);
                    if (dialArgs.rttTextStream != null) {
                        log("dial: setting RTT stream on mPendingMO");
                        this.mPendingMO.setCurrentRttTextStream(dialArgs.rttTextStream);
                    }
                }
            } else {
                throw new CallStateException("service not available");
            }
        } else {
            Rlog.i(LOG_TAG, "dial: shouldNumberBePlacedOnIms = false");
            throw new CallStateException(Phone.CS_FALLBACK);
        }
        return conn;
    }

    /* access modifiers changed from: package-private */
    public boolean isImsServiceReady() {
        ImsManager imsManager = this.mImsManager;
        if (imsManager == null) {
            return false;
        }
        return imsManager.isServiceReady();
    }

    private boolean shouldNumberBePlacedOnIms(boolean isEmergency, String number) {
        try {
            if (this.mImsManager != null) {
                int processCallResult = this.mImsManager.shouldProcessCall(isEmergency, new String[]{number});
                Rlog.i(LOG_TAG, "shouldProcessCall: number: " + Rlog.pii(LOG_TAG, number) + ", result: " + processCallResult);
                if (processCallResult == 0) {
                    return true;
                }
                if (processCallResult != 1) {
                    Rlog.w(LOG_TAG, "shouldProcessCall returned unknown result.");
                    return false;
                }
                Rlog.i(LOG_TAG, "shouldProcessCall: place over CSFB instead.");
                return false;
            }
            Rlog.w(LOG_TAG, "ImsManager unavailable, shouldProcessCall returning false.");
            return false;
        } catch (ImsException e) {
            Rlog.w(LOG_TAG, "ImsService unavailable, shouldProcessCall returning false.");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void cacheCarrierConfiguration(int subId) {
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (carrierConfigManager == null || !SubscriptionController.getInstance().isActiveSubId(subId)) {
            loge("cacheCarrierConfiguration: No carrier config service found or not active subId = " + subId);
            this.mCarrierConfigLoaded = false;
            return;
        }
        PersistableBundle carrierConfig = carrierConfigManager.getConfigForSubId(subId);
        if (carrierConfig == null) {
            loge("cacheCarrierConfiguration: Empty carrier config.");
            this.mCarrierConfigLoaded = false;
            return;
        }
        this.mCarrierConfigLoaded = true;
        updateCarrierConfigCache(carrierConfig);
    }

    @VisibleForTesting
    public void updateCarrierConfigCache(PersistableBundle carrierConfig) {
        Integer fromCode;
        String str;
        this.mAllowEmergencyVideoCalls = carrierConfig.getBoolean("allow_emergency_video_calls_bool");
        this.mTreatDowngradedVideoCallsAsVideoCalls = carrierConfig.getBoolean("treat_downgraded_video_calls_as_video_calls_bool");
        this.mDropVideoCallWhenAnsweringAudioCall = carrierConfig.getBoolean("drop_video_call_when_answering_audio_call_bool");
        this.mAllowAddCallDuringVideoCall = carrierConfig.getBoolean("allow_add_call_during_video_call");
        this.mNotifyVtHandoverToWifiFail = carrierConfig.getBoolean("notify_vt_handover_to_wifi_failure_bool");
        this.mSupportDowngradeVtToAudio = carrierConfig.getBoolean("support_downgrade_vt_to_audio_bool");
        this.mNotifyHandoverVideoFromWifiToLTE = carrierConfig.getBoolean("notify_handover_video_from_wifi_to_lte_bool");
        this.mNotifyHandoverVideoFromLTEToWifi = carrierConfig.getBoolean("notify_handover_video_from_lte_to_wifi_bool");
        this.mIgnoreDataEnabledChangedForVideoCalls = carrierConfig.getBoolean("ignore_data_enabled_changed_for_video_calls");
        this.mIsViLteDataMetered = carrierConfig.getBoolean("vilte_data_is_metered_bool");
        this.mSupportPauseVideo = carrierConfig.getBoolean("support_pause_ims_video_calls_bool");
        this.mAlwaysPlayRemoteHoldTone = carrierConfig.getBoolean("always_play_remote_hold_tone_bool");
        this.mAutoRetryFailedWifiEmergencyCall = carrierConfig.getBoolean("auto_retry_failed_wifi_emergency_call");
        String[] mappings = carrierConfig.getStringArray("ims_reasoninfo_mapping_string_array");
        if (mappings == null || mappings.length <= 0) {
            log("No carrier ImsReasonInfo mappings defined.");
            return;
        }
        for (String mapping : mappings) {
            String[] values = mapping.split(Pattern.quote("|"));
            if (values.length == 3) {
                try {
                    if (values[0].equals(CharacterSets.MIMENAME_ANY_CHARSET)) {
                        fromCode = null;
                    } else {
                        fromCode = Integer.valueOf(Integer.parseInt(values[0]));
                    }
                    String message = values[1];
                    if (message == null) {
                        message = PhoneConfigurationManager.SSSS;
                    }
                    int toCode = Integer.parseInt(values[2]);
                    addReasonCodeRemapping(fromCode, message, Integer.valueOf(toCode));
                    if (("Loaded ImsReasonInfo mapping : fromCode = " + fromCode) == null) {
                        str = "any";
                    } else {
                        str = fromCode + " ; message = " + message + " ; toCode = " + toCode;
                    }
                    log(str);
                } catch (NumberFormatException e) {
                    loge("Invalid ImsReasonInfo mapping found: " + mapping);
                } catch (Exception e2) {
                    loge(e2.toString());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void handleEcmTimer(int action) {
        this.mPhone.handleTimerInEmergencyCallbackMode(action);
        if (action != 0 && action != 1) {
            log("handleEcmTimer, unsupported action " + action);
        }
    }

    /* access modifiers changed from: protected */
    public void dialInternal(ImsPhoneConnection conn, int clirMode, int videoState, Bundle intentExtras) {
        if (conn != null) {
            if (conn.getAddress() != null && conn.getAddress().length() != 0) {
                if (conn.getAddress().indexOf(78) < 0) {
                    setMute(false);
                    boolean isEmergencyCall = this.mPhoneNumberUtilsProxy.isEmergencyNumber(conn.getAddress());
                    int serviceType = isEmergencyCall ? 2 : 1;
                    int callType = ImsCallProfile.getCallTypeFromVideoState(videoState);
                    conn.setVideoState(videoState);
                    try {
                        String[] callees = {conn.getAddress()};
                        ImsCallProfile profile = this.mImsManager.createCallProfile(serviceType, callType);
                        try {
                            profile.setCallExtraInt("oir", clirMode);
                            if (isEmergencyCall) {
                                setEmergencyCallInfo(profile, conn);
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
                                    int dialogId = intentExtras.getInt(ImsExternalCallTracker.EXTRA_IMS_EXTERNAL_CALL_ID);
                                    conn.setIsPulledCall(true);
                                    conn.setPulledDialogId(dialogId);
                                }
                                profile.mCallExtras.putBundle("OemCallExtras", intentExtras);
                            }
                            ImsCall imsCall = this.mImsManager.makeCall(profile, callees, this.mImsCallListener);
                            conn.setImsCall(imsCall);
                            this.mMetrics.writeOnImsCallStart(this.mPhone.getPhoneId(), imsCall.getSession());
                            setVideoCallProvider(conn, imsCall);
                            conn.setAllowAddCallDuringVideoCall(this.mAllowAddCallDuringVideoCall);
                            return;
                        } catch (ImsException e) {
                            e = e;
                            loge("dialInternal : " + e);
                            conn.setDisconnectCause(36);
                            sendEmptyMessageDelayed(18, 500);
                            retryGetImsService();
                            return;
                        } catch (RemoteException e2) {
                            return;
                        }
                    } catch (ImsException e3) {
                        e = e3;
                        loge("dialInternal : " + e);
                        conn.setDisconnectCause(36);
                        sendEmptyMessageDelayed(18, 500);
                        retryGetImsService();
                        return;
                    } catch (RemoteException e4) {
                        return;
                    }
                }
            }
            conn.setDisconnectCause(7);
            sendEmptyMessageDelayed(18, 500);
        }
    }

    public void acceptCall(int videoState) throws CallStateException {
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
                } catch (ImsException e) {
                    throw new CallStateException("cannot accept call");
                }
            } else {
                holdActiveCallForWaitingCall();
            }
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
        log("rejectCall");
        if (this.mRingingCall.getState().isRinging()) {
            hangup(this.mRingingCall);
            return;
        }
        throw new CallStateException("phone not ringing");
    }

    private void setEmergencyCallInfo(ImsCallProfile profile, com.android.internal.telephony.Connection conn) {
        EmergencyNumber num = conn.getEmergencyNumberInfo();
        if (num != null) {
            profile.setEmergencyCallInfo(num, conn.hasKnownUserIntentEmergency());
        }
    }

    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public void switchAfterConferenceSuccess() {
        log("switchAfterConferenceSuccess fg =" + this.mForegroundCall.getState() + ", bg = " + this.mBackgroundCall.getState());
        if (this.mBackgroundCall.getState() == Call.State.HOLDING) {
            log("switchAfterConferenceSuccess");
            this.mForegroundCall.switchWith(this.mBackgroundCall);
        }
    }

    /* access modifiers changed from: protected */
    public void holdActiveCallForPendingMo() throws CallStateException {
        if (this.mHoldSwitchingState == HoldSwapState.PENDING_SINGLE_CALL_HOLD || this.mHoldSwitchingState == HoldSwapState.SWAPPING_ACTIVE_AND_HELD) {
            logi("Ignoring hold request while already holding or swapping");
            return;
        }
        ImsCall callToHold = this.mForegroundCall.getImsCall();
        this.mHoldSwitchingState = HoldSwapState.HOLDING_TO_DIAL_OUTGOING;
        logHoldSwapState("holdActiveCallForPendingMo");
        this.mForegroundCall.switchWith(this.mBackgroundCall);
        try {
            callToHold.hold();
            this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), callToHold.getSession(), 5);
        } catch (ImsException e) {
            this.mForegroundCall.switchWith(this.mBackgroundCall);
            throw new CallStateException(e.getMessage());
        }
    }

    public void holdActiveCall() throws CallStateException {
        if (this.mForegroundCall.getState() != Call.State.ACTIVE) {
            return;
        }
        if (this.mHoldSwitchingState == HoldSwapState.PENDING_SINGLE_CALL_HOLD || this.mHoldSwitchingState == HoldSwapState.SWAPPING_ACTIVE_AND_HELD) {
            logi("Ignoring hold request while already holding or swapping");
            return;
        }
        ImsCall callToHold = this.mForegroundCall.getImsCall();
        if (this.mBackgroundCall.getState().isAlive()) {
            this.mCallExpectedToResume = this.mBackgroundCall.getImsCall();
            this.mHoldSwitchingState = HoldSwapState.SWAPPING_ACTIVE_AND_HELD;
        } else {
            this.mHoldSwitchingState = HoldSwapState.PENDING_SINGLE_CALL_HOLD;
        }
        logHoldSwapState("holdActiveCall");
        this.mForegroundCall.switchWith(this.mBackgroundCall);
        try {
            callToHold.hold();
            this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), callToHold.getSession(), 5);
        } catch (ImsException e) {
            this.mForegroundCall.switchWith(this.mBackgroundCall);
            this.mHoldSwitchingState = HoldSwapState.INACTIVE;
            logHoldSwapState("holdActiveCall: exception");
            throw new CallStateException(e.getMessage());
        }
    }

    public void holdActiveCallForWaitingCall() throws CallStateException {
        if (!this.mBackgroundCall.getState().isAlive() && this.mRingingCall.getState() == Call.State.WAITING) {
            ImsCall callToHold = this.mForegroundCall.getImsCall();
            this.mHoldSwitchingState = HoldSwapState.HOLDING_TO_ANSWER_INCOMING;
            this.mForegroundCall.switchWith(this.mBackgroundCall);
            logHoldSwapState("holdActiveCallForWaitingCall");
            try {
                callToHold.hold();
                this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), callToHold.getSession(), 5);
            } catch (ImsException e) {
                this.mForegroundCall.switchWith(this.mBackgroundCall);
                throw new CallStateException(e.getMessage());
            }
        }
    }

    public void unholdHeldCall() throws CallStateException {
        try {
            ImsCall imsCall = this.mBackgroundCall.getImsCall();
            if (this.mHoldSwitchingState != HoldSwapState.PENDING_SINGLE_CALL_UNHOLD) {
                if (this.mHoldSwitchingState != HoldSwapState.SWAPPING_ACTIVE_AND_HELD) {
                    if (imsCall != null) {
                        this.mCallExpectedToResume = imsCall;
                        this.mHoldSwitchingState = HoldSwapState.PENDING_SINGLE_CALL_UNHOLD;
                        this.mForegroundCall.switchWith(this.mBackgroundCall);
                        logHoldSwapState("unholdCurrentCall");
                        imsCall.resume();
                        this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 6);
                        return;
                    }
                    return;
                }
            }
            logi("Ignoring unhold request while already unholding or swapping");
        } catch (ImsException e) {
            this.mForegroundCall.switchWith(this.mBackgroundCall);
            this.mCallExpectedToResume = null;
            this.mHoldSwitchingState = HoldSwapState.INACTIVE;
            logHoldSwapState("unholdHeldCall: exception");
            throw new CallStateException(e.getMessage());
        }
    }

    private void resumeForegroundCall() throws ImsException {
        ImsCall imsCall = this.mForegroundCall.getImsCall();
        if (imsCall != null) {
            imsCall.resume();
            this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 6);
        }
    }

    private void answerWaitingCall() throws ImsException {
        ImsCall imsCall = this.mRingingCall.getImsCall();
        if (imsCall != null) {
            imsCall.accept(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState));
            this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 2);
        }
    }

    private void maintainConnectTimeCache() {
        long threshold = SystemClock.elapsedRealtime() - 60000;
        this.mPhoneNumAndConnTime.entrySet().removeIf(new Predicate(threshold) {
            /* class com.android.internal.telephony.imsphone.$$Lambda$ImsPhoneCallTracker$R2Z9jNp4rrTM4H39vy492Fbmqyc */
            private final /* synthetic */ long f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ImsPhoneCallTracker.lambda$maintainConnectTimeCache$2(this.f$0, (Map.Entry) obj);
            }
        });
        while (!this.mUnknownPeerConnTime.isEmpty() && this.mUnknownPeerConnTime.peek().mCachedTime < threshold) {
            this.mUnknownPeerConnTime.poll();
        }
    }

    static /* synthetic */ boolean lambda$maintainConnectTimeCache$2(long threshold, Map.Entry e) {
        return ((CacheEntry) e.getValue()).mCachedTime < threshold;
    }

    private void cacheConnectionTimeWithPhoneNumber(ImsPhoneConnection connection) {
        int callDirection;
        if (connection.isIncoming()) {
            callDirection = 0;
        } else {
            callDirection = 1;
        }
        CacheEntry cachedConnectTime = new CacheEntry(SystemClock.elapsedRealtime(), connection.getConnectTime(), connection.getConnectTimeReal(), callDirection);
        maintainConnectTimeCache();
        if (1 == connection.getNumberPresentation()) {
            String phoneNumber = getFormattedPhoneNumber(connection.getAddress());
            if (!this.mPhoneNumAndConnTime.containsKey(phoneNumber) || connection.getConnectTime() > this.mPhoneNumAndConnTime.get(phoneNumber).mConnectTime) {
                this.mPhoneNumAndConnTime.put(phoneNumber, cachedConnectTime);
                return;
            }
            return;
        }
        this.mUnknownPeerConnTime.add(cachedConnectTime);
    }

    /* access modifiers changed from: private */
    public CacheEntry findConnectionTimeUsePhoneNumber(ConferenceParticipant participant) {
        maintainConnectTimeCache();
        if (1 != participant.getParticipantPresentation()) {
            return this.mUnknownPeerConnTime.poll();
        }
        if (participant.getHandle() == null || participant.getHandle().getSchemeSpecificPart() == null) {
            return null;
        }
        String number = ConferenceParticipant.getParticipantAddress(participant.getHandle(), getCountryIso()).getSchemeSpecificPart();
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        return this.mPhoneNumAndConnTime.get(getFormattedPhoneNumber(number));
    }

    private String getFormattedPhoneNumber(String number) {
        String countryIso = getCountryIso();
        if (countryIso == null) {
            return number;
        }
        String phoneNumber = PhoneNumberUtils.formatNumberToE164(number, countryIso);
        return phoneNumber == null ? number : phoneNumber;
    }

    private String getCountryIso() {
        SubscriptionInfo info = SubscriptionManager.from(this.mPhone.getContext()).getActiveSubscriptionInfo(this.mPhone.getSubId());
        if (info == null) {
            return null;
        }
        return info.getCountryIso();
    }

    public void conference() {
        long conferenceConnectTime;
        ImsCall fgImsCall = this.mForegroundCall.getImsCall();
        if (fgImsCall == null) {
            log("conference no foreground ims call");
            return;
        }
        ImsCall bgImsCall = this.mBackgroundCall.getImsCall();
        if (bgImsCall == null) {
            log("conference no background ims call");
        } else if (fgImsCall.isCallSessionMergePending()) {
            log("conference: skip; foreground call already in process of merging.");
        } else if (bgImsCall.isCallSessionMergePending()) {
            log("conference: skip; background call already in process of merging.");
        } else if (!ignoreConference(fgImsCall, bgImsCall)) {
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
            String foregroundId = PhoneConfigurationManager.SSSS;
            ImsPhoneConnection foregroundConnection = this.mForegroundCall.getFirstConnection();
            if (foregroundConnection != null) {
                foregroundConnection.setConferenceConnectTime(conferenceConnectTime);
                foregroundConnection.handleMergeStart();
                foregroundId = foregroundConnection.getTelecomCallId();
                cacheConnectionTimeWithPhoneNumber(foregroundConnection);
            }
            String backgroundId = PhoneConfigurationManager.SSSS;
            ImsPhoneConnection backgroundConnection = findConnection(bgImsCall);
            if (backgroundConnection != null) {
                backgroundConnection.handleMergeStart();
                backgroundId = backgroundConnection.getTelecomCallId();
                cacheConnectionTimeWithPhoneNumber(backgroundConnection);
            }
            log("conference: fgCallId=" + foregroundId + ", bgCallId=" + backgroundId);
            try {
                fgImsCall.merge(bgImsCall);
            } catch (ImsException e) {
                log("conference " + e.getMessage());
                if (foregroundConnection != null) {
                    foregroundConnection.handleMergeComplete();
                }
                if (backgroundConnection != null) {
                    backgroundConnection.handleMergeComplete();
                }
            }
        }
    }

    public void explicitCallTransfer() {
    }

    @UnsupportedAppUsage
    public void clearDisconnected() {
        log("clearDisconnected");
        internalClearDisconnected();
        updatePhoneState();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public boolean canConference() {
        return this.mForegroundCall.getState() == Call.State.ACTIVE && this.mBackgroundCall.getState() == Call.State.HOLDING && !this.mBackgroundCall.isFull() && !this.mForegroundCall.isFull();
    }

    public void checkForDialIssues() throws CallStateException {
        if (SystemProperties.get("ro.telephony.disable-call", "false").equals("true")) {
            throw new CallStateException(5, "ro.telephony.disable-call has been used to disable calling.");
        } else if (this.mPendingMO != null) {
            throw new CallStateException(3, "Another outgoing call is already being dialed.");
        } else if (this.mRingingCall.isRinging()) {
            throw new CallStateException(4, "Can't place a call while another is ringing.");
        } else if (this.mForegroundCall.getState().isAlive() && this.mBackgroundCall.getState().isAlive()) {
            throw new CallStateException(6, "Already an active foreground and background call.");
        }
    }

    public boolean canTransfer() {
        return this.mForegroundCall.getState() == Call.State.ACTIVE && this.mBackgroundCall.getState() == Call.State.HOLDING;
    }

    private void internalClearDisconnected() {
        this.mRingingCall.clearDisconnected();
        this.mForegroundCall.clearDisconnected();
        this.mBackgroundCall.clearDisconnected();
        this.mHandoverCall.clearDisconnected();
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void updatePhoneState() {
        Object obj;
        PhoneConstants.State oldState = this.mState;
        ImsPhoneConnection imsPhoneConnection = this.mPendingMO;
        boolean isPendingMOIdle = imsPhoneConnection == null || !imsPhoneConnection.getState().isAlive();
        if (this.mRingingCall.isRinging()) {
            this.mState = PhoneConstants.State.RINGING;
        } else if (!isPendingMOIdle || !this.mForegroundCall.isIdle() || !this.mBackgroundCall.isIdle()) {
            this.mState = PhoneConstants.State.OFFHOOK;
        } else {
            this.mState = PhoneConstants.State.IDLE;
        }
        if (this.mState == PhoneConstants.State.IDLE && oldState != this.mState) {
            this.mVoiceCallEndedRegistrants.notifyRegistrants(getCallStateChangeAsyncResult());
        } else if (oldState == PhoneConstants.State.IDLE && oldState != this.mState) {
            this.mVoiceCallStartedRegistrants.notifyRegistrants(getCallStateChangeAsyncResult());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("updatePhoneState pendingMo = ");
        ImsPhoneConnection imsPhoneConnection2 = this.mPendingMO;
        if (imsPhoneConnection2 == null) {
            obj = "null";
        } else {
            obj = imsPhoneConnection2.getState();
        }
        sb.append(obj);
        sb.append(", fg= ");
        sb.append(this.mForegroundCall.getState());
        sb.append("(");
        sb.append(this.mForegroundCall.getConnections().size());
        sb.append("), bg= ");
        sb.append(this.mBackgroundCall.getState());
        sb.append("(");
        sb.append(this.mBackgroundCall.getConnections().size());
        sb.append(")");
        log(sb.toString());
        log("updatePhoneState oldState=" + oldState + ", newState=" + this.mState);
        PhoneConstants.State state = this.mState;
        if (state != oldState) {
            handleImsPhoneStateChanged(oldState, state);
            OemConstant.setOemCallState(OemConstant.OEM_CHECK_CALL_STATE, oldState, this.mState);
            if (this.mState != PhoneConstants.State.RINGING) {
                this.mPhone.notifyPhoneStateChanged();
                this.mMetrics.writePhoneState(this.mPhone.getPhoneId(), this.mState);
                notifyPhoneStateChanged(oldState, this.mState);
            }
        }
    }

    private void handleRadioNotAvailable() {
        pollCallsWhenSafe();
    }

    private void dumpState() {
        log("Phone State:" + this.mState);
        log("Ringing call: " + this.mRingingCall.toString());
        List l = this.mRingingCall.getConnections();
        int s = l.size();
        for (int i = 0; i < s; i++) {
            log(l.get(i).toString());
        }
        log("Foreground call: " + this.mForegroundCall.toString());
        List l2 = this.mForegroundCall.getConnections();
        int s2 = l2.size();
        for (int i2 = 0; i2 < s2; i2++) {
            log(l2.get(i2).toString());
        }
        log("Background call: " + this.mBackgroundCall.toString());
        List l3 = this.mBackgroundCall.getConnections();
        int s3 = l3.size();
        for (int i3 = 0; i3 < s3; i3++) {
            log(l3.get(i3).toString());
        }
    }

    public void setTtyMode(int ttyMode) {
        ImsManager imsManager = this.mImsManager;
        if (imsManager == null) {
            Log.w(LOG_TAG, "ImsManager is null when setting TTY mode");
            return;
        }
        try {
            imsManager.setTtyMode(ttyMode);
        } catch (ImsException e) {
            loge("setTtyMode : " + e);
            retryGetImsService();
        }
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        ImsManager imsManager = this.mImsManager;
        if (imsManager == null) {
            this.mPhone.sendErrorResponse(onComplete, getImsManagerIsNullException());
            return;
        }
        try {
            imsManager.setUiTTYMode(this.mPhone.getContext(), uiTtyMode, onComplete);
        } catch (ImsException e) {
            loge("setUITTYMode : " + e);
            this.mPhone.sendErrorResponse(onComplete, e);
            retryGetImsService();
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
        log("hangup connection");
        if (conn.getOwner() == this) {
            hangup(conn.getCall());
            return;
        }
        throw new CallStateException("ImsPhoneConnection " + conn + "does not belong to ImsPhoneCallTracker " + this);
    }

    public void hangup(ImsPhoneCall call) throws CallStateException {
        log("hangup call");
        if (call.getConnections().size() != 0) {
            ImsCall imsCall = call.getImsCall();
            boolean rejectCall = false;
            if (call == this.mRingingCall) {
                log("(ringing) hangup incoming");
                rejectCall = true;
            } else if (call == this.mForegroundCall) {
                if (call.isDialingOrAlerting()) {
                    log("(foregnd) hangup dialing or alerting...");
                    if (oemProcessPendingHangup(call, this.mPendingMO)) {
                        return;
                    }
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
                        imsCall.reject((int) AbstractSIMRecords.EVENT_GET_ALL_OPL_DONE);
                        this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 3);
                    } catch (ImsException e) {
                        throw new CallStateException(e.getMessage());
                    }
                } else {
                    imsCall.terminate((int) AbstractSIMRecords.EVENT_GET_ALL_OPL_DONE);
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 4);
                }
            } else if (this.mPendingMO != null && call == this.mForegroundCall) {
                this.mPendingMO.update(null, Call.State.DISCONNECTED);
                this.mPendingMO.onDisconnect();
                removeConnection(this.mPendingMO);
                this.mPendingMO = null;
                updatePhoneState();
                removeMessages(20);
            }
            this.mPhone.notifyPreciseCallStateChanged();
            return;
        }
        throw new CallStateException("no connections");
    }

    /* access modifiers changed from: protected */
    public void callEndCleanupHandOverCallIfAny() {
        if (this.mHandoverCall.mConnections.size() > 0) {
            log("callEndCleanupHandOverCallIfAny, mHandoverCall.mConnections=" + this.mHandoverCall.mConnections);
            this.mHandoverCall.mConnections.clear();
            this.mConnections.clear();
            this.mState = PhoneConstants.State.IDLE;
        }
    }

    public void sendUSSD(String ussdString, Message response) {
        log("sendUSSD");
        try {
            if (this.mUssdSession != null) {
                this.mUssdSession.sendUssd(ussdString);
                AsyncResult.forMessage(response, (Object) null, (Throwable) null);
                response.sendToTarget();
            } else if (this.mImsManager == null) {
                this.mPhone.sendErrorResponse(response, getImsManagerIsNullException());
            } else {
                ImsCallProfile profile = this.mImsManager.createCallProfile(1, 2);
                profile.setCallExtraInt("dialstring", 2);
                this.mUssdSession = this.mImsManager.makeCall(profile, new String[]{ussdString}, this.mImsUssdListener);
                this.mPendingUssd = response;
                log("pending ussd updated, " + this.mPendingUssd);
            }
        } catch (ImsException e) {
            loge("sendUSSD : " + e);
            this.mPhone.sendErrorResponse(response, e);
            retryGetImsService();
        }
    }

    public void cancelUSSD(Message msg) {
        ImsCall imsCall = this.mUssdSession;
        if (imsCall != null) {
            this.mPendingUssd = msg;
            imsCall.terminate((int) AbstractSIMRecords.EVENT_GET_ALL_OPL_DONE);
        }
    }

    /* access modifiers changed from: protected */
    public synchronized ImsPhoneConnection findConnection(ImsCall imsCall) {
        Iterator<ImsPhoneConnection> it = this.mConnections.iterator();
        while (it.hasNext()) {
            ImsPhoneConnection conn = it.next();
            if (conn.getImsCall() == imsCall) {
                return conn;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public synchronized void removeConnection(ImsPhoneConnection conn) {
        this.mConnections.remove(conn);
        if (this.mIsInEmergencyCall) {
            boolean isEmergencyCallInList = false;
            Iterator<ImsPhoneConnection> it = this.mConnections.iterator();
            while (true) {
                if (it.hasNext()) {
                    ImsPhoneConnection imsPhoneConnection = it.next();
                    if (imsPhoneConnection != null && imsPhoneConnection.isEmergency()) {
                        isEmergencyCallInList = true;
                        break;
                    }
                } else {
                    break;
                }
            }
            if (!isEmergencyCallInList) {
                this.mIsInEmergencyCall = false;
                this.mPhone.sendEmergencyCallStateChange(false);
                startRttEmcGuardTimer();
            }
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public synchronized void addConnection(ImsPhoneConnection conn) {
        this.mConnections.add(conn);
        if (conn.isEmergency()) {
            this.mIsInEmergencyCall = true;
            this.mPhone.sendEmergencyCallStateChange(true);
        }
    }

    /* access modifiers changed from: protected */
    public void processCallStateChange(ImsCall imsCall, Call.State state, int cause) {
        log("processCallStateChange " + imsCall + " state=" + state + " cause=" + cause);
        processCallStateChange(imsCall, state, cause, false);
    }

    /* access modifiers changed from: protected */
    public void processCallStateChange(ImsCall imsCall, Call.State state, int cause, boolean ignoreState) {
        ImsPhoneConnection conn;
        log("processCallStateChange state=" + state + " cause=" + cause + " ignoreState=" + ignoreState);
        if (imsCall != null && (conn = findConnection(imsCall)) != null) {
            conn.updateMediaCapabilities(imsCall);
            if (ignoreState) {
                conn.updateAddressDisplay(imsCall);
                conn.updateExtras(imsCall);
                maybeSetVideoCallProvider(conn, imsCall);
                return;
            }
            boolean changed = conn.update(imsCall, state);
            if (state == Call.State.DISCONNECTED) {
                changed = conn.onDisconnect(cause) || changed;
                conn.getCall().detach(conn);
                removeConnection(conn);
            }
            if (changed && conn.getCall() != this.mHandoverCall) {
                updatePhoneState();
                checkRttCallType();
                this.mPhone.notifyPreciseCallStateChanged();
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

    @VisibleForTesting
    public void addReasonCodeRemapping(Integer fromCode, String message, Integer toCode) {
        this.mImsReasonCodeMap.put(new Pair<>(fromCode, message), toCode);
    }

    @VisibleForTesting
    public int maybeRemapReasonCode(ImsReasonInfo reasonInfo) {
        int code = reasonInfo.getCode();
        String reason = reasonInfo.getExtraMessage();
        if (reason == null) {
            reason = PhoneConfigurationManager.SSSS;
        }
        log("maybeRemapReasonCode : fromCode = " + reasonInfo.getCode() + " ; message = " + reason);
        Pair<Integer, String> toCheck = new Pair<>(Integer.valueOf(code), reason);
        Pair<Integer, String> wildcardToCheck = new Pair<>(null, reason);
        if (this.mImsReasonCodeMap.containsKey(toCheck)) {
            int toCode = this.mImsReasonCodeMap.get(toCheck).intValue();
            log("maybeRemapReasonCode : fromCode = " + reasonInfo.getCode() + " ; message = " + reason + " ; toCode = " + toCode);
            return toCode;
        } else if (reason.isEmpty() || !this.mImsReasonCodeMap.containsKey(wildcardToCheck)) {
            return code;
        } else {
            int toCode2 = this.mImsReasonCodeMap.get(wildcardToCheck).intValue();
            log("maybeRemapReasonCode : fromCode(wildcard) = " + reasonInfo.getCode() + " ; message = " + reason + " ; toCode = " + toCode2);
            return toCode2;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @VisibleForTesting
    public int getDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo, Call.State callState) {
        switch (maybeRemapReasonCode(reasonInfo)) {
            case 0:
                if (this.mPhone.getDefaultPhone().getServiceStateTracker().mRestrictedState.isCsRestricted()) {
                    return 22;
                }
                if (this.mPhone.getDefaultPhone().getServiceStateTracker().mRestrictedState.isCsEmergencyRestricted()) {
                    return 24;
                }
                if (this.mPhone.getDefaultPhone().getServiceStateTracker().mRestrictedState.isCsNormalRestricted()) {
                    return 23;
                }
                break;
            case 106:
            case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED_INFINITE_RETRY /*{ENCODED_INT: 121}*/:
            case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_AUTH_FAILURE_ON_EMERGENCY_CALL /*{ENCODED_INT: 122}*/:
            case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_INVALID_DNS_ADDR /*{ENCODED_INT: 123}*/:
            case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_INVALID_PCSCF_OR_DNS_ADDRESS /*{ENCODED_INT: 124}*/:
            case 131:
            case 132:
            case 144:
                return 18;
            case 108:
                return 45;
            case 111:
                return 17;
            case TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_APN_TYPE_CONFLICT /*{ENCODED_INT: 112}*/:
            case 505:
                if (callState == Call.State.DIALING) {
                    return 62;
                }
                return 61;
            case 143:
                return 3;
            case 201:
            case 202:
            case 203:
            case 335:
                return 13;
            case 240:
                return 20;
            case CallFailCause.FDN_BLOCKED:
                return 21;
            case CallFailCause.IMEI_NOT_ACCEPTED:
                return 58;
            case CallFailCause.DIAL_MODIFIED_TO_USSD:
                return 46;
            case CallFailCause.DIAL_MODIFIED_TO_SS:
                return 47;
            case CallFailCause.DIAL_MODIFIED_TO_DIAL:
                return 48;
            case 247:
                return 66;
            case 248:
                return 69;
            case 249:
                return 70;
            case 250:
                return 67;
            case 251:
                return 68;
            case 321:
            case 331:
            case 340:
            case 362:
                return 12;
            case 332:
                return 12;
            case 333:
                return 7;
            case 337:
            case 341:
                return 8;
            case 338:
                return 4;
            case 339:
            case 361:
            case 510:
            case 1014:
                return 2;
            case 352:
            case 354:
                return 9;
            case 363:
                return 63;
            case 364:
                return 64;
            case AbstractSIMRecords.EVENT_GET_ALL_OPL_DONE /*{ENCODED_INT: 501}*/:
                return 3;
            case 1016:
                return 51;
            case 1403:
                return 53;
            case 1404:
                return 16;
            case 1405:
                return 55;
            case 1406:
                return 54;
            case 1407:
                return 59;
            case 1512:
                return 60;
            case 1514:
                return 71;
            case 1515:
                return 25;
        }
        return 36;
    }

    /* access modifiers changed from: protected */
    public boolean isPhoneInEcbMode() {
        ImsPhone imsPhone = this.mPhone;
        return imsPhone != null && imsPhone.isInEcm();
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void dialPendingMO() {
        boolean isPhoneInEcmMode = isPhoneInEcbMode();
        boolean isEmergencyNumber = this.mPendingMO.isEmergency();
        if (!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyNumber)) {
            sendEmptyMessage(20);
        } else {
            sendEmptyMessage(21);
        }
    }

    public void sendCallStartFailedDisconnect(ImsCall imsCall, ImsReasonInfo reasonInfo) {
        Call.State callState;
        this.mPendingMO = null;
        ImsPhoneConnection conn = findConnection(imsCall);
        if (conn != null) {
            callState = conn.getState();
        } else {
            callState = Call.State.DIALING;
        }
        int cause = getDisconnectCauseFromReasonInfo(reasonInfo, callState);
        setRedialAsEcc(cause);
        setVendorDisconnectCause(conn, reasonInfo);
        processCallStateChange(imsCall, Call.State.DISCONNECTED, cause);
        this.mPhone.notifyImsReason(reasonInfo);
    }

    @UnsupportedAppUsage
    public ImsUtInterface getUtInterface() throws ImsException {
        ImsManager imsManager = this.mImsManager;
        if (imsManager != null) {
            return imsManager.getSupplementaryServiceConfiguration();
        }
        throw getImsManagerIsNullException();
    }

    /* access modifiers changed from: protected */
    public void transferHandoverConnections(ImsPhoneCall call) {
        if (call.mConnections != null) {
            Iterator it = call.mConnections.iterator();
            while (it.hasNext()) {
                com.android.internal.telephony.Connection c = (com.android.internal.telephony.Connection) it.next();
                c.mPreHandoverState = call.mState;
                log("Connection state before handover is " + c.getStateBeforeHandover());
                setMultiPartyState(c);
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
            Iterator it2 = this.mHandoverCall.mConnections.iterator();
            while (it2.hasNext()) {
                com.android.internal.telephony.Connection c2 = (com.android.internal.telephony.Connection) it2.next();
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
        resetRingBackTone(call);
        if (this.mPendingMO != null) {
            logi("pending MO on handover, clearing...");
            this.mPendingMO = null;
        }
    }

    /* access modifiers changed from: protected */
    public void notifySrvccState(Call.SrvccState state) {
        log("notifySrvccState state=" + state);
        this.mSrvccState = state;
        if (this.mSrvccState == Call.SrvccState.COMPLETED) {
            resetState();
            transferHandoverConnections(this.mForegroundCall);
            transferHandoverConnections(this.mBackgroundCall);
            transferHandoverConnections(this.mRingingCall);
            updateForSrvccCompleted();
            oemNotifySrvccComplete();
        }
    }

    private void resetState() {
        this.mIsInEmergencyCall = false;
    }

    @Override // com.android.internal.telephony.CallTracker
    public void handleMessage(Message msg) {
        log("handleMessage what=" + msg.what);
        switch (msg.what) {
            case 14:
                if (this.pendingCallInEcm) {
                    dialInternal(this.mPendingMO, this.pendingCallClirMode, this.mPendingCallVideoState, this.mPendingIntentExtras);
                    this.mPendingIntentExtras = null;
                    this.pendingCallInEcm = false;
                }
                this.mPhone.unsetOnEcbModeExitResponse(this);
                return;
            case 15:
            case 16:
            case 17:
            case 19:
            case 24:
            default:
                return;
            case 18:
                ImsPhoneConnection imsPhoneConnection = this.mPendingMO;
                if (imsPhoneConnection != null) {
                    imsPhoneConnection.onDisconnect();
                    removeConnection(this.mPendingMO);
                    this.mPendingMO = null;
                }
                this.mPendingIntentExtras = null;
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
                return;
            case 20:
                ImsPhoneConnection imsPhoneConnection2 = this.mPendingMO;
                if (imsPhoneConnection2 != null && imsPhoneConnection2.getImsCall() == null) {
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
                    } catch (ImsException e) {
                        e.printStackTrace();
                        this.mPendingMO.setDisconnectCause(36);
                        sendEmptyMessageDelayed(18, 500);
                        return;
                    }
                } else {
                    return;
                }
            case 22:
                AsyncResult ar = (AsyncResult) msg.obj;
                ImsCall call = (ImsCall) ar.userObj;
                Long usage = Long.valueOf(((Long) ar.result).longValue());
                log("VT data usage update. usage = " + usage + ", imsCall = " + call);
                if (usage.longValue() > 0) {
                    updateVtDataUsage(call, usage.longValue());
                    return;
                }
                return;
            case 23:
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.result instanceof Pair) {
                    Pair<Boolean, Integer> p = (Pair) ar2.result;
                    onDataEnabledChanged(((Boolean) p.first).booleanValue(), ((Integer) p.second).intValue());
                    return;
                }
                return;
            case 25:
                if (msg.obj instanceof ImsCall) {
                    ImsCall imsCall = (ImsCall) msg.obj;
                    if (imsCall != this.mForegroundCall.getImsCall()) {
                        Rlog.i(LOG_TAG, "handoverCheck: no longer FG; check skipped.");
                        unregisterForConnectivityChanges();
                        return;
                    }
                    if (!this.mHasAttemptedStartOfCallHandover) {
                        this.mHasAttemptedStartOfCallHandover = true;
                    }
                    if (!imsCall.isWifiCall()) {
                        ImsPhoneConnection conn = findConnection(imsCall);
                        if (conn != null) {
                            Rlog.i(LOG_TAG, "handoverCheck: handover failed.");
                            conn.onHandoverToWifiFailed();
                        }
                        if (isVideoCall(imsCall) && conn.getDisconnectCause() == 0) {
                            registerForConnectivityChanges();
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            case 26:
                SomeArgs args = (SomeArgs) msg.obj;
                try {
                    handleFeatureCapabilityChanged((ImsFeature.Capabilities) args.arg1);
                    return;
                } finally {
                    args.recycle();
                }
            case 27:
                AsyncResult ar3 = (AsyncResult) msg.obj;
                ImsPhoneMmiCode mmiCode = new ImsPhoneMmiCode(this.mPhone);
                try {
                    mmiCode.setIsSsInfo(true);
                    mmiCode.processImsSsData(ar3);
                    return;
                } catch (ImsException e2) {
                    Rlog.e(LOG_TAG, "Exception in parsing SS Data: " + e2);
                    return;
                }
            case 28:
                Pair<ImsCall, ImsReasonInfo> callInfo = (Pair) ((AsyncResult) msg.obj).userObj;
                removeMessages(29);
                this.mPhone.getDefaultPhone().getServiceStateTracker().unregisterForNetworkAttached(this);
                ImsPhoneConnection oldConnection = findConnection((ImsCall) callInfo.first);
                if (oldConnection == null) {
                    sendCallStartFailedDisconnect((ImsCall) callInfo.first, (ImsReasonInfo) callInfo.second);
                    return;
                }
                this.mForegroundCall.detach(oldConnection);
                removeConnection(oldConnection);
                try {
                    oldConnection.onOriginalConnectionReplaced(this.mPhone.getDefaultPhone().dial(this.mLastDialString, this.mLastDialArgs));
                    return;
                } catch (CallStateException e3) {
                    sendCallStartFailedDisconnect((ImsCall) callInfo.first, (ImsReasonInfo) callInfo.second);
                    return;
                }
            case 29:
                Pair<ImsCall, ImsReasonInfo> callInfo2 = (Pair) msg.obj;
                this.mPhone.getDefaultPhone().getServiceStateTracker().unregisterForNetworkAttached(this);
                removeMessages(28);
                sendCallStartFailedDisconnect((ImsCall) callInfo2.first, (ImsReasonInfo) callInfo2.second);
                return;
            case 30:
                try {
                    answerWaitingCall();
                    return;
                } catch (ImsException e4) {
                    loge("handleMessage EVENT_ANSWER_WAITING_CALL exception=" + e4);
                    return;
                }
            case 31:
                try {
                    resumeForegroundCall();
                    return;
                } catch (ImsException e5) {
                    loge("handleMessage EVENT_RESUME_NOW_FOREGROUND_CALL exception=" + e5);
                    return;
                }
        }
    }

    /* JADX WARN: Type inference failed for: r15v0, types: [boolean, int] */
    private void updateVtDataUsage(ImsCall call, long dataUsage) {
        long oldUsage = 0;
        if (this.mVtDataUsageMap.containsKey(Integer.valueOf(call.uniqueId))) {
            oldUsage = this.mVtDataUsageMap.get(Integer.valueOf(call.uniqueId)).longValue();
        }
        long delta = dataUsage - oldUsage;
        this.mVtDataUsageMap.put(Integer.valueOf(call.uniqueId), Long.valueOf(dataUsage));
        log("updateVtDataUsage: call=" + call + ", delta=" + delta);
        long currentTime = SystemClock.elapsedRealtime();
        ? dataRoaming = this.mPhone.getServiceState().getDataRoaming();
        NetworkStats vtDataUsageSnapshot = new NetworkStats(currentTime, 1);
        vtDataUsageSnapshot.combineAllValues(this.mVtDataUsageSnapshot);
        vtDataUsageSnapshot.combineValues(new NetworkStats.Entry(getVtInterface(), -1, 1, 0, 1, dataRoaming == true ? 1 : 0, 1, delta / 2, 0, delta / 2, 0, 0));
        this.mVtDataUsageSnapshot = vtDataUsageSnapshot;
        NetworkStats vtDataUsageUidSnapshot = new NetworkStats(currentTime, 1);
        vtDataUsageUidSnapshot.combineAllValues(this.mVtDataUsageUidSnapshot);
        if (this.mDefaultDialerUid.get() == -1) {
            this.mDefaultDialerUid.set(getPackageUid(this.mPhone.getContext(), ((TelecomManager) this.mPhone.getContext().getSystemService("telecom")).getDefaultDialerPackage()));
        }
        vtDataUsageUidSnapshot.combineValues(new NetworkStats.Entry(getVtInterface(), this.mDefaultDialerUid.get(), 1, 0, 1, (int) dataRoaming, 1, delta / 2, 0, delta / 2, 0, 0));
        this.mVtDataUsageUidSnapshot = vtDataUsageUidSnapshot;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.CallTracker
    @UnsupportedAppUsage
    public void log(String msg) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "] " + msg);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void loge(String msg) {
        Rlog.e(LOG_TAG, "[" + this.mPhone.getPhoneId() + "] " + msg);
    }

    /* access modifiers changed from: package-private */
    public void logi(String msg) {
        Rlog.i(LOG_TAG, "[" + this.mPhone.getPhoneId() + "] " + msg);
    }

    /* access modifiers changed from: package-private */
    public void logHoldSwapState(String loc) {
        String holdSwapState = "???";
        switch (this.mHoldSwitchingState) {
            case INACTIVE:
                holdSwapState = "INACTIVE";
                break;
            case PENDING_SINGLE_CALL_HOLD:
                holdSwapState = "PENDING_SINGLE_CALL_HOLD";
                break;
            case PENDING_SINGLE_CALL_UNHOLD:
                holdSwapState = "PENDING_SINGLE_CALL_UNHOLD";
                break;
            case SWAPPING_ACTIVE_AND_HELD:
                holdSwapState = "SWAPPING_ACTIVE_AND_HELD";
                break;
            case HOLDING_TO_ANSWER_INCOMING:
                holdSwapState = "HOLDING_TO_ANSWER_INCOMING";
                break;
            case PENDING_RESUME_FOREGROUND_AFTER_FAILURE:
                holdSwapState = "PENDING_RESUME_FOREGROUND_AFTER_FAILURE";
                break;
            case HOLDING_TO_DIAL_OUTGOING:
                holdSwapState = "HOLDING_TO_DIAL_OUTGOING";
                break;
        }
        logi("holdSwapState set to " + holdSwapState + " at " + loc);
    }

    public void logState() {
        if (VERBOSE_STATE_LOGGING) {
            Rlog.v(LOG_TAG, "Current IMS PhoneCall State:\n" + " Foreground: " + this.mForegroundCall + "\n" + " Background: " + this.mBackgroundCall + "\n" + " Ringing: " + this.mRingingCall + "\n" + " Handover: " + this.mHandoverCall + "\n");
        }
    }

    @Override // com.android.internal.telephony.CallTracker
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
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
        pw.println(" mMmTelCapabilities=" + this.mMmTelCapabilities);
        pw.println(" mDefaultDialerUid=" + this.mDefaultDialerUid.get());
        pw.println(" mVtDataUsageSnapshot=" + this.mVtDataUsageSnapshot);
        pw.println(" mVtDataUsageUidSnapshot=" + this.mVtDataUsageUidSnapshot);
        pw.println(" mCallQualityMetrics=" + this.mCallQualityMetrics);
        pw.println(" mCallQualityMetricsHistory=" + this.mCallQualityMetricsHistory);
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        try {
            if (this.mImsManager != null) {
                this.mImsManager.dump(fd, pw, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<ImsPhoneConnection> arrayList = this.mConnections;
        if (arrayList != null && arrayList.size() > 0) {
            pw.println("mConnections:");
            for (int i = 0; i < this.mConnections.size(); i++) {
                pw.println("  [" + i + "]: " + this.mConnections.get(i));
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.CallTracker
    public void handlePollCalls(AsyncResult ar) {
    }

    public ImsEcbm getEcbmInterface() throws ImsException {
        ImsManager imsManager = this.mImsManager;
        if (imsManager != null) {
            return imsManager.getEcbmInterface();
        }
        throw getImsManagerIsNullException();
    }

    public ImsMultiEndpoint getMultiEndpointInterface() throws ImsException {
        ImsManager imsManager = this.mImsManager;
        if (imsManager != null) {
            try {
                return imsManager.getMultiEndpointInterface();
            } catch (ImsException e) {
                if (e.getCode() == 902) {
                    return null;
                }
                throw e;
            }
        } else {
            throw getImsManagerIsNullException();
        }
    }

    public boolean isInEmergencyCall() {
        return this.mIsInEmergencyCall;
    }

    public boolean isImsCapabilityAvailable(int capability, int regTech) {
        return getImsRegistrationTech() == regTech && this.mMmTelCapabilities.isCapable(capability);
    }

    public boolean isVolteEnabled() {
        return (getImsRegistrationTech() == 0) && this.mMmTelCapabilities.isCapable(1);
    }

    public boolean isVowifiEnabled() {
        return (getImsRegistrationTech() == 1) && this.mMmTelCapabilities.isCapable(1);
    }

    @Override // com.android.internal.telephony.AbstractCallTracker
    public boolean getVowifiRegStatus() {
        return this.isVowifiRegistered;
    }

    public boolean isVideoCallEnabled() {
        return this.mMmTelCapabilities.isCapable(2);
    }

    @Override // com.android.internal.telephony.CallTracker
    public PhoneConstants.State getState() {
        return this.mState;
    }

    public int getImsRegistrationTech() {
        ImsManager imsManager = this.mImsManager;
        if (imsManager != null) {
            return imsManager.getRegistrationTech();
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public void retryGetImsService() {
        if (!this.mImsManager.isServiceAvailable()) {
            this.mImsManagerConnector.connect();
        }
    }

    /* access modifiers changed from: protected */
    public void setVideoCallProvider(ImsPhoneConnection conn, ImsCall imsCall) throws RemoteException {
        IImsVideoCallProvider oldIImsVideoCallProvider;
        IImsVideoCallProvider imsVideoCallProvider = imsCall.getCallSession().getVideoCallProvider();
        ImsVideoCallProviderWrapper oldImsVideoCallProvider = null;
        Connection.VideoProvider videoProvider = conn.getVideoProvider();
        if (videoProvider instanceof ImsVideoCallProviderWrapper) {
            oldImsVideoCallProvider = (ImsVideoCallProviderWrapper) videoProvider;
        }
        if (!(oldImsVideoCallProvider == null || (oldIImsVideoCallProvider = oldImsVideoCallProvider.getProvider()) == null || imsVideoCallProvider == null)) {
            int oldProviderId = oldIImsVideoCallProvider.getProviderId();
            int newProviderId = imsVideoCallProvider.getProviderId();
            log("odl provider id = " + oldProviderId + ", new provider id = " + newProviderId);
            if (oldProviderId == newProviderId) {
                loge("setVideoCallProvider(), not changed, ignore!!!");
                return;
            }
        }
        if (imsVideoCallProvider != null) {
            boolean useVideoPauseWorkaround = this.mPhone.getContext().getResources().getBoolean(17891563);
            ImsVideoCallProviderWrapper imsVideoCallProviderWrapper = new ImsVideoCallProviderWrapper(imsVideoCallProvider);
            if (useVideoPauseWorkaround) {
                imsVideoCallProviderWrapper.setUseVideoPauseWorkaround(useVideoPauseWorkaround);
            }
            conn.setVideoProvider(imsVideoCallProviderWrapper);
            imsVideoCallProviderWrapper.registerForDataUsageUpdate(this, 22, imsCall);
            imsVideoCallProviderWrapper.addImsVideoProviderCallback(conn);
        }
    }

    public boolean isUtEnabled() {
        return this.mMmTelCapabilities.isCapable(4);
    }

    /* access modifiers changed from: protected */
    public String cleanseInstantLetteringMessage(String callSubject) {
        CarrierConfigManager configMgr;
        PersistableBundle carrierConfig;
        if (TextUtils.isEmpty(callSubject) || (configMgr = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")) == null || (carrierConfig = configMgr.getConfigForSubId(this.mPhone.getSubId())) == null) {
            return callSubject;
        }
        String invalidCharacters = carrierConfig.getString("carrier_instant_lettering_invalid_chars_string");
        if (!TextUtils.isEmpty(invalidCharacters)) {
            callSubject = callSubject.replaceAll(invalidCharacters, PhoneConfigurationManager.SSSS);
        }
        String escapedCharacters = carrierConfig.getString("carrier_instant_lettering_escaped_chars_string");
        if (!TextUtils.isEmpty(escapedCharacters)) {
            return escapeChars(escapedCharacters, callSubject);
        }
        return callSubject;
    }

    private String escapeChars(String toEscape, String source) {
        StringBuilder escaped = new StringBuilder();
        char[] charArray = source.toCharArray();
        for (char c : charArray) {
            if (toEscape.contains(Character.toString(c))) {
                escaped.append("\\");
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    @Override // com.android.internal.telephony.imsphone.ImsPullCall
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

    /* access modifiers changed from: protected */
    public ImsException getImsManagerIsNullException() {
        return new ImsException("no ims manager", 102);
    }

    /* access modifiers changed from: protected */
    public boolean shouldDisconnectActiveCallOnAnswer(ImsCall activeCall, ImsCall incomingCall) {
        if (activeCall == null || incomingCall == null || !this.mDropVideoCallWhenAnsweringAudioCall) {
            return false;
        }
        boolean isActiveCallVideo = isVideoCall(activeCall);
        boolean isActiveCallOnWifi = activeCall.isWifiCall();
        boolean isVoWifiEnabled = this.mImsManager.isWfcEnabledByPlatform() && this.mImsManager.isWfcEnabledByUser();
        boolean isIncomingCallAudio = !incomingCall.isVideoCall();
        log("shouldDisconnectActiveCallOnAnswer : isActiveCallVideo=" + isActiveCallVideo + " isActiveCallOnWifi=" + isActiveCallOnWifi + " isIncomingCallAudio=" + isIncomingCallAudio + " isVowifiEnabled=" + isVoWifiEnabled);
        if (!isActiveCallVideo || !isActiveCallOnWifi || !isIncomingCallAudio || isVoWifiEnabled) {
            return false;
        }
        return true;
    }

    public NetworkStats getVtDataUsage(boolean perUidStats) {
        if (this.mState != PhoneConstants.State.IDLE) {
            Iterator<ImsPhoneConnection> it = this.mConnections.iterator();
            while (it.hasNext()) {
                Connection.VideoProvider videoProvider = it.next().getVideoProvider();
                if (videoProvider != null) {
                    videoProvider.onRequestConnectionDataUsage();
                }
            }
        }
        return perUidStats ? this.mVtDataUsageUidSnapshot : this.mVtDataUsageSnapshot;
    }

    public void registerPhoneStateListener(PhoneStateListener listener) {
        this.mPhoneStateListeners.add(listener);
    }

    public void unregisterPhoneStateListener(PhoneStateListener listener) {
        this.mPhoneStateListeners.remove(listener);
    }

    private void notifyPhoneStateChanged(PhoneConstants.State oldState, PhoneConstants.State newState) {
        for (PhoneStateListener listener : this.mPhoneStateListeners) {
            listener.onPhoneStateChanged(oldState, newState);
        }
    }

    /* access modifiers changed from: protected */
    public void modifyVideoCall(ImsCall imsCall, int newVideoState) {
        ImsPhoneConnection conn = findConnection(imsCall);
        if (conn != null) {
            int oldVideoState = conn.getVideoState();
            if (conn.getVideoProvider() != null) {
                conn.getVideoProvider().onSendSessionModifyRequest(new VideoProfile(oldVideoState), new VideoProfile(newVideoState));
            }
        }
    }

    public boolean isViLteDataMetered() {
        return this.mIsViLteDataMetered;
    }

    /* access modifiers changed from: protected */
    public void onDataEnabledChanged(boolean enabled, int reason) {
        int reasonCode;
        ImsManager imsManager;
        log("onDataEnabledChanged: enabled=" + enabled + ", reason=" + reason);
        if (this.mIsFirstDataEnabledChanged) {
            loge("ignore first onDataEnabledChanged");
            this.mIsFirstDataEnabledChanged = false;
            return;
        }
        this.mIsDataEnabled = enabled;
        if (!this.mIsViLteDataMetered) {
            StringBuilder sb = new StringBuilder();
            sb.append("Ignore data ");
            sb.append(enabled ? "enabled" : "disabled");
            sb.append(" - carrier policy indicates that data is not metered for ViLTE calls.");
            log(sb.toString());
        } else if (!this.mIsDataEnabled || !isRoamingOnAndRoamingSettingOff()) {
            Iterator<ImsPhoneConnection> it = this.mConnections.iterator();
            while (true) {
                boolean isLocalVideoCapable = true;
                if (!it.hasNext()) {
                    break;
                }
                ImsPhoneConnection conn = it.next();
                ImsCall imsCall = conn.getImsCall();
                if (!enabled && (imsCall == null || !imsCall.isWifiCall())) {
                    isLocalVideoCapable = false;
                }
                conn.setLocalVideoCapable(isLocalVideoCapable);
            }
            if (reason == 3) {
                reasonCode = 1405;
            } else if (reason == 2) {
                reasonCode = 1406;
            } else {
                reasonCode = 1406;
            }
            maybeNotifyDataDisabled(enabled, reasonCode);
            handleDataEnabledChange(enabled, reasonCode);
            if (!this.mShouldUpdateImsConfigOnDisconnect && reason != 0 && this.mCarrierConfigLoaded && (imsManager = this.mImsManager) != null) {
                imsManager.updateImsServiceConfig(true);
            }
        } else {
            log("Ignore data on when roaming");
        }
    }

    /* access modifiers changed from: protected */
    public void maybeNotifyDataDisabled(boolean enabled, int reasonCode) {
        if (!enabled) {
            Iterator<ImsPhoneConnection> it = this.mConnections.iterator();
            while (it.hasNext()) {
                ImsPhoneConnection conn = it.next();
                ImsCall imsCall = conn.getImsCall();
                if (imsCall != null && imsCall.isVideoCall() && !imsCall.isWifiCall() && conn.hasCapabilities(3)) {
                    if (reasonCode == 1406) {
                        conn.onConnectionEvent("android.telephony.event.EVENT_DOWNGRADE_DATA_DISABLED", null);
                    } else if (reasonCode == 1405) {
                        conn.onConnectionEvent("android.telephony.event.EVENT_DOWNGRADE_DATA_LIMIT_REACHED", null);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleDataEnabledChange(boolean enabled, int reasonCode) {
        if (!enabled) {
            Iterator<ImsPhoneConnection> it = this.mConnections.iterator();
            while (it.hasNext()) {
                ImsPhoneConnection conn = it.next();
                ImsCall imsCall = conn.getImsCall();
                if (imsCall != null && imsCall.isVideoCall() && !imsCall.isWifiCall()) {
                    log("handleDataEnabledChange - downgrading " + conn);
                    downgradeVideoCall(reasonCode, conn);
                }
            }
        } else if (this.mSupportPauseVideo) {
            Iterator<ImsPhoneConnection> it2 = this.mConnections.iterator();
            while (it2.hasNext()) {
                ImsPhoneConnection conn2 = it2.next();
                log("handleDataEnabledChange - resuming " + conn2);
                if (VideoProfile.isPaused(conn2.getVideoState()) && conn2.wasVideoPausedFromSource(2)) {
                    conn2.resumeVideo(2);
                }
            }
            this.mShouldUpdateImsConfigOnDisconnect = false;
        }
    }

    /* access modifiers changed from: private */
    public void downgradeVideoCall(int reasonCode, ImsPhoneConnection conn) {
        ImsCall imsCall = conn.getImsCall();
        if (imsCall == null) {
            return;
        }
        if (conn.hasCapabilities(3) && (!this.mSupportPauseVideo || ignoreCarrierPauseSupport())) {
            log("downgradeVideoCall :: callId=" + conn.getTelecomCallId() + " Downgrade to audio");
            modifyVideoCall(imsCall, 0);
        } else if (!this.mSupportPauseVideo || reasonCode == 1407 || !isCarrierPauseAllowed(imsCall)) {
            log("downgradeVideoCall :: callId=" + conn.getTelecomCallId() + " Disconnect call.");
            imsCall.terminate((int) AbstractSIMRecords.EVENT_GET_ALL_OPL_DONE, reasonCode);
        } else {
            log("downgradeVideoCall :: callId=" + conn.getTelecomCallId() + " Pause audio");
            this.mShouldUpdateImsConfigOnDisconnect = true;
            conn.pauseVideo(2);
        }
    }

    /* access modifiers changed from: protected */
    public void resetImsCapabilities() {
        log("Resetting Capabilities...");
        boolean tmpIsVideoCallEnabled = isVideoCallEnabled();
        this.mMmTelCapabilities = new MmTelFeature.MmTelCapabilities();
        boolean isVideoEnabled = isVideoCallEnabled();
        if (tmpIsVideoCallEnabled != isVideoEnabled) {
            this.mPhone.notifyForVideoCapabilityChanged(isVideoEnabled);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isWifiConnected() {
        NetworkInfo ni;
        ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        if (cm == null || (ni = cm.getActiveNetworkInfo()) == null || !ni.isConnected() || ni.getType() != 1) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void registerForConnectivityChanges() {
        ConnectivityManager cm;
        if (!this.mIsMonitoringConnectivity && this.mNotifyVtHandoverToWifiFail && (cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity")) != null) {
            Rlog.i(LOG_TAG, "registerForConnectivityChanges");
            NetworkCapabilities capabilities = new NetworkCapabilities();
            capabilities.addTransportType(1);
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.setCapabilities(capabilities);
            cm.registerNetworkCallback(builder.build(), this.mNetworkCallback);
            this.mIsMonitoringConnectivity = true;
        }
    }

    /* access modifiers changed from: private */
    public void unregisterForConnectivityChanges() {
        ConnectivityManager cm;
        if (this.mIsMonitoringConnectivity && this.mNotifyVtHandoverToWifiFail && (cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity")) != null) {
            Rlog.i(LOG_TAG, "unregisterForConnectivityChanges");
            cm.unregisterNetworkCallback(this.mNetworkCallback);
            this.mIsMonitoringConnectivity = false;
        }
    }

    /* access modifiers changed from: private */
    public void scheduleHandoverCheck() {
        ImsCall fgCall = this.mForegroundCall.getImsCall();
        ImsPhoneConnection conn = this.mForegroundCall.getFirstConnection();
        if (this.mNotifyVtHandoverToWifiFail && fgCall != null && isVideoCall(fgCall) && conn != null && conn.getDisconnectCause() == 0 && !hasMessages(25)) {
            Rlog.i(LOG_TAG, "scheduleHandoverCheck: schedule");
            sendMessageDelayed(obtainMessage(25, fgCall), 60000);
        }
    }

    public boolean isCarrierDowngradeOfVtCallSupported() {
        log("isCarrierDowngradeOfVtCallSupported: " + this.mSupportDowngradeVtToAudio);
        return this.mSupportDowngradeVtToAudio;
    }

    @VisibleForTesting
    public void setDataEnabled(boolean isDataEnabled) {
        this.mIsDataEnabled = isDataEnabled;
    }

    /* access modifiers changed from: private */
    public void pruneCallQualityMetricsHistory() {
        if (this.mCallQualityMetricsHistory.size() > 10) {
            this.mCallQualityMetricsHistory.poll();
        }
    }

    private void handleFeatureCapabilityChanged(ImsFeature.Capabilities capabilities) {
        int status;
        boolean tmpIsVideoCallEnabled = isVideoCallEnabled();
        StringBuilder sb = new StringBuilder((int) TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_IFACE_AND_POL_FAMILY_MISMATCH);
        sb.append("handleFeatureCapabilityChanged: ");
        sb.append(capabilities);
        this.mMmTelCapabilities = new MmTelFeature.MmTelCapabilities(capabilities);
        boolean isVideoEnabled = isVideoCallEnabled();
        boolean z = false;
        boolean isVideoEnabledStatechanged = tmpIsVideoCallEnabled != isVideoEnabled;
        sb.append(" isVideoEnabledStateChanged=");
        sb.append(isVideoEnabledStatechanged);
        if (isVideoEnabledStatechanged) {
            log("handleFeatureCapabilityChanged - notifyForVideoCapabilityChanged=" + isVideoEnabled);
            this.mPhone.notifyForVideoCapabilityChanged(isVideoEnabled);
        }
        log(sb.toString());
        log("handleFeatureCapabilityChanged: isVolteEnabled=" + isVolteEnabled() + ", isVideoCallEnabled=" + isVideoCallEnabled() + ", isVowifiEnabled=" + isVowifiEnabled() + ", isUtEnabled=" + isUtEnabled());
        this.mPhone.setImsRegistered(isVolteEnabled() || isVowifiEnabled());
        String registerImsType = PhoneConfigurationManager.SSSS;
        if (isVolteEnabled()) {
            registerImsType = IMS_VOLTE_ENABLE;
        } else if (isVowifiEnabled()) {
            registerImsType = IMS_VOWIFI_ENABLE;
        }
        this.isVowifiRegistered = registerImsType == IMS_VOWIFI_ENABLE;
        SystemProperties.set(PRO_IMS_TYPE + this.mPhone.getPhoneId(), registerImsType);
        if (isVolteEnabled() || isVowifiEnabled()) {
            z = true;
        }
        if (z) {
            status = 4;
        } else {
            status = 5;
        }
        this.mPhone.notifySrvccStateChanged(status);
        if (OemTelephonyUtils.isCTcardAsDefaultDataSubscription(this.mPhone.getDefaultPhone())) {
            log("wenlong VoLteServiceStateChanged need poll");
            this.mPhone.getDefaultPhone().getServiceStateTracker().pollState();
        }
        log("handleFeatureCapabilityChanged: registerImsType = " + registerImsType + ",phoneId = " + this.mPhone.getPhoneId() + ", status " + status);
        this.mPhone.onFeatureCapabilityChanged();
        this.mMetrics.writeOnImsCapabilities(this.mPhone.getPhoneId(), getImsRegistrationTech(), this.mMmTelCapabilities);
    }

    @VisibleForTesting
    public void onCallHoldReceived(ImsCall imsCall) {
        log("onCallHoldReceived");
        ImsPhoneConnection conn = findConnection(imsCall);
        if (conn != null) {
            if (!this.mOnHoldToneStarted && ((ImsPhoneCall.isLocalTone(imsCall) || this.mAlwaysPlayRemoteHoldTone) && conn.getState() == Call.State.ACTIVE)) {
                this.mPhone.startOnHoldTone(conn);
                this.mOnHoldToneStarted = true;
                this.mOnHoldToneId = System.identityHashCode(conn);
            }
            conn.onConnectionEvent("android.telecom.event.CALL_REMOTELY_HELD", null);
            if (this.mPhone.getContext().getResources().getBoolean(17891563) && this.mSupportPauseVideo && VideoProfile.isVideo(conn.getVideoState())) {
                conn.changeToPausedState();
            }
        }
        SuppServiceNotification supp = new SuppServiceNotification();
        supp.notificationType = 1;
        supp.code = 2;
        this.mPhone.notifySuppSvcNotification(supp);
        this.mMetrics.writeOnImsCallHoldReceived(this.mPhone.getPhoneId(), imsCall.getCallSession());
    }

    @VisibleForTesting
    public void setAlwaysPlayRemoteHoldTone(boolean shouldPlayRemoteHoldTone) {
        this.mAlwaysPlayRemoteHoldTone = shouldPlayRemoteHoldTone;
    }

    /* access modifiers changed from: private */
    public String getNetworkCountryIso() {
        ServiceStateTracker sst;
        LocaleTracker lt;
        ImsPhone imsPhone = this.mPhone;
        if (imsPhone == null || (sst = imsPhone.getServiceStateTracker()) == null || (lt = sst.getLocaleTracker()) == null) {
            return PhoneConfigurationManager.SSSS;
        }
        return lt.getCurrentCountry();
    }

    @Override // com.android.internal.telephony.CallTracker
    public ImsPhone getPhone() {
        return this.mPhone;
    }

    /* access modifiers changed from: protected */
    public ImsPhoneConnection makeImsPhoneConnectionForMO(String dialString, boolean isEmergencyNumber) {
        return new ImsPhoneConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, isEmergencyNumber);
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
        return new ImsPhoneConnection(imsPhone, imsCall, this, imsPhoneCall, isUnknown);
    }

    /* access modifiers changed from: protected */
    public ImsCall takeCall(IImsCallSession c, Bundle extras) throws ImsException {
        return this.mImsManager.takeCall(c, extras, this.mImsCallListener);
    }

    /* access modifiers changed from: protected */
    public boolean isEmergencyNumber(String dialString) {
        return this.mPhoneNumberUtilsProxy.isEmergencyNumber(dialString);
    }

    /* access modifiers changed from: protected */
    public void checkforCsfb() throws CallStateException {
    }

    /* access modifiers changed from: protected */
    public boolean canDailOnCallTerminated() {
        return this.mPendingMO != null;
    }

    /* access modifiers changed from: protected */
    public void setRedialAsEcc(int cause) {
    }

    /* access modifiers changed from: protected */
    public void setVendorDisconnectCause(ImsPhoneConnection conn, ImsReasonInfo reasonInfo) {
    }

    /* access modifiers changed from: protected */
    public int updateDisconnectCause(int cause, ImsPhoneConnection conn) {
        return cause;
    }

    /* access modifiers changed from: protected */
    public void setMultiPartyState(com.android.internal.telephony.Connection c) {
    }

    /* access modifiers changed from: protected */
    public void resetRingBackTone(ImsPhoneCall call) {
    }

    /* access modifiers changed from: protected */
    public void updateForSrvccCompleted() {
    }

    /* access modifiers changed from: protected */
    public void logDebugMessagesWithOpFormat(String category, String action, ImsPhoneConnection conn, String msg) {
    }

    /* access modifiers changed from: protected */
    public void logDebugMessagesWithDumpFormat(String category, ImsPhoneConnection conn, String msg) {
    }

    /* access modifiers changed from: protected */
    public void switchWfcModeIfRequired(ImsManager imsManager, boolean isWfcEnabled, boolean isEmergencyNumber) {
    }

    /* access modifiers changed from: protected */
    public AsyncResult getCallStateChangeAsyncResult() {
        return new AsyncResult((Object) null, (Object) null, (Throwable) null);
    }

    /* access modifiers changed from: protected */
    public boolean isCarrierPauseAllowed(ImsCall imsCall) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void releasePendingMOIfRequired() {
    }

    /* access modifiers changed from: protected */
    public boolean isRoamingOnAndRoamingSettingOff() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean ignoreCarrierPauseSupport() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void checkIncomingCallInRttEmcGuardTime(ImsPhoneConnection conn) {
    }

    /* access modifiers changed from: protected */
    public void checkRttCallType() {
    }

    /* access modifiers changed from: protected */
    public void startRttEmcGuardTimer() {
    }

    /* access modifiers changed from: protected */
    public String getVtInterface() {
        return "vt_data0";
    }

    /* access modifiers changed from: private */
    public boolean isNeedToResumeHoldCall(boolean handleWaitingCall) {
        Call.State foregroundCallState = this.mForegroundCall.getState();
        Call.State ringCallState = this.mRingingCall.getState();
        Call.State backCallState = this.mBackgroundCall.getState();
        log("isNeedToResumeHoldCall:" + backCallState);
        log("isNeedToResumeHoldCall, foregroundCallState" + foregroundCallState);
        log("ringCallState: " + ringCallState + ", mBackgroundCall.isIdle: " + this.mBackgroundCall.isIdle());
        if (!handleWaitingCall) {
            if (!this.mForegroundCall.isIdle() || foregroundCallState == Call.State.DISCONNECTING || !this.mRingingCall.isIdle() || ringCallState == Call.State.DISCONNECTING || this.mBackgroundCall.isIdle() || backCallState != Call.State.HOLDING) {
                return false;
            }
            log("need to resume background Call automatically!");
            return true;
        } else if (!this.mForegroundCall.isIdle() || foregroundCallState == Call.State.DISCONNECTING || this.mBackgroundCall.isIdle() || backCallState != Call.State.HOLDING) {
            return false;
        } else {
            log("need to resume background Call automatically!");
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void resumeBackgroundCall() {
        ImsCall imsCall = this.mBackgroundCall.getImsCall();
        log("resumeBackgroundCall: " + imsCall);
        if (imsCall != null) {
            try {
                imsCall.resume();
            } catch (ImsException e) {
                loge("resumeBackgroundCall: " + e);
            }
        } else {
            log("not backgound call need to be resume!!!");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isVideoCall(ImsCall imsCall) {
        return imsCall.isVideoCall() || (this.mTreatDowngradedVideoCallsAsVideoCalls && imsCall.wasVideoCall());
    }

    /* access modifiers changed from: protected */
    public boolean ignoreConference(ImsCall fgImsCall, ImsCall bgImsCall) {
        return false;
    }
}

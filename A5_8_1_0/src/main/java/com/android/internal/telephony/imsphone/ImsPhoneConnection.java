package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.SystemClock;
import android.telecom.Connection.RttTextStream;
import android.telecom.Connection.VideoProvider;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.ims.ImsCall;
import com.android.ims.ImsCallProfile;
import com.android.ims.ImsException;
import com.android.ims.internal.ImsVideoCallProviderWrapper;
import com.android.ims.internal.ImsVideoCallProviderWrapper.ImsVideoProviderWrapperCallback;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Connection.PostDialState;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.uicc.SpnOverride;
import java.util.Objects;

public class ImsPhoneConnection extends Connection implements ImsVideoProviderWrapperCallback {
    /* renamed from: -com-android-internal-telephony-Call$StateSwitchesValues */
    private static final /* synthetic */ int[] f3-com-android-internal-telephony-Call$StateSwitchesValues = null;
    private static final boolean DBG = true;
    private static final int EVENT_DTMF_DELAY_DONE = 5;
    private static final int EVENT_DTMF_DONE = 1;
    private static final int EVENT_NEXT_POST_DIAL = 3;
    private static final int EVENT_PAUSE_DONE = 2;
    private static final int EVENT_WAKE_LOCK_TIMEOUT = 4;
    private static final String LOG_TAG = "ImsPhoneConnection";
    private static final int PAUSE_DELAY_MILLIS = 3000;
    private static final int WAKE_LOCK_TIMEOUT_MILLIS = 60000;
    private long mConferenceConnectTime;
    private long mDisconnectTime;
    private boolean mDisconnected;
    private int mDtmfToneDelay;
    private Bundle mExtras;
    private Handler mHandler;
    private ImsCall mImsCall;
    private ImsVideoCallProviderWrapper mImsVideoCallProviderWrapper;
    private boolean mIsEmergency;
    private boolean mIsMergeInProcess;
    private boolean mIsVideoEnabled;
    private ImsPhoneCallTracker mOwner;
    private ImsPhoneCall mParent;
    private WakeLock mPartialWakeLock;
    private int mPreciseDisconnectCause;
    private ImsRttTextHandler mRttTextHandler;
    private RttTextStream mRttTextStream;
    private boolean mShouldIgnoreVideoStateChanges;
    private UUSInfo mUusInfo;

    class MyHandler extends Handler {
        MyHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ImsPhoneConnection.this.mHandler.sendMessageDelayed(ImsPhoneConnection.this.mHandler.obtainMessage(5), (long) ImsPhoneConnection.this.mDtmfToneDelay);
                    return;
                case 2:
                case 3:
                case 5:
                    ImsPhoneConnection.this.processNextPostDialChar();
                    return;
                case 4:
                    ImsPhoneConnection.this.releaseWakeLock();
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: -getcom-android-internal-telephony-Call$StateSwitchesValues */
    private static /* synthetic */ int[] m3-getcom-android-internal-telephony-Call$StateSwitchesValues() {
        if (f3-com-android-internal-telephony-Call$StateSwitchesValues != null) {
            return f3-com-android-internal-telephony-Call$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ACTIVE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.ALERTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DIALING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.DISCONNECTED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.HOLDING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.IDLE.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.INCOMING.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.WAITING.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        f3-com-android-internal-telephony-Call$StateSwitchesValues = iArr;
        return iArr;
    }

    public ImsPhoneConnection(Phone phone, ImsCall imsCall, ImsPhoneCallTracker ct, ImsPhoneCall parent, boolean isUnknown) {
        super(5);
        this.mExtras = new Bundle();
        this.mConferenceConnectTime = 0;
        this.mDtmfToneDelay = 0;
        this.mIsEmergency = false;
        this.mShouldIgnoreVideoStateChanges = false;
        this.mPreciseDisconnectCause = 0;
        this.mIsMergeInProcess = false;
        this.mIsVideoEnabled = true;
        createWakeLock(phone.getContext());
        acquireWakeLock();
        this.mOwner = ct;
        this.mHandler = new MyHandler(this.mOwner.getLooper());
        this.mImsCall = imsCall;
        if (imsCall == null || imsCall.getCallProfile() == null) {
            this.mNumberPresentation = 3;
            this.mCnapNamePresentation = 3;
        } else {
            this.mAddress = imsCall.getCallProfile().getCallExtra("oi");
            this.mCnapName = imsCall.getCallProfile().getCallExtra("cna");
            this.mNumberPresentation = ImsCallProfile.OIRToPresentation(imsCall.getCallProfile().getCallExtraInt("oir"));
            this.mCnapNamePresentation = ImsCallProfile.OIRToPresentation(imsCall.getCallProfile().getCallExtraInt("cnap"));
            updateMediaCapabilities(imsCall);
        }
        this.mIsIncoming = isUnknown ^ 1;
        this.mCreateTime = System.currentTimeMillis();
        this.mUusInfo = null;
        updateExtras(imsCall);
        this.mParent = parent;
        this.mParent.attach(this, this.mIsIncoming ? State.INCOMING : State.DIALING);
        fetchDtmfToneDelay(phone);
        if (phone.getContext().getResources().getBoolean(17957057)) {
            setAudioModeIsVoip(true);
        }
    }

    public ImsPhoneConnection(Phone phone, String dialString, ImsPhoneCallTracker ct, ImsPhoneCall parent, boolean isEmergency) {
        this(phone, dialString, ct, parent, isEmergency, null);
    }

    public ImsPhoneConnection(Phone phone, String dialString, ImsPhoneCallTracker ct, ImsPhoneCall parent, boolean isEmergency, Bundle extras) {
        super(5);
        this.mExtras = new Bundle();
        this.mConferenceConnectTime = 0;
        this.mDtmfToneDelay = 0;
        this.mIsEmergency = false;
        this.mShouldIgnoreVideoStateChanges = false;
        this.mPreciseDisconnectCause = 0;
        this.mIsMergeInProcess = false;
        this.mIsVideoEnabled = true;
        createWakeLock(phone.getContext());
        acquireWakeLock();
        boolean isConferenceUri = false;
        boolean isSkipSchemaParsing = false;
        if (extras != null) {
            isConferenceUri = extras.getBoolean("org.codeaurora.extra.DIAL_CONFERENCE_URI", false);
            isSkipSchemaParsing = extras.getBoolean("org.codeaurora.extra.SKIP_SCHEMA_PARSING", false);
        }
        this.mOwner = ct;
        this.mHandler = new MyHandler(this.mOwner.getLooper());
        this.mDialString = dialString;
        if (isConferenceUri || isSkipSchemaParsing) {
            this.mAddress = dialString;
            this.mPostDialString = SpnOverride.MVNO_TYPE_NONE;
        } else {
            this.mAddress = PhoneNumberUtils.extractNetworkPortionAlt(dialString);
            this.mPostDialString = PhoneNumberUtils.extractPostDialPortion(dialString);
        }
        this.mIsIncoming = false;
        this.mCnapName = null;
        this.mCnapNamePresentation = 1;
        this.mNumberPresentation = 1;
        this.mCreateTime = System.currentTimeMillis();
        this.mParent = parent;
        parent.attachFake(this, State.DIALING);
        this.mIsEmergency = isEmergency;
        fetchDtmfToneDelay(phone);
        if (phone.getContext().getResources().getBoolean(17957057)) {
            setAudioModeIsVoip(true);
        }
    }

    public void dispose() {
    }

    static boolean equalsHandlesNulls(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }

    static boolean equalsBaseDialString(String a, String b) {
        return a == null ? b == null : b != null ? a.startsWith(b) : false;
    }

    private int applyLocalCallCapabilities(ImsCallProfile localProfile, int capabilities) {
        Rlog.i(LOG_TAG, "applyLocalCallCapabilities - localProfile = " + localProfile);
        capabilities = Connection.removeCapability(capabilities, 4);
        if (this.mIsVideoEnabled) {
            switch (localProfile.mCallType) {
                case 3:
                case 4:
                    capabilities = Connection.addCapability(capabilities, 4);
                    break;
            }
            return capabilities;
        }
        Rlog.i(LOG_TAG, "applyLocalCallCapabilities - disabling video (overidden)");
        return capabilities;
    }

    private static int applyRemoteCallCapabilities(ImsCallProfile remoteProfile, int capabilities) {
        Rlog.w(LOG_TAG, "applyRemoteCallCapabilities - remoteProfile = " + remoteProfile);
        capabilities = Connection.removeCapability(capabilities, 8);
        switch (remoteProfile.mCallType) {
            case 3:
            case 4:
                return Connection.addCapability(capabilities, 8);
            default:
                return capabilities;
        }
    }

    public String getOrigDialString() {
        return this.mDialString;
    }

    public ImsPhoneCall getCall() {
        return this.mParent;
    }

    public long getDisconnectTime() {
        return this.mDisconnectTime;
    }

    public long getHoldingStartTime() {
        return this.mHoldingStartTime;
    }

    public long getHoldDurationMillis() {
        if (getState() != State.HOLDING) {
            return 0;
        }
        return SystemClock.elapsedRealtime() - this.mHoldingStartTime;
    }

    public void setDisconnectCause(int cause) {
        this.mCause = cause;
    }

    public String getVendorDisconnectCause() {
        return null;
    }

    public ImsPhoneCallTracker getOwner() {
        return this.mOwner;
    }

    public State getState() {
        if (this.mDisconnected) {
            return State.DISCONNECTED;
        }
        return super.getState();
    }

    public void hangup() throws CallStateException {
        if (this.mDisconnected) {
            throw new CallStateException("disconnected");
        }
        this.mOwner.hangup(this);
    }

    public void separate() throws CallStateException {
        throw new CallStateException("not supported");
    }

    public void proceedAfterWaitChar() {
        if (this.mPostDialState != PostDialState.WAIT) {
            Rlog.w(LOG_TAG, "ImsPhoneConnection.proceedAfterWaitChar(): Expected getPostDialState() to be WAIT but was " + this.mPostDialState);
            return;
        }
        setPostDialState(PostDialState.STARTED);
        processNextPostDialChar();
    }

    public void proceedAfterWildChar(String str) {
        if (this.mPostDialState != PostDialState.WILD) {
            Rlog.w(LOG_TAG, "ImsPhoneConnection.proceedAfterWaitChar(): Expected getPostDialState() to be WILD but was " + this.mPostDialState);
            return;
        }
        setPostDialState(PostDialState.STARTED);
        StringBuilder buf = new StringBuilder(str);
        buf.append(this.mPostDialString.substring(this.mNextPostDialChar));
        this.mPostDialString = buf.toString();
        this.mNextPostDialChar = 0;
        Rlog.d(LOG_TAG, "proceedAfterWildChar: new postDialString is " + this.mPostDialString);
        processNextPostDialChar();
    }

    public void cancelPostDial() {
        setPostDialState(PostDialState.CANCELLED);
    }

    void onHangupLocal() {
        this.mCause = 3;
    }

    public boolean onDisconnect(int cause) {
        Rlog.d(LOG_TAG, "onDisconnect: cause=" + cause);
        if (this.mCause != 3 || cause == 16) {
            this.mCause = cause;
        }
        return onDisconnect();
    }

    public boolean onDisconnect() {
        boolean changed = false;
        if (!this.mDisconnected) {
            this.mDisconnectTime = System.currentTimeMillis();
            this.mDuration = SystemClock.elapsedRealtime() - this.mConnectTimeReal;
            this.mDisconnected = true;
            this.mOwner.mPhone.notifyDisconnect(this);
            if (this.mParent != null) {
                changed = this.mParent.connectionDisconnected(this);
            } else {
                Rlog.d(LOG_TAG, "onDisconnect: no parent");
            }
            synchronized (this) {
                if (this.mImsCall != null) {
                    this.mImsCall.close();
                }
                this.mImsCall = null;
            }
        }
        releaseWakeLock();
        return changed;
    }

    void onConnectedInOrOut() {
        this.mConnectTime = System.currentTimeMillis();
        this.mConnectTimeReal = SystemClock.elapsedRealtime();
        this.mDuration = 0;
        Rlog.d(LOG_TAG, "onConnectedInOrOut: connectTime=" + this.mConnectTime);
        if (!this.mIsIncoming) {
            processNextPostDialChar();
        }
        releaseWakeLock();
    }

    void onStartedHolding() {
        this.mHoldingStartTime = SystemClock.elapsedRealtime();
    }

    private boolean processPostDialChar(char c) {
        if (PhoneNumberUtils.is12Key(c)) {
            this.mOwner.sendDtmf(c, this.mHandler.obtainMessage(1));
        } else if (c == ',') {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 3000);
        } else if (c == ';') {
            setPostDialState(PostDialState.WAIT);
        } else if (c != 'N') {
            return false;
        } else {
            setPostDialState(PostDialState.WILD);
        }
        return true;
    }

    protected void finalize() {
        releaseWakeLock();
    }

    private void processNextPostDialChar() {
        if (this.mPostDialState != PostDialState.CANCELLED) {
            char c;
            if (this.mPostDialString == null || this.mPostDialString.length() <= this.mNextPostDialChar) {
                setPostDialState(PostDialState.COMPLETE);
                c = 0;
            } else {
                setPostDialState(PostDialState.STARTED);
                String str = this.mPostDialString;
                int i = this.mNextPostDialChar;
                this.mNextPostDialChar = i + 1;
                c = str.charAt(i);
                if (!processPostDialChar(c)) {
                    this.mHandler.obtainMessage(3).sendToTarget();
                    Rlog.e(LOG_TAG, "processNextPostDialChar: c=" + c + " isn't valid!");
                    return;
                }
            }
            notifyPostDialListenersNextChar(c);
            Registrant postDialHandler = this.mOwner.mPhone.getPostDialHandler();
            if (postDialHandler != null) {
                Message notifyMessage = postDialHandler.messageForRegistrant();
                if (notifyMessage != null) {
                    PostDialState state = this.mPostDialState;
                    AsyncResult ar = AsyncResult.forMessage(notifyMessage);
                    ar.result = this;
                    ar.userObj = state;
                    notifyMessage.arg1 = c;
                    notifyMessage.sendToTarget();
                }
            }
        }
    }

    private void setPostDialState(PostDialState s) {
        if (this.mPostDialState != PostDialState.STARTED && s == PostDialState.STARTED) {
            acquireWakeLock();
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4), 60000);
        } else if (this.mPostDialState == PostDialState.STARTED && s != PostDialState.STARTED) {
            this.mHandler.removeMessages(4);
            releaseWakeLock();
        }
        this.mPostDialState = s;
        notifyPostDialListeners();
    }

    private void createWakeLock(Context context) {
        this.mPartialWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
    }

    private void acquireWakeLock() {
        Rlog.d(LOG_TAG, "acquireWakeLock");
        this.mPartialWakeLock.acquire();
    }

    void releaseWakeLock() {
        if (this.mPartialWakeLock != null) {
            synchronized (this.mPartialWakeLock) {
                if (this.mPartialWakeLock.isHeld()) {
                    Rlog.d(LOG_TAG, "releaseWakeLock");
                    this.mPartialWakeLock.release();
                }
            }
        }
    }

    private void fetchDtmfToneDelay(Phone phone) {
        PersistableBundle b = ((CarrierConfigManager) phone.getContext().getSystemService("carrier_config")).getConfigForSubId(phone.getSubId());
        if (b != null) {
            this.mDtmfToneDelay = b.getInt("ims_dtmf_tone_delay_int");
        }
    }

    public int getNumberPresentation() {
        return this.mNumberPresentation;
    }

    public UUSInfo getUUSInfo() {
        return this.mUusInfo;
    }

    public Connection getOrigConnection() {
        return null;
    }

    public synchronized boolean isMultiparty() {
        return this.mImsCall != null ? this.mImsCall.isMultiparty() : false;
    }

    public synchronized boolean isConferenceHost() {
        return this.mImsCall != null ? this.mImsCall.isConferenceHost() : false;
    }

    public boolean isMemberOfPeerConference() {
        return isConferenceHost() ^ 1;
    }

    public synchronized ImsCall getImsCall() {
        return this.mImsCall;
    }

    public synchronized void setImsCall(ImsCall imsCall) {
        this.mImsCall = imsCall;
    }

    public void changeParent(ImsPhoneCall parent) {
        this.mParent = parent;
    }

    public boolean update(ImsCall imsCall, State state) {
        updateCallStateToVideoCallProvider(state);
        if (state == State.ACTIVE) {
            if (imsCall.isPendingHold()) {
                Rlog.w(LOG_TAG, "update : state is ACTIVE, but call is pending hold, skipping");
                return false;
            }
            if (this.mParent.getState().isRinging() || this.mParent.getState().isDialing()) {
                onConnectedInOrOut();
            }
            if (this.mParent.getState().isRinging() || this.mParent == this.mOwner.mBackgroundCall) {
                this.mParent.detach(this);
                this.mParent = this.mOwner.mForegroundCall;
                this.mParent.attach(this);
            }
        } else if (state == State.HOLDING) {
            onStartedHolding();
        }
        boolean updateParent = this.mParent.update(this, imsCall, state);
        boolean updateAddressDisplay = updateAddressDisplay(imsCall);
        boolean updateMediaCapabilities = updateMediaCapabilities(imsCall);
        boolean updateExtras = updateExtras(imsCall);
        if (updateParent || updateAddressDisplay || updateMediaCapabilities) {
            updateExtras = true;
        }
        return updateExtras;
    }

    public int getPreciseDisconnectCause() {
        return this.mPreciseDisconnectCause;
    }

    public void setPreciseDisconnectCause(int cause) {
        this.mPreciseDisconnectCause = cause;
    }

    public void onDisconnectConferenceParticipant(Uri endpoint) {
        ImsCall imsCall = getImsCall();
        if (imsCall != null) {
            try {
                imsCall.removeParticipants(new String[]{endpoint.toString()});
            } catch (ImsException e) {
                Rlog.e(LOG_TAG, "onDisconnectConferenceParticipant: no session in place. Failed to disconnect endpoint = " + endpoint);
            }
        }
    }

    public void setConferenceConnectTime(long conferenceConnectTime) {
        this.mConferenceConnectTime = conferenceConnectTime;
    }

    public long getConferenceConnectTime() {
        return this.mConferenceConnectTime;
    }

    public boolean updateAddressDisplay(ImsCall imsCall) {
        if (imsCall == null) {
            return false;
        }
        boolean changed = false;
        ImsCallProfile callProfile = imsCall.getCallProfile();
        if (callProfile != null && isIncoming()) {
            String address = callProfile.getCallExtra("oi");
            String name = callProfile.getCallExtra("cna");
            int nump = ImsCallProfile.OIRToPresentation(callProfile.getCallExtraInt("oir"));
            int namep = ImsCallProfile.OIRToPresentation(callProfile.getCallExtraInt("cnap"));
            Rlog.d(LOG_TAG, "updateAddressDisplay: callId = " + getTelecomCallId() + " address = " + Rlog.pii(LOG_TAG, address) + " name = " + name + " nump = " + nump + " namep = " + namep);
            if (!this.mIsMergeInProcess) {
                if (!equalsBaseDialString(this.mAddress, address)) {
                    this.mAddress = address;
                    changed = true;
                }
                if (TextUtils.isEmpty(name)) {
                    if (!TextUtils.isEmpty(this.mCnapName)) {
                        this.mCnapName = SpnOverride.MVNO_TYPE_NONE;
                        changed = true;
                    }
                } else if (!name.equals(this.mCnapName)) {
                    this.mCnapName = name;
                    changed = true;
                }
                if (this.mNumberPresentation != nump) {
                    this.mNumberPresentation = nump;
                    changed = true;
                }
                if (this.mCnapNamePresentation != namep) {
                    this.mCnapNamePresentation = namep;
                    changed = true;
                }
            }
        }
        return changed;
    }

    public boolean updateMediaCapabilities(ImsCall imsCall) {
        if (imsCall == null) {
            return false;
        }
        boolean changed = false;
        try {
            ImsCallProfile negotiatedCallProfile = imsCall.getCallProfile();
            if (negotiatedCallProfile != null) {
                int oldVideoState = getVideoState();
                int newVideoState = ImsCallProfile.getVideoStateFromImsCallProfile(negotiatedCallProfile);
                if (oldVideoState != newVideoState) {
                    if (VideoProfile.isPaused(oldVideoState) && (VideoProfile.isPaused(newVideoState) ^ 1) != 0) {
                        this.mShouldIgnoreVideoStateChanges = false;
                    }
                    if (this.mShouldIgnoreVideoStateChanges) {
                        Rlog.d(LOG_TAG, "updateMediaCapabilities - ignoring video state change due to paused state.");
                    } else {
                        updateVideoState(newVideoState);
                        changed = true;
                    }
                    if (!VideoProfile.isPaused(oldVideoState) && VideoProfile.isPaused(newVideoState)) {
                        this.mShouldIgnoreVideoStateChanges = true;
                    }
                }
            }
            int capabilities = getConnectionCapabilities();
            if (this.mOwner.isCarrierDowngradeOfVtCallSupported()) {
                capabilities = Connection.addCapability(capabilities, 3);
            } else {
                capabilities = Connection.removeCapability(capabilities, 3);
            }
            ImsCallProfile localCallProfile = imsCall.getLocalCallProfile();
            Rlog.v(LOG_TAG, "update localCallProfile=" + localCallProfile);
            if (localCallProfile != null) {
                capabilities = applyLocalCallCapabilities(localCallProfile, capabilities);
            }
            ImsCallProfile remoteCallProfile = imsCall.getRemoteCallProfile();
            Rlog.v(LOG_TAG, "update remoteCallProfile=" + remoteCallProfile);
            if (remoteCallProfile != null) {
                capabilities = applyRemoteCallCapabilities(remoteCallProfile, capabilities);
            }
            if (getConnectionCapabilities() != capabilities) {
                setConnectionCapabilities(capabilities);
                changed = true;
            }
            if (!this.mOwner.isViLteDataMetered()) {
                Rlog.v(LOG_TAG, "data is not metered");
            } else if (this.mImsVideoCallProviderWrapper != null) {
                this.mImsVideoCallProviderWrapper.setIsVideoEnabled(hasCapabilities(4));
            }
            int newAudioQuality = getAudioQualityFromCallProfile(localCallProfile, remoteCallProfile);
            if (getAudioQuality() != newAudioQuality) {
                setAudioQuality(newAudioQuality);
                changed = true;
            }
        } catch (ImsException e) {
        }
        return changed;
    }

    private void updateVideoState(int newVideoState) {
        if (this.mImsVideoCallProviderWrapper != null) {
            this.mImsVideoCallProviderWrapper.onVideoStateChanged(newVideoState);
        }
        setVideoState(newVideoState);
    }

    public void sendRttModifyRequest(RttTextStream textStream) {
        getImsCall().sendRttModifyRequest();
        setCurrentRttTextStream(textStream);
    }

    public void sendRttModifyResponse(RttTextStream textStream) {
        boolean accept = textStream != null;
        getImsCall().sendRttModifyResponse(accept);
        if (accept) {
            setCurrentRttTextStream(textStream);
            startRttTextProcessing();
            return;
        }
        Rlog.e(LOG_TAG, "sendRttModifyResponse: foreground call has no connections");
    }

    public void onRttMessageReceived(String message) {
        getOrCreateRttTextHandler().sendToInCall(message);
    }

    public void setCurrentRttTextStream(RttTextStream rttTextStream) {
        this.mRttTextStream = rttTextStream;
    }

    public void startRttTextProcessing() {
        getOrCreateRttTextHandler().initialize(this.mRttTextStream);
    }

    private ImsRttTextHandler getOrCreateRttTextHandler() {
        if (this.mRttTextHandler != null) {
            return this.mRttTextHandler;
        }
        this.mRttTextHandler = new ImsRttTextHandler(Looper.getMainLooper(), new -$Lambda$sWM4a9wDlM8dWP6obUKx8Ol60Ws(this));
        return this.mRttTextHandler;
    }

    /* renamed from: lambda$-com_android_internal_telephony_imsphone_ImsPhoneConnection_37013 */
    /* synthetic */ void m4x78256a1(String message) {
        getImsCall().sendRttMessage(message);
    }

    private void updateWifiStateFromExtras(Bundle extras) {
        if (extras.containsKey("CallRadioTech") || extras.containsKey("callRadioTech")) {
            ImsCall call = getImsCall();
            boolean isWifi = false;
            if (call != null) {
                isWifi = call.isWifiCall();
            }
            if (isWifi() != isWifi) {
                setWifi(isWifi);
            }
        }
    }

    boolean updateExtras(ImsCall imsCall) {
        if (imsCall == null) {
            return false;
        }
        ImsCallProfile callProfile = imsCall.getCallProfile();
        Bundle extras = callProfile != null ? callProfile.mCallExtras : null;
        if (extras == null) {
            Rlog.d(LOG_TAG, "Call profile extras are null.");
        }
        boolean changed = areBundlesEqual(extras, this.mExtras) ^ 1;
        if (changed) {
            updateWifiStateFromExtras(extras);
            this.mExtras.clear();
            this.mExtras.putAll(extras);
            setConnectionExtras(this.mExtras);
        }
        return changed;
    }

    private static boolean areBundlesEqual(Bundle extras, Bundle newExtras) {
        boolean z = true;
        if (extras == null || newExtras == null) {
            if (extras != newExtras) {
                z = false;
            }
            return z;
        } else if (extras.size() != newExtras.size()) {
            return false;
        } else {
            for (String key : extras.keySet()) {
                if (key != null && !Objects.equals(extras.get(key), newExtras.get(key))) {
                    return false;
                }
            }
            return true;
        }
    }

    private int getAudioQualityFromCallProfile(ImsCallProfile localCallProfile, ImsCallProfile remoteCallProfile) {
        int i = 2;
        if (localCallProfile == null || remoteCallProfile == null || localCallProfile.mMediaProfile == null) {
            return 1;
        }
        boolean isEvsCodecHighDef = (localCallProfile.mMediaProfile.mAudioQuality == 18 || localCallProfile.mMediaProfile.mAudioQuality == 19) ? true : localCallProfile.mMediaProfile.mAudioQuality == 20;
        boolean isHighDef = (localCallProfile.mMediaProfile.mAudioQuality == 2 || localCallProfile.mMediaProfile.mAudioQuality == 6 || isEvsCodecHighDef) ? remoteCallProfile.mRestrictCause == 0 : false;
        if (!isHighDef) {
            i = 1;
        }
        return i;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ImsPhoneConnection objId: ");
        sb.append(System.identityHashCode(this));
        sb.append(" telecomCallID: ");
        sb.append(getTelecomCallId());
        sb.append(" address: ");
        sb.append(Rlog.pii(LOG_TAG, getAddress()));
        sb.append(" ImsCall: ");
        synchronized (this) {
            if (this.mImsCall == null) {
                sb.append("null");
            } else {
                sb.append(this.mImsCall);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public void setVideoProvider(VideoProvider videoProvider) {
        super.setVideoProvider(videoProvider);
        if (videoProvider instanceof ImsVideoCallProviderWrapper) {
            this.mImsVideoCallProviderWrapper = (ImsVideoCallProviderWrapper) videoProvider;
        }
    }

    protected boolean isEmergency() {
        return this.mIsEmergency;
    }

    public void onReceiveSessionModifyResponse(int status, VideoProfile requestProfile, VideoProfile responseProfile) {
        if (status == 1 && this.mShouldIgnoreVideoStateChanges) {
            int currentVideoState = getVideoState();
            int newVideoState = responseProfile.getVideoState();
            int changedBits = (currentVideoState ^ newVideoState) & 3;
            if (changedBits != 0) {
                currentVideoState = (currentVideoState & (~(changedBits & currentVideoState))) | (changedBits & newVideoState);
                Rlog.d(LOG_TAG, "onReceiveSessionModifyResponse : received " + VideoProfile.videoStateToString(requestProfile.getVideoState()) + " / " + VideoProfile.videoStateToString(responseProfile.getVideoState()) + " while paused ; sending new videoState = " + VideoProfile.videoStateToString(currentVideoState));
                setVideoState(currentVideoState);
            }
        }
    }

    public void pauseVideo(int source) {
        if (this.mImsVideoCallProviderWrapper != null) {
            this.mImsVideoCallProviderWrapper.pauseVideo(getVideoState(), source);
        }
    }

    public void resumeVideo(int source) {
        if (this.mImsVideoCallProviderWrapper != null) {
            this.mImsVideoCallProviderWrapper.resumeVideo(getVideoState(), source);
        }
    }

    public boolean wasVideoPausedFromSource(int source) {
        if (this.mImsVideoCallProviderWrapper == null) {
            return false;
        }
        return this.mImsVideoCallProviderWrapper.wasVideoPausedFromSource(source);
    }

    private void updateCallStateToVideoCallProvider(State state) {
        if (this.mImsVideoCallProviderWrapper != null) {
            this.mImsVideoCallProviderWrapper.onCallStateChanged(toTelecomCallState(state));
        }
    }

    private static int toTelecomCallState(State state) {
        switch (m3-getcom-android-internal-telephony-Call$StateSwitchesValues()[state.ordinal()]) {
            case 1:
                return 4;
            case 2:
                return 1;
            case 3:
                return 1;
            case 4:
                return 7;
            case 5:
                return 10;
            case 6:
                return 3;
            case 7:
                return 0;
            case 8:
                return 2;
            case 9:
                return 2;
            default:
                return 0;
        }
    }

    public void handleMergeStart() {
        this.mIsMergeInProcess = true;
        onConnectionEvent("android.telecom.event.MERGE_START", null);
    }

    public void handleMergeComplete() {
        this.mIsMergeInProcess = false;
        onConnectionEvent("android.telecom.event.MERGE_COMPLETE", null);
    }

    public void changeToPausedState() {
        int newVideoState = getVideoState() | 4;
        Rlog.i(LOG_TAG, "ImsPhoneConnection: changeToPausedState - setting paused bit; newVideoState=" + VideoProfile.videoStateToString(newVideoState));
        updateVideoState(newVideoState);
        this.mShouldIgnoreVideoStateChanges = true;
    }

    public void changeToUnPausedState() {
        int newVideoState = getVideoState() & -5;
        Rlog.i(LOG_TAG, "ImsPhoneConnection: changeToUnPausedState - unsetting paused bit; newVideoState=" + VideoProfile.videoStateToString(newVideoState));
        updateVideoState(newVideoState);
        this.mShouldIgnoreVideoStateChanges = false;
    }

    public void setVideoEnabled(boolean isVideoEnabled) {
        this.mIsVideoEnabled = isVideoEnabled;
        Rlog.i(LOG_TAG, "setVideoEnabled: mIsVideoEnabled = " + this.mIsVideoEnabled + "; updating local video availability.");
        updateMediaCapabilities(getImsCall());
    }
}

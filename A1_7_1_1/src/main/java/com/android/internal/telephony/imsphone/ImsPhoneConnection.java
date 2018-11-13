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
import android.telecom.ConferenceParticipant;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.ims.ImsCall;
import com.android.ims.ImsCallProfile;
import com.android.ims.ImsException;
import com.android.ims.internal.ImsVideoCallProviderWrapper.ImsVideoProviderWrapperCallback;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Connection.PostDialState;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.UUSInfo;
import com.mediatek.internal.telephony.ConferenceCallMessageHandler;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImsPhoneConnection extends Connection implements ImsVideoProviderWrapperCallback {
    private static final boolean DBG = true;
    private static final int EVENT_DTMF_DELAY_DONE = 5;
    private static final int EVENT_DTMF_DONE = 1;
    private static final int EVENT_NEXT_POST_DIAL = 3;
    private static final int EVENT_PAUSE_DONE = 2;
    private static final int EVENT_WAKE_LOCK_TIMEOUT = 4;
    private static final String LOG_TAG = "ImsPhoneConnection";
    private static final int PAUSE_DELAY_MILLIS = 3000;
    private static final int WAKE_LOCK_TIMEOUT_MILLIS = 60000;
    private int mCallIdBeforeDisconnected = -1;
    private ArrayList<String> mConfDialStrings = null;
    private long mConferenceConnectTime = 0;
    private List<ConferenceParticipant> mConferenceParticipants = null;
    private long mDisconnectTime;
    private boolean mDisconnected;
    private int mDtmfToneDelay = 0;
    private Bundle mExtras = new Bundle();
    private Handler mHandler;
    private ImsCall mImsCall;
    private boolean mIsEmergency = false;
    private boolean mIsWifiStateFromExtras = false;
    private ImsPhoneCallTracker mOwner;
    private ImsPhoneCall mParent;
    private WakeLock mPartialWakeLock;
    private boolean mShouldIgnoreVideoStateChanges = false;
    private UUSInfo mUusInfo;
    private String mVendorCause = null;

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

    public ImsPhoneConnection(Phone phone, ImsCall imsCall, ImsPhoneCallTracker ct, ImsPhoneCall parent, boolean isUnknown) {
        boolean z = false;
        super(5);
        createWakeLock(phone.getContext());
        acquireWakeLock();
        this.mOwner = ct;
        this.mHandler = new MyHandler(this.mOwner.getLooper());
        this.mImsCall = imsCall;
        if (imsCall != null) {
            Rlog.i(LOG_TAG, "ImsPhoneConnection Constructor, call profile: " + imsCall.getCallProfile());
        }
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
        if (!isUnknown) {
            z = true;
        }
        this.mIsIncoming = z;
        this.mCreateTime = System.currentTimeMillis();
        this.mUusInfo = null;
        updateWifiState();
        updateExtras(imsCall);
        this.mParent = parent;
        this.mParent.attach(this, this.mIsIncoming ? State.INCOMING : State.DIALING);
        fetchDtmfToneDelay(phone);
    }

    public ImsPhoneConnection(Phone phone, String dialString, ImsPhoneCallTracker ct, ImsPhoneCall parent, boolean isEmergency) {
        super(5);
        createWakeLock(phone.getContext());
        acquireWakeLock();
        this.mOwner = ct;
        this.mHandler = new MyHandler(this.mOwner.getLooper());
        this.mDialString = dialString;
        this.mAddress = dialString;
        this.mPostDialString = UsimPBMemInfo.STRING_NOT_SET;
        if (!PhoneNumberUtils.isUriNumber(dialString)) {
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

    private static int applyLocalCallCapabilities(ImsCallProfile localProfile, int capabilities) {
        Rlog.w(LOG_TAG, "applyLocalCallCapabilities - localProfile = " + localProfile);
        capabilities = Connection.removeCapability(capabilities, 4);
        switch (localProfile.mCallType) {
            case 3:
            case 4:
                return Connection.addCapability(capabilities, 4);
            default:
                return capabilities;
        }
    }

    private static int applyRemoteCallCapabilities(ImsCallProfile remoteProfile, int capabilities) {
        Rlog.w(LOG_TAG, "applyRemoteCallCapabilities - remoteProfile = " + remoteProfile);
        capabilities = Connection.removeCapability(capabilities, 8);
        switch (remoteProfile.mCallType) {
            case 3:
            case 4:
                capabilities = Connection.addCapability(capabilities, 8);
                break;
        }
        if (remoteProfile.mMediaProfile.mVideoDirection != 1) {
            return Connection.removeCapability(capabilities, 64);
        }
        Rlog.d(LOG_TAG, "Set video ringtone capability");
        return Connection.addCapability(capabilities, 64);
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
        return this.mVendorCause;
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
        if (this.mOwner != null) {
            this.mOwner.logDebugMessagesWithOpFormat("CC", "Hangup", this, "ImsphoneConnection.hangup");
        }
        if (this.mDisconnected) {
            throw new CallStateException(ConferenceCallMessageHandler.STATUS_DISCONNECTED);
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
        } else if (isIncoming() && getConnectTime() == 0 && this.mCause == 3) {
            this.mCause = 16;
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
            this.mCallIdBeforeDisconnected = getCallId();
            if (this.mImsCall != null) {
                this.mImsCall.close();
            }
            this.mImsCall = null;
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
        synchronized (this.mPartialWakeLock) {
            if (this.mPartialWakeLock.isHeld()) {
                Rlog.d(LOG_TAG, "releaseWakeLock");
                this.mPartialWakeLock.release();
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

    public boolean isMultiparty() {
        return this.mImsCall != null ? this.mImsCall.isMultiparty() : false;
    }

    public boolean isConferenceHost() {
        if (this.mImsCall == null) {
            return false;
        }
        return this.mImsCall.isConferenceHost();
    }

    public boolean isMemberOfPeerConference() {
        return !isConferenceHost();
    }

    public ImsCall getImsCall() {
        return this.mImsCall;
    }

    public void setImsCall(ImsCall imsCall) {
        this.mImsCall = imsCall;
    }

    public void changeParent(ImsPhoneCall parent) {
        this.mParent = parent;
    }

    public boolean update(ImsCall imsCall, State state) {
        if (state == State.ACTIVE) {
            if (imsCall.isPendingHold()) {
                Rlog.w(LOG_TAG, "update : state is ACTIVE, but call is pending hold, skipping");
                return false;
            }
            if (this.mParent.getState().isRinging() || this.mParent.getState().isDialing()) {
                onConnectedInOrOut();
            }
            if ((this.mParent.getState().isRinging() || this.mParent == this.mOwner.mBackgroundCall) && this.mParent != this.mOwner.mHandoverCall) {
                Rlog.d(LOG_TAG, "update() - Switch Connection to foreground call:" + this);
                this.mParent.detach(this);
                this.mParent = this.mOwner.mForegroundCall;
                this.mParent.attach(this);
            }
        } else if (state == State.HOLDING) {
            if (this.mParent == this.mOwner.mForegroundCall) {
                Rlog.d(LOG_TAG, "update() - Switch Connection to background call:" + this);
                this.mParent.detach(this);
                this.mParent = this.mOwner.mBackgroundCall;
                this.mParent.attach(this);
            }
            onStartedHolding();
        }
        boolean updateParent = this.mParent.update(this, imsCall, state);
        boolean updateWifiState = updateWifiState();
        boolean updateAddressDisplay = updateAddressDisplay(imsCall);
        boolean updateMediaCapabilities = updateMediaCapabilities(imsCall);
        boolean updateExtras = updateExtras(imsCall);
        if (updateParent || updateWifiState || updateAddressDisplay || updateMediaCapabilities) {
            updateExtras = true;
        }
        return updateExtras;
    }

    public int getPreciseDisconnectCause() {
        return 0;
    }

    public void onDisconnectConferenceParticipant(Uri endpoint) {
        if (this.mOwner != null) {
            this.mOwner.logDebugMessagesWithOpFormat("CC", "RemoveMember", this, " remove: " + endpoint);
        }
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
        if (callProfile != null) {
            String address = callProfile.getCallExtra("oi");
            String name = callProfile.getCallExtra("cna");
            int nump = ImsCallProfile.OIRToPresentation(callProfile.getCallExtraInt("oir"));
            if (!this.mIsIncoming) {
                nump = 1;
            }
            int namep = ImsCallProfile.OIRToPresentation(callProfile.getCallExtraInt("cnap"));
            Rlog.d(LOG_TAG, "address = " + Rlog.pii(LOG_TAG, address) + " name = " + name + " nump = " + nump + " namep = " + namep + " mAddr = " + this.mAddress);
            if (!equalsHandlesNulls(this.mAddress, address)) {
                Rlog.d(LOG_TAG, "update address = " + address + " isMpty = " + isMultiparty());
                if (!TextUtils.isEmpty(address)) {
                    this.mAddress = address;
                    changed = true;
                }
            }
            if (TextUtils.isEmpty(name)) {
                if (!TextUtils.isEmpty(this.mCnapName)) {
                    this.mCnapName = UsimPBMemInfo.STRING_NOT_SET;
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
        if (changed) {
            setConnectionAddressDisplay();
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
                    if (VideoProfile.isPaused(oldVideoState) && !VideoProfile.isPaused(newVideoState)) {
                        this.mShouldIgnoreVideoStateChanges = false;
                    }
                    if (this.mShouldIgnoreVideoStateChanges) {
                        Rlog.d(LOG_TAG, "updateMediaCapabilities - ignoring video state change due to paused state.");
                    } else {
                        setVideoState(newVideoState);
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
            int newAudioQuality = getAudioQualityFromCallProfile(localCallProfile, remoteCallProfile);
            if (getAudioQuality() != newAudioQuality) {
                setAudioQuality(newAudioQuality);
                changed = true;
            }
        } catch (ImsException e) {
        }
        return changed;
    }

    public boolean updateWifiState() {
        if (this.mIsWifiStateFromExtras) {
            return false;
        }
        Rlog.d(LOG_TAG, "updateWifiState: " + this.mOwner.isVowifiEnabled());
        if (isWifi() == this.mOwner.isVowifiEnabled()) {
            return false;
        }
        setWifi(this.mOwner.isVowifiEnabled());
        return true;
    }

    private void updateWifiStateFromExtras(Bundle extras) {
        if (extras.containsKey("CallRadioTech") || extras.containsKey("callRadioTech")) {
            this.mIsWifiStateFromExtras = true;
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
        boolean changed = false;
        Bundle extras = null;
        if (imsCall == null) {
            return false;
        }
        ImsCallProfile callProfile = imsCall.getCallProfile();
        if (callProfile != null) {
            extras = callProfile.mCallExtras;
        }
        if (extras == null) {
            Rlog.d(LOG_TAG, "Call profile extras are null.");
        }
        if (!areBundlesEqual(extras, this.mExtras)) {
            changed = true;
        }
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
        boolean isHighDef = false;
        if (localCallProfile == null || remoteCallProfile == null || localCallProfile.mMediaProfile == null) {
            return 1;
        }
        boolean isEvsCodecHighDef = (localCallProfile.mMediaProfile.mAudioQuality == 18 || localCallProfile.mMediaProfile.mAudioQuality == 19) ? true : localCallProfile.mMediaProfile.mAudioQuality == 20;
        if ((localCallProfile.mMediaProfile.mAudioQuality == 2 || localCallProfile.mMediaProfile.mAudioQuality == 6 || isEvsCodecHighDef) && remoteCallProfile.mRestrictCause == 0) {
            isHighDef = true;
        }
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
        if (this.mImsCall == null) {
            sb.append("null");
        } else {
            sb.append(this.mImsCall);
        }
        sb.append("]");
        sb.append(" state:").append(getState());
        sb.append(" mParent:");
        sb.append(getParentCallName());
        return sb.toString();
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

    public int getCallId() {
        ImsCall call = getImsCall();
        if (call == null || call.getCallSession() == null) {
            return -1;
        }
        String callId = call.getCallSession().getCallId();
        if (callId != null) {
            return Integer.parseInt(callId);
        }
        Rlog.d(LOG_TAG, "Abnormal! Call Id = null");
        return -1;
    }

    int getCallIdBeforeDisconnected() {
        return this.mCallIdBeforeDisconnected;
    }

    ArrayList<String> getConfDialStrings() {
        return this.mConfDialStrings;
    }

    public String getConferenceParticipantAddress(int index) {
        String emptyAddress = UsimPBMemInfo.STRING_NOT_SET;
        if (this.mConferenceParticipants == null) {
            Rlog.d(LOG_TAG, "getConferenceParticipantAddress(): no XML information");
            return emptyAddress;
        } else if (index < 0 || index + 1 >= this.mConferenceParticipants.size()) {
            Rlog.d(LOG_TAG, "getConferenceParticipantAddress(): invalid index");
            return emptyAddress;
        } else {
            ConferenceParticipant participant = (ConferenceParticipant) this.mConferenceParticipants.get(index + 1);
            if (participant == null) {
                Rlog.d(LOG_TAG, "getConferenceParticipantAddress(): empty participant info");
                return emptyAddress;
            }
            Uri userEntity = participant.getHandle();
            Rlog.d(LOG_TAG, "getConferenceParticipantAddress(): ret=" + userEntity);
            return userEntity.toString();
        }
    }

    String getParentCallName() {
        if (this.mOwner == null) {
            return "Unknown";
        }
        if (this.mParent == this.mOwner.mForegroundCall) {
            return "Foreground Call";
        }
        if (this.mParent == this.mOwner.mBackgroundCall) {
            return "Background Call";
        }
        if (this.mParent == this.mOwner.mRingingCall) {
            return "Ringing Call";
        }
        if (this.mParent == this.mOwner.mHandoverCall) {
            return "Handover Call";
        }
        return "Abnormal";
    }

    public boolean isIncomingCallMultiparty() {
        return this.mImsCall != null ? this.mImsCall.isIncomingCallMultiparty() : false;
    }

    public void inviteConferenceParticipants(List<String> numbers) {
        StringBuilder sb = new StringBuilder();
        for (String number : numbers) {
            sb.append(number);
            sb.append(", ");
        }
        if (this.mOwner != null) {
            this.mOwner.logDebugMessagesWithOpFormat("CC", "AddMember", this, " invite with " + sb.toString());
        }
        ImsCall imsCall = getImsCall();
        if (imsCall != null) {
            ArrayList<String> list = new ArrayList();
            list.addAll(numbers);
            String[] participants = (String[]) list.toArray(new String[list.size()]);
            try {
                imsCall.inviteParticipants(participants);
            } catch (ImsException e) {
                Rlog.e(LOG_TAG, "inviteConferenceParticipants: no call session and fail to invite participants " + participants);
            }
        }
    }

    void setConfDialStrings(ArrayList<String> dialStrings) {
        this.mConfDialStrings = dialStrings;
    }

    void setConferenceAsHost() {
        Rlog.d(LOG_TAG, "set is conference host connection: " + this);
        this.mIsIncoming = false;
    }

    void setVendorDisconnectCause(String cause) {
        this.mVendorCause = cause;
    }

    public void updateConferenceParticipants(List<ConferenceParticipant> conferenceParticipants) {
        this.mConferenceParticipants = conferenceParticipants;
        super.updateConferenceParticipants(conferenceParticipants);
    }

    public void unhold() throws CallStateException {
        if (this.mOwner != null) {
            this.mOwner.unhold(this);
        }
    }
}

package com.android.internal.telephony;

import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.telecom.ConferenceParticipant;
import android.telecom.Connection.VideoProvider;
import android.telephony.Rlog;
import com.android.internal.telephony.Call.State;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
public abstract class Connection {
    public static final int AUDIO_QUALITY_HIGH_DEFINITION = 2;
    public static final int AUDIO_QUALITY_STANDARD = 1;
    private static String LOG_TAG;
    protected String mAddress;
    private boolean mAllowAddCallDuringVideoCall;
    private boolean mAnsweringDisconnectsActiveCall;
    private int mAudioQuality;
    private int mCallSubstate;
    protected int mCause;
    protected String mCnapName;
    protected int mCnapNamePresentation;
    protected long mConnectTime;
    protected long mConnectTimeReal;
    private int mConnectionCapabilities;
    protected String mConvertedNumber;
    protected long mCreateTime;
    protected String mDialString;
    protected long mDuration;
    private Bundle mExtras;
    String mForwardingAddress;
    protected long mHoldingStartTime;
    protected boolean mIsIncoming;
    private boolean mIsPulledCall;
    private boolean mIsWifi;
    public Set<Listener> mListeners;
    protected int mNextPostDialChar;
    protected boolean mNumberConverted;
    protected int mNumberPresentation;
    protected Connection mOrigConnection;
    private int mPhoneType;
    private List<PostDialListener> mPostDialListeners;
    protected PostDialState mPostDialState;
    protected String mPostDialString;
    public State mPreHandoverState;
    public boolean mPreMultipartyHostState;
    public boolean mPreMultipartyState;
    private int mPulledDialogId;
    String mRedirectingAddress;
    private String mTelecomCallId;
    Object mUserData;
    private VideoProvider mVideoProvider;
    private int mVideoState;

    public static class Capability {
        public static final int IS_EXTERNAL_CONNECTION = 16;
        public static final int IS_PULLABLE = 32;
        public static final int SUPPORTS_DOWNGRADE_TO_VOICE_LOCAL = 1;
        public static final int SUPPORTS_DOWNGRADE_TO_VOICE_REMOTE = 2;
        public static final int SUPPORTS_VT_LOCAL_BIDIRECTIONAL = 4;
        public static final int SUPPORTS_VT_REMOTE_BIDIRECTIONAL = 8;
        public static final int SUPPORTS_VT_RINGTONE = 64;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.Connection.Capability.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        public Capability() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.Connection.Capability.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Connection.Capability.<init>():void");
        }
    }

    public interface Listener {
        void onAddressDisplayChanged();

        void onAudioQualityChanged(int i);

        void onCallPullFailed(Connection connection);

        void onCallSubstateChanged(int i);

        void onConferenceConnectionsConfigured(ArrayList<Connection> arrayList);

        void onConferenceMergedFailed();

        void onConferenceParticipantsChanged(List<ConferenceParticipant> list);

        void onConferenceParticipantsInvited(boolean z);

        void onConnectionCapabilitiesChanged(int i);

        void onConnectionEvent(String str, Bundle bundle);

        void onExitedEcmMode();

        void onExtrasChanged(Bundle bundle);

        void onHandoverToWifiFailed();

        void onMultipartyStateChanged(boolean z);

        void onRemoteHeld(boolean z);

        void onVideoProviderChanged(VideoProvider videoProvider);

        void onVideoStateChanged(int i);

        void onWifiChanged(boolean z);
    }

    public static abstract class ListenerBase implements Listener {
        public void onVideoStateChanged(int videoState) {
        }

        public void onConnectionCapabilitiesChanged(int capability) {
        }

        public void onWifiChanged(boolean isWifi) {
        }

        public void onVideoProviderChanged(VideoProvider videoProvider) {
        }

        public void onAudioQualityChanged(int audioQuality) {
        }

        public void onConferenceParticipantsChanged(List<ConferenceParticipant> list) {
        }

        public void onCallSubstateChanged(int callSubstate) {
        }

        public void onMultipartyStateChanged(boolean isMultiParty) {
        }

        public void onConferenceMergedFailed() {
        }

        public void onExtrasChanged(Bundle extras) {
        }

        public void onExitedEcmMode() {
        }

        public void onCallPullFailed(Connection externalConnection) {
        }

        public void onHandoverToWifiFailed() {
        }

        public void onConnectionEvent(String event, Bundle extras) {
        }

        public void onConferenceParticipantsInvited(boolean isSuccess) {
        }

        public void onConferenceConnectionsConfigured(ArrayList<Connection> arrayList) {
        }

        public void onRemoteHeld(boolean isHeld) {
        }

        public void onAddressDisplayChanged() {
        }
    }

    public interface PostDialListener {
        void onPostDialChar(char c);

        void onPostDialWait();
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum PostDialState {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.Connection.PostDialState.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.Connection.PostDialState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Connection.PostDialState.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.Connection.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.Connection.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Connection.<clinit>():void");
    }

    public abstract void cancelPostDial();

    public abstract Call getCall();

    public abstract long getDisconnectTime();

    public abstract long getHoldDurationMillis();

    public abstract int getNumberPresentation();

    public abstract int getPreciseDisconnectCause();

    public abstract UUSInfo getUUSInfo();

    public abstract String getVendorDisconnectCause();

    public abstract void hangup() throws CallStateException;

    public abstract boolean isMultiparty();

    public abstract void proceedAfterWaitChar();

    public abstract void proceedAfterWildChar(String str);

    public abstract void separate() throws CallStateException;

    protected Connection(int phoneType) {
        this.mCnapNamePresentation = 1;
        this.mNumberPresentation = 1;
        this.mPostDialListeners = new ArrayList();
        this.mListeners = new CopyOnWriteArraySet();
        this.mNumberConverted = false;
        this.mCause = 0;
        this.mPostDialState = PostDialState.NOT_STARTED;
        this.mPreHandoverState = State.IDLE;
        this.mIsPulledCall = false;
        this.mPreMultipartyState = false;
        this.mPreMultipartyHostState = false;
        this.mPhoneType = phoneType;
    }

    public String getTelecomCallId() {
        return this.mTelecomCallId;
    }

    public void setTelecomCallId(String telecomCallId) {
        this.mTelecomCallId = telecomCallId;
    }

    public String getAddress() {
        return this.mAddress;
    }

    public String getCnapName() {
        return this.mCnapName;
    }

    public String getOrigDialString() {
        return null;
    }

    public int getCnapNamePresentation() {
        return this.mCnapNamePresentation;
    }

    public long getCreateTime() {
        return this.mCreateTime;
    }

    public long getConnectTime() {
        return this.mConnectTime;
    }

    public void setConnectTime(long connectTime) {
        this.mConnectTime = connectTime;
    }

    public long getConnectTimeReal() {
        return this.mConnectTimeReal;
    }

    public long getDurationMillis() {
        if (this.mConnectTimeReal == 0) {
            return 0;
        }
        if (this.mDuration == 0) {
            return SystemClock.elapsedRealtime() - this.mConnectTimeReal;
        }
        return this.mDuration;
    }

    public long getHoldingStartTime() {
        return this.mHoldingStartTime;
    }

    public int getDisconnectCause() {
        return this.mCause;
    }

    public boolean isIncoming() {
        return this.mIsIncoming;
    }

    public State getState() {
        Call c = getCall();
        if (c == null) {
            return State.IDLE;
        }
        return c.getState();
    }

    public State getStateBeforeHandover() {
        return this.mPreHandoverState;
    }

    public List<ConferenceParticipant> getConferenceParticipants() {
        Call c = getCall();
        if (c == null) {
            return null;
        }
        return c.getConferenceParticipants();
    }

    public boolean isAlive() {
        return getState().isAlive();
    }

    public boolean isRinging() {
        return getState().isRinging();
    }

    public Object getUserData() {
        return this.mUserData;
    }

    public void setUserData(Object userdata) {
        this.mUserData = userdata;
    }

    public void clearUserData() {
        this.mUserData = null;
    }

    public final void addPostDialListener(PostDialListener listener) {
        if (!this.mPostDialListeners.contains(listener)) {
            this.mPostDialListeners.add(listener);
        }
    }

    public final void removePostDialListener(PostDialListener listener) {
        this.mPostDialListeners.remove(listener);
    }

    protected final void clearPostDialListeners() {
        this.mPostDialListeners.clear();
    }

    protected final void notifyPostDialListeners() {
        if (getPostDialState() == PostDialState.WAIT) {
            for (PostDialListener listener : new ArrayList(this.mPostDialListeners)) {
                listener.onPostDialWait();
            }
        }
    }

    protected final void notifyPostDialListenersNextChar(char c) {
        for (PostDialListener listener : new ArrayList(this.mPostDialListeners)) {
            listener.onPostDialChar(c);
        }
    }

    public PostDialState getPostDialState() {
        return this.mPostDialState;
    }

    public String getRemainingPostDialString() {
        if (this.mPostDialState == PostDialState.CANCELLED || this.mPostDialState == PostDialState.COMPLETE || this.mPostDialString == null || this.mPostDialString.length() <= this.mNextPostDialChar) {
            return UsimPBMemInfo.STRING_NOT_SET;
        }
        return this.mPostDialString.substring(this.mNextPostDialChar);
    }

    public boolean onDisconnect(int cause) {
        return false;
    }

    public Connection getOrigConnection() {
        return this.mOrigConnection;
    }

    public boolean isConferenceHost() {
        return false;
    }

    public boolean isMemberOfPeerConference() {
        return false;
    }

    public void migrateFrom(Connection c) {
        if (c != null) {
            this.mListeners = c.mListeners;
            this.mDialString = c.getOrigDialString();
            this.mCreateTime = c.getCreateTime();
            this.mConnectTime = c.getConnectTime();
            this.mConnectTimeReal = c.getConnectTimeReal();
            this.mHoldingStartTime = c.getHoldingStartTime();
            this.mOrigConnection = c.getOrigConnection();
            this.mPostDialString = c.mPostDialString;
            this.mNextPostDialChar = c.mNextPostDialChar;
        }
    }

    public final void addListener(Listener listener) {
        this.mListeners.add(listener);
    }

    public final void removeListener(Listener listener) {
        this.mListeners.remove(listener);
    }

    public int getVideoState() {
        return this.mVideoState;
    }

    public int getConnectionCapabilities() {
        return this.mConnectionCapabilities;
    }

    public boolean hasCapabilities(int connectionCapabilities) {
        return (this.mConnectionCapabilities & connectionCapabilities) == connectionCapabilities;
    }

    public static int addCapability(int capabilities, int capability) {
        return capabilities | capability;
    }

    public static int removeCapability(int capabilities, int capability) {
        return (~capability) & capabilities;
    }

    public boolean isWifi() {
        return this.mIsWifi;
    }

    public VideoProvider getVideoProvider() {
        return this.mVideoProvider;
    }

    public int getAudioQuality() {
        return this.mAudioQuality;
    }

    public int getCallSubstate() {
        return this.mCallSubstate;
    }

    public void setVideoState(int videoState) {
        this.mVideoState = videoState;
        for (Listener l : this.mListeners) {
            l.onVideoStateChanged(this.mVideoState);
        }
    }

    public void setConnectionCapabilities(int capabilities) {
        if (this.mConnectionCapabilities != capabilities) {
            this.mConnectionCapabilities = capabilities;
            for (Listener l : this.mListeners) {
                l.onConnectionCapabilitiesChanged(this.mConnectionCapabilities);
            }
        }
    }

    public void setWifi(boolean isWifi) {
        this.mIsWifi = isWifi;
        for (Listener l : this.mListeners) {
            l.onWifiChanged(this.mIsWifi);
        }
    }

    public void setAudioQuality(int audioQuality) {
        this.mAudioQuality = audioQuality;
        for (Listener l : this.mListeners) {
            l.onAudioQualityChanged(this.mAudioQuality);
        }
    }

    public void setConnectionExtras(Bundle extras) {
        if (extras != null) {
            this.mExtras = new Bundle(extras);
        } else {
            this.mExtras = null;
        }
        for (Listener l : this.mListeners) {
            l.onExtrasChanged(this.mExtras);
        }
    }

    public Bundle getConnectionExtras() {
        return this.mExtras == null ? null : new Bundle(this.mExtras);
    }

    public boolean isActiveCallDisconnectedOnAnswer() {
        return this.mAnsweringDisconnectsActiveCall;
    }

    public void setActiveCallDisconnectedOnAnswer(boolean answeringDisconnectsActiveCall) {
        this.mAnsweringDisconnectsActiveCall = answeringDisconnectsActiveCall;
    }

    public boolean shouldAllowAddCallDuringVideoCall() {
        return this.mAllowAddCallDuringVideoCall;
    }

    public void setAllowAddCallDuringVideoCall(boolean allowAddCallDuringVideoCall) {
        this.mAllowAddCallDuringVideoCall = allowAddCallDuringVideoCall;
    }

    public void setIsPulledCall(boolean isPulledCall) {
        this.mIsPulledCall = isPulledCall;
    }

    public boolean isPulledCall() {
        return this.mIsPulledCall;
    }

    public void setPulledDialogId(int pulledDialogId) {
        this.mPulledDialogId = pulledDialogId;
    }

    public int getPulledDialogId() {
        return this.mPulledDialogId;
    }

    public void setCallSubstate(int callSubstate) {
        this.mCallSubstate = callSubstate;
        for (Listener l : this.mListeners) {
            l.onCallSubstateChanged(this.mCallSubstate);
        }
    }

    public void setVideoProvider(VideoProvider videoProvider) {
        this.mVideoProvider = videoProvider;
        for (Listener l : this.mListeners) {
            l.onVideoProviderChanged(this.mVideoProvider);
        }
    }

    public void setConverted(String oriNumber) {
        this.mNumberConverted = true;
        this.mConvertedNumber = this.mAddress;
        this.mAddress = oriNumber;
        this.mDialString = oriNumber;
    }

    public void updateConferenceParticipants(List<ConferenceParticipant> conferenceParticipants) {
        for (Listener l : this.mListeners) {
            l.onConferenceParticipantsChanged(conferenceParticipants);
        }
    }

    public void updateMultipartyState(boolean isMultiparty) {
        for (Listener l : this.mListeners) {
            l.onMultipartyStateChanged(isMultiparty);
        }
    }

    public void onConferenceMergeFailed() {
        for (Listener l : this.mListeners) {
            l.onConferenceMergedFailed();
        }
    }

    public void onExitedEcmMode() {
        for (Listener l : this.mListeners) {
            l.onExitedEcmMode();
        }
    }

    public void onCallPullFailed(Connection externalConnection) {
        for (Listener l : this.mListeners) {
            l.onCallPullFailed(externalConnection);
        }
    }

    public void onHandoverToWifiFailed() {
        for (Listener l : this.mListeners) {
            l.onHandoverToWifiFailed();
        }
    }

    public void onConnectionEvent(String event, Bundle extras) {
        for (Listener l : this.mListeners) {
            l.onConnectionEvent(event, extras);
        }
    }

    public void onDisconnectConferenceParticipant(Uri endpoint) {
    }

    public void pullExternalCall() {
    }

    public int getPhoneType() {
        return this.mPhoneType;
    }

    public String toString() {
        StringBuilder str = new StringBuilder(128);
        str.append(" callId: ").append(getTelecomCallId());
        str.append(" isExternal: ").append((this.mConnectionCapabilities & 16) == 16 ? "Y" : "N");
        if (Rlog.isLoggable(LOG_TAG, 3)) {
            str.append("addr: ").append(getAddress()).append(" pres.: ").append(getNumberPresentation()).append(" dial: ").append(getOrigDialString()).append(" postdial: ").append(getRemainingPostDialString()).append(" cnap name: ").append(getCnapName()).append("(").append(getCnapNamePresentation()).append(")");
        }
        str.append(" incoming: ").append(isIncoming()).append(" state: ").append(getState()).append(" post dial state: ").append(getPostDialState());
        return str.toString();
    }

    public boolean isConfHostBeforeHandover() {
        return this.mPreMultipartyHostState;
    }

    public boolean isMultipartyBeforeHandover() {
        return this.mPreMultipartyState;
    }

    public boolean isIncomingCallMultiparty() {
        return false;
    }

    public void notifyConferenceParticipantsInvited(boolean isSuccess) {
        for (Listener l : this.mListeners) {
            l.onConferenceParticipantsInvited(isSuccess);
        }
    }

    public void notifyConferenceConnectionsConfigured(ArrayList<Connection> radioConnections) {
        for (Listener l : this.mListeners) {
            l.onConferenceConnectionsConfigured(radioConnections);
        }
    }

    public void notifyRemoteHeld(boolean isHeld) {
        Rlog.d(LOG_TAG, "Connection: notify remote hold");
        for (Listener l : this.mListeners) {
            l.onRemoteHeld(isHeld);
        }
    }

    public void setConnectionAddressDisplay() {
        for (Listener l : this.mListeners) {
            l.onAddressDisplayChanged();
        }
    }

    public boolean isVideo() {
        Rlog.d(LOG_TAG, "Connection: isVideo = false");
        return false;
    }

    public String getForwardingAddress() {
        return this.mForwardingAddress;
    }

    public void setForwardingAddress(String address) {
        this.mForwardingAddress = address;
    }

    public String getRedirectingAddress() {
        return this.mRedirectingAddress;
    }

    public void setRedirectingAddress(String address) {
        this.mRedirectingAddress = address;
    }

    public void setNumberPresentation(int num) {
        this.mNumberPresentation = num;
    }

    public long oppoGetConnectTimeReal() {
        return this.mConnectTimeReal;
    }

    public void setStatusForOutgoingCall(boolean isConnected) {
    }

    public void fakeHoldCall() {
    }
}

package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.telecom.Call.Details;
import android.telecom.Connection.VideoProvider;
import android.util.ArraySet;
import com.mediatek.telecom.FormattedLog;
import com.mediatek.telecom.FormattedLog.Builder;
import com.mediatek.telecom.FormattedLog.OpType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class Conference extends Conferenceable {
    public static final long CONNECT_TIME_NOT_SPECIFIED = 0;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    private static final boolean SENLOG = false;
    private static final boolean TELDBG = false;
    private CallAudioState mCallAudioState;
    private final List<Connection> mChildConnections;
    private final List<Connection> mConferenceableConnections;
    private long mConnectTimeMillis;
    private int mConnectionCapabilities;
    private final android.telecom.Connection.Listener mConnectionDeathListener;
    private int mConnectionProperties;
    private DisconnectCause mDisconnectCause;
    private String mDisconnectMessage;
    private Bundle mExtras;
    private final Object mExtrasLock;
    private final Set<Listener> mListeners;
    private PhoneAccountHandle mPhoneAccount;
    private Set<String> mPreviousExtraKeys;
    private int mState;
    private StatusHints mStatusHints;
    private String mTelecomCallId;
    private final List<Connection> mUnmodifiableChildConnections;
    private final List<Connection> mUnmodifiableConferenceableConnections;
    private VideoProvider mVideoProvider;
    private int mVideoState;

    /* renamed from: android.telecom.Conference$1 */
    class AnonymousClass1 extends android.telecom.Connection.Listener {
        final /* synthetic */ Conference this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telecom.Conference.1.<init>(android.telecom.Conference):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass1(android.telecom.Conference r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telecom.Conference.1.<init>(android.telecom.Conference):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.Conference.1.<init>(android.telecom.Conference):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Conference.1.onDestroyed(android.telecom.Connection):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void onDestroyed(android.telecom.Connection r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Conference.1.onDestroyed(android.telecom.Connection):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.Conference.1.onDestroyed(android.telecom.Connection):void");
        }
    }

    public static abstract class Listener {
        public Listener() {
        }

        public void onStateChanged(Conference conference, int oldState, int newState) {
        }

        public void onDisconnected(Conference conference, DisconnectCause disconnectCause) {
        }

        public void onConnectionAdded(Conference conference, Connection connection) {
        }

        public void onConnectionRemoved(Conference conference, Connection connection) {
        }

        public void onConferenceableConnectionsChanged(Conference conference, List<Connection> list) {
        }

        public void onDestroyed(Conference conference) {
        }

        public void onConnectionCapabilitiesChanged(Conference conference, int connectionCapabilities) {
        }

        public void onConnectionPropertiesChanged(Conference conference, int connectionProperties) {
        }

        public void onVideoStateChanged(Conference c, int videoState) {
        }

        public void onVideoProviderChanged(Conference c, VideoProvider videoProvider) {
        }

        public void onStatusHintsChanged(Conference conference, StatusHints statusHints) {
        }

        public void onExtrasChanged(Conference c, Bundle extras) {
        }

        public void onExtrasRemoved(Conference c, List<String> list) {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telecom.Conference.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telecom.Conference.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.Conference.<clinit>():void");
    }

    public Uri getHostAddress() {
        return null;
    }

    public Conference(PhoneAccountHandle phoneAccount) {
        this.mListeners = new CopyOnWriteArraySet();
        this.mChildConnections = new CopyOnWriteArrayList();
        this.mUnmodifiableChildConnections = Collections.unmodifiableList(this.mChildConnections);
        this.mConferenceableConnections = new ArrayList();
        this.mUnmodifiableConferenceableConnections = Collections.unmodifiableList(this.mConferenceableConnections);
        this.mState = 1;
        this.mConnectTimeMillis = 0;
        this.mExtrasLock = new Object();
        this.mConnectionDeathListener = new AnonymousClass1(this);
        this.mPhoneAccount = phoneAccount;
    }

    public final String getTelecomCallId() {
        return this.mTelecomCallId;
    }

    public final void setTelecomCallId(String telecomCallId) {
        this.mTelecomCallId = telecomCallId;
    }

    public final PhoneAccountHandle getPhoneAccountHandle() {
        return this.mPhoneAccount;
    }

    public final List<Connection> getConnections() {
        return this.mUnmodifiableChildConnections;
    }

    public final int getState() {
        return this.mState;
    }

    public final int getConnectionCapabilities() {
        return this.mConnectionCapabilities;
    }

    public final int getConnectionProperties() {
        return this.mConnectionProperties;
    }

    public static boolean can(int capabilities, int capability) {
        return (capabilities & capability) != 0;
    }

    public boolean can(int capability) {
        return can(this.mConnectionCapabilities, capability);
    }

    public void removeCapability(int capability) {
        setConnectionCapabilities(this.mConnectionCapabilities & (~capability));
    }

    public void addCapability(int capability) {
        setConnectionCapabilities(this.mConnectionCapabilities | capability);
    }

    @Deprecated
    public final AudioState getAudioState() {
        return new AudioState(this.mCallAudioState);
    }

    public final CallAudioState getCallAudioState() {
        return this.mCallAudioState;
    }

    public VideoProvider getVideoProvider() {
        return this.mVideoProvider;
    }

    public int getVideoState() {
        return this.mVideoState;
    }

    public void onDisconnect() {
    }

    public void onSeparate(Connection connection) {
    }

    public void onMerge(Connection connection) {
    }

    public void onHold() {
    }

    public void onUnhold() {
    }

    public void onMerge() {
    }

    public void onSwap() {
    }

    public void onPlayDtmfTone(char c) {
    }

    public void onStopDtmfTone() {
    }

    @Deprecated
    public void onAudioStateChanged(AudioState state) {
    }

    public void onCallAudioStateChanged(CallAudioState state) {
    }

    public void onConnectionAdded(Connection connection) {
    }

    public final void setOnHold() {
        setState(5);
    }

    public void onHangupAll() {
    }

    public final void setDialing() {
        setState(3);
    }

    public final void setActive() {
        setState(4);
    }

    public final void setDisconnected(DisconnectCause disconnectCause) {
        this.mDisconnectCause = disconnectCause;
        setState(6);
        for (Listener l : this.mListeners) {
            l.onDisconnected(this, this.mDisconnectCause);
        }
    }

    protected int buildConnectionCapabilities() {
        return 0;
    }

    public final void updateConnectionCapabilities() {
        setConnectionCapabilities(buildConnectionCapabilities());
    }

    public final DisconnectCause getDisconnectCause() {
        return this.mDisconnectCause;
    }

    public final void setConnectionCapabilities(int connectionCapabilities) {
        if (connectionCapabilities != this.mConnectionCapabilities) {
            this.mConnectionCapabilities = connectionCapabilities;
            for (Listener l : this.mListeners) {
                l.onConnectionCapabilitiesChanged(this, this.mConnectionCapabilities);
            }
        }
    }

    public final void setConnectionProperties(int connectionProperties) {
        if (connectionProperties != this.mConnectionProperties) {
            this.mConnectionProperties = connectionProperties;
            for (Listener l : this.mListeners) {
                l.onConnectionPropertiesChanged(this, this.mConnectionProperties);
            }
        }
    }

    public final boolean addConnection(Connection connection) {
        Object[] objArr = new Object[1];
        objArr[0] = connection;
        Log.d((Object) this, "Connection=%s, connection=", objArr);
        if (connection == null || this.mChildConnections.contains(connection) || !connection.setConference(this)) {
            return false;
        }
        this.mChildConnections.add(connection);
        onConnectionAdded(connection);
        for (Listener l : this.mListeners) {
            l.onConnectionAdded(this, connection);
        }
        return true;
    }

    public final void removeConnection(Connection connection) {
        Object[] objArr = new Object[2];
        objArr[0] = connection;
        objArr[1] = this.mChildConnections;
        Log.d((Object) this, "removing %s from %s", objArr);
        if (connection != null && this.mChildConnections.remove(connection)) {
            connection.resetConference();
            for (Listener l : this.mListeners) {
                l.onConnectionRemoved(this, connection);
            }
        }
    }

    public final void setConferenceableConnections(List<Connection> conferenceableConnections) {
        clearConferenceableList();
        for (Connection c : conferenceableConnections) {
            if (!this.mConferenceableConnections.contains(c)) {
                c.addConnectionListener(this.mConnectionDeathListener);
                this.mConferenceableConnections.add(c);
            }
        }
        fireOnConferenceableConnectionsChanged();
    }

    public final void setVideoState(Connection c, int videoState) {
        Object[] objArr = new Object[3];
        objArr[0] = this;
        objArr[1] = c;
        objArr[2] = Integer.valueOf(videoState);
        Log.d((Object) this, "setVideoState Conference: %s Connection: %s VideoState: %s", objArr);
        this.mVideoState = videoState;
        for (Listener l : this.mListeners) {
            l.onVideoStateChanged(this, videoState);
        }
    }

    public final void setVideoProvider(Connection c, VideoProvider videoProvider) {
        Object[] objArr = new Object[3];
        objArr[0] = this;
        objArr[1] = c;
        objArr[2] = videoProvider;
        Log.d((Object) this, "setVideoProvider Conference: %s Connection: %s VideoState: %s", objArr);
        this.mVideoProvider = videoProvider;
        for (Listener l : this.mListeners) {
            l.onVideoProviderChanged(this, videoProvider);
        }
    }

    private final void fireOnConferenceableConnectionsChanged() {
        for (Listener l : this.mListeners) {
            l.onConferenceableConnectionsChanged(this, getConferenceableConnections());
        }
    }

    public final List<Connection> getConferenceableConnections() {
        return this.mUnmodifiableConferenceableConnections;
    }

    public final void destroy() {
        Object[] objArr = new Object[1];
        objArr[0] = this;
        Log.d((Object) this, "destroying conference : %s", objArr);
        List<Connection> disconnectedChild = new ArrayList(this.mChildConnections.size());
        List<Connection> activeChild = new ArrayList(this.mChildConnections.size());
        for (Connection connection : this.mChildConnections) {
            if (connection.getState() != 4) {
                disconnectedChild.add(connection);
            } else {
                activeChild.add(connection);
            }
        }
        for (Connection connection2 : disconnectedChild) {
            objArr = new Object[1];
            objArr[0] = connection2;
            Log.d((Object) this, "removing connection for disconnectedChild %s", objArr);
            removeConnection(connection2);
        }
        for (Connection connection22 : activeChild) {
            objArr = new Object[1];
            objArr[0] = connection22;
            Log.d((Object) this, "removing connection for activeChild %s", objArr);
            removeConnection(connection22);
        }
        if (this.mState != 6) {
            Log.d((Object) this, "setting to disconnected", new Object[0]);
            setDisconnected(new DisconnectCause(2));
        }
        for (Listener l : this.mListeners) {
            l.onDestroyed(this);
        }
    }

    public final Conference addListener(Listener listener) {
        this.mListeners.add(listener);
        return this;
    }

    public final Conference removeListener(Listener listener) {
        this.mListeners.remove(listener);
        return this;
    }

    public Connection getPrimaryConnection() {
        if (this.mUnmodifiableChildConnections == null || this.mUnmodifiableChildConnections.isEmpty()) {
            return null;
        }
        return (Connection) this.mUnmodifiableChildConnections.get(0);
    }

    @Deprecated
    public final void setConnectTimeMillis(long connectTimeMillis) {
        setConnectionTime(connectTimeMillis);
    }

    public final void setConnectionTime(long connectionTimeMillis) {
        this.mConnectTimeMillis = connectionTimeMillis;
    }

    @Deprecated
    public final long getConnectTimeMillis() {
        return getConnectionTime();
    }

    public final long getConnectionTime() {
        return this.mConnectTimeMillis;
    }

    final void setCallAudioState(CallAudioState state) {
        Object[] objArr = new Object[1];
        objArr[0] = state;
        Log.d((Object) this, "setCallAudioState %s", objArr);
        this.mCallAudioState = state;
        onAudioStateChanged(getAudioState());
        onCallAudioStateChanged(state);
    }

    private void setState(int newState) {
        if (newState == 4 || newState == 5 || newState == 6 || newState == 3) {
            if (this.mState != newState) {
                int oldState = this.mState;
                this.mState = newState;
                Builder builder = configDumpLogBuilder(new Builder());
                if (builder != null) {
                    FormattedLog formattedLog = builder.buildDumpInfo();
                    if (formattedLog != null && (!SENLOG || TELDBG)) {
                        Log.d((Object) this, formattedLog.toString(), new Object[0]);
                    }
                }
                for (Listener l : this.mListeners) {
                    l.onStateChanged(this, oldState, newState);
                }
            }
            return;
        }
        Object[] objArr = new Object[1];
        objArr[0] = Connection.stateToString(newState);
        Log.w((Object) this, "Unsupported state transition for Conference call.", objArr);
    }

    private final void clearConferenceableList() {
        for (Connection c : this.mConferenceableConnections) {
            c.removeConnectionListener(this.mConnectionDeathListener);
        }
        this.mConferenceableConnections.clear();
    }

    public String toString() {
        Object[] objArr = new Object[5];
        objArr[0] = Connection.stateToString(this.mState);
        objArr[1] = Details.capabilitiesToString(this.mConnectionCapabilities);
        objArr[2] = Integer.valueOf(getVideoState());
        objArr[3] = getVideoProvider();
        objArr[4] = super.toString();
        return String.format(Locale.US, "[State: %s,Capabilites: %s, VideoState: %s, VideoProvider: %s, ThisObject %s]", objArr);
    }

    public final void setStatusHints(StatusHints statusHints) {
        this.mStatusHints = statusHints;
        for (Listener l : this.mListeners) {
            l.onStatusHintsChanged(this, statusHints);
        }
    }

    public final StatusHints getStatusHints() {
        return this.mStatusHints;
    }

    public final void setExtras(Bundle extras) {
        synchronized (this.mExtrasLock) {
            putExtras(extras);
            if (this.mPreviousExtraKeys != null) {
                List toRemove = new ArrayList();
                for (String oldKey : this.mPreviousExtraKeys) {
                    if (extras == null || !extras.containsKey(oldKey)) {
                        toRemove.add(oldKey);
                    }
                }
                if (!toRemove.isEmpty()) {
                    removeExtras(toRemove);
                }
            }
            if (this.mPreviousExtraKeys == null) {
                this.mPreviousExtraKeys = new ArraySet();
            }
            this.mPreviousExtraKeys.clear();
            if (extras != null) {
                this.mPreviousExtraKeys.addAll(extras.keySet());
            }
        }
    }

    public final void putExtras(Bundle extras) {
        if (extras != null) {
            Bundle listenersBundle;
            synchronized (this.mExtrasLock) {
                if (this.mExtras == null) {
                    this.mExtras = new Bundle();
                }
                this.mExtras.putAll(extras);
                listenersBundle = new Bundle(this.mExtras);
            }
            for (Listener l : this.mListeners) {
                l.onExtrasChanged(this, new Bundle(listenersBundle));
            }
        }
    }

    public final void putExtra(String key, boolean value) {
        Bundle newExtras = new Bundle();
        newExtras.putBoolean(key, value);
        putExtras(newExtras);
    }

    public final void putExtra(String key, int value) {
        Bundle newExtras = new Bundle();
        newExtras.putInt(key, value);
        putExtras(newExtras);
    }

    public final void putExtra(String key, String value) {
        Bundle newExtras = new Bundle();
        newExtras.putString(key, value);
        putExtras(newExtras);
    }

    public final void removeExtras(List<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            synchronized (this.mExtrasLock) {
                if (this.mExtras != null) {
                    for (String key : keys) {
                        this.mExtras.remove(key);
                    }
                }
            }
            List<String> unmodifiableKeys = Collections.unmodifiableList(keys);
            for (Listener l : this.mListeners) {
                l.onExtrasRemoved(this, unmodifiableKeys);
            }
        }
    }

    public final void removeExtras(String... keys) {
        removeExtras(Arrays.asList(keys));
    }

    public final Bundle getExtras() {
        return this.mExtras;
    }

    public void onExtrasChanged(Bundle extras) {
    }

    final void handleExtrasChanged(Bundle extras) {
        Bundle bundle = null;
        synchronized (this.mExtrasLock) {
            this.mExtras = extras;
            if (this.mExtras != null) {
                bundle = new Bundle(this.mExtras);
            }
        }
        onExtrasChanged(bundle);
    }

    public void onInviteConferenceParticipants(List<String> list) {
    }

    protected final void setRinging() {
        setState(2);
    }

    protected Builder configDumpLogBuilder(Builder builder) {
        if (builder == null) {
            return null;
        }
        builder.setCategory("CC");
        builder.setOpType(OpType.DUMP);
        builder.setCallNumber("conferenceCall");
        builder.setCallId(Integer.toString(System.identityHashCode(this)));
        builder.setStatusInfo("isConfCall", "Yes");
        builder.setStatusInfo("state", Connection.callStateToFormattedDumpString(this.mState));
        builder.setStatusInfo("isConfChildCall", "No");
        builder.setStatusInfo("capabilities", Connection.capabilitiesToString(getConnectionCapabilities()));
        return builder;
    }

    public void onDisconnect(String pendingCallAction) {
        onDisconnect();
    }
}

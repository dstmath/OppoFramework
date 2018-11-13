package com.android.internal.telephony.imsphone;

import android.os.AsyncResult;
import android.os.Handler;
import android.telecom.VideoProfile;
import android.util.ArrayMap;
import android.util.Log;
import com.android.ims.ImsCallProfile;
import com.android.ims.ImsExternalCallState;
import com.android.ims.ImsExternalCallStateListener;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.imsphone.ImsExternalConnection.Listener;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker.PhoneStateListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public class ImsExternalCallTracker implements PhoneStateListener {
    private static final int EVENT_VIDEO_CAPABILITIES_CHANGED = 1;
    public static final String EXTRA_IMS_EXTERNAL_CALL_ID = "android.telephony.ImsExternalCallTracker.extra.EXTERNAL_CALL_ID";
    public static final String TAG = "ImsExternalCallTracker";
    private ImsPullCall mCallPuller;
    private final ImsCallNotify mCallStateNotifier;
    private Map<Integer, Boolean> mExternalCallPullableState;
    private final ExternalCallStateListener mExternalCallStateListener;
    private final ExternalConnectionListener mExternalConnectionListener;
    private Map<Integer, ImsExternalConnection> mExternalConnections;
    private final Handler mHandler;
    private boolean mHasActiveCalls;
    private boolean mIsVideoCapable;
    private final ImsPhone mPhone;

    /* renamed from: com.android.internal.telephony.imsphone.ImsExternalCallTracker$1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ ImsExternalCallTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.imsphone.ImsExternalCallTracker.1.<init>(com.android.internal.telephony.imsphone.ImsExternalCallTracker):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(com.android.internal.telephony.imsphone.ImsExternalCallTracker r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.imsphone.ImsExternalCallTracker.1.<init>(com.android.internal.telephony.imsphone.ImsExternalCallTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsExternalCallTracker.1.<init>(com.android.internal.telephony.imsphone.ImsExternalCallTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.imsphone.ImsExternalCallTracker.1.handleMessage(android.os.Message):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void handleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.imsphone.ImsExternalCallTracker.1.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsExternalCallTracker.1.handleMessage(android.os.Message):void");
        }
    }

    public interface ImsCallNotify {
        void notifyPreciseCallStateChanged();

        void notifyUnknownConnection(Connection connection);
    }

    /* renamed from: com.android.internal.telephony.imsphone.ImsExternalCallTracker$2 */
    class AnonymousClass2 implements ImsCallNotify {
        final /* synthetic */ ImsExternalCallTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.imsphone.ImsExternalCallTracker.2.<init>(com.android.internal.telephony.imsphone.ImsExternalCallTracker):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass2(com.android.internal.telephony.imsphone.ImsExternalCallTracker r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.imsphone.ImsExternalCallTracker.2.<init>(com.android.internal.telephony.imsphone.ImsExternalCallTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsExternalCallTracker.2.<init>(com.android.internal.telephony.imsphone.ImsExternalCallTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.imsphone.ImsExternalCallTracker.2.notifyPreciseCallStateChanged():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void notifyPreciseCallStateChanged() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.imsphone.ImsExternalCallTracker.2.notifyPreciseCallStateChanged():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsExternalCallTracker.2.notifyPreciseCallStateChanged():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.imsphone.ImsExternalCallTracker.2.notifyUnknownConnection(com.android.internal.telephony.Connection):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void notifyUnknownConnection(com.android.internal.telephony.Connection r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.imsphone.ImsExternalCallTracker.2.notifyUnknownConnection(com.android.internal.telephony.Connection):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.imsphone.ImsExternalCallTracker.2.notifyUnknownConnection(com.android.internal.telephony.Connection):void");
        }
    }

    public class ExternalCallStateListener extends ImsExternalCallStateListener {
        final /* synthetic */ ImsExternalCallTracker this$0;

        public ExternalCallStateListener(ImsExternalCallTracker this$0) {
            this.this$0 = this$0;
        }

        public void onImsExternalCallStateUpdate(List<ImsExternalCallState> externalCallState) {
            this.this$0.refreshExternalCallState(externalCallState);
        }
    }

    public class ExternalConnectionListener implements Listener {
        final /* synthetic */ ImsExternalCallTracker this$0;

        public ExternalConnectionListener(ImsExternalCallTracker this$0) {
            this.this$0 = this$0;
        }

        public void onPullExternalCall(ImsExternalConnection connection) {
            Log.d(ImsExternalCallTracker.TAG, "onPullExternalCall: connection = " + connection);
            if (this.this$0.mCallPuller == null) {
                Log.e(ImsExternalCallTracker.TAG, "onPullExternalCall : No call puller defined");
            } else {
                this.this$0.mCallPuller.pullExternalCall(connection.getAddress(), connection.getVideoState(), connection.getCallId());
            }
        }
    }

    public ImsExternalCallTracker(ImsPhone phone, ImsPullCall callPuller, ImsCallNotify callNotifier) {
        this.mExternalConnections = new ArrayMap();
        this.mExternalCallPullableState = new ArrayMap();
        this.mExternalConnectionListener = new ExternalConnectionListener(this);
        this.mHandler = new AnonymousClass1(this);
        this.mPhone = phone;
        this.mCallStateNotifier = callNotifier;
        this.mExternalCallStateListener = new ExternalCallStateListener(this);
        this.mCallPuller = callPuller;
    }

    public ImsExternalCallTracker(ImsPhone phone) {
        this.mExternalConnections = new ArrayMap();
        this.mExternalCallPullableState = new ArrayMap();
        this.mExternalConnectionListener = new ExternalConnectionListener(this);
        this.mHandler = new AnonymousClass1(this);
        this.mPhone = phone;
        this.mCallStateNotifier = new AnonymousClass2(this);
        this.mExternalCallStateListener = new ExternalCallStateListener(this);
        registerForNotifications();
    }

    public void tearDown() {
        unregisterForNotifications();
    }

    public void setCallPuller(ImsPullCall callPuller) {
        this.mCallPuller = callPuller;
    }

    public ExternalCallStateListener getExternalCallStateListener() {
        return this.mExternalCallStateListener;
    }

    public void onPhoneStateChanged(State oldState, State newState) {
        this.mHasActiveCalls = newState != State.IDLE;
        Log.i(TAG, "onPhoneStateChanged : hasActiveCalls = " + this.mHasActiveCalls);
        refreshCallPullState();
    }

    private void registerForNotifications() {
        if (this.mPhone != null) {
            Log.d(TAG, "Registering: " + this.mPhone);
            this.mPhone.getDefaultPhone().registerForVideoCapabilityChanged(this.mHandler, 1, null);
        }
    }

    private void unregisterForNotifications() {
        if (this.mPhone != null) {
            Log.d(TAG, "Unregistering: " + this.mPhone);
            this.mPhone.unregisterForVideoCapabilityChanged(this.mHandler);
        }
    }

    public void refreshExternalCallState(List<ImsExternalCallState> externalCallStates) {
        Log.d(TAG, "refreshExternalCallState");
        Iterator<Entry<Integer, ImsExternalConnection>> connectionIterator = this.mExternalConnections.entrySet().iterator();
        boolean wasCallRemoved = false;
        while (connectionIterator.hasNext()) {
            Entry<Integer, ImsExternalConnection> entry = (Entry) connectionIterator.next();
            if (!containsCallId(externalCallStates, ((Integer) entry.getKey()).intValue())) {
                ImsExternalConnection externalConnection = (ImsExternalConnection) entry.getValue();
                externalConnection.setTerminated();
                externalConnection.removeListener(this.mExternalConnectionListener);
                connectionIterator.remove();
                wasCallRemoved = true;
            }
        }
        if (wasCallRemoved) {
            this.mCallStateNotifier.notifyPreciseCallStateChanged();
        }
        if (externalCallStates != null && !externalCallStates.isEmpty()) {
            for (ImsExternalCallState callState : externalCallStates) {
                if (this.mExternalConnections.containsKey(Integer.valueOf(callState.getCallId()))) {
                    updateExistingConnection((ImsExternalConnection) this.mExternalConnections.get(Integer.valueOf(callState.getCallId())), callState);
                } else {
                    Log.d(TAG, "refreshExternalCallState: got = " + callState);
                    if (callState.getCallState() == 1) {
                        createExternalConnection(callState);
                    }
                }
            }
        }
    }

    public Connection getConnectionById(int callId) {
        return (Connection) this.mExternalConnections.get(Integer.valueOf(callId));
    }

    private void createExternalConnection(ImsExternalCallState state) {
        Log.i(TAG, "createExternalConnection : state = " + state);
        int videoState = ImsCallProfile.getVideoStateFromCallType(state.getCallType());
        boolean isCallPullPermitted = isCallPullPermitted(state.isCallPullable(), videoState);
        ImsExternalConnection connection = new ImsExternalConnection(this.mPhone, state.getCallId(), state.getAddress(), isCallPullPermitted);
        connection.setVideoState(videoState);
        connection.addListener(this.mExternalConnectionListener);
        Log.d(TAG, "createExternalConnection - pullable state : externalCallId = " + connection.getCallId() + " ; isPullable = " + isCallPullPermitted + " ; networkPullable = " + state.isCallPullable() + " ; isVideo = " + VideoProfile.isVideo(videoState) + " ; videoEnabled = " + this.mIsVideoCapable + " ; hasActiveCalls = " + this.mHasActiveCalls);
        this.mExternalConnections.put(Integer.valueOf(connection.getCallId()), connection);
        this.mExternalCallPullableState.put(Integer.valueOf(connection.getCallId()), Boolean.valueOf(state.isCallPullable()));
        this.mCallStateNotifier.notifyUnknownConnection(connection);
    }

    private void updateExistingConnection(ImsExternalConnection connection, ImsExternalCallState state) {
        Log.i(TAG, "updateExistingConnection : state = " + state);
        Call.State existingState = connection.getState();
        Call.State newState = state.getCallState() == 1 ? Call.State.ACTIVE : Call.State.DISCONNECTED;
        if (existingState != newState) {
            if (newState == Call.State.ACTIVE) {
                connection.setActive();
            } else {
                connection.setTerminated();
                connection.removeListener(this.mExternalConnectionListener);
                this.mExternalConnections.remove(Integer.valueOf(connection.getCallId()));
                this.mExternalCallPullableState.remove(Integer.valueOf(connection.getCallId()));
                this.mCallStateNotifier.notifyPreciseCallStateChanged();
            }
        }
        int newVideoState = ImsCallProfile.getVideoStateFromCallType(state.getCallType());
        if (newVideoState != connection.getVideoState()) {
            connection.setVideoState(newVideoState);
        }
        this.mExternalCallPullableState.put(Integer.valueOf(state.getCallId()), Boolean.valueOf(state.isCallPullable()));
        boolean isCallPullPermitted = isCallPullPermitted(state.isCallPullable(), newVideoState);
        Log.d(TAG, "updateExistingConnection - pullable state : externalCallId = " + connection.getCallId() + " ; isPullable = " + isCallPullPermitted + " ; networkPullable = " + state.isCallPullable() + " ; isVideo = " + VideoProfile.isVideo(connection.getVideoState()) + " ; videoEnabled = " + this.mIsVideoCapable + " ; hasActiveCalls = " + this.mHasActiveCalls);
        connection.setIsPullable(isCallPullPermitted);
    }

    private void refreshCallPullState() {
        Log.d(TAG, "refreshCallPullState");
        for (ImsExternalConnection imsExternalConnection : this.mExternalConnections.values()) {
            boolean isNetworkPullable = ((Boolean) this.mExternalCallPullableState.get(Integer.valueOf(imsExternalConnection.getCallId()))).booleanValue();
            boolean isCallPullPermitted = isCallPullPermitted(isNetworkPullable, imsExternalConnection.getVideoState());
            Log.d(TAG, "refreshCallPullState : externalCallId = " + imsExternalConnection.getCallId() + " ; isPullable = " + isCallPullPermitted + " ; networkPullable = " + isNetworkPullable + " ; isVideo = " + VideoProfile.isVideo(imsExternalConnection.getVideoState()) + " ; videoEnabled = " + this.mIsVideoCapable + " ; hasActiveCalls = " + this.mHasActiveCalls);
            imsExternalConnection.setIsPullable(isCallPullPermitted);
        }
    }

    private boolean containsCallId(List<ImsExternalCallState> externalCallStates, int callId) {
        if (externalCallStates == null) {
            return false;
        }
        for (ImsExternalCallState state : externalCallStates) {
            if (state.getCallId() == callId) {
                return true;
            }
        }
        return false;
    }

    private void handleVideoCapabilitiesChanged(AsyncResult ar) {
        this.mIsVideoCapable = ((Boolean) ar.result).booleanValue();
        Log.i(TAG, "handleVideoCapabilitiesChanged : isVideoCapable = " + this.mIsVideoCapable);
        refreshCallPullState();
    }

    private boolean isCallPullPermitted(boolean isNetworkPullable, int videoState) {
        if ((!VideoProfile.isVideo(videoState) || this.mIsVideoCapable) && !this.mHasActiveCalls) {
            return isNetworkPullable;
        }
        return false;
    }
}

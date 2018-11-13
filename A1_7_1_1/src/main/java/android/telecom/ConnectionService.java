package android.telecom;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telecom.Conference.Listener;
import android.telecom.Connection.VideoProvider;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.ims.ImsCallProfile;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IConnectionService;
import com.android.internal.telecom.IConnectionServiceAdapter;
import com.android.internal.telecom.IVideoProvider;
import com.android.internal.telecom.RemoteServiceCallback.Stub;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.telecom.FormattedLog;
import com.mediatek.telecom.FormattedLog.Builder;
import com.mediatek.telecom.FormattedLog.OpType;
import com.mediatek.telecom.TelecomManagerEx;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
public abstract class ConnectionService extends Service {
    private static final int MSG_ABORT = 3;
    private static final int MSG_ADD_CONNECTION_SERVICE_ADAPTER = 1;
    private static final int MSG_ANSWER = 4;
    private static final int MSG_ANSWER_VIDEO = 17;
    private static final int MSG_BLIND_ASSURED_ECT = 1005;
    private static final int MSG_CONFERENCE = 12;
    private static final int MSG_CREATE_CONFERENCE = 1003;
    private static final int MSG_CREATE_CONNECTION = 2;
    private static final int MSG_DISCONNECT = 6;
    private static final int MSG_ECT = 1000;
    private static final int MSG_HANDLE_ORDERED_USER_OPERATION = 1004;
    private static final int MSG_HANGUP_ALL = 1001;
    private static final int MSG_HOLD = 7;
    private static final int MSG_INVITE_CONFERENCE_PARTICIPANTS = 1002;
    private static final int MSG_MERGE_CONFERENCE = 18;
    private static final int MSG_ON_CALL_AUDIO_STATE_CHANGED = 9;
    private static final int MSG_ON_EXTRAS_CHANGED = 24;
    private static final int MSG_ON_POST_DIAL_CONTINUE = 14;
    private static final int MSG_PLAY_DTMF_TONE = 10;
    private static final int MSG_PULL_EXTERNAL_CALL = 22;
    private static final int MSG_REJECT = 5;
    private static final int MSG_REJECT_WITH_MESSAGE = 20;
    private static final int MSG_REMOVE_CONNECTION_SERVICE_ADAPTER = 16;
    private static final int MSG_SEND_CALL_EVENT = 23;
    private static final int MSG_SILENCE = 21;
    private static final int MSG_SPLIT_FROM_CONFERENCE = 13;
    private static final int MSG_STOP_DTMF_TONE = 11;
    private static final int MSG_SWAP_CONFERENCE = 19;
    private static final int MSG_UNHOLD = 8;
    private static final int MTK_MSG_BASE = 1000;
    private static final boolean PII_DEBUG = false;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    private static final boolean SDBG = false;
    private static final boolean SENLOG = false;
    public static final String SERVICE_INTERFACE = "android.telecom.ConnectionService";
    private static final boolean TELDBG = false;
    private static Connection sNullConnection;
    private final String LOG_TAG_FROM;
    private final ConnectionServiceAdapter mAdapter;
    private boolean mAreAccountsInitialized;
    private final IBinder mBinder;
    private final Map<String, Conference> mConferenceById;
    private final Listener mConferenceListener;
    private final Map<String, Connection> mConnectionById;
    private final Connection.Listener mConnectionListener;
    private final Handler mHandler;
    private int mId;
    private final Map<Conference, String> mIdByConference;
    private final Map<Connection, String> mIdByConnection;
    private Object mIdSyncRoot;
    private final List<Runnable> mPreInitializationConnectionRequests;
    private final RemoteConnectionManager mRemoteConnectionManager;
    private Conference sNullConference;

    /* renamed from: android.telecom.ConnectionService$3 */
    class AnonymousClass3 extends Listener {
        final /* synthetic */ ConnectionService this$0;

        AnonymousClass3(ConnectionService this$0) {
            this.this$0 = this$0;
        }

        public void onStateChanged(Conference conference, int oldState, int newState) {
            String id = (String) this.this$0.mIdByConference.get(conference);
            switch (newState) {
                case 4:
                    this.this$0.mAdapter.setActive(id);
                    return;
                case 5:
                    this.this$0.mAdapter.setOnHold(id);
                    return;
                default:
                    return;
            }
        }

        public void onDisconnected(Conference conference, DisconnectCause disconnectCause) {
            this.this$0.mAdapter.setDisconnected((String) this.this$0.mIdByConference.get(conference), disconnectCause);
        }

        public void onConnectionAdded(Conference conference, Connection connection) {
        }

        public void onConnectionRemoved(Conference conference, Connection connection) {
        }

        public void onConferenceableConnectionsChanged(Conference conference, List<Connection> conferenceableConnections) {
            this.this$0.mAdapter.setConferenceableConnections((String) this.this$0.mIdByConference.get(conference), this.this$0.createConnectionIdList(conferenceableConnections));
        }

        public void onDestroyed(Conference conference) {
            this.this$0.removeConference(conference);
        }

        public void onConnectionCapabilitiesChanged(Conference conference, int connectionCapabilities) {
            String id = (String) this.this$0.mIdByConference.get(conference);
            Object[] objArr = new Object[1];
            objArr[0] = Connection.capabilitiesToString(connectionCapabilities);
            Log.d((Object) this, "call capabilities: conference: %s", objArr);
            this.this$0.mAdapter.setConnectionCapabilities(id, connectionCapabilities);
        }

        public void onConnectionPropertiesChanged(Conference conference, int connectionProperties) {
            String id = (String) this.this$0.mIdByConference.get(conference);
            Object[] objArr = new Object[1];
            objArr[0] = Connection.propertiesToString(connectionProperties);
            Log.d((Object) this, "call capabilities: conference: %s", objArr);
            this.this$0.mAdapter.setConnectionProperties(id, connectionProperties);
        }

        public void onVideoStateChanged(Conference c, int videoState) {
            String id = (String) this.this$0.mIdByConference.get(c);
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(videoState);
            Log.d((Object) this, "onVideoStateChanged set video state %d", objArr);
            this.this$0.mAdapter.setVideoState(id, videoState);
        }

        public void onVideoProviderChanged(Conference c, VideoProvider videoProvider) {
            String id = (String) this.this$0.mIdByConference.get(c);
            Object[] objArr = new Object[2];
            objArr[0] = c;
            objArr[1] = videoProvider;
            Log.d((Object) this, "onVideoProviderChanged: Connection: %s, VideoProvider: %s", objArr);
            this.this$0.mAdapter.setVideoProvider(id, videoProvider);
        }

        public void onStatusHintsChanged(Conference conference, StatusHints statusHints) {
            String id = (String) this.this$0.mIdByConference.get(conference);
            if (id != null) {
                this.this$0.mAdapter.setStatusHints(id, statusHints);
            }
        }

        public void onExtrasChanged(Conference c, Bundle extras) {
            String id = (String) this.this$0.mIdByConference.get(c);
            if (id != null) {
                this.this$0.mAdapter.putExtras(id, extras);
            }
        }

        public void onExtrasRemoved(Conference c, List<String> keys) {
            String id = (String) this.this$0.mIdByConference.get(c);
            if (id != null) {
                this.this$0.mAdapter.removeExtras(id, keys);
            }
        }
    }

    /* renamed from: android.telecom.ConnectionService$4 */
    class AnonymousClass4 extends Connection.Listener {
        final /* synthetic */ ConnectionService this$0;

        AnonymousClass4(ConnectionService this$0) {
            this.this$0 = this$0;
        }

        public void onStateChanged(Connection c, int state) {
            String id = (String) this.this$0.mIdByConnection.get(c);
            Object[] objArr = new Object[2];
            objArr[0] = id;
            objArr[1] = Connection.stateToString(state);
            Log.d((Object) this, "Adapter set state %s %s", objArr);
            switch (state) {
                case 2:
                    this.this$0.mAdapter.setRinging(id);
                    return;
                case 3:
                    this.this$0.mAdapter.setDialing(id);
                    return;
                case 4:
                    this.this$0.mAdapter.setActive(id);
                    return;
                case 5:
                    this.this$0.mAdapter.setOnHold(id);
                    return;
                case 7:
                    this.this$0.mAdapter.setPulling(id);
                    return;
                default:
                    return;
            }
        }

        public void onDisconnected(Connection c, DisconnectCause disconnectCause) {
            String id = (String) this.this$0.mIdByConnection.get(c);
            Object[] objArr = new Object[1];
            objArr[0] = disconnectCause;
            Log.d((Object) this, "Adapter set disconnected %s", objArr);
            this.this$0.mAdapter.setDisconnected(id, disconnectCause);
        }

        public void onVideoStateChanged(Connection c, int videoState) {
            String id = (String) this.this$0.mIdByConnection.get(c);
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(videoState);
            Log.d((Object) this, "Adapter set video state %d", objArr);
            this.this$0.mAdapter.setVideoState(id, videoState);
        }

        public void onAddressChanged(Connection c, Uri address, int presentation) {
            this.this$0.mAdapter.setAddress((String) this.this$0.mIdByConnection.get(c), address, presentation);
        }

        public void onCallerDisplayNameChanged(Connection c, String callerDisplayName, int presentation) {
            this.this$0.mAdapter.setCallerDisplayName((String) this.this$0.mIdByConnection.get(c), callerDisplayName, presentation);
        }

        public void onDestroyed(Connection c) {
            this.this$0.removeConnection(c);
        }

        public void onPostDialWait(Connection c, String remaining) {
            String id = (String) this.this$0.mIdByConnection.get(c);
            Object[] objArr = new Object[2];
            objArr[0] = c;
            objArr[1] = remaining;
            Log.d((Object) this, "Adapter onPostDialWait %s, %s", objArr);
            this.this$0.mAdapter.onPostDialWait(id, remaining);
        }

        public void onPostDialChar(Connection c, char nextChar) {
            String id = (String) this.this$0.mIdByConnection.get(c);
            Object[] objArr = new Object[2];
            objArr[0] = c;
            objArr[1] = Character.valueOf(nextChar);
            Log.d((Object) this, "Adapter onPostDialChar %s, %s", objArr);
            this.this$0.mAdapter.onPostDialChar(id, nextChar);
        }

        public void onRingbackRequested(Connection c, boolean ringback) {
            String id = (String) this.this$0.mIdByConnection.get(c);
            Object[] objArr = new Object[1];
            objArr[0] = Boolean.valueOf(ringback);
            Log.d((Object) this, "Adapter onRingback %b", objArr);
            this.this$0.mAdapter.setRingbackRequested(id, ringback);
        }

        public void onConnectionCapabilitiesChanged(Connection c, int capabilities) {
            String id = (String) this.this$0.mIdByConnection.get(c);
            Object[] objArr = new Object[1];
            objArr[0] = Connection.capabilitiesToString(capabilities);
            Log.d((Object) this, "capabilities: parcelableconnection: %s", objArr);
            this.this$0.mAdapter.setConnectionCapabilities(id, capabilities);
        }

        public void onConnectionPropertiesChanged(Connection c, int properties) {
            String id = (String) this.this$0.mIdByConnection.get(c);
            Object[] objArr = new Object[1];
            objArr[0] = Connection.propertiesToString(properties);
            Log.d((Object) this, "properties: parcelableconnection: %s", objArr);
            this.this$0.mAdapter.setConnectionProperties(id, properties);
        }

        public void onVideoProviderChanged(Connection c, VideoProvider videoProvider) {
            String id = (String) this.this$0.mIdByConnection.get(c);
            Object[] objArr = new Object[2];
            objArr[0] = c;
            objArr[1] = videoProvider;
            Log.d((Object) this, "onVideoProviderChanged: Connection: %s, VideoProvider: %s", objArr);
            this.this$0.mAdapter.setVideoProvider(id, videoProvider);
        }

        public void onAudioModeIsVoipChanged(Connection c, boolean isVoip) {
            this.this$0.mAdapter.setIsVoipAudioMode((String) this.this$0.mIdByConnection.get(c), isVoip);
        }

        public void onStatusHintsChanged(Connection c, StatusHints statusHints) {
            this.this$0.mAdapter.setStatusHints((String) this.this$0.mIdByConnection.get(c), statusHints);
        }

        public void onConferenceablesChanged(Connection connection, List<Conferenceable> conferenceables) {
            this.this$0.mAdapter.setConferenceableConnections((String) this.this$0.mIdByConnection.get(connection), this.this$0.createIdList(conferenceables));
        }

        public void onConferenceChanged(Connection connection, Conference conference) {
            String id = (String) this.this$0.mIdByConnection.get(connection);
            if (id != null) {
                String str = null;
                if (conference != null) {
                    str = (String) this.this$0.mIdByConference.get(conference);
                }
                this.this$0.mAdapter.setIsConferenced(id, str);
            }
        }

        public void onConferenceMergeFailed(Connection connection) {
            String id = (String) this.this$0.mIdByConnection.get(connection);
            if (id != null) {
                this.this$0.mAdapter.onConferenceMergeFailed(id);
            }
        }

        public void onExtrasChanged(Connection c, Bundle extras) {
            String id = (String) this.this$0.mIdByConnection.get(c);
            if (id != null) {
                this.this$0.mAdapter.putExtras(id, extras);
            }
        }

        public void onExtrasRemoved(Connection c, List<String> keys) {
            String id = (String) this.this$0.mIdByConnection.get(c);
            if (id != null) {
                this.this$0.mAdapter.removeExtras(id, keys);
            }
        }

        public void onConnectionEvent(Connection connection, String event, Bundle extras) {
            String id = (String) this.this$0.mIdByConnection.get(connection);
            if (id != null) {
                this.this$0.mAdapter.onConnectionEvent(id, event, extras);
            }
        }
    }

    /* renamed from: android.telecom.ConnectionService$5 */
    class AnonymousClass5 extends Stub {
        final /* synthetic */ ConnectionService this$0;

        /* renamed from: android.telecom.ConnectionService$5$2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ AnonymousClass5 this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telecom.ConnectionService.5.2.<init>(android.telecom.ConnectionService$5):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass2(android.telecom.ConnectionService.AnonymousClass5 r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telecom.ConnectionService.5.2.<init>(android.telecom.ConnectionService$5):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.ConnectionService.5.2.<init>(android.telecom.ConnectionService$5):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.ConnectionService.5.2.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.ConnectionService.5.2.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.ConnectionService.5.2.run():void");
            }
        }

        AnonymousClass5(ConnectionService this$0) {
            this.this$0 = this$0;
        }

        public void onResult(final List<ComponentName> componentNames, final List<IBinder> services) {
            this.this$0.mHandler.post(new Runnable(this) {
                final /* synthetic */ AnonymousClass5 this$1;

                public void run() {
                    int i = 0;
                    while (i < componentNames.size() && i < services.size()) {
                        this.this$1.this$0.mRemoteConnectionManager.addConnectionService((ComponentName) componentNames.get(i), IConnectionService.Stub.asInterface((IBinder) services.get(i)));
                        i++;
                    }
                    this.this$1.this$0.onAccountsInitialized();
                    Log.d((Object) this, "remote connection services found: " + services, new Object[0]);
                }
            });
        }

        public void onError() {
            this.this$0.mHandler.post(new AnonymousClass2(this));
        }
    }

    /* renamed from: android.telecom.ConnectionService$6 */
    static class AnonymousClass6 extends Connection {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telecom.ConnectionService.6.<init>():void, dex: 
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
        AnonymousClass6() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telecom.ConnectionService.6.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.ConnectionService.6.<init>():void");
        }
    }

    /* renamed from: android.telecom.ConnectionService$7 */
    class AnonymousClass7 extends Conference {
        final /* synthetic */ ConnectionService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telecom.ConnectionService.7.<init>(android.telecom.ConnectionService, android.telecom.PhoneAccountHandle):void, dex: 
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
        AnonymousClass7(android.telecom.ConnectionService r1, android.telecom.PhoneAccountHandle r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telecom.ConnectionService.7.<init>(android.telecom.ConnectionService, android.telecom.PhoneAccountHandle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.ConnectionService.7.<init>(android.telecom.ConnectionService, android.telecom.PhoneAccountHandle):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telecom.ConnectionService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telecom.ConnectionService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.ConnectionService.<clinit>():void");
    }

    public ConnectionService() {
        this.mConnectionById = new ConcurrentHashMap();
        this.mIdByConnection = new ConcurrentHashMap();
        this.mConferenceById = new ConcurrentHashMap();
        this.mIdByConference = new ConcurrentHashMap();
        this.mRemoteConnectionManager = new RemoteConnectionManager(this);
        this.mPreInitializationConnectionRequests = new ArrayList();
        this.mAdapter = new ConnectionServiceAdapter();
        this.mAreAccountsInitialized = false;
        this.mIdSyncRoot = new Object();
        this.mId = 0;
        this.mBinder = new IConnectionService.Stub() {
            public void addConnectionServiceAdapter(IConnectionServiceAdapter adapter) {
                ConnectionService.this.mHandler.obtainMessage(1, adapter).sendToTarget();
            }

            public void removeConnectionServiceAdapter(IConnectionServiceAdapter adapter) {
                ConnectionService.this.mHandler.obtainMessage(16, adapter).sendToTarget();
            }

            public void createConnection(PhoneAccountHandle connectionManagerPhoneAccount, String id, ConnectionRequest request, boolean isIncoming, boolean isUnknown) {
                int i;
                int i2 = 1;
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = connectionManagerPhoneAccount;
                args.arg2 = id;
                args.arg3 = request;
                if (isIncoming) {
                    i = 1;
                } else {
                    i = 0;
                }
                args.argi1 = i;
                if (!isUnknown) {
                    i2 = 0;
                }
                args.argi2 = i2;
                ConnectionService.this.mHandler.obtainMessage(2, args).sendToTarget();
            }

            public void abort(String callId) {
                ConnectionService.this.mHandler.obtainMessage(3, callId).sendToTarget();
            }

            public void answerVideo(String callId, int videoState) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.argi1 = videoState;
                ConnectionService.this.mHandler.obtainMessage(17, args).sendToTarget();
            }

            public void answer(String callId) {
                ConnectionService.this.mHandler.obtainMessage(4, callId).sendToTarget();
            }

            public void reject(String callId) {
                ConnectionService.this.mHandler.obtainMessage(5, callId).sendToTarget();
            }

            public void rejectWithMessage(String callId, String message) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = message;
                ConnectionService.this.mHandler.obtainMessage(20, args).sendToTarget();
            }

            public void silence(String callId) {
                ConnectionService.this.mHandler.obtainMessage(21, callId).sendToTarget();
            }

            public void disconnect(String callId) {
                ConnectionService.this.mHandler.obtainMessage(6, callId).sendToTarget();
            }

            public void hold(String callId) {
                ConnectionService.this.mHandler.obtainMessage(7, callId).sendToTarget();
            }

            public void unhold(String callId) {
                ConnectionService.this.mHandler.obtainMessage(8, callId).sendToTarget();
            }

            public void onCallAudioStateChanged(String callId, CallAudioState callAudioState) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = callAudioState;
                ConnectionService.this.mHandler.obtainMessage(9, args).sendToTarget();
            }

            public void playDtmfTone(String callId, char digit) {
                ConnectionService.this.mHandler.obtainMessage(10, digit, 0, callId).sendToTarget();
            }

            public void stopDtmfTone(String callId) {
                ConnectionService.this.mHandler.obtainMessage(11, callId).sendToTarget();
            }

            public void conference(String callId1, String callId2) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId1;
                args.arg2 = callId2;
                ConnectionService.this.mHandler.obtainMessage(12, args).sendToTarget();
            }

            public void splitFromConference(String callId) {
                ConnectionService.this.mHandler.obtainMessage(13, callId).sendToTarget();
            }

            public void mergeConference(String callId) {
                ConnectionService.this.mHandler.obtainMessage(18, callId).sendToTarget();
            }

            public void swapConference(String callId) {
                ConnectionService.this.mHandler.obtainMessage(19, callId).sendToTarget();
            }

            public void onPostDialContinue(String callId, boolean proceed) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.argi1 = proceed ? 1 : 0;
                ConnectionService.this.mHandler.obtainMessage(14, args).sendToTarget();
            }

            public void pullExternalCall(String callId) {
                ConnectionService.this.mHandler.obtainMessage(22, callId).sendToTarget();
            }

            public void sendCallEvent(String callId, String event, Bundle extras) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = event;
                args.arg3 = extras;
                ConnectionService.this.mHandler.obtainMessage(23, args).sendToTarget();
            }

            public void onExtrasChanged(String callId, Bundle extras) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = extras;
                ConnectionService.this.mHandler.obtainMessage(24, args).sendToTarget();
            }

            public void explicitCallTransfer(String callId) {
                ConnectionService.this.mHandler.obtainMessage(1000, callId).sendToTarget();
            }

            public void blindAssuredEct(String callId, String number, int type) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = number;
                args.argi1 = type;
                ConnectionService.this.mHandler.obtainMessage(1005, args).sendToTarget();
            }

            public void hangupAll(String callId) {
                ConnectionService.this.mHandler.obtainMessage(1001, callId).sendToTarget();
            }

            public void inviteConferenceParticipants(String conferenceCallId, List<String> numbers) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = conferenceCallId;
                args.arg2 = numbers;
                ConnectionService.this.mHandler.obtainMessage(1002, args).sendToTarget();
            }

            public void createConference(PhoneAccountHandle connectionManagerPhoneAccount, String conferenceCallId, ConnectionRequest request, List<String> numbers, boolean isIncoming) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = connectionManagerPhoneAccount;
                args.arg2 = conferenceCallId;
                args.arg3 = request;
                args.arg4 = numbers;
                args.argi1 = isIncoming ? 1 : 0;
                ConnectionService.this.mHandler.obtainMessage(1003, args).sendToTarget();
            }

            public void handleOrderedOperation(String callId, String currentOperation, String pendingOperation) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = callId;
                args.arg2 = currentOperation;
                args.arg3 = pendingOperation;
                ConnectionService.this.mHandler.obtainMessage(1004, args).sendToTarget();
            }
        };
        this.mHandler = new Handler(this, Looper.getMainLooper()) {
            final /* synthetic */ ConnectionService this$0;

            /* renamed from: android.telecom.ConnectionService$2$2 */
            class AnonymousClass2 implements Runnable {
                final /* synthetic */ AnonymousClass2 this$1;
                final /* synthetic */ String val$conferenceCallId;
                final /* synthetic */ PhoneAccountHandle val$connectionManagerPhoneAccount;
                final /* synthetic */ boolean val$isIncoming;
                final /* synthetic */ List val$numbers;
                final /* synthetic */ ConnectionRequest val$request;

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telecom.ConnectionService.2.2.<init>(android.telecom.ConnectionService$2, android.telecom.PhoneAccountHandle, java.lang.String, android.telecom.ConnectionRequest, java.util.List, boolean):void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 11 more
                    */
                AnonymousClass2(android.telecom.ConnectionService.AnonymousClass2 r1, android.telecom.PhoneAccountHandle r2, java.lang.String r3, android.telecom.ConnectionRequest r4, java.util.List r5, boolean r6) {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telecom.ConnectionService.2.2.<init>(android.telecom.ConnectionService$2, android.telecom.PhoneAccountHandle, java.lang.String, android.telecom.ConnectionRequest, java.util.List, boolean):void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: android.telecom.ConnectionService.2.2.<init>(android.telecom.ConnectionService$2, android.telecom.PhoneAccountHandle, java.lang.String, android.telecom.ConnectionRequest, java.util.List, boolean):void");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.ConnectionService.2.2.run():void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 11 more
                    */
                public void run() {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.ConnectionService.2.2.run():void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: android.telecom.ConnectionService.2.2.run():void");
                }
            }

            /*  JADX ERROR: NullPointerException in pass: ModVisitor
                java.lang.NullPointerException
                	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
                	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
                	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
                	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
                	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
                	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
                	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
                	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
                	at java.util.ArrayList.forEach(ArrayList.java:1251)
                	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
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
            public void handleMessage(android.os.Message r31) {
                /*
                r30 = this;
                r0 = r31;
                r3 = r0.what;
                switch(r3) {
                    case 1: goto L_0x0008;
                    case 2: goto L_0x0033;
                    case 3: goto L_0x009d;
                    case 4: goto L_0x00ac;
                    case 5: goto L_0x00e6;
                    case 6: goto L_0x011a;
                    case 7: goto L_0x0138;
                    case 8: goto L_0x0147;
                    case 9: goto L_0x0156;
                    case 10: goto L_0x0188;
                    case 11: goto L_0x019c;
                    case 12: goto L_0x01ab;
                    case 13: goto L_0x01d8;
                    case 14: goto L_0x0205;
                    case 16: goto L_0x0021;
                    case 17: goto L_0x00bb;
                    case 18: goto L_0x01e7;
                    case 19: goto L_0x01f6;
                    case 20: goto L_0x00f5;
                    case 21: goto L_0x0129;
                    case 22: goto L_0x0236;
                    case 23: goto L_0x0245;
                    case 24: goto L_0x027c;
                    case 1000: goto L_0x02a9;
                    case 1001: goto L_0x02b8;
                    case 1002: goto L_0x02c7;
                    case 1003: goto L_0x02ec;
                    case 1004: goto L_0x035a;
                    case 1005: goto L_0x039a;
                    default: goto L_0x0007;
                };
            L_0x0007:
                return;
            L_0x0008:
                r0 = r30;
                r3 = r0.this$0;
                r4 = r3.mAdapter;
                r0 = r31;
                r3 = r0.obj;
                r3 = (com.android.internal.telecom.IConnectionServiceAdapter) r3;
                r4.addAdapter(r3);
                r0 = r30;
                r3 = r0.this$0;
                r3.onAdapterAttached();
                goto L_0x0007;
            L_0x0021:
                r0 = r30;
                r3 = r0.this$0;
                r4 = r3.mAdapter;
                r0 = r31;
                r3 = r0.obj;
                r3 = (com.android.internal.telecom.IConnectionServiceAdapter) r3;
                r4.removeAdapter(r3);
                goto L_0x0007;
            L_0x0033:
                r0 = r31;
                r0 = r0.obj;
                r17 = r0;
                r17 = (com.android.internal.os.SomeArgs) r17;
                r0 = r17;	 Catch:{ all -> 0x0098 }
                r5 = r0.arg1;	 Catch:{ all -> 0x0098 }
                r5 = (android.telecom.PhoneAccountHandle) r5;	 Catch:{ all -> 0x0098 }
                r0 = r17;	 Catch:{ all -> 0x0098 }
                r6 = r0.arg2;	 Catch:{ all -> 0x0098 }
                r6 = (java.lang.String) r6;	 Catch:{ all -> 0x0098 }
                r0 = r17;	 Catch:{ all -> 0x0098 }
                r7 = r0.arg3;	 Catch:{ all -> 0x0098 }
                r7 = (android.telecom.ConnectionRequest) r7;	 Catch:{ all -> 0x0098 }
                r0 = r17;	 Catch:{ all -> 0x0098 }
                r3 = r0.argi1;	 Catch:{ all -> 0x0098 }
                r4 = 1;	 Catch:{ all -> 0x0098 }
                if (r3 != r4) goto L_0x008c;	 Catch:{ all -> 0x0098 }
            L_0x0054:
                r8 = 1;	 Catch:{ all -> 0x0098 }
            L_0x0055:
                r0 = r17;	 Catch:{ all -> 0x0098 }
                r3 = r0.argi2;	 Catch:{ all -> 0x0098 }
                r4 = 1;	 Catch:{ all -> 0x0098 }
                if (r3 != r4) goto L_0x008e;	 Catch:{ all -> 0x0098 }
            L_0x005c:
                r9 = 1;	 Catch:{ all -> 0x0098 }
            L_0x005d:
                r0 = r30;	 Catch:{ all -> 0x0098 }
                r3 = r0.this$0;	 Catch:{ all -> 0x0098 }
                r3 = r3.mAreAccountsInitialized;	 Catch:{ all -> 0x0098 }
                if (r3 != 0) goto L_0x0090;	 Catch:{ all -> 0x0098 }
            L_0x0067:
                r3 = "Enqueueing pre-init request %s";	 Catch:{ all -> 0x0098 }
                r4 = 1;	 Catch:{ all -> 0x0098 }
                r4 = new java.lang.Object[r4];	 Catch:{ all -> 0x0098 }
                r10 = 0;	 Catch:{ all -> 0x0098 }
                r4[r10] = r6;	 Catch:{ all -> 0x0098 }
                r0 = r30;	 Catch:{ all -> 0x0098 }
                android.telecom.Log.d(r0, r3, r4);	 Catch:{ all -> 0x0098 }
                r0 = r30;	 Catch:{ all -> 0x0098 }
                r3 = r0.this$0;	 Catch:{ all -> 0x0098 }
                r10 = r3.mPreInitializationConnectionRequests;	 Catch:{ all -> 0x0098 }
                r3 = new android.telecom.ConnectionService$2$1;	 Catch:{ all -> 0x0098 }
                r4 = r30;	 Catch:{ all -> 0x0098 }
                r3.<init>(r5, r6, r7, r8, r9);	 Catch:{ all -> 0x0098 }
                r10.add(r3);	 Catch:{ all -> 0x0098 }
            L_0x0087:
                r17.recycle();
                goto L_0x0007;
            L_0x008c:
                r8 = 0;
                goto L_0x0055;
            L_0x008e:
                r9 = 0;
                goto L_0x005d;
            L_0x0090:
                r0 = r30;	 Catch:{ all -> 0x0098 }
                r4 = r0.this$0;	 Catch:{ all -> 0x0098 }
                r4.createConnection(r5, r6, r7, r8, r9);	 Catch:{ all -> 0x0098 }
                goto L_0x0087;
            L_0x0098:
                r3 = move-exception;
                r17.recycle();
                throw r3;
            L_0x009d:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.abort(r3);
                goto L_0x0007;
            L_0x00ac:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.answer(r3);
                goto L_0x0007;
            L_0x00bb:
                r0 = r31;
                r0 = r0.obj;
                r17 = r0;
                r17 = (com.android.internal.os.SomeArgs) r17;
                r0 = r17;	 Catch:{ all -> 0x00e1 }
                r0 = r0.arg1;	 Catch:{ all -> 0x00e1 }
                r19 = r0;	 Catch:{ all -> 0x00e1 }
                r19 = (java.lang.String) r19;	 Catch:{ all -> 0x00e1 }
                r0 = r17;	 Catch:{ all -> 0x00e1 }
                r0 = r0.argi1;	 Catch:{ all -> 0x00e1 }
                r29 = r0;	 Catch:{ all -> 0x00e1 }
                r0 = r30;	 Catch:{ all -> 0x00e1 }
                r3 = r0.this$0;	 Catch:{ all -> 0x00e1 }
                r0 = r19;	 Catch:{ all -> 0x00e1 }
                r1 = r29;	 Catch:{ all -> 0x00e1 }
                r3.answerVideo(r0, r1);	 Catch:{ all -> 0x00e1 }
                r17.recycle();
                goto L_0x0007;
            L_0x00e1:
                r3 = move-exception;
                r17.recycle();
                throw r3;
            L_0x00e6:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.reject(r3);
                goto L_0x0007;
            L_0x00f5:
                r0 = r31;
                r0 = r0.obj;
                r17 = r0;
                r17 = (com.android.internal.os.SomeArgs) r17;
                r0 = r30;	 Catch:{ all -> 0x0115 }
                r10 = r0.this$0;	 Catch:{ all -> 0x0115 }
                r0 = r17;	 Catch:{ all -> 0x0115 }
                r3 = r0.arg1;	 Catch:{ all -> 0x0115 }
                r3 = (java.lang.String) r3;	 Catch:{ all -> 0x0115 }
                r0 = r17;	 Catch:{ all -> 0x0115 }
                r4 = r0.arg2;	 Catch:{ all -> 0x0115 }
                r4 = (java.lang.String) r4;	 Catch:{ all -> 0x0115 }
                r10.reject(r3, r4);	 Catch:{ all -> 0x0115 }
                r17.recycle();
                goto L_0x0007;
            L_0x0115:
                r3 = move-exception;
                r17.recycle();
                throw r3;
            L_0x011a:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.disconnect(r3);
                goto L_0x0007;
            L_0x0129:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.silence(r3);
                goto L_0x0007;
            L_0x0138:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.hold(r3);
                goto L_0x0007;
            L_0x0147:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.unhold(r3);
                goto L_0x0007;
            L_0x0156:
                r0 = r31;
                r0 = r0.obj;
                r17 = r0;
                r17 = (com.android.internal.os.SomeArgs) r17;
                r0 = r17;	 Catch:{ all -> 0x0183 }
                r0 = r0.arg1;	 Catch:{ all -> 0x0183 }
                r19 = r0;	 Catch:{ all -> 0x0183 }
                r19 = (java.lang.String) r19;	 Catch:{ all -> 0x0183 }
                r0 = r17;	 Catch:{ all -> 0x0183 }
                r0 = r0.arg2;	 Catch:{ all -> 0x0183 }
                r18 = r0;	 Catch:{ all -> 0x0183 }
                r18 = (android.telecom.CallAudioState) r18;	 Catch:{ all -> 0x0183 }
                r0 = r30;	 Catch:{ all -> 0x0183 }
                r3 = r0.this$0;	 Catch:{ all -> 0x0183 }
                r4 = new android.telecom.CallAudioState;	 Catch:{ all -> 0x0183 }
                r0 = r18;	 Catch:{ all -> 0x0183 }
                r4.<init>(r0);	 Catch:{ all -> 0x0183 }
                r0 = r19;	 Catch:{ all -> 0x0183 }
                r3.onCallAudioStateChanged(r0, r4);	 Catch:{ all -> 0x0183 }
                r17.recycle();
                goto L_0x0007;
            L_0x0183:
                r3 = move-exception;
                r17.recycle();
                throw r3;
            L_0x0188:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r0 = r31;
                r10 = r0.arg1;
                r10 = (char) r10;
                r4.playDtmfTone(r3, r10);
                goto L_0x0007;
            L_0x019c:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.stopDtmfTone(r3);
                goto L_0x0007;
            L_0x01ab:
                r0 = r31;
                r0 = r0.obj;
                r17 = r0;
                r17 = (com.android.internal.os.SomeArgs) r17;
                r0 = r17;	 Catch:{ all -> 0x01d3 }
                r0 = r0.arg1;	 Catch:{ all -> 0x01d3 }
                r20 = r0;	 Catch:{ all -> 0x01d3 }
                r20 = (java.lang.String) r20;	 Catch:{ all -> 0x01d3 }
                r0 = r17;	 Catch:{ all -> 0x01d3 }
                r0 = r0.arg2;	 Catch:{ all -> 0x01d3 }
                r21 = r0;	 Catch:{ all -> 0x01d3 }
                r21 = (java.lang.String) r21;	 Catch:{ all -> 0x01d3 }
                r0 = r30;	 Catch:{ all -> 0x01d3 }
                r3 = r0.this$0;	 Catch:{ all -> 0x01d3 }
                r0 = r20;	 Catch:{ all -> 0x01d3 }
                r1 = r21;	 Catch:{ all -> 0x01d3 }
                r3.conference(r0, r1);	 Catch:{ all -> 0x01d3 }
                r17.recycle();
                goto L_0x0007;
            L_0x01d3:
                r3 = move-exception;
                r17.recycle();
                throw r3;
            L_0x01d8:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.splitFromConference(r3);
                goto L_0x0007;
            L_0x01e7:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.mergeConference(r3);
                goto L_0x0007;
            L_0x01f6:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.swapConference(r3);
                goto L_0x0007;
            L_0x0205:
                r0 = r31;
                r0 = r0.obj;
                r17 = r0;
                r17 = (com.android.internal.os.SomeArgs) r17;
                r0 = r17;	 Catch:{ all -> 0x0231 }
                r0 = r0.arg1;	 Catch:{ all -> 0x0231 }
                r19 = r0;	 Catch:{ all -> 0x0231 }
                r19 = (java.lang.String) r19;	 Catch:{ all -> 0x0231 }
                r0 = r17;	 Catch:{ all -> 0x0231 }
                r3 = r0.argi1;	 Catch:{ all -> 0x0231 }
                r4 = 1;	 Catch:{ all -> 0x0231 }
                if (r3 != r4) goto L_0x022e;	 Catch:{ all -> 0x0231 }
            L_0x021c:
                r27 = 1;	 Catch:{ all -> 0x0231 }
            L_0x021e:
                r0 = r30;	 Catch:{ all -> 0x0231 }
                r3 = r0.this$0;	 Catch:{ all -> 0x0231 }
                r0 = r19;	 Catch:{ all -> 0x0231 }
                r1 = r27;	 Catch:{ all -> 0x0231 }
                r3.onPostDialContinue(r0, r1);	 Catch:{ all -> 0x0231 }
                r17.recycle();
                goto L_0x0007;
            L_0x022e:
                r27 = 0;
                goto L_0x021e;
            L_0x0231:
                r3 = move-exception;
                r17.recycle();
                throw r3;
            L_0x0236:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.pullExternalCall(r3);
                goto L_0x0007;
            L_0x0245:
                r0 = r31;
                r0 = r0.obj;
                r17 = r0;
                r17 = (com.android.internal.os.SomeArgs) r17;
                r0 = r17;	 Catch:{ all -> 0x0277 }
                r0 = r0.arg1;	 Catch:{ all -> 0x0277 }
                r19 = r0;	 Catch:{ all -> 0x0277 }
                r19 = (java.lang.String) r19;	 Catch:{ all -> 0x0277 }
                r0 = r17;	 Catch:{ all -> 0x0277 }
                r0 = r0.arg2;	 Catch:{ all -> 0x0277 }
                r23 = r0;	 Catch:{ all -> 0x0277 }
                r23 = (java.lang.String) r23;	 Catch:{ all -> 0x0277 }
                r0 = r17;	 Catch:{ all -> 0x0277 }
                r0 = r0.arg3;	 Catch:{ all -> 0x0277 }
                r24 = r0;	 Catch:{ all -> 0x0277 }
                r24 = (android.os.Bundle) r24;	 Catch:{ all -> 0x0277 }
                r0 = r30;	 Catch:{ all -> 0x0277 }
                r3 = r0.this$0;	 Catch:{ all -> 0x0277 }
                r0 = r19;	 Catch:{ all -> 0x0277 }
                r1 = r23;	 Catch:{ all -> 0x0277 }
                r2 = r24;	 Catch:{ all -> 0x0277 }
                r3.sendCallEvent(r0, r1, r2);	 Catch:{ all -> 0x0277 }
                r17.recycle();
                goto L_0x0007;
            L_0x0277:
                r3 = move-exception;
                r17.recycle();
                throw r3;
            L_0x027c:
                r0 = r31;
                r0 = r0.obj;
                r17 = r0;
                r17 = (com.android.internal.os.SomeArgs) r17;
                r0 = r17;	 Catch:{ all -> 0x02a4 }
                r0 = r0.arg1;	 Catch:{ all -> 0x02a4 }
                r19 = r0;	 Catch:{ all -> 0x02a4 }
                r19 = (java.lang.String) r19;	 Catch:{ all -> 0x02a4 }
                r0 = r17;	 Catch:{ all -> 0x02a4 }
                r0 = r0.arg2;	 Catch:{ all -> 0x02a4 }
                r24 = r0;	 Catch:{ all -> 0x02a4 }
                r24 = (android.os.Bundle) r24;	 Catch:{ all -> 0x02a4 }
                r0 = r30;	 Catch:{ all -> 0x02a4 }
                r3 = r0.this$0;	 Catch:{ all -> 0x02a4 }
                r0 = r19;	 Catch:{ all -> 0x02a4 }
                r1 = r24;	 Catch:{ all -> 0x02a4 }
                r3.handleExtrasChanged(r0, r1);	 Catch:{ all -> 0x02a4 }
                r17.recycle();
                goto L_0x0007;
            L_0x02a4:
                r3 = move-exception;
                r17.recycle();
                throw r3;
            L_0x02a9:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.explicitCallTransfer(r3);
                goto L_0x0007;
            L_0x02b8:
                r0 = r30;
                r4 = r0.this$0;
                r0 = r31;
                r3 = r0.obj;
                r3 = (java.lang.String) r3;
                r4.hangupAll(r3);
                goto L_0x0007;
            L_0x02c7:
                r0 = r31;
                r0 = r0.obj;
                r17 = r0;
                r17 = (com.android.internal.os.SomeArgs) r17;
                r0 = r17;	 Catch:{ all -> 0x02e7 }
                r13 = r0.arg1;	 Catch:{ all -> 0x02e7 }
                r13 = (java.lang.String) r13;	 Catch:{ all -> 0x02e7 }
                r0 = r17;	 Catch:{ all -> 0x02e7 }
                r15 = r0.arg2;	 Catch:{ all -> 0x02e7 }
                r15 = (java.util.List) r15;	 Catch:{ all -> 0x02e7 }
                r0 = r30;	 Catch:{ all -> 0x02e7 }
                r3 = r0.this$0;	 Catch:{ all -> 0x02e7 }
                r3.inviteConferenceParticipants(r13, r15);	 Catch:{ all -> 0x02e7 }
                r17.recycle();
                goto L_0x0007;
            L_0x02e7:
                r3 = move-exception;
                r17.recycle();
                throw r3;
            L_0x02ec:
                r0 = r31;
                r0 = r0.obj;
                r17 = r0;
                r17 = (com.android.internal.os.SomeArgs) r17;
                r0 = r17;	 Catch:{ all -> 0x0355 }
                r5 = r0.arg1;	 Catch:{ all -> 0x0355 }
                r5 = (android.telecom.PhoneAccountHandle) r5;	 Catch:{ all -> 0x0355 }
                r0 = r17;	 Catch:{ all -> 0x0355 }
                r13 = r0.arg2;	 Catch:{ all -> 0x0355 }
                r13 = (java.lang.String) r13;	 Catch:{ all -> 0x0355 }
                r0 = r17;	 Catch:{ all -> 0x0355 }
                r7 = r0.arg3;	 Catch:{ all -> 0x0355 }
                r7 = (android.telecom.ConnectionRequest) r7;	 Catch:{ all -> 0x0355 }
                r0 = r17;	 Catch:{ all -> 0x0355 }
                r15 = r0.arg4;	 Catch:{ all -> 0x0355 }
                r15 = (java.util.List) r15;	 Catch:{ all -> 0x0355 }
                r0 = r17;	 Catch:{ all -> 0x0355 }
                r3 = r0.argi1;	 Catch:{ all -> 0x0355 }
                r4 = 1;	 Catch:{ all -> 0x0355 }
                if (r3 != r4) goto L_0x0347;	 Catch:{ all -> 0x0355 }
            L_0x0313:
                r8 = 1;	 Catch:{ all -> 0x0355 }
            L_0x0314:
                r0 = r30;	 Catch:{ all -> 0x0355 }
                r3 = r0.this$0;	 Catch:{ all -> 0x0355 }
                r3 = r3.mAreAccountsInitialized;	 Catch:{ all -> 0x0355 }
                if (r3 != 0) goto L_0x0349;	 Catch:{ all -> 0x0355 }
            L_0x031e:
                r3 = "Enqueueing pre-init request %s";	 Catch:{ all -> 0x0355 }
                r4 = 1;	 Catch:{ all -> 0x0355 }
                r4 = new java.lang.Object[r4];	 Catch:{ all -> 0x0355 }
                r10 = 0;	 Catch:{ all -> 0x0355 }
                r4[r10] = r13;	 Catch:{ all -> 0x0355 }
                r0 = r30;	 Catch:{ all -> 0x0355 }
                android.telecom.Log.d(r0, r3, r4);	 Catch:{ all -> 0x0355 }
                r0 = r30;	 Catch:{ all -> 0x0355 }
                r3 = r0.this$0;	 Catch:{ all -> 0x0355 }
                r3 = r3.mPreInitializationConnectionRequests;	 Catch:{ all -> 0x0355 }
                r10 = new android.telecom.ConnectionService$2$2;	 Catch:{ all -> 0x0355 }
                r11 = r30;	 Catch:{ all -> 0x0355 }
                r12 = r5;	 Catch:{ all -> 0x0355 }
                r14 = r7;	 Catch:{ all -> 0x0355 }
                r16 = r8;	 Catch:{ all -> 0x0355 }
                r10.<init>(r11, r12, r13, r14, r15, r16);	 Catch:{ all -> 0x0355 }
                r3.add(r10);	 Catch:{ all -> 0x0355 }
            L_0x0342:
                r17.recycle();
                goto L_0x0007;
            L_0x0347:
                r8 = 0;
                goto L_0x0314;
            L_0x0349:
                r0 = r30;	 Catch:{ all -> 0x0355 }
                r11 = r0.this$0;	 Catch:{ all -> 0x0355 }
                r12 = r5;	 Catch:{ all -> 0x0355 }
                r14 = r7;	 Catch:{ all -> 0x0355 }
                r16 = r8;	 Catch:{ all -> 0x0355 }
                r11.createConference(r12, r13, r14, r15, r16);	 Catch:{ all -> 0x0355 }
                goto L_0x0342;
            L_0x0355:
                r3 = move-exception;
                r17.recycle();
                throw r3;
            L_0x035a:
                r0 = r31;
                r0 = r0.obj;
                r17 = r0;
                r17 = (com.android.internal.os.SomeArgs) r17;
                r0 = r17;	 Catch:{ all -> 0x0395 }
                r0 = r0.arg1;	 Catch:{ all -> 0x0395 }
                r19 = r0;	 Catch:{ all -> 0x0395 }
                r19 = (java.lang.String) r19;	 Catch:{ all -> 0x0395 }
                r0 = r17;	 Catch:{ all -> 0x0395 }
                r0 = r0.arg2;	 Catch:{ all -> 0x0395 }
                r22 = r0;	 Catch:{ all -> 0x0395 }
                r22 = (java.lang.String) r22;	 Catch:{ all -> 0x0395 }
                r0 = r17;	 Catch:{ all -> 0x0395 }
                r0 = r0.arg3;	 Catch:{ all -> 0x0395 }
                r26 = r0;	 Catch:{ all -> 0x0395 }
                r26 = (java.lang.String) r26;	 Catch:{ all -> 0x0395 }
                r3 = "disconnect";	 Catch:{ all -> 0x0395 }
                r0 = r22;	 Catch:{ all -> 0x0395 }
                r3 = r3.equals(r0);	 Catch:{ all -> 0x0395 }
                if (r3 == 0) goto L_0x0390;	 Catch:{ all -> 0x0395 }
            L_0x0385:
                r0 = r30;	 Catch:{ all -> 0x0395 }
                r3 = r0.this$0;	 Catch:{ all -> 0x0395 }
                r0 = r19;	 Catch:{ all -> 0x0395 }
                r1 = r26;	 Catch:{ all -> 0x0395 }
                r3.disconnect(r0, r1);	 Catch:{ all -> 0x0395 }
            L_0x0390:
                r17.recycle();
                goto L_0x0007;
            L_0x0395:
                r3 = move-exception;
                r17.recycle();
                throw r3;
            L_0x039a:
                r0 = r31;
                r0 = r0.obj;
                r17 = r0;
                r17 = (com.android.internal.os.SomeArgs) r17;
                r0 = r17;	 Catch:{ all -> 0x03ca }
                r0 = r0.arg1;	 Catch:{ all -> 0x03ca }
                r19 = r0;	 Catch:{ all -> 0x03ca }
                r19 = (java.lang.String) r19;	 Catch:{ all -> 0x03ca }
                r0 = r17;	 Catch:{ all -> 0x03ca }
                r0 = r0.arg2;	 Catch:{ all -> 0x03ca }
                r25 = r0;	 Catch:{ all -> 0x03ca }
                r25 = (java.lang.String) r25;	 Catch:{ all -> 0x03ca }
                r0 = r17;	 Catch:{ all -> 0x03ca }
                r0 = r0.argi1;	 Catch:{ all -> 0x03ca }
                r28 = r0;	 Catch:{ all -> 0x03ca }
                r0 = r30;	 Catch:{ all -> 0x03ca }
                r3 = r0.this$0;	 Catch:{ all -> 0x03ca }
                r0 = r19;	 Catch:{ all -> 0x03ca }
                r1 = r25;	 Catch:{ all -> 0x03ca }
                r2 = r28;	 Catch:{ all -> 0x03ca }
                r3.explicitCallTransfer(r0, r1, r2);	 Catch:{ all -> 0x03ca }
                r17.recycle();
                goto L_0x0007;
            L_0x03ca:
                r3 = move-exception;
                r17.recycle();
                throw r3;
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.ConnectionService.2.handleMessage(android.os.Message):void");
            }
        };
        this.mConferenceListener = new AnonymousClass3(this);
        this.mConnectionListener = new AnonymousClass4(this);
        this.LOG_TAG_FROM = "IConnectionService-->";
    }

    public final IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public boolean onUnbind(Intent intent) {
        endAllConnections();
        return super.onUnbind(intent);
    }

    private void createConnection(PhoneAccountHandle callManagerAccount, String callId, ConnectionRequest request, boolean isIncoming, boolean isUnknown) {
        Connection connection;
        Object[] objArr = new Object[5];
        objArr[0] = callManagerAccount;
        objArr[1] = callId;
        objArr[2] = request;
        objArr[3] = Boolean.valueOf(isIncoming);
        objArr[4] = Boolean.valueOf(isUnknown);
        Log.d((Object) this, "createConnection, callManagerAccount: %s, callId: %s, request: %s, isIncoming: %b, isUnknown: %b", objArr);
        if (!isIncoming) {
            String callNumber = null;
            if (!(request == null || request.getAddress() == null)) {
                callNumber = request.getAddress().getSchemeSpecificPart();
            }
            FormattedLog formattedLog = new Builder().setCategory("CC").setServiceName(getConnectionServiceName()).setOpType(OpType.OPERATION).setActionName("Dial").setCallNumber(Rlog.pii(SDBG, (Object) callNumber)).setCallId(PhoneConstants.MVNO_TYPE_NONE).buildDebugMsg();
            if (formattedLog != null && (!SENLOG || TELDBG)) {
                Log.d((Object) this, formattedLog.toString(), new Object[0]);
            }
        }
        if (isUnknown) {
            connection = onCreateUnknownConnection(callManagerAccount, request);
        } else if (isIncoming) {
            connection = onCreateIncomingConnection(callManagerAccount, request);
        } else {
            connection = onCreateOutgoingConnection(callManagerAccount, request);
        }
        objArr = new Object[1];
        objArr[0] = connection;
        Log.d((Object) this, "createConnection, connection: %s", objArr);
        if (connection == null) {
            connection = Connection.createFailedConnection(new DisconnectCause(1));
        }
        connection.setTelecomCallId(callId);
        if (connection.getState() != 6) {
            addConnection(callId, connection);
        }
        Uri address = connection.getAddress();
        objArr = new Object[4];
        objArr[0] = Connection.toLogSafePhoneNumber(address == null ? "null" : address.getSchemeSpecificPart());
        objArr[1] = Connection.stateToString(connection.getState());
        objArr[2] = Connection.capabilitiesToString(connection.getConnectionCapabilities());
        objArr[3] = Connection.propertiesToString(connection.getConnectionProperties());
        Log.v((Object) this, "createConnection, number: %s, state: %s, capabilities: %s, properties: %s", objArr);
        objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "createConnection, calling handleCreateConnectionSuccessful %s", objArr);
        PhoneAccountHandle handle = connection.getAccountHandle();
        if (handle == null) {
            handle = request.getAccountHandle();
        } else {
            objArr = new Object[1];
            objArr[0] = handle;
            Log.d((Object) this, "createConnection, set back phone account:%s", objArr);
        }
        this.mAdapter.handleCreateConnectionComplete(callId, request, new ParcelableConnection(handle, connection.getState(), connection.getConnectionCapabilities(), connection.getConnectionProperties(), connection.getAddress(), connection.getAddressPresentation(), connection.getCallerDisplayName(), connection.getCallerDisplayNamePresentation(), connection.getVideoProvider() == null ? null : connection.getVideoProvider().getInterface(), connection.getVideoState(), connection.isRingbackRequested(), connection.getAudioModeIsVoip(), connection.getConnectTimeMillis(), connection.getStatusHints(), connection.getDisconnectCause(), createIdList(connection.getConferenceables()), connection.getExtras()));
        if (isUnknown) {
            triggerConferenceRecalculate();
        }
        if (connection.getState() != 6) {
            forceSuppMessageUpdate(connection);
        }
    }

    public void createConnectionInternal(String callId, ConnectionRequest request) {
        Object[] objArr = new Object[2];
        objArr[0] = callId;
        objArr[1] = request;
        Log.d((Object) this, "createConnectionInternal, callId: %s, request: %s", objArr);
        Connection connection = onCreateOutgoingConnection(null, request);
        objArr = new Object[1];
        objArr[0] = connection;
        Log.d((Object) this, "createConnectionInternal, connection: %s", objArr);
        if (connection == null) {
            connection = Connection.createFailedConnection(new DisconnectCause(1));
        }
        connection.setTelecomCallId(callId);
        if (connection.getState() != 6) {
            addConnection(callId, connection);
        }
        Uri address = connection.getAddress();
        objArr = new Object[4];
        objArr[0] = Connection.toLogSafePhoneNumber(address == null ? "null" : address.getSchemeSpecificPart());
        objArr[1] = Connection.stateToString(connection.getState());
        objArr[2] = Connection.capabilitiesToString(connection.getConnectionCapabilities());
        objArr[3] = Connection.propertiesToString(connection.getConnectionProperties());
        Log.v((Object) this, "createConnectionInternal, number:%s, state:%s, capabilities:%s, properties:%s", objArr);
        objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "createConnectionInternal, calling handleCreateConnectionComplete %s", objArr);
        PhoneAccountHandle handle = connection.getAccountHandle();
        if (handle == null) {
            handle = request.getAccountHandle();
        }
        this.mAdapter.handleCreateConnectionComplete(callId, request, new ParcelableConnection(handle, connection.getState(), connection.getConnectionCapabilities(), connection.getConnectionProperties(), connection.getAddress(), connection.getAddressPresentation(), connection.getCallerDisplayName(), connection.getCallerDisplayNamePresentation(), connection.getVideoProvider() == null ? null : connection.getVideoProvider().getInterface(), connection.getVideoState(), connection.isRingbackRequested(), connection.getAudioModeIsVoip(), connection.getConnectTimeMillis(), connection.getStatusHints(), connection.getDisconnectCause(), createIdList(connection.getConferenceables()), connection.getExtras()));
    }

    private void abort(String callId) {
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "abort %s", objArr);
        findConnectionForAction(callId, "abort").onAbort();
    }

    private void answerVideo(String callId, int videoState) {
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "answerVideo %s", objArr);
        findConnectionForAction(callId, TelecomManagerEx.OPERATION_ANSWER_CALL).onAnswer(videoState);
    }

    private void answer(String callId) {
        logDebugMsgWithOpFormat("CC", "Answer", callId, null);
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "answer %s", objArr);
        findConnectionForAction(callId, TelecomManagerEx.OPERATION_ANSWER_CALL).onAnswer();
    }

    private void reject(String callId) {
        logDebugMsgWithOpFormat("CC", "Reject", callId, null);
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "reject %s", objArr);
        findConnectionForAction(callId, TelecomManagerEx.OPERATION_REJECT_CALL).onReject();
    }

    private void reject(String callId, String rejectWithMessage) {
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "reject %s with message", objArr);
        findConnectionForAction(callId, TelecomManagerEx.OPERATION_REJECT_CALL).onReject(rejectWithMessage);
    }

    private void silence(String callId) {
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "silence %s", objArr);
        findConnectionForAction(callId, "silence").onSilence();
    }

    private void disconnect(String callId) {
        if (!this.mConnectionById.containsKey(callId) || this.mConnectionById.get(callId) == null || ((Connection) this.mConnectionById.get(callId)).getConference() == null) {
            logDebugMsgWithOpFormat("CC", "Hangup", callId, null);
        } else {
            logDebugMsgWithOpFormat("CC", "RemoveMember", callId, null);
        }
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "disconnect %s", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, TelecomManagerEx.OPERATION_DISCONNECT_CALL).onDisconnect();
        } else {
            findConferenceForAction(callId, TelecomManagerEx.OPERATION_DISCONNECT_CALL).onDisconnect();
        }
    }

    private void hold(String callId) {
        logDebugMsgWithOpFormat("CC", "Hold", callId, null);
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "hold %s", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, TelecomManagerEx.OPERATION_HOLD_CALL).onHold();
        } else {
            findConferenceForAction(callId, TelecomManagerEx.OPERATION_HOLD_CALL).onHold();
        }
    }

    private void unhold(String callId) {
        logDebugMsgWithOpFormat("CC", "Unhold", callId, null);
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "unhold %s", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, TelecomManagerEx.OPERATION_UNHOLD_CALL).onUnhold();
        } else {
            findConferenceForAction(callId, TelecomManagerEx.OPERATION_UNHOLD_CALL).onUnhold();
        }
    }

    private void onCallAudioStateChanged(String callId, CallAudioState callAudioState) {
        Object[] objArr = new Object[2];
        objArr[0] = callId;
        objArr[1] = callAudioState;
        Log.d((Object) this, "onAudioStateChanged %s %s", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "onCallAudioStateChanged").setCallAudioState(callAudioState);
        } else {
            findConferenceForAction(callId, "onCallAudioStateChanged").setCallAudioState(callAudioState);
        }
    }

    private void playDtmfTone(String callId, char digit) {
        Object[] objArr = new Object[2];
        objArr[0] = callId;
        objArr[1] = Character.valueOf(digit);
        Log.d((Object) this, "playDtmfTone %s %c", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "playDtmfTone").onPlayDtmfTone(digit);
        } else {
            findConferenceForAction(callId, "playDtmfTone").onPlayDtmfTone(digit);
        }
    }

    private void stopDtmfTone(String callId) {
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "stopDtmfTone %s", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "stopDtmfTone").onStopDtmfTone();
        } else {
            findConferenceForAction(callId, "stopDtmfTone").onStopDtmfTone();
        }
    }

    private void conference(String callId1, String callId2) {
        logDebugMsgWithOpFormat("CC", "Conference", callId1, null);
        Object[] objArr = new Object[2];
        objArr[0] = callId1;
        objArr[1] = callId2;
        Log.d((Object) this, "conference %s, %s", objArr);
        Connection connection2 = findConnectionForAction(callId2, ImsCallProfile.EXTRA_CONFERENCE);
        Conference conference2 = getNullConference();
        if (connection2 == getNullConnection()) {
            conference2 = findConferenceForAction(callId2, ImsCallProfile.EXTRA_CONFERENCE);
            if (conference2 == getNullConference()) {
                objArr = new Object[1];
                objArr[0] = callId2;
                Log.w((Object) this, "Connection2 or Conference2 missing in conference request %s.", objArr);
                return;
            }
        }
        Connection connection1 = findConnectionForAction(callId1, ImsCallProfile.EXTRA_CONFERENCE);
        if (connection1 == getNullConnection()) {
            Conference conference1 = findConferenceForAction(callId1, "addConnection");
            if (conference1 == getNullConference()) {
                objArr = new Object[1];
                objArr[0] = callId1;
                Log.w((Object) this, "Connection1 or Conference1 missing in conference request %s.", objArr);
            } else if (connection2 != getNullConnection()) {
                conference1.onMerge(connection2);
            } else {
                Log.wtf((Object) this, "There can only be one conference and an attempt was made to merge two conferences.", new Object[0]);
            }
        } else if (conference2 != getNullConference()) {
            conference2.onMerge(connection1);
        } else {
            onConference(connection1, connection2);
        }
    }

    private void splitFromConference(String callId) {
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "splitFromConference(%s)", objArr);
        Connection connection = findConnectionForAction(callId, "splitFromConference");
        if (connection == getNullConnection()) {
            objArr = new Object[1];
            objArr[0] = callId;
            Log.w((Object) this, "Connection missing in conference request %s.", objArr);
            return;
        }
        Conference conference = connection.getConference();
        if (conference != null) {
            conference.onSeparate(connection);
        }
    }

    private void mergeConference(String callId) {
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "mergeConference(%s)", objArr);
        Conference conference = findConferenceForAction(callId, "mergeConference");
        if (conference != null) {
            conference.onMerge();
        }
    }

    private void swapConference(String callId) {
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "swapConference(%s)", objArr);
        Conference conference = findConferenceForAction(callId, "swapConference");
        if (conference != null) {
            conference.onSwap();
        }
    }

    private void pullExternalCall(String callId) {
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "pullExternalCall(%s)", objArr);
        Connection connection = findConnectionForAction(callId, "pullExternalCall");
        if (connection != null) {
            connection.onPullExternalCall();
        }
    }

    private void sendCallEvent(String callId, String event, Bundle extras) {
        Object[] objArr = new Object[2];
        objArr[0] = callId;
        objArr[1] = event;
        Log.d((Object) this, "sendCallEvent(%s, %s)", objArr);
        Connection connection = findConnectionForAction(callId, "sendCallEvent");
        if (connection != null) {
            connection.onCallEvent(event, extras);
        }
    }

    private void handleExtrasChanged(String callId, Bundle extras) {
        Object[] objArr = new Object[2];
        objArr[0] = callId;
        objArr[1] = extras;
        Log.d((Object) this, "handleExtrasChanged(%s, %s)", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "handleExtrasChanged").handleExtrasChanged(extras);
        } else if (this.mConferenceById.containsKey(callId)) {
            findConferenceForAction(callId, "handleExtrasChanged").handleExtrasChanged(extras);
        }
    }

    private void onPostDialContinue(String callId, boolean proceed) {
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "onPostDialContinue(%s)", objArr);
        findConnectionForAction(callId, "stopDtmfTone").onPostDialContinue(proceed);
    }

    private void explicitCallTransfer(String callId) {
        Object[] objArr;
        if (canTransfer((Connection) this.mConnectionById.get(callId))) {
            objArr = new Object[1];
            objArr[0] = callId;
            Log.d((Object) this, "explicitCallTransfer %s", objArr);
            findConnectionForAction(callId, "explicitCallTransfer").onExplicitCallTransfer();
            return;
        }
        objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "explicitCallTransfer %s fail", objArr);
    }

    private void explicitCallTransfer(String callId, String number, int type) {
        Object[] objArr;
        if (canBlindAssuredTransfer((Connection) this.mConnectionById.get(callId))) {
            objArr = new Object[3];
            objArr[0] = callId;
            objArr[1] = Rlog.pii(SDBG, (Object) number);
            objArr[2] = Integer.valueOf(type);
            Log.d((Object) this, "explicitCallTransfer %s %s %d", objArr);
            findConnectionForAction(callId, "explicitCallTransfer").onExplicitCallTransfer(number, type);
            return;
        }
        objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "explicitCallTransfer %s fail", objArr);
    }

    private void hangupAll(String callId) {
        Object[] objArr = new Object[1];
        objArr[0] = callId;
        Log.d((Object) this, "hangupAll %s", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "hangupAll").onHangupAll();
        } else {
            findConferenceForAction(callId, "hangupAll").onHangupAll();
        }
    }

    private void inviteConferenceParticipants(String conferenceCallId, List<String> numbers) {
        StringBuilder sb = new StringBuilder();
        for (String number : numbers) {
            sb.append(number);
        }
        logDebugMsgWithOpFormat("CC", "AddMember", conferenceCallId, " numbers=" + sb.toString());
        Object[] objArr = new Object[1];
        objArr[0] = conferenceCallId;
        Log.d((Object) this, "inviteConferenceParticipants %s", objArr);
        if (this.mConferenceById.containsKey(conferenceCallId)) {
            findConferenceForAction(conferenceCallId, "inviteConferenceParticipants").onInviteConferenceParticipants(numbers);
        }
    }

    private void createConference(PhoneAccountHandle callManagerAccount, String conferenceCallId, ConnectionRequest request, List<String> numbers, boolean isIncoming) {
        Object[] objArr = new Object[5];
        objArr[0] = callManagerAccount;
        objArr[1] = conferenceCallId;
        objArr[2] = request;
        objArr[3] = numbers;
        objArr[4] = Boolean.valueOf(isIncoming);
        Log.d((Object) this, "createConference, callManagerAccount: %s, conferenceCallId: %s, request: %s, numbers: %s, isIncoming: %b", objArr);
        if (!isIncoming) {
            StringBuilder sb = new StringBuilder();
            for (String number : numbers) {
                sb.append(Rlog.pii(SDBG, (Object) number));
            }
            FormattedLog formattedLog = new Builder().setCategory("CC").setServiceName(getConnectionServiceName()).setOpType(OpType.OPERATION).setActionName("DialConf").setCallNumber("conferenceCall").setCallId(PhoneConstants.MVNO_TYPE_NONE).setExtraMessage("numbers=" + sb.toString()).buildDebugMsg();
            if (formattedLog != null && (!SENLOG || TELDBG)) {
                Log.d((Object) this, formattedLog.toString(), new Object[0]);
            }
        }
        Conference conference = onCreateConference(callManagerAccount, conferenceCallId, request, numbers, isIncoming);
        if (conference == null) {
            Log.d((Object) this, "Fail to create conference!", new Object[0]);
            conference = getNullConference();
        } else if (conference.getState() != 6) {
            if (this.mIdByConference.containsKey(conference)) {
                objArr = new Object[1];
                objArr[0] = conference;
                Log.d((Object) this, "Re-adding an existing conference: %s.", objArr);
            } else {
                this.mConferenceById.put(conferenceCallId, conference);
                this.mIdByConference.put(conference, conferenceCallId);
                conference.addListener(this.mConferenceListener);
            }
        }
        this.mAdapter.handleCreateConferenceComplete(conferenceCallId, request, new ParcelableConference(conference.getPhoneAccountHandle(), conference.getState(), conference.getConnectionCapabilities(), conference.getConnectionProperties(), null, conference.getVideoProvider() == null ? null : conference.getVideoProvider().getInterface(), conference.getVideoState(), conference.getConnectTimeMillis(), conference.getStatusHints(), conference.getExtras(), conference.getDisconnectCause()));
    }

    protected Conference onCreateConference(PhoneAccountHandle callManagerAccount, String conferenceCallId, ConnectionRequest request, List<String> list, boolean isIncoming) {
        return null;
    }

    protected void replaceConference(Conference oldConf, Conference newConf) {
        Object[] objArr = new Object[2];
        objArr[0] = oldConf;
        objArr[1] = newConf;
        Log.d((Object) this, "SRVCC: oldConf= %s , newConf= %s", objArr);
        if (oldConf != newConf && this.mIdByConference.containsKey(oldConf)) {
            Log.d((Object) this, "SRVCC: start to do replacement", new Object[0]);
            oldConf.removeListener(this.mConferenceListener);
            String id = (String) this.mIdByConference.get(oldConf);
            this.mConferenceById.remove(id);
            this.mIdByConference.remove(oldConf);
            this.mConferenceById.put(id, newConf);
            this.mIdByConference.put(newConf, id);
            newConf.addListener(this.mConferenceListener);
            this.mConferenceListener.onConnectionCapabilitiesChanged(newConf, newConf.getConnectionCapabilities());
        }
    }

    private void disconnect(String callId, String pendingOperation) {
        Object[] objArr = new Object[2];
        objArr[0] = callId;
        objArr[1] = pendingOperation;
        Log.d((Object) this, "disconnect %s, pending call action %s", objArr);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, TelecomManagerEx.OPERATION_DISCONNECT_CALL).onDisconnect();
        } else {
            findConferenceForAction(callId, TelecomManagerEx.OPERATION_DISCONNECT_CALL).onDisconnect(pendingOperation);
        }
    }

    private void onAdapterAttached() {
        if (!this.mAreAccountsInitialized) {
            this.mAdapter.queryRemoteConnectionServices(new AnonymousClass5(this));
        }
    }

    public final RemoteConnection createRemoteIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return this.mRemoteConnectionManager.createRemoteConnection(connectionManagerPhoneAccount, request, true);
    }

    public final RemoteConnection createRemoteOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return this.mRemoteConnectionManager.createRemoteConnection(connectionManagerPhoneAccount, request, false);
    }

    public final void conferenceRemoteConnections(RemoteConnection remoteConnection1, RemoteConnection remoteConnection2) {
        this.mRemoteConnectionManager.conferenceRemoteConnections(remoteConnection1, remoteConnection2);
    }

    public final void addConference(Conference conference) {
        Object[] objArr = new Object[1];
        objArr[0] = conference;
        Log.d((Object) this, "addConference: conference=%s", objArr);
        boolean showHostNumber = showConfHostNumberToParticipant(this);
        Uri hostAddress = conference.getHostAddress();
        String id = addConferenceInternal(conference);
        if (id != null) {
            IVideoProvider iVideoProvider;
            List<String> connectionIds = new ArrayList(2);
            for (Connection connection : conference.getConnections()) {
                if (this.mIdByConnection.containsKey(connection)) {
                    connectionIds.add((String) this.mIdByConnection.get(connection));
                }
            }
            conference.setTelecomCallId(id);
            PhoneAccountHandle phoneAccountHandle = conference.getPhoneAccountHandle();
            int state = conference.getState();
            int connectionCapabilities = conference.getConnectionCapabilities();
            int connectionProperties = conference.getConnectionProperties();
            if (conference.getVideoProvider() == null) {
                iVideoProvider = null;
            } else {
                iVideoProvider = conference.getVideoProvider().getInterface();
            }
            this.mAdapter.addConferenceCall(id, new ParcelableConference(phoneAccountHandle, state, connectionCapabilities, connectionProperties, connectionIds, iVideoProvider, conference.getVideoState(), conference.getConnectTimeMillis(), conference.getStatusHints(), conference.getExtras()));
            if (showHostNumber) {
                Log.d((Object) this, "set host address", new Object[0]);
                this.mAdapter.setAddress(id, hostAddress, 1);
            }
            this.mAdapter.setVideoProvider(id, conference.getVideoProvider());
            this.mAdapter.setVideoState(id, conference.getVideoState());
            for (Connection connection2 : conference.getConnections()) {
                String connectionId = (String) this.mIdByConnection.get(connection2);
                if (connectionId != null) {
                    this.mAdapter.setIsConferenced(connectionId, id);
                }
            }
        }
    }

    public final void addExistingConnection(PhoneAccountHandle phoneAccountHandle, Connection connection) {
        String id = addExistingConnectionInternal(phoneAccountHandle, connection);
        if (id != null) {
            this.mAdapter.addExistingConnection(id, new ParcelableConnection(phoneAccountHandle, connection.getState(), connection.getConnectionCapabilities(), connection.getConnectionProperties(), connection.getAddress(), connection.getAddressPresentation(), connection.getCallerDisplayName(), connection.getCallerDisplayNamePresentation(), connection.getVideoProvider() == null ? null : connection.getVideoProvider().getInterface(), connection.getVideoState(), connection.isRingbackRequested(), connection.getAudioModeIsVoip(), connection.getConnectTimeMillis(), connection.getStatusHints(), connection.getDisconnectCause(), new ArrayList(0), connection.getExtras()));
        }
    }

    public final Collection<Connection> getAllConnections() {
        return this.mConnectionById.values();
    }

    public final Collection<Conference> getAllConferences() {
        return this.mConferenceById.values();
    }

    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return null;
    }

    public void triggerConferenceRecalculate() {
    }

    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return null;
    }

    public Connection onCreateUnknownConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return null;
    }

    public void onConference(Connection connection1, Connection connection2) {
    }

    public void onRemoteConferenceAdded(RemoteConference conference) {
    }

    public void onRemoteExistingConnectionAdded(RemoteConnection connection) {
    }

    public boolean containsConference(Conference conference) {
        return this.mIdByConference.containsKey(conference);
    }

    void addRemoteConference(RemoteConference remoteConference) {
        onRemoteConferenceAdded(remoteConference);
    }

    void addRemoteExistingConnection(RemoteConnection remoteConnection) {
        onRemoteExistingConnectionAdded(remoteConnection);
    }

    private void onAccountsInitialized() {
        this.mAreAccountsInitialized = true;
        for (Runnable r : this.mPreInitializationConnectionRequests) {
            r.run();
        }
        this.mPreInitializationConnectionRequests.clear();
    }

    private String addExistingConnectionInternal(PhoneAccountHandle handle, Connection connection) {
        String id;
        if (connection.getExtras() != null && connection.getExtras().containsKey(Connection.EXTRA_ORIGINAL_CONNECTION_ID)) {
            id = connection.getExtras().getString(Connection.EXTRA_ORIGINAL_CONNECTION_ID);
            Object[] objArr = new Object[2];
            objArr[0] = connection.getTelecomCallId();
            objArr[1] = id;
            Log.d((Object) this, "addExistingConnectionInternal - conn %s reusing original id %s", objArr);
        } else if (handle == null) {
            id = UUID.randomUUID().toString();
        } else {
            id = handle.getComponentName().getClassName() + "@" + getNextCallId();
        }
        addConnection(id, connection);
        return id;
    }

    private void addConnection(String callId, Connection connection) {
        connection.setTelecomCallId(callId);
        this.mConnectionById.put(callId, connection);
        this.mIdByConnection.put(connection, callId);
        connection.addConnectionListener(this.mConnectionListener);
        connection.setConnectionService(this);
        connection.fireOnCallState();
    }

    protected void removeConnection(Connection connection) {
        connection.unsetConnectionService(this);
        connection.removeConnectionListener(this.mConnectionListener);
        String id = (String) this.mIdByConnection.get(connection);
        if (id != null) {
            this.mConnectionById.remove(id);
            this.mIdByConnection.remove(connection);
            this.mAdapter.removeCall(id);
        }
    }

    protected String removeConnectionInternal(Connection connection) {
        String id = (String) this.mIdByConnection.get(connection);
        connection.unsetConnectionService(this);
        connection.removeConnectionListener(this.mConnectionListener);
        this.mConnectionById.remove(this.mIdByConnection.get(connection));
        this.mIdByConnection.remove(connection);
        Object[] objArr = new Object[2];
        objArr[0] = id;
        objArr[1] = connection;
        Log.d((Object) this, "removeConnectionInternal, callId: %s, connection: %s", objArr);
        return id;
    }

    private String addConferenceInternal(Conference conference) {
        Object[] objArr;
        String originalId = null;
        if (conference.getExtras() != null && conference.getExtras().containsKey(Connection.EXTRA_ORIGINAL_CONNECTION_ID)) {
            originalId = conference.getExtras().getString(Connection.EXTRA_ORIGINAL_CONNECTION_ID);
            objArr = new Object[2];
            objArr[0] = conference.getTelecomCallId();
            objArr[1] = originalId;
            Log.d((Object) this, "addConferenceInternal: conf %s reusing original id %s", objArr);
        }
        if (this.mIdByConference.containsKey(conference)) {
            objArr = new Object[1];
            objArr[0] = conference;
            Log.w((Object) this, "Re-adding an existing conference: %s.", objArr);
        } else if (conference != null) {
            String id = originalId == null ? UUID.randomUUID().toString() : originalId;
            this.mConferenceById.put(id, conference);
            this.mIdByConference.put(conference, id);
            conference.addListener(this.mConferenceListener);
            return id;
        }
        return null;
    }

    private void removeConference(Conference conference) {
        if (this.mIdByConference.containsKey(conference)) {
            conference.removeListener(this.mConferenceListener);
            String id = (String) this.mIdByConference.get(conference);
            this.mConferenceById.remove(id);
            this.mIdByConference.remove(conference);
            this.mAdapter.removeCall(id);
        }
    }

    private Connection findConnectionForAction(String callId, String action) {
        if (this.mConnectionById.containsKey(callId)) {
            return (Connection) this.mConnectionById.get(callId);
        }
        notFindConnectionThroughCallId(false, callId, action);
        Object[] objArr = new Object[2];
        objArr[0] = action;
        objArr[1] = callId;
        Log.w((Object) this, "%s - Cannot find Connection %s", objArr);
        return getNullConnection();
    }

    static synchronized Connection getNullConnection() {
        Connection connection;
        synchronized (ConnectionService.class) {
            if (sNullConnection == null) {
                sNullConnection = new AnonymousClass6();
            }
            connection = sNullConnection;
        }
        return connection;
    }

    private Conference findConferenceForAction(String conferenceId, String action) {
        if (this.mConferenceById.containsKey(conferenceId)) {
            return (Conference) this.mConferenceById.get(conferenceId);
        }
        notFindConnectionThroughCallId(true, conferenceId, action);
        Object[] objArr = new Object[2];
        objArr[0] = action;
        objArr[1] = conferenceId;
        Log.w((Object) this, "%s - Cannot find conference %s", objArr);
        return getNullConference();
    }

    private List<String> createConnectionIdList(List<Connection> connections) {
        List<String> ids = new ArrayList();
        for (Connection c : connections) {
            if (this.mIdByConnection.containsKey(c)) {
                ids.add((String) this.mIdByConnection.get(c));
            }
        }
        Collections.sort(ids);
        return ids;
    }

    private List<String> createIdList(List<Conferenceable> conferenceables) {
        List<String> ids = new ArrayList();
        for (Conferenceable c : conferenceables) {
            if (c instanceof Connection) {
                Connection connection = (Connection) c;
                if (this.mIdByConnection.containsKey(connection)) {
                    ids.add((String) this.mIdByConnection.get(connection));
                }
            } else if (c instanceof Conference) {
                Conference conference = (Conference) c;
                if (this.mIdByConference.containsKey(conference)) {
                    ids.add((String) this.mIdByConference.get(conference));
                }
            }
        }
        Collections.sort(ids);
        return ids;
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private android.telecom.Conference getNullConference() {
        /*
        r2 = this;
        r1 = 0;
        r0 = r2.sNullConference;
        if (r0 != 0) goto L_0x000c;
    L_0x0005:
        r0 = new android.telecom.ConnectionService$7;
        r0.<init>(r2, r1);
        r2.sNullConference = r0;
    L_0x000c:
        r0 = r2.sNullConference;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.ConnectionService.getNullConference():android.telecom.Conference");
    }

    private void endAllConnections() {
        for (Connection connection : this.mIdByConnection.keySet()) {
            if (connection.getConference() == null) {
                connection.onDisconnect();
            }
        }
        for (Conference conference : this.mIdByConference.keySet()) {
            conference.onDisconnect();
        }
    }

    private int getNextCallId() {
        int i;
        synchronized (this.mIdSyncRoot) {
            i = this.mId + 1;
            this.mId = i;
        }
        return i;
    }

    public boolean canDial(PhoneAccountHandle accountHandle, String dialString) {
        return true;
    }

    public boolean canTransfer(Connection bgConnection) {
        return false;
    }

    public boolean canBlindAssuredTransfer(Connection bgConnection) {
        return false;
    }

    protected void forceSuppMessageUpdate(Connection conn) {
    }

    protected void logDebugMsgWithOpFormat(String category, String action, String callId, String msg) {
        if (category != null && action != null && callId != null) {
            if (msg == null) {
                msg = PhoneConstants.MVNO_TYPE_NONE;
            }
            Object callNumber = "null";
            String localCallId = "null";
            if (this.mConnectionById.containsKey(callId)) {
                Connection conn = (Connection) this.mConnectionById.get(callId);
                if (!(conn == null || conn.getAddress() == null)) {
                    callNumber = conn.getAddress().getSchemeSpecificPart();
                }
                localCallId = Integer.toString(System.identityHashCode(conn));
            } else if (this.mConferenceById.containsKey(callId)) {
                callNumber = "conferenceCall";
                localCallId = Integer.toString(System.identityHashCode(this.mConferenceById.get(callId)));
            }
            FormattedLog formattedLog = new Builder().setCategory(category).setServiceName(getConnectionServiceName()).setOpType(OpType.OPERATION).setActionName(action).setCallNumber(Rlog.pii(SDBG, callNumber)).setCallId(localCallId).setExtraMessage(msg).buildDebugMsg();
            if (formattedLog != null) {
                Log.d((Object) this, formattedLog.toString(), new Object[0]);
            }
        }
    }

    private String getConnectionServiceName() {
        String className = getClass().getSimpleName();
        int index = className.indexOf("ConnectionService");
        if (index != -1) {
            return className.substring(0, index);
        }
        return className;
    }

    protected boolean showConfHostNumberToParticipant(Context context) {
        Log.d((Object) this, "showConfHostNumberToParticipant", new Object[0]);
        boolean showHostNumber = false;
        int subId = SubscriptionManager.getSubIdUsingPhoneId(SystemProperties.getInt(PhoneConstants.PROPERTY_CAPABILITY_SWITCH, 1) - 1);
        CarrierConfigManager configMgr = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (configMgr != null) {
            PersistableBundle b = configMgr.getConfigForSubId(subId);
            if (b != null) {
                showHostNumber = b.getBoolean(CarrierConfigManager.KEY_SHOW_CONF_HOST_NUMBER_TO_PARTICIPANT);
            }
        }
        Log.d((Object) this, "showHostNumber: %s" + showHostNumber, new Object[0]);
        return showHostNumber;
    }

    void logDebug(String logString) {
        Log.i("IConnectionService-->", logString, new Object[0]);
    }

    protected void notFindConnectionThroughCallId(boolean isConference, String callId, String action) {
    }
}

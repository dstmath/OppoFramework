package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.telecom.VideoProfile.CameraCapabilities;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.ArraySet;
import android.view.Surface;
import com.android.ims.ImsConferenceState;
import com.android.internal.telecom.IVideoCallback;
import com.android.internal.telecom.IVideoProvider;
import com.android.internal.telecom.IVideoProvider.Stub;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.telecom.FormattedLog;
import com.mediatek.telecom.FormattedLog.Builder;
import com.mediatek.telecom.FormattedLog.OpType;
import com.mediatek.telecom.TelecomManagerEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
public abstract class Connection extends Conferenceable {
    public static final int CAPABILITY_BLIND_ASSURED_ECT = 536870912;
    public static final int CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO = 8388608;
    public static final int CAPABILITY_CAN_PAUSE_VIDEO = 1048576;
    public static final int CAPABILITY_CAN_PULL_CALL = 16777216;
    public static final int CAPABILITY_CAN_SEND_RESPONSE_VIA_CONNECTION = 4194304;
    public static final int CAPABILITY_CAN_UPGRADE_TO_VIDEO = 524288;
    public static final int CAPABILITY_CONFERENCE_HAS_NO_CHILDREN = 2097152;
    public static final int CAPABILITY_DISCONNECT_FROM_CONFERENCE = 8192;
    public static final int CAPABILITY_ECT = 67108864;
    public static final int CAPABILITY_HOLD = 1;
    public static final int CAPABILITY_INVITE_PARTICIPANTS = 268435456;
    public static final int CAPABILITY_MANAGE_CONFERENCE = 128;
    public static final int CAPABILITY_MERGE_CONFERENCE = 4;
    public static final int CAPABILITY_MUTE = 64;
    public static final int CAPABILITY_RESPOND_VIA_TEXT = 32;
    public static final int CAPABILITY_SEPARATE_FROM_CONFERENCE = 4096;
    public static final int CAPABILITY_SPEED_UP_MT_AUDIO = 262144;
    public static final int CAPABILITY_SUPPORTS_VT_LOCAL_BIDIRECTIONAL = 768;
    public static final int CAPABILITY_SUPPORTS_VT_LOCAL_RX = 256;
    public static final int CAPABILITY_SUPPORTS_VT_LOCAL_TX = 512;
    public static final int CAPABILITY_SUPPORTS_VT_REMOTE_BIDIRECTIONAL = 3072;
    public static final int CAPABILITY_SUPPORTS_VT_REMOTE_RX = 1024;
    public static final int CAPABILITY_SUPPORTS_VT_REMOTE_TX = 2048;
    public static final int CAPABILITY_SUPPORT_HOLD = 2;
    public static final int CAPABILITY_SWAP_CONFERENCE = 8;
    public static final int CAPABILITY_UNUSED = 16;
    public static final int CAPABILITY_UNUSED_2 = 16384;
    public static final int CAPABILITY_UNUSED_3 = 32768;
    public static final int CAPABILITY_UNUSED_4 = 65536;
    public static final int CAPABILITY_UNUSED_5 = 131072;
    public static final int CAPABILITY_VIDEO_RINGTONE = 1073741824;
    public static final int CAPABILITY_VOICE_RECORD = 33554432;
    public static final int CAPABILITY_VOLTE = 134217728;
    public static final String EVENT_CALL_MERGE_FAILED = "android.telecom.event.CALL_MERGE_FAILED";
    public static final String EVENT_CALL_PULL_FAILED = "android.telecom.event.CALL_PULL_FAILED";
    public static final String EVENT_CALL_REMOTELY_HELD = "android.telecom.event.CALL_REMOTELY_HELD";
    public static final String EVENT_CALL_REMOTELY_UNHELD = "android.telecom.event.CALL_REMOTELY_UNHELD";
    public static final String EVENT_ON_HOLD_TONE_END = "android.telecom.event.ON_HOLD_TONE_END";
    public static final String EVENT_ON_HOLD_TONE_START = "android.telecom.event.ON_HOLD_TONE_START";
    public static final String EXTRA_ANSWERING_DROPS_FG_CALL = "android.telecom.extra.ANSWERING_DROPS_FG_CALL";
    public static final String EXTRA_CALL_SUBJECT = "android.telecom.extra.CALL_SUBJECT";
    public static final String EXTRA_CHILD_ADDRESS = "android.telecom.extra.CHILD_ADDRESS";
    public static final String EXTRA_DISABLE_ADD_CALL = "android.telecom.extra.DISABLE_ADD_CALL";
    public static final String EXTRA_LAST_FORWARDED_NUMBER = "android.telecom.extra.LAST_FORWARDED_NUMBER";
    public static final String EXTRA_ORIGINAL_CONNECTION_ID = "android.telecom.extra.ORIGINAL_CONNECTION_ID";
    private static final boolean PII_DEBUG = false;
    private static final int PROPERTY_CUSTOMIZATION_BASE = 65536;
    public static final int PROPERTY_EMERGENCY_CALLBACK_MODE = 1;
    public static final int PROPERTY_GENERIC_CONFERENCE = 2;
    public static final int PROPERTY_HAS_CDMA_VOICE_PRIVACY = 32;
    public static final int PROPERTY_HIGH_DEF_AUDIO = 4;
    public static final int PROPERTY_IS_DOWNGRADED_CONFERENCE = 64;
    public static final int PROPERTY_IS_EXTERNAL_CALL = 16;
    public static final int PROPERTY_VOLTE = 65536;
    public static final int PROPERTY_WIFI = 8;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    private static final boolean SDBG = false;
    private static final boolean SENLOG = false;
    public static final int STATE_ACTIVE = 4;
    public static final int STATE_DIALING = 3;
    public static final int STATE_DISCONNECTED = 6;
    public static final int STATE_HOLDING = 5;
    public static final int STATE_INITIALIZING = 0;
    public static final int STATE_NEW = 1;
    public static final int STATE_PULLING_CALL = 7;
    public static final int STATE_RINGING = 2;
    private static final boolean TELDBG = false;
    private PhoneAccountHandle mAccountHandle;
    private Uri mAddress;
    private int mAddressPresentation;
    private boolean mAudioModeIsVoip;
    private CallAudioState mCallAudioState;
    private String mCallerDisplayName;
    private int mCallerDisplayNamePresentation;
    private Conference mConference;
    private final android.telecom.Conference.Listener mConferenceDeathListener;
    private final List<Conferenceable> mConferenceables;
    private long mConnectTimeMillis;
    private int mConnectionCapabilities;
    private final Listener mConnectionDeathListener;
    private int mConnectionProperties;
    private ConnectionService mConnectionService;
    private DisconnectCause mDisconnectCause;
    private Bundle mExtras;
    private final Object mExtrasLock;
    private final Set<Listener> mListeners;
    private Set<String> mPreviousExtraKeys;
    private boolean mRingbackRequested;
    private int mState;
    private StatusHints mStatusHints;
    private String mTelecomCallId;
    private final List<Conferenceable> mUnmodifiableConferenceables;
    private VideoProvider mVideoProvider;
    private int mVideoState;

    public static abstract class Listener {
        public void onStateChanged(Connection c, int state) {
        }

        public void onAddressChanged(Connection c, Uri newAddress, int presentation) {
        }

        public void onCallerDisplayNameChanged(Connection c, String callerDisplayName, int presentation) {
        }

        public void onVideoStateChanged(Connection c, int videoState) {
        }

        public void onDisconnected(Connection c, DisconnectCause disconnectCause) {
        }

        public void onPostDialWait(Connection c, String remaining) {
        }

        public void onPostDialChar(Connection c, char nextChar) {
        }

        public void onRingbackRequested(Connection c, boolean ringback) {
        }

        public void onDestroyed(Connection c) {
        }

        public void onConnectionCapabilitiesChanged(Connection c, int capabilities) {
        }

        public void onConnectionPropertiesChanged(Connection c, int properties) {
        }

        public void onVideoProviderChanged(Connection c, VideoProvider videoProvider) {
        }

        public void onAudioModeIsVoipChanged(Connection c, boolean isVoip) {
        }

        public void onStatusHintsChanged(Connection c, StatusHints statusHints) {
        }

        public void onConferenceablesChanged(Connection c, List<Conferenceable> list) {
        }

        public void onConferenceChanged(Connection c, Conference conference) {
        }

        public void onConferenceParticipantsChanged(Connection c, List<ConferenceParticipant> list) {
        }

        public void onConferenceStarted() {
        }

        public void onConferenceMergeFailed(Connection c) {
        }

        public void onExtrasChanged(Connection c, Bundle extras) {
        }

        public void onExtrasRemoved(Connection c, List<String> list) {
        }

        public void onConnectionEvent(Connection c, String event, Bundle extras) {
        }

        public void onConferenceSupportedChanged(Connection c, boolean isConferenceSupported) {
        }
    }

    private static class FailureSignalingConnection extends Connection {
        private boolean mImmutable;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.telecom.Connection.FailureSignalingConnection.<init>(android.telecom.DisconnectCause):void, dex:  in method: android.telecom.Connection.FailureSignalingConnection.<init>(android.telecom.DisconnectCause):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.telecom.Connection.FailureSignalingConnection.<init>(android.telecom.DisconnectCause):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public FailureSignalingConnection(android.telecom.DisconnectCause r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.telecom.Connection.FailureSignalingConnection.<init>(android.telecom.DisconnectCause):void, dex:  in method: android.telecom.Connection.FailureSignalingConnection.<init>(android.telecom.DisconnectCause):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.FailureSignalingConnection.<init>(android.telecom.DisconnectCause):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.telecom.Connection.FailureSignalingConnection.checkImmutable():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void checkImmutable() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.telecom.Connection.FailureSignalingConnection.checkImmutable():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.FailureSignalingConnection.checkImmutable():void");
        }
    }

    public static abstract class VideoProvider {
        private static final int MSG_ADD_VIDEO_CALLBACK = 1;
        private static final int MSG_MTK_BASE = 100;
        private static final int MSG_REMOVE_VIDEO_CALLBACK = 12;
        private static final int MSG_REQUEST_CAMERA_CAPABILITIES = 9;
        private static final int MSG_REQUEST_CONNECTION_DATA_USAGE = 10;
        private static final int MSG_SEND_SESSION_MODIFY_REQUEST = 7;
        private static final int MSG_SEND_SESSION_MODIFY_RESPONSE = 8;
        private static final int MSG_SET_CAMERA = 2;
        private static final int MSG_SET_DEVICE_ORIENTATION = 5;
        private static final int MSG_SET_DISPLAY_SURFACE = 4;
        private static final int MSG_SET_PAUSE_IMAGE = 11;
        private static final int MSG_SET_PREVIEW_SURFACE = 3;
        private static final int MSG_SET_UI_MODE = 100;
        private static final int MSG_SET_ZOOM = 6;
        public static final int SESSION_EVENT_CAMERA_FAILURE = 5;
        private static final String SESSION_EVENT_CAMERA_FAILURE_STR = "CAMERA_FAIL";
        public static final int SESSION_EVENT_CAMERA_READY = 6;
        private static final String SESSION_EVENT_CAMERA_READY_STR = "CAMERA_READY";
        public static final int SESSION_EVENT_ERROR_CAMERA_CRASHED = 8003;
        public static final int SESSION_EVENT_RX_PAUSE = 1;
        private static final String SESSION_EVENT_RX_PAUSE_STR = "RX_PAUSE";
        public static final int SESSION_EVENT_RX_RESUME = 2;
        private static final String SESSION_EVENT_RX_RESUME_STR = "RX_RESUME";
        public static final int SESSION_EVENT_TX_START = 3;
        private static final String SESSION_EVENT_TX_START_STR = "TX_START";
        public static final int SESSION_EVENT_TX_STOP = 4;
        private static final String SESSION_EVENT_TX_STOP_STR = "TX_STOP";
        private static final String SESSION_EVENT_UNKNOWN_STR = "UNKNOWN";
        public static final int SESSION_MODIFY_CANCEL_UPGRADE_FAIL = 200;
        public static final int SESSION_MODIFY_CANCEL_UPGRADE_FAIL_AUTO_DOWNGRADE = 201;
        public static final int SESSION_MODIFY_CANCEL_UPGRADE_FAIL_REMOTE_REJECT_UPGRADE = 202;
        private static final int SESSION_MODIFY_MTK_BASE = 200;
        public static final int SESSION_MODIFY_REQUEST_FAIL = 2;
        public static final int SESSION_MODIFY_REQUEST_INVALID = 3;
        public static final int SESSION_MODIFY_REQUEST_REJECTED_BY_REMOTE = 5;
        public static final int SESSION_MODIFY_REQUEST_SUCCESS = 1;
        public static final int SESSION_MODIFY_REQUEST_TIMED_OUT = 4;
        public static final int UI_MODE_BG = 1;
        public static final int UI_MODE_FG = 0;
        public static final int UI_MODE_FULL_SCREEN = 2;
        public static final int UI_MODE_NORMAL_SCREEN = 3;
        private final VideoProviderBinder mBinder;
        private VideoProviderHandler mMessageHandler;
        private ConcurrentHashMap<IBinder, IVideoCallback> mVideoCallbacks;

        private final class VideoProviderBinder extends Stub {
            final /* synthetic */ VideoProvider this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.<init>(android.telecom.Connection$VideoProvider):void, dex: 
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
            private VideoProviderBinder(android.telecom.Connection.VideoProvider r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.<init>(android.telecom.Connection$VideoProvider):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.<init>(android.telecom.Connection$VideoProvider):void");
            }

            /* synthetic */ VideoProviderBinder(VideoProvider this$1, VideoProviderBinder videoProviderBinder) {
                this(this$1);
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.addVideoCallback(android.os.IBinder):void, dex: 
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
            public void addVideoCallback(android.os.IBinder r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.addVideoCallback(android.os.IBinder):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.addVideoCallback(android.os.IBinder):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.removeVideoCallback(android.os.IBinder):void, dex: 
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
            public void removeVideoCallback(android.os.IBinder r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.removeVideoCallback(android.os.IBinder):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.removeVideoCallback(android.os.IBinder):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.requestCallDataUsage():void, dex: 
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
            public void requestCallDataUsage() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.requestCallDataUsage():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.requestCallDataUsage():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.requestCameraCapabilities():void, dex: 
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
            public void requestCameraCapabilities() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.requestCameraCapabilities():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.requestCameraCapabilities():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.sendSessionModifyRequest(android.telecom.VideoProfile, android.telecom.VideoProfile):void, dex: 
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
            public void sendSessionModifyRequest(android.telecom.VideoProfile r1, android.telecom.VideoProfile r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.sendSessionModifyRequest(android.telecom.VideoProfile, android.telecom.VideoProfile):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.sendSessionModifyRequest(android.telecom.VideoProfile, android.telecom.VideoProfile):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.sendSessionModifyResponse(android.telecom.VideoProfile):void, dex: 
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
            public void sendSessionModifyResponse(android.telecom.VideoProfile r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.sendSessionModifyResponse(android.telecom.VideoProfile):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.sendSessionModifyResponse(android.telecom.VideoProfile):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setCamera(java.lang.String):void, dex: 
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
            public void setCamera(java.lang.String r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setCamera(java.lang.String):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.setCamera(java.lang.String):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setDeviceOrientation(int):void, dex: 
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
            public void setDeviceOrientation(int r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setDeviceOrientation(int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.setDeviceOrientation(int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setDisplaySurface(android.view.Surface):void, dex: 
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
            public void setDisplaySurface(android.view.Surface r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setDisplaySurface(android.view.Surface):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.setDisplaySurface(android.view.Surface):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setPauseImage(android.net.Uri):void, dex: 
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
            public void setPauseImage(android.net.Uri r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setPauseImage(android.net.Uri):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.setPauseImage(android.net.Uri):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setPreviewSurface(android.view.Surface):void, dex: 
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
            public void setPreviewSurface(android.view.Surface r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setPreviewSurface(android.view.Surface):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.setPreviewSurface(android.view.Surface):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setUIMode(int):void, dex: 
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
            public void setUIMode(int r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setUIMode(int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.setUIMode(int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setZoom(float):void, dex: 
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
            public void setZoom(float r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.telecom.Connection.VideoProvider.VideoProviderBinder.setZoom(float):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderBinder.setZoom(float):void");
            }
        }

        private final class VideoProviderHandler extends Handler {
            final /* synthetic */ VideoProvider this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telecom.Connection.VideoProvider.VideoProviderHandler.<init>(android.telecom.Connection$VideoProvider):void, dex: 
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
            public VideoProviderHandler(android.telecom.Connection.VideoProvider r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telecom.Connection.VideoProvider.VideoProviderHandler.<init>(android.telecom.Connection$VideoProvider):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderHandler.<init>(android.telecom.Connection$VideoProvider):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.telecom.Connection.VideoProvider.VideoProviderHandler.<init>(android.telecom.Connection$VideoProvider, android.os.Looper):void, dex: 
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
            public VideoProviderHandler(android.telecom.Connection.VideoProvider r1, android.os.Looper r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.telecom.Connection.VideoProvider.VideoProviderHandler.<init>(android.telecom.Connection$VideoProvider, android.os.Looper):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderHandler.<init>(android.telecom.Connection$VideoProvider, android.os.Looper):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.telecom.Connection.VideoProvider.VideoProviderHandler.handleMessage(android.os.Message):void, dex:  in method: android.telecom.Connection.VideoProvider.VideoProviderHandler.handleMessage(android.os.Message):void, dex: 
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
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.telecom.Connection.VideoProvider.VideoProviderHandler.handleMessage(android.os.Message):void, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 11 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
                	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 12 more
                */
            public void handleMessage(android.os.Message r1) {
                /*
                // Can't load method instructions: Load method exception: null in method: android.telecom.Connection.VideoProvider.VideoProviderHandler.handleMessage(android.os.Message):void, dex:  in method: android.telecom.Connection.VideoProvider.VideoProviderHandler.handleMessage(android.os.Message):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.VideoProvider.VideoProviderHandler.handleMessage(android.os.Message):void");
            }
        }

        public abstract void onRequestCameraCapabilities();

        public abstract void onRequestConnectionDataUsage();

        public abstract void onSendSessionModifyRequest(VideoProfile videoProfile, VideoProfile videoProfile2);

        public abstract void onSendSessionModifyResponse(VideoProfile videoProfile);

        public abstract void onSetCamera(String str);

        public abstract void onSetDeviceOrientation(int i);

        public abstract void onSetDisplaySurface(Surface surface);

        public abstract void onSetPauseImage(Uri uri);

        public abstract void onSetPreviewSurface(Surface surface);

        public abstract void onSetZoom(float f);

        public VideoProvider() {
            this.mVideoCallbacks = new ConcurrentHashMap(8, 0.9f, 1);
            this.mBinder = new VideoProviderBinder(this, null);
            this.mMessageHandler = new VideoProviderHandler(this, Looper.getMainLooper());
        }

        public VideoProvider(Looper looper) {
            this.mVideoCallbacks = new ConcurrentHashMap(8, 0.9f, 1);
            this.mBinder = new VideoProviderBinder(this, null);
            this.mMessageHandler = new VideoProviderHandler(this, looper);
        }

        public final IVideoProvider getInterface() {
            return this.mBinder;
        }

        public void onSetUIMode(int mode) {
        }

        public void receiveSessionModifyRequest(VideoProfile videoProfile) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.receiveSessionModifyRequest(videoProfile);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[1];
                        objArr[0] = ignored;
                        Log.w((Object) this, "receiveSessionModifyRequest callback failed", objArr);
                    }
                }
            }
        }

        public void receiveSessionModifyResponse(int status, VideoProfile requestedProfile, VideoProfile responseProfile) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.receiveSessionModifyResponse(status, requestedProfile, responseProfile);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[1];
                        objArr[0] = ignored;
                        Log.w((Object) this, "receiveSessionModifyResponse callback failed", objArr);
                    }
                }
            }
        }

        public void handleCallSessionEvent(int event) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.handleCallSessionEvent(event);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[1];
                        objArr[0] = ignored;
                        Log.w((Object) this, "handleCallSessionEvent callback failed", objArr);
                    }
                }
            }
        }

        public void changePeerDimensions(int width, int height) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.changePeerDimensions(width, height);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[1];
                        objArr[0] = ignored;
                        Log.w((Object) this, "changePeerDimensions callback failed", objArr);
                    }
                }
            }
        }

        public void changePeerDimensionsWithAngle(int width, int height, int rotation) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.changePeerDimensionsWithAngle(width, height, rotation);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[1];
                        objArr[0] = ignored;
                        Log.w((Object) this, "changePeerDimensionsWithAngle callback failed", objArr);
                    }
                }
            }
        }

        public void setCallDataUsage(long dataUsage) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.changeCallDataUsage(dataUsage);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[1];
                        objArr[0] = ignored;
                        Log.w((Object) this, "setCallDataUsage callback failed", objArr);
                    }
                }
            }
        }

        public void changeCallDataUsage(long dataUsage) {
            setCallDataUsage(dataUsage);
        }

        public void changeCameraCapabilities(CameraCapabilities cameraCapabilities) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        Log.w((Object) this, "changeCameraCapabilities callback=:" + callback, new Object[0]);
                        callback.changeCameraCapabilities(cameraCapabilities);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[1];
                        objArr[0] = ignored;
                        Log.w((Object) this, "changeCameraCapabilities callback failed", objArr);
                    }
                }
            }
        }

        public void changeVideoQuality(int videoQuality) {
            if (this.mVideoCallbacks != null) {
                for (IVideoCallback callback : this.mVideoCallbacks.values()) {
                    try {
                        callback.changeVideoQuality(videoQuality);
                    } catch (RemoteException ignored) {
                        Object[] objArr = new Object[1];
                        objArr[0] = ignored;
                        Log.w((Object) this, "changeVideoQuality callback failed", objArr);
                    }
                }
            }
        }

        public static String sessionEventToString(int event) {
            switch (event) {
                case 1:
                    return SESSION_EVENT_RX_PAUSE_STR;
                case 2:
                    return SESSION_EVENT_RX_RESUME_STR;
                case 3:
                    return SESSION_EVENT_TX_START_STR;
                case 4:
                    return SESSION_EVENT_TX_STOP_STR;
                case 5:
                    return SESSION_EVENT_CAMERA_FAILURE_STR;
                case 6:
                    return SESSION_EVENT_CAMERA_READY_STR;
                default:
                    return "UNKNOWN " + event;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telecom.Connection.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telecom.Connection.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.Connection.<clinit>():void");
    }

    public static boolean can(int capabilities, int capability) {
        return (capabilities & capability) == capability;
    }

    public boolean can(int capability) {
        return can(this.mConnectionCapabilities, capability);
    }

    public void removeCapability(int capability) {
        this.mConnectionCapabilities &= ~capability;
    }

    public void addCapability(int capability) {
        this.mConnectionCapabilities |= capability;
    }

    public static String capabilitiesToString(int capabilities) {
        return capabilitiesToStringInternal(capabilities, true);
    }

    public static String capabilitiesToStringShort(int capabilities) {
        return capabilitiesToStringInternal(capabilities, false);
    }

    private static String capabilitiesToStringInternal(int capabilities, boolean isLong) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (isLong) {
            builder.append("Capabilities:");
        }
        if (can(capabilities, 1)) {
            builder.append(isLong ? " CAPABILITY_HOLD" : " hld");
        }
        if (can(capabilities, 2)) {
            builder.append(isLong ? " CAPABILITY_SUPPORT_HOLD" : " sup_hld");
        }
        if (can(capabilities, 4)) {
            builder.append(isLong ? " CAPABILITY_MERGE_CONFERENCE" : " mrg_cnf");
        }
        if (can(capabilities, 8)) {
            builder.append(isLong ? " CAPABILITY_SWAP_CONFERENCE" : " swp_cnf");
        }
        if (can(capabilities, 32)) {
            builder.append(isLong ? " CAPABILITY_RESPOND_VIA_TEXT" : " txt");
        }
        if (can(capabilities, 64)) {
            builder.append(isLong ? " CAPABILITY_MUTE" : " mut");
        }
        if (can(capabilities, 128)) {
            builder.append(isLong ? " CAPABILITY_MANAGE_CONFERENCE" : " mng_cnf");
        }
        if (can(capabilities, 256)) {
            builder.append(isLong ? " CAPABILITY_SUPPORTS_VT_LOCAL_RX" : " VTlrx");
        }
        if (can(capabilities, 512)) {
            builder.append(isLong ? " CAPABILITY_SUPPORTS_VT_LOCAL_TX" : " VTltx");
        }
        if (can(capabilities, 768)) {
            builder.append(isLong ? " CAPABILITY_SUPPORTS_VT_LOCAL_BIDIRECTIONAL" : " VTlbi");
        }
        if (can(capabilities, 1024)) {
            builder.append(isLong ? " CAPABILITY_SUPPORTS_VT_REMOTE_RX" : " VTrrx");
        }
        if (can(capabilities, 2048)) {
            builder.append(isLong ? " CAPABILITY_SUPPORTS_VT_REMOTE_TX" : " VTrtx");
        }
        if (can(capabilities, 3072)) {
            builder.append(isLong ? " CAPABILITY_SUPPORTS_VT_REMOTE_BIDIRECTIONAL" : " VTrbi");
        }
        if (can(capabilities, 8388608)) {
            builder.append(isLong ? " CAPABILITY_CANNOT_DOWNGRADE_VIDEO_TO_AUDIO" : " !v2a");
        }
        if (can(capabilities, 262144)) {
            builder.append(isLong ? " CAPABILITY_SPEED_UP_MT_AUDIO" : " spd_aud");
        }
        if (can(capabilities, 524288)) {
            builder.append(isLong ? " CAPABILITY_CAN_UPGRADE_TO_VIDEO" : " a2v");
        }
        if (can(capabilities, 1048576)) {
            builder.append(isLong ? " CAPABILITY_CAN_PAUSE_VIDEO" : " paus_VT");
        }
        if (can(capabilities, 2097152)) {
            builder.append(isLong ? " CAPABILITY_SINGLE_PARTY_CONFERENCE" : " 1p_cnf");
        }
        if (can(capabilities, 4194304)) {
            builder.append(isLong ? " CAPABILITY_CAN_SEND_RESPONSE_VIA_CONNECTION" : " rsp_by_con");
        }
        if (can(capabilities, 16777216)) {
            builder.append(isLong ? " CAPABILITY_CAN_PULL_CALL" : " pull");
        }
        if ((33554432 & capabilities) != 0) {
            builder.append(" CAPABILITY_VOICE_RECORD");
        }
        if ((67108864 & capabilities) != 0) {
            builder.append(" CAPABILITY_ECT");
        }
        if ((134217728 & capabilities) != 0) {
            builder.append(" CAPABILITY_VOLTE");
        }
        if ((268435456 & capabilities) != 0) {
            builder.append(" CAPABILITY_INVITE_PARTICIPANTS");
        }
        if ((capabilities & 4096) != 0) {
            builder.append(" CAPABILITY_SEPARATE_FROM_CONFERENCE");
        }
        if ((capabilities & 8192) != 0) {
            builder.append(" CAPABILITY_DISCONNECT_FROM_CONFERENCE");
        }
        if ((536870912 & capabilities) != 0) {
            builder.append(" CAPABILITY_BLIND_ASSURED_ECT");
        }
        if ((1073741824 & capabilities) != 0) {
            builder.append(" CAPABILITY_VIDEO_RINGTONE");
        }
        builder.append("]");
        return builder.toString();
    }

    public static String propertiesToString(int properties) {
        return propertiesToStringInternal(properties, true);
    }

    public static String propertiesToStringShort(int properties) {
        return propertiesToStringInternal(properties, false);
    }

    private static String propertiesToStringInternal(int properties, boolean isLong) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (isLong) {
            builder.append("Properties:");
        }
        if (can(properties, 1)) {
            builder.append(isLong ? " PROPERTY_EMERGENCY_CALLBACK_MODE" : " ecbm");
        }
        if (can(properties, 4)) {
            builder.append(isLong ? " PROPERTY_HIGH_DEF_AUDIO" : " HD");
        }
        if (can(properties, 8)) {
            builder.append(isLong ? " PROPERTY_WIFI" : " wifi");
        }
        if (can(properties, 2)) {
            builder.append(isLong ? " PROPERTY_GENERIC_CONFERENCE" : " gen_conf");
        }
        if (can(properties, 16)) {
            builder.append(isLong ? " PROPERTY_IS_EXTERNAL_CALL" : " xtrnl");
        }
        if (can(properties, 32)) {
            builder.append(isLong ? " PROPERTY_HAS_CDMA_VOICE_PRIVACY" : " priv");
        }
        if (can(properties, 65536)) {
            builder.append(" PROPERTY_VOLTE");
        }
        builder.append("]");
        return builder.toString();
    }

    public Connection() {
        this.mConnectionDeathListener = new Listener() {
            public void onDestroyed(Connection c) {
                if (Connection.this.mConferenceables.remove(c)) {
                    Connection.this.fireOnConferenceableConnectionsChanged();
                }
            }
        };
        this.mConferenceDeathListener = new android.telecom.Conference.Listener() {
            public void onDestroyed(Conference c) {
                if (Connection.this.mConferenceables.remove(c)) {
                    Connection.this.fireOnConferenceableConnectionsChanged();
                }
            }
        };
        this.mListeners = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));
        this.mConferenceables = new ArrayList();
        this.mUnmodifiableConferenceables = Collections.unmodifiableList(this.mConferenceables);
        this.mState = 1;
        this.mRingbackRequested = false;
        this.mConnectTimeMillis = 0;
        this.mExtrasLock = new Object();
    }

    public final String getTelecomCallId() {
        return this.mTelecomCallId;
    }

    public final Uri getAddress() {
        return this.mAddress;
    }

    public final int getAddressPresentation() {
        return this.mAddressPresentation;
    }

    public final String getCallerDisplayName() {
        return this.mCallerDisplayName;
    }

    public final int getCallerDisplayNamePresentation() {
        return this.mCallerDisplayNamePresentation;
    }

    public final int getState() {
        return this.mState;
    }

    public final int getVideoState() {
        return this.mVideoState;
    }

    @Deprecated
    public final AudioState getAudioState() {
        if (this.mCallAudioState == null) {
            return null;
        }
        return new AudioState(this.mCallAudioState);
    }

    public final CallAudioState getCallAudioState() {
        return this.mCallAudioState;
    }

    public final Conference getConference() {
        return this.mConference;
    }

    public final boolean isRingbackRequested() {
        return this.mRingbackRequested;
    }

    public final boolean getAudioModeIsVoip() {
        return this.mAudioModeIsVoip;
    }

    public final long getConnectTimeMillis() {
        return this.mConnectTimeMillis;
    }

    public final StatusHints getStatusHints() {
        return this.mStatusHints;
    }

    public final Bundle getExtras() {
        Bundle bundle = null;
        synchronized (this.mExtrasLock) {
            if (this.mExtras != null) {
                bundle = new Bundle(this.mExtras);
            }
        }
        return bundle;
    }

    public final Connection addConnectionListener(Listener l) {
        this.mListeners.add(l);
        return this;
    }

    public final Connection removeConnectionListener(Listener l) {
        if (l != null) {
            this.mListeners.remove(l);
        }
        return this;
    }

    public final DisconnectCause getDisconnectCause() {
        return this.mDisconnectCause;
    }

    public void setTelecomCallId(String callId) {
        this.mTelecomCallId = callId;
    }

    final void setCallAudioState(CallAudioState state) {
        checkImmutable();
        Object[] objArr = new Object[1];
        objArr[0] = state;
        Log.d((Object) this, "setAudioState %s", objArr);
        this.mCallAudioState = state;
        onAudioStateChanged(getAudioState());
        onCallAudioStateChanged(state);
    }

    public static String stateToString(int state) {
        switch (state) {
            case 0:
                return "INITIALIZING";
            case 1:
                return "NEW";
            case 2:
                return "RINGING";
            case 3:
                return "DIALING";
            case 4:
                return "ACTIVE";
            case 5:
                return "HOLDING";
            case 6:
                return "DISCONNECTED";
            case 7:
                return "PULLING_CALL";
            default:
                Object[] objArr = new Object[1];
                objArr[0] = Integer.valueOf(state);
                Log.wtf((Object) Connection.class, "Unknown state %d", objArr);
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
    }

    public final int getConnectionCapabilities() {
        return this.mConnectionCapabilities;
    }

    public final int getConnectionProperties() {
        return this.mConnectionProperties;
    }

    public final void setAddress(Uri address, int presentation) {
        checkImmutable();
        Object[] objArr = new Object[1];
        objArr[0] = address;
        Log.d((Object) this, "setAddress %s", objArr);
        this.mAddress = address;
        this.mAddressPresentation = presentation;
        for (Listener l : this.mListeners) {
            l.onAddressChanged(this, address, presentation);
        }
    }

    public final void setCallerDisplayName(String callerDisplayName, int presentation) {
        checkImmutable();
        Object[] objArr = new Object[1];
        objArr[0] = callerDisplayName;
        Log.d((Object) this, "setCallerDisplayName %s", objArr);
        this.mCallerDisplayName = callerDisplayName;
        this.mCallerDisplayNamePresentation = presentation;
        for (Listener l : this.mListeners) {
            l.onCallerDisplayNameChanged(this, callerDisplayName, presentation);
        }
    }

    public final void setVideoState(int videoState) {
        checkImmutable();
        Object[] objArr = new Object[1];
        objArr[0] = Integer.valueOf(videoState);
        Log.d((Object) this, "setVideoState %d", objArr);
        this.mVideoState = videoState;
        for (Listener l : this.mListeners) {
            l.onVideoStateChanged(this, this.mVideoState);
        }
    }

    public final void setActive() {
        checkImmutable();
        setRingbackRequested(false);
        setState(4);
    }

    public final void setRinging() {
        checkImmutable();
        setState(2);
    }

    public final void setInitializing() {
        checkImmutable();
        setState(0);
    }

    public final void setInitialized() {
        checkImmutable();
        setState(1);
    }

    public final void setDialing() {
        checkImmutable();
        setState(3);
    }

    public final void setPulling() {
        checkImmutable();
        setState(7);
    }

    public final void setOnHold() {
        checkImmutable();
        setState(5);
    }

    public final void setVideoProvider(VideoProvider videoProvider) {
        checkImmutable();
        this.mVideoProvider = videoProvider;
        for (Listener l : this.mListeners) {
            l.onVideoProviderChanged(this, videoProvider);
        }
    }

    public final VideoProvider getVideoProvider() {
        return this.mVideoProvider;
    }

    public final void setDisconnected(DisconnectCause disconnectCause) {
        checkImmutable();
        this.mDisconnectCause = disconnectCause;
        setState(6);
        Object[] objArr = new Object[1];
        objArr[0] = disconnectCause;
        Log.d((Object) this, "Disconnected with cause %s", objArr);
        for (Listener l : this.mListeners) {
            l.onDisconnected(this, disconnectCause);
        }
    }

    public final void setPostDialWait(String remaining) {
        checkImmutable();
        for (Listener l : this.mListeners) {
            l.onPostDialWait(this, remaining);
        }
    }

    public final void setNextPostDialChar(char nextChar) {
        checkImmutable();
        for (Listener l : this.mListeners) {
            l.onPostDialChar(this, nextChar);
        }
    }

    public final void setRingbackRequested(boolean ringback) {
        checkImmutable();
        if (this.mRingbackRequested != ringback) {
            this.mRingbackRequested = ringback;
            for (Listener l : this.mListeners) {
                l.onRingbackRequested(this, ringback);
            }
        }
    }

    public final void setConnectionCapabilities(int connectionCapabilities) {
        checkImmutable();
        if (this.mConnectionCapabilities != connectionCapabilities) {
            this.mConnectionCapabilities = connectionCapabilities;
            for (Listener l : this.mListeners) {
                l.onConnectionCapabilitiesChanged(this, this.mConnectionCapabilities);
            }
        }
    }

    public final void setConnectionProperties(int connectionProperties) {
        checkImmutable();
        if (this.mConnectionProperties != connectionProperties) {
            this.mConnectionProperties = connectionProperties;
            for (Listener l : this.mListeners) {
                l.onConnectionPropertiesChanged(this, this.mConnectionProperties);
            }
        }
    }

    public final void destroy() {
        for (Listener l : this.mListeners) {
            l.onDestroyed(this);
        }
    }

    public final void setAudioModeIsVoip(boolean isVoip) {
        checkImmutable();
        this.mAudioModeIsVoip = isVoip;
        for (Listener l : this.mListeners) {
            l.onAudioModeIsVoipChanged(this, isVoip);
        }
    }

    public final void setConnectTimeMillis(long connectTimeMillis) {
        this.mConnectTimeMillis = connectTimeMillis;
    }

    public final void setStatusHints(StatusHints statusHints) {
        checkImmutable();
        this.mStatusHints = statusHints;
        for (Listener l : this.mListeners) {
            l.onStatusHintsChanged(this, statusHints);
        }
    }

    public final void setConferenceableConnections(List<Connection> conferenceableConnections) {
        checkImmutable();
        clearConferenceableList();
        for (Connection c : conferenceableConnections) {
            if (!this.mConferenceables.contains(c)) {
                c.addConnectionListener(this.mConnectionDeathListener);
                this.mConferenceables.add(c);
            }
        }
        fireOnConferenceableConnectionsChanged();
    }

    public final void setConferenceables(List<Conferenceable> conferenceables) {
        clearConferenceableList();
        for (Conferenceable c : conferenceables) {
            if (!this.mConferenceables.contains(c)) {
                if (c instanceof Connection) {
                    ((Connection) c).addConnectionListener(this.mConnectionDeathListener);
                } else if (c instanceof Conference) {
                    ((Conference) c).addListener(this.mConferenceDeathListener);
                }
                this.mConferenceables.add(c);
            }
        }
        fireOnConferenceableConnectionsChanged();
    }

    public final List<Conferenceable> getConferenceables() {
        return this.mUnmodifiableConferenceables;
    }

    public final void setConnectionService(ConnectionService connectionService) {
        checkImmutable();
        if (this.mConnectionService != null) {
            Log.e((Object) this, new Exception(), "Trying to set ConnectionService on a connection which is already associated with another ConnectionService.", new Object[0]);
        } else {
            this.mConnectionService = connectionService;
        }
    }

    public final void unsetConnectionService(ConnectionService connectionService) {
        if (this.mConnectionService != connectionService) {
            Log.e((Object) this, new Exception(), "Trying to remove ConnectionService from a Connection that does not belong to the ConnectionService.", new Object[0]);
        } else {
            this.mConnectionService = null;
        }
    }

    public final ConnectionService getConnectionService() {
        return this.mConnectionService;
    }

    public final boolean setConference(Conference conference) {
        checkImmutable();
        if (this.mConference != null) {
            return false;
        }
        this.mConference = conference;
        if (this.mConnectionService != null && this.mConnectionService.containsConference(conference)) {
            fireConferenceChanged();
        }
        return true;
    }

    public final void resetConference() {
        if (this.mConference != null) {
            Log.d((Object) this, "Conference reset", new Object[0]);
            this.mConference = null;
            fireConferenceChanged();
        }
    }

    public final void notifyConnectionLost() {
        sendConnectionEvent(TelecomManagerEx.EVENT_CONNECTION_LOST, null);
    }

    public final void notifyActionFailed(int action) {
        Log.i((Object) this, "notifyActionFailed action = " + action, new Object[0]);
        sendConnectionEvent(TelecomManagerEx.EVENT_OPERATION_FAIL, TelecomManagerEx.createOperationFailBundle(action));
    }

    public void notifySSNotificationToast(int notiType, int type, int code, String number, int index) {
        Log.i((Object) this, "notifySSNotificationToast notiType = " + notiType + " type = " + type + " code = " + code + " number = " + number + " index = " + index, new Object[0]);
        sendConnectionEvent(TelecomManagerEx.EVENT_SS_NOTIFICATION, TelecomManagerEx.createSsNotificationBundle(notiType, type, code, number, index));
    }

    public void notifyNumberUpdate(String number) {
        Log.i((Object) this, "notifyNumberUpdate number = " + number, new Object[0]);
        if (!TextUtils.isEmpty(number)) {
            sendConnectionEvent(TelecomManagerEx.EVENT_NUMBER_UPDATED, TelecomManagerEx.createNumberUpdatedBundle(number));
        }
    }

    public void notifyIncomingInfoUpdate(int type, String alphaid, int cliValidity) {
        Log.i((Object) this, "notifyIncomingInfoUpdate type = " + type + " alphaid = " + alphaid + " cliValidity = " + cliValidity, new Object[0]);
        sendConnectionEvent(TelecomManagerEx.EVENT_INCOMING_INFO_UPDATED, TelecomManagerEx.createIncomingInfoUpdatedBundle(type, alphaid, cliValidity));
    }

    protected void fireOnCdmaCallAccepted() {
        Object[] objArr = new Object[1];
        objArr[0] = stateToString(this.mState);
        Log.d((Object) this, "fireOnCdmaCallAccepted: %s", objArr);
        sendConnectionEvent(TelecomManagerEx.EVENT_CDMA_CALL_ACCEPTED, null);
    }

    public PhoneAccountHandle getAccountHandle() {
        return this.mAccountHandle;
    }

    public void setAccountHandle(PhoneAccountHandle handle) {
        this.mAccountHandle = handle;
        sendConnectionEvent(TelecomManagerEx.EVENT_PHONE_ACCOUNT_CHANGED, TelecomManagerEx.createPhoneAccountChangedBundle(handle));
    }

    public final void notifyVtStatusInfo(int status) {
        Log.d((Object) this, "notifyVtStatusInfo %s" + status, new Object[0]);
        sendConnectionEvent(TelecomManagerEx.EVENT_VT_STATUS_UPDATED, TelecomManagerEx.createVtStatudUpdatedBundle(status));
    }

    public void setCallInfo(Bundle bundle) {
        Log.d((Object) this, "setCallInfo", new Object[0]);
        sendConnectionEvent(TelecomManagerEx.EVENT_UPDATE_VOLTE_EXTRA, bundle);
    }

    public final void setExtras(Bundle extras) {
        checkImmutable();
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

    public final void putExtras(Bundle extras) {
        checkImmutable();
        if (extras != null) {
            Bundle listenerExtras;
            synchronized (this.mExtrasLock) {
                if (this.mExtras == null) {
                    this.mExtras = new Bundle();
                }
                this.mExtras.putAll(extras);
                listenerExtras = new Bundle(this.mExtras);
            }
            for (Listener l : this.mListeners) {
                l.onExtrasChanged(this, new Bundle(listenerExtras));
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

    public final void removeExtras(String... keys) {
        removeExtras(Arrays.asList(keys));
    }

    @Deprecated
    public void onAudioStateChanged(AudioState state) {
    }

    public void onCallAudioStateChanged(CallAudioState state) {
    }

    public void onStateChanged(int state) {
    }

    public void onPlayDtmfTone(char c) {
    }

    public void onStopDtmfTone() {
    }

    public void onDisconnect() {
    }

    public void onDisconnectConferenceParticipant(Uri endpoint) {
    }

    public void onSeparate() {
    }

    public void onAbort() {
    }

    public void onHold() {
    }

    public void onUnhold() {
    }

    public void onAnswer(int videoState) {
    }

    public void onAnswer() {
        onAnswer(0);
    }

    public void onReject() {
    }

    public void onReject(String replyMessage) {
    }

    public void onSilence() {
    }

    public void onPostDialContinue(boolean proceed) {
    }

    public void onPullExternalCall() {
    }

    public void onCallEvent(String event, Bundle extras) {
    }

    public void onExtrasChanged(Bundle extras) {
    }

    public void onExplicitCallTransfer() {
    }

    public void onExplicitCallTransfer(String number, int type) {
    }

    public void onHangupAll() {
    }

    static String toLogSafePhoneNumber(String number) {
        if (number == null) {
            return PhoneConstants.MVNO_TYPE_NONE;
        }
        if (PII_DEBUG) {
            return number;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            if (c == '-' || c == '@' || c == '.') {
                builder.append(c);
            } else {
                builder.append(StateProperty.TARGET_X);
            }
        }
        return builder.toString();
    }

    private void setState(int state) {
        checkImmutable();
        if (this.mState != 6 || this.mState == state) {
            if (this.mState != state) {
                Object[] objArr = new Object[1];
                objArr[0] = stateToString(state);
                Log.d((Object) this, "setState: %s", objArr);
                this.mState = state;
                Builder builder = configDumpLogBuilder(new Builder());
                if (builder != null) {
                    FormattedLog formattedLog = builder.buildDumpInfo();
                    if (formattedLog != null && (!SENLOG || TELDBG)) {
                        Log.d((Object) this, formattedLog.toString(), new Object[0]);
                    }
                }
                onStateChanged(state);
                for (Listener l : this.mListeners) {
                    l.onStateChanged(this, state);
                }
            }
            return;
        }
        Log.d((Object) this, "Connection already DISCONNECTED; cannot transition out of this state.", new Object[0]);
    }

    protected void fireOnCallState() {
    }

    public static Connection createFailedConnection(DisconnectCause disconnectCause) {
        return new FailureSignalingConnection(disconnectCause);
    }

    public void checkImmutable() {
    }

    public static Connection createCanceledConnection() {
        return new FailureSignalingConnection(new DisconnectCause(4));
    }

    private final void fireOnConferenceableConnectionsChanged() {
        for (Listener l : this.mListeners) {
            l.onConferenceablesChanged(this, getConferenceables());
        }
    }

    private final void fireConferenceChanged() {
        for (Listener l : this.mListeners) {
            l.onConferenceChanged(this, this.mConference);
        }
    }

    private final void clearConferenceableList() {
        for (Conferenceable c : this.mConferenceables) {
            if (c instanceof Connection) {
                ((Connection) c).removeConnectionListener(this.mConnectionDeathListener);
            } else if (c instanceof Conference) {
                ((Conference) c).removeListener(this.mConferenceDeathListener);
            }
        }
        this.mConferenceables.clear();
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

    protected final void notifyConferenceMergeFailed() {
        for (Listener l : this.mListeners) {
            l.onConferenceMergeFailed(this);
        }
    }

    protected final void updateConferenceParticipants(List<ConferenceParticipant> conferenceParticipants) {
        for (Listener l : this.mListeners) {
            l.onConferenceParticipantsChanged(this, conferenceParticipants);
        }
    }

    protected void notifyConferenceStarted() {
        for (Listener l : this.mListeners) {
            l.onConferenceStarted();
        }
    }

    protected void notifyConferenceSupportedChanged(boolean isConferenceSupported) {
        for (Listener l : this.mListeners) {
            l.onConferenceSupportedChanged(this, isConferenceSupported);
        }
    }

    public void sendConnectionEvent(String event, Bundle extras) {
        for (Listener l : this.mListeners) {
            l.onConnectionEvent(this, event, extras);
        }
    }

    protected Builder configDumpLogBuilder(Builder builder) {
        if (builder == null) {
            return null;
        }
        builder.setCategory("CC");
        builder.setOpType(OpType.DUMP);
        if (this.mAddress != null) {
            builder.setCallNumber(Rlog.pii(SDBG, this.mAddress.getSchemeSpecificPart()));
        }
        builder.setCallId(Integer.toString(System.identityHashCode(this)));
        builder.setStatusInfo("isConfCall", "No");
        builder.setStatusInfo("state", callStateToFormattedDumpString(this.mState));
        if (getConference() == null) {
            builder.setStatusInfo("isConfChildCall", "No");
        } else {
            builder.setStatusInfo("isConfChildCall", "Yes");
        }
        builder.setStatusInfo("capabilities", capabilitiesToString(getConnectionCapabilities()));
        return builder;
    }

    static String callStateToFormattedDumpString(int state) {
        switch (state) {
            case 0:
            case 1:
                return "new";
            case 2:
                return "ringing";
            case 3:
                return "dialing";
            case 4:
                return "active";
            case 5:
                return "onhold";
            case 6:
                return ImsConferenceState.STATUS_DISCONNECTED;
            default:
                return "unknown";
        }
    }
}

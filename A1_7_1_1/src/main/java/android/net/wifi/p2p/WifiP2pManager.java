package android.net.wifi.p2p;

import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.link.WifiP2pLinkInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class WifiP2pManager {
    public static final int ADD_LOCAL_SERVICE = 139292;
    public static final int ADD_LOCAL_SERVICE_FAILED = 139293;
    public static final int ADD_LOCAL_SERVICE_SUCCEEDED = 139294;
    public static final int ADD_PERSISTENT_GROUP = 139361;
    public static final int ADD_PERSISTENT_GROUP_FAILED = 139362;
    public static final int ADD_PERSISTENT_GROUP_SUCCEEDED = 139363;
    public static final int ADD_SERVICE_REQUEST = 139301;
    public static final int ADD_SERVICE_REQUEST_FAILED = 139302;
    public static final int ADD_SERVICE_REQUEST_SUCCEEDED = 139303;
    private static final int BASE = 139264;
    public static final int BEAM_DISCOVERY_TIMEOUT = 123;
    public static final int BEAM_GO_MODE_DISABLE = 3;
    public static final int BEAM_GO_MODE_ENABLE = 1;
    public static final int BEAM_MODE_DISABLE = 2;
    public static final int BEAM_MODE_ENABLE = 0;
    public static final int BUSY = 2;
    public static final int CANCEL_CONNECT = 139274;
    public static final int CANCEL_CONNECT_FAILED = 139275;
    public static final int CANCEL_CONNECT_SUCCEEDED = 139276;
    public static final int CLEAR_LOCAL_SERVICES = 139298;
    public static final int CLEAR_LOCAL_SERVICES_FAILED = 139299;
    public static final int CLEAR_LOCAL_SERVICES_SUCCEEDED = 139300;
    public static final int CLEAR_SERVICE_REQUESTS = 139307;
    public static final int CLEAR_SERVICE_REQUESTS_FAILED = 139308;
    public static final int CLEAR_SERVICE_REQUESTS_SUCCEEDED = 139309;
    public static final int CONNECT = 139271;
    public static final int CONNECT_FAILED = 139272;
    public static final int CONNECT_SUCCEEDED = 139273;
    public static final int CREATE_GROUP = 139277;
    public static final int CREATE_GROUP_FAILED = 139278;
    public static final int CREATE_GROUP_SUCCEEDED = 139279;
    public static final int DELETE_PERSISTENT_GROUP = 139318;
    public static final int DELETE_PERSISTENT_GROUP_FAILED = 139319;
    public static final int DELETE_PERSISTENT_GROUP_SUCCEEDED = 139320;
    public static final int DISCOVER_PEERS = 139265;
    public static final int DISCOVER_PEERS_FAILED = 139266;
    public static final int DISCOVER_PEERS_SUCCEEDED = 139267;
    public static final int DISCOVER_SERVICES = 139310;
    public static final int DISCOVER_SERVICES_FAILED = 139311;
    public static final int DISCOVER_SERVICES_SUCCEEDED = 139312;
    public static final int ERROR = 0;
    public static final String EXTRA_CLIENT_MESSAGE = "android.net.wifi.p2p.EXTRA_CLIENT_MESSAGE";
    public static final String EXTRA_DISCOVERY_STATE = "discoveryState";
    public static final String EXTRA_HANDOVER_MESSAGE = "android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE";
    public static final String EXTRA_NETWORK_INFO = "networkInfo";
    public static final String EXTRA_P2P_DEVICE_LIST = "wifiP2pDeviceList";
    public static final String EXTRA_PIN_CODE = "android.net.wifi.p2p.EXTRA_PIN_CODE";
    public static final String EXTRA_PIN_METHOD = "android.net.wifi.p2p.EXTRA_PIN_METHOD";
    public static final String EXTRA_WIFI_P2P_DEVICE = "wifiP2pDevice";
    public static final String EXTRA_WIFI_P2P_GROUP = "p2pGroupInfo";
    public static final String EXTRA_WIFI_P2P_INFO = "wifiP2pInfo";
    public static final String EXTRA_WIFI_STATE = "wifi_p2p_state";
    public static final int FREQ_CONFLICT_EX_RESULT = 139356;
    public static final int GET_HANDOVER_REQUEST = 139339;
    public static final int GET_HANDOVER_SELECT = 139340;
    public static final int INITIATOR_REPORT_NFC_HANDOVER = 139342;
    public static final int MIRACAST_DISABLED = 0;
    public static final int MIRACAST_SINK = 2;
    public static final int MIRACAST_SOURCE = 1;
    public static final int NO_SERVICE_REQUESTS = 3;
    public static final int P2P_UNSUPPORTED = 1;
    public static final int PEER_CONNECTION_USER_ACCEPT_FROM_OUTER = 139354;
    public static final int PEER_CONNECTION_USER_REJECT_FROM_OUTER = 139355;
    public static final int PING = 139313;
    public static final int REMOVE_CLIENT = 139357;
    public static final int REMOVE_CLIENT_FAILED = 139358;
    public static final int REMOVE_CLIENT_SUCCEEDED = 139359;
    public static final int REMOVE_GROUP = 139280;
    public static final int REMOVE_GROUP_FAILED = 139281;
    public static final int REMOVE_GROUP_SUCCEEDED = 139282;
    public static final int REMOVE_LOCAL_SERVICE = 139295;
    public static final int REMOVE_LOCAL_SERVICE_FAILED = 139296;
    public static final int REMOVE_LOCAL_SERVICE_SUCCEEDED = 139297;
    public static final int REMOVE_SERVICE_REQUEST = 139304;
    public static final int REMOVE_SERVICE_REQUEST_FAILED = 139305;
    public static final int REMOVE_SERVICE_REQUEST_SUCCEEDED = 139306;
    public static final int REPORT_NFC_HANDOVER_FAILED = 139345;
    public static final int REPORT_NFC_HANDOVER_SUCCEEDED = 139344;
    public static final int REQUEST_CONNECTION_INFO = 139285;
    public static final int REQUEST_GROUP_INFO = 139287;
    public static final int REQUEST_LINK_INFO = 139349;
    public static final int REQUEST_PEERS = 139283;
    public static final int REQUEST_PERSISTENT_GROUP_INFO = 139321;
    public static final int RESPONDER_REPORT_NFC_HANDOVER = 139343;
    public static final int RESPONSE_ADD_PERSISTENT_GROUP = 139364;
    public static final int RESPONSE_CONNECTION_INFO = 139286;
    public static final int RESPONSE_GET_HANDOVER_MESSAGE = 139341;
    public static final int RESPONSE_GROUP_INFO = 139288;
    public static final int RESPONSE_LINK_INFO = 139350;
    public static final int RESPONSE_PEERS = 139284;
    public static final int RESPONSE_PERSISTENT_GROUP_INFO = 139322;
    public static final int RESPONSE_SERVICE = 139314;
    public static final int SET_AUTO_CHANNEL_SELECT = 139351;
    public static final int SET_AUTO_CHANNEL_SELECT_FAILED = 139352;
    public static final int SET_AUTO_CHANNEL_SELECT_SUCCEEDED = 139353;
    public static final int SET_CHANNEL = 139335;
    public static final int SET_CHANNEL_FAILED = 139336;
    public static final int SET_CHANNEL_SUCCEEDED = 139337;
    public static final int SET_DEVICE_NAME = 139315;
    public static final int SET_DEVICE_NAME_FAILED = 139316;
    public static final int SET_DEVICE_NAME_SUCCEEDED = 139317;
    public static final int SET_WFD_INFO = 139323;
    public static final int SET_WFD_INFO_FAILED = 139324;
    public static final int SET_WFD_INFO_SUCCEEDED = 139325;
    public static final int START_LISTEN = 139329;
    public static final int START_LISTEN_FAILED = 139330;
    public static final int START_LISTEN_SUCCEEDED = 139331;
    public static final int START_WPS = 139326;
    public static final int START_WPS_FAILED = 139327;
    public static final int START_WPS_SUCCEEDED = 139328;
    public static final int STOP_DISCOVERY = 139268;
    public static final int STOP_DISCOVERY_FAILED = 139269;
    public static final int STOP_DISCOVERY_SUCCEEDED = 139270;
    public static final int STOP_LISTEN = 139332;
    public static final int STOP_LISTEN_FAILED = 139333;
    public static final int STOP_LISTEN_SUCCEEDED = 139334;
    public static final int STOP_P2P_FIND_ONLY = 139360;
    private static final String TAG = "WifiP2pManager";
    public static final int UPPER_BOUND = 139392;
    public static final String WIFI_P2P_CONNECTION_CHANGED_ACTION = "android.net.wifi.p2p.CONNECTION_STATE_CHANGE";
    public static final String WIFI_P2P_DISCOVERY_CHANGED_ACTION = "android.net.wifi.p2p.DISCOVERY_STATE_CHANGE";
    public static final int WIFI_P2P_DISCOVERY_STARTED = 2;
    public static final int WIFI_P2P_DISCOVERY_STOPPED = 1;
    public static final String WIFI_P2P_PEERS_CHANGED_ACTION = "android.net.wifi.p2p.PEERS_CHANGED";
    public static final String WIFI_P2P_PERSISTENT_GROUPS_CHANGED_ACTION = "android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED";
    public static final String WIFI_P2P_STATE_CHANGED_ACTION = "android.net.wifi.p2p.STATE_CHANGED";
    public static final int WIFI_P2P_STATE_DISABLED = 1;
    public static final int WIFI_P2P_STATE_ENABLED = 2;
    public static final String WIFI_P2P_THIS_DEVICE_CHANGED_ACTION = "android.net.wifi.p2p.THIS_DEVICE_CHANGED";
    private static final Pattern macPattern = null;
    IWifiP2pManager mService;

    public interface ActionListener {
        void onFailure(int i);

        void onSuccess();
    }

    public interface AddPersistentGroupListener {
        void onAddPersistentGroupAdded(WifiP2pGroup wifiP2pGroup);
    }

    public static class Channel {
        private static final int INVALID_LISTENER_KEY = 0;
        private AsyncChannel mAsyncChannel;
        private ChannelListener mChannelListener;
        Context mContext;
        private DnsSdServiceResponseListener mDnsSdServRspListener;
        private DnsSdTxtRecordListener mDnsSdTxtListener;
        private P2pHandler mHandler;
        private int mListenerKey;
        private HashMap<Integer, Object> mListenerMap;
        private Object mListenerMapLock;
        private ServiceResponseListener mServRspListener;
        private UpnpServiceResponseListener mUpnpServRspListener;

        class P2pHandler extends Handler {
            final /* synthetic */ Channel this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.p2p.WifiP2pManager.Channel.P2pHandler.<init>(android.net.wifi.p2p.WifiP2pManager$Channel, android.os.Looper):void, dex: 
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
            P2pHandler(android.net.wifi.p2p.WifiP2pManager.Channel r1, android.os.Looper r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.p2p.WifiP2pManager.Channel.P2pHandler.<init>(android.net.wifi.p2p.WifiP2pManager$Channel, android.os.Looper):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.P2pHandler.<init>(android.net.wifi.p2p.WifiP2pManager$Channel, android.os.Looper):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.P2pHandler.handleMessage(android.os.Message):void, dex: 
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
            public void handleMessage(android.os.Message r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.P2pHandler.handleMessage(android.os.Message):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.P2pHandler.handleMessage(android.os.Message):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-get0(android.net.wifi.p2p.WifiP2pManager$Channel):com.android.internal.util.AsyncChannel, dex: 
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
        /* renamed from: -get0 */
        static /* synthetic */ com.android.internal.util.AsyncChannel m85-get0(android.net.wifi.p2p.WifiP2pManager.Channel r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-get0(android.net.wifi.p2p.WifiP2pManager$Channel):com.android.internal.util.AsyncChannel, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.-get0(android.net.wifi.p2p.WifiP2pManager$Channel):com.android.internal.util.AsyncChannel");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-get1(android.net.wifi.p2p.WifiP2pManager$Channel):android.net.wifi.p2p.WifiP2pManager$ChannelListener, dex: 
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
        /* renamed from: -get1 */
        static /* synthetic */ android.net.wifi.p2p.WifiP2pManager.ChannelListener m86-get1(android.net.wifi.p2p.WifiP2pManager.Channel r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-get1(android.net.wifi.p2p.WifiP2pManager$Channel):android.net.wifi.p2p.WifiP2pManager$ChannelListener, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.-get1(android.net.wifi.p2p.WifiP2pManager$Channel):android.net.wifi.p2p.WifiP2pManager$ChannelListener");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-get2(android.net.wifi.p2p.WifiP2pManager$Channel):android.net.wifi.p2p.WifiP2pManager$Channel$P2pHandler, dex: 
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
        /* renamed from: -get2 */
        static /* synthetic */ android.net.wifi.p2p.WifiP2pManager.Channel.P2pHandler m87-get2(android.net.wifi.p2p.WifiP2pManager.Channel r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-get2(android.net.wifi.p2p.WifiP2pManager$Channel):android.net.wifi.p2p.WifiP2pManager$Channel$P2pHandler, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.-get2(android.net.wifi.p2p.WifiP2pManager$Channel):android.net.wifi.p2p.WifiP2pManager$Channel$P2pHandler");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set0(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$ChannelListener):android.net.wifi.p2p.WifiP2pManager$ChannelListener, dex: 
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
        /* renamed from: -set0 */
        static /* synthetic */ android.net.wifi.p2p.WifiP2pManager.ChannelListener m88-set0(android.net.wifi.p2p.WifiP2pManager.Channel r1, android.net.wifi.p2p.WifiP2pManager.ChannelListener r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set0(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$ChannelListener):android.net.wifi.p2p.WifiP2pManager$ChannelListener, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.-set0(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$ChannelListener):android.net.wifi.p2p.WifiP2pManager$ChannelListener");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set1(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener, dex:  in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set1(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set1(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        /* renamed from: -set1 */
        static /* synthetic */ android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener m89-set1(android.net.wifi.p2p.WifiP2pManager.Channel r1, android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set1(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener, dex:  in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set1(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.-set1(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$DnsSdServiceResponseListener");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set2(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener):android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener, dex:  in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set2(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener):android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set2(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener):android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        /* renamed from: -set2 */
        static /* synthetic */ android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener m90-set2(android.net.wifi.p2p.WifiP2pManager.Channel r1, android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set2(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener):android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener, dex:  in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set2(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener):android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.-set2(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener):android.net.wifi.p2p.WifiP2pManager$DnsSdTxtRecordListener");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set3(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$ServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$ServiceResponseListener, dex: 
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
        /* renamed from: -set3 */
        static /* synthetic */ android.net.wifi.p2p.WifiP2pManager.ServiceResponseListener m91-set3(android.net.wifi.p2p.WifiP2pManager.Channel r1, android.net.wifi.p2p.WifiP2pManager.ServiceResponseListener r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set3(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$ServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$ServiceResponseListener, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.-set3(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$ServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$ServiceResponseListener");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set4(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener, dex:  in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set4(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set4(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        /* renamed from: -set4 */
        static /* synthetic */ android.net.wifi.p2p.WifiP2pManager.UpnpServiceResponseListener m92-set4(android.net.wifi.p2p.WifiP2pManager.Channel r1, android.net.wifi.p2p.WifiP2pManager.UpnpServiceResponseListener r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set4(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener, dex:  in method: android.net.wifi.p2p.WifiP2pManager.Channel.-set4(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.-set4(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener):android.net.wifi.p2p.WifiP2pManager$UpnpServiceResponseListener");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-wrap2(android.net.wifi.p2p.WifiP2pManager$Channel):void, dex: 
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
        /* renamed from: -wrap2 */
        static /* synthetic */ void m93-wrap2(android.net.wifi.p2p.WifiP2pManager.Channel r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-wrap2(android.net.wifi.p2p.WifiP2pManager$Channel):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.-wrap2(android.net.wifi.p2p.WifiP2pManager$Channel):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-wrap3(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.nsd.WifiP2pServiceResponse):void, dex: 
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
        /* renamed from: -wrap3 */
        static /* synthetic */ void m94-wrap3(android.net.wifi.p2p.WifiP2pManager.Channel r1, android.net.wifi.p2p.nsd.WifiP2pServiceResponse r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pManager.Channel.-wrap3(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.nsd.WifiP2pServiceResponse):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.-wrap3(android.net.wifi.p2p.WifiP2pManager$Channel, android.net.wifi.p2p.nsd.WifiP2pServiceResponse):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.p2p.WifiP2pManager.Channel.<init>(android.content.Context, android.os.Looper, android.net.wifi.p2p.WifiP2pManager$ChannelListener):void, dex: 
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
        Channel(android.content.Context r1, android.os.Looper r2, android.net.wifi.p2p.WifiP2pManager.ChannelListener r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.p2p.WifiP2pManager.Channel.<init>(android.content.Context, android.os.Looper, android.net.wifi.p2p.WifiP2pManager$ChannelListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.<init>(android.content.Context, android.os.Looper, android.net.wifi.p2p.WifiP2pManager$ChannelListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.clearListener():void, dex: 
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
        private void clearListener() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.clearListener():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.clearListener():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.getListener(int):java.lang.Object, dex: 
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
        private java.lang.Object getListener(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.getListener(int):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.getListener(int):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.p2p.WifiP2pManager.Channel.handleDnsSdServiceResponse(android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceResponse):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private void handleDnsSdServiceResponse(android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceResponse r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.p2p.WifiP2pManager.Channel.handleDnsSdServiceResponse(android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceResponse):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.handleDnsSdServiceResponse(android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceResponse):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pManager.Channel.handleServiceResponse(android.net.wifi.p2p.nsd.WifiP2pServiceResponse):void, dex: 
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
        private void handleServiceResponse(android.net.wifi.p2p.nsd.WifiP2pServiceResponse r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pManager.Channel.handleServiceResponse(android.net.wifi.p2p.nsd.WifiP2pServiceResponse):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.handleServiceResponse(android.net.wifi.p2p.nsd.WifiP2pServiceResponse):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.handleUpnpServiceResponse(android.net.wifi.p2p.nsd.WifiP2pUpnpServiceResponse):void, dex: 
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
        private void handleUpnpServiceResponse(android.net.wifi.p2p.nsd.WifiP2pUpnpServiceResponse r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.p2p.WifiP2pManager.Channel.handleUpnpServiceResponse(android.net.wifi.p2p.nsd.WifiP2pUpnpServiceResponse):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.handleUpnpServiceResponse(android.net.wifi.p2p.nsd.WifiP2pUpnpServiceResponse):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.wifi.p2p.WifiP2pManager.Channel.putListener(java.lang.Object):int, dex:  in method: android.net.wifi.p2p.WifiP2pManager.Channel.putListener(java.lang.Object):int, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.wifi.p2p.WifiP2pManager.Channel.putListener(java.lang.Object):int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        private int putListener(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.wifi.p2p.WifiP2pManager.Channel.putListener(java.lang.Object):int, dex:  in method: android.net.wifi.p2p.WifiP2pManager.Channel.putListener(java.lang.Object):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.Channel.putListener(java.lang.Object):int");
        }
    }

    public interface ChannelListener {
        void onChannelDisconnected();
    }

    public interface ConnectionInfoListener {
        void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo);
    }

    public interface DnsSdServiceResponseListener {
        void onDnsSdServiceAvailable(String str, String str2, WifiP2pDevice wifiP2pDevice);
    }

    public interface DnsSdTxtRecordListener {
        void onDnsSdTxtRecordAvailable(String str, Map<String, String> map, WifiP2pDevice wifiP2pDevice);
    }

    public interface GroupInfoListener {
        void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup);
    }

    public interface HandoverMessageListener {
        void onHandoverMessageAvailable(String str);
    }

    public interface PeerListListener {
        void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList);
    }

    public interface PersistentGroupInfoListener {
        void onPersistentGroupInfoAvailable(WifiP2pGroupList wifiP2pGroupList);
    }

    public interface ServiceResponseListener {
        void onServiceAvailable(int i, byte[] bArr, WifiP2pDevice wifiP2pDevice);
    }

    public interface UpnpServiceResponseListener {
        void onUpnpServiceAvailable(List<String> list, WifiP2pDevice wifiP2pDevice);
    }

    public interface WifiP2pLinkInfoListener {
        void onLinkInfoAvailable(WifiP2pLinkInfo wifiP2pLinkInfo);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pManager.<clinit>():void");
    }

    public WifiP2pManager(IWifiP2pManager service) {
        this.mService = service;
    }

    private static void checkChannel(Channel c) {
        if (c == null) {
            throw new IllegalArgumentException("Channel needs to be initialized");
        }
    }

    private static void checkServiceInfo(WifiP2pServiceInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("service info is null");
        }
    }

    private static void checkServiceRequest(WifiP2pServiceRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("service request is null");
        }
    }

    private static void checkP2pConfig(WifiP2pConfig c) {
        if (c == null) {
            throw new IllegalArgumentException("config cannot be null");
        } else if (TextUtils.isEmpty(c.deviceAddress)) {
            throw new IllegalArgumentException("deviceAddress cannot be empty");
        }
    }

    private void checkMac(String mac) {
        if (!macPattern.matcher(mac).find()) {
            throw new IllegalArgumentException("MAC needs to be well-formed");
        }
    }

    public Channel initialize(Context srcContext, Looper srcLooper, ChannelListener listener) {
        return initalizeChannel(srcContext, srcLooper, listener, getMessenger());
    }

    public Channel initializeInternal(Context srcContext, Looper srcLooper, ChannelListener listener) {
        return initalizeChannel(srcContext, srcLooper, listener, getP2pStateMachineMessenger());
    }

    private Channel initalizeChannel(Context srcContext, Looper srcLooper, ChannelListener listener, Messenger messenger) {
        if (messenger == null) {
            return null;
        }
        Channel c = new Channel(srcContext, srcLooper, listener);
        if (Channel.m85-get0(c).connectSync(srcContext, Channel.m87-get2(c), messenger) == 0) {
            return c;
        }
        return null;
    }

    public void discoverPeers(Channel c, ActionListener listener) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + "(), pid: " + Process.myPid() + ", tid: " + Process.myTid() + ", uid: " + Process.myUid());
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(DISCOVER_PEERS, 0, c.putListener(listener));
    }

    public void stopPeerDiscovery(Channel c, ActionListener listener) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + "(), pid: " + Process.myPid() + ", tid: " + Process.myTid() + ", uid: " + Process.myUid());
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(STOP_DISCOVERY, 0, c.putListener(listener));
    }

    public void connect(Channel c, WifiP2pConfig config, ActionListener listener) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + "(), pid: " + Process.myPid() + ", tid: " + Process.myTid() + ", uid: " + Process.myUid());
        checkChannel(c);
        checkP2pConfig(config);
        Channel.m85-get0(c).sendMessage(CONNECT, 0, c.putListener(listener), config);
    }

    public void cancelConnect(Channel c, ActionListener listener) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + "(), pid: " + Process.myPid() + ", tid: " + Process.myTid() + ", uid: " + Process.myUid());
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(CANCEL_CONNECT, 0, c.putListener(listener));
    }

    public void createGroup(Channel c, ActionListener listener) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + "(), pid: " + Process.myPid() + ", tid: " + Process.myTid() + ", uid: " + Process.myUid());
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(CREATE_GROUP, -2, c.putListener(listener));
    }

    public void removeGroup(Channel c, ActionListener listener) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + "(), pid: " + Process.myPid() + ", tid: " + Process.myTid() + ", uid: " + Process.myUid());
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(REMOVE_GROUP, 0, c.putListener(listener));
    }

    public void listen(Channel c, boolean enable, ActionListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(enable ? START_LISTEN : STOP_LISTEN, 0, c.putListener(listener));
    }

    public void setWifiP2pChannels(Channel c, int lc, int oc, ActionListener listener) {
        checkChannel(c);
        Bundle p2pChannels = new Bundle();
        p2pChannels.putInt("lc", lc);
        p2pChannels.putInt("oc", oc);
        Channel.m85-get0(c).sendMessage(SET_CHANNEL, 0, c.putListener(listener), p2pChannels);
    }

    public void startWps(Channel c, WpsInfo wps, ActionListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(START_WPS, 0, c.putListener(listener), wps);
    }

    public void addLocalService(Channel c, WifiP2pServiceInfo servInfo, ActionListener listener) {
        checkChannel(c);
        checkServiceInfo(servInfo);
        Channel.m85-get0(c).sendMessage(ADD_LOCAL_SERVICE, 0, c.putListener(listener), servInfo);
    }

    public void removeLocalService(Channel c, WifiP2pServiceInfo servInfo, ActionListener listener) {
        checkChannel(c);
        checkServiceInfo(servInfo);
        Channel.m85-get0(c).sendMessage(REMOVE_LOCAL_SERVICE, 0, c.putListener(listener), servInfo);
    }

    public void clearLocalServices(Channel c, ActionListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(CLEAR_LOCAL_SERVICES, 0, c.putListener(listener));
    }

    public void setServiceResponseListener(Channel c, ServiceResponseListener listener) {
        checkChannel(c);
        Channel.m91-set3(c, listener);
    }

    public void setDnsSdResponseListeners(Channel c, DnsSdServiceResponseListener servListener, DnsSdTxtRecordListener txtListener) {
        checkChannel(c);
        Channel.m89-set1(c, servListener);
        Channel.m90-set2(c, txtListener);
    }

    public void setUpnpServiceResponseListener(Channel c, UpnpServiceResponseListener listener) {
        checkChannel(c);
        Channel.m92-set4(c, listener);
    }

    public void discoverServices(Channel c, ActionListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(DISCOVER_SERVICES, 0, c.putListener(listener));
    }

    public void addServiceRequest(Channel c, WifiP2pServiceRequest req, ActionListener listener) {
        checkChannel(c);
        checkServiceRequest(req);
        Channel.m85-get0(c).sendMessage(ADD_SERVICE_REQUEST, 0, c.putListener(listener), req);
    }

    public void removeServiceRequest(Channel c, WifiP2pServiceRequest req, ActionListener listener) {
        checkChannel(c);
        checkServiceRequest(req);
        Channel.m85-get0(c).sendMessage(REMOVE_SERVICE_REQUEST, 0, c.putListener(listener), req);
    }

    public void clearServiceRequests(Channel c, ActionListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(CLEAR_SERVICE_REQUESTS, 0, c.putListener(listener));
    }

    public void requestPeers(Channel c, PeerListListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(REQUEST_PEERS, 0, c.putListener(listener));
    }

    public void requestConnectionInfo(Channel c, ConnectionInfoListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(REQUEST_CONNECTION_INFO, 0, c.putListener(listener));
    }

    public void requestGroupInfo(Channel c, GroupInfoListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(REQUEST_GROUP_INFO, 0, c.putListener(listener));
    }

    public void setDeviceName(Channel c, String devName, ActionListener listener) {
        checkChannel(c);
        WifiP2pDevice d = new WifiP2pDevice();
        d.deviceName = devName;
        Channel.m85-get0(c).sendMessage(SET_DEVICE_NAME, 0, c.putListener(listener), d);
    }

    public void setWFDInfo(Channel c, WifiP2pWfdInfo wfdInfo, ActionListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(SET_WFD_INFO, 0, c.putListener(listener), wfdInfo);
    }

    public void deletePersistentGroup(Channel c, int netId, ActionListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(DELETE_PERSISTENT_GROUP, netId, c.putListener(listener));
    }

    public void requestPersistentGroupInfo(Channel c, PersistentGroupInfoListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(REQUEST_PERSISTENT_GROUP_INFO, 0, c.putListener(listener));
    }

    public void setMiracastMode(int mode) {
        try {
            this.mService.setMiracastMode(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setMiracastMode(int mode, int freq) {
        try {
            this.mService.setMiracastModeEx(mode, freq);
        } catch (RemoteException e) {
        }
    }

    public void setCrossmountMode(int mode, int freq) {
        try {
            this.mService.setMiracastModeEx(mode, freq);
        } catch (RemoteException e) {
        }
    }

    public Messenger getMessenger() {
        try {
            return this.mService.getMessenger();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Messenger getP2pStateMachineMessenger() {
        try {
            return this.mService.getP2pStateMachineMessenger();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void getNfcHandoverRequest(Channel c, HandoverMessageListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(GET_HANDOVER_REQUEST, 0, c.putListener(listener));
    }

    public void getNfcHandoverSelect(Channel c, HandoverMessageListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(GET_HANDOVER_SELECT, 0, c.putListener(listener));
    }

    public void initiatorReportNfcHandover(Channel c, String handoverSelect, ActionListener listener) {
        checkChannel(c);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_HANDOVER_MESSAGE, handoverSelect);
        Channel.m85-get0(c).sendMessage(INITIATOR_REPORT_NFC_HANDOVER, 0, c.putListener(listener), bundle);
    }

    public void responderReportNfcHandover(Channel c, String handoverRequest, ActionListener listener) {
        checkChannel(c);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_HANDOVER_MESSAGE, handoverRequest);
        Channel.m85-get0(c).sendMessage(RESPONDER_REPORT_NFC_HANDOVER, 0, c.putListener(listener), bundle);
    }

    public boolean setNfcTriggered(boolean enable) {
        try {
            this.mService.setNfcTriggered(enable);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public void connect(Channel c, WifiP2pConfig config, int freq, ActionListener listener) {
        checkChannel(c);
        checkP2pConfig(config);
        Channel.m85-get0(c).sendMessage(CONNECT, freq, c.putListener(listener), config);
    }

    public String getMacAddress() {
        try {
            return this.mService.getMacAddress();
        } catch (RemoteException e) {
            return null;
        }
    }

    public void requestWifiP2pLinkInfo(Channel c, String interfaceAddress, WifiP2pLinkInfoListener listener) {
        WifiP2pLinkInfo info = new WifiP2pLinkInfo();
        info.interfaceAddress = interfaceAddress;
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(REQUEST_LINK_INFO, 0, c.putListener(listener), info);
    }

    public void setP2pAutoChannel(Channel c, boolean enable, ActionListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(SET_AUTO_CHANNEL_SELECT, enable ? 1 : 0, c.putListener(listener));
    }

    public void deinitialize(Channel c) {
        Log.i(TAG, "deinitialize()");
        checkChannel(c);
        Channel.m93-wrap2(c);
    }

    public String getPeerIpAddress(String peerMacAddress) {
        try {
            return this.mService.getPeerIpAddress(peerMacAddress);
        } catch (RemoteException e) {
            return null;
        }
    }

    public void setGCInviteResult(Channel c, boolean accept, int goIntent, ActionListener listener) {
        checkChannel(c);
        if (accept) {
            Channel.m85-get0(c).sendMessage(PEER_CONNECTION_USER_ACCEPT_FROM_OUTER, goIntent, c.putListener(listener));
        } else {
            Channel.m85-get0(c).sendMessage(PEER_CONNECTION_USER_REJECT_FROM_OUTER, -1, c.putListener(listener));
        }
    }

    public void setGCInviteResult(Channel c, boolean accept, int goIntent, int pinMethod, String pinCode, ActionListener listener) {
        checkChannel(c);
        if (pinCode == null) {
            throw new IllegalArgumentException("pinCode needs to be configured");
        } else if (pinMethod == 2 || pinMethod == 1) {
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_PIN_CODE, pinCode);
            bundle.putInt(EXTRA_PIN_METHOD, pinMethod);
            if (accept) {
                Channel.m85-get0(c).sendMessage(PEER_CONNECTION_USER_ACCEPT_FROM_OUTER, goIntent, c.putListener(listener), bundle);
            } else {
                Channel.m85-get0(c).sendMessage(PEER_CONNECTION_USER_REJECT_FROM_OUTER, -1, c.putListener(listener), bundle);
            }
        } else {
            throw new IllegalArgumentException("pinMethod needs to be WpsInfo.KEYPAD/WpsInfo.DISPLAY");
        }
    }

    public void setFreqConflictExResult(Channel c, boolean accept, ActionListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(FREQ_CONFLICT_EX_RESULT, accept ? 1 : 0, c.putListener(listener));
    }

    public void removeClient(Channel c, String mac, ActionListener listener) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + "(), pid: " + Process.myPid() + ", tid: " + Process.myTid() + ", uid: " + Process.myUid());
        checkChannel(c);
        checkMac(mac);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CLIENT_MESSAGE, mac);
        Channel.m85-get0(c).sendMessage(REMOVE_CLIENT, 0, c.putListener(listener), bundle);
    }

    public void setCrossMountIE(boolean isAdd, String hexData) {
        try {
            this.mService.setCrossMountIE(isAdd, hexData);
        } catch (RemoteException e) {
        }
    }

    public void stopP2pFindOnly(Channel c, ActionListener listener) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + "(), pid: " + Process.myPid() + ", tid: " + Process.myTid() + ", uid: " + Process.myUid());
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(STOP_P2P_FIND_ONLY, 0, c.putListener(listener));
    }

    public void createGroup(Channel c, int netId, ActionListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(CREATE_GROUP, netId, c.putListener(listener));
    }

    public void discoverPeers(Channel c, int timeout, ActionListener listener) {
        checkChannel(c);
        Channel.m85-get0(c).sendMessage(DISCOVER_PEERS, timeout, c.putListener(listener));
    }

    public void addPersistentGroup(Channel c, Map<String, String> variables, AddPersistentGroupListener listener) {
        checkChannel(c);
        Bundle bundle = new Bundle();
        HashMap<String, String> hVariables = new HashMap();
        if (variables != null && (variables instanceof HashMap)) {
            hVariables = (HashMap) variables;
        } else if (variables != null) {
            hVariables.putAll(variables);
        }
        bundle.putSerializable("variables", hVariables);
        Channel.m85-get0(c).sendMessage(ADD_PERSISTENT_GROUP, 0, c.putListener(listener), bundle);
    }

    public void setBeamMode(int mode) {
        try {
            this.mService.setBeamMode(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}

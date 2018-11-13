package com.android.server.connectivity.tethering;

import android.net.INetworkStatsService;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.oppo.IElsaManager;

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
public class TetherInterfaceStateMachine extends StateMachine {
    private static final int BASE_IFACE = 327780;
    public static final int CMD_INTERFACE_DOWN = 327784;
    public static final int CMD_IPV6_TETHER_UPDATE = 327793;
    public static final int CMD_IP_FORWARDING_DISABLE_ERROR = 327788;
    public static final int CMD_IP_FORWARDING_ENABLE_ERROR = 327787;
    public static final int CMD_SET_DNS_FORWARDERS_ERROR = 327791;
    public static final int CMD_START_TETHERING_ERROR = 327789;
    public static final int CMD_STOP_TETHERING_ERROR = 327790;
    public static final int CMD_TETHER_CONNECTION_CHANGED = 327792;
    public static final int CMD_TETHER_REQUESTED = 327782;
    public static final int CMD_TETHER_UNREQUESTED = 327783;
    private static final boolean DBG = true;
    private static final String TAG = "TetherInterfaceSM";
    private static final String USB_NEAR_IFACE_ADDR = "192.168.42.129";
    private static final int USB_PREFIX_LENGTH = 24;
    private static final boolean VDBG = true;
    private static final String WIFI_HOST_IFACE_ADDR = "192.168.43.1";
    private static final int WIFI_HOST_IFACE_PREFIX_LENGTH = 24;
    private static final Class[] messageClasses = null;
    private static final SparseArray<String> sMagicDecoderRing = null;
    private final IPv6TetheringInterfaceServices mIPv6TetherSvc;
    private final String mIfaceName;
    private final State mInitialState;
    private final int mInterfaceType;
    private int mLastError;
    private MdDirectTethering mMdDirectTethering;
    private String mMyUpstreamIfaceName;
    private final INetworkManagementService mNMService;
    private final INetworkStatsService mStatsService;
    private final IControlsTethering mTetherController;
    private final State mTetheredState;
    private final State mUnavailableState;

    class InitialState extends State {
        InitialState() {
        }

        public void enter() {
            Log.d(TetherInterfaceStateMachine.TAG, "[ISM_Initial] enter, notifyInterfaceStateChange (" + TetherInterfaceStateMachine.this.mIfaceName + ",STATE_AVAILABLE," + TetherInterfaceStateMachine.this.mLastError + ")");
            TetherInterfaceStateMachine.this.mTetherController.notifyInterfaceStateChange(TetherInterfaceStateMachine.this.mIfaceName, TetherInterfaceStateMachine.this, 1, TetherInterfaceStateMachine.this.mLastError);
        }

        public boolean processMessage(Message message) {
            TetherInterfaceStateMachine.this.maybeLogMessage(this, message.what);
            Log.i(TetherInterfaceStateMachine.TAG, "[ISM_Initial] " + TetherInterfaceStateMachine.this.mIfaceName + " processMessage what=" + message.what);
            switch (message.what) {
                case TetherInterfaceStateMachine.CMD_TETHER_REQUESTED /*327782*/:
                    TetherInterfaceStateMachine.this.mLastError = 0;
                    TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mTetheredState);
                    return true;
                case TetherInterfaceStateMachine.CMD_INTERFACE_DOWN /*327784*/:
                    TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mUnavailableState);
                    return true;
                case TetherInterfaceStateMachine.CMD_IPV6_TETHER_UPDATE /*327793*/:
                    TetherInterfaceStateMachine.this.mIPv6TetherSvc.updateUpstreamIPv6LinkProperties((LinkProperties) message.obj);
                    return true;
                default:
                    return false;
            }
        }
    }

    class TetheredState extends State {
        TetheredState() {
        }

        public void enter() {
            Log.d(TetherInterfaceStateMachine.TAG, "[ISM_Tethered] enter");
            if (TetherInterfaceStateMachine.this.configureIfaceIp(true)) {
                try {
                    TetherInterfaceStateMachine.this.mNMService.tetherInterface(TetherInterfaceStateMachine.this.mIfaceName);
                    if (!TetherInterfaceStateMachine.this.mIPv6TetherSvc.start()) {
                        Log.e(TetherInterfaceStateMachine.TAG, "Failed to start IPv6TetheringInterfaceServices");
                    }
                    Log.d(TetherInterfaceStateMachine.TAG, "Tethered " + TetherInterfaceStateMachine.this.mIfaceName);
                    Log.d(TetherInterfaceStateMachine.TAG, "[ISM_Tethered] notifyInterfaceStateChange (" + TetherInterfaceStateMachine.this.mIfaceName + ",STATE_TETHERED," + TetherInterfaceStateMachine.this.mLastError + ")");
                    TetherInterfaceStateMachine.this.mTetherController.notifyInterfaceStateChange(TetherInterfaceStateMachine.this.mIfaceName, TetherInterfaceStateMachine.this, 2, TetherInterfaceStateMachine.this.mLastError);
                    return;
                } catch (Exception e) {
                    Log.e(TetherInterfaceStateMachine.TAG, "Error Tethering: " + e.toString());
                    TetherInterfaceStateMachine.this.mLastError = 6;
                    TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mInitialState);
                    return;
                }
            }
            TetherInterfaceStateMachine.this.mLastError = 10;
            TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mInitialState);
        }

        public void exit() {
            Log.i(TetherInterfaceStateMachine.TAG, "[ISM_Tethered] exit");
            TetherInterfaceStateMachine.this.mIPv6TetherSvc.stop();
            cleanupUpstream();
            try {
                TetherInterfaceStateMachine.this.mNMService.untetherInterface(TetherInterfaceStateMachine.this.mIfaceName);
            } catch (Exception ee) {
                TetherInterfaceStateMachine.this.mLastError = 7;
                Log.e(TetherInterfaceStateMachine.TAG, "Failed to untether interface: " + ee.toString());
            }
            TetherInterfaceStateMachine.this.configureIfaceIp(false);
            if (TetherInterfaceStateMachine.this.mMdDirectTethering != null && TetherInterfaceStateMachine.this.mMdDirectTethering.isMdtEnable(TetherInterfaceStateMachine.this.mInterfaceType)) {
                TetherInterfaceStateMachine.this.mMdDirectTethering.resetMdtInterface();
            }
        }

        private void cleanupUpstream() {
            Log.d(TetherInterfaceStateMachine.TAG, "cleanupUpstream() mMyUpstreamIfaceName:" + TetherInterfaceStateMachine.this.mMyUpstreamIfaceName);
            if (TetherInterfaceStateMachine.this.mMyUpstreamIfaceName != null) {
                try {
                    TetherInterfaceStateMachine.this.mStatsService.forceUpdate();
                } catch (Exception e) {
                    Log.e(TetherInterfaceStateMachine.TAG, "Exception in forceUpdate: " + e.toString());
                }
                try {
                    TetherInterfaceStateMachine.this.mNMService.stopInterfaceForwarding(TetherInterfaceStateMachine.this.mIfaceName, TetherInterfaceStateMachine.this.mMyUpstreamIfaceName);
                } catch (Exception e2) {
                    Log.e(TetherInterfaceStateMachine.TAG, "Exception in removeInterfaceForward: " + e2.toString());
                }
                try {
                    TetherInterfaceStateMachine.this.mNMService.disableNat(TetherInterfaceStateMachine.this.mIfaceName, TetherInterfaceStateMachine.this.mMyUpstreamIfaceName);
                    Log.d(TetherInterfaceStateMachine.TAG, "[ISM_Tethered] cleanupUpstream disableNat(" + TetherInterfaceStateMachine.this.mIfaceName + ", " + TetherInterfaceStateMachine.this.mMyUpstreamIfaceName + ")");
                    if (TetherInterfaceStateMachine.this.mInterfaceType == 1) {
                        Log.d(TetherInterfaceStateMachine.TAG, "[ISM_Tethered] enableUdpForwarding(false," + TetherInterfaceStateMachine.this.mIfaceName + ", " + TetherInterfaceStateMachine.this.mMyUpstreamIfaceName + ")");
                        TetherInterfaceStateMachine.this.mNMService.enableUdpForwarding(false, TetherInterfaceStateMachine.this.mIfaceName, TetherInterfaceStateMachine.this.mMyUpstreamIfaceName, IElsaManager.EMPTY_PACKAGE);
                    }
                } catch (Exception e22) {
                    Log.e(TetherInterfaceStateMachine.TAG, "Exception in disableNat: " + e22.toString());
                }
                TetherInterfaceStateMachine.this.mMyUpstreamIfaceName = null;
            }
        }

        public boolean processMessage(Message message) {
            TetherInterfaceStateMachine.this.maybeLogMessage(this, message.what);
            Log.i(TetherInterfaceStateMachine.TAG, "[ISM_Tethered] " + TetherInterfaceStateMachine.this.mIfaceName + " processMessage what=" + message.what);
            boolean retValue = true;
            switch (message.what) {
                case TetherInterfaceStateMachine.CMD_TETHER_UNREQUESTED /*327783*/:
                    TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mInitialState);
                    Log.d(TetherInterfaceStateMachine.TAG, "Untethered (unrequested)" + TetherInterfaceStateMachine.this.mIfaceName);
                    break;
                case TetherInterfaceStateMachine.CMD_INTERFACE_DOWN /*327784*/:
                    TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mUnavailableState);
                    Log.d(TetherInterfaceStateMachine.TAG, "Untethered (ifdown)" + TetherInterfaceStateMachine.this.mIfaceName);
                    break;
                case TetherInterfaceStateMachine.CMD_IP_FORWARDING_ENABLE_ERROR /*327787*/:
                case TetherInterfaceStateMachine.CMD_IP_FORWARDING_DISABLE_ERROR /*327788*/:
                case TetherInterfaceStateMachine.CMD_START_TETHERING_ERROR /*327789*/:
                case TetherInterfaceStateMachine.CMD_STOP_TETHERING_ERROR /*327790*/:
                case TetherInterfaceStateMachine.CMD_SET_DNS_FORWARDERS_ERROR /*327791*/:
                    TetherInterfaceStateMachine.this.mLastError = 5;
                    TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mInitialState);
                    break;
                case TetherInterfaceStateMachine.CMD_TETHER_CONNECTION_CHANGED /*327792*/:
                    String newUpstreamIfaceName = message.obj;
                    Log.i(TetherInterfaceStateMachine.TAG, "[ISM_Tethered] CMD_TETHER_CONNECTION_CHANGED mMyUpstreamIfaceName:" + TetherInterfaceStateMachine.this.mMyUpstreamIfaceName + ", newUpstreamIfaceName:" + newUpstreamIfaceName);
                    if ((TetherInterfaceStateMachine.this.mMyUpstreamIfaceName == null && newUpstreamIfaceName == null) || (TetherInterfaceStateMachine.this.mMyUpstreamIfaceName != null && TetherInterfaceStateMachine.this.mMyUpstreamIfaceName.equals(newUpstreamIfaceName))) {
                        Log.d(TetherInterfaceStateMachine.TAG, "Connection changed noop - dropping");
                        break;
                    }
                    cleanupUpstream();
                    if (TetherInterfaceStateMachine.this.mMdDirectTethering != null && TetherInterfaceStateMachine.this.mMdDirectTethering.isMdtEnable(TetherInterfaceStateMachine.this.mInterfaceType)) {
                        TetherInterfaceStateMachine.this.mMdDirectTethering.clearBridgeMac(TetherInterfaceStateMachine.this.mIfaceName);
                    }
                    if (newUpstreamIfaceName != null) {
                        try {
                            TetherInterfaceStateMachine.this.mNMService.enableNat(TetherInterfaceStateMachine.this.mIfaceName, newUpstreamIfaceName);
                            Log.d(TetherInterfaceStateMachine.TAG, "[ISM_Tethered] CMD_TETHER_CONNECTION_CHANGED enableNat(" + TetherInterfaceStateMachine.this.mIfaceName + ", " + newUpstreamIfaceName + ")");
                            TetherInterfaceStateMachine.this.mNMService.startInterfaceForwarding(TetherInterfaceStateMachine.this.mIfaceName, newUpstreamIfaceName);
                        } catch (Exception e) {
                            Log.e(TetherInterfaceStateMachine.TAG, "Exception enabling Nat: " + e.toString());
                            TetherInterfaceStateMachine.this.mLastError = 8;
                            TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mInitialState);
                            return true;
                        }
                    }
                    Log.d(TetherInterfaceStateMachine.TAG, "[ISM_Tethered] CMD_TETHER_CONNECTION_CHANGED finished! mMyUpstreamIfaceName to " + newUpstreamIfaceName);
                    TetherInterfaceStateMachine.this.mMyUpstreamIfaceName = newUpstreamIfaceName;
                    break;
                    break;
                case TetherInterfaceStateMachine.CMD_IPV6_TETHER_UPDATE /*327793*/:
                    TetherInterfaceStateMachine.this.mIPv6TetherSvc.updateUpstreamIPv6LinkProperties((LinkProperties) message.obj);
                    break;
                default:
                    retValue = false;
                    break;
            }
            return retValue;
        }
    }

    class UnavailableState extends State {
        UnavailableState() {
        }

        public void enter() {
            Log.d(TetherInterfaceStateMachine.TAG, "[UnavailableState] enter, notifyInterfaceStateChange (" + TetherInterfaceStateMachine.this.mIfaceName + ",STATE_UNAVAILABLE," + TetherInterfaceStateMachine.this.mLastError + ")");
            TetherInterfaceStateMachine.this.mLastError = 0;
            TetherInterfaceStateMachine.this.mTetherController.notifyInterfaceStateChange(TetherInterfaceStateMachine.this.mIfaceName, TetherInterfaceStateMachine.this, 0, TetherInterfaceStateMachine.this.mLastError);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.tethering.TetherInterfaceStateMachine.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.tethering.TetherInterfaceStateMachine.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.tethering.TetherInterfaceStateMachine.<clinit>():void");
    }

    public TetherInterfaceStateMachine(String ifaceName, Looper looper, int interfaceType, INetworkManagementService nMService, INetworkStatsService statsService, IControlsTethering tetherController) {
        super(ifaceName, looper);
        this.mNMService = nMService;
        this.mStatsService = statsService;
        this.mTetherController = tetherController;
        this.mIfaceName = ifaceName;
        this.mInterfaceType = interfaceType;
        this.mIPv6TetherSvc = new IPv6TetheringInterfaceServices(this.mIfaceName, this.mNMService);
        this.mLastError = 0;
        this.mInitialState = new InitialState();
        addState(this.mInitialState);
        this.mTetheredState = new TetheredState();
        addState(this.mTetheredState);
        this.mUnavailableState = new UnavailableState();
        addState(this.mUnavailableState);
        setInitialState(this.mInitialState);
    }

    public TetherInterfaceStateMachine(String ifaceName, Looper looper, int interfaceType, INetworkManagementService nMService, INetworkStatsService statsService, IControlsTethering tetherController, MdDirectTethering mdDirectTethering) {
        this(ifaceName, looper, interfaceType, nMService, statsService, tetherController);
        this.mMdDirectTethering = mdDirectTethering;
    }

    public int interfaceType() {
        return this.mInterfaceType;
    }

    public String upstreamIfaceName() {
        return this.mMyUpstreamIfaceName;
    }

    private boolean configureIfaceIp(boolean enabled) {
        String ipAsString;
        int prefixLen;
        Log.d(TAG, "configureIfaceIp(" + enabled + ")");
        if (this.mInterfaceType == 1) {
            ipAsString = USB_NEAR_IFACE_ADDR;
            prefixLen = 24;
        } else if (this.mInterfaceType != 0) {
            return true;
        } else {
            ipAsString = WIFI_HOST_IFACE_ADDR;
            prefixLen = 24;
        }
        try {
            InterfaceConfiguration ifcg = this.mNMService.getInterfaceConfig(this.mIfaceName);
            if (ifcg != null) {
                ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(ipAsString), prefixLen));
                if (enabled) {
                    ifcg.setInterfaceUp();
                } else {
                    ifcg.setInterfaceDown();
                }
                ifcg.clearFlag("running");
                Log.d(TAG, " setInterfaceConfig(" + this.mIfaceName + IElsaManager.EMPTY_PACKAGE);
                this.mNMService.setInterfaceConfig(this.mIfaceName, ifcg);
                if (this.mMdDirectTethering != null && this.mMdDirectTethering.isMdtEnable(this.mInterfaceType)) {
                    this.mMdDirectTethering.configureMdtIface(this.mIfaceName, enabled);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error configuring interface " + this.mIfaceName, e);
            return false;
        }
    }

    private void maybeLogMessage(State state, int what) {
        Log.d(TAG, state.getName() + " got " + ((String) sMagicDecoderRing.get(what, Integer.toString(what))) + ", Iface = " + this.mIfaceName);
    }
}

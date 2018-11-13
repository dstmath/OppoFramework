package com.android.server.connectivity;

import android.content.Context;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.RouteInfo;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.server.net.BaseNetworkObserver;
import java.net.Inet4Address;

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
public class Nat464Xlat extends BaseNetworkObserver {
    private static final String CLAT_PREFIX = "v4-";
    private static final int[] NETWORK_TYPES = null;
    private static final String TAG = "Nat464Xlat";
    private String mBaseIface;
    private final Handler mHandler;
    private String mIface;
    private boolean mIsRunning;
    private final INetworkManagementService mNMService;
    private final NetworkAgentInfo mNetwork;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.Nat464Xlat.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.Nat464Xlat.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.Nat464Xlat.<clinit>():void");
    }

    public Nat464Xlat(Context context, INetworkManagementService nmService, Handler handler, NetworkAgentInfo nai) {
        this.mNMService = nmService;
        this.mHandler = handler;
        this.mNetwork = nai;
    }

    public static boolean requiresClat(NetworkAgentInfo nai) {
        return (!nai.networkInfo.isConnected() || (nai.linkProperties != null ? nai.linkProperties.hasIPv4Address() : false)) ? false : ArrayUtils.contains(NETWORK_TYPES, nai.networkInfo.getType());
    }

    public boolean isStarted() {
        return this.mIface != null;
    }

    private void clear() {
        this.mIface = null;
        this.mBaseIface = null;
        this.mIsRunning = false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0097 A:{Splitter: B:18:0x008f, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:21:0x0097, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:0x0098, code:
            android.util.Slog.e(TAG, "Error starting clatd: " + r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void start() {
        if (isStarted()) {
            Slog.e(TAG, "startClat: already started");
        } else if (this.mNetwork.linkProperties == null) {
            Slog.e(TAG, "startClat: Can't start clat with null LinkProperties");
        } else {
            try {
                this.mNMService.registerObserver(this);
                this.mBaseIface = this.mNetwork.linkProperties.getInterfaceName();
                if (this.mBaseIface == null) {
                    Slog.e(TAG, "startClat: Can't start clat on null interface");
                    return;
                }
                this.mIface = CLAT_PREFIX + this.mBaseIface;
                Slog.i(TAG, "Starting clatd on " + this.mBaseIface);
                try {
                    this.mNMService.startClatd(this.mBaseIface);
                } catch (Exception e) {
                }
            } catch (RemoteException e2) {
                Slog.e(TAG, "startClat: Can't register interface observer for clat on " + this.mNetwork);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0017 A:{Splitter: B:3:0x000f, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0017, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0018, code:
            android.util.Slog.e(TAG, "Error stopping clatd: " + r0);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stop() {
        if (isStarted()) {
            Slog.i(TAG, "Stopping clatd");
            try {
                this.mNMService.stopClatd(this.mBaseIface);
            } catch (Exception e) {
            }
        } else {
            Slog.e(TAG, "clatd: already stopped");
        }
    }

    private void updateConnectivityService(LinkProperties lp) {
        Message msg = this.mHandler.obtainMessage(528387, lp);
        msg.replyTo = this.mNetwork.messenger;
        Slog.i(TAG, "sending message to ConnectivityService: " + msg);
        msg.sendToTarget();
    }

    public void fixupLinkProperties(LinkProperties oldLp) {
        if (this.mNetwork.clatd != null && this.mIsRunning && this.mNetwork.linkProperties != null && !this.mNetwork.linkProperties.getAllInterfaceNames().contains(this.mIface)) {
            Slog.d(TAG, "clatd running, updating NAI for " + this.mIface);
            for (LinkProperties stacked : oldLp.getStackedLinks()) {
                if (this.mIface.equals(stacked.getInterfaceName())) {
                    this.mNetwork.linkProperties.addStackedLink(stacked);
                    return;
                }
            }
        }
    }

    private LinkProperties makeLinkProperties(LinkAddress clatAddress) {
        LinkProperties stacked = new LinkProperties();
        stacked.setInterfaceName(this.mIface);
        stacked.addRoute(new RouteInfo(new LinkAddress(Inet4Address.ANY, 0), clatAddress.getAddress(), this.mIface));
        stacked.addLinkAddress(clatAddress);
        return stacked;
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000b A:{Splitter: B:0:0x0000, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:3:0x000b, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x000c, code:
            android.util.Slog.e(TAG, "Error getting link properties: " + r1);
     */
    /* JADX WARNING: Missing block: B:5:0x0027, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private LinkAddress getLinkAddress(String iface) {
        try {
            return this.mNMService.getInterfaceConfig(iface).getLinkAddress();
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0039 A:{Splitter: B:3:0x000c, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:10:0x0039, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x003a, code:
            android.util.Slog.w(TAG, "Changing IPv6 ND offload on " + r5 + "failed: " + r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void maybeSetIpv6NdOffload(String iface, boolean on) {
        if (this.mNetwork.networkInfo.getType() == 1) {
            try {
                Slog.d(TAG, (on ? "En" : "Dis") + "abling ND offload on " + iface);
                this.mNMService.setInterfaceIpv6NdOffload(iface, on);
            } catch (Exception e) {
            }
        }
    }

    public void interfaceLinkStateChanged(String iface, boolean up) {
        if (isStarted() && up && this.mIface.equals(iface)) {
            Slog.i(TAG, "interface " + iface + " is up, mIsRunning " + this.mIsRunning + "->true");
            if (!this.mIsRunning) {
                LinkAddress clatAddress = getLinkAddress(iface);
                if (clatAddress != null) {
                    this.mIsRunning = true;
                    maybeSetIpv6NdOffload(this.mBaseIface, false);
                    LinkProperties lp = new LinkProperties(this.mNetwork.linkProperties);
                    lp.addStackedLink(makeLinkProperties(clatAddress));
                    Slog.i(TAG, "Adding stacked link " + this.mIface + " on top of " + this.mBaseIface);
                    updateConnectivityService(lp);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0067 A:{Splitter: B:6:0x0040, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void interfaceRemoved(String iface) {
        if (isStarted() && this.mIface.equals(iface)) {
            Slog.i(TAG, "interface " + iface + " removed, mIsRunning " + this.mIsRunning + "->false");
            if (this.mIsRunning) {
                try {
                    this.mNMService.unregisterObserver(this);
                    this.mNMService.stopClatd(this.mBaseIface);
                } catch (RemoteException e) {
                }
                maybeSetIpv6NdOffload(this.mBaseIface, true);
                LinkProperties lp = new LinkProperties(this.mNetwork.linkProperties);
                lp.removeStackedLink(this.mIface);
                clear();
                updateConnectivityService(lp);
            }
        }
    }
}

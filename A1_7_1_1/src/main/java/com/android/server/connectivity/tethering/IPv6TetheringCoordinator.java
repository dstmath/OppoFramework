package com.android.server.connectivity.tethering;

import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkState;
import android.net.RouteInfo;
import android.util.Log;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

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
public class IPv6TetheringCoordinator {
    private static final boolean DBG = true;
    private static final String TAG = null;
    private static final boolean VDBG = false;
    private final LinkedList<TetherInterfaceStateMachine> mActiveDownstreams;
    private final ArrayList<TetherInterfaceStateMachine> mNotifyList;
    private NetworkState mUpstreamNetworkState;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.tethering.IPv6TetheringCoordinator.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.tethering.IPv6TetheringCoordinator.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.tethering.IPv6TetheringCoordinator.<clinit>():void");
    }

    public IPv6TetheringCoordinator(ArrayList<TetherInterfaceStateMachine> notifyList) {
        this.mNotifyList = notifyList;
        this.mActiveDownstreams = new LinkedList();
    }

    public void addActiveDownstream(TetherInterfaceStateMachine downstream) {
        Log.d(TAG, "addActiveDownstream: " + downstream);
        if (this.mActiveDownstreams.indexOf(downstream) == -1) {
            this.mActiveDownstreams.offer(downstream);
            updateIPv6TetheringInterfaces();
        }
    }

    public void removeActiveDownstream(TetherInterfaceStateMachine downstream) {
        Log.d(TAG, "removeActiveDownstream: " + downstream);
        stopIPv6TetheringOn(downstream);
        if (this.mActiveDownstreams.remove(downstream)) {
            updateIPv6TetheringInterfaces();
        }
    }

    public void updateUpstreamNetworkState(NetworkState ns) {
        if (canTetherIPv6(ns)) {
            if (!(this.mUpstreamNetworkState == null || ns.network.equals(this.mUpstreamNetworkState.network))) {
                stopIPv6TetheringOnAllInterfaces();
            }
            setUpstreamNetworkState(ns);
            updateIPv6TetheringInterfaces();
            return;
        }
        stopIPv6TetheringOnAllInterfaces();
        setUpstreamNetworkState(null);
    }

    private void stopIPv6TetheringOnAllInterfaces() {
        for (TetherInterfaceStateMachine sm : this.mNotifyList) {
            stopIPv6TetheringOn(sm);
        }
    }

    private void setUpstreamNetworkState(NetworkState ns) {
        if (ns == null) {
            this.mUpstreamNetworkState = null;
        } else {
            this.mUpstreamNetworkState = new NetworkState(null, new LinkProperties(ns.linkProperties), new NetworkCapabilities(ns.networkCapabilities), new Network(ns.network), null, null);
        }
        Log.d(TAG, "setUpstreamNetworkState: " + toDebugString(this.mUpstreamNetworkState));
    }

    private void updateIPv6TetheringInterfaces() {
        Iterator sm$iterator = this.mNotifyList.iterator();
        if (sm$iterator.hasNext()) {
            TetherInterfaceStateMachine sm = (TetherInterfaceStateMachine) sm$iterator.next();
            LinkProperties lp = getInterfaceIPv6LinkProperties(sm);
            Log.d(TAG, "updateIPv6TetheringInterfaces() sendMessage(CMD_IPV6_TETHER_UPDATE) " + sm + " lp:" + lp);
            sm.sendMessage(TetherInterfaceStateMachine.CMD_IPV6_TETHER_UPDATE, 0, 0, lp);
        }
    }

    private LinkProperties getInterfaceIPv6LinkProperties(TetherInterfaceStateMachine sm) {
        if (this.mUpstreamNetworkState == null || sm.interfaceType() == 2) {
            return null;
        }
        TetherInterfaceStateMachine currentActive = (TetherInterfaceStateMachine) this.mActiveDownstreams.peek();
        if (currentActive != null && currentActive == sm) {
            LinkProperties lp = getIPv6OnlyLinkProperties(this.mUpstreamNetworkState.linkProperties);
            if (lp.hasIPv6DefaultRoute() && hasGlobalIPv6Address(lp)) {
                return lp;
            }
            return null;
        }
        return null;
    }

    private static boolean canTetherIPv6(NetworkState ns) {
        boolean canTether;
        boolean supportedConfiguration;
        boolean outcome = false;
        if (ns == null || ns.network == null || ns.linkProperties == null || ns.networkCapabilities == null || !((ns.linkProperties.isProvisioned() || ns.linkProperties.hasIPv6DefaultRoute()) && hasGlobalIPv6Address(ns.linkProperties))) {
            canTether = false;
        } else {
            canTether = ns.networkCapabilities.hasTransport(0);
        }
        Log.d(TAG, "canTether:" + canTether);
        if (ns != null) {
            Log.d(TAG, "LP hasGlobalIPv6Address:" + hasGlobalIPv6Address(ns.linkProperties) + " isProvisioned():" + ns.linkProperties.isProvisioned());
        }
        RouteInfo routeInfo = null;
        RouteInfo v6default = null;
        if (canTether) {
            for (RouteInfo r : ns.linkProperties.getAllRoutes()) {
                if (r.isIPv4Default()) {
                    routeInfo = r;
                } else if (r.isIPv6Default()) {
                    v6default = r;
                }
                if (routeInfo != null && v6default != null) {
                    break;
                }
            }
        }
        Log.d(TAG, "v4default:" + routeInfo + " v6default:" + v6default);
        if (routeInfo == null || v6default == null || routeInfo.getInterface() == null) {
            supportedConfiguration = false;
        } else {
            supportedConfiguration = routeInfo.getInterface().equals(v6default.getInterface());
        }
        if (canTether) {
            outcome = supportedConfiguration;
        }
        if (outcome || routeInfo != null || v6default == null) {
            return outcome;
        }
        Log.d(TAG, "Use IPv6 only");
        return true;
    }

    private static LinkProperties getIPv6OnlyLinkProperties(LinkProperties lp) {
        LinkProperties v6only = new LinkProperties();
        if (lp == null) {
            return v6only;
        }
        v6only.setInterfaceName(lp.getInterfaceName());
        v6only.setMtu(lp.getMtu());
        for (LinkAddress linkAddr : lp.getLinkAddresses()) {
            if (isIPv6GlobalAddress(linkAddr.getAddress()) && linkAddr.getPrefixLength() == 64) {
                v6only.addLinkAddress(linkAddr);
            }
        }
        for (RouteInfo routeInfo : lp.getRoutes()) {
            IpPrefix destination = routeInfo.getDestination();
            if ((destination.getAddress() instanceof Inet6Address) && destination.getPrefixLength() <= 64) {
                v6only.addRoute(routeInfo);
            }
        }
        for (InetAddress dnsServer : lp.getDnsServers()) {
            if (isIPv6GlobalAddress(dnsServer)) {
                v6only.addDnsServer(dnsServer);
            }
        }
        v6only.setDomains(lp.getDomains());
        return v6only;
    }

    private static boolean isIPv6GlobalAddress(InetAddress ip) {
        if (!(ip instanceof Inet6Address) || ip.isAnyLocalAddress() || ip.isLoopbackAddress() || ip.isLinkLocalAddress() || ip.isSiteLocalAddress() || ip.isMulticastAddress()) {
            return false;
        }
        return true;
    }

    private static String toDebugString(NetworkState ns) {
        if (ns == null) {
            return "NetworkState{null}";
        }
        try {
            Object[] objArr = new Object[3];
            objArr[0] = ns.network;
            objArr[1] = ns.networkCapabilities;
            objArr[2] = ns.linkProperties;
            return String.format("NetworkState{%s, %s, %s}", objArr);
        } catch (Exception e) {
            return "NetworkState:" + e;
        }
    }

    private static void stopIPv6TetheringOn(TetherInterfaceStateMachine sm) {
        Log.d(TAG, "stopIPv6TetheringOn() sendMessage(CMD_IPV6_TETHER_UPDATE) " + sm + " lp=null");
        sm.sendMessage(TetherInterfaceStateMachine.CMD_IPV6_TETHER_UPDATE, 0, 0, null);
    }

    private static boolean hasGlobalIPv6Address(LinkProperties linkProperties) {
        if (linkProperties == null) {
            return false;
        }
        for (InetAddress address : linkProperties.getAddresses()) {
            if ((address instanceof Inet6Address) && isIPv6GlobalAddress(address)) {
                return true;
            }
        }
        return false;
    }
}

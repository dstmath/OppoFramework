package com.android.server.connectivity.tethering;

import android.net.INetd;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.RouteInfo;
import android.net.ip.RouterAdvertisementDaemon;
import android.net.ip.RouterAdvertisementDaemon.RaParams;
import android.net.util.NetdService;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Log;
import android.util.Slog;
import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class IPv6TetheringInterfaceServices {
    private static final IpPrefix LINK_LOCAL_PREFIX = null;
    private static final int RFC7421_IP_PREFIX_LENGTH = 64;
    private static final String TAG = null;
    private static final boolean VDBG = true;
    private byte[] mHwAddr;
    private final String mIfName;
    private LinkProperties mLastIPv6LinkProperties;
    private RaParams mLastRaParams;
    private final INetworkManagementService mNMService;
    private NetworkInterface mNetworkInterface;
    private RouterAdvertisementDaemon mRaDaemon;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.tethering.IPv6TetheringInterfaceServices.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.tethering.IPv6TetheringInterfaceServices.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.tethering.IPv6TetheringInterfaceServices.<clinit>():void");
    }

    IPv6TetheringInterfaceServices(String ifname, INetworkManagementService nms) {
        this.mIfName = ifname;
        this.mNMService = nms;
    }

    public boolean start() {
        Log.d(TAG, "start()");
        try {
            this.mNetworkInterface = NetworkInterface.getByName(this.mIfName);
            if (this.mNetworkInterface == null) {
                Log.w(TAG, "Failed to find NetworkInterface for " + this.mIfName);
                stop();
                return false;
            }
            try {
                this.mHwAddr = this.mNetworkInterface.getHardwareAddress();
                this.mRaDaemon = new RouterAdvertisementDaemon(this.mIfName, this.mNetworkInterface.getIndex(), this.mHwAddr);
                if (this.mRaDaemon.start()) {
                    return true;
                }
                stop();
                return false;
            } catch (SocketException e) {
                Log.e(TAG, "Failed to find hardware address for " + this.mIfName, e);
                stop();
                return false;
            }
        } catch (SocketException e2) {
            Log.e(TAG, "Failed to find NetworkInterface for " + this.mIfName, e2);
            stop();
            return false;
        }
    }

    public void stop() {
        Log.d(TAG, "stop()");
        this.mNetworkInterface = null;
        this.mHwAddr = null;
        setRaParams(null);
        if (this.mRaDaemon != null) {
            this.mRaDaemon.stop();
            this.mRaDaemon = null;
        }
    }

    public void updateUpstreamIPv6LinkProperties(LinkProperties v6only) {
        Log.d(TAG, "updateUpstreamIPv6LinkProperties() mRaDaemon:" + this.mRaDaemon);
        if (this.mRaDaemon != null && !Objects.equals(this.mLastIPv6LinkProperties, v6only)) {
            RaParams params = null;
            if (v6only != null) {
                params = new RaParams();
                params.mtu = v6only.getMtu();
                params.hasDefaultRoute = v6only.hasIPv6DefaultRoute();
                for (LinkAddress linkAddr : v6only.getLinkAddresses()) {
                    if (linkAddr.getPrefixLength() == 64) {
                        IpPrefix prefix = new IpPrefix(linkAddr.getAddress(), linkAddr.getPrefixLength());
                        params.prefixes.add(prefix);
                        Log.d(TAG, "params.prefixes.add(" + prefix);
                        Inet6Address dnsServer = getLocalDnsIpFor(prefix);
                        if (dnsServer != null) {
                            params.dnses.add(dnsServer);
                            Log.d(TAG, "params.dnses.add(" + dnsServer);
                        }
                    }
                }
            }
            setRaParams(params);
            this.mLastIPv6LinkProperties = v6only;
        }
    }

    private void configureLocalRoutes(HashSet<IpPrefix> deprecatedPrefixes, HashSet<IpPrefix> newPrefixes) {
        if (!deprecatedPrefixes.isEmpty()) {
            ArrayList<RouteInfo> toBeRemoved = getLocalRoutesFor(deprecatedPrefixes);
            try {
                Log.i(TAG, "configureLocalRoutes toBeRemoved:" + toBeRemoved);
                int removalFailures = this.mNMService.removeRoutesFromLocalNetwork(toBeRemoved);
                if (removalFailures > 0) {
                    String str = TAG;
                    Object[] objArr = new Object[1];
                    objArr[0] = Integer.valueOf(removalFailures);
                    Log.e(str, String.format("Failed to remove %d IPv6 routes from local table.", objArr));
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to remove IPv6 routes from local table: ", e);
            } catch (IllegalStateException e2) {
                Log.e(TAG, "Failed to removeRoutesFromLocalNetwork : ", e2);
            }
        }
        if (newPrefixes != null && !newPrefixes.isEmpty()) {
            HashSet<IpPrefix> addedPrefixes = (HashSet) newPrefixes.clone();
            if (this.mLastRaParams != null) {
                addedPrefixes.removeAll(this.mLastRaParams.prefixes);
            }
            if (this.mLastRaParams == null || this.mLastRaParams.prefixes.isEmpty()) {
                addedPrefixes.add(LINK_LOCAL_PREFIX);
            }
            if (!addedPrefixes.isEmpty()) {
                ArrayList<RouteInfo> toBeAdded = getLocalRoutesFor(addedPrefixes);
                try {
                    Log.i(TAG, "configureLocalRoutes toBeAdded:" + toBeAdded);
                    this.mNMService.addInterfaceToLocalNetwork(this.mIfName, toBeAdded);
                } catch (RemoteException e3) {
                    Log.e(TAG, "Failed to add IPv6 routes to local table: ", e3);
                } catch (IllegalStateException e22) {
                    Log.e(TAG, "Failed to addInterfaceToLocalNetwork : ", e22);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x004f A:{Splitter: B:12:0x002e, ExcHandler: android.os.ServiceSpecificException (r4_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00bc A:{Splitter: B:30:0x009b, ExcHandler: android.os.ServiceSpecificException (r4_1 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00da A:{Splitter: B:20:0x0072, ExcHandler: android.os.ServiceSpecificException (e android.os.ServiceSpecificException)} */
    /* JADX WARNING: Missing block: B:15:0x004f, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:16:0x0050, code:
            android.util.Log.e(TAG, "Failed to remove local dns IP: " + r3, r4);
     */
    /* JADX WARNING: Missing block: B:33:0x00bc, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:34:0x00bd, code:
            android.util.Log.e(TAG, "Failed to add local dns IP: " + r3, r4);
            r11.remove(r1);
     */
    /* JADX WARNING: Missing block: B:36:0x00db, code:
            android.util.Log.e(TAG, "Failed to update local DNS caching server");
     */
    /* JADX WARNING: Missing block: B:37:0x00e3, code:
            if (r11 != null) goto L_0x00e5;
     */
    /* JADX WARNING: Missing block: B:38:0x00e5, code:
            r11.clear();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void configureLocalDns(HashSet<Inet6Address> deprecatedDnses, HashSet<Inet6Address> newDnses) {
        INetd netd = NetdService.getInstance();
        if (netd == null) {
            if (newDnses != null) {
                newDnses.clear();
            }
            Log.e(TAG, "No netd service instance available; not setting local IPv6 addresses");
            return;
        }
        String dnsString;
        if (!deprecatedDnses.isEmpty()) {
            for (Inet6Address dns : deprecatedDnses) {
                dnsString = dns.getHostAddress();
                try {
                    Log.i(TAG, "configureLocalDns netd.interfaceDelAddress:" + dnsString);
                    netd.interfaceDelAddress(this.mIfName, dnsString, 64);
                } catch (Exception e) {
                }
            }
        }
        if (!(newDnses == null || newDnses.isEmpty())) {
            HashSet<Inet6Address> addedDnses = (HashSet) newDnses.clone();
            if (this.mLastRaParams != null) {
                addedDnses.removeAll(this.mLastRaParams.dnses);
            }
            for (Inet6Address dns2 : addedDnses) {
                dnsString = dns2.getHostAddress();
                try {
                    Log.i(TAG, "configureLocalDns netd.interfaceAddAddress:" + dnsString);
                    netd.interfaceAddAddress(this.mIfName, dnsString, 64);
                } catch (Exception e2) {
                }
            }
        }
        try {
            netd.tetherApplyDnsInterfaces();
        } catch (ServiceSpecificException e3) {
        }
    }

    private void setRaParams(RaParams newParams) {
        HashSet hashSet = null;
        if (this.mRaDaemon != null) {
            HashSet hashSet2;
            RaParams deprecatedParams = RaParams.getDeprecatedRaParams(this.mLastRaParams, newParams);
            HashSet hashSet3 = deprecatedParams.prefixes;
            if (newParams != null) {
                hashSet2 = newParams.prefixes;
            } else {
                hashSet2 = null;
            }
            configureLocalRoutes(hashSet3, hashSet2);
            hashSet2 = deprecatedParams.dnses;
            if (newParams != null) {
                hashSet = newParams.dnses;
            }
            configureLocalDns(hashSet2, hashSet);
            this.mRaDaemon.buildNewRa(deprecatedParams, newParams);
        }
        this.mLastRaParams = newParams;
    }

    private ArrayList<RouteInfo> getLocalRoutesFor(HashSet<IpPrefix> prefixes) {
        ArrayList<RouteInfo> localRoutes = new ArrayList();
        for (IpPrefix ipp : prefixes) {
            localRoutes.add(new RouteInfo(ipp, null, this.mIfName));
        }
        return localRoutes;
    }

    private static Inet6Address getLocalDnsIpFor(IpPrefix localPrefix) {
        byte[] dnsBytes = localPrefix.getRawAddress();
        dnsBytes[dnsBytes.length - 1] = (byte) 1;
        try {
            return Inet6Address.getByAddress(null, dnsBytes, 0);
        } catch (UnknownHostException e) {
            Slog.wtf(TAG, "Failed to construct Inet6Address from: " + localPrefix);
            return null;
        }
    }
}

package com.android.server.connectivity.tethering;

import android.net.LinkProperties;
import android.net.NetworkState;
import android.net.RouteInfo;
import android.net.util.InterfaceSet;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

public final class TetheringInterfaceUtils {
    public static InterfaceSet getTetheringInterfaces(NetworkState ns) {
        if (ns == null) {
            return null;
        }
        String if4 = getInterfaceForDestination(ns.linkProperties, Inet4Address.ANY);
        String if6 = getIPv6Interface(ns);
        if (if4 == null && if6 == null) {
            return null;
        }
        return new InterfaceSet(if4, if6);
    }

    public static String getIPv6Interface(NetworkState ns) {
        boolean canTether = false;
        if (!(ns == null || ns.network == null || ns.linkProperties == null || ns.networkCapabilities == null || !ns.linkProperties.hasIpv6DnsServer() || !ns.linkProperties.hasGlobalIpv6Address() || !ns.networkCapabilities.hasTransport(0))) {
            canTether = true;
        }
        if (canTether) {
            return getInterfaceForDestination(ns.linkProperties, Inet6Address.ANY);
        }
        return null;
    }

    private static String getInterfaceForDestination(LinkProperties lp, InetAddress dst) {
        RouteInfo ri;
        if (lp != null) {
            ri = RouteInfo.selectBestRoute(lp.getAllRoutes(), dst);
        } else {
            ri = null;
        }
        if (ri != null) {
            return ri.getInterface();
        }
        return null;
    }
}

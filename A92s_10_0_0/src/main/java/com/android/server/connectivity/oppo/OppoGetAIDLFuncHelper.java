package com.android.server.connectivity.oppo;

import android.net.IDnsResolver;
import android.net.INetd;
import android.util.Log;
import com.android.server.connectivity.util.ReflectionHelper;

public class OppoGetAIDLFuncHelper {
    public static final String TAG = "OppoGetAIDLFuncHelper";

    public static boolean setOppoSlaEnable(INetd inetd) {
        if (inetd == null) {
            return false;
        }
        try {
            return ((Boolean) ReflectionHelper.callMethod(inetd, "android.net.INetd", "setOppoSlaEnable", new Class[0], new Object[0])).booleanValue();
        } catch (SecurityException ex) {
            Log.d(TAG, "<callMethod> error:", ex);
            return false;
        }
    }

    public static boolean setOppoSlaDisable(INetd inetd) {
        if (inetd == null) {
            return false;
        }
        try {
            return ((Boolean) ReflectionHelper.callMethod(inetd, "android.net.INetd", "setOppoSlaDisable", new Class[0], new Object[0])).booleanValue();
        } catch (SecurityException ex) {
            Log.d(TAG, "<callMethod> error:", ex);
            return false;
        }
    }

    public static void setOppoSlaIfaceUp(INetd inetd, String networkType, String ifaceName, String ipAddr, String ipMask, String dns) {
        if (inetd != null) {
            try {
                ReflectionHelper.callMethod(inetd, "android.net.INetd", "setOppoSlaIfaceUp", new Class[]{String.class, String.class, String.class, String.class, String.class}, new Object[]{networkType, ifaceName, ipAddr, ipMask, dns});
            } catch (SecurityException ex) {
                Log.d(TAG, "<callMethod> error:", ex);
            }
        }
    }

    public static void setOppoSlaIfaceDown(INetd inetd, String networkType, String ifaceName) {
        if (inetd != null) {
            try {
                ReflectionHelper.callMethod(inetd, "android.net.INetd", "setOppoSlaIfaceDown", new Class[]{String.class, String.class}, new Object[]{networkType, ifaceName});
            } catch (SecurityException ex) {
                Log.d(TAG, "<callMethod> error:", ex);
            }
        }
    }

    public static void setIPv6DnsConfiguation(INetd inetd, int netId, boolean enableSimpleIpv6Query, int ipv6Retries, int ipv6Timeout) {
        if (inetd != null) {
            try {
                ReflectionHelper.callMethod(inetd, "android.net.INetd", "setIPv6DnsConfiguation", new Class[]{Integer.TYPE, Boolean.TYPE, Integer.TYPE, Integer.TYPE}, new Object[]{Integer.valueOf(netId), Boolean.valueOf(enableSimpleIpv6Query), Integer.valueOf(ipv6Retries), Integer.valueOf(ipv6Timeout)});
            } catch (SecurityException ex) {
                Log.d(TAG, "<callMethod> error:", ex);
            }
        }
    }

    public static void resolveFlushCacheForNet(IDnsResolver dnsresolver, int netId) {
        if (dnsresolver != null) {
            try {
                ReflectionHelper.callMethod(dnsresolver, "android.net.IDnsResolver", "resolveFlushCacheForNet", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(netId)});
            } catch (SecurityException ex) {
                Log.d(TAG, "<callMethod> error:", ex);
            }
        }
    }
}

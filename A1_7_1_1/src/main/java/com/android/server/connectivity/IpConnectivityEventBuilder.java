package com.android.server.connectivity;

import android.net.ConnectivityMetricsEvent;
import android.net.metrics.ApfProgramEvent;
import android.net.metrics.ApfStats;
import android.net.metrics.DefaultNetworkEvent;
import android.net.metrics.DhcpClientEvent;
import android.net.metrics.DhcpErrorEvent;
import android.net.metrics.DnsEvent;
import android.net.metrics.IpManagerEvent;
import android.net.metrics.IpReachabilityEvent;
import android.net.metrics.NetworkEvent;
import android.net.metrics.RaEvent;
import android.net.metrics.ValidationProbeEvent;
import android.os.Parcelable;
import com.android.server.connectivity.metrics.IpConnectivityLogClass;
import com.android.server.connectivity.metrics.IpConnectivityLogClass.ApfStatistics;
import com.android.server.connectivity.metrics.IpConnectivityLogClass.DHCPEvent;
import com.android.server.connectivity.metrics.IpConnectivityLogClass.DNSLookupBatch;
import com.android.server.connectivity.metrics.IpConnectivityLogClass.IpConnectivityEvent;
import com.android.server.connectivity.metrics.IpConnectivityLogClass.IpConnectivityLog;
import com.android.server.connectivity.metrics.IpConnectivityLogClass.IpProvisioningEvent;
import com.android.server.connectivity.metrics.IpConnectivityLogClass.NetworkId;
import com.google.protobuf.nano.MessageNano;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class IpConnectivityEventBuilder {
    private IpConnectivityEventBuilder() {
    }

    public static byte[] serialize(int dropped, List<ConnectivityMetricsEvent> events) throws IOException {
        IpConnectivityLog log = new IpConnectivityLog();
        log.events = toProto((List) events);
        log.droppedEvents = dropped;
        return MessageNano.toByteArray(log);
    }

    public static IpConnectivityEvent[] toProto(List<ConnectivityMetricsEvent> eventsIn) {
        ArrayList<IpConnectivityEvent> eventsOut = new ArrayList(eventsIn.size());
        for (ConnectivityMetricsEvent in : eventsIn) {
            IpConnectivityEvent out = toProto(in);
            if (out != null) {
                eventsOut.add(out);
            }
        }
        return (IpConnectivityEvent[]) eventsOut.toArray(new IpConnectivityEvent[eventsOut.size()]);
    }

    public static IpConnectivityEvent toProto(ConnectivityMetricsEvent ev) {
        IpConnectivityEvent out = new IpConnectivityEvent();
        if (!setEvent(out, ev.data)) {
            return null;
        }
        out.timeMs = ev.timestamp;
        return out;
    }

    private static boolean setEvent(IpConnectivityEvent out, Parcelable in) {
        if (in instanceof DhcpErrorEvent) {
            setDhcpErrorEvent(out, (DhcpErrorEvent) in);
            return true;
        } else if (in instanceof DhcpClientEvent) {
            setDhcpClientEvent(out, (DhcpClientEvent) in);
            return true;
        } else if (in instanceof DnsEvent) {
            setDnsEvent(out, (DnsEvent) in);
            return true;
        } else if (in instanceof IpManagerEvent) {
            setIpManagerEvent(out, (IpManagerEvent) in);
            return true;
        } else if (in instanceof IpReachabilityEvent) {
            setIpReachabilityEvent(out, (IpReachabilityEvent) in);
            return true;
        } else if (in instanceof DefaultNetworkEvent) {
            setDefaultNetworkEvent(out, (DefaultNetworkEvent) in);
            return true;
        } else if (in instanceof NetworkEvent) {
            setNetworkEvent(out, (NetworkEvent) in);
            return true;
        } else if (in instanceof ValidationProbeEvent) {
            setValidationProbeEvent(out, (ValidationProbeEvent) in);
            return true;
        } else if (in instanceof ApfProgramEvent) {
            setApfProgramEvent(out, (ApfProgramEvent) in);
            return true;
        } else if (in instanceof ApfStats) {
            setApfStats(out, (ApfStats) in);
            return true;
        } else if (!(in instanceof RaEvent)) {
            return false;
        } else {
            setRaEvent(out, (RaEvent) in);
            return true;
        }
    }

    private static void setDhcpErrorEvent(IpConnectivityEvent out, DhcpErrorEvent in) {
        out.dhcpEvent = new DHCPEvent();
        out.dhcpEvent.ifName = in.ifName;
        out.dhcpEvent.errorCode = in.errorCode;
    }

    private static void setDhcpClientEvent(IpConnectivityEvent out, DhcpClientEvent in) {
        out.dhcpEvent = new DHCPEvent();
        out.dhcpEvent.ifName = in.ifName;
        out.dhcpEvent.stateTransition = in.msg;
        out.dhcpEvent.durationMs = in.durationMs;
    }

    private static void setDnsEvent(IpConnectivityEvent out, DnsEvent in) {
        out.dnsLookupBatch = new DNSLookupBatch();
        out.dnsLookupBatch.networkId = netIdOf(in.netId);
        out.dnsLookupBatch.eventTypes = bytesToInts(in.eventTypes);
        out.dnsLookupBatch.returnCodes = bytesToInts(in.returnCodes);
        out.dnsLookupBatch.latenciesMs = in.latenciesMs;
    }

    private static void setIpManagerEvent(IpConnectivityEvent out, IpManagerEvent in) {
        out.ipProvisioningEvent = new IpProvisioningEvent();
        out.ipProvisioningEvent.ifName = in.ifName;
        out.ipProvisioningEvent.eventType = in.eventType;
        out.ipProvisioningEvent.latencyMs = (int) in.durationMs;
    }

    private static void setIpReachabilityEvent(IpConnectivityEvent out, IpReachabilityEvent in) {
        out.ipReachabilityEvent = new IpConnectivityLogClass.IpReachabilityEvent();
        out.ipReachabilityEvent.ifName = in.ifName;
        out.ipReachabilityEvent.eventType = in.eventType;
    }

    private static void setDefaultNetworkEvent(IpConnectivityEvent out, DefaultNetworkEvent in) {
        out.defaultNetworkEvent = new IpConnectivityLogClass.DefaultNetworkEvent();
        out.defaultNetworkEvent.networkId = netIdOf(in.netId);
        out.defaultNetworkEvent.previousNetworkId = netIdOf(in.prevNetId);
        out.defaultNetworkEvent.transportTypes = in.transportTypes;
        out.defaultNetworkEvent.previousNetworkIpSupport = ipSupportOf(in);
    }

    private static void setNetworkEvent(IpConnectivityEvent out, NetworkEvent in) {
        out.networkEvent = new IpConnectivityLogClass.NetworkEvent();
        out.networkEvent.networkId = netIdOf(in.netId);
        out.networkEvent.eventType = in.eventType;
        out.networkEvent.latencyMs = (int) in.durationMs;
    }

    private static void setValidationProbeEvent(IpConnectivityEvent out, ValidationProbeEvent in) {
        out.validationProbeEvent = new IpConnectivityLogClass.ValidationProbeEvent();
        out.validationProbeEvent.networkId = netIdOf(in.netId);
        out.validationProbeEvent.latencyMs = (int) in.durationMs;
        out.validationProbeEvent.probeType = in.probeType;
        out.validationProbeEvent.probeResult = in.returnCode;
    }

    private static void setApfProgramEvent(IpConnectivityEvent out, ApfProgramEvent in) {
        out.apfProgramEvent = new IpConnectivityLogClass.ApfProgramEvent();
        out.apfProgramEvent.lifetime = in.lifetime;
        out.apfProgramEvent.filteredRas = in.filteredRas;
        out.apfProgramEvent.currentRas = in.currentRas;
        out.apfProgramEvent.programLength = in.programLength;
        if (isBitSet(in.flags, 0)) {
            out.apfProgramEvent.dropMulticast = true;
        }
        if (isBitSet(in.flags, 1)) {
            out.apfProgramEvent.hasIpv4Addr = true;
        }
    }

    private static void setApfStats(IpConnectivityEvent out, ApfStats in) {
        out.apfStatistics = new ApfStatistics();
        out.apfStatistics.durationMs = in.durationMs;
        out.apfStatistics.receivedRas = in.receivedRas;
        out.apfStatistics.matchingRas = in.matchingRas;
        out.apfStatistics.droppedRas = in.droppedRas;
        out.apfStatistics.zeroLifetimeRas = in.zeroLifetimeRas;
        out.apfStatistics.parseErrors = in.parseErrors;
        out.apfStatistics.programUpdates = in.programUpdates;
        out.apfStatistics.maxProgramSize = in.maxProgramSize;
    }

    private static void setRaEvent(IpConnectivityEvent out, RaEvent in) {
        out.raEvent = new IpConnectivityLogClass.RaEvent();
        out.raEvent.routerLifetime = in.routerLifetime;
        out.raEvent.prefixValidLifetime = in.prefixValidLifetime;
        out.raEvent.prefixPreferredLifetime = in.prefixPreferredLifetime;
        out.raEvent.routeInfoLifetime = in.routeInfoLifetime;
        out.raEvent.rdnssLifetime = in.rdnssLifetime;
        out.raEvent.dnsslLifetime = in.dnsslLifetime;
    }

    private static int[] bytesToInts(byte[] in) {
        int[] out = new int[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i] & 255;
        }
        return out;
    }

    private static NetworkId netIdOf(int netid) {
        NetworkId ni = new NetworkId();
        ni.networkId = netid;
        return ni;
    }

    private static int ipSupportOf(DefaultNetworkEvent in) {
        if (in.prevIPv4 && in.prevIPv6) {
            return 3;
        }
        if (in.prevIPv6) {
            return 2;
        }
        if (in.prevIPv4) {
            return 1;
        }
        return 0;
    }

    private static boolean isBitSet(int flags, int bit) {
        return ((1 << bit) & flags) != 0;
    }
}

package com.android.server.connectivity;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.RouteInfo;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.server.connectivity.-$Lambda$02XS5hPTTued4UVYjxEawOnVOG8.AnonymousClass1;
import com.android.server.net.BaseNetworkObserver;
import java.net.Inet4Address;
import java.util.Objects;

public class Nat464Xlat extends BaseNetworkObserver {
    private static final String CLAT_PREFIX = "v4-";
    private static final int[] NETWORK_TYPES = new int[]{0, 1, 9};
    private static final String TAG = Nat464Xlat.class.getSimpleName();
    private String mBaseIface;
    private String mIface;
    private final INetworkManagementService mNMService;
    private final NetworkAgentInfo mNetwork;
    private State mState = State.IDLE;

    private enum State {
        IDLE,
        STARTING,
        RUNNING,
        STOPPING
    }

    public Nat464Xlat(INetworkManagementService nmService, NetworkAgentInfo nai) {
        this.mNMService = nmService;
        this.mNetwork = nai;
    }

    public static boolean requiresClat(NetworkAgentInfo nai) {
        int netType = nai.networkInfo.getType();
        boolean supported = ArrayUtils.contains(NETWORK_TYPES, nai.networkInfo.getType());
        boolean connected = nai.networkInfo.isConnected();
        int hasIPv4Address = nai.linkProperties != null ? nai.linkProperties.hasIPv4Address() : 0;
        boolean doXlat = SystemProperties.getBoolean("persist.net.doxlat", true);
        if (!doXlat) {
            Slog.i(TAG, "Android Xlat is disabled");
        }
        if (!supported || !connected || (hasIPv4Address ^ 1) == 0) {
            return false;
        }
        if (netType == 0) {
            return doXlat;
        }
        return true;
    }

    public boolean isStarted() {
        return this.mState != State.IDLE;
    }

    public boolean isStarting() {
        return this.mState == State.STARTING;
    }

    public boolean isRunning() {
        return this.mState == State.RUNNING;
    }

    public boolean isStopping() {
        return this.mState == State.STOPPING;
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0048 A:{Splitter: B:2:0x0005, ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:9:0x0048, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x0049, code:
            android.util.Slog.e(TAG, "Error starting clatd on " + r6, r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void enterStartingState(String baseIface) {
        try {
            this.mNMService.registerObserver(this);
            try {
                this.mNMService.startClatd(baseIface);
            } catch (Exception e) {
            }
            this.mIface = CLAT_PREFIX + baseIface;
            this.mBaseIface = baseIface;
            this.mState = State.STARTING;
        } catch (RemoteException e2) {
            Slog.e(TAG, "startClat: Can't register interface observer for clat on " + this.mNetwork.name());
        }
    }

    private void enterRunningState() {
        maybeSetIpv6NdOffload(this.mBaseIface, false);
        this.mState = State.RUNNING;
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0018 A:{Splitter: B:3:0x000c, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:7:0x0018, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:8:0x0019, code:
            android.util.Slog.e(TAG, "Error stopping clatd on " + r4.mBaseIface, r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void enterStoppingState() {
        if (isRunning()) {
            maybeSetIpv6NdOffload(this.mBaseIface, true);
        }
        try {
            this.mNMService.stopClatd(this.mBaseIface);
        } catch (Exception e) {
        }
        this.mState = State.STOPPING;
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x000f A:{Splitter: B:1:0x0001, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x000f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0010, code:
            android.util.Slog.e(TAG, "Error unregistering clatd observer on " + r5.mBaseIface, r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void enterIdleState() {
        try {
            this.mNMService.unregisterObserver(this);
        } catch (Exception e) {
        }
        this.mIface = null;
        this.mBaseIface = null;
        this.mState = State.IDLE;
    }

    public void start() {
        if (isStarted()) {
            Slog.e(TAG, "startClat: already started");
        } else if (this.mNetwork.linkProperties == null) {
            Slog.e(TAG, "startClat: Can't start clat with null LinkProperties");
        } else {
            String baseIface = this.mNetwork.linkProperties.getInterfaceName();
            if (baseIface == null) {
                Slog.e(TAG, "startClat: Can't start clat on null interface");
                return;
            }
            Slog.i(TAG, "Starting clatd on " + baseIface);
            enterStartingState(baseIface);
        }
    }

    public void stop() {
        if (isStarted()) {
            Slog.i(TAG, "Stopping clatd on " + this.mBaseIface);
            boolean wasStarting = isStarting();
            enterStoppingState();
            if (wasStarting) {
                enterIdleState();
            }
        }
    }

    public void fixupLinkProperties(LinkProperties oldLp) {
        if (isRunning()) {
            LinkProperties lp = this.mNetwork.linkProperties;
            if (lp != null && !lp.getAllInterfaceNames().contains(this.mIface)) {
                Slog.d(TAG, "clatd running, updating NAI for " + this.mIface);
                for (LinkProperties stacked : oldLp.getStackedLinks()) {
                    if (Objects.equals(this.mIface, stacked.getInterfaceName())) {
                        lp.addStackedLink(stacked);
                        return;
                    }
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
    /* JADX WARNING: Missing block: B:5:0x0026, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private LinkAddress getLinkAddress(String iface) {
        try {
            return this.mNMService.getInterfaceConfig(iface).getLinkAddress();
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0038 A:{Splitter: B:3:0x000c, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:10:0x0038, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x0039, code:
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

    private void handleInterfaceLinkStateChanged(String iface, boolean up) {
        if (isStarting() && (up ^ 1) == 0 && (Objects.equals(this.mIface, iface) ^ 1) == 0) {
            LinkAddress clatAddress = getLinkAddress(iface);
            if (clatAddress == null) {
                Slog.e(TAG, "clatAddress was null for stacked iface " + iface);
                return;
            }
            Slog.i(TAG, String.format("interface %s is up, adding stacked link %s on top of %s", new Object[]{this.mIface, this.mIface, this.mBaseIface}));
            enterRunningState();
            LinkProperties lp = new LinkProperties(this.mNetwork.linkProperties);
            lp.addStackedLink(makeLinkProperties(clatAddress));
            this.mNetwork.connService().handleUpdateLinkProperties(this.mNetwork, lp);
        }
    }

    private void handleInterfaceRemoved(String iface) {
        if (!Objects.equals(this.mIface, iface)) {
            return;
        }
        if (isRunning() || (isStopping() ^ 1) == 0) {
            Slog.i(TAG, "interface " + iface + " removed");
            if (!isStopping()) {
                enterStoppingState();
            }
            enterIdleState();
            LinkProperties lp = new LinkProperties(this.mNetwork.linkProperties);
            lp.removeStackedLink(iface);
            this.mNetwork.connService().handleUpdateLinkProperties(this.mNetwork, lp);
        }
    }

    public void interfaceLinkStateChanged(String iface, boolean up) {
        this.mNetwork.handler().post(new AnonymousClass1(up, this, iface));
    }

    public void interfaceRemoved(String iface) {
        this.mNetwork.handler().post(new -$Lambda$02XS5hPTTued4UVYjxEawOnVOG8(this, iface));
    }

    public String toString() {
        return "mBaseIface: " + this.mBaseIface + ", mIface: " + this.mIface + ", mState: " + this.mState;
    }
}

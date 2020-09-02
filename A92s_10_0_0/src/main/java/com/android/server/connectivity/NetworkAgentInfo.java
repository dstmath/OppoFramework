package com.android.server.connectivity;

import android.content.Context;
import android.net.IDnsResolver;
import android.net.INetd;
import android.net.INetworkMonitor;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkMonitorManager;
import android.net.NetworkRequest;
import android.net.NetworkState;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.WakeupMessage;
import com.android.server.ConnectivityService;
import com.android.server.connectivity.gatewayconflict.OppoArpPeer;
import com.android.server.connectivity.gatewayconflict.OppoGatewayState;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public class NetworkAgentInfo implements Comparable<NetworkAgentInfo> {
    private static final boolean ADD = true;
    public static final int EVENT_NETWORK_LINGER_COMPLETE = 1001;
    private static final boolean REMOVE = false;
    private static final String TAG = ConnectivityService.class.getSimpleName();
    private static final boolean VDBG = false;
    public final AsyncChannel asyncChannel;
    public boolean avoidUnvalidated;
    public volatile boolean captivePortalValidationPending;
    public final Nat464Xlat clatd;
    public boolean created;
    private int currentScore;
    public boolean everCaptivePortalDetected;
    public boolean everConnected;
    public boolean everValidated;
    public final int factorySerialNumber;
    public boolean lastCaptivePortalDetected;
    public boolean lastValidated;
    public LinkProperties linkProperties;
    private final ConnectivityService mConnService;
    private final Context mContext;
    public OppoGatewayState mGatewayState = null;
    private final Handler mHandler;
    private long mLingerExpiryMs;
    private WakeupMessage mLingerMessage;
    private final SparseArray<LingerTimer> mLingerTimerForRequest = new SparseArray<>();
    private final SortedSet<LingerTimer> mLingerTimers = new TreeSet();
    private boolean mLingering;
    private volatile NetworkMonitorManager mNetworkMonitor;
    private final SparseArray<NetworkRequest> mNetworkRequests = new SparseArray<>();
    private int mNumBackgroundNetworkRequests = 0;
    private int mNumRequestNetworkRequests = 0;
    public final Messenger messenger;
    public final Network network;
    public NetworkCapabilities networkCapabilities;
    public NetworkInfo networkInfo;
    public final NetworkMisc networkMisc;
    public boolean partialConnectivity;

    public static class LingerTimer implements Comparable<LingerTimer> {
        public final long expiryMs;
        public final NetworkRequest request;

        public LingerTimer(NetworkRequest request2, long expiryMs2) {
            this.request = request2;
            this.expiryMs = expiryMs2;
        }

        public boolean equals(Object o) {
            if (!(o instanceof LingerTimer)) {
                return false;
            }
            LingerTimer other = (LingerTimer) o;
            if (this.request.requestId == other.request.requestId && this.expiryMs == other.expiryMs) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.request.requestId), Long.valueOf(this.expiryMs));
        }

        public int compareTo(LingerTimer other) {
            long j = this.expiryMs;
            long j2 = other.expiryMs;
            if (j != j2) {
                return Long.compare(j, j2);
            }
            return Integer.compare(this.request.requestId, other.request.requestId);
        }

        public String toString() {
            return String.format("%s, expires %dms", this.request.toString(), Long.valueOf(this.expiryMs - SystemClock.elapsedRealtime()));
        }
    }

    public NetworkAgentInfo(Messenger messenger2, AsyncChannel ac, Network net, NetworkInfo info, LinkProperties lp, NetworkCapabilities nc, int score, Context context, Handler handler, NetworkMisc misc, ConnectivityService connService, INetd netd, IDnsResolver dnsResolver, INetworkManagementService nms, int factorySerialNumber2) {
        this.messenger = messenger2;
        this.asyncChannel = ac;
        this.network = net;
        this.networkInfo = info;
        this.linkProperties = lp;
        this.networkCapabilities = nc;
        this.currentScore = score;
        this.clatd = new Nat464Xlat(this, netd, dnsResolver, nms);
        this.mConnService = connService;
        this.mContext = context;
        this.mHandler = handler;
        this.networkMisc = misc;
        this.factorySerialNumber = factorySerialNumber2;
        this.mGatewayState = new OppoGatewayState(this.mContext, this);
    }

    public void onNetworkMonitorCreated(INetworkMonitor networkMonitor) {
        this.mNetworkMonitor = new NetworkMonitorManager(networkMonitor);
    }

    public void setNetworkCapabilities(NetworkCapabilities nc) {
        this.networkCapabilities = nc;
        NetworkMonitorManager nm = this.mNetworkMonitor;
        if (nm != null) {
            nm.notifyNetworkCapabilitiesChanged(nc);
        }
    }

    public ConnectivityService connService() {
        return this.mConnService;
    }

    public NetworkMisc netMisc() {
        return this.networkMisc;
    }

    public Handler handler() {
        return this.mHandler;
    }

    public Network network() {
        return this.network;
    }

    public NetworkMonitorManager networkMonitor() {
        return this.mNetworkMonitor;
    }

    /* renamed from: com.android.server.connectivity.NetworkAgentInfo$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkRequest$Type = new int[NetworkRequest.Type.values().length];

        static {
            try {
                $SwitchMap$android$net$NetworkRequest$Type[NetworkRequest.Type.REQUEST.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$NetworkRequest$Type[NetworkRequest.Type.BACKGROUND_REQUEST.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$net$NetworkRequest$Type[NetworkRequest.Type.TRACK_DEFAULT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$net$NetworkRequest$Type[NetworkRequest.Type.LISTEN.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$net$NetworkRequest$Type[NetworkRequest.Type.NONE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    private void updateRequestCounts(boolean add, NetworkRequest request) {
        int delta = add ? 1 : -1;
        int i = AnonymousClass1.$SwitchMap$android$net$NetworkRequest$Type[request.type.ordinal()];
        if (i == 1) {
            this.mNumRequestNetworkRequests += delta;
        } else if (i == 2) {
            this.mNumRequestNetworkRequests += delta;
            this.mNumBackgroundNetworkRequests += delta;
        } else if (i != 3 && i != 4) {
            Log.wtf(TAG, "Unhandled request type " + request.type);
        }
    }

    public boolean addRequest(NetworkRequest networkRequest) {
        NetworkRequest existing;
        synchronized (this.mNetworkRequests) {
            existing = this.mNetworkRequests.get(networkRequest.requestId);
        }
        if (existing == networkRequest) {
            return false;
        }
        if (existing != null) {
            Log.wtf(TAG, String.format("Duplicate requestId for %s and %s on %s", networkRequest, existing, name()));
            updateRequestCounts(false, existing);
        }
        synchronized (this.mNetworkRequests) {
            this.mNetworkRequests.put(networkRequest.requestId, networkRequest);
        }
        updateRequestCounts(true, networkRequest);
        return true;
    }

    public void removeRequest(int requestId) {
        NetworkRequest existing;
        synchronized (this.mNetworkRequests) {
            existing = this.mNetworkRequests.get(requestId);
        }
        if (existing != null) {
            updateRequestCounts(false, existing);
            synchronized (this.mNetworkRequests) {
                this.mNetworkRequests.remove(requestId);
            }
            if (existing.isRequest()) {
                unlingerRequest(existing);
            }
        }
    }

    public boolean isSatisfyingRequest(int id) {
        boolean z;
        synchronized (this.mNetworkRequests) {
            z = this.mNetworkRequests.get(id) != null;
        }
        return z;
    }

    public NetworkRequest requestAt(int index) {
        NetworkRequest valueAt;
        synchronized (this.mNetworkRequests) {
            valueAt = this.mNetworkRequests.valueAt(index);
        }
        return valueAt;
    }

    public int numRequestNetworkRequests() {
        return this.mNumRequestNetworkRequests;
    }

    public int numBackgroundNetworkRequests() {
        return this.mNumBackgroundNetworkRequests;
    }

    public int numForegroundNetworkRequests() {
        return this.mNumRequestNetworkRequests - this.mNumBackgroundNetworkRequests;
    }

    public int numNetworkRequests() {
        return this.mNetworkRequests.size();
    }

    public boolean isBackgroundNetwork() {
        return !isVPN() && numForegroundNetworkRequests() == 0 && this.mNumBackgroundNetworkRequests > 0 && !isLingering();
    }

    public boolean isSuspended() {
        return this.networkInfo.getState() == NetworkInfo.State.SUSPENDED;
    }

    public boolean satisfies(NetworkRequest request) {
        return this.created && request.networkCapabilities.satisfiedByNetworkCapabilities(this.networkCapabilities);
    }

    public boolean satisfiesImmutableCapabilitiesOf(NetworkRequest request) {
        return this.created && request.networkCapabilities.satisfiedByImmutableNetworkCapabilities(this.networkCapabilities);
    }

    public boolean isVPN() {
        return this.networkCapabilities.hasTransport(4);
    }

    private int getCurrentScore(boolean pretendValidated) {
        if (!this.networkMisc.explicitlySelected || (!this.networkMisc.acceptUnvalidated && !pretendValidated)) {
            int score = this.currentScore;
            if (score < 0) {
                return 0;
            }
            return score;
        }
        LinkProperties linkProperties2 = this.linkProperties;
        if (linkProperties2 != null && "wlan0".equals(linkProperties2.getInterfaceName())) {
            return 79;
        }
        LinkProperties linkProperties3 = this.linkProperties;
        if (linkProperties3 == null || !"wlan1".equals(linkProperties3.getInterfaceName())) {
            return 100;
        }
        return 5;
    }

    private boolean ignoreWifiUnvalidationPenalty() {
        return ((this.networkCapabilities.hasTransport(1) || this.networkCapabilities.hasTransport(8)) && this.networkCapabilities.hasCapability(12)) && !(this.mConnService.avoidBadWifi() || this.avoidUnvalidated) && this.everValidated;
    }

    public int getCurrentScore() {
        return getCurrentScore(false);
    }

    public int getCurrentScoreAsValidated() {
        return getCurrentScore(true);
    }

    public void setCurrentScore(int newScore) {
        this.currentScore = newScore;
    }

    public NetworkState getNetworkState() {
        NetworkState networkState;
        synchronized (this) {
            networkState = new NetworkState(new NetworkInfo(this.networkInfo), new LinkProperties(this.linkProperties), new NetworkCapabilities(this.networkCapabilities), this.network, this.networkMisc != null ? this.networkMisc.subscriberId : null, (String) null);
        }
        return networkState;
    }

    public void lingerRequest(NetworkRequest request, long now, long duration) {
        if (this.mLingerTimerForRequest.get(request.requestId) != null) {
            String str = TAG;
            Log.wtf(str, name() + ": request " + request.requestId + " already lingered");
        }
        LingerTimer timer = new LingerTimer(request, now + duration);
        SortedSet<LingerTimer> sortedSet = this.mLingerTimers;
        if (sortedSet != null) {
            synchronized (sortedSet) {
                this.mLingerTimers.add(timer);
            }
        }
        this.mLingerTimerForRequest.put(request.requestId, timer);
    }

    public boolean unlingerRequest(NetworkRequest request) {
        LingerTimer timer = this.mLingerTimerForRequest.get(request.requestId);
        if (timer == null) {
            return false;
        }
        SortedSet<LingerTimer> sortedSet = this.mLingerTimers;
        if (sortedSet != null) {
            synchronized (sortedSet) {
                this.mLingerTimers.remove(timer);
            }
        }
        this.mLingerTimerForRequest.remove(request.requestId);
        return true;
    }

    public long getLingerExpiry() {
        return this.mLingerExpiryMs;
    }

    public void updateLingerTimer() {
        long newExpiry = 0;
        SortedSet<LingerTimer> sortedSet = this.mLingerTimers;
        if (sortedSet != null) {
            synchronized (sortedSet) {
                newExpiry = this.mLingerTimers.isEmpty() ? 0 : this.mLingerTimers.last().expiryMs;
            }
        }
        if (newExpiry != this.mLingerExpiryMs) {
            WakeupMessage wakeupMessage = this.mLingerMessage;
            if (wakeupMessage != null) {
                wakeupMessage.cancel();
                this.mLingerMessage = null;
            }
            if (newExpiry > 0) {
                ConnectivityService connectivityService = this.mConnService;
                Context context = this.mContext;
                Handler handler = this.mHandler;
                this.mLingerMessage = connectivityService.makeWakeupMessage(context, handler, "NETWORK_LINGER_COMPLETE." + this.network.netId, 1001, this);
                this.mLingerMessage.schedule(newExpiry);
            }
            this.mLingerExpiryMs = newExpiry;
        }
    }

    public void linger() {
        this.mLingering = true;
    }

    public void unlinger() {
        this.mLingering = false;
    }

    public boolean isLingering() {
        return this.mLingering;
    }

    public void clearLingerState() {
        WakeupMessage wakeupMessage = this.mLingerMessage;
        if (wakeupMessage != null) {
            wakeupMessage.cancel();
            this.mLingerMessage = null;
        }
        SortedSet<LingerTimer> sortedSet = this.mLingerTimers;
        if (sortedSet != null) {
            synchronized (sortedSet) {
                this.mLingerTimers.clear();
            }
        }
        this.mLingerTimerForRequest.clear();
        updateLingerTimer();
        this.mLingering = false;
    }

    public void dumpLingerTimers(PrintWriter pw) {
        for (LingerTimer timer : this.mLingerTimers) {
            pw.println(timer);
        }
    }

    public String toString() {
        return "NetworkAgentInfo{ ni{" + this.networkInfo + "}  network{" + this.network + "}  nethandle{" + this.network.getNetworkHandle() + "}  lp{" + this.linkProperties + "}  nc{" + this.networkCapabilities + "}  Score{" + getCurrentScore() + "}  everValidated{" + this.everValidated + "}  lastValidated{" + this.lastValidated + "}  created{" + this.created + "} lingering{" + isLingering() + "} explicitlySelected{" + this.networkMisc.explicitlySelected + "} acceptUnvalidated{" + this.networkMisc.acceptUnvalidated + "} everCaptivePortalDetected{" + this.everCaptivePortalDetected + "} lastCaptivePortalDetected{" + this.lastCaptivePortalDetected + "} captivePortalValidationPending{" + this.captivePortalValidationPending + "} partialConnectivity{" + this.partialConnectivity + "} acceptPartialConnectivity{" + this.networkMisc.acceptPartialConnectivity + "} clat{" + this.clatd + "} }";
    }

    public String name() {
        return "NetworkAgentInfo [" + this.networkInfo.getTypeName() + " (" + this.networkInfo.getSubtypeName() + ") - " + Objects.toString(this.network) + "]";
    }

    public int compareTo(NetworkAgentInfo other) {
        return other.getCurrentScore() - getCurrentScore();
    }

    public void startGatewayDetector(OppoArpPeer.ArpPeerChangeCallback callback) {
        this.mGatewayState.startGatewayDetector(callback);
    }
}

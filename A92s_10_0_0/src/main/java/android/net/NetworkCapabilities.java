package android.net;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArraySet;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.util.BitUtils;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public final class NetworkCapabilities implements Parcelable {
    private static final long CONNECTIVITY_MANAGED_CAPABILITIES = 17498112;
    public static final Parcelable.Creator<NetworkCapabilities> CREATOR = new Parcelable.Creator<NetworkCapabilities>() {
        /* class android.net.NetworkCapabilities.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NetworkCapabilities createFromParcel(Parcel in) {
            NetworkCapabilities netCap = new NetworkCapabilities();
            long unused = netCap.mNetworkCapabilities = in.readLong();
            long unused2 = netCap.mUnwantedNetworkCapabilities = in.readLong();
            long unused3 = netCap.mTransportTypes = in.readLong();
            int unused4 = netCap.mLinkUpBandwidthKbps = in.readInt();
            int unused5 = netCap.mLinkDownBandwidthKbps = in.readInt();
            NetworkSpecifier unused6 = netCap.mNetworkSpecifier = (NetworkSpecifier) in.readParcelable(null);
            TransportInfo unused7 = netCap.mTransportInfo = (TransportInfo) in.readParcelable(null);
            int unused8 = netCap.mSignalStrength = in.readInt();
            ArraySet unused9 = netCap.mUids = in.readArraySet(null);
            String unused10 = netCap.mSSID = in.readString();
            return netCap;
        }

        @Override // android.os.Parcelable.Creator
        public NetworkCapabilities[] newArray(int size) {
            return new NetworkCapabilities[size];
        }
    };
    private static final long DEFAULT_CAPABILITIES = 57344;
    private static final long FORCE_RESTRICTED_CAPABILITIES = 4194304;
    private static final int INVALID_UID = -1;
    public static final int LINK_BANDWIDTH_UNSPECIFIED = 0;
    private static final int MAX_NET_CAPABILITY = 30;
    public static final int MAX_TRANSPORT = 8;
    private static final int MIN_NET_CAPABILITY = 0;
    public static final int MIN_TRANSPORT = 0;
    private static final long MUTABLE_CAPABILITIES = 20922368;
    public static final int NET_CAPABILITY_BIP = 27;
    public static final int NET_CAPABILITY_CAPTIVE_PORTAL = 17;
    public static final int NET_CAPABILITY_CBS = 5;
    public static final int NET_CAPABILITY_DUALWIFI = 30;
    public static final int NET_CAPABILITY_DUN = 2;
    public static final int NET_CAPABILITY_EIMS = 10;
    public static final int NET_CAPABILITY_FOREGROUND = 19;
    public static final int NET_CAPABILITY_FOTA = 3;
    public static final int NET_CAPABILITY_IA = 7;
    public static final int NET_CAPABILITY_IMS = 4;
    public static final int NET_CAPABILITY_INTERNET = 12;
    public static final int NET_CAPABILITY_MCX = 23;
    public static final int NET_CAPABILITY_MMS = 0;
    public static final int NET_CAPABILITY_NOT_CONGESTED = 20;
    public static final int NET_CAPABILITY_NOT_METERED = 11;
    public static final int NET_CAPABILITY_NOT_RESTRICTED = 13;
    public static final int NET_CAPABILITY_NOT_ROAMING = 18;
    public static final int NET_CAPABILITY_NOT_SUSPENDED = 21;
    public static final int NET_CAPABILITY_NOT_VPN = 15;
    @SystemApi
    public static final int NET_CAPABILITY_OEM_PAID = 22;
    @SystemApi
    public static final int NET_CAPABILITY_PARTIAL_CONNECTIVITY = 24;
    public static final int NET_CAPABILITY_RCS = 8;
    public static final int NET_CAPABILITY_SUPL = 1;
    public static final int NET_CAPABILITY_TRUSTED = 14;
    public static final int NET_CAPABILITY_VALIDATED = 16;
    public static final int NET_CAPABILITY_VSIM = 26;
    public static final int NET_CAPABILITY_WAP = 25;
    public static final int NET_CAPABILITY_WIFI_P2P = 6;
    public static final int NET_CAPABILITY_XCAP = 9;
    private static final long NON_REQUESTABLE_CAPABILITIES = 20905984;
    @VisibleForTesting
    static final long RESTRICTED_CAPABILITIES = 209717180;
    public static final int SIGNAL_STRENGTH_UNSPECIFIED = Integer.MIN_VALUE;
    private static final String TAG = "NetworkCapabilities";
    public static final int TRANSPORT_BLUETOOTH = 2;
    public static final int TRANSPORT_CELLULAR = 0;
    public static final int TRANSPORT_ETHERNET = 3;
    public static final int TRANSPORT_LOWPAN = 6;
    private static final String[] TRANSPORT_NAMES = {"CELLULAR", "WIFI", "BLUETOOTH", "ETHERNET", "VPN", "WIFI_AWARE", "LOWPAN", "TEST", "WIFI_EXT"};
    public static final int TRANSPORT_TEST = 7;
    public static final int TRANSPORT_VPN = 4;
    public static final int TRANSPORT_WIFI = 1;
    public static final int TRANSPORT_WIFI_AWARE = 5;
    public static final int TRANSPORT_WIFI_EXT = 8;
    @VisibleForTesting
    static final long UNRESTRICTED_CAPABILITIES = 4163;
    private int mEstablishingVpnAppUid = -1;
    /* access modifiers changed from: private */
    public int mLinkDownBandwidthKbps = 0;
    /* access modifiers changed from: private */
    public int mLinkUpBandwidthKbps = 0;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public long mNetworkCapabilities;
    /* access modifiers changed from: private */
    public NetworkSpecifier mNetworkSpecifier = null;
    /* access modifiers changed from: private */
    public String mSSID;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage(maxTargetSdk = 28)
    public int mSignalStrength = Integer.MIN_VALUE;
    /* access modifiers changed from: private */
    public TransportInfo mTransportInfo = null;
    /* access modifiers changed from: private */
    public long mTransportTypes;
    /* access modifiers changed from: private */
    public ArraySet<UidRange> mUids = null;
    /* access modifiers changed from: private */
    public long mUnwantedNetworkCapabilities;

    /* access modifiers changed from: private */
    public interface NameOf {
        String nameOf(int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface NetCapability {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Transport {
    }

    @UnsupportedAppUsage
    public NetworkCapabilities() {
        clearAll();
        this.mNetworkCapabilities = DEFAULT_CAPABILITIES;
    }

    public NetworkCapabilities(NetworkCapabilities nc) {
        if (nc != null) {
            set(nc);
        }
    }

    public void clearAll() {
        this.mUnwantedNetworkCapabilities = 0;
        this.mTransportTypes = 0;
        this.mNetworkCapabilities = 0;
        this.mLinkDownBandwidthKbps = 0;
        this.mLinkUpBandwidthKbps = 0;
        this.mNetworkSpecifier = null;
        this.mTransportInfo = null;
        this.mSignalStrength = Integer.MIN_VALUE;
        this.mUids = null;
        this.mEstablishingVpnAppUid = -1;
        this.mSSID = null;
    }

    public void set(NetworkCapabilities nc) {
        this.mNetworkCapabilities = nc.mNetworkCapabilities;
        this.mTransportTypes = nc.mTransportTypes;
        this.mLinkUpBandwidthKbps = nc.mLinkUpBandwidthKbps;
        this.mLinkDownBandwidthKbps = nc.mLinkDownBandwidthKbps;
        this.mNetworkSpecifier = nc.mNetworkSpecifier;
        this.mTransportInfo = nc.mTransportInfo;
        this.mSignalStrength = nc.mSignalStrength;
        setUids(nc.mUids);
        this.mEstablishingVpnAppUid = nc.mEstablishingVpnAppUid;
        this.mUnwantedNetworkCapabilities = nc.mUnwantedNetworkCapabilities;
        this.mSSID = nc.mSSID;
    }

    @UnsupportedAppUsage
    public NetworkCapabilities addCapability(int capability) {
        checkValidCapability(capability);
        this.mNetworkCapabilities |= (long) (1 << capability);
        this.mUnwantedNetworkCapabilities &= (long) (~(1 << capability));
        return this;
    }

    public void addUnwantedCapability(int capability) {
        checkValidCapability(capability);
        this.mUnwantedNetworkCapabilities |= (long) (1 << capability);
        this.mNetworkCapabilities &= (long) (~(1 << capability));
    }

    @UnsupportedAppUsage
    public NetworkCapabilities removeCapability(int capability) {
        checkValidCapability(capability);
        long mask = (long) (~(1 << capability));
        this.mNetworkCapabilities &= mask;
        this.mUnwantedNetworkCapabilities &= mask;
        return this;
    }

    public NetworkCapabilities setCapability(int capability, boolean value) {
        if (value) {
            addCapability(capability);
        } else {
            removeCapability(capability);
        }
        return this;
    }

    public int[] getCapabilities() {
        return BitUtils.unpackBits(this.mNetworkCapabilities);
    }

    public int[] getUnwantedCapabilities() {
        return BitUtils.unpackBits(this.mUnwantedNetworkCapabilities);
    }

    public void setCapabilities(int[] capabilities, int[] unwantedCapabilities) {
        this.mNetworkCapabilities = BitUtils.packBits(capabilities);
        this.mUnwantedNetworkCapabilities = BitUtils.packBits(unwantedCapabilities);
    }

    @Deprecated
    public void setCapabilities(int[] capabilities) {
        setCapabilities(capabilities, new int[0]);
    }

    public boolean hasCapability(int capability) {
        return isValidCapability(capability) && (this.mNetworkCapabilities & ((long) (1 << capability))) != 0;
    }

    public boolean hasUnwantedCapability(int capability) {
        return isValidCapability(capability) && (this.mUnwantedNetworkCapabilities & ((long) (1 << capability))) != 0;
    }

    public boolean hasConnectivityManagedCapability() {
        return (this.mNetworkCapabilities & CONNECTIVITY_MANAGED_CAPABILITIES) != 0;
    }

    private void combineNetCapabilities(NetworkCapabilities nc) {
        this.mNetworkCapabilities |= nc.mNetworkCapabilities;
        this.mUnwantedNetworkCapabilities |= nc.mUnwantedNetworkCapabilities;
    }

    public String describeFirstNonRequestableCapability() {
        long nonRequestable = (this.mNetworkCapabilities | this.mUnwantedNetworkCapabilities) & NON_REQUESTABLE_CAPABILITIES;
        if (nonRequestable != 0) {
            return capabilityNameOf(BitUtils.unpackBits(nonRequestable)[0]);
        }
        if (this.mLinkUpBandwidthKbps != 0 || this.mLinkDownBandwidthKbps != 0) {
            return "link bandwidth";
        }
        if (hasSignalStrength()) {
            return "signalStrength";
        }
        return null;
    }

    private boolean satisfiedByNetCapabilities(NetworkCapabilities nc, boolean onlyImmutable) {
        long requestedCapabilities = this.mNetworkCapabilities;
        long requestedUnwantedCapabilities = this.mUnwantedNetworkCapabilities;
        long providedCapabilities = nc.mNetworkCapabilities;
        if (onlyImmutable) {
            requestedCapabilities &= -20922369;
            requestedUnwantedCapabilities &= -20922369;
        }
        return (providedCapabilities & requestedCapabilities) == requestedCapabilities && (requestedUnwantedCapabilities & providedCapabilities) == 0;
    }

    public boolean equalsNetCapabilities(NetworkCapabilities nc) {
        return nc.mNetworkCapabilities == this.mNetworkCapabilities && nc.mUnwantedNetworkCapabilities == this.mUnwantedNetworkCapabilities;
    }

    private boolean equalsNetCapabilitiesRequestable(NetworkCapabilities that) {
        return (this.mNetworkCapabilities & -20905985) == (that.mNetworkCapabilities & -20905985) && (this.mUnwantedNetworkCapabilities & -20905985) == (-20905985 & that.mUnwantedNetworkCapabilities);
    }

    public void maybeMarkCapabilitiesRestricted() {
        boolean hasRestrictedCapabilities = true;
        boolean forceRestrictedCapability = (this.mNetworkCapabilities & 4194304) != 0;
        boolean hasUnrestrictedCapabilities = (this.mNetworkCapabilities & UNRESTRICTED_CAPABILITIES) != 0;
        if ((this.mNetworkCapabilities & RESTRICTED_CAPABILITIES) == 0) {
            hasRestrictedCapabilities = false;
        }
        if (forceRestrictedCapability || (hasRestrictedCapabilities && !hasUnrestrictedCapabilities)) {
            removeCapability(13);
        }
    }

    public static boolean isValidTransport(int transportType) {
        return transportType >= 0 && transportType <= 8;
    }

    @UnsupportedAppUsage
    public NetworkCapabilities addTransportType(int transportType) {
        checkValidTransportType(transportType);
        this.mTransportTypes |= (long) (1 << transportType);
        setNetworkSpecifier(this.mNetworkSpecifier);
        return this;
    }

    public NetworkCapabilities removeTransportType(int transportType) {
        checkValidTransportType(transportType);
        this.mTransportTypes &= (long) (~(1 << transportType));
        setNetworkSpecifier(this.mNetworkSpecifier);
        return this;
    }

    public NetworkCapabilities setTransportType(int transportType, boolean value) {
        if (value) {
            addTransportType(transportType);
        } else {
            removeTransportType(transportType);
        }
        return this;
    }

    @SystemApi
    public int[] getTransportTypes() {
        return BitUtils.unpackBits(this.mTransportTypes);
    }

    public void setTransportTypes(int[] transportTypes) {
        this.mTransportTypes = BitUtils.packBits(transportTypes);
    }

    public boolean hasTransport(int transportType) {
        return isValidTransport(transportType) && (this.mTransportTypes & ((long) (1 << transportType))) != 0;
    }

    private void combineTransportTypes(NetworkCapabilities nc) {
        this.mTransportTypes |= nc.mTransportTypes;
    }

    private boolean satisfiedByTransportTypes(NetworkCapabilities nc) {
        long j = this.mTransportTypes;
        return j == 0 || (j & nc.mTransportTypes) != 0;
    }

    public boolean equalsTransportTypes(NetworkCapabilities nc) {
        return nc.mTransportTypes == this.mTransportTypes;
    }

    public void setEstablishingVpnAppUid(int uid) {
        this.mEstablishingVpnAppUid = uid;
    }

    public int getEstablishingVpnAppUid() {
        return this.mEstablishingVpnAppUid;
    }

    public NetworkCapabilities setLinkUpstreamBandwidthKbps(int upKbps) {
        this.mLinkUpBandwidthKbps = upKbps;
        return this;
    }

    public int getLinkUpstreamBandwidthKbps() {
        return this.mLinkUpBandwidthKbps;
    }

    public NetworkCapabilities setLinkDownstreamBandwidthKbps(int downKbps) {
        this.mLinkDownBandwidthKbps = downKbps;
        return this;
    }

    public int getLinkDownstreamBandwidthKbps() {
        return this.mLinkDownBandwidthKbps;
    }

    private void combineLinkBandwidths(NetworkCapabilities nc) {
        this.mLinkUpBandwidthKbps = Math.max(this.mLinkUpBandwidthKbps, nc.mLinkUpBandwidthKbps);
        this.mLinkDownBandwidthKbps = Math.max(this.mLinkDownBandwidthKbps, nc.mLinkDownBandwidthKbps);
    }

    private boolean satisfiedByLinkBandwidths(NetworkCapabilities nc) {
        return this.mLinkUpBandwidthKbps <= nc.mLinkUpBandwidthKbps && this.mLinkDownBandwidthKbps <= nc.mLinkDownBandwidthKbps;
    }

    private boolean equalsLinkBandwidths(NetworkCapabilities nc) {
        return this.mLinkUpBandwidthKbps == nc.mLinkUpBandwidthKbps && this.mLinkDownBandwidthKbps == nc.mLinkDownBandwidthKbps;
    }

    public static int minBandwidth(int a, int b) {
        if (a == 0) {
            return b;
        }
        if (b == 0) {
            return a;
        }
        return Math.min(a, b);
    }

    public static int maxBandwidth(int a, int b) {
        return Math.max(a, b);
    }

    public NetworkCapabilities setNetworkSpecifier(NetworkSpecifier networkSpecifier) {
        if (networkSpecifier == null || Long.bitCount(this.mTransportTypes) == 1) {
            this.mNetworkSpecifier = networkSpecifier;
            return this;
        }
        throw new IllegalStateException("Must have a single transport specified to use setNetworkSpecifier");
    }

    public NetworkCapabilities setTransportInfo(TransportInfo transportInfo) {
        this.mTransportInfo = transportInfo;
        return this;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public NetworkSpecifier getNetworkSpecifier() {
        return this.mNetworkSpecifier;
    }

    public TransportInfo getTransportInfo() {
        return this.mTransportInfo;
    }

    private void combineSpecifiers(NetworkCapabilities nc) {
        NetworkSpecifier networkSpecifier = this.mNetworkSpecifier;
        if (networkSpecifier == null || networkSpecifier.equals(nc.mNetworkSpecifier)) {
            setNetworkSpecifier(nc.mNetworkSpecifier);
            return;
        }
        throw new IllegalStateException("Can't combine two networkSpecifiers");
    }

    private boolean satisfiedBySpecifier(NetworkCapabilities nc) {
        NetworkSpecifier networkSpecifier = this.mNetworkSpecifier;
        return networkSpecifier == null || networkSpecifier.satisfiedBy(nc.mNetworkSpecifier) || (nc.mNetworkSpecifier instanceof MatchAllNetworkSpecifier);
    }

    private boolean equalsSpecifier(NetworkCapabilities nc) {
        return Objects.equals(this.mNetworkSpecifier, nc.mNetworkSpecifier);
    }

    private void combineTransportInfos(NetworkCapabilities nc) {
        TransportInfo transportInfo = this.mTransportInfo;
        if (transportInfo == null || transportInfo.equals(nc.mTransportInfo)) {
            setTransportInfo(nc.mTransportInfo);
            return;
        }
        throw new IllegalStateException("Can't combine two TransportInfos");
    }

    private boolean equalsTransportInfo(NetworkCapabilities nc) {
        return Objects.equals(this.mTransportInfo, nc.mTransportInfo);
    }

    @UnsupportedAppUsage
    public NetworkCapabilities setSignalStrength(int signalStrength) {
        this.mSignalStrength = signalStrength;
        return this;
    }

    @UnsupportedAppUsage
    public boolean hasSignalStrength() {
        return this.mSignalStrength > Integer.MIN_VALUE;
    }

    public int getSignalStrength() {
        return this.mSignalStrength;
    }

    private void combineSignalStrength(NetworkCapabilities nc) {
        this.mSignalStrength = Math.max(this.mSignalStrength, nc.mSignalStrength);
    }

    private boolean satisfiedBySignalStrength(NetworkCapabilities nc) {
        return this.mSignalStrength <= nc.mSignalStrength;
    }

    private boolean equalsSignalStrength(NetworkCapabilities nc) {
        return this.mSignalStrength == nc.mSignalStrength;
    }

    public NetworkCapabilities setSingleUid(int uid) {
        ArraySet<UidRange> identity = new ArraySet<>(1);
        identity.add(new UidRange(uid, uid));
        setUids(identity);
        return this;
    }

    public NetworkCapabilities setUids(Set<UidRange> uids) {
        if (uids == null) {
            this.mUids = null;
        } else {
            this.mUids = new ArraySet<>(uids);
        }
        return this;
    }

    public Set<UidRange> getUids() {
        ArraySet<UidRange> arraySet = this.mUids;
        if (arraySet == null) {
            return null;
        }
        return new ArraySet((ArraySet) arraySet);
    }

    public boolean appliesToUid(int uid) {
        ArraySet<UidRange> arraySet = this.mUids;
        if (arraySet == null) {
            return true;
        }
        Iterator<UidRange> it = arraySet.iterator();
        while (it.hasNext()) {
            if (it.next().contains(uid)) {
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting
    public boolean equalsUids(NetworkCapabilities nc) {
        Set<UidRange> comparedUids = nc.mUids;
        if (comparedUids == null) {
            return this.mUids == null;
        }
        ArraySet<UidRange> arraySet = this.mUids;
        if (arraySet == null) {
            return false;
        }
        Set<UidRange> uids = new ArraySet<>(arraySet);
        for (UidRange range : comparedUids) {
            if (!uids.contains(range)) {
                return false;
            }
            uids.remove(range);
        }
        return uids.isEmpty();
    }

    public boolean satisfiedByUids(NetworkCapabilities nc) {
        ArraySet<UidRange> arraySet;
        if (nc.mUids == null || (arraySet = this.mUids) == null) {
            return true;
        }
        Iterator<UidRange> it = arraySet.iterator();
        while (it.hasNext()) {
            UidRange requiredRange = it.next();
            if (requiredRange.contains(nc.mEstablishingVpnAppUid)) {
                return true;
            }
            if (!nc.appliesToUidRange(requiredRange)) {
                return false;
            }
        }
        return true;
    }

    @VisibleForTesting
    public boolean appliesToUidRange(UidRange requiredRange) {
        ArraySet<UidRange> arraySet = this.mUids;
        if (arraySet == null) {
            return true;
        }
        Iterator<UidRange> it = arraySet.iterator();
        while (it.hasNext()) {
            if (it.next().containsRange(requiredRange)) {
                return true;
            }
        }
        return false;
    }

    private void combineUids(NetworkCapabilities nc) {
        ArraySet<UidRange> arraySet;
        ArraySet<UidRange> arraySet2 = nc.mUids;
        if (arraySet2 == null || (arraySet = this.mUids) == null) {
            this.mUids = null;
        } else {
            arraySet.addAll((ArraySet<? extends UidRange>) arraySet2);
        }
    }

    public NetworkCapabilities setSSID(String ssid) {
        this.mSSID = ssid;
        return this;
    }

    public String getSSID() {
        return this.mSSID;
    }

    public boolean equalsSSID(NetworkCapabilities nc) {
        return Objects.equals(this.mSSID, nc.mSSID);
    }

    public boolean satisfiedBySSID(NetworkCapabilities nc) {
        String str = this.mSSID;
        return str == null || str.equals(nc.mSSID);
    }

    private void combineSSIDs(NetworkCapabilities nc) {
        String str = this.mSSID;
        if (str == null || str.equals(nc.mSSID)) {
            setSSID(nc.mSSID);
            return;
        }
        throw new IllegalStateException("Can't combine two SSIDs");
    }

    public void combineCapabilities(NetworkCapabilities nc) {
        combineNetCapabilities(nc);
        combineTransportTypes(nc);
        combineLinkBandwidths(nc);
        combineSpecifiers(nc);
        combineTransportInfos(nc);
        combineSignalStrength(nc);
        combineUids(nc);
        combineSSIDs(nc);
    }

    private boolean satisfiedByNetworkCapabilities(NetworkCapabilities nc, boolean onlyImmutable) {
        return nc != null && satisfiedByNetCapabilities(nc, onlyImmutable) && satisfiedByTransportTypes(nc) && (onlyImmutable || satisfiedByLinkBandwidths(nc)) && satisfiedBySpecifier(nc) && ((onlyImmutable || satisfiedBySignalStrength(nc)) && ((onlyImmutable || satisfiedByUids(nc)) && (onlyImmutable || satisfiedBySSID(nc))));
    }

    @SystemApi
    public boolean satisfiedByNetworkCapabilities(NetworkCapabilities nc) {
        return satisfiedByNetworkCapabilities(nc, false);
    }

    public boolean satisfiedByImmutableNetworkCapabilities(NetworkCapabilities nc) {
        return satisfiedByNetworkCapabilities(nc, true);
    }

    public String describeImmutableDifferences(NetworkCapabilities that) {
        if (that == null) {
            return "other NetworkCapabilities was null";
        }
        StringJoiner joiner = new StringJoiner(", ");
        long oldImmutableCapabilities = this.mNetworkCapabilities & -20924417;
        long newImmutableCapabilities = -20924417 & that.mNetworkCapabilities;
        if (oldImmutableCapabilities != newImmutableCapabilities) {
            joiner.add(String.format("immutable capabilities changed: %s -> %s", capabilityNamesOf(BitUtils.unpackBits(oldImmutableCapabilities)), capabilityNamesOf(BitUtils.unpackBits(newImmutableCapabilities))));
        }
        if (!equalsSpecifier(that)) {
            joiner.add(String.format("specifier changed: %s -> %s", getNetworkSpecifier(), that.getNetworkSpecifier()));
        }
        if (!equalsTransportTypes(that)) {
            joiner.add(String.format("transports changed: %s -> %s", transportNamesOf(getTransportTypes()), transportNamesOf(that.getTransportTypes())));
        }
        return joiner.toString();
    }

    public boolean equalRequestableCapabilities(NetworkCapabilities nc) {
        if (nc != null && equalsNetCapabilitiesRequestable(nc) && equalsTransportTypes(nc) && equalsSpecifier(nc)) {
            return true;
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NetworkCapabilities)) {
            return false;
        }
        NetworkCapabilities that = (NetworkCapabilities) obj;
        if (!equalsNetCapabilities(that) || !equalsTransportTypes(that) || !equalsLinkBandwidths(that) || !equalsSignalStrength(that) || !equalsSpecifier(that) || !equalsTransportInfo(that) || !equalsUids(that) || !equalsSSID(that)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        long j = this.mNetworkCapabilities;
        int i = ((int) (j & -1)) + (((int) (j >> 32)) * 3);
        long j2 = this.mUnwantedNetworkCapabilities;
        int i2 = i + (((int) (j2 & -1)) * 5) + (((int) (j2 >> 32)) * 7);
        long j3 = this.mTransportTypes;
        return i2 + (((int) (-1 & j3)) * 11) + (((int) (j3 >> 32)) * 13) + (this.mLinkUpBandwidthKbps * 17) + (this.mLinkDownBandwidthKbps * 19) + (Objects.hashCode(this.mNetworkSpecifier) * 23) + (this.mSignalStrength * 29) + (Objects.hashCode(this.mUids) * 31) + (Objects.hashCode(this.mSSID) * 37) + (Objects.hashCode(this.mTransportInfo) * 41);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mNetworkCapabilities);
        dest.writeLong(this.mUnwantedNetworkCapabilities);
        dest.writeLong(this.mTransportTypes);
        dest.writeInt(this.mLinkUpBandwidthKbps);
        dest.writeInt(this.mLinkDownBandwidthKbps);
        dest.writeParcelable((Parcelable) this.mNetworkSpecifier, flags);
        dest.writeParcelable((Parcelable) this.mTransportInfo, flags);
        dest.writeInt(this.mSignalStrength);
        dest.writeArraySet(this.mUids);
        dest.writeString(this.mSSID);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (0 != this.mTransportTypes) {
            sb.append(" Transports: ");
            appendStringRepresentationOfBitMaskToStringBuilder(sb, this.mTransportTypes, $$Lambda$FpGXkd3pLxeXY58eJ_84mi1PLWQ.INSTANCE, "|");
        }
        if (0 != this.mNetworkCapabilities) {
            sb.append(" Capabilities: ");
            appendStringRepresentationOfBitMaskToStringBuilder(sb, this.mNetworkCapabilities, $$Lambda$p1_56lwnt1xBuY1muPblbN1Dtkw.INSTANCE, "&");
        }
        if (0 != this.mUnwantedNetworkCapabilities) {
            sb.append(" Unwanted: ");
            appendStringRepresentationOfBitMaskToStringBuilder(sb, this.mUnwantedNetworkCapabilities, $$Lambda$p1_56lwnt1xBuY1muPblbN1Dtkw.INSTANCE, "&");
        }
        if (this.mLinkUpBandwidthKbps > 0) {
            sb.append(" LinkUpBandwidth>=");
            sb.append(this.mLinkUpBandwidthKbps);
            sb.append("Kbps");
        }
        if (this.mLinkDownBandwidthKbps > 0) {
            sb.append(" LinkDnBandwidth>=");
            sb.append(this.mLinkDownBandwidthKbps);
            sb.append("Kbps");
        }
        if (this.mNetworkSpecifier != null) {
            sb.append(" Specifier: <");
            sb.append(this.mNetworkSpecifier);
            sb.append(">");
        }
        if (this.mTransportInfo != null) {
            sb.append(" TransportInfo: <");
            sb.append(this.mTransportInfo);
            sb.append(">");
        }
        if (hasSignalStrength()) {
            sb.append(" SignalStrength: ");
            sb.append(this.mSignalStrength);
        }
        ArraySet<UidRange> arraySet = this.mUids;
        if (arraySet != null) {
            if (1 == arraySet.size() && this.mUids.valueAt(0).count() == 1) {
                sb.append(" Uid: ");
                sb.append(this.mUids.valueAt(0).start);
            } else {
                sb.append(" Uids: <");
                sb.append(this.mUids);
                sb.append(">");
            }
        }
        if (this.mEstablishingVpnAppUid != -1) {
            sb.append(" EstablishingAppUid: ");
            sb.append(this.mEstablishingVpnAppUid);
        }
        if (this.mSSID != null) {
            sb.append(" SSID: ");
            sb.append(this.mSSID);
        }
        sb.append("]");
        return sb.toString();
    }

    public static void appendStringRepresentationOfBitMaskToStringBuilder(StringBuilder sb, long bitMask, NameOf nameFetcher, String separator) {
        int bitPos = 0;
        boolean firstElementAdded = false;
        while (bitMask != 0) {
            if ((1 & bitMask) != 0) {
                if (firstElementAdded) {
                    sb.append(separator);
                } else {
                    firstElementAdded = true;
                }
                sb.append(nameFetcher.nameOf(bitPos));
            }
            bitMask >>= 1;
            bitPos++;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.util.proto.ProtoOutputStream.write(long, int):void
     arg types: [int, int]
     candidates:
      android.util.proto.ProtoOutputStream.write(long, double):void
      android.util.proto.ProtoOutputStream.write(long, float):void
      android.util.proto.ProtoOutputStream.write(long, long):void
      android.util.proto.ProtoOutputStream.write(long, java.lang.String):void
      android.util.proto.ProtoOutputStream.write(long, boolean):void
      android.util.proto.ProtoOutputStream.write(long, byte[]):void
      android.util.proto.ProtoOutputStream.write(long, int):void */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        for (int transport : getTransportTypes()) {
            proto.write(2259152797697L, transport);
        }
        for (int capability : getCapabilities()) {
            proto.write(2259152797698L, capability);
        }
        proto.write(1120986464259L, this.mLinkUpBandwidthKbps);
        proto.write(1120986464260L, this.mLinkDownBandwidthKbps);
        NetworkSpecifier networkSpecifier = this.mNetworkSpecifier;
        if (networkSpecifier != null) {
            proto.write(1138166333445L, networkSpecifier.toString());
        }
        TransportInfo transportInfo = this.mTransportInfo;
        proto.write(1133871366150L, hasSignalStrength());
        proto.write(NetworkCapabilitiesProto.SIGNAL_STRENGTH, this.mSignalStrength);
        proto.end(token);
    }

    public static String capabilityNamesOf(int[] capabilities) {
        StringJoiner joiner = new StringJoiner("|");
        if (capabilities != null) {
            for (int c : capabilities) {
                joiner.add(capabilityNameOf(c));
            }
        }
        return joiner.toString();
    }

    public static String capabilityNameOf(int capability) {
        if (capability == 30) {
            return "DUALWIFI";
        }
        switch (capability) {
            case 0:
                return "MMS";
            case 1:
                return "SUPL";
            case 2:
                return "DUN";
            case 3:
                return "FOTA";
            case 4:
                return "IMS";
            case 5:
                return "CBS";
            case 6:
                return "WIFI_P2P";
            case 7:
                return "IA";
            case 8:
                return "RCS";
            case 9:
                return "XCAP";
            case 10:
                return "EIMS";
            case 11:
                return "NOT_METERED";
            case 12:
                return "INTERNET";
            case 13:
                return "NOT_RESTRICTED";
            case 14:
                return "TRUSTED";
            case 15:
                return "NOT_VPN";
            case 16:
                return "VALIDATED";
            case 17:
                return "CAPTIVE_PORTAL";
            case 18:
                return "NOT_ROAMING";
            case 19:
                return "FOREGROUND";
            case 20:
                return "NOT_CONGESTED";
            case 21:
                return "NOT_SUSPENDED";
            case 22:
                return "OEM_PAID";
            case 23:
                return "MCX";
            case 24:
                return "PARTIAL_CONNECTIVITY";
            case 25:
                return "WAP";
            case 26:
                return "VSIM";
            case 27:
                return "BIP";
            default:
                return Integer.toString(capability);
        }
    }

    @UnsupportedAppUsage
    public static String transportNamesOf(int[] types) {
        StringJoiner joiner = new StringJoiner("|");
        if (types != null) {
            for (int t : types) {
                joiner.add(transportNameOf(t));
            }
        }
        return joiner.toString();
    }

    public static String transportNameOf(int transport) {
        if (!isValidTransport(transport)) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        return TRANSPORT_NAMES[transport];
    }

    private static void checkValidTransportType(int transport) {
        boolean isValidTransport = isValidTransport(transport);
        Preconditions.checkArgument(isValidTransport, "Invalid TransportType " + transport);
    }

    private static boolean isValidCapability(int capability) {
        return capability >= 0 && capability <= 30;
    }

    private static void checkValidCapability(int capability) {
        boolean isValidCapability = isValidCapability(capability);
        Preconditions.checkArgument(isValidCapability, "NetworkCapability " + capability + "out of range");
    }

    public boolean isMetered() {
        return !hasCapability(11);
    }
}

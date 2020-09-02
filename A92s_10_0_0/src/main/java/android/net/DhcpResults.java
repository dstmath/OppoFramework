package android.net;

import android.annotation.UnsupportedAppUsage;
import android.net.StaticIpConfiguration;
import android.net.shared.InetAddressUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DhcpResults implements Parcelable {
    public static final Parcelable.Creator<DhcpResults> CREATOR = new Parcelable.Creator<DhcpResults>() {
        /* class android.net.DhcpResults.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DhcpResults createFromParcel(Parcel in) {
            return DhcpResults.readFromParcel(in);
        }

        @Override // android.os.Parcelable.Creator
        public DhcpResults[] newArray(int size) {
            return new DhcpResults[size];
        }
    };
    private static final String TAG = "DhcpResults";
    @UnsupportedAppUsage
    public final ArrayList<InetAddress> dnsServers;
    @UnsupportedAppUsage
    public String domains;
    public boolean dupServer;
    @UnsupportedAppUsage
    public InetAddress gateway;
    public boolean internetAccess;
    @UnsupportedAppUsage
    public LinkAddress ipAddress;
    @UnsupportedAppUsage
    public int leaseDuration;
    public long leaseExpiry;
    @UnsupportedAppUsage
    public int mtu;
    @UnsupportedAppUsage
    public Inet4Address serverAddress;
    public String serverHostName;
    public final byte[] serverMac;
    @UnsupportedAppUsage
    public String vendorInfo;

    public DhcpResults() {
        this.dnsServers = new ArrayList<>();
        this.leaseExpiry = 0;
        this.dupServer = false;
        this.internetAccess = false;
        this.serverMac = new byte[6];
    }

    public StaticIpConfiguration toStaticIpConfiguration() {
        return new StaticIpConfiguration.Builder().setIpAddress(this.ipAddress).setGateway(this.gateway).setDnsServers(this.dnsServers).setDomains(this.domains).build();
    }

    public DhcpResults(StaticIpConfiguration source) {
        this.dnsServers = new ArrayList<>();
        this.leaseExpiry = 0;
        this.dupServer = false;
        this.internetAccess = false;
        this.serverMac = new byte[6];
        if (source != null) {
            this.ipAddress = source.getIpAddress();
            this.gateway = source.getGateway();
            this.dnsServers.addAll(source.getDnsServers());
            this.domains = source.getDomains();
        }
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public DhcpResults(DhcpResults source) {
        this(source == null ? null : source.toStaticIpConfiguration());
        if (source != null) {
            this.serverAddress = source.serverAddress;
            this.vendorInfo = source.vendorInfo;
            this.leaseDuration = source.leaseDuration;
            this.mtu = source.mtu;
            this.serverHostName = source.serverHostName;
            this.leaseExpiry = source.leaseExpiry;
            this.dupServer = source.dupServer;
            this.internetAccess = source.internetAccess;
            byte[] bArr = source.serverMac;
            byte[] bArr2 = this.serverMac;
            System.arraycopy(bArr, 0, bArr2, 0, bArr2.length);
        }
    }

    public List<RouteInfo> getRoutes(String iface) {
        return toStaticIpConfiguration().getRoutes(iface);
    }

    public boolean hasMeteredHint() {
        String str = this.vendorInfo;
        if (str != null) {
            return str.contains("ANDROID_METERED");
        }
        return false;
    }

    public void clear() {
        this.ipAddress = null;
        this.gateway = null;
        this.dnsServers.clear();
        this.domains = null;
        this.serverAddress = null;
        this.vendorInfo = null;
        this.leaseDuration = 0;
        this.mtu = 0;
        this.serverHostName = null;
        this.serverAddress = null;
        this.leaseExpiry = 0;
        this.internetAccess = false;
        this.dupServer = false;
    }

    public String toString() {
        StringBuffer str = new StringBuffer(super.toString());
        str.append(" DHCP server ");
        str.append(this.serverAddress);
        str.append(" Vendor info ");
        str.append(this.vendorInfo);
        str.append(" lease ");
        str.append(this.leaseDuration);
        str.append(" seconds");
        if (this.mtu != 0) {
            str.append(" MTU ");
            str.append(this.mtu);
        }
        str.append(" Servername ");
        str.append(this.serverHostName);
        str.append(" dupServer ");
        str.append(this.dupServer);
        str.append(" internetAccess ");
        str.append(this.internetAccess);
        return str.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DhcpResults)) {
            return false;
        }
        DhcpResults target = (DhcpResults) obj;
        if (!toStaticIpConfiguration().equals(target.toStaticIpConfiguration()) || !Objects.equals(this.serverAddress, target.serverAddress) || !Objects.equals(this.vendorInfo, target.vendorInfo) || !Objects.equals(this.serverHostName, target.serverHostName) || this.leaseDuration != target.leaseDuration || this.mtu != target.mtu) {
            return false;
        }
        return true;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        toStaticIpConfiguration().writeToParcel(dest, flags);
        dest.writeInt(this.leaseDuration);
        dest.writeInt(this.mtu);
        InetAddressUtils.parcelInetAddress(dest, this.serverAddress, flags);
        dest.writeString(this.vendorInfo);
        dest.writeString(this.serverHostName);
        dest.writeLong(this.leaseExpiry);
        dest.writeBoolean(this.dupServer);
        dest.writeBoolean(this.internetAccess);
        dest.writeByteArray(this.serverMac);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    /* access modifiers changed from: private */
    public static DhcpResults readFromParcel(Parcel in) {
        DhcpResults dhcpResults = new DhcpResults(StaticIpConfiguration.CREATOR.createFromParcel(in));
        dhcpResults.leaseDuration = in.readInt();
        dhcpResults.mtu = in.readInt();
        dhcpResults.serverAddress = (Inet4Address) InetAddressUtils.unparcelInetAddress(in);
        dhcpResults.vendorInfo = in.readString();
        dhcpResults.serverHostName = in.readString();
        dhcpResults.leaseExpiry = in.readLong();
        dhcpResults.dupServer = in.readBoolean();
        dhcpResults.internetAccess = in.readBoolean();
        in.readByteArray(dhcpResults.serverMac);
        return dhcpResults;
    }

    public boolean setIpAddress(String addrString, int prefixLength) {
        try {
            this.ipAddress = new LinkAddress((Inet4Address) InetAddresses.parseNumericAddress(addrString), prefixLength);
            return false;
        } catch (ClassCastException | IllegalArgumentException e) {
            Log.e(TAG, "setIpAddress failed with addrString " + addrString + "/" + prefixLength);
            return true;
        }
    }

    public boolean setGateway(String addrString) {
        try {
            this.gateway = InetAddresses.parseNumericAddress(addrString);
            return false;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "setGateway failed with addrString " + addrString);
            return true;
        }
    }

    public boolean addDns(String addrString) {
        if (TextUtils.isEmpty(addrString)) {
            return false;
        }
        try {
            this.dnsServers.add(InetAddresses.parseNumericAddress(addrString));
            return false;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "addDns failed with addrString " + addrString);
            return true;
        }
    }

    public LinkAddress getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(LinkAddress ipAddress2) {
        this.ipAddress = ipAddress2;
    }

    public InetAddress getGateway() {
        return this.gateway;
    }

    public void setGateway(InetAddress gateway2) {
        this.gateway = gateway2;
    }

    public List<InetAddress> getDnsServers() {
        return this.dnsServers;
    }

    public void addDnsServer(InetAddress server) {
        this.dnsServers.add(server);
    }

    public String getDomains() {
        return this.domains;
    }

    public void setDomains(String domains2) {
        this.domains = domains2;
    }

    public Inet4Address getServerAddress() {
        return this.serverAddress;
    }

    public void setServerAddress(Inet4Address addr) {
        this.serverAddress = addr;
    }

    public int getLeaseDuration() {
        return this.leaseDuration;
    }

    public void setLeaseDuration(int duration) {
        this.leaseDuration = duration;
    }

    public String getVendorInfo() {
        return this.vendorInfo;
    }

    public void setVendorInfo(String info) {
        this.vendorInfo = info;
    }

    public int getMtu() {
        return this.mtu;
    }

    public void setMtu(int mtu2) {
        this.mtu = mtu2;
    }
}

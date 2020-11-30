package android.net.ip;

import android.net.DhcpResults;
import android.net.LinkProperties;

public class IpClientCallbacks {
    public void onIpClientCreated(IIpClient ipClient) {
    }

    public void onPreDhcpAction() {
    }

    public void onPostDhcpAction() {
    }

    public void onNewDhcpResults(DhcpResults dhcpResults) {
    }

    public void onProvisioningSuccess(LinkProperties newLp) {
    }

    public void onProvisioningFailure(LinkProperties newLp) {
    }

    public void onLinkPropertiesChange(LinkProperties newLp) {
    }

    public void onReachabilityLost(String logMsg) {
    }

    public void onQuit() {
    }

    public void installPacketFilter(byte[] filter) {
    }

    public void startReadPacketFilter() {
    }

    public void setFallbackMulticastFilter(boolean enabled) {
    }

    public void onDhcpRenewCount() {
    }

    public void setNeighborDiscoveryOffload(boolean enable) {
    }

    public void onFindDupServer(String server) {
    }

    public void onUpdateLeaseExpriy(long time) {
    }

    public void onSwitchServerFailure(String server) {
    }

    public void onFixServerFailure(String server) {
    }

    public void onDoDupArp(String ifaceName, int myAddress, int target) {
    }

    public void onDoGatewayDetect(String ifaceName, int myAddress, int target) {
    }

    public void onDhcpStateChange(DhcpResults dhcpResults) {
    }
}
